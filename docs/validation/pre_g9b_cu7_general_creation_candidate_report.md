# PRE-G9B-CU7 — Creación general por clic/Q y candidato de cierre reducido

Entrega: 2026-11-03, Europe/Madrid. Dirección autoral: 2026-11-02. Estado: **CANDIDATO PARA REVISIÓN AUTORAL; BARRERA NORMATIVA B IDENTIFICADA**. `selfApproved=false`. No es aprobación de CU7, adopción de la enmienda propuesta o autorización de publicación.

## 1. Dictamen y pertenencia del requisito a CU7

La creación de un punto sobre Locus V2 desde una posición próxima pertenece a M08/CU7. El clic es un origen de petición geométrica previsto por R6 para su futuro consumidor gráfico; R6 no implementó por sí mismo toda esa interfaz. Se implementa en el candidato una extensión productiva acotada bajo las garantías vigentes: **A, dar al clic acceso al mismo protocolo compartido R1/R6→R2/H1 que ya usa la referencia Q**. No cambia la admisibilidad, el significado del forward ni los estados/garantías existentes.

La generalización adicional **B, elegir una dirección individual fuera de las admisiones seguras R6 y sin el certificado C1 H1**, requiere criterio normativo nuevo. Su implementación se detiene. La dirección forward puede ser válida sin C1, mínimo o inyectividad; ello no deroga la guarda explícita aprobada de H1. El [texto normativo candidato](../architecture/pre_g9b_cu7_general_creation_normative_candidate.md) ofrece una decisión cerrada, con los grados de proximidad separados, para revisión autoral posterior.

El requisito original queda documentado en `docs/architecture/pre_g9b_cu_corrective_usability_plan_candidate.md:165` y `:625`–`:644` (M08 a partir de un punto próximo, asignado a CU7), y en su sucesor `pre_g9b_cu_corrective_usability_plan.md:567`. El origen R6 del clic está en ADR0019 Context/decisiones 1–4, `g9u0_r6_semantic_locus_point_interaction_support.md:50`–`:81` y `locus-v2-point-interaction.md:10`–`:20`. El contrato M08 `:29`–`:44` contempla q world, que puede suministrar Q, sin convertirlo en identidad.

Esta entrega sucede la recomendación anterior de cierre reducido exclusivamente M08-S-R2 del [estudio integrado](../architecture/pre_g9b_cu7_integrated_reassessment.md), recuperado de `7bc5d9f80b6a7217d658b056ff6142fc668f72f3`. La nueva decisión autoral añade la creación como único objetivo funcional candidato y difiere M08-D. No se repite la investigación M09 ni se reabren incrementos aprobados.

## 2. Base, clasificación y separación de operaciones

| Identidad / límite | Valor |
| --- | --- |
| Base productiva requerida | `origin/main` = `eea7f57b8a7e8be9f5fe9356a9f7f989dd7fdf17` |
| Árbol base | `d62179e72dd2bb37226cba5956198809467cb7c1` |
| Rama candidata | `codex/pre-g9b-cu7-creation-candidate` |
| Worktree candidato | `C:/DesarrolloyDatos/Areas/ProyectosNoFinanciados/CeDG/GeoCeDG/artifacts/cu7-creation-worktree` |
| M08-S-R2 aprobado | `86438edabaed06a6509c1be37cf06278941e0e46`, árbol `96e90172201440739bfe8afb503059379c70ff70` |
| Identidad de la entrega | Commit/árbol y hashes en el manifiesto externo de entrega; se fijan al congelar, sin auto-referencia dentro de su propio commit |

Clasificación: caracterización/diseño, implementación focal condicionada y validación. Capa productiva A: servicio kernel compartido de autoría y consumidor Desktop. Fuentes de verdad: código/contratos de la base, ADR0019/0041 con enmienda 2026-11-02, M08, interacción/superficie pública, existencia CU2 y contratos R6/R1/R2/H1. Fuentes derivadas: este informe, anexos, pruebas y logs. `AGENTS.md` se mantiene; `.github/copilot-instructions.md` no existe en la base.

| Operación | Definición y dependencia | Estado en esta actuación |
| --- | --- | --- |
| Forward explícito | `P=F[b,c](u0)`, con dirección/padres semánticos existentes | Reutilizado; no es consulta inversa nueva |
| Creación desde clic o Q | Descubrir/evidenciar, elegir y confirmar una dirección actual; publicar su forward | Único objetivo funcional adicional A/B |
| Arrastre posterior | Editar la dirección de P mediante la continuación admitida por R6 | Capacidad histórica conservada; arrastre general fuera de alcance |

Q es transitorio como referencia de creación; P no lo sigue por una dependencia nueva después del commit. Si Q ya es antecesor legítimo de L, esa dependencia transitiva continúa. L sí propaga cambios a P y sus consumidores. No se convierte `GeoLocusV2` en `Path`; los hits/render solo seleccionan fuente y suministran la petición world.

## 3. Diagnóstico exacto de las limitaciones restantes

Los [anexos kernel](pre_g9b_cu7_general_creation_kernel_diagnosis.md) y [Desktop](pre_g9b_cu7_general_creation_desktop_evidence.md) documentan las lecturas y los casos históricos. Hay tres obstáculos independientes:

