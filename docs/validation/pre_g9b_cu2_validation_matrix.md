# PRE-G9B-CU2 — Matriz de validación y criterios de aceptación

| Campo | Valor |
|---|---|
| Carácter | Matriz candidata de validación de la implementación de `CU2`. No es evidencia de ejecución |
| Estado | Matriz de aceptación de la implementación autorizada el 2026-10-21 ([registro](pre_g9b_cu2_author_architecture_approval_record.md)); los casos críticos y sintéticos exactos son pruebas obligatorias; `selfApproved=false` |
| Normas | [propuesta normativa](../architecture/pre_g9b_cu2_normative_proposal.md); [ADR 0038 (ACCEPTED, 2026-10-21)](../adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md); [estrategia](../architecture/pre_g9b_cu2_implementation_strategy.md) |
| Diseños | [certificación](../architecture/pre_g9b_cu2_hybrid_certification_design.md); [evento de la longitud orientada](../architecture/pre_g9b_cu2_oriented_length_event_design.md); [consumo y representación](../architecture/pre_g9b_cu2_consumption_and_representation.md); [análisis del modelo del autor](../architecture/pre_g9b_cu2_author_model_certification_analysis.md) |
| Decisiones aplicadas | C1-1 (puertas por contadores) y C1-4 (`preserve` frente a `pins`) del [registro de cierre de `CU0`](pre_g9b_cu0_closeout_record.md); D-CU1-2 (contrato de estimación) del [registro de cierre de `CU1`](pre_g9b_cu1_closeout_record.md) |
| Revisión | 2026-10-20, revisión focalizada P1–P5: filas V-CRIT (sustituyen a V-TR-3), V-FOLD (sustituyen a V-JET-1), V-SEL, V-AVAIL, V-UNIF, V-COMPL, V-E2-8, V-E2-9 y V-LIN-6; V-LIN-3 y V-LIN-4 revisadas; puerta GC-8. Cierre de coherencia C1–C3 (2026-10-20): §3.5c nuevo (V-COINC-1 a V-COINC-9, amplía V-SEL-3), §3.5d nuevo (V-POINT-1 a V-POINT-6, amplía V-CRIT-5), V-REV-EXACT-1, V-SEL-3 y V-CRIT-5 revisadas, referencias de §1 y límites de §5. Aprobación autoral (2026-10-21, paso T0): V-REV-EXACT-1 ampliada a V-REV-EXACT-1 a V-REV-EXACT-10 según D-CU2-20-R |

## 1. Referencias analíticas y exactas

Todas las fronteras de la primera familia se comparan con la fórmula del
[análisis](../architecture/pre_g9b_cu2_author_model_certification_analysis.md) §3.2,
`t_c ± β`, con `t_c = π/2 − θp` y `β = arccos((rbCone − excCyl·cos θp)/rbCylinder)`,
evaluada con aritmética de precisión ampliada a partir de los parámetros del modelo. Nunca se
comparan con la validez puntual del evaluador, que excede la raíz exacta hasta 4,3·10⁻⁶
(hallazgo F-CU2-1). Cerca del valor crítico la referencia es la clasificación exacta del
análisis §3.8.3, no la fórmula en coma flotante, que se equivoca en un valor (hallazgo F-CU2-9).

| Dato | Valor de referencia |
|---|---|
| `t_c` | 1,2767651295 |
| valor crítico de la mordedura (`β = 0`) | excCyl = 3,656943727… (no representable en binary64) |
| último binary64 de penetración y primero de mordedura (captura actual) | `0x1.d416bb67a38bbp1` y `0x1.d416bb67a38bcp1`; en el segundo, dos raíces simples separadas 2,4·10⁻⁸ |
| entrada del punto guardado `N` en el hueco (H01) | excCyl = 3,958845384 |
| extremos analíticos a excCyl = 4,0 | 0,6024120274 y 1,9511182317 |
| parámetro guardado de `N` (`pins.H01`) | 0,6456498832 |
| configuración crítica sintética exacta | `p`: centro `(0, 0)`, radio 25; `P = (−25, 50)`; `g_1`: centro `(35, −45)`, radio 40; raíz doble en `u = 1/2`, `t = atan2(4, 3)` |
| tangentes desde `P` a `p` en las configuraciones sintéticas | `3x + 4y = 125`, que toca `p` en `(15, 20)`, y `x = −25`, que lo toca en `(−25, 0)` |
| tangencia exterior sintética (V-CRIT-5, V-POINT-1) | `g_1`: centro `(83, 19)`, radio 40; componente puntual en `t* = atan2(−4, −3)` (`u = −2`), con `λ > 0` y `Q = R = (15, 20)`; `D ≤ 0` a ambos lados |
| dos componentes puntuales sintéticas (V-POINT-2) | `Line(P, N)` (no un rayo: en `t = ±π` la raíz tiene `λ < 0`) con `N` sobre la circunferencia de centro `(15, 70)` y radio 40, inscrita en el ángulo exterior de las dos tangentes; `D ≤ 0` con ceros dobles en `t = ±π` (costura) y `t = atan2(−4, −3)` |
| supresión heredada de una segunda salida sin etiqueta (análisis §3.9) | 49 de 201 muestras indefinidas en el vivo y 55 en el corte, con `|t − t_c| ≤ 10⁻⁸` en el binary64 de penetración más próximo |
| giro de los loci en `t_c` (penetración más próxima) | 133,7° (`o`, `v`); 147,3° (`w`, `a_1`) |
| pliegues de `e_1` | `t = 0` y `t = π`; `|F″(0)|` ≈ 3,93 y `|F″(π)|` ≈ 1,27 |

## 2. Puertas de aceptación por contadores

Se miden con contadores públicos, sin reflexión, como en `CU1`. Los tiempos son indicadores
complementarios y nunca sustituyen a estas puertas.

