package sv.gestionacademica.dto;

import sv.gestionacademica.entity.Matricula;
import sv.gestionacademica.entity.Pago;

/** Resultado de matricular a un estudiante: la matricula creada y el pago pendiente que la acompana. */
public record ResultadoMatricula(Matricula matricula, Pago pago) {
}
