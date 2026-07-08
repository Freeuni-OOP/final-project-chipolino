package RoadReport.TestExceptionHandler;

import RoadReport.exceptions.GlobalExceptionHandler;
import RoadReport.exceptions.core.*;
import RoadReport.exceptions.special.ActionForbiddenException;
import RoadReport.exceptions.special.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestGlobalExceptionHandler {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    public void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    public void testHandleOptimisticLock() {
        ObjectOptimisticLockingFailureException ex = mock(ObjectOptimisticLockingFailureException.class);
        when(ex.getMessage()).thenReturn("Optimistic lock failed");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleOptimisticLock(ex);
        assertErrorResponse(response, HttpStatus.CONFLICT, "Optimistic lock failed");
    }

    @Test
    public void testHandleUserNotFound() {
        UserNotFoundException ex = new UserNotFoundException("User not found");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleUserNotFound(ex);
        assertErrorResponse(response, HttpStatus.NOT_FOUND, "User not found");
    }

    @Test
    public void testHandleUserAlreadyExists() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("This email is taken");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleUserAlreadyExists(ex);
        assertErrorResponse(response, HttpStatus.CONFLICT, "This email is taken");
    }

    @Test
    public void testHandleBadRequest() {
        BadRequestException ex = new BadRequestException("Bad Request");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleBadRequest(ex);
        assertErrorResponse(response, HttpStatus.BAD_REQUEST, "Bad Request");
    }

    @Test
    public void testHandleReportNotFound() {
        ReportNotFoundException ex = new ReportNotFoundException("Report not found");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleReportNotFound(ex);
        assertErrorResponse(response, HttpStatus.NOT_FOUND, "Report not found");
    }

    @Test
    public void testHandleUserBanned() {
        UserBannedException ex = new UserBannedException("User is banned");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleUserBanned(ex);
        assertErrorResponse(response, HttpStatus.FORBIDDEN, "User is banned");
    }

    @Test
    public void testHandleCommentNotFound() {
        CommentNotFoundException ex = new CommentNotFoundException("Comment not found");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleCommentNotFound(ex);
        assertErrorResponse(response, HttpStatus.NOT_FOUND, "Comment not found");
    }

    @Test
    public void testHandleActionForbidden() {
        ActionForbiddenException ex = new ActionForbiddenException("Forbidden action");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleActionForbidden(ex);
        assertErrorResponse(response, HttpStatus.FORBIDDEN, "Forbidden action");
    }

    @Test
    public void testHandleBadCredentials() {
        BadCredentialsException ex = new BadCredentialsException("Wrong password");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleBadCredentials(ex);
        assertErrorResponse(response, HttpStatus.UNAUTHORIZED, "Wrong password");
    }

    @Test
    public void testHandleUsernameNotFound() {
        UsernameNotFoundException ex = new UsernameNotFoundException("Username not found");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleUsernameNotFound(ex);
        assertErrorResponse(response, HttpStatus.NOT_FOUND, "Username not found");
    }

    @Test
    public void testHandleAdminShieldViolation() {
        AdminOperationException ex = new AdminOperationException("Do not have such privileges");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleAdminShieldViolation(ex);
        assertErrorResponse(response, HttpStatus.FORBIDDEN, "Do not have such privileges");
    }

    @Test
    public void testHandleDisabledException() {
        DisabledException ex = new DisabledException("Account is disabled");
        ResponseEntity<String> response = exceptionHandler.handleDisabledException(ex);

        assertAll(
                () -> assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode()),
                () -> assertEquals("Please verify your email address before logging in.", response.getBody())
        );
    }
    private void assertErrorResponse(ResponseEntity<Map<String, String>> response,
                                     HttpStatus expectedStatus,
                                     String expectedMessage) {
        assertAll(
                () -> assertEquals(expectedStatus, response.getStatusCode()),
                () -> assertEquals(expectedMessage, response.getBody().get("message")),
                () -> assertEquals(String.valueOf(expectedStatus.value()),
                        response.getBody().get("status"))
        );
    }


}
