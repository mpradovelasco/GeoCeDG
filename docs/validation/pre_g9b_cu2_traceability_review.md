# PRE-G9B-CU2 — Revisión sustantiva de la trazabilidad de la matriz de validación

| Campo | Valor |
|---|---|
| Carácter | Registro de revisión del descendiente correctivo de `CU2`. No es evidencia de ejecución ni aprobación; `selfApproved=false` |
| Origen | Decisión autoral D-CU2-T13-R4 (2026-10-22), «Control de conformidad»: distinguir los identificadores de `CU2` de sus homónimos de `CU1`, no tomar la aparición textual de un identificador como cumplimiento y asociar cada requisito obligatorio con pruebas efectivas |
| Matriz | [matriz de validación de `CU2`](pre_g9b_cu2_validation_matrix.md), §2 y §3; su §6 resume esta revisión fila a fila |
| Base de la revisión | rama local `phase/pre-g9b-cu2-corrective` tras los commits de R1, R2 y R4 y el refuerzo de §6; el candidato congelado y su verificación constarán en el registro de verificación del descendiente |
| Hallazgos | H-T13-A y H-T13-B (§9), deficiencias preexistentes de fallo cerrado; D-CU2-T13-R6 corrigió H-T13-A, y H-T13-B quedó detenido por un contraejemplo junto al hallazgo nuevo H-T13-C ([registro de R6](pre_g9b_cu2_r6_correction_record.md)); D-CU2-T13-R7 corrigió H-T13-B y clasificó H-T13-C ([registro de R7](pre_g9b_cu2_r7_correction_record.md)); el mandato de autonomía correctiva de 2026-10-23 trató R5 (§10, [inventario](pre_g9b_cu2_r5_gap_inventory.md), [registro de la corrección](pre_g9b_cu2_corrective_autonomy_record.md)) |
| Corrige | la auditoría de T13 ([registro de verificación de `39741a0b`](pre_g9b_cu2_verification_record.md) §2.3), que contó como cubiertas V-DET-1, V-INV-1 y V-INV-2 por las citas homónimas de `CU1` y que solo buscaba citas |

## 1. Método

La decisión D-CU2-T13-R4 pide distinguir los identificadores de `CU2` de sus homónimos de `CU1`,
no tomar la aparición textual de un identificador como prueba de su cumplimiento y asociar cada
requisito obligatorio con pruebas que ejerzan sus propiedades con aserciones sustantivas. La
revisión tiene tres pasos.

1. **Citas.** Un generador de solo lectura recorre las fuentes de prueba compartidas y de
   escritorio y asocia cada fila de §2 y §3 de la matriz con los métodos `@Test` cuyo Javadoc la
   cita y, como «(clase)», con las clases que la citan solo en el Javadoc de clase, en un auxiliar
   o en un comentario; los rangos «V-X-1 a V-X-n» se expanden. Las clases `PreG9BCu1*` se excluyen,
   porque citan las filas homónimas de la matriz de `CU1` (V-DET-1, V-INV-1, V-INV-2, V-CU0-1 a
   V-CU0-3 y V-PROD-1, con otro significado allí).
2. **Primera pasada sustantiva.** Cuatro revisores de solo lectura leen, para cada una de las 122
   filas de tipo F, A y de las puertas GC, el requisito y la aserción esperada, y el código de los
   métodos citantes y de sus auxiliares. Veredicto: `EFFECTIVE` (todo elemento esencial de la
   aserción esperada está aseverado), `PARTIAL` (falta un elemento esencial o la construcción
   difiere de la exigida) o `NOMINAL` (la cita es solo textual).
3. **Segunda pasada sobre todo el corpus.** Para cada veredicto `PARTIAL` o `NOMINAL`, el mismo
   revisor busca en todas las pruebas, citen o no la fila, aserciones del elemento que falta, y
   clasifica: `CONFIRMED` (ninguna prueba lo asevera), `COVERED-BY-UNCITED` (lo asevera una prueba
   que no cita la fila), `MINOR` (el elemento es secundario) o `RETRACTED`.

Las filas que cambia este descendiente las revisa además un revisor independiente con el mismo
criterio. La revisión es un análisis del agente: no es evidencia de ejecución ni aprobación
autoral, y sus veredictos quedan sujetos a la revisión del autor.

## 2. Resultado global

| Clasificación | Filas | Significado |
|---|---|---|
| Efectivas en la primera pasada | 52 | las pruebas que citan la fila aseveran cada elemento esencial |
| Cubiertas por pruebas que no la citan | 18 | el elemento que faltaba lo asevera otra prueba; se corrige la trazabilidad |
| Residuo menor | 12 | falta un elemento secundario, que se registra |
| Hueco confirmado | 34 | ninguna prueba asevera un elemento esencial de la fila |
| Informativa | 1 | GC-7, que miden los bancos |
| Filas de este descendiente | 5 | V-COINC-6 y V-COINC-9, completadas con el modelo; V-DET-1, V-INV-1 y V-INV-2, nuevas (§6) |
| **Total** | **122** | filas de tipo F, A y puertas GC |

## 3. Huecos confirmados

Ninguna prueba del repositorio asevera el elemento indicado. Se agrupan por tema.

