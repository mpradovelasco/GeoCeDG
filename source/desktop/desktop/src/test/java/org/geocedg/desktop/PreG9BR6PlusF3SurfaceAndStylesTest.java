/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Documents.M;
import static org.geocedg.desktop.PreG9BR6PlusF3Documents.MM;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.app;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.awaitQuiescent;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.baseline;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.save;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.undo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.geogebra.common.awt.GColor;
import org.geogebra.common.kernel.ConstructionDefaults;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.desktop.main.AppD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-R6-plus-F3 surface (N1, A1, X1, OFF-B, H-1/H-3, F-1) and the T0 style
 * operation "Apply Styles…" (P5).
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusF3SurfaceAndStylesTest {

	@TempDir
	Path dir;

	private static List<String> actionIds(JMenu menu) {
		List<String> ids = new ArrayList<>();
		for (Component child : menu.getMenuComponents()) {
			if (child instanceof JMenuItem item && !(child instanceof JMenu)) {
				Object id = item.getClientProperty(GeoCeDGActionRegistry.ACTION_ID);
				if (id != null) {
					ids.add(id.toString());
				}
			}
		}
		return ids;
	}

	@Test
	void f3u01FileInsertHoldsBothActionsAfterTheOpenGroup() {
		AppGeoCeDG app = app(MM);
		app.getGuiManager().initMenubar();
		GeoCeDGMenuBar bar = (GeoCeDGMenuBar) app.getGuiManager().getMenuBar();
		JMenu file = bar.getMenu(0);
		JMenu insert = null;
		int openIndex = -1;
		int insertIndex = -1;
		Component[] children = file.getMenuComponents();
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof JMenu submenu
					&& "file-insert".equals(submenu.getClientProperty(
							"geocedg.presentation.group.id"))) {
				insert = submenu;
				insertIndex = i;
			} else if (children[i] instanceof JMenuItem item && "document.open".equals(
					item.getClientProperty(GeoCeDGActionRegistry.ACTION_ID))) {
				openIndex = i;
			}
		}
		assertNotNull(insert, "File -> Insert");
		assertTrue(openIndex >= 0 && insertIndex > openIndex);
		assertEquals("Insert", insert.getText());
		assertEquals(List.of("document.insert-file", "document.apply-styles"), actionIds(insert));
		for (Component child : insert.getMenuComponents()) {
			assertNull(((JMenuItem) child).getAccelerator(), "no accelerator");
		}
		assertEquals(133, GeoCeDGProfile.getActions().size());
	}

	@Test
	void f3u02NamesAreLocalized() {
		assertEquals("Insert File…",
				GeoCeDGProfile.getText("geocedg.document.InsertFile.name", "en"));
		assertEquals("Insertar archivo…",
				GeoCeDGProfile.getText("geocedg.document.InsertFile.name", "es"));
		assertEquals("Apply Styles…",
				GeoCeDGProfile.getText("geocedg.document.ApplyStyles.name", "en"));
		assertEquals("Aplicar estilos…",
				GeoCeDGProfile.getText("geocedg.document.ApplyStyles.name", "es"));
		assertEquals("Insertar", GeoCeDGProfile.getText("Group.File.Insert", "es"));
		for (String key : new String[] {"Insert.Rejected", "Insert.Serious",
				"Insert.Failed.Reloaded", "Open.Reject.Off", "Styles.Rejected"}) {
			assertNotEquals(GeoCeDGProfile.getText(key, "en"), GeoCeDGProfile.getText(key, "es"));
		}
	}

	@Test
	void f3u03TheActionsRunThroughTheRegistryAndCancelDoesNothing() throws Exception {
		AppGeoCeDG app = app(MM);
		baseline(app);
		File source = PreG9BR6PlusF3Documents.single(dir, "u3", "W=(3,3)");
		List<String> messages = new ArrayList<>();
		app.setInsertionMessageSink(messages::add);
		GeoCeDGActionRegistry registry = ((GuiManagerGeoCeDG) app.getGuiManager())
				.getActionRegistry();
		app.setInsertionSourceChooser((host, title) -> null);
		onEdt(() -> registry.invoke("document.insert-file", null));
		assertNull(app.getKernel().lookupLabel("W"));
		// X1: a missing name resolves to the .cedg stem
		app.setInsertionSourceChooser((host, title) -> new File(dir.toFile(), "u3"));
		onEdt(() -> registry.invoke("document.insert-file", null));
		assertNotNull(app.getKernel().lookupLabel("W"));
		assertTrue(messages.isEmpty(), messages.toString());
		app.setInsertionSourceChooser((host, title) -> new File(dir.toFile(), "u3.off"));
		onEdt(() -> registry.invoke("document.apply-styles", null));
		assertEquals(1, messages.size());
		assertTrue(messages.get(0).contains("OFF"), messages.get(0));
	}

	@Test
	void f3u04GeoCeDGOpenRefusesOffAndResolvesDocumentNames() throws Exception {
		AppGeoCeDG app = app(MM);
		List<String> messages = new ArrayList<>();
		app.setInsertionMessageSink(messages::add);
		File off = dir.resolve("mesh.off").toFile();
		Files.writeString(off.toPath(), "OFF\n0 0 0\n");
		File doc = PreG9BR6PlusF3Documents.single(dir, "u4", "W=(3,3)");
		GuiManagerGeoCeDG gui = (GuiManagerGeoCeDG) app.getGuiManager();
		assertTrue(gui.openResolved(new File[] {off}).isEmpty());
		assertTrue(messages.get(0).contains("OFF"), messages.get(0));
		assertNull(GeoCeDGDocumentPolicy.resolveOpenName(dir.resolve("mesh").toFile()),
				"a stem never resolves to .off");
		onEdt(() -> gui.doOpenFiles(new File[] {off}, true));
		assertEquals(2, messages.size());
		List<File> opened = onEdt(() -> gui.openResolved(new File[] {dir.resolve("u4").toFile()}));
		assertEquals(List.of(doc), opened);
		assertEquals(doc, app.getCurrentFile());
		assertNotNull(app.getKernel().lookupLabel("W"));
		assertEquals(GeoCeDGDocumentPolicy.resolveInsertionName(dir.resolve("u4").toFile()), doc);
		File tool = dir.resolve("x.ggt").toFile();
		Files.writeString(tool.toPath(), "tool");
		assertEquals("Insert.Reject.Extension", GeoCeDGDocumentPolicy.insertionRefusal(tool));
	}

	@Test
	void f3u05ClassicAndTheHelperStayOutOfTheProductSurface() throws Exception {
		AppD classic = PreG9BR6PlusF3Fixtures.classic();
		assertFalse(classic.getGuiManager() instanceof GuiManagerGeoCeDG);
		AppGeoCeDG app = app(MM);
		baseline(app);
		File source = PreG9BR6PlusF3Documents.single(dir, "u5", "W=(3,3)");
		int recent = AppD.getFileListSize();
		int managers = G9U1TestApp.dockManagers().size();
		PreG9BR6PlusF3Fixtures.insert(app, source);
		assertEquals(recent, AppD.getFileListSize(), "H-1: no recent-file entry");
		assertEquals(managers, G9U1TestApp.dockManagers().size(), "H-3: helpers released");
	}

	// ------------------------------------------------------------------ T0

	private static File styleSource(Path dir) {
		AppGeoCeDG s = app(M);
		ConstructionDefaults defaults = s.getKernel().getConstruction().getConstructionDefaults();
		GeoPoint point = (GeoPoint) defaults.getDefaultGeo(ConstructionDefaults.DEFAULT_POINT_FREE);
		point.setObjColor(GColor.RED);
		point.setPointSize(9);
		point.setPointStyle(2);
		GeoElement segment = defaults.getDefaultGeo(ConstructionDefaults.DEFAULT_SEGMENT);
		segment.setLineThickness(9);
		segment.setLineType(15);
		segment.setObjColor(GColor.BLUE);
		GeoElement polygon = defaults.getDefaultGeo(ConstructionDefaults.DEFAULT_POLYGON);
		polygon.setAlphaValue(0.8);
		must(s, "T=(9,9)");
		return save(s, dir, "styles");
	}

	private static AppGeoCeDG styleTarget() throws Exception {
		AppGeoCeDG t = app(MM);
		for (String c : new String[] {"A=(0,0)", "B=(4,0)", "C=(4,3)", "sg=Segment(A,B)",
				"pol=Polygon(A,B,C)", "tx=\"note\"", "s=0", "Q=(s,1)",
				"D={false,{-2,2,true,true}}", "Lt=LocusV2(Q,s,D)",
				"dl=LinearDimension(B,C,Vector((1,0)),6)", "Tp=(40,40)",
				"Tsheet=IsoABorder(Tp,4,true,1,1,1.0,true)"}) {
			must(t, c);
		}
		GeoElement a = t.getKernel().lookupLabel("A");
		a.setCaption("first");
		a.setLayer(2);
		a.setFixed(true);
		t.getKernel().lookupLabel("sg").setLayer(3);
		t.getLayerWorkspace().setLayerHidden(3, true);
		baseline(t);
		return t;
	}

	/** Every property T0 must not touch, plus the six it may. */
	private static Map<String, String> styles(AppGeoCeDG app) {
		Map<String, String> styles = new LinkedHashMap<>();
		for (GeoElement g : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			styles.put(g.getLabelSimple(), g.getObjectColor() + "|" + g.getLineThickness() + "|"
					+ g.getLineType() + "|" + (g instanceof GeoPoint p ? p.getPointSize() + "/"
							+ p.getPointStyle() : "-") + "|" + g.getAlphaValue());
		}
		return styles;
	}

	private static Map<String, String> invariants(AppGeoCeDG app) {
		Map<String, String> fixed = new LinkedHashMap<>();
		for (GeoElement g : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			fixed.put(g.getLabelSimple(), g.getLayer() + "|" + g.getRawCaption() + "|"
					+ g.isLocked() + "|" + g.isEuclidianVisible() + "|" + g.getLabelVisible()
					+ "|" + g.toValueString(org.geogebra.common.kernel.StringTemplate.xmlTemplate)
					+ "|" + (g.getParentAlgorithm() == null ? "-"
							: g.getParentAlgorithm().getClass().getSimpleName()));
		}
		return fixed;
	}

	@Test
	void f3t01OnlyTheSixPropertiesChangeAndExclusionsAreKept() throws Exception {
		AppGeoCeDG target = styleTarget();
		File source = styleSource(dir);
		Map<String, String> before = styles(target);
		final Map<String, String> fixed = invariants(target);
		final String xml = GeoCeDGDocumentInsertion.Certificate.normalize(target.getXML());
		final List<Integer> hidden = target.getLayerWorkspace().getHiddenLayers();
		final Object units = target.getDocumentUnits().getState();
		final int cursor = undo(target).getCursor();
		GeoCeDGStyleApplication.Result result = onEdt(() -> target.getStyleApplication()
				.apply(source));
		assertTrue(result.isApplied());
		assertTrue(result.getRestyled() > 0);
		awaitQuiescent(target);
		Map<String, String> after = styles(target);
		final String xmlAfter = GeoCeDGDocumentInsertion.Certificate.normalize(target.getXML());
		assertNotEquals(before.get("A"), after.get("A"));
		assertTrue(after.get("A").startsWith(GColor.RED.toString()), after.get("A"));
		assertNotEquals(before.get("sg"), after.get("sg"));
		for (GeoElement g : target.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			if (GeoCeDGStyleApplication.isExcluded(g)) {
				assertEquals(before.get(g.getLabelSimple()), after.get(g.getLabelSimple()),
						"excluded " + g.getLabelSimple());
			}
		}
		assertEquals(fixed, invariants(target), "geometry, layers, captions, fixed, visibility");
		assertEquals(hidden, target.getLayerWorkspace().getHiddenLayers());
		assertEquals(units, target.getDocumentUnits().getState());
		assertEquals(cursor + 1, undo(target).getCursor(), "one undo step");
		assertFalse(target.isSaved());
		onEdt(() -> undo(target).undo());
		awaitQuiescent(target);
		assertEquals(xml, GeoCeDGDocumentInsertion.Certificate.normalize(target.getXML()));
		onEdt(() -> undo(target).redo());
		// compared as document XML: text line thickness is not serialized (inherited)
		assertEquals(xmlAfter, GeoCeDGDocumentInsertion.Certificate.normalize(target.getXML()));
	}

	@Test
	void f3t02TheSelectionLimitsTheScope() throws Exception {
		AppGeoCeDG target = styleTarget();
		File source = styleSource(dir);
		Map<String, String> before = styles(target);
		target.getSelectionManager().setSelectedGeos(new ArrayList<>(List.of(
				target.getKernel().lookupLabel("B"))));
		GeoCeDGStyleApplication.Result result = onEdt(() -> target.getStyleApplication()
				.apply(source));
		assertEquals(1, result.getRestyled());
		Map<String, String> after = styles(target);
		for (String label : before.keySet()) {
			if (!"B".equals(label)) {
				assertEquals(before.get(label), after.get(label), label);
			}
		}
		assertNotEquals(before.get("B"), after.get("B"));
	}

	@Test
	void f3t03AnInvalidSourceChangesNothing() throws Exception {
		AppGeoCeDG target = styleTarget();
		Path garbage = dir.resolve("broken.cedg");
		Files.writeString(garbage, "not a zip");
		String xml = target.getXML();
		int cursor = undo(target).getCursor();
		for (File bad : new File[] {garbage.toFile(), dir.resolve("missing.cedg").toFile()}) {
			GeoCeDGStyleApplication.Result result = onEdt(() -> target.getStyleApplication()
					.apply(bad));
			assertFalse(result.isApplied());
			assertTrue(result.getMessage().contains("Nothing changed"), result.getMessage());
		}
		assertEquals(xml, target.getXML());
		assertEquals(cursor, undo(target).getCursor());
		assertTrue(target.isSaved());
		assertTrue(target.getKernel().lookupLabel("tx") instanceof GeoText);
		assertTrue(target.getKernel().lookupLabel("sg") instanceof GeoSegment);
	}
}
