package RoadReport.controllers.dto.user;

public record UserUpdateDTO(
        String username,
        String email,
        String password
) {}