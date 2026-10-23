package sv.gestionacademica.service;


import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class ValidacionNegocioTest {

    @Test
    void testExcepcionAnteDatosNulos() {
       
        assertThrows(IllegalArgumentException.class, () -> {
            validarRegistro(null);
        }, "Debería lanzar una excepción IllegalArgumentException si el objeto es nulo");
    }

    private void validarRegistro(Object objeto) {
        if (objeto == null) {
            throw new IllegalArgumentException("El objeto no puede ser nulo");
        }
    }
}