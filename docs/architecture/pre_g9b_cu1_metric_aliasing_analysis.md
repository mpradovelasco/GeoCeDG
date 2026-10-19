# PRE-G9B-CU1 — Aliasing métrico y garantías de longitud disponibles

| Campo | Valor |
|---|---|
| Carácter | Análisis técnico de la preparación de `CU1`. No es normativo: las reglas propuestas están en la [propuesta normativa revisada](pre_g9b_cu1_normative_proposal.md) |
| Estado | `CU1 PREPARATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false`; implementación de `CU1` no autorizada |
| Encargo | [registro de cierre de `CU0`](../validation/pre_g9b_cu0_closeout_record.md) §4 (precisión H10) y §7 |
| Base | cierre documental de `CU0` `df675e9ee42e1a4caac0e3bf2d74ab2d30450581` |
| Fuentes | código de `source/shared/common/src/main/java/org/geocedg/common/kernel/` (rutas abreviadas a `locus/metric/…`, `locus/intersection/…`, `algos/…`); `geocedg/specs/locus/locus-v2-metrics.md` v1.4; línea base de `CU0` (`pins.H10_H11`, `preserve.o_1_total_length*`) |

## 1. Qué hace hoy la métrica

| Hecho | Referencia |
|---|---|
| Orden de capacidades en producción: diferencial y, si no aplica, solo evaluador. La capacidad analítica solo se usa en pruebas | `algos/AlgoLocusMetricV2.java:78-82`; `algos/AlgoLocusBetweenMetricV2.java:53-58`; `locus/metric/LocusMetricCapabilityHierarchy2D.java:45-54` |
| La diferencial solo se aplica a SplineV2 y a sus imágenes por semejanza (Simpson adaptativo con corrección de Richardson, error `defecto/15`, garantía estimada) | `locus/metric/DifferentialLocusMetricCapability2D.java:60-66, 141-162`; `locus/metric/LocusMetricIntegrator2D.java:114-138` |
| Todo `LocusV2(Q, P)` usa `ReconstructibleLocusEvaluator2D`, que no es diferencial y declara calidad `FLOATING_POINT_UNCERTIFIED`; su métrica es siempre la de solo evaluador. Esto incluye el caso H10, las circunferencias de H11 y los loci `o_1` y `e_2` del autor | `locus/ReconstructibleLocusEvaluator2D.java:49-55` |
| Algoritmo de solo evaluador: un intervalo raíz con tres muestras y **sin profundidad mínima**. Un nodo se acepta si `fine − coarse ≤ threshold(max(fine, coarse))`, comparando las dos semicuerdas con la cuerda. Se devuelve `fine` sin corregir, el error es la suma de los defectos y, al llegar a la resolución del parámetro, se acepta sin diagnóstico | `locus/metric/EvaluatorOnlyLocusMetricCapability2D.java:98-109, 291-326` |
| Umbral `max(1e-10, 1e-9·S)` evaluado **por nodo**, con `S` igual a la cuerda local y no a la escala de la componente; la tolerancia no se reparte | `locus/metric/LocusMetricPolicy2D.java:12-13, 135-140` |
| Presupuesto por construcción de componente: 32 768 evaluaciones, 16 384 subdivisiones y profundidad 22. Al agotarse, el resultado es `Absent` con `LIMIT_NOT_ESTABLISHED`; la dimensión agotada solo aparece en el texto del diagnóstico, y el fallo no se memoriza, por lo que cada recálculo vuelve a gastar el presupuesto | `locus/metric/LocusMetricPolicy2D.java:14-16`; `EvaluatorOnlyLocusMetricCapability2D.java:112-118`; `locus/metric/LocusMetricIndex2D.java:74-79, 91-94` |
| Los tipos de garantía ya existen (`EXACT_ARITHMETIC`, `CERTIFIED_ERROR_BOUND`, `ESTIMATED_ERROR`, `FLOATING_POINT_UNCERTIFIED`), pero ninguna ruta de producción emite `CERTIFIED_ERROR_BOUND` | `locus/LocusSemanticMetadata2D.java:55-58`; `locus/metric/MetricErrorEvidence2D.java:61-67` |
| La admisibilidad escalar acepta cualquier `ESTIMATED_ERROR` sin comparar su magnitud con la tolerancia | `locus/metric/LocusMetricResult2D.java:156-174`; `algos/AlgoLocusMetricScalarAdapter.java:62-70` |
| La especificación ya prohíbe que la concordancia del refinamiento de solo evaluador reclame `CERTIFIED_ERROR_BOUND` y exige hipótesis registradas para `ESTIMATED_ERROR` | `locus-v2-metrics.md:281-284` |

