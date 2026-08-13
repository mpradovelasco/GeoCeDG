/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.DoubleFunction;

import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusInstrumentation2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.metric.AnalyticLocusMetricCapability2D;
import org.geocedg.common.kernel.locus.metric.BetweenPositionsMetricQuery;
import org.geocedg.common.kernel.locus.metric.LocusAnalyticMetricEvaluation2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricCapabilityHierarchy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricPositionBinder2D;
import org.geocedg.common.kernel.locus.metric.LocusSemanticPosition2D;
import org.geocedg.common.kernel.locus.metric.MetricPositionBinding2D;
import org.geocedg.common.kernel.locus.metric.OpenBoundaryPolicy;
import org.geocedg.common.kernel.locus.metric.SamePositionPolicy;
import org.geocedg.common.kernel.locus.metric.TraversalDirection;

/** Deterministic semantic fixtures shared only by productive G7B tests. */
final class G7BMetricFixtures {
	static final String BRANCH = "g7b.sheet.main";
	static final double EPS_DOMAIN = 1E-14;

	private G7BMetricFixtures() {
	}

	static LocusDefinition2D definition(String identity, long revision,
			boolean periodic, List<LocusInterval2D> components,
			DoubleFunction<LocusPoint2D> function) {
		return definitionWithBranches(identity, revision, periodic,
				List.of(branch(BRANCH, -4, 4, components)), function);
	}

	static LocusDefinition2D definitionWithBranches(String identity,
			long revision,
			boolean periodic, List<LocusBranch2D> branches,
			DoubleFunction<LocusPoint2D> function) {
		ExplicitNumericDomainProvider2D provider =
				new ExplicitNumericDomainProvider2D("g7b-parameter/v1",
						new LocusInterval2D(-4, 4, true, true),
						Orientation.INCREASING, periodic, EPS_DOMAIN);
		return new LocusDefinition2D(identity, revision,
				branches.isEmpty() ? DefinitionStatus.EMPTY_DOMAIN
						: DefinitionStatus.VALID,
				provider, branches,
				(definition, branch, parameter, session) ->
						LocusEvaluation2D.valid(function.apply(parameter),
								Regularity.UNKNOWN, branch.getQuality()),
				Determinism.POINTWISE_DETERMINISTIC,
				"g7b-fixture-evaluator/v1", new LocusInstrumentation2D());
	}

	static LocusDefinition2D emptyDefinition(String identity, long revision) {
		return definitionWithBranches(identity, revision, false, List.of(),
				parameter -> new LocusPoint2D(0, 0));
	}

	static LocusBranch2D branch(String key, double lower, double upper,
			List<LocusInterval2D> components) {
		EnumSet<BranchProperty> properties =
				EnumSet.of(BranchProperty.FINITE);
		return new LocusBranch2D(key,
				new LocusInterval2D(lower, upper, true, true), components,
				Orientation.INCREASING, "g7b-fixture/v1",
				LocusLineage2D.unchanged(), properties,
				LocusQuality2D.analyticDoubleSemantic());
	}

	static MetricPositionBinding2D bind(LocusDefinition2D definition,
			String branch, double parameter) {
		LocusSemanticPosition2D position = new LocusSemanticPosition2D(
				definition.getLocusIdentity(), branch,
				definition.getProvider().getProviderId(),
				definition.getProvider().canonicalize(parameter));
		return new LocusMetricPositionBinder2D().bind(position, definition);
	}

	static BetweenPositionsMetricQuery between(LocusDefinition2D definition,
			double start, double target, TraversalDirection direction,
			OpenBoundaryPolicy boundary, SamePositionPolicy same) {
		return new BetweenPositionsMetricQuery(bind(definition, BRANCH, start),
				bind(definition, BRANCH, target), direction, boundary, same,
				LocusMetricPolicy2D.initial());
	}

	static LocusMetricCapabilityHierarchy2D analytic(double scale,
			String version) {
		return new LocusMetricCapabilityHierarchy2D(List.of(
				new AnalyticLocusMetricCapability2D(version,
						(branch, start, end) ->
								LocusAnalyticMetricEvaluation2D.exactFinite(
										scale * Math.abs(end - start)))));
	}

	static List<LocusInterval2D> components(double... endpoints) {
		if (endpoints.length % 2 != 0) {
			throw new IllegalArgumentException("Endpoint pairs required");
		}
		ArrayList<LocusInterval2D> components = new ArrayList<>();
		for (int index = 0; index < endpoints.length; index += 2) {
			components.add(new LocusInterval2D(endpoints[index],
					endpoints[index + 1], true, true));
		}
		return components;
	}
}
