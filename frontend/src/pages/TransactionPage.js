import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import ApiService from '../service/ApiService';
import './DashboardPage.css';

function TransactionPage() {
    const [transactions, setTransactions] = useState([]);
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showForm, setShowForm] = useState(false);
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        productId: '',
        transactionType: 'PURCHASE',
        quantity: '',
        notes: ''
    });

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            setLoading(true);
            const [transactionsRes, productsRes] = await Promise.all([
                ApiService.getAllTransactions(),
                ApiService.getAllProducts()
            ]);
            setTransactions(transactionsRes.transactionList || []);
            setProducts(productsRes.productList || []);
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
            await ApiService.createTransaction(formData);
            setShowForm(false);
            setFormData({ productId: '', transactionType: 'PURCHASE', quantity: '', notes: '' });
            fetchData();
        } catch (err) {
            setError('Transaction eklenemedi: ' + err.message);
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Bu transaction\'ı silmek istediğinizden emin misiniz?')) {
            try {
                await ApiService.deleteTransaction(id);
                fetchData();
            } catch (err) {
                setError('Silme başarısız: ' + err.message);
            }
        }
    };

    const getTransactionIcon = (type) => {
        switch(type) {
            case 'PURCHASE': return '📥';
            case 'SALE': return '📤';
            case 'ADJUSTMENT': return '🔧';
            default: return '🔄';
        }
    };

    const getTransactionColor = (type) => {
        switch(type) {
            case 'PURCHASE': return '#48bb78';
            case 'SALE': return '#f56565';
            case 'ADJUSTMENT': return '#ed8936';
            default: return '#667eea';
        }
    };

    const getStockStatusColor = (quantity, reorderLevel = 10) => {
        if (quantity === 0) return '#f56565'; // Kırmızı - Stok yok
        if (quantity <= reorderLevel) return '#ed8936'; // Turuncu - Düşük stok
        return '#48bb78'; // Yeşil - Normal stok
    };

    const getStockStatusText = (quantity, reorderLevel = 10) => {
        if (quantity === 0) return '⚠️ Stok Yok';
        if (quantity <= reorderLevel) return '⚠️ Düşük Stok';
        return '✅ Normal';
    };

    if (loading) return <div className="dashboard-page"><div className="dashboard-container">Yükleniyor...</div></div>;

    return (
        <div className="dashboard-page">

            <div className="dashboard-container">
                <div className="quick-actions">
                    <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
                        <h2>🔄 Stock Transactions</h2>
                        <button onClick={() => {
                            setShowForm(!showForm);
                            setFormData({ productId: '', transactionType: 'PURCHASE', quantity: '', notes: '' });
                        }} className="action-btn">
                            {showForm ? 'İptal' : '+ Yeni Transaction'}
                        </button>
                    </div>

                    {error && <div style={{color: 'red', margin: '10px 0', padding: '10px', background: '#fee', borderRadius: '5px'}}>{error}</div>}

                    {showForm && (
                        <form onSubmit={handleSubmit} style={{marginTop: '20px', padding: '20px', background: '#f5f5f5', borderRadius: '10px'}}>
                            <div style={{display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px'}}>
                                <div>
                                    <label style={{display: 'block', marginBottom: '5px', fontWeight: 'bold'}}>Ürün *</label>
                                    <select
                                        value={formData.productId}
                                        onChange={(e) => setFormData({...formData, productId: e.target.value})}
                                        required
                                        style={{width: '100%', padding: '10px', borderRadius: '5px', border: '1px solid #ddd'}}
                                    >
                                        <option value="">Ürün Seçiniz</option>
                                        {products.map(prod => {
                                            const stockQty = prod.stockQuantity || prod.quantity || 0;
                                            const stockStatus = getStockStatusText(stockQty, prod.reorderLevel);
                                            return (
                                                <option key={prod.id} value={prod.id}>
                                                    {prod.name} - Stok: {stockQty} {stockStatus}
                                                </option>
                                            );
                                        })}
                                    </select>
                                </div>
                                <div>
                                    <label style={{display: 'block', marginBottom: '5px', fontWeight: 'bold'}}>İşlem Tipi *</label>
                                    <select
                                        value={formData.transactionType}
                                        onChange={(e) => setFormData({...formData, transactionType: e.target.value})}
                                        required
                                        style={{width: '100%', padding: '10px', borderRadius: '5px', border: '1px solid #ddd'}}
                                    >
                                        <option value="PURCHASE">📥 Alış (Purchase)</option>
                                        <option value="SALE">📤 Satış (Sale)</option>
                                        <option value="ADJUSTMENT">🔧 Düzeltme (Adjustment)</option>
                                    </select>
                                </div>
                                <div>
                                    <label style={{display: 'block', marginBottom: '5px', fontWeight: 'bold'}}>Miktar *</label>
                                    <input
                                        type="number"
                                        value={formData.quantity}
                                        onChange={(e) => setFormData({...formData, quantity: e.target.value})}
                                        required
                                        min="1"
                                        style={{width: '100%', padding: '10px', borderRadius: '5px', border: '1px solid #ddd'}}
                                    />
                                </div>
                            </div>
                            <div style={{marginTop: '15px'}}>
                                <label style={{display: 'block', marginBottom: '5px', fontWeight: 'bold'}}>Notlar</label>
                                <textarea
                                    value={formData.notes}
                                    onChange={(e) => setFormData({...formData, notes: e.target.value})}
                                    rows="3"
                                    placeholder="İşlem notları (opsiyonel)"
                                    style={{width: '100%', padding: '10px', borderRadius: '5px', border: '1px solid #ddd'}}
                                />
                            </div>
                            <button type="submit" className="action-btn" style={{marginTop: '15px'}}>💾 Kaydet</button>
                        </form>
                    )}

                    <div style={{marginTop: '30px', overflowX: 'auto'}}>
                        {transactions.length === 0 ? (
                            <div style={{textAlign: 'center', padding: '40px', background: '#f9f9f9', borderRadius: '10px'}}>
                                <h3>📭 Henüz transaction kaydı yok</h3>
                                <p>Yeni bir transaction eklemek için yukarıdaki butona tıklayın.</p>
                            </div>
                        ) : (
                            <table style={{width: '100%', borderCollapse: 'collapse', minWidth: '900px'}}>
                                <thead>
                                    <tr style={{background: '#667eea', color: 'white'}}>
                                        <th style={{padding: '12px', textAlign: 'left'}}>📅 Tarih</th>
                                        <th style={{padding: '12px', textAlign: 'left'}}>📦 Ürün</th>
                                        <th style={{padding: '12px', textAlign: 'center'}}>🔄 Tip</th>
                                        <th style={{padding: '12px', textAlign: 'right'}}>📊 Miktar</th>
                                        <th style={{padding: '12px', textAlign: 'center'}}>📦 Güncel Stok</th>
                                        <th style={{padding: '12px', textAlign: 'center'}}>⚠️ Stok Durumu</th>
                                        <th style={{padding: '12px', textAlign: 'left'}}>👤 Kullanıcı</th>
                                        <th style={{padding: '12px', textAlign: 'left'}}>📝 Notlar</th>
                                        <th style={{padding: '12px', textAlign: 'center'}}>⚙️ İşlemler</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {transactions.map((trans, index) => {
                                        // İlgili ürünü bul
                                        const product = products.find(p => p.id === trans.productId || p.name === trans.productName);
                                        const currentStock = product ? (product.stockQuantity || product.quantity || 0) : 0;
                                        const reorderLevel = product?.reorderLevel || 10;

                                        return (
                                            <tr key={trans.id} style={{
                                                borderBottom: '1px solid #ddd',
                                                background: index % 2 === 0 ? '#f9f9f9' : 'white',
                                                transition: 'background 0.2s'
                                            }}
                                            onMouseEnter={(e) => e.currentTarget.style.background = '#e8f4f8'}
                                            onMouseLeave={(e) => e.currentTarget.style.background = index % 2 === 0 ? '#f9f9f9' : 'white'}
                                            >
                                                <td style={{padding: '12px', fontSize: '13px'}}>
                                                    {trans.transactionDate ? new Date(trans.transactionDate).toLocaleDateString('tr-TR', {
                                                        day: '2-digit',
                                                        month: 'short',
                                                        year: 'numeric',
                                                        hour: '2-digit',
                                                        minute: '2-digit'
                                                    }) : 'N/A'}
                                                </td>
                                                <td style={{padding: '12px', fontWeight: 'bold', color: '#2d3748'}}>{trans.productName || 'N/A'}</td>
                                                <td style={{padding: '12px', textAlign: 'center'}}>
                                                    <span style={{
                                                        padding: '6px 12px',
                                                        borderRadius: '20px',
                                                        background: getTransactionColor(trans.transactionType),
                                                        color: 'white',
                                                        fontSize: '12px',
                                                        fontWeight: 'bold',
                                                        display: 'inline-block',
                                                        minWidth: '100px'
                                                    }}>
                                                        {getTransactionIcon(trans.transactionType)} {trans.transactionType}
                                                    </span>
                                                </td>
                                                <td style={{
                                                    padding: '12px',
                                                    textAlign: 'right',
                                                    fontWeight: 'bold',
                                                    fontSize: '16px',
                                                    color: trans.transactionType === 'PURCHASE' ? '#48bb78' : trans.transactionType === 'SALE' ? '#f56565' : '#ed8936'
                                                }}>
                                                    {trans.transactionType === 'PURCHASE' ? '+' : '-'}{trans.quantity}
                                                </td>
                                                <td style={{padding: '12px', textAlign: 'center'}}>
                                                    <div style={{
                                                        display: 'inline-block',
                                                        padding: '6px 12px',
                                                        borderRadius: '8px',
                                                        background: getStockStatusColor(currentStock, reorderLevel) + '20',
                                                        border: `2px solid ${getStockStatusColor(currentStock, reorderLevel)}`,
                                                        fontWeight: 'bold',
                                                        color: getStockStatusColor(currentStock, reorderLevel)
                                                    }}>
                                                        {currentStock} adet
                                                    </div>
                                                </td>
                                                <td style={{padding: '12px', textAlign: 'center'}}>
                                                    <span style={{
                                                        padding: '5px 10px',
                                                        borderRadius: '15px',
                                                        background: getStockStatusColor(currentStock, reorderLevel),
                                                        color: 'white',
                                                        fontSize: '11px',
                                                        fontWeight: 'bold',
                                                        display: 'inline-block',
                                                        minWidth: '90px'
                                                    }}>
                                                        {getStockStatusText(currentStock, reorderLevel)}
                                                    </span>
                                                </td>
                                                <td style={{padding: '12px', color: '#4a5568'}}>{trans.userName || 'N/A'}</td>
                                                <td style={{
                                                    padding: '12px',
                                                    maxWidth: '180px',
                                                    overflow: 'hidden',
                                                    textOverflow: 'ellipsis',
                                                    whiteSpace: 'nowrap',
                                                    color: '#718096',
                                                    fontSize: '13px'
                                                }} title={trans.notes}>
                                                    {trans.notes || '-'}
                                                </td>
                                                <td style={{padding: '12px', textAlign: 'center'}}>
                                                    <button
                                                        onClick={() => handleDelete(trans.id)}
                                                        style={{
                                                            padding: '6px 16px',
                                                            background: '#f56565',
                                                            color: 'white',
                                                            border: 'none',
                                                            borderRadius: '6px',
                                                            cursor: 'pointer',
                                                            fontWeight: 'bold',
                                                            fontSize: '13px',
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
                    <button onClick={() => navigate('/products')} className="action-btn">⬅ Ürünler</button>
                    <button onClick={() => navigate('/categories')} className="action-btn" style={{marginLeft: '10px'}}>Kategoriler</button>
                    <button onClick={() => navigate('/suppliers')} className="action-btn" style={{marginLeft: '10px'}}>Tedarikçiler</button>
                    <button onClick={() => navigate('/profile')} className="action-btn" style={{marginLeft: '10px'}}>Profil →</button>
                </div>
            </div>
        </div>
    );
}

export default TransactionPage;

