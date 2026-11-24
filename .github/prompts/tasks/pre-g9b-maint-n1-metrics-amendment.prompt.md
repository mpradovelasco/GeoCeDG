# GeoCeDG — PRE-G9B-MAINT-N1: enmienda de métricas §28.3 y §29

**PROMPT CANÓNICO — INCREMENTO DOCUMENTAL NORMATIVO AUTORIZADO POR EL AUTOR (2026-11-23) SOBRE LA
BASE EXACTA `e79ace7d1` — LA ENMIENDA QUE PRODUCE ES CANDIDATA Y NO ESTÁ EN VIGOR HASTA LA DECISIÓN
AUTORAL POSTERIOR.** `selfApproved=false`.

- **Origen.** Prompt candidato
  [`maint-n1-metrics-amendment.prompt-candidate.md`](../../../docs/architecture/pre_g9b_maint_p0_candidate_prompts/maint-n1-metrics-amendment.prompt-candidate.md),
  preparado por `PRE-G9B-MAINT-P0` (revisión 3) y aprobado como planificación con la
  [resolución autoral](../../../docs/validation/pre_g9b_maint_p0_author_resolution_record.md) del
  2026-11-23. Se incorpora a la capa canónica en un paso de gobernanza propio, por la instrucción
  autoral del 2026-11-23 que autoriza iniciar exclusivamente `PRE-G9B-MAINT-N1`. El candidato
  queda como evidencia histórica; donde difiere de este prompt, prevalece este prompt.
- **Revisión del candidato.** Correcciones respecto del candidato:
  - el alcance permitido del candidato incluía §28.2; se retira: D-B-1b sigue pendiente y §28.2
    no se modifica;
  - se fija la base exacta (commit y árbol) de la autorización;
  - se incorporan las exclusiones expresas de la autorización: ni B5a ni B2 se implementan, ni
    B2-F2, ni B5b, ni cambios en la política de aislamiento de intersecciones, ni G9B;
  - el umbral del cociente se llama `ρ_J`, porque `ρ` ya designa la razón de una semejanza en el
    contrato de uso condicionado §8.7;
  - la parada por un caso de los modelos del autor, que un incremento documental no puede medir,
    se convierte además en condición de entrada de `MAINT-B5a`;
  - el incremento se entrega en dos commits: este paso de gobernanza y el commit documental N1.

## Objective
<!-- geocedg-field: objective -->

Redactar, como propuesta normativa para aprobación autoral, la enmienda fechada y candidata de
`geocedg/specs/locus/locus-v2-metrics.md` que habilita:

- §28.3 (B5a, D-B-1a): una ruta condicionada no se admite como número si una comprobación
  numérica conservadora de escalado de cuerdas observa la firma de una posible discontinuidad
  alineada con la cuerda. No reclama certificación de continuidad y preserva la distinción entre
  H3 asumida, incumplimiento de H3 no detectado y continuidad certificada;
- §29 (B2-F1, D-B-2): la limpieza transaccional de la autoridad rica auxiliar privada que queda
  sin consumidores al completar correctamente una redefinición del número.

Fuera de esta enmienda por la resolución autoral del 2026-11-23 y por la autorización de N1: el
barrido al cargar (B2-F2, que no se implementa), la admisión T-A (diferida hasta validar B5a),
§28.2 (D-B-1b pendiente), B5b y la política de aislamiento de intersecciones.

`CHANGE_ROUTE=ORDINARY`. `VERIFICATION_CLASS=DOCUMENTATION_STATUS_ONLY`, congelada al inicio.
`PRODUCT_PHASE_EFFECT=NONE`. Nivel previsto: `STATIC` sobre el commit exacto.

## Implementation base
<!-- geocedg-field: implementation_base -->

`main = origin/main = e79ace7d1473f13d32d0b660e92a4d1d45ce39bf`, árbol
`9c43dfa5548a0844215c0ab9ba808f20e3dd3a84`, designados por el autor en la autorización.
Verificar ambos y la referencia remota antes de crear la rama. Rama local propia:
`phase/pre-g9b-maint-n1-metrics-amendment`.

## Authority and evidence hierarchy

`AGENTS.md` (§2, §11.4, §14, §16); código actual de `LocusMetricEngine2D`,
`LocusMetricResult2D`, `LocusMetricEstimator2D`, `MetricEstimatorPolicy2D`,
`LocusMetricPolicy2D`, `AlgoLocusMetricScalarAdapter` y de la transacción de redefinición de
`Construction`; `locus-v2-metrics.md` (§13, §25, §27, §28, §29); contrato de uso condicionado
`post-r1-conditioned-use.md` (§3.4, §4, §5); ADR 0026, 0029, 0037 y 0042. Evidencia:
[plan C](../../../docs/architecture/pre_g9b_maint_p0_limitations_plan.md) §1, §2 y §6,
[informe](../../../docs/validation/pre_g9b_maint_p0_characterization_report.md) §B.2, §B.5 y §F
(sonda P-1) y la [hoja F](../../../docs/validation/pre_g9b_maint_p0_author_decision_sheet.md) §0.

## Allowed scope
<!-- geocedg-field: allowed_scope -->

- `geocedg/specs/locus/locus-v2-metrics.md`: una enmienda fechada y marcada como candidata que
  añade texto a §28.3 y a §29. No reescribir el texto aprobado de esas ni de otras secciones; la
  enmienda declara qué sustituiría al adoptarse.
