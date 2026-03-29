package com.example.Variety.repository;

import com.example.Variety.models.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Indica que es un repositorio JPA
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    // Hereda métodos CRUD: findAll, save, deleteById, findById, etc.
}
