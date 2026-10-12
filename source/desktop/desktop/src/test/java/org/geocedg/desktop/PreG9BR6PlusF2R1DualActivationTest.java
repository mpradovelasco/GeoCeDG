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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.geocedg.desktop.GeoCeDGLineReferenceMemory.Slot;
import org.geocedg.desktop.GeoCeDGToolActivation.Behaviour;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.euclidian.event.AbstractEvent;
import org.geogebra.common.kernel.ModeSetter;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoLinePointLine;
import org.geogebra.common.kernel.algos.AlgoOrthoLinePointLine;
import org.geogebra.common.kernel.algos.AlgoPointOnPath;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.App;
import org.geogebra.common.main.error.ErrorHelper;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.euclidian.event.MouseEventD;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.toolbar.ModeToggleMenuD;
import org.geogebra.desktop.gui.toolbar.ToolbarContainer;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.geogebra.desktop.plugin.GgbAPID;
import org.geogebra.desktop.plugin.ScriptManagerD;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PRE-G9B-R6-plus-F2-R1: dual NORMAL / MEMORY_ENABLED activation of Parallel and
 * Perpendicular (geocedg/specs/ui/authoring-memory.md section 11; decision record v1.3
 * section 7, Option O1). The real application panel is shown in a window; toolbar
 * activations are mouse events posted to the system event queue on the real native
 * button or flyout item, so Swing fills the action-event modifiers; scripting runs
 * through the real JavaScript engine and the original GgbAPID; gestures drive the real
 * Graphics View 1 controller.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusF2R1DualActivationTest {

	private static final int PAR = EuclidianConstants.MODE_PARALLEL;
	private static final int PERP = EuclidianConstants.MODE_ORTHOGONAL;
	private static final double[] ON_L = {0, -4};
	private static final double[] ON_M = {0, 4.5};
	private static final double[] P = {-2, 1};
	private static final double[] Q = {2, 0};
	private static final double[] R = {4, 3};
	private static final double[] S = {-1, 3};
	private static final double[] EMPTY = {-3, -1};

	/** One shown GeoCeDG window with the real toolbar, status bar and Graphics View 1. */
	private static final class Window implements AutoCloseable {
		final AppGeoCeDG app;
		final JFrame frame;
		private double[] lastClick;

		private Window(AppGeoCeDG app, JFrame frame) {
			this.app = app;
			this.frame = frame;
		}

		/** The live native Parallel/Perpendicular group (rebuilt by language changes). */
		ModeToggleMenuD menu() {
			ToolbarContainer container = ((GuiManagerD) app.getGuiManager()).getToolbarPanel();
			for (Component c : container.getToolbar(-1).getComponents()) {
				if (c instanceof JComponent group && group.getClientProperty(
						"geocedg.toolbar.action.ids") instanceof List<?> ids
						&& ids.contains("construction.parallel-line")) {
					return (ModeToggleMenuD) group;
				}
			}
			throw new IllegalStateException("no native Parallel/Perpendicular group");
		}

		JToggleButton button() {
			return GeoCeDGToolbarContainer.toolbarButton(menu());
		}

		static Window open() throws Exception {
			Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
					"toolbar activations need shown components");
			assertTrue(Toolkit.getDefaultToolkit().isModalExclusionTypeSupported(
					Dialog.ModalExclusionType.APPLICATION_EXCLUDE));
			AppGeoCeDG app = G9U1TestApp.create();
			AtomicReference<Window> window = new AtomicReference<>();
			SwingUtilities.invokeAndWait(() -> {
				JPanel panel = app.buildApplicationPanel();
				JFrame frame = new JFrame("PRE-G9B-R6-plus-F2-R1");
				// earlier classes of the full Desktop suite can leave application-modal
				// error dialogs showing; they would discard the posted toolbar events of
				// this window (AWT modality), so the window is excluded from them
				frame.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
				frame.add(panel);
				frame.setSize(1280, 960);
				frame.setLocation(20, 20);
				frame.setVisible(true);
				frame.validate();
				window.set(new Window(app, frame));
			});
			Window w = window.get();
			assertNotNull(w.menu(), "the native Parallel/Perpendicular group");
			w.settle();
			EuclidianView view = app.getEuclidianView1();
			SwingUtilities.invokeAndWait(() -> view.setCoordSystem(450, 330, 30, 30));
			assertTrue(view.getWidth() >= 700 && view.getHeight() >= 520,
					"Graphics View 1 shows the scene: " + view.getWidth() + "x"
							+ view.getHeight());
			// the shown view paints on the event thread: build the scene there too
			SwingUtilities.invokeAndWait(() -> {
				view.setShowAxes(false, false);
				view.showGrid(false);
				for (String input : new String[] {"A=(-6,-4)", "B=(6,-4)", "l=Line(A,B)",
						"C=(-6,4)", "D=(6,5)", "m=Line(C,D)", "P=(-2,1)", "Q=(2,0)",
						"R=(4,3)", "S=(-1,3)"}) {
					w.eval(input);
				}
				app.storeUndoInfo();
			});
			w.settle();
			return w;
		}

		@Override
		public void close() throws Exception {
			SwingUtilities.invokeAndWait(() -> {
				menu().setPopupVisible(false);
				frame.dispose();
			});
		}

		void settle() throws Exception {
			Thread.sleep(150);
			SwingUtilities.invokeAndWait(() -> { });
			SwingUtilities.invokeAndWait(() -> { });
		}

		GeoElement eval(String input) {
			return app.getKernel().getAlgebraProcessor()
					.processAlgebraCommandNoExceptionHandling(input, false,
							ErrorHelper.silent(), false, null)[0].toGeoElement();
		}

		GeoElement lookup(String label) {
			return app.getKernel().lookupLabel(label);
		}

		GeoCeDGEuclidianController controller() {
			return (GeoCeDGEuclidianController) app.getEuclidianView1()
					.getEuclidianController();
		}

		GeoCeDGLineReferenceMemory memory() {
			return controller().getReferenceMemory();
		}

		Behaviour behaviour(Slot slot) {
			return app.getToolActivation().behaviour(slot);
		}

		int size() {
			return app.getKernel().getConstruction().getGeoSetConstructionOrder().size();
		}

		List<GeoElement> objects() {
			return new ArrayList<>(app.getKernel().getConstruction()
					.getGeoSetConstructionOrder());
		}

		int selection() {
			return app.getSelectionManager().getSelectedGeos().size();
		}

		UndoManagerD undo() {
			return (UndoManagerD) app.getKernel().getConstruction().getUndoManager();
		}

		JLabel status() {
			return app.getStatusBar().getSegment(GeoCeDGStatusBar.TOOL_MEMORY_SEGMENT);
		}

		void click(double[] at) throws Exception {
			click(at, 0);
		}

		void click(double[] at, int modifiers) throws Exception {
			if (at == lastClick) {
				// never an inherited double click (Redefine dialog) on the same object
				Thread.sleep(EuclidianConstants.DOUBLE_CLICK_DELAY + 300L);
			}
			lastClick = at;
			SwingUtilities.invokeAndWait(() -> {
				EuclidianView v = app.getEuclidianView1();
				AbstractEvent e = MouseEventD.wrapEvent(new MouseEvent(
						((EuclidianViewD) v).getJPanel(), MouseEvent.MOUSE_PRESSED, 1,
						modifiers, v.toScreenCoordX(at[0]), v.toScreenCoordY(at[1]), 1, false,
						MouseEvent.BUTTON1));
				controller().wrapMousePressed(e);
				controller().wrapMouseReleased(e);
			});
		}

		/** Real native-toolbar activation by posted press/release (Ctrl as modifier). */
		void toolbar(int mode, boolean ctrl) throws Exception {
			Component target;
			ModeToggleMenuD menu = menu();
			JToggleButton button = button();
			if (button.getActionCommand().equals(Integer.toString(mode))) {
				target = button;
			} else {
				SwingUtilities.invokeAndWait(() -> menu.setPopupVisible(true));
				settle();
				target = item(menu, mode);
				assertTrue(target.isShowing(), "flyout item shown");
			}
			AtomicInteger fired = new AtomicInteger();
			ActionListener counter = e -> fired.incrementAndGet();
			List<String> seen = new ArrayList<>();
			java.awt.event.AWTEventListener spy = e -> seen.add(e.getID() + "@"
					+ e.getSource().getClass().getSimpleName() + (((InputEvent) e).isConsumed()
							? "(consumed)" : ""));
			String before = diagnose(target);
			SwingUtilities.invokeAndWait(() -> button.addActionListener(counter));
			Toolkit.getDefaultToolkit().addAWTEventListener(spy,
					java.awt.AWTEvent.MOUSE_EVENT_MASK);
			try {
				post(target, ctrl ? InputEvent.CTRL_DOWN_MASK : 0);
			} finally {
				Toolkit.getDefaultToolkit().removeAWTEventListener(spy);
				SwingUtilities.invokeAndWait(() -> button.removeActionListener(counter));
			}
			SwingUtilities.invokeAndWait(() -> menu.setPopupVisible(false));
			settle();
			assertEquals(1, fired.get(), "the native button fired one activation; " + before
					+ " seen=" + seen);
			assertEquals(mode, app.getMode(), "toolbar activation reached the mode");
		}

		String diagnose(Component target) throws Exception {
			StringBuilder sb = new StringBuilder();
			SwingUtilities.invokeAndWait(() -> {
				sb.append("menuPath=").append(javax.swing.MenuSelectionManager.defaultManager()
						.getSelectedPath().length);
				sb.append(" target=").append(target.getClass().getSimpleName())
						.append(target.getBounds()).append(" showing=")
						.append(target.isShowing()).append(" enabled=").append(target.isEnabled());
				sb.append(" active=").append(java.awt.KeyboardFocusManager
						.getCurrentKeyboardFocusManager().getActiveWindow());
				for (java.awt.Window win : java.awt.Window.getWindows()) {
					if (win.isShowing()) {
						sb.append(" | window ").append(win.getClass().getSimpleName())
								.append(win instanceof Dialog d && d.isModal()
										? "(modal)" : "");
					}
				}
				if (target instanceof JToggleButton t) {
					sb.append(" model armed=").append(t.getModel().isArmed()).append(" pressed=")
							.append(t.getModel().isPressed()).append(" icon=")
							.append(t.getIcon() == null ? -1 : t.getIcon().getIconHeight());
				}
			});
			return sb.toString();
		}

		void post(Component target, int modifiers) throws Exception {
			int x = target.getWidth() / 2;
			int y = target.getHeight() / 2;
			if (target instanceof JToggleButton toggle && toggle.getIcon() != null) {
				// below iconHeight - 4 the inherited button opens its flyout instead
				// (ToolToggleButton.popupTriangleClicked); press inside the icon band
				y = Math.min(y, toggle.getIcon().getIconHeight() / 2);
			}
			Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(target,
					MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
					modifiers | InputEvent.BUTTON1_DOWN_MASK, x, y, 1, false,
					MouseEvent.BUTTON1));
			Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(target,
					MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), modifiers, x, y, 1,
					false, MouseEvent.BUTTON1));
			settle();
		}

		static JMenuItem item(ModeToggleMenuD menu, int mode) throws Exception {
			Field field = ModeToggleMenuD.class.getDeclaredField("menuItemList");
			field.setAccessible(true);
			for (Object o : (List<?>) field.get(menu)) {
				JMenuItem it = (JMenuItem) o;
				if (it.getActionCommand().equals(Integer.toString(mode))) {
					return it;
				}
			}
			throw new IllegalStateException("no flyout item " + mode);
		}

		/** Runs JavaScript through the real engine (ggbApplet = the original GgbAPID). */
		void js(String script) throws Exception {
			SwingUtilities.invokeAndWait(() -> ((ScriptManagerD) app.getScriptManager())
					.evalJavaScript(app, script, null));
			settle();
		}

		GeoElement created() {
			List<GeoElement> created = controller().getJustCreatedGeos();
			assertEquals(1, created.size(), "one created object");
			return created.get(0);
		}

		void rememberParallelL() throws Exception {
			toolbar(PAR, true);
			click(ON_L);
			click(P);
			assertSame(lookup("l"), memory().peek(Slot.PARALLEL));
		}

		void awaitUndo() throws Exception {
			Thread.sleep(400);
			settle();
		}
	}

	private static void assertConstruction(GeoElement line, Class<?> algorithm,
			GeoElement point, GeoElement reference) {
		AlgoElement algo = line.getParentAlgorithm();
		assertNotNull(algo);
		assertSame(algorithm, algo.getClass());
		assertSame(point, algo.getInput(0));
		assertSame(reference, algo.getInput(1));
	}

	// ----------------------------------------------------------------- activation

	@Test
	void plainToolbarActivationIsNormalAndNeverReadsAValidSlot() throws Exception {
		try (Window w = Window.open()) {
			w.rememberParallelL();
			w.app.setMoveMode();
			w.toolbar(PAR, false);
			assertEquals(Behaviour.NORMAL, w.behaviour(Slot.PARALLEL));
			int before = w.size();
			w.click(R);
			assertEquals(before, w.size(), "a point-first click waits for a reference");
			assertEquals(1, w.selection());
			w.app.setMoveMode();
			w.toolbar(PAR, false);
			w.click(EMPTY);
			assertEquals(before + 1, w.size(), "empty space: only the inherited free point");
			assertEquals(1, w.selection());
			assertSame(w.lookup("l"), w.memory().peek(Slot.PARALLEL), "slot not cleared");
		}
	}

	@Test
	void ctrlToolbarActivationEnablesMemoryAndIsLatched() throws Exception {
		try (Window w = Window.open()) {
			w.toolbar(PAR, true);
			assertEquals(Behaviour.MEMORY_ENABLED, w.behaviour(Slot.PARALLEL));
			w.click(ON_L);
			w.click(P);
			assertConstruction(w.created(), AlgoLinePointLine.class, w.lookup("P"),
					w.lookup("l"));
			// Ctrl pressed and released during construction changes nothing
			for (int id : new int[] {KeyEvent.KEY_PRESSED, KeyEvent.KEY_RELEASED}) {
				SwingUtilities.invokeAndWait(() -> w.app.dispatchKeyEvent(new KeyEvent(
						w.app.getMainComponent(), id, System.currentTimeMillis(),
						id == KeyEvent.KEY_PRESSED ? InputEvent.CTRL_DOWN_MASK : 0,
						KeyEvent.VK_CONTROL, KeyEvent.CHAR_UNDEFINED)));
			}
			w.settle();
			assertEquals(Behaviour.MEMORY_ENABLED, w.behaviour(Slot.PARALLEL));
			w.click(R);
			assertConstruction(w.created(), AlgoLinePointLine.class, w.lookup("R"),
					w.lookup("l"));
			// a Ctrl-click in the view is the inherited gesture; the behaviour stays latched
			w.click(S, InputEvent.CTRL_DOWN_MASK);
			assertEquals(Behaviour.MEMORY_ENABLED, w.behaviour(Slot.PARALLEL));
			assertTrue(w.status().isVisible());
			assertEquals(Behaviour.NORMAL, w.behaviour(Slot.PERPENDICULAR));
		}
	}

	@Test
	void sameBehaviourReactivationKeepsThePartialGesture() throws Exception {
		try (Window w = Window.open()) {
			w.toolbar(PAR, true);
			w.click(ON_M);
			assertEquals(1, w.selection());
			w.toolbar(PAR, true);
			assertEquals(1, w.selection(), "Ctrl reclick in MEMORY_ENABLED keeps it");
			w.click(P);
			assertConstruction(w.created(), AlgoLinePointLine.class, w.lookup("P"),
					w.lookup("m"));
			w.app.setMoveMode();
			w.toolbar(PAR, false);
			w.click(Q);
			w.toolbar(PAR, false);
			assertEquals(1, w.selection(), "plain reclick in NORMAL keeps it");
			w.click(ON_L);
			assertConstruction(w.created(), AlgoLinePointLine.class, w.lookup("Q"),
					w.lookup("l"));
		}
	}

	@Test
	void behaviourChangeCancelsOnlyThePartialGesture() throws Exception {
		try (Window w = Window.open()) {
			w.toolbar(PERP, true);
			w.click(ON_L);
			w.click(P);
			final GeoElement perpL = w.created();
			w.awaitUndo();
			SwingUtilities.invokeAndWait(w.app::setSaved);
			List<GeoElement> completed = w.objects();
			final String xml = w.app.getXML();
			final int history = w.undo().getHistorySize();
			final boolean undoPossible = w.undo().undoPossible();
			final boolean redoPossible = w.undo().redoPossible();
			w.memory().clear();
			w.click(EMPTY);
			assertEquals(completed.size() + 1, w.size(), "provisional free point");
			w.toolbar(PERP, false);
			assertEquals(Behaviour.NORMAL, w.behaviour(Slot.PERPENDICULAR));
			w.awaitUndo();
			assertEquals(completed, w.objects(), "only the provisional point is removed");
			assertEquals(0, w.selection());
			assertEquals(PERP, w.app.getMode());
			assertEquals(history, w.undo().getHistorySize(), "no undo step");
			assertEquals(undoPossible, w.undo().undoPossible());
			assertEquals(redoPossible, w.undo().redoPossible());
			assertTrue(w.app.isSaved(), "the saved state is unchanged");
			assertEquals(xml, w.app.getXML(), "no document difference");
			assertTrue(perpL.isDefined());
			// NORMAL -> Ctrl reclick with a pending reference: cancelled, slot untouched
			w.memory().remember(Slot.PERPENDICULAR, w.lookup("m"));
			w.click(ON_L);
			assertEquals(1, w.selection());
			w.toolbar(PERP, true);
			assertEquals(Behaviour.MEMORY_ENABLED, w.behaviour(Slot.PERPENDICULAR));
			assertEquals(0, w.selection());
			assertSame(w.lookup("m"), w.memory().peek(Slot.PERPENDICULAR));
			assertEquals(completed, w.objects());
		}
	}

	@Test
	void flyoutSelectsPerpendicularWithItsOwnIndependentBehaviourAndSlot() throws Exception {
		try (Window w = Window.open()) {
			w.rememberParallelL();
			w.toolbar(PERP, false);
			assertEquals(Behaviour.NORMAL, w.behaviour(Slot.PERPENDICULAR));
			assertEquals(Behaviour.NORMAL, w.behaviour(Slot.PARALLEL), "departure");
			int before = w.size();
			w.click(Q);
			assertEquals(before, w.size(), "the Parallel slot never feeds Perpendicular");
			w.app.setMoveMode();
			w.toolbar(PERP, true);
			assertEquals(Behaviour.MEMORY_ENABLED, w.behaviour(Slot.PERPENDICULAR));
			assertEquals(Behaviour.NORMAL, w.behaviour(Slot.PARALLEL));
			w.click(ON_M);
			w.click(Q);
			assertConstruction(w.created(), AlgoOrthoLinePointLine.class, w.lookup("Q"),
					w.lookup("m"));
			assertSame(w.lookup("m"), w.memory().peek(Slot.PERPENDICULAR));
			assertSame(w.lookup("l"), w.memory().peek(Slot.PARALLEL));
		}
	}

	@Test
	void keyboardSpaceActivatesNormalAndCtrlSpaceHasNoBinding() throws Exception {
		try (Window w = Window.open()) {
			w.rememberParallelL();
			KeyStroke ctrlSpace = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
					InputEvent.CTRL_DOWN_MASK, false);
			assertNull(w.button().getInputMap(JComponent.WHEN_FOCUSED).get(ctrlSpace),
					"Ctrl+Space fires no toolbar activation");
			Object pressed = w.button().getInputMap(JComponent.WHEN_FOCUSED)
					.get(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false));
			Object released = w.button().getInputMap(JComponent.WHEN_FOCUSED)
					.get(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true));
			SwingUtilities.invokeAndWait(() -> {
				Action press = w.button().getActionMap().get(pressed);
				Action release = w.button().getActionMap().get(released);
				press.actionPerformed(new ActionEvent(w.button(), ActionEvent.ACTION_PERFORMED,
						" "));
				release.actionPerformed(new ActionEvent(w.button(),
						ActionEvent.ACTION_PERFORMED, " "));
			});
			w.settle();
			assertEquals(PAR, w.app.getMode());
			assertEquals(Behaviour.NORMAL, w.behaviour(Slot.PARALLEL));
			assertSame(w.lookup("l"), w.memory().peek(Slot.PARALLEL));
		}
	}

	@Test
	void normalKeepsTheInheritedPointOnPathGestureAndNeverWritesTheSlot() throws Exception {
		try (Window w = Window.open()) {
			w.rememberParallelL();
			w.toolbar(PAR, false);
			w.click(ON_M);
			int before = w.size();
			w.click(new double[] {-4, -4});
			assertEquals(before + 2, w.size(), "point on l and the line through it");
			GeoElement line = w.created();
			GeoElement point = line.getParentAlgorithm().getInput(0).toGeoElement();
			assertSame(AlgoPointOnPath.class, point.getParentAlgorithm().getClass());
			assertSame(w.lookup("l"), point.getParentAlgorithm().getInput(0));
			assertConstruction(line, AlgoLinePointLine.class, point, w.lookup("m"));
			assertSame(w.lookup("l"), w.memory().peek(Slot.PARALLEL),
					"a NORMAL certified construction never replaces the slot");
		}
	}

	// ----------------------------------------------------------------- indirect routes

	@Test
	void menuRegistryActivationIsNormalAlsoForTheActiveMemoryTool() throws Exception {
		try (Window w = Window.open()) {
			w.rememberParallelL();
			w.click(ON_M);
			assertEquals(1, w.selection());
			SwingUtilities.invokeAndWait(() -> ((GuiManagerGeoCeDG) w.app.getGuiManager())
					.getActionRegistry().invoke("construction.parallel-line",
							new ActionEvent(w.frame, ActionEvent.ACTION_PERFORMED, "menu")));
			w.settle();
			assertEquals(PAR, w.app.getMode());
			assertEquals(Behaviour.NORMAL, w.behaviour(Slot.PARALLEL));
			assertEquals(0, w.selection(), "the behaviour change cancelled the gesture");
			assertSame(w.lookup("l"), w.memory().peek(Slot.PARALLEL));
			// a single-argument activation of another tool (user tools, dialogs) departs
			w.toolbar(PAR, true);
			w.app.setMode(EuclidianConstants.MODE_POINT);
			assertEquals(Behaviour.NORMAL, w.behaviour(Slot.PARALLEL));
			assertSame(w.lookup("l"), w.memory().peek(Slot.PARALLEL));
		}
	}

	@Test
	void scriptingSelectsNormalAndNeverTouchesTheMemory() throws Exception {
		try (Window w = Window.open()) {
			assertSame(GgbAPID.class, w.app.getGgbApi().getClass(),
					"the original scripting API");
			w.js("ggbApplet.setMode(" + PAR + ")");
			assertEquals(PAR, w.app.getMode());
			assertEquals(Behaviour.NORMAL, w.behaviour(Slot.PARALLEL), "NORMAL -> NORMAL");
			w.rememberParallelL();
			w.click(ON_M);
			w.js("ggbApplet.setMode(" + PAR + ")");
			assertEquals(Behaviour.NORMAL, w.behaviour(Slot.PARALLEL),
					"scripted reselection of the active memory tool");
			assertEquals(0, w.selection());
			assertSame(w.lookup("l"), w.memory().peek(Slot.PARALLEL));
			w.app.getGgbApi().setMode(PAR);
			assertEquals(Behaviour.NORMAL, w.behaviour(Slot.PARALLEL), "Java API route");
			// a NORMAL construction with another reference neither writes nor clears
			w.click(ON_M);
			w.click(Q);
			assertConstruction(w.created(), AlgoLinePointLine.class, w.lookup("Q"),
					w.lookup("m"));
			assertSame(w.lookup("l"), w.memory().peek(Slot.PARALLEL));
			// MEMORY_ENABLED Parallel -> script Perpendicular -> NORMAL
			w.toolbar(PAR, true);
			w.js("ggbApplet.setMode(" + PERP + ")");
			assertEquals(PERP, w.app.getMode());
			assertEquals(Behaviour.NORMAL, w.behaviour(Slot.PERPENDICULAR));
			assertEquals(Behaviour.NORMAL, w.behaviour(Slot.PARALLEL));
			// Ctrl activation after scripted selections reuses the still-valid memory
			w.toolbar(PAR, true);
			w.click(R);
			assertConstruction(w.created(), AlgoLinePointLine.class, w.lookup("R"),
					w.lookup("l"));
		}
	}

	@Test
	void javaScriptDocumentLoadingKeepsTheOriginalApiAndCommands() throws Exception {
		try (Window w = Window.open()) {
			Object api = w.app.getGgbApi();
			String xml = w.app.getXML();
			String library = "function probe(){ggbApplet.evalCommand('jsok=1');"
					+ "ggbApplet.setMode(" + PAR + ");}";
			SwingUtilities.invokeAndWait(() -> {
				w.app.setXML(xml, true);
				w.app.getKernel().setLibraryJavaScript(library);
			});
			w.settle();
			assertSame(api, w.app.getGgbApi());
			assertSame(GgbAPID.class, w.app.getGgbApi().getClass());
			w.js("probe()");
			assertEquals("1", w.lookup("jsok").toValueString(StringTemplate.defaultTemplate));
			assertEquals(PAR, w.app.getMode());
			assertEquals(Behaviour.NORMAL, w.behaviour(Slot.PARALLEL));
			w.js("ggbApplet.evalCommand('par=Line(P,l)');"
					+ "ggbApplet.evalCommand('perp=PerpendicularLine(Q,m)')");
			assertConstruction(w.lookup("par"), AlgoLinePointLine.class, w.lookup("P"),
					w.lookup("l"));
			assertConstruction(w.lookup("perp"), AlgoOrthoLinePointLine.class, w.lookup("Q"),
					w.lookup("m"));
			assertNull(w.memory().peek(Slot.PARALLEL), "commands never write the memory");
			assertNull(w.memory().peek(Slot.PERPENDICULAR));
		}
	}

	// ----------------------------------------------------------------- host and lifecycle

	@Test
	void hostSynchronizationNeverChangesTheBehaviourOrTheGesture() throws Exception {
		try (Window w = Window.open()) {
			w.rememberParallelL();
			w.click(ON_M);
			w.app.setMode(PAR, ModeSetter.DOCK_PANEL);
			w.app.setMode(PAR, ModeSetter.TOOLBAR);
			assertEquals(Behaviour.MEMORY_ENABLED, w.behaviour(Slot.PARALLEL));
			assertEquals(1, w.selection(), "two-argument same-mode synchronization");
			w.click(Q);
			assertConstruction(w.created(), AlgoLinePointLine.class, w.lookup("Q"),
					w.lookup("m"));
			SwingUtilities.invokeAndWait(() -> {
				GuiManagerD gui = (GuiManagerD) w.app.getGuiManager();
				gui.updateToolbar();
				gui.getLayout().getDockManager().setFocusedPanel(App.VIEW_ALGEBRA);
				gui.getLayout().getDockManager().setFocusedPanel(App.VIEW_EUCLIDIAN);
				w.app.getEuclidianView2(1);
			});
			w.settle();
			assertEquals(PAR, w.app.getMode());
			assertEquals(Behaviour.MEMORY_ENABLED, w.behaviour(Slot.PARALLEL));
			w.awaitUndo();
			w.app.getKernel().undo();
			w.app.getKernel().redo();
			SwingUtilities.invokeAndWait(() -> w.app.setXML(w.app.getXML(), true));
			w.settle();
			assertEquals(Behaviour.MEMORY_ENABLED, w.behaviour(Slot.PARALLEL),
					"undo, redo and clearing setXML call no activation");
			assertNull(w.memory().peek(Slot.PARALLEL), "the F2 lifecycle cleared the slot");
			w.app.setMode(EuclidianConstants.MODE_POINT, ModeSetter.DOCK_PANEL);
			assertEquals(Behaviour.NORMAL, w.behaviour(Slot.PARALLEL),
					"a two-argument change to another tool is a departure");
		}
	}

	@Test
	void anUnusedToolbarMarkNeverReachesALaterActivation() throws Exception {
		try (Window w = Window.open()) {
			SwingUtilities.invokeAndWait(() -> w.app.getToolActivation()
					.recordToolbarActivation(new ActionEvent(w.button(),
							ActionEvent.ACTION_PERFORMED, Integer.toString(PAR),
							ActionEvent.CTRL_MASK)));
			SwingUtilities.invokeAndWait(() -> w.app.setMode(PAR));
			assertEquals(Behaviour.NORMAL, w.behaviour(Slot.PARALLEL));
			SwingUtilities.invokeAndWait(() -> w.app.getToolActivation()
					.recordToolbarActivation(new ActionEvent(w.button(),
							ActionEvent.ACTION_PERFORMED, Integer.toString(PAR),
							ActionEvent.CTRL_MASK)));
			w.js("ggbApplet.setMode(" + PAR + ")");
			assertEquals(Behaviour.NORMAL, w.behaviour(Slot.PARALLEL));
			w.toolbar(PAR, true);
			assertEquals(Behaviour.MEMORY_ENABLED, w.behaviour(Slot.PARALLEL));
			w.js("ggbApplet.setMode(" + PAR + ")");
			assertEquals(Behaviour.NORMAL, w.behaviour(Slot.PARALLEL),
					"the consumed toolbar mark does not decide the next activation");
		}
	}

	@Test
	void departureEscapeAndNewResetToNormal() throws Exception {
		try (Window w = Window.open()) {
			w.rememberParallelL();
			w.app.setMode(EuclidianConstants.MODE_POINT);
			assertEquals(Behaviour.NORMAL, w.behaviour(Slot.PARALLEL), "another tool");
			assertSame(w.lookup("l"), w.memory().peek(Slot.PARALLEL));
			w.toolbar(PAR, true);
			SwingUtilities.invokeAndWait(() -> w.app.dispatchKeyEvent(new KeyEvent(
					w.app.getMainComponent(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
					0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED)));
			w.settle();
			assertEquals(EuclidianConstants.MODE_MOVE, w.app.getMode());
			assertEquals(Behaviour.NORMAL, w.behaviour(Slot.PARALLEL), "Escape");
			assertSame(w.lookup("l"), w.memory().peek(Slot.PARALLEL));
			w.toolbar(PAR, false);
			assertEquals(Behaviour.NORMAL, w.behaviour(Slot.PARALLEL), "return without Ctrl");
			w.toolbar(PAR, true);
			w.click(R);
			assertConstruction(w.created(), AlgoLinePointLine.class, w.lookup("R"),
					w.lookup("l"));
			w.awaitUndo();
			SwingUtilities.invokeAndWait(() -> {
				// the asynchronous undo store has landed; nothing is pending to save
				w.app.setSaved();
				w.app.fileNew();
			});
			w.settle();
			assertEquals(Behaviour.NORMAL, w.behaviour(Slot.PARALLEL), "New");
			assertNull(w.memory().peek(Slot.PARALLEL));
		}
	}

	@Test
	void languageChangeKeepsToolBehaviourSlotsAndRerendersTheStatus() throws Exception {
		try (Window w = Window.open()) {
			w.rememberParallelL();
			w.toolbar(PERP, true);
			w.click(ON_M);
			w.click(Q);
			SwingUtilities.invokeAndWait(() -> w.app.setLanguage(Locale.forLanguageTag("es")));
			w.settle();
			assertEquals(PERP, w.app.getMode());
			assertEquals(Behaviour.MEMORY_ENABLED, w.behaviour(Slot.PERPENDICULAR));
			assertEquals("Perpendicular · Memoria", w.status().getText());
			assertSame(w.lookup("l"), w.memory().peek(Slot.PARALLEL));
			assertSame(w.lookup("m"), w.memory().peek(Slot.PERPENDICULAR));
			w.click(R);
			assertConstruction(w.created(), AlgoOrthoLinePointLine.class, w.lookup("R"),
					w.lookup("m"));
			SwingUtilities.invokeAndWait(() -> w.app.setLanguage(Locale.ENGLISH));
			w.settle();
			assertEquals(Behaviour.MEMORY_ENABLED, w.behaviour(Slot.PERPENDICULAR));
			assertEquals("Perpendicular Line · Memory", w.status().getText());
			w.toolbar(PERP, false);
			SwingUtilities.invokeAndWait(() -> w.app.setLanguage(Locale.forLanguageTag("es")));
			w.settle();
			assertEquals(PERP, w.app.getMode());
			assertEquals(Behaviour.NORMAL, w.behaviour(Slot.PERPENDICULAR));
			assertFalse(w.status().isVisible());
			SwingUtilities.invokeAndWait(() -> w.app.setLanguage(Locale.ENGLISH));
		}
	}

	@Test
	void aSlotInvalidatedWhileNormalIsNotRevivedByALaterCtrlActivation() throws Exception {
		try (Window w = Window.open()) {
			w.rememberParallelL();
			w.toolbar(PAR, false);
			SwingUtilities.invokeAndWait(() -> w.lookup("l").remove());
			w.settle();
			w.toolbar(PAR, true);
			int before = w.size();
			w.click(R);
			assertEquals(before, w.size(), "a deleted reference is never reused");
			assertNull(w.memory().peek(Slot.PARALLEL));
		}
	}

	// ----------------------------------------------------------------- status

	@Test
	void statusSegmentIsLaidOutOnlyWhileMemoryIsEnabled() throws Exception {
		try (Window w = Window.open()) {
			JLabel status = w.status();
			assertFalse(status.isVisible(), "hidden by default");
			w.toolbar(PAR, false);
			assertFalse(status.isVisible(), "hidden in NORMAL");
			w.toolbar(PAR, true);
			assertNull(w.memory().peek(Slot.PARALLEL), "shown although no slot is held");
			assertLaidOut(w, "Parallel Line · Memory");
			w.toolbar(PERP, true);
			assertLaidOut(w, "Perpendicular Line · Memory");
			w.toolbar(PERP, false);
			assertFalse(status.isVisible(), "hidden after a behaviour change");
			w.toolbar(PAR, true);
			w.app.setMode(EuclidianConstants.MODE_POINT);
			assertFalse(status.isVisible(), "hidden for other tools");
			SwingUtilities.invokeAndWait(() -> w.app.setLanguage(Locale.forLanguageTag("es")));
			w.settle();
			w.toolbar(PAR, true);
			assertLaidOut(w, "Paralela · Memoria");
			w.toolbar(PERP, true);
			assertLaidOut(w, "Perpendicular · Memoria");
			w.app.setMoveMode();
			SwingUtilities.invokeAndWait(() -> w.app.setLanguage(Locale.ENGLISH));
			w.settle();
			assertFalse(w.app.getXML().contains("Memory"), "never in the document");
		}
	}

	private static void assertLaidOut(Window w, String text) throws Exception {
		SwingUtilities.invokeAndWait(() -> w.frame.validate());
		w.settle();
		JLabel status = w.status();
		GeoCeDGStatusBar bar = w.app.getStatusBar();
		Rectangle bounds = status.getBounds();
		String context = bounds + " in " + bar.getSize();
		assertTrue(status.isVisible(), context);
		assertEquals(text, status.getText());
		assertEquals(w.app.layerText("AuthoringMemory.Status.Tooltip"), status.getToolTipText());
		assertTrue(bounds.width > 0 && bounds.height > 0, context);
		assertTrue(bounds.y >= 0 && bounds.y + bounds.height <= bar.getHeight(),
				"inside the one-row bar: " + context);
		assertTrue(bounds.x + bounds.width <= bar.getWidth(), context);
		assertEquals(status.getPreferredSize().width, bounds.width, "shown in full: " + context);
		assertEquals(bar.getSegment(GeoCeDGStatusBar.LAYER_SEGMENT).getY(), bounds.y,
				"on the row of the permanent segments: " + context);
	}
}
