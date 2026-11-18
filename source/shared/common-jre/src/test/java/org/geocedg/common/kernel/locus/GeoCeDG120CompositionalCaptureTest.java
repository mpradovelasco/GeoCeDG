/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.kernel.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Driver;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operand;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operation;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Step;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geocedg.common.kernel.spatial.identity.EditAuthorityMode;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRole;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoAngle;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.MyVecNode;
import org.geogebra.common.kernel.algos.AlgoDependentPoint;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxModel2D;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxOutcome2D.Kind;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxOutcome2D.Unresolved;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/** Capture/type/provenance oracles; capture alone never establishes geometric validity. */
class GeoCeDG120CompositionalCaptureTest extends BaseUnitTest {

	private SemanticGeneratorDescriptor1D descriptor;

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	private void driver() {
		getKernel().setContinuous(false);
		add("seg=Segment((0,0),(4,0))");
		assertInstanceOf(GeoPoint.class, add("E=Point(seg)"));
		assertInstanceOf(GeoPoint.class, add("BaseTrace=E+(0,1)"));
		GeoLocusV2 base = add("baseLocus=LocusV2(BaseTrace,E)");
		descriptor = assertInstanceOf(AlgoDependentPointLocusV2.class,
				base.getParentAlgorithm()).getGeneratorDescriptor();
	}

	private CertifiedConstructionCapture2D.Outcome capture(String label) {
		return CertifiedConstructionCapture2D.captureWithReason(descriptor, lookup(label),
				lookup("E"), lookup("seg"), null, true);
	}

	private CertifiedConstructionProgram2D program(String label) {
		var result = capture(label);
		return result.getProgram().orElseThrow(() -> new AssertionError(result.getRefusal()));
	}

	private static Set<Operation> operations(CertifiedConstructionProgram2D program) {
		return program.getSteps().stream().map(Step::getOperation)
				.collect(Collectors.toCollection(() -> EnumSet.noneOf(Operation.class)));
	}

	@Test
	void nestedScalarSyntaxRetainsItsDriverInsteadOfFreezingItsCurrentValue() {
		driver();
		add("P=(sin(x(E))+cos(y(E)),sqrt(x(E)+2)/(Distance(E,(0,2))+1)-x(E)*y(E))");
		var before = program("P");
		assertTrue(operations(before).containsAll(EnumSet.of(Operation.POINT_COORDINATES,
				Operation.POINT_X, Operation.POINT_Y, Operation.POINT_DISTANCE,
				Operation.SCALAR_ADD, Operation.SCALAR_SUBTRACT, Operation.SCALAR_MULTIPLY,
				Operation.SCALAR_DIVIDE, Operation.SCALAR_SIN_DOUBLE, Operation.SCALAR_COS_DOUBLE,
				Operation.SCALAR_SQRT)));
		GeoPoint point = (GeoPoint) lookup("E");
		point.setCoords(3, 0, 1);
		point.updateCascade();
		assertEquals(before.getSignature(), program("P").getSignature());
	}

	/** Local IR capture checks context; it is not a public reconstruction certificate. */
	@Test
	void nativeShortRootIrKeepsGenericSineAndItsDomainGuards() {
		driver();
		GeoPoint source = add("RootSource=(x(E),sin(4*pi*x(E)))");
		MyVecNode coordinates = (MyVecNode) source.getDefinition().unwrap();
		var root = new ExpressionNode(getKernel(), coordinates.getY().deepCopy(getKernel()),
				org.geogebra.common.plugin.Operation.SQRT_SHORT, null);
		var tuple = new MyVecNode(getKernel(), coordinates.getX().deepCopy(getKernel()), root);
		GeoPoint shortPoint = new AlgoDependentPoint(getConstruction(),
				new ExpressionNode(getKernel(), tuple), false).getPoint();
		shortPoint.setLabel("ShortRoot");
		var captured = program("ShortRoot");
		assertTrue(operations(captured).contains(Operation.SCALAR_SIN));
		assertFalse(operations(captured).contains(Operation.SCALAR_SIN_DOUBLE));
		assertTrue(operations(captured).contains(Operation.SCALAR_SQRT));
		add("FastRoot=(x(E),sqrt(sin(4*pi*x(E))))");
		var fast = operations(program("FastRoot"));
		assertTrue(fast.contains(Operation.SCALAR_SIN_DOUBLE));
		assertFalse(fast.contains(Operation.SCALAR_SIN));
		var model = new ConstructionBoxModel2D(captured);
		// The fixture's segment is [0,4], so its driver parameter is x/4.
		var positive = model.evaluate(1.0 / 32, 1.0 / 32);
		assertEquals(Kind.VALID, positive.getKind());
		assertTrue(positive.getValueLower(1) <= 1 && positive.getValueUpper(1) >= 1);
		var zero = model.evaluate(1.0 / 16, 1.0 / 16);
		assertEquals(Kind.VALID, zero.getKind());
		assertEquals(0, zero.getValueLower(1));
		assertTrue(zero.getValueUpper(1) >= 0);
		GeoPoint state = (GeoPoint) lookup("E");
		state.getPathParameter().setT(1.0 / 16);
		state.getPath().pathChanged(state); state.updateCoords(); state.updateCascade();
		assertEquals(0, shortPoint.getInhomY());
		assertEquals(Math.sqrt(Math.sin(Math.PI)), ((GeoPoint) lookup("FastRoot")).getInhomY(), 0);
		var negative = model.evaluate(3.0 / 32, 3.0 / 32);
		assertEquals(Kind.UNRESOLVED, negative.getKind());
		assertEquals(Unresolved.PREDICATE, negative.getUnresolved());
		state.getPathParameter().setT(3.0 / 32);
		state.getPath().pathChanged(state); state.updateCoords(); state.updateCascade();
		assertFalse(shortPoint.isDefined());
		var crossing = model.evaluate(1.0 / 16 - 1e-8, 1.0 / 16 + 1e-8);
		assertEquals(Kind.UNRESOLVED, crossing.getKind());
		assertEquals(Unresolved.TOLERANCE, crossing.getUnresolved());
	}