## 2. Explicación cuantitativa de H10, H11 y H12

**H10.** El caso es `Q = (x(P), sin(2πk·x(P)))` con `P` sobre `Segment((0,0),(1,0))`. Las
tres muestras iniciales (`x = 0`, `0,5` y `1`) son ceros de `sin(2πk·x)` para todo `k`
entero. Son colineales, el defecto es 0 y la raíz se acepta en profundidad 0. El
resultado es la longitud 1,0 con error estimado 0, admisible como escalar. Las
longitudes reales para `k = 1, 2, 4` son 4,1883, 8,1118 y 16,0647 (`pins.H10_H11`).

**H11.** En un arco de ángulo `θ` y radio `r`, el defecto entre la cuerda y las dos
semicuerdas es:

```text
4r·sin(θ/4) − 2r·sin(θ/2) ≈ rθ³/32
```

Con nodos pequeños domina el suelo absoluto `1e-10`, de modo que hace falta
`θ ≤ (3,2·10⁻⁹/r)^(1/3)`. Las hojas son diádicas sobre `[−π, π)` y una profundidad `d`
cuesta `2^(d+1) + 1` evaluaciones:

- `r = 7` exige `d = 13`, es decir, 16 385 evaluaciones;
- `r = 8` exige `d = 14`, es decir, 32 769 evaluaciones, por encima de las 32 768 del
  presupuesto, y la longitud queda indefinida.

**H12.** Para `r = 1` se acepta en `d = 13`. El valor devuelto es la suma de
`2^14` cuerdas, cuyo sesgo es `2πr·φ²/24 ≈ 3,85·10⁻⁸` con `φ = π/2^13`; coincide con
el valor fijado, `6,283185268677126` frente a `2π`. La suma de defectos declarada es
≈ `1,16·10⁻⁷`, es decir, tres veces el error real, como corresponde a un error en
`h²`. Es honesta como estimación, pero supera la tolerancia relativa declarada de
`10⁻⁹`, y aun así el resultado es admisible. El valor corregido por Richardson,
`fine + defecto/3`, tendría error de orden `h⁴` (≈ `10⁻¹⁵`).

En `o_1` se observa el mismo sesgo: `13,2364580874` frente a la referencia
independiente `13,2364581958`, una diferencia de `1,08·10⁻⁷` (`8·10⁻⁹` relativo).

## 3. Por qué la profundidad mínima y la concordancia de niveles no bastan

**Proposición (adversario).** Sea `A` un algoritmo determinista, adaptativo o no, que
calcula una longitud a partir de un número finito de evaluaciones puntuales de la
curva. Para toda curva `F` en `[a, b]` y todo `M > 0` existe una curva `G` de clase
`C^∞` tal que:

- `G` coincide con `F` en cada parámetro que `A` evalúa sobre `F`, de modo que
  `A(G) = A(F)`;
- `L(G) > L(F) + M`.

*Demostración.* Sea `S` el conjunto finito de parámetros que `A` evalúa sobre `F`. Como
las respuestas en `S` coinciden, la ejecución sobre `G` consulta los mismos puntos.
Se toma un intervalo `(α, β) ⊂ [a, b] \ S`, una función meseta `φ` de clase `C^∞` con
soporte en `(α, β)` y un vector unitario `n`, y se define:

