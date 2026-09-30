/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.geocedg.common.main.command.CanonicalCommandEntry;
import org.geocedg.common.main.command.CanonicalCommandSurface;
import org.geogebra.common.gui.inputfield.DynamicTextElement;
import org.geogebra.common.gui.inputfield.DynamicTextElement.DynamicTextType;
import org.geogebra.common.gui.inputfield.DynamicTextProcessor;
import org.geogebra.common.kernel.CircularDefinitionException;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.arithmetic.Command;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.FunctionVariable;
import org.geogebra.common.kernel.arithmetic.MyList;
import org.geogebra.common.kernel.commands.CAScmdProcessor;
import org.geogebra.common.kernel.commands.CommandNotFoundError;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.App;
import org.geogebra.common.main.Localization;
import org.geogebra.common.main.MyError;
import org.geogebra.common.main.error.ErrorHandler;
import org.geogebra.common.main.error.ErrorHelper;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.geogebra.common.plugin.EventType;
import org.geogebra.common.plugin.Operation;
import org.geogebra.common.util.AsyncOperation;
import org.geogebra.desktop.gui.DynamicTextInputPane;
import org.geogebra.desktop.headless.AppDNoGui;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-R5-B (ADR 0031 decisions 1, 8, 11 and 12): canonical English heads at
 * every display, editable and error site under a Spanish UI ({@code T-DISPLAY},
 * {@code T-ERROR}, {@code T-GEOCEDG}), unchanged English output, dynamic-text
 * classification ({@code T-DYNTEXT}) and persistence identical to {@code P_DOTXML}
 * ({@code T-XML}).
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BR5BCanonicalDisplayTest {

	private static final String[] TEMPLATES = {"default", "redefine", "inputBar",
			"editor", "latex", "value"};
	/** Witnesses whose definitions contain a localized function name. */
	private static final List<String> FUNCTION_NAME_WITNESSES = List.of("k", "ty");

	@TempDir
	Path temporaryDirectory;

	@Test
	void englishDisplayEditableTextAndSyntaxAreUnchangedFromTheBase() {
		JSONObject fixture = PreG9BR5BFingerprints.baseFixture();
		AppGeoCeDG app = application(Locale.ENGLISH);
		build(app, false);
		for (String[] witness : PreG9BR5BFingerprints.WITNESS) {
			GeoElement geo = app.getKernel().lookupLabel(witness[0]);
			for (String template : TEMPLATES) {
				assertEquals(PreG9BR5BFingerprints.string(fixture, "display", "en",
						witness[0], template), render(geo, template), witness[0] + "." + template);
			}
			assertEquals(PreG9BR5BFingerprints.string(fixture, "display", "en", witness[0],
					"description"), render(geo, "description"), witness[0]);
		}
		assertEquals(PreG9BR5BFingerprints.string(fixture, "syntax", "en.full", "sha256"),
				PreG9BR5BFingerprints.sha256(syntaxLines(app)));
		for (String command : List.of("Circle", "OrthogonalLine", "Mirror", "LocusV2",
				"LocusLength", "SplineV2", "Length")) {
			assertEquals(PreG9BR5BFingerprints.string(fixture, "argumentNumberErrors", "en",
					command), app.getLocalization().getCommandErrorMessageBuilder()
							.buildArgumentNumberError(command, 1), command);
		}
		TreeSet<String> heads = new TreeSet<>();
		CanonicalEntries.heads(app, heads);
		assertEquals(PreG9BR5BFingerprints.strings(fixture, "dictionaries", "en.algebra"),
				new ArrayList<>(heads));
	}

	@Test
	void spanishDisplayAndEditableTextUseCanonicalEnglishHeads() {
		JSONObject fixture = PreG9BR5BFingerprints.baseFixture();
		AppGeoCeDG app = application(PreG9BR5BFingerprints.SPANISH);
		build(app, true);
		for (String[] witness : PreG9BR5BFingerprints.WITNESS) {
			String label = witness[0];
			GeoElement geo = app.getKernel().lookupLabel(label);
			for (String template : TEMPLATES) {
				String expected = PreG9BR5BFingerprints.string(fixture, "display", "en",
						label, template);
				if (FUNCTION_NAME_WITNESSES.contains(label)) {
					expected = localizeSine(expected);
				}
				assertEquals(expected, render(geo, template), label + "." + template);
			}
			String baseSpanishDescription = PreG9BR5BFingerprints.string(fixture, "display",
					"es", label, "description");
			boolean prose = !baseSpanishDescription.equals(PreG9BR5BFingerprints.string(
					fixture, "display", "es", label, "default"));
			String expectedDescription = prose ? baseSpanishDescription
					: PreG9BR5BFingerprints.string(fixture, "display", "en", label,
							"description");
			if (!prose && FUNCTION_NAME_WITNESSES.contains(label)) {
				expectedDescription = localizeSine(expectedDescription);
			}
			assertEquals(expectedDescription, render(geo, "description"), label);
		}
		GeoElement circle = app.getKernel().lookupLabel("k");
		assertEquals("Circle((0, 0), sen(1))",
				circle.getDefinition(StringTemplate.defaultTemplate));
		assertEquals("Circunferencia con centro (0, 0) y radio sen(1)",
				render(circle, "description"));
		for (String[] witness : PreG9BR5BFingerprints.WITNESS) {
			GeoElement geo = app.getKernel().lookupLabel(witness[0]);
			String shown = geo.getRedefineString(false, true);
			for (String alias : List.of("Perpendicular(", "Refleja(", "Bisectriz(",
					"Poligonal(", "FórmulaTexto(", "EstáDefinido(", "media(",
					"Circunferencia(", "ProductoEscalar(", "Si(", "LugarGeométricoV2(",
					"LongitudLugarGeométrico(", "Longitud(", "GráficoCircular(", "Nombre(")) {
				assertFalse(shown.contains(alias), witness[0] + ": " + shown);
			}
		}
	}

	@Test
	void geoCeDGCommandsAndTheLengthAdapterFollowTheHostPolicy() {
		AppGeoCeDG app = application(PreG9BR5BFingerprints.SPANISH);
		build(app, true);
		assertEquals("LocusV2(Q, s, D)", definition(app, "L"));
		assertEquals("LocusLength(L)", definition(app, "M"));
		assertEquals("Length(L)", definition(app, "n"));
		assertEquals("SplineV2(l1, b)", definition(app, "S"));
		Localization loc = app.getLocalization();
		assertEquals("LugarGeométricoV2", loc.getCommand("LocusV2"));
		assertEquals("LongitudLugarGeométrico", loc.getCommand("LocusLength"));
		assertEquals("SplineV2", loc.getCommand("SplineV2"));
		for (String command : List.of("LocusV2", "LocusLength", "SplineV2", "Length")) {
			assertEquals(command, loc.getCommandHead(command));
			assertTrue(loc.getCommandSyntax(command).startsWith(command + "( <"), command);
		}
		assertTrue(loc.getCommandSyntax("LocusV2").contains("<Punto dependiente>"));
		assertTrue(loc.getCommandSyntax("Length").startsWith("Length( <Objeto> )"),
				loc.getCommandSyntax("Length"));
	}

	@Test
	void spanishSyntaxHasCanonicalHeadsAndUnchangedSpanishBodies() {
		JSONObject fixture = PreG9BR5BFingerprints.baseFixture();
		AppGeoCeDG app = application(PreG9BR5BFingerprints.SPANISH);
		Localization loc = app.getLocalization();
		String base = PreG9BR5BFingerprints.string(fixture, "syntax", "es.text");
		Commands[] commands = Commands.values();
		StringBuilder expected = new StringBuilder();
		int start = 0;
		for (int i = 0; i < commands.length; i++) {
			String name = commands[i].name();
			assertTrue(base.startsWith(name + "\t", start), name);
			int end = i + 1 < commands.length
					? base.indexOf("\n" + commands[i + 1].name() + "\t", start) + 1
					: base.length();
			String[] fields = base.substring(start + name.length() + 1, end - 1).split("\t");
			String alias = loc.getCommand(name);
			String head = loc.getCommandHead(name);
			expected.append(name).append('\t').append(replaceHeads(fields[0], alias, head))
					.append('\t').append(replaceHeads(fields[1], alias, head)).append('\n');
			start = end;
		}
		assertEquals(expected.toString(), syntaxLines(app));
		assertEquals("PerpendicularLine( <Punto>, <Lado (semi/recta o segmento)> )",
				loc.getCommandSyntax("OrthogonalLine").split("\n")[0]);
		assertEquals("Circle( <Punto>, <Número o valor numérico (radio)> )",
				loc.getCommandSyntax("Circle").split("\n")[0]);
	}

	@Test
	void errorMessagesNameTheCommandByItsCanonicalHeadWithSpanishProse() {
		JSONObject fixture = PreG9BR5BFingerprints.baseFixture();
		AppGeoCeDG app = application(PreG9BR5BFingerprints.SPANISH);
		Localization loc = app.getLocalization();
		for (String command : List.of("Circle", "OrthogonalLine", "Mirror", "LocusV2",
				"LocusLength", "SplineV2", "Length")) {
			String alias = loc.getCommand(command);
			String head = loc.getCommandHead(command);
			String expected = PreG9BR5BFingerprints.string(fixture, "argumentNumberErrors",
					"es", command).replace("Comando " + alias + ":", "Comando " + head + ":")
					.replace(alias + "( ", head + "( ");
			String shown = loc.getCommandErrorMessageBuilder()
					.buildArgumentNumberError(command, 1);
			assertEquals(expected, shown, command);
			assertTrue(shown.contains("Número ilegal de argumentos"), shown);
		}
		String unknown = new CommandNotFoundError(loc,
				new Command(app.getKernel(), "OrthogonalLine", false)).getMessage();
		assertTrue(unknown.endsWith(" : PerpendicularLine"), unknown);
		String casOnly = null;
		try {
			new CAScmdProcessor(app.getKernel()).process(
					new Command(app.getKernel(), "Solve", false), null);
		} catch (MyError | CircularDefinitionException e) {
			casOnly = e.getMessage();
		}
		assertNotNull(casOnly);
		assertTrue(casOnly.contains("Solve") && !casOnly.contains("Resuelve"), casOnly);
		List<String> shown = new ArrayList<>();
		ErrorHelper.handleCommandError(loc, "Perpendicular", new ErrorHandler() {
			@Override
			public void showError(String msg) {
				shown.add(msg);
			}

			@Override
			public void showCommandError(String command, String message) {
				shown.add(command + "|" + message);
			}

			@Override
			public String getCurrentCommand() {
				return null;
			}

			@Override
			public boolean onUndefinedVariables(String string,
					AsyncOperation<String[]> callback) {
				return false;
			}

			@Override
			public void resetError() {
				// not needed
			}
		});
		assertEquals(1, shown.size());
		assertTrue(shown.get(0).startsWith("OrthogonalLine|"), shown.get(0));
		assertTrue(shown.get(0).contains("\nPerpendicularLine\n"), shown.get(0));
		assertTrue(shown.get(0).contains("PerpendicularLine( <Punto>"), shown.get(0));
	}

	@Test
	void everyKernelPrintSiteUsesTheCanonicalHeadUnderSpanish() throws Exception {
		AppGeoCeDG app = application(PreG9BR5BFingerprints.SPANISH);
		build(app, true);
		Command command = new Command(app.getKernel(), "OrthogonalLine", false);
		command.addArgument(new ExpressionNode(app.getKernel(),
				app.getKernel().lookupLabel("C")));
		assertEquals("PerpendicularLine(C)", command.toString(StringTemplate.defaultTemplate));
		assertEquals("OrthogonalLine(C)", command.toString(StringTemplate.xmlTemplate));
		ExpressionNode data = new ExpressionNode(app.getKernel(), new MyList(app.getKernel()),
				Operation.DATA, new FunctionVariable(app.getKernel()));
		assertTrue(data.toString(StringTemplate.defaultTemplate).startsWith("DataFunction["),
				data.toString(StringTemplate.defaultTemplate));
		assertEquals("DatosFunción", app.getLocalization().getCommand("DataFunction"));
		assertEquals("PieChart", app.getKernel().lookupLabel("pc")
				.toValueString(StringTemplate.defaultTemplate));
		assertEquals("PenStrokeBezier", app.getLocalization().getCommandHead("PenStrokeBezier"));

		// A tool (macro) name is a user identifier: verbatim even when it is a command key.
		Macro tool = new Macro(app.getKernel(), "Mirror",
				new GeoElement[] {app.getKernel().lookupLabel("A"),
						app.getKernel().lookupLabel("B")},
				new GeoElement[] {app.getKernel().lookupLabel("f")});
		app.getKernel().addMacro(tool);
		GeoElementND[] used = app.getKernel().getAlgebraProcessor()
				.processAlgebraCommand("tf=Mirror(C,Dp)", false);
		assertNotNull(used);
		assertEquals("Mirror(C, Dp)", used[0].toGeoElement()
				.getDefinition(StringTemplate.defaultTemplate));
		Command toolCommand = new Command(app.getKernel(), "Mirror", false);
		toolCommand.addArgument(new ExpressionNode(app.getKernel(),
				app.getKernel().lookupLabel("C")));
		assertEquals("Mirror(C)", toolCommand.toString(StringTemplate.defaultTemplate));
	}

	@Test
	void dynamicTextClassificationIsUnchangedUnderSpanish() {
		JSONObject fixture = PreG9BR5BFingerprints.baseFixture();
		for (Locale language : new Locale[] {Locale.ENGLISH, PreG9BR5BFingerprints.SPANISH}) {
			AppGeoCeDG app = application(language);
			build(app, PreG9BR5BFingerprints.SPANISH.equals(language));
			for (String label : List.of("t2", "t3")) {
				List<String> parts = new ArrayList<>();
				for (DynamicTextElement element : new DynamicTextProcessor(app)
						.buildDynamicTextList((GeoText) app.getKernel().lookupLabel(label))) {
					parts.add(element.toString());
				}
				assertEquals(PreG9BR5BFingerprints.strings(fixture, "dynamicText",
						language.getLanguage(), label), parts, label);
			}
			DynamicTextInputPane pane = new DynamicTextInputPane(app);
			assertEquals(DynamicTextType.FORMULA_TEXT,
					pane.insertDynamicText("FormulaText(p)", -1, null).getMode());
			assertEquals(DynamicTextType.DEFINITION,
					pane.insertDynamicText("Name(p)", -1, null).getMode());
		}
	}

	@Test
	void freshConstructionsPersistTheBaseCommandIdentitiesInBothLanguages() {
		JSONObject fixture = PreG9BR5BFingerprints.baseFixture();
		for (Locale language : new Locale[] {Locale.ENGLISH, PreG9BR5BFingerprints.SPANISH}) {
			boolean spanish = PreG9BR5BFingerprints.SPANISH.equals(language);
			AppGeoCeDG app = application(language);
			build(app, spanish);
			String core = PreG9BR5BFingerprints.constructionCore(app);
			assertEquals(PreG9BR5BFingerprints.string(fixture, "constructionCore",
					language.getLanguage()), core, language.toString());
			assertTrue(core.contains("<expression label=\"d\" exp=\"Dot(u, v)\"/>"), core);
			assertTrue(core.contains("<command name=\"OrthogonalLine\">"), core);
			assertFalse(core.contains("PerpendicularLine"), core);
		}
	}

	@Test
	void historicalSpanishDocumentReopensAndSavesIdenticallyToTheBase() throws Exception {
		JSONObject fixture = PreG9BR5BFingerprints.baseFixture();
		Path document = temporaryDirectory.resolve("historical-es.cedg");
		try (InputStream input = PreG9BR5BCanonicalDisplayTest.class.getResourceAsStream(
				PreG9BR5BFingerprints.HISTORICAL_DOCUMENT)) {
			assertNotNull(input);
			Files.copy(input, document);
		}
		String expectedXml = PreG9BR5BFingerprints.string(fixture, "historicalDocument",
				"reopenedConstructionXml");
		for (Locale language : new Locale[] {Locale.ENGLISH, PreG9BR5BFingerprints.SPANISH}) {
			AppGeoCeDG app = application(language);
			assertTrue(app.loadFile(document.toFile(), false));
			assertEquals(expectedXml, PreG9BR5BFingerprints.constructionXml(app),
					language.toString());
			assertEquals(PreG9BR5BFingerprints.string(fixture, "historicalDocument",
					"constructionCore"), PreG9BR5BFingerprints.constructionCore(app));
			for (String[] witness : PreG9BR5BFingerprints.WITNESS) {
				assertEquals(PreG9BR5BFingerprints.string(fixture, "historicalDocument",
						"reopenedValues", witness[0]), app.getKernel().lookupLabel(witness[0])
								.toValueString(StringTemplate.maxPrecision), witness[0]);
			}
			assertEquals(PreG9BR5BFingerprints.string(fixture, "historicalDocument",
					"clickScript"), app.getKernel().lookupLabel("btn")
							.getScript(EventType.CLICK).getInternalText());
			assertEquals("PerpendicularLine(C, f)", definition(app, "p"));
			assertEquals("LocusV2(Q, s, D)", definition(app, "L"));
			File saved = temporaryDirectory.resolve("resaved-" + language.getLanguage()
					+ ".cedg").toFile();
			assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(saved));
			assertEquals(expectedXml, construction(zipXml(saved.toPath())),
					language.toString());
		}
	}

	@Test
	void classicProfileKeepsTheInheritedLocalizedHeads() {
		AppDNoGui classic = PreG9BR5BCanonicalCommandGateTest.classicApplication();
		classic.setLanguage(PreG9BR5BFingerprints.SPANISH);
		classic.getCommandDictionary();
		GeoElementND[] circle = classic.getKernel().getAlgebraProcessor()
				.processAlgebraCommand("c=Circunferencia((0,0),2)", false);
		assertNotNull(circle);
		assertEquals("Circunferencia((0, 0), 2)", circle[0].toGeoElement()
				.getDefinition(StringTemplate.defaultTemplate));
		Localization loc = classic.getLocalization();
		for (Commands command : Commands.values()) {
			assertEquals(loc.getCommand(command.name()), loc.getCommandHead(command.name()));
		}
		assertTrue(loc.getCommandSyntax("Circle").startsWith("Circunferencia( <Punto>"));
	}

	private static String replaceHeads(String syntax, String alias, String head) {
		StringBuilder replaced = new StringBuilder();
		for (String line : syntax.split("\n", -1)) {
			if (replaced.length() > 0) {
				replaced.append('\n');
			}
			replaced.append(line.startsWith(alias + "(")
					? head + line.substring(alias.length()) : line);
		}
		return replaced.toString();
	}

	private static String localizeSine(String english) {
		return english.replace("sin(", "sen(").replace("\\operatorname{sin}",
				"\\operatorname{sen}");
	}

	private static String render(GeoElement geo, String template) {
		switch (template) {
		case "default":
			return String.valueOf(geo.getDefinition(StringTemplate.defaultTemplate));
		case "redefine":
			return String.valueOf(geo.getRedefineString(false, true));
		case "inputBar":
			return String.valueOf(geo.getDefinitionForInputBar());
		case "editor":
			return String.valueOf(geo.getDefinitionForEditor());
		case "latex":
			return String.valueOf(geo.getDefinition(StringTemplate.latexTemplate));
		case "description":
			return String.valueOf(geo.getDefinitionDescription(StringTemplate.defaultTemplate));
		default:
			return String.valueOf(geo.toValueString(StringTemplate.defaultTemplate));
		}
	}

	private static String syntaxLines(App app) {
		Localization loc = app.getLocalization();
		StringBuilder text = new StringBuilder();
		for (Commands command : Commands.values()) {
			String k = command.name();
			text.append(k).append('\t').append(loc.getCommandSyntax(k)).append('\t')
					.append(loc.getCommandSyntaxCAS(k)).append('\n');
		}
		return text.toString();
	}

	private static String definition(App app, String label) {
		return app.getKernel().lookupLabel(label).getDefinition(StringTemplate.defaultTemplate);
	}

	static void build(App app, boolean spanish) {
		for (String input : PreG9BR5BFingerprints.witnessInputs(spanish)) {
			assertNotNull(app.getKernel().getAlgebraProcessor().processAlgebraCommand(input,
					false), input);
		}
	}

	private static AppGeoCeDG application(Locale language) {
		return PreG9BR5BCanonicalCommandGateTest.application(language);
	}

	private static String zipXml(Path archive) throws IOException {
		try (InputStream input = Files.newInputStream(archive);
				ZipInputStream zip = new ZipInputStream(input)) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				if ("geogebra.xml".equals(entry.getName())) {
					return new String(zip.readAllBytes(), StandardCharsets.UTF_8);
				}
			}
		}
		throw new IOException("Missing geogebra.xml in " + archive);
	}

	private static String construction(String xml) {
		int start = xml.indexOf("<construction");
		String end = "</construction>";
		return xml.substring(start, xml.indexOf(end, start) + end.length());
	}

	/** Canonical Input Help heads. */
	private static final class CanonicalEntries {
		static void heads(App app, TreeSet<String> heads) {
			for (CanonicalCommandEntry entry : CanonicalCommandSurface.offeredEntries(app,
					false)) {
				heads.add(entry.getHead());
			}
		}
	}
}
