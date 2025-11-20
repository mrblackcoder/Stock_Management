import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import ApiService from '../service/ApiService';
import './DashboardPage.css';

function ProductPage() {
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [suppliers, setSuppliers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showForm, setShowForm] = useState(false);
    const [exchangeRates, setExchangeRates] = useState({ usd: 0, eur: 0 });
    const [showPricesInUSD, setShowPricesInUSD] = useState(false);
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        name: '', sku: '', description: '', price: '', stockQuantity: '', categoryId: '', supplierId: ''
    });

    useEffect(() => {
        fetchData();
        fetchExchangeRates(); // External API call
    }, []);

    const fetchExchangeRates = async () => {
        try {
            // Using ExchangeRate-API (free, no API key required)
            const response = await fetch('https://api.exchangerate-api.com/v4/latest/TRY');
            const data = await response.json();
            setExchangeRates({
                usd: data.rates.USD || 0.033,
                eur: data.rates.EUR || 0.030
            });
        } catch (err) {
            console.error('Döviz kuru alınamadı:', err);
            // Fallback rates
            setExchangeRates({ usd: 0.033, eur: 0.030 });
        }
    };

    const formatPrice = (price) => {
        if (showPricesInUSD) {
            const usdPrice = (price * exchangeRates.usd).toFixed(2);
            return `$${usdPrice}`;
        }
        return `${price} ₺`;
    };

    const fetchData = async () => {
        try {
            setLoading(true);
            const [productsRes, categoriesRes, suppliersRes] = await Promise.all([
                ApiService.getAllProducts(),
                ApiService.getAllCategories(),
                ApiService.getAllSuppliers()
            ]);
            setProducts(productsRes.productList || []);
            setCategories(categoriesRes.categoryList || []);
            setSuppliers(suppliersRes.supplierList || []);
        } catch (err) {
            setError('Veriler yüklenemedi: ' + err.message);
            if (err.message.includes('403') || err.message.includes('401')) {
                ApiService.logout();
                navigate('/login');
            }
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await ApiService.createProduct(formData);
            setShowForm(false);
            setFormData({ name: '', sku: '', description: '', price: '', stockQuantity: '', categoryId: '', supplierId: '' });
            fetchData();
        } catch (err) {
            setError('Ürün eklenemedi: ' + err.message);
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Bu ürünü silmek istediğinizden emin misiniz?')) {
            try {
                await ApiService.deleteProduct(id);
                fetchData();
            } catch (err) {
                setError('Silme başarısız: ' + err.message);
            }
        }
    };

    const handleLogout = () => {
        ApiService.logout();
        navigate('/login');
    };

    if (loading) return <div className="dashboard-page"><div className="dashboard-container">Yükleniyor...</div></div>;

    return (
        <div className="dashboard-page">
            <div className="dashboard-header">
                <h1>📦 Inventory Management System</h1>
                <div className="user-info">
                    <span className="username">{ApiService.getUsername()}</span>
                    <button onClick={() => navigate('/dashboard')} className="action-btn" style={{marginRight: '10px'}}>Dashboard</button>
                    <button onClick={handleLogout} className="logout-btn">Çıkış</button>
                </div>
            </div>

            <div className="dashboard-container">
                <div className="quick-actions">
                    <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px'}}>
                        <h2>📦 Ürünler</h2>
                        <div>
                            <button
                                onClick={() => setShowPricesInUSD(!showPricesInUSD)}
                                className="action-btn"
                                style={{marginRight: '10px', background: showPricesInUSD ? '#48bb78' : '#667eea'}}
                            >
                                {showPricesInUSD ? '💵 USD (External API)' : '💰 TRY'}
                            </button>
                            <button onClick={() => { setShowForm(!showForm); setFormData({ name: '', sku: '', description: '', price: '', stockQuantity: '', categoryId: '', supplierId: '' }); }}
                                    className="action-btn">
                                {showForm ? 'İptal' : '+ Yeni Ürün'}
                            </button>
                        </div>
                    </div>

                    {exchangeRates.usd > 0 && (
                        <div style={{padding: '10px', background: '#e6f7ff', borderRadius: '5px', marginBottom: '15px', fontSize: '14px'}}>
                            📊 <strong>Güncel Döviz Kurları (External API - ExchangeRate-API):</strong> 1 TRY = ${exchangeRates.usd.toFixed(4)} USD | €{exchangeRates.eur.toFixed(4)} EUR
                        </div>
                    )}

                    {error && <div style={{color: 'red', margin: '10px 0'}}>{error}</div>}

                    {showForm && (
                        <form onSubmit={handleSubmit} style={{marginTop: '20px', padding: '20px', background: '#f5f5f5', borderRadius: '10px'}}>
                            <div style={{display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px'}}>
                                <div>
                                    <label style={{display: 'block', marginBottom: '5px'}}>Ürün Adı *</label>
                                    <input type="text" value={formData.name} onChange={(e) => setFormData({...formData, name: e.target.value})} required style={{width: '100%', padding: '10px', borderRadius: '5px', border: '1px solid #ddd'}} />
                                </div>
                                <div>
                                    <label style={{display: 'block', marginBottom: '5px'}}>SKU *</label>
                                    <input type="text" value={formData.sku} onChange={(e) => setFormData({...formData, sku: e.target.value})} required style={{width: '100%', padding: '10px', borderRadius: '5px', border: '1px solid #ddd'}} />
                                </div>
                                <div>
                                    <label style={{display: 'block', marginBottom: '5px'}}>Fiyat *</label>
                                    <input type="number" value={formData.price} onChange={(e) => setFormData({...formData, price: e.target.value})} required style={{width: '100%', padding: '10px', borderRadius: '5px', border: '1px solid #ddd'}} />
                                </div>
                                <div>
                                    <label style={{display: 'block', marginBottom: '5px'}}>Miktar *</label>
                                    <input type="number" value={formData.stockQuantity} onChange={(e) => setFormData({...formData, stockQuantity: e.target.value})} required style={{width: '100%', padding: '10px', borderRadius: '5px', border: '1px solid #ddd'}} />
                                </div>
                                <div>
                                    <label style={{display: 'block', marginBottom: '5px'}}>Kategori *</label>
                                    <select value={formData.categoryId} onChange={(e) => setFormData({...formData, categoryId: e.target.value})} required style={{width: '100%', padding: '10px', borderRadius: '5px', border: '1px solid #ddd'}}>
                                        <option value="">Seçiniz</option>
                                        {categories.map(cat => <option key={cat.id} value={cat.id}>{cat.name}</option>)}
                                    </select>
                                </div>
                                <div>
                                    <label style={{display: 'block', marginBottom: '5px'}}>Tedarikçi *</label>
                                    <select value={formData.supplierId} onChange={(e) => setFormData({...formData, supplierId: e.target.value})} required style={{width: '100%', padding: '10px', borderRadius: '5px', border: '1px solid #ddd'}}>
                                        <option value="">Seçiniz</option>
                                        {suppliers.map(sup => <option key={sup.id} value={sup.id}>{sup.name}</option>)}
                                    </select>
                                </div>
                            </div>
                            <div style={{marginTop: '15px'}}>
                                <label style={{display: 'block', marginBottom: '5px'}}>Açıklama</label>
                                <textarea value={formData.description} onChange={(e) => setFormData({...formData, description: e.target.value})} rows="3" style={{width: '100%', padding: '10px', borderRadius: '5px', border: '1px solid #ddd'}} />
                            </div>
                            <button type="submit" className="action-btn" style={{marginTop: '15px'}}>Kaydet</button>
                        </form>
                    )}

                    <div style={{marginTop: '30px', overflowX: 'auto'}}>
                        {products.length === 0 ? (
                            <p>Henüz ürün eklenmemiş. Önce kategori ve tedarikçi ekleyin.</p>
                        ) : (
                            <table style={{width: '100%', borderCollapse: 'collapse', minWidth: '800px'}}>
                                <thead>
                                    <tr style={{background: '#667eea', color: 'white'}}>
                                        <th style={{padding: '12px', textAlign: 'left'}}>SKU</th>
                                        <th style={{padding: '12px', textAlign: 'left'}}>Ad</th>
                                        <th style={{padding: '12px', textAlign: 'right'}}>Fiyat</th>
                                        <th style={{padding: '12px', textAlign: 'center'}}>Stok</th>
                                        <th style={{padding: '12px', textAlign: 'left'}}>Kategori</th>
                                        <th style={{padding: '12px', textAlign: 'center'}}>İşlemler</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {products.map((prod, index) => (
                                        <tr key={prod.id} style={{borderBottom: '1px solid #ddd', background: index % 2 === 0 ? '#f9f9f9' : 'white'}}>
                                            <td style={{padding: '12px'}}>{prod.sku}</td>
                                            <td style={{padding: '12px'}}>{prod.name}</td>
                                            <td style={{padding: '12px', textAlign: 'right', fontWeight: 'bold'}}>{formatPrice(prod.price)}</td>
                                            <td style={{padding: '12px', textAlign: 'center'}}>{prod.quantity}</td>
                                            <td style={{padding: '12px'}}>{prod.categoryName || 'N/A'}</td>
                                            <td style={{padding: '12px', textAlign: 'center'}}>
                                                <button onClick={() => handleDelete(prod.id)} style={{padding: '5px 15px', background: '#f56565', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer'}}>Sil</button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        )}
                    </div>
                </div>

                <div style={{marginTop: '30px', textAlign: 'center'}}>
                    <button onClick={() => navigate('/categories')} className="action-btn">⬅ Kategoriler</button>
                    <button onClick={() => navigate('/suppliers')} className="action-btn" style={{marginLeft: '10px'}}>Tedarikçiler →</button>
                    <button onClick={() => navigate('/transactions')} className="action-btn" style={{marginLeft: '10px'}}>İşlemler →</button>
                </div>
            </div>
        </div>
    );
}

export default ProductPage;
