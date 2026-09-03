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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.algos.AlgoSplineV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionQuery2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResolver2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResult2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionStatus2D;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spline.SplinePolynomialModel2D;
import org.geocedg.common.kernel.spline.SplineSemanticEvaluator2D;
import org.geogebra.common.kernel.commands.AlgebraProcessor;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoCurveCartesianND;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.util.InternalClipboard;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.Test;

/**
 * Host lifecycle coverage for the structurally continuous native spline model.
 *
 * <p>These tests reconstruct the unchanged command/XML schema from current
 * command inputs. They are not historical-byte archive fixtures, interval
 * certification proofs, or a replacement for the native archive tests.</p>
 */
final class G9S1R1StructuralSplineLifecycleTest extends G9U0PublicSurfaceTestBase {

	private static final String SPLINE_BRANCH = AlgoSplineV2.BRANCH_KEY;

	@Test
	void highPrecisionQuinticRebuildsExactDerivedAuthorityAfterXmlAndUndo() {
		activateUndo();
		GeoLocusV2 source = quinticSpline();
		assertTrue(source.isDefined());
		assertTrue(model(source).getConstructionEvidence().getWorkingPrecision() > 0);
		assertEquals("spline-structural-precision/v1",
				model(source).getConstructionEvidence().getPolicy());
		GeoPoint point = add("P=Point(S,\"spline-v2/main\",0.5)");
		final PersistentGeoId sourceId = source.getPersistentLocusId();
		final PersistentGeoId pointId = id(point);
		final LocusSemanticAddress2D address = parent(point).getSemanticAddress();
		final String initialSignature = model(source).getSemanticSignature();
		final LocusPoint2D initialPoint = evaluate(source, 0.5);
		getApp().storeUndoInfo();
		move("h", 0.125);
		assertTrue(source.isDefined());
		assertTrue(point.isDefined());
		assertSame(source, parent(point).getSource());
		assertEquals(address, parent(point).getSemanticAddress());
		getApp().storeUndoInfo();
		getKernel().undo();
		assertEquals(initialSignature, model(source("S")).getSemanticSignature());
		getKernel().redo();
		assertTrue(source("S").isDefined());
		assertTrue(point("P").isDefined());
		move("h", -0.25);
		move("h", 0);
		assertEquals(initialSignature, model(source("S")).getSemanticSignature());
		String xml = getApp().getXML();
		assertFalse(xml.contains("coefficientNumerators"));
		assertFalse(xml.contains("freeCoordinates"));
		assertFalse(xml.contains("semantic-spline-polynomial/"));
		getApp().setXML(xml, true);
		assertEquals(sourceId, source("S").getPersistentLocusId());
		assertEquals(pointId, id(point("P")));
		assertEquals(address, parent(point("P")).getSemanticAddress());
		assertEquals(initialSignature, model(source("S")).getSemanticSignature());
		assertEquals(initialPoint.getX(), point("P").getInhomX(), 0);
		assertEquals(initialPoint.getY(), point("P").getInhomY(), 0);
		assertTrue(model(source("S")).getConstructionEvidence().getWorkingPrecision() > 0);
	}

	@Test
	void highPrecisionQuinticCopyRemapsIdentityWithoutCopyingNumericalAuthority() {
		GeoLocusV2 source = quinticSpline();
		assertTrue(source.isDefined());
		GeoPoint point = add("P=Point(S,\"spline-v2/main\",0.375)");
		final PersistentGeoId sourceId = source.getPersistentLocusId();
		final PersistentGeoId pointId = id(point);
		String signature = model(source).getSemanticSignature();
		String clipboard = InternalClipboard.getTextToSave(getApp(), List.of(point), text -> text);
		int separator = clipboard.indexOf('\n');
		InternalClipboard.pasteGeoGebraXMLInternal(getApp(),
				new ArrayList<>(Arrays.asList(clipboard.substring(0, separator).split(" "))),
				clipboard.substring(separator));
		var registry = getConstruction().getSpatialIdentityRegistry();
		GeoIdentityRecord copiedRecord = registry.getRecords().stream()
				.filter(GeoIdentityRecord.class::isInstance).map(GeoIdentityRecord.class::cast)
				.filter(record -> pointId.equals(record.getCopySourceId()))
				.findFirst().orElseThrow();
		GeoPoint copied = assertInstanceOf(GeoPoint.class, registry.getGeo(copiedRecord.getId()));
		GeoLocusV2 copiedSource = parent(copied).getSource();
		assertTrue(copied.isDefined());
		assertNotEquals(pointId, id(copied));
		assertNotEquals(sourceId, copiedSource.getPersistentLocusId());
		assertEquals(signature, model(copiedSource).getSemanticSignature());
		assertEquals(parent(point).getSemanticAddress().getCanonicalParameter(),
				parent(copied).getSemanticAddress().getCanonicalParameter(), 0);
		getApp().setXML(getApp().getXML(), true);
		GeoPoint restored = assertInstanceOf(GeoPoint.class,
				getConstruction().getSpatialIdentityRegistry().getGeo(copiedRecord.getId()));
		assertTrue(restored.isDefined());
		assertEquals(signature, model(parent(restored).getSource()).getSemanticSignature());
	}

