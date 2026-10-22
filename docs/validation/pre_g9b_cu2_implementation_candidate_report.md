# PRE-G9B-CU2 — Informe del candidato técnico de implementación

| Campo | Valor |
|---|---|
| Carácter | Informe del candidato técnico `CU2`: certificación híbrida de existencia, autoridad única de cierre, migración de los consumidores, representación y exportación, y corrección de B07, B10.2, H01 y B06 |
| Estado | `CU2 IMPLEMENTATION CANDIDATE — PENDING AUTHOR REVIEW`; `selfApproved=false`; no se declara `PASS — AUTHOR APPROVED`; el cierre autoral no se ejecuta; `CU3`–`CU10` no autorizados |
| Autorización | instrucción autoral «GeoCeDG — PRE-G9B-CU2. Aprobación autoral de la arquitectura y autorización condicionada de implementación» de 2026-10-21 ([registro de aprobación](pre_g9b_cu2_author_architecture_approval_record.md)); decisión autoral D-CU2-T11-R1 (2026-10-21); «Resolución autoral de las decisiones previas a T12» (2026-10-22) |
| Base autorizada | `070af6f902cd0be291247e73efb5629ef2a39eba`, árbol `49a659d8b334aae3f69c4db000cc842204e9a85f` |
| Rama | `phase/pre-g9b-cu2-implementation` (local, sin publicar) |
| Clase de verificación | `INTEGRATED_PHASE` (D-CU2-12): `STATIC`, `INFRA_UNIT`, `PHASE -Phase PRE-G9B-CU2`, `PHASE -Phase PRE-G9B-CU1`, `PHASE -Phase PRE-G9B-CU0` e `INTEGRATION` sobre el candidato congelado, y los bancos `certification` y `tessellation` antes y después |
| Identidad del commit y resultados de verificación | registro de verificación `pre_g9b_cu2_verification_record.md`, que añade un commit documental descendiente (un commit no puede citar su propia identidad) |

## 1. Comprobación de la base

| Comprobación | Resultado |
|---|---|
| Identidad | `070af6f902cd0be291247e73efb5629ef2a39eba` existe con el árbol `49a659d8b334aae3f69c4db000cc842204e9a85f`; es el cierre de coherencia C1–C3 publicado de la preparación de `CU2` |
| Entrada | `PHASE -Phase PRE-G9B-CU0` (`verification-b57e1dccad8f4570b10394be58e60f9d`, 2/2) y `PHASE -Phase PRE-G9B-CU1` (`verification-4dca68ed6f484b20a38f83fde3c7eaec`, 4/4), `ACCEPTED / COMPLETE` y autenticadas sobre la base |
| Espacio de trabajo | limpio al empezar; la rama se creó en la base exacta |

## 2. Alcance ejecutado

| Paso | Contenido | Commit |
|---|---|---|
| T0 | Normas aprobadas incorporadas antes del código (A2.1-R a A2.6), ADR 0038 aceptado, registro de aprobación y prompt canónico | `0b9c7d64`, `9bfafbf0` |
| T1 | Contadores de certificación (`LocusCertificationWork2D`), bancos `certification` y `tessellation` antes, y caracterización de D-CU2-20-R | `cfa30bad` |
| T2 | Estructura de existencia v2 sin cambio visible: arcos, extremos tipados, componentes puntuales, naturaleza de la evidencia, evidencia exploratoria | `9d95f13a` |
| T3 | D-CU2-20-R: huella exacta v2 y fidelidad de reconstrucción del corte; clase v3 del modelo de construcciones certificadas con resultados de caja tipados | `2e29ba1d`, `df4057ec` |
| T4 | Álgebra exacta de los predicados de primer nivel y certificador híbrido `locus-existence/hybrid/v1` en sombra | `b0618b06`, `91656ade` |
| T5 | Pliegues certificados del mapa trazado (`fold-symmetry/v1`) y chorros de segundo orden | `2df65061` |
| T6 | Certificado estructural de una función métrica y evento `ORIGIN_WRAP` | `ea24c77b` |
| T7 | Modelo del autor certificado en sombra | `00241c65` |
| T8 | Linaje estructural y resolución de selectores (E-3) | `2328aeaf` |
| T9 | Consumidores sobre la estructura de existencia y autoridad única de cierre | `1ace9e4b` |
| T10 | Política de dibujo v2 y aproximaciones de exportación | `3e711f47` |
| T11 | Conmutación: certificados publicados, asentamiento de la selección (D-CU2-T11-R1) y exportaciones parciales | `ae7237c8` |
| Resolución previa a T12 | H10 (opción A), V-CON-6 (opción 2), componente puntual de `restrictedTo` y limitaciones C09, C22/C23 y C51/C70 | `6382adfe` |
| T12 | Inversión de las fijaciones de `CU0` y registro de nueva línea base | `7f9eca77` |
| Corrección en T13 | Etiquetas por defecto, inglesa y española del diagnóstico `EXISTENCE_NOT_CERTIFIED` de la resolución previa a T12, cuya falta detectó la selección ejecutada `final.desktop` | `4e394dc3` |
| T13 | Registro de la fase `PRE-G9B-CU2` con el mecanismo oficial, inventario refrescado con evidencia ejecutada, fijaciones de infraestructura (§12), este informe, su espejo y documentos vivos | candidato |

