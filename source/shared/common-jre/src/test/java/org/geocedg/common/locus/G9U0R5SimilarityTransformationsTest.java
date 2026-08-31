/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geocedg.common.kernel.algos.AlgoLocusSimilarityTransform2D;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;
import org.geocedg.common.kernel.locus.LocusSimilarityTransform2D;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricDiagnosticCode2D;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geogebra.common.kernel.Path;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.Dilateable;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.geos.Mirrorable;
import org.geogebra.common.kernel.geos.Rotatable;
import org.geogebra.common.kernel.geos.Translateable;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.Test;

/** Focused R5 command, semantic, metric and lifecycle acceptance authority. */
class G9U0R5SimilarityTransformationsTest extends G9U0PublicSurfaceTestBase {

	@Test
	void c01OrdinaryCommandsCreateSemanticLocusImages() {
		GeoLocusV2 source = createParabola();
		add("v=Vector((0,0),(1,2))");
		add("A=(3,-1)");
		add("axis:y=x+1");
		String[] expressions = {
				"Lt=Translate(L,v)", "Lr0=Rotate(L,pi/3)",
				"Lr=Rotate(L,pi/3,A)", "Lmp=Reflect(L,A)",
				"Lml=Mirror(L,axis)", "Ld0=Dilate(L,2)",
				"Ld=Dilate(L,-2,A)"};
		Commands[] commands = {Commands.Translate, Commands.Rotate,
				Commands.Rotate, Commands.Mirror, Commands.Mirror,
				Commands.Dilate, Commands.Dilate};
		for (int index = 0; index < expressions.length; index++) {
			GeoLocusV2 image = add(expressions[index]);
			assertNotNull(image);
			assertEquals(commands[index], image.getParentAlgorithm().getClassName());
			assertNotEquals(source.getLocusIdentity(), image.getLocusIdentity());
		}
	}

	@Test
	void c02ParentInputsAreExactNormalDagDependencies() {
		GeoLocusV2 source = createLine();
		GeoNumeric angle = add("a=pi/4");
		GeoPoint center = add("C=(2,3)");
		GeoLocusV2 image = add("T=Rotate(L,a,C)");
		assertEquals(3, image.getParentAlgorithm().getInputLength());
		assertSame(source, image.getParentAlgorithm().getInput(0));
		assertSame(angle, image.getParentAlgorithm().getInput(1));
		assertSame(center, image.getParentAlgorithm().getInput(2));
		var registry = getConstruction().getSpatialIdentityRegistry();
		var record = registry.getGeoRecord(registry.getPersistentGeoId(image));
		assertEquals(Set.of(registry.getPersistentGeoId(source),
				registry.getPersistentGeoId(angle), registry.getPersistentGeoId(center)),
				Set.copyOf(record.getDependencies()));
	}

	@Test
	void c03LegacyMutableTransformationContractsRemainExcluded() {
		GeoLocusV2 source = createLine();
		assertFalse(Path.class.isAssignableFrom(source.getClass()));
		assertFalse(Translateable.class.isAssignableFrom(source.getClass()));
		assertFalse(Rotatable.class.isAssignableFrom(source.getClass()));
		assertFalse(Mirrorable.class.isAssignableFrom(source.getClass()));
		assertFalse(Dilateable.class.isAssignableFrom(source.getClass()));
	}

	@Test
	void i02IdentityMapsAndCoincidentCompositionsAlwaysUseFreshIds() {
		GeoLocusV2 source = createLine();
		GeoLocusV2 translated = add("T0=Translate(L,(0,0))");
		GeoLocusV2 rotated = add("R0=Rotate(L,0)");
		GeoLocusV2 dilated = add("D1=Dilate(L,1)");
		GeoLocusV2 reflected = add("F2=Reflect(Reflect(L,y=0),y=0)");
		Set<PersistentGeoId> identities = new LinkedHashSet<>(Arrays.asList(
				source.getPersistentLocusId(), translated.getPersistentLocusId(),
				rotated.getPersistentLocusId(), dilated.getPersistentLocusId(),
				reflected.getPersistentLocusId()));
		assertEquals(5, identities.size());
		for (GeoLocusV2 image : List.of(translated, rotated, dilated, reflected)) {
			assertPoint(image, 0.5, 0.5, 0);
		}

		LocusPoint2D extreme = new LocusPoint2D(-1E308, 1E308);
		LocusPoint2D zeroRotation = LocusSimilarityTransform2D
				.rotation(0, 1E308, -1E308).transform(extreme);
		LocusPoint2D unitDilation = LocusSimilarityTransform2D
				.dilation(1, 1E308, -1E308).transform(extreme);
		assertEquals(extreme, zeroRotation);
		assertEquals(extreme, unitDilation);
	}

