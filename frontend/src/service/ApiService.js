import axios from "axios";
import CryptoJS from "crypto-js";

export default class ApiService {

    static BASE_URL = "http://localhost:8080/api";
    static ENCRYPTION_KEY = "ims-inventory-management";

    // Encrypt data using CryptoJS
    static encrypt(data) {
        return CryptoJS.AES.encrypt(data, this.ENCRYPTION_KEY).toString();
    }

    // Decrypt data using CryptoJS
    static decrypt(data) {
        const bytes = CryptoJS.AES.decrypt(data, this.ENCRYPTION_KEY);
        return bytes.toString(CryptoJS.enc.Utf8);
    }

    // Save token with encryption
    static saveToken(token) {
        const encryptedToken = this.encrypt(token);
        localStorage.setItem("token", encryptedToken);
    }

    // Retrieve the token
    static getToken() {
        const encryptedToken = localStorage.getItem("token");
        if (!encryptedToken) return null;
        return this.decrypt(encryptedToken);
    }

    // Save role with encryption
    static saveRole(role) {
        const encryptedRole = this.encrypt(role);
        localStorage.setItem("role", encryptedRole);
    }

    // Retrieve the role
    static getRole() {
        const encryptedRole = localStorage.getItem("role");
        if (!encryptedRole) return null;
        return this.decrypt(encryptedRole);
    }

    // Save username
    static saveUsername(username) {
        const encryptedUsername = this.encrypt(username);
        localStorage.setItem("username", encryptedUsername);
    }

    // Get username
    static getUsername() {
        const encryptedUsername = localStorage.getItem("username");
        if (!encryptedUsername) return null;
        return this.decrypt(encryptedUsername);
    }

    // Save user object
    static saveUser(user) {
        const encryptedUser = this.encrypt(JSON.stringify(user));
        localStorage.setItem("user", encryptedUser);
    }

    // Get user object
    static getUser() {
        const encryptedUser = localStorage.getItem("user");
        if (!encryptedUser) return null;
        try {
            return JSON.parse(this.decrypt(encryptedUser));
        } catch (e) {
            return null;
        }
    }

    static clearAuth() {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        localStorage.removeItem("username");
        localStorage.removeItem("user");
    }

    static getHeader() {
        const token = this.getToken();
        return {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json"
        };
    }

    /** AUTH API */

    static async registerUser(registerData) {
        const response = await axios.post(`${this.BASE_URL}/auth/register`, registerData);
        return response.data;
    }

    static async loginUser(loginData) {
        const response = await axios.post(`${this.BASE_URL}/auth/login`, loginData);
        return response.data;
    }

    static logout() {
        this.clearAuth();
    }

    /** GENERIC HTTP METHODS */

    static async get(url) {
        const response = await axios.get(`${this.BASE_URL}${url}`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    /** USER API */

    static async getAllUsers() {
        const response = await axios.get(`${this.BASE_URL}/users/all`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async getUserProfile() {
        const response = await axios.get(`${this.BASE_URL}/users/profile`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    /** PRODUCT API */

    static async getAllProducts(page = 0, size = 10, sortBy = "id") {
        const response = await axios.get(`${this.BASE_URL}/products?page=${page}&size=${size}&sortBy=${sortBy}`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async getProductById(productId) {
        const response = await axios.get(`${this.BASE_URL}/products/${productId}`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async createProduct(productData) {
        const response = await axios.post(`${this.BASE_URL}/products`, productData, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async updateProduct(productId, productData) {
        const response = await axios.put(`${this.BASE_URL}/products/${productId}`, productData, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async deleteProduct(productId) {
        const response = await axios.delete(`${this.BASE_URL}/products/${productId}`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async searchProducts(keyword) {
        const response = await axios.get(`${this.BASE_URL}/products/search?keyword=${keyword}`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async getLowStockProducts() {
        const response = await axios.get(`${this.BASE_URL}/products/low-stock`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    /** CATEGORY API */

    static async getAllCategories() {
        const response = await axios.get(`${this.BASE_URL}/categories`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async getCategoryById(categoryId) {
        const response = await axios.get(`${this.BASE_URL}/categories/${categoryId}`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async createCategory(categoryData) {
        const response = await axios.post(`${this.BASE_URL}/categories`, categoryData, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async updateCategory(categoryId, categoryData) {
        const response = await axios.put(`${this.BASE_URL}/categories/${categoryId}`, categoryData, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async deleteCategory(categoryId) {
        const response = await axios.delete(`${this.BASE_URL}/categories/${categoryId}`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    /** SUPPLIER API */

    static async getAllSuppliers() {
        const response = await axios.get(`${this.BASE_URL}/suppliers`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async getSupplierById(supplierId) {
        const response = await axios.get(`${this.BASE_URL}/suppliers/${supplierId}`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async createSupplier(supplierData) {
        const response = await axios.post(`${this.BASE_URL}/suppliers`, supplierData, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async updateSupplier(supplierId, supplierData) {
        const response = await axios.put(`${this.BASE_URL}/suppliers/${supplierId}`, supplierData, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async deleteSupplier(supplierId) {
        const response = await axios.delete(`${this.BASE_URL}/suppliers/${supplierId}`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    /** STOCK TRANSACTION API */

    static async getAllTransactions() {
        const response = await axios.get(`${this.BASE_URL}/transactions`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async getTransactionById(transactionId) {
        const response = await axios.get(`${this.BASE_URL}/transactions/${transactionId}`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async createTransaction(transactionData) {
        const user = this.getUser();
        const dataWithUserId = {
            ...transactionData,
            userId: user?.id || 1 // Fallback to 1 if user not found
        };
        const response = await axios.post(`${this.BASE_URL}/transactions`, dataWithUserId, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async updateTransaction(transactionId, transactionData) {
        const response = await axios.put(`${this.BASE_URL}/transactions/${transactionId}`, transactionData, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async deleteTransaction(transactionId) {
        const response = await axios.delete(`${this.BASE_URL}/transactions/${transactionId}`, {
            headers: this.getHeader()
        });
        return response.data;
    }
}

