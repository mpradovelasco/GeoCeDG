/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.bspline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoBSpline;
import org.geocedg.common.kernel.algos.AlgoSplineV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.commands.AlgebraProcessor;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.util.InternalClipboard;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-MAINT-A2, BS-11, BS-12 and the passive part of BS-24 ({@code bspline-2d.md}
 * sections 8.5, 10.2 and 10.5): save and reopen, Undo/Redo after a control-point move and a
 * knot edit, copy and paste, compatible redefinition, replacement by another family,
 * identical dependencies live and after reopening, and the provenance block through Undo and
 * copy.
 */
public class MaintA2BSplinePersistenceTest extends BaseUnitTest {
	private static final String ELEMENT_EXPLICIT =
			"<geocedgBSpline contract=\"bspline-2d/v1\" knots=\"explicit\"/>";
	private static final String ELEMENT_RULE =
			"<geocedgBSpline contract=\"bspline-2d/v1\" knots=\"clamped-uniform/v1\"/>";
	private static final String PROVENANCE = "<geocedgBSpline contract=\"bspline-2d/v1\""
			+ " knots=\"clamped-uniform/v1\">\n\t\t<approximationProvenance version=\"1\""
			+ " source=\"sha256:00ff\" method=\"m\"/>\n</geocedgBSpline>\n";

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	private void points() {
		for (String point : new String[] {"A=(0,0)", "B=(1,2)", "C=(3,2.5)", "D=(4,0.5)",
				"E=(6,1)", "F=(7,3)", "G=(5,4)"}) {
			add(point);
		}
	}

	private GeoLocusV2 curve(String label) {
		return (GeoLocusV2) lookup(label);
	}

	private static AlgoBSpline algo(GeoLocusV2 curve) {
		return (AlgoBSpline) curve.getParentAlgorithm();
	}

	private static String digest(GeoLocusV2 curve) {
		return algo(curve).getCapturedDefinition().getDefinitionDigestHex();
	}

	private static List<String> inputs(GeoLocusV2 curve) {
		List<String> labels = new ArrayList<>();
		for (GeoElement input : curve.getParentAlgorithm().getInput()) {
			labels.add(input.getLabelSimple() + ":" + input.getXMLTypeString()
					+ (input.isIndependent() ? "" : ":" + input.getParentAlgorithm()
							.getClassName()));
		}
		return labels;
	}

	private static String identity(GeoLocusV2 curve) {
		return curve.getPersistentLocusId().toExternalForm();
	}

	@Test
	void saveReopenSaveIsByteIdenticalWithIdentityAndDependencies() {
		points();
		add("t=1.5");
		add("K={0,0,0,0,t,4,4,4,4}");
		GeoLocusV2 curve = add("b=BSpline({A,B,C,D,E},3,K)");
		add("c=BSpline({A,B,C,D,E})");
		String identity = identity(curve);
		List<String> dependencies = inputs(curve);
		String saved = getApp().getXML();
		assertTrue(saved.contains(ELEMENT_EXPLICIT) && saved.contains(ELEMENT_RULE));
		reload();
		GeoLocusV2 reopened = curve("b");
		assertTrue(reopened.isDefined());
		assertEquals(identity, identity(reopened));
		assertEquals(dependencies, inputs(reopened), "DAG identical live and after reopen");
		assertEquals(saved, getApp().getXML(), "save, reopen, save is byte-identical");
		reload();
		assertEquals(saved, getApp().getXML());
	}

	@Test
	void undoRedoRestoreTheExactEarlierDefinition() {
		activateUndo();
		points();
		add("t=1.5");
		add("K={0,0,0,0,t,4,4,4,4}");
		add("b=BSpline({A,B,C,D,E},3,K)");
		getApp().storeUndoInfo();
		String initial = digest(curve("b"));
		GeoPoint moved = (GeoPoint) lookup("E");
		moved.setCoords(6.5, 1.75, 1);
		moved.updateCascade();
		getApp().storeUndoInfo();
		String afterMove = digest(curve("b"));
		GeoNumeric knot = (GeoNumeric) lookup("t");
		knot.setValue(2.5);
		knot.updateCascade();
		getApp().storeUndoInfo();
		String afterKnot = digest(curve("b"));
		assertNotEquals(initial, afterMove);
		assertNotEquals(afterMove, afterKnot);
		getKernel().undo();
		assertEquals(afterMove, digest(curve("b")), "undo the knot edit");
		getKernel().undo();
		assertEquals(initial, digest(curve("b")), "undo the control-point move");
		getKernel().redo();
		assertEquals(afterMove, digest(curve("b")));
		getKernel().redo();
		assertEquals(afterKnot, digest(curve("b")));
		assertTrue(getApp().getXML().contains(ELEMENT_EXPLICIT));
	}

