package RoadReport.TestServices.TestEmail;

import RoadReport.entities.User;
import RoadReport.entities.VerificationToken;
import RoadReport.repositories.UserRepository;
import RoadReport.repositories.VerificationTokenRepository;
import RoadReport.services.email.VerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestVerificationService {
    @Mock
    private VerificationTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VerificationService verificationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("bendo")
                .email("nbend@gmail.com")
                .isEnabled(false)
                .build();
    }

    @Test
    public void testCreateVerificationToken_Success() {
        ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
        verificationService.createVerificationToken(user);

        verify(tokenRepository).save(tokenCaptor.capture());
        verify(tokenRepository).deleteByUser(user);
    }

    @Test
    public void testVerifyToken_Success() {
        VerificationToken validToken = new VerificationToken();
        validToken.setToken("token");
        validToken.setUser(user);
        validToken.setExpiryDate(now().plusHours(1));

        when(tokenRepository.findByToken("token")).thenReturn(Optional.of(validToken));
        assertTrue(verificationService.verifyToken("token"));

        verify(userRepository).save(user);
        verify(tokenRepository).delete(validToken);
    }

    @Test
    public void testVerifyToken_ExpiredToken_ReturnsFalse() {
        VerificationToken expiredToken = new VerificationToken();
        expiredToken.setToken("token");
        expiredToken.setUser(user);
        expiredToken.setExpiryDate(now().minusHours(1));

        when(tokenRepository.findByToken("token")).thenReturn(Optional.of(expiredToken));
        assertFalse(verificationService.verifyToken("token"));

        verify(tokenRepository).delete(expiredToken);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testVerifyToken_TokenNotFound_ReturnsFalse() {
        when(tokenRepository.findByToken("token")).thenReturn(Optional.empty());
        assertFalse(verificationService.verifyToken("token"));

        verify(tokenRepository, never()).delete(any());
        verify(userRepository, never()).save(any());
    }
}

