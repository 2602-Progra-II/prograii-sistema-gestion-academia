package sv.gestionacademica.persistence;

import sv.gestionacademica.exception.PersistenciaException;
import java.io.*;
import java.util.List;

public class DataFileManager {

    // Guardar una lista de objetos en un archivo .dat

    public static void guardar(String ruta, Object datos) throws PersistenciaException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ruta))) {
            oos.writeObject(datos);
        } catch (IOException e) {
            throw new PersistenciaException("Error al guardar el archivo: " + ruta, e);
        }
    }

    // Lee una lista de objetos desde un archivo .dat
    
    @SuppressWarnings("unchecked")
    public static <T> List<T> cargar(String ruta) throws PersistenciaException {
        File archivo = new File(ruta);
        if (!archivo.exists()) {
            throw new PersistenciaException("El archivo no existe: " + ruta);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ruta))) {
            return (List<T>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new PersistenciaException("Error al leer el archivo: " + ruta, e);
        }
    }
}