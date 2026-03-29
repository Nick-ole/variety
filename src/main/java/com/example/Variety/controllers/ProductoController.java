package com.example.Variety.controllers;

import com.example.Variety.models.Producto;
import com.example.Variety.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/productos")
public class ProductoController {

    // Servicio encargado de gestionar productos (guardar, listar, eliminar, etc.)
    @Autowired
    private ProductoService productoService;

    // ADMIN — LISTAR PRODUCTOS - Endpoint principal del administrador para visualizar todos los productos
    @GetMapping
    public String listarProductos(@RequestParam(required = false) String busqueda,
                                  Model model) {

        // Obtener todos los productos de BD
        List<Producto> productos = productoService.listarTodos();

        // Filtro por nombre si el parámetro de búsqueda no está vacío
        if (busqueda != null && !busqueda.isEmpty()) {
            productos = productos.stream()
                    .filter(p -> p.getNombre().toLowerCase().contains(busqueda.toLowerCase()))
                    .toList();
        }

        // Enviar lista al modelo para la vista admin/productos.html
        model.addAttribute("productos", productos);

        return "admin/productos";
    }
    
    // ADMIN — GUARDAR PRODUCTO (CREAR / EDITAR) - Recibe automáticamente los campos del formulario y los convierte en un objeto Producto
    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute Producto producto) {

        // Guardar o actualizar producto en la base de datos
        productoService.guardar(producto);

        // Redirigir nuevamente a la tabla de productos
        return "redirect:/admin/productos";
    }

    // ADMIN — ELIMINAR PRODUCTO - Permite eliminar un producto según su ID
    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id) {

        // Invoca al servicio para eliminar el producto si existe
        productoService.eliminar(id);

        return "redirect:/admin/productos";
    }
}
