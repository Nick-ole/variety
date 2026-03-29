package com.example.Variety.services;

import com.example.Variety.models.Usuario;
import com.example.Variety.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User; // Spring Security User
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service // Servicio de usuarios y conexión con Spring Security
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository; // Acceso a BD

    @Autowired
    private PasswordEncoder passwordEncoder; // Para encriptar contraseñas

    @Override
    public UserDetails loadUserByUsername(String usuario) throws UsernameNotFoundException {
        Usuario user = usuarioRepository.findByUsuario(usuario); // Buscar usuario en BD
        if (user == null) {
            throw new UsernameNotFoundException("Usuario no encontrado: " + usuario);
        }

        // Asegurar que el rol tenga formato ROLE_X
        String rolConPrefijo = user.getRol().toUpperCase().startsWith("ROLE_") ?
                user.getRol().toUpperCase() : "ROLE_" + user.getRol().toUpperCase();

        // Convertir Usuario → User (objeto de Spring Security)
        return new User(
                user.getUsuario(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(rolConPrefijo))
        );
    }

    // Verificar si existe un username
    public boolean existeUsuario(String usuario) {
        return usuarioRepository.findByUsuario(usuario) != null;
    }

    // Registrar nuevo usuario con contraseña encriptada
    public Usuario registrar(Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    // Buscar por username
    public Usuario buscarPorUsuario(String usuario) {
        return usuarioRepository.findByUsuario(usuario);
    }

    // Listar todos
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    // Buscar por nombre o rol (panel admin)
    public List<Usuario> buscarPorNombreORol(String busqueda) {
        return usuarioRepository.buscarPorNombreORol(busqueda.toLowerCase());
    }

    // Buscar por ID
    public Usuario buscarPorId(Long id) {
        if (id == null) return null;
        return usuarioRepository.findById(id).orElse(null);
    }

    // Guardar o actualizar
    public void guardar(Usuario usuario) {
        if (usuario != null) {
            usuarioRepository.save(usuario);
        }
    }

    // Eliminar por ID
    public void eliminar(Long id) {
        if (id != null) {
            usuarioRepository.deleteById(id);
        }
    }
}
