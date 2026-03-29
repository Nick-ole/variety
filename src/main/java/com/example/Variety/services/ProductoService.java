package com.example.Variety.services;

import com.example.Variety.models.Producto;
import com.example.Variety.repository.ProductoRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service // Lógica de negocio para productos
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository; // Acceso a BD

    @PostConstruct // Se ejecuta al iniciar la aplicación
    public void initProductos() {
        if (productoRepository.count() == 0) {
            // Datos iniciales para categorías
            // Mujer
            productoRepository.save(new Producto("Blusa elegante", 25.00, "https://img.kwcdn.com/product/Fancyalgo/VirtualModelMatting/b98403796c6e067c0ecd3b4db17bfd17.jpg", "mujer"));
            productoRepository.save(new Producto("Pantalón skinny", 35.00, "https://www.abc.cl/dw/image/v2/BCPP_PRD/on/demandware.static/-/Sites-master-catalog/default/dw457cbb66/images/large/547513-azul.jpg", "mujer"));
            productoRepository.save(new Producto("Zapatillas urbanas", 40.00, "https://media.falabella.com/falabellaPE/126641018_01/w=800,h=800,fit=pad", "mujer"));

            // Hombre
            productoRepository.save(new Producto("Pantalón jean", 35.00, "https://www.lanumero1.com.pe/cdn/shop/products/157214157233_2_1024x.jpg?v=1670428509", "hombre"));
            productoRepository.save(new Producto("Polera deportiva", 28.00, "https://oechsle.vteximg.com.br/arquivos/ids/20487986-800-800/2819329.jpg", "hombre"));
            productoRepository.save(new Producto("Abrigo de invierno", 60.00, "https://media.falabella.com/falabellaPE/120195680_01/w=1500,h=1500,fit=pad", "hombre"));

            // Novedades
            productoRepository.save(new Producto("Vestido Floral Nuevo", 42.00, "https://shop.mango.com/assets/rcs/pics/static/T8/fotos/S/87027183_99.jpg", "novedades"));
            productoRepository.save(new Producto("Zapatillas Sport", 55.00, "https://plazavea.vteximg.com.br/arquivos/ids/27338478-418-418/null.jpg", "novedades"));
            productoRepository.save(new Producto("Bolso de Temporada", 39.00, "https://cdn.shopify.com/s/files/1/0749/9146/8831/files/290221_11BS1BLA_2_480x480.jpg?v=1713292443", "novedades"));

            // Ofertas
            productoRepository.save(new Producto("Vestido Casual", 29.00, "https://m.media-amazon.com/images/I/717jrKFh9yL._UY1000_.jpg", "ofertas"));
            productoRepository.save(new Producto("Sombrero de Playa", 15.00, "https://img.kwcdn.com/product/fancy/e8551012-f347-4661-a668-364c9f019f33.jpg", "ofertas"));
            productoRepository.save(new Producto("Perfume Deluxe", 48.00, "https://http2.mlstatic.com/D_NQ_NP_847863-MLU72566187968_112023-O.webp", "ofertas"));
        }
    }

    // Listar productos por categoría
    public List<Producto> listarPorCategoria(String categoria) {
        return productoRepository.findByCategoria(categoria);
    }

    // Listar todos los productos
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    // Buscar productos por nombre
    public List<Producto> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return listarTodos();
        }
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    // Guardar o actualizar un producto
    public void guardar(Producto producto) {
        if (producto != null) {
            productoRepository.save(producto);
        }
    }

    // Eliminar por ID
    public void eliminar(Long id) {
        if (id != null) {
            productoRepository.deleteById(id);
        }
    }

    // Buscar por ID
    public Producto buscarPorId(Long id) {
        if (id == null) return null;
        return productoRepository.findById(id).orElse(null);
    }
}
