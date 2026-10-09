/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;

import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.desktop.export.DrawingScale;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.gui.app.GeoGebraFrame;
import org.geogebra.desktop.gui.color.GeoGebraColorChooser;
import org.geogebra.desktop.gui.dialog.PropertiesPanelD;
import org.geogebra.desktop.main.AppD;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * POST-E2-P1-R2 author-smoke follow-up B and C (refinement A is measured in
 * {@link PostE2P1R2PhysicalPdfStyleTest}).
 *
 * <p>B: the object Properties keep the host thickness slider as the only style writer;
 * a numeric field beside it follows the slider and its range, applies an accepted
 * integer through the slider as one undo point, and refuses anything else without a
 * style change. Classic has no field.
 *
 * <p>C: the status bar shows the window's session drawing scale a:b, or an explicit
 * non-physical statement without a construction unit; it follows scale changes, New and
 * Open resets, units and language, with one listener for the bar's lifetime, and never
 * changes the document, its saved state or its undo history.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PostE2P1R2SmokeFollowUpTest {
	private static final UnitState MM = UnitState.of(UnitToken.MM, null, null);

	@TempDir
	Path temporaryDirectory;

	// ------------------------------------------------------- B: thickness field

	/**
	 * The real object Properties build the Script and Text editors, whose input dialogs
	 * are owned by the host frame and make it displayable (never visible): this test
	 * disposes that frame of its own embedded hosts before the lifecycle check.
	 */
	@AfterEach
	void disposeDisplayableHostFrames() throws Exception {
		onEdt(() -> {
			for (GeoGebraFrame frame : new ArrayList<>(GeoGebraFrame.getInstances())) {
				if (frame.isDisplayable() && !frame.isVisible()) {
					frame.dispose();
				}
			}
		});
	}

	private static PropertiesPanelD properties(AppD app, GeoElement geo) throws Exception {
		AtomicReference<PropertiesPanelD> panel = new AtomicReference<>();
		SwingUtilities.invokeAndWait(() -> {
			panel.set(new PropertiesPanelD(app, new GeoGebraColorChooser(app), false));
			panel.get().setLabels();
			panel.get().updateSelection(new Object[] {geo});
		});
		return panel.get();
	}

	private static <T extends Component> List<T> find(Container root, Class<T> type) {
		List<T> found = new ArrayList<>();
		for (Component child : root.getComponents()) {
			if (type.isInstance(child)) {
				found.add(type.cast(child));
			}
			if (child instanceof Container) {
				found.addAll(find((Container) child, type));
			}
		}
		return found;
	}

	private static JSpinner field(PropertiesPanelD panel) {
		JSpinner result = null;
		for (JSpinner spinner : find(panel, JSpinner.class)) {
			if ("geocedg.properties.lineThickness".equals(spinner.getName())) {
				assertNull(result, "one thickness field");
				result = spinner;
			}
		}
		return result;
	}

	private static JSlider slider(JSpinner field) {
		List<JSlider> sliders = find(field.getParent(), JSlider.class);
		assertEquals(1, sliders.size(), "the field sits beside the thickness slider");
		return sliders.get(0);
	}

	private static GeoCeDGLineThicknessField binding(JSpinner spinner) {
		return (GeoCeDGLineThicknessField) spinner.getClientProperty(
				GeoCeDGLineThicknessField.class);
	}

	private static void onEdt(Runnable action) throws Exception {
		SwingUtilities.invokeAndWait(action);
	}

	@Test
	void theFieldSitsBesideTheSliderAndFollowsItsValueAndRange() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		app.setLocale(Locale.ENGLISH);
		GeoElement segment = G9U1TestApp.eval(app, "s=Segment((0,0),(4,0))");
		segment.setLineThickness(5);
		PropertiesPanelD panel = properties(app, segment);
		JSpinner spinner = field(panel);
		assertNotNull(spinner, "GeoCeDG adds the numeric field");
		JSlider slider = slider(spinner);
		assertEquals(1, slider.getMinimum());
		assertEquals(GeoElement.MAX_LINE_WIDTH, slider.getMaximum(), "the host range is kept");
		assertEquals(5, slider.getValue());
		assertEquals(5, spinner.getValue());
		assertEquals(app.layerText("Properties.LineThickness.Field"), spinner.getToolTipText());
		// a slider move applies the style through the slider and the field follows
		onEdt(() -> slider.setValue(9));
		assertEquals(9, segment.getLineThickness());
		assertEquals(9, spinner.getValue());
		// the next selection: the field shows its thickness without applying anything
		GeoElement other = G9U1TestApp.eval(app, "t=Segment((0,1),(4,1))");
		other.setLineThickness(3);
		onEdt(() -> panel.updateSelection(new Object[] {other}));
		assertEquals(3, spinner.getValue());
		assertEquals(9, segment.getLineThickness(), "following never writes a style");
		assertEquals(3, other.getLineThickness());
		// a polygon allows thickness 0: the field follows the slider minimum
		GeoElement polygon = G9U1TestApp.eval(app, "q=Polygon((0,0),(1,0),(0,1))");
		onEdt(() -> panel.updateSelection(new Object[] {polygon}));
		assertEquals(0, slider.getMinimum());
		assertTrue(binding(spinner).commitText("0"), "0 is in the polygon range");
		assertEquals(0, polygon.getLineThickness());
		app.setLocale(Locale.forLanguageTag("es"));
		onEdt(panel::setLabels);
		assertEquals(GeoCeDGProfile.getText("Properties.LineThickness.Field", "es"),
				spinner.getToolTipText());
	}

	@Test
	void anAcceptedEntryAppliesThroughTheSliderAsOneUndoPoint() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		app.setLocale(Locale.ENGLISH);
		GeoElement segment = G9U1TestApp.eval(app, "s=Segment((0,0),(4,0))");
		segment.setLineThickness(2);
		segment.updateRepaint();
		UndoManagerD undo = baseline(app);
		PropertiesPanelD panel = properties(app, segment);
		JSpinner spinner = field(panel);
		JSlider slider = slider(spinner);
		AtomicInteger stores = new AtomicInteger();
		CountDownLatch stored = new CountDownLatch(1);
		undo.addUndoInfoStoredListener(() -> {
			stores.incrementAndGet();
			stored.countDown();
		});
		String xml = app.getXML();
		// refused entries: not an integer, outside the range or empty; no style change
		for (String refused : new String[] {"", "abc", "2.5", "-1", "0", "14", "99999",
			"7x", " "}) {
			AtomicReference<Boolean> accepted = new AtomicReference<>();
			onEdt(() -> accepted.set(binding(spinner).commitText(refused)));
			assertFalse(accepted.get(), "refused '" + refused + "'");
			assertEquals(2, segment.getLineThickness(), "style untouched by '" + refused + "'");
			assertEquals(2, slider.getValue());
			assertEquals("2", binding(spinner).getShownText(), "the slider value is shown again");
		}
		assertEquals(xml, app.getXML(), "a refused entry changes nothing");
		AtomicReference<Boolean> accepted = new AtomicReference<>();
		onEdt(() -> accepted.set(binding(spinner).commitText(" 7 ")));
		assertTrue(accepted.get());
		assertEquals(7, segment.getLineThickness());
		assertEquals(7, slider.getValue(), "the slider applied it");
		assertTrue(stored.await(10, TimeUnit.SECONDS));
		onEdt(() -> { });
		assertEquals(1, stores.get(), "one undo point, none for the refused entries");
		// the same value again is no change and no undo point
		onEdt(() -> binding(spinner).commitText("7"));
		app.getKernel().undo();
		assertEquals(2, G9U1TestApp.lookup(app, "s").getLineThickness(), "undo");
		app.getKernel().redo();
		assertEquals(7, G9U1TestApp.lookup(app, "s").getLineThickness(), "redo");
		// a spinner step applies through the slider as well
		onEdt(() -> panel.updateSelection(new Object[] {G9U1TestApp.lookup(app, "s")}));
		onEdt(() -> spinner.setValue(spinner.getNextValue()));
		assertEquals(8, G9U1TestApp.lookup(app, "s").getLineThickness());
		assertEquals(8, slider.getValue());
		assertEquals(1, find(panel, JSpinner.class).stream()
				.filter(s -> "geocedg.properties.lineThickness".equals(s.getName())).count(),
				"no second thickness control or property");
	}

	@Test
	void classicPropertiesHaveNoField() throws Exception {
		AppD classic = G9U1TestApp.withoutWindowDispatcher(new AppD(new CommandLineArguments(
				new String[] {"--silent"}), new JPanel(), true));
		GeoElement segment = classic.getKernel().getAlgebraProcessor()
				.processAlgebraCommand("s=Segment((0,0),(4,0))", false)[0].toGeoElement();
		PropertiesPanelD panel = properties(classic, segment);
		assertNull(field(panel), "Classic keeps the slider alone");
		assertNull(GeoCeDGLineThicknessField.forProduct(classic, new JSlider(), () -> { }));
	}

	// --------------------------------------------------------- C: status bar

	private static UndoManagerD baseline(AppGeoCeDG app) throws Exception {
		app.setUndoActive(true);
		UndoManagerD undo = (UndoManagerD) app.getKernel().getConstruction().getUndoManager();
		try (UndoManagerD.PreparedUndoBaseline baseline = undo.prepareUndoBaseline()) {
			undo.commitUndoBaseline(baseline);
		}
		return undo;
	}

	private static JLabel scale(AppGeoCeDG app) {
		return app.getStatusBar().getSegment(GeoCeDGStatusBar.DRAWING_SCALE_SEGMENT);
	}

	@Test
	void theBarShowsTheSessionDrawingScaleWithoutTouchingTheDocument() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		app.setLocale(Locale.ENGLISH);
		app.getDocumentUnits().replace(MM);
		G9U1TestApp.eval(app, "A=(1,2)");
		GeoCeDGStatusBar bar = app.getStatusBar();
		List<String> names = new ArrayList<>();
		for (Component component : bar.getComponents()) {
			names.add(component.getName());
		}
		assertEquals(List.of("geocedg.status-bar.layer", "geocedg.status-bar.separator",
				"geocedg.status-bar.construction-unit", "geocedg.status-bar.separator",
				"geocedg.status-bar.presentation-unit", "geocedg.status-bar.separator",
				"geocedg.status-bar.drawing-scale", "geocedg.status-bar.separator",
				"geocedg.status-bar.paste-notice"), names, "existing segments kept, in order");
		onEdt(() -> { });
		assertEquals("Scale: 1:1", scale(app).getText());
		assertEquals(app.layerText("Units.Status.ScaleTooltip"), scale(app).getToolTipText());
		UndoManagerD undo = baseline(app);
		AtomicInteger stores = new AtomicInteger();
		undo.addUndoInfoStoredListener(stores::incrementAndGet);
		app.setSaved();
		final String xml = app.getXML();
		app.setDrawingScale(DrawingScale.of(1, 10));
		onEdt(() -> { });
		assertEquals("Scale: 1:10", scale(app).getText(), "a:b, not zoom or DPI");
		app.setDrawingScale(DrawingScale.of(2, 1));
		onEdt(() -> { });
		assertEquals("Scale: 2:1", scale(app).getText());
		assertEquals("Construction unit: mm", app.getStatusBar()
				.getSegment(GeoCeDGStatusBar.CONSTRUCTION_UNIT_SEGMENT).getText());
		assertEquals("Presentation unit: mm", app.getStatusBar()
				.getSegment(GeoCeDGStatusBar.PRESENTATION_UNIT_SEGMENT).getText());
		assertTrue(app.isSaved(), "no modified flag");
		assertEquals(xml, app.getXML(), "no serialization");
		onEdt(() -> { });
		assertEquals(0, stores.get(), "no undo point");
		app.setLocale(Locale.forLanguageTag("es"));
		assertEquals("Escala: 2:1", scale(app).getText());
		assertEquals(GeoCeDGProfile.getText("Units.Status.ScaleTooltip", "es"),
				scale(app).getToolTipText());
		// a scale change refreshes the bar on the changing thread, in order with that
		// thread's own GUI work; nothing is queued on the event thread (a reset or load
		// off the event thread must not race a deferred refresh)
		Thread worker = new Thread(() -> app.setDrawingScale(DrawingScale.of(1, 50)));
		worker.start();
		worker.join();
		assertEquals("Escala: 1:50", scale(app).getText(), "refreshed before any EDT turn");
	}

	@Test
	void withoutAConstructionUnitTheBarSaysTheScaleIsNonPhysical() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		app.setLocale(Locale.ENGLISH);
		app.getDocumentUnits().replace(UnitState.EMPTY);
		onEdt(() -> { });
		assertEquals("Scale: non-physical", scale(app).getText());
		assertEquals(app.layerText("Units.Status.ScaleTooltipNonPhysical"),
				scale(app).getToolTipText());
		app.setDrawingScale(DrawingScale.of(1, 10));
		onEdt(() -> { });
		assertEquals("Scale: non-physical", scale(app).getText(), "no physical scale shown");
		app.getDocumentUnits().replace(MM);
		onEdt(() -> { });
		assertEquals("Scale: 1:10", scale(app).getText(), "a unit makes the scale physical");
		app.setLocale(Locale.forLanguageTag("es"));
		app.getDocumentUnits().replace(UnitState.EMPTY);
		onEdt(() -> { });
		assertEquals("Escala: no física", scale(app).getText());
	}

	@Test
	void newAndOpenResetTheShownScale() throws Exception {
		AppGeoCeDG source = G9U1TestApp.create();
		source.getDocumentUnits().replace(MM);
		G9U1TestApp.eval(source, "A=(1,2)");
		Path file = temporaryDirectory.resolve("mm.cedg");
		assertTrue(((GuiManagerGeoCeDG) source.getGuiManager()).saveAsTo(file.toFile()));

		AppGeoCeDG app = G9U1TestApp.create();
		app.setLocale(Locale.ENGLISH);
		app.getDocumentUnits().replace(MM);
		app.setDrawingScale(DrawingScale.of(1, 10));
		onEdt(() -> { });
		assertEquals("Scale: 1:10", scale(app).getText());
		app.setSaved();
		app.fileNew();
		onEdt(() -> { });
		assertEquals(DrawingScale.ONE_TO_ONE, app.getDrawingScale());
		assertEquals(app.getDocumentUnits().getState().isPhysical()
				? "Scale: 1:1" : "Scale: non-physical", scale(app).getText(), "New");
		app.getDocumentUnits().replace(MM);
		app.setDrawingScale(DrawingScale.of(1, 20));
		app.setSaved();
		assertTrue(app.loadFile(file.toFile(), false));
		onEdt(() -> { });
		assertEquals("Scale: 1:1", scale(app).getText(), "Open");
	}

	@Test
	void oneListenerPerWindowAcrossPanelRebuildsAndOneRow() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		app.setLocale(Locale.ENGLISH);
		app.getDocumentUnits().replace(MM);
		GeoCeDGStatusBar bar = app.getStatusBar();
		assertSame(bar, app.getStatusBar());
		assertEquals(1, app.getDrawingScaleListenerCount(),
				"the bar is the window's only scale presentation listener");
		JPanel panel = null;
		for (int i = 0; i < 3; i++) {
			panel = app.buildApplicationPanel();
			app.updateApplicationLayout();
		}
		assertSame(bar, ((BorderLayout) panel.getLayout()).getLayoutComponent(
				BorderLayout.SOUTH), "a rebuild reattaches the same bar");
		assertEquals(1, app.getDrawingScaleListenerCount(), "no listener per rebuild");

		// a second window has its own bar and scale
		AppGeoCeDG second = G9U1TestApp.create();
		second.setLocale(Locale.ENGLISH);
		second.getDocumentUnits().replace(MM);
		second.getStatusBar();
		app.setDrawingScale(DrawingScale.of(1, 100));
		onEdt(() -> { });
		assertEquals("Scale: 1:100", scale(app).getText());
		assertEquals("Scale: 1:1", scale(second).getText(), "window-scoped");
		assertEquals(1, second.getDrawingScaleListenerCount());

		// the scale segment shares the single row; the notice stays last and on the row
		JPanel laidOut = panel;
		onEdt(() -> {
			laidOut.setSize(1000, 600);
			layoutTree(laidOut);
		});
		JLabel segment = scale(app);
		JLabel layer = bar.getSegment(GeoCeDGStatusBar.LAYER_SEGMENT);
		assertTrue(segment.isVisible());
		assertEquals(segment.getPreferredSize().width, segment.getWidth(), "shown in full");
		assertEquals(layer.getY(), segment.getY(), "on the one row");
		assertTrue(segment.getX() + segment.getWidth() <= bar.getWidth());
		bar.setNoticeTimer((delay, expiry) -> () -> { });
		onEdt(() -> {
			bar.showPasteNotice("notice");
			layoutTree(laidOut);
		});
		JLabel notice = bar.getSegment(GeoCeDGStatusBar.PASTE_NOTICE_SEGMENT);
		assertTrue(notice.isVisible());
		assertTrue(notice.getX() > segment.getX(), "the notice stays last");
		assertEquals(layer.getY(), notice.getY());
		onEdt(bar::clearPasteNotice);
		assertEquals("Scale: 1:100", segment.getText(), "the notice leaves the scale alone");
	}

	private static void layoutTree(Container container) {
		container.doLayout();
		for (Component child : container.getComponents()) {
			if (child instanceof Container) {
				layoutTree((Container) child);
			}
		}
	}
}
