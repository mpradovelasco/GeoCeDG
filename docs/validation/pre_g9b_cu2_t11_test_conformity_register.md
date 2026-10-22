# PRE-G9B-CU2 — Registro de conformidades de pruebas heredadas (paso T11)

| Campo | Valor |
|---|---|
| Carácter | Registro de las conformidades declaradas de las pruebas heredadas que modifica el paso T11 de la implementación de `CU2`. No es evidencia de ejecución ni aprueba nada |
| Estado | Candidato técnico del paso T11, en la rama local de implementación; pendiente de revisión autoral; `selfApproved=false` |
| Autoridad | [estrategia de implementación](../architecture/pre_g9b_cu2_implementation_strategy.md) §5, punto 5 («Cada cambio es una conformidad declarada con su justificación»); decisión del autor D-CU2-T11-R1 (2026-10-21), §5 |
| Base | `3e711f476` (paso T10) |
| Revisión | 2026-10-22: resolución autoral previa a T12 (H10, V-CON-6, componente puntual y tokens históricos), §7 |

Este registro declara, para cada prueba heredada que modifica el paso T11, qué fijaba, qué contrato
aprobado justifica el cambio, qué sigue verificándose, qué cobertura puede haberse perdido y qué
prueba la sustituye. La ejecución de las pruebas consta en el informe del paso, no aquí.

## 0. Alcance, instantánea y convenciones

- **Alcance.** Archivos `source/**/src/test/**` modificados respecto de la base. Las pruebas nuevas van
  en la sección 5. Queda fuera `source/desktop/desktop/src/main/.../LocusV2LaboratoryFixtures.java`
  (código principal), que recibe la misma migración que G1 y G2 con el contrato
  `locus-v2-laboratory-fixture/v1`.
- **Instantánea.** El registro corresponde al árbol del commit local del paso T11: **71 archivos de
  prueba modificados** (69 en la primera redacción y 2 más por la auditoría de la sección 3) y
  cuatro archivos nuevos. La sección 6 recoge lo añadido tras la primera
  redacción: la corrección de un defecto de producción que destapó una conformidad (C70), las
  conformidades C72 y C73, que retiran dos aserciones que pasaban en vacío, y los refuerzos que
  recuperan la cobertura que señalaban G6, C47, C51, C61 y C62.
- **Autoridad de las justificaciones.** Las «precisiones de la implementación (T11)» que se citan de
  [DIS] §6, §7, §8.1 y §9, de [CON] §4, §5.6, §6 y §7, y los añadidos de [SEM] y [EXI] son texto del
  candidato técnico del paso, pendiente de revisión autoral, salvo lo que se marca como decisión
  D-CU2-T11-R1. Las secciones base, aprobadas como arquitectura el 2026-10-21 ([APR]), están en la
  base.
- **Abreviaturas.** [CCM] `geocedg/specs/locus/locus-v2-certified-construction-model.md`;
  [EXI] `geocedg/specs/locus/locus-v2-existence-components.md`; [SEM]
  `geocedg/specs/locus/locus-v2-semantics.md`; [DIS]
  `docs/architecture/pre_g9b_cu2_hybrid_certification_design.md`; [CON]
  `docs/architecture/pre_g9b_cu2_consumption_and_representation.md`; [MAT]
  `docs/validation/pre_g9b_cu2_validation_matrix.md`; [APR]
  `docs/validation/pre_g9b_cu2_author_architecture_approval_record.md`.
- **Base contractual común de los fixtures fuera de clase**, abreviada como **«fuera de clase v3»**:
  - [CCM] §4 y §9: `SCALAR_STATE` solo con el certificado afín directo. Están fuera de clase los
    generadores con soporte locus y los puntos por expresión que no sean la traslación de §5.2.
  - [DIS] §4.1, «Fuera de la clase»: las expresiones afines no directas y las intersecciones
    cónica–cónica; un corte así queda `NOT_ESTABLISHED` con evidencia exploratoria visible.
  - [CON] §4, precisión de T11: los loci fuera de la clase v3 publican `NOT_ESTABLISHED` sin
    componentes, ningún punto semántico los direcciona, la longitud total se rechaza
    (`INVALID_QUERY`) y una intersección es `UNRESOLVED` sin raíces finitas. La decisión H10
    sustituye el rechazo de la longitud total por la estimación condicionada (§7.1).
- **Marcas.**
  - **ATENCIÓN:** toca la identidad persistida, el libro mayor de intersecciones, la orientación o
    la costura, DXF, LaTeX, la reconstrucción o reapertura de documentos, o un límite de `CU3`.
  - **POR VERIFICAR:** no se pudo establecer con el diff y los documentos. Tras la revisión de la
    sección 6 no queda ninguna: cada una se resolvió (con un cambio de prueba, una sonda o la cita
    del texto aprobado) o se convirtió en limitación declarada.
- **Formato de cada conformidad.** Seis campos: (1) comportamiento anterior; (2) contrato; (3)
  aserción antigua → nueva; (4) propiedad que se sigue verificando; (5) cobertura que puede
  haberse perdido; (6) prueba sustitutiva.

## 1. Conformidades agrupadas

### G1 — Constructor retirado `LocusBranch2D(…, List<LocusInterval2D>, …)` → `LocusV2TestBranches.branch`

Afecta a `G9S1R1SplinePairIntervalCertificationTest.spline`,
`PiecewisePolynomialPairIntersectionCapability2DTest.definition`,
`G7BMetricFixtures.periodicDefinition` y `.branch`, `G8AIntersectionTopologyCharacterizationTest.branch`,
`G8BIntersectionFixtures.branch` y `G9U0R2LocusRenderContinuityTest.unboundedTangent`.

También a `G9U0R5SemanticTransformEdgeCasesTest.fixtureBranch`, `G9U0R6SemanticLocusPointInteractionTest`
(`r629MultipleBranchesAreCanonicalUnderDefinitionOrderPerturbation`,
`r645PersistedComponentLineageWinsAtSharedSemanticEndpoint`) y
`LocusMetricProductiveImproperTest.unboundedDefinition`.

Y a `LocusV2HardeningBenchmarkTest.branch`, `LocusV2HardeningValueContractsTest.branch`,
`LocusV2KernelIntegrationTest.branch`, `LocusV2RenderHardeningTest` (dos sitios de
`adaptiveSamplingPreservesComponentsPeriodicSeamAndUnboundedClipping`) y `LocusV2RenderSeparationTest`
(`unboundedImageIsEvaluatedSemanticallyAndClippedOnlyForRender`,
`disconnectedValidComponentsProduceSeparateRenderSubpaths`).

Por último, a `LocusV2SessionHardeningTest.definition`,
`LocusV2ValueContractsTest.branchIdentityIsNotAValidComponentIdentity`, `PostG9U1A6OptionalBranchPointTest`
(`gapsAndMultipleComponentsFailExplicitly`, `branchLike`), `PreG9BCu1EstimatorTest.definition`,
`PreG9BCu1RevisionAndSessionTest.definition` y `PreG9BS1R2ExistenceStructureTest.compatibilityBranch`.

1. **Comportamiento anterior.** El constructor por lista creaba el certificado de compatibilidad
   (`completeCompatibility`, contrato `locus-existence/provider-compatibility/v1`, naturaleza `NONE`) y
   cerraba la rama si tenía la propiedad `PERIODIC`.
2. **Contrato.** [EXI] §15.7: el certificado de compatibilidad se retira y el certificado explícito
   solo se admite si el contrato prueba la existencia y la continuidad. [APR] D-CU2-7, opción A.
   [DIS] §9, precisión de T11 «Retirada» y «Productores estáticos y analíticos»: los fixtures publican
   `locus-existence/provider-proved/v1` con naturaleza `EXACT`. Contrato de fixture
   `locus-v2-test-fixture/v1`.
3. **Aserción.** `new LocusBranch2D(k, d, List.of(…), o, p, l, props, q)` →
   `LocusV2TestBranches.branch(k, d, List.of(…), o, p, l, props, q)`, con los mismos argumentos.
4. **Sigue verificándose.** Las mismas componentes declaradas, en orden canónico y `COMPLETE`, y la
   propiedad propia de cada prueba.
5. **Cobertura.** No es puramente mecánica. Los hechos pasan de `NONE` a `EXACT`, así que en esas
   pruebas se aplica P5 (G3a, G3b), un intervalo de longitud cero pasa a ser componente puntual (C53)
   y las claves de linaje pueden volverse estructurales. En `r645` se conserva la clave binary64
   porque las dos componentes comparten fuentes ([DIS] §7, precisión de T8).
   **ATENCIÓN:** el auxiliar declara cerrada toda rama `PERIODIC`
   (`providerProved(…, periodic, periodic, …)`), como suponía el constructor retirado, aunque la
   función del fixture sea discontinua en la costura. Es el caso de
   `G7BMetricFixtures.periodicDefinition`, que se usa con `(t, 0)` sobre `[-4, 4)` en
   `LocusMetricProductiveRouteTest.periodicLine` y en `LocusMetricProductiveBenchmarkTest`, dos archivos
   no modificados. **Resuelto:** es deliberado. `locus-v2-test-fixture/v1` es un contrato de prueba que
   declara, no un productor del producto, de modo que D-CU2-7 no lo alcanza. La discontinuidad es
   adversaria: `periodicSeamIsResolvedWithoutCartesianEqualityInference` comprueba que la ruta cruza la
   costura por el cierre del certificado y nunca por igualdad cartesiana, que aquí sería falsa. El
   javadoc de `periodicDefinition` cita ahora el contrato de fixture y explica esa intención.
6. **Prueba sustitutiva.** No procede (API retirada). La semántica del certificado explícito la cubre
   C64.

### G2 — `LocusV2Factory.fullDomainBranch(…, props)` → `fullDomainBranch(…, props, LocusV2TestBranches.TEST_FIXTURE_CONTRACT)`

Afecta a `G8AIntersectionFixtures.create`, `G8AIntersectionFunctionalBenchmarkTest.nestedChain`,
`G8AIntersectionTopologyCharacterizationTest.fullBranch`, `G8CDesignFunctionalBenchmarkTest.nestedChain`,
`LocusMetricProductiveLifecycleTest.fixture`, `LocusMetricProductiveNestedTest.locus` y
`LocusV2FunctionalBenchmarkTest.chain`.

También a `LocusV2KernelIntegrationTest` (`cuspAndCollapsedImageRetainTypedSemanticMeaning`, `branches`),
`LocusV2LifecycleHardeningTest.fixture`, `LocusV2RenderHardeningTest` (proveedor periódico de
`adaptiveSampling…` y `locus`), `LocusV2RenderSeparationTest.parabola` y `DrawablesTest.checkDrawables`.

1. **Comportamiento anterior.** Rama de dominio completo con el certificado de compatibilidad implícito.
2. **Contrato.** El de G1, más el javadoc nuevo de `LocusV2Factory.fullDomainBranch` (D-CU2-7).
3. **Aserción.** Se añade el argumento de contrato. El cierre sigue siendo `provider.isPeriodic()`, ahora
   con naturaleza `EXACT`.
4. **Sigue verificándose.** Lo mismo que antes.
5. **Cobertura.** Ninguna identificada. El único proveedor periódico (`LocusV2RenderHardeningTest`)
   dibuja una circunferencia continua.
6. **Prueba sustitutiva.** No procede.

### G3a — Hueco declarado: `OUT_OF_DOMAIN` → `DEPENDENCY_UNDEFINED`

Afecta a `G9U0R5SemanticTransformEdgeCasesTest` (`disconnectedGapSurvivesCollapsedImageAndLaterTransformClosure`
y dos aserciones de `multipleBranchesAndEmptyDefinitionRemainStructurallyExact`),
`LocusV2KernelIntegrationTest.providerOwnsDynamicBranchTopologyAndLineage` y
`LocusV2RenderSeparationTest.disconnectedValidComponentsProduceSeparateRenderSubpaths`.

1. **Comportamiento anterior.** Evaluar en el hueco entre componentes declaradas daba `OUT_OF_DOMAIN`.
   El certificado de compatibilidad no tenía prueba: «Los hechos sin prueba (compatibilidad) no cambian
   nada» ([CON] §4, T9).
2. **Contrato.** [EXI] §15.5. [CON] §4, precisión de T9: P5 da `DEPENDENCY_UNDEFINED` con causa
   `CERTIFIED_NONEXISTENCE`. Se aplica a los certificados explícitos que publica T11 (G1, G2; [EXI]
   §15.7).
3. **Aserción.** `assertEquals(OUT_OF_DOMAIN, …)` → `assertEquals(DEPENDENCY_UNDEFINED, …)`. En las dos
   últimas pruebas se añade además `getDiagnostic().startsWith(CERTIFIED_NONEXISTENCE)`.
4. **Sigue verificándose.** El hueco nunca es válido ni se puentea, y la estructura de ramas se conserva.
5. **Cobertura.** `OUT_OF_DOMAIN` deja de observarse en los huecos declarados.
6. **Prueba sustitutiva.** `G9U0R5SemanticTransformEdgeCasesTest.unboundedCollapsedImageHasRichExactZeroWithoutEndpointEvaluation`
   y `LocusV2RenderSeparationTest.unboundedImageIsEvaluatedSemanticallyAndClippedOnlyForRender` siguen
   fijando `OUT_OF_DOMAIN` fuera del dominio declarado.

### G3b — Desbordamiento dentro de una componente probada: `NON_FINITE` → `EVALUATION_FAILED`

Afecta a `G9U0R5SemanticTransformEdgeCasesTest.finiteRotationOverflowAndUndefinedInputsRecoverWithoutStalePoint`,
`G9U0R5SimilarityTransformationsTest.e04InvalidAddressesOverflowAndNonfiniteInputsFailWithoutStaleGeometry`
y `G9U0R6SemanticLocusPointInteractionTest.r627DeepSimilarityCompositionCountsNestedMissesAndStopsAtBudget`.

1. **Comportamiento anterior.** Una imagen por semejanza que desborda daba `NON_FINITE`.
2. **Contrato.** [EXI] §15.5: un fallo computacional dentro de una existencia certificada es
   `EVALUATION_FAILED`. [DIS] §3.4 (P5). [CON] §4, precisión de T11: «una evaluación no finita dentro de
   una componente probada es `EVALUATION_FAILED` con causa `COMPUTATIONAL_FAILURE`».
3. **Aserción.** `assertEquals(NON_FINITE, …)` → `assertEquals(EVALUATION_FAILED, …)` y
   `assertNull(…getPoint())`. En r627 el diagnóstico empieza por `COMPUTATIONAL_FAILURE` y contiene
   `evaluator NON_FINITE`.
4. **Sigue verificándose.** No quedan coordenadas obsoletas y la recuperación es correcta.
5. **Cobertura.** El estado `NON_FINITE` publicado deja de observarse aquí. r627 lo conserva en el
   diagnóstico.
6. **Prueba sustitutiva.** `LocusV2KernelIntegrationTest.typedInvalidEvaluationsNeverCarryStaleCoordinates`
   (C54).

### G4 — Parábola escalar `(s,s^2)` → parábola certificada por foco y directriz

La sustituye el auxiliar nuevo `G9U0PublicSurfaceTestBase.certifiedParabola()`, de clase v1.

