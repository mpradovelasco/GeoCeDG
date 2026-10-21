# PRE-G9B-CU2 — Diseño consolidado de la certificación híbrida y ampliación del modelo de construcciones certificadas

| Campo | Valor |
|---|---|
| Carácter | Diseño candidato de la preparación de `CU2` (entregables 2 y 4): certificación híbrida DA-1, componentes y su representación, regla de publicación (H01), linaje y ampliación del modelo de construcciones certificadas (clase v3). No es especificación normativa |
| Estado | `CU2 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false`; `TECHNICAL_CANDIDATE_STATE = FROZEN`; `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT` |
| Encargo | [registro de cierre de `CU1`](../validation/pre_g9b_cu1_closeout_record.md) §7 |
| Antecedentes | [propuesta E de `CU0`](pre_g9b_cu0_hybrid_certification_proposal.md) (dirección DA-1 aprobada el 2026-10-19); [enmiendas candidatas A2.1 y A2.2](pre_g9b_cu0_candidate_normative_amendments.md) |
| Autoridades que se preservan | ADR 0027 (existencia y componentes continuas válidas, con admisibilidad local independiente de la completitud global); `locus-v2-existence-components.md` v1.0; `locus-v2-certified-construction-model.md` v1.1; `locus-v2-metrics.md` v1.5 §25 (clases de garantía, aprobadas con `CU1`) |
| Documentos hermanos | [análisis del modelo del autor](pre_g9b_cu2_author_model_certification_analysis.md); [evento de la longitud orientada](pre_g9b_cu2_oriented_length_event_design.md); [consumo y representación](pre_g9b_cu2_consumption_and_representation.md); [propuesta normativa](pre_g9b_cu2_normative_proposal.md); [estrategia](pre_g9b_cu2_implementation_strategy.md) |
| Revisión | 2026-10-20, revisión documental focalizada P1–P5: §3.2 (extremos laterales), §3.4 nuevo (existencia, evaluación y representación), §4.1 (selección de raíz y resultado de caja), §4.6 nuevo (certificado algebraico exacto), §5 (pliegues y parámetros singulares) y §7 (identidades y migración). Cierre de coherencia C1–C3 (2026-10-20): §2 (naturaleza de la evidencia), §3.5 nuevo (componentes puntuales), §4.1 (etiquetado), §4.3 (raíces múltiples), §4.7 nuevo (salidas efectivas en raíces coincidentes, primera inicialización y capacidad mínima candidata), §8 (exactitud de la huella) y §11 (riesgos) |

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

**Naturaleza de la evidencia (C3).** Los niveles dicen qué se afirma; la naturaleza de la prueba
dice con qué garantía:

| Naturaleza | En `CU2` y en GeoCeDG | Puede certificar |
|---|---|---|
| prueba simbólica o algebraica exacta | certificado algebraico exacto (§4.6), factorización del par de tangentes, simetría estructural (§5), totalidad estructural (§4.2) | sí |
| aritmética de intervalos con cotas rigurosas | cajas con redondeo exterior; inclusiones de Krawczyk para raíces simples, ya implementadas en las intersecciones de SplineV2 (`SplinePairIntervalCertification2D`, `SplineImplicitIntervalCertification2D`); envolventes de longitud de clase 2 de `CU1` | sí, para desigualdades estrictas y raíces simples; una multiplicidad, solo con estructura añadida (§4.3) |
| verificación numérica bajo hipótesis | estimador de longitud de clase 3 de `CU1`, con sus hipótesis registradas | no: es una estimación (D-CU1-2) |
| evidencia exploratoria | recorridos muestreados, bisección de la validez puntual | no |
| falta de evidencia | presupuesto agotado, operación fuera de la clase, comportamiento heredado dependiente de tolerancias | no: `UNRESOLVED` o `NOT_ESTABLISHED` |

Las constantes capturadas en binary64 son racionales exactos y definen la configuración que se
certifica. Una relación geométrica ideal que la construcción calcula con redondeo no queda
preservada: se certifica la configuración redondeada, nunca la relación ideal (análisis §3.8.3).

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

### 3.5 Componentes puntuales (C2)

El certificado algebraico exacto (§4.6) puede probar que la existencia se reduce a un parámetro
aislado: una raíz de multiplicidad par con el predicado no positivo a ambos lados, como la
tangencia exterior. Ese conjunto `{t*}` no es un arco de longitud positiva y no se representa como
tal. Se propone una entidad propia, la **componente puntual**, distinta de los arcos e intervalos:

| Aspecto | Contrato |
|---|---|
| existencia | certificada solo en `t*`: todos los predicados de validez se cumplen allí, con no existencia certificada o banda `UNRESOLVED` a cada lado |
| parámetro | una envolvente certificada de `t*`, número algebraico que en general no es binary64, con la fuente estructural que lo define (operación, predicado, raíz aislada) |
| inclusión | cerrada: `{t*}` |
| continuidad | trivial sobre el conjunto unitario; no afirma ninguna continuación ni dirección |
| maximalidad | establecida con no existencia certificada a ambos lados; local si un lado es `UNRESOLVED` |
| completitud | compatible con `COMPLETE`: un dominio cubierto por componentes puntuales y no existencia certificada es completo |
| identidad y linaje | clave de rama más la fuente estructural del parámetro aislado; transiciones tipadas de aparición, desaparición y cambio de dimensión (punto a arco y arco a punto), con relación explícita: la raíz par se desdobla en dos raíces simples que acotan un arco |
| métrica | longitud cero; contribución cero al total; una ruta solo existe dentro de la misma componente y mide cero |
| pertenencia y selección | por la dirección estructural de la componente, no por igualdad de un parámetro binary64, que en general no alcanza `t*`. Un punto semántico no puede seleccionarla por parámetro |
| representación y exportación | nunca un segmento ni una curva. En `CU2` se declara en el inspector, en el anexo DXF y en los comentarios LaTeX, sin dibujarla (D-CU2-19); un consumidor que no la admite lo declara y falla explícitamente |
| posición geométrica | `F(t*)` se obtiene, si se puede, como envolvente certificada con la forma límite (en una raíz doble, `λ = −b′/a`); si no, la componente existe con su posición no disponible (§3.4) |

No se confunde con un parámetro singular interior (§5): allí la raíz par tiene el predicado
positivo a ambos lados y la componente continúa a través de ella. Un certificado de existencia
puntual no crea un objeto punto público ni materializa una intersección: eso pertenece a `CU3`.

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
  - la permutación, los indicadores de inicialización y el estado de etiquetado de las salidas
    son estado del algoritmo: la captura los lee y forman parte de la huella de la revisión. El
    etiquetado decide si la segunda salida se anula en una raíz coincidente (§4.7);
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

**Precisiones de la implementación (T3, 2026-10-21).** Ninguna cambia las reglas aprobadas:

- **Semántica de los predicados.** Los predicados de validez de la clase v3 son los exactos de
  `certified-construction-model` §10.4 (`ConstructionValidityPredicate2D`). Una tolerancia heredada
  que restringe la definición dentro de su conjunto de validez (definición de recta y de punto,
  tipo circunferencia `r ≥ 10⁻⁸`, rama de normalización de coeficientes, supresión de una segunda
  salida sin etiqueta) deja la caja `UNRESOLVED` con causa tipada y nunca certifica existencia.
  Una tolerancia heredada que solo amplía la definición más allá del predicado exacto (ventana de
  tangencia de la intersección recta–cónica, tolerancia de incidencia de los caminos limitados) es
  exceso de evaluación y nunca frontera (hallazgo F-CU2-1).
- **Selección.** La captura lee el estado de la copia del corte, validado contra el vivo: una
  permutación asentada (modo 0) o una primera inicialización pendiente (modo 1, compactación en
  orden bruto de las raíces que están en los caminos). Una permutación viva que la siguiente
  evaluación fijará por cercanía no tiene regla establecida: el informe de fidelidad la anota y no
  hay programa. Quedan sin programa el caso especial de una intersección ya existente
  (`existingIntersections() ≠ 0`), las intersecciones dentro de una macro (su construcción es
  compartida por todas sus instancias, y su estado de selección no pertenece a ninguna), la
  selección por punto de referencia, una recta definida como tangente y el modo continuo, que la
  política del producto mantiene desactivado.
- **Una captura por revisión.** El programa de clase v3 se captura una vez, al construir y validar
  el corte de la revisión, y no depende del orden de las consultas posteriores (V-SEL-6).
- **Constantes.** Una subexpresión en línea que no depende del conductor es una constante en la
  frontera máxima independiente del conductor, con el valor binary64 que calcula el propio
  evaluador (`certified-construction-model` §2 y §3, regla 2). Las constantes internas de una
  macro se leen después de recalcular la propia instancia, porque la construcción de la macro es
  compartida.
- **Dependencia del conductor.** La salida de un locus no depende de la posición del conductor:
  los elementos derivados de ella se clasifican por sus demás entradas, como en la huella v2.
- **Entrada métrica.** Un `LocusLength` entre dos posiciones, con su adaptador escalar, es un paso
  `METRIC_INPUT` cuyo valor nunca se evalúa: una caja que lo necesita queda `UNRESOLVED`
  (`METRIC_VALUE`) hasta el certificado estructural de T6.
- **Banda de una segunda salida sin etiqueta.** Conservadora y de primer nivel:
  `D ≤ 10⁻³·máx(a², b², c²)`, que contiene la ventana de tangencia heredada
  (`D ≤ 4·10⁻⁵·máx(a², b², c²)` para toda tolerancia de reintento y toda normalización de la
  recta), o `D ≤ 10⁻¹⁰·(s² + r²·(a² + b²))`, que cubre el redondeo del discriminante heredado.
