package RoadReport.exceptions;

import jakarta.persistence.OptimisticLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
}