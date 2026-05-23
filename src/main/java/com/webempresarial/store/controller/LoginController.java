package com.webempresarial.store.controller;

import org.springframework.stereotype.Controller; 
import org.springframework.web.bind.annotation.GetMapping;

import com.webempresarial.store.theme.StoreThemeResolver;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class LoginController {

    private final StoreThemeResolver themeResolver;

    public LoginController(StoreThemeResolver themeResolver) {
        this.themeResolver = themeResolver;
    }

    // LOGIN CLIENTE (dinámico por tienda)
    @GetMapping("/login")
    public String loginCliente(HttpServletRequest request) {
        return themeResolver.view(request, "login");
    }

    // LOGIN ADMIN
    @GetMapping("/admin/login")
    public String loginAdmin() {
        return "admin/login";
    }
}