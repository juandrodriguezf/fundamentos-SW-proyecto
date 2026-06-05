package co.edu.javeriana.proyecto.domain;

public class Categoria {
    private Long id;
    private String nombre;
    private String descripcion;
    private int totalLibros;

    public Categoria() {}

    public Categoria(Long id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.totalLibros = 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int getTotalLibros() { return totalLibros; }
    public void setTotalLibros(int totalLibros) { this.totalLibros = totalLibros; }

    @Override
    public String toString() { return nombre; }
}
