# PRE-G9B-CU3 — Expediente técnico de cierre de `CU3-A` (preparado, no aplicado)

| Campo | Valor |
|---|---|
| Carácter | Expediente preparado para la decisión del autor. No registra ninguna aprobación: `CU3-A` y `CU3` no están `PASS — AUTHOR APPROVED` |
| Autoridad | instrucción autoral de 2026-10-27, G7 |
| Estado | pendiente de D1 (P1), del smoke autoral (D2) y de la decisión de aprobación |
| `selfApproved` | `false` |

## 1. Contenido

| # | Elemento | Estado y referencia |
|---|---|---|
| 1 | Alcance implementado de `CU3-A` | puntos estructurales `Intersect(L, r, D)` con integridad `STRICT` bajo `constructive-authority/v1`; certificado de identidad constructiva; transferencia de `CU2`; evidencia de contacto y regla de conflicto en `Σ_C`; adopción explícita y reversible ([informe del candidato](pre_g9b_cu3_implementation_candidate_report.md)) |
| 2 | Candidato | `9f6f27f265725b27673f5c37880947ad6c4f2399`, árbol `abe10e3d…` (o el sucesor de D1-A) |
| 3 | Contratos | `locus-v2-intersections.md` 1.5 (1.3, 1.4 y la enmienda `cu3-certified-admission/v2`); `locus-v2-constructive-authority.md`; ADR 0039 y 0040; presupuestos de §10 |
| 4 | Verificación integrada | `INTEGRATED_PHASE` de `9f6f27f2`, autenticada ([registro de estabilización](pre_g9b_cu3_a_stabilization_record.md) §5) |
| 5 | T1–T3, T6 y T7-a | [registro de T1 a T3](pre_g9b_cu3_performance_optimization_record.md), [registro de T6](pre_g9b_cu3_enclosure_reuse_record.md), [Puerta N](pre_g9b_cu3_t7a_gate_n_record.md), [registro de T7-a](pre_g9b_cu3_t7a_implementation_record.md) |
| 6 | P1 | caso C, decisión D1 pendiente (registro de estabilización §3) |
| 7 | Identidad e integridad | cinco incidencias `STRICT`; contactos de clase 2 sin token admisible; sin reasociación; Deshacer de la adopción restaura los tokens heredados |
| 8 | Rendimiento | arrastres en el banco E3 de unos 43–46 ms y en la configuración del smoke de unos 67–72 ms; primer arrastre del smoke de unos 1,5–1,7 s; referencia experimental, a validar por el autor |
| 9 | Compatibilidad | documentos heredados en `legacy-representation-authority/v1` y `cu3-certified-admission/v1`, sin cambios; sin formatos nuevos |
| 10 | Limitaciones y deuda | premisas A1 y A2; observaciones F1 a F6; normalización de pares por la distancia al origen, no conformidad registrada con §8.1 de `locus-v2-intersections.md`; promoción a autoridad predeterminada fuera de alcance |
| 11 | PowerShell 7.6.9 | corregido en la rama aparte `maintenance/pre-g9b-cu3-verifier-pwsh769` (`08776f45`), sin incorporar; la verificación de `CU3` usa 7.6.6 aislado |
| 12 | `CU3-B` | [hoja de decisiones R1–R6](../architecture/pre_g9b_cu3_b_decision_sheet.md), sin implementar |
| 13 | Smoke autoral | pendiente: [guía](pre_g9b_cu3_manual_test_guide.md) y [hoja de resultados](pre_g9b_cu3_a_smoke_results_sheet.md) |
| 14 | Decisiones finales | D1, el smoke y la aprobación de `CU3-A`; R1–R6 para `CU3-B` |

## 2. Separación de decisiones

| Decisión | Depende de | No implica |
|---|---|---|
| Cierre técnico de `CU3-A` | D1 resuelto y, con D1-A, el candidato nuevo verificado | aprobación autoral |
| Aprobación autoral de `CU3-A` | smoke `PASS` y rendimiento aceptado por el autor | implementación de `CU3-B` ni cierre de `CU3` |
| Implementación de `CU3-B` | R1–R6 | cierre de `CU3` |
| Cierre global de `CU3` | `CU3-A` aprobado y `CU3-B` cerrado o diferido expresamente | fases posteriores |

La continuación permite cerrar `CU3-A` y dejar `CU3-B` pendiente. Aceptar la verificación final de
un candidato parcial no supera la puerta de `CU3-B`.

## 3. Texto de aprobación propuesto (no aplicado)

> DECISIÓN AUTORAL: `CU3-A` `PASS — AUTHOR APPROVED` sobre el candidato `<SHA completo>` (árbol
> `<árbol>`), con su verificación `INTEGRATED_PHASE` autenticada y el smoke autoral `PASS` de
> `<fecha>`. Se aprueban el alcance de `CU3-A` y la política `cu3-certified-admission/v2` con la
> resolución `<D1-A | D1-B>` de P1. Se registran como limitaciones las premisas A1 y A2, las
> observaciones F1 a F6 y la deuda `DEBT-VERIFIER-PWSH-769-TESTJSON` hasta la incorporación de su
> corrección. `CU3-B` queda pendiente de R1–R6. Esta aprobación no cierra `CU3` ni autoriza fases
> posteriores, `main`, etiquetas ni publicaciones.

Este texto solo es una propuesta. No se registra ni se aplica sin la decisión expresa del autor.
