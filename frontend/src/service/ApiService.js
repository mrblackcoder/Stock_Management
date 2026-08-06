import axios from "axios";
import CryptoJS from "crypto-js";

/**
 * Protected traffic. Carries the 401 -> refresh -> retry interceptor.
 */
export const apiClient = axios.create();

/**
 * Authentication traffic: login, register and the refresh call itself.
 *
 * Deliberately interceptor-free. When the refresh request shared the intercepted
 * client, its own 401 re-entered the interceptor, saw isRefreshing === true and
 * parked itself in the queue that only its own completion could drain - the
 * refresh awaited a promise that could never settle, so the flag stayed true and
 * every later 401 hung behind it. Separating the clients makes that impossible by
 * construction rather than by flag bookkeeping.
 */
export const authClient = axios.create();

// Guards a single in-flight refresh; requests that arrive during it wait here.
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
    // Detach first so a queued rejection handler cannot observe a stale queue.
    const pending = failedQueue;
    failedQueue = [];
    pending.forEach(({ resolve, reject }) => {
        if (error) {
            reject(error);
        } else {
            resolve(token);
        }
    });
};

const redirectToLogin = () => {
    if (typeof window !== "undefined" && window.location &&
        !String(window.location.pathname).includes("/login")) {
        window.location.href = "/login";
    }
};

/** Terminal failure: the session cannot be recovered, so drop it and send the user to login. */
const failAuthentication = (error) => {
    ApiService.clearAuth();
    redirectToLogin();
    return Promise.reject(error);
};

const withAuthorization = (config, token) => {
    config.headers = { ...(config.headers || {}), Authorization: `Bearer ${token}` };
    return config;
};

apiClient.interceptors.response.use(
    response => response,
    async error => {
        const originalRequest = error.config;

        if (error.response?.status !== 401 || !originalRequest) {
            return Promise.reject(error);
        }

        // Already retried with a freshly minted token and still refused: the account
        // is disabled or otherwise unusable. One attempt, then a hard logout - no
        // message matching, no second refresh, no loop.
        if (originalRequest._retry) {
            return failAuthentication(error);
        }

        // Read the refresh token before claiming the refresh slot, so the
        // nothing-to-refresh case can never leave isRefreshing stuck true.
        const refreshToken = ApiService.getRefreshToken();
        if (!refreshToken) {
            return failAuthentication(error);
        }

        if (isRefreshing) {
            originalRequest._retry = true;
            return new Promise((resolve, reject) => {
                failedQueue.push({ resolve, reject });
            }).then(token => apiClient(withAuthorization(originalRequest, token)));
        }

        originalRequest._retry = true;
        isRefreshing = true;

        try {
            const { data } = await authClient.post(`${ApiService.BASE_URL}/auth/refresh-token`, {
                refreshToken: refreshToken
            });

            const newToken = data?.token;
            if (!newToken) {
                throw new Error("Refresh response did not contain an access token");
            }

            ApiService.saveToken(newToken);
            if (data.refreshToken) {
                ApiService.saveRefreshToken(data.refreshToken);
            }

            processQueue(null, newToken);
            return apiClient(withAuthorization(originalRequest, newToken));
        } catch (refreshError) {
            processQueue(refreshError, null);
            return failAuthentication(refreshError);
        } finally {
            isRefreshing = false;
        }
    }
);

export default class ApiService {

    static BASE_URL = process.env.REACT_APP_API_URL || "http://localhost:8080/api";
    /**
     * Key used to obfuscate values kept in localStorage.
     *
     * Public by design: every REACT_APP_* value is compiled into the browser bundle,
     * so this key ships to the client and anyone can read it. It raises the effort of
     * casually eyeballing localStorage - it is not a security boundary, and it stops
     * nothing that has script access to the page. The real controls are server-side:
     * short-lived access tokens, and authorization checked on every request.
     */
    static STORAGE_OBFUSCATION_KEY =
        process.env.REACT_APP_ENCRYPTION_KEY || "ims-secure-key-2024-stock-mgmt";

