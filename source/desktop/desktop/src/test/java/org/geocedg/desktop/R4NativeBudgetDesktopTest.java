/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.baseline;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.store;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.undo;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JTextField;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.NativeTransactionalState2D;
import org.geocedg.common.kernel.locus.NativeTransactionalState2D.UnsupportedReason;
import org.geocedg.common.kernel.locus.interaction.IndividualForwardPointCreation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.ExpressionValue;
import org.geogebra.common.kernel.arithmetic.MyDouble;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.plugin.Operation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Real frontend/native-history resource boundaries; no physical mouse-smoke claim. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class R4NativeBudgetDesktopTest {

	@BeforeAll static void initialize() { G9U1SemanticPointInteractionTest.initializeDesktop(); }

	private static GeoElement eval(AppGeoCeDG app, String command) {
		return G9U1SemanticPointInteractionTest.eval(app, command);
	}

	@Test void creationNativeQAndDragExposeTheNativeResourceDiagnosticWithoutPublication() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			for (String command : List.of("driver=Circle((0,0),1)", "N=Point(driver)",
					"v=Vector((0,0),N)", "Traced=N+2v", "L=LocusV2(Traced,N)")) { eval(app, command); }
			var source = (GeoLocusV2) app.getKernel().lookupLabel("L");
			var policy = LocusPointInteractionPolicy2D.initial(.1);
			GeoPoint moving;
			try (var selection = IndividualForwardPointCreation2D.query(source, 3, 0, policy)) {
				moving = selection.commit("Moving", selection.getChoices().get(0));
			}
			var reference = (GeoPoint) eval(app, "Reference=(1,0)");
			resourceHost(app);
			var controller = G9U1SemanticPointInteractionTest.controller(app);
			var tool = controller.referencePointTool(); tool.activate(); tool.setPolicy(policy);
			String xml = app.getXML();
			tool.queryWorld(source, 3, 0);
			var offer = tool.pendingSelection(); assertNotNull(offer);
			assertEquals(GeoCeDGReferencePlacement.Route.INDIVIDUAL_FORWARD_ADDRESS_CREATION, offer.getRoute());
			assertNull(tool.commit(offer.getChoices().get(0)));
			assertEquals(UnsupportedReason.SOURCE_FIDELITY_RESOURCE_EXHAUSTED.name(), tool.lastFailure());
			Field status = GeoCeDGReferencePointTool.class.getDeclaredField("status"); status.setAccessible(true);
			assertResourceMessage(((JTextField) status.get(tool)).getText());
			try (var nativeOffer = new GeoCeDGNativePointPlacement(app.getKernel().lookupLabel("driver"),
					reference, 1, 0, .1)) {
				assertEquals(UnsupportedReason.SOURCE_FIDELITY_RESOURCE_EXHAUSTED.name(),
						assertThrows(IllegalStateException.class, () -> nativeOffer.commit(true)).getMessage());
			}
			var interaction = new GeoCeDGPointInteraction(app);
			assertFalse(interaction.move(moving, 3 * Math.cos(.05), 3 * Math.sin(.05), .1,
					interaction.beginDrag(moving)));
			assertResourceMessage(interaction.getLastMoveDiagnostic());
			assertEquals(xml, app.getXML());
		});
	}

	@Test void exhaustedCurrentHostCannotAdvanceUndoOrRedoCursorAndANewPassRecovers() throws Exception {
		var holder = new AtomicReference<AppGeoCeDG>();
		Cu7M08SDesktopEventsTest.edt(() -> {
			var created = G9U1SemanticPointInteractionTest.app(); eval(created, "AxisP=Point(xAxis)");
			baseline(created); holder.set(created);
		});
		var app = holder.get(); assertNotNull(app);
		Cu7M08SDesktopEventsTest.edt(() -> {
			GeoPoint point = (GeoPoint) app.getKernel().lookupLabel("AxisP");
			point.setCoords(2, 0, 1); point.updateCascade();
		});
		store(app);
		Cu7M08SDesktopEventsTest.edt(() -> {
			var manager = undo(app); assertTrue(manager.undoPossible());
			resourceHost(app);
			assertResourceHistoryUnchanged(app, manager::undo);
			assertResourceHistoryUnchanged(app, () -> {
				try (var ignored = manager.prepareUndoBaseline()) { fail("Exhausted capture returned a baseline"); }
				catch (java.io.IOException failure) { throw new IllegalStateException(failure); }
			});
			((GeoNumeric) app.getKernel().lookupLabel("ResourceRadius")).setDefinition(null);
			assertNull(NativeTransactionalState2D.unsupportedReason(app.getKernel().getConstruction()));
			manager.undo(); assertTrue(manager.redoPossible());
			resourceHost(app); assertResourceHistoryUnchanged(app, manager::redo);
			((GeoNumeric) app.getKernel().lookupLabel("ResourceRadius")).setDefinition(null);
			manager.redo(); assertEquals(2, ((GeoPoint) app.getKernel().lookupLabel("AxisP")).getInhomX());
		});
	}

	private static void assertResourceHistoryUnchanged(AppGeoCeDG app, Runnable command) {
		var manager = undo(app); String xml = app.getXML(); var commands = manager.getCommands();
		int cursor = manager.getCursor();
		var registry = app.getKernel().getConstruction().getSpatialIdentityRegistry();
		var ids = registry.getRecords().stream().map(record -> record.getId()).toList();
		int reservations = registry.getReservedIdentityCount();
		var nativeState = ((GeoPoint) app.getKernel().lookupLabel("AxisP")).captureNativeRollbackState();
		assertEquals(UnsupportedReason.SOURCE_FIDELITY_RESOURCE_EXHAUSTED.name(),
				assertThrows(IllegalStateException.class, command::run).getMessage());
		assertEquals(xml, app.getXML()); assertEquals(cursor, manager.getCursor());
		assertEquals(commands.size(), manager.getCommands().size());
		for (int index = 0; index < commands.size(); index++) {
			assertSame(commands.get(index), manager.getCommands().get(index));
		}
		assertEquals(ids, registry.getRecords().stream().map(record -> record.getId()).toList());
		assertEquals(reservations, registry.getReservedIdentityCount());
		assertTrue(nativeState.matches((GeoPoint) app.getKernel().lookupLabel("AxisP")));
		assertTrue(manager.isQuiescent());
	}

	private static void assertResourceMessage(String message) {
		assertNotNull(message); String lower = message.toLowerCase(java.util.Locale.ROOT);
		assertTrue(lower.contains("budget") || lower.contains("presupuesto"), message);
		assertTrue(lower.contains("native") || lower.contains("nativa"), message);
	}

	private static void resourceHost(AppGeoCeDG app) {
		GeoNumeric radius = (GeoNumeric) eval(app, "ResourceRadius=1");
		eval(app, "ResourceCircle=Circle((0,0),ResourceRadius)"); eval(app, "ResourceP=Point(ResourceCircle)");
		radius.setDefinition((ExpressionNode) sum(app, 4096));
		assertEquals(UnsupportedReason.SOURCE_FIDELITY_RESOURCE_EXHAUSTED,
				NativeTransactionalState2D.unsupportedReason(app.getKernel().getConstruction()));
	}

	private static ExpressionValue sum(AppGeoCeDG app, int leaves) {
		if (leaves == 1) { return new MyDouble(app.getKernel(), 1.0 / 4096); }
		return new ExpressionNode(app.getKernel(), sum(app, leaves / 2), Operation.PLUS,
				sum(app, leaves - leaves / 2));
	}
}
