# PRE-G9B-CU3 — Registro de las puertas del incremento E1

| Campo | Valor |
|---|---|
| Carácter | Registro técnico de las puertas del incremento E1 bajo la autoridad constructiva: G-AUT-1, G-AUT-3 a G-AUT-8 y G-A. Reúne la evidencia de cada puerta y de cada fila de la matriz, las revisiones de D-AUT-11 aplicadas y lo que pasa a E2 y E3. Es evidencia técnica: no aprueba nada, no sustituye la decisión autoral y no declara `CU3` `PASS — AUTHOR APPROVED` |
| Autoridad | [registro de aprobación](pre_g9b_cu3_constructive_authority_approval_record.md) §5 a §10 y §16; [propuesta normativa](../architecture/pre_g9b_cu3_constructive_authority_normative_proposal.md) §6.2 a §6.4 y §10 (D-AUT-11); [diseño de E1](../architecture/pre_g9b_cu3_e1_implementation_design.md); [matriz](pre_g9b_cu3_validation_matrix.md) §3 y §6; [`locus-v2-constructive-authority.md`](../../geocedg/specs/locus/locus-v2-constructive-authority.md) 1.0 |
| Rama | `phase/pre-g9b-cu3-implementation`, local, sin publicar |
| Base | `3cd0f710c370c3985ea536f1cc5edcadeca930ee`; E0 en `1f81666ea90f4ca53a9d782b4f905cc839c61cf1`; presupuestos y diseño de E1 en `acae78a951d6eed81434c82fd1a1d3c2df254cc5` |
| Commit de las puertas | `50ac91c29fb2835874d10c7e7afe56c3c349f79f` (árbol `01b5a4255e90866c71008f7df9e7b696bc581ca9`) |
| Evidencia de coste | [`pre-g9b-cu3-e1-cost-evidence.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-e1-cost-evidence.json), SHA-256 `5bdf81ad1e3a44d4e7a7677bd0719d9627e491ecda27608c549f693f41ab8c2d` (ejecución A) |
| Evidencia del lector de la base | [`pre-g9b-cu3-e1-base-reader-evidence.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-e1-base-reader-evidence.json), SHA-256 `9e6bf217b767c02467f08fa3a303cb1a91543e5584033b6183bedb472ea48e37` |
| Resultado técnico | Las puertas G-AUT-1, G-AUT-3 a G-AUT-8 y G-A de E1 se cumplen con la evidencia de §3 a §8, con la cuestión abierta de §10.1. Las selecciones `PHASE` de `CU0`, `CU1` y `CU2` se aceptan sobre el commit de las puertas. La ejecución `INTEGRATION` se rechaza por los pins de inventario de las dos selecciones ejecutadas finales, sin ninguna comprobación violada (§8 y §10.8). No es aprobación autoral |
| Estado | E1 es un **candidato parcial**: `P ∈ L` es `STRICT` por transferencia y `P ∈ r` es `RELAXED` por `SUBSTITUTE_GENERIC` en los cinco contactos. `CU3` sigue incompleto hasta E2 a E4 |
| `selfApproved` | `false` |

## 1. Alcance y método

E1 entrega construcciones útiles con integridad consultable (propuesta §6.2, diseño de E1 §1). Sus
piezas E1.1 a E1.8 están en el núcleo compartido, salvo la oferta de adopción, el inspector y las
etiquetas, que están en el escritorio y no contienen lógica geométrica. Los presupuestos son límites
semánticos del núcleo; el banco de coste y los informes están en sus capas externas
(`tools/benchmark/pre-g9b-cu/` y este registro).

Cada puerta se evalúa con su criterio y sus pruebas adversarias (propuesta §6.3). Cada fila de la
matriz tiene su prueba, citada por su identificador en el código de la prueba, o la evidencia que se
indica en §4. Las filas que el plan aprobado asigna a E2 o E3 se señalan con su autoridad; ninguna se
da por superada en E1.

Todas las ejecuciones se hicieron sobre el commit de las puertas, con el espacio de trabajo limpio y
sin cambios del árbol durante ellas.

## 2. Commits de E1

