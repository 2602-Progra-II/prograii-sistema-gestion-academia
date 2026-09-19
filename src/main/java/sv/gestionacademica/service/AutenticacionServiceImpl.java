package sv.gestionacademica.service;

import sv.gestionacademica.entity.Sesion;
import java.time.LocalDateTime;

public class AutenticacionServiceImpl implements AutenticacionService {

    @Override
    public boolean validarCredenciales(String nombreUsuario, String contrasena) {
        if (nombreUsuario == null || contrasena == null || nombreUsuario.isEmpty() || contrasena.isEmpty()) {
            throw new IllegalArgumentException("El usuario y la contraseña no pueden estar vacíos.");
        }

        return "admin".equals(nombreUsuario) && "1234".equals(contrasena);
    }

    @Override
    public Sesion iniciarSesion(String nombreUsuario, String contrasena) {
        if (validarCredenciales(nombreUsuario, contrasena)) {
            // Retorna un objeto Sesion o ajusta según tu lógica interna
            return new Sesion(1, LocalDateTime.now(), null, true);
        }
        return null;
    }

    @Override
    public void cerrarSesion(int idSesion) {
        // Implementación del cierre de sesión
    }
}