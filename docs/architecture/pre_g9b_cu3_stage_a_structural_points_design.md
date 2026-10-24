# PRE-G9B-CU3 — Etapa A: puntos estructurales por correspondencia del conductor

| Campo | Valor |
|---|---|
| Carácter | Diseño candidato de la Etapa A de `CU3`. No es especificación normativa ni autoriza implementación |
| Estado | `CU3 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`, revisado por la revisión científica focal de 2026-10-24; `selfApproved=false` |
| Encargo | Instrucción autoral «PRE-G9B-CU3 — Autorización de preparación arquitectónica, científica y normativa» (2026-10-24), §1–§4; precisión autoral «Decisión sobre la planificación de CU3» (2026-10-24); instrucción «PRE-G9B-CU3 — Revisión científica focal del candidato de preparación» (2026-10-24), puntos 2, 3 y 6 |
| Autoridad vigente | `locus-v2-semantics.md` v1.3 (conductores y proveedores); `locus-v2-point-interaction.md` §7, §12, §16 y §17; `locus-v2-existence-components.md` v1.1 §15 (en particular §15.4, componentes puntuales); `locus-v2-certified-construction-model.md` v1.2 §10 (en particular §10.2 y §10.3, selección y salidas coincidentes); `locus-v2-public-surface.md` §6 y §8–§10; `locus-v2-intersections.md` §5 y §8; ADR 0008 (aclaración G8B-R1), 0027 y 0038; `AGENTS.md` §1, §4 y §11.3–§11.5 |
| Antecedente | [propuesta F](pre_g9b_cu0_tangent_materialization_proposal.md) §4 (F-1); enmienda candidata A3.3 de `CU0` |
| Evidencia | [análisis del estado inicial](pre_g9b_cu3_initial_state_and_capability_analysis.md) §2, §3.3, §5 y §5.7 |

## 1. Objetivo

Materializar un punto de un Locus V2 `L` **en la posición del conductor que corresponde a un
punto `D`**, sin buscar raíces. Opcionalmente, ese punto se materializa como intersección con un
objetivo `r` cuando la segunda incidencia está demostrada. Es la Etapa A de DA-3: puntos
estructurales con las dos incidencias.

La precisión autoral de 2026-10-24 separa cuatro hechos (§2). La revisión focal añade cuatro
exigencias:

- separar la correspondencia **exacta** `D ↦ t(D)` de su **aproximación numérica**, con sus
  garantías y su fallo cerrado (§2.3 y §3);
- reconstruir el punto exacto `D` desde su procedencia y su selección heredada (§6);
- mantener las componentes puntuales fuera de la materialización (§2.4);
- no inferir una incidencia exacta de una proyección aproximada (§2.4).

## 2. Los cuatro hechos

Notación:

- `L = LocusV2(Q, E)` con conductor `E` sobre el soporte `S` (familia del proveedor: punto de
  circunferencia, de arco circular o de segmento);
- `F` es la evaluación del corte;
- `D` es el punto propuesto;
- `r` es el objetivo opcional.

### 2.1 Hecho 1 — pertenencia de `D` a sus objetos de origen

**Qué afirma.** El punto **exacto** que define el algoritmo de `D` está sobre los objetos de los
que procede. Para `K = Intersect(p, i, 2)`, la salida exacta de índice 2 está en `p ∩ i`.

**Qué no afirma.**

- Que las coordenadas binary64 de `D` cumplan esas ecuaciones: no las cumplen (§5.7 del
  análisis: `K.x = fl(B.x + 5) ≠ B.x + 5`).
- Que `D` sea un punto sobre un camino: `K.isPointOnPath()` es `false`.
- Cuál de las salidas exactas es `D`: lo decide la regla de selección heredada (§6.2).

**Clases de procedencia candidatas** (decisión D-CU3-2):

