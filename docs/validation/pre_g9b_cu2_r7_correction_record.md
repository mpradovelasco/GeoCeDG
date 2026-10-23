# PRE-G9B-CU2 — Registro de la corrección D-CU2-T13-R7

| Campo | Valor |
|---|---|
| Carácter | Registro de corrección del descendiente correctivo de `CU2`. No es evidencia de aceptación ni aprobación; `selfApproved=false` |
| Origen | Decisión autoral D-CU2-T13-R7: A(a) con B(i), con condiciones normativas, para los hallazgos H-T13-B y H-T13-C del [registro de R6](pre_g9b_cu2_r6_correction_record.md) |
| Base | rama local `phase/pre-g9b-cu2-corrective` sobre `f50a03439` (R6); candidato congelado de T13 `39741a0b`; corrección de H-T13-B en el commit `8d3e5ef0a` y prueba de H-T13-C en `05a6abc18` |
| Resultado | H-T13-C clasificado antes de implementar (§2): fallo exclusivamente computacional que el contrato vigente ya representa; sin cambio de producto, con pruebas. H-T13-B corregido (§3): banda rigurosa del primer intento decisivo del cálculo heredado. Precisiones F-CU2-1 y V-INV-2 aplicadas (§4) |
| Decisiones pendientes | D-CU2-T13-R5 (§7 y [inventario](pre_g9b_cu2_r5_gap_inventory.md) §7); V-DET-1 sigue abierta |
| Verificación | pruebas focales, toda la batería de `:shared:common-jre:test` y las pruebas de escritorio de `CU0`, `CU1`, `CU2` y S1 (§8); ninguna puerta integrada de aceptación y ningún candidato congelado, como pide la decisión mientras R5 siga pendiente |

## 1. Alcance

La decisión autoriza la corrección conjunta de H-T13-B y H-T13-C en este descendiente, con la
precisión mínima de F-CU2-1 y de V-INV-2, sin modificar en general el modelo de certificación ni la
semántica de GeoGebra. Fija cinco principios: no convertir fallos numéricos en
`CERTIFIED_DOES_NOT_EXIST`; no mantener afirmaciones de continuidad o disponibilidad que el
comportamiento heredado no justifica; usar `UNRESOLVED` y `NOT_ESTABLISHED` cuando falte evidencia;
conservar los certificados matemáticos válidos cuando el fallo sea exclusivamente de la evaluación
numérica, declarando `EVALUATION_FAILED` con causa tipada; y no dejar que los consumidores lean la
existencia matemática como disponibilidad operativa.

No se ha cambiado: el algoritmo heredado de intersección, ninguna tolerancia global de GeoGebra, las
reglas de selección de raíces coincidentes, el tratamiento de las salidas indefinidas (ninguna se
sustituye por un valor límite), ningún formato persistido ni identidad duradera, ni las garantías de
`CU0` y `CU1`. No se ha congelado candidato ni ejecutado puerta integrada; nada se ha publicado,
fusionado ni etiquetado, y `geocedg_book` no se ha tocado.

## 2. H-T13-C — clasificación

### 2.1 Situaciones y clases

La decisión pide clasificar cada situación antes de implementar: (a) existencia matemática
certificada con un fallo exclusivamente computacional del evaluador; (b) imposibilidad de demostrar
que la selección heredada sigue definida y continua; (c) incoherencia entre el modelo capturado y la
semántica efectiva heredada. Con la notación de §3.3 del registro de R6 (regímenes S1 a S4 del
primer intento del cálculo heredado):

| Situación | Salida | Clase | Tratamiento |
|---|---|---|---|
| S3: ningún reintento pasa la prueba de incidencia y las dos salidas quedan indefinidas, con dos raíces exactas en el camino | etiquetada | (a) | se conserva la existencia matemática; la evaluación publicada es `EVALUATION_FAILED` con causa `COMPUTATIONAL_FAILURE` (`locus-v2-existence-components.md` §15.5); `LocusLength` da `NUMERICAL_FAILURE`, `Intersect` no establece y el dibujo marca un hueco computacional |
| S4: la raíz seleccionada falla la incidencia y la otra no | etiquetada | (a) | igual |
| S1: tangente declarada en un intento cuyo pie pasa la incidencia | etiquetada | no es un fallo | la salida es el pie, un valor efectivo heredado a menos de `√(10⁻⁵)·R` de la raíz exacta; la existencia certificada es cierta y la posición publicada es la del evaluador |
| S1 y S2 (raíces a menos de `10⁻⁸`): la regla `avoidDoubleTangentPoint` anula la salida | segunda sin etiqueta | (b) | fallo cerrado: banda `UNRESOLVED` con causa `UNLABELLED_COINCIDENT_OUTPUT` (H-T13-B, §3) |
| S3 y S4 de la segunda salida sin etiqueta | segunda sin etiqueta | (b) | tras un primer intento no decisivo, un reintento posterior puede declarar tangente y anular la salida: la banda lo encierra |

