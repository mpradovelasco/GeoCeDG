# PRE-G9B-CU2 — Estrategia mínima de implementación de B07, B10.2, H01 y B06

| Campo | Valor |
|---|---|
| Carácter | Diseño de implementación candidato para `CU2`. No autoriza ningún cambio de código |
| Estado | Estrategia aprobada para la implementación por la decisión autoral de 2026-10-21 ([registro](../validation/pre_g9b_cu2_author_architecture_approval_record.md)), con la precisión D-CU2-20-R incorporada en T1 y T3 y las condiciones de parada de §4.1; `selfApproved=false` |
| Normas | [propuesta normativa](pre_g9b_cu2_normative_proposal.md) (A2.1-R a A2.6); [ADR 0038 (ACCEPTED, 2026-10-21)](../adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md) |
| Diseños | [certificación](pre_g9b_cu2_hybrid_certification_design.md); [evento de la longitud orientada](pre_g9b_cu2_oriented_length_event_design.md); [consumo y representación](pre_g9b_cu2_consumption_and_representation.md); [análisis del modelo del autor](pre_g9b_cu2_author_model_certification_analysis.md) |
| Validación | [matriz de validación y criterios de aceptación](../validation/pre_g9b_cu2_validation_matrix.md) |
| Rutas | abreviadas desde `source/shared/common/src/main/java/org/geocedg/common/kernel/` |
| Revisión | 2026-10-20, revisión focalizada P1–P5: T3, T4, T5, T7, T8, T10 y T11 ajustados, condiciones de parada 12 a 14 y efecto previsto en el valor crítico. Cierre de coherencia C1–C3 (2026-10-20): T2, T3, T4, T9 y T10 ajustados, condiciones de parada 15 a 17, causas, efecto previsto, riesgos y fuera de alcance. Aprobación autoral (2026-10-21, paso T0): D-CU2-20-R en T1 y T3, §4.1, y corrección de la atribución de F-CU2-12. El orden y la numeración T0–T13 no cambian |

## 1. Causas

