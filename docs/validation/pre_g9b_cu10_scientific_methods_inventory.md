# Inventario científico E1 y matriz de generalidad

Anexo de [SCIENTIFIC_METHODS_EDITORIAL_HANDOFF](SCIENTIFIC_METHODS_EDITORIAL_HANDOFF.md). Incluye los 62 identificadores históricos del [plan E1, §6](../architecture/pre_g9b_cu_scientific_methods_editorial_handoff_plan.md), sin renumerar. En cada ficha se distinguen la aplicabilidad matemática, la clase comprobada y la capacidad implementada. La evidencia de fase se conserva con su disposición autoral. Los códigos C y R remiten a los [ejemplos y la bibliografía](pre_g9b_cu10_scientific_examples_and_references.md). Los números 1–5 indican la [naturaleza de la evidencia](SCIENTIFIC_METHODS_EDITORIAL_HANDOFF.md#1-qué-prueba-cada-clase-de-evidencia), no el estado técnico.

La matriz reconcilia el estado histórico del plan con los cierres [CU1](pre_g9b_cu1_closeout_record.md), [CU2](pre_g9b_cu2_closeout_record.md), [CU3-A](pre_g9b_cu3_a_closeout_record.md), [CU3-B1](pre_g9b_cu3_b1_closeout_record.md), [CU3-B2](pre_g9b_cu3_b2_closeout_record.md) y [CU3 global](pre_g9b_cu3_global_closeout_record.md). Las trazas de sondeos y diseños sustituidos siguen en el plan E1. Las categorías describen familias de métodos.

## A. Fundamentos de geometría descriptiva computacional

### SM-A-1

**Objetos espaciales definidos por proyecciones: punto espacial desde vistas ortográficas por mínimos cuadrados (descomposición en valores singulares con umbral de rango) y validación del sistema de proyección (despliegue alrededor de la charnela y cambios de plano).** Resolver P·x=b en marcos ortográficos por mínimos cuadrados y rango SVD. La unicidad requiere rango tres.

**Clase y capacidad.** Piloto de punto G9A con umbral de rango, reconstrucción/reproyección y prueba de despliegue.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` del piloto G9A aprobado, experimental y desactivado por defecto. `DISEÑO` para las demás primitivas, fases G9B/G9C fuera de este cierre.

**Evidencia, ejemplo y límite.** 3. Piloto G9A; rango insuficiente o mal condicionado y restantes primitivas fuera del piloto. R01, R12, C1.

**Trazabilidad.** [`geocedg/specs/spatial/g9-spatial-projection-semantics.md`](../../geocedg/specs/spatial/g9-spatial-projection-semantics.md) 1.0 (`NORMATIVE / AUTHOR APPROVED`), §2.2 y §10; [ADR 0010](../adr/0010-role-gated-spatial-authority-and-durable-identity.md) y [ADR 0011](../adr/0011-g9-spatial-persistence-and-phase-gates.md). [`kernel/spatial/semantic/ProjectionDefinedPointEvaluator`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/semantic/ProjectionDefinedPointEvaluator.java), [`ProjectionSystemEvaluator`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/semantic/ProjectionSystemEvaluator.java); [`kernel/spatial/runtime/AlgoProjectionDefinedSpatialPoint`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/runtime/AlgoProjectionDefinedSpatialPoint.java); [`G9A2ProjectionDefinedPointEvaluatorTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/spatial/G9A2ProjectionDefinedPointEvaluatorTest.java), [`G9A2ProjectionSystemEvaluatorTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/spatial/G9A2ProjectionSystemEvaluatorTest.java), [`G9A3SpatialMutationLifecycleTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/spatial/G9A3SpatialMutationLifecycleTest.java); modelo [`models/regression/g9a2-spatial-point-pilot/`](../../models/regression/g9a2-spatial-point-pilot/).

### SM-A-2

**Identidad entre el objeto espacial, sus coordenadas intrínsecas de proyección y su colocación en un diagrama CeDG.** Distinguir identidad espacial, coordenada intrínseca y transformación al diagrama. El cambio de colocación conserva la asociación tipada.

**Clase y capacidad.** Marcos y colocación del piloto G9A, con persistencia y recomputación probadas.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (track G9A, aprobado; experimental).

**Evidencia, ejemplo y límite.** 1 para la distinción formal, 4 para la regresión. C1, C3; falta ampliación por tipos.

**Trazabilidad.** misma especificación: G9P-R1 separa las coordenadas intrínsecas de su colocación en el diagrama; [ADR 0010](../adr/0010-role-gated-spatial-authority-and-durable-identity.md). [`kernel/spatial/identity/ProjectionBindingRecord`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/identity/ProjectionBindingRecord.java), [`ProjectionDiagramMapRecord`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/identity/ProjectionDiagramMapRecord.java); [`kernel/spatial/semantic/DiagramMapDefinition`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/semantic/DiagramMapDefinition.java), [`ProjectionFrameDefinition`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/semantic/ProjectionFrameDefinition.java); [`G9A2SpatialSemanticRecordXmlTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/spatial/G9A2SpatialSemanticRecordXmlTest.java), [`G9A2SpatialSemanticLifecycleTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/spatial/G9A2SpatialSemanticLifecycleTest.java).

### SM-A-3

**Incidencia, pertenencia y dependencia constructiva; proyección durable de dependencias.** Proyectar un DAG durable preservando la procedencia de aristas y roles de salida identificables.

**Clase y capacidad.** Grafo heredado y proyección de PRE-G9B-R3. Incidencia requiere premisas de construcción y existencia.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA`: grafo de dependencias heredado y proyección durable de `PRE-G9B-R3` (aprobado).

**Evidencia, ejemplo y límite.** 1 para procedencia con hipótesis, 4 para persistencia. C1, C3, R21; fronteras no producen dependencias inferidas.

**Trazabilidad.** [`geocedg/specs/spatial/durable-dependency-projection.md`](../../geocedg/specs/spatial/durable-dependency-projection.md) 1.1; [ADR 0029](../adr/0029-versioned-durable-dependency-projection-and-lazy-migration.md); [`locus-v2-semantics.md`](../../geocedg/specs/locus/locus-v2-semantics.md), «Dependency graph, revisions and nested composition». [`kernel/spatial/identity/DurableDependencyProjection`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/identity/DurableDependencyProjection.java); [`PreG9bR3DurableDependencyProjectionTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9bR3DurableDependencyProjectionTest.java).

### SM-A-4

**Parametrización y continuidad en construcciones dinámicas: dominios del conductor, proveedores y ramas.** Definir F(t) en intervalos orientados y continuidad por componentes, con límites laterales en cada extremo.

**Clase y capacidad.** Locus V2 con dominio de conductor y certificado de componente CU2; parámetro ligado al conductor.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` de la parametrización de Locus V2 (aprobado); continuidad certificada por componente, con arcos y extremos certificados, en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado); la parametrización no cambia.

**Evidencia, ejemplo y límite.** 1/2 en clase v3 cuando el certificado decide. C2, C4, C5; discontinuidades y tramos fuera de clase.

**Trazabilidad.** [`locus-v2-semantics.md`](../../geocedg/specs/locus/locus-v2-semantics.md), «Driver-domain providers and parameters» y «Branch identity and lineage»; [ADR 0027](../adr/0027-locus-v2-existence-and-continuous-valid-components.md); [`locus-v2-existence-components.md`](../../geocedg/specs/locus/locus-v2-existence-components.md) §15.2 y §15.3. [`kernel/algos/AlgoLocusV2`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusV2.java), [`kernel/locus/LocusBranch2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusBranch2D.java), [`LocusExistenceStructure2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusExistenceStructure2D.java); [`kernel/locus/intersection/HybridExistenceCertifier2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/HybridExistenceCertifier2D.java); [`PreG9BS1R2ExistenceStructureTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BS1R2ExistenceStructureTest.java), [`PreG9BCu2ExistenceStructureV2Test`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BCu2ExistenceStructureV2Test.java), [`PreG9BCu2ConsumerMigrationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BCu2ConsumerMigrationTest.java).

### SM-A-5

**Geometría matemática, cálculo y representación como capas distintas.** Separar conjunto existente, evaluación disponible y representación con fidelidad declarada.

**Clase y capacidad.** CU2 conserva existencia bajo fallo de cálculo, y publica componentes y extremos según su autoridad.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado): existencia, evaluación y representación separadas (P5), forma límite de los extremos y dibujo por componentes certificadas; base del principio transversal 8 de §3.

**Evidencia, ejemplo y límite.** 1/2 para existencia certificada, 4 para el control de fallo V-POINT-4. C4, C5; extensión a representaciones futuras.

**Trazabilidad.** [diseño de certificación de `CU2`](../architecture/pre_g9b_cu2_hybrid_certification_design.md) §3.4; [ADR 0038](../adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md); [`locus-v2-existence-components.md`](../../geocedg/specs/locus/locus-v2-existence-components.md) §15.5; [`locus-v2-presentation.md`](../../geocedg/specs/locus/locus-v2-presentation.md); [`dxf-curve-fidelity-and-approximation.md`](../../geocedg/specs/export/dxf-curve-fidelity-and-approximation.md). [`kernel/locus/LocusDefinition2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusDefinition2D.java), [`LocusBoundaryImage2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusBoundaryImage2D.java); [`euclidian/draw/LocusRenderData2D`](../../source/shared/common/src/main/java/org/geocedg/common/euclidian/draw/LocusRenderData2D.java); [`PreG9BCu2RepresentationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BCu2RepresentationTest.java), [`PreG9BCu2AuthorModelConsumerTest`](../../source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BCu2AuthorModelConsumerTest.java) (Desktop).

### SM-A-6

**Intersección de superficies en el modelo del autor: cono y cilindro, penetración y mordedura, verdadera magnitud por giro y desarrollo.** Generar secciones de cono y cilindro y trasladar puntos mediante vistas, giro y verdadera magnitud.

**Clase y capacidad.** Ejemplo CU0 estudiado en CU2; sus clases admitidas de locus y métrica tienen implementación propia.

**Estado y decisión.** `INVESTIGACIÓN` de la construcción de superficies. `IMPLEMENTACIÓN VERIFICADA`, aprobada en CU2, de las capacidades de certificación de loci que usa dentro de su clase.

**Evidencia, ejemplo y límite.** 4 como construcción de superficies, 1/2 para loci certificados concretos. C4–C7; la certificación espacial general queda abierta.

**Trazabilidad.** [análisis del modelo del autor](../architecture/pre_g9b_cu2_author_model_certification_analysis.md) §2–§5. corpus [`models/regression/pre-g9b-cu/`](../../models/regression/pre-g9b-cu/); pruebas de caracterización `PreG9BCu0*` y de certificación `PreG9BCu2AuthorModel*` (Desktop).

## B. Geometría algebraica y certificación exacta

### SM-B-1

**Discriminante de la intersección recta–circunferencia y su factorización por el par de tangentes.** El discriminante al sustituir una recta en una circunferencia distingue dos raíces, doble o ninguna; como forma en los coeficientes de recta expresa tangencia.

**Clase y capacidad.** Factorización por el par de tangentes en las hipótesis del análisis de CU2.

**Estado y decisión.** `INVESTIGACIÓN`.

**Evidencia, ejemplo y límite.** 1 como derivación algebraica del modelo ideal y 4 para el sondeo. C4, R02; falta implementación autónoma y revisar la referencia histórica.

**Trazabilidad.** análisis de `CU2` §3.2 y §3.8. sondeos fuera del repositorio.

### SM-B-2

**Multiplicidad de raíces, tangencias y clasificación de las raíces de un predicado de validez.** La multiplicidad y los signos laterales distinguen frontera, contacto y raíz aislada de un predicado polinómico.

**Clase y capacidad.** Predicado de primer nivel en CU2, clasificando parámetros y componentes puntuales.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado) en los predicados de primer nivel; una raíz múltiple de otro predicado deja una banda `UNRESOLVED`.

**Evidencia, ejemplo y límite.** 1 en polinomio exacto. C4, C5, R13–R16; una raíz múltiple posterior deja UNRESOLVED.

**Trazabilidad.** diseño de `CU2` §3.5 y §4.6; A2.2-R; [`locus-v2-certified-construction-model.md`](../../geocedg/specs/locus/locus-v2-certified-construction-model.md) §10.6; [`locus-v2-existence-components.md`](../../geocedg/specs/locus/locus-v2-existence-components.md) §15.4. [`kernel/locus/exact/RealRootStructure1D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/exact/RealRootStructure1D.java); [`kernel/locus/intersection/HybridExistenceCertifier2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/HybridExistenceCertifier2D.java); [`kernel/locus/LocusSingularParameter2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusSingularParameter2D.java), [`LocusPointComponent2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusPointComponent2D.java); [`RealRootStructure1DTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/exact/RealRootStructure1DTest.java), [`CircleDriverRootStructure2DTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/exact/CircleDriverRootStructure2DTest.java), [`PreG9BCu2HybridCertificationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu2HybridCertificationTest.java).

