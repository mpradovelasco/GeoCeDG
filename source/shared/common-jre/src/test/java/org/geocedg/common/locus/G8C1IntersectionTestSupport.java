/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import java.util.List;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionV2;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionCapability2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;

/** Shared deterministic fixtures for the productive G8C1 tests. */
final class G8C1IntersectionTestSupport {
	private G8C1IntersectionTestSupport() {
	}

	static G8BIntersectionFixtures.Fixture curve(Construction construction,
			String identity, double lower, double upper,
			G8BIntersectionFixtures.Curve curve) {
		return G8BIntersectionFixtures.single(construction, identity, lower,
				upper, true, true, false, curve);
	}

	static G8BIntersectionFixtures.Fixture horizontal(Construction construction,
			String identity, double lower, double upper, double y) {
		return curve(construction, identity, lower, upper,
				(source, branch, parameter) -> new LocusPoint2D(parameter, y));
	}

	static AlgoLocusIntersectionV2 algorithm(Construction construction,
			G8BIntersectionFixtures.Fixture fixture, GeoElement target,
			String identity) {
		return algorithm(construction, fixture, target, identity, null,
				new GeoElement[0]);
	}

	static AlgoLocusIntersectionV2 algorithm(Construction construction,
			G8BIntersectionFixtures.Fixture fixture, GeoElement target,
			String identity, LocusIntersectionCapability2D capability,
			GeoElement[] dependencies) {
		return new AlgoLocusIntersectionV2(construction, fixture.locus(), target,
				identity + "/pair", identity + "/constructive-lineage",
				identity + "/target", identity + "/topology", capability,
				dependencies);
	}

	static LocusIntersectionResult2D result(Construction construction,
			G8BIntersectionFixtures.Fixture fixture, GeoElement target,
			String identity) {
		return algorithm(construction, fixture, target, identity).getResult()
				.getIntersectionResult();
	}

	static List<Double> parameters(LocusIntersectionResult2D result) {
		return result.getFiniteSolutions().stream()
				.map(solution -> solution.getRevisionEvidence()
						.getSemanticParameter())
				.toList();
	}
}
