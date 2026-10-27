# Sistema de Gestión Académica

Proyecto final de **Programación II**, Ingeniería en Desarrollo de Software y Ciencia de Datos.
Es una aplicación de consola en Java para gestionar la parte académica y administrativa de una academia: usuarios y roles, períodos y cursos, matrícula con pago y factura, material didáctico, actividades, entregas y calificaciones, estados de cuenta y reportes.

**Repositorio:** https://github.com/2602-Progra-II/prograii-sistema-gestion-academia

---

## Integrantes

| Integrante | Frente principal |
|---|---|
| Edgard Granados | Auditoría, arquitectura, documentación y README |
| Roberto Gutierrez | Modelo relacional y scripts SQL |
| Fransheska Benitez | DataSource y pool de conexiones |
| José Luis Iraheta | Infraestructura JDBC y repositories |
| Edenilson Amaya | Infraestructura JDBC y repositories |
| Gerardo Gonzales | Integración funcional de módulos |
| Walter Vasquez | Integración funcional de módulos |
| Jorge Melendez | POO avanzada: herencia, abstracción, polimorfismo |
| Bryan Reyes | Concurrencia integrada con la BD |
| Amílcar Crespín | Excepciones, cohesión y calidad |
| Josue Erazo | Pruebas unitarias y de integración |

## Tecnologías

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje (records, switch expressions, streams) |
| Maven | Dependencias, compilación y pruebas |
| Motor de base de datos | Azure SQL Database |
| JDBC (`mssql-jdbc` 12.10) | Acceso a datos con `PreparedStatement` / `ResultSet` |
| HikariCP 6.3 | Pool de conexiones (`DataSource`) |
| JUnit 5 | Pruebas unitarias y de integración |
| Mermaid | Diagramas UML en `docs/uml` |

## Requisitos

- **JDK 21** o superior.
- **Maven 3.9+**, o IntelliJ IDEA, que trae Maven integrado.
- Acceso a una instancia de Azure SQL Database con la base `GestionAcademicaDB` configurada.
- Variables de entorno `DB_URL`, `DB_USER` y `DB_PASSWORD` configuradas.
- JDK compatible con la versión de Java definida en el proyecto.


## Configuración de la base de datos

La aplicación utiliza **Azure SQL Database** como único destino de conexión. No requiere una instancia local de SQL Server.

Antes de ejecutar la aplicación, configura la base de datos `GestionAcademicaDB` en Azure y ejecuta los siguientes scripts desde una herramienta compatible con Azure SQL Database, como Azure Query Editor.

| Orden | Script | Función |
|---|---|---|
| 1 | `database/azure_schema.sql` | Crea las tablas, relaciones e índices necesarios para el sistema. |
| 2 | `database/azure_data.sql` | Inserta los datos iniciales de demostración. Ejecutar solo cuando corresponda cargar esos datos. |
| 3 | `database/azure_usuario_app.sql` | Crea el usuario de aplicación y le asigna permisos de lectura y escritura. Ejecutar una sola vez, sustituyendo el marcador de contraseña por una clave segura. |

**Precaución:** antes de ejecutar los scripts de creación o carga, verifica su contenido y el estado de la base de datos para evitar duplicar registros o afectar datos existentes.

### Configuración de la conexión

La clase `DataSourceConfig` obtiene las credenciales exclusivamente de estas variables de entorno:

| Variable | Valor |
|---|---|
| `DB_URL` | Cadena JDBC de Azure SQL Database. |
| `DB_USER` | Usuario de aplicación de la base de datos. |
| `DB_PASSWORD` | Contraseña del usuario de aplicación. |

Las tres variables son obligatorias. La aplicación no utiliza valores predeterminados ni credenciales incorporadas en el código.

Configura estas variables en la configuración de ejecución de IntelliJ IDEA o en el entorno desde el que ejecutes la aplicación.

La variable `DB_URL` debe contener la cadena JDBC del servidor Azure correspondiente y especificar la base de datos `GestionAcademicaDB`, utilizando cifrado de conexión.

**Seguridad:**
- No publiques contraseñas ni cadenas de conexión con credenciales en el repositorio.
- No incluyas contraseñas reales en los scripts SQL.
- Utiliza un usuario de aplicación con los permisos mínimos necesarios.

## Ejecución

**IntelliJ IDEA:** abre la carpeta como proyecto Maven y ejecuta `sv.gestionacademica.Main`.