### SM-B-3

**Aritmética racional exacta sobre las constantes binary64 capturadas.** Convertir binary64 finito en el racional diádico que codifica y operar exactamente.

**Clase y capacidad.** ExactRational2D en la configuración capturada CU2; entradas libres exactas bajo la política constructiva.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado; D-CU2-16).

**Evidencia, ejemplo y límite.** 1 sobre bits definidos. C4, C5, R16; representar un objeto derivado por sus bits exige justificación de autoridad.

**Trazabilidad.** diseño de `CU2` §4.6; [`locus-v2-certified-construction-model.md`](../../geocedg/specs/locus/locus-v2-certified-construction-model.md) §3 (constantes capturadas) y §10.6. [`kernel/locus/exact/ExactRational2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/exact/ExactRational2D.java); [`ExactRational2DTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/exact/ExactRational2DTest.java), [`PreG9BCu2AuthorModelCertificationTest`](../../source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BCu2AuthorModelCertificationTest.java) (Desktop).

### SM-B-4

**Polinomios en una variable, máximo común divisor y factorización libre de cuadrados.** MCD con P′ y descomposición libre de cuadrados recuperan multiplicidades en característica cero.

**Clase y capacidad.** Polinomios univariados racionales de CU2; sin factorización completa.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado): máximo común divisor y descomposición libre de cuadrados de Yun, sin factorización completa.

**Evidencia, ejemplo y límite.** 1. C5, R14, R16; polinomio nulo y explosión de coeficientes requieren tratamiento específico.

**Trazabilidad.** diseño de `CU2` §4.6. [`kernel/locus/exact/ExactPolynomial1D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/exact/ExactPolynomial1D.java), [`SquareFreeDecomposition1D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/exact/SquareFreeDecomposition1D.java); [`ExactPolynomial1DTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/exact/ExactPolynomial1DTest.java), [`SquareFreeDecomposition1DTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/exact/SquareFreeDecomposition1DTest.java).

### SM-B-5

**Sucesiones de Sturm y recuento certificado de raíces: certificado algebraico exacto de primer nivel.** Contar raíces reales por diferencia de variaciones de Sturm, con extremos y multiplicidades tratados separadamente.

**Clase y capacidad.** Primer nivel con conductor segmento o círculo en cartas racionales del contrato CU2.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado; D-CU2-16; `exact-univariate-sturm/v1`).

**Evidencia, ejemplo y límite.** 1. C5, R13–R16; seam y extremos de carta requieren cobertura; generalizar predicados de mayor profundidad es investigación.

**Trazabilidad.** diseño de `CU2` §4.6; A2.2-R; [ADR 0038](../adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md), decisión 7; [`locus-v2-certified-construction-model.md`](../../geocedg/specs/locus/locus-v2-certified-construction-model.md) §10.6. [`kernel/locus/exact/SturmSequence1D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/exact/SturmSequence1D.java), [`RealRootStructure1D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/exact/RealRootStructure1D.java), [`CircleDriverRootStructure2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/exact/CircleDriverRootStructure2D.java), [`SegmentDriverRootStructure2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/exact/SegmentDriverRootStructure2D.java); [`kernel/locus/intersection/FirstLevelPredicate2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/FirstLevelPredicate2D.java); [`SturmSequence1DTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/exact/SturmSequence1DTest.java), [`CircleDriverRootStructure2DTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/exact/CircleDriverRootStructure2DTest.java), [`PreG9BCu2HybridCertificationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu2HybridCertificationTest.java).

### SM-B-6

**Predicados de validez y transiciones topológicas.** Combinar predicados de validez certificados sobre el parámetro, ordenando autoridades y componentes.

**Clase y capacidad.** Certificador híbrido CU2 combina totalidad, álgebra e intervalos bajo política versionada.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado) sobre el orden de autoridades aprobado.

**Evidencia, ejemplo y límite.** 1/2 por decisión, 5 en bandas indecididas. C4, C5; una decisión de tolerancia heredada no cubre una transición.

**Trazabilidad.** [`locus-v2-existence-components.md`](../../geocedg/specs/locus/locus-v2-existence-components.md) 1.0 §7; diseño de `CU2` §4.3; A2.2-R; [`locus-v2-certified-construction-model.md`](../../geocedg/specs/locus/locus-v2-certified-construction-model.md) §10.4 y §10.5. [`kernel/locus/ConstructionValidityPredicate2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/ConstructionValidityPredicate2D.java); [`kernel/locus/intersection/ConstructionBoxModel2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/ConstructionBoxModel2D.java), [`HybridExistenceCertifier2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/HybridExistenceCertifier2D.java); desde T11, [`kernel/algos/AlgoDependentPointLocusV2`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoDependentPointLocusV2.java) publica el certificado híbrido y el de compatibilidad se retira; [`PreG9BCu2ClassV3ModelTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu2ClassV3ModelTest.java), [`PreG9BCu2HybridCertificationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu2HybridCertificationTest.java).

### SM-B-7

**Eliminación algebraica para predicados posteriores a una raíz cuadrada.** Eliminar radicales introduciendo variables y restricciones de signo para recuperar la rama real.

**Clase y capacidad.** Fuera del primer nivel exacto de CU2; análisis de una eventual eliminación.

**Estado y decisión.** `INVESTIGACIÓN`: laguna identificada, fuera de `CU2`.

**Evidencia, ejemplo y límite.** 5 para el producto. C5, R16–R17; cuadrar introduce soluciones espurias y costes de grado.

**Trazabilidad.** diseño de `CU2` §4.6, «Límites». Sin código productivo propio.

### SM-B-8

**Constantes capturadas frente a relaciones geométricas ideales.** Comparar la semántica de constantes capturadas con las relaciones de procedencia del DAG.

**Clase y capacidad.** Censo CU3 y G-M3; la política adoptable resulta de otro método, SM-B-12.

**Estado y decisión.** `INVESTIGACIÓN`; desde la versión 8, con la evidencia del estudio de la autoridad constructiva de `CU3`: la representación binary64 contradice 66 de las 160 relaciones de procedencia del modelo del autor, y una circunferencia tiene tres radios exactos distintos (matriz, semieje y construcción).

**Evidencia, ejemplo y límite.** 4 para frecuencia de desacuerdo y 1 en contraejemplos exactos sobre bits. C6; reconstrucción profunda es diagnóstico.

**Trazabilidad.** análisis de `CU2` §3.8.3 (F-CU2-10); [`locus-v2-certified-construction-model.md`](../../geocedg/specs/locus/locus-v2-certified-construction-model.md) §3; [estudio de la autoridad constructiva](../architecture/pre_g9b_cu3_constructive_authority_study.md) §1.2 a §1.4. sondeo `tools/benchmark/pre-g9b-cu/cu3-java/` (`PreG9BCu3AuthorityStudyProbe`, evidencia y no producto).

### SM-B-9

**Simetría estructural exacta como prueba de existencia de puntos estacionarios.** Una involución exacta que fija el parámetro impone estacionariedad de una función diferenciable invariante.

**Clase y capacidad.** fold-symmetry/v1 para conductor circular alineado sin términos cruzados y dos involuciones de CU2.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado; D-CU2-6; `fold-symmetry/v1`), restringida al conductor en una circunferencia sin términos cruzados y a dos involuciones, y usada solo para los pliegues del mapa trazado.

**Evidencia, ejemplo y límite.** 1, con 2 para encierros adicionales. C4, C5; no cubre simetría aproximada ni singularidades arbitrarias.

**Trazabilidad.** diseño de `CU2` §5; A2.2-R; [`locus-v2-certified-construction-model.md`](../../geocedg/specs/locus/locus-v2-certified-construction-model.md) §10.7. [`kernel/locus/intersection/ConstructionSymmetry2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/ConstructionSymmetry2D.java); [`PreG9BCu2FoldCertificationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu2FoldCertificationTest.java).

### SM-B-10

**Rechazo de pares de tramos polinómicos por la envolvente convexa de Bernstein.** La envolvente convexa de coeficientes de Bernstein permite excluir pares cuando se prueba separación de sus imágenes.

**Clase y capacidad.** Filtro de candidatos polinómicos en Spline V2 con coma flotante.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (aprobado), como evidencia de candidatos en coma flotante, sin cobertura rigurosa.

**Evidencia, ejemplo y límite.** 3 para el filtro implementado; la propiedad matemática es exacta en su formulación. C8, R11; falta redondeo riguroso en esa ruta.

**Trazabilidad.** [`locus-v2-extended-intersections.md`](../../geocedg/specs/locus/locus-v2-extended-intersections.md) (`G8C1 AND G8C2 NORMATIVE — AUTHOR APPROVED`); [ADR 0009](../adr/0009-locus-v2-locus-intersection-pair-semantics.md). [`kernel/locus/intersection/PiecewisePolynomialPairIntersectionCapability2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/PiecewisePolynomialPairIntersectionCapability2D.java); [`PiecewisePolynomialPairIntersectionCapability2DTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PiecewisePolynomialPairIntersectionCapability2DTest.java), [`G8C2LocusPairKernelTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8C2LocusPairKernelTest.java).

### SM-B-11

**Evaluación exacta de programas de regla y compás.** Evaluar un programa de regla y compás en Σ_C por extensiones cuadráticas y decidir signo/nulidad con cotas de separación.

**Clase y capacidad.** Puntos estructurales de CU3-A, clase y selección admitidas, objetivo autoritativo y presupuesto versionado.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` y aprobada en CU3-A para `Σ_C`; G-M3 y la evaluación profunda previa quedan como `INVESTIGACIÓN`.

**Evidencia, ejemplo y límite.** 1 cuando decide; G-M3 anterior fue diagnóstico. C6, R16–R17; decisiones fuera de clase o agotadas quedan pendientes.

**Trazabilidad.** [propuesta normativa de `CU3`](../architecture/pre_g9b_cu3_normative_proposal.md) A3.5; [Etapa A](../architecture/pre_g9b_cu3_stage_a_structural_points_design.md) §6 (M3); [análisis](../architecture/pre_g9b_cu3_initial_state_and_capability_analysis.md) §5.3, §5.7 y §5.8. sondeo `tools/benchmark/pre-g9b-cu/cu3-java/`, escenario P3; desde la versión 12, [`kernel/locus/constructive/ConstructiveConeEvaluator`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/ConstructiveConeEvaluator.java), [`ExactQuadraticTower`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/ExactQuadraticTower.java), [`ExactEvaluationBudget`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/ExactEvaluationBudget.java) y [`ExactEvaluationCounters`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/ExactEvaluationCounters.java); [`ExactQuadraticTowerTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/ExactQuadraticTowerTest.java), [`ConstructiveConeEvaluatorTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/ConstructiveConeEvaluatorTest.java) y [`StructuralPointMatrixTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/StructuralPointMatrixTest.java); [evidencia de coste de E1](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-e1-cost-evidence.json).

### SM-B-12

**Semántica constructiva intrínseca.** Definir el objeto ideal como función del DAG, entradas libres y clase versionada; el corte se usa para evaluar o probar.