	@Test
	void dependentAngleCanRotateAConstantPointWithoutChangingConstantRotationCapture() {
		driver();
		add("angleValue=-x(E)/2");
		add("P=Rotate((1,0),angleValue,(0,0))");
		assertTrue(operations(program("P")).containsAll(EnumSet.of(
				Operation.POINT_X, Operation.SCALAR_NEGATE, Operation.SCALAR_DIVIDE,
				Operation.ROTATE_BY_ANGLE)));
		assertInstanceOf(GeoPoint.class, add("OldRotation=Rotate(E,pi/6,(1,1))"));
		var old = CertifiedConstructionCapture2D.capture(descriptor, lookup("OldRotation"),
				lookup("E"), lookup("seg"), null).orElseThrow();
		assertEquals(old.getSignature(), program("OldRotation").getSignature());
		assertEquals(List.of(Operation.ROTATE_ABOUT), old.getSteps().stream()
				.map(Step::getOperation).collect(Collectors.toList()));
	}

	@Test
	void dependentAngleFoldingIsRefusedUntilItsUnboundedValueIsRestored() {
		driver();
		GeoAngle angle = assertInstanceOf(GeoAngle.class, add("angleValue=120°*x(E)"));
		angle.setAngleStyle(GeoAngle.AngleStyle.UNBOUNDED);
		add("P=(angleValue,0)");
		assertTrue(capture("P").getProgram().isPresent());
		GeoPoint driver = (GeoPoint) lookup("E");
		driver.setCoords(2, 0, 1);
		driver.updateCascade();
		assertEquals(4 * Math.PI / 3, angle.getDouble(), 1E-12);
		angle.setAngleStyle(GeoAngle.AngleStyle.NOTREFLEX);
		angle.updateCascade();
		assertEquals(2 * Math.PI / 3, angle.getDouble(), 1E-12,
				"The native consumer receives the folded angle, not the expression's raw value");
		var folded = capture("P");
		assertTrue(folded.getProgram().isEmpty());
		assertEquals(CertifiedConstructionCapture2D.ANGLE_STYLE, folded.getRefusal());
		angle.setAngleStyle(GeoAngle.AngleStyle.UNBOUNDED);
		angle.updateCascade();
		assertEquals(4 * Math.PI / 3, angle.getDouble(), 1E-12);
		assertTrue(capture("P").getProgram().isPresent());
	}

	@Test
	void dynamicRadiusRetainsTheDistanceOperandsAndAConstantRadiusKeepsItsOldStep() {
		driver();
		add("circle=Circle((0,0),Distance(E,(2,3)))");
		add("ray=Ray((0,0),(1,0))");
		add("P=Intersect(circle,ray,1)");
		assertTrue(operations(program("P")).containsAll(EnumSet.of(
				Operation.POINT_DISTANCE, Operation.CIRCLE_DYNAMIC_RADIUS,
				Operation.LINE_CIRCLE, Operation.INTERSECTION_OUTPUT)));
		add("constantCircle=Circle(E,3)");
		add("Q=Intersect(constantCircle,ray,1)");
		var constant = operations(program("Q"));
		assertTrue(constant.contains(Operation.CIRCLE_RADIUS));
		assertFalse(constant.contains(Operation.CIRCLE_DYNAMIC_RADIUS));
	}

