# PRE-G9B-CU3 — Etapa A: puntos estructurales por correspondencia del conductor

| Campo | Valor |
|---|---|
| Carácter | Diseño candidato de la Etapa A de `CU3`. No es especificación normativa ni autoriza implementación |
| Estado | `CU3 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`, revisado por la revisión científica focal y por la comprobación final de coherencia (2026-10-24); `selfApproved=false` |
| Encargo | Instrucción autoral «PRE-G9B-CU3 — Autorización de preparación arquitectónica, científica y normativa» (2026-10-24), §1–§4; precisión autoral «Decisión sobre la planificación de CU3»; instrucción «PRE-G9B-CU3 — Revisión científica focal del candidato de preparación», puntos 2, 3 y 6; instrucción «PRE-G9B-CU3 — Comprobación final de coherencia científica», punto 1 (todas de 2026-10-24) |
| Autoridad vigente | `locus-v2-semantics.md` v1.3 (conductores y proveedores); `locus-v2-point-interaction.md` §7, §12, §16 y §17; `locus-v2-existence-components.md` v1.1 §15 (en particular §15.4, componentes puntuales); `locus-v2-certified-construction-model.md` v1.2 §3 (constantes capturadas) y §10 (en particular §10.1–§10.3); `locus-v2-public-surface.md` §6 y §8–§10; `locus-v2-intersections.md` §5 y §8; ADR 0008 (aclaración G8B-R1), 0027 y 0038; `AGENTS.md` §1, §4 y §11.3–§11.5 |
| Antecedente | [propuesta F](pre_g9b_cu0_tangent_materialization_proposal.md) §4 (F-1); enmienda candidata A3.3 de `CU0` |
| Evidencia | [análisis del estado inicial](pre_g9b_cu3_initial_state_and_capability_analysis.md) §2, §3.3, §5, §5.7 y §5.8 |

## 1. Objetivo

Materializar un punto de un Locus V2 `L` **en la posición del conductor que corresponde a un
punto `D`**, sin buscar raíces. Opcionalmente, ese punto se materializa como intersección con un
objetivo `r` cuando la segunda incidencia está demostrada **para ese mismo punto**. Es la Etapa A
de DA-3: puntos estructurales con las dos incidencias.

La comprobación final de coherencia pide precisar qué objeto geométrico exacto materializa
`Intersect(L, r, D)`, y garantizar que la evaluación exacta M3 certifica una propiedad del objeto
CeDG autoritativo y no la de una reconstrucción algebraica cercana. Este documento lo fija en §2.3
y en §6.

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

- Que las coordenadas binary64 de `D` cumplan esas ecuaciones: no las cumplen (análisis §5.7:
  `K.x = fl(B.x + 5) ≠ B.x + 5`).
- Que `D` sea un punto sobre un camino: `K.isPointOnPath()` es `false`.
- Cuál de las salidas exactas es `D`: lo decide la regla de selección heredada (§6.3).

**Clases de procedencia candidatas** (decisión D-CU3-2):

| Clase | Ejemplo | Admitida | Correspondencia exacta |
|---|---|---|---|
| `PATH_POINT` | `D = Point(S)` o cualquier punto con `isPointOnPath()` y `getPath() == S` | sí | **por definición**: el parámetro de camino de `D` sobre `S`, un valor binary64 exacto que define el punto |
| `DEFINING_POINT` | inicio o final de un segmento o de un arco que es `S` | sí | **por definición**: 0 o 1 |
| `INTERSECTION_OUTPUT` | salida de un algoritmo de intersección heredado, de una tabla cerrada y versionada (`AlgoIntersectLineConic`, `AlgoIntersectConics`, `AlgoIntersectLines`, `AlgoIntersectSingle` con su fuente), **una de cuyas entradas es exactamente `S`** y cuya semántica coloca la salida sobre esa entrada | sí | punto **algebraico** exacto en la configuración de decisión (§6.3); el parámetro se encierra (§2.3) |
| `LOCUS_STRUCTURAL` | salida estructural de GeoCeDG sobre un soporte que es a su vez un Locus V2 | no en `CU3` | — |
| cualquier otra | punto libre cercano, imagen por una transformación, punto con coordenadas coincidentes, intersección con un objeto **igual** a `S` pero distinto | **no**: fallo cerrado `PROVENANCE_NOT_ESTABLISHED` | — |

