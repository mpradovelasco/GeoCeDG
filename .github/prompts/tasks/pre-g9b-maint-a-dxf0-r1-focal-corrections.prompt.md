# GeoCeDG — PRE-G9B-MAINT-A-DXF0-R1: corrección focal de menú, madurez, presentación e interfaz

**PROMPT DE TAREA AUTORIZADO POR EL AUTOR EL 2026-11-25** («GeoCeDG — PRE-G9B-MAINT. Cierre de
segunda ola, consolidación Git y planificación de interoperabilidad DXF ampliada», §5), sobre el
candidato técnico `PRE-G9B-MAINT-A-DXF0` indicado abajo. Es la versión reconciliada de esa
autorización con el código del candidato. `selfApproved=false`. No reescribe el prompt histórico
[`pre-g9b-maint-a-dxf0-dxf-spline-reference-import`](pre-g9b-maint-a-dxf0-dxf-spline-reference-import.prompt.md)
ni las verificaciones de `177da00f6`, y no autoriza ningún otro incremento de `PRE-G9B-MAINT`.

## Objective
<!-- geocedg-field: objective -->

El autor informó `AUTHOR_SMOKE = PASS — AUTHOR REPORTED` para el importador `R-a` de A-DXF0 y
pidió una corrección focal antes de su aprobación:

- **R1-1 (menú):** la acción de importación pasa al final del submenú `Insert` de File, con los
  nombres «Import DXF Reference…» y «Importar referencia DXF…». La acción y la conducta del
  importador no cambian.
- **R1-2 (madurez y denominaciones):** la importación está disponible siempre, sin bandera, y no
  se califica como experimental en la interfaz; su madurez formal se reconcilia con `AGENTS.md`
  §5 de forma explícita y acotada al contrato implementado (`R-a`), sin declarar `R-c` ni
  A-DXF1. Se retira el calificativo «experimental» de las denominaciones visibles de la
  exportación DXF existente, sin cambiar sus algoritmos, sus garantías ni sus advertencias
  científicas legítimas.
- **R1-3 (presentación):** las referencias visibles se representan en todas las salidas
  gráficas de Gráficos 1 que sirve el servicio de imagen (pantalla, PNG, SVG, PDF, EMF,
  impresión, portapapeles y vista previa), con la definición y la teselación de referencia ya
  disponibles, conservando la naturaleza vectorial donde el formato la admite y respetando
  visibilidad, encuadre, escala, transformación de presentación y estilo. La referencia nunca
  es un `GeoElement` y la imagen de pantalla nunca es autoridad. Gráficos 2 queda diferido.
  Resuelve la pregunta abierta Q-DXF-3 del contrato en ese sentido.
- **R1-4 (interfaz):** la información habitual es compacta (nombre reconocible, naturaleza,
  visibilidad, operaciones, avisos breves); grado, nudos, pesos, dominio, transformación,
  procedencia, identificadores, hashes, rechazos y garantías pasan a un apartado de detalles.

`CHANGE_ROUTE = ORDINARY`. `VERIFICATION_CLASS = INTEGRATED_PHASE`.
`PRODUCT_PHASE_EFFECT = PRE-G9B-MAINT-A-DXF0-R1`. Nivel de aceptación: `STATIC` +
`PHASE PRE-G9B-MAINT-A-DXF0` (selección ampliada con R1) + `INTEGRATION`, más smoke focal del
autor. `FINAL = NOT AUTHORIZED`. `AUTHOR_DECISION = PENDING`.

## Implementation base
<!-- geocedg-field: implementation_base -->

- Repositorio `mpradovelasco/GeoCeDG`.
- Commit `177da00f6ca886a40b538713fafb49f01514992d` (candidato técnico `PRE-G9B-MAINT-A-DXF0`).
- Árbol `f2f078287623f2566002e2f9e35a89f2e0872727`.

Trabajo en el worktree `GeoCeDG-wt-maint-a-dxf0-r1`, rama local `phase/pre-g9b-maint-a-dxf0-r1`
creada desde el commit exacto; la rama `phase/pre-g9b-maint-a-dxf0` no se mueve y su worktree no
se toca. No se incorpora trabajo de B5a, A2 ni del ensayo de integración.

