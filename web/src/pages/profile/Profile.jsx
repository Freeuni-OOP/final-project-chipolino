import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {getCurrentUser, getUser} from '../../api/userApi.js'
import styles from './Profile.module.css'
import { Card } from '../../components/common/card/Card.jsx'
import { Button } from '../../components/common/button/Button.jsx'
import { Spinner } from '../../components/common/spinner/Spinner.jsx'

const formatRole = (role) => {
    if (!role) return 'User'
    return role.charAt(0).toUpperCase() + role.slice(1).toLowerCase()
}

/**
 * A profile display component that renders user information for both the current user
 * and other participants of the RoadReport platform.
 * <p>The component performs the following tasks:
 * <ul>
 * <li>Fetches user data based on the route parameter (for public profiles) or current session context (for 'My Profile').</li>
 * <li>Displays an account status alert (ban banner) for the owner if the account is currently restricted.</li>
 * <li>Renders a comprehensive profile card including ID, reputation score, join date, and role.</li>
 * <li>Calculates and displays a dynamic "Driver Status" rank based on the user's reputation points.</li>
 * <li>Provides interactive navigation for administrative settings (accessible only to the profile owner).</li>
 * <li>Handles loading and error states to provide feedback when profile data is unavailable.</li>
 * </ul>
 * </p>
 * @param isMe A boolean flag indicating whether the profile being viewed belongs
 * to the authenticated user.
 * @returns A JSX element containing the full profile layout including headers, metrics, and rank information.
 */
export const Profile = ({isMe = false}) => {
    const {id} = useParams()
    const navigate = useNavigate()

    const [user, setUser] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(false)

    useEffect(() => {
        if (!isMe && !id) {
            return
        }

        const fetchPublicProfile = async () => {
            setLoading(true)
            try {
                setError(false)
                const res = isMe? await getCurrentUser() : await getUser(id)
                setUser(res)
                setLoading(false)
            } catch (err) {
                console.error(`Failed to fetch profile for user ID ${id}: ${err}`)
                setError(true)
                setLoading(false)
            }
        }

        void fetchPublicProfile()
    }, [isMe, id])

    if (loading) {
        return (
            <div className={styles.profileContainer}>
                <Spinner />
            </div>
        )
    }

    if (error || !user) {
        return (
            <div className={styles.profileContainer}>
                <Card className={styles.errorCard}>
                    <p className={styles.errorMsg}>
                        {isMe ? 'Failed to load your profile. Please log in again.'
                            : 'User not found or profile is unavailable.'}
                    </p>
                    <Button onClick={() => navigate('/')}>
                        Back to Map
                    </Button>
                </Card>
            </div>
        )
    }

    const {id : userId, username, reputationScore, createDate, role } = user;

    return (
        <div className={styles.profileContainer}>
            {isMe && user.banned ?
                <div className={styles.banBanner}>
                    🚨 <strong>Your account is locked!</strong>
                    {user.banExpiration ?
                        ` Restriction active until: ${new Date(user.banExpiration).toLocaleString()}`
                        : ' Permanent block.'}
                </div> : null
            }

            <div className={styles.profileHeader}>
                <h1 className={styles.title}>
                    {isMe ? 'My Profile' : 'User Profile'}
                </h1>
                <Button type="button"
                        className={styles.backBtn}
                        onClick={() => navigate(-1)}>
                    Back
                </Button>
            </div>

            <Card className={styles.profileCard}>
                <div className={styles.avatarSection}>
                    <div className={`${styles.avatarCircle} 
                                       ${role === 'ADMIN' ? styles.adminAvatar : ''}`}>
                        {username ? username.charAt(0).toUpperCase() : '?'}
                    </div>
                    <h2 className={styles.username}>
                        {username} {isMe && <span className={styles.myTarget}>(You)</span>}
                    </h2>
                    <span className={`${styles.roleBadge} 
                            ${styles[role?.toLowerCase()]}`}>
                        {formatRole(role)}
                    </span>
                </div>

                <div className={styles.infoGrid}>
                    <div className={styles.infoField}>
                        <span className={styles.label}>User ID</span>
                        <span className={styles.value}>#{userId}</span>
                    </div>

                    {isMe && user.email ?
                        <div className={styles.infoField}>
                            <span className={styles.label}>Email Address</span>
                            <span className={styles.value}>{user.email}</span>
                        </div> : null
                    }

                    <div className={styles.infoField}>
                        <span className={styles.label}>
                            User Reputation
                        </span>
                        <span className={`${styles.value} 
                                ${reputationScore >= 0 ? styles.positiveRep : styles.negativeRep}`}>
                                {reputationScore > 0 ? `+${reputationScore}` : reputationScore} REP
                        </span>
                    </div>

                    <div className={styles.infoField}>
                        <span className={styles.label}>
                            Joined RoadReport
                        </span>
                        <span className={styles.value}>
                            {new Date(createDate).toLocaleDateString()}
                        </span>
                    </div>
                </div>

                {isMe ?
                    <div className={styles.accountActions}>
                        <Button className={styles.editBtn} onClick={() => navigate('/settings')}>
                            ⚙️ Edit Settings
                        </Button>
                    </div> : null
                }
            </Card>

            <Card className={styles.rankCard}>
                <h3 className={styles.rankTitle}>Driver Status</h3>
                <p className={styles.rankText}>
                    {reputationScore >= 100 ? '👑 Elite Inspector' :
                        reputationScore >= 50 ? '🚗 Trusted Expert' :
                            reputationScore >= 10 ? '🔍 Active Patrol' :
                                reputationScore <= -20 ? '🚫 Road Outlaw' :
                                    reputationScore <= -10 ? '⛔ Rule Breaker' :
                                        reputationScore <= -5 ? '💥 Infamous User' :
                                            '🌱 Normal Driver'}
                </p>
            </Card>
        </div>
    )
}

export default Profile;