# GeoCeDG 1.2.0 — Diseño correctivo R12

**AUTHOR AUTHORIZED — IMPLEMENTED AND INDEPENDENTLY REVIEWED — FOCAL FAILED.**
El [resultado terminal R12](../validation/geocedg_1_2_0_r12_blocker_report.md)
registra 253 casos, 247 aprobados y seis fallidos. El límite de doce rondas
está consumido; no se ha aplicado otra corrección ni repetido el foco.

El autor autorizó expresamente R12 para corregir los bloqueos del catálogo,
los defaults y la recuperación de imágenes, y continuar después con las
verificaciones globales y el empaquetado. Se autoriza un ciclo adicional;
el límite queda en doce rondas. No autoriza otra corrección por inferencia,
aceptación del candidato, promoción a main, tag o publicación.

Clasificación: depuración e implementación. Capa: perfil y Desktop, incluido
el adaptador de Undo. No cambia la autoridad geométrica, el kernel, R10, I2,
codecs, formatos ni el lector nativo del portapapeles. Se conservan D1–D4 y
la decisión autoral de rotación matemática. Este diseño de implementación no
sustituye los contratos normativos existentes.

La [ejecución R11 y su bloqueo](../validation/geocedg_1_2_0_r11_blocker_report.md)
permanecen como evidencia histórica fallida: 216 casos, 118 aprobados y 98
fallidos, sin errores u omisiones. El trabajo parte de las fuentes de desarrollo
sin commit sobre `6f1d357ca2224dedd0e84fda0faed2d600154bab`, árbol
`b6708de39577009752d45e0b73e9d44d94d695f9`, candidato anterior retirado.
El original del cono se conserva; se usan copias y sus hashes.

Se transcribe el diseño técnico acordado antes de código desde el borrador
externo revisable de SHA-256
`3c54eba735ff8f498e5f390e12ce7e78ed20d4c178613459e431c960b0bc2ba7`.
La revisión independiente contrastó el diseño con contratos, fuentes y oráculos
antes de implementar, sin bloqueos abiertos, en SHA-256
`b7d58247c1a06026967977aadc8aeffbe1e3523e53cbda465e66304424165504`.
El informe externo `r12-design-independent-review.md` tiene SHA-256
`fe6af0515969ae556786e0ebf9f6b4a2f0f222b9d61949f3cd434cb09c09d617`.
Esto aprueba únicamente la preparación de implementación: el parche y los
oráculos completos requieren otra revisión antes del foco. No se declara
compilación, prueba, candidato o empaquetado aprobados.

## Causal scope and smallest product set

1. `apps/geocedg/application-profile.yml`: add the existing image action to `clusters.document-lifecycle.menu_action_ids`; leave the 137 action catalog and all other placements intact.
2. `GeoCeDGDocumentInsertion.java`: image-only authenticated baseline preflight; passive defaults snapshot consumed by image journal; use the existing full native reader for a full native snapshot.
3. `GeoCeDGClipboardImageInsertion.java`: passive defaults freshness in the request spanning its modal dialog; no acquisition before acceptance.
4. `GeoCeDGUndoManager.java`: a package-private nested defaults preservation helper plus a scope around ordinary `readUndoState`. This is the seventh cumulative product source, needed because ordinary Redo constructs images after the clipboard transaction has ended. It adds no metadata to Undo commands and does not change companion authorization.

The remaining three previously edited feature sources stay unchanged. A seventh cumulative source is justified by the causally separate replay lifetime, rather than changing the kernel getter or attaching an ad hoc image flag to historical commands.

## Passive defaults snapshot

Proposed internal API: `GeoCeDGUndoManager.LazyDefaults.capture(Construction)`, `restoreConstruction()`, `restoreArchive()`. A snapshot owns its original Construction reference and a copied map from default type to exact prototype reference, raw nullable `NumberValue`, immutable numeric bits/defined state for admitted constants, and the serialized defaults XML/V2 defaults. It reads `getAnimationStepObject()`, never the mutating `getAnimationStep()`; no source prototypes are warmed before the dialog, on Cancel or before baseline capture. Capturing the value separately is required because a constant MyDouble can be edited in place while retaining its reference and an XML-normalized representation.