Quedan fuera, con fallo cerrado, las intersecciones en modo continuo y las selecciones por punto
de referencia (la misma exclusión de `locus-v2-certified-construction-model.md` §10.2).

### 2.2 Hecho 2 — pertenencia de `D` al soporte concreto del conductor de `L`

**Qué afirma.** El objeto de origen de `D` que lo coloca sobre un soporte es **el mismo objeto**
que el soporte del conductor de `L`: la misma referencia viva y el mismo `PersistentGeoId`.

**Cómo se establece.**

- El descriptor del generador de `L` registra la identidad durable del soporte, y la entrada del
  algoritmo de `D` debe ser ese objeto.
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

### 2.3 Hecho 3 — configuración autoritativa y correspondencia exacta `D ↦ t(D)`

**Autoridad geométrica.** El objeto CeDG autoritativo es `L` tal como lo certifica `CU2`: su
**configuración capturada** `Σ_L`, que es el corte de `L` con sus constantes independientes del
conductor en su estado binary64, tomadas como racionales exactos (D-CU2-16;
`locus-v2-certified-construction-model.md` §3 y §10). Es la configuración de sus componentes, sus
transiciones y su estructura publicada.

**Las configuraciones y su papel.**

| Configuración | Definición | Papel |
|---|---|---|
| `Σ_ideal` | los valores que escribió el usuario (pueden no ser representables, por ejemplo 47°) | ninguno: nunca se decide nada en ella |
| `Σ_L` | la configuración capturada de `L` (`CU2`) | **autoridad**: existencia de `P` en `L`, componentes, correspondencia de `Point(L, D)` |
| `Σ_L⁺` | `Σ_L` **sin ningún cambio**, ampliada con `D`, `r` y los ancestros de ambos que no son constantes de `Σ_L`, con las reglas de §6.2 | **decisión** de la segunda incidencia (M3) y de los conteos certificados de la regla de conflicto |
| `Σ_B` | el subgrafo conjunto recalculado desde una frontera profunda, que también recalcula las constantes de `Σ_L` | **solo diagnóstico** (§6.6): nunca es autoridad ni se transfiere sin la condición de §6.6 |

`Σ_L ⊂ Σ_L⁺` literalmente: las mismas constantes con los mismos valores exactos y el mismo
programa de clase v3. Por eso un hecho decidido en `Σ_L⁺` sobre el punto del corte de `L` es un
hecho **del objeto autoritativo**, y no hay ninguna transferencia que justificar.

**Correspondencia exacta.** En `Σ_L⁺`, sea `D*` el punto exacto de `D` (§6.3). Por los hechos 1 y
2 está sobre el soporte `S` con **la semántica exacta que `S` tiene en `Σ_L`**, la de las
constantes del conductor (§6.2, regla 2). La correspondencia exacta es el `t*(D)` tal que
`γ_S(t*(D)) = D*`. Existe porque `γ_S` recorre `S`; es única módulo el período en una
circunferencia, y única en un segmento o en un arco cuando `D*` está en la parte limitada. Su
orientación es la del conductor.

`t*(D)` es en general **trascendente** (un ángulo), y no se calcula. Se usa con tres garantías:

| Garantía | Contenido | Cuándo |
|---|---|---|
| **exacta** | `t*` se conoce exactamente: el parámetro de camino de un `PATH_POINT`, el 0 o el 1 de un `DEFINING_POINT`, o un valor que la aritmética exacta decide (`t*(K) = 0` porque `K* − c` es un múltiplo positivo del primer vector propio). M3 nunca necesita `t*`: sustituye el **punto** `D*` | procedencias por definición; decisiones de M3 |
| **encerrada** | un intervalo `[t] ∋ t*(D)`, que resulta de evaluar por intervalos la inversa del proveedor sobre un encierro certificado `[D] ∋ D*`, y `[D]` de evaluar por intervalos el algoritmo de `D` sobre las semánticas de `Σ_L⁺` de sus operandos, con la selección exacta; **nunca de las coordenadas binary64 de `D` solas** | `INTERSECTION_OUTPUT` |
| **numérica** | `t̃(D)`, la proyección canónica del proveedor aplicada a las coordenadas binary64 de `D`, y la posición mostrada `P̃ = F(t̃(D))` | solo para mostrar; **no** es evidencia de pertenencia ni de incidencia |

