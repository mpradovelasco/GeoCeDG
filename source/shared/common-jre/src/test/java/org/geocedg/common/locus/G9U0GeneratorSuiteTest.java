/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusCoverageCause2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Completeness;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Coverage;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.SemanticGeneratorFamily1D;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.util.InternalClipboard;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.Test;

/** Exact G22 typed-generator, nesting, cycle and lifecycle scenarios. */
class G9U0GeneratorSuiteTest extends G9U0PublicSurfaceTestBase {

	@Test
	void g01SliderVisibilityIsNotGeneratorIdentity() {
		GeoLocusV2 locus = createParabola();
		GeoNumeric driver = (GeoNumeric) requireLookup("s");
		String identity = locus.getLocusIdentity();
		driver.setEuclidianVisible(!driver.isEuclidianVisible());
		driver.updateVisualStyleRepaint(
				org.geogebra.common.kernel.geos.GProperty.VISIBLE);
		assertEquals(identity, locus.getLocusIdentity());
	}

	@Test
	void g02FreeScalarUsesItsExplicitFiniteDomain() {
		GeoLocusV2 locus = createLine();
		assertEquals(SemanticGeneratorFamily1D.SCALAR_STATE,
				((org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2)
						locus.getParentAlgorithm()).getGeneratorDescriptor()
						.getFamily());
		assertEquals(1, locus.getSemanticDefinition().getBranches().size());
	}

	@Test
	void g03DependentScalarMappingDoesNotMutateTrueCoordinate() {
		add("s=0.25");
		add("t=s^2");
		add("Q=(t,t+1)");
		add("D={false,{-1,1,true,true}}");
		GeoLocusV2 locus = add("L=LocusV2(Q,t,s,D)");
		// PRE-G9B-CU2 step T11, declared conformity: the scalar state t = s^2 is outside class
		// v3, so its total length is refused before any evaluation; evaluating points of the
		// locus, which its evaluator still does, exercises the evaluation transaction.
		locus.getInstrumentation().reset();
		try (LocusEvaluationSession2D session = new LocusEvaluationSession2D(false, 1)) {
			for (double parameter : new double[] {-0.5, 0, 0.75}) {
				locus.evaluate(BRANCH, parameter, session);
			}
		}
		assertTrue(locus.getInstrumentation().getEvaluatorCalls() > 0);
		assertEquals(0.25, ((GeoNumeric) requireLookup("s")).getDouble(), 0);
		assertEquals(0.0625, ((GeoNumeric) requireLookup("t")).getDouble(), 0);
	}

	@Test
	void g04NonInjectivePeriodicAndDiscontinuousMappingsKeepTrueCoordinate() {
		// PRE-G9B-CU2 declared conformity: a scalar state other than the true coordinate is
		// outside class v3 (the scalar program is the direct affine certificate only), so
		// since step T11 these loci are published NOT_ESTABLISHED and their semantic points,
		// which need a certified component, are undefined. The true coordinate is still the
		// coordinate input, and the declared domain still decides where nothing exists.
		add("s=0");
		add("t=s^2");
		add("Q=(t,0)");
		add("D={false,{-2,2,true,true}}");
		GeoLocusV2 nonInjective = add("L=LocusV2(Q,t,s,D)");
		assertNotEstablishedOutsideClass(nonInjective);
		assertFalse(add("Pn=Point(L,\"" + BRANCH + "\",-1)").isDefined());
		assertFalse(add("Pp=Point(L,\"" + BRANCH + "\",1)").isDefined());
		assertSame(requireLookup("s"), nonInjective.getParentAlgorithm()
				.getInput(2));

		add("sp=0");
		add("tp=sin(sp)");
		add("Qp=(tp,cos(sp))");
		add("Dp={true,{0,2*pi,true,false}}");
		GeoLocusV2 periodic = add("Lp=LocusV2(Qp,tp,sp,Dp)");
		assertTrue(periodic.getSemanticDefinition().getProvider().isPeriodic());
		assertNotEstablishedOutsideClass(periodic);
		assertFalse(add("Pp0=Point(Lp,\"" + BRANCH + "\",0)").isDefined());

		add("sd=1");
		add("td=1/sd");
		add("Qd=(td,sd)");
		add("Dd={false,{-2,-0.1,true,true},{0.1,2,true,true}}");
		GeoLocusV2 discontinuous = add("Ld=LocusV2(Qd,td,sd,Dd)");
		assertEquals(List.of(CoverageState.UNRESOLVED,
				CoverageState.CERTIFIED_DOES_NOT_EXIST, CoverageState.UNRESOLVED),
				discontinuous.getSemanticDefinition().getBranches().get(0)
						.getExistenceStructure().getCoverage().stream()
						.map(Coverage::getState).toList());
		assertFalse(add("Pd=Point(Ld,\"" + BRANCH + "\",0)").isDefined());
	}

