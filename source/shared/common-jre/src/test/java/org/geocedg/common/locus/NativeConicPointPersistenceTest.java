/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ReconstructionSliceFingerprint2D;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityId;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.kernel.PathNormalizer;
import org.geogebra.common.kernel.PathParameter;
import org.geogebra.common.kernel.PathRegionHandling;
import org.geogebra.common.kernel.algos.AlgoPointOnPath;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.kernelND.GeoConicNDConstants;
import org.geogebra.common.kernel.matrix.Coords;
import org.junit.jupiter.api.Test;

/** First-load native argument/representation oracles, without fixture normalization. */
class NativeConicPointPersistenceTest extends G9U0PublicSurfaceTestBase {

	private static final String STATE_TAG = "conicPointState";
	private static final double FRESH_H_T = -0x1.d7919666c3e74p0;
	private static final double LEGACY_H_T = -0x1.d7919666c3e75p0;

	private record PointState(long t, int pathType, long x, long y, long z,
			long inhomX, long inhomY, long animation, boolean defined) { }

	private static long bits(double value) { return Double.doubleToLongBits(value); }

	private GeoPoint point(String label) {
		return assertInstanceOf(GeoPoint.class, lookup(label));
	}

	private PointState state(String label) {
		GeoPoint point = point(label);
		return new PointState(bits(point.getPathParameter().t), point.getPathParameter().getPathType(),
				bits(point.x), bits(point.y), bits(point.z), bits(point.inhomX), bits(point.inhomY),
				bits(point.getAnimationValue()), point.isDefined());
	}

	private GeoLocusV2 freshH() {
		add("I=(0.4965866445356883,1.7854818601338847)");
		add("J=(-0.5082406249102692,2.5763181370126467)");
		add("f=Circle(I,J)"); add("D=Point(f)"); add("g=Line(D,xAxis)");
		add("K=Intersect(g,yAxis)"); add("M=Midpoint(K,D)");
		GeoLocusV2 h = add("h=LocusV2(M,D)");
		assertEquals(bits(FRESH_H_T), bits(point("D").getPathParameter().t),
				"The unsaved first-load witness must not be normalized before the test");
		return h;
	}

	private void assertRoundTripsExactly(int cycles, String... labels) {
		String original = getApp().getXML();
		List<PointState> originalStates = Arrays.stream(labels).map(this::state).toList();
		for (int cycle = 0; cycle < cycles; cycle++) {
			getApp().setXML(original, true);
			assertEquals(original, getApp().getXML(), "Exact XML at load " + (cycle + 1));
			assertEquals(originalStates, Arrays.stream(labels).map(this::state).toList(),
					"Exact native argument and raw/cached representation at load " + (cycle + 1));
		}
	}

	private static String removeStateTags(String xml) {
		return xml.replaceAll("(?m)^[\\t ]*<" + STATE_TAG + "\\b[^>]*/>\\r?\\n?", "");
	}

	private static String stateTag(String xml) {
		var matcher = java.util.regex.Pattern.compile("<" + STATE_TAG + "\\b[^>]*/>").matcher(xml);
		assertTrue(matcher.find(), "A supported free conic point must carry its native state");
		return matcher.group();
	}

	private static String conicMatrixTag(String xml) {
		var matcher = java.util.regex.Pattern.compile("<matrix\\b[^>]*/>").matcher(xml);
		assertTrue(matcher.find()); return matcher.group();
	}

	private static String elementBlock(String xml, String label) {
		var matcher = java.util.regex.Pattern.compile("(?s)<element\\b(?=[^>]*\\blabel=\""
				+ java.util.regex.Pattern.quote(label) + "\")[^>]*>.*?</element>").matcher(xml);
		assertTrue(matcher.find(), "The actual document element must exist: " + label);
		return matcher.group();
	}

	@Test void freshCircleDriverAndDependentGraphRoundTripExactlyFromFirstSave() {
		freshH();
		assertTrue(point("D").getXML().contains("<" + STATE_TAG));
		assertRoundTripsExactly(6, "D", "I", "J", "K", "M");
	}

