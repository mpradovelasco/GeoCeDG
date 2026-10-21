# PRE-G9B-CU — Plan de transferencia científica de los métodos de GeoCeDG

| Campo | Valor |
|---|---|
| Carácter | Registro vivo de planificación y trazabilidad científica del mandato editorial E1. No es especificación normativa, no contiene texto editorial y no planifica el libro: la planificación editorial corresponde a `geocedg_book` cuando se autorice esa fase |
| Mandato | instrucción autoral «GeoCeDG — PRE-G9B-CU2. Cierre de coherencia normativa y registro del programa de apéndices científicos» (2026-10-20), §5 |
| Estado | Mandato editorial del autor: **aprobado** (2026-10-20). Diseño detallado de los apéndices: **pendiente de preparación y revisión**. Redacción e incorporación a `geocedg_book`: **no autorizadas todavía**. `selfApproved=false` |
| Registrado en | [plan consolidado](pre_g9b_cu_corrective_usability_plan.md) §2.3, `CU10`, §6 y §8; [hoja de ruta](../roadmap/geocedg_roadmap.md) 5.65, track `PRE-G9B-CU` |
| Libro | `geocedg_book` sin cambios (`78fbc88e9e12346b89d6ded411eaae94de2284b9`) |
| Versión del registro | 1: creación con el cierre de coherencia de `CU2`, sobre la base `cbb7ea8c` |

## 1. Objeto

El autor quiere que los resultados científicos desarrollados en GeoCeDG, en `CU0`–`CU2` y en los
paquetes posteriores del track, pasen al futuro libro como apéndices científicos rigurosos. No
se limitarán a explicar cómo funciona una herramienta o cómo se resolvió un ejemplo: presentarán
el fundamento matemático, la construcción geométrica y las condiciones bajo las que cada técnica
se aplica a familias más generales de problemas CeDG.

Este registro:

- inventaría los métodos que el repositorio usa, diseña o ha estudiado, con su evidencia (§6);
- fija lo que deberá explicar el material futuro de cada método (§4);
- mantiene una matriz de transferencia ampliable (§5 y §6);
- define el expediente de `CU10` (§7) y el mantenimiento del registro (§8).

No escribe texto de apéndices, no diseña los apéndices en detalle y no modifica `geocedg_book`.

## 2. Estado del mandato

| Elemento | Estado |
|---|---|
| Mandato editorial del autor | aprobado (2026-10-20) |
| Este registro | creado; entrada viva que se actualiza al cerrar cada paquete científico (§8) |
| Diseño detallado de los apéndices | pendiente de preparación y revisión |
| Redacción e incorporación a `geocedg_book` | no autorizadas |
| Expediente `SCIENTIFIC_METHODS_EDITORIAL_HANDOFF` | obligatorio en `CU10` (§7) |
| Fase editorial posterior a `CU10` | no automática: la actualización de la base técnica del libro y la incorporación de apéndices, figuras y capítulos necesitan una autorización editorial propia |

El mandato es independiente de la autorización de `CU2` y no es una decisión de su
implementación.

## 3. Principios

1. **Evidencia antes que afirmación.** Un método está en `IMPLEMENTACIÓN VERIFICADA` solo si
   tiene código productivo y pruebas en este repositorio, verificados en su fase. Si solo está en
   una especificación, un ADR o un documento candidato, está en `DISEÑO`. Si se estudió en un
   análisis o un sondeo, incluido el que se descartó como alternativa, está en `INVESTIGACIÓN`.
2. **Estado técnico y aprobación autoral son hechos independientes.** La aprobación se anota
   aparte, con la autoridad que la registra (registro de cierre, especificación o ADR aprobados).
   Un PASS técnico no es aprobación, y un diseño aprobado no es una implementación.
3. **Naturaleza de la evidencia.** Cada resultado se clasifica como prueba simbólica o algebraica
   exacta, aritmética de intervalos con cotas rigurosas, verificación numérica bajo hipótesis,
   evidencia exploratoria o falta de evidencia
   ([diseño de certificación de `CU2`](pre_g9b_cu2_hybrid_certification_design.md) §2). Solo las
   dos primeras certifican.
4. **Generalidad en tres niveles.** Para cada método se separan la aplicabilidad matemática
   general (por ejemplo, las sucesiones de Sturm valen para cualquier polinomio con coeficientes
   exactos), la clase en que GeoCeDG lo prueba o lo usa (por ejemplo, predicados de primer nivel
   con el conductor en una circunferencia) y la capacidad implementada. Ninguna demostración
   para circunferencias o predicados algebraicos de primer nivel se extrapola a funciones,
   superficies o construcciones arbitrarias.
5. **Ejemplo motivador, no marco exclusivo.** La intersección del cilindro y el cono del modelo
   del autor es un ejemplo importante. Cada ficha indica, cuando es justificable, otras familias
   de problemas: intersecciones de superficies regladas, desarrollos, envolventes, curvas de
   intersección, proyecciones, singularidades y construcciones dependientes.
6. **Referencias.** Proceden de las fuentes científicas pertinentes y de la evidencia del
   proyecto. Las obras del autor se citan por su identificador del
   [catálogo de referencias](../references/cedg/catalog.yml). Los datos de las referencias
   clásicas de §9 se verifican contra la fuente antes de su uso editorial.
7. **Autoridad.** Este registro es evidencia y planificación. Las especificaciones y los ADR
   aprobados siguen siendo la autoridad del producto, y `geocedg_book` será la de su planificación
   editorial cuando se autorice esa fase.

## 4. Contenido exigido a cada apéndice futuro

Para cada método, el material futuro explicará:

1. el problema matemático general que resuelve;
2. definiciones, hipótesis y dominio de aplicación;
3. enunciados, propiedades y demostraciones cuando correspondan;
4. invariantes y condiciones de certificación;
5. degeneraciones, discontinuidades y casos límite;
6. la diferencia entre garantía rigurosa y aproximación numérica;
7. el algoritmo constructivo o computacional y su complejidad;
8. su integración conceptual con los procedimientos de la geometría descriptiva;
9. un ejemplo trabajado que motive la técnica;
10. su generalización a otras construcciones CeDG;
11. contraejemplos o situaciones donde la técnica no se aplica;
12. referencias bibliográficas y correspondencia con las especificaciones y pruebas de GeoCeDG.

La aplicabilidad matemática general se distingue siempre de las capacidades que GeoCeDG tiene
implementadas.

## 5. Estructura de la matriz de transferencia

La matriz tiene una entrada por método. Para que crezca sin rehacerse, cada entrada tiene dos
partes con el mismo identificador: la **fila del inventario** (§6.1 a §6.5), con la trazabilidad
técnica, y la **ficha** (§6.6), con el contenido científico y editorial.

| Campo | Parte | Contenido |
|---|---|---|
| Identificador | ambas | `SM-<categoría>-<n>`; nunca se reutiliza ni se renumera |
| Método | inventario | nombre breve |
| Descripción matemática | ficha | qué calcula o prueba, en términos matemáticos |
| Alcance y generalidad | ficha | aplicabilidad general, clase usada por GeoCeDG y capacidad implementada (§3, principio 4) |
| Hipótesis de aplicabilidad | ficha | condiciones necesarias para que el resultado valga |
| Degeneraciones y límites | ficha | casos límite, discontinuidades y situaciones donde no se aplica |
| Especificación o ADR | inventario | fundamento normativo, o el documento candidato o de análisis si no lo hay |
| Código y pruebas | inventario | clases y pruebas, cuando existen |
| Ejemplo o modelo | ficha | construcción reproducible, preferentemente del corpus versionado |
| Estado | inventario | `INVESTIGACIÓN`, `DISEÑO` o `IMPLEMENTACIÓN VERIFICADA`, con la aprobación autoral aparte: aprobado (con su autoridad), candidato o alternativa no elegida |
| Referencias | ficha | catálogo del proyecto y bibliografía de §9 |
| Apéndice candidato | inventario | letra de la categoría (A–E de §6): agrupación provisional, no un diseño de apéndices |
| Cobertura editorial y cuestiones abiertas | ficha | qué falta para el contenido de §4 |

