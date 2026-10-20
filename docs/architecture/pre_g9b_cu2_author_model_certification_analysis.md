# PRE-G9B-CU2 — Caracterización complementaria y análisis de certificación del modelo del autor

| Campo | Valor |
|---|---|
| Carácter | Análisis candidato de la preparación de `CU2`: caracterización complementaria (solo donde hacía falta) y explicación constructiva y matemática de la certificación de los siete Locus V2 del modelo del autor. Evidencia y diseño, no autoridad normativa |
| Estado | `CU2 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false`; `TECHNICAL_CANDIDATE_STATE = FROZEN`; `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT` |
| Encargo | [registro de cierre de `CU1`](../validation/pre_g9b_cu1_closeout_record.md) §7 (preparación de `CU2`) |
| Base | cierre documental de `CU1` `27bf652f176e57ced3807b82afe14fc00b7586a9` |
| Modelo | `models/regression/pre-g9b-cu/original/InterCilConoOblique.cedg` (SHA-256 `1ae6f512…890456c`, igual al manifiesto); `continuous=false` |
| Producto caracterizado | el código de producto aprobado de `CU1` (`143f5d37`), idéntico en el cierre |

## 1. Método

La caracterización de `CU0` ya cubre B07, H01, B06 y B10.2
([línea base](../../geocedg/validation/pre-g9b-cu/cu0-geometric-reference-baseline.json)); no se
repite. Este análisis añade solo lo que el diseño de `CU2` necesitaba y `CU0` no medía:

1. una deducción analítica de la existencia de la primera familia, contrastada con la línea
   base;
2. un sondeo de solo lectura, desde el directorio temporal de la sesión, que usa los
   ayudantes de la línea base de `CU0` (`PreG9BCu0Corpus`, `PreG9BCu0References`) sobre una
   copia de trabajo del modelo. Mide:
   - los extremos de rama cerca del valor crítico y en la zona de H01 (excCyl 3,96 a 5,0);
   - el orden de las raíces de la recta–circunferencia;
   - la continuidad en la costura;
   - la velocidad paramétrica de las seis curvas de las dos primeras familias.

El sondeo no forma parte del candidato. Sus cifras se transcriben aquí y su procedimiento se
describe en §8.

## 2. Construcción compartida

El dibujo es un diedro (vistas de Monge) con línea de tierra `h` (y = 5,2565): alzado encima,
planta debajo. Parámetros del autor: `rbCone` = 5, `rbCylinder` = 1,5, `excCyl` = 2,9 (deslizador
de −5 a 5, paso 0,1), `α_cone` = 47°, `α_cyl` = 40°, los puntos libres `A`, `D_1` y `L_1`, y el
número `v_1` = 0 con el selector textual `text2`.

- **Cono oblicuo:** directriz `p = Circle(B, rbCone)` en el plano de tierra; vértice `V` con
  alzado `v' = H` y planta `v = I`; eje frontal a 47°. `B` e `I` están en la recta `i`, de modo
  que el cono es simétrico respecto del plano frontal que pasa por `i`.
- **Cilindro oblicuo:** directriz `g_1 = Circle(C_c, rbCylinder)` desplazada `excCyl` en
  profundidad; generatrices frontales a 40°. El manifiesto del corpus lo llama «vertical»; el
  XML lo hace oblicuo.
- **Haz auxiliar:** recta por `V` paralela a las generatrices del cilindro; `P` es su traza
  horizontal y el polo de las trazas de todos los planos auxiliares. `|PB|` = 17,252518411.
- **Esfera:** de centro `V` y radio `ρ = |H D_1|` = 5,98457, para el desarrollo del cono.

Nada de lo anterior depende de los conductores.

## 3. Primera familia: `o`, `v`, `w`, `a_1` (conductor `N` sobre `g_1`)

### 3.1 Definiciones

Conductor `N = Point(g_1)`, dominio `[−π, π)` periódico; parámetro guardado 0,645649883219.
Cada plano auxiliar contiene la recta `VP` y la generatriz del cilindro por `N`. Su traza
horizontal es `t = Ray(P, N)`, y `{Q, R} = Intersect(p, t)` da las dos generatrices del cono
contenidas en él.

