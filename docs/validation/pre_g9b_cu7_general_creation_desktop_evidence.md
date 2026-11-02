# CU7 — Creación general: diagnóstico Desktop y plan de validación

Fecha: 2026-11-02, Europe/Madrid. Base inspeccionada: `eea7f57b8a7e8be9f5fe9356a9f7f989dd7fdf17`. Candidato de investigación; `selfApproved=false`. Lectura estática e inspección de evidencia histórica, sin código productivo, pruebas nuevas o manipulación de UI por este agente.

## 1. Dictamen Desktop

Las formas de autoría actuales no recorren el mismo protocolo completo:

| Entrada | Ruta actual | Garantía y límite |
| --- | --- | --- |
| Clic con Punto/Punto en objeto | `GeoCeDGEuclidianController.wrapMousePressed` → `GeoCeDGPointInteraction.create` → `LocusSnapshotPlacement2D` | Solo R6. Unique se confirma directamente; multiple requiere selector. Un resultado unresolved no se ofrece, aunque contenga una semilla local. |
| Herramienta L→Q | `GeoCeDGReferencePointTool` → `GeoCeDGReferencePlacement` → R1/R6 o R2/H1 | Prioridad histórica; solo el motivo tipado GLOBAL_UNIQUENESS_NOT_ESTABLISHED permite el fallback H1. Oferta H1 siempre confirmada y demostrada por H1. |
| Dirección explícita | Diálogo de dirección → `LocusV2PublicOperations.createSemanticPoint` | Construcción forward por rama/componente/u; no equivale a descubrimiento inverso. |
| Arrastre de P existente | `GeoCeDGPointInteraction.move` → `moveInteractiveSemanticPoint` | Sigue R6; no obtiene una admisión H1 o general por crear P desde una referencia. |

Anclas: `source/desktop/desktop/src/main/java/org/geocedg/desktop/GeoCeDGPointInteraction.java:30`–`:77`; `GeoCeDGReferencePlacement.java:55`–`:99`, `:124`–`:145`; `GeoCeDGEuclidianController.java:135`–`:147`, `:173`–`:255`, `:287`–`:300`, `:970`–`:978`; `GeoCeDGLocusV2Dialogs.java:200`–`:248`.

**Existe una ampliación de superficie plausible sin crear un certificado nuevo:** llevar una petición world de clic a `M08SnapshotPlacement2D.query(source,x,y,policy)`, que ya existe en el kernel (`:203`), y mantener exactamente la composición de prioridades y pruebas de la ruta Q. El método `queryFromReference` (`:210`) solo añade validación/liveness de Q; `referenceCurrent` (`:409`) acepta `reference==null`. No hace falta construir un Q auxiliar o un GeoPoint invisible para el clic. Esta posibilidad no decide por sí sola su autorización normativa y no da cobertura a los evaluadores sin la prueba H1 exigida.

El cambio no debe simplemente sustituir `GeoCeDGPointInteraction.create`: cambiaría la confirmación y el tipo de resultado que sus consumidores R6 ya esperan. Una ruta nueva puede conservar íntegro el comportamiento R6 y ofrecer el fallback autorizado mediante el mismo selector contextual. La composición de admisibilidad pertenece preferentemente al kernel; Desktop consume ruta, causas, evidencia, selección y commit, sin fabricar candidatos.

## 2. Selección gráfica, petición world e identidad

`GeoCeDGEuclidianController.java:213`–`:223` limita la elección del Locus al hit de su Drawable real. Después, `:228`–`:232` convierte el cursor a world y consulta el kernel. `interactionRadius` (`:481`) convierte el umbral de captura a world. Ese hit es una selección de fuente, no prueba de parámetro/componente ni autoridad geométrica. No extrae u de vértices o segmentos renderizados.

Consecuencias para un candidato:

- El clic produce coordenadas world transitorias. Los valores del cursor y de Q pueden compartir una petición inmutable y la misma política, manteniendo metadatos de autoría/liveness diferentes.
- La equivalencia semántica correcta compara direcciones elegibles, evidencia y commit para el **mismo** snapshot world/policy. No exige que dos gestos con radios distintos (radio pixel-convertido frente a radio world editable de Q) den el mismo resultado.
- Un evaluador forward finito puede no tener un stroke certificado renderizado/hitable. El test `G9U1SemanticPointInteractionTest.java:150`–`:174` recuerda la frontera del círculo escalar `(cos(u),sin(u))` frente al círculo trazado por punto. No debe suponerse cobertura de clic universal por la mera existencia de `Point(L,b,u)`.
- `GeoLocusV2` continúa sin implementar `Path` (`source/shared/common/src/main/java/org/geocedg/common/kernel/geos/GeoLocusV2.java:34`–`:36`). No arreglar la autoría convirtiéndolo a Path ni dejando actuar una creación heredada de punto libre después de un rechazo semántico.

La herramienta L→Q usa `GeoCeDGReferencePointTool.java:105`–`:127` para seleccionar L y luego un Q existente. Su `query` (`:139`–`:164`) ofrece direcciones explícitas y estado de cobertura; `commit` (`:166`–`:182`) hace un Undo y repaint. El selector pasa el handle efímero exacto, no su índice durable (`:268`–`:282`). El carrier conserva source ID/definition/revision y coordenadas actuales Q; commit rechaza stale/foreign/consumed y solo delega al commit de su ruta.

No se debe confundir creación con arrastre: el clic R6 actual deja `interactionDragPoint` y el drag del mismo gesto vuelve a R6.move. Una oferta H1 o general por clic necesita una frontera de confirmación/completitud del gesto que no presente el arrastre posterior como admitido. R6/SplineV2 deben conservar sus interacciones actuales; la nueva creación puede terminar el gesto y dejar el arrastre general no disponible bajo su causa actual.

## 3. Modelo autoral localizado y procedencia

Se localizó sin pedir una nueva copia:

```text
C:/DesarrolloyDatos/Areas/ProyectosNoFinanciados/CeDG/GeoCeDG/artifacts/pre-g9b-cu7-i1-r1/author-input/TestM08-S.cedg
SHA256 4f6e1be3c42a892f63f6791f3b8ca6994443863af5c5ad2039531604e27c898d
```

El hash coincide con el esperado por `geocedg/validation/pre-g9b-cu/m08s-r2-desktop-events-native-probe/org/geocedg/desktop/Cu7M08SDesktopEventsNativeProbeTest.java:26`–`:28` y con el launch context histórico. El archivo original no se editó ni se abrió en UI por este agente. Para pruebas futuras, copiarlo a un directorio de evidencia nuevo, comprobar hash original antes/después y no guardar sobre él.

El caso principal histórico usa h y `ReferenceR1=(0.8976484384297626,1.7854818601338847)`, radio world `.1`; selección de h cerca de `(.8,2.4)`. El fixture por comandos no se debe hacer pasar por el archivo autoral. `Cu7M08SDesktopEventsTest.general` (`:62`–`:68`) construye una familia correspondiente para regresión independiente. El modelo nativo no es por sí solo ejemplo de evaluador sin H1: se usó precisamente como caso positivo H1.

## 4. Pruebas existentes reutilizables

Las siguientes son **fuentes de pruebas inspeccionadas**, no resultados nuevos:

| Familia | Fuentes y cobertura existente |
| --- | --- |
| R6/SplineV2 Desktop | `G9U1SemanticPointInteractionTest`: línea/spline, self-intersection, cancelación/selector exacto, drag, dilatación negativa/colapso y hits de curva cerrada. |
| Q como valor transitorio | `Cu7M08SCorrectionTest`: referencias libres/dependientes, Q antecesor legítimo de L, propagación de L, pérdida/recuperación, copia/reopen `.cedg`, Undo/Redo, stale/cancel. |
| Límite de creación general R6 | `Cu7M08SGeneralLocusDiagnosticTest.generalCircleIsHitHasLocalCandidateAndExplicitForwardWorks`: hit gráfico y dirección forward válidos, semilla local, UNIQUE no ofrecido y XML intacto. No contradice H1 posterior; examina expresamente GeoCeDGPointInteraction/R6. |
| Composición R1/H1 | `Cu7M08SReferenceToolTest` y `Cu7M08SDesktopEventsTest`: caso general H1, prioridad R1, múltiples direcciones y confirmación, políticas/budgets/stale, foco/modo/teclado, un Undo. |
| Archivo nativo | `Cu7M08SDesktopEventsNativeProbeTest`: carga por hash, eventos del canvas, confirmación Swing, P sin Q padre, Q movido sin efecto extra, `.cedg` y remapeo clipboard. |

