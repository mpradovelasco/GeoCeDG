/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;

import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusInstrumentation2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.metric.LocusMetricCapabilityHierarchy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricComponentBuildException;
import org.geocedg.common.kernel.locus.metric.LocusMetricEngine2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricIndexMode;
import org.geocedg.common.kernel.locus.metric.LocusMetricInstrumentation2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricOwnerLease2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResults2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricSharedOwner2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricDiagnostic2D;
import org.geocedg.common.kernel.locus.metric.MetricDiagnosticCode2D;
import org.geocedg.common.kernel.locus.metric.MetricEstimatorPolicy2D;
import org.geocedg.common.kernel.locus.metric.MetricGuaranteeClass2D;
import org.geocedg.common.kernel.locus.metric.MetricWorkBudget2D;
import org.geocedg.common.kernel.locus.metric.MetricWorkLimit2D;
import org.geocedg.common.kernel.locus.metric.PolicyV2LocusMetricCapability2D;
import org.geocedg.common.kernel.locus.metric.TotalLocusMetricQuery;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU1 estimator v2 ({@code locus-v2-metrics.md} §25.3, §25.7, §25.8 and N3-N5;
 * validation matrix V-MET-EST-1..2, V-ADV-1..5, V-INV-1, V-INV-1b, V-INV-2, V-DET-1 and
 * V-BUD-1). Synthetic semantic definitions evaluate exact Java functions, so that the
 * evaluated parameters and the curves are under complete control; public-command cases
 * exercise the product path.
 */
class PreG9BCu1EstimatorTest extends G9U0PublicSurfaceTestBase {

	private static final double GOLDEN = MetricEstimatorPolicy2D.GOLDEN_SECTION_FRACTION;

	/** V-MET-EST-1 (H10): sine arcs are estimated, with coherent errors. */
	@Test
	void sineArcLengthsAreHonestEstimates() {
		for (int k : new int[] {1, 2, 4}) {
			double frequency = 2 * Math.PI * k;
			LocusMetricResult2D rich = total("sine-" + k, 0, 1,
					x -> new LocusPoint2D(x, Math.sin(frequency * x)));
			double reference = simpson(x -> Math.hypot(1,
					frequency * Math.cos(frequency * x)), 0, 1, 1 << 16);
			assertCoherentEstimate(rich, reference, "k=" + k);
		}
		// The product path. PRE-G9B-CU2 declared conformity (step T11): the public sine
		// (x(P),sin(4*pi*x(P))) is outside class v3, so its existence is not established and
		// its total length is refused; the estimator is exercised on the public path by the
		// certified parabola y = x^2, -2 <= x <= 2.
		add("s=Segment((0,0),(1,0))");
		add("P=Point(s)");
		add("Q=(x(P),sin(4*pi*x(P)))");
		add("L=LocusV2(Q,P)");
		GeoLocusMetricResult metric = add("M=LocusLength(L)");
		assertNoTotalWithoutExistence(metric.getMetricResult(), "public k=2");
		certifiedParabola("Lp", "Qp", "publicParabola");
		GeoLocusMetricResult parabola = add("Mp=LocusLength(Lp)");
		assertCoherentEstimate(parabola.getMetricResult(), simpson(x -> Math.hypot(1, 2 * x),
				-2, 2, 1 << 16), "public parabola");
	}

	/** V-MET-EST-2 (H12): evaluator-only circles and a constructed ellipse. */
	@Test
	void evaluatorOnlyCirclesAndEllipseHaveCoherentErrors() {
		for (double radius : new double[] {1, 7, 8, 20, 1E3}) {
			LocusMetricResult2D rich = total("circle-" + radius, 0, 2 * Math.PI,
					t -> new LocusPoint2D(radius * Math.cos(t), radius * Math.sin(t)));
			assertCoherentEstimate(rich, 2 * Math.PI * radius, "radius " + radius);
		}
		LocusMetricResult2D ellipse = total("ellipse", 0, 2 * Math.PI,
				t -> new LocusPoint2D(3 * Math.cos(t), 2 * Math.sin(t)));
		assertCoherentEstimate(ellipse, simpson(t -> Math.hypot(3 * Math.sin(t),
				2 * Math.cos(t)), 0, 2 * Math.PI, 1 << 16), "ellipse");
	}

