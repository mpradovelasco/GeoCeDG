/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.text.JTextComponent;

import org.geogebra.common.main.App;
import org.geogebra.desktop.GeoGebra3D;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.gui.app.GeoGebraFrame;
import org.geogebra.desktop.main.AppD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * PRE-G9B-R6-plus-C-X1 T-X1-PROCESS: the real Classic application, as the
 * GeoCeDG diagnostic launches it ({@code GeoGebra3D.main} with its own settings
 * file), in a separate windowed JVM. File > Export > Graphics View as Picture is
 * clicked through the real menu bar, then reached through Ctrl+Shift+U from a
 * text field (the menu accelerator) and from the graphics view (the global
 * dispatcher). Every opening must be built and shown inside its EDT dispatch,
 * with no lifecycle operation of the dialog off the EDT, a populated dialog and
 * a working Cancel (INV-X1).
 *
 * <p>The child JVM runs {@code GeoGebra3D.main} on the EDT: the Classic startup
 * itself runs off the EDT in the product, an independent pre-existing defect
 * (DQ-X1-4) that must not perturb this test. Inside the child, the host
 * clipboard fallback is replaced by a recorder, so no run can touch the system
 * clipboard. The child halts itself; the parent kills it after a bounded wait.
 */
class PreG9BR6PlusCX1ClassicPictureProcessTest {

	private static final int MENU_CYCLES = 5;
	private static final int TEXT_ACCELERATOR_CYCLES = 2;
	private static final int VIEW_ACCELERATOR_CYCLES = 1;
	private static final long CHILD_TIMEOUT_SECONDS = 240;

	@TempDir
	Path temporary;

