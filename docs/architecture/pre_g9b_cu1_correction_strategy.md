# PRE-G9B-CU1 — Estrategia de corrección de B10.1, H03, H04, H10, H11 y H12

| Campo | Valor |
|---|---|
| Carácter | Diseño de implementación candidato para `CU1`. No autoriza ningún cambio de código |
| Estado | `CU1 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false` |
| Normas | [propuesta normativa revisada](pre_g9b_cu1_normative_proposal.md) (A1.1-R, A1.2-R) |
| Validación | [matriz de validación y criterios de aceptación](../validation/pre_g9b_cu1_validation_matrix.md) |
| Rutas | abreviadas desde `source/shared/common/src/main/java/org/geocedg/common/kernel/` |

## 1. Causas

| Incidencia | Causa | Referencia |
|---|---|---|
| H03 | la firma del evaluador termina en `"|dependency-update=" + candidateRevision`. Como el descriptor no contiene valores geométricos, ese token es lo único que cambia entre recálculos: `hasSameSemanticContent` nunca es verdadero y cada recálculo publica una revisión, también al mover solo el conductor | `algos/AlgoDependentPointLocusV2.java:154-155`; `algos/AlgoLocusV2.java:86-102`; `locus/LocusDefinition2D.java:167-182` |
| H03 (efecto lateral) | `isSemanticPublicationReady()` reconstruye el evaluador como efecto lateral en cada recálculo | `algos/AlgoDependentPointLocusV2.java:112-128, 228-272` |
| B10.1 | cada evaluación puntual ejecuta `resetExternalState()`, que copia las raíces vivas y llama a `isolatedConstruction.updateConstruction(false)` sobre el corte entero, y después `updateCascade()` desde el conductor. La copia anidada de `o_1` recalcula dos veces por punto: publica dos revisiones y reconstruye dos veces su estado métrico (38 000–43 500 evaluaciones anidadas por punto) | `locus/ReconstructibleLocusEvaluator2D.java:152-176, 276-316`; línea base `pins.B10.1` |
| B10.1 (sesiones) | el evaluador ignora la sesión recibida, y los servicios métricos internos abren sesiones nuevas sin relación con ella; no hay presupuesto entre niveles | `locus/ReconstructibleLocusEvaluator2D.java:152-176`; `locus/metric/EvaluatorOnlyLocusMetricCapability2D.java:100-101, 344-345` |
| B10.1 (consultas entre posiciones) | la consulta entre posiciones construye el estado completo y gasta un presupuesto nuevo en refinar la ruta en cada cálculo | `locus/metric/LocusMetricEngine2D.java:145-152`; `EvaluatorOnlyLocusMetricCapability2D.java:141-173, 328-351` |
| H04 | el índice de `o_1` consume 21 000–23 000 de las 32 768 evaluaciones por llamada, por la tolerancia por nodo y la falta de corrección de Richardson | estudio H de `CU0` |
| H10 | sin profundidad mínima; tres muestras colineales se aceptan con defecto 0 | [análisis](pre_g9b_cu1_metric_aliasing_analysis.md) §2 |
| H11 | el suelo absoluto por nodo exige profundidad 14 para `r = 8` (32 769 evaluaciones) | análisis §2 |
| H12 | valor sin corregir (sesgo en `h²`); tolerancia por nodo sin reparto; el adaptador escalar no compara el error con la tolerancia | análisis §2; `locus/metric/LocusMetricResult2D.java:156-174` |
| Instrumentación | `dependencySliceBuilds`, `dependencySliceSynchronizations` y `wholeLocusRegenerations` no tienen registro; cinco contadores métricos nunca se incrementan; los contadores anidados solo son accesibles por reflexión | `locus/LocusInstrumentation2D.java:14-25`; `locus/metric/LocusMetricInstrumentation2D.java:10-28` |

**Riesgo principal.** Hoy, `resetExternalState()` lee las raíces vivas en cada evaluación,
y el token de H03 hace que cada recálculo invalide todas las cachés. Quitar el token sin
más produciría geometría obsoleta: un cambio de radio no se vería. La corrección debe
introducir una huella exacta de las entradas del corte **antes** de retirar el token
(paso S2).