| Puerta | Criterio |
|---|---|
| GC-1 | Mover solo el conductor de cualquier locus del modelo, también dentro y fuera de un hueco, publica **0** revisiones y hace **0** cálculos de certificado |
| GC-2 | Cambiar una entrada no conductora (`excCyl`, `rbCone`, `v_1`, un punto libre) hace **como máximo 1** cálculo de certificado por locus afectado y revisión |
| GC-3 | El dibujo, la métrica, las intersecciones, los puntos semánticos y la exportación leen el mismo certificado: **0** cálculos de certificado adicionales por consulta de un consumidor |
| GC-4 | Las cajas de existencia por rama y revisión no superan el presupuesto versionado en ningún locus del modelo ni en ningún régimen del barrido de §3.2; el agotamiento solo se admite con `UNRESOLVED` tipado |
| GC-5 | Los vértices de dibujo por vista no superan el presupuesto versionado |
| GC-6 | Durante la certificación de `e_2` se leen **0** valores métricos: el certificado usa solo hechos estructurales |
| GC-7 (informativa) | Carga del modelo, arrastre con y sin `e_2` y dibujo, medidos con el banco antes y después |
| GC-8 | El certificado algebraico exacto de cada predicado de primer nivel del modelo, en todos los valores de §3.2 y §3.2b, no supera sus presupuestos de bits y de bisecciones; su tiempo es informativo (hoy, de 2 a 53 ms) |

## 3. Pruebas

Tipos: **F** focal, **A** adversaria, **R** regresión, **B** banco.

### 3.1 Niveles de evidencia y estructura del certificado

| Id | Requisito | Construcción | Aserción | Tipo | Fijación |
|---|---|---|---|---|---|
| V-LVL-1 | La evidencia exploratoria nunca se promueve | locus con un paso fuera de la clase v3 (intersección cónica–cónica) | `NOT_ESTABLISHED`; evidencia exploratoria presente; cero componentes certificadas; sin completitud ni exportación de la evidencia; ninguna autoridad sobre el DAG ni la identidad | F | — |
| V-LVL-2 | La evidencia exploratoria no se persiste ni avanza revisiones | el locus anterior; guardar y reabrir; cambiar solo la política exploratoria | misma huella de revisión; la evidencia se recalcula | F | — |
| V-LVL-3 | Admisibilidad local independiente de la completitud (ADR 0027) | locus con una componente local certificada y una banda `UNRESOLVED` | los consumidores locales aceptan la componente; los globales devuelven `NOT_ESTABLISHED` | F | — |
| V-COMPL-1 | La completitud es un recubrimiento | el modelo con un presupuesto de cajas de prueba que se agota antes de cubrir el dominio, aunque todas las cajas exploradas sean válidas | `NOT_ESTABLISHED` con la región no cubierta explícita; nunca `COMPLETE` | A | — |
| V-CMP-1 | Continuidad de `F` como requisito | locus sintético que depende de una longitud orientada con origen fijo sobre una circunferencia | el salto es frontera de la componente, nunca un punto interior | F | — |
| V-ARC-1 | Arcos que cruzan la costura | primera familia en mordedura (excCyl 3,7) | **una** componente, arco de inicio canónico `t_c + β` y longitud `2π − 2β`, ambos extremos cerrados | F | `pins.B07` |
| V-ARC-2 | Pertenencia sobre el círculo | el arco anterior | `±π` pertenecen al arco; cada parámetro canónico tiene una sola coordenada de arco | F | — |
| V-UND-1 | Indefinición computacional | dependencia cuyo cálculo falla (política de prueba) antes de certificar la existencia | `UNRESOLVED` con causa tipada, nunca `CERTIFIED_DOES_NOT_EXIST` | F | — |
| V-UNIF-1 | `UNIFORMLY_INVALID` solo con prueba | cajas donde la envolvente de un divisor, una dirección o un radio contiene el cero sin ser idénticamente nulo; y un factor idénticamente nulo | `UNRESOLVED` en el primer caso y `UNIFORMLY_INVALID` en el segundo; las desigualdades estrictas dan `UNIFORMLY_INVALID` por intervalos | A | — |

### 3.2 Clase v3, transiciones, totalidad y periodicidad

| Id | Requisito | Construcción | Aserción | Tipo | Fijación |
|---|---|---|---|---|---|
| V-V3-1 | Envolventes de las capacidades nuevas | rayos, segmentos, circunferencia por centro y punto, recta–circunferencia, pasos de varias salidas, operaciones vectoriales y macros expandidas | en cajas aleatorias deterministas, la envolvente de valor y derivada contiene la referencia de precisión ampliada | F | — |
| V-V3-2 | Resultado de caja tipado | un caso por causa: `DISCRIMINANT_NEGATIVE`, `RAY_PARAMETER_NEGATIVE`, `SEGMENT_PARAMETER_OUTSIDE`, `ZERO_DIRECTION`, `ZERO_RADIUS`, `DIVISOR_ZERO` | `UNIFORMLY_INVALID` con la causa esperada (las de igualdad, por identidad estructural); solo esa salida da `CERTIFIED_DOES_NOT_EXIST` | F | — |
| V-V3-3 | Fuera de la clase | la construcción `{Q, R}` en modo continuo; una intersección por punto de referencia | `NOT_ESTABLISHED` con evidencia exploratoria | F | — |
| V-V3-4 | Límite de pasos | corte de `e_2` con las macros expandidas; corte sintético que excede el límite | el primero cabe; el segundo da `NOT_ESTABLISHED` con causa tipada | F | — |
| V-TR-1 | Transiciones certificadas | primera familia con excCyl ∈ {3,66; 3,7; 3,85; 3,9; 3,95; 3,96; 4,0; 4,5; 5,0} | dos fronteras por locus; cada corchete contiene la referencia analítica de §1 y su anchura no supera `δ`; extremos cerrados | F | `pins.B07` |
| V-TR-2 | Inclusión de extremos según el predicado | rayo (`λ = 0`), segmento, recta–circunferencia (`D = 0`) y un divisor que se anula (dos rectas que se vuelven paralelas) | cerrado, cerrado, cerrado y abierto, respectivamente | F | — |
| V-TR-4 | La validez puntual no es frontera | los casos de V-TR-1 | `preserve.branch_ends` se reproduce sin cambio (mide el evaluador, que `CU2` no cambia) y no coincide con la frontera certificada; la diferencia queda dentro del exceso medido | R | — |
| V-TOT-1 | Totalidad estructural | locus de punto medio y de traslación sobre una circunferencia | `COMPLETE` sin cálculo de cajas (contador 0) | F | — |
| V-PER-1 | Periodicidad estructural | primera familia a excCyl 2,9; `e_1`; `o_1` | `COMPLETE`, una componente periódica cerrada; la continuidad en la costura se certifica con cajas que la envuelven | F | `pins.B07` |
| V-PER-2 | La periodicidad del conductor no basta | locus con un paso con historia (modo continuo) o con un valor aleatorio sobre un conductor circular | ninguna identificación periódica; no se declara componente cerrada | A | — |
| V-PER-3 | Puntos aislados de la construcción | `rbCone` = 8 y 10 | en los conductores donde dos rectas coinciden: frontera abierta certificada o banda `UNRESOLVED`; ninguna componente las atraviesa | A | — |

