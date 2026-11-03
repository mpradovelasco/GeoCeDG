/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.NativeTransactionalState2D;
import org.geocedg.common.kernel.locus.interaction.IndividualForwardPointCreation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.PathParameter;
import org.geogebra.common.kernel.View;
import org.geogebra.common.kernel.algos.AlgoDependentNumber;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.MyDouble;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class R3NativeStateRecoveryTest extends G9U0PublicSurfaceTestBase {

	private record State(long t, int type, long x, long y, long z, long ix, long iy,
			long animation, boolean hasFallback, long fallback, int fallbackType) { }
	private record Capture(NativeTransactionalState2D state, String xml, String key) { }

	private static long bits(double value) { return Double.doubleToLongBits(value); }
	private GeoPoint point(String label) { return (GeoPoint) lookup(label); }
	private static Field field(String name) throws Exception {
		Field field = Construction.class.getDeclaredField(name); field.setAccessible(true); return field;
	}
	private State state(String label) throws Exception {
		GeoPoint point = point(label);
		Field field = GeoPoint.class.getDeclaredField("tempPathParameter"); field.setAccessible(true);
		PathParameter fallback = (PathParameter) field.get(point);
		return new State(bits(point.getPathParameter().t), point.getPathParameter().getPathType(),
				bits(point.x), bits(point.y), bits(point.z), bits(point.inhomX), bits(point.inhomY),
				bits(point.getAnimationValue()), fallback != null,
				fallback == null ? 0 : bits(fallback.t), fallback == null ? 0 : fallback.getPathType());
	}

	private void circle() {
		add("A=(.4965866445356883,1.7854818601338847)");
		add("B=(-.5082406249102692,2.5763181370126467)");
		add("c=Circle(A,B)"); add("P=Point(c)");
	}

	private void hyperbolaAncestor() {
		add("h=Hyperbola((-.7,.3),(1.2,1.1),.6)");
		GeoPoint ancestor = add("H=Point(h)");
		ancestor.setCoords(2.2, 2.4, 1); ancestor.updateCascade();
		add("c=Circle(H,1)"); GeoPoint point = add("P=Point(c)");
		point.updatePathParameter(.73123); point.updateCascade();
	}

	@Test void projectedHyperbolaAncestorKeepsOwnSaveReadableWithoutFalseExactDescendantTag()
			throws Exception {
		hyperbolaAncestor();
		assertFalse(point("H").getXML().contains("<conicPointState"));
		assertFalse(point("P").getXML().contains("<conicPointState"));
		String xml = getApp().getXML();
		assertDoesNotThrow(() -> getApp().getXMLio().processXMLString(xml, true, false));
		assertTrue(point("P").isFinite());
	}

	@ParameterizedTest @ValueSource(booleans = {false, true})
	void rawAngularAncestorNeverTurnsReadableLegacyPayloadIntoAnUnreadableNewFile(boolean rotate)
			throws Exception {
		MyDouble literal = new MyDouble(getKernel(), .4097); literal.setAngle();
		new AlgoDependentNumber(getConstruction(), new ExpressionNode(getKernel(), literal), true)
				.getNumber().toGeoElement().setLabel("ang");
		if (rotate) {
			add("e=Ellipse((-.7,.3),(1.2,1.1),2.5)");
			add("c=Rotate(e,ang,(.31,-.27))");
		} else { add("c=Circle((0,0),ang)"); }
		add("P=Point(c)"); point("P").updatePathParameter(.73123); point("P").updateCascade();
		assertFalse(point("P").getXML().contains("<conicPointState"));
		String xml = getApp().getXML();
		assertDoesNotThrow(() -> getApp().getXMLio().processXMLString(xml, true, false));
		assertTrue(point("P").isFinite());
	}

	@Test void independentColdNativeCircleFrameKeepsAnExactFirstSaveWitness() throws Exception {
		add("c: x^2+y^2=1"); add("P=Point(c)");
		point("P").updatePathParameter(.73123); point("P").updateCascade();
		assertTrue(point("P").getXML().contains("<conicPointState"));
		String xml = getApp().getXML(); State before = state("P");
		for (int cycle = 0; cycle < 6; cycle++) {
			getApp().getXMLio().processXMLString(xml, true, false);
			assertEquals(xml, getApp().getXML()); assertEquals(before, state("P"));
		}
	}

	@Test void aFreshFilePassReclassifiesAnEditedAncestor() {
		add("r=1"); add("c=Circle((0,0),r)"); add("P=Point(c)");
		assertTrue(getApp().getXML().contains("<conicPointState"));
		add("r=0.4097°");
		assertTrue(point("P").isFinite());
		assertFalse(getApp().getXML().contains("<conicPointState"));
	}

	@ParameterizedTest @ValueSource(strings = {"xAxis", "Line((0,0),(1,1))", "Ray((0,0),(1,1))",
			"Segment((0,0),(2,1))", "Circle((.4,-.7),2)", "CircularArc((0,0),(1,0),(0,1))"})
	void ordinaryNativePlaceWithoutDurableIdHasExactTypedRollback(String expression) throws Exception {
		if (!"xAxis".equals(expression)) { add("support=" + expression); }
		add("P=Point(" + ("xAxis".equals(expression) ? "xAxis" : "support") + ")");
		add("Dependent=P+(1,2)");
		point("P").setCoords(.74, .61, 1); point("P").updateCascade();
		assertNull(getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point("P")));
		State originalNative = state("P");
		add("driver=Circle((0,0),1)"); add("N=Point(driver)"); add("v=Vector((0,0),N)");
		add("Traced=N+2v"); GeoLocusV2 locus = add("L=LocusV2(Traced,N)");
		assertFalse(LocusV2PublicOperations.hasUnsupportedInteractiveHostState(getConstruction()),
				() -> String.valueOf(LocusV2PublicOperations.getUnsupportedInteractiveHostStateReason(getConstruction())));
		try (var selection = IndividualForwardPointCreation2D.query(locus, 3, 0,
				LocusPointInteractionPolicy2D.initial(.1))) {
			assertFalse(selection.getChoices().isEmpty(), selection.getFailure().name());
			assertTrue(selection.commit("Created", selection.getChoices().get(0)).isDefined());
		}
		assertEquals(originalNative, state("P"));
		assertNull(getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point("P")));
		var ids = getConstruction().getSpatialIdentityRegistry().getRecords().stream()
				.map(record -> record.getId()).toList();
		State before = state("P"); String xml = getApp().getXML();
		var failure = new IllegalStateException("reject native mutation");
		assertSame(failure, assertThrows(IllegalStateException.class,
				() -> getConstruction().runAtomicConstructionMutation(() -> {
					point("P").setCoords(1.4, .8, 1); point("P").updateCascade();
					add("Transient=(8,9)"); throw failure;
				})));
		assertEquals(xml, getApp().getXML()); assertEquals(before, state("P"));
		assertNull(lookup("Transient"));
		assertEquals(ids, getConstruction().getSpatialIdentityRegistry().getRecords().stream()
				.map(record -> record.getId()).toList());
		var metrics = getConstruction().getLastNativeTransactionMetrics();
		assertEquals(2, metrics.capturedPoints); assertEquals(1, metrics.privateBindings);
		assertTrue(metrics.dagCalls > 0); assertTrue(metrics.xmlRestoreInclusiveNanos > 0);
	}

	@Test void mixedDurableAndPrivateAncestorInputsRestoreExactlyThroughTheNormalDag() throws Exception {
		hyperbolaAncestor(); add("N=Point(c)"); add("v=Vector((0,0),N)");
		add("Traced=N+2v"); GeoLocusV2 locus = add("L=LocusV2(Traced,N)");
		add("Other=Point(Segment((7,2),(9,3)))");
		add("Eligible=Point(Circle((10,10),2))");
		assertTrue(point("Eligible").getXML().contains("<conicPointState"));
		var locusId = locus.getPersistentLocusId();
		var ids = getConstruction().getSpatialIdentityRegistry().getRecords().stream()
				.map(record -> record.getId()).toList();
		List<State> before = List.of(state("H"), state("P"), state("N"), state("Other"), state("Eligible"));
		String xml = getApp().getXML(); var failure = new IllegalStateException("mixed rejection");
		assertSame(failure, assertThrows(IllegalStateException.class,
				() -> getConstruction().runAtomicConstructionMutation(() -> {
					point("H").setCoords(3.1, 1.4, 1); point("H").updateCascade();
					point("Other").setCoords(8.4, 2.8, 1); point("Other").updateCascade(); throw failure;
				})));
		assertEquals(before, List.of(state("H"), state("P"), state("N"), state("Other"), state("Eligible")));
		assertEquals(xml, getApp().getXML());
		assertEquals(ids, getConstruction().getSpatialIdentityRegistry().getRecords().stream()
				.map(record -> record.getId()).toList());
		assertSame(lookup("L"), getConstruction().getSpatialIdentityRegistry().getGeo(locusId));
		assertTrue(getConstruction().getLastNativeTransactionMetrics().privateBindings > 0);
	}

	@Test void publicationCallbacksAndMacroCachesNeverObservePrivateBindings() throws Exception {
		circle(); View view = mock(View.class); AtomicBoolean entered = new AtomicBoolean();
		List<String> observed = new ArrayList<>();
		doAnswer(invocation -> {
			GeoElement output = invocation.getArgument(0);
			if ("Published".equals(output.getLabelSimple()) && entered.compareAndSet(false, true)) {
				observed.add(getApp().getXML());
				Macro macro = new Macro(getKernel(), "R3PrivateBinding",
						new GeoElement[] {lookup("A"), lookup("B")}, new GeoElement[] {point("P")});
				XMLStringBuilder builder = new XMLStringBuilder(); macro.getXML(builder);
				observed.add(builder.toString());
			}
			return null;
		}).when(view).add(any(GeoElement.class));
		getKernel().attach(view);
		try {
			getConstruction().runAtomicConstructionMutation(() -> add("Published=(8,9)"));
		} finally { getKernel().detach(view); }
		assertTrue(entered.get()); assertEquals(2, observed.size());
		observed.forEach(xml -> assertFalse(xml.contains(NativeTransactionalState2D.BINDING_TAG)));
		assertFalse(getApp().getXML().contains(NativeTransactionalState2D.BINDING_TAG));
	}

	private Capture capture() throws Exception {
		NativeTransactionalState2D snapshot = NativeTransactionalState2D.capture(getConstruction());
		String xml;
		field("nativeRollbackCapture").set(getConstruction(), snapshot);
		try { xml = getConstruction().getCurrentUndoXML(false).toString(); snapshot.completeCapture(xml, 0); }
		finally { field("nativeRollbackCapture").set(getConstruction(), null); }
		var matcher = Pattern.compile("<nativeRollbackBinding version=\"1\" key=\"([0-9a-f]{32})\"/>").matcher(xml);
		assertTrue(matcher.find()); return new Capture(snapshot, xml, matcher.group(1));
	}

	private void restore(Capture captured, String xml) throws Throwable {
		var method = Construction.class.getDeclaredMethod("restoreSpatialRedefineSnapshot", String.class);
		method.setAccessible(true); field("nativeRollbackRestore").set(getConstruction(), captured.state);
		try { method.invoke(getConstruction(), xml); }
		catch (InvocationTargetException failure) { throw failure.getCause(); }
		finally { field("nativeRollbackRestore").set(getConstruction(), null); }
	}

	@Test void foreignAndMissingPrivateKeysFailClosed() throws Exception {
		circle(); Capture captured = capture();
		String foreign = captured.xml.replace(captured.key, "00000000000000000000000000000000");
		assertThrows(IllegalStateException.class, () -> restore(captured, foreign));
		assertNull(field("nativeRollbackRestore").get(getConstruction()));
		getApp().getXMLio().processXMLString(captured.xml.replaceAll(
				"\\t<nativeRollbackBinding[^>]*/>\\n", ""), true, false);
		Capture missing = capture();
		assertThrows(IllegalStateException.class, () -> restore(missing,
				missing.xml.replaceAll("\\t<nativeRollbackBinding[^>]*/>\\n", "")));
	}

	@Test void duplicateAndReplayedPrivateKeysFailClosed() throws Throwable {
		circle(); Capture captured = capture();
		String leaf = "\t<nativeRollbackBinding version=\"1\" key=\"" + captured.key + "\"/>\n";
		assertThrows(IllegalStateException.class, () -> restore(captured, captured.xml.replace(leaf, leaf + leaf)));
		getApp().getXMLio().processXMLString(captured.xml.replace(leaf, ""), true, false);
		Capture valid = capture(); restore(valid, valid.xml);
		assertThrows(IllegalStateException.class, () -> restore(valid, valid.xml));
		assertThrows(Exception.class, () -> getApp().getXMLio().processXMLString(valid.xml, true, false));
		assertFalse(getApp().getXML().contains(NativeTransactionalState2D.BINDING_TAG));
	}

	@Test void captureDoesNotMaterializeAbsentFallbackOrAllocateRegistryIds() throws Exception {
		circle(); Field fallback = GeoPoint.class.getDeclaredField("tempPathParameter");
		fallback.setAccessible(true); fallback.set(point("P"), null);
		int records = getConstruction().getSpatialIdentityRegistry().size();
		NativeTransactionalState2D.capture(getConstruction());
		assertNull(fallback.get(point("P")));
		assertEquals(records, getConstruction().getSpatialIdentityRegistry().size());
		String xml = getApp().getXML();
		assertTrue(xml.contains("<conicPointState"));
		var failure = new IllegalStateException("absent fallback");
		assertSame(failure, assertThrows(IllegalStateException.class,
				() -> getConstruction().runAtomicConstructionMutation(() -> { throw failure; })));
		assertNull(fallback.get(point("P")));
		assertEquals(xml, getApp().getXML());
	}

	@Test void aDurablePointStillNeedsAnEarlySerializerSlotBeforeMutation() {
		circle(); add("g=Line(P,xAxis)"); add("K=Intersect(g,yAxis)");
		add("M=Midpoint(K,P)"); add("L=LocusV2(M,P)");
		GeoPoint point = point("P");
		assertNotNull(getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point));
		int records = getConstruction().getSpatialIdentityRegistry().size();
		point.setLabelSet(false);
		try {
			assertEquals(NativeTransactionalState2D.UnsupportedReason.NO_PRIVATE_SERIALIZATION_SLOT,
					LocusV2PublicOperations.getUnsupportedInteractiveHostStateReason(getConstruction()));
			NativeTransactionalState2D.capture(getConstruction());
			assertEquals(records, getConstruction().getSpatialIdentityRegistry().size());
		} finally { point.setLabelSet(true); }
	}

	@Test void classicAtomicBoundaryKeepsItsHistoricalRouteWithoutNativeBindings() {
		var classic = AppCommonFactory.create();
		classic.getKernel().getAlgebraProcessor().processAlgebraCommand("c=Circle((0,0),1)", false);
		classic.getKernel().getAlgebraProcessor().processAlgebraCommand("P=Point(c)", false);
		var failure = new IllegalStateException("classic rejection");
		assertSame(failure, assertThrows(IllegalStateException.class,
				() -> classic.getKernel().getConstruction().runAtomicConstructionMutation(() -> { throw failure; })));
		assertNull(classic.getKernel().getConstruction().getLastNativeTransactionMetrics());
		assertFalse(classic.getXML().contains("<conicPointState"));
		assertFalse(classic.getXML().contains(NativeTransactionalState2D.BINDING_TAG));
	}
}
