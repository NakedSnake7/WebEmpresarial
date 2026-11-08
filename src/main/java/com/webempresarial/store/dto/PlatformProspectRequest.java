package com.webempresarial.store.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlatformProspectRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Size(max = 40) String whatsapp,
        @Size(max = 120) String empresa,
        @Size(max = 150) String instagram,
        @Size(max = 120) String servicio,
        @Size(max = 120) String presupuesto,
        @Size(max = 2000) String objetivo,
        @Size(max = 20) String preferenciaPagoPiloto,
        @Size(max = 12) String cohortHint,
        @Size(max = 24) String form,
        @Size(max = 255) String website
) {}
