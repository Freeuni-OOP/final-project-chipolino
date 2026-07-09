package RoadReport.controllers.dto.user;

import RoadReport.enums.Role;

import java.time.LocalDateTime;

public record UserResponseDTO(
        Long id,
        String username,
        Integer reputationScore,
        LocalDateTime createDate,
        Role role
) {}