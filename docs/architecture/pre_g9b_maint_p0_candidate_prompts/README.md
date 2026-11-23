# PRE-G9B-MAINT-P0 — G. Prompts candidatos de implementación

**PROMPTS CANDIDATOS — NINGUNO ESTÁ AUTORIZADO.** Preparados por PRE-G9B-MAINT-P0
(2026-11-23) sobre la base `f80f0e9b029e678007f399508163bf794951992c` y revisados con las
enmiendas autorales finales del expediente (revisión 2, 2026-11-23). `selfApproved=false`.

Están fuera de `.github/prompts/` y con sufijo `.prompt-candidate.md` a propósito: no forman
parte de la capa canónica de prompts ni de su validación, y no deben leerse como dirección
vigente. Cuando el autor autorice un incremento, su prompt se revisa con las decisiones
tomadas en la [hoja F](../../validation/pre_g9b_maint_p0_author_decision_sheet.md) y se
traslada a `.github/prompts/tasks/` en un paso propio, autorizado por el autor.

Cada prompt sigue la forma de `.github/prompts/tasks/task-template.prompt.md`: objetivo,
base, autoridad, alcance permitido y prohibido, capa, especificación, invariantes,
compatibilidad, pruebas y comandos, límites de autorización y publicación, cierre,
artefactos, paradas y deudas diferidas.

| Prompt | Incremento | Bloque | Depende de |
|---|---|---|---|
| [maint-n1-metrics-amendment](maint-n1-metrics-amendment.prompt-candidate.md) | `MAINT-N1` | B | D-B-1a, D-B-2 (D-B-1b para §28.2) |
| [maint-b5a-conditioned-route-jump-guard](maint-b5a-conditioned-route-jump-guard.prompt-candidate.md) | `MAINT-B5a` | B | N1 |
| [maint-b2-redefinition-orphan-sweep](maint-b2-redefinition-orphan-sweep.prompt-candidate.md) | `MAINT-B2` | B | N1, D-B-2 |
| [maint-b1a-conditioned-total-subtotal-policy](maint-b1a-conditioned-total-subtotal-policy.prompt-candidate.md) | `MAINT-B1a` | B | D-B-1b (pendiente) ∈ {O1, O2}, N1 |
| [maint-u2-point-tool-unification](maint-u2-point-tool-unification.prompt-candidate.md) | `MAINT-U2` | C | D-U2 |
| [maint-i2g0-multi-output-redefine-barrier](maint-i2g0-multi-output-redefine-barrier.prompt-candidate.md) | `MAINT-I2G0` (caracterización y barrera) | C | D-I2G |
| [maint-f8-local-component-creation](maint-f8-local-component-creation.prompt-candidate.md) | `MAINT-F8` (S0 y min) | C | U2, D-F8 |
| [maint-s0-s1-explicit-point-substitution](maint-s0-s1-explicit-point-substitution.prompt-candidate.md) | `MAINT-S0`, `MAINT-S1` | C | S0: —; S1: informe S0, B2, I2G0, D-M09-1..4 |
| [maint-n3-s2-dependent-contract-update](maint-n3-s2-dependent-contract-update.prompt-candidate.md) | `MAINT-N3`, `MAINT-S2` | C | S1, D-M09-2 |
| [maint-a-n1-bspline-nurbs-normative-design](maint-a-n1-bspline-nurbs-normative-design.prompt-candidate.md) | `MAINT-A-N1` | A | D-A-1..D-A-7 |
| [maint-a-dxf0-dxf-spline-reference-import](maint-a-dxf0-dxf-spline-reference-import.prompt-candidate.md) | `MAINT-A-DXF0` | A | contrato de importación de A-N1, D-A-6, D-A-7 |
| [maint-a2-bspline-kernel-family](maint-a2-bspline-kernel-family.prompt-candidate.md) | `MAINT-A2` | A | especificación de familia de A-N1 |
| [maint-a-dxf1-bspline-approximation-of-imported-spline](maint-a-dxf1-bspline-approximation-of-imported-spline.prompt-candidate.md) | `MAINT-A-DXF1` | A | A2, A-DXF0, D-A-8..D-A-10 |
| [maint-a3-bspline-dxf-spline-export](maint-a3-bspline-dxf-spline-export.prompt-candidate.md) | `MAINT-A3` | A | A2 aprobado, D-A-4 |

Cambios de la revisión 2: `maint-a1-bspline-normative-spec` pasa a
`maint-a-n1-bspline-nurbs-normative-design` y `maint-b1a-conditioned-total-without-subtotal`
pasa a `maint-b1a-conditioned-total-subtotal-policy`; se añaden `maint-a-dxf0` y
`maint-a-dxf1`; los demás se actualizan según las enmiendas.

No se preparan prompts para los incrementos opcionales, diferidos o no autorizados
(`MAINT-PERF-O2a`, `MAINT-A-DXF1b`, `MAINT-B5b`, la política de aislamiento de B3, `MAINT-A4`,
`A5`, `A7`, I2G-3 y la NURBS semántica nativa `MAINT-A-R`, cuyo diseño está en la propuesta B
§10). Se prepararán si el autor decide abrirlos.

## Reglas comunes a todos los prompts

- Leer primero `AGENTS.md`, los prompts canónicos de gobernanza y verificación y el roadmap
  desde disco; nunca tratar este expediente ni un resumen como autorización.
- Trabajar en una rama local propia descendiente de la base autorizada; nunca en `main`.
- Un commit técnico, inventario de verificación cuando cambien las selecciones, y luego la
  verificación de la clase congelada sobre el commit exacto.
- No modificar `CLAUDE.md`, `AGENTS.md`, `.github/prompts/**`, `ai-shell/prompts/**`, el
  libro, `main`, etiquetas ni releases.
- Detenerse ante cualquier cambio de serialización sin migración, cualquier efecto sobre
  documentos de 1.2.2 no declarado, una segunda autoridad geométrica o un DAG paralelo, o la
  necesidad de tocar I3, M08-D o la reconstrucción general.
- Informar comandos exactos, códigos de salida, identificadores de verificación y rutas de
  registro; la aceptación técnica nunca es aprobación autoral.
