/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.algos.AlgoLocusMetricV2;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusV2Factory;
import org.geocedg.common.kernel.locus.LocusV2Mode;
import org.geocedg.common.kernel.locus.metric.AnalyticLocusMetricCapability2D;
import org.geocedg.common.kernel.locus.metric.BetweenPositionsMetricQuery;
import org.geocedg.common.kernel.locus.metric.LocusMetricCapabilityHierarchy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricIndexMode;
import org.geocedg.common.kernel.locus.metric.LocusMetricInstrumentation2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricOwnerLease2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricSharedOwner2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricValueKind;
import org.geocedg.common.kernel.locus.metric.OpenBoundaryPolicy;
import org.geocedg.common.kernel.locus.metric.SamePositionPolicy;
import org.geocedg.common.kernel.locus.metric.TotalLocusMetricQuery;
import org.geocedg.common.kernel.locus.metric.TraversalDirection;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.ConstructionDefaults;
import org.geogebra.common.kernel.Path;
import org.geogebra.common.kernel.arithmetic.NumberValue;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.plugin.GeoClass;
import org.junit.jupiter.api.Test;

/** Productive rich-Geo, normal-DAG, P1 and lifecycle contracts. */
class LocusMetricProductiveLifecycleTest extends BaseUnitTest {
	private static final String BRANCH = "g7b.lifecycle.main";

	@Test
	void richGeoPublishesInNormalDagWithDedicatedAppendOnlyClass() {
		Fixture fixture = fixture();
		AlgoLocusMetricV2 metric = metric(fixture, "M1");
		GeoLocusMetricResult result = metric.getResult();
		assertTrue(result.isDefined());
		assertEquals(GeoClass.LOCUS_METRIC_RESULT,
				result.getGeoClassType());
		assertEquals(GeoClass.SHAPE_STADIUM.ordinal() + 1,
				GeoClass.LOCUS_V2.ordinal());
		assertEquals(GeoClass.LOCUS_V2.ordinal() + 1,
				GeoClass.LOCUS_METRIC_RESULT.ordinal());
		assertEquals(GeoClass.LOCUS_METRIC_RESULT.ordinal() + 1,
				GeoClass.LOCUS_INTERSECTION_RESULT.ordinal());
		assertEquals(GeoClass.values().length - 1,
				GeoClass.LOCUS_INTERSECTION_RESULT.ordinal());
		assertEquals(fixture.locus.getSemanticRevision(),
				result.getSourceSemanticRevision());
		assertEquals(2, result.getMetricResult().getMetricValue()
				.getFiniteValue().orElseThrow(), 0);
		assertTrue(getConstruction().getAlgoList().contains(metric));
		assertEquals(fixture.locus, metric.getInput(0));
	}

	@Test
	void richGeoIsNonNumericNonPathNonDrawableAndNonpersistent() {
		Fixture fixture = fixture();
		GeoLocusMetricResult result = metric(fixture, "M1").getResult();
		assertFalse(NumberValue.class.isAssignableFrom(result.getClass()));
		assertFalse(Path.class.isAssignableFrom(result.getClass()));
		assertFalse(result.isGeoElement3D());
		assertFalse(result.hasDrawable3D());
		assertNull(getApp().getEuclidianView1().newDrawable(result));
		assertEquals("", result.getXML());
		assertFalse(getApp().getXML().contains("locusmetricresult"));
		assertFalse(getKernel().createGeoElement(getConstruction(),
				"locusmetricresult") instanceof GeoLocusMetricResult);
	}

	@Test
	void explicitScalarAdapterPublishesOnlyAdmissibleFiniteValue() {
		Fixture fixture = fixture();
		GeoLocusMetricResult rich = metric(fixture, "M1").getResult();
		final AlgoLocusMetricScalarAdapter adapter =
				new AlgoLocusMetricScalarAdapter(getConstruction(), rich);
		assertTrue(rich.isScalarAdmissible());
		assertTrue(adapter.getScalarOutput().isDefined());
		assertEquals(2, adapter.getScalarOutput().getDouble(), 0);
		assertEquals(rich, adapter.getInput(0));
	}

