# PRE-G9B-CU2 — Registro de la corrección autónoma controlada

| Campo | Valor |
|---|---|
| Carácter | Registro de corrección del descendiente correctivo de `CU2` bajo el mandato autoral de autonomía correctiva controlada, con la verificación de su candidato congelado (§12). No es aprobación; `selfApproved=false` |
| Origen | Mandato autoral «PRE-G9B-CU2 — Mandato autoral de autonomía correctiva controlada» de 2026-10-23: completar las correcciones pendientes, cerrar las conformidades exigibles, verificar el candidato y presentarlo para la aprobación final del autor |
| Base | rama local `phase/pre-g9b-cu2-corrective`; candidato congelado anterior `39741a0b44850a32258c2c73370fdc0842b8c666` (T13); correcciones R6 (`e9541c42e`) y R7 (`8d3e5ef0a`, `05a6abc18`, `a7ed313b4`) anteriores al mandato |
| Candidato | `2856f246c61bd51916932262002cdff31e61b565`, árbol `95d9f408e65c3a3cda7181043521a78e30b292ec`: candidato correctivo congelado, verificado en §12 |
| Resultado | cinco defectos de producto corregidos con contrato aprobado y violado (nivel B, §3 y §4); las 34 entradas de R5 tratadas (§5): 24 efectivas, 9 efectivas con una decisión de nivel C pendiente y 1 con residuo; verificación del cierre de bandas de R7 (§6); V-DET-1 con cobertura demostrable (§7) |
| Decisiones pendientes | doce decisiones de nivel C, consolidadas en §9 con alternativas y recomendación |
| Verificación | ejecuciones de desarrollo en §11; la verificación de aceptación del candidato congelado consta en §12, añadido por un commit documental descendiente: `STATIC`, `INFRA_UNIT`, las tres `PHASE` e `INTEGRATION` `ACCEPTED / COMPLETE` a la primera sobre `2856f246c`; referencias `preserve` de `CU0` iguales y bancos atribuidos |
| Estado | `CU2` no está `PASS`; nada se ha publicado, fusionado ni etiquetado; `geocedg_book` no se ha tocado |

## 1. Alcance y reglas aplicadas

El mandato distingue tres niveles. Nivel A, sin parada: pruebas de requisitos aprobados, arneses,
trazabilidad, pruebas adversarias, de regresión y de determinismo, documentación editorial,
registros, inventarios y contadores. Nivel B, con condiciones: defectos de producto donde el contrato
está aprobado y se viola, sin capacidad geométrica nueva, sin cambiar la semántica pública de
selección, identidad o persistencia ni los valores y políticas de `LocusLength`, sin debilitar
garantías, en la capa adecuada y con pruebas adversarias que demuestren el defecto y la corrección.
Nivel C, reservado al autor: formatos persistidos, semántica de selección de raíces, sustitución de
salidas indefinidas por límites, tolerancias globales o algoritmos heredados, capacidades nuevas,
debilitamiento de garantías de `CU0` y `CU1`, cambios del sentido normativo y modificación sustantiva
de contratos aprobados.

No se ha cambiado: ningún formato persistido ni identidad duradera; ninguna regla de selección ni
de fidelidad; ningún valor ni política de `LocusLength` (las longitudes de referencia de `CU0` y
`CU1` se comprueban con las puertas de §12); ninguna tolerancia global de GeoGebra ni el algoritmo
heredado de intersección; ningún texto normativo de `geocedg/specs`. No se ha debilitado ninguna
aserción ni reducido ningún conjunto de casos: las pruebas que se modificaron se refuerzan o se
dividen (V-TEX-1, §5).

## 2. Commits

| Commit | Nivel | Contenido |
|---|---|---|
| `b1475b7e1` | B | causa tipada de un `Point(L, u)` sin resolver (H-CU2A-1) |
| `8abf7e72b` | B | las imágenes por semejanza transforman la posición de las componentes puntuales (H-CU2A-2) |
| `7985ea62d` | A | contador y anchura del cierre de bandas de coincidencia (R7, observabilidad) |
| `baca968cc` | A | pruebas de las correcciones de nivel B, del cierre de bandas, V-CON-7 y V-LVL-1 |
| `225ce6112` | A | filas de exactitud de revisión de R5 (V-REV-EXACT-3, -7, -9, -10) |
| `ed47955b3` | B | un extremo incluido de un arco que cruza la costura se evalúa en su parámetro (H-CU2A-3) |
| `b34ec290d` | A | extremos en la costura, componentes puntuales y filas del grupo G1 |
| `0dfe916a2` | A | partes de modelo de V-REV-EXACT-3, V-B06-3 y V-B07-2 |
| `a05a835cd` | A | GC-1, GC-2 y GC-3 en el modelo; longitudes críticas de V-CON-3 |
| `2a1c29632` | B | la costura de un dominio periódico no es un extremo (H-CU2A-4) |
| `ce93fc5da` | A | pertenencia hasta la costura; V-CON-1, V-CON-3 y V-CON-4 en el modelo; V-LVL-2 |
| `af8875d01` | B | las etiquetas de dibujo nunca entran en la huella del corte (H-CU2A-5) |
| `40913b0f7` | A | grupos G2 y G3 de R5, prueba de la huella y sonda de compactación |
| `cb2ec4f67` | A | transición de V-COINC-3 de las variantes del modelo creadas en `t_c` |
| `2856f246c` | A | inventario JUnit ejecutado, huella del registro y fijaciones de la prueba de cobertura (§12.1); candidato congelado |

Los documentos de este registro, la matriz, la revisión de trazabilidad y el inventario de R5 van
en commits documentales propios: `109447733` antes de congelar y el descendiente documental de
§12 después.

## 3. Hallazgos

Gravedad: alta (resultado geométrico incorrecto publicado), media (fallo o pérdida de un resultado
correcto, sin falsa certificación), baja (diagnóstico o identidad de contenido). Ninguno es una
falsa certificación de existencia ni de inexistencia; H-CU2A-2 sí publicaba, en las imágenes por
semejanza, una envolvente certificada de la posición de una componente puntual que no la contenía.

