package com.example.Variety.util;

import com.example.Variety.models.Usuario;
import com.example.Variety.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration // Clase de configuración de Spring
public class DataLoader {

    @Autowired
    private PasswordEncoder passwordEncoder; // Para encriptar la contraseña del admin

    @Bean
    public CommandLineRunner init(UsuarioService usuarioService) {
        return args -> {

            // Crear usuario admin solo si no existe
            if (!usuarioService.existeUsuario("admin")) {

                Usuario admin = new Usuario(
                        "Admin",               // nombre
                        "General",             // apellido
                        "admin@variety.com",   // correo
                        "admin",               // username
                        passwordEncoder.encode("1234"), // contraseña encriptada
                        "ADMIN"                // rol
                );

                usuarioService.guardar(admin); // Guardar admin en BD
                System.out.println("Admin creado"); // Log en consola
            }
        };
    }
}