	@Test
	void e01TranslationPreservesSemanticAddressAndDomain() {
		GeoLocusV2 source = createParabola();
		GeoLocusV2 image = add("T=Translate(L,(3,-2))");
		assertSame(source.getSemanticDefinition().getProvider(),
				image.getSemanticDefinition().getProvider());
		assertBranchContract(source, image);
		assertPoint(image, 0.5, 3.5, -1.75);
	}

	@Test
	void e02RotationReflectionAndDilationEvaluateComposition() {
		createLine();
		GeoLocusV2 rotation = add("R=Rotate(L,pi/2)");
		GeoLocusV2 central = add("P=Reflect(L,(1,2))");
		GeoLocusV2 axial = add("Lax=Reflect(L,y=x)");
		GeoLocusV2 dilation = add("Ldi=Dilate(L,-2,(1,1))");
		assertPoint(rotation, 0.5, 0, 0.5);
		assertPoint(central, 0.5, 1.5, 4);
		assertPoint(axial, 0.5, 0, 0.5);
		assertPoint(dilation, 0.5, 2, 3);
	}

	@Test
	void e03TransformationClosureUsesNormalComposition() {
		createLine();
		GeoLocusV2 image = add(
				"T=Translate(Rotate(Reflect(Dilate(L,2,(1,0)),y=x),pi/2),(3,4))");
		assertTrue(image.getParentAlgorithm()
				instanceof AlgoLocusSimilarityTransform2D);
		GeoElement parent = image.getParentAlgorithm().getInput(0).toGeoElement();
		assertTrue(parent instanceof GeoLocusV2);
		assertTrue(parent.getParentAlgorithm()
				instanceof AlgoLocusSimilarityTransform2D);
	}

	@Test
	void d01DynamicInputsPublishOnlySemanticChangesAndRecover() {
		createLine();
		GeoNumeric factor = add("k=2");
		GeoLocusV2 image = add("T=Dilate(L,k,(1,0))");
		long before = image.getSemanticRevision();
		factor.setValue(3);
		factor.updateCascade();
		assertTrue(image.getSemanticRevision() > before);
		assertPoint(image, 0.5, -0.5, 0);
		factor.setUndefined();
		factor.updateCascade();
		assertFalse(image.isDefined());
		factor.setValue(3);
		factor.updateCascade();
		assertTrue(image.isDefined());
		assertPoint(image, 0.5, -0.5, 0);
	}

	@Test
	void e04InvalidAddressesOverflowAndNonfiniteInputsFailWithoutStaleGeometry() {
		GeoLocusV2 source = createDisconnectedLine();
		GeoNumeric factor = add("k=1E308");
		GeoLocusV2 image = add("T=Dilate(L,k)");
		LocusEvaluation2D invalidSource = evaluate(source, 0);
		LocusEvaluation2D invalidImage = evaluate(image, 0);
		assertEquals(invalidSource.getStatus(), invalidImage.getStatus());
		assertEquals(EvaluationStatus.NON_FINITE, evaluate(image, 2).getStatus());

		factor.setValue(Double.POSITIVE_INFINITY);
		factor.updateCascade();
		assertFalse(image.isDefined());
		factor.setValue(2);
		factor.updateCascade();
		assertTrue(image.isDefined());
		assertPoint(image, 1.5, 3, 0);
	}

