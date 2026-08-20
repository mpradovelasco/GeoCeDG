/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.geocedg.common.kernel.locus.LocusDualRunDiagnostic2D;
import org.geocedg.common.kernel.locus.LocusDualRunDiagnostic2D.SampleEvidence;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusV2Mode;
import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.kernel.commands.Commands;
import org.junit.jupiter.api.Test;

/** Exact F4 runtime-availability and profile-boundary scenarios. */
class G9U0RuntimeFeatureTest {

	@Test
	void f01OneRuntimeDecisionControlsCommandsAndCreation() {
		AppConfigGeoCeDG config = new AppConfigGeoCeDG(true);
		RuntimeFeatureService service = config.getRuntimeFeatureService();
		assertTrue(service.isLocusV2CreationEnabled());
		assertTrue(config.createCommandFilter().isCommandAllowed(Commands.LocusV2));
		assertTrue(RuntimeFeatureService.mayCreateLocusV2(
				AppCommonFactory.create(config).getKernel().getConstruction()));
	}

	@Test
	void f02DefaultProfileKeepsExperimentalCreationOff() {
		AppConfigGeoCeDG config = new AppConfigGeoCeDG();
		assertFalse(config.getRuntimeFeatureService()
				.isLocusV2CreationEnabled());
		assertFalse(config.createCommandFilter().isCommandAllowed(Commands.LocusV2));
		assertFalse(config.createCommandFilter()
				.isCommandAllowed(Commands.LocusLength));
		assertTrue(config.createCommandFilter().isCommandAllowed(Commands.Locus));
	}

	@Test
	void f03GeoCeDGOptInDoesNotEnableUpstreamClassic() {
		var enabled = AppCommonFactory.create(new AppConfigGeoCeDG(true));
		var classic = AppCommonFactory.create();
		assertTrue(RuntimeFeatureService.mayCreateLocusV2(
				enabled.getKernel().getConstruction()));
		assertFalse(RuntimeFeatureService.mayCreateLocusV2(
				classic.getKernel().getConstruction()));
	}

	@Test
	void f04LaboratoryAndDualRunRemainSeparateFeatures() throws IOException {
		String manifest = Files.readString(repositoryRoot().resolve(
				"geocedg/features/experimental.yml"), StandardCharsets.UTF_8);
		assertTrue(manifest.contains("\"id\": \"cedg.locus.v2\""));
		assertTrue(manifest.contains("\"id\": \"cedg.laboratory.legacy\""));
		assertFalse(manifest.contains("cedg.locus.v2.dual-run-enabled"));

		AppConfigGeoCeDG disabledConfig = new AppConfigGeoCeDG(false);
		assertEquals(LocusV2Mode.DUAL, LocusV2Mode.parse("dual"));
		assertFalse(disabledConfig.getRuntimeFeatureService()
				.isLocusV2CreationEnabled());

		var app = AppCommonFactory.create(new AppConfigGeoCeDG(true));
		LocusV2LaboratoryFixtures.State laboratory =
				LocusV2LaboratoryFixtures.create(
						app.getKernel().getConstruction());
		var locus = laboratory.getEntries().get(0).getLocus();
		String branchKey = locus.getSemanticDefinition().getBranches().get(0)
				.getBranchKey();
		double parameter = 0.25;
		var semantic = locus.evaluate(branchKey, parameter,
				org.geocedg.common.kernel.locus.LocusEvaluationSession2D
						.memoizing(1));
		long revision = locus.getSemanticRevision();
		List<LocusDualRunDiagnostic2D.Comparison> comparison =
				LocusDualRunDiagnostic2D.compare(locus::evaluate,
						List.of(new SampleEvidence(branchKey, parameter,
								new LocusPoint2D(semantic.getPoint().getX(),
										semantic.getPoint().getY()))), 1);
		assertEquals(1, comparison.size());
		assertTrue(comparison.get(0).isWithinEnvelope());
		assertEquals(0, comparison.get(0).getDistance(), 0);
		assertEquals(revision, locus.getSemanticRevision());
		assertTrue(app.getConfig() instanceof AppConfigGeoCeDG);
		assertTrue(((AppConfigGeoCeDG) app.getConfig()).getRuntimeFeatureService()
				.isLocusV2CreationEnabled());
	}

	private static Path repositoryRoot() {
		Path candidate = Path.of("").toAbsolutePath().normalize();
		while (candidate != null) {
			if (Files.isRegularFile(candidate.resolve("AGENTS.md"))) {
				return candidate;
			}
			candidate = candidate.getParent();
		}
		throw new IllegalStateException("GeoCeDG repository root not found");
	}
}
