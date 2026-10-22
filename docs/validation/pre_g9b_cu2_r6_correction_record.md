# PRE-G9B-CU2 — Registro de la corrección D-CU2-T13-R6

| Campo | Valor |
|---|---|
| Carácter | Registro de corrección del descendiente correctivo de `CU2`. No es evidencia de aceptación ni aprobación; `selfApproved=false` |
| Origen | Decisión autoral D-CU2-T13-R6: opción (a), con alcance controlado, para los hallazgos H-T13-A y H-T13-B de la [revisión de trazabilidad](pre_g9b_cu2_traceability_review.md) §9 |
| Base | rama local `phase/pre-g9b-cu2-corrective`; candidato congelado de T13 `39741a0b`; corrección de H-T13-A en el commit `e9541c42e` |
| Resultado | H-T13-A corregido (§2). H-T13-B detenido antes del código por un contraejemplo (§3.4), sin cambio de producto ni de prueba. Hallazgo nuevo H-T13-C (§4), anterior al descendiente |
| Decisiones pendientes | D-CU2-T13-R7 (§5) y D-CU2-T13-R5 ([inventario de los 34 huecos](pre_g9b_cu2_r5_gap_inventory.md)) |
| Verificación | pruebas focales y regresiones `PreG9BCu2*` y `PreG9BCu1*` (§6); ninguna puerta integrada de aceptación, como pide la decisión mientras el candidato siga cambiando |

## 1. Alcance

La decisión autoriza corregir H-T13-A y H-T13-B en este descendiente antes de congelar un candidato
nuevo, con el cambio mínimo, en el núcleo compartido y en los mecanismos existentes de
reconstrucción, selección y certificación. Pide además, antes del código de H-T13-B, una
caracterización matemática breve del predicado actual y de la alternativa, y detener esa
corrección si una limitación intrínseca del evaluador binary64 impide cumplir el contrato vigente.
Eso ha ocurrido (§3.4). Al caracterizar la banda apareció además H-T13-C (§4), un defecto de las
variantes etiquetadas que queda fuera del perímetro de R6.

## 2. H-T13-A — etiqueta pendiente de la segunda salida

### 2.1 Causa demostrada

GeoGebra solo etiqueta objetos visibles. `GeoElement.setLabel` sobre una salida indefinida guarda
el nombre y marca `labelWanted` (`GeoElement.java:2387`), y `updateGeo` pone la etiqueta la primera
vez que la salida es visible (`GeoElement.java:3151`). Un par recta–circunferencia creado donde su
segunda salida no está definida (hueco de una mordedura, raíz doble de la configuración crítica),
con las etiquetas `{Q, R}` o con la orden `Intersect(p, t)` del producto, etiqueta solo la primera
salida y deja pendiente la segunda.

La copia del evaluador, construida desde `Macro.buildMacroXML`, reproducía esa etiqueta pendiente.
Las consultas puntuales de la certificación y del locus publicado definen la segunda salida de la
copia en parámetros que el conductor vivo no ha visitado, y la copia la etiquetaba. Cuando un
consumidor del programa vuelve a validar la fidelidad (`LocusLength`, a través de
`StructuralLocusMetric2D`), el informe tipaba `LABELS live=10 copy=11`, la copia dejaba de ser fiel
y desde entonces toda evaluación del locus fallaba (`RECONSTRUCTION_FIDELITY_NOT_ESTABLISHED`),
mientras el certificado publicado seguía `COMPLETE` con existencia certificada. Recalcular no lo
reparaba, porque no había revisión nueva.

### 2.2 Estado del candidato anterior

El defecto existe en `39741a0b`. La sonda de la revisión da la misma salida con el árbol de ese
candidato, extraído con `git archive` y compilado aparte, y con el descendiente; el código implicado
no cambió entre los dos (R1 solo toca el linaje). Las dos pruebas nuevas de §2.5, copiadas a ese
árbol, fallan en la propiedad corregida:

- `aPendingLabelIsReproducedForTheWholeRevision`:
  `40+2^(-10) labels ==> expected: <Q;-;> but was: <Q;A;>`. Tras las consultas publicadas, la copia
  ha etiquetado su segunda salida.
