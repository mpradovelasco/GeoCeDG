/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.geocedg.common.main.command.CanonicalCommandEntry;
import org.geocedg.common.main.command.CanonicalCommandSurface;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.main.Localization;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.editor.GeoGebraLexer;
import org.geogebra.desktop.gui.editor.HelpOnKeywordPanel;
import org.geogebra.desktop.gui.inputbar.InputBarHelpPanelD;
import org.geogebra.desktop.gui.inputfield.AutoCompleteTextFieldD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PRE-G9B-R5-B (ADR 0031 decisions 8 and 9): autocomplete shows, inserts and finds
 * canonical English heads with identity-carrying entries ({@code T-AUTOCOMPLETE}),
 * and every syntax surface shows the canonical head with the UI-language body,
 * never a literal key ({@code T-SYNTAX}).
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BR5BCanonicalHelpTest {

	/** Offered commands whose only syntax is CAS syntax; pre-existing, outside R5-B. */
	private static final Set<String> CAS_ONLY_SYNTAX = Set.of("CSolutions", "CSolve");

	@Test
	void autocompleteDisplaysAndInsertsCanonicalHeadsAndFindsSpanishAliases()
			throws Exception {
		AppGeoCeDG app = PreG9BR5BCanonicalCommandGateTest.application(
				PreG9BR5BFingerprints.SPANISH);
		List<String> perpendicular = completions(app, "Perp");
		assertTrue(perpendicular.contains(
				"PerpendicularLine( <Punto>, <Lado (semi/recta o segmento)> )"), perpendicular
						.toString());
		assertFalse(perpendicular.stream().anyMatch(line -> line.startsWith("Perpendicular(")),
				perpendicular.toString());
		assertTrue(completions(app, "Refle").contains("Reflect( <Objeto>, <Punto> )"));
		assertTrue(completions(app, "Circunf").stream().anyMatch(line -> line.startsWith(
				"Circle( <Punto>, ")));
		assertTrue(completions(app, "LugarGeom").stream().anyMatch(line -> line.equals(
				"LocusV2( <Punto dependiente>, <Punto restringido> )")));
		assertTrue(completions(app, "LongitudLug").stream().anyMatch(line -> line.startsWith(
				"LocusLength( <Lugar geométrico V2> )")), completions(app, "LongitudLug")
						.toString());
		assertTrue(completions(app, "SplineV").contains("SplineV2( <Lista de puntos> )"));
		for (String prefix : List.of("Perp", "Refle", "Circunf", "LugarGeom", "Longitud")) {
			for (String line : completions(app, prefix)) {
				String head = line.substring(0, Math.max(0, line.indexOf('(')));
				assertFalse(List.of("Perpendicular", "Refleja", "Circunferencia",
						"LugarGeométricoV2", "LongitudLugarGeométrico", "Longitud")
						.contains(head), prefix + ": " + line);
			}
		}

		List<CanonicalCommandEntry> reflect = CanonicalCommandSurface.completions(app,
				"Refle", false);
		assertTrue(reflect.stream().anyMatch(entry -> entry.getIdentity() == Commands.Mirror
				&& "Reflect".equals(entry.getHead()) && "Refleja".equals(entry.getAlias())),
				reflect.toString());
		assertEquals("Mirror", invoke(field(app, "Refle"), "canonicalCommandAt", "Refle"));
		assertEquals("OrthogonalLine", invoke(field(app, "Perpendicular"),
				"canonicalCommandAt", "Perpendicular"));

		AutoCompleteTextFieldD insert = field(app, "Refle");
		List<String> offered = insert.getCompletions();
		insert.validateAutoCompletion(offered.indexOf("Reflect( <Objeto>, <Punto> )"), offered);
		assertEquals("Reflect( <Objeto>, <Punto> )", insert.getText());
		AutoCompleteTextFieldD head = field(app, "Refle(C, f)", 5);
		List<String> headOnly = head.getCompletions();
		head.validateAutoCompletion(headOnly.indexOf("Reflect( <Objeto>, <Punto> )"),
				headOnly);
		assertEquals("Reflect(C, f)", head.getText());
		assertEquals("Mirror", app.getReverseCommand("Reflect"));
	}

	@Test
	void englishAutocompleteKeepsItsCanonicalHeadsAndEnglishBodies() throws Exception {
		AppGeoCeDG app = PreG9BR5BCanonicalCommandGateTest.application(Locale.ENGLISH);
		assertTrue(completions(app, "Perp").contains(
				"PerpendicularLine( <Point>, <Line> )"));
		assertTrue(completions(app, "Reflect").contains("Reflect( <Object>, <Point> )"));
		assertTrue(completions(app, "LocusV").contains(
				"LocusV2( <Dependent Point>, <Constrained Point> )"));
	}

	@Test
	void everyDisplayedCommandHasACanonicalSyntaxAndNeverALiteralKey() {
		for (Locale language : new Locale[] {Locale.ENGLISH, PreG9BR5BFingerprints.SPANISH}) {
			AppGeoCeDG app = PreG9BR5BCanonicalCommandGateTest.application(language);
			Localization loc = app.getLocalization();
			Set<String> withoutAlgebraSyntax = new TreeSet<>();
			for (CanonicalCommandEntry entry : CanonicalCommandSurface.offeredEntries(app,
					false)) {
				String name = entry.getInternalName();
				List<String> lines = CanonicalCommandSurface.syntaxLines(loc, name, false);
				if (lines.isEmpty()) {
					withoutAlgebraSyntax.add(name);
					assertFalse(CanonicalCommandSurface.syntaxLines(loc, name, true).isEmpty(),
							name);
				}
				assertHeads(entry, lines);
				assertHeads(entry, CanonicalCommandSurface.syntaxLines(loc, name, true));
			}
			assertEquals(CAS_ONLY_SYNTAX, withoutAlgebraSyntax, language.toString());
			app.getKernel().getGeoGebraCAS();
			for (CanonicalCommandEntry entry : CanonicalCommandSurface.offeredEntries(app,
					true)) {
				assertHeads(entry, CanonicalCommandSurface.syntaxLines(loc,
						entry.getInternalName(), true));
			}
		}
		Localization loc = PreG9BR5BCanonicalCommandGateTest.application(
				PreG9BR5BFingerprints.SPANISH).getLocalization();
		assertTrue(String.join("\n", CanonicalCommandSurface.syntaxLines(loc,
				"OrthogonalLine", false)).contains("PerpendicularLine( <Punto>, <Plano> )"));
		assertTrue(CanonicalCommandSurface.syntaxLines(loc, "CSolve", true).get(0)
				.startsWith("CSolve( "));
	}

	@Test
	void inputHelpFocusAndKeywordHelpAgreeOnCanonicalSyntax() {
		AppGeoCeDG app = PreG9BR5BCanonicalCommandGateTest.application(
				PreG9BR5BFingerprints.SPANISH);
		app.buildApplicationPanel();
		app.setShowAlgebraInput(true, true);
		GuiManagerD gui = (GuiManagerD) app.getGuiManager();
		InputBarHelpPanelD help = (InputBarHelpPanelD) gui.getInputHelpPanel();
		app.setShowInputHelpPanel(true);
		JTree tree = find(help, JTree.class);
		List<CanonicalCommandEntry> leaves = new ArrayList<>();
		collect((TreeNode) tree.getModel().getRoot(), leaves);
		assertTrue(leaves.stream().anyMatch(entry -> "PerpendicularLine".equals(
				entry.getHead()) && entry.getIdentity() == Commands.OrthogonalLine));
		assertFalse(leaves.stream().anyMatch(entry -> "Perpendicular".equals(
				entry.getHead())));
		for (String command : List.of("OrthogonalLine", "Mirror", "LocusV2", "LocusLength",
				"SplineV2", "Length", "Circle")) {
			help.focusCommand(command);
			DefaultMutableTreeNode selected = (DefaultMutableTreeNode)
					tree.getLastSelectedPathComponent();
			assertNotNull(selected, command);
			CanonicalCommandEntry entry = assertInstanceOf(CanonicalCommandEntry.class,
					selected.getUserObject());
			assertEquals(command, entry.getInternalName());
			String expected = app.getLocalization().getCommandSyntax(command);
			assertEquals(String.join("\n", CanonicalCommandSurface.syntaxLines(
					app.getLocalization(), command, false)), expected, command);
			String shown = find(help, JTextPane.class).getText().replace("\r\n", "\n");
			assertTrue(shown.startsWith(expected), command + ": " + shown);
			String keyword = find(HelpOnKeywordPanel.getInstance(app,
					app.getLocalization().getCommandHead(command)), JTextArea.class).getText();
			assertEquals(expected, keyword, command);
		}
		GeoGebraLexer lexer = new GeoGebraLexer(app);
		assertTrue(lexer.commands.contains("PerpendicularLine"));
		assertTrue(lexer.commands.contains("Perpendicular"));
		assertTrue(lexer.commands.contains("LocusV2"));
	}

	private static void assertHeads(CanonicalCommandEntry entry, List<String> lines) {
		for (String line : lines) {
			assertTrue(line.startsWith(entry.getHead() + "("), entry.getInternalName() + ": "
					+ line);
			assertFalse(line.endsWith(".Syntax") || line.endsWith(".Syntax3D")
					|| line.endsWith(".SyntaxCAS"), line);
		}
	}

	private static List<String> completions(AppGeoCeDG app, String word) throws Exception {
		List<String> completions = field(app, word).getCompletions();
		return completions == null ? List.of() : completions;
	}

	private static AutoCompleteTextFieldD field(AppGeoCeDG app, String text) throws Exception {
		return field(app, text, text.length());
	}

	private static AutoCompleteTextFieldD field(AppGeoCeDG app, String text, int caret)
			throws Exception {
		AutoCompleteTextFieldD field = new AutoCompleteTextFieldD(20, app);
		field.setAutoComplete(true);
		field.setText(text);
		field.setCaretPosition(caret);
		field.updateCurrentWord(false);
		Method reset = AutoCompleteTextFieldD.class.getDeclaredMethod("resetCompletions");
		reset.setAccessible(true);
		reset.invoke(field);
		return field;
	}

	private static Object invoke(AutoCompleteTextFieldD field, String method, String argument)
			throws Exception {
		Method target = AutoCompleteTextFieldD.class.getDeclaredMethod(method, String.class);
		target.setAccessible(true);
		return target.invoke(field, argument);
	}

	private static void collect(TreeNode node, List<CanonicalCommandEntry> leaves) {
		if (node instanceof DefaultMutableTreeNode
				&& ((DefaultMutableTreeNode) node).getUserObject()
						instanceof CanonicalCommandEntry) {
			leaves.add((CanonicalCommandEntry) ((DefaultMutableTreeNode) node).getUserObject());
		}
		for (int i = 0; i < node.getChildCount(); i++) {
			collect(node.getChildAt(i), leaves);
		}
	}

	private static <T> T find(Container container, Class<T> type) {
		for (Component component : container.getComponents()) {
			if (type.isInstance(component)) {
				return type.cast(component);
			}
			if (component instanceof Container) {
				T nested = find((Container) component, type);
				if (nested != null) {
					return nested;
				}
			}
		}
		return null;
	}
}