| Fila | Tipo | Elemento no verificado |
|---|---|---|
| GC-1 | GC | el contador de cálculos de certificado mientras se mueve el conductor de un locus del modelo, dentro y fuera de un hueco |
| GC-2 | GC | la cota de un cálculo de certificado por locus afectado y revisión tras cambiar una entrada no conductora |
| GC-3 | GC | el contador de cálculos con los consumidores métrica, intersección, punto semántico y exportaciones DXF y LaTeX (el dibujo y la exportación de imagen los cubre V-INV-1) |
| GC-8 | GC | los presupuestos del certificado exacto con rbCone 8 y 10 (V-PER-3) y en las variantes de V-CRIT-9 |
| V-LVL-1 | F | que la evidencia exploratoria no tiene autoridad sobre el grafo de dependencias ni sobre la identidad |
| V-LVL-2 | F | guardar y reabrir un locus fuera de la clase y cambiar solo la política exploratoria: misma huella y evidencia recalculada |
| V-UND-1 | F | un fallo real del cálculo de una dependencia antes de certificar; la causa `COMPUTATIONAL_DEPENDENCY_FAILURE` solo se inyecta en pruebas y ningún código productivo la emite |
| V-PER-3 | A | la localización independiente de los conductores degenerados de `o_1` |
| V-E2-5 | F | que solo el punto cuyo cálculo falla queda `EVALUATION_FAILED` |
| V-REV-EXACT-3 | F | para `lr2`, los bits de la copia, la fidelidad declarada y la igualdad con el vivo |
| V-REV-EXACT-7 | A | la causa tipada de la revisión que publica un cambio real del estado de selección o de etiquetado |
| V-REV-EXACT-8 | F | contadores en dos ejecuciones de los estados de V-REV-EXACT-1 a V-REV-EXACT-7 (solo hay un caso sintético) |
| V-REV-EXACT-9 | F | el certificado tras reabrir el estado con pérdida del literal angular |
| V-REV-EXACT-10 | A | un cambio de un ulp o un valor extremo que alcanza un locus anidado; el valor `−1e-300` |
| V-SEL-7 | F | la revisión con causa tipada cuando cambia el estado de selección o de etiquetado; deshacer y rehacer un estado `[1, 0]` |
| V-LIN-4 | A | un selector que interseca dos componentes certificadas: `AMBIGUOUS` no se asevera en ninguna prueba |
| V-COINC-2 | A | las configuraciones del modelo de §3.5c: `lr2` `NOT_ESTABLISHED`, banda tipada, componentes locales, recuentos 49/55 de §1 |
| V-COINC-3 | F | `R2` creada en `t_c`; la regla de primera inicialización tras reabrir; la causa tipada del cambio de estado |
| V-COINC-5 | F | el extremo de `lr2` a excCyl 3,7, no certificado y dentro de la banda |
| V-COINC-7 | A | las salidas etiquetadas definidas en las 201 muestras; en el modelo, las diferencias de `R2` dentro de la banda |
| V-COINC-8 | F | la identidad del certificado de `lr2` al deshacer, rehacer y recalcular; la causa tipada cuando cambia el estado |
| V-POINT-4 | F | el evaluador en los binary64 vecinos de `t*`; un fallo forzado de la forma límite; los puntos rechazados junto a `t*` |
| V-POINT-5 | F | ruta de longitud cero; `Point(L, u)` cerca de `t*`; declaraciones escritas en DXF y LaTeX; inspector; la construcción de V-POINT-2 |
| V-POINT-6 | F | un locus dependiente de un soporte solo puntual que declara su limitación; imágenes por semejanza que heredan las componentes puntuales |
| V-CON-1 | F | en el modelo: rutas métricas, costura de las intersecciones, interacción sobre un arco y exportaciones en los binary64 críticos |
| V-CON-3 | F | la longitud total de un arco certificado del primer grupo en mordedura y de los loci críticos sintéticos, con su clase de §25 |
| V-CON-4 | F | la intersección de un arco certificado con una recta, el marco de fase y los tokens a través de una revisión real |
| V-CON-7 | F | una semejanza (incluida la simetría) de una mordedura: estructura, maximalidad, cierre y claves de componente |
| V-B06-1 | F | el giro en el vértice de cierre de una componente cerrada (último a primer segmento) |
| V-B06-3 | F | el determinismo con la misma vista y la invariancia de zoom y resolución en la mordedura y en la configuración crítica sintética |
| V-B07-2 | F | el tramo de raíz cuadrada del extremo superior y los tramos de los extremos en las mordeduras del modelo (3,7, 3,85 y 3,9) |
| V-B07-4 | F | el estilo punteado y translúcido del dibujo de la evidencia exploratoria |
| V-AVAIL-6 | F | la clase de garantía de las coordenadas dibujadas en un resultado parcial |
| V-TEX-1 | F | la compilación de Asymptote; la compilación PGF y PSTricks se omite sin aviso si faltan las herramientas; la topología LaTeX de la configuración crítica |

## 4. Cubiertas por pruebas que no las citan

