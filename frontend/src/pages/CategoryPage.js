import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import ApiService from '../service/ApiService';
import './CategoryPage.css';

// Category colors and icons based on name keywords
const getCategoryStyle = (name, index) => {
    const styles = [
        { bg: '#667eea', icon: 'fa-laptop', colorEnd: '#764ba2' },
        { bg: '#48bb78', icon: 'fa-leaf', colorEnd: '#38a169' },
        { bg: '#ed8936', icon: 'fa-fire', colorEnd: '#dd6b20' },
        { bg: '#e53e3e', icon: 'fa-heart', colorEnd: '#c53030' },
        { bg: '#38b2ac', icon: 'fa-water', colorEnd: '#319795' },
        { bg: '#9f7aea', icon: 'fa-gem', colorEnd: '#805ad5' },
        { bg: '#ed64a6', icon: 'fa-star', colorEnd: '#d53f8c' },
        { bg: '#4299e1', icon: 'fa-bolt', colorEnd: '#3182ce' }
    ];

    const lowerName = name?.toLowerCase() || '';
    
    if (lowerName.includes('electron') || lowerName.includes('tech')) {
        return { bg: '#4299e1', icon: 'fa-microchip', colorEnd: '#3182ce' };
    }
    if (lowerName.includes('computer') || lowerName.includes('laptop')) {
        return { bg: '#667eea', icon: 'fa-laptop', colorEnd: '#764ba2' };
    }
    if (lowerName.includes('phone') || lowerName.includes('mobile')) {
        return { bg: '#38b2ac', icon: 'fa-mobile-screen', colorEnd: '#319795' };
    }
    if (lowerName.includes('cloth') || lowerName.includes('fashion')) {
        return { bg: '#ed64a6', icon: 'fa-shirt', colorEnd: '#d53f8c' };
    }
    if (lowerName.includes('food') || lowerName.includes('grocery')) {
        return { bg: '#48bb78', icon: 'fa-utensils', colorEnd: '#38a169' };
    }
    if (lowerName.includes('book') || lowerName.includes('education')) {
        return { bg: '#ed8936', icon: 'fa-book', colorEnd: '#dd6b20' };
    }
    if (lowerName.includes('sport') || lowerName.includes('fitness')) {
        return { bg: '#e53e3e', icon: 'fa-dumbbell', colorEnd: '#c53030' };
    }
    if (lowerName.includes('home') || lowerName.includes('furniture')) {
        return { bg: '#9f7aea', icon: 'fa-couch', colorEnd: '#805ad5' };
    }
    if (lowerName.includes('toy') || lowerName.includes('game')) {
        return { bg: '#f6ad55', icon: 'fa-gamepad', colorEnd: '#ed8936' };
    }
    if (lowerName.includes('health') || lowerName.includes('medicine')) {
        return { bg: '#fc8181', icon: 'fa-heart-pulse', colorEnd: '#f56565' };
    }
    if (lowerName.includes('accessory') || lowerName.includes('accessories')) {
        return { bg: '#faf089', icon: 'fa-headphones', colorEnd: '#ecc94b' };
    }

    return styles[index % styles.length];
};

