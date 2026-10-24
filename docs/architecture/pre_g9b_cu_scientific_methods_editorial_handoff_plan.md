# PRE-G9B-CU — Plan de transferencia científica de los métodos de GeoCeDG

| Campo | Valor |
|---|---|
| Carácter | Registro vivo de planificación y trazabilidad científica del mandato editorial E1. No es especificación normativa, no contiene texto editorial y no planifica el libro: la planificación editorial corresponde a `geocedg_book` cuando se autorice esa fase |
| Mandato | instrucción autoral «GeoCeDG — PRE-G9B-CU2. Cierre de coherencia normativa y registro del programa de apéndices científicos» (2026-10-20), §5 |
| Estado | Mandato editorial del autor: **aprobado** (2026-10-20). Diseño detallado de los apéndices: **pendiente de preparación y revisión**. Redacción e incorporación a `geocedg_book`: **no autorizadas todavía**. `selfApproved=false` |
| Registrado en | [plan consolidado](pre_g9b_cu_corrective_usability_plan.md) §2.3, `CU10`, §6 y §8; [hoja de ruta](../roadmap/geocedg_roadmap.md) 5.65, track `PRE-G9B-CU` |
| Libro | `geocedg_book` sin cambios (`78fbc88e9e12346b89d6ded411eaae94de2284b9`) |
| Versión del registro | 1: creación con el cierre de coherencia de `CU2`, sobre la base `cbb7ea8c`. 2: filas y fichas de los métodos de `CU2` actualizadas con su candidato técnico `39741a0b` y la verificación de T13 (§6). 3: las mismas filas y fichas actualizadas con el candidato correctivo `2856f246` del descendiente correctivo de `CU2` y su verificación (§6). 4: las mismas filas actualizadas con el candidato final de la resolución autoral consolidada de 2026-10-23 y las correcciones de su revisión científica focal (§6). 5: cierre de `CU2` (2026-10-24): los estados de sus métodos pasan a aprobados con el [registro de cierre](../validation/pre_g9b_cu2_closeout_record.md), y §3 incorpora el principio transversal 8, que el expediente de §7 recoge (§6). 6: preparación de `CU3` (2026-10-24, candidato documental sin aprobación autoral): filas de los métodos que `CU3` diseña o estudia y métodos nuevos en `DISEÑO` o `INVESTIGACIÓN`, con la evidencia del sondeo de la preparación (§6). 7: aprobación del diseño de `CU3` y autorización de su implementación (2026-10-24): los métodos de `CU3` en `DISEÑO` aprobado, sin código, con la precisión de la autoridad del objetivo (§6). 8: puerta G-M3 y estudio de la autoridad geométrica constructiva de `CU3` (2026-10-24, candidato documental sin aprobación autoral): resultado de la puerta, evidencia nueva de SM-B-8 y métodos nuevos en `INVESTIGACIÓN` (§6). 9: consolidación focal de la autoridad geométrica constructiva de `CU3` (2026-10-24 y 2026-10-25, candidato documental sin aprobación autoral): prototipo y evidencia de SM-B-13, textos normativos candidatos para SM-B-12 y SM-E-14 y método nuevo SM-B-15 en `INVESTIGACIÓN` (§6) |

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
8. **Integridad geométrica frente a precisión numérica.** Principio transversal para el futuro
   libro, incorporado por la decisión autoral de cierre de `CU2` (2026-10-24;
   [registro de cierre](../validation/pre_g9b_cu2_closeout_record.md)). La existencia, la
   identidad constructiva, la topología, las dependencias y la evidencia matemática se preservan y
   no se subordinan a la disponibilidad numérica ni a la fidelidad de una representación
   aproximada: un fallo de cálculo no se convierte en inexistencia, una evaluación válida no prueba
   la existencia, ninguna posición se inventa y una aproximación declara su fidelidad en lugar de
   cambiar la topología. Lo desarrollan SM-A-5 (capas distintas) y SM-E-6 (existencia frente a
   disponibilidad). Ejemplos de `CU2`: V-POINT-4, donde el fallo forzado de la forma límite deja
   la componente puntual con su existencia certificada y su posición `UNAVAILABLE`, y la recupera
   al cesar el fallo; H-T13-C, donde una raíz certificada que el cálculo heredado pierde conserva
   la existencia y la evaluación falla con `COMPUTATIONAL_FAILURE`; V-UND-1, donde una dependencia
   que no se certifica deja `UNRESOLVED` y nunca inexistencia; V-AVAIL-6, donde un resultado
   parcial enumera con su causa lo que no se representa; y el dibujo y la exportación, que
   declaran su fidelidad (`UNCERTIFIED_TURN`, extremos no evaluados) sin unir ni cerrar lo que el
   certificado no prueba. Las cotas demostradas a priori y las pérdidas de cobertura observadas
   experimentalmente se distinguen siempre (por ejemplo, R7-E en el
   [paquete de revisión final](../validation/pre_g9b_cu2_final_review_package.md) §5.1).

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

La versión 2 actualiza las filas y fichas de los métodos que implementa `CU2` con su candidato
técnico `39741a0b44850a32258c2c73370fdc0842b8c666`
([informe](../validation/pre_g9b_cu2_implementation_candidate_report.md),
[registro de verificación](../validation/pre_g9b_cu2_verification_record.md)). En ellas,
«`IMPLEMENTACIÓN VERIFICADA` (`CU2`, candidato)» significa código productivo y pruebas en ese
candidato, verificados con la clase `INTEGRATED_PHASE` de su paso T13, sin aprobación autoral. Cada
fila dice qué parte queda sin prueba o sin implementar.

La versión 3 actualiza esas filas y fichas con el candidato correctivo
`2856f246c61bd51916932262002cdff31e61b565`, descendiente de `39741a0b` en la rama local
`phase/pre-g9b-cu2-corrective`: las decisiones autorales D-CU2-T13-R1, D-CU2-T13-R2, D-CU2-T13-R4 y
D-CU2-T13-R6 (2026-10-22) y D-CU2-T13-R7 (2026-10-23), y el mandato autoral de autonomía correctiva
controlada de 2026-10-23 ([registro de R6](../validation/pre_g9b_cu2_r6_correction_record.md),
[registro de R7](../validation/pre_g9b_cu2_r7_correction_record.md),
[registro de la corrección autónoma](../validation/pre_g9b_cu2_corrective_autonomy_record.md)). Desde
esta versión, «`IMPLEMENTACIÓN VERIFICADA` (`CU2`, candidato)» se refiere a ese candidato,
verificado con las puertas de §12 de ese registro, sin aprobación autoral. Las decisiones de nivel
C pendientes que afectan a un método se citan como C-n de su §9.

La versión 4 actualiza las filas SM-D-1, SM-D-7, SM-D-8, SM-E-2, SM-E-3, SM-E-6, SM-E-8, SM-E-10 y
SM-E-11, y las fichas de SM-D-7, SM-E-2 y SM-E-8, con el candidato final que prepara el cierre de
`CU2`: la resolución autoral consolidada de 2026-10-23 aprobó las
decisiones C-1 a C-12, y su revisión científica focal corrigió R7-A a R7-D y H-CU2A-6 a H-CU2A-11
([paquete de revisión final](../validation/pre_g9b_cu2_final_review_package.md), que da la
identidad del candidato y su verificación). Desde esta versión, «`IMPLEMENTACIÓN VERIFICADA` (`CU2`,
candidato)» se refiere a ese candidato final, sin aprobación autoral; las decisiones C-n ya no
están pendientes.

La versión 5 registra el cierre de `CU2`: el autor lo declaró `PASS — AUTHOR APPROVED` el
2026-10-24 sobre el candidato `6ff1ddf743bb3ee44a4e9a47f7dc238f30d642cb` y su descendiente
documental `098ec04f` ([registro de cierre](../validation/pre_g9b_cu2_closeout_record.md)). Las
filas y fichas de los métodos de `CU2` dicen ahora «(`CU2`, aprobado)»; los párrafos anteriores
conservan su texto histórico. §3 incorpora el principio 8, integridad geométrica frente a
precisión numérica, relacionado con SM-A-5 y SM-E-6. Las limitaciones que aceptó el cierre y el
trabajo futuro que dejó sin autorizar constan en ese registro; ninguna de ellas es una tarea de
`CU3`.

La versión 6 registra la preparación de `CU3` (autorizada el 2026-10-24 sobre `main` =
`e6c908f2`), que es un candidato documental pendiente de revisión autoral
([informe del candidato](../validation/pre_g9b_cu3_preparation_candidate_report.md)).

- **Filas actualizadas:** SM-C-4, SM-C-7 y SM-D-11.
- **Métodos nuevos:**
  - SM-B-11: evaluación exacta de programas de regla y compás con cotas de separación;
  - SM-C-12: qué certifican los intervalos, y chorros del residuo de contacto;
  - SM-D-14: contactos en pliegues frente a tangencias;
  - SM-E-12: puntos estructurales por correspondencia del conductor;
  - SM-E-13: germen de contacto tangente y su ciclo de vida.
- **Estado.** Son `DISEÑO` (candidato de `CU3`) o `INVESTIGACIÓN`. Ninguno tiene código
  productivo, y ninguna fila sube de estado sin la implementación y el cierre de `CU3`.
- **Evidencia.** La del [sondeo de la preparación](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-preparation-characterization.json),
  que es investigación y no certificado.
- **Principio candidato.** La preparación propone para §3 el principio A3.0 de la
  [propuesta normativa de `CU3`](pre_g9b_cu3_normative_proposal.md), en la forma que precisó la
  revisión científica focal de 2026-10-24:
  - los intervalos certifican la existencia de alguna raíz en un intervalo (continuidad y cambio
    de signo certificado) y las desigualdades estrictas;
  - no deciden una igualdad en un parámetro o en un punto prescrito (una incidencia dada, una raíz
    múltiple);
  - esa igualdad se establece por estructura o por aritmética exacta, que son evidencias
    distintas.

  No se añade a §3 sin una decisión autoral.
