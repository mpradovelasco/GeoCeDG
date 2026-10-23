# PRE-G9B-CU2 — Paquete de revisión final

| Campo | Valor |
|---|---|
| Carácter | Paquete de revisión final de `CU2` para la decisión autoral de cierre. No es aprobación; `selfApproved=false` |
| Origen | Resolución autoral «PRE-G9B-CU2 — Resolución autoral consolidada y preparación del cierre» de 2026-10-23: aprueba las decisiones C-1 a C-12 con condiciones, pide completar V-POINT-4 y las precisiones, una revisión científica focal, la verificación final y este paquete, y no declara todavía `CU2` `PASS — AUTHOR APPROVED` |
| Base | rama local `phase/pre-g9b-cu2-corrective`; candidato correctivo anterior `2856f246c61bd51916932262002cdff31e61b565` (árbol `95d9f408e65c3a3cda7181043521a78e30b292ec`), verificado en el [registro de la corrección](pre_g9b_cu2_corrective_autonomy_record.md) §12, y su descendiente documental `c8d4fd39c` |
| Candidato | `6ff1ddf743bb3ee44a4e9a47f7dc238f30d642cb`, árbol `3dfcc575c45ec9ec660199d86d166d89e32851c9` (§1); un commit documental descendiente, que no es un candidato nuevo, completa §1, §6 y §7 |
| Verificación | inventario JUnit regenerado con 0 fallos; `STATIC`, `INFRA_UNIT`, las tres `PHASE` e `INTEGRATION` `ACCEPTED / COMPLETE` a la primera (`INTEGRATION` `verification-a4b0007148a1467ba119058870f024cb`, 19/19); bancos de certificación, teselación y referencias iguales hoja a hoja a los de `2856f246`; V-DXF-4 sin errores en AutoCAD (§6) |
| Estado | `CU2` no está `PASS`; la comprobación manual del autor sigue pendiente y no se simula ni se sustituye por pruebas; nada se ha publicado, fusionado ni etiquetado; `geocedg_book` no se ha tocado |

## 1. Identidad Git

| Elemento | Valor |
|---|---|
| Rama | `phase/pre-g9b-cu2-corrective`, local, sin publicar |
| Candidato anterior | `2856f246c61bd51916932262002cdff31e61b565` y su descendiente documental `c8d4fd39c` |
| Correcciones de producto | `1c543d27b` (`fix`): correcciones de la revisión científica focal (H-CU2A-6 a H-CU2A-11, R7-A a R7-D), comentario de C-2 y gancho de prueba de C-9 |
| Pruebas | `92dbd455a` (`test`): pruebas adversarias de la revisión, decisiones C-1, C-4 y C-9 y los doce residuos menores; `ac15661ab` (`test`): V-DET-1 repite todas las clases de escenario de su contrato |
| Documentos | `e3d01b9bd` (`docs`): matriz, revisión de trazabilidad, registros de la corrección y de R7, inventario de R5, precisión C-8 del modelo de construcción certificada, versión 4 del plan E1 y este paquete |
| Candidato final | `6ff1ddf743bb3ee44a4e9a47f7dc238f30d642cb` (`test`), árbol `3dfcc575c45ec9ec660199d86d166d89e32851c9`: inventario JUnit ejecutado y sus fijaciones (§6.2); congela el candidato |
| Delta desde `c8d4fd39c` | 5 commits; 31 archivos, 1 nuevo (este paquete) y 30 modificados, con 2 133 inserciones y 142 borrados. Producto: 10 archivos de `source/shared/common/src/main` (253 y 30). Pruebas: 10 clases `PreG9BCu2*` (1 429 y 30), ninguna prueba eliminada. Resto, 11 archivos: la matriz, la revisión de trazabilidad, los registros de la corrección y de R7, el inventario de R5, el modelo de construcción certificada, el plan E1, este paquete, el inventario JUnit, el registro de verificación y su prueba de cobertura |
| Descendiente documental | el commit que completa §1, §6 y §7 de este paquete, el plan consolidado y la hoja de ruta; no modifica ningún archivo de producto, de prueba ni de verificación |

## 2. Decisiones C-1 a C-12 aplicadas

Todas las precisiones se limitan al contenido de §9 del
[registro de la corrección](pre_g9b_cu2_corrective_autonomy_record.md); ninguna cambia la
obligatoriedad ni el tipo de una fila, un formato persistido, una identidad, una regla de
selección, un valor o una política de `LocusLength`.

| Decisión | Alternativa aprobada | Aplicación | Evidencia |
|---|---|---|---|
| C-1 | aceptar `STRUCTURE` para el cambio de etiquetado de una salida de un par etiquetado | fila V-REV-EXACT-7 | `PreG9BCu2SelectionSettlementTest.theVariantsCreatedAtTheDoubleRootKeepTheirComponents` asevera ahora `STRUCTURE` y ningún `LABELS` |
| C-2 | declarar reservada `COMPUTATIONAL_DEPENDENCY_FAILURE` | fila V-UND-1; comentario de la constante en `LocusCoverageCause2D` | bytecode idéntico al del candidato anterior (mismo `javac`, misma salida) |
| C-3 | coordenadas dibujadas bit a bit las del evaluador, sin mejorar su clase | fila V-AVAIL-6 | `PreG9BCu2AuthorModelRepresentationTest.drawnVerticesAreTheEvaluationsOfTheirParameters` |
| C-4 | por revisión publicada, un cálculo propio y uno por cada copia anidada reconstruida | fila GC-2 | `PreG9BCu2AuthorModelCertificationTest.aNonDriverChangeComputesOneCertificatePerAffectedLocus` asevera ahora la regla de los anidados y que algún cambio reconstruye una copia anidada |
| C-5 | órdenes de compilación en la fila y omisión visible sin herramientas | fila V-TEX-1 | `PreG9BCu2AuthorModelExportTest` (PGF, PSTricks y Asymptote) |
| C-6 | exceso de evaluación heredado, banda tipada y muestreo del modelo | filas V-COINC-5 y V-COINC-7 | `PreG9BCu2AuthorModelSelectionSettlementTest.theUnlabelledSecondOutputKeepsItsTypedBands` |
| C-7 | «contiene el parámetro dentro de la tolerancia del dominio, entre hijas de una división» | fila V-LIN-4 | `PreG9BCu2LineageTest.anAmbiguousSelectorLeavesThePointUndefinedWithATypedCause` |
| C-8 | precisión editorial de §10.3 | `locus-v2-certified-construction-model.md` §10.3 («bounded by conservative predicates of the inherited computation on first-level inputs») y su línea de estado | sin cambio de significado; la propuesta normativa de la preparación conserva su texto histórico |
| C-9 | (a): prueba del fallo forzado de la forma límite | gancho interno de paquete y prueba (§3) | `PreG9BCu2HybridCertificationTest.aForcedFailureOfTheLimitFormLeavesThePositionUnavailable`; falla si se anula el gancho |
| C-10 | (a) con precisión: limitación computacional tipada de `CU2` | fila V-CON-3; limitación `L-CU2-SQRT-END-LENGTH` (§8) | sin cambio del contrato métrico de `CU1` ni estimador nuevo; la estimación con cambio de variable queda como trabajo futuro sin asignación |
| C-11 | conservar la canonicalización periódica | limitación `L-CU2-PERIODIC-CANONICALIZATION` (§8) | sin cambio de producto ni de direcciones persistidas |
| C-12 | conservar la pertenencia heredada de las componentes cerradas periódicas | limitación `L-CU2-CLOSED-PERIODIC-SEAM` (§8) | sin cambio de producto; referencias de `CU0` y `CU1` intactas |

