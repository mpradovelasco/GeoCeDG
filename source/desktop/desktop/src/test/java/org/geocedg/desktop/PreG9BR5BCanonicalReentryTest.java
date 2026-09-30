/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.geocedg.common.kernel.spatial.identity.SpatialRedefineAssessment;
import org.geocedg.common.main.command.CanonicalCommandReentry;
import org.geocedg.common.main.command.CanonicalCommandSurfaceError;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.error.ErrorHandler;
import org.geogebra.common.util.AsyncOperation;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PRE-G9B-R5-B (ADR 0031 decision 6): presented canonical English editable text
 * re-enters as the same command identity ({@code T-REDEFINE}), and label,
 * function-variable and macro shadowing fail closed with
 * {@code CANONICAL_HEAD_SHADOWED} without construction, XML or undo mutation
 * ({@code T-SHADOW}).
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BR5BCanonicalReentryTest {

	/** One witness per English-versus-internal group, the GeoCeDG commands and Dot/If. */
	private static final String[] REPRESENTATIVES = {"p", "m", "bi", "mb", "ar", "q",
			"t1", "i1", "ty", "ma", "ex", "ov", "uv", "q1", "fl", "mu", "pp", "c", "L", "M",
			"n", "S", "d", "g"};

	@Test
	void presentedEditableTextSubmittedUnchangedKeepsIdentityInBothLanguages() {
		for (Locale language : new Locale[] {Locale.ENGLISH, PreG9BR5BFingerprints.SPANISH}) {
			AppGeoCeDG app = application(language);
			PreG9BR5BCanonicalDisplayTest.build(app,
					PreG9BR5BFingerprints.SPANISH.equals(language));
			for (String label : REPRESENTATIVES) {
				GeoElement geo = app.getKernel().lookupLabel(label);
				String identity = identity(geo);
				String command = commandNames(coreOf(app, label));
				String presented = geo.getDefinitionForInputBar();
				List<String> errors = redefine(app, geo, presented);
				assertEquals(List.of(), errors, label + ": " + presented);
				GeoElement redefined = app.getKernel().lookupLabel(label);
				assertEquals(identity, identity(redefined), label);
				assertEquals(command, commandNames(coreOf(app, label)), label);
				assertEquals(presented, redefined.getDefinitionForInputBar(), label);
			}
		}
	}

	@Test
	void argumentOnlyEditsKeepTheCommandIdentity() {
		AppGeoCeDG app = application(
				PreG9BR5BFingerprints.SPANISH);
		PreG9BR5BCanonicalDisplayTest.build(app, true);
		eval(app, "l2={A,B,Dp,C}");
		String[][] edits = {
				{"p", "PerpendicularLine(C, f)", "PerpendicularLine(Dp, f)", "OrthogonalLine"},
				{"m", "Reflect(C, f)", "Reflect(Dp, f)", "Mirror"},
				{"bi", "AngleBisector(A, C, B)", "AngleBisector(A, Dp, B)",
						"AngularBisector"},
				{"q", "Polyline(A, B, C)", "Polyline(A, B, Dp)", "PolyLine"},
				{"t1", "FormulaText(p)", "FormulaText(m)", "LaTeX"},
				{"i1", "IsDefined(A)", "IsDefined(B)", "Defined"},
				{"ov", "PerpendicularVector(u)", "PerpendicularVector(v)",
						"OrthogonalVector"},
				{"c", "Circle(A, 2)", "Circle(A, 3)", "Circle"},
				{"S", "SplineV2(l1, b)", "SplineV2(l2, b)", "SplineV2"},
				{"M", "LocusLength(L)", "LocusLength(S)", "LocusLength"}};
		for (String[] edit : edits) {
			GeoElement geo = app.getKernel().lookupLabel(edit[0]);
			String presented = geo.getRedefineString(false, true);
			assertEquals(edit[1], presented, edit[0]);
			assertEquals(List.of(), redefine(app, geo, edit[2]), edit[0]);
			GeoElement redefined = app.getKernel().lookupLabel(edit[0]);
			assertEquals(edit[3], identity(redefined), edit[0]);
			assertEquals(edit[2], redefined.getRedefineString(false, true), edit[0]);
			assertTrue(coreOf(app, edit[0]).contains("<command name=\"" + edit[3] + "\">"),
					coreOf(app, edit[0]));
		}
	}

	@Test
	void labelShadowingFailsClosedWithoutMutation() throws Exception {
		AppGeoCeDG app = application(
				PreG9BR5BFingerprints.SPANISH);
		eval(app, "A=(0,0)", "c=Circunferencia(A,2)", "Circle=5");
		GeoElement circle = app.getKernel().lookupLabel("c");
		assertEquals("Circle(A, 2)", circle.getRedefineString(false, true));
		assertShadowedWithoutMutation(app, circle, circle.getRedefineString(false, true),
				"Circle", "Circle");

		List<String> rewritten = redefine(app, circle, "Circunferencia(A, 3)");
		assertEquals(List.of(), rewritten);
		assertEquals("Circle(A, 3)", app.getKernel().lookupLabel("c")
				.getRedefineString(false, true));
	}

	@Test
	void functionVariableShadowingFailsClosedWithoutMutation() throws Exception {
		AppGeoCeDG app = application(
				PreG9BR5BFingerprints.SPANISH);
		eval(app, "A=(0,0)", "B=(3,4)", "h(Distance)=Distancia(A,B)+Distance");
		GeoElement function = app.getKernel().lookupLabel("h");
		assertEquals(7, function.toGeoElement().getKernel().getAlgebraProcessor()
				.evaluateToDouble("h(2)"), 1e-12);
		String presented = function.getDefinitionForInputBar();
		assertTrue(presented.contains("Distance(A, B)"), presented);
		assertShadowedWithoutMutation(app, function, presented, "Distance", "Distance");
	}

	@Test
	void macroShadowingFailsClosedWithoutMutation() throws Exception {
		AppGeoCeDG app = application(
				PreG9BR5BFingerprints.SPANISH);
		eval(app, "A=(0,0)", "B=(2,0)", "c=Circunferencia(A,2)", "g=Recta(A,B)");
		Macro macro = new Macro(app.getKernel(), "Circle",
				new GeoElement[] {app.getKernel().lookupLabel("A"),
						app.getKernel().lookupLabel("B")},
				new GeoElement[] {app.getKernel().lookupLabel("g")});
		app.getKernel().addMacro(macro);
		GeoElement circle = app.getKernel().lookupLabel("c");
		assertShadowedWithoutMutation(app, circle, circle.getRedefineString(false, true),
				"Circle", "Circle");
	}

	private static void assertShadowedWithoutMutation(AppGeoCeDG app, GeoElement geo,
			String presented, String head, String identity) throws Exception {
		UndoManagerD undo = (UndoManagerD) app.getKernel().getConstruction().getUndoManager();
		try (var baseline = undo.prepareUndoBaseline()) {
			undo.commitUndoBaseline(baseline);
		}
		final int history = undo.getHistorySize();
		String xml = PreG9BR5BFingerprints.constructionXml(app);
		AlgoElement parent = geo.getParentAlgorithm();
		CountDownLatch stored = new CountDownLatch(1);
		undo.addUndoInfoStoredListener(stored::countDown);

		List<String> errors = redefine(app, geo, presented);

		assertEquals(1, errors.size(), errors.toString());
		assertTrue(errors.get(0).startsWith(identity + "|CANONICAL_HEAD_SHADOWED"),
				errors.get(0));
		assertTrue(errors.get(0).contains(head), errors.get(0));
		assertEquals(xml, PreG9BR5BFingerprints.constructionXml(app));
		assertSame(geo, app.getKernel().lookupLabel(geo.getLabelSimple()));
		assertSame(parent, app.getKernel().lookupLabel(geo.getLabelSimple())
				.getParentAlgorithm());
		assertFalse(stored.await(2, TimeUnit.SECONDS), "undo point stored");
		assertEquals(history, undo.getHistorySize());

		CanonicalCommandSurfaceError typed = assertThrows(CanonicalCommandSurfaceError.class,
				() -> CanonicalCommandReentry.beforeParse(app.getKernel(), geo, presented)
						.afterParse(app.getKernel().getParser().parseGeoGebraExpression(
								presented)));
		assertEquals(CanonicalCommandSurfaceError.Outcome.CANONICAL_HEAD_SHADOWED,
				typed.getOutcome());
		assertEquals(head, typed.getToken());
		assertEquals(identity, typed.getExpectedIdentity());
		assertNotNull(typed.getConflict());
		assertEquals(xml, PreG9BR5BFingerprints.constructionXml(app));
	}

	private static AppGeoCeDG application(Locale language) {
		AppGeoCeDG app = PreG9BR5BCanonicalCommandGateTest.application(language);
		// Headless: never open the modal legacy-redefine confirmation.
		app.setSpatialRedefinePresentation(new GeoCeDGSpatialRedefineFrontend.Presentation() {
			@Override
			public boolean confirmLegacy(SpatialRedefineAssessment assessment) {
				return true;
			}

			@Override
			public void showUnavailable(SpatialRedefineAssessment assessment) {
				throw new AssertionError("unavailable redefine: " + assessment);
			}

			@Override
			public void showStaleAssessment() {
				throw new AssertionError("stale redefine assessment");
			}
		});
		return app;
	}

	private static List<String> redefine(AppGeoCeDG app, GeoElement geo, String text) {
		List<String> errors = new ArrayList<>();
		app.getKernel().getAlgebraProcessor().changeGeoElement(geo, text, true, true,
				new ErrorHandler() {
					@Override
					public void showError(String msg) {
						errors.add(String.valueOf(msg));
					}

					@Override
					public void showCommandError(String command, String message) {
						errors.add(command + "|" + message);
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
				}, result -> {
					// the redefined element is looked up again by label
				});
		return errors;
	}

	private static String identity(GeoElement geo) {
		AlgoElement parent = geo.getParentAlgorithm();
		if (parent == null) {
			// an independent definition, e.g. g(x) = If(...): its persisted expression line
			// carries the command identity and is compared separately
			return "independent " + geo.getTypeString();
		}
		return parent.getClassName() == null ? parent.getClass().getSimpleName()
				: parent.getClassName().getCommand();
	}

	private static String coreOf(AppGeoCeDG app, String label) {
		StringBuilder element = new StringBuilder();
		String core = PreG9BR5BFingerprints.constructionCore(app);
		String[] lines = core.split("\n");
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].startsWith("<output ") && lines[i].contains("\"" + label + "\"")) {
				int start = i;
				while (start > 0 && !lines[start].startsWith("<command ")) {
					start--;
				}
				for (int j = start; j <= i; j++) {
					element.append(lines[j]).append('\n');
				}
			} else if (lines[i].startsWith("<expression label=\"" + label + "\"")) {
				element.append(lines[i]).append('\n');
			}
		}
		return element.toString();
	}

	private static String commandNames(String core) {
		StringBuilder names = new StringBuilder();
		for (String line : core.split("\n")) {
			if (line.startsWith("<command ") || line.startsWith("<expression ")) {
				names.append(line).append('\n');
			}
		}
		return names.toString();
	}

	private static void eval(AppGeoCeDG app, String... inputs) {
		for (String input : inputs) {
			GeoElementND[] result = app.getKernel().getAlgebraProcessor()
					.processAlgebraCommand(input, false);
			assertNotNull(result, input);
		}
		assertNotNull(app.getKernel().lookupLabel("A").toValueString(
				StringTemplate.defaultTemplate));
	}
}