Niveles gráficos distintos, que hay que informar sin mezclarlos:

1. `Cu7M08SDesktopEventsTest.java:17`–`:43` envía eventos por el JPanel real al listener/controller y botones Swing. No invoca una consulta kernel como sustituto del clic, pero no muestra ventana ni entrada física Windows.
2. La sonda optativa `Cu7M08SDesktopEventsProbeTest` usa frame visible y dispatch AWT sintético. Su caso `characterizePhysicalDispatchOnRealProductFrame` abre el runtime y espera input externo, con observadores y archivos de señal. Es una caracterización histórica que no se debe relanzar sin adaptar paths/perímetro.
3. La comprobación Computer Use histórica del producto está en `artifacts/pre-g9b-cu7-i1-r2-desktop-events/visual-smoke-normal/`. Su `launch-context.json` liga `86438eda...`/`96e90172...`, preferencias aisladas, copia del modelo y `author_smoke=false`. Son evidencias de esa cohorte, no del candidato nuevo ni smoke autoral de este encargo.

El informe histórico registra la limitación de lanzamiento classpath con `--silent`; el lanzamiento normal por `runGeoCeDG` completó su comprobación. No extrapolar el fallo a packaging ni parchear logging para este trabajo.

## 5. Perímetro focal y plan de validación nuevo

Si el integrador determina que la extensión de clic al mismo H1 está normativamente cubierta, el conjunto mínimo afectado sería la orquestación de petición/selección compartida y sus consumidores Desktop, con pruebas concretas. No cambiar el significado de `UNIQUE`, `MULTIPLE`, `UNRESOLVED`, H1 ni añadir admisión por `isEstablished()`; no borrar tests negativos R6 para que una ruta nueva pase.

La campaña debe probar por separado:

| Caso nuevo | Oráculo necesario |
| --- | --- |
| Clic y Q, mismo world/policy/snapshot | Misma dirección semántica ofrecida y mismo forward; el contexto de autoría no es identidad. |
| h del modelo nativo | Archivo original por hash; clic sobre h y L→Q; permiso H1 real, confirmación antes P, sin Q auxiliar ni padre persistente. |
| Ejemplo sin certificado H1 | Aunque forward sea válido, la ruta H1 actual debe rechazar tipadamente; solo una norma nueva podría habilitar otra prueba. No ampliar por este test. |
| Multiplicidad/preimágenes coincidentes | Selección exacta explícita; ningún ordinal o nearest implícito, ningún auto-commit por único candidato descubierto si su ruta exige confirmar. |
| Componente/branch/gap/singularidad/seam | Identidad estructural y direcciones vigentes; fallo seguro al no probarlas, sin punto libre heredado. |
| Undefined, revisión, selección ajena/caducada y budget | Ninguna publicación/Undo extra, fuente/dirección/estado intactos; revalidación de Q cuando exista y del snapshot click cuando no exista. |
| Lifecycle | Q movido/eliminado no agrega dependencia; L mueve P/dependientes; `.cedg`, Undo/Redo, copia completa y IDs; arrastre general continúa fuera. |
| Interacción real | Producto visible nuevo con preferencias/copias propias: seleccionar herramienta, clic canvas y confirmar el panel, comprobar P/Undo/Redo y modelo, capturas/evidencia. Separar de dispatch AWT y de smoke autoral pendiente. |

