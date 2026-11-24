# GeoCeDG — PRE-G9B-MAINT-U2-R1: corrección funcional y de presentación tras el smoke autoral

**PROMPT DE TAREA AUTORIZADO POR EL AUTOR EL 2026-11-24** («GeoCeDG — PRE-G9B-MAINT-U2-R1.
Corrección funcional y de presentación tras smoke autoral»), sobre el candidato técnico
`PRE-G9B-MAINT-U2` indicado abajo. Es la versión reconciliada de esa autorización con el código
del candidato. `selfApproved=false`. No reescribe el prompt histórico
[`pre-g9b-maint-u2-point-tool-unification`](pre-g9b-maint-u2-point-tool-unification.prompt.md)
ni las verificaciones de `98e21093d`, y no autoriza ningún otro incremento de `PRE-G9B-MAINT`.

## Objective
<!-- geocedg-field: objective -->

Corregir los tres defectos y aplicar las dos mejoras que el autor observó en el smoke manual de
`PRE-G9B-MAINT-U2`, sin cambiar la conducta que el autor aprobó en ese smoke:

- **U2-R1-1:** ayudas emergentes del panel Punto compactas y en varias líneas (propósito, radio
  de búsqueda y regla de confirmación separados), en EN y ES.
- **U2-R1-2:** ayuda de la herramienta 134 «Point on semantic curve (explicit address)» en
  Construction → Semantic curves, compacta y en varias líneas, con su nombre y su significado.
- **U2-R1-3 (prioridad principal):** la asistencia de extremos métricos («Choose a position on
  the locus» / «Elegir una posición en el lugar») muestra las posiciones candidatas sin elección
  implícita, exige una selección explícita antes de confirmar, distingue el extremo A del punto
  nuevo y nunca crea un punto en A como sustituto de una selección no efectuada.
- **U2-R1-4:** iconos propios para «Total semantic locus length» y «Partial semantic locus
  length», distintos del de Locus V2, que no cambia.
- **U2-R1-5:** icono propio para «Closed Semantic Spline V2», con una señal discreta de cierre,
  distinto del de Spline V2, que no cambia.

`CHANGE_ROUTE = ORDINARY`. `VERIFICATION_CLASS = INTEGRATED_PHASE`.
`PRODUCT_PHASE_EFFECT = PRE-G9B-MAINT-U2-R1`. Nivel de aceptación: `STATIC` +
`PHASE PRE-G9B-MAINT-U2` + `INTEGRATION`, más smoke manual del autor. `FINAL = NOT AUTHORIZED`.
`AUTHOR_DECISION = PENDING`.

## Implementation base
<!-- geocedg-field: implementation_base -->

- Repositorio `mpradovelasco/GeoCeDG`.
- Commit `98e21093d2795268c25b2c5086a886ca8c288040` (candidato técnico `PRE-G9B-MAINT-U2`).
- Árbol `3d97cbafeb4023bc5d8c79bedd6626861c7f09ab`.

Trabajo en el worktree `GeoCeDG-wt-maint-u2`, en la rama local `phase/pre-g9b-maint-u2-r1`,
creada desde el commit exacto; la rama `phase/pre-g9b-maint-u2` no se mueve. El repositorio
principal y los worktrees de `MAINT-N1` y `MAINT-A-N1` no se tocan, y no se incorpora su trabajo
(sin rebase, merge ni cherry-pick).

## Authority and evidence hierarchy

1. `AGENTS.md`, código, pruebas y contratos de serialización vigentes en la base.
2. El smoke autoral de `98e21093d`: es evidencia de conducta real y prevalece sobre la guía de
   smoke de U2 donde discrepan.
3. El prompt de `PRE-G9B-MAINT-U2` y sus autoridades (ADR 0019 y 0041,
   `geocedg/specs/locus/locus-v2-point-interaction.md`, decisión CU7-R5, decisión E del
   mantenimiento posterior a 1.2.0), el informe del candidato y la guía de smoke de U2.
4. Contrato de verificación vigente: `tools/agent/verify.ps1`, el registro tipado y sus
   esquemas, y la sección activa de `geocedg/specs/operations/verification-levels.md`.

## Allowed scope
<!-- geocedg-field: allowed_scope -->

- `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGMetricEndpointAssistance.java`:
  flujo, textos y controles de los dos diálogos de la asistencia.
- `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGReferencePointTool.java`:
  textos de las ayudas emergentes del panel.
