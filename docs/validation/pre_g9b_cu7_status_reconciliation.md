# PRE-G9B-CU — R0 · Conciliación de CU7

**CU7 — RECONCILED.** El autor aprobó el diseño CU7-D el 2026-10-29,
M08-S-R2 el 2026-11-02 y el cierre reducido R5 el 2026-11-05.
El último cierre alcanza las capacidades de sus tablas positivas. El diseño
prospectivo y las deudas excluidas conservan su estado.

## Identidades y alcance del cotejo

`main` en GitHub, `main` local y `origin/main` coincidieron en
`b842165733261126e0f742586834e2bfc612b6f1`, árbol
`607af3a916025d8a1bbd5cc8f4a21c9a95d7fd66`. El historial publicado contiene
los tres cierres. La resolución R5 se incorporó en
`1c3893f539d926c16da837a42d439a1cf8bea34f`, árbol
`1c0f6e9187663b586acfc7c425053d7be0349c21`, descendiente directo de la
propuesta auditada `91566d0daff274ed85961ac41c593481c02cf21e` y
descendiente del candidato técnico `13ee3b8746d4d874824eb44ca1d05a3e57b494c2`,
árbol `a4fead2412bbb8859dd167e3c22b82c54e584633`. El baseline de CU4
registrado el 2026-11-07 confirma que la resolución ya estaba en
`main`. [Cierre de CU4](pre_g9b_cu4_closeout_record.md)

La tabla coteja expedientes versionados con el código y las selecciones del
baseline. `APPROVED` indica decisión expresa dentro de su perímetro.
`IMPLEMENTED` identifica una capacidad presente en ese perímetro.
`PARTIAL` describe la cobertura global. `BLOCKED` identifica una
precondición técnica demostrada. Los estados pueden coexistir para una fila.

## Matriz de estado

