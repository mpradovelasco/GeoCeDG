# PRE-G9B-CU3 — Estudio arquitectónico de la autoridad geométrica constructiva

| Campo | Valor |
|---|---|
| Carácter | Estudio arquitectónico candidato: investigación, análisis de código y documentación candidata. No es normativa, no adopta ninguna alternativa y no autoriza ninguna implementación |
| Instrucciones | «PRE-G9B-CU3 — Decisión autoral tras la auditoría independiente de G-M3» (G-M3-4 confirmada como bloqueo; estudio arquitectónico focal autorizado) y «Hipótesis arquitectónica adicional para el estudio de autoridad geométrica constructiva» (integridad estricta y relajada), ambas de 2026-10-24 |
| Punto de partida | rama publicada `phase/pre-g9b-cu3-implementation` en `1a0903aa53170c16889a2ecd6b373f1f8ec42364`: [informe de la puerta G-M3](../validation/pre_g9b_cu3_m3_gate_report.md) y su evidencia |
| Rama del estudio | `phase/pre-g9b-cu3-authority-study`, local, creada desde `1a0903aa`. Sondeo en el commit `91e78a92959267311b5a3a49978a329c3190db12` (árbol `1b4fb85989ab76fb3161be0a74eb777a1c49edeb`) |
| Sondeo | `PreG9BCu3AuthorityStudyProbe` en `tools/benchmark/pre-g9b-cu/cu3-java/`, con el lanzador `run-cu3-authority-study.ps1`. Reutiliza sin cambios la aritmética exacta del prototipo de G-M3 |
| Evidencia | [`pre-g9b-cu3-constructive-authority-study-evidence.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-constructive-authority-study-evidence.json), SHA-256 `aeab467c0d0a5659cd77078193c33f7a7d9af6408547c64d377337e7df118054`. Es copia byte a byte de la ejecución A; la ejecución B dio el mismo SHA-256 |
| Estado | `CU3` detenido en T2. Ninguna alternativa queda adoptada ni ninguna norma promovida; `selfApproved=false` |

## Resumen

- **La representación binary64 no es una geometría.** En el modelo del autor, la representación
  binary64 hace falsas exactamente 66 de las 160 relaciones que la propia construcción declara.
  Entre ellas, `A ∈ j` para `j = Line[A, C']`, `D_1 ∈ c_1` para `c_1 = Circle[H, D_1]` y
  `M_1 ∈ c_1` para `M_1 = Intersect[c_1, n]`. Una semántica constructiva que recalcula
  exactamente los objetos de regla y compás, y ancla los puntos sobre caminos, hace exactas las 160
  (§1.2).
- **Una circunferencia tiene tres radios exactos distintos.** Para `c_1` son el de la matriz, el
  del semieje y el de su construcción, y los algoritmos heredados usan unos u otros. De las 41
  rectas recalculadas, 29 no son la recta exacta de su construcción (§1.3).
- **La configuración capturada de `CU2` es una representación certificada.** Sus certificados
  deciden sobre los bits capturados, no sobre la geometría ideal (ADR 0038, decisión 7) (§1.4).
- **Reconstruir el objetivo no da una autoridad.** El mismo objetivo `h_2` recibe cuatro
  semánticas exactas distintas según la frontera de su reconstrucción (§1.5).
- **Los cinco contactos obligatorios son exactos en la semántica constructiva:** igualdad exacta,
  `h′(t*) = 0` exacto y `t*` exacto. Lo mismo ocurre en 12 posiciones racionales de `H` y en las
  revisiones dinámicas en que existen. Contra los objetivos binary64 fallan siempre (§1.7).
- **Hay un límite de constructibilidad.** `H` depende de una rotación de 47°, que no es
  constructible. La evaluación exacta demuestra los contactos para el sustituto binary64 de `H`.
  Su verdad para el objeto ideal descansa en el teorema del análisis §5.3, que es estructural y
  vale para todo `H`, pero que el producto todavía no sabe certificar (§1.6).
- **La rotulación de la costura como raíz transversal es refutable sin M3.** Basta la simetría
  estructural exacta del pliegue, que `CU2` ya certifica (§1.7).
- **Alternativas.**
  - **A**, autoridad constructiva compartida, es el único fundamento coherente.
  - **B**, certificados estructurales, solo es válida dentro de A.
  - **C**, revisión coordinada de la captura y de los objetivos, es la vía contractual de A.
  - **La hipótesis de integridad estricta y relajada** es un modelo de evidencia ortogonal al modelo
    de autoridad: compatible con A, imposible como rescate de la autoridad binary64 (§2).
- **Recomendación.** Adoptar como dirección:
  - A en su forma ideal (A2);
  - la vía C, por etapas y versionada, sin cambiar los certificados ni los tokens de `CU2`;
  - B como capa de prueba;
  - los estados `STRICT`, `RELAXED`, `NOT_ESTABLISHED` y `CONTRADICTED` como modelo de integridad,
    con propagación por premisas.

  El primer paso es una solución parcial conservadora compatible con esa dirección, y el segundo,
  una investigación específica de las pruebas genéricas (§4).

## 1. Diagnóstico matemático y arquitectónico

### 1.1 Seis capas que hoy se confunden

| Capa | Qué es | Qué la soporta hoy | Qué debería ser |
|---|---|---|---|
| 1. Definición geométrica constructiva | el objeto que la construcción define desde sus entradas libres | el grafo de dependencias de GeoGebra (`AlgoElement` y sus entradas) | la autoridad geométrica |
| 2. Identidad semántica | qué objeto es, de forma durable e independiente de las coordenadas | identidad durable (ADR 0010), linaje de componentes de `CU2`, tokens y selectores (ADR 0008, 0013 y 0017) | sin cambios: nunca depende de valores numéricos |
| 3. Relaciones demostradas | incidencias, pertenencias, tangencias e igualdades, con su evidencia | grados de `CU3` (solo diseñados), clases de garantía métrica (ADR 0037), naturaleza de la evidencia de `CU2` | una capa explícita de integridad, relativa a la autoridad de la capa 1 |
| 4. Representaciones | estado binary64 del núcleo (coeficientes de `GeoLine`; matriz, semiejes y vector de traslación de una cónica), constantes capturadas de clase v3, encierros e instancias exactas | **hoy hacen de autoridad**: el contrato de intersecciones §2.1 lee el objetivo desde su estado binary64, y el ADR 0039 toma `Σ_L` como autoridad del punto | representaciones subordinadas, con su error declarado o certificado |
| 5. Evaluación dinámica | recálculo por revisión y continuidad entre revisiones | cascada de GeoGebra; certificados de `CU2` por revisión; linaje | recalcula la integridad por revisión, sin persistirla |
| 6. Presentación y exportación | dibujo, DXF, SVG, PDF y LaTeX | representaciones con evidencia de aproximación (`ApproximationEvidence.Guarantee`) | sin cambios: nunca son autoridad (`AGENTS.md` §11.2) |

`AGENTS.md` ya separa estas capas. El locus es el objeto que define la construcción dependiente
(§11.1), una muestra finita «is a derived approximation of L, not the identity of L» (§11.1), y
los datos de dibujo nunca son autoridad de incidencia (§11.2). La confusión está en la capa 4. La
autoridad binary64 de los objetivos procede del contrato de intersecciones §2.1 («an approved
target adapter supplies an authoritative residual», «from the current `GeoLine` coefficients»). La
autoridad de `Σ_L` procede de los ADR 0028 (decisión 3), 0038 (decisiones 7 y 17) y 0039.

### 1.2 La representación binary64 no es cerrada bajo la construcción

**Argumento matemático.** Los valores binary64 son racionales diádicos.

- **Intersecciones.** La intersección de una recta con una circunferencia tiene en general
  coordenadas irracionales: en `ℚ(√Δ)`, y en general fuera de `ℚ`. En general, ningún punto
  binary64 está entonces exactamente en las dos curvas que lo definen.
- **Rectas.** Los coeficientes de una recta por dos puntos se redondean de forma independiente.
- **Circunferencias.** El cuadrado del radio de una circunferencia por un punto se redondea dos
  veces: en la raíz del semieje y en la constante de la matriz.

En general, ninguna representación binary64 puede conservar a la vez todas las relaciones de
incidencia de una construcción de regla y compás. No es un defecto corregible del código; es una
propiedad del sistema numérico.

**Cómo redondea el núcleo heredado** (lectura del código; los archivos están en
`source/shared/common/src/main/java/org/geogebra/common/kernel/`):

| Objeto | Cálculo | Consecuencia |
|---|---|---|
| `Circle[M, P]` | `r = MyMath.length(Px − Mx, Py − My)` (`geos/GeoPoint.java:1109-1113`). Después, `halfAxes[i] = r` y `matrix[2] = −r·r + mx² + my²` (`kernelND/GeoQuadricND.java:321-345`) | el semieje y la matriz representan dos circunferencias distintas, y ninguna es la construida |
| `Circle[M, r]` | el mismo `setSphereNDMatrix` con `r` exacto | semieje exacto; matriz redondeada |
| `Line[A, B]` | `(Ay − By, Bx − Ax, Ax·By − Ay·Bx)`, con cada término redondeado (`geos/GeoVec3D.java:294-299`) | `A` y `B` no cumplen en general la ecuación exactamente |
| `Line[P, l]` y `OrthogonalLine[P, l]` | la normal se copia o se permuta exactamente; el término independiente se redondea; la normalización es por potencias de dos | el paralelismo y la perpendicularidad son exactos; la incidencia de `P`, no |
| Punto sobre una recta | `X = fl(Sx + fl(t·b))`, `Y = fl(Sy − fl(t·a))` | en general, el punto no está exactamente en la recta |
| Punto sobre una circunferencia | `fl(fl(r·cos t) + mx)`, con el semieje | en general, no está en la circunferencia ni en la de la matriz |
| `Rotate[C, α, A]` | `cos` y `sin` en binary64 (`MyMath.cos` lleva a 0 un `|cos| < 10⁻¹⁶`) | el giro no conserva distancias exactamente |
| `Intersect[recta, circunferencia]` | escalera de tolerancias que empieza en `10⁻¹⁵` y redondeo a tangencia por `isEqual(dis, 0, delta)` (`algos/AlgoIntersectLineConic.java`) | la salida es una raíz numérica de la matriz, no del semieje |

**El propio núcleo heredado reconoce el problema.** `AlgoIntersectLineConic` resuelve un caso
especial por listas de incidencia, es decir, por procedencia, y no numéricamente: «Numerical check
does not work, because when a point incidentally stands on g and c, it may not be considered as
special case» (`algos/AlgoIntersectLineConic.java:314-345`). La incidencia por procedencia es ya,
en el código heredado, más fiable que la verificación numérica.

**Censo de procedencia** (sondeo, `provenance_census`). El sondeo extrae las 160 relaciones que la
procedencia de cada objeto declara en el modelo del autor: punto en recta o circunferencia,
pertenencia a la parte limitada, paralelismo, perpendicularidad, centro y radio. Las decide
exactamente en tres lecturas:

| Lectura | Exactamente verdaderas | Exactamente falsas |
|---|---|---|
| binary64 (circunferencias por su adaptador) | 94 | 66 |
| binary64, relaciones con circunferencias leídas por la matriz | 0 de 13 | 13 de 13 |
| constructiva con sustitutos binary64 en la frontera | 156 | 4 (`H ∈ j`, `A_2 ∈ b`, `N ∈ g_1`, `E_1 ∈ p`: puntos sobre caminos no paralelos a los ejes) |
| constructiva con los puntos sobre caminos anclados | **160** | **0** |

Las relaciones falsas en binary64 incluyen definiciones directas:

- `A ∈ j`, siendo `j = Line[A, C']`;
- `D_1 ∈ c_1`, siendo `c_1 = Circle[H, D_1]`;
- `J ∈ p` y `K ∈ p`, siendo `J, K = Intersect[p, i]`;
- `M_1 ∈ c_1` y `M_1 ∈ n`, siendo `M_1 = Intersect[c_1, n]`.

Si la representación binary64 es la autoridad, la construcción contradice sus propias
definiciones en el 41 % de los casos.

### 1.3 Representaciones discrepantes de circunferencias y rectas

**Circunferencias** (`representations.circles`; el centro coincide exactamente en todas):

| Circunferencia | Construcción | Matriz − constructivo | Semieje² − constructivo | Matriz − semieje² |
|---|---|---|---|---|
| `p` | `Circle[B, rbCone]` | `−1,05·10⁻¹⁵` | `0` | `−1,05·10⁻¹⁵` |
| `g_1` | `Circle[C_c, rbCylinder]` | `−4,00·10⁻¹⁶` | `0` | `−4,00·10⁻¹⁶` |
| `c_1` | `Circle[H, D_1]` | `−3,66·10⁻¹⁵` | `+8,24·10⁻¹⁵` | `−1,19·10⁻¹⁴` |
| `d_1` | `Circle[I, E_1]` | `−2,14·10⁻¹⁶` | `+5,64·10⁻¹⁵` | `−5,85·10⁻¹⁵` |

`c_1` tiene, por tanto, tres circunferencias exactas distintas:

- la de su matriz, `A4² + A5² − A2`: la usan la intersección heredada y la captura de clase v3;
- la de su semieje: la usan los puntos sobre el camino, el adaptador de objetivo y `isOnFullConic`;
- la de su construcción, `|HD_1|²`.

Los coeficientes implícitos `A0 = A1 = 1` y `A3 = 0` son exactos. La discrepancia está solo en la
constante `A2` y en el semieje.

**Rectas** (`representations.lines`). De las 41 rectas que la semántica constructiva recalcula,
solo 12 tienen coeficientes binary64 proporcionales a los de su definición. Son las paralelas a
los ejes por puntos exactos y casos fortuitos. En las otras 29 el término independiente difiere, y
en 9 también la dirección. La mayor diferencia, tras escalar, es `2,1·10⁻¹⁴`.

**Puntos** (`representations.points`). De 27 puntos recalculados, 5 coinciden con su estado
binary64; los demás difieren hasta `2,5·10⁻¹⁵`. `J`, `K`, `L` y `M` difieren en `4,44·10⁻¹⁶`,
porque `K.x = fl(B.x + 5)`.

### 1.4 La configuración capturada de `CU2` es una representación certificada

Según el análisis del código de `CU2`:

- **Constantes.** Cada constante es el valor binary64 vivo, bit a bit: un punto por sus
  coordenadas; una recta por sus coeficientes; una circunferencia por `A2`, `A4` y `A5`, más el
  semieje solo para la banda R7. La huella y la fidelidad del corte comparan bits, sin tolerancia.
- **Certificados.** Deciden la configuración de esos bits. El ADR 0038, decisión 7, lo dice
  expresamente: «The certificate decides the configuration of the captured binary64 constants,
  never an ideal relation that the construction computes with rounding». El hallazgo F-CU2-10 ya
  registraba que las constantes capturadas no cumplen las relaciones ideales.
- **Decisiones abiertas.** Componentes válidas, celdas y transiciones por intervalos usan márgenes
  estrictos: se transfieren a cualquier configuración cercana cuyo error quede por debajo del
  margen.
- **Decisiones cerradas.** Son exactas sobre los bits: la totalidad estructural, las sucesiones de
  Sturm y el máximo común divisor, las componentes puntuales, los parámetros singulares y la
  simetría de los pliegues. Sus igualdades de bits, como un coeficiente constante exactamente 0 o
  un centro igual al del conductor, corresponden en el modelo a relaciones de procedencia.
- **Persistencia.** Las claves de linaje, la dirección de `Point(L, …)` y el texto del contrato de
  los adaptadores dentro de los tokens se persisten. Una clave de linaje usa extremos numéricos en
  hexadecimal cuando un extremo no es estructural.

**Estabilidad** (`cu2_certificate_stability`). Las estructuras de existencia de `e_1` y `o_1` no
cambian al mover un ulp `D_1.x`, `rbCone`, `α_cone` o el parámetro de `H`. En el modelo, los
certificados de `CU2` son robustos frente a perturbaciones del tamaño de la diferencia entre
representación y construcción.

### 1.5 La reconstrucción del objetivo no es intrínseca

El sondeo reconstruye los objetivos desde cuatro fronteras (`reconstruction_boundaries`):

- la de la puerta G-M3, con los descendientes de `K`;
- los descendientes de `D_1`;
- la recalculación profunda;
- el estado binary64.

| Objetivo | Semánticas exactas distintas | Detalle |
|---|---|---|
| `n` | 2 | desde `K` coincide con la profunda; desde `D_1` coincide con la binary64 |
| `h_2` | **4** | las cuatro rectas son distintas dos a dos |
| `c_1` | 3, más el semieje | matriz (desde `K` y binary64), `|HD_1|²` (desde `D_1` y profunda) y semieje |

Un mismo objeto recibiría semánticas exactas distintas según el conductor y la captura con que se
interseque. Eso viola la identidad semántica (capa 2) y la condición de la alternativa A: rectas,
circunferencias, puntos y loci deben compartir una sola definición. Tomar la reconstrucción del
objetivo como autoridad, que G-M3-4 ya prohíbe sin correspondencia demostrada, no es una
alternativa.

### 1.6 La frontera de la clase exacta y la no constructibilidad

Censo de frontera (`boundary`), sobre 101 objetos del modelo:

- 72 se recalculan exactamente;
- 10 son entradas libres;
- 6 son puntos sobre caminos;
- 13 son salidas de algoritmos fuera de la clase del sondeo: rotaciones, ángulos, expresiones,
  macros y puntos o métricas de loci.

La clase ideal cuenta 24 objetos constructibles desde las entradas libres y 77 que no lo son, o
que el sondeo no evalúa. Ese 77 incluye expresiones como `C_c = B + u·excCyl`, que serían
constructibles con un evaluador de expresiones.

**El hecho decisivo.** `H = Point[j]` y `j = Line[A, C']`, con `C' = Rotate[C, α_cone, A]`. El
coseno de un ángulo binary64 distinto de cero es trascendente, y 47° tampoco es constructible con
regla y compás. `H` no es constructible, y con él tampoco `k`, `I`, `c_1`, `n`, `r`, `M_1` y
`h_2`. La premisa del cálculo de los cinco contactos es (`boundary_support`):

- entradas libres: `A`, `D_1`, `rbCone` y `xAxis`;
- puntos sobre caminos: `B`, constructible, y `H`, no constructible.

Consecuencias:

- **Constructibilidad parcial.** La evaluación exacta solo decide de forma exacta para el objeto
  ideal cuando toda su premisa es constructible. Con `H` en la premisa, demuestra los contactos
  para el sustituto binary64 de `H` (o su anclaje), no para el `H` ideal.
- **El teorema es independiente de `H`.** El análisis §5.3 demuestra `J_1(K) = M_1` para cualquier
  `H`, solo con relaciones de procedencia (`k ∥ g`, `I ∈ i ∩ k`, `c_1` con centro en `H ∈ k`) y la
  reflexión respecto de `k`. Es estructural y genérico. Con condiciones de no degeneración
  abiertas, sería certificable por una prueba genérica en las coordenadas de `H`, con las
  condiciones de rama y de no degeneración decididas por encierros en la instancia.
- **Genericidad** (`genericity_in_H`). Con `H` movido a 12 posiciones racionales distintas, los
  cinco contactos dan igualdad exacta y `h′ = 0` exacto: 60 de 60 decisiones. Es evidencia, no
  demostración, de que la incidencia es una identidad en `H`.

### 1.7 Lo que el modelo del autor permite demostrar hoy

**Los cinco contactos en la semántica constructiva** (`mandatory_contacts`). Valen igual con
sustitutos y con anclaje:

| Contacto | `t*` | Incidencia con el objetivo constructivo | `h′(t*)` | `h″(t*)` | Contra el objetivo binary64 | `P_C − P_L` |
|---|---|---|---|---|---|---|
| `e_1·n` en `K` | `0` exacto | **igual**; en la parte limitada | `0` exacto | `−28,525` | distinto | `(−9,2·10⁻¹⁷; −2,9·10⁻¹⁶)` |
| `e_1·c_1` en `K` | `0` exacto | **igual** | `0` exacto | `−24,672` | distinto | igual que el anterior |
| `o_1·h_2` en `K` | `0` exacto | **igual** | `0` exacto | `+2,569` | distinto | `(−9,2·10⁻¹⁷; 0)` |
| `e_1·c_1` en `J` | `π` exacto | **igual** | `0` exacto | `−7,992` | distinto | `(−2,6·10⁻¹⁶; −1,7·10⁻¹⁶)` |
| `e_1·r` en `J` | `π` exacto | **igual**; en la parte limitada | `0` exacto | `+16,236` | distinto | igual que el anterior |

La columna «contra el objetivo binary64» también es informativa: el punto constructivo contra el
objetivo binary64 sigue siendo distinto. Basta con que una de las dos autoridades siga siendo
binary64 para que la incidencia falle.

**Simetría de los pliegues** (`fold_symmetry`). `CU2` certifica pliegues de `e_1` en `0` y en `±π`
(`fold-symmetry/v1:HORIZONTAL`). En seis pares de conductores racionales simétricos respecto de
`i`, tres cerca de `0` y tres cerca de `π`, el punto trazado de `e_1` es exactamente el mismo, en
el programa capturado y en la lectura constructiva. La simetría es estructural: depende de
relaciones de procedencia, no de valores.

`o_1` no tiene pliegue, pero la abscisa de su punto trazado es exactamente simétrica (`dx = 0` en
los seis pares). Como `h_2` es vertical, el residuo de `o_1` respecto de `h_2` es par en `t`, y
`h′(0) = 0` es también estructural. La tangencia de `o_1` con `h_2` es una consecuencia de
simetría.

**Consecuencia para la costura.** Con la simetría exacta `h(π + s) = h(π − s)`, en una caja
simétrica dentro de una componente certificada las raíces simples van por pares. Una raíz en `π`
tiene `h′(π) = 0`, así que nunca es simple. Una única raíz transversal en la caja de la costura es
imposible. La clasificación `TRANSVERSE_ESTABLISHED` de la raíz de la costura (`pins.B08_B09`)
queda refutada por evidencia estructural, sin M3 y con cualquier autoridad que conserve la
simetría. Es una forma nueva de certificado de la regla de conflicto, un certificado de paridad por
simetría exacta, que D-CU3-13 no contemplaba.

**Dinámica** (`dynamics`, 11 revisiones: `rbCone` 3, 4, 6 y 7,5; `α_cone` 35° y 60°; `D_1` ×0,5,
×1,3 y ×3; `excCyl` 3,7 como control; restauración):

- **Revisiones en que los contactos existen.** Los cinco son exactos en la semántica constructiva
  y distintos contra los objetivos binary64.
- **Con `D_1` ×3:**
  - `e_1·n` y `e_1·r` quedan fuera de la parte limitada, con fallo cerrado;
  - `M_1` no existe, así que el objetivo `h_2` es inválido y el contacto `o_1·h_2` falla cerrado;
  - `e_1·c_1` sigue exacto.
- **Coste.** Como mucho 3 514 operaciones y profundidad 2 por contacto.

### 1.8 Diagnóstico

1. **La causa.** El bloqueo de G-M3-4 se debe a que dos contratos aprobados toman una
   representación como autoridad geométrica:
   - el objetivo, por su estado binary64;
   - el locus, por su configuración capturada.

   No es un defecto de M3, que decide exactamente.
2. **Por qué no se arregla en la representación.** La representación binary64 contradice el 41 %
   de las relaciones de procedencia del modelo. En general, ninguna autoridad binary64 puede
   conservar las incidencias de los objetos derivados (§1.2).
3. **La semántica constructiva.**
   - Es coherente: con anclaje, conserva las 160 relaciones.
   - Es intrínseca: su frontera depende solo de la clase de cada objeto, no del conductor ni de la
     captura.
   - Hace exactos los cinco contactos.
4. **Su límite.** Algunas construcciones no son constructibles, como el giro de 47°. En ellas, la
   evaluación exacta decide para los sustitutos de la frontera. La verdad del objeto ideal exige
   una prueba genérica, que existe matemáticamente para el modelo (§5.3) pero que el producto no
   sabe producir todavía.
5. **Lo que se conserva.** Los certificados de `CU2` son robustos en el modelo y su simetría de
   pliegue es estructural. Pueden conservarse como certificados de la representación y
   transferirse al objeto constructivo por margen o por estructura, sin tocar su texto ni sus
   claves de linaje (§1.4).

## 2. Alternativas

### 2.1 A. Autoridad constructiva compartida

**Definición.** El significado geométrico de cada objeto es el que define su construcción desde
las entradas libres: el valor binary64 de una entrada libre es su definición. Lo comparten todos
los participantes de una relación certificada: rectas, segmentos, rayos, circunferencias, puntos y
loci. El estado binary64, la captura de `CU2` y los encierros son representaciones de ese objeto.

**Dos formas de la frontera:**

| Forma | Frontera | Qué demuestra la evaluación exacta | Riesgo |
|---|---|---|---|
| **A1, frontera declarada** | los objetos fuera de la clase exacta (giros, expresiones no evaluadas, algoritmos numéricos) son sustitutos exactos declarados: su binary64 o un anclaje | todo lo que se deriva exactamente de los sustitutos: los cinco contactos, hoy | una relación que depende de la propiedad exacta de un sustituto, como la isometría de un giro, puede salir refutada sin serlo; sus contradicciones no son fiables en la frontera |
| **A2, autoridad ideal** | el objeto matemático ideal; la frontera es solo un dispositivo de cálculo | igual que A1 cuando la premisa es constructible; con premisa no constructible, solo una prueba genérica demuestra | sin pruebas genéricas, lo que depende de entradas no constructibles queda sin demostrar |

**Integración mínima con el núcleo:**

- **Grafo.** La definición constructiva ya existe: es el grafo de GeoGebra. No hace falta persistir
  nada nuevo para definir los objetos; el significado es una función pura del grafo, de las
  entradas libres y de una regla de frontera versionada.
- **Certificados.** Los de `CU2` siguen siendo certificados de su representación `Σ_L`. Una capa de
  transferencia los califica para el objeto constructivo:
  - los abiertos, por margen;
  - los cerrados, por estructura o por una nueva decisión exacta.
- **Persistencia.** Persiste solo la versión de la política de autoridad e integridad que adopte un
  documento (§7).

### 2.2 B. Certificados estructurales de incidencia

**B1, con la representación binary64 como autoridad.** No es válida. En el modelo:

- la procedencia afirma `M_1 ∈ c_1` y `M_1 ∈ n`, que son exactamente falsas para las
  representaciones;
- los conteos de G-M3 prueban que los conjuntos autoritativos de los cinco contactos son disjuntos
  cerca de `t*` (`N = 0` en cuatro).

Un certificado estructural declararía incidentes dos conjuntos exactos incompatibles, que es
justamente lo que la instrucción prohíbe. B1 solo valdría donde la representación conserva la
relación por casualidad: 94 de 160 relaciones, casi todas paralelas a los ejes.

**B2, dentro de la autoridad constructiva.** Es válida y necesaria:

- la procedencia da relaciones exactamente verdaderas para el objeto constructivo (160 de 160 con
  anclaje);
- las relaciones demostradas, como el teorema del análisis §5.3 o la simetría de los pliegues, son
  premisas legítimas.

**Condiciones para que un certificado estructural sea autoridad matemática:**

- que su enunciado se refiera a los objetos de la autoridad constructiva;
- que sus premisas sean relaciones de procedencia o relaciones ya certificadas;
- que sus condiciones de no degeneración y de rama estén decididas en la instancia por encierros;
- que ningún encierro riguroso del objeto constructivo lo contradiga.

B no es una alternativa a A: es su capa de prueba.

### 2.3 C. Revisión coordinada de la captura y de los objetivos

C3 tal como se formuló («la configuración capturada de `L` pasa a ser `Σ_B`») no basta sola. El
punto constructivo contra el objetivo binary64 sigue siendo distinto en los cinco contactos
(§1.7). Además cambiaría el significado de los certificados de `CU2`, de su huella y,
potencialmente, de las claves de linaje persistidas.

**La forma mínima de C** cambia a la vez las dos declaraciones de autoridad:

- el locus, en el ADR 0039 y en el modelo de construcción certificada §11.2;
- el objetivo, en el contrato de intersecciones §2.1 y en el modelo de construcción certificada
  §11.3.

La captura de `CU2` no se reinterpreta: sigue siendo, con su texto, su huella, sus certificados y
sus claves, una **representación certificada**. Lo nuevo es la transferencia a la autoridad
constructiva. Así se estudia C3 junto con la autoridad de los objetivos, como pide la instrucción,
y su alcance se reduce a una recalificación más una capa nueva. Los contratos afectados están en §6.

### 2.4 D. Hipótesis de integridad estricta y relajada

**Valoración general.** Es un modelo de evidencia, ortogonal al modelo de autoridad: dice cómo de
establecida está una relación respecto de una autoridad. Por eso:

- **No rescata la autoridad binary64.** Con la autoridad vigente, los cinco contactos están
  refutados exactamente (G-M3-4). Por la precisión 4 no pueden ser relajadamente válidos respecto
  de ella.
- **Encaja con A2.** Las relaciones demostradas para el objeto constructivo son `STRICT` con
  independencia de sus coordenadas (principio 1). Las que el producto aún no sabe demostrar pueden
  admitirse como `RELAXED` por una política versionada (principio 2), siempre que no exista una
  contradicción rigurosa respecto del objeto constructivo.

Por precisiones:

**1. Integridad frente a precisión.** Son dos ejes:

- **Integridad:** cómo de establecida está una relación geométrica.
- **Precisión de la representación:** cuánto se aleja un valor numérico del objeto autoritativo.
  El vocabulario existente `NumericGuarantee` (`EXACT_ARITHMETIC`, `CERTIFIED_ERROR_BOUND`,
  `ESTIMATED_ERROR` y `FLOATING_POINT_UNCERTIFIED`) ya pertenece a este segundo eje.

Por ejemplo, `M_1 ∈ c_1` es `STRICT` por procedencia, y su representación binary64 la cumple con
error de potencia `2·10⁻¹⁴`. Una incidencia demostrada no pierde integridad porque sus coordenadas
sean aproximadas.

**2. Estados conceptuales.** Siempre son relativos a la autoridad aplicable:

| Estado | Significado | Fuentes de evidencia | Relación con los vocabularios existentes |
|---|---|---|---|
| `STRICT` | demostrada o certificada rigurosamente para el objeto autoritativo | procedencia; teorema estructural; evaluación exacta con premisa constructible; prueba genérica con condiciones de instancia certificadas; certificado de intervalos transferible | abarca, cuando su evidencia se transfiere a la autoridad constructiva, `EXACT` y `RIGOROUS_INTERVAL` de `LocusEvidenceNature2D`, las clases 1 y 2 de garantía métrica (ADR 0037), los grados `EXISTENCE_*` y las clases de contacto 1 y 2 de `CU3` |
| `RELAXED` | admitida por una política numérica versionada, sin contradicción rigurosa | verificación local de G8B-R1; residuo y aislamiento; igualdad exacta solo en los sustitutos de la frontera; compatibilidad numérica de clase 3 | abarca `NUMERICAL_UNDER_HYPOTHESES`, `LOCALLY_VERIFIED` y la clase 3 de garantía métrica (`ESTIMATED`) |
| `NOT_ESTABLISHED` | ni demostrada ni admitida | — | **el mismo significado** que el `NOT_ESTABLISHED` de unas catorce enumeraciones existentes: no demostrado, fallo cerrado |
| `CONTRADICTED` | refutada rigurosamente para el objeto autoritativo | encierro que excluye la relación; decisión exacta con premisa constructible; conteo riguroso; paridad por simetría exacta | generaliza `CONTRADICTED_BY_CERTIFIED_EVIDENCE` de los diseños de `CU3` y la discrepancia `EVALUATOR_DISAGREEMENT` de `LocusDefinition2D`; ningún ADR tiene hoy este nivel |

**Colisiones de nombres que habrá que evitar:**

- `STRICT` ya es un valor de `OpenBoundaryPolicy` y un token público (`strict`) del recorrido
  métrico;
- «grade» pertenece a `CU3` y «guarantee class», a las métricas;
- `RELAXED` y la palabra *integridad* están libres en las especificaciones y los ADR.

Si los estados se convierten en API, necesitan un tipo propio, nunca valores añadidos a una
enumeración existente.

**3. Propagación por el grafo.** Se distinguen dos cosas:

- la **validez propia** de un objeto: si existe en la revisión;
- la **integridad de las relaciones** de las que depende: sus premisas.

Regla propuesta, «propagación por premisas»:

- Cada relación certificada lleva su método y la lista de sus premisas: relaciones de procedencia,
  relaciones certificadas y hechos de existencia.
- Su integridad es la del método, limitada por la peor de sus premisas.
- Una premisa `CONTRADICTED` anula la derivación: lo que dependía de ella pasa a `NOT_ESTABLISHED`,
  y a `CONTRADICTED` solo si se refuta por sí mismo.
- Un objeto muestra la peor integridad de las relaciones en que se apoya su definición, con la
  lista de sus premisas relajadas, de modo que el usuario y sus dependientes ven por qué.
- Un consumidor que exija `STRICT` falla de forma explícita, con causa tipada.

Hay precedentes en el código: la garantía métrica se queda con la peor contribución y nunca mejora
(ADR 0037), `UPSTREAM_NOT_ESTABLISHED` se propaga en `CU2` y `CU3` diseña grados mínimos por
consumidor. La propagación por premisas no es «la peor de todo el grafo». La incidencia de un
punto de intersección es `STRICT` por procedencia aunque una recta lejana sea `RELAXED`; solo
hereda lo que su prueba usa.

**4. Ninguna contradicción se convierte en validez.**

- `RELAXED` exige que no exista una contradicción rigurosa respecto de la **misma** autoridad.
- Cambiar de autoridad no es una relajación. Es una decisión versionada, uniforme y justificada:
  la autoridad binary64 contradice el 41 % de la procedencia. Nunca se elige por relación.
- En A1, una contradicción cuya premisa contiene un sustituto aproximado no es rigurosa respecto del
  objeto ideal. Debe rebajarse a `NOT_ESTABLISHED`, nunca usarse para retirar ni para admitir.

**5. Convivencia:**

- **G8B-R1.** Es la política `RELAXED` v1 de las raíces transversales. Las raíces que admite
  siguen admitidas, y suben a `STRICT` con un certificado de cambio de signo transferible.
- **Identidad semántica.** La integridad nunca entra en tokens ni en selectores (ADR 0008 y 0039).
- **Certificados de `CU2`.** Son representaciones certificadas que se transfieren (§2.3).
- **Persistencia.** La integridad es evidencia de revisión y no se persiste. Se persiste la
  versión de la política que admite relaciones `RELAXED`, porque de ella dependen las admisiones al
  reabrir (ADR 0013: «Reconstructible inputs and policy are persisted; current numeric evidence
  must be recomputed and verified»).
- **Documentos anteriores.** Conservan la política vigente hasta una migración explícita (§7).

**6. Sustitución progresiva.** Una operación que hoy solo admite `RELAXED` puede pasar a `STRICT`
cuando exista un certificador; por ejemplo, la prueba genérica en las coordenadas de `H`. Como la
integridad no es identidad, la subida no cambia tokens, selectores ni el grafo. La bajada, por una
contradicción nueva, quita la admisibilidad sin reasociar (D-CU3-7).

**Relación con la opción C rechazada de D-CU3-5.** Esa opción, «la clase 3 como intersección con
estado visible», se rechazó porque presentaba un contacto dentro de una tolerancia como
intersección. El principio 2 lo revisa con cuatro salvaguardas que entonces faltaban:

- `RELAXED` es un estado de integridad que se propaga, no una marca;
- exige ausencia de contradicción rigurosa respecto de la autoridad aplicable;
- su política es versionada;
- nunca se presenta como demostración.

Aun así, materializar una intersección `RELAXED` cambia D-CU3-5 y necesita una decisión autoral.

### 2.5 Alternativas descartadas

| Alternativa | Por qué se descarta |
|---|---|
| Representación binary64 consistente («reparar» semiejes, matrices y coeficientes) | imposible en general: los puntos de intersección son irracionales (§1.2) |
| Aritmética exacta en todo el núcleo | cambio máximo en el núcleo y en upstream; coste en cada arrastre; no resuelve las operaciones no constructibles |
| Incidencia por tolerancia | contraria a `AGENTS.md` §1, principio 8, y a `OBS-CU0-CU3-TANGENT-EXISTENCE` |
| Prover simbólico heredado como autoridad única | prueba verdad genérica sobre los complejos, sin ramas reales: no distingue rayo de recta ni la salida 1 de la 2. En el modelo, `K_1 ∈ h_2` depende de que `M_1` y `J_1` estén en la misma rama real: con la otra intersección de `c_1` con la recta de `n`, `h_2` sería otra vertical (razonamiento; el prover no se ha ejecutado). Puede ser fuente de evidencia con condiciones de instancia, no autoridad |
| Reconstrucción del objetivo en `Σ_L⁺` como autoridad | no intrínseca: `h_2` tiene cuatro semánticas (§1.5) |
| C3 sola | el objetivo binary64 sigue refutando (§1.7) |
| R-1 como sustituto del objetivo | excluida por la instrucción: solo establece la pertenencia al locus |

### 2.6 Comparación con las condiciones obligatorias

| Condición | Binary64 vigente | A1 | **A2 + B2 + D (por C)** | B1 | C3 sola |
|---|---|---|---|---|---|
| Integridad geométrica y trazabilidad | no: el 41 % de la procedencia es falso | sí, salvo en la frontera | **sí** | no | no |
| Identidad durable independiente de coordenadas | sí | sí | **sí** (la integridad no es identidad) | sí | riesgo en las claves de linaje con extremos numéricos |
| Dependencias dinámicas del DAG | sí | sí | **sí** (función pura del DAG, por revisión) | sí | sí |
| Coherencia entre objeto y representaciones | no: hasta tres circunferencias distintas por objeto | sí, salvo en la frontera | **sí** (representaciones con error declarado) | no | parcial |
| Identidad de soluciones y continuidad explícita | sí | sí | **sí** | sí | parcial |
| Admisibilidad local independiente de la completitud | sí | sí | **sí** (G8B-R1 como `RELAXED` v1) | sí | sí |
| Degeneraciones y fallo cerrado | sí | contradicciones espurias en la frontera | **sí** | no | sí |
| Persistencia y documentos anteriores | sí | nueva versión | **sí** (política persistida; migración explícita) | — | migración de certificados |
| Reproducible y verificable | sí | sí | **sí** | sí | sí |
| Cambio mínimo en el núcleo y en upstream | — | medio | **medio** (capa nueva; sin cambios en upstream) | bajo | medio a alto |
| Establece las dos incidencias del modelo | **no** (refutadas) | sí, relativas a la frontera | **sí**: `P ∈ L` estricta; `P ∈ r` `RELAXED` hoy y `STRICT` con la prueba genérica | no | no |

## 3. Validación del modelo del autor

`P` es el punto del locus en el contacto y `r` el objetivo. Las formas de los casos obligatorios
son `Intersect(e_1, n)`, `Intersect(e_1, c_1)` e `Intersect(o_1, h_2)`, sin conductor, y las
formas de la Etapa A con el conductor `K` o `J`.

| Caso | Autoridad vigente (binary64) | A1 (frontera declarada) | A2 + D (recomendada) |
|---|---|---|---|
| `Intersect(e_1, n)` en `M_1` | **refutado**: `N = 0`; `e_1` no alcanza `n` | `P = F(0)`, incidencia exacta con la `n` constructiva y en su parte limitada: `STRICT` relativo a la frontera | `P ∈ e_1`: `STRICT`, porque `P = F(t*)` con `t*` exacto y los predicados de validez del corte en `t*` son abiertos y se cumplen con margen (transferencia por margen, a implementar). `P ∈ n`: teorema §5.3; hoy `RELAXED`, con igualdad exacta en los sustitutos y genericidad 12 de 12; `STRICT` con la prueba genérica en `H` |
| `Intersect(e_1, c_1)` en `M_1` y en la costura | **refutado** en los dos (`N = 0`); la raíz de la costura publicada como transversal admisible | los dos `STRICT` relativos a la frontera | `P ∈ c_1` en los dos: como arriba. La costura se **reclasifica** como contacto en pliegue por paridad estructural, con su punto `preserve` intacto |
| `Intersect(o_1, h_2)` en `N_1` | **refutado** (`N = 0`) | `STRICT` relativo a la frontera | `P ∈ h_2`: como arriba. Orden 2, tangencia regular, `STRICT` por simetría: el residuo es par porque `h_2` es vertical y `K_1.x = J_1.x` |
| `Intersect(e_1, r)` en la costura | **refutado** como contacto: `N = 2`, la punta del pliegue cruza `r` | `STRICT` relativo a la frontera | como `e_1·c_1` en la costura |

**Qué objeto es la autoridad en A2:**

- **El locus.** Es el locus ideal `{F(t)}` de su construcción; su representación certificada es
  `Σ_L`.
- **Los objetivos.** `n` es el segmento ideal `[H, M]`, con `M = h ∩ Line[K, g]`; `c_1` es la
  circunferencia ideal de centro `H` por `D_1`; `h_2` es la paralela ideal a `g` por `M_1`; `r` es
  el segmento ideal `[H, L]`.
- **La posición materializada.** La de `P_C`, con su representación binary64 y su error declarado.
  Difiere de `P_L` en `10⁻¹⁶` y queda dentro de la tolerancia de la referencia `preserve`.

**Tangencias, pliegues y multiplicidad de preimágenes:**

- **Pliegue (`e_1` en `0` y en `π`).** `F′(t*) = 0` exacto por simetría. La raíz `t*` de `h` tiene
  multiplicidad dos con un solo punto geométrico. Si se perturba el objetivo, desaparece
  (`CONTACT_LOST`) o queda un punto geométrico con dos preimágenes `t* ± s`, con una sola identidad
  (D-CU3-11).
- **Tangencia regular (`o_1·h_2`).** `F′ ≠ 0`, `h′(0) = 0` y `h″ ≠ 0`. Si se perturba, desaparece
  o da dos puntos distintos con identidades nuevas (`CONTACT_SPLIT`).
- **Representación binary64 de un pliegue.** Puede quedar a un ulp por dentro (`N = 0`) o cruzar el
  objetivo (`N = 2`, dos preimágenes de un punto): fue lo que vio G-M3.

**Dinámica.** En las once revisiones del sondeo, la decisión constructiva sigue a la construcción:
iguales donde existen y fallo cerrado donde la construcción deja de cumplir sus condiciones
(`D_1` ×3).

## 4. Recomendación razonada

**Dirección recomendada:**

- **Autoridad: A2.** El significado geométrico de cada objeto es el de su construcción ideal desde
  las entradas libres. Es la única autoridad coherente con `AGENTS.md` §1 (trazabilidad
  constructiva; procedimiento frente a resultado; ninguna aproximación silenciosa) y §11.1–§11.2, y
  con el principio 1 de la hipótesis.
- **Vía contractual: C**, en su forma mínima:
  - se recalifican las dos declaraciones de autoridad, la del locus y la del objetivo;
  - la captura de `CU2` se conserva como representación certificada, con transferencia.
- **Prueba: B2.** Procedencia, teoremas estructurales (simetrías y reflexiones), evaluación exacta
  con premisa constructible, encierros transferibles y, como capacidad nueva, pruebas genéricas en
  las entradas no constructibles.
- **Evidencia: D.** Estados de integridad `STRICT`, `RELAXED`, `NOT_ESTABLISHED` y `CONTRADICTED`,
  relativos a la autoridad, con propagación por premisas y una política `RELAXED` versionada cuya
  versión 1 es G8B-R1.

**Por qué no A1 como autoridad.** A1 da hoy `STRICT` a los cinco contactos, pero relativo a una
frontera convencional. Sus contradicciones en la frontera no son fiables: refutaría la isometría
de un giro, que es verdadera. En A2, la evaluación de A1 sigue siendo el motor de cálculo, con la
calificación honesta de su premisa.

**Por qué la hipótesis D es necesaria y no suficiente:**

- **Necesaria:** sin `RELAXED`, toda construcción con un ángulo no constructible quedaría sin
  intersecciones hasta disponer de pruebas genéricas.
- **No suficiente:** sin el cambio de autoridad, los cinco contactos seguirían refutados.

**Decisión siguiente para el autor**, entre las dos opciones que nombra su instrucción:

1. **Solución parcial conservadora, compatible con la dirección (recomendada como primer paso).**
   - **Cambios normativos**, solo para las formas nuevas de `CU3`, que ningún documento usa:
     - autoridad constructiva para `Intersect(L, r, D)`;
     - estados de integridad con propagación por premisas;
     - política `RELAXED` v1;
     - revisión de D-CU3-5 para materializar `RELAXED` con estado visible.
   - **Resultado en el modelo:** `CU3-A` materializaría los tres casos obligatorios y la costura con
     las dos incidencias explícitas: `P ∈ L` `STRICT` y `P ∈ r` `RELAXED`, con la evidencia de
     igualdad exacta en los sustitutos.
   - **Lo que no cambia:** los resultados existentes (intersecciones sin conductor, regla de
     conflicto, `preserve` y `pins`), los tokens y los certificados de `CU2`.
2. **Investigación arquitectónica específica, en paralelo o a continuación.** Prueba genérica en
   las entradas no constructibles: evaluación exacta paramétrica en las coordenadas de la frontera
   (en el modelo, solo `H`), con las condiciones de rama y de no degeneración certificadas en la
   instancia. Haría `STRICT` los cinco contactos sin cambiar ninguna identidad. Incluye extender la
   regla de conflicto con el certificado de paridad por simetría (§1.7) y la materialización sin
   conductor (`CU3-B`) bajo la autoridad constructiva.

## 5. Alcance mínimo del cambio

**Núcleo (paquete nuevo de GeoCeDG, sin cambios en upstream):**

- **Evaluador constructivo exacto** de la clase de regla y compás:
  - puntos, rectas, segmentos y rayos, circunferencias por centro y punto o por centro y radio;
  - intersecciones recta–recta y recta–circunferencia con la selección heredada;
  - paralelas, perpendiculares y puntos medios;
  - puntos sobre caminos por su parámetro exacto, o anclados;
  - los programas de clase v3 de los loci.

  Parte del prototipo de G-M3, con presupuesto. La tabla de frontera intrínseca y versionada
  clasifica cada algoritmo en constructible, sustituto declarado o fuera de clase.
- **Capa de integridad** para las relaciones que `CU3` necesita: la segunda incidencia de la
  Etapa A y, más tarde, la regla de conflicto. Lleva las premisas y la calificación de la frontera.
- **Transferencia de los certificados de `CU2`:**
  - los abiertos, por margen, con la diferencia exacta entre la constante capturada y la
    constructiva (por ejemplo, `−3,66·10⁻¹⁵` en el radio² de `c_1`);
  - los cerrados, por estructura, como la simetría de los pliegues.

**Sin cambios:**

- los algoritmos binary64 heredados y su dibujo;
- los formatos persistidos existentes;
- el texto de los adaptadores de residuo dentro de los tokens;
- las claves de linaje y la huella de `CU2`.

**Persistencia.** Un registro versionado de la política de autoridad e integridad, que solo
escriben los documentos que la adoptan, con el patrón del ADR 0029.

**Upstream.** Solo las formas nuevas de `CmdPoint` y `CmdIntersect` ya previstas por `CU3`.

## 6. Contratos y ADR que necesitarían revisión

| Contrato | Cláusula | Revisión necesaria | Naturaleza |
|---|---|---|---|
| ADR nuevo | — | «Autoridad geométrica constructiva e integridad de relaciones»: A2, frontera intrínseca, estados, propagación, política `RELAXED`, compatibilidad | decisión nueva |
| `locus-v2-intersections.md` | §2.1 (adaptador «authoritative residual»; coeficientes actuales); CU3.1, último párrafo; CU3.4 (configuración y objetivo del certificado) | el objetivo autoritativo es el objeto constructivo; el adaptador binary64 es la representación que usa la búsqueda numérica de raíces | nueva versión semántica |
| `locus-v2-certified-construction-model.md` | §11.2 y §11.3 (aprobados el 2026-10-24) | la configuración de decisión es la constructiva; `Σ_L` es una representación certificada | nueva versión semántica |
| `locus-v2-certified-construction-model.md` | §3 y §10 (constantes capturadas) | recalificación como representación y reglas de transferencia; sin cambio en la clase v3 | aditiva |
| ADR 0028 | decisión 3 («Its authority is the exact-real evaluation … on the captured constants») | autoridad **de la representación** | recalificación |
| ADR 0038 | decisión 7 (decide la configuración capturada, «never an ideal relation»), decisión 17 (huella) | sin cambio de texto: la transferencia es una capa nueva. La huella podría incluir la versión de la autoridad | aditiva; versión si se incluye |
| ADR 0039 | «The authoritative object is `L` in its `CU2` captured configuration `Σ_L`»; autoridad del objetivo; rechazos de `Σ_B` y de C3 | sustituido parcialmente por el ADR nuevo | nueva versión semántica |
| ADR 0008 | «rich Geo as authority»; G8B-R1 | G8B-R1 pasa a ser la política `RELAXED` v1; el objeto rico sigue siendo autoridad de publicación | recalificación aditiva |
| ADR 0013 | política persistida, evidencia recalculada | la versión de la política de autoridad entra entre las entradas persistidas | aditiva |
| ADR 0017 | barrera de cardinalidad de los selectores | nota de migración si una contradicción retira raíces | migración explícita |
| ADR 0029 | regla versionada; migración solo por un evento explícito; documentos mixtos válidos | patrón de adopción | sin cambio |
| ADR 0037 | correspondencia de las clases 1 a 4 | correspondencia con los estados de integridad | aditiva |
| Decisiones de `CU3` | D-CU3-3 (autoridad `Σ_L`), D-CU3-4 (`Σ_L⁺`), D-CU3-5 (clase 3 no materializada), D-CU3-10 (costura), D-CU3-13 (formas de certificado) | revisión autoral; D-CU3-13 gana el certificado de paridad por simetría | decisiones nuevas |
| `AGENTS.md` | §1, §11 y §11.6 | ninguna: la dirección es coherente con ellos (y el archivo es de solo lectura para los agentes) | — |
| Referencias de `CU0` | `preserve` y `pins` | `preserve.contacts` se conserva; `pins.B08_B09` se invierte con prueba | sin cambio de contrato |

## 7. Riesgos y compatibilidad

**Riesgos:**

- **Construcciones no constructibles.** Con ángulos arbitrarios son la norma en geometría
  descriptiva. Sin pruebas genéricas, muchas relaciones quedarían `RELAXED`. Mitigación: la
  investigación específica de §4 y la propagación por premisas, que no contamina lo que una prueba
  no usa.
- **Coste.** Como mucho 3 552 operaciones exactas, profundidad 2 y 615 bits por contacto en el
  modelo; presupuesto versionado y evaluación solo por revisión.
- **Degeneraciones.** En configuraciones degeneradas, una decisión cerrada de `CU2` sobre `Σ_L` y
  la decisión constructiva pueden diferir, como en el valor crítico de la mordedura. La integridad
  queda entonces `NOT_ESTABLISHED`, sin cambiar las componentes ni las claves de `CU2`.
- **Comprensión del usuario.** Hacen falta un inspector y mensajes que expliquen la integridad y
  sus premisas, separados de la precisión.
- **Banda R7.** Modela el comportamiento binary64 heredado y queda en la capa de representación.

**Compatibilidad** (A = aditiva; V = nueva versión semántica; M = migración explícita):

| Elemento | Efecto | Tipo |
|---|---|---|
| Objetos y construcciones serializados | ningún formato cambia; el significado constructivo es una función del grafo | A |
| Certificados de `CU2` | se conservan como certificados de su representación; la transferencia es nueva | A |
| Tokens de intersección existentes | texto del contrato sin cambios; la integridad no entra en los tokens | A |
| `preserve.contacts` | se conserva: con la autoridad constructiva no hay contradicción rigurosa en los cuatro puntos, y las posiciones difieren en `10⁻¹⁶` | A |
| `pins.B08_B09` | cuando se extienda la regla de conflicto, la costura se reclasifica por paridad estructural; los contactos sin conductor solo cambian si se adopta la materialización `RELAXED` | V |
| Objetos dependientes del grafo | sin reasociación; un dependiente de una relación `RELAXED` muestra su integridad heredada | A |
| Deshacer, Rehacer y reapertura | la integridad se recalcula por revisión; la política persistida reproduce las admisiones | A |
| Archivos antiguos sin semántica constructiva | conservan la autoridad y la política vigentes; ninguna asociación nueva al cargar (`AGENTS.md` §11.6 y §12.6); adopción solo por un evento explícito (ADR 0029) | M |
| Formas nuevas de `CU3` | nacen con la autoridad constructiva | V |

## 8. Estrategia de validación y puertas

| Puerta | Qué establece | Pruebas adversarias |
|---|---|---|
| G-AUT-0, normativa | ADR nuevo y enmiendas aprobados por el autor | — |
| G-AUT-1, cierre de procedencia | el evaluador constructivo hace exactas las relaciones de procedencia (160 de 160 en el modelo, más un corpus sintético) | un punto sobre camino sin anclaje refuta su camino; una recta binary64 refuta sus puntos |
| G-AUT-2, contactos | los cinco contactos exactos, con `h′ = 0`, en el modelo y en barridos dinámicos; genericidad en las entradas no constructibles | objetivo desplazado un ulp; igualdades accidentales (V-M3-5 y V-M3-6); conflicto de representación (V-CFG-6) |
| G-AUT-3, intrinsecidad | un objeto tiene una sola semántica con cualquier conductor y cualquier captura | las cuatro semánticas de `h_2` de §1.5 deben desaparecer |
| G-AUT-4, transferencia de `CU2` | componentes por margen; pliegues por estructura; degeneraciones en `NOT_ESTABLISHED` | perturbaciones de un ulp (§1.4); el valor crítico de la mordedura |
| G-AUT-5, integridad | propagación por premisas; consumidores que exigen `STRICT` fallan de forma explícita; ninguna promoción silenciosa | un grafo sintético con premisas `STRICT`, `RELAXED` y `CONTRADICTED` |
| G-AUT-6, sin blanqueo | ninguna relación `CONTRADICTED` respecto de la autoridad activa pasa a `RELAXED`; la autoridad es por documento y versión, nunca por relación | intentar admitir un contacto refutado bajo binary64 en un documento antiguo |
| G-AUT-7, compatibilidad | documentos anteriores sin cambios; tokens que se resuelven; `preserve` igual; `pins` invertidos solo con prueba | el corpus de `CU0`, `CU1` y `CU2` y el modelo del autor |
| G-AUT-8, coste | presupuesto versionado | inyección de presupuesto |

Después se reanudan las puertas de `CU3` (G-A, G-B y G-final `INTEGRATED_PHASE`) con la matriz
ajustada a la decisión.

## 9. Implicaciones científicas para E1 y `CU10`

- **Principio transversal.** La integridad geométrica es una propiedad de las relaciones entre
  objetos constructivos, no de sus coordenadas. Precisión y representación son ejes distintos.
  Profundiza el principio 8 del registro E1 y la distinción que pidió la aprobación de `CU3`.
- **Resultado:**
  - la representación binary64 no es cerrada bajo regla y compás (§1.2), con el modelo del autor
    como ejemplo (66 de 160);
  - una circunferencia binary64 tiene dos radios y la construida, un tercero;
  - el núcleo heredado ya confía en la procedencia antes que en los números.
- **Métodos del registro E1:**
  - SM-B-8 («constantes capturadas frente a relaciones geométricas ideales») recibe la evidencia de
    este estudio;
  - SM-B-11 recibe el resultado de G-M3;
  - se proponen en `INVESTIGACIÓN`:
    - la semántica constructiva con frontera intrínseca;
    - la prueba genérica con condiciones de instancia;
    - los estados de integridad con propagación por premisas;
    - el certificado de paridad por simetría exacta.
- **Para el expediente de `CU10`:**
  - el modelo del autor ilustra los seis niveles de §1.1;
  - el teorema §5.3 es un ejemplo de prueba estructural;
  - el giro de 47° marca el límite de la constructibilidad.

## 10. Lo que este estudio no establece

- **Ninguna decisión.** No adopta ninguna alternativa, no aprueba ninguna norma ni autoriza
  ninguna implementación.
- **Pruebas genéricas.** No se prototipa la prueba genérica: la genericidad en `H` es evidencia
  (12 de 12), no demostración.
- **Pruebas en `Σ_C`.** Los conteos de raíces en la semántica constructiva no se han certificado
  con un modelo de cajas propio. Los valores de `h″` son exactos en `t*`, y su signo en la caja se
  apoya en el margen de los encierros de `Σ_L` (G-M3).
- **Expresiones.** La clase del sondeo no evalúa expresiones; algunos objetos que clasifica como no
  constructibles lo serían con un evaluador de expresiones.
- **Coste general.** El coste medido es el del modelo; otras construcciones necesitarán su banco.

## 11. Reproducción

```text
pwsh -NoProfile -File tools/benchmark/pre-g9b-cu/run-cu3-authority-study.ps1 -OutputDirectory <directorio nuevo>
```

Hubo dos ejecuciones sobre `91e78a92`, en `artifacts/benchmarks/pre-g9b-cu/cu3-authority-study-91e78a92-runA`
y `…-runB`, que el repositorio ignora:

- códigos de salida 0 en el sondeo y en la limpieza;
- espacio de trabajo limpio y fuentes del producto sin cambios;
- el mismo SHA-256 de `cu3-authority-study.json`;
- 17,6 a 21,2 s por ejecución, a título informativo.
