/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import java.awt.Component;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionQuery2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResolver2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResult2D.Reason;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionWorkBudget2D;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Corrective authoring consumers; geometric admission remains kernel-owned. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class Cu7M08SCorrectionTest {

	@BeforeAll
	static void initialize() {
		G9U1SemanticPointInteractionTest.initializeDesktop();
	}

	@Test
	void creationAndMovementHaveDifferentLocalizedMessages() {
		AppGeoCeDG app = G9U1SemanticPointInteractionTest.app();
		GeoLocusV2 source = G9U1SemanticPointInteractionTest.spline(app);
		var result = new LocusPointInteractionResolver2D().resolve(
				new LocusPointInteractionQuery2D(source, 20, 20,
						LocusPointInteractionPolicy2D.initial(0.1)));
		assertEquals(Reason.CERTIFIED_ABSENCE, result.getReason());
		for (Locale locale : new Locale[] {Locale.ENGLISH, Locale.forLanguageTag("es")}) {
			app.setLanguage(locale);
			var dialogs = new GeoCeDGLocusV2Dialogs(app);
			String creation = dialogs.interactionMessage(result, true);
			assertTrue(creation.startsWith(GeoCeDGProfile.getText(
					"point.creationNotCreated", locale.getLanguage())));
			assertFalse(creation.equals(dialogs.interactionMessage(result, false)));
			assertFalse(dialogs.interactionMessage(result)
					.equals(dialogs.interactionMessage(result, false)));
			assertTrue(creation.contains(GeoCeDGProfile.getText(
					"point.reason.absent", locale.getLanguage())));
		}
	}

	@Test
	void budgetCauseDoesNotChangeUnresolvedAdmission() {
		AppGeoCeDG app = G9U1SemanticPointInteractionTest.app();
		GeoLocusV2 source = G9U1SemanticPointInteractionTest.spline(app);
		var result = new LocusPointInteractionResolver2D().resolve(
				new LocusPointInteractionQuery2D(source, 0, 0,
						new LocusPointInteractionPolicy2D(0.1, 1E-10, 1E-12,
								new LocusPointInteractionWorkBudget2D(1, 2, 1, 1))));
		assertEquals(Reason.BUDGET_EXHAUSTED, result.getReason());
		assertNull(result.getUniqueCandidate());
		assertTrue(new GeoCeDGLocusV2Dialogs(app).interactionMessage(result, true)
				.contains(GeoCeDGProfile.getText("point.reason.budget",
						app.getLocale().getLanguage())));
	}

	@Test
	void freeAndDependentReferencesAreConsumedAsValues() {
		AppGeoCeDG app = G9U1SemanticPointInteractionTest.app();
		GeoLocusV2 source = G9U1SemanticPointInteractionTest.spline(app);
		GeoPoint q = (GeoPoint) G9U1SemanticPointInteractionTest.eval(app, "Q=(0.2,0.01)");
		GeoPoint dependent = (GeoPoint) G9U1SemanticPointInteractionTest.eval(app,
				"R=Q+(0.3,0)");
		var registry = app.getKernel().getConstruction().getSpatialIdentityRegistry();
		assertNull(registry.getPersistentGeoId(q));
		GeoPoint p = from(app, source, q);
		GeoPoint r = from(app, source, dependent);
		assertNull(registry.getPersistentGeoId(q));
		assertNull(registry.getPersistentGeoId(dependent));
		assertNotNull(p);
		assertNotNull(r);
		assertFalse(p.getAllPredecessors().contains(q));
		assertFalse(r.getAllPredecessors().contains(q));
		assertFalse(r.getAllPredecessors().contains(dependent));
		q.setCoords(20, 20, 1);
		q.updateCascade();
		assertEquals(0.2, p.getInhomX(), 1E-8);
		assertEquals(0.5, r.getInhomX(), 1E-8);
		assertTrue(new GeoCeDGPointInteraction(app).move(p, 1, 0, 0.1));
		assertEquals(1, p.getInhomX(), 1E-8);
	}

	@Test
	void sourceChangesPropagateButReferenceChangesDoNot() {
		AppGeoCeDG app = G9U1SemanticPointInteractionTest.app();
		G9U1SemanticPointInteractionTest.spline(app);
		G9U1SemanticPointInteractionTest.eval(app, "k=1");
		GeoLocusV2 source = (GeoLocusV2) G9U1SemanticPointInteractionTest.eval(app,
				"T=Dilate(S,k,(0,0))");
		GeoPoint q = (GeoPoint) G9U1SemanticPointInteractionTest.eval(app, "Q=(0.5,0.01)");
		GeoPoint p = from(app, source, q);
		Object address = parent(p).getCurrentSemanticAddress();
		q.setCoords(5, 5, 1);
		q.updateCascade();
		assertEquals(0.5, p.getInhomX(), 1E-8);
		GeoNumeric k = (GeoNumeric) app.getKernel().lookupLabel("k");
		k.setValue(2);
		k.updateCascade();
		assertEquals(1, p.getInhomX(), 1E-8);
		k.setValue(0);
		k.updateCascade();
		k.setValue(1);
		k.updateCascade();
		assertEquals(address, parent(p).getCurrentSemanticAddress());
	}

	@Test
	void referenceAncestorOfSourceKeepsOnlyItsLegitimateInfluence() {
		AppGeoCeDG app = G9U1SemanticPointInteractionTest.app();
		G9U1SemanticPointInteractionTest.spline(app);
		GeoPoint q = (GeoPoint) G9U1SemanticPointInteractionTest.eval(app, "Q=(0,0)");
		GeoLocusV2 source = (GeoLocusV2) G9U1SemanticPointInteractionTest.eval(app,
				"T=Dilate(S,2,Q)");
		GeoPoint p = from(app, source, q);
		assertFalse(Arrays.asList(p.getParentAlgorithm().getInput()).contains(q));
		assertTrue(p.getAllPredecessors().contains(q));
		q.setCoords(1, 0, 1);
		q.updateCascade();
		assertEquals(-1, p.getInhomX(), 1E-8);
	}

	@Test
	void multipleChoiceCancelAndStaleSelectionDoNotPublish() {
		AppGeoCeDG app = G9U1SemanticPointInteractionTest.app();
		GeoLocusV2 source = (GeoLocusV2) G9U1SemanticPointInteractionTest.eval(app,
				"S=SplineV2({(-1,0),(0,1),(1,0),(0,1),(-1,0)},3)");
		GeoPoint q = (GeoPoint) G9U1SemanticPointInteractionTest.eval(app, "Q=(0,1)");
		var interaction = new GeoCeDGPointInteraction(app);
		String before = app.getXML();
		assertNull(interaction.createFromReference(source, q, policy(), result -> null));
		assertEquals(before, app.getXML());
		GeoPoint p = interaction.createFromReference(source, q, policy(), result -> {
			assertTrue(result.getCandidates().size() > 1);
			return result.getCandidates().get(1);
		});
		assertNotNull(p);
		String[] afterReload = {null};
		assertThrows(IllegalArgumentException.class, () -> interaction.createFromReference(
				source, q, policy(), result -> {
					app.setXML(app.getXML(), true);
					afterReload[0] = app.getXML();
					return result.getCandidates().get(0);
				}));
		assertEquals(afterReload[0], app.getXML());
		assertThrows(IllegalArgumentException.class, () ->
				org.geocedg.common.kernel.locus.interaction.LocusSnapshotPlacement2D
						.query(source, 0, 1, policy()));
		assertEquals(afterReload[0], app.getXML());
	}

	@Test
	void generalLocusFromQStillRejectsWithoutNewPolicy() {
		AppGeoCeDG app = G9U1SemanticPointInteractionTest.app();
		G9U1SemanticPointInteractionTest.eval(app, "I=(0.4965866445356883,1.7854818601338847)");
		G9U1SemanticPointInteractionTest.eval(app, "J=(-0.5082406249102692,2.5763181370126467)");
		G9U1SemanticPointInteractionTest.eval(app, "f=Circle(I,J)");
		G9U1SemanticPointInteractionTest.eval(app, "D=Point(f)");
		G9U1SemanticPointInteractionTest.eval(app, "g=Line(D,xAxis)");
		G9U1SemanticPointInteractionTest.eval(app, "K=Intersect(g,yAxis)");
		G9U1SemanticPointInteractionTest.eval(app, "M=Midpoint(K,D)");
		GeoLocusV2 h = (GeoLocusV2) G9U1SemanticPointInteractionTest.eval(app, "h=LocusV2(M,D)");
		GeoPoint q = (GeoPoint) G9U1SemanticPointInteractionTest.eval(app,
				"Q=(0.8976484384297626,1.7854818601338847)");
		String before = app.getXML();
		var interaction = new GeoCeDGPointInteraction(app);
		assertNull(interaction.createFromReference(h, q, policy(), result -> {
			throw new AssertionError("Unresolved candidate offered");
		}));
		assertEquals(before, app.getXML());
		assertEquals(Reason.GLOBAL_UNIQUENESS_NOT_ESTABLISHED,
				interaction.getLastResult().getReason());
		// The existing multiple-result permission must not be excluded by source family.
		GeoPoint center = (GeoPoint) G9U1SemanticPointInteractionTest.eval(app,
				"QC=(0.24829332226784415,1.8854818601338847)");
		GeoPoint selected = interaction.createFromReference(h, center,
				LocusPointInteractionPolicy2D.initial(1), result -> {
					assertEquals(Reason.MULTIPLE, result.getReason());
					assertFalse(result.getSearchCoverage().establishesCompleteRequestedScope());
					assertTrue(result.getCandidates().size() > 1);
					return result.getCandidates().get(0);
				});
		assertNotNull(selected);
		assertFalse(selected.getAllPredecessors().contains(center));
		var retained = parent(selected).getCurrentSemanticAddress();
		assertFalse(interaction.move(selected, selected.getInhomX() + 0.01,
				selected.getInhomY(), 0.1));
		assertEquals(Reason.GLOBAL_UNIQUENESS_NOT_ESTABLISHED,
				interaction.getLastResult().getReason());
		assertEquals(retained, parent(selected).getCurrentSemanticAddress());
	}

	@Test
	void referenceSnapshotNativeReopenAndClipboardUseForwardOnly(@TempDir Path directory) {
		AppGeoCeDG app = G9U1SemanticPointInteractionTest.app();
		GeoLocusV2 source = G9U1SemanticPointInteractionTest.spline(app);
		GeoPoint q = (GeoPoint) G9U1SemanticPointInteractionTest.eval(app, "Q=(0.5,0.01)");
		GeoPoint p = from(app, source, q);
		String label = p.getLabelSimple();
		var registry = app.getKernel().getConstruction().getSpatialIdentityRegistry();
		var id = registry.getPersistentGeoId(p);
		var address = parent(p).getCurrentSemanticAddress();
		Path file = directory.resolve("reference-snapshot.cedg");
		assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file.toFile()));
		AppGeoCeDG reopened = G9U1SemanticPointInteractionTest.app();
		assertTrue(reopened.loadFile(file.toFile(), false));
		GeoPoint restored = (GeoPoint) reopened.getKernel().lookupLabel(label);
		assertEquals(id, reopened.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getPersistentGeoId(restored));
		assertEquals(address, parent(restored).getCurrentSemanticAddress());
		assertFalse(restored.getAllPredecessors().contains(reopened.getKernel().lookupLabel("Q")));
		String clipboard = InternalClipboard.getTextToSave(app, List.of(p), text -> text);
		G9U1SemanticPointInteractionTest.paste(app, clipboard);
		var copyRecord = registry.getRecords().stream()
				.filter(GeoIdentityRecord.class::isInstance).map(GeoIdentityRecord.class::cast)
				.filter(record -> id.equals(record.getCopySourceId())).findFirst().orElseThrow();
		GeoPoint copy = (GeoPoint) registry.getGeo(copyRecord.getId());
		assertFalse(id.equals(copyRecord.getId()));
		assertFalse(copy.getAllPredecessors().contains(q));
	}

	@Test
	void explicitReferenceDialogCreatesOneUndoAndCancellationCreatesNone() throws Exception {
		AppGeoCeDG app = G9U1SemanticPointInteractionTest.app();
		GeoLocusV2 source = G9U1SemanticPointInteractionTest.spline(app);
		GeoPoint q = (GeoPoint) G9U1SemanticPointInteractionTest.eval(app, "Q=(0.3,0.02)");
		q.setEuclidianVisible(false);
		app.getKernel().initUndoInfo();
		var undo = app.getKernel().getConstruction().getUndoManager();
		await(() -> !undo.undoPossible());
		try (var dialogs = mockStatic(JOptionPane.class)) {
			dialogs.when(() -> JOptionPane.showConfirmDialog(any(Component.class), any(),
					anyString(), anyInt(), anyInt())).thenAnswer(call -> {
				JTabbedPane tabs = call.getArgument(1);
				tabs.setSelectedIndex(1);
				JPanel panel = (JPanel) tabs.getComponentAt(1);
				JComboBox<?> picker = Arrays.stream(panel.getComponents())
						.filter(JComboBox.class::isInstance).map(JComboBox.class::cast)
						.findFirst().orElseThrow();
				for (int i = 0; i < picker.getItemCount(); i++) {
					if (picker.getItemAt(i).toString().startsWith("Q ")) {
						picker.setSelectedIndex(i);
					}
				}
				return JOptionPane.OK_OPTION;
			});
			app.setMode(EuclidianConstants.MODE_LOCUS_V2_POINT);
			var controller = G9U1SemanticPointInteractionTest.controller(app);
			controller.wrapMousePressed(G9U1SemanticPointInteractionTest.event(app, 0.3, 0));
			controller.wrapMouseReleased(G9U1SemanticPointInteractionTest.event(app, 0.3, 0));
		}
		GeoPoint p = app.getKernel().getConstruction().getGeoSetConstructionOrder().stream()
				.filter(GeoPoint.class::isInstance).map(GeoPoint.class::cast)
				.filter(GeoCeDGPointInteraction::owns).findFirst().orElseThrow();
		assertEquals(0.3, p.getInhomX(), 1E-8);
		assertFalse(p.getAllPredecessors().contains(q));
		String label = p.getLabelSimple();
		await(undo::undoPossible);
		app.getKernel().undo();
		await(undo::redoPossible);
		assertNull(app.getKernel().lookupLabel(label));
		assertFalse(undo.undoPossible(), "More than one undo record for creation");
		app.getKernel().redo();
		await(() -> app.getKernel().lookupLabel(label) != null);
		String before = app.getXML();
		try (var dialogs = mockStatic(JOptionPane.class)) {
			dialogs.when(() -> JOptionPane.showConfirmDialog(any(Component.class), any(),
					anyString(), anyInt(), anyInt())).thenReturn(JOptionPane.CANCEL_OPTION);
			assertNull(new GeoCeDGLocusV2Dialogs(app).createSemanticPoint(
					(GeoLocusV2) app.getKernel().lookupLabel(source.getLabelSimple())));
		}
		assertEquals(before, app.getXML());
	}

	private static GeoPoint from(AppGeoCeDG app, GeoLocusV2 source, GeoPoint reference) {
		return new GeoCeDGPointInteraction(app).createFromReference(source, reference,
				policy(), result -> { throw new AssertionError("Expected unique query"); });
	}

	private static LocusPointInteractionPolicy2D policy() {
		return LocusPointInteractionPolicy2D.initial(0.1);
	}

	private static AlgoSemanticLocusPoint2D parent(GeoPoint point) {
		return (AlgoSemanticLocusPoint2D) point.getParentAlgorithm();
	}

	private static void await(BooleanSupplier condition) throws Exception {
		long deadline = System.nanoTime() + 5_000_000_000L;
		while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
			Thread.sleep(10);
		}
		assertTrue(condition.getAsBoolean());
	}
}