Afecta a `G9U0R2LocusPresentationTest`, en diez métodos: `ordinaryColorsRepaintWithoutSemanticMutation`,
`minDefaultAndLargeThicknessAreScaleIndependentPresentation`,
`everyOrdinaryLineTypePersistsWithoutChangingSubpaths`,
`propertiesExposeLineControlsForSingleAndCompatibleMultiselection`,
`completePresentationPersistsAcrossTwoNativeXmlReopens`,
`clipboardCopyPreservesStyleAndRemapsOwnedIdsInBothScopes`,
`everyHostUndoablePresentationPropertyRestoresWithoutNewIdentity`,
`everyPresentationMutationPreservesTheCompleteSemanticWitness`,
`visibilityPersistsAndParticipatesInOrdinaryUndoRedo` y
`everySupportedLabelModeDrawsAndPersistsWithStyleAndCopy`.

Afecta además a `G9U0R2LocusRenderContinuityTest.exactCanonicalRenderTopologySurvivesAllCrossingFixtures`,
`G9U0IntersectionTokenTest` (`i07TangentAndNearTangentRootsAreTopologicallyDistinct`,
`i15MergeAndSplitBurnAmbiguousContinuationTokens`) y `G9U0R4IntersectionAdmissibilityContinuationTest`
(`localAdmissibilityRemainsIndependentOfGlobalCompleteness`, en su parte tangente, donde `LT` pasa a `L`;
`disappearanceNewAppearanceAndTangencyRemainFailClosed`; `mergeCandidateParentKeysRemainExplicitAndNonAdmissible`).

1. **Comportamiento anterior.** `LocusV2(Q,s,D)` con `Q=(s,s^2)` y `D={false,{-2,2,true,true}}`, que
   publicaba `COMPLETE` de compatibilidad.
2. **Contrato.** Fuera de clase v3.
3. **Aserción.** `createParabola()` (o `QT=(t,t^2)`) → `certifiedParabola()`. La curva es la misma,
   `y = x^2` con `-2 ≤ x ≤ 2`, pero el parámetro pasa a ser `u = (x+2)/4` en `[0,1]`. Las aserciones no
   cambian.
4. **Sigue verificándose.** La propiedad de cada prueba sobre una curva certificada idéntica.
5. **Cobertura.** El generador escalar deja de ejercitarse en estas pruebas. La construcción tiene ahora
   objetos auxiliares (foco, directriz, conductor y rectas), que entran en las copias, el XML y el
   deshacer de R2.
6. **Prueba sustitutiva.** Los generadores escalares certificados siguen cubiertos por `createLine()` y
   `createDisconnectedLine()`, ambos `(s,0)`.

### G5 — Curva escalar fuera de clase → diagonal afín directa `(s,s)` o segmento

Afecta a `G9X1LocusV2ExportTest`, en `l01`, `l03`, `l07`, `l08`, `l10`, `l11` y `l12`; a
`PreG9BR6PlusCSharedExportTest.theSemanticAdapterKeepsComponentsGapsOrientationAndRevision` (locus `L`); a
`PreG9BCu2ExportDeclarationTest` (`anApproachedOpenEndIsDeclaredInTheSidecarAndTheComments`,
`theInspectorShowsTheDrawingFidelity`); y a
`PreG9BR6PlusCLatexExportTest.locusV2IsWrittenAsOnePathPerCertifiedComponentInEveryDialect`, donde `L`
era `(s,s^2/4)`.

También a `G9U0R5SimilarityTransformationsTest.m01CollapsedOpenDomainHasTruthfulRichZeroLength`,
`G9U0R2LocusRenderContinuityTest.genuineDiscontinuityOpenUnboundedClippingDashAndOverdrawStayDistinct`
(antes `(t,t^3)`) y `PreG9BR6PlusA2HiddenLayerPersistenceTest.geometryIsInvariantUnderEveryLayerRoute`.

Y a `PreG9BR6PlusBPictureFidelityTest.buildDisjointScene`, donde `(sv,10.8+0.1*sin(4*sv))` pasa a
`(sv,10.8)`. Ese auxiliar lo usan `theViewportLeavesTheLiveViewAndTheConstructionUntouched`,
`theViewportHasItsOwnIdentityAndController`, `theSvgViewBoxIsTheExactAreaWithTheSameAspectAndNoAnisotropy`,
`everyDrawableKindIsCompleteInADisjointArea` y `picturesAreDeterministic`.

1. **Comportamiento anterior.** Curvas escalares curvas con certificado de compatibilidad.
2. **Contrato.** Fuera de clase v3.
3. **Aserción.** `"(s,s^2)"` → `"(s,s)"` y análogos. Las aserciones no cambian: componentes, huecos,
   orientación, extremo abierto, revisión, determinismo e invariancia.
4. **Sigue verificándose.**
   - Una entidad o un trayecto por componente certificada, sin puentear huecos.
   - Orientación decreciente (l03) y `OPEN_END_APPROACHED` declarado.
   - Regla estricta de DXF, invariancia por capas e imágenes deterministas.
5. **Cobertura.** **ATENCIÓN** (DXF, LaTeX, orientación, exportación de imágenes):
   - La aproximación ya no se ejercita sobre curvatura en estas pruebas: una recta se aproxima con la
     profundidad mínima.
   - En l03 la orientación se comprueba sobre un trazo recto.
   - En PDF, SVG y EMF ya no hay un trazo Locus V2 curvo.
6. **Prueba sustitutiva.**
   - Curvas certificadas curvas en exportación: `PreG9BCu2AuthorModelExportTest` (nuevo),
     `G9X1LocusV2ExportTest.l04` y `l05`, y C11.
   - Extremo abierto aproximado sobre una curva: ninguna; limitación.
     `PreG9BCu2RepresentationTest.anOpenEndIsExportedAndDeclared` también usa una recta.
   - Imágenes con un Locus V2 curvo: ninguna identificada.

### G6 — Circunferencia escalar periódica → circunferencia trazada por un punto de circunferencia (costura en −π)

La sustituyen dos auxiliares: `G9U0PublicSurfaceTestBase.createPeriodicCircle()` y
`G9U0R2LocusRenderContinuityTest.createMinimizedPeriodicLocus()`.

Sustituciones directas:
- `G9U0MetricPositionTest.m11DevelopedAndDirectLineMetricAgree`.
- `PreG9BR6PlusCSharedExportTest`: `theSemanticAdapter…` (locus `C`) y `dxfKeepsTheStrictWritabilityRule`
  (antes una parábola de dos componentes).
- `PreG9BR6PlusCLatexExportTest.locusV2IsWritten…` (locus `C`, de centro `(3,1)`).
- `G9U1SemanticPointInteractionTest.closedCurveInteriorIsNotAStrokeHit`, con `k` y `K` ocultos.
- `G9U0R2LocusRenderContinuityTest.exactCanonicalRenderTopologySurvivesAllCrossingFixtures`.

Usuarios de `createPeriodicCircle()` cuyo cuerpo no cambia:
- `G9U0GeneratorSuiteTest`: `g14PeriodicSeamTraversalCarriesLiftEvidence` y
  `g22GeneratorHasZeroRenderSampleOrViewportAuthority`.
- `G9U0R5SemanticTransformEdgeCasesTest.periodicNegativeDilationPreservesFundamentalDomainAndSeam`.
- `G9U0R6…`: `r639ExtremePeriodicLiftFailsClosedWithoutStaleCoordinates` y
  `r640AddressStateCodecRejectsAliasesAndMalformedUtf8`.
- `PostG9U1A6OptionalBranchPointTest.periodicSourceRetainsCanonicalAddressAndLift`.

1. **Comportamiento anterior.** `Q=(cos(u),sin(u))`, `D={true,{0,2*pi,true,false}}`, con dominio
   `[0,2π)`, costura en `(1,0)` y cierre por compatibilidad.
2. **Contrato.** Fuera de clase v3: [CON] §4 T11 cita el «locus escalar periódico». [CCM] §7, nota v1.2,
   y §10.8: identificación periódica de la circunferencia conductora en la clase v3. [EXI] §15.3: el
   cierre solo lo decide el certificado.
3. **Aserción.** `Q=(cos(u),sin(u)); D=…; L=LocusV2(Q,u,D)` →
   `c=Circle((0,0),1); P=Point(c); Q=P+(0,0); L=LocusV2(Q,P)`, de clase v2 y dominio `[−π,π)`.
4. **Sigue verificándose.** Componente periódica cerrada certificada, cierre en el dibujo y en la
   exportación, elevaciones y semántica de interacción.
5. **Cobertura.** **ATENCIÓN** (costura): la costura pasa de `(1,0)` a `(−1,0)`. En los usuarios cuyo
   cuerpo no cambia, estos valores dejan de estar en la costura: los parámetros `0` y `2π` (g14 y
   `periodicNegativeDilation…`), la semilla `(1,0)` (r639 y r640) y `2π+π/2` (`periodicSourceRetains…`).
   **Recuperada** en g14 y en `periodicNegativeDilationPreservesFundamentalDomainAndSeam` con aserciones
   nuevas en `−π` y `π`: el mismo punto con elevaciones distintas en g14 y la misma imagen `(5,−3)` a los
   dos lados de la costura en la dilatación. En r639, r640 y `periodicSourceRetains…` la costura no es la
   propiedad que se prueba (elevación extrema, códec del estado de dirección y elevación de la fuente).
6. **Prueba sustitutiva.** La costura en −π la cubren además C36, C38, C39, C41, C45, C47, C48, C49 y
   C55 y `G9X1LocusV2ExportTest.l05` (C15).

## 2. Conformidades por archivo

### Escritorio (`source/desktop/desktop/src/test/java/org/geocedg/desktop/`)

#### G9U0R3InspectorWorkflowTest.java

**C69 · `i06ExactTokenPointSurvivesEstablishedRecompute`** — **ATENCIÓN** (libro mayor, token exacto)
1. **Comportamiento anterior.** `(s,b)` escalar en `[-2,2]`. Al poner `b=1`, el punto del token se
   movía a `(0,1)` con el mismo token.
2. **Contrato.** Fuera de clase v3: `(s,b)` con `b` libre no es afín directo. [DIS] §4.1, precisión de
   T3 «Constantes»: `(0,b)` es constante respecto del conductor.
3. **Aserción.** `Q=(s,b); D=…; L=LocusV2(Q,s,D)` →
   `G=Segment((-2,0),(2,0)); P=Point(G); Q=P+(0,b); L=LocusV2(Q,P)`. Las aserciones no cambian.
4. **Sigue verificándose.** El punto con token exacto sobrevive al recálculo establecido.
5. **Cobertura.** El generador escalar deja de ejercitarse y el dominio pasa a ser `[0,1]`.
6. **Prueba sustitutiva.** No procede.

**C70 · `i10LongExactTokensHaveBoundedDistinctAccessibleChoices`, `i11CompactChoiceUsesAndPersistsTheCompleteExactToken`**
(auxiliar `createDoubleIntersection`) — **ATENCIÓN** (tokens persistidos en componentes parciales; limitación registrada en §7.4)
1. **Comportamiento anterior.** `(s^2,s)` en `{-2,0}` y `{0,2}` semiabiertos, cortado por `x=1` en
   `s = ±1`.
2. **Contrato.** Fuera de clase v3.
3. **Aserción.** `Q=(s^2,s)` y `u:x=1` → `Q=(s,s)` y `u:x^2+y^2=2`.
4. **Sigue verificándose.** Una raíz por componente en `s = ±1`, elecciones accesibles acotadas y
   persistencia del token completo (los dos tokens tienen 1625 caracteres).
5. **Cobertura.** El objetivo pasa de recta a cónica, que es otra capacidad. Los tokens de componentes
   parciales citan ahora la clave de linaje estructural ([DIS] §7, precisión de T11), de modo que su
   texto difiere del anterior a CU2. Según [DIS] §7, un documento anterior a CU2 con tokens en
   componentes parciales queda latente con fallo cerrado; ninguna prueba modificada lo ejercita
   (limitación declarada).
6. **Prueba sustitutiva.** Ninguna para documentos anteriores a CU2.

**Defecto de producción destapado por C70.** Con el montaje nuevo, `L` quedaba indefinido: al
restringir el certificado a las componentes declaradas `[-2,0)` y `(0,2]`, `restrictedTo` añadía el
hueco declarado `[0,0]` después de `(0,2]`, y el orden canónico de la cobertura, que solo comparaba el
extremo inferior, lo dejaba detrás, de modo que la validación de intervalos disjuntos lanzaba
`IllegalArgumentException`. Afectaba a todo dominio escalar declarado con dos tramos que comparten un
extremo excluido, también con `(s,0)`. Se corrige en `LocusExistenceStructure2D.immutableCoverage`: a
igual extremo inferior, el cerrado precede al abierto. No cambia el orden de ninguna cobertura que antes
fuera válida. Prueba de regresión:
`PreG9BCu2ExistenceStructureV2Test.declaredComponentsSharingAnExcludedEndAreRestrictedInOrder`.

#### G9U0R4NativeArchivePersistenceTest.java

**C01 · `nativeCedgPreservesDormantAndReactivatedExistingPoints`** — **ATENCIÓN** (libro mayor,
documento nativo reabierto)
1. **Comportamiento anterior.** Generador escalar `(p,(p^2-1)(p^2-4))` en `[-bound,bound]`. Al pasar
   `bound` de 1,5 a 3, las raíces ±2 entraban por el extremo móvil; los puntos quedaban latentes y se
   reactivaban tras reabrir y volver a 1,5.
2. **Contrato.** Fuera de clase v3. SplineV2 con certificado de proveedor (`spline-v2-interpolation/v1`,
   [DIS] §9 T11; D-CU2-7).
3. **Aserción.** `ReactivationGenerator=…; LocusV2(…,{-b,b})` →
   `SplineV2(If(reactivationBound>2, nodos hasta ±23/8, nodos hasta ±11/8),3)`.
4. **Sigue verificándose.** Recurrencia 2→4→2 raíces, puntos latentes con su token e identidad,
   reapertura nativa sin objetos nuevos y reactivación.
5. **Cobertura.** Las raíces que aparecen por un extremo móvil del dominio. Ahora aparecen al cambiar la
   lista de nodos en `bound = 2`, y la curva interpola el polinomio sin ser el polinomio.
6. **Prueba sustitutiva.** Ninguna; limitación.

#### G9U0R5NativeArchivePersistenceTest.java

**C02 · `nativeCedgReopensEverySimilarityFamilyWithStableIdentityAndDependencies`** (auxiliar
`createParabola(App)`) — **ATENCIÓN** (identidad en el archivo nativo)
1. **Comportamiento anterior.** Parábola escalar. Tras reabrir y poner `k=3`, `Ld` en 0,5 valía
   `(−4,5; 2,75)`.
2. **Contrato.** Fuera de clase v3.
3. **Aserción.** `assertPoint(reopenedDilation, 0.5, -4.5, 2.75)` → `assertPoint(…, 0.5, -6, 2)`: el
   parámetro 0,5 es ahora el vértice `(0,0)`. El fixture es el de foco y directriz.
4. **Sigue verificándose.** Las ocho familias de semejanza se guardan y reabren con identidades y
   dependencias estables, y hay revisión nueva al cambiar el factor.
5. **Cobertura.** Las transformaciones de un generador escalar en el archivo nativo.
6. **Prueba sustitutiva.** `G9U0R5SimilarityTransformationsTest` transforma loci escalares certificados
   (`createLine`, `createDisconnectedLine`).

#### G9U1PeriodicQuarantineNativeTest.java

