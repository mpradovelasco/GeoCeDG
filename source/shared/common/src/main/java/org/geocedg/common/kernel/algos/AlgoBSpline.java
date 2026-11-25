/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.algos;

import java.util.Collections;
import java.util.EnumSet;

import org.geocedg.common.kernel.bspline.BSplineContractXml;
import org.geocedg.common.kernel.bspline.BSplineDefinition2D;
import org.geocedg.common.kernel.bspline.BSplineInvalidity;
import org.geocedg.common.kernel.bspline.BSplinePersistedContractTarget;
import org.geocedg.common.kernel.bspline.BSplineSemanticEvaluator2D;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
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
import org.geocedg.common.math.bspline.BSplineData;
import org.geocedg.common.math.bspline.BSplineInteriorKnot;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumberValue;

/**
 * PRE-G9B-MAINT-A2: reconstructible normal-DAG parent of one public {@code BSpline}
 * ({@code bspline-2d.md}, contract {@code bspline-2d/v1}). Its inputs are the control-point
 * list, the degree and, for the explicit form, the knot list. A revision whose data fail a
 * precondition publishes no geometry and exposes exactly one typed cause
 * ({@link #getInvalidityCause()}).
 */
public final class AlgoBSpline extends AlgoLocusV2 implements BSplinePersistedContractTarget {
	/** Stable branch address used by Point, metrics and intersections. */
	public static final String BRANCH_KEY = "bspline-2d/main";
	/** Contract of the explicit existence certificate and of compatible redefinition. */
	public static final String PROVIDER_CONTRACT = BSplineDefinition2D.CONTRACT;
	/**
	 * Relative domain tolerance, scaled to the user domain as SplineV2's {@code 1E-12} on its
	 * unit domain. It is consumed only by the provider's tolerant domain membership of a
	 * parameter; span ownership, closure, clamping, multiplicities and continuity are decided
	 * exactly by the library on the binary64 data (section 11.4).
	 */
	public static final double RELATIVE_DOMAIN_EPSILON = 1E-12;

	private final GeoList points;
	private final GeoNumberValue degree;
	private final GeoList knots;
	private BSplineDefinition2D definition;
	private BSplineSemanticEvaluator2D evaluator;
	private BSplineContractXml.PersistedContract persisted;

	/**
	 * @param construction construction
	 * @param points control-point list
	 * @param degree degree
	 * @param knots explicit knot list, or null for the rule {@code clamped-uniform/v1}
	 */
	public AlgoBSpline(Construction construction, GeoList points, GeoNumberValue degree,
			GeoList knots) {
		super(construction, commandInputs(points, degree, knots));
		this.points = points;
		this.degree = degree;
		this.knots = knots;
		publishInitialSnapshot();
	}

	@Override
	protected boolean isSemanticPublicationReady() {
		evaluator = null;
		if (getLocus().getPersistentLocusId() == null) {
			definition = null;
			return false;
		}
		definition = BSplineDefinition2D.capture(points, degree, knots);
		if (!definition.isValid()) {
			return false;
		}
		evaluator = new BSplineSemanticEvaluator2D(BRANCH_KEY, definition.getData());
		return true;
	}

