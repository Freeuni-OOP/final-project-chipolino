package RoadReport.controllers.dto.user;

import RoadReport.enums.Role;

import java.time.LocalDateTime;

public record SelfResponseDTO(
        Long id,
        String username,
        String email,
        Integer reputationScore,
        Boolean banned,
        LocalDateTime banExpiration,
        LocalDateTime createDate,
        Role role
) {}