| Clase | Ejemplo | Admitida | Correspondencia exacta |
|---|---|---|---|
| `PATH_POINT` | `D = Point(S)` o cualquier punto con `isPointOnPath()` y `getPath() == S` | sí | **por definición**: el parámetro de camino de `D` sobre `S`, un valor binary64 exacto que define el punto (§2.3) |
| `DEFINING_POINT` | inicio o final de un segmento o de un arco que es `S` | sí | **por definición**: 0 o 1 |
| `INTERSECTION_OUTPUT` | salida de un algoritmo de intersección heredado, de una tabla cerrada y versionada (`AlgoIntersectLineConic`, `AlgoIntersectConics`, `AlgoIntersectLines`, `AlgoIntersectSingle` con su fuente), **una de cuyas entradas es exactamente `S`** y cuya semántica coloca la salida sobre esa entrada | sí | punto **algebraico** exacto, reconstruido por su algoritmo y su selección (§6.2); el parámetro se encierra (§2.3) |
| `LOCUS_STRUCTURAL` | salida estructural de GeoCeDG sobre un soporte que es a su vez un Locus V2 | no en `CU3` | — |
| cualquier otra | punto libre cercano, imagen por una transformación, punto con coordenadas coincidentes, intersección con un objeto **igual** a `S` pero distinto | **no**: fallo cerrado `PROVENANCE_NOT_ESTABLISHED` | — |

Quedan fuera de la clase, con fallo cerrado, las intersecciones en modo continuo y las
selecciones por punto de referencia: es la misma exclusión que hace la clase v3
(`locus-v2-certified-construction-model.md` §10.2).

### 2.2 Hecho 2 — pertenencia de `D` al soporte concreto del conductor de `L`

**Qué afirma.** El objeto de origen de `D` que lo coloca sobre un soporte es **el mismo objeto**
que el soporte del conductor de `L`: la misma referencia viva y el mismo `PersistentGeoId`.

**Cómo se establece.**

- El descriptor del generador de `L` registra la identidad durable del soporte
  (`SemanticGeneratorDescriptor1D`), y la entrada del algoritmo de `D` debe ser ese objeto.
- En el corte aislado del evaluador, el soporte copiado se resuelve solo por `PersistentGeoId`.
- La correspondencia nunca compara coordenadas entre soportes.

**Fallo cerrado.**

| Situación | Resultado |
|---|---|
| el origen es un objeto distinto aunque coincida geométricamente | `DRIVER_SUPPORT_MISMATCH` |
| el origen es una copia del soporte sin el mismo identificador | `DRIVER_SUPPORT_MISMATCH` |
| `L` no tiene un conductor de punto sobre un camino | `UNSUPPORTED_DRIVER_FAMILY` |
| el soporte de `L` cambió por redefinición | se reevalúa con el soporte nuevo |

**Modelo del autor.** El camino de `E_1` es el objeto `p` (misma referencia y mismo
identificador), y `p` es una entrada del algoritmo de `K`.

### 2.3 Hecho 3 — correspondencia exacta `D ↦ t(D)` y su aproximación numérica

**Tres configuraciones.** Toda afirmación de esta etapa declara a qué configuración se refiere:

| Configuración | Definición | Para qué se usa |
|---|---|---|
| `Σ_ideal` | los valores que escribió el usuario (pueden no ser representables, por ejemplo 47°) | nunca se decide nada en ella |
| `Σ_L` | la **configuración capturada de `L`** de `CU2`: el corte de `L` con los objetos independientes del conductor en su estado binary64, tomados como racionales exactos | autoridad de la estructura de existencia publicada de `L` (componentes, transiciones); `Point(L, D)` |
| `Σ_B` | la **configuración exacta conjunta**: el subgrafo conjunto de `D`, del corte de `L` y de `r`, recalculado exactamente desde una frontera de captura común `B` (§6.1) | autoridad de la segunda incidencia (M3) |

`Σ_L` y `Σ_B` no coinciden en general: `Σ_L` redondea los objetos intermedios que `Σ_B`
recalcula exactamente. La evidencia de una nunca se traslada en silencio a la otra.

**Correspondencia exacta.** En una configuración `Σ`, sea `D*_Σ` el punto exacto de `D`, que
está sobre `S*_Σ` por los hechos 1 y 2. Sea `γ_S` la parametrización del proveedor. La
correspondencia exacta es el `t*_Σ(D)` tal que `γ_S(t*_Σ(D)) = D*_Σ`. Existe porque `γ_S` recorre
`S` entero. Es única módulo el período en una circunferencia, y única en un segmento o en un
arco cuando `D*_Σ` está en la parte limitada. Su orientación es la del conductor, porque usa la
misma `γ_S`.

