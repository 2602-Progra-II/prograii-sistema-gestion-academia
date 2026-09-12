# Guía de Pruebas Automatizadas y Workflow de QA - Proyecto Academia

Este documento establece el flujo de trabajo de desarrollo, las reglas de QA y las instrucciones para ejecutar y agregar pruebas en el proyecto **Sistema de Gestión Academia**.

---

## 📌 Flujo de Trabajo para el Equipo (Workflow)

Para mantener la calidad y el orden en el repositorio, todos los desarrolladores deben seguir este proceso:

1. **Desarrollar en rama propia:** Crea tu rama desde `main` (ejemplo: `feature/matricula` o `feature/cursos`).
2. **Escribir código y pruebas unitarias:** Cada nueva funcionalidad en `src/main/java/com/academia/` debe incluir su archivo de prueba correspondiente en `src/test/java/com/academia/`.
3. **Validar localmente:** Compila y ejecuta la suite de pruebas en tu consola antes de subir cambios. Todas las pruebas deben pasar al 100%.
4. **Abrir Pull Request (PR):** Sube tu rama a GitHub y abre un PR hacia `main`.
5. **Revisión de QA:** El responsable de QA revisará las pruebas de integración, ejecutará la suite completa y aprobará el merge a `main`.

---

## 📝 Guía para Desarrolladores: ¿Cómo agregar una prueba unitaria?

Si estás programando una nueva clase (por ejemplo, `MatriculaService.java`):

1. **Ubicación del test:** Crea un archivo llamado `MatriculaServiceTest.java` en la carpeta `src/test/java/com/academia/`.
2. **Estructura base del test:** Usa la siguiente plantilla básica con JUnit 5:

```java
package com.academia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class MatriculaServiceTest {

    private MatriculaService matriculaService;

    @BeforeEach
    void setUp() {
        matriculaService = new MatriculaService();
    }

    @Test
    @DisplayName("Debe matricular un estudiante correctamente")
    void testMatricularEstudianteExitoso() {
        // 1. Preparar datos
        // 2. Ejecutar método
        // 3. Validar resultado con assertTrue, assertEquals, etc.
    }
}