- **Límite de pasos.** 256 pasos dependientes del conductor tras expandir las macros (rango
  aprobado de 128 a 512, D-CU2-5). Un programa de más de 64 pasos es de clase v3. Evidencia del
  sondeo de T3: los cortes del modelo tienen 10 (`o`, `v`), 13 (`w`, `a_1`, `e_1`), 16 (`o_1`)
  y 27 pasos (`e_2`, con las dos macros expandidas); una caja cuesta 7,7 µs con 64 pasos, 10,4 µs
  con 128 y 18,9 µs con 256 (JVM en régimen), así que con el presupuesto máximo aprobado de
  16 384 cajas una rama y revisión cuesta como mucho unos 0,31 s.
- **Clases v1 y v2.** Un programa sin pasos de la clase v3 capturado por la vía v3 es el mismo
  programa, con la misma versión y la misma firma, y el modelo de cajas reproduce bit a bit sus
  envolventes de valor y derivada.

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

**Raíces múltiples (C3).** Los procedimientos ordinarios de aislamiento de raíces simples
(cambio de signo, derivada que no se anula, Newton de intervalos o Krawczyk) no certifican por sí
solos una raíz múltiple: con datos solo aproximados, un cero doble no es estable frente a
perturbaciones arbitrariamente pequeñas. Existen procedimientos verificados que añaden
estructura: multiplicidad conocida, deflación, factorización, información derivativa o la
verificación de un problema perturbado (Rump y Graillat; análisis §3.8.2). Con los datos exactos
que da la captura, la aritmética exacta decide además la multiplicidad. `CU2` elige, para los
predicados de primer nivel, el certificado algebraico exacto (§4.6). Para los demás usa el
método del punto estacionario: Krawczyk sobre `P′`, con una envolvente de `P″` que excluye el
cero, prueba la existencia y unicidad del extremo `t*`, y el signo de `P(t*)` decide si su
envolvente lo excluye; si no lo excluye, la caja queda `UNRESOLVED`.

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

**Precisiones de la implementación (T6, 2026-10-21).** Ninguna cambia un valor, una política, un
orden ni una dirección de `LocusLength`:

- **Certificado `metric-function-structure/v1`.** `LocusMetricFunctionStructure2D` publica piezas
  del dominio canónico del conductor (`DEFINED`, `UNDEFINED`, `UNRESOLVED` y `BOUNDARY`, esta
  última el encierro de la frontera certificada lejana de una fuente abierta) y los eventos
  `ORIGIN_WRAP`. Lo calcula `AlgoLocusBetweenMetricV2.getFunctionStructure()` una vez por
  certificado de la fuente, dirección del origen y política, solo cuando el destino es la ocurrencia
  generadora del conductor de la fuente y el origen es un punto semántico de la fuente cuyas demás
  entradas no descienden del conductor en el grafo (criterio conservador). En otro caso la
  estructura queda `NOT_ESTABLISHED` con causa tipada.
- **Rectificabilidad (`route-source-facts/v1`).** Una fuente periódica cerrada solo define la
  longitud con una cota superior rigurosa `L <= Σ anchura · (|x′| + |y′|)` sobre una cubierta del
  círculo de 64 cajas más la caja de costura, refinada hasta `2⁻²⁴` y 2 048 cajas con
  envolventes de primer orden; sin cota la estructura no se establece. Una fuente con parámetros
  singulares suele quedar así (limitación).
- **`jumpPositive`.** Cuerda con redondeo exterior entre el punto trazado en `t_A` y en
  `t_A + k·periodo/4` (`k = 1..3`): la mayor separación de encierros disjuntos es la cota inferior
  del salto; el evento guarda `[cuerda, cota]` y la referencia a la longitud total, que solo se
  resuelve por consulta explícita (`resolveJumpValue`), nunca en un certificado.
- **Fuente abierta.** Solo con política estricta y `zero-length`: la ruta está definida desde el
  origen, cerrada en él, hasta la cota interior del extremo lejano, cruzando la costura si el arco
  la cruza. Se detiene allí solo si ese extremo es una frontera certificada (transición, extremo
  del dominio o evento estructural): entonces el resto es inalcanzable (`UNDEFINED`). Pasado el
  borde de una banda `UNRESOLVED` la fuente puede continuar, y la alcanzabilidad de todo lo demás
  queda `UNRESOLVED`; en un dominio acotado, la parte detrás del origen es inalcanzable en
  cualquier caso, porque la ruta no da la vuelta (corrección de T7, hallazgo F-CU2-15). La
  finitud de la ruta en la raíz exacta lejana no se certifica: su encierro es una banda del
  dependiente y su componente queda local (limitación). Las demás políticas sobre fuentes
  abiertas quedan `NOT_ESTABLISHED`.
- **Captura.** La entrada métrica de clase v3 recibe la estructura cuando su origen es fijo y su
  destino es la ocurrencia generadora de una fuente conducida por el mismo punto conductor del
  corte; el evaluador la calcula de forma perezosa, dentro del ámbito de certificación, a partir de
  la longitud rica de su propio corte. Un valor métrico que no depende del conductor no es una
  constante del programa: la captura lo rechaza con `metric-value-constant` (contrato §10.1,
  `locus-v2-metrics.md` §26.3 y condición de T4–T7 de la autorización).
