# PRE-G9B-CU3 — Estrategia mínima de implementación, verificación y riesgos

| Campo | Valor |
|---|---|
| Carácter | Estrategia candidata (entregables 7 y 8 de la instrucción). **No autoriza ninguna implementación**: la siguiente autorización es la aprobación del diseño y, en su caso, de la implementación, sobre una base exacta |
| Estado | `CU3 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false` |
| Normas candidatas | [propuesta normativa](pre_g9b_cu3_normative_proposal.md); [ADR 0039 (PROPOSED)](../adr/0039-locus-v2-structural-points-and-certified-contact-evidence.md) |
| Validación | [matriz](../validation/pre_g9b_cu3_validation_matrix.md) |
| Decisiones | [hoja de decisiones](../validation/pre_g9b_cu3_author_decision_sheet.md) |

## 1. Alcance mínimo propuesto para la implementación

Con las opciones recomendadas de la hoja de decisiones, el alcance es este.

| Pieza | Contenido | Necesaria para |
|---|---|---|
| I-1 | envolventes de `h`, `h′` y `h″`: composición de la envolvente de segundo orden de clase v3 con los adaptadores de residuo de recta, segmento, rayo y circunferencia | Etapa B; aceptación certificada de H-2 |
| I-2 | aceptación certificada de las raíces transversales, deduplicación por corchetes, escala característica S1 y emisión de los diagnósticos de rechazo, con la política versionada nueva y el texto de los adaptadores intacto | H02; H-CU3-1, H-CU3-3 y H-CU3-4 |
| I-3 | taxonomía de evidencia de contacto, orden de contacto (con el pliegue) y germen tangente con su ciclo de vida | Etapa B; H-CU3-2 |
| I-4 | evaluación exacta (M3) de los programas de clase v3 de regla y compás en un punto exacto del conductor, con presupuesto versionado | segunda incidencia de los casos obligatorios |
| I-5 | `Point(L, D)` e `Intersect(L, r, D)`: los cuatro hechos, la tabla de procedencia, la correspondencia y los diagnósticos | Etapa A; los tres casos obligatorios y la costura |
| I-6 | los ejes como referencias reservadas en el registro de identidad y en el adaptador de recta | H06 |
| I-7 (opción B de D-CU3-5) | objeto de punto crítico certificado | usabilidad de los contactos de clase 3 |

Fuera del mínimo, diferibles según el plan §6:

- la Etapa B para los objetivos de cónica, gráfica e implícita, y para los pares;
- I-7, si el autor elige la opción A.

## 2. Pasos, dependencias y condiciones de parada

```text
T0  normas aprobadas incorporadas (A3.0–A3.5), ADR 0039 aceptado, versión de política
T1  pruebas de caracterización y pins nuevos (filas «falla en la base») ────────────┐
T2  I-1 envolventes de h, h', h''  ──► T3  I-2 aceptación certificada (H-2) ──┐     │
T4  I-3 taxonomía, orden y germen  ◄───────────────────────────────────────────┘     │
T5  I-4 evaluación exacta (M3) ─► banco GC3-1 ─► constantes del presupuesto           │
T6  I-5 Etapa A (cuatro hechos)  ◄── T5                                               │
T7  I-6 ejes (identidad reservada)                                                     │
T8  I-7 punto crítico (si B)                                                           │
T9  compatibilidad: tokens persistidos, nota de migración (D-CU3-7, D-CU3-12)          │
T10 inversión de pins B08_B09, H02 y H06; preserve; selecciones de CU0–CU2  ◄──────────┘
T11 congelación, inventario JUnit, STATIC, INFRA_UNIT, PHASE CU3/CU2/CU1/CU0, INTEGRATION
```

Condiciones de parada:

1. Una pieza exigiría cambiar un contrato aprobado de `CU0`–`CU2` (componentes, certificados,
   métrica, presentación o exportación).
2. Habría que cambiar el texto de un adaptador de residuo o un formato de token existente.
3. Habría que materializar una clase 3 como intersección para cumplir un caso obligatorio.
4. M3 no decide los casos obligatorios dentro de un presupuesto razonable (banco GC3-1). En ese
   caso se presentan los resultados y las alternativas antes de seguir.
5. Una tolerancia nueva dependería de la vista, de la posición absoluta o de una constante sin
   justificar con un banco.
6. Un cambio upstream sería necesario fuera de lo que el ADR 0039 justifique, o no se podría
   registrar en `docs/upstream/modified-files.yml`.
7. Se descubriría un defecto preexistente fuera de H-CU3-1 a H-CU3-7. Se registra y se presenta,
   sin corregirlo sin autorización.

## 3. Superficies afectadas

- **Núcleo compartido** (`org.geocedg.common.kernel.locus.intersection`, `…locus.exact` y
  `…kernel.algos`): las piezas nuevas.
