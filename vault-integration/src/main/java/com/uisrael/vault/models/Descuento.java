package com.uisrael.vault.models;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "descuentos")
public class Descuento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "idproducto", nullable = false)
    private Integer idProducto;
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;
    
    @Column(nullable = false)
    private Boolean estado;
    
    @Column(length = 200)
    private String observacion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idproducto", insertable = false, updatable = false)
    private Producto producto;
    
    public Descuento() {}
    
    public Descuento(Integer idProducto, BigDecimal porcentaje, Boolean estado, String observacion) {
        this.idProducto = idProducto;
        this.porcentaje = porcentaje;
        this.estado = estado;
        this.observacion = observacion;
    }
    
    // Getters y Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getIdProducto() {
        return idProducto;
    }
    
    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }
    
    public BigDecimal getPorcentaje() {
        return porcentaje;
    }
    
    public void setPorcentaje(BigDecimal porcentaje) {
        this.porcentaje = porcentaje;
    }
    
    public Boolean getEstado() {
        return estado;
    }
    
    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
    
    public String getObservacion() {
        return observacion;
    }
    
    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
    
    public Producto getProducto() {
        return producto;
    }
    
    public void setProducto(Producto producto) {
        this.producto = producto;
    }
    
    public BigDecimal calcularPrecioConDescuento(BigDecimal precioOriginal) {
        if (estado != null && estado && porcentaje != null && porcentaje.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal descuento = precioOriginal.multiply(porcentaje.divide(BigDecimal.valueOf(100)));
            return precioOriginal.subtract(descuento);
        }
        return precioOriginal;
    }
    
    @Override
    public String toString() {
        return "Descuento{" +
                "id=" + id +
                ", idProducto=" + idProducto +
                ", porcentaje=" + porcentaje +
                ", estado=" + estado +
                ", observacion='" + observacion + '\'' +
                '}';
    }
}