/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Objects;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.locus.G8AIntersectionNumerics.FactorizationProof;
import org.geocedg.common.locus.G8AIntersectionNumerics.Problem;
import org.geocedg.common.locus.G8AIntersectionNumerics.RootProof;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.Completeness;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.ComputationStatus;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.Currentness;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.GeometryKind;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.Result;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.SourceBinding;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.SupportLevel;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.WorkCounters;
import org.geocedg.common.locus.G8ATargetAdapters.LineTarget;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.Algos;
import org.geogebra.common.kernel.arithmetic.ValueType;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.plugin.GeoClass;
import org.junit.jupiter.api.Test;

/** Actual GeoElement/AlgoElement/DAG lifecycle characterization, test-private. */
class G8AIntersectionKernelLifecycleCharacterizationTest extends BaseUnitTest {

	@Test
	void richGeoPublishesAtomicallyAsNonnumericNormalDagOutput() {
		DynamicFixture fixture = dynamicFixture();
		TestIntersectionAlgo algorithm = fixture.algorithm;
		TestRichIntersectionGeo rich = algorithm.getRichResult();
		assertTrue(rich.isDefined());
		assertEquals(algorithm, rich.getParentAlgorithm());
		assertEquals(fixture.locus, algorithm.getInput(0));
		assertEquals(fixture.targetParameter, algorithm.getInput(1));
		assertEquals(ValueType.VOID, rich.getValueType());
		assertFalse(GeoNumeric.class.isAssignableFrom(rich.getClass()));
		assertTrue(getConstruction().getAlgoList().contains(algorithm));
	}

	@Test
	void identifiedSolutionDrivesLaterCedgConstructionThroughNormalDag() {
		DynamicFixture fixture = dynamicFixture();
		String token = fixture.algorithm.getRichResult().getResult().solutions()
				.get(0).durableIdentity().rootToken();
		final TestDownstreamAlgo downstream = new TestDownstreamAlgo(getConstruction(),
				fixture.algorithm.getRichResult(), token);
		assertEquals(10.25, downstream.getPoint().getInhomX(), 1E-15);
		assertEquals(-5, downstream.getPoint().getInhomY(), 1E-15);

		long priorRevision = fixture.locus.getSemanticRevision();
		fixture.source.setValue(2);
		fixture.source.updateCascade();
		assertEquals(priorRevision + 1, fixture.locus.getSemanticRevision());
		assertEquals(token, fixture.algorithm.getRichResult().getResult().solutions()
				.get(0).durableIdentity().rootToken());
		assertEquals(10.25, downstream.getPoint().getInhomX(), 1E-15);
		assertEquals(-3, downstream.getPoint().getInhomY(), 1E-15);
	}

	@Test
	void targetMotionUpdatesRootAndDownstreamWithoutHiddenListener() {
		DynamicFixture fixture = dynamicFixture();
		String token = fixture.algorithm.getRichResult().getResult().solutions()
				.get(0).durableIdentity().rootToken();
		TestDownstreamAlgo downstream = new TestDownstreamAlgo(getConstruction(),
				fixture.algorithm.getRichResult(), token);
		fixture.targetParameter.setValue(0.75);
		fixture.targetParameter.updateCascade();
		assertEquals(0.75, fixture.algorithm.getRichResult().getResult().solutions()
				.get(0).revisionEvidence().semanticParameter(), 0);
		assertEquals(10.75, downstream.getPoint().getInhomX(), 1E-15);
	}

	@Test
	void failedRecomputePublishesCoherentCurrentFailureWithoutStalePoint() {
		DynamicFixture fixture = dynamicFixture();
		String token = fixture.algorithm.getRichResult().getResult().solutions()
				.get(0).durableIdentity().rootToken();
		final TestDownstreamAlgo downstream = new TestDownstreamAlgo(
				getConstruction(), fixture.algorithm.getRichResult(), token);
		fixture.algorithm.injectFailure = true;
		fixture.targetParameter.setValue(0.5);
		fixture.targetParameter.updateCascade();
		Result failure = fixture.algorithm.getRichResult().getResult();
		assertTrue(fixture.algorithm.getRichResult().isDefined());
		assertEquals(ComputationStatus.NUMERICAL_FAILURE, failure.status());
		assertEquals(GeometryKind.UNRESOLVED, failure.geometryKind());
		assertTrue(failure.solutions().isEmpty());
		assertFalse(downstream.getPoint().isDefined());
		assertFalse(fixture.algorithm.getRichResult().publicationActive);
	}

