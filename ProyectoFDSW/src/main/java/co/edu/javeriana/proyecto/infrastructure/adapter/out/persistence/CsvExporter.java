package co.edu.javeriana.proyecto.infrastructure.adapter.out.persistence;

import co.edu.javeriana.proyecto.domain.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CsvExporter {

    private static String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public static String metricasToCsv(MetricasAdmin m) {
        StringBuilder sb = new StringBuilder();
        sb.append("Metrica,Valor\n");
        sb.append("Total Usuarios,").append(m.getTotalUsuarios()).append("\n");
        sb.append("Usuarios Activos,").append(m.getUsuariosActivos()).append("\n");
        sb.append("Usuarios Bloqueados,").append(m.getUsuariosBloqueados()).append("\n");
        sb.append("Total Libros,").append(m.getTotalLibros()).append("\n");
        sb.append("Libros Pendientes,").append(m.getLibrosPendientes()).append("\n");
        sb.append("Libros Aprobados,").append(m.getLibrosAprobados()).append("\n");
        sb.append("Ingresos Totales,").append(String.format("%.2f", m.getIngresosTotales())).append("\n");
        sb.append("Total Ordenes,").append(m.getTotalOrdenes()).append("\n\n");

        sb.append("Rango,Titulo,Autores,Clics\n");
        if (m.getTopLibros() != null) {
            int rank = 1;
            for (Libro l : m.getTopLibros()) {
                sb.append(rank++)
                        .append(",").append(escape(l.getTitulo()))
                        .append(",").append(escape(l.getAutor()))
                        .append(",").append(l.getClics())
                        .append("\n");
            }
        }

        sb.append("\nCategoria,# Libros\n");
        if (m.getLibrosPorCategoria() != null) {
            for (Map.Entry<String, Integer> entry : m.getLibrosPorCategoria().entrySet()) {
                sb.append(escape(entry.getKey())).append(",").append(entry.getValue()).append("\n");
            }
        }

        return sb.toString();
    }

    public static String usuariosToCsv(List<Usuario> usuarios) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Nombre,Email,Activo,Intentos Fallidos\n");
        for (Usuario u : usuarios) {
            sb.append(u.getId())
                    .append(",").append(escape(u.getNombre()))
                    .append(",").append(escape(u.getEmail()))
                    .append(",").append(u.isActivo())
                    .append(",").append(u.getIntentosFallidos())
                    .append("\n");
        }
        return sb.toString();
    }

    public static String librosToCsv(List<Libro> libros) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Titulo,Autor,ISBN,Categoria,Etiquetas,Precio,Stock,Estado,Clics,Calificacion\n");
        for (Libro l : libros) {
            sb.append(l.getId())
                    .append(",").append(escape(l.getTitulo()))
                    .append(",").append(escape(l.getAutor()))
                    .append(",").append(escape(l.getIsbn()))
                    .append(",").append(escape(l.getCategoria()))
                    .append(",").append(escape(l.getEtiquetas()))
                    .append(",").append(String.format("%.2f", l.getPrecio()))
                    .append(",").append(l.getStock())
                    .append(",").append(escape(l.getEstado()))
                    .append(",").append(l.getClics())
                    .append(",").append(String.format("%.1f", l.getCalificacionPromedio()))
                    .append("\n");
        }
        return sb.toString();
    }

    public static String categoriasToCsv(List<Categoria> categorias) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Nombre,Descripcion,# Libros\n");
        for (Categoria c : categorias) {
            sb.append(c.getId())
                    .append(",").append(escape(c.getNombre()))
                    .append(",").append(escape(c.getDescripcion()))
                    .append(",").append(c.getTotalLibros())
                    .append("\n");
        }
        return sb.toString();
    }

    public static String etiquetasToCsv(List<Etiqueta> etiquetas) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Nombre,# Libros\n");
        for (Etiqueta e : etiquetas) {
            sb.append(e.getId())
                    .append(",").append(escape(e.getNombre()))
                    .append(",").append(e.getTotalLibros())
                    .append("\n");
        }
        return sb.toString();
    }
}
