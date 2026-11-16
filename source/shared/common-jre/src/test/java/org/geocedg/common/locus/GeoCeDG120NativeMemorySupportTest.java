/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.geocedg.common.document.controls.ControlLayout;
import org.geocedg.common.document.controls.ControlLayoutXml;
import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.NativeSourceFidelity2D;
import org.geocedg.common.kernel.locus.NativeTransactionalState2D;
import org.geogebra.common.kernel.View;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.algos.AlgoDependentNumber;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.MinusOne;
import org.geogebra.common.kernel.arithmetic.MyDouble;
import org.geogebra.common.kernel.geos.GeoAngle;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoPoint.NativeRollbackPointState;
import org.geogebra.common.kernel.geos.AbsoluteScreenLocateable;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Exact reusable native MEMORY support; the independent FILE perimeter stays fixed. */
class GeoCeDG120NativeMemorySupportTest extends G9U0PublicSurfaceTestBase {
	private static final List<String> INPUTS = List.of("B", "C", "H", "A2", "N", "E1");

	private GeoPoint point(String label) { return (GeoPoint) lookup(label); }
	private AlgoIntersectLineConic selector() {
		return (AlgoIntersectLineConic) point("J").getParentAlgorithm();
	}

	private void nativePair(String first, String second, String line, String circle) {
		// A single algebra label on a two-output command names the output family.
		// Bind both native outputs explicitly, as the saved cone does.
		getKernel().getAlgoDispatcher().intersectLineConic(new String[] {first, second},
				(GeoLine) requireLookup(line), (GeoConic) requireLookup(circle));
		assertNotNull(point(first));
		assertNotNull(point(second));
	}

	private void nativeSources() {
		add("A=(0,0)"); add("g=OrthogonalLine(A,xAxis)"); add("h=OrthogonalLine(A,g)");
		add("B=Point(g)"); add("C=Point(h)");
		// Construct the nondegenerate rotated path from coherent source geometry.
		point("C").setCoords(3, 0, 1); point("C").updateCascade();
		point("B").setCoords(0, 2, 1); point("B").updateCascade();
		// This named native angle models a saved slider, not a lossy angular literal.
		GeoAngle angle = new GeoAngle(getConstruction(), .8);
		angle.setAngleStyle(GeoAngle.AngleStyle.UNBOUNDED); angle.setLabel("alpha");
		add("Cp=Rotate(C,alpha,A)"); add("rotatedSource=Line(A,Cp)");
		add("H=Point(rotatedSource)");
		add("radius=4"); add("p=Circle(B,radius)"); add("parallelSource=Line(B,h)");
		nativePair("J", "K", "parallelSource", "p");
		add("l=Line(J,g)"); add("R=Intersect(l,h)");
		add("Rp=Rotate(R,-alpha,A)"); add("b=Line(A,Rp)"); add("A2=Point(b)");
		add("u=UnitVector(g)"); add("eccentricity=2"); add("Cc=B+u*eccentricity");
		add("g1=Circle(Cc,2)"); add("N=Point(g1)"); add("E1=Point(p)");
		// Ordinary input motion settles the native selector and exercises both DAG chains.
		point("C").setCoords(4, 0, 1); point("C").updateCascade();
		point("B").setCoords(0, 3, 1); point("B").updateCascade();
		point("C").setCoords(3, 0, 1); point("C").updateCascade();
		point("B").setCoords(0, 2, 1); point("B").updateCascade();
		assertNotNull(selector().captureNativeRollbackSelectionState());
	}

	@Test void allSixNativePlaceClosuresArePositiveOnlyInMemory() {
		nativeSources();
		for (String label : INPUTS) {
			GeoPoint point = point(label);
			assertNotNull(point.captureNativeRollbackState(), label);
			assertTrue(NativeSourceFidelity2D.isRecoverableSource(point.getPath().toGeoElement()), label);
			assertEquals(NativeSourceFidelity2D.Outcome.UNSUPPORTED,
					NativeSourceFidelity2D.fileSerializationPass().assess(point.getPath().toGeoElement()).outcome(),
					label + ": a MEMORY witness cannot promote the FILE table");
			assertFalse(point.getXML().contains("<conicPointState"), label);
		}
		assertNull(NativeTransactionalState2D.unsupportedReason(getConstruction()));
		assertFalse(getApp().getXML().contains(NativeTransactionalState2D.BINDING_TAG));
	}

