package RoadReport.controllers;

import RoadReport.controllers.dto.SelfResponseDTO;
import RoadReport.controllers.dto.UserResponseDTO;
import RoadReport.controllers.dto.UserUpdateDTO;
import RoadReport.entities.User;
import RoadReport.security.service.RoadUserDetails;
import RoadReport.services.core.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public SelfResponseDTO getCurrentUser
            (@AuthenticationPrincipal RoadUserDetails userDetails){
        User user = userService.getUserById(userDetails.getId());

        return new SelfResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getReputationScore(),
                user.getBanned(),
                user.getBanExpiration(),
                user.getCreateDate()
        );
    }

    @GetMapping("/{id}")
    public UserResponseDTO getUser(@PathVariable Long id){
        User user = userService.getUserById(id);

        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getReputationScore(),
                user.getCreateDate()
        );
    }

    @PutMapping("/me")
    public SelfResponseDTO updateUser
            (@AuthenticationPrincipal RoadUserDetails userDetails,
             @RequestBody UserUpdateDTO updateData){
        User updatedUser = userService.updateUser(userDetails.getId(), updateData);

        return new SelfResponseDTO(
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getEmail(),
                updatedUser.getReputationScore(),
                updatedUser.getBanned(),
                updatedUser.getBanExpiration(),
                updatedUser.getCreateDate()
        );
    }

    @DeleteMapping("/me")
    public void deleteCurrentUser
            (@AuthenticationPrincipal RoadUserDetails userDetails){
        userService.deleteUser(userDetails.getId());
    }
}