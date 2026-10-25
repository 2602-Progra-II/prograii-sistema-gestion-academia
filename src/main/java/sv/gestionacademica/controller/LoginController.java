package sv.gestionacademica.controller;

import sv.gestionacademica.entity.Sesion;
import sv.gestionacademica.exception.BusinessException;
import sv.gestionacademica.service.AutenticacionService;

import java.util.Optional;

public class LoginController {

    private final AutenticacionService autenticacionService;

    public LoginController(AutenticacionService autenticacionService) {
        this.autenticacionService = autenticacionService;
    }

    /** Devuelve la sesion si las credenciales son validas; si no, muestra el motivo y devuelve vacio. */
    public Optional<Sesion> iniciarSesion(String email, String contrasena) {
        try {
            return Optional.of(autenticacionService.iniciarSesion(email, contrasena));
        } catch (BusinessException e) {
            System.err.println("[Login] " + e.getMessage());
            return Optional.empty();
        }
    }

    public void cerrarSesion(Sesion sesion) {
        if (sesion != null) {
            autenticacionService.cerrarSesion(sesion.getIdSesion());
        }
    }
}