- **Comprobación final de coherencia** (2026-10-24). Precisa la redacción de SM-B-11 (M3 decide
  en la configuración de decisión que contiene sin cambios la configuración capturada de `L`; una
  recalculación más profunda solo es diagnóstica) y de SM-C-12 (los certificados de conteo de raíces
  son la única base de la regla de conflicto). Ningún estado cambia.

La versión 7 registra la aprobación del diseño de `CU3` y la autorización de su implementación en
dos incrementos con puertas bloqueantes (2026-10-24,
[registro de aprobación](../validation/pre_g9b_cu3_author_design_approval_record.md)):

- **Estado.** Los métodos de `CU3` pasan de `DISEÑO` candidato a `DISEÑO` aprobado, sin código
  todavía. Ninguna fila sube a `IMPLEMENTACIÓN VERIFICADA` sin la implementación, su verificación y
  el cierre de `CU3`.
- **Precisión de la autoridad del objetivo** (puerta G-M3-4). La evaluación exacta decide cada
  incidencia contra el objetivo autoritativo del contrato de intersecciones, nunca contra una
  reconstrucción ideal de él (SM-B-11). La aprobación no certifica por anticipado la viabilidad de
  M3 ni su presupuesto.
- **Resultados y límites.** El expediente de `CU10` conservará la distinción entre integridad
  geométrica, precisión numérica y representación que la instrucción de aprobación pide.

La versión 8 registra la puerta G-M3 y el estudio de la autoridad geométrica constructiva de `CU3`
(2026-10-24), que son candidatos documentales sin aprobación autoral:

- **Puerta G-M3** (SM-B-11; [informe](../validation/pre_g9b_cu3_m3_gate_report.md)). M3 decide
  exactamente en los cinco contactos del modelo, pero los refuta contra los objetivos binary64:
  G-M3-4 no se supera, y el autor confirmó el bloqueo tras una auditoría independiente. El estado
  de SM-B-11 no cambia.
- **Estudio** ([informe](pre_g9b_cu3_constructive_authority_study.md)):
  - la representación binary64 contradice 66 de las 160 relaciones de procedencia del modelo;
  - una semántica constructiva con los puntos sobre caminos anclados las hace exactas todas;
  - una circunferencia tiene tres radios exactos;
  - el giro de 47° marca el límite de la constructibilidad.

  Da evidencia nueva a SM-B-8 y propone en `INVESTIGACIÓN` los métodos SM-B-12, SM-B-13, SM-B-14 y
  SM-E-14.
- **Estado.** Ningún método sube de estado; los nuevos no tienen código productivo.

La versión 9 registra la consolidación focal que el autor autorizó tras revisar favorablemente el
estudio y adoptar A2, B2, C y D como dirección de desarrollo normativo (2026-10-24;
[propuesta normativa candidata](pre_g9b_cu3_constructive_authority_normative_proposal.md)). Es un
candidato documental sin aprobación autoral:

- **SM-B-13** gana un prototipo, el certificado de identidad constructiva, con evidencia
  determinista ([evidencia](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-constructive-identity-evidence.json)).
  Demuestra las cinco incidencias obligatorias como identidades en un cuerpo de funciones
  racionales, con `H` como punto libre y con una regla de frontera automática. Las recomprueba en
  las doce posiciones del estudio, que no son la demostración, y prueba o refuta enunciados de
  construcciones sintéticas.
- **SM-B-12 y SM-E-14** reciben textos normativos candidatos: AUT-1 y AUT-2 para la autoridad y la
  política de documento; AUT-3 y AUT-5 para la integridad y la política `RELAXED`.
- **SM-B-15**, nuevo en `INVESTIGACIÓN`: transferencia de certificados entre una representación
  certificada y el objeto constructivo, por margen, por estructura o por una nueva decisión.
- **Estado.** Ningún método sube de estado. El prototipo es investigación, no producto.

### 6.1 A. Fundamentos de geometría descriptiva computacional

