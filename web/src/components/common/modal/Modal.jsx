import styles from './Modal.module.css'

/**
 * Controlled modal window overlay component.
 * Features background clicking auto-dismiss functionality via event propagation filtering.
 * @param {Object} props
 * @param {React.ReactNode} props.children - Components to insert inside the modal card.
 * @param {boolean} props.isOpen - Reactive flag defining visibility states.
 * @param {Function} props.onClose - Dismiss callback triggered by hitting the screen backdrop or exit button.
 */
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