**Clase y capacidad.** CU3-A, política experimental de adopción explícita y registro aditivo. Entradas de caminos y fronteras tipadas.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` y aprobada en CU3-A. El censo conserva su carácter exploratorio.

**Evidencia, ejemplo y límite.** 1 como contrato de significado y procedencia condicionada, 4 en el censo. C6, R21; no convierte la política en predeterminada.

**Trazabilidad.** [estudio](../architecture/pre_g9b_cu3_constructive_authority_study.md) §1.2, §1.6, §2.1 y §5; [propuesta normativa](../architecture/pre_g9b_cu3_constructive_authority_normative_proposal.md) §2. sondeo `PreG9BCu3AuthorityStudyProbe` (evidencia y no producto); desde la versión 12, [`kernel/locus/constructive/AuthorityPolicy`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/AuthorityPolicy.java), [`AuthorityPolicyAdoption`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/AuthorityPolicyAdoption.java), [`AuthorityAdoptionImpact`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/AuthorityAdoptionImpact.java), [`ConstructiveClassTable`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/ConstructiveClassTable.java), [`ConstructiveCone`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/ConstructiveCone.java) y [`ConstructiveOperation`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/ConstructiveOperation.java), y [`kernel/spatial/identity/DocumentAuthorityRecord`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/identity/DocumentAuthorityRecord.java); [`AuthorityPolicyTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/AuthorityPolicyTest.java), [`AuthorityPolicyPersistenceTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/AuthorityPolicyPersistenceTest.java), [`ConstructiveClassTableTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/ConstructiveClassTableTest.java), [`ConstructiveConeTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/ConstructiveConeTest.java), [`PreG9BCu3ConstructiveAdoptionDesktopTest`](../../source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BCu3ConstructiveAdoptionDesktopTest.java) (Desktop) y [`PreG9BCu3ProvenanceCensusTest`](../../source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BCu3ProvenanceCensusTest.java) (Desktop).

### SM-B-13

**Certificado de identidad constructiva.** Probar el residual cero en un cuerpo de funciones con la extensión cuadrática admitida y certificar su celda real.

**Clase y capacidad.** CIC CU3-A, incluidos cinco contactos y revalidación por revisión con clave durable.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` y aprobada en CU3-A para `constructive-identity-certificate/v1`; el prototipo histórico conserva su estado de investigación.

**Evidencia, ejemplo y límite.** 1 para identidad y 2 para condiciones de celda. C6, C9, R16–R17, R22; radicales fuera de clase, rama no demostrada y presupuesto son límites distintos.

**Trazabilidad.** [propuesta normativa](../architecture/pre_g9b_cu3_constructive_authority_normative_proposal.md) §5, texto candidato AUT-6; [estudio](../architecture/pre_g9b_cu3_constructive_authority_study.md) §1.6 y §4. prototipo `tools/benchmark/pre-g9b-cu/cu3-java/` (`PreG9BCu3ConstructiveIdentityProbe`, evidencia y no producto); [evidencia](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-constructive-identity-evidence.json); desde la versión 12, [`kernel/locus/constructive/ConstructiveIdentityCertifier`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/ConstructiveIdentityCertifier.java), [`ConstructiveIdentityCertificate`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/ConstructiveIdentityCertificate.java), [`ConstructiveIdentityArithmetic`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/ConstructiveIdentityArithmetic.java), [`ConstructiveIdentityJetArithmetic`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/ConstructiveIdentityJetArithmetic.java), [`CertificatePolynomial`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/CertificatePolynomial.java), [`CertificateRationalFunction`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/CertificateRationalFunction.java), [`ConstructiveIdentityBudget`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/ConstructiveIdentityBudget.java) y [`ConstructiveIdentityCounters`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/ConstructiveIdentityCounters.java); [`ConstructiveIdentityAlgebraTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/ConstructiveIdentityAlgebraTest.java), [`ConstructiveIdentityCertifierTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/ConstructiveIdentityCertifierTest.java), [`ConstructiveIdentityContactTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/ConstructiveIdentityContactTest.java), [`ConstructiveIdentityIntegrationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/ConstructiveIdentityIntegrationTest.java) y [`ConstructiveModelReplicaTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/ConstructiveModelReplicaTest.java); [evidencia de coste de E2](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-e2-cost-evidence.json) y [de E3](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-e3-cost-evidence.json).

### SM-B-14

**Paridad por simetría exacta.** Una involución empareja ceros simples fuera del punto fijo; en el fijo un cero diferenciable invariante no es simple.

**Clase y capacidad.** CU3-A E3 aplica el pliegue CU2 transferido a parámetro exacto y la identidad de contacto por CIC.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` y aprobada en CU3-A para el caso de raíz doble en punto exacto de conductor circular alineado.

**Evidencia, ejemplo y límite.** 1 con premisas de simetría, identidad e hipótesis de clase. C6, C9; la paridad sola no establece incidencia ni recuento global.

**Trazabilidad.** [estudio](../architecture/pre_g9b_cu3_constructive_authority_study.md) §1.7. sondeo, escenario `fold_symmetry`; desde la versión 12, [`kernel/locus/intersection/RootCountCertificate2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/RootCountCertificate2D.java) (forma `ONE_DOUBLE`), [`CertifiedContactAnalysis2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/CertifiedContactAnalysis2D.java) y [`ExactContactEvidence2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/ExactContactEvidence2D.java), y [`kernel/locus/constructive/ConstructiveResidualJetSource`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/ConstructiveResidualJetSource.java), [`ExactContactMemory`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/ExactContactMemory.java) y [`Cu2CertificateTransfer`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/Cu2CertificateTransfer.java); [`PreG9BCu3ExactDoubleRootTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu3ExactDoubleRootTest.java), [`ConstructiveExactContactTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/ConstructiveExactContactTest.java) y [`PreG9BCu3SeamReclassificationAuthorModelTest`](../../source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BCu3SeamReclassificationAuthorModelTest.java) (Desktop); [evidencia de coste de E3](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-e3-cost-evidence.json).

### SM-B-15

**Transferencia de certificados.** Transportar una prueba entre semánticas solo con correspondencia, misma revisión y una nueva justificación por margen, estructura o decisión.

**Clase y capacidad.** Transferencia CU2 → Σ_C de CU3-A, incluidas decisiones en parámetro de pliegue exacto.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` y aprobada en CU3-A para `cu2-certificate-transfer/v1`.

**Evidencia, ejemplo y límite.** 1/2 según vía. C5, C6, C9; el certificado original y la admisión por compatibilidad mantienen ámbitos separados.

**Trazabilidad.** [propuesta normativa](../architecture/pre_g9b_cu3_constructive_authority_normative_proposal.md) §3; [ADR 0038](../adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md), decisiones 7 y 17. [`kernel/locus/constructive/Cu2CertificateTransfer`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/Cu2CertificateTransfer.java) y [`Cu2ProgramCorrespondence`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/Cu2ProgramCorrespondence.java); [`Cu2CertificateTransferTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/Cu2CertificateTransferTest.java); [`Cu2CertificateTransferTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/Cu2CertificateTransferTest.java).

## C. Intervalos y cálculo verificado

### SM-C-1

**Aritmética de intervalos con redondeo exterior.** Las operaciones con redondeo hacia fuera contienen todos los resultados del dominio si se cubren singularidades y extremos.

**Clase y capacidad.** Clases v1/v2 y clase v3 con forma centrada CU2.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` en las clases v1 y v2 (`PRE-G9B-R2-E0/E1` y `PRE-G9B-R3-X1`, aprobados); clase v3, con forma centrada, en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado).

**Evidencia, ejemplo y límite.** 2. C5, C8, R03–R09; dependencia intervalar, denominadores con cero y presupuestos limitan cobertura.

**Trazabilidad.** [`locus-v2-certified-construction-model.md`](../../geocedg/specs/locus/locus-v2-certified-construction-model.md) 1.1, y 1.2 §10 para la clase v3; [ADR 0028](../adr/0028-semantic-endpoint-admissibility-and-certified-semantic-pair-materialization.md), cuya aprobación consta en los registros de decisión de esas fases. [`kernel/locus/intersection/ConstructionIntervalModel2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/ConstructionIntervalModel2D.java), [`SplineOutwardInterval2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/SplineOutwardInterval2D.java); [`kernel/locus/metric/MetricOutwardArithmetic2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/MetricOutwardArithmetic2D.java), [`MetricEnclosure2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/MetricEnclosure2D.java); [`PreG9bR2E1CertifiedConstructionModelTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9bR2E1CertifiedConstructionModelTest.java), [`PreG9bR3X1CertifiedExpressionPointModelTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9bR3X1CertifiedExpressionPointModelTest.java); clase v3: [`kernel/locus/intersection/ConstructionBoxModel2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/ConstructionBoxModel2D.java), [`ConstructionBoxOutcome2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/ConstructionBoxOutcome2D.java); [`PreG9BCu2ClassV3ModelTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu2ClassV3ModelTest.java).

### SM-C-2

**Aislamiento certificado de raíces simples: Krawczyk y Newton de intervalos.** Aislar con inclusión de Krawczyk en sistemas; en una variable, continuidad, cambio de signo y derivada no nula prueban existencia y unicidad.

**Clase y capacidad.** Spline V2 usa Krawczyk; CU2 usa el criterio univariante para transiciones.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` en las intersecciones de SplineV2 (G9S1-R1, aprobado). Las transiciones de `CU2` no usan Krawczyk: el criterio de una variable (cambio de signo con una envolvente de la derivada que excluye el cero) está en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado).

**Evidencia, ejemplo y límite.** 2. C5, C8, R04–R08; cerca de singularidad, un punto medio o inclusión de cero no basta.

**Trazabilidad.** [`geocedg/specs/curves/spline-v2-pair-materialization.md`](../../geocedg/specs/curves/spline-v2-pair-materialization.md) (`NORMATIVE / IMPLEMENTED — PASS — AUTHOR APPROVED`); [ADR 0021](../adr/0021-spline-pair-singleton-germ-materialization.md); diseño de `CU2` §4.3; [`locus-v2-certified-construction-model.md`](../../geocedg/specs/locus/locus-v2-certified-construction-model.md) §10.5. [`kernel/locus/intersection/SplinePairIntervalCertification2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/SplinePairIntervalCertification2D.java), [`SplineImplicitIntervalCertification2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/SplineImplicitIntervalCertification2D.java); [`G9S1R1SplinePairIntervalCertificationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/G9S1R1SplinePairIntervalCertificationTest.java), [`G9S1R1StructuralImplicitCertificationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/G9S1R1StructuralImplicitCertificationTest.java); transiciones de `CU2`: [`kernel/locus/intersection/HybridExistenceCertifier2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/HybridExistenceCertifier2D.java); [`PreG9BCu2HybridCertificationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu2HybridCertificationTest.java).

### SM-C-3

**Raíces múltiples con procedimientos verificados con estructura añadida.** Verificar raíces múltiples con estructura adicional, o encerrar una perturbación bajo hipótesis de teorema.

**Clase y capacidad.** Alternativa investigada para CU2. El trabajo de Rump–Graillat aborda el problema perturbado.

**Estado y decisión.** `INVESTIGACIÓN`: alternativa no elegida.

**Evidencia, ejemplo y límite.** 4 en GeoCeDG, 5 para una raíz múltiple sin evidencia adicional. C5, R09; cuantificar perturbación y distinguir el problema original.

**Trazabilidad.** análisis de `CU2` §3.8.2, técnica C; [ADR 0038](../adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md), alternativas. Sin código productivo propio.

### SM-C-4

**Punto estacionario certificado de un predicado: Krawczyk sobre `P′` con una envolvente de `P″`.** Aislar un punto crítico usando signo de segunda derivada y bisección de la primera; evaluar el valor en el corchete.

**Clase y capacidad.** Ruta CU2 sin Krawczyk propio ni caso de referencia independiente; extensión al residual de contacto verificada en CU3-A.

**Estado y decisión.** `DISEÑO` aprobado para CU2 con ruta implementada sin Krawczyk y sin prueba propia. `IMPLEMENTACIÓN VERIFICADA` y aprobada en CU3-A para la extensión al residual de contacto.

**Evidencia, ejemplo y límite.** 2 para aislamiento bajo signos, 5 para igualdad no decidida. C5, C6, R04; faltan prueba independiente CU2 y cobertura de críticos degenerados.

**Trazabilidad.** diseño de `CU2` §4.3; análisis §3.8.2, técnica B; A2.2-R; [`locus-v2-certified-construction-model.md`](../../geocedg/specs/locus/locus-v2-certified-construction-model.md) §10.5. [`kernel/locus/intersection/HybridExistenceCertifier2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/HybridExistenceCertifier2D.java) (`stationary`); desde la versión 12, para el residuo de contacto, [`RootCountCertificate2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/RootCountCertificate2D.java) y [`CertifiedContactAnalysis2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/CertifiedContactAnalysis2D.java); [`PreG9BCu3CertifiedEvidenceTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu3CertifiedEvidenceTest.java); [`CertifiedContactAnalysis2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/CertifiedContactAnalysis2D.java); [`PreG9BCu3CertifiedEvidenceTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu3CertifiedEvidenceTest.java).

### SM-C-5

**Cotas de derivadas y chorros de primer y segundo orden.** Acotar derivadas y chorros por composición y propagación exterior en el dominio válido.

**Clase y capacidad.** Primera derivada en métrica y predicados; segundo orden CU2 para puntos críticos y pliegues; composición de residual en CU3.

**Estado y decisión.** primer orden: `IMPLEMENTACIÓN VERIFICADA` (envolventes de valor y derivada; aprobado); segundo orden: `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado), usado solo en los puntos estacionarios y los pliegues, sin prueba de referencia independiente de sus envolventes.

**Evidencia, ejemplo y límite.** 2 cuando toda operación está encerrada. C5, C6, R08; falta prueba de referencia independiente de envolventes de segundo orden.

**Trazabilidad.** [`locus-v2-certified-construction-model.md`](../../geocedg/specs/locus/locus-v2-certified-construction-model.md) §6; diseño de `CU2` §5. [`ConstructionIntervalModel2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/ConstructionIntervalModel2D.java); segundo orden: [`kernel/locus/intersection/ConstructionBoxModel2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/ConstructionBoxModel2D.java); [`PreG9BCu2FoldCertificationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu2FoldCertificationTest.java).

### SM-C-6

**Pliegues certificados: simetría, aislamiento, no degeneración, continuidad y tangentes opuestas.** Probar pliegue por simetría, aislamiento, no degeneración, continuidad y direcciones límite opuestas.

**Clase y capacidad.** Pliegues certificados CU2 para conductor circular en clase admitida.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado; D-CU2-6), restringida al conductor en una circunferencia.

**Evidencia, ejemplo y límite.** 1/2. C4, C5, R20; esquinas, cúspides y cambios fuera de clase quedan abiertos.

**Trazabilidad.** diseño de `CU2` §5; A2.2-R; [ADR 0038](../adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md), decisión 8; [`locus-v2-certified-construction-model.md`](../../geocedg/specs/locus/locus-v2-certified-construction-model.md) §10.7. [`kernel/locus/intersection/HybridExistenceCertifier2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/HybridExistenceCertifier2D.java), [`ConstructionSymmetry2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/ConstructionSymmetry2D.java); [`kernel/locus/LocusCertifiedFold2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusCertifiedFold2D.java); [`PreG9BCu2FoldCertificationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu2FoldCertificationTest.java), [`PreG9BCu2AuthorModelCertificationTest`](../../source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BCu2AuthorModelCertificationTest.java) (Desktop).

### SM-C-7

**Condicionamiento y degeneraciones numéricas: clasificación binary64 junto a un valor crítico, tolerancias del evaluador heredado y resolución de las traslaciones.** Analizar resolución local de binary64, cancelación por traslación y escala de normalización geométrica.

**Clase y capacidad.** CU3-A admite casos H02 por evidencia certificada; la sonda trasladada T=10⁶ y r≤20 conserva la pérdida de una raíz para segmento inclinado.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` y aprobada en CU3-A para la vía certificada de H02. Caracterización de condicionamiento en `INVESTIGACIÓN`.

**Evidencia, ejemplo y límite.** 2 en la vía certificada admitida; 4 para el barrido. C6, C9, R08, R10; limitación L-CU1-TRANSLATION-RESOLUTION.

**Trazabilidad.** [estudio de precisión y tolerancias de `CU0`](../architecture/pre_g9b_cu0_precision_and_tolerance_study.md); análisis de `CU2` §3.3 y §3.8.3 (F-CU2-1, F-CU2-9); `L-CU1-TRANSLATION-RESOLUTION` ([registro de cierre de `CU1`](pre_g9b_cu1_closeout_record.md)). desde la versión 12, [`kernel/locus/intersection/CertifiedAdmissionPolicy2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/CertifiedAdmissionPolicy2D.java) y [`CertifiedResidualTarget2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/CertifiedResidualTarget2D.java); [`PreG9BCu3CertifiedEvidenceTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu3CertifiedEvidenceTest.java) (V-H2-1 a V-H2-5) y [`PreG9BCu0SyntheticCharacterizationTest`](../../source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BCu0SyntheticCharacterizationTest.java) (Desktop, `pins.H02`).

### SM-C-8

**Presupuestos de cálculo y fallo cerrado: `NOT_ESTABLISHED` y `UNRESOLVED`.** Limitar trabajo de forma determinista y conservar la región o proposición que queda indecidida.

**Clase y capacidad.** Presupuesto métrico CU1 y bandas CU2, y presupuestos aprobados de exactitud y revalidación CU3.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` en la métrica de `CU1` (presupuesto de 16 384 cajas; aprobado); en la existencia, `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado): presupuestos versionados del certificador y bandas `UNRESOLVED` tipadas; el cierre de una banda de coincidencia a su resolución solo retira certificación local, nunca afirma inexistencia, y se cuenta y se mide (R7).

**Evidencia, ejemplo y límite.** 5 al agotar trabajo, 4 para rendimiento. C5, C6, C9; la pérdida de cobertura medida no reemplaza una cota.

**Trazabilidad.** [`locus-v2-metrics.md`](../../geocedg/specs/locus/locus-v2-metrics.md) §25; diseño de `CU2` §8; [`locus-v2-certified-construction-model.md`](../../geocedg/specs/locus/locus-v2-certified-construction-model.md) §10.9. [`kernel/locus/metric/MetricGuaranteeClass2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/MetricGuaranteeClass2D.java), [`MetricEnclosure2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/MetricEnclosure2D.java); [`PreG9BCu1MetricClassesTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BCu1MetricClassesTest.java); existencia: [`kernel/locus/intersection/HybridExistenceCertifier2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/HybridExistenceCertifier2D.java) (`Policy`, `getClosedCoincidenceWidth`), [`kernel/locus/exact/ExactBudget1D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/exact/ExactBudget1D.java), [`kernel/locus/LocusCertificationWork2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusCertificationWork2D.java) (`COINCIDENCE_BAND_CLOSURE`); [`PreG9BCu2HybridCertificationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu2HybridCertificationTest.java).

### SM-C-9

**Totalidad estructural: certificación sin cajas de los programas de operaciones totales.** La composición de funciones totales sobre un dominio válido conserva totalidad si cada premisa de entrada se cumple.

**Clase y capacidad.** structural-totality/v1 en CU2, con composición mínima V-CON-6 y paso de valores métricos por pasos totales.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado; `structural-totality/v1`): primera autoridad del certificador, composición mínima de V-CON-6 y paso de valores métricos por pasos totales.

**Evidencia, ejemplo y límite.** 1 por estructura. C5; operaciones parciales y soporte incompleto requieren evidencia adicional.

**Trazabilidad.** diseño de `CU2` §4.2; A2.2-R; [`locus-v2-certified-construction-model.md`](../../geocedg/specs/locus/locus-v2-certified-construction-model.md) §10.8 y §10.10. [`kernel/locus/intersection/ConstructionBoxModel2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/ConstructionBoxModel2D.java) (`isStructurallyTotal`), [`HybridExistenceCertifier2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/HybridExistenceCertifier2D.java); [`kernel/algos/AlgoDependentPointLocusV2`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoDependentPointLocusV2.java); [`PreG9BCu2HybridCertificationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu2HybridCertificationTest.java), [`PreG9BCu2ConsumerMigrationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BCu2ConsumerMigrationTest.java).

### SM-C-10

**Aislamiento de raíces polinómicas por tramos monótonos (ceros de la derivada) y bisección.** Separar un polinomio por puntos críticos y refinar raíces en tramos monótonos por bisección.

**Clase y capacidad.** Ruta numérica de spline, incluida distancia cuadrática en interacción.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (aprobado), con evidencia en coma flotante, no certificada; se usa también para los puntos estacionarios de la distancia al cuadrado en la interacción.

**Evidencia, ejemplo y límite.** 3. C8, R13, R15; multiplicidad y extremos requieren otro certificado.

**Trazabilidad.** [`locus-v2-intersections.md`](../../geocedg/specs/locus/locus-v2-intersections.md) 1.2; [ADR 0008](../adr/0008-locus-v2-intersection-result-and-continuation.md). [`kernel/locus/intersection/PolynomialRootIsolation2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/PolynomialRootIsolation2D.java); [`kernel/locus/interaction/LocusPointInteractionResolver2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/interaction/LocusPointInteractionResolver2D.java); [`PolynomialRootIsolation2DTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PolynomialRootIsolation2DTest.java), [`G8BIntersectionKernelTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8BIntersectionKernelTest.java).

### SM-C-11

**Redondeo dirigido con transformaciones exactas (TwoSum y TwoProduct de Dekker) y suma compensada.** Separar parte redondeada y residual con transformaciones exactas dentro de sus condiciones y sumar con compensación.

**Clase y capacidad.** CU1 usa TwoSum, TwoProduct y redondeo dirigido en el cálculo de envolventes.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (`CU1`, aprobado).

**Evidencia, ejemplo y límite.** 1 para identidad de transformación bajo hipótesis, 2 al propagar cotas. C5, R10; extremos de exponente y subdesbordamiento requieren control.

**Trazabilidad.** [`locus-v2-metrics.md`](../../geocedg/specs/locus/locus-v2-metrics.md) §25. [`kernel/locus/metric/MetricOutwardArithmetic2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/MetricOutwardArithmetic2D.java), [`LocusMetricAggregator2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/LocusMetricAggregator2D.java); [`PreG9BCu1MetricClassesTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BCu1MetricClassesTest.java).

### SM-C-12

**Intervalos, chorros del residual y admisión de contacto.** Encerrar h, h′ y h″, y certificar existencia por continuidad y signo. Recuento certificado resuelve conflicto de raíces.

**Clase y capacidad.** Admisión y grados de contacto CU3-A, raíz doble exacta con evidencia añadida y casos de prueba de ambas autoridades.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` y aprobada en CU3-A para admisión certificada, grados de evidencia y regla de conflicto. Los sondeos previos mantienen su alcance exploratorio.

**Evidencia, ejemplo y límite.** 1/2 en clases admitidas, 5 para igualdad puntual inferida de mera inclusión. C6, C9, R05, R09.

**Trazabilidad.** propuesta normativa de `CU3` A3.0, A3.1-R y A3.5; [Etapa B](../architecture/pre_g9b_cu3_stage_b_tangent_contact_certification_design.md) §2–§4 y §9. sondeo de la preparación, escenarios P2 y P5 a P7; desde la versión 12, [`kernel/locus/intersection/ResidualJetSource2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/ResidualJetSource2D.java), [`ResidualJetEnclosure2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/ResidualJetEnclosure2D.java), [`CapturedResidualJetSource2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/CapturedResidualJetSource2D.java), [`CertifiedResidualTarget2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/CertifiedResidualTarget2D.java), [`RootCountCertificate2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/RootCountCertificate2D.java), [`CertifiedContactAnalysis2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/CertifiedContactAnalysis2D.java), [`CertifiedAdmissionPolicy2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/CertifiedAdmissionPolicy2D.java) e [`IntersectionExistenceEvidence2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/IntersectionExistenceEvidence2D.java), y [`kernel/locus/constructive/ConstructiveResidualJetSource`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/ConstructiveResidualJetSource.java) e [`IntervalJet`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/IntervalJet.java); [`PreG9BCu3CertifiedEvidenceTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu3CertifiedEvidenceTest.java) y [`PreG9BCu3ExactDoubleRootTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu3ExactDoubleRootTest.java).

## D. Curvas, métricas y representación

### SM-D-1

**Longitud de arco, variación total de la función trazada, con clases de garantía.** La longitud es variación total; para ramas absolutamente continuas se integra la rapidez en el dominio.

**Clase y capacidad.** Clases CU1 y estimación condicionada CU2 fuera de v3; arcos críticos exactos como control de cota.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (`CU1`, aprobado); la estimación condicionada de una longitud total fuera de la clase v3 (§26.5, decisión H10), sin autoridad de existencia, en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado). Las longitudes totales de los loci críticos coinciden con los arcos barridos exactos dentro de su cota; la de un arco con extremos de raíz cuadrada (las mordeduras del modelo del autor) es `NOT_ESTABLISHED` con `WORK_BUDGET_EXHAUSTED`, sin cambio de valores ni de política: limitación computacional tipada `L-CU2-SQRT-END-LENGTH`, aceptada por la decisión C-10; el estimador con cambio de variable queda como trabajo futuro, sin asignación.

**Evidencia, ejemplo y límite.** 1/2/3 según clase. C5, C7, R18–R20; raíz cuadrada en extremos puede agotar presupuesto.

**Trazabilidad.** [`locus-v2-metrics.md`](../../geocedg/specs/locus/locus-v2-metrics.md) 1.6, §22–§26; [`locus-v2-public-surface.md`](../../geocedg/specs/locus/locus-v2-public-surface.md) (`LocusLength`); [ADR 0007](../adr/0007-revision-scoped-locus-v2-metric-index.md) y [ADR 0037](../adr/0037-locus-v2-metric-guarantee-classes-and-admissibility.md). `org/geogebra/common/kernel/commands/CmdLocusLength` (con cabecera GeoCeDG); [`kernel/algos/AlgoLocusMetricV2`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusMetricV2.java); [`kernel/locus/metric/LocusMetricEngine2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/LocusMetricEngine2D.java), [`PolicyV2LocusMetricCapability2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/PolicyV2LocusMetricCapability2D.java), [`MetricGuaranteeClass2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/MetricGuaranteeClass2D.java); [`PreG9BCu1MetricClassesTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BCu1MetricClassesTest.java), [`LocusMetricProductiveNumericalTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/LocusMetricProductiveNumericalTest.java), [`PreG9BCu1AcceptanceTest`](../../source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BCu1AcceptanceTest.java) (Desktop), [`PreG9BCu2ConditionedEstimateTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BCu2ConditionedEstimateTest.java), [`PreG9BCu2HybridCertificationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu2HybridCertificationTest.java), [`PreG9BCu2AuthorModelConsumerTest`](../../source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BCu2AuthorModelConsumerTest.java) (Desktop).

### SM-D-2

**Longitud estructural o analítica (clase 1).** Usar fórmula estructural con dominio y rama, como radio por ángulo barrido en arco circular válido.

**Clase y capacidad.** Clase 1 métrica CU1 en las familias declaradas.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (`CU1`, aprobado).

**Evidencia, ejemplo y límite.** 1 para fórmula y premisas. C5, R20; una curva visualmente circular no acredita esa clase.

**Trazabilidad.** [`locus-v2-metrics.md`](../../geocedg/specs/locus/locus-v2-metrics.md) §25.1. [`kernel/locus/metric/StructuralLocusMetric2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/StructuralLocusMetric2D.java); [`PreG9BCu1MetricClassesTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BCu1MetricClassesTest.java).

### SM-D-3

**Cotas certificadas de longitud (clase 2) a partir de envolventes de la primera derivada.** Integrar envolventes exteriores de la rapidez a partir de la primera derivada en cajas.

**Clase y capacidad.** Clase 2 CU1 con capacidad de encierro disponible y presupuesto aprobado.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (`CU1`, aprobado; D-CU1-R4).

**Evidencia, ejemplo y límite.** 2. C5, R03, R19; cotas amplias o derivada singular pueden agotar presupuesto.

**Trazabilidad.** [`locus-v2-metrics.md`](../../geocedg/specs/locus/locus-v2-metrics.md) §25. [`kernel/locus/metric/CertifiedConeEnclosure2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/CertifiedConeEnclosure2D.java), [`MetricEnclosure2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/MetricEnclosure2D.java); [`PreG9BCu1MetricClassesTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BCu1MetricClassesTest.java).

### SM-D-4

**Cuadratura adaptativa y estimación del error (clase 3).** Refinar cuadratura y controlar estimaciones del error bajo regularidad y convergencia observada.

**Clase y capacidad.** Clase 3 CU1, con estimación total condicionada fuera de v3 en CU2.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (`CU1`, aprobado; contrato de estimación D-CU1-2).

**Evidencia, ejemplo y límite.** 3. C5, R18–R19; aliasing y discontinuidades dejan el alcance acotado.

**Trazabilidad.** [`locus-v2-metrics.md`](../../geocedg/specs/locus/locus-v2-metrics.md) §25.12. [`kernel/locus/metric/LocusMetricIntegrator2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/LocusMetricIntegrator2D.java), [`LocusMetricEstimator2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/LocusMetricEstimator2D.java); [`PreG9BCu1EstimatorTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BCu1EstimatorTest.java).

### SM-D-5

**Corrección de Richardson y sus hipótesis.** Extrapolar cancelando el término líder del error en refinamientos con expansión asintótica válida.

**Clase y capacidad.** Producción CU1 usa fine+(fine−coarse)/3. Ruta Simpson de otra capacidad ejercitada por pruebas.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` en la ruta de producción del evaluador (`CU1`, aprobado): `LocusMetricEstimator2D` corrige con `fine + (fine − coarse)/3`; la corrección de Simpson de `LocusMetricIntegrator2D` solo es alcanzable desde `DifferentialLocusMetricCapability2D`, que instancian las pruebas. Sus hipótesis se registran y no certifican (D-CU1-R2).

**Evidencia, ejemplo y límite.** 3. C5, R18–R19; órdenes y constantes no intercambiables entre integradores.

**Trazabilidad.** [`locus-v2-metrics.md`](../../geocedg/specs/locus/locus-v2-metrics.md) §25.3. [`kernel/locus/metric/LocusMetricEstimator2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/LocusMetricEstimator2D.java), [`LocusMetricIntegrator2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/LocusMetricIntegrator2D.java); [`PreG9BCu1EstimatorTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BCu1EstimatorTest.java).

### SM-D-6

**Aliasing y límites de cualquier muestreo finito.** Para muestras finitas, puede existir una función distinta que coincida en todas y tenga longitud o eventos diferentes.

**Clase y capacidad.** Política métrica v2 mitiga el caso H10 y registra hipótesis de su estimación.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` de la política métrica v2, que corrige H10 (`CU1`, aprobado); los límites generales, en `INVESTIGACIÓN`.

**Evidencia, ejemplo y límite.** 1 para contraejemplo abstracto, 3 en estimador. C5, R18–R19; falta generalización a regularidades no comprobadas.

**Trazabilidad.** [análisis de aliasing de `CU1`](../architecture/pre_g9b_cu1_metric_aliasing_analysis.md); [`locus-v2-metrics.md`](../../geocedg/specs/locus/locus-v2-metrics.md). [`kernel/locus/metric/PolicyV2LocusMetricCapability2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/PolicyV2LocusMetricCapability2D.java); [`PreG9BCu1EstimatorTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BCu1EstimatorTest.java), [`PreG9BCu0SyntheticCharacterizationTest`](../../source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BCu0SyntheticCharacterizationTest.java) (Desktop).

### SM-D-7

**Componentes continuas de existencia: arcos, extremos laterales y componentes puntuales.** Describir existencia mediante arcos máximos, extremos unilaterales y componentes aisladas.

**Clase y capacidad.** CU2 certifica componentes, publica DIMENSION_CHANGED y preserva existencia ante fallo de posición.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` de la estructura de existencia (aprobado, ADR 0027), con el certificado de compatibilidad retirado en T11; arcos, extremos laterales y componentes puntuales en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado; D-CU2-19), con la transición tipada de cambio de dimensión `DIMENSION_CHANGED` de §15.4 (D-CU2-T13-R1; V-POINT-3; §6.7). Un fallo forzado de la forma límite deja la posición de una componente puntual no disponible sin cambiar su existencia (V-POINT-4, con el gancho interno de prueba de la decisión C-9).

**Evidencia, ejemplo y límite.** 1/2 según método, 4 para regresión V-POINT-4. C4, C5; límites abiertos y bandas sin decidir.

**Trazabilidad.** [ADR 0027](../adr/0027-locus-v2-existence-and-continuous-valid-components.md); [`locus-v2-existence-components.md`](../../geocedg/specs/locus/locus-v2-existence-components.md) 1.0, y 1.1 §15; diseño de `CU2` §3; A2.1-R. [`kernel/locus/LocusExistenceStructure2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusExistenceStructure2D.java), [`LocusBoundary2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusBoundary2D.java), [`LocusPointComponent2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusPointComponent2D.java), [`LocusLineage2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusLineage2D.java), [`LocusComponentLineage2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusComponentLineage2D.java); [`PreG9BS1R2ExistenceStructureTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BS1R2ExistenceStructureTest.java), [`PreG9BCu2ExistenceStructureV2Test`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BCu2ExistenceStructureV2Test.java), [`PreG9BCu2HybridCertificationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu2HybridCertificationTest.java), [`PreG9BCu2LineageTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu2LineageTest.java).

### SM-D-8

**Periodicidad, costuras y parametrizaciones circulares; autoridad única de cierre.** Representar un parámetro periódico con identificación modular; la costura interior no es un extremo geométrico.

**Clase y capacidad.** CU2 establece autoridad única de cierre y certifica periodicidad con conductor circular.

**Estado y decisión.** periodicidad estructural y autoridad única de cierre en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado), en lugar de los cinco predicados del defecto B07; la periodicidad solo se certifica con el conductor en una circunferencia. La costura de un dominio periódico no es un extremo: todo parámetro canónico pertenece al dominio, y los extremos incluidos de un arco que la cruza se evalúan en su propio parámetro (H-CU2A-3 y H-CU2A-4). Limitaciones aceptadas por las decisiones C-11 y C-12: la canonicalización periódica no es la identidad en el dominio fundamental (`L-CU2-PERIODIC-CANONICALIZATION`) y las componentes cerradas periódicas conservan la pertenencia heredada con tolerancia junto a la costura (`L-CU2-CLOSED-PERIODIC-SEAM`).

**Evidencia, ejemplo y límite.** 1/2 para alcance certificado, 4 para representación. C4, C5; canonicalización y pertenencia heredada cerca de costura son limitaciones aceptadas.

**Trazabilidad.** [`locus-v2-intersections.md`](../../geocedg/specs/locus/locus-v2-intersections.md); diseño de `CU2` §4.4; A2.2-R y A2.5; [`locus-v2-existence-components.md`](../../geocedg/specs/locus/locus-v2-existence-components.md) §15.3; [consumo de `CU2`](../architecture/pre_g9b_cu2_consumption_and_representation.md) §4. [`kernel/locus/LocusExistenceStructure2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusExistenceStructure2D.java) (`isClosedPeriodic`), que leen [`euclidian/draw/LocusRenderCache2D`](../../source/shared/common/src/main/java/org/geocedg/common/euclidian/draw/LocusRenderCache2D.java), [`export/SemanticCurveExportAdapter2D`](../../source/shared/common/src/main/java/org/geocedg/common/export/SemanticCurveExportAdapter2D.java) y [`kernel/locus/metric/LocusMetricRouteResolver2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/LocusMetricRouteResolver2D.java); [`kernel/locus/SemanticGeneratorDomainProvider1D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/SemanticGeneratorDomainProvider1D.java), [`LocusDefinition2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusDefinition2D.java); [`PreG9BCu2ConsumerMigrationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BCu2ConsumerMigrationTest.java), [`PreG9BCu2HybridCertificationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu2HybridCertificationTest.java), [`PreG9BCu2AuthorModelConsumerTest`](../../source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BCu2AuthorModelConsumerTest.java) (Desktop).

### SM-D-9

**Límites laterales, discontinuidades y eventos estructurales (`ORIGIN_WRAP`).** Un recorrido orientado con origen puede tener límites laterales diferentes en el cierre del dominio.

**Clase y capacidad.** Evento ORIGIN_WRAP de CU2 en la métrica y en la propagación a e_2.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado; D-CU2-3).

**Evidencia, ejemplo y límite.** 1 para la discontinuidad estructural, 2 para existencia cuando se certifica. C4, C7; no implica ruptura del conductor periódico.

**Trazabilidad.** [diseño del evento](../architecture/pre_g9b_cu2_oriented_length_event_design.md); A2.3-R; [`locus-v2-metrics.md`](../../geocedg/specs/locus/locus-v2-metrics.md) §26. [`kernel/locus/metric/LocusMetricFunctionStructure2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/LocusMetricFunctionStructure2D.java); [`kernel/locus/intersection/RouteSourceFacts2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/RouteSourceFacts2D.java), [`HybridExistenceCertifier2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/HybridExistenceCertifier2D.java); [`PreG9BCu2MetricStructureTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu2MetricStructureTest.java).

### SM-D-10

**Longitud orientada y desarrollo de superficies.** Transportar longitud orientada a una construcción de desarrollo con parámetros y verdadera magnitud.

**Clase y capacidad.** LocusLength y e_2, arco abierto desde t_N=0 y COMPLETE en el caso B10.2.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` de `LocusLength` y su recorrido orientado (aprobado); el desarrollo `e_2` es una construcción del autor, y su existencia, un arco abierto de longitud completa desde `t_N = 0` y `COMPLETE` (B10.2), está en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado); los valores de `LocusLength` no cambian.

**Evidencia, ejemplo y límite.** 1/2/3 según contrato métrico y certificado. C4, C7; otras superficies desarrollables requieren premisas propias.

**Trazabilidad.** [`locus-v2-metrics.md`](../../geocedg/specs/locus/locus-v2-metrics.md); [análisis de la longitud orientada de `CU0`](../architecture/pre_g9b_cu0_oriented_length_analysis.md). [`kernel/algos/AlgoLocusMetricV2`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusMetricV2.java), [`kernel/locus/metric/LocusMetricRouteResolver2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/metric/LocusMetricRouteResolver2D.java); [`G9U0MetricPositionTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/G9U0MetricPositionTest.java), [`PreG9BCu2AuthorModelRepresentationTest`](../../source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BCu2AuthorModelRepresentationTest.java) (Desktop).

### SM-D-11

**Pliegues, esquinas, cúspides y contactos.** Distinguir pérdida de rango, esquina, cúspide y contacto regular por derivadas y límites laterales.

**Clase y capacidad.** Pliegues CU2, singularidad tipada en dibujo, contactos separados por CU3; taxonomía general pendiente.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` y aprobada para pliegues CU2 y contactos admitidos CU3. Cúspides y clasificación geométrica general pendientes.

**Evidencia, ejemplo y límite.** 1/2 en pliegues y contactos admitidos, 5 en cúspides generales. C5, C9, R20; falta análisis de órdenes superiores.

**Trazabilidad.** diseño de `CU2` §4.6 y §5; [propuesta de materialización tangente de `CU0`](../architecture/pre_g9b_cu0_tangent_materialization_proposal.md). [`kernel/locus/LocusCertifiedFold2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusCertifiedFold2D.java), [`LocusSingularParameter2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusSingularParameter2D.java); [`PreG9BCu2FoldCertificationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu2FoldCertificationTest.java), [`PreG9BCu2RepresentationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BCu2RepresentationTest.java).

### SM-D-12

**Teselación y aproximaciones de dibujo y exportación con fidelidad declarada.** Aproximar curvas conservando separadores de componentes y límites de fidelidad.

**Clase y capacidad.** Teselación adaptativa y exportaciones DXF/LaTeX con error estimado y autoridad de cierre CU2.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` del dibujo adaptativo actual y de las aproximaciones de exportación DXF y LaTeX (aprobado), cuyo error sigue siendo estimado (`ESTIMATED_ERROR`) y no certificado; la política de dibujo `locus-render-policy/v2`, el dibujo y la exportación por componentes certificadas y la fidelidad declarada, en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado); las reglas LaTeX están ya en `latex-semantic-curve-export.md` (D-CU2-11).

**Evidencia, ejemplo y límite.** 3/4 para presentación. C5, C8, R11; el dato de dibujo nunca es prueba de incidencia o cierre.

**Trazabilidad.** [`locus-v2-presentation.md`](../../geocedg/specs/locus/locus-v2-presentation.md) 1, y 2 para la política v2; [`dxf-curve-fidelity-and-approximation.md`](../../geocedg/specs/export/dxf-curve-fidelity-and-approximation.md); [`latex-semantic-curve-export.md`](../../geocedg/specs/export/latex-semantic-curve-export.md); [ADR 0014](../adr/0014-export-only-dxf-approximation-and-sidecar.md); [consumo y representación de `CU2`](../architecture/pre_g9b_cu2_consumption_and_representation.md) §5–§6. [`euclidian/draw/LocusRenderCache2D`](../../source/shared/common/src/main/java/org/geocedg/common/euclidian/draw/LocusRenderCache2D.java), [`LocusComponentTessellator2D`](../../source/shared/common/src/main/java/org/geocedg/common/euclidian/draw/LocusComponentTessellator2D.java); [`export/AdaptiveCurveApproximationBuilder2D`](../../source/shared/common/src/main/java/org/geocedg/common/export/AdaptiveCurveApproximationBuilder2D.java), [`export/SemanticCurveExportAdapter2D`](../../source/shared/common/src/main/java/org/geocedg/common/export/SemanticCurveExportAdapter2D.java); Desktop [`org/geocedg/desktop/export/LatexSemanticExportSupport`](../../source/desktop/desktop/src/main/java/org/geocedg/desktop/export/LatexSemanticExportSupport.java); [`G9X1AdaptiveCurveExportTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/export/G9X1AdaptiveCurveExportTest.java), [`LocusV2RenderHardeningTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/LocusV2RenderHardeningTest.java), [`PreG9BR6PlusCLatexExportTest`](../../source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BR6PlusCLatexExportTest.java) (Desktop), [`PreG9BCu2RepresentationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BCu2RepresentationTest.java), [`PreG9BCu2AuthorModelExportTest`](../../source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BCu2AuthorModelExportTest.java) (Desktop).

### SM-D-13

**Curvas spline semánticas (SplineV2) e intersecciones certificadas de pares.** Representar un spline por tramos con base y nudos, y resolver intersección de dos parámetros con certificado local.

**Clase y capacidad.** G9S1/G9S1-R1, numeradores exactos y presupuesto fijo para cálculo; certificación local de pares admitidos.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (G9S1 y G9S1-R1, aprobados): base de potencias truncadas con numeradores exactos y resolución numérica de trabajo fijo.

**Evidencia, ejemplo y límite.** 1 para representación declarada, 2 en pares certificados, 3 en rutas numéricas. C8, R04, R11; pares singulares y completitud requieren evidencia propia.

**Trazabilidad.** [`geocedg/specs/curves/semantic-spline-2d.md`](../../geocedg/specs/curves/semantic-spline-2d.md); [`geocedg/specs/curves/spline-v2-pair-materialization.md`](../../geocedg/specs/curves/spline-v2-pair-materialization.md); [ADR 0018](../adr/0018-semantic-spline-2d-capability.md), [ADR 0021](../adr/0021-spline-pair-singleton-germ-materialization.md) y [ADR 0022](../adr/0022-structural-spline-continuity.md). [`kernel/algos/AlgoSplineV2`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoSplineV2.java); [`kernel/spline/SplineStructuralModel2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/spline/SplineStructuralModel2D.java), [`SplinePrecisionSolve2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/spline/SplinePrecisionSolve2D.java); [`G9S1SemanticSpline2DTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/G9S1SemanticSpline2DTest.java), [`G9S1R1SplinePairMaterializationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/G9S1R1SplinePairMaterializationTest.java).

### SM-D-14

**Contactos en un pliegue del trazado frente a tangencias: en una fuente singular (`F′ = 0`), un cero doble del residuo no es un orden de contacto geométrico; el arco termina en el contacto y un objetivo perturbado da dos preimágenes de un único punto.** Un cero doble de g∘F con F′=0 puede ser un pliegue; en rama regular las derivadas y las condiciones del objetivo prueban orden de contacto.

**Clase y capacidad.** CU3-A distingue FOLD_CONTACT en e_1 y TWO en o_1·h_2; materialización acotada B1/B2.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` y aprobada en CU3-A en la distinción probada; materializaciones B1 y B2 aprobadas por separado.

**Evidencia, ejemplo y límite.** 1/2 en esas clases, 4 para ángulos del sondeo. C6, C9, R20; no identifica automáticamente tangencia de superficies.

**Trazabilidad.** [análisis de `CU3`](../architecture/pre_g9b_cu3_initial_state_and_capability_analysis.md) §5.4b; Etapa B §4.4 y §6.3; SM-C-6. sondeo de la preparación, escenario P2; desde la versión 12, `ContactOrder` de [`kernel/locus/intersection/IntersectionSemanticMetadata2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/IntersectionSemanticMetadata2D.java) y el orden de contacto de [`ConstructiveIdentityCertificate`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/ConstructiveIdentityCertificate.java); [`ConstructiveIdentityContactTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/ConstructiveIdentityContactTest.java), [`ConstructiveExactContactTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/ConstructiveExactContactTest.java) y [`PreG9BCu3SeamReclassificationAuthorModelTest`](../../source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BCu3SeamReclassificationAuthorModelTest.java) (Desktop).

## E. Identidad, integridad y dinámica

### SM-E-1

**Identidad geométrica frente a identidad de representación: identidades durable, de selección y de componente.** Distinguir identidad durable de construcción, selector de un resultado y linaje de componente.

**Clase y capacidad.** ADR 0010/0017, Locus V2 y linaje CU2 con transiciones explícitas.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` de la identidad durable y de rama (aprobado); separación de las tres identidades y linaje estructural en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado), con el cambio de dimensión tipado (SM-D-7).

**Evidencia, ejemplo y límite.** 1 para reglas y 4 para persistencia probada. C1, C5, C9; no deducir identidad de coordenadas, etiquetas u orden.

**Trazabilidad.** [`locus-v2-semantics.md`](../../geocedg/specs/locus/locus-v2-semantics.md), «Branch identity and lineage»; [ADR 0010](../adr/0010-role-gated-spatial-authority-and-durable-identity.md); diseño de `CU2` §7; A2.1-R; [`locus-v2-existence-components.md`](../../geocedg/specs/locus/locus-v2-existence-components.md) §15.6. [`kernel/spatial/identity/PersistentGeoId`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/spatial/identity/PersistentGeoId.java); [`kernel/locus/LocusComponentLineage2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusComponentLineage2D.java), [`LocusLineage2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusLineage2D.java); [`PreG9BCu2LineageTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu2LineageTest.java).

### SM-E-2

**Selección de ramas e intersecciones; salidas efectivas en raíces coincidentes.** Resolver salidas efectivas según el estado capturado y la selección heredada con control de ramas y redondeo.

**Clase y capacidad.** CU2 conserva selección asentada y bandas no resueltas para coincidencias, incluidas segundas salidas sin etiqueta.

**Estado y decisión.** `INVESTIGACIÓN` del comportamiento heredado (F-CU2-8, F-CU2-13, F-CU2-14); lectura del estado de selección, contrato de salidas efectivas y asentamiento de la selección heredada (`selection-settlement/v1`, D-CU2-T11-R1) en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado; D-CU2-17 y D-CU2-18), con pruebas propias de V-COINC-6 y V-COINC-9 (D-CU2-T13-R2). La banda que deja sin resolver la segunda salida sin etiqueta de una intersección recta–circunferencia es rigurosa: una caja sale de ella solo si el primer intento del cálculo heredado es decisivo en todos sus conductores binary64, con cotas a priori de cada redondeo (H-T13-B, D-CU2-T13-R7); el algoritmo heredado no cambia. La versión 4 añade: en compactación la banda acota también la otra raíz bruta, y sus cotas cubren dos huecos de rigor de redondeo (revisión de R7, R7-A a R7-D); una intersección sin etiqueta dentro de una expresión es una selección del corte, reproducida, asentada o fallida con causa (H-CU2A-7); y una consulta que sacó la copia de su primera inicialización aplica la pertenencia exacta de la regla capturada, de modo que una raíz admitida por la tolerancia heredada antes del inicio del camino nunca se publica como salida (H-CU2A-8).

**Evidencia, ejemplo y límite.** 2 para cotas a priori, 4 para cobertura R7-E. C4, C5; distinguir compactación, proximidad y coincidencia exacta.

**Trazabilidad.** diseño de `CU2` §4.1 y §4.7; A2.2-R; [`locus-v2-certified-construction-model.md`](../../geocedg/specs/locus/locus-v2-certified-construction-model.md) §10.2 y §10.3. clases heredadas `org/geogebra/common/kernel/algos/AlgoIntersectLineConic`, `AlgoIntersectSingle` (con accesores de solo lectura) y `AlgoIntersect`; [`kernel/locus/ReconstructibleLocusEvaluator2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/ReconstructibleLocusEvaluator2D.java), [`kernel/locus/intersection/InheritedLineCircleBand2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/InheritedLineCircleBand2D.java); [`PreG9BCu2ClassV3ModelTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu2ClassV3ModelTest.java), [`PreG9BCu2SelectionSettlementTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu2SelectionSettlementTest.java), [`PreG9BCu2AuthorModelSelectionSettlementTest`](../../source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BCu2AuthorModelSelectionSettlementTest.java) (Desktop).

### SM-E-3

**Continuidad y correspondencia semántica: linaje estructural y resolución de selectores.** Relacionar componentes entre revisiones mediante linaje estructural y resolver selectores solo con evidencia suficiente.

**Clase y capacidad.** CU2 tipa división, fusión y DIMENSION_CHANGED; el cambio de dimensión no resuelve un selector.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado; E-3), con el cambio de dimensión tipado, que no resuelve selectores (SM-D-7); desde la versión 4, una división o una fusión nunca relaciona una componente puntual con la cerrada periódica: aparece o desaparece (H-CU2A-10).

**Evidencia, ejemplo y límite.** 1 para regla de contrato, 4 para regresiones. C5, R21; componente puntual y cerrada periódica no se relacionan por división/fusión.

**Trazabilidad.** diseño de `CU2` §7; A2.1-R; [`locus-v2-existence-components.md`](../../geocedg/specs/locus/locus-v2-existence-components.md) §15.6. [`kernel/locus/LocusComponentLineage2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusComponentLineage2D.java), con claves por fuentes estructurales y los bits de los extremos solo como último recurso; [`kernel/locus/LocusComponentSelectorResolver2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusComponentSelectorResolver2D.java); [`PreG9BCu2LineageTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu2LineageTest.java).

### SM-E-4

**Certificación local frente a completitud global; niveles de evidencia.** Distinguir validez local, existencia sobre arco, maximalidad y cobertura del dominio.

**Clase y capacidad.** CU2 expone completitud y causas pendientes en el modelo certificado.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado) sobre la dirección DA-1 aprobada y el ADR 0027: naturalezas de la evidencia, evidencia exploratoria sin autoridad, maximalidad y completitud; el nivel `POINT_VALID` no tiene tipo propio.

**Evidencia, ejemplo y límite.** 1/2 según premisas, 5 en bandas. C5; una muestra o raíz certificada no prueba exhaustividad.

**Trazabilidad.** [ADR 0027](../adr/0027-locus-v2-existence-and-continuous-valid-components.md); [propuesta E de `CU0`](../architecture/pre_g9b_cu0_hybrid_certification_proposal.md); diseño de `CU2` §2; [ADR 0038](../adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md); [`locus-v2-existence-components.md`](../../geocedg/specs/locus/locus-v2-existence-components.md) §15.1 y §15.8. [`kernel/locus/LocusExistenceStructure2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusExistenceStructure2D.java), [`LocusEvidenceNature2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusEvidenceNature2D.java), [`LocusExploratoryEvidence2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusExploratoryEvidence2D.java); [`PreG9BCu2ExistenceStructureV2Test`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BCu2ExistenceStructureV2Test.java), [`PreG9BCu2ConsumerMigrationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BCu2ConsumerMigrationTest.java).

### SM-E-5

**Propagación de validez y dependencias en el grafo: composición de loci dependientes y anidados.** Propagar por DAG composición, revisión y condiciones de validez del soporte.

**Clase y capacidad.** Loci anidados CU1; caso mínimo V-CON-6 de CU2 con soporte COMPLETE sin componentes puntuales y pasos totales.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` de la composición anidada (`CU1`, aprobado); composición certificada mínima de la existencia (V-CON-6) en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado): un punto de rama de un soporte locus `COMPLETE` sin componentes puntuales y con pasos estructuralmente totales; los loci anidados solo acotan la completitud.

**Evidencia, ejemplo y límite.** 1/2 en clase, 4 para pruebas de recomputación. C5, R21; anidación general solo acota completitud.

**Trazabilidad.** [`locus-v2-semantics.md`](../../geocedg/specs/locus/locus-v2-semantics.md), «Dependency graph, revisions and nested composition»; [ADR 0027](../adr/0027-locus-v2-existence-and-continuous-valid-components.md); [`locus-v2-certified-construction-model.md`](../../geocedg/specs/locus/locus-v2-certified-construction-model.md) §10.10. [`kernel/algos/AlgoNestedLocusV2`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoNestedLocusV2.java), [`AlgoDependentPointLocusV2`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoDependentPointLocusV2.java); [`kernel/locus/LocusExistenceStructure2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusExistenceStructure2D.java) (`composedThroughTotalSteps`); [`LocusMetricProductiveNestedTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/LocusMetricProductiveNestedTest.java), [`PreG9BCu1RevisionAndSessionTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BCu1RevisionAndSessionTest.java), [`PreG9BCu2ConsumerMigrationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BCu2ConsumerMigrationTest.java).

### SM-E-6

**Existencia matemática frente a disponibilidad computacional y de representación.** Conservar la proposición de existencia frente al fallo de localizar, evaluar o representar.

**Clase y capacidad.** H-T13-C, V-UND-1, V-POINT-4 y V-AVAIL-6 de CU2.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado). Una raíz certificada que el cálculo heredado pierde deja la existencia certificada y la evaluación con la causa tipada `COMPUTATIONAL_FAILURE` (H-T13-C, con prueba; comportamiento anterior al descendiente), y un locus sobre una longitud que falla queda `UNRESOLVED`, nunca inexistente (V-UND-1); la causa `COMPUTATIONAL_DEPENDENCY_FAILURE` queda reservada, sin emisor (decisión C-2); el fallo forzado de la forma límite de V-POINT-4 deja la componente puntual existente con la posición `UNAVAILABLE` (decisión C-9); ejemplo central del principio transversal 8 de §3.

**Evidencia, ejemplo y límite.** 1/2 para existencia certificada, 4 para controles de fallo. C4, C5; COMPUTATIONAL_DEPENDENCY_FAILURE se mantiene reservado.

**Trazabilidad.** diseño de `CU2` §3.4; A2.1-R; [ADR 0038](../adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md), decisión 11; [`locus-v2-existence-components.md`](../../geocedg/specs/locus/locus-v2-existence-components.md) §15.5. [`kernel/locus/LocusDefinition2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusDefinition2D.java), [`LocusPointComponent2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusPointComponent2D.java), [`LocusBoundaryImage2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusBoundaryImage2D.java); [`PreG9BCu2ConsumerMigrationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BCu2ConsumerMigrationTest.java), [`PreG9BCu2HybridCertificationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu2HybridCertificationTest.java), [`PreG9BCu2AuthorModelConsumerTest`](../../source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BCu2AuthorModelConsumerTest.java) (Desktop).

