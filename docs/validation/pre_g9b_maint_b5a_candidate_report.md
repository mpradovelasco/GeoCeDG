# PRE-G9B-MAINT-B5a — Informe del candidato técnico

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = INTEGRATED_PHASE
PRODUCT_PHASE_EFFECT      = PRE-G9B-MAINT-B5A
GUIDE_IMPACT              = UPDATE
BOOTSTRAP_IMPACT          = NO CHANGE
```

| Campo | Valor |
|---|---|
| Incremento | `PRE-G9B-MAINT-B5a`: comprobación de escalado de cuerdas `conditioned-route-jump-guard/v1`, condición 8 de la admisión `conditioned-length-scalar/v2` |
| Autorización | Instrucción autoral «GeoCeDG — PRE-G9B-MAINT. Segunda ola coordinada: MAINT-B5a + MAINT-A-DXF0 + MAINT-A2», 2026-11-24, §0 y §7 |
| Base | `228580e9684d2c54650f8d461c2a4d1142a7ef55`, árbol `a4c892aa91df1a0ac3e5c16a62bc8b87cd3064ae` |
| Prompt | [`pre-g9b-maint-b5a-conditioned-route-jump-guard`](../../.github/prompts/tasks/pre-g9b-maint-b5a-conditioned-route-jump-guard.prompt.md), commit de gobernanza `de7c70741d7195f2ccdb04340ece8ef8ef9f6b6f` |
| Norma | [`locus-v2-metrics.md`](../../geocedg/specs/locus/locus-v2-metrics.md) §28.3.1, adoptada ([registro de aprobación](pre_g9b_maint_n1_author_approval_record.md)); condiciones de entrada en el [registro de la enmienda](pre_g9b_maint_n1_metrics_amendment_record.md) §6 |
| Planificación | [plan C](../architecture/pre_g9b_maint_p0_limitations_plan.md) §1.4–§1.6 (B5) y §5 (B4) |
| `selfApproved` | `false` |

Este informe forma parte del commit técnico que describe. Los resultados de `STATIC`,
`PHASE PRE-G9B-MAINT-B5A` e `INTEGRATION` sobre ese commit se obtienen después y se entregan
aparte. La aprobación autoral, si llega, se registra en otro documento.

## 1. Resultado

- La sonda P-1 ya no da número: la recta (`2.459674775249769`) y el arco
  (`0.35128942567868376`) quedan como resultado rico con `CONDITIONED_ROUTE_NOT_ADMISSIBLE` y
  `CHORD_SCALING_JUMP_SIGNATURE`; la estimación se conserva sin cambios. Los tramos antes y
  después del salto siguen siendo número.
- Las 62 rutas condicionadas admitidas hoy que mide la línea de base (26 de las baterías
  compartidas post-R1, de P-1 y de referencias sintéticas; 36 de los modelos del autor `w_2`,
  `a_3` y `trHole`) siguen admitidas salvo las dos de P-1, con el mismo valor bit a bit.
  Ninguna ruta admitida hoy queda inadmisible sin firma ni por agotamiento del presupuesto.
- Valores finales de la política: `ρ_J = 0,90`, `m = 3`, `L = 6`,
  `ε_res(λ) = max(τ_λ, 2^8·u·M_λ)`, `E_cap = 16 384`, todos dentro de los intervalos de
  §28.3.1 (§3).
- El extremo de pliegue conserva su causa (`WORK_BUDGET_EXHAUSTED`) y la comprobación no se
  ejecuta. Las pruebas fijadas `aRouteAcrossAJumpThatTheTraversalDoesNotSeeIsNoNumber` y
  `aJumpAlongTheChordLeavesNoTraceAndIsNotDetected`, y las clases `PostR1ConditionedMetricTest`,
  `PostR1ConditionedEvidenceTest` y `PostR1LengthDeletionDesktopTest`, pasan sin cambios.

## 2. Diseño

### 2.1 Punto de enganche

`LocusMetricEngine2D.computeBetween` conserva los estados de las estimaciones condicionadas de
los tramos de la ruta y los pasa a `conditionedBetween`. Tras las condiciones 1 a 4 (región,
celdas, causas excluidas) se construye la ruta admitida; si el resultado no es admisible por las
condiciones 5 a 7 (estado, finitud, error, actualidad) se devuelve como en v1 y la comprobación
no se ejecuta. Si lo es, `LocusConditionedRouteJumpGuard2D` recorre las hojas aceptadas de cada
tramo en orden creciente de parámetro: admite con `asConditionedRoute(ruta.withJumpGuard(...))`
o publica la misma estimación con `asConditioned(diagnósticos)`.

| Pieza | Cambio |
|---|---|
| `LocusConditionedRouteJumpGuard2D` (nueva) | política, procedimiento de §28.3.1, registro de ruta admitida y diagnósticos |
| `LocusMetricEstimator2D` | accesor aditivo: con `retainLeafNodes()`, cada hoja aceptada conserva los nodos que el estimador ya evaluó (los cinco de cuartos, o los dos extremos de una celda más estrecha que la plantilla), su cuota `τ_λ` y `M_λ`. No cambia evaluaciones, decisiones, valor, error ni presupuesto |
| `PolicyV2LocusMetricCapability2D` | `buildConditionedEstimateState(..., retainLeafNodes)` y `checkConditionedRoute`, que evalúa el punto trazado de la fuente medida con la misma rama y extensión que el estimador |
| `LocusMetricEngine2D` | retención solo para los tramos de ruta (`conditionedRegionState`); los totales y las piezas declaradas no cambian |
| `LocusMetricConditionedRoute2D` | `VERSION = conditioned-length-scalar/v2`; `getJumpGuard()` con el registro |
| `MetricDiagnosticCode2D` | `CHORD_SCALING_JUMP_SIGNATURE`, `CHORD_SCALING_INCONCLUSIVE`, `CHORD_SCALING_BUDGET_EXHAUSTED` |

`LocusConditionedTraversal2D`, su política, la identidad de contenido de la evidencia, los
tokens, la evidencia de existencia, los certificados, la serialización, §26.5, §26.6, §28.1,
§28.2 e `Intersect` no se tocan.

### 2.2 Lecturas de la norma

Ninguna exige una decisión normativa nueva; se señalan para revisión.

| Id | Lectura |
|---|---|
| L-1 | Una celda más estrecha que la plantilla (§25.12, punto 3) solo tiene evaluada la cuerda de la celda entera; esa cuerda es `I_0`. Es el caso que cubre el límite declarado 5 |
| L-2 | `M_λ` incluye el punto de sonda del estimador, que es un nodo evaluado de la hoja |
| L-3 | `τ_λ` es la cuota con la que el estimador aceptó la hoja (incluido su margen `1 − 2^-40`) |
| L-4 | En una imagen por semejanza, cuerdas, cuotas y `M_λ` son los de la fuente medida, como las hojas del estimador; los cocientes no dependen del factor |
| L-5 | Una hoja de la ruta diferencial no tiene cuerdas evaluadas por el estimador: termina inconclusa. Hoy ningún productor condicionado es diferencial (todas las rutas medidas son `POINT_EVALUATOR_ONLY`) |
| L-6 | Una evaluación fallida de la comprobación da `CONDITIONED_ROUTE_NOT_ADMISSIBLE` + `EVALUATION_FAILED`; el resultado rico conserva la estimación, cuyo estado sigue siendo `SUCCESS` |
| L-7 | Una ruta que cruza la costura de una región periódica tiene dos tramos; se comprueban en orden creciente de parámetro, con un único presupuesto |
| L-8 | Las evaluaciones de la comprobación no se suman a los contadores de instrumentación del estimador; constan en el registro y en los diagnósticos. No se memorizan: cada cálculo de la ruta repite la comprobación |
| L-9 | El presupuesto se agota cuando hace falta una evaluación y ya se gastaron `E_cap`: esa hoja queda sin decidir y las siguientes sin probar |

### 2.3 Caché de estados y orden de los consumidores

Revisión pedida por el coordinador sobre el candidato `7c11b4845`. Los estados de las
estimaciones condicionadas los posee y guarda en caché el propietario métrico compartido de la
fuente, por `LocusMetricIndexKey2D`, cuya igualdad incluye la clave de componente. Un estado
construido sin retener nodos no puede servirse a una ruta, porque los espacios de claves son
disjuntos:

| Consumidor | Clave de componente | Retiene nodos |
|---|---|---|
| Ruta entre dos posiciones (`conditionedRegionState`) | `createConditionedRegion(...)` con el sufijo `<inferior>:<superior>` del tramo en hexadecimal | sí, siempre |
| Total o subtotal sobre regiones (§28.2) | `createConditionedRegion(...)`, sin sufijo | no |
| Piezas declaradas (§26.5) | `createDeclared(...)` | no |
| Recorrido numérico completo (§26.6) | `createDeclared(...)` con el sufijo de la versión del recorrido | no |

El sufijo existe aunque el tramo de la ruta sea la región entera, y solo `conditionedRegionState`
construye claves con él. Dos longitudes sobre la misma ruta comparten el mismo estado, que
conserva sus nodos. No es un defecto y no cambia el código. Lo fijan dos pruebas:
`PreG9BMaintB5aPostR1RoutesTest.theOutcomeOfARouteDoesNotDependOnTheOrderOfTheConsumers`, con
el consumidor de una raíz, que tiene tres regiones y cuyo total es un subtotal, y
`PreG9BMaintB5aAuthorModelDesktopTest.theRouteOfTheHoleLocusDoesNotDependOnTheOrderOfItsConsumers`,
con `w_2` y su subtotal. Ambas miden en los dos órdenes, total primero o rutas primero, y con dos
longitudes sobre la misma ruta, y comparan admisión, bits del valor y del error, registro de la
comprobación (hojas y evaluaciones) y diagnósticos.

## 3. Parámetros finales y su justificación

### 3.1 Análisis del modelo colineal

Simulador del procedimiento (curva escalar `s(u)`, hoja `[0, 1]` con nodos de cuartos;
`artifacts/agent/pre-g9b-maint-b5a-after/sim/GuardSim.java`), 4 096 posiciones de la
singularidad en `[-0,25; 1,25]` de la hoja, diádicas y no diádicas. `J_min`: menor salto
alineado que da la firma en toda posición, en unidades de `s·h_0`. `α` extremo: velocidad
`|u − u*|^-α` con `u*` en un nodo. `α` interior: cualquier posición. Orden: primer punto
estacionario con firma.

| `ρ_J` | `m` | `L` | Coste hoja sin firma | `J_min / (s·h_0)` | `α` extremo | `α` interior | Orden estacionario |
|---|---|---|---|---|---|---|---|
| 0,80 | 3 | 5 | 3 | 0,375 | 0,679 | 0,600 | 21 |
| **0,85** (candidato) | 3 | 5 | 3 | 0,583 | 0,766 | **0,690** | 27 |
| 0,85 | 4 | 7 | 4 | 0,292 | 0,766 | 0,696 | 55 |
| 0,90 | 3 | 5 | 3 | 1,000 | 0,848 | 0,783 | 34 |
| **0,90** (final) | **3** | **6** | **4** | **0,500** | **0,848** | **0,783** | **34** |
| 0,90 | 4 | 7 | 4 | 0,500 | 0,848 | 0,786 | 69 |

Tabla completa (24 combinaciones) en `sim/guardsim.tsv`.

### 3.2 Por qué no los valores candidatos

Con `ρ_J = 0,85`, una velocidad divergente **en el interior** de la ruta, cerca de un nodo y no
en él, da la firma desde `α ≈ 0,69`, por debajo del umbral `0,766` que la norma declara para un
extremo. Medido en el producto: la recta `|u − 0,3712|^0,3` (`α = 0,7`), continua y admitida
hoy, quedaba sin número con firma. Es una negativa conservadora permitida, pero contradice la
lista de no regresión («velocidad divergente con `α` por debajo del umbral») y la raíz cúbica
(`α = 2/3`) quedaba a solo `0,02` del límite.

Con `ρ_J = 0,90` toda `α < 0,783` es número en cualquier posición, lo que cubre todo el rango
que el candidato declaraba admitido. Subir `ρ_J` empeora la resolución de saltos
(`J_min = 1,0·s·h_0` con `L = 5`); un nivel más (`L = 6`) la devuelve a `0,5·s·h_0`, mejor que la
del candidato (`0,583`). El precio es una evaluación más por hoja sin firma (4 en vez de 3) y un
máximo de 6 por hoja. `m = 4` con `L = 7` da lo mismo salvo el orden estacionario (69) y un
máximo de 7 por hoja; se mantiene `m = 3`.

### 3.3 Factor de resolución `2^8`

La cuerda de dos puntos de coordenadas `≤ M_λ` tiene un error de redondeo del orden de
`2√2·(u·M_λ + e_ev)`, donde `e_ev` es el error de evaluación del punto trazado. Con
`ε_res ≥ 2^8·u·M_λ`, toda cuerda examinada tiene un error relativo menor que un 1–2 % cuando
`e_ev` es del orden de un redondeo de las coordenadas, y el cociente, menor que un 2–4 %: lejos
de la separación entre `0,5` (curva lisa) y `0,90`. Con `2^4` ese margen pasa a un 20–35 %,
suficiente para invertir una decisión en una hoja con puntos estacionarios; con `2^12` se
declaran 16 veces más hojas bajo resolución. Medido con las mismas curvas trasladadas
(`explore-090-3-6-k4.tsv`, `-k12.tsv`):

| Factor | Diferencias frente a `2^8` |
|---|---|
| `2^4` | idéntico hasta `T = 10^11`; detecta además P-1 en `10^14` y `10^15` y un salto no alineado en `10^12`, donde la estimación ya no es la de la curva sin trasladar |
| `2^12` | P-1 admitido en `10^13`; un salto no alineado admitido en `10^11`; hojas bajo resolución desde `10^6` |

`2^8` conserva todas las decisiones de `2^4` en el rango en que el estimador mide bien y deja
16 veces más margen frente al ruido de evaluación. Se mantiene el valor candidato.

### 3.4 Presupuesto `E_cap = 16 384`

Con 4 evaluaciones por hoja sin firma, `8 192` solo cubre 2 048 hojas: una ruta continua de la
curva `(u, sin 20u)`, con 2 145 hojas, quedaba sin número por presupuesto. `16 384` cubre 4 096
hojas (`sin 30u`, 3 584 hojas, sigue admitida; `sin 40u` agota el presupuesto y es la prueba del
diagnóstico). Una ruta que lo agota ya exige del estimador del orden de 25 000 evaluaciones, tres
cuartas partes de su propio presupuesto (32 768). La mayor ruta medida hoy (512 hojas,
`a_3`) gasta 2 048 evaluaciones: 8 veces menos que el tope.

## 4. Medición antes y después

Sondas temporales fuera del árbol; fuentes, entradas y salidas en
`artifacts/agent/pre-g9b-maint-b5a-baseline/` (base, con `SUMMARY.md`) y
`artifacts/agent/pre-g9b-maint-b5a-after/` (después). Evaluaciones del estimador de una
reconstrucción idéntica del tramo; evaluaciones de la comprobación del registro de la ruta.

### 4.1 Baterías compartidas post-R1, P-1 y referencias

| Ruta | Antes | Después | Hojas | Estimador | Comprobación |
|---|---|---|---|---|---|
| `(x, e^x)` 0,2→0,7; `k·e^x` k=1 | 0.9388979755898462 | igual | 64 | 321 | 256 |
| `k·e^x` k=2 | 1.6631380213945244 | igual | 64 | 321 | 256 |
| bandas, 0,5→0,6 | 0.2915902683208643 | igual | 64 | 321 | 256 |
| región entera `[0,75; 1]` | 0.6513965339576451 | igual | 64 | 321 | 256 |
| región 1 desde 0 | 1.3520983448323027 | igual | 64 | 321 | 256 |
| antes del salto transversal | 0.42785525348468 | igual | 64 | 321 | 256 |
| lejos del pliegue | 1.2065938688423534 | igual | 64 | 321 | 256 |
| agujero de 2e-12: 0,21→0,6 / 0,6→0,9 | 0.5515432893255071 / 0.4242640687119286 | igual | 64 | 321 | 256 |
| periodo entero 3→−3 (costura) | 0.28318530717958645 | igual | 128 | 642 | 512 |
| periodo entero −3→3 / −1→2 | 5.999999999985266 / 2.999999999992633 | igual | 256 / 128 | 1 473 / 705 | 1 024 / 512 |
| consumidor de una raíz, 3 fracciones | 0.5244195144187664, 0.37025403305268767, 0.5244195144187651 | igual | 64 | 321 | 256 |
| consumidor de una raíz, 2 regiones enteras | 1.4183070533009867 / 1.4040639859731827 | igual | 294 / 400 | 1 701 / 2 337 | 1 176 / 1 600 |
| imagen `Dilate(L, 4)` | 1.0181721084696636 | igual | 64 | 321 | 256 |
| `(u^3, u)` / `(u^8, u^2)` | 1.547865654674981 / 1.6002294276666045 | igual | 93 / 145 | 495 / 807 | 372 / 580 |
| P-1 antes / después del salto (recta), antes (arco) | 0.08944271909999167 ×2 / 0.09140123765294082 | igual | 64 | 321 | 256 |
| **P-1 recta 0,7→0,8** | **2.459674775249769, número** | **sin número**, firma | 64 | 321 | — |
| **P-1 arco 0,7→0,8** | **0.35128942567868376, número** | **sin número**, firma | 66 | 333 | — |

Las rutas que ya no eran número (bandas, regiones distintas, salto transversal, pliegue) siguen
igual y con su causa. `PostR1ConditionedScalarCauseTest` (diagonal, cinco causas admitidas)
pasa sin cambios.

### 4.2 Modelos del autor

Cada región de `w_2` y `a_3` (modelo original y variante) y de `trHole`, en las fracciones
`[0,3; 0,7]`, `[0; 1]`, `[0,05; 0,5]` y `[0,5; 0,95]`: 36 rutas, todas admitidas antes y
después, con el mismo valor bit a bit, cuatro evaluaciones de la comprobación por hoja y
ninguna hoja bajo resolución.

| Lugar | Hojas | Estimador | Comprobación | Tiempo de la orden antes → después |
|---|---|---|---|---|
| `w_2`, región entera | 484 | 2 841 | 1 936 | 3,2–4,2 s → 4,8–5,2 s |
| `a_3`, región entera | 512 | 3 009 | 2 048 | 3,2–5,2 s → 5,1–7,6 s |
| `trHole`, región entera | 383 | 2 235 | 1 532 | 0,8 s → 1,2 s |
| `w_2` / `a_3` / `trHole`, fracciones | 64–194 | 321–1 101 | 256–776 | +0,2–0,5 s |

Los tiempos son de la sonda (incluyen crear los puntos y la orden) y varían entre ejecuciones;
la medida estable son las evaluaciones. Ningún resultado métrico ya presente en los documentos
(`w_1`, `angSC`, `m_2`, `n_2`, `mN`) es una ruta condicionada: `e_2`, `o_1` y el resto de loci
son `COMPLETE`.

### 4.3 Coste

Una hoja sin firma cuesta 4 evaluaciones del punto trazado; el estimador gasta de 5 a 6 por
hoja. La comprobación añade, por tanto, entre un 60 y un 80 % de evaluaciones a una ruta, nunca
más de 6 por hoja ni de 16 384 por ruta. En el peor caso, a ~1,3 ms por evaluación del modelo del
autor, el tope son ~21 s, en rutas cuya estimación ya cuesta del orden de 30 s.

## 5. Batería adversarial y límites declarados

Fixture de nivel de motor (`PreG9BMaintB5aRoutes`): recta de dirección `(1, 2)` a velocidad
`√5` en `[0, 1]`, ruta completa de 64 hojas (`h_0 = 1/256`, umbral analítico
`0,5·s·h_0 ≈ 0,0044`); el recorrido y el estimador leen la misma función.

| Caso | Resultado |
|---|---|
| Saltos de 0,005, 0,01, 0,1 y 1 a ángulos 0, 1e-9, 1e-6, 1e-3, 0,05, 0,1, 0,2, 0,3 y 0,35 rad, en 0,75, 1/3, 0,618…, 0,1234567, junto al final (0,9990234) y junto al inicio (0,0002) | 216 rutas, ninguna número. A ángulos 0, 1e-9 y 1e-6 las refusa la comprobación, con la estimación (`√5 + J`) conservada; desde 1e-4, según el tamaño, y desde 1e-3 siempre, el estimador no converge antes de llegar a ella |
| Dos saltos | firma en la hoja del primero; la comprobación se detiene |
| Salto e ida y vuelta que alcanza un nodo | firma |
| Oscilación rápida junto al salto | sin número: el estimador no converge |
| Rampa continua de altura `H` y anchura `w` | negativa conservadora cuando `w` es mucho menor que los niveles y `H` supera la resolución (`H = 0,1`: `w ≤ 3e-4`; `H = 0,01`: `w ≤ 1e-5`); `H = 0,001` nunca |

Límites declarados de §28.3.1, una prueba cada uno (`PreG9BMaintB5aDeclaredLimitsTest`), sin
afirmar que los casos mostrados sean los únicos:

| Límite | Caso fijado |
|---|---|
| 1 | un único valor distinto en 0,3, nunca evaluado |
| 2 | salto de 1 y vuelta en `[0,3001; 0,3001 + w]`, `w = 1e-4, 1e-6, 1e-9` |
| 3 | saltos alineados de `1e-12` a `3e-3`; la estimación los incluye |
| 4 | salto de 0,01 en el primer cuarto de una hoja cuyo último cuarto es diez veces más rápido; el mismo salto solo en su hoja se observa |
| 5 | ruta de unas pocas ulp con saltos de `1e-15` a `1e-12`: toda hoja bajo resolución, sin evaluaciones |
| 6 | selección testificada distinta en una ventana sin nodos del recorrido, posición continua |

## 6. Traslaciones

Las mismas curvas trasladadas por `(T, T)`, `T = 10^0…10^17`
(`PreG9BMaintB5aTranslationTest`; tabla completa en `explore-final.tsv`). Para estas rutas,
`2^8·u·M_λ` supera la cuota de una hoja desde `T ≈ 1,4·10^3`.

| Magnitud | Observación |
|---|---|
| `T ≤ 10^5` | admisibilidad, evaluaciones de la comprobación, hojas y diagnósticos idénticos a la curva sin trasladar en las seis curvas de la prueba y en las ocho de la sonda |
| `10^6` | el estimador cambia una hoja de `u^8` (145 → 144); la comprobación, 580 → 576 evaluaciones; misma admisibilidad |
| `10^7` | primeras hojas bajo resolución de la comprobación (107, punto estacionario de orden 2), ruta admitida; el estimador falla en `u^8` |
| `10^8`–`10^9` | el estimador falla en la mayoría de curvas; P-1 sigue sin número por firma |
| `10^10`–`10^11` | un salto a 0,2 rad que el estimador rechazaba llega a la comprobación, que lo refusa por firma |
| `≥ 10^12` | toda cuerda en el redondeo de las coordenadas: hojas bajo resolución; P-1 sin número hasta `10^13`, número desde `10^14` (límites 3 y 5 ampliados) |

Rango mostrado sin cambios: coordenadas de magnitud `≤ 10^5` para el resultado completo y
`≤ 10^6` para las decisiones de la comprobación sobre las mismas hojas. No es una garantía
universal. Hallazgo ajeno a B5a: desde `T ≈ 10^11` el **estimador** de la base admite valores
que no son los de la curva sin trasladar (p. ej. 0,1186 en lugar de 0,0894 en `10^11`, 0 en
`10^16`); la comprobación no puede verlo porque todas las cuerdas quedan bajo su resolución.

## 7. Negativas conservadoras y falsos positivos

Medidos con los parámetros finales: velocidad divergente con `α ≥ 0,85` en un nodo y
`α ≥ 0,80` en el interior; rampas continuas muy estrechas (§5); orden estacionario `≥ 34` en el
modelo (las pruebas cubren hasta 30 sin firma). Ninguna ruta de las baterías ni de los modelos
del autor la produce. Ningún resultado afirma un salto probado ni un incumplimiento probado de
H3: los mensajes dicen que la firma es compatible con una discontinuidad o con una variación
singular de velocidad.

## 8. Pruebas

Nuevas (compartidas, `org.geocedg.common.locus`): `PreG9BMaintB5aChordScalingCheckTest` (5),
`PreG9BMaintB5aAdversarialBatteryTest` (5), `PreG9BMaintB5aContinuousReferencesTest` (5),
`PreG9BMaintB5aDiagnosticsTest` (5), `PreG9BMaintB5aDeclaredLimitsTest` (6),
`PreG9BMaintB5aTranslationTest` (3), `PreG9BMaintB5aPostR1RoutesTest` (13, con la prueba de orden de §2.3) y
`PreG9BMaintB4SliderPieceReorderingTest` (1, B4: al mover una pieza de deslizadores a otro
lugar, el token de cada lugar nombra la raíz de la pieza que ahora está allí; sin marcos
conservados). Desktop: `PreG9BMaintB5aAuthorModelDesktopTest` (3, con la prueba de orden de §2.3), que lee el número
(`GeoNumeric`) de cada orden. Ninguna prueba existente cambia.

Selección `PHASE PRE-G9B-MAINT-B5A`: compartida (las ocho clases nuevas, las clases post-R1 de
métrica condicionada y las de estimador de CU1/CU2/P2) y Desktop (la prueba nueva,
`PostR1ConditionedLengthDesktopTest`, `PostR1LengthDeletionDesktopTest` y las tres pruebas de
la guía): 171 y 46 identidades de evidencia ejecutada; descubrimiento compartido 7 057 → 7 100
y Desktop 2 627 → 2 630 (evidencia `--test-dry-run`); pin del inventario en el registro y
recuentos de `verification-final-coverage.Tests.ps1` (102 selecciones, 91 fases) actualizados.
Las selecciones `final.*` no se refrescan porque solo `FINAL` las exige y `FINAL` no está
autorizado (precedente U2). Regresión compartida completa en el árbol de trabajo:
8 076 pruebas, 0 fallos, 10 omitidas. En Desktop se ejecutaron la selección de la fase y las
clases `PostR1Conditioned*Desktop*`, sin fallos; la batería Desktop completa no se ejecutó.

## 9. Nota de versión (compatibilidad)

Sin XML nuevo y sin carga persistida. Un documento de cualquier versión con
`LocusLength(L, A, B)` sobre una ruta condicionada cuya comprobación observa una firma, queda
inconclusa o agota el presupuesto muestra un número indefinido donde 1.2.2 mostraba uno; su
resultado rico conserva la estimación con el diagnóstico, y las fórmulas que leen el número
quedan indefinidas por el grafo. La versión de la admisión pasa a `conditioned-length-scalar/v2`.
Ninguna ruta de los modelos del autor cambia.

## 10. Guías e impacto de bootstrap

`GUIDE_IMPACT = UPDATE`: §6.9 de las guías EN/ES añade un párrafo sobre la comprobación, sus
tres causas de indefinición y lo que no detecta. Impacto de bootstrap: sin cambio; ningún
prerrequisito de estación ni supuesto del bootstrap de Windows cambia.

## 11. Asuntos para la decisión del autor

Propuesta de B5a, aceptada por el coordinador para verificación; la decisión es del autor.

| Id | Decisión | Propuesta | Justificación |
|---|---|---|---|
| D-B5a-1 | `ρ_J` | `0,90` (candidato `0,85`) | §3.2: con `0,85`, velocidad divergente interior con `α < 0,766` refusada |
| D-B5a-2 | `m` | `3` (candidato) | §3.1 |
| D-B5a-3 | `L` | `6` (candidato `5`) | §3.2: devuelve la resolución de saltos a `0,5·s·h_0` con `ρ_J = 0,90` |
| D-B5a-4 | factor de `ε_res` | `2^8` (candidato) | §3.3 |
| D-B5a-5 | `E_cap` | `16 384` (candidato `8 192`) | §3.4: cubre 4 096 hojas a 4 evaluaciones |
| D-B5a-6 | Lecturas L-1 a L-9 de la norma | como en §2.2 | ninguna exige una decisión normativa nueva |

Riesgos que la decisión debe considerar:

1. **Velocidad divergente interior**: el umbral de §28.3.1 es el de un extremo; en el interior
   la firma aparece antes (§3.1). Con los valores finales, desde `α ≈ 0,78`.
2. **Coste**: entre un 60 y un 80 % más de evaluaciones por ruta condicionada (§4.3).
3. Deudas que siguen diferidas: B5b, T-A, descubrimiento de raíces de `Intersect` a través de un
   salto alineado.

## 12. Deuda previa, fuera del alcance de B5a

Registrada por el coordinador como deuda previa; B5a no la corrige.

1. **Pin de contrato estático de `geocedg/resources/assets-manifest.yml`.** Un `INFRA_UNIT` de
   desarrollo sobre el árbol del candidato (copia del registro, ignorada por Git, en
   `artifacts/agent/pre-g9b-maint-b5a-infra-dev-copy/`) aceptó todas las hojas salvo `infra.static-semantic`, que falla con «hash
   differs geocedg/resources/assets-manifest.yml». Hash fijado en
   `verification-static-contracts.json`: `5790ba1cb31ea5d9b5c3952f8bfbb96c8c3338460f795eebbc6ddcc7e27cace5`.
   Hash canónico del fichero: `9dd0a986978f6f0d193dca5ebcc0550ee9de9539f1442edd53686fab41d04c9a`.
   Ninguno de los dos ficheros difiere de la base `228580e96` (`git diff --quiet` sobre ambos).
   El último cambio del manifiesto es U2-R1 (`f99a99785`, iconos nuevos registrados), que no
   volvió a fijar el contrato. `STATIC` no evalúa esa prueba y acepta el candidato.
2. **Estimador en coordenadas muy grandes.** Con la ruta trasladada por `(T, T)`
   (`explore-final.tsv`, `PreG9BMaintB5aTranslationTest`), el estimador §25 de la base admite
   valores que no son los de la ruta sin trasladar:

   | Ruta | Sin trasladar | `T = 10^11` | `T = 10^13` | `T = 10^16` |
   |---|---|---|---|---|
   | P-1, antes del salto | 0.08944271909999162 | 0.11855341813151267 | 0.12109375 | 0.0 |
   | `exp`, 0,2→0,7 | 0.9388979755898462 | sin número | 1.1591073960891642 | 0.0 |

   Esos valores salen del estimador, no de la comprobación: en esas magnitudes todas las
   cuerdas quedan bajo `ε_res` y la comprobación no evalúa nada.
