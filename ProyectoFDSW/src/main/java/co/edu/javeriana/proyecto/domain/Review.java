package co.edu.javeriana.proyecto.domain;

public class Review {
    private Long id;
    private Long libroId;
    private String autor;
    private int calificacion;
    private String comentario;

    public Review() {}

    public Review(Long id, Long libroId, String autor, int calificacion, String comentario) {
        this.id = id;
        this.libroId = libroId;
        this.autor = autor;
        this.calificacion = calificacion;
        this.comentario = comentario;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getLibroId() { return libroId; }
    public void setLibroId(Long libroId) { this.libroId = libroId; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public int getCalificacion() { return calificacion; }
    public void setCalificacion(int calificacion) { this.calificacion = calificacion; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
}
