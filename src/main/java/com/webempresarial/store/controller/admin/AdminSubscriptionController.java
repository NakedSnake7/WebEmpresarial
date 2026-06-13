package com.webempresarial.store.controller.admin;

import java.math.BigDecimal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.service.SubscriptionService;
import com.webempresarial.store.repository.StoreRepository;
import com.webempresarial.store.repository.SubscriptionRepository;

@Controller
@RequestMapping("/admin/subscriptions")
public class AdminSubscriptionController {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;
    private final StoreRepository storeRepository;

    public AdminSubscriptionController(
            SubscriptionRepository subscriptionRepository,
            SubscriptionService subscriptionService,
            StoreRepository storeRepository
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionService = subscriptionService;
        this.storeRepository = storeRepository;
    }

    @GetMapping
    public String subscriptions(Model model) {

        model.addAttribute(
                "subscriptions",
                subscriptionRepository.findAllWithStore()
        );

        return "admin/subscriptions/list";
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Long id) {

        subscriptionService.activateManual(id);

        return "redirect:/admin/subscriptions";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id) {

        subscriptionService.cancelById(id);

        return "redirect:/admin/subscriptions";
    }

    @PostMapping("/{id}/expire")
    public String expire(@PathVariable Long id) {

        subscriptionService.expireById(id);

        return "redirect:/admin/subscriptions";
    }

    @PostMapping("/{id}/reactivate")
    public String reactivate(@PathVariable Long id) {

        subscriptionService.reactivate(id);

        return "redirect:/admin/subscriptions";
    }

    @PostMapping("/{id}/trial")
    public String startTrial(
            @PathVariable Long id,
            @RequestParam(defaultValue = "14") int days
    ) {

        subscriptionService.startTrial(id, days);

        return "redirect:/admin/subscriptions";
    }

    @PostMapping("/{id}/plan")
    public String changePlan(
            @PathVariable Long id,
            @RequestParam StorePlan plan,
            @RequestParam BigDecimal monthlyAmount
    ) {

        subscriptionService.changePlan(
                id,
                plan,
                monthlyAmount
        );

        return "redirect:/admin/subscriptions";
    }
    
    @PostMapping("/internal/create")
    public String createInternalSubscription(
            @RequestParam Long storeId,
            @RequestParam StorePlan plan
    ) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Tienda no encontrada"));

        subscriptionService.createInternalSubscription(store, plan);

        return "redirect:/admin/subscriptions";
    }
    
    
}