    /** Obfuscates a stored value. Not encryption in any meaningful sense - see above. */
    static obfuscate(data) {
        return CryptoJS.AES.encrypt(data, this.STORAGE_OBFUSCATION_KEY).toString();
    }

    static deobfuscate(data) {
        try {
            const bytes = CryptoJS.AES.decrypt(data, this.STORAGE_OBFUSCATION_KEY);
            return bytes.toString(CryptoJS.enc.Utf8);
        } catch (e) {
            return null;
        }
    }

    // Check if JWT token is expired
    static isTokenExpired(token) {
        if (!token) return true;
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            // Token expiry time'ı kontrol et (5 dakika buffer)
            return (payload.exp * 1000) < (Date.now() + 5 * 60 * 1000);
        } catch (e) {
            return true;
        }
    }

    // Save token (obfuscated in localStorage)
    static saveToken(token) {
        const storedValue = this.obfuscate(token);
        localStorage.setItem("token", storedValue);
    }

    // Retrieve the token (returns null if expired)
    static getToken() {
        const storedValue = localStorage.getItem("token");
        if (!storedValue) return null;

        const token = this.deobfuscate(storedValue);
        if (!token || this.isTokenExpired(token)) {
            // Locally expired only. The refresh token is deliberately preserved:
            // clearing it here destroyed the very credential the 401 handler needs,
            // which made refresh unreachable for its main case.
            return null;
        }
        return token;
    }
    
    // Get token without expiry check (for specific cases)
    static getRawToken() {
        const storedValue = localStorage.getItem("token");
        if (!storedValue) return null;
        return this.deobfuscate(storedValue);
    }
    
    // Save refresh token
    static saveRefreshToken(refreshToken) {
        const storedValue = this.obfuscate(refreshToken);
        localStorage.setItem("refreshToken", storedValue);
    }
    
    // Get refresh token
    static getRefreshToken() {
        const storedValue = localStorage.getItem("refreshToken");
        if (!storedValue) return null;
        return this.deobfuscate(storedValue);
    }

    // Save role (obfuscated in localStorage)
    static saveRole(role) {
        const storedRole = this.obfuscate(role);
        localStorage.setItem("role", storedRole);
    }

    // Retrieve the role
    static getRole() {
        const storedRole = localStorage.getItem("role");
        if (!storedRole) return null;
        return this.deobfuscate(storedRole);
    }

    // Save username
    static saveUsername(username) {
        const storedUsername = this.obfuscate(username);
        localStorage.setItem("username", storedUsername);
    }

    // Get username
    static getUsername() {
        const storedUsername = localStorage.getItem("username");
        if (!storedUsername) return null;
        return this.deobfuscate(storedUsername);
    }

    // Save user object
    static saveUser(user) {
        const storedUser = this.obfuscate(JSON.stringify(user));
        localStorage.setItem("user", storedUser);
    }

    // Get user object
    static getUser() {
        const storedUser = localStorage.getItem("user");
        if (!storedUser) return null;
        try {
            return JSON.parse(this.deobfuscate(storedUser));
        } catch (e) {
            return null;
        }
    }

    static clearAuth() {
        localStorage.removeItem("token");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("role");
        localStorage.removeItem("username");
        localStorage.removeItem("user");
    }

    static getHeader() {
        const headers = { "Content-Type": "application/json" };

        // Only send Authorization when there is a usable token. Sending
        // "Bearer null" made an expired session look like a malformed token.
        const token = this.getToken();
        if (token) {
            headers.Authorization = `Bearer ${token}`;
        }
        return headers;
    }

    /** AUTH API */

    // Authentication calls use the interceptor-free client: a rejected login or
    // register must reach its caller unchanged and must never start a token refresh.
    static async registerUser(registerData) {
        const response = await authClient.post(`${this.BASE_URL}/auth/register`, registerData);
        return response.data;
    }

    static async loginUser(loginData) {
        const response = await authClient.post(`${this.BASE_URL}/auth/login`, loginData);
        return response.data;
    }

    static async logout() {
        try {
            await apiClient.post(`${this.BASE_URL}/auth/logout`, {}, {
                headers: this.getHeader()
            });
        } catch (e) {
            // Ignore errors during logout
        }
        this.clearAuth();
    }
    
    static async refreshToken() {
        const refreshToken = this.getRefreshToken();
        if (!refreshToken) {
            throw new Error("No refresh token available");
        }
        const response = await authClient.post(`${this.BASE_URL}/auth/refresh-token`, {
            refreshToken: refreshToken
        });
        return response.data;
    }

    /** GENERIC HTTP METHODS */

    static async get(url) {
        const response = await apiClient.get(`${this.BASE_URL}${url}`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    /** USER API */

    static async getAllUsers() {
        const response = await apiClient.get(`${this.BASE_URL}/users`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async getUserProfile() {
        const response = await apiClient.get(`${this.BASE_URL}/users/profile`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    /** PRODUCT API */

    static async getAllProducts(page = 0, size = 10, sortBy = "id") {
        const response = await apiClient.get(`${this.BASE_URL}/products?page=${page}&size=${size}&sortBy=${sortBy}`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async getProductById(productId) {
        const response = await apiClient.get(`${this.BASE_URL}/products/${productId}`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async createProduct(productData) {
        const response = await apiClient.post(`${this.BASE_URL}/products`, productData, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async updateProduct(productId, productData) {
        const response = await apiClient.put(`${this.BASE_URL}/products/${productId}`, productData, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async deleteProduct(productId) {
        const response = await apiClient.delete(`${this.BASE_URL}/products/${productId}`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async searchProducts(keyword) {
        const response = await apiClient.get(`${this.BASE_URL}/products/search?keyword=${keyword}`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async getLowStockProducts() {
        const response = await apiClient.get(`${this.BASE_URL}/products/low-stock`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    /** CATEGORY API */

    static async getAllCategories() {
        const response = await apiClient.get(`${this.BASE_URL}/categories`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async getCategoryById(categoryId) {
        const response = await apiClient.get(`${this.BASE_URL}/categories/${categoryId}`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async createCategory(categoryData) {
        const response = await apiClient.post(`${this.BASE_URL}/categories`, categoryData, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async updateCategory(categoryId, categoryData) {
        const response = await apiClient.put(`${this.BASE_URL}/categories/${categoryId}`, categoryData, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async deleteCategory(categoryId) {
        const response = await apiClient.delete(`${this.BASE_URL}/categories/${categoryId}`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    /** SUPPLIER API */

    static async getAllSuppliers() {
        const response = await apiClient.get(`${this.BASE_URL}/suppliers`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async getSupplierById(supplierId) {
        const response = await apiClient.get(`${this.BASE_URL}/suppliers/${supplierId}`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async createSupplier(supplierData) {
        const response = await apiClient.post(`${this.BASE_URL}/suppliers`, supplierData, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async updateSupplier(supplierId, supplierData) {
        const response = await apiClient.put(`${this.BASE_URL}/suppliers/${supplierId}`, supplierData, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async deleteSupplier(supplierId) {
        const response = await apiClient.delete(`${this.BASE_URL}/suppliers/${supplierId}`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    /** STOCK TRANSACTION API */

    static async getAllTransactions() {
        const response = await apiClient.get(`${this.BASE_URL}/transactions`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async getTransactionById(transactionId) {
        const response = await apiClient.get(`${this.BASE_URL}/transactions/${transactionId}`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async createTransaction(transactionData) {
        const response = await apiClient.post(`${this.BASE_URL}/transactions`, transactionData, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async updateTransaction(transactionId, transactionData) {
        const response = await apiClient.put(`${this.BASE_URL}/transactions/${transactionId}`, transactionData, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async deleteTransaction(transactionId) {
        const response = await apiClient.delete(`${this.BASE_URL}/transactions/${transactionId}`, {
            headers: this.getHeader()
        });
        return response.data;
    }
}