**Vías de cálculo** (decisión D-CU3-3):

- **(a)** proyección canónica del proveedor: el valor numérico `t̃` y, por intervalos sobre `[D]`,
  el encierro `[t]`;
- **(b)** colocar el conductor vivo por el DAG: rechazada, porque mueve el conductor del usuario;
- **(c)** ecuación del algoritmo de origen: su contenido útil es la reconstrucción exacta de `D*`
  (§6.3);
- **(d)** rechazo con su causa.

**Reglas de fallo cerrado de la correspondencia** (en `Σ_L`, con `[t]`):

| Región | Regla | Causa |
|---|---|---|
| interior de un dominio acotado | `[t]` contenido en el interior: pertenencia certificada (condición abierta) | — |
| extremo de un dominio acotado | si la procedencia prueba que `D*` está en la **parte limitada** de `S` (un `DEFINING_POINT`, o una intersección cuyo algoritmo coloca la salida sobre ese segmento o arco), `t*` está en el dominio y `[t] ∩ dominio` es un encierro válido, porque el recorte lo justifica la estructura y no una tolerancia; si solo prueba que está en la recta o la circunferencia soporte, la pertenencia queda indecisa | `OUT_OF_DRIVER_DOMAIN` si `[t]` está fuera; `DRIVER_DOMAIN_UNDECIDED` si lo corta |
| costura de un dominio periódico | si la costura es interior a una componente certificada o la componente es periódica cerrada (autoridad única de cierre de `CU2`), vale; si la costura es un extremo de componente y `[t]` la corta, queda indecisa | `SEAM_SIDE_UNDECIDED` |
| componente continua certificada | `[t]` contenido en el **interior** de una componente continua certificada de `L` (condición abierta) | — |
| extremo de componente, transición certificada, banda `UNRESOLVED`, hueco no válido o discontinuidad | si `[t]` corta alguno, la pertenencia queda indecisa; un extremo incluido es una condición cerrada, que solo M3 puede decidir en `Σ_L⁺` evaluando exactamente el predicado de validez en `D*` | `COMPONENT_BOUNDARY_UNDECIDED` |
| componente puntual | §2.4: fuera de la materialización de `CU3` | `POINT_COMPONENT_NOT_MATERIALIZED` |
| `[D]` no disponible | sin encierro no hay pertenencia certificada | `CORRESPONDENCE_NOT_CERTIFIED` |

**Modelo del autor.** Con la semántica de conductor de `p` en `Σ_L` (centro `B` y semieje 5),
`K* = (B.x + 5, B.y)` y `t*(K) = 0` exactamente. El valor numérico `t̃(K)` es también `0`, y el
conductor colocado en `0` reproduce `K` bit a bit. Para `J`, `J* = (B.x − 5, B.y)` y `t*(J) = π`,
que no es representable: la garantía es encerrada, y el valor canónico `−π` es solo numérico.

### 2.4 Hecho 4 — evaluación de `F(t(D))` y segunda incidencia

**`P ∈ L`.** `P = F(t*(D))` es un punto de `L` por construcción **solo si** `t*(D)` está en el
dominio de existencia de `L`. En `CU3` se exige que `[t]` esté contenido en el **interior** de una
componente **continua** certificada de `Σ_L`. Si la evaluación numérica falla con la existencia
certificada, la posición es `UNAVAILABLE`, no inexistente (V-POINT-4 de `CU2`).

**Componentes puntuales** (decisión D-CU3-9). El contrato de `CU2` (§15.4) dice que una
componente puntual existe aunque ningún parámetro binary64 alcance su parámetro exacto, que su
pertenencia y su selección usan su dirección estructural, y que **no crea ningún objeto de punto
público ni materializa intersecciones**. Por tanto:

- que `t*(D)` coincida con el parámetro de una componente puntual es una condición cerrada que
  ningún encierro decide;
