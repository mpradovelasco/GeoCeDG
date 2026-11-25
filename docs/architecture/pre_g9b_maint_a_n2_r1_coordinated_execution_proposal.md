# Propuesta de ejecución coordinada de las implementaciones `R-c`, exportación y A-DXF1

| Campo | Valor |
|---|---|
| Estado | **PROPUESTA — NO AUTORIZADA.** Preparada por `PRE-G9B-MAINT-A-N2-R1`. `selfApproved=false`, `implementationAuthorized=false` |
| Requisito | adopción autoral expresa de los tres contratos de A-N2-R1 y una instrucción de implementación con base exacta |
| Prompts candidatos | [`pre_g9b_maint_a_n2_r1_candidate_prompts/`](pre_g9b_maint_a_n2_r1_candidate_prompts/) |
| Plan de referencia | [plan operativo](pre_g9b_maint_interop_implementation_plan_candidate.md) |

## 1. Alcance de la autorización conjunta que se propone

Una sola instrucción autoral podría autorizar, sobre un `main` exacto que contenga los
contratos adoptados, los incrementos `MAINT-A-DXF0-RC-S0`, `MAINT-A-DXF0-RC`,
`MAINT-A-DXF0-EXP`, `MAINT-A-DXF1-S0` y `MAINT-A-DXF1`, coordinados por una sesión principal
con un informe final único (instrucción R1 §15). `MAINT-A-DXF1b` queda fuera: requiere su
propia caracterización de coste y una autorización posterior. `MAINT-A3` también queda fuera.

## 2. Worktrees y ramas (se crean solo tras la autorización)

| Incremento | Rama | Worktree | Base |
|---|---|---|---|
| RC-S0 | `phase/pre-g9b-maint-a-dxf0-rc-s0` | `GeoCeDG-wt-maint-a-dxf0-rc-s0` | `main` autorizado |
| DXF1-S0 | `phase/pre-g9b-maint-a-dxf1-s0` | `GeoCeDG-wt-maint-a-dxf1-s0` | `main` autorizado |
| RC (con RC-M como primer commit) | `phase/pre-g9b-maint-a-dxf0-rc` | `GeoCeDG-wt-maint-a-dxf0-rc` | `main` autorizado |
| EXP | `phase/pre-g9b-maint-a-dxf0-exp` | `GeoCeDG-wt-maint-a-dxf0-exp` | `main` autorizado + fusión de RC-M y DXF1-M |
| DXF1 | `phase/pre-g9b-maint-a-dxf1` | `GeoCeDG-wt-maint-a-dxf1` | `main` autorizado + RC-M; DXF1-M como primer commit; servicio tras integrar RC |
| Integración | `integration/pre-g9b-maint-interop` | `GeoCeDG-wt-maint-interop-integration` | `main` autorizado |

## 3. Secuencia y puntos de sincronización

| Fase | Trabajo | Sincronización |
|---|---|---|
| S1 | RC-S0 ∥ DXF1-S0 (solo pruebas de caracterización) | **P1**: revisión de C-1 a C-5 y NUM-1 a NUM-10 por el coordinador; un hallazgo contrario al contrato detiene y se eleva al autor |
| S2 | RC-M en la rama RC; DXF1-M en la rama DXF1 | **P2**: RC-M y DXF1-M congelados; EXP fusiona ambos y DXF1 fusiona RC-M (merge, nunca copia) |
| S3 | RC ∥ EXP ∥ biblioteca de DXF1 | sin fusiones cruzadas salvo RC-M y DXF1-M |
| S4 | integración de RC (`STATIC` + `PHASE` + `INTEGRATION`) | **P3**: RC integrado; DXF1 integra su servicio sobre él |
| S5 | integración de EXP, conformidad AutoCAD/ezdxf | **P4**: evidencia RX-16/RX-17 revisada |
| S6 | DXF1 completo e integración | **P5**: candidato integrado con verificación `CORE + FOCUSED` |
| S7 | informe consolidado único, guías de smoke | **P6**: parada para smoke y revisión autoral |

Cada fusión regenera inventarios JUnit y pines con el actualizador canónico a partir de
evidencia ejecutada; nunca dos ejecuciones de la misma tarea Gradle en un checkout.

## 4. Paradas del coordinador

Solo ante: cambio normativo necesario; arquitectura no autorizada; regresión semántica; fallo de
integridad o persistencia; incumplimiento de una garantía matemática; verificación bloqueante;
necesidad de ampliar el alcance (instrucción R1 §15). Fuera de esos casos, las tareas ordinarias
cubiertas por la autorización no requieren nuevas consultas.

## 5. Lo que nunca hace el coordinador

Autoaprobar resultados; registrar `AUTHOR APPROVED`; promover a `main`; push, etiquetas,
releases o empaquetado; aplicar enmiendas a especificaciones aprobadas sin el registro de
adopción del autor.

## 6. Entregables del encargo futuro

Un informe consolidado con los SHA y árboles de cada candidato y del integrado, las
verificaciones, la evidencia de conformidad externa con versiones, los resultados de las
caracterizaciones, las guías de smoke de RC, EXP y DXF1, las decisiones que hayan surgido y el
estado de cada incremento.
