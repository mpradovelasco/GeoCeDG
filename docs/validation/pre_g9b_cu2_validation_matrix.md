# PRE-G9B-CU2 — Matriz de validación y criterios de aceptación

| Campo | Valor |
|---|---|
| Carácter | Matriz candidata de validación de la implementación de `CU2`. No es evidencia de ejecución |
| Estado | `CU2 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false` |
| Normas | [propuesta normativa](../architecture/pre_g9b_cu2_normative_proposal.md); [ADR 0038 (PROPOSED)](../adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md); [estrategia](../architecture/pre_g9b_cu2_implementation_strategy.md) |
| Diseños | [certificación](../architecture/pre_g9b_cu2_hybrid_certification_design.md); [evento de la longitud orientada](../architecture/pre_g9b_cu2_oriented_length_event_design.md); [consumo y representación](../architecture/pre_g9b_cu2_consumption_and_representation.md); [análisis del modelo del autor](../architecture/pre_g9b_cu2_author_model_certification_analysis.md) |
| Decisiones aplicadas | C1-1 (puertas por contadores) y C1-4 (`preserve` frente a `pins`) del [registro de cierre de `CU0`](pre_g9b_cu0_closeout_record.md); D-CU1-2 (contrato de estimación) del [registro de cierre de `CU1`](pre_g9b_cu1_closeout_record.md) |

## 1. Referencias analíticas

Todas las fronteras de la primera familia se comparan con la fórmula del
[análisis](../architecture/pre_g9b_cu2_author_model_certification_analysis.md) §3.2,
`t_c ± β`, con `t_c = π/2 − θp` y `β = arccos((rbCone − excCyl·cos θp)/rbCylinder)`,
evaluada con aritmética de precisión ampliada a partir de los parámetros del modelo. Nunca se
comparan con la validez puntual del evaluador, que excede la raíz exacta hasta 4,3·10⁻⁶
(hallazgo F-CU2-1).

| Dato | Valor de referencia (10 cifras) |
|---|---|
| `t_c` | 1,2767651295 |
| valor crítico de la mordedura (`β = 0`) | excCyl = 3,656943727 |
| entrada del punto guardado `N` en el hueco (H01) | excCyl = 3,958845384 |
| extremos analíticos a excCyl = 4,0 | 0,6024120274 y 1,9511182317 |
| parámetro guardado de `N` (`pins.H01`) | 0,6456498832 |

## 2. Puertas de aceptación por contadores

Se miden con contadores públicos, sin reflexión, como en `CU1`. Los tiempos son indicadores
complementarios y nunca sustituyen a estas puertas.

| Puerta | Criterio |
|---|---|
| GC-1 | Mover solo el conductor de cualquier locus del modelo, también dentro y fuera de un hueco, publica **0** revisiones y hace **0** cálculos de certificado |
| GC-2 | Cambiar una entrada no conductora (`excCyl`, `rbCone`, `v_1`, un punto libre) hace **como máximo 1** cálculo de certificado por locus afectado y revisión |
| GC-3 | El dibujo, la métrica, las intersecciones, los puntos semánticos y la exportación leen el mismo certificado: **0** cálculos de certificado adicionales por consulta de un consumidor |
| GC-4 | Las cajas de existencia por rama y revisión no superan el presupuesto versionado en ningún locus del modelo ni en ningún régimen del barrido de §3.2; el agotamiento solo se admite en la banda del valor crítico y produce `UNRESOLVED` tipado |
| GC-5 | Los vértices de dibujo por vista no superan el presupuesto versionado |
| GC-6 | Durante la certificación de `e_2` se leen **0** valores métricos: el certificado usa solo hechos estructurales |
| GC-7 (informativa) | Carga del modelo, arrastre con y sin `e_2` y dibujo, medidos con el banco antes y después |

## 3. Pruebas

Tipos: **F** focal, **A** adversaria, **R** regresión, **B** banco.

### 3.1 Niveles de evidencia y estructura del certificado

