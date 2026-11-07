/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.geocedg.common.kernel.spatial.identity.ConstructionGeoRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.EditAuthorityMode;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRole;
import org.geocedg.desktop.GeoCeDGInspectionResult.AdmissionState;
import org.geocedg.desktop.GeoCeDGInspectionResult.Basis;
import org.geocedg.desktop.GeoCeDGInspectionResult.ObservedObject;
import org.geocedg.desktop.GeoCeDGInspectionResult.Phase;
import org.geocedg.desktop.GeoCeDGInspectionResult.Property;
import org.geocedg.desktop.GeoCeDGInspectionResult.Stage;
import org.geocedg.desktop.GeoCeDGInspectionResult.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Native Desktop fixtures and deliberately adversarial differential evidence. */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class GeoCeDGExpertInspectionTest {
	@TempDir
	Path directory;

	@Test
	void nativeTrialUsesCopyWitnessesAndReopenUsesDurableIdentity() throws Exception {
		AppGeoCeDG source = onEdt(() -> model("A=(0,0)", "B=(1,0)", "s=Segment(A,B)"));
		AppGeoCeDG receiver = onEdt(() -> model("A=(4,4)", "R=(1,1)"));
		String receiverId = onEdt(() -> {
			var point = receiver.getKernel().lookupLabel("R");
			var registry = point.getConstruction().getSpatialIdentityRegistry();
			var id = registry.allocatePersistentGeoId();
			registry.registerParticipation(point, new GeoIdentityRecord(id,
					ConstructionGeoRedefineProvider.PROVIDER_ID, "POINT",
					ConstructionGeoRedefineProvider.SCHEMA_ID, ConstructionGeoRedefineProvider.SCHEMA_VERSION,
					EditAuthorityMode.CONSTRUCTION_DEFINED, ProjectionBindingRole.NOT_APPLICABLE,
					"VALUE", 1, 0, 0));
			return id.toExternalForm();
		});
		Path sourceFile = onEdt(() -> write(source, "elementary"));
		Path receiverFile = onEdt(() -> write(receiver, "receiver"));
		byte[] original = Files.readAllBytes(receiverFile);
		var liveBefore = onEdt(() -> GeoCeDGDocumentInsertion.Certificate.of(receiver));
		var history = onEdt(() -> GeoCeDGDocumentInsertion.HistoryRecord.of(
				PreG9BR6PlusF3Fixtures.undo(receiver)));
		var report = inspect(sourceFile, receiverFile);
		assertEquals(AdmissionState.ADMITTED, report.admission().state(),
				() -> GeoCeDGInspectionReport.summary(report));
		assertEquals(3, comparison(report, Phase.RECEIVER, Phase.TRIAL).added().size(),
				() -> GeoCeDGInspectionReport.summary(report));
		var segment = object(stage(report, Phase.SOURCE), "s");
		var copied = comparison(report, Phase.SOURCE, Phase.TRIAL);
		assertEquals(Basis.NATIVE_COPY_TRACE, copied.basis());
		assertEquals(Status.VERIFIED_UNCHANGED, property(copied, segment.ref(), Property.INPUTS));
		assertEquals(Status.NOT_ESTABLISHED, property(copied, segment.ref(), Property.CURRENT_VALUE));
		var reopened = comparison(report, Phase.TRIAL, Phase.REOPENED);
		var durable = object(stage(report, Phase.TRIAL), "R");
		assertEquals(receiverId, durable.identity());
		assertEquals(Status.VERIFIED_UNCHANGED, property(reopened, durable.ref(), Property.DURABLE_ID),
				() -> GeoCeDGInspectionReport.summary(report));
		assertEquals(Status.NOT_ESTABLISHED, property(reopened,
				object(stage(report, Phase.TRIAL), "A").ref(), Property.PRESENCE));
		assertTrue(GeoCeDGInspectionReport.json(report).contains("\"publication\""));
		assertArrayEquals(original, Files.readAllBytes(receiverFile));
		assertEquals(List.of(), liveBefore.serializableDifferences(
				onEdt(() -> GeoCeDGDocumentInsertion.Certificate.of(receiver))));
		assertNull(onEdt(() -> history.mismatch(PreG9BR6PlusF3Fixtures.undo(receiver), null)));
		var repeated = inspect(sourceFile, receiverFile);
		assertEquals(report.comparisons(), repeated.comparisons());
		assertEquals(report.diagnostics(), repeated.diagnostics());
	}

	@Test
	void nativeFamiliesRemainInspectableWhenAdmissionRefuses() throws Exception {
		var receiver = onEdt(() -> write(model("R=(7,7)"), "receiver"));
		var cases = List.of(
				new String[]{"arc", "a", "AlgoConicPartCircle",
						"O=(0,0)", "A=(1,0)", "B=(0,1)", "a=CircularArc(O,A,B)"},
				new String[]{"curve", "c", "AlgoCurveCartesian", "c=Curve(t,t^2,t,0,2)"},
				new String[]{"sequence", "l", "AlgoSequence", "l=Sequence(k^2,k,1,4)"},
				new String[]{"text", "t", "AlgoText", "n=2", "t=Text(n)"});
		for (String[] entry : cases) {
			var path = onEdt(() -> write(model(java.util.Arrays.copyOfRange(entry, 3, entry.length)),
					entry[0]));
			var report = inspect(path, receiver);
			Stage loaded = stage(report, Phase.SOURCE);
			assertTrue(loaded.angleUnit() != null, () -> GeoCeDGInspectionReport.summary(report));
			var object = object(loaded, entry[1]);
			assertTrue(object.producer().endsWith(entry[2]), object.producer());
			assertFalse(object.inputs().isEmpty());
			if (entry[0].equals("arc") || entry[0].equals("curve")) {
				assertEquals(AdmissionState.REJECTED, report.admission().state());
				assertEquals(Status.NOT_EVALUATED, comparison(report, Phase.SOURCE, Phase.TRIAL)
						.status());
			}
		}
	}

	@Test
	void macrosScriptsAndInvalidArchiveStayOutsideNativeLoader() throws Exception {
		var receiver = onEdt(() -> write(model("R=(7,7)"), "receiver"));
		String header = "<geogebra format=\"5.0\"><construction>";
		String tail = "</construction></geogebra>";
		var scripted = zip("scripted", Map.of("geogebra.xml", header
				+ "<element type=\"point\" label=\"P\"><ggbscript onClick=\"Delete[R]\"/>"
				+ "</element>" + tail));
		var macro = zip("macro", Map.of("geogebra.xml", header + tail,
				"geogebra_macro.xml", "<geogebra format=\"5.0\"><macro cmdName=\"Tool\"/></geogebra>"));
		Path invalid = directory.resolve("invalid.ggb");
		Files.writeString(invalid, "not a ZIP");
		for (var entry : Map.of(scripted, "SCRIPT", macro, "MACRO", invalid, "INVALID_ARCHIVE")
				.entrySet()) {
			var report = inspect(entry.getKey(), receiver);
			assertTrue(report.diagnostics().stream()
					.anyMatch(diagnostic -> diagnostic.code().equals(entry.getValue())),
					() -> GeoCeDGInspectionReport.summary(report));
			assertTrue(stage(report, Phase.SOURCE).objects().isEmpty());
			assertEquals(AdmissionState.NOT_EVALUATED, report.admission().state());
			assertEquals(Status.NOT_EVALUATED, comparison(report, Phase.SOURCE, Phase.TRIAL).status());
		}
	}

	@Test
	void dependenciesUseTheSuppliedCorrespondenceAndRetainUncertainty() {
		Stage from = new Stage(Phase.SOURCE, "s", "d", 0, List.of(), List.of(
				fact("a", "A", "1", List.of()), fact("b", "B", "2", List.of()),
				fact("c", "C", "3", List.of("a"))));
		Stage to = new Stage(Phase.REOPENED, "t", "d", 0, List.of(), List.of(
				fact("y", "A", "2", List.of()), fact("x", "B", "1", List.of()),
				fact("z", "C", "3", List.of("y"))));
		var durable = GeoCeDGExpertInspection.compareDurable(from, to);
		assertEquals(Status.VERIFIED_CHANGED, property(durable, "c", Property.INPUTS));
		assertEquals(Status.VERIFIED_CHANGED, property(durable, "a", Property.LABEL));
		var incomplete = GeoCeDGInspectionResult.compare(from, to, Basis.NATIVE_COPY_TRACE,
				Map.of("c", "z"), Set.of("b"));
		assertEquals(Status.NOT_ESTABLISHED, property(incomplete, "c", Property.INPUTS));
		assertEquals(Status.NOT_ESTABLISHED, property(incomplete, "a", Property.PRESENCE));
		assertEquals(Status.KNOWN_OMISSION, property(incomplete, "b", Property.PRESENCE));
		Stage replacement = new Stage(Phase.TRIAL, null, "d", 0, List.of(),
				List.of(fact("q", "A", null, List.of()), fact("b", "B", "2", List.of())));
		var instance = GeoCeDGInspectionResult.compare(from, replacement, Basis.LIVE_INSTANCE,
				Map.of("a", "q"), Set.of());
		assertEquals(Status.VERIFIED_CHANGED, property(instance, "a", Property.DURABLE_ID));
		assertEquals(Status.VERIFIED_CHANGED, property(instance, "b", Property.RUNTIME_INSTANCE));
		assertEquals(Status.NOT_ESTABLISHED, property(instance, "b", Property.PRESENCE));
		assertTrue(instance.added().isEmpty());
		var inventoryOnly = new Stage(Phase.REOPENED, "t", "d", null, List.of(), List.of());
		assertEquals(Status.NOT_EVALUATED,
				GeoCeDGExpertInspection.compareDurable(from, inventoryOnly).status());
	}

	private static ObservedObject fact(String ref, String label, String id, List<String> input) {
		return new ObservedObject(ref, label, id, null, "point", "algo", "ELEMENTARY", input,
				List.of(ref), "", true, null, "");
	}

	private AppGeoCeDG model(String... commands) {
		AppGeoCeDG app = G9U1TestApp.create(false);
		for (String command : commands) {
			assertNotNull(must(app, command));
		}
		return app;
	}

	private Path write(AppGeoCeDG app, String name) throws IOException {
		Path path = directory.resolve(name + ".cedg");
		app.getXMLio().writeGeoGebraFile(Files.newOutputStream(path), false);
		return path;
	}

	private Path zip(String name, Map<String, String> entries) throws IOException {
		Path path = directory.resolve(name + ".ggb");
		try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(path))) {
			for (var entry : entries.entrySet()) {
				zip.putNextEntry(new ZipEntry(entry.getKey()));
				zip.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
				zip.closeEntry();
			}
		}
		return path;
	}

	private static GeoCeDGInspectionResult inspect(Path source, Path receiver) throws Exception {
		return onEdt(() -> new GeoCeDGExpertInspection().inspect(source, receiver));
	}

	private static Stage stage(GeoCeDGInspectionResult report, Phase phase) {
		return report.stages().stream().filter(stage -> stage.phase() == phase).findFirst()
				.orElseThrow(() -> new AssertionError(GeoCeDGInspectionReport.summary(report)));
	}

	private static ObservedObject object(Stage stage, String label) {
		return stage.objects().stream().filter(object -> label.equals(object.label()))
				.findFirst().orElseThrow();
	}

	private static GeoCeDGInspectionResult.Comparison comparison(GeoCeDGInspectionResult report,
			Phase before, Phase after) {
		return report.comparisons().stream().filter(row -> row.before() == before
				&& row.after() == after).findFirst().orElseThrow();
	}

	private static Status property(GeoCeDGInspectionResult.Comparison row, String ref,
			Property property) {
		return row.objects().stream().filter(delta -> ref.equals(delta.beforeRef()))
				.flatMap(delta -> delta.observations().stream())
				.filter(observation -> observation.property() == property)
				.findFirst().orElseThrow().status();
	}
}
