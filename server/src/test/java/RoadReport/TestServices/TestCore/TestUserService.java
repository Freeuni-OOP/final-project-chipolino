package RoadReport.TestServices.TestCore;

import RoadReport.entities.Comment;
import RoadReport.entities.Report;
import RoadReport.entities.User;
import RoadReport.entities.Vote;
import RoadReport.enums.Role;
import RoadReport.repositories.CommentRepository;
import RoadReport.repositories.ReportRepository;
import RoadReport.repositories.UserRepository;
import RoadReport.repositories.VoteRepository;
import RoadReport.services.core.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestUserService {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private UserService userService;

    private User firstUser;
    private User ghostUser;
    private User admin;

    @BeforeEach
    public void init() {
        firstUser = User.builder()
                .id(1L)
                .username("first_user")
                .roles(Role.USER)
                .email("ltaso@freeuni")
                .password("password")
                .build();
        admin = User.builder()
                .id(205L)
                .username("admin")
                .roles(Role.ADMIN)
                .build();
        ghostUser = User.builder().id(999L).username("ghostUser").build();
    }

    @Test
    public void testGetUserById() {
        when(userRepository.findById(firstUser.getId())).thenReturn(Optional.ofNullable(firstUser));
        User user = userService.getUserById(firstUser.getId());

        assertAll (
                () -> assertNotNull(user),
                () -> assertEquals(1L, user.getId()),
                () -> assertEquals("first_user", user.getUsername())
        );
        verify(userRepository, times(1)).findById(firstUser.getId());
        verifyNoMoreInteractions(userRepository);

        // Handle exception
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserById(999L);
        });

        assertEquals("User not found with ID: 999", exception.getMessage());

        verify(userRepository, times(1)).findById(999L);
        verifyNoMoreInteractions(userRepository);
    }


    @Test
    public void testGetUserByUsername() {
        when(userRepository.findUserByUsername(firstUser.getUsername())).thenReturn(Optional.ofNullable(firstUser));
        User user = userService.getUserByUsername(firstUser.getUsername());

        assertAll (
                () -> assertNotNull(user),
                () -> assertEquals(1L, user.getId()),
                () -> assertEquals("first_user", user.getUsername())
        );
        verify(userRepository, times(1)).findUserByUsername(firstUser.getUsername());
        verifyNoMoreInteractions(userRepository);

        // Handle exception
        when(userRepository.findUserByUsername("sansa")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserByUsername("sansa");
        });

        assertEquals("User not found with name: sansa", exception.getMessage());
        verify(userRepository, times(1)).findUserByUsername("sansa");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void testGetUserByEmail() {
        when(userRepository.findUserByEmail(firstUser.getEmail())).thenReturn(Optional.ofNullable(firstUser));
        User user = userService.getUserByEmail(firstUser.getEmail());

        assertAll (
                () -> assertNotNull(user),
                () -> assertEquals("ltaso@freeuni", user.getEmail()),
                () -> assertEquals("first_user", user.getUsername())
        );
        verify(userRepository, times(1)).findUserByEmail(firstUser.getEmail());
        verifyNoMoreInteractions(userRepository);

        // Handle exception
        when(userRepository.findUserByEmail("bufallo@email.com")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserByEmail("bufallo@email.com");
        });

        assertEquals("User not found with name: bufallo@email.com", exception.getMessage());
        verify(userRepository, times(1)).findUserByEmail("bufallo@email.com");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void testRegisterUser() {
        when(userRepository.findUserByEmail(firstUser.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findUserByUsername(firstUser.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("hashed");

        userService.registerUser(firstUser);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertAll(
                () -> assertNotNull(savedUser),
                () -> assertEquals("hashed", savedUser.getPassword()),
                () -> assertEquals("first_user", savedUser.getUsername())
        );

        verify(userRepository, times(1)).findUserByEmail(firstUser.getEmail());
        verify(userRepository, times(1)).findUserByUsername(firstUser.getUsername());
        verify(passwordEncoder, times(1)).encode("password");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void testRegisterUserButEmailExists() {
        when(userRepository.findUserByEmail(firstUser.getEmail())).thenReturn(Optional.of(admin));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(firstUser);
        });

        assertEquals("This name or email is taken", exception.getMessage());

        verify(userRepository, times(1)).findUserByEmail(firstUser.getEmail());
        verify(userRepository, never()).findUserByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    public void testRegisterUserButUsernameExists() {
        when(userRepository.findUserByEmail(firstUser.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findUserByUsername(firstUser.getUsername())).thenReturn(Optional.of(admin));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(firstUser);
        });

        assertEquals("This name or email is taken", exception.getMessage());

        verify(userRepository, times(1)).findUserByEmail(firstUser.getEmail());
        verify(userRepository, times(1)).findUserByUsername(firstUser.getUsername());
        verify(userRepository, never()).save(any(User.class));

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    public void testHandleAcceptedVote() {
        firstUser.setReputationScore(5);
        firstUser.setNonReliable(true);
        when(userRepository.findById(firstUser.getId())).thenReturn(Optional.of(firstUser));

        userService.handleAcceptedVote(firstUser.getId());
        assertAll(
                () -> assertEquals(6, firstUser.getReputationScore()),
                () -> assertTrue(firstUser.getNonReliable())
        );

        verify(userRepository, times(1)).findById(firstUser.getId());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void testHandleAcceptedVoteAndRestoreReliability() {
        firstUser.setReputationScore(19);
        firstUser.setNonReliable(true);
        when(userRepository.findById(firstUser.getId())).thenReturn(Optional.of(firstUser));

        userService.handleAcceptedVote(firstUser.getId());
        assertAll(
                () -> assertEquals(20, firstUser.getReputationScore()),
                () -> assertFalse(firstUser.getNonReliable())
        );

        verify(userRepository, times(1)).findById(firstUser.getId());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void testHandleRejectedVote() {
        firstUser.setReputationScore(10);
        firstUser.setNonReliable(false);
        when(userRepository.findById(firstUser.getId())).thenReturn(Optional.of(firstUser));

        userService.handleRejectedVote(firstUser.getId());

        assertAll(
                () -> assertEquals(9, firstUser.getReputationScore()),
                () -> assertFalse(firstUser.getNonReliable())
        );

        verify(userRepository, times(1)).findById(firstUser.getId());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void testHandleRejectedVoteAndSetUnreliableStatus() {
        firstUser.setReputationScore(-14);
        firstUser.setNonReliable(false);
        when(userRepository.findById(firstUser.getId())).thenReturn(Optional.of(firstUser));

        userService.handleRejectedVote(firstUser.getId());
        assertAll(
                () -> assertEquals(0, firstUser.getReputationScore()),
                () -> assertTrue(firstUser.getNonReliable())
        );

        verify(userRepository, times(1)).findById(firstUser.getId());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void testHandleRejectedReport() {
        firstUser.setRejectedReportsCount(1);
        firstUser.setReputationScore(20);
        firstUser.setBanned(false);
        when(userRepository.findById(firstUser.getId())).thenReturn(Optional.of(firstUser));

        userService.handleRejectedReport(firstUser.getId());
        assertAll(
                () -> assertEquals(2, firstUser.getRejectedReportsCount()),
                () -> assertEquals(15, firstUser.getReputationScore()),
                () -> assertFalse(firstUser.getBanned()),
                () -> assertNull(firstUser.getBanExpiration())
        );

        verify(userRepository, times(1)).findById(firstUser.getId());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void testHandleRejectedReportBanUser() {
        firstUser.setRejectedReportsCount(2);
        firstUser.setReputationScore(10);
        firstUser.setBanned(false);
        when(userRepository.findById(firstUser.getId())).thenReturn(Optional.of(firstUser));
        userService.handleRejectedReport(firstUser.getId());

        assertAll(
                () -> assertEquals(0, firstUser.getRejectedReportsCount()),
                () -> assertEquals(5, firstUser.getReputationScore()),
                () -> assertTrue(firstUser.getBanned()),
                () -> assertNotNull(firstUser.getBanExpiration()),
                () -> assertTrue(firstUser.getBanExpiration().isAfter(LocalDateTime.now()))
        );

        verify(userRepository, times(1)).findById(firstUser.getId());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void testUserIsBanned() {
        firstUser.setBanned(false);
        when(userRepository.findById(firstUser.getId())).thenReturn(Optional.of(firstUser));
        boolean isBanned = userService.userIsBanned(firstUser.getId());

        assertFalse(isBanned);
        verify(userRepository, times(1)).findById(firstUser.getId());
        verify(userRepository, never()).save(any(User.class));
        verifyNoMoreInteractions(userRepository);

        firstUser.setBanned(true);
        firstUser.setBanExpiration(LocalDateTime.now().plusDays(1));
        isBanned = userService.userIsBanned(firstUser.getId());

        assertTrue(isBanned);
        verify(userRepository, times(2)).findById(firstUser.getId());
        verify(userRepository, never()).save(any(User.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void testUserIsBannedWhenExpirationDatePassed() {
        firstUser.setBanned(true);
        firstUser.setBanExpiration(LocalDateTime.now().minusDays(1));
        when(userRepository.findById(firstUser.getId())).thenReturn(Optional.of(firstUser));
        boolean isBanned = userService.userIsBanned(firstUser.getId());

        assertAll(
                () -> assertFalse(isBanned),
                () -> assertFalse(firstUser.getBanned()),
                () -> assertNull(firstUser.getBanExpiration())
        );

        verify(userRepository, times(1)).findById(firstUser.getId());
        verify(userRepository, times(1)).save(firstUser);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void testDeleteUser() {
        Report report1 = new Report(); report1.setId(10L); report1.setUser(firstUser);
        Report report2 = new Report(); report2.setId(11L); report2.setUser(firstUser);
        List<Report> userReports = List.of(report1, report2);
        List<Comment> userComments = new ArrayList<>();
        List<Vote> userVotes = new ArrayList<>();

        when(userRepository.findById(firstUser.getId())).thenReturn(Optional.of(firstUser));
        when(userRepository.findUserByUsername("ghostUser")).thenReturn(Optional.of(ghostUser));
        when(reportRepository.findByUserId(firstUser.getId())).thenReturn(userReports);
        when(commentRepository.findByUserId(firstUser.getId())).thenReturn(userComments);
        when(voteRepository.findByUserId(firstUser.getId())).thenReturn(userVotes);

        userService.deleteUser(firstUser.getId());
        assertAll(
                () -> assertEquals(ghostUser.getId(), report1.getUser().getId()),
                () -> assertEquals(ghostUser.getId(), report2.getUser().getId())
        );

        verify(commentRepository, times(1)).saveAll(userComments);
        verify(voteRepository, times(1)).deleteAll(userVotes);
        verify(reportRepository, times(1)).saveAll(userReports);
        verify(userRepository, times(1)).delete(firstUser);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testDeleteUserWhenTryingToRemoveGhostUser() {
        when(userRepository.findById(ghostUser.getId())).thenReturn(Optional.of(ghostUser));
        when(userRepository.findUserByUsername("ghostUser")).thenReturn(Optional.of(ghostUser));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.deleteUser(ghostUser.getId());
        });

        assertEquals("Cannot delete the system ghost user account.", exception.getMessage());
        verify(reportRepository, never()).findByUserId(anyLong());
        verify(reportRepository, never()).saveAll(anyList());
        verify(userRepository, never()).delete(any(User.class));
    }

}
