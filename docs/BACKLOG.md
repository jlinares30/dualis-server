# Backlog de Historias de Usuario (HUs) – Dualis Server

Este documento centraliza las Historias de Usuario del proyecto **Dualis Server**, sincronizado con el tablero de GitHub Projects.

---

## 📊 Estado de las Historias de Usuario

| ID | Historia de Usuario | Descripción / Objetivo | Estado Actual |
|---|---|---|---|
| **HU05** | Registro de Cuentas | CRUD de cuentas financieras (Débito, Crédito, Efectivo, Inversión). | ✅ **Done** (PR #17) |
| **HU07** | Registro de Transacción | Registro de ingresos, gastos y transferencias con actualización de saldos. | ✅ **Done** (PR #18) |
| **HU08** | Reglas de División (Split) | Reglas de división en pareja (50/50, Proporcional por Ingresos, %, Fijo) y cálculo de deudas. | ✅ **Done** (PR en `feature/split-engine`) |
| **HU06** | Presupuesto Mensual por Categoría | Definición de límites mensuales y control de gasto en tiempo real. | ✅ **Done** (PR en `feature/budgets`) |
| **Categorías** | Módulo de Categorías | Categorías estándar del sistema y personalizadas por espacio. | ✅ **Done** (PR en `feature/categories`) |
| **HU03** | Gestión de Espacios (Workspaces) | Creación y administración de espacios individuales y compartidos. | ✅ **Done** (PR en `feature/workspaces`) |
| **HU04** | Vinculación de Pareja | Invitación y vinculación de pareja a un workspace compartido vía código. | ✅ **Done** (PR en `feature/workspaces`) |
| **HU09** | Balance de Deudas | Vista agregada y liquidación de saldos pendientes de pareja. | ✅ **Done** (PR en `feature/settlements`) |
| **HU10** | Dashboard de Salud Financiera | Resumen de saldos, flujo de caja e indicadores financieros. | ✅ **Done** (PR en `feature/dashboard`) |
| **HU01** | Registro e Inicio de Sesión | Autenticación y registro seguro de usuarios con contraseñas encriptadas. | ✅ **Done** (PR en `feature/auth`) |
| **HU02** | Inicio de Sesión y JWT | Autenticación basada en Tokens JWT y filtro de seguridad stateless. | ✅ **Done** (PR en `feature/auth`) |
| **HU11** | Sincronización de Datos | Sincronización offline/online delta y consistencia de datos. | ✅ **Done** (Implementado en `feature/sync`) |
| **HU12** | Escaneo de Recibos (OCR) | Lectura automática de boletas/facturas para registro de gastos. | 📋 **Backlog** |
| **HU13** | Transacciones Recurrentes | Automatización de gastos fijos y suscripciones. | 📋 **Backlog** |
| **HU14** | Metas de Ahorro Compartidas | Definición y seguimiento de metas de ahorro en pareja. | 📋 **Backlog** |
| **HU15** | Notificaciones Push | Alertas de presupuestos excedidos y pagos pendientes. | 📋 **Backlog** |
| **HU16** | Exportación de Reportes | Exportación de reportes financieros. | 📋 **Backlog** |