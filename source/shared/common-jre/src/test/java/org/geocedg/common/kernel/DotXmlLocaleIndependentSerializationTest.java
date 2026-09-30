/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.algos.AlgoDependentNumber;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoVector;
import org.geogebra.common.plugin.Operation;
import org.junit.jupiter.api.Test;

/**
 * Maintenance DOT/XML: the persisted head of {@link Operation#DOT} must not depend
 * on the UI language, while localized presentation templates keep the current
 * localized head.
 */
class DotXmlLocaleIndependentSerializationTest extends BaseUnitTest {

	private static final Locale SPANISH = new Locale("es");
	private static final Pattern EXPRESSION_ELEMENT =
			Pattern.compile("<expression label=\"[^\"]*\" exp=\"[^\"]*\"/>");

	@Test
	void spanishXmlUsesTheInternalDotHead() {
		useLanguage(SPANISH);
		GeoNumeric dot = buildDotConstruction("ProductoEscalar(u, v)");

		assertDotOfInputs(dot, 11);
		assertEquals("Dot(u, v)", dot.getDefinition(StringTemplate.xmlTemplate));
		String xml = getApp().getXML();
		assertTrue(xml.contains("<expression label=\"d\" exp=\"Dot(u, v)\"/>"), xml);
		assertFalse(xml.contains("ProductoEscalar"), xml);
	}

	@Test
	void englishAndSpanishPersistTheSameDotExpressions() {
		useLanguage(Locale.ENGLISH);
		buildDotConstruction("Dot(u, v)");
		List<String> english = expressionElements(getApp().getXML());

		getKernel().clearConstruction(true);
		useLanguage(SPANISH);
		buildDotConstruction("ProductoEscalar(u, v)");
		List<String> spanish = expressionElements(getApp().getXML());

		getKernel().clearConstruction(true);
		buildDotConstruction("Dot(u, v)");
		List<String> spanishWithEnglishInput = expressionElements(getApp().getXML());

		assertEquals(List.of("<expression label=\"d\" exp=\"Dot(u, v)\"/>",
				"<expression label=\"e\" exp=\"(2 * Dot(u, v))\"/>"), english);
		assertEquals(english, spanish);
		assertEquals(english, spanishWithEnglishInput);
	}

	@Test
	void localizedPresentationTemplatesKeepTheCurrentHead() {
		useLanguage(SPANISH);
		GeoNumeric spanishDot = buildDotConstruction("ProductoEscalar(u, v)");
		assertEquals("ProductoEscalar(u, v)",
				spanishDot.getDefinition(StringTemplate.defaultTemplate));
		assertEquals("ProductoEscalar(u, v)",
				spanishDot.getDefinition(StringTemplate.editTemplate));
		assertTrue(spanishDot.getDefinition(StringTemplate.latexTemplate)
				.contains("\\operatorname{ProductoEscalar}"));
		assertTrue(spanishDot.getDefinition(StringTemplate.mathmlTemplate)
				.contains("<scalarproduct/>"));

		getKernel().clearConstruction(true);
		useLanguage(Locale.ENGLISH);
		GeoNumeric englishDot = buildDotConstruction("Dot(u, v)");
		assertEquals("Dot(u, v)", englishDot.getDefinition(StringTemplate.defaultTemplate));
		assertEquals("Dot(u, v)", englishDot.getDefinition(StringTemplate.editTemplate));
		assertTrue(englishDot.getDefinition(StringTemplate.latexTemplate)
				.contains("\\operatorname{Dot}"));
	}

	@Test
	void nonLocalizingTemplatesUseTheInternalHeadInEveryLanguage() {
		useLanguage(SPANISH);
		GeoNumeric dot = buildDotConstruction("ProductoEscalar(u, v)");

		assertEquals("Dot(u, v)", dot.getDefinition(StringTemplate.noLocalDefault));
		assertEquals("Dot(u, v)", dot.getDefinition(StringTemplate.maxPrecision));
		assertEquals("Dot(u, v)", dot.getDefinition(StringTemplate.casCopyTemplate));
	}

	@Test
	void ifNeighbourAlreadyFollowsTheSameTemplateConvention() {
		useLanguage(SPANISH);
		GeoElement function = add("f(x)=Si(x > 0, x)");

		String display = function.toValueString(StringTemplate.defaultTemplate);
		assertTrue(display.startsWith("Si("), display);
		String xml = getApp().getXML();
		Matcher persisted = Pattern.compile("<expression label=\"f\"[^>]*>").matcher(xml);
		assertTrue(persisted.find(), xml);
		assertTrue(persisted.group().contains("If["), persisted.group());
		assertFalse(persisted.group().contains("Si("), persisted.group());
	}

