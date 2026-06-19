import {useEffect, useState} from 'react'
import {getComments, deleteComment} from '../../../api/commentApi.js'
import styles from './CommentLists.module.css'
import {Card} from "../../common/card/Card.jsx"
import {Button} from "../../common/button/Button.jsx"
import {Spinner} from "../../common/spinner/Spinner.jsx"
import { CommentForm } from '../forms/CommentForm.jsx'

/**
 * A component responsible for displaying, managing, and interacting with a list of comments
 * associated with a specific report.
 * <p>The component performs the following tasks:
 * <ul>
 * <li>Fetches comments from the API based on the provided reportId.</li>
 * <li>Displays a loading state while fetching and an error state if the request fails.</li>
 * <li>Renders a list of comments using individual {@link Card} components.</li>
 * <li>Provides deletion functionality for authorized users (Admins or the comment author).</li>
 * </ul>
 * </p>
 * @param reportId The unique identifier of the road report to fetch comments for.
 * @param currentUser The currently authenticated user object, used to determine deletion permissions.
 * @returns A JSX element containing the comments list or appropriate status/empty messages.
 */
export const CommentLists = ({reportId, currentUser}) => {
    const [comments, setComments] = useState([])
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState(null)

    useEffect(() => {
        if(!reportId){
            return
        }

        const fetchComments = async () => {
            setLoading(true)
            setError(null)
            try {
                const data = await getComments(reportId)
                setComments(data || [])
                setLoading(false)
            } catch (err) {
                console.error(`Failed to load comments:`, err)
                setError("Failed to load comments.")
                setLoading(false)
            }
        }

        void fetchComments()
    }, [reportId])

    const handleCommentAdded = (newComment) => {
        setComments((prevComments) => [...prevComments, newComment]);
    };

    const handleDelete = async (commentId) => {
        if (!window.confirm("Are you sure you want to delete this comment?")) {
            return
        }

        try {
            await deleteComment(commentId)
            setComments((prev) => prev.filter((cm) => cm.id !== commentId))
        } catch (err) {
            console.error(`Failed to delete comment: ${err}`)
            alert("Could not delete comment. Try again.")
        }
    }

    if (loading) {
        return (
            <div className={styles.loader}>
                <Spinner />
            </div>
        )
    }

    if (error) {
        return <div className={styles.error}>{error}</div>
    }

    return (
        <div className={styles.commentsWrapper}>
            <h2 className={styles.commentsTitle}>
                Comments ({comments.length})
            </h2>
            {comments.length !== 0 ?
                <div className={styles.commentsList}>
                    {comments.map((comment) => {
                        const isAdmin = currentUser?.role === 'ADMIN'
                        const isAuthor = currentUser?.username === comment.authorUsername
                        const canDelete = isAdmin || isAuthor

                        return (
                            <Card className={styles.commentCard}
                              key={comment.id}>
                                <div className={styles.commentHeader}>
                                    <span className={styles.commentAuthor}>
                                        {comment.authorUsername || 'Unknown'}
                                    </span>
                                    <span className={styles.commentDate}>
                                            {new Date(comment.createdAt).toLocaleDateString()}
                                    </span>
                                    {canDelete?
                                        <Button className={styles.deleteBtn}
                                                onClick={() => handleDelete(comment.id)}
                                                title="Delete comment">
                                            🗑️
                                        </Button> : null
                                    }
                                </div>
                                <p className={styles.commentContent}>
                                    {comment.content}
                                </p>
                            </Card>
                        )
                    })}
                </div> : <div className={styles.noComments}>
                    No comments yet. Be the first to say something!
                </div>
            }
            {currentUser ? (
                <CommentForm reportId={reportId} onCommentAdded={handleCommentAdded} />
            ) : (
                <p className={styles.loginHint}>Please log in to participate in the discussion.</p>
            )}
        </div>
    )
}