# PRE-G9B-CU2 — Caracterización complementaria y análisis de certificación del modelo del autor

| Campo | Valor |
|---|---|
| Carácter | Análisis candidato de la preparación de `CU2`: caracterización complementaria (solo donde hacía falta) y explicación constructiva y matemática de la certificación de los siete Locus V2 del modelo del autor. Evidencia y diseño, no autoridad normativa |
| Estado | `CU2 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false`; `TECHNICAL_CANDIDATE_STATE = FROZEN`; `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT` |
| Encargo | [registro de cierre de `CU1`](../validation/pre_g9b_cu1_closeout_record.md) §7 (preparación de `CU2`) |
| Base | cierre documental de `CU1` `27bf652f176e57ced3807b82afe14fc00b7586a9` |
| Modelo | `models/regression/pre-g9b-cu/original/InterCilConoOblique.cedg` (SHA-256 `1ae6f512…890456c`, igual al manifiesto); `continuous=false` |
| Producto caracterizado | el código de producto aprobado de `CU1` (`143f5d37`), idéntico en el cierre |
| Revisión | 2026-10-20, revisión documental focalizada (instrucción autoral «PRE-G9B-CU2 — Revisión documental focalizada, certificación de degeneraciones y refinamiento normativo»): §3.2 a §3.6 y §4.3 revisados, §3.8 nuevo (P1), hallazgos F-CU2-8 a F-CU2-11 y dos sondeos nuevos (§8). Cierre de coherencia (2026-10-20): §3.8.2 y §3.8.4 precisados (C3), §3.9 nuevo (C1), hallazgos F-CU2-12 a F-CU2-14 y dos sondeos más (§8) |

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
circunferencia `p`.

**Forma estructural.** El rayo `t` es `X = P + λ (N − P)`. Su intersección con `p` es la ecuación
`a λ² + 2 b′ λ + c = 0`, con `a = |N − P|²`, `b′ = (N − P)·(P − B)` y `c = |PB|² − rbCone²`. El
discriminante reducido es

`D = b′² − a c = rbCone²·|N − P|² − ((N − P) × (P − B))²`.

Con `P` exterior a `p` se factoriza exactamente. Si `θ` es el ángulo entre `N − P` y `B − P`,

`D = |N − P|²·|PB|²·(sin²θp − sin²θ) = |PB|²·[|N − P| sin(θp − θ)]·[|N − P| sin(θp + θ)]`,

y cada corchete es la distancia con signo de `N` a una de las dos tangentes desde `P` a `p`,
positiva del lado de `B`. Es la ecuación del par de tangentes desde `P` (Joachimsthal):
`D(N) = |PB|²·ℓ_A(N)·ℓ_B(N)`, con `ℓ_A` y `ℓ_B` funciones afines de `N`. Como `P` y `B` están en
`i`, `C_c = B + excCyl·u` y `N(t) = C_c + rbCylinder (cos t, sin t)`:

- `ℓ_A(t) = (rbCone − excCyl·cos θp) − rbCylinder·cos(t − t_c)`;
- `ℓ_B(t) = (rbCone + excCyl·cos θp) − rbCylinder·cos(t + t_c)`.

Cada factor es la distancia de un punto que recorre `g_1` a una recta fija, de la forma
`h − r cos(t − τ)`: su signo lo deciden los márgenes `h − r` y `h + r`. El sondeo de esta revisión
confirma la identidad en el modelo con error relativo ≤ 8,7·10⁻¹⁶, y los valores de `h` y `τ` de
las fórmulas con todas las cifras impresas (§8).

Aquí `θp` es el semiángulo con que se ve `p` desde `P`: `sin θp = rbCone / |PB|`,
`cos θp` = 0,957083363, y `t_c = π/2 − θp`. Entonces:

- **Penetración:** si `|excCyl|·cos θp < rbCone − rbCylinder`, es decir
  `|excCyl| < 3,656943727`, los dos factores son positivos en todo el círculo; la intersección
  son dos curvas cerradas, `{v, w}` (`p1`) y `{o, a_1}` (`p2`), y cada locus es una componente
  periódica completa.
- **Mordedura:** para `3,656943727 < |excCyl| < 6,7915`, `ℓ_A` (o `ℓ_B` si `excCyl < 0`) cambia
  de signo en dos raíces simples y cada locus deja de existir en el intervalo de conductor
  `(t_c − β, t_c + β)`, con `t_c = π/2 − θp` = 1,2767651295 (`−t_c` si `excCyl < 0`) y
  `β = arccos((rbCone − excCyl·cos θp)/rbCylinder)`.
- **Sin intersección:** para `|excCyl| > (rbCone + rbCylinder)/cos θp` = 6,7915 (fuera del
  deslizador), `D < 0` en todo el círculo; en la igualdad, un único punto (tangencia exterior).