No se ha encontrado ningún caso (c). El programa capturado reproduce la selección heredada y la copia
del corte coincide con el vivo: la prueba de §2.3 asevera, en cada muestra certificada, que la
evaluación falla exactamente donde la salida viva está indefinida. La representación de la
circunferencia que lee la prueba heredada de incidencia (punto medio y semieje, no la matriz) era una
fuente potencial de (c); la banda de §3 la modela con el semieje capturado.

### 2.2 Corrección del registro de R6

El registro de R6 (§4) describía H-T13-C como un defecto: certificados `COMPLETE` que «afirman
existencia donde el locus publicado no tiene punto», contra §10.3 del modelo de construcción
certificada. Era una descripción excesiva. La existencia de la raíz es un hecho matemático que el
certificado afirma correctamente; la indisponibilidad efectiva la representa el estado de la
evaluación, como exige §15.5, y ningún hecho se infiere de otro (§10.3): el certificado no afirma
disponibilidad. El producto ya aplicaba ese tratamiento (`LocusDefinition2D` concilia la evaluación
con la existencia certificada y antepone `COMPUTATIONAL_FAILURE` al diagnóstico del evaluador). Lo
falso era la precisión F-CU2-1, que daba la ventana de tangencia como mero exceso de evaluación; se
corrige en §4.

### 2.3 Contrato y pruebas

La separación entre existencia matemática, existencia y continuidad certificadas de la rama
seleccionada, disponibilidad operativa y representación se puede expresar con el contrato vigente;
no hace falta ninguna modificación normativa más allá de la precisión de F-CU2-1. La prueba
`aCertifiedRootThatTheInheritedComputationLosesIsAComputationalFailure` (`PreG9BCu2HybridCertificationTest`)
recorre la configuración crítica, la mordedura y la penetración con escala `10⁻³` y traslación
`(10³, 10³)`, y con escala `2⁻⁹` y traslación `(1024, 1024)`, en las unidades de la matriz:
certificado `COMPLETE` de `lq` y `lr`; en cada muestra de existencia certificada cerca de `t_c`, o
bien la evaluación es un punto, la salida viva está definida y el punto dista de una raíz exacta (60
cifras) como mucho `1,05·√(10⁻⁵)·R`, o bien la evaluación es `EVALUATION_FAILED` con
`COMPUTATIONAL_FAILURE` y la salida viva está indefinida; hay fallos computacionales; y
`LocusLength(lq)` da `NUMERICAL_FAILURE` con ese diagnóstico. Pasa en el descendiente y también sobre
`39741a0b`: el comportamiento es anterior y correcto, y la prueba lo fija.

## 3. H-T13-B — banda rigurosa de la segunda salida sin etiqueta

### 3.1 Causa

La banda de T3 (`D ≤ 10⁻³·máx(a², b², c²)` o `D ≤ 10⁻¹⁰·(s² + R²·(a² + b²))`) es conservadora pero
mal condicionada: equivale a `R² − h² ≤ 10⁻³·δ_O²`, con `δ_O` la distancia del origen a la recta, y
crece con el cuadrado de la posición absoluta (registro de R6, §3.2). El conjunto heredado de
supresión S depende de la ventana de tangencia del primer intento, de la normalización de la recta y
de los reintentos (§3.3 de ese registro), no de `δ_O`. Con traslaciones de `10³` sobre los múltiplos
de 125 la banda de T3 ocupaba una parte creciente del dominio sin supresión viva, y desde unas 30
veces el radio todo el dominio.

### 3.2 Contrato anterior y precisión aprobada