- `aCopyWhoseLabelsDoNotCorrespondFailsClosed`: `expected: <false> but was: <true>`. El candidato
  anterior solo comparaba qué salidas tienen etiqueta, así que declara fiel una copia que
  intercambia las etiquetas de sus dos salidas y le captura un programa.

### 2.3 Contrato preservado

- `locus-v2-certified-construction-model.md` §10.2: el corte reproduce el estado de selección de
  cada revisión, incluido el etiquetado de las salidas; ese estado nunca se escribe en un algoritmo
  ni se sustituye por una heurística geométrica.
- §10.3: la disponibilidad de la salida en el corte es un hecho distinto de la existencia de la
  raíz; la corrección la mantiene donde el certificado afirma existencia.
- Regla de fidelidad de `locus-v2-semantics.md`: un corte cuya fidelidad no se establece no da
  programa. El fallo cerrado se conserva para las discrepancias reales (§2.5, prueba negativa).
- No se escribe la permutación del algoritmo, no cambia el algoritmo heredado ni la semántica de
  `LocusLength` (su valor es bit a bit el de la creación con las dos etiquetas), no se inventa
  ninguna salida definida y no cambia ningún formato persistido.

### 2.4 Cambio mínimo aplicado (commit `e9541c42e`)

- `ReconstructibleLocusEvaluator2D.withholdPendingLabels()` (`ReconstructibleLocusEvaluator2D.java:196`
  y `:1103`): al construir la copia, ninguna salida sin etiqueta conserva una etiqueta pendiente. La
  copia mantiene así el etiquetado de su revisión hasta la siguiente.
- `ReconstructionFidelity2D.labelState` (`ReconstructionFidelity2D.java:586`): el etiquetado se
  compara salida a salida y etiqueta a etiqueta (`Q;-;` frente a `Q;A;`), no solo por qué salidas
  tienen etiqueta.

Cuando la salida viva recibe su etiqueta, el XML del algoritmo cambia; esa parte estructural de la
huella publica una revisión nueva, que reconstruye la copia con las dos etiquetas. La prueba de §2.5
lo asevera. La causa tipada de esa revisión es `STRUCTURE`, no `LABELS`; lo trata la fila
V-REV-EXACT-7 del inventario R5.

### 2.5 Pruebas

| Prueba | Clase | Qué demuestra | Sobre `39741a0b` |
|---|---|---|---|
| `aPendingLabelIsReproducedForTheWholeRevision` | positiva y de regresión | radios `40 + 2⁻¹⁰` (conductor en el hueco) y `40` (raíz doble); par creado con `{Q, R}` y con `Intersect(p, t)`; variante sin etiqueta `lr2` en la misma construcción; `LocusLength` creado después de las consultas. En los estados creado, reabierto, recalculado, deshecho y rehecho: etiquetado de la copia igual al vivo salida a salida tras revalidar la fidelidad como lo hace un consumidor; salida publicada en todo parámetro de existencia certificada; certificado y longitud iguales bit a bit a los de la creación con las dos salidas etiquetadas. Al definir el conductor vivo la segunda salida, esta recibe su etiqueta y se publica una revisión nueva; tras asentarse la selección, el certificado vuelve a ser el de la creación etiquetada | falla |
| `aCopyWhoseLabelsDoNotCorrespondFailsClosed` | negativa | una copia cuyas dos salidas intercambian sus etiquetas tras validarse su revisión conserva qué salidas están etiquetadas; la validación siguiente tipa `LABELS`, no captura programa y la evaluación falla cerrada | falla |

Las dos pruebas están en `PreG9BCu2SelectionSettlementTest`, que ya pertenece a la selección de
`CU2`; no se ha añadido ninguna clase. El inventario JUnit se regenerará al congelar el candidato,
como para los métodos nuevos de R1, R2 y R4.

### 2.6 Resultado

