package sv.gestionacademica.service;

import sv.gestionacademica.entity.Sesion;

public interface AutenticacionService {

    Sesion iniciarSesion(String nombreUsuario, String contrasena);

    boolean validarCredenciales(String nombreUsuario, String contrasena);

    void cerrarSesion(int idSesion);
}