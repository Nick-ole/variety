package com.example.Variety.controllers;

import com.example.Variety.models.Usuario;
import com.example.Variety.services.ProductoService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // Servicio para obtener productos desde la base de datos
    @Autowired
    private ProductoService productoService;

    // MÉTODO UTILITARIO: agregarUsuario()
    // Todas las vistas públicas (index, mujer, hombre, etc.) muestran el nombre del usuario si está logueado
    private void agregarUsuario(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        model.addAttribute("usuario", usuario != null ? usuario.getNombre() : null);
    }

    // VISTA PRINCIPAL ("/")
    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        agregarUsuario(model, session);
        return "index";
    }

    // CATEGORÍA: MUJER - Obtiene productos filtrados por categoría y los envía a la vista mujer.html
    @GetMapping("/mujer")
    public String mujer(Model model, HttpSession session) {

        agregarUsuario(model, session);

        // Productos filtrados por la categoría "mujer"
        model.addAttribute("productos", productoService.listarPorCategoria("mujer"));

        return "mujer";
    }

    // CATEGORÍA: HOMBRE - Filtrando por "hombre"
    @GetMapping("/hombre")
    public String hombre(Model model, HttpSession session) {

        agregarUsuario(model, session);

        model.addAttribute("productos", productoService.listarPorCategoria("hombre"));

        return "hombre";
    }

    // CATEGORÍA: NOVEDADES - Se muestran productos de la categoría "novedades"
    @GetMapping("/novedades")
    public String novedades(Model model, HttpSession session) {

        agregarUsuario(model, session);

        model.addAttribute("productos", productoService.listarPorCategoria("novedades"));

        return "novedades";
    }

    // CATEGORÍA: OFERTAS - Muestra productos pertenecientes a la categoría "ofertas"
    @GetMapping("/ofertas")
    public String ofertas(Model model, HttpSession session) {

        agregarUsuario(model, session);

        model.addAttribute("productos", productoService.listarPorCategoria("ofertas"));

        return "ofertas";
    }
}