## 3. Cambios por paso

- **T1.** Contadores públicos por locus: cálculos de certificado y de huella, cajas de existencia,
  lecturas métricas durante la certificación, bandas sin resolver y trabajo de fidelidad. Bancos de
  antes en `artifacts/benchmarks/pre-g9b-cu/cu2-before-{certification,tessellation}-cfa30bad`.
- **T2.** `LocusExistenceStructure2D` v2: topología del dominio, cobertura con causa tipada y
  naturaleza de la prueba, componentes de intervalo y de arco que cruzan la costura, extremos
  `LocusBoundary2D` con fuente, componentes puntuales y consulta única de cierre
  (`isClosedPeriodic()`).
- **T3.** Huella de revisión v2 exacta (estructura, valores binary64, estado de selección y de
  etiquetas, versiones) y validación de la fidelidad del corte reconstruido, con restauración
  demostrable o fallo explícito (`RECONSTRUCTION_FIDELITY_NOT_ESTABLISHED`,
  `SELECTION_NOT_REPRODUCED`). Clase v3: rayos y segmentos, circunferencias, intersección
  recta–circunferencia con su estado de selección leído por accesores de solo lectura
  (`AlgoIntersectLineConic`, `AlgoIntersectSingle`, registrados como modificados), salidas
  indexadas, vectores, macros y entrada métrica estructural; límite de 256 pasos.
- **T4.** Biblioteca exacta (`kernel/locus/exact`: racionales, Sturm, factorización libre de
  cuadrados) y certificador híbrido: partición diádica con la costura, transiciones certificadas,
  certificado algebraico exacto de los predicados de primer nivel (D-CU2-16), puntos estacionarios,
  banda de salidas coincidentes sin etiqueta (D-CU2-18) y evidencia exploratoria (E-1).
- **T5.** Pliegues certificados por simetría estructural exacta, aislamiento y no degeneración.
- **T6.** `LocusMetricFunctionStructure2D` y `RouteSourceFacts2D`: la longitud orientada se
  certifica por su estructura, nunca por su valor (`locus-v2-metrics.md` §26); `ORIGIN_WRAP`
  con `jumpPositive` certificado; un valor métrico constante queda fuera de la clase
  (`metric-value-constant`).
- **T7.** El modelo del autor se certifica en sombra: las seis curvas cerradas de los parámetros
  del autor, los pliegues de `e_1` y el arco abierto de longitud completa de `e_2`; las mordeduras
  contienen la referencia de 50 dígitos; 0 lecturas métricas durante la certificación (GC-6).
- **T8.** Claves de linaje por las fuentes estructurales de los extremos, transiciones tipadas y
  `LocusComponentSelectorResolver2D` (E-3), sin coordenadas, índices ni proximidad.
