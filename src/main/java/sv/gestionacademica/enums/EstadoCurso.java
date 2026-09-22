
package sv.gestionacademica.enums;

public enum EstadoCurso {

    INACTIVO {
        @Override
        public boolean puedeTransicionarA(EstadoCurso destino) {
            return destino == ACTIVO;
        }
    },
    ACTIVO {
        @Override
        public boolean puedeTransicionarA(EstadoCurso destino) {
            return destino == INACTIVO;
        }
    };

    public abstract boolean puedeTransicionarA(EstadoCurso destino);
}