### 3.2b Valor crítico y degeneraciones (P1)

Estas filas prueban el certificado de existencia y continuidad. No crean ni materializan puntos
de intersección ni de contacto, que pertenecen a `CU3`.

| Id | Requisito | Construcción | Aserción | Tipo | Fijación |
|---|---|---|---|---|---|
| V-CRIT-1 | Configuración crítica construida estructuralmente | configuración sintética entera de §1 como construcción GeoCeDG (puntos y radios enteros, conductor sobre `g_1`) | `COMPLETE`; una componente periódica cerrada por locus; un parámetro singular certificado en `t = atan2(4, 3)` con multiplicidad 2 y signos `+/+`; `Q = R` allí | F | — |
| V-CRIT-2 | Parámetros inmediatamente anteriores y posteriores | radio `40 ± 2⁻ᵏ`, `k` ∈ {10, 30, 47} (`2⁻⁴⁷` es la unidad de la última cifra de 40) | `40 − 2⁻ᵏ`: penetración, sin raíces reales; `40 + 2⁻ᵏ`: mordedura, dos raíces simples alrededor de `atan2(4, 3)` | F | — |
| V-CRIT-3 | Separación arbitrariamente pequeña de raíces | los mismos datos como constantes racionales exactas en el certificado, `k` ∈ {80, 200} | dos raíces simples con envolventes disjuntas, o `UNRESOLVED` tipado si se agota el presupuesto; nunca una sola raíz ni una componente que atraviese el hueco | A | — |
| V-CRIT-4 | Discriminante no negativo con cero doble | V-CRIT-1 | ninguna transición publicada en `t_c`; ninguna banda `UNRESOLVED`; el predicado se declara cierto en todo el círculo | F | — |
| V-CRIT-5 | Desaparición real de soluciones | `g_1` de centro `(83, 19)` y radio 40 (tangente exterior); modelo con excCyl 3,7 | en el primero, una componente puntual `{t*}` con `t* = atan2(−4, −3)`, nunca un intervalo (§3.5d); en el segundo, dos transiciones simples que contienen las referencias de §1 | F | — |
| V-CRIT-6 | Continuidad y selección de ramas | V-CRIT-1 | `λ_Q` continua en `t_c` con derivadas laterales distintas; `Q` invierte su movimiento sobre la circunferencia (tangentes laterales opuestas); el locus trazado tiene una esquina; la selección es la misma regla a ambos lados | F | — |
| V-CRIT-7 | Configuraciones binary64 contiguas al valor crítico | modelo con los trece binary64 centrados en el valor crítico | clasificación exacta: penetración hasta `0x1.d416bb67a38bbp1` y mordedura desde `0x1.d416bb67a38bcp1`; ninguna raíz doble; los puntos del evaluador dentro del hueco exacto no se publican como válidos | F | — |
| V-CRIT-8 | Restauración con deshacer y rehacer | modelo: excCyl 2,9 → 3,7 → `0x1.d416bb67a38bcp1` → 2,9, deshaciendo y rehaciendo cada paso | certificado, transiciones de linaje y estado de selección idénticos bit a bit a los de una construcción recién creada con el mismo estado | F | — |
| V-CRIT-9 | Variación de parámetros no conductores | modelo con `rbCylinder` 1,5 → 2,0 y con `α_cyl` distinto | el valor crítico se desplaza según la fórmula; la clasificación exacta lo sigue; ninguna componente se reutiliza de la revisión anterior | F | — |
| V-CRIT-10 | Determinismo e identidad | V-CRIT-1, V-CRIT-7 y V-CRIT-9, dos veces | evidencia del certificado exacto (coeficientes, raíces, multiplicidades, envolventes) idéntica bit a bit; claves de linaje estables cuando solo se desplazan las fronteras | F | — |

### 3.2c Pliegues (P3)

| Id | Requisito | Construcción | Aserción | Tipo | Fijación |
|---|---|---|---|---|---|
| V-FOLD-1 | Pliegues certificados | `e_1` | en `t = 0` y `t = π`: simetría estructural verificada (igualdades exactas de las constantes capturadas y tabla de operaciones), aislamiento por una componente de `F″`, `F″(t₀) ≠ 0`, continuidad y tangentes laterales opuestas; la exención del giro se limita a sus bandas | F | `pins.B06` |
| V-FOLD-2 | La simetría no se supone | `e_1` con `I` desplazado `2⁻⁴⁰` fuera de `i` | ningún pliegue certificado; el giro se refina con diagnóstico | A | — |
| V-FOLD-3 | Un giro cerrado no es un pliegue | el giro de `w` (velocidad 0,067 en t = 1,5999) y los loci en `0x1.d416bb67a38bbp1` | ningún pliegue ni exención; refinamiento hasta `θ_max` o hasta la resolución del parámetro, con diagnóstico | A | — |
| V-FOLD-4 | Pliegue degenerado | curva simétrica con `F″(t₀) = 0` (contacto de orden superior) | existencia probada por la simetría, pero sin certificado de pliegue no degenerado; giro refinado con diagnóstico | A | — |