- en `CU3` no se ha demostrado un direccionamiento estructural entre la procedencia de `D` y la
  fuente estructural de una componente puntual;
- si `[t]` corta el encierro de una componente puntual, el punto queda indefinido con
  `POINT_COMPONENT_NOT_MATERIALIZED`;
- `CU3` no introduce las capacidades de C22 y C23.

**`P ∈ r`.** Es un hecho aparte, que solo afirma `Intersect(L, r, D)`. Es una **igualdad en un
punto prescrito** (A3.0, caso b), y se establece **para el mismo punto `P` de `Σ_L`** solo:

- por identidad de términos tras la sustitución del conductor (M1), o
- por evaluación exacta en `Σ_L⁺` (M3, §6).

Nunca se establece por un residuo bajo una tolerancia, ni por la proximidad entre `P̃` y `r`, ni
por un encierro de `h(P)` que contenga el cero, ni por una demostración en `Σ_B` (§6.6). Si no se
establece:

- `Intersect(L, r, D)` queda indefinido con la causa de §5;
- su evidencia se muestra en el inspector;
- `Point(L, D)` sigue definido.

**Objetivo limitado.** Si `r` es un segmento, un rayo o un arco, M3 decide además en `Σ_L⁺` la
pertenencia del punto a su parte limitada. Si falta: `NOT_ON_LIMITED_TARGET`.

## 3. Superficie pública candidata (F-1-R)

| Forma | Objeto materializado | Afirma | Configuración |
|---|---|---|---|
| `Point(L, D)` | `P = F(t*(D))` en `Σ_L` | `P ∈ L` | `Σ_L`; no necesita M3 |
| `Intersect(L, r, D)` | **el mismo punto `P`** de `Σ_L` | `P ∈ L` y `P ∈ r` | `P ∈ L` en `Σ_L`; `P ∈ r` en `Σ_L⁺`, que contiene `Σ_L` |

`Intersect(L, r, D)` no materializa un punto distinto: materializa el punto de `Point(L, D)` con
la incidencia adicional demostrada para él. Su evidencia registra la configuración de cada
afirmación.

**Colisión con una forma heredada.** GeoGebra tiene `Intersect(<Object>, <Object>, <Initial
Point>)`. En `Intersect(L, r, D)`, con `L` Locus V2, `D` no es nunca un punto inicial; hoy el
producto rechaza esa forma (P10).

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
  copiar reconstruyen el punto desde el DAG, y los certificados se recalculan por revisión con la
  captura de esa misma revisión (huella exacta de `CU2`, D-CU2-20-R).
- **Dependencias:** cualquier cambio de `L` (revisión), de `D` o de `r` propaga por el grafo.
- **Permutación de salidas del origen.** Si la selección heredada intercambia las salidas del
  algoritmo de `D`, `D*` cambia con ella y `P` lo sigue, sin coordenadas antiguas ni reasociación.

## 5. Vigencia, desaparición, degeneraciones y recuperación

| Situación | `Point(L, D)` | `Intersect(L, r, D)` | Causa |
|---|---|---|---|
| `D` indefinido | indefinido | indefinido | `DRIVER_POINT_UNDEFINED` |
| procedencia no admitida o soporte distinto | indefinido | indefinido | `PROVENANCE_NOT_ESTABLISHED`, `DRIVER_SUPPORT_MISMATCH` |
| selección heredada no reconstruible | indefinido | indefinido | `SELECTION_NOT_RECONSTRUCTED` |
| conflicto de representación de una constante en el propio programa (§6.2, regla 3) | indefinido | indefinido | `CONFIGURATION_REPRESENTATION_CONFLICT` |
| `[t]` fuera del dominio, o indeciso en un extremo, en la costura o en un borde de componente | indefinido | indefinido | causas de §2.3 |
| `[t]` en una componente puntual | indefinido | indefinido | `POINT_COMPONENT_NOT_MATERIALIZED` |
| `L` con `Completeness = NOT_ESTABLISHED`, pero `[t]` dentro de una componente continua certificada | **definido** (ADR 0027) | según la incidencia | — |
| M3 fuera de clase o con el presupuesto agotado | definido | indefinido | `EXACT_EVALUATION_OUT_OF_CLASS`, `EXACT_EVALUATION_BUDGET_EXHAUSTED` |
| M3 decide `h ≠ 0` en `Σ_L⁺` | definido | indefinido | `INCIDENCE_REFUTED_IN_CAPTURED_CONFIGURATION`; si además se calculó `Σ_B` y allí la incidencia se demuestra, el diagnóstico añade `INCIDENCE_HOLDS_ONLY_IN_JOINT_RECOMPUTATION` |
| `r` indefinido, o fuera de su parte limitada | definido | indefinido | `TARGET_UNDEFINED`, `NOT_ON_LIMITED_TARGET` |
| la condición vuelve a cumplirse | definido de nuevo con la misma identidad | ídem | — |