| Ejecución | Identidad | Resultado |
|---|---|---|
| `:shared:common-jre:test`, `*PreG9BCu2*` y `*PreG9BCu1*` | `e9541c42e` | 16 clases, 163 pruebas, 0 fallos |
| `:desktop:desktop:test`, `*PreG9BCu2*` | árbol de trabajo con el producto de `e9541c42e` | 9 clases, 52 pruebas, 0 fallos |
| Checkstyle `:shared:common:checkstyleMain` y `:shared:common-jre:checkstyleTest` | árbol de `e9541c42e` | 0 avisos en los ficheros cambiados |
| las dos pruebas de §2.5 sobre el árbol de `39741a0b` | `39741a0b` más las pruebas nuevas | 2 de 2 fallan (§2.2) |

### 2.7 Limitaciones residuales

1. La versión de la regla de fidelidad sigue siendo `reconstruction-fidelity/v1`, aunque la
   comparación del etiquetado es más estricta. Ningún artefacto guardado depende de esa cadena; la
   versión entra en la firma del evaluador y cambiarla alteraría las huellas. Queda a criterio del
   autor.
2. La revisión que publica un cambio de etiquetado se tipa `STRUCTURE` (V-REV-EXACT-7, R5).
3. Las pruebas usan la configuración sintética. El caso equivalente del modelo del autor (crear
   `{Q, R}` en `t_c`, V-COINC-3) es una corrección propuesta en el inventario R5.
4. Si una copia publicada dejara de ser fiel por una causa no prevista, las evaluaciones fallarían
   cerradas mientras el certificado, calculado con la copia validada al construirla, se mantiene. Es
   la regla general de fallo cerrado; no se ha observado tras la corrección.

## 3. H-T13-B — banda de la segunda salida sin etiqueta

### 3.1 Notación

La recta viva `g = (a, b, c)` sale de `GeoVec3D.lineThroughPoints(P, N)`, en binary64:
`a = Py − Ny`, `b = Nx − Px`, `c = Px·Ny − Py·Nx`. La circunferencia `p` tiene la matriz capturada
`(1, 1, A2, 0, A4, A5)`, centro `m = (−A4, −A5)` y `R² = A4² + A5² − A2`. El modelo de cajas usa
`D = (a² + b²)·R² − s²`, con `s = a·mx + b·my + c` el valor de la recta en el centro; así
`D = (a² + b²)·(R² − h²)`, con `h` la distancia de `m` a `g`.

### 3.2 Predicado actual (T13)

`ConstructionBoxModel2D.requireOutsideCoincidenceBand` (`ConstructionBoxModel2D.java:1215`)
certifica una caja fuera de la banda solo si

`D_inf > 10⁻³·máx(a², b², c²)_sup` y `D_inf > 10⁻¹⁰·(s² + R²·(a² + b²))_sup`.

Cubre la decisión heredada de tangencia para todo valor del reintento y toda normalización:
`|dis| < 10⁻⁵`, con `dis = λ²·D` y `λ·máx(|a|, |b|, |c|) ≥ 1/2` tras normalizar, implica
`|D| < 4·10⁻⁵·máx(a², b², c²)`, y la banda toma un factor 25 más. Es conservador, pero está mal
condicionado. Dividido por `a² + b²`, exige `R² − h² > 10⁻³·máx(a², b², c²)/(a² + b²)`. Como
`c²/(a² + b²) = δ_O²`, la distancia del origen a `g`, la banda contiene todo parámetro con
`R² − h² ≤ 10⁻³·δ_O²`. Crece con el cuadrado de la posición absoluta de la recta y, desde
`δ_O ≥ √1000·R ≈ 31,6·R`, ocupa todo el dominio donde la recta corta la circunferencia: es lo
observado en H-T13-B.

### 3.3 Conjunto heredado de supresión

`AlgoIntersectLineConic` normaliza la recta multiplicando por 2 mientras los tres coeficientes
están por debajo de 1/2, o dividiendo mientras los tres superan 2 (`GeoLine.java:1620`): queda
`(x, y, z) = 2^k·(a, b, c)`, con `k = 0` si `|a|, |b| ≤ 2` y `|c| ≥ 1/2`. Después
(`AlgoIntersectLineConic.java:646`, `:698`), con `eps = 10⁻¹⁵, 10⁻¹⁴, …, 10⁻⁶` (diez intentos: el
producto acumulado que seguiría, `10⁻¹⁵·10¹⁰` en binary64, supera `10⁻⁵`):

