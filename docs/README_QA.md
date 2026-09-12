# Guía de Pruebas Automatizadas y QA - Proyecto Academia

Este documento detalla la estructura, configuración y comandos necesarios para compilar y ejecutar la suite de pruebas automatizadas del proyecto **Sistema de Gestión Academia**.

---

## 🛠️ Estructura de Pruebas

El entorno utiliza **JUnit 5 (Console Standalone)** para la ejecución de pruebas unitarias y de integración sin dependencia estricta de un IDE.

* **`AutenticacionServiceTest.java`**: Pruebas unitarias para la validación de credenciales, logins fallidos y manejo de entradas nulas/vacías.
* **`UsuarioServiceTest.java`**: Pruebas unitarias para el registro de usuarios, control de duplicados y consultas de cantidad.
* **`PersistenciaTest.java`**: Pruebas unitarias para el almacenamiento, consulta, eliminación y limpieza de registros simulados.
* **`IntegrationTest.java`**: Pruebas de integración que verifican la interacción entre los módulos del sistema.

---

## 🚀 Comandos de Ejecución (PowerShell)

Asegúrate de ejecutar los siguientes comandos desde la raíz del proyecto (`prograii-sistema-gestion-academia`).

### 1. Compilación del proyecto y pruebas
```powershell
javac -cp "lib/*" -d bin src/main/java/com/academia/*.java src/test/java/com/academia/*.java