| Fila | Pruebas que verifican el elemento | Residuo |
|---|---|---|
| GC-5 | `PreG9BCu2RepresentationTest.theVertexBudgetCoarsensWithoutCrossingBoundaries`, `PreG9BCu2AuthorModelRepresentationTest.turnsAreBoundedOutsideTheCertifiedFolds` | ninguna aserción literal del recuento frente al presupuesto de 4096 |
| V-LVL-3 | `PreG9BCu2RepresentationTest.aLocusWithoutEstablishedExistenceIsDeclaredWithTheMandatorySidecar`, `PreG9BCu2ConsumerMigrationTest.thePrincipalSelectorUsesTheLocalCertifiedComponents`, `PreG9BCu2ConsumerMigrationTest.anOpenBoundaryNeedsACompleteInventory` | — |
| V-UNIF-1 | `PreG9BCu2ClassV3ModelTest.everyInvalidReasonIsTypedAndOnlyItCertifiesNonexistence` | — |
| V-CRIT-6 | `PreG9BCu2RepresentationTest.theSingularParameterIsAnExactUnsmoothedVertex`, `PreG9BCu2SelectionSettlementTest.theOperativeOutputIsTheRootThatTheCertifiedSelectionDesignates` | no se asevera que `Q` invierta su movimiento sobre `p` |
| V-CRIT-7 | `PreG9BCu2AuthorModelConsumerTest.anEvaluatorPointInACertifiedGapIsNotPublished` | un solo valor de mordedura, un locus y un punto |
| V-CRIT-8 | `PreG9BCu2LineageTest.aPointComponentChangesDimensionThroughTypedTransitions` | en el modelo, los certificados se comparan por `hashCode` |
| V-CRIT-10 | `PreG9BCu2AuthorModelLineageTest.movingEndsKeepTheLineageAndTheBiteIsATypedTransition`, `PreG9BCu2LineageTest.theLineageFollowsStructuralSourcesNotEnds` | las configuraciones de V-CRIT-9 no se certifican dos veces |
| V-FOLD-3 | `PreG9BCu2AuthorModelRepresentationTest.turnsAreBoundedOutsideTheCertifiedFolds`, `PreG9BCu2AuthorModelCertificationTest.contiguousBinary64ValuesAreClassifiedExactlyAndDeterministically` | los loci en `bb` no se dibujan |
| V-E2-3 | `PreG9BCu2AuthorModelRepresentationTest.theExcludedEndOfE2IsItsOneSidedLimit`, `PreG9BCu0CompositionCharacterizationTest.orientedLengthResetIsDrawnAsAClosedPeriodicCurve` | — |
| V-REV-EXACT-6 | `PreG9BCu2AuthorModelClassV3Test.theProgramDoesNotDependOnTheDriverPosition`, `PreG9BCu2SelectionSettlementTest.driverMovesAndQueriesDoNotRebuildSettleOrRecertify` | arrastres del modelo dentro del hueco sin contadores |
| V-SEL-1 | `PreG9BCu2AuthorModelSelectionSettlementTest.theModelOutputsAreTheRootsThatTheCertifiedSelectionDesignates`, `PreG9BCu2SelectionSettlementTest.theOperativeOutputIsTheRootThatTheCertifiedSelectionDesignates` | — |
| V-SEL-2 | `PreG9BCu2AuthorModelCertificationTest.everyRevisionPublishesItsOwnGeometry`, `PreG9BCu2AuthorModelSelectionSettlementTest.theModelVariantsStartedInTheGapCaptureTheSameCertificates` | — |
| V-SEL-3 | `PreG9BCu2ClassV3ModelTest.labelledOutputsAtTheCoincidentRootAreBothDefined`, `PreG9BCu2SelectionSettlementTest.theOperativeOutputIsTheRootThatTheCertifiedSelectionDesignates` | `Q = R` solo en los extremos de 3,7 en el modelo |
| V-SEL-4 | `PreG9BCu2AuthorModelLineageTest.movingEndsKeepTheLineageAndTheBiteIsATypedTransition` | — |
| V-SEL-5 | `PreG9BCu2AuthorModelCertificationTest.everyBiteIsOneArcAcrossTheSeamWithTheAnalyticEnds`, `PreG9BCu2AuthorModelSelectionSettlementTest.theModelVariantsStartedInTheGapCaptureTheSameCertificates` | — |
| V-COINC-1 | `PreG9BCu2AuthorModelSelectionSettlementTest.oneUlpChangesAtEveryDriverPositionReproduceTheSelection`, `PreG9BCu2AuthorModelSelectionSettlementTest.theModelOutputsAreTheRootsThatTheCertifiedSelectionDesignates` | las dos salidas definidas y coincidentes en el binary64 de mordedura |
| V-CON-5 | `G9U0R6SemanticLocusPointInteractionTest.r617DisconnectedComponentsRejectGapSelectionAndPreventPointJump` | — |
| V-DXF-1 | `PreG9BCu2RepresentationTest.theSingularParameterIsAnExactExportVertex`, `PreG9BS1AuthorDxfReproductionTest.semanticCurvesExposeApprovedExtendedDxfDisposition` | el orden de los vértices no se comprueba |

## 5. Residuos menores

| Fila | Residuo |
|---|---|
| GC-4 | `e_2` no se comprueba frente al presupuesto de cajas; el agotamiento tipado se admite |
| V-V3-3 | el modo continuo no puede activarse ni cargarse en el producto, así que su caso no puede darse |
| V-PER-1 | ninguna prueba aísla la celda de la costura |
| V-FOLD-2 | el `e_1` desplazado no se dibuja; el refinamiento con diagnóstico solo se asevera de forma genérica |
| V-FOLD-4 | el punto degenerado no se dibuja; el refinamiento con diagnóstico solo se asevera de forma genérica |
| V-E2-8 | la consulta del límite del inicio excluido con `full-cycle` (`w_1 = 0`) |
| V-E2-9 | una comprobación de coordenadas propia de LaTeX |
| V-SEL-6 | ningún orden dibujo-métrica o métrica-dibujo, aunque el certificado se calcula una vez por revisión |
| V-POINT-1 | la operación y la dirección literal de la raíz de la fuente |
| V-CON-2 | el arco del modelo no se recorre con una ruta |
| V-AVAIL-3 | PSTricks y Asymptote comparten el código comprobado de PGF |
| V-DXF-3 | el residuo de cierre no se mide en los loci cerrados del modelo |

