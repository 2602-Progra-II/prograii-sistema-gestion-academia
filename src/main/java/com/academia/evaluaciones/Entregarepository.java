package com.academia;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EntregaRepository {

    private final Map<Integer, EntregaEvaluacion> entregas = new LinkedHashMap<>();
    private int siguienteId = 1;

    public EntregaEvaluacion guardar(EntregaEvaluacion entrega) {
        if (entrega == null) {
            throw new IllegalArgumentException("La entrega no puede ser nula");
        }
        if (entrega.getIdEntrega() == 0) {
            entrega.setIdEntrega(siguienteId++);
        }
        entregas.put(entrega.getIdEntrega(), entrega);
        return entrega;
    }

    public EntregaEvaluacion buscarPorId(int idEntrega) {
        return entregas.get(idEntrega);
    }

    public List<EntregaEvaluacion> listarPorActividad(int idActividad) {
        List<EntregaEvaluacion> resultado = new ArrayList<>();
        for (EntregaEvaluacion entrega : entregas.values()) {
            if (entrega.getIdActividad() == idActividad) {
                resultado.add(entrega);
            }
        }
        return resultado;
    }

    public List<EntregaEvaluacion> listarPorEstudiante(String idEstudiante) {
        List<EntregaEvaluacion> resultado = new ArrayList<>();
        for (EntregaEvaluacion entrega : entregas.values()) {
            if (entrega.getIdEstudiante().equals(idEstudiante)) {
                resultado.add(entrega);
            }
        }
        return resultado;
    }

    public EntregaEvaluacion actualizar(EntregaEvaluacion entrega) {
        if (entrega == null || !entregas.containsKey(entrega.getIdEntrega())) {
            throw new IllegalArgumentException("No existe una entrega con ese id para actualizar");
        }
        entregas.put(entrega.getIdEntrega(), entrega);
        return entrega;
    }

    public int obtenerTotalEntregas() {
        return entregas.size();
    }

    public void limpiar() {
        entregas.clear();
        siguienteId = 1;
    }
}