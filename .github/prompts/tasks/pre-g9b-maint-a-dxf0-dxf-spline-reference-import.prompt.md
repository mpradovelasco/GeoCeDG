# GeoCeDG — PRE-G9B-MAINT-A-DXF0: importación DXF `SPLINE` como referencia externa de solo lectura

**PROMPT DE TAREA AUTORIZADO POR EL AUTOR EL 2026-11-24** («GeoCeDG — PRE-G9B-MAINT. Segunda ola
coordinada: MAINT-B5a + MAINT-A-DXF0 + MAINT-A2», §0, §6 y §8). Es la versión reconciliada del
prompt candidato del expediente P0
([`maint-a-dxf0-dxf-spline-reference-import.prompt-candidate.md`](../../../docs/architecture/pre_g9b_maint_p0_candidate_prompts/maint-a-dxf0-dxf-spline-reference-import.prompt-candidate.md))
con el contrato `dxf-spline-reference/v1` revisión 2, la especificación `bspline-2d` §12, el
ADR 0043, la matriz A-N1 revisión 2 y la instrucción del autor; donde discrepan prevalecen los
contratos aprobados y la instrucción. `selfApproved=false`. No autoriza ningún otro incremento.

## Objective
<!-- geocedg-field: objective -->

Implementar exclusivamente
[`dxf-spline-reference/v1`](../../../geocedg/specs/import/dxf-spline-reference.md) con
persistencia `R-a` (solo sesión): leer entidades `SPLINE` 2D de un DXF ASCII, polinómicas y
racionales, definidas por grado, nudos, puntos de control y pesos (explícitos o unitarios
implícitos, §4.6), y presentarlas como referencias externas inmutables, con procedencia, render
determinado en unidades de modelo, presentación distinta, inspector de solo lectura, importación
desde el menú, transformación afín elegida explícitamente, unidades registradas sin escalado
implícito y diagnósticos tipados por entidad. El archivo DXF no se modifica.

Una referencia no es `GeoElement`, no entra en `Construction` ni en el DAG, no es `GeoLocusV2`,
no alimenta herramientas, comandos ni ajuste magnético, no es autoridad métrica, de intersección
ni de tangencia y no aparece en las exportaciones semánticas. El render nunca sustituye a su
definición.

Este incremento es el **responsable único** de la biblioteca matemática pura compartida
`org.geocedg.common.math.bspline` ([`bspline-2d.md`](../../../geocedg/specs/curves/bspline-2d.md)
§12), desarrollada en la rama técnica compartida `phase/pre-g9b-maint-a-math` (worktree
`GeoCeDG-wt-maint-a-math`) como subtrabajo de este incremento y de `MAINT-A2`, no como incremento
propio. Su commit congelado es un hito técnico de dependencia, nunca un PASS autoral, y se
incorpora a esta rama y a la de `MAINT-A2` por merge normal, sin copia, squash ni duplicación.

`CHANGE_ROUTE = ORDINARY`. `VERIFICATION_CLASS = INTEGRATED_PHASE`.
`PRODUCT_PHASE_EFFECT = PRE-G9B-MAINT-A-DXF0`. Nivel de aceptación: `STATIC` +
`PHASE PRE-G9B-MAINT-A-DXF0` + `INTEGRATION` (`CORE + FOCUSED`), más smoke manual del autor.
`FINAL = NOT AUTHORIZED`. `AUTHOR_DECISION = PENDING`.

## Implementation base
<!-- geocedg-field: implementation_base -->

- Repositorio `mpradovelasco/GeoCeDG`.
- Commit `228580e9684d2c54650f8d461c2a4d1142a7ef55` (`main` publicado, primera ola
  `PRE-G9B-MAINT` aprobada), árbol `a4c892aa91df1a0ac3e5c16a62bc8b87cd3064ae`.
- Después del hito de biblioteca: merge normal del commit congelado de
  `phase/pre-g9b-maint-a-math`, cuyo SHA registra el coordinador.

Trabajo en el worktree hermano `GeoCeDG-wt-maint-a-dxf0`, rama local
`phase/pre-g9b-maint-a-dxf0`. El checkout principal y los worktrees de la primera ola no se tocan.

## Authority and evidence hierarchy

1. `AGENTS.md` (§4, §13, §17), código, pruebas y contratos de serialización vigentes en la base.
2. `dxf-spline-reference/v1` revisión 2; `bspline-2d.md` §3, §11 y §12; ADR 0043;
   [registro de aprobación A-N1](../../../docs/validation/pre_g9b_maint_a_n1_author_approval_record.md);
   [matriz A-N1](../../../docs/validation/pre_g9b_maint_a_n1_validation_matrix.md) §0 y DX-1 a DX-13;
   [sistema de unidades](../../../geocedg/specs/units/unit-system.md).
