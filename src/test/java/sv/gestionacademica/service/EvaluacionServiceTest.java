package sv.gestionacademica.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sv.gestionacademica.entity.EntregaEvaluacion;
import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.repository.EntregaRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class EvaluacionServiceTest {

    private EvaluacionService evaluacionService;
    private InMemoryEntregaRepository entregaRepository;

    @BeforeEach
    void setUp() {
        entregaRepository = new InMemoryEntregaRepository();
        evaluacionService = new EvaluacionService(entregaRepository);
    }

    @Test
    void testEntregarActividadExitoso() {
        Usuario estudiante = new Usuario(1, "Juan", "Pérez", "123456", null, null);

        EntregaEvaluacion entrega = new EntregaEvaluacion();
        entrega.setIdEntrega(1);
        entrega.setIdActividad(101);
        entrega.setRutaArchivoEntrega("/uploads/tarea1.pdf");

        EntregaEvaluacion resultado = evaluacionService.entregarActividad(entrega, estudiante);

        assertNotNull(resultado);
        assertEquals("1", resultado.getIdEstudiante());
        assertNotNull(resultado.getFechaEntrega());
    }

    @Test
    void testCalificarActividadExitoso() {
        Usuario instructor = new Usuario(2, "Carlos", "López", "123456", null, null);

        EntregaEvaluacion entrega = new EntregaEvaluacion(1, "/uploads/tarea1.pdf", LocalDate.now(), -1.0, null, 101, "1");
        entregaRepository.guardar(entrega);

        entrega.setCalificacion(9.5);
        entrega.setObservaciones("Excelente trabajo");

        evaluacionService.calificarActividad(entrega, instructor);

        assertTrue(entrega.estaCalificada());
        assertEquals(9.5, entrega.getCalificacion());
    }

    @Test
    void testConsultarCalificaciones() {
        Usuario estudiante = new Usuario(1, "Juan", "Pérez", "123456", null, null);

        EntregaEvaluacion e1 = new EntregaEvaluacion(1, "/file1.pdf", LocalDate.now(), 8.0, "Buen trabajo", 101, "1");
        EntregaEvaluacion e2 = new EntregaEvaluacion(2, "/file2.pdf", LocalDate.now(), 10.0, "Excelente", 102, "1");

        entregaRepository.guardar(e1);
        entregaRepository.guardar(e2);

        List<EntregaEvaluacion> lista = evaluacionService.consultarCalificaciones(estudiante);

        assertEquals(2, lista.size());
    }

    @Test
    void testEstaCalificadaLogic() {
        EntregaEvaluacion entregaSinCalificar = new EntregaEvaluacion();
        entregaSinCalificar.setCalificacion(-1.0);
        assertFalse(entregaSinCalificar.estaCalificada());

        EntregaEvaluacion entregaCalificada = new EntregaEvaluacion();
        entregaCalificada.setCalificacion(7.5);
        entregaCalificada.setObservaciones("Aprobado");
        assertTrue(entregaCalificada.estaCalificada());
    }

    private static class InMemoryEntregaRepository implements EntregaRepository {
        private final List<EntregaEvaluacion> db = new ArrayList<>();

        @Override
        public EntregaEvaluacion guardar(EntregaEvaluacion entrega) {
            db.removeIf(e -> e.getIdEntrega() == entrega.getIdEntrega());
            db.add(entrega);
            return entrega;
        }

        @Override
        public Optional<EntregaEvaluacion> buscarPorId(int idEntrega) {
            return db.stream().filter(e -> e.getIdEntrega() == idEntrega).findFirst();
        }

        @Override
        public List<EntregaEvaluacion> listarPorActividad(int idActividad) {
            return db.stream().filter(e -> e.getIdActividad() == idActividad).collect(Collectors.toList());
        }

        @Override
        public List<EntregaEvaluacion> listarPorEstudiante(String idEstudiante) {
            return db.stream().filter(e -> e.getIdEstudiante() != null && e.getIdEstudiante().equals(idEstudiante)).collect(Collectors.toList());
        }

        @Override
        public List<EntregaEvaluacion> listarTodas() {
            return new ArrayList<>(db);
        }
    }
}