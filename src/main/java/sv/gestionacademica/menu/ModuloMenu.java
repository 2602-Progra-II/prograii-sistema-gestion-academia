package sv.gestionacademica.menu;

import sv.gestionacademica.entity.Sesion;

/**
 * Punto de conexion entre el menu principal y los menus de cada modulo.
 * Cada modulo (por ejemplo MenuAcademico de material/actividades) solo necesita
 * un metodo ejecutar(Sesion) y Main lo registra; asi nadie edita el menu de otro.
 */
@FunctionalInterface
public interface ModuloMenu {
    void ejecutar(Sesion sesion);
}
