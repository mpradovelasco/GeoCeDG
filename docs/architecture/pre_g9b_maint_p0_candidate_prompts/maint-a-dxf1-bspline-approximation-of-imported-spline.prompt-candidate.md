# GeoCeDG — PRE-G9B-MAINT-A-DXF1: B-spline polinómica aproximante de una `SPLINE` importada

**PROMPT CANDIDATO — NO AUTORIZADO.** Requiere `MAINT-A2` y `MAINT-A-DXF0` cerrados, las
decisiones D-A-8 a D-A-10, un contrato breve de aproximación aprobado y una instrucción
autoral con base exacta.

## Objective
<!-- geocedg-field: objective -->

Permitir que el usuario genere explícitamente, a partir de una referencia importada, una
B-spline polinómica nueva de la familia `bspline-2d/v1`, distinta de la geometría original:

- conversión exacta cuando la fuente es polinómica y está dentro de los límites;
- en otro caso, ajuste por mínimos cuadrados en el parámetro de la fuente, con grado y nudos
  declarados, extremos interpolados, inserción adaptativa de nudos y tolerancia explícita;
- medidas de error declaradas por separado: residuo en muestras y estimación numérica del
  error entre curvas, y la cota global certificada si D-A-9 la incluye;
- registro de procedencia y aproximación congelada e independiente (D-A-10).

`CHANGE_ROUTE=ORDINARY`. `VERIFICATION_CLASS=INTEGRATED_PHASE`. Nivel previsto: `STATIC` +
`PHASE PRE-G9B-MAINT-A-DXF1` + `INTEGRATION`, más smoke autoral.

## Implementation base
<!-- geocedg-field: implementation_base -->

Commit y árbol designados por el autor, que contengan A2 y A-DXF0 aprobados.

## Authority and evidence hierarchy

`bspline-2d.md` y `dxf-spline-reference/v1` aprobados; contrato breve de aproximación;
`AGENTS.md` §4 y §11; [propuesta B](../pre_g9b_maint_p0_bspline_proposal.md) §5.4 y §9.

## Allowed scope
<!-- geocedg-field: allowed_scope -->

- Contrato breve de aproximación (`bspline-param-lsq/v1`, propuesto), aprobado antes del
  código: método, límites, medidas y su grado, fallo tipado, procedencia.
- Servicio de ajuste fuera del núcleo sobre la biblioteca matemática común; produce los datos
  de una B-spline nueva.
- Creación de la aproximante mediante la familia existente, con un solo paso de Undo.
- Bloque pasivo de procedencia en el contrato de la familia, si D-A-10 lo adopta.
- Diálogo Desktop: grado, tolerancia, límites, medidas resultantes y su grado; textos
  «sobre la aproximación».
- Pruebas y selecciones `PRE-G9B-MAINT-A-DXF1`.

## Explicitly forbidden scope
<!-- geocedg-field: forbidden_scope -->

- Presentar la aproximante como exacta o atribuir sus intersecciones, longitudes o tangentes a
  la fuente.
- Reajuste automático ante cambios de la fuente (modo dinámico, diferido).
- Introducir la referencia en `Construction` (R-d).
- Pesos en la aproximante y NURBS nativa.
- Cambiar la evaluación de la familia o sus garantías.

## Architectural placement

Servicio de ajuste fuera del núcleo; la aproximante es una construcción del núcleo de la
familia existente; la procedencia es un dato pasivo.

## Required design/specification

Contrato breve de aproximación aprobado.

## Geometric invariants and degeneracies

- Correspondencia paramétrica en el dominio y la orientación de la fuente.
- Extremos tomados de los datos (`P_0`, `P_n`), no evaluados.
- Fuente cerrada exactamente → aproximante cerrada `C^0` exactamente.
- Continuidad de la aproximante publicada por sus propios nudos.
- Residuo ≤ estimación ≤ cota certificada, cuando existen las tres.
- Ningún objeto si no se alcanza la tolerancia dentro de los límites.

## Compatibility and serialization

La aproximante se guarda como cualquier objeto de la familia; con D-A-10 recomendada, más el
bloque de procedencia versionado. Ningún cambio en documentos existentes.

## Required tests and commands
<!-- geocedg-field: required_checks -->

- Casos AP-1 a AP-6 de la propuesta B §12.
- Determinismo del ajuste para los mismos datos y parámetros.
- Edición posterior: medidas declaradas no aplicables.
- Si se incluye la cota certificada: verificación frente a una evaluación independiente de
  alta precisión en una batería con pesos muy distintos.
- Comandos: `STATIC`, `PHASE PRE-G9B-MAINT-A-DXF1` e `INTEGRATION`; `git diff --check`.

## Authorization boundary
<!-- geocedg-field: authorization_boundary -->

Este incremento; no autoriza el modo dinámico, R-d ni `MAINT-A-R`.

## Publication boundary
<!-- geocedg-field: publication_boundary -->

Rama local. Sin push, `main`, etiquetas ni releases.

## Acceptance and closeout

Candidato congelado, guía de smoke manual con archivos DXF del autor.

## Required artifacts

Contrato breve, commits, verificación, informe con las medidas de la batería;
`GUIDE_IMPACT = UPDATE` (sección de aproximaciones).

## Stop conditions

- Una medida no puede declararse con el grado que el contrato exige.
- La aproximación exige cambiar la evaluación o las garantías de la familia.
- La relación con la fuente exige el modo dinámico.

## Deferred debts

`MAINT-A-DXF1b` (cota certificada, si no entra aquí), modo dinámico vinculado, edición manual
asistida con remedición.