## 6. Filas de este descendiente

Un revisor independiente leyó la primera versión de las pruebas de las siete filas que cambia el
descendiente (R1: V-POINT-3; R2: V-REV-EXACT-5, V-COINC-6 y V-COINC-9; R4: V-DET-1, V-INV-1 y
V-INV-2). Sus observaciones se incorporaron a las pruebas sin debilitar ninguna aserción. Las
pruebas citadas son las de la versión reforzada.

| Fila | Revisión independiente | Refuerzo incorporado | Pruebas | Estado |
|---|---|---|---|---|
| V-POINT-3 | efectiva; sin aseverar la multiplicidad de las raíces del arco, `t*` dentro del arco ni un renombrado | las dos raíces del arco son simples (multiplicidad 1), sus cajas son los extremos del arco y `t*` queda dentro; renombrar la salida en el punto y en el arco publica revisiones sin cambiar claves ni linaje | `PreG9BCu2LineageTest.aPointComponentChangesDimensionThroughTypedTransitions`, `PreG9BCu2LineageTest.renamingTheTracedOutputChangesNoKeyAndNoRelation`, `PreG9BCu2ExistenceStructureV2Test.aChangeOfDimensionRelatesOnlyThroughUniqueStructuralSources` | efectiva |
| V-REV-EXACT-5 | parcial: `lr2` en el modelo no se comparaba con el conductor en `t_c`; el literal angular se comparaba entre dos construcciones nuevas; identidades durables con comodín | variantes `lq`, `lr` y `lr2` del modelo comparadas con el conductor en `t_c`; el literal angular se cambia por programa y se recalcula; las identidades durables se nombran por la etiqueta o el algoritmo de su objeto | `PreG9BCu2AuthorModelSliceFidelityTest.everyStateWithTheDriverAtTheDoubleRootMatchesTheModelSetFromScratch`, `PreG9BCu2AuthorModelSliceFidelityTest.everyStateWithTheDriverAwayFromTheDoubleRootMatchesTheModelSetFromScratch`, `PreG9BCu2RevisionExactnessTest.everyStateMatchesTheSameConstructionBuiltFromScratch` | efectiva |
| V-COINC-6 | parcial: en el modelo, `lr2` no arrancaba sin muestra válida; regla de `lr2` y estado de `t2` sin aseverar; creación de la variante sin etiqueta con el conductor en el hueco sin probar | radio crítico y mordedura sintéticos, y los tres binary64 del modelo, arrancados donde la salida trazada no tiene muestra válida (penetración: la primera de las 201 muestras con `R2` suprimida; mordedura: `t_c`; 3,7: el hueco); certificados idénticos bit a bit, identidades durables incluidas, al reabrir; regla exacta de cada variante (la compactación, que designa las mismas raíces que la permutación `[0, 1]` de referencia) y estado de cada intersección con su salida tipada; la variante sin etiqueta y los loci creados por orden con el conductor allí | `PreG9BCu2SelectionSettlementTest.variantsCreatedWithoutIntersectionCaptureTheSameCertificates`, `PreG9BCu2AuthorModelSelectionSettlementTest.theModelVariantsStartedInTheGapCaptureTheSameCertificates` | efectiva; el par etiquetado creado por orden en el hueco revela H-T13-A (§9) |
| V-COINC-9 | parcial: las muestras sin raíz se omitían; holgura de `4·10⁻⁶` en el valor límite; sin muestras donde las raíces se separan; sin estados de deshacer, rehacer y recalcular | una salida definida sin raíz real en el conductor binary64 es el pie de la raíz doble (ventana heredada, F-CU2-1); nada se publica donde se certifica la inexistencia; el valor límite encierra sin tolerancia `(15, 20)` y los pies de 60 dígitos, calculados con las constantes capturadas, en los extremos del corchete (y en su centro, en el modelo); desplazamientos de `10⁻⁷` a `10⁻²`; estados recalculado, deshecho y rehecho | `PreG9BCu2SelectionSettlementTest.theOperativeOutputIsTheRootThatTheCertifiedSelectionDesignates`, `PreG9BCu2AuthorModelSelectionSettlementTest.theModelOutputsAreTheRootsThatTheCertifiedSelectionDesignates` | efectiva |
| V-DET-1 | parcial: faltaban clases de construcción (asentamiento, salida tipada, primera inicialización en una raíz doble, selectores, semejanzas) y estados (deshacer, rehacer, reabrir); firmas reducidas de los puntos estacionarios en el modelo | escenarios de asentamiento en un hueco, primera inicialización en la raíz doble, punto seleccionado por su parámetro e imágenes por semejanza; los eventos de la copia (desenlace del asentamiento, informe de fidelidad, regla capturada) y los contadores de certificación; deshacer, rehacer y reabrir en cada ejecución; firmas completas en el modelo | `PreG9BCu2HybridCertificationTest.twoRunsOfTheSyntheticConstructionsGiveIdenticalResults`, `PreG9BCu2AuthorModelCertificationTest.twoRunsOfTheAuthorModelGiveIdenticalResults` | abierta (D-CU2-T13-R6): efectiva para las clases cubiertas, pero no se repiten las variantes de V-E2-6, las configuraciones de V-CRIT-9 ni todos los valores de mordedura de V-TR-1, así que no está demostrada la repetición determinista de todos los escenarios que exige su contrato |
| V-INV-1 | efectiva; solo con los parámetros del autor y solo se aseveraba dibujada la primera familia | estados del autor, excCyl 3,7, primer binary64 de la mordedura y rbCone 8; componentes puntuales y banda sintéticas añadidas al documento; se dibuja todo locus visible | `PreG9BCu2AuthorModelRepresentationTest.zoomOutputResolutionAndViewSizeChangeNoCertificate` | efectiva |
| V-INV-2 | parcial: solo configuraciones de discriminante recta–circunferencia con rayo; constantes capturadas sin aseverar; en el caso redondeado, el clasificador del producto | par de componentes puntuales de V-POINT-2, con su punto en la costura; constantes capturadas iguales a las transformaciones exactas bit a bit; en el caso redondeado, los cambios de signo del discriminante de 60 dígitos en 4096 muestras solo donde el certificado cambia de estado, y forma de raíz doble de cada parámetro singular y componente puntual, sin clasificador | `PreG9BCu2HybridCertificationTest.translationsAndScalesOfTheWholeConstructionKeepTheStructure`, `PreG9BCu2HybridCertificationTest.theExactCertificateClassifiesTheTransformedConstantsAsCaptured`, `PreG9BCu2HybridCertificationTest.transformationsOfOrderTenToTheSixFailClosedOrKeepTheStructure` | abierta: efectiva para las configuraciones cubiertas, pero la variante sin etiqueta revela H-T13-B (§9) y no se incluye; con escala `10⁻³` y traslación `10³` en las unidades de la matriz, la definición efectiva heredada cambia (contraejemplo de H-T13-B) y los certificados etiquetados afirman existencia sin salida (H-T13-C), casos que las pruebas no ejercen porque transforman la base exacta ×125 ([registro de R6](pre_g9b_cu2_r6_correction_record.md) §3.4 y §4); pendiente de D-CU2-T13-R7 |