Capture all existing default entries, not only DEFAULT_IMAGE: first image corners exercise DEFAULT_POINT_FREE, and Undo replays an arbitrary pre-existing construction. ConstructionDefaults has an existing getDefaultGeos() map view; copy it, do not retain mutable entries. getDefaultsXML and getV2DefaultsXML provide the existing native serialization authority.

`restoreConstruction()` prevalidates the whole snapshot before its first setter: same current type set and exact prototypes; non-null raw values are never written or rebound; the strict IMAGE snapshot also requires them to remain the identical objects; each originally-null raw must still be null or exactly an actual MyDouble containing GeoNumeric.DEFAULT_SLIDER_INCREMENT (0.1). Only the demonstrated lazy initialization is admissible. Restore the eligible fields with `setAnimationStep((NumberValue)null)`, then require exact defaults XML/V2 equality. An unrecognized change throws; it is not converted into an accepted default modification. No tolerance, prototype replacement, label identity or old GeoNumeric installation is permitted.

`restoreArchive()` is only called immediately after reading this journal's already verified full native archive, which reconstructs the creation prototypes. Check same types/classes; do not require old prototype or raw references. For an originally-null raw, the native defaults writer encoded literal 1, and processAnimationStepList evaluates that literal to a new GeoNumeric. Accept only an unlabelled independent defined owned exact GeoNumeric with value exactly 1 (or a literal MyDouble 1 if that native representation is encountered), then restore null on the actual current prototype. If the native reader failed before applying defaults, null or the same demonstrated lazy MyDouble(0.1) can remain; those two states also admit this limited repair. That repair never converts a failed reader into success: preserve its cause and suppress cleanup failures; bounded reload and the final complete certificate decide recovery. Originally non-null raw values are not overwritten. Require complete defaults XML/V2 equality afterwards. Original semantic GeoNumeric references are never installed after reconstruction. The transaction reports SESSION_RESET for reader/rebuild outcomes; this path makes no old-reference promise.

If native loading changes another default property, the existing whole-default/resource certificate still fails. The scope does not relax digest comparison, omit defaults or normalize arbitrary differences.

## Lifetimes and ordering

- ClipboardState captures the passive helper after explicit acceptance, before its resource archive/baseline capture.
- After native loadImage returns, restore construction defaults before image/resource verification and before own Undo capture. Recheck before publication.
- If loader/fault/NativeAtomic rebuild fails, restore construction defaults before the recovery certificate. If an explicit native full archive was read, use restoreArchive instead. The restore executes after each attempted reader, including failure, with secondary errors suppressed on the owning failure.
- GeoCeDGUndoManager.readUndoState wraps both ordinary and native-companion paths in a narrow passive lazy-null scope. That replay scope only captures originally-null fields, restores their demonstrated lazy changes and never inspects/rebinds non-null dynamic references or certifies them. Restore before the reader returns, including exception exits, so native Redo cannot leave null→0.1 behind. Existing private companion validation, typed failure, cursor recovery, listener policy and runtime/native-state restoration remain intact.
- A normal XML-only Undo reader does not reset ConstructionDefaults; restoration of a replaced prototype there is rejected. Full FILE loading is not globally wrapped and legitimate document defaults remain unchanged.

## Authenticated image baseline and correct recovery representation

Keep the existing generic barrier unchanged by default. Add an image-only validation branch/overload. Its single real package-private boundary is `preflightClipboardUndo(owner, originalCommand, archive)`, returning boolean fail-closed. The owner must be this application's actual construction Undo manager and the original command must be present by identity in its current history. This also rejects a foreign ordinary FileAppState that has no native companion. For the ORIGINAL temporary FileAppState archive require exactly one member named geogebra.xml; drain it, validate CRC and declared expanded size and reject extra/duplicate entries. Obtain the raw XML using a strict UTF-8 decoder. Call `undo.publicUndoXml(originalCommand, rawXml)`; an owned NativeFileAppState checks owner/document/template/private digest and original public digest. Repackage only that authenticated public projection with the existing MyXMLioJre.writeZipped and apply normal FILE preflight for structural admission. Never rewrite the original AppState, discard its companion or submit private bindings to FILE. The public ZIP is disposable evidence only. Tests use this same pure product boundary, not a public test seam or an alternative admission path.

