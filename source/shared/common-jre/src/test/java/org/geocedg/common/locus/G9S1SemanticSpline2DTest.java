/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geocedg.common.kernel.algos.AlgoLocusBetweenMetricV2;
import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionWorkBudget2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricCoverage;
import org.geocedg.common.kernel.locus.metric.MetricValueKind;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoFunction;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoCurveCartesianND;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.util.InternalClipboard;
import org.geogebra.test.TestErrorHandler;
import org.junit.jupiter.api.Test;

/**
 * Public/kernel acceptance matrix for the Option-B G9S1 semantic spline.
 *
 * <p>The tests intentionally use only the proposed public command and the
 * already-approved Locus V2 rich-result, metric, transform, identity and
 * persistence surfaces. They do not inspect a render polyline or prescribe a
 * private spline implementation.</p>
 */
final class G9S1SemanticSpline2DTest extends G9U0PublicSurfaceTestBase {

	private static final String SPLINE_BRANCH = "spline-v2/main";

	@Test
	void s01ToS03LineCircleAndConicProduceRichResults() {
		final GeoLocusV2 spline = createLineSpline("S");
		final GeoLocusV2 defaultDegree = add(
				"Default=SplineV2({SA,SB,SC,SD})");
		add("line:x=0");
		add("segment=Segment((0,-1),(0,1))");
		add("ray=Ray((0,-1),(0,1))");
		add("circle=Circle((0,0),1)");
		add("ellipse=Ellipse((-1,0),(1,0),1.5)");

		GeoLocusIntersectionResult line = add("R1=Intersect(S,line)");
		GeoLocusIntersectionResult segment = add("RS=Intersect(S,segment)");
		GeoLocusIntersectionResult ray = add("RR=Intersect(S,ray)");
		GeoLocusIntersectionResult circle = add("R2=Intersect(S,circle)");
		GeoLocusIntersectionResult conic = add("R3=Intersect(S,ellipse)");

		assertEquals(1, finite(line).size());
		assertEquals(1, finite(segment).size());
		assertEquals(1, finite(ray).size());
		assertEquals(2, finite(circle).size());
		assertEquals(2, finite(conic).size());
		assertAllDistinctTokens(circle);
		assertTrue(finite(circle).stream().allMatch(solution ->
				circle.isPointAdmissible(solution.getIdentity().getRootToken())));
		assertEquals(SPLINE_BRANCH,
				spline.getSemanticDefinition().getBranches().get(0).getBranchKey());
		assertPoint(defaultDegree, 0.5, 0, 0);
	}

	@Test
	void s04MultipleRootsAcrossSpansRemainDistinct() {
		createLineSpline("S");
		add("implicit=ImplicitCurve((x+1)*x*(x-1)+y)");
		GeoLocusIntersectionResult result = add("R=Intersect(S,implicit)");

		assertEquals(3, finite(result).size());
		assertAllDistinctTokens(result);
		assertEquals(List.of(-1.0, 0.0, 1.0), roundedX(result));
	}

	@Test
	void s05RootAtKnotHasCanonicalSingleOwnership() {
		createKnotLineSpline("S");
		add("knotLine:x=-1");
		GeoLocusIntersectionResult result = add("R=Intersect(S,knotLine)");

		assertEquals(1, finite(result).size());
		assertEquals(-1, finite(result).get(0).getEvaluatedPoint().getX(), 1E-10);
		assertEquals(1.0 / 3.0, finite(result).get(0).getRevisionEvidence()
				.getSemanticParameter(), 1E-10);
	}

	@Test
	void s06AndS07TangencyAndNearTangencyRemainTyped() {
		createLineSpline("S");
		add("tangent=Circle((0,1),1)");
		add("near=Circle((0,0.999999),1)");
		GeoLocusIntersectionResult tangent = add("Rt=Intersect(S,tangent)");
		GeoLocusIntersectionResult near = add("Rn=Intersect(S,near)");
		add("coincident:y=0");
		GeoLocusIntersectionResult overlap = add("Ro=Intersect(S,coincident)");

		assertEquals(1, finite(tangent).size(), () -> "tangent parameters="
				+ finite(tangent).stream().map(solution -> solution
						.getRevisionEvidence().getSemanticParameter()).toList());
		assertEquals(ContactClass.TANGENT_ESTABLISHED,
				finite(tangent).get(0).getClassification().getContactClass());
		assertEquals(2, finite(near).size());
		assertTrue(finite(near).stream().allMatch(solution -> solution
				.getClassification().getContactClass()
				== ContactClass.TRANSVERSE_ESTABLISHED));
		assertTrue(finite(tangent).stream().noneMatch(solution ->
				tangent.isPointAdmissible(solution.getIdentity().getRootToken())));
		assertTrue(finite(overlap).isEmpty());
	}

	@Test
	void s08SelfIntersectionPreservesDistinctSemanticPreimages() {
		GeoLocusV2 spline = createSelfIntersectingSpline("S");
		GeoPoint first = add("P=Point(S,\"" + SPLINE_BRANCH + "\",0.25)");
		GeoPoint second = add("Q=Point(S,\"" + SPLINE_BRANCH + "\",0.75)");
		assertEquals(first.getInhomX(), second.getInhomX(), 1E-10);
		assertEquals(first.getInhomY(), second.getInhomY(), 1E-10);
		assertNotEquals(first.getParentAlgorithm(), second.getParentAlgorithm());

		add("cross:y=1");
		GeoLocusIntersectionResult result = add("R=Intersect(S,cross)");
		long preimages = finite(result).stream().map(solution -> solution
				.getRevisionEvidence().getSemanticParameter()).distinct().count();
		assertTrue(preimages >= 2);
		assertEquals(SPLINE_BRANCH,
				spline.getSemanticDefinition().getBranches().get(0).getBranchKey());
	}

