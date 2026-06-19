import styles from './VoteButtons.module.css'
import {useState} from "react"
import {upVote, downVote} from "../../../api/voteApi.js"
import {Button} from "../../common/button/Button.jsx";

/**
 * A voting component that allows users to cast an 'upvote' or 'downvote' on a specific report.
 * <p>The component performs the following tasks:
 * <ul>
 * <li>Maintains local state for upvote and downvote counts, as well as the current user's voting status.</li>
 * <li>Implements optimistic UI updates: increments/decrements counts immediately before
 * the API request is completed to ensure a responsive user experience.</li>
 * <li>Handles state rollbacks if the database request fails, ensuring data integrity.</li>
 * <li>Visually highlights the active vote button based on the user's current selection.</li>
 * </ul>
 * </p>
 * @param reportId The unique identifier of the report being voted on.
 * @param initUpvotes The initial count of upvotes provided by the API.
 * @param initDownvotes The initial count of downvotes provided by the API.
 * @returns A JSX element containing two interactive buttons with dynamic counts and icons.
 */
export const VoteButtons = ({reportId, initUpvotes = 0, initDownvotes = 0}) => {
    const [upvotes, setUpvotes] = useState(initUpvotes)
    const [downvotes, setDownvotes] = useState(initDownvotes)
    const [userVote, setUserVote] = useState(null)
    const [loading, setLoading] = useState(false)

    const handleVote = (type) => {
        if(type === 'UPVOTE') {
            if(userVote === 'UPVOTE') {
                setUpvotes(prev => prev - 1)
                setUserVote(null)
            }
            else {
                if (userVote === 'DOWNVOTE') {
                    setDownvotes(prev => prev - 1)
                }
                setUpvotes(prev => prev + 1)
                setUserVote('UPVOTE')
            }
        } else if (type === 'DOWNVOTE') {
            if (userVote === 'DOWNVOTE') {
                setDownvotes(prev => prev - 1)
                setUserVote(null)
            } else {
                if (userVote === 'UPVOTE') {
                    setUpvotes(prev => prev - 1)
                }
                setDownvotes(prev => prev + 1)
                setUserVote('DOWNVOTE')
            }
        }
    }

    const makeVote = async (type) => {
        if (loading) {
            return
        }
        setLoading(true)

        const backupUpvotes = upvotes
        const backupDownvotes = downvotes
        const backupUserVote = userVote
        handleVote(type);

        try {
            if (type === 'UPVOTE') {
                await upVote(reportId)
            } else if (type === 'DOWNVOTE') {
                await downVote(reportId)
            }
            setLoading(false);
        } catch (err) {
            console.error(`Failed to vote on this report: ${err}`)
            setUpvotes(backupUpvotes)
            setDownvotes(backupDownvotes)
            setUserVote(backupUserVote)
            alert(`Unable to vote on this report, try again`)
            setLoading(false)
        }
    }

    return (
        <div className={styles.voteContainer}>
            <Button className={`${styles.voteBtn} ${styles.upvote} 
                        ${userVote === 'UPVOTE' ? styles.activeUpvote : ''}`}
                    onClick={() => makeVote('UPVOTE')}
                    disabled={loading}
                    aria-label="Upvote">
                <svg className={styles.icon}
                     viewBox="0 0 24 24"
                     fill="none"
                     stroke="currentColor"
                     strokeWidth="2">
                    <path d="M12 19V5M5 12l7-7 7 7"/>
                </svg>
                <span className={styles.count}>
                    {upvotes}
                </span>
            </Button>

            <Button className={`${styles.voteBtn} ${styles.downvote} 
                        ${userVote === 'DOWNVOTE' ? styles.activeDownvote : ''}`}
                    onClick={() => makeVote('DOWNVOTE')}
                    disabled={loading}
                    aria-label="Downvote">
                <svg className={styles.icon}
                     viewBox="0 0 24 24"
                     fill="none"
                     stroke="currentColor"
                     strokeWidth="2">
                    <path d="M12 5v14M5 12l7 7 7-7"/>
                </svg>
                <span className={styles.count}>
                    {downvotes}
                </span>
            </Button>

        </div>
    )
}