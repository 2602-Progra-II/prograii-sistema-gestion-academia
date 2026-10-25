package sv.gestionacademica.menu;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Lectura segura de datos por consola. Todos los menus comparten la MISMA instancia
 * (y por lo tanto el mismo Scanner): varios Scanner sobre System.in se roban la entrada.
 */
public class Consola {

    private final Scanner in;

    public Consola(Scanner in) {
        this.in = in;
    }

    public Scanner getScanner() {
        return in;
    }

    public String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return in.nextLine().trim();
    }

    public String leerTextoObligatorio(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println("Este dato es obligatorio.");
        }
    }

    public int leerEntero(String mensaje) {
        while (true) {
            try {
                return Integer.parseInt(leerTexto(mensaje));
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un número entero válido.");
            }
        }
    }

    public LocalDate leerFecha(String mensaje) {
        while (true) {
            try {
                return LocalDate.parse(leerTexto(mensaje));
            } catch (DateTimeParseException e) {
                System.out.println("Formato de fecha inválido. Usa AAAA-MM-DD (ejemplo: 2026-02-01).");
            }
        }
    }

    public boolean confirmar(String mensaje) {
        String r = leerTexto(mensaje + " (s/n): ");
        return r.equalsIgnoreCase("s") || r.equalsIgnoreCase("si") || r.equalsIgnoreCase("sí");
    }
}