C-11 y C-12 no autorizan inferir correspondencias semánticas por tolerancia o proximidad ni
modificar direcciones persistidas, y nada lo hace.

## 3. Diseño de la prueba de V-POINT-4 (C-9)

La decisión C-9 pide demostrar, con un fallo forzado de la forma límite de una componente puntual,
que la componente conserva su existencia certificada, que su posición pasa explícitamente a no
disponible, que no se inventa una posición ni el fallo se convierte en inexistencia, que los
consumidores reciben el estado y la causa correspondientes y que, al desaparecer el fallo, la
evaluación ordinaria se recupera; prefiere la inyección en el arnés con las interfaces existentes.

- **Definición geométrica.** Ninguna nueva. La posición de una componente puntual es el valor de
  la forma límite: el programa evaluado sobre la envoltura certificada de la raíz suponiendo su
  predicado (`ConstructionBoxModel2D.evaluateAssuming`, diseño de certificación §3.4). El
  certificador ya trata una evaluación válida sin valor como posición `UNAVAILABLE`
  (`HybridExistenceCertifier2D.decideBracket`); la existencia la decide el tipo de la
  evaluación, nunca su valor.
- **Por qué no basta el arnés.** `ConstructionBoxModel2D` y `ConstructionBoxOutcome2D` son
  finales; el evaluador reconstruible certifica con `Policy.DEFAULT` y un modelo que construye él
  mismo, y la posición se fija dentro del certificador y se publica en la componente, que es lo
  que leen el dibujo, la exportación, las semejanzas y el inspector. Ninguna interfaz existente
  permite que una prueba haga fallar la forma límite en la canalización viva y la restablezca
  después. Alternativas descartadas: una estructura construida a mano con posición nula (no
  ejerce el certificador ni la recuperación), clases sombra o instrumentación de bytecode en la
  prueba (no forman parte de la selección oficial y dependen de detalles internos), y una
  política de prueba nueva (exigiría cambiar la API pública de `Policy` y el evaluador).
- **Punto de inyección.** Un campo estático de paquete, `ConstructionBoxModel2D.withholdLimitFormValue`,
  falso por defecto: mientras es verdadero, `evaluateAssuming` devuelve las evaluaciones válidas
  sin su envoltura del punto trazado (con su selección y sus evaluaciones de predicados) y deja
  intactas las no válidas y las no resueltas. El certificador solo lee de esas evaluaciones el
  tipo y el objetivo para decidir; el valor solo da la posición de las componentes puntuales y
  las imágenes de los extremos (`encloseRootImage`). Ninguna decisión de validez cambia, y las
  reglas matemáticas de la certificación no se modifican.
- **Impacto.** Ninguno en producción: el código de producto nunca lo asigna; no es API pública,
  no se persiste ni se serializa, no entra en ningún certificado ni identidad, y con el valor
  falso el método devuelve exactamente la misma evaluación. No hay código upstream afectado.
- **Validación.** `PreG9BCu2HybridCertificationTest.aForcedFailureOfTheLimitFormLeavesThePositionUnavailable`:
  certificado de referencia de la tangencia exterior; con el gancho activo, dos revisiones nuevas
  (`P` un ulp y vuelta) publican la componente con la misma envoltura del parámetro, fuente,
  maximalidad, prueba y evidencia, el mismo recubrimiento (`COMPLETE`, puntos aislados
  certificados en `t*` y no existencia certificada a ambos lados) y la posición `UNAVAILABLE`,
  sin envoltura; el evaluador no publica ningún punto junto a `t*`; `Point(L, u)` sigue indefinido
  con la causa `POINT_COMPONENT`; la longitud total es 0; el dibujo y la declaración de exportación
  dicen `position UNAVAILABLE`, y la imagen por una traslación también. El gancho se desactiva en
  un bloque `finally`; dos revisiones más devuelven el certificado de referencia bit a bit, con la
  envoltura de (15, 20), y los consumidores vuelven a recibirla. Con el efecto del gancho anulado,
  la prueba falla («expected: UNAVAILABLE but was: CERTIFIED_ENCLOSURE»).

## 4. Estado definitivo de conformidad

La [matriz de validación](pre_g9b_cu2_validation_matrix.md) tiene 134 filas: 95 de tipo F, 19 de
tipo A, 8 puertas GC, 11 de regresión (R) y 1 de comprobación externa (B). Su §6 relaciona cada
fila con las pruebas que la citan y resume su revisión sustantiva
([revisión de trazabilidad](pre_g9b_cu2_traceability_review.md), §10 a §12):

| Revisión | Filas | Detalle |
|---|---|---|
| efectiva | 99 | de ellas, 34 por el mandato correctivo (las 34 filas de hueco confirmado de R5; GC-2 y V-POINT-4 se completan con las decisiones C-4 y C-9) y 13 por la resolución consolidada (los doce residuos menores y V-DET-1) |
| cubierta por pruebas que no la citan | 18 | la revisión de trazabilidad nombra las pruebas que aseveran cada elemento |
| descendiente T13 | 4 | V-COINC-6, V-COINC-9 y V-INV-1, efectivas (§6 de la revisión); V-INV-2, efectiva con la precisión B(i) de D-CU2-T13-R7 |
| vía de verificación | 12 | las 11 de tipo R (puertas `PHASE` de `CU0` y `CU1`, `INTEGRATION`, referencias `preserve`, escaneo de autoridad de G9X1 y las pruebas citadas) y V-DXF-4 (tipo B, informativa, con AutoCAD Core Console) |
| informativa | 1 | GC-7: los bancos en otras máquinas son informativos (§5 de la matriz) |

