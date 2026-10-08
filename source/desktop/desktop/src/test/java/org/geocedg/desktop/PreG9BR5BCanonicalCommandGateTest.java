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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.SortedSet;

import org.geocedg.common.main.command.CanonicalCommandHeadGate;
import org.geocedg.common.main.command.CanonicalCommandHeadGate.Failure;
import org.geocedg.common.main.command.CanonicalCommandHeadGate.Finding;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.App;
import org.geogebra.common.main.Localization;
import org.geogebra.common.main.settings.config.AppConfigDefault;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.headless.AppDNoGui;
import org.geogebra.desktop.main.LocalizationD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PRE-G9B-R5-B (ADR 0031 decisions 2, 4, 5 and 7): the canonical English authority,
 * the complete-inventory admissibility gate with its negative control
 * ({@code T-GATE}), unchanged lookups and reverse tables relative to
 * {@code P_DOTXML} ({@code T-INPUT}) and the {@code Perímetro} observation
 * ({@code T-COLLISION}).
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BR5BCanonicalCommandGateTest {

	private static final Locale[] LANGUAGES = {Locale.ENGLISH, PreG9BR5BFingerprints.SPANISH};

	@Test
	void gatePassesOverTheCompleteInventoryInBothLanguagesAndBothTableStates() {
		for (Locale language : LANGUAGES) {
			AppGeoCeDG app = application(language);
			CanonicalCommandHeadGate gate = new CanonicalCommandHeadGate(app);
			gate.captureState("fresh", false);
			app.getKernel().getGeoGebraCAS();
			gate.captureState("casFilled", true);

			assertEquals(List.of(), gate.findings(), language.toString());
			assertTrue(gate.getHeads().keySet().containsAll(gate.getDisplayed()),
					language.toString());
			// complete inventory: 566 stored names, 515 with E(k), 488 displayed (3D included;
			// PRE-G9B-R6-plus-E2 adds AlignedDimension and LinearDimension)
			assertEquals(566, Commands.values().length);
			assertEquals(515, gate.getHeads().size(), language.toString());
			assertEquals(488, gate.getDisplayed().size(), language.toString());
			for (String name : List.of("LocusV2", "LocusLength", "SplineV2", "Length",
					"OrthogonalLine", "Mirror", "LaTeX", "Defined", "PolyLine")) {
				assertTrue(gate.getDisplayed().contains(name), name);
			}
		}
	}

	@Test
	void gateDetectsTheRetainedFrenchRetargetAsNegativeControl() {
		AppDNoGui french = classicApplication();
		french.setLanguage(Locale.FRENCH);
		french.getCommandDictionary();
		CanonicalCommandHeadGate gate = new CanonicalCommandHeadGate(french);
		gate.captureState("fresh", false);

		List<Finding> findings = gate.findings();
		assertTrue(findings.stream().anyMatch(finding -> "Intersection".equals(
				finding.getStoredName()) && finding.getFailure()
						== Failure.HEAD_NOT_SELF_RESOLVING), findings.toString());
		assertEquals("Intersect", french.getReverseCommand("Intersection"));
	}

	@Test
	void canonicalEnglishNameIsTheEnglishCommandBundleValue() throws IOException {
		Properties english = new Properties();
		try (InputStream base = resource("command.properties");
				InputStream overlay = resource("command_en.properties")) {
			english.load(new InputStreamReader(base,
					StandardCharsets.UTF_8));
			english.load(new InputStreamReader(overlay,
					StandardCharsets.UTF_8));
		}
		JSONObject fixture = PreG9BR5BFingerprints.baseFixture();
		for (Locale language : LANGUAGES) {
			Localization loc = application(language).getLocalization();
			for (Commands command : Commands.values()) {
				String name = command.name();
				String canonical = loc.getCanonicalEnglishCommand(name);
				assertEquals(english.getProperty(name), canonical, name);
				String baseEnglishDisplay = PreG9BR5BFingerprints.string(fixture,
						"commandNames", "en", name);
				assertEquals(canonical == null ? name : canonical, baseEnglishDisplay, name);
			}
			assertNull(loc.getCanonicalEnglishCommand("Circle.Syntax"));
			assertEquals("PerpendicularLine", loc.getCanonicalEnglishCommand("OrthogonalLine"));
			assertEquals("Reflect", loc.getCanonicalEnglishCommand("Mirror"));
			assertEquals("FormulaText", loc.getCanonicalEnglishCommand("LaTeX"));
			assertEquals("IsDefined", loc.getCanonicalEnglishCommand("Defined"));
			assertEquals("Polyline", loc.getCanonicalEnglishCommand("PolyLine"));
			for (String divergent : List.of("Binomial", "IntersectConic", "mean", "stdev",
					"mad", "stdevp")) {
				assertNotEquals(loc.getEnglishCommand(divergent),
						loc.getCanonicalEnglishCommand(divergent), divergent);
			}
		}
	}

	@Test
	void reverseTablesAndLookupRegimesAreIdenticalToTheImplementationBase() {
		JSONObject fixture = PreG9BR5BFingerprints.baseFixture();
		SortedSet<String> tokens = PreG9BR5BFingerprints.tokenUniverse();
		assertSummary(fixture, String.join("\n", tokens) + "\n", "tokenUniverse");
		assertSummary(fixture, PreG9BR5BFingerprints.scriptResolution(tokens),
				"scriptResolution");
		assertSummary(fixture, PreG9BR5BFingerprints.xmlExactResolution(tokens),
				"xmlExactResolution");
		for (Locale language : LANGUAGES) {
			String lang = language.getLanguage();
			AppGeoCeDG app = application(language);
			assertSummary(fixture, PreG9BR5BFingerprints.reverseTable(app),
					"reverseTables", lang + ".fresh");
			assertSummary(fixture, PreG9BR5BFingerprints.userResolution(app, tokens),
					"userResolution", lang + ".fresh");
			app.getKernel().getGeoGebraCAS();
			assertNotNull(app.getCommandDictionaryCAS());
			assertSummary(fixture, PreG9BR5BFingerprints.reverseTable(app),
					"reverseTables", lang + ".casFilled");
			assertSummary(fixture, PreG9BR5BFingerprints.userResolution(app, tokens),
					"userResolution", lang + ".casFilled");
		}
	}

	@Test
	void everyCanonicalHeadAndEverySpanishAliasIsAcceptedAsItsIdentity() {
		for (Locale language : LANGUAGES) {
			AppGeoCeDG app = application(language);
			Localization loc = app.getLocalization();
			List<String> mismatches = new ArrayList<>();
			for (Commands command : app.getOfferedCommands(false)) {
				String identity = Commands.englishToInternal(command).name();
				String head = loc.getCommandHead(command.name());
				if (!identity.equals(identity(app.getReverseCommand(head)))) {
					mismatches.add(head + " -> " + app.getReverseCommand(head));
				}
				String alias = loc.getCommand(command.name());
				String resolved = identity(app.getReverseCommand(alias));
				boolean perimetro = "Perímetro".equals(alias);
				if (!perimetro && !identity.equals(resolved)) {
					mismatches.add(alias + " -> " + resolved);
				}
				if (!identity.equals(identity(app.getReverseCommand(command.name())))) {
					mismatches.add(command.name() + " -> "
							+ app.getReverseCommand(command.name()));
				}
			}
			assertEquals(List.of(), mismatches, language.toString());
		}
	}

	@Test
	void perimetroObservationIsUnchangedAndCanonicalHeadsAreUnaffected() {
		AppGeoCeDG app = application(PreG9BR5BFingerprints.SPANISH);
		assertEquals("Perimeter", app.getReverseCommand("Perímetro"));
		assertEquals("Perimeter", app.getReverseCommand("Perimeter"));
		assertEquals("Circumference", app.getReverseCommand("Circumference"));
		GeoNumeric fresh = (GeoNumeric) evaluate(app,
				"poly=Polígono((0,0),(3,0),(0,4))", "per=Perímetro(poly)");
		assertEquals(12, fresh.getDouble(), 1e-12);
		assertEquals("Perimeter(poly)", fresh.getDefinitionForInputBar()
				.replace("per = ", ""));

		app.getKernel().getGeoGebraCAS();
		app.getCommandDictionaryCAS();
		assertEquals("Circumference", app.getReverseCommand("Perímetro"));
		assertEquals("Perimeter", app.getReverseCommand("Perimeter"));
		assertEquals("Circumference", app.getReverseCommand("Circumference"));
		GeoNumeric filled = (GeoNumeric) evaluate(app, "per2=Perímetro(poly)");
		assertEquals(12, filled.getDouble(), 1e-12);
		GeoNumeric circle = (GeoNumeric) evaluate(app, "c=Circunferencia((0,0),1)",
				"cc=Perímetro(c)");
		assertEquals(2 * Math.PI, circle.getDouble(), 1e-12);
		String shown = circle.getDefinitionForInputBar();
		assertTrue(shown.contains("Circumference(c)"), shown);
		assertFalse(shown.contains("Perímetro"), shown);
	}

	private static void assertSummary(JSONObject fixture, String canonical, String... path) {
		String[] lines = append(path, "lines");
		String[] sha = append(path, "sha256");
		assertEquals(PreG9BR5BFingerprints.integer(fixture, lines),
				PreG9BR5BFingerprints.lineCount(canonical), String.join(".", path));
		assertEquals(PreG9BR5BFingerprints.string(fixture, sha),
				PreG9BR5BFingerprints.sha256(canonical), String.join(".", path));
	}

	private static String[] append(String[] path, String last) {
		String[] result = Arrays.copyOf(path, path.length + 1);
		result[path.length] = last;
		return result;
	}

	private static GeoElement evaluate(App app, String... inputs) {
		GeoElementND[] result = null;
		for (String input : inputs) {
			result = app.getKernel().getAlgebraProcessor().processAlgebraCommand(input,
					false);
			assertNotNull(result, input);
		}
		return result[0].toGeoElement();
	}

	private static String identity(String resolved) {
		Commands command = resolved == null ? null : Commands.stringToCommand(resolved);
		return command == null ? resolved : Commands.englishToInternal(command).name();
	}

	private static InputStream resource(String name) {
		InputStream stream = PreG9BR5BCanonicalCommandGateTest.class.getResourceAsStream(
				"/org/geogebra/common/jre/properties/" + name);
		assertNotNull(stream, name);
		return stream;
	}

	static AppGeoCeDG application(Locale language) {
		AppGeoCeDG app = G9U1TestApp.create();
		// The product language switch rebuilds the reverse command table.
		app.setLanguage(language);
		app.getCommandDictionary();
		assertEquals(language.getLanguage(), app.getLocalization().getLocale().getLanguage());
		assertTrue(app.getLocalization().isCanonicalEnglishCommandHeads());
		return app;
	}

	static AppDNoGui classicApplication() {
		Log previousLogger = Log.getLogger();
		try {
			AppDNoGui app = new AppDNoGui(new LocalizationD(3), true, new AppConfigDefault());
			assertFalse(app.getLocalization().isCanonicalEnglishCommandHeads());
			return app;
		} finally {
			Log.setLogger(previousLogger);
		}
	}
}
