/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.G9U1TestApp.lookup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geocedg.common.kernel.spatial.identity.ConstructionGeoRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineAssessment;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineAssessmentStatus;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineExecutionMode;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineImpactEntry;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineImpactReport;
import org.geocedg.desktop.GeoCeDGSpatialRedefineFrontend.ContractChoice;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Classic 5 rendering of the PRE-G9B-R3 typed redefine vocabulary. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9bR3RedefineFrontendTest {
	private static final String FIXTURE =
			"source/shared/common-jre/src/test/resources/org/geocedg/common/"
					+ "locus/g9u0-r2/locusFromMidpoint.cedg";
	private static final Pattern SPATIAL_GEO = Pattern.compile("<geo id=\"[^\"]+\"[^>]*/>");
	private static final String DETAIL = "Candidate changes the durable dependency "
			+ "frontier of the participant";

	@Test
	void everyUnavailableStatusShowsItsTextAndTheKernelReason() {
		RecordingPresentation presentation = new RecordingPresentation();
		GeoCeDGSpatialRedefineFrontend frontend =
				new GeoCeDGSpatialRedefineFrontend(presentation);
		for (SpatialRedefineAssessmentStatus status : EnumSet.of(
				SpatialRedefineAssessmentStatus.INVALID_DAG,
				SpatialRedefineAssessmentStatus.INCOMPATIBLE_HOST_REDEFINE,
				SpatialRedefineAssessmentStatus.UNSUPPORTED,
				SpatialRedefineAssessmentStatus.AMBIGUOUS,
				SpatialRedefineAssessmentStatus.STALE_ASSESSMENT,
				SpatialRedefineAssessmentStatus.DURABLE_CONTRACT_CHANGE,
				SpatialRedefineAssessmentStatus.UNDESCRIBABLE_PROPOSAL)) {
			SpatialRedefineAssessment assessment = assessment(status, "reason " + status,
					false, false, null);
			assertNull(frontend.selectExecutionMode(assessment));
			assertEquals(assessment, presentation.unavailable.get(
					presentation.unavailable.size() - 1));
			for (String language : List.of("en", "es")) {
				String statusText = GeoCeDGProfile.getText("Redefine.Status."
						+ status.name(), language);
				assertFalse(statusText.isBlank(), status + " " + language);
				assertEquals(statusText + "\n\n"
						+ GeoCeDGProfile.getText("Redefine.Detail", language) + ": reason "
						+ status, GeoCeDGSpatialRedefineFrontend.buildUnavailableMessage(
								assessment, language));
			}
			assertEquals(GeoCeDGProfile.getText("Redefine.Status." + status.name(), "en"),
					GeoCeDGSpatialRedefineFrontend.buildUnavailableMessage(
							assessment(status, null, false, false, null), "en"));
		}
		assertEquals("Kernel reason", GeoCeDGProfile.getText("Redefine.Detail", "en"));
		assertEquals("Motivo del núcleo", GeoCeDGProfile.getText("Redefine.Detail", "es"));
		assertTrue(presentation.contractPrompts.isEmpty());
		assertTrue(presentation.confirmations.isEmpty());
	}

	@Test
	void onlyTheKernelAuthorizedContractOperationsAreOfferedAndMapped() {
		SpatialRedefineImpactReport complete = impact(
				SpatialRedefineImpactReport.Completeness.IMPACT_COMPLETE);
		SpatialRedefineImpactReport incomplete = impact(
				SpatialRedefineImpactReport.Completeness.IMPACT_NOT_ESTABLISHED);
		assertChoices(List.of(ContractChoice.RETAIN_IDENTITY,
				ContractChoice.EXPLICIT_REPLACEMENT), true, true, complete);
		assertChoices(List.of(ContractChoice.RETAIN_IDENTITY), true, false, complete);
		assertChoices(List.of(ContractChoice.EXPLICIT_REPLACEMENT), false, true,
				complete);
		assertChoices(List.of(ContractChoice.RETAIN_IDENTITY), true, true, incomplete);
		assertChoices(List.of(), false, true, incomplete);
		assertChoices(List.of(), false, false, null);

		// A status other than a durable-contract change is never upgraded.
		for (SpatialRedefineAssessmentStatus status
				: SpatialRedefineAssessmentStatus.values()) {
			if (status != SpatialRedefineAssessmentStatus.DURABLE_CONTRACT_CHANGE) {
				assertTrue(GeoCeDGSpatialRedefineFrontend.availableContractChoices(
						assessment(status, DETAIL, true, true, complete)).isEmpty());
			}
		}
	}

	@Test
	void witnessRedefineRetainsWithoutAnyDialog() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		for (String command : new String[] {"A=(0,0)", "B=(2,0)", "c=Circle(A,B)",
				"C=Point(c)", "f=Line(C,xAxis)", "D=Intersect(f,yAxis)", "E=Midpoint(D,C)",
				"a=LocusV2(E,C)"}) {
			eval(app, command);
		}
		RecordingPresentation presentation = install(app);
		PersistentGeoId id = id(app, "E");

		Submission result = submit(app, "E=Midpoint(D,C+(1,0))");

		assertNotNull(result.output, result.errors.getErrors());
		assertEquals(id, id(app, "E"));
		assertTrue(presentation.unavailable.isEmpty());
		assertTrue(presentation.confirmations.isEmpty());
		assertTrue(presentation.contractPrompts.isEmpty());
		assertTrue(lookup(app, "a").isDefined());
	}

	@Test
	void aCancelledContractChangeShowsTheReasonAndTheCompleteImpact() throws Exception {
		AppGeoCeDG app = contractApp();
		RecordingPresentation presentation = install(app);
		String xml = app.getXML();

		Submission result = submit(app, "s=a+1");

		assertNull(result.output);
		assertEquals("", result.errors.getErrors());
		assertEquals(xml, app.getXML());
		assertEquals(1, presentation.contractPrompts.size());
		SpatialRedefineAssessment assessment = presentation.contractPrompts.get(0);
		assertEquals(SpatialRedefineAssessmentStatus.DURABLE_CONTRACT_CHANGE,
				assessment.getStatus());
		assertEquals(List.of(ContractChoice.RETAIN_IDENTITY,
				ContractChoice.EXPLICIT_REPLACEMENT), presentation.offered.get(0));
		for (String language : List.of("en", "es")) {
			String message = GeoCeDGSpatialRedefineFrontend.buildContractChangeMessage(
					assessment, language);
			for (String key : List.of("Redefine.Contract.Summary",
					"Redefine.Contract.RetainExplanation",
					"Redefine.Contract.ReplaceExplanation", "Redefine.Legacy.Undo")) {
				assertTrue(message.contains(GeoCeDGProfile.getText(key, language)),
						key + " " + language);
			}
			assertTrue(message.contains(GeoCeDGProfile.getText("Redefine.Detail",
					language) + ": " + DETAIL), language);
			for (SpatialRedefineImpactEntry entry
					: assessment.getImpactReport().getEntries()) {
				assertTrue(message.contains(entry.getParticipantId().toExternalForm()));
			}
		}
		assertTrue(presentation.unavailable.isEmpty());
	}

	@Test
	void keepIdentityExecutesTheCertifiedContractUpdate() throws Exception {
		AppGeoCeDG app = contractApp();
		RecordingPresentation presentation = install(app);
		presentation.choice = ContractChoice.RETAIN_IDENTITY;
		PersistentGeoId id = id(app, "s");

		Submission result = submit(app, "s=a+1");

		assertNotNull(result.output, result.errors.getErrors());
		assertEquals(id, id(app, "s"));
		GeoIdentityRecord record = registry(app).getGeoRecord(id);
		assertEquals(ConstructionGeoRedefineProvider.SCHEMA_VERSION_TRANSITIVE_FRONTIER,
				record.getSchemaVersion());
		assertEquals(List.of(id(app, "a")), record.getDependencies());
		assertEquals(1, presentation.contractPrompts.size());
	}

	@Test
	void explicitReplacementCreatesAFreshIdentity() throws Exception {
		AppGeoCeDG app = contractApp();
		RecordingPresentation presentation = install(app);
		presentation.choice = ContractChoice.EXPLICIT_REPLACEMENT;
		PersistentGeoId id = id(app, "s");

		Submission result = submit(app, "s=a+1");

		assertNotNull(result.output, result.errors.getErrors());
		assertNotEquals(id, id(app, "s"));
		assertNull(registry(app).getGeo(id));
	}

	@Test
	void nativeArchivesKeepVersionOneVersionTwoAndMixedRecords(
			@TempDir Path directory) throws Exception {
		AppGeoCeDG historical = G9U1TestApp.create();
		assertTrue(historical.loadFile(findRepositoryRoot().resolve(FIXTURE).toFile(),
				false));
		List<String> fixtureRecords = spatialGeoRecords(historical.getXML());
		assertTrue(fixtureRecords.stream().allMatch(
				record -> record.contains(" schemaVersion=\"1\"")));
		assertEquals(fixtureRecords, spatialGeoRecords(roundTrip(historical,
				directory.resolve("historical.cedg"))));

		install(historical);
		assertNotNull(submit(historical, "E=Midpoint(D,C+(1,0))").output);
		String mixed = historical.getXML();
		List<String> mixedRecords = spatialGeoRecords(mixed);
		assertEquals(1, mixedRecords.stream().filter(
				record -> record.contains(" schemaVersion=\"2\"")).count());
		assertEquals(mixedRecords, spatialGeoRecords(roundTrip(historical,
				directory.resolve("mixed.cedg"))));

		AppGeoCeDG current = G9U1TestApp.create();
		for (String command : new String[] {"A=(0,0)", "B=(2,0)", "c=Circle(A,B)",
				"C=Point(c)", "f=Line(C,xAxis)", "D=Intersect(f,yAxis)", "E=Midpoint(D,C)",
				"a=LocusV2(E,C)"}) {
			eval(current, command);
		}
		List<String> currentRecords = spatialGeoRecords(current.getXML());
		assertTrue(currentRecords.stream().allMatch(
				record -> record.contains(" schemaVersion=\"2\"")));
		assertEquals(currentRecords, spatialGeoRecords(roundTrip(current,
				directory.resolve("current.cedg"))));
	}

	private static void assertChoices(List<ContractChoice> expected,
			boolean identityPreserving, boolean replacement,
			SpatialRedefineImpactReport impact) {
		SpatialRedefineAssessment assessment = assessment(
				SpatialRedefineAssessmentStatus.DURABLE_CONTRACT_CHANGE, DETAIL,
				identityPreserving, replacement, impact);
		assertEquals(expected,
				GeoCeDGSpatialRedefineFrontend.availableContractChoices(assessment));
		for (ContractChoice choice : ContractChoice.values()) {
			RecordingPresentation presentation = new RecordingPresentation();
			presentation.choice = choice;
			SpatialRedefineExecutionMode mode = new GeoCeDGSpatialRedefineFrontend(
					presentation).selectExecutionMode(assessment);
			if (expected.isEmpty()) {
				assertNull(mode);
				assertTrue(presentation.contractPrompts.isEmpty());
				assertEquals(List.of(assessment), presentation.unavailable);
			} else if (!expected.contains(choice)) {
				assertNull(mode, choice.name());
			} else {
				assertEquals(choice == ContractChoice.RETAIN_IDENTITY
						? SpatialRedefineExecutionMode.IDENTITY_PRESERVING_CONTRACT_UPDATE
						: SpatialRedefineExecutionMode.LEGACY_REPLACEMENT, mode);
			}
		}
	}

	private static SpatialRedefineAssessment assessment(
			SpatialRedefineAssessmentStatus status, String detail,
			boolean identityPreserving, boolean replacement,
			SpatialRedefineImpactReport impact) {
		SpatialRedefineAssessment assessment = mock(SpatialRedefineAssessment.class);
		when(assessment.getStatus()).thenReturn(status);
		when(assessment.getDetail()).thenReturn(detail);
		when(assessment.isIdentityPreservingUpdateAvailable())
				.thenReturn(identityPreserving);
		when(assessment.isExplicitReplacementAvailable()).thenReturn(replacement);
		when(assessment.getImpactReport()).thenReturn(impact);
		return assessment;
	}

	private static SpatialRedefineImpactReport impact(
			SpatialRedefineImpactReport.Completeness completeness) {
		SpatialRedefineImpactReport report = mock(SpatialRedefineImpactReport.class);
		when(report.getCompleteness()).thenReturn(completeness);
		when(report.getEntries()).thenReturn(List.of());
		return report;
	}

	/** Two loci whose independent drivers {@code s} and {@code a} are durable. */
	private static AppGeoCeDG contractApp() {
		AppGeoCeDG app = G9U1TestApp.create();
		for (String command : new String[] {"s=0", "Q=(s,0)",
				"D={false,{-2,2,true,true}}", "L=LocusV2(Q,s,D)", "a=2", "Qa=(a,a^2)",
				"Da={false,{-2,2,true,true}}", "La=LocusV2(Qa,a,Da)"}) {
			eval(app, command);
		}
		assertNotNull(registry(app).getPersistentGeoId(lookup(app, "a")));
		return app;
	}

	private static String roundTrip(AppGeoCeDG app, Path file) throws Exception {
		assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file.toFile()));
		assertTrue(Files.size(file) > 0);
		AppGeoCeDG reopened = G9U1TestApp.create();
		assertTrue(reopened.loadFile(file.toFile(), false));
		return reopened.getXML();
	}

	private static List<String> spatialGeoRecords(String xml) {
		int start = xml.indexOf("<geocedgSpatial");
		int end = xml.indexOf("</geocedgSpatial>");
		assertTrue(start >= 0 && end > start);
		ArrayList<String> records = new ArrayList<>();
		Matcher matcher = SPATIAL_GEO.matcher(xml.substring(start, end));
		while (matcher.find()) {
			records.add(matcher.group());
		}
		return records;
	}

	private static RecordingPresentation install(AppGeoCeDG app) {
		RecordingPresentation presentation = new RecordingPresentation();
		app.setSpatialRedefinePresentation(presentation);
		return presentation;
	}

	private static SpatialIdentityRegistry registry(AppGeoCeDG app) {
		return app.getKernel().getConstruction().getSpatialIdentityRegistry();
	}

	private static PersistentGeoId id(AppGeoCeDG app, String label) {
		GeoElement geo = lookup(app, label);
		PersistentGeoId result = registry(app).getPersistentGeoId(geo);
		assertNotNull(result, label);
		return result;
	}

	private static Submission submit(AppGeoCeDG app, String command) throws Exception {
		AtomicReference<GeoElementND[]> output = new AtomicReference<>();
		ErrorAccumulator errors = new ErrorAccumulator();
		GeoCeDGAlgebraInputSubmission.submit(app, command,
				new EvalInfo(true, true).withSliders(true).withSymbolic(true), errors,
				output::set);
		return new Submission(output.get(), errors);
	}

	private static Path findRepositoryRoot() {
		Path candidate = Path.of("").toAbsolutePath().normalize();
		while (candidate != null) {
			if (Files.isRegularFile(candidate.resolve("AGENTS.md"))
					&& Files.isDirectory(candidate.resolve("geocedg"))) {
				return candidate;
			}
			candidate = candidate.getParent();
		}
		throw new IllegalStateException("GeoCeDG repository root not found");
	}

	private record Submission(GeoElementND[] output, ErrorAccumulator errors) {
	}

	private static final class RecordingPresentation
			implements GeoCeDGSpatialRedefineFrontend.Presentation {
		private final List<SpatialRedefineAssessment> confirmations = new ArrayList<>();
		private final List<SpatialRedefineAssessment> unavailable = new ArrayList<>();
		private final List<SpatialRedefineAssessment> contractPrompts = new ArrayList<>();
		private final List<List<ContractChoice>> offered = new ArrayList<>();
		private ContractChoice choice = ContractChoice.CANCEL;

		@Override
		public boolean confirmLegacy(SpatialRedefineAssessment assessment) {
			confirmations.add(assessment);
			return false;
		}

		@Override
		public void showUnavailable(SpatialRedefineAssessment assessment) {
			unavailable.add(assessment);
		}

		@Override
		public void showStaleAssessment() {
			// Not exercised by these fixtures.
		}

		@Override
		public ContractChoice chooseContractChange(
				SpatialRedefineAssessment assessment) {
			contractPrompts.add(assessment);
			offered.add(GeoCeDGSpatialRedefineFrontend.availableContractChoices(
					assessment));
			return choice;
		}
	}
}
