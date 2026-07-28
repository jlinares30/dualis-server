# Backlog de Historias de Usuario (HUs) – Dualis

Este documento centraliza las Historias de Usuario del proyecto **Dualis**, organizadas por fases para el desarrollo del Producto Mínimo Viable (MVP) y futuras extensiones.

---

## Fase 1: MVP (Core Funcional)

| ID | Historia de Usuario | Descripción / Objetivo | Criterio de Aceptación Clave |
|---|---|---|---|
| **HU01** | Autenticación y Registro | Registro e inicio de sesión de usuarios de forma segura. | JWT generado correctamente, contraseñas encriptadas. |
| **HU02** | Gestión de Perfil | Administración de datos personales, moneda preferida y preferencias. | Permite actualizar perfil y configurar moneda base. |
| **HU03** | Creación de Espacios | Crear espacios Financieros (Individuales o Compartidos en pareja). | Creación de espacios e invitación a pareja vía correo/código. |
| **HU04** | Configuración de Ingresos | Registro de ingresos recurrentes/fijos para cálculo proporcional. | Permite definir ingresos mensuales para reglas de reparto. |
| **HU05** | Gestión de Cuentas | CRUD de cuentas financieras (Débito, Crédito, Efectivo, Inversión). | Modificación de saldos iniciales y asociación a Espacios. |
| **HU06** | Registro de Transacciones | Registro de ingresos, gastos y transferencias entre cuentas. | Categorización (Indispensable / No Indispensable). |
| **HU07** | Motor de Repartición (Split) | Aplicación de reglas de división de gastos en pareja. | Reglas de división: 50/50, proporcional o montos fijos. |
| **HU08** | Dashboard Básico | Visualización del resumen de saldos, gastos del mes y estado actual. | Gráficos simples e indicadores de flujo de caja del espacio. |

---

## Fase 2: Post-MVP (Features Avanzados & ML)

| ID | Historia de Usuario | Descripción / Objetivo |
|---|---|---|
| **HU09** | Transacciones Recurrentes | Automatización de gastos fijos y suscripciones. |
| **HU10** | Módulo de Metas de Ahorro | Definición y seguimiento de metas individuales y en pareja. |
| **HU11** | Modos de Ahorro Gamificados | Alternancia entre modos (Ahorro, Normal, Crecimiento) con ajustes de presupuesto. |
| **HU12** | OCR para Comprobantes | Escaneo automático de boletas/facturas para registro de gastos. |
| **HU13** | Notificaciones Push | Alertas de presupuestos excedidos, pagos pendientes o aportes de la pareja. |
| **HU14** | Reportes Avanzados | Exportación de reportes financieros en PDF y Excel. |
| **HU15** | Copiloto Financiero (IA) | Recomendaciones personalizadas basadas en patrones de gasto e inversión. |
| **HU16** | Proyecciones y Flujo de Caja | Análisis predictivo del saldo futuro según comportamiento financiero. |