| Id | Requisito | Construcción | Aserción | Tipo | Fijación |
|---|---|---|---|---|---|
| V-LVL-1 | La evidencia exploratoria nunca se promueve | locus con un paso fuera de la clase v3 (intersección cónica–cónica) | `NOT_ESTABLISHED`; evidencia exploratoria presente; cero componentes certificadas; sin completitud ni exportación de la evidencia | F | — |
| V-LVL-2 | La evidencia exploratoria no se persiste ni avanza revisiones | el locus anterior; guardar y reabrir; cambiar solo la política exploratoria | misma huella de revisión; la evidencia se recalcula | F | — |
| V-LVL-3 | Admisibilidad local independiente de la completitud (ADR 0027) | locus con una componente local certificada y una banda `UNRESOLVED` | los consumidores locales aceptan la componente; los globales devuelven `NOT_ESTABLISHED` | F | — |
| V-CMP-1 | Continuidad de `F` como requisito | locus sintético que depende de una longitud orientada con origen fijo sobre una circunferencia | el salto es frontera de la componente, nunca un punto interior | F | — |
| V-ARC-1 | Arcos que cruzan la costura | primera familia en mordedura (excCyl 3,7) | **una** componente, arco de inicio canónico `t_c + β` y longitud `2π − 2β`, ambos extremos cerrados | F | `pins.B07` |
| V-ARC-2 | Pertenencia sobre el círculo | el arco anterior | `±π` pertenecen al arco; las direcciones se canonicalizan módulo `2π` | F | — |
| V-UND-1 | Indefinición computacional | dependencia cuyo cálculo falla (política de prueba) antes de certificar la existencia | `UNRESOLVED` con causa tipada, nunca `CERTIFIED_DOES_NOT_EXIST` | F | — |

### 3.2 Clase v3, transiciones, totalidad y periodicidad

| Id | Requisito | Construcción | Aserción | Tipo | Fijación |
|---|---|---|---|---|---|
| V-V3-1 | Envolventes de las capacidades nuevas | rayos, segmentos, circunferencia por centro y punto, recta–circunferencia con cada regla de raíz, pasos de varias salidas, operaciones vectoriales y macros expandidas | en cajas aleatorias deterministas, la envolvente de valor y derivada contiene la referencia de precisión ampliada | F | — |
| V-V3-2 | Resultado de caja tipado | un caso por causa: `DISCRIMINANT_NEGATIVE`, `RAY_PARAMETER_NEGATIVE`, `SEGMENT_PARAMETER_OUTSIDE`, `ZERO_DIRECTION`, `ZERO_RADIUS`, `DIVISOR_ZERO` | `UNIFORMLY_INVALID` con la causa esperada; solo esa salida da `CERTIFIED_DOES_NOT_EXIST` | F | — |
| V-V3-3 | Regla de raíz capturada | `{Q, R}` del modelo; la misma construcción en modo continuo | la regla se captura una vez por revisión y es constante en cada caja; el modo continuo queda fuera de la clase (`NOT_ESTABLISHED` con evidencia exploratoria) | F | — |
| V-V3-4 | Límite de pasos | corte de `e_2` con las macros expandidas; corte sintético que excede el límite | el primero cabe; el segundo da `NOT_ESTABLISHED` con causa tipada | F | — |
| V-TR-1 | Transiciones certificadas | primera familia con excCyl ∈ {3,66; 3,7; 3,85; 3,9; 3,95; 3,96; 4,0; 4,5; 5,0} | dos fronteras por locus; cada corchete contiene la referencia analítica de §1 y su anchura no supera `δ`; extremos cerrados | F | `pins.B07` |
| V-TR-2 | Inclusión de extremos según el predicado | rayo (`λ = 0`), segmento, recta–circunferencia (`D = 0`) y un divisor que se anula (dos rectas que se vuelven paralelas) | cerrado, cerrado, cerrado y abierto, respectivamente | F | — |
| V-TR-3 | Raíz doble y raíces muy próximas | excCyl en el valor crítico (el binary64 más próximo a `(rbCone − rbCylinder)/cos θp`, fijado por prueba); excCyl = 3,657, con raíces separadas por `2β ≈ 0,017` | valor crítico: `NOT_ESTABLISHED` con una banda `UNRESOLVED` que contiene `t_c`. 3,657: dos transiciones certificadas que contienen las referencias, o `NOT_ESTABLISHED` con una banda que contiene las dos. En ningún caso una componente que atraviese el hueco | A | — |
| V-TR-4 | La validez puntual no es frontera | los casos de V-TR-1 | `preserve.branch_ends` se reproduce sin cambio (mide el evaluador, que `CU2` no cambia) y no coincide con la frontera certificada; la diferencia queda dentro del exceso medido | R | — |
| V-TOT-1 | Totalidad estructural | locus de punto medio y de traslación sobre una circunferencia | `COMPLETE` sin cálculo de cajas (contador 0) | F | — |
| V-PER-1 | Periodicidad estructural | primera familia a excCyl 2,9; `e_1`; `o_1` | `COMPLETE`, una componente periódica cerrada; la continuidad en la costura se certifica con cajas que la envuelven | F | `pins.B07` |
| V-PER-2 | La periodicidad del conductor no basta | locus con un paso con historia (modo continuo) o con un valor aleatorio sobre un conductor circular | ninguna identificación periódica; no se declara componente cerrada | A | — |
| V-PER-3 | Puntos aislados de la construcción | `rbCone` = 8 y 10 | en los conductores donde dos rectas coinciden: frontera abierta certificada o banda `UNRESOLVED`; ninguna componente las atraviesa | A | — |
| V-JET-1 | Pliegues certificados | `e_1`; el giro cerrado de `w` (velocidad 0,067 en t = 1,5999) | `e_1`: puntos estacionarios aislados certificados en `t = 0` y `t = ±π`; `w`: ningún pliegue (la envolvente de `F′` excluye el cero) | F | — |

