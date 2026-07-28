### 1. Visión y Propósito del Producto
Dualis es una plataforma inteligente de gestión financiera personal y en pareja (web/móvil) que actúa como un copiloto o mentor adaptativo.
* Enfoque Individual: Presupuestos dinámicos, clasificación de gastos (Indispensable vs. No Indispensable), estrategias de deuda y metas personalizables según modos de vida (Ahorro, Normal, Crecimiento).
* Enfoque en Pareja: Espacios compartidos con reglas de repartición flexibles (Proporcional a ingresos, 50/50, Montos fijos) manteniendo la autonomía individual.
* Visión a futuro: Proyecciones, análisis de flujo de caja y modelos de Machine Learning.

---

### 2. Stack Tecnológico y Arquitectura
* Backend: Java 21 LTS, Spring Boot 4.1.0, PostgreSQL 16, Flyway (migraciones), Lombok.
* Frontend: Next.js.
* Estilo Arquitectónico: Clean Architecture / Layered Architecture modularizada por dominio (DDD-Lite / Strategic DDD).
* Principio de Diseño: Enfoque pragmático. Evitar sobreingeniería o mappings innecesarios JPA-Dominio en CRUDs simples. Priorizar código limpio, mantenible y listo para producción sin frenar la velocidad del MVP.

---

### 3. Workflow y Buenas Prácticas de Trabajo
* Control de Versiones: Git Flow / GitHub Flow con ramas cortas por feature (`feature/huXX-nombre`).
* Gestión de Proyectos: GitHub Projects (Kanban). Las HUs están vinculadas a Issues y PRs. Las HUs se cierran automáticamente mediante PRs usando palabras clave (`closes #Issue`).
* Modularidad: Cada módulo/entidad se desarrolla de forma aislada e independiente.

---

### 4. Directrices de Respuesta
* Respuestas concisas, claras y orientadas a la acción (código listo para implementar, scripts Flyway y DTOs bien estructurados).
* Explicaciones breves sin rodeos teóricos innecesarios.