	@Test void nativePersistenceStateDoesNotChangeDriverConfigurationButRetainsAnotherFreePlaceInput() {
		GeoConic support = add("support=Circle((0,0),2)");
		GeoPoint driver = add("N=Point(support)");
		driver.updatePathParameter(.1); driver.updateCascade();
		GeoConic movingSupport = add("movingSupport=Circle(N,1)");
		GeoPoint other = add("R=Point(movingSupport)");
		other.updatePathParameter(-.2); other.updateCascade();
		GeoLocusV2 locus = add("L=LocusV2(R,N)");
		List<GeoElement> slice = List.of(support, driver, movingSupport, other);
		var initial = ReconstructionSliceFingerprint2D.snapshot(slice, List.of(driver));
		long revision = locus.getSemanticRevision();
		String driverXml = driver.getXML(), otherXml = other.getXML();
		assertTrue(driverXml.contains("<" + STATE_TAG));
		assertTrue(otherXml.contains("<" + STATE_TAG));
		driver.updatePathParameter(.4); driver.updateCascade();
		assertNotEquals(driverXml, driver.getXML());
		assertNotEquals(otherXml, other.getXML());
		var dragged = ReconstructionSliceFingerprint2D.snapshot(slice, List.of(driver));
		assertEquals(ReconstructionSliceFingerprint2D.Change.NONE, dragged.changeFrom(initial));
		assertEquals(revision, locus.getSemanticRevision());
		other.updatePathParameter(.7); other.updateCascade();
		var otherEdited = ReconstructionSliceFingerprint2D.snapshot(slice, List.of(driver));
		assertEquals(ReconstructionSliceFingerprint2D.Change.VALUES, otherEdited.changeFrom(dragged));
		assertEquals(revision + 1, locus.getSemanticRevision());
	}

	@Test void classicContinuousConicHistoryKeepsItsExistingUntaggedLoadRoute() {
		for (boolean continuousAtSave : new boolean[] {true, false}) {
			var classic = AppCommonFactory.create();
			var kernel = classic.getKernel();
			kernel.setContinuous(true);
			assertTrue(kernel.isContinuous(), "Classic retains its existing continuity setting");
			for (String command : List.of("a=0",
					"c:(x*cos(a)+y*sin(a))^2/4+(-x*sin(a)+y*cos(a))^2=1", "P=Point(c)")) {
				kernel.getAlgebraProcessor().processAlgebraCommand(command, false);
			}
			GeoNumeric angle = assertInstanceOf(GeoNumeric.class, kernel.lookupLabel("a"));
			GeoConic conic = assertInstanceOf(GeoConic.class, kernel.lookupLabel("c"));
			GeoPoint point = assertInstanceOf(GeoPoint.class, kernel.lookupLabel("P"));
			for (int step = 1; step <= 12; step++) {
				angle.setValue(step * Math.PI / 12); angle.updateCascade();
			}
			angle.setValue(0.0); angle.updateCascade();
			point.updatePathParameter(0.0); point.updateCascade();
			kernel.setContinuous(continuousAtSave);
			assertEquals(continuousAtSave, kernel.isContinuous());
			assertEquals(-1, conic.getEigenvec(0).getX(), 0);
			assertEquals(-2, point.inhomX, 0);
			assertFalse(point.hasExactFiniteNativeConicPointState());
			assertFalse(point.getXML().contains("<" + STATE_TAG));
			assertTrue(LocusV2PublicOperations.hasUnsupportedInteractiveHostState(kernel.getConstruction()));
			String xml = classic.getXML();
			assertDoesNotThrow(() -> classic.getXMLio().processXMLString(xml, true, false));
			assertEquals(continuousAtSave, kernel.isContinuous());
			GeoConic rebuilt = assertInstanceOf(GeoConic.class, kernel.lookupLabel("c"));
			GeoPoint restored = assertInstanceOf(GeoPoint.class, kernel.lookupLabel("P"));
			assertEquals(1, rebuilt.getEigenvec(0).getX(), 0);
			assertTrue(restored.isFinite());
			assertEquals(Math.PI, Math.abs(restored.getPathParameter().t), 0,
					"The excluded Classic state follows its historical coordinate reprojection");
			assertFalse(restored.getXML().contains("<" + STATE_TAG));
		}
	}

