# PRE-G9B-MAINT-P0 — A. Informe de caracterización

| Campo | Valor |
|---|---|
| Carácter | **Evidencia de planificación.** No aprueba nada ni es autoridad normativa. `selfApproved=false`. Un hecho observado no es una decisión |
| Estado | `CHARACTERIZATION — PENDING AUTHOR REVIEW` (revisión 2) |
| Encargo | Instrucción autoral «GeoCeDG — PRE-G9B-MAINT-P0» y enmiendas autorales finales del expediente (ambas del 2026-11-23): planificación, caracterización técnica y diseño preliminar; sin implementación productiva |
| Base | `main` = `origin/main` = `f80f0e9b029e678007f399508163bf794951992c`, árbol `8c1d4ca163b214898ec392bd9d02f04c955661d1`; etiqueta `v1.2.2` (objeto `7a8e12b8f`) sobre `1b54ff95d`; árbol de trabajo limpio al empezar |
| Método | Lectura del código, las pruebas y los documentos en esa base; dos sondas de caracterización de usar y tirar (§F), ejecutadas desde el directorio temporal de la sesión con un script de inicialización de Gradle y sin versionar. Ninguna prueba, selección ni artefacto del repositorio se ha modificado |
| Expediente | [portada](pre_g9b_maint_p0_dossier.md) |

Marcas: **[C]** verificado en código o prueba (ruta:línea en la base); **[D]** tomado de un
documento (se indica su estado cuando importa); **[S]** observado en una sonda de esta fase;
**[I]** inferencia, pendiente de confirmación. Una inferencia por traza de código no es
confirmación empírica.

Registro de revisiones: la revisión 1 (2026-11-23) es la versión inicial. La revisión 2
(2026-11-23) incorpora las enmiendas autorales finales: hechos de partida para la importación
DXF y NURBS (§A.8), la lectura de P-1 respecto de la hipótesis H3 (§B.5), la decisión pendiente
sobre el subtotal (§B.1), la caracterización que falta antes de I2G0 (§C.4) y los nuevos
solapamientos (§E). Las sondas y sus resultados (§F) no cambian.

## 0. Estado de gobierno

- El roadmap no registra ninguna fase `PRE-G9B-MAINT`; su último registro dice que 1.2.2
  «no autoriza ni inicia ninguna fase» y que las limitaciones residuales quedan como deuda
  de mantenimiento posterior [D, roadmap, registro del 2026-11-23]. La autorización de esta
  planificación existe solo en la instrucción autoral; su registro en el roadmap es una
  decisión de la [hoja F](pre_g9b_maint_p0_author_decision_sheet.md) (D-0).
- `G9B` está `DESIGNED — NOT AUTHORIZED`; «ninguna puerta de producto está autorizada» [D,
  roadmap, cabecera].
- El registro de autorización de la publicación de 1.2.2, §4, declaró no autorizado
  «volver a optimizar D-V6, `Intersect` o la longitud total condicionada; modificar las
  políticas normativas de Locus V2; cambiar la serialización, la identidad o el DAG» [D].
  Toda corrección de los bloques B y C necesita, por tanto, autorización nueva.
- Política de verificación vigente: verificación ordinaria `CORE + FOCUSED`
  (`INTEGRATION` + `PHASE`); `FINAL` solo con autorización expresa
  [D, `verification-levels.md`, «Exhaustive campaigns and the prospective FINAL obligation»].
  El perfil `DEV` está declarado incompleto en el registro; la verificación focal aceptable
  es una `PHASE` registrada.

## A. B-splines, SplineV2 e interoperabilidad

### A.1 Definición vigente de SplineV2

