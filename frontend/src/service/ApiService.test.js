/**
 * Drives the real axios interceptor chain.
 *
 * Nothing here mocks axios itself: each test swaps `defaults.adapter` on the two
 * exported clients, so the genuine request/response interceptors run and the
 * refresh/retry logic is exercised exactly as it is in the browser.
 *
 * Module-scoped state (isRefreshing, failedQueue) is reset by re-requiring the
 * module under jest.resetModules() rather than by any production reset hook.
 */

const BASE_URL = 'http://localhost:8080/api';

// A promise that never settles would hang Jest; every await is raced against this
// so the pre-fix deadlock fails in ~2s instead of stalling the suite.
const DEADLINE_MS = 2000;

const withDeadline = (promise, label) =>
    Promise.race([
        promise,
        new Promise((_, reject) =>
            setTimeout(() => reject(new Error(`deadlock: ${label} never settled`)), DEADLINE_MS)
        )
    ]);

const httpError = (status, config, data = {}) => {
    const error = new Error(`Request failed with status code ${status}`);
    error.config = config;
    error.isAxiosError = true;
    error.response = { status, data, config, headers: {}, statusText: '' };
    return Promise.reject(error);
};

const httpOk = (config, data = {}) =>
    Promise.resolve({ status: 200, data, config, headers: {}, statusText: 'OK' });

/** A structurally valid, far-future JWT so isTokenExpired() reports it as usable. */
const validJwt = (exp = Math.floor(Date.now() / 1000) + 3600) =>
    `header.${btoa(JSON.stringify({ sub: 'tester', exp }))}.signature`;

