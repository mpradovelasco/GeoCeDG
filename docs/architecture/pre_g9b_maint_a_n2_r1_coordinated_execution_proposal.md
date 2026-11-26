# Propuesta de ejecución coordinada de las implementaciones `R-c`, exportación y A-DXF1

| Campo | Valor |
|---|---|
| Estado | **PROPUESTA DE COORDINACIÓN ACTUALIZADA TRAS EL CIERRE NORMATIVO (2026-11-26) — EJECUCIÓN NO AUTORIZADA.** Preparada por `PRE-G9B-MAINT-A-N2-R1` y actualizada por el cierre de `PRE-G9B-MAINT-A-N2`. `selfApproved=false`, `implementationAuthorized=false` |
| Requisito | cumplido: adopción autoral de los tres contratos (2026-11-26, [registro de aprobación](../validation/pre_g9b_maint_a_n2_author_approval_record.md)); pendiente: una instrucción de implementación con base exacta, la publicación del cierre en `main` incluida |
| Prompts candidatos | [`pre_g9b_maint_a_n2_r1_candidate_prompts/`](pre_g9b_maint_a_n2_r1_candidate_prompts/) |
| Plan de referencia | [plan operativo](pre_g9b_maint_interop_implementation_plan_candidate.md) |

## 1. Alcance de la autorización conjunta que se propone

Una sola instrucción autoral podría autorizar, sobre un `main` exacto que contenga los
contratos adoptados, los incrementos `MAINT-A-DXF0-RC-S0`, `MAINT-A-DXF0-RC`,
`MAINT-A-DXF0-EXP`, `MAINT-A-DXF1-S0` y `MAINT-A-DXF1`, coordinados por una sesión principal
con un informe final único (instrucción R1 §15). La autorización podrá dar al coordinador
autonomía para crear ramas y worktrees, coordinar subagentes y ejecutar las verificaciones
previstas, dentro de las paradas de §4.

Quedan expresamente fuera:

- `MAINT-A-DXF1b`, que requiere su propia caracterización de coste y una autorización
  posterior;
- `MAINT-A3`;
- la NURBS nativa (`MAINT-A-R`);
- los mantenimientos independientes del importador y del exportador DXF existentes, entre
  ellos las deudas DT-1 (exponentes de la exportación ordinaria) y DT-2 (`70 = 15`).

## 1 bis. Dependencias y puertas de cada incremento (cierre normativo de 2026-11-26)

| Incremento | Depende de | Puertas de cierre propias |
|---|---|---|
| `MAINT-A-DXF0-RC-S0` | `cedg-reference-store/v1` y ADR 0044, aprobados | C-1 a C-3 y el comportamiento actual de C-5; ningún cambio de producto |
| `MAINT-A-DXF1-S0` | `dxf-spline-approximation/v1`, aprobado | NUM-1 a NUM-10; ningún cambio de producto |
| `MAINT-A-DXF0-RC` | RC-S0 revisada (P1) | hito RC-M como primer commit; RC-1 a RC-26 (RC-25 (f) incluido); la presencia aditiva de 210, 220 y 230 con las seis condiciones de D-R4-1; no regresión DX-1 a DX-13; **C-4 y C-5 en el presupuesto** |
| `MAINT-A-DXF0-EXP` | `cedg-reference-export/v1` y ADR 0045, aprobados; RC-M y DXF1-M fusionados | RX-1 a RX-6, RX-8 a RX-17 y RX-19, con RX-15 (e) por el controlador real; ruta solo de referencias únicamente en el flujo extendido y flujo G5 intacto; identidad de bytes de la exportación ordinaria; evidencia externa RX-16, RX-17 y RX-19 (AutoCAD 2026 `ACADVER` 25.1, ezdxf 1.4.2) |
| `MAINT-A-DXF1` | DXF1-S0 revisada; RC integrado; RC-M fusionado; DXF1-M como primer commit | AP, NUM, RX-7 y RX-18; **constantes `bspline-param-lsq-constants/v1` fijadas**; autocomprobación que relee el bloque |

Hitos compartidos: **RC-M**, los tipos puros del modelo del almacén, y **DXF1-M**, el
analizador del bloque de procedencia. Ambos se fusionan y nunca se copian. La integración
sigue el orden RC → EXP → DXF1.

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
| S1 | RC-S0 ∥ DXF1-S0 (solo pruebas de caracterización) | **P1**: revisión de C-1 a C-3, la parte actual de C-5 y NUM-1 a NUM-10 (C-4 y C-5 en el presupuesto son puertas de cierre de RC, R3) por el coordinador; un hallazgo contrario al contrato detiene y se eleva al autor |
| S2 | RC-M en la rama RC; DXF1-M en la rama DXF1 | **P2**: RC-M y DXF1-M congelados; EXP fusiona ambos y DXF1 fusiona RC-M (merge, nunca copia) |
| S3 | RC ∥ EXP ∥ biblioteca de DXF1 | sin fusiones cruzadas salvo RC-M y DXF1-M |
| S4 | integración de RC (`STATIC` + `PHASE` + `INTEGRATION`) | **P3**: RC integrado; DXF1 integra su servicio sobre él |
| S5 | integración de EXP, conformidad AutoCAD/ezdxf | **P4**: evidencia RX-16, RX-17 y RX-19 revisada con los criterios del contrato de exportación §12.3 (ezdxf exacto; AutoCAD 2026, `ACADVER` 25.1, dentro de 4 ulp por valor, estructura y geometría conservadas; máximos y límites registrados) y la regla `EXPONENT_NOT_PORTABLE`; un fixture que no cumpla detiene y se eleva al autor |
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
