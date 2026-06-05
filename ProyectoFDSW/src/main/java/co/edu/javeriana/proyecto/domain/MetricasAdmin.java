package co.edu.javeriana.proyecto.domain;

import java.util.List;
import java.util.Map;

public class MetricasAdmin {
    private int totalUsuarios;
    private int usuariosActivos;
    private int usuariosBloqueados;
    private int totalLibros;
    private int librosPendientes;
    private int librosAprobados;
    private double ingresosTotales;
    private int totalOrdenes;
    private List<Libro> topLibros;
    private Map<String, Integer> librosPorCategoria;

    public MetricasAdmin() {}

    public int getTotalUsuarios() { return totalUsuarios; }
    public void setTotalUsuarios(int totalUsuarios) { this.totalUsuarios = totalUsuarios; }

    public int getUsuariosActivos() { return usuariosActivos; }
    public void setUsuariosActivos(int usuariosActivos) { this.usuariosActivos = usuariosActivos; }

    public int getUsuariosBloqueados() { return usuariosBloqueados; }
    public void setUsuariosBloqueados(int usuariosBloqueados) { this.usuariosBloqueados = usuariosBloqueados; }

    public int getTotalLibros() { return totalLibros; }
    public void setTotalLibros(int totalLibros) { this.totalLibros = totalLibros; }

    public int getLibrosPendientes() { return librosPendientes; }
    public void setLibrosPendientes(int librosPendientes) { this.librosPendientes = librosPendientes; }

    public int getLibrosAprobados() { return librosAprobados; }
    public void setLibrosAprobados(int librosAprobados) { this.librosAprobados = librosAprobados; }

    public double getIngresosTotales() { return ingresosTotales; }
    public void setIngresosTotales(double ingresosTotales) { this.ingresosTotales = ingresosTotales; }

    public int getTotalOrdenes() { return totalOrdenes; }
    public void setTotalOrdenes(int totalOrdenes) { this.totalOrdenes = totalOrdenes; }

    public List<Libro> getTopLibros() { return topLibros; }
    public void setTopLibros(List<Libro> topLibros) { this.topLibros = topLibros; }

    public Map<String, Integer> getLibrosPorCategoria() { return librosPorCategoria; }
    public void setLibrosPorCategoria(Map<String, Integer> librosPorCategoria) { this.librosPorCategoria = librosPorCategoria; }
}