	@Test
	void copyPasteCreatesANewIdentityAndCopiesTheElement() {
		points();
		GeoLocusV2 curve = add("b=BSpline({A,B,C,D,E},3)");
		String identity = identity(curve);
		int before = countBSplines();
		paste(InternalClipboard.getTextToSave(getApp(),
				Collections.singletonList(curve), text -> text));
		assertEquals(before + 1, countBSplines());
		GeoLocusV2 copy = lastBSpline();
		assertNotEquals(identity, identity(copy));
		assertTrue(copy.isDefined());
		assertEquals(digest(curve), digest(copy), "the copy has the same definition");
		assertEquals(2, countOccurrences(getApp().getXML(), ELEMENT_RULE));
	}

	/** Outcome of one redefinition: refused (with the error) or applied (identity kept?). */
	private String redefinitionOutcome(String label, String definition, boolean replacement) {
		GeoLocusV2 target = curve(label);
		String identity = identity(target);
		String errors = redefine(target, definition, replacement);
		Object after = getKernel().lookupLabel(label);
		String state = after instanceof GeoLocusV2
				? (identity.equals(identity((GeoLocusV2) after)) ? "identity kept"
						: "new identity")
				: "label now " + (after == null ? "absent" : after.getClass().getSimpleName());
		return (errors.isBlank() ? "applied, " : "refused (" + errors.trim() + "), ") + state;
	}

	/**
	 * BS-12 parity ({@code bspline-2d.md} section 8.5): the same compatible redefinition of a
	 * SplineV2 and of a BSpline (a degree change over the same list) through the ordinary
	 * route and through the explicit replacement route has the same outcome for both
	 * families; the outcome is that of the existing Locus V2 rules, not of the family.
	 */
	@Test
	void compatibleRedefinitionHasTheSplineV2OutcomeOnBothRoutes() {
		points();
		add("pts={A,B,C,D,E,F}");
		for (String label : new String[] {"s1", "s2"}) {
			add(label + "=SplineV2(pts,3)");
		}
		for (String label : new String[] {"b1", "b2"}) {
			add(label + "=BSpline(pts,3)");
		}
		String splineOrdinary = redefinitionOutcome("s1", "SplineV2(pts,5)", false);
		String ordinary = redefinitionOutcome("b1", "BSpline(pts,2)", false);
		String splineReplacement = redefinitionOutcome("s2", "SplineV2(pts,5)", true);
		String replacement = redefinitionOutcome("b2", "BSpline(pts,2)", true);
		System.out.println("A2-BS12 ordinary SplineV2: " + splineOrdinary);
		System.out.println("A2-BS12 ordinary BSpline: " + ordinary);
		System.out.println("A2-BS12 replacement SplineV2: " + splineReplacement);
		System.out.println("A2-BS12 replacement BSpline: " + replacement);
		// the existing rules, identical for both families: the ordinary route requests
		// ADVANCED_RETAIN, which the assessment LEGACY_REPLACEMENT_AVAILABLE does not
		// authorize (REDEFINE_REJECTED, nothing changes); the explicit replacement route is
		// applied and gives a new durable identity
		assertEquals(splineOrdinary, ordinary);
		assertEquals("refused (Please check your input), identity kept", ordinary);
		assertEquals(splineReplacement, replacement);
		assertEquals("applied, new identity", replacement);
		assertTrue(curve("b2").getParentAlgorithm() instanceof AlgoBSpline);
		assertEquals(2, algo(curve("b2")).getEvaluator().getData().degree());
		assertTrue(getApp().getXML().contains(ELEMENT_RULE));
	}

	@Test
	void redefinitionKeepsTheFamilyElementAndReplacementRemovesIt() {
		points();
		GeoLocusV2 curve = add("b=BSpline({A,B,C,D,E},3)");
		String list = curve.getParentAlgorithm().getInput(0).getLabelSimple();
		redefine(curve, "BSpline(" + list + ",2)");
		GeoLocusV2 redefined = curve("b");
		assertTrue(redefined.getParentAlgorithm() instanceof AlgoBSpline);
		assertEquals(2, algo(redefined).getEvaluator().getData().degree());
		assertTrue(getApp().getXML().contains(ELEMENT_RULE));
		redefine(redefined, "BSpline(" + list + ",3,{0,0,0,0,1,1,2,2,2})");
		assertTrue(getApp().getXML().contains(ELEMENT_EXPLICIT));
		redefine(curve("b"), "SplineV2(" + list + ",3)");
		assertTrue(curve("b").getParentAlgorithm() instanceof AlgoSplineV2);
		assertFalse(getApp().getXML().contains("geocedgBSpline"),
				"replacement by another family removes the element");
	}