- `p` es el corte de la recta con un eje, `v = (y, −x)`, `u = |v|² = 4^k·(a² + b²)`,
  `d = (p − m)·v`, `w = |p − m|² − R²` y `dis = d² − u·w = u·(R² − h_p²)`, con `h_p` la distancia
  de `m` a la recta por `p` con dirección `v`;
- `delta = mín(10⁻⁵, máx(1, |2d| + u + |w|)·eps)`; si `|dis| < delta`, las dos soluciones son el
  pie de la perpendicular (tangente); si no y `dis > 0`, dos raíces;
- `testPoints` conserva un punto si cumple las pruebas de incidencia de la circunferencia y de la
  recta completa con tolerancia `10⁻⁵`; si no conserva ninguno, se repite con un `eps` diez veces
  mayor y, agotados los intentos, las dos salidas quedan indefinidas;
- las salidas siguen la permutación fija o la compactación, y la pertenencia al rayo admite
  `−10⁻⁵`; por último, `avoidDoubleTangentPoint` (`AlgoIntersect.java:40`) anula la salida 2 sin
  etiqueta si coincide con la 1 dentro de `10⁻⁸` en cada coordenada.

Por tanto, donde `D > 0` y la raíz seleccionada está en el camino, la salida 2 sin etiqueta puede
quedar indefinida por cuatro vías:

- **S1**: tangente en el intento que conserva un punto, y las dos salidas son el mismo pie;
- **S2**: raíces calculadas a menos de `10⁻⁸` en cada coordenada;
- **S3**: ningún intento conserva un punto, y las dos salidas quedan indefinidas, también las
  etiquetadas;
- **S4**: la raíz seleccionada falla la incidencia y la otra no; afecta igual a una salida
  etiquetada.

En el primer intento, la ventana en unidades geométricas es
`R² − h_p² < delta/u ≈ 10⁻¹⁵·máx(1, |2d| + u + |w|)/u`. Depende de la posición absoluta, a través de
`|w| ≈ |p − m|²`, y de la normalización absoluta, a través de `u`. S3 aparece cuando esa ventana
supera la tolerancia de incidencia del pie, del orden de `10⁻⁵·R²`, es decir, con
`10⁻¹⁵·|w|/(u·R²) ≳ 10⁻⁵`.

### 3.4 Contraejemplo: el conjunto heredado no es invariante

Configuración de penetración de la matriz §1 (`g1` de radio `40 − 2⁻¹⁰`), escalada por `10⁻³` y
trasladada `(10³, 10³)` en las unidades de la matriz. En `t = 0.92729521800161`, cerca de `t_c`, la
recta viva es `(6,300·10⁻², 8,400·10⁻², −1,470·10²)`. No se normaliza (`k = 0`), así que
`u = 1,1025·10⁻²`, `w = 1,5626·10⁶` y `dis = 2,547·10⁻¹⁰`, mientras
`delta(10⁻¹⁵) = 1,563·10⁻⁹`. La decisión es «tangente» en los diez intentos. El pie dista del
círculo un `3,70·10⁻⁵` relativo y falla la incidencia, así que las dos salidas quedan indefinidas,
aunque las raíces exactas distan `3,04·10⁻⁴`, el 1,2 % del radio `0,025`. La ventana relativa
predicha, `10⁻¹⁵·|w|/(u·R²) = 2,27·10⁻⁴`, coincide con el mayor margen relativo suprimido observado,
`2,30·10⁻⁴`. Sin transformar, en el mismo `t`, `dis = 0,250` frente a `delta = 4,85·10⁻¹³`: secante.

