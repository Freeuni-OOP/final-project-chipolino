import styles from './Card.module.css';

export const Card = ({children, className = ''}) => {
    return (
        <section className={`${styles.card} ${className}`.trim()}>
            {children}
        </section>
    )
}