package sv.gestionacademica.repository;

import sv.gestionacademica.entity.Usuario;

import java.util.List;

public interface UsuarioRepository {

    void guardar(Usuario usuario);

    Usuario buscarPorId(int idUsuario);

    /** Busca por email (credencial de login). Devuelve null si no existe. */
    Usuario buscarPorEmail(String email);

    List<Usuario> listarTodos();

    void actualizar(Usuario usuario);

    void eliminar(int idUsuario);
}