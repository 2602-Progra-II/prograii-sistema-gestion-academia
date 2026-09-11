package com.academia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

public class UsuarioServiceTest {

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService();
    }

    @Test
    @DisplayName("Debe registrar un nuevo usuario correctamente")
    void testRegistrarUsuarioExitoso() {
        boolean registrado = usuarioService.registrarUsuario("estudiante1");
        assertTrue(registrado, "El usuario debería registrarse con éxito");
        assertTrue(usuarioService.existeUsuario("estudiante1"), "El usuario debería existir en el sistema");
    }

    @Test
    @DisplayName("No debe permitir registrar un usuario duplicado")
    void testRegistrarUsuarioDuplicado() {
        boolean registrado = usuarioService.registrarUsuario("admin");
        assertFalse(registrado, "No se debería permitir registrar un usuario que ya existe");
    }

    @Test
    @DisplayName("Debe lanzar excepción al intentar registrar un usuario nulo o vacío")
    void testRegistrarUsuarioInvalido() {
        assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.registrarUsuario("");
        }, "Debería lanzar excepción si el nombre de usuario está vacío");
    }

    @Test
    @DisplayName("Debe verificar correctamente el número de usuarios registrados")
    void testCantidadUsuarios() {
        assertEquals(1, usuarioService.obtenerCantidadUsuarios(), "Inicialmente debe haber 1 usuario base");
        usuarioService.registrarUsuario("profesor1");
        assertEquals(2, usuarioService.obtenerCantidadUsuarios(), "Debería haber 2 usuarios tras el nuevo registro");
    }
}