	/**
	 * PRE-G9B-CU2 step T11: a locus outside class v3 is published, not established, without
	 * a certified component; it never falls back to a compatibility certificate.
	 */
	private static void assertNotEstablishedOutsideClass(GeoLocusV2 locus) {
		assertTrue(locus.isDefined());
		LocusExistenceStructure2D structure = locus.getSemanticDefinition().getBranches()
				.get(0).getExistenceStructure();
		assertEquals(Completeness.NOT_ESTABLISHED, structure.getCompleteness());
		assertTrue(structure.getContinuousValidComponents().isEmpty());
		assertEquals(LocusCoverageCause2D.OPERATION_OUTSIDE_CLASS,
				structure.getCoverage().get(0).getCause());
	}

	@Test
	void g05PointOnFiniteSegmentIsAnApprovedProvider() {
		GeoLocusV2 locus = pointSupportLocus("g=Segment((0,0),(2,0))");
		assertTrue(locus.isDefined());
		assertEquals(SemanticGeneratorFamily1D.SEGMENT_POINT,
				descriptor(locus).getFamily());
	}

	@Test
	void g06PointOnOrientedCircleIsAnApprovedProvider() {
		GeoLocusV2 locus = pointSupportLocus("g=Circle((0,0),2)");
		assertTrue(locus.isDefined());
		assertEquals(SemanticGeneratorFamily1D.CIRCLE_POINT,
				descriptor(locus).getFamily());
	}

	@Test
	void g07PointOnOrientedCircularArcIsAnApprovedProvider() {
		GeoLocusV2 locus = pointSupportLocus(
				"g=CircumcircularArc((1,0),(0,1),(-1,0))");
		assertTrue(locus.isDefined());
		assertEquals(SemanticGeneratorFamily1D.CIRCULAR_ARC_POINT,
				descriptor(locus).getFamily());
	}

	@Test
	void g08PointOnExplicitV2BranchCanDriveAnotherLocus() {
		GeoLocusV2 inner = createLine();
		GeoPoint state = semanticPoint(inner, 0.5);
		add("Q2=(x(P),x(P)^2)");
		GeoLocusV2 outer = add("L2=LocusV2(Q2,P)");
		assertNotNull(outer);
		assertSame(state, outer.getParentAlgorithm().getInput(1));
	}

	@Test
	void g09LocusPointLocusNestingUsesNormalParents() {
		GeoLocusV2 inner = createLine();
		GeoPoint point = semanticPoint(inner, 0.25);
		add("Q2=(x(P),2*x(P))");
		GeoLocusV2 outer = add("L2=LocusV2(Q2,P)");
		assertSame(inner, point.getParentAlgorithm().getInput(0));
		assertSame(point, outer.getParentAlgorithm().getInput(1));
	}

	@Test
	void g10AcyclicNestingDepthGreaterThanOneIsReconstructible() {
		GeoLocusV2 first = createDisconnectedLine();
		semanticPoint(first, -1.5);
		add("Q2=(x(P),x(P)+1)");
		GeoLocusV2 second = add("L2=LocusV2(Q2,P)");
		add("P2=Point(L2,\"" + BRANCH + "\",1.3)");
		add("Q3=(x(P2),x(P2)+2)");
		GeoLocusV2 third = add("L3=LocusV2(Q3,P2)");
		assertTrue(first.isDefined() && second.isDefined() && third.isDefined());
		assertEquals(2, first.getSemanticDefinition().getBranches().get(0)
				.getValidDomainComponents().size());
		List<PersistentGeoId> identities = List.of(id(first), id(second), id(third));
		reload();
		GeoLocusV2 reopenedFirst = (GeoLocusV2) requireLookup("L");
		GeoLocusV2 reopenedSecond = (GeoLocusV2) requireLookup("L2");
		GeoLocusV2 reopenedThird = (GeoLocusV2) requireLookup("L3");
		assertEquals(identities, List.of(id(reopenedFirst), id(reopenedSecond),
				id(reopenedThird)));
		assertSame(requireLookup("P"), reopenedSecond.getParentAlgorithm()
				.getInput(1));
		assertSame(requireLookup("P2"), reopenedThird.getParentAlgorithm()
				.getInput(1));
		assertTrue(reopenedFirst.isDefined() && reopenedSecond.isDefined()
				&& reopenedThird.isDefined());
	}

