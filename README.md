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
| SQL Server / SQL Server Express | Base de datos relacional |
| JDBC (`mssql-jdbc` 12.10) | Acceso a datos con `PreparedStatement` / `ResultSet` |
| HikariCP 6.3 | Pool de conexiones (`DataSource`) |
| JUnit 5 | Pruebas unitarias y de integración |
| Mermaid | Diagramas UML en `docs/uml` |

## Requisitos

- **JDK 21** o superior.
- **Maven 3.9+**, o IntelliJ IDEA, que trae Maven integrado.
- **SQL Server 2019+** o SQL Server Express, con la **autenticación mixta** habilitada (SQL Server y Windows).
- SQL Server Management Studio o Azure Data Studio para ejecutar los scripts.

## Configuración de la base de datos

Ejecuta los scripts de `database/` **en este orden**, con una cuenta administradora (por ejemplo `sa`):

| # | Script | Qué hace |
|---|---|---|
| 1 | `schema.sql` | Crea la BD `sistema_gestion_academia` y todas las tablas. Se puede volver a ejecutar: borra y recrea todo. |
| 2 | `usuario_app.sql` | Crea el login `academia_app` con el que se conecta la aplicación. Solo hace falta la primera vez. |
| 3 | `data.sql` | Carga los datos de prueba: roles, usuarios, período, cursos, matrículas, actividades y pagos. |

### Conexión

`DataSourceConfig` lee estas variables de entorno. Si no existen, usa los valores por defecto:

| Variable | Valor por defecto |
|---|---|
| `DB_URL` | `jdbc:sqlserver://localhost:1433;databaseName=sistema_gestion_academia;encrypt=true;trustServerCertificate=true;` |
| `DB_USER` | `academia_app` |
| `DB_PASSWORD` | `Academia2026_Segura!` |

Si tu instancia es SQL Express con nombre (por ejemplo `localhost\SQLEXPRESS`), define `DB_URL` con ese host o habilita TCP/IP en el puerto 1433.

**Pool (HikariCP):** máximo 10 conexiones, mínimo 2 inactivas, espera de 30 s, inactividad de 10 min y vida máxima de 30 min. La justificación está en `docs/documentos/datasource-pool.md 3`. Para verificar la conexión, ejecuta `sv.gestionacademica.persistence.DataSourceCheck`.

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

1. `git clone https://github.com/2602-Progra-II/prograii-sistema-gestion-academia.git`
2. Abrir el proyecto en IntelliJ o ejecutar `mvn clean compile`. Maven descarga las dependencias.
3. Ejecutar `schema.sql`, `usuario_app.sql` y `data.sql`.
4. Ejecutar `DataSourceCheck` y confirmar que aparece *Conexión OK* tres veces.
5. Ejecutar `Main` y seguir el recorrido de demostración.
6. Ejecutar `mvn test` y luego volver a ejecutar `schema.sql` y `data.sql`.
