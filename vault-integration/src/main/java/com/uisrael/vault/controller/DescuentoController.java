package com.uisrael.vault.controller;

import com.uisrael.vault.models.Descuento;
import com.uisrael.vault.models.Producto;
import com.uisrael.vault.repository.DescuentoRepository;

import com.uisrael.vault.repository.ProductoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

import javax.validation.Valid;

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
    
    // GET: Obtener descuento por ID
    @GetMapping("/{id}")
    public ResponseEntity<Descuento> getDescuentoById(@PathVariable Integer id) {
        Optional<Descuento> descuento = descuentoRepository.findById(id);
        return descuento.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    // GET: Obtener descuentos por producto
    @GetMapping("/producto/{idProducto}")
    public ResponseEntity<List<Descuento>> getDescuentosByProducto(@PathVariable Integer idProducto) {
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
    public ResponseEntity<Descuento> getDescuentoActivoByProducto(@PathVariable Integer idProducto) {
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
                
              
            } else {
                item.put("descuento", null);
                item.put("precioFinal", producto.getPrecio());
                item.put("ahorro", BigDecimal.ZERO);
            }
            
            response.add(item);
        }
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
  
    
    // PUT: Actualizar descuento
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDescuento(@PathVariable Integer id, @Valid @RequestBody Descuento descuentoDetails) {
        Optional<Descuento> descuentoOpt = descuentoRepository.findById(id);
        
        if (descuentoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Descuento descuento = descuentoOpt.get();
        descuento.setPorcentaje(descuentoDetails.getPorcentaje());
        descuento.setEstado(descuentoDetails.getEstado());
        descuento.setObservacion(descuentoDetails.getObservacion());
        
        Descuento updatedDescuento = descuentoRepository.save(descuento);
        return ResponseEntity.ok(updatedDescuento);
    }
    
    // PATCH: Activar/Desactivar descuento
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Descuento> cambiarEstado(@PathVariable Integer id, @RequestParam Boolean estado) {
        Optional<Descuento> descuentoOpt = descuentoRepository.findById(id);
        
        if (descuentoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Descuento descuento = descuentoOpt.get();
        descuento.setEstado(estado);
        Descuento updatedDescuento = descuentoRepository.save(descuento);
        return ResponseEntity.ok(updatedDescuento);
    }
    
    // DELETE: Eliminar descuento
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDescuento(@PathVariable Integer id) {
        if (!descuentoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        descuentoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    // DELETE: Eliminar todos los descuentos de un producto
    @DeleteMapping("/producto/{idProducto}")
    public ResponseEntity<Void> deleteDescuentosByProducto(@PathVariable Integer idProducto) {
        descuentoRepository.deleteByIdProducto(idProducto);
        return ResponseEntity.noContent().build();
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
        
        OptionalDouble promedio = activos.stream()
                .mapToDouble(d -> d.getPorcentaje().doubleValue())
                .average();
        
        stats.put("porcentajePromedioActivos", promedio.orElse(0.0));
        
        return ResponseEntity.ok(stats);
    }
}