# PRE-G9B-CU2 — Diseño consolidado de la certificación híbrida y ampliación del modelo de construcciones certificadas

| Campo | Valor |
|---|---|
| Carácter | Diseño candidato de la preparación de `CU2` (entregables 2 y 4): certificación híbrida DA-1, componentes y su representación, regla de publicación (H01), linaje y ampliación del modelo de construcciones certificadas (clase v3). No es especificación normativa |
| Estado | `CU2 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false`; `TECHNICAL_CANDIDATE_STATE = FROZEN`; `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT` |
| Encargo | [registro de cierre de `CU1`](../validation/pre_g9b_cu1_closeout_record.md) §7 |
| Antecedentes | [propuesta E de `CU0`](pre_g9b_cu0_hybrid_certification_proposal.md) (dirección DA-1 aprobada el 2026-10-19); [enmiendas candidatas A2.1 y A2.2](pre_g9b_cu0_candidate_normative_amendments.md) |
| Autoridades que se preservan | ADR 0027 (existencia y componentes continuas válidas, con admisibilidad local independiente de la completitud global); `locus-v2-existence-components.md` v1.0; `locus-v2-certified-construction-model.md` v1.1; `locus-v2-metrics.md` v1.5 §25 (clases de garantía, aprobadas con `CU1`) |
| Documentos hermanos | [análisis del modelo del autor](pre_g9b_cu2_author_model_certification_analysis.md); [evento de la longitud orientada](pre_g9b_cu2_oriented_length_event_design.md); [consumo y representación](pre_g9b_cu2_consumption_and_representation.md); [propuesta normativa](pre_g9b_cu2_normative_proposal.md); [estrategia](pre_g9b_cu2_implementation_strategy.md) |

## 1. Objetivo

Sustituir el certificado de compatibilidad, que hoy declara `COMPLETE` una componente periódica
`[−π, π)` sin prueba (`AlgoDependentPointLocusV2.java:165-192` con
`LocusExistenceStructure2D.java:159-175`; `LocusBranch2D.java:30-38`), por
una certificación real de la existencia y la continuidad de Locus V2 que:

- separe evidencia puntual, exploratoria, local y global (DA-1);
- certifique fronteras, transiciones, costuras y discontinuidades estructurales por medios
  rigurosos, nunca por muestreo;
- alcance a los siete loci del modelo del autor sin sobrevalorar la evidencia;
- falle cerrado (`NOT_ESTABLISHED`, `UNRESOLVED`) cuando no pueda probar.

Los defectos que resuelve son B07, H01 y B10.2, en su parte de existencia. La representación
(B06 y el dibujo de las componentes) está en el
[diseño de consumo y representación](pre_g9b_cu2_consumption_and_representation.md).

## 2. Cuatro niveles de evidencia

| Nivel | Contenido | Puede alimentar | Nunca |
|---|---|---|---|
| 1 `POINT_VALID` | una evaluación válida de `F(t)` | puntos exactos y residuos | implica nada sobre un intervalo |
| 2 `EXPLORATORY` | recorridos válidos muestreados, transiciones acotadas por bisección sobre la validez, saltos sospechosos; método, política y trabajo registrados | diagnóstico, inspector, siembra del certificador; dibujo con marca propia solo en bandas `UNRESOLVED` (decisión E-1) | se promueve a componente, completitud o identidad; se exporta |
| 3 `LOCAL_CERTIFIED` | componente certificada en un arco o intervalo (§3), maximal o subintervalo local con bandas `UNRESOLVED` explícitas | todos los consumidores locales (§11 de `existence-components`) | se declara completa |
| 4 `COMPLETE` | recubrimiento certificado de todo el dominio canónico por componentes certificadas, no existencia certificada y fronteras certificadas, con un número finito de componentes | métrica total, rutas periódicas, intersección vacía, cierre periódico | se obtiene deteniendo un muestreador |

La evidencia exploratoria es una estructura hermana de `LocusExistenceStructure2D`, tipada y
derivada. Es determinista por revisión y versión de política, no se persiste y no forma parte de
la huella de revisión ni de la cobertura (`existence-components` §8): no avanza revisiones.

