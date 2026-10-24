# PRE-G9B-CU3 — Informe de la puerta G-M3 (evaluación exacta M3)

| Campo | Valor |
|---|---|
| Carácter | Informe técnico de la puerta temprana y bloqueante G-M3, paso T2 de la estrategia. Es evidencia de un prototipo del arnés de bancos, no del producto. No aprueba nada ni sustituye la decisión autoral |
| Autoridad | [registro de aprobación](pre_g9b_cu3_author_design_approval_record.md) §4 y §9; [matriz](pre_g9b_cu3_validation_matrix.md) §2.1; [estrategia](../architecture/pre_g9b_cu3_implementation_strategy.md) §2; `locus-v2-certified-construction-model.md` §11.2 a §11.4 |
| Rama | `phase/pre-g9b-cu3-implementation`, local, creada desde `80d72a50557a82a7a9d501a26252d1758ac6d94d` |
| Pasos | T0 `00148af3d5795675349259f7a63727d7f94b4881`; T1 `4a1272c0ce28ca672fa4eaede178cc31c1b929fa`; T2, prototipo `245c66e35216f3b233b765bbdb816d0f65c3708b` y `efa372c8dea83ce0cc87daff1064c888f5eaa80d` |
| Prototipo | `PreG9BCu3M3Tower`, `PreG9BCu3M3Evaluator` y `PreG9BCu3M3GateProbe` en `tools/benchmark/pre-g9b-cu/cu3-java/`; lanzador `tools/benchmark/pre-g9b-cu/run-cu3-m3-gate.ps1`, con el `harness.init.gradle` de `CU0` sin cambios |
| Evidencia | [`pre-g9b-cu3-m3-gate-evidence.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-m3-gate-evidence.json), SHA-256 `179972ccdcaa45e6778b858710462040ee4460de740adb1256ce2be2c390ddae`: copia byte a byte de la salida de la ejecución A sobre `efa372c8` (árbol `59238114e7fd59afcbd022996739a9d9188497f3`), con el espacio de trabajo limpio y las fuentes del producto sin cambios |
| Resultado técnico | G-M3-1 y G-M3-3 se cumplen; G-M3-2 está medido, con un techo propuesto pendiente de decisión; **G-M3-4 no se cumple en ninguno de los cinco contactos obligatorios** |
| Estado | Condición de parada (registro §4 y §9; estrategia, condición 4; prompt canónico, «Stop conditions»). Las partes que dependen de M3 están detenidas; no existe código de producto de M3 ni de `Intersect(L, r, D)`. Decisión autoral única pendiente (§8) |
| `selfApproved` | `false` |

## 1. Qué decide la puerta y cómo

G-M3 decide, antes de cualquier código de producto de la segunda incidencia, si la evaluación
exacta M3 demuestra en la configuración de decisión `Σ_L⁺` los cinco contactos obligatorios contra
la autoridad de sus objetivos: `e_1·n`, `e_1·c_1` y `o_1·h_2` en `K`, y `e_1·c_1` y `e_1·r` en `J`.

**Configuraciones.**

- `Σ_L` es la configuración capturada de `L` en `CU2`, y es la autoridad del punto.
- `Σ_L⁺` la contiene sin cambios y añade `D`, `r` y sus ancestros.
- `Σ_B`, la recalculación profunda desde los objetos libres, solo es diagnóstico.
- La autoridad del objetivo es la del contrato de intersecciones §2.1
  (`locus-v2-certified-construction-model.md` §11.3): los coeficientes binary64 de una recta, con
  la parte limitada de un segmento o de un rayo, y el centro y el semieje del adaptador de una
  circunferencia.

**Método del prototipo.**

- **Aritmética.** Es exacta, en una torre finita de extensiones cuadráticas de los racionales, y
  cada valor binary64 de la frontera es el racional que representa. Los signos son exactos, por
  normas conjugadas y sin prueba de irreducibilidad.
- **Evaluación.** El programa de clase v3 capturado se evalúa con el conductor sustituido por el
  punto exacto `D*`, con las fórmulas y los predicados de validez de `ConstructionBoxModel2D`. Da
  chorros exactos de orden dos en `t*` (valor y dos derivadas), a partir del coseno y del seno
  exactos de `D*`.
- **Selección.** `D*` y cada salida de un algoritmo de varias salidas se reconstruyen con la forma
  exacta de la regla de selección heredada (§10.2) y el estado de selección capturado.
- **Residuos.** Se usan sin normalizar: `a·x + b·y + c` para una recta, con sus coeficientes, y la
  potencia `|P − c|² − r²` para una circunferencia. Su signo es el de la distancia con signo.
- **Certificado de conteo de la regla de conflicto** (CU3.4), en la caja `t* ± 10⁻³`. Combina:
  - `h(t*)` y `h′(t*)` exactos;
  - un encierro de `h″` en la caja con redondeo hacia fuera, compuesto con la envolvente de
    clase v3 de `CU2` en la misma configuración;
  - la cota de Taylor;
  - la condición de componente: la caja está en una componente continua certificada.
- **Contadores.** Son deterministas: operaciones exactas (sumas, productos, inversas, decisiones de
  signo y raíces), profundidad de la torre y longitud máxima en bits. La salida no contiene
  tiempos.

El prototipo no es código de producto ni un perfil de verificación. Trabaja sobre copias del
modelo del autor y sobre construcciones sintéticas, y no asigna veredictos.

## 2. Resultado por criterio

| Criterio | Resultado técnico | Evidencia |
|---|---|---|
| Hipótesis del análisis §5.8, con `z = 1` | se cumplen todas, exactamente | `model.hypotheses_5_8`: `z = 1` en `A`, `B`, `H` e `I`; `g` y `k` verticales; `h` e `i` horizontales; incidencias exactas de `A`, `B`, `H` e `I`; `I = (H.x, B.y)`; centro de `c_1` igual a `H`, por la matriz y por el adaptador; `p` con centro `B`, ejes `(1, 0)` y `(0, 1)` y semiejes iguales a `rbCone` |
| G-M3-1, viabilidad matemática | se cumple. M3 decide exactamente, nunca fuera de clase, en los cinco contactos y en las dos ramas de `H_1`, con torres de profundidad 1 o 2. Con los objetivos reconstruidos (V-M3-1 y V-M3-2, diagnóstico) decide igual en los cinco. Los controles dan el resultado esperado (§4) | `model.mandatory_contacts`, `model.other_branch_of_H_1`, `controls` |
| G-M3-2, presupuesto | medido; el techo propuesto `cu3-exact-evaluation-budget/v1` está pendiente de la decisión (§6) | `counters` de cada caso |
| G-M3-3, correspondencia con el algoritmo vivo | se cumple. Cada rama exacta elegida reproduce la salida viva a menos de `3,1·10⁻¹⁵`, y la otra rama está a más de 5 unidades (§5) | `model.live_correspondence` |
| Coherencia de configuraciones (V-CFG-1 a V-CFG-6) | se cumple: ninguna decisión se transfiere desde `Σ_B` ni desde una reconstrucción (§4) | `configurations` |
| **G-M3-4, autoridad del objetivo** | **no se cumple**. Los cinco contactos se refutan exactamente contra la autoridad de su objetivo. La incidencia solo se cumple con el objetivo reconstruido o en `Σ_B`, que son diagnósticos (§3) | `authority`, `reconstruction_of_the_target` y `joint_recomputation_sigma_B` de cada contacto |

## 3. Los cinco contactos obligatorios

| Contacto | `t*` y naturaleza | Autoridad del objetivo | Decisión contra la autoridad | Distancia con signo (aprox.) | Reconstruido / `Σ_B` | Conteo en `t* ± 10⁻³` |
|---|---|---|---|---|---|---|
| `e_1·n` en `K` | `0`; pliegue (`F′(0) = 0` exacto) | segmento `n`: coeficientes binary64; `P_L` en su parte limitada (parámetro ≈ 0,702) | **distinto**; residuo `−6,22·10⁻¹⁵` | `−7,30·10⁻¹⁶` | igual / se cumple | `N = 0` |
| `e_1·c_1` en `K` | `0`; pliegue | `c_1`: centro y semieje del adaptador | **distinto**; potencia `−1,19·10⁻¹⁴` | `−9,94·10⁻¹⁶` | igual / se cumple | `N = 0` |
| `o_1·h_2` en `K` | `0`; tangencia regular (`F′(0) ≠ 0`) | recta `h_2`: `(−1; 0; 4,827722208159745)` | **distinto**; residuo `+1,04·10⁻¹⁵` | `+1,04·10⁻¹⁵` | igual / se cumple | `N = 0` |
| `e_1·c_1` en `J` (costura) | `π` (coseno `−1` y seno `0` exactos); pliegue | `c_1`, como arriba | **distinto**; potencia `−1,19·10⁻¹⁴` | `−9,94·10⁻¹⁶` | igual / se cumple | `N = 0` |
| `e_1·r` en `J` (costura) | `π`; pliegue | segmento `r`: coeficientes binary64; `P_L` en su parte limitada (≈ 0,400) | **distinto**; residuo `−1,58·10⁻¹⁴` | `−1,05·10⁻¹⁵` | igual / se cumple | `N = 2` simples, a ambos lados de `π` |

- En los cinco casos `h′(t*) = 0` exactamente: `t*` es el punto crítico del residuo.
- Normalizada, `h″(t*)` coincide con la del análisis §5.4: `−3,348`, `−2,061`, `+2,569`, `−0,668`
  y `+1,085`.
- El encierro de `h″` en la caja excluye el cero en los cinco casos. Con `h(t*)` y `h′(t*)`
  exactos, el conteo es riguroso en el sentido de CU3.4.
- La otra rama de `H_1` (nodo 2 invertido) da el mismo `P_L`, en `M_1`. Es distinto contra la
  autoridad de `n` y de `c_1`, e igual con los objetivos reconstruidos.
- Las distancias son del orden de un ulp de las coordenadas: `ulp(4,83) = 8,9·10⁻¹⁶`.

## 4. Controles y casos de configuración

| Caso | Resultado | Lectura |
|---|---|---|
| V-M3-5, igualdad accidental exacta: radio 1,5 frente a `y = 1,5` | igual, con `h′ = 0` exacto | clase 2: la igualdad es exacta en `Σ_L⁺` |
| V-M3-4, objetivo desplazado un ulp: `y = nextUp(1,5)` | distinto (`−2,22·10⁻¹⁶`) | un ulp basta para refutar |
| V-M3-6, `0,1 + 0,2` frente a `x = 0,3` | distinto (`−2,78·10⁻¹⁷`) | la igualdad solo vale entre valores no representables: se refuta |
| V-M3-8, fuera de clase y presupuesto | el punto de camino en el parámetro 0,7 da `EXACT_EVALUATION_OUT_OF_CLASS`; la inyección de un presupuesto de 8 operaciones da `EXACT_EVALUATION_BUDGET_EXHAUSTED` (9 > 8) | nunca una decisión |
| V-CFG-1, objetivo `Circle(O, (1, −1))` | distinto (`+1,71·10⁻¹⁶`); en `Σ_B` se cumple | `INCIDENCE_REFUTED_IN_CAPTURED_CONFIGURATION`, con el diagnóstico `INCIDENCE_HOLDS_ONLY_IN_JOINT_RECOMPUTATION` |
| V-CFG-2, el objetivo es la constante `c` | distinto; la matriz y el semieje difieren en `+1,71·10⁻¹⁶`, y con la matriz sería igual | `TARGET_AUTHORITY_MISMATCH`: la matriz nunca es la autoridad |
| V-CFG-3, copia de término idéntico | distinto, como V-CFG-2 | ninguna semántica compartida sin igualdad demostrada |
| V-CFG-4, objetivo con el mismo radio binary64 | distinto | una coincidencia de redondeo no demuestra nada |
| V-CFG-5, modelo del autor | §3 | — |
| V-CFG-6, soporte del conductor usado como operando de circunferencia | `CONFIGURATION_REPRESENTATION_CONFLICT` (matriz menos conductor: `4,02·10⁻¹⁶`) | el sondeo conoce la procedencia por construcción, porque el programa de clase v3 no la registra (§9) |

En V-CFG-1 a V-CFG-4 el conteo es indecidible (el encierro de `h″` contiene el cero): `L` y el
objetivo son circunferencias concéntricas, y el residuo es constante a lo largo de `L`.

## 5. Correspondencia con el algoritmo vivo (G-M3-3)

| Conductor | `D*` frente al vivo | `H_1`: rama elegida / otra | `G_1`: rama elegida / otra | `P_L` frente al punto trazado vivo |
|---|---|---|---|---|
| `K` (`t = 0`) | `4,4·10⁻¹⁶` (la otra salida, a 10) | `4,4·10⁻¹⁶` / `5,15` | `1,1·10⁻¹⁵` / `11,97` | `e_1`: `1,1·10⁻¹⁵`; `o_1`: `1,4·10⁻¹⁵` |
| `J` (`t = π`) | `4,4·10⁻¹⁶` (la otra, a 10) | `1,3·10⁻¹⁵` / `25,15` | `3,0·10⁻¹⁵` / `11,97` | `e_1`: `6,1·10⁻¹⁵`; `o_1`: `5,5·10⁻¹⁵` |

El estado de selección capturado es la permutación `1,0` con las dos salidas etiquetadas. La regla
exacta lo reproduce sin ambigüedad: ninguna raíz exacta coincide con otra
(`SELECTION_NOT_RECONSTRUCTED` no aparece).

## 6. Coste y presupuesto propuesto (G-M3-2)

Cada fila cuenta toda la evaluación del caso:

- `D*`;
- el programa;
- los chorros;
- la decisión autoritativa;
- la reconstrucción del objetivo;
- el certificado de conteo.

| Caso | Operaciones | Profundidad | Bits | `Σ_B`, diagnóstico (operaciones / profundidad / bits) |
|---|---|---|---|---|
| `e_1·n` en `K` | 2 493 | 1 | 316 | 519 / 1 / 214 |
| `e_1·c_1` en `K` | 2 323 | 1 | 325 | 492 / 1 / 217 |
| `o_1·h_2` en `K` | 3 694 | 2 | 269 | 1 221 / 2 / 217 |
| `e_1·c_1` en `J` | 2 323 | 1 | 330 | 492 / 1 / 220 |
| `e_1·r` en `J` | 2 497 | 1 | 322 | 519 / 1 / 215 |
| controles V-M3-4 a V-M3-6 | 254 a 265 | 0 | 103 | — |
| V-CFG-1 a V-CFG-4 | 1 105 | 1 | 106 | V-CFG-1: 231 / 1 / 103; V-CFG-2: 224 / 1 / 103 |

**Propuesta `cu3-exact-evaluation-budget/v1`.** Para cada evaluación exacta de un punto
estructural frente a un objetivo, en una revisión: 100 000 operaciones, profundidad 8 y 4 096
bits. Si se supera, `EXACT_EVALUATION_BUDGET_EXHAUSTED`, con fallo cerrado; V-M3-8 ejercita el
mecanismo. Justificación:

- da márgenes de 27, 4 y 12 veces sobre los máximos medidos (3 694 operaciones, profundidad 2 y
  330 bits), para las revisiones de los barridos V-DYN-1 y para programas de clase v3 más largos;
- el coste del signo por normas conjugadas crece exponencialmente con la profundidad de la torre,
  y por eso el límite de profundidad es la cota esencial;
- GC3-3 y GC3-4 (ninguna evaluación exacta al arrastrar sin cambio de revisión, y una por revisión
  y punto estructural) son obligaciones de una implementación de producto, y el prototipo no puede
  medirlas.

La propuesta solo necesita aprobación si la decisión de §8 lleva M3 al producto. Este informe no
declara aprobada ninguna cota.

## 7. Análisis científico

### 7.1 Por qué se refutan las cinco incidencias

El teorema `J_1(K) = M_1` (análisis §5.3) relaciona objetos ideales. En `Σ_L⁺`, el punto exacto
`P_L` lo cumple con los objetivos reconstruidos desde su subgrafo: igual en los cinco contactos y
en las dos ramas, como diagnóstico. Pero la autoridad de cada objetivo es el estado binary64 que
los algoritmos heredados calculan a partir de entradas redondeadas:

- **`n` y `r`.** Sus coeficientes binary64 salen de puntos y productos redondeados (por ejemplo,
  `K.x = fl(B.x + 5)`, análisis §5.7), y no son proporcionales a los de la recta exacta por sus
  puntos de definición. Tras escalar, la diferencia es `9,6·10⁻¹⁵` y `2,0·10⁻¹⁴`.
- **`h_2`.** Es la recta por `fl(M_1)`, no por el `M_1` exacto.
- **`c_1`.** Su radio tiene dos representaciones:
  - la matriz capturada, `A4² + A5² − A2`, sobre la que `P_L` está exactamente;
  - el semieje del adaptador, que es la autoridad.

  Difieren en `−1,19·10⁻¹⁴` en `r²`, y esa diferencia es exactamente el residuo autoritativo.

**Conclusión general.** Un punto construido es exactamente incidente con un objetivo calculado con
aritmética redondeada solo cuando el redondeo conserva la relación por casualidad, como en V-M3-5
(valores representables y operaciones exactas). Con objetivos derivados por aritmética no trivial,
la incidencia exacta es genéricamente falsa. Contra la autoridad binary64, la clase
`CONTACT_EXISTENCE_EXACT` es la excepción, no la regla. No es un fallo numérico de M3: M3 decide
exactamente, y lo que decide es que los objetos representados no se tocan.

### 7.2 Qué es verdad en la configuración autoritativa

Los conteos locales contra la autoridad son rigurosos:

- **`N = 0` en cuatro contactos.** Cerca de `t*`, el locus no alcanza el objetivo representado:
  - `e_1` queda dentro de `c_1` y a un lado de `n`;
  - `o_1` no llega a `h_2`.
- **`N = 2` en `e_1·r` en `J`.** Hay dos raíces simples, una a cada lado de `π`, cerca de
  `π ± 4,4·10⁻⁸` (estimación de Taylor). `e_1` tiene en `π` un pliegue certificado por simetría
  exacta en `CU2` (análisis §5.4b). Por eso las dos raíces son dos preimágenes de un mismo punto
  geométrico: la punta del pliegue sobrepasa el segmento representado `r` en unos `10⁻¹⁵`, y el
  trazado lo cruza en un solo punto.

En términos geométricos, los cinco contactos existen en la construcción (en `Σ_B` y con los
objetivos reconstruidos). En su representación binary64 se convierten en un hueco del orden de un
ulp o en un cruce del orden de un ulp.

### 7.3 Consecuencias con los contratos aprobados

1. **Casos obligatorios.** V-MAND-1 a V-MAND-4 no pueden cumplirse. `Intersect(L, r, D)` quedaría
   indefinido en los cinco casos con `INCIDENCE_REFUTED_IN_CAPTURED_CONFIGURATION`, con
   `TARGET_AUTHORITY_MISMATCH` y, si se calcula, con `INCIDENCE_HOLDS_ONLY_IN_JOINT_RECOMPUTATION`.
   No se pueden cumplir la aceptación del plan §4 ni el compromiso de cierre del §6 («`CU3`:
   Etapa A y los tres casos obligatorios»). La decisión DA-3 añade que B08 y B09 no se cierran solo
   con una construcción alternativa.
2. **Costura, con M3 en el producto.** La premisa de D-CU3-10 era `N = 1` doble en `π` con M3. Queda
   refutada: contra la autoridad, en la costura el conteo es `N = 0` con `c_1` y `N = 2` con `r`.
   La regla de conflicto aprobada (CU3.4) retiraría entonces las soluciones publicadas en esas
   cajas, que son todos los puntos de `preserve.contacts` de `CU0`:
   - la raíz de la costura de `Intersect(e_1, c_1)`, transversal y admisible en la base;
   - las soluciones indeterminadas en `M_1` de `Intersect(e_1, n)` y de `Intersect(e_1, c_1)`;
   - la tangente en `N_1` de `Intersect(o_1, h_2)`.

   Eso contradice D-CU3-10 («el punto `preserve` no cambia») y la regla `preserve` de `CU0`
   (decisión C1-4), y exige cambiar contratos aprobados.
3. **Costura, sin M3 en el producto.** En el modelo no cambia nada (V-CONF-5: «con M3 desactivada o
   la puerta fallida, no cambia nada»). Los encierros por intervalos de `h(t*)` medidos en la
   preparación tienen anchuras de `1,3·10⁻¹³` a `5,3·10⁻¹³` (P2 y P5): contienen el cero, y los
   residuos autoritativos son mucho menores que esas anchuras, así que son clase 3. La raíz de la
   costura conserva su estado de G8B-R1, y H-CU3-2 en la costura queda como limitación.
4. **`CU3-B`.** I-7 pierde su objeto en el modelo: en la configuración autoritativa ninguno de
   estos contactos es de clase 1 o 2.
5. **Partes independientes de M3:**
   - I-1;
   - I-2: H02, grados y regla de conflicto con certificados de intervalos, que en los casos
     sintéticos deciden (anchuras de `1,8·10⁻¹⁵` a `2,7·10⁻¹⁵`, preparación P6);
   - I-3, sin la clase 2;
   - `Point(L, D)` de I-5: los hechos 1 a 3 y `P ∈ L` no necesitan M3, porque la correspondencia
     de una salida de intersección es encerrada (D-CU3-3; Etapa A §7);
   - I-6 (H06);
   - T9.

### 7.4 Por qué reconstruir el objetivo no resuelve el problema dentro del contrato

- Es lo que G-M3-4 prohíbe sin una correspondencia exacta demostrada, y aquí no existe: las
  diferencias son exactas y no nulas.
- **La semántica reconstruida dependería del conductor y de la captura de `L`.** La frontera de la
  reconstrucción la fijan los dos: se recalculan los descendientes de `D`, y las constantes
  capturadas conservan su representación (`c_1` es su matriz en los programas de `e_1` y de
  `o_1`). Con otro conductor u otra captura cambia la frontera, y el mismo objetivo recibe otra
  semántica exacta. No sería una autoridad del objetivo.
- **Una autoridad constructiva intrínseca** (el objetivo recalculado desde los objetos libres) solo
  es coherente con el punto de `L` en la misma configuración profunda: `P_L` usa la matriz
  capturada de `c_1`, y la recalculación profunda usa `|HD_1|` (Etapa A §6.6). Eso equivale a C3
  junto con una autoridad del objetivo nueva. `Σ_B` se cumple en los cinco contactos, pero se
  cambian dos contratos aprobados, y C3 está excluida de esta autorización.

## 8. Decisión autoral única

1. **Resultado de G-M3.** Confirmar que la puerta no se supera en G-M3-4 y que las partes
   dependientes siguen detenidas.
2. **Vía para los casos obligatorios.** Alternativas de la Etapa A §6.7, completadas con el
   análisis de §7.4:

| Alternativa | Contenido | Consecuencias |
|---|---|---|
| **R-1 (recomendada)** | `CU3-A` sin M3 de producto: `Point(L, D)` con identidad y dinámica para `M_1`, `N_1` y el punto de la costura; `Intersect(L, r, D)` indefinido con la evidencia de clase 3 y, si se calcula, el diagnóstico de `Σ_B`; regla de conflicto solo con certificados de intervalos; H02, H06, H-CU3-1, H-CU3-3 y H-CU3-4 donde los intervalos deciden | ningún contrato aprobado cambia; `preserve` se conserva (V-CONF-5). B08 y B09 siguen abiertos como intersecciones, y el compromiso del plan §6 debe reformularse por decisión autoral. H-CU3-2 (costura y extremo incluido) queda como limitación con esta evidencia; `pins.B08_B09` no se invierte; `CU3-B` se aplaza |
| R-1 con M3 de producto | lo anterior, más M3 como decisión exacta: (a) solo en `Intersect(L, r, D)`; (b) también en los certificados de conteo | (a) da la refutación tipada y la clase 2 en las igualdades exactas, sin efecto en los resultados existentes. (b) retira además todos los puntos de `preserve.contacts`: exige aprobar el cambio de esa referencia de `CU0` y de D-CU3-10. Las dos necesitan el presupuesto de §6 y registrar la procedencia de las constantes (V-CFG-6) |
| R-2 | otro método exacto en `Σ_L⁺` | sin utilidad: la refutación es exacta, y cualquier método exacto con la misma autoridad da el mismo resultado |
| R-3 | aplazar la segunda incidencia con una limitación documentada | más débil que R-1: aplaza también el comando y su diagnóstico |
| Objetivo reconstruido como autoridad, sin C3 | la semántica exacta del objetivo es la de su reconstrucción en `Σ_L⁺` | los cinco contactos se demostrarían, pero la semántica del objetivo dependería del conductor y de la captura (§7.4). Cambia el contrato de intersecciones §2.1 y contradice la precisión G-M3-4. No se recomienda |
| C3 | la configuración capturada de `L` pasa a ser `Σ_B` | excluida por la autorización. Además es insuficiente sola: la autoridad del objetivo seguiría siendo su estado binary64, y lo que `Σ_B` demuestra es la incidencia con los objetivos también recalculados |
| C3 con una autoridad constructiva del objetivo | todo se decide en la recalculación profunda | cerraría B08 y B09 como intersecciones (`Σ_B` se cumple en los cinco), pero cambia dos contratos aprobados, el convenio de captura de `CU2` y el contrato de intersecciones §2.1: es un estudio normativo propio, fuera de `CU3` |

3. **Presupuesto.** Aprobar `cu3-exact-evaluation-budget/v1` solo si se elige una opción con M3 de
   producto.
4. **Costura y `preserve`.** Con R-1, D-CU3-10 conserva «el punto `preserve` no cambia», pero su
   criterio reformulado no puede cumplirse. El punto de la costura sigue siendo la raíz transversal,
   con la evidencia de clase 3, y su forma estructural es `Point(e_1, J)`.

**Recomendación: R-1.** `CU3-A` sin M3 de producto y `CU3-B` aplazado. El presupuesto queda
registrado sin adoptarse, y el resultado científico pasa al registro E1 para `CU10`. Razones:

- conserva todos los contratos aprobados, como pide la instrucción: no se reabren `CU0`–`CU2`, C3
  queda excluida y la autoridad del objetivo no se redefine;
- frente a R-3, da lo que el modelo necesita de la Etapa A: los puntos dinámicos con identidad,
  sin el selector manual, y un comando de intersección honesto;
- frente a la variante (a), no lleva M3 al producto cuando su único efecto en `CU3` sería un
  diagnóstico más preciso (refutación exacta en lugar de clase 3) y la clase 2 de las igualdades
  accidentales exactas;
- no convierte ningún fallo de certificación en una aproximación;
- conserva la evidencia para una futura decisión sobre una autoridad constructiva.

**Qué se reanudaría con R-1.** Los pasos T3 a T10, salvo T6 (M3 de producto), con la matriz
ajustada por la decisión:

- I-1, I-2, I-3 sin la clase 2, I-5 con `Intersect(L, r, D)` indefinido, I-6 y T9;
- G-A con estas filas reformuladas:
  - V-MAND-1 a V-MAND-4: el punto de `Point(L, D)`, e `Intersect` indefinido con su evidencia;
  - V-DYN-1, sin decisiones de M3;
  - V-B-2, V-B-11, V-B-NOISE-1 y V-CONF-5, en su rama sin M3;
  - V-M3-1 a V-M3-9 y V-CFG-1 a V-CFG-6, como evidencia de la puerta y no como filas de producto;
  - §3.9, sin invertir la fila de la costura de `pins.B08_B09`.

## 9. Lo que este informe no establece

- **Fidelidad del prototipo.** Su fidelidad a `ConstructionBoxModel2D` la comprueban la
  correspondencia con el algoritmo vivo (G-M3-3) y los controles, no una demostración formal. Una
  implementación de producto necesitaría sus propias pruebas.
- **Alcance de los conteos.** Son locales, en la caja `t* ± 10⁻³`, no globales.
- **V-CFG-6.** Se decide porque el sondeo conoce por construcción la procedencia de la constante.
  El programa de clase v3 no la registra, y un producto la necesitaría.
- **Lo que no se mide.** GC3-3 y GC3-4 no se miden, y los tiempos no se registran.
- **Estado.** Nada de lo anterior es un `PASS` ni una aprobación autoral.

## 10. Reproducción

```text
pwsh -NoProfile -File tools/benchmark/pre-g9b-cu/run-cu3-m3-gate.ps1 -OutputDirectory <directorio nuevo>
```

Hubo dos ejecuciones sobre `efa372c8`, en `artifacts/benchmarks/pre-g9b-cu/cu3-m3-gate-efa372c8-runA`
y `…-runB`, que el repositorio ignora:

- códigos de salida 0 en el sondeo y en la limpieza;
- espacio de trabajo limpio;
- el mismo SHA-256 de `cu3-m3-gate.json`;
- 12,7 a 17,7 s por ejecución, a título informativo.

El lanzador recompila las pruebas de escritorio sin el sondeo y retira del árbol de compilación
sus clases y su informe.
