package com.example.Variety.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID autoincremental
    private Long id;

    private String nombre;
    private double precio;
    private String imagen;
    private String categoria;

    // Relación inversa: este producto aparece en muchos pedidos
    @ManyToMany(mappedBy = "productos")
    private List<Pedido> pedidos = new ArrayList<>();

    public Producto() {} // Constructor vacío requerido por JPA

    public Producto(String nombre, double precio, String imagen, String categoria) {
        this.nombre = nombre;
        this.precio = precio;
        this.imagen = imagen;
        this.categoria = categoria;
    }

    // Getters y Setters 
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public List<Pedido> getPedidos() { return pedidos; }
    public void setPedidos(List<Pedido> pedidos) { this.pedidos = pedidos; }

    @Override
    public String toString() { // Representación en vistas admin
        return nombre + " — S/ " + precio;
    }
}
