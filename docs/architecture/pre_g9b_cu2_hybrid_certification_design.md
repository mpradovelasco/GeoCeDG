# PRE-G9B-CU2 — Diseño consolidado de la certificación híbrida y ampliación del modelo de construcciones certificadas

| Campo | Valor |
|---|---|
| Carácter | Diseño candidato de la preparación de `CU2` (entregables 2 y 4): certificación híbrida DA-1, componentes y su representación, regla de publicación (H01), linaje y ampliación del modelo de construcciones certificadas (clase v3). No es especificación normativa |
| Estado | `CU2 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false`; `TECHNICAL_CANDIDATE_STATE = FROZEN`; `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT` |
| Encargo | [registro de cierre de `CU1`](../validation/pre_g9b_cu1_closeout_record.md) §7 |
| Antecedentes | [propuesta E de `CU0`](pre_g9b_cu0_hybrid_certification_proposal.md) (dirección DA-1 aprobada el 2026-10-19); [enmiendas candidatas A2.1 y A2.2](pre_g9b_cu0_candidate_normative_amendments.md) |
| Autoridades que se preservan | ADR 0027 (existencia y componentes continuas válidas, con admisibilidad local independiente de la completitud global); `locus-v2-existence-components.md` v1.0; `locus-v2-certified-construction-model.md` v1.1; `locus-v2-metrics.md` v1.5 §25 (clases de garantía, aprobadas con `CU1`) |
| Documentos hermanos | [análisis del modelo del autor](pre_g9b_cu2_author_model_certification_analysis.md); [evento de la longitud orientada](pre_g9b_cu2_oriented_length_event_design.md); [consumo y representación](pre_g9b_cu2_consumption_and_representation.md); [propuesta normativa](pre_g9b_cu2_normative_proposal.md); [estrategia](pre_g9b_cu2_implementation_strategy.md) |
| Revisión | 2026-10-20, revisión documental focalizada P1–P5: §3.2 (extremos laterales), §3.4 nuevo (existencia, evaluación y representación), §4.1 (selección de raíz y resultado de caja), §4.6 nuevo (certificado algebraico exacto), §5 (pliegues y parámetros singulares) y §7 (identidades y migración) |

## 1. Objetivo

Sustituir el certificado de compatibilidad, que hoy declara `COMPLETE` una componente periódica
`[−π, π)` sin prueba (`AlgoDependentPointLocusV2.java:165-192` con
`LocusExistenceStructure2D.java:159-175`; `LocusBranch2D.java:30-38`), por
una certificación real de la existencia y la continuidad de Locus V2 que:

- separe evidencia puntual, exploratoria, local y global (DA-1);
- certifique fronteras, transiciones, costuras y discontinuidades estructurales por medios
  rigurosos, nunca por muestreo;
- certifique también las degeneraciones significativas (raíces dobles, tangencias y pliegues)
  con pruebas exactas o estructurales, nunca con tolerancias;
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
| 4 `COMPLETE` | recubrimiento certificado de todo el dominio canónico por componentes certificadas, no existencia certificada y fronteras certificadas, con un número finito de componentes: la unión de lo certificado es el dominio entero | métrica total, rutas periódicas, intersección vacía, cierre periódico | se obtiene deteniendo un muestreador, porque las cajas exploradas fueran válidas o porque no aparecieran otras componentes |

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

