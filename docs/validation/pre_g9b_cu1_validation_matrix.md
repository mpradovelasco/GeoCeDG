# PRE-G9B-CU1 — Matriz de validación y criterios de aceptación

| Campo | Valor |
|---|---|
| Carácter | Matriz candidata de validación de la implementación de `CU1`. No es evidencia de ejecución |
| Estado | `CU1 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false` |
| Normas | [propuesta normativa revisada](../architecture/pre_g9b_cu1_normative_proposal.md); [estrategia](../architecture/pre_g9b_cu1_correction_strategy.md) |
| Decisiones aplicadas | C1-1 (contadores deterministas), C1-3 (`INTEGRATED_PHASE`), C1-4 (`preserve` frente a `pins`) del [registro de cierre de `CU0`](pre_g9b_cu0_closeout_record.md) |

**Revisión de 2026-10-20 (implementación de `CU1`).** Por las precisiones N1–N5 de la
autorización de implementación:

- V-INV-1 se limita al régimen relativo y se añade V-INV-1b (N4);
- V-ADV-1 deja de exigir que el error real quede dentro del estimado (N5);
- se añaden V-TRANS-1 (N1) y V-CERT-RIGOR (N5).

Las puertas G1–G8 no cambian.

## 1. Puertas de aceptación por contadores (C1-1)

Se miden con la instrumentación pública de S1, sin reflexión, sobre el modelo del autor y
sobre las construcciones sintéticas de la matriz. Los tiempos son indicadores
complementarios y nunca sustituyen a estas puertas.

| Puerta | Criterio |
|---|---|
| G1 | Mover solo el conductor de `o_1`, de `e_2` o de un locus sintético publica **0** revisiones y hace **0** construcciones de corte |
| G2 | Cambiar una entrada no conductora (`excCyl`, `rbCone`, un radio, un punto libre o una función) publica **exactamente 1** revisión por locus afectado y hace **como máximo 1** construcción o sincronización de corte |
| G3 | Con una revisión exterior fija, N evaluaciones puntuales de `e_2` hacen **0** construcciones de corte adicionales, exteriores o anidadas, y **0** revisiones y **0** estados métricos anidados nuevos después de la primera consulta |
| G4 | Las evaluaciones anidadas por consulta puntual de `e_2` están acotadas por el trabajo local de la consulta entre posiciones. El valor exacto lo fija el banco y lo aprueba el autor (objetivo candidato ≤ 256, frente a 31 000–55 000 hoy) |
| G5 | Como máximo **1** construcción de estado métrico por clave completa (identidad, revisión, política, componente), fallos incluidos |
| G6 | La construcción del índice de `o_1` usa **≤ 25 %** del presupuesto de evaluaciones por construcción (H04) |
| G7 | Las circunferencias de radio 8, 20, `10³` y `10⁻³` y el resto de componentes del corpus no agotan el presupuesto (H11) |
| G8 (informativa) | Arrastre y dibujo con `e_2` frente a sin `e_2`, y carga del documento, medidos con el banco antes y después |

## 2. Pruebas

Tipos: **F** focal, **A** adversaria, **R** regresión, **B** banco.

### 2.1 Revisiones, cortes y composición anidada