	@Test
	void failedComputationRecoversThroughNormalDagWithFreshCurrentSnapshot() {
		DynamicFixture fixture = dynamicFixture();
		String token = fixture.algorithm.getRichResult().getResult().solutions()
				.get(0).durableIdentity().rootToken();
		final TestDownstreamAlgo downstream = new TestDownstreamAlgo(
				getConstruction(), fixture.algorithm.getRichResult(), token);
		fixture.algorithm.injectFailure = true;
		fixture.targetParameter.setValue(0.5);
		fixture.targetParameter.updateCascade();
		assertFalse(downstream.getPoint().isDefined());
		fixture.algorithm.injectFailure = false;
		fixture.targetParameter.setValue(0.75);
		fixture.targetParameter.updateCascade();
		Result recovered = fixture.algorithm.getRichResult().getResult();
		assertEquals(ComputationStatus.SUCCESS, recovered.status());
		assertEquals(Currentness.CURRENT, recovered.currentness());
		assertEquals(token,
				recovered.solutions().get(0).durableIdentity().rootToken());
		assertTrue(downstream.getPoint().isDefined());
		assertEquals(10.75, downstream.getPoint().getInhomX(), 1E-15);
	}

	@Test
	void oneThreeTenHundredConsumersRemainBoundedAndRemoveNormally() {
		for (int consumerCount : new int[] {1, 3, 10, 100}) {
			DynamicFixture fixture = dynamicFixture();
			fixture.algorithm.getRichResult().setLabel(
					"g8aConsumerSource" + consumerCount);
			List<TestPointProjectionAlgo> consumers = new java.util.ArrayList<>();
			for (int index = 0; index < consumerCount; index++) {
				consumers.add(new TestPointProjectionAlgo(getConstruction(),
						fixture.algorithm.getRichResult(), 1));
			}
			assertTrue(consumers.stream().allMatch(
					consumer -> consumer.slotCount() == 1
							&& consumer.getPoint(0).isDefined()));
			for (int index = 0; index < consumers.size(); index += 2) {
				consumers.get(index).remove();
			}
			assertTrue(fixture.algorithm.getRichResult().isDefined());
			for (int index = 1; index < consumers.size(); index += 2) {
				consumers.get(index).remove();
			}
			assertEquals(0, fixture.algorithm.getRichResult().getResult().work()
					.retainedIndexEntries());
			assertEquals(0, fixture.algorithm.getRichResult().getResult().work()
					.retainedRootHistoryEntries());
			fixture.algorithm.remove();
		}
	}

	@Test
	void copyAndSetNeverImportRevisionBoundPayloadOrSolverState() {
		DynamicFixture fixture = dynamicFixture();
		TestRichIntersectionGeo source = fixture.algorithm.getRichResult();
		TestRichIntersectionGeo copy =
				(TestRichIntersectionGeo) source.copyInternal(getConstruction());
		assertFalse(copy.isDefined());
		assertEquals(0, copy.sourceRevision);
		assertNull(copy.getResult());
		copy.set(source);
		assertFalse(copy.isDefined());
		assertEquals("explicit recompute required after set/copy", copy.diagnostic);
	}

	@Test
	void boundedPointProjectionRefusesIncompleteSetSemantics() {
		DynamicFixture fixture = dynamicFixture();
		Result complete = fixture.algorithm.getRichResult().getResult();
		TestRichIntersectionGeo standalone = new TestRichIntersectionGeo(
				getConstruction(), complete.sourceBinding().sourcePairIdentity());
		standalone.beginRevision(complete.sourceBinding().locusRevision());
		standalone.publish(complete.sourceBinding().locusRevision(), complete);
		TestPointProjectionAlgo projection = new TestPointProjectionAlgo(
				getConstruction(), standalone, 4);
		assertTrue(projection.getPoint(0).isDefined());

		Problem problem = fixture.algorithm.problemForCurrentInputs();
		Result incomplete = G8AIntersectionNumerics.conservativeBroadPhase(problem,
				List.of(new G8AIntersectionNumerics.ParameterInterval(-2, -1)));
		assertEquals(Completeness.NOT_ESTABLISHED, incomplete.completeness());
		standalone.beginRevision(complete.sourceBinding().locusRevision() + 1);
		standalone.publish(complete.sourceBinding().locusRevision() + 1,
				incomplete);
		projection.compute();
		for (int index = 0; index < projection.slotCount(); index++) {
			assertFalse(projection.getPoint(index).isDefined());
		}
	}

