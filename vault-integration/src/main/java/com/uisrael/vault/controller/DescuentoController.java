package com.uisrael.vault.controller;

import com.uisrael.vault.models.Descuento;
import com.uisrael.vault.models.Producto;
import com.uisrael.vault.repository.DescuentoRepository;
import com.uisrael.vault.repository.ProductoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping("/api/descuentos")
public class DescuentoController {
    
    @Autowired
    private DescuentoRepository descuentoRepository;
    
    @Autowired
    private ProductoRepository productRepository;
    
    // GET: Obtener todos los descuentos
    @GetMapping
    public ResponseEntity<List<Descuento>> getAllDescuentos() {
        List<Descuento> descuentos = descuentoRepository.findAll();
        return new ResponseEntity<>(descuentos, HttpStatus.OK);
    }
    
    // GET: Obtener descuento por ID - CORREGIDO
    @GetMapping("/{id}")
    public ResponseEntity<Descuento> getDescuentoById(@PathVariable Long id) {  // CAMBIADO: Integer → Long
        Optional<Descuento> descuento = descuentoRepository.findById(id);  // CORREGIDO: findAll → findById
        return descuento.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    // GET: Obtener descuentos por producto
    @GetMapping("/producto/{idProducto}")
    public ResponseEntity<List<Descuento>> getDescuentosByProducto(@PathVariable Long idProducto) {
        List<Descuento> descuentos = descuentoRepository.findByIdProducto(idProducto);
        return new ResponseEntity<>(descuentos, HttpStatus.OK);
    }
    
    // GET: Obtener descuentos activos
    @GetMapping("/activos")
    public ResponseEntity<List<Descuento>> getDescuentosActivos() {
        List<Descuento> descuentos = descuentoRepository.findByEstadoTrue();
        return new ResponseEntity<>(descuentos, HttpStatus.OK);
    }
    
    // GET: Obtener descuento activo de un producto
    @GetMapping("/producto/{idProducto}/activo")
    public ResponseEntity<Descuento> getDescuentoActivoByProducto(@PathVariable Long idProducto) {
        Optional<Descuento> descuento = descuentoRepository.findByIdProductoAndEstadoTrue(idProducto);
        return descuento.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    // GET: Productos con descuentos (DETALLADO)
    @GetMapping("/productos-con-descuentos")
    public ResponseEntity<List<Map<String, Object>>> getProductosConDescuentos() {
        List<Object[]> resultados = descuentoRepository.findProductosConDescuentos();
        List<Map<String, Object>> response = new ArrayList<>();
        
        for (Object[] row : resultados) {
            Producto producto = (Producto) row[0];
            Descuento descuento = (Descuento) row[1];
            
            Map<String, Object> item = new HashMap<>();
            item.put("idProducto", producto.getId());
            item.put("nombreProducto", producto.getNombre());
            item.put("precioOriginal", producto.getPrecio());
            
            if (descuento != null) {
                Map<String, Object> descuentoInfo = new HashMap<>();
                descuentoInfo.put("id", descuento.getId());
                descuentoInfo.put("porcentaje", descuento.getPorcentaje());
                descuentoInfo.put("estado", descuento.getEstado() ? "Activo" : "Inactivo");
                descuentoInfo.put("observacion", descuento.getObservacion());
                
                BigDecimal precioFinal = descuento.calcularPrecioConDescuento(producto.getPrecio());
                BigDecimal ahorro = producto.getPrecio().subtract(precioFinal);
                
                item.put("descuento", descuentoInfo);
                item.put("precioFinal", precioFinal);
                item.put("ahorro", ahorro);
            } else {
                item.put("descuento", null);
                item.put("precioFinal", producto.getPrecio());
                item.put("ahorro", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            }
            
            response.add(item);
        }
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    // POST: Crear nuevo descuento - CORREGIDO (sin conversión)
    @PostMapping
    public ResponseEntity<?> createDescuento(@Valid @RequestBody Descuento descuento) {
        try {
            // Validar que el ID de producto no sea nulo
            if (descuento.getIdProducto() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El ID del producto es requerido"));
            }
            
            // Ya no necesitas conversión - idProducto ahora es Long
            if (!productRepository.existsById(descuento.getIdProducto())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El producto con ID " + descuento.getIdProducto() + " no existe"));
            }
            
            // Validar si ya existe descuento activo
            if (descuento.getEstado() && 
                descuentoRepository.existsByIdProductoAndEstadoTrue(descuento.getIdProducto())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El producto ya tiene un descuento activo"));
            }
            
            // Validar porcentaje
            if (descuento.getPorcentaje() == null || 
                descuento.getPorcentaje().compareTo(BigDecimal.ZERO) <= 0 ||
                descuento.getPorcentaje().compareTo(BigDecimal.valueOf(100)) > 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El porcentaje debe estar entre 0 y 100"));
            }
            
            Descuento nuevoDescuento = descuentoRepository.save(descuento);
            return new ResponseEntity<>(nuevoDescuento, HttpStatus.CREATED);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al crear descuento: " + e.getMessage()));
        }
    }
    
    // PUT: Actualizar descuento - CORREGIDO
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDescuento(@PathVariable Long id, @Valid @RequestBody Descuento descuentoDetails) {
        try {
            Optional<Descuento> descuentoOpt = descuentoRepository.findById(id);
            
            if (descuentoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Validar porcentaje
            if (descuentoDetails.getPorcentaje() == null || 
                descuentoDetails.getPorcentaje().compareTo(BigDecimal.ZERO) <= 0 ||
                descuentoDetails.getPorcentaje().compareTo(BigDecimal.valueOf(100)) > 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El porcentaje debe estar entre 0 y 100"));
            }
            
            Descuento descuento = descuentoOpt.get();
            descuento.setPorcentaje(descuentoDetails.getPorcentaje());
            descuento.setEstado(descuentoDetails.getEstado());
            descuento.setObservacion(descuentoDetails.getObservacion());
            
            Descuento updatedDescuento = descuentoRepository.save(descuento);
            return ResponseEntity.ok(updatedDescuento);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar descuento: " + e.getMessage()));
        }
    }
    
    // PATCH: Activar/Desactivar descuento - CORREGIDO
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam Boolean estado) {
        try {
            Optional<Descuento> descuentoOpt = descuentoRepository.findById(id);
            
            if (descuentoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Descuento descuento = descuentoOpt.get();
            descuento.setEstado(estado);
            Descuento updatedDescuento = descuentoRepository.save(descuento);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedDescuento.getId());
            response.put("estado", updatedDescuento.getEstado());
            response.put("mensaje", "Estado actualizado correctamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al cambiar estado: " + e.getMessage()));
        }
    }
    
    // DELETE: Eliminar descuento - CORREGIDO
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDescuento(@PathVariable Long id) {
        if (!descuentoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        descuentoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    // DELETE: Eliminar todos los descuentos de un producto
    @DeleteMapping("/producto/{idProducto}")
    public ResponseEntity<?> deleteDescuentosByProducto(@PathVariable Long idProducto) {
        try {
            if (!productRepository.existsById(idProducto)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El producto con ID " + idProducto + " no existe"));
            }
            
            List<Descuento> descuentos = descuentoRepository.findByIdProducto(idProducto);
            if (descuentos.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El producto no tiene descuentos"));
            }
            
            descuentoRepository.deleteByIdProducto(idProducto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Descuentos eliminados correctamente");
            response.put("cantidad", descuentos.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar descuentos: " + e.getMessage()));
        }
    }
    
    // GET: Estadísticas de descuentos
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> getEstadisticas() {
        List<Descuento> todos = descuentoRepository.findAll();
        List<Descuento> activos = descuentoRepository.findByEstadoTrue();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDescuentos", todos.size());
        stats.put("descuentosActivos", activos.size());
        stats.put("descuentosInactivos", todos.size() - activos.size());
        
        // Calcular promedio con BigDecimal
        BigDecimal promedio = activos.stream()
                .map(Descuento::getPorcentaje)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(activos.isEmpty() ? BigDecimal.ONE : BigDecimal.valueOf(activos.size()), 2, RoundingMode.HALF_UP);
        
        stats.put("porcentajePromedioActivos", activos.isEmpty() ? BigDecimal.ZERO : promedio);
        
        // Producto con mayor descuento
        Optional<Descuento> mayorDescuento = activos.stream()
                .max(Comparator.comparing(Descuento::getPorcentaje));
        
        if (mayorDescuento.isPresent()) {
            Descuento d = mayorDescuento.get();
            Map<String, Object> mayor = new HashMap<>();
            mayor.put("id", d.getId());
            mayor.put("idProducto", d.getIdProducto());
            mayor.put("porcentaje", d.getPorcentaje());
            stats.put("mayorDescuentoActivo", mayor);
        }
        
        return ResponseEntity.ok(stats);
    }
}