1. **Existencia y componentes:** dominio finito y evaluación finita no prueban una componente continua certificada ni su lineage. Una fórmula general fuera de la clase certificable puede no tener ninguna componente admisible; quitar C1 no la crea. La existencia matemática, la disponibilidad numérica y el render son hechos distintos. CU2 §15.5–§15.8 continúa intacto.
2. **Descubrimiento:** R6 busca candidatos bajo su contrato de evidencia local. Su scan/refinamiento no es un inventario completo de todos los argumentos válidos ni de mínimos estrechos no muestreados. Ninguna oferta descubierta no equivale a ausencia de direcciones. Las barreras, gaps, no finitos y presupuestos siguen siendo causas tipadas.
3. **Admisión individual:** un único candidato general con cobertura no completa mantiene `UNRESOLVED_NUMERICAL_SEARCH / GLOBAL_UNIQUENESS_NOT_ESTABLISHED`. La ruta independiente H1 puede ofrecerlo solo con su modelo/regularidad/proximidad/fidelidad vigentes. Tener un binder forward menos restrictivo o un residual pequeño no otorga ese permiso.

**No todo evaluator sin modelo intervalar C1 está bloqueado hoy.** R6 ya puede admitir MULTIPLE de direcciones localmente válidas bajo sus garantías numéricas propias, con selección explícita; no se le aplica H1 retroactivamente. La barrera nueva se refiere a direcciones fuera de esos resultados admisibles que tampoco cumplen H1.

Antes del cambio A, el clic de Punto/Punto en objeto solo recorría R6, mientras la herramienta L→Q componía R6 y H1. Ese desacoplamiento de consumidores impedía que un clic aprovechase una prueba H1 que Q ya podía usar. La fachada compartida resuelve esa diferencia de autoría, conservando los límites del evaluador general. No se presenta como cobertura universal ni como implementación de B.

## 4. Contrato matemático mínimo y nueve propiedades separadas

Sea la dirección completa `a=(idL,provider,b,c,u0,lift,seam)` en una definición actual de L. Una vez que a está elegida y se reconstruye válidamente, `P=F[b,c](u0)` es una construcción forward. Para esa definición no es necesario demostrar `F(u)=q`, un mínimo de distancia o inyectividad local/global. La política de componentes del producto sigue exigiendo la componente continua certificada y su correspondencia estructural.

| Propiedad | Qué debe establecerse | Qué no se infiere de ella |
| --- | --- | --- |
| 1. Parámetro en dominio | Canonicalización del proveedor y pertenencia actual de u0 | Que una muestra finita pruebe existencia/continuidad |
| 2. Rama/componente | Branch key y selector estructural concreto de componente | Identidad por ordinal, etiqueta, coordenadas o render |
| 3. Correspondencia actual | Resolución única del selector por las reglas estructurales existentes | Que containment único cree una correspondencia nueva |
| 4. Forward válido | Binder actual reconstruye la dirección elegida y produce evaluación finita válida | Regularidad, mínimo, inversa o cobertura global |
| 5. Proximidad a q | Grado real de residual/cota world de la ruta que ofrece la dirección | Cercanía ideal certificada si solo existe residual flotante |
| 6. Regularidad local | La prueba que exige la modalidad vigente, con su grado real | Necesidad matemática universal de C1 para definir un punto forward |
| 7. Dirección individual elegida | Un tuple canónico y un handle de consulta actual determinado explícitamente | Que solo exista una dirección válida o próxima |
| 8. Preimagen cartesiana única | Solo si la ruta afirma y demuestra esa propiedad inversa | Que dos direcciones con igual imagen sean la misma dirección |
| 9. Búsqueda completa | Cobertura demostrada del ámbito declarado | Que cero, una o varias ofertas encontradas certifiquen ausencia/unicidad |

Una cúspide puede tener forward definido aunque la derivada se anule; un self-intersection puede tener varias direcciones con la misma imagen. Son hechos matemáticos, no permisos para saltar fallos de existencia o reconstrucción de un evaluador concreto. Una selección humana no convierte en válida una dirección inválida, estructuralmente ambigua, no reconstruible o stale.

En A cada ruta conserva su contrato: R6 mantiene sus tipos/evidencia y H1 exige componente/interior, enclosure puntual fiel, C1/primer jet, world radius y presupuesto. La contabilidad H1 es acumulada; los presupuestos de las rutas históricas no se mezclan ni se anuncia un agregado nuevo.

## 5. Arquitectura reutilizada y cambio focal A

Diseño previo: [frontera de autorización y diseño](../architecture/pre_g9b_cu7_general_creation_design.md). La implementación A reutiliza las siguientes fronteras:

| Frontera | Responsabilidad preservada / nueva utilización |
| --- | --- |
| `LocusPointInteractionResolver2D` | Descubrimiento/estados/evidencia histórica R6 intactos |
| `LocusSnapshotPlacement2D` | Selección/commit históricos UNIQUE o MULTIPLE intactos |
| `M08SnapshotPlacement2D` | Query world o Q, proof H1, single-use, vigencia, presupuesto y publicación preparada intactos |
| `LocusV2PublicOperations` y algoritmo/binder forward | Persistencia de padres/dirección, identidad y recomputación existentes |
| Nueva `LocusPointCreation2D` | Fachada kernel pequeña: compone las dos rutas sin añadir una admisión |
| `GeoCeDGReferencePlacement` | Adaptador Desktop al mismo servicio kernel; no decide admisibilidad |
| `GeoCeDGReferencePointTool` y controlador Desktop | Capturan el clic world o Q, presentan/consumen ofertas y delimitan el gesto |
| `GeoCeDGPointInteraction` | Creación/arrastre históricos R6 conservados dentro de su contrato |

