package RoadReport.config;

import RoadReport.entities.User;
import RoadReport.enums.Role;
import RoadReport.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SystemDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String @NonNull ... args) throws Exception {
        initGhostUser();
    }

    private void initGhostUser() {
        if (userRepository.findUserByUsername("ghostUser").isEmpty()) {

            User ghostUser = new User();
            ghostUser.setUsername("ghostUser");
            ghostUser.setEmail("ghost@roadreport.ge");

            ghostUser.setPassword("PROTECTED_SYSTEM_ACCOUNT_" + UUID.randomUUID());
            ghostUser.setRoles(Role.USER);

            ghostUser.setBanned(true);

            userRepository.save(ghostUser);
        }
    }
}