## 6. Segunda incidencia: M3 en la configuración de decisión `Σ_L⁺`

### 6.1 Mecanismos

| Mecanismo | Contenido | Recomendación |
|---|---|---|
| **M1** identidad de términos | el término de `r` es igual al de un objeto del corte incidente con el punto trazado, con `E := D`; dos objetos de términos idénticos reciben **una sola** semántica exacta en `Σ_L⁺` | vía rápida cuando aplica |
| M2 reglas sintéticas | base de reglas siempre incompleta | no se recomienda |
| **M3** evaluación exacta en `Σ_L⁺` | §6.2 a §6.5 | **dirección arquitectónica candidata del núcleo**, sujeta a la puerta G-M3 |
| M4 prover simbólico | CAS externo | solo evidencia |
| M5 residuo numérico | `|h| ≤ ε` | nunca autoridad |

### 6.2 Construcción de `Σ_L⁺`

Cada objeto del subgrafo `G = corte(L) ∪ ancestros(D) ∪ ancestros(r)` recibe **una sola semántica
exacta**, según estas reglas, en este orden:

1. **Constantes de `Σ_L`.** Un objeto que el programa de clase v3 de `L` captura como constante
   conserva exactamente su valor capturado y su representación en ese programa: la recta por sus
   coeficientes y la circunferencia por su forma de matriz, con centro `(−A4, −A5)` y
   `r² = A4² + A5² − A2`, que es la que usa la clase v3 para la intersección
   (`ConstructionBoxModel2D`, D-CU2-T13-R7). Cuando una constante de `Σ_L` es también objetivo,
   como `c_1`, el objetivo es ese mismo objeto exacto. El adaptador numérico de residuo, que lee
   el semieje, es una evaluación numérica, nunca la semántica exacta.
2. **Soporte del conductor.** Su semántica exacta es la de las constantes del conductor de `Σ_L`:
   centro, semiejes y vectores propios de la circunferencia, o los extremos del segmento. El
   algoritmo de `D` se evalúa con **esa** semántica de `S`, de modo que `D*` está exactamente sobre
   el soporte que recorre el conductor.
3. **Conflicto de representación.** Si una constante tiene en el propio programa de `L` dos
   representaciones exactas distintas, el resultado es `CONFIGURATION_REPRESENTATION_CONFLICT`,
   con fallo cerrado.
4. **Ancestros compartidos.** Un objeto fuera de `Σ_L` que es ancestro de alguna constante de
   `Σ_L` (en el modelo, `g`, ancestro de `k`) se toma en su estado binary64, coherente con el
   cálculo redondeado que produjo esas constantes.
5. **Frontera.** Los elementos independientes, los parámetros de camino y los objetos cuyo
   cálculo exacto saldría de la clase (en el modelo, `H = Point[j]`, que depende de 47°) se toman
   en su estado binary64, como racionales exactos.
6. **Resto.** Todo otro objeto de `ancestros(D) ∪ ancestros(r)` se **recalcula exactamente** a
   partir de las semánticas de sus padres, con la semántica exacta de su algoritmo y la regla de
   selección exacta (§6.3). En el modelo: `K`, `J`, `m`, `l`, `M`, el punto `L` del modelo
   (`Intersect[l, h]`), `n`, `r`, `M_1` y `h_2`.
   Así las relaciones estructurales entre esos objetos sobreviven al redondeo: no se toman en su
   estado binary64, porque su redondeo rompería el teorema (análisis §5.7).