**Extremos laterales (P2).** Un arco se recorre con su coordenada de arco `s ∈ [0, longitud]`,
medida desde el inicio. Un parámetro canónico `t` pertenece al arco si hay una coordenada
incluida `s ≡ t − inicio (mód 2π)`. En un arco abierto de longitud `2π`, como `e_2`, los dos
extremos comparten el parámetro canónico pero son distintos: el inicio, `s = 0`, y el final,
`s = 2π`. Como solo uno de los dos está incluido, la coordenada que corresponde a `t` es única.
Cada extremo tiene su lado, su política de inclusión, su límite lateral y su fuente de frontera.
El extremo excluido no es imagen de ningún parámetro canónico: solo se alcanza como límite
lateral, con una consulta explícita. La continuidad entre los dos extremos nunca se infiere de
que coincidan sus coordenadas ni de que el conductor sea periódico
([diseño del evento](pre_g9b_cu2_oriented_length_event_design.md) §4.4).

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
| fallo de evaluación de un punto | la existencia está certificada, pero el cálculo del valor en un `t` falla (por ejemplo, una estimación métrica que agota su presupuesto) | ningún cambio de cobertura; estado de evaluación `EVALUATION_FAILED` con causa computacional en ese punto |

Una dependencia indefinida por razones de cálculo, como una métrica de clase 4 o una estimación
por encima de `τ`, nunca produce no existencia certificada.

### 3.4 Existencia, evaluación y representación (P5)

Son cuatro hechos distintos, con garantías distintas:

| Capa | Qué afirma | Quién la establece | Si falla |
|---|---|---|---|
| 1. Existencia matemática | que `F(t)` y sus límites laterales existen | el certificado | no existencia certificada o `UNRESOLVED` |
| 2. Topología certificada | continuidad, fronteras, cierre y eventos | el certificado | `UNRESOLVED` y completitud `NOT_ESTABLISHED` |
| 3. Disponibilidad numérica | un valor de `F(t)` con su garantía numérica | el evaluador, y la métrica en `w_1` | estado de evaluación `EVALUATION_FAILED` con causa computacional |
| 4. Disponibilidad de representación | una aproximación de dibujo o de exportación con fidelidad declarada | el dibujo y las exportaciones, a partir del núcleo | fidelidad parcial o fallo explícito |

Reglas:

- la capa 1 nunca se deduce de la 3: un fallo de evaluación no prueba la no existencia, y una
  evaluación válida no prueba la existencia, porque el evaluador da puntos dentro de huecos
  exactos (análisis §3.8.3);
- la capa 3 nunca se deduce de la 1: la existencia certificada no es una garantía numérica de las
  coordenadas;
- la capa 4 nunca inventa: no interpola a través de un parámetro sin evaluar, no une y no
  extrapola un límite lateral sin evaluar;
- si el evaluador da un punto donde el certificado prueba la no existencia, el punto no se
  publica como válido (estado `DEPENDENCY_UNDEFINED`, causa «no existencia certificada»,
  diagnóstico de desacuerdo); si no lo da donde el certificado prueba la existencia, el estado es
  `EVALUATION_FAILED` con causa computacional.

| Situación | Existencia | Evaluación | Dibujo y exportación |
|---|---|---|---|
| fallo puntual de `LocusLength` en `t` (`e_2`) | sin cambio | `EVALUATION_FAILED` en `t` | el tramo que necesita `t` se subdivide; si el fallo persiste hasta la resolución del parámetro, el trazo se corta allí con una marca de hueco computacional y fidelidad parcial |
| fallo del cálculo de un límite lateral | el límite existe (certificado) | `EVALUATION_FAILED` en ese extremo | el trazo termina en el último vértice evaluado, con marca de extremo no evaluado; nunca se extrapola |
| frontera certificada cuyo punto no se puede evaluar | la frontera existe | se usa la forma límite del certificado si la hay (en `D = 0`, `λ = −b′/a`, sin raíz cuadrada); si no, `EVALUATION_FAILED` | como en la fila anterior |
| presupuesto de representación agotado | sin cambio | — | engrosamiento determinista sin cruzar fronteras; si ni así cabe, fallo explícito de esa componente con diagnóstico |
| resultados parciales | sin cambio | — | se representan las componentes disponibles y se enumeran las demás con su causa |
| exportación con extremos abiertos o no evaluables | sin cambio | — | trazo abierto hasta el último vértice evaluado; el anexo DXF y los comentarios LaTeX declaran el estado de cada extremo y la exportación es incompleta |

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

