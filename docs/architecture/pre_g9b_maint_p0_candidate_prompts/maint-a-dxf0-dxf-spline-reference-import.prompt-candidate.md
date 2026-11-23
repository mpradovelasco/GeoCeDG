# GeoCeDG — PRE-G9B-MAINT-A-DXF0: importación DXF `SPLINE` de referencia, de solo lectura

**PROMPT CANDIDATO — NO AUTORIZADO.** Requiere el contrato de importación de `MAINT-A-N1`
aprobado, las decisiones D-A-6 y D-A-7 y una instrucción autoral con base exacta. No depende
de `MAINT-A2`.

## Objective
<!-- geocedg-field: objective -->

Leer entidades DXF `SPLINE` 2D, polinómicas y racionales, definidas por puntos de control,
grado, nudos y, cuando corresponda, pesos, y representarlas fielmente como geometría
importada de referencia:

- reconocimiento de su naturaleza polinómica o racional;
- datos originales y procedencia conservados;
- evaluación para el render, independiente del zoom;
- tratamiento explícito de orientación, dominio, transformación de coordenadas y cierre;
- rechazo diagnosticado por entidad de las variantes no admitidas;
- el archivo DXF nunca se modifica.

La geometría importada no es un objeto CeDG: no entra en el DAG, no se convierte en
`GeoLocusV2` y su render no certifica nada.

`CHANGE_ROUTE=ORDINARY`. `VERIFICATION_CLASS=INTEGRATED_PHASE`. Nivel previsto: `STATIC` +
`PHASE PRE-G9B-MAINT-A-DXF0` + `INTEGRATION`, más smoke autoral.

## Implementation base
<!-- geocedg-field: implementation_base -->

Commit y árbol designados por el autor, que contengan el contrato de importación aprobado.

## Authority and evidence hierarchy

`AGENTS.md` §4 y §13; contrato `dxf-spline-reference/v1` aprobado; condiciones D-V4;
[propuesta B](../pre_g9b_maint_p0_bspline_proposal.md) §5.3, §5.4 y §8; informe §A.8.

## Allowed scope
<!-- geocedg-field: allowed_scope -->

- Lector DXF ASCII de la sección `ENTITIES` limitado a `SPLINE`, en la capa de adaptadores
  fuera del núcleo.
- Modelo de referencia inmutable de la sesión (R-a) o, si D-A-7 lo decide, copia fuera de
  `Construction` (R-c).
- Biblioteca matemática común (de Boor homogéneo, inserción de nudos), si este incremento es
  el primero que la necesita, con la API fijada por A-N1.
- Render en Desktop con presentación propia de referencia; inspector de solo lectura con datos
  y procedencia; acción de menú «Importar referencia DXF…».
- Traducciones EN/ES, guía, pruebas, fixtures DXF con procedencia y selecciones
  `PRE-G9B-MAINT-A-DXF0`.

## Explicitly forbidden scope
<!-- geocedg-field: forbidden_scope -->

- Crear objetos de `Construction`, `GeoLocusV2` o cualquier elemento del DAG a partir de una
  entidad importada.
- Que una referencia sea entrada de herramientas de construcción, de ajuste magnético o de
  comandos de Locus V2.
- Escribir en el archivo DXF; reescribir referencias en la exportación DXF.
- Splines solo con puntos de ajuste, periódicas, no ancladas, 3D, en espacio papel o en
  bloques; DXF binario (todas con diagnóstico).
- Aproximación (`MAINT-A-DXF1`) y NURBS nativa (`MAINT-A-R`).

## Architectural placement

Adaptador de importación fuera del núcleo (AGENTS §4); render en la capa Desktop; biblioteca
matemática pura sin autoridad geométrica.

## Required design/specification

Contrato `dxf-spline-reference/v1` aprobado.

## Geometric invariants and degeneracies

- Los datos leídos se conservan bit a bit tras la lectura decimal declarada.
- Orientación y dominio `[u_p, u_{n+1}]` del archivo; nunca se invierten.
- Cierre solo por igualdad exacta de `P_0` y `P_n`; una bandera contradicha se diagnostica.
- Pesos finitos y estrictamente positivos; `z` constante.
- Teselación con tolerancia declarada en unidades del modelo, idéntica a cualquier zoom.
- Ninguna mutación del DAG, del XML de la construcción ni de Undo.

## Compatibility and serialization

Con R-a, ningún cambio de formato. Con R-c, entrada nueva del archivo del documento fuera de
`Construction`, con la conducta de los lectores 1.2.2 caracterizada en A-N1 y declarada.

## Required tests and commands
<!-- geocedg-field: required_checks -->

- Casos DX-1 a DX-7 de la propuesta B §12, con arcos de circunferencia racionales cuyos puntos
  exactos son conocidos.
- Archivo intacto (resumen antes y después).
- La referencia no aparece en `Construction`, en el XML ni en el historial de Undo.
- Comandos: `STATIC`, `PHASE PRE-G9B-MAINT-A-DXF0` e `INTEGRATION`; `git diff --check`.

## Authorization boundary
<!-- geocedg-field: authorization_boundary -->

Este incremento; no autoriza `MAINT-A-DXF1` ni la persistencia R-d.

## Publication boundary
<!-- geocedg-field: publication_boundary -->

Rama local. Sin push, `main`, etiquetas ni releases.

## Acceptance and closeout

Candidato congelado, guía de smoke manual con archivos DXF del autor.

## Required artifacts

Commits, verificación, fixtures con procedencia, informe; `GUIDE_IMPACT = UPDATE` (sección
nueva de referencias DXF importadas).

## Stop conditions

- La implementación exige que la referencia participe en el DAG o en la identidad.
- Una entidad admitida no se reproduce dentro de la tolerancia declarada.
- Un cambio en la exportación DXF existente o en sus pruebas.

## Deferred debts

Variantes excluidas; persistencia R-b, R-c o R-d según D-A-7; `MAINT-A-DXF1`.
