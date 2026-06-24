import {useState, useEffect, useCallback} from 'react'
import {updateUser, getCurrentUser, deleteCurrentUser} from "../../api/userApi.js";
import styles from './Settings.module.css'

import { Button } from "../../components/common/button/Button.jsx";
import { Card } from "../../components/common/card/Card.jsx";
import { Input } from "../../components/common/input/Input.jsx";
import { Select } from "../../components/common/select/Select.jsx";
import { Modal } from "../../components/common/modal/Modal.jsx";
import { Spinner } from "../../components/common/spinner/Spinner.jsx";

/**
 * Settings Page.
 * Manages user profile updates, local map proximity radius,
 * and account deletion with a verification modal.
 * @returns {JSX.Element} The rendered account settings page.
 */
export const Settings = () => {
    const [profile, setProfile] =
        useState({username : '', email : '', password: ''})

    const [status, setStatus] =
        useState({loading : false, error : false, success : false})

    const [proximityRadius, setProximityRadius] =
        useState(localStorage.getItem('proximityRadius') || '5')

    const [isDeleteModalOpen, setDeleteModalOpen] =
        useState(false);

    /**
     * Fetches authenticated user account details from the backend service.
     * Wrapped in useCallback to preserve reference identity across re-renders.
     */
    const fetchProfile = useCallback(async () => {
        setStatus({loading: true, error: false, success: false})
        try{
            const data = await getCurrentUser()
            setProfile({username : data.username, email : data.email, password: ''})
            setStatus({loading : false, error : false, success : false})
        } catch(err){
            console.log('Error when getting user:', err)
            setStatus({loading : false, error : true, success : false})
        }
    }, [])

    useEffect(() => {void fetchProfile()}, [fetchProfile])


    /**
     * Generic form for changing profile with new information.
     * @param {React.ChangeEvent<HTMLInputElement>} e - Input change event.
     */
    const handleChange = (e) => {
        const {name, value} = e.target
        setProfile((prev) =>
            ({...prev, [name]: value}))
    }

    /**
     * Updated preferred proximity radius.
     * @param {React.ChangeEvent<HTMLSelectElement>} e - Selection event.
     */
    const handleRadiusChange = (e) => {
        const value = e.target.value;
        setProximityRadius(value);
        localStorage.setItem('proximityRadius', value);
    };

    /**
     * Updates profile info
     * Automatically clears password fields after successful server update.
     * @param {React.FormEvent<HTMLFormElement>} e - Submission event.
     */
    const handleSave = async (e) => {
        e.preventDefault()
        setStatus({ loading: true, error: false, success: false });
        try{
            await updateUser(profile)
            setProfile((prev) =>
                ({...prev, password: ''}))
            setStatus({ loading: false, error: false, success: true });
        } catch(err){
            console.log('Error when updating user:', err)
            setStatus({loading : false, error : true, success : false})
        }
    }

    const handleDeleteClick = () => {
        setDeleteModalOpen(true);
    };

    /**
     * Confirms account deletion.
     * Automatically redirects unauthenticated guests back to the logging screens.
     * @param {React.MouseEvent<HTMLButtonElement>} e - Click event.
     */    const handleDelete = async (e) => {
        setStatus({loading : true, error : false, success : false})
        try{
            await deleteCurrentUser()
            setStatus({ loading: false, error: false, success: true });
            window.location.href='/login'
        } catch (err) {
            console.log('Error while deleting profile:', err)
            setStatus({loading : false, error : true, success : false})
            setDeleteModalOpen(false);
        }
    }

    /**
     * Options mapping list tracking proximity thresholds translated into kilometers.
     */
    const radiusOptions = [
        {value: '1', label: '1 km'},
        {value: '3', label: '3 km'},
        {value: '5', label: '5 km'},
        {value: '10', label: '10 km'},
        {value: '0', label: 'All Reports (Global)'}
    ];

    if(status.loading && !profile.username){
        return (
            <div className={styles.settingsContainer}>
                <Spinner/>
            </div>
        )
    }

    return (
        <div className={styles.settingsContainer}>
            <h1 className={styles.title}>Account Settings</h1>

            <Card className={styles.section}>
                <h2 className={styles.sectionTitle}>Profile Details</h2>

                <form className={styles.form} onSubmit={handleSave}>
                    <Input label="Username"
                           type="text"
                           name="username"
                           value={profile.username}
                           onChange={handleChange}
                           required
                           disabled={status.loading}
                    />

                    <Input
                        label="Email Address"
                        type="email"
                        name="email"
                        value={profile.email}
                        onChange={handleChange}
                        required
                        disabled={status.loading}
                    />

                    <Input
                        label="New Password"
                        type="password"
                        name="password"
                        value={profile.password}
                        onChange={handleChange}
                        placeholder="Leave blank to keep unchanged"
                        disabled={status.loading}
                    />

                    {status.error ?
                        <p className={styles.errorMsg}>
                            Error during updating profile
                        </p> : null}
                    {status.success ?
                        <p className={styles.successMsg}>
                            Successfully updated profile
                        </p> : null}

                    <Button type="submit" disabled={status.loading}>
                        {status.loading ? 'Saving...' : 'Save Changes'}
                    </Button>
                </form>
            </Card>

            <Card className={styles.section}>
                <h2 className={styles.sectionTitle}>Map Settings</h2>

                <Select label='Report radius'
                        options={radiusOptions}
                        value={proximityRadius}
                        onChange={handleRadiusChange}
                        disabled={status.loading}
                />
                <span className={styles.hintText}>
                    Sets the distance to show reports
                </span>
            </Card>

            <Card className={styles.section}>
                <h2 className={styles.dangerTitle}>Delete Account</h2>
                <p className={styles.dangerText}>
                    Deleting your account will permanently lose your reputation and settings.
                </p>

                <Button
                    className={styles.dangerBtn}
                    type="button"
                    onClick={handleDeleteClick}
                    disabled={status.loading}
                >Delete Account</Button>
            </Card>

            <Modal isOpen={isDeleteModalOpen}
                   onClose={() => {setDeleteModalOpen(false)}}>
                <h2>Are you sure?</h2>
                <div className={styles.modalActions}>
                    <Button className={styles.dangerBtn}
                            onClick={handleDelete}
                            disabled={status.loading}>
                        Delete
                    </Button>

                    <Button onClick={() => setDeleteModalOpen(false)}
                            disabled={status.loading}>
                        Cancel
                    </Button>
                </div>

            </Modal>
        </div>
    )
}
export default Settings;