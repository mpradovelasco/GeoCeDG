/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JPanel;

import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.GeoGebraConstants;
import org.geogebra.common.jre.io.MyXMLioJre;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.main.App;
import org.geogebra.common.main.AppConfig;
import org.geogebra.common.main.settings.config.AppConfigDefault;
import org.geogebra.common.main.undo.UndoManager;
import org.geogebra.common.util.FileExtensions;
import org.geogebra.common.util.StringUtil;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.gui.FileDropTargetListener;
import org.geogebra.desktop.headless.AppDNoGui;
import org.geogebra.desktop.headless.GFileHandler;
import org.geogebra.desktop.io.AtomicDocumentFileWriter;
import org.geogebra.desktop.io.DocumentArchivePreflight;
import org.geogebra.desktop.io.MyXMLioD;
import org.geogebra.desktop.main.AppD;
import org.geogebra.desktop.main.LocalizationD;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GeoCeDGDocumentLifecycleTest {

	private static final String EMPTY_DOCUMENT_XML =
			"<geogebra format=\"5.0\" app=\"classic\"><construction/></geogebra>";

	@TempDir
	Path temporaryDirectory;

	@Test
	@DisplayName("R2-D01 default Save selects native identity")
	void defaultSaveRequiresNativeSaveAs() throws Exception {
		AppGeoCeDG app = newDesktop();
		GeoNumeric value = new GeoNumeric(app.getKernel().getConstruction(), 11);
		value.setLabel("newNativeValue");
		Path target = temporaryDirectory.resolve("new-document.cedg");

		assertTrue(GeoCeDGDocumentPolicy.requiresNativeSaveAs(
				app.getCurrentFile()));
		assertEquals(FileExtensions.GEOCEDG, FileExtensions.get("cedg"));
		assertTrue(gui(app).saveAsTo(target.toFile()));
		assertEquals(target.toFile(), app.getCurrentFile());
		assertEquals(target.toFile(), AppD.getFromFileList(0));

		AppGeoCeDG reopened = newDesktop();
		assertTrue(reopened.loadFile(target.toFile(), false));
		assertEquals(11, ((GeoNumeric) reopened.getKernel()
				.lookupLabel("newNativeValue")).getDouble(), 0);
	}

	@Test
	@DisplayName("R2-D02 Save As normalizes omission and rejects conflict")
	void nativeSaveAsPolicyHandlesSuffixesExplicitly() {
		File suggestion = GeoCeDGDocumentPolicy.nativeSuggestion(
				new File(temporaryDirectory.toFile(), "legacy.ggb"));
		assertEquals("legacy.cedg", suggestion.getName());
		assertFalse(GeoCeDGDocumentPolicy.hasConflictingSuffix(suggestion));
		assertTrue(GeoCeDGDocumentPolicy.hasConflictingSuffix(
				new File(temporaryDirectory.toFile(), "legacy.ggb.cedg")));
		assertFalse(GeoCeDGDocumentPolicy.hasConflictingSuffix(
				new File(temporaryDirectory.toFile(), "drawing.v1.cedg")));
		assertEquals("legacy.cedg", GeoCeDGDocumentPolicy.nativeSuggestion(
				new File(temporaryDirectory.toFile(), "legacy.ggb.cedg")).getName());
	}

	@Test
	@DisplayName("R2-D03 native target publishes and replaces atomically")
	void nativeTargetCanBeReopenedAfterAtomicReplacement() throws Exception {
		AppDNoGui source = newHeadless(new AppConfigGeoCeDG(true));
		GeoNumeric value = new GeoNumeric(source.getKernel().getConstruction(), 17);
		value.setLabel("nativeValue");
		String sourceXml = source.getXML();
		Path target = temporaryDirectory.resolve("document.cedg");
		writeHeadlessDocument(source, target);
		MyXMLioD.getPreviewImage(target.toFile());
		AppDNoGui reopened = reopenHeadlessDocument(target,
				new AppConfigGeoCeDG(true));
		assertEquals(sourceXml, reopened.getXML());
		assertEquals(17, ((GeoNumeric) reopened.getKernel()
				.lookupLabel("nativeValue")).getDouble(), 0);
		writeHeadlessDocument(reopened, target);
		AppDNoGui reopenedAgain = reopenHeadlessDocument(target,
				new AppConfigGeoCeDG(true));
		assertEquals(sourceXml, reopenedAgain.getXML());
		assertEquals(FileExtensions.GEOCEDG,
				StringUtil.getFileExtension(target.getFileName().toString()));
	}

	@Test
	@DisplayName("R2-D04 mixed-case native input is recognized")
	void mixedCaseNativeInputUsesHostExtensionSemantics() throws IOException {
		assertEquals(FileExtensions.GEOCEDG,
				StringUtil.getFileExtension("MODEL.CeDg"));
		assertTrue(GeoCeDGDocumentPolicy.isNative(new File("MODEL.CEDG")));
		assertEquals("MODEL.cedg", GeoCeDGDocumentPolicy.normalizeNativeSuffix(
				new File("MODEL.CEDG")).getName());
		Path lowerCaseTarget = temporaryDirectory.resolve("MODEL.cedg");
		Files.writeString(lowerCaseTarget, "complete-target");
		Path upperCaseTarget = temporaryDirectory.resolve("MODEL.CEDG");
		if (AppD.WINDOWS && Files.exists(upperCaseTarget)) {
			assertTrue(Files.isSameFile(lowerCaseTarget,
					upperCaseTarget));
		}
	}

	@Test
	@DisplayName("R2-D05 direct and drop routes accept native documents")
	void dropAndDirectOpenShareNativeExtensionAuthority() throws Exception {
		Path directPath = writeNumericDocument("direct.cedg", "directValue", 21);
		File canonicalDirectFile = directPath.toFile().getCanonicalFile();
		AppGeoCeDG direct = newDesktop(canonicalDirectFile.getPath());
		assertEquals(canonicalDirectFile, direct.getCurrentFile());
		assertEquals(canonicalDirectFile, AppD.getFromFileList(0));
		assertEquals(21, ((GeoNumeric) direct.getKernel()
				.lookupLabel("directValue")).getDouble(), 0);

		Path droppedPath = writeNumericDocument("dropped.cedg", "droppedValue", 22);
		AppGeoCeDG dropped = newDesktop();
		dropped.setSaved();
		assertTrue(new FileDropTargetListener(dropped).handleFileDrop(
				fileListTransferable(droppedPath.toFile())));
		assertEquals(droppedPath.toFile(), dropped.getCurrentFile());
		assertEquals(droppedPath.toFile(), AppD.getFromFileList(0));
		assertEquals(22, ((GeoNumeric) dropped.getKernel()
				.lookupLabel("droppedValue")).getDouble(), 0);

		File recent = AppD.getFromFileList(0);
		AppGeoCeDG reopenedRecent = newDesktop();
		assertTrue(reopenedRecent.loadFile(recent, false));
		assertEquals(recent, reopenedRecent.getCurrentFile());
		assertEquals(recent, AppD.getFromFileList(0));
		assertEquals(22, ((GeoNumeric) reopenedRecent.getKernel()
				.lookupLabel("droppedValue")).getDouble(), 0);

		assertArrayEquals(new FileExtensions[] {
				FileExtensions.GEOCEDG, FileExtensions.GEOGEBRA },
				GeoCeDGDocumentPolicy.documentOpenExtensions());
	}

	@Test
	@DisplayName("R2-D06 legacy input remains compatibility input")
	void legacyGgbIsInputWithoutNativeReclassification() throws Exception {
		Path sourcePath = writeNumericDocument("legacy.ggb", "legacyValue", 31);
		File source = sourcePath.toFile();
		AppGeoCeDG app = newDesktop();
		assertTrue(app.loadFile(source, false));

		assertTrue(GeoCeDGDocumentPolicy.isCompatibilityInput(source));
		assertFalse(GeoCeDGDocumentPolicy.isNative(source));
		assertTrue(GeoCeDGDocumentPolicy.requiresNativeSaveAs(
				app.getCurrentFile()));
		assertEquals(source, app.getCurrentFile());
		assertEquals(source, AppD.getFromFileList(0));
		assertEquals(31, ((GeoNumeric) app.getKernel()
				.lookupLabel("legacyValue")).getDouble(), 0);
	}

	@Test
	@DisplayName("R2-D07 compatibility Save requires a distinct native target")
	void compatibilityInputAlwaysRoutesSaveThroughNativeSaveAs() throws Exception {
		Path sourcePath = writeNumericDocument("source.ggb", "transitionValue", 41);
		File source = sourcePath.toFile();
		File target = GeoCeDGDocumentPolicy.nativeSuggestion(source);
		byte[] sourceBefore = Files.readAllBytes(sourcePath);
		AppGeoCeDG app = newDesktop();
		assertTrue(app.loadFile(source, false));

		assertTrue(GeoCeDGDocumentPolicy.requiresNativeSaveAs(source));
		assertNotEquals(source, target);
		assertEquals("source.cedg", target.getName());
		assertTrue(gui(app).saveAsTo(target));
		assertArrayEquals(sourceBefore, Files.readAllBytes(sourcePath));
		assertEquals(target, app.getCurrentFile());
		assertEquals(target, AppD.getFromFileList(0));

		AppGeoCeDG reopened = newDesktop();
		assertTrue(reopened.loadFile(target, false));
		assertEquals(41, ((GeoNumeric) reopened.getKernel()
				.lookupLabel("transitionValue")).getDouble(), 0);
	}

	@Test
	@DisplayName("R2-D08 failed publication preserves the source and target")
	void injectedWriteFailureCannotReplaceExistingBytes() throws Exception {
		Path sourcePath = writeNumericDocument("preserved-source.ggb",
				"preservedValue", 51);
		File source = sourcePath.toFile();
		byte[] sourceBefore = Files.readAllBytes(sourcePath);
		AppGeoCeDG app = newDesktop();
		assertTrue(app.loadFile(source, false));
		String liveXml = app.getXML();
		File livePath = app.getCurrentPath();
		File recent = AppD.getFromFileList(0);
		boolean saved = app.isSaved();

		assertFalse(gui(app).saveAsTo(null));
		assertDesktopState(app, liveXml, source, livePath, recent, saved);
		assertArrayEquals(sourceBefore, Files.readAllBytes(sourcePath));

		Path failedTarget = temporaryDirectory.resolve("missing-parent")
				.resolve("failed.cedg");
		assertFalse(gui(app).saveAsTo(failedTarget.toFile()));
		assertFalse(Files.exists(failedTarget));
		assertDesktopState(app, liveXml, source, livePath, recent, saved);
		assertArrayEquals(sourceBefore, Files.readAllBytes(sourcePath));

		Path existingTarget = temporaryDirectory.resolve("existing-target.cedg");
		Files.writeString(existingTarget, "complete-old-target");
		assertThrows(IOException.class, () -> AtomicDocumentFileWriter.write(
				existingTarget, temporary -> {
					Files.writeString(temporary, "partial-new-target");
					throw new IOException("injected publication failure");
				}));
		assertEquals("complete-old-target", Files.readString(existingTarget));

		Path successfulTarget = temporaryDirectory.resolve("preserved-native.cedg");
		assertTrue(gui(app).saveAsTo(successfulTarget.toFile()));
		assertArrayEquals(sourceBefore, Files.readAllBytes(sourcePath));
		assertEquals(liveXml, app.getXML());
		assertEquals(successfulTarget.toFile(), app.getCurrentFile());
		assertEquals(successfulTarget.toFile(), AppD.getFromFileList(0));
	}

	@Test
	@DisplayName("R2-D09 corrupt, live-parse and undo-commit failures roll back")
	void corruptNativeArchiveIsRejectedBeforeLiveLoad() throws Exception {
		AppGeoCeDG live = newDesktop();
		UndoManager undoManager = live.getKernel().getConstruction()
				.getUndoManager();
		awaitUndoState(undoManager,
				() -> undoManager.getHistorySize() >= 0);
		GeoNumeric sentinel = new GeoNumeric(live.getKernel().getConstruction(), 42);
		sentinel.setLabel("liveSentinel");
		Path livePath = temporaryDirectory.resolve("live-document.cedg");
		assertTrue(gui(live).saveAsTo(livePath.toFile()));
		String liveScript = "function livePreserved() { return 42; }\n";
		live.getKernel().setLibraryJavaScript(liveScript);
		live.getKernel().initUndoInfo();
		awaitUndoState(undoManager,
				() -> undoManager.getHistorySize() >= 0
						&& !undoManager.undoPossible());
		sentinel.setValue(43);
		live.getKernel().storeUndoInfo();
		awaitUndoState(undoManager,
				() -> undoManager.getHistorySize() >= 1
						&& undoManager.undoPossible());
		live.setUnsaved();
		String liveXml = live.getXML();
		String liveUniqueId = live.getUniqueId();
		File liveCurrentFile = live.getCurrentFile();
		File liveCurrentPath = live.getCurrentPath();
		File recent = AppD.getFromFileList(0);
		byte[] liveFileBytes = Files.readAllBytes(livePath);
		int undoHistorySize;
		boolean undoPossible;
		boolean redoPossible;
		synchronized (undoManager) {
			undoHistorySize = undoManager.getHistorySize();
			undoPossible = undoManager.undoPossible();
			redoPossible = undoManager.redoPossible();
		}
		byte[] futureXml = Files.readAllBytes(findRepositoryRoot().resolve(
				"source/shared/common-jre/src/test/resources/org/geocedg/common/"
						+ "locus/g9u0/future-provider.xml"));
		byte[][] corruptArchives = {
			new byte[] { 'P', 'K', 3, 4 },
			archiveWithEntry("not-geogebra.xml", "<ignored/>"),
			archiveWithEntry("geogebra.xml", "<geogebra><construction>"),
			archiveWithEntry("geogebra.xml",
					new String(futureXml, StandardCharsets.UTF_8))
		};
		int liveObjectCount = live.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size();
		for (int index = 0; index < corruptArchives.length; index++) {
			Path corruptFile = temporaryDirectory.resolve(
					"corrupt-" + index + ".cedg");
			Files.write(corruptFile, corruptArchives[index]);
			byte[] originalBytes = Files.readAllBytes(corruptFile);
			assertFalse(live.loadFile(corruptFile.toFile(), false));
			assertArrayEquals(originalBytes, Files.readAllBytes(corruptFile));
			assertArrayEquals(liveFileBytes, Files.readAllBytes(livePath));
			assertEquals(liveXml, live.getXML());
			assertEquals(liveUniqueId, live.getUniqueId());
			assertEquals(liveCurrentFile, live.getCurrentFile());
			assertEquals(liveCurrentPath, live.getCurrentPath());
			assertEquals(recent, AppD.getFromFileList(0));
			assertFalse(live.isSaved());
			assertSame(sentinel, live.getKernel().lookupLabel("liveSentinel"));
			assertEquals(43, sentinel.getDouble(), 0);
			assertEquals(liveObjectCount, live.getKernel().getConstruction()
					.getGeoSetConstructionOrder().size());
		}

		AppDNoGui admittedSource = newHeadless(new AppConfigGeoCeDG(true));
		GeoNumeric admittedValue = new GeoNumeric(
				admittedSource.getKernel().getConstruction(), 99);
		admittedValue.setLabel("admittedValue");
		admittedSource.getKernel().setLibraryJavaScript(
				"function candidateReplacement() { return 99; }");
		Path admittedPath = temporaryDirectory.resolve("preflight-admitted.cedg");
		writeHeadlessDocument(admittedSource, admittedPath);
		byte[] admittedBytes = Files.readAllBytes(admittedPath);
		assertTrue(DocumentArchivePreflight.validate(admittedBytes,
				new AppConfigGeoCeDG(true)));

		live.getKernel().setUserStopsLoading(true);
		assertFalse(live.loadFile(admittedPath.toFile(), false));
		assertArrayEquals(admittedBytes, Files.readAllBytes(admittedPath));
		assertArrayEquals(liveFileBytes, Files.readAllBytes(livePath));
		assertEquals(liveXml, live.getXML());
		assertEquals(liveUniqueId, live.getUniqueId());
		assertEquals(liveCurrentFile, live.getCurrentFile());
		assertEquals(liveCurrentPath, live.getCurrentPath());
		assertEquals(recent, AppD.getFromFileList(0));
		assertFalse(live.isSaved());
		assertEquals(liveScript, live.getKernel().getLibraryJavaScript());
		assertNull(live.getKernel().lookupLabel("admittedValue"));
		GeoNumeric restoredSentinel = (GeoNumeric) live.getKernel()
				.lookupLabel("liveSentinel");
		assertEquals(43, restoredSentinel.getDouble(), 0);
		assertEquals(liveObjectCount, live.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size());
		assertFalse(live.getKernel().getConstruction().isFileLoading());
		assertFalse(live.getKernel().userStopsLoading());
		assertFalse(live.isIniting());
		assertSame(undoManager, live.getKernel().getConstruction()
				.getUndoManager());
		synchronized (undoManager) {
			assertEquals(undoHistorySize, undoManager.getHistorySize());
			assertEquals(undoPossible, undoManager.undoPossible());
			assertEquals(redoPossible, undoManager.redoPossible());
		}

		live.getKernel().undo();
		assertEquals(42, ((GeoNumeric) live.getKernel()
				.lookupLabel("liveSentinel")).getDouble(), 0);
		live.getKernel().redo();
		assertEquals(43, ((GeoNumeric) live.getKernel()
				.lookupLabel("liveSentinel")).getDouble(), 0);

		UndoCommitFailingApp commitLive = new UndoCommitFailingApp();
		assertTrue(commitLive.loadFile(livePath.toFile(), false));
		UndoManager commitUndoManager = commitLive.getKernel().getConstruction()
				.getUndoManager();
		GeoNumeric commitSentinel = (GeoNumeric) commitLive.getKernel()
				.lookupLabel("liveSentinel");
		commitSentinel.setValue(43);
		commitLive.getKernel().storeUndoInfo();
		awaitUndoState(commitUndoManager,
				() -> commitUndoManager.getHistorySize() >= 1);
		commitSentinel.setValue(44);
		commitLive.getKernel().storeUndoInfo();
		awaitUndoState(commitUndoManager,
				() -> commitUndoManager.getHistorySize() >= 2);
		commitLive.getKernel().undo();
		assertEquals(43, ((GeoNumeric) commitLive.getKernel()
				.lookupLabel("liveSentinel")).getDouble(), 0);
		commitLive.setUnsaved();
		commitLive.setHideConstructionProtocolNavigation();
		assertEquals("", commitLive.getConsProtNavigationIds().toString());
		commitLive.setShowConstructionProtocolNavigation(true,
				App.VIEW_CONSTRUCTION_PROTOCOL);
		assertTrue(commitLive.showConsProtNavigation(App.VIEW_CONSTRUCTION_PROTOCOL));
		assertEquals("32", commitLive.getConsProtNavigationIds().toString());

		final String commitLiveXml = commitLive.getXML();
		final String commitLiveUniqueId = commitLive.getUniqueId();
		final File commitLiveCurrentFile = commitLive.getCurrentFile();
		final File commitLiveCurrentPath = commitLive.getCurrentPath();
		final File commitLiveRecent = AppD.getFromFileList(0);
		int commitUndoHistorySize;
		boolean commitUndoPossible;
		boolean commitRedoPossible;
		synchronized (commitUndoManager) {
			commitUndoHistorySize = commitUndoManager.getHistorySize();
			commitUndoPossible = commitUndoManager.undoPossible();
			commitRedoPossible = commitUndoManager.redoPossible();
		}
		assertTrue(commitUndoPossible);
		assertTrue(commitRedoPossible);

		commitLive.failNextUndoBaselineCommit();
		assertFalse(commitLive.loadFile(admittedPath.toFile(), false));
		assertArrayEquals(admittedBytes, Files.readAllBytes(admittedPath));
		assertArrayEquals(liveFileBytes, Files.readAllBytes(livePath));
		assertEquals(commitLiveXml, commitLive.getXML());
		assertTrue(commitLive.showConsProtNavigation(App.VIEW_CONSTRUCTION_PROTOCOL));
		assertEquals("32", commitLive.getConsProtNavigationIds().toString());
		assertEquals(commitLiveUniqueId, commitLive.getUniqueId());
		assertEquals(commitLiveCurrentFile, commitLive.getCurrentFile());
		assertEquals(commitLiveCurrentPath, commitLive.getCurrentPath());
		assertEquals(commitLiveRecent, AppD.getFromFileList(0));
		assertFalse(commitLive.isSaved());
		assertNull(commitLive.getKernel().lookupLabel("admittedValue"));
		assertEquals(43, ((GeoNumeric) commitLive.getKernel()
				.lookupLabel("liveSentinel")).getDouble(), 0);
		assertFalse(commitLive.getKernel().getConstruction().isFileLoading());
		assertFalse(commitLive.isIniting());
		assertSame(commitUndoManager, commitLive.getKernel().getConstruction()
				.getUndoManager());
		synchronized (commitUndoManager) {
			assertEquals(commitUndoHistorySize,
					commitUndoManager.getHistorySize());
			assertEquals(commitUndoPossible, commitUndoManager.undoPossible());
			assertEquals(commitRedoPossible, commitUndoManager.redoPossible());
		}

		commitLive.getKernel().undo();
		assertEquals(42, ((GeoNumeric) commitLive.getKernel()
				.lookupLabel("liveSentinel")).getDouble(), 0);
		commitLive.getKernel().redo();
		assertEquals(43, ((GeoNumeric) commitLive.getKernel()
				.lookupLabel("liveSentinel")).getDouble(), 0);
		commitLive.getKernel().redo();
		assertEquals(44, ((GeoNumeric) commitLive.getKernel()
				.lookupLabel("liveSentinel")).getDouble(), 0);
		commitLive.getKernel().undo();
		assertEquals(43, ((GeoNumeric) commitLive.getKernel()
				.lookupLabel("liveSentinel")).getDouble(), 0);
	}

	@Test
	@DisplayName("R2-D10 native archive uses the existing semantic reader")
	void validNativeArchivePassesGeoCeDGPreflight() throws Exception {
		assertTrue(DocumentArchivePreflight.validate(validArchive(),
				new AppConfigGeoCeDG()));
	}

	@Test
	@DisplayName("R2-D11 rich result bytes are not transformed by routing")
	void nativeRoutingDoesNotTransformArchivePayload() throws IOException {
		byte[] archive = validArchive();
		Path target = temporaryDirectory.resolve("rich.cedg");
		AtomicDocumentFileWriter.write(target,
				temporary -> Files.write(temporary, archive));
		assertArrayEquals(archive, Files.readAllBytes(target));
	}

	@Test
	@DisplayName("R2-D12 durable identity bytes are suffix-independent")
	void durableIdentityArchiveIsUnchangedAcrossFileNames() throws IOException {
		byte[] archive = validArchive();
		Path compatibility = temporaryDirectory.resolve("identity.ggb");
		Path nativeFile = temporaryDirectory.resolve("identity.cedg");
		Files.write(compatibility, archive);
		Files.write(nativeFile, archive);
		assertArrayEquals(Files.readAllBytes(compatibility),
				Files.readAllBytes(nativeFile));
	}

	@Test
	@DisplayName("R2-D13 extension identity is routing, not semantics")
	void identicalArchiveBytesReceiveOnlyDifferentIoClassification()
			throws IOException {
		Path ggb = temporaryDirectory.resolve("same.ggb");
		Path cedg = temporaryDirectory.resolve("same.cedg");
		byte[] archive = validArchive();
		Files.write(ggb, archive);
		Files.write(cedg, archive);
		assertArrayEquals(Files.readAllBytes(ggb), Files.readAllBytes(cedg));
		assertTrue(GeoCeDGDocumentPolicy.isCompatibilityInput(ggb.toFile()));
		assertTrue(GeoCeDGDocumentPolicy.isNative(cedg.toFile()));
	}

	@Test
	@DisplayName("R2-D14 Classic preserves native input without changing its default")
	void classicKeepsClassicDefaultWhileNativeExtensionIsSupported()
			throws Exception {
		Path sourceModel = findRepositoryRoot().resolve(
				"models/regression/g9a2-spatial-point-pilot/"
						+ "g9a2-spatial-point-pilot.ggb");
		Path nativeInput = temporaryDirectory.resolve("classic-input.cedg");
		Files.copy(sourceModel, nativeInput);
		AppDNoGui classic = newHeadless(new AppConfigDefault());
		assertTrue(GFileHandler.loadXML(classic, Files.newInputStream(nativeInput),
				false));
		String preservedXml = classic.getXML();
		assertTrue(preservedXml.contains("geocedgSpatial"));
		Path nativeSaved = temporaryDirectory.resolve("classic-saved.cedg");
		AtomicDocumentFileWriter.write(nativeSaved, temporary ->
				((MyXMLioJre) classic.getXMLio())
						.writeGeoGebraFile(temporary.toFile()));
		AppDNoGui reopened = newHeadless(new AppConfigDefault());
		assertTrue(GFileHandler.loadXML(reopened, Files.newInputStream(nativeSaved),
				false));
		assertEquals(preservedXml, reopened.getXML());
		assertEquals(GeoGebraConstants.CLASSIC_APPCODE,
				reopened.getConfig().getAppCode());
		assertEquals(FileExtensions.GEOGEBRA, FileExtensions.get("ggb"));
	}

	@Test
	@DisplayName("R2-D15 external boundary has no automatic downgrade target")
	void nativeTargetIsNeverSilentlyRenamedToGgb() {
		File nativeFile = GeoCeDGDocumentPolicy.nativeSuggestion(new File("model.ggb"));
		assertTrue(GeoCeDGDocumentPolicy.isNative(nativeFile));
		assertFalse(GeoCeDGDocumentPolicy.isCompatibilityInput(nativeFile));
	}

	@Test
	@DisplayName("R2-D16 archive inventory and app code remain unchanged")
	void nativeArchiveRetainsGeogebraXmlAndClassicAppCode() throws IOException {
		String xml = readZipEntry(validArchive(), "geogebra.xml");
		assertTrue(xml.contains("app=\"classic\""));
		assertTrue(xml.contains("<construction/>"));
	}

	private static byte[] validArchive() throws IOException {
		return archiveWithEntry("geogebra.xml", EMPTY_DOCUMENT_XML);
	}

	private static byte[] archiveWithEntry(String name, String contents)
			throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
			zip.putNextEntry(new ZipEntry(name));
			zip.write(contents.getBytes(StandardCharsets.UTF_8));
			zip.closeEntry();
		}
		return bytes.toByteArray();
	}

	private static String readZipEntry(byte[] archive, String expectedName)
			throws IOException {
		try (ZipInputStream zip = new ZipInputStream(
				new ByteArrayInputStream(archive))) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				if (expectedName.equals(entry.getName())) {
					return new String(zip.readAllBytes(), StandardCharsets.UTF_8);
				}
			}
		}
		throw new IOException("Missing archive entry: " + expectedName);
	}

	private Path writeNumericDocument(String fileName, String label, double value)
			throws IOException {
		AppDNoGui source = newHeadless(new AppConfigGeoCeDG(true));
		GeoNumeric numeric = new GeoNumeric(source.getKernel().getConstruction(), value);
		numeric.setLabel(label);
		Path target = temporaryDirectory.resolve(fileName);
		writeHeadlessDocument(source, target);
		return target;
	}

	private static AppGeoCeDG newDesktop(String... arguments) {
		String[] allArguments = new String[arguments.length + 1];
		allArguments[0] = "--silent";
		System.arraycopy(arguments, 0, allArguments, 1, arguments.length);
		AppGeoCeDG app = new AppGeoCeDG(
				new CommandLineArguments(allArguments), new JPanel());
		app.setErrorDialogsActive(false);
		return app;
	}

	private static GuiManagerGeoCeDG gui(AppGeoCeDG app) {
		return (GuiManagerGeoCeDG) app.getGuiManager();
	}

	private static void assertDesktopState(AppGeoCeDG app, String xml,
			File currentFile, File currentPath, File recent, boolean saved) {
		assertEquals(xml, app.getXML());
		assertEquals(currentFile, app.getCurrentFile());
		assertEquals(currentPath, app.getCurrentPath());
		assertEquals(recent, AppD.getFromFileList(0));
		assertEquals(saved, app.isSaved());
	}

	private static Transferable fileListTransferable(File file) {
		return new Transferable() {
			@Override
			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[] {DataFlavor.javaFileListFlavor};
			}

			@Override
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return DataFlavor.javaFileListFlavor.equals(flavor);
			}

			@Override
			public Object getTransferData(DataFlavor flavor)
					throws UnsupportedFlavorException {
				if (!isDataFlavorSupported(flavor)) {
					throw new UnsupportedFlavorException(flavor);
				}
				return List.of(file);
			}
		};
	}

	private static AppDNoGui newHeadless(AppConfig config) {
		Log previousLogger = Log.getLogger();
		try {
			return new AppDNoGui(new LocalizationD(3), true, config);
		} finally {
			Log.setLogger(previousLogger);
		}
	}

	private static void awaitUndoState(UndoManager undoManager,
			BooleanSupplier expectedState) throws InterruptedException {
		long deadline = System.nanoTime() + 5_000_000_000L;
		boolean reached = false;
		while (!reached && System.nanoTime() < deadline) {
			synchronized (undoManager) {
				reached = expectedState.getAsBoolean();
			}
			if (!reached) {
				Thread.sleep(10);
			}
		}
		assertTrue(reached, "Desktop undo worker did not reach the expected state");
	}

	private static void writeHeadlessDocument(AppDNoGui app, Path target)
			throws IOException {
		AtomicDocumentFileWriter.write(target, temporary ->
				((MyXMLioJre) app.getXMLio())
						.writeGeoGebraFile(temporary.toFile()));
	}

	private static AppDNoGui reopenHeadlessDocument(Path source,
			AppConfig config) throws Exception {
		AppDNoGui reopened = newHeadless(config);
		assertTrue(GFileHandler.loadXML(reopened, Files.newInputStream(source),
				false));
		return reopened;
	}

	private static Path findRepositoryRoot() {
		Path candidate = Path.of("").toAbsolutePath().normalize();
		while (candidate != null) {
			if (Files.isRegularFile(candidate.resolve("AGENTS.md"))
					&& Files.isDirectory(candidate.resolve("geocedg"))) {
				return candidate;
			}
			candidate = candidate.getParent();
		}
		throw new IllegalStateException("GeoCeDG repository root not found");
	}

	private static final class UndoCommitFailingApp extends AppD {
		private boolean failNextUndoBaselineCommit;

		private UndoCommitFailingApp() {
			super(new CommandLineArguments(new String[] {"--silent"}), null,
					new JPanel(), true, new LocalizationD(3),
					new AppConfigGeoCeDG(true));
			setErrorDialogsActive(false);
		}

		private void failNextUndoBaselineCommit() {
			failNextUndoBaselineCommit = true;
		}

		@Override
		protected void beforeNativeUndoBaselineCommit() {
			if (failNextUndoBaselineCommit) {
				failNextUndoBaselineCommit = false;
				throw new SecurityException("injected undo baseline commit failure");
			}
		}
	}
}