| Aspecto | Hecho | Marca y fuente |
|---|---|---|
| Familia | Ecuaciones de `Spline` Classic como padre semántico de Locus V2; geometría igual a Classic a `1e-8` | [C] `SplinePolynomialModel2D.java:15-23`; `G9S1SemanticSpline2DTest` s28 (610-654) |
| Contrato | «floating polynomial interpolation family, not a NURBS, control-point B-spline or arbitrary fitting contract» | [D] `semantic-spline-2d.md` §1, `NORMATIVE — PASS — AUTHOR APPROVED` |
| Admisión | 3–32 puntos 2D finitos; grado entero `3 ≤ d ≤ min(N, 12)`; `(N-1)(d+1) ≤ 512` | [C] `SplinePolynomialModel2D.java:25-29, 71-80, 156-163`; `AlgoSplineV2.java:69-72` |
| Parametrización | `k_i = c_i / c_{N-1}`, `c_i = c_{i-1} + w(P_i − P_{i-1})`, `w = Math.hypot` por defecto (cuerda); una función de peso la sustituye | [C] `SplinePolynomialModel2D.java:102-117, 281-296` |
| Nudos | Estrictamente crecientes; un tramo nulo deja la spline indefinida; nunca hay nudos repetidos | [C] `SplinePolynomialModel2D.java:108-117` |
| Dominio | `[0, 1]` cerrado si es abierta, `[0, 1)` si es periódica; orientación = orden de entrada; nudo interior del tramo derecho | [C] `AlgoSplineV2.java:87-92`; `SplinePolynomialModel2D.java:191-204` |
| Ecuaciones | Interpolación, continuidad de derivadas `1..d-1` en nudos interiores y `d − 1` filas finales: periódicas si es cerrada; si es abierta, `S_0^{(d-1)}(0) = 0`, `S_{M-1}^{(d-1)}(1) = 0` y filas heredadas que evalúan un tramo fuera de su intervalo | [C] `SplinePolynomialModel2D.java:298-357` |
| Interpretación | `d = 3`: spline cúbica natural en parametrización por cuerda; `d ≥ 4`: condiciones no estándar, que ADR 0022 prohíbe sustituir; con `d ≥ 4` invertir el orden de los puntos puede cambiar la curva | [I] para la interpretación; [D] ADR 0022, decisión 2 |
| Interpolación | Numérica, error regresivo relativo `≤ 1e-9` | [C] `SplinePolynomialModel2D.java:478-496` |
| Continuidad | Igualdad exacta de chorros `0..d-1` de la función representada en cada nudo interior y en la costura periódica, sobre numeradores exactos (`BigDecimal`) con escala de precisión `spline-structural-precision/v1` | [C] `SplineStructuralModel2D.java:44-58, 99-134, 306-345`; `SplinePolynomialModel2D.java:258-261` |
| No garantiza | Continuidad geométrica, regularidad (solo `SINGULAR` si la velocidad flotante es exactamente 0) ni interpolación exacta | [C] `SplineSemanticEvaluator2D.java:59-70`; [D] ADR 0022, decisión 4 |
| Cierre | Solo con igualdad exacta binary64 del primer y el último punto; igualdad aproximada → indefinida, sin promediar | [C] `SplinePolynomialModel2D.java:118-123`; `G9S1R1StructuralSplineContinuityTest` |
| Identidad | Un `GeoLocusV2` nuevo con `PersistentGeoId`; rama `spline-v2/main`; una componente; argumentos sin etiqueta promovidos a objetos con identidad propia | [C] `AlgoSplineV2.java:38`; `LocusV2PublicOperations.java:103-135, 1432-1477` |
| Persistencia | `<command name="SplineV2">` con sus entradas; sin atributo de familia ni de versión; las firmas versionadas (`semantic-spline-polynomial/v2`, `spline-v2-evaluator/v2`) no se serializan | [C] `GeoLocusV2.java:507-519`; fixture `literal-pieces-pair-written-by-9ccefdcf.xml:139-151` |
| Calidad | `SEMANTICALLY_CONSTRUCTED`, `ANALYTIC_EVALUATION`, `FLOATING_POINT_UNCERTIFIED`; existencia probada por el proveedor, `spline-v2-interpolation/v1` | [C] `AlgoSplineV2.java:43, 93-112`; `LocusExistenceStructure2D.java:751, 1576-1582` |

### A.2 Representación B-spline y DXF de SplineV2

- No existe base B-spline, red de control ni forma de Bézier como autoridad; una conversión
  flotante a Bernstein existe solo dentro de la capacidad de pares, para el rechazo por
  envolvente convexa [C] `PiecewisePolynomialPairIntersectionCapability2D.java:39, 199-207, 653-694`.
- Matemáticamente, una SplineV2 abierta de grado `d` es una B-spline anclada no uniforme con
  nudos `[0^{d+1}, k_1, …, k_{N-2}, 1^{d+1}]` y `N + d − 1` puntos de control; una cerrada es
  periódica. Esos puntos de control son racionales pero en general no representables en
  binary64 [I].
- DXF: una `LWPOLYLINE` aproximada por componente certificada (`APPROXIMATE`,
  `ORIENTED_DYADIC_REFINEMENT`, `ESTIMATED_ERROR`) [C] `G9X1GeometryExportAdapter.java:291-301`;
  `AdaptiveCurveApproximationBuilder2D.java:258-259`; prueba Desktop
  `PreG9BS1AuthorDxfReproductionTest.java:112-132`.

### A.3 Superficie genérica y puntos ligados a SplineV2

Detallada en la [propuesta B](../architecture/pre_g9b_maint_p0_bspline_proposal.md), §1.3.
Hechos clave:

- Genérico sobre `LocusEvaluator2D`, `LocusDifferentialEvaluator2D` y
  `PiecewisePolynomialLocus2D`: métricas, intersecciones unilaterales (grado compuesto
  máximo 96), pares ricos, punto sobre la curva, semejanzas, dibujo, exportación, persistencia
  [C] `PolynomialTargetIntersectionCapability2D.java:46-70`;
  `PiecewisePolynomialPairIntersectionCapability2D.java:58-60`;
  `LocusSimilarityEvaluator2D.java:21-43`.
- `PiecewisePolynomialLocus2D` exige coeficientes en potencias del parámetro canónico global
  y no admite tramos racionales [C] `PiecewisePolynomialLocus2D.java:8-16, 39-44`.
