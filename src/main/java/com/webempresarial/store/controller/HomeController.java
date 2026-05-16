package com.webempresarial.store.controller;

import jakarta.servlet.http.HttpServletRequest; 

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.webempresarial.store.dto.producto.publico.ProductoDetailDTO;
import com.webempresarial.store.repository.ResenaRepository;
import com.webempresarial.store.service.ProductoService;
import com.webempresarial.store.theme.StoreThemeResolver;

import java.util.List;

@Controller
public class HomeController {

    private final ProductoService productoService;
    private final ResenaRepository resenaRepository;
    private final StoreThemeResolver storeThemeResolver;

    public HomeController(
    	    ProductoService productoService,
    	    ResenaRepository resenaRepository,
    	    StoreThemeResolver storeThemeResolver
    	) {
    	    this.productoService = productoService;
    	    this.resenaRepository = resenaRepository;
    	    this.storeThemeResolver = storeThemeResolver;
    	}
    // =========================
    // DATA GLOBAL (REUTILIZABLE)
    // =========================
    private void cargarDatosGlobales(Model model) {

        List<String> categorias =
            productoService.obtenerCategorias();

        var productos =
            productoService.obtenerProductosIndexOptimizado();

        model.addAttribute("categorias", categorias);
        model.addAttribute("products", productos);
    }
    
 // =========================
 // LAYOUT GLOBAL
 // =========================
    private void aplicarLayout(Model model, HttpServletRequest request) {

        String theme = storeThemeResolver.getTheme(request);

        model.addAttribute("theme", theme);

        switch (theme) {

            // =========================
            // WEBEMPRESARIAL
            // =========================
            case "WebEmpresarial":

                model.addAttribute("showCart", false);
                model.addAttribute("showCheckout", false);
                model.addAttribute("showAuth", false);
                model.addAttribute("showScripts", true);

                break;

            // =========================
            // STRIDE
            // =========================
            case "stride":

                model.addAttribute("showCart", true);
                model.addAttribute("showCheckout", true);
                model.addAttribute("showAuth", true);
                model.addAttribute("showScripts", true);

                break;

            // =========================
            // PUNCHBARLEY
            // =========================
            case "punchbarley":

                model.addAttribute("showCart", true);
                model.addAttribute("showCheckout", true);
                model.addAttribute("showAuth", true);
                model.addAttribute("showScripts", true);

                break;

            // =========================
            // DEFAULT
            // =========================
            default:

                model.addAttribute("showCart", false);
                model.addAttribute("showCheckout", false);
                model.addAttribute("showAuth", false);
                model.addAttribute("showScripts", true);
        }
    }


 // =========================
//  HOME
// =========================
    @GetMapping({"/", "/inicio"})
    public String home(Model model, HttpServletRequest request) {

        aplicarLayout(model, request);

        String theme = storeThemeResolver.getTheme(request);

        if (theme.equals("WebEmpresarial")) {
            model.addAttribute(
                "title",
                "WebEmpresarial™ | Ecommerce, páginas web y sistemas empresariales"
            );

            model.addAttribute(
                "description",
                "Construimos ecommerce, sitios corporativos y sistemas web para empresas que quieren vender, automatizar y escalar."
            );

            return storeThemeResolver.view(request, "index");
        }

        cargarDatosGlobales(model);

        model.addAttribute("title", getStoreTitle(theme));
        model.addAttribute("description", getStoreDescription(theme));

        model.addAttribute(
            "resenasIniciales",
            resenaRepository.findAll(
                PageRequest.of(
                    0,
                    4,
                    Sort.by(Sort.Direction.DESC, "estrellas")
                )
            ).getContent()
        );

        return storeThemeResolver.view(request, "index");
    }
    
private String getStoreTitle(String theme) {
    return switch (theme) {
        case "stride" -> "Stride | Tienda online";
        case "punchbarley" -> "Punch Barley | Tienda online";
        default -> "Tienda online";
    };
}

private String getStoreDescription(String theme) {
    return switch (theme) {
        case "stride" -> "Compra productos de Stride en nuestra tienda online.";
        case "punchbarley" -> "Compra productos de Punch Barley en nuestra tienda online.";
        default -> "Compra productos en nuestra tienda online.";
    };
}

    // =========================
    //  BLOQUEAR LANDING DIRECTO
    // =========================
    @GetMapping("/landing-espacio")
    public String blockLandingDirect() {
        return "redirect:/";
    }

    // =========================
    //  MENÚ
    // =========================
    @GetMapping("/menu")
    public String verMenu(Model model, HttpServletRequest request) {
        aplicarLayout(model, request);
        cargarDatosGlobales(model);
        return storeThemeResolver.view(request, "index");
    }

    // =========================
    //  SUBIR PRODUCTO (PROTEGIDO)
    // =========================
    @GetMapping("/subirProducto")
    public String subirProducto(@AuthenticationPrincipal UserDetails user) {
        if (user == null) {
            return "redirect:/login";
        }
        return "admin/subirProducto";
    }

    // =========================
    //  FRAGMENTO MENÚ (AJAX)
    // =========================
    @GetMapping("/fragmento-menu")
    public String cargarFragmentoMenu(Model model, HttpServletRequest request) {

        cargarDatosGlobales(model);

        return storeThemeResolver.fragment(request, "menu") + " :: menu";    }
    
    
    
    @GetMapping("/producto-detalle/{id}")
    public String verDetalleProducto(@PathVariable Long id, Model model, HttpServletRequest request) {

        aplicarLayout(model, request);

        ProductoDetailDTO producto = productoService.obtenerDetalleProducto(id);
        model.addAttribute("producto", producto);

        return storeThemeResolver.view(request, "producto-detalle");
    }
    // =========================
    //  FRAGMENTO RESEÑAS
    // =========================
    @GetMapping("/fragmento-resenas")
    public String cargarResenasFragment(Model model, HttpServletRequest request) {

        model.addAttribute(
            "resenas",
            resenaRepository.findAll(
                Sort.by(Sort.Direction.DESC, "estrellas")
            )
        );

        return storeThemeResolver.fragment(request, "resenas") + " :: resenas";
    }

    // =========================
    //  LISTA COMPLETA RESEÑAS
    // =========================
    @GetMapping("/lista")
    public String listarResenas(Model model, HttpServletRequest request) {

        aplicarLayout(model, request);

        model.addAttribute(
            "resenas",
            resenaRepository.findAllByOrderByEstrellasDesc()
        );

        return storeThemeResolver.view(request, "resenas");
    }

    // =========================
    //  CHECKOUT CANCELADO
    // =========================
    @GetMapping("/checkout-cancel")
    public String checkoutCancel(Model model, HttpServletRequest request) {
        aplicarLayout(model, request);
        return storeThemeResolver.view(request, "checkout-cancel");
    }

    // =========================
    //  COMPRA EXITOSA
    // =========================
    @GetMapping("/gracias")
    public String gracias(Model model, HttpServletRequest request) {
        aplicarLayout(model, request);
        return storeThemeResolver.view(request, "gracias");
    }

    // =========================
    //  SERVICIO
    // =========================
    @GetMapping("/admin")
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }
    // =========================
    //  PRIVACIDAD
    // =========================
    @GetMapping("/privacy")
    public String privacy(Model model, HttpServletRequest request) {

        aplicarLayout(model, request);

        return storeThemeResolver.view(request, "privacy");
    }
}