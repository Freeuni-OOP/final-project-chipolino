import styles from './Input.module.css'

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