### SM-E-7

**Aproximaciones de dibujo y exportación sin autoridad geométrica.** Derivar proyecciones visuales/exportaciones desde el modelo semántico con fidelidad y disponibilidad explícitas.

**Clase y capacidad.** Dibujo y DXF/LaTeX consume componentes y autoridad única de cierre CU2.

**Estado y decisión.** principio de arquitectura (`AGENTS.md` §4) aplicado en la exportación semántica implementada; autoridad única de cierre en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado; SM-D-8).

**Evidencia, ejemplo y límite.** 3/4 para aproximación. C5, C8; completar polígonos por proximidad alteraría la semántica del cierre.

**Trazabilidad.** [`geocedg/specs/export/geometry-export-foundation.md`](../../geocedg/specs/export/geometry-export-foundation.md); [consumo de `CU2`](../architecture/pre_g9b_cu2_consumption_and_representation.md) §1. véase SM-D-12.

### SM-E-8

**Revisiones semánticas, corte reconstruible y huella exacta.** Reproducir un corte de dependencias con huella exacta de entradas, roles, estados de selección y revisión.

**Clase y capacidad.** CU1 y huella exacta v2 CU2. V-REV-EXACT-5 contrasta documento fresco y H-CU2A-9 conserva lugar libre.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (`CU1`, aprobado); huella exacta v2 y fidelidad del corte (D-CU2-20-R) en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado), con el contraste con un documento recién construido (V-REV-EXACT-5, D-CU2-T13-R2); el corte reproduce la etiqueta pendiente de su revisión (H-T13-A) y las etiquetas de valor derivado y de dibujo diferido quedan fuera de la huella con su espacio (H-CU2A-5). El cambio de etiquetado de la segunda salida de un par etiquetado se tipa `STRUCTURE`, como fija la decisión C-1, y lo asevera una prueba. Desde la versión 4, el lugar libre de un punto sobre un camino o en una región que sigue al conductor es una entrada del corte: la huella lo distingue y la fidelidad lo compara y lo restaura exactamente (H-CU2A-9).