	/**
	 * N3 (§25.12): a direction reversal near a sample hides the same overshoot from
	 * every refinement level; the estimator locates the reversals and keeps the
	 * estimate within its declared error.
	 */
	@Test
	void directionReversalsAreLocated() {
		LocusMetricResult2D backAndForth = total("back-and-forth", 0, 2 * Math.PI,
				t -> new LocusPoint2D(Math.cos(t + 0.40489178628508343), 0));
		assertCoherentEstimate(backAndForth, 4, "cos(t + theta)");
		LocusMetricResult2D astroid = total("astroid", 0, 2 * Math.PI,
				t -> new LocusPoint2D(Math.pow(Math.cos(t), 3), Math.pow(Math.sin(t), 3)));
		assertCoherentEstimate(astroid, 6, "astroid");
		assertTrue(astroid.getErrorEvidence().getAssumptions().stream().anyMatch(
				assumption -> assumption.contains("reversal(s) located")), describe(astroid));
	}

	/**
	 * V-ADV-1, V-ADV-2 and V-ADV-5 (N5): aliasing, a feature narrower than the probe
	 * spacing and a discontinuity never receive class 1 or 2; an estimate records
	 * its hypotheses, which the narrow feature violates.
	 */
	@Test
	void adversarialCurvesNeverReceiveACertifiedClass() {
		double frequency = 2 * Math.PI * (1 << 20);
		LocusMetricResult2D aliasing = total("aliasing", 0, 1,
				x -> new LocusPoint2D(x, Math.sin(frequency * x)));
		assertNotCertified(aliasing, "sin(2 pi 2^20 x)");

		double width = 1E-9;
		double centre = 1 / Math.PI;
		LocusMetricResult2D plateau = total("plateau", 0, 1, x -> new LocusPoint2D(x,
				Math.exp(-Math.pow((x - centre) / width, 2))));
		assertNotCertified(plateau, "narrow plateau");
		assertEquals(MetricGuaranteeClass2D.ESTIMATED, plateau.getGuaranteeClass(),
				describe(plateau));
		// The recorded hypothesis is violated: the feature is narrower than h_probe,
		// so the value (the chord of the flat part) is not asserted.
		assertTrue(probeSpacing(plateau) > width, describe(plateau));

		LocusMetricResult2D steps = total("floor", 0, 1,
				x -> new LocusPoint2D(x, Math.floor(4 * x)));
		assertNotCertified(steps, "floor");

		// PRE-G9B-CU2 declared conformity (step T11): the public aliasing and floor curves are
		// outside class v3, so they are never even estimated: their total length is refused.
		add("s=Segment((0,0),(1,0))");
		add("P=Point(s)");
		add("Q=(x(P),sin(2*pi*1048576*x(P)))");
		add("L=LocusV2(Q,P)");
		assertNoTotalWithoutExistence(((GeoLocusMetricResult) add("M=LocusLength(L)"))
				.getMetricResult(), "public aliasing");
		add("F=(x(P),floor(4*x(P)))");
		add("Lf=LocusV2(F,P)");
		assertNoTotalWithoutExistence(((GeoLocusMetricResult) add("Mf=LocusLength(Lf)"))
				.getMetricResult(), "public floor");
	}