`locus-v2-certified-construction-model.md` §10.3 exige, alrededor de la raíz coincidente, una banda
no resuelta con causa tipada «bounded by conservative first-level predicates», y que fuera de ella la
salida exista donde sus predicados de validez están certificados. La decisión pide que el predicado
nuevo cubra de forma conservadora toda situación en la que el algoritmo heredado pueda dejar la
salida indefinida, sin reducir la banda con una tolerancia ajustada por muestreo, y que las regiones
no certificables queden sin certificar. La precisión B(i) de V-INV-2 está en §4.

### 3.3 Cambio de producto

**Predicado** (`InheritedLineCircleBand2D`, nuevo). Para una recta, un rayo o un segmento por un punto
constante capturado y el conductor de una circunferencia completa, contra una circunferencia
constante capturada, una caja queda fuera de la banda solo si, en todo conductor binary64 de la
caja, el primer intento (`eps = 10⁻¹⁵`) del cálculo de `AlgoIntersectLineConic` es decisivo y
mantiene la salida definida:

1. `u ≥ eps` en su valor calculado: no entra en la rama degenerada;
2. el `dis` calculado supera su ventana `delta = mín(10⁻⁵, máx(1, |2d| + |u| + |w|)·eps)`: no
   declara tangente;
3. la raíz calculada pasa las pruebas heredadas de incidencia, así que no hay reintento: la de la
   circunferencia (`|dist²/h0² − 1| ≤ 10⁻⁵`), la de la recta completa
   (`|a·x + b·y + c| < 10⁻⁵·(|a| + |b|)`) y la del parámetro del rayo o del segmento (`≥ −10⁻⁵` y
   `≤ 1 + 10⁻⁵`);
4. las dos raíces calculadas difieren en más de `10⁻⁸` en alguna coordenada, así que
   `avoidDoubleTangentPoint` no anula la salida.

Cada desigualdad se decide con las envolventes de la caja y cotas a priori de todo redondeo: del
punto del conductor (`Math.cos` y `Math.sin` con un ulp, transformación afín), de
`GeoVec3D.lineThroughPoints`, de la normalización (se enumeran todos los exponentes posibles en la
caja, hasta 64; un coeficiente que se anula en la caja abarca muchos), de `p`, `d`, `w`, `dis`, la
raíz cuadrada, `q = −(d + s)`, `q/u` y `w/q`, de las coordenadas `p + t·v` y de las pruebas de
incidencia (`γ_n = n·2⁻⁵³/(1 − n·2⁻⁵³)`). El `dis` exacto de las entradas vivas se encierra desde
`D = (a² + b²)·R² − s²` de la caja con la identidad `dis = 4^k·((a_f² + b_f²)·R² − (s_f − e_p)²)`,
nunca desde `d² − u·w`, cuyos términos se cancelan. El término de primer orden de la prueba de la
circunferencia, `2·√dis·|δt|`, se acota punto a punto (`√dis·|δs| ≤ redondeo + √dis·u·√dis_c`), no
con los extremos de la caja. La prueba del camino admite como error el margen del parámetro exacto
certificado de la raíz seleccionada más `10⁻⁵`. Fuera de esas cajas, la caja está en la banda, sea
cual sea el motivo; nada en la banda se certifica.

**Captura** (`CertifiedConstructionCapture2D`, `CertifiedConstructionProgram2D`). La prueba heredada
de incidencia lee el punto medio y el semieje `h0` de la circunferencia, no su matriz. El operando
circunferencia de clase v3 conserva ahora `h0` junto a `A2`, `A4` y `A5`, y la captura exige el punto
medio `(−A4, −A5)` exactamente (`setSphereNDMatrix` y `classifyConic` lo dan siempre con
`A0 = A1 = 1`, `A3 = 0`). El predicado usa el desfase exacto `|h0² − R²|`. Sustituye a la cota uniforme
`2⁻⁴⁸·(A4² + A5² + |A2|)` que proponía el registro de R6 (§3.5), demasiado pesimista para una
circunferencia pequeña lejos del origen: con escala `2⁻⁹` y traslación `(1024, 1024)` esa cota sola
consumía más del 60 % de la tolerancia `10⁻⁵`, y el desfase real es 0. El programa es material del
certificado, nunca identidad: no cambia ninguna huella, firma de revisión ni dato persistido.

