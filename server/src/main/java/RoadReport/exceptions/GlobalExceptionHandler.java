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
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> handleOptimisticLock(Exception ex){
        Map<String, String> response = new HashMap<>();

        response.put("error", ex.getMessage());
        response.put("status", String.valueOf(HttpStatus.CONFLICT.value()));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleUserNotFound(UserNotFoundException ex){
        Map<String, String> response = new HashMap<>();

        response.put("error", ex.getMessage());
        response.put("status", String.valueOf((HttpStatus.NOT_FOUND).value()));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String,String>> handleUserAlreadyExists
            (UserAlreadyExistsException ex){
        Map<String, String> response = new HashMap<>();

        response.put("error", ex.getMessage());
        response.put("status", String.valueOf((HttpStatus.CONFLICT).value()));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String,String>> handleBadRequest
            (BadRequestException ex){
        Map<String, String> response = new HashMap<>();

        response.put("error", ex.getMessage());
        response.put("status", String.valueOf((HttpStatus.BAD_REQUEST).value()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleReportNotFound
            (ReportNotFoundException ex){
        Map<String, String> response = new HashMap<>();

        response.put("error", ex.getMessage());
        response.put("status", String.valueOf((HttpStatus.NOT_FOUND).value()));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(UserBannedException.class)
    public ResponseEntity<Map<String,String>> handleUserBanned
            (UserBannedException ex){
        Map<String, String> response = new HashMap<>();

        response.put("error", ex.getMessage());
        response.put("status", String.valueOf((HttpStatus.FORBIDDEN).value()));

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCommentNotFound
            (CommentNotFoundException ex) {
        Map<String, String> response = new HashMap<>();

        response.put("error", ex.getMessage());
        response.put("status", String.valueOf(HttpStatus.NOT_FOUND.value()));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ActionForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleActionForbidden
            (ActionForbiddenException ex) {
        Map<String, String> response = new HashMap<>();

        response.put("error", ex.getMessage());
        response.put("status", String.valueOf(HttpStatus.FORBIDDEN.value()));

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        Map<String, String> response = new HashMap<>();

        response.put("error", ex.getMessage());
        response.put("status", String.valueOf(HttpStatus.UNAUTHORIZED.value()));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUsernameNotFound(UsernameNotFoundException ex) {
        Map<String, String> response = new HashMap<>();

        response.put("error", ex.getMessage());
        response.put("status", String.valueOf(HttpStatus.NOT_FOUND.value()));
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

}