import { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { verifyEmail } from '../../api/authApi.js';
import { Button } from '../../components/common/button/Button.jsx';
import { Spinner } from '../../components/common/spinner/Spinner.jsx';
import styles from './Verify.module.css';

export const Verify = () => {
    const [searchParams] = useSearchParams();
    const [status, setStatus] = useState('loading');
    const [message, setMessage] = useState('');

    useEffect(() => {
        const token = searchParams.get('token');

        if (!token) {
            setStatus('error');
            setMessage('Invalid verification link. Token is missing.');
            return;
        }

        verifyEmail(token)
            .then((res) => {
                setStatus('success');
                setMessage(res);
            })
            .catch((err) => {
                setStatus('error');
                setMessage(err.response?.data || 'Verification failed. The link might be expired.');
            });
    }, [searchParams]);

    return (
        <div className={styles.container}>
            <div className={styles.card}>
                <h2 className={styles.title}>Email Verification</h2>

                {status === 'loading' && (
                    <div className={styles.loadingContainer}>
                        <Spinner />
                        <p className={styles.loadingText}>Verifying your account...</p>
                    </div>
                )}

                {status === 'success' && (
                    <>
                        <div className={styles.successBox}>
                            {message}
                        </div>
                        <div className={styles.inputGroup}>
                            <Link to="/login" className={styles.linkBlock}>
                                <Button type="button">Go to Login</Button>
                            </Link>
                        </div>
                    </>
                )}

                {status === 'error' && (
                    <>
                        <div className={styles.errorBox}>
                            {message}
                        </div>
                        <div className={styles.inputGroup}>
                            <Link to="/register" className={styles.linkBlock}>
                                <Button type="button">Register Again</Button>
                            </Link>
                        </div>
                    </>
                )}
            </div>
        </div>
    );
};