**Evidencia, ejemplo y límite.** 1 para igualdad del estado capturado, 4 para replay. C5; no prueba identidad ideal más allá de la autoridad de la captura.

**Trazabilidad.** [`locus-v2-semantics.md`](../../geocedg/specs/locus/locus-v2-semantics.md) 1.2, enmienda de `CU1`, y 1.3, enmienda de `CU2`; [ADR 0006](../adr/0006-parallel-locus-v2-semantic-entity.md). [`kernel/locus/ReconstructibleLocusEvaluator2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/ReconstructibleLocusEvaluator2D.java), [`ReconstructionSliceFingerprint2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/ReconstructionSliceFingerprint2D.java), [`ReconstructionExactState2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/ReconstructionExactState2D.java), [`ReconstructionFidelity2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/ReconstructionFidelity2D.java); [`PreG9BCu1RevisionAndSessionTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BCu1RevisionAndSessionTest.java), [`PreG9BCu2RevisionExactnessTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BCu2RevisionExactnessTest.java), [`PreG9BCu2SliceFidelityCharacterizationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PreG9BCu2SliceFidelityCharacterizationTest.java), [`PreG9BCu2AuthorModelSliceFidelityTest`](../../source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BCu2AuthorModelSliceFidelityTest.java) (Desktop).

### SM-E-9

**Continuación semántica de las raíces de intersección; fase y rango intrínsecos deterministas de las raíces públicas.** Continuar una raíz por selector tipado, fase y rango intrínseco en su dominio y revisión.

**Clase y capacidad.** Intersecciones Locus V2 con tokens durables y reglas de discontinuidad.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (aprobado).

**Evidencia, ejemplo y límite.** 1 para regla de identidad, 4 en pruebas de transición. C5, C9, R21; permutar por orden visual o reutilizar tokens cruzaría la barrera de identidad.

**Trazabilidad.** [`locus-v2-intersections.md`](../../geocedg/specs/locus/locus-v2-intersections.md) 1.2; [ADR 0008](../adr/0008-locus-v2-intersection-result-and-continuation.md) y [ADR 0017](../adr/0017-deterministic-intersection-phase-rank-identity.md). [`kernel/locus/intersection/LocusIntersectionSolver2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/LocusIntersectionSolver2D.java); [`kernel/algos/AlgoLocusIntersectionV2`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusIntersectionV2.java); [`G8BIntersectionKernelTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/G8BIntersectionKernelTest.java), [`G9U0R4IntersectionAdmissibilityContinuationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/G9U0R4IntersectionAdmissibilityContinuationTest.java).

