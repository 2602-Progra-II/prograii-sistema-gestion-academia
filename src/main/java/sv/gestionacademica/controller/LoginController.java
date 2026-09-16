package main.java.sv.gestionacademica.controller;

import main.java.sv.gestionacademica.entity.Sesion;
import main.java.sv.gestionacademica.service.AutenticacionService;

public class LoginController {

    private AutenticacionService autenticacionService;

    public LoginController(AutenticacionService autenticacionService) {
        this.autenticacionService = autenticacionService;
    }

    public Sesion iniciarSesion(String nombreUsuario, String contrasena) {
        return autenticacionService.iniciarSesion(nombreUsuario, contrasena);
    }
}