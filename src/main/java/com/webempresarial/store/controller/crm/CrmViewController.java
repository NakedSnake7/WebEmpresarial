package com.webempresarial.store.controller.crm;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CrmViewController {

    @GetMapping("/crm/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("crmPage", true);
        model.addAttribute("title", "CRM Dashboard | WebEmpresarial");
        return "crm/dashboard";
    }

    @GetMapping("/crm/pipeline")
    public String pipeline(Model model) {
        model.addAttribute("crmPage", true);
        model.addAttribute("title", "Pipeline Comercial | WebEmpresarial");
        return "crm/pipeline";
    }

    @GetMapping("/crm/tasks")
    public String tasks(Model model) {
        model.addAttribute("crmPage", true);
        model.addAttribute("title", "Tareas Comerciales | WebEmpresarial");
        return "crm/tasks";
    }

    @GetMapping("/crm/reports")
    public String reports(Model model) {
        model.addAttribute("crmPage", true);
        model.addAttribute("title", "Reportes CRM | WebEmpresarial");
        return "crm/reports";
    }
}