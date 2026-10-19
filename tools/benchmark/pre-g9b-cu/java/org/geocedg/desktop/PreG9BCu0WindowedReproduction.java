/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.geocedg.desktop.GeoCeDGLineReferenceMemory.Slot;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.euclidian.event.AbstractEvent;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.App;
import org.geogebra.common.main.error.ErrorHelper;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.euclidian.event.MouseEventD;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.app.GeoGebraFrame;
import org.geogebra.desktop.gui.toolbar.ModeToggleMenuD;
import org.geogebra.desktop.gui.toolbar.ToolbarContainer;
import org.geogebra.desktop.gui.view.consprotocol.ConstructionProtocolNavigationD;
import org.geogebra.desktop.gui.view.consprotocol.ConstructionProtocolViewD;

/**
 * PRE-G9B-CU0 windowed reproduction of B02 (construction-protocol row spacing) and B03
 * (Parallel/Perpendicular reference memories) in the product: the real
 * {@link GeoCeDG#main} start-up with an isolated settings file, the product look and feel,
 * frame, toolbar, construction-protocol view and navigation bar. Events are dispatched to the
 * real components; the native pointer and keyboard are never moved. Each step records row
 * heights or memory states and a screenshot of the relevant component. Evidence only: the
 * scenario assigns no verdict.
 */
final class PreG9BCu0WindowedReproduction {
	private static final long STEP_TIMEOUT_MS = 120_000;
	private final Path work;
	private final Path output;
	private final Map<String, Object> report = new LinkedHashMap<>();
	private final List<Object> b02 = new ArrayList<>();
	private final List<Object> b03 = new ArrayList<>();
	private final List<String> uncaught = new ArrayList<>();
	private AppGeoCeDG app;
	private GeoGebraFrame frame;
	private int shot;

	PreG9BCu0WindowedReproduction(Path work, Path output) {
		this.work = work;
		this.output = output;
	}

	void run() throws Exception {
		Thread.setDefaultUncaughtExceptionHandler((thread, failure) -> {
			synchronized (uncaught) {
				uncaught.add(thread.getName() + ": " + failure);
			}
		});
		try {
			start();
			reproduceB02();
			reproduceB03();
		} catch (Throwable failure) {
			report.put("aborted", failure.toString());
			throw failure;
		} finally {
			synchronized (uncaught) {
				report.put("uncaught_exceptions", new ArrayList<>(uncaught));
			}
			report.put("b02", b02);
			report.put("b03", b03);
			Files.writeString(output.resolve("windowed-b02-b03.json"),
					PreG9BCu0References.json(report), StandardCharsets.UTF_8);
			// The product frame is left as it is: disposing or hiding the last product
			// frame ends the test JVM through the product's own exit path.
		}
	}

	// ---------------------------------------------------------------- start-up

	private void start() throws Exception {
		Path settings = work.resolve("cu0-isolated-preferences.properties");
		report.put("entry_point", "org.geocedg.desktop.GeoCeDG.main");
		report.put("settings", "isolated, created fresh by the product in the harness"
				+ " temporary folder");
		report.put("java_runtime", System.getProperty("java.runtime.version"));
		report.put("java_vendor", System.getProperty("java.vendor"));
		GeoCeDG.main(new String[] {"--settingsFile=" + settings.toAbsolutePath()});
		long deadline = System.currentTimeMillis() + STEP_TIMEOUT_MS;
		while (frame == null) {
			for (GeoGebraFrame candidate : new ArrayList<>(GeoGebraFrame.getInstances())) {
				if (candidate.isShowing() && candidate.getApplication() instanceof AppGeoCeDG
						&& !candidate.getApplication().isIniting()) {
					frame = candidate;
				}
			}
			if (System.currentTimeMillis() > deadline) {
				throw new IllegalStateException("GeoCeDG frame did not show");
			}
			Thread.sleep(200);
		}
		app = (AppGeoCeDG) frame.getApplication();
		settle();
		onEdt(() -> {
			frame.setSize(1500, 980);
			frame.setLocation(10, 10);
			frame.validate();
			report.put("look_and_feel", UIManager.getLookAndFeel().getName());
			report.put("ui_table_row_height", String.valueOf(UIManager.get("Table.rowHeight")));
			report.put("frame", frame.getClass().getName());
			return null;
		});
		settle();
	}

