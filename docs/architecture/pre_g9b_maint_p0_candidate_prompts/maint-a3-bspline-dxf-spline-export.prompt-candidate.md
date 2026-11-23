# GeoCeDG — PRE-G9B-MAINT-A3: exportación DXF `SPLINE` de `BSpline`

**PROMPT CANDIDATO — NO AUTORIZADO.** Requiere `MAINT-A2` aprobado, la decisión D-A-4 y una
instrucción autoral con base exacta. No depende de `MAINT-A-DXF0` ni de `MAINT-A-DXF1`.

## Objective
<!-- geocedg-field: objective -->

Exportar una `BSpline` como entidad DXF `SPLINE` no racional, anclada y no periódica, con los
datos autoritativos sin transformar:

- grado, nudos y puntos de control escritos con `Double.toString`;
- bandera plana, y bandera de cierre solo con cierre exacto;
- sin puntos de ajuste.

Se añade una clase de fidelidad «transferencia exacta de la definición» y su registro en el
manifiesto lateral. La conformidad se demuestra con dos lectores independientes. SplineV2 y
las demás curvas no cambian.

`CHANGE_ROUTE=ORDINARY`. `VERIFICATION_CLASS=INTEGRATED_PHASE`. Nivel previsto: `STATIC` +
`PHASE PRE-G9B-MAINT-A3` + `INTEGRATION`, más evidencia externa de lectores.

## Implementation base
<!-- geocedg-field: implementation_base -->

Commit y árbol designados por el autor, que contengan A2 aprobado.

## Authority and evidence hierarchy

`AGENTS.md` §13; ADR 0005 y 0014 (decisión 8); `geometry-export-foundation.md`;
`dxf-curve-fidelity-and-approximation.md` §4, §6, §8, §9; condiciones D-V4; `bspline-2d.md`.

## Allowed scope
<!-- geocedg-field: allowed_scope -->

- Enmienda candidata de `dxf-curve-fidelity-and-approximation.md`: entidad `SPLINE` y clase
  de fidelidad nueva, aprobada antes del código.
- `GeometryExportModel`: tipo de geometría B-spline de solo lectura.
- `G9X1GeometryExportAdapter` y `DxfExporter`: escritura de `SPLINE`.
- `DxfFidelityManifestWriter`: versión de esquema nueva si es necesaria.
- Pruebas y selecciones.
- Guion de conformidad de lectores fuera de las rutas de producto.

## Explicitly forbidden scope
<!-- geocedg-field: forbidden_scope -->

- Lógica geométrica nueva en el exportador.
- Exportar SplineV2 como `SPLINE`.
- Importación DXF.
- `SPLINE` racional o periódica.
- Cambiar la geometría autoritativa.

## Architectural placement

Adaptador de exportación fuera del núcleo, que consume datos de solo lectura (AGENTS §4 y
§13).

## Required design/specification

Enmienda DXF aprobada.

## Geometric invariants and degeneracies

- Exportar no muta DAG, XML ni Undo.
- Los datos escritos son idénticos bit a bit a los autoritativos.
- El cierre solo es exacto.

## Compatibility and serialization

DXF `AC1015` sin cambios para el resto de entidades; el manifiesto declara la clase nueva.

## Required tests and commands
<!-- geocedg-field: required_checks -->

- Ida y vuelta textual de los datos.
- Manifiesto ligado al hash del DXF.
- Salida pareada atómica.
- Lectores: AutoCAD Core Console y un segundo lector designado por el autor reproducen los
  puntos de referencia (grados 1 a 7, nudos no uniformes y múltiples, cierre `C^0`) dentro de
  una tolerancia de lectura declarada. Si existe el lector de `MAINT-A-DXF0`, puede usarse
  para una prueba de ida y vuelta, pero no cuenta como lector independiente.
- Sin cambios: `GeometryExportFoundationTest`, `G9X1PreflightFidelityTest`,
  `G9X1LocusV2ExportTest`, `G9X1DxfManifestTest`, `G9X1PairedOutputTest`,
  `PreG9BS1AuthorDxfReproductionTest`.
- Comandos: `STATIC`, `PHASE PRE-G9B-MAINT-A3` e `INTEGRATION`; `git diff --check`.

## Authorization boundary
<!-- geocedg-field: authorization_boundary -->

Este incremento.

## Publication boundary
<!-- geocedg-field: publication_boundary -->

Rama local. Sin push, `main`, etiquetas ni releases.

## Acceptance and closeout

Candidato congelado con la evidencia de lectores.

## Required artifacts

Enmienda, commits, verificación, DXF de referencia y salidas de los lectores con sus hashes;
`GUIDE_IMPACT = UPDATE` (sección DXF).

## Stop conditions

- Un lector no conforme.
- Necesidad de transformar los datos (más allá de escribirlos).
- Cualquier cambio en las demás entidades.

## Deferred debts

`MAINT-A5` (`SPLINE` derivada de SplineV2); `SPLINE` racional, que depende de `MAINT-A-R`.
