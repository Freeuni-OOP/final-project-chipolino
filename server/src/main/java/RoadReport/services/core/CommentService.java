package RoadReport.services.core;

import RoadReport.entities.Comment;
import RoadReport.entities.Report;
import RoadReport.entities.User;
import RoadReport.enums.Role;
import RoadReport.exceptions.special.ActionForbiddenException;
import RoadReport.exceptions.core.CommentNotFoundException;
import RoadReport.exceptions.core.ReportNotFoundException;
import RoadReport.exceptions.core.UserNotFoundException;
import RoadReport.repositories.CommentRepository;
import RoadReport.repositories.ReportRepository;
import RoadReport.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    /**
     * creates and store users comment,
     * ensures safety from XSS attacks
     *
     * @param userId   user ID, which writes comment
     * @param reportId report ID, which writes comment
     * @param text     text of comment
     * @throws ReportNotFoundException if report does not exist
     * @throws UserNotFoundException if user does not exist
     */
    @Transactional
    public void addComment(Long userId, Long reportId, String text) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("couldn't found: " + userId));
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException("couldn't found report: " + reportId));

        String safeText = Jsoup.clean(text, Safelist.none());

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setText(safeText);
        comment.setReport(report);
        commentRepository.save(comment);
    }

    /**
     * deletes comment, if user tries to delete other users comment if
     * inside prevents it
     *
     * @param commentId ID of deleted comment
     * @param userId    user id who tries to delete
     * @throws CommentNotFoundException if comment does not exist
     * @throws UserNotFoundException if user does not exist
     * @throws ActionForbiddenException if this user cannot delete this comment
     */
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("couldn't find comment id: " + commentId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("couldn't find user id: " + userId));

        if (!comment.getUser().getId().equals(userId) && user.getRoles() != Role.ADMIN) {
            throw new ActionForbiddenException("Wrong user id for this comment id: " + userId);
        }
        commentRepository.delete(comment);
    }

    /**
     * return every comment of this report
     *
     * @param reportId report ID
     * @return list of comments
     */
    public List<Comment> getCommentsByReport(Long reportId) {
        return commentRepository.findByReportId(reportId);
    }

    /**
     * returns every comment for specific user
     *
     * @param userId user ID
     * @return list of comments
     */
    public List<Comment> getCommentsByUser(Long userId) {
        return commentRepository.findByUserId(userId);
    }
}
