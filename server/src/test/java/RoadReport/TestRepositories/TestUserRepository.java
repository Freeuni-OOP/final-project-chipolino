package RoadReport.TestRepositories;

import RoadReport.entities.User;
import RoadReport.enums.Role;
import RoadReport.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class TestUserRepository {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user;

    @BeforeEach()
    public void setUp() {
        user = new User();
        user.setUsername("giorgi");
        user.setEmail("giorgi29@free.ge");
        user.setPassword("foo");
        user.setRoles(Role.ADMIN);
    }

    @Test
    public void testFindByName(){
        entityManager.persist(user);
        entityManager.flush();

        Optional<User> foundUser = userRepository.findUserByUsername("giorgi");

        assertTrue(foundUser.isPresent());
        assertEquals("giorgi", foundUser.get().getUsername());
        assertEquals(Role.ADMIN, foundUser.get().getRoles());
    }

    @Test
    public void testFindByEmail() {
        entityManager.persist(user);
        entityManager.flush();

        Optional<User> foundUser = userRepository.findUserByEmail("giorgi29@free.ge");

        assertTrue(foundUser.isPresent());
        assertEquals("giorgi29@free.ge", foundUser.get().getEmail());
        assertEquals("giorgi", foundUser.get().getUsername());
    }

    @Test
    public void testUserNotFound() {
        Optional<User> foundUser = userRepository.findUserByUsername("ghost");

        assertTrue(foundUser.isEmpty());
    }
}