La fachada ofrece `query(source,x,y,policy)` y `queryFromReference(source,Q,policy)`. `Selection` conserva ruta, resultados/capabilities subyacentes, Choice privada, fuente/definición/revisión, registry y Q cuando existe. Rutas: `R1_R6`, `R2_H1`, `REJECTED`. La prioridad es exacta: R1/R6; solo `UNRESOLVED_NUMERICAL_SEARCH / GLOBAL_UNIQUENESS_NOT_ESTABLISHED` abre R2/H1. Otro rechazo, presupuesto agotado, fallo de forward/publicación o cancelación no habilita otra ruta.

Los dos orígenes comparten la lógica kernel y el normal form publicado. El clic copia coordenadas world, sin construir un Q invisible; el render/hit no aporta u ni c. La comparación de equivalencia usa el mismo snapshot q/policy/source revision; radios diferentes de dos gestos no implican resultados iguales. Q añade liveness/revalidación de su valor hasta el commit; un clic retiene la petición world capturada. Choice y Selection pertenecen a su propia consulta y no se transportan entre clic y Q.

El modo de referencia conserva L→Q por defecto y añade el selector efímero **Click position / Posición de clic**. En ese origen, un clic sobre L consulta directamente la misma política world del panel que Q; no pasa por el radio de pantalla de la herramienta Punto ordinaria. Esto permite comparar ambas autorías bajo el mismo q y policy incluso al cambiar zoom. La herramienta Punto ordinaria conserva su gesto R6 previo y, solo ante el motivo permitido, puede abrir una petición world contextual; sus radios iniciales de hit/consulta no constituyen una garantía de paridad universal con Q. Cada consulta conserva su presupuesto real; el gesto ordinario puede repetir R6 y no se presenta como un único presupuesto agregado.

La corrección posterior a la cohorte `f3b9d4d86ce514fa2de87854317c86f3bf2e2cb0` captura q world antes de abrir el selector entre varias fuentes superpuestas. Pan/zoom del mismo view mientras se elige L no vuelve a interpretar las coordenadas de pantalla originales; un cambio de view invalida esa petición. La prueba Desktop añadida comprueba el mismo tuple/ofertas antes y después de pan/zoom y que elegir la fuente solo consulta, sin mutar XML. Es una corrección de la captura de autoría, no un criterio de admisibilidad nuevo.

La confirmación contextual H1 y el normal Undo se reutilizan. La creación directa R6/SplineV2 y su gesto de arrastre admitido se conservan; el gesto nuevo de creación H1 termina al confirmar y no se convierte en arrastre general por inferencia. Escape, cambio de herramienta, edición de política o revisión inválida deben retirar/invalidar la oferta y no crear un punto libre heredado.

No se cambian proof H1, resolver, generador, existencia/componentes, codec, normal forms, autoridad constructiva, transacciones generales, algoritmos multisalida o exportación. El criterio B no se implementa llamando una factory de nivel inferior que eluda los placements.

## 6. Perímetro seguro y ejemplos

| Clase/caso | Perímetro A bajo contratos vigentes | Límite conservado |
| --- | --- | --- |
| Fuentes R6 ya admitidas, SplineV2 y R5 soportados | Creación histórica, selección múltiple y gesto válido existentes | No mayor unicidad o garantía por compartir fachada |
| h de `TestM08-S.cedg` con prueba H1 vigente | Candidato principal para ampliar clic y compararlo con Q | No es un caso sin H1; su uso no demuestra B |
| General con varias direcciones R6 admisibles sin modelo C1 H1 | Elección explícita por R6 | No exigir H1 a una admisión antigua ni inferir completitud global |
| General con una semilla y proof H1 completa | H1 con confirmación explícita, incluso una oferta | Sin mínimo ni unicidad cartesiana/global implícitos |
| Construcción de clase v3 con componente certificada pero sin captura intervalar H1 | Caso negativo/diagnóstico; no se concede permiso H1 por certificado de existencia | Sería objetivo de B, solo tras adoptar/implementar su criterio |
| Fórmula finita como exp/sin fuera de clase y sin componente certificada | Rechazo/no oferta por la causa real de componente/capacidad | No es solo una limitación C1; no se amplía CU2 |
| Gaps, punto-componente, lineage ambiguo, no finitos, fuente indefinida, colapso o proof agotada | Fallo controlado de su ruta, sin publicación | Ni clic, confirmación ni fuente dibujada subsanan la prueba |
| Self-intersections, periodicidad y varias componentes | Solo las direcciones/canonicalizaciones admitidas por sus contratos | No cambio de rama por nearest, no wrap en arco abierto ni clave por lista |

Los ejemplos se validan por sus fuentes reales y el resultado guardado. Una fórmula ideal de radio 3 o una clase v3 presente no demuestra por sí sola que la captura/evaluación/existencia runtime cumplan las precondiciones. Los negativos no se convierten en positivos alterando el contrato para que pase la prueba.

