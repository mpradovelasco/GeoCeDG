/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicBoolean;

import org.geocedg.common.kernel.locus.NativeSourceFidelity2D;
import org.geocedg.common.kernel.locus.NativeSourceFidelity2D.Exhaustion;
import org.geocedg.common.kernel.locus.NativeSourceFidelity2D.Limits;
import org.geocedg.common.kernel.locus.NativeSourceFidelity2D.Outcome;
import org.geocedg.common.kernel.locus.NativeTransactionalState2D;
import org.geocedg.common.kernel.locus.NativeTransactionalState2D.UnsupportedReason;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.interaction.IndividualForwardPointCreation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointContinuation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geogebra.common.kernel.algos.AlgoPointOnPath;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.ExpressionValue;
import org.geogebra.common.kernel.arithmetic.MyDouble;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.plugin.Operation;
import org.junit.jupiter.api.Test;

/** R4 P2-D: memo reuse, actual exploration and resource failures have distinct units. */
class R4NativeFidelityBudgetTest extends G9U0PublicSurfaceTestBase {

	@Test void injectedWorkPoliciesCanOnlyReduceTheHardLimits() {
		assertThrows(IllegalArgumentException.class, () -> new Limits(0, 4, 4));
		assertThrows(IllegalArgumentException.class, () -> new Limits(4097, 4, 4));
		assertThrows(IllegalArgumentException.class, () -> new Limits(4, 65537, 4));
		assertThrows(IllegalArgumentException.class, () -> new Limits(4, 4, 257));
		assertThrows(IllegalArgumentException.class, () -> new Limits(4, 4, -1));
		assertTrue(NativeSourceFidelity2D.fileSerializationPass(new Limits(1, 1, 0))
				.isFaithful(getConstruction().getXAxis()));
	}

	@Test void fourThousandOneHundredAxisQueriesKeepTheOneNodeWitness() {
		var pass = NativeSourceFidelity2D.fileSerializationPass();
		long started = System.nanoTime();
		for (int query = 0; query < 4100; query++) {
			assertEquals(Outcome.SUPPORTED, pass.assess(getConstruction().getXAxis()).outcome(),
					"Unchanged axis at query " + (query + 1));
		}
		var metrics = pass.metrics();
		long assessNanos = System.nanoTime() - started;
		assertEquals(4100, metrics.queries()); assertEquals(4099, metrics.memoHits());
		assertEquals(1, metrics.geoNodes()); assertEquals(0, metrics.expressionNodes());
		assertEquals(1, metrics.explorations());
		assertEquals(0, metrics.edges()); assertEquals(Exhaustion.NONE, metrics.exhaustion());
		System.out.println("R4_NATIVE_MEMO queries=" + metrics.queries() + " hits=" + metrics.memoHits()
				+ " geoNodes=" + metrics.geoNodes() + " expressionNodes=" + metrics.expressionNodes()
				+ " edges=" + metrics.edges() + " explorations=" + metrics.explorations()
				+ " exhaustion=" + metrics.exhaustion() + " limits=" + metrics.limits()
				+ " assessNanos=" + assessNanos);
	}

	@Test void fourThousandOneHundredNativePlacesUseOneSourceNode() {
		axisPoints(4100);
		long started = System.nanoTime();
		var assessment = NativeTransactionalState2D.assessHost(getConstruction());
		long assessNanos = System.nanoTime() - started;
		assertNull(assessment.reason()); assertTrue(assessment.hostNodes() >= 4100);
		assertEquals(4100, assessment.sourceMetrics().queries());
		assertEquals(1, assessment.sourceMetrics().processedNodes());
		assertEquals(4099, assessment.sourceMetrics().memoHits());
		assertEquals(Exhaustion.NONE, assessment.sourceMetrics().exhaustion());
		started = System.nanoTime();
		var captured = NativeTransactionalState2D.capture(getConstruction());
		long captureNanos = System.nanoTime() - started;
		assertTrue(captured.isSupported()); assertEquals(4100, captured.metrics().capturedPoints);
		assertEquals(1, captured.metrics().sourceMetrics.processedNodes());
		assertFalse(getApp().getXML().contains(NativeTransactionalState2D.BINDING_TAG));
		var metrics = assessment.sourceMetrics();
		System.out.println("R4_NATIVE_HOST queries=" + metrics.queries() + " hits=" + metrics.memoHits()
				+ " geoNodes=" + metrics.geoNodes() + " expressionNodes=" + metrics.expressionNodes()
				+ " edges=" + metrics.edges() + " explorations=" + metrics.explorations()
				+ " hostNodes=" + assessment.hostNodes() + " capturedPoints=" + captured.metrics().capturedPoints
				+ " exhaustion=" + metrics.exhaustion() + " limits=" + metrics.limits()
				+ " assessNanos=" + assessNanos + " captureNanos=" + captureNanos);
	}

