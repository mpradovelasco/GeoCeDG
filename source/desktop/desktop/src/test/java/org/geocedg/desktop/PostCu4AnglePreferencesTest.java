/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Documents.M;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.app;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.baseline;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.store;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.undo;
import static org.geogebra.common.kernel.Kernel.ANGLE_DEGREE;
import static org.geogebra.common.kernel.Kernel.ANGLE_DEGREES_MINUTES_SECONDS;
import static org.geogebra.common.kernel.Kernel.ANGLE_RADIANT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JRadioButton;

import org.geogebra.common.main.OptionType;
import org.geogebra.common.main.undo.UndoCommand;
import org.geogebra.desktop.gui.app.GeoGebraFrame;
import org.geogebra.desktop.gui.dialog.options.OptionsAdvancedD;
import org.geogebra.desktop.gui.view.properties.PropertiesViewD;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** I0 regression through the Desktop document transitions and actual Swing selector. */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PostCu4AnglePreferencesTest {
	private static final List<Integer> UNITS = List.of(
			ANGLE_DEGREE, ANGLE_RADIANT, ANGLE_DEGREES_MINUTES_SECONDS);
	private final List<AppGeoCeDG> preferencesHosts = new ArrayList<>();

	@TempDir
	Path dir;

	@AfterEach
	void releasePropertiesDialogPeers() throws Exception {
		onEdt(() -> {
			// The Text and Script editors in Properties realize the hidden owner frame.
			// Release these test-owned peers before the shared windowless teardown.
			for (GeoGebraFrame frame : List.copyOf(GeoGebraFrame.getInstances())) {
				if (preferencesHosts.contains(frame.getApplication())
						&& frame.isDisplayable() && !frame.isVisible()) {
					for (Window dialog : frame.getOwnedWindows()) {
						dialog.dispose();
					}
					frame.removeNotify();
				}
			}
		});
	}

	@Test
	void existingSelectorFollowsOpenAndReopenWithoutUserActions() throws Exception {
		onEdt(() -> {
			AppGeoCeDG writer = app(M);
			must(writer, "a = 1");
			AppGeoCeDG host = app(M);
			host.setUndoActive(true);
			host.getKernel().setAngleUnit(ANGLE_RADIANT);
			var radios = radios(global(host));
			var actions = countActions(radios);

			for (String extension : List.of("cedg", "ggb")) {
				var files = documents(writer, extension);
				for (int mode : List.of(ANGLE_DEGREE, ANGLE_RADIANT,
						ANGLE_DEGREES_MINUTES_SECONDS, ANGLE_DEGREE)) {
					assertTrue(host.getGuiManager().loadFile(files.get(mode), false));
					assertMode(host, radios, mode);
					assertTrue(host.isSaved());
					assertFalse(host.getKernel().undoPossible(), "Open establishes a baseline");
				}
			}
			assertEquals(0, actions.get());
			return null;
		});
	}

	@Test
	void firstGlobalPanelReadsOpenedDocumentWithoutEagerPropertiesCreation() throws Exception {
		onEdt(() -> {
			AppGeoCeDG writer = app(M);
			must(writer, "a = 1");
			var files = documents(writer, "cedg");
			for (int mode : UNITS) {
				AppGeoCeDG host = app(M);
				assertFalse(host.getGuiManager().hasPropertiesView());
				assertTrue(host.getGuiManager().loadFile(files.get(mode), false));
				assertFalse(host.getGuiManager().hasPropertiesView());
				assertMode(host, radios(global(host)), mode);
			}
			return null;
		});
	}

	@Test
	void showingOrReselectingGlobalOnlyChangesTheSelector() throws Exception {
		onEdt(() -> {
			AppGeoCeDG host = app(M);
			host.setUndoActive(true);
			host.getKernel().setAngleUnit(ANGLE_DEGREE);
			must(host, "a = 1");
			baseline(host);
			var panel = global(host);
			var radios = radios(panel);
			var actions = countActions(radios);
			assertMode(host, radios, ANGLE_DEGREE);

			panel.setSelectedOptionPanelVisible(false);
			host.getKernel().setAngleUnit(ANGLE_RADIANT);
			Snapshot saved = Snapshot.capture(host);
			panel.setSelectedOptionPanelVisible(true);
			assertMode(host, radios, ANGLE_RADIANT);
			assertEquals(saved, Snapshot.capture(host));

			host.getKernel().setAngleUnit(ANGLE_DEGREES_MINUTES_SECONDS);
			host.setUnsaved();
			Snapshot dirty = Snapshot.capture(host);
			panel.setOptionPanel(OptionType.GLOBAL);
			assertMode(host, radios, ANGLE_DEGREES_MINUTES_SECONDS);
			assertEquals(dirty, Snapshot.capture(host));
			assertEquals(0, actions.get());
			return null;
		});
	}

	@Test
	void undoAndRedoFollowHistoryWithoutAddingAnAngleSelection() throws Exception {
		onEdt(() -> {
			AppGeoCeDG host = app(M);
			host.setUndoActive(true);
			host.getKernel().setAngleUnit(ANGLE_DEGREE);
			must(host, "a = 1");
			baseline(host);
			var radios = radios(global(host));
			var actions = countActions(radios);
			var history = undo(host);
			int start = history.getCursor();
			var initialCommands = history.getCommands();

			radios.get(ANGLE_RADIANT).doClick(0);
			assertMode(host, radios, ANGLE_RADIANT);
			assertFalse(host.isSaved());
			assertEquals(start, history.getCursor());
			assertEquals(initialCommands, history.getCommands());
			store(host);
			radios.get(ANGLE_DEGREES_MINUTES_SECONDS).doClick(0);
			store(host);
			var commands = history.getCommands();
			assertEquals(start + 2, history.getCursor());

			host.getGuiManager().undo();
			assertMode(host, radios, ANGLE_RADIANT);
			assertEquals(start + 1, history.getCursor());
			host.getGgbApi().undo();
			assertMode(host, radios, ANGLE_DEGREE);
			assertEquals(start, history.getCursor());
			host.getGgbApi().redo();
			assertMode(host, radios, ANGLE_RADIANT);
			assertEquals(start + 1, history.getCursor());
			host.getGuiManager().redo();
			assertMode(host, radios, ANGLE_DEGREES_MINUTES_SECONDS);
			assertEquals(start + 2, history.getCursor());
			assertEquals(commands, history.getCommands());
			assertEquals(2, actions.get(), "only the two explicit clicks dispatch actions");
			return null;
		});
	}

	private Map<Integer, File> documents(AppGeoCeDG writer, String extension) throws IOException {
		Map<Integer, File> files = new LinkedHashMap<>();
		for (int mode : UNITS) {
			writer.getKernel().setAngleUnit(mode);
			Path file = dir.resolve("angle-" + mode + "." + extension);
			if ("cedg".equals(extension)) {
				assertTrue(writer.saveGeoGebraFile(file.toFile()));
			} else {
				try (var stream = Files.newOutputStream(file)) {
					writer.getXMLio().writeGeoGebraFile(stream, false);
				}
			}
			files.put(mode, file.toFile());
		}
		return files;
	}

	private PropertiesViewD global(AppGeoCeDG host) {
		preferencesHosts.add(host);
		var panel = (PropertiesViewD) host.getGuiManager().getPropertiesView();
		panel.setOptionPanel(OptionType.GLOBAL);
		return panel;
	}

	private static Map<Integer, JRadioButton> radios(PropertiesViewD view)
			throws ReflectiveOperationException {
		var field = PropertiesViewD.class.getDeclaredField("advancedPanel");
		field.setAccessible(true);
		var options = (OptionsAdvancedD) field.get(view);
		return Map.of(
				ANGLE_DEGREE, radio(options, "angleUnitRadioDegree"),
				ANGLE_RADIANT, radio(options, "angleUnitRadioRadian"),
				ANGLE_DEGREES_MINUTES_SECONDS,
				radio(options, "angleUnitRadioDegreesMinutesSeconds"));
	}

	private static JRadioButton radio(OptionsAdvancedD options, String name)
			throws ReflectiveOperationException {
		var field = OptionsAdvancedD.class.getDeclaredField(name);
		field.setAccessible(true);
		return (JRadioButton) field.get(options);
	}

	private static AtomicInteger countActions(Map<Integer, JRadioButton> radios) {
		var actions = new AtomicInteger();
		radios.values().forEach(button -> button.addActionListener(event ->
				actions.incrementAndGet()));
		return actions;
	}

	private static void assertMode(AppGeoCeDG host, Map<Integer, JRadioButton> radios, int mode) {
		assertEquals(mode, host.getKernel().getAngleUnit());
		for (int unit : UNITS) {
			assertEquals(mode == unit, radios.get(unit).isSelected(), "radio for unit " + unit);
		}
	}

	private record Snapshot(String xml, boolean saved, File file, int cursor,
			List<UndoCommand> commands) {
		static Snapshot capture(AppGeoCeDG host) {
			var history = undo(host);
			return new Snapshot(host.getXML(), host.isSaved(), host.getCurrentFile(),
					history.getCursor(), history.getCommands());
		}
	}
}
