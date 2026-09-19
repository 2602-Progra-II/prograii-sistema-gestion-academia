package sv.gestionacademica.persistence;

import sv.gestionacademica.entity.PeriodoAcademico;
import sv.gestionacademica.repository.PeriodoAcademicoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class PeriodoAcademicoRepositoryImpl implements PeriodoAcademicoRepository {

    private final List<PeriodoAcademico> periodos = new ArrayList<>();
    private int secuenciaId = 1;

    @Override
    public PeriodoAcademico guardar(PeriodoAcademico periodo) {
        periodo.setIdPeriodo(secuenciaId++);
        periodos.add(periodo);
        return periodo;
    }

    @Override
    public Optional<PeriodoAcademico> buscarPorId(int idPeriodo) {
        return periodos.stream()
                .filter(p -> p.getIdPeriodo() == idPeriodo)
                .findFirst();
    }

    @Override
    public Optional<PeriodoAcademico> buscarPorNombre(String nombrePeriodo) {
        return periodos.stream()
                .filter(p -> p.getNombrePeriodo().equalsIgnoreCase(nombrePeriodo))
                .findFirst();
    }

    @Override
    public List<PeriodoAcademico> listarTodos() {
        return new ArrayList<>(periodos);
    }

    @Override
    public List<PeriodoAcademico> listarVigentes() {
        return periodos.stream()
                .filter(PeriodoAcademico::estaVigente)
                .toList();
    }

    @Override
    public PeriodoAcademico actualizar(PeriodoAcademico periodo) {

        return periodo;
    }

    @Override
    public void eliminar(int idPeriodo) {
        periodos.removeIf(p -> p.getIdPeriodo() == idPeriodo);
    }
}
