package com.example.Variety.repository;

import com.example.Variety.models.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository // Repositorio JPA para la entidad Producto
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Busca productos por categoría exacta
    List<Producto> findByCategoria(String categoria);

    // Busca por coincidencia parcial en el nombre (ignoreCase)
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
}