	@Test
	void outputHandlerRetainsBoundedSlotsButCannotBeSemanticIdentityAuthority() {
		DynamicFixture fixture = dynamicFixture();
		TestPointProjectionAlgo projection = new TestPointProjectionAlgo(
				getConstruction(), fixture.algorithm.getRichResult(), 4);
		assertEquals(4, projection.slotCount());
		assertTrue(projection.getPoint(0).isDefined());
		assertFalse(projection.getPoint(1).isDefined());
		fixture.targetParameter.setValue(3);
		fixture.targetParameter.updateCascade();
		assertEquals(GeometryKind.EMPTY,
				fixture.algorithm.getRichResult().getResult().geometryKind());
		assertFalse(projection.getPoint(0).isDefined());
		assertEquals(4, projection.slotCount());
	}

	@Test
	void labelSelectionRemovalAndXmlStayInternalAndNonpersistent() {
		DynamicFixture fixture = dynamicFixture();
		TestRichIntersectionGeo rich = fixture.algorithm.getRichResult();
		rich.setLabel("g8aInternalResult");
		assertEquals("g8aInternalResult", rich.getLabelSimple());
		assertEquals("", rich.getXML());
		assertFalse(getApp().getXML().contains("g8aInternalResult"));
		assertEquals(GeoClass.DEFAULT, rich.getGeoClassType());
		assertEquals(GeoClass.LOCUS_METRIC_RESULT.ordinal() + 1,
				GeoClass.LOCUS_INTERSECTION_RESULT.ordinal());
		assertEquals(GeoClass.LOCUS_INTERSECTION_RESULT,
				GeoClass.values()[GeoClass.values().length - 1]);
		fixture.algorithm.remove();
		assertFalse(getConstruction().getAlgoList().contains(fixture.algorithm));
		assertFalse(rich.isDefined());
	}

	private DynamicFixture dynamicFixture() {
		G8AIntersectionFixtures.Fixture semantic = G8AIntersectionFixtures.create(
				getConstruction(), "g8a-dag-" + getConstruction().getAlgoList().size(),
				-2, 2, true, true, false,
				(source, branch, parameter, session) ->
						new LocusPoint2D(parameter, source),
				parameter -> new LocusPoint2D(1, 0));
		semantic.source().setLabel(null);
		GeoNumeric target = new GeoNumeric(getConstruction(), 0.25);
		TestIntersectionAlgo algorithm = new TestIntersectionAlgo(getConstruction(),
				semantic.locus(), target, semantic.branchKey(),
				semantic.componentKey(), semantic.semanticCurve(), semantic.lower(),
				semantic.upper());
		return new DynamicFixture(semantic.source(), semantic.locus(), target,
				algorithm);
	}

	private record DynamicFixture(GeoNumeric source, GeoLocusV2 locus,
			GeoNumeric targetParameter, TestIntersectionAlgo algorithm) {
	}

	private static final class TestRichIntersectionGeo extends GeoElement {
		private final String sourcePairIdentity;
		private long sourceRevision;
		private Result result;
		private boolean explicitlyUndefined;
		private boolean publicationActive;
		private String diagnostic = "awaiting first publication";

		TestRichIntersectionGeo(Construction construction,
				String sourcePairIdentity) {
			super(construction);
			this.sourcePairIdentity = Objects.requireNonNull(sourcePairIdentity);
			setEuclidianVisible(false);
			setAuxiliaryObject(true);
		}

		void beginRevision(long revision) {
			sourceRevision = revision;
			result = null;
			explicitlyUndefined = false;
			publicationActive = true;
			diagnostic = "computing current revision";
		}

