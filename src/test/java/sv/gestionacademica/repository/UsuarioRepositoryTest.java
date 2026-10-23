package sv.gestionacademica.repository;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UsuarioRepositoryTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUsuarioRepositoryNotNull() {
        
        assertNotNull(usuarioRepository, "El repositorio de usuario debe inicializarse correctamente");
    }
}