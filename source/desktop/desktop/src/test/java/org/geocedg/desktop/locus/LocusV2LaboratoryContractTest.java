/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoNestedLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.geos.GeoLocusNDInterface;
import org.geogebra.common.kernel.geos.GeoLocusable;
import org.junit.jupiter.api.Test;

class LocusV2LaboratoryContractTest extends BaseUnitTest {

	@Test
	void createsAllRequiredFixturesThroughInternalV2Seam() {
		LocusV2LaboratoryFixtures.State state =
				LocusV2LaboratoryFixtures.create(getConstruction());
		List<LocusV2LaboratoryFixtures.Entry> entries = state.getEntries();
		assertEquals(10, entries.size());
		assertTrue(entries.stream().anyMatch(entry ->
				entry.getPurpose().equals("branch/components/lineage")));
		assertTrue(entries.stream().anyMatch(entry ->
				entry.getPurpose().equals("discontinuity/subpaths")));
		assertEquals(DefinitionStatus.VALID, entries.stream()
				.filter(entry -> entry.getPurpose().equals("segment provider"))
				.findFirst().orElseThrow().getLocus().getSemanticDefinition()
				.getDefinitionStatus());
		assertTrue(entries.stream().anyMatch(entry -> entry.getLocus()
				.getSemanticDefinition().getBranches().stream().anyMatch(branch ->
						branch.getProperties().contains(BranchProperty.UNBOUNDED))));
		GeoLocusV2 depthFive = entries.get(entries.size() - 1).getLocus();
		assertInstanceOf(AlgoNestedLocusV2.class, depthFive.getParentAlgorithm());
		assertEquals("laboratory.nested.5", depthFive.getLocusIdentity());
	}

	@Test
	void laboratoryGeosRemainOutsidePublicLegacyContracts() {
		LocusV2LaboratoryFixtures.State state =
				LocusV2LaboratoryFixtures.create(getConstruction());
		for (LocusV2LaboratoryFixtures.Entry entry : state.getEntries()) {
			GeoLocusV2 locus = entry.getLocus();
			Object boundary = locus;
			assertFalse(boundary instanceof org.geogebra.common.kernel.Path);
			assertFalse(boundary instanceof GeoLocusable);
			assertFalse(boundary instanceof GeoLocusNDInterface);
			assertFalse(locus.isGeoLocus());
			assertFalse(locus.isGeoLocusable());
		}
	}

	@Test
	void usesDistinctIdentityAndTemporaryPreferences() throws IOException {
		assertTrue(LocusV2LaboratoryFrame.APPLICATION_TITLE
				.contains("Developer Laboratory"));
		assertFalse(LocusV2LaboratoryFrame.APPLICATION_TITLE.equals("GeoCeDG"));
		assertTrue(LocusV2LaboratoryController.DIAGNOSTICS_WIDTH <= 720);
		assertTrue(LocusV2LaboratoryController.DIAGNOSTICS_HEIGHT <= 520);
		Path settings = LocusV2Laboratory.createTemporarySettingsFile();
		assertEquals("laboratory.properties", settings.getFileName().toString());
		assertTrue(settings.getParent().getFileName().toString()
				.startsWith("geocedg-locus-v2-laboratory-"));
		Files.deleteIfExists(settings);
		Files.deleteIfExists(settings.getParent());
	}
}
