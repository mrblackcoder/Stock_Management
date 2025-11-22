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

            // Fetch all data with correct API methods
            // Dashboard için tüm ürünleri getir (büyük size ile) - supplier bilgileri dahil
            const [productsRes, categoriesRes, suppliersRes, transactionsRes] = await Promise.all([
                ApiService.getAllProducts(0, 1000, "createdAt"), // Büyük size ve createdAt sıralaması
                ApiService.getAllCategories(),
                ApiService.getAllSuppliers(),
                ApiService.getAllTransactions()
            ]);

            // Get low stock products
            const lowStockRes = await ApiService.getLowStockProducts();

            setStats({
                totalProducts: productsRes.productList?.length || 0,
                totalCategories: categoriesRes.categoryList?.length || 0,
                totalSuppliers: suppliersRes.supplierList?.length || 0,
                lowStockProducts: lowStockRes.productList?.length || 0,
                totalTransactions: transactionsRes.transactionList?.length || 0
            });

            // Son 5 ürünü al (en yeni önce) - Supplier bilgileriyle birlikte
            const recentProductsList = productsRes.productList?.slice(0, 5) || [];
            console.log('Recent Products with Suppliers:', recentProductsList.map(p => ({
                id: p.id,
                name: p.name,
                supplier: p.supplierName,
                supplierId: p.supplierId,
                category: p.categoryName
            })));

            setRecentProducts(recentProductsList);
            setLowStockItems(lowStockRes.productList?.slice(0, 5) || []);

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


            {/* Data Tables */}
            <div className="dashboard-content">
                {/* Recent Products */}
                <div className="dashboard-section">
                    <h2>📦 Recent Products</h2>
                    {recentProducts.length > 0 ? (
                        <div className="table-scroll-wrapper">
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>Product Name</th>
                                        <th>SKU</th>
                                        <th>Category</th>
                                        <th>Supplier</th>
                                        <th>Price</th>
                                        <th>Current Stock</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {recentProducts.map(product => {
                                        const stock = product.stockQuantity || product.quantity || 0;
                                        const stockStatus = stock <= 5 ? 'critical' : stock <= 10 ? 'low' : 'good';
                                        const statusIcon = stock <= 5 ? '🔴' : stock <= 10 ? '🟡' : '🟢';

                                        return (
                                            <tr key={product.id}>
                                                <td><strong>{product.name}</strong></td>
                                                <td><code>{product.sku}</code></td>
                                                <td>{product.categoryName || 'N/A'}</td>
                                                <td>
                                                    <span style={{
                                                        background: '#e6efff',
                                                        color: '#667eea',
                                                        padding: '4px 10px',
                                                        borderRadius: '6px',
                                                        fontSize: '13px',
                                                        fontWeight: '600'
                                                    }}>
                                                        🚚 {product.supplierName || 'N/A'}
                                                    </span>
                                                </td>
                                                <td><strong>₺{product.price?.toFixed(2)}</strong></td>
                                                <td>
                                                    <span className={`stock-badge ${stockStatus}`}>
                                                        {stock} units
                                                    </span>
                                                </td>
                                                <td>{statusIcon} {stockStatus === 'critical' ? 'Critical' : stockStatus === 'low' ? 'Low' : 'Good'}</td>
                                            </tr>
                                        );
                                    })}
                                </tbody>
                            </table>
                        </div>
                    ) : (
                        <p className="no-data">No products yet. Add your first product!</p>
                    )}
                </div>

                {/* Low Stock Alert - Enhanced */}
                <div className="dashboard-section low-stock-section">
                    <div className="section-header">
                        <h2>⚠️ Low Stock Alert</h2>
                        <span className="badge-count">{lowStockItems.length} items</span>
                    </div>
                    {lowStockItems.length > 0 ? (
                        <div className="table-scroll-wrapper">
                            <table className="data-table alert-table">
                                <thead>
                                    <tr>
                                        <th>Priority</th>
                                        <th>Product Name</th>
                                        <th>SKU</th>
                                        <th>Category</th>
                                        <th>Supplier</th>
                                        <th>Current Stock</th>
                                        <th>Price/Unit</th>
                                        <th>Status</th>
                                        <th>Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {lowStockItems.map((product, index) => {
                                        const stock = product.stockQuantity || product.quantity || 0;
                                        const isCritical = stock <= 5;
                                        const priorityLevel = isCritical ? 'HIGH' : 'MEDIUM';
                                        const priorityColor = isCritical ? '#dc2626' : '#f59e0b';

                                        return (
                                            <tr key={product.id} className={isCritical ? 'critical-row' : 'warning-row'}>
                                                <td>
                                                    <span
                                                        className="priority-badge"
                                                        style={{
                                                            background: priorityColor,
                                                            color: 'white',
                                                            padding: '4px 8px',
                                                            borderRadius: '4px',
                                                            fontSize: '11px',
                                                            fontWeight: 'bold'
                                                        }}
                                                    >
                                                        {priorityLevel}
                                                    </span>
                                                </td>
                                                <td>
                                                    <strong>{product.name}</strong>
                                                </td>
                                                <td>
                                                    <code style={{
                                                        background: '#f3f4f6',
                                                        padding: '2px 6px',
                                                        borderRadius: '3px',
                                                        fontSize: '12px'
                                                    }}>
                                                        {product.sku}
                                                    </code>
                                                </td>
                                                <td>{product.categoryName || 'N/A'}</td>
                                                <td>{product.supplierName || 'N/A'}</td>
                                                <td>
                                                    <span
                                                        className="stock-indicator"
                                                        style={{
                                                            background: isCritical ? '#fee2e2' : '#fef3c7',
                                                            color: isCritical ? '#dc2626' : '#f59e0b',
                                                            padding: '6px 12px',
                                                            borderRadius: '6px',
                                                            fontWeight: 'bold',
                                                            fontSize: '14px',
                                                            display: 'inline-block'
                                                        }}
                                                    >
                                                        {isCritical ? '🔴' : '🟡'} {stock} units
                                                    </span>
                                                </td>
                                                <td>
                                                    <strong>₺{product.price?.toFixed(2)}</strong>
                                                </td>
                                                <td>
                                                    <span style={{
                                                        color: isCritical ? '#dc2626' : '#f59e0b',
                                                        fontWeight: 'bold'
                                                    }}>
                                                        {isCritical ? '⚠️ CRITICAL' : '⚡ LOW'}
                                                    </span>
                                                </td>
                                                <td>
                                                    <button
                                                        className="reorder-btn"
                                                        onClick={() => navigate('/transactions')}
                                                        style={{
                                                            background: '#3b82f6',
                                                            color: 'white',
                                                            border: 'none',
                                                            padding: '6px 12px',
                                                            borderRadius: '5px',
                                                            cursor: 'pointer',
                                                            fontSize: '12px',
                                                            fontWeight: 'bold'
                                                        }}
                                                    >
                                                        📦 Reorder
                                                    </button>
                                                </td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                        </div>
                    ) : (
                        <div style={{
                            background: '#ecfdf5',
                            border: '2px solid #10b981',
                            borderRadius: '10px',
                            padding: '30px',
                            textAlign: 'center'
                        }}>
                            <div style={{ fontSize: '48px', marginBottom: '10px' }}>✅</div>
                            <p style={{
                                color: '#10b981',
                                fontSize: '18px',
                                fontWeight: 'bold',
                                margin: 0
                            }}>
                                All products have sufficient stock!
                            </p>
                            <p style={{ color: '#6b7280', marginTop: '5px' }}>
                                No action required at this time.
                            </p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

export default DashboardPage;

