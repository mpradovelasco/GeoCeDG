/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.IdentityStatus;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.awt.AwtFactoryD;
import org.geogebra.desktop.util.LoggerD;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** G9U0-R4 native archive persistence for newly admitted public roots. */
class G9U0R4NativeArchivePersistenceTest {

	private static final String FIXTURE =
			"source/shared/common-jre/src/test/resources/org/geocedg/common/"
					+ "locus/g9u0-r2/locusFromMidpoint.cedg";

	@BeforeAll
	static void initializeDesktop() {
		AwtFactory.setPrototypeIfNull(new AwtFactoryD());
		if (Log.getLogger() == null) {
			Log.setLogger(new LoggerD());
		}
	}

	@Test
	void newMidpointCircleTokensSurviveNativeCedgSaveAndReopen(
			@TempDir Path temporaryDirectory) throws Exception {
		AppGeoCeDG app = enabledApp();
		Path fixture = findRepositoryRoot().resolve(FIXTURE);
		assertTrue(app.loadFile(fixture.toFile(), false));
		GeoLocusIntersectionResult rich = (GeoLocusIntersectionResult) eval(app,
				"R4Archive=Intersect(a,c)");
		List<LocusIntersectionSolution2D> roots = rich.getIntersectionResult()
				.getFiniteSolutions();
		assertEquals(2, roots.size());
		Map<PersistentGeoId, String> pointTokens = new LinkedHashMap<>();
		for (int index = 0; index < roots.size(); index++) {
			String token = roots.get(index).getIdentity().getRootToken();
			assertTrue(rich.isPointAdmissible(token));
			Construction construction = app.getKernel().getConstruction();
			GeoText tokenInput = new GeoText(construction, token);
			tokenInput.setAuxiliaryObject(true);
			tokenInput.setEuclidianVisible(false);
			GeoPoint point = LocusV2PublicOperations.selectIntersectionPoint(
					construction, "R4ArchivePoint" + index, rich, tokenInput);
			assertTrue(point.isDefined());
			pointTokens.put(persistentId(app, point), token);
		}
		String ledger = rich.getTokenLedgerState();
		Path nativeDocument = temporaryDirectory.resolve("r4-initial-roots.cedg");
		assertTrue(((GuiManagerGeoCeDG) app.getGuiManager())
				.saveAsTo(nativeDocument.toFile()));
		assertTrue(Files.size(nativeDocument) > 0);

		AppGeoCeDG reopened = enabledApp();
		assertTrue(reopened.loadFile(nativeDocument.toFile(), false));
		GeoLocusIntersectionResult reopenedRich =
				(GeoLocusIntersectionResult) reopened.getKernel()
						.lookupLabel("R4Archive");
		assertNotNull(reopenedRich);
		assertEquals(ledger, reopenedRich.getTokenLedgerState());
		assertPersistedPoints(reopened, reopenedRich, pointTokens);

		GeoPoint radiusPoint = (GeoPoint) reopened.getKernel().lookupLabel("B");
		assertNotNull(radiusPoint);
		double startX = radiusPoint.getInhomX();
		double startY = radiusPoint.getInhomY();
		for (int step = 1; step <= 64; step++) {
			radiusPoint.setCoords(startX + 0.5 * step / 64, startY, 1);
			radiusPoint.updateCascade();
		}
		assertPersistedPoints(reopened, reopenedRich, pointTokens);
	}