| Incidencia | Causa | Referencia |
|---|---|---|
| B07, B10.2 | el productor dependiente publica el certificado de compatibilidad: `COMPLETE` con una componente igual al dominio declarado, sin prueba, y copia `PERIODIC` del descriptor | `algos/AlgoDependentPointLocusV2.java:134-137, 165-192`; `locus/LocusExistenceStructure2D.java:159-175` |
| B07, B10.2 (otros productores) | el constructor de rama sin estructura explícita también crea el certificado de compatibilidad; lo usan `AlgoSplineV2` y los recorridos de segmento (`LocusV2Factory.fullDomainBranch`) | `locus/LocusBranch2D.java:30-38`; `algos/AlgoSplineV2.java:99`; `locus/LocusV2Factory.java:35-50` |
| B07 (certificación imposible) | el modelo de intervalos lanza `ArithmeticException` ante un predicado no uniforme en una caja, no distingue «indeciso» de «uniformemente inválido» y no admite rayos, recta–circunferencia, varias salidas, vectores ni macros | `locus/intersection/ConstructionIntervalModel2D.java:356-363`; `certified-construction-model` §3, §5 y §9 |
| B07 (cierre) | cinco predicados distintos deciden el cierre | `LocusRenderCache2D.java:137-147`; `SemanticCurveExportAdapter2D.java:471-480`; `PolicyV2LocusMetricCapability2D.java:770-781`; `LocusMetricRouteResolver2D.java:102-111`; `LocusPointInteractionResolver2D.java:1158-1162` |
| B07 (dibujo) | abandono del modo adaptativo en la primera evaluación inválida y retorno a 32 muestras uniformes; profundidad mínima 2; proyección acotada de la desviación | `LocusRenderCache2D.java:149-168, 178-180, 205-207, 210-216, 238-239` |
| B07 (exportación) | una componente con extremo abierto se descarta como `MISSING_DOMAIN`; el residuo de cierre es idénticamente 0 | `SemanticCurveExportAdapter2D.java:257-259, 281-295`; `AdaptiveCurveApproximationBuilder2D.java:323-341` |
| H01 | la publicación exige el punto trazado definido en la posición actual del conductor; mientras no publica, la evaluación usa la última definición (F-CU2-3) | `algos/AlgoDependentPointLocusV2.java:114-130, 414-427`; `algos/AlgoLocusV2.java:89-91` |
| B10.2 (métrica) | la ruta de `w_1` envuelve en el origen, pero nada publica ese hecho como evento estructural | `locus/metric/LocusMetricRouteResolver2D.java:102-111, 144-167` |
| B06 | criterio único de cuerda de 0,75 px sin criterio de giro ni profundidad mínima suficiente | `LocusRenderCache2D.java:210-216` |
| Identidad | la clave de componente incluye los bits de sus extremos; ningún productor emite otra transición de linaje que `unchanged()` | `locus/LocusComponentLineage2D.java:21-32`; `locus/LocusLineage2D.java:16-117` |
| Selección (P4) | la salida de una intersección recta–circunferencia es una raíz bruta elegida por una permutación que es estado del algoritmo; se reinicializa por cercanía al crear, al cargar y en cada `updateConstruction`, también en la copia del corte de cada revisión | `org/geogebra/common/kernel/algos/AlgoIntersectLineConic.java:224-235, 268-281, 467-562`; `org/geogebra/common/kernel/Construction.java:1474-1496`; `locus/ReconstructibleLocusEvaluator2D.java:133-143, 334-346` |
| Valor crítico (P1) | ninguna prueba de raíz simple certifica un cero doble, y el evaluador declara tangente lo que está a menos de su tolerancia | `org/geogebra/common/kernel/algos/AlgoIntersectLineConic.java:617-640, 703-704, 741-746`; [análisis](pre_g9b_cu2_author_model_certification_analysis.md) §3.8 |
| Salidas en raíces coincidentes (C1) | la segunda salida sin etiqueta se anula cuando coincide con la primera dentro de 10⁻⁸; su definición cerca de una raíz coincidente es dispersa y difiere entre el vivo y el corte (F-CU2-13) | `org/geogebra/common/kernel/algos/AlgoIntersect.java:40-45`; `AlgoIntersectLineConic.java:280`; `AlgoIntersectSingle.java:236-279`; análisis §3.9 |
| Huella y fidelidad del corte (F-CU2-12, corregido el 2026-10-21) | la huella de `CU1` no incluye el estado de selección ni el de etiquetado; un literal sin texto original puede escribirse con 15 cifras; nada valida que el corte reproduzca sus entradas autoritativas. Los valores numéricos sí se escriben exactos (`Double.toString`) | `locus/ReconstructionSliceFingerprint2D.java`; `locus/ReconstructibleLocusEvaluator2D.java`; `org/geogebra/common/io/XMLStringBuilder.java` (`attr(String, double)`); `org/geogebra/common/kernel/StringTemplate.java:309-325`; diseño de certificación §8.1 |
| Componentes puntuales (C2) | la estructura de existencia solo tiene intervalos de longitud positiva: una existencia reducida a un parámetro aislado no se puede expresar | `locus/LocusExistenceStructure2D.java`; diseño de certificación §3.5 |
| Consumidores | vistas heredadas (`getValidDomainComponents()`, `PERIODIC`) e inconsistencia índice/tamaño en `resolveOpenBoundary` | [consumo](pre_g9b_cu2_consumption_and_representation.md) §2; `LocusMetricRouteResolver2D.java:180-184` |

**Riesgo principal.** El certificado de compatibilidad sostiene hoy la cadena
`o_1 → LocusLength → d_2 → e_2`: la ocurrencia generadora exige `COMPLETE`
(`IntersectionEndpointProvenanceResolver2D.java:262-286`). Publicar certificados reales antes
de migrar los consumidores, o retirar la compatibilidad antes de certificar `o_1`, dejaría
indefinidos `w_1`, `d_2` y `e_2`. Por eso los certificados nuevos se calculan **en sombra**
(T4–T7) y se publican en un solo paso (T11), cuando todos los consumidores leen ya la
estructura de existencia.

## 2. Pasos

Cada paso termina con sus pruebas focales en verde y sin alterar las referencias `preserve`.

### T0 — Incorporar las normas aprobadas