**Envolventes relativas al centro** (`ConstructionBoxModel2D`). Para la banda, `a`, `b`, `a² + b²`,
`s = (P − m) × (N − m)` y `D` se calculan también en coordenadas relativas al centro `m` de la
circunferencia, donde una traslación de toda la construcción se cancela, y se intersecan con las
absolutas (las dos encierran los mismos valores exactos). Con la traslación `(1024, 1024)`, `s`
absoluto cancela términos de `10⁴` veces su valor, y las cajas iniciales daban `D` en
`[−67,8; 9,3·10⁻⁵]`.

**Cierre de la banda** (`HybridExistenceCertifier2D`). Cerca del borde de la banda el margen del
predicado es comparable al redondeo de la envolvente de `D` (en la configuración crítica con
múltiplos de 125, del orden de una unidad frente a términos de `1,7·10¹⁵`), y las celdas de la
resolución de refinamiento alternaban entre válidas y banda: más de diez fragmentos en los peores
casos, de `5·10⁻¹²` a `2·10⁻¹⁰` rad, cada uno una componente local distinta. Un tramo de celdas
válidas más estrecho que la resolución de banda (`2π/2⁸`) entre dos celdas de banda de esta causa se
publica como interior de la banda. Solo retira existencia certificada, nunca la afirma, y restablece
la regla de diseño de que una banda solo se resuelve en sus bordes. La anchura retirada en las
configuraciones probadas va de `10⁻¹⁰` a `10⁻⁹` rad.

**Alcance.** Las demás intersecciones recta–circunferencia con una segunda salida sin etiqueta
conservan la banda de T3, que sigue siendo conservadora.

### 3.4 Resultados

Estructura de `lr2` (segunda salida de `Intersect(p, t2, 2)`) en la sonda de solidez: 3
configuraciones × 14 transformaciones; E existencia certificada, U banda, N inexistencia certificada.

| Transformación (unidad, escala, traslación) | Crítica | Mordedura | Penetración |
|---|---|---|---|
| 125, 1, 0; 1, 1, 0; 125 con `±10³`, `10⁻³`, `10³` y sus combinaciones; 125 con `10⁶` (escala o traslación) | E U E | E U N U E | E, `COMPLETE` |
| 1, `10⁻³`, 0 (constantes redondeadas) | E U N U E | E U N U E | E, `COMPLETE` |
| 1, 1, `(1000,1; −999,7)` | E U E | E U N U E | E, `COMPLETE` |
| 1, `2⁻⁹`, `(1024, 1024)` | E U E | E U N U E | E, `COMPLETE` |
| 1, `10⁻³`, `(10³, 10³)` | U N U | U N U | U |

Con escala `10⁻³` sin traslación en las unidades de la matriz, las constantes capturadas, redondeadas,
separan la raíz doble de la crítica en dos raíces simples según su clasificación exacta; es efecto de
la captura, no de la banda.

Solidez: en 1 047 489 muestras de existencia certificada (uniformes, densas en `t_c ± 0,1` y
vecinos binary64 de cada borde de banda), ninguna evaluación inválida y ninguna salida viva
indefinida. Antes del cierre de banda tampoco hubo ninguna violación.

El fallo cerrado de la última fila está justificado. En 4000 muestras de `t_c ± 0,1` la salida viva
está indefinida en 691 (crítica), 744 (mordedura) y 630 (penetración); la penetración no tiene
inexistencia, así que sus 630 son supresión con dos raíces delante de `P`, y la prueba de §3.5 lo
asevera en las tres configuraciones. Lejos de `t_c` la salida viva suele estar definida, pero la cota
a priori del redondeo del cálculo heredado, `2·√dis·|δt|/R² ≈ 1,74·10⁻⁵`, supera la tolerancia
`10⁻⁵` de la prueba de incidencia: la garantía no puede demostrarse con los datos capturados.

### 3.5 Pruebas

