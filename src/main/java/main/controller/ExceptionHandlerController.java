package main.controller;

import lombok.RequiredArgsConstructor;
import main.api.response.CommonResponse;
import main.api.response.ComplexRs;
import main.api.response.PersonResponse;
import main.errors.*;
import main.model.entities.Post;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@RequiredArgsConstructor
public class ExceptionHandlerController {

    @ExceptionHandler(NoPostEntityException.class)
    public ResponseEntity<CommonResponse<Post>> handleNoPostEntityException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonResponse.<Post>builder()
                .error(NoPostEntityException.class.getName())
                .timestamp(System.currentTimeMillis())
                .errorDescription(e.getMessage())
                .build());
    }

    @ExceptionHandler(PersonNotFoundException.class)
    public ResponseEntity<CommonResponse<PersonResponse>> handlePersonNotFoundByEmailException(Exception e) {
        return ResponseEntity.status(401).body(CommonResponse.<PersonResponse>builder()
                .error(e.getLocalizedMessage())
                .timestamp(System.currentTimeMillis())
                .errorDescription(e.getLocalizedMessage())
                .build());
    }

    @ExceptionHandler(BadAuthorizationException.class)
    public ResponseEntity<CommonResponse<PersonResponse>> handleBadAuthorizationException(Exception e) {
        return ResponseEntity.status(401).body(CommonResponse.<PersonResponse>builder()
                .error(BadAuthorizationException.class.getSimpleName())
                .timestamp(System.currentTimeMillis())
                .errorDescription(e.getMessage())
                .build());
    }

    @ExceptionHandler(EmptyFieldException.class)
    public ResponseEntity<CommonResponse> handleEmptyFieldException(Exception e) {
        return ResponseEntity.status(400).body(CommonResponse.builder()
                .error(EmptyFieldException.class.getSimpleName())
                .timestamp(System.currentTimeMillis())
                .errorDescription(e.getLocalizedMessage())
                .build());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<CommonResponse> handleBadCredentialsException(Exception e) {
        return ResponseEntity.status(400).body(CommonResponse.builder()
                .error("error")
                .timestamp((System.currentTimeMillis()))
                .errorDescription(e.getMessage())
                .build());
    }

    @ExceptionHandler(IncorrectRequestTypeException.class)
    public ResponseEntity<CommonResponse<ComplexRs>> handleIncorrectRequestTypeException(Exception e) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.<ComplexRs>builder()
                .error(IncorrectRequestTypeException.class.getSimpleName())
                .timestamp(System.currentTimeMillis())
                .errorDescription(e.getMessage())
                .build());
    }
}