Se incorporan los textos aprobados de A2.1-R a A2.6 a `locus-v2-existence-components.md`
(1.1), `locus-v2-certified-construction-model.md` (1.2), `locus-v2-metrics.md` (1.6),
`locus-v2-presentation.md` (2) y `locus-v2-semantics.md` (1.3), con las notas de alcance en
`intersections`, `extended-intersections`, `point-interaction`, `similarity-transformations`,
`public-surface` y en la especificación DXF, y la especificación durable de LaTeX si se adopta
D-CU2-11. El ADR 0038 pasa al estado que fije el autor. Diseño antes que código
(`AGENTS.md` §9).

### T1 — Instrumentación y bancos

- Contadores públicos, sin reflexión: cálculos de certificado por revisión, cajas por rama,
  transiciones certificadas, bandas `UNRESOLVED`, recorridos exploratorios, vértices de
  dibujo por vista y lecturas de valores métricos durante una certificación (GC-6).
- Escenarios `certification` y `tessellation` en el arnés de `CU0`
  (`tools/benchmark/pre-g9b-cu/`), medidos antes de cualquier cambio de comportamiento.
- D-CU2-20-R, caracterización focal: pruebas de la huella y de la fidelidad del corte sobre la
  base (cambios de un ulp, literales sin texto original, argumentos literales en línea, estado de
  selección y de etiquetado) y contadores de huellas, reconstrucciones, validaciones,
  restauraciones y fallos de fidelidad ([diseño de certificación](pre_g9b_cu2_hybrid_certification_design.md) §8.1).
- Este paso no cambia ningún comportamiento.

### T2 — Estructura de existencia v2

- Arcos en dominios periódicos (inicio canónico, longitud, inclusión de extremos); intervalos
  sin cambio en los no periódicos.
- Tipos de frontera: extremo de dominio, transición certificada con su fuente
  `(operación, predicado, lado)`, evento estructural y borde de banda `UNRESOLVED`.
- Causas tipadas de la cobertura, con la distinción entre no existencia certificada e
  indefinición computacional.
- Evidencia exploratoria como estructura hermana, derivada y no persistida.
- Componente puntual como entidad propia (C2; diseño de certificación §3.5): envolvente del
  parámetro con su fuente estructural, inclusión cerrada, maximalidad, linaje con transiciones
  de cambio de dimensión, longitud cero y estado de su posición. Nunca un intervalo de longitud
  cero.
- Naturaleza de la prueba registrada en cada hecho certificado (C3).
- Consulta única de cierre (`closed periodic`) que todos los consumidores usarán en T9.
- Las vistas heredadas quedan como adaptadores documentados; un arco se devuelve como una
  componente.
- El productor sigue publicando la compatibilidad, ahora expresada en el modelo nuevo: ningún
  cambio visible.
- Pruebas: V-ARC-2, V-LVL-2 y V-POINT-1 sobre estructuras construidas en prueba.

### T3 — Clase v3 del modelo de construcciones certificadas

- Identificador `certified-construction-program/v3`; los programas v1 y v2 no se reetiquetan.
- Captura de rayos, segmentos, circunferencias por centro y punto, recta–circunferencia,
  pasos de varias salidas, operaciones vectoriales, expansión de macros y entrada métrica
  estructural.
- Selección de las intersecciones (P4, C1): lectura del estado de selección del algoritmo
  (permutación, indicadores de inicialización y etiquetado de las salidas) con accesores de
  solo lectura registrados como modificaciones; huella de la revisión con ese estado;
  comparación con el algoritmo vivo en cada sincronización, con fallo cerrado si difieren;
  compactación solo donde el número de raíces en los caminos está probado.
- D-CU2-20-R, corrección (puerta temprana y bloqueante): huella
  `reconstruction-slice-fingerprint/v2` en `ReconstructionSliceFingerprint2D` con los bits
  binary64 de las entradas autoritativas, el estado de selección y de etiquetado y las versiones
  de política y contrato; validación de fidelidad del corte con restauración admitida y fallo
  explícito, según el ciclo de vida de [diseño de certificación](pre_g9b_cu2_hybrid_certification_design.md)
  §8.1. Se completa con su validación focal antes de que ningún certificado de T4 se considere
  válido o se publique.