**C03 · `actualPeriodicQuarantineRoundTripsAndReactivatesOriginalPoints`,
`provedNonzeroOffsetRetiresOnlyItsGroupAcrossTheNativeArchive`,
`featureOffAndClassicPreserveNativeQuarantineWithoutCreation`** (auxiliares `COMMANDS`,
`quarantinedSeed` y `move`) — **ATENCIÓN** (cuarentena del libro mayor, tokens persistidos)
1. **Comportamiento anterior.** Doble recubrimiento escalar `(cos(2u+a), sin(2u+a))` en `[0,2π)` con
   cuatro raíces en `y=0`; `move` fijaba la fase `a`.
2. **Contrato.** Fuera de clase v3: escalar periódico.
3. **Aserción.** Las órdenes pasan a
   `a=0.1, c=Circle((0,0),1), P=Point(c), Pa=Rotate(P,a), r=Line((0,0),Pa), F=Intersect(r,PerpendicularLine((1,0),r)), L=LocusV2(F,P)`,
   de clase v1. `parameter.setValue(value)` → `parameter.setValue(phase / 2)`.
4. **Sigue verificándose.** Cuatro tokens elegibles, cuarentena de los cuatro al girar π, persistencia
   nativa con doble reapertura y reactivación con los mismos tokens e identidades.
5. **Cobertura.** La cuarentena de un generador periódico explícito. Las raíces están ahora en `(0,0)` y
   `(1,0)`.
6. **Prueba sustitutiva.** No procede: los escalares periódicos ya no tienen raíces.

#### G9U1SemanticPointInteractionTest.java

Solo G6 (`closedCurveInteriorIsNotAStrokeHit`).

#### PreG9BCu2AuthorModelCertificationTest.java

**C04 · `pointwiseValidityBeyondTheExactEndIsNotExistence`**
1. **Comportamiento anterior.** `locus.evaluate(…)` (evaluador vivo) daba puntos válidos más allá del
   extremo exacto (F-CU2-1).
2. **Contrato.** [EXI] §15.5. [CON] §4 T11: la validez puntual se lee en el evaluador del corte. [MAT]
   V-TR-4.
3. **Aserción.** `locus.evaluate(MAIN, parameter, session).isValid()` →
   `slice.evaluate(null, null, parameter, session).isValid()`. Se añade
   `assertEquals(DEPENDENCY_UNDEFINED, row.get("published"))`.
4. **Sigue verificándose.** La validez puntual no es frontera y el certificado da `CERTIFIED_DOES_NOT_EXIST`
   por discriminante.
5. **Cobertura.** Ninguna identificada.
6. **Prueba sustitutiva.** No procede.

**C73 · `theAuthorParametersAreCertifiedFromStructuralFactsOnly`, puerta GC-6** (añadida tras la
primera redacción)
1. **Comportamiento anterior.** Hasta T10 el certificado en sombra se calculaba al pedirlo, así que la
   diferencia de lecturas métricas alrededor de `certificate(app, "e_2")` medía su cálculo.
2. **Contrato.** [DIS] §9, precisión de T11: el certificado se calcula una vez por revisión, al
   publicarla. [MAT] GC-6.
3. **Aserción.** `assertEquals(0L, …"e_2.reads")` con la diferencia alrededor de la lectura →
   `assertEquals("0/0/1", …)`: lecturas acumuladas desde la creación de `e_2` (que incluyen la
   publicación al abrir el documento), lecturas al leer el certificado y cálculos de certificado de
   la revisión.
4. **Sigue verificándose.** GC-6 sin vacuidad: el único cálculo del certificado de `e_2` no lee ningún
   valor métrico. Una sonda de T11 lo comprueba también tras los cambios dinámicos 2,9 → 3,7 → 4,0 →
   7,0 → 2,9, con asentamiento de la primera familia, sin ninguna lectura en ningún locus del modelo.
5. **Cobertura.** Ninguna: con la diferencia alrededor de la lectura, la aserción pasaba en vacío desde
   T11.
6. **Prueba sustitutiva.** No procede.

Métodos nuevos, que no son conformidades: V-PUB-1 a V-PUB-4 (`theLociArePublishedWhileTheSavedDriverIsInTheGap`,
que invierte `pins.H01`; `everyRevisionPublishesItsOwnGeometry`; `anEmptyCertifiedCoverIsPublishedComplete`,
que se amplió durante la redacción con el asentamiento y la reapertura; y
`anUndefinedDefinitionInputLeavesNoStaleDefinition`).

#### PreG9BCu2AuthorModelClassV3Test.java

**C05 · `theRuleIsCapturedWithoutAnyValidSample`** — **ATENCIÓN** (reapertura)
1. **Comportamiento anterior.** Un documento guardado con el conductor en el hueco (excCyl 4,0) no
   construía corte al reabrirse (fijación `H01` de CU0).
2. **Contrato.** [SEM] v1.3 «Publication». [DIS] §6 y su precisión de T11 «Lectura de una construcción
   completa». [MAT] V-SEL-2 y V-PUB-1.
3. **Aserción.** `assertEquals(false, row.get("evaluator_in_gap"))` → `assertEquals(true, …)`.
4. **Sigue verificándose.** Programa idéntico sin muestra válida, una revisión al asentarse y la regla
   `0,0,1,1,1` tras reabrir.
5. **Cobertura.** Ninguna.
6. **Prueba sustitutiva.** No procede.

#### PreG9BCu2AuthorModelConsumerTest.java

**C06 · `theExcludedEndOfE2IsItsOneSidedLimit`, `anEvaluatorPointInACertifiedGapIsNotPublished`**
(auxiliar `injected` → `published`)
1. **Comportamiento anterior.** La definición se fabricaba inyectando el certificado en sombra con el
   evaluador vivo (`t9-injected|…`).
2. **Contrato.** [DIS] §9 T11 «Productores dependientes». [EXI] §15.7.
3. **Aserción.** `injected(e2, certificate(e2).getStructure())` → `published(e2)`, que exige
   `getSemanticSignature()` igual entre el certificado y la definición publicada.
4. **Sigue verificándose.** V-CON-10, el extremo excluido de `e_2` como límite lateral y V-AVAIL-1.
5. **Cobertura.** Ninguna.
6. **Prueba sustitutiva.** No procede.

**C07 · `anEvaluatorPointInACertifiedGapIsNotPublished`**
1. **Comportamiento anterior.** El valor «live» se leía de `o.evaluate(MAIN, inside, …)`.
2. **Contrato.** [EXI] §15.5 y [CON] §4 T11.
3. **Aserción.** → `certified.getEvaluatorCapability().evaluate(null, null, inside, session)`.
4. **Sigue verificándose.** El evaluador del corte da un punto en el hueco certificado y la definición lo
   rechaza.
5. **Cobertura.** Ninguna.
6. **Prueba sustitutiva.** No procede.

#### PreG9BCu2AuthorModelLineageTest.java

**C08 · `containmentCreatesNoCorrespondence`** — **ATENCIÓN** (identidad persistida, reapertura)
1. **Comportamiento anterior.** En la misma sesión, el primer certificado en mordedura era `APPEARED` y
   el selector de dominio completo daba `NO_CORRESPONDENCE`.
2. **Contrato.** [EXI] §15.6. [DIS] §7: E-3 y precisión de T8, «El primer certificado tras abrir un
   documento no tiene padres». [MAT] V-LIN-6.
3. **Aserción.** Se guarda y se reabre el documento editado:
   `PreG9BCu0Corpus.open(PreG9BR6PlusF3Fixtures.save(edited, work, "bite"))`. La aserción
   `"APPEARED|true|NO_CORRESPONDENCE"` no cambia.
4. **Sigue verificándose.** La contención no crea correspondencia.
5. **Cobertura.** La variante en sesión, que con certificados publicados registra ahora `SPLIT`.
6. **Prueba sustitutiva.** `PreG9BCu2AuthorModelLineageTest.movingEndsKeepTheLineageAndTheBiteIsATypedTransition`
   y `PreG9BCu2LineageTest.selectorsResolveOnlyThroughDemonstratedCorrespondence`.

#### PreG9BCu2AuthorModelRepresentationTest.java

Sin conformidades: solo cambia el javadoc de la clase y de `publishAndRender`. Métodos nuevos: V-B07-1
`aBiteIsDrawnToTheImagesOfItsCertifiedEnds` (invierte `pins.B07`), V-B07-3
`noStrokeBridgesTheRealGapNearTheCriticalValue` y el auxiliar `componentEnd`.

#### PreG9BCu2ExportDeclarationTest.java

Solo G5.

#### PreG9BR6PlusA2HiddenLayerPersistenceTest.java

Solo G5.

#### PreG9BR6PlusBPictureFidelityTest.java

Solo G5 (`buildDisjointScene`).

#### PreG9BR6PlusCBaseIdentityTest.java

**C09 · `outputsThatCMustNotChangeAreTheBaseBytes`** (`BASE`, `digests`, `assertDeclaredNonexistence`) —
**ATENCIÓN** (DXF, identidad byte a byte con la base `e6135028`; limitación registrada en §7.4)
1. **Comportamiento anterior.** Seis SHA-256 de loci escalares coincidían con la base:
   `g9x1.locus.components.{dxf,dxf.core,sidecar}`, de `(s,s^2/4)` con dos componentes, y
   `g9x1.locus.closed.{dxf,dxf.core,sidecar}`, de `(cos t, sin t)` (sidecar `aeb007cf` desde T10).
2. **Contrato.** Fuera de clase v3. [CON] §6, precisión de T11: el locus `NOT_ESTABLISHED` se declara
   `EXISTENCE_NOT_ESTABLISHED` y una solicitud sin salida parcial lo rechaza. [MAT] V-DXF-2.
3. **Aserción.** Se eliminan seis entradas de `BASE`. `putG9x1(…, "g9x1.locus.components"/"closed", …)`
   → `assertDeclaredNonexistence`, que comprueba `!isWritable()`, que no hay entidades y la razón
   `EXISTENCE_NOT_ESTABLISHED`.
4. **Sigue verificándose.** Identidad byte a byte de SplineV2, de la función, de las exportaciones
   exactas de G5 y G9X1, de LaTeX y de Classic. Los loci se quedan en la escena para no cambiar los
   colores automáticos.
5. **Cobertura.** Se pierde la identidad byte a byte con la base publicada de las exportaciones DXF de
   loci del generador escalar (`LocusV2(Q,s,D)`), que ya no tienen componentes certificadas. La de la
   SplineV2, que también es un Locus V2 (`g9x1.spline.{dxf,dxf.core,sidecar}`), se conserva.
6. **Prueba sustitutiva.** Para el generador escalar, ninguna con identidad de bytes; limitación. La
   topología la cubren `G9X1LocusV2ExportTest` y
   `PreG9BCu2AuthorModelExportTest.eachCertifiedComponentIsOnePolylineClosedOnlyWhenPeriodic`.

#### PreG9BR6PlusCLatexExportTest.java

Contiene G5 y G6 (`locusV2IsWritten…`); además, el javadoc de la clase cita la especificación durable
(D-CU2-11), sin efecto en las aserciones.

**C10 · `anIncompleteLocusIsEmittedAndReportedIncomplete`** — **ATENCIÓN** (LaTeX, documento anterior
`TestExport1.cedg`)
1. **Comportamiento anterior.** `m` emitía dos componentes localmente certificadas.
2. **Contrato.**
   - [APR] D-CU2-9: certificador especializado de SplineV2 con currentness, independencia del
     conductor y última celda.
   - [DIS] §9, precisión de T11 «Raíz seleccionada de SplineV2»: une las componentes a través de la
     costura y cita el locus `m`.
   - [EXI] §15.3.
   - La unión por la costura no está en el texto literal de D-CU2-9, pero sí en textos aprobados:
     [CON] §4, primer punto de la base («Un arco que cruza la costura se devuelve como **una**
     componente con su forma de arco, nunca como dos intervalos»), y el modelo de arcos de [EXI]
     §15.3. D-CU2-9 ordena corregir el tratamiento de la última celda, que en un dominio periódico es
     la que enlaza con la primera.
3. **Aserción.** `assertEquals(2, locus.getEmittedCount(), …)` → `assertEquals(1, …)`.
4. **Sigue verificándose.** `INCOMPLETE_WITH_CERTIFIED_COMPONENTS`, la cobertura
   `LOCALLY_CERTIFIED_GLOBAL_NOT_ESTABLISHED` y la construcción intacta.
5. **Cobertura.** Ninguna identificada.
6. **Prueba sustitutiva.** No procede.

**C11 · `failedComponentsAreDisclosedAndNeverBridged`** — **ATENCIÓN** (LaTeX)
1. **Comportamiento anterior.** Parábola escalar con dos componentes fallidas. «Nunca puenteado»
   significaba que no aparecía `(-1.,1.)-- (1.,1.)`. El segundo caso era una sola componente de
   parábola.
2. **Contrato.** Fuera de clase v3. [CON] §6.
3. **Aserción.** El fixture pasa a
   `P=Point(Circle((0,0),1)); h=Line(P,P+(1,0)); Q=Intersect(Circle((0,0),1/2),h,1)`: dos arcos de
   clase v3.
   `assertFalse(code.contains("\\draw [") && code.contains("(-1.,1.)-- (1.,1.)"))` →
   `assertEquals(0, report.getEmittedCount())`. El segundo caso es `Q=P+(0,0)` con `c` y `P` ocultos.
4. **Sigue verificándose.** `NO_ADMISSIBLE_OUTPUT`, `[WORK_LIMIT]`, ninguna unión, y texto vacío con
   `REJECTED_NO_ADMISSIBLE_OUTPUT`.
5. **Cobertura.** El caso aislado pasa de una componente abierta a una curva cerrada.
6. **Prueba sustitutiva.** No procede.

#### PreG9BS1AuthorDxfReproductionTest.java

**C12 · `semanticCurvesExposeApprovedExtendedDxfDisposition` (certificado de `m`)** — **ATENCIÓN**
(documento anterior, identidad de raíces)
1. **Comportamiento anterior.** Dos componentes `selected-root-interval/v1` con el mismo prefijo de
   evidencia, ordenadas y separadas por celdas no resueltas («adjacent unresolved cells are not
   crossed»).
2. **Contrato.** D-CU2-9 y [DIS] §9 T11 (como en C10). [EXI] §15.3. [DIS] §7, precisión de T11: los
   tokens de raíz persistidos se reanudan con `identityKey`.
3. **Aserción.** El número de componentes pasa de 2 a 1. Desaparecen la aserción del prefijo común y la
   de orden `get(0).getUpper() < get(1).getLower()`, y entra `assertTrue(certificates.get(0).crossesSeam())`.
   `"adjacent unresolved cells are not crossed"` → `"UNRESOLVED_BAND_EDGE"`.
4. **Sigue verificándose.** `NOT_ESTABLISHED`, la banda `UNRESOLVED` en −π/2 sin cruzar, los extremos
   cerrados y los estados de evaluación.
5. **Cobertura.** Que las dos piezas compartían la misma raíz seleccionada.
6. **Prueba sustitutiva.** No procede.

**C13 · `semanticCurvesExposeApprovedExtendedDxfDisposition` (recuentos DXF)** — **ATENCIÓN** (DXF)
1. **Comportamiento anterior.** `m` daba 2 aproximadas, 2 entidades, 2 coberturas incompletas y 2
   `LWPOLYLINE`. La selección combinada daba 4, y la construcción completa 4 aproximadas.