- `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGAddressPresentation.java`:
  solo la escritura del parámetro (sin signo en un cero redondeado, «u = -0.0000»).
- Una clase Desktop nueva de ayudas emergentes multilínea y su uso en
  `GeoCeDGActionRegistry.java`, `GeoCeDGMenuBar.java` y `GeoCeDGWorkspaceController.java`, solo
  para que una ayuda de herramienta con saltos de línea explícitos se muestre en varias líneas y
  conserve una descripción accesible en texto plano.
- La ayuda `LocusV2.Point.Help` en `menu.properties`, `menu_en.properties` y
  `menu_es.properties` de `source/shared/common-jre`.
- Tres SVG nuevos en `source/shared/common/src/main/resources/org/geogebra/common/icons/svg/web/toolIcons/`,
  su asociación en `org/geocedg/desktop/resources/GeoCeDGToolImageResource.java` y su registro en
  `geocedg/resources/assets-manifest.yml` y en `docs/upstream/modified-files.yml`.
- Pruebas Desktop de U2 que fijaban la conducta corregida, pruebas nuevas de R1 y la prueba de
  recursos de iconos que fijaba el icono compartido.
- `docs/user/geocedg_user_guide_en.md` y `docs/user/geocedg_user_guide_es.md` (§9.3 y las
  referencias a los iconos o a la ayuda que cambien).
- `docs/validation/pre_g9b_maint_u2_author_smoke_guide.md` (procedimiento de la asistencia),
  un informe y una guía de smoke reducida de R1 en `docs/validation/`.
- Ampliación aditiva de la selección `PHASE PRE-G9B-MAINT-U2` con las pruebas de R1 y las
  pruebas de iconos, ayuda y acciones afectadas, con el pin del inventario.
- Este prompt, registrado en un commit preparatorio de gobernanza separado del commit técnico.

## Explicitly forbidden scope
<!-- geocedg-field: forbidden_scope -->

- Núcleo: `LocusPointCreation2D`, la resolución semántica de direcciones, los certificados y
  evidencias de componentes, los tokens de intersección, `SemanticMetricEndpointResolver2D`,
  `AlgoProvenLocusEndpoint2D` y los algoritmos de Locus V2 y de las longitudes.
- `GeoCeDGReferencePlacement`, la política de colocación de la asistencia (radio `0,1`) y la
  conducta de selección del selector `Clic | Referencia Q`, incluida la acción complementaria al
  pulsar el segmento ya seleccionado.
- Identidad, DAG, XML, códecs, Undo/Redo semántico.
- Identificadores de modo (1, 501, 134, 136, 137, 139, 145) y de acción, el perfil, las barras,
  los valores por defecto, las preferencias de origen por herramienta, la herramienta 145 como
  alias, Punto y Punto en objeto sobre objetos Classic, el menú contextual, los radios de
  búsqueda y las reglas de confirmación del procedimiento ordinario.
- La semántica y el algoritmo de cierre de Spline V2; los iconos de Locus V2 y de Spline V2.
- Bibliotecas gráficas o infraestructura nueva de iconos.
- `MAINT-F8`, Ctrl+clic y las demás deudas diferidas de U2; `MAINT-S0`, `S1`, `S2`, `I2G0`,
  `N1`, `A-N1`.
- `CLAUDE.md`, `AGENTS.md`, otros prompts canónicos o de `ai-shell/prompts/`, el prompt
  histórico de U2, el roadmap canónico, `geocedg_book`, `main`, etiquetas y releases.

## Architectural placement

Capa de aplicación Desktop: diálogos, ayudas emergentes, recursos gráficos y su asociación.
La admisión semántica y la publicación siguen en el núcleo a través de
`GeoCeDGReferencePlacement` y `LocusPointCreation2D`; la asistencia solo presenta las
posiciones que ofrece la política y confirma la elegida.

## Required design/specification

No requiere especificación normativa nueva. Si la corrección de U2-R1-3 exigiera cambiar el
núcleo o una interfaz semántica compartida, se detiene y se presenta el diagnóstico.

## Geometric invariants and degeneracies

La dirección del punto creado es exactamente la de la posición elegida en la lista; nunca se
deriva de la proximidad, del ratón ni de la apariencia. Sin posición admisible, o sin una
selección explícita, no se crea nada. Un paso de Undo por creación.

## Compatibility and serialization

