# PRE-G9B-CU3 — Estrategia mínima de implementación, puertas, verificación y riesgos

| Campo | Valor |
|---|---|
| Carácter | Estrategia candidata (entregables 7 y 8 de la instrucción de preparación). **No autoriza ninguna implementación**: la siguiente autorización es la aprobación del diseño y, en su caso, de la implementación, sobre una base exacta |
| Estado | Estrategia de la preparación, revisada por la revisión científica focal y por la comprobación final de coherencia (2026-10-24). **Aprobada por el autor el 2026-10-24**, que autoriza su ejecución T0–T13 en dos incrementos con las puertas G-M3 (con la autoridad del objetivo, G-M3-4), G-A, G-B y G-final ([registro de aprobación](../validation/pre_g9b_cu3_author_design_approval_record.md), única autoridad de ese estado; [prompt canónico](../../.github/prompts/tasks/pre-g9b-cu3-structural-points-and-certified-contact-evidence.prompt.md)); `selfApproved=false`. La implementación se detuvo en T2 (G-M3-4). Desde el 2026-10-25, con la autoridad constructiva aprobada, `CU3` se reanuda desde T2 sobre `3cd0f710` en los incrementos E0 a E4 de la [propuesta normativa](pre_g9b_cu3_constructive_authority_normative_proposal.md) §6, que contienen las piezas I-1 a I-7 de esta estrategia, con las puertas G-AUT, G-A, G-B y G-final ([registro de aprobación de la autoridad constructiva](../validation/pre_g9b_cu3_constructive_authority_approval_record.md)) |
| Normas candidatas | [propuesta normativa](pre_g9b_cu3_normative_proposal.md); [ADR 0039 (PROPOSED)](../adr/0039-locus-v2-structural-points-and-certified-contact-evidence.md) |
| Validación | [matriz](../validation/pre_g9b_cu3_validation_matrix.md) |
| Decisiones | [hoja de decisiones](../validation/pre_g9b_cu3_author_decision_sheet.md), con la recomendación consolidada (§7) |

## 1. Alcance propuesto: dos incrementos en una sola autorización

La revisión focal pide una elección de alcance mínimo con sus puertas, sin otra sucesión de
autorizaciones parciales. La propuesta es **una autorización** con dos incrementos. El segundo
empieza solo cuando el primero supera sus puertas, y no necesita una autorización nueva salvo
que una puerta falle.

| Pieza | Contenido | Incremento |
|---|---|---|
| I-1 | envolventes de `h`, `h′` y `h″` (composición de la envolvente de segundo orden de clase v3 con los adaptadores de recta, segmento, rayo y circunferencia); punto crítico certificado | `CU3-A` |
| I-2 | grados de evidencia; vía certificada aditiva (H02); regla de conflicto; emisión de los diagnósticos de rechazo; política de consumo; política versionada nueva, con el texto de los adaptadores intacto | `CU3-A` |
| I-3 | taxonomía de contacto (clases 1 a 5), orden de contacto (con el pliegue) y certificados de conteo de la regla de conflicto en el resultado rico, **sin** materialización sin conductor. Incluye la clase 2 como evidencia de clasificación cuando M3 está disponible, porque los contactos estructurales del modelo solo se corrigen con `N = 1` doble (Etapa B §9.3) | `CU3-A` |
| I-4 | evaluación exacta M3 en la configuración de decisión `Σ_L⁺`, que contiene la configuración autoritativa: `Σ_L` sin cambios, más `D`, `r` y sus ancestros con las reglas de una sola semántica; reconstrucción exacta de `D*` con la selección heredada; decisión exacta, con chorros de primer orden; presupuesto; `Σ_B` solo como diagnóstico opcional | `CU3-A` (tras la puerta G-M3) |
| I-5 | `Point(L, D)` e `Intersect(L, r, D)`: los cuatro hechos, la tabla de procedencia, la correspondencia exacta y encerrada, el fallo cerrado y los diagnósticos | `CU3-A` |
| I-6 | los ejes como referencias reservadas en el registro de identidad y en el adaptador de recta | `CU3-A` |
| I-7 | materialización **sin conductor** de las clases 1 y 2 (pliegue certificado de `CU2` en `t₀ ∈ {0, π}` más M3), germen tangente, ciclo de vida y parejas de preimágenes en los pliegues | `CU3-B` |

**Qué cierra `CU3-A`:**

- los tres casos obligatorios y la costura, con `Intersect(L, r, K)` e `Intersect(L, r, J)`;
- H02, H06 y H-CU3-1 a H-CU3-4;
- la clasificación honesta de los contactos sin conductor.

