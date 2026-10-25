package com.webempresarial.store.controller.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

import com.webempresarial.store.exception.ImageUploadException;
import com.webempresarial.store.exceptions.FeatureLockedException;
import com.webempresarial.store.service.ProductoService;

import jakarta.servlet.http.HttpServletRequest;
import com.webempresarial.store.exceptions.StoreNotFoundException;

@Order(Ordered.LOWEST_PRECEDENCE)
@ControllerAdvice
public class MvcExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(MvcExceptionHandler.class);

    // =====================================
    // PRODUCTO NO ENCONTRADO
    // =====================================
    @ExceptionHandler(ProductoService.ProductoNotFoundException.class)
    public String handleProductoNotFound(
            ProductoService.ProductoNotFoundException ex,
            Model model) {

        log.warn("Producto no encontrado (MVC): {}", ex.getMessage());

        model.addAttribute("error", ex.getMessage());
        return "error";
    }

    // =====================================
    // FEATURE BLOQUEADA POR PLAN
    // =====================================
    @ExceptionHandler(FeatureLockedException.class)
    public String handleFeatureLocked(
            FeatureLockedException ex,
            RedirectAttributes redirectAttributes
    ) {

        redirectAttributes.addFlashAttribute(
                "lockedFeature",
                ex.getFeature().name()
        );

        return "redirect:/admin/upgrade?feature="
                + ex.getFeature().name();
    }

    // =====================================
    // ERRORES DE NEGOCIO
    // =====================================
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(
            IllegalArgumentException ex,
            Model model) {

        log.warn("Error de negocio (MVC): {}", ex.getMessage());

        model.addAttribute("error", ex.getMessage());
        return "error";
    }

    // =====================================
    // IMAGE UPLOAD EXCEPTION
    // =====================================
    @ExceptionHandler(ImageUploadException.class)
    public String handleImageUpload(
            ImageUploadException ex,
            Model model) {

        log.error("Error subiendo imagen (MVC): {}", ex.getMessage(), ex);

        model.addAttribute("error", ex.getMessage());
        return "error";
    }

    // =====================================
    // HTTP STATUS EXPLÍCITO
    // =====================================
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Void> handleResponseStatusException(
            ResponseStatusException ex) {

        return ResponseEntity
                .status(ex.getStatusCode())
                .build();
    }

    // =====================================
    // TENANT / STORE NO ENCONTRADO
    // =====================================
    @ExceptionHandler(StoreNotFoundException.class)
    public ResponseEntity<Void> handleStoreNotFound(
            StoreNotFoundException ex) {

        log.warn("Tienda no encontrada (MVC): {}", ex.getMessage());

        return ResponseEntity
                .notFound()
                .build();
    }

    // =====================================
    // ERROR GENERAL
    // =====================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> handleGeneral(
            Exception ex,
            HttpServletRequest request) {

        log.error("Error MVC no controlado en path={}",
                request.getRequestURI(), ex);

        return ResponseEntity
                .internalServerError()
                .build();
    }

 // =====================================
 // RECURSOS ESTÁTICOS NO EXISTENTES
 // (.well-known, Chrome, bots, etc.)
 // =====================================
 @ExceptionHandler(NoResourceFoundException.class)
 public void handleNoResource(
         NoResourceFoundException ex,
         HttpServletRequest request) {

     String path = request.getRequestURI();

     // Ignorar requests automáticos
     if (path.startsWith("/.well-known/")
             || path.equals("/favicon.ico")) {
         return; // ⛔ NO log, NO error, NO vista
     }

     // Otros recursos sí se loggean
     log.warn("Recurso estático no encontrado: {}", path);
 }



}
