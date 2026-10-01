/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.geocedg.common.main.command.CanonicalCommandEntry;
import org.geocedg.common.main.command.CanonicalCommandReentry;
import org.geocedg.common.main.command.CanonicalCommandSurface;
import org.geocedg.common.main.command.CanonicalCommandSurfaceError;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.gui.inputfield.DynamicTextElement.DynamicTextType;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.commands.CommandDispatcher;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.Localization;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.awt.AwtFactoryD;
import org.geogebra.desktop.gui.DynamicTextInputPane;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.inputbar.InputBarHelpPanelD;
import org.geogebra.desktop.gui.inputfield.AutoCompleteTextFieldD;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.geogebra.desktop.util.LoggerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * PRE-G9B-R5-B-R1: a runtime product-language change, made through the real
 * {@code Options → GeoCeDG options → Product language…} action in one application
 * session, leaves every command-surface consumer on the new language: Input Help
 * captions, entries, selection and syntax; autocomplete; alias lookup; display; and
 * errors ({@code T-DISPLAY}, {@code T-INPUT}, {@code T-AUTOCOMPLETE},
 * {@code T-SYNTAX}, {@code T-GEOCEDG}, {@code T-ERROR}, {@code T-DYNTEXT},
 * {@code T-L01}), while the construction, its XML and its undo history stay unchanged.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BR5BR1RuntimeLanguageSwitchTest {

	private static final Locale ENGLISH = Locale.ENGLISH;
	private static final Locale SPANISH = PreG9BR5BFingerprints.SPANISH;
	private static final List<String> FOCUSED = List.of("OrthogonalLine", "Circle", "Mirror",
			"LocusV2", "LocusLength", "SplineV2", "Length");
	private static final Map<String, String> FIRST_SYNTAX = Map.of(
			"en.OrthogonalLine", "PerpendicularLine( <Point>, <Line> )",
			"es.OrthogonalLine", "PerpendicularLine( <Punto>, <Lado (semi/recta o segmento)> )",
			"en.Circle", "Circle( <Point>, <Radius Number> )",
			"es.Circle", "Circle( <Punto>, <Número o valor numérico (radio)> )",
			"en.LocusV2", "LocusV2( <Dependent Point>, <Constrained Point> )",
			"es.LocusV2", "LocusV2( <Punto dependiente>, <Punto restringido> )");

	@Test
	void englishToSpanishRebuildsInputHelpAndAliasesForSpanish() throws Exception {
		assertSwitches(ENGLISH, SPANISH);
	}

	@Test
	void spanishToEnglishRebuildsInputHelpAndAliasesForEnglish() throws Exception {
		assertSwitches(SPANISH, ENGLISH);
	}

	@Test
	void englishSpanishEnglishAndBackStayCoherentInOneSession() throws Exception {
		assertSwitches(ENGLISH, SPANISH, ENGLISH, SPANISH);
	}

	@Test
	void switchingLanguageKeepsTheConstructionAndItsCanonicalPresentation()
			throws Exception {
		JSONObject fixture = PreG9BR5BFingerprints.baseFixture();
		AppGeoCeDG app = started(ENGLISH);
		PreG9BR5BCanonicalDisplayTest.build(app, false);
		app.setShowInputHelpPanel(true);
		UndoManagerD undo = (UndoManagerD) app.getKernel().getConstruction().getUndoManager();
		try (var baseline = undo.prepareUndoBaseline()) {
			undo.commitUndoBaseline(baseline);
		}
		final int history = undo.getHistorySize();
		CountDownLatch stored = new CountDownLatch(1);
		undo.addUndoInfoStoredListener(stored::countDown);
		app.setSaved();
		String xml = PreG9BR5BFingerprints.constructionXml(app);
		List<String> english = definitions(app);

		for (Locale language : new Locale[] {SPANISH, ENGLISH, SPANISH}) {
			chooseProductLanguage(app, language);
			String lang = language.getLanguage();
			assertEquals(xml, PreG9BR5BFingerprints.constructionXml(app), lang);
			assertTrue(app.isSaved(), "a language change saves nothing: " + lang);
			List<String> shown = definitions(app);
			for (int i = 0; i < english.size(); i++) {
				String witness = PreG9BR5BFingerprints.WITNESS[i][0];
				String expected = english.get(i);
				if (SPANISH.equals(language) && List.of("k", "ty").contains(witness)) {
					expected = expected.replace("sin(", "sen(");
				}
				assertEquals(expected, shown.get(i), lang + " " + witness);
			}
			Localization loc = app.getLocalization();
			assertEquals("LocusV2(Q, s, D)", definition(app, "L"));
			assertEquals("LocusLength(L)", definition(app, "M"));
			assertEquals("Length(L)", definition(app, "n"));
			assertEquals("SplineV2(l1, b)", definition(app, "S"));

			// T-INPUT: the lookup tables are those of a fresh start in this language
			app.getCommandDictionary();
			String state = app.getKernel().isGeoGebraCASready() ? ".casFilled" : ".fresh";
			String table = PreG9BR5BFingerprints.reverseTable(app);
			assertEquals(PreG9BR5BFingerprints.string(fixture, "reverseTables", lang + state,
					"sha256"), PreG9BR5BFingerprints.sha256(table), lang + state);

			// T-L01 across the runtime switch: aliases and placeholders of this language
			boolean spanish = SPANISH.equals(language);
			assertEquals(spanish ? "LugarGeométricoV2" : "LocusV2", loc.getCommand("LocusV2"));
			assertEquals(spanish ? "LongitudLugarGeométrico" : "LocusLength",
					loc.getCommand("LocusLength"));
			assertTrue(loc.getCommandSyntax("LocusV2").contains(spanish
					? "<Descriptor de dominio>" : "<Domain Descriptor>"), lang);
			assertTrue(loc.getCommandSyntax("LocusLength").contains(spanish
					? "<Punto semántico inicial>" : "<Start Semantic Point>"), lang);

			// T-ERROR: canonical head, prose of this language
			for (String command : List.of("Circle", "OrthogonalLine", "LocusV2")) {
				String error = loc.getCommandErrorMessageBuilder()
						.buildArgumentNumberError(command, 1);
				String base = PreG9BR5BFingerprints.string(fixture, "argumentNumberErrors",
						lang, command);
				assertEquals(spanish ? base.replace("Comando " + loc.getCommand(command) + ":",
						"Comando " + loc.getCommandHead(command) + ":").replace(
						loc.getCommand(command) + "( ", loc.getCommandHead(command) + "( ")
						: base, error, lang + " " + command);
			}

			// T-DYNTEXT: dynamic-text classification of the new language
			DynamicTextInputPane pane = new DynamicTextInputPane(app);
			assertEquals(DynamicTextType.FORMULA_TEXT,
					pane.insertDynamicText("FormulaText(p)", -1, null).getMode());
			assertEquals(DynamicTextType.DEFINITION,
					pane.insertDynamicText("Name(p)", -1, null).getMode());
		}
		assertEquals(xml, PreG9BR5BFingerprints.constructionXml(app));
		assertFalse(stored.await(2, TimeUnit.SECONDS), "undo point stored");
		assertEquals(history, undo.getHistorySize());

		// T-SHADOW still fails closed after the switches
		GeoElement circle = app.getKernel().lookupLabel("k");
		G9U1TestApp.eval(app, "Circle=5");
		String presented = circle.getRedefineString(false, true);
		CanonicalCommandSurfaceError shadowed = assertThrows(
				CanonicalCommandSurfaceError.class,
				() -> CanonicalCommandReentry.beforeParse(app.getKernel(), circle, presented));
		assertEquals(CanonicalCommandSurfaceError.Outcome.CANONICAL_HEAD_SHADOWED,
				shadowed.getOutcome());
	}

	private static void assertSwitches(Locale start, Locale... switches) throws Exception {
		AppGeoCeDG app = started(start);
		G9U1TestApp.eval(app, "c=Circle((0,0),2)");
		app.setShowInputHelpPanel(true);
		String xml = PreG9BR5BFingerprints.constructionXml(app);
		assertCoherent(app);
		for (Locale language : switches) {
			chooseProductLanguage(app, language);
			// the first use after the switch sees the new language, with nothing refreshed
			boolean spanish = SPANISH.equals(language);
			if (spanish) {
				assertEquals("Circle", app.getReverseCommand("Circunferencia"));
			} else {
				assertNull(app.getReverseCommand("Circunferencia"));
			}
			assertCoherent(app);
			assertEquals(xml, PreG9BR5BFingerprints.constructionXml(app));
			assertEquals("c: Circle((0, 0), 2)",
					app.getKernel().lookupLabel("c").getDefinitionForInputBar());
		}
	}

	/** Input Help, autocomplete and syntax all present one generation: the current one. */
	private static void assertCoherent(AppGeoCeDG app) throws Exception {
		Localization loc = app.getLocalization();
		String lang = app.getLocale().getLanguage();
		InputBarHelpPanelD help = (InputBarHelpPanelD) ((GuiManagerD) app.getGuiManager())
				.getInputHelpPanel();
		JTree tree = find(help, JTree.class);
		TreeNode root = (TreeNode) tree.getModel().getRoot();

		// captions of this language only
		Set<String> captions = new HashSet<>();
		for (int i = 0; i < CommandDispatcher.tableCount; i++) {
			captions.add(app.getKernel().getAlgebraProcessor().getSubCommandSetName(i));
		}
		assertEquals(loc.getMenu("MathematicalFunctions"), root.getChildAt(0).toString(), lang);
		assertEquals(loc.getMenu("AllCommands"), root.getChildAt(1).toString(), lang);
		for (int i = 2; i < root.getChildCount(); i++) {
			assertTrue(captions.contains(root.getChildAt(i).toString()),
					lang + ": " + root.getChildAt(i));
		}
		assertEquals(loc.getMenu("InputHelp"), field(help, "titleLabel", JLabel.class).getText());
		assertEquals(loc.getMenu("Paste"),
				field(help, "btnPaste", AbstractButton.class).getText());
		assertEquals(loc.getMenu("ShowOnlineHelp"),
				field(help, "btnOnlineHelp", AbstractButton.class).getText());

		// entries of this language: identity, canonical head and current alias
		Set<String> expected = new TreeSet<>();
		for (CanonicalCommandEntry entry : CanonicalCommandSurface.offeredEntries(app, false)) {
			expected.add(describe(entry));
		}
		Set<String> allCommands = new TreeSet<>();
		TreeNode all = root.getChildAt(1);
		for (int j = 0; j < all.getChildCount(); j++) {
			allCommands.add(describe(assertInstanceOf(CanonicalCommandEntry.class,
					((DefaultMutableTreeNode) all.getChildAt(j)).getUserObject())));
		}
		assertEquals(expected, allCommands, lang);
		List<CanonicalCommandEntry> leaves = new ArrayList<>();
		collect(root, leaves);
		for (CanonicalCommandEntry entry : leaves) {
			assertEquals(loc.getCommandHead(entry.getInternalName()), entry.getHead());
			assertEquals(loc.getCommand(entry.getInternalName()).trim(), entry.getAlias(),
					lang + ": " + entry.getInternalName());
		}

		// selection by identity shows the canonical head with this language's body
		JTextPane pane = find(help, JTextPane.class);
		for (String command : FOCUSED) {
			tree.clearSelection();
			help.focusCommand(command);
			DefaultMutableTreeNode selected = (DefaultMutableTreeNode)
					tree.getLastSelectedPathComponent();
			assertNotNull(selected, lang + " " + command);
			assertEquals(command, assertInstanceOf(CanonicalCommandEntry.class,
					selected.getUserObject()).getInternalName());
			List<String> lines = CanonicalCommandSurface.syntaxLines(loc, command, false);
			assertFalse(lines.isEmpty(), command);
			String shown = pane.getText().replace("\r\n", "\n");
			assertTrue(shown.startsWith(String.join("\n", lines)), lang + " " + command);
			assertTrue(lines.get(0).startsWith(loc.getCommandHead(command) + "( <"),
					lines.get(0));
			assertFalse(shown.contains(".Syntax"), shown);
			String first = FIRST_SYNTAX.get(lang + "." + command);
			if (first != null) {
				assertEquals(first, lines.get(0), lang + " " + command);
			}
		}

		// autocomplete finds this language's aliases and inserts canonical heads
		List<String> circle = completions(app, "Circunf");
		if (SPANISH.getLanguage().equals(lang)) {
			assertTrue(circle.contains(FIRST_SYNTAX.get("es.Circle")), circle.toString());
		} else {
			assertTrue(circle.isEmpty(), circle.toString());
		}
		assertTrue(completions(app, "Perp").contains(FIRST_SYNTAX.get(lang + ".OrthogonalLine")),
				lang);
		assertTrue(completions(app, "LocusV").contains(FIRST_SYNTAX.get(lang + ".LocusV2")),
				lang);
	}

	/** The real product action; only its chooser dialog is answered. */
	static void chooseProductLanguage(AppGeoCeDG app, Locale language) {
		String choice = SPANISH.equals(language) ? "Español" : "English";
		try (MockedStatic<JOptionPane> chooser = Mockito.mockStatic(JOptionPane.class)) {
			chooser.when(() -> JOptionPane.showInputDialog(any(), any(), any(), anyInt(), any(),
					any(), any())).thenReturn(choice);
			((GuiManagerGeoCeDG) app.getGuiManager()).getActionRegistry()
					.invoke("settings.product-language", null);
		}
		assertEquals(language.getLanguage(), app.getLocale().getLanguage());
	}

	/** An application started in a product language, as {@code --language} does. */
	static AppGeoCeDG started(Locale language) {
		AwtFactory.setPrototypeIfNull(new AwtFactoryD());
		if (Log.getLogger() == null) {
			Log.setLogger(new LoggerD());
		}
		AppGeoCeDG app = new AppGeoCeDG(new CommandLineArguments(new String[] {"--silent",
				"--enableLocusV2=true", "--language=" + language.getLanguage()}), new JPanel());
		app.setErrorDialogsActive(false);
		G9U1TestApp.withoutWindowDispatcher(app);
		assertEquals(language.getLanguage(), app.getLocale().getLanguage());
		app.buildApplicationPanel();
		app.setShowAlgebraInput(true, true);
		return app;
	}

	private static List<String> definitions(AppGeoCeDG app) {
		List<String> definitions = new ArrayList<>();
		for (String[] witness : PreG9BR5BFingerprints.WITNESS) {
			GeoElement geo = app.getKernel().lookupLabel(witness[0]);
			definitions.add(geo.getDefinition(StringTemplate.defaultTemplate) + " | "
					+ geo.getRedefineString(false, true));
		}
		return definitions;
	}

	private static String definition(AppGeoCeDG app, String label) {
		return app.getKernel().lookupLabel(label).getDefinition(StringTemplate.defaultTemplate);
	}

	private static String describe(CanonicalCommandEntry entry) {
		return entry.getInternalName() + "|" + entry.getHead() + "|" + entry.getAlias();
	}

	private static List<String> completions(AppGeoCeDG app, String word) throws Exception {
		AutoCompleteTextFieldD field = new AutoCompleteTextFieldD(20, app);
		field.setAutoComplete(true);
		field.setText(word);
		field.setCaretPosition(word.length());
		field.updateCurrentWord(false);
		Method reset = AutoCompleteTextFieldD.class.getDeclaredMethod("resetCompletions");
		reset.setAccessible(true);
		reset.invoke(field);
		List<String> completions = field.getCompletions();
		return completions == null ? List.of() : completions;
	}

	private static <T> T field(Object owner, String name, Class<T> type) throws Exception {
		Field field = owner.getClass().getDeclaredField(name);
		field.setAccessible(true);
		return type.cast(field.get(owner));
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
				T found = find((Container) component, type);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}
}
