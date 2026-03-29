package com.example.Variety.controllers;

import com.example.Variety.models.CarritoItem;
import com.example.Variety.services.CarritoService;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class CarritoAdvice {

    private final CarritoService carritoService;

    public CarritoAdvice(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    // AGREGA AUTOMÁTICAMENTE EL CARRITO A TODAS LAS VISTAS
    // Cada vez que se renderiza una vista, este método añade al modelo el carrito obtenido de la sesión.
    @ModelAttribute("carrito")
    public List<CarritoItem> carrito(HttpSession session) {

        // Devuelve la lista de ítems del carrito desde la sesión
        // Si no existe carrito, lo crea automáticamente
        return carritoService.obtenerCarrito(session);
    }

    // AGREGA EL TOTAL DEL CARRITO A TODAS LAS VISTAS
    // De esta forma, todas las pantallas pueden mostrar el total sin tener que calcularlo manualmente en cada controlador
    @ModelAttribute("total")
    public double total(HttpSession session) {

        return carritoService.obtenerCarrito(session)
                .stream()
                // precio * cantidad por cada ítem
                .mapToDouble(item -> item.getProducto().getPrecio() * item.getCantidad())
                .sum();
    }
}