| Commit | Contenido |
|---|---|
| `f09cd51c7` | fundamentos: `RelationIntegrity`, registros de evidencia, propagación por premisas, requisitos de consumidor, `AuthorityPolicy`, presupuesto `cu3-exact-evaluation-budget/v1` y torre exacta (E1.1) |
| `048683e2a` | registro de política persistido, lectores que fallan cerrados y evento explícito `AUTHORITY_POLICY_ADOPTION` con informe de impacto (E1.2) |
| `97a85b1c9` | tabla de clase `constructive-class/v1` y cono constructivo de una relación (E1.3) |
| `9553e4841` | evaluación exacta del cono en `Σ_C` y encierro por intervalos de los valores ideales (E1.4) |
| `677f4e8d5` | transferencia de los certificados de `CU2` por margen (E1.5) |
| `f15ec0a09` | `SUBSTITUTE_GENERIC` versión 1 (E1.6) |
| `d99def043` | `Point(L, D)` e `Intersect(L, r, D)` bajo la autoridad constructiva (E1.7) |
| `276a7d35e` | evidencia certificada, vía certificada de H02 y regla de conflicto (`CU3-A` I-1 a I-3) |
| `a0cfa2ed9` | configuración constructiva de la evidencia de intersección y recalificación de G8B-R1 (E1.6, E1.8) |
| `7c844602d` | ejes como objetivos de recta reservados (`CU3-A` I-6, H06) |
| `e5728ab78` | matriz de la Etapa A bajo la autoridad constructiva |
| `e80bf42e3` | vías de transferencia por estructura y por nueva decisión (G-AUT-4) |
| `0132e572f`, `947aa3536` | contadores deterministas y banco de G-AUT-8 |
| `dd1000201`, `721501742` | etiquetas de los diagnósticos nuevos y filas de las puertas |
| `c9e9a2bc9` | inventario JUnit de las pruebas de E1 |
| `406a44d9d` | escritorio: oferta de adopción, inspector y etiquetas de la evidencia |
| `86cb710f6` | inventario JUnit de las pruebas de escritorio |
| `45729b6eb` | censo de procedencia del modelo y casi-tangencia decidida por la tolerancia heredada (G-AUT-1) |
| `50ac91c29` | inventario JUnit de las pruebas de G-AUT-1; commit de las puertas |

## 3. Puertas G-AUT de E1

| Puerta | Criterio (propuesta §6.3) | Evidencia | Resultado |
|---|---|---|---|
| G-AUT-1, cierre de procedencia | el evaluador constructivo hace exactas las relaciones de procedencia del modelo y de un corpus sintético, con la semántica de D-AUT-3; ninguna relación de procedencia es `STRICT` sin su hecho de existencia | censo de procedencia del modelo del autor con el evaluador del producto (`PreG9BCu3ProvenanceCensusTest`), que entra solo como regresión: ninguna de las 160 relaciones se refuta, 140 se cumplen exactamente y 20 agotan el presupuesto de bits y fallan cerradas (§10.1); corpus sintético y adversarias en `ConstructiveConeEvaluatorTest` (punto sobre camino frente a su copia binary64, recta por dos puntos frente a la circunferencia, igualdad accidental binary64, casi-tangencia decidida por la tolerancia heredada, punto recortado al extremo de un segmento); fila V-AUT-PROV-1 | superada; el censo, que solo es regresión, deja veinte relaciones sin decidir por el presupuesto aprobado (§10.1, cuestión abierta) |
| G-AUT-3, intrinsecidad | una sola semántica por objeto con cualquier conductor, captura y consumidor | `ConstructiveConeTest`: `h_2` tiene el mismo programa constructivo solo, en el cono de un contacto con el corte de `K_1` y en un cono con el corte de `J`; las cuatro semánticas de `h_2` del estudio (§1.5) no existen bajo la autoridad constructiva, porque el estado binary64 y la captura son representaciones; fila V-AUT-PROV-2 | superada |
| G-AUT-4, transferencia | margen, estructura y nueva decisión; `EVIDENCE_INSUFFICIENT` y fallo cerrado estricto; bytes del certificado de origen intactos | `Cu2CertificateTransferTest` (13 pruebas): margen con encierros que contienen el valor capturado y el constructivo, perturbación de un ulp, pliegue por estructura con premisas constructibles y su rechazo con un sustituto de frontera, nueva decisión exacta de la localización de una raíz o su refutación, componente puntual, huella distinta, estado de selección cambiado, presupuesto inyectado y bytes del certificado iguales antes y después; filas V-AUT-TRF-1 a V-AUT-TRF-6 | superada; los pliegues del modelo quedan para E2 (§10.3) |
| G-AUT-5, integridad | propagación por premisas; consumidores estrictos que fallan de forma explícita | `RelationIntegrityTest` (8 pruebas) con un grafo sintético de premisas `STRICT`, `RELAXED` y `CONTRADICTED`; inspector de escritorio con la integridad separada del error declarado y las premisas no establecidas (`PreG9BCu3ConstructiveAdoptionDesktopTest`); filas V-AUT-INT-1 y V-AUT-INT-2 | superada |
| G-AUT-6, sin blanqueo | ninguna relación `CONTRADICTED` pasa a `RELAXED`; autoridad por documento; (c1) según §4.2b y nunca como prueba; ninguna decisión heredada da `STRICT` | `RelationIntegrityTest`, `SubstituteGenericEvidenceTest` (11), `SubstituteGenericPerturbationSetTest` (7), `ConstructiveClassTableTest` (6), `ConstructiveIntersectionAdmissionTest` (4), `AuthorityPolicyPersistenceTest` (documento sin dos autoridades) y `StructuralPointMatrixTest` (decisiones heredadas condicionadas); filas V-AUT-INT-3, V-AUT-RLX-1 a V-AUT-RLX-7 y V-AUT-NUM-1 a V-AUT-NUM-6 | superada |
| G-AUT-7, compatibilidad | documentos antiguos y nuevos sin la política constructiva hasta la adopción explícita; tokens que se resuelven; `preserve` igual; `pins` invertidos solo con prueba; los lectores anteriores fallan cerrados | `AuthorityPolicyPersistenceTest` (13), `ConstructiveIntersectionAdmissionTest`, modelo del autor sin adoptar y adoptado (`PreG9BCu3StructuralPointAuthorModelTest`), oferta explícita de adopción con una sola transacción y un punto de Deshacer (`PreG9BCu3ConstructiveAdoptionDesktopTest`), lector de la base (§7) y selecciones `PHASE` de `CU0`, `CU1` y `CU2` (§8); filas V-AUT-ADOPT-1 a V-AUT-ADOPT-8 | superada |
| G-AUT-8, coste | presupuestos versionados y contadores deterministas | banco de coste (§6): todas las evaluaciones del modelo y de los casos sintéticos dentro de `cu3-exact-evaluation-budget/v1`, contadores iguales entre ejecuciones, inyección de presupuesto y construcción con raíces independientes; `ExactQuadraticTowerTest` (11) | superada para `cu3-exact-evaluation-budget/v1`; el presupuesto del certificado se mide en E2 |

