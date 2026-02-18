package com.uisrael.vault.models;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "producto")  // CAMBIADO: "Producto" → "producto" (minúscula)
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 100, nullable = false)
    private String nombre;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;
    
    @Column(nullable = false)
    private Boolean iva;
    
    @Column(name = "idcategoria")
    private Long idcategoria;

    // Constructores
    public Producto() {}
    
    public Producto(String nombre, BigDecimal precio, Boolean iva, Long idcategoria) {
        this.nombre = nombre;
        this.precio = precio;
        this.iva = iva;
        this.idcategoria = idcategoria;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public Boolean getIva() { return iva; }
    public void setIva(Boolean iva) { this.iva = iva; }
    public Long getIdcategoria() { return idcategoria; }
    public void setIdcategoria(Long idcategoria) { this.idcategoria = idcategoria; }
    
    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", iva=" + iva +
                ", idcategoria=" + idcategoria +
                '}';
    }
}