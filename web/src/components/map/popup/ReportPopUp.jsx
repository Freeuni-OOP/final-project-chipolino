import React from 'react';
import { Popup } from 'react-leaflet';
import styles from './ReportPopUp.module.css';
import {VoteButtons} from "../vote/VoteButtons.jsx";
import {CommentLists} from "../comments/CommentLists.jsx";
import { Link } from 'react-router-dom'

const formatEnumText = (text) => {
    if (!text) {
        return 'Unknown';
    }
    return text.toLowerCase()
        .replace(/_/g, ' ')
        .replace(/\b\w/g, (char) => char.toUpperCase());
}

/**
 * A popup component displayed when a user clicks on a map marker.
 * <p>The component performs the following tasks:
 * <ul>
 * <li>Displays information about a report, including ID, type, and status.</li>
 * <li>Provides a link to the report author's profile, dynamically routing to '/profile'
 * if it is the current user's report, or '/users/:id' for others.</li>
 * <li>Integrates {@link VoteButtons} for interaction with report ratings.</li>
 * <li>Integrates {@link CommentLists} to show and manage discussions related to the report.</li>
 * <li>Displaying creation date.</li>
 * </ul>
 * </p>
 * @param report The report object containing data.
 * @param currentUser The currently authenticated user object, used to check ownership and set permissions.
 * @returns {@link Popup} component containing the structured report information.
 */
export const ReportPopup = ({report, currentUser}) => {
    const { id, authorUsername, authorId, type, description, status, upvotes, downvotes, createDate } = report;
    const typeStyle = styles[type?.toLowerCase()]
    const statusStyle = styles[status?.toLowerCase()]

    const isOwnReport = currentUser &&
        Number(authorId) === Number(currentUser.id);

    const profilePath = isOwnReport ? '/profile' : `/users/${authorId}`;

    return (
        <Popup minWidth={240}>
            <div className={styles.popupContainer}>
                <div className={styles.header}>
                    <span className={`${styles.badge} ${typeStyle}`}>
                        {formatEnumText(type)}
                    </span>
                    <span className={`${styles.badge} ${statusStyle}`}>
                        {formatEnumText(status)}
                    </span>
                    <span className={styles.reportId}>
                        #{id}
                    </span>
                </div>
                <div className={styles.reportAuthor}>
                    Reported by:{' '}
                    <Link to={profilePath} className={styles.authorLink}>
                        {authorUsername} {isOwnReport && <span className={styles.myTarget}>(You)</span>}
                    </Link>
                </div>

                <div className={styles.description}>
                    {description || 'No additional details provided.'}
                </div>

                <div className={styles.interactiveZone}>
                    <VoteButtons
                        reportId={id}
                        initUpvotes={upvotes}
                        initDownvotes={downvotes}
                    />
                </div>

                <div className={styles.interactiveZone}>
                    <CommentLists reportId={id} currentUser={currentUser}/>
                </div>

                {createDate ?
                    <div className={styles.date}>
                        Reported: {new Date(createDate).toLocaleString()}
                    </div> : null
                }
            </div>
        </Popup>
    )
}