Ejemplo general runtime sin H1: `c=Circle((0,0),1); N=Point(c); v=Vector((0,0),N); Q=N+(2*v); L=LocusV2(Q,N)`. La prueba ejecutada establece captura v3, ausencia de captura v1/v2 y componente continua COMPLETE, pero no le concede permiso H1. La ejecución suplementaria confirma además que `Point(L,"generator.main",0)` está definido en (3,0), manteniendo el rechazo H1. Es una demostración runtime de que forward válido y permiso de autoría son propiedades distintas, no una inferencia a partir del dibujo.

Modelo autoral original recuperado de `artifacts/pre-g9b-cu7-i1-r1/author-input/TestM08-S.cedg`, SHA-256 `4f6e1be3c42a892f63f6791f3b8ca6994443863af5c5ad2039531604e27c898d`. La copia de test `source/desktop/desktop/src/test/resources/org/geocedg/desktop/cu7-general-creation/TestM08-S.cedg` debe conservar exactamente esos bytes. El nativo original no contiene el Q denominado ReferenceR1 de las sondas; para reproducir se crea transitoriamente `ReferenceR1=(0.8976484384297626,1.7854818601338847)`, con radio world `.1`, y se documenta ese paso. No se atribuye ese Q al archivo original ni se guarda sobre él.

## 7. Barrera normativa B y decisión pendiente

La enmienda aceptada exige explícitamente el modelo intervalar C1 y jet no nulo para la vía individual R2/H1 (M08 `:263`–`:269`; ADR0019/0041 y especificaciones sucesoras). El razonamiento forward no autoriza retirarlos unilateralmente. B queda detenida en [enmienda candidata con texto exacto](../architecture/pre_g9b_cu7_general_creation_normative_candidate.md), sin modificar ADR/specs aceptados.

La recomendación normativa preferente es `INDIVIDUAL_FORWARD_ADDRESS_CREATION`, separada de R6/H1, con prueba de identidad/componente/correspondencia/forward, elección explícita y budget/freshness, sin C1, mínimo, unicidad de preimagen o completitud inversa como requisitos universales. La decisión debe admitir expresamente que la proximidad pueda ser `FLOATING_POINT_UNCERTIFIED` cuando solo hay residual numérico. Solo una cota puntual exterior fiel permitiría `CERTIFIED_ERROR_BOUND`. No se declara exactitud, incidencia ideal o STRICT. La configuración capturada no se transfiere a autoridad constructiva por inferencia.

Una alternativa cerrada más conservadora exige siempre enclosure puntual de proximidad, retirando únicamente C1/jet. Sigue excluyendo evaluadores sin esa capacidad y puede necesitar una costura limitada de evidencia nueva; no se obtiene quitando un `if` a H1. La elección entre ambas garantías corresponde al autor. Ninguna está implementada por A.

La tabla inicial candidata se cierra a evaluadores reconstruibles con programa v3 actual, inventario de existencia COMPLETE y componente continua/selector ya establecidos por CU2. No admite nuevos inventarios PARTIAL ni R5 sin captura equivalente; COMPLETE de existencia no es completitud inversa. La nueva norma no añadiría certificados de existencia, nuevos mecanismos de persistencia, reconstrucción de algoritmos, `Path` o arrastre general. Evaluador finito sin componente/selector reconstruible permanece rechazado. No se promocionan singularidades o componentes inciertas por confirmación. La autorización actual exige detener esta parte al identificar el criterio nuevo; no obliga a abandonar la integración A que sí conserva los contratos.

## 8. Pruebas, verificaciones y calidad de la evidencia

Se separan pruebas focales nuevas opt-in, regresión registrada de M08-S-R2, comprobaciones gráficas y STATIC sobre candidato congelado. No se añade una fase `CU7-GENERAL` ficticia ni se modifica registry, runner o infraestructura para obtener cobertura aparente.

| Familia | Casos preparados / oráculo |
| --- | --- |
| Shared opt-in | Paridad clic/Q sobre h; prioridad R6/Spline sin H1; elección múltiple; Q stale; source ABA; cancelación/foreign/replay/thread; agotamiento R6 terminal; independencia Q y propagación L/downstream; normal form/reopen; v3 sin H1; fórmula sin componente |
| Desktop opt-in | Misma dirección desde world/Q; eventos reales del canvas y confirmación; cancelación/modo; source stale; R6/Spline gesture; un Undo/Redo; carga de bytes nativos y persistencia forward |
| Regresión registrada | `PRE-G9B-CU7-I1-M08-S-R2-DESKTOP`: inventario real de su fase, incluidas sus dependencias R6/H1; no se atribuyen a ella los tests opt-in nuevos |
| Gráfica técnica | Producto visible del candidato exacto, copia del modelo y preferencias propias; clic canvas/confirmación/resultado/Undo; evidencia separada de dispatch AWT y de smoke autoral |
| Documental | STATIC canónico congelado; diff de base a candidato; enlaces/formato; hashes de fuente/test/fixture/estado protegido |