	// ---------------------------------------------------------------- B02

	private void reproduceB02() throws Exception {
		File model = PreG9BCu0Corpus.withoutE2(work);
		onEdt(() -> {
			app.setSaved();
			((GuiManagerD) app.getGuiManager()).doOpenFiles(new File[] {model}, true);
			return null;
		});
		awaitCondition(() -> app.getKernel().lookupLabel("o_1") != null, "model opened");
		settle();
		onEdt(() -> {
			app.getGuiManager().setShowView(true, App.VIEW_CONSTRUCTION_PROTOCOL);
			return null;
		});
		settle();
		recordProtocol("open author model and show the construction protocol");
		ConstructionProtocolNavigationD navigation = onEdt(() ->
				(ConstructionProtocolNavigationD) ((GuiManagerD) app.getGuiManager())
						.getConstructionProtocolNavigation());
		report.put("protocol_navigation_bar", navigation == null ? "absent" : "present");
		if (navigation != null) {
			clickNavigation(navigation, "btFirst", "navigation bar: first step");
			clickNavigation(navigation, "btNext", "navigation bar: next step");
			clickNavigation(navigation, "btNext", "navigation bar: next step");
			clickNavigation(navigation, "btPrev", "navigation bar: previous step");
			clickNavigation(navigation, "btLast", "navigation bar: last step");
		}
		tableKey(KeyEvent.VK_UP, "protocol table: arrow up");
		tableKey(KeyEvent.VK_DOWN, "protocol table: arrow down");
		tableKey(KeyEvent.VK_HOME, "protocol table: home");
		tableKey(KeyEvent.VK_END, "protocol table: end");
		tableClick(10, "protocol table: click on row 10");
		onEdt(() -> {
			app.getKernel().getAlgebraProcessor().processAlgebraCommandNoExceptionHandling(
					"cu0Probe=(1,1)", false, ErrorHelper.silent(), false, null);
			app.storeUndoInfo();
			return null;
		});
		settle();
		recordProtocol("edit: create a point by input");
		onEdt(() -> {
			app.getGuiManager().undo();
			return null;
		});
		settle();
		recordProtocol("undo");
		onEdt(() -> {
			SwingUtilities.updateComponentTreeUI(frame);
			return null;
		});
		settle();
		recordProtocol("UI refresh: SwingUtilities.updateComponentTreeUI(frame)");
		if (navigation != null) {
			clickNavigation(navigation, "btNext", "navigation bar after UI refresh: next step");
			clickNavigation(navigation, "btLast", "navigation bar after UI refresh: last step");
		}
		int font = onEdt(() -> app.getFontSize());
		onEdt(() -> {
			app.setFontSize(font + 2, true);
			return null;
		});
		settle();
		recordProtocol("font size +2");
		onEdt(() -> {
			app.setFontSize(font, true);
			return null;
		});
		settle();
		recordProtocol("font size restored");
		Locale locale = onEdt(() -> app.getLocale());
		onEdt(() -> {
			app.setLanguage(Locale.ENGLISH.equals(locale) ? Locale.forLanguageTag("es")
					: Locale.ENGLISH);
			return null;
		});
		settle();
		recordProtocol("language change");
		onEdt(() -> {
			app.setLanguage(locale);
			return null;
		});
		settle();
		recordProtocol("language restored");
		if (navigation != null) {
			clickNavigation(navigation, "btPrev", "navigation bar after language: previous");
		}
	}

	private void clickNavigation(ConstructionProtocolNavigationD navigation, String field,
			String title) throws Exception {
		onEdt(() -> {
			Field button = ConstructionProtocolNavigationD.class.getDeclaredField(field);
			button.setAccessible(true);
			((JButton) button.get(navigation)).doClick(0);
			return null;
		});
		settle();
		recordProtocol(title);
	}