		void publish(long revision, Result candidate) {
			if (revision != sourceRevision) {
				throw new IllegalArgumentException("Publication revision mismatch");
			}
			result = Objects.requireNonNull(candidate);
			publicationActive = false;
			diagnostic = "";
		}

		Result getResult() {
			return result;
		}

		@Override
		public GeoClass getGeoClassType() {
			return GeoClass.DEFAULT;
		}

		@Override
		public ValueType getValueType() {
			return ValueType.VOID;
		}

		@Override
		public String translatedTypeString() {
			return "G8A test-private rich intersection result";
		}

		@Override
		public String translatedTypeStringForAlgebraView() {
			return translatedTypeString();
		}

		@Override
		public GeoElement copy() {
			return new TestRichIntersectionGeo(cons, sourcePairIdentity);
		}

		@Override
		public GeoElement copyInternal(Construction targetConstruction) {
			return new TestRichIntersectionGeo(targetConstruction,
					sourcePairIdentity);
		}

		@Override
		public void set(GeoElementND geo) {
			result = null;
			sourceRevision = 0;
			explicitlyUndefined = true;
			publicationActive = false;
			diagnostic = "explicit recompute required after set/copy";
		}

		@Override
		public boolean isDefined() {
			return !explicitlyUndefined && sourceRevision > 0 && result != null;
		}

		@Override
		public void setUndefined() {
			result = null;
			explicitlyUndefined = true;
			publicationActive = false;
			diagnostic = "undefined";
		}

		@Override
		public void doRemove() {
			setUndefined();
			super.doRemove();
		}

		@Override
		public String toValueString(StringTemplate template) {
			return isDefined() ? "G8ATestIntersection[revision=" + sourceRevision
					+ ", kind=" + result.geometryKind() + "]"
					: "G8ATestIntersection[unpublished]";
		}

		@Override
		protected boolean showInEuclidianView() {
			return false;
		}

		@Override
		public boolean isAlgebraViewEditable() {
			return false;
		}

		@Override
		public void getXML(boolean getListenersToo, XMLStringBuilder builder) {
			// Test-private G8A probe has no persistence contract.
		}
	}

	private static final class TestIntersectionAlgo extends AlgoElement {
		private final GeoLocusV2 locus;
		private final GeoNumeric targetParameter;
		private final String branchKey;
		private final String componentKey;
		private final G8AIntersectionNumerics.Curve2D curve;
		private final double lower;
		private final double upper;
		private final TestRichIntersectionGeo rich;
		private long targetStamp;
		private boolean injectFailure;

		TestIntersectionAlgo(Construction construction, GeoLocusV2 locus,
				GeoNumeric targetParameter, String branchKey, String componentKey,
				G8AIntersectionNumerics.Curve2D curve, double lower, double upper) {
			super(construction, false);
			this.locus = locus;
			this.targetParameter = targetParameter;
			this.branchKey = branchKey;
			this.componentKey = componentKey;
			this.curve = curve;
			this.lower = lower;
			this.upper = upper;
			rich = new TestRichIntersectionGeo(construction,
					locus.getLocusIdentity() + "+dynamic-vertical-line");
			setInputOutput();
			setDependencies();
			compute();
		}

		@Override
		protected void setInputOutput() {
			input = new GeoElement[] {locus, targetParameter};
			setOnlyOutput(rich);
		}

		@Override
		public void compute() {
			long revision = Math.max(1, locus.getSemanticRevision());
			rich.beginRevision(revision);
			targetStamp++;
			try {
				if (injectFailure) {
					throw new IllegalStateException("injected G8A failure");
				}
				Result candidate = G8AIntersectionNumerics.analyticFactorization(
						problemForCurrentInputs());
				rich.publish(revision, candidate);
			} catch (RuntimeException exception) {
				WorkCounters counters = new WorkCounters();
				counters.failedPrivateComputations++;
				counters.publishedSnapshots++;
				counters.completenessNotEstablishedResults++;
				SourceBinding binding = new SourceBinding(
						locus.getLocusIdentity() + "+dynamic-vertical-line",
						locus.getLocusIdentity(), revision, "dynamic-vertical-line",
						targetStamp, "g8a-measured-candidate/v1");
				Result failure = new Result(binding,
						ComputationStatus.NUMERICAL_FAILURE,
						Completeness.NOT_ESTABLISHED, GeometryKind.UNRESOLVED,
						Currentness.CURRENT, SupportLevel.UNSUPPORTED,
						NumericGuarantee.FLOATING_POINT_UNCERTIFIED, List.of(),
						counters.snapshot(), List.of(exception.getClass().getSimpleName()
								+ ": " + exception.getMessage()));
				rich.publish(revision, failure);
			}
		}