- **Certificador.** Los extremos de las piezas cortan la partición exactamente. Un `ORIGIN_WRAP`,
  o el paso de una pieza definida a una no definida en un binary64, es una celda de evento de anchura
  cero, o la celda de costura si está en `−π`, con dos extremos distintos `(paso METRIC_INPUT,
  ORIGIN_WRAP o ROUTE_DOMAIN, lado)`. La discontinuidad del punto trazado se certifica solo si sus
  encierros con la longitud en `0` y en `[cuerda, cota]` son disjuntos, con las operaciones totales
  continuas en la longitud; si no, el evento es una banda cuyos extremos conservan su inclusión.
  Donde la estructura define la ruta, la entrada y los pasos totales que alcanza quedan definidos
  sin valor; donde la excluye, el punto trazado no existe (`DEPENDENCY_CERTIFIED_NONEXISTENT`);
  donde no decide, banda con la causa de la estructura. Si todas las celdas salvo el evento son
  válidas, la componente es el arco abierto de longitud completa que empieza y acaba en el evento.
- **Simetría.** Una entrada métrica nunca es fija en `fold-symmetry/v1`: su valor depende del
  parámetro del conductor a través de la dirección de su destino, no de los valores que lee.
- **Sondeo del modelo del autor (fijado en pruebas en T7).** `e_2`: `COMPLETE`, un arco abierto de
  longitud completa en `t_N = 0`, cerrado en el origen y abierto en el otro extremo, 67 cajas,
  0 lecturas métricas; `w_1`: salto certificado en `[3,22; 56,37]`, `jumpValue` 13,2365 `ESTIMATED`.
- **Valores en vivo.** Hasta T9 y T11 la longitud en vivo de una fuente abierta sigue leyendo la
  estructura de compatibilidad publicada (B07): la estructura en sombra puede declarar la ruta donde
  el valor vivo aún falla.

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
| multiplicidad par | negativo y negativo | punto aislado de existencia: componente puntual (§3.5) |

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
configuración redondeada (análisis §3.8.3). El certificado prueba la existencia matemática de la
raíz y su multiplicidad; que la **salida** que define el punto trazado esté realmente definida en
ella es otra cuestión (§4.7).

**Precisiones de la implementación (T4, 2026-10-21).** Certificador
`HybridExistenceCertifier2D`, contrato `locus-existence/hybrid/v1`. Ninguna precisión cambia las
reglas aprobadas:

- **Política versionada** `hybrid-existence-policy/v1`, dentro de los rangos de D-CU2-5: corchete
  de anchura `δ = 2⁻⁴⁰` periodos (unos 5,7·10⁻¹² en el círculo); 4 096 cajas por rama y revisión,
  incluidas las cajas puntuales del estrechamiento; profundidad mínima 6 (64 celdas); el interior
  de una banda se refina solo hasta la profundidad 8; presupuesto exacto de 4 096 bits y 512
  bisecciones por raíz; evidencia exploratoria de 32 muestras por banda con 24 bisecciones por
  transición muestreada.
- **Partición.** Diádica sobre el dominio canónico. Con conductor circular, una celda de costura
  `[Math.PI, nextUp(Math.PI)]`, sin ningún parámetro canónico interior, cubre el hueco binary64 del
  dominio en la costura, de modo que las celdas cubren el círculo real entero y la continuidad en
  la costura se certifica como en cualquier otro punto (§4.4). Solo se refinan las celdas sin
  decidir vecinas de una celda certificada: el borde de una banda se localiza sin subdividir su
  interior.
- **Forma centrada.** La evaluación certificadora decide cada predicado, cada tolerancia heredada y
  la banda de una segunda salida sin etiqueta con la intersección de la envolvente ordinaria y la
  forma centrada `F(m) + F′(caja)·[−r, r]`: el teorema del valor medio sobre la misma fórmula y la
  misma rama, que conserva la cota rigurosa y elimina la sobreestimación por cancelación. El borde
  de la banda de `R2` pasó de unas 2 800 cajas a unas 240. Las demás consultas del modelo de cajas
  no cambian, y las envolventes de las clases v1 y v2 siguen siendo idénticas bit a bit.
- **Raíces.** Un predicado de primer nivel se clasifica una sola vez por revisión, cuando una celda
  lo deja sin decidir: sus huecos negativos son no existencia exacta, en los positivos el
  predicado se supone cierto, y cada envolvente de raíz es una celda propia que decide su tipo
  (transición, parámetro singular interior o componente puntual) evaluando el resto del programa
  con el predicado supuesto. Un predicado de desigualdad estricta que no es de primer nivel se
  decide por monotonía (cambio de signo con `P′` que excluye el cero, estrechado por bisección
  sobre cajas puntuales hasta `δ`) o por un punto estacionario aislado con `P″`; una raíz múltiple
  de esos predicados queda en banda.
