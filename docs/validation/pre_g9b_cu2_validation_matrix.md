# PRE-G9B-CU2 — Matriz de validación y criterios de aceptación

| Campo | Valor |
|---|---|
| Carácter | Matriz candidata de validación de la implementación de `CU2`. No es evidencia de ejecución |
| Estado | Matriz de aceptación de la implementación autorizada el 2026-10-21 ([registro](pre_g9b_cu2_author_architecture_approval_record.md)); los casos críticos y sintéticos exactos son pruebas obligatorias; `selfApproved=false` |
| Normas | [propuesta normativa](../architecture/pre_g9b_cu2_normative_proposal.md); [ADR 0038 (ACCEPTED, 2026-10-21)](../adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md); [estrategia](../architecture/pre_g9b_cu2_implementation_strategy.md) |
| Diseños | [certificación](../architecture/pre_g9b_cu2_hybrid_certification_design.md); [evento de la longitud orientada](../architecture/pre_g9b_cu2_oriented_length_event_design.md); [consumo y representación](../architecture/pre_g9b_cu2_consumption_and_representation.md); [análisis del modelo del autor](../architecture/pre_g9b_cu2_author_model_certification_analysis.md) |
| Decisiones aplicadas | C1-1 (puertas por contadores) y C1-4 (`preserve` frente a `pins`) del [registro de cierre de `CU0`](pre_g9b_cu0_closeout_record.md); D-CU1-2 (contrato de estimación) del [registro de cierre de `CU1`](pre_g9b_cu1_closeout_record.md) |
| Revisión | 2026-10-20, revisión focalizada P1–P5: filas V-CRIT (sustituyen a V-TR-3), V-FOLD (sustituyen a V-JET-1), V-SEL, V-AVAIL, V-UNIF, V-COMPL, V-E2-8, V-E2-9 y V-LIN-6; V-LIN-3 y V-LIN-4 revisadas; puerta GC-8. Cierre de coherencia C1–C3 (2026-10-20): §3.5c nuevo (V-COINC-1 a V-COINC-9, amplía V-SEL-3), §3.5d nuevo (V-POINT-1 a V-POINT-6, amplía V-CRIT-5), V-REV-EXACT-1, V-SEL-3 y V-CRIT-5 revisadas, referencias de §1 y límites de §5. Aprobación autoral (2026-10-21, paso T0): V-REV-EXACT-1 ampliada a V-REV-EXACT-1 a V-REV-EXACT-10 según D-CU2-20-R. Descendiente correctivo T13 (2026-10-22, decisiones D-CU2-T13-R1, D-CU2-T13-R2 y D-CU2-T13-R4): §6 nuevo, trazabilidad de cada fila con las pruebas que la citan y su [revisión sustantiva](pre_g9b_cu2_traceability_review.md); ninguna fila cambia. Decisión D-CU2-T13-R7 (2026-10-23): precisión de V-INV-2 (B(i)) y sus pruebas ([registro de R7](pre_g9b_cu2_r7_correction_record.md)) |

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
| V-REV-EXACT-4 | Comparación exacta de entradas autoritativas | cortes con valores de elementos independientes, literales escritos con 17 cifras, literales sin texto original (fijados por programa), entre ellos angulares, que el XML escribe en grados, y argumentos literales en línea | la validación compara por bits cada entrada autoritativa con su copia; las restauraciones admitidas devuelven la fidelidad; una discrepancia no restaurable da `NOT_ESTABLISHED` con causa `RECONSTRUCTION_FIDELITY_NOT_ESTABLISHED`, sin certificado y con evaluaciones `EVALUATION_FAILED`; nunca una evaluación silenciosa de otra configuración | A | — |
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
| V-INV-2 | Con traslaciones de `10³` y escalas de `10⁻³` a `10³` de toda la construcción, la estructura (componentes, fuentes de frontera, completitud) es la misma y los corchetes contienen la misma referencia. Con `10⁶` se admite el fallo cerrado (`UNRESOLVED`), nunca una estructura certificada distinta (`L-CU1-TRANSLATION-RESOLUTION`); el certificado exacto clasifica las constantes trasladadas tal como se capturan. *Precisión D-CU2-T13-R7 (B(i)):* las transformaciones en las unidades de la matriz son casos obligatorios, y con todas las constantes múltiplos de 125 complementarios, nunca sustitutos; vale para las salidas etiquetadas y para la segunda salida sin etiqueta. La estructura se conserva donde está rigurosamente establecida: siempre que las constantes binary64 capturadas conserven los tipos de raíz del discriminante sin transformar. Una relación geométrica ideal que la captura no conserva (una raíz doble que se separa) es efecto de la captura, documentado, no un cambio de estructura. Donde la definición efectiva heredada cambia con la magnitud absoluta, o la garantía no puede demostrarse, se admite el fallo cerrado tipado (`UNRESOLVED` con su causa), con evidencia que lo justifique y nunca como salida por defecto; nunca una estructura certificada distinta ni incorrecta. Un fallo exclusivamente computacional del evaluador sobre una existencia certificada es `EVALUATION_FAILED` con causa `COMPUTATIONAL_FAILURE`, no un cambio de estructura | F |
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

## 6. Trazabilidad con las pruebas efectivas

Sección añadida por el descendiente correctivo T13 (decisiones autorales D-CU2-T13-R1,
D-CU2-T13-R2 y D-CU2-T13-R4 de 2026-10-22). Las filas de §2 y §3 no cambian. Cada fila se
relaciona con los métodos de prueba cuyo Javadoc la cita y, marcadas «(clase)», con las clases
que la citan solo en su Javadoc de clase, en un método auxiliar o en un comentario interno; los
rangos «V-X-1 a V-X-n» se expanden. Las clases `PreG9BCu1*` se excluyen porque citan las filas
homónimas de la [matriz de `CU1`](pre_g9b_cu1_validation_matrix.md) (V-DET-1, V-INV-1, V-INV-2,
V-CU0-1 a V-CU0-3 y V-PROD-1, con otro significado allí): la auditoría de T13 las contó como
cubiertas por esas citas, y D-CU2-T13-R4 añadió las pruebas propias de V-DET-1, V-INV-1 y V-INV-2.

