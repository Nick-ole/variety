package com.example.Variety.jwt;

import com.example.Variety.services.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.logging.Logger;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    // Logger para registrar eventos importantes del filtro
    private static final Logger logger = Logger.getLogger(JwtRequestFilter.class.getName());

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;

    // Se inyectan servicios necesarios para validar el token y cargar usuario.
    public JwtRequestFilter(UsuarioService usuarioService, JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
    }

    // Este método se ejecuta una vez por cada request HTTP
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {

        String jwt = null;       // token detectado
        String username = null;  // usuario extraído del token

        try {
            
            // 1. LECTURA DEL JWT DESDE LA COOKIE "jwt-token"
            Cookie jwtCookie = WebUtils.getCookie(request, "jwt-token");

            if (jwtCookie != null && jwtCookie.getValue() != null && !jwtCookie.getValue().isEmpty()) {
                jwt = jwtCookie.getValue();

                logger.fine("JWT encontrado para la ruta: " + request.getRequestURI());

                try {
                    
                    // 2. EXTRAER EL USUARIO (subject) DEL TOKEN
                    username = jwtUtil.extractUsername(jwt);

                    // Cargar detalles del usuario para validar el token
                    UserDetails userDetails = usuarioService.loadUserByUsername(username);
                    
                    // 3. VALIDAR TOKEN (firma, expiración, identidad)
                    if (jwtUtil.validateToken(jwt, userDetails)) {

                        logger.fine("✓ JWT válido para usuario: " + username);

                        // Crear autenticación manualmente para Spring Security
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        // Guardamos autenticación en el contexto
                        // Esto reemplaza al login tradicional
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                    } else {

                        // Token inválido o expirado → limpiar sesión
                        logger.warning("✗ JWT inválido o expirado para usuario: " + username);

                        SecurityContextHolder.clearContext();

                        try {
                            var session = request.getSession(false);
                            if (session != null) {
                                logger.info("Sesión invalidada debido a JWT inválido");
                                session.invalidate();
                            }
                        } catch (Exception e) {
                            logger.warning("Error invalidando sesión: " + e.getMessage());
                        }
                    }

                } catch (Exception e) {

                    // Error procesando token (firma manipulada, formato inválido, etc)
                    logger.warning("✗ Error procesando JWT: " + e.getMessage());

                    SecurityContextHolder.clearContext();

                    try {
                        var session = request.getSession(false);
                        if (session != null) {
                            logger.info("Sesión invalidada por error al procesar JWT");
                            session.invalidate();
                        }
                    } catch (Exception ex) {
                        logger.warning("Error invalidando sesión: " + ex.getMessage());
                    }
                }
            }
            // Si no hay cookie JWT - simplemente no se autentica aquí.
            // La sesión normal (HttpSession) seguirá funcionando si existe.

        } catch (Exception e) {

            // Error inesperado del filtro (raro pero posible)
            logger.severe("Error crítico en JwtRequestFilter: " + e.getMessage());
        }

        // Continuamos con los demás filtros o el controlador destino.
        chain.doFilter(request, response);
    }
}
