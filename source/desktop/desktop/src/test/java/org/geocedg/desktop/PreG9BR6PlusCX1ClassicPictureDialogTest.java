/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.geocedg.desktop.GeoCeDGProfile.ActionDefinition;
import org.geocedg.desktop.export.ExportScalePresentation;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.main.Localization;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.awt.AwtFactoryD;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.main.AppD;
import org.geogebra.desktop.main.GeoGebraPreferencesD;
import org.geogebra.desktop.main.GuiManagerInterfaceD;
import org.geogebra.desktop.util.LoggerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-R6-plus-C-X1 (OBS-R6PLUS-CLASSIC-PICTURE-DIALOG-OFF-EDT): the Classic
 * File menu action Graphics View as Picture builds, populates, packs, loads the
 * preferences of and shows the export dialog inside its own EDT dispatch
 * (INV-X1). The primary assertion is thread ownership: every container and
 * hierarchy event of the dialog, which AWT dispatches synchronously on the
 * mutating thread, happens on the EDT, and no repaint of the dialog is requested
 * from another thread. Before the correction the action started a worker thread,
 * so these assertions fail deterministically, not by chance.
 */
class PreG9BR6PlusCX1ClassicPictureDialogTest {

	private static final String DIALOG_CLASS =
			"org.geogebra.desktop.export.GraphicExportDialog";
	private static final int CYCLES = 5;
	private static final long SCENARIO_TIMEOUT_SECONDS = 180;
	/** clipboard-fallback calls of the Classic hosts, with their EDT flag */
	private static final List<Boolean> FALLBACK = Collections.synchronizedList(
			new ArrayList<>());
	/** the ownership recorder of the scenario JVM */
	private static Ownership ownership;

	@TempDir
	Path temporary;

	@Test
	void classicMenuAndAcceleratorBuildAndShowTheDialogOnTheEdt() throws Exception {
		isolated(temporary, "classicMenuAndAccelerator");
	}

	@Test
	void anExceptionOfTheDialogStillReachesTheUnchangedFallback() throws Exception {
		isolated(temporary, "fallback");
	}

	@Test
	void geocedgPictureRouteIsUnchanged() throws Exception {
		isolated(temporary, "geocedg");
	}