La independencia entre admisibilidad local y completitud global (ADR 0027, decisión 4) se
conserva tal cual. Un consumidor local acepta el nivel 3; uno global exige el 4.

## 3. Componente certificada

### 3.1 Requisitos

Un arco o intervalo `I` del dominio del conductor es una componente certificada de una rama
cuando:

1. **Existencia:** todos los predicados de validez del corte son ciertos en todo `I`, probado
   por cajas válidas (§4.3).
2. **Selección continua:** cada selección capturada (rama de raíz, índice de salida) es la misma
   regla geométrica en todo `I` y no se agota en su interior.
3. **Continuidad de `F`:** `F` es continua en `I`. Para un programa de operaciones continuas se
   sigue de 1 y 2. Una discontinuidad estructural con evaluaciones válidas a ambos lados, como el
   evento de envoltura (§4.5), es una **frontera**, nunca un punto interior. Este requisito es
   nuevo: `existence-components` §4 exige existencia y continuación de la selección, pero no la
   continuidad de la propia función.
4. **Fronteras:** cada extremo de `I` es uno de:
   - extremo del dominio declarado;
   - transición certificada (raíz simple de un predicado de validez);
   - evento estructural;
   - borde de una banda `UNRESOLVED`, y entonces la componente no es maximal.
5. **Inclusión de extremos:** la decide la semántica del predicado en la frontera (§4.3), no la
   evaluación puntual.

### 3.2 Representación en arcos para dominios periódicos

`existence-components` §3 ordena intervalos dentro de `[−π, π)`, de modo que no puede expresar
una componente que cruza la costura (`o`, `v`, `w` y `a_1` en mordedura; `e_2`). Se propone:

- en un dominio periódico, una componente es un **arco** `(inicio, longitud)`, con inicio
  canónico en `[−π, π)` y `0 < longitud ≤ 2π`, más la inclusión de cada extremo;
- un arco de longitud `2π` con continuidad certificada en su propio punto de cierre es una
  **componente periódica cerrada**; si no hay continuidad en ese punto (como en `e_2`), es una
  componente abierta de longitud `2π`;
- la pertenencia de un parámetro se decide sobre el círculo, con la dirección canónica módulo
  `2π`;
- el orden de las componentes es el del inicio canónico y no es identidad;
- en dominios no periódicos los intervalos no cambian.

**Compatibilidad:** la vista heredada `getValidDomainComponents()` solo es válida con la
descomposición completa (ADR 0027) y partiría un arco que cruza la costura en dos intervalos.
Todos los consumidores que la usan pasan a la estructura de existencia, y la vista queda solo
como adaptador documentado (consumo §2).

### 3.3 Indefinición computacional frente a no existencia

Se distinguen tres resultados, que hoy pueden confundirse (`existence-components` §3 cuenta
`DEPENDENCY_UNDEFINED` como no existencia):

| Resultado | Significado | Cobertura |
|---|---|---|
| no existencia certificada | un predicado de validez es falso con certeza (caja `UNIFORMLY_INVALID`), o una dependencia tiene no existencia certificada | `CERTIFIED_DOES_NOT_EXIST` con causa tipada |
| indecidible | el cálculo no puede decidir: presupuesto agotado, caso degenerado, operación fuera de la clase | `UNRESOLVED` con causa tipada |
| fallo de evaluación de un punto | la existencia está certificada, pero el cálculo del valor en un `t` falla (por ejemplo, una estimación métrica que agota su presupuesto) | ningún cambio de cobertura; estado de evaluación `NOT_ESTABLISHED` en ese punto |

Una dependencia indefinida por razones de cálculo, como una métrica de clase 4 o una estimación
por encima de `τ`, nunca produce no existencia certificada.

## 4. Fuentes de certificación

El orden de autoridad de las fronteras de `existence-components` §7 se conserva:

1. eventos estructurales y del proveedor;
2. certificados de intervalos de raíz;
3. fronteras analíticas;
4. predicados de intervalos;
5. aislamiento acotado.

Se añaden contenidos a 1 y a 4.

### 4.1 Programa de construcción de clase v3 (ampliación del modelo)

La clase v3 tiene identificador de versión propio; los programas v1 y v2 no se reetiquetan.
Capacidades nuevas, estrictamente las que necesitan los loci del modelo del autor
([análisis](pre_g9b_cu2_author_model_certification_analysis.md) §3–§5):