## 2. Pasos

Cada paso termina con sus pruebas focales en verde y sin alterar las referencias
`preserve`.

### S0 — Incorporar las normas aprobadas

Se incorporan los textos aprobados de A1.1-R y A1.2-R a `locus-v2-semantics.md` (v1.2),
a `locus-v2-metrics.md` (v1.5) y a la nota de `locus-v2-public-surface.md`, junto con las
correcciones documentales. El ADR 0037 pasa al estado que fije el autor. Diseño antes que
código (`AGENTS.md` §9).

### S1 — Instrumentación

- Registrar las construcciones y sincronizaciones de corte y los recálculos
  equivalentes.
- Agregar en el locus exterior los contadores de sus definiciones anidadas: cortes,
  revisiones, construcciones de estado métrico y llamadas al evaluador.
- Exponerlos por la instrumentación pública, sin reflexión.
- Añadir la evidencia de trabajo al resultado métrico rico (evaluaciones,
  subdivisiones, profundidad, cajas y dimensión agotada), como exige
  `public-surface.md:390-391`.
- Este paso no cambia ningún comportamiento. Permite medir el antes y el después con
  el mismo banco (`tools/benchmark/pre-g9b-cu/`, escenario `composition`).

### S2 — Huella de la instantánea (H03)

- Calcular, en cada recálculo de `AlgoDependentPointLocusV2`, una huella exacta de las
  entradas no conductoras que lee el corte:
  - el mismo conjunto de elementos que hoy copia `resetExternalState()`;
  - los registros de definición y topología ya incluidos en el descriptor;
  - los valores en bits de los números y coordenadas, el texto de las cadenas y los
    estados lógicos.
- Las entradas que no admiten una huella determinista (aleatorias, dependientes del
  tiempo) fuerzan una instantánea nueva en cada recálculo.
- Sustituir el token `dependency-update` por esa huella en la firma del evaluador.
- Separar la reconstrucción del evaluador de `isSemanticPublicationReady()`: el
  evaluador solo se reconstruye cuando cambia la huella o la definición.
- Un recálculo equivalente conserva la revisión y el evaluador, por la rama
  `restoreDefinedStateAfterEquivalentRecompute()` (`algos/AlgoLocusV2.java:100`).
- Cambiar la aserción de `LocusV2PointDrivenCreationRegressionTest.java:58-66` (decisión
  D-CU1-8).
- Pruebas: V-REV-1 a V-REV-3 y la prueba de no obsolescencia V-REV-2, que cubre deslizadores,
  puntos libres, funciones, textos, booleanos y listas.

### S3 — Ciclo de vida del corte (B10.1)

- `resetExternalState()` y `updateConstruction(false)` se ejecutan una vez por revisión,
  al construir o sincronizar el corte, y nunca por punto.
- La evaluación puntual aplica la coordenada del conductor y actualiza solo su cascada.
- Los algoritmos de las copias anidadas que alcance esa cascada siguen la regla de S2:
  recálculo equivalente, sin revisión y sin reconstrucción.
- Pruebas: V-SLICE-1, V-NEST-1 y V-NEST-2.

### S4 — Sesiones con ámbito de corte y presupuesto anidado

- `ReconstructibleLocusEvaluator2D.evaluate` crea, o reutiliza para la misma revisión,
  una sesión hija de la recibida, con ámbito de corte. La hija:
  - comparte la pila de claves activas, que da la detección de ciclos;
  - comparte el presupuesto de trabajo;
  - califica sus claves de memoria con el ámbito del corte.
- Los servicios métricos, de enlace de posiciones y de intersección que se ejecutan
  dentro del corte usan la sesión hija en lugar de `LocusEvaluationSession2D.reference()`.
- Pruebas: V-SESS-1 y V-SESS-2.

### S5 — Consultas entre posiciones locales y fallos memorizados

- La consulta entre posiciones reutiliza la partición del estado en caché y solo refina
  dentro de las hojas que contienen sus extremos.