	@Test
	void g11SelfIntersectionKeepsDistinctSemanticPreimages() {
		// PRE-G9B-CU2 declared conformity: (sin(s),sin(2*s)) is outside class v3 since step
		// T11; the certified projection maps the preimages pi/3 and -pi/3 to one position.
		GeoLocusV2 locus = projectedCircle();
		GeoPoint first = semanticPoint(locus, Math.PI / 3);
		GeoPoint second = add("P2=Point(L,\"" + BRANCH + "\",-pi/3)");
		AlgoSemanticLocusPoint2D firstParent =
				(AlgoSemanticLocusPoint2D) first.getParentAlgorithm();
		AlgoSemanticLocusPoint2D secondParent =
				(AlgoSemanticLocusPoint2D) second.getParentAlgorithm();
		assertEquals(first.getInhomX(), second.getInhomX(), 1E-14);
		assertNotEquals(firstParent.getSemanticAddress().getCanonicalParameter(),
				secondParent.getSemanticAddress().getCanonicalParameter());
	}

	@Test
	void g12DisconnectedComponentsRemainExplicit() {
		GeoLocusV2 locus = createDisconnectedLine();
		assertEquals(2, locus.getSemanticDefinition().getBranches().get(0)
				.getValidDomainComponents().size());
		assertFalse(semanticPoint(locus, 0).isDefined());
	}

	@Test
	void g13DynamicComponentLossWithdrawsAndExactRestoreRebinds() {
		GeoLocusV2 locus = createDisconnectedLine();
		GeoPoint point = semanticPoint(locus, -1.5);
		add("Q2=(x(P),x(P)+1)");
		add("L2=LocusV2(Q2,P)");
		LocusSemanticAddress2D original = address(point);
		assertTrue(point.isDefined());
		editGeoElement(requireLookup("D"), "D={false,{1,2,true,true}}");
		assertFalse(((GeoPoint) requireLookup("P")).isDefined());
		// PRE-G9B-CU2 declared conformity (H01): the position of the driver never decides
		// publication, so the locus on the support stays published while its driver point
		// is withdrawn.
		assertTrue(((GeoLocusV2) requireLookup("L2")).isDefined());
		editGeoElement(requireLookup("D"),
				"D={false,{-2,-1,true,true},{1,2,true,true}}");
		GeoPoint restored = (GeoPoint) requireLookup("P");
		assertTrue(restored.isDefined());
		assertEquals(original, address(restored));
		assertTrue(((GeoLocusV2) requireLookup("L2")).isDefined());
	}

	@Test
	void g14PeriodicSeamTraversalCarriesLiftEvidence() {
		GeoLocusV2 locus = createPeriodicCircle();
		GeoPoint first = semanticPoint(locus, 0);
		GeoPoint second = add("P2=Point(L,\"" + BRANCH + "\",2*pi)");
		LocusSemanticAddress2D a = ((AlgoSemanticLocusPoint2D)
				first.getParentAlgorithm()).getSemanticAddress();
		LocusSemanticAddress2D b = ((AlgoSemanticLocusPoint2D)
				second.getParentAlgorithm()).getSemanticAddress();
		assertEquals(first.getInhomX(), second.getInhomX(), 1E-14);
		assertNotEquals(a.getPeriodicLift(), b.getPeriodicLift());
		// PRE-G9B-CU2 step T11: the seam of the certified circle is at -pi; the two sides of
		// it are the same point with distinct lifts.
		GeoPoint seamStart = add("P3=Point(L,\"" + BRANCH + "\",-pi)");
		GeoPoint seamEnd = add("P4=Point(L,\"" + BRANCH + "\",pi)");
		assertEquals(seamStart.getInhomX(), seamEnd.getInhomX(), 1E-14);
		assertEquals(seamStart.getInhomY(), seamEnd.getInhomY(), 1E-14);
		assertNotEquals(((AlgoSemanticLocusPoint2D) seamStart.getParentAlgorithm())
				.getSemanticAddress().getPeriodicLift(), ((AlgoSemanticLocusPoint2D) seamEnd
						.getParentAlgorithm()).getSemanticAddress().getPeriodicLift());
	}