Ninguna fila obligatoria queda con un elemento sin aseverar ni pendiente de decisión: cada una
está ejercitada por pruebas que aseveran sus elementos esenciales, sigue su vía de verificación o
tiene una decisión autoral que define su tratamiento. De las puertas, GC-1, GC-2, GC-3, GC-4, GC-6
y GC-8 son efectivas, GC-5 la cubre `PreG9BCu2CertificationInstrumentationTest` y GC-7 es
informativa.

| Hallazgos y decisiones | Estado |
|---|---|
| H-T13-A, H-T13-B | corregidos por R6 y R7 (D-CU2-T13-R6 y D-CU2-T13-R7) |
| H-T13-C | clasificado como fallo exclusivamente computacional (D-CU2-T13-R7) |
| H-CU2A-1 a H-CU2A-5 | corregidos por el mandato correctivo (nivel B) |
| H-CU2A-6 a H-CU2A-11, R7-A a R7-D | corregidos por la revisión científica focal (§5) |
| B-2, R7-E | documentados, sin cambio de producto (§5 y §8) |
| O-CU2A-6 | refutado donde la raíz de la tolerancia es la bruta 0, y corregido (H-CU2A-8) |
| O-CU2A-7 a O-CU2A-9, `L-CU2-INDEPENDENT-EXPRESSION-STATE` | sin cambio de producto; propuestas de §8 |
| D-CU2-T13-R1 a D-CU2-T13-R7, C-1 a C-12 | resueltas |

Solo quedan las decisiones de §10.

## 5. Revisión científica focal

La resolución pide revisar, antes del cierre, que la implementación de las garantías científicas
más sensibles siga sus hipótesis y límites documentados, y en especial que ninguna justificación
matemática dependa solo del muestreo numérico. Se encargaron tres revisiones independientes, de
solo lectura, sobre el código y sus documentos (no sobre las conclusiones de los registros); cada
hallazgo se comprobó después con una sonda o una prueba antes de actuar. Cada corrección
H-CU2A tiene una prueba que falla cuando se retira la corrección (comprobado retirándola en el
árbol de trabajo y restaurándola); las correcciones R7-A a R7-D cierran huecos de cotas a priori
sin un caso alcanzable construido, solo ensanchan la banda, y las pruebas de R7 pasan antes y
después de ellas.

| Área | Resultado | ¿Justificación solo por muestreo? |
|---|---|---|
| Cotas de la banda heredada de R7 (H-T13-B) y cierre de bandas | el predicado implementado coincide con el cálculo heredado; las cotas de redondeo son correctas a primer orden, con cuatro huecos de rigor corregidos (R7-A a R7-D) y una precisión documental (R7-E) | no: las cotas son a priori; `SLACK`, `2⁻⁵⁰` y `2⁻²⁰` son márgenes analíticos y ninguna constante de producto se ajustó por muestreo; las pruebas son evidencia añadida |
| Transporte de envolventes por semejanza (H-CU2A-2) | todas las hipótesis se cumplen; sin defecto | no: la envolvente es una cota analítica (aritmética de intervalos hacia fuera, `Math.cos` y `Math.sin` a 1 ulp en la JVM, normalización de la reflexión acotada por `5u + O(u²)` frente a `2⁻⁵⁰`); la prueba de 3 000 casos es evidencia añadida |
| Extremos incluidos y costura (H-CU2A-3, H-CU2A-4) | se cumplen; un defecto de las imágenes por semejanza (H-CU2A-6, corregido) y un caso de redondeo de la canonicalización (B-2, documentado bajo C-11) | no: pertenencia exacta por coordenada de arco y por igualdad de extremos |
| Fidelidad de reconstrucción y selección de raíces coincidentes | se cumplen la no escritura del estado de selección, el etiquetado salida a salida y el asentamiento; tres defectos corregidos (H-CU2A-7, H-CU2A-8, H-CU2A-9) y una limitación propuesta (`L-CU2-INDEPENDENT-EXPRESSION-STATE`) | no: las decisiones son exactas sobre el estado heredado leído; la tolerancia heredada no decide ninguna selección certificada (H-CU2A-8) |
| Existencia matemática frente a disponibilidad computacional | se cumple: la inexistencia solo procede de hechos certificados, un fallo computacional nunca se convierte en inexistencia y la posición de una componente puntual es una envolvente o `UNAVAILABLE` (V-POINT-4, C-9); un diagnóstico sin causa tipada corregido (H-CU2A-11) | no |
| Determinismo de los certificados y conservación del linaje | se cumple: solo se iteran colecciones ordenadas, el presupuesto cuenta cajas y la relación de linaje es estructural; un defecto corregido (H-CU2A-10) y una observación no demostrada alcanzable (O-CU2A-7) | no; la doble ejecución de V-DET-1 repite ahora todas las clases de escenario de su contrato |

### 5.1 Correcciones