Reglas de ampliación: un método nuevo recibe el siguiente identificador de su categoría; un
cambio de estado cita su evidencia (commit, verificación o registro de cierre); un método
sustituido conserva su entrada con el motivo; ninguna entrada se borra.

## 6. Inventario inicial

Inicial y no exhaustivo, con la evidencia del repositorio en la base `cbb7ea8c` y en el cierre de
coherencia de `CU2`. Rutas de código abreviadas desde
`source/shared/common/src/main/java/org/geocedg/common/`. La columna «Ficha» indica si §6.6 ya la
contiene.

### 6.1 A. Fundamentos de geometría descriptiva computacional

| Id | Método | Estado | Especificación o ADR | Código y pruebas | Ap. | Ficha |
|---|---|---|---|---|---|---|
| SM-A-1 | Objetos espaciales definidos por proyecciones: punto espacial desde vistas ortográficas por mínimos cuadrados (descomposición en valores singulares con umbral de rango) y validación del sistema de proyección (despliegue alrededor de la charnela y cambios de plano) | `IMPLEMENTACIÓN VERIFICADA` en el piloto de punto (track G9A, aprobado; experimental y desactivado por defecto), con evidencia numérica bajo umbral, no certificada; las demás primitivas (recta, plano, circunferencia, cónica y curva espacial) en `DISEÑO` (G9B y G9C, no autorizadas) | `geocedg/specs/spatial/g9-spatial-projection-semantics.md` 1.0 (`NORMATIVE / AUTHOR APPROVED`), §2.2 y §10; ADR 0010 y ADR 0011 | `kernel/spatial/semantic/ProjectionDefinedPointEvaluator`, `ProjectionSystemEvaluator`; `kernel/spatial/runtime/AlgoProjectionDefinedSpatialPoint`; `G9A2ProjectionDefinedPointEvaluatorTest`, `G9A2ProjectionSystemEvaluatorTest`, `G9A3SpatialMutationLifecycleTest`; modelo `models/regression/g9a2-spatial-point-pilot/` | A | no |
| SM-A-2 | Identidad entre el objeto espacial, sus coordenadas intrínsecas de proyección y su colocación en un diagrama CeDG | `IMPLEMENTACIÓN VERIFICADA` (track G9A, aprobado; experimental) | misma especificación: G9P-R1 separa las coordenadas intrínsecas de su colocación en el diagrama; ADR 0010 | `kernel/spatial/identity/ProjectionBindingRecord`, `ProjectionDiagramMapRecord`; `kernel/spatial/semantic/DiagramMapDefinition`, `ProjectionFrameDefinition`; `G9A2SpatialSemanticRecordXmlTest`, `G9A2SpatialSemanticLifecycleTest` | A | no |
| SM-A-3 | Incidencia, pertenencia y dependencia constructiva; proyección durable de dependencias | `IMPLEMENTACIÓN VERIFICADA`: grafo de dependencias heredado y proyección durable de `PRE-G9B-R3` (aprobado) | `geocedg/specs/spatial/durable-dependency-projection.md` 1.1; ADR 0029; `locus-v2-semantics.md`, «Dependency graph, revisions and nested composition» | `kernel/spatial/identity/DurableDependencyProjection`; `PreG9bR3DurableDependencyProjectionTest` | A | no |
| SM-A-4 | Parametrización y continuidad en construcciones dinámicas: dominios del conductor, proveedores y ramas | `IMPLEMENTACIÓN VERIFICADA` de la parametrización de Locus V2 (aprobado); continuidad certificada en `DISEÑO` (`CU2`, candidato) | `locus-v2-semantics.md`, «Driver-domain providers and parameters» y «Branch identity and lineage»; ADR 0027 | `kernel/algos/AlgoLocusV2`, `kernel/locus/LocusBranch2D`; `PreG9BS1R2ExistenceStructureTest` | A | no |
| SM-A-5 | Geometría matemática, cálculo y representación como capas distintas | `DISEÑO` (`CU2`, candidato), sobre las normas aprobadas de presentación y exportación | [diseño de certificación de `CU2`](pre_g9b_cu2_hybrid_certification_design.md) §3.4; ADR 0038 (candidato); `locus-v2-presentation.md`; `dxf-curve-fidelity-and-approximation.md` | — | A | SM-E-6 |
| SM-A-6 | Intersección de superficies en el modelo del autor: cono y cilindro, penetración y mordedura, verdadera magnitud por giro y desarrollo | `INVESTIGACIÓN`: construcción del autor del corpus de `CU0`, analizada en `CU2`; las herramientas que usa (Locus V2 y `LocusLength`) están implementadas | [análisis del modelo del autor](pre_g9b_cu2_author_model_certification_analysis.md) §2–§5 | corpus `models/regression/pre-g9b-cu/`; pruebas de caracterización `PreG9BCu0*` (Desktop) | A | no |

### 6.2 B. Geometría algebraica y certificación exacta

| Id | Método | Estado | Especificación o ADR | Código y pruebas | Ap. | Ficha |
|---|---|---|---|---|---|---|
| SM-B-1 | Discriminante de la intersección recta–circunferencia y su factorización por el par de tangentes | `INVESTIGACIÓN` | análisis de `CU2` §3.2 y §3.8 | sondeos fuera del repositorio | B | sí |
| SM-B-2 | Multiplicidad de raíces, tangencias y clasificación de las raíces de un predicado de validez | `DISEÑO` (`CU2`, candidato) | diseño de `CU2` §3.5 y §4.6; A2.2-R | — | B | sí |
| SM-B-3 | Aritmética racional exacta sobre las constantes binary64 capturadas | `DISEÑO` (`CU2`, candidato; D-CU2-16) | diseño de `CU2` §4.6; `locus-v2-certified-construction-model.md` §3 (constantes capturadas) | prototipo en un sondeo, fuera del repositorio | B | SM-B-5 |
| SM-B-4 | Polinomios en una variable, máximo común divisor y factorización libre de cuadrados | `DISEÑO` (`CU2`, candidato) | diseño de `CU2` §4.6 | prototipo en un sondeo | B | SM-B-5 |
| SM-B-5 | Sucesiones de Sturm y recuento certificado de raíces: certificado algebraico exacto de primer nivel | `DISEÑO` (`CU2`, candidato; D-CU2-16) | diseño de `CU2` §4.6; A2.2-R; ADR 0038, decisión 7 | prototipo en un sondeo | B | sí |
| SM-B-6 | Predicados de validez y transiciones topológicas | `DISEÑO` (`CU2`, candidato) sobre el orden de autoridades aprobado | `locus-v2-existence-components.md` 1.0 §7; diseño de `CU2` §4.3; A2.2-R | hoy ningún productor dependiente certifica transiciones: `kernel/algos/AlgoDependentPointLocusV2` publica el certificado de compatibilidad | B | SM-B-2 |
| SM-B-7 | Eliminación algebraica para predicados posteriores a una raíz cuadrada | `INVESTIGACIÓN`: laguna identificada, fuera de `CU2` | diseño de `CU2` §4.6, «Límites» | — | B | no |
| SM-B-8 | Constantes capturadas frente a relaciones geométricas ideales | `INVESTIGACIÓN` | análisis de `CU2` §3.8.3 (F-CU2-10); `locus-v2-certified-construction-model.md` §3 | — | B | SM-B-5 |
| SM-B-9 | Simetría estructural exacta como prueba de existencia de puntos estacionarios | `DISEÑO` (`CU2`, candidato; D-CU2-6) | diseño de `CU2` §5; A2.2-R | — | B | SM-C-6 |
| SM-B-10 | Rechazo de pares de tramos polinómicos por la envolvente convexa de Bernstein | `IMPLEMENTACIÓN VERIFICADA` (aprobado), como evidencia de candidatos en coma flotante, sin cobertura rigurosa | `locus-v2-extended-intersections.md` (`G8C1 AND G8C2 NORMATIVE — AUTHOR APPROVED`); ADR 0009 | `kernel/locus/intersection/PiecewisePolynomialPairIntersectionCapability2D`; `PiecewisePolynomialPairIntersectionCapability2DTest`, `G8C2LocusPairKernelTest` | B | no |

