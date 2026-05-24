package RoadReport.TestSecurity;

import RoadReport.entities.User;
import RoadReport.enums.Role;
import RoadReport.repositories.UserRepository;
import RoadReport.security.service.RoadUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestRoadUserDetailsService {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoadUserDetailsService userDetailsService;

    private User user;

    @BeforeEach
    public void setUp(){
        user = new User();
        user.setId(1L);
        user.setUsername("Giorgi");
        user.setPassword("1234");
        user.setRoles(Role.USER);
    }

    @Test
    public void testExistingUser(){
        when(userRepository.findUserByUsername("Giorgi")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("Giorgi");

        assertEquals("Giorgi", userDetails.getUsername());
        assertEquals("1234", userDetails.getPassword());

        verify(userRepository).findUserByUsername("Giorgi");
    }

    @Test
    public void testNonExistingUser(){
        when(userRepository.findUserByUsername("Giorgi")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("Giorgi"));

        verify(userRepository).findUserByUsername("Giorgi");
    }
}
