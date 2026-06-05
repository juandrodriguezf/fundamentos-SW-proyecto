package co.edu.javeriana.proyecto.domain;

import co.edu.javeriana.proyecto.domain.state.LibroState;
import co.edu.javeriana.proyecto.domain.state.PendienteState;
import co.edu.javeriana.proyecto.domain.state.AprobadoState;
import co.edu.javeriana.proyecto.domain.state.RechazadoState;

public class Libro implements Cloneable {
    private Long id;
    private String titulo;
    private String autor;
    private String isbn;
    private String categoria;
    private String etiquetas; // Comma-separated tags
    private int clics;
    private double precio;
    private String portada;
    private int stock;
    private double calificacionPromedio;
    private LibroState estadoObj;
    private String rutaArchivo;

    public Libro() {
        this.estadoObj = new PendienteState();
    }

    public Libro(Long id, String titulo, String autor, String isbn, String categoria,
                 String etiquetas, int clics, double precio, String portada, int stock,
                 double calificacionPromedio, String estado, String rutaArchivo) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.categoria = categoria;
        this.etiquetas = etiquetas;
        this.clics = clics;
        this.precio = precio;
        this.portada = portada;
        this.stock = stock;
        this.calificacionPromedio = calificacionPromedio;
        this.rutaArchivo = rutaArchivo;
        
        // Mapeo inicial del estado
        if ("APROBADO".equalsIgnoreCase(estado)) {
            this.estadoObj = new AprobadoState();
        } else if ("RECHAZADO".equalsIgnoreCase(estado)) {
            this.estadoObj = new RechazadoState();
        } else {
            this.estadoObj = new PendienteState();
        }
    }

    // --- Métodos de Dominio Enriquecido y Patrón State ---

    public void aprobar() {
        this.estadoObj.aprobar(this);
    }

    public void rechazar() {
        this.estadoObj.rechazar(this);
    }

    public void setEstadoInterno(LibroState nuevoEstado) {
        this.estadoObj = nuevoEstado;
    }

    public void reducirStock(int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        if (this.stock < cantidad) {
            throw new IllegalStateException("No hay suficiente stock para el libro: " + this.titulo);
        }
        this.stock -= cantidad;
    }

    public void incrementarClics() {
        this.clics++;
    }

    // --- Patrón Prototype ---
    
    public Libro clonar() {
        try {
            return (Libro) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Error al clonar el libro", e);
        }
    }

    // --- Getters y Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getEtiquetas() { return etiquetas; }
    public void setEtiquetas(String etiquetas) { this.etiquetas = etiquetas; }

    public int getClics() { return clics; }
    public void setClics(int clics) { this.clics = clics; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getPortada() { return portada; }
    public void setPortada(String portada) { this.portada = portada; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public double getCalificacionPromedio() { return calificacionPromedio; }
    public void setCalificacionPromedio(double calificacionPromedio) { this.calificacionPromedio = calificacionPromedio; }

    // Retorna el string del estado para mantener compatibilidad con UI y DB
    public String getEstado() { return estadoObj.getNombreEstado(); }
    
    // Método deprecado, las transiciones ahora se manejan con aprobar() y rechazar()
    @Deprecated
    public void setEstado(String estado) { 
        if ("APROBADO".equalsIgnoreCase(estado)) {
            this.estadoObj = new AprobadoState();
        } else if ("RECHAZADO".equalsIgnoreCase(estado)) {
            this.estadoObj = new RechazadoState();
        } else {
            this.estadoObj = new PendienteState();
        }
    }

    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }

    @Override
    public String toString() {
        return "Libro{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", autor='" + autor + '\'' +
                ", isbn='" + isbn + '\'' +
                ", categoria='" + categoria + '\'' +
                ", clics=" + clics +
                ", precio=" + precio +
                ", estado='" + getEstado() + '\'' +
                ", rutaArchivo='" + rutaArchivo + '\'' +
                '}';
    }
}