function CategoryPage() {
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showForm, setShowForm] = useState(false);
    const [editMode, setEditMode] = useState(false);
    const [currentCategory, setCurrentCategory] = useState({ id: null, name: '', description: '' });
    const [searchTerm, setSearchTerm] = useState('');
    const navigate = useNavigate();
    const role = ApiService.getRole();
    const isAdmin = role === 'ADMIN';

    useEffect(() => {
        fetchCategories();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const fetchCategories = async () => {
        try {
            setLoading(true);
            const response = await ApiService.getAllCategories();
            setCategories(response.categoryList || []);
            setError('');
        } catch (err) {
            setError('Failed to load categories: ' + err.message);
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
            setError('Operation failed: ' + err.message);
        }
    };

    const handleEdit = (category) => {
        setCurrentCategory(category);
        setEditMode(true);
        setShowForm(true);
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this category?')) {
            try {
                await ApiService.deleteCategory(id);
                fetchCategories();
            } catch (err) {
                setError('Delete failed: ' + err.message);
            }
        }
    };

    const resetForm = () => {
        setShowForm(false);
        setEditMode(false);
        setCurrentCategory({ id: null, name: '', description: '' });
    };

    if (loading) {
        return (
            <div className="dashboard-page">
                <div className="dashboard-container">
                    <div className="loading">
                        <div className="spinner"></div>
                        <p>Loading categories...</p>
                    </div>
                </div>
            </div>
        );
    }

    const filteredCategories = categories.filter(cat =>
        cat.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        cat.description?.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="dashboard-page">
            <div className="dashboard-container">
                <div className="category-page">
                    {/* Header */}
                    <div className="category-header">
                        <h2>
                            <i className="fa-solid fa-folder-tree"></i>
                            Categories
                        </h2>
                        <div className="category-controls">
                            <div className="search-box">
                                <i className="fa-solid fa-magnifying-glass"></i>
                                <input
                                    type="text"
                                    placeholder="Search categories..."
                                    value={searchTerm}
                                    onChange={(e) => setSearchTerm(e.target.value)}
                                />
                            </div>
                            <span className="category-count">
                                <i className="fa-solid fa-layer-group"></i> {categories.length} categories
                            </span>
                            {isAdmin && (
                                <button 
                                    className={`btn-add-category ${showForm ? 'cancel' : ''}`}
                                    onClick={() => showForm ? resetForm() : setShowForm(true)}
                                >
                                    {showForm ? (
                                        <><i className="fa-solid fa-xmark"></i> Cancel</>
                                    ) : (
                                        <><i className="fa-solid fa-plus"></i> New Category</>
                                    )}
                                </button>
                            )}
                        </div>
                    </div>

                    {/* Error Message */}
                    {error && (
                        <div className="error-message">
                            <i className="fa-solid fa-circle-exclamation"></i>
                            {error}
                        </div>
                    )}

                    {/* Add/Edit Form */}
                    {showForm && isAdmin && (
                        <form className="category-form" onSubmit={handleSubmit}>
                            <h3>
                                <i className={`fa-solid ${editMode ? 'fa-pen-to-square' : 'fa-plus-circle'}`}></i>
                                {editMode ? 'Edit Category' : 'Add New Category'}
                            </h3>
                            <div className="form-row">
                                <div className="form-group">
                                    <label>Category Name *</label>
                                    <input
                                        type="text"
                                        value={currentCategory.name}
                                        onChange={(e) => setCurrentCategory({...currentCategory, name: e.target.value})}
                                        placeholder="Enter category name"
                                        required
                                    />
                                </div>
                                <div className="form-group">
                                    <label>Description</label>
                                    <textarea
                                        value={currentCategory.description}
                                        onChange={(e) => setCurrentCategory({...currentCategory, description: e.target.value})}
                                        placeholder="Enter category description"
                                        rows="3"
                                    />
                                </div>
                            </div>
                            <div className="form-actions">
                                <button type="submit" className="btn-save">
                                    <i className={`fa-solid ${editMode ? 'fa-check' : 'fa-floppy-disk'}`}></i>
                                    {editMode ? 'Update' : 'Save'}
                                </button>
                            </div>
                        </form>
                    )}

                    {/* Category Grid */}
                    {filteredCategories.length === 0 ? (
                        <div className="empty-state">
                            <i className="fa-solid fa-folder-open"></i>
                            <h3>{searchTerm ? 'No categories found' : 'No categories yet'}</h3>
                            <p>{searchTerm ? 'Try a different search term' : 'Add your first category to get started'}</p>
                        </div>
                    ) : (
                        <div className="category-grid">
                            {filteredCategories.map((cat, index) => {
                                const style = getCategoryStyle(cat.name, index);
                                return (
                                    <div 
                                        key={cat.id} 
                                        className="category-card"
                                        style={{ '--card-color': style.bg, '--card-color-end': style.colorEnd }}
                                    >
                                        <div className="category-card-header">
                                            <div className="category-icon" style={{ background: style.bg }}>
                                                <i className={`fa-solid ${style.icon}`}></i>
                                            </div>
                                            <div className="category-card-title">
                                                <h3>{cat.name}</h3>
                                                <span>ID: {cat.id}</span>
                                            </div>
                                        </div>
                                        <p className="category-card-description">
                                            {cat.description || 'No description available'}
                                        </p>
                                        {isAdmin && (
                                            <div className="category-card-actions">
                                                <button className="btn-edit" onClick={() => handleEdit(cat)}>
                                                    <i className="fa-solid fa-pen"></i> Edit
                                                </button>
                                                <button className="btn-delete" onClick={() => handleDelete(cat.id)}>
                                                    <i className="fa-solid fa-trash"></i> Delete
                                                </button>
                                            </div>
                                        )}
                                    </div>
                                );
                            })}
                        </div>
                    )}

                    {/* Navigation */}
                    <div className="page-navigation">
                        <button className="btn-nav" onClick={() => navigate('/dashboard')}>
                            <i className="fa-solid fa-arrow-left"></i> Dashboard
                        </button>
                        <button className="btn-nav" onClick={() => navigate('/products')}>
                            Products <i className="fa-solid fa-arrow-right"></i>
                        </button>
                        <button className="btn-nav" onClick={() => navigate('/suppliers')}>
                            Suppliers <i className="fa-solid fa-arrow-right"></i>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default CategoryPage;



