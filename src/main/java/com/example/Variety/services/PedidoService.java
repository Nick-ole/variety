package com.example.Variety.services;

import com.example.Variety.models.Pedido;
import com.example.Variety.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service // Servicio que maneja operaciones de pedidos
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository; // Acceso a la BD

    // Guardar o actualizar un pedido
    public void guardar(Pedido pedido) {
        if (pedido != null) {
            pedidoRepository.save(pedido);
        }
    }

    // Obtener todos los pedidos
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    // Buscar pedido por ID
    public Pedido buscarPorId(Long id) {
        if (id == null) return null;
        return pedidoRepository.findById(id).orElse(null);
    }

    // Cambiar estado del pedido (Ej: Pendiente - Enviado)
    public void cambiarEstado(Long id, String nuevoEstado) {
        if (id == null) return;
        Pedido pedido = pedidoRepository.findById(id).orElse(null);
        if (pedido != null) {
            pedido.setEstado(nuevoEstado);
            pedidoRepository.save(pedido);
        }
    }

    // Eliminar un pedido solo si existe
    public void eliminar(Long id) {
        if (id != null && pedidoRepository.existsById(id)) {
            pedidoRepository.deleteById(id);
        }
    }
}
