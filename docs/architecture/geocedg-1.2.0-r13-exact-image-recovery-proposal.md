# Proposed R13 — exact library JavaScript during image recovery

**DESIGN ONLY — AUTHOR AUTHORIZATION PENDING.** R12 has consumed the authorized twelfth round and ended with failures. No product/test correction, rerun, JVM, Git or CUA has been performed for this proposal. It does not qualify a candidate or reopen unrelated semantics. The design is limited to the observed raw-JavaScript member mismatch and the two separately diagnosed test operational probes.

## Observed cause and retained acceptance

The saved R12 unknown-default before/returned archives contain four identical member names. Construction XML and both default XML members are byte-identical; only geogebra_javascript.js changes from23 to24 bytes. Its exact bytes are `function ggbOnInit() {}` before, and that string plus LF afterwards. SHA256 changes from3aa5d8316bbad619954d1c93da49792492f64eaed2efe3aada9985cce9c75bf2 to90d66739c7658fadada2635213419190cd92f745f4950b71c6573970a8bd1c98. The R12 raw-default evidence also confirms restoration of nulls and non-null values/defined bits.

MyXMLioJre217 reads the member through StreamUtil.loadIntoString, which uses readLine and appends LF after each line; MyXMLioJre245–246 sets the resulting library. Its writer447–448 emits Kernel.getLibraryJavaScript verbatim. This is an exact resource difference, correctly rejected by the existing certificate. The four full-recovery failures are distinct from two test probe failures after a deliberately changed numeric value destroyed its prior definition.

Do not trim, normalize, omit or relax any resource, document or default digest. Do not alter StreamUtil, native XML/ZIP codecs, Kernel, R10, UndoManager, I2, profile or clipboard acquisition.

## Smallest proposed perimeter

Two product sources only:

1. GeoCeDGDocumentInsertion.java — capture the exact immutable prestate Kernel library String; scope only the IMAGE full-native recovery reader and restitution of that captured String.
2. AppGeoCeDG.java — an owned lexical same-document library-recovery scope, consumed by its existing private ClipboardScripts subclass. Override only setGlobalScript and clearGlobalObjects while that scope is active; outside it delegate unchanged.

Tests: GeoCeDG120ClipboardImageRecoveryTest.java; adjust only the two demonstrated dynamic-default operational probes so their own setValue/value-definition mutation is not misreported as insertion drift. Keep their first exact refusal assertion, pointer/link/zero-loader/listener assertions and prove actual linked operation. This is not a change to product acceptance. Other source and test files remain frozen.

## Why the existing Kernel API needs a lexical scope

Kernel.setLibraryJavaScript is not a passive assignment: after writing the string it calls ScriptManagerD.setGlobalScript. That method evaluates all top-level JavaScript into a fresh Rhino scope through CallJavaScript.evalGlobalScript. Neither the main dispatcher disabled flag nor blockUpdateScripts prevents that direct evaluation. ScriptManager.registerGlobalListener appends without deduplication. Calling the setter again can repeat construction changes and global registrations even though the final construction XML later hides the earlier evaluation.

Kernel.resetLibraryJavaScript calls both setLibraryJavaScript and clearGlobalObjects. Suppressing only the first would lose an initialized global scope through the second. Both therefore need the same narrow guard. No silent Kernel field write, reflection, replacement ScriptManager or new global scripting policy is proposed.

## Scope and invariants

Proposed package-private App API: beginClipboardLibraryRecovery(expectedConstruction, expectedDocumentId), returning an AutoCloseable owned scope. Entry occurs only after the accepted image barrier, immediately around the actual same-document full-native recovery read and raw String restitution. Require the exact live App/Construction owner, expected UUID, EDT and the owned ScriptManager class when one already exists. Foreign-manager admission stays refused. Nested scopes, if supported, must match the same owner and close in LIFO order; finally restores the previous guard, including exceptions. Do not run JavaScript on scope entry or exit.

The App guard must be visible to its stateless newScriptManager factory. If there was no manager at entry, do not create one just to capture state. A manager created by the native reader is still the private owned ClipboardScripts; its setGlobalScript/clearGlobalObjects overrides observe the already-active App scope and leave its native private globalObjects field null. The subclass constructor remains only the inherited native initialization, with no dependence on fields initialized after the App super-constructor.

If the existing manager had an initialized private Rhino scope, neither override calls super during the recovery scope, so that exact reference is retained without reading it. If it was lazily null, it remains null. Do not inspect or copy private globalObjects, force initialization or evaluate the library on exit. Creation of a previously absent manager by the normal reader is allowed; this design promises no initialization/evaluation of its global scope, not the absence of that manager object afterwards.

