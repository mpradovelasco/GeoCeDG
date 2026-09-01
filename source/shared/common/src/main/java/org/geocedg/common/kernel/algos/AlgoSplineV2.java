/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.algos;

import java.util.Collections;
import java.util.EnumSet;

import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.ConstructionFidelity;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationMethod;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.RepresentationRole;
import org.geocedg.common.kernel.spline.SplinePolynomialModel2D;
import org.geocedg.common.kernel.spline.SplineSemanticEvaluator2D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoFunctionNVar;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumberValue;

/** Reconstructible normal-DAG parent for one public semantic SplineV2. */
public final class AlgoSplineV2 extends AlgoLocusV2 {
	/** Stable branch address used by Point, metrics and intersections. */
	public static final String BRANCH_KEY = "spline-v2/main";
	private static final double DOMAIN_EPSILON = 1E-12;
	private static final String PROVENANCE =
			"semantic-spline-v2/piecewise-polynomial/v1";

	private final GeoList points;
	private final GeoNumberValue degree;
	private final GeoFunctionNVar weight;
	private SplinePolynomialModel2D model;
	private SplineSemanticEvaluator2D evaluator;

	/** Creates one public command parent from its reconstructible inputs. */
	public AlgoSplineV2(Construction construction, GeoList points,
			GeoNumberValue degree, GeoFunctionNVar weight) {
		super(construction, commandInputs(points, degree, weight));
		this.points = points;
		this.degree = degree;
		this.weight = weight;
		publishInitialSnapshot();
	}

	@Override
	protected boolean isSemanticPublicationReady() {
		if (getLocus().getPersistentLocusId() == null || points == null
				|| degree == null || !points.isDefined()
				|| !degree.toGeoElement().isDefined()
				|| weight != null && !weight.isDefined()) {
			return false;
		}
		double degreeValue = degree.getDouble();
		if (!Double.isFinite(degreeValue) || degreeValue != Math.rint(degreeValue)) {
			return false;
		}
		try {
			model = SplinePolynomialModel2D.create(points, (int) degreeValue,
					weight);
			evaluator = new SplineSemanticEvaluator2D(BRANCH_KEY, model);
			return true;
		} catch (IllegalArgumentException exception) {
			model = null;
			evaluator = null;
			return false;
		}
	}

	@Override
	protected LocusDefinition2D createCandidate(long candidateRevision) {
		boolean periodic = model.isClosed();
		LocusInterval2D domain = new LocusInterval2D(0, 1, true, !periodic);
		ExplicitNumericDomainProvider2D provider =
				new ExplicitNumericDomainProvider2D(
						"SplineV2 normalized oriented parameter t", domain,
						Orientation.INCREASING, periodic, DOMAIN_EPSILON);
		EnumSet<BranchProperty> properties = EnumSet.of(BranchProperty.FINITE);
		if (periodic) {
			properties.add(BranchProperty.PERIODIC);
		}
		LocusQuality2D quality = new LocusQuality2D(
				ConstructionFidelity.SEMANTICALLY_CONSTRUCTED,
				EvaluationMethod.ANALYTIC_EVALUATION,
				RepresentationRole.SEMANTIC_RESULT,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED);
		LocusBranch2D branch = new LocusBranch2D(BRANCH_KEY, domain,
				Collections.singletonList(domain), Orientation.INCREASING,
				PROVENANCE + "|degree=" + model.getDegree() + "|spans="
						+ model.getSpanCount(),
				LocusLineage2D.unchanged(), properties, quality);
		return new LocusDefinition2D(getLocus().getLocusIdentity(),
				candidateRevision, DefinitionStatus.VALID, provider,
				Collections.singletonList(branch), evaluator,
				Determinism.POINTWISE_DETERMINISTIC,
				evaluator.getEvaluatorSignature(), getLocus().getInstrumentation());
	}

	@Override
	public Commands getClassName() {
		return Commands.SplineV2;
	}

	/** @return immutable current polynomial authority, or null while undefined */
	public SplinePolynomialModel2D getPolynomialModel() {
		return model;
	}

	/** @return exact public command inputs */
	public GeoElement[] getDurableDependencyGeos() {
		return getInput().clone();
	}

	private static GeoElement[] commandInputs(GeoList points,
			GeoNumberValue degree, GeoFunctionNVar weight) {
		if (points == null || degree == null) {
			throw new IllegalArgumentException(
					"SplineV2 point list and degree are required");
		}
		return weight == null
				? new GeoElement[] {points, degree.toGeoElement()}
				: new GeoElement[] {points, degree.toGeoElement(), weight};
	}
}