En 47 200 muestras (7200 uniformes y 40 000 en `t_c ± 0,1`), `R2` vivo está indefinido en 6348
donde hay dos raíces delante de `P`, con márgenes relativos de `3,70·10⁻⁵` a `2,30·10⁻⁴`. Sin
transformar no lo está en ninguna. Un certificado fiel a §10.3 tiene que publicar ahí una banda (o
inexistencia) que la configuración sin transformar no tiene. **Ningún predicado correcto puede dar
a `lr2` la misma estructura con esa transformación, que V-INV-2 nombra literalmente** («traslaciones
de `10³` y escalas de `10⁻³` a `10³` de toda la construcción»). Por S3, lo mismo vale para `lq` y
`lr` (§4).

Con la base de la prueba de R4 (constantes multiplicadas por 125 para que las transformaciones sean
exactas en binary64), la supresión viva solo aparece en la raíz doble de la configuración crítica
(1 muestra con escala `10⁻³` y traslación `10³`; 2 con traslación `10⁶`), dentro de la banda que esa
raíz doble impone en toda transformación. En la mordedura y la penetración no aparece con ninguna de
sus transformaciones. Con las unidades de la matriz y traslación `(10³, 10³)`, la supresión crece al
reducir la escala:

| Escala | Crítica | Mordedura | Penetración |
|---|---|---|---|
| `1` | 0 | 0 | 0 |
| `10⁻¹` | 4 | 0 | 0 |
| `10⁻²` | 67 | 0 | 0 |
| `10⁻³` | 6731 | 4676 | 6348 |

### 3.5 Alternativa propuesta (no implementada)

Una caja estaría fuera de la banda solo si, para todo conductor binary64 de la caja, el primer
intento (`eps = 10⁻¹⁵`) del cálculo heredado es decisivo y separa las raíces:

1. `u ≥ eps`: no se entra en la rama degenerada;
2. `dis ≥ delta`: secante, no tangente;
3. la raíz seleccionada calculada pasa las pruebas heredadas de incidencia (circunferencia y recta
   completa con `10⁻⁵`, parámetro del camino `≥ −10⁻⁵`), así que no hay reintento;
4. las dos raíces calculadas difieren en más de `10⁻⁸`, más el redondeo de la comparación, en
   alguna coordenada.

Cada desigualdad se decide con cotas rigurosas. La parte exacta sale de las envolventes ajustadas de
la caja (`D`, `s`, `a² + b²` y las constantes capturadas). El redondeo de cada operación heredada se
acota con `γ_n = n·2⁻⁵³/(1 − n·2⁻⁵³)` sobre envolventes de magnitud, y el del conductor
(`Math.cos` y `Math.sin`, con error de 1 ulp como máximo, y la transformación afín) con `2⁻⁴⁸` por
las magnitudes. Si no son uniformes en la caja, se enumeran el exponente de normalización `k` y las
ramas (`|x| > |y|`, signo de `d`). La identidad clave evita la cancelación entre `d²` y `u·w`: el
valor exacto de `dis` para las entradas vivas `(a_f, b_f, c_f)` es

`4^k·[(a_f² + b_f²)·R² − (s_f − e_p)²]`,

con `s_f` el valor de la recta viva en el centro y `e_p` el residuo de `p` en ella
(`a_f·p_x + c_f` o `b_f·p_y + c_f`, con `|e_p| ≤ 2⁻⁵³·|c_f|`), y el `dis` calculado se aparta de
él como mucho en una cota de redondeo sobre las magnitudes de `d²` y `u·w`.

Alcance: rectas por un punto constante capturado y el conductor, y circunferencias constantes
capturadas (las entradas de primer nivel de §10.6); fuera de él seguiría el predicado de T13. La
prueba de incidencia necesita además el invariante de representación de la circunferencia capturada:
punto medio exactamente `(−A4, −A5)` y `|h0² − R²| ≤ 2⁻⁴⁸·(A4² + A5² + |A2|)`. Se cumple con
`setSphereNDMatrix` (`GeoQuadricND.java:321`; error `≤ γ3·(r² + mx² + my²)`) y con
`classifyConic` (`GeoConicND.java:2608`, `:2776`; error `≤ (γ3 + γ5)·(A4² + A5² + |A2|)`), y se
comprobaría en la captura, fallando cerrado si no se cumple.

Frente a los seis requisitos de la decisión:

