/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.common.locus;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionWorkBudget2D;
import org.geocedg.common.kernel.locus.interaction.M08SnapshotPlacement2D;
import org.junit.jupiter.api.Test;

/** Opt-in characterization: identical input/counters before and after H1. */
class M08BudgetH1ProbeTest extends G9U0PublicSurfaceTestBase {

	@Test
	void characterize149AcrossComposedForwardEvaluators() {
		List<GeoLocusV2> sources = new ArrayList<>();
		sources.add(add("S=SplineV2({(-2,0),(-2/3,0),(2/3,0),(2,0)},3)"));
		String previous = "S";
		for (int depth = 0; depth < 32; depth++) {
			if (depth > 0) {
				String label = "T" + depth;
				sources.add(add(label + "=Translate(" + previous + ",(0,0))"));
				previous = label;
			}
			long before = calls(sources);
			var selection = M08SnapshotPlacement2D.query(sources.get(depth), 0.25, 0.01,
					new LocusPointInteractionPolicy2D(0.1, 1E-10, 1E-12,
							new LocusPointInteractionWorkBudget2D(149, 512, 80, 1024)));
			long afterQuery = calls(sources);
			String commit = "NOT_OFFERED";
			if (!selection.getResult().getChoices().isEmpty()) {
				try {
					selection.commit("P" + depth, selection.getResult().getChoices().get(0));
					commit = "CREATED";
				} catch (M08SnapshotPlacement2D.Rejected rejected) {
					commit = rejected.getFailure().name();
				}
			}
			System.out.println("H1 depth=" + depth + " limit=149 discovery="
					+ selection.getResult().getHistoricalResult().getInstrumentation()
							.getSemanticEvaluations()
					+ " actualQuery=" + (afterQuery - before) + " permission="
					+ selection.getResult().getPermission() + " failure="
					+ selection.getResult().getFailure() + " commit=" + commit
					+ " actualTotal=" + (calls(sources) - before));
			selection.close();
		}
	}

	private static long calls(List<GeoLocusV2> sources) {
		return sources.stream().mapToLong(source -> source.getInstrumentation().snapshot()
				.getEvaluatorCalls()).sum();
	}
}
