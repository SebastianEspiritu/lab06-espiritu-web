package com.tecsup.service;
import com.tecsup.exception.NotFoundException;
import com.tecsup.model.Producto;
import com.tecsup.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class ProductoService {
    private final ProductoRepository repo;
    public ProductoService(ProductoRepository repo) { this.repo = repo; }
    public List<Producto> listar() { return repo.findAll(); }
    public Producto guardar(Producto producto) { return repo.save(producto); }
    public Producto obtener(Long id) { return buscar(id); }
    public Producto actualizar(Long id, Producto cambios) {
        Producto producto = buscar(id);
        producto.setNombre(cambios.getNombre());
        producto.setPrecio(cambios.getPrecio());
        producto.setStock(cambios.getStock());
        return repo.save(producto);
    }
    public void eliminar(Long id) {
        Producto producto = buscar(id);
        repo.delete(producto);
    }
    private Producto buscar(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Producto no encontrado: " + id));
    }
}
