/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PRE-G9B-MAINT-U2: one Point panel with an origin selector (click or reference Q) for Point,
 * Point on object and the alias tool 145; the radius and confirmation rule of each origin; the
 * metric endpoint assistance; and the explicit-address alternative of a component rejection.
 * The selector only chooses an existing route: the addresses, the kernel and the modes are
 * unchanged.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class MaintU2PointToolUnificationDesktopTest {
	private static final Locale SPANISH = Locale.forLanguageTag("es");
	private static final int[] POINT_MODES = {EuclidianConstants.MODE_POINT,
			EuclidianConstants.MODE_POINT_ON_OBJECT, EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT};

	@BeforeAll static void initialize() { G9U1SemanticPointInteractionTest.initializeDesktop(); }

	private static GeoCeDGReferencePointTool tool(AppGeoCeDG app) {
		return Cu7M08SDesktopEventsTest.tool(app);
	}

	private static JPanel panel(AppGeoCeDG app) { return Cu7M08SDesktopEventsTest.panel(app); }

	private static JToggleButton click(AppGeoCeDG app) {
		return Cu7M08SDesktopEventsTest.find(panel(app), JToggleButton.class,
				button -> "m08-click-position".equals(button.getName()));
	}

	private static JToggleButton reference(AppGeoCeDG app) {
		return Cu7M08SDesktopEventsTest.find(panel(app), JToggleButton.class,
				button -> "m08-from-reference".equals(button.getName()));
	}

	private static JLabel rule(AppGeoCeDG app) {
		return Cu7M08SDesktopEventsTest.find(panel(app), JLabel.class,
				label -> "m08-origin-rule".equals(label.getName()));
	}

	private static String instruction(AppGeoCeDG app) {
		return Cu7M08SDesktopEventsTest.find(panel(app), JTextField.class, field -> !field.isEditable())
				.getText();
	}

	/** Names, types and order of the named controls: the shape of the panel, not its texts. */
	private static List<String> shape(Container root) {
		List<String> shape = new ArrayList<>();
		for (Component child : root.getComponents()) {
			if (child.getName() != null) {
				shape.add(child.getClass().getSimpleName() + ":" + child.getName());
			}
			if (child instanceof Container container) { shape.addAll(shape(container)); }
		}
		return shape;
	}

	private static AlgoSemanticLocusPoint2D parent(GeoPoint point) {
		return (AlgoSemanticLocusPoint2D) point.getParentAlgorithm();
	}

	private static List<GeoPoint> points(AppGeoCeDG app) {
		return app.getKernel().getConstruction().getGeoSetConstructionOrder().stream()
				.filter(GeoPoint.class::isInstance).map(GeoPoint.class::cast).toList();
	}

	@Test void pointPointOnObjectAndAliasShareOnePanelWithOneOriginSelector() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			for (Locale locale : List.of(Locale.ENGLISH, SPANISH)) {
				app.setLanguage(locale);
				List<String> first = null;
				for (int mode : POINT_MODES) {
					app.setMode(mode);
					assertEquals(mode, app.getMode(), "the selector never remaps a mode");
					var p = panel(app);
					assertNotNull(p, "mode " + mode);
					assertNotNull(click(app)); assertNotNull(reference(app)); assertNotNull(rule(app));
					assertEquals(1, Cu7M08SDesktopEventsTest.countRadius(p));
					assertNotNull(Cu7M08SDesktopEventsTest.find(p, JCheckBox.class,
							box -> "m08-allow-dragging".equals(box.getName())));
					var shape = shape(p);
					if (first == null) { first = shape; }
					assertEquals(first, shape, "same panel in mode " + mode);
					boolean alias = mode == EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT;
					assertEquals(alias, reference(app).isSelected(), "Q is preselected only by 145");
					assertEquals(!alias, click(app).isSelected());
					assertEquals(alias, tool(app).isReferenceOriginSelected());
					boolean spanish = locale == SPANISH;
					assertEquals(spanish ? "Clic" : "Click", click(app).getText());
					assertEquals(spanish ? "Referencia Q" : "Reference Q", reference(app).getText());
					assertTrue(p.getPreferredSize().width < 900, mode + " " + locale + " "
							+ p.getPreferredSize());
					assertTrue(p.getPreferredSize().height < 90, mode + " " + locale + " "
							+ p.getPreferredSize());
					System.out.println("U2_PANEL mode=" + mode + " locale=" + locale + " size="
							+ p.getPreferredSize().width + "x" + p.getPreferredSize().height
							+ " rule=" + rule(app).getText());
				}
			}
			app.setLanguage(Locale.ENGLISH);
			app.setMode(EuclidianConstants.MODE_MOVE);
			assertNull(panel(app));
		});
	}

	@Test void eachOriginStatesItsRadiusAndConfirmationRuleInBothLanguages() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			for (Locale locale : List.of(Locale.ENGLISH, SPANISH)) {
				app.setLanguage(locale);
				boolean spanish = locale == SPANISH;
				app.setMode(EuclidianConstants.MODE_POINT);
				assertTrue(rule(app).getText().startsWith(spanish ? "Radio de la vista" : "Radius from the view"));
				assertTrue(rule(app).getToolTipText().contains(spanish ? "Confirmar" : "Confirm"));
				assertEquals(spanish ? "Pulse para crear un punto" : "Click to create a point", instruction(app));
				reference(app).doClick();
				assertTrue(rule(app).getText().startsWith(spanish ? "Radio r world alrededor de Q"
						: "Radius r world around Q"));
				assertEquals(spanish ? "Seleccione objeto" : "Select object", instruction(app));
				reference(app).doClick();
				assertFalse(tool(app).isReferenceOriginSelected(), "the segments are complementary");
				app.setMode(EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT);
				assertTrue(rule(app).getText().startsWith(spanish ? "Radio r world alrededor de Q"
						: "Radius r world around Q"));
				click(app).doClick();
				assertTrue(rule(app).getText().startsWith(spanish ? "Radio r world alrededor del clic"
						: "Radius r world around the click"));
				assertEquals(spanish ? "Pulse el objeto para elegir una posición"
						: "Click object to choose a position", instruction(app));
				click(app).doClick();
				assertTrue(tool(app).isReferenceOriginSelected());
				app.setMode(EuclidianConstants.MODE_MOVE);
			}
			app.setLanguage(Locale.ENGLISH);
		});
	}

	@Test void eachToolKeepsItsOwnOriginPreferenceInTheDocument() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			app.setMode(EuclidianConstants.MODE_POINT);
			reference(app).doClick();
			assertTrue(reference(app).isSelected());
			app.setMode(EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT);
			click(app).doClick();
			assertTrue(click(app).isSelected());
			app.setMode(EuclidianConstants.MODE_POINT_ON_OBJECT);
			assertTrue(reference(app).isSelected(), "Point and Point on object share a preference");
			app.setMode(EuclidianConstants.MODE_MOVE);
			app.setMode(EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT);
			assertTrue(click(app).isSelected(), "145 keeps its own choice");
			app.setMode(EuclidianConstants.MODE_POINT);
			assertTrue(reference(app).isSelected());
			reference(app).doClick();
			assertTrue(click(app).isSelected());
			app.setMode(EuclidianConstants.MODE_MOVE);
		});
	}

	@Test void aliasAndPointGiveIdenticalAddressesForTheSameReferenceAndClick() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			List<String> addresses = new ArrayList<>();
			for (int mode : new int[] {EuclidianConstants.MODE_POINT,
					EuclidianConstants.MODE_POINT_ON_OBJECT,
					EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT}) {
				var app = G9U1SemanticPointInteractionTest.app();
				var source = Cu7M08SDesktopEventsTest.general(app);
				var q = Cu7M08SDesktopEventsTest.q(app,
						"ReferenceR1=(0.8976484384297626,1.7854818601338847)");
				app.setMode(mode);
				if (!tool(app).isReferenceOriginSelected()) { reference(app).doClick(); }
				Cu7M08SDesktopEventsTest.pointer(app, .8, 2.4);
				Cu7M08SDesktopEventsTest.assertL(app, source);
				Cu7M08SDesktopEventsTest.pointer(app, q.inhomX, q.inhomY);
				var selection = tool(app).pendingSelection();
				assertNotNull(selection, "mode " + mode);
				assertEquals(GeoCeDGReferencePlacement.Route.R2_H1, selection.getRoute());
				Cu7M08SDesktopEventsTest.confirm(app);
				var point = Cu7M08SDesktopEventsTest.point(app);
				assertSame(source, parent(point).getSource());
				assertFalse(point.getAllPredecessors().contains(q));
				// Each document has its own locus identity; branch, component and parameter must agree.
				addresses.add(GeoCeDGAddressPresentation.technical(parent(point).getCurrentSemanticAddress()));
				assertEquals(mode, app.getMode(), "the panel restarts in the same tool");
				assertTrue(reference(app).isSelected(), "the origin choice survives the restart");
				tool(app).reset();
			}
			assertEquals(3, addresses.size());
			assertEquals(1, addresses.stream().distinct().count(), addresses.toString());
		});
	}

	@Test void aliasModeActionAndProfilePlacementAreUnchanged() throws Exception {
		assertEquals(1, EuclidianConstants.MODE_POINT);
		assertEquals(501, EuclidianConstants.MODE_POINT_ON_OBJECT);
		assertEquals(134, EuclidianConstants.MODE_LOCUS_V2_POINT);
		assertEquals(145, EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT);
		var profile = GeoCeDGProfile.getCatalog();
		var relations = GeoCeDGProfile.strings(GeoCeDGMenuBar.find(profile.getJSONArray(
				"presentation_groups"), "construction-relations").getJSONArray("toolbar_action_ids"));
		assertTrue(relations.containsAll(List.of("construction.point", "construction.point-on-object")));
		var semantic = GeoCeDGProfile.strings(GeoCeDGMenuBar.find(profile.getJSONArray(
				"presentation_groups"), "construction-semantic-curves").getJSONArray("toolbar_action_ids"));
		assertEquals(List.of("semantic.locus-v2.point-explicit", "semantic.locus-v2.point-from-reference"),
				semantic.subList(semantic.size() - 2, semantic.size()),
				"tools 134 and 145 keep their place in the semantic curves flyout");
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			app.setLanguage(Locale.ENGLISH);
			assertTrue(app.getToolHelp(EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT).contains("existing point"));
			app.setMode(EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT);
			assertEquals(EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT, app.getMode());
			assertEquals("Select object", instruction(app));
			// The explicit-address tool keeps its own dialog and never opens the Point panel.
			app.setMode(EuclidianConstants.MODE_LOCUS_V2_POINT);
			assertNull(panel(app));
			app.setMode(EuclidianConstants.MODE_MOVE);
		});
	}

	@Test void contextMenuOpensThePointPanelWithReferenceOriginAndTheObject() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var source = G9U1SemanticPointInteractionTest.spline(app);
			var q = Cu7M08SDesktopEventsTest.q(app, "Q=(0.3,0.02)");
			var gui = (GuiManagerGeoCeDG) app.getGuiManager();
			var item = gui.referencePointMenuItem(List.of(source));
			assertNotNull(item);
			assertEquals("Create point from reference Q", item.getText());
			item.doClick();
			assertEquals(EuclidianConstants.MODE_POINT, app.getMode());
			assertTrue(reference(app).isSelected());
			assertFalse(click(app).isSelected());
			Cu7M08SDesktopEventsTest.assertL(app, source);
			String before = app.getXML();
			Cu7M08SDesktopEventsTest.pointer(app, q.inhomX, q.inhomY);
			assertNotNull(tool(app).pendingSelection());
			assertEquals(before, app.getXML(), "nothing is created before Confirm");
			Cu7M08SDesktopEventsTest.confirm(app);
			var point = Cu7M08SDesktopEventsTest.point(app);
			assertEquals(.3, point.inhomX, 1e-8);
			assertTrue(reference(app).isSelected(), "the menu chose the Point origin preference");
			reference(app).doClick();
			assertTrue(click(app).isSelected());
			app.setMode(EuclidianConstants.MODE_MOVE);
		});
	}

	@Test void allowDraggingSurvivesAnOriginChangeAndLocksTheConfirmedPoint() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var source = G9U1SemanticPointInteractionTest.spline(app);
			var q = Cu7M08SDesktopEventsTest.q(app, "Q=(0.3,0.02)");
			app.setMode(EuclidianConstants.MODE_POINT);
			var free = Cu7M08SDesktopEventsTest.find(panel(app), JCheckBox.class,
					box -> "m08-allow-dragging".equals(box.getName()));
			assertTrue(free.isSelected(), "the default stays on");
			free.doClick();
			reference(app).doClick();
			assertFalse(Cu7M08SDesktopEventsTest.find(panel(app), JCheckBox.class,
					box -> "m08-allow-dragging".equals(box.getName())).isSelected());
			Cu7M08SDesktopEventsTest.pointer(app, -1, 0);
			Cu7M08SDesktopEventsTest.assertL(app, source);
			Cu7M08SDesktopEventsTest.pointer(app, q.inhomX, q.inhomY);
			Cu7M08SDesktopEventsTest.confirm(app);
			assertTrue(Cu7M08SDesktopEventsTest.point(app).isLocked());
			app.setMode(EuclidianConstants.MODE_MOVE);
		});
	}

	@Test void componentRejectionNamesPointByParameterAndTheExplicitAddressTool() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			app.setLanguage(Locale.ENGLISH);
			app.setMode(EuclidianConstants.MODE_POINT);
			String english = tool(app).describeFailure("COMPONENT_NOT_ESTABLISHED");
			assertTrue(english.contains("Point(curve, parameter)"), english);
			assertTrue(english.endsWith("Point on semantic curve (explicit address)"), english);
			assertFalse(english.equals("Component not established"));
			assertTrue(tool(app).describeFailure("BUDGET_EXHAUSTED").contains("Point(curve, parameter)"));
			app.setLanguage(SPANISH);
			app.setMode(EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT);
			String spanish = tool(app).describeFailure("COMPONENT_NOT_ESTABLISHED");
			assertTrue(spanish.contains("Point(curva, parámetro)"), spanish);
			assertTrue(spanish.endsWith("Punto sobre curva semántica (dirección explícita)"), spanish);
			assertEquals("Operación rechazada", tool(app).describeFailure("SOMETHING_ELSE"));
			app.setLanguage(Locale.ENGLISH);
			app.setMode(EuclidianConstants.MODE_MOVE);
		});
	}

	@Test void endpointAssistanceUsesPanelPresentationDragChoiceAndOneUndoStep() throws Exception {
		AtomicReference<AppGeoCeDG> host = new AtomicReference<>();
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			host.set(app);
			app.setLanguage(Locale.ENGLISH);
			Cu7M08SDesktopEventsTest.general(app);
			Cu7M08SDesktopEventsTest.q(app, "A=(0.8976484384297626,1.7854818601338847)");
			Cu7M08SDesktopEventsTest.q(app, "B=(3,-2)");
			// A synchronous baseline: the asynchronous initial store cannot land mid-test.
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
		AtomicReference<List<String>> shown = new AtomicReference<>();
		Cu7M08SDesktopEventsTest.edt(() -> {
			var source = (GeoLocusV2) app.getKernel().lookupLabel("h");
			var a = (GeoPoint) app.getKernel().lookupLabel("A");
			assertTrue(new GeoCeDGMetricEndpointAssistance(app).isApplicable(source, a));
			history.set(undo.getHistorySize());
			List<GeoPoint> before = points(app);
			// PRE-G9B-MAINT-U2-R1: the real option panes; the user selects the position.
			var chosen = new AtomicReference<String>();
			var panes = new MaintU2R1SmokeCorrectionsDesktopTest.Panes(
					pane -> MaintU2R1SmokeCorrectionsDesktopTest.press(pane, "Choose a position on the locus"),
					pane -> {
						var list = MaintU2R1SmokeCorrectionsDesktopTest.find(pane, JList.class, "m08-endpoint-positions");
						var guarantee = MaintU2R1SmokeCorrectionsDesktopTest.find(pane, JLabel.class, "m08-endpoint-guarantee");
						assertNotNull(list); assertNotNull(guarantee);
						List<String> labels = new ArrayList<>();
						for (int i = 0; i < list.getModel().getSize(); i++) {
							labels.add(String.valueOf(list.getModel().getElementAt(i)));
						}
						shown.set(labels);
						assertFalse(labels.isEmpty());
						assertTrue(labels.stream().allMatch(label -> label.contains("u = ")), labels.toString());
						assertTrue(labels.stream().noneMatch(label -> label.contains("t = ")),
								"no raw branch key");
						assertFalse(guarantee.getText().isBlank());
						assertNotNull(guarantee.getToolTipText());
						return MaintU2R1SmokeCorrectionsDesktopTest.choosePosition(0, false, "Create point", chosen)
								.apply(pane);
					});
			MaintU2R1SmokeCorrectionsDesktopTest.with(panes, () -> {
				app.setMode(EuclidianConstants.MODE_LOCUS_V2_LENGTH_BETWEEN);
				Cu7M08SDesktopEventsTest.pointer(app, .8, 2.4);
				Cu7M08SDesktopEventsTest.pointer(app, a.inhomX, a.inhomY);
				assertEquals(List.of(source, a), app.getSelectionManager().getSelectedGeos(), "L then A");
				undo.addUndoInfoStoredListener(() -> { stores.incrementAndGet(); stored.countDown(); });
				Cu7M08SDesktopEventsTest.pointer(app, 3, -2);
				return null;
			});
			var added = points(app).stream().filter(point -> !before.contains(point)).toList();
			assertEquals(1, added.size(), "the assistance creates exactly one point");
			created.set(added.get(0));
			assertTrue(GeoCeDGPointInteraction.owns(created.get()));
			assertSame(source, parent(created.get()).getSource());
			assertTrue(created.get().isLocked(), "Allow dragging off locks the point");
			assertFalse(created.get().getAllPredecessors().contains(a), "the reference is not a parent");
		});
		assertTrue(stored.await(5, TimeUnit.SECONDS), "the host stores the creation");
		Thread.sleep(500);
		assertEquals(1, stores.get(), "exactly one Undo step");
		assertEquals(history.get() + 1, undo.getHistorySize());
		String label = created.get().getLabelSimple();
		Cu7M08SDesktopEventsTest.edt(() -> app.getKernel().undo());
		Cu7M08SDesktopEventsTest.await(() -> app.getKernel().lookupLabel(label) == null);
		Cu7M08SDesktopEventsTest.edt(() -> {
			assertNotNull(app.getKernel().lookupLabel("h"));
			assertNotNull(app.getKernel().lookupLabel("A"));
			assertNotNull(app.getKernel().lookupLabel("B"));
			System.out.println("U2_ENDPOINT labels=" + shown.get() + " one_undo=true locked=true");
			app.setMode(EuclidianConstants.MODE_MOVE);
		});
	}

	@Test void endpointAssistanceCancelCreatesNothingAndIsLocalized() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			app.setLanguage(SPANISH);
			var source = Cu7M08SDesktopEventsTest.general(app);
			var a = Cu7M08SDesktopEventsTest.q(app, "A=(0.8976484384297626,1.7854818601338847)");
			assertTrue(new GeoCeDGMetricEndpointAssistance(app).isApplicable(source, a));
			String before = app.getXML();
			AtomicReference<JOptionPane> message = new AtomicReference<>();
			var panes = new MaintU2R1SmokeCorrectionsDesktopTest.Panes(pane -> {
				Object[] routes = pane.getOptions();
				assertEquals("Cancelar", routes[routes.length - 1]);
				return MaintU2R1SmokeCorrectionsDesktopTest.press(pane, "Elegir una posición en el lugar");
			}, pane -> {
				message.set(pane);
				return MaintU2R1SmokeCorrectionsDesktopTest.press(pane, "Cancelar");
			});
			assertNull(MaintU2R1SmokeCorrectionsDesktopTest.with(panes,
					() -> new GeoCeDGMetricEndpointAssistance(app).offer(source, a)));
			assertEquals(before, app.getXML());
			var free = MaintU2R1SmokeCorrectionsDesktopTest.find(message.get(), JCheckBox.class, "m08-allow-dragging");
			assertEquals("Permitir arrastre", free.getText());
			var list = MaintU2R1SmokeCorrectionsDesktopTest.find(message.get(), JList.class, "m08-endpoint-positions");
			assertTrue(String.valueOf(list.getModel().getElementAt(0)).contains("u = "));
			app.setLanguage(Locale.ENGLISH);
		});
	}

	@Test void pointOnObjectWithReferenceOriginMatchesPointOnANativeSegment() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			List<Double> xs = new ArrayList<>();
			for (int mode : new int[] {EuclidianConstants.MODE_POINT, EuclidianConstants.MODE_POINT_ON_OBJECT}) {
				var app = G9U1SemanticPointInteractionTest.app();
				var segment = G9U1SemanticPointInteractionTest.eval(app, "s=Segment((0,0),(2,0))");
				var q = Cu7M08SDesktopEventsTest.q(app, "Ref=(1,.03)");
				app.setMode(mode);
				reference(app).doClick();
				Cu7M08SDesktopEventsTest.pointer(app, .5, 0);
				Cu7M08SDesktopEventsTest.pointer(app, q.inhomX, q.inhomY);
				Cu7M08SDesktopEventsTest.confirm(app);
				var point = points(app).stream().filter(value -> value.getPath() == segment)
						.reduce((first, last) -> last).orElseThrow();
				assertFalse(point.getAllPredecessors().contains(q));
				xs.add(point.inhomX);
				app.setMode(EuclidianConstants.MODE_MOVE);
			}
			assertEquals(xs.get(0), xs.get(1), 0);
			assertEquals(1, xs.get(0), 1e-10);
		});
	}

	@Test void refusedOfferShowsTheTypedCodeOnlyAsDetail() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			G9U1SemanticPointInteractionTest.spline(app);
			var q = Cu7M08SDesktopEventsTest.q(app, "Q=(0.3,0.02)");
			app.setMode(EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT);
			Cu7M08SDesktopEventsTest.pointer(app, -1, 0);
			Cu7M08SDesktopEventsTest.pointer(app, q.inhomX, q.inhomY);
			q.setCoords(.4, .02, 1); q.updateCascade();
			Cu7M08SDesktopEventsTest.confirm(app);
			assertEquals("STALE_SELECTION", tool(app).lastFailure());
			var status = Cu7M08SDesktopEventsTest.find(panel(app), JTextField.class, field -> !field.isEditable());
			assertTrue(status.getText().startsWith("No point created: "), status.getText());
			// PRE-G9B-MAINT-U2-R1: a multi-line tooltip, the code on its own last line.
			assertTrue(((JComponent) status).getToolTipText().endsWith("<br>[STALE_SELECTION]</html>"));
			app.setMode(EuclidianConstants.MODE_MOVE);
		});
	}
}
