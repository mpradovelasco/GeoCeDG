/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.JPanel;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.SemanticGeneratorDescriptor1D;
import org.geocedg.desktop.AppGeoCeDG;
import org.geocedg.desktop.GeoCeDGEuclidianController;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.Hits;
import org.geogebra.common.kernel.ModeSetter;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.awt.AwtFactoryD;
import org.geogebra.desktop.util.LoggerD;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Exact T4 GeoCeDG-only creation and result-inspection tool scenarios. */
class G9U0ToolSurfaceTest {
	private static final String BRANCH =
			SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY;

	@BeforeAll
	static void initializeAwtFactory() {
		AwtFactory.setPrototypeIfNull(new AwtFactoryD());
		if (Log.getLogger() == null) {
			Log.setLogger(new LoggerD());
		}
	}

	@Test
	void t01CreationUsesADedicatedSelectionTransactionMode() {
		assertNotEquals(EuclidianConstants.MODE_LOCUS,
				EuclidianConstants.MODE_LOCUS_V2);
		assertTrue(EuclidianConstants.MODE_LOCUS_V2 > 0);

		AppGeoCeDG app = enabledApp();
		app.setErrorDialogsActive(false);
		eval(app, "g=Segment((0,0),(2,0))");
		GeoPoint driver = (GeoPoint) eval(app, "S=Point(g)");
		GeoPoint dependent = (GeoPoint) eval(app,
				"Q=(x(S),x(S)^2+1)");
		GeoCeDGEuclidianController controller = controller(app);
		controller.setMode(EuclidianConstants.MODE_LOCUS_V2,
				ModeSetter.TOOLBAR);
		Hits dependentHit = new Hits();
		dependentHit.add(dependent);
		Hits driverHit = new Hits();
		driverHit.add(driver);
		int before = app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size();

		assertFalse(controller.processMode(driverHit, false, false));
		assertEquals(before, app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size());
		assertFalse(controller.processMode(dependentHit, false, false));
		assertEquals(before, app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size());
		assertTrue(app.getSelectionManager().getSelectedGeos().isEmpty());

		controller.refreshHighlighting(dependentHit, false, false);
		assertEquals(before, app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size());
		assertTrue(app.getSelectionManager().getSelectedGeos().isEmpty());

		assertFalse(controller.processMode(dependentHit, false, false));
		controller.setMode(EuclidianConstants.MODE_MOVE, ModeSetter.TOOLBAR);
		assertTrue(app.getSelectionManager().getSelectedGeos().isEmpty());
		assertEquals(before, app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size());

		controller.setMode(EuclidianConstants.MODE_LOCUS_V2,
				ModeSetter.TOOLBAR);
		assertFalse(controller.processMode(dependentHit, false, false));
		assertTrue(controller.processMode(driverHit, false, false));
		GeoLocusV2 created = app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().stream()
				.filter(GeoLocusV2.class::isInstance).map(GeoLocusV2.class::cast)
				.findFirst().orElseThrow();
		assertTrue(app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size() > before);
		assertInstanceOf(AlgoDependentPointLocusV2.class,
				created.getParentAlgorithm());
		assertSame(dependent, created.getParentAlgorithm().getInput(0));
		assertSame(driver, created.getParentAlgorithm().getInput(1));
		assertTrue(app.getSelectionManager().getSelectedGeos().isEmpty());
	}

	@Test
	void t01aPointToolRequiresExplicitPreimageDialog() throws IOException {
		AppGeoCeDG app = enabledApp();
		eval(app, "s=0");
		eval(app, "Q=(sin(s),sin(2*s))");
		eval(app, "D={false,{0,pi,true,true}}");
		GeoLocusV2 locus = (GeoLocusV2) eval(app, "L=LocusV2(Q,s,D)");
		GeoPoint first = (GeoPoint) eval(app,
				"A=Point(L,\"" + BRANCH + "\",0)");
		GeoPoint second = (GeoPoint) eval(app,
				"B=Point(L,\"" + BRANCH + "\",pi)");
		var firstAddress = ((AlgoSemanticLocusPoint2D)
				first.getParentAlgorithm()).getSemanticAddress();
		var secondAddress = ((AlgoSemanticLocusPoint2D)
				second.getParentAlgorithm()).getSemanticAddress();
		assertSame(locus, first.getParentAlgorithm().getInput(0));
		assertEquals(first.getInhomX(), second.getInhomX(), 1E-14);
		assertEquals(first.getInhomY(), second.getInhomY(), 1E-14);
		assertNotEquals(firstAddress.getCanonicalParameter(),
				secondAddress.getCanonicalParameter());

		String dialogSource = dialogSource();
		assertTrue(dialogSource.contains("JTextField branch"));
		assertTrue(dialogSource.contains("JTextField parameter"));
		assertTrue(dialogSource.contains(
				"branch.getAccessibleContext().setAccessibleName(branchLabel)"));
		assertTrue(dialogSource.contains("branchPrompt.setLabelFor(branch)"));
		assertTrue(dialogSource.contains(
				"parameterPrompt.setLabelFor(parameter)"));
	}