| Capacidad | Forma | Predicado de validez | Necesaria para |
|---|---|---|---|
| Rayo | recta más parámetro `λ` | `λ ≥ 0` | `t`, `f_1`, `j_1`, `l_1`, `m_1`, `q_1`, `n_1`, `s_1` |
| Segmento | recta más parámetro | `0 ≤ λ ≤ 1` | (general; ninguno del modelo es dependiente del conductor) |
| Circunferencia por centro y radio constante | centro dependiente, radio fijo | ninguno | general |
| Circunferencia por centro y punto | `AlgoCircleTwoPoints` | radio `> 0` | `d_1` |
| Recta–circunferencia con rama | `AlgoIntersectLineConic` (circunferencia) e `AlgoIntersectSingle` indexado sobre ella | `D ≥ 0`; para el rayo, `λ ≥ 0` en la raíz seleccionada | `{Q, R}`, `H_1`, `G_1`, `J_K` |
| Algoritmos de varias salidas | un paso con salidas indexadas por rama | los de cada salida | `{Q, R}` |
| Vectores | escalar × vector, producto escalar, vector unitario de un vector definido, punto + vector | `‖v‖ > 0` para el unitario | macros del autor |
| Expansión de macros | una macro se expande en sus pasos internos si todos están en la clase | los de sus pasos | `pointJump`, `translationCoor` |
| Entrada métrica estructural | un nodo `LocusLength` del corte aporta su certificado estructural (§4.5), nunca su valor | definición de la ruta | `w_1` → `d_2` en `e_2` |

Reglas:

- **Selección de raíz.** La salida de una intersección recta–circunferencia se identifica por
  una regla geométrica: raíz de parámetro de recta menor o mayor. Se captura una vez por revisión
  a partir de la permutación de salida del modo no continuo y se comprueba constante en cada
  caja. El modo continuo, con historia, queda fuera de la clase. Así se respeta
  `certified-construction-model` §9, que excluye la proximidad como identidad: la regla capturada
  es geométrica y la caja certifica que no cambia.
- **Resultado de caja.** `VALID`, `UNIFORMLY_INVALID` con causa tipada (`DISCRIMINANT_NEGATIVE`,
  `RAY_PARAMETER_NEGATIVE`, `SEGMENT_PARAMETER_OUTSIDE`, `ZERO_DIRECTION`, `ZERO_RADIUS`,
  `DIVISOR_ZERO`) o `UNRESOLVED`. Solo `UNIFORMLY_INVALID` produce `CERTIFIED_DOES_NOT_EXIST`.
  Hoy el modelo no distingue «indeciso» de «uniformemente inválido», y por eso
  `CERTIFIED_DOES_NOT_EXIST` y las componentes maximales son inalcanzables. Hoy un predicado de
  GeoGebra que no es uniforme en una caja lanza `ArithmeticException`
  (`ConstructionIntervalModel2D.java:356-363`), y el modelo de intervalos nunca se usa para
  certificar la existencia de un locus dependiente. La clase v3 convierte esa excepción en los
  tres resultados de caja.
- **Límite de pasos.** El límite actual de 64 pasos (`certified-construction-model` §3) no
  admite el corte de `e_2`: 52 objetos más la expansión de dos macros. La clase v3 lo sustituye
  por un límite versionado de pasos dependientes del conductor (decisión D-CU2-5).
- **Envolventes.** Se reutilizan los modelos de intervalos existentes, con valor y primera
  derivada y redondeo exterior (`certified-construction-model` §6; fachada de `CU1`), y se
  extienden a las capacidades nuevas.
- **Fuera de la clase** siguen: intersección cónica–cónica, cónicas generales, listas, funciones
  generales, raíces seleccionadas de Locus V2, expresiones afines no directas
  (`L-CU1-AFFINE-FORMS`, no necesarias para el modelo) e intersecciones en modo continuo. Un corte
  con un paso fuera de la clase no se certifica: queda `NOT_ESTABLISHED` con evidencia
  exploratoria visible.

### 4.2 Totalidad estructural (autoridad 1)

