/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

import org.geocedg.common.kernel.algos.AlgoLocusBetweenMetricV2;
import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.GeoGebraConstants;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.jre.io.MyXMLioJre;
import org.geogebra.common.kernel.commands.Commands;
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
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Native SplineV2 persistence and preservation boundary authority. */
class G9S1NativeArchivePersistenceTest {

	private static final String SPLINE_BRANCH = "spline-v2/main";

	@BeforeAll
	static void initializeDesktop() {
		AwtFactory.setPrototypeIfNull(new AwtFactoryD());
		if (Log.getLogger() == null) {
			Log.setLogger(new LoggerD());
		}
	}

	@Test
	void nativeCedgReopensSplineConsumersAndExactTokenPoint(
			@TempDir Path temporaryDirectory) throws Exception {
		final AppGeoCeDG app = enabledApp();
		final GeoLocusV2 spline = createDynamicLineSpline(app);
		final GeoLocusMetricResult metric = (GeoLocusMetricResult) eval(app,
				"M=LocusLength(S)");
		final GeoNumeric startParameter = (GeoNumeric) eval(app, "u=0.25");
		final GeoNumeric endParameter = (GeoNumeric) eval(app, "v=0.75");
		final GeoPoint metricStart = (GeoPoint) eval(app,
				"P=Point(S,\"" + SPLINE_BRANCH + "\",u)");
		final GeoPoint metricEnd = (GeoPoint) eval(app,
				"Q=Point(S,\"" + SPLINE_BRANCH + "\",v)");
		final GeoNumeric partial = (GeoNumeric) eval(app,
				"MP=Length(S,P,Q)");
		final AlgoLocusMetricScalarAdapter partialAdapter =
				(AlgoLocusMetricScalarAdapter) partial.getParentAlgorithm();
		final GeoLocusMetricResult partialRich = partialAdapter.getRichInput();
		final PersistentGeoId partialRichId = persistentId(app, partialRich);
		assertEquals(2, partial.getDouble(), 1E-8);
		final AlgoLocusBetweenMetricV2 partialRichParent =
				(AlgoLocusBetweenMetricV2) partialRich.getParentAlgorithm();
		assertEquals(spline, partialRichParent.getInput(0));
		assertEquals(metricStart, partialRichParent.getInput(1));
		assertEquals(metricEnd, partialRichParent.getInput(2));
		eval(app, "c=Circle((0,0),1)");
		final GeoLocusIntersectionResult rich =
				(GeoLocusIntersectionResult) eval(app,
						"R=Intersect(S,c)");
		final Set<String> rootTokens = tokens(rich);
		assertEquals(2, rootTokens.size());
		final String selectedToken = rootTokens.iterator().next();
		assertTrue(rich.isPointAdmissible(selectedToken));
		final GeoText tokenInput = new GeoText(app.getKernel().getConstruction(),
				selectedToken);
		tokenInput.setAuxiliaryObject(true);
		tokenInput.setEuclidianVisible(false);
		final GeoPoint selectedPoint =
				LocusV2PublicOperations.selectIntersectionPoint(
				app.getKernel().getConstruction(), "X", rich, tokenInput);
		assertTrue(selectedPoint.isDefined());
		assertEquals(4, metricValue(metric), 1E-8);

		final Map<String, PersistentGeoId> identities = captureIds(app,
				"S", "M", "u", "v", "P", "Q", "MP", "c", "R", "X");
		final List<PersistentGeoId> dependencies = dependencies(app, spline);
		assertFalse(dependencies.isEmpty());
		final String ledger = rich.getTokenLedgerState();

		final Path nativeDocument =
				temporaryDirectory.resolve("g9s1-spline.cedg");
		assertTrue(gui(app).saveAsTo(nativeDocument.toFile()));
		final String archiveXml = readZipEntry(nativeDocument, "geogebra.xml");
		assertTrue(archiveXml.contains("app=\"classic\""));
		assertTrue(archiveXml.contains("<command name=\"SplineV2\""));
		assertTrue(archiveXml.contains("<command name=\"Length\""));
		assertFalse(archiveXml.contains("renderVertices"));
		assertFalse(archiveXml.contains("sampledPointCloud"));

		final AppGeoCeDG reopened = enabledApp();
		assertTrue(reopened.loadFile(nativeDocument.toFile(), false));
		assertIds(reopened, identities);
		final GeoLocusV2 reopenedSpline =
				(GeoLocusV2) lookup(reopened, "S");
		final GeoLocusMetricResult reopenedMetric =
				(GeoLocusMetricResult) lookup(reopened, "M");
		final GeoNumeric reopenedPartial =
				(GeoNumeric) lookup(reopened, "MP");
		final AlgoLocusMetricScalarAdapter reopenedPartialAdapter =
				(AlgoLocusMetricScalarAdapter) reopenedPartial.getParentAlgorithm();
		final GeoLocusMetricResult reopenedPartialRich =
				reopenedPartialAdapter.getRichInput();
		assertEquals(partialRichId,
				persistentId(reopened, reopenedPartialRich));
		final AlgoLocusBetweenMetricV2 reopenedPartialRichParent =
				(AlgoLocusBetweenMetricV2) reopenedPartialRich.getParentAlgorithm();
		assertEquals(reopenedSpline, reopenedPartialRichParent.getInput(0));
		assertEquals(lookup(reopened, "P"),
				reopenedPartialRichParent.getInput(1));
		assertEquals(lookup(reopened, "Q"),
				reopenedPartialRichParent.getInput(2));
		assertEquals(2, reopenedPartial.getDouble(), 1E-8);
		final GeoLocusIntersectionResult reopenedRich =
				(GeoLocusIntersectionResult) lookup(reopened, "R");
		final GeoPoint reopenedPoint = (GeoPoint) lookup(reopened, "X");
		assertEquals(Commands.SplineV2,
				reopenedSpline.getParentAlgorithm().getClassName());
		assertEquals(dependencies, dependencies(reopened, reopenedSpline));
		assertEquals(ledger, reopenedRich.getTokenLedgerState());
		assertEquals(rootTokens, tokens(reopenedRich));
		assertEquals(selectedToken,
				((AlgoLocusIntersectionPointV2) reopenedPoint.getParentAlgorithm())
						.getSelectedRootToken());
		assertTrue(reopenedPoint.isDefined());
		assertEquals(4, metricValue(reopenedMetric), 1E-8);

		final long revision = reopenedSpline.getSemanticRevision();
		final GeoNumeric height = (GeoNumeric) lookup(reopened, "h");
		height.setValue(0.25);
		height.updateCascade();
		assertTrue(reopenedSpline.getSemanticRevision() > revision);
		assertEquals(0.25, evaluate(reopenedSpline, 0.5).getPoint().getY(), 1E-9);
		assertTrue(reopenedPoint.isDefined());
		assertEquals(0.25, reopenedPoint.getInhomY(), 1E-9);
		assertEquals(1, reopenedPoint.getInhomX() * reopenedPoint.getInhomX()
				+ reopenedPoint.getInhomY() * reopenedPoint.getInhomY(), 1E-8);
		assertEquals(selectedToken,
				((AlgoLocusIntersectionPointV2) reopenedPoint.getParentAlgorithm())
						.getSelectedRootToken());
		assertTrue(reopenedRich.isPointAdmissible(selectedToken));
		assertEquals(rootTokens, tokens(reopenedRich));
		final GeoNumeric reopenedEndParameter =
				(GeoNumeric) lookup(reopened, "v");
		reopenedEndParameter.setValue(0.5);
		reopenedEndParameter.updateCascade();
		assertEquals(1, reopenedPartial.getDouble(), 1E-8);

		final Path updatedDocument =
				temporaryDirectory.resolve("g9s1-spline-updated.cedg");
		assertTrue(gui(reopened).saveAsTo(updatedDocument.toFile()));
		final AppGeoCeDG updated = enabledApp();
		assertTrue(updated.loadFile(updatedDocument.toFile(), false));
		assertIds(updated, identities);
		assertEquals(0.25,
				((GeoNumeric) lookup(updated, "h")).getDouble(), 0);
		assertEquals(0.5,
				((GeoNumeric) lookup(updated, "v")).getDouble(), 0);
		assertEquals(1,
				((GeoNumeric) lookup(updated, "MP")).getDouble(), 1E-8);
		GeoLocusMetricResult updatedPartialRich =
				((AlgoLocusMetricScalarAdapter) lookup(updated, "MP")
						.getParentAlgorithm()).getRichInput();
		assertEquals(partialRichId, persistentId(updated, updatedPartialRich));
		final GeoLocusIntersectionResult updatedRich =
				(GeoLocusIntersectionResult) lookup(updated, "R");
		final GeoPoint updatedPoint = (GeoPoint) lookup(updated, "X");
		assertEquals(rootTokens, tokens(updatedRich));
		assertEquals(selectedToken,
				((AlgoLocusIntersectionPointV2) updatedPoint.getParentAlgorithm())
						.getSelectedRootToken());
		assertTrue(updatedPoint.isDefined());
	}

