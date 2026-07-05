package RoadReport.services.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender emailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;
    /**
     * Sends a verification email to registered user containing activation url.
     *
     * @param userMail email address where the verification link will be sent
     * @param token string connected to user's verification record
     */
    public void sendVerificationEmail(String userMail, String token) {
        String url = frontendUrl + "/verify?token=" + token;
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(userMail);
        email.setSubject("Email Verification");
        email.setText("Please verify your email by clicking the following link: " + url);
        emailSender.send(email);
    }
}