### 3.3 Evento de la longitud orientada y `e_2`

| Id | Requisito | Construcción | Aserción | Tipo | Fijación |
|---|---|---|---|---|---|
| V-E2-1 | `e_2` como una componente abierta | modelo del autor | `COMPLETE`, **una** componente, arco `[t_N, t_N + 2π)` con `t_N = 0` exacto, cerrada en el origen (`zero-length`) y abierta en el otro extremo; no periódica | F | `pins.B10.2` |
| V-E2-2 | Valores idénticos | modelo del autor | `preserve.e_2`, `preserve.e_2_seam_distance` y `preserve.oriented_length_profile` se reproducen dentro de su tolerancia; ningún valor de `LocusLength` cambia | R | — |
| V-E2-3 | Sin segmento de cierre | dibujo, DXF y LaTeX de `e_2` | un trazo abierto; ningún segmento une los extremos (hoy 13,19 unidades); `LWPOLYLINE` sin marca de cierre; trayectoria sin `cycle` | F | `pins.B10.2` |
| V-E2-4 | Evento exacto | `w_1` | un evento `ORIGIN_WRAP` con `driverParameter` igual a la dirección canónica de `N_1`, dirección `FORWARD`, política `ZERO_LENGTH`, dos extremos laterales, `jumpPositive = CERTIFIED` y `jumpValue` con la clase de la longitud total de `o_1` (`ESTIMATED`) | F | — |
| V-E2-5 | Certificado independiente de la clase de la longitud | forzar con una política de prueba el fallo de la estimación de `w_1` en un conductor | la estructura de existencia es idéntica; solo ese punto pasa a `EVALUATION_FAILED`; GC-6 | F | — |
| V-E2-6 | Casos del evento | sentido atrás; origen en la costura; origen que depende del conductor; fuente solo localmente certificada; fuente degenerada con salto no certificado; ruta explícita entre posiciones fijas | los resultados de la tabla de §4.3 del [diseño del evento](../architecture/pre_g9b_cu2_oriented_length_event_design.md) | F | — |
| V-E2-7 | Identidad del evento | arrastrar el conductor; cambiar `v_1` | arrastrar no cambia revisión, evento ni componente; cambiar `v_1` publica revisión nueva con el mismo linaje y fronteras nuevas | F | — |
| V-E2-8 | Extremos laterales | `e_2` con `zero-length` y con `full-cycle` | `t_N` pertenece en `s = 0` con `zero-length` y en `s = 2π` con `full-cycle`; el extremo excluido no es imagen de ningún parámetro canónico; la consulta del límite lateral, con su lado, da `w_1 = L` o `0`; los dos extremos tienen fuentes distintas | F | — |
| V-E2-9 | Extremo no evaluable | forzar el fallo del límite lateral (estimación de la longitud total) | el trazo y las exportaciones terminan en el último vértice evaluado y declaran el extremo; nada se extrapola; la existencia no cambia | F | — |

### 3.4 Publicación (H01)

| Id | Requisito | Construcción | Aserción | Tipo | Fijación |
|---|---|---|---|---|---|
| V-PUB-1 | Publicación independiente del punto trazado | excCyl 4,0 con el conductor guardado (dentro del hueco) | los cuatro loci se publican `COMPLETE` con un arco cuyos corchetes contienen los extremos analíticos de §1; el punto trazado está indefinido y el locus no | F | `pins.H01` |
| V-PUB-2 | Ninguna definición obsoleta (F-CU2-3) | excCyl 3,95 → 3,96 → 4,0 → 4,5 → 5,0 con el conductor dentro del hueco | cada revisión publica su propia geometría, igual bit a bit a la de una construcción recién creada con el mismo estado | F | — |
| V-PUB-3 | Recubrimiento vacío | configuración con `D < 0` en todo el círculo (excCyl > 6,7915, fijado por prueba) | según D-CU2-8: publicado, válido, `COMPLETE`, cero componentes; intersecciones `EMPTY` completas; longitud total cero | F | — |
| V-PUB-4 | Entradas de definición indefinidas | un punto libre del corte indefinido | locus `UNDEFINED`, sin definición anterior visible | F | — |
| V-REV-EXACT-1 | Binary64 adyacentes al valor crítico (D-CU2-20-R) | modelo con excCyl `0x1.d416bb67a38bbp1` → `0x1.d416bb67a38bcp1`, con el conductor en `t = −1` y en `t_c` (punto trazado indefinido) | cada cambio de un ulp publica una revisión en cualquier posición del conductor; la copia del corte tiene los mismos bits que el vivo; cada revisión tiene el certificado de su propia configuración (penetración o mordedura) | F | — |
| V-REV-EXACT-2 | Ida y vuelta | V-REV-EXACT-1 con `bb → bc → bb → bc` | la huella vuelve al mismo valor con el mismo estado; los certificados de cada visita son idénticos bit a bit; ningún certificado se reutiliza entre estados distintos | F | — |
| V-REV-EXACT-3 | Locus cuya entrada difiere en un ulp | `lr2 = LocusV2(R2, N)` y un locus sintético con un número libre cambiado en un ulp | el corte de cada revisión contiene el valor exacto; el locus se evalúa con la configuración viva | F | — |
| V-REV-EXACT-4 | Comparación exacta de entradas autoritativas | cortes con valores de elementos independientes, literales escritos con 17 cifras, literales sin texto original (fijados por programa) y argumentos literales en línea | la validación compara por bits cada entrada autoritativa con su copia; las restauraciones admitidas devuelven la fidelidad; una discrepancia no restaurable da `NOT_ESTABLISHED` con causa `RECONSTRUCTION_FIDELITY_NOT_ESTABLISHED`, sin certificado y con evaluaciones `EVALUATION_FAILED`; nunca una evaluación silenciosa de otra configuración | A | — |
| V-REV-EXACT-5 | Contraste con un documento recién construido | cada estado de V-REV-EXACT-1 a V-REV-EXACT-4, comparado con la misma construcción creada desde cero | huella, corte, programa capturado y certificado idénticos bit a bit | F | — |
| V-REV-EXACT-6 | Movimientos exclusivos del conductor | arrastre del conductor dentro y fuera del hueco y a través de `t_c` | ninguna revisión ni reconstrucción (GC-1); el estado de selección no cambia en el modelo del autor | F | — |
| V-REV-EXACT-7 | Cambios del estado de selección | primera inicialización en una raíz doble (V-COINC-3); `updateConstruction`; cambio de etiquetado de una salida | la transición real del estado heredado publica una revisión con causa tipada; los recálculos posteriores en el mismo estado no publican ninguna; la copia reproduce el estado vivo o la selección falla cerrada | A | — |
| V-REV-EXACT-8 | Reproducibilidad | V-REV-EXACT-1 a V-REV-EXACT-7, dos ejecuciones | huellas, contadores y certificados idénticos | F | — |
| V-REV-EXACT-9 | Guardar, reabrir, deshacer y rehacer | los estados de V-REV-EXACT-1 y V-REV-EXACT-4 | tras reabrir, la instantánea y el certificado corresponden al estado recargado; deshacer y rehacer restauran revisiones con el mismo contenido; una pérdida de precisión del formato persistido, si existe, queda caracterizada y nunca se asocia al certificado anterior | F | — |
| V-REV-EXACT-10 | Propagación, cachés y extremos | locus anidado y `LocusLength` dependientes del valor cambiado; valores extremos (`±1e-300`, subnormales, `1e300`) y entradas indefinidas | los loci anidados, las métricas y los certificados se invalidan y recalculan con la revisión nueva; ningún estado obsoleto sobrevive; los extremos no fabrican igualdad de huellas ni fidelidad | A | — |

