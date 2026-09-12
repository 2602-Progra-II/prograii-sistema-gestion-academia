package com.academia;

import java.util.HashMap;
import java.util.Map;

public class PersistenciaService {
    private Map<String, String> baseDeDatosSimulada = new HashMap<>();

    public boolean guardarRegistro(String clave, String valor) {
        if (clave == null || clave.trim().isEmpty()) {
            throw new IllegalArgumentException("La clave no puede ser nula o vacía");
        }
        if (valor == null) {
            throw new IllegalArgumentException("El valor no puede ser nulo");
        }
        baseDeDatosSimulada.put(clave, valor);
        return true;
    }

    public String obtenerRegistro(String clave) {
        if (clave == null) return null;
        return baseDeDatosSimulada.get(clave);
    }

    public boolean eliminarRegistro(String clave) {
        if (baseDeDatosSimulada.containsKey(clave)) {
            baseDeDatosSimulada.remove(clave);
            return true;
        }
        return false;
    }

    public int obtenerTotalRegistros() {
        return baseDeDatosSimulada.size();
    }

    public void limpiarBaseDeDatos() {
        baseDeDatosSimulada.clear();
    }
}