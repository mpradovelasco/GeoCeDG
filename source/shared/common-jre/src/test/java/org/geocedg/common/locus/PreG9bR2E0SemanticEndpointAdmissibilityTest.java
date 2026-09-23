/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.geocedg.common.kernel.algos.SemanticMetricEndpointResolver2D;
import org.geocedg.common.kernel.algos.SemanticMetricEndpointResolver2D.Family;
import org.geocedg.common.kernel.algos.SemanticMetricEndpointResolver2D.Status;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.kernel.commands.AlgebraProcessor;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.util.InternalClipboard;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R2-E0/E1 objective A: every exactly addressable semantic endpoint
 * family of metrics section 24, with the fail-closed exclusions.
 */
final class PreG9bR2E0SemanticEndpointAdmissibilityTest extends G9U0PublicSurfaceTestBase {

	private final SemanticMetricEndpointResolver2D resolver =
			new SemanticMetricEndpointResolver2D();

	@Test
	void singleSourceRootsOnLinesAndConicsAreEndpointsOnTheirSource() {
		GeoLocusV2 spline = straight();
		add("vl:x=-0.5");
		add("vr:x=0.5");
		GeoPoint left = only(add("RL=Intersect(S,vl)"), "XL");
		GeoPoint right = only(add("RR=Intersect(S,vr)"), "XR");
		assertEquals(Family.SINGLE_SOURCE_INTERSECTION_OCCURRENCE,
				resolver.resolve(spline, left).getFamily());
		assertEquals(0.25, resolver.resolve(spline, left).getAddress()
				.getCanonicalParameter(), 1E-9);
		assertEquals(1, finite(add("M=LocusLength(S,XL,XR)")), 1E-9);
		GeoNumeric scalar = add("m=Length(S,XL,XR)");
		assertTrue(scalar.isDefined());
		assertEquals(1, scalar.getDouble(), 1E-9);

		add("ci=Circle((0,0),0.5)");
		List<GeoPoint> circle = sortedPoints(add("RC=Intersect(S,ci)"), "PC");
		assertEquals(2, circle.size());
		assertEquals(1, finite(add("MC=LocusLength(S," + circle.get(0).getLabelSimple()
				+ "," + circle.get(1).getLabelSimple() + ")")), 1E-9);
		add("pa:y=x^2-0.25");
		List<GeoPoint> parabola = sortedPoints(add("RP=Intersect(S,pa)"), "PP");
		assertEquals(2, parabola.size());
		assertEquals(1, finite(add("MP=LocusLength(S," + parabola.get(0).getLabelSimple()
				+ "," + parabola.get(1).getLabelSimple() + ")")), 1E-9);
	}

	@Test
	void witnessEllipseCircleRootsAreEndpointsOnTheEllipse() {
		witness();
		List<GeoPoint> roots = sortedPoints(add("g=Intersect(a,c)"), "W");
		assertEquals(4, roots.size());
		GeoPoint first = roots.get(0);
		GeoPoint second = roots.get(1);
		double expected = semanticLength("a", parameter(first), parameter(second));
		assertEquals(expected, finite(add("L=LocusLength(a," + first.getLabelSimple() + ","
				+ second.getLabelSimple() + ")")), 1E-9);
		GeoNumeric scalar = add("l=Length(a," + first.getLabelSimple() + ","
				+ second.getLabelSimple() + ")");
		assertEquals(expected, scalar.getDouble(), 1E-9);
	}

	@Test
	void certifiedGenericPairPointsAreEndpointsOnBothSources() {
		witness();
		GeoLocusIntersectionResult pair = add("e=Intersect(d,a)");
		List<LocusIntersectionSolution2D> roots = eligible(pair);
		assertEquals(2, roots.size());
		GeoPoint x = materialize(pair, "X", roots.get(0));
		assertEquals(Family.PAIR_INTERSECTION_OCCURRENCE,
				resolver.resolve((GeoLocusV2) requireLookup("a"), x).getFamily());
		assertEquals(Family.PAIR_INTERSECTION_OCCURRENCE,
				resolver.resolve((GeoLocusV2) requireLookup("d"), x).getFamily());
		assertTrue(finite(add("MD=LocusLength(d,F,X)")) > 0);
		List<GeoPoint> single = sortedPoints(add("g=Intersect(a,c)"), "W");
		double onEllipse = semanticLength("a", Math.min(parameter(single.get(0)),
				pairParameter(x, "a")), Math.max(parameter(single.get(0)), pairParameter(x, "a")));
		String start = parameter(single.get(0)) < pairParameter(x, "a")
				? single.get(0).getLabelSimple() : "X";
		String end = start.equals("X") ? single.get(0).getLabelSimple() : "X";
		assertEquals(onEllipse, finite(add("MA=LocusLength(a," + start + "," + end + ")")),
				1E-9);
	}