| Identificador | Contrato afectado | Evidencia | Gravedad | Nivel | Corrección o decisión pendiente | Riesgo residual |
|---|---|---|---|---|---|---|
| H-CU2A-1 | `locus-v2-existence-components.md` §15.5 (un consumidor que direcciona por parámetro binary64 falla explícitamente con causa tipada); V-LIN-4, V-POINT-5 | `Point(L, u)` quedaba indefinido sin causa en un selector ambiguo, en una componente puntual o fuera de las componentes; sonda sobre `39741a0b`: ningún canal de causa | baja | B | corregido en `b1475b7e1`: `LocusSemanticPointCause2D` y `getResolutionCause()`/`getSelectorStatus()` de `AlgoSemanticLocusPoint2D` | la precisión «interseca» de V-LIN-4 (C-7) |
| H-CU2A-2 | `locus-v2-similarity-transformations.md`, nota de alcance A2.5 (transformar la posición o su envolvente y conservar procedencia y linaje); V-POINT-6 | las imágenes por las cuatro semejanzas públicas conservaban la envolvente de la posición de la fuente; sonda sobre `39741a0b`: posiciones idénticas a la fuente, exportadas y declaradas | alta | B | corregido en `8abf7e72b`: envolvente transportada con aritmética de intervalos hacia fuera (§4.1) | ninguno conocido |
| H-CU2A-3 | §15.3 (un extremo binario incluido pertenece al arco y se evalúa en su parámetro); V-CON-3, V-B07-2 | la canonicalización periódica movía unos ulp los extremos de los arcos que cruzan la costura fuera del arco: `OUT_OF_DOMAIN` y `LocusLength` `NUMERICAL_FAILURE` con radios `40 + 2⁻³⁰`, `40 + 2⁻⁴`, 41 y 45, también sobre `39741a0b` | media | B | corregido en `ed47955b3` (§4.2) | la longitud de los arcos con extremos de raíz cuadrada (O-CU2A-3, C-10) |
| H-CU2A-4 | §15 (todo parámetro del interior certificado tiene una evaluación válida; la inclusión de extremos no es una conveniencia de épsilon); diseño de consumo §4 (pertenencia exacta por coordenada de arco); V-CON-1, V-CON-4 | `SemanticGeneratorDomainProvider1D` aplicaba su tolerancia de extremo (`10⁻¹²`) al extremo excluido de su dominio periódico, la costura: los parámetros canónicos de `[π − 10⁻¹², π)` eran `OUT_OF_DOMAIN` dentro de un arco certificado y la raíz de una intersección en la costura de un arco se perdía (modelo a excCyl 3,7); igual sobre `39741a0b` | media | B | corregido en `2a1c29632` (§4.3) | las componentes cerradas periódicas conservan la pertenencia heredada con tolerancia (C-12) |
| H-CU2A-5 | `locus-v2-semantics.md` v1.3 (la huella nunca se basa en una representación gráfica); propósito de la exclusión de etiquetas diferidas; V-REV-EXACT-9, V-SEL-7 | la huella quitaba las etiquetas excluidas pero no su espacio: tras deshacer en el modelo, el ángulo sin etiqueta `(-α_{cyl})` perdía su color y el mismo contenido tenía otra huella; una condición de visibilidad del punto trazado publicaba una revisión `STRUCTURE`; igual sobre `39741a0b` | baja | B | corregido en `af8875d01` (§4.4) | ninguno conocido |
| O-CU2A-1 | §15.3; direcciones persistidas | la canonicalización periódica `lower + (t − lower) − ⌊…⌋·período` no es la identidad en el dominio fundamental (desplaza unos ulp; es idempotente en su imagen) | — | C | ninguna en este mandato; H-CU2A-3 evalúa los extremos incluidos sin recanonicalizar (C-11) | direcciones a unos ulp de la costura |
| O-CU2A-2 | V-REV-EXACT-7 (causa tipada) | el cambio de etiquetado de la segunda salida de un par etiquetado se tipa `STRUCTURE`: el XML del algoritmo lleva las etiquetas de sus salidas | — | C | C-1 | causa menos precisa que `LABELS` |
| O-CU2A-3 | `locus-v2-metrics.md` §25.1 y §25.7 (clase 4 con causa tipada; sin presupuesto universal) | la longitud total de un arco con extremos de raíz cuadrada (mordedura) es `NOT_ESTABLISHED` con `WORK_BUDGET_EXHAUSTED`: modelo a 3,7 (`o`, `v`, `w`, `a_1`) y casi todos los radios de mordedura sintéticos | — | C | C-10; la referencia independiente consta en §5 (V-CON-3) | sin longitud de las mordeduras |
| O-CU2A-4 | V-SEL-6, asentamiento | el estado de selección de la copia puede diferir del vivo siendo fiel (asentamiento por diseño) | — | — | ninguna | — |
| O-CU2A-5 | V-PER-3 | con rbCone 10 hay una isla certificada de unos `3·10⁻¹²` entre dos bandas junto a `t*` | — | — | ninguna: correcta, no canónica | — |
| O-CU2A-6 | registro de R7 §6.6 | sondeada: en primera inicialización la salida heredada sigue definida unos `5·10⁻⁷` del conductor más allá de la transición exacta del rayo, en inexistencia certificada, y no se publica; nunca designa otra raíz | — | — | cerrada sin defecto; prueba `theCompactionOnARayStartLeavesOnlyAnUnpublishedExcess` | el exceso de evaluación heredado, como F-CU2-1 |
| TD-CU1-W1 | `CU1`, métrica | `w_1` llega a `PARAMETER_RESOLUTION_REACHED` con los conductores `2π·129/512`, `2π·9/32` y `2π·159/512`, también sobre `39741a0b` | — | deuda técnica | ninguna en `CU2`; ya descrita en el Javadoc de AME | huecos computacionales de `e_2` |

## 4. Correcciones de nivel B

### 4.1 H-CU2A-2: envolvente de la posición bajo una semejanza

