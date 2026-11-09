/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.JPanel;

import org.geocedg.common.document.controls.ControlLayout;
import org.geocedg.common.document.controls.ControlLayout.Anchor;
import org.geocedg.common.document.controls.ControlLayout.Panel;
import org.geocedg.common.document.controls.ControlLayout.Slot;
import org.geogebra.common.euclidian.draw.DrawSlider;
import org.geogebra.common.kernel.geos.AbsoluteScreenLocateable;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoInputBox;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.main.App;
import org.geogebra.common.main.settings.LabelVisibility;
import org.geogebra.common.plugin.Event;
import org.geogebra.common.plugin.EventType;
import org.geogebra.common.plugin.ScriptType;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.geogebra3D.App3D;
import org.geogebra.desktop.main.AppD;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

@ExtendWith(G9U1TestApp.Lifecycle.class)
class Cu8ControlLayoutClassicTest {
	private static final String LAYOUT = "geocedgControlLayout";
	private static final String SLOT = "geocedgControlSlot";
	private static final String CLICK_SCRIPT = "SetValue[n, 4]";
	private static final List<String> LABELS = List.of("n", "push", "check", "edit");

	@TempDir
	Path temporary;

	@Test
	void classicOpensAtNativeFallbackAndResavesBothAnchors() throws Exception {
		onEdt(() -> {
			Fixture source = fixture();
			AppGeoCeDG authoring = source.app();
			GeoElement slider = named(authoring, "n");
			GeoElement button = named(authoring, "push");
			authoring.setLabelingStyle(LabelVisibility.AlwaysOn.getValue());
			slider.setLabelMode(GeoElement.LABEL_DEFAULT);
			assertEquals(GeoElement.LABEL_NAME_VALUE, slider.getLabelMode());
			assertTrue(button.getScript(EventType.CLICK).run(new Event(EventType.CLICK, button)));
			((GeoInputBox) named(authoring, "edit")).updateLinkedGeo("4");
			assertSame(slider, named(authoring, "n"));
			assertNotNull(slider.getDefinition());
			authoring.setLabelingStyle(LabelVisibility.AlwaysOff.getValue());
			assertControls(authoring, 4);
			assertTrue(authoring.saveGeoGebraFile(source.archive()));
			String sourceXml = archiveXml(source.archive().toPath());
			assertSliderMode(sourceXml, GeoElement.LABEL_NAME_VALUE);
			assertSliderMode(authoring.getKernel().getConstruction().getCurrentUndoXML(true).toString(),
					GeoElement.LABEL_NAME_VALUE);

			AppD classic = classic();
			classic.setLabelingStyle(LabelVisibility.AlwaysOff.getValue());
			assertTrue(classic.loadFile(source.archive(), false));
			assertRegistry(classic, source);
			assertControls(classic, 4);
			assertClassicSliderOrigin(classic);
			assertEquals(GeoElement.LABEL_NAME_VALUE, named(classic, "n").getLabelMode());

			Path saved = temporary.resolve("classic-resaved.cedg");
			assertTrue(classic.saveGeoGebraFile(saved.toFile()));
			String savedXml = archiveXml(saved);
			assertEquals(constructionXml(sourceXml), constructionXml(savedXml));
			assertTrue(savedXml.contains(LAYOUT));
			assertEquals(LABELS.size(), savedXml.split(SLOT, -1).length - 1);

			AppGeoCeDG reopened = G9U1TestApp.create();
			assertTrue(reopened.loadFile(saved.toFile(), false));
			assertRegistry(reopened, source);
			assertControls(reopened, 4);
			assertEquals(GeoElement.LABEL_NAME_VALUE, named(reopened, "n").getLabelMode());

			named(classic, "n").setLabelMode(GeoElement.LABEL_NAME_VALUE);
			Path explicit = temporary.resolve("classic-explicit-name-value.cedg");
			assertTrue(classic.saveGeoGebraFile(explicit.toFile()));
			assertSliderMode(archiveXml(explicit), GeoElement.LABEL_DEFAULT_NAME_VALUE);
			assertTrue(reopened.loadFile(explicit.toFile(), false));
			assertEquals(GeoElement.LABEL_NAME_VALUE, named(reopened, "n").getLabelMode());
			assertRegistry(reopened, source);
			assertControls(reopened, 4);
			return null;
		});
	}

