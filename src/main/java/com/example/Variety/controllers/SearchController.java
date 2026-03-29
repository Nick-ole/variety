package com.example.Variety.controllers;

import com.example.Variety.models.Producto;
import com.example.Variety.services.ProductoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class SearchController {

    // Servicio que se encarga de buscar productos en la BD
    @Autowired
    private ProductoService productoService;

    // Método utilitario para agregar el usuario al header
    private void agregarUsuario(Model model, HttpSession session) {
        Object usuarioObj = session.getAttribute("usuarioLogueado");

        if (usuarioObj != null) {
            try {
                // Si el objeto se puede convertir a String, lo usamos
                model.addAttribute("usuario", usuarioObj.toString());
            } catch (Exception e) {
                model.addAttribute("usuario", null);
            }
        } else {
            model.addAttribute("usuario", null);
        }
    }

    // BUSCADOR PRINCIPAL (vista /buscar) - Este endpoint muestra la página de resultados de búsqueda.
    // Recibe un parámetro query, lo envía al servicio y muestra la lista
    @GetMapping("/buscar")
    public String buscar(@RequestParam(name = "q", required = false) String q,
                         Model model,
                         HttpSession session) {

        // Agregar el usuario para el header
        agregarUsuario(model, session);

        // Buscar productos cuyo nombre coincida
        model.addAttribute("productos", productoService.buscarPorNombre(q));

        // Mantener el texto escrito en el buscador
        model.addAttribute("q", q);

        return "buscar";
    }

    // AUTOCOMPLETE — versión simple - Devuelve solo los nombres de productos que coinciden con lo buscado.
    // Se usa para el autocompletado en JS (autocomplete.js)
    @GetMapping("/api/autocomplete")
    @ResponseBody
    public List<String> autocomplete(@RequestParam(name = "q", required = false) String q) {

        // Evitar búsquedas vacías
        if (q == null || q.isBlank()) {
            return Collections.emptyList();
        }

        // Buscar coincidencias
        List<Producto> encontrados = productoService.buscarPorNombre(q);

        // Extraer solo nombres
        return encontrados.stream()
                .map(Producto::getNombre)
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
    }

    // AUTOCOMPLETE — versión enriquecida - Devuelve un objeto JSON con más información por cada producto:
    // - id
    // - nombre
    // - precio
    @GetMapping("/api/autocomplete-items")
    @ResponseBody
    public List<Map<String, Object>> autocompleteItems(@RequestParam(name = "q", required = false) String q) {

        if (q == null || q.isBlank()) {
            return Collections.emptyList();
        }

        List<Producto> encontrados = productoService.buscarPorNombre(q);

        // Convertir cada producto en un objeto JSON ligero
        return encontrados.stream()
            .map(p -> {
                Map<String, Object> m = new java.util.HashMap<>();
                m.put("id", p.getId());
                m.put("nombre", p.getNombre());
                m.put("precio", p.getPrecio());
                return m;
            })
            .distinct()
            .limit(10)
            .collect(Collectors.toList());
    }
}
