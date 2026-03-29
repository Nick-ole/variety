package com.example.Variety.controllers;

import com.example.Variety.models.Producto;
import com.example.Variety.models.Usuario;
import com.example.Variety.services.ProductoService;
import com.example.Variety.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class PerfilController {

    // Servicio que gestiona usuarios (consultas, guardar cambios, favoritos, etc.)
    @Autowired
    private UsuarioService usuarioService;

    // Servicio para obtener productos (usado en gestión de favoritos)
    @Autowired
    private ProductoService productoService;

    // Encoder para cifrar contraseñas cuando el usuario actualiza su password
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // VISTA DEL PERFIL - Muestra los datos del usuario logueado
    @GetMapping("/perfil")
    public String verPerfil(Model model, HttpSession session) {

        // Recuperar usuario desde la sesión
        Usuario ses = (Usuario) session.getAttribute("usuarioLogueado");
        if (ses == null) {
            return "redirect:/login"; // protección básica
        }

        // Buscar usuario completo en BD (garantiza datos actualizados)
        Usuario usuario = usuarioService.buscarPorId(ses.getId());

        // Enviar al modelo para usarlo en el formulario de perfil
        model.addAttribute("usuarioObj", usuario);

        // Se agrega el nombre para mostrarlo en el header (menú)
        model.addAttribute("usuario", usuario != null ? usuario.getNombre() : null);

        return "perfil";
    }

    // GUARDAR CAMBIOS DEL PERFIL - Permite modificar nombre, apellido, correo y opcionalmente contraseña
    @PostMapping("/perfil/guardar")
    public String guardarPerfil(@RequestParam Long id,
                                @RequestParam String nombre,
                                @RequestParam String apellido,
                                @RequestParam String correo,
                                @RequestParam(required = false) String password,
                                HttpSession session) {

        // Obtener usuario desde BD
        Usuario usuario = usuarioService.buscarPorId(id);
        if (usuario == null) {
            return "redirect:/perfil";
        }

        // Actualizar datos básicos
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setCorreo(correo);

        // Si el usuario agregó una contraseña nueva - cifrarla
        if (password != null && !password.isBlank()) {
            usuario.setPassword(passwordEncoder.encode(password));
        }

        // Guardar cambios en BD
        usuarioService.guardar(usuario);

        // Actualizar la sesión con la nueva información
        session.setAttribute("usuarioLogueado", usuario);

        return "redirect:/perfil?success=true";
    }

    // VER FAVORITOS - Cada usuario tiene una lista de productos marcados como favoritos
    @GetMapping("/favoritos")
    public String verFavoritos(Model model, HttpSession session) {

        // Validar sesión iniciada
        Usuario ses = (Usuario) session.getAttribute("usuarioLogueado");
        if (ses == null) return "redirect:/login";

        // Recuperar usuario desde BD
        Usuario usuario = usuarioService.buscarPorId(ses.getId());

        // Obtener su lista de productos favoritos
        List<Producto> favoritos = usuario.getFavoritos();

        model.addAttribute("favoritos", favoritos);

        // Agregar nombre al header
        model.addAttribute("usuario", usuario != null ? usuario.getNombre() : null);

        return "favoritos";
    }

    // AGREGAR FAVORITO - Se agrega un producto a la lista de favoritos del usuario
    @PostMapping("/favoritos/agregar/{id}")
    public String agregarFavorito(@PathVariable Long id, HttpSession session) {

        // Validar sesión
        Usuario ses = (Usuario) session.getAttribute("usuarioLogueado");
        if (ses == null) return "redirect:/login";

        Usuario usuario = usuarioService.buscarPorId(ses.getId());
        Producto p = productoService.buscarPorId(id);

        // Solo agregar si existe y no está repetido
        if (p != null && !usuario.getFavoritos().contains(p)) {
            usuario.getFavoritos().add(p);
            usuarioService.guardar(usuario); // guardar cambios
            session.setAttribute("usuarioLogueado", usuario); // sincronizar sesión
        }

        return "redirect:/favoritos";
    }

    // ELIMINAR FAVORITO - Se elimina el producto cuyo ID coincide dentro de la lista de favoritos
    @PostMapping("/favoritos/eliminar/{id}")
    public String eliminarFavorito(@PathVariable Long id, HttpSession session) {

        Usuario ses = (Usuario) session.getAttribute("usuarioLogueado");
        if (ses == null) return "redirect:/login";

        Usuario usuario = usuarioService.buscarPorId(ses.getId());

        // Remover producto usando filter
        usuario.getFavoritos().removeIf(prod -> prod.getId().equals(id));

        usuarioService.guardar(usuario);
        session.setAttribute("usuarioLogueado", usuario); // actualizar sesión

        return "redirect:/favoritos";
    }
}
