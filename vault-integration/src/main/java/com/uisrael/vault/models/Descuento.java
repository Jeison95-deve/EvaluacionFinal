package com.uisrael.vault.models;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "descuentos")
public class Descuento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "idproducto", nullable = false)
    private Long idProducto;
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;
    
    @Column(nullable = false)
    private Boolean estado;
    
    @Column(length = 200)
    private String observacion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idproducto", insertable = false, updatable = false)
    @JsonIgnore
    private Producto producto;
    
    // Constructores
    public Descuento() {}
    
    public Descuento(Long idProducto, BigDecimal porcentaje, Boolean estado, String observacion) {
        this.idProducto = idProducto;
        this.porcentaje = porcentaje;
        this.estado = estado;
        this.observacion = observacion;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getIdProducto() { return idProducto; }
    public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }
    
    public BigDecimal getPorcentaje() { return porcentaje; }
    public void setPorcentaje(BigDecimal porcentaje) { this.porcentaje = porcentaje; }
    
    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }
    
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    
    public BigDecimal calcularPrecioConDescuento(BigDecimal precioOriginal) {
        if (estado != null && estado && 
            porcentaje != null && 
            porcentaje.compareTo(BigDecimal.ZERO) > 0) {
            
            BigDecimal factorDescuento = porcentaje.divide(
                BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP
            );
            BigDecimal descuento = precioOriginal.multiply(factorDescuento);
            
            return precioOriginal.subtract(descuento)
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return precioOriginal.setScale(2, RoundingMode.HALF_UP);
    }
}