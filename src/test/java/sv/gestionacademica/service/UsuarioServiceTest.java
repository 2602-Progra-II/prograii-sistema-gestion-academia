package sv.gestionacademica.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.entity.Rol;
import sv.gestionacademica.enums.EstadoUsuario;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UsuarioServiceTest {

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService() {
            private final List<Usuario> usuarios = new ArrayList<>();

            @Override
            public void registrar(Usuario usuario) {
                if (usuario == null) {
                    throw new IllegalArgumentException("El usuario no puede ser nulo");
                }
                for (Usuario u : usuarios) {
                    if (u.getIdUsuario() == usuario.getIdUsuario()) {
                        throw new IllegalArgumentException("Usuario duplicado");
                    }
                }
                usuarios.add(usuario);
            }

            @Override
            public void modificar(Usuario usuario) {}

            @Override
            public void desactivar(int idUsuario) {}

            @Override
            public void activar(int idUsuario) {}

            @Override
            public void asignarRol(int idUsuario, Rol rol) {}
        };
    }

    @Test
    @DisplayName("Debe registrar un nuevo usuario correctamente")
    void testRegistrarUsuarioExitoso() {
        Usuario usuario = new Usuario(1, "estudiante1", "Pérez", "pass123", EstadoUsuario.ACTIVO, new Rol(1, "Estudiante", "Rol estudiante"));
        assertDoesNotThrow(() -> usuarioService.registrar(usuario), "El usuario debería registrarse con éxito");
    }

    @Test
    @DisplayName("No debe permitir registrar un usuario duplicado")
    void testRegistrarUsuarioDuplicado() {
        Usuario usuario = new Usuario(1, "admin", "Admin", "pass123", EstadoUsuario.ACTIVO, new Rol(1, "Admin", "Rol admin"));
        usuarioService.registrar(usuario);

        assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.registrar(usuario);
        }, "No se debería permitir registrar un usuario que ya existe");
    }

    @Test
    @DisplayName("Debe lanzar excepción al intentar registrar un usuario nulo")
    void testRegistrarUsuarioInvalido() {
        assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.registrar(null);
        }, "Debería lanzar excepción si el usuario es nulo");
    }
}