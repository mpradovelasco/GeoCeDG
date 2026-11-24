# PRE-G9B-MAINT-U2 — Informe del candidato técnico

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = INTEGRATED_PHASE
GUIDE_IMPACT              = UPDATED
```

| Campo | Valor |
|---|---|
| Incremento | `PRE-G9B-MAINT-U2`: simplificación de la herramienta Punto (U2-1 a U2-6) |
| Autorización | Instrucción autoral «GeoCeDG — PRE-G9B-MAINT-U2. Autorización de implementación — Simplificación de la herramienta Punto», 2026-11-23 |
| Base | `e79ace7d1473f13d32d0b660e92a4d1d45ce39bf`, árbol `9c43dfa5548a0844215c0ab9ba808f20e3dd3a84` |
| Prompt | [`pre-g9b-maint-u2-point-tool-unification`](../../.github/prompts/tasks/pre-g9b-maint-u2-point-tool-unification.prompt.md), registrado en el commit de gobernanza `4d351b2bd0ade0bf0d44030d500e8778ed2f4d2a` |
| Planificación | [diseño D](../architecture/pre_g9b_maint_p0_cu7_revised_design.md) §1; [hoja F](pre_g9b_maint_p0_author_decision_sheet.md) §0, D-U2 |
| Guía de smoke | [guía para el autor](pre_g9b_maint_u2_author_smoke_guide.md) |
| `selfApproved` | `false` |

Este artefacto forma parte del commit técnico que describe. No contiene los resultados de la
verificación de ese commit (`STATIC`, `PHASE PRE-G9B-MAINT-U2`, `INTEGRATION`), que se obtienen
después sobre el commit congelado y se entregan aparte. La aprobación autoral, si llega, se
registra en otro documento.

## 1. Resultado

Las entradas interactivas de creación de puntos sobre Locus V2 comparten un único panel. El
núcleo no cambia: todas las rutas siguen pasando por `LocusPointCreation2D` a través de
`GeoCeDGReferencePlacement`, sin cambios en esas clases.

| Id | Cambio | Dónde |
|---|---|---|
| U2-1 | **Punto**, **Punto en objeto** y la herramienta 145 muestran el mismo panel con un selector **Origen:** de dos segmentos, **Clic** y **Referencia Q**, que sustituye los conmutadores «Desde referencia Q» (modos 1 y 501) y «Posición de clic» (modo 145). Los segmentos son complementarios: pulsar el seleccionado elige el otro origen | `GeoCeDGReferencePointTool.buildOriginSelector`, `chooseOrigin`, `showOrigin` |
| U2-2 | La herramienta 145 conserva modo, acción, icono y posición. Abre el mismo panel con **Referencia Q** preseleccionada | `GeoCeDGReferencePointTool.isReferenceOriginSelected` |
| U2-3 | Una línea junto al selector indica el origen del radio y la regla de confirmación de cada origen; la ayuda emergente da el detalle | `GeoCeDGReferencePointTool.showOrigin` |
| U2-4 | «Elegir una posición en el lugar» presenta las posiciones con `GeoCeDGAddressPresentation`, la garantía de la ruta con la misma presentación que el panel, y la casilla **Permitir arrastre** (activada por defecto). La creación es un paso de Undo, que guarda el anfitrión | `GeoCeDGMetricEndpointAssistance.place`; `GeoCeDGReferencePointTool.routePresentation` |
| U2-5 | El rechazo `COMPONENT_NOT_ESTABLISHED` explica que las componentes de la curva no están establecidas en ese lugar y remite a `Point(curva, parámetro)` y a la herramienta 134 por su nombre localizado. F8 no se implementa | `GeoCeDGReferencePointTool.describeFailure` |
| U2-6 | §8.1 de las guías EN/ES reescrita como «Create a point on a curve» / «Crear un punto sobre una curva», con los dos orígenes, la regla de cada uno y la alternativa de dirección explícita; §9.3 describe la asistencia alineada | `docs/user/geocedg_user_guide_en.md`, `_es.md` |

### 1.1 Reglas presentadas (U2-3)

| Herramienta y origen | Radio | Confirmación |
|---|---|---|
| Punto / Punto en objeto, **Clic** | de la vista (tamaño del puntero y zoom) | la posición hallada por la búsqueda se crea al instante (con elección si hay varias); cualquier otra oferta espera a **Confirmar**; sobre objetos nativos actúa la herramienta ordinaria |
| Cualquiera, **Referencia Q** | `r world` alrededor de `Q` sobre Locus V2 y Spline V2; proyección ordinaria de `Q` sobre objetos nativos | toda oferta espera a **Confirmar** |
| 145, **Clic** | `r world` alrededor del clic | toda oferta espera a **Confirmar** |

Son las conductas de la base, caracterizadas por las pruebas existentes; el incremento solo las
presenta.

## 2. Decisiones de diseño dentro del alcance

1. **Preferencias de origen por herramienta.** Punto y Punto en objeto comparten una preferencia
   (Clic por defecto); la herramienta 145 conserva la suya (Referencia Q por defecto). Es el
   modelo de estado de la base (`fromReference` y `clickPosition`), que se conserva para no
   alterar la conducta de ninguna ruta. El menú contextual sigue activando Punto y fijando su
   origen en Referencia Q.
2. **Segmentos complementarios.** Las pruebas existentes accionan los controles por nombre
   (`m08-from-reference`, `m08-click-position`) y esperan que pulsar el control seleccionado lo
   deseleccione. Los dos segmentos conservan esos nombres y, al pulsar el seleccionado, eligen el
   otro origen. Así las pruebas anteriores siguen sin cambios.
3. **Línea de regla en una segunda fila.** El selector y la regla van bajo la fila de estado,
   para respetar las cotas de tamaño del panel que fijan las pruebas existentes (anchura menor
   que 900 px y altura menor que 90 px en EN y ES). Medido: 768×61 (EN) y 735×61 (ES) en
   Punto; 591×61 (EN) y 616×61 (ES) en la herramienta 145.
4. **Cambio de origen y política.** Al cambiar de origen se restaura y se muestra de nuevo la
   política de la referencia solo si una oferta de lienzo la había sustituido; las ediciones de
   la política aún no aplicadas se conservan. En la base, el conmutador de Punto descartaba esas
   ediciones y el de 145 las conservaba; ahora ambos las conservan. No afecta a ninguna ruta ni
   a ningún valor por defecto.
5. **Instrucción al activar.** Con Referencia Q la instrucción es «Seleccione objeto» en las tres
   herramientas. En la base, Punto con Referencia Q podía mostrar la instrucción de la posición
   de clic de la herramienta 145 si esa preferencia estaba activa; la instrucción depende ahora
   solo del origen de la herramienta activa.
6. **Sin cambios en `GeoCeDGEuclidianController` ni en recursos de traducción.** La activación
   existente de los modos 1, 501 y 145 ya abre el panel, así que el alias no requiere cambios en
   el controlador. Los textos nuevos siguen la convención del archivo (`t(en, es)`); la ayuda
   de la herramienta 145 («Select Locus V2, then an existing point») sigue siendo exacta.

## 3. Compatibilidad

- Sin cambios de XML, códecs, identidad, DAG, Undo semántico, modos (1, 501, 134, 145),
  acciones ni perfil (`apps/geocedg/application-profile.yml` sin cambios). Las perspectivas y
  barras guardadas siguen siendo válidas.
- Sin cambios en el núcleo, en `GeoCeDGReferencePlacement`, en `GeoCeDGPointRequest`, en la
  herramienta 134 ni en «Punto en objeto» sobre objetos Classic.
- Diferencias de interfaz deliberadas: los nombres visibles de los conmutadores (ahora
  **Clic**/**Referencia Q**), la línea de regla, la presentación de la asistencia de extremos
  (lista con etiquetas del panel en lugar de «clave de rama t = …», diálogo Aceptar/Cancelar en
  lugar de una lista desplegable) y la casilla **Permitir arrastre** en la asistencia. Antes, la
  asistencia no fijaba el punto; ahora queda arrastrable por defecto y el usuario puede fijarlo.

## 4. Archivos

| Archivo | Cambio |
|---|---|
| `.github/prompts/tasks/pre-g9b-maint-u2-point-tool-unification.prompt.md` | Nuevo (commit de gobernanza `4d351b2bd`) |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGReferencePointTool.java` | U2-1, U2-2, U2-3, U2-5; presentación de ruta compartida |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGMetricEndpointAssistance.java` | U2-4 |
| `source/desktop/desktop/src/test/java/org/geocedg/desktop/MaintU2PointToolUnificationDesktopTest.java` | Nuevo: 12 pruebas focales |
| `docs/user/geocedg_user_guide_en.md`, `docs/user/geocedg_user_guide_es.md` | U2-6 (§8.1 y un párrafo de §9.3) |
| `docs/upstream/modified-files.yml` | Registro de la prueba nueva |
| `geocedg/specs/operations/verification-registry.json` | Fase `PRE-G9B-MAINT-U2`, cuatro nodos JUnit y pin del inventario |
| `geocedg/specs/operations/verification-junit-inventory.json` | Selecciones `pre-g9b-maint-u2.shared` y `.desktop`; descubrimiento actualizado |
| `tools/agent/tests/verification-final-coverage.Tests.ps1` | Recuentos de selecciones (100) y de fases (90) |
| `docs/validation/pre_g9b_maint_u2_candidate_report.md`, `pre_g9b_maint_u2_author_smoke_guide.md` | Nuevos |

## 5. Pruebas

### 5.1 Caracterización previa

Sobre la base `e79ace7d1` (el producto del commit de gobernanza es idéntico), 13 clases Desktop
existentes de la herramienta Punto: 137 pruebas, 0 fallos (`Cu7M08SCorrectionTest`,
`Cu7M08SDesktopEventsTest`, `Cu7M08SGeneralCreationTest`, `Cu7M08SReferenceToolTest`,
`GeneralPointInteractionDesktopTest`, `Maintenance120ConeInteractionDesktopTest`,
`Maintenance120PointToolsDesktopTest`, `PostR1ConditionedPointDesktopTest`,
`R3PointInteractionDesktopTest`, `R4NativeBudgetDesktopTest`, `R4ViewActivationDesktopTest`,
`R5ReferenceCreationDesktopTest`, `R5SemanticPointPointerDesktopTest`). Es evidencia de
desarrollo (Gradle directo), no de aceptación.

### 5.2 Pruebas nuevas

`MaintU2PointToolUnificationDesktopTest`, sobre todo a través de eventos AWT reales del lienzo y
de los controles, sin ventanas visibles; los textos de rechazo y la cancelación de la asistencia
se comprueban directamente:

| Prueba | Obligación |
|---|---|
| `pointPointOnObjectAndAliasShareOnePanelWithOneOriginSelector` | U2-1, U2-2: mismo panel en 1, 501 y 145, EN y ES; Q preseleccionada solo en 145; modos sin reasignar; cotas de tamaño |
| `eachOriginStatesItsRadiusAndConfirmationRuleInBothLanguages` | U2-3 en EN y ES; segmentos complementarios; instrucciones |
| `eachToolKeepsItsOwnOriginPreferenceInTheDocument` | Preferencias por herramienta |
| `aliasAndPointGiveIdenticalAddressesForTheSameReferenceAndClick` | Equivalencia de direcciones con Q en 1, 501 y 145 |
| `aliasModeActionAndProfilePlacementAreUnchanged` | Modos, ayuda de 145 y posiciones de 1, 501, 134 y 145 en el perfil |
| `contextMenuOpensThePointPanelWithReferenceOriginAndTheObject` | Menú contextual y origen |
| `allowDraggingSurvivesAnOriginChangeAndLocksTheConfirmedPoint` | Permitir arrastre |
| `componentRejectionNamesPointByParameterAndTheExplicitAddressTool` | U2-5 en EN y ES |
| `endpointAssistanceUsesPanelPresentationDragChoiceAndOneUndoStep` | U2-4 por la herramienta de longitud parcial: presentación, garantía, arrastre y un paso de Undo |
| `endpointAssistanceCancelCreatesNothingAndIsLocalized` | U2-4: cancelación y textos en ES |
| `pointOnObjectWithReferenceOriginMatchesPointOnANativeSegment` | No regresión de Punto en objeto con Q |
| `refusedOfferShowsTheTypedCodeOnlyAsDetail` | El código tipado queda en la ayuda emergente |

### 5.3 Selección `PHASE PRE-G9B-MAINT-U2`

- `pre-g9b-maint-u2.desktop`: la prueba nueva y 21 clases existentes (las 13 de §5.1,
  `G9U1SemanticPointInteractionTest`, las de perfil e iconos `G9U1ProfileCompilerTest`,
  `GeoCeDGProfileTest`, `G9U1WorkspaceSurfaceTest`, `G9U1IconReviewTest`, y las de la guía
  `PostP1BilingualUserGuideTest`, `PreG9BR5AGuideOutlineTest`,
  `PreG9BCu0GuideCoverageMatrixTest`).
- `pre-g9b-maint-u2.shared`: las pruebas del núcleo `M08PointCreationKernelTest`,
  `IndividualForwardPointCreationTest`, `R5ForwardCreationRoutingTest` y
  `Maintenance120PointInteractionTest`.
- Las selecciones se rellenaron con evidencia ejecutada de los productores sobre el árbol del
  candidato: 64 identidades en `shared` y 272 en `desktop`, todas en verde. El descubrimiento
  se actualizó por ejecución en seco (shared 7057 sin cambios; Desktop 2607 → 2619, las 12
  pruebas nuevas). `input_identities` de las selecciones nuevas es el commit de gobernanza
  `4d351b2bd`, que era `HEAD` al registrarlas. Las selecciones `core.*` no
  cambian (sus filtros no incluyen la prueba nueva) y las `final.*` no se actualizan: solo las
  usa `FINAL`, que no está autorizado.
- Las selecciones existentes no se modifican ni se debilitan.

## 6. Impactos declarados

- `GUIDE_IMPACT = UPDATED`: `docs/user/geocedg_user_guide_en.md` y
  `docs/user/geocedg_user_guide_es.md`, §8.1 y §9.3.
- `BOOTSTRAP IMPACT — NO CHANGE REQUIRED`: no cambian requisitos, herramientas, entornos ni
  procedimientos de instalación.
- `VERIFICATION_INFRASTRUCTURE_IMPACT`: solo el registro de la selección de fase (registro,
  inventario, pin y dos literales de recuento). Ningún verificador, esquema ni comprobación
  cambia.

## 7. Deudas residuales

1. **Ctrl+clic con Punto sobre Locus V2** sigue la ruta heredada; no se fija con una prueba
   (deuda diferida del prompt).
2. **F8** no se implementa. No hay un modelo de 1.2.2 que reproduzca `COMPONENT_NOT_ESTABLISHED`
   desde el panel; la prueba de U2-5 fija el texto, no un caso real. La caracterización es
   `MAINT-F8-S0`.
3. **Rechazo en el clic de Punto.** Cuando el clic con Punto sobre un Locus V2 se rechaza antes
   de llegar al panel, el aviso modal de `GeoCeDGLocusV2Dialogs` sigue mostrando el motivo
   genérico de evidencia local insuficiente. Ese archivo está fuera del alcance autorizado.
4. **Fallo al confirmar en la asistencia de extremos.** Si la creación se rechaza en el último
   paso, el aviso muestra el código tipado, como en la base.
5. **Manifiesto de archivos modificados.** `Assert-GeoCeDGUpstreamBoundary` ya falla en la base
   por más de cien archivos GeoCeDG sin registrar, entre ellos
   `GeoCeDGMetricEndpointAssistance.java`. Este incremento registra solo su prueba nueva.
6. **Registro paralelo de fases.** Otros incrementos de la primera ola (`MAINT-N1`,
   `MAINT-A-N1`) se desarrollan en paralelo y pueden registrar sus propias selecciones. La
   integración de varias ramas exigirá conciliar el registro, el inventario, su pin y los
   literales de recuento; el orden de integración es una decisión del autor.
7. La etiqueta del botón de confirmación del diálogo de la asistencia sigue la configuración
   regional de Swing; la guía no la nombra.