- **Selección de raíz (P4).** La regla procede del algoritmo, nunca de una muestra:
  - la raíz bruta `k` es la `k`-ésima por el parámetro a lo largo de la dirección algorítmica
    `(g_y, −g_x)` de la recta (para `Ray(P, N)`, `N − P`); la salida `i` es la raíz bruta
    `permutation[i]`, con el filtro de los caminos limitados
    ([análisis](pre_g9b_cu2_author_model_certification_analysis.md) §3.4);
  - la permutación y los indicadores de inicialización son estado del algoritmo: la captura los
    lee y forman parte de la huella de la revisión;
  - en cada sincronización de revisión, el estado de la copia del corte debe ser igual al del
    algoritmo vivo; si difiere (la copia lo reinicializa por cercanía, hallazgo F-CU2-8), la
    selección queda `NOT_ESTABLISHED` con diagnóstico tipado y no se elige ninguno de los dos;
  - si el algoritmo no ha salido de la inicialización (`firstIntersection`), la regla es la
    compactación, en orden bruto, de las raíces que están en los caminos. Se certifica solo
    donde el número de raíces en los caminos es constante y está probado (por ejemplo, un rayo
    que sale del centro de la circunferencia: exactamente una); donde cambia, la salida cambia de
    raíz y ese parámetro es una frontera de selección;
  - quedan fuera de la clase el modo continuo (cercanía en cada cálculo), la selección por punto
    de referencia (`Intersect(c, l, A)`) y cualquier selección que dependa de la posición actual.

  Casos de la instrucción: un conductor que empieza sin intersección no impide capturar la regla,
  porque no hace falta una muestra válida; en una tangencia las dos raíces dan el mismo punto y
  la continuidad la prueba §4.6; el paso entre penetración y mordedura conserva la regla, con
  revisión nueva y transición de linaje; si una raíz desaparece temporalmente, la salida es
  indefinida en el hueco y es la misma regla a ambos lados; la división o unión de componentes son
  transiciones de linaje (§7); la permutación heredada se lee y nunca se reconstruye; al guardar,
  abrir, deshacer o rehacer, el estado se vuelve a leer y, si cambia, se publica una revisión con
  diagnóstico (§7). Así se respeta `certified-construction-model` §9, que excluye la proximidad
  como identidad.
- **Resultado de caja.** `VALID`, `UNIFORMLY_INVALID` con causa tipada (`DISCRIMINANT_NEGATIVE`,
  `RAY_PARAMETER_NEGATIVE`, `SEGMENT_PARAMETER_OUTSIDE`, `ZERO_DIRECTION`, `ZERO_RADIUS`,
  `DIVISOR_ZERO`) o `UNRESOLVED`. Solo `UNIFORMLY_INVALID` produce `CERTIFIED_DOES_NOT_EXIST`.
  Por intervalos solo pueden darlo los predicados de desigualdad estricta (`D < 0`, `λ < 0`,
  parámetro fuera del segmento). Los de igualdad (divisor, dirección o radio nulos en toda la
  caja) solo pueden darlo por una identidad estructural exacta, como un factor idénticamente nulo:
  una envolvente que contiene el cero no prueba que se anule en toda la caja y da `UNRESOLVED`.
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
  `UNRESOLVED`, salvo que el predicado sea de primer nivel: entonces decide el certificado
  algebraico exacto (§4.6), que es el caso del cero doble de `D` en el valor crítico de la
  mordedura.

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

**Raíces múltiples.** Ningún método de intervalos prueba que un predicado tenga una raíz de
multiplicidad dada: un cero doble desaparece con perturbaciones arbitrariamente pequeñas, y los
métodos verificados solo prueban que un problema perturbado tiene una raíz múltiple (análisis
§3.8.2). Para predicados que no son de primer nivel, la técnica general es la del punto
estacionario: Krawczyk sobre `P′`, con una envolvente de `P″` que excluye el cero, prueba la
existencia y unicidad del extremo `t*`, y el signo de `P(t*)` decide si su envolvente lo excluye;
si no lo excluye, la caja queda `UNRESOLVED`. Para predicados de primer nivel decide §4.6.

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