	@Test
	void s09ToS11SplinePairImplicitAndLocusPairUseSemanticAuthority() {
		createLineSpline("S");
		createVerticalSpline("T");
		add("implicit=ImplicitCurve(x^3+y^3)");
		GeoFunction functionTarget = add("function(x)=x");
		assertTrue(functionTarget.setInterval(-1, 1));
		GeoLocusV2 locus = createScalarLocus("L", "u", "U", "(0,u)",
				"{false,{-2,2,true,true}}");

		GeoLocusIntersectionResult splinePair = add("R1=Intersect(S,T)");
		GeoLocusIntersectionResult implicit = add("R2=Intersect(S,implicit)");
		GeoLocusIntersectionResult locusPair = add("R3=Intersect(S,L)");
		GeoLocusIntersectionResult function = add("R4=Intersect(S,function)");

		assertEquals(1, finite(splinePair).size());
		assertEquals(1, finite(implicit).size());
		assertEquals(1, finite(locusPair).size());
		assertEquals(1, finite(function).size());
		assertEquals(0, finite(splinePair).get(0).getEvaluatedPoint().getX(), 1E-10);
		assertTrue(locus.isDefined());
	}

	@Test
	void s12ConsumerEnumerationCannotReplaceExactTokens() {
		GeoLocusIntersectionResult result = fourRootResult("R");
		List<LocusIntersectionSolution2D> reversed = new ArrayList<>(finite(result));
		Collections.reverse(reversed);
		Set<String> requested = new LinkedHashSet<>();

		for (int index = 0; index < reversed.size(); index++) {
			String token = reversed.get(index).getIdentity().getRootToken();
			GeoPoint point = materialize(result, "X" + index, token);
			AlgoLocusIntersectionPointV2 parent = assertInstanceOf(
					AlgoLocusIntersectionPointV2.class, point.getParentAlgorithm());
			assertEquals(token, parent.getSelectedRootToken());
			assertTrue(requested.add(token));
		}
		assertEquals(4, requested.size());
	}

	@Test
	void s13AndS14DynamicPathsPreserveMaterializedBindings() {
		FourRootDynamic fixture = createDynamicFourRootFixture();
		List<GeoPoint> points = materializeAll(fixture.result(), "X");
		final Set<String> tokens = selectedTokens(points);

		fixture.offset().setValue(0.2);
		fixture.offset().updateCascade();
		Map<String, LocusPoint2D> direct = pointBindings(points);
		assertTrue(points.stream().allMatch(GeoPoint::isDefined));

		fixture.offset().setValue(0);
		fixture.offset().updateCascade();
		for (double value : List.of(0.05, 0.1, 0.15, 0.2)) {
			fixture.offset().setValue(value);
			fixture.offset().updateCascade();
			assertTrue(points.stream().allMatch(GeoPoint::isDefined));
		}
		assertEquals(direct, pointBindings(points));
		assertEquals(tokens, selectedTokens(points));
	}

	@Test
	void s15AndS16TopologyTransitionDormancyAndReactivationFailClosed() {
		FourRootDynamic fixture = createDynamicFourRootFixture();
		List<GeoPoint> points = materializeAll(fixture.result(), "X");
		final Set<String> originalTokens = selectedTokens(points);
		int constructionSize = getConstruction().getGeoSetConstructionOrder().size();

		fixture.innerRadius().setValue(1);
		fixture.outerRadius().setValue(1);
		fixture.innerRadius().updateCascade();
		fixture.outerRadius().updateCascade();
		assertTrue(points.stream().anyMatch(point -> !point.isDefined()));
		assertEquals(constructionSize,
				getConstruction().getGeoSetConstructionOrder().size());

		fixture.innerRadius().setValue(0.25);
		fixture.outerRadius().setValue(2.25);
		fixture.innerRadius().updateCascade();
		fixture.outerRadius().updateCascade();
		assertTrue(points.stream().allMatch(GeoPoint::isDefined));
		assertEquals(originalTokens, selectedTokens(points));
		assertEquals(constructionSize,
				getConstruction().getGeoSetConstructionOrder().size());
	}

	@Test
	void s17AndS18TotalAndPartialLengthUseSemanticAddresses() {
		createLineSpline("S");
		GeoLocusMetricResult total = add("M=LocusLength(S)");
		GeoPoint first = add("P=Point(S,\"" + SPLINE_BRANCH + "\",0.25)");
		GeoPoint second = add("Q=Point(S,\"" + SPLINE_BRANCH + "\",0.75)");
		GeoLocusMetricResult partial = add("MP=LocusLength(S,P,Q)");

		assertEquals(4, metricValue(total), 1E-8);
		assertEquals(2, metricValue(partial), 1E-8);
		assertSame(first, partial.getParentAlgorithm().getInput(1));
		assertSame(second, partial.getParentAlgorithm().getInput(2));
	}

	@Test
	void partialLengthScalarRoutesThroughTheRichSemanticAuthority() {
		GeoLocusV2 locus = createLine();
		GeoPoint locusStart = add("LP=Point(L,\"" + BRANCH + "\",-1)");
		GeoPoint locusEnd = add("LQ=Point(L,\"" + BRANCH + "\",1)");
		GeoLocusMetricResult locusRich = add("LR=LocusLength(L,LP,LQ)");
		assertRichBetweenMetric(locus, locusRich, locusStart, locusEnd,
				BRANCH, -1, 1, 2);
		GeoNumeric locusScalar = add("LS=Length(L,LP,LQ)");
		assertScalarBetweenMetric(locus, locusScalar, locusStart, locusEnd, 2);

		GeoLocusV2 spline = createLineSpline("S");
		GeoPoint splineStart = add("SP=Point(S,\"" + SPLINE_BRANCH
				+ "\",0.25)");
		GeoPoint splineEnd = add("SQ=Point(S,\"" + SPLINE_BRANCH
				+ "\",0.75)");
		GeoLocusMetricResult splineRich = add("SR=LocusLength(S,SP,SQ)");
		assertRichBetweenMetric(spline, splineRich, splineStart, splineEnd,
				SPLINE_BRANCH, 0.25, 0.75, 2);
		GeoNumeric splineScalar = add("SS=Length(S,SP,SQ)");
		assertScalarBetweenMetric(spline, splineScalar, splineStart, splineEnd,
				2);
	}

