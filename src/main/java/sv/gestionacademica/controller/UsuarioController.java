package sv.gestionacademica.controller;

import sv.gestionacademica.entity.Rol;
import sv.gestionacademica.entity.Usuario;
import sv.gestionacademica.service.UsuarioService;

public class UsuarioController {

    private UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public void registrar(Usuario usuario) {
        usuarioService.registrar(usuario);
    }

    public void modificar(Usuario usuario) {
        usuarioService.modificar(usuario);
    }

    public void activar(int idUsuario) {
        usuarioService.activar(idUsuario);
    }

    public void desactivar(int idUsuario) {
        usuarioService.desactivar(idUsuario);
    }

    public void asignarRol(int idUsuario, Rol rol) {
        usuarioService.asignarRol(idUsuario, rol);
    }
}