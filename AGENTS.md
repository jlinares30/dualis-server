# AGENTS.md — Dualis Server Guidelines & Context

Documento normativo y de contexto para agentes de IA y desarrolladores en **Dualis Server**. Establece estándares de arquitectura, convenciones, patrones, prohibiciones y flujos de trabajo.

---

## 1. Visión y Propósito del Producto

**Dualis** es una plataforma inteligente de gestión financiera personal y en pareja (web/móvil) que actúa como copiloto o mentor adaptativo.

* **Enfoque Individual:** Presupuestos dinámicos, clasificación de gastos (Indispensable vs. No Indispensable), estrategias de deuda y metas personalizables según modos de vida (*Ahorro*, *Normal*, *Crecimiento*).
* **Enfoque en Pareja:** Espacios compartidos (*Workspaces*) con reglas de repartición flexibles (Proporcional a ingresos, 50/50, Montos fijos) manteniendo la privacidad y autonomía individual.
* **Visión a Futuro:** Proyecciones inteligentes, análisis de flujo de caja y modelos de Machine Learning.

---

## 2. Stack Tecnológico

* **Lenguaje:** Java 21 LTS
* **Framework:** Spring Boot 3.4.x (Web, Security, Data JPA, Validation)
* **Base de Datos:** PostgreSQL 16
* **Migraciones de Esquema:** Flyway (`flyway-database-postgresql`)
* **Autenticación & Autorización:** JWT (`jjwt 0.12.6`), Spring Security
* **Documentación API:** SpringDoc OpenAPI v2.8.5 (Swagger UI en `/swagger-ui.html`)
* **Productividad & Boilerplate:** Project Lombok (1.18.x)
* **Testing:** JUnit 5, Mockito, Spring Security Test, Spring Boot Test
* **Contenedores:** Docker, Docker Compose
* **Gestor de Construcción:** Maven Wrapper (`./mvnw`)
* **Frontend Relacionado:** Next.js (TypeScript, Tailwind CSS)

---

## 3. Estructuras de Proyecto

El backend implementa **Clean Architecture / Hexagonal Architecture** modularizada por vertical de dominio (**DDD-Lite** / *Package-by-Feature*).

```
src/main/java/com/dualis/api/
├── config/                     # Configuraciones globales (Security, OpenApi, CORS, Auditing)
├── exception/                  # Manejo global de excepciones (GlobalExceptionHandler, ErrorResponse)
├── shared/                     # Value Objects, DTOs y utilidades comunes compartidas
│   ├── domain/valueobject/     # Money, Currency, etc.
│   └── dto/response/           # ApiResponse, PageResponse
└── modules/                    # Módulos verticales desacoplados por Bounded Context
    ├── account/
    ├── analytics/
    ├── auth/
    ├── budget/
    ├── category/
    ├── investment/
    ├── savingsgoal/
    ├── settlement/
    ├── subscription/
    ├── sync/
    ├── transaction/
    └── workspace/
        ├── controller/                 # Adaptadores de entrada REST (Controllers)
        ├── dto/                        # Contratos de entrada y salida
        │   ├── request/                # DTOs validados con @Valid
        │   └── response/               # DTOs de salida enriquecidos
        ├── application/                # Casos de uso y orquestación
        │   ├── usecase/                # Interfaces de casos de uso (Ports In)
        │   └── service/                # Implementaciones del servicio de aplicación
        ├── domain/                     # Núcleo del negocio (Puro, desacoplado)
        │   ├── model/                  # Entidades de dominio ricas
        │   └── repository/             # Interfaces de persistencia (Ports Out)
        └── infrastructure/             # Adaptadores de salida (BBDD, mensajería, etc.)
            └── adapter/out/persistence/
                ├── entity/             # Entidades JPA (@Entity, @Table)
                ├── repository/         # Spring Data JPA Repositories
                └── mapper/             # Mapeo JPA Entity <-> Domain Model
```

---

## 4. Convenciones de Código

* **Nomenclatura:**
  * Clases y tipos en `PascalCase` (`TransactionApplicationService`, `BudgetRepository`).
  * Métodos, variables y atributos en `camelCase` (`userId`, `calculateBalance()`).
  * Constantes y enumeraciones en `UPPER_SNAKE_CASE` (`SPLIT_RULE_50_50`, `TRANSACTION_TYPE`).
  * Endpoints REST en minúsculas con guiones o plural (`/api/v1/workspaces/{workspaceId}/transactions`).
* **Lombok:**
  * Usar `@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`.
  * Evitar `@Data` en entidades de persistencia JPA para prevenir problemas con proxies (`equals`/`hashCode`/`toString` cíclicos).