	@Test
	void bs11KnotEditsChangeTheCurveGloballyAndRecoverWithoutStaleGeometry() {
		points();
		add("t=1.5");
		add("K={0,0,0,0,t,4,4,4,4}");
		GeoLocusV2 curve = add("b=BSpline({A,B,C,D,E},3,K)");
		BSplineSemanticEvaluator2D before = algo(curve).getEvaluator();
		GeoNumeric knot = (GeoNumeric) lookup("t");
		knot.setValue(3);
		knot.updateCascade();
		BSplineSemanticEvaluator2D after = algo(curve).getEvaluator();
		assertTrue(curve.isDefined(), "a knot edit recomputes the curve; no locality claimed");
		assertNotEquals(Arrays.deepToString(before.getLocalPolynomialCoordinateCoefficients(
				AlgoBSpline.BRANCH_KEY, 0)), Arrays.deepToString(after
						.getLocalPolynomialCoordinateCoefficients(AlgoBSpline.BRANCH_KEY, 0)));
		knot.setValue(5);
		knot.updateCascade();
		assertFalse(curve.isDefined());
		assertNull(curve.getSemanticDefinition(), "no stale geometry");
		assertEquals(BSplineInvalidity.KNOTS_DECREASING, algo(curve).getInvalidityCause());
		knot.setValue(2);
		knot.updateCascade();
		assertTrue(curve.isDefined());
		assertNull(algo(curve).getInvalidityCause());
	}

	@Test
	void bs24ProvenanceSurvivesUndoAndCopy() {
		activateUndo();
		points();
		add("b=BSpline({A,B,C,D,E},3)");
		String xml = getApp().getXML().replace(ELEMENT_RULE + "\n", PROVENANCE);
		assertTrue(xml.contains(PROVENANCE));
		getApp().setXML(xml, true);
		getApp().storeUndoInfo();
		GeoPoint moved = (GeoPoint) lookup("E");
		moved.setCoords(6.5, 1.75, 1);
		moved.updateCascade();
		getApp().storeUndoInfo();
		assertTrue(getApp().getXML().contains(PROVENANCE), "edits keep the passive block");
		getKernel().undo();
		assertTrue(getApp().getXML().contains(PROVENANCE), "undo restores the passive block");
		GeoLocusV2 curve = curve("b");
		paste(InternalClipboard.getTextToSave(getApp(),
				Collections.singletonList(curve), text -> text));
		assertEquals(2, countOccurrences(getApp().getXML(), PROVENANCE),
				"the copy carries the same provenance");
		assertNotNull(algo(lastBSpline()).getCapturedDefinition().getDefinitionDigestHex());
	}

	/** The explicit ADR 0026 replacement route of a participating Locus V2 output. */
	private void redefine(GeoElement target, String definition) {
		String errors = redefine(target, definition, true);
		assertTrue(errors.isBlank(), errors);
	}

	/**
	 * Redefinition through the ordinary route (the Algebra input redefinition) or through the
	 * explicit ADR 0026 replacement route.
	 *
	 * @return accumulated errors, blank when applied
	 */
	private String redefine(GeoElement target, String definition, boolean replacement) {
		ErrorAccumulator errors = new ErrorAccumulator();
		EvalInfo info = new EvalInfo(true, true).withSymbolicMode(
				AlgebraProcessor.getRedefinitionMode(target, getKernel()))
				.withLabelRedefinitionAllowedFor(target.getLabelSimple())
				.withSymbolic(true).withSliders(true);
		if (replacement) {
			info = info.withSpatialReplacementOperation();
		}
		getKernel().getAlgebraProcessor().changeGeoElementNoExceptionHandling(target,
				definition, info, false, ignored -> { }, errors);
		return errors.getErrors();
	}

	private void paste(String clipboard) {
		int separator = clipboard.indexOf('\n');
		List<String> labels = new ArrayList<>(Arrays.asList(
				clipboard.substring(0, separator).split(" ")));
		InternalClipboard.pasteGeoGebraXMLInternal(getApp(), labels,
				clipboard.substring(separator));
	}

	private int countBSplines() {
		int count = 0;
		for (GeoElement geo : getConstruction().getGeoSetConstructionOrder()) {
			if (geo.getParentAlgorithm() instanceof AlgoBSpline) {
				count++;
			}
		}
		return count;
	}

	private GeoLocusV2 lastBSpline() {
		GeoLocusV2 last = null;
		for (GeoElement geo : getConstruction().getGeoSetConstructionOrder()) {
			if (geo.getParentAlgorithm() instanceof AlgoBSpline) {
				last = (GeoLocusV2) geo;
			}
		}
		return last;
	}

	private static int countOccurrences(String text, String needle) {
		int count = 0;
		for (int at = text.indexOf(needle); at >= 0; at = text.indexOf(needle, at + 1)) {
			count++;
		}
		return count;
	}
}
