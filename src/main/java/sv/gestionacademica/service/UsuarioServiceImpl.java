package sv.gestionacademica.service;

import sv.gestionacademica.entity.Rol;
import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.enums.EstadoUsuario;
import sv.gestionacademica.repository.UsuarioRepository;
import sv.gestionacademica.repository.UsuarioRepositoryImpl;

public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl() {
        this.usuarioRepository = new UsuarioRepositoryImpl();
    }

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void registrar(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        usuarioRepository.guardar(usuario);
    }

    @Override
    public void modificar(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        usuarioRepository.actualizar(usuario);
    }

    @Override
    public void activar(int idUsuario) {
        Usuario u = usuarioRepository.buscarPorId(idUsuario);
        if (u != null) {
            u.setEstado(EstadoUsuario.ACTIVO);
            usuarioRepository.actualizar(u);
        }
    }

    @Override
    public void desactivar(int idUsuario) {
        Usuario u = usuarioRepository.buscarPorId(idUsuario);
        if (u != null) {
            u.setEstado(EstadoUsuario.INACTIVO);
            usuarioRepository.actualizar(u);
        }
    }

    @Override
    public void asignarRol(int idUsuario, Rol rol) {
        Usuario u = usuarioRepository.buscarPorId(idUsuario);
        if (u != null) {
            u.setRol(rol);
            usuarioRepository.actualizar(u);
        }
    }
}