	/** V-ADV-3: the honest classification holds for every minimum depth. */
	@Test
	void minimumDepthDoesNotChangeTheHonestClassification() {
		double frequency = 2 * Math.PI * (1 << 20);
		for (int depth : new int[] {6, 10, 14}) {
			LocusMetricPolicy2D policy = LocusMetricPolicy2D.cu1PublicV2()
					.withEstimatorPolicy(new MetricEstimatorPolicy2D("cu1-test/d" + depth,
							depth, GOLDEN, 2, 4096))
					.withWorkBudget(new MetricWorkBudget2D(1 << 18, 1 << 17, 30));
			assertNotCertified(total("aliasing-" + depth, 0, 1,
					x -> new LocusPoint2D(x, Math.sin(frequency * x)), policy),
					"aliasing d_min=" + depth);
			LocusMetricResult2D plateau = total("plateau-" + depth, 0, 1,
					x -> new LocusPoint2D(x, Math.exp(-Math.pow((x - 1 / Math.PI)
							/ 1E-9, 2))), policy);
			assertNotCertified(plateau, "plateau d_min=" + depth);
			if (plateau.getGuaranteeClass() == MetricGuaranteeClass2D.ESTIMATED) {
				assertTrue(probeSpacing(plateau) > 1E-9, describe(plateau));
			}
		}
	}

	/**
	 * V-ADV-4: a curve that vanishes on every parameter the estimator evaluates
	 * repeats the result of its base curve; its class is not 1 or 2. This pins the
	 * limit of any sampling estimator (N5).
	 */
	@Test
	void curveVanishingOnTheProbeSequenceRepeatsItsBaseCurve() {
		LocusMetricResult2D base = total("base-line", 0, 1,
				x -> new LocusPoint2D(x, 0));
		List<Double> evaluated = new ArrayList<>();
		DoubleUnaryOperator hidden = x -> Math.sin(256 * Math.PI * x)
				* Math.sin(64 * Math.PI * x - Math.PI * GOLDEN);
		LocusMetricResult2D adversary = total("vanishing", 0, 1, x -> {
			evaluated.add(x);
			return new LocusPoint2D(x, hidden.applyAsDouble(x));
		});
		assertFalse(evaluated.isEmpty());
		for (double parameter : evaluated) {
			assertTrue(Math.abs(hidden.applyAsDouble(parameter)) < 1E-12,
					"the probe sequence sees the curve at " + parameter);
		}
		assertNotCertified(adversary, "vanishing on the probes");
		assertEquals(MetricGuaranteeClass2D.ESTIMATED, adversary.getGuaranteeClass());
		assertEquals(value(base), value(adversary), 1E-9, describe(adversary));
		double truth = simpson(x -> Math.hypot(1, derivative(hidden, x)), 0, 1, 1 << 18);
		assertTrue(truth > 10 * value(adversary), "the true length is " + truth);
	}

	/**
	 * V-INV-1 (N4): in the relative regime classes, relative errors and work are
	 * covariant under scale and translation, up to rounding.
	 */
	@Test
	void relativeRegimeIsCovariantUnderScaleAndTranslation() {
		LocusMetricInstrumentation2D baseWork = new LocusMetricInstrumentation2D();
		LocusMetricResult2D base = total("inv-base", 0, 1, x -> new LocusPoint2D(x,
				Math.sin(4 * Math.PI * x)), LocusMetricPolicy2D.cu1PublicV2(), baseWork);
		long baseCalls = baseWork.snapshot().getEvaluatorCalls();
		double[][] cases = {{1, 1E2}, {1E3, 0}, {1E3, 1E6}, {1E6, 0}, {1E6, 1E6}};
		for (double[] item : cases) {
			double scale = item[0];
			double shift = item[1];
			LocusMetricInstrumentation2D work = new LocusMetricInstrumentation2D();
			LocusMetricResult2D moved = total("inv-" + scale + "-" + shift, 0, 1,
					x -> new LocusPoint2D(scale * x + shift,
							scale * Math.sin(4 * Math.PI * x) + shift),
					LocusMetricPolicy2D.cu1PublicV2(), work);
			String where = "scale " + scale + " shift " + shift + ": " + describe(moved);
			assertEquals(base.getGuaranteeClass(), moved.getGuaranteeClass(), where);
			assertEquals(scale * value(base), value(moved), 1E-9 * scale * value(base),
					where);
			double ratio = relativeError(moved) / relativeError(base);
			assertTrue(ratio > 0.5 && ratio < 2, where + " ratio " + ratio);
			long calls = work.snapshot().getEvaluatorCalls();
			assertTrue(Math.abs(calls - baseCalls) <= baseCalls / 10,
					where + " calls " + calls + " base " + baseCalls);
		}
	}

