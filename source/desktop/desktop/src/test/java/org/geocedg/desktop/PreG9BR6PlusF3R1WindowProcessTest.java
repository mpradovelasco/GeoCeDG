/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.app.GeoGebraFrame;
import org.geogebra.desktop.gui.menubar.LoadFileListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-R6-plus-F3-R1 shown-window regression (F3R1-E1): the real product window
 * started by {@code GeoCeDG.main} in a child JVM performs an Insert File, Open Recent of
 * the inserted source and New, then closes through its real window-closing route. The
 * process must end by itself; on the original F3 candidate the hidden insertion helpers
 * stayed registered as application windows, so closing the last visible window never
 * reached the application exit.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusF3R1WindowProcessTest {
	private static final String PREFIX = "F3R1|";

	@TempDir
	Path dir;

	/** Child-process scenario: target, source, then the real window close. */
	public static final class Driver {
		private Driver() {
		}

		private static void report(String key, Object value) {
			System.out.println(PREFIX + key + "|" + value);
			System.out.flush();
		}

		private static <T> T edt(Callable<T> action) throws Exception {
			AtomicReference<T> result = new AtomicReference<>();
			AtomicReference<Exception> failure = new AtomicReference<>();
			CountDownLatch done = new CountDownLatch(1);
			SwingUtilities.invokeLater(() -> {
				try {
					result.set(action.call());
				} catch (Exception thrown) {
					failure.set(thrown);
				} finally {
					done.countDown();
				}
			});
			if (!done.await(90, TimeUnit.SECONDS)) {
				report("error", "EDT step timed out");
				Runtime.getRuntime().halt(4);
			}
			if (failure.get() != null) {
				throw failure.get();
			}
			return result.get();
		}

		/**
		 * @param args settings file, target document, source document
		 * @throws Exception on a failed step
		 */
		public static void main(String[] args) throws Exception {
			File target = new File(args[1]);
			File source = new File(args[2]);
			GeoCeDG.main(new String[] {"--settingsfile=" + args[0], "--language=en"});
			GeoGebraFrame frame = null;
			long deadline = System.currentTimeMillis() + 90000;
			while (frame == null && System.currentTimeMillis() < deadline) {
				if (GeoGebraFrame.getInstanceCount() > 0) {
					GeoGebraFrame first = GeoGebraFrame.getInstances().get(0);
					if (first.isShowing() && first.getApplication() != null
							&& !first.getApplication().isIniting()) {
						frame = first;
					}
				}
				Thread.sleep(100);
			}
			if (frame == null) {
				report("error", "no window");
				Runtime.getRuntime().halt(5);
			}
			GeoGebraFrame window = frame;
			AppGeoCeDG app = (AppGeoCeDG) window.getApplication();
			app.setInsertionMessageSink(message -> report("message", message));
			edt(() -> ((GuiManagerD) app.getGuiManager()).loadFile(target, false));
			report("framesBefore", GeoGebraFrame.getInstanceCount());
			GeoCeDGDocumentInsertion.Result result = edt(
					() -> app.getDocumentInsertion().insert(source));
			report("outcome", result.getOutcome());
			report("framesAfter", GeoGebraFrame.getInstanceCount());
			edt(() -> {
				app.setSaved();
				new LoadFileListener(app, source).actionPerformed(null);
				return null;
			});
			report("openRecentLoaded", source.getCanonicalFile().equals(
					app.getCurrentFile() == null ? null : app.getCurrentFile().getCanonicalFile()));
			edt(() -> {
				app.setSaved();
				app.fileNew();
				return null;
			});
			report("titleAfterNew", edt(window::getTitle));
			report("framesBeforeClose", GeoGebraFrame.getInstanceCount());
			Thread watchdog = new Thread(() -> {
				try {
					Thread.sleep(30000);
				} catch (InterruptedException interrupted) {
					return;
				}
				List<String> alive = new ArrayList<>();
				for (Thread t : Thread.getAllStackTraces().keySet()) {
					if (t.isAlive() && !t.isDaemon()) {
						alive.add(t.getName());
					}
				}
				report("stillAlive", "registered windows " + GeoGebraFrame.getInstanceCount()
						+ ", non-daemon threads " + alive);
				Runtime.getRuntime().halt(3);
			}, "f3r1-exit-watchdog");
			watchdog.setDaemon(true);
			watchdog.start();
			report("closing", "WINDOW_CLOSING");
			SwingUtilities.invokeLater(() -> window.dispatchEvent(
					new WindowEvent(window, WindowEvent.WINDOW_CLOSING)));
		}
	}

	private Map<String, String> runChild(File target, File source) throws Exception {
		String java = ProcessHandle.current().info().command().orElse("java");
		List<String> command = new ArrayList<>();
		command.add(java);
		for (String argument : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
			if (!argument.startsWith("-agentlib") && !argument.startsWith("-javaagent")
					&& !argument.startsWith("-Djava.security.manager")) {
				command.add(argument);
			}
		}
		command.add("-cp");
		command.add(System.getProperty("java.class.path"));
		command.add(Driver.class.getName());
		command.add(dir.resolve("settings-" + target.getName() + ".properties").toString());
		command.add(target.getAbsolutePath());
		command.add(source.getAbsolutePath());
		Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
		Map<String, String> report = new LinkedHashMap<>();
		StringBuilder transcript = new StringBuilder();
		Thread reader = new Thread(() -> {
			try (BufferedReader in = new BufferedReader(new InputStreamReader(
					process.getInputStream(), StandardCharsets.UTF_8))) {
				String line;
				while ((line = in.readLine()) != null) {
					transcript.append(line).append('\n');
					if (line.startsWith(PREFIX)) {
						String[] parts = line.split("\\|", 3);
						synchronized (report) {
							report.put(parts[1], parts.length > 2 ? parts[2] : "");
						}
					}
				}
			} catch (java.io.IOException ignored) {
				// process ended
			}
		});
		reader.start();
		boolean ended = process.waitFor(240, TimeUnit.SECONDS);
		if (!ended) {
			process.destroyForcibly();
		}
		reader.join(10000);
		synchronized (report) {
			report.put("ended", String.valueOf(ended));
			report.put("exitCode", ended ? String.valueOf(process.exitValue()) : "-");
			report.put("transcriptTail", transcript.length() > 3000
					? transcript.substring(transcript.length() - 3000) : transcript.toString());
			return report;
		}
	}

	private static void assertCoherentSession(Map<String, String> report) {
		assertEquals("true", report.get("ended"), "the process ended by itself: " + report);
		assertEquals("0", report.get("exitCode"), "normal exit after the real window close: "
				+ report);
		assertFalse(report.containsKey("stillAlive"), String.valueOf(report.get("stillAlive")));
		assertEquals(report.get("framesBefore"), report.get("framesAfter"),
				"no hidden window registered by the insertion: " + report);
		assertEquals("true", report.get("openRecentLoaded"), "Open Recent loads: " + report);
		assertFalse(report.get("titleAfterNew").contains("("), "title " + report.get(
				"titleAfterNew"));
		assertEquals("1", report.get("framesBeforeClose"));
	}

	@Test
	void f3r1e1InsertingLocusV2IntoCircleMKeepsOneWindowAndTheProcessExits()
			throws Exception {
		File circle = PreG9BR6PlusF3R1LifecycleTest.circleM(dir);
		File locus = PreG9BR6PlusF3R1LifecycleTest.locusV2(dir);
		Map<String, String> report = runChild(circle, locus);
		assertCoherentSession(report);
		assertEquals("INSERTED", report.get("outcome"), report.toString());
	}

	@Test
	void f3r1e2InsertingCircleMIntoLocusV2KeepsOneWindowAndTheProcessExits()
			throws Exception {
		File circle = PreG9BR6PlusF3R1LifecycleTest.circleM(dir);
		File locus = PreG9BR6PlusF3R1LifecycleTest.locusV2(dir);
		Map<String, String> report = runChild(locus, circle);
		assertCoherentSession(report);
		assertTrue(report.get("outcome").startsWith("INSERTED"), report.toString());
	}
}