### 3.5 Linaje y migración (E-3 revisada)

| Id | Requisito | Construcción | Aserción | Tipo | Fijación |
|---|---|---|---|---|---|
| V-LIN-1 | Linaje estable con fronteras móviles | excCyl 3,7 → 3,85 → 3,9 | la clave de linaje no cambia; solo cambian las fronteras | F | — |
| V-LIN-2 | Transición registrada | excCyl 2,9 → 3,7 → 2,9 | círculo a arco y vuelta, con transiciones tipadas y relación explícita; ninguna reasociación por etiqueta, índice, coordenadas o proximidad | F | — |
| V-LIN-3 | Selector guardado con correspondencia estructural | `N_1` con el selector `text2` que cita la componente `[−π, π)` de `o_1` | se resuelve por igualdad de la descomposición, con la contención como validación; `preserve.state` sin cambio; al guardar, `text2` se conserva byte a byte | R | — |
| V-LIN-4 | Selector ambiguo | documento sintético con un selector que interseca dos componentes certificadas | selector sin resolver y punto indefinido con causa tipada; ninguna reasociación silenciosa | A | — |
| V-LIN-5 | Archivos antiguos | documentos guardados antes de `CU2` | certificados recalculados al cargar; ninguna componente ni identidad adivinada (ADR 0027, decisión 9) | R | — |
| V-LIN-6 | La contención no crea correspondencia | documento con un selector que cita la componente completa de un locus que ahora está en mordedura, con su parámetro dentro del nuevo arco | selector sin resolver con causa tipada: la descomposición cambió sin una transición registrada desde la componente citada | A | — |

### 3.5b Selección de raíces e identidad (P4)

| Id | Requisito | Construcción | Aserción | Tipo | Fijación |
|---|---|---|---|---|---|
| V-SEL-1 | Regla procedente del algoritmo | `{Q, R}`, `H_1` y `G_1` del modelo | la captura lee el estado (`[0, 1]` para `{Q, R}` y `H_1`; inicialización con compactación para `G_1`); `Q` es la raíz de `λ` menor; el certificado es idéntico con el conductor en cualquier posición | F | — |
| V-SEL-2 | Conductor que empieza sin intersección | documento guardado con excCyl 4,0 y el conductor en el hueco, reabierto | la regla se captura sin muestra válida; certificado idéntico al de V-SEL-1 para ese valor | F | — |
| V-SEL-3 | Tangencia | fronteras de V-TR-1 y parámetro singular de V-CRIT-1 | `Q = R` en la frontera; ambas salidas, etiquetadas, definidas y continuas; ninguna permutación nueva. Ampliada en §3.5c | F | — |
| V-SEL-4 | Cambio entre penetración y mordedura | excCyl 2,9 ↔ 3,7 | la identidad semántica de la selección no cambia; transición de linaje registrada; revisión nueva | F | — |
| V-SEL-5 | Desaparición temporal de una raíz | conductor que atraviesa el hueco a excCyl 3,7 | salida indefinida en el hueco; la misma regla a ambos lados; un solo arco | F | — |
| V-SEL-6 | Permutación heredada y orden de consultas | construcción sintética cuyo algoritmo vivo tiene la permutación `[1, 0]`; el modelo consultado primero por el dibujo y después por la métrica, y al revés | la copia del corte reproduce el estado vivo o la selección falla cerrada con diagnóstico; el certificado no depende del orden de las consultas | A | — |
| V-SEL-7 | Guardar, reabrir, deshacer y rehacer | el modelo y la construcción de V-SEL-6 | el estado de selección se vuelve a leer; si es igual, contenido idéntico; si cambia, revisión nueva con diagnóstico, sin reasociación silenciosa | F | — |

### 3.5c Salidas efectivas en raíces coincidentes (C1)

Amplían V-SEL-3. «Salidas etiquetadas» es `{Q, R}` (o `Intersect(p, t)` con las dos salidas
etiquetadas); «segunda sin etiqueta» es `R2 = Intersect(p, t2, 2)` con `t2 = Ray(P, N)`, cuyas
salidas auxiliares no tienen etiqueta. Cada fila se ejecuta en la configuración crítica sintética,
en los dos binary64 del modelo contiguos al valor crítico y en la frontera simple de excCyl 3,7.

