/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.geocedg.common.main.command.CanonicalCommandSurface;
import org.geocedg.common.main.command.CanonicalCommandSurfaceError;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.gui.dialog.options.model.ScriptInputModel;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.App;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.geogebra.common.plugin.EventType;
import org.geogebra.common.plugin.ScriptType;
import org.geogebra.common.plugin.script.GgbScript;
import org.geogebra.common.plugin.script.Script;
import org.geogebra.desktop.headless.AppDNoGui;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PRE-G9B-R5-B (ADR 0031 decision 10, {@code T-SCRIPT}): the script editor
 * displays canonical English heads, an untouched displayed script keeps its stored
 * text, an edited script saves canonical-first without retargeting, and
 * {@code SCRIPT_TOKEN_AMBIGUOUS} is defined. SCRIPT resolution itself is proven
 * unchanged by the lookup fingerprints of {@link PreG9BR5BCanonicalCommandGateTest}.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BR5BCanonicalScriptTest {

	@Test
	void editorDisplaysCanonicalHeadsInBothLanguages() {
		JSONObject fixture = PreG9BR5BFingerprints.baseFixture();
		String english = PreG9BR5BFingerprints.string(fixture, "scripts", "en", "display");
		for (Locale language : new Locale[] {Locale.ENGLISH, PreG9BR5BFingerprints.SPANISH}) {
			AppGeoCeDG app = PreG9BR5BCanonicalCommandGateTest.application(language);
			PreG9BR5BCanonicalDisplayTest.build(app,
					PreG9BR5BFingerprints.SPANISH.equals(language));
			assertEquals(english, new GgbScript(app, PreG9BR5BFingerprints.STORED_SCRIPT)
					.getText(), language.toString());
		}
	}

	@Test
	void untouchedDisplayedScriptKeepsItsStoredTextInBothLanguages() {
		JSONObject fixture = PreG9BR5BFingerprints.baseFixture();
		for (Locale language : new Locale[] {Locale.ENGLISH, PreG9BR5BFingerprints.SPANISH}) {
			AppGeoCeDG app = PreG9BR5BCanonicalCommandGateTest.application(language);
			PreG9BR5BCanonicalDisplayTest.build(app,
					PreG9BR5BFingerprints.SPANISH.equals(language));
			GeoElement button = button(app, PreG9BR5BFingerprints.STORED_SCRIPT);
			Script stored = button.getScript(EventType.CLICK);
			ScriptInputModel model = model(app, button);
			String[] shown = new String[1];
			model.setListener(new ScriptInputModel.IScriptInputListener() {
				@Override
				public void setInput(String text, ScriptType type) {
					shown[0] = text;
				}

				@Override
				public Object updatePanel(Object[] geos) {
					return null;
				}
			});
			model.updatePanel();

			model.processInput(shown[0], ScriptType.GGBSCRIPT);

			assertSame(stored, button.getScript(EventType.CLICK), language.toString());
			assertEquals(PreG9BR5BFingerprints.STORED_SCRIPT,
					button.getScript(EventType.CLICK).getInternalText());
			// The base rewrote the stored text on an untouched save.
			String baseSave = PreG9BR5BFingerprints.string(fixture, "scripts",
					language.getLanguage(), "untouchedSave");
			assertTrue(!baseSave.equals(PreG9BR5BFingerprints.STORED_SCRIPT), baseSave);
		}
	}

	@Test
	void editedScriptSavesCanonicalFirstWithoutRetargeting() {
		AppGeoCeDG app = PreG9BR5BCanonicalCommandGateTest.application(
				PreG9BR5BFingerprints.SPANISH);
		PreG9BR5BCanonicalDisplayTest.build(app, true);
		GeoElement button = button(app, PreG9BR5BFingerprints.STORED_SCRIPT);
		ScriptInputModel model = model(app, button);
		String displayed = button.getScript(EventType.CLICK).getText();

		model.processInput(displayed + "\nPuntoMedio(A, B)\ncircle(A, 1)",
				ScriptType.GGBSCRIPT);

		String saved = button.getScript(EventType.CLICK).getInternalText();
		assertEquals("OrthogonalLine(C, f)\nOrthogonalLine(C, f)\nLaTeX(p)\n"
				+ "SplineV2({A, B, C, Dp}, 3)\nLocusV2(Q, s, D)\nPerimeter(c)\n"
				+ "Circumference(c)\nMirror(C, f)\nMidpoint(A, B)\nCircle(A, 1)", saved);
		String[] before = (PreG9BR5BFingerprints.STORED_SCRIPT + "\nMidpoint(A, B)\nCircle(A, 1)")
				.split("\n");
		String[] after = saved.split("\n");
		for (int i = 0; i < before.length; i++) {
			assertEquals(Commands.lookupInternal(head(before[i])),
					Commands.lookupInternal(head(after[i])), before[i]);
		}
		assertEquals("Perimeter(c)", GgbScript.localizedScript2Script(app, "Perimeter(c)"));
		assertEquals("Circumference(c)",
				GgbScript.localizedScript2Script(app, "Perímetro(c)"));
		assertEquals("PerpendicularLine(C, f)",
				GgbScript.script2LocalizedScript(app, "OrthogonalLine(C, f)"));
	}

	@Test
	void ambiguousScriptTokenIsTypedAndRejectedWithoutSaving() {
		AppCommon french = AppCommonFactory.create(new AppConfigGeoCeDG(true));
		french.setLocale(Locale.FRENCH);
		french.getCommandDictionary();
		CanonicalCommandSurfaceError error = assertThrows(CanonicalCommandSurfaceError.class,
				() -> CanonicalCommandSurface.delocalizeScriptCommandToken(french,
						"Intersection"));
		assertEquals(CanonicalCommandSurfaceError.Outcome.SCRIPT_TOKEN_AMBIGUOUS,
				error.getOutcome());
		assertEquals("Intersection", error.getExpectedIdentity());
		assertEquals("Intersect", error.getConflict());
		assertTrue(error.getMessage().startsWith("SCRIPT_TOKEN_AMBIGUOUS"), error.getMessage());

		GeoElement button = button(french, "Midpoint(A, B)");
		Script stored = button.getScript(EventType.CLICK);
		model(french, button).processInput("Intersection(l1, l2)", ScriptType.GGBSCRIPT);
		assertSame(stored, button.getScript(EventType.CLICK));

		AppDNoGui classic = PreG9BR5BCanonicalCommandGateTest.classicApplication();
		classic.setLanguage(PreG9BR5BFingerprints.SPANISH);
		classic.getCommandDictionary();
		assertEquals("Circumference(c)", GgbScript.localizedScript2Script(classic,
				GgbScript.script2LocalizedScript(classic, "Perimeter(c)")));
	}

	private static String head(String line) {
		return line.substring(0, line.indexOf('('));
	}

	private static GeoElement button(App app, String script) {
		GeoElement button = app.getKernel().getAlgebraProcessor()
				.processAlgebraCommand("btn=Button(\"Run\")", false)[0].toGeoElement();
		button.setClickScript(app.createScript(ScriptType.GGBSCRIPT, script, false));
		assertNotNull(button.getScript(EventType.CLICK));
		return button;
	}

	private static ScriptInputModel model(App app, GeoElement button) {
		ScriptInputModel model = new ScriptInputModel(app, EventType.CLICK, "OnClick");
		model.setGeos(new Object[] {button});
		return model;
	}
}