| Id | Gravedad | Contrato | Evidencia | Corrección | Prueba que falla sin ella | Riesgo residual |
|---|---|---|---|---|---|---|
| H-CU2A-6 | media | nota de alcance A2.5 de `locus-v2-similarity-transformations.md` (la imagen hereda las componentes con sus extremos); `locus-v2-existence-components.md` §15.3 | el evaluador de la imagen evaluaba la fuente con su evaluación pública, que vuelve a canonicalizar: los extremos incluidos de un arco imagen que cruza la costura se desplazaban unos ulp fuera del arco de la fuente y fallaban (radios `40 + 2⁻³⁰`, `40 + 2⁻⁴`, 41 y 45; imágenes por traslación, rotación, punto, recta y dilatación) | `LocusDefinition2D.evaluateCanonicalParameter`, de paquete, evalúa un parámetro canónico tal cual; `LocusSimilarityEvaluator2D` la usa (comparten proveedor) | `PreG9BCu2HybridCertificationTest.theIncludedEndsOfAnImageArcAcrossTheSeamAreEvaluatedAtTheirOwnParameter` | ninguno conocido; `AlgoNestedLocusV2` no comparte el defecto (su parámetro es la imagen de un mapa) |
| H-CU2A-7 | alta | `locus-v2-certified-construction-model.md` §10.2 (en cada sincronización la copia reproduce el estado de selección del vivo) y la regla de fidelidad de `locus-v2-semantics.md` v1.3 | la fidelidad no comparaba la selección de una intersección sin etiqueta dentro de una expresión: con `Midpoint(Intersect(s,c,i),B)`, el certificado de la compactación de la copia afirmaba existencia donde el punto vivo no está definido y no existencia donde lo está, 35 veces en 128 parámetros para cada `i` | `ReconstructionFidelity2D.compareInline` compara también la selección de esa intersección, que queda reproducida, asentada o fallida con causa | `PreG9BCu2SelectionSettlementTest.anInlineIntersectionIsASelectionOfTheSlice` (35 contradicciones sin la corrección) | ninguno conocido |
| H-CU2A-8 | alta | §10.2 (la compactación certificada es la de las raíces del camino) y el punto 8 del contrato de asentamiento (`locus-v2-semantics.md`: cada consulta comprueba sus salidas con la regla de la revisión y falla explícitamente si difieren) | en la primera inicialización, la compactación heredada cuenta también una raíz dentro de la tolerancia `10⁻⁵` antes del inicio del rayo; cuando esa raíz es la bruta 0, la salida 0 es otra raíz que la certificada; la comprobación de salida usaba la misma tolerancia y el evaluador publicaba, dentro de una componente certificada, la raíz de detrás del inicio, a 9,19 de la certificada (293 de 800 muestras) | la comprobación de salida de `ReconstructibleLocusEvaluator2D` aplica la pertenencia de la regla capturada, el parámetro del rayo o segmento sin tolerancia; esas consultas fallan con `SELECTION_NOT_REPRODUCED`; el certificado no cambia | `PreG9BCu2SelectionSettlementTest.theCompactionNeverPublishesARootWithinTheToleranceBeforeTheRayStart` (raíz publicada a 9,19 sin la corrección) | junto a la transición exacta, dentro del redondeo del cálculo heredado, el signo del parámetro calculado puede diferir del exacto: limitación de disponibilidad numérica, sin cota a priori en esta corrección (§8) |
| H-CU2A-9 | media | `locus-v2-semantics.md` v1.3 (la huella distingue todo cambio semánticamente relevante de las entradas que no son el conductor; ninguna consulta evalúa una definición obsoleta) | el lugar libre de un punto sobre un camino o en una región que sigue al conductor no entraba en la huella (sus coordenadas dependen del conductor y su forma serializada solo guarda coordenadas): arrastrar `B=Point(Segment(N,P))` no publicaba revisión y el evaluador seguía con la posición anterior, distinta tras reabrir | la huella incluye el parámetro exacto del lugar libre (`ReconstructionExactState2D.freePlace`); la fidelidad lo compara y lo restaura por los parámetros del punto, que su algoritmo lee en cada actualización | `PreG9BCu2RevisionExactnessTest.theFreePlaceOfAPointThatFollowsTheDriverIsAnInputOfTheSlice` y `aFreePlaceThatFollowsTheDriverIsComparedAndRestoredExactly` (sin la corrección no hay revisión, y la copia proyectada difiere del vivo en el último bit) | un camino cuyo `pathChanged` cambia el parámetro al moverse el conductor publicaría una revisión por ese movimiento; las versiones de la huella y de la fidelidad no cambian (precedente del registro de R6 §2.7), y ningún artefacto guardado depende de ellas |
| H-CU2A-10 | media | `locus-v2-existence-components.md` §15.4 (un punto y una componente de dimensión uno solo se relacionan por `DIMENSION_CHANGED`) y §15.6 | una edición de valor que convierte una componente cerrada periódica en la componente puntual de una tangencia exterior, con la misma regla de selección, se registraba `SPLIT(cerrada → punto)` | la división y la fusión relacionan la componente cerrada solo con componentes de dimensión uno; un punto aparece o desaparece | `PreG9BCu2LineageTest.aClosedComponentIsNeverSplitIntoAPointComponent` | ninguno conocido; los selectores no se resolvían a través de esa división |
| H-CU2A-11 | baja | §15.8 (un consumidor que direcciona por parámetro binary64 falla con causa tipada) | la evaluación de la propia definición en la envolvente de una componente puntual decía «fuera del dominio de existencia completo» | el diagnóstico nombra la causa `POINT_COMPONENT`, con el mismo estado `OUT_OF_DOMAIN` | `PreG9BCu2HybridCertificationTest.aSemanticPointNearTheTangencyIsUndefinedWithATypedCause` | ninguno |
| R7-A | media (alcance no demostrado) | precisión D-CU2-T13-R7 (una caja sale de la banda solo si el cálculo heredado es decisivo, con cotas a priori de todo redondeo) | en compactación, `P[1]` exige las dos raíces brutas en el camino, pero la banda solo leía el margen de la seleccionada | la banda toma el menor margen de las dos raíces brutas | — (ningún caso alcanzable construido; las pruebas de R7 pasan antes y después) | solo ensancha la banda |
| R7-B | baja | ídem | con `wLow < 0`, el producto `uHigh·wLow` se redondeaba al más cercano y, con cancelación, podía superar la holgura | `Math.nextDown` sobre ese producto | — | despreciable |
| R7-C | baja | ídem | el cambio de unidades de la dirección acotaba el inicio con una norma máxima; el término quedaba corto cuando el inicio está lejos en diagonal | la cota usa `√2·start` | — | solo ensancha la banda |
| R7-D | baja | ídem | con dos envolventes disjuntas de un mismo valor (una no sería correcta) se tomaba la primera | la caja se marca inconsistente y queda en la banda | — | solo ensancha la banda |

R7-E (documental): el cierre de bandas une celdas por su causa y no por la identidad de la banda;
dos bandas distintas de esa causa a menos de `0,0245` rad se unirían, retirando hasta esa anchura:
sigue siendo conservador, pero más que los `10⁻¹⁰` a `10⁻⁹` rad que cita el registro de R7. Notas
bajas de R7: el umbral `kept ≥ 0,9·72` de su prueba tolera hasta 7 discrepancias frente a las 3
documentadas, y la negativa del punto medio de la matriz afecta a toda circunferencia constante
capturada (exige el punto medio exacto).

B-2 (bajo, dentro de C-11): la canonicalización periódica lleva `nextDown(π)` a `−π`, el otro lado
de la misma costura, y en un dominio cerrado por abajo puede devolver el extremo excluido (en
`[0, 1)`, `−10⁻²⁰` da `1`, que el dominio rechaza). Se documenta en
`L-CU2-PERIODIC-CANONICALIZATION` (§8) y se corrige, con nota, la frase de §4.3 del registro de la
corrección que daba su imagen siempre en `[lower, upper)`.

### 5.2 Observaciones sin cambio de producto

