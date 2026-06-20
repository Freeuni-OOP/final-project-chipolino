import React from 'react';
import styles from './Navbar.module.css';
import { Link, useNavigate } from 'react-router-dom';
import {useAuth} from "../../hooks/useAuth.js";

const Navbar = () => {
    const navigate = useNavigate();
    const { user, loading, handleLogout } = useAuth();

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
                <Link to="/" className={styles.logo}>GeoTracker</Link>
            </nav>
        );
    }

    const isAdmin = user && (user.role === 'ADMIN' || user.role === 'ROLE_ADMIN');

    return (
        <nav className={styles.navbar}>
            <Link to="/" className={styles.logo}>
                GeoTracker
            </Link>

            <div className={styles.navLinks}>
                <Link to="/map" className={styles.link}>Map View</Link>

                {user ? (
                    <>
                        {isAdmin && (
                            <Link to="/admin" className={`${styles.link} ${styles.adminLink}`}>
                                🛠️ Admin Panel
                            </Link>
                        )}

                        <Link to="/settings" className={styles.link}>
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