### 6.3 C. Análisis numérico riguroso

| Id | Método | Estado | Especificación o ADR | Código y pruebas | Ap. | Ficha |
|---|---|---|---|---|---|---|
| SM-C-1 | Aritmética de intervalos con redondeo exterior | `IMPLEMENTACIÓN VERIFICADA` en las clases v1 y v2 (`PRE-G9B-R2-E0/E1` y `PRE-G9B-R3-X1`, aprobados); clase v3 en `DISEÑO` (`CU2`) | `locus-v2-certified-construction-model.md` 1.1; ADR 0028, cuya aprobación consta en los registros de decisión de esas fases | `kernel/locus/intersection/ConstructionIntervalModel2D`, `SplineOutwardInterval2D`; `kernel/locus/metric/MetricOutwardArithmetic2D`, `MetricEnclosure2D`; `PreG9bR2E1CertifiedConstructionModelTest`, `PreG9bR3X1CertifiedExpressionPointModelTest` | C | sí |
| SM-C-2 | Aislamiento certificado de raíces simples: Krawczyk y Newton de intervalos | `IMPLEMENTACIÓN VERIFICADA` en las intersecciones de SplineV2 (G9S1-R1, aprobado); `DISEÑO` para las transiciones de `CU2` | `geocedg/specs/curves/spline-v2-pair-materialization.md` (`NORMATIVE / IMPLEMENTED — PASS — AUTHOR APPROVED`); ADR 0021; diseño de `CU2` §4.3 | `kernel/locus/intersection/SplinePairIntervalCertification2D`, `SplineImplicitIntervalCertification2D`; `G9S1R1SplinePairIntervalCertificationTest`, `G9S1R1StructuralImplicitCertificationTest` | C | sí |
| SM-C-3 | Raíces múltiples con procedimientos verificados con estructura añadida | `INVESTIGACIÓN`: alternativa no elegida | análisis de `CU2` §3.8.2, técnica C; ADR 0038, alternativas | — | C | sí |
| SM-C-4 | Punto estacionario certificado de un predicado: Krawczyk sobre `P′` con una envolvente de `P″` | `DISEÑO` (`CU2`, candidato) | diseño de `CU2` §4.3; análisis §3.8.2, técnica B; A2.2-R | — | C | no |
| SM-C-5 | Cotas de derivadas y chorros de primer y segundo orden | primer orden: `IMPLEMENTACIÓN VERIFICADA` (envolventes de valor y derivada; aprobado); segundo orden: `DISEÑO` (`CU2`, paso T5) | `locus-v2-certified-construction-model.md` §6; diseño de `CU2` §5 | `ConstructionIntervalModel2D` | C | SM-C-6 |
| SM-C-6 | Pliegues certificados: simetría, aislamiento, no degeneración, continuidad y tangentes opuestas | `DISEÑO` (`CU2`, candidato; D-CU2-6) | diseño de `CU2` §5; A2.2-R; ADR 0038, decisión 8 | — | C | sí |
| SM-C-7 | Condicionamiento y degeneraciones numéricas: clasificación binary64 junto a un valor crítico, tolerancias del evaluador heredado y resolución de las traslaciones | `INVESTIGACIÓN` | [estudio de precisión y tolerancias de `CU0`](pre_g9b_cu0_precision_and_tolerance_study.md); análisis de `CU2` §3.3 y §3.8.3 (F-CU2-1, F-CU2-9); `L-CU1-TRANSLATION-RESOLUTION` ([registro de cierre de `CU1`](../validation/pre_g9b_cu1_closeout_record.md)) | — | C | no |
| SM-C-8 | Presupuestos de cálculo y fallo cerrado: `NOT_ESTABLISHED` y `UNRESOLVED` | `IMPLEMENTACIÓN VERIFICADA` en la métrica de `CU1` (presupuesto de 16 384 cajas; aprobado); `DISEÑO` en la existencia (`CU2`) | `locus-v2-metrics.md` §25; diseño de `CU2` §8 | `kernel/locus/metric/MetricGuaranteeClass2D`, `MetricEnclosure2D`; `PreG9BCu1MetricClassesTest` | C | no |
| SM-C-9 | Totalidad estructural: certificación sin cajas de los programas de operaciones totales | `DISEÑO` (`CU2`, candidato) | diseño de `CU2` §4.2; A2.2-R | — | C | no |
| SM-C-10 | Aislamiento de raíces polinómicas por tramos monótonos (ceros de la derivada) y bisección | `IMPLEMENTACIÓN VERIFICADA` (aprobado), con evidencia en coma flotante, no certificada; se usa también para los puntos estacionarios de la distancia al cuadrado en la interacción | `locus-v2-intersections.md` 1.2; ADR 0008 | `kernel/locus/intersection/PolynomialRootIsolation2D`; `kernel/locus/interaction/LocusPointInteractionResolver2D`; `PolynomialRootIsolation2DTest`, `G8BIntersectionKernelTest` | C | no |
| SM-C-11 | Redondeo dirigido con transformaciones exactas (TwoSum y TwoProduct de Dekker) y suma compensada | `IMPLEMENTACIÓN VERIFICADA` (`CU1`, aprobado) | `locus-v2-metrics.md` §25 | `kernel/locus/metric/MetricOutwardArithmetic2D`, `LocusMetricAggregator2D`; `PreG9BCu1MetricClassesTest` | C | no |

### 6.4 D. Curvas, métricas y topología

