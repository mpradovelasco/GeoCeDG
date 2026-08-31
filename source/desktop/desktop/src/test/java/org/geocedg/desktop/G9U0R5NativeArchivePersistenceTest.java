/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JPanel;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.SemanticGeneratorDescriptor1D;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityDiagnostic;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.GeoGebraConstants;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.jre.io.MyXMLioJre;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.GetCommand;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.geos.GeoVec3D;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.App;
import org.geogebra.common.main.AppConfig;
import org.geogebra.common.main.settings.config.AppConfigDefault;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.awt.AwtFactoryD;
import org.geogebra.desktop.headless.AppDNoGui;
import org.geogebra.desktop.headless.GFileHandler;
import org.geogebra.desktop.io.AtomicDocumentFileWriter;
import org.geogebra.desktop.main.LocalizationD;
import org.geogebra.desktop.util.LoggerD;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** R5 native archive, feature-off and Classic preservation authority. */
class G9U0R5NativeArchivePersistenceTest {

	private static final String BRANCH =
			SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY;
	private static final String DYNAMIC_DILATE_FIXTURE =
			"source/shared/common-jre/src/test/resources/org/geocedg/common/"
					+ "locus/g9u0-r5/fourSolutionsDynamicDilate.cedg";
	private static final String DYNAMIC_DILATE_FIXTURE_SHA256 =
			"13cde59d54a463413140007e793a50e8cb933cab21d4be286c9d76f6b2f713fe";
	private static final String[] TRANSFORM_LABELS = {
			"Lt", "Lr0", "Lr", "Lmp", "Lml", "Ld0", "Ld", "Lz"
	};

	@BeforeAll
	static void initializeDesktop() {
		AwtFactory.setPrototypeIfNull(new AwtFactoryD());
		if (Log.getLogger() == null) {
			Log.setLogger(new LoggerD());
		}
	}

	@Test
	void authorDynamicDilateFixtureUsesOneLiveParentAcrossFiniteFactorUpdates(
			@TempDir Path temporaryDirectory) throws Exception {
		AppGeoCeDG app = enabledApp();
		Path fixture = findRepositoryRoot().resolve(DYNAMIC_DILATE_FIXTURE);
		assertEquals(25_704, Files.size(fixture));
		assertEquals(DYNAMIC_DILATE_FIXTURE_SHA256,
				HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
						.digest(Files.readAllBytes(fixture))));
		assertTrue(app.loadFile(fixture.toFile(), false));
		GeoNumeric factor = (GeoNumeric) lookup(app, "k");
		GeoLocusV2 image = (GeoLocusV2) lookup(app, "a'");
		AlgoElement parent = image.getParentAlgorithm();
		PersistentGeoId factorId = persistentId(app, factor);
		PersistentGeoId imageId = persistentId(app, image);

		for (double value : List.of(1.0, 2.0, 0.5, -1.0, 0.0, 1.5,
				0.0, -0.75, 2.0)) {
			factor.setValue(value);
			factor.updateRepaint();
			assertSame(parent, image.getParentAlgorithm());
			assertEquals(factorId, persistentId(app, factor));
			assertEquals(imageId, persistentId(app, image));
			assertTrue(image.isDefined(), Double.toString(value));
			boolean collapsed = image.getSemanticDefinition().getBranches().stream()
					.anyMatch(branch -> branch.getProperties()
							.contains(BranchProperty.COLLAPSED_IMAGE));
			assertEquals(value == 0, collapsed, Double.toString(value));
		}

		CapturingErrorHandler editErrors = new CapturingErrorHandler();
		app.getKernel().getAlgebraProcessor().changeGeoElement(factor,
				"k=0.5", true, false, editErrors, null);
		assertTrue(editErrors.getErrors().isBlank(), editErrors.getErrors());
		assertNull(editErrors.getFailure());
		assertSame(factor, lookup(app, "k"));
		assertEquals(factorId, persistentId(app, factor));
		assertSame(parent, image.getParentAlgorithm());
		assertTrue(image.isDefined());

