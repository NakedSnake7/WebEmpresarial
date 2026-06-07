package com.webempresarial.store.controller.advice;

import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.webempresarial.store.exception.ImageUploadException;
import com.webempresarial.store.exceptions.FeatureNotAllowedException;
import com.webempresarial.store.service.ProductoService;

import java.io.IOException;
import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(RestExceptionHandler.class);

    // =====================================
    // PRODUCTO NO ENCONTRADO
    // =====================================
    @ExceptionHandler(ProductoService.ProductoNotFoundException.class)
    public ResponseEntity<?> handleProductoNotFound(
            ProductoService.ProductoNotFoundException ex) {

        log.warn("Producto no encontrado: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "success", false,
                        "error", ex.getMessage()
                ));
    }

    // =====================================
    // VALIDACIONES (@Valid)
    // =====================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(
            MethodArgumentNotValidException ex) {

        String mensaje = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(err -> err.getDefaultMessage())
                .orElse("Datos inválidos");

        log.warn("Error de validación: {}", mensaje);

        return ResponseEntity.badRequest()
                .body(Map.of(
                        "success", false,
                        "error", mensaje
                ));
    }

    // =====================================
    // ERRORES DE NEGOCIO
    // =====================================
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(
            IllegalArgumentException ex) {

        log.warn("Error de negocio: {}", ex.getMessage());

        return ResponseEntity.badRequest()
                .body(Map.of(
                        "success", false,
                        "error", ex.getMessage()
                ));
    }

    // =====================================
    // IO / CLOUDINARY (fallback técnico)
    // =====================================
    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> handleIO(IOException ex) {

        log.error("Error IO / Cloudinary", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "success", false,
                        "error", "Error procesando archivos"
                ));
    }

    // =====================================
    // IMAGE UPLOAD EXCEPTION (controlada)
    // =====================================
    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<?> handleImageUpload(ImageUploadException ex) {

        log.error("Error subiendo imagen: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "success", false,
                        "error", ex.getMessage()
                ));
    }
    
 // =====================================
 // FEATURE NO PERMITIDA POR PLAN
 // =====================================
 @ExceptionHandler(FeatureNotAllowedException.class)
 public ResponseEntity<?> handleFeatureNotAllowed(
         FeatureNotAllowedException ex) {

     log.warn("Feature no permitida por plan: {}", ex.getMessage());

     return ResponseEntity.status(HttpStatus.FORBIDDEN)
             .body(Map.of(
                     "success", false,
                     "error", "FEATURE_NOT_ALLOWED",
                     "message", ex.getMessage(),
                     "upgradeUrl", "/#saas"
             ));
 }
    

    // =====================================
    // FALLBACK GLOBAL
    // =====================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneral(Exception ex) {

        log.error("Error no controlado en API", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "success", false,
                        "error", "Error interno del servidor"
                ));
    }
}
