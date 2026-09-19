package sv.gestionacademica.repository;

import sv.gestionacademica.entity.Usuario;

import java.util.List;

public interface UsuarioRepository {

    void guardar(Usuario usuario);

    Usuario buscarPorId(int idUsuario);

    List<Usuario> listarTodos();

    void actualizar(Usuario usuario);

    void eliminar(int idUsuario);
}