		assertEquals(0.5, factor.getDouble(), 0);
		assertSame(factor, lookup(app, "k"));
		assertSame(image, lookup(app, "a'"));
		assertTrue(image.isDefined());

		final String preFreeInputXml = app.getXML();
		final int preFreeInputGeoCount = app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size();
		final int preFreeInputIdentityCount = app.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getRecords().size();
		CapturingErrorHandler freeInputErrors = new CapturingErrorHandler();
		GeoElementND[] freeInput = app.getKernel().getAlgebraProcessor()
				.processAlgebraCommandNoExceptionHandling("k=0.25", false,
						freeInputErrors, false, null);
		assertNull(freeInput);
		assertTrue(freeInputErrors.getFailure() instanceof SpatialIdentityException);
		SpatialIdentityException identityFailure =
				(SpatialIdentityException) freeInputErrors.getFailure();
		assertEquals(SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
				identityFailure.getDiagnostic().getCode());
		assertEquals(0.5, factor.getDouble(), 0);
		assertSame(factor, lookup(app, "k"));
		assertSame(parent, image.getParentAlgorithm());
		assertEquals(imageId, persistentId(app, image));
		assertTrue(image.isDefined());
		assertEquals(preFreeInputXml, app.getXML());
		assertEquals(preFreeInputGeoCount, app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size());
		assertEquals(preFreeInputIdentityCount, app.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getRecords().size());

