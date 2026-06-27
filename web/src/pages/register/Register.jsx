import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';

import { Input } from '../../components/common/input/Input.jsx';
import { Button } from '../../components/common/button/Button.jsx';
import { Spinner } from '../../components/common/spinner/Spinner.jsx'
import { useAuth } from '../../hooks/useAuth.js';
import styles from './Register.module.css';

/**
 * A registration component that enables new users to create an account on the platform.
 * <p>The component performs the following tasks:
 * <ul>
 * <li>Manages local state for user input (username, email, password, and password confirmation), form validation errors, and loading states.</li>
 * <li>Utilizes the {@link useAuth} hook to manage the registration request and track session state.</li>
 * <li>Automatically redirects authenticated users to the map page via {@link useEffect} if a session is already active.</li>
 * <li>Performs client-side validation to ensure required fields are present and that the password and confirm password fields match.</li>
 * <li>Captures and displays API error messages to the user if registration fails.</li>
 * <li>Provides a responsive UI with a loading spinner during the registration process and navigation links to the login page.</li>
 * </ul>
 * </p>
 * @returns A JSX element containing a registration form with input fields, error handling display, and submission controls.
 */
const Register = () => {
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [errors, setErrors] = useState({});
    const [isLoading, setIsLoading] = useState(false);

    const navigate = useNavigate();
    const { handleRegister, user, loading: authLoading } = useAuth();

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
        if (!email.trim()) newErrors.email = 'Email is required';
        if (password !== confirmPassword) {
            newErrors.confirmPassword = 'Passwords do not match';
        }

        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            return;
        }

        setIsLoading(true);

        try {
            await handleRegister({ username, email, password });
            navigate('/map', { replace: true });
        } catch (err) {
            const errorMessage = err.response?.data?.message || 'Registration failed. Please try again.';
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
                <h2 className={styles.title}>Register</h2>

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
                            label="Email"
                            type="email"
                            placeholder="Eype email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            disabled={isLoading}
                            error={errors.email}
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
                        <Input
                            label="Confirm password"
                            type="password"
                            placeholder="Confirm password"
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                            disabled={isLoading}
                            error={errors.confirmPassword}
                        />
                    </div>

                    <div className={styles.inputGroup}>
                        <Button
                            type="submit"
                            disabled={isLoading}
                        >
                            {isLoading ? <Spinner /> : 'Register'}
                        </Button>
                    </div>

                    <p className={styles.footerText}>
                        Already have an account?{' '}
                        <Link to="/login" className={styles.link}>
                            Login
                        </Link>
                    </p>


                </form>
            </div>
        </div>
    );
};

export default Register;