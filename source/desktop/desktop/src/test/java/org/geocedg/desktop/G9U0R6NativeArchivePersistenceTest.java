/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JPanel;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddressState2D;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionQuery2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResolver2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResult2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionStatus2D;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.GeoGebraConstants;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.jre.io.MyXMLioJre;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Native R6 interactive semantic-point persistence and preservation authority. */
class G9U0R6NativeArchivePersistenceTest {

	private static final String SPLINE_BRANCH = "spline-v2/main";
	private static final LocusPointInteractionResolver2D RESOLVER =
			new LocusPointInteractionResolver2D();

	@BeforeAll
	static void initializeDesktop() {
		AwtFactory.setPrototypeIfNull(new AwtFactoryD());
		if (Log.getLogger() == null) {
			Log.setLogger(new LoggerD());
		}
	}

	@Test
	void nativeCedgReopensOwnedTransformedPointAndMovesItAgain(
			@TempDir Path temporaryDirectory) throws Exception {
		AppGeoCeDG app = enabledApp();
		GeoLocusV2 transformed = createTranslatedSpline(app);
		LocusPointInteractionResult2D initial = resolve(transformed, 3, 4.05,
				0.1);
		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				initial.getStatus());
		GeoPoint point = LocusV2PublicOperations.createInteractiveSemanticPoint(
				app.getKernel().getConstruction(), "P", transformed,
				initial.getUniqueCandidate());
		InteractionSnapshot original = snapshot(app, point);
		assertEquals(initial.getUniqueCandidate().getAddress(), original.address());
		assertEquals(SPLINE_BRANCH, original.address().getBranchKey());
		assertEquals(0.5, original.address().getCanonicalParameter(), 1E-9);
		assertEquals(3, point.getInhomX(), 1E-9);
		assertEquals(4, point.getInhomY(), 1E-9);

		Path nativeDocument = temporaryDirectory.resolve("r6-interactive.cedg");
		assertTrue(gui(app).saveAsTo(nativeDocument.toFile()));
		String archiveXml = readZipEntry(nativeDocument, "geogebra.xml");
		assertTrue(archiveXml.contains("app=\"classic\""));
		assertTrue(archiveXml.contains(
				LocusV2PublicOperations.INTERACTION_POINT_OUTPUT_ROLE));

		AppGeoCeDG reopened = enabledApp();
		assertTrue(reopened.loadFile(nativeDocument.toFile(), false));
		GeoPoint reopenedPoint = assertInstanceOf(GeoPoint.class,
				lookup(reopened, "P"));
		InteractionSnapshot reopenedState = snapshot(reopened, reopenedPoint);
		assertEquals(original, reopenedState);
		assertEquals(transformed.getPersistentLocusId(),
				reopenedState.address().getSourceLocusId());

