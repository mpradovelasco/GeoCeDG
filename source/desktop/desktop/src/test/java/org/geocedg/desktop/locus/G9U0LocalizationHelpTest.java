/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.jre.headless.AppCommon;
import org.junit.jupiter.api.Test;

/** Exact L4 localization, provenance and contextual-help scenarios. */
class G9U0LocalizationHelpTest {

	@Test
	void l01CommandNamesAndSyntaxAreLocalizedInEnglishAndSpanish() {
		AppCommon app = AppCommonFactory.create(new AppConfigGeoCeDG(true));
		app.setLocale(Locale.ENGLISH);
		String englishLocus = app.getLocalization().getCommand("LocusV2");
		String englishLength = app.getLocalization().getCommand("LocusLength");
		String englishLocusSyntax =
				app.getLocalization().getCommandSyntax("LocusV2");
		final String englishLengthSyntax =
				app.getLocalization().getCommandSyntax("LocusLength");
		app.setLocale(new Locale("es"));
		String spanishLocus = app.getLocalization().getCommand("LocusV2");
		String spanishLength = app.getLocalization().getCommand("LocusLength");
		final String spanishLocusSyntax =
				app.getLocalization().getCommandSyntax("LocusV2");
		final String spanishLengthSyntax =
				app.getLocalization().getCommandSyntax("LocusLength");

		assertEquals("LocusV2", englishLocus);
		assertEquals("LocusLength", englishLength);
		assertEquals("LugarGeométricoV2", spanishLocus);
		assertEquals("LongitudLugarGeométrico", spanishLength);
		assertTrue(englishLocusSyntax.contains("<Dependent Point>"));
		assertTrue(englishLocusSyntax.contains("<Domain Descriptor>"));
		assertTrue(englishLengthSyntax.contains("<Start Semantic Point>"));
		assertTrue(spanishLocusSyntax.contains("<Punto dependiente>"));
		assertTrue(spanishLocusSyntax.contains("<Descriptor de dominio>"));
		assertTrue(spanishLengthSyntax.contains("<Punto semántico inicial>"));
		assertNotEquals(englishLocusSyntax, spanishLocusSyntax);
		assertNotEquals(englishLengthSyntax, spanishLengthSyntax);
	}

	@Test
	void l02RichStatusAndDiagnosticLabelsAreLocalized()
			throws IOException, ClassNotFoundException {
		AppCommon app = AppCommonFactory.create(new AppConfigGeoCeDG(true));
		List<String> keys = new ArrayList<>(List.of(
				"LocusV2.Results.Field.Status",
				"LocusV2.Results.Field.Completeness",
				"LocusV2.Results.Field.CompletenessMethod",
				"LocusV2.Results.Field.Geometry",
				"LocusV2.Results.Field.Currentness",
				"LocusV2.Results.Field.Isolation",
				"LocusV2.Results.Field.Continuation",
				"LocusV2.Results.Field.LineageEvent",
				"LocusV2.Results.Field.ContinuationEstablished",
				"LocusV2.Results.Field.OverlapStatus",
				"LocusV2.Results.Field.PairIsolation",
				"LocusV2.Results.Field.SemanticEvaluations",
				"LocusV2.Results.Field.MaximumIsolationDepth",
				"LocusV2.Results.Field.Diagnostics",
				"LocusV2.Results.Field.PointAdmissible",
				"LocusV2.FeatureDisabled", "LocusV2.InvalidToken",
				"LocusV2.UnsupportedTarget", "LocusV2.MetricUnavailable",
				"LocusV2.Results.Value.True",
				"LocusV2.Results.Value.False",
				"LocusV2.Results.Value.NOT_APPLICABLE",
				"LocusV2.Results.Value.UNAVAILABLE"));
		String semantic = "org.geocedg.common.kernel.locus."
				+ "LocusSemanticMetadata2D$";
		String metric = "org.geocedg.common.kernel.locus.metric.";
		String intersection = "org.geocedg.common.kernel.locus.intersection."
				+ "IntersectionSemanticMetadata2D$";
		String[] enumClasses = {
				semantic + "ConstructionFidelity",
				semantic + "NumericGuarantee", semantic + "Regularity",
				metric + "MetricComputationStatus", metric + "MetricCoverage",
				metric + "MetricRectifiability", metric + "TraversalOutcome",
				metric + "MetricEvaluatorMethod2D", metric + "MetricMethod2D",
				metric + "MetricRepresentationRole2D", metric + "MetricUnit2D",
				metric + "MetricErrorEvidenceScope", metric + "MetricValueKind",
				metric + "MetricErrorAmountKind", metric + "MetricDiagnosticCode2D",
				intersection + "ComputationStatus",
				intersection + "Completeness",
				intersection + "CompletenessMethod",
				intersection + "GeometryKind", intersection + "Currentness",
				intersection + "SupportLevel", intersection + "ContactClass",
				intersection + "MultiplicityStatus",
				intersection + "DomainLocation", intersection + "IdentityStatus",
				intersection + "LineageEventKind",
				intersection + "SolverMethod",
				intersection + "LocalIsolationStatus",
				intersection + "PairCoverageStatus",
				intersection + "PairUniquenessStatus",
				intersection + "PairIsolationMethod",
				intersection + "OverlapStatus",
				intersection + "OverlapRelationKind",
				intersection + "DiagnosticCode"
		};
		for (String className : enumClasses) {
			for (Object constant : Class.forName(className).getEnumConstants()) {
				keys.add("LocusV2.Results.Value."
						+ ((Enum<?>) constant).name());
			}
		}
		Properties defaults = menuBundle("menu.properties");
		Properties englishBundle = menuBundle("menu_en.properties");
		Properties spanishBundle = menuBundle("menu_es.properties");
		List<String> distinctKeys = keys.stream().distinct().toList();
		assertTrue(missingKeys(defaults, distinctKeys).isEmpty(),
				"default: " + missingKeys(defaults, distinctKeys));
		assertTrue(missingKeys(englishBundle, distinctKeys).isEmpty(),
				"en: " + missingKeys(englishBundle, distinctKeys));
		assertTrue(missingKeys(spanishBundle, distinctKeys).isEmpty(),
				"es: " + missingKeys(spanishBundle, distinctKeys));
		for (String key : distinctKeys) {
			app.setLocale(Locale.ENGLISH);
			String english = app.getLocalization().getMenu(key);
			app.setLocale(new Locale("es"));
			String spanish = app.getLocalization().getMenu(key);
			assertFalse(english.isBlank(), key);
			assertFalse(spanish.isBlank(), key);
			assertFalse(english.equals(key), key);
			assertFalse(spanish.equals(key), key);
		}
		String dialog = Files.readString(repositoryRoot().resolve(
				"source/desktop/desktop/src/main/java/org/geocedg/desktop/"
						+ "GeoCeDGLocusV2Dialogs.java"), StandardCharsets.UTF_8);
		assertTrue(dialog.contains("localizeEnum(diagnostic.getCode())"));
		assertTrue(dialog.contains("appendWorkSummary(summary, value.getWork())"));
		assertTrue(dialog.contains("localizeEnum(value.getCurrentness())"));
		assertTrue(dialog.contains("localizeEnum(overlap.getStatus())"));
		assertTrue(dialog.contains("localizeEnum(solution.getLineage()"));
		assertFalse(dialog.contains(".append(diagnostic)"));
		assertFalse(dialog.contains("diagnostic.getMessage()"));
		assertFalse(dialog.contains("overlap.getDiagnostic()"));
		assertFalse(dialog.contains("pair.getDiagnostic()"));
		assertFalse(dialog.contains("getContactClass().toString()"));
	}