	@Test
	void g01ZeroDilationIsValidCollapsedSemanticImage() {
		GeoLocusV2 source = createDisconnectedLine();
		GeoLocusV2 image = add("T=Dilate(L,0,(4,5))");
		assertTrue(image.isDefined());
		assertBranchContract(source, image);
		LocusBranch2D branch = image.getSemanticDefinition().getBranch(BRANCH);
		assertTrue(branch.getProperties().contains(BranchProperty.COLLAPSED_IMAGE));
		assertTrue(branch.getProperties().contains(BranchProperty.FINITE));
		assertFalse(branch.getProperties().contains(BranchProperty.UNBOUNDED));
		assertPoint(image, -1.5, 4, 5);
		assertPoint(image, 1.5, 4, 5);
		assertFalse(evaluate(image, 0).isValid());
	}

	@Test
	void m01CollapsedOpenDomainHasTruthfulRichZeroLength() {
		createScalarLocus("L", "s", "Q", "(s,s^2)",
				"{false,{-2,2,false,false}}");
		GeoLocusV2 image = add("T=Dilate(L,0,(4,5))");
		GeoLocusMetricResult metric = totalMetric(image);
		assertTrue(metric.isDefined());
		assertEquals(MetricComputationStatus.SUCCESS,
				metric.getMetricResult().getComputationStatus());
		assertEquals(0, metric.getMetricResult().getMetricValue()
				.getFiniteValue().orElseThrow(), 0);
		assertTrue(metric.getMetricResult().getDiagnostics().stream()
				.anyMatch(diagnostic -> diagnostic.getCode()
						== MetricDiagnosticCode2D.COLLAPSED_IMAGE));
	}

	@Test
	void m02SimilarityLengthCovarianceIsEvaluatorDerived() {
		GeoLocusV2 source = createLine();
		GeoLocusV2 translated = add("T=Translate(L,(2,3))");
		GeoLocusV2 rotated = add("R=Rotate(L,pi/3,(1,2))");
		GeoLocusV2 reflected = add("F=Reflect(L,y=x)");
		GeoLocusV2 dilated = add("Ldi=Dilate(L,-3,(1,2))");
		double length = metricValue(add("Ms=LocusLength(L)"));
		assertEquals(length, metricValue(add("Mt=LocusLength(T)")), 1E-9);
		assertEquals(length, metricValue(add("Mr=LocusLength(R)")), 1E-9);
		assertEquals(length, metricValue(add("Mf=LocusLength(F)")), 1E-9);
		assertEquals(3 * length, metricValue(add("Md=LocusLength(Ldi)")), 1E-8);
	}

	@Test
	void m03PartialAndPeriodicMetricCovarianceUsesSemanticAddresses() {
		createLine();
		add("T=Dilate(L,-3,(1,2))");
		add("P0=Point(L,\"" + BRANCH + "\",-1)");
		add("P1=Point(L,\"" + BRANCH + "\",1)");
		add("TP0=Point(T,\"" + BRANCH + "\",-1)");
		add("TP1=Point(T,\"" + BRANCH + "\",1)");
		assertEquals(2, metricValue(add("Mp=LocusLength(L,P0,P1)")), 1E-9);
		assertEquals(6, metricValue(add("Mtp=LocusLength(T,TP0,TP1)")), 1E-8);

		add("w=0");
		add("Qc=(cos(w),sin(w))");
		add("Dc={true,{0,2*pi,true,false}}");
		add("Cyc=LocusV2(Qc,w,Dc)");
		add("CycT=Rotate(Cyc,pi/3)");
		add("C0=Point(Cyc,\"" + BRANCH + "\",3*pi/2)");
		add("C1=Point(Cyc,\"" + BRANCH + "\",pi/2)");
		add("CT0=Point(CycT,\"" + BRANCH + "\",3*pi/2)");
		add("CT1=Point(CycT,\"" + BRANCH + "\",pi/2)");
		assertEquals(Math.PI,
				metricValue(add("Mc=LocusLength(Cyc,C0,C1)")), 2E-7);
		assertEquals(Math.PI,
				metricValue(add("Mct=LocusLength(CycT,CT0,CT1)")), 2E-7);
	}