	@Test
	void reloadInEitherLanguageKeepsTheDependencyAndRecomputes() {
		useLanguage(SPANISH);
		buildDotConstruction("ProductoEscalar(u, v)");
		String saved = getApp().getXML();

		for (Locale reopenLanguage : new Locale[] {Locale.ENGLISH, SPANISH}) {
			useLanguage(reopenLanguage);
			getApp().setXML(saved, true);
			assertReloadedConstructionRecomputes();
		}
	}

	@Test
	void undoRestoreKeepsTheDependency() {
		activateUndo();
		useLanguage(SPANISH);
		buildDotConstruction("ProductoEscalar(u, v)");
		getApp().storeUndoInfo();
		add("w=(5,6)");
		getApp().storeUndoInfo();

		getKernel().undo();

		assertReloadedConstructionRecomputes();
	}

	@Test
	void historicalLocalizedHeadIsNotReinterpretedByTheXmlReader() {
		useLanguage(SPANISH);
		buildDotConstruction("ProductoEscalar(u, v)");
		String historical = getApp().getXML()
				.replace("exp=\"Dot(u, v)\"", "exp=\"ProductoEscalar(u, v)\"");
		assertTrue(historical.contains("exp=\"ProductoEscalar(u, v)\""), historical);

		for (Locale reopenLanguage : new Locale[] {Locale.ENGLISH, SPANISH}) {
			useLanguage(reopenLanguage);
			getApp().setXML(historical, true);
			GeoNumeric degraded = (GeoNumeric) lookup("d");
			assertNotNull(degraded);
			assertTrue(degraded.isIndependent());
			assertEquals(11, degraded.getDouble(), 0);
		}
	}

	private void assertReloadedConstructionRecomputes() {
		GeoNumeric dot = (GeoNumeric) lookup("d");
		GeoNumeric scaled = (GeoNumeric) lookup("e");
		assertDotOfInputs(dot, 11);
		assertEquals(22, scaled.getDouble(), 0);
		assertTrue(containsDot(scaled.getDefinition()));

		GeoVector v = (GeoVector) lookup("v");
		v.setCoords(2, -1, 0);
		v.updateCascade();
		assertEquals(0, dot.getDouble(), 0);
		assertEquals(0, scaled.getDouble(), 0);

		GeoVector u = (GeoVector) lookup("u");
		u.setCoords(3, 4, 0);
		u.updateCascade();
		v.setCoords(-3, -4, 0);
		v.updateCascade();
		assertEquals(-25, dot.getDouble(), 0);
		assertEquals(-50, scaled.getDouble(), 0);
		assertFalse(dot.isIndependent());
	}

	private GeoNumeric buildDotConstruction(String dotInput) {
		assertInstanceOf(GeoVector.class, add("u=(1,2)"));
		assertInstanceOf(GeoVector.class, add("v=(3,4)"));
		GeoNumeric dot = add("d=" + dotInput);
		GeoNumeric scaled = add("e=2 " + dotInput);
		assertNotNull(dot, dotInput);
		assertNotNull(scaled, dotInput);
		return dot;
	}

	private void assertDotOfInputs(GeoNumeric dot, double expectedValue) {
		assertNotNull(dot);
		assertFalse(dot.isIndependent());
		AlgoElement parent = dot.getParentAlgorithm();
		assertInstanceOf(AlgoDependentNumber.class, parent);
		assertEquals(Operation.DOT, dot.getDefinition().getOperation());
		assertEquals(Set.of(lookup("u"), lookup("v")), Set.of(parent.getInput()));
		assertEquals(expectedValue, dot.getDouble(), 0);
	}

	private static boolean containsDot(ExpressionNode definition) {
		return definition != null && definition.any(value -> value.isExpressionNode()
				&& ((ExpressionNode) value).getOperation() == Operation.DOT);
	}

	/**
	 * Switches the UI language and refreshes the reverse command table, as the
	 * product language switch does; a bare locale change keeps the previous table.
	 */
	private void useLanguage(Locale locale) {
		getApp().setLocale(locale);
		getApp().getCommandDictionary();
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