**Qué añade `CU3-B`:** la materialización de esos contactos con `Intersect(L, r)` sin conductor,
cuando son de clase 1 o 2.

Fuera de los dos incrementos, diferibles según el plan §6 o futuros:

- la Etapa B para cónicas, gráficas, implícitas y pares;
- el objeto público de punto crítico (opción B de D-CU3-5);
- la materialización en componentes puntuales (C22, C23) y C40;
- la normalización de los pares.

## 2. Pasos, dependencias y puertas

```text
T0   normas aprobadas incorporadas (A3.0–A3.5), ADR 0039 aceptado, política versionada
T1   pruebas de caracterización y pins nuevos (filas «falla en la base»); inventario de pruebas
T2   PUERTA G-M3 (temprana y bloqueante): prototipo de M3 en Σ_L⁺ sobre los cinco contactos, los
     controles y el caso adversario V-CFG-1 (demostrado solo en Σ_B); hipótesis aritméticas del
     análisis §5.8; banco GC3-1; correspondencia con el algoritmo vivo ── si falla: PARADA y
     alternativas R-1 a R-3 y C3 al autor (nunca un residuo tolerado ni una demostración en Σ_B)
T3   I-1 envolventes ──► T4 I-2 grados, vía certificada y regla de conflicto ──► T5 I-3 taxonomía
T6   I-4 M3 de producto (tras G-M3) ──► T7 I-5 Etapa A
T8   I-6 ejes
T9   compatibilidad: tokens persistidos, nota de migración (D-CU3-7, D-CU3-12)
T10  PUERTA G-A: matriz de CU3-A en verde; pins B08_B09 (reformulado), H02 y H06 invertidos;
     preserve; selecciones de CU0–CU2; suites G8/G9U0 intactas fuera de las filas de conflicto
T11  I-7 (CU3-B): germen, ciclo de vida, materialización sin conductor ──► PUERTA G-B
T12  congelación, inventario JUnit, STATIC, INFRA_UNIT, PHASE CU3/CU2/CU1/CU0, INTEGRATION
T13  comprobación manual del autor
```

| Puerta | Momento | Bloquea | Criterio |
|---|---|---|---|
| G-M3 | T2 | la segunda incidencia de la Etapa A (I-4 de producto e `Intersect` de I-5) y todo `CU3-B` | G-M3-1 a G-M3-4 de la matriz §2.1 (G-M3-4: autoridad del objetivo, precisión autoral de 2026-10-24); el presupuesto propuesto se presenta al autor con el resultado de la puerta para una única decisión |
| G-A | T10 | el inicio de `CU3-B` | todas las filas obligatorias de `CU3-A` de la matriz, más las regresiones de §3.9 |
| G-B | T11 | la congelación con `CU3-B` | V-B-7 a V-B-10 y V-B-12, y V-B-1 en su forma de clase 2; sin regresión de G-A |
| G-final | T12 | el paquete de revisión | `INTEGRATED_PHASE` completo sobre el candidato congelado |

Si G-B no se supera, el candidato se congela con `CU3-A` solo, y `CU3-B` se presenta como
pendiente sin rebajar ninguna evidencia.

**Condiciones de parada:**

1. Una pieza exigiría cambiar un contrato aprobado de `CU0`–`CU2`, incluida la regla de
   componentes puntuales de §15.4.
2. Habría que cambiar el texto de un adaptador de residuo o un formato de token existente.
3. Habría que materializar una clase 3 como intersección, o aceptar un residuo, una proximidad o
   el prover simbólico como prueba de una igualdad.
4. La puerta G-M3 no se supera, incluida la autoridad del objetivo (G-M3-4), o su presupuesto
   necesita la aprobación autoral.
5. La regla de conflicto restringiría resultados sin una contradicción certificada, o la
   admisibilidad de G8B-R1 cambiaría fuera de las filas identificadas.
6. Una tolerancia nueva dependería de la vista, de la posición absoluta o de una constante sin
   justificar con un banco.
7. Un cambio upstream sería necesario fuera de lo que el ADR 0039 justifique, o no se podría
   registrar en `docs/upstream/modified-files.yml`.
8. Se descubriría un defecto preexistente fuera de H-CU3-1 a H-CU3-7. Se registra y se presenta,
   sin corregirlo sin autorización.

## 3. Superficies afectadas

