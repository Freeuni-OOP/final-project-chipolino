package RoadReport.controllers;

import RoadReport.controllers.dto.comment.CommentRequestDTO;
import RoadReport.controllers.dto.comment.CommentResponseDTO;
import RoadReport.entities.Comment;
import RoadReport.security.service.RoadUserDetails;
import RoadReport.services.core.CommentService;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class CommentController {
    private final CommentService commentService;


    /**
     * Adds a new comment to a specific report.
     *
     * @param reportId    The ID of the report where the comment will be added.
     * @param request     The data transfer object containing the comment's text.
     * @param userDetails The authenticated user's details.
     * @return A {@link ResponseEntity} with HTTP status 201 (Created).
     */
    @SuppressWarnings({"JvmTaintAnalysis"})
    @PostMapping("/reports/{reportId}/comments")
    public ResponseEntity<CommentResponseDTO> addComment(
            @PathVariable Long reportId,
            @RequestBody CommentRequestDTO request,
            @AuthenticationPrincipal RoadUserDetails userDetails
    ) {
        Comment comment = commentService.addComment(userDetails.getId(), reportId, request.content());
        CommentResponseDTO response = new CommentResponseDTO(
                comment.getId(),
                comment.getText(),
                comment.getUser().getUsername(),
                comment.getCreateDate()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all comments associated with a specific report.
     * The returned list is ordered chronologically, newest first.
     *
     * @param reportId The ID of the report whose comments are being fetched.
     * @return A {@link ResponseEntity} containing a list of {@link CommentResponseDTO} objects.
     */
    @GetMapping("/reports/{reportId}/comments")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsByReport(@PathVariable Long reportId) {
        List<Comment> comments = commentService.getCommentsByReport(reportId);

        List<CommentResponseDTO> responseList = comments.stream()
                .map(comment -> new CommentResponseDTO(
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
     * @return A {@link ResponseEntity} containing the updated {@link CommentResponseDTO}.
     */
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponseDTO> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentRequestDTO request,
            @AuthenticationPrincipal RoadUserDetails userDetails
    ) {
        Comment comment = commentService.updateComment(commentId, userDetails.getId(), request.content());
        CommentResponseDTO response = new CommentResponseDTO(
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
            @AuthenticationPrincipal RoadUserDetails userDetails
    ) {
        commentService.deleteComment(commentId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

}
