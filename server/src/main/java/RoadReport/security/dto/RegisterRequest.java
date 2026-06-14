package RoadReport.security.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    String username;
    String email;
    String password;
}
