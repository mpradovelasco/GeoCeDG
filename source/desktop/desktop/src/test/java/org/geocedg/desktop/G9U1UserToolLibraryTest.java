/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.awt.DefaultKeyboardFocusManager;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JMenu;
import javax.swing.JPanel;

import org.geocedg.desktop.GeoCeDGUserToolLibrary.Package;
import org.geogebra.common.io.XMLParseException;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.io.MyXMLioD;
import org.geogebra.desktop.main.AppD;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Self-authored native macro fixtures; never reads or writes real user preferences. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class G9U1UserToolLibraryTest {

	@TempDir
	Path temporary;
	private AppGeoCeDG app;
	private GeoCeDGUserToolLibrary library;
	private Path storage;

	@BeforeEach
	void setup() throws IOException {
		app = G9U1TestApp.create();
		storage = temporary.resolve("tools.json");
		library = new GeoCeDGUserToolLibrary(app, storage);
	}

	@Test
	void installDoesNotChangeConstructionOrDocumentArchive() throws Exception {
		G9U1TestApp.eval(app, "P=(3,4)");
		String before = app.getXML();
		byte[] beforeArchive = archiveXml(app);
		Package tool = library.install("owned.ggt", midpointPackage("OwnedMidpoint"));
		assertEquals(List.of("OwnedMidpoint"), tool.commands());
		assertEquals(before, app.getXML());
		assertArrayEquals(beforeArchive, archiveXml(app));
		assertEquals(0, app.getKernel().getMacroNumber());
		assertTrue(Files.exists(storage));
	}

	@Test
	void restartAndNewDocumentRetainLibraryWithoutRegisteringDocumentMacros() throws Exception {
		Package tool = library.install("owned.ggt", midpointPackage("OwnedMidpoint"));
		AppGeoCeDG restarted = G9U1TestApp.create();
		GeoCeDGUserToolLibrary reloaded = new GeoCeDGUserToolLibrary(restarted, storage);
		assertEquals(tool.id(), reloaded.packages().get(0).id());
		assertEquals(0, restarted.getKernel().getMacroNumber());
		restarted.getKernel().clearConstruction(true);
		assertEquals(tool.id(), reloaded.packages().get(0).id());
		assertEquals(0, restarted.getKernel().getMacroNumber());
	}

	@Test
	void explicitActivationUsesExistingMacroEngineAndDynamicInputs() throws Exception {
		Package tool = library.install("owned.ggt", midpointPackage("OwnedMidpoint"));
		final GeoPoint p = (GeoPoint) G9U1TestApp.eval(app, "P=(4,2)");
		final GeoPoint q = (GeoPoint) G9U1TestApp.eval(app, "Q=(8,6)");
		String constructionBefore = app.getXML();
		Macro macro = library.activate(tool.id(), "OwnedMidpoint");
		// Host ToolManager edits/removes via macro.getKernel(), not its registration lookup.
		assertSame(app.getKernel(), macro.getKernel());
		assertSame(macro, library.activate(tool.id(), "OwnedMidpoint"));
		assertEquals(constructionBefore, app.getXML());
		// The same owner used by the inherited ToolManager removes the actual registration.
		macro.getKernel().removeMacro(macro);
		assertNull(app.getKernel().getMacro("OwnedMidpoint"));
		macro = library.activate(tool.id(), "OwnedMidpoint");
		assertSame(app.getKernel(), macro.getKernel());
		assertEquals(1, app.getKernel().getMacroNumber());
		GeoElement[] output = app.getKernel().useMacro(new String[] {"R"}, macro,
				new GeoElement[] {p, q});
		assertNotNull(output);
		GeoPoint result = (GeoPoint) output[0];
		assertEquals(6, result.getInhomX(), 1e-12);
		assertEquals(4, result.getInhomY(), 1e-12);
		q.setCoords(12, 10, 1);
		q.updateRepaint();
		assertEquals(8, result.getInhomX(), 1e-12);
		assertEquals(6, result.getInhomY(), 1e-12);
	}

	@Test
	void installedToolIsInvocableThroughNativeCommandDispatcher() throws Exception {
		Package tool = library.install("owned.ggt", midpointPackage("OwnedMidpoint"));
		library.activate(tool.id(), "OwnedMidpoint");
		G9U1TestApp.eval(app, "P=(2,0)");
		G9U1TestApp.eval(app, "Q=(6,4)");
		GeoPoint result = (GeoPoint) G9U1TestApp.eval(app, "R=OwnedMidpoint(P,Q)");
		assertEquals(4, result.getInhomX(), 1e-12);
		assertEquals(2, result.getInhomY(), 1e-12);
	}

	@Test
	void pinsAreApplicationPreferencesAndDoNotChangeConstruction() throws Exception {
		Package tool = library.install("owned.ggt", midpointPackage("OwnedMidpoint"));
		String before = app.getXML();
		library.pin(tool.id(), "OwnedMidpoint", true);
		assertTrue(new GeoCeDGUserToolLibrary(app, storage).packages().get(0)
				.isPinned("OwnedMidpoint"));
		library.pin(tool.id(), "OwnedMidpoint", false);
		assertFalse(new GeoCeDGUserToolLibrary(app, storage).packages().get(0)
				.isPinned("OwnedMidpoint"));
		assertEquals(before, app.getXML());
	}

	@Test
	void removalDoesNotDeleteExistingDocumentMacroOrOutput() throws Exception {
		Package tool = library.install("owned.ggt", midpointPackage("OwnedMidpoint"));
		Macro macro = library.activate(tool.id(), "OwnedMidpoint");
		GeoElement p = G9U1TestApp.eval(app, "P=(0,0)");
		GeoElement q = G9U1TestApp.eval(app, "Q=(4,0)");
		GeoElement result = app.getKernel().useMacro(new String[] {"R"}, macro,
				new GeoElement[] {p, q})[0];
		String before = app.getXML();
		library.remove(tool.id());
		assertTrue(new GeoCeDGUserToolLibrary(app, storage).packages().isEmpty());
		assertSame(macro, app.getKernel().getMacro("OwnedMidpoint"));
		assertSame(result, G9U1TestApp.lookup(app, "R"));
		assertTrue(result.isDefined());
		assertEquals(before, app.getXML());
	}

	@Test
	void documentMacrosNeverAutoInstallOrBecomePreferenceSnapshot() throws Exception {
		app.loadMacroFileFromByteArray(midpointPackage("DocumentOnly"), false);
		assertNotNull(app.getKernel().getMacro("DocumentOnly"));
		assertTrue(library.packages().isEmpty());
		assertFalse(Files.exists(storage));
		String snapshot = zipXml(app.getMacroFileAsByteArray(), "geogebra_macro.xml");
		assertFalse(snapshot.contains("cmdName=\"DocumentOnly\""));
		assertNotNull(app.getKernel().getMacro("DocumentOnly"));
	}

	@Test
	void preferenceRestoreCannotSmuggleDocumentMacrosButExplicitLoadStillWorks() throws Exception {
		byte[] bytes = midpointPackage("DocumentOnly");
		app.loadMacroFileFromByteArray(bytes, true);
		assertEquals(0, app.getKernel().getMacroNumber());
		app.loadMacroFileFromByteArray(bytes, false);
		Macro macro = app.getKernel().getMacro("DocumentOnly");
		assertNotNull(macro);
		app.loadMacroFileFromByteArray(bytes, true);
		assertSame(macro, app.getKernel().getMacro("DocumentOnly"));
		assertTrue(library.packages().isEmpty());
	}

	@Test
	void classicRetainsInheritedPreferenceMacroContract() throws Exception {
		AppD classic = new AppD(new CommandLineArguments(new String[] {"--silent"}),
				new JPanel(), false);
		G9U1TestApp.withoutWindowDispatcher(classic);
		classic.setErrorDialogsActive(false);
		classic.loadMacroFileFromByteArray(midpointPackage("DocumentOnly"), true);
		assertNotNull(classic.getKernel().getMacro("DocumentOnly"));
		assertTrue(zipXml(classic.getMacroFileAsByteArray(), "geogebra_macro.xml")
				.contains("cmdName=\"DocumentOnly\""));
		assertThrows(IllegalArgumentException.class,
				() -> new GeoCeDGUserToolLibrary(classic, temporary.resolve("classic.json")));
	}

	@Test
	void windowlessFixtureReleasesGlobalDispatcherWithoutChangingNativeRegistration()
			throws Exception {
		final KeyboardFocusManager previous = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		InspectableFocusManager isolated = new InspectableFocusManager();
		KeyboardFocusManager.setCurrentKeyboardFocusManager(isolated);
		try {
			AppD classic = new AppD(new CommandLineArguments(new String[] {"--silent"}),
					new JPanel(), false);
			assertTrue(isolated.contains(classic));
			assertSame(classic, G9U1TestApp.withoutWindowDispatcher(classic));
			assertFalse(isolated.contains(classic));
			AppGeoCeDG product = G9U1TestApp.create();
			assertFalse(isolated.contains(product));
			midpointPackage("TemporaryToolHost");
			assertEquals(0, isolated.dispatcherCount());
		} finally {
			KeyboardFocusManager.setCurrentKeyboardFocusManager(previous);
		}
		assertSame(previous, KeyboardFocusManager.getCurrentKeyboardFocusManager());
	}

	private static final class InspectableFocusManager extends DefaultKeyboardFocusManager {
		boolean contains(KeyEventDispatcher dispatcher) {
			List<KeyEventDispatcher> registered = getKeyEventDispatchers();
			return registered != null && registered.contains(dispatcher);
		}

		int dispatcherCount() {
			List<KeyEventDispatcher> registered = getKeyEventDispatchers();
			return registered == null ? 0 : registered.size();
		}
	}

	@Test
	void nativeLengthCollisionFailsClosed() throws Exception {
		assertNativeCollisionRejected("Length");
	}

	@Test
	void nativeIntersectCollisionFailsClosed() throws Exception {
		assertNativeCollisionRejected("Intersect");
	}

	@Test
	void nativePointCollisionFailsClosed() throws Exception {
		assertNativeCollisionRejected("Point");
	}

	@Test
	void nativeSplineV2CollisionFailsClosed() throws Exception {
		assertNativeCollisionRejected("SplineV2");
	}

	@Test
	void nativeMidpointCollisionFailsClosed() throws Exception {
		assertNativeCollisionRejected("Midpoint");
	}

	private void assertNativeCollisionRejected(String nativeName) throws Exception {
		byte[] proposed = changeXml(midpointPackage("OwnedMidpoint"),
				xml -> xml.replace("cmdName=\"OwnedMidpoint\"", "cmdName=\"" + nativeName + "\""));
		assertRejectedWithoutMutation(proposed);
	}

	@Test
	void installedCommandCollisionPreservesOriginalLibraryBytes() throws Exception {
		byte[] original = midpointPackage("OwnedMidpoint");
		library.install("first.ggt", original);
		byte[] before = Files.readAllBytes(storage);
		assertThrows(IOException.class, () -> library.install("second.ggt", original));
		assertArrayEquals(before, Files.readAllBytes(storage));
		assertEquals(1, library.packages().size());
	}

	@Test
	void secondWindowRechecksInstalledCommandCollisionAgainstCurrentStore() throws Exception {
		AppGeoCeDG otherApp = G9U1TestApp.create();
		GeoCeDGUserToolLibrary other = new GeoCeDGUserToolLibrary(otherApp, storage);
		library.install("first.ggt", midpointPackage("OwnedMidpoint"));
		byte[] before = Files.readAllBytes(storage);
		assertThrows(IOException.class,
				() -> other.install("conflicting.ggt", midpointPackage("OwnedMidpoint")));
		assertArrayEquals(before, Files.readAllBytes(storage));
		assertEquals(1, other.packages().size());
		assertEquals(0, otherApp.getKernel().getMacroNumber());
	}

	@Test
	void concurrentWindowPinAndRemovePreserveOtherPackagesAndRejectRemovedActivation()
			throws Exception {
		AppGeoCeDG otherApp = G9U1TestApp.create();
		GeoCeDGUserToolLibrary other = new GeoCeDGUserToolLibrary(otherApp, storage);
		Package first = library.install("first.ggt", midpointPackage("FirstMidpoint"));
		Package second = other.install("second.ggt", midpointPackage("SecondMidpoint"));
		library.pin(first.id(), "FirstMidpoint", true);
		assertEquals(2, new GeoCeDGUserToolLibrary(app, storage).packages().size());
		JMenu menu = new JMenu();
		new GeoCeDGUserTools(app, library).populate(menu);
		assertEquals("SecondMidpoint", menu.getItem(3).getText());
		other.remove(second.id());
		GeoCeDGUserToolLibrary reopened = new GeoCeDGUserToolLibrary(app, storage);
		assertEquals(1, reopened.packages().size());
		assertEquals(first.id(), reopened.packages().get(0).id());
		assertTrue(reopened.packages().get(0).isPinned("FirstMidpoint"));
		// This window saw SecondMidpoint earlier but must not activate a stale cached entry.
		assertThrows(IOException.class, () -> library.activate(second.id(), "SecondMidpoint"));
		assertEquals(0, app.getKernel().getMacroNumber());
		byte[] before = Files.readAllBytes(storage);
		Files.writeString(storage, "{\"version\":1,\"packages\":[{}]}");
		assertThrows(IOException.class, library::refresh);
		assertEquals(first.id(), library.packages().get(0).id());
		assertTrue(library.packages().get(0).isPinned("FirstMidpoint"));
		Files.write(storage, before);
	}

	@Test
	void busyLibraryLockRejectsWithoutReplacingPreferences() throws Exception {
		Package first = library.install("first.ggt", midpointPackage("FirstMidpoint"));
		byte[] before = Files.readAllBytes(storage);
		Path lockPath = storage.resolveSibling(storage.getFileName() + ".lock");
		try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE,
				StandardOpenOption.WRITE); FileLock lock = channel.tryLock()) {
			assertNotNull(lock);
			IOException rejected = assertThrows(IOException.class,
					() -> library.pin(first.id(), "FirstMidpoint", true));
			assertEquals("UserTools.LibraryBusy", rejected.getMessage());
			assertArrayEquals(before, Files.readAllBytes(storage));
			assertFalse(first.isPinned("FirstMidpoint"));
		}
	}

	@Test
	void documentCollisionAtInstallOrActivationDoesNotReplaceMacro() throws Exception {
		byte[] bytes = midpointPackage("OwnedMidpoint");
		Package tool = library.install("owned.ggt", bytes);
		app.loadMacroFileFromByteArray(bytes, false);
		Macro document = app.getKernel().getMacro("OwnedMidpoint");
		assertNotNull(document);
		assertEquals("UserTools.DocumentConflict", library.unavailableReason(tool));
		assertThrows(IOException.class, () -> library.activate(tool.id(), "OwnedMidpoint"));
		assertSame(document, app.getKernel().getMacro("OwnedMidpoint"));
		GeoCeDGUserToolLibrary other = new GeoCeDGUserToolLibrary(app,
				temporary.resolve("other.json"));
		assertThrows(IOException.class, () -> other.install("owned.ggt", bytes));
		assertTrue(other.packages().isEmpty());
		Package pair = library.install("pair.ggt",
				midpointPackage("FirstMidpoint", "SecondMidpoint"));
		final Macro first = library.activate(pair.id(), "FirstMidpoint");
		Macro second = app.getKernel().getMacro("SecondMidpoint");
		assertSame(app.getKernel(), second.getKernel());
		second.getKernel().removeMacro(second);
		String before = app.getXML();
		final byte[] preferences = Files.readAllBytes(storage);
		assertEquals("UserTools.DocumentConflict", library.unavailableReason(pair));
		assertThrows(IOException.class, () -> library.activate(pair.id(), "FirstMidpoint"));
		assertSame(first, app.getKernel().getMacro("FirstMidpoint"));
		assertSame(document, app.getKernel().getMacro("OwnedMidpoint"));
		assertNull(app.getKernel().getMacro("SecondMidpoint"));
		assertEquals(2, app.getKernel().getMacroNumber());
		assertEquals(before, app.getXML());
		assertArrayEquals(preferences, Files.readAllBytes(storage));
	}

	@Test
	void malformedArchiveAndDoctypeFailWithoutMutation() throws Exception {
		assertRejectedWithoutMutation(new byte[] {1, 2, 3});
		assertRejectedWithoutMutation(ggt("<geogebra><macro></geogebra>"));
		String xml = zipXml(midpointPackage("OwnedMidpoint"), "geogebra_macro.xml");
		int opening = xml.indexOf("<geogebra");
		assertRejectedWithoutMutation(ggt(xml.substring(0, opening)
				+ "<!DOCTYPE geogebra [<!ENTITY remote SYSTEM 'file:///absent'>]>"
				+ xml.substring(opening)));
		assertActivationRollback(false);
		assertActivationRollback(true);
	}

	private void assertActivationRollback(boolean parserThrows) throws Exception {
		AppGeoCeDG host = spy(G9U1TestApp.create());
		host.loadMacroFileFromByteArray(midpointPackage("ExistingMidpoint"), false);
		final Macro existing = host.getKernel().getMacro("ExistingMidpoint");
		G9U1TestApp.eval(host, "P=(7,9)");
		GeoCeDGUserToolLibrary tools = new GeoCeDGUserToolLibrary(host,
				temporary.resolve("rollback-" + parserThrows + ".json"));
		Package tool = tools.install("owned.ggt", midpointPackage("OwnedMidpoint"));
		final String before = host.getXML();
		MyXMLioD xmlio = spy(host.getXMLio());
		doReturn(xmlio).when(host).getXMLio();
		doAnswer(invocation -> {
			invocation.callRealMethod();
			host.getKernel().setMacroCommandName(host.getKernel().getMacro("OwnedMidpoint"),
					"UnexpectedMidpoint");
			if (parserThrows) {
				throw new XMLParseException("Injected registration failure");
			}
			return null;
		}).when(xmlio).processXMLString(anyString(), eq(false), eq(true), eq(false));
		assertThrows(IOException.class, () -> tools.activate(tool.id(), "OwnedMidpoint"));
		assertSame(existing, host.getKernel().getMacro("ExistingMidpoint"));
		assertNull(host.getKernel().getMacro("OwnedMidpoint"));
		assertNull(host.getKernel().getMacro("UnexpectedMidpoint"));
		assertEquals(1, host.getKernel().getMacroNumber());
		assertEquals(before, host.getXML());
	}

	@Test
	void scriptsAndSerializedSemanticObjectsAreRejected() throws Exception {
		byte[] original = midpointPackage("OwnedMidpoint");
		assertRejectedWithoutMutation(changeXml(original, xml -> xml.replace("</construction>",
				"<ggbscript val=\"SetValue(a,1)\"/></construction>")));
		assertRejectedWithoutMutation(changeXml(original,
				xml -> xml.replace("type=\"point\"", "type=\"locusv2\"")));
		assertRejectedWithoutMutation(changeXml(original,
				xml -> xml.replace("type=\"point\"", "type=\"point3d\"")));
		assertRejectedWithoutMutation(changeXml(original, xml -> xml.replace("</construction>",
				"<geocedgSpatial><object id=\"bad\"/></geocedgSpatial></construction>")));
	}

	@Test
	void featureDisabledBodyCannotBypassPolicyThroughFileLoadOrExpression() throws Exception {
		AppGeoCeDG disabled = G9U1TestApp.create(false);
		GeoCeDGUserToolLibrary disabledLibrary = new GeoCeDGUserToolLibrary(disabled,
				temporary.resolve("disabled.json"));
		byte[] original = midpointPackage("OwnedMidpoint");
		byte[] command = changeXml(original,
				xml -> xml.replace("name=\"Midpoint\"", "name=\"SplineV2\""));
		assertTrue(assertThrows(IOException.class,
				() -> disabledLibrary.install("off.ggt", command)).getMessage()
				.contains("LocusV2.FeatureDisabled"));
		byte[] expression = changeXml(original, xml -> xml.replace("</construction>",
				"<expression label=\"x\" exp=\"LocusV2(a,b)\"/></construction>"));
		assertThrows(IOException.class, () -> disabledLibrary.install("off.ggt", expression));
		assertEquals(0, disabled.getKernel().getMacroNumber());
		assertTrue(disabledLibrary.packages().isEmpty());
	}

	@Test
	void tamperedStoredPackageFailsClosedAndIsNotRewritten() throws Exception {
		Package tool = library.install("owned.ggt", midpointPackage("OwnedMidpoint"));
		String tampered = Files.readString(storage).replace(tool.id(), "0".repeat(64));
		Files.writeString(storage, tampered);
		byte[] before = Files.readAllBytes(storage);
		assertThrows(IOException.class, () -> new GeoCeDGUserToolLibrary(app, storage));
		assertArrayEquals(before, Files.readAllBytes(storage));
		assertEquals(0, app.getKernel().getMacroNumber());
	}

	@Test
	void dynamicMenuAndPinsReuseManagementActionWithoutAddingProductIds() throws Exception {
		Package tool = library.install("owned.ggt", midpointPackage("OwnedMidpoint"));
		GeoCeDGUserTools presentation = new GeoCeDGUserTools(app, library);
		JMenu menu = new JMenu();
		presentation.populate(menu);
		assertEquals("automation.manage-user-tools",
				menu.getItem(0).getClientProperty(GeoCeDGActionRegistry.ACTION_ID));
		assertEquals("OwnedMidpoint", menu.getItem(2).getText());
		assertNull(menu.getItem(2).getClientProperty(GeoCeDGActionRegistry.ACTION_ID));
		JPanel pins = new JPanel();
		presentation.populatePins(pins);
		assertEquals(0, pins.getComponentCount());
		library.pin(tool.id(), "OwnedMidpoint", true);
		presentation.populatePins(pins);
		assertEquals(1, pins.getComponentCount());
		library.pin(tool.id(), "OwnedMidpoint", false);
		presentation.populatePins(pins);
		assertEquals(0, pins.getComponentCount());
		assertEquals(0, app.getKernel().getMacroNumber());
	}

	private void assertRejectedWithoutMutation(byte[] bytes) {
		String before = app.getXML();
		assertThrows(IOException.class, () -> library.install("bad.ggt", bytes));
		assertEquals(before, app.getXML());
		assertEquals(0, app.getKernel().getMacroNumber());
		assertTrue(library.packages().isEmpty());
		assertFalse(Files.exists(storage));
	}

	private static byte[] midpointPackage(String... names) throws Exception {
		AppGeoCeDG source = G9U1TestApp.create();
		GeoElement a = G9U1TestApp.eval(source, "A=(0,0)");
		GeoElement b = G9U1TestApp.eval(source, "B=(2,0)");
		GeoElement m = G9U1TestApp.eval(source, "M=Midpoint(A,B)");
		ArrayList<Macro> macros = new ArrayList<>();
		for (String name : names) {
			Macro macro = new Macro(source.getKernel(), name, new GeoElement[] {a, b},
					new GeoElement[] {m});
			source.getKernel().addMacro(macro);
			macros.add(macro);
		}
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		source.getXMLio().writeMacroStream(output, macros,
				new ArrayList<>());
		return output.toByteArray();
	}

	private static byte[] changeXml(byte[] original, UnaryOperator<String> change)
			throws IOException {
		return ggt(change.apply(zipXml(original, "geogebra_macro.xml")));
	}

	private static byte[] ggt(String xml) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (ZipOutputStream zip = new ZipOutputStream(output)) {
			zip.putNextEntry(new ZipEntry("geogebra_macro.xml"));
			zip.write(xml.getBytes(StandardCharsets.UTF_8));
			zip.closeEntry();
		}
		return output.toByteArray();
	}

	private static byte[] archiveXml(AppD app) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		app.getXMLio().writeGeoGebraFile(output, false);
		return zipXml(output.toByteArray(), "geogebra.xml").getBytes(StandardCharsets.UTF_8);
	}

	private static String zipXml(byte[] bytes, String name) throws IOException {
		try (ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(bytes))) {
			ZipEntry entry;
			while ((entry = input.getNextEntry()) != null) {
				if (name.equals(entry.getName())) {
					return new String(input.readAllBytes(), StandardCharsets.UTF_8);
				}
			}
		}
		throw new IOException("Missing archive entry: " + name);
	}
}
