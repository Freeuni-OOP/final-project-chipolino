import styles from './Spinner.module.css'

/**
 * Spinner component used to provide visual feedback during loading states.
 * @returns {React.JSX.Element} A container element with an animating spinner.
 */
export const Spinner = () => {
    return (
        <div className={styles.spinnerContainer}>
            <div className={styles.spinner}></div>
        </div>
    )
}