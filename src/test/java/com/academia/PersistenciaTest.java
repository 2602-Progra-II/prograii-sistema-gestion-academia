package com.academia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

public class PersistenciaTest {

    private PersistenciaService persistenciaService;

    @BeforeEach
    void setUp() {
        persistenciaService = new PersistenciaService();
    }

    @Test
    @DisplayName("Debe guardar y recuperar un registro correctamente")
    void testGuardarYObtenerRegistro() {
        boolean guardado = persistenciaService.guardarRegistro("usr_1", "Roberto Gutierrez");
        assertTrue(guardado, "El registro debería guardarse con éxito");
        
        String resultado = persistenciaService.obtenerRegistro("usr_1");
        assertEquals("Roberto Gutierrez", resultado, "El valor recuperado debe coincidir con el almacenado");
    }

    @Test
    @DisplayName("Debe lanzar excepción si se intenta guardar una clave vacía o nula")
    void testGuardarClaveInvalida() {
        assertThrows(IllegalArgumentException.class, () -> {
            persistenciaService.guardarRegistro("", "Datos de prueba");
        }, "Debe lanzar excepción al intentar guardar una clave vacía");
    }

    @Test
    @DisplayName("Debe eliminar un registro existente correctamente")
    void testEliminarRegistro() {
        persistenciaService.guardarRegistro("usr_2", "Maria Lopez");
        boolean eliminado = persistenciaService.eliminarRegistro("usr_2");
        
        assertTrue(eliminado, "Debería retornar true confirmando la eliminación");
        assertNull(persistenciaService.obtenerRegistro("usr_2"), "El registro ya no debería existir");
    }

    @Test
    @DisplayName("Debe limpiar todos los registros guardados")
    void testLimpiarBaseDeDatos() {
        persistenciaService.guardarRegistro("config_1", "Tema_Oscuro");
        persistenciaService.guardarRegistro("config_2", "Idioma_ES");
        assertEquals(2, persistenciaService.obtenerTotalRegistros());

        persistenciaService.limpiarBaseDeDatos();
        assertEquals(0, persistenciaService.obtenerTotalRegistros(), "La base de datos debería quedar vacía tras limpiar");
    }
}