- Envolventes de valor y primera derivada con redondeo exterior para cada capacidad nueva.
- Resultado de caja `VALID`, `UNIFORMLY_INVALID` con causa tipada o `UNRESOLVED`, en lugar de
  la excepción, solo en el camino v3; `UNIFORMLY_INVALID` por intervalos solo para
  desigualdades estrictas.
- Límite versionado de pasos dependientes del conductor tras expandir macros (D-CU2-5).
- Pruebas: V-V3-1 a V-V3-4, V-SEL-1 a V-SEL-7, V-UNIF-1, V-COINC-1, V-COINC-2, V-COINC-8 y
  V-REV-EXACT-1 a V-REV-EXACT-10.

### T4 — Certificador híbrido en sombra

- Contrato versionado `locus-existence/hybrid/v1`.
- Partición diádica sobre el círculo, que envuelve la costura, con profundidad mínima y
  presupuesto por rama y revisión (D-CU2-5).
- Atajo de la tabla de operaciones totales (autoridad 1).
- Transiciones certificadas por Newton de intervalos o Krawczyk escalar sobre el predicado,
  hasta un corchete de anchura `δ`; inclusión de extremos según el predicado.
- Certificado algebraico exacto de los predicados de primer nivel (P1;
  [diseño de certificación](pre_g9b_cu2_hybrid_certification_design.md) §4.6): racionales y
  polinomios exactos, máximo común divisor, sucesión de Sturm, bisección exacta y evidencia
  registrada, con su presupuesto. Produce transiciones, parámetros singulares interiores y
  componentes puntuales.
- Para los predicados que no son de primer nivel, método del punto estacionario (Krawczyk sobre
  `P′` con `P″`) y `UNRESOLVED` si no decide.
- Salidas efectivas en raíces coincidentes (C1; diseño de certificación §4.7): una segunda
  salida sin etiqueta recibe una banda `UNRESOLVED` alrededor de la raíz coincidente, acotada por
  predicados de primer nivel conservadores, y su locus queda `NOT_ESTABLISHED`; su valor límite
  se registra como evidencia y nunca como salida.
- Identificación periódica solo con la premisa de función de configuración; cierre solo con
  continuidad certificada en el punto de cierre.
- Evidencia exploratoria determinista para las bandas sin resolver.
- Un cálculo por revisión, compartido (A1.1-R de `CU1`).
- El resultado se expone a las pruebas y a la instrumentación, **no** se publica todavía.
- Pruebas: V-LVL-1, V-LVL-3, V-CMP-1, V-TR-1, V-TR-2, V-TR-4, V-CRIT-1 a V-CRIT-10,
  V-COINC-3 a V-COINC-7, V-COINC-9, V-POINT-2 a V-POINT-4, V-TOT-1, V-PER-1 a V-PER-3, V-UND-1,
  V-COMPL-1, GC-4 y GC-8 sobre el certificado en sombra.

### T5 — Pliegues certificados y chorros de segundo orden (P3, D-CU2-6)

- Envolventes de la segunda derivada para las operaciones de la clase v3.
- Certificado de simetría estructural: igualdades racionales exactas de las constantes
  capturadas y tabla versionada de operaciones equivariantes e invariantes.
- Certificación de un pliegue con sus cinco pruebas separadas: existencia por simetría,
  aislamiento y no degeneración por `F″`, continuidad y tangentes laterales opuestas
  ([diseño de certificación](pre_g9b_cu2_hybrid_certification_design.md) §5).
- Pruebas: V-FOLD-1 a V-FOLD-4.

### T6 — Certificado estructural de la métrica y evento `ORIGIN_WRAP`

- En el resultado rico de `LocusLength`, ligado a la revisión y no persistido: dominio de
  definición, continuidad y evento con `jumpPositive` certificado por una cuerda con
  redondeo exterior.
- Ningún valor, política, orden ni dirección de `LocusLength` cambia.
- Pruebas: V-E2-4, V-E2-6 y V-E2-2.

### T7 — Certificación del modelo del autor en sombra

- `e_1` y `o_1`: predicados uniformes; componentes periódicas cerradas; pliegues de `e_1`.
- `o`, `v`, `w` y `a_1`: penetración, mordedura y valor crítico, este con el certificado
  algebraico exacto; y los binary64 contiguos al valor crítico.