	@Test void parserNegativeNamedAngleKeepsItsAnonymousMemoryRecipeAndExactRollback() {
		add("A=(0,0)"); add("Seed=(2,1)");
		GeoAngle angle = new GeoAngle(getConstruction(), .8726646259971648);
		angle.setAngleStyle(GeoAngle.AngleStyle.UNBOUNDED); angle.setLabel("alpha");
		add("Rotated=Rotate(Seed,-alpha,A)");
		GeoAngle negative = java.util.Arrays.stream(point("Rotated").getParentAlgorithm().getInput())
				.filter(input -> input.getClass() == GeoAngle.class && !input.isLabelSet())
				.map(input -> (GeoAngle) input).findFirst().orElseThrow();
		assertEquals(GeoAngle.AngleStyle.UNBOUNDED, negative.getAngleStyle());
		assertEquals(Double.doubleToLongBits(-angle.getValue()),
				Double.doubleToLongBits(negative.getRawAngle()));
		assertSame(MinusOne.class, negative.getDefinition().getChild(0).getClass());
		assertTrue(NativeSourceFidelity2D.isRecoverableSource(negative));
		assertEquals(NativeSourceFidelity2D.Outcome.UNSUPPORTED,
				NativeSourceFidelity2D.fileSerializationPass().assess(negative).outcome());
		add("route=Line(A,Rotated)"); add("NativePlace=Point(route)");
		long sourceZero = Double.doubleToLongBits(((GeoLine) requireLookup("route")).z);
		assertEquals(Double.doubleToLongBits(-0.0), sourceZero,
				"The source's computed negative zero must survive exact MEMORY restoration");
		assertTrue(NativeSourceFidelity2D.isRecoverableSource(requireLookup("route")));
		assertNull(NativeTransactionalState2D.unsupportedReason(getConstruction()));
		var place = point("NativePlace").captureNativeRollbackState();
		assertNotNull(place);
		String xml = getApp().getXML();
		var rejected = new IllegalStateException("negative named-angle rollback");
		assertSame(rejected, assertThrows(IllegalStateException.class,
				() -> getConstruction().runAtomicConstructionMutation(() -> {
					point("Seed").setCoords(3, 2, 1); point("Seed").updateCascade();
					throw rejected;
				})));
		assertEquals(xml, getApp().getXML());
		assertTrue(place.matches(point("NativePlace")));
		assertEquals(sourceZero, Double.doubleToLongBits(((GeoLine) requireLookup("route")).z));
		assertEquals(1, getConstruction().getLastNativeTransactionMetrics().dagCalls);
		assertFalse(getConstruction().isNativeRollbackStateAuthorized(point("NativePlace"), place));
		assertFalse(point("NativePlace").restorePendingNativeRollbackState(),
				"The one-use point payload must not survive the restore session");
	}

	@Test void memoryMinusOnePermissionDoesNotIncludeMutationsAnglesOrSubclasses() {
		MyDouble exact = new MinusOne(getKernel());
		var accepted = new AlgoDependentNumber(getConstruction(),
				new ExpressionNode(getKernel(), exact), false).getNumber().toGeoElement();
		assertTrue(NativeSourceFidelity2D.isRecoverableSource(accepted));
		assertEquals(NativeSourceFidelity2D.Outcome.UNSUPPORTED,
				NativeSourceFidelity2D.fileSerializationPass().assess(accepted).outcome());
		MyDouble altered = new MinusOne(getKernel()); altered.set(-2);
		MyDouble signedZero = new MinusOne(getKernel()); signedZero.set(-0.0);
		MyDouble angular = new MinusOne(getKernel()); angular.setAngle();
		MyDouble subclass = new MinusOne(getKernel()) { };
		for (MyDouble rejected : List.of(altered, signedZero, angular, subclass)) {
			var output = new AlgoDependentNumber(getConstruction(),
					new ExpressionNode(getKernel(), rejected), false).getNumber().toGeoElement();
			assertFalse(NativeSourceFidelity2D.isRecoverableSource(output));
			assertEquals(NativeSourceFidelity2D.Outcome.UNSUPPORTED,
					NativeSourceFidelity2D.fileSerializationPass().assess(output).outcome());
		}
	}

