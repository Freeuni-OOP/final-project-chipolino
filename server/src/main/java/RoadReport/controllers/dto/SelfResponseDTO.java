package RoadReport.controllers.dto;

import java.time.LocalDateTime;

public record SelfResponseDTO(
        Long id,
        String username,
        String email,
        Integer reputationScore,
        Boolean banned,
        LocalDateTime banExpiration,
        LocalDateTime createDate
) {}