	@Test
	void generatorPointIsAnEndpointAtTheLiveDriverParameter() {
		witness();
		List<GeoPoint> roots = sortedPoints(add("g=Intersect(a,c)"), "W");
		GeoPoint generator = (GeoPoint) requireLookup("E");
		GeoLocusV2 ellipse = (GeoLocusV2) requireLookup("a");
		assertEquals(Family.GENERATOR_OCCURRENCE,
				resolver.resolve(ellipse, generator).getFamily());
		GeoPoint target = roots.get(3);
		setDriver(1.9);
		GeoLocusMetricResult metric = add("LE=LocusLength(a,E," + target.getLabelSimple()
				+ ")");
		String key = resolver.resolve(ellipse, generator).getOccurrenceKey();
		assertEquals(semanticLength("a", driver(), parameter(target)), finite(metric), 1E-9);
		setDriver(1.2);
		assertEquals(semanticLength("a", driver(), parameter(target)), finite(metric), 1E-9);
		assertEquals(key, resolver.resolve(ellipse, generator).getOccurrenceKey());

		// The generator of a scalar locus follows its true coordinate.
		add("s0=0");
		add("Q0=(s0,0)");
		add("Dm={false,{-2,2,true,true}}");
		add("Lg=LocusV2(Q0,s0,Dm)");
		add("Rline=Point(Lg,\"" + BRANCH + "\",1.5)");
		move("s0", -1);
		assertEquals(2.5, finite(add("MG=LocusLength(Lg,Q0,Rline)")), 1E-9);
		move("s0", 0.5);
		assertEquals(1, finite((GeoLocusMetricResult) requireLookup("MG")), 1E-9);
	}

	@Test
	void similarityImagePointsRequireTheSameParameterObjects() {
		witness();
		List<GeoPoint> roots = sortedPoints(add("g=Intersect(a,c)"), "W");
		String first = roots.get(0).getLabelSimple();
		String second = roots.get(1).getLabelSimple();
		double base = finite(add("L0=LocusLength(a," + first + "," + second + ")"));
		add("alpha=30deg");
		add("aR=Rotate(a,alpha,A)");
		add("IR=Rotate(" + first + ",alpha,A)");
		add("JR=Rotate(" + second + ",alpha,A)");
		assertEquals(base, finite(add("LR=LocusLength(aR,IR,JR)")), 1E-9);
		// Durable point identity starts when the point first participates.
		assertUnique(Family.SIMILARITY_IMAGE_OCCURRENCE, "aR", "IR");
		// Images of an explicit semantic point and of the generator compose too.
		add("PS=Point(a,\"" + BRANCH + "\"," + parameter(roots.get(0)) + ")");
		add("PSR=Rotate(PS,alpha,A)");
		assertEquals(base, finite(add("LS=LocusLength(aR,PSR,JR)")), 1E-9);
		add("ER=Rotate(E,alpha,A)");
		assertEquals(finite(add("LE=LocusLength(a,E," + second + ")")),
				finite(add("LER=LocusLength(aR,ER,JR)")), 1E-9);
		assertUnique(Family.SIMILARITY_IMAGE_OCCURRENCE, "aR", "ER");
		add("vt=Vector((0.4,-0.2))");
		add("aT=Translate(a,vt)");
		add("IT=Translate(" + first + ",vt)");
		add("JT=Translate(" + second + ",vt)");
		assertEquals(base, finite(add("LT=LocusLength(aT,IT,JT)")), 1E-9);
		add("k=2");
		add("aD=Dilate(a,k,A)");
		add("ID=Dilate(" + first + ",k,A)");
		add("JD=Dilate(" + second + ",k,A)");
		assertEquals(2 * base, finite(add("LD=LocusLength(aD,ID,JD)")), 1E-7 * base);
		add("my:x=0");
		add("aM=Mirror(a,my)");
		add("IM=Mirror(" + first + ",my)");
		add("JM=Mirror(" + second + ",my)");
		assertEquals(base, finite(add("LM=LocusLength(aM,IM,JM)")), 1E-9);
		assertUnique(Family.SIMILARITY_IMAGE_OCCURRENCE, "aM", "IM");
		// Equal-valued but separate literal parameters are not DAG lineage.
		add("IL=Rotate(" + first + ",30deg,A)");
		assertEquals(Status.NO_ADDRESS, resolver.resolve((GeoLocusV2) requireLookup("aR"),
				(GeoPoint) requireLookup("IL")).getStatus());
		assertInvalid(add("LL=LocusLength(aR,IL,JR)"));
	}

