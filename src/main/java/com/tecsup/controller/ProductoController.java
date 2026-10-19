package com.tecsup.controller;
import com.tecsup.dto.ProductoDTO;
import com.tecsup.model.Producto;
import com.tecsup.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private final ProductoService service;
    public ProductoController(ProductoService service) { this.service = service; }
    @GetMapping
    public ResponseEntity<List<Producto>> listar() { return ResponseEntity.ok(service.listar()); }
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtener(@PathVariable Long id) { return ResponseEntity.ok(service.obtener(id)); }
    @PostMapping
    public ResponseEntity<Producto> guardar(@Valid @RequestBody ProductoDTO dto) {
        return ResponseEntity.status(201).body(service.guardar(producto(dto)));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, producto(dto)));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok("Eliminado correctamente");
    }
    private Producto producto(ProductoDTO dto) {
        return new Producto(null, dto.getNombre(), dto.getPrecio(), dto.getStock());
    }
}