`t*_Σ(D)` es en general un número **trascendente** (un ángulo), y no se calcula. Se usa de tres
maneras:

| Garantía | Contenido | Cuándo |
|---|---|---|
| **exacta** | `t*` se conoce exactamente: el parámetro de camino de un `PATH_POINT`, el 0 o el 1 de un `DEFINING_POINT`, o un valor que la aritmética exacta decide (por ejemplo, `t*(K) = 0` porque `K* − c` es un múltiplo positivo del primer vector propio). Para M3 nunca se necesita `t*`: se sustituye el **punto** exacto `D*`, no un parámetro (§6.3) | procedencias por definición; decisiones de M3 |
| **encerrada** | un intervalo `[t] ∋ t*_Σ(D)`, obtenido evaluando por intervalos la inversa del proveedor sobre un encierro certificado `[D] ∋ D*_Σ`. `[D]` sale de evaluar por intervalos el algoritmo de `D` sobre sus operandos capturados y con la selección exacta (§6.2), **nunca de las coordenadas binary64 de `D` solas**, que no traen una cota de error | `INTERSECTION_OUTPUT` en `Σ_L` |
| **numérica** | `t̃(D)`, la proyección canónica del proveedor aplicada a las coordenadas binary64 de `D` (vía a), y el punto mostrado `P̃ = F(t̃(D))` del evaluador | solo para la posición mostrada; **no** es evidencia de pertenencia ni de incidencia |

**Vías de cálculo** (decisión D-CU3-3):

- **(a)** proyección canónica del proveedor: da el valor numérico `t̃` y, evaluada por
  intervalos sobre `[D]`, el encierro `[t]`;
- **(b)** colocar el conductor vivo por el DAG: rechazada, porque mueve el conductor del usuario
  y da el mismo valor que (a);
- **(c)** ecuación del algoritmo de origen: no recomendada como vía separada; su contenido útil
  es la reconstrucción exacta de `D*` (§6.2);
- **(d)** rechazo con su causa, cuando no hay vía demostrable.

**Reglas de fallo cerrado de la correspondencia**, aplicadas en `Σ_L` para `Point(L, D)` y
repetidas en `Σ_B` por M3 para `Intersect(L, r, D)`:

| Región | Regla | Causa |
|---|---|---|
| interior de un dominio acotado | `[t]` contenido en el interior: pertenencia certificada (condición abierta) | — |
| extremo de un dominio acotado | si la procedencia prueba que `D*` está en la **parte limitada** de `S` (un `DEFINING_POINT`, o una intersección cuyo algoritmo coloca la salida sobre ese segmento o arco), entonces `t*` está en el dominio y `[t] ∩ dominio` es un encierro válido: el recorte lo justifica la estructura, no una tolerancia. Si la procedencia solo prueba que `D*` está en la recta o la circunferencia soporte, la pertenencia queda indecisa | `OUT_OF_DRIVER_DOMAIN` si `[t]` está fuera; `DRIVER_DOMAIN_UNDECIDED` si lo corta |
| costura de un dominio periódico | `P* = F(t*)` no depende del representante. La pertenencia a una componente se decide con `[t]` en el levantamiento: si la costura es interior a una componente certificada o la componente es periódica cerrada (autoridad única de cierre de `CU2`), vale; si la costura es un extremo de componente y `[t]` la corta, queda indecisa | `SEAM_SIDE_UNDECIDED` |
| componente continua certificada | `[t]` contenido en el **interior** de una componente continua certificada de `L` (condición abierta) | — |
| extremo de componente, transición certificada, banda `UNRESOLVED`, hueco no válido o discontinuidad | si `[t]` corta cualquiera de ellos, la pertenencia queda indecisa. Un extremo incluido es una condición cerrada (`t*` igual al parámetro del extremo), que solo M3 puede decidir en `Σ_B` evaluando exactamente el predicado de validez en `D*` | `COMPONENT_BOUNDARY_UNDECIDED` |
| componente puntual | §2.4: fuera de la materialización de `CU3` | `POINT_COMPONENT_NOT_MATERIALIZED` |
| `[D]` no disponible (algoritmo fuera de la tabla o fuera de clase) | sin encierro no hay pertenencia certificada | `CORRESPONDENCE_NOT_CERTIFIED` |

