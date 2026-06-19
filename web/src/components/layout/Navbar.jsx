import React from 'react';
import styles from './Navbar.module.css';

const Navbar = () => {
    return (
        <nav className={styles.navbar}>
            <a href="/" className={styles.logo}>
                GeoTracker
            </a>

            <div className={styles.navLinks}>
                <a href="/map" className={styles.link}>Map View</a>

                <a href="/login" className={styles.link}>Log In</a>
                <a href="/register" className={styles.link}>Register</a>
            </div>
        </nav>
    );
};

export default Navbar;