## 7. Filas efectivas en la primera pasada

GC-6, V-COMPL-1, V-CMP-1, V-ARC-1, V-ARC-2, V-V3-1, V-V3-2, V-V3-4, V-TR-1, V-TR-2, V-TOT-1,
V-PER-2, V-CRIT-1, V-CRIT-2, V-CRIT-3, V-CRIT-4, V-CRIT-5, V-CRIT-9, V-FOLD-1, V-E2-1, V-E2-4,
V-E2-6, V-E2-7, V-PUB-1, V-PUB-2, V-PUB-3, V-PUB-4, V-REV-EXACT-1, V-REV-EXACT-2, V-REV-EXACT-4,
V-REV-EXACT-5, V-LIN-1, V-LIN-2, V-LIN-6, V-COINC-4, V-POINT-2, V-POINT-3, V-CON-6, V-CON-8,
V-CON-9, V-B06-2, V-B07-1, V-B07-3, V-REP-1, V-REP-2, V-REP-3, V-AVAIL-1, V-AVAIL-2, V-AVAIL-4,
V-AVAIL-5, V-DXF-2 y V-TEX-2.

## 8. Decisión requerida D-CU2-T13-R5

La decisión D-CU2-T13-R4 limita la ampliación a las pruebas de V-DET-1, V-INV-1 y V-INV-2, sus
auxiliares y la trazabilidad, y pide asociar cada requisito obligatorio con pruebas efectivas. Las
34 filas de §3 no lo están, y sus pruebas quedan fuera de ese alcance. Son huecos de verificación
anteriores a este descendiente (pasos T1 a T13), no defectos demostrados del producto; alguna fila
puede revelar uno al probarla (por ejemplo V-UND-1, cuya causa tipada no emite ningún código
productivo).

| Opción | Contenido | Consecuencia |
|---|---|---|
| (a) | Completar las 34 filas con pruebas en este mismo descendiente, con la regla de R2 y R4 para los defectos (caracterizar, corregir dentro del perímetro de `CU2` o escalar) | candidato con todos los requisitos obligatorios verificados; más trabajo antes de congelar |
| (b) | Completar un subconjunto que el autor priorice (por ejemplo, las puertas GC, las configuraciones de §3.5c, las causas tipadas y V-UND-1) y registrar el resto como limitación con propietario | verificación parcial declarada |
| (c) | Congelar ahora con R1, R2 y R4 y registrar las 34 filas como no conformidad abierta para la revisión autoral | sin trabajo adicional; el cierre de `CU2` hereda la deuda |

La decisión D-CU2-T13-R6 no autoriza globalmente R5 y pide clasificar los 34 huecos antes de
decidir: el [inventario](pre_g9b_cu2_r5_gap_inventory.md) da, para cada fila, su identificador, su
obligatoriedad, la evidencia existente, la clasificación, el impacto, la corrección mínima y su
relación con R1, R2, R4 y R6.

## 9. Hallazgos del refuerzo y decisión requerida D-CU2-T13-R6

El refuerzo de §6 reveló dos deficiencias del producto. Las dos fallan cerradas (no publican nada
falso) y son anteriores al descendiente: sondas idénticas, ejecutadas sobre el árbol del candidato
`39741a0b` extraído con `git archive` y compilado aparte, dan la misma salida, y el código
implicado no ha cambiado desde entonces (R1 solo toca el linaje). Su corrección cambia el
comportamiento del producto fuera del perímetro de R1, R2 y R4, así que no se ha corregido ni se
ha fijado en ninguna prueba.