Una cita no demuestra el cumplimiento de la fila. La columna «Revisión» resume la
[revisión sustantiva](pre_g9b_cu2_traceability_review.md) de las filas de tipo F, A y de las
puertas GC: «efectiva» (las pruebas que la citan aseveran cada elemento esencial), «cubierta por
pruebas que no la citan» (las nombra la revisión), «residuo menor», «hueco confirmado» (ninguna
prueba asevera un elemento esencial; tratamiento pendiente de la decisión D-CU2-T13-R5, con el
[inventario de los 34 huecos](pre_g9b_cu2_r5_gap_inventory.md)) y las filas de este descendiente,
revisadas aparte (§6 de la revisión). De sus hallazgos, D-CU2-T13-R6 corrigió H-T13-A
([registro de R6](pre_g9b_cu2_r6_correction_record.md)), y D-CU2-T13-R7 corrigió H-T13-B y
clasificó H-T13-C ([registro de R7](pre_g9b_cu2_r7_correction_record.md)). Las filas de tipo R y B
indican su vía de verificación. La ejecución de cada método consta en el inventario JUnit ejecutado
y en el registro de verificación del candidato.

El mandato autoral de autonomía correctiva controlada de 2026-10-23
([registro de la corrección](pre_g9b_cu2_corrective_autonomy_record.md)) trata las 34 filas de hueco
confirmado: «efectiva (mandato correctivo)» cuando las pruebas añadidas aseveran cada elemento
esencial, con la corrección de producto o la decisión de nivel C pendiente que se indica cuando la
fila depende de una de ellas, y «residuo» cuando un elemento queda sin aseverar. Las citas se
regeneraron con el mismo método y se unieron a las anteriores, que conservan las asociaciones
añadidas a mano; solo se retiró el nombre anterior de una prueba renombrada.

