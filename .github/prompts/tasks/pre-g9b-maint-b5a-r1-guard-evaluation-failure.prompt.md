# GeoCeDG — PRE-G9B-MAINT-B5a-R1: corrección focal L-6 (fallo de evaluación de la guarda)

**PROMPT DE TAREA AUTORIZADO POR EL AUTOR EL 2026-11-25** («GeoCeDG — PRE-G9B-MAINT-B5a-R1.
Corrección focal L-6 y reconstrucción del candidato integrado»), sobre el candidato técnico
`PRE-G9B-MAINT-B5A` indicado abajo. Es la versión reconciliada de esa autorización con el código
del candidato. `selfApproved=false`. No reescribe el prompt ni las verificaciones de B5a y no
autoriza ningún otro incremento.

## Objective
<!-- geocedg-field: objective -->

Restablecer la coherencia entre un fallo real de evaluación de la guarda de escalado de cuerdas
(`conditioned-route-jump-guard/v1`, `locus-v2-metrics.md` §28.3.1) y el resultado rico de
Locus V2. Hoy (lectura L-6 de la auditoría) ese fallo rehúsa el escalar, pero el resultado rico
conserva la estimación con estado `SUCCESS`, contra §28.1 («an evaluation that fails inside the
stretch is a failure of the result, never a value»).

Tras R1, cuando falle una evaluación de la guarda dentro del tramo, el resultado completo indica
fallo con el mecanismo tipado existente (`NUMERICAL_FAILURE` + `EVALUATION_FAILED`), sin valor
publicable, con el escalar inadmisible y la causa trazable hasta la evaluación que falló (leaf,
parámetro, estado y diagnóstico de la evaluación). La firma observada, la hoja inconclusa y el
agotamiento del presupuesto conservan exactamente sus reglas de §28.3.1.

`CHANGE_ROUTE = ORDINARY`. `VERIFICATION_CLASS = INTEGRATED_PHASE`.
`PRODUCT_PHASE_EFFECT = PRE-G9B-MAINT-B5A-R1`. Nivel: `STATIC` + `PHASE PRE-G9B-MAINT-B5A` +
`INTEGRATION`. `FINAL = NOT AUTHORIZED`. `AUTHOR_DECISION = PENDING`.

## Implementation base
<!-- geocedg-field: implementation_base -->

- Repositorio `mpradovelasco/GeoCeDG`.
- Commit `65ca487da35d34d500fb12e5d5c815e03eca5754` (candidato técnico `PRE-G9B-MAINT-B5A`).
- Árbol `31ee1a88242ac3790030053e9cfe367281e64bfc`.

Rama local `phase/pre-g9b-maint-b5a-r1` creada desde el commit exacto en el worktree
`GeoCeDG-wt-maint-b5a` (limpio y libre); la rama `phase/pre-g9b-maint-b5a` no se mueve.

## Authority and evidence hierarchy

1. `AGENTS.md`, código, pruebas y contratos de serialización vigentes en la base.
2. La instrucción autoral de 2026-11-25 y la decisión autoral sobre los parámetros de B5a.
3. `locus-v2-metrics.md` §28.1 y §28.3.1, el informe de B5a y la auditoría L-1 a L-9.
4. Contrato de verificación vigente: `tools/agent/verify.ps1`, el registro y sus esquemas.

## Allowed scope
<!-- geocedg-field: allowed_scope -->

- `LocusConditionedRouteJumpGuard2D`: la evaluación del punto trazado devuelve la evaluación
  (no un punto nulo), el fallo se registra con su estado y diagnóstico y el `Run` lo expone.
- `LocusMetricEngine2D.conditionedBetween`: un fallo de evaluación de la guarda publica un
  resultado de fallo; los demás caminos no cambian.
- `LocusMetricResult2D`: un método de paquete que publica ese fallo (valor ausente,
  `NUMERICAL_FAILURE`, cobertura incompleta, sin ruta admitida) conservando la procedencia, la
  ruta y los diagnósticos.
- `PolicyV2LocusMetricCapability2D.checkConditionedRoute`: entrega la evaluación a la guarda.
- La prueba de B5a que fijaba la conducta anterior, pruebas focales nuevas, la selección
  `PHASE PRE-G9B-MAINT-B5A`, el inventario JUnit y sus pines, `docs/upstream/modified-files.yml`,
  un informe focal en `docs/validation/`.
