import styles from './Button.module.css';

/**
 * Generic button controller wrapper.
 * @param {Object} props
 * @param {React.ReactNode} props.children - Text or icon inserted inside the button.
 * @param {string} [props.className=''] - Additional styles used to change boundaries or colors.
 * @param {boolean} [props.disabled] - Flag pinpointing if the button is disabled.
 * @param {Function} [props.onClick] - Method triggered by clicking on the button.
 */
export const Button = ({children, className = '', disabled, onClick, ...rest}) => {
    return (
        <button
            className={`${styles.btn} ${className}`.trim()}
            disabled={disabled}
            onClick={onClick}
            {...rest}>{children}</button>
    )
}