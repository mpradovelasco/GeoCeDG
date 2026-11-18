/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */
package org.geocedg.common.locus;

import static org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter.richResultOf;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operation;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusCoverageCause2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;
import org.geocedg.common.kernel.locus.ReconstructionFidelity2D.DiscrepancyKind;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Completeness;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxModel2D;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxOutcome2D.Kind;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxOutcome2D.Unresolved;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricGuaranteeClass2D;
import org.geogebra.common.kernel.arithmetic.MyDouble;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.MyVecNode;
import org.geogebra.common.kernel.algos.AlgoDependentPoint;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** JUMP-CONTEXT: fidelity to the producer, never equivalence from proximity. */
class GeoCeDG120NativeScalarContextTest extends PreG9bR3RedefineTestBase {
	private static final String BRANCH = "generator.main";

	private void driver() {
		add("s=Segment((0,0),(1,0))");
		add("P=Point(s)");
	}

	private CertifiedConstructionProgram2D program(GeoLocusV2 locus) {
		var evaluator = (ReconstructibleLocusEvaluator2D) locus.getSemanticDefinition()
				.getEvaluatorCapability();
		return evaluator.captureCertifiedProgramV3().orElseThrow(() -> new AssertionError(
				"capture-refusal=" + evaluator.getProgramV3Refusal()
						+ "; fidelity=" + evaluator.getFidelityReport()));
	}

	private double y(GeoLocusV2 locus, double parameter) {
		try (var session = LocusEvaluationSession2D.memoizing(64)) {
			var result = locus.getSemanticDefinition().evaluate(BRANCH, parameter, session);
			assertTrue(result.isValid(), result.toString());
			return result.getPoint().getY();
		}
	}

	private void enclosed(GeoLocusV2 locus, double parameter) {
		var result = new ConstructionBoxModel2D(program(locus)).evaluate(parameter, parameter);
		assertEquals(Kind.VALID, result.getKind());
		double nativeValue = y(locus, parameter);
		assertTrue(result.getValueLower(1) <= nativeValue
				&& nativeValue <= result.getValueUpper(1),
				() -> result.getValueLower(1) + " <= " + nativeValue
						+ " <= " + result.getValueUpper(1));
	}

	@ParameterizedTest @ValueSource(ints = {2, 4})
	void cartesianSineHasCompleteGeometryAndOrdinaryNumericLength(int frequency) {
		driver();
		add("Q=(x(P),sin(" + frequency + "*pi*x(P)))");
		GeoLocusV2 locus = add("L=LocusV2(Q,P)");
		var program = program(locus);
		assertTrue(program.getSignature().contains("native-scalar-dispatch/v1"));
		assertTrue(program.getSteps().stream().anyMatch(
				step -> step.getOperation() == Operation.SCALAR_SIN_DOUBLE));
		assertFalse(program.getSteps().stream().anyMatch(
				step -> step.getOperation() == Operation.SCALAR_SIN));
		var structure = locus.getSemanticDefinition().getBranches().get(0).getExistenceStructure();
		assertEquals(Completeness.COMPLETE, structure.getCompleteness());
		assertEquals(1, structure.getContinuousValidComponents().size());
		String before = structure.getSemanticSignature();
		GeoNumeric scalar = add("a=LocusLength(L)");
		var metric = richResultOf(scalar).getMetricResult();
		assertEquals(MetricComputationStatus.SUCCESS, metric.getComputationStatus(),
				metric.getDiagnostics().toString());
		assertEquals(MetricGuaranteeClass2D.ESTIMATED, metric.getGuaranteeClass());
		assertTrue(metric.isScalarAdmissible());
		assertFalse(metric.isConditionedEstimate());
		assertTrue(metric.getErrorEvidence().getRigorousEnclosure().isEmpty());
		assertTrue(metric.getErrorEvidence().getEstablishedAbsoluteAmount()
				<= metric.getErrorEvidence().getRequestedTolerance().orElseThrow());
		// Independent elliptic-integral references with the exact binary64 coefficient.
		double reference = frequency == 4 ? 8.111828618839560 : 4.188275203698434;
		assertEquals(reference, scalar.getValue(),
				metric.getErrorEvidence().getEstablishedAbsoluteAmount());
		assertEquals(2 * scalar.getValue() + 1,
				((GeoNumeric) add("b=2*a+1")).getValue(), 0);
		assertEquals(before, locus.getSemanticDefinition().getBranches().get(0)
				.getExistenceStructure().getSemanticSignature());
		System.out.println("JUMP-CONTEXT runtime=" + System.getProperty("java.version")
				+ " kernel=" + getKernel().getClass().getProtectionDomain().getCodeSource().getLocation());
		System.out.println("JUMP-CONTEXT sine" + frequency + " value=" + scalar.getValue()
				+ " error=" + metric.getErrorEvidence().getEstablishedAbsoluteAmount()
				+ " tau=" + metric.getErrorEvidence().getRequestedTolerance().orElseThrow()
				+ " geometry=" + structure.getCompleteness()
				+ " conditioned=" + metric.isConditionedEstimate());
	}

