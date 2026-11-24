# PRE-G9B-MAINT, primera ola — registro de integración de N1, A-N1 y U2-R1

| Campo | Valor |
|---|---|
| Carácter | Registro de la integración técnica autorizada. `selfApproved=false` |
| Fecha | 24 de noviembre de 2026 |
| Instrucción | «GeoCeDG — PRE-G9B-MAINT. Cierre autoral coordinado de N1, A-N1 y U2-R1, integración y promoción a `main`», entregada por el autor en la conversación |
| Ruta | `CHANGE_ROUTE = ORDINARY` |
| Verificación de la integración | `INTEGRATION_VERIFICATION = STATIC + PHASE PRE-G9B-MAINT-U2 + INTEGRATION` (`CORE + FOCUSED`, no exhaustiva) |
| `FINAL` | `NOT AUTHORIZED` |
| Base común | `e79ace7d1473f13d32d0b660e92a4d1d45ce39bf`, árbol `9c43dfa5548a0844215c0ab9ba808f20e3dd3a84` (`main` publicado) |
| Rama y worktree | `integration/pre-g9b-maint-first-wave`, worktree `GeoCeDG-wt-maint-integration` |

## 1. Candidatos aprobados

| Incremento | Commit | Árbol | Registro de aprobación |
|---|---|---|---|
| `PRE-G9B-MAINT-N1` (rev. 2) | `67663ceeefe9613dc229ba25da43af8e71557b1b` | `6920d582594a5620918b55f5ed37f131fccc7983` | [N1](pre_g9b_maint_n1_author_approval_record.md) |
| `PRE-G9B-MAINT-A-N1` R2 | `8747c731eccc3644cd1da8b74eb3453a68511813` | `89df425e50a1fc3932ac7b59a0b80af5a48072b9` | [A-N1](pre_g9b_maint_a_n1_author_approval_record.md) |
| `PRE-G9B-MAINT-U2-R1` | `f99a99785ffd5c733a98262888a48a8fad31ec0e` | `dea0f32aebd2dd7915c1a8d044818c77eb39269b` | [U2-R1](pre_g9b_maint_u2_r1_author_approval_record.md) |

## 2. Integración

Por `git merge --no-ff`, sin squash, rebase, cherry-pick ni reescritura, en este orden:

| Merge | Commit | Árbol | Delta incorporado |
|---|---|---|---|
| N1 | `72c23caeba6121b9b002ebb857ca2c205e3b8cea` | `6920d582594a5620918b55f5ed37f131fccc7983` | 3 archivos documentales |
| A-N1 R2 | `5e6c3483b7fd244a24acd18cdb50427720cadc30` | `71c15cb55e62ae02a35dac497dca653f64d09a49` | 6 archivos documentales |
| U2-R1 | `c558af8ede6bbc3f2cb17325cd59ccab67820894` | `5d88a04cfab6d3e9d88fd7420ec77f6e878f92c3` | 30 archivos (U2 y U2-R1) |

Ningún merge tuvo conflictos: los tres deltas respecto de la base son disjuntos por rutas. El
árbol resultante contiene exactamente la unión de los tres deltas (39 rutas), con los blobs de
cada candidato sin cambios, y los tres commits son ancestros del resultado. Las ramas de origen
no se modificaron.

N1 y A-N1 no contienen código. Todos los cambios bajo `source/` proceden de U2-R1 (Desktop,
propiedades de menú y tres iconos SVG); no cambian el núcleo, SplineV2, la persistencia ni el
DAG. La selección `PHASE PRE-G9B-MAINT-U2`, el inventario JUnit, el pin del registro y los
recuentos de `verification-final-coverage.Tests.ps1` son los de U2-R1, sin cambios, porque las
otras dos ramas no los tocan.

## 3. Conciliación documental

El commit que contiene este registro añade solo los tres registros de aprobación, este registro,
las líneas de estado de `locus-v2-metrics.md`, `bspline-2d.md`, `dxf-spline-reference.md` y el
ADR 0043, y la entrada aditiva del roadmap. Los informes de candidato, matrices y guías
congelados no se modifican.

## 4. Verificación y publicación

La verificación integrada se ejecuta sobre el candidato que contiene este registro, con
directorios de registro nuevos. Sus identificadores y resultados los informa el artefacto
siguiente, porque un commit no puede citar sus propias ejecuciones. Las verificaciones de los
candidatos aislados no se atribuyen al árbol integrado.

La promoción a `main` exige, según `AGENTS.md` §8.1 y el prompt canónico de verificación, una
autorización autoral expresa que nombre el SHA exacto del candidato. Este registro no la
contiene ni la origina. No se crean etiquetas, releases ni paquetes.