G-AUT-2, G-AUT-9 y G-AUT-10 son puertas de E2 (propuesta §6.3) y no se evalúan aquí. La parte de
G-AUT-8 que mide el presupuesto del certificado de identidad constructiva también es de E2.

## 4. Puerta G-A

G-A exige las filas obligatorias de `CU3-A` de la matriz y las regresiones de su §3.9 (estrategia
§2), ajustadas a la decisión de la autoridad constructiva (propuesta §6.3: «las de `CU3`, ajustadas
a esta decisión»). De las 138 filas de la matriz, 100 están citadas por su identificador en una
prueba de E1. Las demás tienen la evidencia, o el incremento, que se indica.

| Filas | Evidencia en E1 |
|---|---|
| V-A-1 a V-A-22 | `StructuralPointMatrixTest` (25 pruebas), `StructuralPointTest` (7) y, V-A-21, `PreG9BCu3StructuralPointAuthorModelTest` |
| V-MAND-1 a V-MAND-4, V-DYN-1 a V-DYN-3 | `PreG9BCu3StructuralPointAuthorModelTest`, `StructuralPointMatrixTest` y `StructuralPointTest`: los cinco contactos con `P ∈ L` `STRICT` y `P ∈ r` `RELAXED`, los barridos con la misma identidad, la desaparición y la reaparición, y `Point(o_1, K)` igual al rodeo `N_1` del autor |
| V-DET-CU3-1, V-VIEW-1 | `StructuralPointMatrixTest` |
| V-M3-4, V-M3-5, V-M3-6, V-M3-8 | `StructuralPointMatrixTest`, `ExactQuadraticTowerTest` y el banco de coste: decisión exacta en `Σ_C`, con la igualdad accidental exacta demostrada y la no representable refutada |
| V-M3-1, V-M3-2 | bajo AUT-7 la reconstrucción en `Σ_L⁺` solo es diagnóstico; su análogo en `Σ_C`, los cinco contactos exactos para el sustituto de la rotación, está en `ConstructiveConeEvaluatorTest`; la incidencia ideal la decide el certificado de E2 (V-AUT-CIC-1) |
| V-M3-3 | control con los objetos en su estado binary64: `ConstructiveConeEvaluatorTest` y el censo, donde la copia binary64 de un punto sobre camino refuta su camino |
| V-M3-7 | correspondencia con el algoritmo vivo: `ConstructiveConeEvaluatorTest` (selección heredada reproducida y salida viva en la otra rama con `BRANCH_CORRESPONDENCE_NOT_DEMONSTRATED`) y V-A-20 |
| V-M3-9, V-CFG-5, V-CFG-6 | E2 (G-AUT-2): con AUT-7 la autoridad del objetivo es el objeto constructivo, y la decisión estricta de los cinco contactos contra él es del certificado; V-CFG-6 es una de las adversarias de G-AUT-2. En E1, `P ∈ r` es `RELAXED` y el punto materializado es `P_L` |
| V-CFG-1 a V-CFG-4 | `StructuralPointMatrixTest` y el banco, leídas con AUT-7: en `Σ_C` los dos radios de V-CFG-1 son `√2` y la incidencia se demuestra; el objetivo constante y el término distinto con el mismo radio ideal se deciden por su objeto constructivo |
| V-B-1, V-B-3 a V-B-6, V-B-11, V-B-DUP-1 | `PreG9BCu3CertifiedEvidenceTest` (14) y `ConstructiveIntersectionAdmissionTest` |
| V-B-2, V-B-NOISE-1, V-CONF-5 | E3: la reclasificación de la costura y la regla de conflicto con la raíz doble necesitan el certificado de paridad por simetría exacta (propuesta §6.2, E3; D-AUT-11 sobre D-CU3-10 y D-CU3-13). En E1 (`ConstructiveIntersectionAdmissionTest`), bajo la política heredada G8B-R1 conserva la raíz de la costura con la clase 3 visible; en la adopción se recalifica y no se admite, porque un contacto sin cambio de signo nunca se admite por una cota del residuo |
| V-B-7 a V-B-10, V-B-12 | `CU3-B`, E3 |
| V-EVID-1 a V-EVID-6 | `PreG9BCu3CertifiedEvidenceTest`, `StructuralPointMatrixTest` y las suites de regresión G8B, G8C1, G8C2 y G9U0-R4 (§8); V-EVID-5 son las filas V-CONF |
| V-CONF-1 a V-CONF-4, V-CONF-6 a V-CONF-8 | `PreG9BCu3CertifiedEvidenceTest` y `ConstructiveIntersectionAdmissionTest` (la regla decide en `Σ_C`) |
| V-H2-1 a V-H2-5 | `PreG9BCu3CertifiedEvidenceTest`; inversión de `pins.H02` en `PreG9BCu0SyntheticCharacterizationTest` |
| V-H2-PAIR-1 | sin cambios en los pares; la no conformidad con §8.1 sigue registrada como limitación |
| V-H3-1 a V-H3-4 | `PreG9BCu3ReservedAxisTargetTest` (5); inversión de `pins.H06` |
| V-COMPAT-1, V-COMPAT-3 | selecciones `PHASE` de `CU0`, `CU1` y `CU2` y suites de regresión (§8): tokens, `preserve.contacts`, `e_2` y `LocusLength` sin cambios |
| V-COMPAT-2 | D-AUT-11 sobre D-CU3-7: los documentos antiguos conservan G8B-R1 bajo la política heredada; en la adopción, la recalificación de §4.4 (V-AUT-ADOPT-8) retira con causa tipada lo contradicho, sin reasociación |
| V-A-COMPAT-1, V-A-COMPAT-2 | lector de la base (§7): un documento con formas de `CU3` lleva siempre el registro de política, y la base aborta la carga entera |
| V-AUT-ADOPT, V-AUT-TRF, V-AUT-PROV, V-AUT-INT, V-AUT-RLX, V-AUT-NUM | §3 |
| V-AUT-ID-1 a 7, V-AUT-CIC-1 a 6, V-AUT-DYN-2 a 4 | E2 (G-AUT-2, G-AUT-9 y G-AUT-10). V-AUT-DYN-1 tiene ya su parte de E1 (`StructuralPointMatrixTest`) |
| `pins.B08_B09` | E3, con la reclasificación de la costura |

