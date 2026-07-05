package RoadReport.services.email;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Service
public class EmailService {
    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${sender}")
    private String senderMail;

    private final RestClient restClient = RestClient.create();
    /**
     * Sends a verification email to registered user containing activation url.
     *
     * @param userMail email address where the verification link will be sent
     * @param token string connected to user's verification record
     */
    @Async
    public void sendVerificationEmail(String userMail, String token) {
        Map<String, Object> requestBody = getEmail(userMail, token);

        try{
            restClient.post()
                    .uri("https://api.brevo.com/v3/smtp/email")
                    .header("api-key", apiKey)
                    .contentType(APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Verification email successfully sent to {}", userMail);
        } catch (Exception ex){
            log.info("Email is not sent: {}", ex.getMessage());
        }
    }

    private @NonNull Map<String, Object> getEmail(String userMail, String token) {
        String url = frontendUrl + "/verify?token=" + token;

        return Map.of(
                "sender", Map.of("name", "RoadReport", "email", senderMail),
                "to", List.of(Map.of("email", userMail)),
                "subject", "Email Verification - RoadReport",
                "htmlContent", "<h2>Verify your email</h2>" +
                        "<p>" +
                            "Please click the link below to verify your account:" +
                        "</p>" +
                        "<a href='" + url + "'>" +
                            "Click here to verify" +
                        "</a>"
        );
    }
}
