import styles from './Modal.module.css'

export const Modal = ({children, isOpen, onClose}) => {
    if(!isOpen) {
        return null;
    }

    return (
        <div className={styles.overlay} onClick={onClose}>
            <div className={styles.modal}
                 onClick={(e)=>{e.stopPropagation()}}>
                {children}
            </div>
        </div>
    )
}