**H-T13-A — Etiqueta pendiente de la segunda salida.** Una intersección recta–circunferencia
creada con dos etiquetas mientras su segunda salida no está definida (conductor en el hueco de una
mordedura o en la raíz doble de la configuración crítica; también la orden `Intersect(p, t)` del
producto) etiqueta solo la primera: GeoGebra etiqueta solo objetos visibles y deja pendiente la
segunda. El locus de la primera salida se reconstruye fiel y su certificado es correcto (igual,
salvo identidades, al de la variante etiquetada). Las consultas puntuales de la certificación
definen la segunda salida en la copia, que recibe entonces su etiqueta pendiente. Cuando un
consumidor de programa de clase v1/v2 revalida la fidelidad (`LocusLength`, a través de
`StructuralLocusMetric2D`; no `Translate` ni `Intersect`), el informe tipa
`LABELS live=10 copy=11`, la copia deja de ser fiel y desde entonces toda evaluación del locus falla
(`RECONSTRUCTION_FIDELITY_NOT_ESTABLISHED`) mientras el certificado publicado sigue `COMPLETE` con
existencia certificada; recalcular no lo repara, porque no hay revisión nueva. Contradice la
coherencia que verifica V-COINC-9 (donde el certificado afirma existencia, el corte tiene salida).

**H-T13-B — Banda de la segunda salida sin etiqueta y traslaciones.** El predicado conservador de
la banda (`locus-v2-certified-construction-model.md` §10.3) exige un margen del discriminante mayor
que `10⁻³·máx(a², b², c²)`, con los coeficientes de la recta, y `c` crece con la distancia de la
recta al origen. La banda de `lr2` en la configuración crítica sintética mide 0,07 rad sin
trasladar, 0,105 con una traslación de `10³` sobre el radio 3125 y 0,4 con escala `10⁻¹` y
traslación `10³`; desde unas 30 veces el radio (escala `10⁻²` o `10⁻³` con traslación `10³`, o
traslación `10⁶`) cubre todo el dominio: certificado `NOT_ESTABLISHED`, `UNRESOLVED` en todo el
dominio y sin componentes locales. La salida heredada está definida en las 720 posiciones
muestreadas en todos esos casos, y su ventana de tangencia es como mucho `10⁻⁵` en el
discriminante. V-INV-2 solo admite el fallo cerrado con `10⁶`; en las unidades de la matriz
(radio 25) una traslación de `10³` son 40 radios, así que la variante sin etiqueta no conserva su
estructura con las traslaciones exigidas.

| Opción | H-T13-A | H-T13-B |
|---|---|---|
| (a) | Corregir en este descendiente: la copia reproduce el etiquetado de la revisión hasta la siguiente y no adquiere etiquetas pendientes en las consultas; sin cambio normativo; pruebas de regresión | Corregir en este descendiente: un predicado de banda que siga siendo conservador frente a la supresión heredada sin crecer con la posición absoluta (coeficientes referidos al centro de la circunferencia, ventana heredada acotada); pruebas de V-INV-2 con `lr2` |
| (b) | Corregir con una precisión normativa: el cambio de etiquetado que causan las consultas se tipa como salida, como el de la primera inicialización en D-CU2-T11-R1 | Precisar en la matriz que V-INV-2 admite el fallo cerrado de la variante sin etiqueta desde una traslación relativa dada, y registrarlo como limitación junto a `L-CU1-TRANSLATION-RESOLUTION` |
| (c) | Registrar como deficiencia preexistente, de fallo cerrado, con propietario y plazo | Igual |

**Resultado de D-CU2-T13-R6.** El autor eligió la opción (a) con alcance controlado. H-T13-A está
corregido en el commit `e9541c42e`, con pruebas que fallan sobre `39741a0b`. La corrección de H-T13-B
se detuvo antes del código: la caracterización exigida mostró que la definición efectiva heredada
no es invariante con escala `10⁻³` y traslación `10³` en las unidades de la matriz, y que en ese
régimen los certificados etiquetados afirman existencia sin salida (hallazgo nuevo H-T13-C, también
anterior al descendiente). Todo consta en el [registro de R6](pre_g9b_cu2_r6_correction_record.md),
que pide la decisión D-CU2-T13-R7. El [inventario de los 34 huecos](pre_g9b_cu2_r5_gap_inventory.md)
responde a la petición de R6 sobre D-CU2-T13-R5.

**Resultado de D-CU2-T13-R7.** El autor aprobó A(a) con B(i), con condiciones normativas. H-T13-B
está corregido: la banda de la segunda salida sin etiqueta es el predicado riguroso del primer
intento decisivo del cálculo heredado, la captura conserva el semieje que lee la prueba de
incidencia y V-INV-2 tiene la precisión B(i) y pruebas con la variante sin etiqueta, que fallan
sobre `39741a0b`. H-T13-C no es un defecto del producto: clasificado según la decisión, el régimen
S3 es un fallo exclusivamente computacional que el contrato vigente ya representa (existencia
matemática conservada y evaluación `EVALUATION_FAILED` con causa `COMPUTATIONAL_FAILURE`); el
registro de R6 lo describía de más. Se corrige la precisión F-CU2-1 y se fija el comportamiento en
pruebas. Todo consta en el [registro de R7](pre_g9b_cu2_r7_correction_record.md); D-CU2-T13-R5 sigue
pendiente, y V-DET-1 abierta.

