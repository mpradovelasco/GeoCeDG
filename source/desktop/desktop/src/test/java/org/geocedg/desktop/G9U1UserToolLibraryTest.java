/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.awt.Color;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;

import org.geocedg.desktop.GeoCeDGUserToolLibrary.Package;
import org.geogebra.common.io.XMLParseException;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.move.ggtapi.models.json.JSONArray;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.io.MyXMLioD;
import org.geogebra.desktop.main.AppD;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Self-authored native macro fixtures; never reads or writes real user preferences. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class G9U1UserToolLibraryTest {

	@TempDir
	Path temporary;
	private AppGeoCeDG app;
	private GeoCeDGUserToolLibrary library;
	private Path storage;

	@BeforeEach
	void setup() throws IOException {
		app = G9U1TestApp.create();
		storage = temporary.resolve("tools.json");
		library = new GeoCeDGUserToolLibrary(app, storage);
	}

	@Test
	void installDoesNotChangeConstructionOrDocumentArchive() throws Exception {
		G9U1TestApp.eval(app, "P=(3,4)");
		String before = app.getXML();
		byte[] beforeArchive = archiveXml(app);
		Package tool = library.install("owned.ggt", midpointPackage("OwnedMidpoint"));
		assertEquals(List.of("OwnedMidpoint"), tool.commands());
		assertEquals(before, app.getXML());
		assertArrayEquals(beforeArchive, archiveXml(app));
		assertEquals(0, app.getKernel().getMacroNumber());
		assertTrue(Files.exists(storage));
	}

	@Test
	void restartAndNewDocumentRetainLibraryWithoutRegisteringDocumentMacros() throws Exception {
		Package tool = library.install("owned.ggt", midpointPackage("OwnedMidpoint"));
		AppGeoCeDG restarted = G9U1TestApp.create();
		GeoCeDGUserToolLibrary reloaded = new GeoCeDGUserToolLibrary(restarted, storage);
		assertEquals(tool.id(), reloaded.packages().get(0).id());
		assertEquals(0, restarted.getKernel().getMacroNumber());
		restarted.getKernel().clearConstruction(true);
		assertEquals(tool.id(), reloaded.packages().get(0).id());
		assertEquals(0, restarted.getKernel().getMacroNumber());
	}

	@Test
	void explicitActivationUsesExistingMacroEngineAndDynamicInputs() throws Exception {
		Package tool = library.install("owned.ggt", midpointPackage("OwnedMidpoint"));
		final GeoPoint p = (GeoPoint) G9U1TestApp.eval(app, "P=(4,2)");
		final GeoPoint q = (GeoPoint) G9U1TestApp.eval(app, "Q=(8,6)");
		String constructionBefore = app.getXML();
		Macro macro = library.activate(tool.id(), "OwnedMidpoint");
		// Host ToolManager edits/removes via macro.getKernel(), not its registration lookup.
		assertSame(app.getKernel(), macro.getKernel());
		assertSame(macro, library.activate(tool.id(), "OwnedMidpoint"));
		assertEquals(constructionBefore, app.getXML());
		// The same owner used by the inherited ToolManager removes the actual registration.
		macro.getKernel().removeMacro(macro);
		assertNull(app.getKernel().getMacro("OwnedMidpoint"));
		macro = library.activate(tool.id(), "OwnedMidpoint");
		assertSame(app.getKernel(), macro.getKernel());
		assertEquals(1, app.getKernel().getMacroNumber());
		GeoElement[] output = app.getKernel().useMacro(new String[] {"R"}, macro,
				new GeoElement[] {p, q});
		assertNotNull(output);
		GeoPoint result = (GeoPoint) output[0];
		assertEquals(6, result.getInhomX(), 1e-12);
		assertEquals(4, result.getInhomY(), 1e-12);
		q.setCoords(12, 10, 1);
		q.updateRepaint();
		assertEquals(8, result.getInhomX(), 1e-12);
		assertEquals(6, result.getInhomY(), 1e-12);
	}

	@Test
	void installedToolIsInvocableThroughNativeCommandDispatcher() throws Exception {
		Package tool = library.install("owned.ggt", midpointPackage("OwnedMidpoint"));
		library.activate(tool.id(), "OwnedMidpoint");
		G9U1TestApp.eval(app, "P=(2,0)");
		G9U1TestApp.eval(app, "Q=(6,4)");
		GeoPoint result = (GeoPoint) G9U1TestApp.eval(app, "R=OwnedMidpoint(P,Q)");
		assertEquals(4, result.getInhomX(), 1e-12);
		assertEquals(2, result.getInhomY(), 1e-12);
	}

	@Test
	void pinsAreApplicationPreferencesAndDoNotChangeConstruction() throws Exception {
		Package tool = library.install("owned.ggt", midpointPackage("OwnedMidpoint"));
		String before = app.getXML();
		library.pin(tool.id(), "OwnedMidpoint", true);
		assertTrue(new GeoCeDGUserToolLibrary(app, storage).packages().get(0)
				.isPinned("OwnedMidpoint"));
		library.pin(tool.id(), "OwnedMidpoint", false);
		assertFalse(new GeoCeDGUserToolLibrary(app, storage).packages().get(0)
				.isPinned("OwnedMidpoint"));
		assertEquals(before, app.getXML());
	}

	@Test
	void pinLayoutPersistsOrderAndGroupsWithoutChangingDocument() throws Exception {
		Package tool = library.install("owned.ggt",
				midpointPackage("FirstMidpoint", "SecondMidpoint", "ThirdMidpoint"));
		G9U1TestApp.eval(app, "P=(3,4)");
		final String before = app.getXML();
		final int steps = app.getKernel().getConstruction().steps();
		final boolean undo = app.getKernel().getConstruction().getUndoManager().undoPossible();
		library.pin(tool.id(), "FirstMidpoint", true);
		library.pin(tool.id(), "SecondMidpoint", true);
		library.pin(tool.id(), "ThirdMidpoint", true);
		library.setPinGroup(tool.id(), "FirstMidpoint", "Bisectors");
		library.setPinGroup(tool.id(), "SecondMidpoint", "Bisectors");
		library.movePinned(tool.id(), "SecondMidpoint", -1);

		GeoCeDGUserToolLibrary reopened = new GeoCeDGUserToolLibrary(app, storage);
		assertEquals(List.of("SecondMidpoint", "FirstMidpoint", "ThirdMidpoint"),
				reopened.pinnedCommands().stream()
						.map(GeoCeDGUserToolLibrary.PinnedCommand::command).toList());
		assertEquals(List.of("Bisectors", "Bisectors", ""), reopened.pinnedCommands()
				.stream().map(GeoCeDGUserToolLibrary.PinnedCommand::group).toList());
		assertEquals(3, new JSONObject(Files.readString(storage)).getInt("version"));
		assertEquals(before, app.getXML());
		assertEquals(steps, app.getKernel().getConstruction().steps());
		assertEquals(undo, app.getKernel().getConstruction().getUndoManager().undoPossible());
		assertEquals(0, app.getKernel().getMacroNumber());

		JPanel pins = new JPanel();
		new GeoCeDGUserTools(app, reopened).populatePins(pins);
		assertEquals(2, pins.getComponentCount());
		assertEquals("Bisectors", ((JToggleButton) pins.getComponent(0))
				.getClientProperty("geocedg.userTool.group"));
		assertEquals(2, ((JToggleButton) pins.getComponent(0))
				.getClientProperty("geocedg.userTool.groupSize"));
		assertEquals("ThirdMidpoint", ((JToggleButton) pins.getComponent(1))
				.getClientProperty("geocedg.userTool.command"));
	}

	@Test
	void iconlessPinnedButtonsAndGroupsStayCompactWithFullAccessibleIdentity()
			throws Exception {
		assertEquals("A", GeoCeDGUserTools.monogram("  --alpha"));
		assertEquals("A", GeoCeDGUserTools.monogram("  7 axis"));
		assertEquals("\u2022", GeoCeDGUserTools.monogram(" -- "));
		Package tool = library.install("owned.ggt",
				midpointPackage("FirstMidpoint", "SecondMidpoint", "ThirdMidpoint"));
		library.pin(tool.id(), "FirstMidpoint", true);
		library.pin(tool.id(), "SecondMidpoint", true);
		library.pin(tool.id(), "ThirdMidpoint", true);
		library.setPinGroup(tool.id(), "FirstMidpoint", "Bisectors");
		library.setPinGroup(tool.id(), "SecondMidpoint", "Bisectors");

		JPanel toolbar = new JPanel();
		new GeoCeDGUserTools(app, library).populatePins(toolbar);
		assertEquals(2, toolbar.getComponentCount());
		JToggleButton group = (JToggleButton) toolbar.getComponent(0);
		JToggleButton command = (JToggleButton) toolbar.getComponent(1);
		for (JToggleButton button : List.of(group, command)) {
			assertNotNull(button.getIcon());
			assertNull(button.getText());
			assertEquals(app.getScaledIconSize() + 12,
					button.getPreferredSize().width);
			assertEquals(button.getPreferredSize().width,
					button.getPreferredSize().height);
			assertEquals(button.getPreferredSize(), button.getMinimumSize());
			assertEquals(button.getPreferredSize(), button.getMaximumSize());
			assertEquals(button.getToolTipText(),
					button.getAccessibleContext().getAccessibleDescription());
		}
		assertEquals("monogram",
				group.getClientProperty("geocedg.userTool.icon.source"));
		assertEquals("F", group.getClientProperty("geocedg.userTool.monogram"));
		assertEquals("FirstMidpoint",
				group.getClientProperty("geocedg.userTool.activeCommand"));
		assertEquals("Bisectors", group.getAccessibleContext().getAccessibleName());
		assertTrue(group.getToolTipText().contains("Bisectors"));
		assertTrue(group.getToolTipText().contains("FirstMidpoint"));
		assertTrue(group.getToolTipText().contains("SecondMidpoint"));
		JPopupMenu popup = (JPopupMenu) group.getClientProperty("geocedg.userTool.popup");
		for (int i = 0; i < popup.getComponentCount(); i++) {
			JMenuItem item = (JMenuItem) popup.getComponent(i);
			assertEquals(item.getText(), item.getAccessibleContext().getAccessibleName());
			assertEquals(item.getToolTipText(),
					item.getAccessibleContext().getAccessibleDescription());
			assertNotNull(item.getIcon());
		}
		((JMenuItem) popup.getComponent(1)).doClick();
		assertEquals("SecondMidpoint",
				group.getClientProperty("geocedg.userTool.activeCommand"));
		assertEquals("S", group.getClientProperty("geocedg.userTool.monogram"));
		assertEquals("monogram",
				command.getClientProperty("geocedg.userTool.icon.source"));
		assertEquals("T", command.getClientProperty("geocedg.userTool.monogram"));
		assertEquals("ThirdMidpoint",
				command.getAccessibleContext().getAccessibleName());
		assertTrue(command.getToolTipText().contains("ThirdMidpoint"));
		assertTrue(command.getToolTipText().contains(tool.name()));
	}

	@Test
	void pngPinIconPersistsDigestAndTransparentAspectPaddingWithoutDocumentMutation()
			throws Exception {
		Package tool = library.install("owned.ggt", midpointPackage("OwnedMidpoint"));
		G9U1TestApp.eval(app, "P=(3,4)");
		final String before = app.getXML();
		final byte[] beforeArchive = archiveXml(app);
		final int steps = app.getKernel().getConstruction().steps();
		byte[] source = png(80, 20, new Color(20, 80, 160, 220));

		library.pin(tool.id(), "OwnedMidpoint", "wide-source.png", source);
		GeoCeDGUserToolLibrary.PinnedCommand pin = library.pinnedCommands().get(0);
		GeoCeDGUserToolLibrary.PinIcon icon = pin.icon();
		assertNotNull(icon);
		assertEquals("wide-source.png", icon.sourceName());
		assertEquals(80, icon.sourceWidth());
		assertEquals(20, icon.sourceHeight());
		assertArrayEquals(source, icon.sourceBytes());
		assertEquals(HexFormat.of().formatHex(
				MessageDigest.getInstance("SHA-256").digest(source)), icon.sourceDigest());
		BufferedImage normalized = ImageIO.read(new ByteArrayInputStream(icon.toolbarBytes()));
		assertEquals(GeoCeDGUserToolLibrary.TOOLBAR_ICON_SIZE, normalized.getWidth());
		assertEquals(GeoCeDGUserToolLibrary.TOOLBAR_ICON_SIZE, normalized.getHeight());
		assertEquals(0, normalized.getRGB(0, 0) >>> 24);
		assertTrue(normalized.getRGB(32, 32) >>> 24 > 0);

		JSONObject root = new JSONObject(Files.readString(storage));
		assertEquals(3, root.getInt("version"));
		assertEquals(1, root.getInt("definitionDigestVersion"));
		assertTrue(Files.readString(storage).contains(icon.sourceDigest()));
		GeoCeDGUserToolLibrary reopened = new GeoCeDGUserToolLibrary(app, storage);
		assertEquals(icon.sourceDigest(), reopened.pinnedCommands().get(0).icon()
				.sourceDigest());
		JPanel toolbar = new JPanel();
		new GeoCeDGUserTools(app, reopened).populatePins(toolbar);
		JToggleButton button = (JToggleButton) toolbar.getComponent(0);
		assertNotNull(button.getIcon());
		assertTrue(button.getIcon() instanceof ImageIcon);
		assertEquals("custom",
				button.getClientProperty("geocedg.userTool.icon.source"));
		assertNull(button.getClientProperty("geocedg.userTool.monogram"));
		assertNull(button.getText());
		assertEquals("OwnedMidpoint", button.getAccessibleContext().getAccessibleName());
		assertEquals(app.getScaledIconSize() + 12, button.getPreferredSize().height);
		assertEquals(button.getPreferredSize().height, button.getPreferredSize().width);

		assertEquals(before, app.getXML());
		assertArrayEquals(beforeArchive, archiveXml(app));
		assertEquals(steps, app.getKernel().getConstruction().steps());
		assertEquals(0, app.getKernel().getMacroNumber());
	}

	@Test
	void changingUnpinningAndRemovingPngIconsCleansInlineApplicationData() throws Exception {
		Package tool = library.install("owned.ggt", midpointPackage("OwnedMidpoint"));
		final String before = app.getXML();
		byte[] first = png(64, 16, Color.RED);
		byte[] second = png(16, 64, Color.BLUE);
		library.pin(tool.id(), "OwnedMidpoint", "first.png", first);
		String firstDigest = library.pinnedCommands().get(0).icon().sourceDigest();
		library.setPinIcon(tool.id(), "OwnedMidpoint", "second.png", second);
		String secondDigest = library.pinnedCommands().get(0).icon().sourceDigest();
		assertNotEquals(firstDigest, secondDigest);
		assertFalse(Files.readString(storage).contains(firstDigest));
		assertTrue(Files.readString(storage).contains(secondDigest));

		library.setPinIcon(tool.id(), "OwnedMidpoint", null, null);
		assertNull(library.pinnedCommands().get(0).icon());
		assertFalse(Files.readString(storage).contains(secondDigest));
		library.pin(tool.id(), "OwnedMidpoint", "second.png", second);
		library.pin(tool.id(), "OwnedMidpoint", false);
		assertFalse(Files.readString(storage).contains(secondDigest));
		library.pin(tool.id(), "OwnedMidpoint", "first.png", first);
		library.remove(tool.id());
		assertFalse(Files.readString(storage).contains(firstDigest));
		assertEquals(before, app.getXML());
		assertEquals(0, app.getKernel().getMacroNumber());
	}

	@Test
	void invalidSpoofedOversizedAndDimensionBombPngFailWithoutMutation() throws Exception {
		Package tool = library.install("owned.ggt", midpointPackage("OwnedMidpoint"));
		library.pin(tool.id(), "OwnedMidpoint", true);
		byte[] preferences = Files.readAllBytes(storage);
		String before = app.getXML();
		assertThrows(IOException.class,
				() -> library.setPinIcon(tool.id(), "OwnedMidpoint", "icon.jpg",
						png(16, 16, Color.RED)));
		assertThrows(IOException.class,
				() -> library.setPinIcon(tool.id(), "OwnedMidpoint", "spoof.png",
						new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a}));
		assertThrows(IOException.class,
				() -> library.setPinIcon(tool.id(), "OwnedMidpoint", "large.png",
						new byte[GeoCeDGUserToolLibrary.MAX_ICON_BYTES + 1]));
		assertThrows(IOException.class,
				() -> library.setPinIcon(tool.id(), "OwnedMidpoint", "bomb.png",
						png(GeoCeDGUserToolLibrary.MAX_ICON_EDGE + 1, 1, Color.RED)));
		assertArrayEquals(preferences, Files.readAllBytes(storage));
		assertNull(library.pinnedCommands().get(0).icon());
		assertEquals(before, app.getXML());
		assertEquals(0, app.getKernel().getMacroNumber());
	}

	@Test
	void groupedPngPinsKeepIndividualPopupIconsWithoutChangingToolIdentity()
			throws Exception {
		Package tool = library.install("owned.ggt",
				midpointPackage("FirstMidpoint", "SecondMidpoint"));
		library.pin(tool.id(), "FirstMidpoint", "first.png", png(32, 16, Color.RED));
		library.pin(tool.id(), "SecondMidpoint", "second.png", png(16, 32, Color.BLUE));
		library.setPinGroup(tool.id(), "FirstMidpoint", "Pair");
		library.setPinGroup(tool.id(), "SecondMidpoint", "Pair");
		JPanel toolbar = new JPanel();
		new GeoCeDGUserTools(app, library).populatePins(toolbar);
		assertEquals(1, toolbar.getComponentCount());
		JToggleButton group = (JToggleButton) toolbar.getComponent(0);
		assertNotNull(group.getIcon());
		assertNull(group.getText());
		assertEquals("custom",
				group.getClientProperty("geocedg.userTool.icon.source"));
		assertNull(group.getClientProperty("geocedg.userTool.monogram"));
		assertEquals("FirstMidpoint",
				group.getClientProperty("geocedg.userTool.activeCommand"));
		JPopupMenu popup = (JPopupMenu) group.getClientProperty("geocedg.userTool.popup");
		assertEquals(2, popup.getComponentCount());
		for (int i = 0; i < popup.getComponentCount(); i++) {
			assertNotNull(((JMenuItem) popup.getComponent(i)).getIcon());
		}
		assertEquals(List.of("FirstMidpoint", "SecondMidpoint"),
				library.pinnedCommands().stream()
						.map(GeoCeDGUserToolLibrary.PinnedCommand::command).toList());
		assertEquals(0, app.getKernel().getMacroNumber());
	}

	@Test
	void managerReflectsAndRefreshesEffectivePinOrderAndGroup() throws Exception {
		Package tool = library.install("owned.ggt",
				midpointPackage("FirstMidpoint", "SecondMidpoint", "ThirdMidpoint"));
		final String before = app.getXML();
		library.pin(tool.id(), "FirstMidpoint", true);
		library.pin(tool.id(), "SecondMidpoint", true);
		library.pin(tool.id(), "ThirdMidpoint", true);
		library.setPinGroup(tool.id(), "FirstMidpoint", "Bisectors");
		library.setPinGroup(tool.id(), "SecondMidpoint", "Bisectors");
		library.movePinned(tool.id(), "SecondMidpoint", -1);

		JPanel manager = new JPanel();
		new GeoCeDGUserTools(app, library).populateManagerPins(manager, tool);
		JPanel firstRow = (JPanel) manager.getComponent(0);
		assertEquals("SecondMidpoint",
				firstRow.getClientProperty("geocedg.userTool.command"));
		assertEquals(0, firstRow.getClientProperty("geocedg.userTool.order"));
		assertEquals("1. SecondMidpoint", ((JCheckBox) firstRow.getComponent(0)).getText());
		assertEquals("Bisectors", ((JButton) firstRow.getComponent(1)).getText());

		((JButton) firstRow.getComponent(3)).doClick();
		assertEquals("FirstMidpoint", ((JPanel) manager.getComponent(0))
				.getClientProperty("geocedg.userTool.command"));
		assertEquals("SecondMidpoint", ((JPanel) manager.getComponent(1))
				.getClientProperty("geocedg.userTool.command"));
		assertEquals("Bisectors", ((JButton) ((JPanel) manager.getComponent(0))
				.getComponent(1)).getText());
		assertFalse(((JButton) ((JPanel) manager.getComponent(0)).getComponent(2))
				.isEnabled());
		assertFalse(((JButton) ((JPanel) manager.getComponent(2)).getComponent(3))
				.isEnabled());
		assertEquals(before, app.getXML());
		assertEquals(0, app.getKernel().getMacroNumber());
	}

	@Test
	void iconChooserPresentationIsLocalizedAndAccessibleInEnglishAndSpanish()
			throws Exception {
		Package tool = library.install("owned.ggt", midpointPackage("OwnedMidpoint"));
		library.pin(tool.id(), "OwnedMidpoint", true);
		GeoCeDGUserTools presentation = new GeoCeDGUserTools(app, library);
		for (Locale locale : List.of(Locale.ENGLISH, new Locale("es"))) {
			app.setLocale(locale);
			JPanel manager = new JPanel();
			presentation.populateManagerPins(manager, tool);
			JButton icon = (JButton) ((JPanel) manager.getComponent(0)).getComponent(4);
			String expected = locale.getLanguage().equals("es")
					? "Elegir icono PNG…" : "Choose PNG icon…";
			assertEquals(expected, icon.getText());
			assertEquals(expected, icon.getAccessibleContext().getAccessibleName());
			assertFalse(icon.getToolTipText().isBlank());
			assertEquals(icon.getToolTipText(),
					icon.getAccessibleContext().getAccessibleDescription());
		}
	}

	@Test
	void groupedPinsRemainOneContiguousVisualUnitWhenReordered() throws Exception {
		Package tool = library.install("owned.ggt",
				midpointPackage("FirstMidpoint", "SecondMidpoint", "ThirdMidpoint"));
		library.pin(tool.id(), "FirstMidpoint", true);
		library.pin(tool.id(), "SecondMidpoint", true);
		library.pin(tool.id(), "ThirdMidpoint", true);
		library.setPinGroup(tool.id(), "FirstMidpoint", "Bisectors");
		library.setPinGroup(tool.id(), "ThirdMidpoint", "Bisectors");

		assertEquals(List.of("FirstMidpoint", "ThirdMidpoint", "SecondMidpoint"),
				library.pinnedCommands().stream()
						.map(GeoCeDGUserToolLibrary.PinnedCommand::command).toList());
		JPanel pins = new JPanel();
		new GeoCeDGUserTools(app, library).populatePins(pins);
		assertEquals(2, pins.getComponentCount());
		assertEquals("Bisectors", ((JToggleButton) pins.getComponent(0))
				.getClientProperty("geocedg.userTool.group"));
		assertEquals("SecondMidpoint", ((JToggleButton) pins.getComponent(1))
				.getClientProperty("geocedg.userTool.command"));

		library.movePinned(tool.id(), "ThirdMidpoint", 1);
		assertEquals(List.of("SecondMidpoint", "FirstMidpoint", "ThirdMidpoint"),
				library.pinnedCommands().stream()
						.map(GeoCeDGUserToolLibrary.PinnedCommand::command).toList());
		pins = new JPanel();
		new GeoCeDGUserTools(app, library).populatePins(pins);
		assertEquals("SecondMidpoint", ((JToggleButton) pins.getComponent(0))
				.getClientProperty("geocedg.userTool.command"));
		assertEquals("Bisectors", ((JToggleButton) pins.getComponent(1))
				.getClientProperty("geocedg.userTool.group"));
	}

	@Test
	void versionOnePinPreferenceMigratesWithoutRegisteringItsMacro() throws Exception {
		byte[] bytes = midpointPackage("OwnedMidpoint");
		String id = HexFormat.of().formatHex(
				MessageDigest.getInstance("SHA-256").digest(bytes));
		JSONArray pins = new JSONArray().put("OwnedMidpoint");
		JSONArray packages = new JSONArray().put(new JSONObject().put("name", "owned.ggt")
				.put("sha256", id).put("ggt", Base64.getEncoder().encodeToString(bytes))
				.put("pinned", pins));
		Files.writeString(storage,
				new JSONObject().put("version", 1).put("packages", packages).toString());

		GeoCeDGUserToolLibrary migrated = new GeoCeDGUserToolLibrary(app, storage);
		assertEquals(List.of("OwnedMidpoint"), migrated.pinnedCommands().stream()
				.map(GeoCeDGUserToolLibrary.PinnedCommand::command).toList());
		assertEquals("", migrated.pinnedCommands().get(0).group());
		assertEquals(0, migrated.pinnedCommands().get(0).order());
		assertEquals(1, new JSONObject(Files.readString(storage)).getInt("version"));
		migrated.setPinGroup(id, "OwnedMidpoint", "Planar");
		assertEquals(3, new JSONObject(Files.readString(storage)).getInt("version"));
		assertEquals("Planar", new GeoCeDGUserToolLibrary(app, storage)
				.pinnedCommands().get(0).group());
		assertEquals(0, app.getKernel().getMacroNumber());
	}

	@Test
	void maximumStoredPinOrderStillAllowsAValidNewPin() throws Exception {
		byte[] bytes = midpointPackage("FirstMidpoint", "SecondMidpoint");
		String id = HexFormat.of().formatHex(
				MessageDigest.getInstance("SHA-256").digest(bytes));
		JSONArray pins = new JSONArray().put(new JSONObject()
				.put("command", "FirstMidpoint").put("group", "").put("order", 4095));
		JSONArray packages = new JSONArray().put(new JSONObject().put("name", "owned.ggt")
				.put("sha256", id).put("ggt", Base64.getEncoder().encodeToString(bytes))
				.put("pinned", pins));
		Files.writeString(storage,
				new JSONObject().put("version", 2).put("packages", packages).toString());

		GeoCeDGUserToolLibrary boundary = new GeoCeDGUserToolLibrary(app, storage);
		boundary.pin(id, "SecondMidpoint", true);
		GeoCeDGUserToolLibrary reopened = new GeoCeDGUserToolLibrary(app, storage);
		List<Integer> orders = reopened.pinnedCommands().stream()
				.map(GeoCeDGUserToolLibrary.PinnedCommand::order).toList();
		assertEquals(2, orders.size());
		assertTrue(orders.stream().allMatch(order -> order >= 0 && order <= 4095));
		assertFalse(orders.get(0).equals(orders.get(1)));
		assertEquals(0, app.getKernel().getMacroNumber());
	}

	@Test
	void removalDoesNotDeleteExistingDocumentMacroOrOutput() throws Exception {
		Package tool = library.install("owned.ggt", midpointPackage("OwnedMidpoint"));
		Macro macro = library.activate(tool.id(), "OwnedMidpoint");
		GeoElement p = G9U1TestApp.eval(app, "P=(0,0)");
		GeoElement q = G9U1TestApp.eval(app, "Q=(4,0)");
		GeoElement result = app.getKernel().useMacro(new String[] {"R"}, macro,
				new GeoElement[] {p, q})[0];
		String before = app.getXML();
		library.remove(tool.id());
		assertTrue(new GeoCeDGUserToolLibrary(app, storage).packages().isEmpty());
		assertSame(macro, app.getKernel().getMacro("OwnedMidpoint"));
		assertSame(result, G9U1TestApp.lookup(app, "R"));
		assertTrue(result.isDefined());
		assertEquals(before, app.getXML());
	}

	@Test
	void documentMacrosNeverAutoInstallOrBecomePreferenceSnapshot() throws Exception {
		app.loadMacroFileFromByteArray(midpointPackage("DocumentOnly"), false);
		assertNotNull(app.getKernel().getMacro("DocumentOnly"));
		assertTrue(library.packages().isEmpty());
		assertFalse(Files.exists(storage));
		String snapshot = zipXml(app.getMacroFileAsByteArray(), "geogebra_macro.xml");
		assertFalse(snapshot.contains("cmdName=\"DocumentOnly\""));
		assertNotNull(app.getKernel().getMacro("DocumentOnly"));
	}

	@Test
	void preferenceRestoreCannotSmuggleDocumentMacrosButExplicitLoadStillWorks() throws Exception {
		byte[] bytes = midpointPackage("DocumentOnly");
		app.loadMacroFileFromByteArray(bytes, true);
		assertEquals(0, app.getKernel().getMacroNumber());
		app.loadMacroFileFromByteArray(bytes, false);
		Macro macro = app.getKernel().getMacro("DocumentOnly");
		assertNotNull(macro);
		app.loadMacroFileFromByteArray(bytes, true);
		assertSame(macro, app.getKernel().getMacro("DocumentOnly"));
		assertTrue(library.packages().isEmpty());
	}

	@Test
	void classicRetainsInheritedPreferenceMacroContract() throws Exception {
		AppD classic = new AppD(new CommandLineArguments(new String[] {"--silent"}),
				new JPanel(), false);
		G9U1TestApp.withoutWindowDispatcher(classic);
		classic.setErrorDialogsActive(false);
		classic.loadMacroFileFromByteArray(midpointPackage("DocumentOnly"), true);
		assertNotNull(classic.getKernel().getMacro("DocumentOnly"));
		assertTrue(zipXml(classic.getMacroFileAsByteArray(), "geogebra_macro.xml")
				.contains("cmdName=\"DocumentOnly\""));
		assertThrows(IllegalArgumentException.class,
				() -> new GeoCeDGUserToolLibrary(classic, temporary.resolve("classic.json")));
	}

	@Test
	void windowlessFixtureReleasesGlobalDispatcherWithoutChangingNativeRegistration()
			throws Exception {
		final KeyboardFocusManager previous = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		InspectableFocusManager isolated = new InspectableFocusManager();
		KeyboardFocusManager.setCurrentKeyboardFocusManager(isolated);
		try {
			AppD classic = new AppD(new CommandLineArguments(new String[] {"--silent"}),
					new JPanel(), false);
			assertTrue(isolated.contains(classic));
			assertSame(classic, G9U1TestApp.withoutWindowDispatcher(classic));
			assertFalse(isolated.contains(classic));
			AppGeoCeDG product = G9U1TestApp.create();
			assertFalse(isolated.contains(product));
			midpointPackage("TemporaryToolHost");
			assertEquals(0, isolated.dispatcherCount());
		} finally {
			KeyboardFocusManager.setCurrentKeyboardFocusManager(previous);
		}
		assertSame(previous, KeyboardFocusManager.getCurrentKeyboardFocusManager());
	}

	private static final class InspectableFocusManager extends DefaultKeyboardFocusManager {
		boolean contains(KeyEventDispatcher dispatcher) {
			List<KeyEventDispatcher> registered = getKeyEventDispatchers();
			return registered != null && registered.contains(dispatcher);
		}

		int dispatcherCount() {
			List<KeyEventDispatcher> registered = getKeyEventDispatchers();
			return registered == null ? 0 : registered.size();
		}
	}

	@Test
	void nativeLengthCollisionFailsClosed() throws Exception {
		assertNativeCollisionRejected("Length");
	}

	@Test
	void nativeIntersectCollisionFailsClosed() throws Exception {
		assertNativeCollisionRejected("Intersect");
	}

	@Test
	void nativePointCollisionFailsClosed() throws Exception {
		assertNativeCollisionRejected("Point");
	}

	@Test
	void nativeSplineV2CollisionFailsClosed() throws Exception {
		assertNativeCollisionRejected("SplineV2");
	}

	@Test
	void nativeMidpointCollisionFailsClosed() throws Exception {
		assertNativeCollisionRejected("Midpoint");
	}

	private void assertNativeCollisionRejected(String nativeName) throws Exception {
		byte[] proposed = changeXml(midpointPackage("OwnedMidpoint"),
				xml -> xml.replace("cmdName=\"OwnedMidpoint\"", "cmdName=\"" + nativeName + "\""));
		assertRejectedWithoutMutation(proposed);
	}

	@Test
	void installedCommandCollisionPreservesOriginalLibraryBytes() throws Exception {
		byte[] original = midpointPackage("OwnedMidpoint");
		library.install("first.ggt", original);
		byte[] before = Files.readAllBytes(storage);
		assertThrows(IOException.class, () -> library.install("second.ggt", original));
		assertArrayEquals(before, Files.readAllBytes(storage));
		assertEquals(1, library.packages().size());
	}

	@Test
	void secondWindowRechecksInstalledCommandCollisionAgainstCurrentStore() throws Exception {
		AppGeoCeDG otherApp = G9U1TestApp.create();
		GeoCeDGUserToolLibrary other = new GeoCeDGUserToolLibrary(otherApp, storage);
		library.install("first.ggt", midpointPackage("OwnedMidpoint"));
		byte[] before = Files.readAllBytes(storage);
		assertThrows(IOException.class,
				() -> other.install("conflicting.ggt", midpointPackage("OwnedMidpoint")));
		assertArrayEquals(before, Files.readAllBytes(storage));
		assertEquals(1, other.packages().size());
		assertEquals(0, otherApp.getKernel().getMacroNumber());
	}

	@Test
	void concurrentWindowPinAndRemovePreserveOtherPackagesAndRejectRemovedActivation()
			throws Exception {
		AppGeoCeDG otherApp = G9U1TestApp.create();
		GeoCeDGUserToolLibrary other = new GeoCeDGUserToolLibrary(otherApp, storage);
		Package first = library.install("first.ggt", midpointPackage("FirstMidpoint"));
		Package second = other.install("second.ggt", midpointPackage("SecondMidpoint"));
		library.pin(first.id(), "FirstMidpoint", true);
		assertEquals(2, new GeoCeDGUserToolLibrary(app, storage).packages().size());
		JMenu menu = new JMenu();
		new GeoCeDGUserTools(app, library).populate(menu);
		assertEquals("SecondMidpoint", menu.getItem(3).getText());
		other.remove(second.id());
		GeoCeDGUserToolLibrary reopened = new GeoCeDGUserToolLibrary(app, storage);
		assertEquals(1, reopened.packages().size());
		assertEquals(first.id(), reopened.packages().get(0).id());
		assertTrue(reopened.packages().get(0).isPinned("FirstMidpoint"));
		// This window saw SecondMidpoint earlier but must not activate a stale cached entry.
		assertThrows(IOException.class, () -> library.activate(second.id(), "SecondMidpoint"));
		assertEquals(0, app.getKernel().getMacroNumber());
		byte[] before = Files.readAllBytes(storage);
		Files.writeString(storage, "{\"version\":1,\"packages\":[{}]}");
		assertThrows(IOException.class, library::refresh);
		assertEquals(first.id(), library.packages().get(0).id());
		assertTrue(library.packages().get(0).isPinned("FirstMidpoint"));
		Files.write(storage, before);
	}

	@Test
	void busyLibraryLockRejectsWithoutReplacingPreferences() throws Exception {
		Package first = library.install("first.ggt", midpointPackage("FirstMidpoint"));
		byte[] before = Files.readAllBytes(storage);
		Path lockPath = storage.resolveSibling(storage.getFileName() + ".lock");
		try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE,
				StandardOpenOption.WRITE); FileLock lock = channel.tryLock()) {
			assertNotNull(lock);
			IOException rejected = assertThrows(IOException.class,
					() -> library.pin(first.id(), "FirstMidpoint", true));
			assertEquals("UserTools.LibraryBusy", rejected.getMessage());
			assertArrayEquals(before, Files.readAllBytes(storage));
			assertFalse(first.isPinned("FirstMidpoint"));
		}
	}

	@Test
	void equivalentEmbeddedMacroIsAdoptedWithoutDuplicateOrReplacement() throws Exception {
		byte[] bytes = midpointPackage("OwnedMidpoint");
		Package tool = library.install("owned.ggt", bytes);
		app.loadMacroFileFromByteArray(bytes, false);
		Macro document = app.getKernel().getMacro("OwnedMidpoint");
		assertNotNull(document);
		document.setShowInToolBar(!document.isShowInToolBar());
		String before = app.getXML();
		assertNull(library.unavailableReason(tool));
		assertSame(document, library.activate(tool.id(), "OwnedMidpoint"));
		assertSame(document, app.getKernel().getMacro("OwnedMidpoint"));
		assertNull(app.getKernel().getMacro("OwnedMidpoint1"));
		assertEquals(1, app.getKernel().getMacroNumber());
		assertEquals(before, app.getXML());
		GeoCeDGUserToolLibrary other = new GeoCeDGUserToolLibrary(app,
				temporary.resolve("other.json"));
		Package installedFromDocument = other.install("owned.ggt", bytes);
		assertNull(other.unavailableReason(installedFromDocument));
		assertSame(document, other.activate(installedFromDocument.id(), "OwnedMidpoint"));
	}

	@Test
	void mismatchedOrPartialEmbeddedPackageFailsClosedWithoutRenameOrReplacement()
			throws Exception {
		byte[] installedBytes = midpointPackage("OwnedMidpoint");
		Package tool = library.install("owned.ggt", installedBytes);
		app.loadMacroFileFromByteArray(linePackage("OwnedMidpoint"), false);
		Macro document = app.getKernel().getMacro("OwnedMidpoint");
		assertNotNull(document);
		String before = app.getXML();
		byte[] preferences = Files.readAllBytes(storage);
		assertEquals("UserTools.DefinitionMismatch", library.unavailableReason(tool));
		assertThrows(IOException.class, () -> library.activate(tool.id(), "OwnedMidpoint"));
		assertSame(document, app.getKernel().getMacro("OwnedMidpoint"));
		assertNull(app.getKernel().getMacro("OwnedMidpoint1"));
		assertEquals(before, app.getXML());
		assertArrayEquals(preferences, Files.readAllBytes(storage));
		GeoCeDGUserToolLibrary other = new GeoCeDGUserToolLibrary(app,
				temporary.resolve("mismatch-install.json"));
		IOException installMismatch = assertThrows(IOException.class,
				() -> other.install("owned.ggt", installedBytes));
		assertTrue(installMismatch.getMessage().startsWith("UserTools.DefinitionMismatch"));
		assertTrue(other.packages().isEmpty());

		document.getKernel().removeMacro(document);
		Package pair = library.install("pair.ggt",
				midpointPackage("FirstMidpoint", "SecondMidpoint"));
		app.loadMacroFileFromByteArray(midpointPackage("FirstMidpoint"), false);
		Macro first = app.getKernel().getMacro("FirstMidpoint");
		String partialBefore = app.getXML();
		final byte[] partialPreferences = Files.readAllBytes(storage);
		assertEquals("UserTools.DocumentConflict", library.unavailableReason(pair));
		assertThrows(IOException.class, () -> library.activate(pair.id(), "FirstMidpoint"));
		assertSame(first, app.getKernel().getMacro("FirstMidpoint"));
		assertNull(app.getKernel().getMacro("SecondMidpoint"));
		assertNull(app.getKernel().getMacro("FirstMidpoint1"));
		assertEquals(1, app.getKernel().getMacroNumber());
		assertEquals(partialBefore, app.getXML());
		assertArrayEquals(partialPreferences, Files.readAllBytes(storage));
	}

	@Test
	void malformedArchiveAndDoctypeFailWithoutMutation() throws Exception {
		assertRejectedWithoutMutation(new byte[] {1, 2, 3});
		assertRejectedWithoutMutation(ggt("<geogebra><macro></geogebra>"));
		String xml = zipXml(midpointPackage("OwnedMidpoint"), "geogebra_macro.xml");
		int opening = xml.indexOf("<geogebra");
		assertRejectedWithoutMutation(ggt(xml.substring(0, opening)
				+ "<!DOCTYPE geogebra [<!ENTITY remote SYSTEM 'file:///absent'>]>"
				+ xml.substring(opening)));
		assertActivationRollback(false);
		assertActivationRollback(true);
	}

	private void assertActivationRollback(boolean parserThrows) throws Exception {
		AppGeoCeDG host = spy(G9U1TestApp.create());
		host.loadMacroFileFromByteArray(midpointPackage("ExistingMidpoint"), false);
		final Macro existing = host.getKernel().getMacro("ExistingMidpoint");
		G9U1TestApp.eval(host, "P=(7,9)");
		GeoCeDGUserToolLibrary tools = new GeoCeDGUserToolLibrary(host,
				temporary.resolve("rollback-" + parserThrows + ".json"));
		Package tool = tools.install("owned.ggt", midpointPackage("OwnedMidpoint"));
		final String before = host.getXML();
		MyXMLioD xmlio = spy(host.getXMLio());
		doReturn(xmlio).when(host).getXMLio();
		doAnswer(invocation -> {
			invocation.callRealMethod();
			host.getKernel().setMacroCommandName(host.getKernel().getMacro("OwnedMidpoint"),
					"UnexpectedMidpoint");
			if (parserThrows) {
				throw new XMLParseException("Injected registration failure");
			}
			return null;
		}).when(xmlio).processXMLString(anyString(), eq(false), eq(true), eq(false));
		assertThrows(IOException.class, () -> tools.activate(tool.id(), "OwnedMidpoint"));
		assertSame(existing, host.getKernel().getMacro("ExistingMidpoint"));
		assertNull(host.getKernel().getMacro("OwnedMidpoint"));
		assertNull(host.getKernel().getMacro("UnexpectedMidpoint"));
		assertEquals(1, host.getKernel().getMacroNumber());
		assertEquals(before, host.getXML());
	}

	@Test
	void scriptsAndSerializedSemanticObjectsAreRejected() throws Exception {
		byte[] original = midpointPackage("OwnedMidpoint");
		assertRejectedWithoutMutation(changeXml(original, xml -> xml.replace("</construction>",
				"<ggbscript val=\"SetValue(a,1)\"/></construction>")));
		assertRejectedWithoutMutation(changeXml(original,
				xml -> xml.replace("type=\"point\"", "type=\"locusv2\"")));
		assertRejectedWithoutMutation(changeXml(original,
				xml -> xml.replace("type=\"point\"", "type=\"point3d\"")));
		assertRejectedWithoutMutation(changeXml(original, xml -> xml.replace("</construction>",
				"<geocedgSpatial><object id=\"bad\"/></geocedgSpatial></construction>")));
	}

	@Test
	void featureDisabledBodyCannotBypassPolicyThroughFileLoadOrExpression() throws Exception {
		AppGeoCeDG disabled = G9U1TestApp.create(false);
		GeoCeDGUserToolLibrary disabledLibrary = new GeoCeDGUserToolLibrary(disabled,
				temporary.resolve("disabled.json"));
		byte[] original = midpointPackage("OwnedMidpoint");
		byte[] command = changeXml(original,
				xml -> xml.replace("name=\"Midpoint\"", "name=\"SplineV2\""));
		assertTrue(assertThrows(IOException.class,
				() -> disabledLibrary.install("off.ggt", command)).getMessage()
				.contains("LocusV2.FeatureDisabled"));
		byte[] expression = changeXml(original, xml -> xml.replace("</construction>",
				"<expression label=\"x\" exp=\"LocusV2(a,b)\"/></construction>"));
		assertThrows(IOException.class, () -> disabledLibrary.install("off.ggt", expression));
		assertEquals(0, disabled.getKernel().getMacroNumber());
		assertTrue(disabledLibrary.packages().isEmpty());
	}

	@Test
	void tamperedStoredPackageFailsClosedAndIsNotRewritten() throws Exception {
		Package tool = library.install("owned.ggt", midpointPackage("OwnedMidpoint"));
		byte[] original = Files.readAllBytes(storage);
		String tampered = new String(original, StandardCharsets.UTF_8)
				.replace(tool.id(), "0".repeat(64));
		Files.writeString(storage, tampered);
		byte[] before = Files.readAllBytes(storage);
		assertThrows(IOException.class, () -> new GeoCeDGUserToolLibrary(app, storage));
		assertArrayEquals(before, Files.readAllBytes(storage));
		assertEquals(0, app.getKernel().getMacroNumber());

		Files.write(storage, original);
		String definitionTampered = Files.readString(storage).replace(
				tool.definitionDigest("OwnedMidpoint"), "f".repeat(64));
		Files.writeString(storage, definitionTampered);
		byte[] definitionBefore = Files.readAllBytes(storage);
		assertThrows(IOException.class, () -> new GeoCeDGUserToolLibrary(app, storage));
		assertArrayEquals(definitionBefore, Files.readAllBytes(storage));
		assertEquals(0, app.getKernel().getMacroNumber());
	}

	@Test
	void dynamicMenuAndPinsReuseManagementActionWithoutAddingProductIds() throws Exception {
		Package tool = library.install("owned.ggt", midpointPackage("OwnedMidpoint"));
		GeoCeDGUserTools presentation = new GeoCeDGUserTools(app, library);
		JMenu menu = new JMenu();
		presentation.populate(menu);
		assertEquals("automation.manage-user-tools",
				menu.getItem(0).getClientProperty(GeoCeDGActionRegistry.ACTION_ID));
		assertEquals("OwnedMidpoint", menu.getItem(2).getText());
		assertNull(menu.getItem(2).getClientProperty(GeoCeDGActionRegistry.ACTION_ID));
		JPanel pins = new JPanel();
		presentation.populatePins(pins);
		assertEquals(0, pins.getComponentCount());
		library.pin(tool.id(), "OwnedMidpoint", true);
		presentation.populatePins(pins);
		assertEquals(1, pins.getComponentCount());
		library.pin(tool.id(), "OwnedMidpoint", false);
		presentation.populatePins(pins);
		assertEquals(0, pins.getComponentCount());
		assertEquals(0, app.getKernel().getMacroNumber());
	}

	private void assertRejectedWithoutMutation(byte[] bytes) {
		String before = app.getXML();
		assertThrows(IOException.class, () -> library.install("bad.ggt", bytes));
		assertEquals(before, app.getXML());
		assertEquals(0, app.getKernel().getMacroNumber());
		assertTrue(library.packages().isEmpty());
		assertFalse(Files.exists(storage));
	}

	private static byte[] midpointPackage(String... names) throws Exception {
		AppGeoCeDG source = G9U1TestApp.create();
		GeoElement a = G9U1TestApp.eval(source, "A=(0,0)");
		GeoElement b = G9U1TestApp.eval(source, "B=(2,0)");
		GeoElement m = G9U1TestApp.eval(source, "M=Midpoint(A,B)");
		ArrayList<Macro> macros = new ArrayList<>();
		for (String name : names) {
			Macro macro = new Macro(source.getKernel(), name, new GeoElement[] {a, b},
					new GeoElement[] {m});
			source.getKernel().addMacro(macro);
			macros.add(macro);
		}
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		source.getXMLio().writeMacroStream(output, macros,
				new ArrayList<>());
		return output.toByteArray();
	}

	private static byte[] linePackage(String name) throws Exception {
		AppGeoCeDG source = G9U1TestApp.create();
		GeoElement a = G9U1TestApp.eval(source, "A=(0,0)");
		GeoElement b = G9U1TestApp.eval(source, "B=(2,0)");
		GeoElement line = G9U1TestApp.eval(source, "g=Line(A,B)");
		Macro macro = new Macro(source.getKernel(), name, new GeoElement[] {a, b},
				new GeoElement[] {line});
		source.getKernel().addMacro(macro);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		source.getXMLio().writeMacroStream(output, new ArrayList<>(List.of(macro)),
				new ArrayList<>());
		return output.toByteArray();
	}

	private static byte[] png(int width, int height, Color color) throws IOException {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try {
			graphics.setColor(color);
			graphics.fillRect(0, 0, width, height);
		} finally {
			graphics.dispose();
		}
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		assertTrue(ImageIO.write(image, "png", output));
		return output.toByteArray();
	}

	private static byte[] changeXml(byte[] original, UnaryOperator<String> change)
			throws IOException {
		return ggt(change.apply(zipXml(original, "geogebra_macro.xml")));
	}

	private static byte[] ggt(String xml) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (ZipOutputStream zip = new ZipOutputStream(output)) {
			zip.putNextEntry(new ZipEntry("geogebra_macro.xml"));
			zip.write(xml.getBytes(StandardCharsets.UTF_8));
			zip.closeEntry();
		}
		return output.toByteArray();
	}

	private static byte[] archiveXml(AppD app) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		app.getXMLio().writeGeoGebraFile(output, false);
		return zipXml(output.toByteArray(), "geogebra.xml").getBytes(StandardCharsets.UTF_8);
	}

	private static String zipXml(byte[] bytes, String name) throws IOException {
		try (ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(bytes))) {
			ZipEntry entry;
			while ((entry = input.getNextEntry()) != null) {
				if (name.equals(entry.getName())) {
					return new String(input.readAllBytes(), StandardCharsets.UTF_8);
				}
			}
		}
		throw new IOException("Missing archive entry: " + name);
	}
}