### 4.6 Certificado algebraico exacto de predicados de primer nivel (P1)

**Aplicabilidad.** Un predicado es de primer nivel cuando sus entradas son constantes capturadas
y puntos que dependen del conductor por operaciones racionales (el propio conductor,
traslaciones, puntos medios, intersecciones de rectas), sin raíces cuadradas intermedias, y el
conductor recorre una circunferencia o un segmento. Lo son el discriminante `D` de `{Q, R}` y el
de `H_1` en el modelo del autor ([análisis](pre_g9b_cu2_author_model_certification_analysis.md)
§3.8 y §4).

**Método.**

1. Las constantes capturadas son racionales exactos: sus valores binary64.
2. Con `u = tan(t/2)`, `cos t = (1 − u²)/(1 + u²)` y `sin t = 2u/(1 + u²)`. Multiplicado por la
   potencia adecuada de `1 + u²`, el predicado es un polinomio `p(u)` con coeficientes racionales
   exactos: de grado 4 para `D` con un conductor circular. `t = ±π` (`u = ∞`) se evalúa aparte,
   también exactamente.
3. La parte libre de cuadrados (división por el máximo común divisor con la derivada) y la
   sucesión de Sturm dan el número de raíces reales distintas, intervalos racionales que aíslan
   cada una, la multiplicidad de cada raíz y el signo de `p` entre raíces. Todo en aritmética
   entera exacta.
4. Cada intervalo se estrecha por bisección exacta hasta la anchura `δ` en `t`; la envolvente en
   `t` se obtiene con `2·atan` de redondeo exterior.

**Fronteras que produce.**

| Raíz | Signo a ambos lados | Resultado |
|---|---|---|
| multiplicidad impar | distinto | transición certificada con extremo cerrado; es una frontera analítica (autoridad 3 de `existence-components` §7) |
| multiplicidad par | positivo y positivo | parámetro singular interior: la existencia continúa y la selección tiene una esquina (multiplicidad 2) o es suave (multiplicidad ≥ 4) |
| multiplicidad par | negativo y negativo | punto aislado de existencia: componente degenerada `[t*, t*]` |

En un parámetro singular de multiplicidad 2 la continuidad es estructural: `√D` de una función
continua no negativa es continua, y `a = |N − P|² ≥ a_mín > 0` se certifica aparte.

**API candidata** (núcleo compartido; nombres provisionales):

| Tipo | Responsabilidad |
|---|---|
| `ExactRational2D` | racional exacto sobre `BigInteger`, construido sin redondeo desde un binary64 |
| `ExactPolynomial1D` | polinomio sobre los racionales: suma, producto, derivada, división, máximo común divisor y evaluación exacta |
| `RealRootStructure1D` | raíces reales: intervalos racionales aislantes, multiplicidades, signos entre raíces y valor en el infinito |
| `FirstLevelDriverPredicate2D` | construye `p(u)` desde un paso capturado de primer nivel y un conductor circular o rectilíneo; vacío si el predicado no es de primer nivel |
| `DegenerationEvidence2D` | por raíz: tipo (transición, singular interior, punto aislado), envolvente en `t`, multiplicidad, signos laterales, versión del método y presupuesto consumido |

**Evidencia registrada:** versión del método (`exact-univariate-sturm/v1`), huella de los
coeficientes, grado, bits máximos, anchura `δ`, número de bisecciones y resultado. Es
determinista: solo depende de las constantes capturadas y de la versión.

**Coste medido** (análisis §8): de 2 a 53 ms por configuración del modelo, con coeficientes de
hasta 217 bits; hasta 0,3 s en casos sintéticos con raíces separadas `2⁻¹⁰⁰`. El presupuesto
limita los bits de los coeficientes y el número de bisecciones, con valores versionados
(D-CU2-5); agotarlo da `UNRESOLVED` tipado.

