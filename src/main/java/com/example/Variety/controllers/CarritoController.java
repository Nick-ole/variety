package com.example.Variety.controllers;

import com.example.Variety.models.Producto;
import com.example.Variety.services.CarritoService;
import com.example.Variety.repository.ProductoRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/carrito")
public class CarritoController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CarritoService carritoService;

    // MÉTODO UTILITARIO: buildCartResponse()
    private Map<String, Object> buildCartResponse(HttpSession session) {

        // Obtiene el carrito desde la sesión mediante CarritoService
        List<com.example.Variety.models.CarritoItem> carrito = carritoService.obtenerCarrito(session);

        List<Map<String, Object>> items = new ArrayList<>();
        double total = 0;

        // Recorre todos los productos del carrito
        for (var item : carrito) {
            Map<String, Object> m = new HashMap<>();
            m.put("productoId", item.getProducto().getId());
            m.put("nombre", item.getProducto().getNombre());
            m.put("precio", item.getProducto().getPrecio());
            m.put("imagen", item.getProducto().getImagen());
            m.put("cantidad", item.getCantidad());
            items.add(m);

            // Acumula total del carrito
            total += item.getProducto().getPrecio() * item.getCantidad();
        }

        // Arma respuesta final
        Map<String, Object> resp = new HashMap<>();
        resp.put("items", items);
        resp.put("total", total);
        resp.put("count", items.size()); // número total de productos (tipos)

        return resp;
    }

    // AGREGAR PRODUCTO AL CARRITO
    @PostMapping("/api/agregar/{id}")
    @ResponseBody
    public Map<String, Object> apiAgregar(@PathVariable(required = true) Long id, HttpSession session) {

        if (id != null) {
            Producto producto = productoRepository.findById(id).orElse(null);
            if (producto != null) {
                carritoService.agregarProducto(session, producto);
            }
        }

        // Devuelve la respuesta estándar en JSON
        return buildCartResponse(session);
    }

    // SUMAR CANTIDAD DE UN PRODUCTO - Incrementa la cantidad de un producto dentro del carrito
    @PostMapping("/api/sumar/{id}")
    @ResponseBody
    public Map<String, Object> apiSumar(@PathVariable Long id, HttpSession session) {

        carritoService.incrementarPorId(session, id);

        return buildCartResponse(session);
    }

    // RESTAR CANTIDAD DE UN PRODUCTO - Si baja a 0, CarritoService decidirá si elimina el producto
    @PostMapping("/api/restar/{id}")
    @ResponseBody
    public Map<String, Object> apiRestar(@PathVariable Long id, HttpSession session) {

        carritoService.disminuirPorId(session, id);

        return buildCartResponse(session);
    }
    
    // ELIMINAR PRODUCTO DEL CARRITO - Elimina un producto completamente
    @PostMapping("/api/eliminar/{id}")
    @ResponseBody
    public Map<String, Object> apiEliminar(@PathVariable Long id, HttpSession session) {

        carritoService.eliminarPorId(session, id);

        return buildCartResponse(session);
    }

    // VACIAR CARRITO COMPLETO - Limpia completamente la lista de productos en sesión
    @PostMapping("/api/vaciar")
    @ResponseBody
    public Map<String, Object> apiVaciar(HttpSession session) {

        carritoService.vaciarCarrito(session);

        return buildCartResponse(session);
    }

    // OBTENER ESTADO DEL CARRITO - Este endpoint se usa cuando la página carga para sincronizar
    // el carrito si el usuario ya tenía productos guardados en sesión
    @GetMapping("/api/estado")
    @ResponseBody
    public Map<String, Object> apiEstado(HttpSession session) {
        return buildCartResponse(session);
    }
}
