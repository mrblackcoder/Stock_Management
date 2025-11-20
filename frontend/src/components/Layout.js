import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import ApiService from '../service/ApiService';
import './Layout.css';

function Layout({ children }) {
    const navigate = useNavigate();
    const user = ApiService.getUser() || { username: 'Guest', role: 'USER' };

    const handleLogout = () => {
        ApiService.clearAuth();
        navigate('/login');
    };

    return (
        <div className="layout-container">
            {/* Sidebar */}
            <aside className="sidebar">
                <div className="sidebar-header">
                    <h2>📦 IMS</h2>
                    <p>Inventory Management</p>
                </div>

                <nav className="sidebar-nav">
                    <NavLink to="/dashboard" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
                        <span className="nav-icon">📊</span>
                        <span className="nav-text">Dashboard</span>
                    </NavLink>

                    <NavLink to="/products" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
                        <span className="nav-icon">📦</span>
                        <span className="nav-text">Products</span>
                    </NavLink>

                    <NavLink to="/categories" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
                        <span className="nav-icon">🏷️</span>
                        <span className="nav-text">Categories</span>
                    </NavLink>

                    <NavLink to="/suppliers" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
                        <span className="nav-icon">🚚</span>
                        <span className="nav-text">Suppliers</span>
                    </NavLink>

                    <NavLink to="/transactions" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
                        <span className="nav-icon">🔄</span>
                        <span className="nav-text">Transactions</span>
                    </NavLink>

                    <NavLink to="/profile" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
                        <span className="nav-icon">👤</span>
                        <span className="nav-text">Profile</span>
                    </NavLink>
                </nav>

                <div className="sidebar-footer">
                    <div className="user-info">
                        <span className="user-icon">👤</span>
                        <span className="user-name">{user.username || 'User'}</span>
                    </div>
                    <button className="logout-btn" onClick={handleLogout}>
                        🚪 Logout
                    </button>
                </div>
            </aside>

            {/* Main Content */}
            <main className="main-content">
                <div className="content-wrapper">
                    {children}
                </div>
            </main>
        </div>
    );
}

export default Layout;