Fuentes opt-in: `geocedg/validation/pre-g9b-cu7-general-creation/focused.init.gradle`, `probe-shared/org/geocedg/common/locus/M08PointCreationKernelTest.java` y `probe-desktop/org/geocedg/desktop/Cu7M08SGeneralCreationTest.java`. El init añade estas fuentes solo al invocarse y guarda JUnit en un destino externo explícito. El productor neutral existente `tools/agent/checks/gradle-command-evidence-producer.ps1` conserva el proceso: su propio exit 0 no es aceptación de las pruebas; exigir `completion_state=COMPLETED`, `inner_exit_code=0` y JUnit real de los casos previstos.

Comandos de selección reproducible, con rutas de evidencia nuevas y valores efectivos conservados por el integrador:

```powershell
& tools/agent/checks/gradle-command-evidence-producer.ps1 `
  -EvidencePath <shared>/process.json -GradleTask ':shared:common-jre:test' `
  -AdditionalArguments @('--init-script','geocedg/validation/pre-g9b-cu7-general-creation/focused.init.gradle',
    '--tests','org.geocedg.common.locus.M08PointCreationKernelTest',
    '-Dgeocedg.cu7.creation.junit=<shared-junit>') -OutputDirectory <shared>/process
& tools/agent/checks/gradle-command-evidence-producer.ps1 `
  -EvidencePath <desktop>/process.json -GradleTask ':desktop:desktop:test' `
  -AdditionalArguments @('--init-script','geocedg/validation/pre-g9b-cu7-general-creation/focused.init.gradle',
    '--tests','org.geocedg.desktop.Cu7M08SGeneralCreationTest',
    '-Dgeocedg.cu7.creation.junit=<desktop-junit>') -OutputDirectory <desktop>/process
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-CU7-I1-M08-S-R2-DESKTOP -LogDirectory <regression>
& tools/agent/verify.ps1 -Profile STATIC -LogDirectory <static-frozen>
git diff --check eea7f57b8a7e8be9f5fe9356a9f7f989dd7fdf17 HEAD
```

<!-- INTEGRATOR_EXECUTION_EVIDENCE_START -->
Se distinguen dos cohortes. La primera se congeló en
`f3b9d4d86ce514fa2de87854317c86f3bf2e2cb0`, árbol
`a01428d7c3919a1021d90b68db6ec7afa8490dc2`: sus resultados focales, PHASE de regresión y
STATIC conservan esa identidad. Después se corrigió únicamente la captura world previa
al callback de elección de fuente, se añadió una prueba Desktop y se precisó documentalmente
el perímetro inicial COMPLETE de B. Las doce pruebas Desktop pasan en esa fuente corregida.
El sucesor se congelará con su identidad en el manifiesto externo; no se le atribuye la
PHASE ni el STATIC del commit anterior. La comprobación gráfica y el STATIC final del
sucesor se registrarán externamente, sin anticipar sus resultados aquí.

| Ejecución | Evidencia guardada / identidad | Resultado conocido al congelar | Límites |
| --- | --- | --- | --- |
| Shared focal nuevo, cohorte previa a `f3b9d4d8` | `focus-shared-ascii-02/process.json` y JUnit | Proceso COMPLETED, inner exit 0; 12 tests, 0 fallos/errores/omitidos | Conserva 11 casos ya positivos y confirma fixture v3 corregido sin relajar producción; fuente identificada por hashes |
| Desktop focal nuevo, cohorte previa a `f3b9d4d8` | `focus-desktop-02/process.json` y JUnit | Proceso COMPLETED, inner exit 0; 11 tests, 0 fallos/errores/omitidos | Incluye canvas AWT, paridad q/policy en zoom50/150 y radios0.2/0.0001, native/copia/reopen; no es entrada física |
| Confirmación suplementaria forward v3, cohorte `f3b9d4d8` | `v3-forward-confirmation/process.json` y JUnit | COMPLETED, inner exit 0; 1 test, 0 fallos/errores/omitidos | Comprueba explícitamente Point(L,branch,0) y rechazo de H1; no cambia admisión |
| PHASE M08-S-R2-DESKTOP, regresión de `f3b9d4d8` | `regression-phase/verification-result.json`, `regression-phase.exit.txt`, `regression-cases-summary.json` | COMPLETED, ACCEPTED / COMPLETE, exit 0; 196 ejecuciones, 0 fallos/errores/omitidos | Commit `f3b9d4d86ce514fa2de87854317c86f3bf2e2cb0`; árbol `a01428d7c3919a1021d90b68db6ec7afa8490dc2`; no incluye probes nuevos ni acredita el sucesor corregido |
| STATIC de `f3b9d4d8` | `static/verification-result.json` y `static.exit.txt` | ACCEPTED / COMPLETE, exit 0 | Identidad del commit `f3b9d4d8`; no reatribuido al sucesor; diagnósticos separados |
| Desktop focal después de la corrección del callback | `corrective-desktop/process.json`, logs `corrective-desktop/process/`, JUnit `TEST-org.geocedg.desktop.Cu7M08SGeneralCreationTest.xml` | COMPLETED, inner exit 0; 12 tests, 0 fallos/errores/omitidos | Añade `sourceChooserKeepsTheWorldRequestCapturedBeforeZoomAndPan`; ejecuta también los once casos anteriores. Es evidencia focal de la fuente corregida, no otra PHASE ni entrada física |
| Prueba gráfica técnica del sucesor | Producto visible, preferencias/copia propias y contexto de lanzamiento | Evidencia posterior en `visual-smoke/` y `delivery.json` | No se anticipa éxito ni se confunde con AWT o smoke autoral |
| STATIC final, diff e identidad del sucesor | Informe tipado, diff y manifiesto externos | Resultado posterior en `delivery.json` | Identidad final fijada al congelar; no reemplaza ni extiende retroactivamente las cohortes previas; sin receipt FINAL |

