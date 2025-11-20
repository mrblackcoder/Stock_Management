import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import ApiService from '../service/ApiService';
import './DashboardPage.css';

function CategoryPage() {
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showForm, setShowForm] = useState(false);
    const [editMode, setEditMode] = useState(false);
    const [currentCategory, setCurrentCategory] = useState({ id: null, name: '', description: '' });
    const navigate = useNavigate();

    useEffect(() => {
        fetchCategories();
    }, []);

    const fetchCategories = async () => {
        try {
            setLoading(true);
            const response = await ApiService.getAllCategories();
            setCategories(response.categoryList || []);
            setError('');
        } catch (err) {
            setError('Kategoriler yüklenemedi: ' + err.message);
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
            if (editMode) {
                await ApiService.updateCategory(currentCategory.id, currentCategory);
            } else {
                await ApiService.createCategory(currentCategory);
            }
            setShowForm(false);
            setCurrentCategory({ id: null, name: '', description: '' });
            setEditMode(false);
            fetchCategories();
        } catch (err) {
            setError('İşlem başarısız: ' + err.message);
        }
    };

    const handleEdit = (category) => {
        setCurrentCategory(category);
        setEditMode(true);
        setShowForm(true);
    };

    const handleDelete = async (id) => {
        if (window.confirm('Bu kategoriyi silmek istediğinizden emin misiniz?')) {
            try {
                await ApiService.deleteCategory(id);
                fetchCategories();
            } catch (err) {
                setError('Silme işlemi başarısız: ' + err.message);
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
                        <h2>📁 Kategoriler</h2>
                        <button onClick={() => { setShowForm(!showForm); setEditMode(false); setCurrentCategory({ id: null, name: '', description: '' }); }}
                                className="action-btn">
                            {showForm ? 'İptal' : '+ Yeni Kategori'}
                        </button>
                    </div>

                    {error && <div style={{color: 'red', margin: '10px 0'}}>{error}</div>}

                    {showForm && (
                        <form onSubmit={handleSubmit} style={{marginTop: '20px', padding: '20px', background: '#f5f5f5', borderRadius: '10px'}}>
                            <div style={{marginBottom: '15px'}}>
                                <label style={{display: 'block', marginBottom: '5px'}}>Kategori Adı *</label>
                                <input
                                    type="text"
                                    value={currentCategory.name}
                                    onChange={(e) => setCurrentCategory({...currentCategory, name: e.target.value})}
                                    required
                                    style={{width: '100%', padding: '10px', borderRadius: '5px', border: '1px solid #ddd'}}
                                />
                            </div>
                            <div style={{marginBottom: '15px'}}>
                                <label style={{display: 'block', marginBottom: '5px'}}>Açıklama</label>
                                <textarea
                                    value={currentCategory.description}
                                    onChange={(e) => setCurrentCategory({...currentCategory, description: e.target.value})}
                                    rows="3"
                                    style={{width: '100%', padding: '10px', borderRadius: '5px', border: '1px solid #ddd'}}
                                />
                            </div>
                            <button type="submit" className="action-btn">{editMode ? 'Güncelle' : 'Kaydet'}</button>
                        </form>
                    )}

                    <div style={{marginTop: '30px'}}>
                        {categories.length === 0 ? (
                            <p>Henüz kategori eklenmemiş.</p>
                        ) : (
                            <table style={{width: '100%', borderCollapse: 'collapse'}}>
                                <thead>
                                    <tr style={{background: '#667eea', color: 'white'}}>
                                        <th style={{padding: '12px', textAlign: 'left'}}>ID</th>
                                        <th style={{padding: '12px', textAlign: 'left'}}>Ad</th>
                                        <th style={{padding: '12px', textAlign: 'left'}}>Açıklama</th>
                                        <th style={{padding: '12px', textAlign: 'center'}}>İşlemler</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {categories.map((cat, index) => (
                                        <tr key={cat.id} style={{borderBottom: '1px solid #ddd', background: index % 2 === 0 ? '#f9f9f9' : 'white'}}>
                                            <td style={{padding: '12px'}}>{cat.id}</td>
                                            <td style={{padding: '12px'}}>{cat.name}</td>
                                            <td style={{padding: '12px'}}>{cat.description}</td>
                                            <td style={{padding: '12px', textAlign: 'center'}}>
                                                <button onClick={() => handleEdit(cat)} style={{marginRight: '10px', padding: '5px 15px', background: '#48bb78', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer'}}>Düzenle</button>
                                                <button onClick={() => handleDelete(cat.id)} style={{padding: '5px 15px', background: '#f56565', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer'}}>Sil</button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        )}
                    </div>
                </div>

                <div style={{marginTop: '30px', textAlign: 'center'}}>
                    <button onClick={() => navigate('/dashboard')} className="action-btn">⬅ Dashboard'a Dön</button>
                    <button onClick={() => navigate('/products')} className="action-btn" style={{marginLeft: '10px'}}>Ürünler →</button>
                    <button onClick={() => navigate('/suppliers')} className="action-btn" style={{marginLeft: '10px'}}>Tedarikçiler →</button>
                </div>
            </div>
        </div>
    );
}

export default CategoryPage;