- **En los extremos** `Q = R = T+`, el punto de tangencia de la recta límite: raíz doble y
  extremo **cerrado**. `o` y `v` comparten sus extremos, igual que `w` y `a_1`. Todos los
  extremos están sobre la generatriz fija `VT+`.
- **Singularidad:** cada locus tiene en los extremos una singularidad de raíz cuadrada,
  `|F′(t)| ~ (t − t±)^(−1/2)`, con longitud finita. Por eso la frontera se certifica sobre `D`,
  que es regular, y no sobre `F`.
- **Valor crítico** `excCyl = 3,656943727…`: `ℓ_A` alcanza el cero sin cambiar de signo y `D`
  tiene un cero doble en `t_c`. No es una raíz simple, pero tampoco una pérdida de existencia:
  su estudio y su certificación están en §3.8.

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
sentido contrario. El evaluador trata la recta como tangente si `|dis| ≤ δ`, con
`δ = mín(10⁻⁵, máx(1, |2d| + |u| + |w|)·ε)` y `ε` desde 10⁻¹⁵, multiplicado por 10 mientras los
puntos no superen la comprobación de incidencia (`AlgoIntersectLineConic.java:617-640, 703-704,
741-746`). El exceso crece como `δ/|D′(t*)|` cuando `D′` tiende a cero cerca del valor crítico. Los extremos de `CU0` (`preserve.branch_ends`) son, por tanto, evidencia de la
evaluación y no fronteras exactas: difieren de la raíz exacta de `D` en más de su tolerancia de
10⁻⁷ (1,6·10⁻⁷ en 3,7). La matriz de `CU2` compara las fronteras certificadas con la referencia
analítica y no con esos valores (§7, hallazgo F-CU2-1).

### 3.4 Orden de las raíces, costura y regularidad

- **Orden (P4):** lo define el algoritmo, no una muestra.
  - `intersectLineConic` parametriza la recta como `X = p + s (g_y, −g_x)` con sus coeficientes
    normalizados, escalados por potencias de 2 sin cambiar de signo (`GeoLine.java:1620-1649`), y
    devuelve primero la raíz de `s` menor (`AlgoIntersectLineConic.java:758-777`). Para
    `Ray(P, N)` los coeficientes son `(P_y − N_y, N_x − P_x, ·)` (`GeoVec3D.java:295-299`), así que
    `(g_y, −g_x) = N − P` y la raíz bruta 0 es la de parámetro de rayo `λ` menor, la rama `−√D`.
  - La salida `i` es la raíz bruta `permutation[i]` (`AlgoIntersectLineConic.java:467-484`). La
    permutación es un **estado** del algoritmo: en la primera evaluación con dos raíces sobre los
    caminos se toma el orden bruto, compactando las raíces que no están en los caminos
    (`firstIntersection`, 526-547); después se ajusta por cercanía a las posiciones anteriores
    (`setNearTo`, 550-556) en cada `initForNearToRelationship`, que se llama al crear, al cargar y
    en `Construction.updateConstruction` (`Construction.java:1474-1496`). Las intersecciones de
    una sola salida además comunican al algoritmo padre la posición guardada en el XML
    (`AlgoIntersectSingle.java:225-235`).
  - Estado leído en el modelo (sondeo, §8): para `{Q, R}`, `permutation = [0, 1]` tras la carga,
    tras `updateConstruction` con el conductor en el hueco y tras saltos lejanos del conductor;
    `Q` es siempre la raíz de `λ` menor. `H_1` toma la salida 0 de una permutación `[0, 1]` sobre
    `i` (la intersección de `x` mayor). `G_1`, sobre el rayo `s_1` que sale del centro de `c_1`,
    nunca ve dos raíces en el rayo: su algoritmo sigue en `firstIntersection` y la salida 0 es la
    única raíz del rayo (hallazgo F-CU2-8).
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
   única en un corchete de anchura `≤ δ`. Como `D` es aquí un predicado de primer nivel (sus
   entradas son constantes y el propio conductor), el certificado algebraico exacto (§3.8)
   da además todas sus raíces con su multiplicidad. La frontera es cerrada, porque en ella la
   ecuación en `λ` tiene una raíz doble y `Q = R` existe.
4. **Continuidad de la selección:** en cada caja válida la rama `−√D` (o `+√D`) es continua.
   La regla de selección es la misma en todo el intervalo porque procede del estado del
   algoritmo (§3.4), fijo durante la revisión, y no de una muestra.
5. **Costura:** todas las operaciones del corte son funciones de la configuración actual (modo
   no continuo, sin historia), y el conductor es el punto de la circunferencia `g_1`. Por tanto
   `F` es una función sobre el círculo y la continuidad certificada en un entorno de `±π`
   certifica el paso por la costura (§4.4 del diseño).