Raíz externa de estas rutas: `C:/DesarrolloyDatos/Areas/ProyectosNoFinanciados/CeDG/GeoCeDG/artifacts/pre-g9b-cu7-general-creation/`. El manifiesto registra commit/árbol, comandos/exit/log, hashes, estado final y resultados efectivamente concluidos. No se usa un nombre de archivo para presumir PASS.

El resumen de PHASE desglosa sus 196 ejecuciones: 5 `final.desktop.g9u1-isolated`, 31
`pre-g9b-cu7-i1-r1.desktop`, 69 `pre-g9b-cu7-i1-r2-desktop.desktop`, 15
`pre-g9b-cu7-i1-r2-kernel-h1.shared` y 76 `pre-g9b-cu7-i1-r2-kernel.shared`, todas con
exit 0 y sin fallos, errores u omitidos. Su plan resuelto tiene 15 nodos y hash
`17a17d10e05d7bc1884b68575382b29d848b8d5a7e6235ab17fc4973ca90f037`.
Estos números describen la selección registrada ejecutada en `f3b9d4d8`; no una fase nueva
de creación general ni una aceptación del cambio posterior de callback.

La ejecución correctiva usó el productor neutral existente con tarea
`:desktop:desktop:test`, filtro `org.geocedg.desktop.Cu7M08SGeneralCreationTest`,
`focused.init.gradle` y destino JUnit `corrective-desktop/junit`, en el worktree ASCII.
`process.json` conserva comando/argumentos efectivos, hashes y `inner_exit_code=0`;
JUnit confirma 12 casos, 0 fallos, 0 errores y 0 omitidos. No se cambian registry, inventario
histórico, proof H1 o semántica de los tests para obtener este resultado.

Intentos conservados: `focus-shared-01` no descubrió tests por selección incorrecta de proyectos de builds incluidos, corregida solo en init opt-in. `focus-shared-02` compiló la clase pero su worker falló al cargarla en el worktree con ruta acentuada; no se parcheó producto para compensarlo. El mismo candidato ejecutó tests en el worktree ASCII. Eso clasifica el síntoma como contexto de ejecución/ruta, sin afirmar que toda ruta Unicode sea incompatible ni cambiar requisitos de bootstrap. `focus-shared-ascii-01` ejecutó 12 casos y falló la premisa del fixture v3 (`outside-class`): la expresión intermedia `W=2*V` no pertenece a la clase capturada; se sustituyó en el test por el patrón inline admitido `Q=N+(2*v)`. Se conservaron todas las assertions de componente/captura/rechazo. El run ASCII posterior ejecutó los 12 correctamente. Ninguno de esos exits fallidos se borra o reclasifica como PASS.
<!-- INTEGRATOR_EXECUTION_EVIDENCE_END -->

Las ejecuciones de M08-S-R2 del candidato aprobado son históricas, no evidencia nueva de A. PlanOnly, lectura de assertions y derivaciones matemáticas tampoco son pruebas ejecutadas. Los intentos fallidos se conservan con su clasificación; un problema de entorno, permisos Windows o runtime no se convierte en parche geométrico. El criterio B no tiene pruebas positivas productivas mientras siga sin aprobación/implementación. No se ejecuta FINAL, packaging, una fase futura o una aceptación autoral por inferencia del nombre del encargo.

## 9. Estrategia integrada actualizada y diferimientos

La siguiente matriz aplica la dirección autoral nueva sin repetir M09. Los contratos prospectivos y las aprobaciones históricas conservan su contenido; se modifica la prioridad/estrategia candidata y el perímetro propuesto de cierre.