| Id | Requisito | Construcción | Aserción | Tipo | Fijación |
|---|---|---|---|---|---|
| V-REV-1 | Revisión estable (A1.1-R) | `o_1`; locus sintético de punto medio sobre circunferencia | mover solo el conductor deja la revisión, el evaluador y los estados métricos intactos | F | `pins.H03` |
| V-REV-2 | No obsolescencia | el mismo locus; un cambio por cada clase de entrada no conductora: deslizador, punto libre, radio, función redefinida, texto, booleano y lista | revisión nueva; las muestras y la longitud coinciden **bit a bit** con las de una construcción recién creada con los mismos comandos y valores | F | — |
| V-REV-3 | Entrada sin huella determinista | locus cuyo corte lee un valor aleatorio | cada recálculo publica revisión (no se reutiliza en falso) | F | — |
| V-SLICE-1 | Corte una vez por revisión | `e_2` | G3 | F | `pins.B10.1` |
| V-NEST-1 | Reutilización anidada y trabajo local | `e_2` en 24 parámetros, incluidos `±10⁻⁶` del origen | G3 y G4; valores iguales a los de `preserve.e_2` | F | `pins.B10.1` |
| V-NEST-2 | Contadores públicos coherentes | `e_2` | los contadores anidados agregados coinciden con la suma por nivel; no se usa reflexión | F | — |
| V-SESS-1 | Ciclos entre niveles | evaluador de prueba que reentra por la sesión hija | diagnóstico `CYCLE_REENTRY`, sin desbordamiento | F | — |
| V-SESS-2 | Presupuesto anidado | consulta exterior con presupuesto de prueba pequeño | fallo tipado con la dimensión agotada; ningún valor parcial | F | — |
| V-CACHE-1 | Invalidación correcta | cadena `o_1 → d_2 → e_2` con cambio de `excCyl` | todos los estados métricos de la revisión anterior se descartan; valores iguales a los de una construcción recién creada con el mismo estado | F | — |

### 2.2 Métrica

| Id | Requisito | Construcción | Aserción | Tipo | Fijación |
|---|---|---|---|---|---|
| V-MET-STRUCT-1 | Clase 1, semejanza del conductor (H11) | `Q = P + (0,0)`, `Rotate(P, α, O)` y `Dilate(P, k, O)` sobre circunferencias de radio 1, 7, 8, 20, `10³` y `10⁻³` | clase `STRUCTURAL`; el intervalo de redondeo contiene `2π·λ·r`; ningún presupuesto agotado | F | `pins.H10_H11` (circunferencias) |
| V-MET-STRUCT-2 | Clase 1, imagen afín | loci afines con conductor segmento | clase `STRUCTURAL`; `abs(a)·Δu` | F | — |
| V-MET-STRUCT-3 | Clase 1, imagen por semejanza | `Translate`, `Rotate`, `Mirror` y `Dilate` de un locus fuente | clase de la fuente; valor `λ·L_fuente` | F | — |
| V-MET-ENCL-1 | Clase 2, programa v1/v2 | locus de pies de perpendicular (recta por dos puntos, perpendicular e intersección de rectas sobre conductor circunferencia) y construcciones con puntos medios | la envolvente contiene la referencia independiente (cuadratura de alta precisión del integrando exacto); el valor está dentro; si la anchura no supera `τ`, clase 2; si la supera, clase 3 con la envolvente adjunta | F | — |
| V-MET-ENCL-2 | Clase 2 en SplineV2 | SplineV2 de grados 3 y 7 | la estimación de Simpson queda dentro de la envolvente; la clase cumple 25.4 | F | — |
| V-MET-EST-1 | Clase 3, aliasing caracterizado (H10) | `Q = (x(P), sin(2πk·x(P)))`, `k = 1, 2, 4` | clase `ESTIMATED`; el valor menos la referencia, en valor absoluto, no supera el error declarado, y este no supera `τ` | F | `pins.H10_H11` (seno) |
| V-MET-EST-2 | Coherencia del error (H12) | circunferencias por la vía de solo evaluador, forzada con una política de prueba; elipse construida; `o_1` | error real ≤ error declarado; error declarado ≤ `τ` cuando el valor es admisible | F | — |
| V-MET-EST-3 | Longitud de `o_1` | modelo del autor | diferencia con `preserve.o_1_total_length_independent` ≤ `10⁻⁸`; con `preserve.o_1_total_length`, ≤ `10⁻⁶` | R | — |
| V-ADM-1 | Adaptador escalar | resultados de cada clase, y de clase 3 con error mayor que `τ` (política de prueba) | escalar definido si y solo si es admisible según 25.4; mismo valor que el resultado rico | F | — |
| V-DAG-1 | Uso como entrada | cadena `o_1 → d_2 → e_2` | la entrada métrica de `d_2` es de clase 3; `e_2` está definido; la longitud de `e_2` es como mucho `ESTIMATED_ERROR` | F | — |
| V-BUD-1 | Fallo memorizado | presupuesto de prueba pequeño | `NOT_ESTABLISHED` con la dimensión tipada en el resultado; un segundo recálculo con la misma clave no repite la construcción (G5) | F | — |
| V-INV-1 | Covarianza de escala y traslación en el régimen relativo (H11, H12; revisada por N4) | construcciones de V-MET-STRUCT-1 y V-MET-EST-1 escaladas por `1`, `10³` y `10⁶` (todas con `eps_rel·S ≥ eps_abs`) y trasladadas `10⁶` | mismas clases; errores relativos iguales dentro de un factor 2; el trabajo solo difiere donde el redondeo cambia una decisión de refinamiento | F | — |
| V-INV-1b | Régimen del suelo absoluto (N4) | la misma construcción escalada por `10⁻³`, con `S < eps_abs/eps_rel` | la tolerancia aplicada es `eps_abs`, como declara el contrato; no se exige invariancia de clase, de tolerancia relativa ni de trabajo | F | — |
| V-TRANS-1 | Propagación por transformaciones (N1) | homotecias de factor 2, 0 y −3, giros y simetrías de un locus de clase 1 y de uno de clase 3 | la imagen nunca mejora la clase de la fuente; valor y error absoluto escalados por `abs(λ)` con su cota de redondeo; `λ = 0` da cero exacto | F | — |
| V-CERT-RIGOR | Rigor de las clases certificadas (N5) | circunferencias, arcos, segmentos y programas v1/v2 con referencias independientes; envolventes con π, normas y raíces | toda cota `CERTIFIED_ERROR_BOUND` contiene la referencia; si una operación no tiene envolvente rigurosa, la certificación falla cerrada | F | — |
| V-INV-2 | Independencia de la vista | zoom, DPI y tamaño de vista distintos | resultados idénticos bit a bit | F | — |
| V-DET-1 | Determinismo | todas las anteriores dos veces | resultados idénticos bit a bit | F | — |

