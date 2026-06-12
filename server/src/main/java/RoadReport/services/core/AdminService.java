package RoadReport.services.core;

import RoadReport.entities.Comment;
import RoadReport.entities.Report;
import RoadReport.entities.User;
import RoadReport.enums.ReportStatus;
import RoadReport.enums.Role;
import RoadReport.exceptions.core.AdminOperationException;
import RoadReport.exceptions.special.BadRequestException;
import RoadReport.security.service.RoadUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service class responsible for administrative operations.
 * Contains logic for user moderation, reputation management, and content control.
 * All operations enforce an "Admin Shield" to prevent actions against other administrators.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserService userService;
    private final ReportService reportService;
    private final CommentService commentService;

    /**
     * Bans a user for a specified number of days.
     * The user's status is set to banned, and an expiration date is calculated.
     *
     * @param targetUserId The ID of the user to be banned.
     * @param daysToBan    The duration of the ban in days. Must be greater than zero.
     * @throws AdminOperationException if the target user is an Administrator.
     * @throws BadRequestException     if the daysToBan is zero or negative.
     */
    @Transactional
    public void banUser(Long targetUserId, Integer daysToBan) {
        User user = userService.getUserById(targetUserId);

        if(user.getRoles() == Role.ADMIN) {
            throw new AdminOperationException("Security Violation: Administrative accounts are protected from this action.");
        }
        if (daysToBan <= 0) throw new BadRequestException("Days must be positive");

        user.setBanned(true);
        user.setBanExpiration(LocalDateTime.now().plusDays(daysToBan));
    }

    /**
     * Lifts a ban from a user immediately by resetting their ban status and expiration date.
     *
     * @param targetUserId The ID of the user to be unbanned.
     */
    @Transactional
    public void unbanUser(Long targetUserId) {
        User user = userService.getUserById(targetUserId);

        user.setBanned(false);
        user.setBanExpiration(null);
    }

    /**
     * Permanently deletes a user from the system.
     * Depending on UserService implementation, this may reassign their content to a ghost user.
     *
     * @param targetUserId The ID of the user to be deleted.
     * @throws AdminOperationException if the target user is an Administrator.
     */
    @Transactional
    public void deleteUser(Long targetUserId) {
        User user = userService.getUserById(targetUserId);

        if(user.getRoles() == Role.ADMIN) {
            throw new AdminOperationException("Security Violation: Administrative accounts are protected from this action.");
        }

        userService.deleteUser(targetUserId);
    }

    /**
     * Manually adjusts a user's reputation score.
     * Can either completely reset the score to 0 or modify the current score by a specific amount.
     *
     * @param targetUserId The ID of the user whose reputation is being adjusted.
     * @param isReset      If true, forces the user's reputation back to 0, ignoring the score parameter.
     * @param score        The amount of points to add or subtract from the current score (ignored if isReset is true).
     * @throws AdminOperationException  if the target user is an Administrator.
     * @throws BadRequestException if isReset is false but no score modifier is provided.
     */
    @Transactional
    public void adjustReputation(Long targetUserId, boolean isReset, Integer score) {
        User user = userService.getUserById(targetUserId);
        if(user.getRoles() == Role.ADMIN) {
            throw new AdminOperationException("Security Violation: Administrative accounts are protected from this action.");
        }

        if (isReset) {
            user.setReputationScore(0);
        } else {
            if (score == null) throw new BadRequestException("Score must be provided when not resetting");
            user.setReputationScore(user.getReputationScore() + score);
        }
    }

    /**
     * Forcibly overrides the status of a specific road report.
     *
     * @param reportId        The ID of the report to modify.
     * @param newReportStatus The new status to apply to the report (e.g., PERMANENT, REJECTED).
     * @throws AdminOperationException if the report was submitted by an Administrator.
     */
    @Transactional
    public void overrideReportStatus(Long reportId, ReportStatus newReportStatus) {
        Report report = reportService.getReportById(reportId);

        if (report.getUser().getRoles() == Role.ADMIN) {
            throw new AdminOperationException("Security Violation: Administrative accounts are protected from this action.");
        }

        report.setStatus(newReportStatus);
    }

    /**
     * Permanently deletes a specific road report from the database.
     *
     * @param reportId The ID of the report to be removed.
     * @throws AdminOperationException if the report was submitted by an Administrator.
     */
    @Transactional
    public void deleteReport(Long reportId) {
        Report report = reportService.getReportById(reportId);

        if (report.getUser().getRoles() == Role.ADMIN) {
            throw new AdminOperationException("Security Violation: Administrative accounts are protected from this action.");
        }

        reportService.deleteReport(report);
    }

    /**
     * Forcibly deletes a user comment from a report.
     *
     * @param commentId       The ID of the comment to be removed.
     * @param roadUserDetails The authenticated details of the admin performing the action,
     *                        used to bypass standard ownership checks in the CommentService.
     * @throws AdminOperationException if the comment was submitted by an Administrator.
     */
    @Transactional
    public void deleteComment(Long commentId, RoadUserDetails roadUserDetails) {
        Comment comment = commentService.getCommentById(commentId);

        if(comment.getUser().getRoles() == Role.ADMIN) {
            throw new AdminOperationException("Security Violation: Administrative accounts are protected from this action.");
        }

        commentService.deleteComment(commentId, roadUserDetails.getId());
    }
}