	@Test
	void palindromicSplineNonminimumGuardsDoNotObstructCrossingResolution() {
		GeoLocusV2 source = palindromicSpline();
		assertTrue(model(source).isClosed());
		assertEquals(2, model(source).getStructuralContinuityOrder());
		// Record the represented model, not the ideal interpolant: structural
		// continuity does not make its rounded free coordinates exact input data.
		for (double parameter : new double[] {0, 0.25, 0.5, 0.75}) {
			double[] derivative = model(source).evaluateDerivative(parameter);
			System.out.println("G9S1_R1_R6_STRUCTURAL_JET u="
					+ Double.toHexString(parameter) + "|dx="
					+ Double.toHexString(derivative[0]) + "|dy="
					+ Double.toHexString(derivative[1]));
			assertTrue(Double.isFinite(derivative[0]));
			assertTrue(Double.isFinite(derivative[1]));
		}
		assertTrue(model(source).evaluateDerivative(0.25)[0] > 0);
		assertTrue(model(source).evaluateDerivative(0.75)[0] < 0);
		LocusPoint2D crossing = evaluate(source, 0.25);
		for (double parameter : new double[] {0, 0.5}) {
			LocusPoint2D center = evaluate(source, parameter);
			LocusPoint2D neighbor = evaluate(source, parameter + 1.0 / 128);
			assertTrue(Math.hypot(center.getX() - crossing.getX(),
					center.getY() - crossing.getY())
					> Math.hypot(neighbor.getX() - crossing.getX(),
							neighbor.getY() - crossing.getY()));
		}
		LocusPointInteractionResult2D result = new LocusPointInteractionResolver2D()
				.resolve(new LocusPointInteractionQuery2D(source,
						crossing.getX(), crossing.getY(),
						LocusPointInteractionPolicy2D.initial(1E-6)));
		assertEquals(LocusPointInteractionStatus2D.MULTIPLE_SEMANTIC_PREIMAGES,
				result.getStatus(), result.getDiagnostic());
		assertEquals(2, result.getCandidates().size());
		for (double parameter : new double[] {0.25, 0.75}) {
			assertEquals(1, result.getCandidates().stream().filter(candidate ->
					Math.abs(candidate.getAddress().getCanonicalParameter()
							- parameter) < 1E-8).count());
		}
	}

	@Test
	void nativeRetracedSplineSingularTrueMinimumStillFailsClosed() {
		add("A=(1,0)");
		add("B=(0,0)");
		add("C=(1,0)");
		GeoLocusV2 source = add("S=SplineV2({A,B,C},3)");
		LocusPoint2D target = evaluate(source, 0.5);
		// Here zero speed is an assertion about the actual represented model,
		// not an approximation to an ideal spline's turning point.
		assertEquals(0, model(source).evaluateDerivative(0.5)[0], 0);
		assertEquals(0, model(source).evaluateDerivative(0.5)[1], 0);
		final int count = getConstruction().getGeoSetConstructionOrder().size();
		LocusPointInteractionResult2D result = new LocusPointInteractionResolver2D()
				.resolve(new LocusPointInteractionQuery2D(source,
						target.getX(), target.getY(),
						LocusPointInteractionPolicy2D.initial(1E-6)));
		assertEquals(LocusPointInteractionStatus2D.UNRESOLVED_NUMERICAL_SEARCH,
				result.getStatus(), result.getDiagnostic());
		assertNull(result.getUniqueCandidate());
		// UNRESOLVED may retain diagnostic candidates, but the singular
		// semantic address itself is not promoted to an admissible preimage.
		assertTrue(result.getCandidates().stream().noneMatch(candidate ->
				candidate.getAddress().getCanonicalParameter() == 0.5));
		assertTrue(result.getDiagnostic().contains("Singular"), result.getDiagnostic());
		assertEquals(count, getConstruction().getGeoSetConstructionOrder().size());
	}

	@Test
	void commandSchemaRebuildsVersionedModelWithoutPersistingDerivedState() {
		GeoLocusV2 source = lineSpline();
		GeoPoint point = interactive(source, 0.25);
		final PersistentGeoId sourceId = source.getPersistentLocusId();
		final PersistentGeoId pointId = id(point);
		final LocusSemanticAddress2D address = parent(point).getSemanticAddress();
		String signature = model(source).getSemanticSignature();
		assertTrue(signature.startsWith("semantic-spline-polynomial/v2|"), signature);
		String xml = getApp().getXML();
		assertTrue(xml.contains("name=\"SplineV2\""));
		assertFalse(xml.contains("semantic-spline-polynomial/"));
		assertFalse(xml.contains("coefficientNumerators"));
		assertFalse(xml.contains("structuralContinuityOrder"));
		assertFalse(xml.contains("intervalBoxes"));

		getApp().setXML(xml, true);
		GeoLocusV2 restored = source("S");
		GeoPoint restoredPoint = point("P");
		assertEquals(sourceId, restored.getPersistentLocusId());
		assertEquals(pointId, id(restoredPoint));
		assertEquals(signature, model(restored).getSemanticSignature());
		assertEquals(address, parent(restoredPoint).getSemanticAddress());
		assertSame(restored, parent(restoredPoint).getSource());
		assertTrue(restoredPoint.isDefined());
	}