6. **Resultado:**
   - en penetración: `COMPLETE` con una componente periódica cerrada;
   - en mordedura: `COMPLETE` con **una** componente, el arco `[t_c + β, t_c − β + 2π]` con
     ambos extremos cerrados, que cruza la costura;
   - en el valor crítico: `COMPLETE` con una componente periódica cerrada y un parámetro
     singular certificado en `t_c`, por el certificado algebraico exacto (§3.8). Solo si ese
     certificado no se aplica o agota su presupuesto: `NOT_ESTABLISHED` con una banda
     `UNRESOLVED` explícita alrededor de `t_c`, sin componente inventada.

La evaluación puntual de `F` dentro del corchete de una frontera no está garantizada (§3.3). El
dibujo llega hasta la imagen del extremo `F(t*)`, que en la raíz doble es `T+` proyectado, con
la parametrización local `t = t* ± s²` (§5.2 del
[diseño de consumo y representación](pre_g9b_cu2_consumption_and_representation.md)).

### 3.6 Casos degenerados

- **Valor crítico** (`β = 0`): cero doble de `D` sin cambio de signo. Las dos curvas se tocan
  en un punto doble, y cada locus tiene allí una esquina, porque la selección pasa de una rama
  analítica a la otra. No hay pérdida de existencia ni de continuidad; ninguna prueba de raíz
  simple lo certifica, pero sí el certificado algebraico exacto (§3.8). Cerca del valor crítico
  (3,657 a 3,66) el producto actual dibuja **un solo subtrazo a través de un hueco real**
  (hallazgo F-CU2-2).
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

### 3.8 Certificación del valor crítico de la mordedura (P1)

#### 3.8.1 Geometría y estructura

El valor crítico es la configuración en que `g_1` es tangente a la tangente `T_A` desde `P` a
`p`: `h_A = rbCylinder`, es decir `excCyl·cos θp = rbCone − rbCylinder`. En el espacio, el plano
auxiliar que contiene `T_A` es tangente a la vez al cono y al cilindro: las superficies se tocan
en un punto y la curva de intersección tiene allí un punto doble.

En esa configuración, por §3.2,

`D(t) = |PB|² · 2 rbCylinder · sen²((t − t_c)/2) · ℓ_B(t)`, con
`ℓ_B(t) ≥ h_B − rbCylinder = 2 (rbCone − rbCylinder) = 7`.

Es la factorización `D = g (t − t_c)² H(t)` que la instrucción proponía como ilustración, ahora
deducida de la construcción: `D ≥ 0` en todo el círculo, `D = 0` solo en `t_c`, con multiplicidad
exactamente 2.

| Cuestión (instrucción §3.1) | Resultado |
|---|---|
| 1. Expresión de `D(t; μ)` | §3.2: `|PB|²` por las distancias de `N(t)` a las dos tangentes desde `P` |
| 2. Condición crítica | `g_1` tangente a `T_A`: `h_A = rbCylinder` |
| 3. Multiplicidad en el conductor | 2 en `t_c`; ninguna otra raíz |
| 4. Signo de `D` a ambos lados | positivo a los dos lados |
| 5. `Q` y `R` cuando `D = 0` | existen y coinciden en `λ = −b′/a`; están en el rayo porque `c > 0` (`P` exterior) y `b′ < 0` |
| 6. Continuidad de cada rama seleccionada | `λ_Q = (−b′ − √D)/a`, con `√D = |PB|·√(2 rbCylinder·ℓ_B)·|sen((t − t_c)/2)|`: continua y no derivable en `t_c` |
| 7. Puntos derivados de `o`, `v`, `w` y `a_1` | existen y son continuos: los rayos desde `H` e `I` nunca limitan (§3.2) |
| 8. Topología | tabla siguiente |

| Configuración | `o`, `v`, `w`, `a_1` |
|---|---|
| Penetración (`h_A > rbCylinder`) | cada locus, una componente periódica cerrada y regular; dos curvas espaciales separadas |
| Crítica (`h_A = rbCylinder`) | cada locus, una componente periódica cerrada y continua, con un parámetro singular certificado en `t_c`; `o(t_c) = v(t_c)` y `w(t_c) = a_1(t_c)`: las dos curvas espaciales se tocan en el punto doble |
| Mordedura (`h_A < rbCylinder`) | cada locus, un arco con extremos cerrados en `t_c ± β` que cruza la costura; `o` y `v` comparten los extremos, como `w` y `a_1`: una sola curva espacial |