	@Test
	void nativeCedgReopenPathMatchesDirectDeterministicBinding(
			@TempDir Path temporaryDirectory) throws Exception {
		Path fixture = findRepositoryRoot().resolve(FIXTURE);
		Path seedDocument = temporaryDirectory.resolve("r4-path-seed.cedg");
		AppGeoCeDG seed = enabledApp();
		assertTrue(seed.loadFile(fixture.toFile(), false));
		GeoLocusIntersectionResult seedRich =
				(GeoLocusIntersectionResult) eval(seed,
						"R4Archive=Intersect(a,c)");
		List<LocusIntersectionSolution2D> roots = seedRich.getIntersectionResult()
				.getFiniteSolutions();
		assertEquals(2, roots.size());
		Map<PersistentGeoId, String> pointTokens = new LinkedHashMap<>();
		for (int index = 0; index < roots.size(); index++) {
			LocusIntersectionSolution2D root = roots.get(index);
			String token = root.getIdentity().getRootToken();
			Construction construction = seed.getKernel().getConstruction();
			GeoText tokenInput = new GeoText(construction, token);
			tokenInput.setAuxiliaryObject(true);
			tokenInput.setEuclidianVisible(false);
			GeoPoint point = LocusV2PublicOperations.selectIntersectionPoint(
					construction, "R4ArchivePoint" + index, seedRich, tokenInput);
			assertTrue(point.isDefined());
			pointTokens.put(persistentId(seed, point), token);
		}
		assertTrue(((GuiManagerGeoCeDG) seed.getGuiManager())
				.saveAsTo(seedDocument.toFile()));

		AppGeoCeDG direct = enabledApp();
		assertTrue(direct.loadFile(seedDocument.toFile(), false));
		GeoPoint directRadius = (GeoPoint) direct.getKernel().lookupLabel("B");
		double initialX = directRadius.getInhomX();
		double initialY = directRadius.getInhomY();
		setPoint(directRadius, initialX + 0.05, initialY);
		ArchiveSnapshot directSnapshot = captureSnapshot(direct, pointTokens);

		AppGeoCeDG staged = enabledApp();
		assertTrue(staged.loadFile(seedDocument.toFile(), false));
		GeoPoint stagedRadius = (GeoPoint) staged.getKernel().lookupLabel("B");
		setPoint(stagedRadius, initialX + 0.02, initialY);
		Path intermediate = temporaryDirectory.resolve("r4-path-intermediate.cedg");
		assertTrue(((GuiManagerGeoCeDG) staged.getGuiManager())
				.saveAsTo(intermediate.toFile()));

		AppGeoCeDG reopened = enabledApp();
		assertTrue(reopened.loadFile(intermediate.toFile(), false));
		GeoPoint reopenedRadius =
				(GeoPoint) reopened.getKernel().lookupLabel("B");
		setPoint(reopenedRadius, initialX + 0.05, initialY);
		ArchiveSnapshot reopenedSnapshot = captureSnapshot(reopened, pointTokens);

		assertEquals(directSnapshot, reopenedSnapshot);
	}

