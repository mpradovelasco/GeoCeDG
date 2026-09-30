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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.geogebra.common.jre.io.MyXMLioJre;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.algos.AlgoDependentNumber;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoVector;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.App;
import org.geogebra.common.main.settings.config.AppConfigDefault;
import org.geogebra.common.plugin.Operation;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.headless.AppDNoGui;
import org.geogebra.desktop.io.AtomicDocumentFileWriter;
import org.geogebra.desktop.main.LocalizationD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * Maintenance DOT/XML: a {@code Dot} expression authored in either product
 * language survives native and compatibility save/reopen as the same dynamic
 * dependency on its inputs.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class DotXmlLocaleIndependentPersistenceTest {

	private static final Locale SPANISH = new Locale("es");
	private static final Pattern EXPRESSION_ELEMENT =
			Pattern.compile("<expression label=\"[^\"]*\" exp=\"[^\"]*\"/>");
	private static final List<String> PERSISTED_EXPRESSIONS = List.of(
			"<expression label=\"d\" exp=\"Dot(u, v)\"/>",
			"<expression label=\"e\" exp=\"(2 * Dot(u, v))\"/>");

	@TempDir
	Path temporaryDirectory;

	@Test
	void spanishNativeDocumentReopensAsTheSameDependencyInBothLanguages()
			throws Exception {
		AppGeoCeDG author = application(SPANISH);
		buildDotConstruction(author, "ProductoEscalar(u, v)");
		// PRE-G9B-R5-B (ADR 0031): GeoCeDG presents the canonical English head; the
		// Spanish alias above still creates the same Dot dependency.
		assertEquals("Dot(u, v)", find(author, "d")
				.getDefinition(StringTemplate.defaultTemplate));
		Path target = temporaryDirectory.resolve("dot-es.cedg");

		assertTrue(((GuiManagerGeoCeDG) author.getGuiManager()).saveAsTo(target.toFile()));

		String xml = constructionXml(target);
		assertEquals(PERSISTED_EXPRESSIONS, expressionElements(xml));
		assertFalse(xml.contains("ProductoEscalar"), xml);
		for (Locale reopenLanguage : new Locale[] {Locale.ENGLISH, SPANISH}) {
			AppGeoCeDG reopened = application(reopenLanguage);
			assertTrue(reopened.loadFile(target.toFile(), false));
			assertReopenedConstructionRecomputes(reopened);
		}
	}

	@Test
	void englishAndSpanishNativeDocumentsPersistTheSameDotExpressions()
			throws Exception {
		AppGeoCeDG english = application(Locale.ENGLISH);
		buildDotConstruction(english, "Dot(u, v)");
		Path englishTarget = temporaryDirectory.resolve("dot-en.cedg");
		assertTrue(((GuiManagerGeoCeDG) english.getGuiManager())
				.saveAsTo(englishTarget.toFile()));

		AppGeoCeDG spanish = application(SPANISH);
		buildDotConstruction(spanish, "ProductoEscalar(u, v)");
		Path spanishTarget = temporaryDirectory.resolve("dot-es.cedg");
		assertTrue(((GuiManagerGeoCeDG) spanish.getGuiManager())
				.saveAsTo(spanishTarget.toFile()));

		assertEquals(PERSISTED_EXPRESSIONS, expressionElements(constructionXml(englishTarget)));
		assertEquals(PERSISTED_EXPRESSIONS, expressionElements(constructionXml(spanishTarget)));

		AppGeoCeDG reopened = application(Locale.ENGLISH);
		assertTrue(reopened.loadFile(englishTarget.toFile(), false));
		assertReopenedConstructionRecomputes(reopened);
	}

	@Test
	void spanishCompatibilityDocumentReopensAsTheSameDependency() throws Exception {
		// GeoCeDG saves only native documents, so the .ggb comes from the Classic writer.
		AppDNoGui classic = classicApplication();
		classic.setLanguage(SPANISH);
		// The headless language switch does not rebuild the reverse command table.
		classic.getCommandDictionary();
		buildDotConstruction(classic, "ProductoEscalar(u, v)");
		Path target = temporaryDirectory.resolve("dot-es.ggb");
		AtomicDocumentFileWriter.write(target, temporary ->
				((MyXMLioJre) classic.getXMLio()).writeGeoGebraFile(temporary.toFile()));
		assertFalse(application(SPANISH).saveGeoGebraFile(
				temporaryDirectory.resolve("rejected.ggb").toFile()));

		assertEquals(PERSISTED_EXPRESSIONS, expressionElements(constructionXml(target)));
		for (Locale reopenLanguage : new Locale[] {Locale.ENGLISH, SPANISH}) {
			AppGeoCeDG reopened = application(reopenLanguage);
			assertTrue(reopened.loadFile(target.toFile(), false));
			assertReopenedConstructionRecomputes(reopened);
		}
	}

	private static AppGeoCeDG application(Locale language) {
		AppGeoCeDG app = G9U1TestApp.create();
		// The product language switch rebuilds the reverse command table.
		app.setLanguage(language);
		assertEquals(language.getLanguage(),
				app.getLocalization().getLocale().getLanguage());
		return app;
	}

	private static AppDNoGui classicApplication() {
		Log previousLogger = Log.getLogger();
		try {
			return new AppDNoGui(new LocalizationD(3), true, new AppConfigDefault());
		} finally {
			Log.setLogger(previousLogger);
		}
	}

	private static void buildDotConstruction(App app, String dotInput) {
		assertInstanceOf(GeoVector.class, evaluate(app, "u=(1,2)"));
		assertInstanceOf(GeoVector.class, evaluate(app, "v=(3,4)"));
		assertDotOfInputs(app, (GeoNumeric) evaluate(app, "d=" + dotInput), 11);
		assertEquals(22, ((GeoNumeric) evaluate(app, "e=2 " + dotInput)).getDouble(), 0);
	}

	private static void assertReopenedConstructionRecomputes(App app) {
		GeoNumeric dot = (GeoNumeric) find(app, "d");
		GeoNumeric scaled = (GeoNumeric) find(app, "e");
		assertDotOfInputs(app, dot, 11);
		assertFalse(scaled.isIndependent());
		assertEquals(22, scaled.getDouble(), 0);

		GeoVector u = (GeoVector) find(app, "u");
		u.setCoords(3, 4, 0);
		u.updateCascade();
		assertEquals(25, dot.getDouble(), 0);
		assertEquals(50, scaled.getDouble(), 0);

		GeoVector v = (GeoVector) find(app, "v");
		v.setCoords(-4, 3, 0);
		v.updateCascade();
		assertEquals(0, dot.getDouble(), 0);
		v.setCoords(-1, -2, 0);
		v.updateCascade();
		assertEquals(-11, dot.getDouble(), 0);
		assertEquals(-22, scaled.getDouble(), 0);
	}

	private static void assertDotOfInputs(App app, GeoNumeric dot, double value) {
		assertFalse(dot.isIndependent());
		AlgoElement parent = dot.getParentAlgorithm();
		assertInstanceOf(AlgoDependentNumber.class, parent);
		assertEquals(Operation.DOT, dot.getDefinition().getOperation());
		assertEquals(Set.of(find(app, "u"), find(app, "v")), Set.of(parent.getInput()));
		assertEquals(value, dot.getDouble(), 0);
	}

	private static GeoElement evaluate(App app, String command) {
		GeoElementND[] result = app.getKernel().getAlgebraProcessor()
				.processAlgebraCommand(command, false);
		assertNotNull(result, command);
		assertEquals(1, result.length, command);
		return result[0].toGeoElement();
	}

	private static GeoElement find(App app, String label) {
		GeoElement result = app.getKernel().lookupLabel(label);
		assertNotNull(result, label);
		return result;
	}

	private static String constructionXml(Path archive) throws IOException {
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

	private static List<String> expressionElements(String xml) {
		List<String> elements = new ArrayList<>();
		Matcher matcher = EXPRESSION_ELEMENT.matcher(xml);
		while (matcher.find()) {
			elements.add(matcher.group());
		}
		return elements;
	}
}