| Id | Método | Estado | Especificación o ADR | Código y pruebas | Ap. | Ficha |
|---|---|---|---|---|---|---|
| SM-A-1 | Objetos espaciales definidos por proyecciones: punto espacial desde vistas ortográficas por mínimos cuadrados (descomposición en valores singulares con umbral de rango) y validación del sistema de proyección (despliegue alrededor de la charnela y cambios de plano) | `IMPLEMENTACIÓN VERIFICADA` en el piloto de punto (track G9A, aprobado; experimental y desactivado por defecto), con evidencia numérica bajo umbral, no certificada; las demás primitivas (recta, plano, circunferencia, cónica y curva espacial) en `DISEÑO` (G9B y G9C, no autorizadas) | `geocedg/specs/spatial/g9-spatial-projection-semantics.md` 1.0 (`NORMATIVE / AUTHOR APPROVED`), §2.2 y §10; ADR 0010 y ADR 0011 | `kernel/spatial/semantic/ProjectionDefinedPointEvaluator`, `ProjectionSystemEvaluator`; `kernel/spatial/runtime/AlgoProjectionDefinedSpatialPoint`; `G9A2ProjectionDefinedPointEvaluatorTest`, `G9A2ProjectionSystemEvaluatorTest`, `G9A3SpatialMutationLifecycleTest`; modelo `models/regression/g9a2-spatial-point-pilot/` | A | no |
| SM-A-2 | Identidad entre el objeto espacial, sus coordenadas intrínsecas de proyección y su colocación en un diagrama CeDG | `IMPLEMENTACIÓN VERIFICADA` (track G9A, aprobado; experimental) | misma especificación: G9P-R1 separa las coordenadas intrínsecas de su colocación en el diagrama; ADR 0010 | `kernel/spatial/identity/ProjectionBindingRecord`, `ProjectionDiagramMapRecord`; `kernel/spatial/semantic/DiagramMapDefinition`, `ProjectionFrameDefinition`; `G9A2SpatialSemanticRecordXmlTest`, `G9A2SpatialSemanticLifecycleTest` | A | no |
| SM-A-3 | Incidencia, pertenencia y dependencia constructiva; proyección durable de dependencias | `IMPLEMENTACIÓN VERIFICADA`: grafo de dependencias heredado y proyección durable de `PRE-G9B-R3` (aprobado) | `geocedg/specs/spatial/durable-dependency-projection.md` 1.1; ADR 0029; `locus-v2-semantics.md`, «Dependency graph, revisions and nested composition» | `kernel/spatial/identity/DurableDependencyProjection`; `PreG9bR3DurableDependencyProjectionTest` | A | no |
| SM-A-4 | Parametrización y continuidad en construcciones dinámicas: dominios del conductor, proveedores y ramas | `IMPLEMENTACIÓN VERIFICADA` de la parametrización de Locus V2 (aprobado); continuidad certificada por componente, con arcos y extremos certificados, en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado); la parametrización no cambia | `locus-v2-semantics.md`, «Driver-domain providers and parameters» y «Branch identity and lineage»; ADR 0027; `locus-v2-existence-components.md` §15.2 y §15.3 | `kernel/algos/AlgoLocusV2`, `kernel/locus/LocusBranch2D`, `LocusExistenceStructure2D`; `kernel/locus/intersection/HybridExistenceCertifier2D`; `PreG9BS1R2ExistenceStructureTest`, `PreG9BCu2ExistenceStructureV2Test`, `PreG9BCu2ConsumerMigrationTest` | A | no |
| SM-A-5 | Geometría matemática, cálculo y representación como capas distintas | `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado): existencia, evaluación y representación separadas (P5), forma límite de los extremos y dibujo por componentes certificadas; base del principio transversal 8 de §3 | [diseño de certificación de `CU2`](pre_g9b_cu2_hybrid_certification_design.md) §3.4; ADR 0038; `locus-v2-existence-components.md` §15.5; `locus-v2-presentation.md`; `dxf-curve-fidelity-and-approximation.md` | `kernel/locus/LocusDefinition2D`, `LocusBoundaryImage2D`; `euclidian/draw/LocusRenderData2D`; `PreG9BCu2RepresentationTest`, `PreG9BCu2AuthorModelConsumerTest` (Desktop) | A | SM-E-6 |
| SM-A-6 | Intersección de superficies en el modelo del autor: cono y cilindro, penetración y mordedura, verdadera magnitud por giro y desarrollo | `INVESTIGACIÓN`: construcción del autor del corpus de `CU0`, analizada en `CU2`; las herramientas que usa (Locus V2 y `LocusLength`) están implementadas, y la certificación de la existencia de sus loci (mordedura, pliegues y `e_2`) está en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado) | [análisis del modelo del autor](pre_g9b_cu2_author_model_certification_analysis.md) §2–§5 | corpus `models/regression/pre-g9b-cu/`; pruebas de caracterización `PreG9BCu0*` y de certificación `PreG9BCu2AuthorModel*` (Desktop) | A | no |

### 6.2 B. Geometría algebraica y certificación exacta

| Id | Método | Estado | Especificación o ADR | Código y pruebas | Ap. | Ficha |
|---|---|---|---|---|---|---|
| SM-B-1 | Discriminante de la intersección recta–circunferencia y su factorización por el par de tangentes | `INVESTIGACIÓN` | análisis de `CU2` §3.2 y §3.8 | sondeos fuera del repositorio | B | sí |
| SM-B-2 | Multiplicidad de raíces, tangencias y clasificación de las raíces de un predicado de validez | `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado) en los predicados de primer nivel; una raíz múltiple de otro predicado deja una banda `UNRESOLVED` | diseño de `CU2` §3.5 y §4.6; A2.2-R; `locus-v2-certified-construction-model.md` §10.6; `locus-v2-existence-components.md` §15.4 | `kernel/locus/exact/RealRootStructure1D`; `kernel/locus/intersection/HybridExistenceCertifier2D`; `kernel/locus/LocusSingularParameter2D`, `LocusPointComponent2D`; `RealRootStructure1DTest`, `CircleDriverRootStructure2DTest`, `PreG9BCu2HybridCertificationTest` | B | sí |
| SM-B-3 | Aritmética racional exacta sobre las constantes binary64 capturadas | `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado; D-CU2-16) | diseño de `CU2` §4.6; `locus-v2-certified-construction-model.md` §3 (constantes capturadas) y §10.6 | `kernel/locus/exact/ExactRational2D`; `ExactRational2DTest`, `PreG9BCu2AuthorModelCertificationTest` (Desktop) | B | SM-B-5 |
| SM-B-4 | Polinomios en una variable, máximo común divisor y factorización libre de cuadrados | `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado): máximo común divisor y descomposición libre de cuadrados de Yun, sin factorización completa | diseño de `CU2` §4.6 | `kernel/locus/exact/ExactPolynomial1D`, `SquareFreeDecomposition1D`; `ExactPolynomial1DTest`, `SquareFreeDecomposition1DTest` | B | SM-B-5 |
| SM-B-5 | Sucesiones de Sturm y recuento certificado de raíces: certificado algebraico exacto de primer nivel | `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado; D-CU2-16; `exact-univariate-sturm/v1`) | diseño de `CU2` §4.6; A2.2-R; ADR 0038, decisión 7; `locus-v2-certified-construction-model.md` §10.6 | `kernel/locus/exact/SturmSequence1D`, `RealRootStructure1D`, `CircleDriverRootStructure2D`, `SegmentDriverRootStructure2D`; `kernel/locus/intersection/FirstLevelPredicate2D`; `SturmSequence1DTest`, `CircleDriverRootStructure2DTest`, `PreG9BCu2HybridCertificationTest` | B | sí |
| SM-B-6 | Predicados de validez y transiciones topológicas | `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado) sobre el orden de autoridades aprobado | `locus-v2-existence-components.md` 1.0 §7; diseño de `CU2` §4.3; A2.2-R; `locus-v2-certified-construction-model.md` §10.4 y §10.5 | `kernel/locus/ConstructionValidityPredicate2D`; `kernel/locus/intersection/ConstructionBoxModel2D`, `HybridExistenceCertifier2D`; desde T11, `kernel/algos/AlgoDependentPointLocusV2` publica el certificado híbrido y el de compatibilidad se retira; `PreG9BCu2ClassV3ModelTest`, `PreG9BCu2HybridCertificationTest` | B | SM-B-2 |
| SM-B-7 | Eliminación algebraica para predicados posteriores a una raíz cuadrada | `INVESTIGACIÓN`: laguna identificada, fuera de `CU2` | diseño de `CU2` §4.6, «Límites» | — | B | no |
| SM-B-8 | Constantes capturadas frente a relaciones geométricas ideales | `INVESTIGACIÓN`; desde la versión 8, con la evidencia del estudio de la autoridad constructiva de `CU3`: la representación binary64 contradice 66 de las 160 relaciones de procedencia del modelo del autor, y una circunferencia tiene tres radios exactos distintos (matriz, semieje y construcción) | análisis de `CU2` §3.8.3 (F-CU2-10); `locus-v2-certified-construction-model.md` §3; [estudio de la autoridad constructiva](pre_g9b_cu3_constructive_authority_study.md) §1.2 a §1.4 | sondeo `tools/benchmark/pre-g9b-cu/cu3-java/` (`PreG9BCu3AuthorityStudyProbe`, evidencia y no producto) | B | SM-B-5 |
| SM-B-9 | Simetría estructural exacta como prueba de existencia de puntos estacionarios | `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado; D-CU2-6; `fold-symmetry/v1`), restringida al conductor en una circunferencia sin términos cruzados y a dos involuciones, y usada solo para los pliegues del mapa trazado | diseño de `CU2` §5; A2.2-R; `locus-v2-certified-construction-model.md` §10.7 | `kernel/locus/intersection/ConstructionSymmetry2D`; `PreG9BCu2FoldCertificationTest` | B | SM-C-6 |
| SM-B-10 | Rechazo de pares de tramos polinómicos por la envolvente convexa de Bernstein | `IMPLEMENTACIÓN VERIFICADA` (aprobado), como evidencia de candidatos en coma flotante, sin cobertura rigurosa | `locus-v2-extended-intersections.md` (`G8C1 AND G8C2 NORMATIVE — AUTHOR APPROVED`); ADR 0009 | `kernel/locus/intersection/PiecewisePolynomialPairIntersectionCapability2D`; `PiecewisePolynomialPairIntersectionCapability2DTest`, `G8C2LocusPairKernelTest` | B | no |
| SM-B-11 | Evaluación exacta de programas de regla y compás en un punto exacto del conductor, en la configuración de decisión `Σ_L⁺`: la configuración capturada de `L` sin ningún cambio, ampliada con el punto del conductor, el objetivo y sus ancestros con una sola semántica exacta por objeto (los objetos intermedios se recalculan exactamente, porque su redondeo rompe las relaciones estructurales: `K.x = fl(B.x + 5)`); una recalculación más profunda, que recalcule también las constantes capturadas, solo es diagnóstica y no se transfiere al punto de `L`; cada incidencia se decide contra el objetivo autoritativo (coeficientes binary64 de la recta y su parte limitada; centro y semieje del adaptador de la circunferencia), nunca contra una reconstrucción ideal del objetivo (precisión autoral G-M3-4); torre de extensiones cuadráticas sobre los racionales de la frontera, reconstrucción exacta del punto del conductor con la selección heredada, y decisión del signo y la nulidad con cotas constructivas de separación de raíces | `DISEÑO` (`CU3`, diseño aprobado el 2026-10-24; implementación autorizada, sin código todavía) e `INVESTIGACIÓN`: la identidad `J_1(K) = M_1` del modelo, deducida y comprobada con 80 cifras en las dos ramas de `H_1` en una recalculación profunda (diagnóstica); su validez en la configuración capturada se apoya en la aritmética heredada (análisis §5.8) y la decide la puerta G-M3 | [propuesta normativa de `CU3`](pre_g9b_cu3_normative_proposal.md) A3.5; [Etapa A](pre_g9b_cu3_stage_a_structural_points_design.md) §6 (M3); [análisis](pre_g9b_cu3_initial_state_and_capability_analysis.md) §5.3, §5.7 y §5.8 | sondeo `tools/benchmark/pre-g9b-cu/cu3-java/`, escenario P3 | B | no |
| SM-B-12 | Semántica constructiva intrínseca: el significado exacto de cada objeto es el de su construcción desde las entradas libres, con una frontera versionada de la clase exacta y una semántica de los puntos sobre caminos; la representación binary64 y la captura de `CU2` son representaciones suyas | `INVESTIGACIÓN` (estudio de la autoridad constructiva de `CU3`, versión 8; desde la versión 9, con los textos normativos candidatos AUT-1 y AUT-2, en los que un punto sobre camino es el que le asigna la aplicación de camino heredada sobre el camino ideal, y la autoridad es una política versionada por documento; sin aprobación autoral) | [estudio](pre_g9b_cu3_constructive_authority_study.md) §1.2, §1.6, §2.1 y §5; [propuesta normativa](pre_g9b_cu3_constructive_authority_normative_proposal.md) §2 | sondeo `PreG9BCu3AuthorityStudyProbe` (evidencia y no producto) | B | no |
| SM-B-13 | Prueba genérica de incidencias con entradas no constructibles (certificado de identidad constructiva): evaluación exacta en un cuerpo de funciones racionales de las indeterminadas de la frontera, ampliado por una raíz cuadrada; regla de frontera automática (entradas libres como indeterminadas, cortes de los objetos fuera de la clase, que olvidan relaciones y nunca las inventan); condiciones de rama, validez, pertenencia y no degeneración certificadas por encierros de valor medio de segundo orden con valores exactos en el centro de la caja; celdas, recomprobación por revisión sin trabajo simbólico y pruebas nuevas por celda | `INVESTIGACIÓN`; desde la versión 9, con prototipo y evidencia determinista: las cinco incidencias obligatorias, identidades en dos alcances (`B.y` y `H`; las ocho coordenadas libres del cono de dependencias); orden de contacto por jets exactos; controles adversarios; construcciones sintéticas con teoremas, conjeturas falsas y dependencia de la rama; la genericidad 12 de 12 de la versión 8 queda como evidencia anterior | [propuesta normativa](pre_g9b_cu3_constructive_authority_normative_proposal.md) §5, texto candidato AUT-6; [estudio](pre_g9b_cu3_constructive_authority_study.md) §1.6 y §4 | prototipo `tools/benchmark/pre-g9b-cu/cu3-java/` (`PreG9BCu3ConstructiveIdentityProbe`, evidencia y no producto); [evidencia](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-constructive-identity-evidence.json) | B | no |
| SM-B-14 | Certificado de paridad por simetría exacta: en una caja simétrica respecto de un pliegue certificado, las raíces simples van por pares y una raíz en el punto fijo nunca es simple | `INVESTIGACIÓN` (estudio de `CU3`) | [estudio](pre_g9b_cu3_constructive_authority_study.md) §1.7 | sondeo, escenario `fold_symmetry` | B | no |
| SM-B-15 | Transferencia de certificados de una representación certificada al objeto constructivo: correspondencia estructural, misma revisión y una vía rigurosa, sea margen (decisiones abiertas, con encierros que contienen el valor capturado y el constructivo), estructura (decisiones cerradas implicadas por premisas estrictas) o nueva decisión (decisiones cerradas sobre bits); sin vía, evidencia insuficiente y fallo cerrado estricto, sin tocar el certificado de origen | `INVESTIGACIÓN` (versión 9; texto normativo candidato AUT-4; sin aprobación autoral) | [propuesta normativa](pre_g9b_cu3_constructive_authority_normative_proposal.md) §3; ADR 0038, decisiones 7 y 17 | — | B | no |

### 6.3 C. Análisis numérico riguroso

| Id | Método | Estado | Especificación o ADR | Código y pruebas | Ap. | Ficha |
|---|---|---|---|---|---|---|
| SM-C-1 | Aritmética de intervalos con redondeo exterior | `IMPLEMENTACIÓN VERIFICADA` en las clases v1 y v2 (`PRE-G9B-R2-E0/E1` y `PRE-G9B-R3-X1`, aprobados); clase v3, con forma centrada, en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado) | `locus-v2-certified-construction-model.md` 1.1, y 1.2 §10 para la clase v3; ADR 0028, cuya aprobación consta en los registros de decisión de esas fases | `kernel/locus/intersection/ConstructionIntervalModel2D`, `SplineOutwardInterval2D`; `kernel/locus/metric/MetricOutwardArithmetic2D`, `MetricEnclosure2D`; `PreG9bR2E1CertifiedConstructionModelTest`, `PreG9bR3X1CertifiedExpressionPointModelTest`; clase v3: `kernel/locus/intersection/ConstructionBoxModel2D`, `ConstructionBoxOutcome2D`; `PreG9BCu2ClassV3ModelTest` | C | sí |
| SM-C-2 | Aislamiento certificado de raíces simples: Krawczyk y Newton de intervalos | `IMPLEMENTACIÓN VERIFICADA` en las intersecciones de SplineV2 (G9S1-R1, aprobado). Las transiciones de `CU2` no usan Krawczyk: el criterio de una variable (cambio de signo con una envolvente de la derivada que excluye el cero) está en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado) | `geocedg/specs/curves/spline-v2-pair-materialization.md` (`NORMATIVE / IMPLEMENTED — PASS — AUTHOR APPROVED`); ADR 0021; diseño de `CU2` §4.3; `locus-v2-certified-construction-model.md` §10.5 | `kernel/locus/intersection/SplinePairIntervalCertification2D`, `SplineImplicitIntervalCertification2D`; `G9S1R1SplinePairIntervalCertificationTest`, `G9S1R1StructuralImplicitCertificationTest`; transiciones de `CU2`: `kernel/locus/intersection/HybridExistenceCertifier2D`; `PreG9BCu2HybridCertificationTest` | C | sí |
| SM-C-3 | Raíces múltiples con procedimientos verificados con estructura añadida | `INVESTIGACIÓN`: alternativa no elegida | análisis de `CU2` §3.8.2, técnica C; ADR 0038, alternativas | — | C | sí |
| SM-C-4 | Punto estacionario certificado de un predicado: Krawczyk sobre `P′` con una envolvente de `P″` | `DISEÑO` (`CU2`, aprobado). El candidato lo implementa sin Krawczyk: una envolvente de `P″` de signo constante, la bisección de los signos de `P′` y la envolvente de `P` en el corchete. No tiene prueba propia ni fila de la matriz. Su extensión al punto crítico de un residuo de contacto (clases 3 y 4) está en `DISEÑO` de `CU3` (aprobado el 2026-10-24), con la evidencia del sondeo de la preparación (SM-C-12) | diseño de `CU2` §4.3; análisis §3.8.2, técnica B; A2.2-R; `locus-v2-certified-construction-model.md` §10.5 | `kernel/locus/intersection/HybridExistenceCertifier2D` (`stationary`) | C | no |
| SM-C-5 | Cotas de derivadas y chorros de primer y segundo orden | primer orden: `IMPLEMENTACIÓN VERIFICADA` (envolventes de valor y derivada; aprobado); segundo orden: `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado), usado solo en los puntos estacionarios y los pliegues, sin prueba de referencia independiente de sus envolventes | `locus-v2-certified-construction-model.md` §6; diseño de `CU2` §5 | `ConstructionIntervalModel2D`; segundo orden: `kernel/locus/intersection/ConstructionBoxModel2D`; `PreG9BCu2FoldCertificationTest` | C | SM-C-6 |
| SM-C-6 | Pliegues certificados: simetría, aislamiento, no degeneración, continuidad y tangentes opuestas | `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado; D-CU2-6), restringida al conductor en una circunferencia | diseño de `CU2` §5; A2.2-R; ADR 0038, decisión 8; `locus-v2-certified-construction-model.md` §10.7 | `kernel/locus/intersection/HybridExistenceCertifier2D`, `ConstructionSymmetry2D`; `kernel/locus/LocusCertifiedFold2D`; `PreG9BCu2FoldCertificationTest`, `PreG9BCu2AuthorModelCertificationTest` (Desktop) | C | sí |
| SM-C-7 | Condicionamiento y degeneraciones numéricas: clasificación binary64 junto a un valor crítico, tolerancias del evaluador heredado y resolución de las traslaciones | `INVESTIGACIÓN`; la preparación de `CU3` mide H02 con traslaciones de hasta 10⁶ y propone la aceptación certificada y la escala característica S1 (`DISEÑO` de `CU3`, aprobado el 2026-10-24; [normalización y ejes](pre_g9b_cu3_residual_normalization_and_axes_design.md)) | [estudio de precisión y tolerancias de `CU0`](pre_g9b_cu0_precision_and_tolerance_study.md); análisis de `CU2` §3.3 y §3.8.3 (F-CU2-1, F-CU2-9); `L-CU1-TRANSLATION-RESOLUTION` ([registro de cierre de `CU1`](../validation/pre_g9b_cu1_closeout_record.md)) | — | C | no |
| SM-C-8 | Presupuestos de cálculo y fallo cerrado: `NOT_ESTABLISHED` y `UNRESOLVED` | `IMPLEMENTACIÓN VERIFICADA` en la métrica de `CU1` (presupuesto de 16 384 cajas; aprobado); en la existencia, `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado): presupuestos versionados del certificador y bandas `UNRESOLVED` tipadas; el cierre de una banda de coincidencia a su resolución solo retira certificación local, nunca afirma inexistencia, y se cuenta y se mide (R7) | `locus-v2-metrics.md` §25; diseño de `CU2` §8; `locus-v2-certified-construction-model.md` §10.9 | `kernel/locus/metric/MetricGuaranteeClass2D`, `MetricEnclosure2D`; `PreG9BCu1MetricClassesTest`; existencia: `kernel/locus/intersection/HybridExistenceCertifier2D` (`Policy`, `getClosedCoincidenceWidth`), `kernel/locus/exact/ExactBudget1D`, `kernel/locus/LocusCertificationWork2D` (`COINCIDENCE_BAND_CLOSURE`); `PreG9BCu2HybridCertificationTest` | C | no |
| SM-C-9 | Totalidad estructural: certificación sin cajas de los programas de operaciones totales | `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado; `structural-totality/v1`): primera autoridad del certificador, composición mínima de V-CON-6 y paso de valores métricos por pasos totales | diseño de `CU2` §4.2; A2.2-R; `locus-v2-certified-construction-model.md` §10.8 y §10.10 | `kernel/locus/intersection/ConstructionBoxModel2D` (`isStructurallyTotal`), `HybridExistenceCertifier2D`; `kernel/algos/AlgoDependentPointLocusV2`; `PreG9BCu2HybridCertificationTest`, `PreG9BCu2ConsumerMigrationTest` | C | no |
| SM-C-10 | Aislamiento de raíces polinómicas por tramos monótonos (ceros de la derivada) y bisección | `IMPLEMENTACIÓN VERIFICADA` (aprobado), con evidencia en coma flotante, no certificada; se usa también para los puntos estacionarios de la distancia al cuadrado en la interacción | `locus-v2-intersections.md` 1.2; ADR 0008 | `kernel/locus/intersection/PolynomialRootIsolation2D`; `kernel/locus/interaction/LocusPointInteractionResolver2D`; `PolynomialRootIsolation2DTest`, `G8BIntersectionKernelTest` | C | no |
| SM-C-11 | Redondeo dirigido con transformaciones exactas (TwoSum y TwoProduct de Dekker) y suma compensada | `IMPLEMENTACIÓN VERIFICADA` (`CU1`, aprobado) | `locus-v2-metrics.md` §25 | `kernel/locus/metric/MetricOutwardArithmetic2D`, `LocusMetricAggregator2D`; `PreG9BCu1MetricClassesTest` | C | no |
| SM-C-12 | Qué certifican los intervalos: la existencia de alguna raíz en un intervalo (continuidad y cambio de signo certificado) y las desigualdades estrictas, nunca una igualdad en un parámetro o punto prescrito (una incidencia dada, una raíz múltiple); envolventes del residuo de contacto `h`, `h′` y `h″` compuestas con la clase v3; punto crítico certificado; grados de evidencia y vía certificada de admisión, junto a la verificación local de G8B-R1; certificados de conteo de raíces (número exacto y localización, con continuidad certificada) como única base de la regla de conflicto, en la que un punto crítico aislado no es una contradicción | `DISEÑO` (`CU3`, diseño aprobado el 2026-10-24; implementación autorizada, sin código todavía) e `INVESTIGACIÓN`: el sondeo clasifica los contactos del modelo (clase 3), las casi-tangencias (clases 3 y 4) y el barrido H02, y encuentra H-CU3-1 a H-CU3-4 en el motor vigente | propuesta normativa de `CU3` A3.0, A3.1-R y A3.5; [Etapa B](pre_g9b_cu3_stage_b_tangent_contact_certification_design.md) §2–§4 y §9 | sondeo de la preparación, escenarios P2 y P5 a P7 | C | no |

### 6.4 D. Curvas, métricas y topología

| Id | Método | Estado | Especificación o ADR | Código y pruebas | Ap. | Ficha |
|---|---|---|---|---|---|---|
| SM-D-1 | Longitud de arco, variación total de la función trazada, con clases de garantía | `IMPLEMENTACIÓN VERIFICADA` (`CU1`, aprobado); la estimación condicionada de una longitud total fuera de la clase v3 (§26.5, decisión H10), sin autoridad de existencia, en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado). Las longitudes totales de los loci críticos coinciden con los arcos barridos exactos dentro de su cota; la de un arco con extremos de raíz cuadrada (las mordeduras del modelo del autor) es `NOT_ESTABLISHED` con `WORK_BUDGET_EXHAUSTED`, sin cambio de valores ni de política: limitación computacional tipada `L-CU2-SQRT-END-LENGTH`, aceptada por la decisión C-10; el estimador con cambio de variable queda como trabajo futuro, sin asignación | `locus-v2-metrics.md` 1.6, §22–§26; `locus-v2-public-surface.md` (`LocusLength`); ADR 0007 y ADR 0037 | `org/geogebra/common/kernel/commands/CmdLocusLength` (con cabecera GeoCeDG); `kernel/algos/AlgoLocusMetricV2`; `kernel/locus/metric/LocusMetricEngine2D`, `PolicyV2LocusMetricCapability2D`, `MetricGuaranteeClass2D`; `PreG9BCu1MetricClassesTest`, `LocusMetricProductiveNumericalTest`, `PreG9BCu1AcceptanceTest` (Desktop), `PreG9BCu2ConditionedEstimateTest`, `PreG9BCu2HybridCertificationTest`, `PreG9BCu2AuthorModelConsumerTest` (Desktop) | D | sí |
| SM-D-2 | Longitud estructural o analítica (clase 1) | `IMPLEMENTACIÓN VERIFICADA` (`CU1`, aprobado) | `locus-v2-metrics.md` §25.1 | `kernel/locus/metric/StructuralLocusMetric2D`; `PreG9BCu1MetricClassesTest` | D | SM-D-1 |
| SM-D-3 | Cotas certificadas de longitud (clase 2) a partir de envolventes de la primera derivada | `IMPLEMENTACIÓN VERIFICADA` (`CU1`, aprobado; D-CU1-R4) | `locus-v2-metrics.md` §25 | `kernel/locus/metric/CertifiedConeEnclosure2D`, `MetricEnclosure2D`; `PreG9BCu1MetricClassesTest` | D | SM-D-1 |
| SM-D-4 | Cuadratura adaptativa y estimación del error (clase 3) | `IMPLEMENTACIÓN VERIFICADA` (`CU1`, aprobado; contrato de estimación D-CU1-2) | `locus-v2-metrics.md` §25.12 | `kernel/locus/metric/LocusMetricIntegrator2D`, `LocusMetricEstimator2D`; `PreG9BCu1EstimatorTest` | D | SM-D-1 |
| SM-D-5 | Corrección de Richardson y sus hipótesis | `IMPLEMENTACIÓN VERIFICADA` en la ruta de producción del evaluador (`CU1`, aprobado): `LocusMetricEstimator2D` corrige con `fine + (fine − coarse)/3`; la corrección de Simpson de `LocusMetricIntegrator2D` solo es alcanzable desde `DifferentialLocusMetricCapability2D`, que instancian las pruebas. Sus hipótesis se registran y no certifican (D-CU1-R2) | `locus-v2-metrics.md` §25.3 | `kernel/locus/metric/LocusMetricEstimator2D`, `LocusMetricIntegrator2D`; `PreG9BCu1EstimatorTest` | D | SM-D-1 |
| SM-D-6 | Aliasing y límites de cualquier muestreo finito | `IMPLEMENTACIÓN VERIFICADA` de la política métrica v2, que corrige H10 (`CU1`, aprobado); los límites generales, en `INVESTIGACIÓN` | [análisis de aliasing de `CU1`](pre_g9b_cu1_metric_aliasing_analysis.md); `locus-v2-metrics.md` | `kernel/locus/metric/PolicyV2LocusMetricCapability2D`; `PreG9BCu1EstimatorTest`, `PreG9BCu0SyntheticCharacterizationTest` (Desktop) | D | no |
| SM-D-7 | Componentes continuas de existencia: arcos, extremos laterales y componentes puntuales | `IMPLEMENTACIÓN VERIFICADA` de la estructura de existencia (aprobado, ADR 0027), con el certificado de compatibilidad retirado en T11; arcos, extremos laterales y componentes puntuales en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado; D-CU2-19), con la transición tipada de cambio de dimensión `DIMENSION_CHANGED` de §15.4 (D-CU2-T13-R1; V-POINT-3; §6.7). Un fallo forzado de la forma límite deja la posición de una componente puntual no disponible sin cambiar su existencia (V-POINT-4, con el gancho interno de prueba de la decisión C-9) | ADR 0027; `locus-v2-existence-components.md` 1.0, y 1.1 §15; diseño de `CU2` §3; A2.1-R | `kernel/locus/LocusExistenceStructure2D`, `LocusBoundary2D`, `LocusPointComponent2D`, `LocusLineage2D`, `LocusComponentLineage2D`; `PreG9BS1R2ExistenceStructureTest`, `PreG9BCu2ExistenceStructureV2Test`, `PreG9BCu2HybridCertificationTest`, `PreG9BCu2LineageTest` | D | sí |
| SM-D-8 | Periodicidad, costuras y parametrizaciones circulares; autoridad única de cierre | periodicidad estructural y autoridad única de cierre en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado), en lugar de los cinco predicados del defecto B07; la periodicidad solo se certifica con el conductor en una circunferencia. La costura de un dominio periódico no es un extremo: todo parámetro canónico pertenece al dominio, y los extremos incluidos de un arco que la cruza se evalúan en su propio parámetro (H-CU2A-3 y H-CU2A-4). Limitaciones aceptadas por las decisiones C-11 y C-12: la canonicalización periódica no es la identidad en el dominio fundamental (`L-CU2-PERIODIC-CANONICALIZATION`) y las componentes cerradas periódicas conservan la pertenencia heredada con tolerancia junto a la costura (`L-CU2-CLOSED-PERIODIC-SEAM`) | `locus-v2-intersections.md`; diseño de `CU2` §4.4; A2.2-R y A2.5; `locus-v2-existence-components.md` §15.3; [consumo de `CU2`](pre_g9b_cu2_consumption_and_representation.md) §4 | `kernel/locus/LocusExistenceStructure2D` (`isClosedPeriodic`), que leen `euclidian/draw/LocusRenderCache2D`, `export/SemanticCurveExportAdapter2D` y `kernel/locus/metric/LocusMetricRouteResolver2D`; `kernel/locus/SemanticGeneratorDomainProvider1D`, `LocusDefinition2D`; `PreG9BCu2ConsumerMigrationTest`, `PreG9BCu2HybridCertificationTest`, `PreG9BCu2AuthorModelConsumerTest` (Desktop) | D | SM-D-7 |
| SM-D-9 | Límites laterales, discontinuidades y eventos estructurales (`ORIGIN_WRAP`) | `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado; D-CU2-3) | [diseño del evento](pre_g9b_cu2_oriented_length_event_design.md); A2.3-R; `locus-v2-metrics.md` §26 | `kernel/locus/metric/LocusMetricFunctionStructure2D`; `kernel/locus/intersection/RouteSourceFacts2D`, `HybridExistenceCertifier2D`; `PreG9BCu2MetricStructureTest` | D | sí |
| SM-D-10 | Longitud orientada y desarrollo de superficies | `IMPLEMENTACIÓN VERIFICADA` de `LocusLength` y su recorrido orientado (aprobado); el desarrollo `e_2` es una construcción del autor, y su existencia, un arco abierto de longitud completa desde `t_N = 0` y `COMPLETE` (B10.2), está en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado); los valores de `LocusLength` no cambian | `locus-v2-metrics.md`; [análisis de la longitud orientada de `CU0`](pre_g9b_cu0_oriented_length_analysis.md) | `kernel/algos/AlgoLocusMetricV2`, `kernel/locus/metric/LocusMetricRouteResolver2D`; `G9U0MetricPositionTest`, `PreG9BCu2AuthorModelRepresentationTest` (Desktop) | D | SM-D-9 |
| SM-D-11 | Pliegues, esquinas, cúspides y contactos | pliegues en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado; SM-C-6); en un parámetro singular, el dibujo deja un vértice exacto sin probar la esquina; contactos tangentes en `DISEÑO` candidato para `CU3`, no aprobado tal como está formulado (`OBS-CU0-CU3-TANGENT-EXISTENCE`), y sustituido por la taxonomía de evidencia de contacto de la preparación de `CU3` (`DISEÑO` candidato; [Etapa B](pre_g9b_cu3_stage_b_tangent_contact_certification_design.md)), que separa contactos en pliegues y tangencias (SM-D-14); cúspides sin estudiar | diseño de `CU2` §4.6 y §5; [propuesta de materialización tangente de `CU0`](pre_g9b_cu0_tangent_materialization_proposal.md) | `kernel/locus/LocusCertifiedFold2D`, `LocusSingularParameter2D`; `PreG9BCu2FoldCertificationTest`, `PreG9BCu2RepresentationTest` | D | SM-C-6 |
| SM-D-12 | Teselación y aproximaciones de dibujo y exportación con fidelidad declarada | `IMPLEMENTACIÓN VERIFICADA` del dibujo adaptativo actual y de las aproximaciones de exportación DXF y LaTeX (aprobado), cuyo error sigue siendo estimado (`ESTIMATED_ERROR`) y no certificado; la política de dibujo `locus-render-policy/v2`, el dibujo y la exportación por componentes certificadas y la fidelidad declarada, en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado); las reglas LaTeX están ya en `latex-semantic-curve-export.md` (D-CU2-11) | `locus-v2-presentation.md` 1, y 2 para la política v2; `dxf-curve-fidelity-and-approximation.md`; `latex-semantic-curve-export.md`; ADR 0014; [consumo y representación de `CU2`](pre_g9b_cu2_consumption_and_representation.md) §5–§6 | `euclidian/draw/LocusRenderCache2D`, `LocusComponentTessellator2D`; `export/AdaptiveCurveApproximationBuilder2D`, `export/SemanticCurveExportAdapter2D`; Desktop `org/geocedg/desktop/export/LatexSemanticExportSupport`; `G9X1AdaptiveCurveExportTest`, `LocusV2RenderHardeningTest`, `PreG9BR6PlusCLatexExportTest` (Desktop), `PreG9BCu2RepresentationTest`, `PreG9BCu2AuthorModelExportTest` (Desktop) | D | no |
| SM-D-13 | Curvas spline semánticas (SplineV2) e intersecciones certificadas de pares | `IMPLEMENTACIÓN VERIFICADA` (G9S1 y G9S1-R1, aprobados): base de potencias truncadas con numeradores exactos y resolución numérica de trabajo fijo | `geocedg/specs/curves/semantic-spline-2d.md`; `geocedg/specs/curves/spline-v2-pair-materialization.md`; ADR 0018, ADR 0021 y ADR 0022 | `kernel/algos/AlgoSplineV2`; `kernel/spline/SplineStructuralModel2D`, `SplinePrecisionSolve2D`; `G9S1SemanticSpline2DTest`, `G9S1R1SplinePairMaterializationTest` | D | no |
| SM-D-14 | Contactos en un pliegue del trazado frente a tangencias: en una fuente singular (`F′ = 0`), un cero doble del residuo no es un orden de contacto geométrico; el arco termina en el contacto y un objetivo perturbado da dos preimágenes de un único punto | `INVESTIGACIÓN` (sondeo de la preparación de `CU3`): `e_1` en `t = 0` y `t = π` (pliegues de `CU2`), ángulos de 58,4° y 31,6° con los objetivos; `o_1·h_2`, tangencia regular | [análisis de `CU3`](pre_g9b_cu3_initial_state_and_capability_analysis.md) §5.4b; Etapa B §4.4 y §6.3; SM-C-6 | sondeo de la preparación, escenario P2 | D | no |