	@Test void genericNumericProducerKeepsItsZeroBranchesAndDistinctSignature() {
		driver();
		add("n=sin(4*pi*x(P))");
		add("Qf=(x(P),sin(4*pi*x(P)))");
		add("Qg=(x(P),n)");
		GeoLocusV2 fast = add("Lf=LocusV2(Qf,P)");
		GeoLocusV2 generic = add("Lg=LocusV2(Qg,P)");
		assertNotEquals(program(fast).getSignature(), program(generic).getSignature());
		assertTrue(program(generic).getSteps().stream().anyMatch(
				step -> step.getOperation() == Operation.SCALAR_SIN));
		assertFalse(program(generic).getSteps().stream().anyMatch(
				step -> step.getOperation() == Operation.SCALAR_SIN_DOUBLE));
		for (double t : new double[] {.249999998, .25, .250000002}) {
			assertEquals(Math.sin(4 * Math.PI * t), y(fast, t), 0);
			assertEquals(new MyDouble(getKernel(), 4 * Math.PI * t).sin().getDouble(),
					y(generic, t), 0);
			enclosed(fast, t);
			enclosed(generic, t);
		}
		assertNotEquals(0, y(fast, .25));
		assertEquals(0, y(generic, .25));
		var model = new ConstructionBoxModel2D(program(generic));
		var crossing = model.evaluate(.249999998, .250000002);
		assertEquals(Kind.UNRESOLVED, crossing.getKind());
		assertEquals(Unresolved.TOLERANCE, crossing.getUnresolved());
		assertEquals(Kind.VALID, new ConstructionBoxModel2D(program(fast))
				.evaluate(.249999998, .250000002).getKind());
		assertFalse(((GeoNumeric) add("a=LocusLength(Lg)")).isDefined());
	}

	@Test void nestedSineDoesNotPropagateFastContextThroughANumericGeo() {
		driver();
		add("n=sin(4*pi*x(P))");
		add("Q=(x(P),sin(n))");
		GeoLocusV2 locus = add("L=LocusV2(Q,P)");
		var operations = program(locus).getSteps().stream().map(step -> step.getOperation()).toList();
		assertTrue(operations.containsAll(List.of(Operation.SCALAR_SIN, Operation.SCALAR_SIN_DOUBLE)));
		assertEquals(0, y(locus, .25));
		add("R=(x(P),sin(sin(4*pi*x(P))))");
		GeoLocusV2 inline = add("Li=LocusV2(R,P)");
		assertEquals(2, program(inline).getSteps().stream()
				.filter(step -> step.getOperation() == Operation.SCALAR_SIN_DOUBLE).count());
		assertEquals(Math.sin(Math.sin(Math.PI)), y(inline, .25), 0);
		enclosed(inline, .25);
	}

	@Test void independentConstantsUseTheirActualConsumerDispatch() {
		driver();
		add("c=pi"); add("n=sin(c)");
		add("Qf=(x(P),sin(c))"); add("Qg=(x(P),n)");
		GeoLocusV2 fast = add("Lf=LocusV2(Qf,P)");
		GeoLocusV2 generic = add("Lg=LocusV2(Qg,P)");
		assertEquals(Math.sin(Math.PI), y(fast, .4), 0);
		assertEquals(0, y(generic, .4));
		assertNotEquals(program(fast).getSignature(), program(generic).getSignature());
		enclosed(fast, .4); enclosed(generic, .4);
	}

	@Test void cosineHasTheSameContextBoundaryWithoutWeakeningGenericGuards() {
		driver(); add("n=cos(2*pi*x(P))");
		add("Qf=(x(P),cos(2*pi*x(P)))"); add("Qg=(x(P),n)");
		GeoLocusV2 fast = add("Lf=LocusV2(Qf,P)");
		GeoLocusV2 generic = add("Lg=LocusV2(Qg,P)");
		assertEquals(Math.cos(Math.PI / 2), y(fast, .25), 0);
		assertEquals(0, y(generic, .25));
		enclosed(fast, .25); enclosed(generic, .25);
		assertEquals(Kind.UNRESOLVED, new ConstructionBoxModel2D(program(generic))
				.evaluate(.24999999, .25000001).getKind());
		assertEquals(Kind.VALID, new ConstructionBoxModel2D(program(fast))
				.evaluate(.24999999, .25000001).getKind());
	}

