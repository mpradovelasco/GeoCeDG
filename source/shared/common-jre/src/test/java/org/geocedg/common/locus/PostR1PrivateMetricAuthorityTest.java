/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineExecutionMode;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

/**
 * Closing check of GeoCeDG 1.2.2 ({@code locus-v2-metrics.md} section 29): the private rich
 * authority that a {@code LocusLength} or {@code Length} command creates for its number is
 * removed with that number when nothing else reads it, and only then.
 */
final class PostR1PrivateMetricAuthorityTest extends PreG9bR3RedefineTestBase {

	private List<GeoLocusMetricResult> results() {
		List<GeoLocusMetricResult> results = new ArrayList<>();
		for (GeoElement geo : getConstruction().getGeoSetConstructionOrder()) {
			if (geo instanceof GeoLocusMetricResult) {
				results.add((GeoLocusMetricResult) geo);
			}
		}
		return results;
	}

	private static GeoLocusMetricResult authority(GeoNumeric number) {
		GeoLocusMetricResult rich = AlgoLocusMetricScalarAdapter.richResultOf(number);
		assertNotNull(rich);
		return rich;
	}

	@Test
	void theAuthorityOfALengthGoesWithItsNumber() {
		GeoLocusV2 locus = certifiedParabola();
		GeoNumeric total = add("t=LocusLength(" + locus.getLabelSimple() + ")");
		GeoLocusMetricResult rich = authority(total);
		assertTrue(rich.isPrivateScalarAuthority());
		assertEquals(1, results().size());

		total.removeOrSetUndefinedIfHasFixedDescendent();

		assertNull(getKernel().lookupLabel("t"));
		assertFalse(rich.isLabelSet(), "the private authority was removed");
		assertEquals(List.of(), results());
		assertTrue(locus.isLabelSet(), "the locus stays");
	}

	@Test
	void anAuthorityThatAnotherAlgorithmReadsStays() {
		GeoLocusV2 locus = certifiedParabola();
		GeoNumeric total = add("t=LocusLength(" + locus.getLabelSimple() + ")");
		GeoLocusMetricResult rich = authority(total);
		GeoNumeric second = new AlgoLocusMetricScalarAdapter(getConstruction(), "u", rich)
				.getScalarOutput();

		total.removeOrSetUndefinedIfHasFixedDescendent();
		assertTrue(rich.isLabelSet(), "another number still reads the authority");
		assertTrue(second.isLabelSet());

		second.removeOrSetUndefinedIfHasFixedDescendent();
		assertFalse(rich.isLabelSet(), "the last reader took it with it");
	}

	@Test
	void theAuthorityOfALengthBetweenTwoPointsAndOfAReopenedDocumentGoesToo()
			throws Exception {
		GeoLocusV2 locus = certifiedParabola();
		add("EndA=Point(" + locus.getLabelSimple() + ",\"" + BRANCH + "\",0.25)");
		add("EndB=Point(" + locus.getLabelSimple() + ",\"" + BRANCH + "\",0.75)");
		add("lenB=LocusLength(" + locus.getLabelSimple() + ",EndA,EndB)");
		add("t=LocusLength(" + locus.getLabelSimple() + ")");
		assertEquals(2, results().size());

		AppCommon reopened = openInFreshApp(getApp().getXML());
		for (String label : new String[] {"lenB", "t"}) {
			GeoNumeric number = (GeoNumeric) reopened.getKernel().lookupLabel(label);
			GeoLocusMetricResult rich = authority(number);
			assertTrue(rich.isPrivateScalarAuthority(), "the reserved name was read back");
			number.removeOrSetUndefinedIfHasFixedDescendent();
			assertFalse(rich.isLabelSet(), label);
		}
		for (GeoElement geo : reopened.getKernel().getConstruction()
				.getGeoSetConstructionOrder()) {
			assertFalse(geo instanceof GeoLocusMetricResult, geo.getLabelSimple());
		}
		assertNotNull(reopened.getKernel().lookupLabel("EndA"));
		assertNotNull(reopened.getKernel().lookupLabel("EndB"));
	}

	@Test
	void removingTheLocusRemovesNumberAndAuthorityAsBefore() {
		GeoLocusV2 locus = certifiedParabola();
		GeoNumeric total = add("t=LocusLength(" + locus.getLabelSimple() + ")");
		GeoLocusMetricResult rich = authority(total);
		locus.removeOrSetUndefinedIfHasFixedDescendent();
		assertFalse(total.isLabelSet());
		assertFalse(rich.isLabelSet());
		assertEquals(List.of(), results());
	}

	@Test
	void undoAndRedoOfTheDeletionRestoreAndRemoveTheirPair() {
		getApp().setUndoActive(true);
		GeoLocusV2 locus = certifiedParabola();
		add("t=LocusLength(" + locus.getLabelSimple() + ")");
		getApp().storeUndoInfo();
		((GeoNumeric) getKernel().lookupLabel("t")).removeOrSetUndefinedIfHasFixedDescendent();
		getApp().storeUndoInfo();
		assertEquals(List.of(), results());

		getKernel().undo();
		GeoNumeric restored = (GeoNumeric) getKernel().lookupLabel("t");
		assertNotNull(restored);
		assertTrue(authority(restored).isPrivateScalarAuthority());
		assertEquals(1, results().size());

		getKernel().redo();
		assertNull(getKernel().lookupLabel("t"));
		assertEquals(List.of(), results());
	}

	@Test
	void aRedefinedNumberKeepsTheRecordedLimitation() {
		// locus-v2-metrics.md section 29, declared limit: a redefinition of the number is a
		// transaction of the spatial identity, which removes nothing else; the authority of
		// before stays in the document without a reader, as in 1.2.0 and 1.2.1. Pinned so that
		// a change of this behaviour is seen.
		GeoLocusV2 locus = certifiedParabola();
		GeoLocusMetricResult rich = authority(add("t=LocusLength(" + locus.getLabelSimple()
				+ ")"));
		Outcome outcome = redefine("t", "5", always(SpatialRedefineExecutionMode
				.LEGACY_REPLACEMENT));
		assertTrue(outcome.succeeded(), outcome.errors());
		assertEquals(5, ((GeoNumeric) getKernel().lookupLabel("t")).getValue(), 0);
		assertTrue(rich.isLabelSet());
		assertTrue(rich.getAlgorithmList().isEmpty());
		assertEquals(1, results().size());
	}
}