- **T9.** Todos los consumidores leen la estructura: rutas sobre arcos, posiciones, intersecciones,
  puntos semánticos, interacción, selector principal, loci anidados, imágenes por semejanza, dibujo y
  exportación; límites laterales de extremos excluidos; separación P5 entre existencia y evaluación.
- **T10.** Dibujo `locus-render-policy/v2` (profundidad mínima 4, giro máximo 10°, presupuesto de
  4 096 vértices, exenciones solo en pliegues certificados, cortes marcados, fidelidad declarada) y
  exportaciones DXF y LaTeX con extremos declarados, residuo de cierre y manifiesto de esquema 3.
- **T11.** Los productores dependientes publican el certificado híbrido; la compatibilidad se
  retira; los productores estáticos y analíticos publican su certificado de proveedor
  (`locus-existence/provider-proved/v1`); regla de publicación H01; recubrimiento vacío válido
  (D-CU2-8); raíz seleccionada de `SplineV2` (D-CU2-9); asentamiento de la selección
  `selection-settlement/v1` y no existencia independiente de la selección (D-CU2-T11-R1);
  exportación por partes con cortes declarados.
- **Resolución previa a T12.**
  - **H10:** estimación condicionada de clase 3 de la longitud total de una construcción fuera de
    la clase v3 (`locus-v2-metrics.md` §26.5), sin autoridad certificadora.
  - **V-CON-6:** composición certificada mínima sobre un soporte locus completo con pasos
    estructuralmente totales (`locus-v2-certified-construction-model.md` §10.10).
  - **Componente puntual:** `restrictedTo` publica la existencia aislada que deja la declaración
    como componente puntual.
- **T12.** Fijaciones B07, H01, B06 y B10.2 de `CU0` invertidas in situ; extremos de rama medidos
  en el evaluador (V-TR-4); [registro de nueva línea base](pre_g9b_cu2_t12_new_baseline_record.md);
  corrección D-CU2-15 del manifiesto.

## 4. Decisiones autorales aplicadas

| Decisión | Aplicación |
|---|---|
| D-CU2-1, 2 y 4 | Certificado híbrido, clase v3 y evento `ORIGIN_WRAP` según las enmiendas aprobadas |
| D-CU2-3 | Opción A: `ORIGIN_WRAP` en el certificado estructural de la función métrica; ningún valor de `LocusLength` cambia |
| D-CU2-5 y D-CU2-10 | Constantes fijadas dentro de los rangos aprobados (§8) |
| D-CU2-6 a D-CU2-9, D-CU2-11 | Opción A en todas |
| D-CU2-12 a D-CU2-15 | Clase `INTEGRATED_PHASE`, base exacta, procedimiento C1-4 de inversión y corrección del manifiesto |
| D-CU2-16 a D-CU2-19 | Certificado algebraico exacto, accesores de solo lectura, tratamiento conservador de salidas coincidentes y componentes puntuales declaradas y no dibujadas |
| D-CU2-20-R | Huella exacta y fidelidad de reconstrucción antes de cualquier certificado (T1–T3) |
| E-1 a E-3 | Evidencia exploratoria dibujada solo en bandas sin resolver; dirección en componentes locales; identidad por igualdad de la descomposición o transición registrada |
| D-CU2-T11-R1 | Asentamiento determinista de la selección heredada, no existencia independiente de la selección y continuación de T11 |
| Resolución previa a T12 | H10 opción A, V-CON-6 opción 2, corrección de la componente puntual y fallo cerrado de los tokens históricos |

## 5. Puertas por contadores GC-1 a GC-8