	@Test
	void nativeCedgPreservesDormantAndReactivatedExistingPoints(
			@TempDir Path temporaryDirectory) throws Exception {
		AppGeoCeDG app = enabledApp();
		eval(app, "reactivationParameter=0");
		eval(app, "ReactivationGenerator=(reactivationParameter,"
				+ "(reactivationParameter^2-1)*(reactivationParameter^2-4))");
		eval(app, "reactivationBound=1.5");
		eval(app, "reactivationDomain={false,{-reactivationBound,"
				+ "reactivationBound,true,true}}");
		eval(app, "reactivationLocus=LocusV2(ReactivationGenerator,"
				+ "reactivationParameter,reactivationDomain)");
		eval(app, "reactivationAxis:y=0");
		GeoLocusIntersectionResult rich = (GeoLocusIntersectionResult) eval(app,
				"reactivationResult=Intersect(reactivationLocus,reactivationAxis)");
		assertEquals(2, rich.getIntersectionResult().getFiniteSolutions().size());
		Map<PersistentGeoId, String> pointTokens = new LinkedHashMap<>();
		for (int index = 0; index < 2; index++) {
			LocusIntersectionSolution2D root = rich.getIntersectionResult()
					.getFiniteSolutions().get(index);
			String token = root.getIdentity().getRootToken();
			Construction construction = app.getKernel().getConstruction();
			GeoText tokenInput = new GeoText(construction, token);
			tokenInput.setAuxiliaryObject(true);
			tokenInput.setEuclidianVisible(false);
			GeoPoint point = LocusV2PublicOperations.selectIntersectionPoint(
					construction, "reactivationPoint" + index, rich, tokenInput);
			assertTrue(point.isDefined());
			pointTokens.put(persistentId(app, point), token);
		}
		int constructionSize = app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size();
		GeoNumeric bound = (GeoNumeric) app.getKernel()
				.lookupLabel("reactivationBound");
		bound.setValue(3);
		bound.updateCascade();
		assertEquals(4, rich.getIntersectionResult().getFiniteSolutions().size());
		assertEquals(constructionSize, app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size());
		assertDormantPoints(app, pointTokens);

		Path dormantDocument = temporaryDirectory.resolve("r4-dormant.cedg");
		assertTrue(((GuiManagerGeoCeDG) app.getGuiManager())
				.saveAsTo(dormantDocument.toFile()));
		AppGeoCeDG reopenedDormant = enabledApp();
		assertTrue(reopenedDormant.loadFile(dormantDocument.toFile(), false));
		GeoLocusIntersectionResult reopenedRich =
				(GeoLocusIntersectionResult) reopenedDormant.getKernel()
						.lookupLabel("reactivationResult");
		assertNotNull(reopenedRich);
		assertEquals(4,
				reopenedRich.getIntersectionResult().getFiniteSolutions().size());
		assertDormantPoints(reopenedDormant, pointTokens);

		GeoNumeric reopenedBound = (GeoNumeric) reopenedDormant.getKernel()
				.lookupLabel("reactivationBound");
		reopenedBound.setValue(1.5);
		reopenedBound.updateCascade();
		assertEquals(2,
				reopenedRich.getIntersectionResult().getFiniteSolutions().size());
		assertPersistedPoints(reopenedDormant, reopenedRich, pointTokens);
		assertEquals(constructionSize, reopenedDormant.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size());

		Path reactivatedDocument = temporaryDirectory.resolve(
				"r4-reactivated.cedg");
		assertTrue(((GuiManagerGeoCeDG) reopenedDormant.getGuiManager())
				.saveAsTo(reactivatedDocument.toFile()));
		AppGeoCeDG reopenedActive = enabledApp();
		assertTrue(reopenedActive.loadFile(reactivatedDocument.toFile(), false));
		GeoLocusIntersectionResult activeRich =
				(GeoLocusIntersectionResult) reopenedActive.getKernel()
						.lookupLabel("reactivationResult");
		assertNotNull(activeRich);
		assertPersistedPoints(reopenedActive, activeRich, pointTokens);
		assertEquals(constructionSize, reopenedActive.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size());
	}

	private static void assertPersistedPoints(AppGeoCeDG app,
			GeoLocusIntersectionResult rich,
			Map<PersistentGeoId, String> pointTokens) {
		SpatialIdentityRegistry registry = app.getKernel().getConstruction()
				.getSpatialIdentityRegistry();
		for (Map.Entry<PersistentGeoId, String> entry : pointTokens.entrySet()) {
			GeoPoint point = (GeoPoint) registry.getGeo(entry.getKey());
			assertNotNull(point);
			assertTrue(point.isDefined());
			LocusIntersectionSolution2D current = rich
					.findExactPointAdmissibleSolution(entry.getValue())
					.orElseThrow();
			assertEquals(current.getEvaluatedPoint().getX(), point.getInhomX(),
					1E-9);
			assertEquals(current.getEvaluatedPoint().getY(), point.getInhomY(),
					1E-9);
			assertEquals(entry.getValue(),
					((AlgoLocusIntersectionPointV2) point.getParentAlgorithm())
							.getSelectedRootToken());
			GeoText tokenInput = (GeoText) point.getParentAlgorithm().getInput(1);
			assertEquals(entry.getValue(), tokenInput.getTextString());
			assertTrue(tokenInput.isAuxiliaryObject());
			assertFalse(tokenInput.isEuclidianVisible());
		}
	}

	private static void assertDormantPoints(AppGeoCeDG app,
			Map<PersistentGeoId, String> pointTokens) {
		SpatialIdentityRegistry registry = app.getKernel().getConstruction()
				.getSpatialIdentityRegistry();
		for (Map.Entry<PersistentGeoId, String> entry : pointTokens.entrySet()) {
			GeoPoint point = (GeoPoint) registry.getGeo(entry.getKey());
			assertNotNull(point);
			assertFalse(point.isDefined());
			AlgoLocusIntersectionPointV2 algorithm =
					(AlgoLocusIntersectionPointV2) point.getParentAlgorithm();
			assertEquals(entry.getValue(), algorithm.getSelectedRootToken());
			assertEquals(entry.getValue(), algorithm.getEffectiveRootToken());
			GeoText tokenInput = (GeoText) algorithm.getInput(1);
			assertEquals(entry.getValue(), tokenInput.getTextString());
			assertTrue(tokenInput.isAuxiliaryObject());
			assertFalse(tokenInput.isEuclidianVisible());
		}
	}