**Relación de las ramas.** Las ramas analíticas `λ_± = (−b′ ± s(t))/a`, con la raíz con signo
`s(t) = |PB|·√(2 rbCylinder·ℓ_B)·sen((t − t_c)/2)`, se cruzan transversalmente en `t_c`. La
regla del algoritmo (raíz de `λ` menor, §3.4) pasa de una a otra en `t_c`. Como `PN(t_c)` es
tangente común a `p` y a `g_1`, la dirección de la recta es estacionaria en `t_c`: `Q` llega al
punto de tangencia y vuelve sobre `p`, con tangentes laterales opuestas (giro de 180° en la
configuración sintética de §3.8.3). Los loci combinan `Q` o `R` con `N`, que se mueve con
regularidad, y tienen en `t_c` una **esquina**, no un pliegue. En la configuración binary64 más
próxima por el lado de la penetración, el giro medido en `t_c` es de 133,7° para `o` y `v` y de
147,3° para `w` y `a_1`, estable entre `h` = 10⁻³ y 10⁻⁵ (§8).

| Distinción (instrucción §3.1) | En el modelo |
|---|---|
| aparición o desaparición de dos raíces simples | al pasar de penetración a mordedura, el cero doble se desdobla en dos raíces simples reales `t_c ± β` |
| raíz doble aislada de un predicado de validez | en el valor crítico, `D ≥ 0` con un cero doble aislado: el predicado se cumple en todo el círculo |
| tangencia geométrica | `PN(t_c)` es tangente común a `p` y a `g_1`; en el espacio, cono y cilindro son tangentes |
| punto singular de la parametrización | `λ_Q` no es derivable en `t_c` y `Q` invierte su movimiento sobre `p` |
| cambio de regularidad sin pérdida de continuidad | los cuatro loci: continuos, con una esquina en `t_c` |
| pérdida real de existencia o continuidad | solo en la mordedura, en `(t_c − β, t_c + β)`; nunca en el valor crítico |

#### 3.8.2 Técnicas de certificación estudiadas

| Técnica | Aplicabilidad | Qué puede probar |
|---|---|---|
| A. Factorización estructural | `D` de una recta por un punto fijo exterior `P` y un punto que recorre una circunferencia fija | el signo de cada factor a partir de dos márgenes escalares, `h − r` y `h + r`; las raíces en forma cerrada, `τ ± arccos(h/r)` |
| B. Punto estacionario de `D` | cualquier `D` con envolventes de `D′` y `D″` | existencia y unicidad del mínimo `t*` (Krawczyk sobre `D′`, con una envolvente de `D″` que excluye el cero) y el signo de `D(t*)` cuando su envolvente lo excluye. Nunca `D(t*) = 0` |
| C. Intervalos para raíces múltiples | según la estructura disponible | el aislamiento ordinario de raíces simples (cambio de signo, derivada que no se anula, Newton de intervalos o Krawczyk) no certifica por sí solo una multiplicidad: con datos solo aproximados, un cero doble no es estable frente a perturbaciones arbitrariamente pequeñas. Los procedimientos verificados para raíces múltiples añaden estructura (multiplicidad conocida, deflación, factorización, información derivativa) o verifican la raíz múltiple de un problema **perturbado** (Rump y Graillat, *Verified error bounds for multiple roots of systems of nonlinear equations*, Numerical Algorithms, 2010) |
| D. Información constructiva exacta | constantes capturadas en binary64, que son racionales exactos (`certified-construction-model` §3), y conductor sobre una circunferencia | con `u = tan(t/2)`, `(1 + u²)²·D` es un polinomio de grado 4 con coeficientes racionales exactos. El máximo común divisor con su derivada y la sucesión de Sturm dan **exactamente** el número de raíces reales, sus multiplicidades y el signo entre ellas; `t = ±π` se trata aparte |

Entre las técnicas estudiadas, D es la que establece la igualdad del caso crítico con las
constantes capturadas: la decide con aritmética exacta, sin hipótesis numéricas. A lo explica y
da las referencias analíticas; B sirve, para casos estrictos, donde D no se aplica; un
procedimiento de C que añada estructura podría ser una alternativa en otras clases de
predicados, pero aquí no aporta nada que D no dé. La incidencia tangencial no es información
constructiva adicional en el modelo del autor: el valor crítico no está construido, sino que lo
produce un deslizador.

#### 3.8.3 Configuración crítica exacta frente a binary64

La referencia semántica son los racionales binary64 de las constantes capturadas
(`certified-construction-model` §3). Resultado del método D sobre las configuraciones capturadas
del modelo (sondeo, §8):

