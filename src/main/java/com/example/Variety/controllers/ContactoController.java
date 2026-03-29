package com.example.Variety.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ContactoController {

    @PostMapping("/contacto")
    public String enviarContacto(@RequestParam String nombre,
                                 @RequestParam String correo,
                                 @RequestParam String mensaje,
                                 Model model) {
        
        System.out.println("Nuevo mensaje:");
        System.out.println("Nombre: " + nombre);
        System.out.println("Correo: " + correo);
        System.out.println("Mensaje: " + mensaje);

        // Mostrar confirmación en la misma página
        model.addAttribute("exito", "Gracias por contactarnos, " + nombre + ". Te responderemos pronto.");
        return "index"; // vuelve al index mostrando el mensaje
    }
}