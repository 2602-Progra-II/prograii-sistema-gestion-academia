package com.academia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {

    // Simulación del servicio a probar
    // NOTA: Reemplazar 'AutenticacionService' por la clase real de tu proyecto si varía el nombre
    private AutenticacionService autenticacionService;

    @BeforeEach
    void setUp() {
        // Inicialización antes de cada prueba
        autenticacionService = new AutenticacionService();
    }

    @Test
    @DisplayName("Debe autenticar exitosamente con credenciales válidas")
    void testAutenticacionExitosa() {
        boolean resultado = autenticacionService.login("admin", "1234");
        assertTrue(resultado, "El usuario debería autenticarse correctamente con credenciales válidas.");
    }

    @Test
    @DisplayName("Debe fallar la autenticación con contraseña incorrecta")
    void testAutenticacionPasswordIncorrecto() {
        boolean resultado = autenticacionService.login("admin", "clave_erronea");
        assertFalse(resultado, "La autenticación debería fallar cuando la contraseña es incorrecta.");
    }

    @Test
    @DisplayName("Debe fallar la autenticación cuando el usuario no existe")
    void testAutenticacionUsuarioNoExiste() {
        boolean resultado = autenticacionService.login("usuario_inexistente", "1234");
        assertFalse(resultado, "La autenticación debería fallar si el usuario no está registrado.");
    }

    @Test
    @DisplayName("Debe rechazar credenciales nulas o vacías")
    void testAutenticacionCamposVacios() {
        assertThrows(IllegalArgumentException.class, () -> {
            autenticacionService.login("", "");
        }, "Debería lanzar excepción si se envían parámetros vacíos.");
    }
}