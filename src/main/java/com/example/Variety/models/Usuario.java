package com.example.Variety.models;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID autoincremental
    private Long id;

    private String nombre;
    private String apellido;
    private String correo;
    private String usuario;      // username para login
    private String password;     // contraseña encriptada
    private String rol = "CLIENTE"; // rol por defecto
    private boolean activo = true;  // habilitado/deshabilitado

    // Relación: un usuario tiene muchos pedidos
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pedido> pedidos = new ArrayList<>();

    // Productos marcados como favoritos por el usuario
    @ManyToMany
    @JoinTable(name = "usuario_favoritos",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "producto_id"))
    private List<Producto> favoritos = new ArrayList<>();

    public Usuario() {} // requerido por JPA

    public Usuario(String nombre, String apellido, String correo,
                   String usuario, String password, String rol) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.usuario = usuario;
        this.password = password;
        this.rol = rol;
        this.activo = true;
    }

    // UserDetails: autoridad del usuario
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.rol.toUpperCase()));
    }

    // UserDetails: credenciales y estado
    @Override
    public String getPassword() { return this.password; }

    @Override
    public String getUsername() { return this.usuario; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return this.activo; }

    // Getters y Setters 
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public void setPassword(String password) { this.password = password; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public List<Pedido> getPedidos() { return pedidos; }
    public void setPedidos(List<Pedido> pedidos) { this.pedidos = pedidos; }
    public List<Producto> getFavoritos() { return favoritos; }
    public void setFavoritos(List<Producto> favoritos) { this.favoritos = favoritos; }
}
