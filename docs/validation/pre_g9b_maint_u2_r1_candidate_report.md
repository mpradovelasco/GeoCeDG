# PRE-G9B-MAINT-U2-R1 — Informe del candidato técnico

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
CHANGE_ROUTE              = ORDINARY
VERIFICATION_CLASS        = INTEGRATED_PHASE
PRODUCT_PHASE_EFFECT      = PRE-G9B-MAINT-U2-R1
GUIDE_IMPACT              = UPDATED
```

| Campo | Valor |
|---|---|
| Incremento | `PRE-G9B-MAINT-U2-R1`: corrección funcional y de presentación tras el smoke autoral de U2 (U2-R1-1 a U2-R1-5) |
| Autorización | Instrucción autoral «GeoCeDG — PRE-G9B-MAINT-U2-R1. Corrección funcional y de presentación tras smoke autoral», 2026-11-24 |
| Base | candidato `PRE-G9B-MAINT-U2` `98e21093d2795268c25b2c5086a886ca8c288040`, árbol `3d97cbafeb4023bc5d8c79bedd6626861c7f09ab` |
| Rama | `phase/pre-g9b-maint-u2-r1`, en el worktree `GeoCeDG-wt-maint-u2`; `phase/pre-g9b-maint-u2` no se mueve |
| Prompt | [`pre-g9b-maint-u2-r1-smoke-corrections`](../../.github/prompts/tasks/pre-g9b-maint-u2-r1-smoke-corrections.prompt.md), registrado en el commit de gobernanza `ae688c3939ca9ee9bc6fe65f3a9a47dd9a0af990` |
| Guía de smoke | [guía reducida de R1](pre_g9b_maint_u2_r1_author_smoke_guide.md) |
| `selfApproved` | `false` |

Este artefacto forma parte del commit técnico que describe. No contiene los resultados de la
verificación de ese commit (`STATIC`, `PHASE PRE-G9B-MAINT-U2`, `INTEGRATION`), que se obtienen
después sobre el commit congelado y se entregan aparte. No reescribe `98e21093d` ni sus
verificaciones. La aprobación autoral, si llega, se registra en otro documento.

## 1. Causa de U2-R1-3

Caracterización con una sonda temporal fuera del árbol, sobre `98e21093d` sin cambios, con los
diálogos modales reales manejados desde un hilo auxiliar y el modelo de la guía de smoke de U2
(lugar `h` de CU7, `A=(0.8976…, 1.7854…)`, `B=(3,-2)`, herramienta 136, clics en `h`, `A`, `B`):

1. El tercer clic lanza la asistencia porque `A` no tiene dirección métrica (`NO_ADDRESS`).
   Primer diálogo: «The selected point has no admitted metric address.», con **Choose a
   position on the locus** y **Cancel**. No nombraba ni `A` ni `h`.
2. Segundo diálogo: la política de colocación (radio 0,1 alrededor de `A`) ofrece **una sola
   posición**, «Branch 1 · Section 1 · u = -0.0000», **ya seleccionada**, con **OK** activo
   desde el principio. Pulsar la fila no cambia nada, porque ya estaba seleccionada.
3. **OK** crea un punto semántico en `(0.8876…, 1.7855…)`, a 0,010 de `A`, con dirección
   `generator.main / …/periodic-closure / u=-6.4e-11`. A la vista es «un punto sobre `A`».
   La longitud no se calcula: la herramienta termina con el punto creado y no lo dice.
4. Con `A=(0.8, 2.45)` ocurre lo mismo: una posición preseleccionada y un punto a 0,005 de `A`.

Las direcciones son correctas: el punto queda sobre `h`, en la posición de `h` junto a `A`. El
defecto está en el flujo del diálogo, no en el núcleo ni en el algoritmo de creación:

- **selección implícita**: la única posición venía elegida y el botón activo, así que el
  usuario solo podía confirmar, y el punto aparecía junto a `A` sin una selección propia;
- **textos que no distinguen** el extremo `A` del punto nuevo: no decían que las posiciones
  son las de `h` junto a `A`, que `A` no cambia ni que hay que volver a medir;
- la **prueba de U2** sustituía los dos diálogos por respuestas simuladas
  (`mockStatic(JOptionPane)`) y confirmaba la fila preseleccionada: solo comprobaba el
  resultado final, no la selección ni los controles.

En el lugar `h` de la guía, un barrido de 8.181 referencias (rejilla de 0,05) nunca ofrece más de
una posición con radio 0,1. Con varias candidatas, el comportamiento de U2 sí exigía elegir.

## 2. Resultado

| Id | Cambio | Dónde |
|---|---|---|
| U2-R1-1 | Las ayudas emergentes del panel Punto pasan a varias líneas cortas (60 caracteres como máximo). La regla de origen separa propósito, **Radio:** y **Confirmar:**; «Permitir arrastre», la garantía de la ruta, el encabezado de las ofertas y el estado (con el código tipado del rechazo en su propia línea) se compactan. Medido con la fuente por defecto: de 1.150–1.620 px en una línea a unos 300–340 px | `GeoCeDGReferencePointTool.showOrigin`, `buildPanel`, `updateStatus`, `refuse`, `showChoices`, `showSelection`; `GeoCeDGTooltips` |
| U2-R1-2 | La ayuda de la herramienta 134 pasa a tres líneas compactas en EN y ES, con el mismo nombre y significado. Una ayuda de herramienta con saltos de línea explícitos se muestra en varias líneas en el menú, en el desplegable y en la barra; la descripción accesible sigue siendo texto plano | `LocusV2.Point.Help` en `menu.properties`, `menu_en.properties`, `menu_es.properties`; `GeoCeDGActionRegistry`, `GeoCeDGMenuBar`, `GeoCeDGWorkspaceController` |
| U2-R1-3 | El primer diálogo dice qué extremo carece de dirección sobre qué curva y que no cambia. El segundo enumera las posiciones de la curva junto a `A`, **sin ninguna seleccionada**, con **Crear punto** desactivado hasta que el usuario pulsa una fila, la garantía, **Permitir arrastre** y una nota: el punto nuevo no sustituye a `A` en la longitud y hay que volver a medir. Sin posición admisible, el diálogo lo dice y no crea nada. Cancelar o cerrar no crea nada. La dirección creada es la de la fila elegida | `GeoCeDGMetricEndpointAssistance` |
| U2-R1-3 | El parámetro que redondea a cero se escribe sin signo («u = 0.0000», no «-0.0000») en la asistencia y en el panel | `GeoCeDGAddressPresentation.label` |
| U2-R1-4 | Iconos propios de longitud total (la curva entera en azul con sus extremos y una cota de lado a lado) y parcial (un tramo azul entre dos extremos blancos y una cota de ese tramo). El de Locus V2 no cambia | `mode_geocedg_locuslength_total.svg`, `mode_geocedg_locuslength_partial.svg`; `GeoCeDGToolImageResource` |
| U2-R1-5 | Icono propio de Spline V2 cerrada: el dibujo de Spline V2 con una flecha azul fina que une el último punto de control con el primero. Los de Spline V2 y Spline V2 con grado no cambian | `mode_geocedg_splinev2_closed.svg`; `GeoCeDGToolImageResource` |

## 3. Decisiones de diseño dentro del alcance

1. **Sin selección implícita, ni siquiera con una sola posición.** Es la regla que el autor
   pide («nunca debe crearse silenciosamente un punto en A»). El panel Punto conserva su regla
   (una oferta única por clic se confirma con **Confirmar**); la asistencia es un diálogo
   aparte y su conducta corregida no cambia la del panel.
2. **Las candidatas siguen siendo las de la política de colocación junto a `A`.** La asistencia
   existe para dar a la longitud un extremo semántico en el lugar de `A`, así que ofrece las
   posiciones de `h` que la política admite alrededor de `A` (radio 0,1, sin cambios). Elegir
   cualquier otra posición de la curva con el ratón es la tarea de la herramienta Punto (§8.1
   de la guía). La dirección del punto nunca se deriva de la proximidad: es la de la fila
   elegida, y la prueba lo comprueba comparando la dirección técnica de la fila con la del
   punto creado.
3. **Diálogos a través de un único punto de presentación.** Los tres diálogos de la asistencia
   (rutas, posiciones, avisos) se muestran con un `JOptionPane` real mediante
   `GeoCeDGMetricEndpointAssistance.Modal`. En el producto es la ventana modal (`WINDOW`); las
   pruebas sustituyen solo la ventana y pulsan los botones, la lista y la casilla reales, con
   sus propios oyentes.
4. **Sin continuación automática de la longitud.** Tras crear el punto, la herramienta termina
   como antes; el diálogo lo dice («vuelva a medir con el punto nuevo»). Encadenar la medida
   cambiaría la conducta de la herramienta y no se pidió.
5. **Ayudas multilínea con saltos explícitos.** `GeoCeDGTooltips` produce el HTML que las
   ayudas de Swing ya admiten (`<br>`), con líneas de 60 caracteres como máximo. Los saltos son
   explícitos, así que la anchura sigue la fuente y escala con ella en HiDPI. En las acciones
   del perfil solo cambian las ayudas de herramienta que contienen un salto de línea (hoy, la
   134); las demás conservan su texto exacto.
6. **Iconos con el lenguaje existente.** Mismo marco de 24 unidades, trazo negro `#111`,
   acento azul `#4b6fff`, círculos blancos de control y cotas con puntas rellenas, como los de
   las cotas de R6-plus-E2. Se descartaron dos variantes de cierre: un ↻ abajo a la derecha (se
   tocaba con un punto de control y no se leía a 24 px) y un anillo arriba a la izquierda (se
   confundía con un punto de control).