For explicit recovery without a native companion, replace the incompatible readZipFromMemory(fullArchive) call with existing readNativeZipFromInputStream(fullArchive). That reader reads resources/macros/defaults as well as the construction and admits native numeric layers/Control Layout through the existing route. The archive is the unchanged verified P7 snapshot. With a native checkpoint, retain restoreNativeUndoSnapshot(template, processConstructionSnapshot(privateXML)) and its owner authority; do not feed private XML to FILE or replace its companion with a public projection. Existing attempts/backstops remain bounded; no new fallback is introduced.

## Required oracles before candidate

Preserve the 44 R11 cases. Add first REAL native clipboard image with no prior image in two actual hosts: blank document and ABC fixture. Capture passive raw values and archive members before invoking the operation, no warm getter. Exercise natural null and custom non-null MyDouble where appropriate; success, one Undo and one Redo preserve exact raw null/refs for same-prototype XML readers and exact defaults/resource bytes. Keep the reserved thumbnail filename case.

The Point(path)+Control Layout test must reach both normal rollback and forced explicit reload through the authenticated baseline; assert hooks reached, original companion authority preserved, private tags absent from saved FILE, session reset declared and runtime references re-queried. A full native reload may replace prototype and NumberValue references; assert coherent current owners, raw null/value and complete serialized defaults, not old instances.

Negatives: unknown default raw mutation refuses certificate; malformed/tampered or foreign companion cannot acquire FILE authority; extra Undo ZIP members/CRC failure rejected before detach publication. Existing late store/attach, recovery failure and serious isolation cases stay intact. Root will choose the narrow canonical runtime selection after independent review; no pass is claimed here.

## Evidence/limits

AGENTS, R11 blocker, ConstructionDefaults, GeoElement, ConsElementXMLHandler, MyXMLioJre, UndoManagerD, GeoCeDGUndoManager, GeoCeDGDocumentInsertion and document insertion contract were read. R11 remains failed (216/118 pass/98 fail; image 44/22 pass/22 fail). This proposal contains no test execution, no product edit and no final candidate or packaging claim. The source-derived step1→0.1 explanation still needs the new saved before/after oracle; it was not logged as a member diff by R11.

## Root-concurred admission clarification (before implementation)

IMAGE admission is a pure separate predicate, evaluated before presentation and again after the dialog and at barrier freshness: null and exact MyDouble are admitted; an exact GeoNumeric is admitted only if independent, unlabelled and absent from the live Construction geoSet. This is the constant literal representation emitted by parsing defaults. A labelled/dependent/DAG-member GeoNumeric or another NumberValue class is rejected with a typed image reason BEFORE mutation. Ownership by the same Construction alone does not establish liveness. There is no existing authenticated semantic remap for such dynamic defaults and none is added here.

The request captures strict passive defaults freshness before the modal dialog and compares raw identities, prototype map and full defaults XML at confirmation/barrier. Thus a default changed without a Kernel event also invalidates the request; the dialog's external edit is preserved. No lazy getter is used by this preflight or freshness check. Snapshot acquisition itself never repairs defaults.

Ordinary Undo of unrelated historical documents is not newly invalidated because a non-null dynamic default exists. The reader scope only preserves previously-null lazy fields; it neither modifies nor makes a new preservation claim about historical dynamic defaults. Strict IMAGE validation covers both its initial transaction and its admissible preconditions, and never re-installs an old semantic GeoNumeric after reconstruction. If the user changes a default dynamically outside this feature later, its historical Undo semantics remain the existing contract; this correction does not implement general dynamic-default remapping.

Additional negative oracle agreed with root: a live labelled GeoNumeric installed as a prototype animation step causes typed IMAGE rejection before presentation/loader/history; installing it during the modal dialog (main dispatcher enabled or disabled) causes rejection before loader and preserves the edit. The source set is four modified files / seven cumulative product paths, no new class file or kernel API.

## Interfaces y verificación acordadas antes de código

Las interfaces package-private del helper son `LazyDefaults.capture(Construction)`,
`matchesCurrent()`, `restoreConstruction()` y `restoreArchive()`. El predicado
pasivo `unsupportedImageStep(Construction)` produce la causa tipada
`DYNAMIC_ANIMATION_STEP`. El scope de lectura de Undo captura exclusivamente los
campos originalmente nulos (`captureLazyNulls`), sin introducir nuevos rechazos
para defaults dinámicos históricos ni reinstalar sus referencias. La admisión
estricta de la nueva imagen sigue separada de esa conservación acotada.

