import styles from './Input.module.css'

/**
 * Standard text form input controller.
 * @param {Object} props
 * @param {string} [props.label] - Optional text string initializing label.
 * @param {string} [props.error] - Dynamic validation alert string changing visual borders for errors.
 * @param {string} [props.className=''] - Additional styles used to change boundaries or colors.
 */
export const Input = ({label, error, className = '', ...rest}) => {
    return (
        <div className={styles.inputContainer}>
            {label ? <label className={styles.label}>{label}</label> : null}
            <input
                className={`${styles.inputField} ${error ? styles.error : ''} ${className}`.trim()}
                {...rest}/>
            {error ? <span className={styles.errorMessage}>{error}</span> : null}
        </div>
    )
}