	/**
	 * Limitation of V-INV-1 for class 3 (reported in the CU1 candidate): a unit-size
	 * curve keeps its class, value and work under translations up to 1E5, but at 1E6
	 * the coordinate rounding (ulp 1.2E-10, a tenth of the component tolerance) exceeds
	 * the per-leaf shares and the estimate fails closed with a typed cause; it never
	 * publishes a value it cannot support.
	 */
	@Test
	void translationBeyondTheCoordinateResolutionFailsClosed() {
		LocusMetricInstrumentation2D baseWork = new LocusMetricInstrumentation2D();
		LocusMetricResult2D base = total("shift-base", 0, 1, x -> new LocusPoint2D(x,
				Math.sin(4 * Math.PI * x)), LocusMetricPolicy2D.cu1PublicV2(), baseWork);
		long baseCalls = baseWork.snapshot().getEvaluatorCalls();
		for (double shift : new double[] {1E3, 1E4, 1E5}) {
			LocusMetricInstrumentation2D work = new LocusMetricInstrumentation2D();
			LocusMetricResult2D moved = total("shift-" + shift, 0, 1,
					x -> new LocusPoint2D(x + shift, Math.sin(4 * Math.PI * x) + shift),
					LocusMetricPolicy2D.cu1PublicV2(), work);
			String where = "shift " + shift + ": " + describe(moved);
			assertEquals(MetricGuaranteeClass2D.ESTIMATED, moved.getGuaranteeClass(), where);
			assertEquals(value(base), value(moved), 1E-9 * value(base), where);
			assertTrue(Math.abs(work.snapshot().getEvaluatorCalls() - baseCalls)
					<= baseCalls / 10, where);
		}
		LocusMetricResult2D far = total("shift-1E6", 0, 1, x -> new LocusPoint2D(
				x + 1E6, Math.sin(4 * Math.PI * x) + 1E6));
		assertNotCertified(far, "shift 1E6");
		assertEquals(MetricGuaranteeClass2D.NOT_ESTABLISHED, far.getGuaranteeClass(),
				describe(far));
	}

	/** V-INV-1b (N4): below the absolute floor the requested tolerance is absolute. */
	@Test
	void absoluteFloorRegimeUsesTheAbsoluteTolerance() {
		LocusMetricResult2D small = total("floor-regime", 0, 1, x -> new LocusPoint2D(
				1E-3 * x, 1E-3 * Math.sin(4 * Math.PI * x)));
		assertEquals(LocusMetricPolicy2D.DEFAULT_ABSOLUTE_TOLERANCE,
				small.getErrorEvidence().getRequestedTolerance().orElseThrow(), 0);
		assertTrue(small.isScalarAdmissible(), describe(small));
	}