| Locus | Punto trazado | Significado |
|---|---|---|
| `o` | `W = Intersect(i_1, f_1)`, `f_1 = Ray(H, S)` | alzado de la rama `p2` (generatriz `VR`) |
| `v` | `A_1 = Intersect(i_1, j_1)`, `j_1 = Ray(H, U)` | alzado de la rama `p1` (generatriz `VQ`) |
| `w` | `C_1 = Intersect(k_1, l_1)`, `l_1 = Ray(I, Q)` | planta de `p1` |
| `a_1` | `B_1 = Intersect(h_1, m_1)`, `m_1 = Ray(I, R)` | planta de `p2` |

### 3.2 Predicado de existencia

Los rayos nunca limitan: `i_1` tiene pendiente `−tan 40°` y los alzados de todas las
generatrices tienen pendiente positiva desde `v'`, de modo que se cortan siempre debajo de `v'`.
`W` y `A_1` existen exactamente cuando existen `R` y `Q`, y `B_1` y `C_1` cuando existen `W` y
`A_1`. El único predicado de existencia es el discriminante `D(t)` de la recta `PN` frente a la
circunferencia `p`. `D` es una función analítica de `(cos t, sin t)` con coeficientes que no
dependen del conductor.

Sea `θp` el ángulo de la tangente desde `P` a `p` por el lado del cilindro:
`sin θp = rbCone / |PB|`, `cos θp` = 0,957083363. Entonces:

- **Penetración:** si `|excCyl|·cos θp < rbCone − rbCylinder`, es decir
  `|excCyl| < 3,656943727`, la intersección son dos curvas cerradas, `{v, w}` (`p1`) y `{o, a_1}`
  (`p2`), y cada locus es una componente periódica completa.
- **Mordedura:** para `3,656943727 < |excCyl| < 6,7915` cada locus deja de existir en el
  intervalo de conductor `(t_c − β, t_c + β)`, con `t_c = π/2 − θp` = 1,2767651295 (`−t_c` si
  `excCyl < 0`) y `β = arccos((rbCone − excCyl·cos θp)/rbCylinder)`.
- **En los extremos** `Q = R = T+`, el punto de tangencia de la recta límite: raíz doble y
  extremo **cerrado**. `o` y `v` comparten sus extremos, igual que `w` y `a_1`. Todos los
  extremos están sobre la generatriz fija `VT+`.
- **Singularidad:** cada locus tiene en los extremos una singularidad de raíz cuadrada,
  `|F′(t)| ~ (t − t±)^(−1/2)`, con longitud finita. Por eso la frontera se certifica sobre `D`,
  que es regular, y no sobre `F`.
- **Valor crítico** `excCyl = 3,656943727`: `β = 0` y `D` tiene un cero doble en `t_c`. No es
  una raíz simple y ninguna prueba de raíz simple lo certifica (§3.6).

### 3.3 Contraste con la evaluación del producto

Extremos predichos por la fórmula frente a los localizados por bisección sobre la validez de
la evaluación (lado válido, sondeo de esta preparación):

| excCyl | Predicho | Medido | Exceso de la validez puntual |
|---|---|---|---:|
| 3,657 | (1,2682909993, 1,2852392598) | (1,2682953418, 1,2852349236) | 4,3·10⁻⁶ |
| 3,658 | (1,2400490154, 1,3134812437) | (1,2400500200, 1,3134802453) | 1,0·10⁻⁶ |
| 3,66 | (1,2143038443, 1,3392264148) | (1,2143044363, 1,3392258290) | 5,9·10⁻⁷ |
| 3,7 | (1,0418225300, 1,5117077290) | (1,0418226911, 1,5117075743) | 1,6·10⁻⁷ |
| 3,85 | (0,7751755506, 1,7783547085) | (0,7751756301, 1,7783546352) | 7,9·10⁻⁸ |
| 3,9 | (0,7123780143, 1,8411522447) | (0,7123780861, 1,8411521792) | 7,2·10⁻⁸ |
| 3,96 | (0,6444023848, 1,9091278743) | (0,6444024499, 1,9091278154) | 6,5·10⁻⁸ |
| 4,0 | (0,6024120274, 1,9511182317) | (0,6024120892, 1,9511181761) | 6,2·10⁻⁸ |
| 4,5 | (0,1863116418, 2,3672186173) | (0,1863116859, 2,3672185793) | 4,4·10⁻⁸ |
| 5,0 | (−0,1504832559, 2,7040135150) | (−0,1504832163, 2,7040134814) | 4,0·10⁻⁸ |