| Configuración | Definición | En el modelo del autor |
|---|---|---|
| matemáticamente crítica | `h_A = rbCylinder` exactamente, con las constantes capturadas | no es representable: no hay raíz doble en ningún valor binary64 de `excCyl` |
| valores representables más próximos | los binary64 entre los que cambia la clasificación | `0x1.d416bb67a38bbp1` (3,6569437271164964): penetración, sin raíces reales. `0x1.d416bb67a38bcp1` (3,656943727116497): mordedura, dos raíces simples separadas 2,4·10⁻⁸ |
| ligeramente anterior | `h_A > rbCylinder` | penetración, decidida exactamente |
| ligeramente posterior | `h_A < rbCylinder` | mordedura, decidida exactamente; hueco de anchura `≈ 2√(2 |h_A − r| / r)` |
| no decidible dentro del presupuesto | — | ninguna: de 2 a 53 ms por configuración, con coeficientes de hasta 217 bits |

Ningún binary64 de `excCyl` es exactamente crítico. Con `P`, `B` y los radios capturados fijos,
el margen `h_A − rbCylinder` es una función afín y estrictamente monótona de `C_c.y`
(`C_c.x = B.x` exactamente), y `C_c.y = fl(B.y + excCyl)` no decrece con `excCyl`. El margen es
positivo en `0x1.d416bb67a38bbp1` y negativo en el binary64 siguiente, `0x1.d416bb67a38bcp1`, de
modo que no se anula en ningún valor representable.

Consecuencias (hallazgo F-CU2-9):

- la fórmula cerrada evaluada en `double` clasifica como penetración el primer valor de
  mordedura: cerca del valor crítico ni siquiera la referencia analítica decide en coma flotante;
- el evaluador devuelve `Q` y `R` definidos en `t_c` en todas esas configuraciones, también
  dentro del hueco exacto, por su tolerancia de tangencia (§3.3). La existencia la decide el
  certificado, no el evaluador.

Las constantes capturadas no cumplen exactamente las relaciones ideales: `P.y` difiere de `B.y`
en una unidad de la última cifra (hallazgo F-CU2-10). El certificado usa los valores capturados;
las fórmulas cerradas son referencias.

**Construcción sintética reproducible.** El valor crítico exacto exige datos que lo conserven en
aritmética racional. Con enteros:

- `p` de centro `(0, 0)` y radio 25; `P = (−25, 50)`, con tangentes `3x + 4y = 125` y `x = −25`;
- `g_1` de centro `(35, −45)` y radio 40, tangente interior a la primera.

El método D prueba una sola raíz real, de multiplicidad 2, en `u = 1/2` (`t = atan2(4, 3)`), con
`D ≥ 0` a ambos lados. Con radio `40 + 2⁻ᵏ` hay dos raíces simples y con `40 − 2⁻ᵏ` ninguna,
para `k` hasta 200 (raíces separadas `≈ 2⁻¹⁰⁰`). Con `g_1` de centro `(83, 19)` y radio 40,
tangente exterior, prueba un punto aislado de existencia: cero doble con `D ≤ 0` a ambos lados,
que el diseño representa como componente puntual
([diseño de certificación](pre_g9b_cu2_hybrid_certification_design.md) §3.5).

Una tangencia que la construcción calcula con redondeo, por ejemplo un radio definido como la
distancia de un centro a una tangente, se captura redondeada y se certifica como su configuración
redondeada. Conservar la relación exigiría capturar simbólicamente el subgrafo independiente del
conductor; no se propone para `CU2`.

#### 3.8.4 Resultado

El caso crítico tiene un certificado: el certificado algebraico exacto del
[diseño de certificación](pre_g9b_cu2_hybrid_certification_design.md) §4.6. Establece la
existencia matemática en todo el círculo, la multiplicidad 2 en `t_c`, la inclusión de `t_c` en
la componente, la relación de las ramas a ambos lados y la completitud. En el modelo del autor
decide exactamente cualquier valor de `excCyl`: penetración o mordedura. `NOT_ESTABLISHED`
queda como fallo cerrado cuando el certificado no se aplica (predicado que no es de primer
nivel) o agota su presupuesto.

Que la construcción seleccionada sea continua en `t_c` exige además que las salidas que definen
los puntos trazados estén realmente definidas en la raíz coincidente. En el modelo lo están,
porque `Q` y `R` tienen etiqueta (§3.9); con una segunda salida sin etiqueta no lo estarían, y la
certificación completa no sería posible.

### 3.9 Salidas efectivas en la raíz coincidente (C1)

GeoGebra anula la segunda salida de una intersección cuando no tiene etiqueta y coincide con la
primera dentro de 10⁻⁸ en cada coordenada (`AlgoIntersect.avoidDoubleTangentPoint`; detalle en el
[diseño de certificación](pre_g9b_cu2_hybrid_certification_design.md) §4.7). Un tercer sondeo y
una comprobación posterior (§8) miden el ciclo completo en el modelo y en construcciones
añadidas a su copia de trabajo:

| Caso | Resultado |
|---|---|
| etiquetado de `{Q, R}` | etiquetadas en el vivo y en las copias de los cortes de `o` y `v`, también tras recargar el XML |
| raíz coincidente con ambas salidas etiquetadas | `R` nunca se anula: 0 de 201 muestras indefinidas en `|t − t_c| ≤ 10⁻⁸`, tanto en la penetración más próxima como en la mordedura más próxima. El locus de `R` tampoco, en la penetración más próxima; en la mordedura más próxima su corte no se resincronizó (hallazgo F-CU2-12) y el dato no cuenta |
| segunda salida sin etiqueta (`R2 = Intersect(p, t2, 2)`, con `t2 = Ray(P, N)`) | las salidas auxiliares del algoritmo padre no tienen etiqueta. En la penetración más próxima, `R2` está indefinida en 49 de 201 muestras dispersas de `|t − t_c| ≤ 10⁻⁸`, con `Q` y `R` iguales en ellas (ventana de tangencia); el locus de `R2` lo está en 55. En la mordedura más próxima, `R2` está indefinida en las 201 |
| frontera simple de mordedura (excCyl 3,7) | `R` está definida hasta 1,6·10⁻⁷ dentro del hueco exacto; `R2` deja de estarlo en la propia frontera |
| raíz doble en la primera inicialización | `R3 = Intersect(p, t3, 2)` creada con el conductor en `t_c`: el algoritmo sigue en `firstIntersection` (dos raíces iguales no inicializan) y `R3` está indefinida; al apartar el conductor se inicializa en orden bruto y `R3` es la raíz de `λ` mayor; al volver a `t_c`, permutación `[0, 1]` fija y `R3` de nuevo indefinida |
| conductor sin intersección al crear | `R4` creada en el hueco: indefinida, sin inicializar; al salir, orden bruto |
| recálculo y recarga del XML (como deshacer) | permutación `[0, 1]`, etiquetado y comportamiento de la banda idénticos |
| copia del corte frente al vivo | igual etiquetado y permutación para `{Q, R}`; para `J/K` (`Intersect(p, i)`), permutación `[1, 0]` en el vivo y en la copia: la salida `J` es la raíz bruta 1 |
| igualdad entre selección certificada y resultado operativo | `Q` es la raíz de `λ` menor y `R` la mayor en todos los estados comprobados |

Conclusiones:

1. En el modelo del autor la raíz doble certificada y las salidas efectivas son coherentes: en
   `t_c` las dos salidas están definidas y coinciden, y los cuatro loci son continuos con su
   esquina. La afirmación de completitud de §3.8 se mantiene, con la precondición de que la copia
   del corte reproduzca el etiquetado y la permutación del algoritmo vivo.
2. Con una segunda salida sin etiqueta, la definición efectiva cerca de una raíz coincidente
   depende de la tolerancia y del redondeo, y difiere entre el vivo y el corte (49 frente a 55
   muestras). No se puede certificar ni la existencia ni la continuidad en esa banda: el locus que
   dependa de esa salida queda `NOT_ESTABLISHED`, con la banda `UNRESOLVED` y componentes locales
   a ambos lados.
3. El valor límite de la raíz coincidente está demostrado, pero no es una salida disponible.
   Usarlo como salida cambiaría la semántica del Locus V2 respecto del objeto vivo (decisión
   D-CU2-18).

Además, la comprobación posterior encontró que el corte de `lr2 = LocusV2(R2, N)` no se
resincronizó tras un cambio de un ulp de `excCyl`: su revisión no cambió y su copia de `excCyl`
conservó el valor anterior, de modo que el locus se evaluó con la configuración previa
(hallazgo F-CU2-12).

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

### 4.3 Pliegues de `e_1` (P3)

**Simetría demostrada.** La simetría respecto de `i` deja invariante `e_1`:
`e_1(−t) = e_1(t)`. Prueba sobre la construcción:

- el conductor `E_1 = B + rbCone (cos t, sin t)` recorre `p`, cuyo centro `B` está en `i`, y la
  simetría respecto de `i`, que es horizontal, lleva `t` a `−t`;
- `F_1 = Intersect(h, p_1)`, con `p_1` la vertical por `E_1`, solo depende de `x(E_1)`, que la
  simetría conserva;
- `d_1 = Circle(I, E_1)` tiene radio `|I E_1|`, invariante porque `I` está en `i`, y
  `H_1 = Intersect(d_1, i, 1)` solo depende de ese radio y del estado de selección (§3.4);
- `I_1`, `s_1`, `G_1`, `t_1` y `q_1 = Ray(H, F_1)` dependen de `F_1` y `H_1`, y
  `J_1 = Intersect(q_1, t_1)` también.