- **Superficies existentes que se tocan:**
  - `LocusIntersectionPolicy2D`;
  - las capacidades adaptativa y polinómica;
  - `PublicTargetIntersectionCapability2D`;
  - `PublicIntersectionRootIdentityResolver2D`;
  - `LocusIntersectionTokenLedger2D` (un formato de estado nuevo; los antiguos se siguen
    leyendo).
- **Registro de identidad espacial**, solo con la opción A de D-CU3-8.
- **Upstream mínimo:** los procesadores `CmdPoint` y `CmdIntersect`, que reciben las formas
  nuevas (como en `CU2`, las sobrecargas V2 viven en los procesadores existentes), y la
  localización de la sintaxis. Cada archivo se registra en `docs/upstream/modified-files.yml`.
- **Escritorio:** el inspector y los mensajes. Ninguna lógica geométrica.

### Pruebas existentes afectadas, por categorías

- las de `CU0` que fijan B08/B09, H02 y H06 (se invierten en T10);
- las de G8 y G9U0 que fijan la clasificación de contacto o las raíces de casi-tangencias (se
  revisan: una que espere una raíz espuria es un pin de un defecto);
- las de la fase `PRE-G9B-CU2` que materializan raíces transversales en loci con pliegues.

El inventario exacto se completa en T1.

## 4. Plan de verificación

| Momento | Perfil | Evidencia |
|---|---|---|
| durante T1–T10 | `DEV` y pruebas focalizadas | consola en el directorio temporal; ninguna ejecución de aceptación como efecto secundario |
| candidato congelado | `STATIC`, `INFRA_UNIT`, `PHASE -Phase PRE-G9B-CU3`, `PRE-G9B-CU2`, `PRE-G9B-CU1`, `PRE-G9B-CU0` e `INTEGRATION`, cada uno una vez sobre el mismo commit y el mismo árbol | ejecuciones re-autenticadas (`Read-VerificationJson`, `Get-VerificationResultHash`) |
| bancos | GC3-1 a GC3-4 antes y después, con los contadores deterministas | `artifacts/benchmarks/pre-g9b-cu/` |
| comprobación del autor | los tres casos y la costura en la aplicación real, la dinámica y la persistencia | la comprobación manual del autor; nunca se simula |

`FINAL` no lo exige la clase y queda para `CU10`.

Registrar la fase exige añadir `PRE-G9B-CU3` al registro y las selecciones al inventario. Es
infraestructura de verificación y se hace dentro de la implementación autorizada, como en `CU1` y
`CU2`.

## 5. Riesgos y degeneraciones

| Riesgo | Efecto | Mitigación |
|---|---|---|
| coste de M3 | la torre de extensiones crece con la profundidad del corte | presupuesto versionado, banco GC3-1 y fallo cerrado con `EXACT_EVALUATION_BUDGET_EXHAUSTED`; la clase 3 sigue siendo visible |
| ámbito de M3 | las constantes trascendentes (ángulos introducidos como funciones del conductor) quedan fuera de la clase | `EXACT_EVALUATION_OUT_OF_CLASS`; las constantes independientes del conductor ya son racionales capturados |
| documentos antiguos con raíces que cambian | tokens de raíces mal clasificadas o espurias dejan de resolverse | D-CU3-7, nota de migración y V-COMPAT-1/2 |
| menos puntos materializables con la opción (i) de D-CU3-6 | fuentes sin envolventes | diagnóstico visible y ampliación de la clase v3 en un paquete posterior |
| pliegues sin simetría exacta | `F′ = 0` sin certificado de pliegue | orden no establecido (Etapa B §4.4); nunca se supone una tangencia |
| contactos de orden mayor que dos y cúspides | `h″` no certificado lejos del cero | clase 5 con su diagnóstico |
| raíces cerca de la costura | lados del levantamiento periódico | la canonicalización del proveedor; `L-CU2-CLOSED-PERIODIC-SEAM` se conserva |
| condición de no degeneración en el límite (`|HD_1| = |HM|`) | `M_1` es un extremo de `n` | la pertenencia al objetivo limitado; caso de frontera en la matriz (V-A-14) |
| permutación de las salidas heredadas | `D` cambia de punto geométrico | se sigue al objeto, sin reasociar (V-A-15) |
| el objeto de punto crítico se confunde con una intersección | uso incorrecto por el usuario | familia, nombre y ficha propios, y ninguna conversión automática |

## 6. Fuera de alcance

- `CU4`, `CU5`, `CU6`, `CU7`, `CU8-D`, `CU9`, `CU10` y `G9B`.
- El perfil de esfuerzo (H-1) y cualquier tolerancia configurable.
- La materialización de las componentes puntuales y C22, C23 y C40, salvo que lo decida D-CU3-9.
- El trabajo futuro registrado en el cierre de `CU2` §6.
- Los pares de loci tangentes; los contactos de orden superior; la eliminación algebraica general.
- Cualquier cambio en `geocedg_book`.