- Ligado a SplineV2: modelo certificado (`CertifiedIntervalCurveModel2D.capture`, privado
  de paquete), tokens D2 de pares, certificación de objetivos implícitos, certificador
  D-CU2-9, extremos de longitud en puntos definidores, cableado de producto
  [C] `CertifiedIntervalCurveModel2D.java:20, 49-64`;
  `SplineImplicitIntervalCertification2D.java:110-127`;
  `SelectedRootContinuousDomainCertifier2D.java:466-470`;
  `SplineConstructorOccurrenceResolver2D.java:65-88`; [D]
  `spline-v2-pair-materialization.md` §1, §10.

### A.4 Exportación DXF vigente

- `DxfExporter` escribe ASCII `AC1015` con `POINT`, `LINE`, `RAY`, `XLINE`, `CIRCLE`, `ARC`,
  `ELLIPSE` y `LWPOLYLINE`; otra entidad lanza error; números con `Double.toString`
  [C] `DxfExporter.java:332-389, 507-516`.
- Exactas: punto, segmento, rayo, recta, circunferencia, arco, elipse y arco elíptico,
  polígono y polilínea [C] `GeoElementGeometryExportAdapter.java:179-241`.
- Aproximadas con dominio explícito: parábola, hipérbola, función y curva paramétrica
  (incluidas `Spline` y `BezierCurve` Classic) [C] `G9X1GeometryExportAdapter.java:476-534`.
- Locus V2: una polilínea por componente certificada, sin unir huecos; componentes
  puntuales declaradas; regiones condicionadas omitidas con diagnóstico desde 1.2.2
  [D] `dxf-curve-fidelity-and-approximation.md` §10-§11.
- Sin bulges ni `SPLINE` [C] `DxfExporter.java:468-478`. Tolerancia explícita en unidades del
  modelo (Desktop `0.001`), límites de trabajo y manifiesto lateral obligatorio
  (`org.geocedg.dxf.fidelity-manifest` v3) [C] `GeoCeDGDxfExportController.java:61, 362`;
  `GeometryExportRequest.java:26-34`; `DxfFidelityManifestWriter.java:41-55, 335-403`.
- No hay importación DXF en ningún código principal [C].
- Reservas: ADR 0014, decisión 8; `dxf-curve-fidelity-and-approximation.md` §4 y §9 (X1C);
  nota de `PRE-G9B-S1` en el roadmap; condiciones D-V4 del 2026-11-21 [D].

### A.5 Herencia upstream y nombres

- `Spline` Classic: `GeoCurveCartesian` de expresión, inadecuada como autoridad (ADR 0018 ya
  la rechazó); `BezierCurve`: una cúbica de 4 puntos con orientación invertida
  (`t = 0` da el último punto); `PenStrokeBezier`: tinta. No existe código de B-spline,
  NURBS ni de Boor [C] `AlgoSpline.java`, `AlgoBezierCurve.java:41-85`.
- Libres: `BSpline`, `BSplineV2`, `Nurbs`. Ocupados: `Spline`, `SplineV2`, `BezierCurve`,
  `PenStroke`, `PenStrokeBezier`, `Curve`, `CurveCartesian` [C] `Commands.java:179-180, 363-367`.
  Un comando nuevo exige regenerar el corpus de huellas R5-B [C] corpus
  `pre-g9b-r5-b-base-fingerprints.json`.

### A.6 Higiene documental

Cabeceras de estado desactualizadas respecto del roadmap o de los registros de cierre: ADR
0022 y `g9s1_r1_structural_spline_continuity.md` (dicen candidato pendiente; el cierre
`g9s1-r1-author-closeout.json` registra `PASS_AUTHOR_APPROVED`),
`spline-v2-and-ordered-list-authoring.md` (candidato; R4 está aprobada), fila «Phase» de
`dxf-curve-fidelity-and-approximation.md` (G9X1 está aprobada) y
`pre_g9b_stabilization_deployability_design.md` §2.2 (el código devuelve ya `SplineV2` como
tipo) [D]/[C]. No se corrigen en P0: su conciliación es una tarea documental propia (D-0).

### A.7 Pruebas que fijan SplineV2

Compartidas: `G9S1SemanticSpline2DTest`, `G9S1R1StructuralSplineContinuityTest`,
`G9S1R1SplinePrecisionAdmissionTest`, `G9S1R1SplinePairMaterializationTest` (las cuatro en
CORE), `G9S1R1StructuralSplineLifecycleTest`, `G9S1R1NativeStructuralPairTest`,
`G9S1R1SplinePairIntervalCertificationTest`, `G9S1R1StructuralImplicitCertificationTest`,
`PostG9U1A1SplineConstructorProvenanceTest`, `PostG9U1A3CompatibleV2RedefineTest`,
`PostR1DeclaredPieceIdentityTest`, `G9X1LocusV2ExportTest`. Desktop:
`PreG9BR4SplineV2AuthoringTest`, `G9S1R1NativeArchivePersistenceTest`,
`PreG9BS1AuthorDxfReproductionTest`, `PreG9BR6PlusCLatexExportTest`. Escenarios congelados en
`geocedg/validation/g9s1`, `g9s1-r1` y `export/g9x1`.

