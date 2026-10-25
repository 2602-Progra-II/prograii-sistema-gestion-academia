package sv.gestionacademica.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.entity.Rol;
import sv.gestionacademica.enums.EstadoUsuario;
import sv.gestionacademica.persistence.DataSourceConfig;
import sv.gestionacademica.repository.RolRepository;
import sv.gestionacademica.repository.RolRepositoryImpl;
import sv.gestionacademica.service.UsuarioService;
import sv.gestionacademica.service.UsuarioServiceImpl;

import java.sql.Connection;
import java.sql.Statement;

public class UsuarioServiceTest {

    private UsuarioService usuarioService;
    private RolRepository rolRepository;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioServiceImpl();
        rolRepository = new RolRepositoryImpl(); // Instanciamos la implementación

        // Limpiar la base de datos antes de cada prueba respetando las FK
        try (Connection conn = DataSourceConfig.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM usuario");
            stmt.executeUpdate("DELETE FROM rol");
        } catch (Exception e) {
            // Silencioso si las tablas están vacías
        }
    }

    @Test
    @DisplayName("Debe registrar un nuevo usuario correctamente en la BD")
    void testRegistrarUsuarioExitoso() {
        Rol rol = new Rol(0, "ADMIN", "Administrador del sistema");
        rolRepository.save(rol); // Usamos save() en lugar de guardar()[cite: 19]

        Usuario usuario = new Usuario(0, "Bryan", "Reyes", "1234", EstadoUsuario.ACTIVO, rol);

        assertDoesNotThrow(() -> usuarioService.registrar(usuario));
        assertTrue(usuario.getIdUsuario() > 0, "El ID del usuario debe ser generado por la BD");
    }

    @Test
    @DisplayName("Debe lanzar excepción al intentar registrar un usuario nulo")
    void testRegistrarUsuarioInvalido() {
        assertThrows(IllegalArgumentException.class, () -> usuarioService.registrar(null));
    }

    @Test
    @DisplayName("Debe permitir activar y desactivar un usuario existente")
    void testActivarDesactivarUsuario() {
        Rol rol = new Rol(0, "ESTUDIANTE", "Estudiante regular");
        rolRepository.save(rol); // Usamos save() en lugar de guardar()[cite: 19]

        Usuario usuario = new Usuario(0, "Ana", "García", "pass", EstadoUsuario.INACTIVO, rol);
        usuarioService.registrar(usuario);

        assertDoesNotThrow(() -> usuarioService.activar(usuario.getIdUsuario()));
        assertDoesNotThrow(() -> usuarioService.desactivar(usuario.getIdUsuario()));
    }
}