1. La banda encierra S. Crece con la traslación solo como lo hace S, a través de `w` y de la
   normalización, nunca como `10⁻³·δ_O²`.
2. Las cotas son rigurosas.
3. Reproduce la ventana, sus reintentos, las pruebas de incidencia y la comparación de `10⁻⁸`, sin
   sustituirlas.
4. Solo deja fuera de la banda las cajas donde 1 a 4 están demostradas.
5. En otro caso, `UNRESOLVED` con la causa `UNLABELLED_COINCIDENT_OUTPUT`.
6. No cambia tolerancias globales, el algoritmo heredado ni la identidad de las raíces.

### 3.6 Estado

Por la cláusula de R6 («si una limitación intrínseca del evaluador binary64 impide satisfacer el
contrato vigente, presentar el contraejemplo y detener esa corrección para decisión autoral»), la
corrección de H-T13-B queda detenida antes del código. No cambia ningún fichero de producto ni de
prueba, y el predicado de T13 sigue vigente. V-INV-2 sigue sin la variante sin etiqueta hasta la
decisión de §5; no se ha excluido nada nuevo.

Según los sondeos, con la alternativa la variante sin etiqueta conservaría en las transformaciones
de la prueba de R4 la estructura de referencia, porque allí S queda confinado a la raíz doble crítica.
Es una predicción, no un resultado: nada se ha implementado.

## 4. Hallazgo H-T13-C — existencia certificada sin salida efectiva

En el régimen S3 de §3.3, la intersección heredada falla entera: las dos salidas quedan
indefinidas, también las etiquetadas, aunque `D > 0` y las raíces están en el camino. El modelo de
cajas certifica esos parámetros como existentes. La precisión de implementación F-CU2-1 del diseño
de certificación dice que la ventana de tangencia heredada es exceso de evaluación y nunca frontera
(«solo amplía la definición más allá del predicado exacto»). Es falsa cuando el pie falla la prueba
de incidencia, y contradice §10.3 de `locus-v2-certified-construction-model.md`: la existencia de la
raíz y la definición efectiva de la salida son hechos distintos, y «none is inferred from another».
Contradice también la garantía de §8 del
[registro de aprobación](pre_g9b_cu2_author_architecture_approval_record.md): «ninguna
disponibilidad numérica inferida de la existencia».

Contraste, con 20 000 muestras en `t_c ± 0,05`, entre el certificado publicado y la evaluación
publicada y la salida viva:

| Transformación (unidades de la matriz) | Configuración | `lq` y `lr`: existencia certificada | Evaluación inválida y salida viva indefinida |
|---|---|---|---|
| escala `10⁻³`, traslación `(10³, 10³)` | crítica | 19 790 | 5452 |
| | mordedura | 17 198 | 4303 |
| | penetración | 20 000 | 6312 |
| escala `2⁻⁹`, traslación `(1024, 1024)`, exacta | crítica | 20 000 | 403 |
| | mordedura | 17 204 | 205 |
| | penetración | 20 000 | 0 |
| escala `10⁻²`, traslación `(10³, 10³)`; sin transformar; base ×125 con escala `10⁻³` y traslación `10³` | las tres | de 17 204 a 20 000 | 0 |

En los dos primeros grupos, los certificados de `lq` y `lr` son `COMPLETE` y afirman existencia donde
el locus publicado no tiene punto. `lr2` no afirma nada falso, porque la banda de T13 ocupa allí todo
su dominio. La ventana relativa predicha separa los casos: `2,27·10⁻⁴` y `1,63·10⁻⁵` respectivamente.
Hay S3 donde el margen relativo cae entre `10⁻⁵` y ese valor, lo que no ocurre en la penetración
exacta, cuyo margen mínimo es `3,72·10⁻⁵`.

- **Preexistente**: el sondeo, ejecutado con los jars de `39741a0b`, da una salida idéntica byte a
  byte.
