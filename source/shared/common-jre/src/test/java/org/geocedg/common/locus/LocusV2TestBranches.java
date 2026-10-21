/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import java.util.List;
import java.util.Set;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;

/**
 * Branches of the analytic test fixtures (PRE-G9B-CU2 step T11; decision D-CU2-7, option
 * A). A fixture defines its locus as its point function on the components it declares, so
 * its branch carries the explicit certificate of that contract
 * ({@link LocusExistenceStructure2D#providerProved}), which replaces the retired
 * provider-compatibility certificate of the former list constructor of
 * {@link LocusBranch2D}. A periodic fixture whose single component is its whole domain
 * declares its closure, as that constructor assumed.
 */
public final class LocusV2TestBranches {
	/** Contract of the explicit certificates of the test fixtures. */
	public static final String TEST_FIXTURE_CONTRACT = "locus-v2-test-fixture/v1";

	private LocusV2TestBranches() {
		// Static test fixture factory.
	}

	/**
	 * @return a fixture branch with the explicit certificate of its declared components, the
	 *         arguments of the former list constructor of {@link LocusBranch2D}
	 */
	public static LocusBranch2D branch(String branchKey,
			LocusInterval2D declaredDriverDomain, List<LocusInterval2D> components,
			Orientation orientation, String provenance, LocusLineage2D lineage,
			Set<BranchProperty> properties, LocusQuality2D quality) {
		boolean periodic = properties != null
				&& properties.contains(BranchProperty.PERIODIC);
		return new LocusBranch2D(branchKey, declaredDriverDomain,
				LocusExistenceStructure2D.providerProved(branchKey, declaredDriverDomain,
						components, periodic, periodic, TEST_FIXTURE_CONTRACT),
				orientation, provenance, lineage, properties, quality);
	}

	/**
	 * A fixture branch whose point function proves nothing, such as an evaluator that only
	 * returns a typed invalid status: its existence is not established, so the definition
	 * reports the evaluator's own status (PRE-G9B-CU2, P5 applies only to proved facts).
	 *
	 * @return a fixture branch without a certified component
	 */
	public static LocusBranch2D unproved(String branchKey,
			LocusInterval2D declaredDriverDomain, Orientation orientation,
			String provenance, Set<BranchProperty> properties, LocusQuality2D quality) {
		return new LocusBranch2D(branchKey, declaredDriverDomain,
				LocusExistenceStructure2D.notEstablished(branchKey, declaredDriverDomain,
						"locus-v2-test-fixture/unproved"),
				orientation, provenance, LocusLineage2D.unchanged(), properties, quality);
	}
}