	@Test
	void g15PublicSplitAndMergeNeverRetargetBoundPoints() {
		GeoLocusV2 splitSource = createLine();
		GeoPoint splitPoint = semanticPoint(splitSource, 1.5);
		LocusSemanticAddress2D wholeAddress = address(splitPoint);
		editGeoElement(requireLookup("D"),
				"D={false,{-2,-0.1,true,true},{0.1,2,true,true}}");
		assertFalse(((GeoPoint) requireLookup("P")).isDefined());
		assertEquals(wholeAddress, address((GeoPoint) requireLookup("P")));
		editGeoElement(requireLookup("D"), "D={false,{-2,2,true,true}}");
		assertTrue(((GeoPoint) requireLookup("P")).isDefined());
		assertEquals(wholeAddress, address((GeoPoint) requireLookup("P")));

		add("u=0");
		add("U=(u,1)");
		add("Du={false,{-2,-0.1,true,true},{0.1,2,true,true}}");
		add("Lu=LocusV2(U,u,Du)");
		GeoPoint mergePoint = add("Pu=Point(Lu,\"" + BRANCH + "\",1.5)");
		LocusSemanticAddress2D componentAddress = address(mergePoint);
		editGeoElement(requireLookup("Du"), "Du={false,{-2,2,true,true}}");
		assertFalse(((GeoPoint) requireLookup("Pu")).isDefined());
		assertEquals(componentAddress,
				address((GeoPoint) requireLookup("Pu")));
	}

	@Test
	void g16DirectCycleRedefinitionIsRejected() {
		GeoLocusV2 locus = createLine();
		semanticPoint(locus, 0.25);
		assertCycleRedefinitionRejected(locus, "LocusV2(P,s,D)",
				List.of("L", "P", "Q", "s", "D"));
	}

	@Test
	void g17IndirectCycleRedefinitionIsRejected() {
		GeoLocusV2 first = createLine();
		semanticPoint(first, 0.2);
		add("Q2=(x(P),x(P)+1)");
		GeoLocusV2 second = add("L2=LocusV2(Q2,P)");
		GeoPoint nested = add("P2=Point(L2,\"" + BRANCH + "\",0.3)");
		// PRE-G9B-CU2 declared conformity: a locus on a locus support is outside class v3
		// since step T11, so the point on it has no certified component and is undefined
		// before the rejected redefinition as after it.
		assertFalse(nested.isDefined());
		assertCycleRedefinitionRejected(first, "LocusV2(P2,s,D)",
				List.of("L", "P", "Q", "s", "D", "L2", "Q2"));
		assertFalse(requireLookup("P2").isDefined());
		assertTrue(((GeoLocusV2) requireLookup("L2")).isDefined());
	}

	@Test
	void g18GeneratorSaveAndReopenRestoresNativeType() {
		GeoLocusV2 locus = createParabola();
		String identity = locus.getLocusIdentity();
		reload();
		GeoLocusV2 reopened = (GeoLocusV2) requireLookup("L");
		assertEquals(identity, reopened.getLocusIdentity());
		assertTrue(reopened.isDefined());
	}