Sin cambios de XML, modos, acciones ni perfil. Los iconos nuevos solo cambian la imagen de dos
herramientas de longitud y de la herramienta de Spline V2 cerrado; las barras guardadas siguen
siendo válidas.

## Required tests and commands
<!-- geocedg-field: required_checks -->

Antes del código, caracterizar con una sonda temporal (fuera del árbol) el flujo real de la
asistencia: secuencia de diálogos y controles, selección inicial, momento de la creación y
dirección usada. Pruebas nuevas o ampliadas:

1. asistencia completa a través de los controles reales: alternativas, activación de la
   elección, candidatas sin selección implícita, botón de creación inactivo hasta seleccionar,
   selección por ratón, garantía, «Permitir arrastre» activado y desactivado, dirección
   creada igual a la elegida, cancelación sin mutaciones, ausencia de creación sin posición
   admisible, un único Undo, Redo y actualización dinámica del punto;
2. varias posiciones candidatas sin elección implícita y correspondencia de la elegida;
3. ayudas emergentes multilínea del panel y de la herramienta 134 en EN y ES, sin cambiar la
   semántica de los controles;
4. iconos propios de las dos longitudes y de Spline V2 cerrado, distintos entre sí y de los de
   Locus V2 y Spline V2, legibles a tamaño de barra y en HiDPI.

La prueba de la asistencia falla con la conducta inicial y pasa con la corrección. Las pruebas
de U2 siguen en verde, salvo las aserciones que fijaban la conducta corregida, que se ajustan.
Sobre el commit técnico congelado y limpio:

```powershell
git diff --check 98e21093d2795268c25b2c5086a886ca8c288040 HEAD
& tools/agent/verify.ps1 -Profile STATIC -LogDirectory <directorio-nuevo>
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-MAINT-U2 -LogDirectory <directorio-nuevo>
& tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory <directorio-nuevo>
```

Registrar comandos exactos, códigos de salida, identificadores de ejecución, rutas de registro,
cobertura efectiva, diagnósticos y diferencias con el candidato anterior. No ejecutar `FINAL`.
No atribuir cobertura exhaustiva a `CORE` + `FOCUSED`.

## Authorization boundary
<!-- geocedg-field: authorization_boundary -->

Autorizado: las cinco actuaciones U2-R1-1 a U2-R1-5, el registro de este prompt, la ampliación
de la selección `PHASE PRE-G9B-MAINT-U2`, los commits locales y la verificación `STATIC`,
`PHASE` e `INTEGRATION`. No autoriza F8, Ctrl+clic, otro incremento ni `FINAL`. La aceptación
técnica no es aprobación autoral.

## Publication boundary
<!-- geocedg-field: publication_boundary -->

Commits locales en `phase/pre-g9b-maint-u2-r1`. Sin push, merge, promoción a `main`, etiquetas
ni releases. El worktree se conserva para la revisión del autor.

## Acceptance and closeout

El incremento termina como `PRE-G9B-MAINT-U2-R1 — TECHNICAL CANDIDATE — PENDING AUTHOR REVIEW`.
El candidato congelado no registra una aprobación futura:

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
```

El cierre de U2 requiere el smoke autoral sobre R1 y una decisión expresa que identifique el
commit aceptado.

## Required artifacts

- Rama, ruta del worktree, SHA y árbol del commit técnico y del commit de gobernanza.
- Causa de U2-R1-3, corrección y pruebas de regresión.
- Relación de ayudas emergentes y de iconos y recursos modificados.
- Capturas EN/ES de las ayudas, del diálogo corregido y de los iconos a tamaño real, en
  `artifacts/agent/` sin añadirlas al repositorio.
- Guía de smoke reducida con el modelo de reproducción de la asistencia.
- Resultados de `STATIC`, `PHASE` e `INTEGRATION`; deudas residuales; estado de los worktrees de
  N1 y A-N1.

## Stop conditions

Un cambio semántico necesario en el núcleo; la imposibilidad de garantizar que la dirección
creada es la elegida; un cambio de identidad, DAG o XML; una regresión en la creación de puntos;
un cambio incompatible de perfil o de modos; la necesidad de sustituir la infraestructura de
iconos; una verificación requerida que falle por causa de R1; la pérdida del aislamiento de los
worktrees.

## Deferred debts

Las deudas residuales de U2 que este prompt no aborda: Ctrl+clic, F8, el rechazo genérico de
`GeoCeDGLocusV2Dialogs`, el manifiesto upstream que ya falla en la base y la conciliación con
N1 y A-N1.
