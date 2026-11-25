# Plan operativo de implementación: `R-c`, exportación DXF de referencias y A-DXF1

| Campo | Valor |
|---|---|
| Estado | **CANDIDATE — PENDING AUTHOR REVIEW.** Planificación, no autorización. `implementationAuthorized=false` para todos los incrementos de este plan; `selfApproved=false` |
| Base | `main` `a9ee803bf4be92c58ab7e120111ebacc70055487` (segunda ola publicada) |
| Escrito por | `PRE-G9B-MAINT-A-N2` |
| Contratos | [`cedg-reference-store/v1`](../../geocedg/specs/import/reference-store.md), [`cedg-reference-export/v1`](../../geocedg/specs/export/dxf-reference-export.md), [`dxf-spline-approximation/v1`](../../geocedg/specs/import/dxf-spline-approximation.md); [enmiendas](pre_g9b_maint_a_n2_candidate_amendments.md); ADR [0044](../adr/0044-document-store-of-imported-references.md) y [0045](../adr/0045-separate-dxf-export-of-imported-references.md) (propuestos) |
| Antecedente | borrador `maint-interop-implementation-plan-draft.md` de la sesión anterior (evidencia, no autoridad), revisado contra el código de la base |

## 0. Principios

1. Contrato aprobado antes del código de cada incremento (AGENTS §9); una autorización por
   incremento sobre una base exacta y con `VERIFICATION_CLASS` congelada al empezar.
2. Ningún worktree de implementación se crea antes de la aprobación del contrato
   correspondiente.
3. Capas separadas: referencia, construcción y render. Ninguna asociación por coordenadas,
   etiquetas, handles, proximidad u orden.
4. Verificación proporcional `CORE + FOCUSED`; `FINAL` no se propone para ninguno de estos
   incrementos. Antes de cualquier `FINAL` siguen pendientes las deudas del verificador
   registradas en la segunda ola (pin del manifiesto de recursos, `final.desktop`).

## 1. Secuencia de referencia (instrucción del autor §11)

| Orden | Id | Contenido | Capa | Clase propuesta | Esfuerzo |
|---|---|---|---|---|---|
| 1 | `MAINT-A-DXF0-RC-S0` | caracterización C-1 a C-5 del contrato `R-c` (rutas de apertura, 1.2.2 aislada, Classic, coste en límites, escáneres) | pruebas y sondas | `BOUNDED_PHASE` (`STATIC` + `PHASE`) | XS |
| 2 | `MAINT-A-DXF0-RC` | almacén documental: códec, identidades, preparación y confirmación, estado guardado, gestor por `id`, escáneres, Classic según RC-D4 | documento + Desktop + gancho upstream mínimo en `MyXMLioJre` | `INTEGRATED_PHASE` | M |
| 3 | `MAINT-A-DXF0-EXP` | exportación de referencias como `SPLINE`, resolutor de estados, manifiesto v4, conformidad externa | adaptador de exportación + Desktop | `INTEGRATED_PHASE` + evidencia de lectores externos | M |
| 4 | `MAINT-A-DXF1-S0` | caracterización NUM-1 a NUM-10 con las rutas de creación elegidas; ruta de redefinición por sustitución; estabilidad de bytes | pruebas y sondas | `BOUNDED_PHASE` | XS |
| 5 | `MAINT-A-DXF1` | conversión exacta, aproximación con estimación, procedencia y aplicabilidad (cierra BS-24), entradas de derivación, corrección de la redefinición por sustitución | biblioteca + servicio externo + núcleo GeoCeDG aditivo + Desktop | `INTEGRATED_PHASE` | M-L |
| 6 | `MAINT-A-DXF1b` | cota global certificada en aritmética racional exacta con presupuesto | biblioteca + servicio | `INTEGRATED_PHASE` | M |

Hito interno recomendado: **`MAINT-A-DXF0-RC-M`**, primer commit de RC con los tipos puros
compartidos (identificador de referencia, entrada de derivación, preferencia de exportación,
interfaz de instantánea del almacén) y sus pruebas unitarias, sin persistencia ni interfaz.
EXP y DXF1 lo fusionan antes de empezar, como se hizo con la biblioteca matemática
(`d22c4cc85`). Así ningún incremento redefine el modelo.

## 2. Dependencias