| Id | Requisito | Construcción | Aserción | Tipo | Fijación |
|---|---|---|---|---|---|
| V-COINC-1 | Tangencia con ambas salidas etiquetadas | salidas etiquetadas | la captura lee el etiquetado, igual en el vivo y en la copia del corte; en la raíz doble las dos salidas están definidas y coinciden; componente periódica cerrada con parámetro singular | F | — |
| V-COINC-2 | Tangencia con la segunda sin etiqueta | segunda sin etiqueta y su locus `lr2 = LocusV2(R2, N)` | banda `UNRESOLVED` con causa tipada (supresión heredada de la segunda salida) que contiene todas las muestras indefinidas del vivo y del corte; `lr2` `NOT_ESTABLISHED`, con componentes locales a ambos lados; nunca `COMPLETE` ni continuidad a través de la banda | A | — |
| V-COINC-3 | Raíz doble en la primera inicialización | las dos variantes creadas con el conductor exactamente en la raíz doble de la configuración crítica sintética, y documentos guardados así y reabiertos | mientras el algoritmo sigue en inicialización (dos raíces iguales no lo inicializan), la regla capturada es la compactación en orden bruto, certificada porque las dos raíces están en el rayo en todo el círculo; designa las mismas raíces que la permutación `[0, 1]` que el algoritmo fija al separarse; ese cambio de estado se vuelve a leer y publica una revisión con diagnóstico y las mismas componentes (D-CU2-18); con la segunda sin etiqueta, la salida 2 está indefinida en `t_c` y dentro de la banda | F | — |
| V-COINC-4 | Raíces que convergen y se separan | conductor que recorre `t_c` en la configuración crítica (`+/+`) y en la de radio `40 + 2⁻³⁰` | cada salida designa la misma raíz bruta a ambos lados; ninguna salida cambia de raíz en la raíz doble; en mordedura, el hueco es no existencia certificada | F | — |
| V-COINC-5 | Desaparición temporal | radio `40 + 2⁻³⁰` y modelo a excCyl 3,7, con el conductor atravesando el hueco | salidas indefinidas en el hueco y la misma regla a ambos lados; con salidas etiquetadas, extremos cerrados certificados; con la segunda sin etiqueta, su extremo no se certifica y queda dentro de la banda | F | — |
| V-COINC-6 | Conductor que empieza sin intersección | las dos variantes creadas, y guardadas y reabiertas, con el conductor en el hueco | la regla se captura sin muestra válida; certificado igual al de V-COINC-1 o V-COINC-2 | F | — |
| V-COINC-7 | Corte frente a algoritmo vivo | 201 conductores de `|t − t_c| ≤ 10⁻⁸` en las dos variantes | con salidas etiquetadas, definidas en todas las muestras del vivo y del corte; con la segunda sin etiqueta, toda diferencia de definición entre el vivo y el corte cae dentro de la banda `UNRESOLVED`; el certificado no afirma nada en que difieran | A | — |
| V-COINC-8 | Guardar, reabrir, deshacer, rehacer y recalcular | las dos variantes; `updateConstruction` y recarga del XML | el estado de selección y de etiquetado se vuelve a leer en cada operación; si no cambia, certificado idéntico bit a bit; si cambia, revisión nueva con diagnóstico, sin reasociación | F | — |
| V-COINC-9 | Selección certificada frente a resultado operativo | V-COINC-1 a V-COINC-8 | en toda muestra fuera de las bandas `UNRESOLVED`, la salida operativa del vivo y del corte es la raíz que designa la selección certificada (`Q` la de `λ` menor, `R` la mayor), comparada con la referencia de precisión ampliada; el valor límite certificado de la raíz coincidente se registra como evidencia y nunca sustituye a una salida indefinida | F | — |

### 3.5d Componentes puntuales (C2)

Amplían V-CRIT-5. Una componente puntual no es una raíz doble interior: V-CRIT-1 sigue siendo un
parámetro singular interior de una componente periódica cerrada.

| Id | Requisito | Construcción | Aserción | Tipo | Fijación |
|---|---|---|---|---|---|
| V-POINT-1 | Parámetro único | estructura construida en prueba; tangencia exterior sintética de §1 | entidad propia, nunca un intervalo de longitud cero: envolvente de `t*` que contiene `atan2(−4, −3)`, fuente estructural (operación, predicado y raíz), inclusión cerrada, maximal con no existencia certificada a ambos lados | F | — |
| V-POINT-2 | Dos componentes separadas por una región inválida | dos componentes puntuales sintéticas de §1 | dos componentes puntuales, una en la costura, con fuentes distintas y separadas por no existencia certificada; ninguna se une a la otra a través de la costura ni se trata como arco | A | — |
| V-POINT-3 | Transición entre punto y arco | tangencia exterior con radio `40 − 2⁻³⁰` → `40` → `40 + 2⁻³⁰` → `40`, deshaciendo y rehaciendo cada paso | recubrimiento vacío, componente puntual, arco entre dos raíces simples y otra vez componente puntual; transiciones de linaje tipadas de aparición y de cambio de dimensión con relación explícita; ninguna reasociación por etiqueta, índice, coordenadas o proximidad | F | — |
| V-POINT-4 | Punto geométrico no evaluable | V-POINT-1 con el evaluador en los binary64 más próximos a `t*` | ningún binary64 alcanza `t*`; la componente existe; su posición es la envolvente certificada de la forma límite, que contiene `(15, 20)`, o, con un fallo forzado de esa forma, «no disponible» sin que cambie la existencia; los puntos del evaluador en parámetros con no existencia certificada no se publican como válidos | F | — |
| V-POINT-5 | Métrica, pertenencia y representación | V-POINT-1 y V-POINT-2 | longitud total exactamente 0; una ruta solo dentro de la componente y de longitud 0; `Point(L, u)` con `u` binary64 próximo a `t*`: fallo explícito con causa tipada; dibujo: nada dibujado y fidelidad parcial con causa; DXF: ninguna entidad de trazo y anexo que la declara; LaTeX: ningún camino y comentario que la declara; inspector: envolvente, fuente y estado de la posición | F | — |
| V-POINT-6 | Completitud con solo existencia puntual | V-POINT-1 y V-POINT-2 | `COMPLETE`: el recubrimiento son las componentes puntuales y la no existencia certificada; las intersecciones no declaran `EMPTY` ni un conjunto completo sin excluir rigurosamente las posiciones; un locus dependiente con ese soporte declara la limitación y no es `COMPLETE`; las semejanzas heredan las componentes | F | — |