| Capacidad | Disposición actual | Causa y capacidad no disponible | Límite/rechazo vigente y dependencia de reapertura |
| --- | --- | --- | --- |
| M08-S-R2 | **Cerrado PASS — AUTHOR APPROVED**, sin reapertura | Ninguna revisión de su cierre por esta ampliación | Mantener su perímetro R1/R2/H1, Q transitorio, forward y feature flag |
| Creación A por clic/Q bajo garantías vigentes | **Única ampliación productiva candidata de esta entrega** | Unifica autorías; no todos los evaluadores generales quedan cubiertos | Regresión más pruebas nuevas y gráfica; candidato exacto y revisión independiente antes de cierre autoral |
| Creación B individual sin C1 fuera de admisiones existentes | **Candidato normativo, implementación detenida** | Falta adoptar un criterio y grado de proximidad propios | Sin permiso por residual/UNRESOLVED; reapertura tras decisión explícita de enmienda y gate de identidad/forward/evidencia |
| M08-D, incluida la afín | **Diferido por decisión autoral del 2026-11-02** | No se crea correspondencia dinámica P(Q,L) ni nuevo mapa persistente | No hay provider D por este trabajo; forward/CU3 existentes dentro de sus contratos. Reapertura solo por nueva prioridad y contrato/codec propios |
| M09/I2a/I2b | **Diferido por precondiciones transaccionales pendientes** | No nueva redefinición pública conservando identidad por M09 | Se conservan modos/rutas actuales; no se prometen todos los nuevos códigos tipados futuros. Reapertura con cierre ordinary+durable, claims, Undo, vigencia y target sin ID demostrados |
| I2G | **Diferido por integridad de productores multisalida** | No separar una salida conservando hermanos/consumidores mediante un adaptador supuesto | Barreras de productor/grupo actuales; no detach por índice/etiqueta. Reapertura con mapping completo y lifecycle/lectores de un productor aprobado |
| I3 | **Diferido por cierre coordinado, identidad y reconstrucción** | No nueva operación N/L/soporte coordinada | Conservar rutas ya admitidas; no ordinary refresh ampliado ni barrera por coordenadas. Reapertura con overlay y correspondencia/barreras efectivas propias |
| Arrastre general Locus V2 | **Deuda específica no bloqueante para crear** | P creado no obtiene continuación arbitraria por cualquier gesto | Solo movimiento R6 admitido; unresolved/ambigüedad no reasocia. Reapertura con contrato de continuación propio; no es dependencia de A |
| I5 | **Revisión y cierre reducido pendientes** | No origina funcionalidad ni borra exclusiones | Requiere ámbito exacto implementado, evidencia, revisión independiente y decisión autoral SHA/árbol |

Se conserva `DEBT-CU7-GENERAL-LOCUS-V2-POINT-DRAG`, la posible restricción configurable de rama/componente, CU10, `CU10-GUIDE-INTEGRATION-CHECK`, E1, `DEBT-CU3-EXACT-TANGENCY-GENERALIZATION` y obligaciones editoriales. No se cambia el libro, su orden autoral, la autoridad constructiva ni su deuda científica para compensar la creación.

## 10. Propuesta precisa de cierre reducido

Perímetro candidato propuesto: **CU7 reducido — M08-S-R2 aprobado + creación por clic/Q bajo las admisiones vigentes R1/R6 y R2/H1, con protocolo compartido de evidencia/selección/commit**. Ese nombre debe acompañar toda declaración de cierre. No significa «creación universal en todo Locus V2», implementación de B, M09 o M08-D.

Para cerrarlo se necesitan: A implementada en el candidato exacto; pruebas nuevas, regresión y gráfica con sus límites; smoke funcional autoral; revisión independiente favorable; y una decisión autoral que adopte expresamente este perímetro y sus exclusiones y apruebe el SHA/árbol correspondiente. No se atribuye esa aprobación al presente informe ni a una ejecución técnica satisfactoria.

Si el autor considera B indispensable para el objetivo funcional de CU7, la barrera normativa debe resolverse primero, seguida de su implementación y validación propias; CU7 global continúa abierto entretanto. Si adopta el perímetro A y excluye explícitamente B con su deuda/criterio de reapertura, puede cerrarse con alcance reducido honesto. La elección de alcance no cambia la aprobación técnica histórica de M08-S-R2.

La enmienda nueva propuesta es una puerta de decisión verificable, no una obligación de construir infraestructura geométrica general. El candidato A permite revisar trabajo concreto sin adelantar B. No se solicita la aprobación repetida de clic/Q o de los diferimientos ya decididos; lo pendiente es el criterio nuevo de admisión y el perímetro de cierre exacto.

## 11. Protocolo de smoke autoral

El smoke del agente y el autoral son evidencias distintas. El siguiente protocolo queda preparado para el autor sobre el candidato exacto; sus filas no se marcan PASS sin ejecución y comunicación autoral.

