package com.example.Variety.controllers;

import com.example.Variety.models.Usuario;
import com.example.Variety.services.UsuarioService;

import jakarta.servlet.http.HttpSession;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class UsuarioAdvice {

    // Servicio que permite buscar y obtener información del usuario.
    @Autowired
    private UsuarioService usuarioService;

    // Atributo global: usuarioLogueado
    @ModelAttribute("usuarioLogueado")
    public Usuario usuario(HttpSession session) {

        // Intentar obtener usuario desde la sesión
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");

        // Si no está en sesión → intentar recuperarlo desde seguridad
        if (u == null) {

            // Obtener autenticación actual del contexto de Spring
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            // Verificar si está autenticado y no es anónimo
            if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {

                Object principal = auth.getPrincipal();
                String username = null;

                // Si principal es un UserDetails → obtener username
                if (principal instanceof UserDetails) {
                    username = ((UserDetails) principal).getUsername();

                // Si el principal es un String (caso raro) → usarlo directamente
                } else if (principal instanceof String) {
                    username = (String) principal;
                }

                // Si obtuvimos el username, buscamos al usuario en BD
                if (username != null) {
                    Usuario found = usuarioService.buscarPorUsuario(username);

                    // Si existe, lo guardamos en sesión para futuros usos
                    if (found != null) {
                        session.setAttribute("usuarioLogueado", found);
                        return found;
                    }
                }
            }
        }

        // Si no se encontró en la sesión ni en Spring - devolver null
        return u;
    }

    // Atributo global: usuario - Este método solo devuelve el nombre del usuario logueado, para mostrarlo en el header
    @ModelAttribute("usuario")
    public String nombreUsuario(HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        return u != null ? u.getNombre() : null;
    }
    
    // Atributo global: cartSize - Devuelve el número de productos en el carrito
    // Esto permite mostrar el ícono del carrito actualizado en todas las vistas
    @ModelAttribute("cartSize")
    public Integer cartSize(HttpSession session) {
        List<?> carrito = (List<?>) session.getAttribute("carrito");
        return carrito == null ? 0 : carrito.size();
    }
}
