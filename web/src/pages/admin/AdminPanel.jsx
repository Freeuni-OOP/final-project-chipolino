import {useState} from 'react'
import styles from './AdminPanel.module.css'

import { Card } from "../../components/common/card/Card.jsx"
import { Button } from "../../components/common/button/Button.jsx"
import { Modal } from "../../components/common/modal/Modal.jsx"
import { Spinner } from "../../components/common/spinner/Spinner.jsx"
import { Input } from "../../components/common/input/Input.jsx"

import {
    banUser, unbanUser, deleteUser, adjustReputation,
    overrideReportStatus, deleteReport, deleteComment, selectUser
} from '../../api/adminApi.js'

/**
 * Administrative control panel for system moderation.
 * <p>This component provides a centralized interface for administrators to manage
 * users, reports, and comments. It is divided into three primary functional tabs:
 * <ul>
 * <li><b>User Management:</b> Lookup users by ID to inspect profiles, manage bans,
 * adjust reputation scores, and remove users from the system.</li>
 * <li><b>Report Overrides:</b> Forcefully update the status of reports or delete reports entirely.</li>
 * <li><b>Comment Control:</b> Remove individual comments by ID to maintain platform standards.</li>
 * </ul>
 * </p>
 * * @returns A JSX element containing the administrative navigation, input forms for
 * entity lookup, and action triggers within cards and modals.
 */