- `e_2`: entrada métrica estructural y macros totales; una componente abierta.
- Pruebas: V-ARC-1, V-E2-1 y V-E2-5 sobre el certificado en sombra, y GC-6.

### T8 — Linaje estructural y migración de selectores (E-3)

- Clave de linaje de componente por las fuentes de sus fronteras; transiciones tipadas
  (círculo a arco y vuelta); identidades durable, de selección y de componente separadas.
- Resolución de selectores persistidos solo por correspondencia estructural demostrada,
  validada por contención única; sin reescribir el contenido persistido; fallo cerrado con
  causa tipada ante ambigüedad (E-3 revisada).
- Pruebas: V-LIN-1 a V-LIN-6 y V-E2-7.

### T9 — Migración de los consumidores y autoridad única de cierre

- Dibujo, exportación DXF y LaTeX, métrica total, rutas, prueba de ciclo, vinculación de
  posiciones, ocurrencia generadora, intersecciones, punto semántico, interacción, selector de
  rama principal, loci anidados y semejanzas leen la estructura de existencia y la consulta
  única de cierre.
- Corrección de `resolveOpenBoundary`.
- Separación entre existencia, evaluación y representación (P5): un punto del evaluador en una
  no existencia certificada no se publica como válido; un fallo de evaluación es computacional;
  la consulta explícita de límites laterales sustituye a la canonicalización de los extremos.
- Componentes puntuales (C2; consumo §3.1): aportan cero a totales y rutas; los consumidores
  que direccionan por un parámetro binary64 fallan explícitamente con causa tipada; las
  intersecciones no declaran `EMPTY` sin excluirlas; los loci dependientes declaran la
  limitación.
- Con la compatibilidad aún publicada, el comportamiento visible no cambia salvo las
  correcciones declaradas.
- Pruebas: V-CON-1 a V-CON-10, V-AVAIL-1, V-POINT-5 y V-POINT-6.

### T10 — Política de dibujo v2 y aproximaciones de exportación

- Profundidad mínima `d_min`, criterio de giro `θ_max`, desviación sin acotar, exención solo
  en pliegues certificados y en el vértice de un parámetro singular certificado, y presupuesto
  de vértices (D-CU2-10).
- Reparametrización `t = t* ± s²` en fronteras de discriminante, vértice exacto con la raíz
  doble en los parámetros singulares, y límites laterales en fronteras de evento.
- Sin retorno a muestras uniformes; sin interpolar a través de un punto no evaluable; fidelidad
  declarada por componente (P5).
- Exportación: extremos abiertos con límite o, si no se puede evaluar, hasta el último vértice
  evaluado con su estado declarado; profundidad mínima para componentes abiertas; residuo de
  cierre contra el certificado.
- Componentes puntuales declaradas y no dibujadas (D-CU2-19): inspector, anexo DXF y
  comentarios LaTeX, con fidelidad parcial; nunca un segmento ni una curva. Las bandas de la
  supresión heredada (C1) no se cruzan.
- Pruebas: V-B06-1 a V-B06-3, V-B07-2, V-REP-1, V-REP-2, V-DXF-3 y V-AVAIL-2 a V-AVAIL-6, con
  certificados de prueba, y la parte de representación de V-POINT-5.

### T11 — Conmutación

En un solo paso:

- el productor dependiente publica el certificado híbrido; un locus que no se certifica queda
  `NOT_ESTABLISHED`, con o sin componentes locales, y nunca vuelve a la compatibilidad;
- los productores estáticos y analíticos pasan al certificado explícito que fije D-CU2-7;
- se retira `completeCompatibility`;
- se aplica la regla de publicación (H01), sin definiciones obsoletas, y la de recubrimiento
  vacío (D-CU2-8);
- el certificador de 512 celdas de SplineV2 se trata según D-CU2-9.

Pruebas: V-PUB-1 a V-PUB-4, V-B07-1, V-B07-3, V-B07-4, V-E2-3, V-E2-8, V-E2-9, V-DXF-1,
V-DXF-2, V-TEX-1, V-TEX-2, V-DET-1, V-INV-1, V-INV-2 y todas las anteriores sobre los
certificados publicados.

