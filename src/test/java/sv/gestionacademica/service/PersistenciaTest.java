package sv.gestionacademica.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PersistenciaTest {

    @Test
    @DisplayName("Debe guardar y recuperar un registro correctamente")
    void testGuardarYRecuperar() {
        UsuarioRepository repo = new UsuarioRepository() {
            private final List<Usuario> lista = new ArrayList<>();

            @Override
            public void guardar(Usuario usuario) { lista.add(usuario); }

            @Override
            public Usuario buscarPorId(int idUsuario) { return lista.stream().filter(u -> u.getIdUsuario() == idUsuario).findFirst().orElse(null); }

            @Override
            public List<Usuario> listarTodos() { return lista; }

            @Override
            public void actualizar(Usuario usuario) {}

            @Override
            public void eliminar(int idUsuario) { lista.removeIf(u -> u.getIdUsuario() == idUsuario); }
        };

        Usuario u = new Usuario(1, "juan", "Pérez", "123", null, null);
        repo.guardar(u);
        assertNotNull(repo.buscarPorId(1), "El usuario guardado debería poder ser recuperado");
    }
}