Hipótesis: la envolvente fuente `[x₀, x₁] × [y₀, y₁]` contiene la posición exacta. Las semejanzas
son afines, así que la imagen de la caja es un paralelogramo contenido en la caja de las imágenes de
sus cuatro vértices. Cada coordenada se calcula con aritmética de intervalos hacia fuera
(`nextDown`/`nextUp` en cada operación); `Math.cos` y `Math.sin` están a menos de 1 ulp del valor
exacto, y la envolvente se ensancha dos ulp para cubrir el cambio de binada. La reflexión en una
recta normaliza sus coeficientes con un error de `5u + O(u²)`, acotado por un ensanchamiento
relativo de `2⁻⁵⁰` más el espaciado subnormal. Límites: una imagen que desborda no tiene envolvente
(`UNAVAILABLE`). La prueba `theTransformedEnclosureHoldsTheExactImage` comprueba en 3 000 casos
aleatorios (cajas de `2⁻²⁰` a `2³⁰`, cocientes subnormales) que la envolvente contiene la imagen
exacta a 60 cifras de cada vértice y no es más ancha que la caja transformada salvo `10⁻¹²` relativo.

### 4.2 H-CU2A-3: extremos incluidos de un arco que cruza la costura

La pertenencia de un arco es exacta en su coordenada de arco; la canonicalización del proveedor no
es la identidad en el dominio fundamental. `LocusDefinition2D` evalúa ahora un extremo binario
incluido en su propio parámetro, sin recanonicalizar, por `evaluateComponentEnd` y por los extremos
elevados de `evaluateInComponent`. Los extremos son bit a bit los del vivo con el conductor allí.
La regla vale para todo extremo incluido de una componente que no es cerrada periódica, no solo en
la costura: en los extremos de raíz cuadrada de las mordeduras del modelo, el vértice dibujado del
extremo se desplaza del orden de 10⁻⁵ px (§12.3).

### 4.3 H-CU2A-4: la costura de un dominio periódico

Un dominio periódico es una circunferencia: su extremo excluido es la costura, identificada con el
extremo incluido, no un extremo. Todo parámetro canónico (imagen de la canonicalización, en
`[lower, upper)`) pertenece al dominio. La tolerancia del proveedor se aplica ahora solo al extremo
incluido de un dominio periódico y a los extremos de uno no periódico; la pertenencia a cada
componente la sigue decidiendo la estructura: un arco que cruza la costura por su coordenada de
arco exacta, una componente cerrada periódica y un intervalo por su intervalo con la tolerancia
heredada (diseño de consumo §4), de modo que los loci de `CU0` y `CU1` no cambian. Prueba adversaria:
`theCanonicalParametersOfAnArcBelongToItUpToTheSeam`, que falla sin la corrección con
`OUT_OF_DOMAIN` en `0x1.921fb5444244cp1`.

### 4.4 H-CU2A-5: etiquetas excluidas de la huella

Cada etiqueta de valor derivado y de dibujo diferido se elimina ahora con el espacio que la precede,
de modo que su presencia no cambia la parte de estructura. Las etiquetas excluidas, las cuatro partes
de la huella y su tipificación no cambian; la huella solo existe en memoria y conserva su versión.
Prueba adversaria: `drawingStateOfASliceElementPublishesNoRevision`, que falla sin la corrección.

## 5. R5: las 34 entradas

Clasificación del [inventario](pre_g9b_cu2_r5_gap_inventory.md), conservada. «Efectiva»: las pruebas
aseveran cada elemento esencial de la fila. Las decisiones C-n están en §9.

| Fila | Clase | Estado | Pruebas nuevas o reforzadas |
|---|---|---|---|
| GC-1 | GC | efectiva | `PreG9BCu2AuthorModelCertificationTest.driverMovesPublishNoRevisionAndComputeNoCertificate` |
| GC-2 | GC | efectiva para los certificados propios; anidados, C-4 | `PreG9BCu2AuthorModelCertificationTest.aNonDriverChangeComputesOneCertificatePerAffectedLocus`, `PreG9BCu2HybridCertificationTest.theShadowCertificateIsComputedOncePerRevision` |
| GC-3 | GC | efectiva | `PreG9BCu2AuthorModelCertificationTest.consumersReadThePublishedCertificateOnly` |
| GC-8 | GC | efectiva | presupuestos aseverados en V-PER-3 y en la clasificación de V-CRIT-9 |
| V-LVL-1 | F | efectiva | `PreG9BCu2HybridCertificationTest.exploratoryEvidenceHasNoAuthorityOverTheDefinition` |
| V-LVL-2 | F | efectiva | `PreG9BCu2HybridCertificationTest.exploratoryEvidenceIsNeitherPersistedNorARevisionSource` |
| V-UND-1 | F | efectiva: `UNRESOLVED` tipado, nunca inexistencia; la causa, C-2 | `PreG9BCu2HybridCertificationTest.aLocusOnALengthThatFailsIsNeverCertifiedNonexistent` |
| V-PER-3 | A | efectiva | localización independiente de los conductores degenerados de `o_1` |
| V-E2-5 | F | efectiva | solo los huecos causados por el fallo inyectado; los demás, fallos independientes de la métrica de `CU1` |
| V-REV-EXACT-3 | F | efectiva | sintético y modelo con `lr2`: bits de la copia y evaluación igual al vivo |
| V-REV-EXACT-7 | A | efectiva para la selección; etiquetado, C-1 | `PreG9BCu2SelectionSettlementTest.aRealSelectionTransitionPublishesARevisionWithItsTypedCause` |
| V-REV-EXACT-8 | F | efectiva | V-DET-1 ampliada; `PreG9BCu2AuthorModelCertificationTest.twoRunsOfTheVariantsAlternatingTheDriverGiveIdenticalResults` |
| V-REV-EXACT-9 | F | efectiva (con H-CU2A-5) | literal angular perdido; `drawingStateOfASliceElementPublishesNoRevision` |
| V-REV-EXACT-10 | A | efectiva | extremos negativos y cadena anidada |
| V-SEL-7 | F | efectiva | `aLivePermutationOneZeroIsReadAgainOnUndoRedoAndReopen`; `PreG9BCu2AuthorModelSelectionSettlementTest.theSelectionStateIsReadAgainOnUndoRedoAndReopen` |
| V-LIN-4 | A | efectiva (con H-CU2A-1); «interseca», C-7 | `PreG9BCu2LineageTest.anAmbiguousSelectorLeavesThePointUndefinedWithATypedCause` |
| V-COINC-2 | A | efectiva | `PreG9BCu2AuthorModelSelectionSettlementTest.theUnlabelledSecondOutputKeepsItsTypedBands` |
| V-COINC-3 | F | efectiva | `theVariantsCreatedAtTheDoubleRootKeepTheirComponents`; `theVariantsCreatedAtTheDoubleRootLeaveItWithATypedTransition` |
| V-COINC-5 | F | efectiva; redacción, C-6 | `theUnlabelledSecondOutputKeepsItsTypedBands` |
| V-COINC-7 | A | efectiva; redacción y muestreo, C-6 | `theUnlabelledSecondOutputKeepsItsTypedBands` |
| V-COINC-8 | F | efectiva | `theSelectionStateIsReadAgainOnUndoRedoAndReopen` (con recálculo) |
| V-POINT-4 | F | residuo: el fallo forzado de la forma límite, sin gancho (C-9) | vecinos de `t*` en ulp |
| V-POINT-5 | F | efectiva (con H-CU2A-1) | punto cerca de `t*`, declaraciones, consumidores |
| V-POINT-6 | F | efectiva (con H-CU2A-2) | `aSimilarityImageTransformsThePositionOfAPointComponent`, `theTransformedEnclosureHoldsTheExactImage` |
| V-CON-1 | F | efectiva (con H-CU2A-4) | `PreG9BCu2AuthorModelConsumerTest.theCertificateIsTheSingleClosureAuthority`, `theCanonicalParametersOfAnArcBelongToItUpToTheSeam` |
| V-CON-3 | F | efectiva; mordeduras no establecidas, C-10 | `theTotalLengthsOfTheCriticalLociMatchTheExactSweptArcs`, `theTotalLengthsOfTheFirstFamilyHaveTheirClass` |
| V-CON-4 | F | efectiva | `anIntersectionWithAnArcKeepsItsTokensThroughARevision` |
| V-CON-7 | F | efectiva | imágenes de la mordedura por los cuatro comandos públicos |
| V-B06-1 | F | efectiva | giro en el vértice de cierre, medido cíclicamente |
| V-B06-3 | F | efectiva | determinismo e invariancia de zoom y resolución en las mordeduras y en la crítica |
| V-B07-2 | F | efectiva (con H-CU2A-3) | tramos de raíz cuadrada de ambos extremos a 3,7, 3,85 y 3,9 |
| V-B07-4 | F | efectiva | evidencia exploratoria punteada y translúcida |
| V-AVAIL-6 | F | efectiva con la redacción propuesta; C-3 | `drawnVerticesAreTheEvaluationsOfTheirParameters` |
| V-TEX-1 | F | efectiva; orden e integración continua, C-5 | `thePgfDocumentCompiles`, `thePstricksDocumentCompiles`, `theAsymptoteDocumentCompiles`, `theCriticalLociAreClosedInLatexOnlyWhenPeriodic`; topología de los siete loci en V-CON-1 |

