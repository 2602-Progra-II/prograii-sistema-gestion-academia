package sv.gestionacademica.service;

import java.util.ArrayList;
import java.util.List;

public class EstadoCuentaService {
    private List<EstadoCuenta> listaCuentas = new ArrayList<>();


    public void registrarEstadoCuenta(EstadoCuenta cuenta) {
        listaCuentas.removeIf(c -> c.getIdEstudiante().equals(cuenta.getIdEstudiante()));
        listaCuentas.add(cuenta);
    }


    public EstadoCuenta consultarEstadoCuenta(String idEstudiante) {
        for (EstadoCuenta cuenta : listaCuentas) {
            if (cuenta.getIdEstudiante().equalsIgnoreCase(idEstudiante)) {
                return cuenta;
            }
        }
        return null;
    }


    public List<EstadoCuenta> obtenerEstudiantesEnMora() {
        List<EstadoCuenta> enMoraList = new ArrayList<>();
        for (EstadoCuenta cuenta : listaCuentas) {
            if (cuenta.isEnMora()) {
                enMoraList.add(cuenta);
            }
        }
        return enMoraList;
    }
}