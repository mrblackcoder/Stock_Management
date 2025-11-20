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
        name: '', sku: '', description: '', price: '', stockQuantity: '', reorderLevel: '10', categoryId: '', supplierId: ''
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
            setFormData({ name: '', sku: '', description: '', price: '', stockQuantity: '', reorderLevel: '10', categoryId: '', supplierId: '' });
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


    if (loading) return <div className="dashboard-page"><div className="dashboard-container">Yükleniyor...</div></div>;

    return (
        <div className="dashboard-page">

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
                            <button onClick={() => { setShowForm(!showForm); setFormData({ name: '', sku: '', description: '', price: '', stockQuantity: '', reorderLevel: '10', categoryId: '', supplierId: '' }); }}
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
                                    {products.map((prod, index) => {
                                        const stockQty = prod.stockQuantity || prod.quantity || 0;
                                        const reorderLevel = prod.reorderLevel || 10;

                                        // Stok durumu belirleme
                                        const getStockColor = () => {
                                            if (stockQty === 0) return '#f56565';
                                            if (stockQty <= reorderLevel) return '#ed8936';
                                            return '#48bb78';
                                        };

                                        const getStockBadge = () => {
                                            if (stockQty === 0) return '⚠️ Stok Yok';
                                            if (stockQty <= reorderLevel) return '⚠️ Düşük';
                                            return '✅ Normal';
                                        };

                                        return (
                                            <tr key={prod.id} style={{
                                                borderBottom: '1px solid #ddd',
                                                background: index % 2 === 0 ? '#f9f9f9' : 'white',
                                                transition: 'background 0.2s'
                                            }}
                                            onMouseEnter={(e) => e.currentTarget.style.background = '#e8f4f8'}
                                            onMouseLeave={(e) => e.currentTarget.style.background = index % 2 === 0 ? '#f9f9f9' : 'white'}
                                            >
                                                <td style={{padding: '12px', fontFamily: 'monospace', color: '#4a5568'}}>{prod.sku}</td>
                                                <td style={{padding: '12px', fontWeight: 'bold', color: '#2d3748'}}>{prod.name}</td>
                                                <td style={{padding: '12px', textAlign: 'right', fontWeight: 'bold', fontSize: '15px', color: '#667eea'}}>
                                                    {formatPrice(prod.price)}
                                                </td>
                                                <td style={{padding: '12px', textAlign: 'center'}}>
                                                    <div style={{display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '4px'}}>
                                                        <div style={{
                                                            padding: '6px 12px',
                                                            borderRadius: '8px',
                                                            background: getStockColor() + '20',
                                                            border: `2px solid ${getStockColor()}`,
                                                            fontWeight: 'bold',
                                                            fontSize: '16px',
                                                            color: getStockColor(),
                                                            minWidth: '60px'
                                                        }}>
                                                            {stockQty}
                                                        </div>
                                                        <span style={{
                                                            fontSize: '11px',
                                                            padding: '3px 8px',
                                                            borderRadius: '10px',
                                                            background: getStockColor(),
                                                            color: 'white',
                                                            fontWeight: 'bold'
                                                        }}>
                                                            {getStockBadge()}
                                                        </span>
                                                    </div>
                                                </td>
                                                <td style={{padding: '12px', color: '#4a5568'}}>{prod.categoryName || 'N/A'}</td>
                                                <td style={{padding: '12px', textAlign: 'center'}}>
                                                    <button
                                                        onClick={() => handleDelete(prod.id)}
                                                        style={{
                                                            padding: '6px 16px',
                                                            background: '#f56565',
                                                            color: 'white',
                                                            border: 'none',
                                                            borderRadius: '6px',
                                                            cursor: 'pointer',
                                                            fontWeight: 'bold',
                                                            transition: 'all 0.2s'
                                                        }}
                                                        onMouseEnter={(e) => e.target.style.background = '#e53e3e'}
                                                        onMouseLeave={(e) => e.target.style.background = '#f56565'}
                                                    >
                                                        🗑️ Sil
                                                    </button>
                                                </td>
                                            </tr>
                                        );
                                    })}
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