- **Predicados de igualdad.** Nunca se suponen ciertos, porque alrededor de su raíz rige la
  tolerancia heredada de definición (finitud de `GeoPoint.updateCoords`, definición de recta,
  radio mínimo), que una caja no decide. Su raíz exacta, si el predicado es de primer nivel, se
  registra como evento abierto dentro de la banda (V-TR-2, V-PER-3); un predicado de igualdad
  idénticamente nulo es no existencia exacta.
- **Representación de una frontera.** Una componente termina en el extremo interior, cerrado, de
  la envolvente de la raíz que la limita: su interior seguro (`existence-components` §7). La
  frontera lleva la envolvente y la inclusión del extremo exacto, y la envolvente se cubre con un
  estado propio, `CERTIFIED_BOUNDARY`, que es la evidencia de frontera de `existence-components`
  §5 y es compatible con `COMPLETE`; ningún extremo se redondea en silencio. Los parámetros
  singulares forman parte de la estructura, con envolvente, fuente y multiplicidad, dentro de
  existencia certificada.
- **Selección por compactación.** El cambio de signo de la pertenencia de una raíz que una
  compactación selecciona cambia la raíz seleccionada, no la existencia: no es una transición, y
  su celda queda en banda.
- **Fuentes.** La fuente de una raíz exacta es `root-k`, su orden canónico en el certificado
  exacto, y distingue las raíces de un mismo predicado dentro de la revisión (V-POINT-2); la de
  un extremo de componente es su lado. El linaje estructural entre revisiones es el paso T8.
- **Certificado en sombra.** `AlgoDependentPointLocusV2.getHybridExistenceCertificate()` lo calcula
  una vez por revisión, dentro de un ámbito de certificación (GC-6), y lo comparten todas las
  consultas; mover el conductor no lo recalcula. La estructura publicada no cambia hasta T11. Sin
  programa de clase v3 el certificado es `NOT_ESTABLISHED`, con la razón tipada de la captura
  (`outside-class` o `step-limit-exceeded` con `OPERATION_OUTSIDE_CLASS`; `unsettled-selection`
  con `NOT_CERTIFIED`) y evidencia exploratoria. Mientras rija H01, un locus sin revisión publicada
  no tiene certificado (T11).
- **Valor métrico.** Hasta T6, una caja que necesita el valor de una entrada métrica estructural
  queda `UNRESOLVED` sin refinar.

### 4.7 Definición efectiva de las salidas en raíces coincidentes (C1)

GeoGebra anula la segunda salida de una intersección cuando no tiene etiqueta y coincide con la
primera: `AlgoIntersect.avoidDoubleTangentPoint` (`AlgoIntersect.java:40-45`), llamada al final
de cada cálculo de `AlgoIntersectLineConic` (`AlgoIntersectLineConic.java:280`). La coincidencia
se mide con `DoubleUtil.isEqual`, una tolerancia absoluta de 10⁻⁸ en cada coordenada
(`GeoPoint.java:1036-1046`; `DoubleUtil.java:69-75`). Una intersección simple de índice 2
(`Intersect(c, l, 2)`) copia esa salida auxiliar, que nunca tiene etiqueta
(`AlgoIntersectSingle.java:236-279`).

Hay que distinguir siete hechos:

| Hecho | Quién lo establece | En una raíz doble |
|---|---|---|
| 1. existencia matemática de la intersección | el certificado (§4.6) | la recta es tangente: un punto doble |
| 2. multiplicidad | el certificado | 2 |
| 3. identidad semántica de cada solución seleccionada | el estado de selección (§4.1) | la salida `i` es la raíz bruta `permutation[i]`; las dos designan el mismo punto |
| 4. definición efectiva de cada salida `GeoElement` | el algoritmo heredado | la salida 0 está definida; la 1, solo si tiene etiqueta |
| 5. disponibilidad de un punto para la evaluación de Locus V2 | la copia del corte | la del algoritmo de la copia, con el estado de etiquetado de la copia |
| 6. continuidad de los límites laterales | el certificado | existen y coinciden: `√D` es continua |
| 7. continuidad de la construcción seleccionada | 4 y 5 junto con 6 | continua si la salida está definida en todo el entorno; si no, interrumpida |

**Modelo del autor.** `{Q, R}` son salidas etiquetadas de un algoritmo de varias salidas, tanto
en el vivo como en las copias de los cortes de `o` y `v` (sondeo, análisis §3.9). La regla no se
aplica y en la raíz doble las dos salidas están definidas y coinciden: la construcción seleccionada
es continua, con su esquina, y la componente periódica cerrada con un parámetro singular de §3.8
del análisis se mantiene. Lo hace con una precondición nueva: el estado de etiquetado de cada
salida forma parte de la identidad semántica de la selección y de la huella de la revisión, y la
copia del corte debe reproducirlo (hoy lo reproduce).

**Segunda salida sin etiqueta.** La salida 1 queda indefinida donde sus coordenadas calculadas
coinciden con las de la salida 0 dentro de 10⁻⁸, y también dentro de la ventana de tangencia del
evaluador, donde las dos son iguales. Esa definición efectiva depende de la tolerancia y del
redondeo: el sondeo la encuentra dispersa en la banda `|t − t_c| ≤ 10⁻⁸`, con 49 de 201 muestras
indefinidas en el vivo y 55 en la copia del corte. Por eso:

- el certificado no declara existencia ni continuidad de esa salida a través de una raíz
  coincidente: publica alrededor de ella una banda `UNRESOLVED` con causa tipada (supresión
  heredada de la segunda salida), acotada por predicados de primer nivel conservadores (distancia
  entre las raíces hasta `√2·10⁻⁸` más un margen de redondeo, y la ventana de tangencia);
- fuera de la banda, la salida existe donde el discriminante lo prueba;
- un locus que dependa de esa salida queda `NOT_ESTABLISHED`, con componentes locales a ambos
  lados; en una frontera simple de mordedura, el extremo de esa salida no se certifica.

**Raíz doble en la primera inicialización.** Dos raíces iguales no inicializan el algoritmo
(análisis §3.9): mientras sigue en inicialización, la regla es la compactación en orden bruto
(§4.1). El primer movimiento que separa las raíces fija la permutación, y ese cambio del estado
heredado se vuelve a leer y publica una revisión con diagnóstico, con las mismas componentes. La
regla de A1.1-R se cumple, porque la huella cambió; lo que no se cumple en este caso es su
consecuencia habitual, que mover solo el conductor no publique revisión. La causa es el estado
heredado, no la posición del conductor (D-CU2-18).

El valor límite de la raíz coincidente (`λ = −b′/a`, con su envolvente) se certifica como
evidencia y nunca como salida operativa. Para devolver la continuidad a esas construcciones hay
dos caminos, los dos fuera de la autorización actual (D-CU2-18):

- **selección continua de raíces coincidentes en el corte**, capacidad mínima del núcleo,
  diseñada aquí y no implementada:

  | Aspecto | Diseño |
  |---|---|
  | Contrato | dentro de la banda de supresión certificada, el corte da a la salida 1 suprimida el valor de la raíz coincidente que designa su selección, calculado con la forma límite (`λ = −b′/a`) y su envolvente; fuera de la banda no actúa |
  | Precondiciones | salida de índice 1 sin etiqueta de una intersección recta–circunferencia de la clase v3, o la auxiliar de una intersección simple; raíces coincidentes certificadas por el certificado exacto; la misma raíz bruta designada a ambos lados; límite común de los dos lados certificado |
  | Ciclo de vida | se decide al sincronizar cada revisión y forma parte de su huella; no se persiste; se recalcula al cargar, deshacer, rehacer y recalcular |
  | Compatibilidad | el Locus V2 queda definido donde el objeto vivo no lo está: cambia la semántica respecto del comportamiento heredado y hay que declararlo en la especificación y en el inspector; el objeto vivo no cambia |
  | Pruebas | V-COINC-2, V-COINC-5, V-COINC-7 y V-COINC-9 con la capacidad activa: continuidad a través de la banda, igualdad con la referencia de precisión ampliada y divergencia declarada con el vivo |
  | Frontera | escribe en el corte la definición de una salida que el algoritmo heredado anula: modifica en la copia el comportamiento heredado, aunque no el algoritmo vivo ni su permutación. Por eso la propuesta se detiene aquí y necesita autorización específica |

- **cambiar la regla heredada**: cambio de comportamiento upstream.

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

**Precisiones de la implementación (T5, 2026-10-21).** Ninguna cambia las cinco pruebas:

- **Tabla de operaciones `fold-symmetry/v1`.** Un conductor circular sin términos cruzados en sus
  ejes capturados (`x` solo depende de `cos t` e `y` de `sin t`) admite dos reflexiones: en la
  horizontal por el centro (`t → −t`, puntos fijos `0` y `π`) y en la vertical (`t → π − t`,
  puntos fijos `±π/2`). Un paso es fijo, con el mismo valor exacto en `t` y en `σ(t)`, si todos
  los pasos que lee son fijos, o si lee el conductor solo a través de una magnitud que la
  reflexión conserva: la recta por el conductor perpendicular al espejo (paralela a una recta
  constante perpendicular al espejo, o perpendicular a una paralela) y la circunferencia por el
  conductor con centro constante sobre el espejo, por igualdad exacta de los binary64 capturados.
  El punto trazado es simétrico si su paso es fijo; cualquier otro uso del conductor deja el
  programa sin simetría (V-FOLD-2, V-FOLD-3).
- **Pruebas 2 y 3.** Cajas de aislamiento alrededor del punto fijo, de `2⁻⁸` a `2⁻³⁰` periodos,
  en las que el modelo de cajas encierra el punto trazado con sus derivadas primera y segunda; la
  primera caja en la que una componente de `F″` excluye el cero certifica a la vez el aislamiento
  y la no degeneración. Esa caja es la única banda donde el dibujo podrá eximir el giro (paso T9).
  Sus cajas cuentan en el presupuesto y en el contador de cajas del certificado.
- **Prueba 4.** La imagen canónica de la caja está en existencia certificada y dentro de una sola
  componente, también alrededor de la costura.