	@Test
	void p02SemanticPointCovarianceKeepsAddressAndDistinctIdentity() {
		createLine();
		add("dx=2");
		add("v=Vector((0,0),(dx,3))");
		add("T=Translate(L,v)");
		GeoPoint sourcePoint = add("P=Point(L,\"" + BRANCH + "\",0.75)");
		GeoPoint imagePoint = add("TP=Point(T,\"" + BRANCH + "\",0.75)");
		assertEquals(sourcePoint.getInhomX() + 2, imagePoint.getInhomX(), 1E-10);
		assertEquals(sourcePoint.getInhomY() + 3, imagePoint.getInhomY(), 1E-10);
		SpatialIdentityRegistry registry = getConstruction()
				.getSpatialIdentityRegistry();
		assertNotEquals(registry.getPersistentGeoId(sourcePoint),
				registry.getPersistentGeoId(imagePoint));
		GeoNumeric dx = (GeoNumeric) requireLookup("dx");
		dx.setValue(-4);
		dx.updateCascade();
		assertEquals(sourcePoint.getInhomX() - 4, imagePoint.getInhomX(), 1E-10);
		assertEquals(sourcePoint.getInhomY() + 3, imagePoint.getInhomY(), 1E-10);
	}

	@Test
	void p03EverySimilarityFamilyFeedsNormalSemanticPointsAndDynamicInputs() {
		createLine();
		final GeoNumeric angle = add("a=0");
		final GeoPoint center = add("C=(1,2)");
		add("m=0");
		add("g:y=m*x");
		final GeoNumeric factor = add("k=2");
		add("R=Rotate(L,a,C)");
		add("F=Reflect(L,g)");
		add("Ld=Dilate(L,k,C)");
		GeoPoint rotated = add("PR=Point(R,\"" + BRANCH + "\",0.5)");
		GeoPoint reflected = add("PF=Point(F,\"" + BRANCH + "\",0.5)");
		GeoPoint dilated = add("PD=Point(Ld,\"" + BRANCH + "\",0.5)");
		assertEquals(0.5, rotated.getInhomX(), 1E-10);
		assertEquals(0.5, reflected.getInhomX(), 1E-10);
		assertEquals(0, reflected.getInhomY(), 1E-10);
		assertEquals(0, dilated.getInhomX(), 1E-10);
		assertEquals(-2, dilated.getInhomY(), 1E-10);

		angle.setValue(Math.PI / 2);
		angle.updateCascade();
		assertEquals(3, rotated.getInhomX(), 1E-10);
		assertEquals(1.5, rotated.getInhomY(), 1E-10);
		factor.setValue(-1);
		factor.updateCascade();
		assertEquals(1.5, dilated.getInhomX(), 1E-10);
		assertEquals(4, dilated.getInhomY(), 1E-10);
		center.setCoords(2, 0, 1);
		center.updateCascade();
		assertTrue(rotated.isDefined());
		assertTrue(dilated.isDefined());
	}

	@Test
	void x01TransformedQueriesHaveCovariantGeometryButFreshTokens() {
		createLine();
		GeoConic circle = add("c=Circle((0,0),1)");
		GeoLocusIntersectionResult source = add("Rs=Intersect(L,c)");
		add("v=Vector((0,0),(3,4))");
		add("T=Translate(L,v)");
		add("ct=Translate(c,v)");
		GeoLocusIntersectionResult transformed = add("Rt=Intersect(T,ct)");
		assertSame(circle, source.getParentAlgorithm().getInput(1));
		assertEquals(2, source.getIntersectionResult().getFiniteSolutions().size());
		assertEquals(2,
				transformed.getIntersectionResult().getFiniteSolutions().size());
		assertNotEquals(source.getSourcePairIdentity(),
				transformed.getSourcePairIdentity());
		assertEquals(Set.of(-1.0, 1.0), xCoordinates(source, 0));
		assertEquals(Set.of(2.0, 4.0), xCoordinates(transformed, 0));
		Set<String> sourceTokens = tokens(source);
		Set<String> transformedTokens = tokens(transformed);
		assertTrue(Collections.disjoint(sourceTokens, transformedTokens));
		assertTrue(transformedTokens.stream().allMatch(transformed::isPointAdmissible));
		String token = transformedTokens.iterator().next();
		GeoText tokenGeo = tokenInput(token);
		GeoPoint point = LocusV2PublicOperations.selectIntersectionPoint(
				getConstruction(), "TPx", transformed, tokenGeo);
		assertTrue(point.isDefined());
		assertEquals(token,
				((org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2)
						point.getParentAlgorithm()).getSelectedRootToken());
	}