## Authority and evidence hierarchy

1. `AGENTS.md`, código, pruebas y contratos de serialización vigentes en la base.
2. La instrucción autoral de 2026-11-25 (§5) y el smoke autoral de `177da00f6`.
3. El contrato aprobado `dxf-spline-reference/v1` revisión 2
   (`geocedg/specs/import/dxf-spline-reference.md`), el prompt, el informe y la guía de smoke
   de A-DXF0.
4. Contrato de verificación vigente: `tools/agent/verify.ps1`, el registro tipado y sus
   esquemas, y `geocedg/specs/operations/verification-levels.md`.

## Allowed scope
<!-- geocedg-field: allowed_scope -->

- `apps/geocedg/application-profile.yml`: grupo `file-insert`, grupo `file-import-export`, la
  acción `import.dxf-reference` (madurez, notas) y textos localizados de la importación y de las
  denominaciones visibles de `export.dxf-2d`.
- Desktop: `GeoCeDGEuclidianView`, `AppGeoCeDG` (cableado del servicio de imagen),
  `GeoCeDGDxfReferenceDialogs`, `GeoCeDGDxfReferenceText`, `GeoCeDGDxfReferenceImport`,
  `GeoCeDGMenuBar` (texto de reserva de la acción DXF), una clase nueva de dibujo de referencias
  y, en `org.geocedg.desktop.export`, `ExportViewport`, `PictureExportService` y una interfaz
  nueva de superposición de presentación.
- `tools/agent/workspace-profile-validation.ps1` (madurez de la acción) y sus pines estáticos.
- Pruebas Desktop de A-DXF0 y de perfil que fijaban la conducta corregida, y pruebas nuevas R1.
- `docs/user/geocedg_user_guide_en.md`, `docs/user/geocedg_user_guide_es.md`, la matriz
  `geocedg/validation/pre-g9b-cu/guide-coverage-matrix.json` y `README.md`, solo en los
  nombres, la ubicación del menú y la descripción de la presentación.
- `geocedg/specs/import/dxf-spline-reference.md`: solo el registro de la resolución autoral de
  Q-DXF-3 (§10 y §17).
- Ampliación aditiva de la selección `PHASE PRE-G9B-MAINT-A-DXF0` con R1, el inventario JUnit y
  sus pines; `docs/upstream/modified-files.yml` para los archivos nuevos o modificados.
- Informe focal y guía breve de smoke R1 en `docs/validation/`.
- Este prompt, registrado en un commit preparatorio de gobernanza separado del commit técnico.

## Explicitly forbidden scope
<!-- geocedg-field: forbidden_scope -->

- `org.geocedg.common.importer.dxf` y `org.geocedg.common.math.bspline` (lectura, admisión,
  procedencia, teselación y constantes), salvo que una prueba demuestre un defecto; en tal caso
  se detiene.
- La exportación DXF semántica, sus algoritmos, su modelo de exportación, sus clases de
  fidelidad y sus garantías; las exportaciones LaTeX semánticas.
- `Construction`, el DAG, la identidad, el XML, los códecs y el Undo/Redo semántico; convertir
  la referencia en `GeoElement`, darle prueba de impacto, ajuste magnético o capa.
- Persistencia `R-c`, exportación DXF de referencias, A-DXF1, `R-d`, A-R y Gráficos 2.
- `CLAUDE.md`, `AGENTS.md`, otros prompts canónicos o de `ai-shell/prompts/`, el prompt
  histórico de A-DXF0, el roadmap canónico, `main`, etiquetas y releases.

## Architectural placement

Capa de aplicación Desktop y servicio de exportación de imagen (fuera del núcleo). La
superposición de referencias es presentación: lee el almacén de sesión `R-a` y dibuja la
teselación en unidades del modelo con la transformación de coordenadas de la vista exportada.

## Required design/specification

El contrato `dxf-spline-reference/v1` ya define la presentación (§10) y deja abierta Q-DXF-3 con
la propuesta que el autor adopta. Solo se registra esa resolución. Si la presentación exigiera
cambiar el lector, la teselación o el contrato de exportación DXF, se detiene.