	@Test
	void g19RealClipboardCopyWithinAndAcrossConstructionsUsesFreshIds() {
		// PRE-G9B-CU2 step T11, declared conformity: the point on the scalar parabola, outside
		// class v3, was undefined; the certified line keeps the copied points defined.
		GeoLocusV2 locus = createLine();
		GeoPoint point = semanticPoint(locus, 0.5);
		assertTrue(point.isDefined());
		Set<PersistentGeoId> sourceIds = identityIds(getConstruction()
				.getSpatialIdentityRegistry().getRecords());
		String clipboard = InternalClipboard.getTextToSave(getApp(),
				List.of(point), text -> text);
		paste(getApp(), clipboard);
		List<GeoIdentityRecord> localCopies = copyRecords(getApp());
		assertFalse(localCopies.isEmpty());
		assertTrue(localCopies.stream().allMatch(record ->
				sourceIds.contains(record.getCopySourceId())
						&& !sourceIds.contains(record.getId())));
		assertTrue(localCopies.stream().map(record -> getConstruction()
				.getSpatialIdentityRegistry().getGeo(record.getId()))
				.anyMatch(GeoLocusV2.class::isInstance));
		assertTrue(localCopies.stream().map(record -> getConstruction()
				.getSpatialIdentityRegistry().getGeo(record.getId()))
				.anyMatch(geo -> geo instanceof GeoPoint && geo.isDefined()));

		AppCommon target = AppCommonFactory.create(new AppConfigGeoCeDG(true));
		paste(target, clipboard);
		List<GeoIdentityRecord> targetCopies = copyRecords(target);
		assertFalse(targetCopies.isEmpty());
		assertTrue(targetCopies.stream().allMatch(record ->
				sourceIds.contains(record.getCopySourceId())
						&& !sourceIds.contains(record.getId())));
		assertTrue(target.getKernel().getConstruction()
				.getGeoSetConstructionOrder().stream()
				.anyMatch(GeoLocusV2.class::isInstance));
		assertTrue(target.getKernel().getConstruction()
				.getGeoSetConstructionOrder().stream()
				.anyMatch(geo -> geo.getParentAlgorithm() instanceof AlgoSemanticLocusPoint2D
						&& geo.isDefined()));
	}

	@Test
	void g20UndoRedoRestoresTheSameOperationIdentityGraph() {
		activateUndo();
		getApp().storeUndoInfo();
		// PRE-G9B-CU2 step T11, declared conformity: the point on the scalar parabola, outside
		// class v3, was undefined; on the certified line it is defined.
		GeoLocusV2 locus = createLine();
		assertTrue(semanticPoint(locus, 0.5).isDefined());
		getApp().storeUndoInfo();
		final String createdXml = getApp().getXML();
		Set<PersistentGeoId> createdIds = identityIds(getConstruction()
				.getSpatialIdentityRegistry().getRecords());
		((GeoNumeric) requireLookup("s")).setValue(0.75);
		requireLookup("s").updateCascade();
		getApp().storeUndoInfo();
		String changedXml = getApp().getXML();
		assertEquals(createdIds, identityIds(getConstruction()
				.getSpatialIdentityRegistry().getRecords()));
		((GeoLocusV2) requireLookup("L")).remove();
		getApp().storeUndoInfo();
		assertNull(lookup("L"));

		getKernel().undo();
		assertEquals(changedXml, getApp().getXML());
		assertEquals(createdIds, identityIds(getConstruction()
				.getSpatialIdentityRegistry().getRecords()));
		getKernel().undo();
		assertEquals(createdXml, getApp().getXML());
		assertEquals(createdIds, identityIds(getConstruction()
				.getSpatialIdentityRegistry().getRecords()));
		getKernel().redo();
		assertEquals(changedXml, getApp().getXML());
		getKernel().redo();
		assertNull(lookup("L"));
	}

	@Test
	void g21NestedQueryCountersAreDeterministic() {
		// PRE-G9B-CU1 conformance (locus-v2-semantics.md v1.2, decision D-CU1-8):
		// an update without a semantic change keeps the revision and the metric
		// component state, so it repeats no evaluator work; identical semantic
		// changes repeat identical, positive work.
		// PRE-G9B-CU2 declared conformity: (s,k*s^2) is outside class v3 since step T11; a
		// certified circle whose radius is the non-driver input k has an estimated total
		// length that evaluates the locus.
		add("k=1");
		add("ck=Circle((0,0),k)");
		add("Sk=Point(ck)");
		add("Q=Sk+(0,0)");
		GeoLocusV2 locus = add("L=LocusV2(Q,Sk)");
		var metric = totalMetric(locus);
		GeoNumeric coefficient = (GeoNumeric) requireLookup("k");
		locus.getInstrumentation().reset();
		locus.getParentAlgorithm().update();
		metric.getParentAlgorithm().update();
		assertEquals(0, locus.getInstrumentation().getEvaluatorCalls());
		locus.getInstrumentation().reset();
		coefficient.setValue(2);
		coefficient.updateCascade();
		long first = locus.getInstrumentation().getEvaluatorCalls();
		coefficient.setValue(1);
		coefficient.updateCascade();
		locus.getInstrumentation().reset();
		coefficient.setValue(2);
		coefficient.updateCascade();
		long second = locus.getInstrumentation().getEvaluatorCalls();
		assertTrue(first > 0);
		assertEquals(first, second);
	}

