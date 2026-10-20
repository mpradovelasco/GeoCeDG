# PRE-G9B-CU2 — Diseño del evento estructural de la longitud orientada

| Campo | Valor |
|---|---|
| Carácter | Diseño candidato de la preparación de `CU2` para B10.2: evento estructural de envoltura de la longitud orientada y certificación de loci dependientes de ella. No es especificación normativa |
| Estado | `CU2 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false`; `TECHNICAL_CANDIDATE_STATE = FROZEN`; `AUTHOR_DECISION = NOT_RECORDED_IN_THIS_ARTIFACT` |
| Antecedentes | [análisis D de `CU0`](pre_g9b_cu0_oriented_length_analysis.md); enmienda candidata A2.3 de las [enmiendas de `CU0`](pre_g9b_cu0_candidate_normative_amendments.md); [análisis del modelo del autor](pre_g9b_cu2_author_model_certification_analysis.md) §5 |
| Contratos afectados | `locus-v2-metrics.md` v1.5 §22, §24.5 y §25.5; `locus-v2-existence-components.md` §3, §7 y §8 |
| Normativa propuesta | enmienda A2.3-R de la [propuesta normativa](pre_g9b_cu2_normative_proposal.md) |

## 1. Problema

`e_2 = LocusV2(R_1, E_1)` desarrolla el cilindro vertical que proyecta `o_1`. Su abscisa es
`x(L_1) − w_1(t)`, con `w_1 = LocusLength(o_1, N_1, K_1)` en la forma por defecto (adelante,
estricta, `zero-length`). `w_1` es un diente de sierra:

- crece continuamente con derivada `σ` (la velocidad de `o_1`);
- salta exactamente `L` (longitud total de `o_1`) cuando el punto generador `K_1` pasa por el
  origen `N_1`, en el parámetro `t_N` = 0;
- es continua en la costura `±π` del dominio del conductor.

Hoy el certificado de compatibilidad declara `e_2` como una componente periódica cerrada del
dominio `[−π, π)`, y el dibujo cierra la curva con un segmento de 13,19 unidades que une las dos
orillas del desarrollo (B10.2).

## 2. Cinco discontinuidades que no se confunden

La instrucción de 2026-10-20 exige distinguirlas con rigor:

| Tipo | Definición | En `e_2` |
|---|---|---|
| Discontinuidad del recorrido de longitud | la longitud orientada desde un origen fijo se reinicia cuando el destino pasa por el origen; es una propiedad de la ruta métrica, no de la geometría de la fuente | sí: en `t_N`, salto `L` |
| Discontinuidad real de la construcción | la función de la construcción deja de ser continua por sí misma: cambio de rama, selección no continuable, hueco de no existencia | no |
| Corte de parametrización periódica | la costura `±π` del dominio canónico del conductor; es un artefacto de la carta del parámetro y la función puede ser continua a través de ella | sí, y `e_2` es continua en ella |
| Continuidad entre extremos de componentes | si los extremos de una o varias componentes coinciden y la función se continúa entre ellos | no: los dos extremos de `e_2` corresponden al mismo punto del espacio (la generatriz de corte del desarrollo), pero en el plano distan `L`; no se unen |
| Conexión artificial del dibujo | un segmento dibujado que no corresponde a ningún tramo continuo de la función | sí: el segmento de cierre actual |

El diseño trata la primera como **evento estructural**, la segunda como frontera de la
existencia, la tercera como una propiedad de la carta que la representación de componentes en
arcos absorbe, la cuarta como una cuestión de identidad y nunca de continuidad, y la quinta
como un defecto que desaparece cuando el dibujo solo sigue componentes certificadas.

## 3. Principios

1. **`LocusLength` no cambia.** Ningún valor, política de recorrido, política de misma posición,
   orden, cociente ni dirección cambia. `preserve.oriented_length_profile` y `preserve.e_2` se
   reproducen.
2. **La periodicidad del conductor no se hereda.** `e_2` no se cierra porque su conductor sea
   periódico. Una componente es periódica solo si su continuidad en todo el círculo está
   certificada (diseño de certificación §4.4).
