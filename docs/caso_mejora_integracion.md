# Caso de mejora 2: integración final (antes / después)

Complementa `caso_mejora_frente8.md`. Son mejoras de legibilidad, reutilización y cohesión aplicadas en la rama `feature/core-integracion`.

## 1. Mapeo duplicado → método extraído (`UsuarioRepositoryImpl`)

**Antes.** `buscarPorId` y `listarTodos` repetían el mismo bloque de unas 15 líneas que convierte un `ResultSet` en `Usuario`. Al agregar el email, había que cambiarlo en dos lugares, y un olvido producía usuarios con datos distintos según la consulta.

```java
// repetido en buscarPorId y en listarTodos
Rol rol = new Rol(rs.getInt("id_rol"), rs.getString("nombre_rol"), rs.getString("descripcion"));
boolean activo = rs.getBoolean("activo");
EstadoUsuario estado = activo ? EstadoUsuario.ACTIVO : EstadoUsuario.INACTIVO;
return new Usuario(rs.getInt("id_usuario"), rs.getString("nombre"), ...);
```

**Después.** Un solo método privado, `mapearUsuario(ResultSet)`, que usan `buscarPorId`, `buscarPorEmail` y `listarTodos`.

**Beneficio:** un único punto de cambio (DRY). Gracias a eso, el email del login se agregó en una sola línea.

## 2. Reflexión frágil → contrato explícito (`MaterialRepositoryImpl`, `ActividadRepositoryImpl`)

**Antes.** Las entidades no tenían `idCurso`. El repositorio lo buscaba por reflexión (`getMethod("getIdCurso")`, `getDeclaredField("idCurso")`) y, al no encontrarlo, **guardaba todo en el primer curso de la BD** sin avisar. Era un error de datos silencioso.

**Después.** `MaterialDidactico` y `ActividadEvaluativa` tienen `getIdCurso()` y `setIdCurso()`, y el repositorio los usa directamente. `AcademicoController` siempre asigna el curso elegido.

**Beneficio:** el compilador verifica el contrato, el código se entiende al leerlo y el material queda en el curso correcto.

## 3. Stubs → responsabilidades en la capa correcta (`AcademicoController`)

**Antes.** `publicarMaterial()`, `crearActividad()`, `entregarActividad()` y `calificarActividad()` lanzaban `UnsupportedOperationException`.

**Después.**
- Las reglas de permisos están centralizadas en `exigirInstructorDelCurso()` y las comparten publicar, crear y calificar.
- Las reglas de entrega (matrícula activa, fecha límite según el tipo, reentrega mientras no esté calificada) están en `entregarActividad()`.
- Las reglas propias de cada tipo de actividad viven en sus subclases (`permiteEntregaTardia`, `calcularNotaFinal`), no en `if` dentro del controller.
- El menú (`MenuAcademico`) solo lee datos y muestra resultados.

**Beneficio:** alta cohesión, porque cada clase tiene una sola razón para cambiar. Las reglas se prueban sin consola ni BD (`AcademicoControllerTest`).

## 4. Errores técnicos y errores de negocio

| Tipo | Excepción | Dónde | Cómo se muestra |
|---|---|---|---|
| Regla de negocio | `BusinessException` | Servicios y controllers | `[Regla de negocio] ...` |
| Fallo de una transacción | `TransaccionException` | `FlujoMatriculaService` (con rollback) | `[Error técnico] ...` |
| Fallo de acceso a datos | `PersistenciaException` (ahora unchecked) | `ProcesoConcurrenteService` | `[Error] ...` |
