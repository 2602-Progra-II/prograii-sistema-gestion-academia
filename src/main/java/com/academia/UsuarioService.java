package com.academia;

import java.util.ArrayList;
import java.util.List;

public class UsuarioService {
    private List<String> usuarios = new ArrayList<>();

    public UsuarioService() {
        // Usuario base por defecto
        usuarios.add("admin");
    }

    public boolean registrarUsuario(String usuario) {
        if (usuario == null || usuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacío");
        }
        if (usuarios.contains(usuario)) {
            return false; // Usuario ya existe
        }
        return usuarios.add(usuario);
    }

    public boolean existeUsuario(String usuario) {
        if (usuario == null) return false;
        return usuarios.contains(usuario);
    }

    public int obtenerCantidadUsuarios() {
        return usuarios.size();
    }
}