## 4. Compatibilidad

- Sin cambios en el núcleo, `LocusPointCreation2D`, `GeoCeDGReferencePlacement`, la política
  de colocación, la resolución de direcciones, certificados, tokens, identidad, DAG, XML,
  códecs ni Undo/Redo semántico.
- Sin cambios de modos (1, 501, 134, 136, 137, 139, 145), acciones, perfil, barras ni valores
  por defecto. El selector **Clic | Referencia Q**, las preferencias de origen por herramienta,
  la herramienta 145, el menú contextual, los radios y las reglas de confirmación del panel no
  cambian.
- Los iconos nuevos cambian solo la imagen de tres herramientas; las barras guardadas siguen
  siendo válidas.
- Diferencias de interfaz deliberadas: los textos y botones de la asistencia (**Crear punto**
  en lugar de **Aceptar**), la ausencia de preselección, el formato multilínea de las ayudas y
  el nuevo texto de `LocusV2.Point.Help`.

## 5. Archivos

| Archivo | Cambio |
|---|---|
| `.github/prompts/tasks/pre-g9b-maint-u2-r1-smoke-corrections.prompt.md` | Nuevo (commit de gobernanza `ae688c393`) |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGMetricEndpointAssistance.java` | U2-R1-3 |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGTooltips.java` | Nuevo: ayudas multilínea |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGReferencePointTool.java` | U2-R1-1 |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGAddressPresentation.java` | Parámetro sin signo en cero |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGActionRegistry.java`, `GeoCeDGMenuBar.java`, `GeoCeDGWorkspaceController.java` | U2-R1-2: ayuda multilínea y descripción accesible plana |
| `source/shared/common-jre/src/main/resources/org/geogebra/common/jre/properties/menu.properties`, `menu_en.properties`, `menu_es.properties` | U2-R1-2: `LocusV2.Point.Help` |
| `source/shared/common/src/main/resources/org/geogebra/common/icons/svg/web/toolIcons/mode_geocedg_locuslength_total.svg`, `mode_geocedg_locuslength_partial.svg`, `mode_geocedg_splinev2_closed.svg` | Nuevos: U2-R1-4, U2-R1-5 |
| `source/desktop/desktop/src/main/java/org/geocedg/desktop/resources/GeoCeDGToolImageResource.java` | Asociación de los tres iconos |
| `geocedg/resources/assets-manifest.yml`, `docs/upstream/modified-files.yml` | Registro de los recursos y archivos nuevos |
| `source/desktop/desktop/src/test/java/org/geocedg/desktop/MaintU2R1SmokeCorrectionsDesktopTest.java` | Nuevo: 8 pruebas |
| `source/desktop/desktop/src/test/java/org/geocedg/desktop/MaintU2PointToolUnificationDesktopTest.java` | Las dos pruebas de la asistencia recorren los paneles reales; la del rechazo espera el código en su propia línea |
| `source/desktop/desktop/src/test/java/org/geocedg/desktop/GeoCeDGToolImageResourceTest.java` | Cada modo con su icono; el rojo solo se exige donde el dibujo lo tiene |
| `docs/user/geocedg_user_guide_en.md`, `docs/user/geocedg_user_guide_es.md` | §9.3: la asistencia corregida |
| `docs/validation/pre_g9b_maint_u2_author_smoke_guide.md` | §7 corregido y remisión a la guía de R1 |
| `geocedg/specs/operations/verification-junit-inventory.json`, `verification-registry.json` | Ampliación aditiva de la selección `pre-g9b-maint-u2.desktop`, rutas de impacto y pin |
| `docs/validation/pre_g9b_maint_u2_r1_candidate_report.md`, `pre_g9b_maint_u2_r1_author_smoke_guide.md` | Nuevos |

## 6. Pruebas

### 6.1 La regresión falla con el defecto

Con el código corregido y una sola línea temporal que reintroduce la conducta de U2 (la única
fila preseleccionada y el botón activo), `MaintU2R1SmokeCorrectionsDesktopTest` falla en
`endpointAssistanceRequiresAnExplicitSelectionThroughTheRealPanes` con «no position is chosen
for the user ==> expected: <-1> but was: <0>». Sin esa línea, las 8 pruebas pasan. El archivo se
restauró y se comprobó que su hash coincidía con la versión corregida. Es evidencia de desarrollo
(Gradle directo), no de aceptación.

### 6.2 Pruebas nuevas

`MaintU2R1SmokeCorrectionsDesktopTest`:

| Prueba | Obligación |
|---|---|
| `endpointAssistanceRequiresAnExplicitSelectionThroughTheRealPanes` | Por la herramienta 136 y eventos reales del lienzo: textos que nombran `A` y `h`; alternativas; candidatas sin selección; **Crear punto** inactivo (pulsarlo no hace nada) hasta el clic de ratón en la fila; garantía; **Permitir arrastre** desactivado → punto fijo; un único punto con la dirección de la fila elegida, distinto de `A`, sin `A` como padre; `A` sin cambios; un paso de Undo; actualización dinámica al mover `I` con la misma dirección; Undo y Redo con la misma dirección |
| `cancellingOrClosingTheAssistanceCreatesNothingInSpanish` | Cancelar en cada diálogo (también con una fila seleccionada) y cerrar sin valor: sin mutación; textos en ES |
| `withoutAnAdmissiblePositionNothingIsCreatedAndTheReasonIsShown` | Sin posición admisible: aviso y ningún punto por aproximación |
| `severalPositionsHaveNoImplicitChoiceAndTheSelectedOneIsCreated` | Dos posiciones (spline recorrida dos veces): sin selección implícita; se crea la segunda, la elegida, no la primera; arrastre activo |
| `pointPanelTooltipsAreCompactAndMultiLineInBothLanguages` | U2-R1-1 en EN y ES, Punto y 145, los dos orígenes: propósito primero, **Radio:**, **Confirmar:**, líneas de 60 caracteres como máximo, anchura menor que 520 px y menor que la mitad de la de una sola línea |
| `refusalTooltipKeepsTheTypedCodeOnItsOwnLine` | El código tipado del rechazo en la última línea |
| `explicitAddressToolHelpIsCompactAndMultiLine` | U2-R1-2 en EN y ES: tres líneas, nombre sin cambios, elemento de menú y barra multilínea, descripción accesible plana; las demás ayudas no cambian |
| `lengthAndClosedSplineToolsHaveTheirOwnIcons` | U2-R1-4 y U2-R1-5: asociación por clave y por modo, Locus V2 y Spline V2 sin cambios, cinco imágenes distintas entre sí a 24, 32 y 48 px, iconos de las acciones del perfil |

### 6.3 Pruebas ajustadas

- `MaintU2PointToolUnificationDesktopTest`: `endpointAssistanceUsesPanelPresentationDragChoiceAndOneUndoStep`
  y `endpointAssistanceCancelCreatesNothingAndIsLocalized` conservan sus obligaciones
  (presentación, garantía, arrastre, un Undo, cancelación en ES), pero recorren los paneles
  reales y eligen la fila con el ratón; `refusedOfferShowsTheTypedCodeOnlyAsDetail` espera el
  código en su propia línea.
- `GeoCeDGToolImageResourceTest`: las dos longitudes resuelven sus propios iconos; el azul
  propio y el trazo `#111` se exigen siempre y el rojo solo en el icono de Locus V2.