	@Test
	void p1InvalidationNeverLeavesOldSuccessCurrentAfterSourceRevision() {
		Fixture fixture = fixture();
		final AlgoLocusMetricV2 metric = metric(fixture, "M1");
		final AlgoLocusMetricScalarAdapter adapter =
				new AlgoLocusMetricScalarAdapter(getConstruction(),
						metric.getResult());
		long oldRevision = fixture.locus.getSemanticRevision();
		fixture.source.setValue(2);
		fixture.source.updateCascade();
		assertEquals(oldRevision + 1, fixture.locus.getSemanticRevision());
		assertTrue(metric.getResult().isDefined());
		assertEquals(fixture.locus.getSemanticRevision(),
				metric.getResult().getSourceSemanticRevision());
		assertEquals(fixture.locus.getSemanticRevision(),
				metric.getQuery().getSemanticRevision());
		assertEquals(MetricValueKind.FINITE,
				metric.getResult().getMetricResult().getMetricValue().getKind());
		assertEquals(MetricComputationStatus.SUCCESS,
				metric.getResult().getMetricResult().getComputationStatus());
		assertEquals(2, metric.getResult().getMetricResult().getMetricValue()
				.getFiniteValue().orElseThrow(), 0);
		assertTrue(metric.getResult().isScalarAdmissible());
		assertTrue(adapter.getScalarOutput().isDefined());
		assertEquals(2, fixture.locus.getMetricSharedOwnerForDiagnostics()
				.statistics().getBuilds());
		assertEquals(1, fixture.locus.getMetricSharedOwnerForDiagnostics()
				.statistics().getRetainedEntries());
	}

	@Test
	void explicitRebindViaNewQueryRecoversAfterRevisionChange() {
		Fixture fixture = fixture();
		AlgoLocusMetricV2 stale = betweenMetric(fixture, "stale");
		fixture.source.setValue(2);
		fixture.source.updateCascade();
		assertEquals(MetricValueKind.ABSENT,
				stale.getResult().getMetricResult().getMetricValue().getKind());

		AlgoLocusMetricV2 rebound = betweenMetric(fixture, "rebound");
		assertTrue(rebound.getResult().isDefined());
		assertEquals(MetricComputationStatus.SUCCESS,
				rebound.getResult().getMetricResult().getComputationStatus());
		assertEquals(1, rebound.getResult().getMetricResult().getMetricValue()
				.getFiniteValue().orElseThrow(), 0);
	}

	@Test
	void copyCopyInternalAndSetNeverInheritCurrentRevisionPayload() {
		Fixture fixture = fixture();
		GeoLocusMetricResult source = metric(fixture, "M1").getResult();
		GeoLocusMetricResult copy =
				(GeoLocusMetricResult) source.copy();
		GeoLocusMetricResult internal =
				(GeoLocusMetricResult) source.copyInternal(getConstruction());
		assertFalse(copy.isDefined());
		assertFalse(internal.isDefined());
		assertEquals(0, copy.getSourceSemanticRevision());
		copy.beginMetricRevision(source.getSourceSemanticRevision());
		copy.publishMetricResult(source.getSourceSemanticRevision(),
				source.getMetricResult());
		assertTrue(copy.isDefined());
		copy.set(source);
		assertFalse(copy.isDefined());
		assertEquals(0, copy.getSourceSemanticRevision());
	}

	@Test
	void multipleConsumersShareOwnerAndLastRemovalReleasesState() {
		Fixture fixture = fixture();
		AlgoLocusMetricV2 first = metric(fixture, "M1");
		AlgoLocusMetricV2 second = metric(fixture, "M2");
		LocusMetricSharedOwner2D owner =
				fixture.locus.getMetricSharedOwnerForDiagnostics();
		assertEquals(2, owner.getActiveLeaseCount());
		assertEquals(1, owner.statistics().getBuilds());
		assertEquals(1, fixture.locus.getMetricInstrumentation().snapshot()
				.getCrossResultHits());
		first.remove();
		assertEquals(1, owner.getActiveLeaseCount());
		assertFalse(owner.isReleased());
		second.remove();
		assertEquals(0, owner.getActiveLeaseCount());
		assertTrue(owner.isReleased());
		assertEquals(0, owner.statistics().getRetainedEntries());
		assertTrue(fixture.locus.isDefined());
	}

	@Test
	void sourceUndefinedAndRemovalSynchronouslyClearSharedState() {
		Fixture fixture = fixture();
		final AlgoLocusMetricV2 metric = metric(fixture, "M1");
		LocusMetricSharedOwner2D owner =
				fixture.locus.getMetricSharedOwnerForDiagnostics();
		assertEquals(1, owner.statistics().getRetainedEntries());
		fixture.locus.setUndefined();
		assertEquals(0, owner.statistics().getRetainedEntries());
		fixture.source.updateCascade();
		assertTrue(fixture.locus.isDefined());
		fixture.locus.remove();
		assertTrue(owner.isReleased());
		assertFalse(getConstruction().getAlgoList().contains(metric));
	}