	/** V-INV-2: results are bitwise independent of the view. */
	@Test
	void resultsDoNotDependOnTheView() {
		// PRE-G9B-CU2 declared conformity (step T11): the public scalar (t,sin(t)) is outside
		// class v3 and has no total length; the certified parabola is estimated publicly.
		certifiedParabola("Lt", "Qt", "firstParabola");
		final LocusMetricResult2D first = ((GeoLocusMetricResult) add("M1=LocusLength(Lt)"))
				.getMetricResult();
		getApp().getEuclidianView1().setCoordSystem(400, 300, 250, 250);
		getApp().getEuclidianView1().setRealWorldCoordSystem(-100, 100, -50, 50);
		getKernel().updateConstruction(false);
		LocusMetricResult2D recomputed = ((GeoLocusMetricResult) lookup("M1"))
				.getMetricResult();
		certifiedParabola("Lu", "Qu", "secondParabola");
		LocusMetricResult2D second = ((GeoLocusMetricResult) add("M2=LocusLength(Lu)"))
				.getMetricResult();
		for (LocusMetricResult2D other : List.of(recomputed, second)) {
			assertBitwiseEqual(first, other);
		}
	}

	/** V-DET-1: identical sources give bitwise identical results. */
	@Test
	void resultsAreBitwiseDeterministic() {
		DoubleFunction<LocusPoint2D> curve = x -> new LocusPoint2D(x,
				Math.sin(4 * Math.PI * x));
		assertBitwiseEqual(total("det-a", 0, 1, curve), total("det-b", 0, 1, curve));
		DoubleFunction<LocusPoint2D> circle = t -> new LocusPoint2D(7 * Math.cos(t),
				7 * Math.sin(t));
		assertBitwiseEqual(total("det-c", 0, 2 * Math.PI, circle),
				total("det-d", 0, 2 * Math.PI, circle));
	}

	/**
	 * V-BUD-1 (§25.7): an exhausted budget is a typed failure, memoized under its
	 * complete key; a second computation does not rebuild the component.
	 */
	@Test
	void exhaustedBudgetIsATypedMemoizedFailure() {
		LocusDefinition2D definition = definition("budget", 0, 1,
				x -> new LocusPoint2D(x, Math.sin(8 * Math.PI * x)));
		LocusMetricPolicy2D policy = LocusMetricPolicy2D.cu1PublicV2()
				.withWorkBudget(new MetricWorkBudget2D(200, 100, 22));
		LocusMetricInstrumentation2D instrumentation = new LocusMetricInstrumentation2D();
		LocusMetricSharedOwner2D owner = new LocusMetricSharedOwner2D("budget",
				instrumentation);
		LocusMetricResult2D first;
		LocusMetricResult2D second;
		long builds;
		// A live metric result holds its lease; the owner and its index stay active.
		try (LocusMetricOwnerLease2D consumer = owner.acquireLease()) {
			first = compute(definition, policy, owner, instrumentation);
			builds = instrumentation.snapshot().getComponentStateBuilds();
			second = compute(definition, policy, owner, instrumentation);
		}
		assertEquals(MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
				first.getComputationStatus(), describe(first));
		assertEquals(MetricGuaranteeClass2D.NOT_ESTABLISHED, first.getGuaranteeClass());
		assertTrue(first.getDiagnostics().stream().anyMatch(diagnostic ->
				diagnostic.getCode() == MetricDiagnosticCode2D.WORK_BUDGET_EXHAUSTED
						&& diagnostic.getWorkLimit()
								== MetricWorkLimit2D.MAXIMUM_EVALUATIONS),
				describe(first));
		assertFalse(first.isScalarAdmissible());
		assertEquals(builds, instrumentation.snapshot().getComponentStateBuilds(),
				"a memoized failure is not rebuilt");
		assertEquals(first.getDiagnostics(), second.getDiagnostics());
		assertEquals(first.getComputationStatus(), second.getComputationStatus());
	}

	private static LocusMetricResult2D total(String identity, double lower,
			double upper, DoubleFunction<LocusPoint2D> curve) {
		return total(identity, lower, upper, curve, LocusMetricPolicy2D.cu1PublicV2());
	}

	private static LocusMetricResult2D total(String identity, double lower,
			double upper, DoubleFunction<LocusPoint2D> curve,
			LocusMetricPolicy2D policy) {
		return total(identity, lower, upper, curve, policy,
				new LocusMetricInstrumentation2D());
	}

