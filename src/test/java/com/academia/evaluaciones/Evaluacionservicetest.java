package com.academia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EvaluacionServiceTest {

    private EvaluacionService evaluacionService;
    private EntregaRepository entregaRepository;

    private ActividadEvaluativa actividadVigente;
    private ActividadEvaluativa actividadVencida;

    @BeforeEach
    void setUp() {
        entregaRepository = new EntregaRepository();
        evaluacionService = new EvaluacionService(entregaRepository);

        actividadVigente = new ActividadEvaluativa(
                1, "Tarea 1", "Ejercicios de repaso", LocalDate.now().plusDays(5), 20.0);
        actividadVencida = new ActividadEvaluativa(
                2, "Tarea 0", "Diagnóstico inicial", LocalDate.now().minusDays(1), 10.0);
    }

    @Test
    @DisplayName("Debe registrar una entrega correctamente cuando la actividad está vigente")
    void testEntregarActividadExitosa() {
        EntregaEvaluacion entrega = evaluacionService.entregarActividad(
                actividadVigente, "est001", "/entregas/est001/tarea1.pdf");

        assertNotNull(entrega, "La entrega debería crearse");
        assertTrue(entrega.getIdEntrega() > 0, "Debería asignarse un id de entrega");
        assertEquals("est001", entrega.getIdEstudiante());
        assertFalse(entrega.estaCalificada(), "Una entrega recién creada no debería estar calificada");
        assertEquals(1, entregaRepository.obtenerTotalEntregas());
    }

    @Test
    @DisplayName("No debe permitir entregar una actividad vencida")
    void testEntregarActividadVencida() {
        assertThrows(IllegalStateException.class, () -> {
            evaluacionService.entregarActividad(actividadVencida, "est001", "/entregas/est001/tarea0.pdf");
        }, "Debería lanzar excepción si la actividad ya venció");
    }

    @Test
    @DisplayName("No debe permitir que un estudiante entregue dos veces la misma actividad")
    void testEntregarActividadDuplicada() {
        evaluacionService.entregarActividad(actividadVigente, "est001", "/entregas/est001/v1.pdf");

        assertThrows(IllegalStateException.class, () -> {
            evaluacionService.entregarActividad(actividadVigente, "est001", "/entregas/est001/v2.pdf");
        }, "Debería lanzar excepción si el estudiante ya entregó esa actividad");
    }

    @Test
    @DisplayName("Debe lanzar excepción al entregar con id de estudiante vacío")
    void testEntregarActividadEstudianteInvalido() {
        assertThrows(IllegalArgumentException.class, () -> {
            evaluacionService.entregarActividad(actividadVigente, "  ", "/entregas/est001/tarea1.pdf");
        }, "Debería lanzar excepción si el id del estudiante está vacío");
    }

    @Test
    @DisplayName("Debe lanzar excepción al entregar con actividad nula")
    void testEntregarActividadNula() {
        assertThrows(IllegalArgumentException.class, () -> {
            evaluacionService.entregarActividad(null, "est001", "/entregas/est001/tarea1.pdf");
        }, "Debería lanzar excepción si la actividad es nula");
    }

    @Test
    @DisplayName("Debe calificar una entrega existente correctamente")
    void testCalificarActividadExitosa() {
        EntregaEvaluacion entrega = evaluacionService.entregarActividad(
                actividadVigente, "est001", "/entregas/est001/tarea1.pdf");

        EntregaEvaluacion calificada = evaluacionService.calificarActividad(
                entrega.getIdEntrega(), 8.5, "Buen trabajo, revisar conclusiones", "inst001");

        assertTrue(calificada.estaCalificada(), "La entrega debería quedar marcada como calificada");
        assertEquals(8.5, calificada.getCalificacion());
        assertEquals("Buen trabajo, revisar conclusiones", calificada.getObservaciones());
    }

    @Test
    @DisplayName("Debe lanzar excepción al calificar una entrega que no existe")
    void testCalificarActividadInexistente() {
        assertThrows(IllegalArgumentException.class, () -> {
            evaluacionService.calificarActividad(999, 9.0, "N/A", "inst001");
        }, "Debería lanzar excepción si el id de entrega no existe");
    }

    @Test
    @DisplayName("Debe lanzar excepción al calificar fuera del rango permitido")
    void testCalificarActividadFueraDeRango() {
        EntregaEvaluacion entrega = evaluacionService.entregarActividad(
                actividadVigente, "est001", "/entregas/est001/tarea1.pdf");

        assertThrows(IllegalArgumentException.class, () -> {
            evaluacionService.calificarActividad(entrega.getIdEntrega(), 15.0, "N/A", "inst001");
        }, "Debería lanzar excepción si la calificación es mayor a " + EvaluacionService.CALIFICACION_MAXIMA);
    }

    @Test
    @DisplayName("Debe lanzar excepción al calificar sin id de instructor")
    void testCalificarActividadSinInstructor() {
        EntregaEvaluacion entrega = evaluacionService.entregarActividad(
                actividadVigente, "est001", "/entregas/est001/tarea1.pdf");

        assertThrows(IllegalArgumentException.class, () -> {
            evaluacionService.calificarActividad(entrega.getIdEntrega(), 7.0, "N/A", "");
        }, "Debería lanzar excepción si no se indica el instructor que califica");
    }

    @Test
    @DisplayName("Debe listar únicamente las calificaciones del estudiante consultado")
    void testConsultarCalificacionesPorEstudiante() {
        evaluacionService.entregarActividad(actividadVigente, "est001", "/entregas/est001/tarea1.pdf");
        evaluacionService.entregarActividad(actividadVigente, "est002", "/entregas/est002/tarea1.pdf");

        List<EntregaEvaluacion> resultado = evaluacionService.consultarCalificaciones("est001");

        assertEquals(1, resultado.size(), "Solo debería devolver las entregas de est001");
        assertEquals("est001", resultado.get(0).getIdEstudiante());
    }

    @Test
    @DisplayName("Debe filtrar calificaciones por las actividades de un curso específico")
    void testConsultarCalificacionesPorCurso() {
        ActividadEvaluativa actividadOtroCurso = new ActividadEvaluativa(
                3, "Tarea de otro curso", "No pertenece al curso consultado",
                LocalDate.now().plusDays(3), 15.0);

        evaluacionService.entregarActividad(actividadVigente, "est001", "/entregas/est001/tarea1.pdf");
        evaluacionService.entregarActividad(actividadOtroCurso, "est001", "/entregas/est001/otra.pdf");

        List<EntregaEvaluacion> resultado = evaluacionService.consultarCalificacionesPorCurso(
                "est001", List.of(actividadVigente));

        assertEquals(1, resultado.size(), "Solo debería incluir entregas de actividades del curso indicado");
        assertEquals(actividadVigente.getIdActividad(), resultado.get(0).getIdActividad());
    }

    @Test
    @DisplayName("Debe lanzar excepción al consultar calificaciones sin id de estudiante")
    void testConsultarCalificacionesSinEstudiante() {
        assertThrows(IllegalArgumentException.class, () -> {
            evaluacionService.consultarCalificaciones(null);
        }, "Debería lanzar excepción si no se indica el estudiante");
    }
}