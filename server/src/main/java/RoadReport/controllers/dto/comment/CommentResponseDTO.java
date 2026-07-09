package RoadReport.controllers.dto.comment;

import java.time.LocalDateTime;

public record CommentResponseDTO(Long id,
                              String content,
                              String authorUsername,
                              LocalDateTime createdAt) {
}