	@Test void intrinsicUnsupportedMemoDoesNotBecomeAResourceFailure() {
		var unsupported = new GeoBoolean(getConstruction());
		var pass = NativeSourceFidelity2D.fileSerializationPass(new Limits(1, 4, 4));
		for (int query = 0; query < 4100; query++) {
			assertEquals(Outcome.UNSUPPORTED, pass.assess(unsupported).outcome());
		}
		assertEquals(1, pass.metrics().processedNodes());
		assertEquals(Exhaustion.NONE, pass.metrics().exhaustion());
		assertEquals(Outcome.RESOURCE_EXHAUSTED,
				pass.assess(getConstruction().getXAxis()).outcome());
		assertEquals(Outcome.UNSUPPORTED, pass.assess(unsupported).outcome());
		assertTrue(NativeSourceFidelity2D.isFaithfulSource(getConstruction().getXAxis()));
	}

	@Test void newNodeBudgetIsEffectiveAndANewPassCanProveTheSameSource() {
		GeoList list = new GeoList(getConstruction());
		for (int index = 0; index < 4; index++) { list.add(new GeoNumeric(getConstruction(), index)); }
		var pass = NativeSourceFidelity2D.fileSerializationPass(new Limits(2, 20, 20));
		assertEquals(Outcome.RESOURCE_EXHAUSTED, pass.assess(list).outcome());
		assertEquals(2, pass.metrics().processedNodes());
		assertEquals(Exhaustion.NODES, pass.metrics().exhaustion());
		assertEquals(Outcome.RESOURCE_EXHAUSTED, pass.assess(list).outcome());
		assertTrue(NativeSourceFidelity2D.fileSerializationPass(new Limits(5, 20, 20)).isFaithful(list));
	}

	@Test void examinedEdgesBoundAListOfRepeatedMemoDestinations() {
		GeoList list = new GeoList(getConstruction());
		for (int index = 0; index < 12; index++) { list.add(getConstruction().getXAxis()); }
		var pass = NativeSourceFidelity2D.fileSerializationPass(new Limits(2, 4, 20));
		assertEquals(Outcome.RESOURCE_EXHAUSTED, pass.assess(list).outcome());
		assertEquals(Exhaustion.EDGES, pass.metrics().exhaustion());
		assertEquals(4, pass.metrics().edges()); assertEquals(2, pass.metrics().processedNodes());
		assertEquals(3, pass.metrics().memoHits());
		assertEquals(Outcome.SUPPORTED, pass.assess(getConstruction().getXAxis()).outcome());
		assertEquals(4, pass.metrics().edges()); assertEquals(2, pass.metrics().processedNodes());
		assertTrue(NativeSourceFidelity2D.fileSerializationPass(new Limits(2, 12, 20)).isFaithful(list));
	}

	@Test void newExplorationDepthHasItsOwnResourceDiagnostic() {
		GeoList root = new GeoList(getConstruction()); GeoList child = root;
		for (int depth = 0; depth < 6; depth++) {
			GeoList next = new GeoList(getConstruction()); child.add(next); child = next;
		}
		child.add(getConstruction().getXAxis());
		var pass = NativeSourceFidelity2D.fileSerializationPass(new Limits(20, 20, 2));
		assertEquals(Outcome.RESOURCE_EXHAUSTED, pass.assess(root).outcome());
		assertEquals(Exhaustion.DEPTH, pass.metrics().exhaustion());
		assertEquals(2, pass.metrics().maximumDepth());
		assertTrue(NativeSourceFidelity2D.fileSerializationPass().isFaithful(root));
	}