Cifras de V-CON-3. Configuración crítica sintética (radio 40): `lq` = 73,334059516061842, referencia
exacta (dos tangentes y un arco, 60 cifras) 73,334059516392240, error `3,3·10⁻¹⁰` frente a la cota
establecida `9,9·10⁻⁹`; `lr` = 146,12985081782818, referencia 146,12985081832106, error `4,9·10⁻¹⁰`
frente a `1,48·10⁻⁸`; clase 3 (`ESTIMATED`). Modelo a excCyl 2,9 (ciclos cerrados, clase 3): errores
de `1,4·10⁻¹¹` a `2,2·10⁻¹¹` frente a cotas de `5,7·10⁻¹⁰` a `9,0·10⁻¹⁰`, con la extrapolación de
Richardson de poligonales de 2¹² y 2¹³ cuerdas como referencia. Modelo a 3,7 (arcos):
`NOT_ESTABLISHED`, `WORK_BUDGET_EXHAUSTED` (`MAXIMUM_DEPTH`); referencias independientes
(poligonales agrupadas como un coseno en los extremos): `o` 8,79997490659, `v` 8,90928240599, `w`
8,50989552286, `a_1` 9,77622428265.

V-COINC-2: muestras indefinidas de `R2` o de su copia (no fijadas): 51 en el último binary64 de
penetración, 201 en el primero de mordedura y 430 a 3,7; todas en banda, en un encierro de frontera
o en inexistencia certificada; 5 bandas tipadas.

## 6. R7: verificación del cierre de bandas

El cierre de bandas de R7 convierte en `UNRESOLVED` (banda tipada) tramos de existencia certificada
más estrechos que la resolución de banda entre dos celdas de la misma banda. Verificación: en 54
casos (configuraciones crítica, de mordedura y de penetración bajo las transformaciones de V-INV-2),
el cierre solo aparece en la configuración crítica; la mayor anchura de existencia certificada
retirada es `1,473·10⁻⁹` rad, y la de cada ejecución queda por debajo de `2π/256` (la resolución de
banda, aseverada por `theClosureOfACoincidenceBandOnlyWithholdsCertifiedExistence`). Efectos: solo
retira certificación local; nunca produce `CERTIFIED_DOES_NOT_EXIST` ni afirma inexistencia; las
componentes se reducen a sus partes certificadas; los consumidores (dibujo, exportaciones, métrica,
intersecciones y puntos) ven la banda. El contador `COINCIDENCE_BAND_CLOSURE` y
`Result.getClosedCoincidenceWidth()` lo hacen observable.

## 7. V-DET-1

La doble ejecución sintética (`twoRunsOfTheSyntheticConstructionsGiveIdenticalResults`) compara bit a
bit, en cada paso, la revisión, la huella, la rama publicada, raíces, evidencia exacta, puntos
estacionarios, cajas, claves y transiciones de linaje, eventos del corte, contadores de certificación,
de cortes y de revisiones, propios y anidados, y los cambios del corte por tipo, imágenes, puntos y
métricas; ahora también con un ulp del radio crítico de ida y vuelta con `lr2` y el conductor en la
raíz doble, salidas renombradas y los literales de V-REV-EXACT-4. En el modelo, la doble ejecución del
autor y la de las variantes alternando el conductor entre `t = −1` y `t_c` abren el mismo documento
guardado (identidades duraderas, nada se normaliza). Ninguna diferencia.

## 8. Matriz de conformidad

