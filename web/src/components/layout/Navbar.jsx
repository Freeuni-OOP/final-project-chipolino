import React from 'react';
import styles from './Navbar.module.css';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import {useAuth} from "../../hooks/useAuth.js";

const Navbar = () => {
    const navigate = useNavigate();
    const { user, loading, handleLogout } = useAuth();

    const [searchParams] = useSearchParams();
    const isViewingMine = searchParams.get('view') === 'mine';

    const logout = async () => {
        try {
            await handleLogout();
            navigate('/login');
        } catch (err) {
            console.error("Logout failed:", err);
        }
    };

    if (loading) {
        return (
            <nav className={styles.navbar}>
                <Link to="/" className={styles.logo}>RoadReport</Link>
            </nav>
        );
    }

    const isAdmin = user && (user.role === 'ADMIN' || user.role === 'ROLE_ADMIN');

    return (
        <nav className={styles.navbar}>
            <Link to="/" className={styles.logo}>
                RoadReport
            </Link>

            <div className={styles.navLinks}>
                <Link to="/map" className={styles.link}>Map View</Link>

                {user ? (
                    <>
                        <button
                            onClick={() => {
                                if (isViewingMine) {
                                    navigate('/map');
                                } else {
                                    navigate('/map?view=mine');
                                }
                            }}
                            className={styles.link}
                            style={{
                                background: 'none',
                                border: 'none',
                                font: 'inherit',
                                cursor: 'pointer'
                            }}
                        >
                            {isViewingMine ? 'All Reports' : 'My Reports'}
                        </button>

                        {isAdmin && (
                            <Link to="/admin" className={`${styles.link} ${styles.adminLink}`}>
                                🛠️ Admin Panel
                            </Link>
                        )}

                        <Link to="/profile" className={styles.link}>
                            Profile ({user.username || 'User'})
                        </Link>
                        <button onClick={logout} className={styles.logoutBtn}>
                            Log Out
                        </button>
                    </>
                ) : (
                    <>
                        <Link to="/login" className={styles.link}>Log In</Link>
                        <Link to="/register" className={styles.link}>Register</Link>
                    </>
                )}
            </div>
        </nav>
    );
};

export default Navbar;