### T12 — Fijaciones y referencias de `CU0` (C1-4)

Se aplica el procedimiento de §5 a `pins.B07`, `pins.H01`, `pins.B06` y `pins.B10.2`, y se
comprueba que todas las secciones `preserve` se reproducen dentro de su tolerancia. Pruebas:
V-CU0-1 a V-CU0-3 y V-CU1-1.

### T13 — Verificación

- Selección `PRE-G9B-CU2` registrada con el mecanismo oficial.
- `PHASE -Phase PRE-G9B-CU2`, `PHASE -Phase PRE-G9B-CU1` y `PHASE -Phase PRE-G9B-CU0` (con las
  fijaciones invertidas).
- `INFRA_UNIT`, `INTEGRATION` y `STATIC` sobre el candidato congelado (V-PROD-1 y V-PROD-2).
- Bancos antes y después, y la comprobación externa V-DXF-4 (informativa).

## 3. Dependencias y orden de retirada

| Paso | Requiere |
|---|---|
| T1 | T0 |
| T2, T3 | T1 |
| T4 | T2 y T3 |
| T5 | T3 |
| T6 | T2 |
| T7 | T4, T5 y T6 |
| T8 | T2 |
| T9 | T2 (independiente de T4–T8) |
| T10 | T9; la exención de pliegues, T5; la de parámetros singulares, T4 |
| T11 | T7, T8, T9 y T10 |
| T12 | T11 |
| T13 | T12 |

T5, T6 y T8 son independientes entre sí; T9 y T10 pueden avanzar en paralelo a T4–T7. El
orden de retirada del [diseño de certificación](pre_g9b_cu2_hybrid_certification_design.md) §9
se cumple: certificador, familias, evento, `e_2`, consumidores, retirada y fijaciones.

## 4. Condiciones de parada

El trabajo se detiene y se presenta al autor, sin rodeos ni debilitamientos, si:

1. la clase v3 no certifica un locus del modelo en un régimen en que el
   [análisis](pre_g9b_cu2_author_model_certification_analysis.md) §6 prevé certificarlo con los
   presupuestos dentro de los intervalos aprobados;
2. certificar `e_2` exigiera el valor de una longitud (instrucción de cierre de `CU1`);
3. una corrección exigiera cambiar valores, políticas, orden o dirección de `LocusLength`, o
   contradijera el contrato métrico de `CU1` (§25);
4. un valor `preserve` cambiara fuera de su tolerancia;
5. hiciera falta un punto estructural, una materialización de raíz tangente, un cambio del motor
   de intersecciones o una herramienta pública nueva (`CU3`);
6. la captura exigiera modificar clases upstream más allá de accesores de solo lectura
   registrados como modificaciones;
7. hiciera falta cambiar el formato persistido o la proyección de dependencias durables del
   ADR 0029;
8. una prueba existente solo pudiera pasar debilitándola;
9. una puerta GC-1 a GC-6 no se cumpliera;
10. `INTEGRATION` fallara por una causa ajena a `CU2` (precedente D-CU1-R6): se registra, se
    caracteriza y se presenta;
11. la clase de verificación aprobada resultara insuficiente: `VERIFICATION_ESCALATION_REQUEST`;
12. el certificado algebraico exacto no fuese aplicable a un predicado que necesita el modelo del
    autor, o agotara su presupuesto dentro de los intervalos aprobados;
13. la copia del corte no reprodujera el estado de selección del algoritmo vivo en el modelo del
    autor, o reproducirlo exigiera escribir en clases heredadas;
14. resolver un selector persistido exigiera reescribir contenido persistido o reasociarlo por
    contención sin una correspondencia demostrada;
15. un locus del modelo del autor dependiera de una segunda salida sin etiqueta en una raíz
    coincidente, la copia del corte no reprodujera el etiquetado del vivo, o mantener la
    continuidad exigiera sustituir una salida por su valor límite o por otra salida (D-CU2-18);
16. la huella de la revisión no distinguiera un cambio de entrada o el corte no reprodujera sus
    entradas autoritativas con las modificaciones que autoriza D-CU2-20-R: ningún certificado se
    reutiliza ni se publica entre estados que la huella no distingue;