### 3.6 Consumidores (A2.5)

| Id | Requisito | Construcción | Aserción | Tipo | Fijación |
|---|---|---|---|---|---|
| V-CON-1 | Autoridad única de cierre | los siete loci del modelo en penetración, mordedura y binary64 contiguos al valor crítico | dibujo, exportación, rutas métricas, costura de intersección e interacción toman la misma decisión de cierre que el certificado | F | — |
| V-CON-2 | Rutas en un arco que cruza la costura | dos posiciones del arco de mordedura a ambos lados de `±π` | la ruta sigue el arco; `full-cycle` y `wrap-to-start` se rechazan con diagnóstico tipado | F | — |
| V-CON-3 | Métrica total | primera familia en mordedura y en la configuración crítica sintética | longitud del arco o del ciclo con la clase que le corresponda (§25) | F | — |
| V-CON-4 | Intersecciones | primera familia en mordedura frente a una recta que corta el arco | marco de fase del arco; sin cuarentena de costura en un arco; `EMPTY` solo con `COMPLETE`; tokens estables cuando las fronteras se mueven | F | — |
| V-CON-5 | Puntos semánticos | `Point(L, u)` con `u` en el arco, en la costura y en una banda `UNRESOLVED` | definido, definido y sin resolver con causa tipada; el arrastre no cruza huecos | F | — |
| V-CON-6 | Composición anidada | locus con soporte locus sobre un arco de mordedura | hereda el arco; su completitud no supera la del origen | F | — |
| V-CON-7 | Semejanzas | `Translate`, `Rotate`, `Mirror` y `Dilate` de un locus en mordedura | la imagen conserva cobertura, maximalidad, completitud, linaje transportado y cierre | F | — |
| V-CON-8 | `resolveOpenBoundary` | caso con lista certificada distinta de la completa | se compara la componente correcta (hoy índice frente a tamaño) | F | — |
| V-CON-9 | Selector de rama principal | locus `NOT_ESTABLISHED` con una componente local | opera sobre la componente certificada | F | — |
| V-CON-10 | Ocurrencia generadora | cadena `o_1 → w_1 → d_2 → e_2` | lee la estructura de existencia; `w_1`, `d_2` y `e_2` siguen definidos | R | — |

### 3.7 Representación y teselación (B06, B07)

| Id | Requisito | Construcción | Aserción | Tipo | Fijación |
|---|---|---|---|---|---|
| V-B06-1 | Giro acotado | `o`, `v`, `w`, `a_1`, `e_1` y `o_1` a los parámetros del autor; la primera familia en mordedura | todo giro fuera de una banda de pliegue certificado y de un vértice singular certificado es ≤ `θ_max` | F | `pins.B06` |
| V-B06-2 | Exención mínima | `e_1`; configuración crítica sintética | la exención del giro se limita a las bandas de los dos pliegues de `e_1` y al vértice singular | F | `pins.B06` |
| V-B06-3 | Determinismo e invariancia | todas las anteriores, dos veces y con zoom y DPI distintos | vértices idénticos con la misma vista; fronteras, cierre y número de subtrazos idénticos con cualquier vista | F | — |
| V-B07-1 | Dibujo hasta la frontera | excCyl 3,7, 3,85 y 3,9 | un trazo abierto que cruza la costura; los vértices extremos distan ≤ 0,75 px de la imagen analítica de la frontera (hoy 6 a 24 px de hueco) | F | `pins.B07` |
| V-B07-2 | Reparametrización de raíz cuadrada | los mismos casos | el último tramo cumple la desviación de cuerda con el número de vértices de un tramo regular | F | — |
| V-B07-3 | Ninguna unión falsa (F-CU2-2) | excCyl 3,657, 3,66 y `0x1.d416bb67a38bcp1` | ningún segmento de dibujo cruza el hueco real | A | `pins.B07` |
| V-B07-4 | Evidencia exploratoria visible (E-1) | locus fuera de la clase v3 (V-LVL-1) | si se aprueba E-1: dibujada con su estilo solo dentro de la banda y nunca unida a una componente; si no: no se dibuja | F | — |
| V-REP-1 | Extremos de evento | `e_2` | el trazo termina en el límite lateral `w_1(t_N⁻) = L` con desviación ≤ 0,75 px | F | `pins.B10.2` |
| V-REP-2 | Sin retorno a muestras uniformes | fallo de evaluación forzado dentro de una componente certificada | ningún camino de 32 muestras uniformes; el punto se marca como fallo computacional | F | — |
| V-REP-3 | Vértice singular | configuración crítica sintética | vértice exacto en `t_c` calculado con la raíz doble; los tramos laterales no se suavizan; el giro medido coincide con el del certificado | F | — |

### 3.7b Existencia, evaluación y representación (P5)

| Id | Requisito | Construcción | Aserción | Tipo | Fijación |
|---|---|---|---|---|---|
| V-AVAIL-1 | Punto del evaluador en una no existencia certificada | `0x1.d416bb67a38bcp1` con el conductor en `t_c` | el punto no se publica como válido: `DEPENDENCY_UNDEFINED`, causa «no existencia certificada» y diagnóstico de desacuerdo | F | — |
| V-AVAIL-2 | Fallo puntual de `LocusLength` | política de prueba que falla en un conductor de `e_2` | `EVALUATION_FAILED` en ese punto; la existencia no cambia; el trazo se corta con marca y fidelidad parcial | F | — |
| V-AVAIL-3 | Fallo de un límite lateral | política de prueba que falla en la longitud total | V-E2-9; el anexo DXF y los comentarios LaTeX declaran el extremo y la exportación es incompleta | F | — |
| V-AVAIL-4 | Frontera certificada no evaluable | una frontera de V-TR-1 donde el evaluador no define el punto | el vértice se calcula con la forma límite (`λ = −b′/a`); si no la hay, el trazo termina en el último vértice evaluado con marca | F | — |
| V-AVAIL-5 | Presupuesto de representación agotado | presupuesto de vértices de prueba muy pequeño | engrosamiento determinista sin cruzar fronteras; si ni así cabe, fallo explícito de la componente con diagnóstico | A | — |
| V-AVAIL-6 | Resultados parciales | documento con una componente representable y otra que falla | se representa la primera; la segunda se enumera con su causa; las coordenadas conservan su propia clase de garantía | F | — |

