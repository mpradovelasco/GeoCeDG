/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.geocedg.common.kernel.dimension.AlgoNativeDimension;
import org.geocedg.desktop.GeoCeDGDimensionOffsetConversion.Eligibility;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.euclidian.event.AbstractEvent;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.algos.ConstructionElement;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.awt.GGraphics2DD;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.euclidian.event.MouseEventD;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.main.AppD;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

/**
 * POST-E2-P4: the explicit "Make offset draggable" conversion of a native dimension
 * typed with a literal offset. The literal number becomes a free, labelled, unlocked
 * number in place: identities, value, geometry, dependents and the command signature are
 * kept; one undo point; deterministic persistence; ineligible cases change nothing.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PostE2P4OffsetConversionTest {

	@TempDir
	Path temporary;

	private AppGeoCeDG app;
	private GeoCeDGDimensionOffsetConversion conversion;

	@BeforeEach
	void setUp() {
		app = G9U1TestApp.create();
		conversion = new GeoCeDGDimensionOffsetConversion(app);
	}

	private static AlgoNativeDimension dimension(AppD app, String command) {
		GeoElementND[] result = app.getKernel().getAlgebraProcessor()
				.processAlgebraCommand(command, false);
		assertNotNull(result, command);
		AlgoNativeDimension owner = AlgoNativeDimension.ownerOf(result[0].toGeoElement());
		assertNotNull(owner, command);
		return owner;
	}

	private static String definition(AlgoNativeDimension owner) {
		return owner.getDefinition(StringTemplate.xmlTemplate);
	}

	private static String geometry(AlgoNativeDimension owner) {
		StringBuilder text = new StringBuilder();
		text.append(owner.getValue().getDouble());
		for (GeoSegment segment : new GeoSegment[] {owner.getDimensionLine(),
				owner.getExtensionA(), owner.getExtensionB()}) {
			text.append(' ').append(segment.getStartPoint().getInhomX()).append(',')
					.append(segment.getStartPoint().getInhomY()).append(':')
					.append(segment.getEndPoint().getInhomX()).append(',')
					.append(segment.getEndPoint().getInhomY());
		}
		return text.append(' ').append(owner.getPresentationText().getTextString())
				.toString();
	}

	private static List<ConstructionElement> order(Construction cons) {
		List<ConstructionElement> elements = new ArrayList<>();
		for (int i = 0; i < cons.steps(); i++) {
			elements.add(cons.getConstructionElement(i));
		}
		return elements;
	}

	private static UndoManagerD undoBaseline(AppD app) throws Exception {
		UndoManagerD undo = (UndoManagerD) app.getKernel().getConstruction()
				.getUndoManager();
		try (var baseline = undo.prepareUndoBaseline()) {
			undo.commitUndoBaseline(baseline);
		}
		return undo;
	}

	private static void await(BooleanSupplier condition) throws Exception {
		long deadline = System.nanoTime() + 5_000_000_000L;
		while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
			Thread.sleep(10);
		}
		assertTrue(condition.getAsBoolean());
	}

	@Test
	void anAlignedLiteralOffsetBecomesANamedFreeNumberInPlace() throws Exception {
		G9U1TestApp.eval(app, "A=(0,0)");
		G9U1TestApp.eval(app, "B=(4,0)");
		AlgoNativeDimension owner = dimension(app, "AlignedDimension(A,B,1)");
		final GeoNumeric dependent = (GeoNumeric) G9U1TestApp.eval(app,
				"m=2*" + owner.getValue().getLabelSimple());
		final GeoPoint middle = (GeoPoint) G9U1TestApp.eval(app,
				"M=Midpoint(" + owner.getDimensionLine().getLabelSimple() + ")");
		G9U1TestApp.eval(app, "Q=(9,9)");
		final Construction cons = app.getKernel().getConstruction();
		GeoElement literal = owner.getOffsetInput().toGeoElement();
		final GeoElement[] outputs = owner.getOutput().clone();
		final String geometry = geometry(owner);
		assertEquals("AlignedDimension[A, B, 1]", definition(owner));
		assertFalse(GeoCeDGEuclidianController.isDraggableOffset(owner));
		assertEquals(Eligibility.ELIGIBLE, conversion.eligibility(owner));

		GeoNumeric number = conversion.convert(owner);

		assertSame(literal, number, "the literal number is labelled in place");
		assertEquals("offset1", number.getLabelSimple());
		assertTrue(number.isIndependent() && number.isLabelSet() && !number.isLocked());
		assertTrue(number.isAuxiliaryObject() && !number.isEuclidianVisible(),
				"hidden auxiliary like a tool-created offset (section 8.2)");
		assertSame(number, app.getKernel().lookupLabel("offset1"));
		assertEquals("AlignedDimension[A, B, offset1]", definition(owner));
		assertTrue(GeoCeDGEuclidianController.isDraggableOffset(owner));
		assertEquals(Eligibility.ALREADY_DRAGGABLE, conversion.eligibility(owner));
		for (int i = 0; i < outputs.length; i++) {
			assertSame(outputs[i], owner.getOutput(i), "output " + i + " keeps its identity");
		}
		assertSame(owner, AlgoNativeDimension.ownerOf(outputs[0]));
		assertEquals(geometry, geometry(owner), "value and geometry identical");
		assertEquals(8, dependent.getDouble(), 0);
		assertSame(dependent, app.getKernel().lookupLabel("m"));
		assertSame(middle, app.getKernel().lookupLabel("M"));
		assertEquals(2, middle.getInhomX(), 0);
		assertEquals(1, middle.getInhomY(), 0);
		assertEquals(owner.getConstructionIndex() - 1, number.getConstructionIndex(),
				"the number is ordered just before its dimension");
		assertEquals(1, number.getAlgorithmList().size());
		String xml = app.getXML();
		int element = xml.indexOf("label=\"offset1\"");
		int command = xml.indexOf("<command name=\"AlignedDimension\"");
		assertTrue(element > 0 && element < command, "persisted before the command");
		assertTrue(xml.contains("<input a0=\"A\" a1=\"B\" a2=\"offset1\"/>"));

		// the repeated action is redundant and changes nothing
		List<ConstructionElement> converted = order(cons);
		try (MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
			assertNull(conversion.convert(owner));
			assertFalse(conversion.run(owner));
			dialogs.verify(() -> JOptionPane.showMessageDialog(any(), any(), anyString(),
					anyInt()), times(1));
		}
		assertEquals(xml, app.getXML());
		assertEquals(converted, order(cons));
	}

	@Test
	void linearAndFiveArgumentDimensionsConvertOnlyTheirOffset() {
		G9U1TestApp.eval(app, "A=(0,0)");
		G9U1TestApp.eval(app, "B=(4,3)");
		G9U1TestApp.eval(app, "v=Vector((1,1))");
		AlgoNativeDimension line = dimension(app, "LinearDimension(A,B,xAxis,1.5)");
		AlgoNativeDimension vector = dimension(app, "LinearDimension(A,B,v,-2)");
		AlgoNativeDimension full = dimension(app,
				"LinearDimension(A,B,yAxis,-0.25,0.2,0.1)");
		AlgoNativeDimension aligned = dimension(app, "AlignedDimension(A,B,3,0.2,0.1)");
		List<String> before = new ArrayList<>();
		for (AlgoNativeDimension owner : List.of(line, vector, full, aligned)) {
			before.add(geometry(owner));
			assertEquals(Eligibility.ELIGIBLE, conversion.eligibility(owner));
		}
		assertEquals("offset1", conversion.convert(line).getLabelSimple());
		assertEquals("offset2", conversion.convert(vector).getLabelSimple());
		assertEquals("offset3", conversion.convert(full).getLabelSimple());
		assertEquals("offset4", conversion.convert(aligned).getLabelSimple());
		assertEquals("LinearDimension[A, B, xAxis, offset1]", definition(line));
		assertEquals("LinearDimension[A, B, v, offset2]", definition(vector));
		assertEquals("LinearDimension[A, B, yAxis, offset3, 0.2, 0.1]", definition(full),
				"overshoot and gap stay literal");
		assertEquals("AlignedDimension[A, B, offset4, 0.2, 0.1]", definition(aligned));
		List<AlgoNativeDimension> owners = List.of(line, vector, full, aligned);
		for (int i = 0; i < owners.size(); i++) {
			assertEquals(before.get(i), geometry(owners.get(i)));
			assertTrue(GeoCeDGEuclidianController.isDraggableOffset(owners.get(i)));
		}
		assertEquals(-2, ((GeoNumeric) app.getKernel().lookupLabel("offset2")).getDouble(), 0);
	}

	@Test
	void labelsAvoidExistingNames() {
		G9U1TestApp.eval(app, "A=(0,0)");
		G9U1TestApp.eval(app, "B=(4,0)");
		GeoNumeric taken = (GeoNumeric) G9U1TestApp.eval(app, "offset1=7");
		G9U1TestApp.eval(app, "offset2=Text(\"x\")");
		AlgoNativeDimension owner = dimension(app, "AlignedDimension(A,B,1)");
		assertEquals("offset3", conversion.convert(owner).getLabelSimple());
		assertSame(taken, app.getKernel().lookupLabel("offset1"));
		assertEquals(7, taken.getDouble(), 0);
		assertEquals("AlignedDimension[A, B, offset3]", definition(owner));
	}

	@Test
	void draggingAfterConversionWritesOnlyTheNewNumber() throws Exception {
		EuclidianView view = app.getEuclidianView1();
		SwingUtilities.invokeAndWait(() -> {
			((EuclidianViewD) view).getJPanel().setSize(new Dimension(800, 600));
			view.updateSize();
			view.setCoordSystem(400, 300, 50, 50);
			view.setShowAxes(false, false);
			view.showGrid(false);
		});
		final GeoPoint a = (GeoPoint) G9U1TestApp.eval(app, "A=(-2,0)");
		final GeoPoint b = (GeoPoint) G9U1TestApp.eval(app, "B=(2,0)");
		AlgoNativeDimension owner = dimension(app, "AlignedDimension(A,B,1)");
		GeoCeDGEuclidianController controller = (GeoCeDGEuclidianController) view
				.getEuclidianController();
		app.setMode(EuclidianConstants.MODE_MOVE);
		paint(app);
		// before the conversion the literal offset is not dragged (section 8.4)
		drag(controller, -1, 1, 3);
		assertEquals(1, ((GeoNumeric) owner.getOffsetInput()).getDouble(), 0);
		GeoNumeric number = conversion.convert(owner);
		UndoManagerD undo = undoBaseline(app);
		CountDownLatch stored = new CountDownLatch(1);
		undo.addUndoInfoStoredListener(stored::countDown);
		paint(app);
		drag(controller, -1, 1, 3);
		assertEquals(3, number.getDouble(), 1E-9, "the named offset follows the pointer");
		assertSame(number, owner.getOffsetInput());
		assertEquals(3, owner.getDimensionLine().getStartPoint().getInhomY(), 1E-9);
		assertEquals(-2, a.getInhomX(), 0);
		assertEquals(0, a.getInhomY(), 0);
		assertEquals(2, b.getInhomX(), 0);
		assertEquals(0, b.getInhomY(), 0);
		assertTrue(stored.await(5, TimeUnit.SECONDS), "the drag stores one undo point");
	}

	private static void drag(GeoCeDGEuclidianController controller, double x, double from,
			double to) {
		controller.wrapMousePressed(event(controller, x, from));
		controller.wrapMouseDragged(event(controller, x, (from + to) / 2), true);
		controller.wrapMouseDragged(event(controller, x, to), true);
		controller.wrapMouseReleased(event(controller, x, to));
	}

	private static AbstractEvent event(GeoCeDGEuclidianController controller, double x,
			double y) {
		EuclidianView view = controller.getView();
		return MouseEventD.wrapEvent(new MouseEvent(((EuclidianViewD) view).getJPanel(),
				MouseEvent.MOUSE_PRESSED, 1, 0, view.toScreenCoordX(x), view.toScreenCoordY(y),
				1, false, MouseEvent.BUTTON1));
	}

	private static void paint(AppD app) throws Exception {
		EuclidianView view = app.getEuclidianView1();
		BufferedImage image = new BufferedImage(view.getWidth(), view.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		SwingUtilities.invokeAndWait(() -> {
			view.updateAllDrawables(true);
			Graphics2D graphics = image.createGraphics();
			view.paint(new GGraphics2DD(graphics));
			graphics.dispose();
		});
	}

	@Test
	void oneUndoStepRevertsAndOneRedoStepRestoresTheConversion() throws Exception {
		G9U1TestApp.eval(app, "A=(0,0)");
		G9U1TestApp.eval(app, "B=(4,0)");
		AlgoNativeDimension owner = dimension(app, "AlignedDimension(A,B,1)");
		String value = owner.getValue().getLabelSimple();
		final String literalXml = app.getXML();
		UndoManagerD undo = undoBaseline(app);
		AtomicInteger stores = new AtomicInteger();
		CountDownLatch stored = new CountDownLatch(1);
		undo.addUndoInfoStoredListener(() -> {
			stores.incrementAndGet();
			stored.countDown();
		});
		assertTrue(conversion.run(owner));
		assertTrue(stored.await(5, TimeUnit.SECONDS));
		final String convertedXml = app.getXML();
		assertEquals(1, stores.get(), "one undo point");

		app.getKernel().undo();
		await(() -> app.getKernel().lookupLabel("offset1") == null);
		AlgoNativeDimension undone = AlgoNativeDimension
				.ownerOf(app.getKernel().lookupLabel(value));
		assertEquals("AlignedDimension[A, B, 1]", definition(undone));
		assertFalse(GeoCeDGEuclidianController.isDraggableOffset(undone));
		assertEquals(literalXml, app.getXML(), "no partial or orphan state after undo");

		app.getKernel().redo();
		await(() -> app.getKernel().lookupLabel("offset1") != null);
		AlgoNativeDimension redone = AlgoNativeDimension
				.ownerOf(app.getKernel().lookupLabel(value));
		assertEquals("AlignedDimension[A, B, offset1]", definition(redone));
		assertTrue(GeoCeDGEuclidianController.isDraggableOffset(redone));
		assertEquals(convertedXml, app.getXML());
		assertEquals(1, stores.get(), "undo and redo store nothing");
	}

	@Test
	void saveAndReopenRestoreTheConvertedConstructionDeterministically() throws Exception {
		G9U1TestApp.eval(app, "A=(0,0)");
		G9U1TestApp.eval(app, "B=(4,0)");
		AlgoNativeDimension owner = dimension(app, "AlignedDimension(A,B,1)");
		G9U1TestApp.eval(app, "m=2*" + owner.getValue().getLabelSimple());
		String value = owner.getValue().getLabelSimple();
		final String geometry = geometry(owner);
		conversion.convert(owner);
		File file = temporary.resolve("converted.cedg").toFile();
		assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file));

		AppGeoCeDG reopened = G9U1TestApp.create();
		reopened.loadFile(file, false);
		AlgoNativeDimension restored = AlgoNativeDimension
				.ownerOf(reopened.getKernel().lookupLabel(value));
		GeoElement number = reopened.getKernel().lookupLabel("offset1");
		assertNotNull(number);
		assertSame(number, restored.getOffsetInput());
		assertTrue(number.isIndependent() && !number.isLocked() && number.isAuxiliaryObject());
		assertEquals("AlignedDimension[A, B, offset1]", definition(restored));
		assertTrue(GeoCeDGEuclidianController.isDraggableOffset(restored));
		assertEquals(geometry, geometry(restored));
		assertEquals(8, ((GeoNumeric) reopened.getKernel().lookupLabel("m")).getDouble(), 0);
		String once = reopened.getKernel().getConstruction().getCurrentUndoXML(false)
				.toString();
		AppGeoCeDG again = G9U1TestApp.create();
		again.setXML(reopened.getXML(), true);
		assertEquals(once, again.getKernel().getConstruction().getCurrentUndoXML(false)
				.toString(), "a reopened conversion re-saves identically");
		assertEquals(app.getKernel().getConstruction().getCurrentUndoXML(false).toString(),
				once, "the converted construction persists as it was in memory");
	}

	@Test
	void expressionsNamedLockedAndUndefinedOffsetsAreNotConverted() {
		G9U1TestApp.eval(app, "A=(0,0)");
		G9U1TestApp.eval(app, "B=(4,0)");
		G9U1TestApp.eval(app, "s=2");
		GeoNumeric locked = (GeoNumeric) G9U1TestApp.eval(app, "t=1");
		locked.setFixed(true);
		G9U1TestApp.eval(app, "u=s+1");
		final GeoPoint undefinedPoint = (GeoPoint) G9U1TestApp.eval(app, "C=(1,1)");
		assertEquals(Eligibility.EXPRESSION, eligibility("AlignedDimension(A,B,2*3)"));
		assertEquals(Eligibility.EXPRESSION, eligibility("AlignedDimension(A,B,pi)"));
		assertEquals(Eligibility.EXPRESSION, eligibility("AlignedDimension(A,B,1/3)"));
		assertEquals(Eligibility.EXPRESSION, eligibility("AlignedDimension(A,B,sqrt(2))"));
		assertEquals(Eligibility.EXPRESSION, eligibility("AlignedDimension(A,B,s+1)"));
		assertEquals(Eligibility.EXPRESSION, eligibility("AlignedDimension(A,B,x(A)+1)"));
		assertEquals(Eligibility.ALREADY_DRAGGABLE, eligibility("AlignedDimension(A,B,s)"));
		assertEquals(Eligibility.NAMED_OFFSET, eligibility("AlignedDimension(A,B,t)"));
		assertEquals(Eligibility.NAMED_OFFSET, eligibility("LinearDimension(A,B,xAxis,u)"));
		AlgoNativeDimension undefined = dimension(app, "AlignedDimension(A,C,1)");
		undefinedPoint.setUndefined();
		undefinedPoint.updateCascade();
		assertEquals(Eligibility.UNDEFINED, conversion.eligibility(undefined));

		String xml = app.getXML();
		Construction cons = app.getKernel().getConstruction();
		List<ConstructionElement> order = order(cons);
		try (MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
			for (ConstructionElement element : order) {
				if (element instanceof AlgoNativeDimension) {
					AlgoNativeDimension owner = (AlgoNativeDimension) element;
					assertNull(conversion.convert(owner));
					assertFalse(conversion.run(owner));
					JMenuItem item = conversion.menuItem(List.of(owner.getValue()));
					assertFalse(item.isEnabled(), definition(owner));
					assertEquals(GeoCeDGProfile.getText(conversion.eligibility(owner)
							.getReasonKey(), "en"), item.getToolTipText());
				}
			}
		}
		assertEquals(xml, app.getXML(), "unavailable cases leave the construction unchanged");
		assertEquals(order, order(cons));
		assertNull(app.getKernel().lookupLabel("offset1"));
	}

	private Eligibility eligibility(String command) {
		return conversion.eligibility(dimension(app, command));
	}

	@Test
	void aConstructionShownAtAnEarlierStepOrAMacroResultIsNotConverted() throws Exception {
		G9U1TestApp.eval(app, "A=(0,0)");
		G9U1TestApp.eval(app, "B=(4,0)");
		AlgoNativeDimension owner = dimension(app, "AlignedDimension(A,B,1)");
		G9U1TestApp.eval(app, "Q=(9,9)");
		Construction cons = app.getKernel().getConstruction();
		cons.setStep(cons.steps() - 2);
		assertEquals(Eligibility.UNAVAILABLE, conversion.eligibility(owner));
		assertNull(conversion.convert(owner));
		cons.setStep(cons.steps() - 1);
		assertEquals(Eligibility.ELIGIBLE, conversion.eligibility(owner));

		// a dimension produced by a user macro is not a native dimension output here
		GeoElement a = app.getKernel().lookupLabel("A");
		GeoElement b = app.getKernel().lookupLabel("B");
		Macro macro = new Macro(app.getKernel(), "DimTool", new GeoElement[] {a, b},
				new GeoElement[] {owner.getValue()});
		app.getKernel().addMacro(macro);
		G9U1TestApp.eval(app, "P=(1,5)");
		G9U1TestApp.eval(app, "R=(3,5)");
		GeoElement result = G9U1TestApp.eval(app, "DimTool(P,R)");
		assertNull(AlgoNativeDimension.ownerOf(result));
		assertNull(conversion.menuItem(List.of(result)));
		assertNull(GeoCeDGDimensionOffsetConversion.contextDimension(
				List.of(owner.getValue(), a)), "not one semantic dimension");
		assertSame(owner, GeoCeDGDimensionOffsetConversion.contextDimension(
				List.of(owner.getDimensionLine(), owner.getPresentationText())));
	}

	@Test
	void aFailureAfterLabellingRestoresTheLiteralAndStoresNothing() throws Exception {
		G9U1TestApp.eval(app, "A=(0,0)");
		G9U1TestApp.eval(app, "B=(4,0)");
		AlgoNativeDimension owner = dimension(app, "AlignedDimension(A,B,1)");
		G9U1TestApp.eval(app, "m=2*" + owner.getValue().getLabelSimple());
		Construction cons = app.getKernel().getConstruction();
		GeoElement literal = owner.getOffsetInput().toGeoElement();
		final String xml = app.getXML();
		final List<ConstructionElement> order = order(cons);
		final int step = cons.getStep();
		UndoManagerD undo = undoBaseline(app);
		AtomicInteger stores = new AtomicInteger();
		undo.addUndoInfoStoredListener(stores::incrementAndGet);
		GeoCeDGDimensionOffsetConversion failing = new GeoCeDGDimensionOffsetConversion(app,
				() -> {
					throw new IllegalStateException("injected");
				});
		assertThrows(IllegalStateException.class, () -> failing.convert(owner));
		try (MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
			assertFalse(failing.run(owner));
			dialogs.verify(() -> JOptionPane.showMessageDialog(any(), any(), anyString(),
					anyInt()), times(1));
		}
		Thread.sleep(200);
		assertEquals(0, stores.get(), "a failed conversion stores no undo point");
		assertSame(literal, owner.getOffsetInput());
		assertFalse(literal.isLabelSet());
		assertFalse(literal.isInConstructionList());
		assertFalse(literal.isAuxiliaryObject());
		assertNull(app.getKernel().lookupLabel("offset1"), "no orphan number");
		assertEquals(order, order(cons));
		assertEquals(step, cons.getStep());
		assertEquals(xml, app.getXML());
		assertEquals(Eligibility.ELIGIBLE, conversion.eligibility(owner));
		assertEquals("offset1", conversion.convert(owner).getLabelSimple(),
				"a later conversion still succeeds");
	}

	@Test
	void theObjectContextMenuOffersTheActionForOneDimensionOnly() throws Exception {
		G9U1TestApp.eval(app, "A=(0,0)");
		G9U1TestApp.eval(app, "B=(4,0)");
		AlgoNativeDimension owner = dimension(app, "AlignedDimension(A,B,1)");
		GuiManagerGeoCeDG gui = (GuiManagerGeoCeDG) app.getGuiManager();
		for (GeoElement output : owner.getOutput()) {
			JPopupMenu menu = new JPopupMenu();
			gui.decorateProductContextMenu(menu, List.of(output));
			JMenuItem item = offsetItem(menu);
			assertNotNull(item, output.getLabelSimple());
			assertEquals("Make offset draggable", item.getText());
			assertTrue(item.isEnabled());
		}
		JPopupMenu other = new JPopupMenu();
		gui.decorateProductContextMenu(other, List.of(app.getKernel().lookupLabel("A")));
		assertNull(offsetItem(other), "no action for a point");
		JPopupMenu menu = new JPopupMenu();
		gui.decorateProductContextMenu(menu, List.of(owner.getDimensionLine()));
		UndoManagerD undo = undoBaseline(app);
		CountDownLatch stored = new CountDownLatch(1);
		undo.addUndoInfoStoredListener(stored::countDown);
		offsetItem(menu).doClick();
		assertTrue(stored.await(5, TimeUnit.SECONDS));
		assertEquals("AlignedDimension[A, B, offset1]", definition(owner));
		JPopupMenu after = new JPopupMenu();
		gui.decorateProductContextMenu(after, List.of(owner.getDimensionLine()));
		assertFalse(offsetItem(after).isEnabled(), "already draggable");
		app.setLanguage(java.util.Locale.forLanguageTag("es"));
		JPopupMenu spanish = new JPopupMenu();
		gui.decorateProductContextMenu(spanish, List.of(owner.getDimensionLine()));
		assertEquals("Hacer desplazamiento arrastrable", offsetItem(spanish).getText());
	}

	private static JMenuItem offsetItem(JPopupMenu menu) {
		for (int i = 0; i < menu.getComponentCount(); i++) {
			if (menu.getComponent(i) instanceof JMenuItem) {
				JMenuItem item = (JMenuItem) menu.getComponent(i);
				if (item.getText() != null && (item.getText().equals("Make offset draggable")
						|| item.getText().equals("Hacer desplazamiento arrastrable"))) {
					return item;
				}
			}
		}
		return null;
	}

	@Test
	void classicHasNoActionAndReadsAConvertedDocumentUnchanged() throws Exception {
		G9U1TestApp.eval(app, "A=(0,0)");
		G9U1TestApp.eval(app, "B=(4,0)");
		AlgoNativeDimension owner = dimension(app, "AlignedDimension(A,B,1)");
		final String value = owner.getValue().getLabelSimple();
		conversion.convert(owner);
		String xml = app.getXML();

		AppD classic = G9U1TestApp.withoutWindowDispatcher(new AppD(new CommandLineArguments(
				new String[] {"--silent"}), new JPanel(), true));
		assertFalse(classic.getGuiManager() instanceof GuiManagerGeoCeDG);
		Method decorate = GuiManagerD.class.getDeclaredMethod("decorateProductContextMenu",
				JPopupMenu.class);
		decorate.setAccessible(true);
		JPopupMenu menu = new JPopupMenu();
		decorate.invoke(classic.getGuiManager(), menu);
		assertEquals(0, menu.getComponentCount(), "Classic context menus are unchanged");

		classic.setXML(xml, true);
		GeoElement number = classic.getKernel().lookupLabel("offset1");
		assertNotNull(number);
		assertTrue(number.isIndependent());
		assertEquals(1, ((GeoNumeric) number).getDouble(), 0);
		AlgoNativeDimension read = AlgoNativeDimension
				.ownerOf(classic.getKernel().lookupLabel(value));
		assertSame(number, read.getOffsetInput());
		assertEquals(geometry(owner), geometry(read));
	}
}
