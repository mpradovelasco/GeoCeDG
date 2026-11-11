# PRE-G9B-CU10 · Matriz de compromisos y registro de deudas

Conciliación CU10-A y CU10-E para revisión autoral. La base examinada es
`b6a86bf00dfb691c82e6009dfed1a9a0c20a865a`, árbol `8ebaeb3a545a7bdb435f72ea966fcd7f89594888`.
Las decisiones se atribuyen a su alcance y fecha. Las campañas anteriores conservan sus
identidades y veredictos. Los resultados de la campaña integrada se atribuyen en el informe
principal de CU10. `selfApproved=false`.

La conciliación se apoya en el [plan consolidado §§2–7](../architecture/pre_g9b_cu_corrective_usability_plan.md),
[roadmap vigente](../roadmap/geocedg_roadmap.md), registros autorales enlazados y
[registro ejecutable](../../geocedg/specs/operations/verification-registry.json). Los hitos
del plan fechados, las propuestas y los campos históricos de los espejos JSON conservan su
contexto. La aprobación de diseño, la verificación técnica, el smoke y la publicación son
actos de distinto alcance.

Las tablas usan **V** para cumplido y verificado en la identidad indicada, **L** cumplido con
limitación aprobada, **D** diferido por decisión autoral, **F** fuera del alcance aprobado,
**P** bloqueado o pendiente de decisión o de evidencia. La segunda parte desarrolla los
límites y los requisitos de reapertura de las filas correspondientes.

## 1. Matriz de compromisos CU0–CU9