Las 133 filas de §2 y §3 de la [matriz](pre_g9b_cu2_validation_matrix.md): sus citas se regeneraron
con el método de T13 y se unieron a las anteriores (§6 de la matriz). Toda fila obligatoria de tipo
F, A y GC tiene al menos una prueba que la cita, salvo las de proceso (V-CU0-1 a V-CU0-3, V-CU1-1,
V-PROD-1 y V-PROD-2), que verifican las puertas y los registros, V-E2-2 (referencias `preserve`) y
V-DXF-4 (informativa). La revisión de trazabilidad (§10) da el estado de cada fila tras el mandato:
la única fila obligatoria con un elemento sin aseverar es V-POINT-4 (C-9).

## 9. Decisiones de nivel C

Se presentan juntas. La recomendación es la primera alternativa salvo indicación.

| Id | Asunto | Alternativas | Recomendación |
|---|---|---|---|
| C-1 | Causa del cambio de etiquetado (V-REV-EXACT-7, O-CU2A-2) | (a) aceptar `STRUCTURE` (el XML de la definición cambia) y precisar la fila; (b) excluir las etiquetas de salida de la parte de estructura para que lo tipe `LABELS` | (a) en `CU2` |
| C-2 | Causa `COMPUTATIONAL_DEPENDENCY_FAILURE` sin emisor (V-UND-1) | (a) declararla reservada; (b) emitirla donde un fallo de cálculo de una dependencia decide la existencia | (a): §15.5 no la exige por su nombre y los fallos ya se tipan |
| C-3 | Clase de las coordenadas dibujadas (V-AVAIL-6) | (a) precisar la fila: «coordenadas iguales bit a bit a las del evaluador, sin mejorar su clase» (ya probado); (b) propagar la garantía numérica al dibujo | (a) |
| C-4 | Certificaciones anidadas por revisión (GC-2) | (a) regla: por revisión publicada, un cálculo propio y uno por cada copia anidada reconstruida; (b) compartir certificados entre el vivo y sus copias | (a) |
| C-5 | Orden de compilación y herramientas TeX (V-TEX-1) | (a) la fila nombra `pdflatex -halt-on-error`, `latex` y `dvips`, y `asy -noV -f pdf`; sin herramientas las pruebas se omiten visiblemente y la verificación local compila; (b) instalar TeX y Asymptote en la integración continua | (a) |
| C-6 | Redacción de V-COINC-5 y V-COINC-7 (F-CU2-1) | (a) la salida etiquetada está definida más allá del extremo exacto (exceso de evaluación), la segunda sin etiqueta tiene su banda tipada, y V-COINC-7 muestrea a 3,7 alrededor de `t_c` y de los extremos analíticos del hueco; (b) mantener el texto | (a) |
| C-7 | «Interseca» en V-LIN-4 | (a) «contiene el parámetro dentro de la tolerancia del dominio, entre hijas de una división»; (b) mantener | (a) |
| C-8 | §10.3 del modelo de construcción certificada | (a) precisión editorial: banda acotada por predicados conservadores del cálculo heredado sobre entradas de primer nivel (registro de R7 §4); (b) mantener | opcional |
| C-9 | Fallo forzado de la forma límite (V-POINT-4) | (a) un gancho de fallo de prueba, de visibilidad de paquete, en el evaluador de la forma límite; (b) aceptar la cobertura por revisión de código y por los vecinos | (a), si el autor admite un gancho de prueba en el producto |
| C-10 | Longitud de los arcos con extremos de raíz cuadrada (O-CU2A-3) | (a) limitación tipada en `CU2` y un estimador con cambio de variable en las transiciones de raíz cuadrada certificadas en `CU3`; (b) enmendar §25 ahora | (a) |
| C-11 | Canonicalización periódica (O-CU2A-1) | (a) mantener; (b) hacerla la identidad en el dominio fundamental (afecta a las direcciones persistidas) | (a) |
| C-12 | Costura de las componentes cerradas periódicas (residuo de H-CU2A-4) | (a) mantener la pertenencia heredada con tolerancia (diseño §4; referencias de `CU0` y `CU1` intactas); (b) pertenencia exacta hasta la costura también para ellas | (a) en `CU2` |

## 10. Deficiencias residuales

- V-POINT-4: sin fallo forzado de la forma límite (C-9).
- Longitud de las mordeduras no establecida (O-CU2A-3, C-10).
- Ventana `[π − 10⁻¹², π)` de las componentes cerradas periódicas (C-12).
- Exceso de evaluación heredado en los extremos de rayo y de tangencia (F-CU2-1, O-CU2A-6): nunca
  publicado.
- Deuda de `CU1`: `w_1` con `PARAMETER_RESOLUTION_REACHED` en tres conductores (TD-CU1-W1).
- Limitaciones residuales de R7 (registro de R7 §6), sin cambio.

## 11. Ejecuciones de desarrollo

Focales y de módulo; los registros de consola están en el directorio temporal de la sesión, fuera
del árbol.

| Ejecución | Resultado |
|---|---|
| `:shared:common-jre:test`, batería completa, tras H-CU2A-4 | 7 242 pruebas, 0 fallos, 10 omitidas |
| `:shared:common-jre:test`, batería completa, tras H-CU2A-5 | 7 245 pruebas, 0 fallos, 10 omitidas |
| `:desktop:desktop:test` de `PreG9BCu0*`, `PreG9BCu1*`, `PreG9BCu2*` y `PreG9BS1*`, tras H-CU2A-4 | 87 pruebas, 0 fallos |
| la misma selección, tras H-CU2A-5 | 93 pruebas, 1 fallo: la prueba nueva de Asymptote esperaba a un visor (`asy` sin `-noV`); corregida la prueba, `PreG9BCu2AuthorModelExportTest` 8/8 |
| pruebas nuevas, focales | todas pasan; las de H-CU2A-4 y H-CU2A-5 fallan sin la corrección |
| Checkstyle `:shared:common:checkstyleMain`, `:shared:common-jre:checkstyleTest`, `:desktop:desktop:checkstyleTest` | sin avisos en líneas del mandato; los 4 avisos de pruebas de escritorio son anteriores a `CU2` |

## 12. Verificación de aceptación del candidato congelado