### SM-E-10

**Resolución semántica de direcciones inversas y estado interactivo de los puntos sobre loci.** Invertir el parámetro de una trayectoria distinguiendo preimágenes, interacciones y selectores de rama.

**Clase y capacidad.** Point(L,u) y causas tipadas CU2, incluida POINT_COMPONENT; no elegir en ambigüedad.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (aprobado); un `Point(L, u)` sin resolver (selector ambiguo, componente puntual o parámetro fuera de las componentes) tiene causa tipada en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado; H-CU2A-1); con la precisión de V-LIN-4 de la decisión C-7; desde la versión 4, la evaluación de la propia definición en la envolvente de una componente puntual también nombra la causa `POINT_COMPONENT` (H-CU2A-11).

**Evidencia, ejemplo y límite.** 3 para búsqueda numérica y 4 para selección comprobada. C5, R21; autointersección y singularidad requieren procedencia adicional.

**Trazabilidad.** [`locus-v2-point-interaction.md`](../../geocedg/specs/locus/locus-v2-point-interaction.md) 2; [ADR 0019](../adr/0019-semantic-locus-point-interaction-support.md); [`locus-v2-existence-components.md`](../../geocedg/specs/locus/locus-v2-existence-components.md) §15.5. [`kernel/algos/AlgoSemanticLocusPoint2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoSemanticLocusPoint2D.java); [`kernel/locus/interaction/LocusPointInteractionResolver2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/interaction/LocusPointInteractionResolver2D.java), [`kernel/locus/LocusSemanticPointCause2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusSemanticPointCause2D.java); [`G9U0R6SemanticLocusPointInteractionTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/G9U0R6SemanticLocusPointInteractionTest.java), [`PostG9U1A6OptionalBranchPointTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/PostG9U1A6OptionalBranchPointTest.java), [`PreG9BCu2LineageTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu2LineageTest.java).