**Por qué no se recalculan las constantes de `Σ_L`.** Recalcularlas cambiaría el objeto `L`, y el
punto ya no sería el que `CU2` certifica. Esa es la diferencia con `Σ_B` (§6.6).

**Por qué no se toma en binary64 lo que está fuera de `Σ_L`.** Las igualdades se decidirían por
coincidencias de redondeo entre objetos redondeados independientemente. Por ejemplo, dos
circunferencias de radio `√2` construidas de dos maneras tienen el mismo radio binary64 y
«coincidirían» (V-CFG-3).

### 6.3 Reconstrucción exacta de `D` y selección heredada

Cada algoritmo de varias salidas del subgrafo (`K = Intersect[p, i]`, `H_1 = Intersect[d_1, i, 1]`,
`G_1 = Intersect[c_1, s_1, 1]`, `M_1 = Intersect[c_1, n]`) se evalúa con la **versión exacta de la
regla de §10.2** de la clase v3:

- las raíces exactas se ordenan por el parámetro a lo largo de la recta del algoritmo, con una
  comparación exacta;
- se aplica el estado de selección capturado (permutación, indicadores de inicialización y
  etiquetas) mediante los accesores de solo lectura de `CU2` (D-CU2-17);
- si dos raíces exactas coinciden, la selección no es reconstruible y la regla heredada de
  supresión de §10.3 no se reproduce: `SELECTION_NOT_RECONSTRUCTED`;
- las intersecciones en modo continuo, las selecciones por punto de referencia y una
  inicialización cuyo número de raíces no esté probado constante quedan fuera, con fallo cerrado.

Las coordenadas binary64 de `D` no entran en el cálculo exacto. Solo sirven en la comprobación de
correspondencia con el algoritmo vivo (§6.7, G-M3-3).

### 6.4 Decisión exacta

En `Σ_L⁺`, con el conductor sustituido por el **punto** exacto `D*`:

- los programas de regla y compás generan una torre finita de extensiones cuadráticas de los
  racionales de la frontera;
- se deciden con aritmética exacta y cotas constructivas de separación:
  - el signo y la nulidad del residuo de `r`;
  - los predicados de validez del corte;
  - el parámetro de un objetivo limitado;
  - la primera derivada del residuo, por aritmética exacta de chorros, cuando la regla de
    conflicto la necesita;
- el resultado es «igual», «distinto», o fuera de clase o con el presupuesto agotado; nunca
  «igual dentro de una tolerancia».

**Igualdades accidentales.** Una igualdad que no es estructural se demuestra **si y solo si es
exacta en `Σ_L⁺`**. Si solo vale en `Σ_ideal`, con valores no representables, M3 decide
exactamente la desigualdad.

### 6.5 El modelo del autor en `Σ_L⁺`

El teorema `J_1(K) = M_1` (análisis §5.3) solo necesita relaciones de incidencia, paralelismo y
perpendicularidad entre las constantes: `k` vertical y por `H`; `h` e `i` horizontales; `I = i ∩ k`;
`c_1` con centro en `H`; `p` con centro en `i`; `g ∥ k`. No necesita ningún valor métrico exacto:
vale para cualquier radio de `c_1` y de `p`.

El análisis de la aritmética heredada (análisis §5.8) muestra que esas relaciones se cumplen
**exactamente** en los estados binary64 que `Σ_L` captura. Las operaciones que las producen son
productos por 0 y por ±1 y copias de coordenadas, y la normalización de coeficientes solo escala por
potencias de dos.

Por tanto se espera que M3 demuestre en `Σ_L⁺` los cinco contactos obligatorios, para el punto
autoritativo de `L`. La puerta G-M3 lo confirma o lo refuta.

### 6.6 Relación con `Σ_B` y transferencia

`Σ_B` recalcula también las constantes de `Σ_L`: por ejemplo, el radio de `c_1` es el exacto
`|HD_1|` en `Σ_B` y el capturado `√(A4² + A5² − A2)` en `Σ_L`. Por eso:

