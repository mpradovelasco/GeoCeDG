# GeoCeDG — PRE-G9B-MAINT-N1: enmienda de métricas §28-§29

**PROMPT CANDIDATO — NO AUTORIZADO.** Preparado por PRE-G9B-MAINT-P0. No se ejecuta sin
instrucción autoral expresa con base exacta y con las decisiones D-B-1a y D-B-2 tomadas. La
parte de B1 requiere además D-B-1b, hoy pendiente.

## Objective
<!-- geocedg-field: objective -->

Redactar, como propuesta normativa para aprobación autoral, la enmienda fechada de
`geocedg/specs/locus/locus-v2-metrics.md` que habilita:

- §28.3: una ruta condicionada no es número si la prueba de escalado de cuerdas observa la firma de un salto (B5a), y, si se decide, admisión T-A;
- §29: barrido de la autoridad rica privada sin lector al completar una redefinición y, si se decide, al cargar (B2);
- §28.2, solo si D-B-1b elige O1 u O2: el subtotal no se estima automáticamente (O1) o se estima solo a petición expresa para la revisión vigente (O2). Con O3 no se toca §28.2.

`CHANGE_ROUTE=ORDINARY`. `VERIFICATION_CLASS=DOCUMENTATION_STATUS_ONLY`.
`PRODUCT_PHASE_EFFECT=NONE`. Nivel previsto: `STATIC`.

## Implementation base
<!-- geocedg-field: implementation_base -->

Commit y árbol designados por el autor en la autorización; verificar ambos. Por defecto, la
base del expediente P0 aprobado.

## Authority and evidence hierarchy

`AGENTS.md`; código actual de `LocusMetricEngine2D`, `LocusMetricResult2D`,
`LocusMetricEstimator2D` y `AlgoLocusMetricScalarAdapter`; `locus-v2-metrics.md`;
`post-r1-conditioned-use.md`; ADR 0037, 0042. Evidencia:
[plan C](../pre_g9b_maint_p0_limitations_plan.md) §1-§3 e
[informe](../../validation/pre_g9b_maint_p0_characterization_report.md) §B y §F (sonda P-1).

## Allowed scope
<!-- geocedg-field: allowed_scope -->

- `geocedg/specs/locus/locus-v2-metrics.md`: enmienda fechada y marcada como candidata en
  §28.2, §28.3 y §29. No reescribir otras secciones.
- Un registro de la propuesta en `docs/validation/` con la decisión autoral que la motiva.

## Explicitly forbidden scope
<!-- geocedg-field: forbidden_scope -->

Código, pruebas, selecciones, otras especificaciones, ADR, roadmap, libro; cualquier cambio
de los totales H10 y P2, de los certificados o del recorrido condicionado (B5b está diferido).

## Architectural placement

Especificación normativa del núcleo (métricas de Locus V2).

## Required design/specification

Debe fijar:

- la prueba de escalado: cociente `c(h/2)/c(h)` en bisecciones del subintervalo de cuerda
  máxima, umbral `ρ`, número de niveles `m`, resolución `ε_res`, paso mínimo;
- el presupuesto de trabajo: niveles adicionales por hoja, tope de evaluaciones adicionales
  por ruta y negativa conservadora con causa propia si se agota;
- la causa y el diagnóstico;
- su carácter de comprobación numérica conservadora, no prueba universal ni certificación, y
  la distinción entre discontinuidad no detectada, incumplimiento de H3 y ausencia de
  certificación de continuidad (plan C §1.2);
- (si O1 u O2) el estado público del total no calculado, la remisión a rutas por región y, en
  O2, la petición expresa y la vigencia del valor por revisión;
- el criterio de autoridad huérfana: privada, sin lector, nunca un resultado con nombre del
  usuario;
- el momento del barrido respecto del paso de Undo y de la reversión.

## Geometric invariants and degeneracies

Sin ampliación de garantías de existencia, continuidad o completitud. Casos que deben quedar
descritos: salto alineado, salto transversal bajo resolución, extremo de tipo pliegue
(`α`), puntos estacionarios, parametrización muy no uniforme.

## Compatibility and serialization

Ningún cambio de XML. Declarar el efecto sobre documentos de 1.2.0–1.2.2: contenido rico
mostrado de totales condicionados y, si se decide, la retirada de huérfanos al cargar.

## Required tests and commands
<!-- geocedg-field: required_checks -->

`tools/agent/verify.ps1 -Profile STATIC` sobre el commit exacto; `git diff --check`.

## Authorization boundary
<!-- geocedg-field: authorization_boundary -->

Autoriza solo la redacción de la enmienda candidata. Su adopción es una decisión autoral
posterior; no autoriza B5a, B1a ni B2. Si D-B-1b sigue pendiente, la enmienda cubre solo
§28.3 y §29 y la parte de §28.2 se redacta después.

## Publication boundary
<!-- geocedg-field: publication_boundary -->

Commit local en rama propia. Sin push, `main`, etiquetas ni releases salvo autorización
expresa.

## Acceptance and closeout

Se detiene con la enmienda candidata para revisión autoral.

## Required artifacts

Enmienda, registro, identificador `STATIC`, `GUIDE_IMPACT = NONE` (salvo que el autor
decida documentar el cambio visible del inspector).

## Stop conditions

Si la redacción exige cambiar H10, P2, certificados o el recorrido; si aparece un caso en el
que la prueba de escalado niega un número hoy correcto en los modelos del autor sin firma de
salto.

## Deferred debts

`conditioned-traversal/v2` (B5b); admisión T-B y T-C.