	@Test void sqrtShortFallbackKeepsGenericEvaluationOfItsNestedSine() throws Exception {
		driver();
		GeoPoint source = add("Q=(x(P),sin(4*pi*x(P)))");
		MyVecNode coordinates = (MyVecNode) source.getDefinition().unwrap();
		var shortRoot = new ExpressionNode(getKernel(), coordinates.getY().deepCopy(getKernel()),
				org.geogebra.common.plugin.Operation.SQRT_SHORT, null);
		var tuple = new MyVecNode(getKernel(), coordinates.getX().deepCopy(getKernel()), shortRoot);
		GeoPoint shortPoint = new AlgoDependentPoint(getConstruction(),
				new ExpressionNode(getKernel(), tuple), false).getPoint();
		shortPoint.setLabel("R");
		GeoLocusV2 fallback = add("Ls=LocusV2(R,P)");
		// The serializer prints sqrt(...), which reparses as SQRT, not SQRT_SHORT.
		// Equal spelling does not establish the original producer's evaluation context.
		String serialized = shortPoint.getDefinition()
				.toString(org.geogebra.common.kernel.StringTemplate.xmlTemplate);
		MyVecNode reparsed = (MyVecNode) getKernel().getParser().parseExpression(serialized).unwrap();
		assertEquals(org.geogebra.common.plugin.Operation.SQRT_SHORT, shortRoot.getOperation());
		assertEquals(org.geogebra.common.plugin.Operation.SQRT,
				((ExpressionNode) reparsed.getY().unwrap()).getOperation());
		GeoPoint driver = (GeoPoint) lookup("P");
		assertTrue(shortPoint.isChildOf(driver));
		driver.getPathParameter().setT(.25);
		driver.getPath().pathChanged(driver); driver.updateCoords(); driver.updateCascade();
		assertEquals(0, shortPoint.getInhomY());
		var definition = fallback.getSemanticDefinition();
		var evaluator = (ReconstructibleLocusEvaluator2D) definition.getEvaluatorCapability();
		assertTrue(evaluator.captureCertifiedProgramV3().isEmpty());
		assertEquals("reconstruction-fidelity-not-established", evaluator.getProgramV3Refusal());
		assertFalse(evaluator.isReconstructionFaithful());
		assertTrue(evaluator.getFidelityReport().getDiscrepancies().stream().anyMatch(
				d -> d.getElement().equals("R") && d.getKind() == DiscrepancyKind.STRUCTURE));
		assertTrue(evaluator.getFidelityReport().getUnsettledSelections().isEmpty());
		var structure = definition.getBranches().get(0).getExistenceStructure();
		assertEquals(Completeness.NOT_ESTABLISHED, structure.getCompleteness());
		assertTrue(structure.getContinuousValidComponents().isEmpty());
		assertEquals(LocusCoverageCause2D.RECONSTRUCTION_FIDELITY_NOT_ESTABLISHED,
				structure.getCoverage().get(0).getCause());
		try (var session = LocusEvaluationSession2D.memoizing(64)) {
			var refused = definition.evaluate(BRANCH, .25, session);
			assertEquals(EvaluationStatus.EVALUATION_FAILED, refused.getStatus());
			assertTrue(refused.getDiagnostic().contains("RECONSTRUCTION_FIDELITY_NOT_ESTABLISHED"));
			assertNull(refused.getPoint());
		}
		assertFalse(((GeoNumeric) add("shortLength=LocusLength(Ls)")).isDefined());
		// Opcode assertions live in the separate unit IR test; no unit program bypasses
		// the public reconstruction gate. Keep the original native numeric contrast.
		add("Qf=(x(P),sqrt(sin(4*pi*x(P))))");
		GeoLocusV2 fast = add("Lf=LocusV2(Qf,P)");
		assertEquals(Math.sqrt(Math.sin(Math.PI)), y(fast, .25), 0);
		assertTrue(y(fast, .25) > 0);
	}

	@Test void realInvalidityUndoAndReopenKeepIdentityAndRecaptureContext() {
		activateUndo(); driver(); add("amp=1");
		add("Q=(x(P),amp*sin(4*pi*x(P)))");
		GeoLocusV2 locus = add("L=LocusV2(Q,P)");
		GeoNumeric scalar = add("a=LocusLength(L)");
		var scalarId = getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(scalar);
		String signature = program(locus).getSignature();
		assertTrue(scalar.isDefined());
		getApp().storeUndoInfo();
		GeoNumeric amplitude = (GeoNumeric) lookup("amp");
		assertNotNull(amplitude.getDefinition());
		amplitude.setUndefined();
		assertNotNull(amplitude.getDefinition(), "setUndefined retains the original expression");
		// Use a literal undefined numeric state, with no old expression to reconstruct.
		amplitude.setValue(Double.NaN);
		assertNull(amplitude.getDefinition());
		getKernel().updateConstruction(false);
		assertFalse(scalar.isDefined()); getApp().storeUndoInfo();
		getKernel().undo(); assertTrue(lookup("a").isDefined());
		getKernel().redo(); assertFalse(lookup("a").isDefined());
		getKernel().undo(); assertTrue(lookup("a").isDefined());
		reload();
		assertTrue(lookup("a").isDefined());
		assertEquals(scalarId, getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(lookup("a")));
		assertEquals(signature, program((GeoLocusV2) lookup("L")).getSignature());
	}
}