	private static ArchiveSnapshot captureSnapshot(AppGeoCeDG app,
			Map<PersistentGeoId, String> pointTokens) {
		GeoLocusIntersectionResult rich =
				(GeoLocusIntersectionResult) app.getKernel()
						.lookupLabel("R4Archive");
		assertNotNull(rich);
		GeoPoint radius = (GeoPoint) app.getKernel().lookupLabel("B");
		Map<String, ArchiveBinding> bindings = new LinkedHashMap<>();
		SpatialIdentityRegistry registry = app.getKernel().getConstruction()
				.getSpatialIdentityRegistry();
		for (Map.Entry<PersistentGeoId, String> entry : pointTokens.entrySet()) {
			GeoPoint point = (GeoPoint) registry.getGeo(entry.getKey());
			assertNotNull(point);
			AlgoLocusIntersectionPointV2 algorithm =
					(AlgoLocusIntersectionPointV2) point.getParentAlgorithm();
			LocusIntersectionSolution2D root = rich
					.findExactPointAdmissibleSolution(entry.getValue()).orElseThrow();
			ArchiveBinding duplicate = bindings.put(entry.getKey().toExternalForm(),
					new ArchiveBinding(point.isDefined(), entry.getValue(),
							algorithm.getSelectedRootToken(),
							algorithm.getEffectiveRootToken(),
							((GeoText) algorithm.getInput(1)).getTextString(),
							root.getIdentity().getExplicitContinuationKey()
									.orElseThrow(),
							root.getIdentity().getEstablishedBranchLineage(),
							root.getRevisionEvidence().getResolvedValidComponentKey(),
							root.getRevisionEvidence().getCurrentRootGerm()
									.orElseThrow(),
							root.getIdentity().getIdentityStatus(),
							Double.doubleToLongBits(root.getRevisionEvidence()
									.getSemanticParameter()),
							Double.doubleToLongBits(point.getInhomX()),
							Double.doubleToLongBits(point.getInhomY())));
			assertEquals(null, duplicate);
		}
		return new ArchiveSnapshot(rich.getTokenLedgerState(),
				Double.doubleToLongBits(radius.getInhomX()),
				Double.doubleToLongBits(radius.getInhomY()), Map.copyOf(bindings));
	}

	private static void setPoint(GeoPoint point, double x, double y) {
		point.setCoords(x, y, 1);
		point.updateCascade();
	}

	private record ArchiveSnapshot(String ledger, long radiusXBits,
			long radiusYBits, Map<String, ArchiveBinding> bindings) {
	}

	private record ArchiveBinding(boolean defined, String expectedToken,
			String selectedToken, String effectiveToken, String tokenInput,
			String explicitContinuationKey, String branchLineage,
			String component, String currentRootGerm, IdentityStatus identityStatus,
			long parameterBits, long pointXBits, long pointYBits) {
	}

	private static AppGeoCeDG enabledApp() {
		AppGeoCeDG app = new AppGeoCeDG(new CommandLineArguments(new String[] {
				"--silent", "--enableLocusV2=true"}), new JPanel());
		app.setErrorDialogsActive(false);
		return app;
	}

	private static GeoElement eval(AppGeoCeDG app, String command) {
		var output = app.getKernel().getAlgebraProcessor()
				.processAlgebraCommand(command, false);
		assertNotNull(output, command);
		assertTrue(output.length > 0, command);
		return output[0].toGeoElement();
	}

	private static PersistentGeoId persistentId(AppGeoCeDG app,
			GeoElement geo) {
		PersistentGeoId id = app.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getPersistentGeoId(geo);
		assertNotNull(id);
		return id;
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
}
