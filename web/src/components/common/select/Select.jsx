import styles from './Select.module.css';

/**
 * Reusable HTML `<select>` UI wrapper with field label.
 * @param {Object} props
 * @param {String} [props.label] - Optional descriptive label text placed above the dropdown field.
 * @param {Array<Object>} [props.options=[]] - Array containing selectable option configurations.
 */
export const Select = ({ label, placeholder, options = [], ...rest }) => {
    return (
        <div className={styles.selectContainer}>
            {label && <label className={styles.label}>{label}</label>}
            <div className={styles.selectWrapper}>
                <select className={styles.selectField} {...rest}>
                    {placeholder ?
                        <option value="" disabled hidden>
                            {placeholder}
                        </option> : null
                    }

                    {options.map((elem) => (
                        <option key={elem.value} value={elem.value}>
                            {elem.label || elem.value}
                        </option>
                    ))}
                </select>
            </div>
        </div>
    );
};