## Geometric invariants and degeneracies

La referencia se dibuja desde su teselación única en unidades del modelo; zoom, DPI, tamaño de
letra y escala de salida solo cambian la proyección a la salida. Las referencias ocultas no se
dibujan. Una teselación incompleta conserva su marca visible.

## Compatibility and serialization

Sin cambios de serialización. Los documentos guardados no contienen referencias (`R-a`).

## Required tests and commands
<!-- geocedg-field: required_checks -->

- Menú: la acción al final de File → Insert, fuera del grupo de exportación, con sus nombres EN
  y ES; madurez formal y ausencia de bandera; denominaciones de la exportación DXF sin el
  calificativo.
- Presentación: referencias visibles en PNG, portapapeles, impresión, PDF, SVG y EMF de
  Gráficos 1; trayectoria vectorial en SVG con coordenadas iguales a la teselación; ocultas no
  dibujadas; independencia de zoom, escala de salida y tamaño de letra; Gráficos 2 sin
  referencias; DXF y LaTeX semánticos sin referencias; `Construction`, su XML y el historial de
  Undo intactos tras exportar.
- Interfaz: informe compacto con detalles separados; lista del gestor compacta.

Sobre el commit técnico congelado y limpio:

```powershell
git diff --check 177da00f6ca886a40b538713fafb49f01514992d HEAD
& tools/agent/verify.ps1 -Profile STATIC -LogDirectory <directorio-nuevo>
& tools/agent/verify.ps1 -Profile PHASE -Phase PRE-G9B-MAINT-A-DXF0 -LogDirectory <directorio-nuevo>
& tools/agent/verify.ps1 -Profile INTEGRATION -LogDirectory <directorio-nuevo>
```

Registrar comandos exactos, códigos de salida, identificadores, rutas de registro, cobertura y
diagnósticos. No ejecutar `FINAL`. No atribuir cobertura exhaustiva a `CORE + FOCUSED`.

## Authorization boundary
<!-- geocedg-field: authorization_boundary -->

Autorizado: R1-1 a R1-4, sus pruebas, el registro de este prompt, la ampliación de la fase
`PRE-G9B-MAINT-A-DXF0`, los commits locales y la verificación `STATIC`, `PHASE` e
`INTEGRATION`. No autoriza `R-c`, la exportación DXF de referencias, A-DXF1, otro incremento ni
`FINAL`. La aceptación técnica no es aprobación autoral.

## Publication boundary
<!-- geocedg-field: publication_boundary -->

Commits locales en `phase/pre-g9b-maint-a-dxf0-r1`. Sin push, promoción a `main`, etiquetas,
releases ni paquetes de publicación. El worktree se conserva para la revisión del autor.

## Acceptance and closeout

Estado final: `PRE-G9B-MAINT-A-DXF0-R1 — TECHNICAL CANDIDATE — PENDING AUTHOR REVIEW`, con smoke
focal del autor pendiente.

```text
TECHNICAL_CANDIDATE_STATE = FROZEN
AUTHOR_DECISION           = NOT_RECORDED_IN_THIS_ARTIFACT
```

## Required artifacts

Commit de gobernanza (este prompt) y commit técnico con inventario y registro; identificadores
de verificación; informe focal R1; guía breve de smoke R1; `GUIDE_IMPACT = UPDATE` (ubicación,
nombres y presentación de referencias); impacto de bootstrap: sin cambio.

## Stop conditions

- La presentación exige convertir la referencia en `GeoElement` o tocar `Construction`.
- Una salida gráfica solo puede obtenerse rasterizando la pantalla.
- La corrección exige cambiar el lector, la teselación o el contrato de exportación DXF.
- Gráficos 2 exige un aumento material de alcance.
- Un fallo de `INTEGRATION` atribuible al cambio.

## Deferred debts

Gráficos 2; persistencia `R-c`; exportación DXF de referencias; A-DXF1; reconciliación de la
madurez formal de `export.dxf-2d`, que esta corrección no cambia.
