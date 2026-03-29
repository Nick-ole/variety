package com.example.Variety.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {

    // ID AUTOINCREMENTAL
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Datos del cliente que realizó el pedido
    private String nombreCliente;
    private String direccion;
    private String telefono;
    private String metodoPago;

    // Total del pedido calculado a partir del carrito
    private double total;

    // Estado del pedido (Pendiente, Enviado, Entregado, etc.)
    @Column(nullable = false)
    private String estado = "Pendiente";

    // RELACIÓN: MUCHOS PEDIDOS → UN USUARIO
    // Un usuario puede hacer muchos pedidos.
    // fetch = LAZY evita cargar todo el usuario completo a menos que se necesite.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // RELACIÓN: MUCHOS PEDIDOS ↔ MUCHOS PRODUCTOS
    @ManyToMany
    @JoinTable(
        name = "pedido_productos",
        joinColumns = @JoinColumn(name = "pedido_id"),
        inverseJoinColumns = @JoinColumn(name = "producto_id")
    )
    private List<Producto> productos = new ArrayList<>();

    // CONSTRUCTOR vacío (obligatorio para Hibernate/JPA)
    public Pedido() {}

    // Constructor auxiliar para crear pedidos desde el servicio o controlador
    public Pedido(String nombreCliente, String direccion, String telefono,
                  String metodoPago, double total) {
        this.nombreCliente = nombreCliente;
        this.direccion = direccion;
        this.telefono = telefono;
        this.metodoPago = metodoPago;
        this.total = total;
        this.estado = "Pendiente"; // estado inicial por defecto
    }

    // GETTERS Y SETTERS — Acceso estándar a los atributos
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public List<Producto> getProductos() { return productos; }
    public void setProductos(List<Producto> productos) { this.productos = productos; }

    // Método auxiliar para agregar un producto al pedido
    public void agregarProducto(Producto producto) {
        this.productos.add(producto);
    }
}
