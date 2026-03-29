package com.example.Variety.config;

import com.example.Variety.jwt.JwtRequestFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Filtro personalizado que valida el JWT en cada request
    // @Lazy evita problemas de dependencias circulares
    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(@Lazy JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    // BEAN: PasswordEncoder - Se usa para encriptar contraseñas
    // BCrypt es el estándar de seguridad recomendado
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // BEAN: AuthenticationManager
    // Necesario para el proceso de login manual (AuthController)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // CONFIGURACIÓN PRINCIPAL DE SEGURIDAD
    // Aquí se definen:
    //  - rutas públicas
    //  - rutas protegidas
    //  - uso de JWT
    //  - manejo de sesiones
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // Deshabilitamos CSRF porque usamos JWT,
            // aunque las cookies HTTP-Only permiten mantener cierta protección.
            .csrf(csrf -> csrf.disable())

            // AUTORIZACIONES: Qué rutas requieren autenticación y cuáles no
            .authorizeHttpRequests(auth -> auth

                // --- Rutas solo para ADMIN ---
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // --- Rutas que requieren usuario logueado ---
                .requestMatchers("/perfil").authenticated()
                .requestMatchers("/usuario/**").authenticated()

                // --- Carrito: completamente público ---
                .requestMatchers("/carrito/**").permitAll()
                .requestMatchers("/carrito/api/**").permitAll()

                // --- Pedido ---
                .requestMatchers("/pedido").permitAll()  // vista pública
                .requestMatchers("/pedido/confirmar").authenticated() // requiere login

                // --- API pública ---
                .requestMatchers("/api/**").permitAll()

                // --- Páginas públicas ---
                .requestMatchers("/", "/login", "/register", "/logout").permitAll()
                .requestMatchers("/productos/**", "/tienda").permitAll()
                .requestMatchers("/mujer", "/hombre", "/novedades", "/ofertas").permitAll()
                .requestMatchers("/error").permitAll()

                // --- Recursos estáticos ---
                .requestMatchers("/*.js", "/*.css", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/styles.css").permitAll()

                // --- H2 Console (solo desarrollo) ---
                .requestMatchers("/h2-console/**").permitAll()

                // --- Cualquier otra ruta requiere autenticación ---
                .anyRequest().authenticated()
            )

            // MANEJO DE ERRORES DE AUTENTICACIÓN
            // Redirige al login si no hay sesión o token válido
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {

                    // Si es AJAX → devolver JSON (usado en carrito)
                    String requestedWith = request.getHeader("X-Requested-With");
                    if ("XMLHttpRequest".equals(requestedWith)) {
                        response.setStatus(401);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\": \"Unauthorized\"}");
                    } else {
                        // Si es petición normal → enviar al login
                        response.sendRedirect("/login");
                    }
                })
            )

            // SESIÓN: solo se crea si es necesario (IF_REQUIRED)
            // Compatibilidad entre JWT + HttpSession local
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )

            // H2 Console requiere deshabilitar frameOptions
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))

            // Deshabilitamos el login por formulario de Spring
            .formLogin(form -> form.disable());

        // Agregar nuestro filtro JWT ANTES del filtro de autenticación de Spring Security
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