### 2.3 Casos adversarios

Ninguno de estos casos se supera aumentando la profundidad mínima. Comprueban la
honestidad de la clasificación, no la exactitud, que es imposible por la proposición del
[análisis](../architecture/pre_g9b_cu1_metric_aliasing_analysis.md) §3.

| Id | Construcción | Aserción |
|---|---|---|
| V-ADV-1 | `sin(2π·2^20·x)` sobre `[0, 1]` | nunca clase 1 o 2; si es de clase 3, sus hipótesis quedan registradas y accesibles. No se exige que el error real quede dentro del estimado (N5) |
| V-ADV-2 | meseta estrecha `A·exp(−((x − x₀)/w)²)`, con `x₀ = 1/π` y `w` inferior al espaciado mínimo de sondeo | clase 3, nunca 1 o 2. Las hipótesis registradas incluyen «no feature narrower than `h_probe`» con `h_probe > w`; la prueba documenta que esa hipótesis no se cumple y no afirma el valor |
| V-ADV-3 | V-ADV-1 y V-ADV-2 con políticas de prueba `d_min = 6, 10, 14` | la clasificación honesta se mantiene con cualquier profundidad mínima |
| V-ADV-4 | curva que se anula en todos los parámetros de sondeo registrados en una primera ejecución | el resultado repite el de la curva base y su clase no es 1 ni 2. Demuestra el límite y lo deja fijado |
| V-ADV-5 | expresión discontinua (`floor`) | nunca clase 1 o 2 |

### 2.4 Regresión de `CU0` y del producto

| Id | Aserción |
|---|---|
| V-CU0-1 | Todas las secciones `preserve` de la línea base de `CU0` se reproducen dentro de su tolerancia |
| V-CU0-2 | Las fijaciones `pins.B10.1`, `pins.H03` y `pins.H10_H11` quedan invertidas con el procedimiento de la [estrategia](../architecture/pre_g9b_cu1_correction_strategy.md) §4 y un registro de nueva línea base; las demás fijaciones (`B06`, `B07`, `H01`, `B08_B09`, `B10.2`, `H02`, `H06`) no cambian |
| V-CU0-3 | Las pruebas existentes que fijan el comportamiento corregido se cambian solo como conformidad declarada |
| V-PROD-1 | `final.shared` y `final.desktop` pasan sin pruebas debilitadas |

