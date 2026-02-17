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
public interface DescuentoRepository extends JpaRepository<Descuento, Integer> {
    
    // Buscar descuentos por producto
    List<Descuento> findByIdProducto(Integer idProducto);
    
    // Buscar descuentos activos
    List<Descuento> findByEstadoTrue();
    
    // Buscar descuentos inactivos
    List<Descuento> findByEstadoFalse();
    
    // Buscar descuento activo de un producto específico
    Optional<Descuento> findByIdProductoAndEstadoTrue(Integer idProducto);
    
    // Buscar descuentos con porcentaje mayor a X
    List<Descuento> findByPorcentajeGreaterThan(BigDecimal porcentaje);
    
    // Verificar si un producto tiene descuento activo
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM descuentos d WHERE d.idProducto = :idProducto AND d.estado = true")
    boolean existsDescuentoActivo(@Param("idProducto") Integer idProducto);
    
    // Obtener todos los productos con sus descuentos (JPQL)
    @Query("SELECT p, d FROM Producto p LEFT JOIN Descuento d ON p.id = d.idProducto")
    List<Object[]> findProductosConDescuentos();
    
    // Obtener solo productos con descuentos activos
    @Query("SELECT p, d FROM Producto p JOIN Descuento d ON p.id = d.idProducto WHERE d.estado = true")
    List<Object[]> findProductosConDescuentosActivos();
    
    // Buscar por observación (like)
    List<Descuento> findByObservacionContaining(String texto);
    
    // Eliminar descuentos de un producto
    void deleteByIdProducto(Integer idProducto);
}