package com.academia;

public class AutenticacionService {

    public boolean login(String usuario, String password) {
        if (usuario == null || password == null || usuario.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("El usuario y la contraseña no pueden estar vacíos.");
        }
        
        if ("admin".equals(usuario) && "1234".equals(password)) {
            return true;
        }
        
        return false;
        
    }
}