| Id | Método | Estado | Especificación o ADR | Código y pruebas | Ap. | Ficha |
|---|---|---|---|---|---|---|
| SM-D-1 | Longitud de arco, variación total de la función trazada, con clases de garantía | `IMPLEMENTACIÓN VERIFICADA` (`CU1`, aprobado) | `locus-v2-metrics.md` 1.5, §22–§25; `locus-v2-public-surface.md` (`LocusLength`); ADR 0007 y ADR 0037 | `org/geogebra/common/kernel/commands/CmdLocusLength` (con cabecera GeoCeDG); `kernel/algos/AlgoLocusMetricV2`; `kernel/locus/metric/LocusMetricEngine2D`, `PolicyV2LocusMetricCapability2D`, `MetricGuaranteeClass2D`; `PreG9BCu1MetricClassesTest`, `LocusMetricProductiveNumericalTest`, `PreG9BCu1AcceptanceTest` (Desktop) | D | sí |
| SM-D-2 | Longitud estructural o analítica (clase 1) | `IMPLEMENTACIÓN VERIFICADA` (`CU1`, aprobado) | `locus-v2-metrics.md` §25.1 | `kernel/locus/metric/StructuralLocusMetric2D`; `PreG9BCu1MetricClassesTest` | D | SM-D-1 |
| SM-D-3 | Cotas certificadas de longitud (clase 2) a partir de envolventes de la primera derivada | `IMPLEMENTACIÓN VERIFICADA` (`CU1`, aprobado; D-CU1-R4) | `locus-v2-metrics.md` §25 | `kernel/locus/metric/CertifiedConeEnclosure2D`, `MetricEnclosure2D`; `PreG9BCu1MetricClassesTest` | D | SM-D-1 |
| SM-D-4 | Cuadratura adaptativa y estimación del error (clase 3) | `IMPLEMENTACIÓN VERIFICADA` (`CU1`, aprobado; contrato de estimación D-CU1-2) | `locus-v2-metrics.md` §25.12 | `kernel/locus/metric/LocusMetricIntegrator2D`, `LocusMetricEstimator2D`; `PreG9BCu1EstimatorTest` | D | SM-D-1 |
| SM-D-5 | Corrección de Richardson y sus hipótesis | `IMPLEMENTACIÓN VERIFICADA` en la ruta de producción del evaluador (`CU1`, aprobado): `LocusMetricEstimator2D` corrige con `fine + (fine − coarse)/3`; la corrección de Simpson de `LocusMetricIntegrator2D` solo es alcanzable desde `DifferentialLocusMetricCapability2D`, que instancian las pruebas. Sus hipótesis se registran y no certifican (D-CU1-R2) | `locus-v2-metrics.md` §25.3 | `kernel/locus/metric/LocusMetricEstimator2D`, `LocusMetricIntegrator2D`; `PreG9BCu1EstimatorTest` | D | SM-D-1 |
| SM-D-6 | Aliasing y límites de cualquier muestreo finito | `IMPLEMENTACIÓN VERIFICADA` de la política métrica v2, que corrige H10 (`CU1`, aprobado); los límites generales, en `INVESTIGACIÓN` | [análisis de aliasing de `CU1`](pre_g9b_cu1_metric_aliasing_analysis.md); `locus-v2-metrics.md` | `kernel/locus/metric/PolicyV2LocusMetricCapability2D`; `PreG9BCu1EstimatorTest`, `PreG9BCu0SyntheticCharacterizationTest` (Desktop) | D | no |
| SM-D-7 | Componentes continuas de existencia: arcos, extremos laterales y componentes puntuales | `IMPLEMENTACIÓN VERIFICADA` de la estructura de existencia (aprobado, ADR 0027), hoy con el certificado de compatibilidad que `CU2` retira; arcos, extremos laterales y componentes puntuales en `DISEÑO` (`CU2`, candidato; D-CU2-19) | ADR 0027; `locus-v2-existence-components.md` 1.0; diseño de `CU2` §3; A2.1-R | `kernel/locus/LocusExistenceStructure2D`; `PreG9BS1R2ExistenceStructureTest` | D | sí |
| SM-D-8 | Periodicidad, costuras y parametrizaciones circulares; autoridad única de cierre | hoy cinco predicados distintos deciden el cierre (defecto B07); periodicidad estructural y autoridad única de cierre en `DISEÑO` (`CU2`, candidato) | `locus-v2-intersections.md`; diseño de `CU2` §4.4; A2.2-R y A2.5 | `euclidian/draw/LocusRenderCache2D`, `export/SemanticCurveExportAdapter2D`, `kernel/locus/metric/LocusMetricRouteResolver2D` (cierre actual) | D | SM-D-7 |
| SM-D-9 | Límites laterales, discontinuidades y eventos estructurales (`ORIGIN_WRAP`) | `DISEÑO` (`CU2`, candidato; D-CU2-3) | [diseño del evento](pre_g9b_cu2_oriented_length_event_design.md); A2.3-R | — | D | sí |
| SM-D-10 | Longitud orientada y desarrollo de superficies | `IMPLEMENTACIÓN VERIFICADA` de `LocusLength` y su recorrido orientado (aprobado); el desarrollo `e_2` es una construcción del autor cuya representación certificada está en `DISEÑO` (`CU2`) | `locus-v2-metrics.md`; [análisis de la longitud orientada de `CU0`](pre_g9b_cu0_oriented_length_analysis.md) | `kernel/algos/AlgoLocusMetricV2`, `kernel/locus/metric/LocusMetricRouteResolver2D`; `G9U0MetricPositionTest` | D | SM-D-9 |
| SM-D-11 | Pliegues, esquinas, cúspides y contactos | pliegues y esquinas en `DISEÑO` (`CU2`); contactos tangentes en `DISEÑO` candidato para `CU3`, no aprobado tal como está formulado (`OBS-CU0-CU3-TANGENT-EXISTENCE`); cúspides sin estudiar | diseño de `CU2` §4.6 y §5; [propuesta de materialización tangente de `CU0`](pre_g9b_cu0_tangent_materialization_proposal.md) | — | D | SM-C-6 |
| SM-D-12 | Teselación y aproximaciones de dibujo y exportación con fidelidad declarada | `IMPLEMENTACIÓN VERIFICADA` del dibujo adaptativo actual y de las aproximaciones de exportación DXF y LaTeX (aprobado), cuyo error es estimado (`ESTIMATED_ERROR`) y no certificado; las reglas LaTeX solo están en registros de validación (D-CU2-11); política de dibujo v2, topología certificada y fidelidad declarada en `DISEÑO` (`CU2`) | `locus-v2-presentation.md` 1; `dxf-curve-fidelity-and-approximation.md`; ADR 0014; [consumo y representación de `CU2`](pre_g9b_cu2_consumption_and_representation.md) §5–§6 | `euclidian/draw/LocusRenderCache2D`; `export/AdaptiveCurveApproximationBuilder2D`, `export/SemanticCurveExportAdapter2D`; Desktop `org/geocedg/desktop/export/LatexSemanticExportSupport`; `G9X1AdaptiveCurveExportTest`, `LocusV2RenderHardeningTest`, `PreG9BR6PlusCLatexExportTest` (Desktop) | D | no |
| SM-D-13 | Curvas spline semánticas (SplineV2) e intersecciones certificadas de pares | `IMPLEMENTACIÓN VERIFICADA` (G9S1 y G9S1-R1, aprobados): base de potencias truncadas con numeradores exactos y resolución numérica de trabajo fijo | `geocedg/specs/curves/semantic-spline-2d.md`; `geocedg/specs/curves/spline-v2-pair-materialization.md`; ADR 0018, ADR 0021 y ADR 0022 | `kernel/algos/AlgoSplineV2`; `kernel/spline/SplineStructuralModel2D`, `SplinePrecisionSolve2D`; `G9S1SemanticSpline2DTest`, `G9S1R1SplinePairMaterializationTest` | D | no |

### 6.5 E. Semántica dinámica CeDG

