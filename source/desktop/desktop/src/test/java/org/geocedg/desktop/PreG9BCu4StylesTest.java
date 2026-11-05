/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */
package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Documents.M;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.app;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.awaitQuiescent;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.baseline;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.save;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.store;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.undo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JPanel;

import org.geocedg.common.jre.io.LegacyStyleDefaultsReader;
import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.io.XMLParseException;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.ConstructionDefaults;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.main.App;
import org.geogebra.common.main.GeoGebraPreferences;
import org.geogebra.common.main.settings.LabelVisibility;
import org.geogebra.common.main.settings.LabelVisibility;
import org.geogebra.common.plugin.EuclidianStyleConstants;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.main.GeoGebraPreferencesD;
import org.geogebra.desktop.main.GeoGebraPortablePreferences;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** CU4 appearance at the normal native load, preference and T0 boundaries. */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu4StylesTest {
	@TempDir
	Path work;

	private static ConstructionDefaults defaults(AppGeoCeDG app) {
		return app.getKernel().getConstruction().getConstructionDefaults();
	}

	private static GeoElement prototype(AppGeoCeDG app, int type) {
		return defaults(app).getDefaultGeo(type);
	}

	private static void setCurves(AppGeoCeDG app) {
		for (String command : List.of("t=0", "P=(t,1)", "dom={false,{-2,2,true,true}}",
				"L=LocusV2(P,t,dom)", "S=SplineV2({(0,0),(1,2),(3,1),(4,0)},3)")) {
			must(app, command);
		}
	}

	@Test
	void stylesResolveByCurveTypeAndUndoWithoutChangingIdentityOrLabels() throws Exception {
		onEdt(() -> {
			AppGeoCeDG source = app(M);
			prototype(source, ConstructionDefaults.DEFAULT_LINE).setObjColor(GColor.GREEN);
			GeoElement locusDefault = prototype(source, ConstructionDefaults.DEFAULT_LOCUS_V2);
			locusDefault.setObjColor(GColor.RED);
			locusDefault.setLineThickness(7);
			locusDefault.setLineType(EuclidianStyleConstants.LINE_TYPE_DOTTED);
			prototype(source, ConstructionDefaults.DEFAULT_SPLINE_V2).setObjColor(GColor.BLUE);
			setCurves(source);
			source.getKernel().lookupLabel("L").setObjColor(GColor.MAGENTA);
			File file = save(source, work, "styles");

			AppGeoCeDG target = app(M);
			target.setLabelingStyle(ConstructionDefaults.LABEL_VISIBLE_ALWAYS_OFF);
			setCurves(target);
			GeoLocusV2 locus = (GeoLocusV2) target.getKernel().lookupLabel("L");
			GeoLocusV2 spline = (GeoLocusV2) target.getKernel().lookupLabel("S");
			GeoList dynamicColor = (GeoList) must(target, "rgb={0.25,0.5,0.75}");
			spline.setColorFunction(dynamicColor);
			GColor renderedSplineColor = spline.getObjectColor();
			var locusId = locus.getPersistentLocusId();
			var splineId = spline.getPersistentLocusId();
			var definition = locus.getSemanticDefinition();
			GColor oldColor = locus.getObjectColor();
			baseline(target);
			int cursor = undo(target).getCursor();
			target.getSelectionManager().setSelectedGeos(new ArrayList<>(List.of(locus, spline)));
			assertEquals(2, target.getStyleApplication().apply(file).getRestyled());
			awaitQuiescent(target);
			assertEquals(GColor.RED, locus.getObjectColor());
			assertEquals(GColor.BLUE, spline.getStaticObjectColor());
			assertSame(dynamicColor, spline.getColorFunction());
			assertEquals(renderedSplineColor, spline.getObjectColor());
			assertEquals(7, locus.getLineThickness());
			assertEquals(EuclidianStyleConstants.LINE_TYPE_DOTTED, locus.getLineType());
			assertFalse(locus.getLabelVisible());
			assertSame(definition, locus.getSemanticDefinition());
			assertEquals(cursor + 1, undo(target).getCursor());
			assertEquals(0, target.getStyleApplication().apply(file).getRestyled());
			assertEquals(cursor + 1, undo(target).getCursor());
			undo(target).undo();
			assertEquals(oldColor, target.getKernel().lookupLabel("L").getObjectColor());
			undo(target).redo();
			File restyled = save(target, work, "restyled");
			AppGeoCeDG reopened = app(M);
			reopened.setLabelingStyle(ConstructionDefaults.LABEL_VISIBLE_ALWAYS_ON);
			assertTrue(reopened.loadExistingFile(restyled, false));
			GeoLocusV2 loadedLocus = (GeoLocusV2) reopened.getKernel().lookupLabel("L");
			GeoLocusV2 loadedSpline = (GeoLocusV2) reopened.getKernel().lookupLabel("S");
			assertEquals(locusId, loadedLocus.getPersistentLocusId());
			assertEquals(splineId, loadedSpline.getPersistentLocusId());
			assertEquals(GColor.RED, loadedLocus.getObjectColor());
			assertEquals(GColor.BLUE, loadedSpline.getStaticObjectColor());
			assertEquals(renderedSplineColor, loadedSpline.getObjectColor());
			assertNotNull(loadedSpline.getColorFunction());
			assertFalse(loadedLocus.getLabelVisible());
			return null;
		});
	}

	@Test
	void automaticLabelingFollowsAlgebraVisibilityOnlyForNewCurves() throws Exception {
		onEdt(() -> {
			AppGeoCeDG target = app(M);
			target.getSettings().getLabelSettings().setLabelVisibility(LabelVisibility.Automatic);
			prototype(target, ConstructionDefaults.DEFAULT_SPLINE_V2).setLabelVisible(true);
			JPanel panel = target.buildApplicationPanel();
			panel.addNotify();
			try {
				target.getGuiManager().setShowView(true, App.VIEW_ALGEBRA);
				GeoElement first = must(target, "S=SplineV2({(0,0),(1,2),(3,1),(4,0)},3)");
				assertTrue(first.getLabelVisible());
				target.getGuiManager().setShowView(false, App.VIEW_ALGEBRA);
				GeoElement second = must(target, "T=SplineV2({(0,0),(1,3),(3,1),(4,0)},3)");
				assertFalse(second.getLabelVisible());
				assertTrue(first.getLabelVisible());
			} finally {
				panel.removeNotify();
			}
			return null;
		});
	}

	@Test
	void cu7WorldAndReferenceKeepTheirAddressAfterRestylingTheSupport() throws Exception {
		onEdt(() -> {
			AppGeoCeDG source = app(M);
			prototype(source, ConstructionDefaults.DEFAULT_LOCUS_V2).setObjColor(GColor.RED);
			File style = save(source, work, "support-style");
			AppGeoCeDG target = app(M);
			target.setLabelingStyle(ConstructionDefaults.LABEL_VISIBLE_ALWAYS_OFF);
			GeoLocusV2 support = Cu7M08SDesktopEventsTest.general(target);
			GeoPoint q = (GeoPoint) must(target, "Q=(0.8976484384297626,1.7854818601338847)");
			var policy = LocusPointInteractionPolicy2D.initial(.1);
			try (var before = new GeoCeDGReferencePlacement(support, q, policy)) {
				var address = before.getChoices().get(0).getAddress();
				target.getSelectionManager().setSelectedGeos(new ArrayList<>(List.of(support)));
				baseline(target);
				assertEquals(1, target.getStyleApplication().apply(style).getRestyled());
				assertFalse(support.getLabelVisible());
				for (boolean fromQ : new boolean[]{false, true}) {
					try (var placement = fromQ
							? new GeoCeDGReferencePlacement(support, q, policy)
							: new GeoCeDGReferencePlacement(support, q.getInhomX(), q.getInhomY(), policy)) {
						assertEquals(address, placement.getChoices().get(0).getAddress());
						GeoPoint point = placement.commit(placement.getChoices().get(0));
						assertSame(support,
								((AlgoSemanticLocusPoint2D) point.getParentAlgorithm()).getSource());
						assertFalse(point.getAllPredecessors().contains(q));
					}
				}
			}
			awaitQuiescent(target);
			return null;
		});
	}

	@Test
	void personalDefaultsSurviveRestartAndDocumentDefaultsTakePrecedence() throws Exception {
		runSession("save");
		runSession("reopen");
	}

	@Test
	void portableAppearancePreferencesCannotEvaluateOrChangeTheConstruction() throws Exception {
		runSession("graph-invariance");
	}

	private void runSession(String action) throws Exception {
		List<String> command = new ArrayList<>();
		command.add(ProcessHandle.current().info().command().orElseThrow());
		ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
				.filter(arg -> arg.startsWith("--add-") || arg.startsWith("--enable-native-access"))
				.forEach(command::add);
		command.addAll(List.of("-cp", System.getProperty("java.class.path"),
				PortableSession.class.getName(), work.toString(), action));
		Path transcript = work.resolve(action + ".log");
		Process process = new ProcessBuilder(command).redirectErrorStream(true)
				.redirectOutput(transcript.toFile()).start();
		try {
			assertTrue(process.waitFor(75, TimeUnit.SECONDS), "Portable session timed out");
			assertEquals(0, process.exitValue(), Files.readString(transcript));
		} finally {
			if (process.isAlive()) {
				process.destroyForcibly();
			}
		}
	}

	/** Fresh process for each side of the Portable preference round trip. */
	public static final class PortableSession {
		private PortableSession() {
		}

		/** @param args scratch directory and session action */
		public static void main(String[] args) {
			Path work = Path.of(args[0]);
			try {
				GeoGebraPreferencesD.setPropertyFileName(work.resolve("settings.properties").toString());
				onEdt(() -> {
					AppGeoCeDG app = app(M);
					if ("graph-invariance".equals(args[1])) {
						assertAppearancePreferencesPreserveGraph(app);
					} else if ("save".equals(args[1])) {
						prototype(app, ConstructionDefaults.DEFAULT_LOCUS).setObjColor(GColor.ORANGE);
						prototype(app, ConstructionDefaults.DEFAULT_LOCUS_V2).setObjColor(GColor.RED);
						prototype(app, ConstructionDefaults.DEFAULT_SPLINE_V2).setObjColor(GColor.BLUE);
						app.setLabelingStyle(LabelVisibility.AlwaysOn.getValue());
						GeoGebraPreferencesD.getPref().saveXMLPreferences(app);
						prototype(app, ConstructionDefaults.DEFAULT_LOCUS_V2).setObjColor(GColor.GREEN);
						app.setLabelingStyle(LabelVisibility.AlwaysOff.getValue());
						setCurves(app);
						app.getKernel().lookupLabel("L").setObjColor(GColor.MAGENTA);
						File document = save(app, work, "document");
						removeLabelingStyle(document.toPath(), work.resolve("legacy.cedg"));
					} else {
						assertEquals(GColor.ORANGE, prototype(app, ConstructionDefaults.DEFAULT_LOCUS)
								.getObjectColor());
						assertEquals(GColor.RED, prototype(app, ConstructionDefaults.DEFAULT_LOCUS_V2)
								.getObjectColor());
						assertEquals(GColor.BLUE, prototype(app, ConstructionDefaults.DEFAULT_SPLINE_V2)
								.getObjectColor());
						assertTrue(app.loadExistingFile(work.resolve("document.cedg").toFile(), false));
						assertEquals(GColor.GREEN, prototype(app, ConstructionDefaults.DEFAULT_LOCUS_V2)
								.getObjectColor());
						assertEquals(GColor.MAGENTA, app.getKernel().lookupLabel("L").getObjectColor());
						assertEquals(LabelVisibility.AlwaysOff.getValue(), app.getLabelingStyle());
						assertFalse(must(app, "L2=LocusV2(P,t,dom)").isLabelVisible());
						Path legacy = work.resolve("legacy.cedg");
						assertTrue(app.loadExistingFile(legacy.toFile(), false));
						assertPersonalLabelFallback(app);
						app.setSaved();
						app.fileNew();
						assertEquals(GColor.RED, prototype(app, ConstructionDefaults.DEFAULT_LOCUS_V2)
								.getObjectColor());
						AppGeoCeDG openedAtStartup = new AppGeoCeDG(new CommandLineArguments(
								new String[]{"--silent", work.resolve("document.cedg").toString()}),
								new JPanel());
						assertEquals(GColor.GREEN,
								prototype(openedAtStartup, ConstructionDefaults.DEFAULT_LOCUS_V2)
										.getObjectColor());
						assertEquals(GColor.MAGENTA,
								openedAtStartup.getKernel().lookupLabel("L").getObjectColor());
						assertEquals(LabelVisibility.AlwaysOff.getValue(),
								openedAtStartup.getLabelingStyle());
						AppGeoCeDG legacyAtStartup = new AppGeoCeDG(new CommandLineArguments(
								new String[]{"--silent", legacy.toString()}), new JPanel());
						assertPersonalLabelFallback(legacyAtStartup);
						assertLegacyObjectLabelsIndependentOfPreferences(app, work);
					}
					return null;
				});
				System.exit(0);
			} catch (Throwable failure) {
				failure.printStackTrace();
				System.exit(1);
			}
		}

		private static void assertAppearancePreferencesPreserveGraph(AppGeoCeDG app)
				throws Exception {
			setCurves(app);
			must(app, "d=Distance((1,2),(4,6))");
			baseline(app);
			must(app, "Next=(4,5)");
			store(app);
			undo(app).undo();
			awaitQuiescent(app);
			assertTrue(undo(app).redoPossible());

			Construction construction = app.getKernel().getConstruction();
			var before = new PreG9BR6PlusF3Documents.Observation(app);
			var geos = List.copyOf(construction.getGeoSetConstructionOrder());
			var algorithms = List.copyOf(construction.getAlgoList());
			var dependencies = dependencies(algorithms);
			var identities = List.copyOf(construction.getSpatialIdentityRegistry().getRecords());
			int steps = construction.steps();
			var prefs = (GeoGebraPortablePreferences) GeoGebraPreferencesD.getPref();
			ConstructionDefaults defaults = defaults(app);
			GeoElement locus = prototype(app, ConstructionDefaults.DEFAULT_LOCUS);
			GeoNumeric slider = (GeoNumeric) prototype(app, ConstructionDefaults.DEFAULT_NUMBER);
			var anchor = slider.getStartPoint();
			int maxLayer = app.getMaxLayerUsed();
			String green = """
					<element type="locus" label="Locus" default="80">
					  <objColor r="0" g="255" b="0" alpha="0"/>
					  <lineStyle thickness="7" type="15"/>
					</element>
					""";
			String number = """
					<element type="numeric" label="Number" default="50">
					  <show object="true" label="true"/>
					  <layer val="20"/>
					  <slider min="-2" max="8" width="175" fixed="true" horizontal="true"/>
					  <animation step="0.25" speed="2" type="0" playing="false"/>
					</element>
					""";
			readPersonalStyles(prefs, app, wrapDefaults(green + number));
			assertEquals(GColor.GREEN, locus.getObjectColor());
			assertEquals(7, locus.getLineThickness());
			assertEquals(-2, slider.getIntervalMin());
			assertEquals(8, slider.getIntervalMax());
			assertEquals(0.25, slider.getAnimationStep());
			assertEquals(2, slider.getAnimationSpeed());
			assertTrue(slider.isSetEuclidianVisible());
			assertSame(anchor, slider.getStartPoint());
			assertEquals(maxLayer, app.getMaxLayerUsed());
			assertGraphUnchanged(app, before, geos, algorithms, dependencies, identities, steps);

			String red = green.replace("r=\"0\" g=\"255\"", "r=\"255\" g=\"0\"");
			String blue = green.replace("g=\"255\" b=\"0\"", "g=\"0\" b=\"255\"");
			for (String invalid : List.of(
					wrapDefaults(red + number.replace("step=\"0.25\"",
							"step=\"Distance((0,0),(1,0))\"")),
					wrapDefaults(red + number.replace("min=\"-2\"", "min=\"d\"")),
					wrapDefaults(red + blue),
					wrapDefaults(red.replace("thickness=\"7\"", "thickness=\"7\" thickness=\"8\"")),
					wrapDefaults(red.replace("</element>", "<condition show=\"d>0\"/></element>")),
					wrapDefaults(red.replace("alpha=\"0\"", "alpha=\"0\" dynamicr=\"d\"")),
					wrapDefaults(red + """
							<element type="text" label="Text" default="100">
							  <startPoint exp="Point(Circle((0,0),1))"/>
							</element>
							"""),
					wrapDefaults(red).replace("</geogebra>", "<construction/></geogebra>"),
					wrapDefaults(red) + "<geogebra/>",
					"<!DOCTYPE geogebra>" + wrapDefaults(red))) {
				assertThrows(XMLParseException.class, () -> LegacyStyleDefaultsReader.read(invalid, defaults));
				readPersonalStyles(prefs, app, invalid);
				assertEquals(GColor.GREEN, locus.getObjectColor());
				assertEquals(0.25, slider.getAnimationStep());
				assertEquals(maxLayer, app.getMaxLayerUsed());
				assertGraphUnchanged(app, before, geos, algorithms, dependencies, identities, steps);
			}
			defaults.applyPersonalDefaults();
			assertEquals(maxLayer, app.getMaxLayerUsed());
			assertGraphUnchanged(app, before, geos, algorithms, dependencies, identities, steps);
			undo(app).redo();
			awaitQuiescent(app);
			assertNotNull(app.getKernel().lookupLabel("Next"));
		}

		private static String wrapDefaults(String elements) {
			return "<geogebra format=\"5.0\"><defaults>" + elements + "</defaults></geogebra>";
		}

		private static void readPersonalStyles(GeoGebraPortablePreferences prefs, AppGeoCeDG app,
				String xml) {
			prefs.savePreference(GeoGebraPreferences.XML_DEFAULT_OBJECT_PREFERENCES, xml);
			prefs.rememberGeoCeDGObjectPreferences(app);
			defaults(app).overlayPersonalDefaults();
		}

		private static List<List<GeoElement>> dependencies(List<AlgoElement> algorithms) {
			return algorithms.stream().map(algo -> Arrays.asList(algo.getInput().clone())).toList();
		}

		private static void assertGraphUnchanged(AppGeoCeDG app,
				PreG9BR6PlusF3Documents.Observation before, List<GeoElement> geos,
				List<AlgoElement> algorithms, List<List<GeoElement>> dependencies,
				List<?> identities, int steps) {
			Construction construction = app.getKernel().getConstruction();
			var after = new PreG9BR6PlusF3Documents.Observation(app);
			assertEquals(geos, List.copyOf(construction.getGeoSetConstructionOrder()));
			assertEquals(algorithms, construction.getAlgoList());
			assertEquals(dependencies, dependencies(construction.getAlgoList()));
			assertEquals(identities, List.copyOf(construction.getSpatialIdentityRegistry().getRecords()));
			assertEquals(steps, construction.steps());
			assertEquals(List.of(), before.serializableDifferences(after));
			assertTrue(before.sameInstances(after));
			assertEquals(0, undo(app).getPendingStores());
			assertTrue(undo(app).redoPossible());
		}

		private static void assertPersonalLabelFallback(AppGeoCeDG app) {
			assertEquals(LabelVisibility.AlwaysOn.getValue(), app.getLabelingStyle());
			assertFalse(app.getKernel().lookupLabel("L").isLabelVisible());
			assertTrue(must(app, "L2=LocusV2(P,t,dom)").isLabelVisible());
		}

		private static void assertLegacyObjectLabelsIndependentOfPreferences(AppGeoCeDG app,
				Path work) throws Exception {
			Path original = PreG9BCu0Corpus.repositoryRoot().resolve(
					"models/regression/g9a2-spatial-point-pilot/g9a2-spatial-point-pilot.ggb");
			File copy = Files.copy(original, work.resolve(original.getFileName())).toFile();
			assertTrue(app.loadExistingFile(copy, false));
			assertEquals(LabelVisibility.AlwaysOn.getValue(), app.getLabelingStyle());
			List<Boolean> originalVisibility = labelVisibility(app);
			assertEquals(11, originalVisibility.size());

			app.setLabelingStyle(LabelVisibility.AlwaysOff.getValue());
			GeoGebraPreferencesD.getPref().saveXMLPreferences(app);
			assertTrue(app.loadExistingFile(copy, false));
			assertEquals(LabelVisibility.AlwaysOff.getValue(), app.getLabelingStyle());
			assertEquals(originalVisibility, labelVisibility(app));
		}

		private static List<Boolean> labelVisibility(AppGeoCeDG app) {
			return app.getKernel().getConstruction().getGeoSetConstructionOrder().stream()
					.map(GeoElement::isLabelVisible).toList();
		}

		private static void removeLabelingStyle(Path source, Path output) throws Exception {
			try (ZipInputStream input = new ZipInputStream(Files.newInputStream(source));
					ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(output))) {
				for (ZipEntry entry; (entry = input.getNextEntry()) != null;) {
					zip.putNextEntry(new ZipEntry(entry.getName()));
					if ("geogebra.xml".equals(entry.getName())) {
						String xml = new String(input.readAllBytes(), StandardCharsets.UTF_8);
						String stripped = xml.replaceFirst("<labelingStyle\\b[^>]*/>", "");
						assertFalse(stripped.equals(xml));
						zip.write(stripped.getBytes(StandardCharsets.UTF_8));
					} else {
						input.transferTo(zip);
					}
					zip.closeEntry();
				}
			}
		}
	}
}