3. Expediente P0, propuesta B, y este prompt.
4. Contrato de verificación vigente: `tools/agent/verify.ps1`, el registro tipado, sus esquemas y
   la sección activa de `geocedg/specs/operations/verification-levels.md`.

## Allowed scope
<!-- geocedg-field: allowed_scope -->

- Biblioteca pura `org.geocedg.common.math.bspline` en `source/shared/common` y sus pruebas
  directas en `source/shared/common-jre`, solo en la rama compartida
  `phase/pre-g9b-maint-a-math`, con el contrato de §12 de la especificación de familia: datos
  inmutables validados, de Boor homogéneo, derivadas polinómicas y racionales, búsqueda de tramo y
  propiedad de nudos, inserción de nudos, forma de Bernstein y forma local de potencias por tramo,
  continuidad estructural, codificación canónica y digest, rechazos tipados y límites de trabajo.
- Lector DXF ASCII de entidades `SPLINE`, modelo de referencia inmutable, transformación afín,
  procedencia, informe de importación y teselación, en paquetes GeoCeDG fuera del núcleo
  (`org.geocedg.common.importer` u homólogo en `source/shared/common`, sin importar tipos de
  `Construction`, `GeoElement` ni del núcleo), con sus pruebas.
- Capa Desktop: almacén de referencias de la sesión del documento (`R-a`), su vaciado en
  New/Open/Close, aviso de solo sesión al guardar, dibujo de la referencia con presentación propia
  en la vista gráfica, inspector de solo lectura, acción de menú «Importar referencia DXF…» y
  diálogo de transformación y unidades, con traducciones EN/ES (`menu*.properties`).
- Registro de archivos upstream modificados (`docs/upstream/modified-files.yml`) si se toca una
  ruta upstream, y del inventario de recursos si se añade un recurso.
- Fixtures DXF reproducibles con procedencia en el árbol de pruebas; guía de smoke manual con
  archivos DXF reproducibles.
- Guías de usuario EN/ES: sección nueva de referencias DXF importadas (`GUIDE_IMPACT`).
- Selecciones de inventario y registro de la fase `PRE-G9B-MAINT-A-DXF0`, pin del inventario y
  recuentos de `tools/agent/tests/verification-final-coverage.Tests.ps1`.
- Informe del candidato en `docs/validation/`.
- Este prompt, registrado en un commit preparatorio de gobernanza separado del commit técnico.

## Explicitly forbidden scope
<!-- geocedg-field: forbidden_scope -->

- Crear objetos de `Construction`, `GeoElement`, `GeoLocusV2` o cualquier elemento del DAG a
  partir de una entidad importada; modificar el XML de `Construction` o su historial de Undo.
- Que una referencia sea entrada de herramientas, comandos, ajuste magnético, capacidades de
  Locus V2, métricas, intersecciones o tangentes; asociaciones por proximidad o coordenadas.
- Escribir en el archivo DXF; cambiar el exportador DXF, sus contratos o sus pruebas.
- Splines solo con puntos de ajuste, periódicas, no ancladas, 3D, en espacio papel o en bloques;
  DXF binario (todas con diagnóstico, nunca admitidas).
- Persistencia `R-c` o `R-d`; ajuste o aproximación de una referencia (`MAINT-A-DXF1`); NURBS
  nativa (`MAINT-A-R`); interfaces racionales en Locus V2.
- Una segunda implementación B-spline, copias manuales o cambios de API de la biblioteca fuera de
  un commit común autorizado por el coordinador; cualquier cambio en la rama `MAINT-A2` o en la de
  `MAINT-B5a`.
- La redacción normativa de los contratos aprobados.
- `CLAUDE.md`, `AGENTS.md`, otros prompts canónicos o de `ai-shell/prompts/`, el roadmap,
  `geocedg_book`, `main`, etiquetas y releases.

## Architectural placement

Adaptador de importación fuera del núcleo (AGENTS §4); almacén de sesión y render en la capa de
aplicación Desktop; biblioteca matemática pura sin autoridad geométrica.

## Required design/specification

`dxf-spline-reference/v1` revisión 2 y `bspline-2d.md` §12 aprobados. Las constantes candidatas
(`FILE_BYTES_MAX`, `SPLINE_ENTITIES_MAX`, `N_MAX_IMPORT`, `W_RATIO_MAX`, `RENDER_REL_TOL`) se
implementan con sus valores candidatos y se revisan tras la caracterización; las constantes
racionales de los criterios E1 a E5 se fijan por el procedimiento de la matriz §0.2. Si hace falta
ampliar el contrato de importación, se detiene.

## Geometric invariants and degeneracies

