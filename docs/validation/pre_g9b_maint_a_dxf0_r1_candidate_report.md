# PRE-G9B-MAINT-A-DXF0-R1 — informe del candidato técnico

**PRE-G9B-MAINT-A-DXF0-R1 — TECHNICAL CANDIDATE — PENDING AUTHOR REVIEW**

| Campo | Valor |
|---|---|
| Carácter | Informe técnico. `selfApproved=false`. Evidencia, no autoridad; no registra ninguna aprobación |
| Instrucción | «GeoCeDG — PRE-G9B-MAINT. Cierre de segunda ola, consolidación Git y planificación de interoperabilidad DXF ampliada», §5, del autor, 2026-11-25 |
| Prompt | [`pre-g9b-maint-a-dxf0-r1-focal-corrections`](../../.github/prompts/tasks/pre-g9b-maint-a-dxf0-r1-focal-corrections.prompt.md) (commit de gobernanza `751857c25`) |
| Base | candidato A-DXF0 `177da00f6ca886a40b538713fafb49f01514992d`, árbol `f2f078287623f2566002e2f9e35a89f2e0872727` (se conserva con sus verificaciones) |
| Rama / worktree | `phase/pre-g9b-maint-a-dxf0-r1` / `GeoCeDG-wt-maint-a-dxf0-r1` |
| Ruta y clase | `CHANGE_ROUTE = ORDINARY`; `VERIFICATION_CLASS = INTEGRATED_PHASE`; `STATIC` + `PHASE PRE-G9B-MAINT-A-DXF0` (selección ampliada) + `INTEGRATION`, `CORE + FOCUSED`; `FINAL = NOT AUTHORIZED` |
| Smoke | A-DXF0: `AUTHOR_SMOKE = PASS — AUTHOR REPORTED` (se conserva). R1: guía breve [pre_g9b_maint_a_dxf0_r1_author_smoke_guide.md](pre_g9b_maint_a_dxf0_r1_author_smoke_guide.md), pendiente del autor |

## 1. Cambios

| Id | Cambio | Archivos |
|---|---|---|
| R1-1 | La acción `import.dxf-reference` pasa del grupo `file-import-export` al final del grupo `file-insert` (File → Insert). Nombres: «Import DXF Reference…» / «Importar referencia DXF…». Acción, símbolo y conducta sin cambio | `apps/geocedg/application-profile.yml` |
| R1-2 | Madurez formal `experimental` → `stable`, sin bandera, acotada en las notas de la acción al contrato implementado (`R-a`; sin `R-c` ni A-DXF1). Las denominaciones visibles de `export.dxf-2d` (nombre, ayuda corta, estado, error, texto de reserva del menú) pierden «experimental»; su madurez formal, algoritmos, garantías y advertencias no cambian | perfil, `GeoCeDGMenuBar`, `tools/agent/workspace-profile-validation.ps1` (+ pines estáticos), guías, matriz de cobertura, `README.md` |
| R1-3 | Las referencias visibles se dibujan en todas las salidas del servicio de imagen de Gráficos 1 (PNG, portapapeles, impresión y vista previa, PDF, SVG, EMF) con la misma presentación que la vista en vivo | `GeoCeDGDxfReferencePainter` (nuevo), `ExportPresentationOverlay` (nuevo), `ExportViewport`, `PictureExportService`, `AppGeoCeDG`, `GeoCeDGEuclidianView` |
| R1-4 | Informe de importación breve (archivo, referencias por nombre «capa · identificador» y naturaleza, avisos en una línea) con **Detalles…** para el informe técnico; lista del gestor compacta; ficha con nombre, naturaleza y visibilidad antes del apartado «Detalles» | `GeoCeDGDxfReferenceText`, `GeoCeDGDxfReferenceDialogs`, `GeoCeDGDxfReferenceImport`, perfil |
| Q-DXF-3 | Registro de la resolución autoral de la pregunta abierta en el contrato (§10, §17); ninguna otra regla cambia | `geocedg/specs/import/dxf-spline-reference.md` |

## 2. Diseño de la presentación (R1-3)

- **Una sola presentación.** `GeoCeDGDxfReferencePainter` dibuja cada referencia visible como una
  trayectoria (`GGeneralPath`) desde su teselación única en unidades del modelo, con la
  transformación de coordenadas de la vista de destino. La vista en vivo y el servicio de imagen
  usan el mismo método; las anchuras de trazo y discontinuidad se multiplican por la escala de
  estilo física de la salida (1 en pantalla), como los demás objetos.
- **Superposición del servicio.** `ExportViewport.drawObjects` dibuja los objetos de la
  construcción y después la superposición, dentro del mismo recorte del área. La ruta SVG, que no
  usa `drawObjects`, escribe la superposición en un grupo propio `presentation`.
- **Alcance.** La superposición solo actúa si la vista de origen es Gráficos 1. Gráficos 2 queda
  diferido, como indicó el autor. Técnicamente bastaría ampliar el predicado y dibujar también en
  la vista en vivo de Gráficos 2 (que hoy no muestra referencias); no se hace sin decisión
  autoral (decisión pendiente).
- **Rutas del anfitrión sin cambio.** `exportPaint` de la propia vista en vivo (miniatura del
  documento, API de guiones) no dibuja referencias: el documento guardado no las contiene (`R-a`).
- **Autoridad.** Ninguna imagen de pantalla se usa; la referencia no es `GeoElement`, no tiene
  `Drawable`, capa, prueba de impacto ni entrada de Deshacer.

## 3. Pruebas

Nueva clase `PreG9BMaintADxf0R1DesktopTest` (6 pruebas):