	@Test
	void featureOffAndClassicPreserveSplineWithoutCreation(
			@TempDir Path temporaryDirectory) throws Exception {
		final AppGeoCeDG source = enabledApp();
		createDynamicLineSpline(source);
		eval(source, "M=LocusLength(S)");
		final Path nativeDocument =
				temporaryDirectory.resolve("g9s1-preservation.cedg");
		assertTrue(gui(source).saveAsTo(nativeDocument.toFile()));

		assertPreservesWithoutCreation(nativeDocument, temporaryDirectory,
				"feature-off", AppConfigGeoCeDG::new);
		assertPreservesWithoutCreation(nativeDocument, temporaryDirectory,
				"classic", AppConfigDefault::new);
	}

	private static void assertPreservesWithoutCreation(Path nativeDocument,
			Path temporaryDirectory, String route,
			Supplier<AppConfig> configFactory) throws Exception {
		final AppDNoGui app =
				reopenHeadless(nativeDocument, configFactory.get());
		final GeoLocusV2 spline = (GeoLocusV2) lookup(app, "S");
		assertTrue(spline.isDefined());
		assertEquals(Commands.SplineV2,
				spline.getParentAlgorithm().getClassName());
		assertFalse(RuntimeFeatureService.mayCreateLocusV2(
				app.getKernel().getConstruction()));
		assertEquals(GeoGebraConstants.CLASSIC_APPCODE,
				app.getConfig().getAppCode());
		final String preservedXml = app.getXML();
		final int geoCount = app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size();
		final int identityCount = app.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getRecords().size();

		final CapturingErrorHandler errors = new CapturingErrorHandler();
		GeoElementND[] output = null;
		RuntimeException thrown = null;
		try {
			output = app.getKernel().getAlgebraProcessor()
					.processAlgebraCommandNoExceptionHandling(
							"Forbidden=SplineV2({A,B,C,D},3)", false,
							errors, false, null);
		} catch (RuntimeException exception) {
			thrown = exception;
		}
		assertTrue(output == null || output.length == 0, route);
		assertTrue(thrown != null || errors.getFailure() != null
				|| !errors.getErrors().isBlank(), route);
		assertNull(app.getKernel().lookupLabel("Forbidden"));
		assertEquals(preservedXml, app.getXML());
		assertEquals(geoCount, app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size());
		assertEquals(identityCount, app.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getRecords().size());

		final Path resaved =
				temporaryDirectory.resolve(route + "-resaved.cedg");
		writeHeadless(app, resaved);
		final AppDNoGui reopened =
				reopenHeadless(resaved, configFactory.get());
		assertEquals(preservedXml, reopened.getXML());
		assertTrue(((GeoLocusV2) lookup(reopened, "S")).isDefined());
		assertFalse(RuntimeFeatureService.mayCreateLocusV2(
				reopened.getKernel().getConstruction()));
	}