Añadido por el commit documental descendiente, que no modifica el candidato ni es un candidato
nuevo.

| Comprobación | Resultado |
|---|---|
| Candidato | `2856f246c61bd51916932262002cdff31e61b565`, árbol `95d9f408e65c3a3cda7181043521a78e30b292ec`, en la rama local `phase/pre-g9b-cu2-corrective` (sin publicar) |
| Candidato anterior | `39741a0b44850a32258c2c73370fdc0842b8c666` (T13), árbol `0bfeb76da3ad3a470b9de61eec0650ea0895b3aa`, con su commit documental `5d2868500841d478e77465894b87e794ee6404c9` |
| Delta desde el commit documental de T13 | 27 commits (los anteriores al mandato, los del mandato de §2 y el de inventario); 47 archivos, 7 nuevos y 40 modificados, con 10 527 inserciones y 236 borrados. Producto: 21 archivos bajo `source/` fuera de `src/test`, 2 nuevos (`LocusSemanticPointCause2D`, `InheritedLineCircleBand2D`) y 19 modificados, con 1 336 inserciones y 66 borrados. Pruebas: 13 clases `PreG9BCu2*` modificadas, con 7 562 inserciones y 134 borrados; ninguna prueba eliminada. Resto, 13 archivos: las especificaciones `locus-v2-semantics.md` y `locus-v2-existence-components.md` (enmienda de D-CU2-T13-R1), el diseño de certificación híbrida (precisiones de R7), los cinco registros nuevos (revisión de trazabilidad, inventario de R5, registros de R6, de R7 y de este mandato), la matriz, `docs/upstream/modified-files.yml`, el inventario JUnit, el registro de verificación y su prueba de cobertura |
| Límite upstream | `Assert-GeoCeDGUpstreamBoundary` (base `9b93256b`) correcto: 1 162 archivos registrados en `docs/upstream/modified-files.yml` (1 160 en T13 y los dos archivos nuevos del descendiente) |
| Libro y gobernanza | `geocedg_book`, `AGENTS.md`, `CLAUDE.md`, `FIRST_AGENT_TASK.md`, `ai-shell/prompts/` y `.github/prompts/` sin cambios |
| Árbol de trabajo e índice | limpios antes de cada ejecución de aceptación |
| Publicación | `main` = `origin/main` = `48a0775e4f2f122f5e2614c62b5773777752ccd1`; el remoto no tiene ninguna rama de implementación ni correctiva de `CU2` |

### 12.1 Inventario de JUnit

El productor oficial `tools/agent/checks/gradle-test-evidence-producer.ps1` se ejecutó sobre el
árbol de `109447733`, cuyos archivos de prueba y de producto son los del candidato.

| Selección | Modo | Resultado | Duración |
|---|---|---|---:|
| `discovery.shared` | descubrimiento en seco | `COMPLETED`, código 0; 724 archivos y 6 331 casos | 1,6 min |
| `discovery.desktop` | descubrimiento en seco | `COMPLETED`, código 0; 190 archivos y 2 253 casos | 1,4 min |
| `pre-g9b-cu2.shared` | ejecución | 21 clases, 233 pruebas, 0 fallos, 0 omitidas | 1,7 min |
| `pre-g9b-cu2.desktop` | ejecución | 9 clases, 68 pruebas, 0 fallos, 0 omitidas | 5,8 min |
| `final.shared` | ejecución | 724 archivos, 7 248 pruebas, 0 fallos, 10 omitidas | 4,1 min |
| `final.desktop` | ejecución | 189 archivos, 2 247 pruebas, 0 fallos, 1 omitida | 21,2 min |

Las omitidas son las preexistentes y upstream de T13.

El actualizador oficial `tools/agent/update-verification-junit-inventory.ps1` se ejecutó en la
sesión con las dos evidencias de descubrimiento y las cuatro de ejecución:

- `discovery.shared`: 6 290 → 6 331, y `final.shared`: 7 207 → 7 248 (+41);
- `discovery.desktop`: 2 231 → 2 253, y `final.desktop`: 2 225 → 2 247 (+22);
- `pre-g9b-cu2.shared`: 192 → 233, y `pre-g9b-cu2.desktop`: 46 → 68: las pruebas que el
  descendiente correctivo añade, desde R1 hasta el mandato, en clases `PreG9BCu2*` existentes;
  ninguna clase nueva;
- las demás selecciones no cambian: ninguna otra selecciona una prueba de las clases modificadas.

La huella `junit_inventory` del registro cambia de
`6ba8034547fc9184d7e52fb5764e78c15f9860a9b7a551df6a38795cbdab5c2f` a
`07a6a7c8e9e58c5a1d13e45f28b06f1293fcef06e8d5e8cadfdff9b4e0f8fd1d`; la anterior se reprodujo antes
desde el archivo de `HEAD` con `Get-VerificationCanonicalTextSha256`. Las fijaciones de
`verification-final-coverage.Tests.ps1` para `pre-g9b-cu2.shared` y `pre-g9b-cu2.desktop` pasan de
192 y 46 a 233 y 68. Antes del commit, las 23 hojas de nivel `INFRA_UNIT` del registro (las 22 del
perfil y `infra.guide-structure-diagnostic`) se ejecutaron directamente sobre el árbol y pasaron.
El commit `2856f246c` congela el candidato.

### 12.2 Puertas de aceptación

Clase `INTEGRATED_PHASE` (D-CU2-12), con las puertas que fija el mandato. Ejecuciones secuenciales,
desde la raíz del repositorio, con el candidato como `HEAD` y el árbol de trabajo y el índice
limpios:

```text
& tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts\agent\pre-g9b-cu2a-static-2856f246
& tools/agent/verify.ps1 -Profile INFRA_UNIT -LogDirectory artifacts\agent\pre-g9b-cu2a-infra-unit-2856f246
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU2 -LogDirectory artifacts\agent\pre-g9b-cu2a-phase-pre-g9b-cu2-2856f246
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU1 -LogDirectory artifacts\agent\pre-g9b-cu2a-phase-pre-g9b-cu1-2856f246
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0 -LogDirectory artifacts\agent\pre-g9b-cu2a-phase-pre-g9b-cu0-2856f246
& tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory artifacts\agent\pre-g9b-cu2a-integration-2856f246
```