### A.8 Punto de partida para la importación DXF y NURBS

| Aspecto | Hecho | Marca |
|---|---|---|
| Lector DXF | No existe en ningún código principal; tampoco un modelo de geometría importada | [C] (§A.4) |
| Evaluación B-spline o NURBS | No existe código de de Boor, de B-spline ni de NURBS; `PiecewisePolynomialLocus2D` no admite tramos racionales | [C] (§A.3, §A.5) |
| Colocación | Un adaptador de importación pertenece a la capa externa al núcleo; el núcleo solo cuando la capacidad participa en el DAG, en la identidad o en la serialización | [D] AGENTS §4 |
| Condiciones del autor | Importación de solo lectura; una representación de intercambio nunca sustituye en silencio al objeto CeDG autoritativo; procedencia y tolerancias conservadas | [D] roadmap, registro del 2026-11-21 (D-V4) |
| Entidad `SPLINE` | Banderas (70: 1 cerrada, 2 periódica, 4 racional, 8 plana), grado (71), recuentos de nudos, puntos de control y puntos de ajuste (72, 73, 74), nudos (40), pesos (41), puntos de control (10/20/30), puntos de ajuste (11/21/31), tolerancias (42-44), tangentes de extremo (12/13) | [D] referencia DXF pública del formato, no del repositorio |
| Documento `.cedg` | No está caracterizado cómo trataría 1.2.2 una entrada desconocida del archivo del documento ni dónde guardaría datos no geométricos de referencia | pendiente (`MAINT-A-N1`) |
| Consumidores de Locus V2 | No está caracterizado, consumidor por consumidor, cuáles dependen solo de `LocusEvaluator2D` y `LocusDifferentialEvaluator2D` y cuáles de `PiecewisePolynomialLocus2D` | [I], pendiente (`MAINT-A-N1`) |

## B. Limitaciones residuales de 1.2.2

La lista aprobada está en `geocedg_1_2_2_closing_evidence.md` §5: siete entradas (B1–B5,
D-V6 y el fallo histórico de Desktop
`PreG9BCu2AuthorModelConsumerTest.theCertificateIsTheSingleClosureAuthority`) [D].

### B.1 Longitud total condicionada

| Aspecto | Hecho | Marca |
|---|---|---|
| Reproducción | Arnés `tools/benchmark/post-r1-locus/run-harness.ps1 -Scenario intersect -Configuration total`; prueba Desktop `PostR1LengthDeletionDesktopTest.java:45-111` (escalar indefinido, trabajo mayor con total, vuelta a la base tras borrar) | [C] |
| Coste medido por consumidor | Arnés: 7 622 ms frente a 1 117 ms de mediana (≈ +6,5 s), 5 722 evaluaciones de `w_2` por cambio frente a 16; ejecutable empaquetado: ≈ +4,6 s | [D] `post_r1_locus_real_change_performance.md` §11.2, §11.5; informe final §3-§4 |
| Dónde | 95 % en construir el estado de estimación de cada región completa; ≈ 2 850 evaluaciones por región, concentradas junto a los extremos de región, donde el lugar deja de existir | [D] rendimiento §11.4 |
| Causa | `LocusMetricEngine2D.conditionedTotal` construye un estimador por región en cada revisión; la clave del índice incluye la revisión, así que no hay reutilización entre revisiones (además prohibida) | [C] `LocusMetricEngine2D.java:83-192, 611-619`; [D] métricas §28.2 |
| Escalar indefinido | `isScalarAdmissible` exige cobertura completa, recorrido P2 o ruta condicionada vigente, y la ruta se niega para un subtotal | [C] `LocusMetricResult2D.java:161-192, 225-233` |
| Por qué no puede ser número | Las bandas no ausentes no tienen contribución ni cota; en un extremo de tipo pliegue la longitud perdida escala como `O(√δ)`, no controlada por la tolerancia | [D] métricas §28.2-§28.3; contrato §5.3; [I] para la escala |
| Trabajo redundante | Ninguno dentro del contrato: la evidencia no se recorre de nuevo y el memo anidado solo abarata cada evaluación. El recuento ≈ 5 700 lo fija §28.2 («the rich result carries the value») | [C]/[D] |
| Valor del resultado rico | No consta en los registros (solo el escalar indefinido y el coste), ni si algún consumidor lo lee. Es dato previo a la decisión pendiente sobre el subtotal (D-B-1b) | pendiente |

### B.2 Huérfano tras redefinir un número de longitud

| Aspecto | Hecho | Marca |
|---|---|---|
| Fijado por | `PostR1PrivateMetricAuthorityTest.java:139-155` (`aRedefinedNumberKeepsTheRecordedLimitation`) | [C] |
| Causa | `AlgoLocusMetricScalarAdapter.remove()` no retira la autoridad si el número se está sustituyendo | [C] `AlgoLocusMetricScalarAdapter.java:94-107` |
| Por qué no se retira dentro de la redefinición | La guarda del registro de identidad rechaza publicar un cambio de grafo durante una redefinición en curso | [C] `SpatialIdentityRegistry.java:3088-3122, 3886-3898`; [D] evidencia de cierre §2 |
| Limpieza posterior | No existe barrido posterior a la transacción ni al cargar; los huérfanos de borrados anteriores a `781629cb1` también persisten | [C] (ningún otro llamador de `isPrivateScalarAuthority`) |
| Efectos | Se guarda y se recalcula en cada cambio (medido para el caso de borrado: 6,2–6,5 s tras reabrir); el caso de redefinición no se ha medido aislado | [D]/[I] |

