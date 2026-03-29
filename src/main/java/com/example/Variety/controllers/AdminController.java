package com.example.Variety.controllers;

import com.example.Variety.models.Pedido;
import com.example.Variety.models.Producto;
import com.example.Variety.models.Usuario;
import com.example.Variety.services.PedidoService;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class AdminController {

    // Servicio encargado de gestionar pedidos en la base de datos
    private final PedidoService pedidoService;

    public AdminController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // MÉTODO UTILITARIO: agregarUsuario()
    
    private void agregarUsuario(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        model.addAttribute("usuario", usuario != null ? usuario.getNombre() : null);
    }

    // VISTA PRINCIPAL DEL DASHBOARD DEL ADMINISTRADOR
    
    @GetMapping("/admin/dashboard")
    public String dashboard(Model model, HttpSession session) {

        // Agrega nombre del usuario al header
        agregarUsuario(model, session);

        return "admin/dashboard";
    }

    // LISTAR + FILTRAR PEDIDOS

    @GetMapping("/admin/pedidos")
    public String verPedidosAdmin(@RequestParam(required = false) String estado,
                                  @RequestParam(required = false) String busqueda,
                                  Model model, HttpSession session) {

        // 1. Obtener todos los pedidos desde la BD
        List<Pedido> pedidos = pedidoService.listarTodos();

        // 2. FILTRO POR ESTADO
        // Si el administrador selecciona un estado, filtra la lista.
        if (estado != null && !estado.isBlank()) {
            pedidos = pedidos.stream()
                    .filter(p -> p.getEstado().equalsIgnoreCase(estado))
                    .toList();
        }

        // 3. FILTRO DE BÚSQUEDA
        // Busca en nombre del cliente o teléfono.
        if (busqueda != null && !busqueda.isBlank()) {
            String texto = busqueda.toLowerCase();
            pedidos = pedidos.stream()
                    .filter(p -> p.getNombreCliente().toLowerCase().contains(texto)
                            || p.getTelefono().toLowerCase().contains(texto))
                    .toList();
        }

        // 4. Pasar datos al frontend
        model.addAttribute("pedidos", pedidos);

        // Guarda los parámetros actuales para mantener sus valores en el formulario
        model.addAttribute("param", Map.of(
                "estado", estado == null ? "" : estado,
                "busqueda", busqueda == null ? "" : busqueda
        ));

        // Mostrar nombre del usuario en el encabezado
        agregarUsuario(model, session);

        return "admin/pedidos";
    }

    // CAMBIAR ESTADO DE UN PEDIDO
    
    @PostMapping("/admin/pedidos/{id}/estado")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam String estado) {

        // Buscar pedido por ID
        Pedido pedido = pedidoService.buscarPorId(id);

        // Si existe, actualizarlo y guardarlo
        if (pedido != null) {
            pedido.setEstado(estado);
            pedidoService.guardar(pedido);
        }

        return "redirect:/admin/pedidos";
    }

    // ELIMINAR PEDIDO
    
    @PostMapping("/admin/pedidos/{id}/eliminar")
    public String eliminarPedido(@PathVariable Long id) {

        // Delegamos la eliminación al servicio
        pedidoService.eliminar(id);

        return "redirect:/admin/pedidos";
    }

    // API — OBTENER PRODUCTOS DE UN PEDIDO (AJAX)
    
    @GetMapping("/api/pedidos/{id}/productos")
    @ResponseBody
    public List<Producto> obtenerProductosPedido(@PathVariable Long id) {

        Pedido pedido = pedidoService.buscarPorId(id);

        // Si no hay pedido o no tiene productos, devolver lista vacía
        if (pedido == null || pedido.getProductos() == null) {
            return new ArrayList<>();
        }

        return pedido.getProductos();
    }
}