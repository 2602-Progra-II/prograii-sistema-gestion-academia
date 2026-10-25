# Frente 6: Herencia, abstracción, interfaces y polimorfismo

## 1. Jerarquía elegida: actividades evaluativas

```
            «abstract» ActividadEvaluativa  (implements Identificable<String>)
             ├── Tarea      se acepta tarde, con −20 % de la nota
             ├── Examen     no admite entregas tardías; se aprueba con 6.0
             └── Proyecto   no admite entregas tardías; +5 % si se entrega a tiempo (máx. 10)
```

**Por qué es una especialización real y no artificial:** en la academia, una tarea, un examen y un proyecto comparten los mismos datos (nombre, ponderación, fecha límite, curso), pero **se califican con reglas distintas**. Esas diferencias de comportamiento son las que justifican la herencia. No se crearon subclases solo para tener una jerarquía.

El tipo se guarda en la columna `actividad_evaluativa.tipo_actividad`. Al leer de la BD, `ActividadRepositoryImpl` vuelve a construir **la subclase correcta**, de modo que la jerarquía se usa en todo el sistema.

## 2. Abstracción

`ActividadEvaluativa` es `abstract` y declara:

- `abstract double calcularNotaFinal(double nota)`: cada subclase **está obligada** a definir su regla.
- Métodos con implementación por defecto que las subclases pueden sobrescribir: `calcularNotaFinal(double, LocalDate)`, `permiteEntregaTardia()` y `getTipo()`.
- `final double calcularAporte(double notaFinal)`: un cálculo común que **ninguna subclase debe cambiar** (nota × ponderación / 100).

## 3. Polimorfismo (dónde se ve en ejecución)

| Lugar | Llamada | Qué decide la subclase |
|---|---|---|
| `AcademicoController.entregarActividad` | `actividad.permiteEntregaTardia()` | Si una entrega vencida se acepta (Tarea) o se rechaza (Examen, Proyecto) |
| `MenuAcademico` → *Mis calificaciones* | `a.calcularNotaFinal(nota, fechaEntrega)` | Penalización, bonificación o nota tal cual |
| `TipoActividad.crear(...)` | Método abstracto por constante del enum | Qué subclase se instancia (fábrica polimórfica, sin `switch`) |
| `EstadoCurso` / `EstadoPeriodo` | `puedeTransicionarA(...)` por constante | Qué transiciones de estado son válidas |

En todos estos casos el código trabaja con una referencia de tipo `ActividadEvaluativa` (o con el enum) y **no pregunta de qué clase es el objeto**.

## 4. Interfaces usadas de forma significativa

| Interfaz | Implementaciones | Para qué sirve |
|---|---|---|
| `*Repository` (Usuario, Curso, Pago, Matricula…) | `*RepositoryImpl` (JDBC) y `RepositoriosEnMemoria` (pruebas) | Los servicios no dependen de JDBC; en las pruebas se sustituyen por versiones en memoria |
| `AutenticacionService`, `UsuarioService` | `AutenticacionServiceImpl`, `UsuarioServiceImpl` | El controller depende del contrato, no de la implementación |
| `ModuloMenu` | `MenuUsuarios`, `MenuPeriodosCursos`, `MenuAcademico`, `MenuReportes` y una lambda para `MenuCore` | El menú principal abre cualquier módulo sin conocer su clase |
| `Identificable<ID>` | `MaterialDidactico`, `ActividadEvaluativa` | Contrato genérico de entidad con id |

## 5. Colecciones y genéricos

- `List`, `Map`, `Optional<T>` y `Stream` en servicios y controllers.
- `ConcurrentHashMap`, `DoubleAdder` y `AtomicInteger` en la concurrencia.
- `Identificable<ID>` es una interfaz genérica, y `InMemoryRepository<T extends Identificable<ID>, ID>` es un repositorio genérico con tipo acotado (ver `caso_mejora_frente8.md`).
- `FlujoMatriculaService.enTransaccion(TrabajoTransaccional<T>)` es un método genérico que ejecuta cualquier trabajo dentro de una transacción y devuelve su resultado tipado.
- `ResultadoMatricula` y `ResultadoCierreCartera` son `record` usados como DTO.

## 6. Pruebas

`ActividadPolimorfismoTest` recorre una `List<ActividadEvaluativa>` con las tres subclases y verifica que **la misma llamada** dé resultados distintos según la subclase. `AcademicoControllerTest` verifica las reglas de entrega tardía de cada tipo.