The existing outer IMAGE event scope remains necessary: it keeps listenersEnabled=false even across native enableListeners calls. EventDispatcher.clearView still invokes native reset. ScriptManager.reset may clear the already-empty per-object maps, then observes that false flag and retains global listener arrays/JsReference objects. Native reference-memory reset remains allowed and declared. Object listeners stay conservatively refused; global listeners and ordinary GeoElement scripts remain admissible.

## Reader order and raw restitution

ClipboardState captures `libraryJavaScript = kernel.getLibraryJavaScript()` as an immutable String before the barrier, alongside its unchanged full resource digest. No normalization or parsing is performed. The verified P7 snapshot remains the authority and is read unchanged by readNativeZipFromInputStream.

In the checkpoint-null branch only:

1. Enter the owned same-document library scope before invoking the native full reader.
2. Read the existing exact snapshot using the native reader. Its temporary normalized Kernel string may be assigned, but its global-scope evaluation/clearing is suppressed by the owned overrides.
3. Before leaving the same scope, if necessary call the existing Kernel.setLibraryJavaScript with the captured exact String. Its assignment restores the raw content while the guarded setGlobalScript remains a no-op.
4. Require raw String equality. Finish the existing defaults restoration and full document/resource certificate before reporting recovery or attaching history.

If the reader throws, attempt raw restitution in finally within the still-active scope; preserve the original reader cause and add restitution/cleanup failures as suppressed. A failed reader is never turned into success by a repaired string. Existing bounded attempts, isolation, session-reset declarations, companion ownership and strict certificates remain unchanged. The native-companion construction-only branch does not gain this scope or a new JavaScript setter.

Normal FILE Open, unrelated Undo/Redo and library editing outside the lexical IMAGE recovery scope continue to evaluate/clear exactly as before. The design is not a general script sandbox. The inspected direct MyXMLioJre/handler path does not call ggbOnInit; ordinary event callbacks are blocked by the existing outer scope. ScriptManagerD private getGlobalObjects is reached by script execution/listener/init paths, not by the two guarded setters once overridden. Arbitrary external direct ScriptManager execution while the reader owns the EDT is outside this narrow orchestration; no claim of suppressing all possible caller code is made. Tests must expose an unexpected actual scope initialization rather than infer it from final XML alone.

## Required focused oracles before any candidate claim

Preserve the complete R12 selection and the56 image invocations. The four failing full-recovery paths must now return declared SESSION_RESET only after exact resource/default/construction/history comparison; no weakened assertions or discarded failures.

- Parameterize a plain full-native recovery over the default library without terminal LF, a function-only custom library without terminal LF, CRLF, multiple lines, Unicode and already-LF forms. Save exact before/returned native archives and compare every nonconstruction member, especially JS bytes; retain loader/reload/hook counters.
- Initialized-scope/named callback oracle: library top-level registers click listener `f` and owns a lexical counter; `f` updates a numeric witness. Warm THREE deliveries before baseline (counter/witness3). Recover, assert exact state before probing, then one CLICK must yield4 and another5. This detects fresh Rhino scope, duplicate registration and a second setter evaluation; warming only1 can accidentally coincide with two duplicate deliveries.
- Register a real native BaseFunction callback independently, preserve its original JsReference and callback count, and verify one delivery after recovery. Do not initialize the scope through the diagnostic probe during the owned read.
- Lazy-null oracle using only existing public APIs: set a library whose top level sets LazyWitness=41, then explicitly restore witness7 and call scripts.clearGlobalObjects before baseline. Recovery must retain witness7 and exact bytes. Afterwards ScriptManagerD.evalJavaScript(app,"void 0",null) must lazily initialize once and change it to41. If the reader evaluated before XML and XML hid that mutation, its now-initialized scope will not make the later change; the oracle detects it without reflection.
- Exercise ordinary library setter and FILE Open after scope exit: native top-level evaluation and clearing must still occur, proving the guard does not leak. Preserve ordinary object-listener refusal and global callbacks outside the scope.
- Exception/nesting guard: failed full reader and failed restitution close the lexical guard, preserve the owning failure/suppressed chain and retain existing bounded recovery/isolation. No evaluation on close. Validate the no-manager-at-entry factory path statically and with an actual reachable fixture if available; do not claim a runtime test by assuming private state.

No new benchmark, global gate or packaging is implied before this proposal is independently reviewed and the author authorizes the exact additional correction. Runtime assertions above are requirements, not obtained results. F5 scripts remain paused drafts; no final measurement has occurred.