Una operación es **estructuralmente total** sobre su dominio de entrada si está definida y es
continua para toda entrada válida. Tabla versionada inicial:

- traslación por un vector;
- vector unitario de una recta definida;
- paralela y perpendicular por un punto;
- intersección de una recta con su perpendicular;
- punto medio;
- homotecia y giro de parámetros constantes;
- punto + escalar·vector unitario de recta definida;
- proyección ortogonal sobre una recta definida.

Las macros del autor `pointJump` y `translationCoor` son totales una vez expandidas.

Un corte cuyas operaciones dependientes del conductor son todas totales, con entradas
dependientes certificadas en un arco, queda certificado en ese arco sin cálculo numérico de
cajas.

### 4.3 Transiciones certificadas (autoridad 4)

Cada operación condicional del programa expone su predicado de validez `P(t)`: discriminante,
parámetro de rayo o segmento, norma, divisor o radio. Con los modelos de intervalos se obtienen
envolventes de `P` y de `P′` sobre cajas del dominio:

- `P` estrictamente positivo en la caja: caja válida para ese predicado;
- `P` estrictamente negativo: caja `UNIFORMLY_INVALID`;
- en otro caso, si `P` cambia de signo entre los extremos y la envolvente de `P′` excluye el
  cero, la caja contiene una **raíz simple única** (Newton de intervalos o Krawczyk escalar); se
  estrecha hasta un corchete de anchura `δ`, constante versionada (D-CU2-5);
- si `P` y `P′` contienen el cero en una caja de resolución mínima, se forma una banda
  `UNRESOLVED`. Es el caso degenerado del valor crítico de la mordedura, un cero doble de `D`.

La inclusión del extremo sigue el predicado:

| Predicado en la raíz | Extremo |
|---|---|
| `D = 0` (recta–circunferencia) | cerrado: la raíz doble existe y las dos salidas coinciden |
| `λ = 0` (origen del rayo) | cerrado |
| `0 ≤ λ ≤ 1` en un extremo | cerrado |
| divisor `= 0`, norma `= 0`, radio `= 0` | abierto |

`F` puede tener una singularidad de raíz cuadrada en la frontera aunque `P` sea regular; por eso
la frontera se certifica sobre `P` y nunca sobre la evaluación de `F`. La validez puntual de la
evaluación no se usa como frontera: excede la raíz exacta entre 4·10⁻⁸ y 4,3·10⁻⁶ en el modelo
del autor (hallazgo F-CU2-1).

### 4.4 Periodicidad estructural y autoridad única de cierre

**Premisa de función de configuración.** Un corte tiene esta propiedad cuando:

- su conductor es un punto de una circunferencia (o de otro proveedor periódico) que es función
  continua del punto del círculo;
- cada operación del corte es función de sus entradas actuales, sin historia: modo no continuo,
  sin aleatoriedad ni tiempo;
- cada selección capturada es una regla geométrica constante en cada componente.

Entonces `F` es una función bien definida sobre el círculo `S¹`. Las cajas se toman sobre el
círculo, de modo que la partición envuelve la costura, y un arco que contiene la costura se
certifica como cualquier otro. `certified-construction-model` §7 («periodo siempre 0; no se
reclama identificación periódica») se modifica: un programa de clase v3 con la premisa de
función de configuración puede reclamar la identificación periódica del círculo de su conductor.
El motivo que da §7, que el dominio canónico en binary64 no es exactamente cerrado bajo `cos` y
`sin` reales (la costura se separa unos `2,45·10⁻¹⁶ r`), afecta a la evaluación en coma
flotante y no a la función semántica real. Las cajas que envuelven la costura evalúan el punto
conductor con envolventes de redondeo exterior de las funciones reales, en las que `t` y
`t + 2π` dan el mismo punto.

**Cierre.** Una componente es periódica cerrada si y solo si es un arco de longitud `2π`
certificado sin frontera, con continuidad incluida en el punto de cierre. Hoy cinco predicados
independientes deciden el cierre: dibujo, exportación, rutas métricas, costura de intersección y
resolución de identidad (`LocusRenderCache2D.java:137-147`,
`SemanticCurveExportAdapter2D.java:471-480`, `PolicyV2LocusMetricCapability2D.java:770-781`,
`LocusMetricRouteResolver2D.java:102-111`, `LocusPointInteractionResolver2D.java:1158-1162`). Todos pasan a una
**autoridad única** de cierre derivada del certificado. La periodicidad del proveedor nunca
basta: hoy se copia `PERIODIC` del descriptor (`AlgoDependentPointLocusV2.java:134-137`), y eso
desaparece.

