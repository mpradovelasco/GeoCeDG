/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.intersection.LocalContinuationEvidence2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointContinuation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionWorkBudget2D;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/** R4 adaptive request-path proof, with no geometric mutation of internal charts. */
class R4PointContinuationChainTest extends R4ContinuationTestBase {
	@Test void regularPositiveProjectionSheetCanUseSmallChartsWithoutAParameterRadiusFloor() {
		policy = LocusPointInteractionPolicy2D.initial(.1);
		add("c=Circle((0,0),1)"); add("N=Point(c)");
		add("axis=Line((0,0),(1,0))"); add("normal=PerpendicularLine(N,axis)");
		add("Q=Intersect(axis,normal)"); GeoLocusV2 source = add("L=LocusV2(Q,N)");
		GeoPoint point = offered(source, .1, "P");
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		var id = getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point);
		var component = address(point).getComponentLineageKey();
		for (double parameter : new double[] {.099, .08, .06, .04, .03, .025, .024,
				.02, .018, .016, .014, .012, .011, .010}) {
			var target = at(source, parameter); var result = move(point, context, target.getX(), target.getY());
			assertEquals(parameter, address(point).getCanonicalParameter(), 3e-11);
			assertTrue(address(point).getCanonicalParameter() > 0, "Never change to the negative preimage");
			assertEquals(0, address(point).getPeriodicLift());
			assertEquals(component, address(point).getComponentLineageKey());
			assertEquals(id, getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point));
			assertTrue(result.getIntervalPasses() <= policy.getWorkBudget().getMaximumSubdivisions());
		}
	}

	@Test void oneEventCompletesTheIndependentChordAndMatchesTwentyFourSubrequests() {
		GeoLocusV2 source = meetCircle();
		GeoPoint point = offered(source, .1, "P");
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		move(point, context, Math.cos(.11), Math.sin(.11));
		GeoPoint dependent = add("Dependent=P+(1,2)");
		var id = getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point);
		var old = address(point); int count = getConstruction().getGeoSetConstructionOrder().size();
		long start = System.nanoTime();
		var result = move(point, context, Math.cos(1.91), Math.sin(1.91));
		long elapsed = System.nanoTime() - start;
		assertTrue(result.getContinuationCharts() > 1);
		assertTrue(result.getContinuationSubdivisions() > 0);
		assertTrue(result.getContinuationChartAttempts() > result.getContinuationCharts());
		assertEquals(1.91, lifted(point), 3e-11);
		assertEquals(old.getComponentLineageKey(), address(point).getComponentLineageKey());
		assertEquals(id, getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point));
		assertEquals(count, getConstruction().getGeoSetConstructionOrder().size());
		assertEquals(point.inhomX + 1, dependent.inhomX, 0);
		assertEquals(point.inhomY + 2, dependent.inhomY, 0);
		var metrics = getConstruction().getLastNativeTransactionMetrics();
		assertNotNull(metrics); assertEquals(0, metrics.dagCalls);
		var repeated = move(point, context, Math.cos(1.91), Math.sin(1.91));
		assertFalse(repeated.hasChanged()); assertEquals(0, repeated.getDagPropagationNanos());
		assertSame(metrics, getConstruction().getLastNativeTransactionMetrics());

		GeoPoint stepped = offered(source, .1, "Stepped");
		var steppedContext = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(stepped);
		move(stepped, steppedContext, Math.cos(.11), Math.sin(.11));
		long steppedNanos = 0, steppedWork = 0, steppedRuns = 0, steppedCharts = 0;
		for (int step = 1; step <= 24; step++) {
			double fraction = step / 24.0;
			double x = (1 - fraction) * Math.cos(.11) + fraction * Math.cos(1.91);
			double y = (1 - fraction) * Math.sin(.11) + fraction * Math.sin(1.91);
			start = System.nanoTime(); var portion = move(stepped, steppedContext, x, y);
			steppedNanos += System.nanoTime() - start; steppedWork += portion.getIntervalPasses();
			steppedRuns += portion.getContinuationIntervalEvaluations(); steppedCharts += portion.getContinuationCharts();
		}
		assertEquals(lifted(point), lifted(stepped), 3e-11);
		assertEquals(point.inhomX, stepped.inhomX, 3e-11); assertEquals(point.inhomY, stepped.inhomY, 3e-11);
		System.out.println("R4_CHAIN_PERFORMANCE headless=true singleNanos=" + elapsed
				+ " resolutionNanos=" + result.getResolutionNanos() + " dagNanos=" + result.getDagPropagationNanos()
				+ " nativeCaptureNanos=" + metrics.nativeCaptureNanos + " xmlCaptureNanos=" + metrics.xmlCaptureNanos
				+ " attempts=" + result.getContinuationChartAttempts() + " charts=" + result.getContinuationCharts()
				+ " subdivisions=" + result.getContinuationSubdivisions() + " intervalRuns=" + result.getContinuationIntervalEvaluations()
				+ " correspondence=" + result.getContinuationCorrespondenceChecks() + " proofWork=" + result.getIntervalPasses()
				+ " semanticEvaluations=" + result.getSemanticEvaluations() + " stepped24Nanos=" + steppedNanos
				+ " stepped24Work=" + steppedWork + " stepped24IntervalRuns=" + steppedRuns + " stepped24Charts=" + steppedCharts);
	}

	@Test void reverseChainAndCertifiedSeamRetainLiftComponentAndNoopIdentity() {
		GeoLocusV2 source = meetCircle(); GeoPoint point = offered(source, 2.9, "P");
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		move(point, context, Math.cos(3), Math.sin(3));
		var component = address(point).getComponentLineageKey();
		move(point, context, Math.cos(4.18), Math.sin(4.18));
		assertEquals(4.18, lifted(point), 3e-11); assertEquals(1, address(point).getPeriodicLift());
		move(point, context, Math.cos(2.9), Math.sin(2.9));
		assertEquals(2.9, lifted(point), 3e-11); assertEquals(0, address(point).getPeriodicLift());
		assertEquals(component, address(point).getComponentLineageKey());
	}

	@Test void cumulativeBudgetExhaustionCannotPublishAnInternalChart() {
		GeoLocusV2 source = meetCircle(); GeoPoint point = offered(source, .1, "P");
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		move(point, context, Math.cos(.11), Math.sin(.11));
		var old = address(point); String xml = getApp().getXML();
		var snapshot = getConstruction().getLastNativeTransactionMetrics();
		var limited = new LocusPointInteractionPolicy2D(.5, 1E-10, 1E-12,
				new LocusPointInteractionWorkBudget2D(32768, 100, 80, 1024));
		var result = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				Math.cos(1.91), Math.sin(1.91), limited, context);
		assertEquals(LocusPointContinuation2D.Status.BUDGET_EXHAUSTED, result.getStatus());
		assertEquals(100, result.getIntervalPasses());
		assertTrue(result.getContinuationSubdivisions() > 0);
		assertEquals(old, address(point)); assertEquals(xml, getApp().getXML());
		assertSame(snapshot, getConstruction().getLastNativeTransactionMetrics());
		assertEquals(0, result.getDagPropagationNanos());
	}

	@Test void uncertifiableFinalProjectionAfterCertifiedPrefixesLeavesPointAndDagUntouched() {
		GeoLocusV2 source = meetCircle(); GeoPoint point = offered(source, .1, "P");
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		move(point, context, Math.cos(.11), Math.sin(.11));
		var old = address(point); String xml = getApp().getXML();
		var snapshot = getConstruction().getLastNativeTransactionMetrics();
		var result = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point, 0, 0, policy, context);
		assertFalse(result.isEstablished()); assertTrue(result.getContinuationCharts() > 0);
		assertEquals(old, address(point)); assertEquals(xml, getApp().getXML());
		assertSame(snapshot, getConstruction().getLastNativeTransactionMetrics());
		assertEquals(0, result.getDagPropagationNanos()); assertTrue(context.isStopped());
	}

	@Test void publicationFreshnessFailureAfterWholeChainHasNoPartialAddressOrSnapshot() {
		GeoLocusV2 source = meetCircle(); GeoPoint point = offered(source, .1, "P");
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		move(point, context, Math.cos(.11), Math.sin(.11));
		var old = address(point); String xml = getApp().getXML();
		var snapshot = getConstruction().getLastNativeTransactionMetrics(); AtomicInteger calls = new AtomicInteger();
		var failure = assertThrows(IllegalStateException.class,
				() -> LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
						Math.cos(1.91), Math.sin(1.91), policy, context, () -> {
							if (calls.incrementAndGet() == 2) { throw new IllegalStateException("R4_EXPIRED_REQUEST"); }
						}));
		assertEquals("R4_EXPIRED_REQUEST", failure.getMessage()); assertEquals(2, calls.get());
		assertEquals(old, address(point)); assertEquals(xml, getApp().getXML());
		assertSame(snapshot, getConstruction().getLastNativeTransactionMetrics());
	}

	@Test void sourceRevisionChangedAtPublicationRollsBackTheOnlyFinalMutation() {
		GeoNumeric radius = add("radius=1");
		add("c=Circle((0,0),radius)"); add("N=Point(c)");
		add("horizontal=Line(N,xAxis)"); add("vertical=Line(N,yAxis)"); add("Q=Intersect(horizontal,vertical)");
		GeoLocusV2 source = add("L=LocusV2(Q,N)"); GeoPoint point = offered(source, .1, "P");
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		move(point, context, Math.cos(.11), Math.sin(.11));
		var old = address(point); var id = getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point);
		String xml = getApp().getXML(); AtomicInteger calls = new AtomicInteger();
		assertThrows(IllegalStateException.class,
				() -> LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
						Math.cos(1.91), Math.sin(1.91), policy, context, () -> {
							if (calls.incrementAndGet() == 3) { radius.setValue(1.1); radius.updateCascade(); }
						}));
		assertTrue(calls.get() >= 3); assertEquals(xml, getApp().getXML());
		GeoPoint restored = (GeoPoint) getConstruction().getSpatialIdentityRegistry().getGeo(id);
		assertNotNull(restored); assertEquals(old, address(restored));
	}

	@Test void sourceRevisionChangingBetweenInternalChartsMakesTheCapturedGestureUnpublishable() {
		GeoNumeric radius = add("radius=1");
		add("c=Circle((0,0),radius)"); add("N=Point(c)");
		add("horizontal=Line(N,xAxis)"); add("vertical=Line(N,yAxis)"); add("Q=Intersect(horizontal,vertical)");
		GeoLocusV2 source = add("L=LocusV2(Q,N)"); GeoPoint point = offered(source, .1, "P");
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		move(point, context, Math.cos(.11), Math.sin(.11));
		var capturedDefinition = source.getSemanticDefinition(); var capturedAddress = address(point);
		var component = capturedDefinition.getBranch(BRANCH).getExistenceStructure()
				.getContinuousValidComponents().stream().filter(value ->
						value.contains(capturedAddress.getCanonicalParameter(), 0)).findFirst().orElseThrow();
		var snapshot = getConstruction().getLastNativeTransactionMetrics();
		AtomicInteger charges = new AtomicInteger(), charts = new AtomicInteger();
		AtomicReference<String> externalXml = new AtomicReference<>();
		AtomicReference<LocusSemanticAddress2D> externalAddress = new AtomicReference<>();
		var proposal = LocalContinuationEvidence2D.establish(capturedDefinition, capturedAddress,
				component, Math.cos(1.91), Math.sin(1.91), policy,
				(LocalContinuationEvidence2D.GestureState) null, new LocalContinuationEvidence2D.Work() {
					@Override public void charge() {
						assertTrue(charges.incrementAndGet() <= policy.getWorkBudget().getMaximumSubdivisions());
					}
					@Override public void certifiedChart() {
						if (charts.incrementAndGet() == 1) {
							radius.setValue(1.1); radius.updateCascade();
							externalXml.set(getApp().getXML()); externalAddress.set(address(point));
						}
					}
				});
		assertNotNull(proposal, "The pure old-snapshot proof can finish without publishing its result");
		assertTrue(charts.get() > 1); assertNotSame(capturedDefinition, source.getSemanticDefinition());
		assertNotNull(externalXml.get()); assertEquals(externalXml.get(), getApp().getXML());
		assertEquals(externalAddress.get(), address(point));
		var refused = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				Math.cos(1.91), Math.sin(1.91), policy, context);
		assertEquals(LocusPointContinuation2D.Status.INVALID_SOURCE, refused.getStatus());
		assertEquals(0, refused.getIntervalPasses()); assertEquals(0, refused.getDagPropagationNanos());
		assertEquals(externalXml.get(), getApp().getXML()); assertEquals(externalAddress.get(), address(point));
		assertSame(snapshot, getConstruction().getLastNativeTransactionMetrics());
	}

	@Test void chainDoesNotClampLineCircleTangencyOrBridgeItsMissingBand() {
		add("c=Circle((0,0),2)"); GeoPoint driver = add("N=Point(c)");
		driver.updatePathParameter(Math.PI / 2); driver.updateCascade();
		add("g=Line(N,yAxis)"); add("f=Circle((0,0),1)"); add("Q=Intersect(g,f,1)");
		GeoLocusV2 source = add("L=LocusV2(Q,N)"); GeoPoint point = offered(source, Math.PI / 2, "P");
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		var old = address(point); String xml = getApp().getXML();
		var result = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point, 1, 0, policy, context);
		assertFalse(result.isEstablished()); assertEquals(old, address(point)); assertEquals(xml, getApp().getXML());
	}

	@Test void completedChainReopensAndContinuesFromItsDurableAddress() {
		GeoLocusV2 source = meetCircle(); GeoPoint point = offered(source, .1, "P");
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		move(point, context, Math.cos(.11), Math.sin(.11)); move(point, context, Math.cos(1.91), Math.sin(1.91));
		var id = getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point); var saved = address(point);
		getApp().setXML(getApp().getXML(), true);
		GeoPoint reopened = (GeoPoint) lookup("P"); assertEquals(saved, address(reopened));
		assertEquals(id, getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(reopened));
		move(reopened, LocusV2PublicOperations.beginInteractiveSemanticPointDrag(reopened), Math.cos(.3), Math.sin(.3));
		assertEquals(.3, lifted(reopened), 3e-11);
	}
}
