package com.webempresarial.store.controller.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.webempresarial.store.exception.ImageUploadException;
import com.webempresarial.store.service.ProductoService;

import jakarta.servlet.http.HttpServletRequest;

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
    // ERROR GENERAL
    // =====================================
    @ExceptionHandler(Exception.class)
    public String handleGeneral(
            Exception ex,
            Model model,
            HttpServletRequest request) {

        log.error("Error MVC no controlado en path={}",
                request.getRequestURI(), ex);

        model.addAttribute("error",
                "Ocurrió un error inesperado. Intenta nuevamente.");

        model.addAttribute("path", request.getRequestURI());

        return "error";
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
