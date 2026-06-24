import React from 'react'
import { Link } from 'react-router-dom'
import styles from './NotFound.module.css'
import {Button} from "../../components/common/button/Button.jsx"

/**
 * A fallback component for handling non-existent routes (404 Not Found).
 * <p>The component performs the following tasks:
 * <ul>
 * <li>Displays a clear '404' error code and a thematic visual indicator (map/location emoji).</li>
 * <li>Includes an explicit navigation control ({@link Button}) wrapped in a {@link Link}
 * to redirect the user back to the primary application view (the map).</li>
 * </ul>
 * </p>
 * @returns A JSX element serving as a full-page layout for invalid URL routes.
 */
export const NotFound = () => {
    return (
        <div className={styles.container}>
            <div className={styles.content}>
                <h1 className={styles.errorCode}>
                    404
                </h1>
                <div className={styles.emoji}>
                    🗺️❌
                </div>
                <h2 className={styles.title}></h2>
                <p className={styles.message}>
                    Sorry, we could not find the page.
                </p>

                <Link to="/" className={styles.link}>
                    <Button className={styles.homeBtn}>
                        Return to Map
                    </Button>
                </Link>
            </div>
        </div>
    )
}

export default NotFound;