	private static LocusMetricResult2D total(String identity, double lower,
			double upper, DoubleFunction<LocusPoint2D> curve, LocusMetricPolicy2D policy,
			LocusMetricInstrumentation2D instrumentation) {
		return compute(definition(identity, lower, upper, curve), policy,
				new LocusMetricSharedOwner2D(identity, instrumentation), instrumentation);
	}

	/** One closed component equal to the declared domain of a synthetic source. */
	private static LocusDefinition2D definition(String identity, double lower,
			double upper, DoubleFunction<LocusPoint2D> curve) {
		LocusInterval2D domain = new LocusInterval2D(lower, upper, true, true);
		ExplicitNumericDomainProvider2D provider = new ExplicitNumericDomainProvider2D(
				"cu1-estimator-parameter/v1", domain, Orientation.INCREASING, false,
				1E-14);
		LocusBranch2D branch = LocusV2TestBranches.branch(G7BMetricFixtures.BRANCH, domain,
				List.of(domain), Orientation.INCREASING, "cu1-estimator-fixture/v1",
				LocusLineage2D.unchanged(), EnumSet.of(BranchProperty.FINITE),
				LocusQuality2D.analyticDoubleSemantic());
		return new LocusDefinition2D(identity, 1, DefinitionStatus.VALID, provider,
				List.of(branch), (definition, semanticBranch, parameter, session) ->
						LocusEvaluation2D.valid(curve.apply(parameter),
								Regularity.UNKNOWN, semanticBranch.getQuality()),
				Determinism.POINTWISE_DETERMINISTIC, "cu1-estimator-fixture/v1",
				new LocusInstrumentation2D());
	}

	/** Computes like {@code AlgoLocusMetricV2}: a build failure becomes a typed result. */
	private static LocusMetricResult2D compute(LocusDefinition2D definition,
			LocusMetricPolicy2D policy, LocusMetricSharedOwner2D owner,
			LocusMetricInstrumentation2D instrumentation) {
		try (LocusMetricOwnerLease2D lease = owner.acquireLease()) {
			return new LocusMetricEngine2D().compute(new TotalLocusMetricQuery(
					definition.getLocusIdentity(), definition.getSemanticRevision(),
					policy), definition, new LocusMetricCapabilityHierarchy2D(
							List.of(new PolicyV2LocusMetricCapability2D())), owner,
					LocusMetricIndexMode.LAZY_COMPONENT_REVISION, instrumentation,
					"cu1-estimator-test");
		} catch (LocusMetricComponentBuildException exception) {
			return LocusMetricResults2D.failure(definition.getLocusIdentity(),
					definition.getSemanticRevision(), policy,
					exception.getComputationStatus(), Optional.empty(),
					exception.getDiagnostics());
		}
	}

	/** Coherent estimate: real error within the declared error, itself within tau. */
	private static void assertCoherentEstimate(LocusMetricResult2D rich,
			double reference, String where) {
		String message = where + ": " + describe(rich);
		assertEquals(MetricGuaranteeClass2D.ESTIMATED, rich.getGuaranteeClass(), message);
		double declared = rich.getErrorEvidence().getEstablishedAbsoluteAmount();
		double tolerance = rich.getErrorEvidence().getRequestedTolerance().orElseThrow();
		assertTrue(Math.abs(value(rich) - reference) <= declared, message
				+ " real error " + Math.abs(value(rich) - reference));
		assertTrue(declared <= tolerance, message + " tolerance " + tolerance);
		assertTrue(rich.isScalarAdmissible(), message);
		assertTrue(rich.getErrorEvidence().getAssumptions().stream().anyMatch(
				assumption -> assumption.startsWith("no feature narrower than h_probe")),
				message);
	}