- **Prueba 5.** Se sigue de las anteriores; el certificado registra la componente y la envolvente
  de `F″` en la caja, que dan las direcciones opuestas de las tangentes laterales.
- **Punto estacionario degenerado.** Con la simetría y la prueba 4, pero sin caja en la que una
  componente de `F″` excluya el cero, el punto estacionario está demostrado (`F′(t₀) = 0`) y nunca
  se certifica como pliegue no degenerado (V-FOLD-4).

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
- **Exactitud de la huella y fidelidad del corte (F-CU2-12 corregido; D-CU2-20-R).** El
  certificado de una revisión solo es válido si la revisión cambia con cualquier cambio
  semánticamente relevante de sus entradas y si el corte reproduce exactamente las entradas
  autoritativas. La caracterización del 2026-10-21 corrigió F-CU2-12: los valores numéricos se
  escriben con `Double.toString` (`XMLStringBuilder.attr(String, double)`), exacto en ida y vuelta,
  y un cambio de un ulp de `excCyl` publica revisión con la copia exacta; la observación anterior
  venía de H01 y F-CU2-3. Las lagunas reales y su ciclo de vida corregido están en §8.1.

### 8.1 Ciclo de vida de captura, reconstrucción, validación y publicación (D-CU2-20-R)

Descripción previa al código que exige D-CU2-20-R. Se basa en la lectura de
`AlgoDependentPointLocusV2.synchronizeEvaluatorWithCurrentSlice`,
`ReconstructionSliceFingerprint2D.compute`, `ReconstructibleLocusEvaluator2D` (constructor y
`resetExternalState`), `Macro.buildMacroXML` y `MacroKernel.loadXML`, y en el sondeo
`Cu2FidelityProbe` del directorio temporal de la sesión.

**Estado actual (`CU1`).** En cada recálculo del algoritmo padre:

1. `getReconstructionSliceGeos()` reúne los elementos del corte: los predecesores del punto
   trazado, del estado, de la coordenada, del soporte y de las entradas de dominio.
2. La huella v1 es el SHA-256 de la serialización XML de los algoritmos y elementos del corte en
   orden de construcción, con los atributos numéricos de los elementos dependientes del conductor
   neutralizados. No incluye el estado de selección de los algoritmos de intersección, que no se
   serializa; el etiquetado de sus salidas entra solo a través del XML del algoritmo (renombrar
   una salida cambia la huella). Resume la representación serializada y no los valores: un
   número escrito por el usuario al que se asigna por programa su mismo valor pierde su
   expresión literal, y la huella cambia y publica una revisión con el mismo estado semántico
   (caracterización de T1).
3. Si el descriptor o la huella cambian, el corte se reconstruye: `Macro.buildMacroXML` escribe el
   XML de los elementos con la plantilla XML ordinaria y `MacroKernel.loadXML` lo carga en un
   `MacroKernel` aislado. Los valores de los elementos (`<value val>`, `<coords>`) se escriben con
   `Double.toString` y son exactos; los literales con texto original conservan ese texto; un
   literal `MyDouble` sin texto original también se escribe exacto (`Kernel.format` con
   `GEOGEBRA_XML` usa `MyDouble.toString`), salvo si es angular: `Kernel.formatAngle` lo escribe en
   grados (`Math.toDegrees`, `checkInteger`, `checkDecimalFraction`), y la copia puede diferir del
   valor vivo (caracterización de T1: 0,4097 rad vuelve un ulp por encima). Un argumento literal en
   línea de un comando se convierte en un elemento independiente sin etiqueta del corte, y su valor
   se reproduce exacto en los casos medidos.
4. `resetExternalState` copia los valores de las dependencias durables independientes y llama a
   `updateConstruction`, que reinicializa por cercanía el estado de selección (F-CU2-8).
5. Nada compara el corte con el estado vivo antes de evaluar o de capturar un programa.

**Ciclo corregido.**

