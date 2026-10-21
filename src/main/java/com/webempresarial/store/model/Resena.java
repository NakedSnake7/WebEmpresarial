package com.webempresarial.store.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

public class Resena {

    private Long id;

    @NotBlank(
            message = "El nombre es obligatorio"
    )
    @Size(
            max = 150,
            message = "El nombre no puede exceder 150 caracteres"
    )
    private String nombre;

    private MultipartFile imagen;

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

    /*
     * Campo administrativo intencional.
     *
     * El formulario de gestión permite marcar manualmente
     * una reseña como "Cliente verificado".
     */
    private boolean verificado;

    /*
     * Metadatos administrados exclusivamente por el servidor.
     * El controller bloquea su binding desde requests.
     */
    private String imagenUrl;
    private String publicId;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public MultipartFile getImagen() {
        return imagen;
    }

    public void setImagen(MultipartFile imagen) {
        this.imagen = imagen;
    }

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

    public boolean isVerificado() {
        return verificado;
    }

    public void setVerificado(boolean verificado) {
        this.verificado = verificado;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