1. Registrar SHA/árbol, launcher y preferencias aisladas del candidato. Activar `cedg.locus.v2`. Verificar hash del original `TestM08-S.cedg`; abrir una copia en un directorio nuevo y conservar el original intacto. Registrar versión/política y no adoptar autoridad constructiva como paso implícito del ensayo.
2. Comprobar que h es la fuente real y que el archivo original no contiene el Q auxiliar de las sondas. Usar una zona interior de h que produjo proof H1 en la evidencia; fijar radio/policy y conservar el estado previo.
3. En Punto sobre Locus V2, activar **Posición de clic / Click position**, fijar la misma política world que se usará con Q y hacer un clic real sobre h. Confirmar también el fallback del Punto/Punto en objeto ordinario, conservando que su radio inicial histórico es distinto. Si R6 queda en la causa autorizada y H1 ofrece dirección, debe aparecer la confirmación contextual antes de crear P. No debe aparecer un punto libre de fallback ni un GeoPoint Q ficticio.
4. Confirmar una oferta exacta. Comprobar P sobre la dirección semántica elegida, L como padre legítimo y ausencia de Q nuevo como padre. El resultado debe seguir siendo forward, no una afirmación de proyección exacta o mínima distancia.
5. Repetir y cancelar con Escape/cancelación; repetir y cambiar herramienta con oferta pendiente. Comprobar ausencia de P, objetos huérfanos o Undo extra. No se consume ni se reutiliza el handle cancelado para crear después.
6. Preparar otra oferta y modificar L o su configuración antes de confirmar; la oferta debe quedar stale/inválida y no publicar. Si se cambia radio/policy, exigir nueva consulta en lugar de confirmar evidencia vieja.
7. Crear explícitamente `ReferenceR1=(0.8976484384297626,1.7854818601338847)` si se reproduce ese caso. Elegir la herramienta de referencia L→Q, seleccionar h y luego Q, con radio `.1` y la misma política comparativa. Confirmar dirección; no atribuir la existencia de Q al original.
8. Mover Q después de la creación y comprobar que no mueve P por una dependencia nueva. Mover una entrada legítima de L y comprobar que P y un dependiente ordinario de P se recalculan manteniendo la dirección; si una entrada Q ya afecta L, interpretar esa dependencia transitiva correctamente.
9. Crear un caso de dos direcciones admisibles/preimágenes coincidentes documentado por la suite. Ver ambas direcciones y seleccionar una explícitamente; cancelar la otra. No deduplicar por coordenadas ni seleccionar por orden de lista. Probar seam periódico solo en la fuente certificada correspondiente; no wrap en arco abierto ni salto de gap.
10. Verificar que confirmar la creación nueva termina el gesto y no inicia un arrastre general implícito. Comprobar aparte una creación R6/SplineV2 y su arrastre previamente admitido. Para P general, un movimiento posterior solo puede usar la admisibilidad histórica real; una falla no puede elegir otra rama por cercanía.
11. Comprobar un Undo por éxito y Redo que restituye la misma dirección/ID bajo las reglas existentes. Guardar una copia `.cedg`, cerrar y reabrir; verificar padres, dirección y ausencia de q/consulta persistidos. Duplicar un cierre completo y verificar remapeo de IDs sin asociación por etiquetas.
12. Provocar/revertir un estado indefinido o pérdida de componente mediante una entrada controlada del fixture ejecutado. P debe quedar indefinido y recuperar solo cuando su selector/correspondencia retenidos se revaliden; no cambiar dirección por coordenadas. Una recuperación no demostrada no se marca PASS por parecido visual.
13. Cargar el ejemplo general sin H1 de la prueba ejecutada y confirmar su rechazo/no oferta actual. Cargar el caso sin componente y comprobar que no se ofrece aunque pueda evaluarse finitamente en algunas muestras. No habilitar B durante el smoke ni aumentar admisibilidad para pasar el negativo.
14. Registrar capturas/valores, pasos ejecutados/omitidos, resultado y limitaciones. Guardar solo copias; verificar otra vez el hash original. Comunicar el resultado autoral del perímetro A con su SHA exacto, separado de cualquier decisión sobre la enmienda B.

## 12. Revisión independiente, inventario e impactos

Se recomienda revisión independiente del candidato A y de la propuesta B como dos objetos separados. Para A: comprobar prioridad de rutas, ausencia de fallback después de rechazo/commit, dueños/single-use/freshness y presupuestos, paridad q/Q, controlador gráfico sin punto libre ni drag implícito, normal form/codec intactos y la evidencia real. Para B: comprobar la suficiencia matemática, conservar existencia/lineage/autoridad, la separación entre proximidad numérica y certificada, y la ausencia de unicidad/STRICT/mínimos implícitos. Una revisión favorable del texto B no implementa el criterio.

Inspeccionados: AGENTS y prompts canónicos; ADR0019/0041; M08; interacción, superficie pública y existencia; roadmap/plan CU y estudio integrado; diseños y pruebas R6/R1/R2/H1; resolver, placements, public operations, binder y algoritmo/state forward; consumers y controlador Desktop; wrappers/registro y pruebas opt-in; archivo autoral y su procedencia. El detalle por archivo/ancla está en los tres anexos y el diseño. Las referencias a código/pruebas no significan ejecución exhaustiva.

Delta implementado de A: nueva fachada `LocusPointCreation2D`, adaptador `GeoCeDGReferencePlacement`, consumer/controlador del clic, pruebas opt-in/init, fixture nativo por bytes, guías y documentos candidatos. El manifiesto de entrega debe enumerar el diff real guardado; no se atribuye a este informe un cambio no presente en Git. Ningún cambio a criterios R6/H1, existencia, codecs, ADR aceptados, upstream amplio, modelos legacy originales, `CLAUDE.md`, libro, packaging o infraestructura.

`BOOTSTRAP IMPACT — NO CHANGE REQUIRED`: mismos requisitos de estación, runtimes, JDK/Gradle y dependencias; no cambia supuesto consumido por bootstrap. `VERIFICATION_INFRASTRUCTURE_IMPACT=NONE`: registro, runner, schemas e inventarios históricos intactos; pruebas nuevas opt-in aisladas mediante productor existente y log/JUnit propios. `GUIDE_IMPACT=UPDATE`: describir el clic nuevo, confirmación y límites reales; CU10/guía/E1 permanecen fuera del cierre técnico de este candidato.

La entrega debe conservar commit/árbol congelado, comando/exit/log de cada ejecución, hashes de fuentes y fixture, estado final limpio y main/originales intactos. No se promueve a main, publica rama, etiqueta/release ni registra aprobación autoral. El resultado final es candidato A y barrera/decisión B delimitada, con CU7 global pendiente del alcance y aprobación autorales explícitos.