## 10. Resolución por el mandato de autonomía correctiva

El mandato autoral de autonomía correctiva controlada de 2026-10-23 sustituye, para R5, a la
decisión D-CU2-T13-R5: autoriza las pruebas (nivel A), las correcciones de producto con contrato
aprobado y violado (nivel B) y reserva al autor las precisiones normativas (nivel C). El
[registro de la corrección](pre_g9b_cu2_corrective_autonomy_record.md) da el detalle; la §6 de la
matriz cita cada prueba nueva y resume el estado de cada fila.

Erratas de esta revisión, corregidas en §3: V-LVL-1 y V-B07-4 son de tipo F y V-PER-3 de tipo A,
como en la matriz. La cita de V-CON-7 era nominal (la imagen se construía con las ramas de la
fuente); la prueba nueva construye las imágenes con los cuatro comandos públicos. R2 y R4 cubrían
parte de V-REV-EXACT-3 y V-REV-EXACT-8; las pruebas nuevas cubren el resto.

| Estado tras el mandato | Filas |
|---|---|
| Efectiva | GC-1, GC-3, GC-8, V-LVL-1, V-LVL-2, V-PER-3, V-E2-5, V-REV-EXACT-3, V-REV-EXACT-8, V-REV-EXACT-9 (con H-CU2A-5), V-REV-EXACT-10, V-SEL-7, V-COINC-2, V-COINC-3, V-COINC-8, V-POINT-5 (con H-CU2A-1), V-POINT-6 (con H-CU2A-2), V-CON-1 (con H-CU2A-4), V-CON-4, V-CON-7, V-B06-1, V-B06-3, V-B07-2 (con H-CU2A-3), V-B07-4 |
| Efectiva, con una decisión de nivel C pendiente sobre la redacción o una causa | GC-2 (C-4), V-UND-1 (C-2), V-REV-EXACT-7 (C-1), V-LIN-4 (con H-CU2A-1; C-7), V-COINC-5 y V-COINC-7 (C-6), V-CON-3 (limitación O-CU2A-3; C-10), V-AVAIL-6 (C-3), V-TEX-1 (C-5) |
| Residuo | V-POINT-4: el fallo forzado de la forma límite no tiene gancho de prueba (C-9); los vecinos de `t*` son efectivos |

V-DET-1 tiene cobertura demostrable: la doble ejecución sintética y la del modelo comparan bit a
bit revisiones, huellas, certificados, eventos y contadores, propios y anidados, en los estados de
V-REV-EXACT-1 a V-REV-EXACT-7 (V-REV-EXACT-8).

## 11. Resolución autoral consolidada

La resolución autoral consolidada de 2026-10-23 aprobó las decisiones C-1 a C-12 de §9 del
[registro de la corrección](pre_g9b_cu2_corrective_autonomy_record.md). Las precisiones de las
filas se aplicaron en la matriz sin cambiar su obligatoriedad ni su tipo, y las pruebas se
reforzaron donde una decisión fija un elemento nuevo que aseverar:

| Fila | Decisión | Estado |
|---|---|---|
| GC-2 | C-4: por revisión publicada, un cálculo propio y uno por cada copia anidada reconstruida | efectiva: `aNonDriverChangeComputesOneCertificatePerAffectedLocus` asevera ahora también la regla de los anidados y que algún cambio reconstruye una copia anidada |
| V-UND-1 | C-2: `COMPUTATIONAL_DEPENDENCY_FAILURE` reservada | efectiva; la constante lo declara |
| V-REV-EXACT-7 | C-1: el etiquetado de una salida de un par etiquetado se tipa `STRUCTURE` | efectiva: `theVariantsCreatedAtTheDoubleRootKeepTheirComponents` asevera `STRUCTURE` y ningún `LABELS` |
| V-LIN-4 | C-7: «contiene el parámetro dentro de la tolerancia del dominio, entre hijas de una división» | efectiva, con la redacción precisada |
| V-COINC-5, V-COINC-7 | C-6: exceso de evaluación heredado, banda tipada y muestreo del modelo | efectivas, con la redacción y el muestreo precisados |
| V-CON-3 | C-10: limitación computacional tipada de las mordeduras (`L-CU2-SQRT-END-LENGTH`) | efectiva, con la limitación aceptada |
| V-AVAIL-6 | C-3: coordenadas dibujadas bit a bit las del evaluador, sin mejorar su clase | efectiva, con la redacción precisada |
| V-TEX-1 | C-5: órdenes de compilación y omisión visible sin herramientas | efectiva, con la redacción precisada |
| V-POINT-4 | C-9: fallo forzado de la forma límite | efectiva: `aForcedFailureOfTheLimitFormLeavesThePositionUnavailable`, con el gancho interno de paquete `ConstructionBoxModel2D.withholdLimitFormValue` |

C-8 es una precisión editorial de §10.3 del modelo de construcción certificada, sin cambio de
significado; C-11 y C-12 conservan el comportamiento vigente con sus limitaciones documentadas
(`L-CU2-PERIODIC-CANONICALIZATION` y `L-CU2-CLOSED-PERIODIC-SEAM`). Los doce residuos menores de
§5 se cierran en §12.

## 12. Cierre de los residuos menores