### 6.5 E. Semántica dinámica CeDG

| Id | Método | Estado | Especificación o ADR | Código y pruebas | Ap. | Ficha |
|---|---|---|---|---|---|---|
| SM-E-1 | Identidad geométrica frente a identidad de representación: identidades durable, de selección y de componente | `IMPLEMENTACIÓN VERIFICADA` de la identidad durable y de rama (aprobado); separación de las tres identidades y linaje estructural en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado), con el cambio de dimensión tipado (SM-D-7) | `locus-v2-semantics.md`, «Branch identity and lineage»; ADR 0010; diseño de `CU2` §7; A2.1-R; `locus-v2-existence-components.md` §15.6 | `kernel/spatial/identity/PersistentGeoId`; `kernel/locus/LocusComponentLineage2D`, `LocusLineage2D`; `PreG9BCu2LineageTest` | E | no |
| SM-E-2 | Selección de ramas e intersecciones; salidas efectivas en raíces coincidentes | `INVESTIGACIÓN` del comportamiento heredado (F-CU2-8, F-CU2-13, F-CU2-14); lectura del estado de selección, contrato de salidas efectivas y asentamiento de la selección heredada (`selection-settlement/v1`, D-CU2-T11-R1) en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado; D-CU2-17 y D-CU2-18), con pruebas propias de V-COINC-6 y V-COINC-9 (D-CU2-T13-R2). La banda que deja sin resolver la segunda salida sin etiqueta de una intersección recta–circunferencia es rigurosa: una caja sale de ella solo si el primer intento del cálculo heredado es decisivo en todos sus conductores binary64, con cotas a priori de cada redondeo (H-T13-B, D-CU2-T13-R7); el algoritmo heredado no cambia. La versión 4 añade: en compactación la banda acota también la otra raíz bruta, y sus cotas cubren dos huecos de rigor de redondeo (revisión de R7, R7-A a R7-D); una intersección sin etiqueta dentro de una expresión es una selección del corte, reproducida, asentada o fallida con causa (H-CU2A-7); y una consulta que sacó la copia de su primera inicialización aplica la pertenencia exacta de la regla capturada, de modo que una raíz admitida por la tolerancia heredada antes del inicio del camino nunca se publica como salida (H-CU2A-8) | diseño de `CU2` §4.1 y §4.7; A2.2-R; `locus-v2-certified-construction-model.md` §10.2 y §10.3 | clases heredadas `org/geogebra/common/kernel/algos/AlgoIntersectLineConic`, `AlgoIntersectSingle` (con accesores de solo lectura) y `AlgoIntersect`; `kernel/locus/ReconstructibleLocusEvaluator2D`, `kernel/locus/intersection/InheritedLineCircleBand2D`; `PreG9BCu2ClassV3ModelTest`, `PreG9BCu2SelectionSettlementTest`, `PreG9BCu2AuthorModelSelectionSettlementTest` (Desktop) | E | sí |
| SM-E-3 | Continuidad y correspondencia semántica: linaje estructural y resolución de selectores | `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado; E-3), con el cambio de dimensión tipado, que no resuelve selectores (SM-D-7); desde la versión 4, una división o una fusión nunca relaciona una componente puntual con la cerrada periódica: aparece o desaparece (H-CU2A-10) | diseño de `CU2` §7; A2.1-R; `locus-v2-existence-components.md` §15.6 | `kernel/locus/LocusComponentLineage2D`, con claves por fuentes estructurales y los bits de los extremos solo como último recurso; `kernel/locus/LocusComponentSelectorResolver2D`; `PreG9BCu2LineageTest` | E | no |
| SM-E-4 | Certificación local frente a completitud global; niveles de evidencia | `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado) sobre la dirección DA-1 aprobada y el ADR 0027: naturalezas de la evidencia, evidencia exploratoria sin autoridad, maximalidad y completitud; el nivel `POINT_VALID` no tiene tipo propio | ADR 0027; [propuesta E de `CU0`](pre_g9b_cu0_hybrid_certification_proposal.md); diseño de `CU2` §2; ADR 0038; `locus-v2-existence-components.md` §15.1 y §15.8 | `kernel/locus/LocusExistenceStructure2D`, `LocusEvidenceNature2D`, `LocusExploratoryEvidence2D`; `PreG9BCu2ExistenceStructureV2Test`, `PreG9BCu2ConsumerMigrationTest` | E | sí |
| SM-E-5 | Propagación de validez y dependencias en el grafo: composición de loci dependientes y anidados | `IMPLEMENTACIÓN VERIFICADA` de la composición anidada (`CU1`, aprobado); composición certificada mínima de la existencia (V-CON-6) en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado): un punto de rama de un soporte locus `COMPLETE` sin componentes puntuales y con pasos estructuralmente totales; los loci anidados solo acotan la completitud | `locus-v2-semantics.md`, «Dependency graph, revisions and nested composition»; ADR 0027; `locus-v2-certified-construction-model.md` §10.10 | `kernel/algos/AlgoNestedLocusV2`, `AlgoDependentPointLocusV2`; `kernel/locus/LocusExistenceStructure2D` (`composedThroughTotalSteps`); `LocusMetricProductiveNestedTest`, `PreG9BCu1RevisionAndSessionTest`, `PreG9BCu2ConsumerMigrationTest` | E | no |
| SM-E-6 | Existencia matemática frente a disponibilidad computacional y de representación | `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado). Una raíz certificada que el cálculo heredado pierde deja la existencia certificada y la evaluación con la causa tipada `COMPUTATIONAL_FAILURE` (H-T13-C, con prueba; comportamiento anterior al descendiente), y un locus sobre una longitud que falla queda `UNRESOLVED`, nunca inexistente (V-UND-1); la causa `COMPUTATIONAL_DEPENDENCY_FAILURE` queda reservada, sin emisor (decisión C-2); el fallo forzado de la forma límite de V-POINT-4 deja la componente puntual existente con la posición `UNAVAILABLE` (decisión C-9); ejemplo central del principio transversal 8 de §3 | diseño de `CU2` §3.4; A2.1-R; ADR 0038, decisión 11; `locus-v2-existence-components.md` §15.5 | `kernel/locus/LocusDefinition2D`, `LocusPointComponent2D`, `LocusBoundaryImage2D`; `PreG9BCu2ConsumerMigrationTest`, `PreG9BCu2HybridCertificationTest`, `PreG9BCu2AuthorModelConsumerTest` (Desktop) | E | sí |
| SM-E-7 | Aproximaciones de dibujo y exportación sin autoridad geométrica | principio de arquitectura (`AGENTS.md` §4) aplicado en la exportación semántica implementada; autoridad única de cierre en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado; SM-D-8) | `geocedg/specs/export/geometry-export-foundation.md`; [consumo de `CU2`](pre_g9b_cu2_consumption_and_representation.md) §1 | véase SM-D-12 | E | no |
| SM-E-8 | Revisiones semánticas, corte reconstruible y huella exacta | `IMPLEMENTACIÓN VERIFICADA` (`CU1`, aprobado); huella exacta v2 y fidelidad del corte (D-CU2-20-R) en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado), con el contraste con un documento recién construido (V-REV-EXACT-5, D-CU2-T13-R2); el corte reproduce la etiqueta pendiente de su revisión (H-T13-A) y las etiquetas de valor derivado y de dibujo diferido quedan fuera de la huella con su espacio (H-CU2A-5). El cambio de etiquetado de la segunda salida de un par etiquetado se tipa `STRUCTURE`, como fija la decisión C-1, y lo asevera una prueba. Desde la versión 4, el lugar libre de un punto sobre un camino o en una región que sigue al conductor es una entrada del corte: la huella lo distingue y la fidelidad lo compara y lo restaura exactamente (H-CU2A-9) | `locus-v2-semantics.md` 1.2, enmienda de `CU1`, y 1.3, enmienda de `CU2`; ADR 0006 | `kernel/locus/ReconstructibleLocusEvaluator2D`, `ReconstructionSliceFingerprint2D`, `ReconstructionExactState2D`, `ReconstructionFidelity2D`; `PreG9BCu1RevisionAndSessionTest`, `PreG9BCu2RevisionExactnessTest`, `PreG9BCu2SliceFidelityCharacterizationTest`, `PreG9BCu2AuthorModelSliceFidelityTest` (Desktop) | E | sí |
| SM-E-9 | Continuación semántica de las raíces de intersección; fase y rango intrínsecos deterministas de las raíces públicas | `IMPLEMENTACIÓN VERIFICADA` (aprobado) | `locus-v2-intersections.md` 1.2; ADR 0008 y ADR 0017 | `kernel/locus/intersection/LocusIntersectionSolver2D`; `kernel/algos/AlgoLocusIntersectionV2`; `G8BIntersectionKernelTest`, `G9U0R4IntersectionAdmissibilityContinuationTest` | E | no |
| SM-E-10 | Resolución semántica de direcciones inversas y estado interactivo de los puntos sobre loci | `IMPLEMENTACIÓN VERIFICADA` (aprobado); un `Point(L, u)` sin resolver (selector ambiguo, componente puntual o parámetro fuera de las componentes) tiene causa tipada en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado; H-CU2A-1); con la precisión de V-LIN-4 de la decisión C-7; desde la versión 4, la evaluación de la propia definición en la envolvente de una componente puntual también nombra la causa `POINT_COMPONENT` (H-CU2A-11) | `locus-v2-point-interaction.md` 2; ADR 0019; `locus-v2-existence-components.md` §15.5 | `kernel/algos/AlgoSemanticLocusPoint2D`; `kernel/locus/interaction/LocusPointInteractionResolver2D`, `kernel/locus/LocusSemanticPointCause2D`; `G9U0R6SemanticLocusPointInteractionTest`, `PostG9U1A6OptionalBranchPointTest`, `PreG9BCu2LineageTest` | E | no |
| SM-E-11 | Transformaciones de semejanza de loci con herencia explícita de su semántica | `IMPLEMENTACIÓN VERIFICADA` (aprobado); la imagen de una componente puntual transporta la envolvente de su posición con aritmética de intervalos hacia fuera, en `IMPLEMENTACIÓN VERIFICADA` (`CU2`, aprobado; H-CU2A-2); desde la versión 4, los extremos incluidos de un arco imagen que cruza la costura se evalúan en su propio parámetro, sin recanonicalizar en la fuente (H-CU2A-6) | `locus-v2-similarity-transformations.md`, nota de alcance A2.5 | `kernel/algos/AlgoLocusSimilarityTransform2D`; `kernel/locus/LocusSimilarityTransform2D`; `G9U0R5SimilarityTransformationsTest`, `PreG9BCu2HybridCertificationTest` | E | no |
| SM-E-12 | Puntos estructurales por correspondencia del conductor: procedencia, identidad del soporte, existencia, unicidad y orientación de `D ↦ t(D)`, evaluación en una componente certificada y segunda incidencia probada, con fallo cerrado en cada hecho | `DISEÑO` (`CU3`, diseño aprobado el 2026-10-24; implementación autorizada, sin código todavía) | propuesta normativa de `CU3` A3.3-R; [Etapa A](pre_g9b_cu3_stage_a_structural_points_design.md) | sondeo de la preparación, escenarios P1, P2, P4 y P10 | E | no |
| SM-E-13 | Germen de contacto tangente y ciclo de vida (`CURRENT`, `CONTACT_LOST`, `CONTACT_SPLIT`, `CONTACT_NOT_CERTIFIED`, `IDENTITY_DISCONTINUITY`) sin transferencia de tokens | `DISEÑO` (`CU3`, diseño aprobado el 2026-10-24; implementación autorizada, sin código todavía) | propuesta normativa de `CU3` A3.2-R; [ADR 0039 (PROPOSED)](../adr/0039-locus-v2-structural-points-and-certified-contact-evidence.md); Etapa B §6 | — | E | no |
| SM-E-14 | Integridad geométrica de las relaciones, separada de la precisión de la representación: estados conceptuales `STRICT`, `RELAXED`, `NOT_ESTABLISHED` y `CONTRADICTED`, relativos a la autoridad, con propagación por premisas y una política `RELAXED` versionada; ninguna contradicción rigurosa se admite mediante una tolerancia | `INVESTIGACIÓN` (hipótesis autoral de 2026-10-24, evaluada en el estudio de `CU3`; no es una API); desde la versión 9, con los textos normativos candidatos AUT-3 y AUT-5: un tipo propio, una política `RELAXED` positiva (igualdad exacta con los sustitutos de frontera o, solo en raíces con cambio de signo, encierro compatible), la distinción entre evidencia relajada, contradicción rigurosa y ausencia de evidencia, y la recalificación de G8B-R1 sin herencia automática | [estudio](pre_g9b_cu3_constructive_authority_study.md) §2.4; [propuesta normativa](pre_g9b_cu3_constructive_authority_normative_proposal.md) §4 | — | E | no |

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

La versión 2 añade una diferencia entre textos del candidato de `CU2`, que corresponde a la
revisión autoral de ese candidato: `locus-v2-existence-components.md` §15.4 pide transiciones de
linaje tipadas para el cambio de dimensión entre punto y arco, pero el vocabulario de transiciones
de `locus-v2-semantics.md` (`UNCHANGED`, `APPEARED`, `DISAPPEARED`, `SPLIT` y `MERGED`) no tiene
ninguna. El candidato registra el paso de punto a arco como una desaparición y una aparición, y la
fila V-POINT-3 de la matriz de `CU2` no tiene prueba.

La versión 3 registra su resolución en el candidato correctivo, pendiente de la aprobación autoral
del candidato: la decisión D-CU2-T13-R1 añadió `DIMENSION_CHANGED` al vocabulario de linaje de
`locus-v2-semantics.md` y la regla de relación a `locus-v2-existence-components.md` §15.4, con
prueba de V-POINT-3 en `PreG9BCu2LineageTest`.

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
| Alcance y generalidad | cualquier predicado escalar continuo de existencia sobre un dominio de una dimensión. La continuidad de la construcción exige además que la salida que define el punto trazado esté definida (SM-E-2). En `CU2` (aprobado), la multiplicidad solo se decide en los predicados de primer nivel (SM-B-5), y los demás dejan bandas no resueltas. El extremo es cerrado en una raíz del discriminante o de un parámetro de rayo o segmento, y abierto, con una banda no resuelta alrededor de la raíz, cuando se anula un divisor, una dirección o un radio (`locus-v2-certified-construction-model.md` §10.5 y §10.6) |
| Hipótesis | el predicado decide la existencia cerca de la raíz y la selección es la misma regla a ambos lados |
| Degeneraciones y límites | varios predicados que se anulan a la vez; multiplicidad 4 o mayor, sin esquina; salidas anuladas por reglas heredadas |
| Ejemplo o modelo | penetración, valor crítico y mordedura del modelo del autor; tangencia exterior sintética (V-CRIT-5 y V-POINT-1 a V-POINT-6 de la matriz de `CU2`) |
| Referencias | por completar |
| Cobertura y cuestiones abiertas | demostración de la continuidad y de las tangentes laterales en la esquina; multiplicidades altas; relación con los puntos dobles y aislados de las curvas algebraicas |

#### SM-B-5 — Certificado algebraico exacto: racionales, máximo común divisor y Sturm

| Campo | Contenido |
|---|---|
| Descripción matemática | Un predicado de primer nivel depende racionalmente de `cos t` y `sin t`. Con `u = tan(t/2)` se convierte en un polinomio `p(u)` con coeficientes racionales exactos, porque las constantes binary64 capturadas son racionales diádicos. La parte libre de cuadrados `p / gcd(p, p′)` y la sucesión de Sturm cuentan las raíces reales distintas de cada intervalo; la bisección exacta las aísla, y la multiplicidad y el signo entre raíces se deciden exactamente; `t = ±π` se trata aparte |
| Alcance y generalidad | Sturm y el máximo común divisor valen para cualquier polinomio en una variable con coeficientes exactos. `CU2` (aprobado) los aplica solo a predicados de primer nivel con el conductor en una circunferencia o un segmento; no a predicados posteriores a una raíz cuadrada (SM-B-7) ni a funciones no algebraicas. Presupuestos: 4 096 bits de coeficiente y 512 bisecciones por raíz; con el conductor en un segmento, solo en el dominio `[0, 1]` |
| Hipótesis | constantes capturadas exactas; dependencia racional del conductor; presupuestos de bits y de bisecciones |
| Degeneraciones y límites | predicado idénticamente nulo (identidad estructural); coeficientes grandes; raíces muy próximas (`2⁻¹⁰⁰` en las pruebas sintéticas). Certifica la configuración capturada, nunca la relación ideal que la construcción calcula con redondeo |
| Ejemplo o modelo | valor crítico de la mordedura y binary64 contiguos ([análisis de `CU2`](pre_g9b_cu2_author_model_certification_analysis.md) §3.8; V-CRIT-1 a V-CRIT-10) |
| Referencias | Sturm; Yun; von zur Gathen y Gerhard; Basu, Pollack y Roy; Collins y Akritas, como alternativa |
| Cobertura y cuestiones abiertas | complejidad en bits; comparación con el aislamiento por la regla de Descartes; eliminación algebraica para predicados de nivel superior |

#### SM-C-1 — Aritmética de intervalos con redondeo exterior

| Campo | Contenido |
|---|---|
| Descripción matemática | Envolventes del valor y de la primera derivada de cada operación de un programa de construcción sobre una caja del parámetro, con redondeo hacia fuera: el rango verdadero queda contenido en la envolvente. En la clase v3 (`CU2`, aprobado), cada caja da `VALID`, `UNIFORMLY_INVALID` o `UNRESOLVED` |
| Alcance y generalidad | funciones construidas con operaciones cuya extensión de intervalos es correcta. GeoCeDG la tiene implementada en las clases v1 y v2 del modelo de construcciones certificadas y en las envolventes métricas de `CU1`; la clase v3, con forma centrada, está implementada en `CU2` (aprobado) en `ConstructionBoxModel2D` |
| Hipótesis | extensiones de intervalos correctas y redondeo dirigido |
| Degeneraciones y límites | sobreestimación por dependencia; una envolvente que contiene el cero no prueba nada; un predicado de igualdad solo es uniformemente inválido por una identidad estructural. Hay dos implementaciones independientes, sin biblioteca común: la de las curvas ensancha un ulp cada operación y la métrica decide el sentido del redondeo con TwoSum y Dekker (SM-C-11); las envolventes trigonométricas suponen que `StrictMath` yerra como mucho un ulp; el paquete de intervalos heredado de GeoGebra no se usa como certificado |
| Ejemplo o modelo | certificación de intersecciones de Locus V2; cajas del modelo del autor (`PreG9BCu2AuthorModelCertificationTest`) |
| Referencias | Moore (1966); Neumaier; Tucker |
| Cobertura y cuestiones abiertas | subdivisión, presupuestos y fallo cerrado; aritmética afín y formas afines, hoy una limitación registrada de `CU1` (`L-CU1-AFFINE-FORMS`) |

#### SM-C-2 — Aislamiento certificado de raíces simples

| Campo | Contenido |
|---|---|
| Descripción matemática | Con el operador de Krawczyk `K(X) = y − C·f(y) + (I − C·f′(X))(X − y)`, si `K(X)` está en el interior de `X`, `X` contiene un único cero. En una variable, un cambio de signo con una envolvente de la derivada que excluye el cero prueba una raíz simple única en el corchete |
| Alcance y generalidad | raíces simples de funciones o sistemas diferenciables. El operador de Krawczyk está implementado en la certificación de las intersecciones de SplineV2; las transiciones de los predicados de validez de `CU2` (aprobado) usan solo el criterio de una variable (`locus-v2-certified-construction-model.md` §10.5) |
| Hipótesis | derivada no singular en la caja |
| Degeneraciones y límites | raíces múltiples o casi múltiples: el operador no contrae (SM-C-3) |
| Ejemplo o modelo | intersecciones de pares de SplineV2 (`G9S1R1SplinePairIntervalCertificationTest`); en `CU2`, extremos en parámetros de rayo o segmento (V-TR-2, `PreG9BCu2HybridCertificationTest`). Las fronteras de la mordedura son raíces de un predicado de primer nivel, y las decide el certificado exacto (SM-B-5) |
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
| Alcance y generalidad | curvas con una simetría estructural verificable sobre el programa capturado; sin simetría, el pliegue no se certifica y el dibujo se refina con diagnóstico. Implementado en `CU2` (aprobado) con el conductor en una circunferencia y cajas de aislamiento de `2⁻⁸` a `2⁻³⁰` periodos; las tangentes laterales opuestas se deducen de `F″(t₀)` |
| Hipótesis | simetría verificada por igualdades racionales exactas de las constantes capturadas y por una tabla de operaciones equivariantes |
| Degeneraciones y límites | contacto de orden superior (`F″(t₀) = 0`); simetría aproximada |
| Ejemplo o modelo | `e_1` del modelo del autor, con pliegues en `0` y `π` (análisis de `CU2` §4.3; V-FOLD-1 a V-FOLD-4) |
| Referencias | do Carmo |
| Cobertura y cuestiones abiertas | puntos estacionarios sin simetría; cúspides; relación con los puntos de contorno aparente de las proyecciones |

#### SM-D-1 — Longitud de arco con clases de garantía

| Campo | Contenido |
|---|---|
| Descripción matemática | `L = ∫ ‖F′(t)‖ dt` sobre una componente, con cuatro clases de garantía: estructural, envolvente certificada (cotas inferior y superior), estimación numérica (cuadratura adaptativa con corrección de Richardson, cuyo defecto entre niveles estima el error restante) y no establecida. Las hipótesis de un estimador no son una certificación (D-CU1-R2) |
| Alcance y generalidad | curvas rectificables trazadas por Locus V2; las envolventes certificadas tienen un presupuesto versionado de 16 384 cajas (D-CU1-R4). Fuera de la clase v3 de `CU2`, la longitud total es una estimación condicionada (`locus-v2-metrics.md` §26.5) que no certifica existencia, continuidad ni completitud |
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
| Cobertura y cuestiones abiertas | dominios de dimensión superior; relación con la descomposición en celdas; la transición tipada de cambio de dimensión entre punto y arco, implementada en el candidato correctivo (D-CU2-T13-R1; V-POINT-3; §6.7), relaciona solo claves de componente y no resuelve selectores; el fallo forzado de la forma límite, con prueba (V-POINT-4, decisión C-9) |

#### SM-D-9 — Eventos estructurales de una función métrica y desarrollo con longitud orientada

| Campo | Contenido |
|---|---|
| Descripción matemática | La longitud orientada desde un origen fijo sobre una componente cerrada es continua salvo en el origen, donde salta la longitud total. El evento `ORIGIN_WRAP` se certifica con hechos estructurales (dominio, continuidad y salto positivo probado por una cuerda con redondeo exterior), sin usar el valor de la longitud, y el dependiente es una componente abierta cortada en el origen, con dos extremos laterales distintos |
| Alcance y generalidad | funciones métricas de curvas cerradas con un origen fijo y una política de misma posición declarada; los demás casos están en el [diseño del evento](pre_g9b_cu2_oriented_length_event_design.md) §4.3. Implementado en `CU2` (aprobado) con una entrada métrica por programa: el corte se certifica cuando las envolventes del punto trazado en la longitud `0` y en las cotas rigurosas de la longitud total son disjuntas |
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
| Cobertura y cuestiones abiertas | el problema general de la continuidad en geometría dinámica; selección continua de raíces coincidentes (D-CU2-18); en `CU2`, V-COINC-6 (conductor que empieza sin intersección) y V-COINC-9 (selección certificada frente a resultado operativo) tienen prueba propia en el candidato correctivo (D-CU2-T13-R2); la redacción de V-COINC-5 y V-COINC-7 sobre el exceso de evaluación heredado, precisada por la decisión C-6 |

#### SM-E-4 — Certificación local frente a completitud global

| Campo | Contenido |
|---|---|
| Descripción matemática | Cuatro niveles de evidencia (`POINT_VALID`, `EXPLORATORY`, `LOCAL_CERTIFIED` y `COMPLETE`) y cinco naturalezas de prueba. La admisibilidad local es independiente de la completitud global: un consumidor local acepta una componente certificada y uno global exige `COMPLETE` |
| Alcance y generalidad | la semántica de existencia de Locus V2; trasladable a cualquier sistema de geometría dinámica con objetos definidos sobre dominios de parámetro |
| Hipótesis | ninguna adicional |
| Degeneraciones y límites | presupuestos agotados (`UNRESOLVED`); evidencia exploratoria sin autoridad; la estimación condicionada de una longitud total (`locus-v2-metrics.md` §26.5) es el único consumo global sin `COMPLETE`, y no certifica nada |
| Ejemplo o modelo | V-LVL-1 a V-LVL-3 y V-COMPL-1 de la matriz de `CU2` |
| Referencias | ADR 0027; ADR 0038 |
| Cobertura y cuestiones abiertas | semántica de fallo cerrado; en `CU2` (aprobado), el nivel `POINT_VALID` no tiene tipo propio |

#### SM-E-6 — Existencia matemática, disponibilidad numérica y representación

| Campo | Contenido |
|---|---|
| Descripción matemática | Cuatro capas con garantías distintas: existencia matemática, topología certificada, disponibilidad numérica y disponibilidad de representación. Ninguna se deduce de otra: una evaluación válida no prueba la existencia, un fallo no prueba la no existencia y la existencia certificada no garantiza las coordenadas |
| Alcance y generalidad | cualquier objeto calculado; en GeoCeDG, Locus V2 (`CU2`, aprobado) |
| Hipótesis | ninguna adicional |
| Degeneraciones y límites | puntos del evaluador dentro de huecos exactos; fallos de evaluación con existencia certificada; extremos no evaluables |
| Ejemplo o modelo | V-AVAIL-1 a V-AVAIL-6 y V-POINT-4 de la matriz de `CU2`; V-POINT-4 lo prueba `PreG9BCu2HybridCertificationTest.aForcedFailureOfTheLimitFormLeavesThePositionUnavailable` |
| Referencias | por completar |
| Cobertura y cuestiones abiertas | fidelidad declarada de las aproximaciones de dibujo y exportación; es la ficha de referencia del principio transversal 8 de §3 para el libro |

#### SM-E-8 — Revisiones semánticas, corte reconstruible y huella exacta

| Campo | Contenido |
|---|---|
| Descripción matemática | Un Locus V2 se evalúa sobre un corte de la construcción sincronizado una vez por revisión. La revisión cambia con las entradas de definición y no con la posición del conductor, de modo que mover el conductor no recalcula los certificados. La huella de la revisión debe ser exacta |
| Alcance y generalidad | implementado en `CU1`; la exactitud de la huella es un hallazgo de `CU2` (F-CU2-12), y la huella exacta v2 y la fidelidad del corte están implementadas en `CU2` (candidato; D-CU2-20-R) |
| Hipótesis | entradas con estado determinista |
| Degeneraciones y límites | estado heredado que cambia con el conductor (primera inicialización en una raíz doble, D-CU2-18); estado de selección fuera de la huella y literales angulares sin texto original reconstruidos con otro valor (F-CU2-12, corregido; D-CU2-20-R) |
| Ejemplo o modelo | puertas GC-1 y GC-2 y V-REV-EXACT-1 de la matriz de `CU2` |
| Referencias | por completar |
| Cobertura y cuestiones abiertas | semántica de revisiones y reutilización de certificados; en `CU2`, el contraste con un documento recién construido (V-REV-EXACT-5) tiene prueba propia en el candidato correctivo (D-CU2-T13-R2); la causa del cambio de etiquetado (`STRUCTURE`, decisión C-1) y la regla de los certificados anidados por revisión (GC-2, decisión C-4) están fijadas y probadas |

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
9. las lagunas pendientes de investigación o de redacción;
10. los principios transversales para el libro, entre ellos la distinción entre integridad
    geométrica y precisión numérica (principio 8 de §3), con sus ejemplos de `CU2` y su relación
    con SM-A-5 y SM-E-6.

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
| Demostración algebraica de teoremas geométricos (SM-B-13) | Wu Wen-Tsün, «On the decision problem and the mechanization of theorem-proving in elementary geometry», *Scientia Sinica* 21 (1978); S.-C. Chou, *Mechanical Geometry Theorem Proving*, Reidel, 1988 |

## 10. Límites de este registro

- Es inicial y no exhaustivo. Los paquetes posteriores añaden los métodos que empleen o
  investiguen.
- Los estados proceden de la evidencia citada. El registro no vuelve a verificar métodos de fases
  cerradas.
- La agrupación por categorías no es un diseño de apéndices: ese diseño está pendiente de
  preparación y revisión.
- Las fichas de §6.6 resumen el contenido científico disponible; no son texto editorial ni
  sustituyen a las demostraciones que exige §4.