**Consola:**
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass=sv.gestionacademica.Main
```

Al iniciar, el sistema pide **email y contraseña** y muestra el menú que corresponde al rol. Para salir, deja el email vacío.

### Credenciales de prueba (`data.sql`)

| Rol | Email | Contraseña |
|---|---|---|
| Administrador | `admin@academia.edu` | `Admin123` |
| Profesor | `ana.garcia@academia.edu` | `Prof123` |
| Estudiante | `roberto.gutierrez@academia.edu` | `Est123` |
| Estudiante | `fransheska.perez@academia.edu` | `Est123` |

### Qué puede hacer cada rol

- **Administrador:**
  - Gestiona usuarios (registrar, activar, desactivar, asignar rol).
  - Gestiona períodos y cursos (activar, finalizar, asignar instructor).
  - Matricula estudiantes y aprueba o rechaza pagos; al aprobar se emite la factura.
  - Accede a material y calificaciones de todos los cursos.
  - Consulta reportes: estado de cuenta, mora y facturas.
  - Ejecuta el cierre de cartera concurrente.
- **Profesor:** ve sus cursos, publica material, crea actividades (tarea, examen o proyecto), revisa entregas y califica.
- **Estudiante:** ve sus matrículas y su estado de cuenta, consulta el material y las actividades de sus cursos, entrega actividades y ve sus calificaciones.

### Recorrido de demostración

1. **Administrador** → *Matrícula, pagos y facturas*: matricula a Fransheska en Cálculo Diferencial y aprueba el pago. En una sola transacción, la matrícula pasa a activa y se emite la factura.
2. **Profesor (Ana)** → *Material, actividades y calificaciones*: publica material y crea un examen.
3. **Estudiante (Roberto)** → entrega la *Tarea 1*. Ya venció, así que se acepta con penalización del 20 %. Un examen vencido, en cambio, se rechaza.
4. **Ana** califica la entrega. **Roberto** → *Mis calificaciones* muestra la nota, la nota final según el tipo y su aporte al curso.
5. **Administrador** → *Reportes* y *Cierre de cartera concurrente*.

## Arquitectura

```
Main → MenuPrincipal (login + menú por rol) → Menús (ModuloMenu)
     → Controllers → Services (reglas de negocio) → Repositories (interfaces)
     → *RepositoryImpl (JDBC) → DataSource (HikariCP) → SQL Server
