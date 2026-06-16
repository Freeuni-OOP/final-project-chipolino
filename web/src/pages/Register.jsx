import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';

import { Input } from '../components/common/input/Input';
import { Button } from '../components/common/button/Button';
import  { Spinner } from '../components/common/spinner/Spinner'
import { useAuth } from '../hooks/useAuth';
import styles from './Register.module.css';

const Register = () => {
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [errors, setErrors] = useState({});
    const [isLoading, setIsLoading] = useState(false);

    const navigate = useNavigate();
    const { handleRegister } = useAuth();

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
            navigate('/');
        } catch (err) {
            const errorMessage = err.response?.data?.message || 'Registration failed. Please try again.';
            setErrors({ general: errorMessage });
        } finally {
            setIsLoading(false);
        }
    };
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
                            label="username"
                            type="text"
                            placeholder="type username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            disabled={isLoading}
                            error={errors.username}
                        />
                    </div>

                    <div className={styles.inputGroup}>
                        <Input
                            label="email"
                            type="email"
                            placeholder="type email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            disabled={isLoading}
                            error={errors.email}
                        />
                    </div>

                    <div className={styles.inputGroup}>
                        <Input
                            label="password"
                            type="password"
                            placeholder="type password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            disabled={isLoading}
                            error={errors.password}
                        />
                    </div>

                    <div className={styles.inputGroup}>
                        <Input
                            label="confirm password"
                            type="password"
                            placeholder="confirm password"
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
                        already have an account?{' '}
                        <Link to="/login" className={styles.link}>
                            login
                        </Link>
                    </p>


                </form>
            </div>
        </div>
    );
};

export default Register;