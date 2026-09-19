package sv.gestionacademica.controller;

import sv.gestionacademica.entity.Sesion;
import sv.gestionacademica.service.AutenticacionService;

public class LoginController {

    private AutenticacionService autenticacionService;

    public LoginController(AutenticacionService autenticacionService) {
        this.autenticacionService = autenticacionService;
    }

    public Sesion iniciarSesion(String nombreUsuario, String contrasena) {
        return autenticacionService.iniciarSesion(nombreUsuario, contrasena);
    }
}