**Modelo del autor.** En `Σ_L`, `K*` es la intersección exacta de la `p` capturada (centro `B`,
radio 5) con la `i` capturada, `K* = (B.x + 5, B.y)`, y `t*(K) = 0` exactamente. El valor
numérico `t̃(K)` es también `0`, porque `fl(B.x + 5)` y `B.y` dan `K.y − B.y = 0`, y el conductor
colocado en `0` reproduce `K` bit a bit. Para `J`, `t*(J) = π`, que no es representable: la
garantía es encerrada, y el valor canónico `−π` es solo numérico.

### 2.4 Hecho 4 — evaluación de `F(t(D))` y segunda incidencia

**`P ∈ L`.** `P = F(t*(D))` es un punto de `L` por construcción **solo si** `t*(D)` está en el
dominio de existencia de `L`. En `CU3` se exige que `[t]` esté contenido en el **interior** de una
componente **continua** certificada de `Σ_L` (§2.3). Si la evaluación numérica falla con la
existencia certificada, la posición es `UNAVAILABLE`, no inexistente (V-POINT-4 de `CU2`).

**Componentes puntuales** (decisión D-CU3-9). El contrato de `CU2` (§15.4) dice que una
componente puntual existe aunque ningún parámetro binary64 alcance su parámetro exacto; que su
pertenencia y su selección usan su dirección estructural y nunca la igualdad con un parámetro
binary64; y que **no crea ningún objeto de punto público ni materializa intersecciones**. Por
tanto:

- que `t*(D)` coincida con el parámetro de una componente puntual es una condición cerrada, que
  ningún encierro decide;
- en `CU3` **no se ha demostrado** un direccionamiento estructural entre la procedencia de `D` y
  la fuente estructural de una componente puntual;
- si `[t]` corta el encierro de una componente puntual, o está fuera de toda componente continua
  y dentro de una puntual, el punto queda indefinido con `POINT_COMPONENT_NOT_MATERIALIZED`;
- `CU3` no introduce las capacidades de C22 y C23. Decidir exactamente la pertenencia (por
  ejemplo, con M3 evaluando el predicado de validez en `D*`) y materializar ahí un punto
  necesitan su propia autorización.

**`P ∈ r`.** Es un hecho aparte, que solo afirma `Intersect(L, r, D)`. Es una **igualdad en un
punto prescrito**, una condición cerrada (A3.0, caso b), que se establece solo:

- por identidad de términos tras la sustitución del conductor (M1), o
- por evaluación exacta en `Σ_B` (M3, §6).

**Nunca** por un residuo bajo una tolerancia, ni por la proximidad entre `P̃` y `r`, ni por un
encierro de `h(P)` que contenga el cero. Si no se establece:

- `Intersect(L, r, D)` queda indefinido con `INCIDENCE_NOT_ESTABLISHED`, o con
  `INCIDENCE_REFUTED` si M3 decide exactamente que `h ≠ 0` en `Σ_B`;
- su evidencia numérica se muestra en el inspector;
- `Point(L, D)` sigue definido, porque no afirma la incidencia.

**Objetivo limitado.** Si `r` es un segmento, un rayo o un arco, hace falta además la pertenencia
del punto a la parte limitada, que M3 decide exactamente en `Σ_B` evaluando el parámetro del
objetivo. Si falta: `NOT_ON_LIMITED_TARGET`.

## 3. Superficie pública candidata (F-1-R)

| Forma | Significado | Afirma | Configuración |
|---|---|---|---|
| `Point(L, D)` | punto de `L` en la posición del conductor que corresponde a `D` | `P ∈ L` (hechos 1–4, primera parte) | `Σ_L`; no necesita M3 |
| `Intersect(L, r, D)` | el mismo punto como intersección de `L` con `r` | además, `P ∈ r` demostrado | `P ∈ L` en `Σ_L` **y** en `Σ_B`; `P ∈ r` en `Σ_B` |

`Intersect(L, r, D)` es estrictamente más fuerte que `Point(L, D)`: exige todo lo de `Point` y
además la incidencia y la validez del corte en `Σ_B`. Su evidencia registra las dos
configuraciones.

