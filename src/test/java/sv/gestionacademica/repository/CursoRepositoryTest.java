package sv.gestionacademica.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CursoRepositoryTest {

    @Mock
    private CursoRepository cursoRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCursoRepositoryNotNull() {
        
        assertNotNull(cursoRepository, "El repositorio de cursos debe inicializarse correctamente");
    }
}