	@Test
	void dependentCopiesInheritMembershipOnly() {
		witness();
		List<GeoPoint> roots = sortedPoints(add("g=Intersect(a,c)"), "W");
		String first = roots.get(0).getLabelSimple();
		String second = roots.get(1).getLabelSimple();
		double base = finite(add("L0=LocusLength(a," + first + "," + second + ")"));
		add("Q=" + first);
		assertEquals(Family.DEPENDENT_COPY_OCCURRENCE, resolver.resolve(
				(GeoLocusV2) requireLookup("a"), (GeoPoint) requireLookup("Q")).getFamily());
		assertEquals(base, finite(add("LQ=LocusLength(a,Q," + second + ")")), 1E-9);
		add("QF=F");
		assertEquals(finite(add("LF=LocusLength(d,F,H)")),
				finite(add("LQF=LocusLength(d,QF,H)")), 1E-12);
		add("free=CopyFreeObject(" + first + ")");
		assertInvalid(add("LC=LocusLength(a,free," + second + ")"));
		// A copy never composes a transported occurrence.
		add("vt=Vector((0.4,-0.2))");
		add("dT=Translate(d,vt)");
		assertTrue(finite(add("LA1=LocusLength(dT,F,H)")) > 0);
		assertInvalid(add("LQT=LocusLength(dT,QF,H)"));
	}

	@Test
	void membershipPointsAreNotTransportedThroughImages() {
		witness();
		List<GeoPoint> roots = sortedPoints(add("g=Intersect(a,c)"), "W");
		String first = roots.get(0).getLabelSimple();
		String second = roots.get(1).getLabelSimple();
		add("alpha=30deg");
		add("aR=Rotate(a,alpha,A)");
		assertInvalid(add("LI=LocusLength(aR," + first + "," + second + ")"));
		add("JR=Rotate(" + second + ",alpha,A)");
		// The generator of a is not transported to its image either.
		assertEquals(Status.NO_ADDRESS, resolver.resolve((GeoLocusV2) requireLookup("aR"),
				(GeoPoint) requireLookup("E")).getStatus());
		assertInvalid(add("LE=LocusLength(aR,E,JR)"));
		// The approved A1 constructor transport is unchanged.
		add("dR=Rotate(d,alpha,A)");
		assertEquals(finite(add("LD=LocusLength(d,F,H)")),
				finite(add("LDR=LocusLength(dR,F,H)")), 1E-9);
	}