	@Test
	void t02GeneralIntersectToolKeepsOneControllerDispatch() {
		AppGeoCeDG app = enabledApp();
		eval(app, "s=0");
		eval(app, "Q=(s,0)");
		eval(app, "D={false,{-2,2,true,true}}");
		GeoLocusV2 locus = (GeoLocusV2) eval(app, "L=LocusV2(Q,s,D)");
		GeoElement target = eval(app, "target:x=0");
		GeoCeDGEuclidianController controller = controller(app);
		controller.setMode(EuclidianConstants.MODE_INTERSECT,
				ModeSetter.TOOLBAR);
		Hits locusHit = new Hits();
		locusHit.add(locus);
		assertFalse(controller.processMode(locusHit, false, false));
		Hits targetHit = new Hits();
		targetHit.add(target);
		assertTrue(controller.processMode(targetHit, false, false));
		long richCount = app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().stream()
				.filter(GeoLocusIntersectionResult.class::isInstance).count();
		assertEquals(1, richCount);

		controller.clearSelections();
		GeoElement firstLine = eval(app, "u0a:x+y=1");
		GeoElement secondLine = eval(app, "u0b:x-y=0");
		long pointsBefore = app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().stream()
				.filter(GeoPoint.class::isInstance).count();
		controller.setMode(EuclidianConstants.MODE_INTERSECT,
				ModeSetter.TOOLBAR);
		Hits firstLineHit = new Hits();
		firstLineHit.add(firstLine);
		assertFalse(controller.processMode(firstLineHit, false, false));
		Hits secondLineHit = new Hits();
		secondLineHit.add(secondLine);
		assertTrue(controller.processMode(secondLineHit, false, false));
		assertEquals(pointsBefore + 1, app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().stream()
				.filter(GeoPoint.class::isInstance).count());
		assertEquals(richCount, app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().stream()
				.filter(GeoLocusIntersectionResult.class::isInstance).count());
	}

	@Test
	void t03ExactTokenChooserHasKeyboardAccessibleInspectionEntry()
			throws IOException {
		AppGeoCeDG app = enabledApp();
		eval(app, "s=0");
		eval(app, "Q=(s,0)");
		eval(app, "D={false,{-2,2,true,true}}");
		eval(app, "L=LocusV2(Q,s,D)");
		eval(app, "target:x=0");
		GeoLocusIntersectionResult rich = (GeoLocusIntersectionResult)
				eval(app, "R=Intersect(L,target)");
		assertNotNull(rich.getIntersectionResult());
		String token = rich.getIntersectionResult().getFiniteSolutions().get(0)
				.getIdentity().getRootToken();
		GeoPoint chosen = (GeoPoint) eval(app,
				"X=Intersect(R,\"" + token + "\")");
		assertTrue(chosen.isDefined());
		assertSame(rich, chosen.getParentAlgorithm().getInput(0));
		assertFalse(token.isBlank());

		String dialogSource = dialogSource();
		assertTrue(dialogSource.contains("JComboBox<TokenChoice> tokens"));
		assertTrue(dialogSource.contains(
				"tokens.getAccessibleContext().setAccessibleName(tokenLabel)"));
		assertTrue(dialogSource.contains("prompt.setLabelFor(tokens)"));
		assertTrue(dialogSource.contains("tokens.getSelectedItem()"));
	}

	private static AppGeoCeDG enabledApp() {
		return new AppGeoCeDG(new CommandLineArguments(new String[] {
				"--silent", "--enableLocusV2=true"}), new JPanel());
	}

	private static GeoCeDGEuclidianController controller(AppGeoCeDG app) {
		return (GeoCeDGEuclidianController) app.getEuclidianView1()
				.getEuclidianController();
	}

	private static GeoElement eval(AppGeoCeDG app, String command) {
		var output = app.getKernel().getAlgebraProcessor()
				.processAlgebraCommand(command, false);
		assertNotNull(output, command);
		assertTrue(output.length > 0, command);
		return output[0].toGeoElement();
	}

	private static String dialogSource() throws IOException {
		return Files.readString(repositoryRoot().resolve(
				"source/desktop/desktop/src/main/java/org/geocedg/desktop/"
						+ "GeoCeDGLocusV2Dialogs.java"), StandardCharsets.UTF_8);
	}

	private static Path repositoryRoot() {
		Path candidate = Path.of("").toAbsolutePath().normalize();
		while (candidate != null) {
			if (Files.isRegularFile(candidate.resolve("AGENTS.md"))) {
				return candidate;
			}
			candidate = candidate.getParent();
		}
		throw new IllegalStateException("GeoCeDG repository root not found");
	}
}
