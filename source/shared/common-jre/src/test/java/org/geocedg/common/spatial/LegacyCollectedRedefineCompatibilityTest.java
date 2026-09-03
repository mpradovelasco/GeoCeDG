/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

/** Ordinary collected redefinitions must not fabricate spatial transactions. */
class LegacyCollectedRedefineCompatibilityTest extends BaseUnitTest {

	@Test
	void ordinaryBatchRebuildsDependenciesAndRemainsUnassociated() throws Exception {
		add("a=1");
		add("b=a+1");
		assertTrue(getConstruction().getSpatialIdentityRegistry().isEmpty());

		replaceOrdinaryValue(10);
		assertOrdinaryValues(10, 11);
		replaceOrdinaryValue(20);
		assertOrdinaryValues(20, 21);
	}

	@Test
	void emptySpatialCompletionStillRejects() {
		IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
				() -> getConstruction().getSpatialIdentityRegistry()
						.completeRedefineHostOperations(List.of()));
		assertEquals("Completed redefine context collection cannot be empty",
				failure.getMessage());
		assertTrue(getConstruction().getSpatialIdentityRegistry().isEmpty());
	}

	private void replaceOrdinaryValue(double value) throws Exception {
		getConstruction().startCollectingRedefineCalls();
		getConstruction().replace(lookup("a"),
				new GeoNumeric(getConstruction(), value));
		getConstruction().processCollectedRedefineCalls();
	}

	private void assertOrdinaryValues(double value, double dependentValue) {
		assertEquals(value, ((GeoNumeric) lookup("a")).getDouble(), 0);
		assertEquals(dependentValue, ((GeoNumeric) lookup("b")).getDouble(), 0);
		assertTrue(getConstruction().getSpatialIdentityRegistry().isEmpty());
		assertFalse(getConstruction().getSpatialIdentityRegistry()
				.isParticipating(lookup("a")));
		assertFalse(getConstruction().getSpatialIdentityRegistry()
				.isParticipating(lookup("b")));
	}
}