	@Test
	void partialLengthScalarRecomputesAndFailsClosedWithoutMetricIntersectionCoupling() {
		GeoNumeric scale = add("a=1");
		add("A=(-2*a,0)");
		add("B=(-2*a/3,0)");
		add("C=(2*a/3,0)");
		add("D=(2*a,0)");
		GeoLocusV2 spline = add("S=SplineV2({A,B,C,D},3)");
		final GeoNumeric startParameter = add("u=0.25");
		GeoNumeric endParameter = add("v=0.75");
		GeoPoint start = add("P=Point(S,\"" + SPLINE_BRANCH + "\",u)");
		GeoPoint end = add("Q=Point(S,\"" + SPLINE_BRANCH + "\",v)");
		GeoNumeric scalar = add("MP=Length(S,P,Q)");
		assertScalarBetweenMetric(spline, scalar, start, end, 2);

		add("circle=Circle((0,0),1)");
		GeoLocusIntersectionResult intersections = add("R=Intersect(S,circle)");

		scale.setValue(2);
		scale.updateCascade();
		assertEquals(4, scalar.getDouble(), 1E-8);
		LocusIntersectionResult2D intersectionSnapshot =
				intersections.getIntersectionResult();
		endParameter.setValue(0.875);
		endParameter.updateCascade();
		assertEquals(5, scalar.getDouble(), 1E-8);
		assertSame(intersectionSnapshot, intersections.getIntersectionResult());

		startParameter.setValue(-0.1);
		startParameter.updateCascade();
		assertFalse(start.isDefined());
		assertFalse(scalar.isDefined());
		startParameter.setValue(0.25);
		startParameter.updateCascade();
		assertTrue(start.isDefined());
		assertEquals(5, scalar.getDouble(), 1E-8);
		scale.setUndefined();
		scale.updateCascade();
		assertFalse(spline.isDefined());
		assertFalse(scalar.isDefined());
		scale.setValue(2);
		scale.updateCascade();
		assertTrue(spline.isDefined());
		assertEquals(5, scalar.getDouble(), 1E-8);

		GeoLocusV2 other = createLineSpline("O");
		GeoPoint otherEnd = add("OQ=Point(O,\"" + SPLINE_BRANCH + "\",0.75)");
		GeoNumeric mismatched = add("BadSource=Length(S,P,OQ)");
		assertFalse(mismatched.isDefined());
		add("Afree=(1,0)");
		GeoNumeric unaddressed = add("BadPoint=Length(S,Afree,Q)");
		assertFalse(unaddressed.isDefined());
		assertTrue(other.isDefined());
	}

	@Test
	void partialLengthScalarIsSimilarityCovariantIncludingCollapsedImage() {
		createLineSpline("S");
		add("T=Translate(S,(3,4))");
		add("R=Rotate(S,pi/3,(1,2))");
		add("axis:y=x");
		add("F=Reflect(S,axis)");
		GeoNumeric factor = add("k=-3");
		add("D=Dilate(S,k,(1,2))");

		assertEquals(4, ((GeoNumeric) add("Total=Length(S)")).getDouble(),
				1E-8);
		assertTransformedPartial("S", "S", 2);
		assertTransformedPartial("T", "T", 2);
		assertTransformedPartial("R", "R", 2);
		assertTransformedPartial("F", "F", 2);
		GeoNumeric dilated = assertTransformedPartial("D", "D", 6);

		factor.setValue(0);
		factor.updateCascade();
		assertTrue(dilated.isDefined());
		assertEquals(0, dilated.getDouble(), 0);
		factor.setValue(2);
		factor.updateCascade();
		assertEquals(4, dilated.getDouble(), 1E-8);

		add("rpar=0");
		add("LPoint=(rpar,1)");
		add("LD={false,{-2,2,true,true}}");
		add("L=LocusV2(LPoint,rpar,LD)");
		add("LT=Translate(L,(1,2))");
		add("LP=Point(L,\"" + BRANCH + "\",-1)");
		add("LQ=Point(L,\"" + BRANCH + "\",1)");
		add("LTP=Point(LT,\"" + BRANCH + "\",-1)");
		add("LTQ=Point(LT,\"" + BRANCH + "\",1)");
		assertEquals(2, ((GeoNumeric) add("LM=Length(L,LP,LQ)"))
				.getDouble(), 1E-8);
		assertEquals(2, ((GeoNumeric) add("LTM=Length(LT,LTP,LTQ)"))
				.getDouble(), 1E-8);
	}

	@Test
	void partialLengthScalarUndoRedoCopyAndClassicRoutingRemainStable() {
		activateUndo();
		GeoLocusV2 source = createLineSpline("S");
		GeoPoint start = add("P=Point(S,\"" + SPLINE_BRANCH + "\",0.25)");
		GeoPoint end = add("Q=Point(S,\"" + SPLINE_BRANCH + "\",0.75)");
		getApp().storeUndoInfo();
		GeoNumeric scalar = add("MP=Length(S,P,Q)");
		AlgoLocusMetricScalarAdapter adapter = assertInstanceOf(
				AlgoLocusMetricScalarAdapter.class, scalar.getParentAlgorithm());
		GeoLocusMetricResult rich = adapter.getRichInput();
		SpatialIdentityRegistry registry = getConstruction()
				.getSpatialIdentityRegistry();
		final PersistentGeoId sourceId = idOf(registry, source);
		final PersistentGeoId startId = idOf(registry, start);
		final PersistentGeoId endId = idOf(registry, end);
		final PersistentGeoId richId = idOf(registry, rich);
		final PersistentGeoId scalarId = idOf(registry, scalar);
		getApp().storeUndoInfo();

		getKernel().undo();
		assertNull(lookup("MP"));
		getKernel().redo();
		scalar = assertInstanceOf(GeoNumeric.class, requireLookup("MP"));
		adapter = assertInstanceOf(AlgoLocusMetricScalarAdapter.class,
				scalar.getParentAlgorithm());
		rich = adapter.getRichInput();
		assertEquals(scalarId, idOf(registry, scalar));
		assertEquals(richId, idOf(registry, rich));
		assertEquals(2, scalar.getDouble(), 1E-8);

		String clipboard = InternalClipboard.getTextToSave(getApp(),
				Collections.singletonList(scalar), text -> text);
		paste(clipboard);
		GeoIdentityRecord copiedSourceRecord = recordCopying(registry, sourceId);
		GeoIdentityRecord copiedStartRecord = recordCopying(registry, startId);
		GeoIdentityRecord copiedEndRecord = recordCopying(registry, endId);
		GeoIdentityRecord copiedRichRecord = recordCopying(registry, richId);
		GeoIdentityRecord copiedScalarRecord = recordCopying(registry, scalarId);
		GeoLocusV2 copiedSource = assertInstanceOf(GeoLocusV2.class,
				registry.getGeo(copiedSourceRecord.getId()));
		GeoPoint copiedStart = assertInstanceOf(GeoPoint.class,
				registry.getGeo(copiedStartRecord.getId()));
		GeoPoint copiedEnd = assertInstanceOf(GeoPoint.class,
				registry.getGeo(copiedEndRecord.getId()));
		GeoLocusMetricResult copiedRich = assertInstanceOf(
				GeoLocusMetricResult.class,
				registry.getGeo(copiedRichRecord.getId()));
		GeoNumeric copiedScalar = assertInstanceOf(GeoNumeric.class,
				registry.getGeo(copiedScalarRecord.getId()));
		assertScalarBetweenMetric(copiedSource, copiedScalar, copiedStart,
				copiedEnd, 2);
		assertSame(copiedRich,
				((AlgoLocusMetricScalarAdapter) copiedScalar.getParentAlgorithm())
						.getRichInput());

		add("Classic=Curve(t,0,t,-2,2)");
		add("CP=(-1,0)");
		add("CQ=(1,0)");
		GeoNumeric classicLength = add("ClassicPartial=Length(Classic,CP,CQ)");
		assertTrue(classicLength.isDefined());
		assertEquals(2, classicLength.getDouble(), 1E-8);
		assertFalse(classicLength.getParentAlgorithm()
				instanceof AlgoLocusMetricScalarAdapter);
	}