	@Test
	void x02TransformedQueryBindingIsPathIndependentUnderRegularMotion() {
		createLine();
		add("c=Circle((0,0),1)");
		GeoNumeric shift = add("h=0");
		add("v=Vector((0,0),(h,0))");
		add("T=Translate(L,v)");
		add("ct=Translate(c,v)");
		GeoLocusIntersectionResult rich = add("R=Intersect(T,ct)");
		materializeAll(rich, "PX");
		String initial = getApp().getXML();

		shift.setValue(1);
		shift.updateCascade();
		Map<String, LocusPoint2D> direct = tokenPoints(rich);
		Map<String, LocusPoint2D> directChildren = materializedPoints(rich);

		getApp().setXML(initial, true);
		shift = (GeoNumeric) requireLookup("h");
		for (int step = 1; step <= 20; step++) {
			shift.setValue(step / 20.0);
			shift.updateCascade();
		}
		rich = (GeoLocusIntersectionResult) requireLookup("R");
		assertEquals(direct, tokenPoints(rich));
		assertEquals(directChildren, materializedPoints(rich));

		getApp().setXML(initial, true);
		shift = (GeoNumeric) requireLookup("h");
		for (double value : new double[] {0.8, 0.3, 1}) {
			shift.setValue(value);
			shift.updateCascade();
		}
		rich = (GeoLocusIntersectionResult) requireLookup("R");
		assertEquals(direct, tokenPoints(rich));
		assertEquals(directChildren, materializedPoints(rich));
	}

	@Test
	void x03RotationReflectionAndDilationIntersectionGeometryCovaries() {
		createLine();
		add("c=Circle((0,0),1)");
		GeoLocusIntersectionResult source = add("Rs=Intersect(L,c)");
		add("Lr=Rotate(L,pi/2)");
		add("cr=Rotate(c,pi/2)");
		GeoLocusIntersectionResult rotated = add("Rr=Intersect(Lr,cr)");
		add("Lf=Reflect(L,y=x)");
		add("cf=Reflect(c,y=x)");
		GeoLocusIntersectionResult reflected = add("Rf=Intersect(Lf,cf)");
		add("Ld=Dilate(L,-2)");
		add("cd=Dilate(c,-2)");
		GeoLocusIntersectionResult dilated = add("Rd=Intersect(Ld,cd)");
		assertPointSet(source, new double[][] {{-1, 0}, {1, 0}});
		assertPointSet(rotated, new double[][] {{0, -1}, {0, 1}});
		assertPointSet(reflected, new double[][] {{0, -1}, {0, 1}});
		assertPointSet(dilated, new double[][] {{-2, 0}, {2, 0}});
		Set<String> allTokens = new LinkedHashSet<>();
		for (GeoLocusIntersectionResult result
				: List.of(source, rotated, reflected, dilated)) {
			assertEquals(2, tokens(result).size());
			assertTrue(tokens(result).stream().allMatch(allTokens::add));
		}
	}

	@Test
	void x04EverySupportedTargetFamilyConsumesTheTransformedEvaluator() {
		createParabola();
		GeoLocusV2 transformed = add("T=Rotate(L,pi/7,(1,-2))");
		List<GeoLocusIntersectionResult> results =
				allPublicIntersectionFamilies(transformed);
		assertEquals(10, results.size());
		assertTrue(results.stream().allMatch(result ->
				result.getIntersectionResult() != null));
	}

	@Test
	void p01SaveReopenPreservesCommandIdentityAndDurableOutput() {
		createParabola();
		GeoLocusV2 image = add("T=Rotate(L,pi/3,(1,2))");
		PersistentGeoId id = image.getPersistentLocusId();
		String xml = getApp().getXML();
		assertTrue(xml.contains("name=\"Rotate\""));
		getApp().setXML(xml, true);
		GeoLocusV2 reopened = (GeoLocusV2) requireLookup("T");
		assertTrue(reopened.isDefined());
		assertEquals(id, reopened.getPersistentLocusId());
		assertEquals(Commands.Rotate, reopened.getParentAlgorithm().getClassName());
		assertPoint(reopened, 0.5,
				1 + Math.cos(Math.PI / 3) * -0.5
						- Math.sin(Math.PI / 3) * -1.75,
				2 + Math.sin(Math.PI / 3) * -0.5
						+ Math.cos(Math.PI / 3) * -1.75);
	}

