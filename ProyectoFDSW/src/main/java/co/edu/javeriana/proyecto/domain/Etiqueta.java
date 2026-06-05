package co.edu.javeriana.proyecto.domain;

public class Etiqueta {
    private Long id;
    private String nombre;
    private int totalLibros;

    public Etiqueta() {}

    public Etiqueta(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
        this.totalLibros = 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getTotalLibros() { return totalLibros; }
    public void setTotalLibros(int totalLibros) { this.totalLibros = totalLibros; }

    @Override
    public String toString() { return nombre; }
}