* **Inmutabilidad y DTOs:**
  * DTOs preferiblemente modelados como `record` o clases con `@Getter` y `@Builder`.
  * Parámetros de entrada validados exhaustivamente con Bean Validation (`@NotNull`, `@NotBlank`, `@Positive`, etc.).
* **Manejo de Errores:**
  * Lanzar excepciones de negocio personalizadas (`ResourceNotFoundException`, `BusinessRuleException`).
  * Todo error debe ser transformado a una respuesta uniforme a través de `GlobalExceptionHandler`.
* **Persistencia & Migraciones:**
  * Esquema versionado estrictamente con scripts Flyway en `src/main/resources/db/migration/` con formato `V{version}__{descripcion}.sql`.
  * Jamás usar `hibernate.hbm2ddl.auto=create` o `update` en entornos compartidos ni producción.

---

## 5. Patrones

* **Hexagonal / Ports and Adapters:** El dominio define contratos (`Ports`) que la infraestructura implementa (`Adapters`).
* **Pragmatic DDD-Lite:** 
  * Priorizar separación limpia de capas en lógica de negocio compleja (liquidaciones, prorrateo, presupuestos dinámicos).
  * Evitar sobreingeniería o mappings dobles innecesarios JPA-Dominio en operaciones CRUD triviales si no aportan valor.
* **Separación Request / Response:** Jamás exponer entidades JPA directas en los endpoints REST; usar siempre DTOs dedicados.
* **Single Responsibility (SRP):** Casos de uso atómicos por acción o servicios de aplicación bien acotados por entidad raíz.

---

## 6. Prohibiciones

* ❌ **NO exponer Entidades JPA en Controllers:** Todo request/response pasa por DTOs.
* ❌ **NO modificar migraciones Flyway ya aplicadas:** Cualquier cambio de base de datos requiere una nueva migración con número correlativo.
* ❌ **NO acoplar módulos directamente a nivel de infraestructura:** Si un módulo necesita interactuar con otro, debe hacerlo a través del caso de uso / servicio de aplicación o eventos, nunca llamando directamente a los repositorios JPA privados de otro módulo.
* ❌ **NO usar lógica de negocio en Controllers:** Los controladores se limitan a recibir requests, validar, invocar la capa de aplicación y mapear respuestas HTTP.
* ❌ **NO commitear secretos ni credenciales:** Las claves JWT, credenciales de DB y API keys deben gestionarse por variables de entorno (`application.yml` parametrizado).
* ❌ **NO saltarse validaciones de seguridad o pertenencia a Workspace:** Toda operación sobre un recurso compartido debe validar que el usuario autenticado tiene permisos sobre el espacio o recurso.

---

## 7. Flujo de Trabajo

* **Branching Strategy:** GitHub Flow / ramas cortas basadas en feature:
  * Formato: `feature/huXX-descripcion` o `fix/huXX-descripcion`.
* **Gestión de Tareas:**
  * Tareas vinculadas a GitHub Issues en el tablero Kanban de GitHub Projects.
  * Cierre automatizado de tareas vinculando los PRs (`Closes #<issue_number>`).
* **Modularidad:**
  * Cada módulo/entidad se desarrolla de forma aislada e independiente para minimizar conflictos en git.
* **Directrices de Interacción para el Agente:**
  * Respuestas concisas, claras y orientadas a la acción (código listo para implementar, DTOs y scripts SQL limpios).
  * Sin rodeos teóricos innecesarios.

---

## 8. Testing, CI/CD

* **Testing:**
  * **Unit Tests:** Cobertura de lógica de negocio en capa de dominio y servicios con JUnit 5 + Mockito.
  * **Controller Tests:** Verificación de contratos REST, estatus HTTP y validaciones con `@WebMvcTest` y `MockMvc`.
  * **Ejecución local:** `./mvnw clean test`
* **Contenedores para pruebas e integración:** Docker Compose para desplegar PostgreSQL localmente:
  * `docker compose up -d`
* **CI/CD:**
  * Validación automática en Pull Requests (compilación, pruebas unitarias y verificación de formato).
  * Empaquetado en contenedor Docker multi-stage (`Dockerfile`).

---

## 9. Estilo de Commits y PRs

* **Conventional Commits:**
  * `feat(<modulo>): <descripcion en minuscula>` (ej. `feat(transactions): support cross-currency transfers`)
  * `fix(<modulo>): <descripcion>` (ej. `fix(settlement): correct debt calculation by payer`)
  * `refactor(<modulo>): <descripcion>`
  * `test(<modulo>): <descripcion>`
  * `docs: <descripcion>`
  * `chore: <descripcion>`
* **Pull Requests:**
  * Título conciso siguiendo Conventional Commits.
  * Resumen de cambios técnicos y de dominio.
  * Instrucciones de prueba y referencia al Issue vinculado (`Closes #123`).
