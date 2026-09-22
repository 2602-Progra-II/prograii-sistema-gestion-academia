
package sv.gestionacademica.enums;


public enum EstadoPeriodo {

    PLANIFICADO {
        @Override
        public boolean puedeTransicionarA(EstadoPeriodo destino) {
            return destino == ACTIVO;
        }
    },
    ACTIVO {
        @Override
        public boolean puedeTransicionarA(EstadoPeriodo destino) {
            return destino == FINALIZADO;
        }
    },
    FINALIZADO {
        @Override
        public boolean puedeTransicionarA(EstadoPeriodo destino) {
            return false; // estado terminal, no admite más cambios
        }
    };

    public abstract boolean puedeTransicionarA(EstadoPeriodo destino);
}