| Puerta | Comprobación en el candidato | Prueba |
|---|---|---|
| GC-1 | Mover solo el conductor publica 0 revisiones y hace 0 cálculos de certificado, también dentro y fuera de un hueco tras un asentamiento | `PreG9BCu2SelectionSettlementTest`, banco `certification` (contadores de `PreG9BCu2CertificationInstrumentationTest`) |
| GC-2 | Como máximo 1 cálculo de certificado por locus afectado y revisión | `PreG9BCu2HybridCertificationTest` |
| GC-3 | Los consumidores leen el certificado publicado: 0 cálculos adicionales por consulta | `PreG9BCu2HybridCertificationTest`, `PreG9BCu2SelectionSettlementTest` |
| GC-4 | Cajas de existencia dentro del presupuesto de 4 096 por rama y revisión; el agotamiento solo con `UNRESOLVED` tipado | `PreG9BCu2HybridCertificationTest`, `PreG9BCu2AuthorModelCertificationTest` |
| GC-5 | Vértices de dibujo dentro del presupuesto de 4 096; un presupuesto menor engrosa el dibujo sin cruzar fronteras | `PreG9BCu2RepresentationTest.theVertexBudgetCoarsensWithoutCrossingBoundaries`, banco `tessellation` |
| GC-6 | 0 lecturas métricas durante la certificación de `e_2` (`0/0/1`: creación, apertura y revisión) | `PreG9BCu2AuthorModelCertificationTest`, `PreG9BCu2MetricStructureTest` |
| GC-7 | Informativa: bancos antes y después | registro de verificación |
| GC-8 | El certificado exacto no supera sus presupuestos de bits y bisecciones en el modelo y en las configuraciones sintéticas | `PreG9BCu2HybridCertificationTest`, `PreG9BCu2AuthorModelCertificationTest` |

## 6. Conformidad de pruebas heredadas

- Desde la base, el candidato añade 31 archivos de prueba (30 clases con pruebas y el soporte
  `LocusV2TestBranches`) y modifica 68.
- [Registro de conformidades de T11](pre_g9b_cu2_t11_test_conformity_register.md): 71 archivos de
  prueba modificados en el paso T11, 73 conformidades propias (C01–C73), 13 correcciones de la
  auditoría de vacuidad y 7 conformidades agrupadas. Su §7 registra la resolución previa a T12:
  - restauradas: C35, C57, C58, C60, C62 y C63;
  - limitaciones registradas: C09, C22/C23 y C51/C70.

  Su §8 registra la conformidad del fixture `scalar-locus` de la matriz GGBScript, que T11 cambió
  sin registrarla y que la ejecución de desarrollo de `INFRA_UNIT` detectó en T13 (§9).
- [Registro de nueva línea base](pre_g9b_cu2_t12_new_baseline_record.md) del paso T12:
  - `preserve`: 621 de 661 hojas idénticas y 40 dentro de tolerancia;
  - `pins` de `CU2`: 100 campos invertidos;
  - `pins` de `CU1`: conservan su inversión;
  - `pins` de `CU3`: sin cambios.

  La línea base de `CU0` no cambia.
- No se ha eliminado ninguna prueba. Cada aserción heredada que cambia es una conformidad
  declarada con el contrato aprobado que la justifica, y la cobertura que se pierde consta como
  limitación (§10).

## 7. Referencias numéricas

| Valor | Base (`T10` y `CU1`) | Candidato |
|---|---|---|
| H10, longitud de `sin(2πkx)` en `[0,1]`, `k = 1, 2, 4` | 4,188275203684875; 8,111828618823123; 16,06471513503404 (`ESTIMATED`) | idénticos bit a bit, `ESTIMATED` condicionada (§26.5) |
| Longitud total de `o_1` | 13,236458195804072 | idéntica |
| `d_2` en el estado del modelo | 5,026489133092055 | idéntico |
| `e_2` | una componente periódica cerrada de compatibilidad | un arco abierto de longitud completa desde `t_N = 0`, `COMPLETE` (B10.2) |
| Mordedura de `o`, `v`, `w` y `a_1` en 3,7 | una componente `[−π, π)` cerrada | un arco certificado `[1,5117077290126846, 7,325007837228292]` |

## 8. Constantes versionadas (D-CU2-5 y D-CU2-10)

| Constante | Valor | Rango aprobado |
|---|---|---|
| Anchura de los corchetes de transición | `2^-40` periodos | `2^-48`–`2^-32` |
| Cajas de existencia por rama y revisión | 4 096 | 1 024–16 384 |
| Profundidad mínima de existencia | 6 | 4–8 |
| Límite de pasos dependientes del conductor (clase v3) | 256 | 128–512 |
| Bits de coeficiente del certificado exacto | 4 096 | 1 024–16 384 |
| Bisecciones exactas por raíz | 512 | 256–2 048 |
| Dibujo: profundidad mínima, giro máximo y presupuesto de vértices | 4, 10° y 4 096 | candidatos de DC-6 |

