# Documento de Caso de Mejora: Refactorización y Calidad (Frente 8)

**Proyecto:** Sistema de Gestión de Academia (`prograii-sistema-gestion-academia`)  
**Módulo:** Material Didáctico y Actividades Evaluativas  
**Responsable:** Amílcar Alexander Crespín Hernández  
**Rama:** `feature/material-actividades`  

---

## 1. Justificación y Diagnóstico Técnico
Durante la Entrega 2, la gestión de materiales didácticos operaba bajo una estructura monolítica concentrada en una única clase, utilizando colecciones acopladas y excepciones genéricas de Java (`IllegalArgumentException`). Para la Entrega 3, QA y Arquitectura señalaron la necesidad de desacoplar responsabilidades, eliminar código redundante en la persistencia y estandarizar las excepciones de dominio.

---

## 2. Cuadro Comparativo de Refactorización

| Métrica / Dimensión | Entrega 2 (Antes) | Entrega 3 (Refactorizado) | Beneficio e Impacto en Calidad |
| :--- | :--- | :--- | :--- |
| **Separación de Responsabilidades (SRP)** | Clase monolítica que mezclaba modelo, persistencia en listas privadas y lógica de negocio. | Arquitectura multicapa estricta: `entity`, `repository`, `service` y `exception`. | **Alta cohesión:** Cada capa tiene un propósito único y desacoplado. |
| **Manejo de Excepciones** | Excepciones genéricas no tipadas (`IllegalArgumentException`). | Jerarquía semántica de negocio (`BusinessException`, `MaterialInvalidoException`, `ActividadInvalidaException`, etc.). | **Trazabilidad:** La capa de controladores y los tests de QA capturan fallos específicos sin ambigüedad. |
| **Reutilización y DRY** | Métodos CRUD repetidos idénticamente en cada repositorio. | Clase abstracta genérica `InMemoryRepository<T extends Identificable<ID>, ID>`. | **Mantenibilidad:** La persistencia en memoria se gestiona en un solo punto, reduciendo los repositorios a menos de 10 líneas. |
| **Retrocompatibilidad** | Cambios estructurales afectaban directamente a los consumidores. | Patrón Fachada implementado en `MaterialDidacticoService`. | **Cero regresiones:** Mantiene soporte a invocaciones previas sin alterar contratos públicos. |