2. **Contrato.** [CON] §3, fila DXF: un `LWPOLYLINE` por componente, en el orden del arco aunque cruce
   la costura. [EXI] §15.8 y D-CU2-9.
3. **Aserción.** `assertEquals(2, …)` → `1` en esos cuatro recuentos; la combinada pasa de 4 a 3 y
   `complete.getApproximateCount()` de 4 a 3.
4. **Sigue verificándose.** Escritura con anexo obligatorio y 26 entidades exactas: los puntos de
   intersección del documento siguen materializados.
5. **Cobertura.** Ninguna.
6. **Prueba sustitutiva.** No procede.

#### locus/G9U0ToolSurfaceTest.java

**C71 · `t01aPointToolRequiresExplicitPreimageDialog`**
1. **Comportamiento anterior.** Ocho escalar `(sin(s),sin(2s))` en `[0,π]`, con puntos en 0 y π de
   coordenadas iguales.
2. **Contrato.** Fuera de clase v3.
3. **Aserción.** El fixture pasa al pie de la perpendicular desde `(1,0)`, un doble recubrimiento de
   clase v1. `Point(L,…,0)` y `pi` → `0.5` y `0.5-pi`.
4. **Sigue verificándose.** La herramienta exige el diálogo explícito de preimagen.
5. **Cobertura.** El cruce transversal pasa a ser un doble recubrimiento.
6. **Prueba sustitutiva.** Ninguna identificada.

### Compartidas (`source/shared/common-jre/src/test/java/`)

#### export/G9X1LocusV2ExportTest.java

Contiene G5 (l01, l03, l07, l08, l10, l11 y l12).

**C14 · `l04CoincidentConstructiveComponentsRemainDistinct`** — **ATENCIÓN** (DXF)
1. **Comportamiento anterior.** `(s^2,0)` en `[-2,-1]∪[1,2]`, con extremos exactos `x` = 4/1 y 1/4 y
   tolerancia 0.
2. **Contrato.** Fuera de clase v3.
3. **Aserción.** El fixture pasa a `Q=Intersect(PerpendicularLine(P,eje), Segment((-1/2,0),(1/2,0)))`
   con `P` sobre la circunferencia unidad. `assertEquals(4, first(first).getX(), 0)` →
   `assertEquals(-0.5, …, 1E-9)`, y así los demás. Se añade que todo vértice tenga `y == 0`.
4. **Sigue verificándose.** Dos componentes con la misma imagen y sentidos opuestos, la misma fuente y
   claves distintas.
5. **Cobertura.** Los extremos son ahora transiciones certificadas: la tolerancia pasa de 0 a 1E-9.
6. **Prueba sustitutiva.** No procede.

**C15 · `l05TypedFullPeriodEvidenceClosesThePolyline`** — fixture G6.
1. **Comportamiento anterior.** Dominio `[0,2π)` y dominio explícito `full-period` `[0, 2π]`.
2. **Contrato.** G6.
3. **Aserción.** `new SemanticDomain(…, "full-period", 0, 2*Math.PI, …)` → `(…, -Math.PI, Math.PI, …)`.
4. **Sigue verificándose.** El cierre se toma del certificado de período completo.
5. **Cobertura.** Ninguna.
6. **Prueba sustitutiva.** No procede.

**C16 · `l06EndpointProximityDoesNotInferClosure`** — **ATENCIÓN** (DXF, cierre)
1. **Comportamiento anterior.** `(cos s, sin s)` no periódico en `[0,2π]`, con extremos a menos de
   1E-12, polilínea abierta y fin de parámetro 2π.
2. **Contrato.** Fuera de clase v3. [EXI] §15.3. SplineV2 cerrado solo por igualdad exacta de extremos
   ([DIS] §9 T11).
3. **Aserción.** El fixture pasa a `SplineV2({(1,0),(0,1),(-1,0),(0,-1),(1,1E-7)},3)`. `< 1E-12` →
   `< 1E-6`, y `getParameterEnd()` pasa de 2π a 1.
4. **Sigue verificándose.** La proximidad no infiere cierre.
5. **Cobertura.** La prueba adversaria se debilita de extremos a menos de 1E-12 a extremos a 1E-7, y no
   puede acercarse más con una SplineV2: su contrato (`SplinePolynomialModel2D`) rechaza unos extremos
   iguales según la precisión de puntos del núcleo pero no idénticos en bits («SplineV2 periodic
   closure requires exactly equal finite endpoints»), y la SplineV2 queda indefinida. Una sonda de T11
   lo comprueba: con 1E-8, 1E-9, 1E-10, 1E-12 y 1E-13 es indefinida y no exporta nada; con 1E-7 es
   abierta, `COMPLETE` y exporta una polilínea abierta.
6. **Prueba sustitutiva.** Ninguna con extremos coincidentes en redondeo; limitación.

**C17 · `l09LocusRevisionChangeInvalidatesPreflight`**
1. **Comportamiento anterior.** `(s,k)`. Mover `s` no cambiaba la revisión; `k=0.25` sí.
2. **Contrato.** Fuera de clase v3. [SEM] v1.3, huella exacta (GC-1).
3. **Aserción.** El fixture es la circunferencia de radio `r`. `driver.setValue(0.75)` →
   `driver.setCoords(0,1,1)`; `k=0.25` → `r=1.25`.
4. **Sigue verificándose.** El conductor no cambia la revisión y una entrada no conductora invalida el
   preflight.
5. **Cobertura.** Ninguna.
6. **Prueba sustitutiva.** No procede.

#### export/PreG9BR6PlusCSharedExportTest.java

Contiene G5 y G6, y javadoc nuevo que cita `latex-semantic-curve-export.md` §3, sin efecto.

**C18 · `semanticComponentsAreExcludedOnlyByACertifiedDisjointnessProof`**
1. **Comportamiento anterior.** La disyunción se probaba sobre la componente de compatibilidad de
   `(s,s^2)`.
2. **Contrato.** Fuera de clase v3.
3. **Aserción.** `…getContinuousValidComponents().get(0).getInterval()` → `getDeclaredDriverDomain()`.
   Se añade `assertTrue(…getContinuousValidComponents().isEmpty())`.
4. **Sigue verificándose.** Sin modelo certificado no hay exclusión: el resultado sigue siendo
   `NO_CERTIFIED_MODEL` o `PROVEN_DISJOINT`, como antes.
5. **Cobertura.** Ninguna.
6. **Prueba sustitutiva.** No procede.

#### kernel/locus/intersection/G9S1R1SplinePairIntervalCertificationTest.java

Solo G1.

#### kernel/locus/intersection/PiecewisePolynomialPairIntersectionCapability2DTest.java

Solo G1. El fixture periódico `a-periodic` (`x = t(1−t)`) es continuo en la costura.

#### kernel/locus/intersection/PreG9BCu2ClassV3ModelTest.java

Sin conformidades: solo cambian comentarios.

#### kernel/locus/intersection/PreG9BCu2HybridCertificationTest.java

**C19 · `outOfClassLociHaveOnlyExploratoryEvidence`**
1. **Comportamiento anterior.** La estructura publicada no era la híbrida:
   `assertNotEquals(HybridExistenceCertifier2D.CONTRACT, …)`.
2. **Contrato.** [EXI] §15.7 y [DIS] §9 T11. [MAT] V-LVL-1, V-V3-3 y V-PER-2.
3. **Aserción.** → `assertEquals(CONTRACT, published.getCertifierContract())`, la misma evidencia de
   cobertura y `assertSame(result.getExploration(), locus(label).getExploratoryEvidence(BRANCH))`.
4. **Sigue verificándose.** `NOT_ESTABLISHED`, sin componentes, sin cierre, `OPERATION_OUTSIDE_CLASS` y
   exploración no certificante.
5. **Cobertura.** Ninguna.
6. **Prueba sustitutiva.** No procede.

**C20 · `theShadowCertificateIsComputedOncePerRevision`**
1. **Comportamiento anterior.** El certificado en sombra se calculaba al pedirlo:
   `assertEquals(result.getExistenceBoxes(), boxes - before)`.
2. **Contrato.** [DIS] §8 y §9 T11 (una vez por revisión, al publicar). [MAT] GC-2 y GC-3.
3. **Aserción.** → `assertTrue(boxes >= result.getExistenceBoxes())`. Tras leer, los contadores
   `EXISTENCE_BOX` y `CERTIFICATE_COMPUTATION` no cambian, y `updateConstruction(false)` conserva el
   resultado.
4. **Sigue verificándose.** Un cálculo por revisión, compartido. Leer, recalcular o mover el conductor
   no recalcula; una revisión nueva sí.
5. **Cobertura.** La igualdad exacta pasa a ser una cota: desde T11 el certificado se calcula al
   publicar cada revisión, y el contador acumula el trabajo de todas las revisiones publicadas desde
   la creación del locus (el auxiliar `critical` recalcula la construcción tras crearla), no solo el
   del certificado leído. El nombre conserva «Shadow».
6. **Prueba sustitutiva.** El número exacto de cajas de cada certificado lo fijan los presupuestos de
   `PreG9BCu2AuthorModelCertificationTest.theAuthorParametersAreCertifiedFromStructuralFactsOnly`; que
   leer, recalcular o mover el conductor no repite trabajo lo fija esta misma prueba.

#### kernel/locus/intersection/PreG9BCu2LineageTest.java

Sin conformidades: solo cambia el javadoc.

#### kernel/locus/intersection/PreG9bR3X1CertifiedExpressionPointModelTest.java

**C21 · `nonfiniteOrUndefinedConstantVectorsAreRefused`**
1. **Comportamiento anterior.** Un vector indefinido o no finito dejaba el locus indefinido.
2. **Contrato.** [SEM] v1.3 «Publication». [DIS] §6. [CCM] §5.2: la captura rechaza el vector no
   finito.
3. **Aserción.** `assertFalse(locus.isDefined())` se mantiene para `NaN`. Para el no finito pasa a
   `assertTrue(locus.isDefined())` y `NOT_ESTABLISHED`.
4. **Sigue verificándose.** `E` indefinido y ninguna raíz de par admisible.
5. **Cobertura.** Ninguna.
6. **Prueba sustitutiva.** No procede.

#### locus/G7BMetricFixtures.java, G8AIntersectionFixtures.java, G8AIntersectionFunctionalBenchmarkTest.java, G8BIntersectionFixtures.java y G8CDesignFunctionalBenchmarkTest.java

Solo G1 o G2. Ver la **ATENCIÓN** de G1 sobre `periodicDefinition`.

#### locus/G8AIntersectionTopologyCharacterizationTest.java

Contiene G1 y G2.

**C22 · `isolatedAndEmptyComponentsHaveExplicitSetSemantics`** — **ATENCIÓN** (materialización
diferida a CU3; limitación registrada en §7.4)
1. **Comportamiento anterior.** La raíz en el parámetro aislado `[0,0]` se materializaba con
   `DomainLocation.ISOLATED_COMPONENT`.
2. **Contrato.**
   - [EXI] §15.4: «materializes no intersection»; un parámetro binary64 no la direcciona.
   - [APR] D-CU2-19.
   - [CON] §3.1 y §4, precisión de T11 «Componentes puntuales del proveedor».
3. **Aserción.** `assertEquals(ISOLATED_COMPONENT, …)` →
   `assertEquals(1, …getPointComponents().size())` y `assertTrue(hit.solutions().isEmpty())`.
4. **Sigue verificándose.** La componente vacía sigue dando `EMPTY` y `COMPLETE`.
5. **Cobertura.** `ISOLATED_COMPONENT`, que sigue en `LocusIntersectionSolver2D`, ya no se ejercita.
6. **Prueba sustitutiva.** Ninguna; limitación (D-CU2-19, CU3).

#### locus/G8BIntersectionTopologyAndScientificTest.java

**C23 · `isolatedEmptyAndCollapsedComponentsUseDistinctSetSemantics`** — **ATENCIÓN** (CU3; limitación
registrada en §7.4)
1. **Comportamiento anterior.** La raíz de la componente aislada se publicaba como
   `ISOLATED_COMPONENT`.
2. **Contrato.** [EXI] §15.4 y §15.8 (nunca `EMPTY` ni conjunto completo sin excluir la componente
   puntual). D-CU2-19.
3. **Aserción.** → sin soluciones, `GeometryKind.UNRESOLVED` y `Completeness.NOT_ESTABLISHED`.
4. **Sigue verificándose.** Semánticas distintas para la componente aislada, la vacía y la colapsada.
5. **Cobertura.** Como C22.
6. **Prueba sustitutiva.** Ninguna; limitación.

#### locus/G9S1R1SplinePairMaterializationTest.java

**C24 · `genericLocusPairStaysRichOnlyDespiteSharedInfrastructure`**
1. **Comportamiento anterior.** `(t^3/8,t)`, un locus sin modelo de intervalos.
2. **Contrato.** Fuera de clase v3.
3. **Aserción.** El fixture pasa a `TX=Intersect(Circle((0,0),3/2), Line((0,0),TD), 1)`. Las aserciones
   no cambian.
4. **Sigue verificándose.** El par se queda solo rico, con raíces finitas y ninguna elegible.
5. **Cobertura.** Ninguna. El comentario decía «certified (class v2)»; la intersección
   recta–circunferencia es de clase v3 ([CCM] §9, nota v1.2, y §10.1) y se ha corregido.
6. **Prueba sustitutiva.** No procede.

#### locus/G9U0GeneratorSuiteTest.java

Contiene G6 (g14 y g22, ver el punto 5 de G6).

**C25 · `g04…KeepTrueCoordinate` (no inyectivo)**
1. **Comportamiento anterior.** `Pn` y `Pp` estaban definidos, con la misma `x` y parámetros canónicos
   distintos.
2. **Contrato.** Fuera de clase v3: estado escalar distinto de la coordenada verdadera. [EXI] §15.8.
3. **Aserción.** `assertEquals(negative.getInhomX(), positive.getInhomX(), 0)` y `assertNotEquals(…)` →
   `assertNotEstablishedOutsideClass(nonInjective)` y ambos puntos indefinidos.
4. **Sigue verificándose.** La coordenada verdadera es la entrada 2 y la causa es
   `OPERATION_OUTSIDE_CLASS`.
5. **Cobertura.** Preimágenes distintas con la misma posición en este generador.
6. **Prueba sustitutiva.** C28, C34 y C71.

**C26 · `g04…` (periódico)**
1. **Comportamiento anterior.** Las elevaciones en 0 y 2π eran distintas.
2. **Contrato.** Como C25.
3. **Aserción.** `assertNotEquals(…getPeriodicLift(), …)` → `assertNotEstablishedOutsideClass(periodic)`.
   `Pp0` queda indefinido y `Pp1` se elimina.
4. **Sigue verificándose.** El proveedor sigue siendo periódico.
5. **Cobertura.** La elevación con coordenada verdadera.
6. **Prueba sustitutiva.** g14 (ya no en la costura; ver G6).

**C27 · `g04…` (discontinuo)**
1. **Comportamiento anterior.** Dos componentes válidas heredadas.
2. **Contrato.** Como C25, y [DIS] §9 T11: certificado restringido a lo declarado.
3. **Aserción.** `assertEquals(2, …getValidDomainComponents().size())` →
   `[UNRESOLVED, CERTIFIED_DOES_NOT_EXIST, UNRESOLVED]`.
4. **Sigue verificándose.** El hueco no existe y `Pd` queda indefinido.
5. **Cobertura.** Las componentes declaradas ya no se certifican.
6. **Prueba sustitutiva.** No procede.