	@Test
	void g22GeneratorHasZeroRenderSampleOrViewportAuthority() {
		GeoLocusV2 locus = createPeriodicCircle();
		locus.getInstrumentation().reset();
		semanticPoint(locus, 0.5);
		assertEquals(0, locus.getInstrumentation().getRenderEvaluations());
		assertEquals(0, locus.getInstrumentation().getWholeLocusRegenerations());
	}

	private GeoLocusV2 pointSupportLocus(String supportCommand) {
		add(supportCommand);
		add("S=Point(g)");
		add("Q=(x(S),y(S)+x(S)^2)");
		return add("L=LocusV2(Q,S)");
	}

	private org.geocedg.common.kernel.locus.SemanticGeneratorDescriptor1D
			descriptor(GeoLocusV2 locus) {
		return ((org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2)
				locus.getParentAlgorithm()).getGeneratorDescriptor();
	}

	private LocusSemanticAddress2D address(GeoPoint point) {
		return ((AlgoSemanticLocusPoint2D) point.getParentAlgorithm())
				.getSemanticAddress();
	}

	private PersistentGeoId id(GeoElement geo) {
		PersistentGeoId id = getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(geo);
		assertNotNull(id);
		return id;
	}

	private void assertCycleRedefinitionRejected(GeoLocusV2 target,
			String definition, List<String> labels) {
		String beforeXml = getApp().getXML();
		int beforeSteps = getConstruction().steps();
		int beforeGeos = getConstruction().getGeoSetConstructionOrder().size();
		int beforeRecords = getConstruction().getSpatialIdentityRegistry()
				.getRecords().size();
		final int beforeReservations = getConstruction().getSpatialIdentityRegistry()
				.getReservedIdentityCount();
		final PersistentGeoId identity = id(target);
		final List<String> inputs = Arrays.stream(
				target.getParentAlgorithm().getInput())
				.map(GeoElement::getLabelSimple).collect(Collectors.toList());
		ErrorAccumulator errors = new ErrorAccumulator();
		setErrorHandler(errors);
		try {
			editGeoElement(target, definition);
		} finally {
			resetErrorHandler();
		}
		assertFalse(errors.getErrors().isEmpty());
		assertEquals(beforeXml, getApp().getXML());
		assertEquals(beforeSteps, getConstruction().steps());
		assertEquals(beforeGeos,
				getConstruction().getGeoSetConstructionOrder().size());
		assertEquals(beforeRecords, getConstruction()
				.getSpatialIdentityRegistry().getRecords().size());
		assertEquals(beforeReservations, getConstruction()
				.getSpatialIdentityRegistry().getReservedIdentityCount());
		GeoLocusV2 restored = (GeoLocusV2) requireLookup(target.getLabelSimple());
		assertEquals(identity, id(restored));
		assertEquals(inputs, Arrays.stream(restored.getParentAlgorithm().getInput())
				.map(GeoElement::getLabelSimple).collect(Collectors.toList()));
		for (String label : labels) {
			assertTrue(requireLookup(label).isDefined(), label + " became stale");
		}
	}

	private static Set<PersistentGeoId> identityIds(
			java.util.Collection<?> records) {
		return records.stream().filter(GeoIdentityRecord.class::isInstance)
				.map(GeoIdentityRecord.class::cast).map(GeoIdentityRecord::getId)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	private static List<GeoIdentityRecord> copyRecords(AppCommon app) {
		return app.getKernel().getConstruction().getSpatialIdentityRegistry()
				.getRecords().stream().filter(GeoIdentityRecord.class::isInstance)
				.map(GeoIdentityRecord.class::cast)
				.filter(record -> record.getCopySourceId() != null)
				.collect(Collectors.toList());
	}

	private static void paste(AppCommon app, String clipboard) {
		int separator = clipboard.indexOf('\n');
		List<String> labels = new ArrayList<>(Arrays.asList(
				clipboard.substring(0, separator).split(" ")));
		InternalClipboard.pasteGeoGebraXMLInternal(app, labels,
				clipboard.substring(separator));
	}
}