| Id | Método | Estado | Especificación o ADR | Código y pruebas | Ap. | Ficha |
|---|---|---|---|---|---|---|
| SM-E-1 | Identidad geométrica frente a identidad de representación: identidades durable, de selección y de componente | `IMPLEMENTACIÓN VERIFICADA` de la identidad durable y de rama (aprobado); separación de las tres identidades y linaje estructural en `DISEÑO` (`CU2`) | `locus-v2-semantics.md`, «Branch identity and lineage»; ADR 0010; diseño de `CU2` §7; A2.1-R | `kernel/spatial/identity/PersistentGeoId`; `kernel/locus/LocusComponentLineage2D`, `LocusLineage2D` | E | no |
| SM-E-2 | Selección de ramas e intersecciones; salidas efectivas en raíces coincidentes | `INVESTIGACIÓN` del comportamiento heredado (F-CU2-8, F-CU2-13, F-CU2-14); lectura del estado de selección y contrato de salidas efectivas en `DISEÑO` (`CU2`, candidato; D-CU2-17 y D-CU2-18) | diseño de `CU2` §4.1 y §4.7; A2.2-R | clases heredadas `org/geogebra/common/kernel/algos/AlgoIntersectLineConic`, `AlgoIntersectSingle` y `AlgoIntersect` | E | sí |
| SM-E-3 | Continuidad y correspondencia semántica: linaje estructural y resolución de selectores | `DISEÑO` (`CU2`, candidato; E-3) | diseño de `CU2` §7; A2.1-R | hoy `kernel/locus/LocusComponentLineage2D` usa los bits de los extremos | E | no |
| SM-E-4 | Certificación local frente a completitud global; niveles de evidencia | `DISEÑO` (`CU2`, candidato) sobre la dirección DA-1 aprobada y el ADR 0027 | ADR 0027; [propuesta E de `CU0`](pre_g9b_cu0_hybrid_certification_proposal.md); diseño de `CU2` §2; ADR 0038 (candidato) | `kernel/locus/LocusExistenceStructure2D` | E | sí |
| SM-E-5 | Propagación de validez y dependencias en el grafo: composición de loci dependientes y anidados | `IMPLEMENTACIÓN VERIFICADA` de la composición anidada (`CU1`, aprobado); composición de la existencia en `DISEÑO` (`CU2`) | `locus-v2-semantics.md`, «Dependency graph, revisions and nested composition»; ADR 0027 | `kernel/algos/AlgoNestedLocusV2`, `AlgoDependentPointLocusV2`; `LocusMetricProductiveNestedTest`, `PreG9BCu1RevisionAndSessionTest` | E | no |
| SM-E-6 | Existencia matemática frente a disponibilidad computacional y de representación | `DISEÑO` (`CU2`, candidato) | diseño de `CU2` §3.4; A2.1-R; ADR 0038, decisión 11 | — | E | sí |
| SM-E-7 | Aproximaciones de dibujo y exportación sin autoridad geométrica | principio de arquitectura (`AGENTS.md` §4) aplicado en la exportación semántica implementada; autoridad única de cierre en `DISEÑO` (`CU2`) | `geocedg/specs/export/geometry-export-foundation.md`; [consumo de `CU2`](pre_g9b_cu2_consumption_and_representation.md) §1 | véase SM-D-12 | E | no |
| SM-E-8 | Revisiones semánticas, corte reconstruible y huella exacta | `IMPLEMENTACIÓN VERIFICADA` (`CU1`, aprobado), con el hallazgo F-CU2-12 sobre la exactitud de la huella (D-CU2-20) | `locus-v2-semantics.md` 1.2, enmienda de `CU1`; ADR 0006 | `kernel/locus/ReconstructibleLocusEvaluator2D`, `ReconstructionSliceFingerprint2D`; `PreG9BCu1RevisionAndSessionTest` | E | sí |
| SM-E-9 | Continuación semántica de las raíces de intersección; fase y rango intrínsecos deterministas de las raíces públicas | `IMPLEMENTACIÓN VERIFICADA` (aprobado) | `locus-v2-intersections.md` 1.2; ADR 0008 y ADR 0017 | `kernel/locus/intersection/LocusIntersectionSolver2D`; `kernel/algos/AlgoLocusIntersectionV2`; `G8BIntersectionKernelTest`, `G9U0R4IntersectionAdmissibilityContinuationTest` | E | no |
| SM-E-10 | Resolución semántica de direcciones inversas y estado interactivo de los puntos sobre loci | `IMPLEMENTACIÓN VERIFICADA` (aprobado) | `locus-v2-point-interaction.md` 2; ADR 0019 | `kernel/algos/AlgoSemanticLocusPoint2D`; `kernel/locus/interaction/LocusPointInteractionResolver2D`; `G9U0R6SemanticLocusPointInteractionTest`, `PostG9U1A6OptionalBranchPointTest` | E | no |
| SM-E-11 | Transformaciones de semejanza de loci con herencia explícita de su semántica | `IMPLEMENTACIÓN VERIFICADA` (aprobado) | `locus-v2-similarity-transformations.md` | `kernel/algos/AlgoLocusSimilarityTransform2D`; `kernel/locus/LocusSimilarityTransform2D`; `G9U0R5SimilarityTransformationsTest` | E | no |

### 6.7 Discrepancias de estado observadas

Al reunir la evidencia del inventario aparecen cabeceras de estado anteriores al código o a los
registros de cierre. No se corrigen aquí, porque la instrucción no autoriza modificar
especificaciones ni ADR aprobados; quedan como lagunas para el expediente de `CU10` (§7, punto 9):

- el ADR 0028 dice «Proposed», aunque sus dos fases (`PRE-G9B-R2-E0/E1` y `PRE-G9B-R3-X1`)
  cerraron `PASS — AUTHOR APPROVED`;
- el ADR 0022 dice que su implementación está pendiente de revisión autoral, mientras que
  `spline-v2-pair-materialization.md` declara aprobadas sus correcciones A y B;
- las cabeceras de `locus-v2-presentation.md`, `dxf-curve-fidelity-and-approximation.md` y
  `g9-spatial-projection-semantics.md` dicen que la implementación no está iniciada o autorizada,
  aunque hay código y evidencia de cierre aprobada (G9U0-R2, G9X1 y G9A1–G9A3);
- `locus-v2-existence-components.md` §9 nombra un `LocusContinuousDomainCertifier2D` que no
  existe en el código;
- `semantic-spline-2d.md` describe todavía la ruta anterior de Simpson y Richardson de la
  métrica.

### 6.6 Fichas iniciales

Fichas de los métodos centrales de `CU0`–`CU2`. Las demás se completan con el mantenimiento de §8
y, a más tardar, en el expediente de `CU10`.

#### SM-B-1 — Discriminante recta–circunferencia y par de tangentes

| Campo | Contenido |
|---|---|
| Descripción matemática | Para la recta que pasa por un punto fijo `P`, exterior a una circunferencia `c`, y por un punto `B`, el discriminante `D` de la intersección es, salvo un factor positivo, el producto de las distancias con signo de `B` a las dos tangentes desde `P` a `c`: la recta corta `c` si y solo si `B` está en el doble ángulo de esas tangentes. Si `B` recorre una circunferencia, cada factor es `h − r·cos(t − τ)` y las raíces tienen la forma cerrada `τ ± arccos(h/r)` |
| Alcance y generalidad | general para una recta por un punto fijo exterior a una cónica, mediante la ecuación del par de tangentes (`S·S₁₁ = S₁²`); en GeoCeDG es referencia analítica y explicación de la mordedura, no una capacidad implementada |
| Hipótesis | `P` y la circunferencia no dependen del conductor; `P` es exterior |
| Degeneraciones y límites | `P` sobre la circunferencia (tangentes coincidentes); `B = P`; conductor tangente a una de las tangentes (raíz doble, valor crítico); no se aplica si `P` o la circunferencia dependen del conductor |
| Ejemplo o modelo | primera familia (`o`, `v`, `w`, `a_1`) del modelo del autor en el corpus `models/regression/pre-g9b-cu/`; configuraciones sintéticas enteras de la [matriz de `CU2`](../validation/pre_g9b_cu2_validation_matrix.md) §1 |
| Referencias | Salmon; Monge |
| Cobertura y cuestiones abiertas | demostración general para cónicas; relación con la clasificación clásica de las intersecciones de superficies (penetración, mordedura y tangencia); generalización a superficies regladas proyectadas desde un vértice |

#### SM-B-2 — Clasificación de las raíces de un predicado de validez

| Campo | Contenido |
|---|---|
| Descripción matemática | Con `P(t) ≥ 0` como condición de existencia: una raíz de multiplicidad impar con cambio de signo es una transición, cuyo extremo incluye o excluye la raíz según el predicado; una de multiplicidad par con `P ≥ 0` a ambos lados es un parámetro singular interior (la existencia continúa y, con multiplicidad 2, `√P` es continua y la selección puede tener una esquina); una de multiplicidad par con `P ≤ 0` a ambos lados es un punto aislado de existencia, una componente puntual |
| Alcance y generalidad | cualquier predicado escalar continuo de existencia sobre un dominio de una dimensión. La continuidad de la construcción exige además que la salida que define el punto trazado esté definida (SM-E-2) |
| Hipótesis | el predicado decide la existencia cerca de la raíz y la selección es la misma regla a ambos lados |
| Degeneraciones y límites | varios predicados que se anulan a la vez; multiplicidad 4 o mayor, sin esquina; salidas anuladas por reglas heredadas |
| Ejemplo o modelo | penetración, valor crítico y mordedura del modelo del autor; tangencia exterior sintética (V-CRIT-5 y V-POINT-1 a V-POINT-6 de la matriz de `CU2`) |
| Referencias | por completar |
| Cobertura y cuestiones abiertas | demostración de la continuidad y de las tangentes laterales en la esquina; multiplicidades altas; relación con los puntos dobles y aislados de las curvas algebraicas |

#### SM-B-5 — Certificado algebraico exacto: racionales, máximo común divisor y Sturm

