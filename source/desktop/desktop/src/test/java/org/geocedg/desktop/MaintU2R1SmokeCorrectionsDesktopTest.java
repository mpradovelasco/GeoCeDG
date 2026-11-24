/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JToolTip;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.desktop.resources.GeoCeDGToolImageResource;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.geogebra.desktop.util.ImageManagerD;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PRE-G9B-MAINT-U2-R1: corrections after the author's smoke of U2. The metric endpoint
 * assistance is driven through its real option panes, buttons, list and check box (only the
 * modal window is replaced): no position is chosen for the user, the created point keeps the
 * position the user selected, and nothing is created without a selection. Also: multi-line
 * tooltips of the Point panel and of tool 134, and distinct length and closed spline icons.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class MaintU2R1SmokeCorrectionsDesktopTest {
	private static final Locale SPANISH = Locale.forLanguageTag("es");
	private static final String A = "A=(0.8976484384297626,1.7854818601338847)";

	@BeforeAll static void initialize() { G9U1SemanticPointInteractionTest.initializeDesktop(); }

	/** Scripted answers to the option panes of the assistance, given on the real panes. */
	static final class Panes implements GeoCeDGMetricEndpointAssistance.Modal {
		private final List<Function<JOptionPane, Object>> steps;
		final List<JOptionPane> shown = new ArrayList<>();
		final List<String> titles = new ArrayList<>();

		@SafeVarargs Panes(Function<JOptionPane, Object>... steps) { this.steps = List.of(steps); }

		@Override public Object show(JOptionPane pane, Component parent, String title) {
			shown.add(pane);
			titles.add(title);
			layout(pane);
			int step = shown.size() - 1;
			assertTrue(step < steps.size(), "unexpected pane: " + pane.getMessage());
			return steps.get(step).apply(pane);
		}
	}

	/** Runs the action with the given panes and restores the product modal window. */
	static <T> T with(Panes panes, java.util.concurrent.Callable<T> action) throws Exception {
		var previous = GeoCeDGMetricEndpointAssistance.useModal(panes);
		try {
			return action.call();
		} finally {
			GeoCeDGMetricEndpointAssistance.useModal(previous);
		}
	}

	static void layout(Container root) {
		root.setSize(root.getPreferredSize());
		layoutTree(root);
	}

	private static void layoutTree(Container root) {
		root.doLayout();
		for (Component child : root.getComponents()) {
			if (child instanceof Container container) { layoutTree(container); }
		}
	}

	static <T extends Component> T find(Container root, Class<T> type, String name) {
		return Cu7M08SDesktopEventsTest.find(root, type, c -> name.equals(c.getName()));
	}

	static List<String> texts(JOptionPane pane) {
		List<String> texts = new ArrayList<>();
		collect(pane, texts);
		return texts;
	}

	private static void collect(Container root, List<String> texts) {
		for (Component child : root.getComponents()) {
			if (child instanceof JLabel label && label.getText() != null) { texts.add(label.getText()); }
			if (child instanceof AbstractButton button && button.getText() != null) { texts.add(button.getText()); }
			if (child instanceof Container container) { collect(container, texts); }
		}
	}

	/** Presses the option button with this text, as a user would, and returns the pane value. */
	static Object press(JOptionPane pane, String text) {
		var button = Cu7M08SDesktopEventsTest.find(pane, AbstractButton.class, b -> text.equals(b.getText()));
		assertNotNull(button, text + " in " + texts(pane));
		assertTrue(button.isEnabled(), text);
		button.doClick();
		return pane.getValue();
	}

	/** Mouse click on one row of the list, through the list's own listeners. */
	static void clickRow(JList<?> list, int row) {
		Rectangle cell = list.getCellBounds(row, row);
		assertNotNull(cell, "row " + row);
		for (int id : new int[] {MouseEvent.MOUSE_PRESSED, MouseEvent.MOUSE_RELEASED, MouseEvent.MOUSE_CLICKED}) {
			list.dispatchEvent(new MouseEvent(list, id, System.currentTimeMillis(),
					id == MouseEvent.MOUSE_PRESSED ? InputEvent.BUTTON1_DOWN_MASK : 0,
					cell.x + 4, cell.y + cell.height / 2, 1, false, MouseEvent.BUTTON1));
		}
	}

	/** The technical address of a row, as its tooltip shows it. */
	static String detail(JList<?> list, int row) {
		Rectangle cell = list.getCellBounds(row, row);
		return list.getToolTipText(new MouseEvent(list, MouseEvent.MOUSE_MOVED, 0, 0,
				cell.x + 4, cell.y + cell.height / 2, 0, false));
	}

	/**
	 * The user's choice in the positions pane: nothing is preselected and Create point is off
	 * until a row is clicked; then the drag choice, and Create point.
	 */
	static Function<JOptionPane, Object> choosePosition(int row, boolean allowDragging, String create,
			AtomicReference<String> chosen) {
		return pane -> {
			JList<?> list = find(pane, JList.class, "m08-endpoint-positions");
			JButton button = find(pane, JButton.class, "m08-endpoint-create");
			JCheckBox free = find(pane, JCheckBox.class, "m08-allow-dragging");
			assertNotNull(list); assertNotNull(button); assertNotNull(free);
			assertEquals(-1, list.getSelectedIndex(), "no position is chosen for the user");
			assertFalse(button.isEnabled(), "Create point waits for a selection");
			button.doClick();
			assertNull(pane.getValue() == JOptionPane.UNINITIALIZED_VALUE ? null : pane.getValue(),
					"a disabled Create point does nothing");
			assertTrue(free.isSelected(), "Allow dragging starts on, as in the panel");
			clickRow(list, row);
			assertEquals(row, list.getSelectedIndex(), "the clicked row is selected");
			assertTrue(button.isEnabled());
			if (!allowDragging) { free.doClick(); }
			assertEquals(allowDragging, free.isSelected());
			chosen.set(detail(list, row));
			return press(pane, create);
		};
	}

	private static List<GeoPoint> points(AppGeoCeDG app) {
		return app.getKernel().getConstruction().getGeoSetConstructionOrder().stream()
				.filter(GeoPoint.class::isInstance).map(GeoPoint.class::cast).toList();
	}

	private static String address(GeoPoint point) {
		return GeoCeDGAddressPresentation.technical(
				((AlgoSemanticLocusPoint2D) point.getParentAlgorithm()).getSemanticAddress());
	}

	@Test void endpointAssistanceRequiresAnExplicitSelectionThroughTheRealPanes() throws Exception {
		AtomicReference<AppGeoCeDG> host = new AtomicReference<>();
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			host.set(app);
			app.setLanguage(Locale.ENGLISH);
			Cu7M08SDesktopEventsTest.general(app);
			Cu7M08SDesktopEventsTest.q(app, A);
			Cu7M08SDesktopEventsTest.q(app, "B=(3,-2)");
			var baselineUndo = (UndoManagerD) app.getKernel().getConstruction().getUndoManager();
			try (UndoManagerD.PreparedUndoBaseline baseline = baselineUndo.prepareUndoBaseline()) {
				baselineUndo.commitUndoBaseline(baseline);
			}
		});
		var app = host.get();
		var undo = (UndoManagerD) app.getKernel().getConstruction().getUndoManager();
		AtomicInteger history = new AtomicInteger();
		AtomicInteger stores = new AtomicInteger();
		CountDownLatch stored = new CountDownLatch(1);
		AtomicReference<GeoPoint> created = new AtomicReference<>();
		AtomicReference<String> chosen = new AtomicReference<>();
		Cu7M08SDesktopEventsTest.edt(() -> {
			var source = (GeoLocusV2) app.getKernel().lookupLabel("h");
			var a = (GeoPoint) app.getKernel().lookupLabel("A");
			assertTrue(new GeoCeDGMetricEndpointAssistance(app).isApplicable(source, a));
			history.set(undo.getHistorySize());
			List<GeoPoint> before = points(app);
			var panes = new Panes(pane -> {
				String message = String.valueOf(pane.getMessage());
				assertTrue(message.startsWith("A has no semantic address on h"), message);
				assertTrue(message.contains("A does not change"), message);
				assertEquals(List.of("Choose a position on the locus", "Cancel"),
						Arrays.stream(pane.getOptions()).map(String::valueOf).toList());
				return press(pane, "Choose a position on the locus");
			}, pane -> {
				var texts = texts(pane);
				assertTrue(texts.contains("Positions of h next to A (search radius 0.1):"), texts.toString());
				assertTrue(texts.contains("Select a position, then Create point."), texts.toString());
				assertTrue(texts.stream().anyMatch(text -> text.startsWith("The new point does not replace A")));
				JList<?> list = find(pane, JList.class, "m08-endpoint-positions");
				for (int i = 0; i < list.getModel().getSize(); i++) {
					String label = String.valueOf(list.getModel().getElementAt(i));
					assertTrue(label.contains("u = ") && !label.contains("-0.0000"), label);
				}
				JLabel guarantee = find(pane, JLabel.class, "m08-endpoint-guarantee");
				assertFalse(guarantee.getText().isBlank());
				assertNotNull(guarantee.getToolTipText());
				return choosePosition(0, false, "Create point", chosen).apply(pane);
			});
			with(panes, () -> {
				app.setMode(EuclidianConstants.MODE_LOCUS_V2_LENGTH_BETWEEN);
				Cu7M08SDesktopEventsTest.pointer(app, .8, 2.4);
				Cu7M08SDesktopEventsTest.pointer(app, a.inhomX, a.inhomY);
				assertEquals(List.of(source, a), app.getSelectionManager().getSelectedGeos(), "L then A");
				undo.addUndoInfoStoredListener(() -> { stores.incrementAndGet(); stored.countDown(); });
				Cu7M08SDesktopEventsTest.pointer(app, 3, -2);
				return null;
			});
			assertEquals(2, panes.shown.size(), "route pane, then positions pane");
			assertEquals(List.of("Construct a metric endpoint", "Construct a metric endpoint"), panes.titles);
			var added = points(app).stream().filter(point -> !before.contains(point)).toList();
			assertEquals(1, added.size(), "exactly one point, for the selected position");
			created.set(added.get(0));
			assertNotSame(a, created.get(), "A itself is never the created point");
			assertEquals(chosen.get(), address(created.get()), "the created address is the selected one");
			assertSame(source, ((AlgoSemanticLocusPoint2D) created.get().getParentAlgorithm()).getSource());
			assertTrue(created.get().isLocked(), "Allow dragging off locks the point");
			assertFalse(created.get().getAllPredecessors().contains(a), "A is not a parent");
			assertEquals(.8976484384297626, a.inhomX, 0, "A does not change");
		});
		assertTrue(stored.await(5, TimeUnit.SECONDS), "the host stores the creation");
		Thread.sleep(500);
		assertEquals(1, stores.get(), "exactly one Undo step");
		assertEquals(history.get() + 1, undo.getHistorySize());
		String label = created.get().getLabelSimple();
		String address = address(created.get());
		// Dynamic update: the point follows its curve and keeps its address.
		Cu7M08SDesktopEventsTest.edt(() -> {
			var i = (GeoPoint) app.getKernel().lookupLabel("I");
			double x = created.get().inhomX;
			double y = created.get().inhomY;
			i.setCoords(i.inhomX + .2, i.inhomY, 1);
			i.updateCascade();
			assertTrue(created.get().isDefined());
			assertTrue(Math.hypot(created.get().inhomX - x, created.get().inhomY - y) > 1e-3,
					"the point moves with its curve");
			assertEquals(address, address(created.get()), "with the same address");
			i.setCoords(i.inhomX - .2, i.inhomY, 1);
			i.updateCascade();
			assertEquals(x, created.get().inhomX, 1e-7);
		});
		Cu7M08SDesktopEventsTest.edt(() -> app.getKernel().undo());
		Cu7M08SDesktopEventsTest.await(() -> app.getKernel().lookupLabel(label) == null);
		Cu7M08SDesktopEventsTest.edt(() -> app.getKernel().redo());
		Cu7M08SDesktopEventsTest.await(() -> app.getKernel().lookupLabel(label) instanceof GeoPoint);
		Cu7M08SDesktopEventsTest.edt(() -> {
			var redone = (GeoPoint) app.getKernel().lookupLabel(label);
			assertEquals(address, address(redone), "Redo restores the same address");
			assertTrue(redone.isLocked());
			assertNotNull(app.getKernel().lookupLabel("A"));
			System.out.println("U2R1_ENDPOINT address=" + address + " one_undo=true redo=true");
			app.setMode(EuclidianConstants.MODE_MOVE);
		});
	}

	@Test void cancellingOrClosingTheAssistanceCreatesNothingInSpanish() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			app.setLanguage(SPANISH);
			var source = Cu7M08SDesktopEventsTest.general(app);
			var a = Cu7M08SDesktopEventsTest.q(app, A);
			String before = app.getXML();
			var assistance = new GeoCeDGMetricEndpointAssistance(app);
			// Cancel in the route pane.
			var panes = new Panes(pane -> press(pane, "Cancelar"));
			assertNull(with(panes, () -> assistance.offer(source, a)));
			assertTrue(String.valueOf(panes.shown.get(0).getMessage())
					.startsWith("A no tiene dirección semántica sobre h"));
			// A row selected, then Cancelar in the positions pane.
			panes = new Panes(pane -> press(pane, "Elegir una posición en el lugar"), pane -> {
				var texts = texts(pane);
				assertTrue(texts.contains("Posiciones de h junto a A (radio de búsqueda 0,1):"), texts.toString());
				assertTrue(texts.contains("Permitir arrastre"));
				assertTrue(texts.contains("Crear punto"));
				JList<?> list = find(pane, JList.class, "m08-endpoint-positions");
				assertFalse(String.valueOf(list.getModel().getElementAt(0)).contains("-0,0000"));
				clickRow(list, 0);
				return press(pane, "Cancelar");
			});
			assertNull(with(panes, () -> assistance.offer(source, a)));
			// The positions pane closed (Escape or the window button) without a value.
			panes = new Panes(pane -> press(pane, "Elegir una posición en el lugar"), pane -> {
				clickRow(find(pane, JList.class, "m08-endpoint-positions"), 0);
				return null;
			});
			assertNull(with(panes, () -> assistance.offer(source, a)));
			// The route pane closed without a value.
			assertNull(with(new Panes(pane -> null), () -> assistance.offer(source, a)));
			assertEquals(before, app.getXML(), "no mutation");
			app.setLanguage(Locale.ENGLISH);
		});
	}

	@Test void withoutAnAdmissiblePositionNothingIsCreatedAndTheReasonIsShown() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			app.setLanguage(Locale.ENGLISH);
			var source = Cu7M08SDesktopEventsTest.general(app);
			var far = Cu7M08SDesktopEventsTest.q(app, "F=(6,6)");
			var assistance = new GeoCeDGMetricEndpointAssistance(app);
			assertTrue(assistance.isApplicable(source, far));
			String before = app.getXML();
			AtomicReference<String> told = new AtomicReference<>();
			var panes = new Panes(pane -> press(pane, "Choose a position on the locus"), pane -> {
				told.set(String.valueOf(pane.getMessage()));
				assertNull(pane.getOptions(), "an information pane, no choice");
				return JOptionPane.OK_OPTION;
			});
			assertNull(with(panes, () -> assistance.offer(source, far)));
			assertTrue(told.get().startsWith("No position of h was found next to F (search radius 0.1)."
					+ " No point was created."), told.get());
			assertEquals(before, app.getXML(), "no point by approximation");
		});
	}

	@Test void severalPositionsHaveNoImplicitChoiceAndTheSelectedOneIsCreated() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			app.setLanguage(Locale.ENGLISH);
			// A doubly traced spline: the reference sees two positions with distinct addresses.
			var source = (GeoLocusV2) G9U1SemanticPointInteractionTest.eval(app,
					"S=SplineV2({(-1,0),(0,1),(1,0),(0,1),(-1,0)},3)");
			var reference = Cu7M08SDesktopEventsTest.q(app, "R=(0,1)");
			List<GeoPoint> before = points(app);
			AtomicReference<String> chosen = new AtomicReference<>();
			AtomicReference<String> other = new AtomicReference<>();
			var panes = new Panes(pane -> press(pane, "Choose a position on the locus"), pane -> {
				JList<?> list = find(pane, JList.class, "m08-endpoint-positions");
				assertEquals(2, list.getModel().getSize());
				other.set(detail(list, 0));
				return choosePosition(1, true, "Create point", chosen).apply(pane);
			});
			var point = with(panes, () -> new GeoCeDGMetricEndpointAssistance(app).offer(source, reference));
			assertNotNull(point);
			assertEquals(1, points(app).stream().filter(p -> !before.contains(p)).count());
			assertNotEquals(other.get(), chosen.get());
			assertEquals(chosen.get(), address(point), "the second row, not the first");
			assertFalse(point.isLocked(), "Allow dragging on");
		});
	}

	private static List<String> tooltipLines(String tooltip) {
		assertNotNull(tooltip);
		assertTrue(tooltip.startsWith("<html>") && tooltip.endsWith("</html>"), tooltip);
		String body = tooltip.substring("<html>".length(), tooltip.length() - "</html>".length());
		return Arrays.stream(body.split("<br/?>")).map(String::trim).toList();
	}

	private static int width(String tooltip) {
		JToolTip tip = new JToolTip();
		tip.setTipText(tooltip);
		return tip.getPreferredSize().width;
	}

	@Test void pointPanelTooltipsAreCompactAndMultiLineInBothLanguages() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			for (Locale locale : List.of(Locale.ENGLISH, SPANISH)) {
				app.setLanguage(locale);
				boolean spanish = locale == SPANISH;
				for (int mode : new int[] {EuclidianConstants.MODE_POINT,
						EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT}) {
					app.setMode(mode);
					var panel = Cu7M08SDesktopEventsTest.panel(app);
					var reference = find(panel, javax.swing.JToggleButton.class, "m08-from-reference");
					for (int origin = 0; origin < 2; origin++) {
						var rule = find(panel, JLabel.class, "m08-origin-rule");
						var lines = tooltipLines(rule.getToolTipText());
						assertTrue(lines.size() >= 3, lines.toString());
						assertTrue(lines.stream().allMatch(line -> line.length() <= GeoCeDGTooltips.COLUMNS),
								lines.toString());
						assertTrue(lines.stream().anyMatch(line -> line.startsWith(spanish ? "Radio:" : "Radius:")),
								lines.toString());
						assertTrue(lines.stream().anyMatch(line -> line.startsWith(spanish ? "Confirmar:" : "Confirm:")),
								lines.toString());
						assertFalse(lines.get(0).startsWith(spanish ? "Radio" : "Radius"), "purpose first");
						String plain = GeoCeDGTooltips.plain(rule.getToolTipText());
						assertTrue(width(rule.getToolTipText()) * 2 < width(plain) + 1,
								"multi-line tooltip is at most half as wide as one line");
						assertTrue(width(rule.getToolTipText()) < 520, "compact: " + width(rule.getToolTipText()));
						System.out.println("U2R1_TOOLTIP mode=" + mode + " locale=" + locale + " lines="
								+ lines.size() + " width=" + width(rule.getToolTipText()) + " one_line_width="
								+ width(plain));
						reference.doClick();
					}
					var free = find(panel, JCheckBox.class, "m08-allow-dragging");
					assertEquals(2, tooltipLines(free.getToolTipText()).size());
				}
				app.setMode(EuclidianConstants.MODE_MOVE);
			}
			app.setLanguage(Locale.ENGLISH);
		});
	}

	@Test void refusalTooltipKeepsTheTypedCodeOnItsOwnLine() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			G9U1SemanticPointInteractionTest.spline(app);
			var q = Cu7M08SDesktopEventsTest.q(app, "Q=(0.3,0.02)");
			app.setMode(EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT);
			Cu7M08SDesktopEventsTest.pointer(app, -1, 0);
			Cu7M08SDesktopEventsTest.pointer(app, q.inhomX, q.inhomY);
			q.setCoords(.4, .02, 1); q.updateCascade();
			Cu7M08SDesktopEventsTest.confirm(app);
			var status = Cu7M08SDesktopEventsTest.find(Cu7M08SDesktopEventsTest.panel(app), JTextField.class,
					field -> !field.isEditable());
			var lines = tooltipLines(((JComponent) status).getToolTipText());
			assertEquals("[STALE_SELECTION]", lines.get(lines.size() - 1));
			assertTrue(lines.get(0).startsWith("No point created: "));
			app.setMode(EuclidianConstants.MODE_MOVE);
		});
	}

	@Test void explicitAddressToolHelpIsCompactAndMultiLine() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			for (Locale locale : List.of(Locale.ENGLISH, SPANISH)) {
				app.setLanguage(locale);
				var registry = new GeoCeDGActionRegistry(app);
				Action action = registry.get("semantic.locus-v2.point-explicit");
				String tooltip = (String) action.getValue(Action.SHORT_DESCRIPTION);
				var lines = tooltipLines(tooltip);
				assertEquals(3, lines.size(), lines.toString());
				assertTrue(tooltip.contains(locale == SPANISH ? "clave de rama" : "branch key"), tooltip);
				assertTrue(lines.stream().allMatch(line -> line.length() <= GeoCeDGTooltips.COLUMNS));
				assertEquals(locale == SPANISH ? "Punto sobre curva semántica (dirección explícita)"
						: "Point on semantic curve (explicit address)", action.getValue(Action.NAME),
						"name unchanged");
				String plain = (String) action.getValue(Action.LONG_DESCRIPTION);
				assertFalse(plain.contains("\n") || plain.contains("<"), plain);
				var item = GeoCeDGMenuBar.createItem(action, new ButtonGroup());
				assertEquals(tooltip, item.getToolTipText(), "the menu item shows the multi-line help");
				assertEquals(plain, item.getAccessibleContext().getAccessibleDescription());
				String toolbar = app.getToolTooltipHTML(EuclidianConstants.MODE_LOCUS_V2_POINT);
				assertTrue(toolbar.split("<br").length >= 4, toolbar);
				assertTrue(width(tooltip) < 520, "compact: " + width(tooltip));
				System.out.println("U2R1_HELP134 locale=" + locale + " width=" + width(tooltip)
						+ " one_line_width=" + width(plain));
				// Other tool helps keep their single-line text.
				assertEquals(app.getToolHelp(EuclidianConstants.MODE_SPLINE_V2),
						registry.get("semantic.spline-v2.create").getValue(Action.SHORT_DESCRIPTION));
			}
			app.setLanguage(Locale.ENGLISH);
		});
	}

	@Test void lengthAndClosedSplineToolsHaveTheirOwnIcons() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			assertSame(GeoCeDGToolImageResource.LOCUS_V2, GeoCeDGToolImageResource.forIconKey("geocedg.action.LocusV2Create"));
			assertSame(GeoCeDGToolImageResource.LOCUS_LENGTH_TOTAL,
					GeoCeDGToolImageResource.forIconKey("geocedg.action.LocusLengthTotal"));
			assertSame(GeoCeDGToolImageResource.LOCUS_LENGTH_PARTIAL,
					GeoCeDGToolImageResource.forIconKey("geocedg.action.LocusLengthPartial"));
			assertSame(GeoCeDGToolImageResource.SPLINE_V2, GeoCeDGToolImageResource.forIconKey("geocedg.action.SplineV2Create"));
			assertSame(GeoCeDGToolImageResource.SPLINE_V2, GeoCeDGToolImageResource.forIconKey("geocedg.action.SplineV2CreateDegree"));
			assertSame(GeoCeDGToolImageResource.SPLINE_V2_CLOSED,
					GeoCeDGToolImageResource.forIconKey("geocedg.action.SplineV2CreateClosed"));
			assertSame(GeoCeDGToolImageResource.LOCUS_LENGTH_TOTAL, GeoCeDGToolImageResource.forMode("locuslength.total"));
			assertSame(GeoCeDGToolImageResource.LOCUS_LENGTH_PARTIAL, GeoCeDGToolImageResource.forMode("locuslength.partial"));
			assertSame(GeoCeDGToolImageResource.SPLINE_V2_CLOSED, GeoCeDGToolImageResource.forMode("splinev2.closed"));
			assertTrue(GeoCeDGToolImageResource.LOCUS_V2.getFilename().endsWith("/mode_locusv2.svg"), "unchanged");
			assertTrue(GeoCeDGToolImageResource.SPLINE_V2.getFilename().endsWith("/mode_geocedg_splinev2.svg"), "unchanged");
			var family = List.of(GeoCeDGToolImageResource.LOCUS_V2, GeoCeDGToolImageResource.LOCUS_LENGTH_TOTAL,
					GeoCeDGToolImageResource.LOCUS_LENGTH_PARTIAL, GeoCeDGToolImageResource.SPLINE_V2,
					GeoCeDGToolImageResource.SPLINE_V2_CLOSED);
			assertEquals(family.size(), new HashSet<>(family.stream().map(GeoCeDGToolImageResource::getFilename).toList()).size());
			for (int size : new int[] {24, 32, 48}) {
				List<BufferedImage> rasters = new ArrayList<>();
				for (var resource : family) {
					rasters.add(ImageManagerD.toBufferedImage(ImageManagerD.getScaledImage(resource.renderImage(), size, size)));
				}
				for (int i = 0; i < rasters.size(); i++) {
					for (int j = i + 1; j < rasters.size(); j++) {
						assertTrue(difference(rasters.get(i), rasters.get(j)) > size,
								family.get(i) + " and " + family.get(j) + " differ at " + size + " px");
					}
				}
			}
			var registry = new GeoCeDGActionRegistry(app);
			for (String id : List.of("measure.locus-v2-total-length", "measure.locus-v2-partial-length",
					"semantic.spline-v2.create-closed", "semantic.locus-v2.create", "semantic.spline-v2.create")) {
				assertNotNull(registry.get(id).getValue(Action.SMALL_ICON), id);
				assertNotNull(registry.get(id).getValue(Action.LARGE_ICON_KEY), id);
			}
		});
	}

	/** Pixels whose opaque colour differs clearly between two rasters of the same size. */
	private static int difference(BufferedImage first, BufferedImage second) {
		int count = 0;
		for (int y = 0; y < first.getHeight(); y++) {
			for (int x = 0; x < first.getWidth(); x++) {
				int p = first.getRGB(x, y);
				int q = second.getRGB(x, y);
				int alpha = Math.abs((p >>> 24) - (q >>> 24));
				int red = Math.abs(((p >> 16) & 255) - ((q >> 16) & 255));
				int green = Math.abs(((p >> 8) & 255) - ((q >> 8) & 255));
				int blue = Math.abs((p & 255) - (q & 255));
				if (alpha > 96 || red + green + blue > 192) { count++; }
			}
		}
		return count;
	}
}