- Nuevo `docs/validation/pre_g9b_maint_n1_metrics_amendment_record.md`: registro de la propuesta
  con la decisión autoral que la motiva, las decisiones normativas, el efecto de compatibilidad y
  las condiciones de entrada de `MAINT-B5a` y `MAINT-B2`.
- Este prompt, en su commit de gobernanza propio.
- Cálculos analíticos o numéricos de apoyo en el directorio temporal de la sesión, fuera del
  árbol; no son evidencia de aceptación.

## Explicitly forbidden scope
<!-- geocedg-field: forbidden_scope -->

Código, pruebas, selecciones, inventarios, otras especificaciones, ADR, roadmap, libro, `main`,
etiquetas y releases; §28.2; la implementación de B5a o B2; B2-F2 o cualquier barrido, migración
o reparación al cargar; B5b (`conditioned-traversal/v2`) y el recorrido condicionado; la
política de aislamiento de intersecciones; cualquier cambio de los totales H10 y P2, de los
certificados, del inventario de componentes, de los tokens o de la identidad semántica; G9B.

## Architectural placement

Especificación normativa del núcleo Java compartido (métricas de Locus V2 y ciclo de vida de su
autoridad privada).

## Required design/specification

La enmienda debe fijar:

- la prueba de escalado: cociente `c(h/2)/c(h)` sobre bisecciones sucesivas del subintervalo de
  cuerda máxima de cada hoja aceptada, umbral `ρ_J`, número de niveles consecutivos `m`, niveles
  máximos por hoja `L`, resolución `ε_res` e intervalo mínimo;
- el presupuesto de trabajo: evaluaciones adicionales por hoja, tope por ruta y negativa
  conservadora con causa propia si se agota; valores candidatos fijados por análisis, con
  intervalos admisibles dentro de los cuales `MAINT-B5a` los fija mediante pruebas focales;
- diagnósticos distintos para firma de salto observada, comprobación inconclusa y agotamiento de
  presupuesto, y la lista declarada de contraejemplos no detectables por muestreo finito y de
  falsos positivos conservadores;
- que B5a no toca el inventario de componentes, los tokens, la identidad semántica, los
  certificados de existencia, el valor ni el error de la estimación;
- su carácter de comprobación numérica conservadora, no prueba universal ni certificación, y la
  distinción entre discontinuidad no detectada, incumplimiento de H3 y ausencia de certificación
  de continuidad (plan C §1.2);
- el criterio de autoridad huérfana: privada, sin lector, nunca un resultado con nombre del
  usuario ni un resultado con identidad y uso independiente;
- el momento de la limpieza respecto del paso de Undo y de la reversión: después de completar
  correctamente la redefinición, dentro del mismo ciclo de Undo/Redo y sin cambios parciales si
  la redefinición falla.

## Geometric invariants and degeneracies

Sin ampliación de garantías de existencia, continuidad o completitud. Casos que deben quedar
descritos: salto alineado, salto transversal bajo resolución, extremo de tipo pliegue
(velocidad divergente de exponente `α`), puntos estacionarios, parametrización muy no uniforme,
varios saltos y discontinuidad evitable.

## Compatibility and serialization

Ningún cambio de XML. Declarar el efecto sobre documentos existentes: rutas que dejarían de ser
número por B5a. La carga de documentos existentes no cambia: no hay barrido al cargar (B2-F2 no
se implementa) y los huérfanos heredados quedan como limitación documentada.

## Required tests and commands
<!-- geocedg-field: required_checks -->

- `Set-Location <repo>; [Environment]::CurrentDirectory = <repo>`.
- `tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts/agent/<nombre nuevo>` sobre el
  commit exacto del candidato, con la consola en el directorio temporal de la sesión.
- Validación del contrato de este prompt con `Test-PromptContractDocument` (perfil `task`).
- `git diff --check` sobre el delta respecto de la base.

## Authorization boundary
<!-- geocedg-field: authorization_boundary -->

Autoriza solo este paso de gobernanza y la redacción de la enmienda candidata con su registro.
Su adopción es una decisión autoral posterior. No autoriza `MAINT-B5a`, `MAINT-B2`, `MAINT-B1a`
ni ningún otro incremento. §28.2 no se toca mientras D-B-1b esté pendiente.

## Publication boundary
<!-- geocedg-field: publication_boundary -->

Commits locales en la rama propia. Sin push, `main`, etiquetas ni releases salvo autorización
expresa posterior.

## Acceptance and closeout

Se detiene con la enmienda candidata confirmada y `STATIC` sobre su commit exacto, para revisión
autoral. La aceptación técnica de `STATIC` no es aprobación autoral.

## Required artifacts

Commit de gobernanza, commit documental N1, enmienda, registro, identificador `STATIC` con
comando, código de salida y ruta de registro, `GUIDE_IMPACT = NONE` (la guía cambiará con
`MAINT-B5a`, no con la norma), impacto de bootstrap: sin cambio (ningún prerrequisito de
estación cambia).

## Stop conditions

- La redacción exige cambiar §28.2, H10, P2, certificados, el recorrido condicionado o la
  política de aislamiento.
- El análisis muestra un caso de referencia continuo, de los enumerados en el plan C §1.5, que la
  prueba de escalado negaría sin firma de salto con los valores candidatos.
- La limpieza de §29 exige relajar una guarda de identidad, tratar la redefinición como borrar y
  crear, o un barrido al cargar.

## Deferred debts

`conditioned-traversal/v2` (B5b); admisión T-A (D-B-1c), T-B y T-C; política del subtotal
(D-B-1b); huérfanos heredados de documentos anteriores (limitación declarada, sin barrido).
