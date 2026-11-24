# GeoCeDG — PRE-G9B-MAINT-A2: familia B-spline polinómica `bspline-2d/v1` en el núcleo

**PROMPT DE TAREA AUTORIZADO POR EL AUTOR EL 2026-11-24** («GeoCeDG — PRE-G9B-MAINT. Segunda ola
coordinada: MAINT-B5a + MAINT-A-DXF0 + MAINT-A2», §0, §6 y §9). Es la versión reconciliada del
prompt candidato del expediente P0
([`maint-a2-bspline-kernel-family.prompt-candidate.md`](../../../docs/architecture/pre_g9b_maint_p0_candidate_prompts/maint-a2-bspline-kernel-family.prompt-candidate.md))
con la especificación `bspline-2d/v1` aprobada, el ADR 0043, la matriz A-N1 revisión 2 y la
instrucción del autor; donde discrepan prevalecen los contratos aprobados y la instrucción.
`selfApproved=false`. No autoriza ningún otro incremento.

## Objective
<!-- geocedg-field: objective -->

Implementar exclusivamente la familia semántica polinómica
[`bspline-2d/v1`](../../../geocedg/specs/curves/bspline-2d.md):

- el comando `BSpline` con sus tres formas `BSpline(points, degree, knots)`,
  `BSpline(points, degree)` y `BSpline(points)`, en el parámetro del usuario y con la regla
  persistida `clamped-uniform/v1`; grado de 1 a 7, de `p + 1` a 128 puntos de control, sin
  reducción silenciosa del grado, nudos anclados, multiplicidad interior máxima `p`, cierre solo
  por igualdad exacta, continuidad estructural publicada y causas tipadas de §7;
- un algoritmo subclase de `AlgoLocusV2` que publica un `GeoLocusV2` con identidad durable propia,
  rama `bspline-2d/main` y contrato de proveedor `bspline-2d/v1`, con existencia probada por el
  proveedor y sin afirmaciones de regularidad ni `G^1` certificadas;
- un evaluador sobre la biblioteca matemática compartida `org.geocedg.common.math.bspline` que
  implementa `LocusEvaluator2D`, `LocusDifferentialEvaluator2D` y `PiecewisePolynomialLocus2D`;
- la representación polinómica autoritativa local por tramo de §11.3 mediante una extensión
  estrictamente aditiva y un accesor compartido para los consumidores compatibles; coeficientes
  globales sin autoridad; declinación tipada `POLYNOMIAL_PUBLICATION_ILL_CONDITIONED` con
  `κ_max = 2^16` (candidato sujeto a validación) donde un consumidor no pueda encaminarse; límite
  de 64 tramos de la ruta polinómica unilateral con respaldo adaptativo de garantías más débiles y
  sin afirmación de completitud;
- la persistencia versionada `<geocedgBSpline contract="bspline-2d/v1" …>` de §8 con fallo
  cerrado tipado y rollback, Undo/Redo, reapertura, copia y redefinición según las reglas
  aprobadas, y lectura pasiva del bloque de procedencia de aproximación (§10.5);
- el mínimo de producto: cadena de tipo, estilo por defecto, admisión de inserción, ayuda y
  traducciones EN/ES. Sin herramienta gráfica.

SplineV2 queda bit-idéntica (BS-13).

`CHANGE_ROUTE = ORDINARY`. `VERIFICATION_CLASS = INTEGRATED_PHASE`.
`PRODUCT_PHASE_EFFECT = PRE-G9B-MAINT-A2`. Nivel de aceptación: `STATIC` +
`PHASE PRE-G9B-MAINT-A2` + `INTEGRATION` (`CORE + FOCUSED`), más smoke manual del autor.
`FINAL = NOT AUTHORIZED`; si una interfaz genérica de Locus V2 cambia, se emite
`VERIFICATION_ESCALATION_REQUEST` en lugar de ejecutar `FINAL`. `AUTHOR_DECISION = PENDING`.

## Implementation base
<!-- geocedg-field: implementation_base -->

- Repositorio `mpradovelasco/GeoCeDG`.
- Commit `228580e9684d2c54650f8d461c2a4d1142a7ef55` (`main` publicado, primera ola
  `PRE-G9B-MAINT` aprobada), árbol `a4c892aa91df1a0ac3e5c16a62bc8b87cd3064ae`.
- Después del hito de biblioteca: merge normal del commit congelado de
  `phase/pre-g9b-maint-a-math` (biblioteca compartida, responsable `MAINT-A-DXF0`), cuyo SHA
  registra el coordinador. `MAINT-A2` no implementa otra biblioteca B-spline.

Trabajo en el worktree hermano `GeoCeDG-wt-maint-a2`, rama local `phase/pre-g9b-maint-a2`. El
checkout principal y los worktrees de la primera ola no se tocan.