	@Test
	void coincidentPointsAndCoincidentRootsAreNeverMerged() {
		witness();
		List<GeoPoint> roots = sortedPoints(add("g=Intersect(a,c)"), "W");
		GeoPoint first = roots.get(0);
		add("K=(" + first.getInhomX() + "," + first.getInhomY() + ")");
		assertEquals(Status.NO_ADDRESS, resolver.resolve((GeoLocusV2) requireLookup("a"),
				(GeoPoint) requireLookup("K")).getStatus());
		assertInvalid(add("LK=LocusLength(a,K," + roots.get(1).getLabelSimple() + ")"));
		// A figure eight passes its centre at two semantic parameters. The coincident
		// roots have no unique current selector and stay fail-closed, never merged.
		add("O8=(0,0)");
		add("N1=(1,1)");
		add("N2=(2,0)");
		add("N3=(1,-1)");
		add("N4=(-1,1)");
		add("N5=(-2,0)");
		add("N6=(-1,-1)");
		add("S8=SplineV2({O8,N1,N2,N3,O8,N4,N5,N6,O8},3)");
		add("hx:y=0");
		GeoLocusIntersectionResult passes = add("R8=Intersect(S8,hx)");
		List<LocusIntersectionSolution2D> centre = passes.getIntersectionResult()
				.getFiniteSolutions().stream().filter(root -> Math.hypot(
						root.getEvaluatedPoint().getX(), root.getEvaluatedPoint().getY()) < 1E-9)
				.toList();
		assertEquals(2, centre.size());
		assertTrue(centre.stream().noneMatch(root -> passes.isPointAdmissible(
				root.getIdentity().getRootToken())));
		List<GeoPoint> lobes = sortedPoints(passes, "Z");
		assertEquals(2, lobes.size());
		// Explicit semantic points at both centre passes coincide and stay distinct.
		add("P80=Point(S8,\"spline-v2/main\",0)");
		add("P81=Point(S8,\"spline-v2/main\",0.5)");
		assertTrue(finite(add("L8=LocusLength(S8,P80,P81)")) > 4);
		double across = finite(add("LZ=LocusLength(S8,Z0,Z1)"));
		assertEquals(across, finite(add("LZ0=LocusLength(S8,Z0,P81)"))
				+ finite(add("LZ1=LocusLength(S8,P81,Z1)")), 1E-8);
	}

	@Test
	void wrongSourceAndDormantEndpointsFailClosed() {
		witness();
		List<GeoPoint> roots = sortedPoints(add("g=Intersect(a,c)"), "W");
		String first = roots.get(0).getLabelSimple();
		String second = roots.get(1).getLabelSimple();
		assertInvalid(add("LW=LocusLength(d," + first + "," + second + ")"));
		GeoLocusMetricResult metric = add("L=LocusLength(a," + first + "," + second + ")");
		final double value = finite(metric);
		final String key = resolver.resolve((GeoLocusV2) requireLookup("a"), roots.get(0))
				.getOccurrenceKey();
		moveTo("A", 40.78, 2.24);
		moveTo("B", 42.56, 4.78);
		assertFalse(roots.get(0).isDefined());
		assertInvalid(metric);
		moveTo("A", 0.78, 2.24);
		moveTo("B", 2.56, 4.78);
		assertTrue(roots.get(0).isDefined());
		assertEquals(value, finite(metric), 1E-9);
		assertEquals(key, resolver.resolve((GeoLocusV2) requireLookup("a"), roots.get(0))
				.getOccurrenceKey());
	}

	@Test
	void familyChangeByRedefinitionIsAFreshResolutionNeverARetarget() {
		straight();
		add("vl:x=-0.5");
		GeoPoint left = only(add("RL=Intersect(S,vl)"), "XL");
		assertEquals(1.5, finite(add("M=LocusLength(S,XL,C)")), 1E-9);
		assertUnique(Family.SINGLE_SOURCE_INTERSECTION_OCCURRENCE, "S", "XL");
		String semantic = "Point(S,\"spline-v2/main\",0.5)";
		// A compatible redefinition across families is rejected atomically; the
		// rolled-back metric resolves the same family again.
		assertFalse(change(left, semantic, false).isEmpty());
		assertUnique(Family.SINGLE_SOURCE_INTERSECTION_OCCURRENCE, "S", "XL");
		assertEquals(1.5, finite((GeoLocusMetricResult) requireLookup("M")), 1E-9);
		// Replacement has replacement identity semantics for the dependent closure,
		// so no metric object survives to be retargeted. Family change inside one
		// metric object is NOT_APPLICABLE_UNDER_CURRENT_LIFECYCLE; the retained
		// family guard stays the fail-closed authority.
		Object before = requireLookup("M").getParentAlgorithm();
		assertEquals("", change(requireLookup("XL"), semantic, true));
		GeoElement current = lookup("M");
		assertTrue(current == null || current.getParentAlgorithm() != before);
	}

