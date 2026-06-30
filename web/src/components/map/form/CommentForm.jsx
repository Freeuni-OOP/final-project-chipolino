import {useState, useRef, useEffect} from "react";
import {addComment} from "../../../api/commentApi.js"
import {Button} from "../../common/button/Button.jsx"
import styles from './CommentForm.module.css'

/**
 * A form component that enables users to compose and submit new comments under a specific road report.
 * <p>The component performs the following tasks:
 * <ul>
 * <li>Manages internal state for the comment text input, character counter, loading indicators, and error feedback.</li>
 * <li>Triggers validation to ensure submissions are non-empty and respect maximum character thresholds.</li>
 * <li>Dispatches network requests using {@link addComment} to append data safely on the server side.</li>
 * <li>Emits an {@code onCommentAdded} callback event upon successful responses to update parent collections instantly.</li>
 * <li>Resets all textual form fields to an initial blank state once operations resolve successfully.</li>
 * </ul>
 * </p>
 * @param reportId The unique identifier of the road report to which this comment belongs.
 * @param onCommentAdded A callback function invoked after a successful submission, passing the newly created comment data.
 * @returns A JSX element containing a structured text submission layout with integrated loading indicators and limits.
 */
export const CommentForm = ({reportId, onCommentAdded}) => {
    const [content, setContent] = useState('')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState(null)
    const textareaRef = useRef(null)

    useEffect(() => {
        if (textareaRef.current) {
            textareaRef.current.scrollTop = 0;
            adjustHeight(textareaRef.current)
        }
    }, [])

    const submit = async (e) => {
        e.preventDefault()
        const trim = content.trim()
        if(!trim){
            return
        }

        setLoading(true)
        setError(null)

        try {
            const comment = await addComment(reportId, trim)
            setContent('')
            if (textareaRef.current) textareaRef.current.scrollTop = 0;
            if (onCommentAdded) {
                onCommentAdded(comment)
            }
            setLoading(false)
        } catch (err) {
            console.error(`Failed to submit comment: ${err}`)
            setError('Could not post your comment. Please try again.');
            setLoading(false)
        }
    }

    const adjustHeight = (el) => {
        if (!el) return
        const MAX = 220 // px
        el.style.height = 'auto'
        const newH = Math.min(el.scrollHeight, MAX)
        el.style.height = newH + 'px'
        el.style.overflowY = el.scrollHeight > MAX ? 'auto' : 'hidden'
    }

    return (
        <form className={styles.formContainer} onSubmit={submit}>
            <div className={styles.inputWrapper}>
                <textarea
                    ref={textareaRef}
                    className={styles.textarea}
                    placeholder="Write a comment..."
                    value={content}
                    onChange={(e) => {
                        setContent(e.target.value)
                        adjustHeight(e.target)
                    }}
                    maxLength={500}
                    disabled={loading}
                    required
                />
                <span className={styles.charCounter}>
                    {content.length}/500
                </span>
            </div>

            {error ? <div className={styles.errorMessage}>
                        {error}
                    </div> : null
            }

            <div className={styles.actions}>
                <Button type="submit"
                        disabled={loading || !content.trim()}
                        className={styles.submitBtn}>
                    {loading ? 'Posting...' : 'Send 💬'}
                </Button>
            </div>
        </form>
    )
}