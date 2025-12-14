import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ApiService from '../service/ApiService';
import './DashboardPage.css';
import { Chart as ChartJS, ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement, Title } from 'chart.js';
import { Pie, Bar } from 'react-chartjs-2';

// Register Chart.js components
ChartJS.register(ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement, Title);

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
    const [userRole, setUserRole] = useState('');
    const [categoryData, setCategoryData] = useState({ labels: [], data: [] });
    const [transactionData, setTransactionData] = useState({ labels: [], purchases: [], sales: [] });

    useEffect(() => {
        // Get user role using ApiService (handles decryption)
        const role = ApiService.getRole();
        setUserRole(role || 'USER');
        fetchDashboardData();
    }, []);

    const fetchDashboardData = async () => {
        try {
            setLoading(true);

            // Fetch all data with correct API methods
            // Dashboard için ürünleri getir - supplier bilgileri dahil
            const [productsRes, categoriesRes, suppliersRes, transactionsRes] = await Promise.all([
                ApiService.getAllProducts(0, 100, "createdAt"), // Optimized size
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
            setRecentProducts(recentProductsList);
            setLowStockItems(lowStockRes.productList?.slice(0, 5) || []);

            // Category distribution data for Pie Chart
            const categoryCount = {};
            productsRes.productList?.forEach(product => {
                const catName = product.categoryName || 'Uncategorized';
                categoryCount[catName] = (categoryCount[catName] || 0) + 1;
            });
            setCategoryData({
                labels: Object.keys(categoryCount),
                data: Object.values(categoryCount)
            });

            // Transaction data for Bar Chart (last 7 days simulation)
            const purchaseCount = transactionsRes.transactionList?.filter(t => t.transactionType === 'PURCHASE').length || 0;
            const saleCount = transactionsRes.transactionList?.filter(t => t.transactionType === 'SALE').length || 0;
            const adjustCount = transactionsRes.transactionList?.filter(t => t.transactionType === 'ADJUSTMENT').length || 0;
            setTransactionData({
                labels: ['Purchases', 'Sales', 'Adjustments'],
                data: [purchaseCount, saleCount, adjustCount]
            });

            setLoading(false);
        } catch (err) {
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
                <h1>Dashboard</h1>
                <p>Welcome to your Inventory Management System</p>
            </div>

            {/* Statistics Cards */}
            <div className="stats-grid">
                <div className="stat-card blue">
                    <div className="stat-icon">P</div>
                    <div className="stat-info">
                        <h3>{stats.totalProducts}</h3>
                        <p>Total Products</p>
                    </div>
                </div>

                <div className="stat-card green">
                    <div className="stat-icon">C</div>
                    <div className="stat-info">
                        <h3>{stats.totalCategories}</h3>
                        <p>Categories</p>
                    </div>
                </div>

                <div className="stat-card orange">
                    <div className="stat-icon">S</div>
                    <div className="stat-info">
                        <h3>{stats.totalSuppliers}</h3>
                        <p>Suppliers</p>
                    </div>
                </div>

                <div className="stat-card red">
                    <div className="stat-icon">!</div>
                    <div className="stat-info">
                        <h3>{stats.lowStockProducts}</h3>
                        <p>Low Stock Alert</p>
                    </div>
                </div>

                <div className="stat-card purple">
                    <div className="stat-icon">T</div>
                    <div className="stat-info">
                        <h3>{stats.totalTransactions}</h3>
                        <p>Transactions</p>
                    </div>
                </div>
            </div>

            {/* Charts Section */}
            <div className="charts-grid">
                <div className="chart-card">
                    <h3>📊 Products by Category</h3>
                    {categoryData.labels.length > 0 ? (
                        <div style={{ height: '250px', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                            <Pie
                                data={{
                                    labels: categoryData.labels,
                                    datasets: [{
                                        data: categoryData.data,
                                        backgroundColor: [
                                            '#667eea', '#48bb78', '#ed8936', '#f56565', '#9f7aea',
                                            '#38b2ac', '#ed64a6', '#4fd1c5', '#fc8181', '#63b3ed'
                                        ],
                                        borderWidth: 2,
                                        borderColor: '#fff'
                                    }]
                                }}
                                options={{
                                    responsive: true,
                                    maintainAspectRatio: false,
                                    plugins: {
                                        legend: { position: 'bottom', labels: { boxWidth: 12 } }
                                    }
                                }}
                            />
                        </div>
                    ) : (
                        <p className="no-data">No category data available</p>
                    )}
                </div>

                <div className="chart-card">
                    <h3>📈 Transaction Summary</h3>
                    {transactionData.data && transactionData.data.length > 0 ? (
                        <div style={{ height: '250px' }}>
                            <Bar
                                data={{
                                    labels: transactionData.labels,
                                    datasets: [{
                                        label: 'Count',
                                        data: transactionData.data,
                                        backgroundColor: ['#48bb78', '#f56565', '#ed8936'],
                                        borderRadius: 8
                                    }]
                                }}
                                options={{
                                    responsive: true,
                                    maintainAspectRatio: false,
                                    plugins: {
                                        legend: { display: false }
                                    },
                                    scales: {
                                        y: { beginAtZero: true, ticks: { stepSize: 1 } }
                                    }
                                }}
                            />
                        </div>
                    ) : (
                        <p className="no-data">No transaction data available</p>
                    )}
                </div>
            </div>


            {/* Data Tables */}
            <div className="dashboard-content">
                {/* Recent Products */}
                <div className="dashboard-section">
                    <h2>Recent Products</h2>
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
                                        const statusLabel = stock <= 5 ? 'Critical' : stock <= 10 ? 'Low' : 'Good';

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
                                                        {product.supplierName || 'N/A'}
                                                    </span>
                                                </td>
                                                <td><strong>₺{product.price?.toFixed(2)}</strong></td>
                                                <td>
                                                    <span className={`stock-badge ${stockStatus}`}>
                                                        {stock} units
                                                    </span>
                                                </td>
                                                <td><strong>{statusLabel}</strong></td>
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
                        <h2>Low Stock Alert</h2>
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
                                        {userRole === 'ADMIN' && <th>Action</th>}
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
                                                        {stock} units
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
                                                        {isCritical ? 'CRITICAL' : 'LOW'}
                                                    </span>
                                                </td>
                                                {userRole === 'ADMIN' && (
                                                    <td>
                                                        <button
                                                            className="reorder-btn"
                                                            onClick={() => navigate('/transactions', {
                                                                state: {
                                                                    productId: product.id,
                                                                    productName: product.name,
                                                                    action: 'purchase'
                                                                }
                                                            })}
                                                            title={`Reorder ${product.name}`}
                                                        >
                                                            Reorder
                                                        </button>
                                                    </td>
                                                )}
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
                            <div style={{ fontSize: '48px', marginBottom: '10px' }}>&#10003;</div>
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