	@Test
	void reopenUndoRedoAndCopyRevalidateEveryNewFamily() {
		activateUndo();
		straight();
		add("vl:x=-0.5");
		add("vr:x=0.5");
		only(add("RL=Intersect(S,vl)"), "XL");
		only(add("RR=Intersect(S,vr)"), "XR");
		add("QL=XL");
		add("theta=40deg");
		add("SR=Rotate(S,theta,C)");
		add("XLR=Rotate(XL,theta,C)");
		add("XRR=Rotate(XR,theta,C)");
		getKernel().setContinuous(false);
		add("Ac=(0.78,2.24)");
		add("Bc=(2.56,4.78)");
		add("cc=Circle(Ac,Bc)");
		add("Cc=Point(cc)");
		add("Kc=(1,1)");
		add("Mc=Midpoint(Cc,Kc)");
		add("ring=LocusV2(Mc,Cc)");
		add("Pr=Point(ring,\"" + BRANCH + "\",2)");
		List<String> metrics = List.of("MS=LocusLength(S,XL,XR)", "MQ=LocusLength(S,QL,XR)",
				"MI=LocusLength(SR,XLR,XRR)", "MG=LocusLength(ring,Mc,Pr)");
		List<Double> values = new ArrayList<>();
		List<GeoLocusMetricResult> results = new ArrayList<>();
		for (String definition : metrics) {
			GeoLocusMetricResult result = add(definition);
			results.add(result);
			values.add(finite(result));
		}
		getApp().setXML(getApp().getXML(), true);
		for (int index = 0; index < metrics.size(); index++) {
			String label = metrics.get(index).substring(0, 2);
			assertEquals(values.get(index), finite((GeoLocusMetricResult) requireLookup(label)),
					1E-9, label);
		}
		getApp().storeUndoInfo();
		moveTo("Kc", 1.5, 1.2);
		getApp().storeUndoInfo();
		getKernel().undo();
		assertEquals(values.get(3), finite((GeoLocusMetricResult) requireLookup("MG")), 1E-9);
		getKernel().redo();
		assertTrue(((GeoLocusMetricResult) requireLookup("MG")).getMetricResult()
				.getMetricValue().getFiniteValue().isPresent());
		getKernel().undo();
		for (String label : List.of("MS", "MQ", "MI")) {
			GeoLocusMetricResult result = (GeoLocusMetricResult) requireLookup(label);
			String clipboard = InternalClipboard.getTextToSave(getApp(), List.of(result),
					text -> text);
			int separator = clipboard.indexOf('\n');
			InternalClipboard.pasteGeoGebraXMLInternal(getApp(),
					new ArrayList<>(Arrays.asList(clipboard.substring(0, separator)
							.split(" "))), clipboard.substring(separator));
			PersistentGeoId original = id(result);
			var registry = getConstruction().getSpatialIdentityRegistry();
			GeoIdentityRecord copy = registry.getRecords().stream()
					.filter(GeoIdentityRecord.class::isInstance)
					.map(GeoIdentityRecord.class::cast)
					.filter(record -> original.equals(record.getCopySourceId()))
					.findFirst().orElseThrow();
			GeoLocusMetricResult copied = (GeoLocusMetricResult) registry.getGeo(copy.getId());
			assertEquals(finite(result), finite(copied), 1E-9, label);
		}
	}

	@Test
	void compatibleRedefineFollowsTheGeometry() {
		witness();
		List<GeoPoint> roots = sortedPoints(add("g=Intersect(a,c)"), "W");
		GeoPoint first = roots.get(0);
		GeoPoint second = roots.get(1);
		GeoLocusMetricResult metric = add("L=LocusLength(a," + first.getLabelSimple() + ","
				+ second.getLabelSimple() + ")");
		moveTo("B", 2.7, 4.9);
		assertTrue(first.isDefined() && second.isDefined());
		assertEquals(semanticLength("a", parameter(first), parameter(second)),
				finite(metric), 1E-9);
	}

