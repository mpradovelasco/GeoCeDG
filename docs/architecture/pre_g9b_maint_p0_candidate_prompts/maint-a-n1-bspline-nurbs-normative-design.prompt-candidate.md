# GeoCeDG — PRE-G9B-MAINT-A-N1: diseño normativo de la familia B-spline preparada para NURBS y del contrato de importación de referencia

**PROMPT CANDIDATO — NO AUTORIZADO.** D-A-1 a D-A-10 quedaron aprobadas como dirección el
2026-11-23 y `MAINT-A-N1` está en la primera ola de planificación; su ejecución requiere una
instrucción autoral con base exacta. Sustituye al candidato `MAINT-A1` de la revisión 1 del
expediente. Persistencia de referencias: R-a en la primera implementación, R-c como objetivo
posterior prioritario (con caracterización previa), R-d no autorizada.

## Objective
<!-- geocedg-field: objective -->

Redactar como candidatos, para aprobación autoral, dos documentos aprobables por separado:

1. La especificación `geocedg/specs/curves/bspline-2d.md` y el ADR siguiente libre
   («Explicit 2D B-spline family, rational-ready, as a Locus V2 provider»). Recogen:
   - la definición general racional y su subconjunto polinómico `bspline-2d/v1`;
   - el alcance inicial, la API y la persistencia versionada y serializada;
   - la continuidad estructural por nudos y la política numérica;
   - la relación con SplineV2;
   - la reserva de pesos y de `SPLINE` DXF;
   - el diseño de la extensión racional `MAINT-A-R`, sin autorizarla.
2. El contrato de importación de referencia `dxf-spline-reference/v1`, fuera del núcleo:
   alcance, validación, procedencia, render, distinción frente a las construcciones CeDG y
   persistencia según D-A-7.

Fija además la colocación y la API de la biblioteca matemática común (propuesta B §5.3).

`CHANGE_ROUTE=ORDINARY`. `VERIFICATION_CLASS=DOCUMENTATION_STATUS_ONLY`. Nivel previsto:
`STATIC`.

## Implementation base
<!-- geocedg-field: implementation_base -->

Commit y árbol designados por el autor.

## Authority and evidence hierarchy

`AGENTS.md` §4, §9, §11, §13; `semantic-spline-2d.md`; ADR 0014, 0018, 0021, 0022 y 0028;
`dxf-curve-fidelity-and-approximation.md` §4 y §9; condiciones D-V4 (roadmap, 2026-11-21).
Evidencia: [propuesta B](../pre_g9b_maint_p0_bspline_proposal.md), sobre todo §2, §5, §6, §8
y §10, e informe §A.

## Allowed scope
<!-- geocedg-field: allowed_scope -->

- Especificación nueva y ADR nuevo de la familia, ambos marcados como candidatos.
- Contrato nuevo de importación de referencia, marcado como candidato.
- Matriz de validación candidata con los casos BS-1 a BS-15 y DX-1 a DX-7.
- Caracterización de solo lectura necesaria para fijar el diseño (sin cambiar código):
  - qué consumidores de Locus V2 dependen solo de `LocusEvaluator2D` y
    `LocusDifferentialEvaluator2D` y cuáles de `PiecewisePolynomialLocus2D`;
  - qué admite el lector XML para un identificador de contrato serializado;
  - cómo trata 1.2.2 una entrada desconocida del archivo `.cedg`.

## Explicitly forbidden scope
<!-- geocedg-field: forbidden_scope -->

- Código.
- `semantic-spline-2d.md` y los ADR de SplineV2, salvo una referencia cruzada aditiva si el
  autor la pide.
- La especificación DXF de exportación.
- El roadmap.
- Aprobar o especificar como autorizada la NURBS nativa.

## Architectural placement

- Familia: especificación del núcleo compartido (familia de curvas semánticas).
- Importación: contrato de un adaptador fuera del núcleo.
- Biblioteca matemática: código puro y sin estado, sin dependencias de `Construction`,
  `GeoElement` ni de la vista.

## Required design/specification

La especificación de la familia debe fijar:

- la definición racional, la forma homogénea y el caso `w ≡ 1`;
- la regla de propiedad de nudos;
- precondiciones, rechazos tipados y degeneraciones, incluidos los de pesos para la
  extensión;
- continuidad paramétrica por multiplicidad, y regularidad y `G^1` como hechos distintos;
- el cierre por igualdad exacta;
- el parámetro semántico según D-A-2;
- las formas del comando, la regla `clamped-uniform/v1` y los límites según D-A-3;
- la publicación polinómica y su condicionamiento: base local por tramo o comprobación que
  falle cerrada (decisión técnica que la especificación debe tomar y justificar);
- las semejanzas por composición y los pares solo ricos;
- el identificador de contrato serializado, la regla de versiones (`v1` subconjunto exacto de
  la versión racional) y los lectores anteriores;
- la separación entre evaluación numérica, evidencia condicionada y resultados certificados;
- la ubicación del registro de procedencia de aproximación (D-A-10) y su comportamiento en
  copia, redefinición y persistencia, o su remisión expresa al contrato de aproximación de
  `MAINT-A-DXF1`;
- las garantías publicadas.

El contrato de importación debe fijar el alcance de la propuesta B §8.3 según D-A-6, la
procedencia de §8.4, las reglas de distinción de §8.5 y la persistencia R-a de D-A-7. R-c se
deja como objetivo posterior, con la lista de lo que su caracterización previa debe establecer
(compatibilidad del archivo `.cedg`, lectores anteriores, guardado y reapertura). R-d no se
especifica.

## Geometric invariants and degeneracies

Los de la propuesta B §2 y §8.

## Compatibility and serialization

Sin efecto en documentos existentes; contrato versionado y serializado desde el origen. La
importación no cambia el formato del documento con R-a.

## Required tests and commands
<!-- geocedg-field: required_checks -->

`STATIC`; `git diff --check`.

## Authorization boundary
<!-- geocedg-field: authorization_boundary -->

Redacción candidata; la aprobación es autoral y no autoriza `MAINT-A2`, `MAINT-A-DXF0` ni
`MAINT-A-R`.

## Publication boundary
<!-- geocedg-field: publication_boundary -->

Rama local. Sin push, `main`, etiquetas ni releases.

## Acceptance and closeout

Especificación, ADR y contrato de importación candidatos para revisión, con las respuestas
de la caracterización de solo lectura.

## Required artifacts

Documentos, `STATIC`.

## Stop conditions

- La especificación exige cambiar SplineV2 o una interfaz genérica de Locus V2 de forma no
  aditiva.
- La preparación para NURBS exige una interfaz racional general en el núcleo antes de
  `MAINT-A-R`.
- El contrato de importación exige que la referencia participe en el DAG (R-d no está
  autorizada).

## Deferred debts

`MAINT-A-R`, `MAINT-A4`, `A5` y `A7`; variantes de importación diferidas.
