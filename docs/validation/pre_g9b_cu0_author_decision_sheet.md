# PRE-G9B-CU0 — Hoja de decisiones autorales

| Campo | Valor |
|---|---|
| Carácter | Hoja breve con las decisiones necesarias para autorizar `CU1` y preparar `CU2`/`CU3`. No registra ninguna decisión tomada por el agente |
| Estado | `PENDING AUTHOR DECISIONS`; `selfApproved=false` |
| Contexto | [plan consolidado](../architecture/pre_g9b_cu_corrective_usability_plan.md); [informe del candidato CU0](pre_g9b_cu0_baseline_candidate_report.md) |

## 1. Ya decidido por la instrucción de 2026-10-19 (no requiere nueva decisión)

Marco `PRE-G9B-CU` aprobado con enmiendas; `CU0` autorizado; DA-1 dirección híbrida;
DA-3 opción C por etapas con los tres casos obligatorios; DA-5 sin coincidencia de
coordenadas; M07 elevada en `CU6`; directrices de B03 y H02; direcciones DC-1, DC-3 y
DC-4; DC-6 con límites justificados por pruebas; `CU8` solo diseño; libro en pausa.

## 2. Para autorizar `CU1`

| Id | Decisión | Opciones | Recomendación |
|---|---|---|---|
| C1-0 | Aceptación del candidato `CU0` y commit base de `CU1` | el commit candidato `CU0` (o su publicación, si se decide) | aceptar `CU0` y usar su commit como base |
| C1-1 | Puertas de aceptación de B10.1 (`DT-1`) | por contadores deterministas, con tiempos informativos | (a) 0 reconstrucciones del índice métrico anidado por consulta puntual tras el primer cálculo de cada revisión; (b) ninguna revisión publicada al mover solo el conductor; (c) como máximo una construcción de corte por revisión; (d) informativo: arrastre con `e_2` ≤ 2 × sin `e_2` (hoy ≈ 5,7 s frente a ≈ 75 ms por paso) y carga ≤ 3 s (hoy ≈ 15 s) |
| C1-2 | Incluir en `CU1` la conformidad métrica (H10 P0, H11 P1, H12 P2) con la política métrica v2 ([A1.2](../architecture/pre_g9b_cu0_candidate_normative_amendments.md)) | incluir / paquete aparte | incluir: comparte el índice métrico que `CU1` reorganiza y H10 da valores erróneos sin aviso |
| C1-3 | Clase de verificación de `CU1` | `INTEGRATED_PHASE` / `GLOBAL_IMPACT` | `INTEGRATED_PHASE` (`PHASE` propio + `INTEGRATION`) |
| C1-4 | Regla de referencias de `CU0` | aceptar que los valores `preserve` de la línea base son regresiones obligatorias y que las fijaciones `pins` las invierte su paquete propietario | aceptar |

## 3. Para preparar `CU2` (decidir antes de autorizarlo)

| Id | Decisión | Recomendación |
|---|---|---|
| E-0 | Aprobar la especificación de certificación híbrida: niveles de evidencia, evidencia estructural, clase v3 y publicación (enmiendas A2.1–A2.2 y ADR nuevo) | aprobar tras revisión, como exige la instrucción |
| D-1 | Representación de `e_2`: componente abierta cortada en el origen de la longitud orientada, sin cambiar `LocusLength`, con evento estructural de envoltura (A2.3) | adoptar |
| E-1 | Evidencia `EXPLORATORY` en el dibujo: no dibujarla, o dibujarla con marca visual distinta | dibujarla con marca visual solo en bandas `UNRESOLVED`, nunca como certificada |
| E-2 | Puntos y extremos métricos: exigir componente certificada que contenga la dirección, o aceptar validez puntual | exigir componente certificada (local) para rutas y continuidad; validez puntual solo para puntos exactos aislados |
| E-3 | Linaje de componentes por fuente estructural de frontera y migración de selectores guardados | adoptar el linaje estructural; migración explícita y fallo cerrado para selectores antiguos que no casen |

## 4. Para preparar `CU3`

| Id | Decisión | Recomendación |
|---|---|---|
| F-1 | Formas públicas de la Etapa A: `Point(L, D)` e `Intersect(L, r, D)` | adoptar (nombres sujetos a la superficie canónica inglesa) |
| F-2 | Clases de contacto tangente materializables | `TANGENT_STRUCTURAL` y `TANGENT_CERTIFIED_ISOLATED` (con estado «contacto dentro de ε» visible); `TANGENT_NUMERICAL` no |
| H-2 | Normalización de residuos por escala característica invariante (A3.1) | adoptar |
| H-3 | Ejes como objetivos de intersección o mensaje explícito (A3.4) | admitir los ejes como rectas |
| H-1 | Perfil de esfuerzo de documento (`STANDARD` / `HIGH` / `EXHAUSTIVE`) | posponer hasta después de `CU3`; no condiciona `CU1`–`CU3` |

## 5. Diseños que necesitan aprobación posterior (no bloquean `CU1`)

- Propuesta F completa y enmienda del ADR 0017 (antes de implementar la Etapa B).
- `CU7-D` (M08 y M09 reformulada) con sus decisiones G-1 a G-4.
- `CU6-D` (M07) con sus decisiones M07-a a M07-d.
- Enmiendas de `CU4` y `CU5`, en sus paquetes.

## 6. Fuera de decisión en este momento

Publicación, etiqueta, actualización del libro y autorización de cualquier paquete
distinto de los anteriores.
