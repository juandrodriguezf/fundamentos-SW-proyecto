package co.edu.javeriana.proyecto.domain;

import java.time.LocalDateTime;

public class Compra {
    private Long id;
    private Long usuarioId;
    private Long libroId;
    private int cantidad;
    private double precioUnitario;
    private String ordenId;
    private LocalDateTime fecha;

    // Libro asociado (para mostrar info en UI)
    private Libro libro;

    public Compra() {}

    public Compra(Long id, Long usuarioId, Long libroId, int cantidad, double precioUnitario, String ordenId, LocalDateTime fecha) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.libroId = libroId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.ordenId = ordenId;
        this.fecha = fecha;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Long getLibroId() { return libroId; }
    public void setLibroId(Long libroId) { this.libroId = libroId; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public String getOrdenId() { return ordenId; }
    public void setOrdenId(String ordenId) { this.ordenId = ordenId; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public Libro getLibro() { return libro; }
    public void setLibro(Libro libro) { this.libro = libro; }

    public double getSubtotal() {
        return precioUnitario * cantidad;
    }
}
