package RoadReport.controllers.dto;

public record UserUpdateDTO(
        String username,
        String email,
        String password
) {}