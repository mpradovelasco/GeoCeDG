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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.geocedg.common.kernel.dimension.AlgoNativeDimension;
import org.geogebra.common.awt.GPoint;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.desktop.awt.GGraphics2DD;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.euclidian.event.MouseEventD;
import org.geogebra.desktop.gui.ContextMenuChooseGeoD;
import org.geogebra.desktop.main.AppD;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

/**
 * POST-E2-P4 follow-up: a right click in the Graphics View opens the inherited chooser
 * menu even for one object. For outputs of one native dimension that menu carries the
 * same "Make offset draggable" action as the object menu of the Algebra View; every
 * other context keeps the inherited chooser. The real controller produces the right
 * click; only the final showing of the popup is captured.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PostE2P4GraphicsContextMenuTest {

	private static final String ACTION_EN = "Make offset draggable";
	private static final String ACTION_ES = "Hacer desplazamiento arrastrable";

	@TempDir
	Path temporary;

	private AppGeoCeDG app;
	private GuiManagerGeoCeDG gui;
	private final List<JPopupMenu> shown = new ArrayList<>();
	private final List<List<GeoElement>> inherited = new ArrayList<>();

	@BeforeEach
	void setUp() throws Exception {
		app = graphicsApp();
		gui = capture(app, shown, inherited);
	}

	private static AppGeoCeDG graphicsApp() throws Exception {
		AppGeoCeDG created = G9U1TestApp.create();
		EuclidianView view = created.getEuclidianView1();
		SwingUtilities.invokeAndWait(() -> {
			((EuclidianViewD) view).getJPanel().setSize(new Dimension(800, 600));
			view.updateSize();
			view.setCoordSystem(400, 300, 50, 50);
			view.setShowAxes(false, false);
			view.showGrid(false);
		});
		created.setMode(EuclidianConstants.MODE_MOVE);
		return created;
	}

	/**
	 * Installs a spy of the product GUI manager: a Graphics View menu for a dimension runs
	 * the real override and is captured instead of shown; any other context is recorded
	 * as routed to the inherited chooser.
	 */
	private static GuiManagerGeoCeDG capture(AppGeoCeDG target, List<JPopupMenu> menus,
			List<List<GeoElement>> others) throws Exception {
		GuiManagerGeoCeDG spied = spy((GuiManagerGeoCeDG) target.getGuiManager());
		doAnswer(invocation -> {
			ArrayList<GeoElement> selected = invocation.getArgument(0);
			if (spied.chooseGeoOffsetItem(selected) == null) {
				others.add(new ArrayList<>(selected));
				return null;
			}
			return invocation.callRealMethod();
		}).when(spied).showPopupChooseGeo(any(), any(), any(EuclidianView.class),
				any(GPoint.class));
		doAnswer(invocation -> {
			menus.add(invocation.getArgument(0));
			return null;
		}).when(spied).showContextMenu(any(), any(), any());
		Field field = AppD.class.getDeclaredField("guiManager");
		field.setAccessible(true);
		field.set(target, spied);
		return spied;
	}

	private static AlgoNativeDimension dimension(AppD target, String command) {
		GeoElementND[] result = target.getKernel().getAlgebraProcessor()
				.processAlgebraCommand(command, false);
		assertNotNull(result, command);
		return AlgoNativeDimension.ownerOf(result[0].toGeoElement());
	}

	private static void paint(AppD target) throws Exception {
		EuclidianView view = target.getEuclidianView1();
		BufferedImage image = new BufferedImage(view.getWidth(), view.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		SwingUtilities.invokeAndWait(() -> {
			view.updateAllDrawables(true);
			Graphics2D graphics = image.createGraphics();
			view.paint(new GGraphics2DD(graphics));
			graphics.dispose();
		});
	}

	/** A right click (press and release) at model coordinates through the controller. */
	private static void rightClick(AppD target, double x, double y) throws Exception {
		paint(target);
		EuclidianView view = target.getEuclidianView1();
		GeoCeDGEuclidianController controller = (GeoCeDGEuclidianController) view
				.getEuclidianController();
		int sx = view.toScreenCoordX(x);
		int sy = view.toScreenCoordY(y);
		for (int id : new int[] {MouseEvent.MOUSE_PRESSED, MouseEvent.MOUSE_RELEASED}) {
			MouseEvent event = new MouseEvent(((EuclidianViewD) view).getJPanel(), id, 1,
					InputEvent.BUTTON3_DOWN_MASK, sx, sy, 1, true, MouseEvent.BUTTON3);
			if (id == MouseEvent.MOUSE_PRESSED) {
				controller.wrapMousePressed(MouseEventD.wrapEvent(event));
			} else {
				controller.wrapMouseReleased(MouseEventD.wrapEvent(event));
			}
		}
	}

	private static JMenuItem action(JPopupMenu menu) {
		for (int i = 0; i < menu.getComponentCount(); i++) {
			Component component = menu.getComponent(i);
			if (component instanceof JMenuItem && !(component instanceof JMenu)) {
				String text = ((JMenuItem) component).getText();
				if (ACTION_EN.equals(text) || ACTION_ES.equals(text)) {
					return (JMenuItem) component;
				}
			}
		}
		return null;
	}

	private JPopupMenu lastMenu() {
		assertFalse(shown.isEmpty(), "a Graphics View dimension menu was built");
		return shown.get(shown.size() - 1);
	}

	private static String definition(AlgoNativeDimension owner) {
		return owner.getDefinition(StringTemplate.xmlTemplate);
	}

	private static String geometry(AlgoNativeDimension owner) {
		StringBuilder text = new StringBuilder().append(owner.getValue().getDouble());
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

	private static String constructionXml(AppD target) {
		// the construction itself; document ids and view settings are not part of it
		String xml = target.getKernel().getConstruction().getCurrentUndoXML(false)
				.toString();
		return xml.substring(xml.indexOf("<construction"),
				xml.indexOf("</construction>"));
	}

	private static UndoManagerD undoBaseline(AppD target) throws Exception {
		UndoManagerD undo = (UndoManagerD) target.getKernel().getConstruction()
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

	private void points() {
		G9U1TestApp.eval(app, "A=(-2,0)");
		G9U1TestApp.eval(app, "B=(2,0)");
	}

	@Test
	void caseAAnAlignedDimensionLineOffersTheEnabledActionAndConverts() throws Exception {
		points();
		AlgoNativeDimension owner = dimension(app, "AlignedDimension(A,B,1)");
		GeoElement literal = owner.getOffsetInput().toGeoElement();
		final String geometry = geometry(owner);
		final String before = constructionXml(app);
		UndoManagerD undo = undoBaseline(app);
		AtomicInteger stores = new AtomicInteger();
		CountDownLatch stored = new CountDownLatch(1);
		undo.addUndoInfoStoredListener(() -> {
			stores.incrementAndGet();
			stored.countDown();
		});

		rightClick(app, -1, 1);

		assertTrue(inherited.isEmpty());
		JPopupMenu menu = lastMenu();
		JMenuItem item = action(menu);
		assertNotNull(item, "the dimension line menu offers the action");
		assertTrue(item.isEnabled());
		assertEquals(1, ((GeoNumeric) literal).getDouble(), 0,
				"a right click in Move mode does not drag the offset");
		assertEquals(before, constructionXml(app), "opening the menu changes nothing");
		Thread.sleep(200);
		assertEquals(0, stores.get(), "opening the menu stores no undo point");
		// the inherited chooser entries are all kept, followed by the action
		JPopupMenu inheritedMenu = new ContextMenuChooseGeoD(app, app.getEuclidianView1(),
				new ArrayList<>(List.of(owner.getDimensionLine())),
				new ArrayList<>(List.of(owner.getDimensionLine())), new Point(0, 0),
				new GPoint(0, 0)).getWrappedPopup();
		assertEquals(inheritedMenu.getComponentCount() + 2, menu.getComponentCount());
		assertSame(item, menu.getComponent(menu.getComponentCount() - 1));

		item.doClick();

		assertTrue(stored.await(5, TimeUnit.SECONDS));
		assertSame(literal, owner.getOffsetInput(), "the same number object");
		assertEquals("offset1", literal.getLabelSimple());
		assertTrue(literal.isIndependent() && !literal.isLocked());
		assertEquals("AlignedDimension[A, B, offset1]", definition(owner));
		assertEquals(geometry, geometry(owner));
		assertTrue(GeoCeDGEuclidianController.isDraggableOffset(owner));
		assertEquals(1, stores.get(), "one undo point");
	}

	@Test
	void caseBALinearDimensionLineBehavesTheSame() throws Exception {
		points();
		AlgoNativeDimension owner = dimension(app, "LinearDimension(A,B,xAxis,1)");
		final String geometry = geometry(owner);
		rightClick(app, 1, 1);
		JMenuItem item = action(lastMenu());
		assertNotNull(item);
		assertTrue(item.isEnabled());
		item.doClick();
		assertEquals("LinearDimension[A, B, xAxis, offset1]", definition(owner));
		assertEquals(geometry, geometry(owner));
	}

	@Test
	void casesCDEIneligibleOffsetsShowTheDisabledActionWithTheirReason() throws Exception {
		points();
		G9U1TestApp.eval(app, "s=1");
		AlgoNativeDimension draggable = dimension(app, "AlignedDimension(A,B,s)");
		final String xml = constructionXml(app);
		rightClick(app, -1, 1);
		JMenuItem item = action(lastMenu());
		assertFalse(item.isEnabled(), "no redundant conversion");
		assertEquals(GeoCeDGProfile.getText("Dimensions.Offset.AlreadyDraggable", "en"),
				item.getToolTipText());
		item.doClick();
		assertEquals(xml, constructionXml(app));
		draggable.remove();

		dimension(app, "AlignedDimension(A,B,2*3)");
		rightClick(app, -1, 6);
		item = action(lastMenu());
		assertFalse(item.isEnabled());
		assertEquals(GeoCeDGProfile.getText("Dimensions.Offset.Expression", "en"),
				item.getToolTipText());
		app.getKernel().getConstruction().getConstructionElement(
				app.getKernel().getConstruction().steps() - 1).remove();

		GeoNumeric locked = (GeoNumeric) G9U1TestApp.eval(app, "t=2");
		locked.setFixed(true);
		dimension(app, "AlignedDimension(A,B,t)");
		rightClick(app, -1, 2);
		item = action(lastMenu());
		assertFalse(item.isEnabled());
		assertEquals(GeoCeDGProfile.getText("Dimensions.Offset.Named", "en"),
				item.getToolTipText());
		assertNull(app.getKernel().lookupLabel("offset1"));
	}

	@Test
	void caseFAnOrdinarySegmentKeepsTheInheritedChooser() throws Exception {
		GeoElement segment = G9U1TestApp.eval(app, "Segment((-2,-2),(2,-2))");
		rightClick(app, 0, -2);
		assertTrue(shown.isEmpty(), "no product menu for an ordinary segment");
		assertEquals(List.of(List.of(segment)), inherited);
		assertNull(gui.chooseGeoOffsetItem(List.of(segment)));
	}

	@Test
	void caseGEveryDrawnOutputResolvesItsOwnDimensionOnly() throws Exception {
		points();
		AlgoNativeDimension first = dimension(app, "AlignedDimension(A,B,1)");
		G9U1TestApp.eval(app, "C=(-2,-4)");
		G9U1TestApp.eval(app, "D=(2,-4)");
		AlgoNativeDimension second = dimension(app, "AlignedDimension(C,D,-1)");
		// the value text of the first dimension (lifted above its line)
		rightClick(app, 0, 1.25);
		assertNotNull(action(lastMenu()));
		assertFalse(first.getOffsetInput().toGeoElement().isLabelSet(),
				"opening the menu converts nothing");
		// the extension line at A (between the point and the dimension line)
		rightClick(app, -2, 0.6);
		action(lastMenu()).doClick();
		assertTrue(first.getOffsetInput().toGeoElement().isLabelSet());
		assertFalse(second.getOffsetInput().toGeoElement().isLabelSet(),
				"the other dimension is not touched");
		// the extension line at C of the second dimension (its line lies below)
		rightClick(app, -2, -4.6);
		action(lastMenu()).doClick();
		assertEquals("AlignedDimension[C, D, offset2]", definition(second));
		assertEquals("AlignedDimension[A, B, offset1]", definition(first));
		// a mixed context is never one dimension
		assertNull(gui.chooseGeoOffsetItem(List.of(first.getDimensionLine(),
				second.getDimensionLine())));
		assertNull(gui.chooseGeoOffsetItem(List.of(first.getDimensionLine(),
				app.getKernel().lookupLabel("A"))));
	}

	@Test
	void caseHOverlappingObjectsKeepTheInheritedChooserAndItsRerouting()
			throws Exception {
		points();
		AlgoNativeDimension owner = dimension(app, "AlignedDimension(A,B,1)");
		GeoElement segment = G9U1TestApp.eval(app, "Segment((-1,1),(1,1))");
		paint(app);
		EuclidianView view = app.getEuclidianView1();
		ArrayList<GeoElement> hits = new ArrayList<>(List.of(segment,
				owner.getDimensionLine()));
		// the inherited chooser opened for the segment lists the dimension line; choosing
		// it re-opens the menu through the normal route, now for the dimension line
		ContextMenuChooseGeoD chooser = new ContextMenuChooseGeoD(app, view,
				new ArrayList<>(List.of(segment)), hits, new Point(0, 0), new GPoint(10, 10));
		JMenu selectAnother = (JMenu) chooser.getWrappedPopup().getComponent(1);
		JMenuItem choice = null;
		for (int i = 0; i < selectAnother.getItemCount(); i++) {
			JMenuItem candidate = selectAnother.getItem(i);
			if (candidate != null && candidate.getText() != null && candidate.getText()
					.contains(owner.getDimensionLine().getLabelSimple())) {
				choice = candidate;
			}
		}
		assertNotNull(choice, "the chooser lists the dimension line");
		choice.doClick();
		JMenuItem item = action(lastMenu());
		assertNotNull(item);
		assertTrue(item.isEnabled());
		// and the menu opened for the dimension line keeps the chooser for the segment
		JPopupMenu menu = lastMenu();
		assertTrue(menu.getComponent(1) instanceof JMenu, "select-another submenu kept");
		JMenu back = (JMenu) menu.getComponent(1);
		JMenuItem toSegment = null;
		for (int i = 0; i < back.getItemCount(); i++) {
			if (back.getItem(i).getText().contains(segment.getLabelSimple())) {
				toSegment = back.getItem(i);
			}
		}
		assertNotNull(toSegment);
		toSegment.doClick();
		assertEquals(List.of(List.of(segment)), inherited,
				"choosing the segment goes back to the inherited chooser");
		assertFalse(owner.getOffsetInput().toGeoElement().isLabelSet());
	}

	@Test
	void caseIJBothRoutesProduceTheSameTransaction() throws Exception {
		AppGeoCeDG algebra = graphicsApp();
		List<JPopupMenu> algebraShown = new ArrayList<>();
		capture(algebra, algebraShown, new ArrayList<>());
		for (AppGeoCeDG target : List.of(app, algebra)) {
			G9U1TestApp.eval(target, "A=(-2,0)");
			G9U1TestApp.eval(target, "B=(2,0)");
			dimension(target, "AlignedDimension(A,B,1)");
			G9U1TestApp.eval(target, "m=2*a");
		}
		AlgoNativeDimension graphics = AlgoNativeDimension.ownerOf(
				app.getKernel().lookupLabel("a"));
		AlgoNativeDimension algebraic = AlgoNativeDimension.ownerOf(
				algebra.getKernel().lookupLabel("a"));
		final String literal = constructionXml(app);
		assertEquals(literal, constructionXml(algebra));
		final GeoElement graphicsNumber = graphics.getOffsetInput().toGeoElement();
		final GeoElement algebraNumber = algebraic.getOffsetInput().toGeoElement();
		UndoManagerD graphicsUndo = undoBaseline(app);
		UndoManagerD algebraUndo = undoBaseline(algebra);
		CountDownLatch stored = new CountDownLatch(2);
		graphicsUndo.addUndoInfoStoredListener(stored::countDown);
		algebraUndo.addUndoInfoStoredListener(stored::countDown);

		rightClick(app, -1, 1);
		action(lastMenu()).doClick();
		// Algebra View route: the object menu of the value
		JPopupMenu algebraMenu = new JPopupMenu();
		((GuiManagerGeoCeDG) algebra.getGuiManager()).decorateProductContextMenu(algebraMenu,
				List.of(algebraic.getValue()));
		action(algebraMenu).doClick();

		assertTrue(stored.await(5, TimeUnit.SECONDS), "one undo point on each route");
		assertSame(graphicsNumber, graphics.getOffsetInput());
		assertSame(algebraNumber, algebraic.getOffsetInput());
		final String converted = constructionXml(app);
		assertEquals(converted, constructionXml(algebra), "same parameterization and DAG");
		app.getKernel().undo();
		algebra.getKernel().undo();
		await(() -> app.getKernel().lookupLabel("offset1") == null
				&& algebra.getKernel().lookupLabel("offset1") == null);
		assertEquals(literal, constructionXml(app));
		assertEquals(literal, constructionXml(algebra));
		app.getKernel().redo();
		algebra.getKernel().redo();
		await(() -> app.getKernel().lookupLabel("offset1") != null
				&& algebra.getKernel().lookupLabel("offset1") != null);
		assertEquals(converted, constructionXml(app));
		assertEquals(converted, constructionXml(algebra));
		File graphicsFile = temporary.resolve("graphics.cedg").toFile();
		File algebraFile = temporary.resolve("algebra.cedg").toFile();
		assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(graphicsFile));
		assertTrue(((GuiManagerGeoCeDG) algebra.getGuiManager()).saveAsTo(algebraFile));
		AppGeoCeDG first = G9U1TestApp.create();
		first.loadFile(graphicsFile, false);
		AppGeoCeDG second = G9U1TestApp.create();
		second.loadFile(algebraFile, false);
		assertEquals(constructionXml(first), constructionXml(second), "same saved document");
		assertEquals(converted, constructionXml(first));
	}

	@Test
	void aMenuKeptAcrossNewOrOpenNeverConvertsAStaleDimension() throws Exception {
		points();
		dimension(app, "AlignedDimension(A,B,1)");
		rightClick(app, -1, 1);
		JMenuItem staleAfterNew = action(lastMenu());
		rightClick(app, 1, 1);
		JMenuItem staleAfterOpen = action(lastMenu());
		File other = temporary.resolve("other.cedg").toFile();
		AppGeoCeDG source = G9U1TestApp.create();
		G9U1TestApp.eval(source, "C=(5,5)");
		assertTrue(((GuiManagerGeoCeDG) source.getGuiManager()).saveAsTo(other));

		app.setSaved();
		app.fileNew();
		G9U1TestApp.eval(app, "A=(-2,0)");
		G9U1TestApp.eval(app, "B=(2,0)");
		AlgoNativeDimension fresh = dimension(app, "AlignedDimension(A,B,1)");
		final String afterNew = constructionXml(app);
		try (MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
			staleAfterNew.doClick();
			dialogs.verify(() -> JOptionPane.showMessageDialog(any(), any(), anyString(),
					anyInt()), times(1));
		}
		assertEquals(afterNew, constructionXml(app), "the stale action changes nothing");
		assertFalse(fresh.getOffsetInput().toGeoElement().isLabelSet(),
				"a new dimension is never converted by an old menu");

		app.setSaved();
		app.loadFile(other, false);
		final String afterOpen = constructionXml(app);
		try (MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
			staleAfterOpen.doClick();
			dialogs.verify(() -> JOptionPane.showMessageDialog(any(), any(), anyString(),
					anyInt()), times(1));
		}
		assertEquals(afterOpen, constructionXml(app));
		assertNull(app.getKernel().lookupLabel("offset1"));
	}

	@Test
	void theGraphicsViewActionIsLocalized() throws Exception {
		points();
		dimension(app, "AlignedDimension(A,B,1)");
		app.setLanguage(Locale.forLanguageTag("es"));
		rightClick(app, -1, 1);
		JMenuItem item = action(lastMenu());
		assertEquals(ACTION_ES, item.getText());
		assertEquals(GeoCeDGProfile.getText("Dimensions.Offset.MakeDraggableTip", "es"),
				item.getToolTipText());
	}
}