| Campo | Contenido |
|---|---|
| Descripción matemática | Un predicado de primer nivel depende racionalmente de `cos t` y `sin t`. Con `u = tan(t/2)` se convierte en un polinomio `p(u)` con coeficientes racionales exactos, porque las constantes binary64 capturadas son racionales diádicos. La parte libre de cuadrados `p / gcd(p, p′)` y la sucesión de Sturm cuentan las raíces reales distintas de cada intervalo; la bisección exacta las aísla, y la multiplicidad y el signo entre raíces se deciden exactamente; `t = ±π` se trata aparte |
| Alcance y generalidad | Sturm y el máximo común divisor valen para cualquier polinomio en una variable con coeficientes exactos. El diseño de `CU2` los aplica solo a predicados de primer nivel con el conductor en una circunferencia o un segmento; no a predicados posteriores a una raíz cuadrada (SM-B-7) ni a funciones no algebraicas |
| Hipótesis | constantes capturadas exactas; dependencia racional del conductor; presupuestos de bits y de bisecciones |
| Degeneraciones y límites | predicado idénticamente nulo (identidad estructural); coeficientes grandes; raíces muy próximas (`2⁻¹⁰⁰` en las pruebas sintéticas). Certifica la configuración capturada, nunca la relación ideal que la construcción calcula con redondeo |
| Ejemplo o modelo | valor crítico de la mordedura y binary64 contiguos ([análisis de `CU2`](pre_g9b_cu2_author_model_certification_analysis.md) §3.8; V-CRIT-1 a V-CRIT-10) |
| Referencias | Sturm; Yun; von zur Gathen y Gerhard; Basu, Pollack y Roy; Collins y Akritas, como alternativa |
| Cobertura y cuestiones abiertas | complejidad en bits; comparación con el aislamiento por la regla de Descartes; eliminación algebraica para predicados de nivel superior |

#### SM-C-1 — Aritmética de intervalos con redondeo exterior

| Campo | Contenido |
|---|---|
| Descripción matemática | Envolventes del valor y de la primera derivada de cada operación de un programa de construcción sobre una caja del parámetro, con redondeo hacia fuera: el rango verdadero queda contenido en la envolvente. En la clase v3 (diseño de `CU2`), cada caja da `VALID`, `UNIFORMLY_INVALID` o `UNRESOLVED` |
| Alcance y generalidad | funciones construidas con operaciones cuya extensión de intervalos es correcta. GeoCeDG la tiene implementada en las clases v1 y v2 del modelo de construcciones certificadas y en las envolventes métricas de `CU1`; la clase v3 es diseño |
| Hipótesis | extensiones de intervalos correctas y redondeo dirigido |
| Degeneraciones y límites | sobreestimación por dependencia; una envolvente que contiene el cero no prueba nada; un predicado de igualdad solo es uniformemente inválido por una identidad estructural. Hay dos implementaciones independientes, sin biblioteca común: la de las curvas ensancha un ulp cada operación y la métrica decide el sentido del redondeo con TwoSum y Dekker (SM-C-11); las envolventes trigonométricas suponen que `StrictMath` yerra como mucho un ulp; el paquete de intervalos heredado de GeoGebra no se usa como certificado |
| Ejemplo o modelo | certificación de intersecciones de Locus V2; cajas del modelo del autor (diseño) |
| Referencias | Moore (1966); Neumaier; Tucker |
| Cobertura y cuestiones abiertas | subdivisión, presupuestos y fallo cerrado; aritmética afín y formas afines, hoy una limitación registrada de `CU1` (`L-CU1-AFFINE-FORMS`) |

#### SM-C-2 — Aislamiento certificado de raíces simples

| Campo | Contenido |
|---|---|
| Descripción matemática | Con el operador de Krawczyk `K(X) = y − C·f(y) + (I − C·f′(X))(X − y)`, si `K(X)` está en el interior de `X`, `X` contiene un único cero. En una variable, un cambio de signo con una envolvente de la derivada que excluye el cero prueba una raíz simple única en el corchete |
| Alcance y generalidad | raíces simples de funciones o sistemas diferenciables. Implementado en la certificación de las intersecciones de SplineV2; diseñado para las transiciones de los predicados de validez de `CU2` |
| Hipótesis | derivada no singular en la caja |
| Degeneraciones y límites | raíces múltiples o casi múltiples: el operador no contrae (SM-C-3) |
| Ejemplo o modelo | intersecciones de pares de SplineV2 (`G9S1R1SplinePairIntervalCertificationTest`); fronteras de la mordedura del modelo del autor (diseño, V-TR-1) |
| Referencias | Krawczyk; Moore (1977); Neumaier; Rump (2010, *Acta Numerica*), teorema 13.3, que cita el propio código |
| Cobertura y cuestiones abiertas | precondicionado; relación con el método del punto estacionario (SM-C-4) |

#### SM-C-3 — Raíces múltiples y procedimientos verificados con estructura añadida

| Campo | Contenido |
|---|---|
| Descripción matemática | El aislamiento ordinario de raíces simples no certifica una raíz múltiple: con datos aproximados, un cero doble no es estable frente a perturbaciones arbitrariamente pequeñas. Los procedimientos verificados para raíces múltiples añaden estructura (multiplicidad conocida, deflación, factorización o información derivativa) o prueban que un problema perturbado próximo tiene una raíz múltiple |
| Alcance y generalidad | alternativa estudiada en `CU2` y no elegida para los predicados de primer nivel, cuyos datos exactos permiten decidir la multiplicidad de la configuración capturada (SM-B-5) |
| Hipótesis | las de cada procedimiento |
| Degeneraciones y límites | certifican un problema próximo, no el capturado, salvo que la estructura añadida lo identifique |
| Ejemplo o modelo | valor crítico de la mordedura (técnica C del análisis de `CU2` §3.8.2) |
| Referencias | Rump y Graillat; Neumaier |
| Cobertura y cuestiones abiertas | cuándo convienen frente al certificado exacto: predicados no algebraicos o de nivel superior |

#### SM-C-6 — Pliegues certificados por simetría estructural y chorros de segundo orden

| Campo | Contenido |
|---|---|
| Descripción matemática | Para una curva plana, `F′(t₀) = 0` son dos ecuaciones con una incógnita: genéricamente no tienen solución, y una envolvente de `F′` que contiene el cero no prueba nada. Un pliegue se certifica con cinco pruebas: existencia por una involución exacta `σ` del dominio con `F ∘ σ = F`; aislamiento por una componente de `F″` que excluye el cero; no degeneración, `F″(t₀) ≠ 0`; continuidad; y tangentes laterales opuestas |
| Alcance y generalidad | curvas con una simetría estructural verificable sobre el programa capturado; sin simetría, el pliegue no se certifica y el dibujo se refina con diagnóstico |
| Hipótesis | simetría verificada por igualdades racionales exactas de las constantes capturadas y por una tabla de operaciones equivariantes |
| Degeneraciones y límites | contacto de orden superior (`F″(t₀) = 0`); simetría aproximada |
| Ejemplo o modelo | `e_1` del modelo del autor, con pliegues en `0` y `π` (análisis de `CU2` §4.3; V-FOLD-1 a V-FOLD-4) |
| Referencias | do Carmo |
| Cobertura y cuestiones abiertas | puntos estacionarios sin simetría; cúspides; relación con los puntos de contorno aparente de las proyecciones |

#### SM-D-1 — Longitud de arco con clases de garantía