**Colisión con una forma heredada.** GeoGebra ya tiene `Intersect(<Object>, <Object>, <Initial
Point>)`, cuyo tercer argumento es un punto de arranque de una iteración numérica. En
`Intersect(L, r, D)`, con `L` Locus V2, `D` no es nunca un punto inicial; hoy el producto
rechaza esa forma (P10).

Alternativas para la decisión D-CU3-1:

- **A**, recomendada: `Point(L, D)` e `Intersect(L, r, D)`, documentando `D` como correspondencia
  del conductor;
- B: un comando propio para la incidencia;
- C: solo `Point(L, D)`.

## 4. Objeto semántico, identidad y DAG

- **Algoritmo nuevo** del núcleo compartido (nombre de trabajo
  `AlgoLocusDriverCorrespondencePoint2D`). Entradas: `L`, `D` y, en la segunda forma, `r`.
  Salida: un punto. Su `compute()` resuelve los hechos 1 a 4 en orden y para en el primero que
  falla, con su causa.
- **Identidad del punto:** su propia identidad durable, más las identidades durables de `L`, `D` y
  `r` como entradas. No contiene token de raíz, parámetro, coordenadas, índice de salida,
  configuración ni proximidad.
- **No se persiste ningún parámetro ni ningún certificado.** Deshacer, Rehacer, guardar, reabrir y
  copiar reconstruyen el punto desde el DAG, y los certificados se recalculan por revisión.
- **Dependencias:** cualquier cambio de `L` (revisión), de `D` o de `r` propaga por el grafo.
- **Permutación de salidas del origen.** Si la selección heredada intercambia las salidas del
  algoritmo de `D`, `D*` cambia con ella (§6.2) y `P` lo sigue. No se conservan coordenadas
  antiguas ni se reasocia nada.

## 5. Vigencia, desaparición, degeneraciones y recuperación

| Situación | `Point(L, D)` | `Intersect(L, r, D)` | Causa |
|---|---|---|---|
| `D` indefinido | indefinido | indefinido | `DRIVER_POINT_UNDEFINED` |
| procedencia no admitida o soporte distinto | indefinido | indefinido | `PROVENANCE_NOT_ESTABLISHED`, `DRIVER_SUPPORT_MISMATCH` |
| selección heredada no reconstruible (raíces coincidentes, empate, inicialización no certificada) | indefinido | indefinido | `SELECTION_NOT_RECONSTRUCTED` |
| `[t]` fuera del dominio, o indeciso en un extremo, en la costura o en un borde de componente | indefinido | indefinido | causas de §2.3 |
| `[t]` en una componente puntual | indefinido | indefinido | `POINT_COMPONENT_NOT_MATERIALIZED` |
| `L` con `Completeness = NOT_ESTABLISHED`, pero `[t]` dentro de una componente continua certificada | **definido** (admisibilidad local, ADR 0027) | según la incidencia | — |
| M3 fuera de clase o con el presupuesto agotado | definido | indefinido | `EXACT_EVALUATION_OUT_OF_CLASS`, `EXACT_EVALUATION_BUDGET_EXHAUSTED` |
| M3 decide `h ≠ 0` | definido | indefinido | `INCIDENCE_REFUTED` |
| `r` indefinido, o fuera de su parte limitada | definido | indefinido | `TARGET_UNDEFINED`, `NOT_ON_LIMITED_TARGET` |
| la condición vuelve a cumplirse | definido de nuevo con la misma identidad | ídem | — |

El modelo del autor lo ilustra (análisis §5.5): con `D_1` ×3, `Intersect(e_1, n, K)` quedaría
indefinido con `NOT_ON_LIMITED_TARGET`, mientras que `Intersect(e_1, c_1, K)` y `Point(e_1, K)`
seguirían definidos.

## 6. Segunda incidencia: mecanismos y M3

### 6.1 Mecanismos

| Mecanismo | Contenido | Modelo del autor | Recomendación |
|---|---|---|---|
| **M1** identidad de términos tras la sustitución del conductor | el término de `r` es igual al de un objeto del corte incidente con el punto trazado, con `E := D` | **no basta**: las incidencias dependen de `k ∥ g`, de una reflexión y de la simetría de `c_1` (análisis §2 y §5.3) | vía rápida cuando aplica |
| **M2** reglas sintéticas con no degeneración certificada | base de reglas siempre incompleta | parcial | no se recomienda |
| **M3** evaluación exacta en `Σ_B` | §6.2 y §6.3 | recalculada con 80 cifras sobre la frontera `{H, B, altura de h, rbCone, D_1}`: `0E-79` en las dos ramas (P3) | **dirección arquitectónica candidata del núcleo**, sujeta a la puerta temprana de §6.5 |
| **M4** prover simbólico de GeoGebra | «generalmente verdadero», CAS externo | — | solo evidencia |
| **M5** residuo numérico | `|h| ≤ ε` | clase 3 | nunca autoridad |