### 4.5 Certificado estructural de la función métrica

Un nodo `LocusLength(L, A, B)` del corte aporta al certificado del dependiente solo hechos
estructurales:

- su dominio de definición, que es la preimagen del conjunto donde la ruta existe;
- su continuidad, salvo en los eventos;
- sus eventos de envoltura;
- la positividad certificada del salto.

Nunca aporta su valor. El detalle está en el
[diseño del evento de la longitud orientada](pre_g9b_cu2_oriented_length_event_design.md) §4–§5.

Con esto se cumple el requisito de la instrucción de cierre de `CU1`: una longitud `ESTIMATED`
no se convierte en evidencia rigurosa. Lo certificado es la topología del dependiente; sus
coordenadas siguen siendo estimaciones bajo el contrato de estimación (D-CU1-2).

## 5. Chorros de segundo orden

El modelo de intervalos tiene hoy valor y primera derivada. La segunda derivada se necesita para:

- **certificar pliegues:** `F′` contiene el cero y `F″` lo excluye en una caja, de modo que hay
  un punto estacionario aislado. `e_1` tiene dos, en `t = 0` y `t = ±π`. Así la teselación puede
  eximir el criterio de ángulo en un pliegue certificado sin bajar la calidad en el resto
  (consumo §5.1);
- más adelante, `CU3` (contactos tangentes), que no se adelanta.

Propuesta: incluir en `CU2` solo la segunda derivada de las operaciones de la clase v3, y solo
para pliegues. Sin ella, un giro alto no certificado se refina hasta la resolución del parámetro
y se marca, sin eximirlo (decisión D-CU2-6).

## 6. Regla de publicación (H01)

Hoy la publicación exige que el punto trazado esté definido en la posición actual del conductor
(`AlgoDependentPointLocusV2.java:114-130`, entradas en 414-427; `AlgoLocusV2.java:89-91`). Por eso el locus
desaparece cuando el conductor cae en un hueco, aunque el locus exista en un arco. Propuesta:

1. Un Locus V2 se publica si y solo si sus **entradas de definición** están definidas: las
   entradas que no son el conductor y el subgrafo que no depende de él. El estado del punto
   trazado en la posición actual del conductor no interviene.
2. La existencia sobre el dominio la declara solo el certificado.
3. Estados:

   | Definición | Certificado | Estado del locus |
   |---|---|---|
   | indefinida | — | `UNDEFINED` |
   | definida | recubrimiento certificado vacío | publicado, válido, `COMPLETE`, cero componentes (decisión D-CU2-8) |
   | definida | sin certificar | publicado, `NOT_ESTABLISHED`, con las componentes locales que haya |

4. El punto trazado puede estar indefinido en la posición actual. Eso afecta al propio punto y a
   los consumidores del punto actual, nunca al locus.
5. Un recálculo que no publica descarta la definición anterior; ningún consumidor ve geometría
   obsoleta (hallazgo F-CU2-3).
6. **Coherencia con A1.1-R de `CU1`:** la publicación no depende del conductor, de modo que mover
   solo el conductor no publica revisión.
7. Las entradas que deciden la publicación se definen aparte de la proyección de dependencias
   durables del ADR 0029, que no cambia ni migra. `public-surface` §(347-350) se acota: un
   componente que desaparece por un cambio de definición deja indefinido el punto que dependía de
   él, pero no la mera posición actual del conductor.

## 7. Linaje e identidad

Hoy la identidad de una componente incluye los bits exactos de sus extremos
(`LocusComponentLineage2D.java:21-32`): cambia en cuanto una frontera se mueve un poco. Las
transiciones de linaje (`LocusLineage2D.java:16-117`) existen, pero ningún productor emite otra
que `unchanged()`.
Propuesta (decisión E-3):

