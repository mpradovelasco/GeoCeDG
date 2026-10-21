/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusCertificationWork2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInstrumentation2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.ReconstructionFidelity2D;
import org.geocedg.common.kernel.locus.ReconstructionFidelity2D.DiscrepancyKind;
import org.geocedg.common.kernel.locus.ReconstructionSliceFingerprint2D;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.MacroKernel;
import org.geogebra.common.kernel.algos.AlgoDependentNumber;
import org.geogebra.common.kernel.algos.AlgoDependentPoint;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.ConstructionElement;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.ExpressionValue;
import org.geogebra.common.kernel.arithmetic.MyDouble;
import org.geogebra.common.kernel.arithmetic.MyVecNode;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.plugin.Operation;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU2 step T3, D-CU2-20-R on synthetic constructions: admitted restorations of a
 * reconstructed slice (V-REV-EXACT-4), reproducibility (V-REV-EXACT-8), reopening, undo and
 * redo (V-REV-EXACT-9), propagation and extreme values (V-REV-EXACT-10), and the typed causes
 * of slice rebuilds (V-REV-EXACT-7). The certificate parts of these rows are completed by the
 * steps that publish certificates.
 */
class PreG9BCu2RevisionExactnessTest extends BaseUnitTest {
	private static final Comparator<ConstructionElement> RECONSTRUCTION_ORDER =
			Comparator.comparingInt(ConstructionElement::getConstructionIndex)
					.thenComparingLong(ConstructionElement::getID);
	private static final double[] DRIVER_PARAMETERS = {-3.0, -1.1, 0.4, 2.1};

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	/**
	 * V-REV-EXACT-4: the validation compares every authoritative input bit for bit; the
	 * admitted restorations, by label, by input path and by literal order, return the copy
	 * to exact fidelity.
	 */
	@Test
	void admittedRestorationsReturnTheCopyToExactFidelity() throws Exception {
		add("r=3.6569437271164964");
		add("c=Circle((0,0),r)");
		add("P=Point(c)");
		add("Q=Rotate(P,30deg,(1,0))");
		GeoPoint q = (GeoPoint) lookup("Q");
		ExpressionNode shifted = new ExpressionNode(getKernel(), q, Operation.PLUS,
				new MyVecNode(getKernel(), new MyDouble(getKernel(), 0.1),
						new MyDouble(getKernel(), 0.2)));
		new AlgoDependentPoint(getConstruction(), shifted, false).getPoint().setLabel("R");
		List<GeoElement> slice = slice(lookup("R"));
		List<GeoElement> roots = List.of(lookup("P"));
		Construction copy = reconstruct(slice);
		assertTrue(ReconstructionFidelity2D.validate(slice, copy, roots, false).isFaithful());

		GeoNumeric radius = (GeoNumeric) copy.lookupLabel("r");
		radius.setValue(Math.nextUp(radius.getValue()));
		GeoPoint center = inlinePoint(copy.lookupLabel("Q").getParentAlgorithm());
		center.setCoords(Math.nextUp(1.0), 0, 1);
		MyDouble literal = firstLiteral(copy.lookupLabel("R").getDefinition());
		literal.set(Math.nextUp(0.1));

		ReconstructionFidelity2D.Report found = ReconstructionFidelity2D.validate(slice, copy,
				roots, false);
		assertFalse(found.isFaithful());
		assertEquals(EnumSet.of(DiscrepancyKind.VALUE, DiscrepancyKind.INLINE_VALUE,
				DiscrepancyKind.LITERAL), kinds(found));
		ReconstructionFidelity2D.Report restored = ReconstructionFidelity2D.validate(slice,
				copy, roots, true);
		assertTrue(restored.isFaithful(), restored.toString());
		assertEquals(3, restored.getRestorations());
		assertEquals(Double.doubleToRawLongBits(((GeoNumeric) lookup("r")).getValue()),
				Double.doubleToRawLongBits(radius.getValue()));
		assertEquals(Double.doubleToRawLongBits(0.1),
				Double.doubleToRawLongBits(literal.getDouble()));
		GeoPoint liveR = (GeoPoint) lookup("R");
		GeoPoint copyR = (GeoPoint) copy.lookupLabel("R");
		assertEquals(Double.doubleToRawLongBits(liveR.getInhomX()),
				Double.doubleToRawLongBits(copyR.getInhomX()));
	}