## 5. Revisiones de D-AUT-11 aplicadas en E1

| Decisión de `CU3` | Revisión bajo `constructive-authority/v1` | Implementación en E1 |
|---|---|---|
| D-CU3-3 | autoridad constructiva; `Σ_L` como representación transferida | `Cu2CertificateTransfer`; `P ∈ L` `STRICT` solo con la transferencia establecida |
| D-CU3-4 | M3 en `Σ_C` con sustitutos declarados; el certificado para las premisas no constructibles | `ConstructiveConeEvaluator` exacto; con un sustituto no constructible su decisión vale para el sustituto y nunca da `STRICT` por sí sola |
| D-CU3-5 | D-AUT-7: `Intersect(L, r, D)` con `P ∈ r` `RELAXED` e integridad visible | `AlgoLocusDriverCorrespondencePoint2D`, inspector de escritorio |
| D-CU3-6, D-CU3-7 | G8B-R1 conservada bajo la política antigua y recalificada en la adopción | `IntersectionAdmissionImpact`, V-AUT-ADOPT-8 |
| D-CU3-10, D-CU3-13 | reclasificación de la costura por paridad; certificado de paridad en `Σ_C` | E3 |
| G-M3-4 | autoridad del objetivo constructiva | V-CFG-1 a V-CFG-4 leídas con AUT-7; la decisión estricta de los cinco contactos, E2 |

## 6. Coste y presupuestos (G-AUT-8)

