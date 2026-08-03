import { extractApiMessage, isAuthFailure } from './apiError';

/** Shapes an axios-style failure carrying a backend Response body. */
const httpFailure = (status, data) => {
    const error = new Error(`Request failed with status code ${status}`);
    error.isAxiosError = true;
    error.response = { status, data, headers: {} };
    return error;
};

describe('extractApiMessage', () => {
    test('prefersBackendMessageOverAxiosMessage', () => {
        const productHistoryConflict = httpFailure(409, {
            statusCode: 409,
            message: 'Product cannot be deleted because it has stock transaction history.'
        });
        expect(extractApiMessage(productHistoryConflict, 'Silme başarısız.'))
            .toBe('Product cannot be deleted because it has stock transaction history.');

        const insufficientStock = httpFailure(422, {
            statusCode: 422,
            message: 'Insufficient stock for product: Laptop. Available: 2, Requested: 5'
        });
        expect(extractApiMessage(insufficientStock, 'Transaction eklenemedi.'))
            .toBe('Insufficient stock for product: Laptop. Available: 2, Requested: 5');

        const databaseConflict = httpFailure(409, {
            statusCode: 409,
            message: 'The operation conflicts with existing data.'
        });
        expect(extractApiMessage(databaseConflict, 'Delete failed.'))
            .toBe('The operation conflicts with existing data.');

        const serverFailure = httpFailure(500, {
            statusCode: 500,
            message: 'An unexpected error occurred.'
        });
        expect(extractApiMessage(serverFailure, 'Failed to load dashboard data. Please try again.'))
            .toBe('An unexpected error occurred.');
    });

    test('usesValidationDetailsForBadRequest', () => {
        // The backend currently flattens field errors into `message`.
        const flattened = httpFailure(400, {
            statusCode: 400,
            message: 'Validation failed: sku: SKU is required; price: must be greater than 0'
        });
        expect(extractApiMessage(flattened, 'Ürün eklenemedi.'))
            .toBe('Validation failed: sku: SKU is required; price: must be greater than 0');

        // A field map is rendered rather than dropped, so a future backend shape still reads well.
        const fieldMap = httpFailure(400, {
            statusCode: 400,
            errors: { sku: 'SKU is required', price: 'must be greater than 0' }
        });
        expect(extractApiMessage(fieldMap, 'Ürün eklenemedi.'))
            .toBe('sku: SKU is required; price: must be greater than 0');

        const fieldList = httpFailure(400, {
            statusCode: 400,
            errors: ['SKU is required', 'price must be greater than 0']
        });
        expect(extractApiMessage(fieldList, 'Ürün eklenemedi.'))
            .toBe('SKU is required; price must be greater than 0');
    });

    test('usesNetworkMessageWhenNoResponseExists', () => {
        const networkFailure = new Error('Network Error');
        networkFailure.isAxiosError = true;

        expect(extractApiMessage(networkFailure, 'Veriler yüklenemedi.')).toBe('Network Error');

        const timeout = new Error('timeout of 5000ms exceeded');
        expect(extractApiMessage(timeout, 'Veriler yüklenemedi.')).toBe('timeout of 5000ms exceeded');
    });

    test('usesFallbackWhenNoUsefulMessageExists', () => {
        // A response arrived but carried nothing readable: the caller's own wording wins
        // over axios boilerplate like "Request failed with status code 500".
        expect(extractApiMessage(httpFailure(500, {}), 'Veriler yüklenemedi.'))
            .toBe('Veriler yüklenemedi.');
        expect(extractApiMessage(httpFailure(503, { statusCode: 503, message: '   ' }), 'Delete failed.'))
            .toBe('Delete failed.');
        expect(extractApiMessage(undefined, 'Operation failed.')).toBe('Operation failed.');
        expect(extractApiMessage(null, 'Operation failed.')).toBe('Operation failed.');
    });
});

describe('isAuthFailure', () => {
    test('detectsUnauthorizedAndForbiddenByStatus', () => {
        expect(isAuthFailure(httpFailure(401, { message: 'Authentication is required to access this resource.' }))).toBe(true);
        expect(isAuthFailure(httpFailure(403, { message: 'You do not have permission to access this resource.' }))).toBe(true);
    });

    test('ignoresOtherFailuresIncludingOnesWithoutAMessage', () => {
        expect(isAuthFailure(httpFailure(409, {}))).toBe(false);
        expect(isAuthFailure(httpFailure(422, {}))).toBe(false);
        expect(isAuthFailure(httpFailure(500, {}))).toBe(false);

        // Previously this path substring-matched err.message and threw when it was absent.
        const messageless = { isAxiosError: true };
        expect(isAuthFailure(messageless)).toBe(false);
        expect(isAuthFailure(new Error('Network Error'))).toBe(false);
        expect(isAuthFailure(undefined)).toBe(false);
    });
});