	@Test
	void s19SelfIntersectionRequiresAddressedMetricEndpoints() {
		createSelfIntersectingSpline("S");
		add("P=Point(S,\"" + SPLINE_BRANCH + "\",0.25)");
		add("Q=Point(S,\"" + SPLINE_BRANCH + "\",0.75)");
		GeoLocusMetricResult addressed = add("M=LocusLength(S,P,Q)");
		assertTrue(metricValue(addressed) > 0);

		add("A=(0,1)");
		GeoLocusMetricResult ambiguous = add("Ambiguous=LocusLength(S,A,Q)");
		assertEquals(MetricComputationStatus.INVALID_QUERY,
				ambiguous.getMetricResult().getComputationStatus());
	}

	@Test
	void s20ToS22SimilarityClosureMetricAndIntersectionCovariance() {
		GeoLocusV2 source = createLineSpline("S");
		GeoLocusV2 translated = add("T=Translate(S,(3,4))");
		GeoLocusV2 composed = add("C=Rotate(Dilate(T,-2,(1,1)),pi/2)");
		add("reflectAxis:y=0");
		GeoLocusV2 reflected = add("F=Reflect(C,reflectAxis)");
		assertTrue(composed.isDefined());
		assertTrue(reflected.isDefined());
		assertNotEquals(source.getPersistentLocusId(),
				translated.getPersistentLocusId());
		assertNotEquals(translated.getPersistentLocusId(),
				composed.getPersistentLocusId());

		double sourceLength = metricValue(add("MS=LocusLength(S)"));
		assertEquals(sourceLength, metricValue(add("MT=LocusLength(T)")), 1E-8);
		assertEquals(2 * sourceLength,
				metricValue(add("MC=LocusLength(C)")), 1E-7);
		assertEquals(2 * sourceLength,
				metricValue(add("MF=LocusLength(F)")), 1E-7);

		add("c1=Circle((0,0),1)");
		add("c2=Circle((3,4),1)");
		GeoLocusIntersectionResult first = add("R1=Intersect(S,c1)");
		GeoLocusIntersectionResult second = add("R2=Intersect(T,c2)");
		assertTranslatedPointSetsNear(first, second, 3, 4, 1E-9);
		assertTrue(Collections.disjoint(tokens(first), tokens(second)));
	}

	@Test
	void s23AndS24DefiningPointsAndTargetRemainDynamic() {
		GeoNumeric height = add("h=0");
		final GeoNumeric weightOffset = add("q=0");
		add("A=(-2,h)");
		add("B=(-1,h)");
		add("C=(0.5,h)");
		add("D=(2,h)");
		add("w(x,y)=sqrt(x^2+y^2)+q");
		GeoLocusV2 spline = add("S=SplineV2({A,B,C,D},3,w)");
		final GeoNumeric radius = add("r=1.5");
		add("c=Circle((0,0),r)");
		GeoLocusIntersectionResult result = add("R=Intersect(S,c)");
		List<GeoPoint> points = materializeAll(result, "X");
		long revision = spline.getSemanticRevision();

		height.setValue(0.25);
		height.updateCascade();
		assertTrue(spline.getSemanticRevision() > revision);
		assertTrue(points.stream().allMatch(GeoPoint::isDefined));
		revision = spline.getSemanticRevision();
		weightOffset.setValue(0.2);
		weightOffset.updateCascade();
		assertTrue(spline.getSemanticRevision() > revision);
		assertTrue(points.stream().allMatch(GeoPoint::isDefined));
		radius.setValue(1.25);
		radius.updateCascade();
		assertTrue(points.stream().allMatch(GeoPoint::isDefined));
		assertEquals(2, finite(result).size());
	}

	@Test
	void s25SaveReopenReconstructsAuthoritativeInputsAndChildren() {
		GeoLocusV2 source = createLineSpline("S");
		PersistentGeoId sourceId = source.getPersistentLocusId();
		add("c=Circle((0,0),1)");
		GeoLocusIntersectionResult result = add("R=Intersect(S,c)");
		String token = finite(result).get(0).getIdentity().getRootToken();
		materialize(result, "X", token);
		add("M=LocusLength(S)");
		String ledger = result.getTokenLedgerState();
		String xml = getApp().getXML();
		assertTrue(xml.contains("name=\"SplineV2\""));
		assertFalse(xml.contains("renderVertices"));

		getApp().setXML(xml, true);
		GeoLocusV2 reopened = assertInstanceOf(GeoLocusV2.class,
				requireLookup("S"));
		GeoLocusIntersectionResult reopenedResult = assertInstanceOf(
				GeoLocusIntersectionResult.class, requireLookup("R"));
		assertEquals(sourceId, reopened.getPersistentLocusId());
		assertEquals(ledger, reopenedResult.getTokenLedgerState());
		assertTrue(assertInstanceOf(GeoPoint.class, requireLookup("X")).isDefined());
		assertEquals(4, metricValue(assertInstanceOf(GeoLocusMetricResult.class,
				requireLookup("M"))), 1E-8);
	}