	@Test
	void s02CopyRemapsTheTransformDependencySliceWithoutIdReuse() {
		GeoLocusV2 source = createLine();
		GeoNumeric factor = add("k=2");
		final GeoPoint center = add("C=(1,2)");
		GeoLocusV2 image = add("T=Dilate(L,k,C)");
		SpatialIdentityRegistry registry = getConstruction()
				.getSpatialIdentityRegistry();
		PersistentGeoId imageId = image.getPersistentLocusId();
		String clipboard = InternalClipboard.getTextToSave(getApp(),
				Collections.singletonList(image), text -> text);
		paste(clipboard);
		GeoIdentityRecord copiedRecord = recordCopying(registry, imageId);
		GeoLocusV2 copy = (GeoLocusV2) registry.getGeo(copiedRecord.getId());
		assertNotNull(copy);
		assertNotEquals(source.getPersistentLocusId(), copy.getPersistentLocusId());
		assertNotEquals(imageId, copy.getPersistentLocusId());
		assertEquals(3, copy.getParentAlgorithm().getInputLength());
		assertNotSame(source, copy.getParentAlgorithm().getInput(0));
		assertNotSame(factor, copy.getParentAlgorithm().getInput(1));
		assertNotSame(center, copy.getParentAlgorithm().getInput(2));
		Set<PersistentGeoId> copiedInputs = new LinkedHashSet<>();
		for (int index = 0; index < copy.getParentAlgorithm().getInputLength(); index++) {
			GeoElement input = copy.getParentAlgorithm().getInput(index).toGeoElement();
			copiedInputs.add(registry.getPersistentGeoId(input));
			PersistentGeoId originalInputId = registry.getPersistentGeoId(
					image.getParentAlgorithm().getInput(index).toGeoElement());
			GeoIdentityRecord copiedInputRecord =
					recordCopying(registry, originalInputId);
			assertEquals(originalInputId, copiedInputRecord.getCopySourceId());
			assertEquals(registry.getPersistentGeoId(input), copiedInputRecord.getId());
		}
		assertEquals(copiedInputs, new LinkedHashSet<>(copiedRecord.getDependencies()));
		assertPoint(copy, 0.5, 0, -2);
	}

	@Test
	void s03UndoRedoAndRenamePreserveTheTransformIdentity() {
		final GeoLocusV2 source = createLine();
		activateUndo();
		getApp().storeUndoInfo();
		GeoLocusV2 image = add("T=Rotate(L,pi/2)");
		PersistentGeoId imageId = image.getPersistentLocusId();
		getApp().storeUndoInfo();
		getKernel().undo();
		assertNull(lookup("T"));
		getKernel().redo();
		image = (GeoLocusV2) requireLookup("T");
		assertEquals(imageId, image.getPersistentLocusId());
		long revision = image.getSemanticRevision();
		source.setLabel("SourceRenamed");
		image.setLabel("ImageRenamed");
		assertEquals(imageId, image.getPersistentLocusId());
		assertEquals(revision, image.getSemanticRevision());
		assertPoint(image, 0.5, 0, 0.5);
	}

	@Test
	void s04XmlKeepsOrdinaryCommandsAndContainsNoDerivedGeometryPayload() {
		createLine();
		add("T=Translate(L,(1,2))");
		add("R=Rotate(T,pi/4,(2,3))");
		String xml = getApp().getXML();
		assertTrue(xml.contains("app=\"classic\""));
		assertTrue(xml.contains("<command name=\"Translate\""));
		assertTrue(xml.contains("<command name=\"Rotate\""));
		assertFalse(xml.contains("renderVertices"));
		assertFalse(xml.contains("sampledPointCloud"));
		assertFalse(xml.contains("serializedCallback"));
		assertFalse(xml.contains("detachedMatrix"));
	}