| Id | Descripción | Disposición |
|---|---|---|
| O-CU2A-6 (refinada) | el cierre «sin defecto» de R5 valía para la configuración sondeada, donde la raíz de la tolerancia es la bruta 1 | refutado donde es la bruta 0: H-CU2A-8; nota en el registro de la corrección y en el de R7 §6.6 |
| O-CU2A-7 | la igualdad de las firmas de selección de dos certificados es verdadera cuando ambas son nulas (sin programa de clase v3), como en un locus compuesto sobre un soporte; un cambio de selección del soporte que dividiera una componente cerrada se registraría como `SPLIT` en el compuesto | no se ha demostrado alcanzable: la composición exige un soporte `COMPLETE` sin componentes puntuales, y un cambio de selección del soporte sin cambio de valores solo puede deberse al reinicio de cercanía de `updateConstruction` con el conductor en un hueco; se propone, como trabajo futuro, tomar como identidad de selección de un certificado compuesto la del soporte |
| O-CU2A-8 | el `e_1` desplazado declara su giro junto a la costura en varios vértices a ambos lados (siete marcas con giros de 180°, algunas con el mismo parámetro canónico): la deduplicación de marcas compara parámetros elevados que cruzan la costura | cosmético: el contrato de presentación pide declarar la fidelidad con sus causas, y la fidelidad (`PARTIAL`, `UNCERTIFIED_TURN`) es la misma; la prueba de V-FOLD-2 asevera las declaraciones en `0` y en la costura y ninguna en otro lugar |
| O-CU2A-9 | un corte con dos entradas métricas estructurales cuyos eventos de origen coinciden (la primera versión del gemelo de `e_2` de V-E2-8, que leía también `w_1` a través de `j_2`) tiene la celda del evento como banda sin resolver y su ensamblado falla cerrado: `NOT_ESTABLISHED`, «A component of positive length has no canonical extent» | conservador, sin certificado falso; capacidad no exigida por ninguna fila; se propone como trabajo futuro |
| `L-CU2-INDEPENDENT-EXPRESSION-STATE` (propuesta) | por lectura de código, sin sonda: el estado exacto de un elemento independiente definido por una expresión (función, curva implícita) se lee de su XML, que escribe con 15 cifras significativas los literales sin texto original; un cambio de un ulp de esos literales por programa no se distingue en la huella ni en la fidelidad | baja, fuera de la clase v3 (sin certificado); los literales escritos por el usuario conservan su texto; se propone aceptarla como limitación y, como trabajo futuro, leer el estado exacto de los literales de la expresión (sin las variables de función) en una versión posterior de la huella |

## 6. Evidencia de verificación

### 6.1 Ejecuciones de desarrollo

Ejecuciones sobre el árbol de trabajo antes del congelado; no son evidencia de aceptación.

| Ejecución | Resultado |
|---|---|
| prueba adversaria de cada corrección H-CU2A, con la corrección retirada y restaurada | falla sin la corrección y pasa con ella; la de C-9 falla con el efecto del gancho anulado |
| pruebas `PreG9BCu2*` compartidas (13 clases) | 177/177 |
| pruebas `PreG9BCu2*` de Desktop (9 clases) | 72/72 |
| batería compartida completa (`:shared:common-jre:test`) | 724 clases, 7 257 pruebas, 0 fallos, 10 omitidas |
| Checkstyle | código 0; 4 avisos preexistentes en pruebas de Desktop no tocadas |
| dobles ejecuciones de V-DET-1 tras su ampliación (`ac15661ab`) | idénticas bit a bit; `PreG9BCu2HybridCertificationTest` completa 39/39 |

Añadidas por el commit documental descendiente, que no modifica el candidato ni es un candidato
nuevo, las secciones 6.2 a 6.7 recogen la verificación del candidato congelado. Los resultados y
antecedentes de las verificaciones anteriores se conservan íntegros: los de `2856f246` en el
[registro de la corrección](pre_g9b_cu2_corrective_autonomy_record.md) §12 y en
`artifacts/agent/pre-g9b-cu2a-*-2856f246`, y los de T13 en su
[registro de verificación](pre_g9b_cu2_verification_record.md).

### 6.2 Inventario de JUnit

El productor oficial `tools/agent/checks/gradle-test-evidence-producer.ps1` se ejecutó sobre el
árbol de `e3d01b9bd`, cuyos archivos de prueba y de producto son los del candidato.

| Selección | Modo | Resultado | Duración |
|---|---|---|---:|
| `discovery.shared` | descubrimiento en seco | `COMPLETED`, código 0; 724 archivos y 6 340 casos | 1,5 min |
| `discovery.desktop` | descubrimiento en seco | `COMPLETED`, código 0; 190 archivos y 2 257 casos | 1,4 min |
| `pre-g9b-cu2.shared` | ejecución | 21 clases, 242 pruebas, 0 fallos, 0 omitidas | 1,7 min |
| `pre-g9b-cu2.desktop` | ejecución | 9 clases, 72 pruebas, 0 fallos, 0 omitidas | 5,5 min |
| `final.shared` | ejecución | 724 archivos, 7 257 pruebas, 0 fallos, 10 omitidas | 3,7 min |
| `final.desktop` | ejecución | 189 archivos, 2 251 pruebas, 0 fallos, 1 omitida | 19,7 min |

Las omitidas son las preexistentes y upstream. El actualizador oficial
`tools/agent/update-verification-junit-inventory.ps1` se ejecutó con las dos evidencias de
descubrimiento y las cuatro de ejecución:

- `discovery.shared`: 6 331 → 6 340, y `final.shared`: 7 248 → 7 257 (+9);
- `discovery.desktop`: 2 253 → 2 257, y `final.desktop`: 2 247 → 2 251 (+4);
- `pre-g9b-cu2.shared`: 233 → 242, y `pre-g9b-cu2.desktop`: 68 → 72: los 13 métodos nuevos de §7,
  en clases `PreG9BCu2*` existentes; ninguna clase nueva;
- las demás selecciones no cambian.

La huella `junit_inventory` del registro cambia de
`07a6a7c8e9e58c5a1d13e45f28b06f1293fcef06e8d5e8cadfdff9b4e0f8fd1d` a
`712c8c77c10b3d15ab4bb0df2d549a7b505037102582d6ae5881ab5ef14c283a`; la anterior se reprodujo antes
desde el archivo de `HEAD` con `Get-VerificationCanonicalTextSha256`. Las fijaciones de
`verification-final-coverage.Tests.ps1` pasan de 233 y 68 a 242 y 72. Antes del commit, las 23
hojas de nivel `INFRA_UNIT` del registro se ejecutaron directamente sobre el árbol y pasaron. El
commit `6ff1ddf74` congela el candidato.

### 6.3 Puertas de aceptación