La política del certificador es `hybrid-existence-policy/v1` (además, 32 muestras exploratorias
por banda y cajas de aislamiento de pliegues de `2^-8` a `2^-30` periodos). La justificación de
los valores por bancos reproducibles consta en el diseño de certificación (§4.6) y en el diseño de
consumo (§5.4).

## 9. Precisiones de implementación y hallazgos

Las precisiones de cada paso están en el texto del candidato de los diseños (certificación §4–§9,
consumo §4–§7, evento de la longitud orientada, análisis del modelo del autor) y de las
especificaciones enmendadas. Hallazgos registrados durante la implementación:

- F-CU2-12: su atribución se corrigió en T1: el efecto observado no venía de la huella, sino de la
  regla H01, que impedía publicar la revisión (F-CU2-3).
- F-CU2-15: bandas de pertenencia a un rayo de `o_1` por debajo de `1e-9` hacían falsamente
  inexistente `e_2` en T6; corregido en T7.
- F-CU2-16: claves estructurales solo con naturalezas que certifican (T9).
- F-CU2-17: `SemanticGeneratorDomainProvider1D.canonicalize` desplaza un ulp los parámetros del
  dominio (`lower + (p − lower)`); preexistente y no corregido. Las referencias de `CU0` lo
  reproducen (V-TR-4).
- Defecto destapado por C70 en T11: orden de la cobertura en `restrictedTo`; corregido con prueba de
  regresión.
- La resolución previa a T12 añadió el diagnóstico `EXISTENCE_NOT_CERTIFIED` sin etiquetas en las
  tablas `menu*.properties`. Lo detectó en T13 la selección ejecutada `final.desktop`
  (`G9U0LocalizationHelpTest.l02RichStatusAndDiagnosticLabelsAreLocalized`); se corrigió en
  `4e394dc3` y la evidencia del inventario se volvió a producir sobre el árbol corregido.
- La ejecución de desarrollo de `INFRA_UNIT` de T13 (`verification-827c8f612b76423083b8e826a1dc910b`,
  `REJECTED_VERIFICATION_CORE`, 20/22) detectó dos fijaciones de infraestructura sin actualizar:
  - `infra.static-semantic`: T11 cambió el fixture `scalar-locus` de la matriz GGBScript de
    `PRE-G9B-R6` (una forma afín escalar no directa, ahora fuera de la clase v3, pasa a un locus
    trazado por un punto de circunferencia), pero no actualizó su huella en el catálogo de
    contratos estáticos ni lo registró como conformidad. Ahora consta en el
    [registro de conformidades de T11](pre_g9b_cu2_t11_test_conformity_register.md) §8;
  - `infra.final-coverage`: los recuentos de selecciones y de fases que fija
    `verification-final-coverage.Tests.ps1`, que `CU0` y `CU1` también actualizaron al registrar
    sus fases.

  Las dos se corrigen en T13 (§12).

## 10. Limitaciones y deudas

- **Construcciones fuera de la clase v3** (expresiones afines no directas, loci escalares
  periódicos, soportes locus sin composición total, puntos por expresión arbitraria, raíces
  seleccionadas fuera de D-CU2-9): `NOT_ESTABLISHED` sin componentes. Sus consumidores no infieren
  nada; su longitud total es la estimación condicionada de §26.5.
- **V-CON-6** se limita a soportes `COMPLETE` sin componentes puntuales y a pasos de la tabla
  `structural-totality/v1`; los parámetros singulares y los pliegues del soporte no se componen; el
  dependiente compuesto no tiene modelo de curva certificado.
- **C09:** identidad de bytes de las exportaciones DXF de loci escalares fuera de clase.
- **C22 y C23:** materialización de raíces en componentes puntuales, de `CU3` (D-CU2-19).
- **C51 y C70:** documentos anteriores a `CU2` con selectores o tokens de componentes parciales;
  quedan latentes con fallo cerrado.