| Fase | Alcance previsto → perímetro efectivamente adoptado | Disposición y fuente de verificación |
|---|---|---|
| CU0 | Caracterización reproducible, corpus con manifiesto, referencias `preserve` y `pins`, arnés y banco, reproducción ventaneada B02/B03, cobertura inicial de la guía y propuestas D–H. El autor aprobó los entregables y C1-0/1/3/4 el 2026-10-20. C1-2 exigió revisar A1.2 antes de implementar, para distinguir una garantía de longitud de la mera concordancia entre refinamientos. | **V** para caracterización y decisiones. **F** para código productivo y adopción normativa general de D–H. STATIC 3/3, INFRA_UNIT 22/22, PHASE CU0 2/2 (21 pruebas), INTEGRATION 19/19 autenticadas en la tabla §2. Smoke autoral de producto no aplicable. [Cierre CU0](pre_g9b_cu0_closeout_record.md) · [corpus y banco](pre_g9b_cu0_baseline_candidate_report.md) |
| CU1 | B10.1/H03/H04 y H10/H11/H12. Revisión por contenido, corte reutilizable, composición anidada con sesión y contadores, índices métricos reutilizables y cuatro clases de garantía. Se preservan las referencias geométricas y se reemplazan los `pins` por pruebas de aceptación. D-CU1-R1 a R6 y smoke aprobados el 2026-10-20. R6 sincronizó la prueba ISO A con su escritura de Undo. | **V** correcciones en su contrato y **L** límites de resolución, variación y presupuesto. **D** perfil de esfuerzo de documento H-d. **F** existencia, dibujo y materialización en CU1. STATIC, INFRA_UNIT, PHASE CU1 y CU0 e INTEGRATION 19/19 del candidato correctivo. Los dos rechazos 18/19 de `8ee19667` son históricos. [Cierre CU1](pre_g9b_cu1_closeout_record.md) · [ADR 0037](../adr/0037-locus-v2-metric-guarantee-classes-and-admissibility.md) · [métrica §25](../../geocedg/specs/locus/locus-v2-metrics.md#25-pre-g9b-cu1-normative-amendment-metric-guarantee-classes-estimator-v2-admissibility-and-budgets) · [semántica](../../geocedg/specs/locus/locus-v2-semantics.md) |
| CU2 | B07, B10.2, H01 y B06. C-1 a C-12, D-CU2-20-R y decisiones de arquitectura: programa de clase v3 y selección reproducible, certificación híbrida, huella exacta v2, transiciones y pliegues admitidos, `UNIFORMLY_INVALID`, cierre con autoridad única, consumidores y teselación por componente. El autor aprobó el 2026-10-24 con comprobación manual satisfactoria. | **V** cuatro incidencias; `e_2` en el caso certificado tiene un arco abierto `COMPLETE` desde `t_N=0`, cortado por `ORIGIN_WRAP`, con `o_1 → LocusLength → d_2 → e_2`. **L** dominio de clase v3, costura, presupuestos y disponibilidad numérica. **F** certificación por muestreo y materialización CU3. STATIC, INFRA_UNIT, PHASE CU0/CU1/CU2, INTEGRATION 19/19 en primera ejecución, matriz de 134 filas y regresión V-DXF-4 con AutoCAD Core Console según el registro. [Cierre CU2](pre_g9b_cu2_closeout_record.md) · [ADR 0038](../adr/0038-locus-v2-hybrid-existence-certification-and-certified-closure.md) · [existencia y componentes](../../geocedg/specs/locus/locus-v2-existence-components.md) |
| CU3-A | Etapa A, B08/B09 con conductor y H02/H06. La puerta histórica G-M3 refutó los contactos contra la representación fijada. El autor reanudó la implementación con autoridad constructiva de adopción explícita, integridad por relación, CIC, y transferencia condicionada desde CU2. Se materializaron las formas `Point(L,D)` e `Intersect(L,r,D)` en la clase admitida. R1–R6 y cierre de A el 2026-10-27, tras disponer las observaciones del smoke. | **V** incidencias certificadas y ejes; **L** H02 a gran traslación, familias y presupuestos de los certificados. **F** punto crítico sin incidencia y adopción predeterminada. INTEGRATED_PHASE sobre `f61f2954`. [Cierre A](pre_g9b_cu3_a_closeout_record.md) · [ADR 0039](../adr/0039-locus-v2-structural-points-and-certified-contact-evidence.md) · [ADR 0040](../adr/0040-constructive-geometric-authority-and-relation-integrity.md) · [autoridad constructiva](../../geocedg/specs/locus/locus-v2-constructive-authority.md) |
| CU3-B1/B2 y cierre global | Etapa B sin conductor: anclas de pliegue certificado B1 y anclas de tangencia regular B2 de clase 1 o 2 con precondiciones P1. Libro de identidad v6, vigencia, latencia, lado/germen y recuperación dentro de sus pruebas. Smokes satisfactorios. B1, B2 y cierre global condicionadamente aprobado el 2026-10-28 (D-CU3G-1–4). | **V** los tres casos obligatorios de §3. **L** producción de certificados regulares en las clases demostradas, con caso efectivo `o_1·h_2`. **D** generalización y verificación integrada de la guía cambiada tras el producto. **F** certificación universal, C3, puntos críticos sin incidencia y C22/C23/C40. INTEGRATED_PHASE de `aabfd0b9` y `2d13ad4c`. Se conservan el rechazo 18/19 y la repetición 19/19 en el segundo SHA. [B1](pre_g9b_cu3_b1_closeout_record.md) · [B2](pre_g9b_cu3_b2_closeout_record.md) · [cierre global](pre_g9b_cu3_global_closeout_record.md) · [intersecciones](../../geocedg/specs/locus/locus-v2-intersections.md) |
| CU4 | B01 inserción, B05/M01/M05.1/H09 en DC-1/DC-3 (valores, etiquetas, estilos y apariencia), B2 angular certificado incluido `Text(theta)` terminal, diagnósticos de F3/P4/B2/herramienta y copia nativa de vistas. Transacción aislada, confirmación vigente, remapeo L1, persistencia e historial. El autor confirmó smoke y adoptó B2 el 2026-11-07. | **V** alcance certificado. **L** fronteras de P4/B2 y desfase visual del selector angular. **D** H08. **F** inserción general fuera de las tablas. FINAL 41/41 sobre `ba4d16018`, recibo `65a6ab2e89a1e83b7f375624903cc4912f9f152b7b6b796e8de5e2923bad47b0`. [Cierre CU4](pre_g9b_cu4_closeout_record.md) · [diseño](pre_g9b_cu4_author_design_approval_record.md) · [contrato de inserción §10](../../geocedg/specs/ui/document-insertion.md) |
| Inserción experta pos-CU4 | Seguimiento separado de exclusiones P4. Se adoptó el perímetro pasivo 2D EXP-01–EXP-04 con revisión, informe, consentimiento, ejecución aislada, recuperación, persistencia y guardado explícito. Smoke autoral del 2026-11-08 en `066b53d0`; el sucesor se verificó con la activación predeterminada y la limpieza de consola. | **V** perímetro pasivo y controles con overrides. **L** ARC-01 ordinario, familias y macros limitadas. **F** 3D, contenido activo, conversión angular experta y remapeo durable participante. STATIC 3/3, PHASE 6/6 (228 Desktop, 35 shared) y dos arranques del sucesor, uno desde app-image interna. [Cierre pos-CU4](post_cu4_expert_insertion_closeout_record.md) · [contrato experto](../../geocedg/specs/ui/expert-document-insertion-candidate.md) |
| CU5 | B02 altura de filas del protocolo medida con fuente/renderizador, B03 memoria de referencia de sesión e independiente por herramienta, H05 suspensión por paso e invalidación por instancia, M04/DC-4 sentido de ambos ejes canónicos y admisión focal de estos en Paralela/Perpendicular en Gráficos 1. El autor confirmó smoke y aprobó el 2026-11-09. | **V** PHASE CU5 4/4 (57 casos), INTEGRATION conjunta 19/19, foco de ejes 52 casos. **F** persistencia XML o identidad durable de ranuras y ejes y reasociación por etiqueta o proximidad. [Cierre CU5/CU6](pre_g9b_cu5_cu6_closeout_record.md#cu5--protocolo-memoria-y-orientación) · [memoria](../../geocedg/specs/ui/authoring-memory.md) · [orientación](../../geocedg/specs/presentation/orientation-cue.md) |
| CU6 | Diseño M07 y B04/M02/M03/M06/M10. Insignia del índice de la salida recta–cónica, ESC del menú, arrastre de rótulos con `labelOffset`, búsqueda de Álgebra, ayudas Valor/Definición y captura explícita de argumentos en cuatro campos. Se adoptaron persistencia Classic de capas numéricas explícitas y compatibilidad R5-B/D1 acotada. Smoke y aprobación autoral con limitación explícita el 2026-11-09. | **V** interacción adoptada y selección desde Álgebra con prueba de vigencia. **L** `OBS-CU6-M07-GV1-SLIDER-PICK`, abierta y no bloqueante. PHASE CU6 4/4 (114 casos), INTEGRATION conjunta 19/19. **F** identidad deducida de etiquetas e índices de presentación. [Cierre CU5/CU6](pre_g9b_cu5_cu6_closeout_record.md#cu6--interacción-presentación-y-persistencia) · [diseño CU6-D](../architecture/pre_g9b_cu6_d_interaction_design.md) · [ADR 0031](../adr/0031-canonical-english-command-surface-compatibility.md) |
| CU7-D y M08-S-R2 | M08/M09 fueron previstos como colocación y redefinición. El 2026-10-29 se aprobó el diseño prospectivo CU7-D. El 2026-11-02 se adoptó M08-S-R2 en su clase positiva: L→Q, política world, autoridades R1/R6 y R2/H1 separadas, Q transitorio, punto forward durable. La evidencia incluye dictamen independiente y smoke favorable comunicados por el autor. | **V** CU7-D como diseño y M08-S-R2 en su clase; **L** presupuestos y cobertura. STATIC de diseño y STATIC, INFRA_UNIT, PHASE (196 ejecuciones) e INTEGRATION del candidato M08-S-R2. **F** nueva dependencia Q o equivalencia entre admisibilidad local y completitud. [CU7-D](pre_g9b_cu7_d_closeout_record.md) · [cierre M08-S-R2](pre_g9b_cu7_i1_m08s_r2_closeout_record.md) · [ADR 0041](../adr/0041-locus-v2-point-placement-and-point-redefinition.md) |
| CU7-R5 | El 2026-11-05 el autor aprobó cierre reducido, smoke `PASS — AUTHOR REPORTED` y D1 (`conicPointState` v1). Tablas positivas de clic/Q sobre Locus V2 y Q nativa, arrastre y continuación certificados, similitudes tipadas y recuperación según rutas, con identidad y DAG. D1 tiene tabla FILE acotada y deja la ruta privada de rollback separada. | **V** cierre reducido. **L** familias, presupuesto, portabilidad y hosts de persistencia acotados. **D** U2, F8 desactivado y líneas científicas de §5. **F** movilidad y reconstrucción generales. Auditoría independiente y recibo FINAL histórico `aa8d0c33de7dd092b77f1d2ac5a9a8477ef910cdaa1e0d362fd4b230ce310222`, sobre `13ee3b874` exclusivamente. [Resolución R5](pre_g9b_cu7_r5_author_decision_record.md) · [matriz positiva](pre_g9b_cu7_r5_reduced_closeout_proposal.md#capacidades-propuestas-para-el-cierre-reducido) · [conciliación CU7](pre_g9b_cu7_status_reconciliation.md) · [D1 §8.3](../../geocedg/specs/units/unit-system.md#83-omission-rule-and-legacy-byte-identity) |
| CU8-D/I | M05: diseño de complejidad media y adopción v1 de paneles Desktop en Gráficos 1, fijos o ligados al lienzo, para deslizadores, botones, casillas y campos aptos. R1/R2: composición por cobertura, contexto y propiedades, reparación de límites del campo, compatibilidad nativa y restauración transaccional. El autor aprobó CU8-I/R1/R2 y confirmó smoke de R2 el 2026-11-10. | **V** composición, apariencia, propiedad, persistencia e historial de la tabla. **L** lectores anteriores, inserción y M07. **F** anidamiento, Gráficos 2 y transferencia de paneles. STATIC 3/3, PHASE 6/6 (285 Desktop y 49 shared), INTEGRATION 19/19 y smoke gráfico. El primer intento de INTEGRATION quedó interrumpido por un apagado registrado, sin veredicto de producto. [Cierre CU8](pre_g9b_cu8_i_closeout_record.md) · [v1](../../geocedg/specs/ui/control-layout-v1.md) · [R1/R2](../../geocedg/specs/ui/control-layout-r1.md) |
| CU9 | B11 políticas y ejemplos `LocusLength`. B12 dos guías, tareas, ejemplos, matriz 294, navegación, búsqueda local y comparación con Classic fijado. H07 rutas, formatos y disponibilidad vigentes. R1/R2 añadieron enlaces, figuras, clasificaciones y capturas prácticas. El autor aprobó las tres entregas el 2026-11-11. | **V** contenidos, 282 cubiertas y 12 exclusiones justificadas, estructura y evidencias históricas de §4. **L** anclas del visor y legibilidad de capturas reducidas. **P** comprobación de integración y empaquetado del candidato de CU10. [Cierre CU9](pre_g9b_cu9_closeout_record.md) · [mantenimiento de la guía](../../geocedg/specs/operations/documentation-maintenance.md) |

## 2. Identidades históricas contrastadas

`git show --no-patch --format='%H %T'` y `git merge-base --is-ancestor` comprobaron
las identidades y la ascendencia al baseline indicado. En las filas con dos evidencias,
el registro de cierre identifica cuál corresponde al smoke y cuál a la campaña técnica.

| Hito | Commit | Árbol |
|---|---|---|
| CU0 candidato | `636a0070b6f4e32a8f270c25cfa70d9bd3b284b7` | `8fac2cae5c799fe7da215641c2a94ae95044a84d` |
| CU0 verificación documentada | `d684e74050988a3051a1692bfcf0651440b1567a` | `6c59bdbfbee7a9dbba1b4d0a0f45e290d500c811` |
| CU0 cierre | `df675e9ee42e1a4caac0e3bf2d74ab2d30450581` | `76f2ddbe155b4a1b068ef475630e536ad5fc3d87` |
| CU1 candidato correctivo | `143f5d3793fc645b7c79503d7717997716637be2` | `4de3cd7150fe0ec12607c1ab2a9697134f9de453` |
| CU1 verificación documentada | `2fe8ed198e1861a5b0e4558e8088fe20901249e4` | `c9df612e0d55d310a964a397ce0108d63abaf135` |
| CU1 cierre | `27bf652f176e57ced3807b82afe14fc00b7586a9` | `94b50ab6b2b429a913a31e29c3d73f16175c64e6` |
| CU2 candidato | `6ff1ddf743bb3ee44a4e9a47f7dc238f30d642cb` | `3dfcc575c45ec9ec660199d86d166d89e32851c9` |
| CU2 verificación documentada | `098ec04f7ac72b691a989796637b3ff681e6e3de` | `a1012756d8e1ffa9bd5b74475c5fe10dc60a94f5` |
| CU2 cierre | `111d9d46d8d9a01cf31c91ed149e2780a6c8597d` | `ced26a83e02248ba50e64ef50d23eb4601c18d95` |
| CU3-A candidato | `f61f295447d75755e4c932a0f8f41533563550dd` | `875e1cbf5dca2b6d0bbea8b72d0c24ec3aa306c0` |
| CU3-B1 candidato | `dd3a2676d231f5c8b2c2bf0a9016eb6d8a5f7d2b` | `f4cbd64e8c147977a5e417fa70182f3b893e6a87` |
| CU3-B1 sucesor de pruebas | `aabfd0b952838f964d2f2c25b3ebcba643935345` | `c2b061ec0e6345147e561b93dbe0e8ef62898e8d` |
| CU3-B2 y producto de CU3 | `2d13ad4c9b61d6f443b691b8214a8b2361ad0451` | `64dca8e5138050c0e1c5fc6b659dd33ffa430728` |
| CU3 cierre global | `c75622e5c4cfb6c2d6ad9a05cd5ebe09fedb7bbb` | `ed847a2881aedc6d8d02737003a58c7fe145f136` |
| CU4 candidato | `ba4d16018cf683750a20619d9d59c84428bcbef7` | `6dc9ae9ecdd4dc05f3fb0a5fb23d3320d06ea477` |
| CU4 cierre | `6c8ccfe7ab6b6f82f35aa508c2c0843692d76293` | `59a5e9ec17965e54c421127a52c674207e07e98f` |
| Inserción experta smoke manual | `066b53d0cc2dd8e78cf1c542e6adea820ec77d5c` | `b60b95dec5c6c7de1e52b28c8de878d72f552448` |
| Inserción experta sucesor verificado | `bf25d95355e2cd27555e316cf8024e7fa927052e` | `eabab698973f25b36791780f3809916a180b5208` |
| Inserción experta cierre | `1544f8baf02cb9b0199e4b003082a669283de945` | `3dc060c43047d591c01f4f74400a3b2abb11b7eb` |
| CU5/CU6 producto | `547fdee503f837218db06991c6c17be366c10578` | `a22d1fe136fdbcc6dd953527394751c30935a082` |
| CU5/CU6 cierre | `876a67362939cb5208a9562e396bbd3add7dfc04` | `211a518644a51e6507a042c6320bdf13b9d8c349` |
| CU7-D diseño | `7e7a90e4e39db04ea49ee9ab1acf69ea34c81080` | `25e0e2f0a78670c31be39766021d3f7561bc3bf6` |
| CU7-D cierre | `caf67ffc3758eccdef5202b7524b6c2b4e5d2fd4` | `ca8ec1dd869ce8acc9d3c271036f11b6ee878c24` |
| CU7 M08-S-R2 producto | `86438edabaed06a6509c1be37cf06278941e0e46` | `96e90172201440739bfe8afb503059379c70ff70` |
| CU7 M08-S-R2 cierre | `eea7f57b8a7e8be9f5fe9356a9f7f989dd7fdf17` | `d62179e72dd2bb37226cba5956198809467cb7c1` |
| CU7-R5 candidato | `13ee3b8746d4d874824eb44ca1d05a3e57b494c2` | `a4fead2412bbb8859dd167e3c22b82c54e584633` |
| CU7-R5 propuesta auditada | `91566d0daff274ed85961ac41c593481c02cf21e` | `8529953c6887b0e1ecd1607f04e83ed1140b2ecc` |
| CU7-R5 resolución | `1c3893f539d926c16da837a42d439a1cf8bea34f` | `1c0f6e9187663b586acfc7c425053d7be0349c21` |
| CU8-I | `81b6fc0661a61c7fa7a853ab17584d6a55b39a19` | `b5d4028fa7aa32b95f175e7ac7a2467921cdb289` |
| CU8-I-R1 | `075407a3142e1afe3cffebb3327a1d8aca9beddc` | `f60e6a0a1c72fbcc5418704296b950132349942c` |
| CU8-I-R2 producto | `5b4f7f712c7a4fa32881ad58da3c46ff9808f92e` | `0c693aa6b9f6b83e16cf53efd0e7c736cb5155a8` |
| CU8 cierre | `6c032394d70a004a4b91a88338e647f13d365cfa` | `96f155ee138b5ec3d15707791d73c46cc3c134df` |
| CU9 inicial | `7035b01c501c9303cd75331c070d7d1061751e6b` | `0afd5d9289686f8c3772d28c2e3d4195f0bbcb16` |
| CU9-R1 | `e2be710c43e33a22648d9729cb71b091791f538b` | `51284a796191e6e9fe962a785d407a3a17e62e44` |
| CU9-R2 producto | `f5356cb1e7bb60ed152ff236b7dceb3fd64bad86` | `84289da22ca60159d17c8077f0a38bdb6d0db759` |
| CU9 cierre y base de CU10 | `b6a86bf00dfb691c82e6009dfed1a9a0c20a865a` | `8ebaeb3a545a7bdb435f72ea966fcd7f89594888` |

## 3. Conciliación científica de los casos obligatorios de CU3

| Caso obligatorio | Resultado con conductor | Resultado sin conductor y frontera |
|---|---|---|
| `e_1·n` | Ambas incidencias `STRICT` con conductor `K` en la clase admitida. | Ancla en el pliegue certificado de `0`, clase 2 `FOLD_CONTACT`. El objetivo limitado requiere la pertenencia a su porción efectiva. |
| `e_1·c_1` | Incidencias `STRICT` con `K` y `J` para los contactos de `0` y de la costura. | B1 materializa los pliegues certificados de `0` y `π`. D-CU3-10 conserva el de la costura con su clasificación de contacto estructural en pliegue. |
| `o_1·h_2` | Incidencias `STRICT` con `K`, orden dos en el punto regular `0`. | B2 permite `u_1`, ancla regular de clase 2, con signo certificado de la curvatura relativa, correspondencia y vigencia. |

Las cinco filas de incidencia con conductor del informe E3 incluyen adicionalmente
`e_1·r` con `J` en `π`. El recuento de tres casos obligatorios agrupa los contactos del
par `e_1·c_1`. La coincidencia numérica, un residuo pequeño o el aislamiento de un punto
crítico dejan la existencia sin demostrar. `STRICT` se asigna por relación bajo sus
hipótesis. En particular, el CIC y las transferencias admitidas operan dentro de sus
celdas y guardas. La continuidad y las componentes certificadas pertenecen al contrato
de CU2. Las transiciones regulares tangencia ↔ pareja y la ausencia regular certificada
K1/K2 permanecen sin demostrar en CU3-B2. [Expediente global, §§2–4](pre_g9b_cu3_global_closeout_dossier.md) ·
[E3, tablas §§4–7](pre_g9b_cu3_implementation_candidate_report.md) ·
[evidencia versionada del CIC](../../geocedg/validation/pre-g9b-cu/pre-g9b-cu3-constructive-identity-evidence.json).

## 4. Guía CU9 y atribución de la evidencia

El diagnóstico registrado `tools/agent/diagnostics/guide-structure-diagnostic.ps1` se
ejecutó en CU10 con `-RepositoryRoot` apuntando al checkout y `-ResultPath` apuntando a
`artifacts/agent/pre-g9b-cu10-matrix/guide-structure-diagnostic.json`. Salida 0,
`DIAGNOSTIC_CLEAR`. El cotejo local de fuentes, también con salida 0, registró en
`artifacts/agent/pre-g9b-cu10-matrix/guide-source-audit.json`:

| Fuente | Resultado observado en CU10 |
|---|---|
| Matriz | 294 entradas, 282 `COVERED` con grupo de clasificación o indicación de disponibilidad, 12 `EXCLUDED_WITH_REASON`, cero parciales y ausentes. Los destinos de cobertura resuelven en ambas ediciones. |
| ES y EN | 118 anclas estructurales, 48 enlaces internos con destino válido y 15 figuras por edición. El diagnóstico compara la secuencia bilingüe y detectó cero divergencias. |
| Inventario | 17 PNG, tamaños y SHA-256 cotejados. Ambas ediciones referencian los recursos correspondientes a su idioma y los comunes. |
| Fuente publicada | `git diff --name-only b6a86bf0 -- docs/user geocedg/validation/pre-g9b-cu/guide-coverage-matrix.json` sin diferencias en el momento del cotejo. |

La búsqueda y navegación en ejecución están acreditadas por la evidencia histórica
de PHASE CU9 e INTEGRATION en CU9 inicial y R1, app-image interna R1 fuera del checkout,
y 127 pruebas Desktop más smoke bilingüe de R2. La prueba empaquetada R2 cotejó los dos
Markdown y 17 PNG del JAR. El cotejo de CU10 anterior es estático y queda atribuido a las
fuentes. `CU10-GUIDE-INTEGRATION-CHECK` requiere la evidencia del árbol final y del
paquete de CU10. El valor histórico `CANDIDATE` en `status` del inventario conserva
su contexto. La adopción se acredita en el registro autoral de CU9.
[Guía ES](../user/geocedg_user_guide_es.md) · [guía EN](../user/geocedg_user_guide_en.md) ·
[matriz](../../geocedg/validation/pre-g9b-cu/guide-coverage-matrix.json) ·
[figuras](../user/images/figures.json) · [cierre y resultados CU9](pre_g9b_cu9_closeout_record.md).

## 5. Registro consolidado de limitaciones y deudas

Se conservan los identificadores originales. Las filas con una descripción en lugar de
un identificador remiten al apartado normativo o al caso exacto. «Mantenimiento» indica
la capa responsable propuesta cuando el registro de origen carece de fase asignada.
Esta asignación documental carece de efecto ejecutivo.

### Evaluación, certificación e identidad

| ID o caso | Procedencia y destino | Disposición vigente |
|---|---|---|
| `L-CU1-TRANSLATION-RESOLUTION` | [CU1 §6](pre_g9b_cu1_closeout_record.md#6-limitations-and-debts), mantenimiento métrico futuro | **L**, abierta y no bloqueante. Para una curva unitaria trasladada aproximadamente 10⁶, la clase 3 puede fallar por resolución binary64 y reparto local del error. La clase 1 queda fuera de ese caso. Mantener fallo explícito y objetivo de invariancia sin tolerancia dependiente de la posición. |
| `L-CU1-CLASS2-REACH` | CU1 §6 y [CU2 §8](pre_g9b_cu2_final_review_package.md#8-limitaciones-aceptadas-y-trabajo-futuro), paquete métrico posterior | **L**. Cota de encerramiento rigurosa pero coste computacional alto para giro total apreciable y tolerancia estrecha. CU2 completó su alcance sin ampliarla. |
| `L-CU1-AFFINE-FORMS` | CU1 §6 y CU2 §8, paquete métrico posterior | **L**. El proveedor certifica formas afines directas. Otras formas pueden permanecer en la clase 3. CU2 completó su alcance sin ampliar el certificado. |
| `L-CU1-REVERSAL-DENSITY` | CU1 §6, mantenimiento métrico si se caracteriza otro caso | **L**. La partición mínima puede omitir dos inversiones de dirección consecutivas entre muestras. El estimador conserva la hipótesis explícita. |
| `L-CU1-IMPROPER-EVALUATOR` | CU1 §6, paquete métrico posterior | **L**. Componentes impropias sin derivada conservan `LIMIT_NOT_ESTABLISHED`. |
| `L-CU1-E2-LENGTH` | CU1 §6 → CU2 §8 | **V** cuestión topológica respondida por el `ORIGIN_WRAP` certificado y el arco abierto de B10.2. **L** para la clase métrica de la longitud de `e_2`, que conserva la garantía de su estimación. |
| `TD-CU1-W1` y cortes computacionales de `e_2` | CU2 §8, paquete métrico posterior | **L**. `w_1` puede devolver `PARAMETER_RESOLUTION_REACHED` en los tres conductores caracterizados. `e_2` declara sus cortes computacionales. |
| H-d | [Plan consolidado §6](../architecture/pre_g9b_cu_corrective_usability_plan.md#6-compromisos-de-cierre-y-elementos-diferibles), planificación métrica | **D** perfil de esfuerzo de documento, fuera de la corrección aceptada de CU1. |
| `L-CU2-SQRT-END-LENGTH` | [CU2 §6](pre_g9b_cu2_closeout_record.md#6-limitations-and-future-work), C-10, investigación métrica | **L**. Extremos de raíz cuadrada: `NOT_ESTABLISHED` con `WORK_BUDGET_EXHAUSTED`. Estimador por cambio de variable como trabajo futuro sin autorización. |
| `L-CU2-PERIODIC-CANONICALIZATION` (`F-CU2-17`, `O-CU2A-1`) | CU2 §6, C-11/B-2, mantenimiento métrico | **L**. Deriva de ulp junto a la costura. Se conservan las direcciones persistidas y los extremos incluidos se evalúan en su propio parámetro. |
| `L-CU2-CLOSED-PERIODIC-SEAM` | CU2 §6, C-12, mantenimiento métrico | **L**. Pertenencia heredada cerca de la costura cerrada, ventana `[π − 10⁻¹², π)` del caso caracterizado. |
| `L-CU2-INDEPENDENT-EXPRESSION-STATE` | CU2 §6, futura versión de huella | **L**. Fuera de clase v3, cambio programático de un ulp en un literal independiente sin texto de origen puede quedar indistinguible. Los literales escritos por el usuario conservan su texto. |
| `F-CU2-1` / `O-CU2A-6`, riesgo residual `H-CU2A-8` | CU2 §6 y expediente CU2 §8, certificación y evaluación | **L**. Exceso del cálculo heredado más allá de frontera exacta. Junto al inicio de rayo o segmento, signo redondeado no reproducible da `SELECTION_NOT_REPRODUCED` cuando la existencia sí está certificada. La disponibilidad de coordenadas y la existencia tienen estados propios. |
| R7-E y residuos de R7 | [Registro R7 §6](pre_g9b_cu2_r7_correction_record.md#6-limitaciones-residuales) y CU2 §6, certificación | **L**. La banda lineal–circular tiene cota a priori. La fusión conservadora de celdas de una causa puede unir bandas separadas menos de `0,0245 rad`. Anchuras experimentales `10⁻¹⁰–10⁻⁹ rad` no constituyen una cota matemática. |
| `O-CU2A-7`, `O-CU2A-8`, `O-CU2A-9` | CU2 §6 y §8, futura certificación/consulta | **F** para CU10: identidad de selección en certificados compuestos, deduplicación de giros junto a la costura y dos eventos métricos coincidentes en el mismo corte. |
| Clase v3, V-CON-6, C09, C51 y C70 | CU2 §8 y [T13 §10](pre_g9b_cu2_implementation_candidate_report.md), certificación futura | **L** con exclusiones o límites de clase y fallos cerrados según sus filas. La admisibilidad local y la completitud global se conservan como propiedades independientes. |
| C22, C23 y C40 | CU2 §8 y [plan §2.8](../architecture/pre_g9b_cu_corrective_usability_plan.md#28-aprobación-del-diseño-e-implementación-de-cu3-2026-10-24), futura materialización | **F**. Se excluyeron de la autorización de CU3 la materialización de raíces en componentes puntuales y el seguimiento transversal de raíces de estas filas. |
| `OBS-CU0-CU3-TANGENT-EXISTENCE` | [CU0 §6](pre_g9b_cu0_closeout_record.md#6-observations-registered-for-later-packages) → ADR 0039/0040 y CU3 | **V** precisión adoptada: punto crítico, residuo pequeño, contacto estructural y raíz de existencia demostrada tienen consecuencias distintas. La formulación histórica F-2 quedó excluida. |
| `DEBT-CU3-EXACT-TANGENCY-GENERALIZATION` | [CU3-B2 §4](pre_g9b_cu3_b2_closeout_record.md#4-alcance-demostrado-y-deuda), planificación científica posterior a CU10 y al trabajo editorial del libro | **D** no bloqueante. Procedencia constructiva y decisión exacta para nuevas familias. B2 materializa certificados admitidos, con producción comprobada en las clases y celdas demostradas. K1/K2, objetivos libres en clase 3, cónicas no circulares y porciones limitadas fuera del certificado quedan pendientes de demostración. |
| H02 a gran traslación, familias del CIC, presupuestos y promociones | [CU3 global §§3–4](pre_g9b_cu3_global_closeout_dossier.md#3-alcance-funcional-demostrado) y [candidato CU3 §§6–7](pre_g9b_cu3_implementation_candidate_report.md#6-degeneraciones-y-límites), mantenimiento científico | **L** para H02 (una raíz puede perderse en el caso trasladado `10⁶` con `r ≤ 20`), conductores de arcos y familias fuera de la clase, cortes y presupuestos. **F** ampliar T5/T7-b, los presupuestos o promover por defecto la autoridad constructiva. La adopción por documento sigue explícita y reversible. |
| `DEBT-CU3B1-LEDGER-V6-HIGHWATER-IDEMPOTENCE` | CU3-B1 → [CU3-B2 K6](pre_g9b_cu3_b2_closeout_record.md#1-hechos-del-cierre) | **V** resuelta. El libro de anclas v6 reabre sin reservas que avancen `nextIncarnation`. Se conserva la frontera de lectura de versiones anteriores. |
| `OBS-CU3B1-DRIVER-SUPPORT-REDEFINITION-BARRIER` | CU3-B1 K5 → posible I3 futuro | **F**. Camino inexistente en el núcleo actual. Se reabre si se autoriza una ruta que cambie el soporte efectivo del conductor. |
| `OBS-CU3B2-SILENT-DRIVER-REDEFINITION-REFUSAL` | CU3-B2 → CU7/M09/I3 | **D** en el perímetro de redefinición excluido de R5. Conservar diagnóstico y atribución hasta un contrato de redefinición aprobado. |
| `OBS-CU3A-SMOKE-01` | [CU3-A §1](pre_g9b_cu3_a_closeout_record.md#1-obs-cu3a-smoke-01), construcción del autor | **V** cerrada por decisión autoral. La degeneración de `K_1` en generatrices de perfil explica la pérdida de la componente cerrada en la construcción original. La corrección por contragiro en planta se verificó sobre copia. Persiste la degeneración excepcional `rbCone=d` en la construcción corregida. |
| `OBS-CU3A-SMOKE-02` | [CU3-A §2](pre_g9b_cu3_a_closeout_record.md#2-obs-cu3a-smoke-02), adopción por documento | **V** explicación aceptada: una copia ya adoptada y una limpia presentan rutas distintas. |
| `OBS-CU3A-XMLEDIT-01` | [CU3-A §5](pre_g9b_cu3_a_closeout_record.md#5-obs-cu3a-xmledit-01--omisión-de-objetos-al-cargar-un-xml-editado-externamente), persistencia y protocolo | **L** abierta y no bloqueante. El `consStep` persistido puede ocultar pasos tras insertar objetos editando XML externamente. La incidencia queda fuera de las mutaciones nativas verificadas. |

### Interfaz, inserción, persistencia y documentación

| ID o caso | Procedencia y destino | Disposición vigente |
|---|---|---|
| `OBS-CU1-ISOA-UNDO-STORE-RACE` | CU1 §6, D-CU1-R6 | **V** prueba de ISO A sincronizada con el almacén asíncrono de Undo. |
| `OBS-CU1-UNDO-ASYNC-STORE-WINDOW` | CU1 §6, propuesta upstream separada si se autoriza | **L** observación heredada. Undo previo a la escritura actúa sobre la historia consolidada. El intervalo medido entonces es informativo. |
| `OBS-CU1-UNDO-TEMP-FILES` | CU1 §6, infraestructura de pruebas | **P** deuda de higiene no bloqueante con disposición en CU10: conservar abierta para mantenimiento focal. Aún existen escenarios de prueba que terminan con `Runtime.halt`. No se atribuye una limpieza general de `GeoGebraUndoInfo*.ggb`. |
| `OBS-CU2-OFF-EDT-TOOLBAR-RACE` y `OBS-DESKTOP-OFF-EDT-TOOLBAR-MAXSIZE-RACE` | [CU2 §2](pre_g9b_cu2_closeout_record.md#2-approved-identities-and-authentication-2026-10-24) y [CU3-B2 §2](pre_g9b_cu3_b2_closeout_record.md#2-incidencia-de-la-barra-de-herramientas-fuera-del-edt), mantenimiento Desktop | **P** conservar ambos identificadores y los rechazos históricos. La segunda firma atribuye la carrera de Swing a invalidación de tamaño desde el EDT durante lectura fuera del EDT. No consta un cierre expreso en CU5/CU6; un pase posterior no acredita su corrección. |
| `DEBT-VERIFIER-PWSH-769-TESTJSON` | [Registro de mantenimiento](pre_g9b_cu3_verifier_pwsh769_record.md), infraestructura | **V** cerrada por `48f0d2ce`, con INFRA_UNIT 22/22 en PowerShell 7.6.9 y 7.6.6 según el plan §2.21. |
| H08 | [CU4, diseño aprobado](pre_g9b_cu4_author_design_approval_record.md), interfaz futura | **D** renombrado con subíndices, por decisión expresa. |
| `OBS-CU4-ANGLE-PREFERENCES-REFRESH` | [CU4](pre_g9b_cu4_closeout_record.md#obs-cu4-angle-preferences-refresh), Desktop, fase por determinar | **D** abierta, no bloqueante y prioritaria cuando se autorice la siguiente actuación. El selector existente puede quedar desfasado tras abrir otro modo angular; el kernel carga el valor del archivo. |
| F3, instantánea Undo con macros | [CU4-R1](pre_g9b_cu4_r1_candidate_report.md) y [CU4 cierre](pre_g9b_cu4_closeout_record.md#límites-y-publicación), inserción/persistencia | **L**. La instantánea de `InterCilConoOblique.cedg` puede carecer de las definiciones de macros. La reconstrucción aislada detecta el desacuerdo antes de mutar. |
| `OBS-CU10-B01-LAYER-PROFILE` | [Campaña CU10-B](pre_g9b_cu10_model_validation.md) y [antecedente C1](../architecture/post_cu4_pre_i3_c1_layer_reconciliation.md), inserción Desktop y serialización interna | **P**. Con el perfil habitual hubo 14 rechazos antes de mutar, nueve con diagnóstico explícito de capa. Los 14 coinciden por identidad y mensaje con casos históricos de CU9. El contrato exige rechazar si la copia de prueba cambia la capa. La interacción exacta entre el catálogo instalado y los escalares sin etiqueta de capa en la copia interna requiere una caracterización propia antes de cambiar persistencia o admisión. |
| P4, B2 y ARC-01 | [Contrato ordinario](../../geocedg/specs/ui/document-insertion.md) y [cierre experto](post_cu4_expert_insertion_closeout_record.md#adopción-y-límites), futuras familias y oráculos | **L**. B2 se circunscribe a su certificado angular. El arco circular directo admitido en la envoltura experta sigue una decisión independiente de ARC-01/P4 ordinario. Macros fuera de clase, contenido activo, 3D, conversión angular experta y remapeo durable participante son **F**. |
| `OBS-CU0-CU6-M07-SELECTION-IDENTITY` | CU0 §6 → [CU6-D y cierre conjunto](pre_g9b_cu5_cu6_closeout_record.md#cu6--interacción-presentación-y-persistencia) | **V** para el contrato adoptado: selección vigente por instancia, documento y operandos; nueva selección cuando caduca. La etiqueta se introduce como texto. |
| `OBS-CU6-M07-GV1-SLIDER-PICK` | [CU6](pre_g9b_cu5_cu6_closeout_record.md#obs-cu6-m07-gv1-slider-pick) → CU8, interfaz futura | **L** abierta, no bloqueante por decisión autoral. Homotecia con captura activa rechaza el deslizador visible en Gráficos 1 y conserva el texto. Álgebra inserta el nombre; ESC desarma primero la captura. CU8 conserva esa línea base y el aislamiento del panel. |
| M08-D | [CU7 conciliación](pre_g9b_cu7_status_reconciliation.md#matriz-de-estado) y [M08 §4](../../geocedg/specs/locus/candidates/cu7-m08-point-placement.md#4-dynamic-correspondence-d), correspondencia dinámica futura | **D**. Exige mapa dinámico reconstruible, persistible y con pérdida de unicidad explícita. La referencia Q inicial de M08-S/R5 es transitoria. |
| M09/I2a+I2b | [M09 y estudio A0](pre_g9b_cu7_integrated_m09_evidence.md), redefinición singleton futura | **D/P**. Bloqueo técnico de la transacción A0: identidad, clausura de los DAG y sus consumidores, vigencia, rollback exacto y persistencia. Restaurar XML con instancias y épocas nuevas no supera la puerta. |
| I2G | [Estudio M09/I2G](pre_g9b_cu7_integrated_m09_evidence.md), redefinición de salida multisalida | **D**. Exige adaptador tipado y demostración de atomicidad, salidas hermanas y consumidores. |
| I3 | [Estudio I3/M08-D](pre_g9b_cu7_integrated_i3_m08d_evidence.md), transición de soporte, conductor y fuente | **D**. Exige cierre de impacto y política explícita para las barreras CU3. |
| F8 · `LOCAL_COMPONENT_FORWARD_ADDRESS_CREATION` | [Resolución R5](pre_g9b_cu7_r5_author_decision_record.md), decisión científica futura | **D**, desactivado. No se amplía una dirección local a partir de inventario incompleto sin su propia prueba de identidad y admisibilidad. |
| U2 | Resolución R5, Desktop | **D**. Simplificación de entradas de Punto; deuda P3 no bloqueante. Se conservan el acceso especializado y las autoridades de las rutas. |
| `DEBT-CU7-GENERAL-LOCUS-V2-POINT-DRAG` y familias no admitidas | [M08-S-R2 y límites](pre_g9b_cu7_i1_m08s_r2_closeout_record.md#límites-conservados), [cierre reducido R5](pre_g9b_cu7_r5_reduced_closeout_proposal.md#decisiones-y-deudas-exactas) | **F** en el cierre. Reconstrucción, movilidad y creación generales, restricción configurable de ramas/componentes, panel reutilizable, familias y hosts no comprobados requieren autorización. Límites de presupuesto, lectores/DPI y ruta classpath con `--silent` se conservan como **L** del perímetro probado. |
| CU8 v1, lectores anteriores | [Contrato v1](../../geocedg/specs/ui/control-layout-v1.md), compatibilidad documental | **L**. Un lector anterior puede perder layout, desbloqueo de ciertas casillas o «Nombre y valor» de un deslizador definido al guardar. El lector nuevo avisa. Dentro de las rutas probadas se preservan geometría, valores, identidad y DAG. |
| CU8 v1, inserción y alcance | [Cierre CU8](pre_g9b_cu8_i_closeout_record.md#matriz-de-capacidades-y-límites), inserción Desktop futura | **L**. Fuentes con paneles, incluso vacíos, se rechazan por ambas vías. F3/P4/B2 admite un receptor con paneles solo si certifica pertenencia y recuperación, incluido Redo pendiente. La envoltura experta pasiva lo rechaza. Transferencia de panel, anidamiento y Gráficos 2 son **F**. |
| `DOC-CU3-CU9-STRUCTURAL-POINTS` y ayuda contextual | [CU3 global](pre_g9b_cu3_global_closeout_dossier.md#2-matriz-de-requisitos-y-evidencias) → CU9 y mantenimiento de sintaxis | **V** cobertura en ambas guías y matriz. **P** para los recursos de ayuda de sintaxis `Point.Syntax` / `Intersect.Syntax`: el cotejo EN/ES no encontró formas con conductor en esas claves. Sigue como observación de ayuda interna, separada de B11/B12/H07. |
| CU9, destinos internos y capturas | [Traspaso CU9](pre_g9b_cu9_closeout_record.md#traspaso-a-cu10), mantenimiento editorial | **L**. Los identificadores internos se resuelven en el visor Desktop. Las capturas reducidas pierden legibilidad en columnas estrechas. Figuras y fuentes tienen trazabilidad en §4. |

### Verificación integrada, distribución y líneas futuras

| ID o compromiso | Procedencia y destino | Disposición en el expediente |
|---|---|---|
| `CU10-GUIDE-INTEGRATION-CHECK` | D-CU3G-2 y traspaso CU9 → CU10-G/C/H | **P** hasta atribuir integración y empaquetado de la guía vigente a la identidad exacta de CU10. §4 contiene la comprobación de fuentes y la evidencia histórica que la precede. |
| Barridos de autor, FINAL, smoke y revisión | Plan §§4, 6 y 7 y mandato CU10 → informe principal | **P** hasta verificación del candidato de CU10. Los casos y diagnósticos históricos forman el punto de comparación. El smoke autoral permanece pendiente de comunicación expresa del autor. |
| E1 · `SCIENTIFIC_METHODS_EDITORIAL_HANDOFF` | [Plan científico §§3–7](../architecture/pre_g9b_cu_scientific_methods_editorial_handoff_plan.md), CU10-D y futura actuación editorial autorizada | Entregable obligatorio para el cierre. Revisión y adopción reservadas al autor. La reactivación editorial y los cambios en el libro quedan **F** en CU10. |
| INTERNAL, NC y COMMERCIAL | [Contrato Windows](../../geocedg/specs/packaging/windows-packaging.md), [D1](pre_g9b_d1_closeout_report.md), [matriz legal](../licensing/component-matrix.md), CU10-C | INTERNAL es evaluación interna y su circulación queda restringida. NC conserva la composición auditada D1 y las obligaciones de su aviso y acceso al código. Sus builds de CU10 se acreditan en el informe principal. COMMERCIAL **F**, fail-closed, pendiente de términos externos y nueva decisión. La trazabilidad histórica 39/39 JAR y 46/46 fuentes corresponde a D1. |
| Runtime, instaladores, asociación y portabilidad Windows | Contrato Windows §§2–6, CU10-C | **P** hasta evidencia de app-image, ZIP, MSI y EXE, versión, runtime incluido y pruebas de instalación controlada. El instalador tiene la asociación `.cedg`; `.ggb` conserva su ruta de interoperabilidad. Los hashes identifican cada ejecución. |
| Versión, publicación, etiqueta y release | [AGENTS.md §8.1](../../AGENTS.md#81-geocedg-publication-policy), contrato Windows y roadmap | **P** decisiones independientes de versión, aceptación integrada, cierre autoral, promoción exacta a `main`, etiqueta anotada nueva e inmutable y eventual distribución. La autorización técnica de CU10 cubre evaluación y generación de paquetes de prueba. |
| G9B, G9C, G9U2 y G10 | [Roadmap](../roadmap/geocedg_roadmap.md#g9b---proyecciones-canónicas-de-primitivas) | **F**. G9B/G9C conservan diseño y sus puertas. G9U2 depende de la aprobación global G9. G10P es planificación aprobada; las fases productivas G10 dependen de la puerta global y de autorización propia. |
| G11, G12 y G15 y deudas anteriores al minitrack | [Conciliación PRE-G9B-R7](pre_g9b_r7_debt_reconciliation.md) y [roadmap](../roadmap/geocedg_roadmap.md) | **F** sus ampliaciones. Siguen los estados y propietarios aprobados, incluida la deuda `TD-R3-CONSTRUCTION-PROTOCOL-MINIMAL-REORDER` abierta y diferida. G11/G12/G15 conservan alcance futuro revisado. |

**Capa y validación.** Conciliación documental de alcance y evidencia. El delta de
este expediente conserva guías, geometría, formatos y configuración de verificación.
`GUIDE_IMPACT = NONE` por ser una auditoría de las guías existentes.
`VERIFICATION_INFRASTRUCTURE_IMPACT = NONE`. `BOOTSTRAP IMPACT — NO CHANGE REQUIRED`
porque el expediente conserva las herramientas y sus prerrequisitos. Las verificaciones
focales propias son el cotejo de identidades, la comprobación de fuentes y los enlaces
de este expediente. El perfil FINAL pertenece al candidato integrado de CU10.