Si el criterio sin C1 exige aprobación normativa, no hay implementación ni prueba positiva productiva de ese criterio en este encargo antes de decisión. La caracterización gráfica de la limitación vigente puede ejecutarse y guardarse con su resultado negativo, sin llamarla validación del nuevo contrato.

## 6. Wrappers y comandos reproducibles propuestos

Autoridad: `tools/agent/verify.ps1`, registry/esquemas y procedimiento JUnit. No inventar fase `CU7-GENERAL` registrada. La base contiene `PRE-G9B-CU7-I1-M08-S-R2-DESKTOP` y otras cohortes R1/kernel/H1; su selección solo cubre su inventario real. Una nueva clase de tests debe registrarse por el procedimiento canónico antes de atribuirle PHASE. DEV incompleto no es selección focal completa.

Desarrollo focal mediante productor existente, ejemplo que debe resolver paths a un directorio nuevo y a la fuente final autorizada:

```powershell
& tools/agent/checks/gradle-command-evidence-producer.ps1 `
  -EvidencePath <evidence>/process.json -GradleTask ':desktop:desktop:test' `
  -AdditionalArguments @('--tests','org.geocedg.desktop.Cu7M08SDesktopEventsTest') `
  -OutputDirectory <evidence>/process
```

El wrapper devuelve 0 como productor neutral: comprobar además `process.json.completion_state`, `inner_exit_code`, logs y JUnit. Para nativo, el init script existente añade fuentes optativas pero fija `C:/Temp/TestM08-S.cedg` en la prueba; no confundir que el fixture existe en artifacts con que ese path está preparado. Usar copia por hash y registrar la ruta, o adaptar un arnés aislado expresamente, sin tocar el original.

Lanzamiento del producto visible para Computer Use, con `--silent` omitido según la caracterización histórica, desde el worktree exacto:

```powershell
& tools/agent/checks/gradle-command-evidence-producer.ps1 `
  -EvidencePath <evidence>/process.json -GradleTask ':desktop:desktop:runGeoCeDG' `
  -AdditionalArguments @('--args=--enableLocusV2=true --language=en --settingsfile=<isolated-prefs> <copied-model>') `
  -OutputDirectory <evidence>/launch
```

El launcher histórico `artifacts/pre-g9b-cu7-i1-r2-desktop-events/launch-visual-smoke-normal.ps1` está ligado a su commit, archivos de señal y carpetas. Inspeccionarlo sirve de patrón; no reutilizarlo ciegamente, sobrescribir sus outputs ni falsear sus guardas. Abrir una ventana visible solo para la prueba interactiva autorizada; el shell auxiliar/Gradle puede permanecer oculto. Usar el skill computer-use y la API autorizada si se opera la ventana.

Después de trabajo focal autorizado: congelar candidato exacto y elegir perfiles según delta/contrato vigente, no cuatro campañas por inercia histórica. STATIC para entrega documental; PHASE registrada y campaña integrada requerida si hay producto, con cobertura real de nuevas pruebas y regresiones. Preservar fuente/test/JUnit/hash/exit; ninguna ejecución anterior se acredita como nuevo PASS.

## 7. Inventario y efectos

Inspeccionados: AGENTS; carrier/consumer/controller/dialogs/reference tool; M08SnapshotPlacement; GeoLocusV2; contrato M08 enmienda final; interacción; tests G9U1/R1/M08 y sondas events/native/controladas; registry/inventario de fases; wrapper gradle; informes/procesos/launch contexts/capturas listadas de Events; archivo autoral por hash. Varias búsquedas amplias se acotaron después; no se afirma lectura exhaustiva del registry o de todas las suites.

Escrito solo este anexo en staging ignorado. Sin cambios productivos, contratos, artefactos históricos, original nativo, `CLAUDE.md` o libro. Sin tests/GUI nuevos, por instrucción de lectura inicial. BOOTSTRAP IMPACT — NO CHANGE REQUIRED; VERIFICATION_INFRASTRUCTURE_IMPACT=NONE; GUIDE_IMPACT=NONE para este anexo. La futura creación visible exigiría documentar su alcance real, manteniendo CU10/E1 y deudas.
