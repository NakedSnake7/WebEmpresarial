package com.webempresarial.store.controller.admin.dev;

import com.webempresarial.store.feature.automation.AutomationTrigger; 
import com.webempresarial.store.feature.event.PlatformEvent;
import com.webempresarial.store.feature.event.PlatformEventBus;
import com.webempresarial.store.feature.registry.AutomationRegistry;



import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/platform/dev")
public class PlatformDevController {

    private final PlatformEventBus platformEventBus;
    private final AutomationRegistry automationRegistry;

    public PlatformDevController(PlatformEventBus platformEventBus, AutomationRegistry automationRegistry) {
        this.platformEventBus = platformEventBus;
        this.automationRegistry = automationRegistry;
    }

    @GetMapping
    public String index(Model model) {
    	model.addAttribute("defaultEvent", AutomationTrigger.LEAD_CREATED);

    	model.addAttribute(
    		    "events",
    		    automationRegistry.getRegisteredTriggers()
    		);
        return "admin/platform/dev/index";
    }

    @PostMapping("/publish-event")
    public String publishEvent(
            @RequestParam String eventName,
            @RequestParam(defaultValue = "DEV") String sourceModule,
            @RequestParam(defaultValue = "TEST_PAYLOAD") String payload
    ) {
        platformEventBus.publish(
                PlatformEvent.of(
                        eventName,
                        sourceModule,
                        payload
                )
        );

        return "redirect:/admin/platform/operations";
    }
}