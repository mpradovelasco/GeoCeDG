/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.tree.TreePath;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.dimension.AlgoNativeDimension;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.algos.AlgoPointOnPath;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.main.App;
import org.geogebra.desktop.gui.layout.DockManagerD;
import org.geogebra.desktop.gui.layout.DockPanelD;
import org.geogebra.desktop.gui.view.algebra.GeoMutableTreeNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Desktop entry and visibility regressions. Physical OS input is recorded separately. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class R5ReferenceCreationDesktopTest {
    @BeforeAll static void initialize() { G9U1SemanticPointInteractionTest.initializeDesktop(); }
    private static GeoElement eval(AppGeoCeDG app, String command) {
        return G9U1SemanticPointInteractionTest.eval(app, command);
    }
    private static GeoCeDGReferencePointTool tool(AppGeoCeDG app) {
        return G9U1SemanticPointInteractionTest.controller(app).referencePointTool();
    }
    private static GeoLocusV2 parabola(AppGeoCeDG app) {
        for (String command : List.of("Focus=(0,1/4)",
                "directrix=Line((-2,-1/4),(2,-1/4))", "segment=Segment((-2,-1/4),(2,-1/4))",
                "Driver=Point(segment)", "normal=PerpendicularLine(Driver,directrix)",
                "chord=Line(Focus,Driver)", "Middle=Midpoint(Focus,Driver)",
                "bisector=PerpendicularLine(Middle,chord)", "Q=Intersect(normal,bisector)",
                "L=LocusV2(Q,Driver)")) { eval(app, command); }
        var source = (GeoLocusV2) app.getKernel().lookupLabel("L");
        for (var geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
            geo.setEuclidianVisible(geo == source); geo.updateRepaint();
        }
        return source;
    }
    private static JToggleButton reference(Container panel) {
        return Cu7M08SDesktopEventsTest.find(panel, JToggleButton.class,
                value -> "m08-from-reference".equals(value.getName()));
    }
    private static GeoPoint nativePoint(AppGeoCeDG app, GeoElement source) {
        return app.getKernel().getConstruction().getGeoSetConstructionOrder().stream()
                .filter(GeoPoint.class::isInstance).map(GeoPoint.class::cast)
                .filter(point -> point.getPath() == source).reduce((first, last) -> last).orElseThrow();
    }

    private static TreePath algebraPath(GeoCeDGAlgebraView tree, GeoElement geo) {
        var root = (javax.swing.tree.DefaultMutableTreeNode) tree.getModel().getRoot();
        var nodes = root.depthFirstEnumeration();
        while (nodes.hasMoreElements()) {
            var node = nodes.nextElement();
            if (node instanceof GeoMutableTreeNode value && value.getGeo() == geo) {
                return new TreePath(value.getPath());
            }
        }
        throw new AssertionError("Algebra row missing for " + geo.getLabelSimple());
    }

    private static void algebraClick(GeoCeDGAlgebraView tree, GeoElement geo, int count) {
        tree.setSize(800, 600);
        var path = algebraPath(tree, geo); tree.expandPath(path.getParentPath());
        var bounds = tree.getPathBounds(path); assertNotNull(bounds);
        int x = bounds.x + Math.min(80, bounds.width - 1), y = bounds.y + bounds.height / 2;
        for (int id : new int[] {MouseEvent.MOUSE_PRESSED, MouseEvent.MOUSE_RELEASED, MouseEvent.MOUSE_CLICKED}) {
            tree.dispatchEvent(new MouseEvent(tree, id, System.currentTimeMillis(),
                    id == MouseEvent.MOUSE_PRESSED ? InputEvent.BUTTON1_DOWN_MASK : 0,
                    x, y, count, false, MouseEvent.BUTTON1));
        }
    }

    private static DockManagerD manager(AppGeoCeDG app) {
        return ((GuiManagerGeoCeDG) app.getGuiManager()).getLayout().getDockManager();
    }

    private static void globalCanvasClick(AppGeoCeDG app) {
        var canvas = app.getEuclidianView1().getJPanel();
        assertNotNull(javax.swing.SwingUtilities.getAncestorOfClass(DockPanelD.class, canvas));
        canvas.dispatchEvent(new MouseEvent(canvas, MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(), 0, 10, 10, 1, false, MouseEvent.BUTTON1));
    }

    private record DesktopContext(String xml, int history, DockPanelD focused,
            Object view, long generation, int mode, List<GeoElement> selection) {
        static DesktopContext capture(AppGeoCeDG app) {
            var xml = new XMLStringBuilder();
            app.getKernel().getConstruction().getConstructionXML(xml, false);
            return new DesktopContext(xml.toString(),
                    app.getKernel().getConstruction().getUndoManager().getHistorySize(),
                    manager(app).getFocusedPanel(), app.getActiveEuclidianView(),
                    manager(app).getEuclidianViewActivationGeneration(), app.getMode(),
                    List.copyOf(app.getSelectionManager().getSelectedGeos()));
        }
        void assertUnchanged(AppGeoCeDG app) { assertEquals(this, capture(app)); }
    }

    @Test void globalAwtClicksKeepTwoApplicationsAndTheirReferenceContextsSeparate() throws Exception {
        record Hosts(AppGeoCeDG first, GeoLocusV2 semantic, GeoPoint firstQ,
                AppGeoCeDG second, GeoElement nativeSource, GeoPoint secondQ) { }
        var hosts = new AtomicReference<Hosts>();
        Cu7M08SDesktopEventsTest.edt(() -> {
            var first = G9U1SemanticPointInteractionTest.app(); first.buildApplicationPanel();
            var semantic = parabola(first);
            var firstQ = (GeoPoint) eval(first, "ReferenceQ=(.43,.12)");
            var second = G9U1SemanticPointInteractionTest.app(); second.buildApplicationPanel();
            var nativeSource = eval(second, "source=Segment((0,0),(2,0))");
            var secondQ = (GeoPoint) eval(second, "ReferenceQ=(1,.03)");
            first.setActiveView(App.VIEW_EUCLIDIAN); second.setActiveView(App.VIEW_EUCLIDIAN);
            hosts.set(new Hosts(first, semantic, firstQ, second, nativeSource, secondQ));
        });
        var pair = hosts.get(); var first = pair.first(); var second = pair.second();
        var firstUndo = (GeoCeDGUndoManager) first.getKernel().getConstruction().getUndoManager();
        var secondUndo = (GeoCeDGUndoManager) second.getKernel().getConstruction().getUndoManager();
        Cu7M08SDesktopEventsTest.await(() -> firstUndo.isQuiescent() && secondUndo.isQuiescent());
        Cu7M08SDesktopEventsTest.edt(() -> {
            first.getKernel().initUndoInfo(); second.getKernel().initUndoInfo();
        });
        Cu7M08SDesktopEventsTest.await(() -> firstUndo.isQuiescent() && secondUndo.isQuiescent()
                && firstUndo.getHistorySize() == 0 && secondUndo.getHistorySize() == 0);
        Cu7M08SDesktopEventsTest.edt(() -> {
            assertTrue(G9U1TestApp.dockManagers().containsAll(List.of(manager(first), manager(second))));
            var delivered = new AtomicInteger();
            AWTEventListener observer = event -> {
                if (event.getID() == MouseEvent.MOUSE_CLICKED
                        && (event.getSource() == first.getEuclidianView1().getJPanel()
                        || event.getSource() == second.getEuclidianView1().getJPanel())) {
                    delivered.incrementAndGet();
                }
            };
            Toolkit.getDefaultToolkit().addAWTEventListener(observer, java.awt.AWTEvent.MOUSE_EVENT_MASK);
            try {
                var beforeFirst = DesktopContext.capture(first);
                globalCanvasClick(second); beforeFirst.assertUnchanged(first);
                assertSame(second.getEuclidianView1(), second.getActiveEuclidianView());
                var beforeSecond = DesktopContext.capture(second);
                globalCanvasClick(first); beforeSecond.assertUnchanged(second);
                assertSame(first.getEuclidianView1(), first.getActiveEuclidianView());
                assertEquals(2, delivered.get(), "Component dispatch must traverse the global AWT listeners");
            } finally { Toolkit.getDefaultToolkit().removeAWTEventListener(observer); }
            var untouchedSecond = DesktopContext.capture(second);
            ((GuiManagerGeoCeDG) first.getGuiManager()).referencePointMenuItem(List.of(pair.semantic())).doClick();
            assertEquals(EuclidianConstants.MODE_POINT, first.getMode());
            assertTrue(reference(Cu7M08SDesktopEventsTest.panel(first)).isSelected());
            assertTrue(first.getSelectionManager().getSelectedGeos().contains(pair.semantic()));
            algebraClick((GeoCeDGAlgebraView) first.getGuiManager().getAlgebraView(), pair.firstQ(), 1);
            assertNotNull(tool(first).pendingSelection(), tool(first).lastFailure());
            assertTrue(tool(first).pendingSelection().getRequest().isCurrent());
            Cu7M08SDesktopEventsTest.confirm(first);
            var semanticPoint = Cu7M08SDesktopEventsTest.point(first);
            assertSame(pair.semantic(), ((AlgoSemanticLocusPoint2D) semanticPoint.getParentAlgorithm()).getSource());
            untouchedSecond.assertUnchanged(second);
        });
        // Ordinary Desktop stores are asynchronous; measure isolation only after our own store ends.
        Cu7M08SDesktopEventsTest.await(() -> firstUndo.isQuiescent() && firstUndo.getHistorySize() == 1);
        var untouchedFirst = new AtomicReference<DesktopContext>();
        Cu7M08SDesktopEventsTest.edt(() -> {
            assertEquals(0, secondUndo.getHistorySize());
            untouchedFirst.set(DesktopContext.capture(first));
            ((GuiManagerGeoCeDG) second.getGuiManager()).referencePointMenuItem(List.of(pair.nativeSource())).doClick();
            assertTrue(reference(Cu7M08SDesktopEventsTest.panel(second)).isSelected());
            algebraClick((GeoCeDGAlgebraView) second.getGuiManager().getAlgebraView(), pair.secondQ(), 1);
            Cu7M08SDesktopEventsTest.confirm(second);
            var point = nativePoint(second, pair.nativeSource());
            assertSame(pair.nativeSource(), point.getPath());
            assertFalse(point.getAllPredecessors().contains(pair.secondQ()));
            untouchedFirst.get().assertUnchanged(first);
        });
        Cu7M08SDesktopEventsTest.await(() -> secondUndo.isQuiescent() && secondUndo.getHistorySize() == 1);
        Cu7M08SDesktopEventsTest.edt(() -> {
            assertTrue(firstUndo.isQuiescent()); assertEquals(1, firstUndo.getHistorySize());
            untouchedFirst.get().assertUnchanged(first);
        });
    }

    @Test void foreignProgrammaticPanelsCannotChangeFocusTicketsOrConstruction() throws Exception {
        Cu7M08SDesktopEventsTest.edt(() -> {
            var first = G9U1SemanticPointInteractionTest.app(); first.buildApplicationPanel();
            var source = eval(first, "source=Segment((0,0),(2,0))");
            var second = G9U1SemanticPointInteractionTest.app(); second.buildApplicationPanel();
            first.setActiveView(App.VIEW_EUCLIDIAN); second.setActiveView(App.VIEW_EUCLIDIAN);
            var request = GeoCeDGPointRequest.forSource(first, source, 1, .03,
                    LocusPointInteractionPolicy2D.initial(.1));
            var beforeFirst = DesktopContext.capture(first); var beforeSecond = DesktopContext.capture(second);
            for (int viewId : new int[] {App.VIEW_EUCLIDIAN, App.VIEW_ALGEBRA}) {
                var foreign = manager(second).getPanel(viewId); assertNotNull(foreign);
                manager(first).setFocusedPanel(foreign, false);
                manager(first).setFocusedPanel(foreign);
                assertTrue(request.isCurrent());
                beforeFirst.assertUnchanged(first); beforeSecond.assertUnchanged(second);
            }
            manager(second).setFocusedPanel(manager(first).getPanel(App.VIEW_EUCLIDIAN), false);
            beforeFirst.assertUnchanged(first); beforeSecond.assertUnchanged(second);
        });
    }

    @Test void ownViewAbaAndNullStillInvalidateWhileANewOwnReferenceRequestWorks() throws Exception {
        Cu7M08SDesktopEventsTest.edt(() -> {
            var app = G9U1SemanticPointInteractionTest.app(); app.buildApplicationPanel();
            app.getGuiManager().setShowView(true, App.VIEW_EUCLIDIAN2);
            app.setActiveView(App.VIEW_EUCLIDIAN);
            var source = eval(app, "source=Segment((0,0),(2,0))");
            var q = (GeoPoint) eval(app, "ReferenceQ=(1,.03)");
            var request = GeoCeDGPointRequest.forSource(app, source, 1, .03,
                    LocusPointInteractionPolicy2D.initial(.1));
            var before = DesktopContext.capture(app);
            manager(app).setFocusedPanel(manager(app).getPanel(App.VIEW_EUCLIDIAN2), false);
            long secondGeneration = manager(app).getEuclidianViewActivationGeneration();
            assertTrue(secondGeneration > before.generation());
            manager(app).setFocusedPanel(manager(app).getPanel(App.VIEW_EUCLIDIAN), false);
            assertSame(app.getEuclidianView1(), app.getActiveEuclidianView());
            assertTrue(manager(app).getEuclidianViewActivationGeneration() > secondGeneration);
            assertFalse(request.isCurrent());
            var afterAba = DesktopContext.capture(app);
            assertEquals(before.xml(), afterAba.xml()); assertEquals(before.history(), afterAba.history());
            var current = GeoCeDGPointRequest.forSource(app, source, 1, .03,
                    LocusPointInteractionPolicy2D.initial(.1));
            manager(app).setFocusedPanel(manager(app).getPanel(App.VIEW_ALGEBRA), false);
            assertEquals(afterAba.generation(), manager(app).getEuclidianViewActivationGeneration());
            assertTrue(current.isCurrent());
            manager(app).setFocusedPanel(null, false);
            assertFalse(current.isCurrent());
            assertTrue(manager(app).getEuclidianViewActivationGeneration() > afterAba.generation());
            app.setActiveView(App.VIEW_EUCLIDIAN);
            ((GuiManagerGeoCeDG) app.getGuiManager()).referencePointMenuItem(List.of(source)).doClick();
            assertTrue(reference(Cu7M08SDesktopEventsTest.panel(app)).isSelected());
            assertTrue(tool(app).selectFromAlgebra(q, app.getMainComponent()));
            Cu7M08SDesktopEventsTest.confirm(app);
            assertSame(source, nativePoint(app, source).getPath());
        });
    }

    @Test void secondAlgebraClickKeepsThePendingNativeAndV2OffersInsteadOfRedefiningQ() throws Exception {
        Cu7M08SDesktopEventsTest.edt(() -> {
            for (boolean semantic : new boolean[] {false, true}) {
                var app = G9U1SemanticPointInteractionTest.app();
                GeoElement source = semantic ? parabola(app)
                        : eval(app, "source=Segment((0,0),(2,0))");
                var q = (GeoPoint) eval(app, semantic ? "ReferenceQ=(.43,.12)" : "ReferenceQ=(1,.03)");
                var gui = (GuiManagerGeoCeDG) app.getGuiManager();
                gui.referencePointMenuItem(List.of(source)).doClick();
                var tree = (GeoCeDGAlgebraView) gui.getAlgebraView();
                algebraClick(tree, q, 1);
                var offerPanel = tool(app).contextMenu(); assertNotNull(offerPanel);
                var semanticOffer = tool(app).pendingSelection();
                long misses = semanticOffer == null ? 0 : semanticOffer.getSemanticMisses();
                int geos = app.getKernel().getConstruction().getGeoSetConstructionOrder().size();
                int history = app.getKernel().getConstruction().getUndoManager().getHistorySize();
                algebraClick(tree, q, 2);
                assertFalse(tree.isEditing());
                assertEquals(EuclidianConstants.MODE_POINT, app.getMode());
                assertSame(offerPanel, tool(app).contextMenu());
                assertSame(semanticOffer, tool(app).pendingSelection());
                if (semanticOffer != null) { assertEquals(misses, semanticOffer.getSemanticMisses()); }
                assertEquals(geos, app.getKernel().getConstruction().getGeoSetConstructionOrder().size());
                assertEquals(history, app.getKernel().getConstruction().getUndoManager().getHistorySize());
                Cu7M08SDesktopEventsTest.confirm(app);
                assertEquals(1, app.getKernel().getConstruction().getGeoSetConstructionOrder().stream()
                        .filter(GeoPoint.class::isInstance).map(GeoPoint.class::cast)
                        .filter(point -> semantic ? GeoCeDGPointInteraction.owns(point)
                                : point.getPath() == source).count());
            }
        });
    }

    @Test void algebraObjectRowClicksReachReferenceSelectionWithoutChangingTools() throws Exception {
        Cu7M08SDesktopEventsTest.edt(() -> {
            var app = G9U1SemanticPointInteractionTest.app();
            var source = eval(app, "source=Segment((0,0),(2,0))");
            var q = (GeoPoint) eval(app, "ReferenceQ=(1,.03)");
            var gui = (GuiManagerGeoCeDG) app.getGuiManager();
            gui.referencePointMenuItem(List.of(source)).doClick();
            var tree = (GeoCeDGAlgebraView) gui.getAlgebraView();
            algebraClick(tree, q, 1);
            assertEquals(EuclidianConstants.MODE_POINT, app.getMode());
            assertNotNull(tool(app).contextMenu());
            Cu7M08SDesktopEventsTest.confirm(app);
            assertSame(source, nativePoint(app, source).getPath());
        });
    }

    @Test void nativeReferenceFixedOptionBlocksEditingButKeepsConductorDependence() throws Exception {
        Cu7M08SDesktopEventsTest.edt(() -> {
            var app = G9U1SemanticPointInteractionTest.app();
            eval(app, "A=(0,0)"); var b = (GeoPoint) eval(app, "B=(2,0)");
            var source = eval(app, "source=Segment(A,B)");
            var q = (GeoPoint) eval(app, "ReferenceQ=(1,.03)");
            ((GuiManagerGeoCeDG) app.getGuiManager()).referencePointMenuItem(List.of(source)).doClick();
            Cu7M08SDesktopEventsTest.find(Cu7M08SDesktopEventsTest.panel(app), JCheckBox.class,
                    value -> "m08-allow-dragging".equals(value.getName())).doClick();
            tool(app).selectFromAlgebra(q, app.getMainComponent());
            Cu7M08SDesktopEventsTest.confirm(app);
            var point = nativePoint(app, source);
            assertTrue(point.isLocked()); assertEquals(1, point.inhomX, 0);
            b.setCoords(4, 0, 1); b.updateCascade();
            assertEquals(2, point.inhomX, 0); assertTrue(point.isLocked());
            assertFalse(point.getAllPredecessors().contains(q));
        });
    }

    @Test void pointControlsAndConfirmationRemainMountedWhenInputIsHidden() throws Exception {
        Cu7M08SDesktopEventsTest.edt(() -> {
            var app = G9U1SemanticPointInteractionTest.app(); var source = parabola(app);
            JPanel application = app.buildApplicationPanel();
            app.setShowAlgebraInput(false, true); app.setMode(EuclidianConstants.MODE_POINT);
            var toggle = reference(application);
            assertNotNull(toggle, "Reference option must belong to the visible product layout");
            assertFalse(app.showAlgebraInput());
            Cu7M08SDesktopEventsTest.pointer(app, 1, 1);
            assertNotNull(tool(app).pendingSelection(), tool(app).lastFailure());
            var confirm = Cu7M08SDesktopEventsTest.button(application, "Confirm");
            assertNotNull(confirm, "Valid offer confirmation must not live in hidden input");
            Cu7M08SDesktopEventsTest.press(confirm);
            var point = Cu7M08SDesktopEventsTest.point(app);
            assertSame(source, ((AlgoSemanticLocusPoint2D) point.getParentAlgorithm()).getSource());
            assertFalse(point.isLocked()); assertFalse(app.showAlgebraInput());
            app.setShowAlgebraInput(true, true);
            assertNotNull(reference(Cu7M08SDesktopEventsTest.panel(app)));
            app.setShowAlgebraInput(false, true);
            assertNotNull(reference(application), "Layout rebuild remounts the same contextual flow");
        });
    }

    @Test void canvasOfferRadiusDoesNotBecomeTheNextReferencePolicy() throws Exception {
        Cu7M08SDesktopEventsTest.edt(() -> {
            var app = G9U1SemanticPointInteractionTest.app(); var source = parabola(app);
            var q = (GeoPoint) eval(app, "ReferenceQ=(.43,.12)");
            app.setMode(EuclidianConstants.MODE_POINT);
            var tool = tool(app);
            double capturedRadius = .028284271247461905;
            var request = tool.bindContext(GeoCeDGPointRequest.forSource(app, source, 1, 1,
                    tool.canvasPolicy(capturedRadius)));
            var offer = new GeoCeDGReferencePlacement(source, request);
            tool.offerWorld(request, offer);
            assertEquals(capturedRadius, Double.parseDouble(Cu7M08SDesktopEventsTest.radius(app).getText()), 0);
            reference(Cu7M08SDesktopEventsTest.panel(app)).doClick();
            assertEquals(.1, Double.parseDouble(Cu7M08SDesktopEventsTest.radius(app).getText()), 0);
            assertTrue(tool.selectFromAlgebra(source, app.getMainComponent()));
            assertTrue(tool.selectFromAlgebra(q, app.getMainComponent()));
            assertNotNull(tool.pendingSelection(), tool.lastFailure());
            assertEquals(.1, tool.pendingSelection().getRequest().getRadius(), 0);
            Cu7M08SDesktopEventsTest.confirm(app);
            assertNotNull(Cu7M08SDesktopEventsTest.point(app));
        });
    }

    @Test void anExplicitReferencePolicySurvivesCanvasOffersAndRetainsItsRejection() throws Exception {
        Cu7M08SDesktopEventsTest.edt(() -> {
            var app = G9U1SemanticPointInteractionTest.app(); var source = parabola(app);
            var q = (GeoPoint) eval(app, "ReferenceQ=(.43,.12)");
            app.setMode(EuclidianConstants.MODE_POINT);
            var tool = tool(app); tool.setPolicy(LocusPointInteractionPolicy2D.initial(.01));
            var request = tool.bindContext(GeoCeDGPointRequest.forSource(app, source, 1, 1,
                    tool.canvasPolicy(.03)));
            tool.offerWorld(request, new GeoCeDGReferencePlacement(source, request));
            reference(Cu7M08SDesktopEventsTest.panel(app)).doClick();
            assertEquals(.01, Double.parseDouble(Cu7M08SDesktopEventsTest.radius(app).getText()), 0);
            tool.selectFromAlgebra(source, app.getMainComponent());
            String before = app.getXML();
            tool.selectFromAlgebra(q, app.getMainComponent());
            assertNull(tool.pendingSelection());
            assertEquals("NO_ADDRESS_DISCOVERED", tool.lastFailure());
            assertEquals(before, app.getXML());
        });
    }

    @Test void nativeContextReferenceWorksForCircleSegmentAndParabolaWithoutAQParent(@TempDir Path directory)
            throws Exception {
        Cu7M08SDesktopEventsTest.edt(() -> {
            String[] commands = {"source=Circle((0,0),1)", "source=Segment((0,0),(2,0))",
                    "source=Parabola((0,1/4),Line((-2,-1/4),(2,-1/4)))"};
            double[][] references = {{1.04, .02}, {1, .03}, {.43, .12}};
            for (int index = 0; index < commands.length; index++) {
                var app = G9U1SemanticPointInteractionTest.app();
                var source = eval(app, commands[index]);
                var q = (GeoPoint) eval(app, "ReferenceQ=(" + references[index][0] + "," + references[index][1] + ")");
                var gui = (GuiManagerGeoCeDG) app.getGuiManager();
                var action = gui.referencePointMenuItem(List.of(source));
                assertNotNull(action); assertEquals("Create point from reference Q", action.getText());
                action.doClick();
                assertEquals(EuclidianConstants.MODE_POINT, app.getMode());
                assertTrue(reference(Cu7M08SDesktopEventsTest.panel(app)).isSelected());
                assertTrue(Cu7M08SDesktopEventsTest.find(Cu7M08SDesktopEventsTest.panel(app), JCheckBox.class,
                        value -> "m08-allow-dragging".equals(value.getName())).isSelected());
                assertTrue(tool(app).selectFromAlgebra(q, app.getMainComponent()));
                Cu7M08SDesktopEventsTest.confirm(app);
                var point = nativePoint(app, source); point.setLabel("NativeP");
                assertInstanceOf(AlgoPointOnPath.class, point.getParentAlgorithm());
                assertFalse(point.isLocked()); assertFalse(GeoCeDGPointInteraction.owns(point));
                assertFalse(point.getAllPredecessors().contains(q));
                assertSame(source, point.getParentAlgorithm().getInput()[0]);
                double x = point.inhomX, y = point.inhomY;
                q.setCoords(8, 9, 1); q.updateCascade();
                assertEquals(x, point.inhomX, 0); assertEquals(y, point.inhomY, 0);
                var file = directory.resolve("native-q-" + index + ".cedg");
                assertTrue(gui.saveAsTo(file.toFile()));
                var reopened = G9U1SemanticPointInteractionTest.app();
                assertTrue(reopened.loadFile(file.toFile(), false));
                var restored = (GeoPoint) reopened.getKernel().lookupLabel("NativeP");
                assertSame(reopened.getKernel().lookupLabel("source"), restored.getPath());
                assertTrue(restored.isPointerChangeable());
                assertFalse(restored.getAllPredecessors().contains(reopened.getKernel().lookupLabel("ReferenceQ")));
            }
        });
    }

    @Test void nativeQUsesItsOrdinaryProjectionWithoutTheV2DiscoveryRadius() throws Exception {
        Cu7M08SDesktopEventsTest.edt(() -> {
            var app = G9U1SemanticPointInteractionTest.app();
            var source = eval(app, "source=Segment((0,0),(2,0))");
            var q = (GeoPoint) eval(app, "ReferenceQ=(1,5)");
            try (var placement = new GeoCeDGNativePointPlacement(source, q, 1, 5, .01)) {
                var point = placement.commit(true);
                assertEquals(1, point.inhomX, 0); assertEquals(0, point.inhomY, 0);
                assertSame(source, point.getPath()); assertFalse(point.getAllPredecessors().contains(q));
            }
        });
    }

    @Test void unsupportedNativePathHasNoNewContextPermission() throws Exception {
        Cu7M08SDesktopEventsTest.edt(() -> {
            var app = G9U1SemanticPointInteractionTest.app();
            var source = eval(app, "f=x^2");
            assertFalse(GeoCeDGNativePointPlacement.supports(source));
            assertNull(((GuiManagerGeoCeDG) app.getGuiManager()).referencePointMenuItem(List.of(source)));
            assertThrows(IllegalArgumentException.class,
                    () -> new GeoCeDGNativePointPlacement(source, null, 0, 0, .1));
        });
    }

    @Test void dimensionFigureSegmentsRetainP4InsteadOfAcquiringNativeReferencePlacement() throws Exception {
        Cu7M08SDesktopEventsTest.edt(() -> {
            var app = G9U1SemanticPointInteractionTest.app();
            eval(app, "A=(-2,0)"); eval(app, "B=(2,0)");
            var ordinary = eval(app, "ordinary=Segment(A,B)");
            var q = (GeoPoint) eval(app, "ReferenceQ=(.5,1.03)");
            var gui = (GuiManagerGeoCeDG) app.getGuiManager();
            assertTrue(GeoCeDGNativePointPlacement.supports(ordinary));
            assertNotNull(gui.referencePointMenuItem(List.of(ordinary)));
            for (String command : List.of("AlignedDimension(A,B,1)", "LinearDimension(A,B,xAxis,1)")) {
                var outputs = app.getKernel().getAlgebraProcessor().processAlgebraCommand(command, false);
                assertNotNull(outputs); assertEquals(5, outputs.length);
                var owner = assertInstanceOf(AlgoNativeDimension.class, outputs[0].getParentAlgorithm());
                app.setMode(EuclidianConstants.MODE_POINT);
                var option = reference(Cu7M08SDesktopEventsTest.panel(app));
                if (!option.isSelected()) { option.doClick(); }
                assertTrue(tool(app).isReferenceRequestActive());
                String before = app.getXML();
                int history = app.getKernel().getConstruction().getUndoManager().getHistorySize();
                var selection = List.copyOf(app.getSelectionManager().getSelectedGeos());
                for (var figure : List.of(owner.getDimensionLine(), owner.getExtensionA(), owner.getExtensionB())) {
                    assertSame(owner, AlgoNativeDimension.ownerOf(figure));
                    assertFalse(GeoCeDGNativePointPlacement.supports(figure));
                    assertNull(gui.referencePointMenuItem(List.of(figure)));
                    assertThrows(IllegalArgumentException.class, () -> tool(app).startFromReference(figure));
                    assertThrows(IllegalArgumentException.class,
                            () -> new GeoCeDGNativePointPlacement(figure, q, q.inhomX, q.inhomY, .1));
                    assertFalse(tool(app).selectFromAlgebra(figure, app.getMainComponent()));
                    tool(app).click(List.of(figure), app.getMainComponent(), 0, 0);
                    assertNull(tool(app).contextMenu()); assertNull(tool(app).pendingSelection());
                }
                assertEquals(before, app.getXML());
                assertEquals(history, app.getKernel().getConstruction().getUndoManager().getHistorySize());
                assertEquals(selection, app.getSelectionManager().getSelectedGeos());
                assertTrue(GeoCeDGNativePointPlacement.supports(ordinary));
                assertNotNull(gui.referencePointMenuItem(List.of(ordinary)));
                app.setMode(EuclidianConstants.MODE_MOVE);
            }
        });
    }
}