La resolución exige que toda fila obligatoria esté realmente ejercitada o tenga una decisión
autoral que defina su tratamiento. Cada residuo de §5 tiene ahora una prueba que asevera el
elemento que faltaba; la §6 de la matriz cita las pruebas nuevas y marca las filas «efectiva
(resolución consolidada)». La revisión científica focal, con sus correcciones H-CU2A-6 a
H-CU2A-11 y sus observaciones, consta en el [paquete de revisión final](pre_g9b_cu2_final_review_package.md) §5.

| Fila | Residuo de §5 | Prueba que lo cierra |
|---|---|---|
| GC-4 | `e_2` frente al presupuesto de cajas | `constructionDegeneraciesAreBandsThatNoComponentCrosses` incluye `e_2`: dentro del presupuesto, o agotamiento tipado `BUDGET_EXHAUSTED` sin `COMPLETE` |
| V-V3-3 | el modo continuo no puede activarse ni cargarse | `outOfClassShapesHaveNoProgram`: un documento que pide `continuous="true"` no lo activa y la intersección conserva su programa; el caso de la fila no puede darse en el producto |
| V-PER-1 | ninguna prueba aísla la celda de la costura | la celda `[Math.PI, nextUp(Math.PI)]`, evaluada como en el certificador, es válida, con la selección de las cajas de los dos lados canónicos y una envolvente del punto trazado que corta las de ellas: configuración crítica sintética (`lq`, `lr`) y los seis loci cerrados del modelo |
| V-FOLD-2 | el `e_1` desplazado no se dibuja | `theDisplacedE1DeclaresItsTurnsAndExemptsNone`: con `I` redefinido `2⁻⁴⁰` fuera de `i`, `e_1` sigue cerrada sin pliegue; su dibujo no exime ningún giro y declara `UNCERTIFIED_TURN` en `0` y junto a la costura, única causa de su fidelidad parcial |
| V-FOLD-4 | el punto degenerado no se dibuja | `aDegenerateStationaryPointIsRefinedAndDeclaredNeverExempted`: las únicas exenciones son la banda del pliegue de la costura; el punto degenerado se declara una vez en `0` |
| V-E2-8 | el límite del inicio excluido con `full-cycle` | `withFullCycleTheExcludedStartOfE2IsItsOneSidedLimit`: el gemelo de `e_2` que lee la longitud con `full-cycle` (`w_1` es entrada protegida de su escalar) incluye `t_N` en `s = 2π`; la consulta del inicio excluido da `w = 0` |
| V-E2-9 | una comprobación de coordenadas propia de LaTeX | `anUnevaluatedEndOfE2IsDeclaredAndNeverExtrapolated`: el último vértice escrito por PGF es el último vértice exportado y no el límite, a media unidad de la última cifra impresa |
| V-SEL-6 | ningún orden dibujo-métrica o métrica-dibujo | `drawingAndMeasuringInEitherOrderGiveTheSameResults`: con `[1, 0]`, dibujar y medir en los dos órdenes dan los mismos estados, el mismo certificado, calculado una vez, la misma longitud y el mismo dibujo |
| V-POINT-1 | la operación y la dirección literal de la raíz | `anExternalTangencyIsAPointComponent` asevera el origen (identidad durable de `Q` con `-INTERSECTION_OUTPUT`), el predicado `DISCRIMINANT` y la raíz `root-0` |
| V-CON-2 | el arco del modelo no se recorre con una ruta | `routesFollowTheBiteArcOfTheModelAcrossTheSeam`: en la mordedura de excCyl 3,7, la ruta entre las dos posiciones a `0,1` de la costura sigue el arco sin envolver; `wrap-to-start` y `full-cycle` se rechazan con su diagnóstico tipado |
| V-AVAIL-3 | PSTricks y Asymptote comparten el código de PGF | `anUnevaluatedEndOfE2IsDeclaredAndNeverExtrapolated` asevera la declaración `END_NOT_EVALUATED` también en PSTricks y Asymptote |
| V-DXF-3 | el residuo de cierre en los loci cerrados del modelo | `theClosureResidualOfEachClosedLocusIsMeasuredAgainstItsCertificate`: en los seis loci cerrados, el residuo es exactamente la distancia entre el inicio y la curva justo antes del punto de cierre, nunca la identidad trivial, que es 0 |

V-DET-1 seguía marcada «abierta» en la matriz desde §6 (D-CU2-T13-R6), aunque el mandato
correctivo amplió su cobertura (§10 y §7 del registro de la corrección). Las clases que §6
señalaba sin repetir se repiten ahora. La doble ejecución sintética cubre los seis casos de V-E2-6
(sentido atrás; origen en la costura; origen que depende del conductor; fuente solo localmente
certificada, con un locus que depende de su longitud; fuente degenerada de longitud cero; ruta
entre posiciones fijas), las políticas de misma posición y de extremo abierto de V-E2-8, leídas
por el adaptador escalar, y las construcciones de las correcciones H-CU2A-7 a H-CU2A-10. Ninguna
construcción da un salto que ninguna cuerda acote ni una estructura que falle computacionalmente
(V-UND-1): sus estructuras sintéticas se certifican sobre dos construcciones nuevas, con el mismo
resultado. La doble ejecución del modelo recorre los nueve valores de mordedura de V-TR-1 y los
cambios de V-CRIT-9 (`rbCylinder` a 2,0 y `α_cyl` a 35°). Las dos ejecuciones son idénticas bit a
bit; la fila queda «efectiva (resolución consolidada)».

Tras el cierre ninguna fila obligatoria queda con un elemento sin aseverar.
