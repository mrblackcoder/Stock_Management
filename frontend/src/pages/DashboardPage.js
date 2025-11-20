import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ApiService from '../service/ApiService';
import './DashboardPage.css';

function DashboardPage() {
    const navigate = useNavigate();
    const [stats, setStats] = useState({
        totalProducts: 0,
        totalCategories: 0,
        totalSuppliers: 0,
        lowStockProducts: 0,
        totalTransactions: 0
    });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [recentProducts, setRecentProducts] = useState([]);
    const [lowStockItems, setLowStockItems] = useState([]);

    useEffect(() => {
        fetchDashboardData();
    }, []);

    const fetchDashboardData = async () => {
        try {
            setLoading(true);

            // Fetch all data with correct API endpoints
            const [productsRes, categoriesRes, suppliersRes, transactionsRes] = await Promise.all([
                ApiService.get('/products'),
                ApiService.get('/categories'),
                ApiService.get('/suppliers'),
                ApiService.get('/transactions')
            ]);

            // Get low stock products
            const lowStockRes = await ApiService.get('/products/low-stock');

            setStats({
                totalProducts: productsRes.data?.productList?.length || 0,
                totalCategories: categoriesRes.data?.categoryList?.length || 0,
                totalSuppliers: suppliersRes.data?.supplierList?.length || 0,
                lowStockProducts: lowStockRes.data?.productList?.length || 0,
                totalTransactions: transactionsRes.data?.transactionList?.length || 0
            });

            setRecentProducts(productsRes.data?.productList?.slice(0, 5) || []);
            setLowStockItems(lowStockRes.data?.productList?.slice(0, 5) || []);

            setLoading(false);
        } catch (err) {
            console.error('Error fetching dashboard data:', err);
            setError('Failed to load dashboard data. Please try again.');
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="dashboard-page">
                <div className="loading">
                    <div className="spinner"></div>
                    <p>Loading dashboard...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="dashboard-page">
                <div className="error-message">{error}</div>
            </div>
        );
    }

    return (
        <div className="dashboard-page">
            <div className="dashboard-header">
                <h1>📊 Dashboard</h1>
                <p>Welcome to your Inventory Management System</p>
            </div>

            {/* Statistics Cards */}
            <div className="stats-grid">
                <div className="stat-card blue">
                    <div className="stat-icon">📦</div>
                    <div className="stat-info">
                        <h3>{stats.totalProducts}</h3>
                        <p>Total Products</p>
                    </div>
                </div>

                <div className="stat-card green">
                    <div className="stat-icon">🏷️</div>
                    <div className="stat-info">
                        <h3>{stats.totalCategories}</h3>
                        <p>Categories</p>
                    </div>
                </div>

                <div className="stat-card orange">
                    <div className="stat-icon">🚚</div>
                    <div className="stat-info">
                        <h3>{stats.totalSuppliers}</h3>
                        <p>Suppliers</p>
                    </div>
                </div>

                <div className="stat-card red">
                    <div className="stat-icon">⚠️</div>
                    <div className="stat-info">
                        <h3>{stats.lowStockProducts}</h3>
                        <p>Low Stock Alert</p>
                    </div>
                </div>

                <div className="stat-card purple">
                    <div className="stat-icon">🔄</div>
                    <div className="stat-info">
                        <h3>{stats.totalTransactions}</h3>
                        <p>Transactions</p>
                    </div>
                </div>
            </div>

            {/* Quick Actions */}
            <div className="quick-actions">
                <h2>⚡ Quick Actions</h2>
                <div className="action-buttons">
                    <button className="action-btn" onClick={() => navigate('/products')}>
                        ➕ Add Product
                    </button>
                    <button className="action-btn" onClick={() => navigate('/categories')}>
                        🏷️ Manage Categories
                    </button>
                    <button className="action-btn" onClick={() => navigate('/suppliers')}>
                        🚚 Manage Suppliers
                    </button>
                    <button className="action-btn" onClick={fetchDashboardData}>
                        🔄 Refresh Data
                    </button>
                </div>
            </div>

            {/* Data Tables */}
            <div className="dashboard-content">
                {/* Recent Products */}
                <div className="dashboard-section">
                    <h2>📦 Recent Products</h2>
                    {recentProducts.length > 0 ? (
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>Name</th>
                                    <th>SKU</th>
                                    <th>Price</th>
                                    <th>Stock</th>
                                </tr>
                            </thead>
                            <tbody>
                                {recentProducts.map(product => (
                                    <tr key={product.id}>
                                        <td>{product.name}</td>
                                        <td>{product.sku}</td>
                                        <td>${product.price}</td>
                                        <td>{product.quantity}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    ) : (
                        <p className="no-data">No products yet. Add your first product!</p>
                    )}
                </div>

                {/* Low Stock Alert */}
                <div className="dashboard-section">
                    <h2>⚠️ Low Stock Alert</h2>
                    {lowStockItems.length > 0 ? (
                        <table className="data-table alert">
                            <thead>
                                <tr>
                                    <th>Product</th>
                                    <th>Current Stock</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                {lowStockItems.map(product => (
                                    <tr key={product.id} className="warning">
                                        <td>{product.name}</td>
                                        <td className="stock-low">{product.quantity}</td>
                                        <td>⚠️ Low Stock</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    ) : (
                        <p className="no-data success">✅ All products have sufficient stock!</p>
                    )}
                </div>
            </div>
        </div>
    );
}

export default DashboardPage;