- **Núcleo compartido** (`org.geocedg.common.kernel.locus.intersection`, `…locus.exact` y
  `…kernel.algos`): las piezas nuevas, incluida la construcción de `Σ_L⁺` para M3. Reutiliza sin
  cambios el programa capturado de clase v3 de `L`, sus operaciones y sus accesores de selección,
  y lo amplía con `D`, `r` y sus ancestros según las reglas de la Etapa A §6.2.
- **Superficies existentes que se tocan:**
  - `LocusIntersectionPolicy2D`;
  - las capacidades adaptativa y polinómica (grados, disparador y regla de conflicto);
  - `PublicTargetIntersectionCapability2D`;
  - `PublicIntersectionRootIdentityResolver2D`;
  - `LocusIntersectionTokenLedger2D`, con un formato de estado nuevo solo en `CU3-B`; los antiguos
    se siguen leyendo.
- **Registro de identidad espacial**, solo con la opción A de D-CU3-8.
- **Upstream mínimo:** los procesadores `CmdPoint` y `CmdIntersect`, que reciben las formas
  nuevas (como en `CU2`, las sobrecargas V2 viven en los procesadores existentes), y la
  localización de la sintaxis. Cada archivo se registra en `docs/upstream/modified-files.yml`.
- **Escritorio:** el inspector (grados, clases, configuraciones y causas) y los mensajes. Ninguna
  lógica geométrica.

### Pruebas existentes afectadas, por categorías

- las de `CU0` que fijan B08/B09, H02 y H06 (se invierten en T10);
- las de G8 y G9U0 que fijan raíces de casi-tangencias contradichas por la evidencia certificada
  (se revisan una a una: una que espere una raíz contradicha fija un defecto). El resto debe pasar
  sin cambios (V-EVID-1);
- las de la fase `PRE-G9B-CU2` que materializan raíces en loci con pliegues.

**Inventario de T1** (base `80d72a50`; búsqueda de las clasificaciones de contacto y de las
intersecciones de Locus V2 en las fuentes de prueba):

| Categoría | Pruebas | Efecto previsto |
|---|---|---|
| pins de `CU0` | `PreG9BCu0LocusReferenceTest` (`pins.B08_B09`, con la raíz de la costura `TRANSVERSE_ESTABLISHED` admisible); `PreG9BCu0SyntheticCharacterizationTest` (`pins.H02` y `pins.H06`) | se invierten en T10 con pruebas propias (decisión C1-4); la de la costura, según la evidencia de M3 con la autoridad del objetivo |
| pins de `CU3` (T1) | `PreG9BCu3BaseCharacterizationTest`: tangencia declarada con `δ = +10⁻¹³` (H-CU3-3), cuatro y tres soluciones con `δ = −10⁻¹³` y `−10⁻¹⁰` (H-CU3-1 y H-CU3-4), tangencia en el extremo del cuarto de arco clasificada transversal y admisible (H-CU3-2) | se invierten en T10 solo con la prueba de la corrección |
| clasificación de tangencias exactas | `G8BIntersectionKernelTest`, `G8BIntersectionTopologyAndScientificTest`, `G8BIntersectionLifecycleTest`, `G8C1ExtendedTargetKernelTest`, `G8C1ExtendedTargetLifecycleTest`, `G9S1SemanticSpline2DTest`, `G9S1R1StructuralImplicitCertificationTest` y `G9U0R4IntersectionAdmissibilityContinuationTest` | sin cambios previstos: una clase 3 no es una contradicción; se revisan una a una en T4 y T5 |
| pares de loci | `G8C2LocusPairKernelTest`, `G8C2LocusPairLifecycleTest` y `G9S1R1NativeStructuralPairTest` | sin cambios: los pares quedan fuera de `CU3` |
| clasificaciones inyectadas por accesorios | `G8BIntersectionFixtures` y `G8C2IntersectionTestSupport` (identidad y ciclo de vida sin resolvedor) y el modelo propio de `G8AIntersection*` | sin cambios |
| modelo del autor en `CU2` | `PreG9BCu2AuthorModelConsumerTest`, `PreG9BCu2AuthorModelSelectionSettlementTest` y `PreG9BCu2ConsumerMigrationTest` | se revisan en T4 y T5; la regla de conflicto solo cambia raíces junto a los pliegues del modelo con un certificado de conteo |

Ninguna prueba se debilita: una que esperase una raíz contradicha fija un defecto y se invierte
con su prueba de corrección.

## 4. Plan de verificación