	/**
	 * Runs one scenario in a fresh JVM of the test classpath. The test JVM then
	 * retains no application, frame or dialog: the complete Desktop suite runs
	 * close to its heap limit, and the embedded hosts of an in-process scenario
	 * would stay reachable for the rest of it.
	 */
	static void isolated(Path directory, String scenario) throws Exception {
		Path out = directory.resolve(scenario + ".txt");
		Path console = directory.resolve(scenario + ".console.txt");
		List<String> command = new ArrayList<>(List.of(
				ProcessHandle.current().info().command().orElseThrow(),
				"-cp", System.getProperty("java.class.path"),
				"--add-exports", "java.base/java.lang=ALL-UNNAMED",
				"--add-exports", "java.desktop/sun.awt=ALL-UNNAMED",
				"--add-exports", "java.desktop/sun.java2d=ALL-UNNAMED",
				Scenario.class.getName(), scenario, directory.toString(), out.toString()));
		Process process = new ProcessBuilder(command).redirectErrorStream(true)
				.redirectOutput(console.toFile()).start();
		boolean ended = process.waitFor(SCENARIO_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		if (!ended) {
			process.destroyForcibly();
		}
		String record = Files.exists(out) ? Files.readString(out) : "(no record)";
		assertTrue(ended, scenario + " ended within " + SCENARIO_TIMEOUT_SECONDS + " s: "
				+ record);
		assertEquals("SCENARIO OK " + scenario, record.trim(), record);
		assertEquals(0, process.exitValue(), record);
	}

	/** Main class of one scenario JVM. */
	public static final class Scenario {
		private Scenario() {
		}

		/**
		 * @param args scenario, working directory, result file
		 */
		public static void main(String[] args) {
			String result;
			try {
				AwtFactory.setPrototypeIfNull(new AwtFactoryD());
				Log.setLogger(new LoggerD());
				// a test-owned preference file, as the Classic product uses with
				// --settingsfile, and an in-memory GeoCeDG unit-preference store
				GeoGebraPreferencesD.setPropertyFileName(
						Paths.get(args[1]).resolve("classic-x1.properties").toString());
				GeoCeDGUnitPreferences.useStoreForTesting(
						new PreG9BR6PlusD1DocumentUnitsTest.MemoryStore());
				ownership = Ownership.install();
				switch (args[0]) {
				case "classicMenuAndAccelerator":
					classicMenuAndAccelerator();
					break;
				case "fallback":
					fallback();
					break;
				case "geocedg":
					geocedg();
					break;
				default:
					throw new IllegalArgumentException(args[0]);
				}
				result = "SCENARIO OK " + args[0];
			} catch (Throwable failure) {
				StringBuilder text = new StringBuilder("SCENARIO FAILED " + args[0] + ": "
						+ failure);
				for (StackTraceElement element : failure.getStackTrace()) {
					text.append("\n    at ").append(element);
				}
				result = text.toString();
			}
			try {
				Files.write(Paths.get(args[2]), result.getBytes(StandardCharsets.UTF_8));
			} catch (Exception ignored) {
				// the parent reports the missing record
			}
			Runtime.getRuntime().halt(0);
		}
	}

	// ------------------------------------------------- T-X1-OWNERSHIP and friends

	/**
	 * One Classic application: five menu openings and one accelerator opening,
	 * each checked for ownership, population, the host scale UI and Cancel.
	 */
	static void classicMenuAndAccelerator() throws Exception {
		AppD classic = classic();
		assertNull(classic.getExportScalePresentation(), "Classic keeps the host seam");
		JMenuItem item = pictureItem(classic);
		final String xml = onEdt(classic::getXML);
		final boolean saved = onEdt(classic::isSaved);
		for (int cycle = 1; cycle <= CYCLES; cycle++) {
			JDialog dialog = openThroughTheMenu(item, cycle);
			settle();
			assertPopulated(dialog, classic.getLocalization(), cycle);
			assertHostScaleUi(dialog, classic, cycle);
			cancel(dialog, classic.getLocalization(), cycle);
		}

		// T-X1-ACCELERATOR: Ctrl+Shift+U is this item's accelerator; from a text field
		// the global dispatcher declines and the key bindings invoke this action
		KeyStroke accelerator = item.getAccelerator();
		assertNotNull(accelerator, "the item has its Ctrl+Shift+U accelerator");
		assertEquals(KeyEvent.VK_U, accelerator.getKeyCode());
		int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
		assertEquals(shortcut | InputEvent.SHIFT_DOWN_MASK,
				accelerator.getModifiers() & (shortcut | InputEvent.SHIFT_DOWN_MASK),
				"menu shortcut key plus Shift");
		Action action = item.getAction();
		AtomicReference<JDialog> shown = new AtomicReference<>();
		onEdt(() -> {
			Set<Window> before = exportDialogs();
			action.actionPerformed(new ActionEvent(item, ActionEvent.ACTION_PERFORMED,
					"accelerator"));
			shown.set(newShowingDialog(before));
			return null;
		});
		assertNotNull(shown.get(), "the dialog is showing when the accelerator returns");
		settle();
		assertPopulated(shown.get(), classic.getLocalization(), CYCLES + 1);
		cancel(shown.get(), classic.getLocalization(), CYCLES + 1);

		ownership.assertEdtOnly(CYCLES + 1);
		// T-X1-CANCEL: Cancel saved the dialog preferences as before (host keys and values)
		assertEquals("300", GeoGebraPreferencesD.getPref().loadPreference(
				GeoGebraPreferencesD.EXPORT_PIC_DPI, "unset"));
		assertEquals("png", GeoGebraPreferencesD.getPref().loadPreference(
				GeoGebraPreferencesD.EXPORT_PIC_FORMAT, "unset"));
		// T-X1-NO-SERIALIZATION: opening and cancelling never touches the document.
		// The <gui> window record is excluded: the dialog owner is the host frame,
		// which an embedded test application creates lazily on first use, before and
		// after the correction alike; it is not document content.
		assertEquals(withoutGui(xml), withoutGui(onEdt(classic::getXML)),
				"document XML unchanged");
		assertEquals(saved, onEdt(classic::isSaved), "saved state unchanged");
		assertEquals(List.of(), FALLBACK, "the clipboard fallback was not reached");
	}

	// -------------------------------------------------------------- T-X1-FALLBACK

	static void fallback() throws Exception {
		AppD classic = onEdt(() -> new AppD(new CommandLineArguments(
				new String[] {"--silent"}), new JPanel(), true) {
			@Override
			protected GuiManagerInterfaceD newGuiManager() {
				return new GuiManagerD(this) {
					@Override
					public void showGraphicExport() {
						throw new IllegalStateException("C-X1 fallback probe");
					}
				};
			}

			@Override
			public void copyGraphicsViewToClipboard() {
				// never the system clipboard: only the call and its thread are recorded
				FALLBACK.add(SwingUtilities.isEventDispatchThread());
			}
		});
		G9U1TestApp.withoutWindowDispatcher(classic);
		JMenuItem item = pictureItem(classic);
		onEdt(() -> {
			item.doClick(0);
			return null;
		});
		assertEquals(List.of(Boolean.TRUE), FALLBACK,
				"the catch branch ran once, synchronously, on the EDT");
		assertNull(onEdt(PreG9BR6PlusCX1ClassicPictureDialogTest::showingDialog));
		assertEquals(Cursor.DEFAULT_CURSOR,
				onEdt(() -> classic.getMainComponent().getCursor().getType()),
				"the default cursor is restored after the fallback");
		ownership.assertNoUncaught();
	}

	// ------------------------------------------------------------- T-X1-GEOCEDG

	static void geocedg() throws Exception {
		AppGeoCeDG product = G9U1TestApp.create();
		ExportScalePresentation presentation = product.getExportScalePresentation();
		assertNotNull(presentation);
		ActionDefinition picture = null;
		for (ActionDefinition definition : GeoCeDGProfile.getActions()) {
			if ("host.export.picture".equals(definition.target())) {
				picture = definition;
			}
		}
		assertNotNull(picture, "the GeoCeDG profile keeps its picture action");
		String id = picture.id();
		GeoCeDGActionRegistry registry =
				((GuiManagerGeoCeDG) product.getGuiManager()).getActionRegistry();
		AtomicReference<JDialog> shown = new AtomicReference<>();
		onEdt(() -> {
			Set<Window> before = exportDialogs();
			registry.invoke(id, new ActionEvent(product, ActionEvent.ACTION_PERFORMED, id));
			shown.set(newShowingDialog(before));
			return null;
		});
		assertNotNull(shown.get(), "GeoCeDG v2 opens its dialog synchronously on the EDT");
		settle();
		assertPopulated(shown.get(), product.getLocalization(), 1);
		Localization loc = product.getLocalization();
		List<String> host = hostModes(loc);
		List<String> own = new ArrayList<>();
		for (String key : new String[] {"ScaleInCentimeter", "FixedSize", "SizeInPixels"}) {
			own.add(presentation.deviceScaleLabel(key, loc.getMenu(key) + ":"));
		}
		assertEquals(own, onEdt(() -> scaleModes(shown.get())),
				"GeoCeDG keeps its own labelled device modes");
		assertNotEquals(host, own);
		cancel(shown.get(), loc, 1);
		ownership.assertEdtOnly(1);
	}

	/**
	 * T-X1-CLASSIC-NO-GEOCEDG-UI: the three host scale modes, no GeoCeDG
	 * non-physical wording or statement, no GeoCeDG component.
	 */
	private static void assertHostScaleUi(JDialog dialog, AppD classic, int cycle)
			throws Exception {
		String prefix = "cycle " + cycle + ": ";
		assertEquals(hostModes(classic.getLocalization()), onEdt(() -> scaleModes(dialog)),
				prefix + "Classic shows the three host scale modes");
		Set<String> texts = onEdt(() -> texts(dialog));
		String language = classic.getLocale().getLanguage();
		for (String key : new String[] {"ExportScale.DeviceShort",
				"ExportScale.DeviceStatement", "ExportScale.DeviceCm",
				"ExportScale.DeviceFixed", "ExportScale.DevicePixels"}) {
			String wording = GeoCeDGProfile.getText(key, language).trim();
			assertFalse(texts.contains(wording) || texts.contains(wording + ":"),
					prefix + "no GeoCeDG non-physical wording in Classic: " + wording);
		}
		assertTrue(onEdt(() -> geocedgClasses(dialog)).isEmpty(),
				prefix + "no GeoCeDG unit or drawingScale control in Classic");
	}

	private static List<String> hostModes(Localization loc) {
		return List.of(loc.getMenu("ScaleInCentimeter") + ":", loc.getMenu("FixedSize") + ":",
				loc.getMenu("SizeInPixels") + ":");
	}
	// ------------------------------------------------------------------ fixtures

	private static AppD classic() throws Exception {
		AppD classic = onEdt(() -> new AppD(new CommandLineArguments(
				new String[] {"--silent"}), new JPanel(), true) {
			@Override
			public void copyGraphicsViewToClipboard() {
				// never the system clipboard: a reached fallback is only recorded
				FALLBACK.add(SwingUtilities.isEventDispatchThread());
			}
		});
		return G9U1TestApp.withoutWindowDispatcher(classic);
	}

	/**
	 * The real Classic File menu, its items built lazily as when it is opened, and
	 * the item of the Graphics View as Picture action.
	 */
	private static JMenuItem pictureItem(AppD app) throws Exception {
		Class<?> type = Class.forName("org.geogebra.desktop.gui.menubar.FileMenuD");
		Constructor<?> constructor = type.getDeclaredConstructor(AppD.class);
		constructor.setAccessible(true);
		return onEdt(() -> {
			JMenu menu = (JMenu) constructor.newInstance(app);
			((MenuListener) menu).menuSelected(new MenuEvent(menu));
			Field field = type.getDeclaredField("exportGraphicAction");
			field.setAccessible(true);
			JMenuItem item = find(menu, field.get(menu));
			assertNotNull(item, "the File menu offers Graphics View as Picture");
			return item;
		});
	}

	static JMenuItem find(JMenu menu, Object action) {
		for (Component child : menu.getMenuComponents()) {
			if (child instanceof JMenu) {
				JMenuItem nested = find((JMenu) child, action);
				if (nested != null) {
					return nested;
				}
			} else if (child instanceof JMenuItem
					&& ((JMenuItem) child).getAction() == action) {
				return (JMenuItem) child;
			}
		}
		return null;
	}

	private static JDialog openThroughTheMenu(JMenuItem item, int cycle) throws Exception {
		AtomicReference<JDialog> shown = new AtomicReference<>();
		onEdt(() -> {
			Set<Window> before = exportDialogs();
			item.doClick(0);
			// INV-X1: built and shown inside this dispatch, not later by a worker
			shown.set(newShowingDialog(before));
			return null;
		});
		assertNotNull(shown.get(),
				"cycle " + cycle + ": the dialog is showing when the menu dispatch returns");
		return shown.get();
	}

	private static JDialog showingDialog() {
		return newShowingDialog(Set.of());
	}

	/** every export dialog that exists now, showing or not */
	static Set<Window> exportDialogs() {
		Set<Window> dialogs = Collections.newSetFromMap(new IdentityHashMap<>());
		for (Window window : Window.getWindows()) {
			if (window.getClass().getName().equals(DIALOG_CLASS)) {
				dialogs.add(window);
			}
		}
		return dialogs;
	}

	/** the showing export dialog created after {@code before} was taken */
	static JDialog newShowingDialog(Set<Window> before) {
		for (Window window : Window.getWindows()) {
			if (window.isShowing() && window.getClass().getName().equals(DIALOG_CLASS)
					&& !before.contains(window)) {
				return (JDialog) window;
			}
		}
		return null;
	}

	static void assertPopulated(JDialog dialog, Localization loc, int cycle)
			throws Exception {
		String prefix = "cycle " + cycle + ": ";
		onEdt(() -> {
			assertTrue(dialog.isShowing(), prefix + "showing");
			assertFalse(dialog.isModal(), prefix + "the dialog stays modeless");
			assertTrue(dialog.isValid(), prefix + "laid out");
			assertTrue(dialog.getContentPane().getComponentCount() > 0, prefix + "content");
			assertTrue(count(dialog, JComboBox.class) >= 3, prefix + "format, mode and dpi");
			for (String key : new String[] {"Save", "Cancel"}) {
				AbstractButton button = button(dialog, loc.getMenu(key));
				assertNotNull(button, prefix + key);
				assertTrue(button.isShowing() && button.getWidth() > 0
						&& button.getHeight() > 0, prefix + key + " is laid out");
			}
			BufferedImage image = new BufferedImage(dialog.getWidth(), dialog.getHeight(),
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g = image.createGraphics();
			dialog.getRootPane().paint(g);
			g.dispose();
			Set<Integer> colours = new HashSet<>();
			for (int y = 0; y < image.getHeight(); y += 2) {
				for (int x = 0; x < image.getWidth(); x += 2) {
					colours.add(image.getRGB(x, y));
				}
			}
			assertTrue(colours.size() > 5, prefix + "the dialog paints its content");
			return null;
		});
	}

	static void cancel(JDialog dialog, Localization loc, int cycle)
			throws Exception {
		onEdt(() -> {
			button(dialog, loc.getMenu("Cancel")).doClick(0);
			return null;
		});
		settle();
		assertFalse(onEdt(dialog::isShowing), "cycle " + cycle + ": Cancel closes the dialog");
		onEdt(() -> {
			dialog.dispose();
			return null;
		});
	}

	private static String withoutGui(String xml) {
		int start = xml.indexOf("<gui>");
		int end = xml.indexOf("</gui>");
		assertTrue(start >= 0 && end > start, "the document has one gui record");
		return xml.substring(0, start) + xml.substring(end + "</gui>".length());
	}

	static List<String> scaleModes(Container dialog) {
		for (JComboBox<?> box : combos(dialog, new ArrayList<>())) {
			List<String> items = new ArrayList<>();
			for (int i = 0; i < box.getItemCount(); i++) {
				items.add(String.valueOf(box.getItemAt(i)));
			}
			if (items.size() == 3 && box.getParent() != null && box.getParent().getClass()
					.getName().equals("org.geogebra.desktop.export.PrintScalePanel")) {
				return items;
			}
		}
		return List.of();
	}

	private static List<JComboBox<?>> combos(Container container, List<JComboBox<?>> out) {
		for (Component child : container.getComponents()) {
			if (child instanceof JComboBox) {
				out.add((JComboBox<?>) child);
			} else if (child instanceof Container) {
				combos((Container) child, out);
			}
		}
		return out;
	}

	private static Set<String> texts(Component component) {
		Set<String> out = new HashSet<>();
		if (component instanceof JLabel) {
			out.add(String.valueOf(((JLabel) component).getText()).trim());
		}
		if (component instanceof Container) {
			for (Component child : ((Container) component).getComponents()) {
				out.addAll(texts(child));
			}
		}
		return out;
	}

	private static Set<String> geocedgClasses(Container container) {
		Set<String> out = new HashSet<>();
		for (Component child : container.getComponents()) {
			if (child.getClass().getName().startsWith("org.geocedg.")) {
				out.add(child.getClass().getName());
			}
			if (child instanceof Container) {
				out.addAll(geocedgClasses((Container) child));
			}
		}
		return out;
	}

	private static int count(Container container, Class<?> type) {
		int n = 0;
		for (Component child : container.getComponents()) {
			if (type.isInstance(child)) {
				n++;
			}
			if (child instanceof Container) {
				n += count((Container) child, type);
			}
		}
		return n;
	}

	private static AbstractButton button(Container container, String text) {
		for (Component child : container.getComponents()) {
			if (child instanceof AbstractButton
					&& text.equals(((AbstractButton) child).getText())) {
				return (AbstractButton) child;
			}
			if (child instanceof Container) {
				AbstractButton nested = button((Container) child, text);
				if (nested != null) {
					return nested;
				}
			}
		}
		return null;
	}

	/** lets the EDT process the window events posted by showing or hiding */
	static void settle() throws Exception {
		for (int i = 0; i < 3; i++) {
			onEdt(() -> null);
		}
	}

	interface EdtCall<T> {
		T call() throws Exception;
	}

	static <T> T onEdt(EdtCall<T> call) throws Exception {
		if (SwingUtilities.isEventDispatchThread()) {
			return call.call();
		}
		AtomicReference<T> result = new AtomicReference<>();
		AtomicReference<Exception> failure = new AtomicReference<>();
		AtomicReference<Error> error = new AtomicReference<>();
		SwingUtilities.invokeAndWait(() -> {
			try {
				result.set(call.call());
			} catch (Exception e) {
				failure.set(e);
			} catch (Error e) {
				error.set(e);
			}
		});
		if (error.get() != null) {
			throw error.get();
		}
		if (failure.get() != null) {
			throw failure.get();
		}
		return result.get();
	}

	/**
	 * Thread-ownership recorder for the export dialog: container and hierarchy
	 * events, repaint requests and uncaught exceptions, installed on the EDT and
	 * removed after the test.
	 */
	static final class Ownership implements AutoCloseable {
		private final Map<String, AtomicInteger> edtStages = new ConcurrentHashMap<>();
		private final List<String> offEdt = Collections.synchronizedList(new ArrayList<>());
		private final List<Throwable> uncaught = Collections.synchronizedList(
				new ArrayList<>());
		private final AWTEventListener listener = this::event;
		private RepaintManager previousManager;
		private Thread.UncaughtExceptionHandler previousHandler;

		static Ownership install() throws Exception {
			Ownership ownership = new Ownership();
			onEdt(() -> {
				ownership.previousManager = RepaintManager.currentManager((JComponent) null);
				RepaintManager.setCurrentManager(new RepaintManager() {
					@Override
					public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
						ownership.repaint(c);
						super.addDirtyRegion(c, x, y, w, h);
					}
				});
				Toolkit.getDefaultToolkit().addAWTEventListener(ownership.listener,
						AWTEvent.CONTAINER_EVENT_MASK | AWTEvent.HIERARCHY_EVENT_MASK);
				return null;
			});
			ownership.previousHandler = Thread.getDefaultUncaughtExceptionHandler();
			Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
				if (thread.getName().startsWith("AWT-EventQueue")) {
					ownership.uncaught.add(error);
				}
				if (ownership.previousHandler != null) {
					ownership.previousHandler.uncaughtException(thread, error);
				}
			});
			return ownership;
		}