### 6.4 Selección `PHASE PRE-G9B-MAINT-U2`

Ampliación aditiva de `pre-g9b-maint-u2.desktop` con `MaintU2R1SmokeCorrectionsDesktopTest`,
`GeoCeDGToolImageResourceTest`, `G9U1ActionRegistryTest`, `PreG9BR4SplineV2AuthoringTest` y
`locus.G9U0LocalizationHelpTest` (27 clases; de 272 a 327 identidades, con evidencia ejecutada;
el descubrimiento Desktop pasa de 2.619 a 2.627 y el compartido sigue en 7.057;
`pre-g9b-maint-u2.shared` sigue en 64). Ningún filtro anterior se retira. La selección registrada sigue
siendo `PRE-G9B-MAINT-U2`, sin una fase R1 separada: la autorización pide incluir las pruebas
de R1 en la selección focal de U2 y la infraestructura no exige otra. Los recuentos de
selecciones (100) y de fases (90) no cambian.

## 7. Impactos

- `GUIDE_IMPACT = UPDATED`: `docs/user/geocedg_user_guide_en.md` y
  `docs/user/geocedg_user_guide_es.md`, §9.3.
- `BOOTSTRAP IMPACT — NO CHANGE REQUIRED`: no cambian el arranque, la gobernanza de agentes ni
  los prompts canónicos (solo se añade el prompt de tarea de R1).
- Infraestructura de verificación: solo la ampliación de la selección y su pin.

## 8. Deudas residuales

1. Las de U2 que R1 no aborda: Ctrl+clic; F8; el rechazo genérico de `GeoCeDGLocusV2Dialogs`
   antes del panel; el manifiesto upstream, que ya falla en la base; la conciliación del
   registro, del inventario y de los literales de recuento con N1 y A-N1.
2. Tras crear el extremo, la longitud se mide de nuevo a mano (decisión 4). Encadenarla sería
   un cambio de conducta que requiere decisión autoral.
3. La asistencia sigue mostrando el código tipado entre corchetes cuando el núcleo rechaza la
   confirmación (`[...]`), ahora tras «No se creó ningún punto:».
4. Las selecciones `final.*` del inventario no se regeneran (FINAL no está autorizado), como
   en U2.
5. El botón **Aceptar** de los avisos sigue el idioma de Swing y no el de GeoCeDG, como en U2;
   los botones de la asistencia ya están localizados.