La consola de cada ejecución se redirigió al directorio temporal de la sesión, fuera del
repositorio. Todas terminaron con código de salida 0 a la primera, y sus `result_hash` se
autenticaron con `Read-VerificationJson` y `Get-VerificationResultHash`.

| Perfil | `run_id` | `result_hash` | Plan | Duración | Resultado |
|---|---|---|---|---:|---|
| `STATIC` | `verification-4750c53b651f48d085264099ea63a1d4` | `5576fdb3b27f72ad7b5f29c27727302d4d895c1ed97492877a27e1c86831dd07` | `25dfacc8…cc52` | 0,1 min | `ACCEPTED / COMPLETE`, 3/3; solo diagnósticos permanentes |
| `INFRA_UNIT` | `verification-c666c886bea04619a5d9e4a4cacfef3b` | `46a39cf3d018a232783a2aae7f113bd814dedd84734195195b5409ad0b81039d` | `830887b1…ce1d` | 2,2 min | `ACCEPTED / COMPLETE`, 22/22 |
| `PHASE -Phase PRE-G9B-CU2` | `verification-012a2eff0a0f4240aef201f6c94af738` | `47ab34e8ace6da40f54f2a0ee61023fb6d734803e49a5771b7ba48cfd4d08cf0` | `b9dc6d9e…a930` | 9,6 min | `ACCEPTED / COMPLETE`, 4/4; `pre-g9b-cu2.shared`: 21 clases y 233 pruebas; `pre-g9b-cu2.desktop`: 9 clases y 68 pruebas; 0 fallos, 0 omitidas |
| `PHASE -Phase PRE-G9B-CU1` | `verification-0cc38b330cd542c1bf506f7fece317e4` | `f61b9b2d3d55edba8b5bab439ddbb2b43bb4acc9898c7d5ff4fb6aa0ecb4a262` | `b0e4f130…4519` | 5,5 min | `ACCEPTED / COMPLETE`, 4/4; `pre-g9b-cu1.shared`: 6 clases y 57 pruebas; `pre-g9b-cu1.desktop`: 4 clases y 20 pruebas; 0 fallos |
| `PHASE -Phase PRE-G9B-CU0` | `verification-a43bf15d7dc2466cbce771687051e209` | `efdcf68cba58173d424e9ed81f0552520327b6a9cd8cec458ec25aaa59d51cef` | `43e3e97e…9173` | 2,7 min | `ACCEPTED / COMPLETE`, 2/2; `pre-g9b-cu0.desktop`: 6 clases y 21 pruebas; 0 fallos |
| `INTEGRATION` | `verification-d0679cf451004c2fb43c42be229b9fb3` | `41409661a1b16687f6a3deddc4622734c3102df08a5c2c61a58f7d42a96b22ec` | `4502c4bd…7aea` | 54,7 min | `ACCEPTED / COMPLETE`, 19/19 |

Los seis planes son los mismos de T13: las selecciones conservan su definición y solo cambian sus
identidades esperadas. En `INTEGRATION` pasaron `junit.shared.final` (724 archivos y 7 248
pruebas, con las 10 omitidas upstream), `junit.desktop.final` (189 archivos y 2 247 pruebas, con
1 omitida upstream), `junit.desktop.g9u1-isolated` (2 clases y 5 pruebas) y
`junit.desktop.revision3-diagnostic` (1 prueba). Los diagnósticos de `STATIC` y de `INTEGRATION`
son los permanentes, `diagnostic.governance` = `DIAGNOSTIC_FINDING` y
`diagnostic.historical-consistency` = `DIAGNOSTIC_UNAVAILABLE`; los demás son `DIAGNOSTIC_CLEAR`:
los cuatro de Checkstyle, `diagnostic.style`, `diagnostic.documentation`,
`diagnostic.guide-structure`, `junit.desktop.revision3-diagnostic` y
`repository.whitespace-diagnostic`.

La carrera `OBS-CU2-OFF-EDT-TOOLBAR-RACE`, que rechazó la primera `INTEGRATION` de T13, no se
presentó: hubo una sola `INTEGRATION`, aceptada. Las dos de T13 se conservan tal como constan en su
[registro de verificación](pre_g9b_cu2_verification_record.md) §4, como pide la autorización de
T13-R1/R2 (§4), que tampoco permite tocar Swing ni pruebas ajenas a `CU2` sin una regresión
atribuible al candidato.

### 12.3 Bancos y referencias

El arnés de `CU0` (`tools/benchmark/pre-g9b-cu/run-harness.ps1`) se ejecutó sobre el candidato con
el árbol de trabajo limpio (`product_sources_modified = false`), después de las puertas. Las
salidas están en `artifacts/benchmarks/pre-g9b-cu/cu2-after-{certification,tessellation,references}-2856f246`,
que Git ignora, y se comparan hoja a hoja con las de `39741a0b`, sin tiempos ni identidades.

- **`certification`** (garantías GC): las 3 936 hojas de contadores de `39741a0b` son iguales. Hay
  224 hojas nuevas, los contadores `coincidence_band_closure` y `nested_coincidence_band_closure`
  de cada locus y paso, todas a 0: el cierre de bandas no actúa en los pasos del modelo del autor.
  La tabla de GC-1 a GC-7 del [registro de verificación de T13](pre_g9b_cu2_verification_record.md)
  §3 vale sin cambios. Tiempos, informativos: carga de 4 198 a 3 204 ms; pasos de 3,7–92,1 ms a
  4,4–84,2 ms.
- **`tessellation`**: los vértices de cada vista y la fidelidad de los 660 candidatos no cambian.
  En 33 candidatos de la mordedura de excCyl 3,85 (`o`, `v`, `w` y `a_1`) cambia el mayor giro
  fuera de exenciones, como mucho 6,2·10⁻⁶ grados.
- **`references`**: las 661 hojas `preserve` de la línea base de `CU0` son iguales. De las 346
  hojas `pins` cambian 12: `max_render_gap_at_branch_end_px` de `o`, `v`, `w` y `a_1` a excCyl 3,7,
  3,85 y 3,9, con valores nuevos de 2,7·10⁻⁵ a 5,3·10⁻⁵ px y una variación máxima de 1,23·10⁻⁵ px,
  muy por debajo de los 0,75 px que asevera `PreG9BCu0LocusReferenceTest` (V-B07-1), que pasa en
  `PHASE -Phase PRE-G9B-CU0`.

