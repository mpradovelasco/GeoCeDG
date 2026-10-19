/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.geocedg.desktop.GeoCeDGLineReferenceMemory.Slot;
import org.geogebra.common.kernel.ConstructionDefaults;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU0 characterization of authoring behaviour reported with the 2026-10-19 author
 * input. Pins are reference evidence, not normative expectations: B01 and M05.1/B05 are owned
 * by CU4, M04 and H05 by CU5. B04 preserves the inherited correspondence between the outputs
 * of {@code Intersect(c, n)} and the indexed form {@code Intersect(c, n, k)}.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu0AuthoringCharacterizationTest {

	@TempDir
	Path work;

	@Test
	void insertionRejectsFreeAnglesAndStaticTextBeforeMutation() throws Exception {
		AppGeoCeDG app = PreG9BR6PlusF3Fixtures.onEdt(
				() -> PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.M));
		GeoCeDGDocumentInsertion.Result result = PreG9BR6PlusF3Fixtures.insert(app,
				PreG9BCu0Corpus.workingCopy(PreG9BCu0Corpus.GGB, work));
		assertEquals("REJECTED_BEFORE_MUTATION", result.getOutcome().name(),
				PreG9BCu0Baseline.pinMessage("B01", "CU4", "the P4 matrix rejects the model"));
		String message = result.getMessage();
		for (String name : List.of("text4 (Text)", "α_C (Angle)",
				"α_{cyl} (Angle)")) {
			assertTrue(message.contains(name), PreG9BCu0Baseline.pinMessage("B01", "CU4",
					"rejected " + name));
		}
		assertEquals(0, app.getKernel().getConstruction().getGeoSetConstructionOrder().size());
	}

	@Test
	void geoCeDGEntitiesIgnoreTheNoNewObjectsLabelingStyle() throws Exception {
		Map<String, Boolean> visible = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.M);
			app.setLabelingStyle(ConstructionDefaults.LABEL_VISIBLE_ALWAYS_OFF);
			for (String command : List.of("A=(0,0)", "c=Circle(A,2)", "P=Point(c)",
					"Q=P+(1,1)", "loc=Locus(Q,P)", "L=LocusV2(Q,P)", "M=LocusLength(L)",
					"SP=SplineV2({(0,0),(1,2),(3,1),(4,0)},3)")) {
				PreG9BR6PlusF3Fixtures.must(app, command);
			}
			Map<String, Boolean> labels = new java.util.LinkedHashMap<>();
			for (String label : List.of("A", "c", "P", "loc", "L", "M", "SP")) {
				labels.put(label, app.getKernel().lookupLabel(label).isLabelVisible());
			}
			return labels;
		});
		for (String label : List.of("A", "c", "P", "loc")) {
			assertFalse(visible.get(label), label + " follows the labeling style");
		}
		for (String label : List.of("L", "M", "SP")) {
			assertTrue(visible.get(label), PreG9BCu0Baseline.pinMessage("B05", "CU4", label
					+ " is created with a visible label under No New Objects"));
		}
	}

	@Test
	void typedSliderLosesNameAndValueAfterUndoUnderNoNewObjects() throws Exception {
		int[] modes = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.M);
			app.setLabelingStyle(ConstructionDefaults.LABEL_VISIBLE_ALWAYS_OFF);
			Kernel kernel = app.getKernel();
			GeoNumeric typed = (GeoNumeric) PreG9BR6PlusF3Fixtures.must(app, "a=2");
			typed.setEuclidianVisible(true);
			typed.setLabelMode(GeoElement.LABEL_NAME_VALUE);
			typed.updateRepaint();
			PreG9BR6PlusF3Fixtures.must(app, "b=Slider(0,10)");
			PreG9BR6PlusF3Fixtures.baseline(app);
			PreG9BR6PlusF3Fixtures.must(app, "P=(1,1)");
			PreG9BR6PlusF3Fixtures.store(app);
			kernel.getConstruction().getUndoManager().undo();
			PreG9BR6PlusF3Fixtures.awaitQuiescent(app);
			return new int[] {kernel.lookupLabel("a").getLabelMode(),
					kernel.lookupLabel("b").getLabelMode()};
		});
		assertEquals(GeoElement.LABEL_VALUE, modes[0], PreG9BCu0Baseline.pinMessage("M05.1",
				"CU4", "a typed number shown as name + value comes back as value only"));
		assertEquals(GeoElement.LABEL_NAME_VALUE, modes[1], "Slider() keeps name + value");
	}

	@Test
	void orientationCueAdmissionCountsOfTheAuthorModels() throws Exception {
		int[] counts = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			int[] result = new int[4];
			count(PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(PreG9BCu0Corpus.GGB, work)),
					result, 0);
			count(PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work)), result, 2);
			return result;
		});
		assertEquals(27, counts[0]);
		assertEquals(27, counts[1], "every line of the Classic model roots at segment f");
		assertEquals(41, counts[2]);
		assertEquals(19, counts[3], PreG9BCu0Baseline.pinMessage("M04", "CU5",
				"22 lines whose reference chain starts at OrthogonalLine(A, xAxis) are excluded"));
	}

	private static void count(AppGeoCeDG app, int[] counts, int offset) {
		for (GeoElement geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			if (GeoCeDGOrientationCue.isCueType(geo)) {
				counts[offset]++;
				counts[offset + 1] += GeoCeDGOrientationCue.isAdmitted((GeoLine) geo) ? 1 : 0;
			}
		}
	}

	@Test
	void intersectionOutputsFollowTheIndexedOrder() throws Exception {
		List<String> mismatches = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.M);
			for (String command : List.of("c=Circle((0,0),3)", "k=1",
					"n=Segment((-5,k),(5,k))", "Intersect(c,n)", "I1=Intersect(c,n,1)",
					"I2=Intersect(c,n,2)")) {
				PreG9BR6PlusF3Fixtures.must(app, command);
			}
			List<String> errors = new java.util.ArrayList<>();
			for (double k : new double[] {2, 2.9, 2.99, 1, -1, -2.9}) {
				PreG9BCu0Corpus.setNumber(app, "k", k);
				compare(app, "A", "I1", k, errors);
				compare(app, "B", "I2", k, errors);
			}
			return errors;
		});
		assertEquals(List.of(), mismatches);
	}

	private static void compare(AppGeoCeDG app, String output, String indexed, double k,
			List<String> errors) {
		GeoPoint a = (GeoPoint) app.getKernel().lookupLabel(output);
		GeoPoint b = (GeoPoint) app.getKernel().lookupLabel(indexed);
		if (Math.abs(a.getInhomX() - b.getInhomX()) > 1e-12
				|| Math.abs(a.getInhomY() - b.getInhomY()) > 1e-12) {
			errors.add(output + "!=" + indexed + " at k=" + k);
		}
	}

	@Test
	void memorySlotStaysHeldBeforeItsReferenceExists() throws Exception {
		String[] states = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.M);
			for (String command : List.of("A=(0,0)", "B=(3,1)", "l=Line(A,B)",
					"m=Line(A,(0,4))")) {
				PreG9BR6PlusF3Fixtures.must(app, command);
			}
			PreG9BR6PlusF3Fixtures.baseline(app);
			GeoCeDGLineReferenceMemory memory = ((GeoCeDGEuclidianController) app
					.getActiveEuclidianView().getEuclidianController()).getReferenceMemory();
			GeoElement reference = app.getKernel().lookupLabel("l");
			memory.remember(Slot.PARALLEL, reference);
			app.getKernel().setConstructionStep(1);
			String early = String.valueOf(memory.getState(Slot.PARALLEL));
			boolean available = reference.isAvailableAtConstructionStep(
					app.getKernel().getConstructionStep())
					|| app.getKernel().lookupLabel("l") != null;
			app.getKernel().lastStep();
			return new String[] {early, String.valueOf(available),
					String.valueOf(memory.getState(Slot.PARALLEL))};
		});
		assertEquals("false", states[1], "l does not exist at the current protocol step");
		assertEquals("HELD", states[0], PreG9BCu0Baseline.pinMessage("H05", "CU5",
				"the slot reports HELD for a reference that does not exist at this step"));
		assertEquals("HELD", states[2]);
	}
}