## 3. Verificación

Clase `INTEGRATED_PHASE` (C1-3). El candidato congelado se verifica con:

1. el registro de la selección `pre-g9b-cu1.desktop` (y, si la implementación añade pruebas
   compartidas, la selección compartida que corresponda) y de la fase `PRE-G9B-CU1` con el
   mecanismo oficial, actualizando el inventario con evidencia ejecutada y la huella del
   registro;
2. `verify.ps1 -Profile STATIC`;
3. `verify.ps1 -Profile INFRA_UNIT`, porque cambian el registro y el inventario;
4. `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU1`;
5. `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0`, con las fijaciones invertidas;
6. `verify.ps1 -Profile INTEGRATION`, con `-LogDirectory artifacts\agent\<nuevo>`;
7. el banco `composition` antes y después, con contadores (G8 informativa).

`FINAL` no es exigible para la clase y queda para `CU10`.

## 4. Lo que estas pruebas no pueden demostrar

- La exactitud de una estimación de clase 3 en una curva arbitraria: ninguna prueba
  finita puede hacerlo (proposición del análisis §3). Por eso la matriz exige que la
  clasificación sea honesta.
- La certificación de `o_1` y de `e_2`, que depende de la clase v3 de `CU2` (DEP-1).
- El rendimiento en máquinas distintas: G8 es informativa.

## 5. Notas del candidato de implementación (2026-10-20)

La implementación ejecutó la matriz con estas precisiones, que el
[informe del candidato](pre_g9b_cu1_implementation_candidate_report.md) detalla:

- **V-CACHE-1.** `excCyl` no es antecesor de la cadena `o_1 → d_2 → e_2`: solo alimenta la
  familia del cilindro. El cambio que debe invalidar es el del punto libre `D_1`, antecesor
  de `o_1`; el de `excCyl` comprueba que un cambio fuera de la cadena no publica revisión ni
  reconstruye estados (G1).
- **V-INV-1.** Con escala 1, una traslación de `10⁶` sitúa el redondeo de las coordenadas
  (`ulp(10⁶) ≈ 1,2·10⁻¹⁰`) por encima de las cuotas de las hojas: la estimación de clase 3
  falla cerrada con `CONSISTENCY_NOT_ESTABLISHED`. Hasta `10⁵` se conservan la clase, el valor y
  el trabajo, y las escalas `10³` y `10⁶` trasladadas `10⁶` cumplen la fila. La clase 1 no se ve
  afectada. Queda fijado por prueba y como decisión autoral pendiente.
- **V-MET-STRUCT-1 y V-CERT-RIGOR.** La referencia rigurosa es la longitud semántica exacta del
  programa capturado, calculada en aritmética decimal: el dominio binario64
  `[−fl(π), fl(π)]` es `2,4·10⁻¹⁶` más corto que `2π`. `2π·λ·r` se comprueba como verdad
  geométrica con tolerancia relativa `10⁻¹⁴`.
- **V-MET-STRUCT-2.** El certificado afín del proveedor reconoce solo las formas directas
  `(u, c)`, `(c, u)` y `(u, u)`. Una expresión afín como `(2u+1, 3u−1)` sigue siendo una
  estimación honesta, también fijada por prueba.
- **N3, inversiones de dirección.** El pie de una perpendicular invierte su dirección dos
  veces; una inversión próxima a una muestra oculta el mismo exceso a todos los niveles. El
  estimador localiza las inversiones y las usa como fronteras de la partición
  (`locus-v2-metrics.md` §25.12).
- **V-CU0-2.** Los pines `B10.1`, `H03` y `H10_H11` se invierten sin renombrar métodos, de modo
  que las identidades de la selección `pre-g9b-cu0.desktop` no cambian. La línea base de `CU0`
  conserva las firmas fijadas como evidencia histórica y ningún valor `preserve` cambia fuera de
  sus tolerancias.
