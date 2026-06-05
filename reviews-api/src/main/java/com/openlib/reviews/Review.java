package com.openlib.reviews;

import jakarta.persistence.*;

@Entity
@Table(name = "reviews")
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long libroId;
    
    @Column(nullable = false)
    private String autor;
    
    @Column(nullable = false)
    private int calificacion;
    
    @Column(length = 1000)
    private String comentario;

    public Review() {}

    public Review(Long libroId, String autor, int calificacion, String comentario) {
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