## Authority and evidence hierarchy

1. `AGENTS.md` (§4, §10, §11, §14, §17), código, pruebas y contratos de serialización vigentes en
   la base.
2. `bspline-2d.md` (`v1`); ADR 0043; ADR 0006, 0013, 0016, 0018, 0026, 0031 y 0032;
   especificaciones Locus V2 de `geocedg/specs/locus/`;
   [registro de aprobación A-N1](../../../docs/validation/pre_g9b_maint_a_n1_author_approval_record.md)
   (incluidas las obligaciones BS-11 y AP-2);
   [matriz A-N1](../../../docs/validation/pre_g9b_maint_a_n1_validation_matrix.md) §0 y BS-1 a BS-24.
3. Código de referencia: `AlgoSplineV2`, `SplineSemanticEvaluator2D`,
   `LocusV2PublicOperations.createSpline`, `CmdSplineV2`; expediente P0, propuesta B; este prompt.
4. Contrato de verificación vigente: `tools/agent/verify.ps1`, el registro tipado, sus esquemas y
   la sección activa de `geocedg/specs/operations/verification-levels.md`.

## Allowed scope
<!-- geocedg-field: allowed_scope -->

- Paquete nuevo bajo `org.geocedg.common.kernel` para el modelo inmutable de la definición y el
  evaluador; un algoritmo nuevo; un procesador de comando nuevo; registro del comando, sus
  cabeceras canónicas y traducciones; fábrica en `LocusV2PublicOperations`.
- Cadena de tipo, estilo por defecto, admisión de inserción y ayuda.
- Extensión aditiva de `PiecewisePolynomialLocus2D` (coeficientes locales por tramo), el accesor
  compartido de coeficientes y su uso en los consumidores polinómicos (intersección unilateral con
  objetivo, pares, ruta polinómica de interacción de puntos, envoltura de semejanzas), con
  conducta bit-idéntica para toda fuente que no ofrezca la extensión.
- Gancho protegido aditivo del escritor XML de comandos (vacío por defecto) y códec GeoCeDG del
  elemento `<geocedgBSpline>` en el manejador de comandos, con excepción tipada en la familia de
  fallo cerrado de `MyXMLio.doParseXML`; registro en `docs/upstream/modified-files.yml` de cada
  ruta upstream tocada.
- Corpus de huellas R5-B regenerado desde evidencia ejecutada, si el comando nuevo lo exige.
- Pruebas, escenarios congelados, fixtures de persistencia y guía de smoke manual.
- Guías de usuario EN/ES: sección nueva de curvas `BSpline` (`GUIDE_IMPACT`).
- Selecciones de inventario y registro de la fase `PRE-G9B-MAINT-A2`, pin del inventario y
  recuentos de `tools/agent/tests/verification-final-coverage.Tests.ps1`.
- Informe del candidato en `docs/validation/`.
- Este prompt, registrado en un commit preparatorio de gobernanza separado del commit técnico.

## Explicitly forbidden scope
<!-- geocedg-field: forbidden_scope -->

- Cambios semánticos de SplineV2, de sus pruebas o de sus bytes persistidos.
- Una interfaz genérica no aditiva; interfaces racionales en Locus V2; pesos explícitos
  (`MAINT-A-R`); periódicas, no ancladas e imágenes afines exactas sobre la red (`MAINT-A7`).
- Modelo certificado de intervalos, pares certificados, tokens D2 y certificados nuevos
  (`MAINT-A4`); exportación DXF `SPLINE` (`MAINT-A3`); importación DXF y aproximación
  (`MAINT-A-DXF0`, `MAINT-A-DXF1`); escritura de un bloque de procedencia nuevo; `MAINT-A5`.
- Reglas de identidad o de reconstrucción general; migración o reinterpretación de documentos
  antiguos.
- Una segunda implementación B-spline, copias manuales o cambios de API de la biblioteca fuera de
  un commit común autorizado por el coordinador; cualquier cambio en las ramas `MAINT-A-DXF0` o
  `MAINT-B5a`.
- Herramienta gráfica.
- La redacción normativa de los contratos aprobados.
- `CLAUDE.md`, `AGENTS.md`, otros prompts canónicos o de `ai-shell/prompts/`, el roadmap,
  `geocedg_book`, `main`, etiquetas y releases.

## Architectural placement

Núcleo Java compartido, AGENTS §4: semántica, DAG, identidad y serialización. La biblioteca pura
es una dependencia sin autoridad geométrica propia.

## Required design/specification

