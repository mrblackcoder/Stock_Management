import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import ApiService from '../service/ApiService';
import './Layout.css';

function Layout({ children }) {
    const navigate = useNavigate();
    const user = ApiService.getUser() || { username: 'Guest', role: 'USER' };
    const role = ApiService.getRole();
    const isAdmin = role === 'ADMIN';

    const handleLogout = () => {
        ApiService.clearAuth();
        navigate('/login');
    };

    return (
        <div className="layout-container">
            {/* Sidebar */}
            <aside className="sidebar">
                <div className="sidebar-header">
                    <h2>IMS</h2>
                    <p>Inventory Management</p>
                </div>

                <nav className="sidebar-nav">
                    <NavLink to="/dashboard" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
                        <span className="nav-icon">■</span>
                        <span className="nav-text">Dashboard</span>
                    </NavLink>

                    <NavLink to="/products" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
                        <span className="nav-icon">▪</span>
                        <span className="nav-text">Products</span>
                    </NavLink>

                    {/* Admin Only Menu Items */}
                    {isAdmin && (
                        <>
                            <NavLink to="/categories" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
                                <span className="nav-icon">●</span>
                                <span className="nav-text">Categories</span>
                            </NavLink>

                            <NavLink to="/suppliers" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
                                <span className="nav-icon">◆</span>
                                <span className="nav-text">Suppliers</span>
                            </NavLink>

                            <NavLink to="/transactions" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
                                <span className="nav-icon">↻</span>
                                <span className="nav-text">Transactions</span>
                            </NavLink>
                        </>
                    )}

                    <NavLink to="/profile" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
                        <span className="nav-icon">⚙</span>
                        <span className="nav-text">Profile</span>
                    </NavLink>
                </nav>

                <div className="sidebar-footer">
                    <button className="logout-btn" onClick={handleLogout}>
                        ← Logout
                    </button>
                </div>
            </aside>

            {/* Main Content */}
            <main className="main-content">
                {/* Top Header Bar */}
                <header className="top-header">
                    <div className="header-left">
                        <h1 className="page-title">Inventory Management System</h1>
                    </div>
                    <div className="header-right">
                        <div className="user-profile">
                            <div className="user-avatar">
                                {(user.username || 'G').charAt(0).toUpperCase()}
                            </div>
                            <div className="user-details">
                                <span className="user-display-name">{user.username || 'Guest'}</span>
                                <span className={`user-role-badge ${role?.toLowerCase() || 'user'}`}>
                                    {isAdmin ? '👑 Admin' : '👤 User'}
                                </span>
                            </div>
                        </div>
                        <button className="header-logout-btn" onClick={handleLogout} title="Logout">
                            🚪 Çıkış
                        </button>
                    </div>
                </header>

                <div className="content-wrapper">
                    {children}
                </div>
            </main>
        </div>
    );
}

export default Layout;

