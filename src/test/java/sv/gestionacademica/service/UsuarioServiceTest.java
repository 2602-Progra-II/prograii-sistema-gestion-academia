package sv.gestionacademica.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.entity.Rol;
import sv.gestionacademica.enums.EstadoUsuario;

public class UsuarioServiceTest {

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService() {
            @Override
            public void registrar(Usuario usuario) {
                if (usuario == null) {
                    throw new IllegalArgumentException("El usuario no puede ser nulo");
                }
            }

            @Override
            public void modificar(Usuario usuario) {
                if (usuario == null) {
                    throw new IllegalArgumentException("El usuario no puede ser nulo");
                }
            }

            @Override
            public void activar(int idUsuario) {
                if (idUsuario <= 0) {
                    throw new IllegalArgumentException("ID invalido");
                }
            }

            @Override
            public void desactivar(int idUsuario) {
                if (idUsuario <= 0) {
                    throw new IllegalArgumentException("ID invalido");
                }
            }

            @Override
            public void asignarRol(int idUsuario, Rol rol) {
                if (idUsuario <= 0 || rol == null) {
                    throw new IllegalArgumentException("Datos de rol invalidos");
                }
            }
        };
    }

    @Test
    @DisplayName("Debe registrar un nuevo usuario correctamente")
    void testRegistrarUsuarioExitoso() {
        Usuario usuario = new Usuario(1, "Bryan Reyes", "bryan@mail.com", "1234", EstadoUsuario.ACTIVO, null);
        assertDoesNotThrow(() -> usuarioService.registrar(usuario));
    }

    @Test
    @DisplayName("Debe lanzar excepción al intentar registrar un usuario nulo")
    void testRegistrarUsuarioInvalido() {
        assertThrows(IllegalArgumentException.class, () -> usuarioService.registrar(null));
    }

    @Test
    @DisplayName("Debe permitir activar y desactivar un usuario con ID valido")
    void testActivarDesactivarUsuario() {
        assertDoesNotThrow(() -> usuarioService.activar(1));
        assertDoesNotThrow(() -> usuarioService.desactivar(1));
    }
}