	@Test void rejectedAtomicMutationRestoresFreshConicDriverAndGraphExactly() {
		GeoLocusV2 h = freshH();
		var sourceId = h.getPersistentLocusId();
		getKernel().setUndoActive(true); getKernel().initUndoInfo();
		String original = getApp().getXML();
		PointState driver = state("D");
		Set<SpatialIdentityId> ids = getConstruction().getSpatialIdentityRegistry().getRecords()
				.stream().map(record -> record.getId()).collect(Collectors.toSet());
		int reservations = getConstruction().getSpatialIdentityRegistry().getReservedIdentityCount();
		int history = getConstruction().getUndoManager().getHistorySize();
		var rejection = new IllegalStateException("native conic mutation rejected");
		assertSame(rejection, assertThrows(IllegalStateException.class,
				() -> getConstruction().runAtomicConstructionMutation(() -> {
					point("I").setCoords(.8, 2.1, 1); point("I").updateCascade();
					add("Transient=(4,5)");
					throw rejection;
				})));
		assertEquals(original, getApp().getXML()); assertEquals(driver, state("D"));
		assertNull(lookup("Transient"));
		var registry = getConstruction().getSpatialIdentityRegistry();
		assertEquals(ids, registry.getRecords().stream().map(record -> record.getId())
				.collect(Collectors.toSet()));
		assertEquals(reservations, registry.getReservedIdentityCount());
		assertEquals(history, getConstruction().getUndoManager().getHistorySize());
		assertTrue(assertInstanceOf(GeoLocusV2.class, registry.getGeo(sourceId)).isDefined());
		assertRoundTripsExactly(2, "D", "I", "J", "K", "M");
	}

	@Test void finiteEllipseParabolaAndNativeParameterEditedHyperbolaBranchesRoundTripExactly() {
		add("ellipse=Ellipse((-.7,.3),(1.2,1.1),2.5)");
		add("parabola=Parabola((.3,1.4),y=-.7)");
		add("hyperbola=Hyperbola((-.7,.3),(1.2,1.1),.6)");
		add("E=Point(ellipse)"); add("P=Point(parabola)");
		add("H=Point(hyperbola)"); add("Other=Point(hyperbola)");
		point("E").setCoords(.8, 2.4, 1); point("E").updateCascade();
		point("P").setCoords(1.9, .8, 1); point("P").updateCascade();
		point("H").setCoords(2.2, 2.4, 1); point("H").updateCascade();
		point("Other").setCoords(-2.2, -1.4, 1); point("Other").updateCascade();
		// Native parameter edits construct the supported forward representation. A separate
		// test preserves the untagged boundary for coordinate-projected hyperbola state.
		point("H").updatePathParameter(point("H").getPathParameter().t);
		point("Other").updatePathParameter(point("Other").getPathParameter().t);
		for (String label : List.of("E", "P", "H", "Other")) {
			assertTrue(point(label).isFinite());
			assertTrue(Double.isFinite(point(label).getPathParameter().t));
			assertTrue(point(label).getXML().contains("<" + STATE_TAG), label);
		}
		assertNotEquals(point("H").getPathParameter().t > 1,
				point("Other").getPathParameter().t > 1, "Both native hyperbola sheets are exercised");
		assertRoundTripsExactly(3, "E", "P", "H", "Other");
	}

	@Test void coordinateProjectedHyperbolaKeepsItsDistinctUntaggedRepresentation() {
		GeoConic conic = add("hyperbola=Hyperbola((-.7,.3),(1.2,1.1),.6)");
		add("H=Point(hyperbola)");
		point("H").setCoords(2.2, 2.4, 1); point("H").updateCascade();
		assertTrue(point("H").isFinite());
		Coords forward = new Coords(3);
		conic.pathChangedWithoutCheck(forward, new PathParameter(point("H").getPathParameter()), true);
		assertTrue(bits(point("H").x) != bits(forward.getX())
				|| bits(point("H").y) != bits(forward.getY()),
				"The native projection witness and its compressed-argument forward state differ in bits");
		assertFalse(point("H").getXML().contains("<" + STATE_TAG));
	}