Clase `INTEGRATED_PHASE` (D-CU2-12). Ejecuciones secuenciales, desde la raíz del repositorio, con
el candidato como `HEAD` y el árbol de trabajo y el índice limpios:

```text
& tools/agent/verify.ps1 -Profile STATIC -LogDirectory artifacts\agent\pre-g9b-cu2f-static-6ff1ddf7
& tools/agent/verify.ps1 -Profile INFRA_UNIT -LogDirectory artifacts\agent\pre-g9b-cu2f-infra-unit-6ff1ddf7
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU2 -LogDirectory artifacts\agent\pre-g9b-cu2f-phase-pre-g9b-cu2-6ff1ddf7
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU1 -LogDirectory artifacts\agent\pre-g9b-cu2f-phase-pre-g9b-cu1-6ff1ddf7
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0 -LogDirectory artifacts\agent\pre-g9b-cu2f-phase-pre-g9b-cu0-6ff1ddf7
& tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory artifacts\agent\pre-g9b-cu2f-integration-6ff1ddf7
```

La consola de cada ejecución se redirigió al directorio temporal de la sesión, fuera del
repositorio. Todas terminaron con código de salida 0 a la primera, y sus `result_hash` se
autenticaron con `Read-VerificationJson` y `Get-VerificationResultHash`.

| Perfil | `run_id` | `result_hash` | Plan | Duración | Resultado |
|---|---|---|---|---:|---|
| `STATIC` | `verification-b60eca30025f4379b08eafffe518f309` | `577cf92058b7cfd6d480ee0b9e393620e85c532d89f521575b62eb07b6976b86` | `25dfacc8…cc52` | 0,1 min | `ACCEPTED / COMPLETE`, 3/3; solo diagnósticos permanentes |
| `INFRA_UNIT` | `verification-3d7571b1f9e440d4adc69246bdc05bdc` | `9c10cd42da3ce56ed138f43907a80051319de132ff47f06901dd338f1f21e2e7` | `830887b1…ce1d` | 2,2 min | `ACCEPTED / COMPLETE`, 22/22 |
| `PHASE -Phase PRE-G9B-CU2` | `verification-6d0dc9163e5642889799b50f8927ad11` | `0c5be979d6d750fd1b340500ce56b9c9b962e6a5f39725878988b11293e5ad41` | `b9dc6d9e…a930` | 9,6 min | `ACCEPTED / COMPLETE`, 4/4; `pre-g9b-cu2.shared`: 21 clases y 242 pruebas; `pre-g9b-cu2.desktop`: 9 clases y 72 pruebas; 0 fallos, 0 omitidas |
| `PHASE -Phase PRE-G9B-CU1` | `verification-e6d45e0ae7b24affbfb14d5477ca9a87` | `32acd12d5b54c11be92a7d435e36cf1cdffa3dba1a993d0422524b8b7944e063` | `b0e4f130…4519` | 5,5 min | `ACCEPTED / COMPLETE`, 4/4; `pre-g9b-cu1.shared`: 6 clases y 57 pruebas; `pre-g9b-cu1.desktop`: 4 clases y 20 pruebas; 0 fallos |
| `PHASE -Phase PRE-G9B-CU0` | `verification-e7b66154af944b39aacdac9bda39cbd4` | `ec7d02aed2da1d9b365daa75693493bdfc9536d5b6d12189bd3e7b0caad315af` | `43e3e97e…9173` | 2,6 min | `ACCEPTED / COMPLETE`, 2/2; `pre-g9b-cu0.desktop`: 6 clases y 21 pruebas; 0 fallos |
| `INTEGRATION` | `verification-a4b0007148a1467ba119058870f024cb` | `57989175d345dc4a27f87c7306f279ac3e036bb221e1fc0237a4c6137233a428` | `4502c4bd…7aea` | 49,9 min | `ACCEPTED / COMPLETE`, 19/19 |

Los seis planes son los de T13 y de `2856f246`: las selecciones conservan su definición y solo
cambian sus identidades esperadas. En `INTEGRATION` pasaron `junit.shared.final` (724 archivos y
7 257 pruebas, con las 10 omitidas upstream), `junit.desktop.final` (189 archivos y 2 251 pruebas,
con 1 omitida upstream) y `junit.desktop.g9u1-isolated` (2 clases y 5 pruebas). Los diagnósticos
de `STATIC` y de `INTEGRATION` son los permanentes, `diagnostic.governance` =
`DIAGNOSTIC_FINDING` y `diagnostic.historical-consistency` = `DIAGNOSTIC_UNAVAILABLE`; los demás
son `DIAGNOSTIC_CLEAR`: los cuatro de Checkstyle, `diagnostic.style`,
`diagnostic.documentation`, `diagnostic.guide-structure`, `junit.desktop.revision3-diagnostic` (1
prueba) y `repository.whitespace-diagnostic`. La carrera `OBS-CU2-OFF-EDT-TOOLBAR-RACE` no se
presentó: hubo una sola `INTEGRATION`, aceptada.

### 6.4 Referencias de `CU0` y `CU1` e indicadores de certificación

El arnés de `CU0` (`tools/benchmark/pre-g9b-cu/run-harness.ps1`) se ejecutó sobre el candidato,
después de las puertas, con el árbol de trabajo limpio (`product_sources_modified = false`). Las
salidas están en `artifacts/benchmarks/pre-g9b-cu/cu2-after-{certification,tessellation,references}-6ff1ddf7`,
que Git ignora, y se comparan hoja a hoja con las de `2856f246`, sin tiempos ni identidades.

- **`certification`** (indicadores de certificación, puertas GC): las 4 160 hojas de contadores
  son iguales, también los de cierre de bandas, todos a 0. La tabla de GC-1 a GC-7 del
  [registro de verificación de T13](pre_g9b_cu2_verification_record.md) §3 vale sin cambios.
  Tiempos, informativos: carga de 3 204 a 3 287 ms; pasos de 4,4–84,2 ms a 5,4–91,1 ms.
- **`tessellation`**: las 6 689 hojas son iguales: vértices de cada vista, fidelidad de los 660
  candidatos y giros fuera de exenciones.
- **`references`**: las 661 hojas `preserve` de la línea base de `CU0` (sin tiempos) y las 346
  `pins` son iguales a las de `2856f246`; las 12 `pins` que cambiaron frente a `39741a0b` siguen
  atribuidas a H-CU2A-3 (registro de la corrección §12.3).
- **`PHASE` de `CU0` y de `CU1`**: aceptadas (§6.3), con las referencias `preserve` de
  `PreG9BCu0LocusReferenceTest` y `PreG9BCu0CompositionCharacterizationTest` y las pruebas de
  `CU1`.

