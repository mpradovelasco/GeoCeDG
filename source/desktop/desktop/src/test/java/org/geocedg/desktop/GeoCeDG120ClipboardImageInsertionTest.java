/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

import org.geocedg.desktop.GeoCeDGClipboardImageInsertion.Choice;
import org.geocedg.desktop.GeoCeDGClipboardImageInsertion.Position;
import org.geogebra.common.kernel.algos.ConstructionElement;
import org.geogebra.common.kernel.ConstructionDefaults;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.arithmetic.NumberValue;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoImage;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.main.undo.UndoCommand;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.MyImageD;
import org.geogebra.desktop.gui.util.ImageSelection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/** New product operation: native blocks, exact cursor/layer ownership and real clipboard pixels. */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class GeoCeDG120ClipboardImageInsertionTest {
	@TempDir(cleanup = CleanupMode.NEVER) Path directory;

	static AppGeoCeDG testApp() {
		return testHost(G9U1TestApp.create());
	}

	static AppGeoCeDG testHost(AppGeoCeDG app) {
		app.getInsertionIsolation().setPresentation(new PreG9BR6PlusF3RecoveryTest.Scripted());
		// Embedded test host only: return the real serious outcome without a modal
		// or closing an unrelated application window. Assertions still reject it.
		app.getInsertionIsolation().setCloser(target -> { });
		return app;
	}

	static AppGeoCeDG fixture() {
		AppGeoCeDG app = testApp();
		must(app, "A=(1,1)"); must(app, "B=(3,1)"); must(app, "C=(2,3)");
		app.getKernel().lookupLabel("A").setLayer(1);
		app.getLayerWorkspace().setWorkingLayer(2);
		app.getLayerWorkspace().setLayerHidden(7, true);
		app.getSelectionManager().setSelectedGeos(new ArrayList<>(List.of(
				app.getKernel().lookupLabel("A"), app.getKernel().lookupLabel("B"))));
		app.setInsertionMessageSink(message -> { });
		return app;
	}

	static List<GeoElement> geos(AppGeoCeDG app) {
		return new ArrayList<>(app.getKernel().getConstruction().getGeoSetConstructionOrder());
	}

	static List<GeoImage> documentImages(AppGeoCeDG app) {
		// app.images also owns unlabelled ConstructionDefaults prototypes.
		return geos(app).stream().filter(GeoImage.class::isInstance).map(GeoImage.class::cast).toList();
	}

	static List<ConstructionElement> elements(AppGeoCeDG app) {
		var construction = app.getKernel().getConstruction();
		List<ConstructionElement> result = new ArrayList<>();
		for (int i = 0; i < construction.steps(); i++) { result.add(construction.getConstructionElement(i)); }
		return result;
	}

	static BufferedImage pixels(int salt) {
		BufferedImage result = new BufferedImage(8, 6, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < result.getHeight(); y++) {
			for (int x = 0; x < result.getWidth(); x++) {
				result.setRGB(x, y, 0xff000000 | ((x * 29 + salt) & 255) << 16
						| ((y * 41 + salt * 3) & 255) << 8 | ((x + y + salt * 7) & 255));
			}
		}
		return result;
	}

	static GeoPoint corner(AppGeoCeDG app, double x, double y) {
		GeoPoint point = new GeoPoint(app.getKernel().getConstruction());
		point.setCoords(x, y, 1); point.setLabel(null); point.setLabelVisible(false); point.update();
		return point;
	}

	/** Simulated native creation, deliberately using the same public kernel primitives. */
	static List<GeoImage> nativeImages(AppGeoCeDG app, int count) {
		List<GeoImage> images = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			GeoPoint first = corner(app, 1 + i, 1), second = corner(app, 3 + i, 1);
			String name = "clipboard-simulated-" + i + ".png";
			app.getImageManager().addExternalImage(name, new MyImageD(pixels(13 + i)));
			GeoImage image = new GeoImage(app.getKernel().getConstruction());
			image.setImageFileName(name); image.setCorner(first, 0); image.setCorner(second, 1);
			image.setLabel(null); GeoImage.updateInstances(app); images.add(image);
		}
		GeoImage last = images.get(images.size() - 1);
		var controller = app.getActiveEuclidianView().getEuclidianController();
		controller.clearSelections();
		controller.memorizeJustCreatedGeos(new GeoElement[] {
				last, last.getStartPoint(0), last.getStartPoint(1)});
		return images;
	}

	static GeoCeDGClipboardImageInsertion operation(AppGeoCeDG app, int layer,
			Position position, GeoCeDGClipboardImageInsertion.NativeLoader loader) {
		var operation = new GeoCeDGClipboardImageInsertion(app);
		operation.setPresentation((target, state) -> new Choice(layer, position));
		operation.setNativeLoader(loader);
		return operation;
	}

	static void assertOutcome(GeoCeDGDocumentInsertion.Outcome expected,
			GeoCeDGDocumentInsertion.Result result) {
		assertNotNull(result);
		if (result.getOutcome() != expected) {
			System.out.println("Clipboard insertion expected " + expected + " but returned "
					+ result.getOutcome() + ": " + result.getMessage());
			if (result.getFailure() != null) { result.getFailure().printStackTrace(System.out); }
		}
		assertEquals(expected, result.getOutcome(),
				() -> result.getMessage() + " " + result.getFailure());
	}

	static void assertInserted(GeoCeDGDocumentInsertion.Result result) {
		assertOutcome(GeoCeDGDocumentInsertion.Outcome.INSERTED, result);
	}

	static final class NoFormats implements Transferable {
		@Override public DataFlavor[] getTransferDataFlavors() { return new DataFlavor[0]; }
		@Override public boolean isDataFlavorSupported(DataFlavor flavor) { return false; }
		@Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
			throw new UnsupportedFlavorException(flavor);
		}
	}

	static void restoreClipboard(Clipboard clipboard, Transferable previous) {
		clipboard.setContents(previous == null ? new NoFormats() : previous, null);
	}

	/** Independent observer: none of these reads calls the lazy step getter. */
	static final class DefaultsSnapshot {
		record Prototype(GeoElement geo, NumberValue raw, Long bits, Boolean defined) { }
		final Construction construction;
		final Map<Integer, Prototype> prototypes = new LinkedHashMap<>();
		final String two, three, v2;

		DefaultsSnapshot(AppGeoCeDG app) {
			construction = app.getKernel().getConstruction();
			var defaults = construction.getConstructionDefaults();
			defaults.getDefaultGeos().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
				NumberValue raw = entry.getValue().getAnimationStepObject();
				prototypes.put(entry.getKey(), new Prototype(entry.getValue(), raw,
						raw == null ? null : Double.doubleToRawLongBits(raw.getDouble()), raw == null ? null : raw.isDefined()));
			});
			XMLStringBuilder twoXml = new XMLStringBuilder(), threeXml = new XMLStringBuilder();
			defaults.getDefaultsXML(twoXml, threeXml);
			two = twoXml.toString(); three = threeXml.toString(); v2 = defaults.getV2DefaultsXML();
			assertConstruction(app); // Even the observer's serialization must be passive.
		}

		boolean samePrototypes(AppGeoCeDG app) {
			var defaults = app.getKernel().getConstruction().getConstructionDefaults();
			return defaults.getDefaultGeos().size() == prototypes.size()
					&& prototypes.entrySet().stream().allMatch(entry -> defaults.getDefaultGeo(entry.getKey()) == entry.getValue().geo());
		}

		void assertConstruction(AppGeoCeDG app) {
			assertSame(construction, app.getKernel().getConstruction()); assertTrue(samePrototypes(app));
			prototypes.forEach((type, before) -> {
				GeoElement current = construction.getConstructionDefaults().getDefaultGeo(type);
				assertSame(before.geo(), current, "Default prototype " + type);
				assertSame(before.raw(), current.getAnimationStepObject(), "Raw step identity " + type);
				if (before.raw() != null) {
					assertEquals(before.bits().longValue(), Double.doubleToRawLongBits(before.raw().getDouble()), "Raw step bits " + type);
					assertEquals(before.defined().booleanValue(), before.raw().isDefined(), "Raw defined state " + type);
					if (before.raw() instanceof GeoElement rawGeo) {
						assertSame(construction, rawGeo.getConstruction());
						if (rawGeo.isLabelSet()) {
							assertTrue(geos(app).stream().anyMatch(geo -> geo == rawGeo), "A labelled raw step must remain live");
						} else {
							assertTrue(rawGeo.isIndependent()); assertFalse(rawGeo.isInConstructionList());
							assertFalse(geos(app).contains(rawGeo), "A literal step cannot become a document geo");
						}
					}
				}
			});
			assertXml(app);
		}

		void assertArchive(AppGeoCeDG app) {
			assertSame(construction, app.getKernel().getConstruction());
			var defaults = construction.getConstructionDefaults();
			assertEquals(prototypes.keySet(), defaults.getDefaultGeos().stream().map(Map.Entry::getKey).collect(java.util.stream.Collectors.toSet()));
			prototypes.forEach((type, before) -> {
				GeoElement current = defaults.getDefaultGeo(type); assertNotNull(current);
				assertEquals(before.geo().getClass(), current.getClass()); assertSame(construction, current.getConstruction());
				NumberValue raw = current.getAnimationStepObject();
				if (before.raw() == null) { assertNull(raw, "Archive restore must retain the captured null step " + type); }
				else {
					assertNotNull(raw); assertEquals(before.bits().longValue(), Double.doubleToRawLongBits(raw.getDouble()), "Archive raw bits " + type);
					assertEquals(before.defined().booleanValue(), raw.isDefined(), "Archive raw defined state " + type);
					if (raw instanceof GeoElement rawGeo) {
						assertSame(construction, rawGeo.getConstruction()); assertTrue(rawGeo.isIndependent());
						assertFalse(rawGeo.isLabelSet()); assertFalse(rawGeo.isInConstructionList()); assertFalse(geos(app).contains(rawGeo));
					}
				}
			});
			assertXml(app);
		}

		private void assertXml(AppGeoCeDG app) {
			XMLStringBuilder twoXml = new XMLStringBuilder(), threeXml = new XMLStringBuilder();
			var defaults = app.getKernel().getConstruction().getConstructionDefaults(); defaults.getDefaultsXML(twoXml, threeXml);
			assertEquals(two, twoXml.toString()); assertEquals(three, threeXml.toString()); assertEquals(v2, defaults.getV2DefaultsXML());
		}

		void saveEvidence(Path directory, String stage, AppGeoCeDG app) throws IOException {
			Path output = directory.resolve(stage); Files.createDirectories(output);
			Files.write(output.resolve("native.cedg"), nativeArchive(app));
			XMLStringBuilder twoXml = new XMLStringBuilder(), threeXml = new XMLStringBuilder();
			var defaults = app.getKernel().getConstruction().getConstructionDefaults(); defaults.getDefaultsXML(twoXml, threeXml);
			Files.writeString(output.resolve("geogebra_defaults2d.xml"), twoXml.toString());
			Files.writeString(output.resolve("geogebra_defaults3d.xml"), threeXml.toString());
			StringBuilder raw = new StringBuilder();
			defaults.getDefaultGeos().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
				NumberValue value = entry.getValue().getAnimationStepObject();
				raw.append(entry.getKey()).append(" prototype=").append(System.identityHashCode(entry.getValue()))
						.append(" raw=").append(value == null ? "null" : value.getClass().getName() + "@" + System.identityHashCode(value)
								+ " bits=" + Long.toUnsignedString(Double.doubleToRawLongBits(value.getDouble())) + " defined=" + value.isDefined()).append('\n');
			});
			Files.writeString(output.resolve("raw-defaults.txt"), raw.toString());
			System.out.println("CLIPBOARD_R12_EVIDENCE " + output.toAbsolutePath());
		}
	}

	static byte[] nativeArchive(AppGeoCeDG app) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream(); app.getXMLio().writeGeoGebraFile(bytes, false); return bytes.toByteArray();
	}

	static boolean containsFailure(Throwable root, Throwable wanted) {
		Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
		List<Throwable> pending = new ArrayList<>(); pending.add(root);
		while (!pending.isEmpty()) {
			Throwable failure = pending.remove(pending.size() - 1);
			if (failure == null || !seen.add(failure)) { continue; }
			if (failure == wanted) { return true; }
			pending.add(failure.getCause()); Collections.addAll(pending, failure.getSuppressed());
		}
		return false;
	}

	static boolean containsCauseCode(Throwable root, String code) {
		Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
		List<Throwable> pending = new ArrayList<>(); pending.add(root);
		while (!pending.isEmpty()) {
			Throwable failure = pending.remove(pending.size() - 1);
			if (failure == null || !seen.add(failure)) { continue; }
			if (code.equals(failure.getMessage())) { return true; }
			pending.add(failure.getCause()); Collections.addAll(pending, failure.getSuppressed());
		}
		return false;
	}

	static final class State {
		final PreG9BR6PlusF3Documents.Observation observation;
		final List<GeoImage> images;
		final List<GeoElement> selection, justCreated;
		final int step, working, maxLayer, mode;
		final List<Integer> hidden;
		final boolean scripts, blocked;
		final DefaultsSnapshot defaults;
		State(AppGeoCeDG app) {
			defaults = new DefaultsSnapshot(app);
			observation = new PreG9BR6PlusF3Documents.Observation(app);
			images = List.copyOf(app.images);
			selection = List.copyOf(app.getSelectionManager().getSelectedGeos());
			justCreated = List.copyOf(app.getActiveEuclidianView().getEuclidianController().getJustCreatedGeos());
			step = app.getKernel().getConstruction().getStep();
			working = app.getLayerWorkspace().getWorkingLayer();
			hidden = app.getLayerWorkspace().getHiddenLayers();
			maxLayer = app.getMaxLayerUsed(); mode = app.getMode();
			scripts = app.isScriptingDisabled(); blocked = app.isBlockUpdateScripts();
		}
		void assertRestored(AppGeoCeDG app, boolean sameInstances) {
			var current = new PreG9BR6PlusF3Documents.Observation(app);
			assertEquals(List.of(), observation.serializableDifferences(current));
			assertEquals(step, app.getKernel().getConstruction().getStep());
			assertEquals(working, app.getLayerWorkspace().getWorkingLayer());
			assertEquals(hidden, app.getLayerWorkspace().getHiddenLayers());
			assertEquals(maxLayer, app.getMaxLayerUsed());
			assertEquals(scripts, app.isScriptingDisabled()); assertEquals(blocked, app.isBlockUpdateScripts());
			assertEquals(observation.commands.size(), undo(app).getCommands().size());
			for (int i = 0; i < observation.commands.size(); i++) {
				assertSame(observation.commands.get(i), undo(app).getCommands().get(i));
			}
			if (sameInstances) {
				defaults.assertConstruction(app);
				assertEquals(images.size(), app.images.size(), "No registered orphan image may remain");
				assertTrue(observation.sameInstances(current)); assertEquals(images, app.images);
				assertEquals(selection, app.getSelectionManager().getSelectedGeos());
				assertEquals(justCreated, app.getActiveEuclidianView().getEuclidianController().getJustCreatedGeos());
				assertEquals(mode, app.getMode());
			} else {
				if (defaults.samePrototypes(app)) { defaults.assertConstruction(app); } else { defaults.assertArchive(app); }
				// The native reader can rebuild a defaults prototype. A fresh ordinary
				// image must still be a real document geo, never an unlabelled orphan.
				GeoElement activePrototype = app.getKernel().getConstruction().getConstructionDefaults()
						.getDefaultGeo(ConstructionDefaults.DEFAULT_IMAGE);
				for (GeoImage image : app.images) {
					if (!images.contains(image) && image != activePrototype) {
						assertTrue(documentImages(app).contains(image), "A rebuilt registry must not retain an orphan image");
					}
				}
			}
		}
	}

	@Test void cancelDoesNotInvokeTheLoaderOrCreateATemporaryBaseline() throws Exception {
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		onEdt(() -> {
			baseline(app); app.getKernel().getConstruction().setStep(0);
			State before = new State(app); AtomicInteger calls = new AtomicInteger();
			var operation = new GeoCeDGClipboardImageInsertion(app);
			operation.setPresentation((target, state) -> {
				assertEquals(2, state.currentLayer()); assertEquals(0, state.currentStep());
				assertTrue(state.hiddenLayers().contains(7)); return null;
			});
			operation.setNativeLoader(target -> { calls.incrementAndGet(); return true; });
			assertNull(operation.insert()); assertEquals(0, calls.get()); before.assertRestored(app, true);
			return null;
		});
	}

	@ParameterizedTest @CsvSource({"-1,AFTER_CURRENT", "0,AFTER_CURRENT", "2,AFTER_CURRENT",
			"-1,APPEND", "0,APPEND", "2,APPEND"})
	void bothPositionsPreserveTheHiddenSuffixAndOwnEveryCorner(int step, Position position) throws Exception {
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		onEdt(() -> {
			DefaultsSnapshot defaults = new DefaultsSnapshot(app);
			baseline(app); defaults.assertConstruction(app); var construction = app.getKernel().getConstruction(); construction.setStep(step);
			List<ConstructionElement> old = elements(app); List<GeoElement> oldGeos = geos(app);
			List<String> oldXml = oldGeos.stream().map(GeoElement::getXML).toList();
			AtomicInteger calls = new AtomicInteger();
			int anchor = position == Position.APPEND ? old.size() - 1 : step;
			assertInserted(operation(app, 7, position, target -> {
				calls.incrementAndGet(); nativeImages(target, 2); return true;
			}).insert());
			assertEquals(1, calls.get()); assertEquals(old.size() + 6, construction.steps());
			defaults.assertConstruction(app);
			assertEquals(anchor + 6, construction.getStep());
			assertEquals(2, app.getLayerWorkspace().getWorkingLayer());
			assertEquals(List.of(7), app.getLayerWorkspace().getHiddenLayers());
			List<ConstructionElement> current = elements(app);
			List<ConstructionElement> retained = current.stream().filter(old::contains).toList();
			assertEquals(old, retained);
			for (int i = 0; i < oldGeos.size(); i++) { assertEquals(oldXml.get(i), oldGeos.get(i).getXML()); }
			Set<GeoElement> newGeos = Collections.newSetFromMap(new IdentityHashMap<>());
			for (int i = 1; i <= 6; i++) { newGeos.add((GeoElement) current.get(anchor + i)); }
			assertEquals(6, newGeos.size());
			for (int block = 0; block < 2; block++) {
				int start = anchor + 1 + block * 3;
				GeoPoint first = assertInstanceOf(GeoPoint.class, current.get(start));
				GeoPoint second = assertInstanceOf(GeoPoint.class, current.get(start + 1));
				GeoImage image = assertInstanceOf(GeoImage.class, current.get(start + 2));
				assertSame(first, image.getStartPoint(0)); assertSame(second, image.getStartPoint(1));
				assertNotSame(first, second); assertFalse(oldGeos.contains(first)); assertFalse(oldGeos.contains(second));
				assertTrue(image.isAuxiliaryObject()); assertFalse(first.isAuxiliaryObject());
				assertFalse(second.isAuxiliaryObject()); assertFalse(first.isLabelVisible());
				assertFalse(second.isLabelVisible()); assertNotNull(image.getFillImage());
				assertTrue(first.getLocateableList().contains(image)); assertTrue(second.getLocateableList().contains(image));
			}
			for (GeoElement created : newGeos) {
				assertEquals(7, created.getLayer()); assertTrue(created.isLabelSet()); assertTrue(created.isDefined());
				assertTrue(oldGeos.stream().noneMatch(previous -> previous.getLabelSimple().equals(created.getLabelSimple())));
			}
			return null;
		});
	}

	@ParameterizedTest @ValueSource(booleans = {false, true})
	void oneUndoPreservesTheLiveBaselineAndDiscardsRedoOnlyAfterSuccess(boolean unstoredEdit) throws Exception {
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		onEdt(() -> { baseline(app); must(app, "Kept=(8,8)"); app.storeUndoInfo(); return null; });
		awaitQuiescent(app);
		onEdt(() -> { must(app, "Discarded=(9,9)"); app.storeUndoInfo(); return null; });
		awaitQuiescent(app);
		onEdt(() -> { app.getKernel().undo(); if (unstoredEdit) { must(app, "Live=(5,6)"); } return null; });
		awaitQuiescent(app);
		List<UndoCommand> prefix = onEdt(() -> { return List.copyOf(undo(app).getCommands().subList(0, undo(app).getCursor())); });
		int preCount = onEdt(() -> { return geos(app).size(); });
		DefaultsSnapshot defaults = onEdt(() -> { return new DefaultsSnapshot(app); });
		onEdt(() -> {
			assertTrue(undo(app).redoPossible());
			assertInserted(operation(app, 0, Position.APPEND, target -> { nativeImages(target, 1); return true; }).insert());
			assertFalse(undo(app).redoPossible());
			defaults.assertConstruction(app);
			assertEquals(prefix.size() + 1 + (unstoredEdit ? 1 : 0), undo(app).getCommands().size());
			for (int i = 0; i < prefix.size(); i++) { assertSame(prefix.get(i), undo(app).getCommands().get(i)); }
			assertEquals(preCount + 3, geos(app).size()); return null;
		});
		for (int cycle = 0; cycle < 3; cycle++) {
			onEdt(() -> {
				app.getKernel().undo(); assertEquals(preCount, geos(app).size()); assertEquals(0, documentImages(app).size());
				defaults.assertConstruction(app);
				assertNotNull(app.getKernel().lookupLabel("Kept")); assertNull(app.getKernel().lookupLabel("Discarded"));
				assertEquals(unstoredEdit, app.getKernel().lookupLabel("Live") != null);
				app.getKernel().redo(); assertEquals(preCount + 3, geos(app).size());
				defaults.assertConstruction(app);
				assertEquals(1, documentImages(app).size()); GeoImage image = documentImages(app).get(0);
				assertNotNull(image.getFillImage()); assertNotNull(image.getStartPoint(0)); assertNotNull(image.getStartPoint(1));
				assertEquals(0, image.getLayer()); assertEquals(0, image.getStartPoint(0).getLayer());
				return null;
			});
		}
	}

	@ParameterizedTest @ValueSource(booleans = {false, true})
	void firstRealClipboardAcquisitionPreservesUnwarmedDefaultsInBlankAndAbcHosts(boolean blank) throws Exception {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard(); Transferable original = clipboard.getContents(null);
		AppGeoCeDG app = onEdt(() -> { return blank ? testApp() : fixture(); });
		BufferedImage pixels = pixels(119);
		try {
			clipboard.setContents(new ImageSelection(pixels), null);
			AtomicReference<DefaultsSnapshot> before = new AtomicReference<>();
			String label = onEdt(() -> {
				assertEquals(blank ? 0 : 3, geos(app).size()); assertTrue(documentImages(app).isEmpty());
				EuclidianViewD view = (EuclidianViewD) app.getEuclidianView1(); view.setSize(new Dimension(640, 480));
				view.updateSize(); view.setRealWorldCoordSystem(-5, 5, -4, 4);
				DefaultsSnapshot defaults = new DefaultsSnapshot(app); before.set(defaults);
				assertNull(defaults.prototypes.get(ConstructionDefaults.DEFAULT_IMAGE).raw(), "No image may warm this host before capture");
				assertNotNull(defaults.prototypes.get(ConstructionDefaults.DEFAULT_POINT_FREE));
				defaults.saveEvidence(directory, "first-real-before-" + blank, app); defaults.assertConstruction(app);
				baseline(app); defaults.assertConstruction(app); AtomicInteger calls = new AtomicInteger();
				var result = operation(app, 0, Position.APPEND, target -> {
					calls.incrementAndGet(); return ((GuiManagerD) target.getGuiManager()).loadImage(null, true);
				}).insert();
				defaults.saveEvidence(directory, "first-real-returned-" + blank, app); assertInserted(result);
				assertEquals(1, calls.get()); assertEquals((blank ? 0 : 3) + 3, geos(app).size());
				defaults.assertConstruction(app); defaults.saveEvidence(directory, "first-real-inserted-" + blank, app);
				GeoImage image = documentImages(app).get(0); assertNotNull(image.getStartPoint(0)); assertNotNull(image.getStartPoint(1));
				assertSame(image.getStartPoint(0), elements(app).get(elements(app).size() - 3));
				assertSame(image.getStartPoint(1), elements(app).get(elements(app).size() - 2));
				assertPixels(pixels, (BufferedImage) ((MyImageD) image.getFillImage()).getImage()); return image.getLabelSimple();
			});
			onEdt(() -> {
				app.getKernel().undo(); assertTrue(documentImages(app).isEmpty()); assertEquals(blank ? 0 : 3, geos(app).size());
				before.get().assertConstruction(app); before.get().saveEvidence(directory, "first-real-undo-" + blank, app);
				app.getKernel().redo(); assertEquals(1, documentImages(app).size()); before.get().assertConstruction(app);
				before.get().saveEvidence(directory, "first-real-redo-" + blank, app); return null;
			});
			File saved = onEdt(() -> { return save(app, directory, "first-real-" + blank); });
			String resource = onEdt(() -> { return ((GeoImage) app.getKernel().lookupLabel(label)).getImageFileName(); });
			assertPixels(pixels, ImageIO.read(new ByteArrayInputStream(resourceBytes(saved, resource))));
			AppGeoCeDG reopened = onEdt(() -> { return testHost(PreG9BCu0Corpus.open(saved)); });
			onEdt(() -> {
				GeoImage image = assertInstanceOf(GeoImage.class, reopened.getKernel().lookupLabel(label));
				assertEquals(0, image.getLayer()); assertEquals(0, image.getStartPoint(0).getLayer()); assertEquals(0, image.getStartPoint(1).getLayer());
				assertPixels(pixels, (BufferedImage) ((MyImageD) image.getFillImage()).getImage());
				GeoElement prototype = reopened.getKernel().getConstruction().getConstructionDefaults().getDefaultGeo(ConstructionDefaults.DEFAULT_IMAGE);
				GeoNumeric literal = assertInstanceOf(GeoNumeric.class, prototype.getAnimationStepObject());
				assertEquals(GeoNumeric.class, literal.getClass()); assertEquals(1, literal.getDouble());
				assertTrue(literal.isIndependent()); assertFalse(literal.isLabelSet()); assertFalse(literal.isInConstructionList());
				assertFalse(geos(reopened).contains(literal));
				DefaultsSnapshot loaded = new DefaultsSnapshot(reopened); AtomicInteger calls = new AtomicInteger();
				assertInserted(operation(reopened, 0, Position.APPEND, target -> { calls.incrementAndGet(); nativeImages(target, 1); return true; }).insert());
				assertEquals(1, calls.get()); loaded.assertConstruction(reopened);
				reopened.getKernel().undo(); loaded.assertConstruction(reopened);
				reopened.getKernel().redo(); loaded.assertConstruction(reopened);
				loaded.saveEvidence(directory, "first-real-file-literal-" + blank, reopened); return null;
			});
		} finally { restoreClipboard(clipboard, original); }
	}

	@ParameterizedTest @ValueSource(strings = {"clipboard-simulated-0.png", "geogebra_thumbnail.png"})
	void realWindowsClipboardIsReadAfterConfirmationAndPersistsItsPixels(String oldResource) throws Exception {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable original = clipboard.getContents(null);
		BufferedImage chosen = pixels(91);
		AppGeoCeDG app = onEdt(() -> { return fixture(); });
		AtomicReference<GeoImage> previousImage = new AtomicReference<>();
		AtomicReference<String> previousResource = new AtomicReference<>();
		AtomicReference<byte[]> previousBytes = new AtomicReference<>();
		try {
			clipboard.setContents(new ImageSelection(pixels(31)), null);
			String label = onEdt(() -> {
				EuclidianViewD view = (EuclidianViewD) app.getEuclidianView1();
				view.setSize(new Dimension(640, 480)); view.updateSize();
				view.setRealWorldCoordSystem(-5, 5, -4, 4);
				GeoImage existing = nativeImages(app, 1).get(0);
				if (!oldResource.equals(existing.getImageFileName())) {
					// This is a referenced native image, even when its member name also
					// names the optional derived thumbnail. Preserve its referenced bytes.
					app.getImageManager().addExternalImage(oldResource, new MyImageD(pixels(13)));
					existing.setImageFileName(oldResource); GeoImage.updateInstances(app);
				}
				previousImage.set(existing);
				previousResource.set(existing.getImageFileName());
				previousBytes.set(resourceBytes(save(app, directory, "clipboard-existing-before"), existing.getImageFileName()));
				String existingXml = existing.getXML();
				baseline(app); AtomicInteger calls = new AtomicInteger();
				var operation = new GeoCeDGClipboardImageInsertion(app);
				operation.setPresentation((target, state) -> {
					assertEquals(0, calls.get(), "The native acquisition has not run before confirmation");
					clipboard.setContents(new ImageSelection(chosen), null); return new Choice(5, Position.APPEND);
				});
				operation.setNativeLoader(target -> {
					calls.incrementAndGet(); return ((GuiManagerD) target.getGuiManager()).loadImage(null, true);
				});
				assertInserted(operation.insert()); assertEquals(1, calls.get());
				assertEquals(2, documentImages(app).size()); assertEquals(existingXml, existing.getXML());
				assertTrue(documentImages(app).contains(existing), "Existing image identity is unchanged");
				return documentImages(app).stream().filter(image -> image != existing).findFirst().orElseThrow().getLabelSimple();
			});
			onEdt(() -> {
				app.getKernel().undo(); assertEquals(1, documentImages(app).size());
				assertNull(app.getKernel().lookupLabel(label));
				assertEquals(previousResource.get(), documentImages(app).get(0).getImageFileName());
				assertArrayEquals(previousBytes.get(), liveResourceBytes(app, previousResource.get()),
						"Undo must preserve the earlier referenced native resource, including the thumbnail member name");
				app.getKernel().redo(); assertEquals(2, documentImages(app).size());
				assertInstanceOf(GeoImage.class, app.getKernel().lookupLabel(label));
				assertArrayEquals(previousBytes.get(), liveResourceBytes(app, previousResource.get())); return null;
			});
			var saved = onEdt(() -> { return save(app, directory, "clipboard-native-pixels"); });
			assertArrayEquals(previousBytes.get(), resourceBytes(saved, previousResource.get()),
					"The earlier native image bytes must survive the insertion");
			String resource = onEdt(() -> { return ((GeoImage) app.getKernel().lookupLabel(label)).getImageFileName(); });
			try (ZipFile archive = new ZipFile(saved)) {
				var entry = archive.getEntry(resource); assertNotNull(entry, "The saved archive must own the image bytes");
				try (var input = archive.getInputStream(entry)) { assertPixels(chosen, ImageIO.read(input)); }
			}
			AppGeoCeDG reopened = onEdt(() -> { return PreG9BCu0Corpus.open(saved); });
			onEdt(() -> {
				assertEquals(2, documentImages(reopened).size());
				GeoImage existing = assertInstanceOf(GeoImage.class,
						reopened.getKernel().lookupLabel(previousImage.get().getLabelSimple()));
				assertEquals(previousResource.get(), existing.getImageFileName());
				assertPixels(pixels(13), (BufferedImage) ((MyImageD) existing.getFillImage()).getImage());
				GeoImage image = assertInstanceOf(GeoImage.class, reopened.getKernel().lookupLabel(label));
				assertEquals(resource, image.getImageFileName()); assertEquals(5, image.getLayer());
				assertEquals(5, image.getStartPoint(0).getLayer()); assertEquals(5, image.getStartPoint(1).getLayer());
				assertPixels(chosen, (BufferedImage) ((MyImageD) image.getFillImage()).getImage()); return null;
			});
		} finally {
			// Preserve the actual prior clipboard owner data even if the native call fails.
			restoreClipboard(clipboard, original);
		}
	}

	private static byte[] liveResourceBytes(AppGeoCeDG app, String name) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		app.getXMLio().writeGeoGebraFile(bytes, false);
		try (ZipInputStream archive = new ZipInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
			for (var entry = archive.getNextEntry(); entry != null; entry = archive.getNextEntry()) {
				if (name.equals(entry.getName())) { return archive.readAllBytes(); }
			}
		}
		throw new AssertionError("Missing referenced native resource: " + name);
	}

	private static byte[] resourceBytes(File file, String name) throws IOException {
		try (ZipFile archive = new ZipFile(file)) {
			var entry = archive.getEntry(name); assertNotNull(entry, "Existing native resource entry");
			try (var input = archive.getInputStream(entry)) { return input.readAllBytes(); }
		}
	}

	static void assertPixels(BufferedImage expected, BufferedImage actual) {
		assertNotNull(actual); assertEquals(expected.getWidth(), actual.getWidth()); assertEquals(expected.getHeight(), actual.getHeight());
		assertArrayEquals(expected.getRGB(0, 0, expected.getWidth(), expected.getHeight(), null, 0, expected.getWidth()),
				actual.getRGB(0, 0, actual.getWidth(), actual.getHeight(), null, 0, actual.getWidth()));
	}
}