### B.3 Coste de `Intersect` condicionado

| Aspecto | Hecho | Marca |
|---|---|---|
| Medido | Arnés: +0,75 s (1 863 frente a 1 117 ms); ejecutable: +0,55–0,62 s; 776 evaluaciones extra de `w_2` a ≈ 1,2 ms | [D] rendimiento §11.2-§11.5; evidencia de cierre §3 |
| Dominante | Aislamiento 73 %: rejilla uniforme de 256 subdivisiones por región (43 %) y búsqueda ternaria de mínimos, 2 evaluaciones por iteración (26 %) | [D] rendimiento §11.3; [C] `ExtendedTargetIntersectionCapability2D.java:44, 228-248, 391-433` |
| Repetición | No queda trabajo repetido dentro de la política de aislamiento vigente | [D] rendimiento §11.3 |
| Palancas | Sección áurea (≈ −130 evaluaciones, cambia los últimos bits); semillas desde la evidencia (≤ −514, puede cambiar raíces); suprimir la búsqueda de mínimos con cambio de signo (contradice AGENTS §11.5); coste por evaluación (O2-a, sin cambio normativo, estimado) | [D] rendimiento §9.2, §11.6 |

### B.4 Piezas gobernadas por deslizadores

| Aspecto | Hecho | Marca |
|---|---|---|
| Regla | Una pieza es una ranura posicional si su declaración llega a un objeto con nombre sin identidad duradera; un deslizador libre no la tiene | [C] `LocusDeclaredPieceIdentity.java:147-157`; `PostR1DeclaredPieceIdentityTest.java:396-399, 421-423` |
| Fijado por | `aPieceGovernedByNamedObjectsWithoutDurableIdentityStaysAPlaceOfItsList` | [C] `PostR1DeclaredPieceIdentityTest.java:167-201` |
| Semántica aprobada | El token D2 es una ranura: «no claim of physical continuation» | [D] `spline-v2-pair-materialization.md` §6; existencia §20.6 |
| Caso constructivo demostrado | Ninguno: el único defecto demostrado (escenario B, piezas literales) está corregido; ningún modelo de `models/` usa listas de dominio gobernadas por deslizadores | [D] registro de contradicción C2 §1-§2; [C] lectura de los `.cedg` de `models/` |
| Sin prueba | La reordenación de piezas de deslizadores al redefinir la lista | [C] |

### B.5 Saltos alineados con la cuerda

| Aspecto | Hecho | Marca |
|---|---|---|
| Criterio | `findingOf` solo mira la distancia perpendicular del punto medio (límite `1e-3`) y el giro entre semicuerdas (`0,35 rad`); ningún criterio compara las longitudes de las semicuerdas ni la cuerda con el paso | [C] `LocusConditionedTraversal2D.java:413-439, 611-627`; política `LocusConditionedTraversalPolicy2D.java:73-76` |
| Fijado por | `aJumpAlongTheChordLeavesNoTraceAndIsNotDetected` | [C] `PostR1ConditionedEvidenceTest.java:384-402` |
| **Consecuencia confirmada** | **Una ruta que cruza un salto alineado con la cuerda, dentro de una región, se admite como número `ESTIMATED` que incluye la longitud del salto.** | **[S] sonda P-1 (§F)** |
| Mecanismo | En modo de solo evaluador, sobre nodos alineados y monótonos todos los niveles de cuerdas coinciden, el error de Richardson y el de sondeo son cero y la hoja se acepta | [C] `LocusMetricEstimator2D.java:246-345`; [I] para el mecanismo exacto |
| Diferencia con el caso pinchado existente | Un salto transversal por debajo del límite (`9e-4`) no converge y no da número (`aRouteAcrossAJumpThatTheTraversalDoesNotSeeIsNoNumber`); el alineado sí da número | [C] `PostR1ConditionedMetricTest.java:230-254`; [S] |
| Lectura | P-1 es un incumplimiento de la hipótesis H3 (continuidad) no detectado: la hipótesis declarada es falsa y la garantía condicionada queda vacía. No es la mera ausencia de certificación de continuidad, que es el estado normal de toda región condicionada | [S]/[D] contrato de uso condicionado §4 |

### B.6 D-V6

Grafo solo: 605 ms (cumple). Grafo y evidencia que consume el dibujo: 1,10 s de mediana,
1,22 s p95 tras el memo C5. Ejecutable con el documento guardado: 835–990 ms. El autor no
exige un tiempo universal inferior a 1 s y no autorizó C6 para ese objetivo [D] rendimiento
§3, §8.3; evidencia de cierre §3; autorización de la intervención final §3.