Ninguna corrección de §5 cambia una salida de los bancos, que ejercen el modelo del autor; las
pruebas adversarias de esas correcciones son construcciones sintéticas.

### 6.5 V-DXF-4 (informativa)

La resolución pide repetir V-DXF-4 porque cambió la evaluación de extremos geométricos. Un sondeo
del directorio temporal de la sesión, añadido al conjunto de pruebas de Desktop con un
`--init-script` y retirado después del árbol de compilación, escribió sobre el candidato las 11
exportaciones DXF estrictas de V-DXF-1: `o`, `v`, `w`, `a_1`, `e_1`, `o_1` y `e_2` en el estado
guardado, y `o`, `v`, `w` y `a_1` en la mordedura de excCyl 3,7. AutoCAD 2026 Core Console
(`accoreconsole.exe`) abrió cada archivo con un guion `.scr` que cuenta las entidades con AutoLISP
y ejecuta `AUDIT` sin corregir. En los 11 archivos no hubo errores de carga, `AUDIT` dio «Total
errors found 0 fixed 0» y AutoCAD no modificó ningún archivo; hay una `LWPOLYLINE` por archivo,
salvo en `e_2`, con tres: sus tres partes abiertas con los dos cortes declarados. Es el mismo
resultado que en T13. Comprobación externa e informativa, no una puerta.

### 6.6 Otras comprobaciones

| Comprobación | Resultado |
|---|---|
| Escaneo de autoridad de G9X1 (V-PROD-2) | `Assert-GeoCeDGDxfSourceAuthority` sobre los 46 caminos del perímetro de `g9x1-evidence.json`: las mismas tres faltas preexistentes en la base `070af6f9`, en `2856f246` y en el candidato (el roadmap y `modified-files.yml`, que nombran `LocusRenderCache`, y `AppGeoCeDG.java`, con `EuclidianView`); no empeora |
| Límite upstream | `Assert-GeoCeDGUpstreamBoundary` (base `9b93256b`) correcto: 1 162 archivos registrados en `docs/upstream/modified-files.yml`, los mismos que en `2856f246` (ningún archivo nuevo bajo `source/`) |
| Libro y gobernanza | `geocedg_book` en `78fbc88e9e12346b89d6ded411eaae94de2284b9`, limpio; `AGENTS.md`, `CLAUDE.md`, `FIRST_AGENT_TASK.md`, `ai-shell/prompts/` y `.github/prompts/` sin cambios |
| Árbol de trabajo e índice | limpios antes de cada ejecución de aceptación, de los bancos y del sondeo de V-DXF-4 |
| Publicación | `main` = `origin/main` = `48a0775e4f2f122f5e2614c62b5773777752ccd1`; el remoto no tiene la rama correctiva; nada se ha publicado, fusionado ni etiquetado |

### 6.7 Lo que no se ha ejecutado

- `FINAL`: la resolución no lo exige salvo que el contrato de cierre o la autoridad ejecutable
  requieran un recibo, y no lo requieren para la clase `INTEGRATED_PHASE`. `FINAL` añade a
  `INTEGRATION` las hojas de `INFRA_UNIT`, que se ejecutaron aparte sobre el mismo candidato; solo
  `FINAL` escribe `verification-receipt.json`, así que un cierre ligado a un recibo necesitaría
  esa campaña, por decisión autoral (§10).
- La comprobación manual del autor sobre el candidato: pendiente, no se simula.
- Los escenarios `composition` y `windowed` del arnés, como en `2856f246`: las referencias de
  composición las comprueban las `PHASE` de `CU0` y de `CU1`.

## 7. Comparación con el candidato anterior

| Aspecto | `2856f246c` | Candidato final |
|---|---|---|
| Producto | — | 10 archivos de `source/shared/common/src/main` (253 líneas añadidas y 30 retiradas): H-CU2A-6 a H-CU2A-11, R7-A a R7-D, comentario de C-2 y gancho de paquete de C-9; ningún cambio fuera de `kernel/locus`, ninguno en código upstream |
| Pruebas | — | 13 métodos nuevos (9 compartidos y 4 de Desktop) y aserciones reforzadas en pruebas existentes (§2, §5 y §12 de la revisión de trazabilidad); ninguna aserción debilitada |
| Conformidad | 34 filas de R5 tratadas (24 efectivas, 9 efectivas con una decisión de nivel C pendiente y 1 residuo); 12 residuos menores; V-DET-1 abierta | §4: ninguna fila obligatoria con un elemento sin aseverar |
| Decisiones pendientes | C-1 a C-12 | ninguna, salvo las de §10 |
| Inventario JUnit | `pre-g9b-cu2.shared` 233 y `pre-g9b-cu2.desktop` 68; `final.shared` 7 248 y `final.desktop` 2 247 | 242 y 72; 7 257 y 2 251; 0 fallos (§6.2) |
| Puertas `INTEGRATED_PHASE` | las seis `ACCEPTED / COMPLETE` a la primera | las seis `ACCEPTED / COMPLETE` a la primera, con los mismos planes (§6.3) |
| Referencias de `CU0` y `CU1`, indicadores de certificación y bancos | `preserve` iguales a la línea base; frente a `39741a0b`, 12 `pins` y 33 giros de teselación atribuidos a H-CU2A-3 | iguales hoja a hoja a los de `2856f246` (§6.4) |
| V-DXF-4 | no repetida; vale la de T13 | repetida con AutoCAD Core Console: sin errores, como en T13 (§6.5) |

## 8. Limitaciones aceptadas y trabajo futuro

Ninguna limitación certifica nada que no esté probado ni retira una garantía aprobada: todas fallan
cerradas o son evidencia sin autoridad. Aceptar una limitación no obliga a mantenerla.

