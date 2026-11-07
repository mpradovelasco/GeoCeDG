/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.geocedg.desktop.GeoCeDGInspectionResult.AdmissionState;
import org.geocedg.desktop.GeoCeDGInspectionResult.Basis;
import org.geocedg.desktop.GeoCeDGInspectionResult.Phase;
import org.geocedg.desktop.GeoCeDGInspectionResult.Property;
import org.geocedg.desktop.GeoCeDGInspectionResult.Status;
import org.geogebra.common.main.App;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Rehearses the production expert policy against archives emitted by Desktop. */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class GeoCeDGExpertTrialTest {
	@TempDir
	Path directory;

	@Test
	void rehearsesCircularArcAndDisclosesLayerChangeWithActualReceiverContext() throws Exception {
		onEdt(() -> {
			var source = model("O=(0,0)", "A=(2,0)", "B=(0,3)", "a=CircularArc(O,A,B)", "u=0.5");
			var receiver = model("A=(7,7)");
			receiver.getKernel().lookupLabel("A").setLayer(4);
			receiver.getLayerWorkspace().setWorkingLayer(2);
			receiver.setUnsaved();
			Trial result = rehearse("arc", source, receiver);
			assertTrue(result.expert().available(), result::summary);
			assertEquals(AdmissionState.REJECTED, result.report().admission().state());
			var sourceScalar = stage(result, Phase.SOURCE).objects().stream()
					.filter(object -> "u".equals(object.label())).findFirst().orElseThrow();
			var copiedScalar = comparison(result, Phase.SOURCE, Phase.TRIAL).objects().stream()
					.filter(delta -> delta.beforeRef().equals(sourceScalar.ref())).findFirst().orElseThrow();
			var presentation = copiedScalar.observations().stream()
					.filter(row -> row.property() == Property.PRESENTATION_FIELDS).findFirst().orElseThrow();
			assertEquals(Status.VERIFIED_CHANGED, presentation.status());
			assertTrue(presentation.before().contains("layer=0"), presentation.before());
			assertTrue(presentation.after().contains("layer=4"), presentation.after());
			assertEquals(Basis.NATIVE_ARCHIVE_DECLARATION,
					comparison(result, Phase.TRIAL, Phase.REOPENED).basis());
			assertEquals(5, comparison(result, Phase.RECEIVER, Phase.TRIAL).added().size());
			assertNull(receiver.getKernel().lookupLabel("a"));
			assertEquals(2, receiver.getLayerWorkspace().getWorkingLayer());
			assertFalse(receiver.isSaved());
			return null;
		});
	}

	@Test
	void nativeCurveSequenceAndTextRehearseWithUncertaintyDisclosed() throws Exception {
		onEdt(() -> {
			var receiver = model("R=(7,7)");
			for (String[] item : List.of(new String[]{"curve", "c=Curve(t,t^2,t,0,2)"},
					new String[]{"sequence", "n=2", "s=Sequence(k*n,k,1,4)"},
					new String[]{"text", "n=2", "t=Text(n)"})) {
				Trial result = rehearse(item[0], model(java.util.Arrays.copyOfRange(item, 1, item.length)),
						receiver);
				assertTrue(result.expert().available(), () -> item[0] + "\n" + result.summary());
				var copied = comparison(result, Phase.SOURCE, Phase.TRIAL);
				assertTrue(copied.assessments().stream().anyMatch(row ->
						row.dimension() == GeoCeDGInspectionResult.Dimension.GEOMETRIC_EQUIVALENCE
								&& row.status() == Status.NOT_ESTABLISHED));
				assertEquals(item.length - 1, comparison(result, Phase.RECEIVER, Phase.TRIAL).added().size());
			}
			return null;
		});
	}

	@Test
	void disclosesViewMembershipChangeMadeByNativeCopy() throws Exception {
		onEdt(() -> {
			var source = model("B=(2,1)");
			var point = source.getKernel().lookupLabel("B");
			point.addView(App.VIEW_EUCLIDIAN2);
			point.removeView(App.VIEW_EUCLIDIAN);
			Trial result = rehearse("view", source, model("A=(0,0)"));
			assertTrue(result.expert().available(), result::summary);
			var presentation = comparison(result, Phase.SOURCE, Phase.TRIAL).objects().get(0)
					.observations().stream().filter(row -> row.property() == Property.PRESENTATION_FIELDS)
					.findFirst().orElseThrow();
			assertEquals(Status.VERIFIED_CHANGED, presentation.status());
			assertTrue(presentation.before().contains("view1=false,view2=true"), presentation.before());
			assertTrue(presentation.after().contains("view1=true,view2=true"), presentation.after());
			return null;
		});
	}

	@Test
	void unusableArcTerminalDirectionDoesNotOfferPublication() throws Exception {
		onEdt(() -> {
			var source = model("O=(0,0)", "A=(2,0)", "B=(0,0)", "a=CircularArc(O,A,B)");
			var receiver = model("R=(7,7)");
			Trial result = rehearse("bad-arc", source, receiver);
			assertFalse(result.expert().available(), result::summary);
			assertEquals("NATIVE_EXPERT_COPY", result.expert().reason(), result::summary);
			assertTrue(result.report().diagnostics().stream().anyMatch(diagnostic ->
					diagnostic.detail().contains("Circular arc support")), result::summary);
			return null;
		});
	}

	private Trial rehearse(String name, AppGeoCeDG source, AppGeoCeDG receiver) throws Exception {
		Path input = directory.resolve(name + "-source.ggb");
		Path staged = directory.resolve(name + "-receiver.cedg");
		Path context = directory.resolve(name + "-context");
		Files.write(input, GeoCeDGExpertIntegrity.nativeArchive(source));
		try (var live = GeoCeDGLiveInspectionSnapshot.capture(receiver)) {
			live.stageReceiver(staged);
			live.stageLayerContext(context);
			var worker = new GeoCeDGExpertInspection();
			var report = worker.inspectExpert(input, staged, GeoCeDGExpertContext.read(context), null);
			assertTrue(live.matchesCurrentState(receiver));
			return new Trial(report, worker.expertTrial());
		}
	}

	private static AppGeoCeDG model(String... commands) {
		AppGeoCeDG app = G9U1TestApp.create(false);
		for (String command : commands) {
			assertNotNull(must(app, command));
		}
		return app;
	}

	private static GeoCeDGInspectionResult.Stage stage(Trial result, Phase phase) {
		return result.report().stages().stream().filter(row -> row.phase() == phase)
				.findFirst().orElseThrow();
	}

	private static GeoCeDGInspectionResult.Comparison comparison(Trial result, Phase from, Phase to) {
		return result.report().comparisons().stream().filter(row -> row.before() == from && row.after() == to)
				.findFirst().orElseThrow();
	}

	private record Trial(GeoCeDGInspectionResult report, GeoCeDGExpertInspection.ExpertTrial expert) {
		String summary() {
			return GeoCeDGInspectionReport.summary(report);
		}
	}
}
