package com.webempresarial.store.knowledge.api.exception;

import com.webempresarial.store.knowledge.api.dto.KnowledgeApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice(
        basePackages = "com.webempresarial.store.knowledge.api"
)
public class KnowledgeApiExceptionHandler {
	
	@ExceptionHandler(
	        DuplicateKnowledgeVersionException.class
	)
	public ResponseEntity<KnowledgeApiErrorResponse>
	handleDuplicateKnowledgeVersion(
	        DuplicateKnowledgeVersionException exception,
	        HttpServletRequest request
	) {
	    return buildResponse(
	            HttpStatus.CONFLICT,
	            exception.getMessage(),
	            request
	    );
	}
	@ExceptionHandler(
	        KnowledgeVersionNotFoundException.class
	)
	public ResponseEntity<KnowledgeApiErrorResponse>
	handleKnowledgeVersionNotFound(
	        KnowledgeVersionNotFoundException exception,
	        HttpServletRequest request
	) {
	    return buildResponse(
	            HttpStatus.NOT_FOUND,
	            exception.getMessage(),
	            request
	    );
	}

	@ExceptionHandler(
	        DuplicateKnowledgeCodeException.class
	)
	public ResponseEntity<KnowledgeApiErrorResponse>
	handleDuplicateKnowledgeCode(
	        DuplicateKnowledgeCodeException exception,
	        HttpServletRequest request
	) {
	    return buildResponse(
	            HttpStatus.CONFLICT,
	            exception.getMessage(),
	            request
	    );
	}
	
    @ExceptionHandler(
            KnowledgeTenantNotResolvedException.class
    )
    public ResponseEntity<KnowledgeApiErrorResponse>
    handleTenantNotResolved(
            KnowledgeTenantNotResolvedException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                exception.getMessage(),
                request
        );
    }
    
    @ExceptionHandler(
            KnowledgeObjectNotFoundException.class
    )
    public ResponseEntity<KnowledgeApiErrorResponse>
    handleKnowledgeObjectNotFound(
            KnowledgeObjectNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(
            MethodArgumentNotValidException.class
    )
    public ResponseEntity<KnowledgeApiErrorResponse>
    handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<KnowledgeApiErrorResponse.FieldViolation> violations =
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(fieldError ->
                                new KnowledgeApiErrorResponse.FieldViolation(
                                        fieldError.getField(),
                                        fieldError.getDefaultMessage(),
                                        fieldError.getRejectedValue()
                                )
                        )
                        .toList();

        KnowledgeApiErrorResponse response =
                KnowledgeApiErrorResponse.validation(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "La solicitud contiene valores inválidos",
                        request.getRequestURI(),
                        violations
                );

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler(
            ConstraintViolationException.class
    )
    public ResponseEntity<KnowledgeApiErrorResponse>
    handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<KnowledgeApiErrorResponse.FieldViolation> violations =
                exception.getConstraintViolations()
                        .stream()
                        .map(violation ->
                                new KnowledgeApiErrorResponse.FieldViolation(
                                        violation
                                                .getPropertyPath()
                                                .toString(),
                                        violation.getMessage(),
                                        violation.getInvalidValue()
                                )
                        )
                        .toList();

        KnowledgeApiErrorResponse response =
                KnowledgeApiErrorResponse.validation(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "La solicitud contiene valores inválidos",
                        request.getRequestURI(),
                        violations
                );

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler(
            HttpMessageNotReadableException.class
    )
    public ResponseEntity<KnowledgeApiErrorResponse>
    handleMalformedBody(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "El cuerpo JSON es inválido o contiene valores no reconocidos",
                request
        );
    }

    @ExceptionHandler(
            IllegalArgumentException.class
    )
    public ResponseEntity<KnowledgeApiErrorResponse>
    handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(
            IllegalStateException.class
    )
    public ResponseEntity<KnowledgeApiErrorResponse>
    handleIllegalState(
            IllegalStateException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<KnowledgeApiErrorResponse>
    handleUnexpectedError(
            Exception exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno al procesar la solicitud",
                request
        );
    }

    private ResponseEntity<KnowledgeApiErrorResponse>
    buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        KnowledgeApiErrorResponse response =
                KnowledgeApiErrorResponse.of(
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}