| Prueba (`PreG9BCu2HybridCertificationTest`) | Qué demuestra | Sobre `39741a0b` |
|---|---|---|
| `theUnlabelledSecondOutputKeepsItsStructureUnderTheTransforms` | V-INV-2 con `lr2`: 24 transformaciones (las de la fila en las unidades de la matriz, obligatorias, y con múltiplos de 125, complementarias; `10⁶`; `2⁻⁹` y `1024`; `10⁻²` y `10³`; `(1000,1; −999,7)`), tres configuraciones. Donde las constantes capturadas conservan los tipos de raíz, el descriptor es el de referencia (69 de 72 casos; los otros 3 son la crítica con constantes redondeadas: con escala `10⁻³` su raíz doble se separa en dos raíces simples, y con escala `10⁻²` y traslación `10³` o con traslación `(1000,1; −999,7)` desaparece); toda banda tiene la causa tipada; todo lo certificado concuerda con la referencia de 60 cifras; con múltiplos de 125 las bandas miden menos de `10⁻⁴` | falla: `CRITICAL unit 1, scale 1, translation (1000, 1000)`, descriptor distinto |
| `theUnlabelledSecondOutputFailsClosedWhereTheInheritedComputationIsNotDecisive` | el caso obligatorio de escala `10⁻³` y traslación `10³`: `NOT_ESTABLISHED`, bandas con la causa tipada, nada contradicho por la referencia y supresión viva donde hay dos raíces delante de `P` | pasa (la banda de T3 ocupaba todo el dominio) |
| `everyCertifiedSampleOfTheUnlabelledSecondOutputIsDefined` | solidez adversaria en 9 transformaciones × 3 configuraciones, con los vecinos binary64 de cada borde de banda | pasa |
| `theCapturedCircleCarriesTheHalfAxisOfTheInheritedIncidenceTest` | el operando circunferencia lleva `h0` bit a bit y el punto medio de la matriz; trasladada, `h0²` difiere de `R²` | falla: `ArrayIndexOutOfBoundsException` (el operando tenía tres valores) |
| `anUnlabelledSecondOutputLeavesLocalComponentsAroundABand` (actualizada) | la banda de V-COINC-2, 5 y 7 queda a menos de `10⁻⁴` de las raíces coincidentes y hay existencia certificada en `t_c − 0,01`; un único componente local | falla: banda ancha de T3 |
| `theCentredFormEnclosesTheReference` (actualizada) | la propiedad de la banda antigua se sustituye por la de la nueva: en una caja certificada fuera de la banda, `R2` viva está definida en cada muestra | pasa |

`PreG9BCu2ClassV3ModelTest` solo adapta los dos programas sintéticos al operando de cuatro valores.
Sobre `39741a0b` falla también `twoRunsOfTheSyntheticConstructionsGiveIdenticalResults` (V-DET-1):
no es atribuible a R7, porque la versión confirmada en `f50a03439`, sin ningún cambio de R7, falla
igual sobre ese árbol; depende del comportamiento del descendiente.

Las pruebas de esta sección y la de §2.3 están en `PreG9BCu2HybridCertificationTest`, que ya
contiene V-INV-2; no se ha añadido ninguna clase. La de §2.3 va en su propio commit, sin cambio de
producto. El inventario JUnit se regenerará al congelar el candidato, como para los métodos nuevos de
R1, R2, R4 y R6.

## 4. Precisiones aplicadas

- **F-CU2-1** (diseño de certificación, §4.1, «Semántica de los predicados»): la ventana de tangencia
  no solo amplía la definición; el texto de §2.1 sustituye a la nota de T13.
- **Banda** (mismo apartado): el predicado de §3.3 para la familia capturada; la banda de T3 para el
  resto.
- **V-INV-2** ([matriz](pre_g9b_cu2_validation_matrix.md), fila y trazabilidad): transformaciones en
  las unidades de la matriz obligatorias y múltiplos de 125 complementarios; estructura conservada
  donde está rigurosamente establecida (constantes capturadas que conservan los tipos de raíz);
  fallo cerrado tipado con evidencia donde la definición efectiva heredada cambia o la garantía no
  puede demostrarse, nunca una estructura certificada distinta ni incorrecta; efecto documentado de
  las constantes binary64 sobre las relaciones ideales; salidas etiquetadas y sin etiqueta.

No se ha modificado ningún texto normativo de `geocedg/specs`. §10.3 del modelo de construcción
certificada pide una banda «bounded by conservative first-level predicates»; el predicado de §3.3 es
conservador y solo lee envolventes de primer nivel (la recta por un punto constante y el conductor y
la circunferencia capturada) con cotas a priori del cálculo heredado. Si el autor prefiere que el
texto lo diga, la precisión editorial mínima sería «bounded by conservative predicates of the
inherited computation on first-level inputs»; no se ha aplicado.

## 5. Conservación de garantías