	@Test
	void s26UndoRedoRestoresSemanticIdentityGraph() {
		activateUndo();
		GeoLocusV2 spline = createLineSpline("S");
		final PersistentGeoId id = spline.getPersistentLocusId();
		add("c=Circle((0,0),1)");
		GeoLocusIntersectionResult result = add("R=Intersect(S,c)");
		getApp().storeUndoInfo();
		String token = finite(result).get(0).getIdentity().getRootToken();
		assertTrue(materialize(result, "X", token).isDefined());
		getApp().storeUndoInfo();

		getKernel().undo();
		assertNull(lookup("X"));
		assertNotNull(requireLookup("S"));
		assertNotNull(requireLookup("R"));
		getKernel().redo();
		GeoLocusV2 restored = assertInstanceOf(GeoLocusV2.class,
				requireLookup("S"));
		assertEquals(id, restored.getPersistentLocusId());
		GeoPoint restoredPoint = assertInstanceOf(GeoPoint.class,
				requireLookup("X"));
		assertTrue(restoredPoint.isDefined());
		assertEquals(token, assertInstanceOf(AlgoLocusIntersectionPointV2.class,
				restoredPoint.getParentAlgorithm()).getSelectedRootToken());
	}

	@Test
	void s27CopyRemapsSplineAndDependencySlice() {
		GeoLocusV2 source = createLineSpline("S");
		PersistentGeoId sourceId = source.getPersistentLocusId();
		SpatialIdentityRegistry registry = getConstruction()
				.getSpatialIdentityRegistry();
		String clipboard = InternalClipboard.getTextToSave(getApp(),
				Collections.singletonList(source), text -> text);
		paste(clipboard);

		GeoIdentityRecord copiedRecord = registry.getRecords().stream()
				.filter(GeoIdentityRecord.class::isInstance)
				.map(GeoIdentityRecord.class::cast)
				.filter(record -> sourceId.equals(record.getCopySourceId()))
				.findFirst().orElseThrow();
		GeoLocusV2 copy = assertInstanceOf(GeoLocusV2.class,
				registry.getGeo(copiedRecord.getId()));
		assertNotEquals(sourceId, copy.getPersistentLocusId());
		assertEquals(sourceId, copiedRecord.getCopySourceId());
		assertPoint(copy, 0.25, -1, 0);
		assertFalse(copiedRecord.getDependencies().isEmpty());
	}

	@Test
	void s28ClassicSplineCompatibilityRemainsUnchanged() {
		add("A=(-2,1)");
		add("B=(-0.5,-1)");
		add("C=(0.75,2)");
		add("D=(2,-0.25)");
		add("w(x,y)=sqrt(x^2+4*y^2)+0.5");
		final GeoCurveCartesianND classic = assertInstanceOf(
				GeoCurveCartesianND.class,
				add("Classic=Spline({A,B,C,D},3)"));
		final GeoLocusV2 semantic = add(
				"Semantic=SplineV2({A,B,C,D},3)");
		final GeoCurveCartesianND weightedClassic = assertInstanceOf(
				GeoCurveCartesianND.class,
				add("WeightedClassic=Spline({A,B,C,D},3,w)"));
		final GeoLocusV2 weightedSemantic = add(
				"WeightedSemantic=SplineV2({A,B,C,D},3,w)");
		final GeoLocusV2 minimumPointWrapping = add(
				"MinimumPointWrapping=SplineV2(A,B,C)");
		final GeoLocusV2 minimumPointList = add(
				"MinimumPointList=SplineV2({A,B,C})");
		assertFalse(((GeoElement) classic) instanceof GeoLocusV2);
		assertTrue(semantic.isDefined());
		assertTrue(weightedSemantic.isDefined());
		assertTrue(minimumPointWrapping.isDefined());
		for (double parameter : List.of(0.0, 0.25, 0.5, 0.75, 1.0)) {
			LocusEvaluation2D wrapped = evaluate(minimumPointWrapping, parameter);
			LocusEvaluation2D listed = evaluate(minimumPointList, parameter);
			assertTrue(wrapped.isValid(), wrapped.getDiagnostic());
			assertTrue(listed.isValid(), listed.getDiagnostic());
			assertEquals(listed.getPoint().getX(), wrapped.getPoint().getX(), 1E-12);
			assertEquals(listed.getPoint().getY(), wrapped.getPoint().getY(), 1E-12);
		}
		assertClassicEquivalent(classic, semantic, 1E-8);
		assertClassicEquivalent(weightedClassic, weightedSemantic, 1E-8);

		AppCommon featureOff = AppCommonFactory.create(new AppConfigGeoCeDG(false));
		process(featureOff, "A=(-2,1)");
		process(featureOff, "B=(-0.5,-1)");
		process(featureOff, "C=(0.75,2)");
		process(featureOff, "D=(2,-0.25)");
		assertTrue(process(featureOff, "Classic=Spline({A,B,C,D},3)")
				instanceof GeoCurveCartesianND);
		assertThrows(AssertionError.class, () -> process(featureOff,
				"Blocked=SplineV2({A,B,C,D},3)"));
	}