3. **El evento es estructural.** Se deriva de las entradas del DAG: la componente fuente
   certificada, la dirección semántica del origen y la política. No se infiere de coordenadas,
   de saltos entre muestras ni del dibujo.
4. **La clase numérica no certifica.** La longitud `L` es de clase `ESTIMATED` mientras la fuente
   no alcance las clases 1 o 2. El certificado de `e_2` no usa el valor de `L`, solo el hecho
   certificado `L > 0` (§5).

## 4. Definición del evento

### 4.1 Condiciones

Una consulta `LocusLength(L, A, B)` publica un **evento de envoltura del origen**
(`ORIGIN_WRAP`) en su resultado rico cuando se cumplen todas estas condiciones:

1. `B` es la ocurrencia generadora del conductor de `L` (`locus-v2-metrics.md` §24.5): su
   dirección semántica en `L` coincide con el parámetro del conductor, sin transformación.
2. `A` es una posición semántica fija: su dirección no depende del conductor (depende solo de
   entradas que no son el conductor, como `text2` y `v_1`).
3. `A` pertenece a una componente `C` de `L` certificada como **periódica completa**, es decir
   una sola componente cerrada que cubre todo el círculo con continuidad certificada.
4. La ruta tiene un sentido (adelante o atrás) y una política de misma posición.

### 4.2 Contenido

| Campo | Contenido | Garantía |
|---|---|---|
| `kind` | `ORIGIN_WRAP` | — |
| `source` | clave revisable de la componente `C` y su linaje estructural | exacto |
| `driverParameter` | la dirección canónica de `A` en `C` | exacto: es una dirección semántica, no una raíz numérica |
| `direction` | `FORWARD` o `BACKWARD` | exacto |
| `sameSide` | `ZERO_LENGTH` (el origen pertenece al tramo que empieza) o `FULL_CYCLE` (pertenece al que acaba) | exacto |
| `jumpPositive` | `CERTIFIED` si una cuerda con redondeo exterior entre dos puntos certificados de `C` es positiva; si no, `NOT_ESTABLISHED` | riguroso |
| `jumpValue` | referencia al resultado de la longitud total de `C`, con su clase de garantía | la de ese resultado (clase 3 en el modelo del autor) |

El evento está ligado a la revisión, no se serializa y no cambia el valor de ninguna consulta.
Viaja en el resultado rico, no en el escalar `Length`.

### 4.3 Casos

| Caso | Evento |
|---|---|
| `C` periódica completa, sentido adelante | salto en `t_A`; la función es continua en el resto del círculo |
| `C` periódica completa, sentido atrás | salto en `t_A`, con el lado de misma posición reflejado |
| `C` componente no periódica `[c₀, c₁]` y `A` en ella | sin envoltura. Adelante, la ruta existe solo para `t ∈ [t_A, c₁]`, y la existencia del dependiente queda limitada por esas fronteras estructurales. Atrás, `[c₀, t_A]` |
| origen en la costura (`t_A = −π`) | el corte coincide con la costura; la componente dependiente es `[−π, π)` y **no** periódica |
| origen que depende del conductor | sin evento estructural; el dependiente solo puede certificarse por otra vía (transición numérica certificada) o queda `NOT_ESTABLISHED` |
| `C` solo localmente certificada (no completa) | sin envoltura (no hay círculo completo); valen las fronteras certificadas de `C` |
| `jumpPositive = NOT_ESTABLISHED` | el dependiente no puede certificar la discontinuidad: banda `UNRESOLVED` en `t_A`, completitud `NOT_ESTABLISHED` |
| rutas explícitas entre dos posiciones fijas | no dependen del conductor y no generan eventos |

## 5. Certificación de un locus que depende de una longitud orientada

El productor dependiente certifica `e_2` componiendo hechos estructurales:

1. **Fuente:** `o_1` está certificada como componente periódica completa y rectificable. Lo
   segundo, con una cota superior rigurosa de la longitud por la envolvente de la primera
   derivada: `L ≤ 2π·sup|F′|`.
2. **Dominio de definición de `w_1`:** todo el círculo, porque la ruta adelante sobre una
   componente periódica completa existe siempre.