El presupuesto `cu3-exact-evaluation-budget/v1` es un límite semántico del núcleo
(`ExactEvaluationBudget`, `ExactQuadraticTower`); su agotamiento lanza una excepción tipada que el
consumidor convierte en `EXACT_EVALUATION_BUDGET_EXHAUSTED`, con sus contadores, y el límite solo se
puede restringir para las inyecciones adversarias (`ExactEvaluationBudget.restrictedTo`). La
resolución de cada punto estructural lleva los contadores deterministas de su evaluación exacta y
los de `SUBSTITUTE_GENERIC`. El banco (`tools/benchmark/pre-g9b-cu/run-cu3-e1-cost.ps1`, sonda
`PreG9BCu3E1CostProbe`) solo los mide y no asigna veredicto.

Dos ejecuciones sobre el commit de las puertas, con el espacio de trabajo limpio, dan los mismos
contadores (solo cambian los tiempos), y coinciden con los de la ejecución anterior sobre
`947aa353`. La evidencia versionada es la ejecución A.

| Evaluación | Integridad | Causa | Operaciones | Profundidad | Bits | Evaluaciones SG | Operaciones SG | Mayor SG |
|---|---|---|---|---|---|---|---|---|
| `Point(e_1, K)`, `Point(o_1, K)`, `Point(e_1, J)` | `STRICT` | — | 83 | 0 | 109 | 0 | 0 | 0 |
| `Intersect(e_1, n, K)` | `RELAXED` | — | 543 | 1 | 385 | 9 | 4 638 | 526 |
| `Intersect(e_1, c_1, K)` | `RELAXED` | — | 479 | 1 | 477 | 9 | 4 062 | 462 |
| `Intersect(o_1, h_2, K)` | `RELAXED` | — | 1 531 | 2 | 478 | 9 | 11 554 | 1 514 |
| `Intersect(e_1, c_1, J)` | `RELAXED` | — | 479 | 1 | 477 | 9 | 4 062 | 462 |
| `Intersect(e_1, r, J)` | `RELAXED` | — | 543 | 1 | 389 | 9 | 4 638 | 526 |
| inyección: 8 operaciones en `Intersect(e_1, n, K)` | `NOT_ESTABLISHED` | `EXACT_EVALUATION_BUDGET_EXHAUSTED` | 9 | 0 | 52 | 0 | 0 | 0 |
| V-M3-5: radio 1,5 frente a `y = 1,5` | `STRICT` | — | 79 | 0 | 6 | 0 | 0 | 0 |
| V-M3-4: un ulp de distancia | `NOT_ESTABLISHED` | `INCIDENCE_REFUTED_IN_CONSTRUCTIVE_CONFIGURATION` | 79 | 0 | 53 | 0 | 0 | 0 |
| V-M3-6: `0,3` frente a `0,1 + 0,2` | `NOT_ESTABLISHED` | `INCIDENCE_REFUTED_IN_CONSTRUCTIVE_CONFIGURATION` | 77 | 0 | 107 | 0 | 0 | 0 |
| V-CFG-1: recalculación conjunta | `STRICT` | — | 238 | 1 | 6 | 0 | 0 | 0 |
| raíces cuadradas independientes | `STRICT` | — | 2 740 | 3 | 105 | 0 | 0 | 0 |
| las mismas, con profundidad 1 | `NOT_ESTABLISHED` | `EXACT_EVALUATION_BUDGET_EXHAUSTED` | 266 | 1 | 105 | 0 | 0 | 0 |

Además, el arrastre del conductor sin cambio de revisión devuelve la misma resolución con los mismos
contadores, sin ninguna evaluación nueva (GC3-3).

Lectura:

- En los cinco contactos obligatorios, la evaluación más cara usa el 1,6 % de las operaciones, una
  profundidad de 2 sobre 8 y el 12 % de los bits del presupuesto. Cada evaluación de
  `SUBSTITUTE_GENERIC` está dentro del presupuesto por sí misma (precondición 5 de §7.1: `N + 1`
  evaluaciones, cada una con su presupuesto).
- Las inyecciones agotan el presupuesto y fallan cerradas, sin ninguna decisión, también con la
  construcción de raíces independientes (profundidad 3 frente a un límite inyectado de 1).
- El censo de procedencia (§3) muestra que el límite de 4 096 bits se alcanza en el modelo fuera
  de los cinco contactos: veinte relaciones del cono de la rotación necesitan 4 126 bits. Fallan
  cerradas. El censo no es un consumidor de producto y el presupuesto aprobado se aplica a la
  evaluación de un punto estructural frente a su objetivo; no se modifica (§10).
- El presupuesto del certificado de identidad constructiva se mide en E2.

## 7. Compatibilidad y persistencia (G-AUT-7)

