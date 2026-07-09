package RoadReport.security.service;

import RoadReport.entities.User;
import RoadReport.repositories.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoadUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    /**
     * Locates a user based on the provided username.
     * Maps the database {@link User} entity to a Spring {@link UserDetails} object.
     *
     * @param username the username identifying the user whose data is required
     * @return a fully populated user record required by the authentication system
     * @throws UsernameNotFoundException if the user could not be found with the given username
     */
    @Override
    public @NonNull RoadUserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findUserByUsername(username);

        return user.map(RoadUserDetails::new).
                orElseThrow(() -> new UsernameNotFoundException
                        ("User not found with username: " + username));
    }
}
