/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import java.util.Collections;
import java.util.EnumSet;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusPointFunction2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusV2Factory;
import org.geocedg.common.kernel.locus.LocusV2Mode;
import org.geocedg.common.locus.G8AIntersectionNumerics.Curve2D;
import org.geocedg.common.locus.G8AIntersectionNumerics.FactorizationProof;
import org.geocedg.common.locus.G8AIntersectionNumerics.Problem;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.Policy;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.WorkBudget;
import org.geocedg.common.locus.G8ATargetAdapters.Target2D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoNumeric;

/** Shared test-private semantic fixtures for G8A. */
final class G8AIntersectionFixtures {
	static final double DOMAIN_EPSILON = 1E-14;

	private G8AIntersectionFixtures() {
	}

	interface TangentFunction {
		LocusPoint2D evaluate(double parameter);
	}

	record Fixture(GeoNumeric source, GeoLocusV2 locus, String branchKey,
			String componentKey, double lower, double upper,
			boolean lowerIncluded, boolean upperIncluded, boolean periodic,
			TangentFunction tangent) {
		Curve2D semanticCurve() {
			return new Curve2D() {
				@Override
				public LocusPoint2D evaluate(double parameter) {
					try (LocusEvaluationSession2D session =
							LocusEvaluationSession2D.reference()) {
						LocusEvaluation2D evaluation = locus.evaluate(branchKey,
								parameter, session);
						if (!evaluation.isValid()) {
							throw new IllegalStateException(evaluation.getDiagnostic());
						}
						return evaluation.getPoint();
					}
				}

				@Override
				public LocusPoint2D derivative(double parameter) {
					return tangent == null ? null : tangent.evaluate(parameter);
				}
			};
		}

		Problem problem(Target2D target, FactorizationProof proof,
				String topologyContext) {
			return new Problem(locus.getLocusIdentity() + "+" + target.identity(),
					locus.getLocusIdentity(), locus.getSemanticRevision(),
					target.identity(), 1, branchKey, branchKey + "/lineage-v1",
					componentKey, topologyContext, lower, upper, lowerIncluded,
					upperIncluded, periodic, semanticCurve(), target, proof,
					Policy.measuredCandidate(), WorkBudget.measuredCandidate());
		}
	}

	static Fixture create(Construction construction, String identity,
			double lower, double upper, boolean lowerIncluded,
			boolean upperIncluded, boolean periodic,
			LocusPointFunction2D function, TangentFunction tangent) {
		ExplicitNumericDomainProvider2D provider =
				new ExplicitNumericDomainProvider2D(identity + "/provider-v1",
						new LocusInterval2D(lower, upper, lowerIncluded,
								upperIncluded), Orientation.INCREASING, periodic,
						DOMAIN_EPSILON);
		String branchKey = identity + "/branch-main";
		LocusBranch2D branch = LocusV2Factory.fullDomainBranch(branchKey,
				provider, identity + "/semantic-v1",
				EnumSet.of(BranchProperty.FINITE));
		GeoNumeric source = new GeoNumeric(construction, 0);
		GeoLocusV2 locus = LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				construction, identity, source, provider,
				Collections.singletonList(branch), function,
				identity + "/evaluator-v1");
		return new Fixture(source, locus, branchKey, branchKey + "/component-0",
				lower, upper, lowerIncluded, upperIncluded, periodic, tangent);
	}
}
