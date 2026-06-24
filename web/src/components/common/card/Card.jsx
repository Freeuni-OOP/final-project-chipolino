import styles from './Card.module.css';

/**
 * Card wrapper using `<section>` block containers.
 * @param {Object} props
 * @param {React.ReactNode} props.children - View inserted inside the card layout.
 * @param {string} [props.className=''] - Additional styles used to change boundaries or colors.
 */
export const Card = ({children, className = ''}) => {
    return (
        <section className={`${styles.card} ${className}`.trim()}>
            {children}
        </section>
    )
}