### 6.2 Reconstrucción exacta de `D` y frontera de captura

**Por qué hace falta una configuración conjunta.** Si M3 tomara como constantes exactas los
estados binary64 de los objetos intermedios, las relaciones estructurales se romperían. En el
modelo, `K.x` binary64 es `fl(B.x + 5)`, que difiere de `B.x + 5` en `4,44·10⁻¹⁶` (análisis §5.7).
Una `n` capturada a través del `M` redondeado ya no pasaría exactamente por el contacto en
`K* = (B.x + 5, B.y)`, y M3 **refutaría** una incidencia que es un teorema. Es la cuestión abierta
1 de la preparación de `CU2`: conservar la relación exacta exige capturar simbólicamente el
subgrafo independiente del conductor.

**Frontera de captura `B`.** El subgrafo conjunto `G = ancestros(D) ∪ corte(L) ∪ ancestros(r)` se
recalcula exactamente desde una frontera de objetos cuyo estado binary64 se toma como racional
exacto:

1. elementos independientes (puntos libres, números) y parámetros de camino de puntos sobre
   caminos de la frontera;
2. objetos cuyo cálculo exacto saldría de la clase (rotaciones por ángulos no especiales,
   funciones trascendentes, operaciones fuera de la tabla). En el modelo, `H = Point[j]` depende
   de 47° a través de `j`: `H` entra en la frontera con su valor binary64.

Todo lo que está por encima de la frontera se **recalcula** exactamente, incluidos `S`, `D`, el
corte y `r`. Así `D*` está exactamente sobre `S*` de `Σ_B`, y el corte usa la misma `S*`.

**Alcance:**

- un teorema que vale para cualquier valor de los objetos de la frontera (como `J_1(K) = M_1`, que
  vale para cualquier `H`, `B`, `A`, `rbCone` y `D_1`) se decide en `Σ_B`;
- una relación que solo vale por cancelaciones entre las definiciones trascendentes de objetos de
  la frontera se pierde; M3 la refuta en `Σ_B`, y la causa se registra como
  `INCIDENCE_REFUTED_IN_CAPTURED_CONFIGURATION`, nunca como inexistencia en `Σ_ideal`.

**Selección heredada.** Cada algoritmo de varias salidas del subgrafo (`K = Intersect[p, i]`,
`H_1 = Intersect[d_1, i, 1]`, `G_1 = Intersect[c_1, s_1, 1]`, `M_1 = Intersect[c_1, n]`) se evalúa
con la **versión exacta de la regla de §10.2** de la clase v3:

- las raíces exactas se ordenan por el parámetro a lo largo de la recta del algoritmo, una
  comparación exacta;
- se aplica el estado de selección capturado (permutación, indicadores de inicialización y
  etiquetas) mediante los accesores de solo lectura de `CU2` (D-CU2-17);
- si dos raíces exactas coinciden, la selección no es reconstruible y la regla de supresión
  heredada de §10.3 no se reproduce: `SELECTION_NOT_RECONSTRUCTED`;
- las intersecciones en modo continuo, las selecciones por punto de referencia y una
  inicialización cuyo número de raíces no esté probado constante quedan fuera, con fallo cerrado.

**Las coordenadas binary64 de `D` no son autoridad geométrica.** No entran en el cálculo exacto.
Se usan solo en la **comprobación de correspondencia con el algoritmo vivo** (§6.5, G-M3-3): el
encierro por intervalos de la rama exacta elegida debe contener el valor vivo y ser disjunto del
de la otra rama. Si no, `SELECTION_NOT_RECONSTRUCTED`.

### 6.3 Decisión exacta

En `Σ_B`, con el conductor sustituido por el **punto** exacto `D*` (el par `(cos t*, sin t*)` de una
circunferencia es `(D* − c)/r`, algebraico, sin calcular `t*`):