`bspline-2d.md` (`v1`) y ADR 0043 aprobados. Las constantes `K_pos(p)`, `K_der(p)` y `ρ_der`
de la matriz §0.1 se fijan por el procedimiento de §0.2 (derivación de cotas de redondeo y
validación independiente de alta precisión); `κ_max` se valida numéricamente.

## Geometric invariants and degeneracies

- Los de la especificación (§3 a §7).
- La evaluación coincide con una referencia racional exacta independiente dentro de E1 a E5.
- Continuidad publicada igual a la estructura de nudos; regularidad y `G^1` no establecidas.
- BS-11: el movimiento de un punto de control solo cambia los `p + 1` tramos de su soporte; un
  cambio de grado o de vector de nudos puede cambiar globalmente la definición y no se le impone
  localidad artificial.
- Invariancia de zoom y DPI; determinismo; una revisión indefinida no conserva geometría anterior.

## Compatibility and serialization

Comando y contrato nuevos. Versiones futuras desconocidas, elemento ausente o malformado, valor
de `knots` desconocido o hijo desconocido fallan cerrados con causa tipada y rollback, sin geometría
parcial ni absorción por el sistema heredado de errores de comandos. Los lectores 1.2.2 fallan como
predice §8.4 (BS-22). Sin cambio de documentos existentes.

## Required tests and commands
<!-- geocedg-field: required_checks -->

- BS-1 a BS-24 de la matriz A-N1 revisión 2 en el perímetro de `MAINT-A2`: BS-14 pertenece a
  `MAINT-A3`; BS-24 se valida como lector y contrato pasivo sobre fixtures, sin atribuir a `A2` la
  creación de una aproximación.
- Evaluación y derivadas, continuidad y degeneraciones, intersecciones, longitud, semejanzas R5,
  identidad y persistencia, Undo/Redo, rechazos tipados, compatibilidad de lectores, inventario de
  consumidores de coeficientes globales y regresión bit-idéntica de SplineV2.
- Sin cambios: `G9S1SemanticSpline2DTest`, `G9S1R1StructuralSplineContinuityTest`,
  `G9S1R1SplinePrecisionAdmissionTest`, `G9S1R1SplinePairMaterializationTest`,
  `PreG9BR4SplineV2AuthoringTest` y las selecciones CORE de SplineV2.

Sobre el commit técnico congelado y limpio:

```powershell
git diff --check 228580e9684d2c54650f8d461c2a4d1142a7ef55 HEAD
& tools/agent/verify.ps1 -Profile STATIC -LogDirectory <directorio-nuevo>
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-MAINT-A2 -LogDirectory <directorio-nuevo>
& tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory <directorio-nuevo>
```

Registrar comandos exactos, códigos de salida, identificadores, rutas de registro, cobertura,
diagnósticos. No ejecutar `FINAL`. No atribuir cobertura exhaustiva a `CORE + FOCUSED`.

## Authorization boundary
<!-- geocedg-field: authorization_boundary -->

Autorizado: la familia `bspline-2d/v1`, sus pruebas, el registro de este prompt y de la fase
`PRE-G9B-MAINT-A2`, los commits locales y la verificación `STATIC`, `PHASE` e `INTEGRATION`. No
autoriza `MAINT-A3`, `A4`, `A5`, `A7`, `A-DXF1`, `A-R`, otro incremento ni `FINAL`. La aceptación
técnica no es aprobación autoral.

## Publication boundary
<!-- geocedg-field: publication_boundary -->

Commits locales en `phase/pre-g9b-maint-a2`. Sin push, promoción a `main`, etiquetas, releases ni
paquetes de publicación. El worktree se conserva para la revisión del autor.

## Acceptance and closeout

Estado final: `PRE-G9B-MAINT-A2 — TECHNICAL CANDIDATE — PENDING AUTHOR REVIEW`, con smoke manual
del autor pendiente.

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
```

## Required artifacts

Commit técnico (con inventario y registro), identificadores de verificación, escenarios y
evidencia congelados, informe con resultados BS y constantes derivadas, guía de smoke,
`GUIDE_IMPACT = UPDATE` (curvas `BSpline`), impacto de bootstrap: sin cambio.

## Stop conditions

- Necesidad de modificar SplineV2 de forma semántica.
- Una interfaz genérica que no pueda ser aditiva.
- La persistencia no puede fallar cerrada con rollback sin cambiar la conducta de documentos
  existentes.
- La familia necesita pesos explícitos o certificados no autorizados.
- El condicionamiento previsto en §11.3 no puede resolverse.
- Se precisa modificar reglas de identidad o de reconstrucción general.
- Un fallo de `INTEGRATION` atribuible al cambio.

## Deferred debts

`MAINT-A3`, `MAINT-A-DXF1`, `MAINT-A4`, `A5`, `A7` y `MAINT-A-R`.