	@Test void expressionNodesAreChargedAndExhaustionIsNotMemoizedAsUnsupported() {
		GeoNumeric number = new GeoNumeric(getConstruction(), 1);
		number.setDefinition((ExpressionNode) sum(8));
		var pass = NativeSourceFidelity2D.fileSerializationPass(new Limits(3, 40, 20));
		assertEquals(Outcome.RESOURCE_EXHAUSTED, pass.assess(number).outcome());
		assertEquals(3, pass.metrics().processedNodes()); assertTrue(pass.metrics().expressionNodes() > 0);
		assertEquals(Exhaustion.NODES, pass.metrics().exhaustion());
		assertTrue(NativeSourceFidelity2D.fileSerializationPass().isFaithful(number));
	}

	@Test void fileSerializationReportsSharedSourceWorkWithoutRevokingItsFields() {
		add("c=Circle((0,0),1)");
		for (int index = 0; index < 12; index++) { add("P" + index + "=Point(c)"); }
		String xml = getApp().getXML();
		assertEquals(12, xml.split("<conicPointState", -1).length - 1);
		var metrics = getConstruction().getLastNativeFileFidelityMetrics();
		assertNotNull(metrics); assertEquals(12, metrics.queries());
		assertTrue(metrics.memoHits() >= 11); assertTrue(metrics.processedNodes() < metrics.queries());
		assertEquals(Exhaustion.NONE, metrics.exhaustion());
	}

	@Test void memoryRollbackAndFreshUndoSessionsRetainTheSamePositiveBudgetEvidence() throws Exception {
		axisPoints(8);
		String originalXml = getApp().getXML();
		var original = ((GeoPoint) lookup("P0")).captureNativeRollbackState();
		var failure = new IllegalStateException("forced budget regression rollback");
		assertSame(failure, assertThrows(IllegalStateException.class,
				() -> getConstruction().runAtomicConstructionMutation(() -> {
					GeoPoint point = (GeoPoint) lookup("P0"); point.setCoords(9, 0, 1);
					point.updateCascade(); throw failure;
				})));
		assertTrue(original.matches((GeoPoint) lookup("P0"))); assertEquals(originalXml, getApp().getXML());
		var metrics = getConstruction().getLastNativeTransactionMetrics();
		assertEquals(1, metrics.sourceMetrics.processedNodes()); assertEquals(8, metrics.capturedPoints);
		var snapshot = getConstruction().captureNativeUndoSnapshot(); assertNotNull(snapshot);
		for (int cycle = 0; cycle < 3; cycle++) {
			GeoPoint point = (GeoPoint) lookup("P0"); point.setCoords(7 + cycle, 0, 1); point.updateCascade();
			getConstruction().restoreNativeUndoSnapshot(snapshot.template(), () -> {
				try { getApp().getXMLio().processXMLString(snapshot.xml().toString(), true, false); }
				catch (Exception exception) { throw new IllegalStateException(exception); }
			});
			assertTrue(original.matches((GeoPoint) lookup("P0")));
			assertEquals(originalXml, getApp().getXML());
			assertEquals(1, getConstruction().getLastNativeUndoRestoreMetrics().sourceMetrics.processedNodes());
		}
	}

	@Test void actualDefaultExhaustionRefusesAtomicAndUndoCaptureBeforeMutation() {
		GeoNumeric radius = add("r=1"); add("c=Circle((0,0),r)"); add("P=Point(c)");
		radius.setDefinition((ExpressionNode) sum(4096));
		String xml = getApp().getXML();
		assertEquals(Exhaustion.NODES, getConstruction().getLastNativeFileFidelityMetrics().exhaustion());
		assertFalse(xml.contains("<conicPointState"));
		assertEquals(UnsupportedReason.SOURCE_FIDELITY_RESOURCE_EXHAUSTED,
				NativeTransactionalState2D.unsupportedReason(getConstruction()));
		AtomicBoolean mutated = new AtomicBoolean();
		var rejected = assertThrows(IllegalStateException.class,
				() -> getConstruction().runAtomicConstructionMutation(() -> mutated.set(true)));
		assertEquals(UnsupportedReason.SOURCE_FIDELITY_RESOURCE_EXHAUSTED.name(), rejected.getMessage());
		assertEquals(0, getConstruction().getLastNativeTransactionMetrics().xmlCaptureNanos);
		assertEquals(0, getConstruction().getLastNativeTransactionMetrics().capturedPoints);
		assertFalse(mutated.get()); assertEquals(xml, getApp().getXML());
		assertThrows(IllegalStateException.class, () -> getConstruction().captureNativeUndoSnapshot());
		assertThrows(IllegalStateException.class,
				() -> getConstruction().runNativeUndoOperation(() -> mutated.set(true)));
		assertFalse(mutated.get()); assertEquals(xml, getApp().getXML());
	}

