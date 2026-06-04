package RoadReport.controllers.dto;

import java.time.LocalDateTime;

public record UserResponseDTO(
        Long id,
        String username,
        Integer reputationScore,
        LocalDateTime createDate
) {}