	@Test
	void l03GeoCeDGIconHasOwnedProvenanceAndTextFallback() throws IOException {
		assertNotNull(G9U0LocalizationHelpTest.class.getResource(
				"/org/geogebra/common/icons/svg/web/toolIcons/mode_locusv2.svg"));
		String assets = Files.readString(repositoryRoot().resolve(
				"geocedg/resources/assets-manifest.yml"), StandardCharsets.UTF_8);
		assertTrue(assets.contains("geocedg.locus-v2-tool-icon"));
		assertTrue(assets.contains("GeoCeDG-authored G9U0 semantic locus icon"));
		assertTrue(assets.contains("accessible text fallback"));
	}

	@Test
	void l04LegacyAndV2HelpRemainContextuallyDistinct() {
		AppCommon app = AppCommonFactory.create(new AppConfigGeoCeDG(true));
		app.setLocale(Locale.ENGLISH);
		String legacySyntax = app.getLocalization().getCommandSyntax("Locus");
		String v2Syntax = app.getLocalization().getCommandSyntax("LocusV2");
		String v2 = app.getLocalization().getMenu("LocusV2.Help");
		String point = app.getLocalization().getMenu("LocusV2.Point.Help");
		String result = app.getLocalization().getMenu("LocusV2.Results.Help");
		assertTrue(legacySyntax.contains("Point Creating Locus Line"));
		assertFalse(legacySyntax.contains("Domain Descriptor"));
		assertTrue(v2Syntax.contains("Domain Descriptor"));
		assertFalse(v2.equals("LocusV2.Help"));
		assertFalse(point.equals(v2));
		assertFalse(result.equals(v2));
		assertTrue(point.contains("branch key"));
		assertTrue(result.contains("exact token"));
	}

	private static Path repositoryRoot() {
		Path candidate = Path.of("").toAbsolutePath().normalize();
		while (candidate != null) {
			if (Files.isRegularFile(candidate.resolve("AGENTS.md"))) {
				return candidate;
			}
			candidate = candidate.getParent();
		}
		throw new IllegalStateException("GeoCeDG repository root not found");
	}

	private static Properties menuBundle(String name) throws IOException {
		Properties properties = new Properties();
		try (java.io.Reader reader = Files.newBufferedReader(repositoryRoot()
				.resolve("source/shared/common-jre/src/main/resources/"
						+ "org/geogebra/common/jre/properties/" + name),
				StandardCharsets.UTF_8)) {
			properties.load(reader);
		}
		return properties;
	}

	private static List<String> missingKeys(Properties properties,
			List<String> keys) {
		return keys.stream().filter(key -> !properties.containsKey(key)).toList();
	}
}