	@Test void legacyUntaggedConicPointUsesHistoricalCoordinateReprojection() {
		freshH();
		String oldXml = removeStateTags(getApp().getXML());
		assertFalse(oldXml.contains("<" + STATE_TAG));
		getApp().setXML(oldXml, true);
		assertEquals(bits(LEGACY_H_T), bits(point("D").getPathParameter().t));
		assertEquals(bits(0.15395091539221306), bits(point("D").x));
		assertEquals(bits(0.5535319360850215), bits(point("D").y));
		assertNotEquals(bits(FRESH_H_T), bits(point("D").getPathParameter().t));
		assertTrue(((GeoLocusV2) lookup("h")).isDefined());
	}

	@Test void explicitConicParameterRemainsControlledByItsNumericInput() {
		add("c=Circle((.4,-.7),2)"); add("u=.2"); add("D=Point(c,u)");
		assertFalse(point("D").getXML().contains("<" + STATE_TAG));
		getApp().setXML(getApp().getXML(), true);
		GeoNumeric input = assertInstanceOf(GeoNumeric.class, lookup("u"));
		AlgoPointOnPath parent = assertInstanceOf(AlgoPointOnPath.class, point("D").getParentAlgorithm());
		assertEquals(2, parent.getInput().length); assertSame(input, parent.getInput()[1]);
		PointState before = state("D");
		input.setValue(.61); input.updateCascade();
		assertNotEquals(before, state("D"));
		var path = point("D").getPath();
		assertEquals(bits(PathNormalizer.toParentPathParameter(.61,
				path.getMinParameter(), path.getMaxParameter())), bits(point("D").getPathParameter().t));
		assertFalse(point("D").getXML().contains("<" + STATE_TAG));
	}

	@Test void limitedConicPointKeepsHistoricalPersistence() {
		add("arc=Semicircle((-1,0),(1,0))"); add("D=Point(arc)");
		assertTrue(assertInstanceOf(GeoConic.class, lookup("arc")).isLimitedPath());
		assertFalse(point("D").getXML().contains("<" + STATE_TAG));
		getApp().setXML(getApp().getXML(), true);
		assertTrue(point("D").isFinite()); assertSame(lookup("arc"), point("D").getPath());
		assertFalse(point("D").getXML().contains("<" + STATE_TAG));
	}

	@Test void reopenedConicPointContinuesToFollowSourceAndDependents() {
		freshH();
		getApp().setXML(getApp().getXML(), true);
		PointState before = state("D"); double oldX = point("D").inhomX, oldY = point("D").inhomY;
		GeoPoint center = point("I");
		// A translation of both circle inputs preserves the native argument and moves the normal DAG.
		point("J").setCoords(point("J").inhomX + .25, point("J").inhomY - .2, 1);
		center.setCoords(center.inhomX + .25, center.inhomY - .2, 1);
		GeoElement.updateCascade(List.of(center, point("J")));
		assertEquals(before.t(), bits(point("D").getPathParameter().t));
		assertEquals(oldX + .25, point("D").inhomX, 1E-14);
		assertEquals(oldY - .2, point("D").inhomY, 1E-14);
		assertEquals(point("D").inhomX / 2, point("M").inhomX, 1E-14);
		assertEquals(point("D").inhomY, point("M").inhomY, 0);
		assertTrue(((GeoLocusV2) lookup("h")).isDefined());
		assertRoundTripsExactly(2, "D", "K", "M");
	}

	@Test void restoredAnimationOriginAdvancesFromTheSavedPoint() {
		add("c=Circle((1.2,-.7),2)"); add("D=Point(c)");
		point("D").setCoords(3.1, -.2, 1); point("D").updateCascade();
		String original = getApp().getXML(); PointState saved = state("D");
		assertNotNull(point("D").doAnimationStep(60, null));
		PointState expected = state("D"); assertNotEquals(saved, expected);
		getApp().setXML(original, true);
		assertEquals(saved, state("D"), "The fresh saved origin must be restored before animation");
		assertNotNull(point("D").doAnimationStep(60, null));
		assertEquals(expected, state("D"),
				"The same first animation step must produce exactly the same native state after load");
	}