	@Test
	void modelRevisionChangesWithoutChangingProviderSourceOrAddressIdentity() {
		GeoLocusV2 source = lineSpline();
		GeoPoint point = interactive(source, 0.25);
		PersistentGeoId sourceId = source.getPersistentLocusId();
		final LocusSemanticAddress2D address = parent(point).getSemanticAddress();
		String provider = source.getSemanticDefinition().getProvider().getSemanticSignature();
		String provenance = source.getSemanticDefinition().getBranch(SPLINE_BRANCH)
				.getProvenance();
		String signature = model(source).getSemanticSignature();
		long revision = source.getSemanticRevision();
		move("h", 0.25);

		assertSame(source, source("S"));
		assertEquals(sourceId, source.getPersistentLocusId());
		assertTrue(source.getSemanticRevision() > revision);
		assertNotEquals(signature, model(source).getSemanticSignature());
		assertEquals(provider, source.getSemanticDefinition().getProvider().getSemanticSignature());
		assertEquals(provenance, source.getSemanticDefinition().getBranch(SPLINE_BRANCH)
				.getProvenance());
		assertEquals(address, parent(point).getSemanticAddress());
		assertEquals(0.25, point.getInhomY(), 1E-12);
		SplineSemanticEvaluator2D evaluator = assertInstanceOf(SplineSemanticEvaluator2D.class,
				source.getSemanticDefinition().getEvaluatorCapability());
		assertTrue(evaluator.getEvaluatorSignature().startsWith("spline-v2-evaluator/v2|"));
		assertTrue(evaluator.getPolynomialCapabilitySignature()
				.startsWith("spline-v2-piecewise-polynomial/v2|"));
		long unchanged = source.getSemanticRevision();
		source.getParentAlgorithm().compute();
		assertEquals(unchanged, source.getSemanticRevision());
	}

	@Test
	void invalidSourceRecoveryRetainsInteractionOwnedPointAndExactDirection() {
		GeoLocusV2 source = lineSpline();
		GeoPoint point = interactive(source, 0.25);
		final PersistentGeoId sourceId = source.getPersistentLocusId();
		final PersistentGeoId pointId = id(point);
		LocusSemanticAddress2D address = parent(point).getSemanticAddress();
		final int count = getConstruction().getGeoSetConstructionOrder().size();
		move("h", Double.NaN);
		assertFalse(source.isDefined());
		assertFalse(point.isDefined());
		assertEquals(address, parent(point).getSemanticAddress());
		move("h", 0.5);
		assertTrue(source.isDefined());
		assertTrue(point.isDefined());
		assertSame(point, point("P"));
		assertEquals(sourceId, source.getPersistentLocusId());
		assertEquals(pointId, id(point));
		assertEquals(address, parent(point).getSemanticAddress());
		assertEquals(0.5, point.getInhomY(), 1E-12);
		assertEquals(count, getConstruction().getGeoSetConstructionOrder().size());
	}

	@Test
	void compatibleRenamePreservesStructuralModelAndExistingBindings() {
		GeoLocusV2 source = lineSpline();
		GeoPoint point = interactive(source, 0.25);
		PersistentGeoId sourceId = source.getPersistentLocusId();
		LocusSemanticAddress2D address = parent(point).getSemanticAddress();
		String signature = model(source).getSemanticSignature();
		assertTrue(source.rename("RenamedSpline"));
		assertTrue(requireLookup("A").rename("RenamedInput"));
		assertTrue(point.rename("RenamedPoint"));
		assertEquals(sourceId, source.getPersistentLocusId());
		assertEquals(signature, model(source).getSemanticSignature());
		assertEquals(address, parent(point).getSemanticAddress());
		getApp().setXML(getApp().getXML(), true);
		assertEquals(sourceId, source("RenamedSpline").getPersistentLocusId());
		assertEquals(address, parent(point("RenamedPoint")).getSemanticAddress());
		assertTrue(point("RenamedPoint").isDefined());
	}

	@Test
	void closureCopyRemapsSourceAndPointButPreservesSemanticDirection() {
		GeoLocusV2 source = lineSpline();
		GeoPoint point = interactive(source, 0.25);
		PersistentGeoId sourceId = source.getPersistentLocusId();
		PersistentGeoId pointId = id(point);
		LocusSemanticAddress2D address = parent(point).getSemanticAddress();
		String clipboard = InternalClipboard.getTextToSave(getApp(), List.of(point), text -> text);
		int separator = clipboard.indexOf('\n');
		InternalClipboard.pasteGeoGebraXMLInternal(getApp(),
				new ArrayList<>(Arrays.asList(clipboard.substring(0, separator).split(" "))),
				clipboard.substring(separator));
		var registry = getConstruction().getSpatialIdentityRegistry();
		GeoIdentityRecord copiedRecord = registry.getRecords().stream()
				.filter(GeoIdentityRecord.class::isInstance).map(GeoIdentityRecord.class::cast)
				.filter(record -> pointId.equals(record.getCopySourceId()))
				.findFirst().orElseThrow();
		GeoPoint copied = assertInstanceOf(GeoPoint.class, registry.getGeo(copiedRecord.getId()));
		GeoLocusV2 copiedSource = parent(copied).getSource();
		final PersistentGeoId copiedSourceId = copiedSource.getPersistentLocusId();
		assertTrue(copied.isDefined());
		assertNotEquals(pointId, id(copied));
		assertNotEquals(sourceId, copiedSource.getPersistentLocusId());
		assertEquals(address.getCanonicalParameter(),
				parent(copied).getSemanticAddress().getCanonicalParameter(), 0);
		assertEquals(address.getComponentLineageKey(),
				parent(copied).getSemanticAddress().getComponentLineageKey());
		assertEquals(model(source).getSemanticSignature(),
				model(copiedSource).getSemanticSignature());
		getApp().setXML(getApp().getXML(), true);
		GeoPoint restored = assertInstanceOf(GeoPoint.class,
				getConstruction().getSpatialIdentityRegistry().getGeo(copiedRecord.getId()));
		assertTrue(restored.isDefined());
		assertEquals(copiedSourceId,
				parent(restored).getSource().getPersistentLocusId());
	}

