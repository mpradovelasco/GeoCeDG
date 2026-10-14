/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.kernel.ConstructionDefaults;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.App;
import org.geogebra.common.plugin.EuclidianStyleConstants;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.euclidian.EuclidianStyleBarD;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.geogebra3D.euclidianFor3D.EuclidianViewFor3DD;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.app.GeoGebraFrame;
import org.geogebra.desktop.gui.color.GeoGebraColorChooser;
import org.geogebra.desktop.gui.dialog.PropertiesPanelD;
import org.geogebra.desktop.gui.util.PopupMenuButtonD;
import org.geogebra.desktop.gui.util.SelectionTableD;
import org.geogebra.desktop.main.AppD;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import com.formdev.flatlaf.FlatClientProperties;

/**
 * PRE-G9B-R6-plus-F4-A: the numeric line-thickness field of Object Properties in the
 * line-style popup of the GeoCeDG Graphics style bar (architecture A1, no inherited
 * change). Each test covers one obligation of the canonical prompt; the real-window
 * keyboard-focus gate is evidence outside this class.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusF4AStyleBarThicknessTest {
	private static final String PROPERTIES_FIELD = "geocedg.properties.lineThickness";

	@TempDir
	Path temporaryDirectory;

	/** The real object Properties make the embedded host frame displayable. */
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

	// ----------------------------------------------------------------- helpers

	private static void onEdt(Runnable action) throws Exception {
		SwingUtilities.invokeAndWait(action);
	}

	private static <T> T onEdt(java.util.concurrent.Callable<T> action) throws Exception {
		AtomicReference<T> result = new AtomicReference<>();
		AtomicReference<Exception> failure = new AtomicReference<>();
		SwingUtilities.invokeAndWait(() -> {
			try {
				result.set(action.call());
			} catch (Exception e) {
				failure.set(e);
			}
		});
		if (failure.get() != null) {
			throw failure.get();
		}
		return result.get();
	}

	private static Object read(Object target, Class<?> owner, String name) throws Exception {
		Field field = owner.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(target);
	}

	private static EuclidianStyleBarD bar(EuclidianViewD view) throws Exception {
		return onEdt(() -> (EuclidianStyleBarD) view.getStyleBar());
	}

	private static PopupMenuButtonD lineButton(EuclidianStyleBarD bar) throws Exception {
		return (PopupMenuButtonD) read(bar, EuclidianStyleBarD.class, "btnLineStyle");
	}

	private static JPopupMenu popup(PopupMenuButtonD button) throws Exception {
		return (JPopupMenu) read(button, PopupMenuButtonD.class, "myPopup");
	}

	private static List<JSpinner> spinners(Container container) {
		List<JSpinner> found = new ArrayList<>();
		for (Component item : container.getComponents()) {
			if (item instanceof JSpinner spinner) {
				found.add(spinner);
			}
		}
		return found;
	}

	private static GeoCeDGLineThicknessField field(PopupMenuButtonD button) {
		return (GeoCeDGLineThicknessField) button.getClientProperty(
				GeoCeDGLineThicknessField.class);
	}

	/**
	 * The line-style popup holds the table and, below it, one row with the inherited
	 * slider and the field at its right; the popup is light-weight without drop shadow.
	 */
	private static GeoCeDGLineThicknessField assertOneFieldBesideTheSlider(
			EuclidianStyleBarD bar) throws Exception {
		assertTrue(bar instanceof GeoCeDGEuclidianStyleBar, bar.getClass().getName());
		PopupMenuButtonD button = lineButton(bar);
		JPopupMenu popup = popup(button);
		Component[] items = popup.getComponents();
		assertEquals(2, items.length, "table, then the thickness row");
		assertTrue(items[0] instanceof SelectionTableD);
		assertTrue(items[1] instanceof JPanel);
		JPanel row = (JPanel) items[1];
		assertEquals(GeoCeDGEuclidianStyleBar.ROW_NAME, row.getName());
		Component[] cells = row.getComponents();
		assertEquals(2, cells.length, "slider, field");
		assertSame(button.getMySlider(), cells[0], "the inherited thickness slider");
		assertTrue(cells[1] instanceof JSpinner);
		JSpinner spinner = (JSpinner) cells[1];
		assertEquals(GeoCeDGEuclidianStyleBar.FIELD_NAME, spinner.getName());
		assertTrue(spinners(popup).isEmpty(), "no field outside the row");
		assertTrue(popup.isLightWeightPopupEnabled(), "this popup only is light-weight");
		assertEquals(Boolean.FALSE, popup.getClientProperty(
				FlatClientProperties.POPUP_DROP_SHADOW_PAINTED), "without drop shadow");
		assertFalse(popup.isFocusable(), "the inherited popup focusability is kept");
		GeoCeDGLineThicknessField field = field(button);
		assertNotNull(field);
		assertSame(spinner, field.getComponent());
		assertTrue(isInBar(bar, button), "the bar shows this button");
		return field;
	}

	private static boolean isInBar(Container bar, Component button) {
		for (Component child : bar.getComponents()) {
			if (child == button) {
				return true;
			}
		}
		return false;
	}

	private static void select(AppD app, EuclidianStyleBarD bar, GeoElement... geos)
			throws Exception {
		onEdt(() -> {
			app.getSelectionManager().clearSelectedGeos(false, false);
			for (GeoElement geo : geos) {
				app.getSelectionManager().addSelectedGeo(geo, false, false);
			}
			bar.updateStyleBar();
		});
	}

	private static boolean commit(GeoCeDGLineThicknessField field, String entry)
			throws Exception {
		return onEdt(() -> field.commitText(entry));
	}

	private static UndoManagerD baseline(AppGeoCeDG app) throws Exception {
		app.setUndoActive(true);
		UndoManagerD undo = (UndoManagerD) app.getKernel().getConstruction().getUndoManager();
		try (UndoManagerD.PreparedUndoBaseline baseline = undo.prepareUndoBaseline()) {
			undo.commitUndoBaseline(baseline);
		}
		return undo;
	}

	private static AppGeoCeDG english() {
		AppGeoCeDG app = G9U1TestApp.create();
		app.setLocale(Locale.ENGLISH);
		return app;
	}

	private static EuclidianViewD graphics2(AppD app) throws Exception {
		return onEdt(() -> ((GuiManagerD) app.getGuiManager()).getEuclidianView2(1));
	}

	// ------------------------------------------------------------------- tests

	@Test
	void graphics1AndGraphics2StyleBarsCarryOneFieldBesideTheSlider() throws Exception {
		AppGeoCeDG app = english();
		GeoCeDGLineThicknessField first = assertOneFieldBesideTheSlider(
				bar(app.getEuclidianView1()));
		assertEquals(app.layerText("Properties.LineThickness.Field"),
				first.getComponent().getToolTipText(), "the Properties field text");

		EuclidianViewD second = graphics2(app);
		assertTrue(second instanceof EuclidianViewFor3DD, "the inherited Graphics 2 view");
		assertEquals(EuclidianViewFor3DD.class, second.getClass().getSuperclass(),
				"only the style-bar factory is overridden");
		assertEquals(App.VIEW_EUCLIDIAN2, second.getViewID());
		GeoCeDGLineThicknessField other = assertOneFieldBesideTheSlider(bar(second));
		assertNotSame(first, other, "one field per view");
	}

	@Test
	void theFieldIsAttachedAgainAfterLanguageAndFontChanges() throws Exception {
		AppGeoCeDG app = english();
		EuclidianStyleBarD bar = bar(app.getEuclidianView1());
		PopupMenuButtonD before = lineButton(bar);
		assertOneFieldBesideTheSlider(bar);

		onEdt(() -> app.setLanguage(Locale.forLanguageTag("es")));
		PopupMenuButtonD spanish = lineButton(bar);
		assertNotSame(before, spanish, "createButtons() made a new line-style button");
		GeoCeDGLineThicknessField field = assertOneFieldBesideTheSlider(bar);
		assertEquals(GeoCeDGProfile.getText("Properties.LineThickness.Field", "es"),
				field.getComponent().getToolTipText());

		int size = app.getSettings().getFontSettings().getAppFontSize() + 4;
		onEdt(() -> app.getSettings().getFontSettings().setAppFontSize(size));
		PopupMenuButtonD larger = lineButton(bar);
		assertNotSame(spanish, larger, "the font change rebuilt the buttons");
		field = assertOneFieldBesideTheSlider(bar);
		assertEquals(app.getPlainFont().getSize(), field.getComponent().getFont().getSize(),
				"the field uses the new font");
	}

	@Test
	void classicStyleBarsHaveNoField() throws Exception {
		AppD classic = G9U1TestApp.withoutWindowDispatcher(new AppD(new CommandLineArguments(
				new String[] {"--silent"}), new JPanel(), true));
		for (EuclidianViewD view : new EuclidianViewD[] {classic.getEuclidianView1(),
				graphics2(classic)}) {
			EuclidianStyleBarD bar = bar(view);
			assertEquals(EuclidianStyleBarD.class, bar.getClass(), "the inherited style bar");
			PopupMenuButtonD button = lineButton(bar);
			JPopupMenu popup = popup(button);
			Component[] items = popup.getComponents();
			assertEquals(2, items.length, "table and slider only");
			assertTrue(items[0] instanceof SelectionTableD);
			assertSame(button.getMySlider(), items[1]);
			assertNull(field(button));
			assertFalse(popup.isLightWeightPopupEnabled(), "the inherited heavy-weight popup");
			assertNull(popup.getClientProperty(FlatClientProperties.POPUP_DROP_SHADOW_PAINTED));
		}
		assertTrue(graphics2(classic).getClass().getName().startsWith("org.geogebra.desktop."),
				"Classic keeps the inherited Graphics 2 view");
	}

	@Test
	void theFieldFollowsTheSliderTheSelectionAndTheCreationModeDefaults() throws Exception {
		AppGeoCeDG app = english();
		EuclidianStyleBarD bar = bar(app.getEuclidianView1());
		GeoElement segment = G9U1TestApp.eval(app, "s=Segment((0,0),(4,0))");
		GeoElement other = G9U1TestApp.eval(app, "t=Segment((0,1),(4,1))");
		segment.setLineThickness(5);
		other.setLineThickness(3);
		select(app, bar, segment);
		GeoCeDGLineThicknessField field = assertOneFieldBesideTheSlider(bar);
		JSlider slider = lineButton(bar).getMySlider();
		assertEquals(1, slider.getMinimum());
		assertEquals(GeoElement.MAX_LINE_WIDTH, slider.getMaximum(), "the host range");
		assertEquals(5, field.getShownValue());

		select(app, bar, other);
		assertEquals(3, field.getShownValue(), "the selection's thickness");
		assertEquals(5, segment.getLineThickness(), "following never writes a style");
		assertEquals(3, other.getLineThickness());

		onEdt(() -> slider.setValue(9));
		assertEquals(9, other.getLineThickness(), "the slider still applies");
		assertEquals(9, field.getShownValue(), "the field follows the slider");

		GeoElement polygon = G9U1TestApp.eval(app, "q=Polygon((0,0),(1,0),(0,1))");
		select(app, bar, polygon);
		assertEquals(0, slider.getMinimum(), "a polygon allows 0");
		assertTrue(commit(field, "0"));
		assertEquals(0, polygon.getLineThickness());

		// a creation mode: the targets are the mode's construction defaults
		AppGeoCeDG twin = english();
		EuclidianStyleBarD twinBar = bar(twin.getEuclidianView1());
		onEdt(() -> {
			app.getSelectionManager().clearSelectedGeos(false, false);
			app.setMode(EuclidianConstants.MODE_SEGMENT);
			bar.setMode(EuclidianConstants.MODE_SEGMENT);
			twin.setMode(EuclidianConstants.MODE_SEGMENT);
			twinBar.setMode(EuclidianConstants.MODE_SEGMENT);
		});
		GeoElement defaults = app.getKernel().getConstruction().getConstructionDefaults()
				.getDefaultGeo(ConstructionDefaults.DEFAULT_SEGMENT);
		GeoElement twinDefaults = twin.getKernel().getConstruction().getConstructionDefaults()
				.getDefaultGeo(ConstructionDefaults.DEFAULT_SEGMENT);
		assertEquals(defaults.getLineThickness(), field.getShownValue(),
				"the default segment's thickness");
		int entry = defaults.getLineThickness() == 8 ? 6 : 8;
		assertTrue(commit(field, Integer.toString(entry)));
		JSlider twinSlider = lineButton(twinBar).getMySlider();
		onEdt(() -> twinSlider.setValue(entry));
		assertEquals(entry, defaults.getLineThickness(), "the creation default changed");
		assertEquals(twinDefaults.getLineThickness(), defaults.getLineThickness(),
				"as moving the slider");
		assertEquals(twinDefaults.getLineType(), defaults.getLineType());
	}

	@Test
	void refusedEntriesRestoreTheValueAndChangeNothing() throws Exception {
		AppGeoCeDG app = english();
		EuclidianStyleBarD bar = bar(app.getEuclidianView1());
		GeoElement segment = G9U1TestApp.eval(app, "s=Segment((0,0),(4,0))");
		segment.setLineThickness(2);
		segment.updateRepaint();
		select(app, bar, segment);
		GeoCeDGLineThicknessField field = assertOneFieldBesideTheSlider(bar);
		UndoManagerD undo = baseline(app);
		AtomicInteger stores = new AtomicInteger();
		undo.addUndoInfoStoredListener(stores::incrementAndGet);
		String xml = app.getXML();
		for (String refused : new String[] {"", "abc", "2.5", "-1", "0", "14", "7x"}) {
			assertFalse(commit(field, refused), "refused '" + refused + "'");
			assertEquals(2, segment.getLineThickness(), "style untouched by '" + refused + "'");
			assertEquals(2, lineButton(bar).getMySlider().getValue());
			assertEquals("2", onEdt(field::getShownText), "the value is shown again");
		}
		assertEquals(xml, app.getXML(), "a refused entry changes nothing");
		Thread.sleep(500);
		onEdt(() -> { });
		assertEquals(0, stores.get(), "no undo step");
	}

	@Test
	void anAcceptedEntryEqualsMovingTheSliderAndStoresOneUndoStep() throws Exception {
		AppGeoCeDG app = english();
		AppGeoCeDG twin = english();
		List<GeoElement[]> pairs = new ArrayList<>();
		for (AppGeoCeDG each : new AppGeoCeDG[] {app, twin}) {
			GeoElement s = G9U1TestApp.eval(each, "s=Segment((0,0),(4,0))");
			GeoElement t = G9U1TestApp.eval(each, "t=Segment((0,1),(4,1))");
			s.setLineThickness(3);
			// t already has the entered thickness and another line type: the slider's
			// own path would store an undo point here as well
			t.setLineThickness(5);
			t.setLineType(EuclidianStyleConstants.LINE_TYPE_DASHED_SHORT);
			s.updateRepaint();
			t.updateRepaint();
			pairs.add(new GeoElement[] {s, t});
		}
		EuclidianStyleBarD bar = bar(app.getEuclidianView1());
		EuclidianStyleBarD twinBar = bar(twin.getEuclidianView1());
		select(app, bar, pairs.get(0));
		select(twin, twinBar, pairs.get(1));
		GeoCeDGLineThicknessField field = assertOneFieldBesideTheSlider(bar);
		assertEquals(3, field.getShownValue(), "the first selected object");

		UndoManagerD undo = baseline(app);
		AtomicInteger stores = new AtomicInteger();
		CountDownLatch stored = new CountDownLatch(1);
		undo.addUndoInfoStoredListener(() -> {
			stores.incrementAndGet();
			stored.countDown();
		});
		int history = undo.getHistorySize();
		assertTrue(commit(field, "5"));
		JSlider twinSlider = lineButton(twinBar).getMySlider();
		onEdt(() -> twinSlider.setValue(5));
		assertTrue(stored.await(10, TimeUnit.SECONDS));
		Thread.sleep(1000);
		onEdt(() -> { });
		assertEquals(1, stores.get(), "exactly one undo step for the entry");
		assertEquals(history + 1, undo.getHistorySize());
		for (int i = 0; i < 2; i++) {
			GeoElement mine = pairs.get(0)[i];
			GeoElement theirs = pairs.get(1)[i];
			assertEquals(5, mine.getLineThickness(), mine.getLabelSimple());
			assertEquals(theirs.getLineThickness(), mine.getLineThickness(),
					"same targets as the slider");
			assertEquals(theirs.getLineType(), mine.getLineType(),
					"same line type as the slider (inherited coupling)");
		}
		assertEquals(EuclidianStyleConstants.LINE_TYPE_FULL, pairs.get(0)[1].getLineType(),
				"the line type selected in the popup is re-applied, as by the slider");
		// the same value again is no change and no undo step
		assertTrue(commit(field, "5"));
		Thread.sleep(500);
		onEdt(() -> { });
		assertEquals(1, stores.get());
	}

	@Test
	void undoRedoSaveAndReopenKeepTheEnteredThickness() throws Exception {
		AppGeoCeDG app = english();
		EuclidianStyleBarD bar = bar(app.getEuclidianView1());
		GeoElement segment = G9U1TestApp.eval(app, "s=Segment((0,0),(4,0))");
		segment.setLineThickness(2);
		segment.updateRepaint();
		select(app, bar, segment);
		GeoCeDGLineThicknessField field = assertOneFieldBesideTheSlider(bar);
		UndoManagerD undo = baseline(app);
		CountDownLatch stored = new CountDownLatch(1);
		undo.addUndoInfoStoredListener(stored::countDown);
		assertTrue(commit(field, "9"));
		assertTrue(stored.await(10, TimeUnit.SECONDS));
		onEdt(() -> { });
		assertEquals(9, G9U1TestApp.lookup(app, "s").getLineThickness());
		app.getKernel().undo();
		assertEquals(2, G9U1TestApp.lookup(app, "s").getLineThickness(), "undo");
		app.getKernel().redo();
		assertEquals(9, G9U1TestApp.lookup(app, "s").getLineThickness(), "redo");

		Path file = temporaryDirectory.resolve("thickness.cedg");
		assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file.toFile()));
		AppGeoCeDG reopened = english();
		assertTrue(reopened.loadFile(file.toFile(), false));
		assertEquals(9, G9U1TestApp.lookup(reopened, "s").getLineThickness(), "reopened");
	}

	@Test
	void thePropertiesFieldKeepsItsOwnBehaviour() throws Exception {
		AppGeoCeDG app = english();
		EuclidianStyleBarD bar = bar(app.getEuclidianView1());
		GeoElement segment = G9U1TestApp.eval(app, "s=Segment((0,0),(4,0))");
		segment.setLineThickness(4);
		select(app, bar, segment);
		GeoCeDGLineThicknessField barField = assertOneFieldBesideTheSlider(bar);
		AtomicReference<PropertiesPanelD> panel = new AtomicReference<>();
		onEdt(() -> {
			panel.set(new PropertiesPanelD(app, new GeoGebraColorChooser(app), false));
			panel.get().setLabels();
			panel.get().updateSelection(new Object[] {segment});
		});
		List<JSpinner> named = new ArrayList<>();
		collect(panel.get(), named);
		assertEquals(1, named.size(), "one Properties field, under its own name");
		GeoCeDGLineThicknessField properties = (GeoCeDGLineThicknessField) named.get(0)
				.getClientProperty(GeoCeDGLineThicknessField.class);
		assertNotSame(barField, properties);
		assertEquals(4, properties.getShownValue());
		assertTrue(commit(properties, "6"));
		assertEquals(6, segment.getLineThickness(), "the Properties field applies as before");
		onEdt(bar::updateStyleBar);
		assertEquals(6, barField.getShownValue(), "the style bar shows the new value");
	}

	private static void collect(Container root, List<JSpinner> named) {
		for (Component child : root.getComponents()) {
			if (child instanceof JSpinner spinner && PROPERTIES_FIELD.equals(spinner.getName())) {
				named.add(spinner);
			}
			if (child instanceof Container container) {
				collect(container, named);
			}
		}
	}

	@Test
	void theFieldSitsRightOfTheSliderOnOneRowSizedFromItsFont() throws Exception {
		AppGeoCeDG app = english();
		EuclidianStyleBarD bar = bar(app.getEuclidianView1());
		GeoCeDGLineThicknessField field = assertOneFieldBesideTheSlider(bar);
		JSpinner spinner = (JSpinner) field.getComponent();
		JSlider slider = lineButton(bar).getMySlider();
		JPanel row = (JPanel) spinner.getParent();
		onEdt(() -> {
			row.setSize(row.getPreferredSize());
			row.doLayout();
		});
		assertEquals(slider.getY() + slider.getHeight() / 2.0,
				spinner.getY() + spinner.getHeight() / 2.0, 1.0, "centred on the slider");
		assertTrue(spinner.getX() > slider.getX() + slider.getWidth(), "right of the slider");
		assertEquals(spinner.getPreferredSize(), spinner.getSize(), "not stretched");
		JFormattedTextField text = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
		assertEquals(Integer.toString(GeoElement.MAX_LINE_WIDTH).length(), text.getColumns(),
				"as many columns as the largest thickness has digits");
		assertEquals(spinner.getFontMetrics(spinner.getFont()).charWidth(' '),
				spinner.getX() - (slider.getX() + slider.getWidth()), "a gap from the font");
		int width = spinner.getPreferredSize().width;
		java.awt.Font font = spinner.getFont();
		onEdt(() -> field.setFont(font.deriveFont(font.getSize2D() * 2)));
		assertTrue(spinner.getPreferredSize().width > width,
				"the field follows its font, no fixed pixel width");
	}

	private static void closePopup(JPopupMenu popup) {
		PopupMenuEvent event = new PopupMenuEvent(popup);
		for (PopupMenuListener listener : popup.getPopupMenuListeners()) {
			if (listener.getClass().getEnclosingClass() == GeoCeDGEuclidianStyleBar.class) {
				listener.popupMenuWillBecomeInvisible(event);
			}
		}
	}

	@Test
	void closingThePopupEndsTheEditAsFocusLossDoesInProperties() throws Exception {
		AppGeoCeDG app = english();
		EuclidianStyleBarD bar = bar(app.getEuclidianView1());
		GeoElement segment = G9U1TestApp.eval(app, "s=Segment((0,0),(4,0))");
		segment.setLineThickness(3);
		segment.updateRepaint();
		select(app, bar, segment);
		GeoCeDGLineThicknessField field = assertOneFieldBesideTheSlider(bar);
		JPopupMenu popup = popup(lineButton(bar));
		JFormattedTextField text = ((JSpinner.DefaultEditor) ((JSpinner) field.getComponent())
				.getEditor()).getTextField();
		UndoManagerD undo = baseline(app);
		AtomicInteger stores = new AtomicInteger();
		CountDownLatch stored = new CountDownLatch(1);
		undo.addUndoInfoStoredListener(() -> {
			stores.incrementAndGet();
			stored.countDown();
		});
		onEdt(() -> text.setText("7"));
		assertEquals(3, segment.getLineThickness(), "typing alone applies nothing");
		onEdt(() -> closePopup(popup));
		assertEquals(7, segment.getLineThickness(), "closing applies a valid entry");
		assertTrue(stored.await(10, TimeUnit.SECONDS));
		onEdt(() -> text.setText("abc"));
		onEdt(() -> closePopup(popup));
		assertEquals(7, segment.getLineThickness(), "a refused entry changes nothing");
		assertEquals("7", onEdt(field::getShownText), "and is restored");
		onEdt(() -> closePopup(popup));
		Thread.sleep(500);
		onEdt(() -> { });
		assertEquals(1, stores.get(), "one undo step; none for a refusal or a plain close");
	}

	@Test
	void aPopupWindowIsCoveredOnlyByAnotherVisibleWindowOverIt() throws Exception {
		onEdt(() -> {
			JFrame owner = new JFrame();
			JWindow popupWindow = new JWindow(owner);
			JWindow other = new JWindow(owner);
			try {
				popupWindow.setBounds(0, 0, 40, 40);
				other.setBounds(20, 20, 40, 40);
				assertFalse(GeoCeDGEuclidianStyleBar.isCoveredByAnotherWindow(popupWindow),
						"a hidden window covers nothing");
				other.setVisible(true);
				assertTrue(GeoCeDGEuclidianStyleBar.isCoveredByAnotherWindow(popupWindow),
						"a visible overlapping window, e.g. a tooltip");
				other.setBounds(100, 100, 40, 40);
				assertFalse(GeoCeDGEuclidianStyleBar.isCoveredByAnotherWindow(popupWindow),
						"no overlap");
			} finally {
				other.dispose();
				popupWindow.dispose();
				owner.dispose();
			}
		});
	}

	@Test
	void theFieldIsOnlyEditableWhereTheKeyboardReachesIt() throws Exception {
		assertFalse(GeoCeDGEuclidianStyleBar.isKeyboardReachable(null));
		onEdt(() -> {
			JWindow heavyWeight = new JWindow();
			heavyWeight.setFocusableWindowState(false);
			JFrame application = new JFrame();
			try {
				assertFalse(GeoCeDGEuclidianStyleBar.isKeyboardReachable(heavyWeight),
						"a separate non-focusable popup window");
				assertTrue(GeoCeDGEuclidianStyleBar.isKeyboardReachable(application),
						"the application window");
			} finally {
				heavyWeight.dispose();
				application.dispose();
			}
		});
	}
}