```text
G = F + ε·φ(t)·sin(ωt)·n
```

`G` coincide con `F` en `S`, y su longitud crece sin cota cuando `ω` crece. ∎

La misma construcción funciona con un factor polinómico que se anula en `S` en lugar
de la meseta, y entonces `G` es analítica. Consecuencias:

1. Ni la profundidad mínima, ni la concordancia de dos o más niveles, ni ninguna
   combinación de pruebas finitas de muestreo certifican la longitud. Son algoritmos
   del tipo `A`.
2. La regularidad cualitativa (`C^k`, analiticidad) no basta. Hace falta información
   **cuantitativa** sobre la curva: cotas de derivadas sobre intervalos, una forma
   estructural conocida o una complejidad acotada.
3. Un algoritmo de muestreo solo puede producir una **estimación**, válida bajo
   hipótesis explícitas sobre la escala de los rasgos de la curva. Si se elige bien,
   esa hipótesis se cumple en los casos caracterizados y puede declararse.

Casos que no se resuelven aumentando la profundidad mínima:

- `sin(2π·2^m·x)` se anula en todos los puntos diádicos hasta la profundidad `m`;
- una meseta estrecha situada entre muestras pasa inadvertida para cualquier rejilla
  más gruesa que su anchura;
- una curva construida para anularse en la secuencia de sondeo de un algoritmo
  determinista concreto lo engaña siempre.

## 4. Información que sí permite garantías

### 4.1 Identidades estructurales (clase 1)

| Identidad | Longitud | Capacidad existente | Uso actual en la métrica |
|---|---|---|---|
| Cero semántico o imagen colapsada | 0 exacto | casos especiales de la métrica | sí |
| Imagen afín del parámetro, `F(u) = c + a·u` | `abs(a)·Δu` | `CertifiedAffineLocus2D` con coeficientes afines exactos (`locus/ReconstructibleLocusEvaluator2D.java:185-215, 236-274`; `locus/LocusSimilarityEvaluator2D.java:51-81`) | **no**; solo la interacción de puntos |
| Imagen por semejanza de un locus fuente | `λ·L_fuente` | `LocusSimilarityTransform2D.getLengthScale()`, «exact metric scale» (`locus/LocusSimilarityTransform2D.java:243-246`) | **no** |
| Semejanza con parámetros constantes aplicada al conductor (circunferencia, arco, segmento) | `λ·2πr`, `λ·r·w·Δτ`, `λ·√(a²+b²)·Δt` | el programa capturado de la clase v1/v2 describe la cadena de operaciones y conoce los parámetros constantes (`locus/CertifiedConstructionProgram2D.java:31-58`; `locus/intersection/ConstructionIntervalModel2D.java:102`) | **no** |

El resultado se calcula con redondeo hacia fuera. Su cota es la del redondeo, unas
pocas ulp, y no hace falta ninguna hipótesis. Las circunferencias de H11
(`Q = P + (0,0)`) son de este tipo: la longitud es `2πr` exactamente, sin
presupuesto.

### 4.2 Envolventes de la primera derivada (clase 2)

`CertifiedIntervalCurveModel2D` da envolventes hacia fuera, en aritmética binary64 con
redondeo exterior, del valor y de la primera derivada de la curva **semántica** (la
exacta, no la evaluada en coma flotante) sobre cajas de parámetro. Hay dos
implementaciones:

- `SplineIntervalModel2D`, con coeficientes racionales exactos, para SplineV2;
- `ConstructionIntervalModel2D`, para programas de las clases v1 y v2.

Referencias: `locus/intersection/CertifiedIntervalCurveModel2D.java:20-27, 49-64`;
`locus/intersection/ConstructionIntervalModel2D.java:21-26, 85-103`;
`locus-v2-certified-construction-model.md:23-25, 198-208`.

