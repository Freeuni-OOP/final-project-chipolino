import styles from './Button.module.css';

export const Button = ({children, disabled, onClick, ...rest}) => {
    return (
        <button
            className={styles.btn}
            disabled={disabled}
            onClick={onClick}
            {...rest}>{children}</button>
    )
}