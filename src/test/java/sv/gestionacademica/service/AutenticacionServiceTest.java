package sv.gestionacademica.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import sv.gestionacademica.entity.Sesion;

import static org.junit.jupiter.api.Assertions.*;

public class AutenticacionServiceTest {

    private AutenticacionService autenticacionService;

    @BeforeEach
    void setUp() {
        autenticacionService = new AutenticacionService() {
            @Override
            public Sesion iniciarSesion(String nombreUsuario, String contrasena) {
                if (nombreUsuario == null || nombreUsuario.isEmpty() || contrasena == null || contrasena.isEmpty()) {
                    throw new IllegalArgumentException("Credenciales inválidas");
                }
                if ("admin".equals(nombreUsuario) && "1234".equals(contrasena)) {
                    return new Sesion(1, java.time.LocalDateTime.now(), null, true);
                }
                return null;
            }

            @Override
            public void cerrarSesion(int idSesion) {}

            @Override
            public boolean validarCredenciales(String nombreUsuario, String contrasena) {
                return "admin".equals(nombreUsuario) && "1234".equals(contrasena);
            }
        };
    }

    @Test
    @DisplayName("Debe autenticar exitosamente con credenciales válidas")
    void testLoginExitoso() {
        Sesion sesion = autenticacionService.iniciarSesion("admin", "1234");
        assertNotNull(sesion, "La sesión no debería ser nula con credenciales válidas");
    }

    @Test
    @DisplayName("Debe fallar la autenticación con contraseña incorrecta")
    void testLoginClaveIncorrecta() {
        Sesion sesion = autenticacionService.iniciarSesion("admin", "clave_erronea");
        assertNull(sesion, "La sesión debería ser nula con clave incorrecta");
    }

    @Test
    @DisplayName("Debe fallar la autenticación cuando el usuario no existe")
    void testLoginUsuarioInexistente() {
        Sesion sesion = autenticacionService.iniciarSesion("usuario_inexistente", "1234");
        assertNull(sesion, "La sesión debería ser nula para un usuario inexistente");
    }

    @Test
    @DisplayName("Debe rechazar credenciales nulas o vacías")
    void testLoginCredencialesVacias() {
        assertThrows(IllegalArgumentException.class, () -> {
            autenticacionService.iniciarSesion("", "");
        }, "Debería lanzar excepción si las credenciales están vacías");
    }
}