describe('ApiService', () => {
    let ApiService;
    let apiClient;
    let authClient;
    let originalLocation;

    beforeEach(() => {
        localStorage.clear();
        jest.resetModules();

        originalLocation = window.location;
        Object.defineProperty(window, 'location', {
            configurable: true,
            writable: true,
            // href must stay absolute: axios reads window.location.href through
            // isURLSameOrigin and a relative value makes `new URL(...)` throw.
            value: {
                origin: 'http://localhost',
                protocol: 'http:',
                host: 'localhost',
                pathname: '/products',
                href: 'http://localhost/products',
                assign: jest.fn()
            }
        });

        // eslint-disable-next-line global-require
        const module = require('./ApiService');
        ApiService = module.default;
        apiClient = module.apiClient;
        authClient = module.authClient;
    });

    afterEach(() => {
        Object.defineProperty(window, 'location', {
            configurable: true,
            writable: true,
            value: originalLocation
        });
    });

    test('getAllUsersUsesTheUsersEndpoint', async () => {
        const requests = [];
        apiClient.defaults.adapter = config => {
            requests.push(config);
            return httpOk(config, { statusCode: 200, userList: [] });
        };

        await withDeadline(ApiService.getAllUsers(), 'getAllUsers');

        expect(requests).toHaveLength(1);
        expect(requests[0].url).toBe(`${BASE_URL}/users`);
        expect(requests[0].url).not.toContain('/users/all');
    });

    test('refreshFailureRejectsQueuedRequestsWithoutHanging', async () => {
        ApiService.saveToken(validJwt());
        ApiService.saveRefreshToken('stored-refresh-token');

        let refreshCalls = 0;
        apiClient.defaults.adapter = config => httpError(401, config, {
            statusCode: 401,
            message: 'Authentication is required to access this resource.'
        });
        authClient.defaults.adapter = config => {
            refreshCalls += 1;
            return httpError(401, config, { statusCode: 401, message: 'Invalid refresh token' });
        };

        const settled = await withDeadline(
            Promise.allSettled([ApiService.getAllProducts(), ApiService.getAllCategories()]),
            'two protected requests during a failing refresh'
        );

        expect(settled.map(result => result.status)).toEqual(['rejected', 'rejected']);
        expect(refreshCalls).toBe(1);

        // Authentication was dropped, so nothing is left half-signed-in.
        expect(localStorage.getItem('token')).toBeNull();
        expect(localStorage.getItem('refreshToken')).toBeNull();

        // The refresh slot was released: a later 401 fails fast instead of queueing
        // behind a stale isRefreshing flag forever.
        await expect(
            withDeadline(ApiService.getAllSuppliers(), 'request issued after a failed refresh')
        ).rejects.toThrow(/status code 401/);
        expect(refreshCalls).toBe(1);
    });

    test('concurrentUnauthorizedRequestsShareOneRefreshAttempt', async () => {
        ApiService.saveToken(validJwt());
        ApiService.saveRefreshToken('stored-refresh-token');

        const attemptsByUrl = {};
        const authorizationByAttempt = [];
        let refreshCalls = 0;

        apiClient.defaults.adapter = config => {
            attemptsByUrl[config.url] = (attemptsByUrl[config.url] || 0) + 1;
            if (attemptsByUrl[config.url] === 1) {
                return httpError(401, config, { statusCode: 401, message: 'Authentication is invalid.' });
            }
            authorizationByAttempt.push(config.headers.Authorization);
            return httpOk(config, { statusCode: 200 });
        };
        authClient.defaults.adapter = config => {
            refreshCalls += 1;
            return httpOk(config, { statusCode: 200, token: 'new-access-token' });
        };

        await withDeadline(
            Promise.all([
                ApiService.getAllProducts(),
                ApiService.getAllCategories(),
                ApiService.getAllSuppliers()
            ]),
            'three concurrent protected requests'
        );

        expect(refreshCalls).toBe(1);
        expect(authorizationByAttempt).toHaveLength(3);
        authorizationByAttempt.forEach(header => expect(header).toBe('Bearer new-access-token'));
    });

    test('successfulRefreshRetriesOriginalRequestsWithTheNewToken', async () => {
        ApiService.saveToken(validJwt());
        ApiService.saveRefreshToken('stored-refresh-token');

        const attempts = [];
        let refreshCalls = 0;

        apiClient.defaults.adapter = config => {
            attempts.push(config);
            if (attempts.length === 1) {
                return httpError(401, config, { statusCode: 401 });
            }
            return httpOk(config, { statusCode: 201, message: 'Transaction created successfully' });
        };
        authClient.defaults.adapter = config => {
            refreshCalls += 1;
            return httpOk(config, {
                statusCode: 200,
                token: 'new-access-token',
                refreshToken: 'rotated-refresh-token'
            });
        };

        const payload = { productId: 7, transactionType: 'PURCHASE', quantity: 3 };
        const result = await withDeadline(ApiService.createTransaction(payload), 'createTransaction');

        expect(result.statusCode).toBe(201);
        expect(refreshCalls).toBe(1);
        expect(attempts).toHaveLength(2);

        // The retry is the same call, not a reconstructed one.
        const [first, retry] = attempts;
        expect(retry.method).toBe(first.method);
        expect(retry.url).toBe(`${BASE_URL}/transactions`);
        expect(JSON.parse(retry.data)).toEqual(payload);
        expect(retry.headers.Authorization).toBe('Bearer new-access-token');

        expect(ApiService.getRawToken()).toBe('new-access-token');
        // A refresh token returned by the backend is stored; this PR does not rotate it itself.
        expect(ApiService.getRefreshToken()).toBe('rotated-refresh-token');
    });

    test('missingRefreshTokenClearsAuthWithoutStrandingTheRefreshSlot', async () => {
        ApiService.saveToken(validJwt());

        let refreshCalls = 0;
        apiClient.defaults.adapter = config => httpError(401, config, { statusCode: 401 });
        authClient.defaults.adapter = config => {
            refreshCalls += 1;
            return httpOk(config, { statusCode: 200, token: 'unexpected' });
        };

        await expect(
            withDeadline(ApiService.getAllProducts(), 'first 401 without a refresh token')
        ).rejects.toThrow(/status code 401/);

        // Second request must fail just as quickly: isRefreshing was never claimed.
        await expect(
            withDeadline(ApiService.getAllCategories(), 'second 401 without a refresh token')
        ).rejects.toThrow(/status code 401/);

        expect(refreshCalls).toBe(0);
        expect(localStorage.getItem('token')).toBeNull();
    });

    test('retriedRequestThatIsStillUnauthorizedLogsOutInsteadOfLooping', async () => {
        ApiService.saveToken(validJwt());
        ApiService.saveRefreshToken('stored-refresh-token');

        let protectedCalls = 0;
        let refreshCalls = 0;
        apiClient.defaults.adapter = config => {
            protectedCalls += 1;
            return httpError(401, config, { statusCode: 401, message: 'Authentication is invalid.' });
        };
        authClient.defaults.adapter = config => {
            refreshCalls += 1;
            return httpOk(config, { statusCode: 200, token: 'new-access-token' });
        };

        await expect(
            withDeadline(ApiService.getAllProducts(), 'disabled-account style repeated 401')
        ).rejects.toThrow(/status code 401/);

        expect(refreshCalls).toBe(1);
        expect(protectedCalls).toBe(2); // original + one retry, then a hard stop
        expect(localStorage.getItem('token')).toBeNull();
    });

    test('invalidLoginDoesNotTriggerTokenRefresh', async () => {
        ApiService.saveRefreshToken('stored-refresh-token');

        const authRequests = [];
        authClient.defaults.adapter = config => {
            authRequests.push(config);
            return httpError(401, config, { statusCode: 401, message: 'Invalid username or password.' });
        };
        apiClient.defaults.adapter = jest.fn(config => httpOk(config, {}));

        await expect(
            withDeadline(ApiService.loginUser({ username: 'a', password: 'b' }), 'loginUser')
        ).rejects.toMatchObject({ response: { status: 401 } });

        // Exactly the login call: no refresh-token request was provoked.
        expect(authRequests).toHaveLength(1);
        expect(authRequests[0].url).toBe(`${BASE_URL}/auth/login`);
        expect(apiClient.defaults.adapter).not.toHaveBeenCalled();
        // A failed login must not destroy an existing session either.
        expect(ApiService.getRefreshToken()).toBe('stored-refresh-token');
    });

    test('locallyExpiredAccessTokenPreservesTheRefreshTokenAndSendsNoBearerNull', async () => {
        // Expired far enough in the past to be outside the five-minute buffer.
        ApiService.saveToken(validJwt(Math.floor(Date.now() / 1000) - 3600));
        ApiService.saveRefreshToken('stored-refresh-token');

        expect(ApiService.getToken()).toBeNull();
        expect(ApiService.getRefreshToken()).toBe('stored-refresh-token');

        const requests = [];
        apiClient.defaults.adapter = config => {
            requests.push(config);
            return httpOk(config, { statusCode: 200, productList: [] });
        };

        await withDeadline(ApiService.getAllProducts(), 'request with a locally expired token');

        expect(requests).toHaveLength(1);
        expect(requests[0].headers.Authorization).toBeUndefined();
        expect(JSON.stringify(requests[0].headers)).not.toContain('Bearer null');
    });
});
