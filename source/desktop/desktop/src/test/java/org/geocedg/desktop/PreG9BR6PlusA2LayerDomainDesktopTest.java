/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.G9U1TestApp.lookup;
import static org.geocedg.desktop.PreG9BR6PlusA1LayerWorkspaceTest.algebraView;
import static org.geocedg.desktop.PreG9BR6PlusA1LayerWorkspaceTest.pressEye;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.sized;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.awt.GPoint;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.euclidian.event.PointerEventType;
import org.geogebra.common.gui.dialog.ToolCreationDialogModel;
import org.geogebra.common.gui.view.algebra.AlgebraView.SortMode;
import org.geogebra.common.jre.io.MyXMLioJre;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.AppConfig;
import org.geogebra.common.main.settings.config.AppConfigDefault;
import org.geogebra.common.util.InternalClipboard;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.awt.GGraphics2DD;
import org.geogebra.desktop.headless.AppDNoGui;
import org.geogebra.desktop.headless.GFileHandler;
import org.geogebra.desktop.io.AtomicDocumentFileWriter;
import org.geogebra.desktop.main.LocalizationD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * PRE-G9B-R6-plus-A-2 T-WORKSPACE-DOMAIN, T-DOMAIN-ROUTES and T-DOMAIN-CONSUMERS
 * (Desktop rows) and T-CLASSIC (Desktop): the GeoCeDG layer domain 0..99 in the
 * workspace, its chooser, the status bar, the Algebra View, paste, macro and Locus V2
 * outputs, hit testing and the draw order; Classic keeps 0..9.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusA2LayerDomainDesktopTest {
	private static final GColor RED = GColor.newColor(220, 0, 0);
	private static final GColor BLUE = GColor.newColor(0, 0, 210);

	@TempDir
	Path temporary;

	// ------------------------------------------------------- T-WORKSPACE-DOMAIN

	@Test
	void theChooserAndTheStatusBarOfferTheConfiguredDomain() {
		AppGeoCeDG app = G9U1TestApp.create();
		app.setLocale(Locale.ENGLISH);
		GeoCeDGLayerWorkspace workspace = app.getLayerWorkspace();
		assertEquals(99, workspace.getMaxLayer());
		assertEquals(app.getConfig().getMaxLayer(), workspace.getMaxLayer(),
				"DQ-A2-11: derived from the configured authority");
		List<Object> messages = new ArrayList<>();
		AtomicReference<Object[]> offered = new AtomicReference<>();
		int[] answer = {99};
		try (MockedStatic<JOptionPane> pane = Mockito.mockStatic(JOptionPane.class)) {
			pane.when(() -> JOptionPane.showInputDialog(any(), any(), any(), anyInt(), any(),
					any(Object[].class), any())).thenAnswer(invocation -> {
						messages.add(invocation.getArgument(1));
						offered.set(invocation.getArgument(5));
						return answer[0];
					});
			assertTrue(app.chooseWorkingLayer());
			assertEquals(99, workspace.getWorkingLayer());
			app.setLocale(Locale.forLanguageTag("es"));
			answer[0] = 42;
			assertTrue(app.chooseWorkingLayer());
		}
		assertEquals(List.of("Layer for new objects (0 to 99). A hidden layer chosen here is"
				+ " shown.", "Capa para los objetos nuevos (de 0 a 99). Una capa oculta"
						+ " elegida aquí se muestra."), messages);
		assertEquals(100, offered.get().length);
		assertEquals(0, offered.get()[0]);
		assertEquals(99, offered.get()[99]);
		assertEquals(42, workspace.getWorkingLayer());
		assertEquals("Capa: 42", app.getStatusBar()
				.getSegment(GeoCeDGStatusBar.LAYER_SEGMENT).getText());
		assertThrows(IllegalArgumentException.class, () -> workspace.setWorkingLayer(100));
		assertThrows(IllegalArgumentException.class, () -> workspace.setLayerHidden(100, true));
		assertThrows(IllegalArgumentException.class, () -> workspace.setWorkingLayer(-1));
		assertEquals(42, workspace.getWorkingLayer());
		assertEquals(42, eval(app, "A=(1,1)").getLayer(), "interactive creation on 42");
	}

	@Test
	void theProductBoundIsTheOnlyNinetyNineLiteralOfTheLayerSites() throws Exception {
		Path root = G9U1AlgebraGestureEditingTest.findRepositoryRoot();
		String shared = "source/shared/common/src/main/java/";
		String desktop = "source/desktop/desktop/src/main/java/org/geocedg/desktop/";
		List<String> sites = new ArrayList<>(List.of(
				shared + "org/geogebra/common/main/AppConfig.java",
				shared + "org/geogebra/common/main/App.java",
				shared + "org/geogebra/common/kernel/geos/GeoElement.java",
				shared + "org/geogebra/common/kernel/ConstructionDefaults.java",
				shared + "org/geogebra/common/kernel/scripting/CmdShowHideLayer.java",
				shared + "org/geogebra/common/plugin/GgbAPI.java",
				shared + "org/geogebra/common/gui/dialog/options/model/LayerModel.java",
				shared + "org/geogebra/common/properties/impl/objects/LayerProperty.java",
				shared + "org/geogebra/common/geogebra3D/euclidian3D/draw/Drawable3D.java",
				shared + "org/geogebra/common/io/MyXMLHandler.java",
				shared + "org/geogebra/common/io/MyXMLio.java",
				desktop + "AppGeoCeDG.java", desktop + "GeoCeDGLayerWorkspace.java",
				desktop + "GeoCeDGWorkingLayerChooser.java", desktop + "GeoCeDGAlgebraView.java",
				desktop + "GeoCeDGStatusBar.java"));
		try (Stream<Path> layers = Files.list(
				root.resolve(shared + "org/geocedg/common/kernel/layers"))) {
			layers.forEach(path -> sites.add(root.relativize(path).toString()));
		}
		Pattern literal = Pattern.compile("(?<![\\w.])99(?![\\w.])");
		for (String site : sites) {
			String source = Files.readString(root.resolve(site), StandardCharsets.UTF_8);
			assertFalse(literal.matcher(source).find(), "DQ-A2-11: no 99 literal in " + site);
		}
		String config = Files.readString(root.resolve(shared
				+ "org/geocedg/common/main/settings/config/AppConfigGeoCeDG.java"),
				StandardCharsets.UTF_8);
		assertEquals(1, literal.matcher(config).results().count(), "only L_MAX");
		assertTrue(config.contains("public static final int L_MAX = 99;"));
	}

	@Test
	void theAlgebraViewOrdersAndHidesGroupsAboveNine() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoCeDGLayerWorkspace workspace = app.getLayerWorkspace();
		for (int layer : new int[] {99, 10, 2, 42, 9}) {
			workspace.setWorkingLayer(layer);
			eval(app, "P_{" + layer + "}=(" + layer + ",0)");
		}
		workspace.setWorkingLayer(0);
		GeoCeDGAlgebraView view = algebraView(app);
		view.setTreeMode(SortMode.LAYER);
		view.setSize(400, 600);
		view.doLayout();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) view.getModel().getRoot();
		List<Object> order = new ArrayList<>();
		for (int i = 0; i < root.getChildCount(); i++) {
			order.add(((DefaultMutableTreeNode) root.getChildAt(i)).getUserObject());
		}
		assertEquals(List.of(2, 9, 10, 42, 99), order, "numeric order over 0..99");

		pressEye(view, 42);
		pressEye(view, 99);
		assertEquals(List.of(42, 99), workspace.getHiddenLayers());
		assertTrue(lookup(app, "P_{42}").isEuclidianVisible(), "object visibility untouched");
		pressEye(view, 99);
		assertEquals(List.of(42), workspace.getHiddenLayers());
	}

	// ---------------------------------------------------------- T-DOMAIN-ROUTES

	@Test
	void pasteKeepsACopiedLayerAboveNine() {
		AppGeoCeDG app = G9U1TestApp.create();
		app.getLayerWorkspace().setWorkingLayer(50);
		GeoElement a = eval(app, "A=(1,1)");
		app.getLayerWorkspace().setWorkingLayer(3);
		Set<GeoElement> before = new HashSet<>(app.getKernel().getConstruction()
				.getGeoSetConstructionOrder());
		InternalClipboard.duplicate(app, List.of(a));
		List<GeoElement> created = new ArrayList<>(app.getKernel().getConstruction()
				.getGeoSetConstructionOrder());
		created.removeAll(before);
		assertEquals(1, created.size());
		assertEquals(50, created.get(0).getLayer(), "AQ-L8: the copied layer, never the WL");
	}

	// ------------------------------------------------------- T-DOMAIN-CONSUMERS

	@Test
	void macroAndLocusV2OutputsTakeAWorkingLayerAboveNine() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoElement a = eval(app, "A=(1,1)");
		GeoElement b = eval(app, "B=(2,2)");
		GeoElement f = eval(app, "f=Line(A,B)");
		ToolCreationDialogModel builder = new ToolCreationDialogModel(app, () -> {
			// no dialog to update
		});
		builder.addToInput(a);
		builder.addToInput(b);
		builder.addToOutput(f);
		builder.createTool();
		builder.finish(app, "WorkLine", "WorkLine", "two points", false, null);
		app.getLayerWorkspace().setWorkingLayer(42);
		assertEquals(42, eval(app, "g=WorkLine((1,3),(2,3))").getLayer());
		app.getLayerWorkspace().setWorkingLayer(77);
		eval(app, "s=0");
		eval(app, "Q=(s,0)");
		eval(app, "D={false,{-2,2,true,true}}");
		GeoElement locus = eval(app, "L=LocusV2(Q,s,D)");
		assertInstanceOf(GeoLocusV2.class, locus);
		assertEquals(77, locus.getLayer());
		assertEquals(77, app.getMaxLayerUsed());
	}

	@Test
	void hitTestingAndTheDrawOrderFollowLayersAboveNine() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		GeoCeDGLayerWorkspace workspace = app.getLayerWorkspace();
		workspace.setWorkingLayer(50);
		GeoElement top = eval(app, "A=(2,1)");
		workspace.setWorkingLayer(10);
		GeoElement low = eval(app, "B=(2,1)");
		workspace.setWorkingLayer(0);
		GPoint at = new GPoint(view.toScreenCoordX(2), view.toScreenCoordY(1));
		assertEquals(List.of(top), hits(view, at), "50 is above 10, although created first");
		workspace.setLayerHidden(50, true);
		assertEquals(List.of(low), hits(view, at));
		workspace.setLayerHidden(50, false);
		top.remove();
		low.remove();

		// The square on 50 is created first; within one layer the later object is on top.
		workspace.setWorkingLayer(50);
		GeoElement over = fill(eval(app, "q1=Polygon((-1,-1),(3,-1),(3,3),(-1,3))"), BLUE);
		workspace.setWorkingLayer(10);
		GeoElement under = fill(eval(app, "q2=Polygon((0,0),(2,0),(2,2),(0,2))"), RED);
		workspace.setWorkingLayer(0);
		assertEquals(BLUE, pixel(app, view, 1, 1), "layer 50 is drawn above layer 10");
		over.setLayer(5);
		over.updateRepaint();
		assertEquals(RED, pixel(app, view, 1, 1), "layer 10 is drawn above layer 5");
		assertEquals(10, under.getLayer());
	}

	// ------------------------------------------------------------------ T-CLASSIC

	@Test
	void theClassicDiagnosticConfigurationClampsAndDropsTheElement() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		app.getLayerWorkspace().setWorkingLayer(50);
		eval(app, "A=(1,1)");
		app.getLayerWorkspace().setWorkingLayer(99);
		eval(app, "B=(2,1)");
		app.getLayerWorkspace().setWorkingLayer(0);
		app.getLayerWorkspace().setLayerHidden(50, true);
		Path file = temporary.resolve("domain.cedg");
		assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file.toFile()));
		String saved = documentXml(file);
		assertTrue(saved.contains("<geocedgHiddenLayers version=\"1\" layers=\"50\"/>"));

		AppDNoGui classic = newHeadless(new AppConfigDefault());
		try (InputStream input = Files.newInputStream(file)) {
			assertTrue(GFileHandler.loadXML(classic, input, false));
		}
		assertEquals(9, classic.getKernel().lookupLabel("A").getLayer(), "AQ-L1c");
		assertEquals(9, classic.getKernel().lookupLabel("B").getLayer());
		assertEquals(9, classic.getMaxLayerUsed());
		assertTrue(classic.getKernel().lookupLabel("A").isEuclidianVisible(),
				"Classic shows every layer");
		Path resaved = temporary.resolve("classic.ggb");
		AtomicDocumentFileWriter.write(resaved, target ->
				((MyXMLioJre) classic.getXMLio()).writeGeoGebraFile(target.toFile()));
		String classicXml = documentXml(resaved);
		assertFalse(classicXml.contains("geocedgHiddenLayers"), "DQ-A2-5: dropped on re-save");
		assertFalse(classicXml.contains("<layer val=\"50\"/>"));
		assertTrue(classicXml.contains("<layer val=\"9\"/>"));
	}

	// ------------------------------------------------------------------ helpers

	private static AppDNoGui newHeadless(AppConfig config) {
		Log previousLogger = Log.getLogger();
		try {
			return new AppDNoGui(new LocalizationD(3), true, config);
		} finally {
			Log.setLogger(previousLogger);
		}
	}

	static String documentXml(Path archive) throws IOException {
		try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(archive))) {
			for (ZipEntry entry = zip.getNextEntry(); entry != null;
					entry = zip.getNextEntry()) {
				if ("geogebra.xml".equals(entry.getName())) {
					return new String(zip.readAllBytes(), StandardCharsets.UTF_8);
				}
			}
		}
		throw new AssertionError("no geogebra.xml in " + archive);
	}

	private static List<GeoElement> hits(EuclidianView view, GPoint at) {
		view.updateAllDrawables(true);
		view.setHits(at, PointerEventType.MOUSE);
		return new ArrayList<>(view.getHits());
	}

	private static GeoElement fill(GeoElement polygon, GColor color) {
		polygon.setObjColor(color);
		polygon.setAlphaValue(1);
		polygon.setLineOpacity(255);
		polygon.setLabelVisible(false);
		polygon.updateRepaint();
		return polygon;
	}

	private static GColor pixel(AppGeoCeDG app, EuclidianView view, double x, double y)
			throws Exception {
		BufferedImage image = new BufferedImage(view.getWidth(), view.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		SwingUtilities.invokeAndWait(() -> {
			java.awt.Graphics2D g = image.createGraphics();
			g.setColor(java.awt.Color.WHITE);
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
			view.updateAllDrawables(true);
			view.paint(new GGraphics2DD(g));
			g.dispose();
		});
		int rgb = image.getRGB(view.toScreenCoordX(x), view.toScreenCoordY(y));
		for (GColor color : new GColor[] {RED, BLUE}) {
			if (Math.abs(((rgb >> 16) & 0xff) - color.getRed()) <= 12
					&& Math.abs(((rgb >> 8) & 0xff) - color.getGreen()) <= 12
					&& Math.abs((rgb & 0xff) - color.getBlue()) <= 12) {
				return color;
			}
		}
		throw new AssertionError("unexpected colour " + Integer.toHexString(rgb));
	}
}
