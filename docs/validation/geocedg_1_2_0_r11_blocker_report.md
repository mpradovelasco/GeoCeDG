# GeoCeDG 1.2.0 — Bloqueo del cierre tras R11

**TECHNICAL CLOSEOUT BLOCKED — CORRECTION BUDGET EXHAUSTED (11/11).**
Este informe no califica un candidato ni sustituye la aceptación autoral.
La ronda adicional autorizada mediante «Autorizo una ronda de corrección
adicional» se aplicó al perímetro R11 revisado. Su foco terminó con fallos.
No se ha aplicado otra corrección ni repetido la campaña.

## Resultado y procedencia

La ejecución canónica fue:

```powershell
pwsh -NoLogo -NoProfile -File tools/agent/checks/gradle-test-evidence-producer.ps1 -RepositoryRoot . -InventoryPath artifacts/agent/geocedg-1.2.0/clipboard-image-focused-inventory.json -SelectionId clipboard.initial -EvidencePath artifacts/agent/geocedg-1.2.0/r11-focal-desktop-01/evidence.json -OutputDirectory artifacts/agent/geocedg-1.2.0/r11-focal-desktop-01
```

El productor neutral terminó con código **0** y conservó el código **1** del
proceso Gradle: ese cero externo no significa aprobación. La ejecución terminó
naturalmente, en 266 349.6758 ms, con dieciséis XML: **216 casos, 118 aprobados,
98 fallidos, cero errores y cero omisiones**. No hubo interrupción ni timeout.
Las dos clases nuevas contienen 44 casos, 22 aprobados y 22 fallidos. Las cuatro
clases seleccionadas del cono y de vistas R10 aprobaron sus 14 casos.

Las 67 fuentes del input conservaron sus hashes durante la ejecución, confirmado
también de forma independiente antes de las actualizaciones documentales de
este informe. El input sigue siendo desarrollo sin commit sobre el candidato
retirado `6f1d357ca2224dedd0e84fda0faed2d600154bab`, árbol
`b6708de39577009752d45e0b73e9d44d94d695f9`; no es un nuevo candidato exacto.

| Evidencia | SHA-256 |
|---|---|
| [Input de fuentes](../../artifacts/agent/geocedg-1.2.0/r11-focal-desktop-01/source-before-runtime.json) | `66527f4aa492437ed8a776de6e0760fd9be585ad28d479e02f3938116522f27e` |
| [Productor](../../artifacts/agent/geocedg-1.2.0/r11-focal-desktop-01/evidence.json) | `1659307d3214e820b0461da8e4543c2788c6a418aa42ef63751724cba5554e19` |
| [Recuento y disposición](../../artifacts/agent/geocedg-1.2.0/r11-focal-desktop-01/diagnostic-disposition.json) | `5befae004fae54c9203074e88262431c6cf6834778dbc1b732ef9ad6491b0352` |

Los logs completos y XML están junto a esas evidencias. La revisión terminal
independiente está en el expediente externo
`geocedg-120-clipboard-image-design/r11-result-independent-review.md`, SHA-256
`df15b8ff42e720daa6ba36dfe50ca3274edfad360b96ef26d748340aaf968133`.
El primer foco inicial fallido permanece separado y sin reescribir.

## Bloqueos identificados

| Bloqueo | Hecho y efecto | Estado |
|---|---|---|
| Perfil incompleto | El guard acepta ahora 137 acciones, pero `document.insert-clipboard-image` falta en `clusters:document-lifecycle.menu_action_ids`. Sí figura en `presentation_groups:file-insert`. `validatePlacements` exige igualdad con todo el catálogo y activa el fallback v1 al rechazar la omisión. De ahí proceden las ausencias de menús, acciones y textos de v2 observadas. | Causa productiva confirmada por lectura independiente. La revisión estática volvió a omitir este cierre del conjunto; su ACK se ha retirado íntegramente. |
| Integridad de recursos | Ocho casos de inserción y varias recuperaciones con loaders simulados notifican `Clipboard insertion changed an existing resource` o un certificado de recuperación no satisfecho. Dos casos reales con imagen previa sí pasan; eso no demuestra la primera inserción general ni la ruta pública del menú. | La fuente deriva una mutación del prototipo de imagen que cambia `geogebra_defaults2d.xml`, explicada a continuación. Los bytes antes/después no fueron registrados por el throw; no se afirma haber observado ese diff en el run. |
| Lector de recuperación incompatible | La rama sin companion entrega `barrier.snapshot.getArchive()`, un ZIP nativo completo, a `readZipFromMemory`. Ese lector exige el XML como primer miembro; el writer nativo escribe imágenes o JavaScript/defaults antes. Las cadenas conservan `geogebra.xml not found`. | Incompatibilidad de lector y representación confirmada por fuente y stack. Ese mensaje no demuestra ausencia o corrupción del XML del archivo nativo. |
| Baseline privado y preflight FILE | El caso con Point(path) nativo y Control Layout se rechaza antes del loader: el baseline temporal contiene bindings MEMORY y su preflight FILE descartable devuelve `Private native binding outside its rollback session`. | No se alcanzó la inyección de recuperación prevista. Debe conservarse la autoridad del companion; no se autoriza eliminar tags o relajar el preflight por conveniencia. |

No se atribuyen los 98 fallos a una única causa. Los nuevos diagnósticos
requieren corrección y verificación propias; no se confunden con una incidencia
de entorno. La corrección del arnés permitió que los resultados serios
retornasen a sus aserciones con causa y suppressed completos; no convirtió
ningún fallo en aprobación.

