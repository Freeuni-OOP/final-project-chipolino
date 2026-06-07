package RoadReport.controllers;

import RoadReport.controllers.dto.CommentRequest;
import RoadReport.controllers.dto.CommentResponse;
import RoadReport.entities.Comment;
import RoadReport.entities.User;
import RoadReport.services.core.CommentService;
import RoadReport.services.core.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class CommentController {
    private final CommentService commentService;
    private final UserService userService;


    /**
     * Adds a new comment to a specific report.
     *
     * @param reportId    The ID of the report where the comment will be added.
     * @param request     The data transfer object containing the comment's text.
     * @param userDetails The authenticated user's details.
     * @return A {@link ResponseEntity} with HTTP status 201 (Created).
     */
    @PostMapping("/reports/{reportId}/comments")
    public ResponseEntity<Void> addComment(
            @PathVariable Long reportId,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        commentService.addComment(user.getId(), reportId, request.content());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Retrieves all comments associated with a specific report.
     * The returned list is ordered chronologically, newest first.
     *
     * @param reportId The ID of the report whose comments are being fetched.
     * @return A {@link ResponseEntity} containing a list of {@link CommentResponse} objects.
     */
    @GetMapping("/reports/{reportId}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentsByReport(@PathVariable Long reportId) {
        List<Comment> comments = commentService.getCommentsByReport(reportId);

        List<CommentResponse> responseList = comments.stream()
                .map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getText(),
                        comment.getUser().getUsername(),
                        comment.getCreateDate()
                ))
                .toList();

        return ResponseEntity.ok(responseList);
    }

    /**
     * Updates the text of an existing comment.
     * Ensures that only the original author can modify their own comment.
     *
     * @param commentId   The ID of the comment to update.
     * @param request     The data transfer object containing the updated text.
     * @param userDetails The authenticated user's details.
     * @return A {@link ResponseEntity} containing the updated {@link CommentResponse}.
     */
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        Comment comment = commentService.updateComment(commentId, user.getId(), request.content());
        CommentResponse response = new CommentResponse(
                comment.getId(),
                comment.getText(),
                comment.getUser().getUsername(),
                comment.getCreateDate()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes an existing comment.
     * Ensures that a user can only delete their own comment.
     *
     * @param commentId   The ID of the comment to delete.
     * @param userDetails The authenticated user's details.
     * @return A {@link ResponseEntity} with HTTP status 204 (No Content).
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        commentService.deleteComment(commentId, user.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Admin operation to forcefully delete any comment.
     * Bypasses ownership checks.
     *
     * @param commentId The ID of the comment to delete.
     * @return A {@link ResponseEntity} with HTTP status 204 (No Content).
     */
    @DeleteMapping("/admin/comments/{commentId}")
    public ResponseEntity<Void> adminDeleteComment(@PathVariable Long commentId) {
        commentService.adminDeleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

}