- **C40:** seguimiento transversal de raíces sobre loci dependientes certificados (`CU3`).
- **`e_2`**: dos cortes computacionales de unos `1,5·10⁻¹⁰` donde la longitud de `w_1` es
  `LIMIT_NOT_ESTABLISHED` (métrica de `CU1`, preexistente), declarados en dibujo y exportación.
- **F-CU2-17** (canonicalización con un ulp), preexistente.

## 11. Decisiones autorales pendientes

- Revisión del candidato técnico y de sus evidencias de verificación.
- Revisión de los textos normativos incorporados como candidato:
  - `locus-v2-metrics.md` §26 con §26.5;
  - `locus-v2-certified-construction-model.md` §10 con §10.10;
  - `locus-v2-existence-components.md` §15 con las precisiones de §15.7 y §15.8;
  - las precisiones de implementación de los diseños.
- Revisión de las conformidades declaradas del registro de T11 y del registro de nueva línea base
  de T12.
- Revisión de las filas de `CU2` del registro E1, que se actualizan en el commit documental del
  registro de verificación.
- Disposición de las limitaciones de §10 y de su propietario.

## 12. Infraestructura de verificación modificada

- Fase `PRE-G9B-CU2` en `verification-registry.json`, que exige `compile.shared.semantic`,
  `compile.desktop.semantic`, `junit.shared.pre-g9b-cu2.semantic` y
  `junit.desktop.pre-g9b-cu2.semantic`, con sus cuatro nodos productor/proyección en la forma de
  los de `CU1`.
- Selecciones nuevas en `verification-junit-inventory.json`, con todas las clases de prueba que
  añade `CU2`:
  - `pre-g9b-cu2.shared`: 21 clases y 192 pruebas, las 13 `PreG9BCu2*` y las 8 de la biblioteca
    exacta `kernel/locus/exact` de T4;
  - `pre-g9b-cu2.desktop`: 9 clases y 46 pruebas.

  Su `input_identities` es el commit T0 `9bfafbf0`, como las selecciones de `CU1` citan su
  commit S0.
- Recuentos refrescados:
  - `discovery.shared`: 6 098 → 6 290, y `final.shared`: 7 015 → 7 207 (+192, las 21 clases
    nuevas);
  - `discovery.desktop`: 2 185 → 2 231, y `final.desktop`: 2 179 → 2 225 (+46, las 9 clases
    nuevas).

  Las 47 clases shared y las 16 Desktop que modifica el candidato conservan su número de
  pruebas: las conformidades de T11 cambian aserciones, no casos.
- El actualizador oficial `tools/agent/update-verification-junit-inventory.ps1` rellenó las
  selecciones con la evidencia del productor oficial, sobre el árbol del commit `4e394dc3`. Las
  demás selecciones no cambian.
- Huella `junit_inventory` del registro: `4e105851…07f8` → `6ba80345…5c2f`, calculada con
  `Get-VerificationCanonicalTextSha256`. La huella anterior se reprodujo desde el blob de `HEAD`.
- `verification-final-coverage.Tests.ps1`, en la forma de `CU1`:
  - 66 → 68 selecciones y 70 → 71 fases `PHASE`;
  - fijaciones de los recuentos de `pre-g9b-cu2.shared` (192) y `pre-g9b-cu2.desktop` (46).
- `verification-static-contracts.json`: la huella canónica de la matriz GGBScript pasa de
  `f167fa71…` (la de la base) a `ee99a54f…` (la del fixture de T11). La huella `static_contracts`
  del registro pasa de `a670be95…d8ec` a `286d787d…5c6e`.

## 13. Entregables de la instrucción

