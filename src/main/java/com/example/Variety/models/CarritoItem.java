package com.example.Variety.models;


//Clase CarritoItem - Representa un elemento del carrito de compras.
public class CarritoItem {

    // Producto asociado a este item
    private Producto producto;

    // Cantidad del mismo producto en el carrito
    private int cantidad;

    // Constructor para crear un item del carrito
    public CarritoItem(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }

    // Getter del producto
    public Producto getProducto() {
        return producto;
    }

    // Getter de la cantidad
    public int getCantidad() {
        return cantidad;
    }

    // Incrementa la cantidad del producto en 1, se usa cuando el usuario presiona "+" en el carrito
    public void incrementar() {
        this.cantidad++;
    }

    //Disminuye la cantidad del producto en 1, se usa cuando el usuario presiona "-" en el carrito
    public void disminuir() {
        this.cantidad--;
    }
}
