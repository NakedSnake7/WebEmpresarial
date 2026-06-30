package com.webempresarial.store.controller;

import com.webempresarial.store.dto.producto.publico.ProductoDetailDTO; 
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.ResenaRepository;
import com.webempresarial.store.service.ProductoService;
import com.webempresarial.store.theme.StoreResolver;
import com.webempresarial.store.theme.StoreThemeResolver;
import com.webempresarial.store.feature.registry.DashboardRegistry;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class HomeController {

    private final ProductoService productoService;
    private final ResenaRepository resenaRepository;
    private final StoreThemeResolver storeThemeResolver;
    private final StoreResolver storeResolver;
    private final DashboardRegistry dashboardRegistry;

    public HomeController(
            ProductoService productoService,
            ResenaRepository resenaRepository,
            StoreThemeResolver storeThemeResolver,
            StoreResolver storeResolver,
            DashboardRegistry dashboardRegistry
    ) {
        this.productoService = productoService;
        this.resenaRepository = resenaRepository;
        this.storeThemeResolver = storeThemeResolver;
        this.storeResolver = storeResolver;
        this.dashboardRegistry = dashboardRegistry;
    }

    private void cargarDatosGlobales(Model model, Store store) {

        List<String> categorias =
                productoService.obtenerCategorias(store);

        var productos =
                productoService.obtenerProductosIndexOptimizado(store);

        model.addAttribute("categorias", categorias);
        model.addAttribute("products", productos);
    }

    private void aplicarLayout(Model model, HttpServletRequest request) {

        Store store = storeResolver.getCurrentStore(request);
        String theme = store.getTheme();

        model.addAttribute("store", store);
        model.addAttribute("theme", theme);

        boolean esTienda = !theme.equals("WebEmpresarial");

        model.addAttribute("showCart", esTienda);
        model.addAttribute("showCheckout", esTienda);
        model.addAttribute("showAuth", esTienda);
        model.addAttribute("showScripts", true);
    }

    @GetMapping({"/", "/inicio"})
    public String home(Model model, HttpServletRequest request) {

        Store store = storeResolver.getCurrentStore(request);
        aplicarLayout(model, request);

        String theme = store.getTheme();

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

        cargarDatosGlobales(model, store);

        model.addAttribute("title", store.getNombre() + " | Tienda online");
        model.addAttribute("description", "Compra productos de " + store.getNombre() + " en nuestra tienda online.");

        model.addAttribute(
                "resenasIniciales",
                resenaRepository.findByStoreOrderByEstrellasDesc(
                        store,
                        PageRequest.of(0, 4)
                ).getContent()
        );

        return storeThemeResolver.view(request, "index");
    }

    @GetMapping("/landing-espacio")
    public String blockLandingDirect() {
        return "redirect:/";
    }

    @GetMapping("/menu")
    public String verMenu(Model model, HttpServletRequest request) {

        Store store = storeResolver.getCurrentStore(request);

        aplicarLayout(model, request);
        cargarDatosGlobales(model, store);

        return storeThemeResolver.view(request, "index");
    }

    @GetMapping("/subirProducto")
    public String subirProducto(@AuthenticationPrincipal UserDetails user) {
        if (user == null) {
            return "redirect:/login";
        }

        return "admin/subirProducto";
    }

    @GetMapping("/fragmento-menu")
    public String cargarFragmentoMenu(Model model, HttpServletRequest request) {

        Store store = storeResolver.getCurrentStore(request);

        cargarDatosGlobales(model, store);

        return storeThemeResolver.fragment(request, "menu") + " :: menu";
    }

    @GetMapping("/producto-detalle/{id}")
    public String verDetalleProducto(
            @PathVariable Long id,
            Model model,
            HttpServletRequest request
    ) {

        Store store = storeResolver.getCurrentStore(request);

        aplicarLayout(model, request);

        ProductoDetailDTO producto =
                productoService.obtenerDetalleProducto(id, store);

        model.addAttribute("producto", producto);

        return storeThemeResolver.view(request, "producto-detalle");
    }

    @GetMapping("/fragmento-resenas")
    public String cargarResenasFragment(Model model, HttpServletRequest request) {

        Store store = storeResolver.getCurrentStore(request);

        model.addAttribute(
                "resenas",
                resenaRepository.findByStoreOrderByEstrellasDesc(store)
        );

        return storeThemeResolver.fragment(request, "resenas") + " :: resenas";
    }

    @GetMapping("/lista")
    public String listarResenas(Model model, HttpServletRequest request) {

        Store store = storeResolver.getCurrentStore(request);

        aplicarLayout(model, request);

        model.addAttribute(
                "resenas",
                resenaRepository.findByStoreOrderByEstrellasDesc(store)
        );

        return storeThemeResolver.view(request, "resenas");
    }

    @GetMapping("/checkout-cancel")
    public String checkoutCancel(Model model, HttpServletRequest request) {
        aplicarLayout(model, request);
        return storeThemeResolver.view(request, "checkout-cancel");
    }

    @GetMapping("/gracias")
    public String gracias(Model model, HttpServletRequest request) {
        aplicarLayout(model, request);
        return storeThemeResolver.view(request, "gracias");
    }

    @GetMapping("/admin")
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(
            Model model,
            HttpServletRequest request
    ) {
        Store store = storeResolver.getCurrentStore(request);

        model.addAttribute("store", store);
        model.addAttribute("dashboardWidgets", dashboardRegistry.widgets(store));

        return "admin/dashboard";
    }

    @GetMapping("/privacy")
    public String privacy(Model model, HttpServletRequest request) {
        aplicarLayout(model, request);
        return storeThemeResolver.view(request, "privacy");
    }
}