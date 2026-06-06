package RoadReport.services.core;

import RoadReport.entities.Comment;
import RoadReport.entities.Report;
import RoadReport.entities.User;
import RoadReport.enums.Role;
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
     * @throws IllegalArgumentException if comment or user does not exist
     */
    @Transactional
    public void addComment(Long userId, Long reportId, String text) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("couldn't found: " + userId));
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("couldn't found report: " + reportId));

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
     * @param commentId ID od delete comment
     * @param userId    user id who tries to delete
     * @throws IllegalArgumentException if comment or user does not exist
     * @throws IllegalStateException   if this user cant delete this comment
     */
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("couldn't find comment id: " + commentId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("couldn't find user id: " + userId));

        if (!comment.getUser().getId().equals(userId) && user.getRoles() != Role.ADMIN) {
            throw new IllegalStateException("Wrong user id for this comment id: " + userId);
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
        return commentRepository.findByReportIdOrderByCreateDateDesc(reportId);
    }

    /**
     * returns every comment for specific user
     *
     * @param userId user ID
     * @return list of comments
     */
    public List<Comment> getCommentsByUser(Long userId) {
        return commentRepository.findByUserIdOrderByCreateDateDesc(userId);
    }

    /**
     * Admin method to force delete any comment without ownership checks.
     *
     * @param commentId ID of the comment to delete
     */
    @Transactional
    public void adminDeleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new IllegalArgumentException("couldn't find comment id: " + commentId);
        }
        commentRepository.deleteById(commentId);
    }

    /**
     * Updates an existing comment.
     * Ensures safety from XSS attacks and verifies ownership.
     *
     * @param commentId ID of the comment to update
     * @param userId    ID of the user requesting the update
     * @param newText   The new text for the comment
     * @return The updated Comment entity
     * @throws IllegalArgumentException if comment does not exist
     * @throws IllegalStateException    if user is not the owner of the comment
     */
    @Transactional
    public Comment updateComment(Long commentId, Long userId, String newText) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("couldn't find comment id: " + commentId));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalStateException("You can only edit your own comments");
        }

        String text = Jsoup.clean(newText, Safelist.none());
        comment.setText(text);

        return commentRepository.save(comment);
    }
}
