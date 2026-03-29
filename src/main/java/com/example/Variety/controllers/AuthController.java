package com.example.Variety.controllers;

import com.example.Variety.models.Usuario;
import com.example.Variety.services.UsuarioService;
import com.example.Variety.jwt.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/")
public class AuthController {

    // Servicios principales:
    // UsuarioService - manejar usuarios y búsqueda en BD
    // AuthenticationManager - autenticar credenciales con Spring Security
    // JwtUtil - generar y validar tokens JWT
    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(UsuarioService usuarioService,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    // VISTA DE LOGIN
    // Sólo muestra el formulario HTML (login.html)
    @GetMapping("login")
    public String loginView() {
        return "login";
    }

    // PROCESO DE LOGIN
    @PostMapping("login")
    public String login(@RequestParam String usuario,
                        @RequestParam String password,
                        HttpServletRequest request,
                        HttpServletResponse response,
                        HttpSession session,
                        Model model) {
        try {
            // 1. VALIDAR CREDENCIALES
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usuario, password)
            );

            // 2. Buscar usuario en BD (para guardar en sesión)
            Usuario user = usuarioService.buscarPorUsuario(usuario);

            // 3. Cargar UserDetails (necesario para JWT)
            UserDetails userDetails = usuarioService.loadUserByUsername(usuario);

            // 4. Crear autenticación manualmente para Spring Security
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Registramos autenticación en el contexto global
            SecurityContextHolder.getContext().setAuthentication(authToken);

            // 5. Generar JWT
            String jwt = jwtUtil.generateToken(userDetails);

            // 6. Guardarlo en cookie HTTP-Only (seguro y no accesible por JS)
            Cookie jwtCookie = new Cookie("jwt-token", jwt);
            jwtCookie.setHttpOnly(true);  // previene ataques XSS
            jwtCookie.setSecure(false);  // debe ser true si usas HTTPS
            jwtCookie.setMaxAge(2 * 60); // expira en 2 minutos
            jwtCookie.setPath("/");
            response.addCookie(jwtCookie);

            // 7. Configurar HttpSession (solo para datos, no autenticación)
            session.setMaxInactiveInterval(2 * 60); // misma duración que el JWT
            session.setAttribute("usuarioLogueado", user);

            // 8. REDIRECCIÓN SEGÚN ROL
            String rol = user.getRol();
            if (rol != null && (rol.equalsIgnoreCase("ADMIN") || rol.equalsIgnoreCase("ROLE_ADMIN"))) {
                return "redirect:/admin/dashboard";
            }

            return "redirect:/";

        } catch (Exception e) {
            // Error de autenticación: credenciales incorrectas
            model.addAttribute("error", "Usuario o contraseña incorrectos");
            return "login";
        }
    }

    // REGISTRO DE USUARIO

    @PostMapping("register")
    public String register(@RequestParam String nombre,
                           @RequestParam String apellido,
                           @RequestParam String correo,
                           @RequestParam String usuario,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           Model model) {

        // 1. Validar contraseñas
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorRegistro", "Las contraseñas no coinciden");
            return "index";
        }

        // 2. Verificar si el usuario ya existe
        if (usuarioService.existeUsuario(usuario)) {
            model.addAttribute("errorRegistro", "El usuario ya existe");
            return "index";
        }

        // 3. Crear nuevo usuario (rol por defecto: CLIENTE)
        Usuario nuevo = new Usuario(nombre, apellido, correo, usuario, password, "CLIENTE");

        // Registrar en BD (UsuarioService se encarga de encriptar password)
        nuevo = usuarioService.registrar(nuevo);

        // 4. Autenticación automática del nuevo usuario
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        nuevo,
                        null,
                        nuevo.getAuthorities()
                );

        authToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 5. Crear sesión
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(2 * 60); // 2 minutos
        session.setAttribute("usuarioLogueado", nuevo);

        // 6. Generar JWT y guardarlo en cookie
        UserDetails userDetails = usuarioService.loadUserByUsername(usuario);
        String jwt = jwtUtil.generateToken(userDetails);
        Cookie jwtCookie = new Cookie("jwt-token", jwt);

        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setMaxAge(2 * 60);
        jwtCookie.setPath("/");

        response.addCookie(jwtCookie);

        return "redirect:/";
    }

    // LOGOUT
    // Elimina completamente:
    //  la sesión HttpSession
    //  la cookie JWT
    
    @GetMapping("logout")
    public String logout(HttpServletResponse response, HttpSession session) {

        // Borrar sesión
        session.invalidate();

        // Eliminar cookie (maxAge = 0)
        Cookie jwtCookie = new Cookie("jwt-token", null);
        jwtCookie.setMaxAge(0);
        jwtCookie.setPath("/");
        response.addCookie(jwtCookie);

        return "redirect:/login?logout=true";
    }
}