### 3.3 Evento de la longitud orientada y `e_2`

| Id | Requisito | Construcción | Aserción | Tipo | Fijación |
|---|---|---|---|---|---|
| V-E2-1 | `e_2` como una componente abierta | modelo del autor | `COMPLETE`, **una** componente, arco `[t_N, t_N + 2π)` con `t_N = 0` exacto, cerrada en el origen (`zero-length`) y abierta en el otro extremo; no periódica | F | `pins.B10.2` |
| V-E2-2 | Valores idénticos | modelo del autor | `preserve.e_2`, `preserve.e_2_seam_distance` y `preserve.oriented_length_profile` se reproducen dentro de su tolerancia; ningún valor de `LocusLength` cambia | R | — |
| V-E2-3 | Sin segmento de cierre | dibujo, DXF y LaTeX de `e_2` | un trazo abierto; ningún segmento une los extremos (hoy 13,19 unidades); `LWPOLYLINE` sin marca de cierre; trayectoria sin `cycle` | F | `pins.B10.2` |
| V-E2-4 | Evento exacto | `w_1` | un evento `ORIGIN_WRAP` con `driverParameter` igual a la dirección canónica de `N_1`, dirección `FORWARD`, lado `ZERO_LENGTH`, `jumpPositive = CERTIFIED` y `jumpValue` con la clase de la longitud total de `o_1` (`ESTIMATED`) | F | — |
| V-E2-5 | Certificado independiente de la clase de la longitud | forzar con una política de prueba el fallo de la estimación de `w_1` en un conductor | la estructura de existencia es idéntica; solo ese punto pasa a estado de evaluación `NOT_ESTABLISHED`; GC-6 | F | — |
| V-E2-6 | Casos del evento | sentido atrás; origen en la costura; origen que depende del conductor; fuente solo localmente certificada; fuente degenerada con salto no certificado; ruta explícita entre posiciones fijas | los resultados de la tabla de §4.3 del [diseño del evento](../architecture/pre_g9b_cu2_oriented_length_event_design.md) | F | — |
| V-E2-7 | Identidad del evento | arrastrar el conductor; cambiar `v_1` | arrastrar no cambia revisión, evento ni componente; cambiar `v_1` publica revisión nueva con el mismo linaje y fronteras nuevas | F | — |

