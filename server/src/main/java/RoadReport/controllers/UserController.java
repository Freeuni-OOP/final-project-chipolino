package RoadReport.controllers;

import RoadReport.controllers.dto.user.SelfResponseDTO;
import RoadReport.controllers.dto.user.UserResponseDTO;
import RoadReport.controllers.dto.user.UserUpdateDTO;
import RoadReport.entities.User;
import RoadReport.security.service.RoadUserDetails;
import RoadReport.services.core.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * Gets the private profile details of current user.
     * @param roadUserDetails the authenticated user principal
     * @return a {@link ResponseEntity} containing the full account details
     * with an HTTP 200 OK status
     */
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
                roadUserDetails.getCreateDate(),
                roadUserDetails.getRole()
        );
        return ResponseEntity.ok(selfResponse);
    }

    /**
     * Gets the public profile details of a specific user by their ID.
     * @param id the unique identifier of the target user
     * @return a {@link ResponseEntity} containing the public profile details
     * with an HTTP 200 OK status
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable Long id){
        User user = userService.getUserById(id);

        UserResponseDTO userResponse = new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getReputationScore(),
                user.getCreateDate(),
                user.getRoles()
        );
        return ResponseEntity.ok(userResponse);
    }


    /**
     * Updates profile details for current user.
     * Verifies identity using the active security context
     * @param userDetails the authenticated user principal
     * @param updateData DTO containing the requested profile changes
     * @return a {@link ResponseEntity} containing the newly updated full profile
     * with an HTTP 200 OK status
     */
    @PutMapping("/me")
    public ResponseEntity<SelfResponseDTO> updateUser
            (@AuthenticationPrincipal RoadUserDetails userDetails,
             @RequestBody UserUpdateDTO updateData){
        User user = userService.getUserById(userDetails.getId());
        user = userService.updateUser(user.getId(), updateData);

        SelfResponseDTO selfResponse = new SelfResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getReputationScore(),
                user.getBanned(),
                user.getBanExpiration(),
                user.getCreateDate(),
                user.getRoles()
        );
        return ResponseEntity.ok(selfResponse);
    }

    /**
     * Permanently deletes the account of the currently authenticated user.
     * Extracts the ID from the active security context
     * to ensure users can only delete their own accounts.
     * @param userDetails the authenticated user principal
     * @return a {@link ResponseEntity}
     * with an HTTP 204 No Content status indicating successful removal
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser
            (@AuthenticationPrincipal RoadUserDetails userDetails){
        User user = userService.getUserById(userDetails.getId());

        userService.deleteUser(user.getId());

        return ResponseEntity.noContent().build();
    }
}