17. un consumidor necesitara materializar o dibujar una componente puntual más allá de lo que
    fije D-CU2-19 (`CU3`).

### 4.1 Condiciones adicionales de la autorización de 2026-10-21

Rigen además las doce condiciones de parada de la decisión autoral
([registro](../validation/pre_g9b_cu2_author_architecture_approval_record.md) §9), entre ellas: no
poder garantizar la exactitud de las instantáneas o la fidelidad de la reconstrucción con las
modificaciones autorizadas; un cambio de un ulp que no invalide un estado semánticamente
distinto; que D-CU2-20-R exija cambiar la persistencia o el comportamiento heredado; y que la
verificación exigible supere la clase aprobada (`VERIFICATION_ESCALATION_REQUEST`). Ante
cualquiera: diagnóstico, evidencia, alternativas mínimas y decisión autoral necesaria.

## 5. Procedimiento para fijaciones y referencias (C1-4)

1. Cada fijación propiedad de `CU2` (`pins.B07`, `pins.H01`, `pins.B06`, `pins.B10.2`) se
   invierte con una prueba de aceptación nueva de `CU2` que afirma el comportamiento corregido.
2. La aserción de fijación de la prueba de `CU0` correspondiente se sustituye por una
   comprobación de que su incidencia está invertida. La comprobación nombra la prueba de `CU2`
   y no conserva el valor defectuoso como expectativa. Los métodos no se renombran, de modo que
   las identidades de la selección `pre-g9b-cu0.desktop` no cambian (precedente de `CU1`).
3. La línea base se regenera con el arnés (`-Scenario references`), junto con un **registro de
   nueva línea base** que lista cada campo cambiado con su valor anterior y nuevo, su causa y la
   prueba de `CU2` que lo justifica. Las secciones `preserve` deben reproducirse dentro de su
   tolerancia; cualquier diferencia mayor detiene el trabajo.
4. Ninguna referencia numérica cambia sin ese registro. La línea base conserva las firmas
   fijadas como evidencia histórica.
5. Las pruebas existentes que fijan el comportamiento actual se inventarían antes de
   cambiarlas: las que afirman el contrato `locus-existence/provider-compatibility/v1`, la
   componente `[−π, π)` o el cierre periódico de loci dependientes, y las de dibujo y
   exportación que fijan el número de vértices o subtrazos. Cada cambio es una conformidad
   declarada con su justificación.

## 6. Efecto previsto en el modelo del autor