En los fixtures sin imagen previa, los miembros anteriores emitidos por el
writer son metadatos nativos, no recursos PNG. La revisión posterior deriva de
fuente una causa suficiente: `DEFAULT_IMAGE` comienza con
`animationIncrement=null`; su XML escribe `animation@step="1"`. El primer
`GeoImage` copia sus propiedades mediante `setBasicVisualStyle`, que llama a
`getAnimationStep()` del prototipo. Ese getter inicializa el campo con
`DEFAULT_SLIDER_INCREMENT`, cuyo valor es `0.1`; el siguiente
`geogebra_defaults2d.xml` escribe `animation@step="0.1"`. El constructor ha
mutado un default anterior y la restauración de la construcción no restaura
ese prototipo. Los casos detenidos antes de construir GeoImage y los dos casos
reales con imagen previa son coherentes con esta derivación.

Es una derivación de la ruta determinista del código, contrastada con los
oráculos diferenciales, no un diff de bytes capturado durante la ejecución.
El throw actual no registra el miembro ni sus bytes antes/después. La
normalización de `defined` sugerida inicialmente se descarta como explicación
no demostrada. Antes de corregir conviene observar explícitamente ese miembro
y la primera adquisición real en un documento sin imagen previa.
Las inyecciones de store/attach o recuperación que fueron detenidas por una
comprobación anterior no se consideran ejercidas.

El diagnóstico Desktop conserva las 22 cadenas de fallo completas en el
expediente externo
`geocedg-120-clipboard-image-design/r11-runtime-diagnosis-desktop/diagnosis.md`,
SHA-256 `c0fb04414d509cdbee3b33b9adb07675a970ee9c40e466f2cb80331fcdeb3972`.

## Perímetro conservado y deuda

R11 modificó cinco fuentes productivas: perfil, `GeoCeDGProfile`, `AppGeoCeDG`,
`GeoCeDGClipboardImageInsertion` y la rama de imágenes de
`GeoCeDGDocumentInsertion`. `GeoCeDGActionRegistry` conserva su cambio inicial;
el perímetro acumulado es de seis fuentes. Se modificaron los dos tests de
imágenes. No se tocaron kernel, R10, I2, codecs ni el lector nativo del
portapapeles. Se conservan D1–D4, formatos e identidad geométrica.

Los cambios documentales registran autorización, resultados y bloqueo. Los
filtros PHASE de 1.2.0 incluyen ya las doce clases Shared y trece Desktop; sus
identidades derivadas siguen pendientes del productor y actualizador canónicos
sobre un checkpoint limpio. No se han inventado recuentos ni hashes finales.

```text
GUIDE_IMPACT = UPDATED
GUIDE_PATHS = docs/architecture/geocedg-1.2.0-clipboard-image-insertion-design.md; docs/architecture/geocedg-1.2.0-clipboard-image-correction-proposal.md; docs/validation/geocedg_1_2_0_validation_dossier.md; docs/validation/geocedg_1_2_0_author_smoke.md; docs/validation/geocedg_1_2_0_editorial_handoff.md; docs/validation/geocedg_1_2_0_r11_blocker_report.md
```

Las guías ES/EN de la nueva imagen siguen pendientes como capacidad verificada.
`BOOTSTRAP_IMPACT = NONE` e `INFRASTRUCTURE_IMPACT = NONE` en R11: no cambia
dependencias, JDK, codecs ni requisitos de estación. El conjunto 1.2.0 mantiene
su obligación de `FINAL` por los cambios de kernel, persistencia y DAG anteriores.

La inspección de causas contrastó además `GeoCeDGProfile`, `MyXMLioJre`,
`GeoElement`, `GeoImage`, `ConstructionDefaults`, `GeoCeDGUndoManager`, los
contratos de inserción y recuperación y los dieciséis XML de pruebas. El último
`git -c core.whitespace=cr-at-eol diff --check` terminó con código 0. Ese control
de formato no cambia el resultado fallido del foco.

El original `C:\Temp\InterCilConoOblique_2.cedg` conserva SHA-256
`4cde946eba57bfd36d531c677dc9f622b9036d6e95cd92941311b51d5f786a01`.
El worktree de desarrollo permanece con cambios; no se presenta como limpio ni
como candidato aceptable. No hubo promoción a main, publicación, tag o Release.

Quedan pendientes Computer Use de la imagen, guías ES/EN y cobertura de esta
capacidad como comportamiento verificado, checkpoint final e inventarios,
STATIC/INFRA_UNIT/WORKSTATION, PHASE afectados, INTEGRATION, FINAL, medición F5
del candidato exacto y generación/verificación de sus paquetes INTERNAL y NC.
La fecha autoral de versión está disponible: **2026-11-16**. El empaquetado está
detenido por el bloqueo técnico, no por metadatos autorales ausentes. Los
instaladores del candidato retirado siguen siendo evidencia histórica.

El [guion autoral](geocedg_1_2_0_author_smoke.md) sigue siendo un borrador y el
[expediente editorial](geocedg_1_2_0_editorial_handoff.md) no promueve el
manuscrito ni el baseline del libro. El cierre técnico requiere una nueva
disposición autoral antes de aplicar más correcciones. No se presupone otra
ronda a partir de la autorización funcional de imágenes.