| Prueba | Comprueba |
|---|---|
| `r11…` | la acción es la última de File → Insert y no está directamente en File; nombres EN/ES; sigue importando por el registro |
| `r12…` | madurez `stable`, sin bandera, notas acotadas; `export.dxf-2d` conserva `experimental` formal y ninguno de sus textos EN/ES dice «experimental»; texto de reserva del menú |
| `r13EveryPictureRoute…` | PNG, impresión, PDF, EMF, portapapeles y SVG contienen la referencia; PDF sin imágenes; en SVG, una trayectoria con un punto por punto de la teselación en su posición del área (tolerancia relativa 10⁻⁴); XML de la construcción e historial de Deshacer intactos; la referencia oculta no se dibuja |
| `r13PictureOutputsAreIndependent…` | dos zooms × dos tamaños de letra: SVG normalizado idéntico; PNG a escala de salida 1 y 2 con los puntos en su posición |
| `r13GraphicsTwoAndSemanticExports…` | la superposición no dibuja nada para Gráficos 2; DXF semántico y PGF/PSTricks/Asymptote sin referencias |
| `r14…` | informe breve sin datos técnicos y detalles con ellos; línea de lista y ficha compactas; EN y ES; los fallos explican la causa en el texto ordinario |

Pruebas existentes actualizadas por el cambio intencionado: `PreG9BMaintADxf0DesktopTest`
(puertos del presentador, nombre, la prueba de Q-DXF-3 pasa a exigir referencias en las rutas del
servicio y su ausencia en las semánticas y del anfitrión), `G9U1WorkspaceSurfaceTest` (entradas
directas de File), `PreG9BR6PlusF3SurfaceAndStylesTest` (contenido de Insert), `GeoCeDGProfileTest`
(texto de reserva DXF).

Selección `pre-g9b-maint-a-dxf0.desktop` ampliada con `PreG9BMaintADxf0R1DesktopTest`,
`PreG9BR6PlusBPictureFidelityTest`, `PreG9BR6PlusD1ExportRegressionTest`,
`PostE2P1R2PhysicalPdfStyleTest`, `PostE2P1R2BaseFingerprintTest` y
`PreG9BR6PlusF4DEmbeddedFontPdfTest` (regresión byte a byte de las rutas de imagen sin
referencias).

**Defecto encontrado y corregido antes de congelar.** La suite Desktop completa (diagnóstico,
Gradle directo, 2 692 pruebas) detectó que la primera versión escribía siempre un grupo SVG
`presentation` vacío, lo que cambiaba los bytes de las exportaciones SVG sin referencias
(`PostE2P1R2BaseFingerprintTest`, `PreG9BR6PlusF4DEmbeddedFontPdfTest`). La superposición declara
ahora si tiene contenido (`hasContent`) y el grupo solo se escribe en ese caso; ambas clases
pasan. Los otros dos fallos de esa ejecución son ajenos a R1: el histórico
`PreG9BCu2AuthorModelConsumerTest.theCertificateIsTheSingleClosureAuthority` (desde `9ccefdcf`) y
`G9U1Revision3AlgebraEditingDiagnostic`, que exige el archivo autoral ignorado
`artifacts/smoke-test-g9u1/Revision3.cedg`, ausente en un worktree nuevo y fuera de
`final.desktop`.

## 4. Verificación

**Evidencia de inventario** (productor oficial `tools/agent/checks/gradle-test-evidence-producer.ps1`
sobre el commit técnico `902f8d4f9`, actualizador `tools/agent/update-verification-junit-inventory.ps1`;
evidencia en `artifacts/agent/pre-g9b-maint-a-dxf0-r1-inventory-902f8d4f/` del worktree):

| Selección | Resultado | Identidades |
|---|---|---|
| `discovery.shared` (`--test-dry-run`) | completada, salida 0 | sin cambio |
| `discovery.desktop` (`--test-dry-run`) | completada, salida 0 | 2 634 → 2 640 |
| `pre-g9b-maint-a-dxf0.desktop` (ejecutada, 25 clases) | completada, salida 0, 0 fallos | 274 → 313 |

`final.shared` no cambia (R1 no toca pruebas compartidas). `final.desktop` conserva su huella
anterior: la suite Desktop completa contiene el fallo histórico CU2 y el actualizador rechaza
evidencia con fallos (situación previa a la ola, como en A-DXF0). Pin `junit_inventory` del
registro actualizado; recuentos globales de selecciones (102) y fases (91) sin cambio.

Las ejecuciones de aceptación `STATIC`, `PHASE PRE-G9B-MAINT-A-DXF0` e `INTEGRATION` sobre el
candidato congelado se registran en el informe consolidado de la sesión (este documento queda
congelado en el commit candidato).

## 5. Desviaciones y observaciones

- **Contrato:** se registra la resolución de Q-DXF-3 en §10 y §17 de `dxf-spline-reference.md`;
  es la decisión del autor, no una regla nueva del agente.
- **Madurez de la exportación DXF:** sigue `experimental` en el perfil con su bandera
  `cedg.export.dxf.2d`; solo cambian sus denominaciones visibles. Reconciliarla es una decisión
  pendiente.
- **Naturaleza `RATIONAL_EQUAL_WEIGHTS`:** en la interfaz se rotula «Spline de pesos iguales»
  (la curva es polinómica); el identificador del contrato sigue en los detalles.
- **Nombre reconocible:** capa e identificador DXF tal como se leen; nunca es identidad
  (contrato §9).

## 6. Fuera de alcance

Persistencia `R-c`, exportación DXF de referencias, A-DXF1, Gráficos 2, A-R.
