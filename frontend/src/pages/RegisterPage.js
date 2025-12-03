import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import ApiService from '../service/ApiService';
import './LoginPage.css';

function RegisterPage() {
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        fullName: ''
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const [passwordStrength, setPasswordStrength] = useState({ score: 0, label: '', errors: [] });
    const navigate = useNavigate();

    // Password strength calculator
    const calculatePasswordStrength = (password) => {
        if (!password) {
            return { score: 0, label: '', errors: [] };
        }

        let score = 0;
        let errors = [];

        // Length check
        if (password.length < 8) {
            errors.push('At least 8 characters');
        } else {
            score += Math.min(password.length * 2, 25);
        }

        // Character variety
        if (!/[a-z]/.test(password)) {
            errors.push('One lowercase letter');
        } else {
            score += 5;
        }

        if (!/[A-Z]/.test(password)) {
            errors.push('One uppercase letter');
        } else {
            score += 5;
        }

        if (!/[0-9]/.test(password)) {
            errors.push('One digit');
        } else {
            score += 5;
        }

        // eslint-disable-next-line no-useless-escape
        if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) {
            errors.push('One special character');
        } else {
            score += 10;
        }

        // Bonus for length > 12
        if (password.length > 12) {
            score += Math.min((password.length - 12) * 2, 20);
        }

        // Common password check
        const commonPasswords = ['password', '123456', 'qwerty', 'admin', 'letmein', 'welcome'];
        if (commonPasswords.some(p => password.toLowerCase().includes(p))) {
            score -= 30;
            errors.push('Avoid common passwords');
        }

        score = Math.max(0, Math.min(100, score));

        let label = 'Very Weak';
        if (score >= 80) label = 'Very Strong';
        else if (score >= 60) label = 'Strong';
        else if (score >= 40) label = 'Fair';
        else if (score >= 20) label = 'Weak';

        return { score, label, errors };
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({
            ...formData,
            [name]: value
        });
        setError('');

        // Update password strength when password changes
        if (name === 'password') {
            setPasswordStrength(calculatePasswordStrength(value));
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        // Client-side validation
        if (passwordStrength.score < 40) {
            setError('Please choose a stronger password');
            setLoading(false);
            return;
        }

        try {
            const response = await ApiService.registerUser(formData);

            if (response.statusCode === 200 || response.statusCode === 201) {
                navigate('/login');
            } else {
                setError(response.message || 'Registration failed');
            }
        } catch (err) {
            setError(err.response?.data?.message || 'Registration failed.');
        } finally {
            setLoading(false);
        }
    };

    const getStrengthColor = () => {
        if (passwordStrength.score >= 80) return '#28a745';
        if (passwordStrength.score >= 60) return '#5cb85c';
        if (passwordStrength.score >= 40) return '#f0ad4e';
        if (passwordStrength.score >= 20) return '#ff9800';
        return '#dc3545';
    };

    return (
        <div className="login-container">
            <div className="login-card">
                <div className="login-header">
                    <i className="fas fa-user-plus"></i>
                    <h2>Create Account</h2>
                    <p>Register for Inventory Management System</p>
                </div>

                {error && (
                    <div className="alert alert-danger">
                        <i className="fas fa-exclamation-circle"></i> {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="login-form">
                    <div className="form-group">
                        <label htmlFor="fullName">
                            <i className="fas fa-id-card"></i> Full Name
                        </label>
                        <input
                            type="text"
                            id="fullName"
                            name="fullName"
                            value={formData.fullName}
                            onChange={handleChange}
                            required
                            placeholder="Enter your full name"
                        />
                    </div>

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
                            placeholder="Choose a username"
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="email">
                            <i className="fas fa-envelope"></i> Email
                        </label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            value={formData.email}
                            onChange={handleChange}
                            required
                            placeholder="Enter your email"
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
                            placeholder="Choose a strong password"
                        />
                        {formData.password && (
                            <div className="password-strength">
                                <div className="strength-bar">
                                    <div 
                                        className="strength-fill" 
                                        style={{ 
                                            width: `${passwordStrength.score}%`,
                                            backgroundColor: getStrengthColor()
                                        }}
                                    ></div>
                                </div>
                                <span className="strength-label" style={{ color: getStrengthColor() }}>
                                    {passwordStrength.label}
                                </span>
                                {passwordStrength.errors.length > 0 && (
                                    <ul className="password-requirements">
                                        {passwordStrength.errors.map((err, idx) => (
                                            <li key={idx}><i className="fas fa-times"></i> {err}</li>
                                        ))}
                                    </ul>
                                )}
                            </div>
                        )}
                    </div>

                    <button type="submit" className="btn-login" disabled={loading}>
                        {loading ? (
                            <>
                                <i className="fas fa-spinner fa-spin"></i> Registering...
                            </>
                        ) : (
                            <>
                                <i className="fas fa-user-plus"></i> Register
                            </>
                        )}
                    </button>
                </form>

                <div className="login-footer">
                    <p>Already have an account? <Link to="/login">Login here</Link></p>
                </div>
            </div>
        </div>
    );
}

export default RegisterPage;

