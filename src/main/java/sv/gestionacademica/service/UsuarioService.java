package sv.gestionacademica.service;

import sv.gestionacademica.entity.Rol;
import sv.gestionacademica.entity.Usuario;

public interface UsuarioService {

    void registrar(Usuario usuario);

    void modificar(Usuario usuario);

    void activar(int idUsuario);

    void desactivar(int idUsuario);

    void asignarRol(int idUsuario, Rol rol);
}