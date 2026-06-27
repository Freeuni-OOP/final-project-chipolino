import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';

import { Input } from '../../components/common/input/Input.jsx';
import { Button } from '../../components/common/button/Button.jsx';
import { Spinner } from '../../components/common/spinner/Spinner.jsx';
import { useAuth } from '../../hooks/useAuth.js';
import styles from './Login.module.css';

/**
 * Authentication component for user login.
 * <p>The page performs the following tasks:
 * <ul>
 * <li>Manages local state for user input (username/password), form validation errors, and loading states.</li>
 * <li>Uses the {@link useAuth} hook to handle the authentication process and track session state.</li>
 * <li>Redirects authenticated users automatically to the map page via {@link useEffect} if a valid session exists.</li>
 * <li>Performs client-side validation to ensure fields are not empty before API submission.</li>
 * <li>Implements error handling to capture and display API response messages to the user.</li>
 * <li>Provides a responsive UI with a loading spinner during the login process and navigation links to the registration page.</li>
 * </ul>
 * </p>
 * @returns A JSX element containing a login form with input fields, error display, and submission controls.
 */
const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [errors, setErrors] = useState({});
    const [isLoading, setIsLoading] = useState(false);

    const navigate = useNavigate();
    const { handleLogin, user, loading: authLoading } = useAuth();

    useEffect(() => {
        if (!authLoading && user) {
            navigate('/map', { replace: true });
        }
    }, [user, authLoading, navigate]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrors({});

        const newErrors = {};
        if (!username.trim()) newErrors.username = 'Username is required';
        if (!password) newErrors.password = 'Password is required';

        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            return;
        }

        setIsLoading(true);

        try {
            await handleLogin({ username, password });
            navigate('/map', { replace: true });
        } catch (err) {
            const errorMessage = err.response?.data?.message || 'Invalid credentials. Please try again.';
            setErrors({ general: errorMessage });
        } finally {
            setIsLoading(false);
        }
    };

    if (authLoading) {
        return <Spinner fullScreen />;
    }

    return (
        <div className={styles.container}>
            <div className={styles.card}>
                <h2 className={styles.title}>Login</h2>
                {errors.general && (
                    <div className={styles.errorBox}>
                        {errors.general}
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    <div className={styles.inputGroup}>
                        <Input
                            label="Username"
                            type="text"
                            placeholder="Type username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            disabled={isLoading}
                            error={errors.username}
                        />
                    </div>

                    <div className={styles.inputGroup}>
                        <Input
                            label="Password"
                            type="password"
                            placeholder="Type password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            disabled={isLoading}
                            error={errors.password}
                        />
                    </div>

                    <div className={styles.inputGroup}>
                        <Button
                            type="submit"
                            disabled={isLoading}
                        >
                            {isLoading ? <Spinner /> : 'Login'}
                        </Button>
                    </div>

                    <p className={styles.footerText}>
                        Don't have an account?{' '}
                        <Link to="/register" className={styles.link}>
                            Register
                        </Link>
                    </p>

                </form>
            </div>
        </div>
    );

};
export default Login;