		LocusPointInteractionResult2D moved =
				LocusV2PublicOperations.moveInteractiveSemanticPoint(reopenedPoint,
						4, 4.02, policy(0.1));
		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				moved.getStatus());
		assertEquals(original.pointId(), persistentId(reopened, reopenedPoint));
		assertEquals(original.branchInputId(), persistentId(reopened,
				semanticParent(reopenedPoint).getBranchInput()));
		assertEquals(original.parameterInputId(), persistentId(reopened,
				semanticParent(reopenedPoint).getParameterInput().toGeoElement()));
		assertNotEquals(original.address(),
				semanticParent(reopenedPoint).getSemanticAddress());
		assertEquals(moved.getUniqueCandidate().getAddress(),
				semanticParent(reopenedPoint).getSemanticAddress());
		assertEquals(0.75, semanticParent(reopenedPoint).getSemanticAddress()
				.getCanonicalParameter(), 1E-9);
		assertEquals(4, reopenedPoint.getInhomX(), 1E-9);
		assertEquals(4, reopenedPoint.getInhomY(), 1E-9);

		GeoNumeric displacement = assertInstanceOf(GeoNumeric.class,
				lookup(reopened, "dy"));
		LocusSemanticAddress2D movedAddress = semanticParent(reopenedPoint)
				.getSemanticAddress();
		displacement.setValue(5);
		displacement.updateCascade();
		assertTrue(reopenedPoint.isDefined());
		assertEquals(4, reopenedPoint.getInhomX(), 1E-9);
		assertEquals(5, reopenedPoint.getInhomY(), 1E-9);
		assertEquals(movedAddress,
				semanticParent(reopenedPoint).getSemanticAddress());

		InteractionSnapshot updated = snapshot(reopened, reopenedPoint);
		Path updatedDocument = temporaryDirectory.resolve(
				"r6-interactive-updated.cedg");
		assertTrue(gui(reopened).saveAsTo(updatedDocument.toFile()));
		AppGeoCeDG updatedReopen = enabledApp();
		assertTrue(updatedReopen.loadFile(updatedDocument.toFile(), false));
		GeoPoint twiceReopenedPoint = assertInstanceOf(GeoPoint.class,
				lookup(updatedReopen, "P"));
		assertEquals(updated, snapshot(updatedReopen, twiceReopenedPoint));
		assertEquals(5, twiceReopenedPoint.getInhomY(), 1E-9);
	}

	@Test
	void featureOffAndClassicPreserveOwnedPointWithoutCreationAuthority(
			@TempDir Path temporaryDirectory) throws Exception {
		AppGeoCeDG source = enabledApp();
		GeoLocusV2 transformed = createTranslatedSpline(source);
		GeoPoint point = LocusV2PublicOperations.createInteractiveSemanticPoint(
				source.getKernel().getConstruction(), "P", transformed,
				resolve(transformed, 3, 4.05, 0.1).getUniqueCandidate());
		InteractionSnapshot expected = snapshot(source, point);
		Path nativeDocument = temporaryDirectory.resolve("r6-preservation.cedg");
		assertTrue(gui(source).saveAsTo(nativeDocument.toFile()));

		assertPreservesWithoutCreation(nativeDocument, temporaryDirectory,
				"feature-off", AppConfigGeoCeDG::new, expected);
		assertPreservesWithoutCreation(nativeDocument, temporaryDirectory,
				"classic", AppConfigDefault::new, expected);
	}

	@Test
	void nativeCedgReopensDormantAddressAndRecoversTheSamePoint(
			@TempDir Path temporaryDirectory) throws Exception {
		AppGeoCeDG app = enabledApp();
		eval(app, "s=0");
		eval(app, "Q=(s,0)");
		GeoNumeric lower = assertInstanceOf(GeoNumeric.class,
				eval(app, "a=-2"));
		GeoNumeric upper = assertInstanceOf(GeoNumeric.class,
				eval(app, "b=-1"));
		eval(app, "D={false,{a,b,true,true}}");
		GeoLocusV2 locus = assertInstanceOf(GeoLocusV2.class,
				eval(app, "L=LocusV2(Q,s,D)"));
		GeoPoint point = LocusV2PublicOperations.createInteractiveSemanticPoint(
				app.getKernel().getConstruction(), "P", locus,
				resolve(locus, -1.5, 0, 0.1).getUniqueCandidate());
		final InteractionSnapshot active = snapshot(app, point);
		String encodedAddress = semanticParent(point).getBranchInput()
				.getTextString();

		lower.setValue(1);
		upper.setValue(2);
		GeoElement.updateCascade(List.of(lower, upper));
		assertFalse(point.isDefined());
		assertEquals(active.address(), semanticParent(point).getSemanticAddress());
		assertNull(semanticParent(point).getCurrentSemanticAddress());
		assertEquals(encodedAddress,
				semanticParent(point).getBranchInput().getTextString());

		Path dormantDocument = temporaryDirectory.resolve("r6-dormant.cedg");
		assertTrue(gui(app).saveAsTo(dormantDocument.toFile()));
		AppGeoCeDG reopened = enabledApp();
		assertTrue(reopened.loadFile(dormantDocument.toFile(), false));
		GeoPoint reopenedPoint = assertInstanceOf(GeoPoint.class,
				lookup(reopened, "P"));
		AlgoSemanticLocusPoint2D reopenedParent = semanticParent(reopenedPoint);
		assertFalse(reopenedPoint.isDefined());
		assertEquals(active.address(), reopenedParent.getSemanticAddress());
		assertNull(reopenedParent.getCurrentSemanticAddress());
		assertEquals(encodedAddress, reopenedParent.getBranchInput().getTextString());
		assertEquals(active.pointId(), persistentId(reopened, reopenedPoint));
		assertEquals(active.branchInputId(), persistentId(reopened,
				reopenedParent.getBranchInput()));
		assertEquals(active.parameterInputId(), persistentId(reopened,
				reopenedParent.getParameterInput().toGeoElement()));
		assertTrue(reopenedParent.getBranchInput().isAuxiliaryObject());
		assertFalse(reopenedParent.getBranchInput().isEuclidianVisible());
		GeoElement reopenedParameter = reopenedParent.getParameterInput()
				.toGeoElement();
		assertTrue(reopenedParameter.isAuxiliaryObject());
		assertFalse(reopenedParameter.isEuclidianVisible());

		GeoNumeric reopenedLower = assertInstanceOf(GeoNumeric.class,
				lookup(reopened, "a"));
		GeoNumeric reopenedUpper = assertInstanceOf(GeoNumeric.class,
				lookup(reopened, "b"));
		reopenedLower.setValue(-2);
		reopenedUpper.setValue(-1);
		GeoElement.updateCascade(List.of(reopenedLower, reopenedUpper));
		assertTrue(reopenedPoint.isDefined());
		assertEquals(active.pointId(), persistentId(reopened, reopenedPoint));
		assertEquals(active.address(), reopenedParent.getSemanticAddress());
		assertEquals(-1.5, reopenedPoint.getInhomX(), 1E-9);
		assertEquals(0, reopenedPoint.getInhomY(), 1E-9);
	}

	private static void assertPreservesWithoutCreation(Path nativeDocument,
			Path temporaryDirectory, String route,
			Supplier<AppConfig> configFactory, InteractionSnapshot expected)
			throws Exception {
		AppDNoGui app = reopenHeadless(nativeDocument, configFactory.get());
		GeoLocusV2 transformed = assertInstanceOf(GeoLocusV2.class,
				lookup(app, "T"));
		GeoPoint point = assertInstanceOf(GeoPoint.class, lookup(app, "P"));
		assertEquals(expected, snapshot(app, point), route);
		assertFalse(RuntimeFeatureService.mayCreateLocusV2(
				app.getKernel().getConstruction()), route);
		assertEquals(GeoGebraConstants.CLASSIC_APPCODE,
				app.getConfig().getAppCode(), route);
		String preservedXml = app.getXML();
		int geoCount = app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size();
		final int identityCount = app.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getRecords().size();
		LocusPointInteractionResult2D result = resolve(transformed,
				point.getInhomX(), point.getInhomY(), 0.1);
		assertEquals(LocusPointInteractionStatus2D.UNIQUE_ADMISSIBLE_PREIMAGE,
				result.getStatus(), route);

		assertThrows(IllegalArgumentException.class,
				() -> LocusV2PublicOperations.moveInteractiveSemanticPoint(point,
						point.getInhomX() + 0.01, point.getInhomY(), policy(0.1)),
				route);
		assertEquals(expected, snapshot(app, point), route);
		assertThrows(IllegalArgumentException.class,
				() -> LocusV2PublicOperations.createInteractiveSemanticPoint(
						app.getKernel().getConstruction(), "Forbidden", transformed,
						result.getUniqueCandidate()), route);
		assertNull(app.getKernel().lookupLabel("Forbidden"), route);
		assertEquals(preservedXml, app.getXML(), route);
		assertEquals(geoCount, app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size(), route);
		assertEquals(identityCount, app.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getRecords().size(), route);

		Path resaved = temporaryDirectory.resolve(route + "-resaved.cedg");
		writeHeadless(app, resaved);
		AppDNoGui reopened = reopenHeadless(resaved, configFactory.get());
		assertEquals(preservedXml, reopened.getXML(), route);
		assertEquals(expected, snapshot(reopened,
				assertInstanceOf(GeoPoint.class, lookup(reopened, "P"))), route);
	}

	private static InteractionSnapshot snapshot(App app, GeoPoint point) {
		AlgoSemanticLocusPoint2D parent = semanticParent(point);
		GeoText branchInput = parent.getBranchInput();
		GeoNumeric parameterInput = assertInstanceOf(GeoNumeric.class,
				parent.getParameterInput().toGeoElement());
		assertTrue(branchInput.isAuxiliaryObject());
		assertFalse(branchInput.isEuclidianVisible());
		assertTrue(parameterInput.isAuxiliaryObject());
		assertFalse(parameterInput.isEuclidianVisible());
		assertTrue(point.isDefined());
		SpatialIdentityRegistry registry = app.getKernel().getConstruction()
				.getSpatialIdentityRegistry();
		PersistentGeoId pointId = persistentId(app, point);
		PersistentGeoId branchInputId = persistentId(app, branchInput);
		PersistentGeoId parameterInputId = persistentId(app, parameterInput);
		GeoIdentityRecord pointRecord = registry.getGeoRecord(pointId);
		assertNotNull(pointRecord);
		assertEquals(LocusV2PublicOperations.INTERACTION_POINT_OUTPUT_ROLE,
				pointRecord.getStableOutputRole());
		PersistentGeoId sourceId = persistentId(app, parent.getSource());
		assertTrue(pointRecord.getDependencies().contains(sourceId));
		assertTrue(pointRecord.getDependencies().contains(branchInputId));
		assertTrue(pointRecord.getDependencies().contains(parameterInputId));
		assertEquals(3, pointRecord.getDependencies().size());
		assertNotNull(parent.getSemanticAddress());
		assertEquals(sourceId,
				parent.getSemanticAddress().getSourceLocusId());
		assertEquals(LocusSemanticAddressState2D.encode(
				parent.getSemanticAddress()), branchInput.getTextString());
		assertEquals(parameterInput.getDouble(),
				parent.getSemanticAddress().getCanonicalParameter(), 1E-12);
		return new InteractionSnapshot(pointId, branchInputId, parameterInputId,
				List.copyOf(pointRecord.getDependencies()),
				parent.getSemanticAddress());
	}

	private static GeoLocusV2 createTranslatedSpline(App app) {
		eval(app, "h=0");
		eval(app, "A=(-2,h)");
		eval(app, "B=(-2/3,h)");
		eval(app, "C=(2/3,h)");
		eval(app, "D=(2,h)");
		eval(app, "S=SplineV2({A,B,C,D},3)");
		eval(app, "dy=4");
		eval(app, "v=Vector((0,0),(3,dy))");
		return assertInstanceOf(GeoLocusV2.class,
				eval(app, "T=Translate(S,v)"));
	}

	private static LocusPointInteractionResult2D resolve(GeoLocusV2 source,
			double targetX, double targetY, double radius) {
		return RESOLVER.resolve(new LocusPointInteractionQuery2D(source, targetX,
				targetY, policy(radius)));
	}

	private static LocusPointInteractionPolicy2D policy(double radius) {
		return LocusPointInteractionPolicy2D.initial(radius);
	}

	private static AlgoSemanticLocusPoint2D semanticParent(GeoPoint point) {
		return assertInstanceOf(AlgoSemanticLocusPoint2D.class,
				point.getParentAlgorithm());
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

	private record InteractionSnapshot(PersistentGeoId pointId,
			PersistentGeoId branchInputId, PersistentGeoId parameterInputId,
			List<PersistentGeoId> dependencies, LocusSemanticAddress2D address) {
	}
}