| Momento | Perfil | Evidencia |
|---|---|---|
| T2 (puerta G-M3) | prototipo y banco en el arnés de `tools/benchmark/pre-g9b-cu`, más pruebas focalizadas | informe de la puerta, con contadores, decisiones y correspondencia; ninguna ejecución de aceptación |
| durante T3–T11 | `DEV` y pruebas focalizadas | consola en el directorio temporal; ninguna ejecución de aceptación como efecto secundario |
| candidato congelado | `STATIC`, `INFRA_UNIT`, `PHASE -Phase PRE-G9B-CU3`, `PRE-G9B-CU2`, `PRE-G9B-CU1`, `PRE-G9B-CU0` e `INTEGRATION`, cada uno una vez sobre el mismo commit y el mismo árbol | ejecuciones re-autenticadas (`Read-VerificationJson`, `Get-VerificationResultHash`) |
| bancos | GC3-1 a GC3-5 antes y después | `artifacts/benchmarks/pre-g9b-cu/` |
| comprobación del autor | los tres casos y la costura en la aplicación real, la dinámica y la persistencia | la comprobación manual del autor; nunca se simula |

`FINAL` no lo exige la clase y queda para `CU10`. Registrar la fase exige añadir `PRE-G9B-CU3` al
registro y las selecciones al inventario, como en `CU1` y `CU2`.

## 5. Riesgos y degeneraciones

| Riesgo | Efecto | Mitigación |
|---|---|---|
| redondeo de las constantes capturadas de `L` | una relación estructural que el redondeo de una constante de `Σ_L` rompe (por ejemplo, el coeficiente `z` redondeado de una paralela oblicua) se refuta en `Σ_L⁺`, aunque `Σ_B` la demuestre | se registra como `INCIDENCE_REFUTED_IN_CAPTURED_CONFIGURATION`, nunca como inexistencia; V-M3-3 lo documenta; alternativas de la puerta |
| coste de M3 | la torre crece con la profundidad del subgrafo conjunto | presupuesto versionado, banco GC3-1 y `EXACT_EVALUATION_BUDGET_EXHAUSTED`; la clase 3 sigue visible |
| correspondencia con el algoritmo vivo | una selección heredada definida por comparaciones binary64 cerca de un empate | encierros disjuntos de las ramas exactas o `SELECTION_NOT_RECONSTRUCTED` (V-M3-7) |
| disparador de la regla de conflicto | una raíz contradicha fuera del disparador seguiría publicada | el disparador cubre los mecanismos de H-CU3-1 a H-CU3-4; la limitación se declara (matriz §5) |
| documentos antiguos | los tokens de soluciones contradichas dejan de resolverse | D-CU3-7, nota de migración y V-COMPAT-2 |
| configuraciones distintas (`Σ_L` y `Σ_B`) | una demostración en `Σ_B` no es una propiedad del punto autoritativo | las decisiones se toman en `Σ_L⁺`, que contiene `Σ_L`; `Σ_B` es diagnóstico y no se transfiere sin la condición de la Etapa A §6.6 (V-CFG-1) |
| regla de conflicto demasiado amplia | retirar soluciones válidas por la mera existencia de un punto crítico | certificados de conteo con continuidad y localización (Etapa B §9.1); la clase 3 no contradice (V-CONF-4) |
| pliegues sin simetría exacta | `F′ = 0` sin certificado de pliegue | orden no establecido (Etapa B §4.4) |
| contactos de orden mayor que dos y cúspides | `h″` no certificado lejos del cero | clase 5 con su diagnóstico |
| raíces cerca de la costura | lados del levantamiento periódico | reglas de §2.3 de la Etapa A; `L-CU2-CLOSED-PERIODIC-SEAM` se conserva |
| condición de no degeneración en el límite (`|HD_1| = |HM|`) | `M_1` es un extremo de `n` | M3 decide la pertenencia al objetivo limitado (V-A-19) |
| permutación de las salidas heredadas | `D*` cambia de punto | se sigue al objeto, sin reasociar (V-A-21) |
| componentes puntuales | un punto estructural exacto sobre una componente puntual | `POINT_COMPONENT_NOT_MATERIALIZED` (V-A-15); fuera de `CU3` |

## 6. Fuera de alcance

- `CU4`, `CU5`, `CU6`, `CU7`, `CU8-D`, `CU9`, `CU10` y `G9B`.
- El perfil de esfuerzo (H-1) y cualquier tolerancia configurable.
- El objeto público de punto crítico.
- La materialización en componentes puntuales, C22, C23 y C40.
- La normalización y la tangencia de los pares de loci.
- El trabajo futuro registrado en el cierre de `CU2` §6.
- Los contactos de orden superior y la eliminación algebraica general.
- Cualquier cambio en `geocedg_book`.
