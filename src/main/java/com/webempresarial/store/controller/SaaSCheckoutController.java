package com.webempresarial.store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SaaSCheckoutController {

    @GetMapping("/billing/success")
    public String success(
            @RequestParam(
                    name = "session_id",
                    required = false
            )
            String sessionId,
            Model model
    ) {
        model.addAttribute("sessionId", sessionId);

        return "billing/success";
    }
}