	private void controlLayoutAndMixedMetricHistory() {
		var locus = createPeriodicCircle();
		var legacy = LocusV2PublicOperations.scalarLength(getConstruction(), "LegacyLength", locus);
		var rich = AlgoLocusMetricScalarAdapter.richResultOf(legacy);
		assertNotNull(rich);
		assertTrue(rich.rename("LegacyMetric"));
		rich.setAuxiliaryObject(false);
		assertInstanceOf(GeoNumeric.class, add("PublicLength=LocusLength(L)"));
		GeoElement control = add("LayoutControl=Button()");
		AbsoluteScreenLocateable location = (AbsoluteScreenLocateable) control;
		location.setAbsoluteScreenLocActive(true);
		location.setAbsoluteScreenLoc(10, 10);
		control.setEuclidianVisible(true);
		control.update();
		var layout = getConstruction().getControlLayout();
		var panel = layout.addPanel(ControlLayout.Anchor.FIXED, 0, 0, 160, 100, true);
		layout.addSlot(panel.id(), control, 4, 6);
		assertTrue(assertDoesNotThrow(() -> ControlLayoutXml.inspect(
						getConstruction().getCurrentUndoXML(false).toString())),
				"The fixture must exercise the native Control Layout snapshot reader");
		assertNull(NativeTransactionalState2D.unsupportedReason(getConstruction()));
	}

	@ParameterizedTest @ValueSource(booleans = {false, true})
	void controlLayoutRollbackKeepsHistoricalAndNumericMetricContracts(boolean entryLoading) {
		controlLayoutAndMixedMetricHistory();
		String xml = getApp().getXML();
		var layout = getConstruction().getControlLayout();
		String panelId = layout.panels().get(0).id();
		String slotId = layout.slot(lookup("LayoutControl")).id();
		var rejected = new IllegalStateException("mixed metric history rollback");
		getConstruction().setFileLoading(entryLoading);
		try {
			assertSame(rejected, assertThrows(IllegalStateException.class,
					() -> getConstruction().runAtomicConstructionMutation(() -> {
						assertEquals(entryLoading, getConstruction().isFileLoading());
						add("Transient=(7,8)");
						throw rejected;
					})));
			assertEquals(entryLoading, getConstruction().isFileLoading());
			var rich = assertInstanceOf(GeoLocusMetricResult.class, lookup("LegacyMetric"));
			var oldChild = assertInstanceOf(GeoNumeric.class, lookup("LegacyLength"));
			assertSame(rich, AlgoLocusMetricScalarAdapter.richResultOf(oldChild));
			var current = assertInstanceOf(GeoNumeric.class, lookup("PublicLength"));
			assertNotSame(rich, AlgoLocusMetricScalarAdapter.richResultOf(current));
			assertEquals(2 * Math.PI, oldChild.getDouble(), 1e-9);
			assertEquals(oldChild.getDouble(), current.getDouble(), 1e-9);
			assertEquals(panelId, layout.panels().get(0).id());
			assertEquals(slotId, layout.slot(lookup("LayoutControl")).id());
			assertEquals(xml, getApp().getXML());
			assertNull(lookup("Transient"));
			assertEquals(1, getConstruction().getLastNativeTransactionMetrics().dagCalls);
			getConstruction().runAtomicConstructionMutation(() -> {
				assertEquals(entryLoading, getConstruction().isFileLoading());
				add("Completed=(3,4)");
			});
			assertEquals(entryLoading, getConstruction().isFileLoading());
			assertNotNull(lookup("Completed"));
		} finally {
			getConstruction().setFileLoading(false);
		}
	}