En todos los casos la evaluación puntual sigue siendo válida un poco **dentro** del hueco: el
umbral absoluto heredado del discriminante declara tangente lo que es ligeramente secante en
sentido contrario. El exceso crece como `ε/|D′(t*)|` cuando `D′` tiende a cero cerca del valor
crítico. Los extremos de `CU0` (`preserve.branch_ends`) son, por tanto, evidencia de la
evaluación y no fronteras exactas: difieren de la raíz exacta de `D` en más de su tolerancia de
10⁻⁷ (1,6·10⁻⁷ en 3,7). La matriz de `CU2` compara las fronteras certificadas con la referencia
analítica y no con esos valores (§7, hallazgo F-CU2-1).

### 3.4 Orden de las raíces, costura y regularidad

- **Orden:** con excCyl = 3,7, `Q` fue la raíz más cercana a `P` a lo largo del rayo en los 44
  conductores válidos de 48. El orden lo fija la permutación de salida de
  `AlgoIntersectLineConic` (modo no continuo), no la geometría. Pero dentro de cada componente
  coincide con una regla geométrica certificable: `Q` es la raíz de parámetro de rayo menor,
  la rama `−√D`.
- **Costura:** con excCyl = 3,7, `|F(π − 10⁻⁹) − F(−π)|` vale 9,9·10⁻¹⁰ para `o` y `v` y
  1,7·10⁻⁹ para `w` y `a_1`. La función es continua en la costura y el conjunto válido
  `[−π, t_c − β] ∪ [t_c + β, π)` es **un solo arco** del círculo que la cruza.
- **Regularidad:** la velocidad mínima medida (4 000 muestras, excCyl = 2,9) es 0,509 para `o`,
  0,484 para `v` y 0,737 para `a_1`. Para `w` es 0,067, en t = 1,5999: un giro muy cerrado, sin
  punto estacionario. Ninguna de las cuatro tiene pliegues; el giro de 156,8° de `w` en B06 es
  una falta de teselación.

### 3.5 Certificación constructiva

Para cada valor de los parámetros del autor, la certificación de un locus de esta familia se
construye así, con las capacidades definidas en el
[diseño de certificación](pre_g9b_cu2_hybrid_certification_design.md):

1. **Programa de clase v3** del corte: recta y rayo `PN` (predicado de rayo `λ ≥ 0`),
   intersección recta–circunferencia `{Q, R}` con la selección capturada como rama de raíz,
   rectas paralelas y perpendiculares (ya de clase v1), rayos desde `H` y desde `I`, e
   intersecciones de rectas.
2. **Predicados de validez:** `D(t) ≥ 0` y los predicados de rayo de `f_1`, `j_1`, `l_1`, `m_1`
   y `t`. Con aritmética de intervalos de redondeo exterior se evalúan en cajas del dominio:
   - `D > 0` y los rayos con `λ > 0` en toda la caja: caja **válida**;
   - `D < 0` en toda la caja: **`UNIFORMLY_INVALID`** con causa `DISCRIMINANT_NEGATIVE`;
   - en otro caso: **`UNRESOLVED`**, que se subdivide.
3. **Transiciones:** una caja `UNRESOLVED` que contiene un cambio de signo de `D` y una
   envolvente de `D′` que excluye el cero (Newton de intervalos) certifica una raíz simple
   única en un corchete de anchura `≤ δ`. La frontera es cerrada, porque en la raíz doble de la
   recta–circunferencia `Q = R` existe.
4. **Continuidad de la selección:** en cada caja válida la rama `−√D` (o `+√D`) es continua.
   La selección capturada es la misma en todo el intervalo porque la permutación congelada no
   cambia mientras `D > 0`.
5. **Costura:** todas las operaciones del corte son funciones de la configuración actual (modo
   no continuo, sin historia), y el conductor es el punto de la circunferencia `g_1`. Por tanto
   `F` es una función sobre el círculo y la continuidad certificada en un entorno de `±π`
   certifica el paso por la costura (§4.4 del diseño).
6. **Resultado:**
   - en penetración: `COMPLETE` con una componente periódica cerrada;
   - en mordedura: `COMPLETE` con **una** componente, el arco `[t_c + β, t_c − β + 2π]` con
     ambos extremos cerrados, que cruza la costura;
   - en el valor crítico, y en una banda alrededor que el presupuesto no resuelva:
     `NOT_ESTABLISHED` con una banda `UNRESOLVED` explícita alrededor de `t_c`, sin componente
     inventada.