- Datos leídos conservados bit a bit tras la lectura decimal declarada (§4.5).
- Orientación y dominio `[u_p, u_{n+1}]` del archivo; nunca se invierten ni renormalizan.
- Cierre solo por igualdad exacta de `P_0` y `P_n`; una bandera contradicha se diagnostica.
- Naturaleza decidida solo por los pesos efectivos, por comparación exacta; pesos implícitos
  nunca registrados como valores del código 41.
- Transformación solo de puntos de control, en el orden de operaciones declarado.
- Teselación en unidades de modelo con tolerancia declarada, idéntica a cualquier zoom y DPI.
- Ninguna mutación del DAG, del XML de la construcción ni de Undo.

## Compatibility and serialization

Con `R-a`, ningún cambio de formato: las referencias no se guardan; al guardar con referencias se
avisa de que son de sesión y al reabrir hay que volver a importarlas. Se declara en la guía.

## Required tests and commands
<!-- geocedg-field: required_checks -->

- Biblioteca: equivalencia polinómica con pesos unitarios (bit a bit con de Boor polinómico),
  evaluación racional de arcos circulares, nudos múltiples, extremos del dominio, derivadas,
  degeneraciones, reproducibilidad y condicionamiento, contra referencias independientes en
  aritmética racional exacta.
- DX-1 a DX-13 de la matriz A-N1 revisión 2, incluidos los fixtures Q y H de §2.1 con las tres
  comprobaciones separadas, pesos opcionales del código 41, banderas incoherentes, archivos
  parcialmente válidos, referencias no seleccionables, archivo intacto, construcción y Undo
  intactos, zoom y DPI, procedencia determinista, límites de archivo, entidades y pesos, ciclo de
  sesión y transformación y unidades.
- Smoke manual preparado con archivos DXF reproducibles.

Sobre el commit técnico congelado y limpio:

```powershell
git diff --check 228580e9684d2c54650f8d461c2a4d1142a7ef55 HEAD
& tools/agent/verify.ps1 -Profile STATIC -LogDirectory <directorio-nuevo>
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-MAINT-A-DXF0 -LogDirectory <directorio-nuevo>
& tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory <directorio-nuevo>
```

Registrar comandos exactos, códigos de salida, identificadores, rutas de registro, cobertura,
diagnósticos. No ejecutar `FINAL`; si una campaña exige escalado, emitir
`VERIFICATION_ESCALATION_REQUEST`. No atribuir cobertura exhaustiva a `CORE + FOCUSED`.

## Authorization boundary
<!-- geocedg-field: authorization_boundary -->

Autorizado: la biblioteca compartida como subtrabajo, la importación `R-a`, sus pruebas, el
registro de este prompt y de la fase `PRE-G9B-MAINT-A-DXF0`, los commits locales y la verificación
`STATIC`, `PHASE` e `INTEGRATION`. No autoriza `MAINT-A-DXF1`, `R-c`, `R-d`, `MAINT-A-R`, otro
incremento ni `FINAL`. La aceptación técnica no es aprobación autoral.

## Publication boundary
<!-- geocedg-field: publication_boundary -->

Commits locales en `phase/pre-g9b-maint-a-math` (solo biblioteca) y
`phase/pre-g9b-maint-a-dxf0`. Sin push, promoción a `main`, etiquetas, releases ni paquetes de
publicación. Los worktrees se conservan para la revisión del autor.

## Acceptance and closeout

Estado final: `PRE-G9B-MAINT-A-DXF0 — TECHNICAL CANDIDATE — PENDING AUTHOR REVIEW`, con smoke
manual del autor pendiente.

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
```

## Required artifacts

Commit de biblioteca (hito de dependencia) y commit técnico (con inventario y registro);
identificadores de verificación; fixtures con procedencia; informe con resultados DX-1 a DX-13,
constantes fijadas y su derivación; guía de smoke; `GUIDE_IMPACT = UPDATE` (referencias DXF
importadas); impacto de bootstrap: sin cambio.

## Stop conditions

- La referencia necesita entrar en `Construction`, en el DAG o en la identidad.
- El lector requiere modificar el exportador DXF o sus contratos.
- Se introduce una asociación geométrica por proximidad.
- Una entidad admitida no se reproduce dentro de la tolerancia declarada.
- Se modifica el XML de `Construction`.
- Es necesario ampliar el contrato de importación o cambiar la especificación de la biblioteca.
- Un fallo de `INTEGRATION` atribuible al cambio.

## Deferred debts

Variantes excluidas; persistencia `R-c`; `R-b` y `R-d`; `MAINT-A-DXF1`; exportaciones de imagen
con referencias visibles (Q-DXF-3) más allá de la presentación de la vista.