- **Política del documento.** El registro `authorityPolicy` va dentro de la sección
  `geocedgSpatial`, delante de los registros de identidad (diseño de E1 §4). Todo documento, nuevo
  o existente, nace o se conserva bajo `legacy-representation-authority/v1`, sin registro
  (V-AUT-ADOPT-1). Abrir, editar, guardar, Deshacer y Rehacer no cambian la política
  (V-AUT-ADOPT-3); ninguna operación deja dos autoridades en un documento (V-AUT-ADOPT-5); copiar,
  pegar y las macros siguen la política del destino, y un fragmento del portapapeles o una macro
  nunca llevan el registro (V-AUT-ADOPT-6).
- **Adopción explícita.** `AUTHORITY_POLICY_ADOPTION` calcula su informe de impacto antes de
  aplicarse, se aplica en una sola mutación atómica de la construcción y no cambia ninguna identidad,
  token ni selector: en el modelo del autor, todas las identidades durables son iguales antes y
  después (`PreG9BCu3StructuralPointAuthorModelTest`). En un documento vacío el informe está vacío y
  el evento sigue siendo explícito (V-AUT-ADOPT-2). Deshacer restaura la política anterior y Rehacer
  la vuelve a aplicar (V-AUT-ADOPT-4). La recalificación de G8B-R1 ocurre una sola vez, en la
  adopción, y el informe la enumera (V-AUT-ADOPT-8).
- **Escritorio.** Crear `Point(L, D)` o `Intersect(L, r, D)` desde la entrada algebraica en un
  documento heredado falla con `CONSTRUCTIVE_AUTHORITY_REQUIRED`; el escritorio ofrece entonces la
  adopción con su informe, con Cancelar como opción por defecto. Si se rechaza, se muestra el
  rechazo tipado y el documento no cambia. Si se acepta, la adopción y la entrada son una sola
  transacción con un solo punto de Deshacer, también cuando la entrada termina sin resultado; un
  Deshacer vuelve a la política anterior sin el punto (`PreG9BCu3ConstructiveAdoptionDesktopTest`).
  No hay una acción de menú aparte (§10).
- **Lector anterior (V-AUT-ADOPT-7, V-A-COMPAT-1 y V-A-COMPAT-2).** El producto de la base
  `3cd0f710`, construido aparte desde `git archive`, abre tres documentos escritos por el producto
  del commit de las puertas
  ([evidencia](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-e1-base-reader-evidence.json)): un
  control sin la política se carga; un documento vacío adoptado y otro con `Point(L, D)` e
  `Intersect(L, g, D)` fallan cerrados con `MALFORMED_RECORD` («Unknown geocedgSpatial record:
  authorityPolicy») y no cargan nada: el documento abierto antes sigue intacto y ninguna forma de
  `CU3` se reinterpreta. Un documento con formas de `CU3` lleva siempre el registro, porque esas
  formas exigen la política constructiva, de modo que la base aborta la carga entera en lugar de
  perder solo esos objetos (D-CU3-12).
- **Persistencia.** Un punto estructural se guarda como una orden ordinaria con su identidad
  durable. No se persiste ningún parámetro, certificado, transferencia ni integridad: guardar,
  reabrir, Deshacer, Rehacer y copiar lo reconstruyen desde el grafo (V-A-22).
- **Documentos anteriores.** Las selecciones `PHASE` de `CU0`, `CU1` y `CU2` se aceptan (§8): las
  referencias `preserve`, los tokens de las raíces transversales, `e_2` y `LocusLength` no cambian
  (V-COMPAT-1 y V-COMPAT-3).

## 8. Regresión

Todas las ejecuciones se hicieron sobre `50ac91c29fb2835874d10c7e7afe56c3c349f79f`, con el espacio
de trabajo limpio antes y después, en este orden y sin cambios del árbol entre ellas. Los resultados
de `verify.ps1` se re-autentican recalculando su `result_hash`.