## C. Mejoras pendientes de PRE-G9B-CU7

### C.1 U2

- Definición: decisión autoral del 2026-11-05: «diferir la simplificación de las entradas
  de la herramienta Punto. Conservar la implementación funcional R5 y registrar la mejora de
  interfaz como deuda no bloqueante» [D] `pre_g9b_cu7_r5_author_decision_record.md:10, 25`.
  Contenido: propuesta de cierre reducido, líneas 133-138, y hallazgo P3-U de la auditoría
  R5, líneas 154-167 [D].
- Restricciones vigentes: «Punto en objeto» se conserva (solo él coloca un punto en el
  interior de un polígono o una cónica rellena; mantenimiento de 1.2.0, decisión E) [D]/[C]
  `Maintenance120PointToolsDesktopTest`; la herramienta 134 (dirección explícita) es otra
  capacidad [D] auditoría R5 l.160.
- «U1» se cita pero no está definido en el repositorio [C].
- Inventario de entradas de creación (12, E1–E12): todas las entradas interactivas de Locus
  V2 pasan por la misma fachada del núcleo, `LocusPointCreation2D`, con cinco rutas
  (`R1_R6`, `R2_H1`, `INDIVIDUAL_FORWARD_ADDRESS_CREATION`, `REJECTED`,
  `CONDITIONED_PARAMETER_ADDRESS`) [C] `LocusPointCreation2D.java:25-32, 157-256, 357-413`;
  el adaptador Desktop replica el enumerado por nombre [C] `GeoCeDGReferencePlacement.java:18-22, 88`.
- No hay proveedores ni selección duplicados; la fragmentación es de interfaz [C]:
  - tres accesos al mismo flujo «L, luego Q»: conmutador «Desde referencia Q» de
    Punto/Punto en objeto (`GeoCeDGReferencePointTool.java:568-583`), herramienta 145
    (`EuclidianConstants.java:476`) y menú contextual (`GuiManagerGeoCeDG.java:228-281`);
  - el panel muestra conmutadores excluyentes según el modo («Desde referencia Q» frente a
    «Posición de clic», líneas 568-598);
  - el clic con Punto y «Posición de clic» en 145 solo difieren en el origen del radio y en
    si un resultado R1 único se confirma;
  - el diálogo «Elegir una posición» de la asistencia de extremos métricos
    (`GeoCeDGMetricEndpointAssistance.java:65-95`) muestra claves y parámetros en bruto,
    ignora «Permitir arrastre» y **no tiene prueba**;
  - la guía describe el mismo flujo en cuatro párrafos (§8.1 EN/ES) [D].
- Ctrl+clic o clic derecho con Punto sobre un Locus V2 van a la ruta heredada [C]
  `GeoCeDGEuclidianController.java:243`; [I] probablemente crea un punto libre; sin prueba.

### C.2 F8 (`LOCAL_COMPONENT_FORWARD_ADDRESS_CREATION`)

- Colisión de nombres: este F8 es el hallazgo 8 de la auditoría R3 de CU7; el «F8» de la
  línea posterior a R1 es la corrección de la copia de rebanada (`350b9d115`), ajena a este
  estudio [C]/[D].
- Definición: permiso nuevo que permitiría ofrecer y publicar una dirección explícita en una
  componente certificada (máxima o `LOCAL_CERTIFIED_SUBINTERVAL`) de una rama cuyo inventario
  global es `NOT_ESTABLISHED`, con siete condiciones [D]
  `pre_g9b_cu7_r3_partial_forward_permission_proposal.md` (propuesta nunca adoptada).
- Desactivación: por ausencia, no por bandera. El permiso individual existente exige que
  todas las ramas sean `COMPLETE`, y la búsqueda no inspecciona componentes de ramas no
  completas para crear un punto nuevo [C] `IndividualForwardPointCreation2D.java:199-228`;
  `LocusPointInteractionResolver2D.java:1426-1438, 1552-1557`.
- La exigencia `COMPLETE` es una restricción de alcance de producto, no una condición
  matemática: existencia §11 admite una dirección exacta dentro de una componente
  certificada y §15.8 reserva `COMPLETE` a los consumidores globales [D]
  `locus-v2-existence-components.md`; candidato normativo de creación general l.82, l.162;
  propuesta F8 l.106-108.
- 1.2.2 no cubre el caso: las regiones condicionadas se calculan solo sobre los parámetros
  que ningún certificado cubre [C] `AlgoDependentPointLocusV2.java:554-568`;
  `LocusConditionedTraversal2D.java:124`; [D] contrato de uso condicionado §1.
- Consecuencia trazada [I, por traza de código]: en un mismo locus, un clic sobre la parte
  condicionada (evidencia más débil) crea un punto y un clic sobre una componente local
  certificada (evidencia más fuerte) se rechaza con «Componente no establecida». Ninguna
  prueba cubre un locus mixto.
- Equivalente existente: `Point(L, u)` y `Point(L, "rama", u)`, la herramienta 134 y, bajo
  autoridad constructiva, `Point(L, D)` crean ya el punto en ese caso [C]
  `PreG9BCu2ConsumerMigrationTest.thePrincipalSelectorUsesTheLocalCertifiedComponents`
  (548-565); [S] sonda P-2.
