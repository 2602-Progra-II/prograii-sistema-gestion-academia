// package sv.gestionacademica.entity;

// public class PeriodoAcademico {
    
// }
package sv.gestionacademica.entity;

import sv.gestionacademica.enums.EstadoPeriodo;

import java.time.LocalDate;


public class PeriodoAcademico {

    private int idPeriodo;
    private String nombrePeriodo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoPeriodo estado;

    /** Crea un periodo nuevo (id lo asigna quien lo persista). Nace PLANIFICADO. */
    public PeriodoAcademico(String nombrePeriodo, LocalDate fechaInicio, LocalDate fechaFin) {
        this.nombrePeriodo = nombrePeriodo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = EstadoPeriodo.PLANIFICADO;
    }

    /** Reconstruye un periodo que ya existe (ya tiene id y estado). */
    public PeriodoAcademico(int idPeriodo, String nombrePeriodo, LocalDate fechaInicio,
                             LocalDate fechaFin, EstadoPeriodo estado) {
        this.idPeriodo = idPeriodo;
        this.nombrePeriodo = nombrePeriodo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
    }

    public int getIdPeriodo() {
        return idPeriodo;
    }

    public void setIdPeriodo(int idPeriodo) {
        this.idPeriodo = idPeriodo;
    }

    public String getNombrePeriodo() {
        return nombrePeriodo;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public EstadoPeriodo getEstado() {
        return estado;
    }


    public void renombrar(String nuevoNombre) {
        if (estado == EstadoPeriodo.FINALIZADO) {
            throw new IllegalStateException("No se puede renombrar un periodo finalizado.");
        }
        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del periodo no puede estar vacío.");
        }
        this.nombrePeriodo = nuevoNombre;
    }


    public boolean fechasValidas() {
        return fechaInicio != null && fechaFin != null && fechaInicio.isBefore(fechaFin);
    }


    public boolean estaVigente() {
        LocalDate hoy = LocalDate.now();
        return estado == EstadoPeriodo.ACTIVO
                && !hoy.isBefore(fechaInicio)
                && !hoy.isAfter(fechaFin);
    }


    public void cambiarEstado(EstadoPeriodo nuevoEstado) {
        if (!this.estado.puedeTransicionarA(nuevoEstado)) {
            throw new IllegalStateException(
                    "Transición de estado inválida: " + this.estado + " -> " + nuevoEstado);
        }
        this.estado = nuevoEstado;
    }

    @Override
    public String toString() {
        return "PeriodoAcademico{id=%d, nombre='%s', inicio=%s, fin=%s, estado=%s}"
                .formatted(idPeriodo, nombrePeriodo, fechaInicio, fechaFin, estado);
    }


}