| Fila | Tipo | Revisión | Pruebas que la citan |
|---|---|---|---|
| GC-1 | GC | efectiva (mandato correctivo) | `PreG9BCu2AuthorModelCertificationTest.driverMovesPublishNoRevisionAndComputeNoCertificate`, `PreG9BCu2SelectionSettlementTest.driverMovesAndQueriesDoNotRebuildSettleOrRecertify`, `G9U0CommandSurfaceTest` (clase), `G9X1LocusV2ExportTest` (clase), `PreG9BCu2CertificationInstrumentationTest` (clase) |
| GC-2 | GC | efectiva (mandato correctivo) para los certificados propios; la regla de los anidados, decisión C-4 | `PreG9BCu2AuthorModelCertificationTest.aNonDriverChangeComputesOneCertificatePerAffectedLocus`, `PreG9BCu2HybridCertificationTest.theShadowCertificateIsComputedOncePerRevision`, `PreG9BCu2CertificationInstrumentationTest` (clase) |
| GC-3 | GC | efectiva (mandato correctivo) | `PreG9BCu2AuthorModelCertificationTest.consumersReadThePublishedCertificateOnly`, `PreG9BCu2HybridCertificationTest.theShadowCertificateIsComputedOncePerRevision`, `PreG9BCu2SelectionSettlementTest.driverMovesAndQueriesDoNotRebuildSettleOrRecertify`, `PreG9BCu2CertificationInstrumentationTest` (clase) |
| GC-4 | GC | residuo menor | `PreG9BCu2AuthorModelCertificationTest.theAuthorParametersAreCertifiedFromStructuralFactsOnly`, `PreG9BCu2CertificationInstrumentationTest` (clase), `PreG9BCu2HybridCertificationTest` (clase) |
| GC-5 | GC | cubierta por pruebas que no la citan | `PreG9BCu2CertificationInstrumentationTest` (clase) |
| GC-6 | GC | efectiva | `PreG9BCu2AuthorModelCertificationTest.aFailingLengthEstimateLeavesTheCertificateUnchanged`, `PreG9BCu2AuthorModelCertificationTest.theAuthorParametersAreCertifiedFromStructuralFactsOnly`, `PreG9BCu2MetricStructureTest.anOriginWrapCutsTheDependentLocusIntoOneOpenArc`, `PreG9BCu2CertificationInstrumentationTest` (clase) |
| GC-7 | GC | informativa (bancos) | `certification`, `tessellation` |
| GC-8 | GC | efectiva (mandato correctivo) | `PreG9BCu2AuthorModelCertificationTest.constructionDegeneraciesAreBandsThatNoComponentCrosses`, `PreG9BCu2AuthorModelCertificationTest.contiguousBinary64ValuesAreClassifiedExactlyAndDeterministically`, `PreG9BCu2AuthorModelCertificationTest.theAuthorParametersAreCertifiedFromStructuralFactsOnly`, `PreG9BCu2HybridCertificationTest` (clase) |
| V-LVL-1 | F | efectiva (mandato correctivo) | `PreG9BCu2HybridCertificationTest.exploratoryEvidenceHasNoAuthorityOverTheDefinition`, `PreG9BCu2HybridCertificationTest.outOfClassLociHaveOnlyExploratoryEvidence`, `PreG9BCu2RepresentationTest.exploratoryEvidenceIsDrawnApartAndOnlyInsideItsBands` |
| V-LVL-2 | F | efectiva (mandato correctivo) | `PreG9BCu2ExistenceStructureV2Test.exploratoryEvidenceNeverCertifiesAndIsOutsideTheSnapshot`, `PreG9BCu2HybridCertificationTest.exploratoryEvidenceIsNeitherPersistedNorARevisionSource` |
| V-LVL-3 | F | cubierta por pruebas que no la citan | `PreG9BCu2HybridCertificationTest.anUnlabelledSecondOutputLeavesLocalComponentsAroundABand` |
| V-COMPL-1 | A | efectiva | `PreG9BCu2AuthorModelCertificationTest.anExhaustedBudgetIsNeverComplete`, `PreG9BCu2HybridCertificationTest.anExhaustedBudgetIsNeverComplete` |
| V-CMP-1 | F | efectiva | `PreG9BCu2MetricStructureTest.anOriginWrapCutsTheDependentLocusIntoOneOpenArc` |
| V-ARC-1 | F | efectiva | `PreG9BCu2AuthorModelCertificationTest.everyBiteIsOneArcAcrossTheSeamWithTheAnalyticEnds` |
| V-ARC-2 | F | efectiva | `PreG9BCu2AuthorModelCertificationTest.everyBiteIsOneArcAcrossTheSeamWithTheAnalyticEnds`, `PreG9BCu2ExistenceStructureV2Test.anArcCrossingTheSeamIsOneComponentWithUniqueCoordinates` |
| V-UND-1 | F | efectiva (mandato correctivo): `UNRESOLVED` tipado, nunca inexistencia; el nombre de la causa, decisión C-2 | `PreG9BCu2HybridCertificationTest.aLocusOnALengthThatFailsIsNeverCertifiedNonexistent`, `PreG9BCu2MetricStructureTest.anUnestablishedJumpOrStructureLeavesTheDependentUnresolved` |
| V-UNIF-1 | A | cubierta por pruebas que no la citan | `PreG9BCu2ClassV3ModelTest.equalityPredicatesAreInvalidOnlyByStructuralIdentity` |
| V-V3-1 | F | efectiva | `PreG9BCu2ClassV3ModelTest.newCapabilitiesEncloseTheExtendedPrecisionReference` |
| V-V3-2 | F | efectiva | `PreG9BCu2ClassV3ModelTest.everyInvalidReasonIsTypedAndOnlyItCertifiesNonexistence` |
| V-V3-3 | F | residuo menor | `PreG9BCu2ClassV3ModelTest.outOfClassShapesHaveNoProgram`, `PreG9BCu2HybridCertificationTest.outOfClassLociHaveOnlyExploratoryEvidence` |
| V-V3-4 | F | efectiva | `PreG9BCu2AuthorModelClassV3Test.everyModelSliceHasAClassV3ProgramWithItsAlgorithmRules`, `PreG9BCu2ClassV3ModelTest.theStepLimitIsVersionedAfterMacroExpansion`, `PreG9BCu2HybridCertificationTest.theStepLimitRefusalIsTyped` |
| V-TR-1 | F | efectiva | `PreG9BCu2AuthorModelCertificationTest.everyBiteIsOneArcAcrossTheSeamWithTheAnalyticEnds` |
| V-TR-2 | F | efectiva | `PreG9BCu2HybridCertificationTest.endInclusionFollowsThePredicate` |
| V-TR-4 | R | vía de verificación | `PreG9BCu2AuthorModelCertificationTest.pointwiseValidityBeyondTheExactEndIsNotExistence`, `PreG9BCu0LocusReferenceTest` (clase), `PreG9BCu0References` (clase) |
| V-TOT-1 | F | efectiva | `PreG9BCu2HybridCertificationTest.structuralTotalityDecidesWithoutAnyBox` |
| V-PER-1 | F | residuo menor | `PreG9BCu2AuthorModelCertificationTest.theAuthorParametersAreCertifiedFromStructuralFactsOnly`, `PreG9BCu2HybridCertificationTest.theCriticalConfigurationIsOneClosedComponentWithASingularParameter` |
| V-PER-2 | A | efectiva | `PreG9BCu2HybridCertificationTest.outOfClassLociHaveOnlyExploratoryEvidence` |
| V-PER-3 | A | efectiva (mandato correctivo) | `PreG9BCu2AuthorModelCertificationTest.constructionDegeneraciesAreBandsThatNoComponentCrosses` |
| V-CRIT-1 | F | efectiva | `PreG9BCu2HybridCertificationTest.theCriticalConfigurationIsOneClosedComponentWithASingularParameter`, `CircleDriverRootStructure2DTest` (clase) |
| V-CRIT-2 | F | efectiva | `PreG9BCu2HybridCertificationTest.theNeighboursOfTheCriticalRadiusArePenetrationAndBite`, `CircleDriverRootStructure2DTest` (clase) |
| V-CRIT-3 | A | efectiva | `PreG9BCu2HybridCertificationTest.exactCertificatesSeparateArbitrarilyCloseRoots`, `CircleDriverRootStructure2DTest` (clase) |
| V-CRIT-4 | F | efectiva | `PreG9BCu2HybridCertificationTest.theCriticalConfigurationIsOneClosedComponentWithASingularParameter`, `CircleDriverRootStructure2DTest` (clase) |
| V-CRIT-5 | F | efectiva | `PreG9BCu2AuthorModelCertificationTest.everyBiteIsOneArcAcrossTheSeamWithTheAnalyticEnds`, `PreG9BCu2HybridCertificationTest.anExternalTangencyIsAPointComponent`, `CircleDriverRootStructure2DTest` (clase) |
| V-CRIT-6 | F | cubierta por pruebas que no la citan | `PreG9BCu2HybridCertificationTest` (clase) |
| V-CRIT-7 | F | cubierta por pruebas que no la citan | `PreG9BCu2AuthorModelCertificationTest.contiguousBinary64ValuesAreClassifiedExactlyAndDeterministically` |
| V-CRIT-8 | F | cubierta por pruebas que no la citan | `PreG9BCu2AuthorModelCertificationTest.undoAndRedoRestoreTheCertificates` |
| V-CRIT-9 | F | efectiva | `PreG9BCu2AuthorModelCertificationTest.nonDriverParametersMoveTheCriticalValue` |
| V-CRIT-10 | F | cubierta por pruebas que no la citan | `PreG9BCu2AuthorModelCertificationTest.contiguousBinary64ValuesAreClassifiedExactlyAndDeterministically`, `PreG9BCu2HybridCertificationTest.certificatesAreDeterministic`, `CircleDriverRootStructure2DTest` (clase) |
| V-FOLD-1 | F | efectiva | `PreG9BCu2AuthorModelCertificationTest.theAuthorParametersAreCertifiedFromStructuralFactsOnly`, `PreG9BCu2FoldCertificationTest.aSymmetricLocusFoldsAtTheFixedPoints`, `PreG9BCu2FoldCertificationTest.theSymmetryIsVerifiedOnTheCapturedConstants` |
| V-FOLD-2 | A | residuo menor | `PreG9BCu2AuthorModelCertificationTest.theFoldsOfE1RestOnTheCapturedConstants`, `PreG9BCu2FoldCertificationTest.theSymmetryIsVerifiedOnTheCapturedConstants` |
| V-FOLD-3 | A | cubierta por pruebas que no la citan | `PreG9BCu2AuthorModelCertificationTest.theAuthorParametersAreCertifiedFromStructuralFactsOnly`, `PreG9BCu2FoldCertificationTest.aClosedTurnWithoutSymmetryIsNotAFold` |
| V-FOLD-4 | A | residuo menor | `PreG9BCu2FoldCertificationTest.aDegenerateStationaryPointIsNotAFold` |
| V-E2-1 | F | efectiva | `PreG9BCu2AuthorModelCertificationTest.theAuthorParametersAreCertifiedFromStructuralFactsOnly`, `PreG9BCu2MetricStructureTest.anOriginWrapCutsTheDependentLocusIntoOneOpenArc` |
| V-E2-2 | R | vía de verificación | `PreG9BCu0CompositionCharacterizationTest`, `PreG9BCu0LocusReferenceTest`, `preserve` |
| V-E2-3 | F | cubierta por pruebas que no la citan | `PreG9BCu2AuthorModelExportTest.eachCertifiedComponentIsOnePolylineClosedOnlyWhenPeriodic`, `PreG9BCu2AuthorModelExportTest.latexClosesOnlyClosedPeriodicComponents`, `PreG9BCu2AuthorModelConsumerTest` (clase) |
| V-E2-4 | F | efectiva | `PreG9BCu2AuthorModelCertificationTest.theAuthorParametersAreCertifiedFromStructuralFactsOnly`, `PreG9BCu2MetricStructureTest.anOriginWrapCutsTheDependentLocusIntoOneOpenArc` |
| V-E2-5 | F | efectiva (mandato correctivo) | `PreG9BCu2AuthorModelCertificationTest.aFailingLengthEstimateLeavesTheCertificateUnchanged`, `PreG9BCu2AuthorModelExportTest.anUnevaluatedEndOfE2IsDeclaredAndNeverExtrapolated`, `PreG9BCu2AuthorModelRepresentationTest.aFailingLengthCutsTheStrokeOfE2` |
| V-E2-6 | F | efectiva | `PreG9BCu2MetricStructureTest.aLengthBetweenFixedPositionsIsNeverACertificateInput`, `PreG9BCu2MetricStructureTest.aLocallyCertifiedSourceKeepsItsBandUnresolved`, `PreG9BCu2MetricStructureTest.anIntervalSourceDefinesTheRouteFromTheOriginToItsEnd`, `PreG9BCu2MetricStructureTest.anOpenSourceArcDefinesTheRouteAcrossTheSeamToItsFarEnd`, `PreG9BCu2MetricStructureTest.anOriginAtTheSeamCutsAtTheSeam`, `PreG9BCu2MetricStructureTest.anOriginThatFollowsTheDriverHasNoStructure`, `PreG9BCu2MetricStructureTest.anUnestablishedJumpOrStructureLeavesTheDependentUnresolved`, `PreG9BCu2MetricStructureTest.theIncludedEndFollowsDirectionAndSamePosition` |
| V-E2-7 | F | efectiva | `PreG9BCu2AuthorModelLineageTest.theEventOfE2KeepsItsLineageWhenTheOriginMoves` |
| V-E2-8 | F | residuo menor | `PreG9BCu2AuthorModelConsumerTest.theExcludedEndOfE2IsItsOneSidedLimit`, `PreG9BCu2MetricStructureTest.theIncludedEndFollowsDirectionAndSamePosition` |
| V-E2-9 | F | residuo menor | `PreG9BCu2AuthorModelExportTest.anUnevaluatedEndOfE2IsDeclaredAndNeverExtrapolated` |
| V-PUB-1 | F | efectiva | `PreG9BCu2AuthorModelCertificationTest.theLociArePublishedWhileTheSavedDriverIsInTheGap`, `PreG9BCu2AuthorModelSelectionSettlementTest` (clase) |
| V-PUB-2 | F | efectiva | `PreG9BCu2AuthorModelCertificationTest.everyRevisionPublishesItsOwnGeometry` |
| V-PUB-3 | F | efectiva | `PreG9BCu2AuthorModelCertificationTest.anEmptyCertifiedCoverIsPublishedComplete`, `PreG9BCu2SelectionSettlementTest.aGloballyEmptyDomainIsPublishedCompleteAfterADynamicChange` |
| V-PUB-4 | F | efectiva | `PreG9BCu2AuthorModelCertificationTest.anUndefinedDefinitionInputLeavesNoStaleDefinition` |
| V-REV-EXACT-1 | F | efectiva | `PreG9BCu2AuthorModelCertificationTest.twoRunsOfTheVariantsAlternatingTheDriverGiveIdenticalResults`, `PreG9BCu2AuthorModelSelectionSettlementTest.oneUlpChangesAtEveryDriverPositionReproduceTheSelection`, `PreG9BCu2AuthorModelSliceFidelityTest.everyStateWithTheDriverAwayFromTheDoubleRootMatchesTheModelSetFromScratch`, `PreG9BCu2AuthorModelSliceFidelityTest.oneUlpChangesOfExcCylPublishRevisionsWithExactSliceCopies`, `PreG9BCu2SliceFidelityCharacterizationTest.oneUlpOfAFreeNumberPublishesARevisionWithAnExactSliceCopy`, `PreG9BCu2HybridCertificationTest` (clase), `PreG9BCu2SelectionSettlementTest` (clase) |
| V-REV-EXACT-2 | F | efectiva | `PreG9BCu2AuthorModelCertificationTest.twoRunsOfTheVariantsAlternatingTheDriverGiveIdenticalResults`, `PreG9BCu2AuthorModelSliceFidelityTest.everyStateWithTheDriverAwayFromTheDoubleRootMatchesTheModelSetFromScratch`, `PreG9BCu2RevisionExactnessTest.drawingStateOfASliceElementPublishesNoRevision`, `PreG9BCu2RevisionExactnessTest.everyStateMatchesTheSameConstructionBuiltFromScratch`, `PreG9BCu2SliceFidelityCharacterizationTest.representationOnlyChangesKeepTheFingerprintAndTheRevision` |
| V-REV-EXACT-3 | F | efectiva (mandato correctivo) | `PreG9BCu2AuthorModelSliceFidelityTest.oneUlpOfExcCylReachesTheUnlabelledOutputExactly`, `PreG9BCu2RevisionExactnessTest.everyStateMatchesTheSameConstructionBuiltFromScratch`, `PreG9BCu2RevisionExactnessTest.oneUlpOfTheRadiusReachesTheUnlabelledOutputExactly`, `PreG9BCu2SliceFidelityCharacterizationTest.oneUlpOfAFreeNumberPublishesARevisionWithAnExactSliceCopy`, `PreG9BCu2HybridCertificationTest` (clase) |
| V-REV-EXACT-4 | A | efectiva | `PreG9BCu2AuthorModelSliceFidelityTest.everySliceOfTheCompleteModelIsReconstructedFaithfully`, `PreG9BCu2HybridCertificationTest.twoRunsOfTheSyntheticConstructionsGiveIdenticalResults`, `PreG9BCu2RevisionExactnessTest.admittedRestorationsReturnTheCopyToExactFidelity`, `PreG9BCu2RevisionExactnessTest.everyStateMatchesTheSameConstructionBuiltFromScratch`, `PreG9BCu2SliceFidelityCharacterizationTest.anUnreproducibleAngleLiteralFailsExplicitly` |
| V-REV-EXACT-5 | F | efectiva | `PreG9BCu2AuthorModelSliceFidelityTest.everyStateWithTheDriverAtTheDoubleRootMatchesTheModelSetFromScratch`, `PreG9BCu2AuthorModelSliceFidelityTest.everyStateWithTheDriverAwayFromTheDoubleRootMatchesTheModelSetFromScratch`, `PreG9BCu2RevisionExactnessTest.everyStateMatchesTheSameConstructionBuiltFromScratch` |
| V-REV-EXACT-6 | F | cubierta por pruebas que no la citan | `PreG9BCu2SliceFidelityCharacterizationTest.driverOnlyMovesKeepTheRevisionAndTheSlice` |
| V-REV-EXACT-7 | A | efectiva (mandato correctivo) para la selección; la causa del cambio de etiquetado, decisión C-1 | `PreG9BCu2RevisionExactnessTest.sliceRebuildsRecordTheirTypedCause`, `PreG9BCu2SelectionSettlementTest.aRealSelectionTransitionPublishesARevisionWithItsTypedCause`, `PreG9BCu2SelectionSettlementTest.theVariantsCreatedAtTheDoubleRootKeepTheirComponents`, `PreG9BCu2HybridCertificationTest` (clase) |
| V-REV-EXACT-8 | F | efectiva (mandato correctivo) | `PreG9BCu2AuthorModelCertificationTest.twoRunsOfTheVariantsAlternatingTheDriverGiveIdenticalResults`, `PreG9BCu2HybridCertificationTest.twoRunsOfTheSyntheticConstructionsGiveIdenticalResults`, `PreG9BCu2RevisionExactnessTest.twoRunsGiveIdenticalFingerprintsAndCounters` |
| V-REV-EXACT-9 | F | efectiva (mandato correctivo; corrección H-CU2A-5) | `PreG9BCu2RevisionExactnessTest.aLostAngleLiteralIsNeverAssociatedWithThePreviousCertificate`, `PreG9BCu2RevisionExactnessTest.aReopenedDocumentIsReconstructedFaithfullyInItsReloadedState`, `PreG9BCu2RevisionExactnessTest.drawingStateOfASliceElementPublishesNoRevision`, `PreG9BCu2RevisionExactnessTest.undoAndRedoRestoreSnapshotsWithTheSameContent` |
| V-REV-EXACT-10 | A | efectiva (mandato correctivo) | `PreG9BCu2RevisionExactnessTest.negativeExtremesAndANestedChainLeaveNoStaleState`, `PreG9BCu2RevisionExactnessTest.propagationAndExtremeValuesKeepTheFingerprintExact` |
| V-LIN-1 | F | efectiva | `PreG9BCu2AuthorModelLineageTest.movingEndsKeepTheLineageAndTheBiteIsATypedTransition`, `PreG9BCu2LineageTest.theLineageFollowsStructuralSourcesNotEnds` |
| V-LIN-2 | F | efectiva | `PreG9BCu2AuthorModelLineageTest.movingEndsKeepTheLineageAndTheBiteIsATypedTransition`, `PreG9BCu2LineageTest.theLineageFollowsStructuralSourcesNotEnds` |
| V-LIN-3 | R | vía de verificación | `PreG9BCu2AuthorModelLineageTest.theSelectorOfN1ResolvesByEqualityOfTheDecomposition` |
| V-LIN-4 | A | efectiva (mandato correctivo; corrección H-CU2A-1); «interseca», decisión C-7 | `PreG9BCu2LineageTest.anAmbiguousSelectorLeavesThePointUndefinedWithATypedCause`, `PreG9BCu2LineageTest.selectorsResolveOnlyThroughDemonstratedCorrespondence` |
| V-LIN-5 | R | vía de verificación | `PreG9BCu2AuthorModelLineageTest.theFirstCertificatesAfterOpeningGuessNoLineage`, `PreG9BCu2LineageTest.theLineageFollowsStructuralSourcesNotEnds` |
| V-LIN-6 | A | efectiva | `PreG9BCu2AuthorModelLineageTest.containmentCreatesNoCorrespondence`, `PreG9BCu2LineageTest.anAmbiguousSelectorLeavesThePointUndefinedWithATypedCause`, `PreG9BCu2LineageTest.selectorsResolveOnlyThroughDemonstratedCorrespondence` |
| V-SEL-1 | F | cubierta por pruebas que no la citan | `PreG9BCu2AuthorModelClassV3Test.everyModelSliceHasAClassV3ProgramWithItsAlgorithmRules`, `PreG9BCu2AuthorModelClassV3Test.theProgramDoesNotDependOnTheDriverPosition`, `PreG9BCu2ClassV3ModelTest.theSelectionRuleIsReadFromTheAlgorithm`, `PreG9BCu2SelectionSettlementTest.aCopyRebuiltInAGapIsSettledToTheLiveState`, `PreG9BCu2AuthorModelSelectionSettlementTest` (clase) |
| V-SEL-2 | F | cubierta por pruebas que no la citan | `PreG9BCu2AuthorModelClassV3Test.theRuleIsCapturedWithoutAnyValidSample` |
| V-SEL-3 | F | cubierta por pruebas que no la citan | `PreG9BCu2AuthorModelClassV3Test.atTheTangencyBothLabelledOutputsCoincide` |
| V-SEL-4 | F | cubierta por pruebas que no la citan | `PreG9BCu2AuthorModelClassV3Test.penetrationAndBiteKeepTheSelectionRule` |
| V-SEL-5 | F | cubierta por pruebas que no la citan | `PreG9BCu2AuthorModelClassV3Test.theGapIsUniformlyInvalidWithTheSameRuleOnBothSides` |
| V-SEL-6 | A | residuo menor | `PreG9BCu2ClassV3ModelTest.anInheritedPermutationIsReproducedInAnyQueryOrder`, `PreG9BCu2ClassV3ModelTest.aSelectionTheCopyCannotReproduceFailsClosed`, `PreG9BCu2SelectionSettlementTest.aCopyInItsFirstInitializationDepartsAtOneParameterInAnyQueryOrder`, `PreG9BCu2SelectionSettlementTest.aLivePermutationOneZeroIsReadAgainOnUndoRedoAndReopen`, `PreG9BCu2SelectionSettlementTest.aLivePermutationThatSettlementCannotProduceFailsClosed` |
| V-SEL-7 | F | efectiva (mandato correctivo) | `PreG9BCu2AuthorModelSelectionSettlementTest.theSelectionStateIsReadAgainOnUndoRedoAndReopen`, `PreG9BCu2ClassV3ModelTest.selectionAndLabelStatesAreReadAgainOnReopenUndoRedoAndRecompute`, `PreG9BCu2RevisionExactnessTest.drawingStateOfASliceElementPublishesNoRevision`, `PreG9BCu2SelectionSettlementTest.aLivePermutationOneZeroIsReadAgainOnUndoRedoAndReopen`, `PreG9BCu2SelectionSettlementTest.theSettledStateIsReadAgainOnReopenUndoAndRedo` |
| V-COINC-1 | F | cubierta por pruebas que no la citan | `PreG9BCu2AuthorModelClassV3Test.atTheTangencyBothLabelledOutputsCoincide`, `PreG9BCu2ClassV3ModelTest.labelledOutputsAtTheCoincidentRootAreBothDefined`, `PreG9BCu2HybridCertificationTest.theCriticalConfigurationIsOneClosedComponentWithASingularParameter`, `PreG9BCu2SelectionSettlementTest.theOperativeOutputIsTheRootThatTheCertifiedSelectionDesignates`, `PreG9BCu2SelectionSettlementTest.variantsCreatedWithoutIntersectionCaptureTheSameCertificates` |
| V-COINC-2 | A | efectiva (mandato correctivo) | `PreG9BCu2AuthorModelSelectionSettlementTest.theUnlabelledSecondOutputKeepsItsTypedBands`, `PreG9BCu2ClassV3ModelTest.anUnlabelledSecondOutputHasATypedUnresolvedBand`, `PreG9BCu2HybridCertificationTest.anUnlabelledSecondOutputLeavesLocalComponentsAroundABand`, `PreG9BCu2SelectionSettlementTest.theOperativeOutputIsTheRootThatTheCertifiedSelectionDesignates`, `PreG9BCu2SelectionSettlementTest.variantsCreatedWithoutIntersectionCaptureTheSameCertificates` |
| V-COINC-3 | F | efectiva (mandato correctivo) | `PreG9BCu2AuthorModelSelectionSettlementTest.theVariantsCreatedAtTheDoubleRootLeaveItWithATypedTransition`, `PreG9BCu2SelectionSettlementTest.aDoubleRootAtTheFirstInitializationKeepsItsRuleInAnyQueryOrder`, `PreG9BCu2SelectionSettlementTest.theOperativeOutputIsTheRootThatTheCertifiedSelectionDesignates`, `PreG9BCu2SelectionSettlementTest.theVariantsCreatedAtTheDoubleRootKeepTheirComponents` |
| V-COINC-4 | F | efectiva | `PreG9BCu2HybridCertificationTest.theNeighboursOfTheCriticalRadiusArePenetrationAndBite`, `PreG9BCu2SelectionSettlementTest.theOperativeOutputIsTheRootThatTheCertifiedSelectionDesignates` |
| V-COINC-5 | F | efectiva (mandato correctivo); la redacción de la fila, decisión C-6 | `PreG9BCu2AuthorModelSelectionSettlementTest.theUnlabelledSecondOutputKeepsItsTypedBands`, `PreG9BCu2HybridCertificationTest.anUnlabelledSecondOutputLeavesLocalComponentsAroundABand`, `PreG9BCu2HybridCertificationTest.theNeighboursOfTheCriticalRadiusArePenetrationAndBite`, `PreG9BCu2SelectionSettlementTest.theOperativeOutputIsTheRootThatTheCertifiedSelectionDesignates` |
| V-COINC-6 | F | descendiente T13 (§6 de la revisión) | `PreG9BCu2AuthorModelSelectionSettlementTest.theModelOutputsAreTheRootsThatTheCertifiedSelectionDesignates`, `PreG9BCu2AuthorModelSelectionSettlementTest.theModelVariantsStartedInTheGapCaptureTheSameCertificates`, `PreG9BCu2SelectionSettlementTest.theOperativeOutputIsTheRootThatTheCertifiedSelectionDesignates`, `PreG9BCu2SelectionSettlementTest.variantsCreatedWithoutIntersectionCaptureTheSameCertificates` |
| V-COINC-7 | A | efectiva (mandato correctivo); la redacción y el muestreo de la fila, decisión C-6 | `PreG9BCu2AuthorModelSelectionSettlementTest.theUnlabelledSecondOutputKeepsItsTypedBands`, `PreG9BCu2HybridCertificationTest.anUnlabelledSecondOutputLeavesLocalComponentsAroundABand`, `PreG9BCu2SelectionSettlementTest.theOperativeOutputIsTheRootThatTheCertifiedSelectionDesignates` |
| V-COINC-8 | F | efectiva (mandato correctivo) | `PreG9BCu2AuthorModelSelectionSettlementTest.theSelectionStateIsReadAgainOnUndoRedoAndReopen`, `PreG9BCu2ClassV3ModelTest.selectionAndLabelStatesAreReadAgainOnReopenUndoRedoAndRecompute`, `PreG9BCu2SelectionSettlementTest.theOperativeOutputIsTheRootThatTheCertifiedSelectionDesignates`, `PreG9BCu2SelectionSettlementTest.theSettledStateIsReadAgainOnReopenUndoAndRedo` |
| V-COINC-9 | F | descendiente T13 (§6 de la revisión) | `PreG9BCu2AuthorModelSelectionSettlementTest.theModelOutputsAreTheRootsThatTheCertifiedSelectionDesignates`, `PreG9BCu2SelectionSettlementTest.theOperativeOutputIsTheRootThatTheCertifiedSelectionDesignates` |
| V-POINT-1 | F | residuo menor | `PreG9BCu2ExistenceStructureV2Test.anIsolatedParameterIsAPointComponentAndNeverAZeroLengthInterval`, `PreG9BCu2HybridCertificationTest.anExternalTangencyIsAPointComponent`, `PreG9BCu2HybridCertificationTest.pointComponentsAreNeverExcludedOrPromotedByConsumers`, `CircleDriverRootStructure2DTest` (clase), `PreG9BCu2ConsumerMigrationTest` (clase), `PreG9BCu2RepresentationTest` (clase) |
| V-POINT-2 | A | efectiva | `PreG9BCu2HybridCertificationTest.pointComponentsAreNeverExcludedOrPromotedByConsumers`, `PreG9BCu2HybridCertificationTest.twoPointComponentsAreSeparatedOneAtTheSeam`, `CircleDriverRootStructure2DTest` (clase) |
| V-POINT-3 | F | efectiva | `PreG9BCu2ExistenceStructureV2Test.aChangeOfDimensionRelatesOnlyThroughUniqueStructuralSources`, `PreG9BCu2LineageTest.aPointComponentChangesDimensionThroughTypedTransitions`, `PreG9BCu2LineageTest.renamingTheTracedOutputChangesNoKeyAndNoRelation` |
| V-POINT-4 | F | residuo: el fallo forzado de la forma límite no tiene gancho de prueba (decisión C-9); los vecinos de `t*`, efectivos (mandato correctivo) | `PreG9BCu2HybridCertificationTest.anExternalTangencyIsAPointComponent`, `PreG9BCu2HybridCertificationTest.aSemanticPointNearTheTangencyIsUndefinedWithATypedCause` |
| V-POINT-5 | F | efectiva (mandato correctivo; corrección H-CU2A-1) | `PreG9BCu2ConsumerMigrationTest.pointComponentsAreDeclaredAndNeverAddressed`, `PreG9BCu2HybridCertificationTest.aSemanticPointNearTheTangencyIsUndefinedWithATypedCause`, `PreG9BCu2HybridCertificationTest.pointComponentsAreNeverExcludedOrPromotedByConsumers`, `PreG9BCu2RepresentationTest.aPointComponentIsDeclaredAndNeverDrawn`, `PreG9BCu2RepresentationTest.aPointComponentIsDeclaredInTheExport` |
| V-POINT-6 | F | efectiva (mandato correctivo; corrección H-CU2A-2) | `PreG9BCu2ConsumerMigrationTest.pointComponentsAreDeclaredAndNeverAddressed`, `PreG9BCu2HybridCertificationTest.anExternalTangencyIsAPointComponent`, `PreG9BCu2HybridCertificationTest.aSimilarityImageTransformsThePositionOfAPointComponent`, `PreG9BCu2HybridCertificationTest.pointComponentsAreNeverExcludedOrPromotedByConsumers`, `PreG9BCu2HybridCertificationTest.theTransformedEnclosureHoldsTheExactImage`, `PreG9BCu2HybridCertificationTest.twoPointComponentsAreSeparatedOneAtTheSeam` |
| V-CON-1 | F | efectiva (mandato correctivo; corrección H-CU2A-4) | `PreG9BCu2AuthorModelConsumerTest.theCertificateIsTheSingleClosureAuthority`, `PreG9BCu2AuthorModelExportTest.latexClosesOnlyClosedPeriodicComponents`, `PreG9BCu2ConsumerMigrationTest.theCertificateIsTheSingleClosureAuthority`, `PreG9BCu2HybridCertificationTest.theCanonicalParametersOfAnArcBelongToItUpToTheSeam` |
| V-CON-2 | F | residuo menor | `PreG9BCu2ConsumerMigrationTest.routesFollowAnArcAcrossTheSeam` |
| V-CON-3 | F | efectiva (mandato correctivo); la longitud de las mordeduras, no establecida (O-CU2A-3, decisión C-10) | `PreG9BCu2AuthorModelConsumerTest.theTotalLengthsOfTheFirstFamilyHaveTheirClass`, `PreG9BCu2ConsumerMigrationTest.theTotalLengthFollowsTheComponents`, `PreG9BCu2HybridCertificationTest.theIncludedEndsOfAnArcAcrossTheSeamAreEvaluatedAtTheirOwnParameter`, `PreG9BCu2HybridCertificationTest.theTotalLengthsOfTheCriticalLociMatchTheExactSweptArcs` |
| V-CON-4 | F | efectiva (mandato correctivo) | `PreG9BCu2AuthorModelConsumerTest.anIntersectionWithAnArcKeepsItsTokensThroughARevision`, `PreG9BCu2ConsumerMigrationTest.intersectionsUseTheArcAndItsStructuralLineage`, `PreG9BCu2HybridCertificationTest.theCanonicalParametersOfAnArcBelongToItUpToTheSeam` |
| V-CON-5 | F | cubierta por pruebas que no la citan | `PreG9BCu2ConsumerMigrationTest.positionsResolveOnTheArcAndCertifiedNonexistenceIsNeverValid` |
| V-CON-6 | F | efectiva | `PreG9BCu2ConsumerMigrationTest.aDeclaredDomainIsNeverPromotedWithoutATotalComposition`, `PreG9BCu2ConsumerMigrationTest.aDependentOnALocusSupportInheritsTheArc` |
| V-CON-7 | F | efectiva (mandato correctivo) | `PreG9BCu2ConsumerMigrationTest.aSimilarityImageComposesTheStructureAndTheOneSidedLimits`, `PreG9BCu2HybridCertificationTest.similarityImagesOfABiteKeepItsStructure` |
| V-CON-8 | F | efectiva | `PreG9BCu2ConsumerMigrationTest.anOpenBoundaryNeedsACompleteInventory` |
| V-CON-9 | F | efectiva | `PreG9BCu2ConsumerMigrationTest.thePrincipalSelectorUsesTheLocalCertifiedComponents` |
| V-CON-10 | R | vía de verificación | `PreG9BCu2AuthorModelConsumerTest.theGeneratingOccurrenceChainStaysDefined` |
| V-B06-1 | F | efectiva (mandato correctivo) | `PreG9BCu2AuthorModelRepresentationTest.turnsAreBoundedOutsideTheCertifiedFolds`, `PreG9BCu2RepresentationTest.theSingularParameterIsAnExactUnsmoothedVertex` |
| V-B06-2 | F | efectiva | `PreG9BCu2AuthorModelRepresentationTest.turnsAreBoundedOutsideTheCertifiedFolds`, `PreG9BCu2FoldCertificationTest.theStructurePublishesOnlyTheNonDegenerateFolds`, `PreG9BCu2RepresentationTest.theSingularParameterIsAnExactUnsmoothedVertex`, `PreG9BCu2AuthorModelCertificationTest` (clase) |
| V-B06-3 | F | efectiva (mandato correctivo) | `PreG9BCu2AuthorModelRepresentationTest.drawingsAreDeterministicAndTheirTopologyViewInvariant`, `PreG9BCu2RepresentationTest.drawingsAreDeterministicAndTheirTopologyViewInvariant` |
| V-B07-1 | F | efectiva | `PreG9BCu2AuthorModelRepresentationTest.aBiteIsDrawnToTheImagesOfItsCertifiedEnds`, `PreG9BCu0LocusReferenceTest` (clase) |
| V-B07-2 | F | efectiva (mandato correctivo; corrección H-CU2A-3) | `PreG9BCu2AuthorModelRepresentationTest.aBiteIsDrawnToTheImagesOfItsCertifiedEnds`, `PreG9BCu2RepresentationTest.theTransitionEndsAreReachedInTheSquareRootCoordinate` |
| V-B07-3 | A | efectiva | `PreG9BCu2AuthorModelRepresentationTest.noStrokeBridgesTheRealGapNearTheCriticalValue` |
| V-B07-4 | F | efectiva (mandato correctivo) | `PreG9BCu2RepresentationTest.exploratoryEvidenceIsDrawnApartAndOnlyInsideItsBands`, `PreG9BCu2RepresentationTest.exploratoryEvidenceIsDrawnDottedAndTranslucent` |
| V-REP-1 | F | efectiva | `PreG9BCu2AuthorModelRepresentationTest.theExcludedEndOfE2IsItsOneSidedLimit` |
| V-REP-2 | F | efectiva | `PreG9BCu2RepresentationTest.aComputationalFailureCutsTheStrokeWithAMark` |
| V-REP-3 | F | efectiva | `PreG9BCu2RepresentationTest.theSingularParameterIsAnExactUnsmoothedVertex`, `PreG9BCu2RepresentationTest.theSingularVertexIsTheDoubleRootOfTheTangency` |
| V-AVAIL-1 | F | efectiva | `PreG9BCu2AuthorModelConsumerTest.anEvaluatorPointInACertifiedGapIsNotPublished`, `PreG9BCu2ConsumerMigrationTest.positionsResolveOnTheArcAndCertifiedNonexistenceIsNeverValid` |
| V-AVAIL-2 | F | efectiva | `PreG9BCu2AuthorModelRepresentationTest.aFailingLengthCutsTheStrokeOfE2`, `PreG9BCu2RepresentationTest.aComputationalFailureCutsTheStrokeWithAMark` |
| V-AVAIL-3 | F | residuo menor | `PreG9BCu2AuthorModelExportTest.anUnevaluatedEndOfE2IsDeclaredAndNeverExtrapolated`, `PreG9BCu2RepresentationTest.anUnevaluatedOneSidedLimitIsDeclared` |
| V-AVAIL-4 | F | efectiva | `PreG9BCu2RepresentationTest.aClosedTransitionHasALimitFormImage`, `PreG9BCu2RepresentationTest.anUnevaluableEndWithoutLimitFormEndsAtTheLastEvaluatedVertex`, `PreG9BCu2RepresentationTest.theTransitionEndsAreReachedInTheSquareRootCoordinate` |
| V-AVAIL-5 | A | efectiva | `PreG9BCu2RepresentationTest.theVertexBudgetCoarsensWithoutCrossingBoundaries` |
| V-AVAIL-6 | F | efectiva con la redacción propuesta (mandato correctivo); decisión C-3 | `PreG9BCu2AuthorModelRepresentationTest.drawnVerticesAreTheEvaluationsOfTheirParameters`, `PreG9BCu2RepresentationTest.aComponentThatCannotBeDrawnIsListedWithItsCause` |
| V-DXF-1 | F | cubierta por pruebas que no la citan | `PreG9BCu2AuthorModelExportTest.eachCertifiedComponentIsOnePolylineClosedOnlyWhenPeriodic` |
| V-DXF-2 | F | efectiva | `PreG9BCu2RepresentationTest.aLocusWithoutEstablishedExistenceIsDeclaredWithTheMandatorySidecar`, `PreG9BR6PlusCBaseIdentityTest` (clase) |
| V-DXF-3 | F | residuo menor | `PreG9BCu2RepresentationTest.theClosureResidualIsMeasuredAgainstTheClosureCertificate` |
| V-DXF-4 | B | vía de verificación | comprobación externa informativa con AutoCAD (registro de verificación) |
| V-TEX-1 | F | efectiva (mandato correctivo); la orden en la fila y TeX en la integración continua, decisión C-5 | `PreG9BCu2AuthorModelExportTest.latexClosesOnlyClosedPeriodicComponents`, `PreG9BCu2AuthorModelExportTest.theAsymptoteDocumentCompiles`, `PreG9BCu2AuthorModelExportTest.theCriticalLociAreClosedInLatexOnlyWhenPeriodic`, `PreG9BCu2AuthorModelExportTest.thePgfDocumentCompiles`, `PreG9BCu2AuthorModelExportTest.thePstricksDocumentCompiles` |
| V-TEX-2 | F | efectiva | `PreG9BCu2AuthorModelExportTest.theLatexTestsCiteTheDurableSpecification`, `PreG9BR6PlusCLatexExportTest` (clase), `PreG9BR6PlusCSharedExportTest` (clase) |
| V-DET-1 | F | descendiente T13, abierta (§6 de la revisión) | `PreG9BCu2AuthorModelCertificationTest.twoRunsOfTheAuthorModelGiveIdenticalResults`, `PreG9BCu2HybridCertificationTest.twoRunsOfTheSyntheticConstructionsGiveIdenticalResults` |
| V-INV-1 | F | descendiente T13 (§6 de la revisión) | `PreG9BCu2AuthorModelRepresentationTest.zoomOutputResolutionAndViewSizeChangeNoCertificate` |
| V-INV-2 | F | descendiente T13; precisión D-CU2-T13-R7 (B(i)), H-T13-B corregido y H-T13-C clasificado ([registro de R7](pre_g9b_cu2_r7_correction_record.md)) | `PreG9BCu2HybridCertificationTest.aCertifiedRootThatTheInheritedComputationLosesIsAComputationalFailure`, `PreG9BCu2HybridCertificationTest.everyCertifiedSampleOfTheUnlabelledSecondOutputIsDefined`, `PreG9BCu2HybridCertificationTest.theExactCertificateClassifiesTheTransformedConstantsAsCaptured`, `PreG9BCu2HybridCertificationTest.theUnlabelledSecondOutputFailsClosedWhereTheInheritedComputationIsNotDecisive`, `PreG9BCu2HybridCertificationTest.theUnlabelledSecondOutputKeepsItsStructureUnderTheTransforms`, `PreG9BCu2HybridCertificationTest.transformationsOfOrderTenToTheSixFailClosedOrKeepTheStructure`, `PreG9BCu2HybridCertificationTest.translationsAndScalesOfTheWholeConstructionKeepTheStructure` |
| V-CU0-1 | R | vía de verificación | `PHASE PRE-G9B-CU0`, `preserve` |
| V-CU0-2 | R | vía de verificación | `PHASE PRE-G9B-CU0` |
| V-CU1-1 | R | vía de verificación | `PHASE PRE-G9B-CU1` |
| V-CU0-3 | R | vía de verificación | proceso: [registro de conformidades](pre_g9b_cu2_t11_test_conformity_register.md) |
| V-PROD-1 | R | vía de verificación | `final.desktop`, `final.shared`, `INTEGRATION` |
| V-PROD-2 | R | vía de verificación | `INTEGRATION`, `verify-g9x1-extended-dxf.ps1` |
