import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import ApiService from '../service/ApiService';
import './DashboardPage.css';

function SupplierPage() {
    const [suppliers, setSuppliers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showForm, setShowForm] = useState(false);
    const [expandedSupplier, setExpandedSupplier] = useState(null);
    const [supplierProducts, setSupplierProducts] = useState({});
    const [allProducts, setAllProducts] = useState([]);
    const navigate = useNavigate();
    const role = ApiService.getRole();
    const isAdmin = role === 'ADMIN';

    const [formData, setFormData] = useState({
        name: '', email: '', phone: '', address: '', description: ''
    });

    useEffect(() => {
        fetchSuppliers();
        fetchAllProducts();
    }, []);

    const fetchSuppliers = async () => {
        try {
            setLoading(true);
            const response = await ApiService.getAllSuppliers();
            setSuppliers(response.supplierList || []);
        } catch (err) {
            setError('Tedarikçiler yüklenemedi: ' + err.message);
            if (err.message.includes('403') || err.message.includes('401')) {
                ApiService.logout();
                navigate('/login');
            }
        } finally {
            setLoading(false);
        }
    };

    const fetchAllProducts = async () => {
        try {
            const response = await ApiService.getAllProducts();
            setAllProducts(response.productList || []);
        } catch (err) {
            // Silently fail - products list is optional for supplier page
        }
    };

    const toggleSupplierProducts = (supplierId, supplierName) => {
        if (expandedSupplier === supplierId) {
            setExpandedSupplier(null);
        } else {
            setExpandedSupplier(supplierId);
            // Filter products by supplier
            const products = allProducts.filter(p => p.supplierName === supplierName || p.supplierId === supplierId);
            setSupplierProducts({...supplierProducts, [supplierId]: products});
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await ApiService.createSupplier(formData);
            setShowForm(false);
            setFormData({ name: '', email: '', phone: '', address: '', description: '' });
            fetchSuppliers();
        } catch (err) {
            setError('Tedarikçi eklenemedi: ' + err.message);
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Bu tedarikçiyi silmek istediğinizden emin misiniz?')) {
            try {
                await ApiService.deleteSupplier(id);
                fetchSuppliers();
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

            <div className="dashboard-container">
                <div className="quick-actions">
                    <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
                        <h2>Tedarikçiler</h2>
                        {isAdmin && (
                            <button onClick={() => { setShowForm(!showForm); setFormData({ name: '', email: '', phone: '', address: '', description: '' }); }}
                                    className="action-btn">
                                {showForm ? 'İptal' : '+ Yeni Tedarikçi'}
                            </button>
                        )}
                    </div>

                    {error && <div style={{color: 'red', margin: '10px 0'}}>{error}</div>}

                    {showForm && isAdmin && (
                        <form onSubmit={handleSubmit} style={{marginTop: '20px', padding: '20px', background: '#f5f5f5', borderRadius: '10px'}}>
                            <div style={{display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px'}}>
                                <div>
                                    <label style={{display: 'block', marginBottom: '5px'}}>Tedarikçi Adı *</label>
                                    <input type="text" value={formData.name} onChange={(e) => setFormData({...formData, name: e.target.value})} required style={{width: '100%', padding: '10px', borderRadius: '5px', border: '1px solid #ddd'}} />
                                </div>
                                <div>
                                    <label style={{display: 'block', marginBottom: '5px'}}>Email</label>
                                    <input type="email" value={formData.email} onChange={(e) => setFormData({...formData, email: e.target.value})} style={{width: '100%', padding: '10px', borderRadius: '5px', border: '1px solid #ddd'}} />
                                </div>
                                <div>
                                    <label style={{display: 'block', marginBottom: '5px'}}>Telefon</label>
                                    <input type="text" value={formData.phone} onChange={(e) => setFormData({...formData, phone: e.target.value})} style={{width: '100%', padding: '10px', borderRadius: '5px', border: '1px solid #ddd'}} />
                                </div>
                                <div>
                                    <label style={{display: 'block', marginBottom: '5px'}}>Adres</label>
                                    <input type="text" value={formData.address} onChange={(e) => setFormData({...formData, address: e.target.value})} style={{width: '100%', padding: '10px', borderRadius: '5px', border: '1px solid #ddd'}} />
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
                        {suppliers.length === 0 ? (
                            <p>Henüz tedarikçi eklenmemiş.</p>
                        ) : (
                            <table style={{width: '100%', borderCollapse: 'collapse'}}>
                                <thead>
                                    <tr style={{background: '#667eea', color: 'white'}}>
                                        <th style={{padding: '12px', textAlign: 'left'}}>Ad</th>
                                        <th style={{padding: '12px', textAlign: 'left'}}>Email</th>
                                        <th style={{padding: '12px', textAlign: 'left'}}>Telefon</th>
                                        <th style={{padding: '12px', textAlign: 'center'}}>Ürünler</th>
                                        <th style={{padding: '12px', textAlign: 'center'}}>İşlemler</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {suppliers.map((sup, index) => {
                                        const products = allProducts.filter(p => p.supplierName === sup.name || p.supplierId === sup.id);
                                        const isExpanded = expandedSupplier === sup.id;

                                        return (
                                            <React.Fragment key={sup.id}>
                                                <tr style={{borderBottom: '1px solid #ddd', background: index % 2 === 0 ? '#f9f9f9' : 'white'}}>
                                                    <td style={{padding: '12px'}}><strong>{sup.name}</strong></td>
                                                    <td style={{padding: '12px'}}>{sup.email || 'N/A'}</td>
                                                    <td style={{padding: '12px'}}>{sup.phone || 'N/A'}</td>
                                                    <td style={{padding: '12px', textAlign: 'center'}}>
                                                        <button
                                                            onClick={() => toggleSupplierProducts(sup.id, sup.name)}
                                                            style={{
                                                                padding: '6px 12px',
                                                                background: '#48bb78',
                                                                color: 'white',
                                                                border: 'none',
                                                                borderRadius: '5px',
                                                                cursor: 'pointer',
                                                                display: 'flex',
                                                                alignItems: 'center',
                                                                gap: '5px',
                                                                margin: '0 auto'
                                                            }}
                                                        >
                                                            <span>{isExpanded ? '▼' : '▶'}</span>
                                                            <span>{products.length} Ürün</span>
                                                        </button>
                                                    </td>
                                                    <td style={{padding: '12px', textAlign: 'center'}}>
                                                        {isAdmin && (
                                                            <button onClick={() => handleDelete(sup.id)} style={{padding: '5px 15px', background: '#f56565', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer'}}>Sil</button>
                                                        )}
                                                        {!isAdmin && <span style={{color: '#999'}}>Yetki yok</span>}
                                                    </td>
                                                </tr>
                                                {isExpanded && (
                                                    <tr style={{background: '#f0f4ff'}}>
                                                        <td colSpan="5" style={{padding: '15px'}}>
                                                            {products.length > 0 ? (
                                                                <div>
                                                                    <h4 style={{margin: '0 0 10px 0', color: '#667eea'}}>
                                                                        {sup.name} Tedarikçisine Ait Ürünler
                                                                    </h4>
                                                                    <table style={{width: '100%', borderCollapse: 'collapse', background: 'white', borderRadius: '8px', overflow: 'hidden'}}>
                                                                        <thead>
                                                                            <tr style={{background: '#e6efff'}}>
                                                                                <th style={{padding: '10px', textAlign: 'left', color: '#667eea'}}>Ürün Adı</th>
                                                                                <th style={{padding: '10px', textAlign: 'left', color: '#667eea'}}>SKU</th>
                                                                                <th style={{padding: '10px', textAlign: 'left', color: '#667eea'}}>Kategori</th>
                                                                                <th style={{padding: '10px', textAlign: 'right', color: '#667eea'}}>Fiyat</th>
                                                                                <th style={{padding: '10px', textAlign: 'center', color: '#667eea'}}>Stok</th>
                                                                            </tr>
                                                                        </thead>
                                                                        <tbody>
                                                                            {products.map((product, idx) => {
                                                                                const stock = product.quantity || product.stockQuantity || 0;
                                                                                const stockStatus = stock <= 5 ? 'CRIT' : stock <= 10 ? 'LOW' : 'OK';

                                                                                return (
                                                                                    <tr key={product.id} style={{borderBottom: idx < products.length - 1 ? '1px solid #e6efff' : 'none'}}>
                                                                                        <td style={{padding: '10px'}}><strong>{product.name}</strong></td>
                                                                                        <td style={{padding: '10px'}}>
                                                                                            <code style={{
                                                                                                background: '#f3f4f6',
                                                                                                padding: '3px 8px',
                                                                                                borderRadius: '4px',
                                                                                                fontSize: '12px'
                                                                                            }}>
                                                                                                {product.sku}
                                                                                            </code>
                                                                                        </td>
                                                                                        <td style={{padding: '10px'}}>{product.categoryName || 'N/A'}</td>
                                                                                        <td style={{padding: '10px', textAlign: 'right'}}>
                                                                                            <strong>₺{product.price?.toFixed(2)}</strong>
                                                                                        </td>
                                                                                        <td style={{padding: '10px', textAlign: 'center'}}>
                                                                                            <span style={{
                                                                                                padding: '4px 10px',
                                                                                                borderRadius: '12px',
                                                                                                background: stock <= 5 ? '#fee2e2' : stock <= 10 ? '#fef3c7' : '#d1fae5',
                                                                                                color: stock <= 5 ? '#dc2626' : stock <= 10 ? '#f59e0b' : '#10b981',
                                                                                                fontSize: '13px',
                                                                                                fontWeight: '600'
                                                                                            }}>
                                                                                                {stockStatus} {stock}
                                                                                            </span>
                                                                                        </td>
                                                                                    </tr>
                                                                                );
                                                                            })}
                                                                        </tbody>
                                                                    </table>
                                                                </div>
                                                            ) : (
                                                                <div style={{
                                                                    textAlign: 'center',
                                                                    padding: '20px',
                                                                    background: '#fff3cd',
                                                                    borderRadius: '8px',
                                                                    border: '1px solid #ffc107'
                                                                }}>
                                                                    <p style={{margin: 0, color: '#856404'}}>
                                                                        Bu tedarikçiye henüz ürün atanmamış
                                                                    </p>
                                                                </div>
                                                            )}
                                                        </td>
                                                    </tr>
                                                )}
                                            </React.Fragment>
                                        );
                                    })}
                                </tbody>
                            </table>
                        )}
                    </div>
                </div>

                <div style={{marginTop: '30px', textAlign: 'center'}}>
                    <button onClick={() => navigate('/products')} className="action-btn">⬅ Ürünler</button>
                    <button onClick={() => navigate('/transactions')} className="action-btn" style={{marginLeft: '10px'}}>İşlemler →</button>
                </div>
            </div>
        </div>
    );
}

export default SupplierPage;