	private static void assertNotCertified(LocusMetricResult2D rich, String where) {
		MetricGuaranteeClass2D guarantee = rich.getGuaranteeClass();
		String message = where + ": " + describe(rich);
		assertNotEquals(MetricGuaranteeClass2D.STRUCTURAL, guarantee, message);
		assertNotEquals(MetricGuaranteeClass2D.CERTIFIED_ENCLOSURE, guarantee, message);
		if (guarantee == MetricGuaranteeClass2D.ESTIMATED) {
			assertFalse(rich.getErrorEvidence().getAssumptions().isEmpty(), message);
		} else {
			assertEquals(MetricGuaranteeClass2D.NOT_ESTABLISHED, guarantee, message);
			assertFalse(rich.isScalarAdmissible(), message);
			assertTrue(rich.getDiagnostics().stream().map(MetricDiagnostic2D::getCode)
					.anyMatch(code -> code == MetricDiagnosticCode2D.WORK_BUDGET_EXHAUSTED
							|| code == MetricDiagnosticCode2D.CONSISTENCY_NOT_ESTABLISHED
							|| code == MetricDiagnosticCode2D.PARAMETER_RESOLUTION_REACHED),
					message);
		}
	}

	private static void assertBitwiseEqual(LocusMetricResult2D first,
			LocusMetricResult2D second) {
		assertEquals(Double.doubleToLongBits(value(first)),
				Double.doubleToLongBits(value(second)), describe(second));
		assertEquals(Double.doubleToLongBits(
				first.getErrorEvidence().getEstablishedAbsoluteAmount()),
				Double.doubleToLongBits(
						second.getErrorEvidence().getEstablishedAbsoluteAmount()));
		assertEquals(first.getErrorEvidence().getRequestedTolerance(),
				second.getErrorEvidence().getRequestedTolerance());
		assertEquals(first.getErrorEvidence().getRigorousEnclosure(),
				second.getErrorEvidence().getRigorousEnclosure());
		assertEquals(first.getGuaranteeClass(), second.getGuaranteeClass());
	}

	private static double probeSpacing(LocusMetricResult2D rich) {
		for (String assumption : rich.getErrorEvidence().getAssumptions()) {
			String prefix = "no feature narrower than h_probe = ";
			if (assumption.startsWith(prefix)) {
				return Double.parseDouble(assumption.substring(prefix.length(),
						assumption.indexOf(' ', prefix.length())));
			}
		}
		throw new AssertionError("No probe-spacing hypothesis: " + describe(rich));
	}

	private static double relativeError(LocusMetricResult2D rich) {
		return rich.getErrorEvidence().getEstablishedAbsoluteAmount() / value(rich);
	}

	private static double value(LocusMetricResult2D rich) {
		return rich.getMetricValue().getFiniteValue().orElseThrow(
				() -> new AssertionError(describe(rich)));
	}

	private static String describe(LocusMetricResult2D rich) {
		return rich.getGuaranteeClass() + " " + rich.getComputationStatus() + " "
				+ rich.getMetricValue().getFiniteValue() + " error "
				+ rich.getErrorEvidence().getAbsoluteEvidence() + " tolerance "
				+ rich.getErrorEvidence().getRequestedTolerance() + " "
				+ rich.getDiagnostics();
	}

	private static double derivative(DoubleUnaryOperator function, double x) {
		double step = 1E-7;
		return (function.applyAsDouble(x + step) - function.applyAsDouble(x - step))
				/ (2 * step);
	}

	/** Composite Simpson rule with an even number of intervals. */
	private static double simpson(DoubleUnaryOperator function, double lower,
			double upper, int intervals) {
		double step = (upper - lower) / intervals;
		double sum = function.applyAsDouble(lower) + function.applyAsDouble(upper);
		for (int index = 1; index < intervals; index++) {
			sum += (index % 2 == 1 ? 4 : 2)
					* function.applyAsDouble(lower + index * step);
		}
		return sum * step / 3;
	}
}