- **Alcance**: rectas cuya dirección no se normaliza (`|a|, |b| ≤ 2` y `|c| ≥ 1/2`: dirección corta
  lejos del origen) frente a circunferencias pequeñas. No aparece en las transformaciones que
  ejercen las pruebas de V-INV-2 ni en el modelo del autor con sus parámetros, cuyas pruebas
  V-COINC-9 comparan las salidas en las muestras. El modelo trasladado no se ha explorado.
- **No corregido**: está fuera del perímetro de R6 (H-T13-A y H-T13-B) y su corrección cambia la
  precisión F-CU2-1, aunque sea para cumplir §10.3. No se ha fijado en ninguna prueba.

## 5. Decisión requerida D-CU2-T13-R7

Hay que decidir dos cosas, ligadas.

**A. Corrección.**

| Opción | Contenido | Consecuencia |
|---|---|---|
| (a) | Corrección conjunta de H-T13-B y H-T13-C con la alternativa de §3.5. La banda de la salida sin etiqueta y, para toda salida de una intersección recta–circunferencia, la misma certificación del primer intento decisivo: si la raíz seleccionada no lo cumple, la caja queda `UNRESOLVED` con causa tipada (`TOLERANCE`, la de una tolerancia heredada que restringe la definición). Comprobación en la captura del invariante de la circunferencia. Requiere modificar la precisión F-CU2-1 | certificados fieles a la definición efectiva heredada; la banda deja de crecer con `δ_O²`; trabajo de implementación y de pruebas adversarias sobre todo el muestreo |
| (b) | Registrar H-T13-B y H-T13-C como deficiencias preexistentes, con propietario y plazo | los certificados etiquetados siguen afirmando existencia sin salida en el régimen S3 (no recomendado) |

**B. Alcance de V-INV-2.** Con cualquier certificado correcto, V-INV-2 no se puede cumplir
literalmente con escala `10⁻³` y traslación `10³` en las unidades de la matriz: la definición
efectiva heredada cambia allí (§3.4), también para las variantes etiquetadas. Los certificados
actuales solo conservan la estructura en ese caso porque son falsos (§4). Hace falta una precisión
autoral, que este descendiente no puede hacer. Por ejemplo:

- (i) admitir, donde la definición efectiva heredada cambia con la magnitud absoluta, el fallo
  cerrado tipado (`UNRESOLVED`), nunca una estructura certificada distinta, como
  `L-CU1-TRANSLATION-RESOLUTION` con `10⁶`;
- (ii) fijar V-INV-2 sobre la base exacta de la prueba de R4 (constantes ×125), donde la definición
  efectiva es invariante, y registrar la limitación de escala absoluta del algoritmo heredado.

Recomendación del agente: A(a) con B(i). Es la única combinación que elimina las afirmaciones falsas
sin cambiar el contrato por un resultado favorable.

## 6. Ejecuciones

Todas son focales. Los registros de consola están en el directorio temporal de la sesión, fuera del
árbol; las puertas de aceptación se ejecutarán sobre la identidad exacta del candidato que se congele.

| Ejecución | Resultado |
|---|---|
| `PreG9BCu2SelectionSettlementTest` (`:shared:common-jre:test`) | 14 pruebas, 0 fallos |
| `*PreG9BCu2*` y `*PreG9BCu1*` compartidas sobre `e9541c42e` | 16 clases, 163 pruebas, 0 fallos |
| `*PreG9BCu2*` de escritorio | 9 clases, 52 pruebas, 0 fallos |
| las dos pruebas de §2.5 sobre el árbol de `39741a0b` | 2 fallos, en la propiedad corregida |
| sondeo de la supresión viva: 3 configuraciones × 20 transformaciones × 47 200 muestras | §3.4 |
| repetición paso a paso del cálculo heredado en tres parámetros y siete transformaciones | §3.4 |
| contraste certificado–evaluación–salida viva (§4), con el descendiente y con los jars de `39741a0b` | §4; salida idéntica |

Los resultados y rechazos anteriores del registro de verificación de `39741a0b` (incluido el rechazo
`REJECTED_SEMANTIC` de INTEGRATION `7660aed9…` y su repetición aceptada `a22f4681…`) se conservan sin
cambios. El registro del candidato nuevo los citará junto a los suyos.