- los programas de regla y compás generan una torre finita de extensiones cuadráticas de los
  racionales de la frontera;
- el signo y la nulidad del residuo `h` del objetivo, de los predicados de validez del corte y del
  parámetro de un objetivo limitado se deciden con aritmética exacta y cotas constructivas de
  separación de raíces;
- el resultado es «igual», «distinto» o «fuera de clase o presupuesto agotado». Nunca es «igual
  dentro de una tolerancia».

**Igualdades accidentales.** Una igualdad que **no es estructural** (la coincidencia de valores
escritos) se demuestra **si y solo si es exacta en `Σ_B`**. Por ejemplo, una circunferencia de
radio 1,5 trazada y la recta `y = 1,5`: los dos valores son representables y la tangencia es
exacta en `Σ_B`, así que M3 la demuestra. Si la coincidencia solo vale en `Σ_ideal` con valores
no representables (por ejemplo, `0.1 + 0.2` frente a `0.3`), M3 decide exactamente la
desigualdad en `Σ_B` (`INCIDENCE_REFUTED`). Esto corrige la formulación anterior de esta
preparación, que decía que una igualdad accidental «en general no se prueba».

### 6.4 Modelo del autor

La frontera es `{A, B (parámetro de camino), rbCone, H, D_1}`. Por encima de ella, el subgrafo es
de regla y compás, y la torre es pequeña:

- `|HD_1|` y `|HM|` dan dos raíces cuadradas;
- `|HI_1| = |HM|` exactamente, por la reflexión, así que la tercera es la misma;
- `d_1 ∩ i` es racional, porque su discriminante es el cuadrado de `|IK|`.

La evidencia P3, una evaluación con 80 cifras sobre esa frontera y no una prueba exacta, da
`|J_1 − M_1| = 0E-79` en las dos ramas de `H_1`.

### 6.5 Puerta temprana y bloqueante de M3 (G-M3)

Se ejecuta **antes** de implementar la segunda incidencia de la Etapa A, inmediatamente después de
las pruebas de caracterización (estrategia, paso T2). Si no se supera, la implementación de esa
parte se detiene y se presentan las alternativas. Tiene tres criterios:

| Criterio | Contenido | Se supera si |
|---|---|---|
| **G-M3-1** viabilidad matemática | los cinco contactos obligatorios (`e_1·n`, `e_1·c_1` y `o_1·h_2` en `K`; `e_1·c_1` y `e_1·r` en `J`), con su frontera, sus subgrafos dentro de la clase y la reconstrucción exacta de `K` y `J` | M3 decide **igual** en los cinco y en las dos ramas de `H_1`; decide **distinto** en un control negativo (objetivo desplazado un ulp); decide **igual** en un control accidental exacto; y decide **distinto** en un control accidental no representable |
| **G-M3-2** presupuesto computacional | contadores deterministas: operaciones exactas, grado de la torre, longitud máxima en bits; arrastre del conductor sin cambio de revisión (GC3-3) y cambio de parámetro (GC3-4) | los cinco contactos por debajo de un techo versionado, fijado por el banco del candidato y aprobado por el autor con la autorización |
| **G-M3-3** correspondencia con el algoritmo vivo | para cada algoritmo de varias salidas del subgrafo: la rama exacta elegida por la regla exacta de §10.2 con el estado capturado; el encierro de esa rama contiene el valor vivo y es disjunto del de la otra rama; la fidelidad de reconstrucción de `CU2` (D-CU2-20-R) se cumple | todas las salidas de los cinco casos |

**Si M3 resulta inviable para los casos obligatorios**, nunca se sustituye su evidencia por un
residuo tolerado. Alternativas, que se presentan al autor con la evidencia de la puerta:

| Alternativa | Contenido | Consecuencia |
|---|---|---|
| R-1 | `Point(L, D)` sin M3 y `Intersect(L, r, D)` indefinido con `INCIDENCE_NOT_ESTABLISHED` y la evidencia de clase 3 | B08 y B09 no se cierran como intersecciones; el compromiso del plan §6 para `CU3` necesita una decisión autoral |
| R-2 | otro método exacto: eliminación algebraica (resultantes) sobre la misma frontera, o parametrización racional del conductor (tangente del semiángulo) con aritmética racional | otra puerta equivalente antes de adoptarlo |
| R-3 | aplazar la segunda incidencia de los casos obligatorios con una limitación documentada | decisión autoral expresa, porque el plan §6 la hace compromiso de cierre |
| — | aceptar un residuo, una proximidad o el prover simbólico como autoridad | **rechazada** |

