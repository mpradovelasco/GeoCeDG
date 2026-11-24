# PRE-G9B-MAINT-A-N1 R2 — registro de la aprobación autoral

**PRE-G9B-MAINT-A-N1 — CONTRATOS NORMATIVOS APROBADOS POR EL AUTOR — IMPLEMENTACIÓN NO AUTORIZADA**

| Campo | Valor |
|---|---|
| Carácter | Registro de la decisión autoral. `selfApproved=false`: la decisión es del autor y este registro solo la transcribe; el agente no aprueba nada |
| Fecha de la decisión | 24 de noviembre de 2026 |
| Instrucción | «GeoCeDG — PRE-G9B-MAINT. Cierre autoral coordinado de N1, A-N1 y U2-R1, integración y promoción a `main`», entregada por el autor en la conversación |
| Naturaleza | Aprobación individual de tres contratos normativos. Implementación: **no autorizada** |
| Base común | `e79ace7d1473f13d32d0b660e92a4d1d45ce39bf`, árbol `9c43dfa5548a0844215c0ab9ba808f20e3dd3a84` |
| Candidato aprobado | `8747c731eccc3644cd1da8b74eb3453a68511813`, árbol `89df425e50a1fc3932ac7b59a0b80af5a48072b9` (R2, padre `307bc8fda`), rama `phase/pre-g9b-maint-a-n1` |
| Clase de verificación | `DOCUMENTATION_STATUS_ONLY`; `STATIC` |
| Resultado técnico | `STATIC` `verification-a887c27dd0f14debab36649866a00ccd`, `ACCEPTED / COMPLETE` sobre el candidato exacto (registro `artifacts/agent/pre-g9b-maint-a-n1-r2-static` del worktree `GeoCeDG-wt-maint-a-n1`) |
| Integración | Primera ola coordinada; véase el [registro de integración](pre_g9b_maint_first_wave_integration_record.md) |

## 1. Decisión

El autor aprueba como contratos normativos, individualmente:

1. la especificación [`bspline-2d/v1`](../../geocedg/specs/curves/bspline-2d.md);
2. el [ADR 0043](../adr/0043-explicit-2d-bspline-family-rational-ready.md), que pasa de
   propuesto a aceptado;
3. el contrato [`dxf-spline-reference/v1`](../../geocedg/specs/import/dxf-spline-reference.md),
   revisión 2.

Adopta las propuestas Q-BS-1 a Q-BS-6, Q-DXF-1 a Q-DXF-4 y Q-AN1-1 del
[informe del candidato](pre_g9b_maint_a_n1_candidate_report.md), con estas precisiones:

- representación polinómica local por tramo y extensión aditiva, preservando SplineV2;
- hasta 128 puntos de control; la ruta adaptativa para más de 64 tramos no presupone
  completitud;
- `κ_max = 2^16` queda como umbral candidato sujeto a validación numérica;
- `<geocedgBSpline>` exige rechazo tipado y rollback demostrados antes de implementar
  `MAINT-A2`;
- biblioteca matemática pura compartida, sin autoridad geométrica independiente;
- pesos DXF conforme a la revisión 2;
- límites iniciales de importación revisables tras caracterización;
- referencias externas de solo lectura, sesión R-a, sin incorporación al DAG.

La [matriz de validación](pre_g9b_maint_a_n1_validation_matrix.md) y el informe del candidato no
se aprueban como contratos separados; conservan su redacción y sus constantes
`PENDING_VALIDATION_IN_MAINT_A2`.

## 2. Observaciones para incrementos posteriores

Se registran como obligaciones futuras, sin abrir otra corrección de A-N1:

- **BS-11:** distinguir el soporte local del movimiento de un punto de control de los cambios de
  grado o de vector de nudos, que no tienen soporte local.
- **AP-2:** no imponer una relación de orden entre residuos de distintas muestras y estimaciones
  de error sin justificación matemática.

## 3. Límites

La aprobación no autoriza `MAINT-A2`, `MAINT-A-DXF0`, `MAINT-A-DXF1` ni `MAINT-A3`. La NURBS
nativa `MAINT-A-R` (`bspline-2d/v2`) sigue diseñada y no autorizada. No cambia SplineV2, sus ADR
ni los contratos de exportación DXF aprobados.

## 4. Estado

Las cabeceras de las dos especificaciones y del ADR 0043 registran la aprobación y remiten a
este registro. No cambia código, pruebas, selecciones ni inventarios. La aceptación técnica de
`STATIC` es un hecho independiente de esta aprobación autoral.
