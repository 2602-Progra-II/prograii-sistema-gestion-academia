package sv.gestionacademica.service;

import sv.gestionacademica.entity.Sesion;

/**
 * Inicio y cierre de sesion. La credencial es el email del usuario
 * (columna usuario.email, UNIQUE) y su contraseña.
 */
public interface AutenticacionService {

    /** Devuelve la sesion abierta o lanza BusinessException si las credenciales no son validas. */
    Sesion iniciarSesion(String email, String contrasena);

    boolean validarCredenciales(String email, String contrasena);

    void cerrarSesion(int idSesion);
}