### 3.4 Publicación (H01)

| Id | Requisito | Construcción | Aserción | Tipo | Fijación |
|---|---|---|---|---|---|
| V-PUB-1 | Publicación independiente del punto trazado | excCyl 4,0 con el conductor guardado (dentro del hueco) | los cuatro loci se publican `COMPLETE` con un arco cuyos corchetes contienen los extremos analíticos de §1; el punto trazado está indefinido y el locus no | F | `pins.H01` |
| V-PUB-2 | Ninguna definición obsoleta (F-CU2-3) | excCyl 3,95 → 3,96 → 4,0 → 4,5 → 5,0 con el conductor dentro del hueco | cada revisión publica su propia geometría, igual bit a bit a la de una construcción recién creada con el mismo estado | F | — |
| V-PUB-3 | Recubrimiento vacío | configuración con `D < 0` en todo el círculo, fijada por prueba | según D-CU2-8: publicado, válido, `COMPLETE`, cero componentes; intersecciones `EMPTY` completas; longitud total cero | F | — |
| V-PUB-4 | Entradas de definición indefinidas | un punto libre del corte indefinido | locus `UNDEFINED`, sin definición anterior visible | F | — |

### 3.5 Linaje y migración (E-3)

| Id | Requisito | Construcción | Aserción | Tipo | Fijación |
|---|---|---|---|---|---|
| V-LIN-1 | Linaje estable con fronteras móviles | excCyl 3,7 → 3,85 → 3,9 | la clave de linaje no cambia; solo cambian las fronteras | F | — |
| V-LIN-2 | Transición registrada | excCyl 2,9 → 3,7 → 2,9 | círculo a arco y vuelta, con transiciones tipadas y relación explícita; ninguna reasociación por geometría | F | — |
| V-LIN-3 | Migración de selectores guardados | `N_1` con el selector `text2` que cita la componente `[−π, π)` de `o_1` | resolución por contención única; reescritura perezosa al guardar; `preserve.state` sin cambio | R | — |
| V-LIN-4 | Selector ambiguo | documento sintético con un selector que interseca dos componentes certificadas | selector sin resolver y punto indefinido con causa tipada; ninguna reasociación silenciosa | A | — |
| V-LIN-5 | Archivos antiguos | documentos guardados antes de `CU2` | certificados recalculados al cargar; ninguna componente ni identidad adivinada (ADR 0027, decisión 9) | R | — |

### 3.6 Consumidores (A2.5)

| Id | Requisito | Construcción | Aserción | Tipo | Fijación |
|---|---|---|---|---|---|
| V-CON-1 | Autoridad única de cierre | los siete loci del modelo en penetración, mordedura y valor crítico | dibujo, exportación, rutas métricas, costura de intersección e interacción toman la misma decisión de cierre que el certificado | F | — |
| V-CON-2 | Rutas en un arco que cruza la costura | dos posiciones del arco de mordedura a ambos lados de `±π` | la ruta sigue el arco; `full-cycle` y `wrap-to-start` se rechazan con diagnóstico tipado | F | — |
| V-CON-3 | Métrica total | primera familia en mordedura y en el valor crítico | longitud del arco con la clase que le corresponda (§25); en el valor crítico, `NOT_ESTABLISHED` | F | — |
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
| V-B06-1 | Giro acotado | `o`, `v`, `w`, `a_1`, `e_1` y `o_1` a los parámetros del autor; la primera familia en mordedura | todo giro fuera de una banda de pliegue certificado es ≤ `θ_max` | F | `pins.B06` |
| V-B06-2 | Exención mínima | `e_1` | la exención del criterio de giro se limita a las bandas de sus dos pliegues certificados | F | `pins.B06` |
| V-B06-3 | Determinismo e invariancia | todas las anteriores, dos veces y con zoom y DPI distintos | vértices idénticos con la misma vista; fronteras, cierre y número de subtrazos idénticos con cualquier vista | F | — |
| V-B07-1 | Dibujo hasta la frontera | excCyl 3,7, 3,85 y 3,9 | un trazo abierto que cruza la costura; los vértices extremos distan ≤ 0,75 px de la imagen analítica de la frontera (hoy 6 a 24 px de hueco) | F | `pins.B07` |
| V-B07-2 | Reparametrización de raíz cuadrada | los mismos casos | el último tramo cumple la desviación de cuerda con el número de vértices de un tramo regular | F | — |
| V-B07-3 | Ninguna unión falsa (F-CU2-2) | excCyl 3,657 y 3,66 | ningún segmento de dibujo cruza el hueco real ni una banda `UNRESOLVED` | A | `pins.B07` |
| V-B07-4 | Evidencia exploratoria visible (E-1) | V-TR-3 | si se aprueba E-1: dibujada con su estilo solo dentro de la banda y nunca unida a una componente; si no: no se dibuja | F | — |
| V-REP-1 | Extremos de evento | `e_2` | el trazo termina en el límite lateral `w_1(t_N⁻) = L` con desviación ≤ 0,75 px | F | `pins.B10.2` |
| V-REP-2 | Sin retorno a muestras uniformes | fallo de evaluación forzado dentro de una componente certificada | ningún camino de 32 muestras uniformes; el punto se marca como fallo computacional | F | — |