| Magnitud | Antes | Después previsto | Referencia |
|---|---|---|---|
| `o`, `v`, `w`, `a_1` a excCyl 2,9 | `COMPLETE` de compatibilidad | `COMPLETE` híbrido, una componente periódica cerrada certificada | `pins.B07` |
| Los mismos en mordedura (3,7; 3,85; 3,9) | componente falsa `[−π, π)`; 2 subtrazos; hueco de 6 a 24 px en los extremos | un arco cerrado que cruza la costura; 1 subtrazo; extremos a ≤ 0,75 px de la frontera | `pins.B07` invertida |
| Los mismos cerca del valor crítico (3,657–3,66) | un subtrazo a través de un hueco real (F-CU2-2) | transiciones certificadas exactamente; ninguna unión | V-B07-3 |
| Los mismos en los binary64 contiguos al valor crítico | `COMPLETE` de compatibilidad; el evaluador define puntos dentro del hueco exacto | penetración o mordedura decididas exactamente; los puntos del evaluador en el hueco no se publican como válidos | V-CRIT-7 |
| Los mismos en el valor crítico, en una configuración que lo represente exactamente | — (no representable con el deslizador) | `COMPLETE`, una componente periódica cerrada con un parámetro singular certificado en `t_c`; `{Q, R}` etiquetadas, definidas e iguales en `t_c` | V-CRIT-1, V-COINC-1 |
| Los mismos tras un cambio de un ulp de excCyl | revisión nueva y corte exacto con el conductor fuera de la degeneración; sin republicación con el punto trazado indefinido (H01) | revisión nueva y corte exacto en cualquier posición del conductor, con la fidelidad validada y el estado de selección en la huella (D-CU2-20-R) | V-REV-EXACT-1 a V-REV-EXACT-10 |
| Construcciones con una segunda salida sin etiqueta (fuera del modelo) | `COMPLETE` de compatibilidad; definición dispersa cerca de la raíz coincidente | banda `UNRESOLVED` y `NOT_ESTABLISHED`, con componentes locales | V-COINC-2 |
| Existencia reducida a un parámetro aislado (fuera del modelo) | `COMPLETE` de compatibilidad con una componente `[−π, π)` | componente puntual declarada, sin dibujo | V-POINT-1 a V-POINT-6 |
| Los mismos a excCyl 4,0 con el conductor guardado | no publicados | publicados, `COMPLETE`, un arco | `pins.H01` invertida |
| `e_1` | `COMPLETE` de compatibilidad; giro de 180° | `COMPLETE` certificado; dos pliegues certificados; giro ≤ `θ_max` fuera de ellos | `pins.B06` invertida |
| `o_1` | `COMPLETE` de compatibilidad | `COMPLETE` certificado; selector de `N_1` migrado | `preserve.state` |
| `e_2` | periódica cerrada falsa; segmento de cierre de 13,19 | `COMPLETE`, una componente abierta `[t_N, t_N + 2π)`; sin segmento de cierre | `pins.B10.2` invertida |
| Giro máximo del dibujo | `o` 49,4°, `v` 54,1°, `w` 156,8°, `a_1` 38,4°, `o_1` 18,7° | ≤ `θ_max` (candidato 10°) | `pins.B06` invertida |
| Vértices de dibujo | 9 a 27 por locus | del orden del giro total entre `θ_max` (unas decenas a ~150), dentro del presupuesto | GC-5 |
| Valores de `LocusLength` y coordenadas de `e_2` | — | sin cambio | `preserve.e_2`, `preserve.oriented_length_profile` |
| Extremos de rama por validez puntual | — | sin cambio (el evaluador no cambia) | `preserve.branch_ends` |

## 7. Riesgos

| Riesgo | Mitigación |
|---|---|
| Estado intermedio incoherente entre productores y consumidores | certificados en sombra (T4–T7) y conmutación única (T11) |
| Coste de las cajas cerca de fronteras casi degeneradas | presupuesto y `UNRESOLVED` tipado; banco antes y después (GC-4) |
| La clase v3 no alcanza una construcción del usuario fuera del modelo | fallo cerrado con evidencia exploratoria visible; nunca compatibilidad |
| Migración de identidad de componentes | linaje estructural, correspondencia estructural demostrada (la contención solo la valida) y fallo cerrado |
| Selección dependiente del estado heredado | lectura del estado del algoritmo, comparación con el algoritmo vivo y fallo cerrado si difieren (condición 13) |
| Supresión heredada de una segunda salida sin etiqueta | banda `UNRESOLVED`; nunca sustitución por el valor límite ni por otra salida (condición 15) |
| Huella incompleta o corte infiel (D-CU2-20-R) | caracterización en T1, corrección en T3 antes de T4; parada si exige cambiar persistencia, comportamiento heredado o identidad durable (condición 16 y §4.1) |
| Cambio de vértices en dibujo y exportaciones | conformidad declarada por prueba; DXF y LaTeX con la misma topología |
| Pruebas que dependen de la compatibilidad | inventario y conformidad declarada (§5) |
| Dependencia de `CU3` | parada y presentación (§4, condición 5) |

## 8. Fuera de alcance

- puntos estructurales, materialización de raíces tangentes y cambios del motor de
  intersecciones (`CU3`), incluida la observación F-CU2-5;
- la materialización y el dibujo de las componentes puntuales (D-CU2-19);
- la selección continua de raíces coincidentes en el corte y cualquier cambio de la regla
  heredada de la segunda salida (D-CU2-18);
- herramientas públicas nuevas y mejoras de interfaz;
- cónicas generales, intersección cónica–cónica, listas, funciones generales e intersecciones
  en modo continuo en la clase v3;
- certificación (clases 1 y 2) de la longitud de `o_1` y de `e_2`;
- invariancia de traslación de la clase 3 (`L-CU1-TRANSLATION-RESOLUTION`);
- el libro.
