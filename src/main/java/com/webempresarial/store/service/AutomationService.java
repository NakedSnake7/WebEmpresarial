package com.webempresarial.store.service;

import java.time.LocalDateTime; 

import org.springframework.stereotype.Service;

import com.webempresarial.store.entity.Lead;
import com.webempresarial.store.model.LeadPriority;

import com.webempresarial.store.commerce.infrastructure.order.notification.NotificationService;

@Service
public class AutomationService {

    private final SalesTaskService taskService;
    private final MailerSendService mailerSendService;
    private final WhatsAppService whatsAppService;
    private final NotificationService notificationService;

    public AutomationService(
            SalesTaskService taskService,
            MailerSendService mailerSendService,
            WhatsAppService whatsAppService,
            NotificationService notificationService
    ) {
        this.taskService = taskService;
        this.mailerSendService = mailerSendService;
        this.whatsAppService = whatsAppService;
        this.notificationService = notificationService;
    }

    public void onLeadCreated(Lead lead) {

        taskService.createTask(
                lead,
                "Contactar nuevo lead",
                "Contactar al lead lo antes posible. Lead nuevo desde " + lead.getSource(),
                LocalDateTime.now().plusHours(2),
                LeadPriority.HIGH
        );

        mailerSendService.sendLeadWelcomeEmail(lead);

        whatsAppService.prepareInitialMessage(lead);

        notificationService.notifyNewLead(lead);
    }

    public void onProposalSent(Lead lead) {

        taskService.createTask(
                lead,
                "Dar seguimiento a propuesta",
                "Revisar si el cliente ya vio o respondió la propuesta.",
                LocalDateTime.now().plusHours(24),
                LeadPriority.HIGH
        );
    }

    public void onLeadNotResponding24h(Lead lead) {

        taskService.createTask(
                lead,
                "Follow-up 24h",
                "Enviar seguimiento corto por WhatsApp.",
                LocalDateTime.now(),
                LeadPriority.MEDIUM
        );
    }
}