**Pruebas:** filas V-CRIT de la [matriz de validación](../validation/pre_g9b_cu2_validation_matrix.md).

**Límites.** Los predicados posteriores a una raíz cuadrada, como los de `G_1` o los rayos que
dependen de `Q`, no son de primer nivel: se certifican por cajas, y su eliminación algebraica
queda fuera de `CU2`. Una tangencia que la construcción calcula con redondeo se certifica como su
configuración redondeada (análisis §3.8.3).

## 5. Puntos estacionarios, pliegues y parámetros singulares (P3)

Para una curva plana, `F′(t) = 0` son dos ecuaciones con una incógnita: genéricamente no tienen
solución, y una envolvente de `F′` que contiene el cero no prueba que exista un punto
estacionario. Un pliegue solo se certifica con cinco pruebas separadas:

1. **Existencia** por una simetría estructural exacta: una involución `σ` del dominio del
   conductor con `F ∘ σ = F`. En sus puntos fijos `t₀`, si `F` es derivable,
   `F′(t₀) = −F′(t₀) = 0`. La simetría se verifica sobre el programa capturado, nunca se supone:
   - el centro de la circunferencia del conductor está en una recta `L` independiente del
     conductor (igualdad racional exacta de las constantes capturadas), y `σ` es la simetría
     respecto de `L`;
   - cada paso del corte es equivariante (pasos de planta) o invariante (pasos de alzado que
     solo dependen de magnitudes que `σ` conserva, como `x` si `L` es horizontal, o distancias a
     puntos de `L`), según una tabla versionada de operaciones;
   - las selecciones capturadas son invariantes (§4.1).
2. **Aislamiento y unicidad:** en una caja `T₀ ∋ t₀`, la envolvente de una componente de `F″`
   excluye el cero; esa componente de `F′` es estrictamente monótona y `t₀` es el único punto
   estacionario de `T₀`.
3. **No degeneración:** `F″(t₀) ≠ 0`, con la misma envolvente.
4. **Continuidad:** `t₀` es interior a una componente certificada.
5. **Naturaleza:** con `F ∘ σ = F`, las dos mitades recorren el mismo arco y las tangentes
   laterales son opuestas, `±F″(t₀)/|F″(t₀)|`: es un pliegue.

Los chorros de segundo orden de la clase v3 sirven para 2 y 3. Sin 1, un giro cerrado es solo un
giro: se refina hasta la resolución del parámetro, con diagnóstico, y no se exime (decisión
D-CU2-6). `e_1` cumple las cinco pruebas en `t = 0` y `t = π`
([análisis](pre_g9b_cu2_author_model_certification_analysis.md) §4.3).

**Parámetros singulares.** Un cero de multiplicidad 2 sin cambio de signo de un predicado
(§4.6) es un parámetro singular: la componente continúa, la selección cambia de rama analítica y
la curva tiene una esquina (análisis §3.8.1). El certificado lo publica con su tipo, y el dibujo
lo trata como un vértice exacto (consumo §5.1).

`CU3` reutilizará estos chorros para los contactos tangentes; `CU2` no los materializa.

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

## 7. Linaje e identidad (P4, E-3)

Se distinguen tres identidades:

| Identidad | Qué identifica | Autoridad | Cambia cuando |
|---|---|---|---|
| durable del resultado | el objeto de construcción (`Q`, `N_1`): etiqueta, identidad persistente e índice de salida | DAG y persistencia; `CU2` no la cambia | se redefine el objeto |
| semántica de la selección | la regla que da la salida a partir de las entradas: algoritmo y versión, raíz bruta y estado de permutación (§4.1) | el algoritmo; es entrada de la revisión | cambia el estado del algoritmo: revisión nueva con diagnóstico |
| linaje de la componente | la componente dentro de la revisión | las fuentes estructurales de sus fronteras | aparecen, desaparecen, se dividen o se unen componentes |