La prueba usa hechos exactos de las constantes capturadas, que el sondeo comprueba: `h` e `i`
horizontales y `k` vertical con coeficientes nulos exactos, `I.y = B.y`, y el conductor
parametrizado con los ejes propios de `p` iguales a `(1, 0)` y `(0, 1)`. Medido:
`max|e_1(t) − e_1(−t)|` = 6,4·10⁻¹⁵, y `o_1` es simétrica respecto de `i` con el mismo error.

**Pliegues.** De `e_1(−t) = e_1(t)` se sigue `F′(0) = −F′(0)`, luego `F′(0) = 0`, y lo mismo en
`t = π`: son puntos estacionarios exactos. Las segundas diferencias dan `|F″(0)|` ≈ 3,93 y
`|F″(π)|` ≈ 1,27, es decir, pliegues no degenerados (§8). La velocidad de 1,3·10⁻⁷ que se midió
«en `t = ±π`» se tomó en `t = ±(π − 10⁻⁷)`: es `|F″(π)|·10⁻⁷` y confirma `F′(π) = 0`. Por la
simetría, las dos mitades recorren el mismo arco con tangentes laterales opuestas: la imagen se
pliega en `M_1` y en (1,60844, 10,13096), y el giro de 180° de B06 es real.

**Certificación.** Una envolvente de `F′` que contiene el cero no prueba que exista un punto
estacionario: para una curva plana, `F′(t) = 0` son dos ecuaciones con una incógnita, que
genéricamente no tienen solución. El procedimiento del
[diseño de certificación](pre_g9b_cu2_hybrid_certification_design.md) §5 establece por separado
la existencia (simetría estructural verificada sobre el programa capturado), el aislamiento
(una componente de `F″` excluye el cero en una caja), la no degeneración (`F″(t₀) ≠ 0`), la
continuidad y la naturaleza del pliegue (tangentes laterales opuestas). Sin esa simetría, un
giro cerrado no es un pliegue certificado.

`o_1` es regular: velocidad mínima 1,60.

### 4.4 Certificación

Programa de clase v3 con:

- la circunferencia por centro y punto (`d_1`);
- intersecciones recta–cónica indexadas con raíz seleccionada (`AlgoIntersectSingle` sobre
  `AlgoIntersectLineConic`);
- rayos con predicado uniforme;
- operaciones de clase v1.

Ningún predicado cambia de signo, y la función es de la configuración actual: `COMPLETE` con
una componente periódica cerrada para cada locus. Las selecciones de `H_1` y `G_1` proceden del
estado de sus algoritmos (§3.4). La descomposición certificada de `o_1` es una sola componente
periódica cerrada igual al dominio, la misma que cita el selector guardado de
`N_1 = Point(o_1, text2, v_1)`: la correspondencia es estructural, por igualdad de la
descomposición, y no por contención (decisión E-3 revisada).

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
  en el origen y abierta en el otro extremo; con `full-cycle`, al revés. Sus dos extremos
  corresponden al mismo punto espacial y al mismo parámetro canónico `t_N`, pero son dos límites
  laterales distintos, `t_N⁺` y `t_N⁻`, separados `L` en el plano: no se identifican y la
  componente **no es periódica** ([diseño del evento](pre_g9b_cu2_oriented_length_event_design.md)
  §4.4).

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
| `o`, `v`, `w`, `a_1` | valor crítico (solo con datos que lo representen exactamente) | cero doble de `D` sin cambio de signo | `COMPLETE`, una componente periódica cerrada con un parámetro singular certificado en `t_c`, porque `{Q, R}` están etiquetadas y el corte reproduce su etiquetado (§3.9) | certificado algebraico exacto; lectura del etiquetado |
| `o`, `v`, `w`, `a_1` | binary64 contiguos al valor crítico | `D` sin raíces o con dos raíces simples a 10⁻⁸ | penetración o mordedura, decididas exactamente | certificado algebraico exacto |
| `e_1` | todos | predicados uniformes | `COMPLETE`, una componente periódica cerrada; pliegues certificados en 0 y `±π` | clase v3 + simetría estructural + chorros de segundo orden |
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
| F-CU2-8 | La salida de una intersección recta–circunferencia es una raíz bruta, ordenada por el parámetro a lo largo de la dirección algorítmica de la recta, a través de una permutación que es estado del algoritmo; esa permutación se reinicializa por cercanía al crear, cargar y en `updateConstruction`, también en la copia del corte de cada revisión. Con un solo punto en los caminos (`G_1`), el algoritmo no sale de la inicialización y compacta (§3.4) | la regla de raíz se lee del estado del algoritmo y es entrada de la revisión; la copia del corte debe reproducir el estado del algoritmo vivo, o la selección falla cerrada |
| F-CU2-9 | Cerca del valor crítico la clasificación exacta cambia entre dos binary64 consecutivos; la fórmula cerrada en `double` clasifica mal el primer valor de mordedura, y el evaluador da puntos dentro del hueco exacto (§3.8.3) | la existencia la decide un certificado exacto; el evaluador no es frontera ni prueba de existencia |
| F-CU2-10 | Las constantes capturadas no cumplen exactamente las relaciones ideales: `P.y` difiere de `B.y` en una unidad de la última cifra | el certificado usa los valores capturados; las fórmulas cerradas son solo referencias |
| F-CU2-11 | En el valor crítico la raíz seleccionada `Q` invierte su movimiento sobre `p` y los loci tienen una esquina de 133,7° (`o`, `v`) y 147,3° (`w`, `a_1`), sin pérdida de continuidad (§3.8.1) | el certificado publica un parámetro singular; el dibujo lo trata como vértice exacto, sin unirlo ni suavizarlo |
| F-CU2-12 | La huella del corte de `CU1` usa la serialización XML, con 15 cifras significativas: un cambio de un ulp de `excCyl` no publicó revisión de `lr2 = LocusV2(R2, N)` y su corte conservó el valor anterior (§3.9) | contradice la «exact fingerprint» de `locus-v2-semantics.md` v1.2; los certificados por revisión quedarían obsoletos cerca del valor crítico; corrección pendiente de decisión (D-CU2-20) |
| F-CU2-13 | GeoGebra anula una segunda salida sin etiqueta que coincide con la primera dentro de 10⁻⁸; cerca de una raíz coincidente la definición efectiva es dispersa y difiere entre el vivo y el corte (§3.9) | el certificado no declara continuidad a través de esa banda: `UNRESOLVED` acotada y `NOT_ESTABLISHED` global para los loci afectados (D-CU2-18) |
| F-CU2-14 | En el modelo, `J/K = Intersect(p, i)` tiene la permutación `[1, 0]` en el vivo y en el corte: la salida `J` es la raíz bruta 1 (§3.9) | el estado de selección no es la identidad en general; la captura lo lee (P4) |

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