- La sonda P-2 no pudo confirmar el rechazo del clic: el locus sintético de las pruebas se
  rechaza también con inventario `COMPLETE` (`UNSUPPORTED_CAPTURE`), porque la ruta de clic
  exige la captura de un productor real [S]. La confirmación necesita un locus mixto real.
- Estado de la evidencia sobre F8: la conducta del clic sobre una componente certificada local
  es una inferencia por traza [I]; ni la traza ni P-2 son confirmación empírica. Hasta una
  prueba discriminante (`MAINT-F8-S0`), F8 no tiene una carencia demostrada.

### C.3 M09 y sustitución explícita

- M09 aprobado (contrato prospectivo, 2026-10-29) separa identidad duradera de `P`,
  definición y token de raíz; solo admite una actualización cerrada que conserve la
  identidad y excluye expresamente reasociar dependientes a un punto nuevo (M09-B) [D]
  `cu7-m09-point-redefinition.md` §1-§3; ADR 0041, decisión 8.
- Bloqueo: las puertas son del camino de sustitución del anfitrión, no de la conservación
  de identidad [D] `pre_g9b_cu7_integrated_m09_evidence.md` §4; [C]:
  - la reversión recarga XML y recrea los objetos Java (`Construction:3019-3049`);
  - las vistas observan y escriben a mitad de operación (P07);
  - un punto de Undo almacenado antes del fallo no se revierte (P09);
  - la vigencia compara XML completo y no ve un ABA silencioso (`SpatialIdentityRegistry:5416-5420`);
  - un objetivo sin identidad no tiene contexto de entrada (`SpatialIdentityRegistry:3590-3594`).
- **La sustitución con identidad nueva ya existe** para objetivos con identidad duradera y
  productor de salida única: modo `LEGACY_REPLACEMENT` del ADR 0026, con informe de impacto
  tipado, confirmación explícita en Desktop y un único paso de Undo [C]
  `ConstructionGeoRedefineProvider.java:169-192`; `SpatialIdentityRegistry.java:5181-5204, 5601-5640`;
  `GeoCeDGSpatialRedefineFrontend.java:65-129, 202-228`; prueba
  `PreG9bR3DurableContractRedefineTest.java:44-74, 183-223`.
- Sus límites [C]:
  - **retira y elimina todo el cierre duradero dependiente** (`Construction:2364-2420`), incluido un Locus V2 dependiente;
  - su informe anuncia esos dependientes como `BECOMES_NONCURRENT` aunque se eliminan, y omite la cascada ordinaria, como ya señaló CU0 [D] `pre_g9b_cu0_redefinition_and_canvas_picking_design.md` §1.2;
  - no está disponible para objetivos sin identidad ni para productores múltiples.
- Reasociación de dependientes: no existe API para recablear entradas; el anfitrión lo hace
  por reescritura de XML y reconstrucción completa, con la etiqueta como asa del analizador
  [C] `Construction:2214-2271, 3306-3350`. Con un `P'` existente ya etiquetado, la ruta
  nativa fusiona `P` en `P'`; esa ruta no está caracterizada en GeoCeDG [C]/[I].
- Los ciclos se rechazan antes de mutar [C] `SpatialIdentityRegistry:5118-5123, 5445-5458`;
  prueba `PostG9U1A3R1RedefineAssessmentTest.java:68-73`.

### C.4 I2G (productores con varias salidas)

- No existe operación segura para separar una salida: `AlgoIntersectLineConic` mantiene
  matrices fijas, permutación, edad y estados de incidencia de todas sus salidas [C]
  `AlgoIntersectLineConic.java:65-96, 297-377`; [D] ADR 0041, decisión 7 («no fictional
  upstream detach API»).
- Comportamiento actual al redefinir una salida [C]:
  - todos sin identidad: los hermanos y sus dependientes se eliminan en silencio (`Construction:4011-4016`; `AlgoElement:877-885`);
  - objetivo con identidad: rechazo con causa engañosa (`STALE_ASSESSMENT` o rol duplicado);
  - **objetivo sin identidad con un hermano con identidad: el hermano y su registro se destruyen** (fijado por `G9A1SpatialIdentityRedefineHostTest.java:483-497`).
- Los productores de puntos propios de GeoCeDG tienen todos una sola salida; I2G afecta solo
  a productores heredados (recta-cónica, cónica-cónica, polinómicos, vértices de polígono
  regular, tangentes) [C]/[I].
- Tras un fallo de reconstrucción nativa, la instantánea de restauración se toma después de
  eliminar los hermanos [C] `Construction:2179-2216`; [D] CU0 §1.6; [I] los hermanos podrían
  quedar eliminados; sin sonda.
- Sin caracterizar todavía: la conducta Classic legítima de cada productor heredado de varias
  salidas cuando el grupo no contiene nada CeDG, que una barrera no debe cambiar, frente a los
  casos en que la redefinición destruye identidades, dependientes duraderos o reclamaciones
  CeDG. Es el primer paso de `MAINT-I2G0` (diseño D §4.3) [pendiente].
