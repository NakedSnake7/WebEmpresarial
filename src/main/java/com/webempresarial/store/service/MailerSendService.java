package com.webempresarial.store.service;

import org.springframework.stereotype.Service;

import com.webempresarial.store.entity.Lead;

@Service
public class MailerSendService {

    public void sendLeadWelcomeEmail(Lead lead) {
        // TODO: integrar MailerSend real después
        System.out.println("Email automático preparado para lead: " + lead.getNombre());
    }
}