| Ejecución | Orden | Resultado |
|---|---|---|
| `PRE-G9B-CU0` | `verify.ps1 -Profile PHASE -Phase PRE-G9B-CU0 -LogDirectory artifacts\agent\pre-g9b-cu3-e1-final-phase-pre-g9b-cu0-50ac91c2` | `verification-539a2f1e20bb4fa9866eae4cb73d114e`, resultado `3ec2efdfddd73918c34d85ee068045e5077e52dfd38dbc5200f3d627ace88ef1`, `ACCEPTED / COMPLETE` 2/2, re-autenticado |
| `PRE-G9B-CU1` | ídem, `-Phase PRE-G9B-CU1`, `…-pre-g9b-cu1-50ac91c2` | `verification-17ffd29dad9b41ab8db0f0d77fd3d586`, resultado `10eb8b608ad22ee8252cc6ab1b980877655ab50d57a115c382822fd1ed4edd05`, `ACCEPTED / COMPLETE` 4/4, re-autenticado |
| `PRE-G9B-CU2` | ídem, `-Phase PRE-G9B-CU2`, `…-pre-g9b-cu2-50ac91c2` | `verification-7da36cef47bb4f97b9cb418f7f550422`, resultado `2e8b0f3a8b6c2cc17f121502fc875aa890947dde8cea89eaf16e1c700d551bc1`, `ACCEPTED / COMPLETE` 4/4, re-autenticado |
| suites compartidas | `:shared:common-jre:test --rerun` con `G8B*`, `G8C1*`, `G8C2*` y `G9U0R4*` de `org.geocedg.common.locus`, y los paquetes `kernel.locus.intersection`, `kernel.locus.constructive` y `kernel.spatial` | correctas |
| suites de escritorio | `:desktop:desktop:test --rerun` con `PreG9BCu3*`, `G9U0R4*`, `G9U0LocalizationHelpTest` y `G9U1AlgebraSubmissionTest` | correctas |
| `INTEGRATION` | `verify.ps1 -Profile INTEGRATION -LogDirectory artifacts\agent\pre-g9b-cu3-e1-integration-50ac91c2` | `verification-af25e7abe1d34d6da63fc3f97fb085d5`, resultado `8a6eb31c02bde6236af3b5d3f3579a569e84ded197fce5d84d27cc31126b66f6`, `REJECTED_VERIFICATION_CORE / UNTRUSTED`, re-autenticado: 17 comprobaciones de aceptación satisfechas, ninguna violada y dos no fiables (`junit.shared.final.semantic` y `junit.desktop.final.semantic`: «Executed JUnit identities differ from the tracked selection inventory»); los cuatro productores JUnit terminan con código 0 (§10.8) |

Las mismas tres selecciones `PHASE` se habían aceptado ya sobre `86cb710f` (ensayo previo a las dos
pruebas de G-AUT-1). Las suites de E1, con las 131 pruebas del paquete `constructive`, las 19 de la
evidencia certificada y de los ejes, y las de escritorio, pasan.

## 9. Las dos incidencias en los cinco contactos

| Contacto | `P ∈ L` | `P ∈ r` | Vía |
|---|---|---|---|
| `Intersect(e_1, n, K)` | `STRICT` | `RELAXED` | margen en `t = 0`; (c1) con el sustituto de la rotación |
| `Intersect(e_1, c_1, K)` | `STRICT` | `RELAXED` | margen en `t = 0`; (c1) |
| `Intersect(o_1, h_2, K)` | `STRICT` | `RELAXED` | margen en `t = 0`; (c1) |
| `Intersect(e_1, c_1, J)` | `STRICT` | `RELAXED` | margen en `t = π`; (c1) |
| `Intersect(e_1, r, J)` | `STRICT` | `RELAXED` | margen en `t = π`; (c1) |

`P ∈ r` no es `STRICT` en E1: la evaluación exacta demuestra cada incidencia para el valor binary64
del sustituto de la rotación, y `SUBSTITUTE_GENERIC` la comprueba en las ocho perturbaciones de la
versión 1, lo que es evidencia computacional y no prueba. La identidad para todo valor del sustituto
es el objeto del certificado de E2. El orden de contacto tampoco se decide en E1.

## 10. Limitaciones y cuestiones abiertas

1. **Censo de procedencia y presupuesto de la evaluación exacta (cuestión abierta).** Con el
   evaluador del producto y `cu3-exact-evaluation-budget/v1`, ninguna de las 160 relaciones del
   censo se refuta y 140 se cumplen exactamente. Las veinte restantes pertenecen al cono de la
   rotación (`H`, `S`, `W`, `U`, `A_1`, `B_1` y `C_1` sobre `f_1`, `h_1`, `i_1`, `j_1`, `k_1`,
   `l_1` y `m_1`, y los paralelismos de `h_1` y `k_1` con `k`). Necesitan 4 126 bits frente a los
   4 096 del presupuesto, fallan cerradas y quedan sin decidir. El estudio decidió las 160 con un
   presupuesto de caracterización: 200 000 000 operaciones, 2²² bits y profundidad 48. El
   presupuesto aprobado se aplica a la evaluación de un punto estructural frente a su objetivo, y
   el censo es una regresión de la semántica, no un consumidor de producto. Por eso este registro
   no propone ningún cambio: el límite no se eleva y ningún agotamiento se acepta como aproximación.
   Queda para el autor decidir si el censo completo debe reproducirse con un presupuesto propio de
   caracterización, fuera del producto, o si basta la regresión sin refutaciones.
