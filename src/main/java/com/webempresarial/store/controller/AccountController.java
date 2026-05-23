package com.webempresarial.store.controller;

import com.webempresarial.store.model.Order;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.Cliente;
import com.webempresarial.store.service.OrderService;
import com.webempresarial.store.service.UserService;
import com.webempresarial.store.theme.StoreResolver;
import com.webempresarial.store.theme.StoreThemeResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AccountController {

    private final UserService userService;
    private final OrderService orderService;
    private final StoreThemeResolver storeThemeResolver;
    private final StoreResolver storeResolver;

    public AccountController(
            UserService userService,
            OrderService orderService,
            StoreThemeResolver storeThemeResolver,
            StoreResolver storeResolver
    ) {
        this.userService = userService;
        this.orderService = orderService;
        this.storeThemeResolver = storeThemeResolver;
        this.storeResolver = storeResolver;
    }

    @GetMapping("/cuenta")
    public String cuenta(
            HttpServletRequest request,
            @AuthenticationPrincipal UserDetails authUser,
            Model model
    ) {
        if (authUser == null) {
            return "redirect:/login";
        }

        Store store = storeResolver.getCurrentStore(request);

        Cliente usuario = userService
                .findByEmail(authUser.getUsername(), store)
                .orElseThrow();

        String direccion = usuario.getDefaultAddress();

        if (direccion == null) {
            direccion = orderService.obtenerUltimaDireccion(usuario, store);
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("direccion", direccion);

        return storeThemeResolver.view(request, "cuenta");
    }

    @PostMapping("/cuenta/actualizar")
    public String actualizarCuenta(
            HttpServletRequest request,
            @AuthenticationPrincipal UserDetails authUser,
            @RequestParam("fullName") String fullName,
            @RequestParam("phone") String phone,
            RedirectAttributes redirectAttributes
    ) {
        if (authUser == null) {
            return "redirect:/login";
        }

        Store store = storeResolver.getCurrentStore(request);

        Cliente usuario = userService
                .findByEmail(authUser.getUsername(), store)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setFullName(fullName);
        usuario.setPhone(phone);

        userService.save(usuario, store);

        redirectAttributes.addFlashAttribute(
                "success",
                "Datos actualizados correctamente"
        );

        return "redirect:/cuenta";
    }

    @PostMapping("/cuenta/direccion")
    public String guardarDireccion(
            HttpServletRequest request,
            @AuthenticationPrincipal UserDetails authUser,
            @RequestParam String address
    ) {
        Store store = storeResolver.getCurrentStore(request);

        Cliente cliente = userService
                .findByEmail(authUser.getUsername(), store)
                .orElseThrow();

        cliente.setDefaultAddress(address);
        userService.save(cliente, store);

        return "redirect:/cuenta";
    }

    @GetMapping("/pedidos")
    public String misPedidos(
            HttpServletRequest request,
            @AuthenticationPrincipal UserDetails authUser,
            Model model
    ) {
        if (authUser == null) {
            return "redirect:/login";
        }

        Store store = storeResolver.getCurrentStore(request);

        List<Order> pedidos = orderService.findByCustomerEmail(
                authUser.getUsername(),
                store
        );

        model.addAttribute("pedidos", pedidos);

        return storeThemeResolver.view(request, "pedidos");
    }
}