	@Test
	void closedPeriodicSplineHasSeamAwareMetricsAndIntersection() {
		add("A=(1,0)");
		add("B=(0,1)");
		add("C=(-1,0)");
		add("D=(0,-1)");
		final GeoLocusV2 spline = add("S=SplineV2({A,B,C,D,A},3)");
		assertTrue(spline.getSemanticDefinition().getProvider().isPeriodic());
		assertFalse(spline.getSemanticDefinition().getProvider()
				.getDeclaredDomain().isUpperClosed());

		final LocusEvaluation2D lowerSeam = evaluate(spline, 0);
		final LocusEvaluation2D equivalentUpperSeam = evaluate(spline, 1);
		assertTrue(lowerSeam.isValid(), lowerSeam.getDiagnostic());
		assertTrue(equivalentUpperSeam.isValid(),
				equivalentUpperSeam.getDiagnostic());
		assertEquals(lowerSeam.getPoint().getX(),
				equivalentUpperSeam.getPoint().getX(), 1E-9);
		assertEquals(lowerSeam.getPoint().getY(),
				equivalentUpperSeam.getPoint().getY(), 1E-9);

		final GeoLocusMetricResult total = add("M=LocusLength(S)");
		add("P=Point(S,\"" + SPLINE_BRANCH + "\",0.9)");
		add("Q=Point(S,\"" + SPLINE_BRANCH + "\",0.1)");
		final GeoLocusMetricResult acrossSeam = add(
				"MP=LocusLength(S,P,Q)");
		assertEquals(MetricComputationStatus.SUCCESS,
				total.getMetricResult().getComputationStatus());
		assertEquals(MetricComputationStatus.SUCCESS,
				acrossSeam.getMetricResult().getComputationStatus());
		assertTrue(metricValue(total) > 0);
		assertTrue(metricValue(acrossSeam) > 0);
		assertTrue(metricValue(acrossSeam) < metricValue(total));

		add("axis:y=0");
		final GeoLocusIntersectionResult result = add("R=Intersect(S,axis)");
		assertEquals(2, finite(result).size());
		assertEquals(1, finite(result).stream().filter(solution ->
				Math.abs(solution.getRevisionEvidence().getSemanticParameter())
						< 1E-9).count());
		assertAllDistinctTokens(result);
	}

	@Test
	void invalidDegreeWeightAndZeroLengthInputsFailClosed() {
		add("A=(0,0)");
		add("B=(1,0)");
		add("C=(1,0)");
		add("D=(2,0)");
		add("E=(3,0)");
		assertThrows(AssertionError.class,
				() -> add("BadDegree=SplineV2({A,B,D,E},2)"));
		GeoLocusV2 zeroSpan = add("ZeroSpan=SplineV2({A,B,C,D},3)");
		assertFalse(zeroSpan.isDefined());
		add("w(x,y)=0");
		GeoLocusV2 invalidWeight = add("BadWeight=SplineV2({A,B,D,E},3,w)");
		assertFalse(invalidWeight.isDefined());
		assertTrue(invalidWeight.getSemanticDefinition() == null
				|| invalidWeight.getSemanticDefinition().getBranches().isEmpty());

		final GeoNumeric dynamicDegree = add("n=3");
		final GeoLocusV2 dynamic = add("Dynamic=SplineV2({A,B,D,E},n)");
		assertTrue(dynamic.isDefined());
		dynamicDegree.setValue(2);
		dynamicDegree.updateCascade();
		assertFalse(dynamic.isDefined());
		dynamicDegree.setValue(3);
		dynamicDegree.updateCascade();
		assertTrue(dynamic.isDefined());
	}

	@Test
	void s29FunctionalWorkIsBoundedAndDeterministic() {
		StringBuilder points = new StringBuilder();
		for (int index = 0; index < 25; index++) {
			String label = "P" + index;
			add(label + "=(" + (index - 12) + "/4,sin(" + index + "/3))");
			if (index > 0) {
				points.append(',');
			}
			points.append(label);
		}
		GeoLocusV2 spline = add("S=SplineV2({" + points + "},5)");
		add("axis:y=0");
		GeoLocusIntersectionResult first = add("R1=Intersect(S,axis)");
		GeoLocusIntersectionResult second = add("R2=Intersect(S,axis)");
		var firstWork = first.getIntersectionResult().getWork();
		var secondWork = second.getIntersectionResult().getWork();

		assertEquals(firstWork.getSemanticEvaluations(),
				secondWork.getSemanticEvaluations());
		assertEquals(firstWork.getIsolationSubdivisions(),
				secondWork.getIsolationSubdivisions());
		assertTrue(firstWork.getSemanticEvaluations()
				<= LocusIntersectionWorkBudget2D
						.DEFAULT_MAXIMUM_SEMANTIC_EVALUATIONS);
		assertTrue(firstWork.getIsolationSubdivisions()
				<= LocusIntersectionWorkBudget2D
						.DEFAULT_MAXIMUM_ISOLATION_SUBDIVISIONS);
		assertTrue(firstWork.hasZeroForbiddenAuthorityReads());
		assertTrue(spline.isDefined());
	}

	@Test
	void s30SemanticConsumersNeverReadRenderAuthority() {
		GeoLocusV2 spline = createLineSpline("S");
		spline.getInstrumentation().reset();
		add("P=Point(S,\"" + SPLINE_BRANCH + "\",0.25)");
		add("M=LocusLength(S)");
		add("c=Circle((0,0),1)");
		GeoLocusIntersectionResult result = add("R=Intersect(S,c)");

		assertEquals(0, spline.getInstrumentation().getRenderEvaluations());
		assertEquals(0, spline.getInstrumentation().getWholeLocusRegenerations());
		assertTrue(result.getIntersectionResult().getWork()
				.hasZeroForbiddenAuthorityReads());
	}

	private GeoLocusV2 createLineSpline(String label) {
		add(label + "A=(-2,0)");
		add(label + "B=(-2/3,0)");
		add(label + "C=(2/3,0)");
		add(label + "D=(2,0)");
		return add(label + "=SplineV2({" + label + "A," + label + "B,"
				+ label + "C," + label + "D},3)");
	}

	private GeoLocusV2 createKnotLineSpline(String label) {
		add(label + "A=(-3,0)");
		add(label + "B=(-1,0)");
		add(label + "C=(1,0)");
		add(label + "D=(3,0)");
		return add(label + "=SplineV2({" + label + "A," + label + "B,"
				+ label + "C," + label + "D},3)");
	}

