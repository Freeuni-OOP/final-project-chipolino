package RoadReport.TestServices.TestEmail;

import RoadReport.services.email.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
public class TestEmailService {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        setField(emailService, "apiKey", "mockKey");
        setField(emailService, "frontendUrl", "https://final.app");
        setField(emailService, "senderMail", "mock@gmail.com");
        setField(emailService, "restClient", restClient);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);

        when(requestBodySpec.header(any(String.class), any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    public void testSendVerificationEmail_Success() {
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

        emailService.sendVerificationEmail("gio@gmail.com", "token");

        verify(responseSpec).toBodilessEntity();
    }

    @Test
    public void testSendVerificationEmail_Failure_LogsException() {
        when(responseSpec.toBodilessEntity()).thenThrow(new RuntimeException("API Gateway Error"));

        emailService.sendVerificationEmail("gio@gmail.com", "token");

        verify(responseSpec).toBodilessEntity();
    }
}