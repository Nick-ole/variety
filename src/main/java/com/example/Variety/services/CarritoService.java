package com.example.Variety.services;

import com.example.Variety.models.CarritoItem;
import com.example.Variety.models.Producto;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service // Servicio que maneja el carrito en la sesión
public class CarritoService {

    private static final String SESSION_CART = "carrito"; // clave en sesión

    // Obtener carrito desde la sesión (crear si no existe)
    @SuppressWarnings("unchecked")
    public List<CarritoItem> obtenerCarrito(HttpSession session) {
        List<CarritoItem> carrito = (List<CarritoItem>) session.getAttribute(SESSION_CART);

        if (carrito == null) {
            carrito = new ArrayList<>();
            session.setAttribute(SESSION_CART, carrito);
        }

        return carrito;
    }

    // Buscar item por ID de producto
    private CarritoItem buscarItem(List<CarritoItem> carrito, Long productId) {
        for (CarritoItem item : carrito) {
            if (item.getProducto().getId().equals(productId)) {
                return item;
            }
        }
        return null;
    }

    // Agregar producto (o aumentar cantidad si ya existe)
    public void agregarProducto(HttpSession session, Producto producto) {
        List<CarritoItem> carrito = obtenerCarrito(session);

        CarritoItem item = buscarItem(carrito, producto.getId());

        if (item != null) {
            item.incrementar(); // ya existe - suma 1
        } else {
            carrito.add(new CarritoItem(producto, 1)); // nuevo item
        }
    }

    // Incrementar cantidad por ID
    public void incrementarPorId(HttpSession session, Long productId) {
        List<CarritoItem> carrito = obtenerCarrito(session);

        CarritoItem item = buscarItem(carrito, productId);
        if (item != null) {
            item.incrementar();
        }
    }

    // Disminuir cantidad (si llega a 0, se elimina)
    public void disminuirPorId(HttpSession session, Long productId) {
        List<CarritoItem> carrito = obtenerCarrito(session);

        CarritoItem item = buscarItem(carrito, productId);

        if (item != null) {
            item.disminuir();
            if (item.getCantidad() <= 0) {
                carrito.remove(item);
            }
        }
    }

    // Eliminar producto completamente del carrito
    public void eliminarPorId(HttpSession session, Long productId) {
        List<CarritoItem> carrito = obtenerCarrito(session);

        CarritoItem item = buscarItem(carrito, productId);
        if (item != null) {
            carrito.remove(item);
        }
    }

    // Vaciar carrito (borrar atributo de la sesión)
    public void vaciarCarrito(HttpSession session) {
        session.removeAttribute(SESSION_CART);
    }
}