	private static GeoLocusV2 createDynamicLineSpline(App app) {
		eval(app, "h=0");
		eval(app, "A=(-2,h)");
		eval(app, "B=(-2/3,h)");
		eval(app, "C=(2/3,h)");
		eval(app, "D=(2,h)");
		return (GeoLocusV2) eval(app, "S=SplineV2({A,B,C,D},3)");
	}

	private static Set<String> tokens(GeoLocusIntersectionResult result) {
		assertNotNull(result.getIntersectionResult());
		return result.getIntersectionResult().getFiniteSolutions().stream()
				.map(solution -> solution.getIdentity().getRootToken())
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	private static double metricValue(GeoLocusMetricResult metric) {
		return metric.getMetricResult().getMetricValue().getFiniteValue()
				.orElseThrow();
	}

	private static LocusEvaluation2D evaluate(GeoLocusV2 spline,
			double parameter) {
		try (LocusEvaluationSession2D session =
				LocusEvaluationSession2D.reference()) {
			return spline.evaluate(SPLINE_BRANCH, parameter, session);
		}
	}

	private static Map<String, PersistentGeoId> captureIds(App app,
			String... labels) {
		final Map<String, PersistentGeoId> identities = new LinkedHashMap<>();
		for (String label : labels) {
			identities.put(label, persistentId(app, lookup(app, label)));
		}
		return Map.copyOf(identities);
	}

	private static void assertIds(App app,
			Map<String, PersistentGeoId> expected) {
		final SpatialIdentityRegistry registry = app.getKernel().getConstruction()
				.getSpatialIdentityRegistry();
		for (Map.Entry<String, PersistentGeoId> entry : expected.entrySet()) {
			final GeoElement geo = lookup(app, entry.getKey());
			assertEquals(entry.getValue(), registry.getPersistentGeoId(geo),
					entry.getKey());
			assertEquals(geo, registry.getGeo(entry.getValue()), entry.getKey());
		}
	}

	private static List<PersistentGeoId> dependencies(App app,
			GeoElement geo) {
		final SpatialIdentityRegistry registry = app.getKernel().getConstruction()
				.getSpatialIdentityRegistry();
		final GeoIdentityRecord record =
				registry.getGeoRecord(persistentId(app, geo));
		assertNotNull(record);
		return List.copyOf(record.getDependencies());
	}

	private static PersistentGeoId persistentId(App app, GeoElement geo) {
		final PersistentGeoId id = app.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getPersistentGeoId(geo);
		assertNotNull(id, geo.getLabelSimple());
		return id;
	}

	private static GeoElement eval(App app, String command) {
		final GeoElementND[] output = app.getKernel().getAlgebraProcessor()
				.processAlgebraCommand(command, false);
		assertNotNull(output, command);
		assertTrue(output.length > 0, command);
		return output[0].toGeoElement();
	}

	private static GeoElement lookup(App app, String label) {
		final GeoElement geo = app.getKernel().lookupLabel(label);
		assertNotNull(geo, label);
		return geo;
	}

	private static AppGeoCeDG enabledApp() {
		final AppGeoCeDG app = new AppGeoCeDG(
				new CommandLineArguments(new String[] {
				"--silent", "--enableLocusV2=true"}), new JPanel());
		app.setErrorDialogsActive(false);
		return app;
	}

	private static GuiManagerGeoCeDG gui(AppGeoCeDG app) {
		return (GuiManagerGeoCeDG) app.getGuiManager();
	}

	private static AppDNoGui newHeadless(AppConfig config) {
		final Log previousLogger = Log.getLogger();
		try {
			return new AppDNoGui(new LocalizationD(3), true, config);
		} finally {
			Log.setLogger(previousLogger);
		}
	}

	private static AppDNoGui reopenHeadless(Path source, AppConfig config)
			throws Exception {
		final AppDNoGui reopened = newHeadless(config);
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
			final ZipEntry entry = archive.getEntry(entryName);
			assertNotNull(entry, entryName);
			return new String(archive.getInputStream(entry).readAllBytes(),
					StandardCharsets.UTF_8);
		}
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