	@Test
	void undoRedoRestoresInvalidAndValidSourceWithoutReplacingDurableIdentity() {
		activateUndo();
		add("h=1");
		add("A=(-2*h,0)");
		add("B=(0,0)");
		add("C=(2*h,0)");
		GeoLocusV2 source = add("S=SplineV2({A,B,C},3)");
		GeoPoint point = interactive(source, 0.25);
		final PersistentGeoId sourceId = source.getPersistentLocusId();
		final PersistentGeoId pointId = id(point);
		final LocusSemanticAddress2D address = parent(point).getSemanticAddress();
		getApp().storeUndoInfo();
		// Finite coincident interpolation inputs invalidate the native spline.
		// A NaN numeric also exercises host numeric XML/base-contract restoration;
		// this test keeps inputs finite to isolate native spline invalidity.
		move("h", 0);
		assertFalse(source.isDefined());
		assertFalse(point.isDefined());
		getApp().storeUndoInfo();
		move("h", 0.5);
		getApp().storeUndoInfo();
		getKernel().undo();
		assertFalse(point("P").isDefined());
		getKernel().undo();
		assertTrue(point("P").isDefined());
		getKernel().redo();
		assertFalse(point("P").isDefined());
		getKernel().redo();
		assertTrue(point("P").isDefined());
		assertEquals(sourceId, source("S").getPersistentLocusId());
		assertEquals(pointId, id(point("P")));
		assertEquals(address, parent(point("P")).getSemanticAddress());
	}

	@Test
	void oneSidedR4TokenReactivatesAfterStructuralSourceRecomputation() {
		GeoLocusV2 source = lineSpline();
		add("c=Circle((0,0),1)");
		GeoLocusIntersectionResult rich = add("R=Intersect(S,c)");
		String token = rich.getIntersectionResult().getFiniteSolutions().stream()
				.map(root -> root.getIdentity().getRootToken()).filter(rich::isPointAdmissible)
				.findFirst().orElseThrow();
		GeoText tokenInput = new GeoText(getConstruction(), token);
		GeoPoint point = LocusV2PublicOperations.selectIntersectionPoint(
				getConstruction(), "X", rich, tokenInput);
		final PersistentGeoId pointId = id(point);
		final PersistentGeoId sourceId = source.getPersistentLocusId();
		final int count = getConstruction().getGeoSetConstructionOrder().size();
		move("h", 0.25);
		assertTrue(point.isDefined());
		move("h", 2);
		assertFalse(point.isDefined());
		getApp().setXML(getApp().getXML(), true);
		assertFalse(point("X").isDefined());
		move("h", 0);
		GeoPoint restored = point("X");
		assertTrue(restored.isDefined());
		assertEquals(sourceId, source("S").getPersistentLocusId());
		assertEquals(pointId, id(restored));
		assertEquals(token, assertInstanceOf(AlgoLocusIntersectionPointV2.class,
				restored.getParentAlgorithm()).getSelectedRootToken());
		assertEquals(count, getConstruction().getGeoSetConstructionOrder().size());
	}

	@Test
	void transformedPartialMetricAndPointRecoverAcrossNegativeAndZeroDilation() {
		GeoLocusV2 source = lineSpline();
		add("k=1");
		GeoLocusV2 transformed = add("T=Dilate(S,k,(0,0))");
		GeoPoint first = add("P=Point(T,\"spline-v2/main\",0.25)");
		GeoPoint second = add("Q=Point(T,\"spline-v2/main\",0.75)");
		GeoNumeric partial = add("M=Length(T,P,Q)");
		PersistentGeoId transformedId = transformed.getPersistentLocusId();
		LocusSemanticAddress2D address = parent(first).getSemanticAddress();
		assertNotEquals(source.getPersistentLocusId(), transformedId);
		for (double scale : new double[] {2, -2, 0, -0.5, 1}) {
			move("k", scale);
			assertTrue(first.isDefined());
			assertTrue(second.isDefined());
			assertTrue(partial.isDefined());
			assertEquals(2 * Math.abs(scale), partial.getDouble(), 1E-8);
			assertEquals(transformedId, transformed.getPersistentLocusId());
			assertEquals(address, parent(first).getSemanticAddress());
		}
		getApp().setXML(getApp().getXML(), true);
		assertEquals(transformedId, source("T").getPersistentLocusId());
		assertEquals(address, parent(point("P")).getSemanticAddress());
		assertEquals(2, assertInstanceOf(GeoNumeric.class, requireLookup("M")).getDouble(), 1E-8);
	}