Hoy la identidad de una componente incluye los bits exactos de sus extremos
(`LocusComponentLineage2D.java:21-32`): cambia en cuanto una frontera se mueve un poco. Las
transiciones de linaje (`LocusLineage2D.java:16-117`) existen, pero ningún productor emite otra
que `unchanged()`. Propuesta (decisión E-3 revisada):

- **Linaje estructural:** la clave de una componente es la clave de rama más la lista ordenada
  de las fuentes de sus fronteras. Cada fuente es `(identidad durable de la operación, tipo de
  predicado, lado)`, `(fuente del evento, lado)` o `EXTREMO_DE_DOMINIO`. La clave no cambia
  cuando las fronteras se desplazan sin cambiar de fuente.
- **Transiciones:** al pasar de penetración a mordedura, la componente periódica cerrada se
  convierte en un arco con dos fronteras del predicado `D`. Es una transición registrada
  (`SPLIT` del círculo en arco) con su relación explícita. Ninguna revisión, recálculo ni cambio
  temporal de existencia reconstruye asociaciones por etiqueta, índice, coordenadas o
  proximidad.
- **Selectores persistidos:** por ejemplo el `text2` de `N_1`, que cita la componente `[−π, π)`
  de `o_1` con sus extremos. La contención única de un parámetro en una componente solo
  **valida** una correspondencia ya demostrada: nunca la crea ni reasocia una selección antigua
  con otra solución. Hay correspondencia demostrada cuando:
  - la componente citada era todo el dominio declarado de la rama y el certificado actual prueba
    una sola componente periódica cerrada igual al dominio: igualdad de la descomposición (el caso
    de `N_1` sobre `o_1`); o
  - el certificado registra una transición de linaje explícita desde la componente citada, con la
    misma regla de selección.

  Con correspondencia demostrada y contención única del parámetro, el selector se resuelve. En
  otro caso (selector antiguo sin fuente estructural frente a una descomposición distinta,
  ambigüedad o varias componentes candidatas) queda sin resolver, y el punto, indefinido con una
  causa tipada.
- **Sin cambio de formato:** la resolución se hace al cargar y en cada revisión. El selector
  persistido no se reescribe; `text2`, además, es un objeto del usuario. Reescribirlo sería un
  cambio de contrato persistente que necesita su propia autorización.

## 8. Determinismo, presupuestos y coste

- Las particiones son diádicas sobre el dominio y dependen solo de él y de la versión de la
  política.
- Los presupuestos son explícitos (cajas por rama y revisión, profundidad); su agotamiento
  produce `UNRESOLVED` tipado.
- El certificado se calcula una vez por revisión y lo reutilizan todos los consumidores (A1.1-R
  de `CU1`).
- Contadores públicos de trabajo de certificación para las puertas de rendimiento, con la misma
  política de `CU1`: los contadores deciden y los tiempos informan.
- El certificado algebraico exacto (§4.6) cuesta de 2 a 53 ms por predicado en el modelo del
  autor y se calcula una vez por revisión, con su propio presupuesto de bits y bisecciones.
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

1. certificador híbrido tras un contrato versionado (`locus-existence/hybrid/v1`), con el
   certificado algebraico exacto (§4.6) y el de pliegues (§5);
2. certificación de la segunda familia (`e_1`, `o_1`) y de la primera (`o`, `v`, `w`, `a_1`),
   incluido el valor crítico;
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
| Un predicado necesario no es de primer nivel y tiene una raíz múltiple | cajas y punto estacionario; `UNRESOLVED` si no deciden; parar si ocurre en el modelo del autor |
| La copia del corte no reproduce el estado de selección del algoritmo vivo | selección `NOT_ESTABLISHED` con diagnóstico; parar si ocurre en el modelo del autor |
| Coste del certificado exacto con coeficientes grandes | presupuesto de bits y bisecciones; `UNRESOLVED` tipado |