		private void event(AWTEvent event) {
			Window window = dialogOf((Component) event.getSource());
			if (window == null) {
				return;
			}
			if (!SwingUtilities.isEventDispatchThread()) {
				offEdt.add(event.paramString() + " on " + Thread.currentThread().getName());
				return;
			}
			for (String stage : stages(Thread.currentThread().getStackTrace())) {
				edtStages.computeIfAbsent(stage, k -> new AtomicInteger()).incrementAndGet();
			}
		}

		private void repaint(JComponent component) {
			if (!SwingUtilities.isEventDispatchThread() && dialogOf(component) != null) {
				offEdt.add("repaint of " + component.getClass().getName() + " on "
						+ Thread.currentThread().getName());
			}
		}

		private static Window dialogOf(Component component) {
			Window window = component instanceof Window ? (Window) component
					: SwingUtilities.getWindowAncestor(component);
			return window != null && window.getClass().getName().equals(DIALOG_CLASS)
					? window : null;
		}

		/** the lifecycle stages of the dialog present in a stack */
		private static Set<String> stages(StackTraceElement[] stack) {
			Set<String> stages = new HashSet<>();
			for (StackTraceElement element : stack) {
				String type = element.getClassName();
				String method = element.getMethodName();
				if (type.equals(DIALOG_CLASS)) {
					if (method.equals("<init>")) {
						stages.add("constructor");
					} else if (method.equals("initGUI")) {
						stages.add("initGUI");
					} else if (method.equals("loadPreferences")) {
						stages.add("loadPreferences");
					}
				} else if (type.equals("java.awt.Window") && method.equals("pack")) {
					stages.add("pack");
				} else if ((type.equals("java.awt.Window") || type.equals("java.awt.Dialog"))
						&& method.equals("show")) {
					stages.add("show");
				} else if (type.equals("javax.swing.SwingUtilities")
						&& method.equals("updateComponentTreeUI")) {
					stages.add("updateComponentTreeUI");
				}
			}
			return stages;
		}

		void assertNoUncaught() {
			assertEquals(List.of(), new ArrayList<>(uncaught),
					"no uncaught exception on the EDT");
		}

		/**
		 * INV-X1: no dialog event or repaint off the EDT, and every lifecycle stage
		 * observed on the EDT at least once per opened dialog.
		 */
		void assertEdtOnly(int openings) {
			assertEquals(List.of(), new ArrayList<>(offEdt),
					"no export-dialog lifecycle operation off the EDT");
			for (String stage : new String[] {"constructor", "initGUI", "pack",
					"loadPreferences", "updateComponentTreeUI", "show"}) {
				AtomicInteger seen = edtStages.get(stage);
				assertNotNull(seen, "stage observed on the EDT: " + stage);
				assertTrue(seen.get() >= openings, "stage " + stage + " on the EDT "
						+ seen.get() + " times for " + openings + " openings");
			}
			assertNoUncaught();
		}

		@Override
		public void close() throws Exception {
			onEdt(() -> {
				Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
				RepaintManager.setCurrentManager(previousManager);
				return null;
			});
			Thread.setDefaultUncaughtExceptionHandler(previousHandler);
		}
	}
}