```

| Paquete | Responsabilidad |
|---|---|
| `menu`, `MenuCore` | Presentación por consola, sin reglas de negocio |
| `controller` | Entrada a los casos de uso; traduce excepciones a mensajes |
| `service` | Reglas de negocio, transacciones y concurrencia |
| `repository` | Interfaces de acceso a datos y sus implementaciones JDBC |
| `persistence` | `DataSourceConfig` (pool) |
| `entity`, `enums`, `dto`, `exception` | Modelo de dominio, estados, resultados y errores |

Diagramas en `docs/uml` (Mermaid; GitHub los dibuja directamente):

| Archivo | Contenido |
|---|---|
| `01-clases-dominio.mmd` | Modelo de dominio |
| `02-clases-academico.mmd` | Módulo académico y jerarquía de actividades |
| `03-clases-financiero.mmd` | Flujo transaccional, estado de cuenta y concurrencia |
| `04-clases-autenticacion.mmd` | Login y menús |
| `05-modelo-relacional.mmd` | Modelo relacional |
| `06-modelo-arquitectura.mmd` | Arquitectura por capas |

## Conceptos aplicados

| Concepto | Dónde |
|---|---|
| Herencia y clase abstracta | `ActividadEvaluativa` → `Tarea`, `Examen`, `Proyecto` (`docs/poo-herencia-polimorfismo.md`) |
| Polimorfismo | `calcularNotaFinal`, `permiteEntregaTardia`; fábrica `TipoActividad.crear`; enums con métodos por constante |
| Interfaces | Repositories, `AutenticacionService`, `UsuarioService`, `ModuloMenu`, `Identificable<ID>` |
| Colecciones y genéricos | `List`, `Map`, `Optional`, `ConcurrentHashMap`, `InMemoryRepository<T, ID>`, `enTransaccion(TrabajoTransaccional<T>)` |
| Transacciones JDBC | `FlujoMatriculaService`: matrícula → pago → factura con `commit`/`rollback` y bloqueo `UPDLOCK` |
| Excepciones | `BusinessException` (negocio), `TransaccionException` y `PersistenciaException` (técnicas) |
| Calidad | `docs/caso_mejora_frente8.md` y `docs/caso_mejora_integracion.md` (antes/después) |

## Concurrencia

**Cierre de cartera** (`ProcesoConcurrenteService`, menú Administrador, opción 6): recalcula el estado de cuenta de **todos** los estudiantes en paralelo.

- Un `ExecutorService` de 4 hilos ejecuta una tarea por estudiante.
- Cada tarea pide **su propia conexión** al `DataSource` y la devuelve con try-with-resources.
- Los resultados se juntan en un `ConcurrentHashMap` y los totales en `DoubleAdder` / `AtomicInteger`, para evitar condiciones de carrera.
- El error de un estudiante no detiene a los demás.

Detalle en `docs/frente-7-concurrencia.md`.

## Pruebas

```bash
mvn test
```

| Tipo | Clases | Requiere BD |
|---|---|---|
| Unitarias, con repositories en memoria (`src/test/.../support`) | `AutenticacionServiceImplTest`, `AcademicoControllerTest`, `ActividadPolimorfismoTest`, `EstadoCuentaDesdeBDTest`, `CierreCarteraConcurrenteTest`, `EstadoCuentaServiceTest`, `ReporteServiceTest` | No |
| Integración (JDBC real) | `FlujoMatriculaServiceTest`, `ProcesoConcurrenteServiceTest`, `UsuarioServiceTest`, `CursoServiceTest`, `PeriodoAcademicoServiceTest`, `MaterialServiceTest`, `ActividadServiceTest`, `EvaluacionServiceTest`, `PagoServiceTest`, `FacturaServiceTest` | Sí |

Para correr solo las pruebas unitarias, que no necesitan SQL Server:
```bash
mvn test -Dtest="AutenticacionServiceImplTest,AcademicoControllerTest,ActividadPolimorfismoTest,EstadoCuentaDesdeBDTest,CierreCarteraConcurrenteTest,EstadoCuentaServiceTest,ReporteServiceTest"
```

> ⚠️ **Varias pruebas de integración borran tablas de la BD** para empezar desde un estado conocido. Después de `mvn test`, vuelve a ejecutar `schema.sql` y `data.sql` antes de usar la aplicación o hacer la demo.


## Prueba desde cero (checklist)

Esta guía permite preparar el proyecto y ejecutar la aplicación utilizando la base de datos alojada en Azure SQL.

1. **Clonar el repositorio**

   ```bash
   git clone https://github.com/2602-Progra-II/prograii-sistema-gestion-academia.git
   ```

2. **Preparar el entorno de desarrollo**

   Abrir el proyecto en IntelliJ IDEA, verificar que esté configurado el JDK requerido y ejecutar:

   ```bash
   mvn clean compile
   ```

   Maven descargará las dependencias necesarias y compilará el proyecto.

3. **Configurar las variables de entorno**

   Configurar las siguientes variables de entorno en la configuración de ejecución de IntelliJ IDEA:

   | Variable | Descripción |
   |---|---|
   | `DB_URL` | jdbc:sqlserver://sql-gestion-academica-uees.database.windows.net:1433;databaseName=GestionAcademicaDB;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;|
   | `DB_USER` | academia_app|
   | `DB_PASSWORD` | PrograII2026 |

   Estas variables permiten que la aplicación se conecte a la base de datos sin almacenar credenciales directamente en el código fuente.

4. **Verificar la conectividad con Azure SQL**

   Confirmar que el servidor de Azure SQL permita conexiones desde la dirección IP pública del equipo donde se ejecutará el programa y que el usuario configurado tenga los permisos necesarios.

5. **Ejecutar la aplicación**

   Ejecutar la clase principal `Main` desde IntelliJ IDEA y comprobar que la aplicación se conecta a Azure SQL y permite realizar el recorrido de demostración.

6. **Ejecutar las pruebas automatizadas**

   Desde la terminal, ejecutar:

   ```bash
   mvn test
   ```

   Revisar los resultados y verificar que las pruebas que requieren conexión a Azure SQL se ejecuten con las variables de entorno y la conectividad correctamente configuradas.

> **Importante:** La base de datos de Azure debe estar preparada previamente para la evaluación. No ejecutar scripts de creación de tablas o carga de datos sobre la base compartida sin verificar antes su propósito y sus posibles efectos.