	@Test
	void classicPictureDialogIsBuiltAndShownOnTheEdtInTheRealApplication()
			throws Exception {
		String classPath = System.getProperty("java.class.path");
		String agent = null;
		for (String entry : classPath.split(File.pathSeparator)) {
			if (entry.contains("byte-buddy-agent")) {
				agent = entry;
			}
		}
		assertNotNull(agent, "the byte-buddy agent of the test classpath sandboxes the"
				+ " clipboard of the child");
		Path out = temporary.resolve("result.txt");
		Path console = temporary.resolve("console.txt");
		List<String> command = new ArrayList<>(List.of(
				ProcessHandle.current().info().command().orElseThrow(),
				"-javaagent:" + agent, "-cp", classPath,
				"--add-exports", "java.base/java.lang=ALL-UNNAMED",
				"--add-exports", "java.desktop/sun.awt=ALL-UNNAMED",
				"--add-exports", "java.desktop/sun.java2d=ALL-UNNAMED",
				Driver.class.getName(),
				temporary.resolve("classic-x1.properties").toString(), out.toString()));
		Process process = new ProcessBuilder(command).redirectErrorStream(true)
				.redirectOutput(console.toFile()).start();
		boolean ended = process.waitFor(CHILD_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		if (!ended) {
			process.destroyForcibly();
		}
		String record = Files.exists(out) ? Files.readString(out) : "(no record)";
		String detail = record + "\n--- console (tail) ---\n" + tail(console);
		// the child record is kept in the JUnit report as evidence
		System.out.println("C-X1 child record:\n" + record);
		assertTrue(ended, "the child ended within " + CHILD_TIMEOUT_SECONDS + " s\n"
				+ detail);
		assertEquals(0, process.exitValue(), "child exit code\n" + detail);
		assertTrue(record.contains("RESULT ok=true"), detail);
		assertTrue(record.contains("openings=" + (MENU_CYCLES + TEXT_ACCELERATOR_CYCLES
				+ VIEW_ACCELERATOR_CYCLES)), detail);
		assertTrue(record.contains("clipboardFallback=0"), detail);
		assertFalse(record.contains("FAILURE"), detail);
	}

	private static String tail(Path file) throws Exception {
		if (!Files.exists(file)) {
			return "(none)";
		}
		List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
		return String.join("\n", lines.subList(Math.max(0, lines.size() - 40),
				lines.size()));
	}

	/** Records and skips the host clipboard fallback inside the child JVM only. */
	public static final class ClipboardFallback {
		/** fallback calls recorded instead of copying */
		public static final AtomicInteger CALLS = new AtomicInteger();

		private ClipboardFallback() {
		}

		/**
		 * Inlined advice: counts the call and skips the original body.
		 *
		 * @return true to skip the clipboard copy
		 */
		@Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
		public static boolean enter() {
			CALLS.incrementAndGet();
			return true;
		}
	}

	/** Main class of the isolated Classic JVM. */
	public static final class Driver {
		private static final List<String> LOG = Collections.synchronizedList(
				new ArrayList<>());
		private static final AtomicLong PONG = new AtomicLong(System.nanoTime());
		private static volatile AppD app;

		private Driver() {
		}

		/**
		 * @param args settings file, result file
		 * @throws Exception on a harness failure
		 */
		public static void main(String[] args) throws Exception {
			Path out = Paths.get(args[1]);
			new AgentBuilder.Default()
					.disableClassFormatChanges()
					.with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
					.type(ElementMatchers.named("org.geogebra.desktop.main.AppD"))
					.transform((builder, type, loader, module, domain) -> builder.visit(
							Advice.to(ClipboardFallback.class).on(ElementMatchers
									.named("copyGraphicsViewToClipboard")
									.or(ElementMatchers.named("simpleExportToClipboard")))))
					.installOn(ByteBuddyAgent.getInstrumentation());
			watchdog(out);
			int openings = 0;
			PreG9BR6PlusCX1ClassicPictureDialogTest.Ownership ownership = null;
			try {
				// startup on the EDT; the product's own startup thread is out of scope
				SwingUtilities.invokeAndWait(() -> GeoGebra3D.main(new String[] {
						"--showSplash=false", "--settingsfile=" + args[0], "--language=en"}));
				app = waitForApplication();
				PreG9BR6PlusCX1ClassicPictureDialogTest.settle();
				ownership = PreG9BR6PlusCX1ClassicPictureDialogTest.Ownership.install();
				JMenuItem item = menuItem();
				for (int cycle = 1; cycle <= MENU_CYCLES; cycle++) {
					// the real item of the real menu bar; its click fires the same action
					// listeners as a mouse release on the open menu
					openings += cycle(cycle, "menu", () -> item.doClick(0));
				}
				for (int cycle = 1; cycle <= TEXT_ACCELERATOR_CYCLES; cycle++) {
					openings += cycle(cycle, "accelerator-from-text", () -> {
						KeyEvent key = shortcut(firstText(app.getFrame().getContentPane()));
						log("dispatcher consumed=" + app.dispatchKeyEvent(key));
						log("key bindings consumed=" + SwingUtilities.processKeyBindings(key));
					});
				}
				for (int cycle = 1; cycle <= VIEW_ACCELERATOR_CYCLES; cycle++) {
					openings += cycle(cycle, "accelerator-from-view", () -> {
						KeyEvent key = shortcut(((EuclidianViewD) app.getActiveEuclidianView())
								.getJPanel());
						log("dispatcher consumed=" + app.dispatchKeyEvent(key));
					});
				}
				ownership.assertEdtOnly(openings);
				LOG.add("RESULT ok=true openings=" + openings + " clipboardFallback="
						+ ClipboardFallback.CALLS.get());
			} catch (Throwable failure) {
				LOG.add("FAILURE " + failure);
				for (StackTraceElement element : failure.getStackTrace()) {
					LOG.add("    at " + element);
				}
				LOG.add("RESULT ok=false openings=" + openings + " clipboardFallback="
						+ ClipboardFallback.CALLS.get());
			}
			write(out);
			Runtime.getRuntime().halt(0);
		}

		interface EdtAction {
			void run() throws Exception;
		}

		private static int cycle(int cycle, String route, EdtAction action)
				throws Exception {
			AtomicReference<JDialog> shown = new AtomicReference<>();
			PreG9BR6PlusCX1ClassicPictureDialogTest.onEdt(() -> {
				Set<Window> before = PreG9BR6PlusCX1ClassicPictureDialogTest.exportDialogs();
				action.run();
				shown.set(PreG9BR6PlusCX1ClassicPictureDialogTest.newShowingDialog(before));
				return null;
			});
			assertNotNull(shown.get(), route + " cycle " + cycle
					+ ": the dialog is showing when the dispatch returns");
			PreG9BR6PlusCX1ClassicPictureDialogTest.settle();
			PreG9BR6PlusCX1ClassicPictureDialogTest.assertPopulated(shown.get(),
					app.getLocalization(), cycle);
			assertEquals(List.of(app.getLocalization().getMenu("ScaleInCentimeter") + ":",
					app.getLocalization().getMenu("FixedSize") + ":",
					app.getLocalization().getMenu("SizeInPixels") + ":"),
					PreG9BR6PlusCX1ClassicPictureDialogTest.onEdt(
							() -> PreG9BR6PlusCX1ClassicPictureDialogTest.scaleModes(
									shown.get())),
					"Classic keeps the host scale modes");
			PreG9BR6PlusCX1ClassicPictureDialogTest.cancel(shown.get(),
					app.getLocalization(), cycle);
			log(route + " cycle " + cycle + " populated and cancelled");
			return 1;
		}

		/** the real File menu of the Classic frame, built as when it is opened */
		private static JMenuItem menuItem() throws Exception {
			return PreG9BR6PlusCX1ClassicPictureDialogTest.onEdt(() -> {
				JMenuBar bar = app.getFrame().getJMenuBar();
				JMenu file = bar.getMenu(0);
				file.doClick(0);
				for (MenuListener listener : file.getMenuListeners()) {
					listener.menuSelected(new MenuEvent(file));
				}
				Field field = file.getClass().getDeclaredField("exportGraphicAction");
				field.setAccessible(true);
				JMenuItem item = PreG9BR6PlusCX1ClassicPictureDialogTest.find(file,
						field.get(file));
				assertNotNull(item, "the Classic File menu offers Graphics View as Picture");
				return item;
			});
		}

		private static KeyEvent shortcut(Component source) {
			assertNotNull(source, "a key source");
			return new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
					InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, KeyEvent.VK_U,
					KeyEvent.CHAR_UNDEFINED);
		}

		private static JTextComponent firstText(Container container) {
			for (Component child : container.getComponents()) {
				if (child instanceof JTextComponent && child.isShowing()) {
					return (JTextComponent) child;
				}
				if (child instanceof Container) {
					JTextComponent nested = firstText((Container) child);
					if (nested != null) {
						return nested;
					}
				}
			}
			return null;
		}

		private static AppD waitForApplication() throws Exception {
			Field initing = App.class.getDeclaredField("initing");
			initing.setAccessible(true);
			for (int i = 0; i < 1200; i++) {
				AtomicReference<AppD> found = new AtomicReference<>();
				SwingUtilities.invokeAndWait(() -> {
					for (GeoGebraFrame frame : GeoGebraFrame.getInstances()) {
						if (frame.isShowing()) {
							found.set(frame.getApplication());
						}
					}
				});
				if (found.get() != null && !(Boolean) initing.get(found.get())) {
					return found.get();
				}
				Thread.sleep(100);
			}
			throw new AssertionError("the Classic window did not start");
		}

		private static void log(String text) {
			LOG.add(text);
		}

		/** EDT liveness: a stall of 30 s writes a lock-aware thread dump and halts */
		private static void watchdog(Path out) {
			Thread pinger = new Thread(() -> {
				while (true) {
					SwingUtilities.invokeLater(() -> PONG.set(System.nanoTime()));
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						return;
					}
				}
			}, "x1-pinger");
			pinger.setDaemon(true);
			pinger.start();
			Thread watchdog = new Thread(() -> {
				long start = System.nanoTime();
				while (true) {
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						return;
					}
					long stale = (System.nanoTime() - PONG.get()) / 1_000_000;
					long total = (System.nanoTime() - start) / 1_000_000;
					if (stale > 30_000 || total > 200_000) {
						LOG.add("FAILURE watchdog: EDT stale " + stale + " ms after " + total
								+ " ms");
						for (ThreadInfo info : ManagementFactory.getThreadMXBean()
								.dumpAllThreads(true, true)) {
							LOG.add(info.toString());
						}
						LOG.add("RESULT ok=false");
						write(out);
						Runtime.getRuntime().halt(3);
					}
				}
			}, "x1-watchdog");
			watchdog.setDaemon(true);
			watchdog.start();
		}

		private static synchronized void write(Path out) {
			try {
				List<String> copy;
				synchronized (LOG) {
					copy = new ArrayList<>(LOG);
				}
				Files.write(out, String.join("\n", copy).getBytes(StandardCharsets.UTF_8));
			} catch (Exception ignored) {
				// the parent reports the missing record
			}
		}
	}
}