3. **Continuidad de `w_1`:** en todo el círculo salvo en `t_A`; la integral de una velocidad
   acotada es continua.
4. **Evento:** frontera de autoridad 1 (`locus-v2-existence-components.md` §7) en
   `t_A = driverParameter`, con inclusión según `sameSide`. Su no degeneración se apoya en
   `jumpPositive = CERTIFIED`.
5. **Resto del corte:** `pointJump` y `translationCoor` expandidas son afines y totales (tabla de
   operaciones totales, diseño §4.2), y `J_1` es de la segunda familia certificada.
6. **Resultado:** `COMPLETE`, una componente **abierta** representada como el arco
   `[t_A, t_A + 2π)` (con `zero-length`) que cruza la costura; no periódica.

En ningún paso interviene el valor estimado de `L` o de `w_1(t)`. Los valores de `e_2` son
estimaciones bajo el contrato de estimación de `CU1` (D-CU1-2): la estructura de existencia se
certifica y las coordenadas se declaran estimadas. Si la estimación de `w_1(t)` falla por
presupuesto en algún `t`, ese punto queda con estado de evaluación `NOT_ESTABLISHED`
(computacional). No se convierte en no existencia, porque la existencia ya está certificada
(diseño de certificación §3.3).

## 6. Consecuencias por consumidor

| Consumidor | Consecuencia |
|---|---|
| Dibujo | polilínea abierta de `e_2(t_A⁺)` a `e_2((t_A + 2π)⁻)`; sin segmento de cierre |
| DXF | un `LWPOLYLINE` abierto por la componente, en el orden del arco |
| LaTeX | un trazo abierto |
| Métrica de `e_2` | longitud sobre la componente abierta; sin ciclo completo ni `wrap-to-start`; la clase es la de su estimación (`L-CU1-E2-LENGTH`) |
| Intersecciones con `e_2` | marco de fase del arco `[t_A, t_A + 2π)`, sin cuarentena de costura (no es periódica); las raíces en sus extremos siguen la política de extremos abiertos o cerrados |
| Puntos semánticos sobre `e_2` | direcciones canónicas en `[−π, π)`; la pertenencia a la componente se decide sobre el arco |
| Identidad | el linaje de la componente se toma de la fuente estructural de su frontera, `(w_1, linaje del origen, lado)`, no de los bits de sus extremos |

## 7. Identidad y revisiones

- Arrastrar el conductor no cambia la revisión, el evento ni la componente (A1.1-R de `CU1`).
- Cambiar `v_1` o el selector de `N_1` desplaza `t_A`: revisión nueva, componente con el mismo
  linaje estructural y fronteras nuevas.
- Recertificar `o_1` debe mantener resoluble el selector de `N_1` (decisión E-3).

## 8. Alternativas descartadas

| Alternativa | Motivo |
|---|---|
| Parámetro elevado acumulado | el número de vueltas dependería de la historia del arrastre y rompería el determinismo |
| Certificar `e_2` a través del valor de `d_2` (propuesta E de `CU0`) | usaría una longitud de clase 3 como entrada rigurosa, lo que prohíben §25.5 y la instrucción de cierre de `CU1` |
| Localizar el salto como raíz numérica de la diferencia entre muestras | inferencia por muestreo; no certifica nada |
| Corte elegido por el usuario | posible como opción futura; no hace falta para la semántica actual |
| Cambiar la política por defecto de `LocusLength` | cambio silencioso de semántica, expresamente excluido |

## 9. Validación

Filas V-E2-1 a V-E2-7 de la [matriz de validación](../validation/pre_g9b_cu2_validation_matrix.md):

- `e_2` como una componente abierta;
- valores idénticos;
- ausencia de segmento de cierre en el dibujo, el DXF y LaTeX;
- evento exacto en `t_A`;
- certificado independiente de la clase de la longitud: forzar la estimación de `w_1` a fallar
  en un conductor no cambia la estructura de existencia, solo el estado de evaluación de ese
  punto;
- casos de §4.3;
- identidad del evento al arrastrar el conductor y al cambiar `v_1` (§7).