| Id | Clase | Propietario o destino | Disposición |
|---|---|---|---|
| `L-CU2-SQRT-END-LENGTH` | limitación computacional tipada aceptada (C-10) | trabajo futuro sin asignación ejecutiva ni autorización (estimación con cambio de variable en las transiciones de raíz cuadrada certificadas) | la longitud total de un arco con extremos de raíz cuadrada es `NOT_ESTABLISHED` con `WORK_BUDGET_EXHAUSTED` (`MAXIMUM_DEPTH`); el contrato métrico de `CU1` no cambia; referencias independientes en el registro de la corrección §5 |
| `L-CU2-PERIODIC-CANONICALIZATION` | limitación aceptada (C-11); es F-CU2-17 y O-CU2A-1, con B-2 | ninguna asignación | la canonicalización periódica `lower + (t − lower) − ⌊…⌋·período` desplaza unos ulp un parámetro del dominio fundamental y es idempotente en su imagen; lleva `nextDown(π)` al otro lado de la misma costura y, en un dominio cerrado por abajo, puede devolver el extremo excluido, que el dominio rechaza; las direcciones persistidas no cambian; los extremos incluidos se evalúan en su propio parámetro (H-CU2A-3 y H-CU2A-6) |
| `L-CU2-CLOSED-PERIODIC-SEAM` | limitación aceptada (C-12) | ninguna asignación | las componentes cerradas periódicas conservan la pertenencia heredada con tolerancia junto a la costura, ventana `[π − 10⁻¹², π)`; las referencias de `CU0` y `CU1` no cambian |
| F-CU2-1 | comportamiento heredado documentado | — | exceso de evaluación del cálculo heredado (ventana de tangencia, incidencia) más allá del extremo exacto; nunca se publica como existencia (O-CU2A-6) |
| Riesgo residual de H-CU2A-8 | limitación de disponibilidad numérica | — | junto a la transición exacta de una raíz con el inicio de un rayo o segmento, dentro del redondeo del cálculo heredado, el signo del parámetro calculado puede diferir del exacto, sin cota a priori en esta corrección; donde la componente certificada existe, la consulta falla cerrada con `SELECTION_NOT_REPRODUCED`, que la definición tipa como fallo computacional, y donde se certifica la inexistencia la definición no publica nada, como con el exceso de F-CU2-1 |
| Limitaciones residuales de R7 | aceptadas con R7 | — | [registro de R7](pre_g9b_cu2_r7_correction_record.md) §6, con la precisión R7-E y las notas de §5.1 |
| `L-CU2-INDEPENDENT-EXPRESSION-STATE` | limitación propuesta (§5.2) | trabajo futuro sin asignación | requiere la aceptación del autor (§10) |
| O-CU2A-7, O-CU2A-8, O-CU2A-9 | observaciones (§5.2) | trabajo futuro sin asignación | identidad de selección de los certificados compuestos; deduplicación de marcas de giro junto a la costura; dos eventos métricos coincidentes en un corte |
| Construcciones fuera de la clase v3; alcance de V-CON-6; C09; C51 y C70 | limitaciones de T13 | — | [informe del candidato de T13](pre_g9b_cu2_implementation_candidate_report.md) §10, sin cambio |
| C22, C23 y C40 | trabajo de `CU3` | `CU3`, que requiere autorización propia | materialización de raíces en componentes puntuales y seguimiento transversal de raíces |
| `e_2`: cortes computacionales; TD-CU1-W1 | deuda de la métrica de `CU1` | paquete métrico posterior | `w_1` con `PARAMETER_RESOLUTION_REACHED` en tres conductores; `e_2` declara sus cortes |
| `L-CU1-E2-LENGTH` | cuestión de `CU1` asignada a `CU2`, respondida | — | el salto de la longitud orientada es el evento estructural certificado `ORIGIN_WRAP` (D-CU2-3) y `e_2` es un arco abierto de longitud completa desde `t_N = 0`, `COMPLETE` (B10.2); su longitud sigue en la clase de su estimación |
| `L-CU1-CLASS2-REACH`, `L-CU1-AFFINE-FORMS` | limitaciones de `CU1` | paquete métrico posterior | `CU2` no las necesitó para su alcance |
| `OBS-CU2-OFF-EDT-TOOLBAR-RACE` | carrera preexistente de una prueba de Desktop | tratamiento de la autorización de T13-R1/R2 §4 | no se presentó en la `INTEGRATION` del candidato correctivo ni en la del final; no se tocan Swing ni pruebas ajenas a `CU2` |

## 9. Estado de E1

El [plan de transferencia científica](../architecture/pre_g9b_cu_scientific_methods_editorial_handoff_plan.md)
está en su versión 4: las filas SM-D-1, SM-D-7, SM-D-8, SM-E-2, SM-E-3, SM-E-6, SM-E-8, SM-E-10 y
SM-E-11, y las fichas de SM-D-7, SM-E-2 y SM-E-8, recogen el candidato final, las decisiones C-1 a
C-12 y las correcciones de §5; ninguna decisión C-n figura ya como pendiente. El mandato editorial
del autor está aprobado (2026-10-20); el diseño detallado de los apéndices sigue pendiente de
preparación y revisión, y su redacción e incorporación a `geocedg_book` no están autorizadas.
`geocedg_book` no ha cambiado (`78fbc88e9e12346b89d6ded411eaae94de2284b9`). Este paquete no pide
cambiar ese estado.

## 10. Propuesta de cierre de `CU2`

Se propone al autor:

1. **Declarar `PRE-G9B-CU2` `PASS — AUTHOR APPROVED`** sobre el candidato final de §1, con su
   descendiente documental, si su comprobación manual lo confirma. Esa comprobación sigue
   pendiente, y ninguna prueba automatizada la sustituye.
2. **Aprobar como texto normativo** el que `CU2` incorporó y que hoy dice «pendiente de revisión
   autoral»: `locus-v2-existence-components.md` 1.1 §15 (con D-CU2-T13-R1), el modelo de
   construcción certificada 1.2 §10 (con §10.10 y la precisión C-8), `locus-v2-metrics.md` 1.6,
   `locus-v2-semantics.md` 1.3 (con `DIMENSION_CHANGED`), `locus-v2-presentation.md` 2,
   `dxf-curve-fidelity-and-approximation.md` 1.2, `latex-semantic-curve-export.md` 1.0 §5 y la
   nota de alcance A2.5, y la implementación del ADR 0038. El commit de cierre conciliaría sus
   líneas de estado, como hizo el de `CU1`.
3. **Aceptar las limitaciones de §8** con su disposición, incluida la propuesta
   `L-CU2-INDEPENDENT-EXPRESSION-STATE`, y dejar como trabajo futuro sin asignación la estimación
   con cambio de variable de C-10 y las observaciones O-CU2A-7 a O-CU2A-9.
4. **Forma del cierre:** un registro de cierre y su espejo JSON, como en `CU0` y `CU1`, con la
   evidencia `INTEGRATED_PHASE` del candidato exacto. No hace falta `FINAL`: ni el plan (`FINAL`
   una vez en `CU10`) ni la autoridad ejecutable lo exigen para esta clase; solo sería necesario
   si el autor quiere un cierre ligado a un recibo de `tools/agent/phase-closeout.ps1`.
5. **Fuera del cierre, a decisión del autor:** la publicación de la rama de revisión, la
   promoción a `main`, cualquier etiqueta y la autorización de `CU3`. `geocedg_book` no cambia.
