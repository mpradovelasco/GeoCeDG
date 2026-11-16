/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.desktop;

import static org.geocedg.desktop.GeoCeDG120ClipboardImageInsertionTest.*;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.geocedg.common.document.controls.ControlLayout;
import org.geocedg.desktop.GeoCeDGClipboardImageInsertion.Choice;
import org.geocedg.desktop.GeoCeDGClipboardImageInsertion.Position;
import org.geogebra.common.kernel.geos.AbsoluteScreenLocateable;
import org.geogebra.common.kernel.ConstructionDefaults;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.View;
import org.geogebra.common.kernel.arithmetic.MyDouble;
import org.geogebra.common.kernel.arithmetic.NumberValue;
import org.geogebra.common.main.undo.UndoCommand;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoImage;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.plugin.Event;
import org.geogebra.common.plugin.EventListener;
import org.geogebra.common.plugin.EventType;
import org.geogebra.common.plugin.JsReference;
import org.geogebra.common.plugin.ScriptManager;
import org.geogebra.common.plugin.script.GgbScript;
import org.geogebra.desktop.gui.MyImageD;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.main.undo.FileAppState;
import org.geogebra.desktop.plugin.ScriptManagerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/** Failure paths: incomplete native creation, original history, declared reload and isolation. */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class GeoCeDG120ClipboardImageRecoveryTest {
	@TempDir(cleanup = CleanupMode.NEVER) Path directory;

	private static byte[] undoArchive(UndoCommand command) throws IOException {
		return Files.readAllBytes(assertInstanceOf(FileAppState.class, command.getAppState()).getFile().toPath());
	}

	private static byte[] onlyUndoXml(byte[] archive) throws IOException {
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive))) {
			var entry = zip.getNextEntry(); assertNotNull(entry); assertEquals("geogebra.xml", entry.getName());
			byte[] xml = zip.readAllBytes(); assertNull(zip.getNextEntry(), "An Undo archive has exactly one member"); return xml;
		}
	}

	private static byte[] storedUndo(byte[] xml, boolean extra) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (ZipOutputStream zip = new ZipOutputStream(output)) {
			CRC32 crc = new CRC32(); crc.update(xml); ZipEntry entry = new ZipEntry("geogebra.xml");
			entry.setMethod(ZipEntry.STORED); entry.setSize(xml.length); entry.setCompressedSize(xml.length); entry.setCrc(crc.getValue());
			zip.putNextEntry(entry); zip.write(xml); zip.closeEntry();
			if (extra) { zip.putNextEntry(new ZipEntry("unexpected.txt")); zip.write(1); zip.closeEntry(); }
		}
		return output.toByteArray();
	}

	private static void addNativeInput(AppGeoCeDG app) {
		must(app, "BaselineCircle=Circle(A,2)"); must(app, "BaselinePlace=Point(BaselineCircle)");
	}

	@ParameterizedTest @ValueSource(strings = {"extra-member", "stored-crc", "changed-xml", "foreign-owner", "foreign-ordinary-owner"})
	void imageBaselinePreflightAuthenticatesOriginalBytesAndRejectsAlteredOrForeignCopies(String variant) throws Exception {
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		AppGeoCeDG foreign = onEdt(() -> { return fixture(); });
		onEdt(() -> {
			addNativeInput(app); if (!"foreign-ordinary-owner".equals(variant)) { addNativeInput(foreign); }
			baseline(app); baseline(foreign);
			UndoCommand original = undo(app).getCommands().get(0), other = undo(foreign).getCommands().get(0);
			assertInstanceOf(GeoCeDGUndoManager.NativeFileAppState.class, original.getAppState());
			if ("foreign-ordinary-owner".equals(variant)) { assertFalse(other.getAppState() instanceof GeoCeDGUndoManager.NativeFileAppState); }
			else { assertInstanceOf(GeoCeDGUndoManager.NativeFileAppState.class, other.getAppState()); }
			byte[] bytes = undoArchive(original), otherBytes = undoArchive(other), xml = onlyUndoXml(bytes);
			assertTrue(new String(xml, StandardCharsets.UTF_8).contains("<nativeRollbackBinding"));
			State before = new State(app), otherBefore = new State(foreign);
			assertTrue(app.getDocumentInsertion().preflightClipboardUndo(undo(app), original, bytes.clone()));
			before.assertRestored(app, true); otherBefore.assertRestored(foreign, true);
			byte[] changed; UndoCommand submitted = original;
			switch (variant) {
			case "extra-member":
				changed = storedUndo(xml, true);
				try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(changed))) {
					assertEquals("geogebra.xml", zip.getNextEntry().getName()); assertArrayEquals(xml, zip.readAllBytes());
					assertNotNull(zip.getNextEntry());
				}
				break;
			case "stored-crc":
				changed = storedUndo(xml, false);
				int nameLength = (changed[26] & 255) | (changed[27] & 255) << 8;
				int extraLength = (changed[28] & 255) | (changed[29] & 255) << 8;
				assertArrayEquals(xml, Arrays.copyOfRange(changed, 30 + nameLength + extraLength, 30 + nameLength + extraLength + xml.length));
				changed[14] ^= 1; // Change only the STORED local-header CRC; preserve the XML body.
				assertArrayEquals(xml, Arrays.copyOfRange(changed, 30 + nameLength + extraLength, 30 + nameLength + extraLength + xml.length));
				break;
			case "changed-xml":
				changed = storedUndo((new String(xml, StandardCharsets.UTF_8) + "\n").getBytes(StandardCharsets.UTF_8), false);
				assertArrayEquals((new String(xml, StandardCharsets.UTF_8) + "\n").getBytes(StandardCharsets.UTF_8), onlyUndoXml(changed));
				break;
			case "foreign-owner":
			case "foreign-ordinary-owner":
				assertTrue(foreign.getDocumentInsertion().preflightClipboardUndo(undo(foreign), other, otherBytes.clone()));
				before.assertRestored(app, true); otherBefore.assertRestored(foreign, true);
				changed = otherBytes.clone(); submitted = other; break;
			default: throw new AssertionError(variant);
			}
			Files.write(directory.resolve("baseline-original.zip"), bytes); Files.write(directory.resolve("baseline-" + variant + ".zip"), changed);
			System.out.println("CLIPBOARD_R12_BASELINE_EVIDENCE " + directory.toAbsolutePath() + " variant=" + variant);
			assertFalse(app.getDocumentInsertion().preflightClipboardUndo(undo(app), submitted, changed));
			before.assertRestored(app, true); otherBefore.assertRestored(foreign, true);
			assertArrayEquals(bytes, undoArchive(original)); assertArrayEquals(otherBytes, undoArchive(other)); return null;
		});
	}

	@ParameterizedTest @ValueSource(booleans = {false, true})
	void aDynamicDefaultStepIsRefusedBeforeOrDuringTheDialogWithoutLosingTheExternalLink(boolean duringDialog) throws Exception {
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		onEdt(() -> {
			GeoNumeric rate = assertInstanceOf(GeoNumeric.class, must(app, "DefaultRate=0.25"));
			GeoElement prototype = app.getKernel().getConstruction().getConstructionDefaults().getDefaultGeo(ConstructionDefaults.DEFAULT_IMAGE);
			if (!duringDialog) { prototype.setAnimationStep((NumberValue) rate); }
			baseline(app); app.getEventDispatcher().disableListeners();
			AtomicReference<State> expected = new AtomicReference<>(new State(app));
			AtomicInteger dialogs = new AtomicInteger(), loads = new AtomicInteger(), events = new AtomicInteger();
			EventListener observer = event -> events.incrementAndGet(); app.getEventDispatcher().addEventListener(observer);
			try {
				var operation = operation(app, 7, Position.APPEND, target -> { loads.incrementAndGet(); return true; });
				operation.setPresentation((target, state) -> {
					dialogs.incrementAndGet(); assertTrue(duringDialog);
					prototype.setAnimationStep((NumberValue) rate); // Deliberately emits no Kernel event.
					assertEquals(state.currentStep(), target.getKernel().getConstruction().getStep()); expected.set(new State(target));
					return new Choice(7, Position.APPEND);
				});
				var result = operation.insert(); assertOutcome(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION, result);
				assertTrue(containsCauseCode(result.getFailure(), "DYNAMIC_ANIMATION_STEP"));
				assertEquals(duringDialog ? 1 : 0, dialogs.get()); assertEquals(0, loads.get()); expected.get().assertRestored(app, true);
				assertSame(prototype, app.getKernel().getConstruction().getConstructionDefaults().getDefaultGeo(ConstructionDefaults.DEFAULT_IMAGE));
				assertSame(rate, prototype.getAnimationStepObject()); assertSame(rate, app.getKernel().lookupLabel("DefaultRate"));
				double value = rate.getDouble(); long valueBits = Double.doubleToRawLongBits(value);
				var definition = rate.getDefinition(); var exact = rate.toDecimal();
				saveRateProbeEvidence(app, rate, "before-probe");
				try {
					rate.setValue(0.5, false);
					assertSame(rate, prototype.getAnimationStepObject());
					assertEquals(0.5, prototype.getAnimationStepObject().getDouble());
					saveRateProbeEvidence(app, rate, "changed-probe");
				} finally {
					// The test's value probe clears its own definition/exact metadata.
					// Restore those fields after setValue, without changing animationValue.
					rate.setValue(value, false); rate.setExactValue(exact); rate.setDefinition(definition);
				}
				assertEquals(valueBits, Double.doubleToRawLongBits(rate.getDouble()));
				assertSame(definition, rate.getDefinition()); assertSame(exact, rate.toDecimal());
				saveRateProbeEvidence(app, rate, "restored-probe");
				app.getEventDispatcher().dispatchEvent(new Event(EventType.UPDATE, rate)); assertEquals(0, events.get());
				expected.get().assertRestored(app, true);
			} finally { app.getEventDispatcher().removeEventListener(observer); app.getEventDispatcher().enableListeners(); }
			return null;
		});
	}

	@Test void anInPlaceConstantStepEditInvalidatesTheModalRequestEvenWhenItsXmlIsIdentical() throws Exception {
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		onEdt(() -> {
			GeoElement prototype = app.getKernel().getConstruction().getConstructionDefaults().getDefaultGeo(ConstructionDefaults.DEFAULT_IMAGE);
			MyDouble raw = new MyDouble(app.getKernel(), 0.0); prototype.setAnimationStep(raw);
			baseline(app); app.getEventDispatcher().disableListeners(); DefaultsSnapshot initial = new DefaultsSnapshot(app);
			AtomicReference<State> expected = new AtomicReference<>(); AtomicInteger dialogs = new AtomicInteger(), loads = new AtomicInteger();
			var operation = operation(app, 7, Position.APPEND, target -> { loads.incrementAndGet(); return true; });
			operation.setPresentation((target, state) -> {
				dialogs.incrementAndGet(); raw.set(-0.0); // Same object and XML, different captured bits; no Kernel event.
				assertSame(raw, prototype.getAnimationStepObject());
				assertEquals(initial.two, new DefaultsSnapshot(target).two);
				assertNotEquals(initial.prototypes.get(ConstructionDefaults.DEFAULT_IMAGE).bits().longValue(), Double.doubleToRawLongBits(raw.getDouble()));
				expected.set(new State(target)); return new Choice(7, Position.APPEND);
			});
			try {
				var result = operation.insert(); assertOutcome(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION, result);
				assertTrue(result.getMessage().contains(app.layerText("ClipboardImage.Changed")));
				assertEquals(1, dialogs.get()); assertEquals(0, loads.get()); expected.get().assertRestored(app, true);
				assertSame(raw, prototype.getAnimationStepObject()); assertEquals(Double.doubleToRawLongBits(-0.0), Double.doubleToRawLongBits(raw.getDouble()));
				AtomicInteger events = new AtomicInteger(); EventListener observer = event -> events.incrementAndGet();
				app.getEventDispatcher().addEventListener(observer);
				try { app.getEventDispatcher().dispatchEvent(new Event(EventType.UPDATE, app.getKernel().lookupLabel("A"))); assertEquals(0, events.get()); }
				finally { app.getEventDispatcher().removeEventListener(observer); }
			} finally { app.getEventDispatcher().enableListeners(); }
			return null;
		});
	}

	@Test void anUnexpectedDefaultMutationRequiresTheFullNativeReloadInsteadOfLazyNormalization() throws Exception {
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		onEdt(() -> {
			assertNull(app.getKernel().getConstruction().captureNativeUndoSnapshot());
			DefaultsSnapshot defaults = new DefaultsSnapshot(app); assertNull(defaults.prototypes.get(ConstructionDefaults.DEFAULT_IMAGE).raw());
			defaults.saveEvidence(directory, "unknown-default-before", app); baseline(app); State before = new State(app);
			AtomicInteger loads = new AtomicInteger(), commits = new AtomicInteger(), reloads = new AtomicInteger();
			AtomicReference<byte[]> observed = new AtomicReference<>(); AtomicReference<IOException> evidenceFailure = new AtomicReference<>();
			app.getDocumentInsertion().setFaults(new GeoCeDGDocumentInsertion.Faults() {
				@Override public void afterCommit() {
					commits.incrementAndGet();
					app.getKernel().getConstruction().getConstructionDefaults().getDefaultGeo(ConstructionDefaults.DEFAULT_IMAGE)
							.setAnimationStep(new MyDouble(app.getKernel(), 0.2));
					try { observed.set(nativeArchive(app)); } catch (IOException failure) { evidenceFailure.set(failure); }
					// No injected exception: production must detect this unexpected raw change.
				}
				@Override public void beforeReload(int attempt) { reloads.incrementAndGet(); }
			});
			var result = operation(app, 7, Position.APPEND, target -> { loads.incrementAndGet(); nativeImages(target, 1); return true; }).insert();
			if (observed.get() != null) { Files.write(directory.resolve("unknown-default-observed.cedg"), observed.get()); }
			defaults.saveEvidence(directory, "unknown-default-returned", app);
			assertOutcome(GeoCeDGDocumentInsertion.Outcome.FAILED_RECOVERY_CERTIFIED_SESSION_RESET, result);
			assertTrue(containsCauseCode(result.getFailure(), "UNEXPECTED_LAZY_DEFAULT_CHANGE"));
			assertEquals(1, loads.get()); assertEquals(1, commits.get()); assertEquals(1, reloads.get());
			assertNull(evidenceFailure.get()); assertNotNull(observed.get());
			before.assertRestored(app, false); assertFalse(defaults.samePrototypes(app)); defaults.assertArchive(app);
			defaults.saveEvidence(directory, "unknown-default-recovered", app); assertFalse(app.isInsertionIsolated()); return null;
		});
	}

	private void saveRateProbeEvidence(AppGeoCeDG app, GeoNumeric rate, String stage) throws IOException {
		Files.write(directory.resolve(stage + ".cedg"), nativeArchive(app));
		Files.writeString(directory.resolve(stage + "-rate.txt"), "bits="
				+ Long.toUnsignedString(Double.doubleToRawLongBits(rate.getDouble()))
				+ "\ndefinition=" + rate.getDefinition() + "\nexact=" + rate.toDecimal() + "\n");
		System.out.println("CLIPBOARD_R13_PROBE " + directory.toAbsolutePath() + " stage=" + stage);
	}

	private static Map<String, byte[]> archiveMembers(byte[] archive) throws IOException {
		Map<String, byte[]> members = new LinkedHashMap<>();
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive))) {
			for (ZipEntry entry; (entry = zip.getNextEntry()) != null;) {
				assertNull(members.put(entry.getName(), zip.readAllBytes()), "Duplicate native archive member");
			}
		}
		assertTrue(members.containsKey("geogebra.xml"));
		assertTrue(members.containsKey("geogebra_javascript.js")); return members;
	}

	private void saveLibraryArchive(String stage, byte[] archive) throws Exception {
		Files.write(directory.resolve(stage + ".cedg"), archive);
		StringBuilder metadata = new StringBuilder();
		for (var member : archiveMembers(archive).entrySet()) {
			CRC32 crc = new CRC32(); crc.update(member.getValue());
			String digest = java.util.HexFormat.of().formatHex(
					java.security.MessageDigest.getInstance("SHA-256").digest(member.getValue()));
			metadata.append(member.getKey()).append(" bytes=").append(member.getValue().length)
					.append(" crc32=").append(crc.getValue()).append(" sha256=").append(digest).append('\n');
		}
		Files.writeString(directory.resolve(stage + "-members.txt"), metadata);
		Files.write(directory.resolve(stage + "-javascript.bin"), archiveMembers(archive).get("geogebra_javascript.js"));
		System.out.println("CLIPBOARD_R13_LIBRARY_EVIDENCE " + directory.toAbsolutePath() + " stage=" + stage);
	}

	private static void assertLibraryResources(byte[] before, byte[] after, String raw) throws IOException {
		Map<String, byte[]> expected = archiveMembers(before), actual = archiveMembers(after);
		assertEquals(expected.keySet(), actual.keySet());
		assertArrayEquals(raw.getBytes(StandardCharsets.UTF_8), expected.get("geogebra_javascript.js"));
		for (var member : expected.entrySet()) {
			if (!"geogebra.xml".equals(member.getKey())) {
				assertArrayEquals(member.getValue(), actual.get(member.getKey()), "Exact native resource " + member.getKey());
			}
		}
	}

	/** The real IMAGE checkpoint-null path; no archive, reader or restoration seam is replaced. */
	private void recoverLibraryThroughNativeArchive(AppGeoCeDG app) throws Exception {
		assertNull(app.getKernel().getConstruction().captureNativeUndoSnapshot());
		baseline(app); State before = new State(app); String raw = app.getKernel().getLibraryJavaScript();
		byte[] archive = nativeArchive(app); saveLibraryArchive("library-before", archive);
		AtomicInteger loads = new AtomicInteger(), commits = new AtomicInteger(), removals = new AtomicInteger(), reloads = new AtomicInteger();
		IllegalStateException injected = new IllegalStateException("R13 force actual full native image recovery");
		app.getDocumentInsertion().setFaults(new GeoCeDGDocumentInsertion.Faults() {
			@Override public void afterCommit() { commits.incrementAndGet(); throw injected; }
			@Override public void duringRemoval() { removals.incrementAndGet(); throw new IllegalStateException("R13 full native reader required"); }
			@Override public void beforeReload(int attempt) { reloads.incrementAndGet(); }
		});
		var result = operation(app, 7, Position.APPEND, target -> { loads.incrementAndGet(); nativeImages(target, 1); return true; }).insert();
		byte[] returned = nativeArchive(app); saveLibraryArchive("library-returned", returned);
		assertOutcome(GeoCeDGDocumentInsertion.Outcome.FAILED_RECOVERY_CERTIFIED_SESSION_RESET, result);
		assertTrue(containsFailure(result.getFailure(), injected));
		assertEquals(1, loads.get()); assertEquals(1, commits.get()); assertEquals(1, removals.get()); assertEquals(1, reloads.get());
		assertEquals(raw, app.getKernel().getLibraryJavaScript()); before.assertRestored(app, false);
		assertLibraryResources(archive, returned, raw); assertFalse(app.isInsertionIsolated());
	}

	@ParameterizedTest @ValueSource(strings = {"default-no-lf", "custom-no-lf", "crlf", "multiline", "unicode", "already-lf"})
	void fullNativeRecoveryPreservesEveryLibraryResourceByte(String format) throws Exception {
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		onEdt(() -> {
			String raw = switch (format) {
			case "default-no-lf" -> Kernel.defaultLibraryJavaScript;
			case "custom-no-lf" -> "function ggbOnInit() {}\nfunction customRecovery() { return 13; }";
			case "crlf" -> "function ggbOnInit() {}\r\nfunction customRecovery() { return 13; }\r\n";
			case "multiline" -> "function ggbOnInit() {\n  var n = 13;\n}\n\nfunction customRecovery() { return 13; }";
			case "unicode" -> "// CeDG: π, á, cono oblicuo\nfunction ggbOnInit() {}";
			case "already-lf" -> "function ggbOnInit() {}\n";
			default -> throw new AssertionError(format);
			};
			if ("default-no-lf".equals(format)) { assertFalse(raw.endsWith("\n")); }
			app.getKernel().setLibraryJavaScript(raw); recoverLibraryThroughNativeArchive(app); return null;
		});
	}

	@Test void initializedLibraryScopeAndBothKindsOfGlobalCallbacksSurviveNativeRecovery() throws Exception {
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		onEdt(() -> {
			must(app, "LibraryCounter=0");
			String raw = "var recoveryCounter = 0;\n"
					+ "function recoveryClick(name) { recoveryCounter++; ggbApplet.setValue(\"LibraryCounter\", recoveryCounter); }\n"
					+ "ggbApplet.registerClickListener(\"recoveryClick\");\nfunction ggbOnInit() {}";
			app.getKernel().setLibraryJavaScript(raw); var scripts = app.getScriptManager(); scripts.dropListenersOnReset();
			for (int i = 1; i <= 3; i++) {
				app.getEventDispatcher().dispatchEvent(new Event(EventType.CLICK, app.getKernel().lookupLabel("A")));
				assertEquals(i, ((GeoNumeric) app.getKernel().lookupLabel("LibraryCounter")).getDouble());
			}
			AtomicInteger nativeCalls = new AtomicInteger(); List<Scriptable> nativeScopes = new ArrayList<>();
			BaseFunction nativeFunction = new BaseFunction() {
				private static final long serialVersionUID = 1L;
				@Override public Object call(Context context, Scriptable scope, Scriptable self, Object[] arguments) {
					nativeCalls.incrementAndGet(); nativeScopes.add(scope.getPrototype()); return null;
				}
			};
			scripts.registerAddListener(nativeFunction); List<JsReference> references = List.copyOf(scripts.getAddListeners());
			JsReference original = references.get(references.size() - 1); assertSame(nativeFunction, original.getNativeRunnable());
			app.getEventDispatcher().dispatchEvent(new Event(EventType.ADD, app.getKernel().lookupLabel("A")));
			assertEquals(1, nativeCalls.get()); assertNotNull(nativeScopes.get(0));
			recoverLibraryThroughNativeArchive(app);
			assertSame(scripts, app.getScriptManager()); assertEquals(references, scripts.getAddListeners());
			for (int i = 0; i < references.size(); i++) { assertSame(references.get(i), scripts.getAddListeners().get(i)); }
			assertEquals(1, nativeCalls.get(), "Recovery cannot publish intermediate ADD callbacks");
			for (int i = 4; i <= 5; i++) {
				app.getEventDispatcher().dispatchEvent(new Event(EventType.CLICK, app.getKernel().lookupLabel("A")));
				assertEquals(i, ((GeoNumeric) app.getKernel().lookupLabel("LibraryCounter")).getDouble());
			}
			app.getEventDispatcher().dispatchEvent(new Event(EventType.ADD, app.getKernel().lookupLabel("A")));
			assertEquals(2, nativeCalls.get()); assertSame(nativeScopes.get(0), nativeScopes.get(1), "The initialized Rhino global scope itself must survive");
			return null;
		});
	}

	@Test void aLazyNullLibraryScopeRemainsUninitializedUntilTheFirstOrdinaryEval() throws Exception {
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		onEdt(() -> {
			must(app, "LazyWitness=7");
			String raw = "ggbApplet.setValue(\"LazyWitness\", 41);\nfunction ggbOnInit() {}";
			app.getKernel().setLibraryJavaScript(raw);
			assertEquals(41, ((GeoNumeric) app.getKernel().lookupLabel("LazyWitness")).getDouble());
			((GeoNumeric) app.getKernel().lookupLabel("LazyWitness")).setValue(7, false);
			var scripts = assertInstanceOf(ScriptManagerD.class, app.getScriptManager()); scripts.clearGlobalObjects();
			recoverLibraryThroughNativeArchive(app);
			GeoNumeric witness = (GeoNumeric) app.getKernel().lookupLabel("LazyWitness"); assertEquals(7, witness.getDouble());
			scripts.evalJavaScript(app, "void 0", null); assertEquals(41, witness.getDouble());
			witness.setValue(7, false); scripts.evalJavaScript(app, "void 0", null);
			assertEquals(7, witness.getDouble(), "A second ordinary eval uses the now-initialized scope"); return null;
		});
	}

	@Test void ordinaryLibrarySetterClearingAndFileOpenRetainTheirNativeBehaviorAfterRecovery() throws Exception {
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		onEdt(() -> {
			must(app, "OrdinaryWitness=0"); must(app, "OnInitWitness=0");
			String raw = "ggbApplet.setValue(\"OrdinaryWitness\", 23);\n"
					+ "function ordinaryFileAdd(name) { ggbApplet.setValue(\"OnInitWitness\", 99); }\n"
					+ "ggbApplet.registerAddListener(\"ordinaryFileAdd\");\nfunction ggbOnInit() {}";
			app.getKernel().setLibraryJavaScript(raw);
			((GeoNumeric) app.getKernel().lookupLabel("OrdinaryWitness")).setValue(7, false);
			recoverLibraryThroughNativeArchive(app);
			assertEquals(7, ((GeoNumeric) app.getKernel().lookupLabel("OrdinaryWitness")).getDouble());
			app.getKernel().setLibraryJavaScript(raw);
			assertEquals(23, ((GeoNumeric) app.getKernel().lookupLabel("OrdinaryWitness")).getDouble());
			var scripts = assertInstanceOf(ScriptManagerD.class, app.getScriptManager()); scripts.clearGlobalObjects();
			((GeoNumeric) app.getKernel().lookupLabel("OrdinaryWitness")).setValue(7, false);
			scripts.evalJavaScript(app, "void 0", null);
			assertEquals(23, ((GeoNumeric) app.getKernel().lookupLabel("OrdinaryWitness")).getDouble());
			((GeoNumeric) app.getKernel().lookupLabel("OrdinaryWitness")).setValue(7, false);
			for (int i = scripts.getAddListeners().size(); i > 0; i--) { scripts.unregisterAddListener("ordinaryFileAdd"); }
			assertTrue(scripts.getAddListeners().isEmpty()); scripts.clearGlobalObjects();
			File file = save(app, directory, "ordinary-open-after-library-recovery");
			assertTrue(app.loadFile(file, false));
			assertEquals(1, scripts.getAddListeners().size(), "Ordinary FILE top-level evaluation registers its native callback");
			((GeoNumeric) app.getKernel().lookupLabel("OnInitWitness")).setValue(0, false);
			app.getEventDispatcher().dispatchEvent(new Event(EventType.ADD, app.getKernel().lookupLabel("A")));
			assertEquals(99, ((GeoNumeric) app.getKernel().lookupLabel("OnInitWitness")).getDouble());
			return null;
		});
	}

	@Test void libraryRecoveryScopeChecksItsOwnerAndClosesNestedExceptionalScopesWithoutEvaluation() throws Exception {
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		AppGeoCeDG foreign = onEdt(() -> { return fixture(); });
		var construction = app.getKernel().getConstruction(); String uuid = app.getUniqueId();
		assertThrows(IllegalStateException.class, () -> app.beginClipboardLibraryRecovery(construction, uuid), "The test worker is outside EDT");
		onEdt(() -> {
			must(app, "ScopeWitness=7"); String raw = "ggbApplet.setValue(\"ScopeWitness\", 41);\nfunction ggbOnInit() {}";
			app.getKernel().setLibraryJavaScript(raw); var scripts = assertInstanceOf(ScriptManagerD.class, app.getScriptManager());
			((GeoNumeric) app.getKernel().lookupLabel("ScopeWitness")).setValue(7, false); scripts.clearGlobalObjects();
			State before = new State(app);
			assertThrows(IllegalStateException.class, () -> app.beginClipboardLibraryRecovery(foreign.getKernel().getConstruction(), uuid));
			assertThrows(IllegalStateException.class, () -> app.beginClipboardLibraryRecovery(construction, uuid + "-foreign"));
			IllegalStateException injected = new IllegalStateException("nested library scope");
			try (var outer = app.beginClipboardLibraryRecovery(construction, uuid)) {
				app.getKernel().setLibraryJavaScript(raw); scripts.clearGlobalObjects();
				assertSame(injected, assertThrows(IllegalStateException.class, () -> {
					try (var inner = app.beginClipboardLibraryRecovery(construction, uuid)) {
						app.getKernel().setLibraryJavaScript(raw); scripts.clearGlobalObjects(); throw injected;
					}
				}));
				assertEquals(7, ((GeoNumeric) app.getKernel().lookupLabel("ScopeWitness")).getDouble());
			}
			try (var outer = app.beginClipboardLibraryRecovery(construction, uuid)) {
				var inner = app.beginClipboardLibraryRecovery(construction, uuid);
				try {
					assertTrue(assertThrows(IllegalStateException.class, outer::close).getMessage().contains("LIFO"));
					app.getKernel().setLibraryJavaScript(raw); scripts.clearGlobalObjects();
				} finally { inner.close(); inner.close(); }
			}
			var changedOwner = app.beginClipboardLibraryRecovery(construction, uuid);
			try {
				app.setUniqueId(uuid + "-changed");
				assertEquals("CLIPBOARD_LIBRARY_OWNER_CHANGED", assertThrows(IllegalStateException.class, changedOwner::close).getMessage());
			} finally { app.setUniqueId(uuid); changedOwner.close(); }
			before.assertRestored(app, true);
			assertEquals(7, ((GeoNumeric) app.getKernel().lookupLabel("ScopeWitness")).getDouble(), "No eval at either scope exit");
			app.getKernel().setLibraryJavaScript(raw);
			assertEquals(41, ((GeoNumeric) app.getKernel().lookupLabel("ScopeWitness")).getDouble(), "Native setter resumes after both exceptional closes");
			System.out.println("CLIPBOARD_R13_MANAGER_LIMIT initializedFixture=" + app.hasScriptManager()
					+ "; absent-manager factory branch is source-reviewed, not claimed as a reached runtime precondition"); return null;
		});
	}

	@ParameterizedTest @ValueSource(booleans = {false, true})
	void anActualNativeReaderFailureRestoresRawBytesBeforeTheBoundedRetryAndClosesItsGuard(boolean failRestitution) throws Exception {
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		onEdt(() -> {
			String raw = "function ggbOnInit() {}\nfunction readerFailureWitness() { return 13; }";
			app.getKernel().setLibraryJavaScript(raw); assertNull(app.getKernel().getConstruction().captureNativeUndoSnapshot());
			baseline(app); State before = new State(app); byte[] archive = nativeArchive(app); saveLibraryArchive("reader-failure-before", archive);
			AtomicInteger loads = new AtomicInteger(), commits = new AtomicInteger(), removals = new AtomicInteger(), reloads = new AtomicInteger();
			AtomicInteger readerThrows = new AtomicInteger(); AtomicBoolean attached = new AtomicBoolean();
			IllegalStateException mutation = new IllegalStateException("R13 recover this mutation");
			boolean loading = app.getKernel().getLoadingMode(), notifications = app.getKernel().isNotifyViewsActive();
			String uuid = app.getUniqueId(), changedUuid = uuid + "-r13-restitution-failure";
			IllegalStateException readerFailure = new IllegalStateException("R13 real reader postparse reset failure");
			AtomicReference<Throwable> observedReaderFailure = new AtomicReference<>(); View observer = mock(View.class);
			doAnswer(invocation -> {
				if (readerThrows.getAndIncrement() == 0) {
					assertTrue(app.getKernel().getConstruction().isFileLoading()); assertFalse(app.getKernel().getLoadingMode());
					assertEquals(raw + "\n", app.getKernel().getLibraryJavaScript(), "The actual native reader already assigned the normalized library");
					if (failRestitution) { app.setUniqueId(changedUuid); }
					observedReaderFailure.set(readerFailure); throw readerFailure;
				}
				return null;
			}).when(observer).reset();
			app.getDocumentInsertion().setFaults(new GeoCeDGDocumentInsertion.Faults() {
				@Override public void afterCommit() { commits.incrementAndGet(); throw mutation; }
				@Override public void duringRemoval() { removals.incrementAndGet(); throw new IllegalStateException("R13 force full reader"); }
				@Override public void beforeReload(int attempt) throws Exception {
					reloads.incrementAndGet();
					if (attempt == 1) { app.getKernel().attach(observer); attached.set(true); }
					else {
						try {
							assertEquals(2, attempt); assertSame(readerFailure, observedReaderFailure.get());
							assertEquals(raw, app.getKernel().getLibraryJavaScript());
							List<Throwable> ownerFailures = Arrays.stream(readerFailure.getSuppressed())
									.filter(problem -> problem instanceof IllegalStateException
											&& "CLIPBOARD_LIBRARY_OWNER_CHANGED".equals(problem.getMessage())).toList();
							assertEquals(failRestitution ? 2 : 0, ownerFailures.size(), "Both raw-setter cleanup and TWR close retain their owner refusal");
							if (failRestitution) {
								assertEquals(changedUuid, app.getUniqueId()); assertNotSame(ownerFailures.get(0), ownerFailures.get(1));
								// Still using the deliberately wrong UUID: this ordinary setter
								// would fail again if the failed close had leaked the App scope.
								// The original library contains functions only; no state is changed.
								assertDoesNotThrow(() -> app.getKernel().setLibraryJavaScript(raw));
							}
							java.io.StringWriter chain = new java.io.StringWriter();
							readerFailure.printStackTrace(new java.io.PrintWriter(chain));
							Files.writeString(directory.resolve("reader-failure-chain.txt"), chain.toString());
							Files.write(directory.resolve("reader-failure-before-retry-javascript.bin"), app.getKernel().getLibraryJavaScript().getBytes(StandardCharsets.UTF_8));
							System.out.println("CLIPBOARD_R13_READER_FAILURE failRestitution=" + failRestitution + " evidence=" + directory.toAbsolutePath());
							readerFailure.printStackTrace(System.out);
						} finally { app.setUniqueId(uuid); }
					}
				}
			});
			try {
				var result = operation(app, 7, Position.APPEND, target -> { loads.incrementAndGet(); nativeImages(target, 1); return true; }).insert();
				byte[] returned = nativeArchive(app); saveLibraryArchive("reader-failure-returned", returned);
				assertOutcome(GeoCeDGDocumentInsertion.Outcome.FAILED_RECOVERY_CERTIFIED_SESSION_RESET, result);
				assertEquals(1, loads.get()); assertEquals(1, commits.get()); assertEquals(1, removals.get()); assertEquals(2, reloads.get());
				assertSame(readerFailure, observedReaderFailure.get()); assertTrue(readerThrows.get() >= 1);
				app.getKernel().detach(observer); attached.set(false);
				assertEquals(loading, app.getKernel().getLoadingMode()); assertEquals(notifications, app.getKernel().isNotifyViewsActive());
				assertTrue(containsFailure(result.getFailure(), mutation)); before.assertRestored(app, false); assertLibraryResources(archive, returned, raw);
				// Existing retry policy does not publish a recovered first-attempt exception.
				// Its identity/reach and raw-finally are observed at the second real attempt.
				must(app, "GuardExitWitness=0"); app.getKernel().setLibraryJavaScript("ggbApplet.setValue(\"GuardExitWitness\", 53);");
				assertEquals(53, ((GeoNumeric) app.getKernel().lookupLabel("GuardExitWitness")).getDouble());
			} finally { app.setUniqueId(uuid); if (attached.get()) { app.getKernel().detach(observer); } }
			return null;
		});
	}

	private static BaseFunction callback(List<String> calls, String kind) {
		return new BaseFunction() {
			private static final long serialVersionUID = 1L;
			@Override public Object call(Context context, Scriptable scope, Scriptable self, Object[] arguments) {
				calls.add(kind + ":" + java.util.Arrays.toString(arguments)); return null;
			}
		};
	}

	private static final class ObjectCallbacks {
		final GeoElement receiver;
		final ScriptManager scripts;
		final List<String> calls = new ArrayList<>(), expected = new ArrayList<>();
		final Map<GeoElement, JsReference> updates, clicks;

		ObjectCallbacks(AppGeoCeDG app, boolean update, boolean click) {
			receiver = app.getKernel().lookupLabel("A"); scripts = app.getScriptManager();
			// Exercise the public mode left by redefine; the default keep-on-reset
			// would otherwise hide the callback-map loss this refusal prevents.
			scripts.dropListenersOnReset();
			BaseFunction updateFunction = callback(calls, "update"), clickFunction = callback(calls, "click");
			assertNotSame(updateFunction, clickFunction);
			if (update) { scripts.registerObjectUpdateListener("A", updateFunction); expected.add("update:[A]"); }
			if (click) { scripts.registerObjectClickListener("A", clickFunction); expected.add("click:[A]"); }
			updates = new HashMap<>(scripts.getUpdateListenerMap());
			clicks = new HashMap<>(scripts.getClickListenerMap());
			if (update) { assertSame(updateFunction, updates.get(receiver).getNativeRunnable()); }
			if (click) { assertSame(clickFunction, clicks.get(receiver).getNativeRunnable()); }
		}

		void assertIntactAndOperative(AppGeoCeDG app, boolean listenersDisabled) {
			assertSame(receiver, app.getKernel().lookupLabel("A")); assertSame(scripts, app.getScriptManager());
			assertEquals(updates, scripts.getUpdateListenerMap()); assertEquals(clicks, scripts.getClickListenerMap());
			updates.forEach((geo, reference) -> assertSame(reference, scripts.getUpdateListenerMap().get(geo)));
			clicks.forEach((geo, reference) -> assertSame(reference, scripts.getClickListenerMap().get(geo)));
			assertTrue(calls.isEmpty(), "The rejected operation must not execute either object callback");
			// This direct native listener entry proves the registered functions even
			// when the application's dispatcher was already disabled. No flag toggle.
			scripts.sendEvent(new Event(EventType.UPDATE, receiver));
			scripts.sendEvent(new Event(EventType.CLICK, receiver));
			assertEquals(expected, calls);
			app.getEventDispatcher().dispatchEvent(new Event(EventType.UPDATE, receiver));
			app.getEventDispatcher().dispatchEvent(new Event(EventType.CLICK, receiver));
			List<String> throughDispatcher = new ArrayList<>(expected);
			if (!listenersDisabled) { throughDispatcher.addAll(expected); }
			assertEquals(throughDispatcher, calls, "The previous dispatcher flag must also remain effective");
		}
	}

	@ParameterizedTest @CsvSource({"true,false,false", "false,true,false", "true,true,false",
			"true,false,true", "false,true,true", "true,true,true"})
	void objectCallbacksRefuseBeforeTheDialogAndRemainDistinct(boolean update, boolean click,
			boolean listenersDisabled) throws Exception {
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		onEdt(() -> {
			baseline(app); ObjectCallbacks callbacks = new ObjectCallbacks(app, update, click);
			if (listenersDisabled) { app.getEventDispatcher().disableListeners(); }
			State before = new State(app); AtomicInteger dialogs = new AtomicInteger(), loads = new AtomicInteger();
			var operation = operation(app, 7, Position.APPEND, target -> { loads.incrementAndGet(); return true; });
			operation.setPresentation((target, state) -> { dialogs.incrementAndGet(); return new Choice(7, Position.APPEND); });
			var result = operation.insert(); assertOutcome(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION, result);
			assertNotNull(result.getFailure()); assertEquals("OBJECT_LISTENERS", result.getFailure().getMessage());
			assertEquals(0, dialogs.get()); assertEquals(0, loads.get()); before.assertRestored(app, true);
			callbacks.assertIntactAndOperative(app, listenersDisabled); before.assertRestored(app, true); return null;
		});
	}

	@ParameterizedTest @ValueSource(booleans = {false, true})
	void objectCallbacksRegisteredDuringTheDialogRefuseBeforeTheBarrier(boolean listenersDisabled) throws Exception {
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		onEdt(() -> {
			baseline(app); if (listenersDisabled) { app.getEventDispatcher().disableListeners(); }
			State before = new State(app); GeoElement original = app.getKernel().lookupLabel("A");
			AtomicInteger dialogs = new AtomicInteger(), loads = new AtomicInteger();
			AtomicReference<ObjectCallbacks> registered = new AtomicReference<>();
			var operation = operation(app, 7, Position.APPEND, target -> { loads.incrementAndGet(); return true; });
			operation.setPresentation((target, state) -> {
				dialogs.incrementAndGet(); registered.set(new ObjectCallbacks(target, true, true));
				assertEquals(state.currentStep(), target.getKernel().getConstruction().getStep());
				assertSame(original, target.getKernel().lookupLabel("A")); return new Choice(7, Position.APPEND);
			});
			var result = operation.insert(); assertOutcome(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION, result);
			assertNotNull(result.getFailure()); assertEquals("OBJECT_LISTENERS", result.getFailure().getMessage());
			assertEquals(1, dialogs.get()); assertEquals(0, loads.get()); before.assertRestored(app, true);
			assertNotNull(registered.get()); registered.get().assertIntactAndOperative(app, listenersDisabled);
			before.assertRestored(app, true); return null;
		});
	}

	@Test void globalListenersAndOrdinaryObjectScriptsRemainAdmissibleAndOperative() throws Exception {
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		onEdt(() -> {
			GeoElement owner = app.getKernel().lookupLabel("A");
			GgbScript script = new GgbScript(app, "OrdinaryScriptWitness=41"); owner.setClickScript(script);
			List<String> globalCalls = new ArrayList<>(); var scripts = app.getScriptManager();
			scripts.dropListenersOnReset(); scripts.registerClickListener(callback(globalCalls, "global"));
			assertTrue(scripts.getUpdateListenerMap().isEmpty()); assertTrue(scripts.getClickListenerMap().isEmpty());
			baseline(app); String ownerXml = owner.getXML(); AtomicInteger loads = new AtomicInteger();
			assertInserted(operation(app, 7, Position.APPEND, target -> {
				loads.incrementAndGet(); nativeImages(target, 1); return true;
			}).insert());
			assertEquals(1, loads.get()); assertEquals(ownerXml, owner.getXML());
			assertSame(owner, app.getKernel().lookupLabel("A")); assertSame(script, owner.getScript(EventType.CLICK));
			assertTrue(globalCalls.isEmpty()); assertNull(app.getKernel().lookupLabel("OrdinaryScriptWitness"));
			owner.runClickScripts(null);
			assertEquals(41, assertInstanceOf(GeoNumeric.class,
					app.getKernel().lookupLabel("OrdinaryScriptWitness")).getDouble());
			assertEquals(List.of("global:[A]"), globalCalls); awaitQuiescent(app); return null;
		});
	}

	@Test void anEmptyHostStillDeclaresTheNativeReconstructionInsteadOfVacuousInstanceEquality() throws Exception {
		AppGeoCeDG app = onEdt(() -> { return testApp(); });
		onEdt(() -> {
			baseline(app); assertTrue(geos(app).isEmpty()); State before = new State(app);
			AtomicInteger calls = new AtomicInteger();
			var result = operation(app, 0, Position.APPEND, target -> { calls.incrementAndGet(); return false; }).insert();
			assertEquals(1, calls.get());
			assertOutcome(GeoCeDGDocumentInsertion.Outcome.FAILED_RECOVERY_CERTIFIED_SESSION_RESET, result);
			assertTrue(result.getDeclaredResets().contains("Insert.Reset.Instances"));
			before.assertRestored(app, false); assertTrue(geos(app).isEmpty()); return null;
		});
	}

	@Test void nativeRebuildKeepsGlobalJavaScriptListenersButStillResetsReferenceMemory() throws Exception {
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		onEdt(() -> {
			GeoElement line = must(app, "RememberedLine=Line(A,B)");
			var memory = ((GeoCeDGEuclidianController) app.getActiveEuclidianView()
					.getEuclidianController()).getReferenceMemory();
			memory.remember(GeoCeDGLineReferenceMemory.Slot.PARALLEL, line);
			assertSame(line, memory.peek(GeoCeDGLineReferenceMemory.Slot.PARALLEL)); baseline(app);
			AtomicInteger calls = new AtomicInteger();
			BaseFunction function = new BaseFunction() {
				private static final long serialVersionUID = 1L;
				@Override public Object call(Context context, Scriptable scope, Scriptable self, Object[] arguments) {
					calls.incrementAndGet(); return null;
				}
			};
			var scripts = app.getScriptManager();
			// A real redefine leaves this public mode false. Its default true would
			// hide ScriptManager.reset's listenersEnabled branch and miss this bug.
			scripts.dropListenersOnReset(); scripts.registerAddListener(function);
			app.getEventDispatcher().dispatchEvent(new Event(EventType.ADD, app.getKernel().lookupLabel("A")));
			assertEquals(1, calls.get());
			State before = new State(app);
			AtomicInteger commits = new AtomicInteger(); IllegalStateException injected = new IllegalStateException("native rebuild with registered global listener");
			app.getDocumentInsertion().setFaults(new GeoCeDGDocumentInsertion.Faults() {
				@Override public void afterCommit() { commits.incrementAndGet(); throw injected; }
			});
			var result = operation(app, 7, Position.APPEND, target -> { nativeImages(target, 1); return true; }).insert();
			assertOutcome(GeoCeDGDocumentInsertion.Outcome.FAILED_RECOVERY_CERTIFIED_SESSION_RESET, result);
			assertEquals(1, commits.get()); assertTrue(containsFailure(result.getFailure(), injected));
			before.assertRestored(app, false);
			assertNull(memory.peek(GeoCeDGLineReferenceMemory.Slot.PARALLEL),
					"Native clearView must still invalidate reference memory on reconstructed instances");
			int beforeEvent = calls.get();
			app.getEventDispatcher().dispatchEvent(new Event(EventType.ADD, app.getKernel().lookupLabel("A")));
			assertEquals(beforeEvent + 1, calls.get(), "The original registered global JS callback must remain usable");
			GeoElement currentLine = app.getKernel().lookupLabel("RememberedLine");
			memory.remember(GeoCeDGLineReferenceMemory.Slot.PARALLEL, currentLine);
			scripts.dropListenersOnReset(); app.getEventDispatcher().clearView();
			assertNull(memory.peek(GeoCeDGLineReferenceMemory.Slot.PARALLEL));
			beforeEvent = calls.get();
			app.getEventDispatcher().dispatchEvent(new Event(EventType.ADD, app.getKernel().lookupLabel("A")));
			assertEquals(beforeEvent, calls.get(), "An ordinary reset outside the lexical operation retains native behavior");
			return null;
		});
	}

	@Test void preflightRefusalsNeverInvokeTheNativeLoaderOrRewriteTheHost() throws Exception {
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		onEdt(() -> {
			baseline(app); State before = new State(app); AtomicInteger calls = new AtomicInteger();
			var operation = operation(app, 7, Position.APPEND, target -> { calls.incrementAndGet(); return true; });
			app.getKernel().setUndoActive(false);
			try {
				assertOutcome(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION, operation.insert());
				assertEquals(0, calls.get()); before.assertRestored(app, true);
			} finally { app.getKernel().setUndoActive(true); }
			setAccounting(app, 0, true);
			try {
				assertOutcome(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION, operation.insert());
				assertEquals(0, calls.get()); before.assertRestored(app, true);
			} finally { setAccounting(app, 0, false); }
			setAccounting(app, 1, false);
			try {
				assertOutcome(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION, operation.insert());
				assertEquals(0, calls.get()); before.assertRestored(app, true);
			} finally { setAccounting(app, 0, false); }
			GeoConic undefined = new GeoConic(app.getKernel().getConstruction());
			undefined.setUndefined(); undefined.setLabel("UndefinedHost");
			State unsupported = new State(app);
			var rejected = operation.insert();
			assertOutcome(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION, rejected);
			assertNotNull(rejected.getFailure()); assertTrue(rejected.getFailure().getMessage().contains("UNDEFINED_NATIVE_CONIC"));
			assertEquals(0, calls.get()); unsupported.assertRestored(app, true);
			return null;
		});
	}

	@ParameterizedTest @ValueSource(booleans = {false, true})
	void aModalHostEditIsNotSilentlyRetargetedOrRolledBack(boolean listenersDisabled) throws Exception {
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		onEdt(() -> {
			baseline(app); AtomicInteger calls = new AtomicInteger(); AtomicReference<State> edited = new AtomicReference<>();
			if (listenersDisabled) { app.getEventDispatcher().disableListeners(); }
			var operation = operation(app, 7, Position.AFTER_CURRENT, target -> { calls.incrementAndGet(); return true; });
			operation.setPresentation((target, state) -> {
				if (listenersDisabled) {
					GeoPoint existing = (GeoPoint) target.getKernel().lookupLabel("A");
					existing.setCoords(4, 7, 1); existing.updateRepaint();
					assertEquals(state.currentStep(), target.getKernel().getConstruction().getStep(),
							"An in-place edit must be detected without a different anchor or event delivery");
				} else { must(target, "ExternalEdit=(4,7)"); }
				target.setUnsaved(); edited.set(new State(target));
				return new Choice(7, Position.AFTER_CURRENT);
			});
			var result = operation.insert();
			assertOutcome(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION, result);
			assertEquals(0, calls.get());
			if (listenersDisabled) {
				GeoPoint existing = (GeoPoint) app.getKernel().lookupLabel("A");
				assertEquals(4, existing.inhomX); assertEquals(7, existing.inhomY);
			} else { assertNotNull(app.getKernel().lookupLabel("ExternalEdit")); }
			edited.get().assertRestored(app, true); return null;
		});
	}

	@ParameterizedTest @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
	void everyPartialNativeStageHasCertifiedMemoryRecoveryWithoutOrphans(int stage) throws Exception {
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		onEdt(() -> {
			baseline(app); app.getKernel().getConstruction().setStep(0);
			State before = new State(app); List<GeoPoint> madePoints = new ArrayList<>();
			AtomicReference<GeoImage> madeImage = new AtomicReference<>(); AtomicInteger calls = new AtomicInteger();
			IllegalStateException injected = new IllegalStateException("partial native stage " + stage);
			var result = operation(app, 7, Position.APPEND, target -> {
				calls.incrementAndGet();
				if (stage == 0) { return false; }
				GeoPoint first = corner(target, 1, 1); madePoints.add(first);
				if (stage == 6) { return true; } // A native boolean is insufficient without a complete 3N delta.
				if (stage == 1) { throw injected; }
				GeoPoint second = corner(target, 3, 1); madePoints.add(second);
				if (stage == 2) { throw injected; }
				GeoImage image = new GeoImage(target.getKernel().getConstruction()); madeImage.set(image);
				if (stage == 3) { throw injected; }
				target.getImageManager().addExternalImage("partial-image.png", new MyImageD(pixels(67)));
				image.setImageFileName("partial-image.png");
				image.setCorner(stage == 7 ? (GeoPoint) target.getKernel().lookupLabel("A") : first, 0);
				image.setCorner(second, 1);
				if (stage == 4) { throw injected; }
				image.setLabel(null); return stage == 7; // 3N objects, but an old selected point is an inadmissible corner.
			}).insert();
			assertEquals(1, calls.get());
			assertOutcome(GeoCeDGDocumentInsertion.Outcome.FAILED_RECOVERY_CERTIFIED_SESSION_RESET, result);
			if (stage >= 1 && stage <= 4) { assertTrue(containsFailure(result.getFailure(), injected)); }
			if (stage == 0 || stage == 5) { assertTrue(containsCauseCode(result.getFailure(), app.layerText("ClipboardImage.BadClipboard"))); }
			if (stage == 6) { assertTrue(containsCauseCode(result.getFailure(), "Clipboard image payload is incomplete")); }
			if (stage == 7) { assertTrue(containsCauseCode(result.getFailure(), "Clipboard image corners, order or resource are invalid")); }
			assertTrue(result.getDeclaredResets().contains("Insert.Reset.Instances"));
			before.assertRestored(app, false);
			assertNotSame(before.observation.certificate.instances.get("A"), app.getKernel().lookupLabel("A"));
			if (madeImage.get() != null) { assertFalse(app.images.contains(madeImage.get())); }
			for (GeoPoint point : madePoints) {
				assertFalse(geos(app).contains(point)); assertTrue(point.getLocateableList().isEmpty());
			}
			return null;
		});
	}

	@ParameterizedTest @ValueSource(ints = {0, 1, 2})
	void commitStoreAndAttachFaultsKeepOriginalHistoryAndDeclareTheMemoryReset(int location) throws Exception {
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		onEdt(() -> {
			baseline(app); app.getKernel().getConstruction().setStep(0); State before = new State(app);
			AtomicBoolean attachFailed = new AtomicBoolean(); AtomicInteger commits = new AtomicInteger(), stores = new AtomicInteger(), attaches = new AtomicInteger();
			IllegalStateException injected = new IllegalStateException("R12 exact fault " + location);
			app.getDocumentInsertion().setFaults(new GeoCeDGDocumentInsertion.Faults() {
				@Override public void afterCommit() { commits.incrementAndGet(); if (location == 0) { throw injected; } }
				@Override public void beforeOwnStore() { stores.incrementAndGet(); if (location == 1) { throw injected; } }
				@Override public void beforeAttach() {
					attaches.incrementAndGet();
					if (location == 2 && attachFailed.compareAndSet(false, true)) {
						throw injected;
					}
				}
			});
			var result = operation(app, 7, Position.APPEND, target -> { nativeImages(target, 2); return true; }).insert();
			assertOutcome(GeoCeDGDocumentInsertion.Outcome.FAILED_RECOVERY_CERTIFIED_SESSION_RESET, result);
			assertNotNull(result.getFailure()); assertTrue(result.getDeclaredResets().contains("Insert.Reset.Instances"));
			assertEquals(1, commits.get()); assertEquals(location == 0 ? 0 : 1, stores.get());
			assertEquals(location == 2 ? 1 : 0, attaches.get()); assertTrue(containsFailure(result.getFailure(), injected));
			before.assertRestored(app, false); return null;
		});
	}

	@ParameterizedTest @ValueSource(booleans = {false, true})
	void failedSubtractionOrCertificateUsesTheNativeReloadAndDeclaresItsReset(boolean certificateFault) throws Exception {
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		onEdt(() -> {
			baseline(app); State before = new State(app); AtomicInteger certificates = new AtomicInteger(), reloads = new AtomicInteger(), commits = new AtomicInteger();
			byte[] fullArchive = nativeArchive(app);
			try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(fullArchive))) {
				assertNotEquals("geogebra.xml", zip.getNextEntry().getName(), "This is the complete native ZIP, not an Undo ZIP");
				boolean found = false; for (ZipEntry entry; (entry = zip.getNextEntry()) != null;) { found |= "geogebra.xml".equals(entry.getName()); }
				assertTrue(found);
			}
			Files.write(directory.resolve("complete-native-before.cedg"), fullArchive);
			System.out.println("CLIPBOARD_R12_FULL_ARCHIVE " + directory.toAbsolutePath());
			IllegalStateException injected = new IllegalStateException("force image recovery");
			app.getDocumentInsertion().setFaults(new GeoCeDGDocumentInsertion.Faults() {
				@Override public void afterCommit() { commits.incrementAndGet(); throw injected; }
				@Override public void duringRemoval() { if (!certificateFault) { throw new IllegalStateException("force native reload"); } }
				@Override public void beforeReload(int attempt) { reloads.incrementAndGet(); }
				@Override public void beforeCertificate() {
					int ordinal = certificates.getAndIncrement();
					if (certificateFault && ordinal == 0) { throw new IllegalStateException("first certificate only"); }
				}
			});
			var result = operation(app, 7, Position.APPEND, target -> { nativeImages(target, 1); return true; }).insert();
			assertOutcome(GeoCeDGDocumentInsertion.Outcome.FAILED_RECOVERY_CERTIFIED_SESSION_RESET, result);
			assertTrue(result.getDeclaredResets().contains("Insert.Reset.Instances"));
			assertEquals(1, commits.get()); assertEquals(1, reloads.get());
			assertTrue(containsFailure(result.getFailure(), injected));
			assertEquals(certificateFault ? 2 : 1, certificates.get());
			assertFalse(before.defaults.samePrototypes(app)); before.defaults.assertArchive(app);
			before.assertRestored(app, false); assertFalse(app.isInsertionIsolated()); return null;
		});
	}

	@Test void uncertifiedRecoveryTransfersTheVerifiedSnapshotToIsolationWithoutOverwritingTheFile() throws Exception {
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		File file = onEdt(() -> { return save(app, directory, "before-image-isolation"); });
		byte[] bytes = Files.readAllBytes(file.toPath());
		onEdt(() -> {
			app.setCurrentFile(file); baseline(app); AtomicBoolean isolated = new AtomicBoolean();
			AtomicInteger commits = new AtomicInteger(), removals = new AtomicInteger(), reloads = new AtomicInteger();
			IllegalStateException injected = new IllegalStateException("force isolation");
			app.getDocumentInsertion().setFaults(new GeoCeDGDocumentInsertion.Faults() {
				@Override public void afterCommit() { commits.incrementAndGet(); throw injected; }
				@Override public void duringRemoval() { removals.incrementAndGet(); throw new IllegalStateException("no subtraction"); }
				@Override public void beforeReload(int attempt) { reloads.incrementAndGet(); throw new IllegalStateException("no reload " + attempt); }
			});
			var dialog = new PreG9BR6PlusF3RecoveryTest.Scripted();
			dialog.exits.add(GeoCeDGInsertionIsolation.Exit.CLOSE);
			dialog.probe = () -> {
				assertTrue(app.isInsertionIsolated()); assertNotNull(app.getInsertionIsolation().getSnapshot());
				assertFalse(app.getKernel().isUndoActive()); assertTrue(app.isBlockUpdateScripts());
				AtomicInteger forbidden = new AtomicInteger();
				var blocked = operation(app, 1, Position.APPEND, target -> { forbidden.incrementAndGet(); return true; }).insert();
				assertOutcome(GeoCeDGDocumentInsertion.Outcome.REJECTED_BEFORE_MUTATION, blocked);
				assertEquals(0, forbidden.get()); isolated.set(true);
			};
			app.getInsertionIsolation().setPresentation(dialog);
			app.getInsertionIsolation().setCloser(target -> { }); // Test-owned embedded host: observe the exit without closing another window.
			var result = operation(app, 7, Position.APPEND, target -> { nativeImages(target, 1); return true; }).insert();
			assertOutcome(GeoCeDGDocumentInsertion.Outcome.SERIOUS_RECOVERY_FAILURE, result);
			assertEquals(1, commits.get()); assertEquals(1, removals.get()); assertEquals(2, reloads.get()); assertTrue(containsFailure(result.getFailure(), injected));
			assertTrue(isolated.get()); assertEquals(1, dialog.choices); return null;
		});
		assertArrayEquals(bytes, Files.readAllBytes(file.toPath()));
	}

	@ParameterizedTest @ValueSource(booleans = {false, true})
	void lexicalEventsAndOperationFlagsRestoreTheirActualPreviousState(boolean initiallyEnabled) throws Exception {
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		onEdt(() -> {
			baseline(app); var dispatcher = app.getEventDispatcher(); AtomicInteger events = new AtomicInteger();
			GeoElement target = app.getKernel().lookupLabel("A");
			EventListener listener = event -> { if (event.type == EventType.UPDATE) { events.incrementAndGet(); } };
			dispatcher.addEventListener(listener);
			if (initiallyEnabled) { dispatcher.enableListeners(); } else { dispatcher.disableListeners(); }
			app.setScriptingDisabled(!initiallyEnabled); app.setBlockUpdateScripts(!initiallyEnabled);
			try {
				try (var outer = app.silenceClipboardEvents()) {
					dispatcher.enableListeners(); dispatcher.dispatchEvent(new Event(EventType.UPDATE, target)); assertEquals(0, events.get());
					assertThrows(IllegalStateException.class, () -> {
						try (var inner = app.silenceClipboardEvents()) {
							dispatcher.enableListeners(); dispatcher.dispatchEvent(new Event(EventType.UPDATE, target));
							throw new IllegalStateException("nested lexical scope");
						}
					});
					dispatcher.dispatchEvent(new Event(EventType.UPDATE, target)); assertEquals(0, events.get());
				}
				dispatcher.dispatchEvent(new Event(EventType.UPDATE, target)); assertEquals(initiallyEnabled ? 1 : 0, events.get());
				events.set(0);
				assertInserted(operation(app, 7, Position.APPEND, nativeApp -> {
					dispatcher.enableListeners(); dispatcher.dispatchEvent(new Event(EventType.UPDATE, target));
					assertEquals(0, events.get(), "Unverified creation cannot publish callbacks even if a reader enables listeners");
					nativeImages(nativeApp, 1); return true;
				}).insert());
				assertEquals(!initiallyEnabled, app.isScriptingDisabled()); assertEquals(!initiallyEnabled, app.isBlockUpdateScripts());
				events.set(0); dispatcher.dispatchEvent(new Event(EventType.UPDATE, target));
				assertEquals(initiallyEnabled ? 1 : 0, events.get());
			} finally { dispatcher.removeEventListener(listener); dispatcher.enableListeners(); }
			return null;
		});
	}

	@ParameterizedTest @ValueSource(booleans = {false, true})
	void imageRecoveryPreservesAnExistingNativePathAndControlLayout(boolean forceNativeReload) throws Exception {
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		onEdt(() -> {
			must(app, "NativeCircle=Circle(A,2)"); GeoPoint place = (GeoPoint) must(app, "NativePlace=Point(NativeCircle)");
			var nativeState = place.captureNativeRollbackState(); assertNotNull(nativeState);
			GeoElement control = must(app, "ImageControl=Button()");
			AbsoluteScreenLocateable screen = (AbsoluteScreenLocateable) control;
			screen.setAbsoluteScreenLocActive(true); screen.setAbsoluteScreenLoc(10, 10); control.setEuclidianVisible(true);
			var layout = app.getKernel().getConstruction().getControlLayout();
			var panel = layout.addPanel(ControlLayout.Anchor.FIXED, 0, 0, 150, 90, true);
			var slot = layout.addSlot(panel.id(), control, 4, 6);
			baseline(app); State before = new State(app); assertNotNull(app.getKernel().getConstruction().captureNativeUndoSnapshot());
			AtomicInteger loads = new AtomicInteger(), commits = new AtomicInteger(), reloads = new AtomicInteger();
			IllegalStateException injected = new IllegalStateException("image rollback with existing layout");
			app.getDocumentInsertion().setFaults(new GeoCeDGDocumentInsertion.Faults() {
				@Override public void afterCommit() { commits.incrementAndGet(); throw injected; }
				@Override public void duringRemoval() { if (forceNativeReload) { throw new IllegalStateException("force companion reload"); } }
				@Override public void beforeReload(int attempt) { reloads.incrementAndGet(); }
			});
			var result = operation(app, 7, Position.APPEND, target -> {
				loads.incrementAndGet(); UndoCommand held = undo(target).getCommands().get(0);
				assertInstanceOf(GeoCeDGUndoManager.NativeFileAppState.class, held.getAppState());
				try {
					String privateXml = new String(onlyUndoXml(undoArchive(held)), StandardCharsets.UTF_8);
					assertTrue(privateXml.contains("<nativeRollbackBinding"));
					assertFalse(undo(target).publicUndoXml(held, privateXml).contains("<nativeRollbackBinding"));
				} catch (IOException failure) { throw new IllegalStateException("Cannot observe the owned baseline", failure); }
				nativeImages(target, 1); return true;
			}).insert();
			assertOutcome(GeoCeDGDocumentInsertion.Outcome.FAILED_RECOVERY_CERTIFIED_SESSION_RESET, result);
			assertEquals(1, loads.get()); assertEquals(1, commits.get()); assertEquals(forceNativeReload ? 1 : 0, reloads.get());
			assertTrue(containsFailure(result.getFailure(), injected)); before.defaults.assertConstruction(app);
			assertTrue(result.getDeclaredResets().contains("Insert.Reset.Instances")); before.assertRestored(app, false);
			GeoPoint currentPlace = (GeoPoint) app.getKernel().lookupLabel("NativePlace");
			assertNotSame(place, currentPlace); assertTrue(nativeState.matches(currentPlace));
			var currentLayout = app.getKernel().getConstruction().getControlLayout();
			GeoElement currentControl = app.getKernel().lookupLabel("ImageControl");
			assertEquals(slot.id(), currentLayout.slot(currentControl).id());
			assertEquals(panel.id(), currentLayout.panels().get(0).id());
			byte[] saved = nativeArchive(app);
			try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(saved))) {
				boolean found = false;
				for (ZipEntry entry; (entry = zip.getNextEntry()) != null;) {
					if ("geogebra.xml".equals(entry.getName())) { found = true; assertFalse(new String(zip.readAllBytes(), StandardCharsets.UTF_8).contains("<nativeRollbackBinding")); }
				}
				assertTrue(found);
			}
			return null;
		});
	}

	@ParameterizedTest @ValueSource(booleans = {false, true})
	void theRealNativeLoaderRejectsPlainTextAndVerifiedZeroFlavorClipboardContents(boolean noFormats) throws Exception {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable original = clipboard.getContents(null);
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		try {
			try {
				clipboard.setContents(noFormats ? new NoFormats()
						: new StringSelection("GeoCeDG incompatible clipboard: plain text only"), null);
			} catch (IllegalStateException unavailable) {
				throw new IllegalStateException("ENVIRONMENT: Windows clipboard cannot install the controlled test content", unavailable);
			}
			DataFlavor[] actualFlavors = clipboard.getAvailableDataFlavors();
			if (noFormats) {
				assertEquals(0, actualFlavors.length,
						"ENVIRONMENT: Windows did not expose the requested zero-flavor clipboard; do not call this a real empty case");
			} else {
				assertTrue(clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor));
				assertFalse(clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor));
			}
			System.out.println("CLIPBOARD_REAL_NATIVE noFormats=" + noFormats + " actualFlavorCount=" + actualFlavors.length);
			onEdt(() -> {
				baseline(app); app.getKernel().getConstruction().setStep(0); State before = new State(app);
				AtomicInteger calls = new AtomicInteger();
				var result = operation(app, 7, Position.APPEND, target -> {
					calls.incrementAndGet(); return ((GuiManagerD) target.getGuiManager()).loadImage(null, true);
				}).insert();
				assertEquals(1, calls.get());
				assertOutcome(GeoCeDGDocumentInsertion.Outcome.FAILED_RECOVERY_CERTIFIED_SESSION_RESET, result);
				assertTrue(result.getDeclaredResets().contains("Insert.Reset.Instances")); before.assertRestored(app, false);
				return null;
			});
		} finally { restoreClipboard(clipboard, original); }
	}
}
