package sv.gestionacademica.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sv.gestionacademica.entity.PeriodoAcademico;
import sv.gestionacademica.enums.EstadoPeriodo;
import sv.gestionacademica.repository.PeriodoAcademicoRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PeriodoAcademicoServiceTest {

    private PeriodoAcademicoService periodoAcademicoService;
    private FakePeriodoAcademicoRepository repository;

    @BeforeEach
    void setUp() {
        repository = new FakePeriodoAcademicoRepository();
        periodoAcademicoService = new PeriodoAcademicoService(repository);
    }

    @Test
    @DisplayName("Debería registrar un periodo académico correctamente")
    void testRegistrarPeriodoExitoso() {
        PeriodoAcademico periodo = crearPeriodo(1, "Ciclo 01-2026", LocalDate.now(), LocalDate.now().plusMonths(4));

        PeriodoAcademico resultado = periodoAcademicoService.registrarPeriodo(periodo);

        assertNotNull(resultado);
        assertEquals("Ciclo 01-2026", resultado.getNombrePeriodo());
    }

    @Test
    @DisplayName("Debería lanzar excepción al registrar periodo con fechas inválidas")
    void testRegistrarPeriodoFechasInvalidas() {
        PeriodoAcademico periodo = crearPeriodo(1, "Ciclo 01-2026", LocalDate.now().plusMonths(4), LocalDate.now());

        assertThrows(IllegalArgumentException.class, () -> {
            periodoAcademicoService.registrarPeriodo(periodo);
        });
    }

    @Test
    @DisplayName("Debería lanzar excepción al registrar un periodo con nombre duplicado")
    void testRegistrarPeriodoNombreDuplicado() {
        PeriodoAcademico periodo1 = crearPeriodo(1, "Ciclo 01-2026", LocalDate.now(), LocalDate.now().plusMonths(4));
        periodoAcademicoService.registrarPeriodo(periodo1);

        PeriodoAcademico periodo2 = crearPeriodo(2, "Ciclo 01-2026", LocalDate.now(), LocalDate.now().plusMonths(4));

        assertThrows(IllegalArgumentException.class, () -> {
            periodoAcademicoService.registrarPeriodo(periodo2);
        });
    }

    @Test
    @DisplayName("Debería modificar el nombre de un periodo existente")
    void testModificarPeriodoExitoso() {
        PeriodoAcademico periodo = crearPeriodo(1, "Ciclo 01-2026", LocalDate.now(), LocalDate.now().plusMonths(4));
        periodoAcademicoService.registrarPeriodo(periodo);

        PeriodoAcademico modificado = periodoAcademicoService.modificarPeriodo(1, "Ciclo 01-2026 Editado");

        assertEquals("Ciclo 01-2026 Editado", modificado.getNombrePeriodo());
    }

    @Test
    @DisplayName("Debería activar un periodo existente")
    void testActivarPeriodoExitoso() {
        PeriodoAcademico periodo = crearPeriodo(1, "Ciclo 01-2026", LocalDate.now(), LocalDate.now().plusMonths(4));
        periodoAcademicoService.registrarPeriodo(periodo);

        PeriodoAcademico activado = periodoAcademicoService.activarPeriodo(1);

        assertEquals(EstadoPeriodo.ACTIVO, activado.getEstado());
    }

    @Test
    @DisplayName("Debería finalizar un periodo existente")
    void testFinalizarPeriodoExitoso() {
        PeriodoAcademico periodo = crearPeriodo(1, "Ciclo 01-2026", LocalDate.now(), LocalDate.now().plusMonths(4));
        periodoAcademicoService.registrarPeriodo(periodo);
        periodoAcademicoService.activarPeriodo(1);

        PeriodoAcademico finalizado = periodoAcademicoService.finalizarPeriodo(1);

        assertEquals(EstadoPeriodo.FINALIZADO, finalizado.getEstado());
    }

    @Test
    @DisplayName("Debería lanzar NoSuchElementException al consultar un periodo inexistente")
    void testConsultarPeriodoInexistente() {
        assertThrows(NoSuchElementException.class, () -> {
            periodoAcademicoService.consultarPeriodo(99);
        });
    }

    @Test
    @DisplayName("Debería listar todos los periodos registrados")
    void testListarPeriodos() {
        PeriodoAcademico p1 = crearPeriodo(1, "Ciclo 01-2026", LocalDate.now(), LocalDate.now().plusMonths(4));
        PeriodoAcademico p2 = crearPeriodo(2, "Ciclo 02-2026", LocalDate.now(), LocalDate.now().plusMonths(4));

        periodoAcademicoService.registrarPeriodo(p1);
        periodoAcademicoService.registrarPeriodo(p2);

        List<PeriodoAcademico> lista = periodoAcademicoService.listarPeriodos();

        assertEquals(2, lista.size());
    }

    // --- Método auxiliar para instanciar la entidad según sus constructores/métodos ---
    private PeriodoAcademico crearPeriodo(int id, String nombre, LocalDate inicio, LocalDate fin) {
        try {
            // Intenta instanciar con constructor completo si existe
            return PeriodoAcademico.class.getConstructor(int.class, String.class, LocalDate.class, LocalDate.class)
                    .newInstance(id, nombre, inicio, fin);
        } catch (Exception e) {
            try {
                // Si la clase tiene constructor por defecto, asigna los campos vía métodos o reflexión
                PeriodoAcademico p = PeriodoAcademico.class.getDeclaredConstructor().newInstance();
                setCampo(p, "idPeriodo", id);
                setCampo(p, "nombrePeriodo", nombre);
                setCampo(p, "fechaInicio", inicio);
                setCampo(p, "fechaFin", fin);
                setCampo(p, "estado", EstadoPeriodo.PLANIFICADO);
                return p;
            } catch (Exception ex) {
                throw new RuntimeException("Error al crear la entidad PeriodoAcademico en la prueba", ex);
            }
        }
    }

    private void setCampo(Object target, String nombreCampo, Object valor) {
        try {
            var campo = target.getClass().getDeclaredField(nombreCampo);
            campo.setAccessible(true);
            campo.set(target, valor);
        } catch (Exception ignored) {
            // Ignorar si el campo no existe con ese nombre exacto
        }
    }

    // --- Repositorio Fake con la interfaz completa ---
    private static class FakePeriodoAcademicoRepository implements PeriodoAcademicoRepository {
        private final Map<Integer, PeriodoAcademico> datos = new HashMap<>();

        @Override
        public PeriodoAcademico guardar(PeriodoAcademico p) {
            datos.put(p.getIdPeriodo(), p);
            return p;
        }

        @Override
        public Optional<PeriodoAcademico> buscarPorId(int idPeriodo) {
            return Optional.ofNullable(datos.get(idPeriodo));
        }

        @Override
        public Optional<PeriodoAcademico> buscarPorNombre(String nombrePeriodo) {
            return datos.values().stream()
                    .filter(p -> p.getNombrePeriodo() != null && p.getNombrePeriodo().equalsIgnoreCase(nombrePeriodo))
                    .findFirst();
        }

        @Override
        public List<PeriodoAcademico> listarTodos() {
            return new ArrayList<>(datos.values());
        }

        @Override
        public List<PeriodoAcademico> listarVigentes() {
            return datos.values().stream()
                    .filter(p -> p.getEstado() == EstadoPeriodo.ACTIVO)
                    .collect(Collectors.toList());
        }

        @Override
        public PeriodoAcademico actualizar(PeriodoAcademico p) {
            datos.put(p.getIdPeriodo(), p);
            return p;
        }

        @Override
        public void eliminar(int idPeriodo) {
            datos.remove(idPeriodo);
        }
    }
}