| Incremento | Dependencias estrictas | Dependencias convenientes |
|---|---|---|
| RC-S0 | contrato `R-c` aprobado (o, como mínimo, autorizado para caracterización) | — |
| RC | contrato `R-c` y ADR 0044 aprobados; RC-S0 revisada (C-1 es evidencia obligatoria del contrato §14) | — |
| EXP | contrato de exportación y ADR 0045 aprobados; hito RC-M (identificador, preferencias, instantánea) | RC integrado antes que EXP (orden de integración); A-DXF1 no es necesario: antes de él solo existen los estados 1, 7 y la selección del usuario |
| DXF1-S0 | contrato A-DXF1 aprobado o autorizado para caracterización | puede ejecutarse en cualquier momento tras la base |
| DXF1 | contrato A-DXF1 aprobado; DXF1-S0 revisada; RC integrado (AX-D1: identificador y entradas de derivación persistentes) | EXP integrado antes, para probar el resolutor con construcciones reales; no es estricto: DXF1 prueba con el resolutor sobre registros sintéticos |
| DXF1b | DXF1 integrado | — |
| `MAINT-A3` (fuera de este plan) | su propia autorización | **nunca en paralelo con EXP** (mismos ficheros del exportador y del manifiesto) |

Dependencias que el borrador anterior daba por estrictas y no lo son: EXP no necesita la
persistencia de RC para escribir referencias (solo el modelo RC-M); DXF1 no necesita el
exportador. Dependencia que el borrador no tenía: DXF1 sí necesita RC por AX-D1.

## 3. Paralelismo

| Par | Ficheros de producto | Acoplamiento | Evaluación |
|---|---|---|---|
| RC-S0 ∥ DXF1-S0 | solo sondas en copias separadas | ninguno | **viable** desde la aprobación de los contratos |
| RC ∥ EXP | disjuntos salvo el gestor de referencias (texto de estados) | tipos de RC-M | **viable** en worktrees separados tras RC-M; integración serial RC → EXP |
| RC ∥ DXF1 (parte de biblioteca) | disjuntos: ajuste, QR, muestreo y estimación son biblioteca pura | ninguno | **viable**: la biblioteca de DXF1 puede desarrollarse mientras RC avanza; el servicio y la interfaz esperan a RC |
| EXP ∥ DXF1 | disjuntos | EXP lee bloques que DXF1 escribe | viable con bloques sintéticos; integración EXP → DXF1 |
| EXP ∥ `MAINT-A3` | **los mismos** (`DxfExporter`, adaptadores, manifiesto) | clases de fidelidad y `schema_version` | **no**; serializar |
| DXF1 ∥ DXF1b | mismo paquete de biblioteca | gramática compartida | no; DXF1b tras DXF1 |

Restricciones operativas: nunca dos ejecuciones de la misma tarea Gradle en un mismo checkout;
cada worktree con su evidencia; verificación integrada repetida en cada fusión.

Recomendación: integración y cierre en el orden del autor (RC → EXP → DXF1 → DXF1b); desarrollo
paralelo de RC, EXP (tras RC-M) y la parte de biblioteca de DXF1; caracterizaciones RC-S0 y
DXF1-S0 en paralelo al principio.

## 4. Límites de ficheros

### 4.1 RC

Puede tocar: `org.geocedg.common.importer.dxf` (códec y validación del almacén; constructores
de registros), `MyXMLioJre` (rama de lectura y paso de escritura), `AppGeoCeDG`,
`GeoCeDGDxfReferenceSession`, `GeoCeDGDxfReferenceDialogs`, `GeoCeDGDxfReferenceText`,
`GeoCeDGDxfReferenceImport`, los escáneres de inspección y de modo angular, Classic según
RC-D4, guías y pruebas. No toca: `MyXMLHandler`, `getFullXML`, `doParseXML`, Undo, identidad
espacial, exportadores, núcleo B-spline.

### 4.2 EXP

Puede tocar: `GeoCeDGDxfExportController`, `GeometryExportService` (sobrecarga), `DxfExporter`,
`DxfEncodingResult`, `DxfFidelityManifestWriter`, `GeometryExportPreflight` (regla de
escritura), un adaptador nuevo de referencias, el resolutor, guías, pruebas y un guion de
conformidad fuera de las rutas de producto. No toca: persistencia del documento, núcleo,
biblioteca matemática, lector DXF, adaptadores de construcción.

### 4.3 DXF1

Puede tocar: clases nuevas en `org.geocedg.common.math.bspline`; un paquete nuevo de servicio
fuera del núcleo; `org.geocedg.common.kernel.bspline` de forma aditiva (fábrica de
`PersistedContract`, clase de procedencia, aplicabilidad, conservación del bloque en la
redefinición por sustitución); acción, diálogo e informe de Desktop; escritura de entradas de
derivación por la API del almacén. No toca: exportador, `MyXMLHandler`, plantilla XML, rutas
upstream.

