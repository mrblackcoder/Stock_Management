import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import ApiService from '../service/ApiService';
import './DashboardPage.css';

function SupplierPage() {
    const [suppliers, setSuppliers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showForm, setShowForm] = useState(false);
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        name: '', email: '', phone: '', address: '', description: ''
    });

    useEffect(() => {
        fetchSuppliers();
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
                    <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
                        <h2>🏢 Tedarikçiler</h2>
                        <button onClick={() => { setShowForm(!showForm); setFormData({ name: '', email: '', phone: '', address: '', description: '' }); }}
                                className="action-btn">
                            {showForm ? 'İptal' : '+ Yeni Tedarikçi'}
                        </button>
                    </div>

                    {error && <div style={{color: 'red', margin: '10px 0'}}>{error}</div>}

                    {showForm && (
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
                                        <th style={{padding: '12px', textAlign: 'center'}}>İşlemler</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {suppliers.map((sup, index) => (
                                        <tr key={sup.id} style={{borderBottom: '1px solid #ddd', background: index % 2 === 0 ? '#f9f9f9' : 'white'}}>
                                            <td style={{padding: '12px'}}>{sup.name}</td>
                                            <td style={{padding: '12px'}}>{sup.email || 'N/A'}</td>
                                            <td style={{padding: '12px'}}>{sup.phone || 'N/A'}</td>
                                            <td style={{padding: '12px', textAlign: 'center'}}>
                                                <button onClick={() => handleDelete(sup.id)} style={{padding: '5px 15px', background: '#f56565', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer'}}>Sil</button>
                                            </td>
                                        </tr>
                                    ))}
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