	@Test void sourceUndefinedRoundTripRetainsArgumentThenRecoversThroughTheDag() {
		add("Center=(.4,-.7)"); add("radius=2"); add("c=Circle(Center,radius)");
		add("D=Point(c)"); add("Dependent=D+(1,2)");
		point("D").setCoords(2.1, .4, 1); point("D").updateCascade();
		PointState valid = state("D");
		GeoPoint center = point("Center"); center.setUndefined(); center.updateCascade();
		assertFalse(lookup("c").isDefined(), "This fixture must exercise an actually undefined source");
		assertFalse(point("D").isDefined()); assertFalse(point("Dependent").isDefined());
		assertTrue(Double.isNaN(point("D").x) && Double.isNaN(point("D").y)
				&& Double.isNaN(point("D").z));
		assertTrue(point("D").getXML().contains("<" + STATE_TAG));
		String original = getApp().getXML(); String inactiveMatrix = conicMatrixTag(lookup("c").getXML());
		String pointXml = point("D").getXML();
		PointState undefined = state("D");
		for (int cycle = 0; cycle < 2; cycle++) {
			getApp().setXML(original, true);
			assertFalse(lookup("c").isDefined()); assertFalse(point("Dependent").isDefined());
			assertEquals(undefined, state("D"), "The native undefined point state is preserved exactly");
			assertEquals(pointXml, point("D").getXML(), "The encoded fallback fields are also preserved exactly");
			String restored = getApp().getXML();
			String restoredMatrix = conicMatrixTag(lookup("c").getXML());
			assertNotEquals(original, restored,
					"The historical inactive source-matrix difference remains outside the exact host table");
			assertNotEquals(inactiveMatrix, restoredMatrix);
			assertEquals(original.replace(inactiveMatrix, ""), restored.replace(restoredMatrix, ""),
					"Only the inactive undefined-conic matrix changes; no point state is normalized");
		}
		center = point("Center"); center.setCoords(.4, -.7, 1); center.updateCascade();
		assertTrue(point("D").isFinite()); assertTrue(point("Dependent").isDefined());
		assertEquals(valid.t(), bits(point("D").getPathParameter().t));
		assertEquals(point("D").inhomX + 1, point("Dependent").inhomX, 0);
		assertEquals(point("D").inhomY + 2, point("Dependent").inhomY, 0);
	}

	@Test void nanRadiusWithDefinedConicAndNonfiniteRepresentationKeepsLegacyPersistence() {
		add("Center=(.4,-.7)"); add("radius=2"); add("c=Circle(Center,radius)");
		add("D=Point(c)");
		GeoNumeric radius = assertInstanceOf(GeoNumeric.class, lookup("radius"));
		radius.setUndefined(); radius.updateCascade();
		assertTrue(lookup("c").isDefined(), "The native NaN-radius state is distinct from source undefined");
		assertFalse(point("D").isDefined());
		assertTrue(Double.isNaN(point("D").x) && Double.isNaN(point("D").y));
		assertTrue(Double.isNaN(point("D").z));
		assertFalse(point("D").getXML().contains("<" + STATE_TAG));
	}

	@Test void signedZeroNativeArgumentSurvivesWithoutDefaultParameterSubstitution() {
		add("c=Circle((0,0),1)"); add("D=Point(c)");
		point("D").updatePathParameter(-0.0);
		assertEquals(bits(-0.0), bits(point("D").getPathParameter().t));
		assertTrue(stateTag(point("D").getXML()).contains("t=\"-0.0\""));
		assertRoundTripsExactly(2, "D");
	}

	@Test void recoveredCircleRetainsItsCompatibleDegeneratePathType() {
		add("Center=(.4,-.7)"); add("radius=2"); add("c=Circle(Center,radius)");
		add("D=Point(c)"); point("D").setCoords(2.1, .4, 1); point("D").updateCascade();
		long originalArgument = bits(point("D").getPathParameter().t);
		GeoNumeric radius = assertInstanceOf(GeoNumeric.class, lookup("radius"));
		radius.setValue(0); radius.updateCascade();
		assertEquals(GeoConicNDConstants.CONIC_SINGLE_POINT,
				point("D").getPathParameter().getPathType());
		radius.setValue(2); radius.updateCascade();
		assertTrue(point("D").isFinite());
		assertEquals(GeoConicNDConstants.CONIC_CIRCLE,
				assertInstanceOf(GeoConic.class, lookup("c")).getType());
		assertEquals(GeoConicNDConstants.CONIC_SINGLE_POINT,
				point("D").getPathParameter().getPathType());
		assertEquals(originalArgument, bits(point("D").getPathParameter().t));
		assertTrue(point("D").getXML().contains("<" + STATE_TAG));
		assertRoundTripsExactly(3, "D");
	}