	private void tableKey(int key, String title) throws Exception {
		onEdt(() -> {
			JTable table = table();
			table.requestFocusInWindow();
			table.dispatchEvent(new KeyEvent(table, KeyEvent.KEY_PRESSED,
					System.currentTimeMillis(), 0, key, KeyEvent.CHAR_UNDEFINED));
			table.dispatchEvent(new KeyEvent(table, KeyEvent.KEY_RELEASED,
					System.currentTimeMillis(), 0, key, KeyEvent.CHAR_UNDEFINED));
			return null;
		});
		settle();
		recordProtocol(title);
	}

	private void tableClick(int row, String title) throws Exception {
		onEdt(() -> {
			JTable table = table();
			Rectangle cell = table.getCellRect(Math.min(row, table.getRowCount() - 1), 2, true);
			int x = cell.x + cell.width / 2;
			int y = cell.y + cell.height / 2;
			for (int id : new int[] {MouseEvent.MOUSE_PRESSED, MouseEvent.MOUSE_RELEASED,
					MouseEvent.MOUSE_CLICKED}) {
				table.dispatchEvent(new MouseEvent(table, id, System.currentTimeMillis(),
						id == MouseEvent.MOUSE_PRESSED ? InputEvent.BUTTON1_DOWN_MASK : 0, x, y,
						1, false, MouseEvent.BUTTON1));
			}
			return null;
		});
		settle();
		recordProtocol(title);
	}

	private JTable table() {
		return ((ConstructionProtocolViewD) app.getGuiManager().getConstructionProtocolView())
				.getTable();
	}

	private void recordProtocol(String title) throws Exception {
		Map<String, Object> row = onEdt(() -> {
			JTable table = table();
			Map<Integer, Integer> histogram = new TreeMap<>();
			for (int i = 0; i < table.getRowCount(); i++) {
				histogram.merge(table.getRowHeight(i), 1, Integer::sum);
			}
			Map<String, Object> values = new LinkedHashMap<>();
			values.put("step", title);
			values.put("construction_step", app.getKernel().getConstructionStep());
			values.put("rows", table.getRowCount());
			Map<String, Object> heights = new LinkedHashMap<>();
			for (Map.Entry<Integer, Integer> entry : histogram.entrySet()) {
				heights.put(entry.getKey() + " px", entry.getValue());
			}
			values.put("row_heights", heights);
			values.put("table_row_height_property", table.getRowHeight());
			values.put("table_font_size", table.getFont().getSize());
			values.put("ui_table_row_height", String.valueOf(UIManager.get("Table.rowHeight")));
			values.put("columns", table.getColumnCount());
			return values;
		});
		row.put("screenshot", screenshot("b02", ((ConstructionProtocolViewD) app.getGuiManager()
				.getConstructionProtocolView()).getCpPanel()));
		b02.add(row);
	}

	// ---------------------------------------------------------------- B03

	private static final double[] ON_L = {0, -4};
	private static final double[] ON_M = {0, 4.5};
	private static final double[] P = {-2, 1};
	private static final double[] Q = {2, 0};
	private static final double[] R = {4, 3};
	private static final double[] S = {-1, 3};
	private double[] lastClick;