2. **H02 en traslaciones grandes.** El segmento inclinado de `pins.H02` (de `(−2r, 0,4r)` a
   `(2r, 0,6r)`), trasladado a `T = 10⁶` con `r ≤ 20`, sigue perdiendo una raíz. La representación
   binary64 del punto queda por encima de `τ_rel·L`, y la corrección necesitaría un suelo de
   posición, que las condiciones de parada prohíben. La matriz no lo incluye: V-H2-1 barre la recta
   y ese segmento en el origen, en todos los radios, y V-H2-2 barre las traslaciones con la recta
   `y = T + 0,5 r` (§1 de la matriz); las dos se cumplen.
3. **Pliegues del modelo.** La transferencia por estructura de los pliegues de `e_1` en `K` y en `J`
   da `STRUCTURAL_PREMISE_NOT_STRICT`, porque sus premisas de simetría pasan por el corte de la
   rotación. El certificado de E2 debe establecerlas.
4. **Nueva decisión.** La vía de nueva decisión de la transferencia está implementada para la
   localización de una raíz. Las demás decisiones cerradas (conteo de Sturm, multiplicidad, valor
   crítico) dan `CLOSED_DECISION_NOT_REDECIDED` y fallan cerradas; ningún consumidor de E1 las pide.
5. **Sin acción de menú.** La adopción se ofrece cuando una entrada la necesita (diseño de E1 §4).
   Una acción de menú aparte tocaría las validaciones fijadas del perfil de la aplicación, así que no
   se añadió. Queda disponible para una decisión del autor.
6. **Incidencias refutadas con sustitutos.** En el modelo, V-A-17 y V-A-19 fallan cerradas con
   `INCIDENCE_NOT_ESTABLISHED` y no como refutación: una refutación exacta con un sustituto no
   constructible no es rigurosa para el objeto ideal (especificación §7, apartado b). La salida
   certificada es la del certificado de E2 (V-AUT-DYN-1).
7. **Otras.** `DRIVER_DOMAIN_UNDECIDED` no es alcanzable, porque `D*` es exacto. Los pares
   (V-H2-PAIR-1) no cambian. El orden de contacto no se decide en E1. La posición materializada de un
   punto estructural es el centro de su encierro riguroso, con su error declarado aparte de la
   integridad.
8. **`INTEGRATION` rechazada por el inventario.** Los commits de inventario de E1 (`c9e9a2bc`,
   `86cb710f` y `50ac91c2`) renovaron las selecciones de descubrimiento y de fase, pero no los pins
   de las selecciones ejecutadas `final.shared` y `final.desktop`, que solo se renuevan con la
   evidencia de una ejecución completa (`-SelectionEvidencePath`). Por eso la ejecución
   `INTEGRATION` sobre el commit de las puertas se rechaza sin ninguna comprobación violada: las
   identidades ejecutadas, con las pruebas nuevas de E1, difieren de las fijadas. Ningún resultado de
   producto cambia y los cuatro productores JUnit terminan con código 0. La corrección es renovar
   esos pins con evidencia ejecutada en el inventario de E2; la ejecución `INTEGRATION` de la puerta
   de E2 cubre también E1. Las selecciones `PHASE` no dependen de esos pins y se aceptaron.

## 11. Pendiente para E2 a E4 y pruebas manuales

- **E2.** Certificado de identidad constructiva v1 en el producto, con monomios de 16
  indeterminadas, la corrección del defecto de pertenencia a segmentos del prototipo con pruebas
  adversarias, la clave `cic-structural-key/v1` por identidad durable, celdas y recomprobación; subida
  de `P ∈ r` de `RELAXED` a `STRICT` y retirada por `NOT_AN_IDENTITY`; orden de contacto por jets
  exactos; premisas de los pliegues; puertas G-AUT-2, G-AUT-9 y G-AUT-10, y G-AUT-8 para el
  presupuesto del certificado. Filas V-M3-9, V-CFG-5, V-CFG-6, V-AUT-ID, V-AUT-CIC y V-AUT-DYN-2 a 4.
  Renovación de los pins de `final.shared` y `final.desktop` con evidencia ejecutada y ejecución
  `INTEGRATION` en la puerta de E2 (§10.8).
- **E3.** Certificado de paridad en la regla de conflicto, reclasificación de la costura con la
  inversión de `pins.B08_B09`, y `CU3-B`. Filas V-B-2, V-B-NOISE-1, V-CONF-5, V-B-7 a V-B-10 y
  V-B-12.
- **E4.** Selecciones `pre-g9b-cu3.*`, inventario `final.*`, verificación `INTEGRATED_PHASE`,
  registro de cierre, nota de migración de D-CU3-12 y documentación de desarrollo y de usuario.
- **Pruebas manuales del autor en el escritorio.**
  - La oferta de adopción en inglés y en español: el informe, Adoptar y Cancelar.
  - Deshacer de la adopción con el punto creado.
  - Las filas del inspector de definiciones y los campos del diálogo de resultados ricos.
  - La apertura de un documento adoptado con un producto anterior.
