package com.webempresarial.store.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ResenaEditDTO {

    @NotBlank(
            message = "El comentario es obligatorio"
    )
    @Size(
            max = 1000,
            message = "El comentario no puede exceder 1000 caracteres"
    )
    private String comentario;

    @NotNull(
            message = "La calificación es obligatoria"
    )
    @Min(
            value = 1,
            message = "La calificación mínima es 1"
    )
    @Max(
            value = 5,
            message = "La calificación máxima es 5"
    )
    private Integer estrellas;

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Integer getEstrellas() {
        return estrellas;
    }

    public void setEstrellas(Integer estrellas) {
        this.estrellas = estrellas;
    }
}