- Este prompt, en un commit preparatorio de gobernanza.

## Explicitly forbidden scope
<!-- geocedg-field: forbidden_scope -->

- Los cinco parámetros aprobados, la política `conditioned-route-jump-guard/v1`, H1 a H4, el
  valor y el error de las rutas admitidas, la firma, la hoja inconclusa y el presupuesto.
- `LocusConditionedTraversal2D`, la identidad de contenido, la revisión semántica, regiones y
  bandas, tokens, certificados, serialización, SplineV2, A2 y su interfaz de derivadas
  unilaterales, A-DXF0-R1.
- Nuevos códigos de estado o de diagnóstico; cambios de presentación ajenos a los diagnósticos.
- `CLAUDE.md`, `AGENTS.md`, otros prompts canónicos, el roadmap canónico, `main`, etiquetas y
  releases.

## Architectural placement

Núcleo, métrica Locus V2: publicación del resultado rico de una ruta condicionada.

## Required design/specification

Ninguna norma nueva: aplica §28.1 y §28.3.1 tal como están. Si la corrección exigiera cambiar
el contrato, se detiene.

## Geometric invariants and degeneracies

Una ruta admitida conserva valor y error bit a bit. Ninguna ruta rehusada por firma,
inconclusión o presupuesto cambia de estado, valor ni diagnósticos.

## Compatibility and serialization

Sin cambios de serialización. Solo cambia el estado rico de una ruta condicionada cuya guarda
encuentra una evaluación fallida dentro del tramo; su escalar ya era indefinido.

## Required tests and commands
<!-- geocedg-field: required_checks -->

Pruebas focales: fallo de evaluación en la guarda; estado de fallo; ausencia de valor; rechazo
del escalar; causa conservada; recuperación tras cambiar la construcción; independencia del
orden de consulta y de las cachés; P-1; rutas condicionadas admitidas; longitudes ordinarias y
A2. La prueba principal debe fallar en `65ca487da` y pasar con R1.

Sobre el commit técnico congelado y limpio:

```powershell
git diff --check 65ca487da35d34d500fb12e5d5c815e03eca5754 HEAD
& tools/agent/verify.ps1 -Profile STATIC -LogDirectory <directorio-nuevo>
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-MAINT-B5A -LogDirectory <directorio-nuevo>
& tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory <directorio-nuevo>
```

No ejecutar `FINAL`.

## Authorization boundary
<!-- geocedg-field: authorization_boundary -->

Autorizado: la corrección L-6, sus pruebas, el registro de este prompt, la ampliación de la fase
`PRE-G9B-MAINT-B5A`, los commits locales, la verificación individual y la nueva integración desde
`2a89348d2348798f623f1fabcdc651a5332cd59c` con sus cinco verificaciones. No autoriza `FINAL`,
otro incremento ni la promoción. La aceptación técnica no es aprobación autoral.

## Publication boundary
<!-- geocedg-field: publication_boundary -->

Commits locales en `phase/pre-g9b-maint-b5a-r1` y en una rama de integración nueva. Sin push,
promoción a `main`, etiquetas, releases ni paquetes.

## Acceptance and closeout

Estado final esperado: `PRE-G9B-MAINT SECOND WAVE — CORRECTED INTEGRATION CANDIDATE — PENDING
EXACT-SHA PROMOTION APPROVAL`.

## Required artifacts

Commit de gobernanza, commit técnico y de inventario; identificadores de verificación; informe
focal; `GUIDE_IMPACT = NONE` (la guía ya describe el fallo de evaluación como «sin valor»);
impacto de bootstrap: sin cambio.

## Stop conditions

- La corrección exige un estado o diagnóstico nuevo, o cambiar el contrato.
- Cambia el valor, el error o el estado de una ruta admitida o rehusada por otra causa.
- Exige tocar `LocusConditionedTraversal2D`, A2 o A-DXF0-R1.
- Un fallo de `INTEGRATION` atribuible al cambio.

## Deferred debts

Fallo histórico CU2; pin del manifiesto de recursos y `final.desktop` antes de un `FINAL`.