	@Test void nonunitHomogeneousRepresentationKeepsTheHistoricalUntaggedRoute() {
		add("c=Circle((0,0),1)"); add("D=Point(c)");
		point("D").setCoords(new Coords(2, 0, 2), false);
		assertEquals(bits(2), bits(point("D").z)); assertTrue(point("D").isFinite());
		assertFalse(point("D").getXML().contains("<" + STATE_TAG));
		getApp().setXML(getApp().getXML(), true);
		assertTrue(point("D").isFinite()); assertSame(lookup("c"), point("D").getPath());
	}

	@Test void noncanonicalSignedZeroCoordinatesDoNotAcquireForwardCoherentState() {
		add("c=Circle((0,0),1)"); add("D=Point(c)");
		point("D").updatePathParameter(-0.0);
		assertEquals(bits(0.0), bits(point("D").y), "The native forward representation has +0 y");
		point("D").setCoords(new Coords(1, -0.0, 1), false);
		assertEquals(bits(-0.0), bits(point("D").y));
		assertFalse(point("D").getXML().contains("<" + STATE_TAG),
				"A geometrically equal but bitwise noncanonical representation retains the legacy route");
	}

	@Test void pathHandlingOffStillPreservesTheFirstLoadAndReleasesFutureComputation() {
		freshH();
		getKernel().setUsePathAndRegionParameters(PathRegionHandling.OFF);
		assertRoundTripsExactly(2, "D", "K", "M");
		GeoPoint driver = point("D"); PointState before = state("D");
		driver.setCoords(.8, 2.4, 1); driver.updateCascade();
		assertNotEquals(before, state("D")); assertTrue(driver.isFinite());
		assertEquals(driver.inhomX / 2, point("M").inhomX, 1E-14);
	}

	@Test void malformedOrInapplicableTaggedStateIsRejectedInsteadOfUsingLegacyCoordinates() {
		freshH();
		String original = getApp().getXML(); String tag = stateTag(original);
		List<String> malformed = List.of(
				tag.replace("version=\"1\"", "version=\"99\""),
				tag.replaceFirst(" t=\"[^\"]*\"", " t=\"Infinity\""),
				tag.replaceFirst(" x=\"[^\"]*\"", " x=\"100.0\""),
				tag.replaceFirst(" tempPathType=\"[^\"]*\"", ""),
				tag + tag);
		for (String invalid : malformed) {
			assertNotEquals(tag, invalid);
			assertThrows(IllegalArgumentException.class,
					() -> getApp().getXMLio().processXMLString(original.replace(tag, invalid), true, false));
			getApp().setXML(original, true);
			assertEquals(original, getApp().getXML());
		}
		String driver = elementBlock(original, "D");
		var coords = java.util.regex.Pattern.compile("<coords\\b[^>]*/>").matcher(driver);
		assertTrue(coords.find()); String coordinateTag = coords.group();
		List<String> incompatibleElements = List.of(
				driver.replace(tag, "").replace(coordinateTag, coordinateTag + tag),
				driver.replace(coordinateTag, ""),
				driver.replace(tag, tag + "<curveParam t=\"0.5\"/>"),
				driver.replace(coordinateTag, coordinateTag.replaceFirst("x=\"[^\"]*\"", "x=\"100.0\"")));
		for (String invalid : incompatibleElements) {
			assertNotEquals(driver, invalid);
			assertThrows(IllegalArgumentException.class,
					() -> getApp().getXMLio().processXMLString(original.replace(driver, invalid), true, false));
			getApp().setXML(original, true);
			assertEquals(original, getApp().getXML());
		}
		add("u=.2"); add("Explicit=Point(f,u)");
		String explicitDocument = getApp().getXML(); String explicit = elementBlock(explicitDocument, "Explicit");
		assertFalse(explicit.contains("<" + STATE_TAG)); assertTrue(explicitDocument.contains(explicit));
		String injected = explicit.replace("<coords", tag + "<coords");
		assertNotEquals(explicit, injected);
		assertThrows(IllegalArgumentException.class, () -> getApp().getXMLio().processXMLString(
				explicitDocument.replace(explicit, injected), true, false));
	}
}