	@Test
	void repeatedInlineOperationsHaveDistinctStructuralRolesWithinTheirProducer() {
		driver();
		add("P=(x(E)*y(E)+x(E)*(y(E)+1),(x(E)+1)*y(E))");
		var captured = capture("P");
		assertTrue(captured.getProgram().isPresent(), captured.getRefusal());
		var products = captured.getOrigins().stream()
				.filter(origin -> origin.getOperation().equals("SCALAR_MULTIPLY"))
				.collect(Collectors.toList());
		assertEquals(3, products.size());
		assertEquals(3, products.stream().map(origin -> origin.getMacroPath())
				.collect(Collectors.toSet()).size());
		for (var origin : products) {
			assertSame(lookup("P"), origin.getAnchor());
			assertTrue(origin.getMacroPath().startsWith("-e"));
		}
	}

	@Test
	void anAnonymousDistanceIsAnchoredAtTheCircleRadiusRole() {
		driver();
		add("circle=Circle((0,0),Distance(E,(2,3)))");
		add("ray=Ray((0,0),(1,0))");
		add("P=Intersect(circle,ray,1)");
		var captured = capture("P");
		assertTrue(captured.getProgram().isPresent(), captured.getRefusal());
		var distance = captured.getOrigins().stream()
				.filter(origin -> origin.getOperation().equals("POINT_DISTANCE"))
				.findFirst().orElseThrow();
		assertSame(lookup("circle"), distance.getAnchor());
		assertEquals("-eRadius", distance.getMacroPath());
	}

	@Test
	void captureDoesNotMistakeDivisionOrSquareRootSyntaxForAValidityCertificate() {
		driver();
		add("P=(x(E)/(x(E)-x(E)),sqrt(-x(E)-1))");
		assertFalse(lookup("P").isDefined());
		assertTrue(operations(program("P")).containsAll(EnumSet.of(
				Operation.SCALAR_DIVIDE, Operation.SCALAR_SQRT)));
	}

	@Test
	void aScalarFunctionOutsideTheAdmittedVocabularyRemainsAnExplicitRefusal() {
		driver();
		add("P=(exp(x(E)),0)");
		var result = capture("P");
		assertTrue(result.getProgram().isEmpty());
		assertEquals(CertifiedConstructionCapture2D.OUTSIDE_CLASS, result.getRefusal());
	}

	@Test
	void theIrRejectsScalarPointTypeConfusionAndForwardReferences() {
		double[] circle = {1, 1, 1, 0, 0, 1, 0, 0};
		var domain = new LocusInterval2D(-Math.PI, Math.PI, true, false);
		assertThrows(IllegalArgumentException.class, () -> new CertifiedConstructionProgram2D(
				Driver.CIRCLE, circle, domain, List.of(new Step(Operation.SCALAR_SIN,
						List.of(Operand.node(0))))));
		assertThrows(IllegalArgumentException.class, () -> new CertifiedConstructionProgram2D(
				Driver.CIRCLE, circle, domain, List.of(new Step(Operation.POINT_COORDINATES,
						List.of(Operand.node(1), Operand.scalar(0))))));
	}

	/** An inert test participation establishes identity only, never a geometry certificate. */
	private void registerOwner(GeoElement owner) {
		var registry = getConstruction().getSpatialIdentityRegistry();
		registry.registerParticipation(owner, new GeoIdentityRecord(
				registry.allocatePersistentGeoId(), "test.composition-owner", owner.getClass().getSimpleName(),
				"test.composition-owner/v1", 1, EditAuthorityMode.PROJECTION_DEFINED,
				ProjectionBindingRole.DEFINING, "OWNER", 1, 0, 0));
	}

	private void legacyProducers() {
		driver();
		add("O=(0,0)");
		add("angleValue=-x(E)/2");
		add("rotated=Rotate((1,0),angleValue,O)");
		add("circle=Circle(O,Distance(E,(0,2)))");
		add("ray=Ray(O,rotated)");
		add("P=Intersect(circle,ray,1)");
	}

