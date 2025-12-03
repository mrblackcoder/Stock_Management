import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import ApiService from '../service/ApiService';
import './LoginPage.css';

function LoginPage() {
    const [formData, setFormData] = useState({
        username: '',
        password: ''
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
        setError('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            const response = await ApiService.loginUser(formData);

            if (response.statusCode === 200) {
                // Save authentication data
                ApiService.saveToken(response.token);
                
                // Save refresh token if available
                if (response.refreshToken) {
                    ApiService.saveRefreshToken(response.refreshToken);
                }
                
                ApiService.saveRole(response.user?.role || 'USER');
                ApiService.saveUsername(response.user?.username || formData.username);

                // Save complete user data (encrypted)
                if (response.user) {
                    ApiService.saveUser(response.user);
                }

                navigate('/dashboard');
            } else {
                setError(response.message || 'Login failed');
            }
        } catch (err) {
            console.error('Login error:', err);
            setError(err.response?.data?.message || 'Login failed. Please check your credentials.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-container">
            <div className="login-card">
                <div className="login-header">
                    <i className="fas fa-box-open"></i>
                    <h2>Inventory Management System</h2>
                    <p>Login to your account</p>
                </div>

                {error && (
                    <div className="alert alert-danger">
                        <i className="fas fa-exclamation-circle"></i> {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="login-form">
                    <div className="form-group">
                        <label htmlFor="username">
                            <i className="fas fa-user"></i> Username
                        </label>
                        <input
                            type="text"
                            id="username"
                            name="username"
                            value={formData.username}
                            onChange={handleChange}
                            required
                            placeholder="Enter your username"
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="password">
                            <i className="fas fa-lock"></i> Password
                        </label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            required
                            placeholder="Enter your password"
                        />
                    </div>

                    <button type="submit" className="btn-login" disabled={loading}>
                        {loading ? (
                            <>
                                <i className="fas fa-spinner fa-spin"></i> Logging in...
                            </>
                        ) : (
                            <>
                                <i className="fas fa-sign-in-alt"></i> Login
                            </>
                        )}
                    </button>
                </form>

                <div className="login-footer">
                    <p>Don't have an account? <Link to="/register">Register here</Link></p>
                </div>
            </div>
        </div>
    );
}

export default LoginPage;