- Los fallos deterministas se memorizan bajo su clave completa
  (`locus/metric/LocusMetricIndex2D.java:74-79, 91-94`), con la dimensión de trabajo como
  campo tipado.
- Pruebas: V-NEST-1 (límite de evaluaciones por consulta) y V-BUD-1.

### S6 — Estimador v2 (H10, H11, H12, H04)

- Política `cu1-metric-policy/v2` en `LocusMetricPolicy2D`: versiones, `d_min`, fracción de
  sondeo, factor de seguridad, presupuestos y tolerancia por componente.
- En `EvaluatorOnlyLocusMetricCapability2D`:
  - partición mínima uniforme;
  - corrección de Richardson;
  - sonda no diádica por hoja;
  - reparto de la tolerancia por cuota de parámetro;
  - terminación en la resolución del parámetro notificada como diagnóstico;
  - hipótesis registradas con sus valores.
- Revisar `LocusMetricIntegrator2D` y `DifferentialLocusMetricCapability2D:160`, que pasa
  `geometricScale = 0`, para que apliquen la misma tolerancia por componente.
- `LocusMetricResult2D.isScalarAdmissible()` compara el error con `τ` según la clase
  (25.4), y `LocusMetricAggregator2D` propaga la clase.
- Pruebas: V-MET-EST-1 a V-MET-EST-3, V-ADM-1 y V-INV-1.

### S7 — Capacidad estructural (clase 1)

- Nueva capacidad, primera en el orden, para:
  - ceros e imágenes colapsadas (ya existentes);
  - imágenes afines, mediante `CertifiedAffineLocus2D`;
  - imágenes por semejanza, mediante `LocusSimilarityTransform2D.getLengthScale()` y el
    resultado de la fuente;
  - semejanzas del conductor: un programa capturado de clase v1/v2 cuyos pasos son todos
    traslaciones, giros, simetrías u homotecias con parámetros constantes sobre un
    conductor circunferencia, arco o segmento.
- Aritmética con redondeo hacia fuera.
- Pruebas: V-MET-STRUCT-1 a V-MET-STRUCT-3, que invierten H11.

### S8 — Envolvente certificada (clase 2)

- Fachada pública mínima sobre `CertifiedIntervalCurveModel2D`, con `SplineIntervalModel2D`
  y `ConstructionIntervalModel2D`, sin cambiar su contrato.
- Raíz cuadrada con redondeo exterior.
- Cota del cono por caja y refinamiento adaptativo por anchura de la envolvente, con su
  presupuesto.
- La envolvente se adjunta como evidencia cuando no alcanza `τ`, y se comprueba que la
  estimación quede dentro de ella.
- Pruebas: V-MET-ENCL-1 y V-MET-ENCL-2.

### S9 — Fijaciones y referencias de `CU0` (C1-4)

Se aplica el procedimiento de §4 a `pins.B10.1`, `pins.H03` y `pins.H10_H11`, y se
comprueba que todas las secciones `preserve` se reproducen dentro de su tolerancia.

### S10 — Verificación

- Selección `PRE-G9B-CU1` registrada con el mecanismo oficial.
- `PHASE -Phase PRE-G9B-CU1` y `PHASE -Phase PRE-G9B-CU0` (con las fijaciones invertidas).
- `INFRA_UNIT`, `INTEGRATION` y `STATIC` sobre el candidato congelado.
- Banco antes y después.

S6, S7 y S8 son independientes entre sí una vez hecho S1. S2 precede a S3, y S3 a S4 y S5.

## 3. Efecto previsto en el modelo del autor

