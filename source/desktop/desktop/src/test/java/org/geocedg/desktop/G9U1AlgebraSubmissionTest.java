/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.G9U1TestApp.lookup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.desktop.gui.inputbar.AlgebraInputD;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Real input adapter, explicit G9A transactions and ordinary native persistence. */
class G9U1AlgebraSubmissionTest {

	@Test
	void explicitSubmissionCreatesOneObjectAndOneUndoTransaction() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		// Establish the existing native-document baseline synchronously. An
		// asynchronous init can race the fixture's immediate first submission.
		UndoManagerD undo = (UndoManagerD) app.getKernel().getConstruction().getUndoManager();
		try (var baseline = undo.prepareUndoBaseline()) {
			undo.commitUndoBaseline(baseline);
		}
		int before = app.getKernel().getConstruction().getGeoSetConstructionOrder().size();
		submit(app, "k=0.25");
		assertEquals(before + 1,
				app.getKernel().getConstruction().getGeoSetConstructionOrder().size());
		assertEquals(0.25, ((GeoNumeric) lookup(app, "k")).getDouble());
		await(
				() -> app.getKernel().getConstruction().getUndoManager().undoPossible());
		app.getKernel().undo();
		await(
				() -> app.getKernel().lookupLabel("k") == null);
		assertFalse(app.getKernel().getConstruction().getUndoManager().undoPossible());
		app.getKernel().redo();
		await(
				() -> app.getKernel().lookupLabel("k") != null);
		assertEquals(0.25, ((GeoNumeric) lookup(app, "k")).getDouble());
	}

	@Test
	void focusLossAndEscapeNeverSubmitInput() {
		AppGeoCeDG app = G9U1TestApp.create();
		AlgebraInputD input = new AlgebraInputD(app);
		input.getTextField().setText("k=2");
		input.focusLost(new FocusEvent(input, FocusEvent.FOCUS_LOST));
		assertNull(app.getKernel().lookupLabel("k"));
		input.keyPressed(new KeyEvent(input, KeyEvent.KEY_PRESSED,
				0, 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED));
		assertEquals("", input.getTextField().getText());
		assertNull(app.getKernel().lookupLabel("k"));
	}

	@Test
	void canceledSliderPromptCreatesNothingAndNextEnterStillWorks() {
		AppGeoCeDG app = G9U1TestApp.create();
		AlgebraInputD input = new AlgebraInputD(app);
		String xml = app.getXML();
		try (org.mockito.MockedStatic<javax.swing.JOptionPane> dialogs =
				org.mockito.Mockito.mockStatic(javax.swing.JOptionPane.class)) {
			dialogs.when(() -> javax.swing.JOptionPane.showOptionDialog(
					org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
					org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyInt(),
					org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.any(),
					org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
					.thenReturn(1);
			input.getTextField().setText("k=unknownInputVariable");
			input.keyPressed(new KeyEvent(input, KeyEvent.KEY_PRESSED,
					0, 0, KeyEvent.VK_ENTER, '\n'));
			assertEquals(xml, app.getXML());
			input.getTextField().setText("k=1");
			input.keyPressed(new KeyEvent(input, KeyEvent.KEY_PRESSED,
					0, 0, KeyEvent.VK_ENTER, '\n'));
			assertEquals(1, ((GeoNumeric) lookup(app, "k")).getDouble());
		}
	}

	@Test
	void existingNumericRedefinePreservesIdentityAcrossZeroAndNativeReopen(
			@TempDir Path directory) throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		for (String command : new String[] {"k=1", "A=(-2,0)", "B=(0,0)",
				"C=(2,0)", "S=SplineV2({A,B,C},3)", "O=(0,0)",
				"T=Dilate(S,k,O)", "M=Length(T)"}) {
			eval(app, command);
		}
		PersistentGeoId id = app.getKernel().getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(lookup(app, "k"));
		assertNotNull(id);
		for (double value : new double[] {0.25, -2, 0, 1}) {
			submit(app, "k=" + value);
			assertEquals(id, app.getKernel().getConstruction().getSpatialIdentityRegistry()
					.getPersistentGeoId(lookup(app, "k")));
			assertEquals(4 * Math.abs(value), ((GeoNumeric) lookup(app, "M")).getDouble(), 1E-8);
			assertTrue(((GeoLocusV2) lookup(app, "T")).isDefined());
			Path file = directory.resolve("redefine-" + value + ".cedg");
			assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file.toFile()));
			AppGeoCeDG reopened = G9U1TestApp.create();
			assertTrue(reopened.loadFile(file.toFile(), false));
			assertEquals(id, reopened.getKernel().getConstruction().getSpatialIdentityRegistry()
					.getPersistentGeoId(lookup(reopened, "k")));
			app = reopened;
		}
	}

	@Test
	void noneditableSemanticDefinitionCannotBeSilentlyReplaced() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		eval(app, "S=SplineV2({(0,0),(1,0),(2,0)},3)");
		String xml = app.getXML();
		ErrorAccumulator errors = new ErrorAccumulator();
		AtomicReference<GeoElementND[]> result = new AtomicReference<>();
		GeoCeDGAlgebraInputSubmission.submit(app, "S=5", new EvalInfo(true), errors,
				result::set);
		assertNull(result.get());
		assertFalse(errors.getErrors().isEmpty());
		assertEquals(xml, app.getXML());
	}

	@Test
	void explicitRichQueryAutoMaterializationIsOneUndoAndNeverRunsOnSelection()
			throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		eval(app, "S=SplineV2({(-2,0),(0,0),(2,0)},3)");
		eval(app, "axis:x=0");
		long originalPoints = app.getKernel().getConstruction().getGeoSetConstructionOrder()
				.stream().filter(GeoElement::isGeoPoint).count();
		UndoManagerD undo = (UndoManagerD) app.getKernel().getConstruction().getUndoManager();
		try (var baseline = undo.prepareUndoBaseline()) {
			undo.commitUndoBaseline(baseline);
		}
		GeoCeDGEuclidianController controller = (GeoCeDGEuclidianController)
				app.getEuclidianView1().getEuclidianController();
		controller.setAutoMaterializeIntersectionSolutions(true);
		submit(app, "R=Intersect(S,axis)");
		assertEquals(originalPoints + 1, app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().stream().filter(GeoElement::isGeoPoint).count());
		await(() -> undo.getHistorySize() == 1);
		app.getKernel().undo();
		await(() -> app.getKernel().getConstruction().getGeoSetConstructionOrder()
				.stream().filter(GeoElement::isGeoPoint).count() == originalPoints);
		assertNull(app.getKernel().lookupLabel("R"));
		assertFalse(undo.undoPossible());
		app.getKernel().redo();
		await(() -> app.getKernel().lookupLabel("R") != null);
		assertEquals(originalPoints + 1, app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().stream().filter(GeoElement::isGeoPoint).count());
		app.getSelectionManager().setSelectedGeos(java.util.List.of(lookup(app, "R")));
		assertEquals(originalPoints + 1, app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().stream().filter(GeoElement::isGeoPoint).count());
	}

	@Test
	void invalidSyntaxCannotPartiallyMutateExistingTarget() {
		AppGeoCeDG app = G9U1TestApp.create();
		eval(app, "k=1");
		String xml = app.getXML();
		org.junit.jupiter.api.Assertions.assertThrows(org.geogebra.common.main.BracketsError.class,
				() -> submit(app, "k=("));
		assertEquals(xml, app.getXML());
	}

	private static GeoElement submit(AppGeoCeDG app, String command) throws Exception {
		AtomicReference<GeoElementND[]> output = new AtomicReference<>();
		ErrorAccumulator errors = new ErrorAccumulator();
		GeoCeDGAlgebraInputSubmission.submit(app, command,
				new EvalInfo(true, true).withSliders(true).withSymbolic(true), errors, output::set);
		assertEquals("", errors.getErrors(), command);
		assertNotNull(output.get(), command);
		assertEquals(1, output.get().length);
		return output.get()[0].toGeoElement();
	}

	private static void await(BooleanSupplier condition) throws InterruptedException {
		long deadline = System.nanoTime() + 10_000_000_000L;
		while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
			Thread.sleep(10);
		}
		assertTrue(condition.getAsBoolean(), "Desktop undo operation did not complete");
	}
}