La evaluación puntual de `F` dentro del corchete de una frontera no está garantizada (§3.3). El
dibujo llega hasta la imagen del extremo `F(t*)`, que en la raíz doble es `T+` proyectado, con
la parametrización local `t = t* ± s²` (§5.2 del
[diseño de consumo y representación](pre_g9b_cu2_consumption_and_representation.md)).

### 3.6 Casos degenerados

- **Valor crítico** (`β = 0`): cero doble de `D`. Las dos curvas se tocan en un punto doble, y
  cada locus tiene allí una esquina, porque la permutación congelada pasa de una rama suave a
  la otra. Ninguna raíz simple lo certifica; el resultado es `NOT_ESTABLISHED` con una banda
  `UNRESOLVED`. Cerca del valor crítico (3,657 a 3,66) el producto actual dibuja **un solo
  subtrazo a través de un hueco real** (hallazgo F-CU2-2).
- **`rbCone > 7,5735`** (el deslizador llega a 10): en conductores aislados, `k_1`/`l_1`,
  `h_1`/`m_1` y `b_1`/`n_1` coinciden como verticales. Son puntos aislados de no existencia
  causados por la construcción, no por la geometría. Tienen que dar no existencia puntual o
  `UNRESOLVED`, nunca una continuidad inventada.

### 3.7 H01

El punto guardado `N` entra en el hueco cuando `t_c − β ≤ t_N`, es decir desde
`excCyl` = 3,958845384. Desde 3,96 los cuatro loci dejan de estar definidos en el producto,
aunque existen en un arco. Con el conductor movido a −1 se publican de nuevo y sus extremos
medidos coinciden con la fórmula (tabla de §3.3). Esos valores son la referencia que faltaba
para invertir `pins.H01`.

Hallazgo F-CU2-3: mientras la regla de publicación oculta el locus, su evaluación sigue usando
la última definición publicada. En el sondeo, con excCyl = 3,96, 4,0, 4,5 y 5,0 y el conductor
dentro del hueco, los recorridos y los extremos evaluados eran idénticos a los de 3,95, aunque
la revisión avanzaba. Un consumidor que evalúe un locus no publicado ve geometría obsoleta.

## 4. Segunda familia: `e_1` y `o_1` (conductor `E_1` sobre `p`)

### 4.1 Definiciones

Conductor `E_1 = Point(p)`, dominio `[−π, π)` periódico. Alzado de la generatriz por `E_1`:
`q_1 = Ray(H, F_1)`. Planta: `n_1 = Ray(I, E_1)`. Verdadera magnitud por giro alrededor de la
vertical de `V`: `d_1 = Circle(I, E_1)`, `H_1 = Intersect(d_1, i, 1)` y
`s_1 = Ray(H, I_1)`. Punto a distancia `ρ` del vértice: `G_1 = Intersect(c_1, s_1, 1)`,
devuelto a su altura por `t_1 = OrthogonalLine(G_1, k)`.

| Locus | Punto trazado | Significado |
|---|---|---|
| `e_1` | `J_1 = Intersect(q_1, t_1)` | alzado de la curva cono ∩ esfera(`V`, `ρ`) |
| `o_1` | `K_1 = Intersect(b_1, n_1)`, `b_1 = Line(J_1, k)` | planta de esa curva; cerrada, recorrida una vez |

### 4.2 Existencia

Ambos loci existen en todo el círculo:

- los rayos desde `v'` cortan todas las horizontales por debajo de `v'`;
- `d_1` tiene su centro en `i`, de modo que corta `i` siempre en dos puntos distintos;
- `s_1` sale del centro de `c_1`, de modo que exactamente una raíz está en el rayo;
- `v` está fuera de `p` (`x_I − x_B` = 7,5735 > `rbCone`).

Cada uno de estos hechos es un predicado uniforme que la clase v3 certifica sobre todo el
dominio. La completitud se demuestra; no se hereda del conductor.

### 4.3 Pliegues de `e_1`

