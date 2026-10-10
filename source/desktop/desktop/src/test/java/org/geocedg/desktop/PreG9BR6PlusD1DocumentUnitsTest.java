/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.common.kernel.units.UsmDefinition;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.geogebra.desktop.util.LoggerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * PRE-G9B-R6-plus-D1 Desktop obligations T-UNDO, T-SAVED, T-UI, T-USM-LIFECYCLE,
 * T-STATUS, T-PREFS and T-NEW-OPEN on the real AppGeoCeDG host.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusD1DocumentUnitsTest {
	@TempDir
	Path temporaryDirectory;

	/**
	 * Unit-system v1.1: new documents whose suffix default is "shown" start EMPTY as in
	 * v1.0, for tests whose subject is not the dimension unit suffix policy.
	 */
	static AutoCloseable suffixShownDefaults() {
		MemoryStore store = new MemoryStore();
		store.values.put(GeoCeDGUnitPreferences.DIMENSION_SUFFIX_KEY,
				GeoCeDGUnitPreferences.SUFFIX_SHOWN);
		return GeoCeDGUnitPreferences.useStoreForTesting(store);
	}

	/** In-memory preference store; counts writes. */
	static final class MemoryStore implements GeoCeDGPresentationPreferences.Store {
		final Map<String, String> values = new HashMap<>();
		int saves;

		@Override
		public String load(String key) {
			return values.get(key);
		}

		@Override
		public void save(String key, String value) {
			saves++;
			values.put(key, value);
		}
	}

	/**
	 * Gives each test an empty unit-preference store, so the installed preferences of
	 * the machine are never read or written and new documents start without defaults.
	 */
	static final class EmptyUnitPreferences implements BeforeEachCallback, AfterEachCallback {
		private static final ExtensionContext.Namespace NAMESPACE =
				ExtensionContext.Namespace.create(EmptyUnitPreferences.class);

		@Override
		public void beforeEach(ExtensionContext context) {
			context.getStore(NAMESPACE).put("store",
					GeoCeDGUnitPreferences.useStoreForTesting(new MemoryStore()));
		}

		@Override
		public void afterEach(ExtensionContext context) throws Exception {
			AutoCloseable override = context.getStore(NAMESPACE).remove("store",
					AutoCloseable.class);
			if (override != null) {
				override.close();
			}
		}
	}

	/**
	 * Gives each test the Desktop logger and restores the previous one afterwards.
	 * A headless application created earlier in the same JVM may leave its logger,
	 * which rejects the logged exceptions of the host's fail-closed load paths.
	 */
	static final class DesktopLogger implements BeforeEachCallback, AfterEachCallback {
		private static final ExtensionContext.Namespace NAMESPACE =
				ExtensionContext.Namespace.create(DesktopLogger.class);

		@Override
		public void beforeEach(ExtensionContext context) {
			context.getStore(NAMESPACE).put("previous", new Log[] {Log.getLogger()});
			Log.setLogger(new LoggerD());
		}

		@Override
		public void afterEach(ExtensionContext context) {
			Log[] previous = context.getStore(NAMESPACE).remove("previous", Log[].class);
			if (previous != null) {
				Log.setLogger(previous[0]);
			}
		}
	}

	private static GeoCeDGDocumentUnits.Request request(UnitToken construction,
			UnitToken presentation, String factor, String name, String symbol) {
		return new GeoCeDGDocumentUnits.Request(construction, presentation, factor != null,
				factor, name, symbol);
	}

	private static UndoManagerD baseline(AppGeoCeDG app) throws Exception {
		app.setUndoActive(true);
		UndoManagerD undo = (UndoManagerD) app.getKernel().getConstruction().getUndoManager();
		try (UndoManagerD.PreparedUndoBaseline baseline = undo.prepareUndoBaseline()) {
			undo.commitUndoBaseline(baseline);
		}
		return undo;
	}

	private static CountDownLatch storeLatch(UndoManagerD undo) {
		CountDownLatch stored = new CountDownLatch(1);
		undo.addUndoInfoStoredListener(stored::countDown);
		return stored;
	}

	private static void flushEdt() throws Exception {
		SwingUtilities.invokeAndWait(() -> { });
	}

	private static UnitState state(AppGeoCeDG app) {
		return app.getDocumentUnits().getState();
	}

	// ------------------------------------------------------------------- T-UNDO

	@Test
	void eachUnitOperationIsExactlyOneDesktopUndoPoint() throws Exception {
		AppGeoCeDG app;
		try (AutoCloseable shown = suffixShownDefaults()) {
			app = G9U1TestApp.create();
		}
		G9U1TestApp.eval(app, "A=(1,2)");
		UndoManagerD undo = baseline(app);
		Deque<GeoCeDGDocumentUnits.Request> requests = new ArrayDeque<>();
		app.setDocumentUnitsPrompt((owner, initial) -> requests.removeFirst());
		UsmDefinition inch = UsmDefinition.of(0.0254, "inch", null);
		UsmDefinition foot = UsmDefinition.of(0.3048, "inch", null);
		UsmDefinition footNamed = UsmDefinition.of(0.3048, "foot", null);
		UsmDefinition footSymbol = UsmDefinition.of(0.3048, "foot", "ft");
		Object[][] steps = {
			{request(UnitToken.MM, null, null, null, null), UnitState.of(UnitToken.MM, null,
					null)},
			{request(UnitToken.MM, UnitToken.CM, null, null, null),
				UnitState.of(UnitToken.MM, UnitToken.CM, null)},
			{request(UnitToken.MM, UnitToken.CM, "0.0254", "inch", ""),
				UnitState.of(UnitToken.MM, UnitToken.CM, inch)},
			{request(UnitToken.MM, UnitToken.CM, "0.3048", "inch", ""),
				UnitState.of(UnitToken.MM, UnitToken.CM, foot)},
			{request(UnitToken.MM, UnitToken.CM, "0.3048", "foot", ""),
				UnitState.of(UnitToken.MM, UnitToken.CM, footNamed)},
			{request(UnitToken.MM, UnitToken.CM, "0.3048", "foot", "ft"),
				UnitState.of(UnitToken.MM, UnitToken.CM, footSymbol)},
			{request(UnitToken.MM, UnitToken.CM, "0.3048", "foot", ""),
				UnitState.of(UnitToken.MM, UnitToken.CM, footNamed)},
			{request(UnitToken.USM, UnitToken.CM, "0.3048", "foot", ""),
				UnitState.of(UnitToken.USM, UnitToken.CM, footNamed)},
			{request(UnitToken.MM, UnitToken.CM, "0.3048", "foot", ""),
				UnitState.of(UnitToken.MM, UnitToken.CM, footNamed)},
			{request(UnitToken.MM, UnitToken.CM, null, null, null),
				UnitState.of(UnitToken.MM, UnitToken.CM, null)},
			{request(UnitToken.USM, UnitToken.CM, "0.0254", "inch", ""),
				UnitState.of(UnitToken.USM, UnitToken.CM, inch)},
			{request(null, null, null, null, null), UnitState.EMPTY}
		};
		List<UnitState> history = new ArrayList<>();
		history.add(UnitState.EMPTY);
		for (Object[] step : steps) {
			int size = undo.getHistorySize();
			app.setSaved();
			CountDownLatch stored = storeLatch(undo);
			requests.add((GeoCeDGDocumentUnits.Request) step[0]);
			assertTrue(app.editDocumentUnits(), String.valueOf(step[1]));
			assertTrue(stored.await(5, TimeUnit.SECONDS), "one undo point: " + step[1]);
			assertEquals(size + 1, undo.getHistorySize(), "define and select is one too");
			assertEquals(step[1], state(app));
			assertFalse(app.isSaved(), "the operation marks the document modified");
			history.add((UnitState) step[1]);
		}
		CountDownLatch noOp = storeLatch(undo);
		requests.add(request(null, null, null, null, null));
		assertFalse(app.editDocumentUnits(), "a no-op is no operation");
		app.setDocumentUnitsPrompt((owner, initial) -> null);
		assertFalse(app.editDocumentUnits(), "Cancel is no operation");
		assertFalse(noOp.await(2, TimeUnit.SECONDS), "and neither stores an undo point");
		app.setDocumentUnitsPrompt((owner, initial) -> requests.removeFirst());
		for (int i = history.size() - 2; i >= 0; i--) {
			app.getKernel().undo();
			assertEquals(history.get(i), state(app), "undo to " + i);
		}
		for (int i = 1; i < history.size(); i++) {
			app.getKernel().redo();
			assertEquals(history.get(i), state(app), "redo to " + i);
		}
	}

	@Test
	void rejectedEditsWarnAndChangeNothing() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		app.getDocumentUnits().replace(UnitState.of(UnitToken.USM, null,
				UsmDefinition.of(0.0254, null, "in")));
		UndoManagerD undo = baseline(app);
		Deque<GeoCeDGDocumentUnits.Request> requests = new ArrayDeque<>();
		app.setDocumentUnitsPrompt((owner, initial) -> requests.removeFirst());
		List<String> messages = new ArrayList<>();
		try (MockedStatic<JOptionPane> pane = Mockito.mockStatic(JOptionPane.class)) {
			pane.when(() -> JOptionPane.showMessageDialog(any(), any(), any(), anyInt()))
					.thenAnswer(invocation -> {
						messages.add(String.valueOf(invocation.getArgument(1, Object.class)));
						return null;
					});
			final UnitState before = state(app);
			final String xml = app.getXML();
			CountDownLatch stored = storeLatch(undo);
			requests.add(request(UnitToken.USM, null, "0,0254", null, null));
			requests.add(request(UnitToken.USM, null, "abc", null, null));
			requests.add(request(UnitToken.USM, null, "0.0254", " in", null));
			requests.add(request(UnitToken.USM, null, null, null, null));
			for (int i = 0; i < 4; i++) {
				assertFalse(app.editDocumentUnits());
			}
			assertFalse(stored.await(2, TimeUnit.SECONDS));
			assertEquals(before, state(app));
			assertEquals(xml, app.getXML());
			assertEquals(List.of(app.layerText("Units.Error.FactorComma"),
					app.layerText("Units.Error.Factor"), app.layerText("Units.Error.Label"),
					app.layerText("Units.Error.UsmInUse")), messages);
		}
	}

	@Test
	void validationCoversEveryRefusal() {
		UnitState empty = UnitState.EMPTY;
		assertEquals("Units.Error.UsmUndefined", GeoCeDGDocumentUnits.validate(
				request(UnitToken.USM, null, null, null, null), empty).errorKey);
		assertEquals("Units.Error.PresentationWithoutConstruction",
				GeoCeDGDocumentUnits.validate(request(null, UnitToken.MM, null, null, null),
						empty).errorKey);
		assertEquals("Units.Error.Factor", GeoCeDGDocumentUnits.validate(
				request(UnitToken.MM, null, "1e400", null, null), empty).errorKey);
		assertEquals("Units.Error.Factor", GeoCeDGDocumentUnits.validate(
				request(UnitToken.MM, null, "1 000", null, null), empty).errorKey,
				"no grouping separators");
		assertEquals("Units.Error.Label", GeoCeDGDocumentUnits.validate(
				request(UnitToken.MM, null, "1", "a".repeat(65), null), empty).errorKey);
		GeoCeDGDocumentUnits.Result valid = GeoCeDGDocumentUnits.validate(
				request(UnitToken.USM, UnitToken.MM, "2.54E-2", "", "in"), empty);
		assertTrue(valid.isValid());
		assertEquals(UnitState.of(UnitToken.USM, UnitToken.MM,
				UsmDefinition.of(0.0254, null, "in")), valid.state);
		GeoCeDGDocumentUnits.Request round = GeoCeDGDocumentUnits.fromState(valid.state);
		assertEquals("0.0254", round.factor);
		assertEquals("", round.name);
		assertEquals("in", round.symbol);
	}

	@Test
	void theDefaultDialogReturnsTheFieldsOrNull() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoCeDGDocumentUnits.Request initial = GeoCeDGDocumentUnits.fromState(
				UnitState.of(UnitToken.CM, UnitToken.MM, UsmDefinition.of(0.0254, "in", null)));
		try (MockedStatic<JOptionPane> pane = Mockito.mockStatic(JOptionPane.class)) {
			pane.when(() -> JOptionPane.showConfirmDialog(any(), any(), any(), anyInt(),
					anyInt())).thenReturn(JOptionPane.OK_OPTION);
			GeoCeDGDocumentUnits.Request answer = GeoCeDGDocumentUnitsPrompt.dialog()
					.ask(app, initial);
			assertSame(UnitToken.CM, answer.construction);
			assertSame(UnitToken.MM, answer.presentation);
			assertTrue(answer.usmDefined);
			assertEquals("0.0254", answer.factor);
			assertEquals("in", answer.name);
			pane.when(() -> JOptionPane.showConfirmDialog(any(), any(), any(), anyInt(),
					anyInt())).thenReturn(JOptionPane.CANCEL_OPTION);
			assertNull(GeoCeDGDocumentUnitsPrompt.dialog().ask(app, initial));
		}
	}

	// ------------------------------------------------------------------ T-SAVED

	@Test
	void aUnitChangeMakesAnEmptyDocumentUnsavedAndAnUntouchedOneStaysSaved() {
		AppGeoCeDG app = G9U1TestApp.create();
		app.setSaved();
		assertTrue(app.isSaved());
		app.setUnsaved();
		assertTrue(app.isSaved(), "upstream: changes of an empty document are ignored");
		app.setSaved();
		app.setDocumentUnitsPrompt((owner, initial) -> request(UnitToken.MM, null, null,
				null, null));
		assertTrue(app.editDocumentUnits());
		assertFalse(app.isSaved(), "a unit change is save-relevant: New and close prompt");
		assertFalse(app.getKernel().getConstruction().isStarted());
	}

	// ----------------------------------------------------------------- T-STATUS

	@Test
	void statusSegmentsPresentTheEffectiveUnitsAndOpenTheDialog() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		app.setLocale(Locale.ENGLISH);
		GeoCeDGStatusBar bar = app.getStatusBar();
		List<String> names = new ArrayList<>();
		for (Component component : bar.getComponents()) {
			names.add(component.getName());
		}
		assertEquals(List.of("geocedg.status-bar.layer", "geocedg.status-bar.separator",
				"geocedg.status-bar.construction-unit", "geocedg.status-bar.separator",
				"geocedg.status-bar.presentation-unit", "geocedg.status-bar.separator",
				// POST-E2-P1-R2 smoke follow-up C adds the drawing-scale segment
				"geocedg.status-bar.drawing-scale", "geocedg.status-bar.separator",
				// PRE-G9B-R6-plus-E3 adds the hidden sheet-coherence segment
				"geocedg.status-bar.sheet-coherence", "geocedg.status-bar.separator",
				"geocedg.status-bar.paste-notice"), names);
		JLabel construction = bar.getSegment(GeoCeDGStatusBar.CONSTRUCTION_UNIT_SEGMENT);
		JLabel presentation = bar.getSegment(GeoCeDGStatusBar.PRESENTATION_UNIT_SEGMENT);
		assertEquals("Layer: 0", bar.getSegment(GeoCeDGStatusBar.LAYER_SEGMENT).getText());
		assertEquals("Construction unit: unspecified", construction.getText());
		assertEquals("Presentation unit: none", presentation.getText());
		app.getDocumentUnits().replace(UnitState.of(UnitToken.MM, null, null));
		flushEdt();
		assertEquals("Construction unit: mm", construction.getText());
		assertEquals("Presentation unit: mm", presentation.getText());
		assertEquals(app.layerText("Units.Status.PresentationTooltipFollows"),
				presentation.getToolTipText());
		app.getDocumentUnits().replace(UnitState.of(UnitToken.USM, UnitToken.CM,
				UsmDefinition.of(0.0254, "inch", "in")));
		flushEdt();
		assertEquals("Construction unit: in", construction.getText());
		assertEquals("Presentation unit: cm", presentation.getText());
		assertEquals(app.layerText("Units.Status.ConstructionTooltip")
				+ " Custom unit inch: 1 usm = 0.0254 m.", construction.getToolTipText());
		assertTrue(presentation.getToolTipText().startsWith(
				app.layerText("Units.Status.PresentationTooltipExplicit")));
		app.setLocale(Locale.forLanguageTag("es"));
		assertEquals("Unidad de construcción: in", construction.getText());
		assertEquals("Capa: 0", bar.getSegment(GeoCeDGStatusBar.LAYER_SEGMENT).getText());
		app.getDocumentUnits().replace(UnitState.EMPTY);
		flushEdt();
		assertEquals("Unidad de construcción: sin especificar", construction.getText());
		assertEquals("Unidad de presentación: ninguna", presentation.getText());
		AtomicInteger dialogs = new AtomicInteger();
		app.setDocumentUnitsPrompt((owner, initial) -> {
			dialogs.incrementAndGet();
			return null;
		});
		for (JLabel label : new JLabel[] {construction, presentation}) {
			label.dispatchEvent(new MouseEvent(label, MouseEvent.MOUSE_CLICKED,
					System.currentTimeMillis(), 0, 2, 2, 1, false, MouseEvent.BUTTON1));
		}
		assertEquals(2, dialogs.get(), "both segments open the same dialog action");
	}

	@Test
	void statusFollowsUndoAndRedo() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		app.setLocale(Locale.ENGLISH);
		G9U1TestApp.eval(app, "A=(1,1)");
		UndoManagerD undo = baseline(app);
		app.setDocumentUnitsPrompt((owner, initial) -> request(UnitToken.M, null, null,
				null, null));
		CountDownLatch stored = storeLatch(undo);
		assertTrue(app.editDocumentUnits());
		assertTrue(stored.await(5, TimeUnit.SECONDS));
		flushEdt();
		JLabel construction = app.getStatusBar().getSegment(
				GeoCeDGStatusBar.CONSTRUCTION_UNIT_SEGMENT);
		assertEquals("Construction unit: m", construction.getText());
		app.getKernel().undo();
		flushEdt();
		assertEquals("Construction unit: unspecified", construction.getText());
		app.getKernel().redo();
		flushEdt();
		assertEquals("Construction unit: m", construction.getText());
		JPanel rebuilt = app.buildApplicationPanel();
		app.updateApplicationLayout();
		assertSame(app.getStatusBar(), ((BorderLayout) rebuilt.getLayout())
				.getLayoutComponent(BorderLayout.SOUTH), "a layout rebuild keeps the bar");
		assertSame(construction, app.getStatusBar().getSegment(
				GeoCeDGStatusBar.CONSTRUCTION_UNIT_SEGMENT));
		app.getDocumentUnits().replace(UnitState.of(UnitToken.CM, null, null));
		flushEdt();
		assertEquals("Construction unit: cm", construction.getText(),
				"and it still refreshes after the rebuild");
	}

	@Test
	void pasteNoticeLifecycleNeedsNoRealSleep() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoCeDGStatusBar bar = app.getStatusBar();
		List<Runnable> expiries = new ArrayList<>();
		AtomicInteger cancels = new AtomicInteger();
		bar.setNoticeTimer((delay, expiry) -> {
			assertEquals(GeoCeDGStatusBar.PASTE_NOTICE_MILLIS, delay);
			expiries.add(expiry);
			return cancels::incrementAndGet;
		});
		JLabel notice = bar.getSegment(GeoCeDGStatusBar.PASTE_NOTICE_SEGMENT);
		assertFalse(notice.isVisible());
		bar.showPasteNotice("first");
		assertTrue(notice.isVisible());
		assertEquals("first", notice.getText());
		bar.showPasteNotice("second");
		assertEquals("second", notice.getText());
		assertEquals(1, cancels.get(), "the next notice replaces the first");
		expiries.get(0).run();
		assertEquals("second", notice.getText(), "a stale expiry is ignored");
		expiries.get(1).run();
		assertFalse(notice.isVisible());
		assertEquals("", notice.getText());
		bar.showPasteNotice("third");
		app.setSaved();
		assertTrue(app.clearConstruction());
		assertFalse(notice.isVisible(), "a document transition clears it");
	}

	// ------------------------------------------------------------------ T-PREFS

	@Test
	void preferencesUseTheirKeysAndNeverRewriteOnRead() {
		MemoryStore store = new MemoryStore();
		GeoCeDGUnitPreferences preferences = new GeoCeDGUnitPreferences(store);
		assertSame(GeoCeDGUnitPreferences.ConstructionDefault.UNSPECIFIED,
				preferences.construction());
		assertSame(GeoCeDGUnitPreferences.PresentationDefault.NONE, preferences.presentation());
		assertEquals(UnitState.of(null, null, null, false), preferences.newDocumentState(),
				"unit-system v1.1: new documents hide the dimension unit suffix");
		for (String invalid : new String[] {"usm", "km", "", "MM", " mm"}) {
			store.values.put(GeoCeDGUnitPreferences.CONSTRUCTION_KEY, invalid);
			store.values.put(GeoCeDGUnitPreferences.PRESENTATION_KEY, invalid);
			assertSame(GeoCeDGUnitPreferences.ConstructionDefault.UNSPECIFIED,
					preferences.construction(), invalid);
			assertSame(GeoCeDGUnitPreferences.PresentationDefault.NONE,
					preferences.presentation(), invalid);
		}
		assertEquals(0, store.saves, "reading never writes");
		store.values.put(GeoCeDGUnitPreferences.PRESENTATION_KEY, "cm");
		store.values.put(GeoCeDGUnitPreferences.CONSTRUCTION_KEY, "unspecified");
		assertFalse(preferences.newDocumentState().hasUnitMetadata(),
				"a presentation default is inert without a physical construction default");
		store.values.put(GeoCeDGUnitPreferences.CONSTRUCTION_KEY, "mm");
		assertEquals(UnitState.of(UnitToken.MM, UnitToken.CM, null, false),
				preferences.newDocumentState());
		preferences.setConstruction(GeoCeDGUnitPreferences.ConstructionDefault.M);
		preferences.setPresentation(GeoCeDGUnitPreferences.PresentationDefault.NONE);
		assertEquals("m", store.values.get("geocedg.units.new-document-construction.v1"));
		assertEquals("none", store.values.get("geocedg.units.new-document-presentation.v1"));
	}

	@Test
	void thePreferencePanelNeverTouchesTheCurrentDocument() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		G9U1TestApp.eval(app, "A=(1,2)");
		app.getDocumentUnits().replace(UnitState.of(UnitToken.CM, null, null));
		UndoManagerD undo = baseline(app);
		app.setSaved();
		MemoryStore store = new MemoryStore();
		GeoCeDGNewDocumentUnitsPanel panel = new GeoCeDGNewDocumentUnitsPanel(app,
				new GeoCeDGUnitPreferences(store));
		String xml = app.getXML();
		int history = undo.getHistorySize();
		CountDownLatch stored = storeLatch(undo);
		panel.constructionControl().setSelectedIndex(3);
		panel.presentationControl().setSelectedIndex(1);
		assertEquals("m", store.values.get(GeoCeDGUnitPreferences.CONSTRUCTION_KEY));
		assertEquals("mm", store.values.get(GeoCeDGUnitPreferences.PRESENTATION_KEY));
		assertFalse(stored.await(2, TimeUnit.SECONDS));
		assertEquals(history, undo.getHistorySize());
		assertEquals(xml, app.getXML());
		assertEquals(UnitState.of(UnitToken.CM, null, null), state(app));
		assertTrue(app.isSaved());
		assertTrue(app.newProductNewDocumentUnitsPanel() instanceof GeoCeDGNewDocumentUnitsPanel);
	}

	// --------------------------------------------------------------- T-NEW-OPEN

	@Test
	void defaultsApplyToNewBlankDocumentsOnlyAndBelongToTheBaseline() throws Exception {
		MemoryStore store = new MemoryStore();
		store.values.put(GeoCeDGUnitPreferences.CONSTRUCTION_KEY, "mm");
		store.values.put(GeoCeDGUnitPreferences.PRESENTATION_KEY, "cm");
		UnitState defaults = UnitState.of(UnitToken.MM, UnitToken.CM, null, false);
		try (AutoCloseable override = GeoCeDGUnitPreferences.useStoreForTesting(store)) {
			AppGeoCeDG app = G9U1TestApp.create();
			assertEquals(defaults, state(app), "startup without a file");
			assertTrue(app.isSaved(), "an untouched new document stays saved");
			UndoManagerD undo = (UndoManagerD) app.getKernel().getConstruction()
					.getUndoManager();
			app.setUndoActive(true);
			app.applyNewDocumentUnitDefaults();
			app.setDocumentUnitsPrompt((owner, initial) -> request(UnitToken.M, null, null,
					null, null));
			CountDownLatch stored = storeLatch(undo);
			assertTrue(app.editDocumentUnits());
			assertTrue(stored.await(5, TimeUnit.SECONDS));
			app.getKernel().undo();
			assertEquals(defaults, state(app), "the baseline includes the defaults");

			app.setSaved();
			app.fileNew();
			assertEquals(defaults, state(app), "File > New");
			assertTrue(app.isSaved());
			app.getDocumentUnits().replace(UnitState.of(UnitToken.M, null, null));
			app.setSaved();
			app.reset();
			assertEquals(defaults, state(app), "reset without a current file");

			Path physical = temporaryDirectory.resolve("physical.cedg");
			app.getDocumentUnits().replace(UnitState.of(UnitToken.M, null, null));
			G9U1TestApp.eval(app, "B=(3,4)");
			assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(physical.toFile()));
			Path legacy = temporaryDirectory.resolve("legacy.cedg");
			app.getDocumentUnits().replace(UnitState.EMPTY);
			assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(legacy.toFile()));
			app.setSaved();
			assertTrue(app.loadFile(physical.toFile(), false));
			assertEquals(UnitState.of(UnitToken.M, null, null), state(app),
					"Open keeps the document's own units");
			app.getDocumentUnits().replace(UnitState.of(UnitToken.CM, null, null));
			app.setSaved();
			app.reset();
			assertEquals(UnitState.of(UnitToken.M, null, null), state(app),
					"reset with a current file reloads the document's own units");
			assertTrue(app.loadFile(legacy.toFile(), false));
			assertTrue(state(app).isEmpty(), "Open never applies defaults");
			assertTrue(Files.exists(physical));
			app.getGgbApi().setXML(app.getXML());
			assertTrue(state(app).isEmpty(), "setXML never applies defaults");
			app.getGgbApi().evalXML("<element type=\"point\" label=\"Z\">"
					+ "<coords x=\"1\" y=\"1\" z=\"1\"/></element>");
			assertTrue(state(app).isEmpty(), "evalXML never applies defaults");

			AppGeoCeDG helper = G9U1TestApp.withoutWindowDispatcher(
					(AppGeoCeDG) app.newAppForTemplateOrInsertFile());
			assertTrue(state(helper).isEmpty(), "a hidden helper gets no defaults");
			store.values.put(GeoCeDGUnitPreferences.CONSTRUCTION_KEY, "cm");
			assertTrue(state(app).isEmpty(), "a preference change changes no document");
		}
		MemoryStore inert = new MemoryStore();
		inert.values.put(GeoCeDGUnitPreferences.PRESENTATION_KEY, "cm");
		try (AutoCloseable override = GeoCeDGUnitPreferences.useStoreForTesting(inert)) {
			AppGeoCeDG app = G9U1TestApp.create();
			assertFalse(state(app).hasUnitMetadata(), "unspecified default: no unit metadata");
			assertFalse(state(app).isDimensionUnitSuffixShown(),
					"unit-system v1.1: the suffix policy of new documents still applies");
		}
	}
}