| Entregable (registro de aprobación §11; prompt de `CU2`) | Dónde está |
|---|---|
| Commit técnico exacto, árbol y rama local | registro de verificación (commit documental descendiente) |
| Matriz de correspondencia entre decisiones e implementación | §4 de este informe, con el detalle por paso de §2 y §3 |
| Especificaciones y ADR | enmiendas A2.1-R a A2.6 incorporadas en T0 (versiones en el [registro de aprobación](pre_g9b_cu2_author_architecture_approval_record.md) §2) y precisiones de 2026-10-22: `locus-v2-metrics.md` §26.5, `locus-v2-certified-construction-model.md` §10.10 y `locus-v2-existence-components.md` §15.7 y §15.8. Todas como texto del candidato pendiente de revisión. ADR 0038: `ACCEPTED — ARCHITECTURE AUTHOR APPROVED WITH PRECISION D-CU2-20-R` |
| Código y pruebas, con su ubicación | §3. La semántica geométrica está en el núcleo compartido `org.geocedg.common.kernel`: `locus` (estructura, certificador, linaje), `locus/exact` (biblioteca exacta), `locus/intersection` (clase v3, pliegues, asentamiento), `locus/metric`, `algos` y `geos`. La presentación está en `euclidian/draw` y la exportación en `org.geocedg.common.export`. En Desktop solo cambian siete archivos: la exportación (preflight y manifiesto DXF de esquema 3, soporte LaTeX), el laboratorio de Locus V2 y el inspector de definiciones. Las clases heredadas `AlgoIntersectLineConic` y `AlgoIntersectSingle` reciben solo accesores de solo lectura (D-CU2-17). Las pruebas unitarias, sintéticas y adversarias están en `common-jre`, y las del modelo del autor, en Desktop |
| Evidencia de D-CU2-20-R | T1 y T3; `PreG9BCu2RevisionExactnessTest`, `PreG9BCu2SliceFidelityCharacterizationTest` y `PreG9BCu2AuthorModelSliceFidelityTest` |
| Certificación del modelo del autor | T7 y T11; §7; `PreG9BCu2AuthorModel*Test` |
| Casos sintéticos y adversarios | las 21 clases de `pre-g9b-cu2.shared` |
| Puertas GC-1 a GC-8 antes y después | §5; bancos del registro de verificación |
| Registro de inversión de fijaciones y líneas base conservadas | [registro de nueva línea base](pre_g9b_cu2_t12_new_baseline_record.md) |
| Resultados autenticados de `STATIC`, `INFRA_UNIT`, las tres `PHASE` e `INTEGRATION` | registro de verificación |
| Limitaciones, degeneraciones sin resolver y observaciones | §9 y §10 |
| Filas de `CU2` del registro E1 | se actualizan en el commit documental del registro de verificación, porque el registro E1 (§8) exige el commit y la verificación de la fase para cambiar un estado |
| Informe y espejo legible por máquina, con el estado del espacio de trabajo y del remoto | este informe, [`pre-g9b-cu2-candidate-evidence.json`](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu2-candidate-evidence.json) y §14 |
| Estado del roadmap y del plan | fila «Última fase ejecutada» del roadmap y fila de `CU2` del plan consolidado |
| Impacto en el arranque | ninguno: `AGENTS.md`, `CLAUDE.md`, `FIRST_AGENT_TASK.md`, `ai-shell/prompts/` y los prompts canónicos no cambian. Solo cambió el prompt de tarea de `CU2`, en T0 y por instrucción autoral |
| `GUIDE_IMPACT` | ninguno: la guía corresponde a `CU9` |

## 14. Estado del espacio de trabajo, del remoto y de la autorización

- **Espacio de trabajo.** El candidato está en la rama local `phase/pre-g9b-cu2-implementation`,
  sin rama remota de seguimiento. Las ejecuciones de aceptación parten de un árbol de trabajo y un
  índice limpios.
- **Remoto.** `main` = `origin/main` = `main` remoto =
  `48a0775e4f2f122f5e2614c62b5773777752ccd1`. El remoto tiene la rama
  `phase/pre-g9b-cu2-preparation` en la base `070af6f9` y ninguna rama de implementación de
  `CU2`.
- **Autorización.** `CU2` sigue siendo `CU2 IMPLEMENTATION CANDIDATE`. No se publica nada, no
  cambian `main`, las etiquetas, las releases ni `geocedg_book`, y no se ejecuta el cierre
  autoral. La aprobación queda reservada a la revisión autoral de las evidencias.