La clase v1/v2 admite:

- conductores: circunferencia, arco, segmento y escalar afín;
- operaciones: recta por dos puntos, paralela, perpendicular, intersección de rectas
  ilimitadas, punto medio, traslación, giro, simetría y homotecia con parámetros
  constantes, y `P ± V`.

Excluye rayos y segmentos como operandos de intersección, intersecciones con cónicas,
expresiones generales, listas, macros y loci como conductor
(`locus-v2-certified-construction-model.md:248-261`).

**Lema (cota del cono).** Sea `F` absolutamente continua en `[a, b]` con `F'(t)` en
un cono convexo cerrado de vértice el origen y semiángulo `α < π/2` para casi todo
`t`. Entonces:

```text
|F(b) − F(a)| ≤ L ≤ |F(b) − F(a)| / cos α
```

*Demostración.* Sea `u` la bisectriz unitaria del cono. Para todo `t` se cumple
`F'(t)·u ≥ |F'(t)|·cos α`, y por tanto:

```text
|F(b) − F(a)| ≥ (F(b) − F(a))·u = ∫ F'·u ≥ cos α · ∫ |F'| = L·cos α
```

La cota inferior es la desigualdad de la cuerda. ∎

Con envolventes rigurosas, una partición en cajas da una **envolvente certificada**
de la longitud:

- cota inferior: suma de cotas inferiores de cuerdas, obtenidas de las envolventes
  puntuales de los extremos;
- cota superior: suma de `cuerda_sup / cos α_sup` en cada caja, o
  `|caja|·sup|F'|` donde la caja de derivadas contiene el origen o no cabe en un
  cono de semiángulo menor que `π/2` (convergencia de primer orden).

**Convergencia y coste.** En una caja que gira un ángulo `δ`, el semiángulo es
`α ≈ c·δ/2`, con `c ∈ [1, √2]` según la sobreestimación de la caja. El exceso relativo
por caja es `α²/2`, de modo que la convergencia es de segundo orden. Para una curva
de giro total `Θ` y tolerancia relativa `τ` hacen falta del orden de
`n ≈ c·Θ / √(8τ)` cajas. En una circunferencia (`Θ = 2π`):

| Tolerancia relativa certificada | Cajas |
|---|---:|
| `10⁻⁶` | 2 200–3 100 |
| `10⁻⁸` | 22 000–31 000 |
| `10⁻⁹` | 70 000–100 000 |

El coste crece con el giro total y con `1/√τ`. Una curva muy oscilante (H10 con `k`
grande) tiene un giro enorme y agota cualquier presupuesto fijo. Ese es el límite
honesto: el resultado queda no establecido o en estimación, nunca en certificado
falso.

Falta para usar esta capacidad en la métrica:

- una fachada pública, porque los tipos son privados del paquete `intersection`;
- una raíz cuadrada con redondeo exterior. `Math.sqrt` está correctamente redondeada,
  y tomar `nextUp` del resultado aplicado a una cota superior da una cota rigurosa.

Las cajas que el modelo rechaza (divisor que puede anularse, configuración
degenerada) quedan sin resolver y hacen que la clase 2 no se alcance en esa
componente; nunca se certifican.

### 4.3 Estimación con hipótesis explícitas (clase 3)

Cuando no hay capacidad de las clases 1 o 2, solo cabe estimar. Una estimación bien
diseñada:

- parte de una partición uniforme mínima;
- usa sumas de cuerdas corregidas por Richardson, con error de orden `h⁴` en curvas
  `C⁴`;
- contrasta cada hoja con una sonda interior situada en una fracción no diádica, como
  la sección áurea. Eso rompe el aliasing con frecuencias conmensurables con la
  rejilla diádica, como en H10;
- registra sus hipótesis: regularidad por tramos a escalas mayores que el
  espaciado mínimo de sondeo `h_probe`, régimen asintótico de Richardson y ausencia de
  rasgos más estrechos que `h_probe`.