| Capacidad | Estado | Fuente versionada y constatación | Dependencia o límite |
|---|---|---|---|
| CU7-D, diseño de M08 y M09 | APPROVED | [Cierre de diseño](pre_g9b_cu7_d_closeout_record.md), candidato `7e7a90e4e39db04ea49ee9ab1acf69ea34c81080`, árbol `25e0e2f0a78670c31be39766021d3f7561bc3bf6`. [ADR 0041](../adr/0041-locus-v2-point-placement-and-point-redefinition.md). | Hereda los contratos de identidad y dependencia de CU3. El diseño prospectivo condiciona cada implementación a su puerta. |
| M08-S-R2, referencia inicial L→Q | APPROVED / IMPLEMENTED | [Cierre canónico](pre_g9b_cu7_i1_m08s_r2_closeout_record.md), candidato `86438edabaed06a6509c1be37cf06278941e0e46`, árbol `96e90172201440739bfe8afb503059379c70ff70`. `Cu7M08SReferenceToolTest` y `Cu7M08SDesktopEventsTest`. | Autoridades R1/R6 y R2/H1 separadas. Panel contextual y política world. Q inicia la selección, sin añadir un padre ficticio. |
| R5, cierre reducido | APPROVED / IMPLEMENTED | [Resolución](pre_g9b_cu7_r5_author_decision_record.md) y [matriz positiva](pre_g9b_cu7_r5_reduced_closeout_proposal.md#capacidades-propuestas-para-el-cierre-reducido). | Incluye exclusivamente la interacción, persistencia y recuperación atestadas por las tablas del expediente. |
| R5, clic y Q sobre Locus V2 | IMPLEMENTED | `LocusPointCreation2D.Route` separa R1/R6, R2/H1 y el permiso individual forward. `R5ForwardCreationRoutingTest` comprueba actualidad de revisión y ausencia de un padre Q nuevo. | Selección por dirección y permiso vigente. La ruta individual v3 conserva inventario `COMPLETE`. Los presupuestos y rechazos se mantienen. |
| R5, arrastre semántico y similitudes tipadas | IMPLEMENTED | `R5SemanticPointPointerDesktopTest`, `R4PointContinuationChainTest` y [diseño R5 de arrastre](../architecture/pre_g9b_cu7_r5_pointer_drag_design.md). | Cartas certificadas y presupuesto acumulado. Las similitudes requieren invertibilidad y correspondencia. Las rutas SplineV2 conservan su dominio. |
| R5, Q nativa y clic nativo ordinario | IMPLEMENTED | `GeoCeDGNativePointPlacement` y [evidencia R5](pre_g9b_cu7_r5_validation_matrix.md). Q nativa cubre recta, rayo, segmento, cónica y arco 2D exactos dentro de las guardas. | La herramienta excluye ejes, 3D, figuras de cotas con propietario semántico y otras familias fuera de tabla. La exclusión Q de ejes pertenece a Punto y es independiente del uso orientado de ejes en Paralela/Perpendicular. |
| R5, bloqueo, DAG, persistencia, Undo/Redo y recuperación | IMPLEMENTED | [Auditoría independiente](pre_g9b_cu7_r5_independent_audit.md), `R3NativeStateRecoveryTest` y `R5SemanticPointPointerDesktopTest`. | Las tablas FILE, MEMORY y Undo son distintas. Bloquear la edición interactiva conserva la pertenencia y dependencia. Las garantías se circunscriben a los hosts y rutas comprobados. |
| D1, compatibilidad XML | APPROVED / IMPLEMENTED | [Enmienda vigente de unidades §8.3](../../geocedg/specs/units/unit-system.md#83-omission-rule-and-legacy-byte-identity), `GeoPoint`, `ConsElementXMLHandler`, `R3NativeStateGrammarTest` y `R3NativeFamilyCompatibilityTest`. | Solo `conicPointState` v1 reconocido y validado en el perímetro adoptado. Classic estricto, políticas de macros y límites de lectores se conservan. `nativeRollbackBinding` tiene su propia ruta privada. |
| CU7 global | PARTIAL | [Resolución R5](pre_g9b_cu7_r5_author_decision_record.md) y [cierre M08-S-R2](pre_g9b_cu7_i1_m08s_r2_closeout_record.md). | El conjunto de todas las familias, garantías y procedimientos del diseño queda fuera de la aprobación reducida. |
| U2, simplificación de la herramienta Punto | DEFERRED | [Resolución R5](pre_g9b_cu7_r5_author_decision_record.md). | Deuda organizativa no bloqueante. Conserva los accesos y autoridades de R5. |
| F8, `LOCAL_COMPONENT_FORWARD_ADDRESS_CREATION` | DEFERRED | [Resolución R5](pre_g9b_cu7_r5_author_decision_record.md) y [propuesta F8](../architecture/pre_g9b_cu7_r3_partial_forward_permission_proposal.md). La enumeración de rutas del baseline carece del permiso F8. | Sigue desactivado. Requiere decisión sobre una dirección local con identidad suficiente cuando no se haya establecido la completitud. |
| M08-D, correspondencia dinámica | DEFERRED | [Contrato M08 §4](../../geocedg/specs/locus/candidates/cu7-m08-point-placement.md#4-dynamic-correspondence-d) y [caracterización](pre_g9b_cu7_integrated_i3_m08d_evidence.md). | Exige mapa dinámico propio, reconstruible y persistible, con dominio y pérdida de unicidad explícitos. M08-S y el Q de R5 solo inicializan una selección. |
| M09/I2a+I2b, redefinición singleton | DEFERRED / BLOCKED | [Contrato M09](../../geocedg/specs/locus/candidates/cu7-m09-point-redefinition.md) y [evidencia de M09](pre_g9b_cu7_integrated_m09_evidence.md). La rama de estudio A0 llega a `249934d09cb313e39f36b0dd79d288b7c0b800d7` fuera de `main`. | Requiere atomicidad demostrada, clausura de ambos DAG y de consumidores, identidad, vigencia y persistencia. La recuperación XML que recompone instancias y cambia épocas no satisface el estado exacto A0. |
| I2G, redefinición de salida de productor multisalida | DEFERRED | [Evidencia de M09 e I2G](pre_g9b_cu7_integrated_m09_evidence.md). | Adaptador tipado y demostración de hermanos, consumidores y publicación atómica. Una hipotética solución singleton carece de esa prueba. |
| I3, transición coordinada de soporte, conductor y fuente | DEFERRED | [Evidencia I3/M08-D](pre_g9b_cu7_integrated_i3_m08d_evidence.md). | Requiere clausura prospectiva del impacto y una política de las barreras de CU3. El cambio de soporte/conductor que reabriría K5 permanece excluido. |
| Reconstrucción general, arrastre/creación universales y familias restantes | FUTURE | [Estudio integrado](../architecture/pre_g9b_cu7_integrated_reassessment.md), [resolución](pre_g9b_cu7_r5_author_decision_record.md) y `DEBT-CU7-GENERAL-LOCUS-V2-POINT-DRAG`. | Cada familia exige adaptadores, certificados y pruebas de sus degeneraciones y garantías. El cierre R5 acepta trabajo acotado y portabilidad limitada. |
| CU9, cobertura de guía | FUTURE | [Plan consolidado §§4–6](../architecture/pre_g9b_cu_corrective_usability_plan.md#cu9--guía-de-usuario) y [cierre global CU3](pre_g9b_cu3_global_closeout_record.md). | Documenta las tablas positivas y exclusiones efectivas de CU7. Mantiene la cobertura de `Point(L, D)` e `Intersect(L, r, D)` de CU3. |
| CU10, cierre del minitrack | FUTURE | [Plan consolidado](../architecture/pre_g9b_cu_corrective_usability_plan.md#cu10--cierre-integrado), cierre global CU3 y resolución R5. | Conserva FINAL, integración y empaquetado de la guía, smoke autoral, expediente E1 y disposición explícita de deudas. La generalización exacta de tangencias sigue diferida según su cierre. |

Los límites de lectores antiguos, portabilidad y familias de R5 permanecen
registrados. El expediente histórico conserva el smoke `PASS — AUTHOR REPORTED`
y el recibo FINAL `aa8d0c33de7dd092b77f1d2ac5a9a8477ef910cdaa1e0d362fd4b230ce310222`
de `13ee3b874`. Las once omisiones históricas y el diagnóstico Revision3 por
fixture ausente conservan su clasificación. El recibo pertenece al SHA
técnico, y este cotejo documental no reejecuta su aceptación.

## Discrepancias documentales y dependencias

- El roadmap combinaba una fila R5 con integración todavía condicionada,
  un resumen CU7-D pendiente y una próxima puerta I2 histórica. Las tres
  referencias de estado vigente se alinean con la aprobación e integración
  verificadas y con el diferimiento actual de I2.
- El plan consolidado retenía la entrega inicial de CU7-D como estado actual.
  Se actualizan el resumen y el carril de secuencia al cierre reducido.
  La tabla de compromisos y las secciones fechadas conservan su función
  histórica. El plan candidato de 2026-10-19 conserva su condición de
  documento histórico y sus propuestas.
- Los registros CU3, CU4, inserción experta, CU7-D, M08-S-R2 y R5 y sus
  contratos se conservan. Las decisiones sobre F8, U2, M08-D, M09 y las
  generalizaciones mantienen sus límites. Las diferencias de alcance
  entre el diseño prospectivo y la entrega reducida se explican por
  sucesión de decisiones. No se identificó una contradicción normativa
  material pendiente de resolución para R0.
- CU5 y CU6 pueden avanzar por su propia puerta. El cierre reducido
  proporciona la base factual de CU7 para CU9 y CU10. Toda futura extensión
  de ese alcance exige decisión y evidencia propias.

## Worktrees preservados

| Rama y carpeta | Commit y árbol | Observación |
|---|---|---|
| `codex/pre-g9b-cu7-general-point-creation`, `cu7-general-creation` | `eea7f57b8a7e8be9f5fe9356a9f7f989dd7fdf17` / `d62179e72dd2bb37226cba5956198809467cb7c1` | 84 commits detrás del baseline y cero exclusivos confirmados en Git. El árbol de trabajo contiene tres fuentes Desktop modificadas y fuentes, documentación y fixtures sin seguimiento. |
| `research/pre-g9b-cu7-integrated-reassessment`, `cu7-integrated-study` | `7bc5d9f80b6a7217d658b056ff6142fc668f72f3` / `817e3d384fa09d44ea40729bb8ebf420689331d9` | Árbol de trabajo limpio. 84 commits detrás y un commit documental exclusivo. |

Se inspeccionaron con `git --no-optional-locks status` y consultas de historial.
Las dos ramas y sus worktrees se preservan.

## Validación del delta

`VERIFICATION_CLASS=DOCUMENTATION_STATUS_ONLY` y
`VERIFICATION_INFRASTRUCTURE_IMPACT=NONE`. `GUIDE_IMPACT=NONE` porque el
delta actualiza planificación y trazabilidad. `BOOTSTRAP IMPACT — NO CHANGE REQUIRED`
porque se conservan ejecutables, requisitos y procedimientos de instalación.
La comprobación pertinente es STATIC sobre el commit de R0, junto con
`git diff --check`, comprobación de enlaces y de ascendencia. Los informes
del verificador y sus hashes se conservan fuera del commit para vincularlos
al SHA congelado.
