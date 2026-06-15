import styles from './Select.module.css';

export const Select = ({ label, options = [], ...rest }) => {
    return (
        <div className={styles.selectContainer}>
            {label && <label className={styles.label}>{label}</label>}
            <div className={styles.selectWrapper}>
                <select className={styles.selectField} {...rest}>
                    {options.map((elem) => (
                        <option key={elem.value} value={elem.value}>
                            {elem.value}
                        </option>
                    ))}
                </select>
            </div>
        </div>
    );
};