Resuelve los casos caracterizados (H10 con `k = 1, 2, 4`, el sesgo H12, H11 por la
vía de solo evaluador), pero **no es una garantía**. El adversario de §3 la engaña,
y su clase lo declara.

### 4.4 Lo que no existe

| Capacidad | Situación |
|---|---|
| Cotas de curvatura, de Lipschitz o de variación total | no existen en el repositorio |
| Segunda derivada (jets de segundo orden) en el modelo de intervalos | no existe; es una ampliación de la clase v3, enmienda A2.2 de `CU2` |
| Intervalos de expresiones generales del conductor (`sin`, `exp`, `sqrt`…) | no existen en el modelo certificado. El paquete de intervalos heredado de GeoGebra (`org/geogebra/common/kernel/interval/`) **no es riguroso**: la suma no redondea hacia fuera, las cotas de π colapsan a `Math.PI` y ajusta resultados por tolerancia. No sirve para certificar |
| Intervalos para rayos, intersecciones con cónicas y conductores locus | no existen. Pertenecen a la clase v3 de `CU2` |

## 5. Cobertura por familia en `CU1`

| Familia | Clase alcanzable en `CU1` | Qué la ampliaría |
|---|---|---|
| Cero, imagen colapsada, componente puntual | 1 | — |
| `LocusV2(Q, P)` cuyo programa es una cadena de semejanzas con parámetros constantes sobre un conductor circunferencia, arco o segmento (casos de H11) | 1 | — |
| Loci afines (`CertifiedAffineLocus2D`) | 1 | — |
| Imágenes por semejanza de loci (`LocusSimilarityEvaluator2D`) | la de la fuente, con la escala exacta | — |
| `LocusV2(Q, P)` con programa de clase v1/v2 (rectas por dos puntos, paralelas, perpendiculares, intersecciones de rectas, puntos medios, transformaciones, `P ± V`) | 2 si la envolvente alcanza la tolerancia; si no, 3 con la envolvente adjunta | más presupuesto (coste ∝ giro/√τ) |
| SplineV2 | 2 con `SplineIntervalModel2D` si alcanza la tolerancia; si no, 3 (Simpson) con la envolvente adjunta | jets de segundo orden (A2.2) |
| `LocusV2(Q, P)` con expresiones generales (H10), rayos, segmentos acotados, intersecciones recta–cónica o cónica–cónica, conductores locus, loci anidados, listas o macros (`o_1`, `e_2`) | 3 | clase v3 de `CU2` (rayos, recta–circunferencia); las expresiones generales necesitarían una capacidad nueva, hoy no planificada |

Consecuencia para el modelo del autor: `o_1` y `e_2` quedan en clase 3 durante `CU1`.
La cadena `o_1 → LocusLength → d_2 → e_2` sigue definida bajo el contrato de
estimación (propuesta normativa §3.5). Su certificación depende de la clase v3
de `CU2`.

## 6. Consecuencias para el diseño

1. Cuatro clases realizables: estructural, envolvente certificada, estimación y no
   establecido. Se corresponden con las garantías ya tipadas, y la concordancia de
   sumas de cuerdas nunca asciende a las clases 1 o 2.
2. La clase 3 se mejora para que resuelva los casos caracterizados y declare sus
   hipótesis. Las pruebas adversarias comprueban la **honestidad de la
   clasificación**, no la exactitud, porque la proposición de §3 impide lo segundo.
3. La tolerancia se fija por componente, con una escala invariante por traslación, y
   se reparte entre hojas. El error declarado se compara con ella antes de admitir el
   valor (H11, H12).
4. El presupuesto no puede ser universal. El coste depende del giro total y de la
   tolerancia, el agotamiento es un resultado tipado y memorizado, y los valores por
   defecto se justifican con el banco.
5. El uso de un resultado como entrada del DAG depende de su clase (propuesta
   normativa §3.5).