- H-T13-A: `PreG9BCu2SelectionSettlementTest` pasa entero; la copia sigue reproduciendo el
  etiquetado de su revisión.
- `CU0` y `CU1`: las pruebas de referencia de ambas fases pasan, en el núcleo compartido y en
  escritorio (§8); ningún valor de referencia ha cambiado.
- Puertas de `CU2`: GC-4 (presupuesto de cajas) sigue aseverada en
  `PreG9BCu2HybridCertificationTest`; las certificaciones por revisión no cambian (GC-2, GC-3).
- Selección y fidelidad: no cambia ninguna regla de selección ni de fidelidad; las pruebas de fidelidad
  del corte y de asentamiento pasan.
- Formatos: ninguna huella, firma de revisión, XML ni identidad persistente cambia. El operando
  circunferencia es material del certificado.

## 6. Limitaciones residuales

1. **Escala `10⁻³` con traslación `10³` en las unidades de la matriz.** `lr2` falla cerrado en todo el
   dominio (§3.4). Es la limitación del evaluador heredado que describe §3.4 del registro de R6; la
   banda no la reduce.
2. **Otras familias.** Las intersecciones recta–circunferencia que no son de la familia capturada
   (recta que no pasa por un punto constante y el conductor, circunferencia dependiente del
   conductor) conservan la banda de T3, que sigue creciendo con la distancia al origen.
3. **Cotas a priori.** La cota del redondeo del conductor (`2⁻⁴⁹` por las magnitudes) es unas 13 veces
   la observada; en la configuración crítica fija la semianchura de la banda (`1,6·10⁻⁷` rad) a
   través del margen de `D`. Las cotas de `w` y `d` suman magnitudes y no siguen la cancelación del
   cálculo.
4. **Cierre de la banda.** Retira la existencia certificada de tramos de anchura menor que la
   resolución de banda entre dos celdas de la misma banda; en las configuraciones probadas, de
   `10⁻¹⁰` a `10⁻⁹` rad.
5. **Valores efectivos S1.** Una salida etiquetada en el régimen S1 publica el pie de la
   perpendicular, a menos de `√(10⁻⁵)·R` de la raíz exacta; es la definición efectiva heredada y no
   se corrige.
6. **Observación no confirmada.** Por lectura de código: en la primera inicialización, la selección
   por compactación cuenta las raíces que están en el camino con la tolerancia heredada de `10⁻⁵`,
   mientras el modelo de cajas las cuenta con el predicado exacto; en una franja de esa anchura junto
   a una transición del parámetro del rayo, la raíz seleccionada podría diferir. Está fuera del
   perímetro de R7 y no se ha sondeado; se propone tratarla con R5 (grupo G3).

## 7. R5

R5 sigue pendiente; esta decisión no autoriza los 34 cambios. El
[inventario](pre_g9b_cu2_r5_gap_inventory.md) §7 presenta el contenido completo de G1 a G4 y la
propuesta de resolución separados en refuerzo de pruebas, corrección de producto, modificación
normativa y deficiencias de trazabilidad, con el efecto de R7 sobre G3. V-DET-1 queda abierta hasta
demostrar su cobertura conforme al contrato.

## 8. Ejecuciones

Todas son focales o de módulo; los registros de consola están en el directorio temporal de la
sesión, fuera del árbol.

| Ejecución | Resultado |
|---|---|
| sonda de solidez: 3 configuraciones × 14 transformaciones, 1 047 489 muestras certificadas | 0 evaluaciones inválidas, 0 salidas vivas indefinidas |
| sonda instrumentada del predicado (condición que falla en cada caja) en las transformaciones extremas | §3.3 y §3.4 |
| `:shared:common-jre:test`, batería completa | 724 clases, 7225 pruebas, 0 fallos, 10 omitidas |
| `:desktop:desktop:test`, `*PreG9BCu0*`, `*PreG9BCu1*`, `*PreG9BCu2*`, `*PreG9BS1*` | 17 clases, 80 pruebas, 0 fallos |
| Checkstyle `:shared:common:checkstyleMain` y `:shared:common-jre:checkstyleTest` | 0 avisos (un aviso de distancia de declaración en una prueba nueva, corregido) |
| `PreG9BCu2HybridCertificationTest` sobre el árbol de `39741a0b` | 4 de 25 fallan: las tres de §3.5 marcadas «falla» y V-DET-1, preexistente |