**C28 · `g11SelfIntersectionKeepsDistinctSemanticPreimages`**
1. **Comportamiento anterior.** `(sin s, sin 2s)` con preimágenes 0 y π.
2. **Contrato.** Fuera de clase v3.
3. **Aserción.** → `projectedCircle()`, con `Math.PI/3` y `-pi/3`.
4. **Sigue verificándose.** Padres y direcciones distintos con las mismas coordenadas.
5. **Cobertura.** Una autointersección transversal: ahora es un pliegue por simetría.
6. **Prueba sustitutiva.** Ninguna identificada.

**C29 · `g13DynamicComponentLossWithdrawsAndExactRestoreRebinds`**
1. **Comportamiento anterior.** `L2`, sobre el punto retirado, quedaba indefinido.
2. **Contrato.** [SEM] v1.3 «Publication» y [DIS] §6.
3. **Aserción.** `assertFalse(L2.isDefined())` → `assertTrue(…)`.
4. **Sigue verificándose.** Retirada y revinculación exacta.
5. **Cobertura.** Ninguna.
6. **Prueba sustitutiva.** No procede.

**C30 · `g17IndirectCycleRedefinitionIsRejected`**
1. **Comportamiento anterior.** `P2`, sobre el locus anidado, estaba definido y figuraba entre los objetos
   que no deben quedar obsoletos.
2. **Contrato.** Fuera de clase v3: generador con soporte locus.
3. **Aserción.** `List.of(…, "L2", "P2", "Q2")` → sin `"P2"`. Se añade `assertFalse(nested.isDefined())`
   antes y después.
4. **Sigue verificándose.** El rechazo sin cambios de XML, pasos, identidades ni reservas.
5. **Cobertura.** Un punto definido sobre el anidado tras el rechazo.
6. **Prueba sustitutiva.** Ninguna identificada.

**C31 · `g21NestedQueryCountersAreDeterministic`**
1. **Comportamiento anterior.** `(s,k*s^2)`.
2. **Contrato.** Fuera de clase v3.
3. **Aserción.** El fixture pasa a `ck=Circle((0,0),k); Sk=Point(ck); Q=Sk+(0,0)`. Las aserciones no
   cambian.
4. **Sigue verificándose.** Cero trabajo sin cambio semántico, y trabajo idéntico y positivo con cambios
   idénticos.
5. **Cobertura.** Ninguna. El comentario («estimated total length») es correcto: aquí el radio es el
   número libre `k`, mientras que m04 fija `STRUCTURAL` para la circunferencia de radio literal 1. La
   propia prueba lo confirma, porque exige trabajo positivo del evaluador, que una longitud estructural
   no realiza.
6. **Prueba sustitutiva.** No procede.

#### locus/G9U0IntersectionTokenTest.java

Contiene G4 (i07 e i15).

**C32 · `i14StableSemanticPreimageRetainsExactTokenContinuation`** — **ATENCIÓN** (tokens)
1. **Comportamiento anterior.** `(s,b)`; al cambiar `b` el token continuaba.
2. **Contrato.** Fuera de clase v3. [CON] §3, fila «Transformaciones por semejanza».
3. **Aserción.** → `L0=(s,0)` y `L=Translate(L0,(0,b))`.
4. **Sigue verificándose.** La continuación exacta del token con la misma preimagen.
5. **Cobertura.** La revisión cambia ahora por la semejanza y no por la huella del generador.
6. **Prueba sustitutiva.** No procede.

**C72 · `i06LocallyUnisolatedRootCannotBecomeAPoint`** (añadida tras la primera redacción; ver §3)
1. **Comportamiento anterior.** `(s,abs(s))` en `[-1,1]` cortado por `y=0`: la raíz de la esquina en
   `s = 0` tenía aislamiento local `NOT_ESTABLISHED`.
2. **Contrato.** Fuera de clase v3: desde T11 la intersección no tiene raíces finitas, y la aserción
   `allMatch` sobre una lista vacía pasaba en vacío.
3. **Aserción.** El fixture pasa a `certifiedParabola()` cortada por `y=0`, tangente en el vértice. Se
   añaden `assertEquals(1, …getFiniteSolutions().size())` y la no admisibilidad de la raíz; la
   aserción de aislamiento no cambia.
4. **Sigue verificándose.** Una raíz cuyo aislamiento local no está establecido nunca es admisible como
   punto, ahora sin vacuidad.
5. **Cobertura.** La raíz es una tangencia de una curva certificada y no la esquina de una curva no
   derivable.
6. **Prueba sustitutiva.** No procede.

#### locus/G9U0MetricPositionTest.java

Contiene G6 (m11).

**C33 · `m01ExplicitSemanticPointCarriesDurableAddress`** — `createParabola()` → `createLine()`.
1. **Comportamiento anterior.** Dirección durable del punto sobre la parábola escalar.
2. **Contrato.** Fuera de clase v3, y E-2.
3. **Aserción.** Cambia el fixture; las aserciones no.
4. **Sigue verificándose.** La dirección durable.
5. **Cobertura.** Ninguna.
6. **Prueba sustitutiva.** No procede.

**C34 · `m02SelfIntersectionPositionsRemainDifferentPreimages`** — como C28: `projectedCircle()` con
±π/3.

**C35 · `m04RichTotalLengthIsNonnumericAuthority`** — restaurada por la resolución previa a T12 (§7.1)
1. **Comportamiento anterior.** La circunferencia y la elipse escalares eran `ESTIMATED`; la elipse medía
   15,86543958929059 ± 2E-6.
2. **Contrato.** Fuera de clase v3, con la longitud total rechazada.
3. **Aserción.** La circunferencia (G6) pasa de `ESTIMATED` a `STRUCTURAL`. Se elimina
   `assertRichTotal(ellipseMetric, 15.86543958929059, 2E-6)`, y la clase de la elipse pasa de
   `ESTIMATED` a `NOT_ESTABLISHED`, con `INVALID_QUERY` y sin valor.
4. **Sigue verificándose.** Autoridad no numérica de la longitud total, procedencia y clases.
5. **Cobertura.** La estimación pública de clase 3 de una elipse y de una circunferencia no estructural.
6. **Prueba sustitutiva.** `PreG9BCu1EstimatorTest.evaluatorOnlyCirclesAndEllipseHaveCoherentErrors` (solo
   evaluador) y C57 (parábola pública `ESTIMATED`).

**C36 · `m05RichPartialLengthDependsOnExactSemanticEndpoints`**
1. **Comportamiento anterior.** Puntos en 3π/2 y π/2, con una ruta que cruza 0.
2. **Contrato.** G6.
3. **Aserción.** → `pi/2` y `-pi/2`.
4. **Sigue verificándose.** `TARGET_REACHED`, dos contribuciones y longitud π a través de −π.
5. **Cobertura.** Ninguna.
6. **Prueba sustitutiva.** No procede.

#### locus/G9U0PublicSurfaceTestBase.java

Aquí se originan G4 (`certifiedParabola`, nuevo) y G6 (`createPeriodicCircle`, cambiado). Hay auxiliares
nuevos: `projectedCircle` y `assertNoTotalWithoutExistence`. `createParabola()` se conserva sin cambio
(ver la sección 3). Sin conformidad propia adicional.

#### locus/G9U0R2LocusPresentationTest.java

Solo G4 (diez métodos).

#### locus/G9U0R2LocusRenderContinuityTest.java

Contiene G1 (`unboundedTangent`), G4, G5 y G6.

#### locus/G9U0R4IntersectionAdmissibilityContinuationTest.java

Contiene G4.

**C37 · `localAdmissibilityRemainsIndependentOfGlobalCompleteness`, parte `(v,abs(v))`** — **ATENCIÓN**
(admisibilidad)
1. **Comportamiento anterior.** Había raíces con aislamiento `NOT_ESTABLISHED` y ninguna admisible.
2. **Contrato.** Fuera de clase v3: «COVERAGE_NOT_ESTABLISHED».
3. **Aserción.** `assertFalse(…getFiniteSolutions().isEmpty())` y los dos `allMatch` →
   `assertTrue(…isEmpty())` y el diagnóstico `COVERAGE_NOT_ESTABLISHED`.
4. **Sigue verificándose.** Ninguna raíz admisible.
5. **Cobertura.** Que una raíz de aislamiento no establecido nunca sea admisible en la ruta pública.
6. **Prueba sustitutiva.** `G8BIntersectionLifecycleTest.localizationOnlyAndFailedResidualCannotDefinePoint`.
   Ver la sección 3 (i06).

**C38 · `periodicSeamIsCanonicalAndDoesNotDuplicateTheBoundaryRoot`** — **ATENCIÓN** (costura)
1. **Comportamiento anterior.** Raíces en 0 (la costura) y π, dentro de `[0,2π)`.
2. **Contrato.** G6.
3. **Aserción.** `0, Math.PI` → `-Math.PI, 0`; el rango pasa a `[−π,π)`.
4. **Sigue verificándose.** La raíz de la costura es canónica y no se duplica.
5. **Cobertura.** Ninguna.
6. **Prueba sustitutiva.** No procede.

**C39 · `orientedRootCrossesPeriodicSeamWithoutDuplicateOrTokenSwap`** — **ATENCIÓN** (orientación,
tokens)
1. **Comportamiento anterior.** La raíz cerca de la costura 0 cruzaba hacia 3π/2.
2. **Contrato.** G6.
3. **Aserción.** `< Math.PI/2` → `> Math.PI/2`, y `> 3*Math.PI/2` → `< -Math.PI/2`.
4. **Sigue verificándose.** Cruce sin duplicar ni intercambiar tokens.
5. **Cobertura.** Ninguna.
6. **Prueba sustitutiva.** No procede.

**C40 · `stableTransverseMotionUsesDeterministicCurrentSelectors`** — **ATENCIÓN** (límite de CU3)
1. **Comportamiento anterior.** Movimiento transversal sobre la parábola escalar.
2. **Contrato.** Fuera de clase v3, y SplineV2 de proveedor. El comentario declara que la parábola
   certificada, como locus dependiente, no establece el germen numérico en cada paso: «an
   intersection-engine limitation reserved to CU3» ([DIS] §10; estrategia §8).
3. **Aserción.** `intersect(createParabola(), …)` → `intersect(parabolaSpline(), …)`.
4. **Sigue verificándose.** Selectores deterministas y tokens estables, ahora sobre tramos analíticos.
5. **Cobertura.** El movimiento transversal ya no se prueba sobre loci dependientes ni escalares. El
   cambio de fixture es una consecuencia de la clase v3 para la parábola escalar, no una corrección
   del motor: el motor de intersecciones no se modifica en CU2 y la limitación que muestra la
   parábola certificada, un locus dependiente cuyo germen numérico de raíz no se establece en cada
   paso del movimiento, pertenece al motor y no a la publicación de T11. **Observación para CU3**
   (declarada, no corregida): el seguimiento transversal de raíces sobre loci dependientes
   certificados.
6. **Prueba sustitutiva.** Ninguna para loci dependientes; limitación de `CU3`.

**C41 · `spatialLeftRightOrderCanReverseWithoutTokenSwap`** — **ATENCIÓN** (orientación)
1. **Comportamiento anterior.** Raíces en `a` y `a+π` dentro de `[0,2π)`.
2. **Contrato.** G6.
3. **Aserción.** `< Math.PI` y `> Math.PI` → `> 0` y `< 0`.
4. **Sigue verificándose.** El orden se invierte sin intercambio de tokens.
5. **Cobertura.** Ninguna.
6. **Prueba sustitutiva.** No procede.

**C42 · `repeatedDeterministicSelectorsUseIntrinsicSemanticRank`,
`rankedCollisionGroupAppearanceAndDisappearanceInvalidatesWithoutShift`** — **ATENCIÓN** (libro mayor)
1. **Comportamiento anterior.** Polinomio escalar en `[-3,3]`.
2. **Contrato.** Fuera de clase v3, y SplineV2 de proveedor.
3. **Aserción.** → `polynomialSpline("3")`, que interpola en múltiplos impares de 1/8.
4. **Sigue verificándose.** Cuatro raíces, rango intrínseco y grupo de colisión sin desplazamiento.
5. **Cobertura.** Las raíces son ahora las de la interpolación.
6. **Prueba sustitutiva.** No procede.

**C43 · `existingTwoRootPointsReactivateAfterFourRootTopologyRecurrence`** — **ATENCIÓN** (libro mayor,
reapertura)
1. **Comportamiento anterior.** Las raíces ±2 entraban por los extremos móviles en las rutas DIRECT,
   STAGED, REOPENED_DORMANT, MANY_SMALL y FORWARD_REVERSE.
2. **Contrato.** Como C01.
3. **Aserción.** → `SplineV2(If(reactivationBound>2,…))`.
4. **Sigue verificándose.** Recurrencia 2→4→2 y reactivación determinista.
5. **Cobertura.** Como C01. Con `2 < bound < 23/8` (2,25; 2,5 y los pasos de MANY_SMALL) la lista va de
   `−bound` a `−23/8 < −bound`: los nodos no son monótonos en `x` y la SplineV2 se pliega en sus
   extremos, donde el polinomio es positivo, así que sus cruces con `y = 0` siguen siendo las cuatro
   raíces. El javadoc de `polynomialNodes`, que afirmaba que la spline es siempre la gráfica del
   polinomio, se ha corregido.
6. **Prueba sustitutiva.** Ninguna.

**C44 · `reversingProviderOrientationNeverTransfersRankedTokensWithoutMap`** — **ATENCIÓN** (orientación,
tokens)
1. **Comportamiento anterior.** Polinomio escalar con dominio reversible, cortado por `y=0`.
2. **Contrato.** Fuera de clase v3.
3. **Aserción.** → `(p,0)` y `ImplicitCurve(y-(x^2-1)*(x^2-4))`.
4. **Sigue verificándose.** La reversión no transfiere tokens con rango: cuatro raíces, dos por clase de
   germen.
5. **Cobertura.** Se intercambian los papeles: la orientación se invierte sobre una recta y las raíces
   las aporta un objetivo implícito, que es otra capacidad.
6. **Prueba sustitutiva.** No procede.

**C45 · `rankedPeriodicSeamInvalidatesInsteadOfRotatingOpaqueTokens`** — **ATENCIÓN** (tokens, costura)
1. **Comportamiento anterior.** Generador escalar periódico no circular cortado por `y=0`.
2. **Contrato.** Fuera de clase v3.
3. **Aserción.** → `Rotate(Point(Circle((0,0),1)),rankedPhase)` con
   `Ellipse((-0.8703846,0),(0.8703846,0),1.0037776)` como objetivo.
4. **Sigue verificándose.** Invalidación al cruzar la costura, ahora en −π.
5. **Cobertura.** La costura de una curva no circular.
6. **Prueba sustitutiva.** No procede.

#### locus/G9U0R5SemanticTransformEdgeCasesTest.java

Contiene G1, G3a, G3b y G6.

**C46 · `decreasingOrientationAndNegativeScalePreserveSemanticAddress`** — **ATENCIÓN** (orientación)
1. **Comportamiento anterior.** `(s,s^2)` con dominio `{2,-2}`. La imagen en 0,5 era `(2,5; 7,25)` y el
   punto fuente `(0,5; 0,25)`.