	@Test
	void s01TransformedImageStartsWithOrdinarySourceStyle() {
		GeoLocusV2 source = createLine();
		source.setLineThickness(9);
		source.setLineType(15);
		source.setLineOpacity(77);
		GeoLocusV2 image = add("T=Translate(L,(1,2))");
		assertEquals(9, image.getLineThickness());
		assertEquals(15, image.getLineType());
		assertEquals(77, image.getLineOpacity());
		long revision = image.getSemanticRevision();
		image.setLineThickness(3);
		image.updatePresentationRepaint();
		assertEquals(revision, image.getSemanticRevision());
	}

	@Test
	void n01UnsupportedInversionAnd3dCenterRemainOutsideR5() {
		createLine();
		add("c=Circle((0,0),1)");
		assertThrows(AssertionError.class, () -> add("Bad=Reflect(L,c)"));
		add("badCenter:x=1");
		assertThrows(AssertionError.class,
				() -> add("Bad3=Rotate(L,1,badCenter)"));
		assertFalse(getApp().getXML().contains("label=\"Bad\""));
		assertFalse(getApp().getXML().contains("label=\"Bad3\""));
	}

	@Test
	void i01CollapsedIntersectionNeverFabricatesIsolatedAdmissibleRoots() {
		createLine();
		GeoLocusV2 image = add("T=Dilate(L,0,(0,0))");
		GeoLocusIntersectionResult through = intersect(image, "x=0");
		assertNotNull(through.getIntersectionResult());
		assertTrue(through.getIntersectionResult().getFiniteSolutions().isEmpty());
		assertTrue(through.getIntersectionResult().getOverlapEvidence().isEmpty());
		assertEquals(GeometryKind.UNRESOLVED,
				through.getIntersectionResult().getGeometryKind());
		assertTrue(through.getIntersectionResult().getDiagnostics().stream()
				.anyMatch(diagnostic -> diagnostic.getCode()
						== DiagnosticCode.CAPABILITY_NOT_AVAILABLE
						&& diagnostic.getMessage().contains("non-isolated")
						&& diagnostic.getMessage().contains("overlap")));
		assertTrue(through.getIntersectionResult().getFiniteSolutions().stream()
				.noneMatch(solution -> through.isPointAdmissible(
						solution.getIdentity().getRootToken())));
		add("missTarget:x=1");
		GeoLocusIntersectionResult miss = add("Rmiss=Intersect(T,missTarget)");
		assertTrue(miss.getIntersectionResult().getFiniteSolutions().isEmpty());
		assertTrue(miss.getIntersectionResult().getOverlapEvidence().isEmpty());
	}

	private static void assertBranchContract(GeoLocusV2 source,
			GeoLocusV2 image) {
		LocusBranch2D sourceBranch = source.getSemanticDefinition().getBranch(BRANCH);
		LocusBranch2D imageBranch = image.getSemanticDefinition().getBranch(BRANCH);
		assertEquals(sourceBranch.getBranchKey(), imageBranch.getBranchKey());
		assertEquals(sourceBranch.getDeclaredDriverDomain(),
				imageBranch.getDeclaredDriverDomain());
		assertEquals(sourceBranch.getValidDomainComponents(),
				imageBranch.getValidDomainComponents());
		assertEquals(sourceBranch.getOrientation(), imageBranch.getOrientation());
		assertEquals(sourceBranch.getLineage(), imageBranch.getLineage());
	}

	private static void assertPoint(GeoLocusV2 locus, double parameter,
			double expectedX, double expectedY) {
		LocusEvaluation2D evaluation = evaluate(locus, parameter);
		assertTrue(evaluation.isValid(), evaluation.getDiagnostic());
		LocusPoint2D point = evaluation.getPoint();
		assertEquals(expectedX, point.getX(), 1E-10);
		assertEquals(expectedY, point.getY(), 1E-10);
	}

