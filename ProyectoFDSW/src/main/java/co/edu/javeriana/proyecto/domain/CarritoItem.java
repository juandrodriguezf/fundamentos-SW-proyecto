package co.edu.javeriana.proyecto.domain;

public class CarritoItem {
    private Libro libro;
    private int cantidad;

    public CarritoItem(Libro libro, int cantidad) {
        this.libro = libro;
        this.cantidad = cantidad;
    }

    public Libro getLibro() {
        return libro;
    }

    public void setLibro(Libro libro) {
        this.libro = libro;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
    
    public double getSubtotal() {
        return libro.getPrecio() * cantidad;
    }
}