		Path reopenedPath = temporaryDirectory.resolve("r5-dynamic-dilate.cedg");
		assertTrue(gui(app).saveAsTo(reopenedPath.toFile()));
		AppGeoCeDG reopened = enabledApp();
		assertTrue(reopened.loadFile(reopenedPath.toFile(), false));
		GeoNumeric reopenedFactor = (GeoNumeric) lookup(reopened, "k");
		GeoLocusV2 reopenedImage = (GeoLocusV2) lookup(reopened, "a'");
		assertEquals(factorId, persistentId(reopened, reopenedFactor));
		assertEquals(imageId, persistentId(reopened, reopenedImage));
		for (double value : List.of(0.0, 1.0, -2.0, 0.0, 0.5)) {
			reopenedFactor.setValue(value);
			reopenedFactor.updateRepaint();
			assertTrue(reopenedImage.isDefined(), Double.toString(value));
		}
		CapturingErrorHandler reopenedEditErrors = new CapturingErrorHandler();
		reopened.getKernel().getAlgebraProcessor().changeGeoElement(reopenedFactor,
				"k=-0.25", true, false, reopenedEditErrors, null);
		assertTrue(reopenedEditErrors.getErrors().isBlank(),
				reopenedEditErrors.getErrors());
		assertNull(reopenedEditErrors.getFailure());
		assertSame(reopenedFactor, lookup(reopened, "k"));
		assertEquals(factorId, persistentId(reopened, reopenedFactor));
		assertEquals(imageId, persistentId(reopened, reopenedImage));
		assertTrue(reopenedImage.isDefined());
	}

	@Test
	void nativeCedgReopensEverySimilarityFamilyWithStableIdentityAndDependencies(
			@TempDir Path temporaryDirectory) throws Exception {
		AppGeoCeDG app = enabledApp();
		final GeoLocusV2 source = createParabola(app);
		eval(app, "v=Vector((0,0),(1,2))");
		eval(app, "a=pi/3");
		eval(app, "C=(3,-1)");
		eval(app, "axis:y=x+1");
		eval(app, "k=-2");
		String[] expressions = {
				"Lt=Translate(L,v)", "Lr0=Rotate(L,a)",
				"Lr=Rotate(L,a,C)", "Lmp=Reflect(L,C)",
				"Lml=Mirror(L,axis)", "Ld0=Dilate(L,k)",
				"Ld=Dilate(L,k,C)", "Lz=Dilate(L,0,C)"
		};
		for (String expression : expressions) {
			assertTrue(eval(app, expression) instanceof GeoLocusV2, expression);
		}
		Map<String, TransformSnapshot> before = captureTransforms(app);
		Set<PersistentGeoId> outputIds = before.values().stream()
				.map(TransformSnapshot::id)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(TRANSFORM_LABELS.length, outputIds.size());
		assertFalse(outputIds.contains(source.getPersistentLocusId()));
		GeoLocusV2 collapsed = (GeoLocusV2) lookup(app, "Lz");
		assertTrue(collapsed.getSemanticDefinition().getBranch(BRANCH)
				.getProperties().contains(BranchProperty.COLLAPSED_IMAGE));
		assertPoint(collapsed, 0.5, 3, -1);
		GeoLocusMetricResult collapsedMetric = (GeoLocusMetricResult) eval(app,
				"Mz=LocusLength(Lz)");
		assertEquals(0, collapsedMetric.getMetricResult().getMetricValue()
				.getFiniteValue().orElseThrow(), 0);

		Path nativeDocument = temporaryDirectory.resolve("r5-all-families.cedg");
		assertTrue(gui(app).saveAsTo(nativeDocument.toFile()));
		assertTrue(Files.size(nativeDocument) > 0);
		String archiveXml = readZipEntry(nativeDocument, "geogebra.xml");
		assertTrue(archiveXml.contains("app=\"classic\""));
		for (String command : List.of("Translate", "Rotate", "Mirror", "Dilate")) {
			assertTrue(archiveXml.contains("<command name=\"" + command + "\""),
					command);
		}
		for (String forbidden : List.of("renderVertices", "sampledPointCloud",
				"serializedCallback", "detachedMatrix")) {
			assertFalse(archiveXml.contains(forbidden), forbidden);
		}

		AppGeoCeDG reopened = enabledApp();
		assertTrue(reopened.loadFile(nativeDocument.toFile(), false));
		assertEquals(before, captureTransforms(reopened));
		assertEquals(GeoGebraConstants.CLASSIC_APPCODE,
				reopened.getConfig().getAppCode());
		GeoLocusV2 reopenedCollapsed = (GeoLocusV2) lookup(reopened, "Lz");
		assertTrue(reopenedCollapsed.getSemanticDefinition().getBranch(BRANCH)
				.getProperties().contains(BranchProperty.COLLAPSED_IMAGE));
		assertPoint(reopenedCollapsed, 0.5, 3, -1);
		GeoLocusMetricResult reopenedCollapsedMetric =
				(GeoLocusMetricResult) lookup(reopened, "Mz");
		assertEquals(0, reopenedCollapsedMetric.getMetricResult().getMetricValue()
				.getFiniteValue().orElseThrow(), 0);
		GeoLocusV2 reopenedDilation = (GeoLocusV2) lookup(reopened, "Ld");
		long revision = reopenedDilation.getSemanticRevision();
		GeoNumeric factor = (GeoNumeric) lookup(reopened, "k");
		factor.setValue(3);
		factor.updateCascade();
		assertTrue(reopenedDilation.getSemanticRevision() > revision);
		assertPoint(reopenedDilation, 0.5, -4.5, 2.75);
	}

	@Test
	void nativeCedgReopensDownstreamPointMetricAndTransformedIntersectionTokens(
			@TempDir Path temporaryDirectory) throws Exception {
		AppGeoCeDG app = enabledApp();
		final GeoLocusV2 source = createLine(app);
		eval(app, "dy=1");
		eval(app, "v=Vector((0,0),(0,dy))");
		GeoLocusV2 transformed = (GeoLocusV2) eval(app, "T=Translate(L,v)");
		GeoPoint semanticPoint = (GeoPoint) eval(app,
				"P=Point(T,\"" + BRANCH + "\",0.5)");
		GeoLocusMetricResult metric = (GeoLocusMetricResult) eval(app,
				"M=LocusLength(T)");
		eval(app, "target:x=0");
		GeoLocusIntersectionResult sourceResult =
				(GeoLocusIntersectionResult) eval(app, "Rs=Intersect(L,target)");
		GeoLocusIntersectionResult transformedResult =
				(GeoLocusIntersectionResult) eval(app, "Rt=Intersect(T,target)");
		Set<String> sourceTokens = tokens(sourceResult);
		Set<String> transformedTokens = tokens(transformedResult);
		assertEquals(1, sourceTokens.size());
		assertEquals(1, transformedTokens.size());
		assertTrue(java.util.Collections.disjoint(sourceTokens, transformedTokens));
		String selectedToken = transformedTokens.iterator().next();
		assertTrue(transformedResult.isPointAdmissible(selectedToken));
		GeoText tokenInput = new GeoText(app.getKernel().getConstruction(),
				selectedToken);
		tokenInput.setAuxiliaryObject(true);
		tokenInput.setEuclidianVisible(false);
		GeoPoint intersectionPoint = LocusV2PublicOperations
				.selectIntersectionPoint(app.getKernel().getConstruction(), "X",
						transformedResult, tokenInput);
		assertTrue(intersectionPoint.isDefined());
		Map<String, PersistentGeoId> identities = captureIds(app,
				"L", "T", "P", "M", "Rs", "Rt", "X");
		String sourceLedger = sourceResult.getTokenLedgerState();
		String transformedLedger = transformedResult.getTokenLedgerState();

		Path nativeDocument = temporaryDirectory.resolve("r5-consumers.cedg");
		assertTrue(gui(app).saveAsTo(nativeDocument.toFile()));
		AppGeoCeDG reopened = enabledApp();
		assertTrue(reopened.loadFile(nativeDocument.toFile(), false));
		assertIds(reopened, identities);
		GeoLocusV2 reopenedSource = (GeoLocusV2) lookup(reopened, "L");
		GeoLocusV2 reopenedTransform = (GeoLocusV2) lookup(reopened, "T");
		GeoPoint reopenedSemanticPoint = (GeoPoint) lookup(reopened, "P");
		GeoLocusMetricResult reopenedMetric =
				(GeoLocusMetricResult) lookup(reopened, "M");
		GeoLocusIntersectionResult reopenedSourceResult =
				(GeoLocusIntersectionResult) lookup(reopened, "Rs");
		GeoLocusIntersectionResult reopenedTransformResult =
				(GeoLocusIntersectionResult) lookup(reopened, "Rt");
		final GeoPoint reopenedIntersectionPoint =
				(GeoPoint) lookup(reopened, "X");
		assertEquals(sourceLedger, reopenedSourceResult.getTokenLedgerState());
		assertEquals(transformedLedger,
				reopenedTransformResult.getTokenLedgerState());
		assertEquals(sourceTokens, tokens(reopenedSourceResult));
		assertEquals(transformedTokens, tokens(reopenedTransformResult));
		assertNotEquals(reopenedSource.getPersistentLocusId(),
				reopenedTransform.getPersistentLocusId());
		assertTrue(reopenedSemanticPoint.isDefined());
		assertTrue(reopenedMetric.isDefined());
		assertTrue(reopenedIntersectionPoint.isDefined());
		assertEquals(selectedToken,
				((AlgoLocusIntersectionPointV2) reopenedIntersectionPoint
						.getParentAlgorithm()).getSelectedRootToken());

		GeoNumeric displacement = (GeoNumeric) lookup(reopened, "dy");
		displacement.setValue(0.5);
		displacement.updateCascade();
		assertEquals(0.5, reopenedSemanticPoint.getInhomY(), 1E-9);
		assertEquals(0.5, reopenedIntersectionPoint.getInhomY(), 1E-9);
		assertTrue(reopenedMetric.isDefined());
		assertTrue(reopenedTransformResult.isPointAdmissible(selectedToken));
		assertEquals(source.getSemanticDefinition().getBranches().size(),
				reopenedTransform.getSemanticDefinition().getBranches().size());
		final String directFinalLedger = reopenedTransformResult.getTokenLedgerState();

		AppGeoCeDG alternatePath = enabledApp();
		assertTrue(alternatePath.loadFile(nativeDocument.toFile(), false));
		GeoNumeric alternateDisplacement =
				(GeoNumeric) lookup(alternatePath, "dy");
		alternateDisplacement.setValue(0.25);
		alternateDisplacement.updateCascade();
		Path midPath = temporaryDirectory.resolve("r5-consumers-midpath.cedg");
		assertTrue(gui(alternatePath).saveAsTo(midPath.toFile()));

		AppGeoCeDG alternateReopened = enabledApp();
		assertTrue(alternateReopened.loadFile(midPath.toFile(), false));
		GeoNumeric finalDisplacement =
				(GeoNumeric) lookup(alternateReopened, "dy");
		finalDisplacement.setValue(0.5);
		finalDisplacement.updateCascade();
		assertIds(alternateReopened, identities);
		GeoLocusIntersectionResult alternateResult =
				(GeoLocusIntersectionResult) lookup(alternateReopened, "Rt");
		GeoPoint alternateSemanticPoint =
				(GeoPoint) lookup(alternateReopened, "P");
		GeoPoint alternateIntersectionPoint =
				(GeoPoint) lookup(alternateReopened, "X");
		assertEquals(transformedTokens, tokens(alternateResult));
		assertEquals(directFinalLedger, alternateResult.getTokenLedgerState());
		assertEquals(selectedToken,
				((AlgoLocusIntersectionPointV2) alternateIntersectionPoint
						.getParentAlgorithm()).getSelectedRootToken());
		assertEquals(reopenedSemanticPoint.getInhomX(),
				alternateSemanticPoint.getInhomX(), 0);
		assertEquals(reopenedSemanticPoint.getInhomY(),
				alternateSemanticPoint.getInhomY(), 0);
		assertEquals(reopenedIntersectionPoint.getInhomX(),
				alternateIntersectionPoint.getInhomX(), 0);
		assertEquals(reopenedIntersectionPoint.getInhomY(),
				alternateIntersectionPoint.getInhomY(), 0);
		assertTrue(alternateResult.isPointAdmissible(selectedToken));
	}

	@Test
	void featureOffAndClassicPreserveTransformedNativeDocumentWithoutCreation(
			@TempDir Path temporaryDirectory) throws Exception {
		AppGeoCeDG source = enabledApp();
		createLine(source);
		eval(source, "v=Vector((0,0),(1,2))");
		eval(source, "T=Translate(L,v)");
		Path nativeDocument = temporaryDirectory.resolve("r5-preservation.cedg");
		assertTrue(gui(source).saveAsTo(nativeDocument.toFile()));

		assertPreservesWithoutCreation(nativeDocument, temporaryDirectory,
				"feature-off", AppConfigGeoCeDG::new);
		assertPreservesWithoutCreation(nativeDocument, temporaryDirectory,
				"classic", AppConfigDefault::new);
	}

	private static void assertPreservesWithoutCreation(Path nativeDocument,
			Path temporaryDirectory, String route,
			Supplier<AppConfig> configFactory) throws Exception {
		AppDNoGui app = reopenHeadless(nativeDocument, configFactory.get());
		GeoLocusV2 loaded = (GeoLocusV2) lookup(app, "T");
		GeoVec3D vector = (GeoVec3D) lookup(app, "v");
		assertTrue(loaded.isDefined());
		assertFalse(RuntimeFeatureService.mayCreateLocusV2(
				app.getKernel().getConstruction()));
		assertEquals(GeoGebraConstants.CLASSIC_APPCODE,
				app.getConfig().getAppCode());
		String preservedXml = app.getXML();
		final int geoCount = app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size();
		final int identityCount = app.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getRecords().size();

		assertThrows(IllegalArgumentException.class,
				() -> LocusV2PublicOperations.translate(
						app.getKernel().getConstruction(), "ForbiddenDirect",
						loaded, vector));
		CapturingErrorHandler errors = new CapturingErrorHandler();
		GeoElementND[] output = null;
		RuntimeException thrown = null;
		try {
			output = app.getKernel().getAlgebraProcessor()
					.processAlgebraCommandNoExceptionHandling(
							"ForbiddenCommand=Translate(T,v)", false, errors,
							false, null);
		} catch (RuntimeException exception) {
			thrown = exception;
		}
		assertTrue(output == null || output.length == 0, route);
		assertTrue(thrown != null || errors.getFailure() != null
				|| !errors.getErrors().isBlank(), route);
		assertNull(app.getKernel().lookupLabel("ForbiddenDirect"));
		assertNull(app.getKernel().lookupLabel("ForbiddenCommand"));
		assertEquals(preservedXml, app.getXML());
		assertEquals(geoCount, app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size());
		assertEquals(identityCount, app.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getRecords().size());

		Path resaved = temporaryDirectory.resolve(route + "-resaved.cedg");
		writeHeadless(app, resaved);
		AppDNoGui reopened = reopenHeadless(resaved, configFactory.get());
		assertEquals(preservedXml, reopened.getXML());
		assertTrue(((GeoLocusV2) lookup(reopened, "T")).isDefined());
		assertFalse(RuntimeFeatureService.mayCreateLocusV2(
				reopened.getKernel().getConstruction()));
	}

	private static Map<String, TransformSnapshot> captureTransforms(App app) {
		Map<String, TransformSnapshot> snapshots = new LinkedHashMap<>();
		SpatialIdentityRegistry registry = app.getKernel().getConstruction()
				.getSpatialIdentityRegistry();
		for (String label : TRANSFORM_LABELS) {
			GeoLocusV2 locus = (GeoLocusV2) lookup(app, label);
			PersistentGeoId id = persistentId(app, locus);
			GeoIdentityRecord record = registry.getGeoRecord(id);
			LocusPoint2D point = evaluate(locus, 0.5).getPoint();
			assertNotNull(point, label);
			TransformSnapshot replaced = snapshots.put(label,
					new TransformSnapshot(id,
							locus.getParentAlgorithm().getClassName(),
							List.copyOf(record.getDependencies()),
							Double.doubleToLongBits(point.getX()),
							Double.doubleToLongBits(point.getY())));
			assertNull(replaced);
		}
		return Map.copyOf(snapshots);
	}

	private static Map<String, PersistentGeoId> captureIds(App app,
			String... labels) {
		Map<String, PersistentGeoId> identities = new LinkedHashMap<>();
		for (String label : labels) {
			identities.put(label, persistentId(app, lookup(app, label)));
		}
		return Map.copyOf(identities);
	}

	private static void assertIds(App app,
			Map<String, PersistentGeoId> expected) {
		SpatialIdentityRegistry registry = app.getKernel().getConstruction()
				.getSpatialIdentityRegistry();
		for (Map.Entry<String, PersistentGeoId> entry : expected.entrySet()) {
			GeoElement geo = lookup(app, entry.getKey());
			assertEquals(entry.getValue(), registry.getPersistentGeoId(geo),
					entry.getKey());
			assertEquals(geo, registry.getGeo(entry.getValue()), entry.getKey());
		}
	}

	private static Set<String> tokens(GeoLocusIntersectionResult result) {
		assertNotNull(result.getIntersectionResult());
		return result.getIntersectionResult().getFiniteSolutions().stream()
				.map(solution -> solution.getIdentity().getRootToken())
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	private static GeoLocusV2 createParabola(App app) {
		eval(app, "s=0");
		eval(app, "Q=(s,s^2)");
		eval(app, "D={false,{-2,2,true,true}}");
		return (GeoLocusV2) eval(app, "L=LocusV2(Q,s,D)");
	}

	private static GeoLocusV2 createLine(App app) {
		eval(app, "s=0");
		eval(app, "Q=(s,0)");
		eval(app, "D={false,{-2,2,true,true}}");
		return (GeoLocusV2) eval(app, "L=LocusV2(Q,s,D)");
	}

	private static void assertPoint(GeoLocusV2 locus, double parameter,
			double expectedX, double expectedY) {
		LocusEvaluation2D evaluation = evaluate(locus, parameter);
		assertTrue(evaluation.isValid(), evaluation.getDiagnostic());
		assertEquals(expectedX, evaluation.getPoint().getX(), 1E-10);
		assertEquals(expectedY, evaluation.getPoint().getY(), 1E-10);
	}

	private static LocusEvaluation2D evaluate(GeoLocusV2 locus,
			double parameter) {
		try (LocusEvaluationSession2D session =
				LocusEvaluationSession2D.reference()) {
			return locus.evaluate(BRANCH, parameter, session);
		}
	}

	private static GeoElement eval(App app, String command) {
		GeoElementND[] output = app.getKernel().getAlgebraProcessor()
				.processAlgebraCommand(command, false);
		assertNotNull(output, command);
		assertTrue(output.length > 0, command);
		return output[0].toGeoElement();
	}

	private static GeoElement lookup(App app, String label) {
		GeoElement geo = app.getKernel().lookupLabel(label);
		assertNotNull(geo, label);
		return geo;
	}

	private static PersistentGeoId persistentId(App app, GeoElement geo) {
		PersistentGeoId id = app.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getPersistentGeoId(geo);
		assertNotNull(id, geo.getLabelSimple());
		return id;
	}

	private static AppGeoCeDG enabledApp() {
		AppGeoCeDG app = new AppGeoCeDG(new CommandLineArguments(new String[] {
				"--silent", "--enableLocusV2=true"}), new JPanel());
		app.setErrorDialogsActive(false);
		return app;
	}

	private static Path findRepositoryRoot() {
		Path candidate = Path.of("").toAbsolutePath().normalize();
		while (candidate != null) {
			if (Files.isRegularFile(candidate.resolve("AGENTS.md"))
					&& Files.isDirectory(candidate.resolve("geocedg"))) {
				return candidate;
			}
			candidate = candidate.getParent();
		}
		throw new IllegalStateException("GeoCeDG repository root not found");
	}

	private static GuiManagerGeoCeDG gui(AppGeoCeDG app) {
		return (GuiManagerGeoCeDG) app.getGuiManager();
	}

	private static AppDNoGui newHeadless(AppConfig config) {
		Log previousLogger = Log.getLogger();
		try {
			return new AppDNoGui(new LocalizationD(3), true, config);
		} finally {
			Log.setLogger(previousLogger);
		}
	}

	private static AppDNoGui reopenHeadless(Path source, AppConfig config)
			throws Exception {
		AppDNoGui reopened = newHeadless(config);
		assertTrue(GFileHandler.loadXML(reopened, Files.newInputStream(source),
				false));
		return reopened;
	}

	private static void writeHeadless(AppDNoGui app, Path target)
			throws Exception {
		AtomicDocumentFileWriter.write(target, temporary ->
				((MyXMLioJre) app.getXMLio())
						.writeGeoGebraFile(temporary.toFile()));
	}

	private static String readZipEntry(Path archivePath, String entryName)
			throws Exception {
		try (ZipFile archive = new ZipFile(archivePath.toFile())) {
			ZipEntry entry = archive.getEntry(entryName);
			assertNotNull(entry, entryName);
			return new String(archive.getInputStream(entry).readAllBytes(),
					StandardCharsets.UTF_8);
		}
	}

	private record TransformSnapshot(PersistentGeoId id, GetCommand command,
			List<PersistentGeoId> dependencies, long xBits, long yBits) {
	}

	private static final class CapturingErrorHandler extends ErrorAccumulator {
		private Throwable failure;

		@Override
		public void log(Throwable throwable) {
			failure = throwable;
		}

		private Throwable getFailure() {
			return failure;
		}
	}
}