El diseño base de oráculos Desktop, SHA-256
`d8634713c39dccadda49bcae9d7fa0bfd3df85cedda3f97fb26e7d9a6e3d4dc1`,
se completa aquí para conservar los 44 casos anteriores y añadir doce
invocaciones: dos
primeras adquisiciones Windows reales (host vacío y A/B/C sin imagen), una
recarga forzada con companion nativo y dos negativos del default ligado al DAG
(antes de mostrar el diálogo y durante el bucle modal), cinco negativos de
autenticación de baseline, uno de mutación inesperada del default y uno de
edición modal del mismo objeto MyDouble sin notificación. El positivo
del literal FILE se integra en la reapertura de los casos reales. El total
definido es 56 (13 en inserción y 43 en recuperación); los resultados e
identidades se recogerán de los XML reales,
sin inventar identidades derivadas.

Los cinco negativos usan baselines REALES: control positivo del comando
owned válido y después rechazo de miembros ZIP extra con XML intacto, CRC
alterado conservando los bytes XML, XML alterado con CRC válido y comando de
otro dueño con su archivo propio válido, tanto NativeFileAppState como
FileAppState ordinario. Se modifican copias de los bytes, no
el archivo del historial; se comprueba la pureza antes/después. Así se ejercen
forma, integridad y autenticación del nuevo validador, además de las regresiones
existentes del lector de Undo.

El control de edición modal cambia el MyDouble original de `+0` a `-0` sin
reemplazar la referencia ni emitir UPDATE. La petición debe caducar por los
bits capturados, conservar la edición externa y rechazar antes del loader y
de la barrera; no basta comparar la representación XML normalizada.

El negativo de default establece `MyDouble(0.2)` desde `afterCommit`, sin lanzar
una excepción de test. Debe alcanzar el rechazo tipado
`UNEXPECTED_LAZY_DEFAULT_CHANGE`, la recarga nativa completa y la recuperación
certificada con `SESSION_RESET`, conservando los defaults y el historial del
preestado. Los cambios de prototipos y otras diferencias serializadas tienen
causas separadas `DEFAULT_PROTOTYPES_CHANGED` y `DEFAULTS_SERIALIZATION_CHANGED`.
Se exigen los contadores de hooks alcanzados; una inadmisión anterior no cuenta
como prueba de esa recuperación.

La admisión pasiva comprueba propietarios y pertenencia al DAG por identidad
en todo el geoSet y la clausura de los ConstructionElement, sus entradas,
salidas y miembros de listas, incluido el sufijo inactivo. No usa lookupLabel,
el cursor del protocolo ni `getAlgorithmList()`, que inicializa una lista.
`MyDouble.wrap().getKernel()` permite comprobar su propietario creando solo
una expresión local sin evaluar ni modificar el valor fuente.

Se mantienen los dieciséis filtros de R11 y se añaden tres clases existentes
por el cambio en el lector de Undo: `R3NativeUndoLifecycleDesktopTest`,
`R3NativeUndoRecoveryDesktopTest` y `PreG9BR6PlusF3UndoAccountingTest`.
Se incluye además el control focal
`R4NativeBudgetDesktopTest.exhaustedCurrentHostCannotAdvanceUndoOrRedoCursorAndANewPassRecovers`.
La selección canónica de desarrollo tendrá veinte filtros, con las cuatro
clases del cono/vistas R10 ya presentes. No duplica esos casos ni sustituye
PHASE, INTEGRATION o FINAL.

Antes del foco se revisarán independientemente las cuatro fuentes cambiadas,
los dos tests y los cierres completos del perfil: 137 acciones únicas, cluster,
presentación, menú, toolbar, políticas, localización y binding productivo. No
basta comprobar el recuento ni un subconjunto de la proyección.

`GUIDE_IMPACT = UPDATED`: arquitectura y estado de ejecución ahora; guías ES/EN,
cobertura y guion autoral después de verificar el comportamiento real.
`BOOTSTRAP_IMPACT = NONE`: no cambian dependencias, runtimes, JDK o codecs.
El conjunto GeoCeDG 1.2.0 mantiene su nivel `FINAL` por los cambios anteriores de
kernel, persistencia y DAG. Tras resolver R12 se registrará un nuevo checkpoint
limpio, se derivarán los inventarios oficiales y se ejecutarán los perfiles
globales y de distribución sobre el árbol definitivo. Los artefactos de los
candidatos retirados conservan únicamente su valor histórico.