	@Test
	void periodicInteractionDirectionBitsAndLiftSurviveStructuralModelReopen() {
		GeoLocusV2 source = closedSpline();
		GeoPoint point = interactive(source, 0.98);
		PersistentGeoId pointId = id(point);
		final PersistentGeoId sourceId = source.getPersistentLocusId();
		LocusPoint2D target = evaluate(source, 0.02);
		LocusPointInteractionResult2D moved = LocusV2PublicOperations.moveInteractiveSemanticPoint(
				point, target.getX(), target.getY(), LocusPointInteractionPolicy2D.initial(0.2));
		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				moved.getStatus(), moved.getDiagnostic());
		LocusSemanticAddress2D address = parent(point).getSemanticAddress();
		assertEquals(1, address.getPeriodicLift());
		assertEquals(0.02, address.getCanonicalParameter(), 1E-8);
		assertEquals(1, moved.getCandidates().size());
		long canonicalBits = Double.doubleToLongBits(address.getCanonicalParameter());
		getApp().setXML(getApp().getXML(), true);
		GeoPoint restored = point("P");
		assertTrue(restored.isDefined());
		assertEquals(pointId, id(restored));
		assertEquals(sourceId, parent(restored).getSource().getPersistentLocusId());
		assertEquals(address, parent(restored).getSemanticAddress());
		assertEquals(canonicalBits, Double.doubleToLongBits(
				parent(restored).getSemanticAddress().getCanonicalParameter()));
	}

	@Test
	void approximateHostEndpointEqualityCannotDeclareStructuralPeriodicClosure() {
		GeoPoint first = add("A=(1,0)");
		add("B=(0,1)");
		add("C=(-1,0)");
		add("D=(0,-1)");
		GeoPoint last = add("E=(1,0)");
		last.setCoords(Math.nextUp(1.0), 0, 1);
		last.updateCascade();
		assertTrue(first.isEqual(last));
		assertNotEquals(first.getInhomX(), last.getInhomX());
		GeoList inputs = add("inputs={A,B,C,D,E}");
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> SplinePolynomialModel2D.create(inputs, 3, null));
		assertTrue(exception.getMessage().contains("closed")
				|| exception.getMessage().contains("closure"), exception.getMessage());
		GeoLocusV2 rejected = add("S=SplineV2(inputs,3)");
		assertFalse(rejected.isDefined());
		GeoCurveCartesianND classic = assertInstanceOf(GeoCurveCartesianND.class,
				add("Classic=Spline(inputs,3)"));
		assertTrue(classic.isDefined());
	}

	@Test
	void exactDistinctEndpointObjectsRetainClosedProviderAndCanonicalOwnership() {
		add("A=(1,0)");
		add("B=(0,1)");
		add("C=(-1,0)");
		add("D=(0,-1)");
		GeoPoint last = add("E=(1,0)");
		last.setCoords(1, -0.0, 1);
		last.updateCascade();
		GeoLocusV2 source = add("S=SplineV2({A,B,C,D,E},3)");
		assertTrue(source.isDefined());
		assertTrue(model(source).isClosed());
		assertEquals(2, model(source).getStructuralContinuityOrder());
		assertEquals(ExplicitNumericDomainProvider2D.PROVIDER_ID,
				source.getSemanticDefinition().getProvider().getProviderId());
		assertTrue(source.getSemanticDefinition().getProvider().isPeriodic());
		assertFalse(source.getSemanticDefinition().getProvider().getDeclaredDomain()
				.isUpperClosed());
		assertEquals(1, model(source).findOwningSpan(model(source).getKnots()[1]));
		GeoPoint point = add("P=Point(S,\"spline-v2/main\",1)");
		assertEquals(0, parent(point).getSemanticAddress().getCanonicalParameter(), 0);
		assertEquals(1, parent(point).getSemanticAddress().getPeriodicLift());
		assertTrue(point.isDefined());
	}

	@Test
	void currentReopenAndDifferentRegularUpdatePathsYieldIdenticalModelSignatures() {
		GeoLocusV2 source = lineSpline();
		GeoPoint point = interactive(source, 0.25);
		PersistentGeoId sourceId = source.getPersistentLocusId();
		PersistentGeoId pointId = id(point);
		String initial = getApp().getXML();
		List<String> expected = null;
		for (double[] path : new double[][] {{0.25}, {0.05, 0.1, 0.25}, {-0.5, 0.5, 0.25}}) {
			getApp().setXML(initial, true);
			for (double value : path) {
				move("h", value);
			}
			getApp().setXML(getApp().getXML(), true);
			assertEquals(sourceId, source("S").getPersistentLocusId());
			assertEquals(pointId, id(point("P")));
			assertTrue(point("P").isDefined());
			List<String> current = List.of(model(source("S")).getSemanticSignature(),
					Double.toHexString(point("P").getInhomX()),
					Double.toHexString(point("P").getInhomY()),
					parent(point("P")).getBranchInput().getTextString());
			if (expected == null) {
				expected = current;
			} else {
				assertEquals(expected, current);
			}
		}
	}

	@Test
	void sameSourceRichOnlyQueryRetainsItsSingleDependencyAcrossReopen() {
		GeoLocusV2 source = lineSpline();
		GeoLocusIntersectionResult rich = add("R=Intersect(S,S)");
		PersistentGeoId sourceId = source.getPersistentLocusId();
		PersistentGeoId richId = id(rich);
		assertSameSourceArgumentsAndSingleDependency(rich);
		assertNoMaterializableRoots(rich);
		getApp().setXML(getApp().getXML(), true);
		GeoLocusIntersectionResult restored = (GeoLocusIntersectionResult) requireLookup("R");
		assertEquals(sourceId, source("S").getPersistentLocusId());
		assertEquals(richId, id(restored));
		assertSameSourceArgumentsAndSingleDependency(restored);
		assertNoMaterializableRoots(restored);
	}

	@Test
	void sameSourceRichOnlyClosureCopyRemapsOneActualSourceWithoutPairAllocation() {
		lineSpline();
		GeoLocusIntersectionResult rich = add("R=Intersect(S,S)");
		PersistentGeoId richId = id(rich);
		String clipboard = InternalClipboard.getTextToSave(getApp(), List.of(rich), text -> text);
		int separator = clipboard.indexOf('\n');
		InternalClipboard.pasteGeoGebraXMLInternal(getApp(),
				new ArrayList<>(Arrays.asList(clipboard.substring(0, separator).split(" "))),
				clipboard.substring(separator));
		var registry = getConstruction().getSpatialIdentityRegistry();
		GeoIdentityRecord copy = registry.getRecords().stream()
				.filter(GeoIdentityRecord.class::isInstance).map(GeoIdentityRecord.class::cast)
				.filter(record -> richId.equals(record.getCopySourceId()))
				.findFirst().orElseThrow();
		GeoLocusIntersectionResult copied = assertInstanceOf(GeoLocusIntersectionResult.class,
				registry.getGeo(copy.getId()));
		assertNotEquals(richId, id(copied));
		assertSameSourceArgumentsAndSingleDependency(copied);
		assertNoMaterializableRoots(copied);
	}

	@Test
	void explicitCompatibleNumericRedefinePreservesPairSourceTokenAndPoint() throws Exception {
		GeoLocusIntersectionResult rich = pairResult();
		GeoPoint point = pairPoint(rich);
		// A spline's transitive numeric need not be identity-participating. An
		// ordinary scalar-locus control makes h an explicit registered dependency,
		// so this really exercises G9A rather than unregistered upstream editing.
		add("Qh=(h,0)");
		add("Dh={false,{-2,2,true,true}}");
		add("Lh=LocusV2(Qh,h,Dh)");
		PersistentGeoId sourceId = source("S").getPersistentLocusId();
		final PersistentGeoId pointId = id(point);
		GeoNumeric numeric = (GeoNumeric) requireLookup("h");
		PersistentGeoId numericId = id(numeric);
		assertNotNull(numericId);
		final String token = ((AlgoLocusIntersectionPointV2) point.getParentAlgorithm())
				.getSelectedRootToken();
		ErrorAccumulator errors = new ErrorAccumulator();
		AtomicReference<GeoElementND> callback = new AtomicReference<>();
		getKernel().getAlgebraProcessor().changeGeoElementNoExceptionHandling(
				numeric, "0.25", redefineInfo(numeric), false, callback::set, errors);
		assertTrue(errors.getErrors().isBlank(), errors.getErrors());
		assertNotNull(callback.get());
		assertEquals(numericId, id(requireLookup("h")));
		assertEquals(sourceId, source("S").getPersistentLocusId());
		assertTrue(point("X").isDefined());
		assertEquals(pointId, id(point("X")));
		assertEquals(0.25, point("X").getInhomY(), 1E-9);
		assertEquals(token, ((AlgoLocusIntersectionPointV2) point("X").getParentAlgorithm())
				.getSelectedRootToken());
		getApp().setXML(getApp().getXML(), true);
		assertEquals(pointId, id(point("X")));
		assertTrue(point("X").isDefined());
	}

	@Test
	void incompatibleSplineReplacementRetiresOldPairAndPointWithoutRetargeting() throws Exception {
		GeoLocusIntersectionResult rich = pairResult();
		GeoPoint point = pairPoint(rich);
		PersistentGeoId oldSource = source("S").getPersistentLocusId();
		PersistentGeoId oldPoint = id(point);
		assertExplicitSplineReplacementAfterAtomicGuard("materialized-pair");
		assertNotEquals(oldSource, source("S").getPersistentLocusId());
		assertNull(getConstruction().getSpatialIdentityRegistry().getGeo(oldSource));
		assertNull(getConstruction().getSpatialIdentityRegistry().getGeo(oldPoint));
		assertNull(lookup("R"));
		assertNull(lookup("X"));
		GeoLocusIntersectionResult current = add("NewResult=Intersect(S,T)");
		assertTrue(current.getIntersectionResult().getFiniteSolutions().stream()
				.anyMatch(root -> current.isPointAdmissible(root.getIdentity().getRootToken())));
	}

	@Test
	void nativeSplineExplicitReplacementSucceedsWithoutPairConsumers() throws Exception {
		lineSpline();
		assertExplicitSplineReplacementAfterAtomicGuard("no-pair-consumer");
		assertTrue(getConstruction().getGeoSetConstructionOrder().stream()
				.noneMatch(GeoLocusIntersectionResult.class::isInstance));
	}

	@Test
	void nativeSplineExplicitReplacementRetiresRichOnlyPairWithoutPointChild() throws Exception {
		GeoLocusIntersectionResult rich = pairResult();
		PersistentGeoId oldResult = id(rich);
		assertExplicitSplineReplacementAfterAtomicGuard("rich-pair-no-point");
		assertNull(lookup("R"));
		assertNull(getConstruction().getSpatialIdentityRegistry().getGeo(oldResult));
	}

	@Test
	void nativeSplineExplicitReplacementRebuildsWithOrdinarySemanticPointOnly() throws Exception {
		lineSpline();
		GeoPoint point = add("P=Point(S,\"spline-v2/main\",0.25)");
		PersistentGeoId oldPoint = id(point);
		assertExplicitSplineReplacementAfterAtomicGuard("semantic-point-no-pair");
		assertNull(lookup("P"));
		assertNull(getConstruction().getSpatialIdentityRegistry().getGeo(oldPoint));
	}

	@Test
	void stagedSplineOutputUsesExactSerializationOverlayWithoutPublishingLiveIdentity() {
		lineSpline();
		GeoList inputs = add("replacementPoints={A,B,C}");
		GeoNumeric degree = new GeoNumeric(getConstruction(), 3);
		AlgoSplineV2 algorithm = new AlgoSplineV2(getConstruction(), inputs, degree, null);
		GeoLocusV2 candidate = algorithm.getLocus();
		candidate.setLoadedLabel("CandidateSpline");
		var registry = getConstruction().getSpatialIdentityRegistry();
		PersistentGeoId decided = registry.allocatePersistentGeoId();
		assertNull(candidate.getPersistentLocusId());
		assertEquals("", candidate.getXML());
		try (var ignored = registry.beginSerializationOverlay(candidate, decided)) {
			assertNull(candidate.getPersistentLocusId());
			assertEquals(decided, registry.getPersistentGeoIdForSerialization(candidate));
			// Actual G9A rebuilds serialize inside the existing identity-XML scope.
			// An arbitrary standalone geo fragment deliberately omits durable IDs.
			getConstruction().beginSpatialIdentityXML();
			try {
				String xml = candidate.getXML();
				assertTrue(xml.contains("<element type=\"locusv2\""), xml);
				assertTrue(xml.contains("geocedgId=\"" + decided.toExternalForm() + "\""), xml);
			} finally {
				getConstruction().endSpatialIdentityXML();
			}
		} finally {
			registry.abandonReservedConstructionIdentities(List.of(decided));
			algorithm.remove();
			degree.remove();
		}
		assertNull(candidate.getPersistentLocusId());
		assertNull(registry.getPersistentGeoIdForSerialization(candidate));
	}

	private void assertExplicitSplineReplacementAfterAtomicGuard(String context) throws Exception {
		add("replacementPoints={A,B,C}");
		String definition = "SplineV2(replacementPoints,3)";
		String before = getApp().getXML();
		final PersistentGeoId sourceId = source("S").getPersistentLocusId();
		int geos = getConstruction().getGeoSetConstructionOrder().size();
		int records = getConstruction().getSpatialIdentityRegistry().getRecords().size();
		int reservations = getConstruction().getSpatialIdentityRegistry()
				.getReservedIdentityCount();
		ErrorAccumulator rejected = new ErrorAccumulator();
		getKernel().getAlgebraProcessor().changeGeoElementNoExceptionHandling(
				source("S"), definition, redefineInfo(source("S")), false,
				ignored -> { }, rejected);
		assertFalse(rejected.getErrors().isBlank());
		assertEquals(before, getApp().getXML());
		assertEquals(geos, getConstruction().getGeoSetConstructionOrder().size());
		assertEquals(records, getConstruction().getSpatialIdentityRegistry().getRecords().size());
		assertEquals(reservations,
				getConstruction().getSpatialIdentityRegistry().getReservedIdentityCount());
		AtomicReference<Throwable> failure = new AtomicReference<>();
		ErrorAccumulator explicit = new ErrorAccumulator() {
			@Override
			public void log(Throwable error) {
				failure.set(error);
			}
		};
		// The no-consumer baseline establishes that this exact explicit replacement
		// is supported. Attaching a rich pair must not break the transaction.
		getKernel().getAlgebraProcessor().changeGeoElementNoExceptionHandling(
				source("S"), definition,
				redefineInfo(source("S")).withSpatialReplacementOperation(),
				false, ignored -> { }, explicit);
		if (!explicit.getErrors().isBlank()) {
			System.out.println("G9S1_R1_REDEFINE_FAILURE_CONTEXT=" + context);
			if (failure.get() != null) {
				failure.get().printStackTrace(System.out);
			}
			// An unsuccessful transaction still has no license to leak candidate
			// geos. Preserve this assertion while diagnosing the supported path.
			assertEquals(before, getApp().getXML());
		}
		assertTrue(explicit.getErrors().isBlank(), explicit.getErrors());
		assertNotNull(source("S").getPersistentLocusId());
		assertNotEquals(sourceId, source("S").getPersistentLocusId());
		assertNull(getConstruction().getSpatialIdentityRegistry().getGeo(sourceId));
		assertEquals(reservations,
				getConstruction().getSpatialIdentityRegistry().getReservedIdentityCount());
	}

	private GeoLocusIntersectionResult pairResult() {
		lineSpline();
		add("E=(0,-2)");
		add("F=(0,0)");
		add("G=(0,2)");
		add("T=SplineV2({E,F,G},3)");
		return add("R=Intersect(S,T)");
	}

	private GeoPoint pairPoint(GeoLocusIntersectionResult rich) {
		String token = rich.getIntersectionResult().getFiniteSolutions().stream()
				.map(root -> root.getIdentity().getRootToken()).filter(rich::isPointAdmissible)
				.findFirst().orElseThrow();
		return LocusV2PublicOperations.selectIntersectionPoint(getConstruction(), "X", rich,
				new GeoText(getConstruction(), token));
	}

	private static void assertNoMaterializableRoots(GeoLocusIntersectionResult rich) {
		assertNotNull(rich.getIntersectionResult());
		assertTrue(rich.getIntersectionResult().getFiniteSolutions().stream()
				.noneMatch(root -> rich.isPointAdmissible(root.getIdentity().getRootToken())));
		assertFalse(rich.getTokenLedgerState().startsWith("5|"));
	}

	private void assertSameSourceArgumentsAndSingleDependency(GeoLocusIntersectionResult rich) {
		GeoElement[] inputs = rich.getParentAlgorithm().getInput();
		assertEquals(2, inputs.length);
		assertSame(inputs[0], inputs[1]);
		assertEquals(1, getConstruction().getSpatialIdentityRegistry().getGeoRecord(id(rich))
				.getDependencies().size());
	}

	private EvalInfo redefineInfo(GeoElement target) {
		return new EvalInfo(true, true).withSymbolicMode(
				AlgebraProcessor.getRedefinitionMode(target, getKernel()))
				.withLabelRedefinitionAllowedFor(target.getLabelSimple())
				.withSymbolic(true).withSliders(true);
	}

	private GeoLocusV2 lineSpline() {
		add("h=0");
		add("A=(-2,h)");
		add("B=(0,h)");
		add("C=(2,h)");
		return add("S=SplineV2({A,B,C},3)");
	}

	private GeoLocusV2 quinticSpline() {
		add("h=0");
		StringBuilder points = new StringBuilder();
		for (int index = 0; index < 25; index++) {
			String label = "HP" + index;
			add(label + "=(" + (index - 12) + "/4,sin(" + index + "/3)+h)");
			if (index > 0) {
				points.append(',');
			}
			points.append(label);
		}
		return add("S=SplineV2({" + points + "},5)");
	}

	private GeoLocusV2 closedSpline() {
		add("A=(1,0)");
		add("B=(0,1)");
		add("C=(-1,0)");
		add("D=(0,-1)");
		return add("S=SplineV2({A,B,C,D,A},3)");
	}

	private GeoLocusV2 palindromicSpline() {
		add("A=(-1,0)");
		add("B=(0,1)");
		add("C=(1,0)");
		add("D=(0,1)");
		add("E=(-1,0)");
		return add("S=SplineV2({A,B,C,D,E},3)");
	}

	private GeoPoint interactive(GeoLocusV2 source, double parameter) {
		LocusPoint2D target = evaluate(source, parameter);
		LocusPointInteractionResult2D resolved = new LocusPointInteractionResolver2D().resolve(
				new LocusPointInteractionQuery2D(source, target.getX(), target.getY(),
						LocusPointInteractionPolicy2D.initial(1E-6)));
		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				resolved.getStatus(), resolved.getDiagnostic());
		assertNotNull(resolved.getUniqueCandidate());
		return LocusV2PublicOperations.createInteractiveSemanticPoint(
				getConstruction(), "P", source, resolved.getUniqueCandidate());
	}

	private static LocusPoint2D evaluate(GeoLocusV2 source, double parameter) {
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.reference()) {
			var evaluation = source.evaluate(SPLINE_BRANCH, parameter, session);
			assertTrue(evaluation.isValid(), evaluation.getDiagnostic());
			return evaluation.getPoint();
		}
	}

	private void move(String label, double value) {
		GeoNumeric input = assertInstanceOf(GeoNumeric.class, requireLookup(label));
		input.setValue(value);
		input.updateCascade();
	}

	private GeoLocusV2 source(String label) {
		return assertInstanceOf(GeoLocusV2.class, requireLookup(label));
	}

	private GeoPoint point(String label) {
		return assertInstanceOf(GeoPoint.class, requireLookup(label));
	}

	private PersistentGeoId id(GeoElement geo) {
		return getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(geo);
	}

	private static SplinePolynomialModel2D model(GeoLocusV2 source) {
		return assertInstanceOf(AlgoSplineV2.class, source.getParentAlgorithm())
				.getPolynomialModel();
	}

	private static AlgoSemanticLocusPoint2D parent(GeoPoint point) {
		return assertInstanceOf(AlgoSemanticLocusPoint2D.class, point.getParentAlgorithm());
	}
}
