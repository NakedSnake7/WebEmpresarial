package com.webempresarial.store.service;

import org.springframework.stereotype.Service;

import com.webempresarial.store.entity.Lead;

@Service
public class WhatsAppService {

    public void prepareInitialMessage(Lead lead) {

        String message = """
                Hola %s 👋

                Gracias por contactar a WebEmpresarial™.

                Recibimos tu solicitud sobre:
                %s

                En breve te contactaremos para ayudarte 🚀
                """
                .formatted(
                        lead.getNombre(),
                        lead.getServicio()
                );

        System.out.println(message);

        // TODO:
        // Integrar WhatsApp API real después
    }
}