### SM-E-11

**Transformaciones de semejanza de loci con herencia explícita de su semántica.** Transportar una componente bajo semejanza, conservando autoridad de parámetro y envolvente exterior.

**Clase y capacidad.** CU2 transforma componentes puntuales y evalúa extremos propios al cruzar costura.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` (aprobado); la imagen de una componente puntual transporta la envolvente de su posición con aritmética de intervalos hacia fuera, en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado; H-CU2A-2); desde la versión 4, los extremos incluidos de un arco imagen que cruza la costura se evalúan en su propio parámetro, sin recanonicalizar en la fuente (H-CU2A-6).

**Evidencia, ejemplo y límite.** 1 para transporte estructural, 2 para encierro de posición. C5; fuera de semejanzas se requiere política de transformación.

**Trazabilidad.** [`locus-v2-similarity-transformations.md`](../../geocedg/specs/locus/locus-v2-similarity-transformations.md), nota de alcance A2.5. [`kernel/algos/AlgoLocusSimilarityTransform2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusSimilarityTransform2D.java); [`kernel/locus/LocusSimilarityTransform2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/LocusSimilarityTransform2D.java); [`G9U0R5SimilarityTransformationsTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/locus/G9U0R5SimilarityTransformationsTest.java), [`PreG9BCu2HybridCertificationTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu2HybridCertificationTest.java).

### SM-E-12

**Puntos estructurales por correspondencia del conductor.** Establecer D↦t(D) por procedencia, soporte, existencia, unicidad y orientación; probar una segunda incidencia separadamente.

**Clase y capacidad.** Point(L,D) e Intersect(L,r,D) de CU3-A; CIC para P∈r donde aplica.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` y aprobada en CU3-A en las formas con conductor y la clase de objetivos aprobadas.

**Evidencia, ejemplo y límite.** 1/2, y 5 cuando se pierde una premisa. C6, C9; soporte o rama ambigua y objetivo fuera de clase.

**Trazabilidad.** propuesta normativa de `CU3` A3.3-R; [Etapa A](../architecture/pre_g9b_cu3_stage_a_structural_points_design.md). sondeo de la preparación, escenarios P1, P2, P4 y P10; desde la versión 12, [`kernel/algos/AlgoLocusDriverCorrespondencePoint2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/algos/AlgoLocusDriverCorrespondencePoint2D.java) y [`kernel/locus/constructive/StructuralPointResolver`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/StructuralPointResolver.java), [`StructuralPointResolution`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/StructuralPointResolution.java), [`DriverParameterEnclosure`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/DriverParameterEnclosure.java) e [`InheritedLineCircleSelection`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/InheritedLineCircleSelection.java); [`StructuralPointTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/StructuralPointTest.java), [`StructuralPointMatrixTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/StructuralPointMatrixTest.java) y [`PreG9BCu3StructuralPointAuthorModelTest`](../../source/desktop/desktop/src/test/java/org/geocedg/desktop/PreG9BCu3StructuralPointAuthorModelTest.java) (Desktop).

### SM-E-13

**Germen de contacto tangente y ciclo de vida.** Anclar un contacto certificado en germen, linaje y orientación, con estados de pérdida o cambio sin transferir token.

**Clase y capacidad.** B1 materializa FOLD_CONTACT con ancla de pliegue; B2 materializa tangencia regular clase 1/2, orden dos, con P1.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` y aprobada para los dos incrementos acotados de CU3-B, con la semántica de sus anclas y transiciones. El diseño histórico permanece como antecedente.

**Evidencia, ejemplo y límite.** 1/2 para evidencia del contacto, 4 para persistencia y transiciones. C9; las clases 3–5 permanecen en el resultado rico sin materializar el contacto, incluida la clase 4 con ausencia certificada o dos raíces transversales certificadas. La generalización de la producción de certificados exactos de tangencia regular sigue diferida.

**Trazabilidad.** [`locus-v2-intersections.md`](../../geocedg/specs/locus/locus-v2-intersections.md), CU3.3 y enmienda 1.6; [ADR 0039](../adr/0039-locus-v2-structural-points-and-certified-contact-evidence.md) enmendado; [ADR 0017](../adr/0017-deterministic-intersection-phase-rank-identity.md). [`FoldContactAnchor2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/FoldContactAnchor2D.java); [`RegularTangentAnchor2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/RegularTangentAnchor2D.java); [`PreG9BCu3FoldAnchorTransitionTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu3FoldAnchorTransitionTest.java); [`PreG9BCu3RegularTangentGermTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu3RegularTangentGermTest.java); [`PreG9BCu3B2LoadPolicyTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/PreG9BCu3B2LoadPolicyTest.java).

### SM-E-14

**Integridad de relaciones.** Atribuir integridad tipada a relación y revisión por autoridad, con propagación por premisas y admisión positiva.

**Clase y capacidad.** CU3-A implementa STRICT, RELAXED, NOT_ESTABLISHED y CONTRADICTED, requalificación G8B-R1 y dos vías de admisión.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` y aprobada en CU3-A. `RELAXED` conserva su carácter de admisión positiva.

**Evidencia, ejemplo y límite.** 1/2 para STRICT; compatibilidad RELAXED conserva categoría de su evidencia y carece de fuerza de prueba. C6, C9; análisis central §§2.2–2.3.

**Trazabilidad.** [estudio](../architecture/pre_g9b_cu3_constructive_authority_study.md) §2.4; [propuesta normativa](../architecture/pre_g9b_cu3_constructive_authority_normative_proposal.md) §4. [`kernel/locus/constructive/RelationIntegrity`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/RelationIntegrity.java), [`RelationIntegrityRecord`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/RelationIntegrityRecord.java), [`SubstituteGenericEvidence`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/SubstituteGenericEvidence.java), [`SubstituteGenericPerturbationSet`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/SubstituteGenericPerturbationSet.java) e [`IntersectionAdmissionImpact`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/IntersectionAdmissionImpact.java), y [`kernel/locus/intersection/ConstructiveRequalification2D`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/intersection/ConstructiveRequalification2D.java); [`RelationIntegrityTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/RelationIntegrityTest.java), [`SubstituteGenericEvidenceTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/SubstituteGenericEvidenceTest.java), [`SubstituteGenericPerturbationSetTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/SubstituteGenericPerturbationSetTest.java) y [`ConstructiveIntersectionAdmissionTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/ConstructiveIntersectionAdmissionTest.java).

### SM-E-15

**Reutilización exacta para el rendimiento interactivo.** Reducir reevaluación por especialización exacta de celdas, preparación de márgenes y compartición por revisión.

**Clase y capacidad.** T1–T3, T6 y T7-a integrados en CU3-A con revalidación, comparaciones del corpus y presupuesto compartido.

**Estado y decisión.** `IMPLEMENTACIÓN VERIFICADA` y aprobada en CU3-A para T1–T3, T6, T7-a y contabilidad D1-A. Los tiempos y coberturas del sondeo son evidencia exploratoria.

**Evidencia, ejemplo y límite.** 1/2 para conservación de la prueba bajo las precondiciones, 4 para tiempo y contadores. C9; falta cobertura fuera de la clase y del corpus.

**Trazabilidad.** [plan de optimización](../architecture/pre_g9b_cu3_performance_optimization_plan.md); [`locus-v2-constructive-authority.md`](../../geocedg/specs/locus/locus-v2-constructive-authority.md) §8.2. [`kernel/locus/constructive/ConstructiveIdentityRecheckMemory`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/ConstructiveIdentityRecheckMemory.java), [`ConstructiveSliceEnclosureMemory`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/ConstructiveSliceEnclosureMemory.java), [`ConstructiveResidualJetSource`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/ConstructiveResidualJetSource.java) y [`CertificatePolynomial`](../../source/shared/common/src/main/java/org/geocedg/common/kernel/locus/constructive/CertificatePolynomial.java); [`ConstructiveIdentityRecheckMemoryTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/ConstructiveIdentityRecheckMemoryTest.java), [`ConstructiveSliceEnclosureMemoryTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/ConstructiveSliceEnclosureMemoryTest.java); [`PreG9BCu3EarlyExactDecisionTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/intersection/PreG9BCu3EarlyExactDecisionTest.java); [`ConstructiveEarlyExactDecisionTest`](../../source/shared/common-jre/src/test/java/org/geocedg/common/kernel/locus/constructive/ConstructiveEarlyExactDecisionTest.java).

## Conciliaciones documentales y lagunas

El [plan E1, §6.7](../architecture/pre_g9b_cu_scientific_methods_editorial_handoff_plan.md#67-discrepancias-de-estado-observadas) registró cabeceras anteriores a su evidencia de fase para ADR 0028, ADR 0022, presentación, DXF y semántica espacial G9. La ficha de cada método limita su estado a la implementación y cierre verificables. El nombre histórico `LocusContinuousDomainCertifier2D` carece de clase productiva con ese nombre: la certificación de CU2 remite a `HybridExistenceCertifier2D`. Para la métrica se distinguen la estimación de producción y la ruta Simpson/Richardson citada en el historial spline. El hueco de linaje de la primera versión de CU2 se resolvió por D-CU2-T13-R1 con `DIMENSION_CHANGED` y V-POINT-3. Cualquier actualización de las normas de origen tiene su propio alcance de revisión.

Las lagunas principales de investigación son certificación algebraica de predicados posteriores a radicales, generalización de tangencia exacta, órdenes singulares fuera de clase y construcción espacial por tipo. Las lagunas de implementación incluyen el presupuesto métrico en extremos de raíz cuadrada y la frontera exacta sin arcos en CU3. Las de evidencia incluyen la referencia independiente de envolventes de segundo orden y la redefinición efectiva del soporte del conductor. El [anexo de fuentes](pre_g9b_cu10_scientific_examples_and_references.md) mantiene las lagunas bibliográficas y los casos de aplicación.