	@Test void aRealUnsupportedSourceKeepsItsDistinctHostReason() {
		add("r=Distance((0,0),(1,1))"); add("c=Circle((0,0),r)"); add("P=Point(c)");
		var assessment = NativeTransactionalState2D.assessHost(getConstruction());
		assertEquals(UnsupportedReason.UNSUPPORTED_SOURCE_CLOSURE, assessment.reason());
		assertEquals(Exhaustion.NONE, assessment.sourceMetrics().exhaustion());
	}

	@Test void creationAndMovementRetainTheirHistoricalStatusAndExposeTheResourceSubreason() {
		add("driver=Circle((0,0),1)"); add("N=Point(driver)"); add("v=Vector((0,0),N)");
		add("Traced=N+2v"); GeoLocusV2 locus = add("L=LocusV2(Traced,N)");
		var policy = LocusPointInteractionPolicy2D.initial(.1);
		GeoPoint moving;
		try (var choice = IndividualForwardPointCreation2D.query(locus, 3, 0, policy)) {
			assertEquals(IndividualForwardPointCreation2D.Permission.EXPLICIT_FORWARD_SELECTION_REQUIRED,
					choice.getPermission());
			moving = choice.commit("Moving", choice.getChoices().get(0));
		}
		GeoNumeric radius = add("r=1"); add("other=Circle((0,0),r)"); add("OtherP=Point(other)");
		radius.setDefinition((ExpressionNode) sum(4096));
		String xml = getApp().getXML();
		try (var choice = IndividualForwardPointCreation2D.query(locus, 3, 0, policy)) {
			var rejected = assertThrows(IndividualForwardPointCreation2D.Rejected.class,
					() -> choice.commit("Unpublished", choice.getChoices().get(0)));
			assertEquals(IndividualForwardPointCreation2D.Failure.UNSUPPORTED_HOST_STATE, rejected.getFailure());
			assertEquals(UnsupportedReason.SOURCE_FIDELITY_RESOURCE_EXHAUSTED, rejected.getNativeHostReason());
			assertEquals(UnsupportedReason.SOURCE_FIDELITY_RESOURCE_EXHAUSTED.name(), rejected.getDiagnostic());
		}
		var context = LocusPointContinuation2D.begin(moving);
		var result = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(moving,
				3 * Math.cos(.05), 3 * Math.sin(.05), policy, context);
		assertEquals(LocusPointContinuation2D.Status.UNSUPPORTED_HOST_STATE, result.getStatus());
		assertEquals(UnsupportedReason.SOURCE_FIDELITY_RESOURCE_EXHAUSTED, result.getNativeHostReason());
		assertEquals(UnsupportedReason.SOURCE_FIDELITY_RESOURCE_EXHAUSTED.name(), result.getDiagnostic());
		assertTrue(context.isStopped()); assertNull(lookup("Unpublished"));
		assertEquals(xml, getApp().getXML());
	}

	private void axisPoints(int count) {
		for (int index = 0; index < count; index++) {
			GeoPoint point = (GeoPoint) new AlgoPointOnPath(getConstruction(),
					getConstruction().getXAxis(), 1 + index * .01, 0).getP();
			point.setLabel("P" + index); assertTrue(point.canCaptureNativeRollbackState());
		}
	}

	private ExpressionValue sum(int leaves) {
		return sum(leaves, 1.0 / leaves);
	}

	private ExpressionValue sum(int leaves, double value) {
		if (leaves == 1) { return new MyDouble(getKernel(), value); }
		return new ExpressionNode(getKernel(), sum(leaves / 2, value), Operation.PLUS,
				sum(leaves - leaves / 2, value));
	}
}
