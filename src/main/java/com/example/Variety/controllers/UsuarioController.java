package com.example.Variety.controllers;

import com.example.Variety.models.Usuario;
import com.example.Variety.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioController(UsuarioService usuarioService, PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
    }
    
    // LISTAR USUARIOS
    @GetMapping
    public String listarUsuarios(@RequestParam(required = false) String busqueda, Model model) {
        List<Usuario> usuarios = (busqueda != null && !busqueda.isBlank())
                ? usuarioService.buscarPorNombreORol(busqueda)
                : usuarioService.listarTodos();

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("busqueda", busqueda == null ? "" : busqueda);
        return "admin/usuarios";
    }

    // GUARDAR USUARIO (CREAR / EDITAR)
    @PostMapping("/guardar")
    public String guardarUsuario(@RequestParam(required = false) Long id,
                                 @RequestParam String nombre,
                                 @RequestParam String correo,
                                 @RequestParam("nombreUsuario") String nombreUsuario,
                                 @RequestParam(required = false) String password,
                                 @RequestParam String rol,
                                 @RequestParam(required = false, defaultValue = "false") boolean activo,
                                 Model model) {

        Usuario usuario = (id != null) ? usuarioService.buscarPorId(id) : new Usuario();

        // Validar nombre de usuario duplicado
        Usuario existente = usuarioService.buscarPorUsuario(nombreUsuario);
        if (existente != null && (id == null || !existente.getId().equals(id))) {
            model.addAttribute("error", "El nombre de usuario ya existe");
            return "admin/usuarios";
        }

        // Asignar datos
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setUsuario(nombreUsuario);
        usuario.setRol(rol.toUpperCase());
        usuario.setActivo(activo);

        // Encriptar contraseña solo si se ingresó nueva
        if (password != null && !password.isBlank()) {
            usuario.setPassword(passwordEncoder.encode(password));
        }

        usuarioService.guardar(usuario);
        return "redirect:/admin/usuarios";
    }

    // ELIMINAR USUARIO
    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return "redirect:/admin/usuarios";
    }

    // EDITAR USUARIO
    @GetMapping("/editar/{id}")
    public String editarUsuario(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id);
        if (usuario == null) {
            return "redirect:/admin/usuarios";
        }
        model.addAttribute("usuario", usuario);
        return "admin/usuario_form";
    }
}