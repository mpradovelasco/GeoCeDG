# PRE-G9B-MAINT-B5a-R1 — informe del candidato técnico (corrección focal L-6)

**PRE-G9B-MAINT-B5A-R1 — TECHNICAL CANDIDATE — PENDING AUTHOR REVIEW**

| Campo | Valor |
|---|---|
| Carácter | Informe técnico. `selfApproved=false`. Evidencia, no autoridad |
| Instrucción | «GeoCeDG — PRE-G9B-MAINT-B5a-R1. Corrección focal L-6 y reconstrucción del candidato integrado», del autor, 2026-11-25 |
| Prompt | [`pre-g9b-maint-b5a-r1-guard-evaluation-failure`](../../.github/prompts/tasks/pre-g9b-maint-b5a-r1-guard-evaluation-failure.prompt.md) (gobernanza `203cc4ebe`) |
| Base | B5a `65ca487da35d34d500fb12e5d5c815e03eca5754`, árbol `31ee1a88242ac3790030053e9cfe367281e64bfc` (conservado con sus verificaciones) |
| Rama / worktree | `phase/pre-g9b-maint-b5a-r1` / `GeoCeDG-wt-maint-b5a` (reutilizado: limpio, libre; la rama `phase/pre-g9b-maint-b5a` no se mueve) |
| Nivel | `STATIC` + `PHASE PRE-G9B-MAINT-B5A` + `INTEGRATION` (`CORE + FOCUSED`); `FINAL = NOT AUTHORIZED` |

## 1. Causa exacta de L-6

1. **Cuándo falla.** En `LocusConditionedRouteJumpGuard2D.Run.leaf`, cuando la evaluación del
   punto trazado en el punto medio de `I_k`, que solo hace la guarda, no es válida
   (`evaluateInComponent(...).isValid() == false`), aunque las evaluaciones del estimador hayan
   sido válidas.
2. **Representación interna.** `PolicyV2LocusMetricCapability2D` entregaba un punto o `null`; el
   `Run` guardaba un texto (`failure`) y se detenía. El estado y el diagnóstico de la evaluación
   se perdían.
3. **Resultado rico.** `LocusMetricEngine2D.conditionedBetween` publicaba
   `between.asConditioned(run.refusal())`: estado `SUCCESS`, valor finito de la estimación, error
   estimado, contribuciones y clase de garantía `ESTIMATED`, con `CONDITIONED_ROUTE_NOT_ADMISSIBLE`
   y `EVALUATION_FAILED`.
4. **Adaptador escalar.** No publicaba número (`isScalarAdmissible() == false`), pero por la
   cobertura incompleta y la falta de ruta admitida, no por el fallo.
5. **Observadores del `SUCCESS` erróneo.** Todo consumidor del resultado rico (estado, valor,
   clase de garantía, contribuciones). No hay caché del resultado de la guarda: se recalcula en
   cada consulta sobre el estado del estimador, que sí se memoriza y no cambia.
6. **Mecanismo tipado existente.** `MetricComputationStatus.NUMERICAL_FAILURE` con
   `MetricDiagnosticCode2D.EVALUATION_FAILED` y valor ausente, el mismo que usa el estimador para
   una evaluación fallida dentro del tramo (`LocusMetricEstimator2D`). No se crea ningún código.

Reproducción determinista: la prueba de B5a `aFailedEvaluationOfTheCheckIsNoNumber` fijaba ese
estado `SUCCESS` con el mismo valor que la ruta admitida; la prueba principal nueva falla sobre
`65ca487da` («EVALUATION_FAILED value=2.23606797749979», evidencia en
`artifacts/agent/pre-g9b-maint-b5a-r1-before-fix/` del worktree).

## 2. Corrección

| Archivo | Cambio |
|---|---|
| `LocusConditionedRouteJumpGuard2D` | `TracedPoint` devuelve la evaluación; el fallo registra hoja, parámetro, estado y diagnóstico; `Run.hasEvaluationFailure()`; el `CONDITIONED_ROUTE_NOT_ADMISSIBLE` del fallo dice que el resultado no publica valor |
| `PolicyV2LocusMetricCapability2D` | entrega la evaluación completa a la guarda |
| `LocusMetricEngine2D` | si la guarda registra un fallo de evaluación, publica `asConditionedFailure(NUMERICAL_FAILURE, refusal)`; firma, inconclusión y presupuesto siguen en `asConditioned` |
| `LocusMetricResult2D` | `asConditionedFailure`: valor ausente, cobertura incompleta, `UNDETERMINED`, papel `DIAGNOSTIC_PARTIAL_VALUE`, error no aplicable, sin contribuciones, ruta numérica ni ruta admitida; conserva ruta, procedencia y diagnósticos |

Sin cambios en los parámetros, la política `conditioned-route-jump-guard/v1`, H1 a H4,
`LocusConditionedTraversal2D`, identidades, regiones, tokens, certificados, serialización,
SplineV2, A2 ni A-DXF0-R1.

## 3. Pruebas

Nueva clase `PreG9BMaintB5aR1GuardFailureTest` (4 pruebas) y arnés `PreG9BMaintB5aRoutes` con
una sesión persistente opcional (motor y propietario compartidos; la ruta por defecto no cambia):

| Requisito | Prueba |
|---|---|
| 1–5 fallo en la guarda, estado de fallo, sin valor, escalar rechazado, causa conservada | `aFailedEvaluationOfTheCheckIsATypedFailureWithNoValue` (principal: falla en `65ca487da`, pasa en R1) |
| 6 recuperación tras cambiar la construcción | `aRevisionWithValidEvaluationsRecovers` (revisión 2 válida admitida con el valor bit a bit de un cálculo nuevo; revisión 3 fallida vuelve a fallar) |
| 7 orden de consulta y cachés | `cachesAndQueryOrderChangeNothing` (repetición e intercalado en una sesión y en otra con orden inverso) |
| distinción de firma, inconclusión y presupuesto | `theOtherRefusalsKeepTheirEstimateAndStatus` y las pruebas de diagnósticos de B5a |
| 8 P-1, 9 rutas admitidas | `PreG9BMaintB5aChordScalingCheckTest`, `PreG9BMaintB5aPostR1RoutesTest`, `PreG9BMaintB5aContinuousReferencesTest` y el resto de la selección de B5a |
| 10 longitudes ordinarias y A2 | suite compartida completa (diagnóstico) y, en la integración, `PHASE PRE-G9B-MAINT-A2` |

`PreG9BMaintB5aDiagnosticsTest.aFailedEvaluationOfTheCheckIsNoNumber` se actualiza a la conducta
corregida. La selección `pre-g9b-maint-b5a.shared` añade la clase nueva.

## 4. Observación corregida de paso

Nueve entradas de pruebas de B5a en `docs/upstream/modified-files.yml` tenían la descripción
dentro del campo `path` y `purpose` vacío (defecto de B5a, presente también en la integración
`2a89348d2`). R1 separa ruta y propósito sin cambiar su contenido.

## 5. Verificación

Inventario y ejecuciones de aceptación sobre el candidato congelado: en el informe consolidado
de la sesión (este documento queda congelado en el commit candidato).
