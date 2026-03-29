package com.example.Variety.repository;

import com.example.Variety.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository // Repositorio JPA para Usuario
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar por username (para login)
    Usuario findByUsuario(String usuario);

    // Búsqueda por nombre o rol (convertido a minúsculas)
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.nombre) LIKE %:busqueda% OR LOWER(u.rol) LIKE %:busqueda%")
    List<Usuario> buscarPorNombreORol(@Param("busqueda") String busqueda);
}