		Problem problemForCurrentInputs() {
			double root = targetParameter.getDouble();
			LineTarget target = new LineTarget("dynamic-vertical-line", 1, 0,
					-root);
			FactorizationProof proof = new FactorizationProof(1,
					List.of(new RootProof("unique-root", root, 1)), false,
					"x(t)-target");
			return new Problem(
					locus.getLocusIdentity() + "+dynamic-vertical-line",
					locus.getLocusIdentity(), Math.max(1, locus.getSemanticRevision()),
					target.identity(), targetStamp, branchKey,
					branchKey + "/lineage-v1", componentKey, "topology-1", lower,
					upper, true, true, false, curve, target, proof,
					G8AIntersectionSemanticModel.Policy.measuredCandidate(),
					G8AIntersectionSemanticModel.WorkBudget.measuredCandidate());
		}

		TestRichIntersectionGeo getRichResult() {
			return rich;
		}

		@Override
		public Algos getClassName() {
			return Algos.Expression;
		}
	}

	private static final class TestDownstreamAlgo extends AlgoElement {
		private final TestRichIntersectionGeo rich;
		private final String selectedToken;
		private final GeoPoint point;

		TestDownstreamAlgo(Construction construction,
				TestRichIntersectionGeo rich, String selectedToken) {
			super(construction, false);
			this.rich = rich;
			this.selectedToken = selectedToken;
			point = new GeoPoint(construction);
			setInputOutput();
			setDependencies();
			compute();
		}

		@Override
		protected void setInputOutput() {
			input = new GeoElement[] {rich};
			setOnlyOutput(point);
		}

		@Override
		public void compute() {
			if (!rich.isDefined() || !rich.getResult().pointProjectionAdmissible()) {
				point.setUndefined();
				return;
			}
			rich.getResult().solutions().stream()
					.filter(solution -> solution.durableIdentity().rootToken()
							.equals(selectedToken))
					.findFirst().ifPresentOrElse(solution -> point.setCoords(
							solution.point().getX() + 10,
							solution.point().getY() - 5, 1), point::setUndefined);
		}

		GeoPoint getPoint() {
			return point;
		}

		@Override
		public Algos getClassName() {
			return Algos.Expression;
		}
	}

	private static final class TestPointProjectionAlgo extends AlgoElement {
		private final TestRichIntersectionGeo rich;
		private final int maximumSlots;
		private final OutputHandler<GeoPoint> points;

		TestPointProjectionAlgo(Construction construction,
				TestRichIntersectionGeo rich, int maximumSlots) {
			super(construction, false);
			this.rich = rich;
			this.maximumSlots = maximumSlots;
			points = new OutputHandler<>(() -> {
				GeoPoint point = new GeoPoint(cons);
				point.setUndefined();
				point.setParentAlgorithm(this);
				return point;
			});
			points.adjustOutputSize(maximumSlots, false);
			setInputOutput();
			setDependencies();
			compute();
		}

		@Override
		protected void setInputOutput() {
			input = new GeoElement[] {rich};
		}

		@Override
		public void compute() {
			for (int index = 0; index < points.size(); index++) {
				points.getElement(index).setUndefined();
			}
			if (!rich.isDefined() || !rich.getResult().pointProjectionAdmissible()
					|| rich.getResult().solutions().size() > maximumSlots) {
				return;
			}
			for (int index = 0; index < rich.getResult().solutions().size(); index++) {
				LocusPoint2D value = rich.getResult().solutions().get(index).point();
				points.getElement(index).setCoords(value.getX(), value.getY(), 1);
			}
		}

		GeoPoint getPoint(int index) {
			return points.getElement(index);
		}

		int slotCount() {
			return points.size();
		}

		@Override
		public Algos getClassName() {
			return Algos.Expression;
		}
	}
}