### 3.8 Exportaciones (A2.6)

| Id | Requisito | Construcción | Aserción | Tipo | Fijación |
|---|---|---|---|---|---|
| V-DXF-1 | Topología DXF | modelo en penetración, en mordedura y `e_2`; configuración crítica sintética | un `LWPOLYLINE` por componente certificada, en el orden del arco; marca de cierre solo en componentes periódicas cerradas; los extremos abiertos se exportan (hoy `MISSING_DOMAIN`); el parámetro singular es un vértice exacto | F | — |
| V-DXF-2 | Locus `NOT_ESTABLISHED` | locus fuera de la clase v3 | fidelidad incompleta con anexo obligatorio y estricta en cada componente declarada | F | — |
| V-DXF-3 | Residuo de cierre | componentes periódicas cerradas del modelo | el residuo se mide contra el certificado de cierre (hoy idénticamente 0) | F | — |
| V-DXF-4 | Comprobación externa (informativa) | las exportaciones de V-DXF-1 | AutoCAD Core Console abre los archivos y `AUDIT` no informa errores | B | — |
| V-TEX-1 | Topología LaTeX | PGF, PSTricks y Asymptote de los mismos casos | `cycle`, `\pspolygon` o `--cycle` solo en componentes periódicas cerradas; `e_2` abierto; los documentos compilan con `-halt-on-error` | F | — |
| V-TEX-2 | Especificación durable (D-CU2-11) | — | si se adopta, las pruebas citan la especificación nueva y no el registro de validación | F | — |

### 3.9 Determinismo, invariancia y regresión

| Id | Aserción | Tipo |
|---|---|---|
| V-DET-1 | Todas las filas anteriores, ejecutadas dos veces, dan certificados idénticos bit a bit (componentes, corchetes, evidencia exacta, claves de linaje, eventos) | F |
| V-INV-1 | Zoom, DPI y tamaño de vista no cambian ningún certificado | F |
| V-INV-2 | Con traslaciones de `10³` y escalas de `10⁻³` a `10³` de toda la construcción, la estructura (componentes, fuentes de frontera, completitud) es la misma y los corchetes contienen la misma referencia. Con `10⁶` se admite el fallo cerrado (`UNRESOLVED`), nunca una estructura certificada distinta (`L-CU1-TRANSLATION-RESOLUTION`); el certificado exacto clasifica las constantes trasladadas tal como se capturan | F |
| V-CU0-1 | Todas las secciones `preserve` de la línea base de `CU0` se reproducen dentro de su tolerancia | R |
| V-CU0-2 | Las fijaciones `pins.B07`, `pins.H01`, `pins.B06` y `pins.B10.2` quedan invertidas con el procedimiento de la [estrategia](../architecture/pre_g9b_cu2_implementation_strategy.md) §5 y un registro de nueva línea base; `B10.1`, `H03` y `H10_H11` conservan la inversión de `CU1`; `B08_B09`, `H02` y `H06` (de `CU3`) no cambian | R |
| V-CU1-1 | Las pruebas de aceptación de `CU1` y sus puertas G1–G7 se mantienen; ninguna estimación de clase 3 aparece como entrada rigurosa de un certificado | R |
| V-CU0-3 | Las pruebas existentes que fijan el certificado de compatibilidad cambian solo como conformidad declarada | R |
| V-PROD-1 | `final.shared` y `final.desktop` pasan sin pruebas debilitadas | R |
| V-PROD-2 | Las pruebas de exportación DXF y LaTeX existentes pasan. El escaneo de autoridad de `verify-g9x1-extended-dxf.ps1`, que ya falla en la base por `AppGeoCeDG`, no empeora | R |

## 4. Verificación

Clase propuesta `INTEGRATED_PHASE` (decisión D-CU2-12). El candidato congelado se verifica con:

1. el registro de la selección `pre-g9b-cu2.desktop` (y de una selección compartida si la
   implementación añade pruebas compartidas) y de la fase `PRE-G9B-CU2` con el mecanismo
   oficial, con el inventario actualizado con evidencia ejecutada;
2. `verify.ps1 -Profile STATIC`;
3. `verify.ps1 -Profile INFRA_UNIT`, porque cambian el registro y el inventario;
4. `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU2`;
5. `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0`, con las fijaciones invertidas, y
   `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU1`;
6. `verify.ps1 -Profile INTEGRATION`, con `-LogDirectory artifacts\agent\<nuevo>`;
7. los bancos `certification` y `tessellation` antes y después, con contadores (GC-7
   informativa).

`FINAL` no es exigible para la clase y queda para `CU10`.

## 5. Lo que estas pruebas no pueden demostrar

- La certificación de construcciones fuera de la clase v3, o de predicados que no son de primer
  nivel con raíces múltiples: fallan cerradas, y la matriz solo comprueba que fallan así.
- La criticidad exacta de una configuración cuya tangencia se calcula con redondeo: se
  certifica la configuración redondeada que se captura.
- La continuidad, a través de una raíz coincidente, de una construcción que depende de una
  segunda salida sin etiqueta: la matriz solo comprueba que falla cerrada (D-CU2-18).
- La representación gráfica de las componentes puntuales, que en `CU2` solo se declara
  (D-CU2-19).
- La existencia de un pliegue sin simetría estructural.
- La exactitud de las coordenadas de `e_2`, que son estimaciones de clase 3 (D-CU1-2).
- El rendimiento en otras máquinas: GC-7 y el tiempo de GC-8 son informativos.
- Los contactos tangentes y los puntos estructurales, que pertenecen a `CU3`.