	private void reproduceB03() throws Exception {
		onEdt(() -> {
			app.getGuiManager().setShowView(false, App.VIEW_CONSTRUCTION_PROTOCOL);
			app.setSaved();
			app.clearConstruction();
			app.setSaved();
			return null;
		});
		settle();
		onEdt(() -> {
			EuclidianView view = app.getEuclidianView1();
			view.setCoordSystem(450, 330, 30, 30);
			for (String input : new String[] {"A=(-6,-4)", "B=(6,-4)", "l=Line(A,B)",
					"C=(-6,4)", "D=(6,5)", "m=Line(C,D)", "P=(-2,1)", "Q=(2,0)", "R=(4,3)",
					"S=(-1,3)"}) {
				app.getKernel().getAlgebraProcessor().processAlgebraCommandNoExceptionHandling(
						input, false, ErrorHelper.silent(), false, null);
			}
			app.storeUndoInfo();
			return null;
		});
		settle();
		recordMemory("scene built (lines l, m; points P, Q, R, S)");
		toolbar(EuclidianConstants.MODE_PARALLEL, true);
		recordMemory("Ctrl + toolbar: Parallel (memory requested)");
		click(ON_L);
		recordMemory("click on l: remember the Parallel reference");
		click(P);
		recordMemory("click on P: parallel through P");
		click(Q);
		recordMemory("click on Q: parallel through Q with the remembered reference");
		onEdt(() -> {
			app.getGuiManager().setShowView(true, App.VIEW_CONSTRUCTION_PROTOCOL);
			return null;
		});
		settle();
		ConstructionProtocolNavigationD navigation = onEdt(() ->
				(ConstructionProtocolNavigationD) ((GuiManagerD) app.getGuiManager())
						.getConstructionProtocolNavigation());
		if (navigation != null) {
			navigate(navigation, "btPrev");
			recordMemory("protocol navigation bar: previous step");
			navigate(navigation, "btFirst");
			recordMemory("protocol navigation bar: first step");
			navigate(navigation, "btLast");
			recordMemory("protocol navigation bar: last step");
		}
		click(R);
		recordMemory("click on R after navigation (Parallel still active)");
		toolbar(EuclidianConstants.MODE_ORTHOGONAL, true);
		recordMemory("Ctrl + toolbar: Perpendicular (memory requested)");
		click(ON_M);
		recordMemory("click on m: remember the Perpendicular reference");
		click(S);
		recordMemory("click on S: perpendicular through S");
		toolbar(EuclidianConstants.MODE_PARALLEL, false);
		recordMemory("toolbar without Ctrl: Parallel (normal activation)");
		click(R);
		recordMemory("click on R with Parallel in normal activation");
		toolbar(EuclidianConstants.MODE_PARALLEL, true);
		recordMemory("Ctrl + toolbar: Parallel again");
		click(S);
		recordMemory("click on S with Parallel memory requested again");
		onEdt(() -> {
			app.getGuiManager().undo();
			return null;
		});
		settle();
		recordMemory("undo");
	}

	private void navigate(ConstructionProtocolNavigationD navigation, String field)
			throws Exception {
		onEdt(() -> {
			Field button = ConstructionProtocolNavigationD.class.getDeclaredField(field);
			button.setAccessible(true);
			((JButton) button.get(navigation)).doClick(0);
			return null;
		});
		settle();
	}

	private void recordMemory(String title) throws Exception {
		Map<String, Object> row = onEdt(() -> {
			GeoCeDGLineReferenceMemory memory = ((GeoCeDGEuclidianController) app
					.getEuclidianView1().getEuclidianController()).getReferenceMemory();
			Map<String, Object> values = new LinkedHashMap<>();
			values.put("step", title);
			values.put("mode", app.getMode());
			values.put("parallel_behaviour", String.valueOf(app.getToolActivation()
					.behaviour(Slot.PARALLEL)));
			values.put("perpendicular_behaviour", String.valueOf(app.getToolActivation()
					.behaviour(Slot.PERPENDICULAR)));
			values.put("parallel_slot", String.valueOf(memory.getState(Slot.PARALLEL)));
			values.put("perpendicular_slot", String.valueOf(memory.getState(Slot.PERPENDICULAR)));
			values.put("objects", app.getKernel().getConstruction().getGeoSetConstructionOrder()
					.size());
			values.put("construction_step", app.getKernel().getConstructionStep());
			JLabel status = app.getStatusBar().getSegment(GeoCeDGStatusBar.TOOL_MEMORY_SEGMENT);
			values.put("status_memory_text", status == null ? null : status.getText());
			List<String> lines = new ArrayList<>();
			for (GeoElement geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
				if (geo.getParentAlgorithm() != null && geo.isGeoLine()) {
					lines.add(geo.getLabelSimple() + "=" + geo.getParentAlgorithm()
							.getClassName());
				}
			}
			values.put("constructed_lines", lines);
			return values;
		});
		row.put("screenshot", screenshot("b03", frame.getRootPane()));
		b03.add(row);
	}