	/**
	 * V-REV-EXACT-9: a reopened document is the live state; its slice reproduces the reloaded
	 * values. An angle literal without original text loses its exact value in the persisted
	 * format, and that loss is characterized: the reloaded value is the one written in degrees.
	 */
	@Test
	void aReopenedDocumentIsReconstructedFaithfullyInItsReloadedState() throws Exception {
		add("c=Circle((0,0),3.6569437271164964)");
		add("P=Point(c)");
		MyDouble literal = new MyDouble(getKernel(), 0.4097);
		literal.setAngle();
		new AlgoDependentNumber(getConstruction(), new ExpressionNode(getKernel(), literal),
				true).getNumber().toGeoElement().setLabel("ang");
		add("Q=Rotate(P,ang,(1,1))");
		GeoLocusV2 before = add("l=LocusV2(Q,P)");
		assertFalse(evaluator(before).isReconstructionFaithful());

		reload();
		GeoLocusV2 locus = assertInstanceOf(GeoLocusV2.class, lookup("l"));
		GeoNumeric angle = assertInstanceOf(GeoNumeric.class, lookup("ang"));
		assertEquals("0x1.a3886594af4f2p-2", Double.toHexString(angle.getValue()));
		assertTrue(evaluator(locus).isReconstructionFaithful(),
				evaluator(locus).getFidelityReport().toString());
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
			for (double parameter : DRIVER_PARAMETERS) {
				LocusEvaluation2D evaluation = locus.getSemanticDefinition().evaluate(
						locus.getSemanticDefinition().getBranches().get(0).getBranchKey(),
						parameter, session);
				assertTrue(evaluation.isValid(), evaluation.toString());
			}
		}
	}

	/** V-REV-EXACT-9: undo and redo restore snapshots with the same content. */
	@Test
	void undoAndRedoRestoreSnapshotsWithTheSameContent() {
		activateUndo();
		add("r=3.6569437271164964");
		add("c=Circle((0,0),r)");
		add("P=Point(c)");
		add("Q=Rotate(P,30deg,(1,0))");
		add("l=LocusV2(Q,P)");
		getApp().storeUndoInfo();
		String original = fingerprint("l");
		GeoNumeric radius = (GeoNumeric) lookup("r");
		radius.setValue(Math.nextUp(radius.getValue()));
		radius.updateCascade();
		getApp().storeUndoInfo();
		String changed = fingerprint("l");
		assertNotEquals(original, changed);
		getKernel().undo();
		assertEquals(original, fingerprint("l"));
		getKernel().redo();
		assertEquals(changed, fingerprint("l"));
	}

	/** V-REV-EXACT-8: two runs give identical fingerprints and counters. */
	@Test
	void twoRunsGiveIdenticalFingerprintsAndCounters() {
		Map<String, Object> first = run(getApp());
		Map<String, Object> second = run(AppCommonFactory.create(new AppConfigGeoCeDG(true)));
		assertEquals(first, second);
	}

	/**
	 * V-REV-EXACT-10: a one-ulp change reaches the locus and its metric with the new
	 * revision; extreme values never share a fingerprint and are reconstructed faithfully.
	 */
	@Test
	void propagationAndExtremeValuesKeepTheFingerprintExact() throws Exception {
		add("r=3.6569437271164964");
		add("c=Circle((0,0),r)");
		add("P=Point(c)");
		add("Q=Rotate(P,30deg,(1,0))");
		GeoLocusV2 locus = add("l=LocusV2(Q,P)");
		GeoLocusMetricResult length = add("len=LocusLength(l)");
		GeoNumeric radius = (GeoNumeric) lookup("r");
		radius.setValue(Math.nextUp(radius.getValue()));
		radius.updateCascade();
		assertEquals(locus.getSemanticRevision(), length.getSourceSemanticRevision());

		add("t=0");
		add("k=1");
		add("V=(t,k t)");
		add("dom={false,{-2,2,true,true}}");
		GeoLocusV2 scalar = add("L=LocusV2(V,t,dom)");
		GeoNumeric factor = (GeoNumeric) lookup("k");
		Set<String> fingerprints = new LinkedHashSet<>();
		double[] extremes = {1e-300, Math.nextUp(1e-300), Double.MIN_VALUE, 0, 1e300,
				Math.nextDown(1e300)};
		for (double value : extremes) {
			factor.setValue(value);
			factor.updateCascade();
			GeoElement driver = lookup("t");
			fingerprints.add(ReconstructionSliceFingerprint2D.compute(
					((AlgoDependentPointLocusV2) scalar.getParentAlgorithm())
							.getReconstructionSliceGeos(), List.of(driver, driver)));
			assertTrue(evaluator(scalar).isReconstructionFaithful(),
					evaluator(scalar).getFidelityReport().toString());
		}
		assertEquals(extremes.length, fingerprints.size());
	}

	/**
	 * V-REV-EXACT-7: each slice rebuild records the typed part of the snapshot that changed;
	 * a recompute of the whole construction in the same state rebuilds nothing.
	 */
	@Test
	void sliceRebuildsRecordTheirTypedCause() {
		add("r=3.6569437271164964");
		add("c=Circle((0,0),r)");
		add("P=Point(c)");
		add("h=Line(P,(5,1))");
		add("S=Intersect(c,h)");
		add("M=Midpoint(S_1,(0,0))");
		GeoLocusV2 locus = add("l=LocusV2(M,P)");
		LocusInstrumentation2D instrumentation = locus.getInstrumentation();
		long values = instrumentation.getSliceChanges(
				ReconstructionSliceFingerprint2D.Change.VALUES);
		long structure = instrumentation.getSliceChanges(
				ReconstructionSliceFingerprint2D.Change.STRUCTURE);
		long validations = instrumentation.getCertificationWork(
				LocusCertificationWork2D.FIDELITY_VALIDATION);
		GeoNumeric radius = (GeoNumeric) lookup("r");
		radius.setValue(Math.nextUp(radius.getValue()));
		radius.updateCascade();
		assertEquals(values + 1, instrumentation.getSliceChanges(
				ReconstructionSliceFingerprint2D.Change.VALUES));
		assertEquals(structure, instrumentation.getSliceChanges(
				ReconstructionSliceFingerprint2D.Change.STRUCTURE));
		assertEquals(validations + 1, instrumentation.getCertificationWork(
				LocusCertificationWork2D.FIDELITY_VALIDATION));
		long revision = locus.getSemanticRevision();
		long builds = instrumentation.getDependencySliceBuilds();
		getKernel().updateConstruction(false);
		assertEquals(revision, locus.getSemanticRevision());
		assertEquals(builds, instrumentation.getDependencySliceBuilds());
		// Renaming an output resets the evidence counters of the locus with its identity
		// refresh; the rebuild that follows is recorded as a structure change.
		lookup("S_2").rename("T");
		assertTrue(instrumentation.getSliceChanges(
				ReconstructionSliceFingerprint2D.Change.STRUCTURE) >= 1);
		assertEquals(0, instrumentation.getCertificationWork(
				LocusCertificationWork2D.FIDELITY_FAILURE));
	}

	private Map<String, Object> run(AppCommon app) {
		for (String command : List.of("r=3.6569437271164964", "c=Circle((0,0),r)",
				"P=Point(c)", "Q=Rotate(P,30deg,(1,0))", "l=LocusV2(Q,P)")) {
			app.getKernel().getAlgebraProcessor().processAlgebraCommand(command, false);
		}
		GeoNumeric radius = (GeoNumeric) app.getKernel().lookupLabel("r");
		radius.setValue(Math.nextUp(radius.getValue()));
		radius.updateCascade();
		GeoLocusV2 locus = (GeoLocusV2) app.getKernel().lookupLabel("l");
		GeoElement driver = app.getKernel().lookupLabel("P");
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("fingerprint", ReconstructionSliceFingerprint2D.compute(
				((AlgoDependentPointLocusV2) locus.getParentAlgorithm())
						.getReconstructionSliceGeos(), List.of(driver, driver)));
		result.put("revision", locus.getSemanticRevision());
		result.put("builds", locus.getInstrumentation().getDependencySliceBuilds());
		for (LocusCertificationWork2D kind : LocusCertificationWork2D.values()) {
			result.put(kind.name(), locus.getInstrumentation().getCertificationWork(kind));
		}
		return result;
	}

	private String fingerprint(String label) {
		GeoLocusV2 locus = (GeoLocusV2) lookup(label);
		GeoElement driver = lookup("P");
		return ReconstructionSliceFingerprint2D.compute(
				((AlgoDependentPointLocusV2) locus.getParentAlgorithm())
						.getReconstructionSliceGeos(), List.of(driver, driver));
	}

	private List<GeoElement> slice(GeoElement target) {
		List<GeoElement> result = new ArrayList<>(target.getAllPredecessors());
		result.add(target);
		return result;
	}

	private Construction reconstruct(List<GeoElement> slice) throws Exception {
		Set<ConstructionElement> elements = new TreeSet<>(RECONSTRUCTION_ORDER);
		Set<Long> usedAlgorithmIds = new TreeSet<>();
		for (GeoElement geo : slice) {
			if (getConstruction().isConstantElement(geo)) {
				continue;
			}
			elements.add(geo);
			AlgoElement parent = geo.getParentAlgorithm();
			if (parent != null && parent.isInConstructionList()) {
				Macro.addDependentAlgo(parent, elements, usedAlgorithmIds);
			}
		}
		MacroKernel macroKernel = getKernel().newMacroKernel();
		macroKernel.setGlobalVariableLookup(false);
		macroKernel.loadXML(Macro.buildMacroXML(getKernel(), elements).toString());
		return macroKernel.getConstruction();
	}

	private static GeoPoint inlinePoint(AlgoElement algorithm) {
		for (GeoElement input : algorithm.getInput()) {
			if (input instanceof GeoPoint && !input.isLabelSet()) {
				return (GeoPoint) input;
			}
		}
		throw new AssertionError("No inline point input");
	}

	private static MyDouble firstLiteral(ExpressionValue value) {
		ExpressionValue node = value.unwrap();
		if (node instanceof MyDouble) {
			return (MyDouble) node;
		}
		for (int index = 0; index < node.getChildCount(); index++) {
			if (!(node.getChild(index).unwrap() instanceof GeoElement)) {
				MyDouble found = firstLiteral(node.getChild(index));
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}

	private static Set<DiscrepancyKind> kinds(ReconstructionFidelity2D.Report report) {
		Set<DiscrepancyKind> kinds = EnumSet.noneOf(DiscrepancyKind.class);
		for (ReconstructionFidelity2D.Discrepancy discrepancy : report.getDiscrepancies()) {
			kinds.add(discrepancy.getKind());
		}
		return kinds;
	}

	private static ReconstructibleLocusEvaluator2D evaluator(GeoLocusV2 locus)
			throws Exception {
		Field field = AlgoDependentPointLocusV2.class.getDeclaredField("evaluator");
		field.setAccessible(true);
		return assertInstanceOf(ReconstructibleLocusEvaluator2D.class,
				field.get(locus.getParentAlgorithm()));
	}
}