| Campo | Contenido |
|---|---|
| Descripción matemática | `L = ∫ ‖F′(t)‖ dt` sobre una componente, con cuatro clases de garantía: estructural, envolvente certificada (cotas inferior y superior), estimación numérica (cuadratura adaptativa con corrección de Richardson, cuyo defecto entre niveles estima el error restante) y no establecida. Las hipótesis de un estimador no son una certificación (D-CU1-R2) |
| Alcance y generalidad | curvas rectificables trazadas por Locus V2; las envolventes certificadas tienen un presupuesto versionado de 16 384 cajas (D-CU1-R4) |
| Hipótesis | componente certificada; para las estimaciones, regularidad y ausencia de aliasing |
| Degeneraciones y límites | aliasing de un muestreo finito (H10); singularidades; alcance limitado de la envolvente de clase 2 (`L-CU1-CLASS2-REACH`); resolución de las traslaciones en binary64 (`L-CU1-TRANSLATION-RESOLUTION`) |
| Ejemplo o modelo | `o_1` y `e_2` del modelo del autor |
| Referencias | Richardson y Gaunt; Davis y Rabinowitz; `cedg.reference.tools-and-oblique-cone-2025` |
| Cobertura y cuestiones abiertas | variación total; demostración de las cotas; condiciones de validez de la extrapolación |

#### SM-D-7 — Componentes de existencia: arcos, extremos laterales y componentes puntuales

| Campo | Contenido |
|---|---|
| Descripción matemática | El dominio del conductor se recubre con componentes certificadas (intervalos, o arcos con coordenada de arco y extremos con lado, inclusión, límite lateral y fuente), componentes puntuales `{t*}`, no existencia certificada y bandas no resueltas. La completitud es un recubrimiento certificado, nunca el resultado de detener un muestreador |
| Alcance y generalidad | dominios de una dimensión (circunferencia o segmento); las construcciones de varios parámetros no están cubiertas |
| Hipótesis | fronteras certificadas (SM-B-2, SM-C-2) y continuidad de la función trazada |
| Degeneraciones y límites | arcos abiertos de longitud completa; puntos aislados; bandas no resueltas |
| Ejemplo o modelo | mordedura (un arco que cruza la costura); `e_2` (arco abierto de longitud `2π`); tangencia exterior sintética (componente puntual) |
| Referencias | ADR 0027; Basu, Pollack y Roy (conjuntos semialgebraicos) |
| Cobertura y cuestiones abiertas | dominios de dimensión superior; relación con la descomposición en celdas |

#### SM-D-9 — Eventos estructurales de una función métrica y desarrollo con longitud orientada

| Campo | Contenido |
|---|---|
| Descripción matemática | La longitud orientada desde un origen fijo sobre una componente cerrada es continua salvo en el origen, donde salta la longitud total. El evento `ORIGIN_WRAP` se certifica con hechos estructurales (dominio, continuidad y salto positivo probado por una cuerda con redondeo exterior), sin usar el valor de la longitud, y el dependiente es una componente abierta cortada en el origen, con dos extremos laterales distintos |
| Alcance y generalidad | funciones métricas de curvas cerradas con un origen fijo y una política de misma posición declarada; los demás casos están en el [diseño del evento](pre_g9b_cu2_oriented_length_event_design.md) §4.3 |
| Hipótesis | componente fuente periódica cerrada y certificada; origen independiente del conductor |
| Degeneraciones y límites | origen que depende del conductor; fuente solo localmente certificada; salto no certificable |
| Ejemplo o modelo | `e_2` del modelo del autor (V-E2-1 a V-E2-9) |
| Referencias | `cedg.reference.intersection-flattening-2023`; `cedg.reference.developable-surfaces-manuscript`; `cedg.reference.tools-and-oblique-cone-2025` |
| Cobertura y cuestiones abiertas | desarrollo de superficies desarrollables en general; isometría local; elección del corte |

#### SM-E-2 — Selección de ramas de intersección y salidas efectivas

| Campo | Contenido |
|---|---|
| Descripción matemática | La salida `i` de una intersección recta–circunferencia es la raíz bruta `permutation[i]`, ordenada por el parámetro a lo largo de la dirección algorítmica de la recta; la permutación es estado del algoritmo y se inicializa por cercanía al crear, cargar y recalcular. Una segunda salida sin etiqueta se anula si coincide con la primera dentro de `10⁻⁸`. Se separan siete hechos: existencia, multiplicidad, identidad semántica de la solución, definición efectiva de la salida, disponibilidad en el corte, continuidad de los límites laterales y continuidad de la construcción |
| Alcance y generalidad | algoritmos heredados recta–cónica e intersección simple; cada algoritmo heredado tiene su regla, que no se generaliza sin inspeccionarla |
| Hipótesis | modo no continuo; selección leída del algoritmo, nunca de una muestra |
| Degeneraciones y límites | raíces coincidentes; primera inicialización en una raíz doble; conductor sin intersección al crear; permutación invertida |
| Ejemplo o modelo | `{Q, R}`, `H_1`, `G_1` y `J/K` del modelo del autor (análisis de `CU2` §3.4 y §3.9; V-SEL y V-COINC) |
| Referencias | Kortenkamp (continuidad en geometría dinámica); código heredado de GeoGebra |
| Cobertura y cuestiones abiertas | el problema general de la continuidad en geometría dinámica; selección continua de raíces coincidentes (D-CU2-18) |

#### SM-E-4 — Certificación local frente a completitud global

| Campo | Contenido |
|---|---|
| Descripción matemática | Cuatro niveles de evidencia (`POINT_VALID`, `EXPLORATORY`, `LOCAL_CERTIFIED` y `COMPLETE`) y cinco naturalezas de prueba. La admisibilidad local es independiente de la completitud global: un consumidor local acepta una componente certificada y uno global exige `COMPLETE` |
| Alcance y generalidad | la semántica de existencia de Locus V2; trasladable a cualquier sistema de geometría dinámica con objetos definidos sobre dominios de parámetro |
| Hipótesis | ninguna adicional |
| Degeneraciones y límites | presupuestos agotados (`UNRESOLVED`); evidencia exploratoria sin autoridad |
| Ejemplo o modelo | V-LVL-1 a V-LVL-3 y V-COMPL-1 de la matriz de `CU2` |
| Referencias | ADR 0027; ADR 0038 (candidato) |
| Cobertura y cuestiones abiertas | semántica de fallo cerrado |

#### SM-E-6 — Existencia matemática, disponibilidad numérica y representación

| Campo | Contenido |
|---|---|
| Descripción matemática | Cuatro capas con garantías distintas: existencia matemática, topología certificada, disponibilidad numérica y disponibilidad de representación. Ninguna se deduce de otra: una evaluación válida no prueba la existencia, un fallo no prueba la no existencia y la existencia certificada no garantiza las coordenadas |
| Alcance y generalidad | cualquier objeto calculado; en GeoCeDG, Locus V2 (diseño de `CU2`) |
| Hipótesis | ninguna adicional |
| Degeneraciones y límites | puntos del evaluador dentro de huecos exactos; fallos de evaluación con existencia certificada; extremos no evaluables |
| Ejemplo o modelo | V-AVAIL-1 a V-AVAIL-6 y V-POINT-4 de la matriz de `CU2` |
| Referencias | por completar |
| Cobertura y cuestiones abiertas | fidelidad declarada de las aproximaciones de dibujo y exportación |

#### SM-E-8 — Revisiones semánticas, corte reconstruible y huella exacta

| Campo | Contenido |
|---|---|
| Descripción matemática | Un Locus V2 se evalúa sobre un corte de la construcción sincronizado una vez por revisión. La revisión cambia con las entradas de definición y no con la posición del conductor, de modo que mover el conductor no recalcula los certificados. La huella de la revisión debe ser exacta |
| Alcance y generalidad | implementado en `CU1`; la exactitud de la huella es un hallazgo de `CU2` (F-CU2-12) |
| Hipótesis | entradas con estado determinista |
| Degeneraciones y límites | estado heredado que cambia con el conductor (primera inicialización en una raíz doble, D-CU2-18); estado de selección fuera de la huella y literales angulares sin texto original reconstruidos con otro valor (F-CU2-12, corregido; D-CU2-20-R) |
| Ejemplo o modelo | puertas GC-1 y GC-2 y V-REV-EXACT-1 de la matriz de `CU2` |
| Referencias | por completar |
| Cobertura y cuestiones abiertas | semántica de revisiones y reutilización de certificados |