La revisión focalizada añadió dos sondeos de investigación con el mismo procedimiento:

1. **Caso crítico y simetría.**
   - Lee las constantes capturadas y comprueba sus relaciones exactas (coeficientes de `h`, `i`
     y `k`, `P.y` frente a `B.y`, `I.y`, ejes propios de `p`, parametrización del conductor).
   - Contrasta la factorización de §3.2 en 1 000 conductores para excCyl 2,9, 3,7 y 4,0.
   - Clasifica exactamente, con aritmética racional (`BigInteger`, máximo común divisor y
     sucesión de Sturm del polinomio en `u = tan(t/2)`), excCyl ∈ {2,9; 3,6; 3,65; 3,656;
     3,6569; 3,65694; 3,656943727; 3,657; 3,66; 3,7; 4,0; 5,0} y los trece binary64 centrados
     en el valor crítico calculado en `double`, junto con las configuraciones sintéticas enteras
     de §3.8.3.
   - Comprueba en `t_c` si el evaluador define `Q` y `R`.
   - Lee por reflexión, sin modificarlo, el estado de selección de `AlgoIntersectLineConic`
     tras la carga, tras `updateConstruction` con el conductor en el hueco y tras saltos del
     conductor.
   - Mide la simetría de `e_1` y `o_1` y las segundas diferencias en 0 y `π`.
2. **Esquinas y selecciones simples.** Mide el giro de `o`, `v`, `w` y `a_1` en `t_c` con
   `h` = 10⁻³ a 10⁻⁶ en cinco configuraciones de penetración, la más próxima
   `0x1.d416bb67a38bbp1`, y lee el estado de los algoritmos padre de `H_1` y `G_1`.

El cierre de coherencia añadió otros dos (§3.9):

3. **Salidas efectivas en raíces coincidentes.** Lee por reflexión el etiquetado y la permutación
   de `{Q, R}` en el vivo y en las copias de los cortes de `o` y `v`. Añade a la copia de trabajo
   en memoria `t2 = Ray(P, N)`, `Q2 = Intersect(p, t2, 1)`, `R2 = Intersect(p, t2, 2)`,
   `lr = LocusV2(R, N)` y `lr2 = LocusV2(R2, N)`, y mide la definición de `R`, `R2` y sus loci
   en 201 conductores de `|t − t_c| ≤ 10⁻⁸` en los dos binary64 contiguos al valor crítico, y en
   la frontera de mordedura de excCyl 3,7. Repite el ciclo de inicialización con la raíz doble y
   con el conductor sin intersección, `updateConstruction` y una recarga del XML.
4. **Corte frente a vivo.** Compara la copia de `R2` en el corte de `lr2` con la salida viva,
   junto con la revisión de `lr2` y la copia de `excCyl`, tras cambios de un ulp; lee la
   permutación viva de `J/K`.

Ningún sondeo modifica el repositorio ni el modelo versionado.
