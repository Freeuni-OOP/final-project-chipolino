package RoadReport.services.email;


import RoadReport.entities.User;
import RoadReport.entities.VerificationToken;
import RoadReport.repositories.UserRepository;
import RoadReport.repositories.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.UUID.randomUUID;

@Service
@RequiredArgsConstructor
public class VerificationService {
    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    /**
     * Generates registration verification token for user..
     *
     * @param user  User entity requiring email verification
     * @return the newly generated unique UUID token string
     */
    @Transactional
    public String createVerificationToken(User user) {
        tokenRepository.deleteByUser(user);

        String token = randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));

        tokenRepository.save(verificationToken);
        return token;
    }

    /**
     * Validates verification token and activates user account.
     *
     * @param token string submitted for validation
     * @return true if token is valid and account was successfully enabled;
     *         false if token does not exist or has expired
     */
    @Transactional
    public boolean verifyToken(String token) {
        Optional<VerificationToken> optionalToken = tokenRepository.findByToken(token);

        if(optionalToken.isEmpty()) {
            return false;
        }
        VerificationToken verificationToken = optionalToken.get();
        if(verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(verificationToken);
            return false;
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        tokenRepository.delete(verificationToken);
        return true;
    }
}
