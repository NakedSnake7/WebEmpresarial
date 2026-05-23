package com.webempresarial.store.model;



import com.fasterxml.jackson.annotation.JsonBackReference;  
import jakarta.persistence.*;

@Entity
@Table(
    name = "variante_atributos",
    indexes = {
        @Index(name = "idx_atributo_variante", columnList = "variante_id")
    }
)
public class VarianteAtributo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ej: "talla", "color", "ram", "almacenamiento"
    @Column(nullable = false, length = 80)
    private String nombre;

    // Ej: "42", "rojo", "16GB", "512GB"
    @Column(nullable = false, length = 120)
    private String valor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variante_id", nullable = false)
    @JsonBackReference
    private ProductoVariante variante;
    
    
    
    // -----------------------------
    // GETTERS / SETTERS
    // -----------------------------

    public Long getId() {
        return id;
    }

    public ProductoVariante getVariante() {
		return variante;
	}

	public void setVariante(ProductoVariante variante) {
		this.variante = variante;
	}

	public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

 
}