	@Test
	void failedBuildPublishesRichFailureWithoutIndexEntry() {
		Fixture fixture = fixture();
		LocusMetricCapabilityHierarchy2D failing =
				new LocusMetricCapabilityHierarchy2D(List.of(
						new AnalyticLocusMetricCapability2D("failing/v1",
								(branch, start, end) -> {
									throw new IllegalStateException("injected");
								})));
		AlgoLocusMetricV2 metric = new AlgoLocusMetricV2(getConstruction(),
				fixture.locus, new TotalLocusMetricQuery(
						fixture.locus.getLocusIdentity(),
						fixture.locus.getSemanticRevision(),
						LocusMetricPolicy2D.initial()),
				failing, LocusMetricIndexMode.LAZY_COMPONENT_REVISION,
				"failing", new GeoElement[0]);
		assertTrue(metric.getResult().isDefined());
		assertEquals(MetricValueKind.ABSENT,
				metric.getResult().getMetricResult().getMetricValue().getKind());
		assertEquals(MetricComputationStatus.NUMERICAL_FAILURE,
				metric.getResult().getMetricResult().getComputationStatus());
		assertEquals(0, fixture.locus.getMetricSharedOwnerForDiagnostics()
				.statistics().getRetainedEntries());
		assertEquals(0, fixture.locus.getMetricSharedOwnerForDiagnostics()
				.statistics().getActiveBuilds());
	}

	@Test
	void ownerRejectsOffThreadLeaseReleaseBeforeMutation()
			throws InterruptedException {
		LocusMetricSharedOwner2D owner = new LocusMetricSharedOwner2D(
				"thread-confined-owner", new LocusMetricInstrumentation2D());
		LocusMetricOwnerLease2D lease = owner.acquireLease();
		Throwable[] failure = new Throwable[1];
		Thread worker = new Thread(() -> {
			try {
				lease.close();
			} catch (RuntimeException exception) {
				failure[0] = exception;
			}
		});
		worker.start();
		worker.join();

		assertTrue(failure[0] instanceof IllegalStateException);
		assertEquals(1, owner.getActiveLeaseCount());
		lease.close();
		assertEquals(0, owner.getActiveLeaseCount());
		assertTrue(owner.isReleased());
	}

	@Test
	void labelsSelectionAndDefaultsRemainDiagnosticOnly() {
		Fixture fixture = fixture();
		GeoLocusMetricResult result = metric(fixture, "M1").getResult();
		result.setLabel("internalMetric");
		getApp().getSelectionManager().addSelectedGeo(result);
		assertEquals("internalMetric", result.getLabelSimple());
		assertTrue(getApp().getSelectionManager().containsSelectedGeo(result));
		assertEquals(ConstructionDefaults.DEFAULT_LINE,
				getConstruction().getConstructionDefaults()
						.getDefaultType(result));
		assertFalse(result.isAlgebraViewEditable());
	}

	private AlgoLocusMetricV2 metric(Fixture fixture, String identity) {
		return new AlgoLocusMetricV2(getConstruction(), fixture.locus,
				new TotalLocusMetricQuery(fixture.locus.getLocusIdentity(),
						fixture.locus.getSemanticRevision(),
						LocusMetricPolicy2D.initial()),
				G7BMetricFixtures.analytic(1, "line/v1"),
				LocusMetricIndexMode.LAZY_COMPONENT_REVISION, identity,
				new GeoElement[0]);
	}

	private AlgoLocusMetricV2 betweenMetric(Fixture fixture, String identity) {
		BetweenPositionsMetricQuery query = new BetweenPositionsMetricQuery(
				G7BMetricFixtures.bind(fixture.locus.getSemanticDefinition(),
						BRANCH, -0.5),
				G7BMetricFixtures.bind(fixture.locus.getSemanticDefinition(),
						BRANCH, 0.5),
				TraversalDirection.FORWARD, OpenBoundaryPolicy.STRICT,
				SamePositionPolicy.ZERO_LENGTH, LocusMetricPolicy2D.initial());
		return new AlgoLocusMetricV2(getConstruction(), fixture.locus, query,
				G7BMetricFixtures.analytic(1, "line/v1"),
				LocusMetricIndexMode.LAZY_COMPONENT_REVISION, identity,
				new GeoElement[0]);
	}

	private Fixture fixture() {
		ExplicitNumericDomainProvider2D provider =
				new ExplicitNumericDomainProvider2D("lifecycle-param/v1",
						new LocusInterval2D(-1, 1, true, true),
						Orientation.INCREASING, false,
						G7BMetricFixtures.EPS_DOMAIN);
		LocusBranch2D branch = LocusV2Factory.fullDomainBranch(BRANCH, provider,
				"g7b-lifecycle/v1",
				EnumSet.of(BranchProperty.FINITE));
		GeoNumeric source = new GeoNumeric(getConstruction(), 1);
		source.setLabel("g7bMetricSource");
		GeoLocusV2 locus = LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				getConstruction(), "g7b-lifecycle-" + source.hashCode(),
				source, provider, Collections.singletonList(branch),
				(value, semanticBranch, parameter, session) ->
						new LocusPoint2D(value + parameter, 0),
				"g7b-lifecycle-line/v1");
		locus.setLabel("g7bMetricLocus");
		return new Fixture(source, locus);
	}

	private static final class Fixture {
		private final GeoNumeric source;
		private final GeoLocusV2 locus;

		private Fixture(GeoNumeric source, GeoLocusV2 locus) {
			this.source = source;
			this.locus = locus;
		}
	}
}
