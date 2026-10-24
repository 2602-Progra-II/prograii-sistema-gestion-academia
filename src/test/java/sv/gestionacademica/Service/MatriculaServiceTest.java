package sv.gestionacademica.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sv.gestionacademica.entity.Curso;
import sv.gestionacademica.entity.Matricula;
import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.enums.EstadoMatricula;

class MatriculaServiceTest {

    private MatriculaService matriculaService;
    private Usuario estudiantePrueba;
    private Curso cursoPrueba;

    @BeforeEach
    void setUp() {
        matriculaService = new MatriculaService();
        cursoPrueba = new Curso(1, "MAT101", "Matemáticas", "Curso de prueba", null, 1, 1);
        estudiantePrueba = new Usuario(1, "Juan", "Perez", "password123", null, null);
    }

    @Test
    void crearMatricula_DatosValidos_CreaMatriculaExitosamente() {
        Matricula resultado = matriculaService.crearMatricula(estudiantePrueba, cursoPrueba, EstadoMatricula.ACTIVA);

        assertNotNull(resultado, "La matrícula no debería ser nula");
        assertEquals(1, resultado.getIdMatricula(), "El ID inicial debe ser 1");
        assertEquals(estudiantePrueba, resultado.getEstudiante(), "El estudiante debe coincidir");
        assertEquals(1, matriculaService.listarMatriculas().size(), "Debe haber 1 matrícula en la lista");
    }

    @Test
    void crearMatricula_EstudianteNulo_LanzaExcepcion() {
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, () -> {
            matriculaService.crearMatricula(null, cursoPrueba, EstadoMatricula.ACTIVA);
        });

        assertEquals("El estudiante y el curso son obligatorios", excepcion.getMessage());
    }
}