	@Test
	void userGuideWorkedExampleEndpointsHoldAsDocumented() {
		// User guide sections 7.5 and 9.3, both editions.
		getKernel().setContinuous(false);
		add("h=0");
		add("A=(-2,h)");
		add("B=(-2/3,h)");
		add("C=(2/3,h)");
		add("D=(2,h)");
		add("S=SplineV2({A,B,C,D},3)");
		add("P=Point(S,\"spline-v2/main\",0.25)");
		add("Q=Point(S,\"spline-v2/main\",0.75)");
		assertEquals(4, ((GeoNumeric) add("M=Length(S)")).getDouble(), 1E-9);
		assertEquals(2, ((GeoNumeric) add("MP=Length(S,P,Q)")).getDouble(), 1E-9);
		assertEquals(8.0 / 3, ((GeoNumeric) add("MA=Length(S,A,C)")).getDouble(), 1E-9);
		add("K=(-2,0)");
		assertFalse(((GeoNumeric) add("MK=Length(S,K,Q)")).isDefined());
	}

	@Test
	void ineligibleTransformsFailClosedAndNestedImagesCompose() {
		witness();
		List<GeoPoint> roots = sortedPoints(add("g=Intersect(a,c)"), "W");
		String first = roots.get(0).getLabelSimple();
		String second = roots.get(1).getLabelSimple();
		final double base = finite(add("L0=LocusLength(a," + first + "," + second + ")"));
		add("inverse=Mirror(" + first + ",c)");
		assertInvalid(add("LV=LocusLength(a,inverse," + second + ")"));
		add("alpha=30deg");
		add("aR=Rotate(a,alpha,A)");
		add("aRR=Rotate(aR,alpha,A)");
		add("IR=Rotate(" + first + ",alpha,A)");
		add("JR=Rotate(" + second + ",alpha,A)");
		add("IRR=Rotate(IR,alpha,A)");
		add("JRR=Rotate(JR,alpha,A)");
		assertEquals(base, finite(add("LRR=LocusLength(aRR,IRR,JRR)")), 1E-9);
	}

	private void witness() {
		getKernel().setContinuous(false);
		add("A=(0.78,2.24)");
		add("B=(2.56,4.78)");
		add("c=Circle(A,B)");
		add("C=Point(c)");
		add("f=Line(C,xAxis)");
		add("D=Intersect(f,yAxis)");
		add("E=Midpoint(D,C)");
		add("a=LocusV2(E,C)");
		add("F=(-3,1)");
		add("G=(0.56,3.94)");
		add("H=(6.28,-1.36)");
		add("l1={F,G,H}");
		add("b=3");
		add("d=SplineV2(l1,b)");
	}

	private GeoLocusV2 straight() {
		getKernel().setContinuous(false);
		add("A=(-1,0)");
		add("B=(0,0)");
		add("C=(1,0)");
		return add("S=SplineV2({A,B,C},3)");
	}

	private double semanticLength(String locus, double from, double to) {
		String suffix = Integer.toHexString(System.identityHashCode(new Object()));
		add("PS" + suffix + "=Point(" + locus + ",\"" + BRANCH + "\"," + from + ")");
		add("PT" + suffix + "=Point(" + locus + ",\"" + BRANCH + "\"," + to + ")");
		return finite(add("LS" + suffix + "=LocusLength(" + locus + ",PS" + suffix + ",PT"
				+ suffix + ")"));
	}

	private void setDriver(double parameter) {
		GeoPoint driver = (GeoPoint) requireLookup("C");
		driver.getPathParameter().setT(parameter);
		((GeoConic) requireLookup("c")).pathChanged(driver);
		driver.updateCoords();
		driver.updateCascade();
	}

	private double driver() {
		return ((GeoPoint) requireLookup("C")).getPathParameter().getT();
	}

	private static double parameter(GeoPoint point) {
		GeoLocusIntersectionResult rich = ((org.geocedg.common.kernel.algos
				.AlgoLocusIntersectionPointV2) point.getParentAlgorithm()).getRichInput();
		String token = ((org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2) point
				.getParentAlgorithm()).getEffectiveRootToken();
		return rich.findExactPointAdmissibleSolution(token).orElseThrow()
				.getRevisionEvidence().getSemanticParameter();
	}