	private GeoLocusV2 createVerticalSpline(String label) {
		add(label + "A=(0,-2)");
		add(label + "B=(0,-2/3)");
		add(label + "C=(0,2/3)");
		add(label + "D=(0,2)");
		return add(label + "=SplineV2({" + label + "A," + label + "B,"
				+ label + "C," + label + "D},3)");
	}

	private GeoLocusV2 createSelfIntersectingSpline(String label) {
		add(label + "A=(-1,0)");
		add(label + "B=(0,1)");
		add(label + "C=(1,0)");
		add(label + "D=(0,1)");
		add(label + "E=(-1,0)");
		return add(label + "=SplineV2({" + label + "A," + label + "B,"
				+ label + "C," + label + "D," + label + "E},3)");
	}

	private GeoLocusIntersectionResult fourRootResult(String resultLabel) {
		createLineSpline("S");
		add("implicit=ImplicitCurve((x^2-0.25)*(x^2-2.25)+y)");
		return add(resultLabel + "=Intersect(S,implicit)");
	}

	private FourRootDynamic createDynamicFourRootFixture() {
		createLineSpline("S");
		GeoNumeric offset = add("motionOffset=0");
		GeoNumeric innerRadius = add("innerRadius=0.25");
		GeoNumeric outerRadius = add("outerRadius=2.25");
		add("implicit=ImplicitCurve(((x-motionOffset)^2-innerRadius)"
				+ "*((x-motionOffset)^2-outerRadius)+y)");
		GeoLocusIntersectionResult result = add("R=Intersect(S,implicit)");
		assertEquals(4, finite(result).size());
		return new FourRootDynamic(offset, innerRadius, outerRadius, result);
	}

	private static List<LocusIntersectionSolution2D> finite(
			GeoLocusIntersectionResult result) {
		assertNotNull(result);
		LocusIntersectionResult2D rich = result.getIntersectionResult();
		assertNotNull(rich);
		return rich.getFiniteSolutions();
	}

	private static void assertAllDistinctTokens(
			GeoLocusIntersectionResult result) {
		assertEquals(finite(result).size(), tokens(result).size());
	}

	private static Set<String> tokens(GeoLocusIntersectionResult result) {
		Set<String> tokens = new LinkedHashSet<>();
		for (LocusIntersectionSolution2D solution : finite(result)) {
			tokens.add(solution.getIdentity().getRootToken());
		}
		return tokens;
	}

	private GeoPoint materialize(GeoLocusIntersectionResult result,
			String label, String token) {
		assertTrue(result.isPointAdmissible(token));
		GeoText tokenInput = new GeoText(getConstruction(), token);
		tokenInput.setAuxiliaryObject(true);
		tokenInput.setEuclidianVisible(false);
		return LocusV2PublicOperations.selectIntersectionPoint(getConstruction(),
				label, result, tokenInput);
	}

	private List<GeoPoint> materializeAll(GeoLocusIntersectionResult result,
			String prefix) {
		List<GeoPoint> points = new ArrayList<>();
		int index = 0;
		for (LocusIntersectionSolution2D solution : finite(result)) {
			points.add(materialize(result, prefix + index++,
					solution.getIdentity().getRootToken()));
		}
		return points;
	}

	private static Set<String> selectedTokens(List<GeoPoint> points) {
		Set<String> result = new LinkedHashSet<>();
		for (GeoPoint point : points) {
			AlgoLocusIntersectionPointV2 parent = (AlgoLocusIntersectionPointV2)
					point.getParentAlgorithm();
			result.add(parent.getSelectedRootToken());
		}
		return result;
	}

	private static Map<String, LocusPoint2D> pointBindings(
			List<GeoPoint> points) {
		Map<String, LocusPoint2D> result = new LinkedHashMap<>();
		for (GeoPoint point : points) {
			AlgoLocusIntersectionPointV2 parent = (AlgoLocusIntersectionPointV2)
					point.getParentAlgorithm();
			result.put(parent.getSelectedRootToken(), new LocusPoint2D(
					point.getInhomX(), point.getInhomY()));
		}
		return result;
	}

	private static List<Double> roundedX(GeoLocusIntersectionResult result) {
		return finite(result).stream()
				.map(solution -> Math.rint(solution.getEvaluatedPoint().getX()
						* 1E9) / 1E9)
				// The existing rounded geometric comparison is not IEEE sign-bit
				// identity. Normalize only an already-rounded zero, not the root.
				.map(value -> value == 0 ? 0.0 : value)
				.sorted().toList();
	}

