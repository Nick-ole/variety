package com.example.Variety.controllers;

import com.example.Variety.models.CarritoItem;
import com.example.Variety.models.Pedido;
import com.example.Variety.models.Producto;
import com.example.Variety.models.Usuario;
import com.example.Variety.services.CarritoService;
import com.example.Variety.services.PedidoService;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/pedido")
public class PedidoController {

    // Servicios necesarios:
    // CarritoService - Obtener productos del carrito
    // PedidoService - Guardar y consultar pedidos en la BD
    private final CarritoService carritoService;
    private final PedidoService pedidoService;

    public PedidoController(CarritoService carritoService, PedidoService pedidoService) {
        this.carritoService = carritoService;
        this.pedidoService = pedidoService;
    }
    
    // CLIENTE - MOSTRAR RESUMEN DEL PEDIDO
    @GetMapping
    public String mostrarPedido(Model model, HttpSession session) {

        // Obtener carrito actual desde la sesión
        List<CarritoItem> carrito = carritoService.obtenerCarrito(session);

        // Calcular total del carrito
        double total = carrito.stream()
                .mapToDouble(item -> item.getProducto().getPrecio() * item.getCantidad())
                .sum();

        // Enviar datos al frontend
        model.addAttribute("carrito", carrito);
        model.addAttribute("total", total);

        // Mostrar nombre del usuario en el header
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        model.addAttribute("usuarioLogueado", usuario);

        return "pedido";
    }

    // CLIENTE — CONFIRMAR PEDIDO
    @PostMapping("/confirmar")
    public String confirmarPedido(@RequestParam String nombre,
                                  @RequestParam String direccion,
                                  @RequestParam String telefono,
                                  @RequestParam String metodoPago,
                                  HttpSession session) {

        // Obtener productos del carrito
        List<CarritoItem> carrito = carritoService.obtenerCarrito(session);

        // Validar carrito vacío
        if (carrito.isEmpty()) {
            return "redirect:/pedido?error=carrito_vacio";
        }

        // Validar usuario logueado
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        // CREACIÓN DEL PEDIDO

        Pedido pedido = new Pedido();

        pedido.setNombreCliente(nombre);
        pedido.setDireccion(direccion);
        pedido.setTelefono(telefono);
        pedido.setMetodoPago(metodoPago);
        pedido.setEstado("Pendiente"); // estado inicial
        pedido.setUsuario(usuario);    // relación con usuario

        // Calcular total
        pedido.setTotal(carrito.stream()
                .mapToDouble(item -> item.getProducto().getPrecio() * item.getCantidad())
                .sum());

        // Convertir CarritoItem → lista de Productos repetidos
        List<Producto> productos = carrito.stream()
                .flatMap(item -> java.util.Collections
                        .nCopies(item.getCantidad(), item.getProducto())
                        .stream())
                .collect(Collectors.toList());

        pedido.setProductos(productos);

        // Guardar en BD
        pedidoService.guardar(pedido);

        // Vaciar carrito
        carritoService.vaciarCarrito(session);

        // Redirigir a la pantalla de confirmación
        return "redirect:/pedido/confirmado?id=" + pedido.getId();
    }

    // CLIENTE — VER PEDIDO CONFIRMADO
    @GetMapping("/confirmado")
    public String verPedidoConfirmado(@RequestParam Long id, Model model) {

        // Buscar pedido en BD
        Pedido pedido = pedidoService.buscarPorId(id);

        if (pedido == null) {
            // Si no existe el ID - evitar error 500
            return "redirect:/";
        }

        // En caso de pedidos sin productos
        if (pedido.getProductos() == null) {
            pedido.setProductos(new ArrayList<>());
        }

        // Agrupar productos por ID
        Map<Long, List<Producto>> productosAgrupados =
                pedido.getProductos().stream()
                        .collect(Collectors.groupingBy(Producto::getId));

        // Enviar a la vista
        model.addAttribute("pedido", pedido);
        model.addAttribute("productosAgrupados", productosAgrupados);

        return "pedido_confirmado";
    }
}
