import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../css/LoginPage.css';
import Navbar from '../components/navbar';

const LoginPage = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    const handleAdminLogin = async (e) => {
        e.preventDefault();
        try {
            const response = await fetch('http://localhost:8080/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password }),
            });

            if (response.ok) {
                const data = await response.json();
                localStorage.setItem('token', data.token); // Armazena o token
                navigate('/admin'); // Redireciona para a página de admin
            } else {
                setError('Invalid credentials. Please try again.');
            }
        } catch (error) {
            console.error('Error during login:', error);
            setError('Something went wrong. Please try again later.');
        }
    };

    const handleGuestLogin = () => {
        navigate('/'); // Redireciona para a HomePage
    };

    return (
        <div className="login-page">
            <Navbar />
            <div className="login-form">
                <h2>Login</h2>
                <form onSubmit={handleAdminLogin}>
                    <div className="form-group">
                        <label>Username:</label>
                        <input
                            type="text"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                             maxLength={128}
                        />
                    </div>
                    <div className="form-group">
                        <label>Password:</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                             maxLength={128}
                        />
                    </div>
                    {error && <div className="error-message">{error}</div>}
                    <button type="submit" className="login-button">Login as Admin</button>
                </form>
                <button className="guest-button" onClick={handleGuestLogin}>Enter as Guest</button>
            </div>
        </div>
    );
};

export default LoginPage;
