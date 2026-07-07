package RoadReport.TestSecurity;

import RoadReport.entities.User;
import RoadReport.enums.Role;
import RoadReport.repositories.UserRepository;
import RoadReport.security.service.RoadUserDetails;
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

    @Test
    public void testRoadUserDetailsGettersAndContracts() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        user.setEmail("giorgi@gmail.com");
        user.setReputationScore(100);
        user.setBanned(false);
        user.setBanExpiration(now);
        user.setCreateDate(now);
        user.setEnabled(true);

        when(userRepository.findUserByUsername("Giorgi")).thenReturn(Optional.of(user));

        RoadUserDetails userDetails = (RoadUserDetails) userDetailsService.loadUserByUsername("Giorgi");

        assertEquals(1L, userDetails.getId());
        assertEquals("giorgi@gmail.com", userDetails.getEmail());
        assertEquals(Role.USER, userDetails.getRole());
        assertEquals(100, userDetails.getReputationScore());
        assertFalse(userDetails.getBanned());
        assertEquals(now, userDetails.getBanExpiration());
        assertEquals(now, userDetails.getCreateDate());

        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());

        assertEquals("ROLE_USER", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    public void testRoadUserDetailsBuilder() {
        RoadUserDetails customDetails = RoadUserDetails.builder()
                .user(user)
                .build();

        assertNotNull(customDetails);
        assertEquals("Giorgi", customDetails.getUsername());
    }
}