Por la simetría del cono respecto del plano de `i`, `e_1(t) = e_1(−t)`. La velocidad medida es
exactamente 0 en t = 0 y 1,3·10⁻⁷ en t = ±π: son **puntos estacionarios**, y la imagen es un
arco recorrido dos veces que se pliega en `M_1` y en (1,60844, 10,13096). El giro de 180° de
B06 es real. Para teselarlo sin bajar la calidad hace falta certificar un pliegue aislado:
envolvente de la primera derivada que contiene el cero y envolvente de la segunda que lo
excluye (chorros de segundo orden, §5 del diseño). La simetría es propia de esta configuración
y no sirve de certificado estructural.

`o_1` es regular: velocidad mínima 1,60.

### 4.4 Certificación

Programa de clase v3 con:

- la circunferencia por centro y punto (`d_1`);
- intersecciones recta–cónica indexadas con raíz seleccionada (`AlgoIntersectSingle` sobre
  `AlgoIntersectLineConic`);
- rayos con predicado uniforme;
- operaciones de clase v1.

Ningún predicado cambia de signo, y la función es de la configuración actual: `COMPLETE` con
una componente periódica cerrada para cada locus. `o_1` conserva la componente `[−π, π)` que
referencia `N_1 = Point(o_1, text2, v_1)` (decisión E-3 sobre el linaje).

## 5. `e_2` y la longitud orientada

### 5.1 Forma cerrada

`e_2(t) = (x(L_1) − w_1(t), y(L_1) + y(J_1(t)) − 5,2565)`, con
`w_1 = LocusLength(o_1, N_1, K_1)` en su forma por defecto (adelante / estricta /
`zero-length`) y `d_2 = Length(w_1)`. Las macros `pointJump` y `translationCoor` son afines y
totales en sus entradas.

### 5.2 Estructura

- `w_1(t)` está definida para todo `t`: `o_1` es una componente periódica completa y la ruta
  adelante existe siempre.
- `w_1` es continua y creciente salvo en `t_N`, la dirección canónica de `N_1` (0, exacta,
  porque `N_1` es la posición semántica de parámetro `v_1 = 0` en la componente de `text2`).
  En `t_N` salta exactamente `L`, la longitud total de `o_1`. Su derivada `σ` es continua.
- `w_1` es continua en `±π`: 6,61623 frente a 6,62023 en `π ∓ 10⁻³`, distancia de costura
  `e_2` de 2,0·10⁻⁹.
- Por tanto `e_2` es **una componente abierta** `[t_N, t_N + 2π)`. Con `zero-length` es cerrada
  en el origen y abierta en el otro extremo; con `full-cycle`, al revés. Sus dos extremos son el
  mismo punto espacial y la componente **no es periódica**.

### 5.3 Certificación sin usar el valor de la longitud

La longitud de `o_1` es de clase `ESTIMATED` (clase 3) desde `CU1`. Por la instrucción de cierre
de `CU1` y por `locus-v2-metrics.md` §25.5, no puede ser entrada rigurosa de un certificado.
La certificación de `e_2` usa **solo hechos estructurales**:

| Hecho | Fundamento | Usa el valor de `L` |
|---|---|---|
| `o_1` es una componente periódica completa y rectificable | certificado de `o_1` (§4.4) y cota superior de su longitud por la envolvente de la primera derivada (`L ≤ 2π·sup|F′|`) | no |
| `w_1(t)` definida para todo `t` | ruta adelante sobre una componente periódica completa | no |
| `w_1` continua salvo en `t_N` | continuidad de la integral de una velocidad acotada | no |
| `t_N` exacto | dirección semántica de `N_1` | no |
| salto no nulo en `t_N` | `L ≥ |o_1(a) − o_1(b)| > 0` para dos puntos certificados distintos (cuerda con redondeo exterior) | no; solo `L > 0` |
| macros totales | tabla de operaciones totales (diseño §4.2) | no |

El resultado es `COMPLETE` con una componente abierta `[t_N, t_N + 2π)`. Los **valores** de
`e_2` siguen siendo estimaciones (contrato de estimación de `CU1`, D-CU1-2): la topología se
certifica y las coordenadas se declaran estimadas. Esta es la diferencia principal con la
propuesta de `CU0` (E §6; D §10), que certificaba `e_2` a través de `d_2` (§7, hallazgo F-CU2-7).

## 6. Resumen por locus