	private static void assertTranslatedPointSetsNear(
			GeoLocusIntersectionResult source,
			GeoLocusIntersectionResult transformed, double dx, double dy,
			double tolerance) {
		List<LocusPoint2D> remaining = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : finite(transformed)) {
			remaining.add(solution.getEvaluatedPoint());
		}
		for (LocusIntersectionSolution2D solution : finite(source)) {
			double expectedX = solution.getEvaluatedPoint().getX() + dx;
			double expectedY = solution.getEvaluatedPoint().getY() + dy;
			int match = -1;
			for (int index = 0; index < remaining.size(); index++) {
				LocusPoint2D candidate = remaining.get(index);
				if (Math.hypot(candidate.getX() - expectedX,
						candidate.getY() - expectedY) <= tolerance) {
					match = index;
					break;
				}
			}
			assertTrue(match >= 0, "No transformed root near expected point");
			remaining.remove(match);
		}
		assertTrue(remaining.isEmpty());
	}

	private GeoNumeric assertTransformedPartial(String prefix,
			String sourceLabel, double expected) {
		GeoPoint start = add(prefix + "P=Point(" + sourceLabel + ",\""
				+ SPLINE_BRANCH + "\",0.25)");
		GeoPoint end = add(prefix + "Q=Point(" + sourceLabel + ",\""
				+ SPLINE_BRANCH + "\",0.75)");
		GeoNumeric scalar = add(prefix + "M=Length(" + sourceLabel + ","
				+ prefix + "P," + prefix + "Q)");
		assertScalarBetweenMetric(assertInstanceOf(GeoLocusV2.class,
				requireLookup(sourceLabel)), scalar, start, end, expected);
		return scalar;
	}

	private static void assertRichBetweenMetric(GeoLocusV2 source,
			GeoLocusMetricResult rich, GeoPoint start, GeoPoint end,
			String branch, double startParameter, double endParameter,
			double expected) {
		assertTrue(rich.isDefined());
		assertEquals(source.getSemanticRevision(),
				rich.getSourceSemanticRevision());
		assertEquals(MetricComputationStatus.SUCCESS,
				rich.getMetricResult().getComputationStatus());
		assertEquals(MetricValueKind.FINITE,
				rich.getMetricResult().getMetricValue().getKind());
		assertEquals(MetricCoverage.COMPLETE,
				rich.getMetricResult().getCoverage());
		assertEquals(expected, metricValue(rich), 1E-8);
		assertTrue(rich.getMetricResult().getErrorEvidence()
				.getNumericGuarantee().isPresent());
		assertNotEquals(NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
				rich.getMetricResult().getErrorEvidence()
						.getNumericGuarantee().orElseThrow());
		assertEquals(source.getLocusIdentity(), rich.getMetricResult()
				.getProvenance().getLocusIdentity());
		assertEquals(source.getSemanticRevision(), rich.getMetricResult()
				.getProvenance().getSemanticRevision());
		AlgoLocusBetweenMetricV2 parent = assertInstanceOf(
				AlgoLocusBetweenMetricV2.class, rich.getParentAlgorithm());
		assertSame(source, parent.getInput(0));
		assertSame(start, parent.getInput(1));
		assertSame(end, parent.getInput(2));
		assertSemanticPosition(start, source, branch, startParameter);
		assertSemanticPosition(end, source, branch, endParameter);
	}

	private static void assertScalarBetweenMetric(GeoLocusV2 source,
			GeoNumeric scalar, GeoPoint start, GeoPoint end, double expected) {
		assertTrue(scalar.isDefined());
		assertEquals(expected, scalar.getDouble(), 1E-8);
		AlgoLocusMetricScalarAdapter adapter = assertInstanceOf(
				AlgoLocusMetricScalarAdapter.class, scalar.getParentAlgorithm());
		GeoLocusMetricResult rich = adapter.getRichInput();
		assertTrue(rich.isAuxiliaryObject());
		assertRichBetweenMetric(source, rich, start, end,
				((AlgoSemanticLocusPoint2D) start.getParentAlgorithm())
						.getSemanticPosition().getBranchKey(),
				((AlgoSemanticLocusPoint2D) start.getParentAlgorithm())
						.getSemanticPosition().getProviderCanonicalParameter(),
				((AlgoSemanticLocusPoint2D) end.getParentAlgorithm())
						.getSemanticPosition().getProviderCanonicalParameter(),
				expected);
	}

	private static void assertSemanticPosition(GeoPoint point,
			GeoLocusV2 source, String branch, double parameter) {
		AlgoSemanticLocusPoint2D parent = assertInstanceOf(
				AlgoSemanticLocusPoint2D.class, point.getParentAlgorithm());
		assertSame(source, parent.getSource());
		assertEquals(branch, parent.getSemanticPosition().getBranchKey());
		assertEquals(parameter, parent.getSemanticPosition()
				.getProviderCanonicalParameter(), 0);
	}

	private static PersistentGeoId idOf(SpatialIdentityRegistry registry,
			GeoElement geo) {
		PersistentGeoId id = registry.getPersistentGeoId(geo);
		assertNotNull(id);
		return id;
	}

	private static GeoIdentityRecord recordCopying(
			SpatialIdentityRegistry registry, PersistentGeoId sourceId) {
		return registry.getRecords().stream()
				.filter(GeoIdentityRecord.class::isInstance)
				.map(GeoIdentityRecord.class::cast)
				.filter(record -> sourceId.equals(record.getCopySourceId()))
				.findFirst().orElseThrow();
	}

	private static double metricValue(GeoLocusMetricResult metric) {
		return metric.getMetricResult().getMetricValue().getFiniteValue()
				.orElseThrow();
	}

	private static LocusEvaluation2D evaluate(GeoLocusV2 locus,
			double parameter) {
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.reference()) {
			return locus.evaluate(SPLINE_BRANCH, parameter, session);
		}
	}

	private static void assertClassicEquivalent(GeoCurveCartesianND classic,
			GeoLocusV2 semantic, double tolerance) {
		assertEquals(0, classic.getMinParameter(), 0);
		assertEquals(1, classic.getMaxParameter(), 0);
		for (int sample = 0; sample <= 40; sample++) {
			double parameter = sample / 40.0;
			LocusEvaluation2D evaluation = evaluate(semantic, parameter);
			assertTrue(evaluation.isValid(), evaluation.getDiagnostic());
			assertEquals(classic.getFun(0).value(parameter),
					evaluation.getPoint().getX(), tolerance,
					"x compatibility at t=" + parameter);
			assertEquals(classic.getFun(1).value(parameter),
					evaluation.getPoint().getY(), tolerance,
					"y compatibility at t=" + parameter);
		}
	}

	private static void assertPoint(GeoLocusV2 locus, double parameter,
			double expectedX, double expectedY) {
		LocusEvaluation2D evaluation = evaluate(locus, parameter);
		assertTrue(evaluation.isValid(), evaluation.getDiagnostic());
		assertEquals(expectedX, evaluation.getPoint().getX(), 1E-9);
		assertEquals(expectedY, evaluation.getPoint().getY(), 1E-9);
	}

	private void paste(String clipboard) {
		int separator = clipboard.indexOf('\n');
		List<String> labels = new ArrayList<>(Arrays.asList(
				clipboard.substring(0, separator).split(" ")));
		InternalClipboard.pasteGeoGebraXMLInternal(getApp(), labels,
				clipboard.substring(separator));
	}

	private static GeoElement process(AppCommon app, String command) {
		GeoElementND[] result = app.getKernel().getAlgebraProcessor()
				.processAlgebraCommandNoExceptionHandling(command, false,
						TestErrorHandler.INSTANCE, false, null);
		assertNotNull(result);
		assertTrue(result.length > 0);
		return result[0].toGeoElement();
	}

	private record FourRootDynamic(GeoNumeric offset, GeoNumeric innerRadius,
			GeoNumeric outerRadius, GeoLocusIntersectionResult result) {
	}
}