2. **Contrato.** Fuera de clase v3.
3. **Aserción.** `"(s,s^2)"` → `"(s,s)"`; `7.25` → `6.5` (dos veces) y `0.25` → `0.5`.
4. **Sigue verificándose.** `DECREASING`, la dirección semántica y la longitud por 3.
5. **Cobertura.** Ninguna relevante.
6. **Prueba sustitutiva.** No procede.

#### locus/G9U0R5SimilarityTransformationsTest.java

Contiene G3b (e04) y G5 (m01).

**C47 · `m03PartialAndPeriodicMetricCovarianceUsesSemanticAddresses`** — fixture G6.
1. **Comportamiento anterior.** Ruta de longitud π entre 3π/2 y π/2, que cruzaba la costura en 0.
2. **Contrato.** G6.
3. **Aserción.** Cambia el fixture y se intercambian los extremos: `3*pi/2 → pi/2` pasa a
   `pi/2 → 3*pi/2`. La longitud esperada sigue siendo π.
4. **Sigue verificándose.** La covarianza métrica bajo rotación, con la ruta hacia delante de π/2 a
   3π/2 (canónico −π/2), que cruza la costura en −π.
5. **Cobertura.** Recuperada: con los extremos sin intercambiar, 3π/2 se canonicalizaba a −π/2 y la
   ruta ya no cruzaba la costura.
6. **Prueba sustitutiva.** No procede; C36 lo cubre también sin semejanza.

#### locus/G9U0R6SemanticLocusPointInteractionTest.java

Contiene G1 (r629 y r645), G3b (r627) y G6 (r639 y r640).

**C48 · `r609PeriodicSeamUsesCanonicalParameterAndNearestIntrinsicLift`** — **ATENCIÓN** (costura)
1. **Comportamiento anterior.** Semilla en `(1,0)` y ángulo −0,05.
2. **Contrato.** G6.
3. **Aserción.** `resolve(circle, 1, 0, …)` → `(-1, 0)`; `angle = -0.05` → `-Math.PI - 0.05`; `x` pasa
   de 1 a −1.
4. **Sigue verificándose.** Parámetro canónico y elevación más próxima al cruzar, con la dirección
   retenida.
5. **Cobertura.** Ninguna.
6. **Prueba sustitutiva.** No procede.

**C49 · `r624VersionedAddressStateRoundTripPreservesPeriodicSemanticEvidence`** — **ATENCIÓN**
(identidad persistida)
1. **Comportamiento anterior.** Semilla y consulta elevada en `(1,0)`.
2. **Contrato.** G6.
3. **Aserción.** `(1,0)` → `(-1,0)` en ambas.
4. **Sigue verificándose.** Ida y vuelta del estado versionado de la dirección, con `UPPER_APPROACH`.
5. **Cobertura.** Ninguna.
6. **Prueba sustitutiva.** No procede.

**C50 · `r630CuspWithZeroSemanticSpeedCannotClaimUniqueAddress`**
1. **Comportamiento anterior.** Cúspide escalar `(t^2,t^3)`.
2. **Contrato.** Fuera de clase v3.
3. **Aserción.** → cardioide pedal de clase v1; `resolve(cusp, 0, 0, …)` → `(1, 0)`.
4. **Sigue verificándose.** Con velocidad nula no hay dirección única.
5. **Cobertura.** La cúspide polinómica escalar.
6. **Prueba sustitutiva.** No procede.

**C51 · `r638RepeatedComponentLossPreservesLastAcceptedAddressUntilRecovery`** — **ATENCIÓN**
(identidad persistida; limitación registrada en §7.4)
1. **Comportamiento anterior.** La segunda pérdida era la componente desplazada `{-1.75,-1.25}`, que
   contiene −1,5 pero tiene otra clave binary64; el punto seguía indefinido.
2. **Contrato.** [EXI] §15.6: «Moving a boundary without changing its source keeps the lineage». [DIS]
   §7 y [APR] E-3.
3. **Aserción.** `"D={false,{-1.75,-1.25,…},{1,2,…}}"` → `"D={false,{-1.25,-1.1,…},{1,2,…}}"`.
4. **Sigue verificándose.** La dirección aceptada se conserva y la revinculación exacta funciona.
5. **Cobertura.** Con la clave estructural, una componente declarada que se desplaza sin cambiar de
   fuente conserva el linaje y revincula el punto si lo contiene. **Afirmado ahora** en la misma
   prueba: tras la segunda pérdida, la componente desplazada original `{-1.75,-1.25}`, que contiene
   −1,5, revincula el punto a su dirección aceptada. Sigue sin probarse la resolución, al abrir un
   documento anterior a CU2, de un selector con clave binary64 de una componente parcial (limitación
   declarada, como C70).
6. **Prueba sustitutiva.** La propia prueba.

**C52 · auxiliar `assertDiscoveredCandidatesForwardVerified`** (r609, r618, r631 y r646) — **ATENCIÓN**
(identidad persistida)
1. **Comportamiento anterior.** La clave de la dirección era
   `LocusComponentLineage2D.create(branchKey, component)` de la vista heredada.
2. **Contrato.** [EXI] §15.6. [CON] §7, precisión de T9: las direcciones nuevas citan
   `LocusComponentLineage2D.of`.
3. **Aserción.** `branch.getValidDomainComponents()…create(…)` →
   `structure.getContinuousValidComponents()…LocusComponentLineage2D.of(structure, component)`.
4. **Sigue verificándose.** El candidato cita una componente certificada que contiene su parámetro y se
   verifica hacia delante.
5. **Cobertura.** Con certificados publicados, las direcciones nuevas citan claves estructurales (por
   ejemplo, `…/component/periodic-closure`): el texto persistido de los puntos interactivos nuevos
   difiere del anterior a CU2. r618 y r646 usan loci fuera de clase (ver la sección 3).
6. **Prueba sustitutiva.** No procede.

#### locus/LocusMetricProductiveImproperTest.java, LocusMetricProductiveLifecycleTest.java, LocusMetricProductiveNestedTest.java, LocusV2FunctionalBenchmarkTest.java, LocusV2HardeningBenchmarkTest.java, LocusV2HardeningValueContractsTest.java, LocusV2LifecycleHardeningTest.java, LocusV2RenderHardeningTest.java, LocusV2SessionHardeningTest.java y LocusV2ValueContractsTest.java

Solo G1 o G2.

#### locus/LocusV2KernelIntegrationTest.java

Contiene G1, G2 y G3a.

**C53 · `providerOwnsDynamicBranchTopologyAndLineage` (parámetros aislados)**
1. **Comportamiento anterior.** La vista heredada listaba dos intervalos de longitud cero.
2. **Contrato.**
   - [EXI] §15.4.
   - [DIS] §9, precisión de T11: un intervalo de longitud cero es una componente puntual
     (`PROVIDER_ISOLATED_POINT`).
   - [CON] §4, precisión de T11: la vista heredada no los lista.
   - D-CU2-19.
3. **Aserción.** `assertTopology(…, 2, …)` y `interval.getLower() == getUpper()` →
   `assertTopology(…, 0, …)`, `getPointComponents().size() == 2` y
   `point.getParameterLower() == getParameterUpper()`.
4. **Sigue verificándose.** La topología dinámica y el linaje.
5. **Cobertura.** Ninguna.
6. **Prueba sustitutiva.** No procede.

**C54 · `typedInvalidEvaluationsNeverCarryStaleCoordinates`**
1. **Comportamiento anterior.** La definición `non-finite` usaba `branches(provider)`, una rama de
   compatibilidad.
2. **Contrato.** [CON] §4 T9: P5 solo sobre hechos probados. [EXI] §15.5.
3. **Aserción.** `branches(provider)` → `LocusV2TestBranches.unproved(…)`, que usa `notEstablished`.
4. **Sigue verificándose.** `NON_FINITE` tipado, sin coordenadas obsoletas.
5. **Cobertura.** El fallo del evaluador pasa a una rama no certificada; en una rama probada sería G3b.
6. **Prueba sustitutiva.** G3b.

#### locus/LocusV2RenderSeparationTest.java

Contiene G1, G2 y G3a.

#### locus/PostG9U1A2ExplicitTraversalPolicyTest.java

**C55 · `periodicSeamAndSamePositionPoliciesUseSemanticAddresses`** — **ATENCIÓN** (costura)
1. **Comportamiento anterior.** Puntos en 5,5 y 0,5 dentro de `[0,2π)`.
2. **Contrato.** G6.
3. **Aserción.** → `2.5` y `-2.5`.
4. **Sigue verificándose.** La ruta directa cruza la costura con la misma longitud (2π − 5), y las
   políticas de misma posición.
5. **Cobertura.** Ninguna.
6. **Prueba sustitutiva.** No procede.

#### locus/PostG9U1A3CompatibleV2RedefineTest.java

**C56 · `compatibleLocusRedefineRetainsIdentityAndAdvancesTopology`** — **ATENCIÓN** (identidad en la
redefinición)
1. **Comportamiento anterior.** `L` se redefinía de `(s,0)` a `(s,2*s)`.
2. **Contrato.** Fuera de clase v3: forma afín no directa (L-CU1-AFFINE-FORMS).
3. **Aserción.** `"Q2=(s,2*s)"` → `"Q2=(s,s)"`.
4. **Sigue verificándose.** Las identidades de L, P y M, las revisiones +1 y el deshacer y rehacer.
5. **Cobertura.** La redefinición hacia una expresión fuera de clase con puntos semánticos dependientes.
6. **Prueba sustitutiva.** Parcial: C68, que cubre reclamaciones de intersección, no puntos semánticos.

#### locus/PostG9U1A6OptionalBranchPointTest.java

Contiene G1 y G6 (`periodicSourceRetains…`).

#### locus/PreG9BCu1EstimatorTest.java

Contiene G1. Las tres conformidades siguientes son **ATENCIÓN** por [MAT] V-CU1-1: «Las pruebas de
aceptación de CU1 … se mantienen».

**C57 · `sineArcLengthsAreHonestEstimates`** — restaurada por la resolución previa a T12 (§7.1)
1. **Comportamiento anterior.** El seno público `(x(P),sin(4πx(P)))` se estimaba (k=2).
2. **Contrato.** Fuera de clase v3, con la longitud total rechazada.
3. **Aserción.** `assertCoherentEstimate(metric…, …, "public k=2")` → `assertNoTotalWithoutExistence(…)`.
   Se añade `assertCoherentEstimate` de la parábola certificada pública.
4. **Sigue verificándose.** El estimador en curvas de evaluador (k = 1, 2 y 4) y en una curva pública
   certificada (`ESTIMATED`).
5. **Cobertura.** Una curva oscilante pública.
6. **Prueba sustitutiva.** La parte de evaluador del mismo método.

**C58 · `adversarialCurvesNeverReceiveACertifiedClass`** — restaurada por la resolución previa a T12
(§7.1)
1. **Comportamiento anterior.** Las curvas públicas de aliasing y `floor` no recibían clase 1 o 2.
2. **Contrato.** Como C57.
3. **Aserción.** `assertNotCertified(…)` → `assertNoTotalWithoutExistence(…)` (dos veces).
4. **Sigue verificándose.** Nunca una clase certificada; en la ruta pública ni siquiera hay estimación.
5. **Cobertura.** La clasificación pública adversaria.
6. **Prueba sustitutiva.** La parte de evaluador del mismo método.

**C59 · `resultsDoNotDependOnTheView`**
1. **Comportamiento anterior.** Dos loci escalares `(t,sin t)`.
2. **Contrato.** Fuera de clase v3.
3. **Aserción.** → dos `certifiedParabola(…)`.
4. **Sigue verificándose.** Independencia bit a bit de la vista (V-INV-2).
5. **Cobertura.** Ninguna.
6. **Prueba sustitutiva.** No procede.

#### locus/PreG9BCu1MetricClassesTest.java

Todas son **ATENCIÓN** por V-CU1-1.

**C60 · `affineImagesAreStructural`** — restaurada por la resolución previa a T12 (§7.1)
1. **Comportamiento anterior.** `(2w+1,3w-1)` era `ESTIMATED`, sin envolvente, con valor 4√13 y
   admisible.
2. **Contrato.** [DIS] §4.1, texto aprobado: «expresiones afines no directas (`L-CU1-AFFINE-FORMS`…)»
   fuera de la clase. [CON] §3, tabla aprobada: la métrica total exige `COMPLETE` en cada rama («Con
   `NOT_ESTABLISHED`, `NOT_ESTABLISHED`»), regla que ya aplicaba el motor métrico. No cambia ningún
   valor ni política de `LocusLength`: el locus pasa a `NOT_ESTABLISHED` y la regla existente rechaza
   su total. El cierre de CU1 (`pre_g9b_cu1_closeout_record.md`) describe L-CU1-AFFINE-FORMS bajo el
   certificado de compatibilidad («other affine expressions are measured in class 3») y la asigna a
   `CU2` «only if strictly needed for its scope; otherwise a later package»; el diseño aprobado de
   `CU2` la deja fuera de la clase.
3. **Aserción.** Las cuatro aserciones de estimación → `assertNoTotalWithoutExistence(expression, …)`.
4. **Sigue verificándose.** Las formas afines directas son estructurales.
5. **Cobertura.** La medición de clase 3 de formas afines no directas.
6. **Prueba sustitutiva.** Ninguna; limitación.

**C61 · `similarityImagesKeepTheSourceClassAndScaleValueAndError`**
1. **Comportamiento anterior.** La fuente no estructural, un seno, tenía la clase fijada en `ESTIMATED`.
2. **Contrato.** Fuera de clase v3.
3. **Aserción.** La fuente pasa a `certifiedParabola`; la clase sigue fijada en `ESTIMATED`, como en
   C57 para la misma parábola. Un primer borrador de la conformidad admitía también
   `CERTIFIED_ENCLOSURE` y se ha vuelto a fijar.
4. **Sigue verificándose.** Las imágenes conservan la clase `ESTIMATED` de la fuente y escalan el valor
   y el error.
5. **Cobertura.** La fuente oscilante (un seno).
6. **Prueba sustitutiva.** No procede.

**C62 · `scalarAdapterPublishesExactlyTheAdmissibleRichValues`** — restaurada por la resolución previa
a T12 (§7.1)
1. **Comportamiento anterior.** `b=Length(Lt)` de `(t,sin t)` estaba definido con `ESTIMATED`.
2. **Contrato.** Fuera de clase v3.
3. **Aserción.** Se añade `assertFalse(…bt=Length(Lt)…isDefined())`. La fuente estimada pasa a la
   parábola certificada, con `assertEquals(ESTIMATED, …)` como antes (un primer borrador lo había
   debilitado a `assertNotEquals(STRUCTURAL, …)`).
4. **Sigue verificándose.** El adaptador publica exactamente los valores admisibles y nada si la
   existencia no está establecida.
5. **Cobertura.** La fuente oscilante.
6. **Prueba sustitutiva.** No procede.

#### locus/PreG9BCu1RevisionAndSessionTest.java

Solo G1.

#### locus/PreG9BCu2ConsumerMigrationTest.java

**C63 · `aDependentOnALocusSupportInheritsTheArc`** — **ATENCIÓN** (V-CON-6 cubierta solo en parte en T11; restaurada por la resolución previa a T12, §7.2)
1. **Comportamiento anterior.** `compatibilityInherited` (retirado) daba al dependiente una componente
   con el arco que cruza la costura, los mismos extremos, sin cerrar, con la misma completitud y la
   clave binary64.
2. **Contrato.**
   - [CCM] §9: soporte locus fuera de clase.
   - [CON] §3 «Composición de loci dependientes» y §4 T11.
   - [DIS] §9 T11: retirada.
   - [MAT] V-CON-6.