- **Linaje estructural:** la clave de una componente es la clave de rama más la lista ordenada
  de las fuentes de sus fronteras. Cada fuente es `(identidad durable de la operación, tipo de
  predicado, lado)`, `(fuente del evento, lado)` o `EXTREMO_DE_DOMINIO`. La clave no cambia
  cuando las fronteras se desplazan sin cambiar de fuente.
- **Transiciones:** al pasar de penetración a mordedura, la componente periódica cerrada se
  convierte en un arco con dos fronteras del predicado `D`. Es una transición registrada
  (`SPLIT` del círculo en arco) con su relación explícita, sin reasociación por geometría.
- **Selectores persistidos:** por ejemplo el `text2` de `N_1`, que cita la componente `[−π, π)`
  de `o_1` con sus extremos. Al cargar se resuelven por **contención única** en una componente
  certificada de la revisión actual y se reescriben de forma perezosa (estilo ADR 0029). Si no
  hay contención única, el selector queda sin resolver y el punto queda indefinido con causa
  tipada, nunca reasociado en silencio.

## 8. Determinismo, presupuestos y coste

- Las particiones son diádicas sobre el dominio y dependen solo de él y de la versión de la
  política.
- Los presupuestos son explícitos (cajas por rama y revisión, profundidad); su agotamiento
  produce `UNRESOLVED` tipado.
- El certificado se calcula una vez por revisión y lo reutilizan todos los consumidores (A1.1-R
  de `CU1`).
- Contadores públicos de trabajo de certificación para las puertas de rendimiento, con la misma
  política de `CU1`: los contadores deciden y los tiempos informan.
- El certificador de 512 celdas fijas de SplineV2 (`SelectedRootContinuousDomainCertifier2D`)
  queda sustituido por el certificador de cajas sobre su modelo de intervalos, o se conserva como
  caso particular (decisión D-CU2-9). Hoy:
  - elige el tramo monótono según la raíz actual (líneas 99-115), así que su revisión y sus
    componentes siguen al conductor, contra A1.1-R;
  - nunca certifica la última celda de un dominio semiabierto (171-177);
  - su resultado es siempre `NOT_ESTABLISHED`.

## 9. Retirada del certificado de compatibilidad

Hoy el certificado de compatibilidad sostiene la cadena `o_1 → LocusLength → d_2 → e_2`, porque
la ocurrencia generadora exige `COMPLETE`. Orden seguro:

1. certificador híbrido tras un contrato versionado (`locus-existence/hybrid/v1`);
2. certificación de la segunda familia (`e_1`, `o_1`) y de la primera (`o`, `v`, `w`, `a_1`);
3. evento estructural de la métrica;
4. certificación de `e_2`;
5. migración de los consumidores a la estructura de existencia y a la autoridad única de cierre;
6. retirada de `completeCompatibility`;
7. inversión de las fijaciones B07, H01, B10.2 y B06.

Un locus que no se pueda certificar queda `NOT_ESTABLISHED`, con sus componentes locales o sin
ellas: falla cerrado y no vuelve a la compatibilidad.

## 10. Relación con `CU3`

`CU2` entrega a `CU3` la existencia, la continuidad y las fronteras certificadas de los extremos
donde se producen los contactos. `CU2` no implementa puntos estructurales, materialización de
raíces tangentes ni cambios del motor de intersecciones. La precisión
`OBS-CU0-CU3-TANGENT-EXISTENCE` sigue intacta. El hallazgo F-CU2-5 (la raíz de la costura de
`e_1 ∩ c_1` es una tangencia) se registra para `CU3`.

## 11. Riesgos y condiciones de parada

| Riesgo | Tratamiento |
|---|---|
| La clase v3 no alcanza una construcción | fallo cerrado, con evidencia exploratoria visible; nunca compatibilidad |
| El coste de las cajas cerca de fronteras casi degeneradas | presupuesto y `UNRESOLVED` tipado; banco antes y después |
| Migración de identidad | linaje estructural y migración explícita; fallo cerrado ante ambigüedad |
| Pruebas existentes que dependen de la compatibilidad | conformidad declarada, nunca debilitamiento |
| Una regla de `CU2` contradice el contrato métrico de `CU1` | parar y presentar la dependencia |
| La certificación de `e_2` exigiera el valor de la longitud | parar: se viola la instrucción de cierre de `CU1` |