	private double pairParameter(GeoPoint point, String locus) {
		GeoLocusIntersectionResult rich = ((org.geocedg.common.kernel.algos
				.AlgoLocusIntersectionPointV2) point.getParentAlgorithm()).getRichInput();
		String token = ((org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2) point
				.getParentAlgorithm()).getEffectiveRootToken();
		var pair = rich.findExactPointAdmissibleSolution(token).orElseThrow()
				.getPairEvidence().orElseThrow();
		String identity = ((GeoLocusV2) requireLookup(locus)).getLocusIdentity();
		return pair.getFirst().getLocusIdentity().equals(identity)
				? pair.getFirst().getSemanticParameter() : pair.getSecond().getSemanticParameter();
	}

	private List<GeoPoint> sortedPoints(GeoLocusIntersectionResult rich, String prefix) {
		List<LocusIntersectionSolution2D> roots = new ArrayList<>(eligible(rich));
		roots.sort(Comparator.comparingDouble(root -> root.getRevisionEvidence()
				.getSemanticParameter()));
		List<GeoPoint> points = new ArrayList<>();
		for (int index = 0; index < roots.size(); index++) {
			points.add(materialize(rich, prefix + index, roots.get(index)));
		}
		return points;
	}

	private GeoPoint only(GeoLocusIntersectionResult rich, String label) {
		List<LocusIntersectionSolution2D> roots = eligible(rich);
		assertEquals(1, roots.size(), "expected exactly one admissible root");
		return materialize(rich, label, roots.get(0));
	}

	private GeoPoint materialize(GeoLocusIntersectionResult rich, String label,
			LocusIntersectionSolution2D root) {
		GeoText token = new GeoText(getConstruction(), root.getIdentity().getRootToken());
		token.setAuxiliaryObject(true);
		token.setEuclidianVisible(false);
		GeoPoint point = LocusV2PublicOperations.selectIntersectionPoint(getConstruction(),
				label, rich, token);
		assertTrue(point.isDefined());
		return point;
	}

	private static List<LocusIntersectionSolution2D> eligible(
			GeoLocusIntersectionResult rich) {
		assertNotNull(rich.getIntersectionResult());
		return rich.getIntersectionResult().getFiniteSolutions().stream()
				.filter(root -> rich.isPointAdmissible(root.getIdentity().getRootToken()))
				.toList();
	}

	private PersistentGeoId id(GeoElement geo) {
		return getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(geo);
	}

	private void move(String label, double value) {
		GeoNumeric number = (GeoNumeric) requireLookup(label);
		number.setValue(value);
		number.updateCascade();
	}

	private void moveTo(String label, double x, double y) {
		GeoPoint point = (GeoPoint) requireLookup(label);
		point.setCoords(x, y, 1);
		point.updateCascade();
	}

	private String change(GeoElement target, String definition, boolean replacement) {
		ErrorAccumulator errors = new ErrorAccumulator();
		AtomicReference<GeoElementND> result = new AtomicReference<>();
		EvalInfo info = new EvalInfo(true, true)
				.withSymbolicMode(AlgebraProcessor.getRedefinitionMode(target, getKernel()))
				.withLabelRedefinitionAllowedFor(target.getLabelSimple())
				.withSymbolic(true).withSliders(true);
		if (replacement) {
			info = info.withSpatialReplacementOperation();
		}
		getKernel().getAlgebraProcessor().changeGeoElementNoExceptionHandling(
				target, definition, info, false, result::set, errors);
		return errors.getErrors();
	}

	private void assertUnique(Family family, String source, String endpoint) {
		SemanticMetricEndpointResolver2D.Resolution resolution = resolver.resolve(
				(GeoLocusV2) requireLookup(source), (GeoPoint) requireLookup(endpoint));
		assertEquals(Status.UNIQUE, resolution.getStatus(), resolution.getDiagnostic());
		assertEquals(family, resolution.getFamily());
	}

	private static void assertInvalid(GeoLocusMetricResult result) {
		assertEquals(MetricComputationStatus.INVALID_QUERY,
				result.getMetricResult().getComputationStatus(),
				result.getMetricResult().getDiagnostics().toString());
	}

	private static double finite(GeoLocusMetricResult result) {
		assertNotNull(result.getMetricResult());
		return result.getMetricResult().getMetricValue().getFiniteValue().orElseThrow(() ->
				new AssertionError(result.getMetricResult().getComputationStatus() + " "
						+ result.getMetricResult().getDiagnostics()));
	}
}