3. **Aserción.** Se sustituye por una construcción real. El soporte es `COMPLETE` con un arco que cruza
   la costura, y el dependiente `M=LocusV2(S+(1,0),S)`. El dominio declarado son las piezas del arco y
   el resultado es `NOT_ESTABLISHED`, sin componentes y con `OPERATION_OUTSIDE_CLASS`.
4. **Sigue verificándose.** El dominio procede del soporte y la completitud no lo supera.
5. **Cobertura.** «Hereda el arco» queda solo como dominio declarado, sin componente ni linaje.
6. **Prueba sustitutiva.** Ninguna; limitación de la clase v3.

#### locus/PreG9BCu2ExistenceStructureV2Test.java

**C64 · `compatibilityIsExpressedInTheNewModelWithoutLegacyChange` → renombrado
`theProviderCertificateCitesItsContract`**
1. **Comportamiento anterior.** `completeCompatibility`: contrato de compatibilidad, naturaleza `NONE`,
   evidencias `provider-endpoints/v1` y `provider-interval-contract/v1`, y cierre por la bandera.
2. **Contrato.** [EXI] §15.7, D-CU2-7 y [DIS] §9 T11.
3. **Aserción.** → `providerProved(BRANCH, …, periodic, closed, contract)`, con `PROVIDER_PROVED_CONTRACT`,
   naturaleza `EXACT`, evidencia igual al contrato y origen `provider-contract`. Se añade el caso
   periódico sin cierre probado, que da un arco abierto.
4. **Sigue verificándose.** El orden canónico, la cobertura alterna y el cierre solo con prueba.
5. **Cobertura.** Ninguna; la API está retirada. El método se renombra; no figura en ninguna selección ni
   inventario del repositorio.
6. **Prueba sustitutiva.** No procede.

#### locus/PreG9BCu2RepresentationTest.java

Sin conformidades: solo cambia el javadoc de `publishCertificate`. Métodos nuevos: V-B07-4
`exploratoryEvidenceIsDrawnApartAndOnlyInsideItsBands` y V-DXF-2
`aLocusWithoutEstablishedExistenceIsDeclaredWithTheMandatorySidecar`.

#### locus/PreG9BS1R2ExistenceStructureTest.java

Solo G1. El auxiliar conserva el nombre `compatibilityBranch`.

#### locus/PreG9bR2E1SemanticPairMaterializationTest.java

**C65 · `affineScalarPairMaterializesAndUncertifiableScalarIsDiagnosedRichOnly`**
1. **Comportamiento anterior.** La parábola `(u,u^2-1)` daba un par solo rico.
2. **Contrato.** Fuera de clase v3.
3. **Aserción.** El fixture pasa a la circunferencia de radio 3/2 por intersección recta–circunferencia.
   Las aserciones no cambian.
4. **Sigue verificándose.** El par afín se materializa y el no modelado se diagnostica.
5. **Cobertura.** Ninguna. El comentario decía «class v2», como en C24, y se ha corregido a clase v3.
6. **Prueba sustitutiva.** No procede.

**C66 · `slicesOutsideClassV1StayRichOnlyWithDiagnostic`**
1. **Comportamiento anterior.** `expression` tenía raíces solo ricas con «no certified interval curve
   model».
2. **Contrato.** [CCM] §9, que menciona `(x(C)/2, y(C))`, y [CON] §4 T11.
3. **Aserción.** → `"COVERAGE_NOT_ESTABLISHED"` para `expression`; la rama cónica no cambia.
4. **Sigue verificándose.** Ninguna raíz elegible.
5. **Cobertura.** Las raíces solo ricas de un locus de expresión.
6. **Prueba sustitutiva.** No procede.

#### locus/PreG9bR3X1ExpressionPointPairMaterializationTest.java

**C67 · `anUncertifiedExpressionIssuesNoPairToken`** — **ATENCIÓN** (libro mayor)
1. **Comportamiento anterior.** Había raíces solo ricas, ningún token de par y el libro mayor en `4|…`.
2. **Contrato.** Fuera de clase v3.
3. **Aserción.** `assertFalse(…isEmpty())` → `assertTrue(…isEmpty())`, con el diagnóstico
   `COVERAGE_NOT_ESTABLISHED`.
4. **Sigue verificándose.** Ningún token de par, el libro mayor en `4|…` y ningún programa capturado.
5. **Cobertura.** La ausencia de token cuando hay raíces.
6. **Prueba sustitutiva.** No procede.

#### locus/PreG9bR3X1RedefineWitnessTest.java

**C68 · `aRedefineIntoAnUnsupportedExpressionIsTruthfullyRichOnly`** — **ATENCIÓN** (libro mayor,
redefinición)
1. **Comportamiento anterior.** Había raíces solo ricas, la reclamación quedaba latente y `X` indefinido
   con su identidad.
2. **Contrato.** Fuera de clase v3.
3. **Aserción.** `assertFalse(…isEmpty())` → `assertTrue(…isEmpty())`, con el diagnóstico
   `COVERAGE_NOT_ESTABLISHED`.
4. **Sigue verificándose.** La reclamación nunca se mueve a otra raíz y la identidad se conserva.
5. **Cobertura.** La latencia con raíces presentes pero inelegibles.
6. **Prueba sustitutiva.** Ninguna identificada.

#### org/geogebra/common/euclidian/DrawablesTest.java

Solo G2.

## 3. Pruebas no modificadas con fixtures fuera de la clase v3 (auditoría de vacuidad)

Una prueba que T11 no modifica puede seguir pasando sin ejercitar ya su propiedad, porque bajo un
locus fuera de la clase v3 la intersección se rechaza antes de trabajar, la longitud total se rechaza
sin evaluar, el resolvedor de interacción se detiene sin componentes que buscar y el dibujo no tiene
vértices certificados. Se auditó el código de cada método que usa un fixture así. La tabla da el
veredicto y lo hecho; las pruebas corregidas pasan a ser conformidades de este paso, con el mismo
formato resumido en la columna «Acción».

| Prueba | Veredicto | Acción |
|---|---|---|
| `G9U0IntersectionTokenTest.i01` | sigue significativa: despacho e identidad de diez familias, y la parte `(sOption,0)` está certificada | ninguna |
| `G9U0IntersectionTokenTest.i06` | vacua: `allMatch` sobre una lista vacía | corregida: C72 |
| `G9U0IntersectionTokenTest.i10` | vacua: la intersección se rechaza sin trabajar, así que el límite de trabajo no se alcanza (y antes de T11 tampoco se afirmaba) | declarada. Sustituta: `G8BIntersectionKernelTest.deterministicWorkLimitProducesTypedAtomicFailure`, que afirma `WORK_LIMIT_REACHED`, `UNRESOLVED` y ninguna solución sobre una rama de fixture; el comando público no permite fijar el presupuesto |
| `G9U0GeneratorSuiteTest.g01`, `g18` | siguen significativas: la identidad es el `PersistentGeoId` | ninguna |
| `G9U0GeneratorSuiteTest.g03` | vacua: la longitud total se rechaza sin evaluar | corregida: evalúa tres puntos del locus (su evaluador sigue respondiendo fuera de la clase), exige llamadas al evaluador y conserva las aserciones sobre `s` y `t` |
| `G9U0GeneratorSuiteTest.g19`, `g20` | testigo debilitado: el punto semántico estaba indefinido | corregidas: `createLine()` y aserciones de que el punto y sus copias están definidos |
| `G9U0R2LocusRenderContinuityTest.addingMovingStylingAndDeletingCrossingLineCannotSplitTheLocus` | parcialmente vacua: huellas de dibujo vacías y ninguna raíz | corregida: `certifiedParabola()`, dos raíces en `y=1`, longitud finita y una subtrayectoria dibujada |
| `G9U0R5SimilarityTransformationsTest.c01` | sigue significativa: despacho e identidad | ninguna |
| `G9U0R5SimilarityTransformationsTest.e01` | parcialmente vacua: compara dos listas vacías de componentes | corregida: `certifiedParabola()`, componentes no vacías, punto en `u = 0,625` |
| `G9U0R5SimilarityTransformationsTest.x04` | vacua: diez rechazos inmediatos | corregida: imagen certificada y al menos una familia que evalúa |
| `G9U0R5SimilarityTransformationsTest.p01` | debilitada: no reabría una imagen certificada | corregida: `certifiedParabola()`, punto en `u = 0,625` |
| `G9U0R6SemanticLocusPointInteractionTest.r618` | vacua: barrera del resolvedor sin candidatos | corregida: `certifiedParabola()` y `assertAllCandidatesForwardVerified`, que exige al menos un candidato |
| `G9U0R6SemanticLocusPointInteractionTest.r646` | vacua: el escalar exponencial no tiene componente que buscar | declarada. Ningún fixture certificado tiene un mínimo estrecho genérico sin un evaluador de prueba; la propiedad (un mínimo estrecho no eleva la cobertura acotada) queda sin prueba pública |
| `G9U0MetricPositionTest.m07`, `m08` | parcialmente vacuas: la parte pública pasa por el rechazo genérico fuera de clase; la del integrador y la puerta rica siguen siendo significativas | declaradas. Sustitutas: `LocusMetricProductiveImproperTest` (métrica impropia o infinita en el motor) y `G9U0R5SimilarityTransformationsTest.e04` (`EVALUATION_FAILED` dentro de una componente certificada) |
| `PreG9BR6PlusCLatexExportTest.invalidToleranceAndStaleSourcesRejectTheWholeExport` | sigue significativa, con el testigo de revisión obsoleta debilitado | corregida: `Q=(s,s)`, con una componente certificada |
| `G9U0CommandSurfaceTest.c09`, `c10` | vacuas: la longitud total no evalúa nada | corregidas: `certifiedParabola()`, cuya longitud estimada evalúa el locus (`getEvaluatorCalls() > 0`); c09 mueve el conductor de la parábola y comprueba que conserva su estado |
| `G9U0CommandSurfaceTest.c14` | sigue significativa: revisión e identidad no dependen del certificado | ninguna (sus dos componentes no están certificadas, pero la prueba no las afirma) |
| `G9U0CommandSurfaceTest` c02, c06, c08, c11, c12 y c13 | siguen significativas | ninguna |
| `G9U0PersistenceCompatibilityTest.p05` | parcialmente vacua: la ida y vuelta llevaba un punto indefinido, una longitud rechazada y ninguna raíz | corregida: `certifiedParabola()` con punto definido, longitud finita y dos raíces |
| `G9U0PersistenceCompatibilityTest.p07` | testigo debilitado | corregida: `certifiedParabola()` y punto copiado definido |
| `G9U0PersistenceCompatibilityTest.p01`, `p13` | siguen significativas: descriptores y preservación | ninguna |

El auxiliar `assertDiscoveredCandidatesForwardVerified` no exige candidatos; r618 usa ahora
`assertAllCandidatesForwardVerified`, que sí los exige.

## 4. Resumen

Categorías: F = fixture fuera de clase v3; API = API retirada; PUB = publicación; E3 = identidad E-3;
PT = componentes puntuales; EXP = exportación; OTRA = P5 u otra.

| Archivo | Conformidades propias | Grupos | Categoría |
|---|---:|---|---|
| G9U0R3InspectorWorkflowTest | 2 (C69, C70) | — | F, E3 |
| G9U0R4NativeArchivePersistenceTest | 1 | — | F |
| G9U0R5NativeArchivePersistenceTest | 1 | — | F |
| G9U1PeriodicQuarantineNativeTest | 1 | — | F |
| G9U1SemanticPointInteractionTest | 0 | G6 | F |
| PreG9BCu2AuthorModelCertificationTest | 2 (C04, C73) | — | PUB |
| PreG9BCu2AuthorModelClassV3Test | 1 | — | PUB |
| PreG9BCu2AuthorModelConsumerTest | 2 | — | PUB |
| PreG9BCu2AuthorModelLineageTest | 1 | — | E3 |
| PreG9BCu2AuthorModelRepresentationTest | 0 | — | — (solo adiciones) |
| PreG9BCu2ExportDeclarationTest | 0 | G5 | F, EXP |
| PreG9BR6PlusA2HiddenLayerPersistenceTest | 0 | G5 | F |
| PreG9BR6PlusBPictureFidelityTest | 0 | G5 | F, EXP |
| PreG9BR6PlusCBaseIdentityTest | 1 | — | EXP, F |
| PreG9BR6PlusCLatexExportTest | 2 | G5, G6 | EXP, F |
| PreG9BS1AuthorDxfReproductionTest | 2 | — | EXP (D-CU2-9) |
| locus/G9U0ToolSurfaceTest | 1 | — | F |
| G9X1LocusV2ExportTest | 4 | G5, G6 | F, EXP |
| PreG9BR6PlusCSharedExportTest | 1 | G5, G6 | F, EXP |
| G9S1R1SplinePairIntervalCertificationTest | 0 | G1 | API |
| PiecewisePolynomialPairIntersectionCapability2DTest | 0 | G1 | API |
| PreG9BCu2ClassV3ModelTest | 0 | — | — (comentarios) |
| PreG9BCu2HybridCertificationTest | 2 | — | PUB |
| PreG9BCu2LineageTest | 0 | — | — (javadoc) |
| PreG9bR3X1CertifiedExpressionPointModelTest | 1 | — | PUB |
| G7BMetricFixtures | 0 | G1 | API |
| G8AIntersectionFixtures | 0 | G2 | API |
| G8AIntersectionFunctionalBenchmarkTest | 0 | G2 | API |
| G8AIntersectionTopologyCharacterizationTest | 1 | G1, G2 | PT, API |
| G8BIntersectionFixtures | 0 | G1 | API |
| G8BIntersectionTopologyAndScientificTest | 1 | — | PT |
| G8CDesignFunctionalBenchmarkTest | 0 | G2 | API |
| G9S1R1SplinePairMaterializationTest | 1 | — | F |
| G9U0GeneratorSuiteTest | 7 | G6 | F, PUB |
| G9U0IntersectionTokenTest | 2 (C32, C72) | G4 | F |
| G9U0MetricPositionTest | 4 | G6 | F |
| G9U0PublicSurfaceTestBase | 0 | origen de G4 y G6 | F |
| G9U0R2LocusPresentationTest | 0 | G4 | F |
| G9U0R2LocusRenderContinuityTest | 0 | G1, G4, G5, G6 | F, API |
| G9U0R4IntersectionAdmissibilityContinuationTest | 9 | G4 | F |
| G9U0R5SemanticTransformEdgeCasesTest | 1 | G1, G3a, G3b, G6 | F, OTRA, API |
| G9U0R5SimilarityTransformationsTest | 1 | G3b, G5 | F, OTRA |
| G9U0R6SemanticLocusPointInteractionTest | 5 | G1, G3b, G6 | F, E3, OTRA |
| LocusMetricProductiveImproperTest | 0 | G1 | API |
| LocusMetricProductiveLifecycleTest | 0 | G2 | API |
| LocusMetricProductiveNestedTest | 0 | G2 | API |
| LocusV2FunctionalBenchmarkTest | 0 | G2 | API |
| LocusV2HardeningBenchmarkTest | 0 | G1 | API |
| LocusV2HardeningValueContractsTest | 0 | G1 | API |
| LocusV2KernelIntegrationTest | 2 | G1, G2, G3a | PT, OTRA, API |
| LocusV2LifecycleHardeningTest | 0 | G2 | API |
| LocusV2RenderHardeningTest | 0 | G1, G2 | API |
| LocusV2RenderSeparationTest | 0 | G1, G2, G3a | API, OTRA |
| LocusV2SessionHardeningTest | 0 | G1 | API |
| LocusV2ValueContractsTest | 0 | G1 | API |
| PostG9U1A2ExplicitTraversalPolicyTest | 1 | — | F |
| PostG9U1A3CompatibleV2RedefineTest | 1 | — | F |
| PostG9U1A6OptionalBranchPointTest | 0 | G1, G6 | API, F |
| PreG9BCu1EstimatorTest | 3 | G1 | F (V-CU1-1) |
| PreG9BCu1MetricClassesTest | 3 | — | F (V-CU1-1) |
| PreG9BCu1RevisionAndSessionTest | 0 | G1 | API |
| PreG9BCu2ConsumerMigrationTest | 1 | — | API, F |
| PreG9BCu2ExistenceStructureV2Test | 1 | — | API |
| PreG9BCu2RepresentationTest | 0 | — | — (solo adiciones) |
| PreG9BS1R2ExistenceStructureTest | 0 | G1 | API |
| PreG9bR2E1SemanticPairMaterializationTest | 2 | — | F |
| PreG9bR3X1ExpressionPointPairMaterializationTest | 1 | — | F |
| PreG9bR3X1RedefineWitnessTest | 1 | — | F |
| DrawablesTest | 0 | G2 | API |
| G9U0CommandSurfaceTest | 2 (§3: c09, c10) | — | F |
| G9U0PersistenceCompatibilityTest | 2 (§3: p05, p07) | — | F |
| **Total: 71 archivos** | **73 propias (C01–C73) y 13 corregidas en la §3** | **7 agrupadas (G1, G2, G3a, G3b, G4, G5, G6)** | |