| Magnitud | Antes | Después previsto | Referencia `preserve` |
|---|---|---|---|
| Revisiones de `o_1` al mover el conductor | +1 | 0 | `pins.H03` invertida |
| Revisiones y estados métricos anidados por punto de `e_2` | +2 / +2 | 0 / 0 tras la primera consulta de cada revisión | `pins.B10.1` invertida |
| Evaluaciones anidadas por punto de `e_2` | 31 000–55 000 | acotadas por el trabajo local de la consulta entre posiciones (objetivo ≤ 256) | G3 |
| Clase de la longitud de `o_1` | `ESTIMATED_ERROR` sin hipótesis de escala | clase 3 con hipótesis registradas (sin clase v3 no hay envolvente) | — |
| Longitud total de `o_1` | `13,2364580874` (sesgo `1,08·10⁻⁷`) | ≈ `13,2364581958` | dentro de `10⁻⁶` |
| Puntos de `e_2` y perfil de `d_2` | sesgo heredado de `w_1` (≈ `10⁻⁷`) | corregidos en la misma magnitud | dentro de `10⁻⁶` (`LENGTH_TOLERANCE`) |
| H10 con `k = 1, 2, 4` | 1,0 | clase 3 dentro de `τ` | `pins.H10_H11` invertida |
| Circunferencias de radio 1, 7, 8 y 20 | sesgo `6·10⁻⁹` relativo; radio ≥ 8 indefinido | clase 1, `2πr` con cota de redondeo | `pins.H10_H11` invertida |
| Arrastre con `e_2` (informativo) | ≈ 5,7 s por paso | próximo al caso sin `e_2` (≈ 75 ms) | indicador complementario (C1-1) |

## 4. Procedimiento para fijaciones y referencias (C1-4)

1. Cada fijación propiedad de `CU1` (`pins.B10.1`, `pins.H03`, `pins.H10_H11`) se invierte
   con una prueba de aceptación nueva de `CU1` que afirma el comportamiento corregido.
2. La aserción de fijación de la prueba de `CU0` correspondiente se sustituye por una
   comprobación de que su incidencia está invertida. La comprobación nombra la prueba de
   `CU1` y no conserva el valor defectuoso como expectativa.
3. La línea base se regenera con el arnés (`-Scenario references`), junto con un
   **registro de nueva línea base**. El registro lista cada campo cambiado con su valor
   anterior y nuevo, su causa y la prueba de `CU1` que lo justifica. Las secciones
   `preserve` deben reproducirse dentro de su tolerancia; cualquier diferencia mayor
   detiene el trabajo y se presenta al autor.
4. Ninguna referencia numérica cambia sin ese registro.
5. Las pruebas existentes que fijan el comportamiento actual se inventarían antes de
   cambiarlas. Son, al menos:
   - `LocusV2PointDrivenCreationRegressionTest.java:58-66`;
   - `source/shared/common-jre/src/test/java/org/geocedg/common/locus/LocusMetricProductiveNumericalTest.java:216-326`;
   - las pruebas G7/G9 que comparan longitudes con el estimador actual.

   Cada cambio es una conformidad declarada con su justificación, nunca un
   debilitamiento silencioso.

## 5. Riesgos

| Riesgo | Mitigación |
|---|---|
| Resultados obsoletos tras retirar el token | huella exacta antes de retirarlo (S2); V-REV-2 compara, para cada clase de entrada, con una construcción recién creada con los mismos comandos y valores |
| Colisión de sesiones entre la copia anidada y el locus vivo | claves con ámbito de corte (S4) |
| Hilo propietario de las métricas anidadas | sin cambios: toda la evaluación sigue en el hilo del núcleo |
| Coste de la sonda y de la partición mínima en loci simples | medido con el banco; constantes dentro de los intervalos aprobados |
| La envolvente rechaza cajas cerca de configuraciones degeneradas | queda sin resolver y cae a la clase 3, sin falsos certificados |
| Pruebas históricas que fijan el comportamiento actual | inventario y conformidad declarada (§4) |
| Necesidad de cambiar un contrato de `CU2` o `CU3` | parada y presentación de la dependencia |

## 6. Fuera de alcance

- certificación de componentes de existencia y regla de publicación (`CU2`);
- topología y evento de envoltura de `e_2` (`CU2`);
- teselación (`CU2`);
- materialización y tangencias (`CU3`);
- clase v3 del modelo certificado (`CU2`);
- M08 y M09;
- mejoras de interfaz;
- libro.