## 7. Los tres casos obligatorios

| Forma | Hechos 1–3 | `P ∈ L` | `P ∈ r` | Resultado previsto |
|---|---|---|---|---|
| `Intersect(e_1, n, K)` | se cumplen: salida 2 de `AlgoIntersectLineConic(p, i)`; `p` es el soporte de `E_1`; `t*(K) = 0` exacto | `[t] = {0}` dentro de una componente certificada `COMPLETE` | teorema del análisis §5.3; M3 en `Σ_B` (puerta G-M3) | `M_1`, dinámico; orden `FOLD_CONTACT` |
| `Intersect(e_1, c_1, K)` | igual | igual | igual | `M_1`, dinámico |
| `Intersect(o_1, h_2, K)` | igual | igual (`o_1`) | `K_1 ∈ b_1 = h_2`; M3 | `N_1`, dinámico; tangencia regular |
| `Intersect(e_1, c_1, J)` e `Intersect(e_1, r, J)` (costura) | `J` es la salida 1; `t*(J) = π` encerrado | `[t]` alrededor de `π`: la costura es interior a una componente cerrada de `e_1`, o la regla de la costura lo decide | el mismo teorema con la generatriz `r`; M3 | el punto de la costura, como contacto estructural en el pliegue |

**Dinámica.** Con `rbCone`, `α_cone`, `D_1` o `excCyl` cambiados, `K`, `t*(K) = 0` y los contactos
siguen la construcción con la misma identidad (P4). `Point(o_1, K)` sustituye al rodeo del autor
`Point(o_1, text2, v_1)`.

**Desaparición.** Con `|HD_1| > |HM|`, `Intersect(e_1, n, K)` da `NOT_ON_LIMITED_TARGET` y
reaparece al volver.

## 8. Persistencia y compatibilidad

- Se serializa como `<command name="Point">` o `<command name="Intersect">` con los argumentos
  `L`, `D` (y `r`), más la identidad durable del punto. No hay ningún elemento XML nuevo.
- **Lectores anteriores.** El producto actual rechaza las dos formas (P10). Si la carga completa
  del documento se aborta, no se ha comprobado (V-A-COMPAT-2). Hace falta la nota de migración de
  `AGENTS.md` §11.6 (D-CU3-12).
- La Etapa A solo añade formas nuevas y no cambia ningún documento existente.

## 9. Diagnósticos tipados (candidatos)

`DRIVER_POINT_UNDEFINED`, `PROVENANCE_NOT_ESTABLISHED`, `DRIVER_SUPPORT_MISMATCH`,
`UNSUPPORTED_DRIVER_FAMILY`, `SELECTION_NOT_RECONSTRUCTED`, `CORRESPONDENCE_NOT_CERTIFIED`,
`OUT_OF_DRIVER_DOMAIN`, `DRIVER_DOMAIN_UNDECIDED`, `SEAM_SIDE_UNDECIDED`,
`COMPONENT_BOUNDARY_UNDECIDED`, `POINT_COMPONENT_NOT_MATERIALIZED`, `TARGET_UNDEFINED`,
`INCIDENCE_NOT_ESTABLISHED`, `INCIDENCE_REFUTED`, `INCIDENCE_REFUTED_IN_CAPTURED_CONFIGURATION`,
`NOT_ON_LIMITED_TARGET`, `EXACT_EVALUATION_BUDGET_EXHAUSTED` y `EXACT_EVALUATION_OUT_OF_CLASS`.

Cada uno es un fallo cerrado: ningún punto conserva coordenadas antiguas ni se reasocia.

## 10. Relación con otros paquetes

- `CU7` (M08, punto sobre Locus V2 por interacción) es otra familia: la Etapa A no crea puntos
  por arrastre ni por proximidad.
- Las componentes puntuales de `CU2` quedan fuera (§2.4). La materialización en ellas y el
  seguimiento de C40 necesitan su propia autorización (D-CU3-9).
