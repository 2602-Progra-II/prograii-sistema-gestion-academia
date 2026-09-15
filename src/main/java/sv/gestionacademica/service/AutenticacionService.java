package main.java.sv.gestionacademica.service;

import main.java.sv.gestionacademica.entity.Sesion;

public interface AutenticacionService {

    Sesion iniciarSesion(String nombreUsuario, String contrasena);

    boolean validarCredenciales(String nombreUsuario, String contrasena);

    void cerrarSesion(int idSesion);
}