	@Test
	void classicUndoRedoAndRejectedOpenKeepLiveSlots() throws Exception {
		Fixture source = onEdt(this::fixture);
		Path invalid = temporary.resolve("orphan-layout.cedg");
		Path compatibility = temporary.resolve("marked.ggb");
		File replacement = temporary.resolve("valid-replacement.cedg").toFile();
		Files.copy(source.archive().toPath(), compatibility);
		String withoutSlots = archiveXml(source.archive().toPath())
				.replaceAll(" " + SLOT + "=\"[^\"]*\"", "");
		assertTrue(withoutSlots.contains(LAYOUT));
		assertFalse(withoutSlots.contains(SLOT));
		try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(invalid))) {
			zip.putNextEntry(new ZipEntry("geogebra.xml"));
			zip.write(withoutSlots.getBytes(StandardCharsets.UTF_8));
		}

		FailingClassic classic = onEdt(() -> {
			AppGeoCeDG incoming = G9U1TestApp.create();
			eval(incoming, "P=(1,2)");
			assertTrue(incoming.saveGeoGebraFile(replacement));

			FailingClassic app = G9U1TestApp.withoutWindowDispatcher(new FailingClassic());
			app.setErrorDialogsActive(false);
			assertTrue(app.loadFile(source.archive(), false));
			return app;
		});
		CountDownLatch stored = new CountDownLatch(1);
		onEdt(() -> {
			GeoNumeric slider = (GeoNumeric) named(classic, "n");
			slider.setValue(4);
			slider.updateCascade();
			undo(classic).addUndoInfoStoredListener(stored::countDown);
			classic.getKernel().storeUndoInfo();
			return null;
		});
		assertTrue(stored.await(5, TimeUnit.SECONDS), "Classic stored the value edit");
		onEdt(() -> {
			assertTrue(undo(classic).undoPossible());
			classic.getKernel().undo();
			assertRegistry(classic, source);
			assertControls(classic, 3);
			assertTrue(undo(classic).redoPossible());

			for (Path rejected : List.of(invalid, compatibility)) {
				assertFalse(classic.loadFile(rejected.toFile(), false));
				assertRedoPending(classic, source);
			}
			classic.failBeforePublication = true;
			assertFalse(classic.loadFile(replacement, false));
			assertFalse(classic.failBeforePublication, "failure happened after successful preflight");
			assertRedoPending(classic, source);

			classic.getKernel().redo();
			assertRegistry(classic, source);
			assertControls(classic, 4);
			assertFalse(undo(classic).redoPossible());
			return null;
		});
	}

	@Test
	void replacingWithUnmarkedLegacyFileClearsLayoutInBothProfiles() throws Exception {
		Path legacy = PreG9BR6PlusD1LegacyFingerprints.findRepositoryRoot().resolve(
				"models/regression/pre-g9b-s3-text-view-scaling/pre-g9b-s3-text-view-scaling.ggb");
		byte[] sourceBytes = Files.readAllBytes(legacy);
		onEdt(() -> {
			Fixture source = fixture();
			for (AppD app : List.of(classic(), G9U1TestApp.create())) {
				assertTrue(app.loadFile(source.archive(), false));
				assertFalse(layout(app).isEmpty());
				assertTrue(app.loadFile(legacy.toFile(), false));
				assertTrue(layout(app).isEmpty());
				Path target = temporary.resolve(app.getClass().getSimpleName() + "-legacy.cedg");
				assertTrue(app.saveGeoGebraFile(target.toFile()));
				String xml = archiveXml(target);
				assertFalse(xml.contains(LAYOUT));
				assertFalse(xml.contains(SLOT));
			}
			return null;
		});
		assertArrayEquals(sourceBytes, Files.readAllBytes(legacy));
	}

	private Fixture fixture() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoNumeric slider = (GeoNumeric) eval(app, "n=Slider(0,10,1)");
		eval(app, "push=Button(\"Run\")");
		GeoBoolean checkbox = (GeoBoolean) eval(app, "check=Checkbox(\"Enabled\")");
		eval(app, "edit=InputBox(n)");
		eval(app, "dependent=2*n");
		slider.setValue(3);
		slider.updateCascade();
		slider.setAbsoluteScreenLocActive(true);
		slider.setSliderFixed(false);
		slider.setSliderWidth(145, true);
		checkbox.setValue(true);
		checkbox.setCheckboxFixed(false);
		named(app, "push").setClickScript(app.createScript(
				ScriptType.GGBSCRIPT, CLICK_SCRIPT, false));

		ControlLayout layout = layout(app);
		Panel fixed = layout.addPanel(Anchor.FIXED, 410, 220, 340, 180, false);
		Panel world = layout.addPanel(Anchor.WORLD, -2, 3, 210, 120, true);
		Map<String, Slot> slots = new LinkedHashMap<>();
		for (int index = 0; index < LABELS.size(); index++) {
			String label = LABELS.get(index);
			GeoElement owner = named(app, label);
			owner.setViewFlags(List.of(App.VIEW_EUCLIDIAN));
			owner.setVisibleInView3D(false);
			owner.setVisibleInViewForPlane(false);
			owner.setEuclidianVisible(true);
			owner.setLayer(index);
			((AbsoluteScreenLocateable) owner).setAbsoluteScreenLoc(41 + 30 * index,
					53 + 31 * index);
			assertTrue(layout.isEligible(owner), label);
			slots.put(label, layout.addSlot(index < 2 ? fixed.id() : world.id(),
					owner, 10 * index, 15 * index));
		}
		File archive = temporary.resolve("layout.cedg").toFile();
		assertTrue(app.saveGeoGebraFile(archive));
		return new Fixture(app, archive, List.of(fixed, world), slots);
	}

	private static AppD classic() {
		AppD app = G9U1TestApp.withoutWindowDispatcher(
				new App3D(new CommandLineArguments(new String[] {"--silent"}), new JPanel()));
		app.setErrorDialogsActive(false);
		return app;
	}

	private static void assertRegistry(AppD app, Fixture source) {
		ControlLayout layout = layout(app);
		assertEquals(source.panels().size(), layout.panels().size());
		for (Panel panel : source.panels()) {
			assertEquals(panel, layout.panel(panel.id()));
			assertEquals(2, layout.slots(panel.id()).size());
		}
		source.slots().forEach((label, expected) -> {
			GeoElement owner = named(app, label);
			Slot slot = layout.slot(owner);
			assertNotNull(slot, label);
			assertSame(owner, slot.control(), label);
			assertEquals(expected.id(), slot.id(), label);
			assertEquals(expected.panelId(), slot.panelId(), label);
			assertEquals(expected.dx(), slot.dx(), label);
			assertEquals(expected.dy(), slot.dy(), label);
		});
	}

	private static void assertControls(AppD app, double value) {
		GeoNumeric slider = (GeoNumeric) named(app, "n");
		GeoNumeric dependent = (GeoNumeric) named(app, "dependent");
		assertEquals(value, slider.getDouble());
		assertEquals(145, slider.getSliderWidth());
		assertEquals(2 * value, dependent.getDouble());
		assertSame(slider, dependent.getParentAlgorithm().getInput(0));
		assertSame(slider, ((GeoInputBox) named(app, "edit")).getLinkedGeo());
		assertTrue(((GeoBoolean) named(app, "check")).getBoolean());
		assertEquals(CLICK_SCRIPT, named(app, "push").getScript(EventType.CLICK).getText());
		for (int index = 0; index < LABELS.size(); index++) {
			GeoElement geo = named(app, LABELS.get(index));
			AbsoluteScreenLocateable position = (AbsoluteScreenLocateable) geo;
			assertTrue(position.isAbsoluteScreenLocActive());
			assertEquals(41 + 30 * index, position.getAbsoluteScreenLocX());
			assertEquals(53 + 31 * index, position.getAbsoluteScreenLocY());
			assertEquals(index, geo.getLayer());
		}
	}

	private static void assertClassicSliderOrigin(AppD classic) {
		var view = classic.getEuclidianView1();
		var drawable = assertInstanceOf(DrawSlider.class, view.getDrawableFor(named(classic, "n")));
		var insets = view.getSafeAreaInsets();
		assertEquals(Math.max(41, insets.getLeft()), drawable.getSliderLocation().getX());
		assertEquals(Math.max(53, insets.getTop()), drawable.getSliderLocation().getY());
	}

	private static void assertRedoPending(AppD classic, Fixture source) {
		assertEquals(source.archive(), classic.getCurrentFile());
		assertRegistry(classic, source);
		assertControls(classic, 3);
		assertTrue(undo(classic).redoPossible());
	}

	private static GeoElement named(AppD app, String label) {
		GeoElement geo = app.getKernel().lookupLabel(label);
		assertNotNull(geo, label);
		return geo;
	}

	private static ControlLayout layout(AppD app) {
		return app.getKernel().getConstruction().getControlLayout();
	}

	private static UndoManagerD undo(AppD app) {
		return (UndoManagerD) app.getKernel().getConstruction().getUndoManager();
	}

	private static String archiveXml(Path file) throws Exception {
		try (ZipFile zip = new ZipFile(file.toFile());
				var input = zip.getInputStream(zip.getEntry("geogebra.xml"))) {
			return new String(input.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private static void assertSliderMode(String xml, int mode) {
		int start = xml.indexOf("<element type=\"numeric\" label=\"n\" ");
		assertTrue(start >= 0);
		int end = xml.indexOf("</element>", start);
		assertTrue(xml.substring(start, end).contains("<labelMode val=\"" + mode + "\"/>"));
	}

	private static String constructionXml(String xml) {
		return xml.substring(xml.indexOf("<construction "), xml.indexOf("</construction>"));
	}

	private record Fixture(AppGeoCeDG app, File archive, List<Panel> panels, Map<String, Slot> slots) {
	}

	private static final class FailingClassic extends App3D {
		private boolean failBeforePublication;

		private FailingClassic() {
			super(new CommandLineArguments(new String[] {"--silent"}), new JPanel());
		}

		@Override
		protected void beforeNativeUndoBaselineCommit() {
			if (failBeforePublication) {
				failBeforePublication = false;
				throw new IllegalStateException("injected native open failure");
			}
		}
	}
}
