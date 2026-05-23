package com.webempresarial.store.controller.admin;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.StoreAdminService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/stores")
public class StoreAdminController {

    private final StoreAdminService storeAdminService;

    public StoreAdminController(StoreAdminService storeAdminService) {
        this.storeAdminService = storeAdminService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("stores", storeAdminService.listarTiendas());
        return "admin/stores/list";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("store", new Store());
        return "admin/stores/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Store store) {
        storeAdminService.guardar(store);
        return "redirect:/admin/stores";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("store", storeAdminService.buscarPorId(id));
        return "admin/stores/form";
    }

    @PostMapping("/estado/{id}")
    public String cambiarEstado(@PathVariable Long id) {
        storeAdminService.cambiarEstado(id);
        return "redirect:/admin/stores";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        storeAdminService.eliminar(id);
        return "redirect:/admin/stores";
    }
}