| Locus | Régimen (parámetros del autor) | Predicado o evento | Estructura certificada esperada | Capacidades nuevas |
|---|---|---|---|---|
| `o`, `v`, `w`, `a_1` | penetración (`|excCyl| < 3,65694`) | `D > 0` uniforme | `COMPLETE`, una componente periódica cerrada | clase v3 (rayos, recta–circunferencia con rama), periodicidad estructural |
| `o`, `v`, `w`, `a_1` | mordedura | `D` con dos raíces simples | `COMPLETE`, un arco cerrado que cruza la costura | lo anterior + `UNIFORMLY_INVALID` + transiciones certificadas + arcos circulares |
| `o`, `v`, `w`, `a_1` | valor crítico | cero doble de `D` | `NOT_ESTABLISHED` con banda `UNRESOLVED` | ninguna (fallo cerrado) |
| `e_1` | todos | predicados uniformes | `COMPLETE`, una componente periódica cerrada; pliegues certificados en 0 y `±π` | clase v3 + chorros de segundo orden |
| `o_1` | todos | predicados uniformes | `COMPLETE`, una componente periódica cerrada | clase v3 |
| `e_2` | todos | evento estructural de la longitud orientada | `COMPLETE`, una componente abierta `[t_N, t_N + 2π)` | certificado estructural de la función métrica + operaciones afines totales (macros) |

## 7. Hallazgos

| Id | Hallazgo | Consecuencia para `CU2` |
|---|---|---|
| F-CU2-1 | La validez puntual de la evaluación se extiende de 4·10⁻⁸ a 4,3·10⁻⁶ más allá de la raíz exacta de `D` (§3.3) | las fronteras se certifican sobre los predicados, nunca por bisección de la validez; la matriz compara con la referencia analítica, no con `preserve.branch_ends` a 10⁻⁷ |
| F-CU2-2 | Cerca del valor crítico (3,657–3,66), el dibujo actual une con un solo subtrazo un hueco real de hasta 5,3 px | B07 incluye uniones falsas, no solo huecos; ningún segmento de dibujo cruza una banda no certificada |
| F-CU2-3 | Un locus oculto por la regla de publicación sigue evaluando su última definición publicada (§3.7) | la regla de publicación (H01) no deja definiciones obsoletas a la vista de ningún consumidor |
| F-CU2-4 | En el extremo t = 1,0418226911 (excCyl 3,7), el corte aislado evalúa un punto válido mientras que `Q` y `R` del núcleo vivo no están definidos; en el otro extremo los dos están definidos con `|QR|` = 0 | la validez puntual cerca de una frontera depende del redondeo y del contexto; solo el certificado decide |
| F-CU2-5 | La raíz de la costura de `e_1 ∩ c_1`, fijada en `CU0` como `TRANSVERSE_ESTABLISHED`, es geométricamente una tangencia (`h` no cambia de signo; `e_1` es simétrica también respecto de `t = π`) | observación para `CU3`: «la raíz transversal de la costura se conserva» conservaría un artefacto |
| F-CU2-6 | El manifiesto del corpus describe el cilindro como vertical; es oblicuo (generatrices frontales a 40°) | corrección documental del manifiesto en un paquete autorizado; no afecta a las referencias |
| F-CU2-7 | La propuesta E de `CU0` certificaba `e_2` a través del valor de `d_2`, que es de clase 3 | `CU2` certifica la topología de `e_2` con hechos estructurales de la función métrica (§5.3) |

## 8. Reproducibilidad del sondeo

Clase de sondeo en el paquete `org.geocedg.desktop`, compilada con `javac` contra el classpath
de pruebas Desktop y ejecutada fuera de Gradle, con el directorio de trabajo en el directorio
temporal de la sesión. Hace lo siguiente:

1. abre la variante sin `e_2` del modelo (`PreG9BCu0Corpus.withoutE2`);
2. recorre excCyl ∈ {3,6; 3,65; 3,656; 3,657; 3,658; 3,66; 3,7; 3,85; 3,9; 3,95; 3,96; 4,0; 4,5;
   5,0} con `PreG9BCu0References.existenceEvidence` (720 muestras y bisección hasta 10⁻¹³);
   desde 3,96 mueve antes el conductor a −1;
3. compara los extremos con `t_c ± β`;
4. mide el orden de `Q` y `R` a lo largo del rayo en 48 conductores, la continuidad en la
   costura y la velocidad paramétrica (diferencias centrales con `h` = 10⁻⁶, 4 000 muestras).

El sondeo no modifica el repositorio ni el modelo versionado.