### 3.8 Exportaciones (A2.6)

| Id | Requisito | Construcción | Aserción | Tipo | Fijación |
|---|---|---|---|---|---|
| V-DXF-1 | Topología DXF | modelo en penetración, en mordedura y `e_2` | un `LWPOLYLINE` por componente certificada, en el orden del arco; marca de cierre solo en componentes periódicas cerradas; los extremos abiertos se exportan (hoy `MISSING_DOMAIN`) | F | — |
| V-DXF-2 | Locus `NOT_ESTABLISHED` | valor crítico | fidelidad incompleta con anexo obligatorio y estricta en cada componente declarada | F | — |
| V-DXF-3 | Residuo de cierre | componentes periódicas cerradas del modelo | el residuo se mide contra el certificado de cierre (hoy idénticamente 0) | F | — |
| V-DXF-4 | Comprobación externa (informativa) | las exportaciones de V-DXF-1 | AutoCAD Core Console abre los archivos y `AUDIT` no informa errores | B | — |
| V-TEX-1 | Topología LaTeX | PGF, PSTricks y Asymptote de los mismos casos | `cycle`, `\pspolygon` o `--cycle` solo en componentes periódicas cerradas; `e_2` abierto; los documentos compilan con `-halt-on-error` | F | — |
| V-TEX-2 | Especificación durable (D-CU2-11) | — | si se adopta, las pruebas citan la especificación nueva y no el registro de validación | F | — |

### 3.9 Determinismo, invariancia y regresión

| Id | Aserción | Tipo |
|---|---|---|
| V-DET-1 | Todas las filas anteriores, ejecutadas dos veces, dan certificados idénticos bit a bit (componentes, corchetes, claves de linaje, eventos) | F |
| V-INV-1 | Zoom, DPI y tamaño de vista no cambian ningún certificado | F |
| V-INV-2 | Con traslaciones de `10³` y escalas de `10⁻³` a `10³` de toda la construcción, la estructura (componentes, fuentes de frontera, completitud) es la misma y los corchetes contienen la misma referencia. Con `10⁶` se admite el fallo cerrado (`UNRESOLVED`), nunca una estructura certificada distinta (`L-CU1-TRANSLATION-RESOLUTION`) | F |
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

- La certificación de construcciones fuera de la clase v3: fallan cerradas, y la matriz solo
  comprueba que fallan así.
- La exactitud de las coordenadas de `e_2`, que son estimaciones de clase 3 (D-CU1-2).
- Una componente en el valor crítico exacto: por diseño queda `NOT_ESTABLISHED`.
- El rendimiento en otras máquinas: GC-7 es informativa.
- Los contactos tangentes y los puntos estructurales, que pertenecen a `CU3`.