	private static LocusEvaluation2D evaluate(GeoLocusV2 locus,
			double parameter) {
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.reference()) {
			return locus.evaluate(BRANCH, parameter, session);
		}
	}

	private static double metricValue(GeoLocusMetricResult metric) {
		return metric.getMetricResult().getMetricValue().getFiniteValue()
				.orElseThrow();
	}

	private static Set<Double> xCoordinates(GeoLocusIntersectionResult result,
			double offset) {
		Set<Double> coordinates = new LinkedHashSet<>();
		for (LocusIntersectionSolution2D solution
				: result.getIntersectionResult().getFiniteSolutions()) {
			coordinates.add(solution.getEvaluatedPoint().getX() + offset);
		}
		return coordinates;
	}

	private static Set<String> tokens(GeoLocusIntersectionResult result) {
		Set<String> tokens = new LinkedHashSet<>();
		for (LocusIntersectionSolution2D solution
				: result.getIntersectionResult().getFiniteSolutions()) {
			tokens.add(solution.getIdentity().getRootToken());
		}
		return tokens;
	}

	private static void assertPointSet(GeoLocusIntersectionResult result,
			double[][] expected) {
		List<LocusPoint2D> actual = new ArrayList<>();
		for (LocusIntersectionSolution2D solution
				: result.getIntersectionResult().getFiniteSolutions()) {
			actual.add(solution.getEvaluatedPoint());
		}
		actual.sort(Comparator.comparingDouble(LocusPoint2D::getX)
				.thenComparingDouble(LocusPoint2D::getY));
		List<double[]> sortedExpected = new ArrayList<>(Arrays.asList(expected));
		sortedExpected.sort(Comparator.comparingDouble((double[] point) -> point[0])
				.thenComparingDouble(point -> point[1]));
		assertEquals(sortedExpected.size(), actual.size());
		for (int index = 0; index < actual.size(); index++) {
			assertEquals(sortedExpected.get(index)[0], actual.get(index).getX(), 1E-8);
			assertEquals(sortedExpected.get(index)[1], actual.get(index).getY(), 1E-8);
		}
	}

	private void materializeAll(GeoLocusIntersectionResult result, String prefix) {
		int index = 0;
		for (LocusIntersectionSolution2D solution
				: result.getIntersectionResult().getFiniteSolutions()) {
			String token = solution.getIdentity().getRootToken();
			assertTrue(result.isPointAdmissible(token));
			LocusV2PublicOperations.selectIntersectionPoint(getConstruction(),
					prefix + index++, result, tokenInput(token));
		}
	}

	private static Map<String, LocusPoint2D> tokenPoints(
			GeoLocusIntersectionResult result) {
		Map<String, LocusPoint2D> points = new LinkedHashMap<>();
		for (LocusIntersectionSolution2D solution
				: result.getIntersectionResult().getFiniteSolutions()) {
			points.put(solution.getIdentity().getRootToken(),
					solution.getEvaluatedPoint());
		}
		return points;
	}

	private Map<String, LocusPoint2D> materializedPoints(
			GeoLocusIntersectionResult result) {
		Map<String, LocusPoint2D> points = new LinkedHashMap<>();
		for (int index = 0; index < 2; index++) {
			GeoPoint point = (GeoPoint) requireLookup("PX" + index);
			var parent = (org.geocedg.common.kernel.algos
					.AlgoLocusIntersectionPointV2) point.getParentAlgorithm();
			assertTrue(point.isDefined(), result.getTokenLedgerState());
			points.put(parent.getSelectedRootToken(),
					new LocusPoint2D(point.getInhomX(), point.getInhomY()));
		}
		return points;
	}

	private void paste(String clipboard) {
		int separator = clipboard.indexOf('\n');
		List<String> labels = new ArrayList<>(Arrays.asList(
				clipboard.substring(0, separator).split(" ")));
		InternalClipboard.pasteGeoGebraXMLInternal(getApp(), labels,
				clipboard.substring(separator));
	}

	private static GeoIdentityRecord recordCopying(
			SpatialIdentityRegistry registry, PersistentGeoId source) {
		return registry.getRecords().stream()
				.filter(GeoIdentityRecord.class::isInstance)
				.map(GeoIdentityRecord.class::cast)
				.filter(record -> source.equals(record.getCopySourceId()))
				.findFirst().orElseThrow();
	}

	private GeoText tokenInput(String token) {
		GeoText input = new GeoText(getConstruction(), token);
		input.setAuxiliaryObject(true);
		input.setEuclidianVisible(false);
		return input;
	}
}