export const AdminPanel = () => {
    const [tab, setTab] = useState('users')
    const [loading, setLoading] = useState(false)
    const [actionLoading, setActionLoading] = useState(false)
    const [statusMsg, setStatusMsg] = useState({ type: '', text: '' })

    const [searchUserId, setSearchUserId] = useState('')
    const [selectedUser, setSelectedUser] = useState(null)
    const [selectedReportId, setSelectedReportId] = useState('')
    const [selectedCommentId, setSelectedCommentId] = useState('')

    const [banDays, setBanDays] = useState('3')
    const [isBanModalOpen, setBanModalOpen] = useState(false)
    const [repScore, setRepScore] = useState(0)
    const [isRepModalOpen, setRepModalOpen] = useState(false)

    const triggerNotification = (type, msg) => {
        setStatusMsg({ type : type, text : msg })
        setTimeout(() =>
            setStatusMsg({ type: '', text: '' }),
            4000)
    }

    const fetchUser = async (e) => {
        e.preventDefault()
        setLoading(true)
        setSelectedUser(null)
        try{
            const user = await selectUser(searchUserId)
            setSelectedUser(user)
            setLoading(false)
            triggerNotification('success', 'User selected successfully.')
        } catch (err) {
            console.error(err)
            setLoading(false)
            triggerNotification('error', 'User selection failed.')
        }
    }

    const applyBan = async () => {
        setActionLoading(true)
        try{
            await banUser(selectedUser.id, Number(banDays))
            setSelectedUser((prev) => ({...prev, banned : true}))
            setActionLoading(false)
            setBanModalOpen(false)
            triggerNotification('success', `User successfully banned for ${banDays} days.`)
        } catch (err) {
            console.error(err)
            setActionLoading(false)
            triggerNotification('error', 'Failed to apply ban on user.')
        }
    }

    const liftBan = async () => {
        setActionLoading(true)
        try{
            await unbanUser(selectedUser.id)
            setSelectedUser((prev) => ({...prev, banned : false}))
            setActionLoading(false)
            triggerNotification('success', 'User ban lifted successfully.')
        } catch (err) {
            console.error(err)
            setActionLoading(false)
            triggerNotification('error', 'Failed to lift ban from user.')
        }
    }

    const applyReputation = async (isReset) => {
        setActionLoading(true)
        try {
            const finalScore = isReset ? 0 : Number(repScore)
            await adjustReputation(selectedUser.id, isReset, Number(finalScore))
            setSelectedUser((prev) => ({...prev, reputationScore: finalScore}))
            setActionLoading(false)
            setRepModalOpen(false)
            triggerNotification('success', `User reputation adjusted to ${finalScore} points.`)
        } catch (err) {
            console.error(err)
            setActionLoading(false)
            triggerNotification('error', 'Failed to adjust user reputation.')
        }
    }

    const deleteSelectedUser = async () => {
        if (!window.confirm('Delete this user? This action cannot be undone.')) {
            return
        }

        setActionLoading(true)
        try{
            await deleteUser(selectedUser.id)
            triggerNotification('success', `User #${selectedUser.id} was deleted.`)
            setSearchUserId('');
            setSelectedUser(null)
            setActionLoading(false)
        } catch (err) {
            console.error(err)
            setActionLoading(false)
            triggerNotification('error', 'Failed to delete user.')
        }
    }


    const changeReportStatus = async (status) => {
        if(!selectedReportId) {
            return
        }

        setActionLoading(true)
        try{
            await overrideReportStatus(selectedReportId, status)
            triggerNotification('success', `Report #${selectedReportId} status forced to ${status}.`)
            setSelectedReportId('')
            setActionLoading(false)
        } catch (err) {
            console.error(err)
            setActionLoading(false)
            triggerNotification('error', 'Failed to override report status.')
        }
    }

    const deleteSelectedReport = async () => {
        if (!selectedReportId.trim() ||
            !window.confirm('Delete this report? This action cannot be undone.')){
            return
        }

        setActionLoading(true)
        try{
            await deleteReport(selectedReportId)
            triggerNotification('success', `Report #${selectedReportId} was deleted.`)
            setSelectedReportId('')
            setActionLoading(false)
        } catch (err) {
            console.error(err)
            setActionLoading(false)
            triggerNotification('error', 'Failed to delete report.')
        }
    }

    const deleteSelectedComment = async (e) => {
        e.preventDefault();
        if (!selectedCommentId.trim() ||
            !window.confirm('Delete this comment? This action cannot be undone.')){
            return
        }

        setActionLoading(true)
        try{
            await deleteComment(selectedCommentId)
            triggerNotification('success', `Comment #${selectedCommentId} was deleted.`)
            setSelectedCommentId('')
            setActionLoading(false)
        } catch (err) {
            console.error(err)
            setActionLoading(false)
            triggerNotification('error', 'Failed to delete comment.')
        }
    }

    return (
        <div className={styles.adminContainer}>
            <header className={styles.adminHeader}>
                <h1 className={styles.title}>System Moderation Page</h1>
            </header>
            <div className={styles.tabsContainer}>
                <Button className={`${styles.tabButton}
                            ${tab === 'users' ? styles.activeTab : ''}`}
                        disabled={loading || actionLoading}
                        onClick={() => {
                            setTab('users')
                            setSelectedUser(null)
                        }}>
                    👤 User Management
                </Button>

                <Button className={`${styles.tabButton} 
                             ${tab === 'reports' ? styles.activeTab : ''}`}
                        disabled={loading || actionLoading}
                        onClick={() => {
                            setTab('reports')
                        }}>
                    ⚠️ Report Overrides
                </Button>

                <Button className={`${styles.tabButton} 
                              ${tab === 'comments' ? styles.activeTab : ''}`}
                        disabled={loading || actionLoading}
                        onClick={() => {
                            setTab('comments')
                        }}>
                    💬 Comment Control
                </Button>
            </div>


            {statusMsg.text ?
                <div className={`${styles.notification} 
                                    ${statusMsg.type === 'success' ? 
                                        styles.notificationSuccess 
                                        : styles.notificationError}`}>
                    {statusMsg.text}
                </div> : null}

            {loading && !selectedUser ?
                <div className={styles.spinnerWrapper}>
                    <Spinner/>
                </div> : null}


            {!loading && tab === 'users' ?
                <div className={styles.tabContent}>
                    <Card className={styles.actionCard}>
                        <h2 className={styles.cardTitle}>User Manager</h2>
                        <form className={styles.inlineForm}
                              onSubmit={fetchUser}>
                            <Input label='User ID'
                                   type='number'
                                   placeholder='Enter exact user ID...'
                                   value={searchUserId}
                                   onChange ={(e) =>
                                       setSearchUserId(e.target.value)}
                                   required>
                            </Input>
                            <Button className={styles.queryBtn}
                                    type="submit"
                                    disabled={loading}>
                                Get Account Info
                            </Button>
                        </form>
                    </Card>

                    {selectedUser ?
                        <Card className={styles.profileInspectionCard}>
                            <div className={styles.profileHeader}>
                                <div>
                                    <h3 className={styles.profileName}>
                                        {selectedUser.username}
                                    </h3>
                                    <p className={styles.profileEmail}>
                                        {selectedUser.email}
                                    </p>
                                </div>
                                <span className={`${styles.statusBadge} 
                                                  ${selectedUser.banned ? styles.badgeBanned : styles.badgeActive}`}>
                                    {selectedUser.banned ? 'SUSPENDED' : 'ACTIVE'}
                                </span>
                            </div>

                            <div className={styles.metricRow}>
                                <div className={styles.metricBox}>
                                    <span className={styles.metricLabel}>
                                        Reputation Score
                                    </span>
                                    <span className={styles.metricValue}>
                                        {selectedUser.reputationScore ?? 0}
                                    </span>
                                </div>
                                <div className={styles.metricBox}>
                                    <span className={styles.metricLabel}>
                                        Database ID
                                    </span>
                                    <span className={styles.metricValue}>
                                        #{selectedUser.id}
                                    </span>
                                </div>
                            </div>

                            <div className={styles.administrativeActions}>
                                <h4 className={styles.actionsHeading}>
                                    User administration
                                </h4>

                                <div className={styles.btnGrid}>
                                    {selectedUser.banned ? (
                                        <Button onClick={liftBan}
                                                className={styles.secondaryBtn}
                                                disabled={actionLoading}>
                                            Lift User Ban
                                        </Button>
                                    ) : (
                                        <Button onClick={() =>
                                                    setBanModalOpen(true)}
                                                className={styles.dangerBtn}
                                                disabled={actionLoading}>
                                            Apply User Ban
                                        </Button>
                                    )}
                                    <Button onClick={() => {
                                                        setRepScore(selectedUser.reputationScore || 0);
                                                        setRepModalOpen(true);
                                                    }}
                                            disabled={actionLoading}>
                                        Adjust Reputation
                                    </Button>
                                    <Button onClick={deleteSelectedUser}
                                            className={styles.criticalBtn}
                                            disabled={actionLoading}>
                                        Delete User From System
                                    </Button>
                                </div>
                            </div>
                        </Card> : null}
                </div> : null
            }

            {!loading && tab === 'reports' ?
                <div className={styles.tabContent}>
                    <Card className={styles.actionCard}>
                        <h2 className={styles.cardTitle}>Report Manager</h2>

                        <Input label='Report ID'
                               type='number'
                               placeholder='Enter exact report ID...'
                               value={selectedReportId}
                               onChange={(e) =>
                                   setSelectedReportId(e.target.value)}
                        />

                        <div className={styles.reportActionSection}>
                            <h4 className={styles.actionsHeading}>
                                Enforce Status On Report
                            </h4>

                            <div className={styles.btnGrid}>
                                <Button disabled={!selectedReportId.trim()
                                                    || actionLoading}
                                        onClick={() =>
                                            changeReportStatus('PERMANENT')}>
                                    Force Status: PERMANENT
                                </Button>
                                <Button disabled={!selectedReportId.trim()
                                                    || actionLoading}
                                        onClick={() =>
                                            changeReportStatus('TEMPORARY')}>
                                    Force Status: TEMPORARY
                                </Button>
                                <Button disabled={!selectedReportId.trim()
                                                    || actionLoading}
                                        onClick={() =>
                                            changeReportStatus('REMOVED')}>
                                    Force Status: REMOVED
                                </Button>
                            </div>

                            <hr className={styles.divider}/>

                            <h4 className={styles.criticalHeading}>Delete Action</h4>
                            <Button disabled={!selectedReportId.trim()
                                                || actionLoading}
                                    onClick={deleteSelectedReport}
                                    className={styles.criticalBtn}>
                                Delete Report Permanently
                            </Button>
                        </div>
                    </Card>
                </div> : null
            }

            {!loading && tab === 'comments' ?
                <div className={styles.tabContent}>
                    <Card className={styles.actionCard}>
                        <h2 className={styles.cardTitle}>Comment Manager</h2>

                        <form className={styles.inlineForm}
                            onSubmit={deleteSelectedComment}>
                            <Input
                                label="Comment ID"
                                type="number"
                                placeholder="Enter exact comment ID..."
                                value={selectedCommentId}
                                onChange={(e) =>
                                    setSelectedCommentId(e.target.value)}
                                required
                            />
                            <Button type="submit"
                                    disabled={!selectedCommentId.trim() || actionLoading}
                                    className={styles.criticalBtn}>
                                Delete Comment
                            </Button>
                        </form>
                    </Card>
                </div> : null
            }

            <Modal isOpen={isBanModalOpen}
                   onClose={() => !actionLoading && setBanModalOpen(false)}>
                <h3 className={styles.modalTitle}>Apply User Ban</h3>
                <Input
                    label="Ban Length (Days)"
                    type="number"
                    value={banDays}
                    onChange={(e) => setBanDays(e.target.value)}
                    min="1"
                    required
                />

                <div className={styles.modalActions}>
                    <Button onClick={applyBan}
                            className={styles.dangerBtn}
                            disabled={actionLoading}>
                        Confirm Ban
                    </Button>
                    <Button onClick={() => setBanModalOpen(false)}
                            className={styles.secondaryBtn}
                            disabled={actionLoading}>
                        Cancel
                    </Button>
                </div>
            </Modal>

            <Modal isOpen={isRepModalOpen}
                    onClose={() => !actionLoading && setRepModalOpen(false)}>
                <h3 className={styles.modalTitle}>Update Reputation</h3>
                <Input
                    label="New Reputation Score"
                    type="number"
                    value={repScore}
                    onChange={(e) => setRepScore(e.target.value)}
                    required
                />

                <div className={styles.modalActions}>
                    <Button onClick={() =>
                        applyReputation(false)}
                        disabled={actionLoading}>
                        Save New Score
                    </Button>
                    <Button onClick={() => applyReputation(true)}
                            className={styles.dangerBtn}
                            disabled={actionLoading}>
                        Reset Reputation to Zero
                    </Button>
                    <Button onClick={() => setRepModalOpen(false)}
                            className={styles.secondaryBtn}
                            disabled={actionLoading}>
                        Cancel
                    </Button>
                </div>
            </Modal>
        </div>
    )
}

export default AdminPanel;