	private void click(double[] at) throws Exception {
		if (at == lastClick) {
			Thread.sleep(EuclidianConstants.DOUBLE_CLICK_DELAY + 300L);
		}
		lastClick = at;
		onEdt(() -> {
			EuclidianView view = app.getEuclidianView1();
			AbstractEvent event = MouseEventD.wrapEvent(new MouseEvent(
					((EuclidianViewD) view).getJPanel(), MouseEvent.MOUSE_PRESSED,
					System.currentTimeMillis(), 0, view.toScreenCoordX(at[0]),
					view.toScreenCoordY(at[1]), 1, false, MouseEvent.BUTTON1));
			GeoCeDGEuclidianController controller = (GeoCeDGEuclidianController) view
					.getEuclidianController();
			controller.wrapMousePressed(event);
			controller.wrapMouseReleased(event);
			return null;
		});
		settle();
	}

	/** Real toolbar activation by posted press/release, with Ctrl as modifier if asked. */
	private void toolbar(int mode, boolean ctrl) throws Exception {
		ModeToggleMenuD menu = onEdt(this::menu);
		JToggleButton button = onEdt(() -> GeoCeDGToolbarContainer.toolbarButton(menu));
		Component target;
		if (onEdt(() -> button.getActionCommand().equals(Integer.toString(mode)))) {
			target = button;
		} else {
			onEdt(() -> {
				menu.setPopupVisible(true);
				return null;
			});
			settle();
			target = onEdt(() -> item(menu, mode));
		}
		int x = target.getWidth() / 2;
		int y = target.getHeight() / 2;
		if (target instanceof JToggleButton toggle && toggle.getIcon() != null) {
			y = Math.min(y, toggle.getIcon().getIconHeight() / 2);
		}
		int modifiers = ctrl ? InputEvent.CTRL_DOWN_MASK : 0;
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(target,
				MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
				modifiers | InputEvent.BUTTON1_DOWN_MASK, x, y, 1, false, MouseEvent.BUTTON1));
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(target,
				MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), modifiers, x, y, 1, false,
				MouseEvent.BUTTON1));
		settle();
		onEdt(() -> {
			menu.setPopupVisible(false);
			return null;
		});
		settle();
	}

	private ModeToggleMenuD menu() {
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

	private static JMenuItem item(ModeToggleMenuD menu, int mode) throws Exception {
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

	// ---------------------------------------------------------------- helpers

	private String screenshot(String prefix, Component component) throws Exception {
		String name = String.format(Locale.ROOT, "%s-%02d.png", prefix, ++shot);
		BufferedImage image = onEdt(() -> {
			int width = Math.max(1, component.getWidth());
			int height = Math.max(1, component.getHeight());
			BufferedImage picture = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g = picture.createGraphics();
			component.paint(g);
			g.dispose();
			return picture;
		});
		ImageIO.write(image, "png", output.resolve(name).toFile());
		return name;
	}

	private void settle() throws Exception {
		Thread.sleep(250);
		onEdt(() -> null);
		onEdt(() -> null);
	}

	private static void awaitCondition(Callable<Boolean> condition, String what)
			throws Exception {
		long deadline = System.currentTimeMillis() + STEP_TIMEOUT_MS;
		while (!Boolean.TRUE.equals(PreG9BR6PlusF3Fixtures.onEdt(condition))) {
			if (System.currentTimeMillis() > deadline) {
				throw new IllegalStateException("timeout: " + what);
			}
			Thread.sleep(200);
		}
	}

	private static <T> T onEdt(Callable<T> action) throws Exception {
		AtomicReference<Throwable> stuck = new AtomicReference<>();
		Thread watchdog = new Thread(() -> {
			try {
				Thread.sleep(STEP_TIMEOUT_MS);
				StringBuilder dump = new StringBuilder();
				for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces()
						.entrySet()) {
					if (entry.getKey().getName().startsWith("AWT-EventQueue")) {
						for (StackTraceElement element : entry.getValue()) {
							dump.append(element).append('\n');
						}
					}
				}
				stuck.set(new IllegalStateException("EDT step exceeded timeout:\n" + dump));
			} catch (InterruptedException finished) {
				// step completed
			}
		}, "cu0-edt-watchdog");
		watchdog.setDaemon(true);
		watchdog.start();
		try {
			return PreG9BR6PlusF3Fixtures.onEdt(action);
		} finally {
			watchdog.interrupt();
			if (stuck.get() != null) {
				System.err.println(stuck.get().getMessage());
			}
		}
	}
}