	@ParameterizedTest @ValueSource(booleans = {false, true})
	void controlLayoutReaderFailureStillRestoresTheEntryLoadingFlag(boolean entryLoading) {
		controlLayoutAndMixedMetricHistory();
		var mutationFailure = new IllegalStateException("injected mutation failure");
		var readerFailure = new IllegalStateException("injected snapshot reader failure");
		AtomicBoolean entered = new AtomicBoolean();
		View observer = mock(View.class);
		doAnswer(invocation -> {
			entered.set(true);
			assertTrue(getConstruction().isFileLoading(), "The reader must see reconstruction context");
			throw readerFailure;
		}).when(observer).clearView();
		getKernel().attach(observer);
		getConstruction().setFileLoading(entryLoading);
		try {
			var failure = assertThrows(IllegalStateException.class,
					() -> getConstruction().runAtomicConstructionMutation(() -> { throw mutationFailure; }));
			assertTrue(entered.get(), "The real snapshot reader must start");
			assertTrue(failure.getMessage().contains("could not restore its host snapshot"));
			assertEquals(entryLoading, getConstruction().isFileLoading());
			assertTrue(java.util.Arrays.asList(failure.getSuppressed()).contains(mutationFailure));
			boolean readerCause = false;
			for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
				readerCause |= cause == readerFailure;
			}
			assertTrue(readerCause, "The reader failure must remain distinct from mutation failure");
		} finally {
			getKernel().detach(observer);
			getConstruction().setFileLoading(false);
		}
	}

	@ParameterizedTest @ValueSource(booleans = {false, true})
	void failedMutationRestoresNativeInputsAndTheActualRootPermutation(boolean exchangeRoots) {
		nativeSources();
		if (exchangeRoots) {
			GeoPoint first = point("J"), second = point("K");
			double x = first.x, y = first.y, z = first.z;
			int[] oldPermutation = selector().getSelectionPermutation();
			first.setCoords(second.x, second.y, second.z); second.setCoords(x, y, z);
			selector().initForNearToRelationship(); selector().compute();
			assertFalse(java.util.Arrays.equals(oldPermutation, selector().getSelectionPermutation()));
			first.updateCascade();
		}
		var savedSelection = selector().captureNativeRollbackSelectionState();
		assertNotNull(savedSelection);
		int[] permutation = selector().getSelectionPermutation();
		var oldSelector = selector();
		Map<String, NativeRollbackPointState> inputs = new LinkedHashMap<>();
		for (String label : INPUTS) { inputs.put(label, point(label).captureNativeRollbackState()); }
		String before = getApp().getXML();
		var ids = getConstruction().getSpatialIdentityRegistry().getRecords().stream()
				.map(record -> record.getId()).toList();
		int reservations = getConstruction().getSpatialIdentityRegistry().getReservedIdentityCount();
		var rejected = new IllegalStateException("injected native MEMORY rejection");
		assertSame(rejected, assertThrows(IllegalStateException.class,
				() -> getConstruction().runAtomicConstructionMutation(() -> {
					point("B").setCoords(0, 3, 1); point("B").updateCascade();
					point("C").setCoords(5, 0, 1); point("C").updateCascade();
					add("Transient=(8,9)");
					throw rejected;
				})));
		assertEquals(before, getApp().getXML());
		assertNotSame(oldSelector, selector(), "Normal XML reconstructs the producer");
		assertTrue(savedSelection.matches(selector()));
		assertArrayEquals(permutation, selector().getSelectionPermutation());
		inputs.forEach((label, state) -> assertTrue(state.matches(point(label)), label));
		assertEquals(ids, getConstruction().getSpatialIdentityRegistry().getRecords().stream()
				.map(record -> record.getId()).toList());
		assertEquals(reservations, getConstruction().getSpatialIdentityRegistry().getReservedIdentityCount());
		assertNull(lookup("Transient"));
		assertEquals(1, getConstruction().getLastNativeTransactionMetrics().dagCalls);
		assertFalse(getApp().getXML().contains(NativeTransactionalState2D.BINDING_TAG));
	}

	@Test void selectorPayloadCannotBeInstalledOutsideItsOwningRestore() {
		nativeSources();
		var state = selector().captureNativeRollbackSelectionState();
		assertNotNull(state);
		assertThrows(IllegalStateException.class, () -> selector().installNativeRollbackSelectionState(state));
		assertThrows(IllegalStateException.class, () -> selector().installNativeRollbackSelectionState(null));
	}

	@Test void pendingAndLimitedSelectorsDoNotGainMemoryPermission() {
		add("A=(0,0)"); add("c=Circle(A,2)"); add("g=Line((-3,0),(3,0))");
		nativePair("J", "K", "g", "c");
		var selection = (AlgoIntersectLineConic) point("J").getParentAlgorithm();
		selection.initForNearToRelationship();
		assertNull(selection.captureNativeRollbackSelectionState());
		add("segment=Segment((-3,0),(3,0))");
		nativePair("S", "T", "segment", "c");
		var limited = (AlgoIntersectLineConic) point("S").getParentAlgorithm();
		assertNull(limited.captureNativeRollbackSelectionState());
	}

	private void isolatedSelectorSource() {
		add("A=(0,0)"); add("radius=2"); add("circle=Circle(A,radius)");
		add("line=Line((-3,0),(3,0))");
		nativePair("J", "K", "line", "circle");
		// A real parameter excursion establishes a settled native selection.
		var radius = (org.geogebra.common.kernel.geos.GeoNumeric) lookup("radius");
		radius.setValue(3); radius.updateCascade();
		radius.setValue(2); radius.updateCascade();
		add("edge=Line(J,K)"); add("P=Point(edge)");
		assertNotNull(selector().captureNativeRollbackSelectionState());
		assertNull(NativeTransactionalState2D.unsupportedReason(getConstruction()));
	}

	private void assertPrivateRestoreRefused(String xml,
			org.geogebra.common.kernel.Construction.NativeUndoSnapshot snapshot, String expected) {
		String before = getApp().getXML();
		var selection = selector().captureNativeRollbackSelectionState();
		var place = point("P").captureNativeRollbackState();
		RuntimeException rejected = assertThrows(RuntimeException.class,
				() -> getConstruction().restoreNativeUndoSnapshot(snapshot.template(), () -> {
					try { getApp().getXMLio().processXMLString(xml, true, false); }
					catch (Exception failure) { throw new IllegalStateException(failure); }
				}));
		System.out.println("R9_NATIVE_PRIVATE_REJECTION expected=" + expected);
		rejected.printStackTrace(System.out);
		StringBuilder causes = new StringBuilder();
		for (Throwable cause = rejected; cause != null; cause = cause.getCause()) {
			causes.append(cause.getMessage()).append('\n');
		}
		assertTrue(causes.toString().contains(expected), causes.toString());
		assertEquals(before, getApp().getXML(), "Rejected private input restores its exact pre-state");
		assertTrue(selection.matches(selector()));
		assertTrue(place.matches(point("P")));
	}

	@Test void authenticatedSelectorRejectsChangedSourcesBeforeConsumption() {
		isolatedSelectorSource();
		var snapshot = getConstruction().captureNativeUndoSnapshot();
		assertNotNull(snapshot);
		String xml = snapshot.xml().toString();
		var value = java.util.regex.Pattern.compile(
				"(<element type=\"numeric\" label=\"radius\"[^>]*>[\\s\\S]*?<value val=\")2(\"/>)")
				.matcher(xml);
		assertTrue(value.find(), xml);
		String edited = value.replaceFirst(java.util.regex.Matcher.quoteReplacement(
				value.group(1) + "3" + value.group(2)));
		assertNotEquals(xml, edited);
		assertPrivateRestoreRefused(edited, snapshot, "Native selector source changed");
	}

	@Test void authenticatedSelectorRejectsAReboundSecondOutputRole() {
		isolatedSelectorSource();
		var snapshot = getConstruction().captureNativeUndoSnapshot();
		assertNotNull(snapshot);
		String xml = snapshot.xml().toString();
		var first = java.util.regex.Pattern.compile(
				"<element type=\"point\" label=\"J\"[^>]*>\\s*(<nativeRollbackBinding[^>]*/>)")
				.matcher(xml);
		assertTrue(first.find(), xml);
		String binding = first.group(1);
		String without = xml.replace(binding, "");
		var second = java.util.regex.Pattern.compile("(<element type=\"point\" label=\"K\"[^>]*>)")
				.matcher(without);
		assertTrue(second.find(), xml);
		String edited = second.replaceFirst("$1\n\t" + binding);
		assertPrivateRestoreRefused(edited, snapshot, "incompatible producer/role");
	}

	@ParameterizedTest @ValueSource(booleans = {false, true})
	void authenticatedSelectorRejectsForeignAndDuplicateBindings(boolean duplicate) {
		isolatedSelectorSource();
		var snapshot = getConstruction().captureNativeUndoSnapshot();
		String xml = snapshot.xml().toString();
		var first = java.util.regex.Pattern.compile(
				"<element type=\"point\" label=\"J\"[^>]*>\\s*(<nativeRollbackBinding[^>]*/>)")
				.matcher(xml);
		assertTrue(first.find(), xml);
		String binding = first.group(1);
		String edited;
		if (duplicate) {
			// Each element has one syntactically valid tag. J binds the key first;
			// replaying that same key on K must reach the owning session's guard.
			var second = java.util.regex.Pattern.compile(
					"(<element type=\"point\" label=\"K\"[^>]*>)").matcher(xml);
			assertTrue(second.find(), xml);
			edited = second.replaceFirst("$1\n\t" + java.util.regex.Matcher.quoteReplacement(binding));
		} else {
			edited = xml.replace(binding, binding.replaceFirst("key=\"[0-9a-f]{32}\"",
					"key=\"00000000000000000000000000000000\""));
		}
		assertNotEquals(xml, edited);
		assertPrivateRestoreRefused(edited, snapshot, duplicate
				? "duplicate or replayed native rollback binding" : "Foreign private native rollback binding");
	}

	@Test void reducedMemoryBudgetNeverFallsBackToAnIntrinsicUnsupportedWitness() {
		nativeSources();
		var reason = NativeTransactionalState2D.assessHost(getConstruction(),
				new NativeSourceFidelity2D.Limits(1, 1, 1)).reason();
		assertEquals(NativeTransactionalState2D.UnsupportedReason.SOURCE_FIDELITY_RESOURCE_EXHAUSTED, reason);
	}
}