	@Override
	protected LocusDefinition2D createCandidate(long candidateRevision) {
		BSplineData data = definition.getData();
		boolean closed = data.isExactlyClosed();
		double start = data.domainStart();
		double end = data.domainEnd();
		LocusInterval2D domain = new LocusInterval2D(start, end, true, !closed);
		ExplicitNumericDomainProvider2D provider = new ExplicitNumericDomainProvider2D(
				"BSpline user knot parameter u", domain, Orientation.INCREASING, closed,
				RELATIVE_DOMAIN_EPSILON * (end - start));
		EnumSet<BranchProperty> properties = EnumSet.of(BranchProperty.FINITE);
		if (closed) {
			properties.add(BranchProperty.PERIODIC);
		}
		LocusQuality2D quality = new LocusQuality2D(
				ConstructionFidelity.SEMANTICALLY_CONSTRUCTED,
				EvaluationMethod.ANALYTIC_EVALUATION, RepresentationRole.SEMANTIC_RESULT,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED);
		// Existence on the whole domain is proved by the contract (sections 9 and 15);
		// continuity across the seam of an exactly closed curve is C^0 by exact equality.
		LocusBranch2D branch = new LocusBranch2D(BRANCH_KEY, domain,
				LocusExistenceStructure2D.providerProved(BRANCH_KEY, domain,
						Collections.singletonList(domain), closed, closed,
						PROVIDER_CONTRACT),
				Orientation.INCREASING, provenance(data), LocusLineage2D.unchanged(),
				properties, quality);
		return new LocusDefinition2D(getLocus().getLocusIdentity(), candidateRevision,
				DefinitionStatus.VALID, provider, Collections.singletonList(branch), evaluator,
				Determinism.POINTWISE_DETERMINISTIC, evaluator.getEvaluatorSignature(),
				getLocus().getInstrumentation());
	}

	private String provenance(BSplineData data) {
		StringBuilder text = new StringBuilder(PROVIDER_CONTRACT).append("|degree=")
				.append(data.degree()).append("|spans=").append(data.spanCount())
				.append("|knots=").append(definition.getKnotRule()).append("|continuity=");
		boolean first = true;
		for (BSplineInteriorKnot knot : data.interiorKnots()) {
			text.append(first ? "" : ",").append(Double.toHexString(knot.value()))
					.append(":C").append(knot.continuityOrder());
			first = false;
		}
		if (data.isExactlyClosed()) {
			text.append("|seam=C").append(data.seamContinuityOrder());
		}
		return text.toString();
	}

	@Override
	public Commands getClassName() {
		return Commands.BSpline;
	}

	/** One contract for every form: degree, knot list and knot rule are data (ADR 0026). */
	@Override
	public String getV2RedefineContractId() {
		return PROVIDER_CONTRACT;
	}

	@Override
	protected void getCmdExtensionXML(XMLStringBuilder sb, StringTemplate tpl) {
		BSplineContractXml.write(sb, getPersistedContract());
	}

	@Override
	public void restorePersistedContract(BSplineContractXml.PersistedContract contract) {
		persisted = contract;
	}

	/** @return the contract written for this command, keeping a read provenance block */
	public BSplineContractXml.PersistedContract getPersistedContract() {
		String rule = getKnotRule();
		return persisted == null ? BSplineContractXml.PersistedContract.of(rule)
				: persisted.withKnotRule(rule);
	}

	/** @return persisted knot rule of this command form */
	public String getKnotRule() {
		return knots == null ? BSplineContractXml.KNOTS_CLAMPED_UNIFORM_V1
				: BSplineContractXml.KNOTS_EXPLICIT;
	}

	/** @return the typed cause of the current undefined revision, or null */
	public BSplineInvalidity getInvalidityCause() {
		return definition == null ? null : definition.getInvalidity();
	}

	/** @return captured definition of the current revision, or null before identity */
	public BSplineDefinition2D getCapturedDefinition() {
		return definition;
	}

	/** @return evaluator of the current valid revision, or null */
	public BSplineSemanticEvaluator2D getEvaluator() {
		return evaluator;
	}

	/** @return exact public command inputs */
	public GeoElement[] getDurableDependencyGeos() {
		return getInput().clone();
	}

	private static GeoElement[] commandInputs(GeoList points, GeoNumberValue degree,
			GeoList knots) {
		if (points == null || degree == null) {
			throw new IllegalArgumentException("BSpline point list and degree are required");
		}
		return knots == null ? new GeoElement[] {points, degree.toGeoElement()}
				: new GeoElement[] {points, degree.toGeoElement(), knots};
	}
}