**Atribución.** El arnés se ejecutó también sobre el candidato con `LocusDefinition2D` sustituida
por su versión de `39741a0b`, compilada contra el `common.jar` del candidato y antepuesta al
classpath de las pruebas de Desktop por un script de inicialización del directorio temporal, sin
tocar el árbol. Así reproduce hoja a hoja las salidas `references` y `tessellation` de `39741a0b`.
Los cambios proceden, por tanto, solo de H-CU2A-3 (§4.2): todo extremo incluido de una componente
que no es cerrada periódica se evalúa en su propio parámetro binary64, y no en la imagen de la
canonicalización, que lo desplaza unos ulp (O-CU2A-1). En un extremo de raíz cuadrada, ese
desplazamiento mueve el vértice dibujado del orden de 10⁻⁵ px. Las demás correcciones del
descendiente no cambian ninguno de los tres bancos.

### 12.4 Comparación con el candidato anterior

**Garantías conservadas.** Las que la autorización autoral del descendiente (T13-R1/R2, §3) pide
conservar, con las pruebas que las ejercen en las puertas de §12.2:

| Garantía | Pruebas |
|---|---|
| H10: estimación condicionada sin autoridad de certificación | `PreG9BCu2ConditionedEstimateTest` |
| V-CON-6: composición certificada por pasos estructuralmente totales | `PreG9BCu2ConsumerMigrationTest` |
| Componentes puntuales y coherencia de `restrictedTo` | `PreG9BCu2ExistenceStructureV2Test`, `PreG9BCu2ConsumerMigrationTest` |
| Fallo cerrado de los tokens históricos sin correspondencia demostrada | `PreG9BCu2LineageTest.selectorsResolveOnlyThroughDemonstratedCorrespondence` |
| Exactitud de revisión y fidelidad de reconstrucción | `PreG9BCu2RevisionExactnessTest`, `PreG9BCu2SliceFidelityCharacterizationTest`, `PreG9BCu2AuthorModelSliceFidelityTest` |
| Admisibilidad local independiente de la completitud global | `PreG9BCu2ExistenceStructureV2Test`, `PreG9BCu2HybridCertificationTest` (V-LVL-1, V-LVL-2) |
| Semántica de selección de raíces coincidentes | `PreG9BCu2SelectionSettlementTest`, `PreG9BCu2AuthorModelSelectionSettlementTest` |
| Compatibilidad con `CU0` y `CU1` | `PHASE -Phase PRE-G9B-CU0`, `PHASE -Phase PRE-G9B-CU1` y las referencias de §12.3 |
| Sin segmentos ficticios ni cierres incorrectos | `PreG9BCu2RepresentationTest`, `PreG9BCu2AuthorModelConsumerTest.theCertificateIsTheSingleClosureAuthority`, `PreG9BCu2AuthorModelExportTest` |

**Comportamiento modificado.** Cada cambio tiene una prueba adversaria que falla sobre `39741a0b`
(registros de R6 y R7 y §3 y §4 de este registro):

| Cambio | Origen |
|---|---|
| transición de linaje `DIMENSION_CHANGED` entre una componente puntual y una de dimensión uno, con su enmienda normativa mínima | D-CU2-T13-R1, `3803de5da` |
| el corte reproduce la etiqueta pendiente de su revisión, y la fidelidad compara el etiquetado salida a salida | H-T13-A, `e9541c42e` |
| banda rigurosa de la segunda salida sin etiqueta; cierre de una banda de coincidencia a su resolución | H-T13-B, `8d3e5ef0a` |
| causa tipada de un `Point(L, u)` sin resolver | H-CU2A-1, `b1475b7e1` |
| las imágenes por semejanza transportan la envolvente de la posición de una componente puntual | H-CU2A-2, `8abf7e72b` |
| un extremo incluido de una componente que no es cerrada periódica se evalúa en su parámetro: en la costura ya no falla, y en las mordeduras el vértice dibujado del extremo se mueve del orden de 10⁻⁵ px (§12.3) | H-CU2A-3, `ed47955b3` |
| la costura de un dominio periódico no es un extremo | H-CU2A-4, `2a1c29632` |
| las etiquetas excluidas salen de la huella con su espacio | H-CU2A-5, `af8875d01` |

H-T13-C no cambia el producto: su prueba (`05a6abc18`) pasa también sobre `39741a0b`.

**Nuevo.** El contador `COINCIDENCE_BAND_CLOSURE` y `Result.getClosedCoincidenceWidth()`
(`7985ea62d`); 41 pruebas de shared y 22 de Desktop en las selecciones de `CU2`. Las cuatro filas
obligatorias sin prueba de T13 (V-POINT-3, V-REV-EXACT-5, V-COINC-6 y V-COINC-9) y las tres de
D-CU2-T13-R4 (V-DET-1, V-INV-1 y V-INV-2) tienen pruebas propias; de las 34 entradas de R5 queda un
elemento sin aseverar (V-POINT-4, C-9).

### 12.5 Lo que no se ha ejecutado

- `FINAL`: el mandato fija las puertas de `INTEGRATED_PHASE` (D-CU2-12), y la autoridad ejecutable
  (`verify.ps1` y el registro) no exige más para esta clase. `FINAL` añade a `INTEGRATION` las hojas
  de `INFRA_UNIT`, y las dos campañas se ejecutaron por separado sobre el mismo candidato;
  pero solo `FINAL` escribe `verification-receipt.json`, así que un cierre ligado a un recibo
  necesitaría esa campaña, por decisión autoral.
- La prueba manual del autor sobre el candidato.
- La comprobación externa V-DXF-4 (informativa) de T13 no se repitió: el descendiente no modifica
  el código de exportación, y las pruebas de exportación DXF de `final.desktop` pasan en
  `INTEGRATION`.

### 12.6 Estado

`CU2` no está `PASS`. El candidato espera la revisión autoral, con las doce decisiones de nivel C
de §9. Nada se ha publicado, fusionado ni etiquetado; `main` no cambia y `geocedg_book` no se ha
tocado. `selfApproved=false`.
