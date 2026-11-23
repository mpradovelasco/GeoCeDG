# GeoCeDG — PRE-G9B-MAINT-B1a: política del subtotal del total condicionado

**PROMPT CANDIDATO — NO AUTORIZADO.** Requiere la decisión normativa D-B-1b, **pendiente**
tras la resolución autoral del 2026-11-23, y, si elige O1 u O2, una enmienda de §28.2 aprobada
y una instrucción autoral con base exacta. Si el autor elige O3, este incremento no existe y la
mejora posible es `MAINT-PERF-O2a`. Antes de adoptar O2 debe ejecutarse la caracterización
`MAINT-B1-S0` (resultado rico vigente, sus consumidores y su comportamiento por revisión); hasta
entonces §28.2 no se modifica. La admisión T-A queda diferida hasta validar B5a y fuera de este
prompt.

## Objective
<!-- geocedg-field: objective -->

Eliminar el coste automático innecesario del total condicionado sin perder información
científica potencialmente útil, según la opción decidida (plan C §3.2):

- **O1:** cuando el total solo puede ser subtotal, no construir el estimador; el resultado rico
  declara regiones y bandas y remite a `LocusLength(L, A, B)` por región.
- **O2 (recomendación técnica):** como O1 en cada revisión; el subtotal se estima solo cuando
  el usuario lo pide expresamente (acción del inspector), para la revisión vigente. El valor
  queda marcado con su revisión, no se recalcula solo tras un cambio, nunca se muestra como
  vigente si es de otra revisión y no se guarda.

Objetivo medible: el coste por cambio real con un total condicionado no calculable es aproximadamente
el de la base.

`CHANGE_ROUTE=ORDINARY`. `VERIFICATION_CLASS=INTEGRATED_PHASE`. Nivel previsto: `STATIC` +
`PHASE PRE-G9B-MAINT-B1A` + `INTEGRATION`.

## Implementation base
<!-- geocedg-field: implementation_base -->

Commit y árbol designados por el autor.

## Authority and evidence hierarchy

`AGENTS.md` §11.4; `locus-v2-metrics.md` §28 enmendado; contrato de uso condicionado §5.3 y
§8.4; código de `LocusMetricEngine2D.conditionedTotal` y `LocusMetricResult2D`. Evidencia:
plan C §3, informe §B.1, documento de rendimiento §11.

## Allowed scope
<!-- geocedg-field: allowed_scope -->

- Paso previo, solo medición: estado y valor del resultado rico actual de `LocusLength(w_2)` y
  sus lectores.
- `LocusMetricEngine2D` y `LocusMetricResult2D`: rama de total condicionado.
- (O2) acción del inspector que pide el subtotal para la revisión vigente y su presentación.
- Presentación del estado en el inspector.
- Pruebas y selecciones `PRE-G9B-MAINT-B1A`.

## Explicitly forbidden scope
<!-- geocedg-field: forbidden_scope -->

Totales H10 y P2; rutas condicionadas (§28.3); reutilización entre revisiones; cachés
compartidas; persistencia del valor pedido; el arnés post-R1, salvo para leerlo.

## Architectural placement

Núcleo Java compartido: métricas de Locus V2; acción y presentación en Desktop.

## Required design/specification

Caracterización `MAINT-B1-S0` revisada; D-B-1b decidida; §28.2 enmendado.

## Geometric invariants and degeneracies

- Ninguna banda se omite en silencio.
- El escalar público de un total con bandas sigue indefinido, también tras una petición.
- (O2) el valor pedido es idéntico al que hoy se publica para la misma revisión.

## Compatibility and serialization

Sin XML nuevo. En documentos de 1.2.0–1.2.2 con totales condicionados cambia el contenido rico
mostrado sin petición; el escalar no cambia (ya era indefinido).

## Required tests and commands
<!-- geocedg-field: required_checks -->

- Medición previa del resultado rico actual.
- Arnés `tools/benchmark/post-r1-locus/run-harness.ps1 -Scenario intersect -Configuration total`,
  antes y después: medianas, p95, recuentos de evaluaciones y compendios idénticos salvo el
  campo declarado.
- Pruebas compartidas:
  - una rama con bandas construye 0 estados de región sin petición;
  - (O2) la petición produce el valor actual; tras un cambio, el valor queda declarado de otra
    revisión y no se recalcula solo;
  - una región sin bandas no cambia;
  - H10 y P2 sin cambios.
- Adaptar `PostR1LengthDeletionDesktopTest`.
- Comandos: `STATIC`, `PHASE PRE-G9B-MAINT-B1A` e `INTEGRATION`; `git diff --check`.

## Authorization boundary
<!-- geocedg-field: authorization_boundary -->

Este incremento, en la opción decidida.

## Publication boundary
<!-- geocedg-field: publication_boundary -->

Rama local. Sin push, `main`, etiquetas ni releases.

## Acceptance and closeout

Candidato congelado con la medición antes y después.

## Required artifacts

Commits, verificación, mediciones del arnés, informe; `GUIDE_IMPACT = UPDATE` (sección de
longitudes condicionadas).

## Stop conditions

- La medición previa muestra un consumidor que necesita el subtotal en cada revisión (volver
  al autor antes de seguir).
- H10 o P2 cambian.
- Los compendios difieren fuera del campo declarado.

## Deferred debts

Admisión T-A (diferida hasta validar B5a), T-B y T-C; comando explícito para pedir el subtotal desde guiones; `MAINT-PERF-O2a`.
