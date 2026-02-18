package com.uisrael.vault.repository;

import com.uisrael.vault.models.Descuento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface DescuentoRepository extends JpaRepository<Descuento, Long> {  // CAMBIADO: Integer → Long
    
    // Buscar descuentos por producto - CAMBIADO: Integer → Long
    List<Descuento> findByIdProducto(Long idProducto);
    
    // Buscar descuentos activos
    List<Descuento> findByEstadoTrue();
    
    // Buscar descuentos inactivos
    List<Descuento> findByEstadoFalse();
    
    // Buscar descuento activo de un producto específico - CAMBIADO: Integer → Long
    Optional<Descuento> findByIdProductoAndEstadoTrue(Long idProducto);
    
    // Buscar descuentos con porcentaje mayor a X
    List<Descuento> findByPorcentajeGreaterThan(BigDecimal porcentaje);
    
    // Verificar si un producto tiene descuento activo - CORREGIDO
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Descuento d WHERE d.idProducto = :idProducto AND d.estado = true")
    boolean existsDescuentoActivo(@Param("idProducto") Long idProducto);  // CAMBIADO: Integer → Long
    
    // Obtener todos los productos con sus descuentos
    @Query("SELECT p, d FROM Producto p LEFT JOIN Descuento d ON p.id = d.idProducto")
    List<Object[]> findProductosConDescuentos();
    
    // Obtener solo productos con descuentos activos
    @Query("SELECT p, d FROM Producto p JOIN Descuento d ON p.id = d.idProducto WHERE d.estado = true")
    List<Object[]> findProductosConDescuentosActivos();
    
    // Buscar por observación (like)
    List<Descuento> findByObservacionContaining(String texto);
    
    // Eliminar descuentos de un producto - CAMBIADO: Integer → Long
    void deleteByIdProducto(Long idProducto);
    
    // ===== MÉTODOS ADICIONALES ÚTILES =====
    
    // Versión simplificada sin @Query (Spring genera automáticamente)
    boolean existsByIdProductoAndEstadoTrue(Long idProducto);
    
    // Contar descuentos activos de un producto
    long countByIdProductoAndEstadoTrue(Long idProducto);
    
    // Buscar descuentos por rango de porcentaje
    List<Descuento> findByPorcentajeBetween(BigDecimal min, BigDecimal max);
}