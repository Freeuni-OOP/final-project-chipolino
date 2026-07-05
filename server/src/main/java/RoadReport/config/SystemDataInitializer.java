package RoadReport.config;

import RoadReport.entities.User;
import RoadReport.enums.Role;
import RoadReport.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SystemDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    @Transactional
    public void run(String @NonNull ... args) throws Exception {
        initGhostUser();
        initAdminUser();
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

    private void initAdminUser() {
        if (userRepository.findUserByUsername("admin").isEmpty() && userRepository.findUserByEmail("admin@roadreport.ge").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@roadreport.ge");

            admin.setPassword(passwordEncoder.encode("admin"));

            admin.setRoles(Role.ADMIN);

            admin.setBanned(false);

            userRepository.save(admin);
        }
    }
}