- **Puntos distintos.** El punto exacto `P_B` de `Σ_B` y el punto autoritativo `P_L` son, en
  general, **puntos distintos**. Una cota certificada de `|P_L − P_B|` es información de precisión,
  nunca evidencia de incidencia.
- **Condición de transferencia.** Una demostración en `Σ_B` vale para `P_L` solo si `Σ_B` y `Σ_L⁺`
  asignan, demostrado exactamente, el mismo valor exacto a cada objeto que la demostración usa. En
  ese caso coinciden sobre ese subgrafo y `P_B = P_L`. El diseño no se apoya en la transferencia:
  decide directamente en `Σ_L⁺`.
- **Si las configuraciones difieren y `Σ_L⁺` no demuestra la incidencia**, `Intersect(L, r, D)`
  queda indefinido. Si `Σ_B` se ha calculado como diagnóstico y allí la incidencia se demuestra, el
  inspector muestra `INCIDENCE_HOLDS_ONLY_IN_JOINT_RECOMPUTATION`: la construcción es incidente en
  la recalculación profunda, pero las constantes redondeadas de `Σ_L` rompen la relación. Ninguna
  coincidencia numérica cambia el resultado.
- **El contrato de `CU2` se conserva.** Ninguna constante ni ningún certificado de `Σ_L` se
  modifica ni se reinterpreta como certificado de otra configuración. `Σ_L⁺` solo añade objetos.
  Hacer que la configuración capturada de `L` sea la recalculación profunda (la cuestión abierta 1
  de la preparación de `CU2`) sería una enmienda del contrato de `CU2`, con certificados nuevos, y
  queda fuera de `CU3` (alternativa C3).

### 6.7 Puerta temprana y bloqueante de M3 (G-M3)

Se ejecuta **antes** de implementar la segunda incidencia (estrategia, paso T2). Si no se supera,
esa parte se detiene y se presentan las alternativas.

| Criterio | Contenido | Se supera si |
|---|---|---|
| **G-M3-1** viabilidad matemática en `Σ_L⁺` | los cinco contactos obligatorios (`e_1·n`, `e_1·c_1` y `o_1·h_2` en `K`; `e_1·c_1` y `e_1·r` en `J`), con las reglas de §6.2 y las hipótesis aritméticas del análisis §5.8 comprobadas sobre los estados capturados | M3 decide **igual** en los cinco y en las dos ramas de `H_1`, y pasan los controles V-M3-3 a V-M3-6 y V-CFG-1 a V-CFG-3 |
| **G-M3-2** presupuesto computacional | contadores deterministas: operaciones exactas, grado de la torre, longitud máxima en bits; GC3-3 y GC3-4 | los cinco contactos bajo un techo versionado, fijado por el banco y aprobado por el autor |
| **G-M3-3** correspondencia con el algoritmo vivo | para cada algoritmo de varias salidas, la rama exacta elegida por la regla exacta de §10.2 con el estado capturado; su encierro contiene el valor vivo y es disjunto del de la otra rama; la fidelidad de reconstrucción de `CU2` (D-CU2-20-R) se cumple | todas las salidas de los cinco casos |

**Si M3 resulta inviable en `Σ_L⁺` para los casos obligatorios**, nunca se sustituye su evidencia
por un residuo ni por una demostración en `Σ_B`. Alternativas, que se presentan al autor con la
evidencia de la puerta:

| Alternativa | Contenido | Consecuencia |
|---|---|---|
| R-1 | `Point(L, D)` sin M3 e `Intersect(L, r, D)` indefinido, con la evidencia de clase 3 y, si se calcula, el diagnóstico de `Σ_B` | B08 y B09 no se cierran como intersecciones; el compromiso del plan §6 necesita una decisión autoral |
| R-2 | otro método exacto en `Σ_L⁺` (eliminación algebraica, o parametrización racional del conductor) | otra puerta equivalente |
| R-3 | aplazar la segunda incidencia de los casos obligatorios con una limitación documentada | decisión autoral expresa |
| C3 | enmendar el contrato de `CU2` para que la configuración capturada de `L` sea la recalculación profunda | cambia un contrato aprobado; necesita su propia autorización y certificados nuevos; fuera de `CU3` |
| — | aceptar un residuo, una proximidad, una demostración en `Σ_B` o el prover simbólico como autoridad | **rechazada** |