- Sin caracterizar todavía para la sustitución: las referencias por nombre fuera del DAG, las
  fronteras de la transacción de redefinición (notificación de vistas, almacenamiento
  asíncrono de Undo, arrendamiento de publicación) y lo que restaura exactamente la reversión
  del ADR 0026. Es `MAINT-S0` (diseño D §3.5) [pendiente].

## D. Otras deudas observadas (no forman parte de los cinco puntos)

| Id | Hecho | Fuente |
|---|---|---|
| OD-1 | Fallo histórico de Desktop `PreG9BCu2AuthorModelConsumerTest.theCertificateIsTheSingleClosureAuthority`, preexistente en `9ccefdcf` | [D] evidencia de cierre §5 |
| OD-2 | Tras redefinir una lista literal, el registro de tokens conserva la revisión anterior de la firma del proveedor hasta la siguiente lectura del documento | [D] informe final §5 |
| OD-3 | Informe de impacto de la sustitución legada inexacto (eliminados anunciados como `BECOMES_NONCURRENT`; cascada ordinaria ausente) | §C.3 |
| OD-4 | Destrucción silenciosa de un hermano con identidad (fijada por prueba) | §C.4 |
| OD-5 | Asistencia «Elegir una posición» sin prueba y con etiquetas en bruto | §C.1 |
| OD-6 | Cabeceras de estado desactualizadas | §A.6 |

## E. Solapamientos y carencias entre bloques

- **B2 y la sustitución M09 comparten el borde de la transacción de redefinición**
  (`completeSpatialRedefineOperation`, `Construction:2640-2646`) pero no sus invariantes: B2
  retira una autoridad privada inalcanzable cuyo único lector fue sustituido; la sustitución
  decide el destino de dependientes públicos. Se planifican por separado y en serie, para no
  editar a la vez el mismo borde.
- **B5 y B1 comparten la especificación de métricas §28**: una única decisión normativa puede
  cubrir ambas enmiendas, con incrementos de código separados.
- **U2 y F8 comparten el panel de Punto**: U2 primero (solo interfaz) y F8 después, si se
  autoriza (núcleo).
- **B-splines no depende de los bloques B ni C**; reutiliza SplineV2 y Locus V2 sin tocarlos.
- **Importación de referencia, familia B-spline y aproximación** comparten un núcleo de
  evaluación matemático (propuesta B §5.3): la importación (`MAINT-A-DXF0`) y la familia
  (`MAINT-A2`) son independientes salvo por esa biblioteca común; la aproximación
  (`MAINT-A-DXF1`) necesita las dos.
- **O2-a (coste por evaluación)** beneficiaría a B1, B3 y D-V6 a la vez; es la única palanca
  de rendimiento sin cambio normativo.

## F. Sondas de caracterización de esta fase

Ejecutadas sobre la base exacta, desde el directorio temporal de la sesión, con
`gradlew.bat :shared:common-jre:test --tests <sonda> --init-script <directorio temporal>`. Las
fuentes de las sondas no están en el repositorio. Los resultados previos de
`build/test-results` se respaldaron y se restauraron, y las clases de las sondas se
eliminaron. No son evidencia de aceptación.

| Id | Pregunta | Montaje | Resultado | Lectura |
|---|---|---|---|---|
| P-1 | ¿Una ruta que cruza un salto alineado con la cuerda da un escalar admitido? | `L = LocusV2(Q, P)`, `P` en el segmento `[(0,0),(1,0)]`, recorrido entre `u = 0,7` y `u = 0,8`. Caso recta: `Q = (s, 2s)`, `s = x + floor(x + 0,25)` (salto `(1, 2)` en `0,75`). Caso arco: `Q = (x + j, e^x + j·e^{0,75})`, `j = 0,05·floor(x + 0,25)` | Recta: una región `[0, 1]`, `SUCCESS`, `ESTIMATED`, admitido, **2,459674775249769** (= √5 · 1,1); la longitud de las piezas continuas es √5 · 0,1 = 0,2236. Arco: una región, admitido, **0,35128942567868376** ≈ 0,2341 (arco) + 0,1171 (salto) | Escalar admitido erróneo bajo hipótesis H3 declarada pero falsa. 2 pruebas, 0 fallos (la sonda no afirma la respuesta, la imprime) |
| P-2 | ¿Un clic dentro de una componente `LOCAL_CERTIFIED_SUBINTERVAL` de un inventario `NOT_ESTABLISHED` crea un punto? | Locus analítico sintético (circunferencia unidad) con el fixture `localBand()` de `PreG9BCu2ConsumerMigrationTest` y, como control, la misma componente con inventario `COMPLETE`; clic en `t = 0,5` con radio `0,05` | Ambos: `REJECTED`, `UNSUPPORTED_CAPTURE`, 0 opciones. `Point(Ll, 0,5)` crea el punto (0,8776, 0,4794) | No discriminante: un locus sintético no tiene captura de productor; la confirmación de F8 caso C1 exige un locus mixto real |
