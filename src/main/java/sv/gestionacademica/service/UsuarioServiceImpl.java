package sv.gestionacademica.service;

import java.util.ArrayList;
import java.util.List;
import sv.gestionacademica.entity.Usuario;

public class UsuarioServiceImpl implements UsuarioService {
    private List<Usuario> usuarios = new ArrayList<>();

    @Override
    public boolean registrarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo.");
        }
        return usuarios.add(usuario);
    }

    @Override
    public Usuario buscarPorId(int id) {
        for (Usuario u : usuarios) {
            if (u.getIdUsuario() == id) { // Ajusta el getter según el nombre exacto en Usuario.java
                return u;
            }
        }
        return null;
    }
}