## 7. Los tres casos obligatorios

| Forma | Hechos 1–3 | `P ∈ L` (`Σ_L`) | `P ∈ r` (`Σ_L⁺`) | Resultado previsto |
|---|---|---|---|---|
| `Intersect(e_1, n, K)` | salida 2 de `AlgoIntersectLineConic(p, i)`; `p` es el soporte de `E_1`; `t*(K) = 0` exacto | `[t] = {0}` dentro de una componente certificada `COMPLETE` | teorema del análisis §5.3, con las relaciones exactas de §5.8; M3 | `M_1`, dinámico; orden `FOLD_CONTACT` |
| `Intersect(e_1, c_1, K)` | igual | igual | igual, con `c_1` en su forma de matriz capturada | `M_1`, dinámico |
| `Intersect(o_1, h_2, K)` | igual | igual (`o_1`) | `K_1 ∈ b_1 = h_2`; M3 | `N_1`, dinámico; tangencia regular |
| `Intersect(e_1, c_1, J)` e `Intersect(e_1, r, J)` (costura) | `J` es la salida 1; `t*(J) = π` encerrado | `[t]` alrededor de `π`, según la regla de la costura | el mismo teorema con la generatriz `r`; M3 | el punto de la costura, como contacto estructural en el pliegue |

**Dinámica.** Con `rbCone`, `α_cone`, `D_1` o `excCyl` cambiados, `K`, `t*(K) = 0` y los contactos
siguen la construcción con la misma identidad (P4), y M3 decide en cada revisión con la captura de
esa revisión. `Point(o_1, K)` sustituye al rodeo del autor `Point(o_1, text2, v_1)`.

**Desaparición.** Con `|HD_1| > |HM|`, `Intersect(e_1, n, K)` da `NOT_ON_LIMITED_TARGET` y
reaparece al volver.

## 8. Persistencia y compatibilidad

- Se serializa como `<command name="Point">` o `<command name="Intersect">` con los argumentos
  `L`, `D` (y `r`), más la identidad durable del punto. No hay ningún elemento XML nuevo.
- **Lectores anteriores.** El producto actual rechaza las dos formas (P10). Si la carga completa
  del documento se aborta, no se ha comprobado (V-A-COMPAT-2). Hace falta la nota de migración de
  `AGENTS.md` §11.6 (D-CU3-12).
- La Etapa A solo añade formas nuevas.

## 9. Diagnósticos tipados (candidatos)

`DRIVER_POINT_UNDEFINED`, `PROVENANCE_NOT_ESTABLISHED`, `DRIVER_SUPPORT_MISMATCH`,
`UNSUPPORTED_DRIVER_FAMILY`, `SELECTION_NOT_RECONSTRUCTED`, `CONFIGURATION_REPRESENTATION_CONFLICT`,
`CORRESPONDENCE_NOT_CERTIFIED`, `OUT_OF_DRIVER_DOMAIN`, `DRIVER_DOMAIN_UNDECIDED`,
`SEAM_SIDE_UNDECIDED`, `COMPONENT_BOUNDARY_UNDECIDED`, `POINT_COMPONENT_NOT_MATERIALIZED`,
`TARGET_UNDEFINED`, `INCIDENCE_NOT_ESTABLISHED`, `INCIDENCE_REFUTED_IN_CAPTURED_CONFIGURATION`,
`INCIDENCE_HOLDS_ONLY_IN_JOINT_RECOMPUTATION` (diagnóstico, nunca autoridad),
`NOT_ON_LIMITED_TARGET`, `EXACT_EVALUATION_BUDGET_EXHAUSTED` y `EXACT_EVALUATION_OUT_OF_CLASS`.

Cada uno es un fallo cerrado: ningún punto conserva coordenadas antiguas ni se reasocia.

## 10. Relación con otros paquetes

- `CU7` (M08, punto sobre Locus V2 por interacción) es otra familia: la Etapa A no crea puntos
  por arrastre ni por proximidad.
- Las componentes puntuales de `CU2` quedan fuera (§2.4). La materialización en ellas y C40
  necesitan su propia autorización (D-CU3-9).
