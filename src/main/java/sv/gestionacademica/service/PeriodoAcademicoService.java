
package sv.gestionacademica.service;

import sv.gestionacademica.entity.PeriodoAcademico;
import sv.gestionacademica.enums.EstadoPeriodo;
import sv.gestionacademica.repository.PeriodoAcademicoRepository;

import java.util.List;
import java.util.NoSuchElementException;

public class PeriodoAcademicoService {

    private final PeriodoAcademicoRepository periodoAcademicoRepository;

    public PeriodoAcademicoService(PeriodoAcademicoRepository periodoAcademicoRepository) {
        this.periodoAcademicoRepository = periodoAcademicoRepository;
    }

    public PeriodoAcademico registrarPeriodo(PeriodoAcademico periodo) {
        if (!periodo.fechasValidas()) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha de fin.");
        }
        if (periodoAcademicoRepository.buscarPorNombre(periodo.getNombrePeriodo()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un periodo con ese nombre.");
        }
        return periodoAcademicoRepository.guardar(periodo);
    }

    public PeriodoAcademico modificarPeriodo(int idPeriodo, String nuevoNombre) {
        PeriodoAcademico periodo = obtenerOFallar(idPeriodo);
        periodo.renombrar(nuevoNombre);
        return periodoAcademicoRepository.actualizar(periodo);
    }

    public PeriodoAcademico activarPeriodo(int idPeriodo) {
        PeriodoAcademico periodo = obtenerOFallar(idPeriodo);
        periodo.cambiarEstado(EstadoPeriodo.ACTIVO);
        return periodoAcademicoRepository.actualizar(periodo);
    }

    public PeriodoAcademico finalizarPeriodo(int idPeriodo) {
        PeriodoAcademico periodo = obtenerOFallar(idPeriodo);
        periodo.cambiarEstado(EstadoPeriodo.FINALIZADO);
        return periodoAcademicoRepository.actualizar(periodo);
    }

    public PeriodoAcademico consultarPeriodo(int idPeriodo) {
        return obtenerOFallar(idPeriodo);
    }

    public List<PeriodoAcademico> listarPeriodos() {
        return periodoAcademicoRepository.listarTodos();
    }

    private PeriodoAcademico obtenerOFallar(int idPeriodo) {
        return periodoAcademicoRepository.buscarPorId(idPeriodo)
                .orElseThrow(() -> new NoSuchElementException(
                        "No existe un periodo académico con id " + idPeriodo));
    }
}