### 4.4 Puntos calientes compartidos

Registro de acciones del perfil y literales de recuento; pines de encabezados de guías
(`PreG9BR6PlusGProductMetadataTest`); inventario JUnit (`discovery.shared` y selecciones con
clases tocadas), pin del registro y recuentos de `verification-final-coverage.Tests.ps1`;
guías (`GUIDE_IMPACT = UPDATE`) y textos localizados. Cada fusión regenera estos artefactos
con el actualizador canónico a partir de evidencia ejecutada. Lección de la segunda ola: incluir
en la selección `PHASE` las clases de recuento de guías y de etiquetas de diagnósticos cuando el
incremento toca guías o diagnósticos.

## 5. Verificación por incremento

| Incremento | Nivel | Notas |
|---|---|---|
| RC-S0, DXF1-S0 | `STATIC` + `PHASE` registrada propia | evidencia de caracterización; sin cambio de producto |
| RC, EXP, DXF1, DXF1b | `STATIC` + `PHASE PRE-G9B-MAINT-<id>` + `INTEGRATION` (`CORE + FOCUSED`) | el perfil `DEV` no es evidencia; `git diff --check`; `GUIDE_IMPACT`; impacto de bootstrap (sin cambio esperado; los lectores externos de EXP no son requisito de estación) |
| EXP | además: evidencia de conformidad externa (RX-16) | AutoCAD Core Console y un segundo lector que designe el autor |
| todos | `FINAL` no se propone | `VERIFICATION_ESCALATION_REQUEST` solo si RC necesitara tocar `MyXMLHandler` o Undo, lo que es condición de parada del contrato |

## 6. Smokes autorales

| Incremento | Guion |
|---|---|
| RC | importar polinómicas y racionales; ocultar y renombrar; guardar, cerrar, **mover el DXF**, reabrir y ver referencias y detalles; quitar una; abrir en Classic; editar `contract` a `v2` y comprobar mensaje y documento previo; abrir en 1.2.2 y comprobar la pérdida declarada |
| EXP | documento con una referencia racional sin construcción, una convertida y una con aproximación fallida; exportación ordinaria y con la casilla; abrir el DXF en AutoCAD |
| DXF1 | el guion del contrato A-DXF1 §17 |

## 7. Riesgos

| Riesgo | Mitigación |
|---|---|
| una ruta de apertura vacía el almacén antes de la confirmación | C-1; preparación y confirmación; RC-13 |
| pérdida declarada al guardar con 1.2.2 | notas de versión; decisión del autor D-A-N2-2 |
| escáneres que rechazan documentos con almacén grande | registro de la entrada y tope de 4 MiB (RC-D9) |
| lectores externos no conformes con `SPLINE` racional | RX-16 antes del cierre de EXP; condición de parada |
| redefinición por sustitución pierde la procedencia | corrección dentro de DXF1 (AX-D13) con NUM-6 |
| `-0.0` cambia el resumen tras guardar | normalización previa (AX-D12), NUM-8 |
| ruta de listas construidas en código no estable en bytes | no usarla; DXF1-S0 |
| coste de la cota exacta | DXF1b con presupuesto y última cota válida |
| churn de inventarios al fusionar en paralelo | integración serial y regeneración canónica |
| colisión con `MAINT-A3` | serializar EXP y A3; coordinar `schema_version` |

## 8. Decisiones de planificación para el autor

| Id | Pregunta | Recomendación | Consecuencia |
|---|---|---|---|
| D-P-1 | Id del incremento de exportación | `MAINT-A-DXF0-EXP` | agrupa la línea de importación |
| D-P-2 | Hito de modelo compartido RC-M | sí, primer commit de RC | evita tres definiciones del modelo |
| D-P-3 | Paralelismo | desarrollo paralelo de RC, EXP (tras RC-M) y biblioteca de DXF1; integración serial RC → EXP → DXF1 → DXF1b | calendario más corto a cambio de regenerar inventarios en cada fusión |
| D-P-4 | Caracterizaciones RC-S0 y DXF1-S0 como incrementos acotados propios | sí, en paralelo al principio | decisiones basadas en evidencia antes del código |
| D-P-5 | Orden relativo de EXP y `MAINT-A3` | EXP antes; A3 después, nunca en paralelo | A3 añadirá la `SPLINE` de construcciones que EXP no toca |
| D-P-6 | DXF1 sobre `R-c` | estricto (AX-D1) | DXF1 no empieza su servicio antes de integrar RC |