## 7. Expediente de `CU10`: `SCIENTIFIC_METHODS_EDITORIAL_HANDOFF`

Antes de reanudar la edición del libro, el cierre del track produce un expediente de transferencia
científica, preparado para la revisión autoral y para su transferencia posterior al repositorio
del libro. Contiene:

1. el inventario consolidado de métodos, con el estado final de cada uno;
2. el alcance matemático y la generalidad de cada método, en los tres niveles de §3;
3. los vínculos a definiciones, demostraciones, especificaciones, ADR, implementaciones y
   pruebas;
4. la clasificación de resultados rigurosos frente a estimaciones, con la naturaleza de su
   evidencia;
5. los tratamientos de las degeneraciones;
6. ejemplos constructivos reproducibles, con su modelo y su versión;
7. una propuesta estructurada de apéndices científicos, que el autor revisa y que la fase
   editorial puede cambiar;
8. la bibliografía y las referencias por completar;
9. las lagunas pendientes de investigación o de redacción.

Condiciones:

- cada método del inventario tiene su ficha completa o la razón por la que no la tiene;
- su preparación forma parte del alcance documental obligatorio de `CU10` y no sustituye ninguna
  verificación del producto;
- no inicia la fase editorial: la actualización de la base técnica de `geocedg_book`, la
  incorporación de apéndices, figuras y capítulos y la continuación del libro necesitan una
  autorización editorial separada.

## 8. Mantenimiento del registro

- Al cerrar cada paquete científico del track (`CU2`, `CU3` y los que traigan métodos nuevos), su
  candidato actualiza las filas y fichas de los métodos que implementa, diseña o estudia: estado,
  evidencia, ejemplos y cuestiones abiertas.
- Los cambios son de trazabilidad y de contenido científico, nunca texto editorial del libro.
- Un estado sube solo con evidencia: `IMPLEMENTACIÓN VERIFICADA` exige el commit y la
  verificación de su fase; «aprobado» exige el registro de cierre o la norma aprobada que lo dice.
- El registro no reabre fases cerradas ni cambia sus registros: cita su evidencia.
- Ninguna actualización modifica `geocedg_book`.

## 9. Referencias

**Obras del autor y del proyecto** (identificadores del
[catálogo de referencias](../references/cedg/catalog.yml)):

| Identificador | Obra (título según el catálogo) |
|---|---|
| `cedg.reference.book-2023` | «Modelado parametrico computacional de sistemas 3D mediante Geometria Descriptiva (CeDG): sistema diedrico», 2023 |
| `cedg.reference.foundations-cad-2021` | «Graphical Modelling with Computer Extended Descriptive Geometry (CeDG): Description and Comparison with CAD», 2021 |
| `cedg.reference.spatial-association-2022` | «Asociacion entre vistas ortograficas y objetos 3D en CeDG: prueba de concepto», 2022 |
| `cedg.reference.lsim-preprint-2022` y `cedg.reference.lsim-abstract-2022` | «Intersection between surfaces using Computer Extended Descriptive Geometry (CeDG): application to the focal illumination of a sphere», 2022 |
| `cedg.reference.sheet-metal-2021` | «Comparison of Computer Extended Descriptive Geometry (CeDG) with CAD in the Modeling of Sheet Metal Patterns», 2021 |
| `cedg.reference.intersection-flattening-2023` | «Intersection and Flattening of Surfaces in 3D Models through Computer-Extended Descriptive Geometry (CeDG)», 2023 |
| `cedg.reference.developable-surfaces-manuscript` | «Modeling general developable surfaces in CeDG: a preliminary study», manuscrito |
| `cedg.reference.tools-and-oblique-cone-2025` | «Extension of the Dynamic Geometry Software for CeDG Support and Application to the Accurate Flattening of Developable Spatial Surfaces», 2025 |
| `cedg.reference.discrete-elbows-2025`, `cedg.reference.discrete-elbow-chapter-2024` y `cedg.reference.discrete-elbow-abstract-2023` | modelos 3D de variable discreta en CeDG: codos poligonales de chapa, 2023–2025 |

El catálogo solo contiene obras CeDG. Las referencias clásicas de la tabla siguiente no están en
él; el expediente de `CU10` decide dónde se registran una vez verificadas.

**Bibliografía clásica por verificar.** Datos por comprobar contra la fuente antes de su uso
editorial:

| Tema | Referencia |
|---|---|
| Geometría descriptiva | G. Monge, *Géométrie descriptive*, París, año VII (1799) |
| Par de tangentes y ecuación de Joachimsthal | G. Salmon, *A Treatise on Conic Sections* |
| Aritmética de intervalos | R. E. Moore, *Interval Analysis*, Prentice-Hall, 1966 |
| Operador de Krawczyk | R. Krawczyk, «Newton-Algorithmen zur Bestimmung von Nullstellen mit Fehlerschranken», *Computing* 4 (1969) |
| Existencia por intervalos | R. E. Moore, «A test for existence of solutions to nonlinear systems», *SIAM Journal on Numerical Analysis* 14 (1977) |
| Métodos de intervalos | A. Neumaier, *Interval Methods for Systems of Equations*, Cambridge University Press, 1990 |
| Numérica validada | W. Tucker, *Validated Numerics: A Short Introduction to Rigorous Computations*, Princeton University Press, 2011 |
| Métodos de verificación | S. M. Rump, «Verification methods: Rigorous results using floating-point arithmetic», *Acta Numerica* 19 (2010), citado en `SplinePairIntervalCertification2D` |
| Raíces múltiples verificadas | S. M. Rump y S. Graillat, «Verified error bounds for multiple roots of systems of nonlinear equations», *Numerical Algorithms*, 2010 |
| Transformaciones exactas del redondeo | T. J. Dekker, «A floating-point technique for extending the available precision», *Numerische Mathematik* 18 (1971) |
| Mínimos cuadrados y valores singulares | G. H. Golub y C. F. Van Loan, *Matrix Computations*, Johns Hopkins University Press |
| Bases de Bernstein y envolvente convexa | G. Farin, *Curves and Surfaces for CAGD*, Morgan Kaufmann |
| Sucesiones de Sturm | C. Sturm, «Mémoire sur la résolution des équations numériques», 1829 (publicada en 1835) |
| Descomposición libre de cuadrados | D. Y. Y. Yun, «On square-free decomposition algorithms», SYMSAC 1976 |
| Aislamiento por la regla de Descartes | G. E. Collins y A. G. Akritas, «Polynomial real root isolation using Descartes' rule of signs», SYMSAC 1976 |
| Álgebra computacional | J. von zur Gathen y J. Gerhard, *Modern Computer Algebra*, Cambridge University Press |
| Geometría algebraica real | S. Basu, R. Pollack y M.-F. Roy, *Algorithms in Real Algebraic Geometry*, Springer |
| Extrapolación de Richardson | L. F. Richardson y J. A. Gaunt, «The deferred approach to the limit», *Philosophical Transactions of the Royal Society A* 226 (1927) |
| Integración numérica | P. J. Davis y P. Rabinowitz, *Methods of Numerical Integration*, Academic Press |
| Curvas y superficies | M. P. do Carmo, *Differential Geometry of Curves and Surfaces*, Prentice-Hall, 1976 |
| Continuidad en geometría dinámica | U. Kortenkamp, *Foundations of Dynamic Geometry*, tesis doctoral, ETH Zúrich, 1999 |

## 10. Límites de este registro

- Es inicial y no exhaustivo. Los paquetes posteriores añaden los métodos que empleen o
  investiguen.
- Los estados proceden de la evidencia citada. El registro no vuelve a verificar métodos de fases
  cerradas.
- La agrupación por categorías no es un diseño de apéndices: ese diseño está pendiente de
  preparación y revisión.
- Las fichas de §6.6 resumen el contenido científico disponible; no son texto editorial ni
  sustituyen a las demostraciones que exige §4.