| Fase | Contenido | Autoridad |
|---|---|---|
| 1. Captura de la instantánea | huella `reconstruction-slice-fingerprint/v2`: la parte estructural de v1; los bits binary64 de los valores de los elementos independientes y de los literales de las definiciones del corte, codificados en hexadecimal; el estado de selección (permutación e indicadores de inicialización) de cada intersección del corte y el etiquetado de sus salidas, leídos con accesores de solo lectura; las versiones de la política de reconstrucción y del contrato de certificación. Independiente de la representación: un mismo valor, escrito como literal o asignado por programa, da la misma huella (V-REV-EXACT-2). Nunca identidad de objetos Java, índices circunstanciales, posición del conductor ni datos de dibujo | algoritmo padre, sobre el estado vivo |
| 2. Decisión de revisión | descriptor y huella iguales: recálculo equivalente, sin reconstrucción ni revisión. Distintos: reconstrucción. Un cambio del estado heredado de selección (por ejemplo, el fin de una primera inicialización) es un cambio de instantánea con causa tipada; un recálculo con el mismo estado no publica revisión | algoritmo padre |
| 3. Reconstrucción | `Macro.buildMacroXML` y `MacroKernel.loadXML`, sin cambios upstream y sin cambio del formato persistido | `MacroKernel` aislado |
| 4. Restauración admitida | solo del original vivo a la copia, nunca al revés, y solo donde la correspondencia es la propia de la reconstrucción: dependencias durables por su identidad; elementos independientes por la etiqueta con la que el propio XML del corte los definió; argumentos literales en línea por su posición de entrada en el algoritmo copiado de la misma salida; hojas `MyDouble` por recorrido paralelo de árboles de expresión de estructura idéntica. Cada restauración es una asignación de valor exacta seguida de la actualización del grafo de la copia | evaluador |
| 5. Validación de fidelidad | comparación exacta, por bits, de cada entrada autoritativa viva con su copia, y del estado de selección y de etiquetado de cada intersección. Resultado tipado: fiel, o no fiel con la lista de discrepancias (elemento, clase de valor, causa) | evaluador |
| 6. Publicación | fiel: revisión publicada con el evaluador; el certificado (en sombra hasta T11) cita la huella. No fiel: revisión publicada con existencia `NOT_ESTABLISHED` y causa `RECONSTRUCTION_FIDELITY_NOT_ESTABLISHED`, sin certificado, y evaluaciones `EVALUATION_FAILED` con esa causa | algoritmo padre |
| 7. Reapertura, deshacer y rehacer | el documento recargado es el estado vivo; la instantánea se captura de él y el certificado le corresponde. La pérdida de precisión de un formato persistido, si existe, se caracteriza; nunca se asocian certificados del estado anterior | ciclo normal de carga |

Contadores públicos nuevos (T1): huellas calculadas, reconstrucciones, validaciones de fidelidad,
restauraciones y fallos de fidelidad. Pruebas: filas V-REV-EXACT de la
[matriz](../validation/pre_g9b_cu2_validation_matrix.md). Si la fidelidad exigiera cambiar el
formato persistido, el comportamiento heredado o la identidad durable, la implementación se
detiene y se presenta una propuesta correctiva independiente.

**Precisiones de la implementación (T3, 2026-10-21).** Ninguna cambia el ciclo:

- la huella v2 tiene cuatro partes con resumen propio (estructura, valores exactos, selección y
  etiquetado), de modo que la causa de cada reconstrucción queda tipada; la estructura excluye
  la forma serializada de los elementos libres y las etiquetas de valor derivado de los
  dependientes (`coords`, `value`, `matrix`, `eigenvectors`, `pathParameter`,
  `regionParameters`), y los valores exactos incluyen, además de las entradas autoritativas, los
  valores derivados que no dependen del conductor, porque un valor aleatorio cambia al
  recalcular sin cambiar ninguna entrada;
- las entradas sin etiqueta de un algoritmo definido por una expresión se recorren en el orden
  de su árbol, porque la reconstrucción no conserva el orden de sus entradas; las de un comando
  conservan el orden de sus argumentos;
- un valor aleatorio se restaura con la interfaz pública del propio algoritmo
  (`SetRandomValue`), que es una asignación exacta y no escribe estado de selección;
- una selección viva en su primera inicialización tiene por regla la compactación de las raíces
  que están en los caminos, y la copia la reproduce solo si también sigue en su primera
  inicialización; una permutación viva que la siguiente evaluación fijará por cercanía no tiene
  regla que reproducir: no se compara, el informe la anota como selección no asentada y no hay
  programa de clase v3; cuando se asienta, cambia la parte de selección de la huella, el corte se
  reconstruye y la comparación se repite (precisión de T3b);
- la actualización de la copia después de restaurar es la de `updateConstruction(false)` sin su
  reinicialización del estado de selección por cercanía: la copia conserva el estado con el que
  se validó su revisión, cualesquiera que sean las consultas que la alcanzaron después. Con la
  reinicialización (F-CU2-8), una recaptura tras consultas en un hueco desplazaba la permutación
  de la copia y hacía infiel la revisión según el orden de las consultas (sondeo de T3b);
- un literal angular sin texto original cuyo árbol reconstruido tiene otra forma no tiene
  correspondencia demostrada: no se restaura, y el locus queda `NOT_ESTABLISHED` con causa
  `RECONSTRUCTION_FIDELITY_NOT_ESTABLISHED`.

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
| Un locus del modelo del autor depende de una segunda salida sin etiqueta en una raíz coincidente, o la copia no reproduce el etiquetado | banda `UNRESOLVED` y `NOT_ESTABLISHED` (§4.7); parar y presentar la decisión D-CU2-18, sin sustituir la salida por su límite |
| La huella no distingue un cambio de entrada o el corte no reproduce sus entradas autoritativas (D-CU2-20-R) | ningún certificado se reutiliza ni se publica entre estados que la huella no distingue; fallo explícito ante una discrepancia no restaurable; parar si la corrección exige cambiar persistencia, comportamiento heredado o identidad durable (§8.1) |
| Coste del certificado exacto con coeficientes grandes | presupuesto de bits y bisecciones; `UNRESOLVED` tipado |