Las correcciones de la §3 también afectan a archivos ya listados: `G9U0GeneratorSuiteTest` (g03, g19 y
g20), `G9U0R2LocusRenderContinuityTest` (R2-L09), `G9U0R5SimilarityTransformationsTest` (e01, x04 y
p01), `G9U0R6SemanticLocusPointInteractionTest` (r618), `G9U0IntersectionTokenTest` (i06, que es C72)
y `PreG9BR6PlusCLatexExportTest`.

## 5. Pruebas nuevas

- `PreG9BCu2AuthorModelExportTest` (escritorio): DXF y LaTeX del modelo del autor siguen la topología de
  los certificados publicados. Un `LWPOLYLINE` por componente, cerrado solo si es periódica cerrada;
  `e_2` en tres partes abiertas cortadas en sus dos huecos computacionales declarados; las pruebas LaTeX
  citan la especificación durable; cierran solo componentes periódicas cerradas y compilan (V-DXF-1,
  V-E2-3, V-TEX-1 y V-TEX-2). V-E2-9 y V-AVAIL-3: un extremo excluido de `e_2` no evaluable se declara
  (`END_NOT_EVALUATED`) y nunca se extrapola en el dibujo, el DXF ni LaTeX. Ningún presupuesto de trabajo
  hace fallar solo el límite lateral, porque todos los valores de `w_1` leen la misma estimación de la
  longitud de `o_1` (presupuestos hasta 2048 fallan el límite y siete parámetros interiores; desde 3072
  todo se evalúa), así que la prueba retira la longitud de la consulta del límite mientras se exporta.
- `PreG9BCu2AuthorModelSelectionSettlementTest` (escritorio): D-CU2-T11-R1 en el modelo del autor. El
  corte reconstruido con el conductor en el hueco o en `t_c` se asienta al estado vivo, el certificado
  es el de `t = −1`, y la construcción viva y las identidades quedan intactas (V-REV-EXACT-1, V-SEL-1 y
  V-PUB-1).
- `PreG9BCu2SelectionSettlementTest` (compartida): `selection-settlement/v1`, la salida de la primera
  inicialización en consultas y `selection-independent-nonexistence/v1`, sobre construcciones sintéticas
  recta–circunferencia (V-REV-EXACT-1, V-SEL-1, V-SEL-6, V-SEL-7, V-COINC-3, V-COINC-8 y V-PUB-3).
- `LocusV2TestBranches` (compartida; auxiliar, no es una prueba): ramas de fixture con certificado
  explícito `locus-v2-test-fixture/v1` (`branch`) y ramas no probadas (`unproved`).
- Métodos nuevos en archivos modificados (no son conformidades): V-PUB-1 a V-PUB-4
  (`PreG9BCu2AuthorModelCertificationTest`; V-PUB-3 comprueba el estado vacío tras el cambio dinámico,
  por la prueba de inexistencia independiente de la selección, y el mismo documento reabierto, por el
  certificado ordinario), V-B07-1 y V-B07-3 (`PreG9BCu2AuthorModelRepresentationTest`), V-B07-4 y
  V-DXF-2 (`PreG9BCu2RepresentationTest`), y la regresión del defecto destapado por C70,
  `declaredComponentsSharingAnExcludedEndAreRestrictedInOrder` (`PreG9BCu2ExistenceStructureV2Test`).

## 6. Revisión del registro tras la primera redacción

- **Defecto de producción.** C70 destapó que un dominio escalar declarado con dos tramos que comparten
  un extremo excluido dejaba el locus indefinido (orden de la cobertura en
  `LocusExistenceStructure2D`). Está corregido y tiene prueba de regresión (ver C70).
- **Aserciones que pasaban en vacío.** C72 (`i06`: lista de raíces vacía) y C73 (GC-6 medida alrededor
  de la lectura de un certificado ya calculado). La auditoría de la sección 3 encontró además trece
  métodos no modificados, vacuos o con el testigo debilitado, que se han corregido con fixtures
  certificados, y cuatro (i10, r646, m07 y m08) que se declaran con sus pruebas sustitutivas.
- **Cobertura recuperada en las propias pruebas.** La costura en `±π` (G6: g14 y la dilatación
  negativa; C47: ruta de m03), la revinculación explícita de r638 (C51) y la clase `ESTIMATED` fijada
  de nuevo (C61 y C62). En l06 (C16) se intentó acercar los extremos a 1E-13, y el contrato de la
  SplineV2 lo impide; la limitación queda documentada.
- **Marcas resueltas sin cambio de prueba.** G1 (cierre declarado deliberadamente adversario), C10 y
  C12 (unión por la costura respaldada por textos aprobados), C31 (comentario correcto) y C60
  (precedencia del diseño aprobado de `CU2` y regla existente de la métrica total). Se corrigieron los
  comentarios de C24 y C65 y los javadocs de C43 y de `G7BMetricFixtures.periodicDefinition`.
- **Observación para `CU3`.** C40: seguimiento transversal de raíces sobre loci dependientes
  certificados. Se declara y no se corrige en `CU2`.
- **Limitaciones declaradas que se mantienen.** C09 (identidad de bytes de exportaciones DXF del
  generador escalar con la base), C22 y C23 (`ISOLATED_COMPONENT` ya no se ejercita; D-CU2-19 y `CU3`),
  C51 y C70 (documentos anteriores a CU2 con tokens o selectores de componentes parciales) y C63
  (V-CON-6 solo en parte: el dependiente de un soporte locus hereda el arco como dominio declarado,
  sin componente certificada, porque el soporte locus está fuera de la clase v3). La resolución
  autoral previa a T12 restaura C63 y las conformidades de H10 y registra las demás (§7).

## 7. Resolución autoral previa a T12 (2026-10-22)

La resolución del autor «Resolución autoral de las decisiones previas a T12» decide H10 (opción A),
V-CON-6 (opción 2), la corrección de la componente puntual y el fallo cerrado de los tokens
históricos. Esta sección registra su efecto en las conformidades; la ejecución consta en el informe
del paso.

### 7.1 H10: estimación condicionada (opción A)

Contrato: `locus-v2-metrics.md` §26.4 (enmendado) y §26.5 (nuevo); [EXI] §15.8; [CON] §3 y §4,
precisión H10. La longitud total de un locus `NOT_ESTABLISHED` solo por `OPERATION_OUTSIDE_CLASS` es
una estimación de clase 3 sobre sus piezas declaradas: sin envolvente rigurosa, con las hipótesis no
certificadas de existencia y continuidad y con el diagnóstico `EXISTENCE_NOT_CERTIFIED`. La estructura
de existencia no cambia. Los valores públicos de H10 coinciden bit a bit con T10
(`0x1.0c0cb3709e5a9p2`, `0x1.0394199c9344fp3` y `0x1.010912bcc8705p4`, con los mismos errores
estimados).

| Conformidad | Estado | Aserción restaurada |
|---|---|---|
| C57 `sineArcLengthsAreHonestEstimates` | Restaurada | `assertCoherentEstimate` del seno público k=2 (valor y error frente a Simpson) |
| C58 `adversarialCurvesNeverReceiveACertifiedClass` | Restaurada | `assertNotCertified` del aliasing y de `floor` públicos |
| C60 `affineImagesAreStructural` | Restaurada | `(2w+1,3w-1)`: `ESTIMATED`, sin envolvente, 4√13 dentro del error y admisible |
| C62 `scalarAdapterPublishesExactlyTheAdmissibleRichValues` | Restaurada | `b=Length(Lt)` de `(t,sin t)` definido e igual al valor rico `ESTIMATED` |
| C35 `m04RichTotalLengthIsNonnumericAuthority` | Restaurada (la elipse) | elipse escalar periódica `ESTIMATED`, 15,86543958929059 ± 2E-6; la circunferencia conserva el cambio de G6 |

Las cinco comprueban además la ausencia de autoridad certificadora con
`assertConditionedWithoutAuthority` (`G9U0PublicSurfaceTestBase`):

- la clase es `ESTIMATED` o un fallo tipado, nunca certificada, y no hay envolvente rigurosa;
- un valor lleva la hipótesis y el diagnóstico explícitos;
- la estructura de existencia sigue `NOT_ESTABLISHED`, sin componentes ni puntos, con cobertura solo
  `OPERATION_OUTSIDE_CLASS` o complemento declarado.

`assertNoTotalWithoutExistence` se conserva para las causas que mantienen el rechazo. C59 y C61
conservan su cambio de fixture, que no retiró aserciones; las fuentes oscilantes vuelven a cubrirse en
C57 y en H10-5. La H10 de `CU0` (`PreG9BCu0SyntheticCharacterizationTest`) pasa sin modificación
(V-CU0-2).

Pruebas nuevas en `PreG9BCu2ConditionedEstimateTest` (compartida):

- H10-1 `aSineOutsideClassV3IsEstimatedWithoutAnyCertifyingAuthority`: valor, error y escalar. La
  firma de la estructura no cambia, la intersección no tiene raíces ni completitud y `Point(L, …)`
  queda indefinido. Un locus que lee el escalar queda fuera de clase (`metric-value-constant`); el
  mismo con una constante es `COMPLETE`.
- H10-2 `onlyAConstructionOutsideTheClassIsEligible`: siete causas distintas, la no existencia
  certificada interior y un certificado parcial mantienen el rechazo; las piezas declaradas suman sin
  el complemento.
- H10-3 `aWholePeriodClosesOnlyUnderARecordedHypothesis`: la elipse con la hipótesis de costura; una
  curva periódica que no cierra falla y su escalar queda indefinido.
- H10-4 `anExcludedDeclaredEndKeepsItsTypedStatus`: `LIMIT_NOT_ESTABLISHED`.
- H10-5 `similarityImagesOfAConditionedEstimateStayConditioned`: traslación y dilatación.

### 7.2 V-CON-6: composición certificada mínima (opción 2)

Contrato: [CCM] §4 y §10.10 (nuevo); [EXI] §15.7; [CON] §3 y §4, precisión V-CON-6. C63 queda
**restaurada**: `aDependentOnALocusSupportInheritsTheArc` vuelve a afirmar la herencia del arco:

- una componente que cruza la costura, con el intervalo, el inicio, el fin, los extremos y la
  maximalidad del soporte, sin cierre y con una naturaleza que certifica;
- las mismas claves de linaje y la completitud del soporte;
- su no existencia como `DEPENDENCY_CERTIFIED_NONEXISTENT` y puntos que son los del soporte
  trasladados;
- la misma composición tras un giro y una homotecia.

La prueba nueva `aDeclaredDomainIsNeverPromotedWithoutATotalComposition` comprueba que un paso no
total (una recta por el punto generador) y la lectura del motor fuera del punto generador
(`driver-outside-generator-point`) dejan el dependiente `NOT_ESTABLISHED`, sin componentes ni
existencia certificada.

### 7.3 Componente puntual (corrección de `restrictedTo`)

Defecto: al restringir un certificado a las piezas declaradas, un parámetro aislado declarado dentro
de una componente certificada dejaba cobertura `CERTIFIED_EXISTS` en `[a,a]` sin componente. Ahora es
una componente puntual (fuente `declared-isolated-parameter`, maximalidad y naturaleza de la
componente) con cobertura `CERTIFIED_ISOLATED_POINTS` solo en ese parámetro. Dos puntos con la misma
fuente se distinguen por la clave de su envolvente, como las componentes de [EXI] §15.6. No se
materializa ningún objeto punto. Pruebas:

- `aDeclaredIsolatedParameterOfCertifiedExistenceIsAPointComponent` y
  `declaredIsolatedParametersNeverInventExistence` (`PreG9BCu2ExistenceStructureV2Test`): sin punto
  en una no existencia o en una banda sin resolver, dos puntos con claves distintas, el contacto en un
  extremo cerrado y la desaparición como transición tipada;
- `aDeclaredIsolatedParameterIsAPublishedPointComponent` (`PreG9BCu2ConsumerMigrationTest`): el punto
  no se direcciona con un binary64, la longitud total es 2 sin aporte suyo, la intersección no es
  exhaustiva y no aparece ningún objeto punto.

### 7.4 Limitaciones registradas (decisión 4: fallo cerrado)

Ninguna se resuelve por coordenadas, índice, proximidad ni mera contención. Las identidades
persistidas se conservan, los tokens quedan latentes y los diagnósticos son tipados.

| Conformidad | Limitación |
|---|---|
| C09 | Las exportaciones DXF de loci del generador escalar fuera de clase pierden la identidad de bytes con la base: el locus sigue `NOT_ESTABLISHED` (H10 no cambia la existencia) y la exportación lo declara `EXISTENCE_NOT_ESTABLISHED` |
| C22, C23 | La materialización de la raíz en un parámetro aislado (`ISOLATED_COMPONENT`) queda para `CU3` (D-CU2-19): la componente puntual se declara y ninguna intersección la excluye ni la materializa |
| C51, C70 | Un documento anterior a `CU2` con un selector o token de una componente parcial (clave binary64) no se revincula: su clave difiere de la estructural y no hay correspondencia estructural demostrada, así que queda latente con fallo cerrado. La clave de todo el dominio sí resuelve (E-3) |

Evidencia de la sonda de compatibilidad (documento escrito en T10 y leído en el árbol actual):

- las identidades persistidas se conservan y `Pp` y `Pw` revinculan;
- `Xw` (todo el dominio) resuelve y `Xp` (componente parcial) queda indefinido;
- `Li` publica ahora su componente puntual en 0, y `Ri` sigue `UNRESOLVED` sin raíces, con `Xi`
  latente;
- el locus escalar fuera de clase sigue `NOT_ESTABLISHED`, con `Po`, `Ro` y `Xo` sin resolver.
