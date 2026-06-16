import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';

import { Input } from '../components/common/input/Input';
import { Button } from '../components/common/button/Button';
import  { Spinner } from '../components/common/spinner/Spinner'
import { useAuth } from '../hooks/useAuth';
import styles from './Login.module.css';

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [errors, setErrors] = useState({});
    const [isLoading, setIsLoading] = useState(false);

    const navigate = useNavigate();
    const { handleLogin } = useAuth();

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
            navigate('/');
        } catch (err) {
            const errorMessage = err.response?.data?.message || 'Invalid credentials. Please try again.';
            setErrors({ general: errorMessage });
        } finally {
            setIsLoading(false);
        }
    };
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
                        <Button
                            type="submit"
                            disabled={isLoading}
                        >
                            {isLoading ? <Spinner /> : 'Login'}
                        </Button>
                    </div>

                    <p className={styles.footerText}>
                        do not have account?{' '}
                        <Link to="/register" className={styles.link}>
                            register
                        </Link>
                    </p>

                </form>
            </div>
        </div>
    );

};
export default Login;