	@Test
	void legacyProducerOriginsLiftToTheFirstActualDurableOwner() {
		legacyProducers();
		registerOwner(lookup("P"));
		var registry = getConstruction().getSpatialIdentityRegistry();
		assertNull(registry.getPersistentGeoId(lookup("circle")));
		assertNull(registry.getPersistentGeoId(lookup("rotated")));
		var captured = capture("P");
		assertTrue(captured.getProgram().isPresent(), captured.getRefusal());
		assertTrue(operations(captured.getProgram().orElseThrow()).containsAll(EnumSet.of(
				Operation.POINT_DISTANCE, Operation.ROTATE_BY_ANGLE,
				Operation.CIRCLE_DYNAMIC_RADIUS)));
		var origins = captured.getOrigins().stream()
				.filter(origin -> Operation.valueOf(origin.getOperation()).isCompositionProof())
				.collect(Collectors.toList());
		assertFalse(origins.isEmpty());
		for (var origin : origins) { assertSame(lookup("P"), origin.getAnchor()); }
		assertTrue(origins.stream().filter(origin -> origin.getOperation().equals("POINT_DISTANCE"))
				.allMatch(origin -> origin.getMacroPath().contains("-pAlgoIntersectSingle-i0")
						&& origin.getMacroPath().contains("-eRadius")));
		assertTrue(origins.stream().filter(origin -> origin.getOperation().equals("ROTATE_BY_ANGLE"))
				.allMatch(origin -> origin.getMacroPath().contains("-pAlgoIntersectSingle-i1")
						&& origin.getMacroPath().contains("-pAlgoJoinPointsRay-i1")));
		var paths = origins.stream().map(origin -> origin.getMacroPath()).collect(Collectors.toList());
		lookup("circle").setLabel("renamedCircle");
		lookup("rotated").setLabel("renamedRotation");
		add("Unrelated=(17,19)");
		var after = capture("P");
		assertTrue(after.getProgram().isPresent(), after.getRefusal());
		assertEquals(paths, after.getOrigins().stream()
				.filter(origin -> Operation.valueOf(origin.getOperation()).isCompositionProof())
				.map(origin -> origin.getMacroPath()).collect(Collectors.toList()));
		registerOwner(lookup("renamedCircle"));
		var nearest = capture("P");
		assertTrue(nearest.getProgram().isPresent(), nearest.getRefusal());
		assertTrue(nearest.getOrigins().stream()
				.filter(origin -> origin.getOperation().equals("POINT_DISTANCE"))
				.allMatch(origin -> origin.getAnchor() == lookup("renamedCircle")
						&& origin.getMacroPath().equals("-eRadius")));
	}

	@Test
	void constantPointAliasingUsesTheActualSourceInsteadOfCoordinates() {
		driver();
		add("C=(0,2)");
		add("D=(0,2)");
		add("Q=Rotate(E,Distance(E,C),C)");
		add("R=Rotate(E,Distance(E,C),D)");
		var shared = program("Q");
		var distinct = program("R");
		Operand distanceCenter = shared.getSteps().stream()
				.filter(step -> step.getOperation() == Operation.POINT_DISTANCE)
				.findFirst().orElseThrow().getOperands().get(1);
		Operand rotationCenter = shared.getSteps().stream()
				.filter(step -> step.getOperation() == Operation.ROTATE_BY_ANGLE)
				.findFirst().orElseThrow().getOperands().get(2);
		assertTrue(distanceCenter.hasSameCapturedSource(rotationCenter));
		Operand distinctCenter = distinct.getSteps().stream()
				.filter(step -> step.getOperation() == Operation.ROTATE_BY_ANGLE)
				.findFirst().orElseThrow().getOperands().get(2);
		Operand distinctDistanceCenter = distinct.getSteps().stream()
				.filter(step -> step.getOperation() == Operation.POINT_DISTANCE)
				.findFirst().orElseThrow().getOperands().get(1);
		assertFalse(distinctDistanceCenter.hasSameCapturedSource(distinctCenter));
		assertFalse(shared.getSignature().equals(distinct.getSignature()));
	}

	@Test
	void aliasMetadataChangesOnlyNewCompositionProofSignatures() {
		double[] circle = {1, 1, 1, 0, 0, 1, 0, 0};
		var domain = new LocusInterval2D(-Math.PI, Math.PI, true, false);
		Operand first = Operand.point(0, 2, 1, 0, 2);
		Operand equalButDistinct = Operand.point(0, 2, 1, 0, 2);
		var oldShared = new CertifiedConstructionProgram2D(Driver.CIRCLE, circle, domain,
				List.of(new Step(Operation.MIDPOINT, List.of(Operand.node(0), first)),
						new Step(Operation.MIDPOINT, List.of(Operand.node(1), first))));
		var oldDistinct = new CertifiedConstructionProgram2D(Driver.CIRCLE, circle, domain,
				List.of(new Step(Operation.MIDPOINT, List.of(Operand.node(0), first)),
						new Step(Operation.MIDPOINT, List.of(Operand.node(1), equalButDistinct))));
		assertEquals(oldShared.getSignature(), oldDistinct.getSignature());
		assertFalse(first.hasSameCapturedSource(equalButDistinct));
		assertTrue(first.hasSameCapturedSource(first));
		assertTrue(Operand.node(0).hasSameCapturedSource(Operand.node(0)));
		assertFalse(Operand.node(0).hasSameCapturedSource(Operand.node(1)));
	}
}
