package RoadReport.exceptions;

import RoadReport.exceptions.core.*;
import RoadReport.exceptions.special.ActionForbiddenException;
import RoadReport.exceptions.special.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles database concurrency conflicts caused by optimistic locking failures.
     * @param ex the caught {@link ObjectOptimisticLockingFailureException}
     * @return a {@code 409 Conflict} response containing the concurrency error message
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> handleOptimisticLock(Exception ex){
        Map<String, String> response = new HashMap<>();

        response.put("error", ex.getMessage());
        response.put("status", String.valueOf(HttpStatus.CONFLICT.value()));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handles situations where a requested user entity could not be found in the database.
     * @param ex the caught {@link UserNotFoundException}
     * @return a {@code 404 Not Found} response with the specific error message
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleUserNotFound(UserNotFoundException ex){
        Map<String, String> response = new HashMap<>();

        response.put("error", ex.getMessage());
        response.put("status", String.valueOf((HttpStatus.NOT_FOUND).value()));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handles registration or update conflicts where the identity attributes
     * are already taken.
     * @param ex the caught {@link UserAlreadyExistsException}
     * @return a {@code 409 Conflict} response with the specific error message
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String,String>> handleUserAlreadyExists
            (UserAlreadyExistsException ex){
        Map<String, String> response = new HashMap<>();

        response.put("error", ex.getMessage());
        response.put("status", String.valueOf((HttpStatus.CONFLICT).value()));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handles incorrect requests.
     * @param ex the caught {@link BadRequestException}
     * @return a {@code 400 Bad Request} response specifying the issue
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String,String>> handleBadRequest
            (BadRequestException ex){
        Map<String, String> response = new HashMap<>();

        response.put("error", ex.getMessage());
        response.put("status", String.valueOf((HttpStatus.BAD_REQUEST).value()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles situations where a specific road report item cannot be located.
     * @param ex the caught {@link ReportNotFoundException}
     * @return a {@code 404 Not Found} response with specific message
     */
    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleReportNotFound
            (ReportNotFoundException ex){
        Map<String, String> response = new HashMap<>();

        response.put("error", ex.getMessage());
        response.put("status", String.valueOf((HttpStatus.NOT_FOUND).value()));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handles operations attempted by a user account which has been banned.
     * @param ex the caught {@link UserBannedException}
     * @return a {@code 403 Forbidden} response with information that user is banned
     */
    @ExceptionHandler(UserBannedException.class)
    public ResponseEntity<Map<String,String>> handleUserBanned
            (UserBannedException ex){
        Map<String, String> response = new HashMap<>();

        response.put("error", ex.getMessage());
        response.put("status", String.valueOf((HttpStatus.FORBIDDEN).value()));

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handles situations where a specific comment item cannot be located.
     * @param ex the caught {@link CommentNotFoundException}
     * @return a {@code 404 Not Found} response with specific message
     */
    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCommentNotFound
            (CommentNotFoundException ex) {
        Map<String, String> response = new HashMap<>();

        response.put("error", ex.getMessage());
        response.put("status", String.valueOf(HttpStatus.NOT_FOUND.value()));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handles problems where an action violates permission rules
     * @param ex the caught {@link ActionForbiddenException}
     * @return a {@code 403 Forbidden} response indicating rights mismatch
     */
    @ExceptionHandler(ActionForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleActionForbidden
            (ActionForbiddenException ex) {
        Map<String, String> response = new HashMap<>();

        response.put("error", ex.getMessage());
        response.put("status", String.valueOf(HttpStatus.FORBIDDEN.value()));

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handles failed authentication attempts due to wrong criteria.
     * @param ex the caught {@link BadCredentialsException}
     * @return a {@code 401 Unauthorized} response with failure details
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        Map<String, String> response = new HashMap<>();

        response.put("error", ex.getMessage());
        response.put("status", String.valueOf(HttpStatus.UNAUTHORIZED.value()));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handles situations where a specific user with such username cannot be located.
     * @param ex the caught {@link UsernameNotFoundException}
     * @return a {@code 404 Not Found} response with specific message
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUsernameNotFound(UsernameNotFoundException ex) {
        Map<String, String> response = new HashMap<>();

        response.put("error", ex.getMessage());
        response.put("status", String.valueOf(HttpStatus.NOT_FOUND.value()));
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

}