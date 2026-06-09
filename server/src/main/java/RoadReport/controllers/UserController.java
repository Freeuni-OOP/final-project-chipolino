package RoadReport.controllers;

import RoadReport.controllers.dto.SelfResponseDTO;
import RoadReport.controllers.dto.UserResponseDTO;
import RoadReport.controllers.dto.UserUpdateDTO;
import RoadReport.entities.User;
import RoadReport.security.service.RoadUserDetails;
import RoadReport.services.core.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<SelfResponseDTO> getCurrentUser
            (@AuthenticationPrincipal RoadUserDetails roadUserDetails){

        SelfResponseDTO selfResponse = new SelfResponseDTO(
                roadUserDetails.getId(),
                roadUserDetails.getUsername(),
                roadUserDetails.getEmail(),
                roadUserDetails.getReputationScore(),
                roadUserDetails.getBanned(),
                roadUserDetails.getBanExpiration(),
                roadUserDetails.getCreateDate()
        );
        return ResponseEntity.ok(selfResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable Long id){
        User user = userService.getUserById(id);

        UserResponseDTO userResponse = new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getReputationScore(),
                user.getCreateDate()
        );
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/me")
    public ResponseEntity<SelfResponseDTO> updateUser
            (@AuthenticationPrincipal UserDetails userDetails,
             @RequestBody UserUpdateDTO updateData){
        User user = userService.getUserByUsername(userDetails.getUsername());
        userService.updateUser(user.getId(), updateData);

        SelfResponseDTO selfResponse = new SelfResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getReputationScore(),
                user.getBanned(),
                user.getBanExpiration(),
                user.getCreateDate()
        );
        return ResponseEntity.ok(selfResponse);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser
            (@AuthenticationPrincipal UserDetails userDetails){
        User user = userService.getUserByUsername(userDetails.getUsername());

        userService.deleteUser(user.getId());

        return ResponseEntity.noContent().build();
    }
}