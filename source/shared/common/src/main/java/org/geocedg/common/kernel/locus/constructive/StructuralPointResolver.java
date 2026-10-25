/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.constructive.ConstructiveCone.ConstructiveConeException;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Failure;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Incidence;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.IncidenceResult;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Point;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Result;
import org.geocedg.common.kernel.locus.constructive.ExactQuadraticTower.Element;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoIntersect;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.algos.AlgoIntersectLines;
import org.geogebra.common.kernel.algos.AlgoIntersectSingle;
import org.geogebra.common.kernel.algos.AlgoJoinPointsSegment;
import org.geogebra.common.kernel.algos.AlgoPointOnPath;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoConicPart;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoSegment;

/**
 * The structural point {@code Point(L, D)} and the intersection {@code Intersect(L, r, D)}
 * under the constructive authority (Stage A design sections 2 to 5, with the revisions of
 * D-AUT-11; E1 implementation design section 9). The four facts are resolved in order and the
 * first that fails leaves the point undefined with its typed cause:
 * <ol>
 * <li>{@code D} comes from an admitted provenance on its support: a point on the path, a
 * defining point of a segment, or an output of an intersection algorithm with the support among
 * its inputs;</li>
 * <li>that support is the support of the driver of {@code L}, the same object;</li>
 * <li>the constructive point {@code D*} is exact, and its driver parameter lies in a certified
 * component of {@code L}: the membership {@code P in L} transfers from {@code CU2}
 * ({@code cu2-certificate-transfer/v1}) and is {@code STRICT};</li>
 * <li>for the intersection, {@code P in r} is decided against the constructive target: by the
 * exact evaluation when it uses no boundary substitute ({@code STRICT}, or a rigorous
 * contradiction), otherwise by {@code SUBSTITUTE_GENERIC} ({@code RELAXED}).</li>
 * </ol>
 * The materialized position is a representation: the centre of a rigorous enclosure of the
 * constructive point, with the enclosure as its declared error. Nothing is persisted.
 */
public final class StructuralPointResolver {

	/** Typed causes of an undefined structural point. */
	public enum Cause {
		/** The document is not governed by {@code constructive-authority/v1}. */
		CONSTRUCTIVE_AUTHORITY_REQUIRED,
		/** {@code L} is not a locus driven by a point on a path of this version. */
		UNSUPPORTED_DRIVER_FAMILY,
		/** {@code D} is undefined. */
		DRIVER_POINT_UNDEFINED,
		/** {@code D} has no admitted provenance on a support. */
		PROVENANCE_NOT_ESTABLISHED,
		/** The support of {@code D} is not the support of the driver of {@code L}. */
		DRIVER_SUPPORT_MISMATCH,
		/** The inherited selection of an intersection is not reconstructed. */
		SELECTION_NOT_RECONSTRUCTED,
		/** {@code D*} or its driver parameter is not established rigorously. */
		CORRESPONDENCE_NOT_CERTIFIED,
		/** {@code D*} lies on the support line of a bounded driver outside its limited part. */
		OUT_OF_DRIVER_DOMAIN,
		/** The parameter enclosure meets the seam of a periodic domain at a component end. */
		SEAM_SIDE_UNDECIDED,
		/** The parameter enclosure meets the end of a component. */
		COMPONENT_BOUNDARY_UNDECIDED,
		/** The parameter enclosure meets a point component. */
		POINT_COMPONENT_NOT_MATERIALIZED,
		/** The parameter lies in no certified continuous component. */
		OUTSIDE_CERTIFIED_COMPONENTS,
		/** The membership {@code P in L} does not transfer from {@code CU2}. */
		MEMBERSHIP_NOT_TRANSFERRED,
		/** The target is undefined. */
		TARGET_UNDEFINED,
		/** {@code P in r} is neither established nor admitted. */
		INCIDENCE_NOT_ESTABLISHED,
		/** {@code P in r} is rigorously refuted for the constructive objects. */
		INCIDENCE_REFUTED_IN_CONSTRUCTIVE_CONFIGURATION,
		/** {@code P} lies outside the limited part of the target, rigorously. */
		NOT_ON_LIMITED_TARGET,
		/** An exact evaluation leaves the class. */
		EXACT_EVALUATION_OUT_OF_CLASS,
		/** An exact evaluation exhausts {@code cu3-exact-evaluation-budget/v1}. */
		EXACT_EVALUATION_BUDGET_EXHAUSTED,
		/** No rigorous enclosure of the constructive point exists for its position. */
		POSITION_UNAVAILABLE
	}

	private StructuralPointResolver() {
	}

	/**
	 * @param locus the locus {@code L}
	 * @param driverPoint the point {@code D}
	 * @param target the target {@code r} of an intersection, or {@code null}
	 * @param budget the budget of each exact evaluation
	 * @return the resolution of the four facts
	 */
	public static StructuralPointResolution resolve(GeoLocusV2 locus, GeoPoint driverPoint,
			GeoElement target, ExactEvaluationBudget budget) {
		return resolve(locus, driverPoint, target, budget, null);
	}

	/**
	 * @param locus the locus {@code L}
	 * @param driverPoint the point {@code D}
	 * @param target the target {@code r} of an intersection, or {@code null}
	 * @param budget the budget of each exact evaluation
	 * @param previous the resolution of an earlier computation, or {@code null}; it is
	 *        returned unchanged when the state of every input and the revision of the locus are
	 *        the same, so a drag of the driver of the locus repeats nothing
	 * @return the resolution of the four facts
	 */
	public static StructuralPointResolution resolve(GeoLocusV2 locus, GeoPoint driverPoint,
			GeoElement target, ExactEvaluationBudget budget,
			StructuralPointResolution previous) {
		Objects.requireNonNull(locus);
		Objects.requireNonNull(driverPoint);
		Construction construction = locus.getConstruction();
		if (!AuthorityPolicy.of(construction).isConstructive()) {
			return StructuralPointResolution.undefined(Cause.CONSTRUCTIVE_AUTHORITY_REQUIRED,
					"the document is governed by " + AuthorityPolicy.LEGACY_AUTHORITY, null);
		}
		AlgoElement parent = locus.getParentAlgorithm();
		if (!(parent instanceof AlgoDependentPointLocusV2) || parent.getInputLength() < 2
				|| !(parent.getInput(1) instanceof GeoPoint)
				|| !((GeoPoint) parent.getInput(1)).isPointOnPath()) {
			return StructuralPointResolution.undefined(Cause.UNSUPPORTED_DRIVER_FAMILY,
					"not a locus driven by a point on a path", null);
		}
		GeoElement traced = parent.getInput(0).toGeoElement();
		GeoPoint driver = (GeoPoint) parent.getInput(1);
		GeoElement support = driver.getPath().toGeoElement();
		// Circles and segments only: arcs are outside constructive-class/v1 (D-AUT-11).
		if (support instanceof GeoConicPart
				|| !(support instanceof GeoConic || support instanceof GeoSegment)) {
			return StructuralPointResolution.undefined(Cause.UNSUPPORTED_DRIVER_FAMILY,
					"a driver on " + support.getTypeString() + " has no parameter correspondence"
							+ " in " + Cu2CertificateTransfer.VERSION, null);
		}
		if (!driverPoint.isDefined()) {
			return StructuralPointResolution.undefined(Cause.DRIVER_POINT_UNDEFINED,
					"D is undefined", null);
		}
		if (target != null && !target.isDefined()) {
			return StructuralPointResolution.undefined(Cause.TARGET_UNDEFINED,
					"the target is undefined", null);
		}
		List<String> premises = new ArrayList<>();
		Cause provenance = provenance(driverPoint, support, driver, premises);
		if (provenance != null) {
			return StructuralPointResolution.undefined(provenance,
					"D is not a point of the support of the driver by provenance", null);
		}
		ConstructiveCone.Builder builder = ConstructiveCone.builder(construction);
		final int targetNode = target == null ? -1 : builder.add(target);
		int supportNode = builder.add(support);
		int point;
		try {
			point = builder.addWithDriverSubstitution(traced, driver, driverPoint);
		} catch (ConstructiveConeException refused) {
			return StructuralPointResolution.undefined(Cause.PROVENANCE_NOT_ESTABLISHED,
					refused.getMessage(), null);
		}
		ConstructiveCone cone = builder.build();
		String digest = cone.getStateDigest() + "|revision=" + locus.getSemanticRevision()
				+ "|budget=" + budget;
		if (previous != null && digest.equals(previous.getStateDigest())) {
			return previous;
		}
		ConstructiveConeEvaluator<Element> exact = ConstructiveConeEvaluator.exact(cone, budget);
		return decide(locus, cone, exact, point, supportNode, targetNode, premises, budget,
				digest).withExactCounters(exact.getExactCounters());
	}

	/** Facts 2 to 4 on the exact evaluation of the cone at its base configuration. */
	private static StructuralPointResolution decide(GeoLocusV2 locus, ConstructiveCone cone,
			ConstructiveConeEvaluator<Element> exact, int point, int supportNode, int targetNode,
			List<String> premises, ExactEvaluationBudget budget, String digest) {
		int replacement = cone.getReplacementNode();
		IncidenceResult onSupport = exact.incidence(replacement, supportNode);
		if (onSupport.getIncidence() == Incidence.NOT_DECIDED) {
			return StructuralPointResolution.undefined(cause(onSupport.getFailure()),
					String.valueOf(onSupport.getFailure()), digest);
		}
		if (!onSupport.getSubstitutes().isEmpty()) {
			return StructuralPointResolution.undefined(Cause.CORRESPONDENCE_NOT_CERTIFIED,
					"the constructive point of D depends on a boundary substitute", digest);
		}
		if (onSupport.getIncidence() == Incidence.NOT_ON_LIMITED_TARGET) {
			return StructuralPointResolution.undefined(Cause.OUT_OF_DRIVER_DOMAIN,
					"the constructive point of D lies on the support line outside its limited"
							+ " part", digest);
		}
		if (onSupport.getIncidence() != Incidence.HOLDS) {
			return StructuralPointResolution.undefined(Cause.PROVENANCE_NOT_ESTABLISHED,
					"the constructive point of D is not on the support", digest);
		}
		premises.add("D* lies exactly on the support of the driver");
		Cu2CertificateTransfer.Record transfer =
				Cu2CertificateTransfer.membership(locus, cone, point, exact);
		if (!transfer.isTransferred()) {
			return StructuralPointResolution.undefined(cause(transfer), transfer.toString(),
					digest).withTransfer(transfer);
		}
		RelationIntegrityRecord membership = RelationIntegrityRecord.derive("P on L",
				RelationIntegrity.STRICT, RelationIntegrityRecord.Evidence.TRANSFERRED_BY_MARGIN,
				Collections.emptyList(), Collections.emptyList(), transfer.toString());
		double[] position = position(cone, point);
		if (position == null) {
			return StructuralPointResolution.undefined(Cause.POSITION_UNAVAILABLE,
					"no rigorous enclosure of the constructive point", digest)
					.withTransfer(transfer);
		}
		StructuralPointResolution resolved = StructuralPointResolution.defined(position,
				membership, null, premises, digest).withTransfer(transfer);
		if (targetNode < 0) {
			return resolved;
		}
		return incidence(resolved, cone, exact, point, targetNode, budget, digest);
	}

	/** Fact 4 of the intersection: {@code P in r} against the constructive target. */
	private static StructuralPointResolution incidence(StructuralPointResolution resolved,
			ConstructiveCone cone, ConstructiveConeEvaluator<Element> exact, int point,
			int target, ExactEvaluationBudget budget, String digest) {
		IncidenceResult decided = exact.incidence(point, target);
		if (decided.getIncidence() == Incidence.NOT_DECIDED) {
			return StructuralPointResolution.undefined(cause(decided.getFailure()),
					String.valueOf(decided.getFailure()), digest)
					.withTransfer(resolved.getTransfer());
		}
		if (decided.getSubstitutes().isEmpty()) {
			switch (decided.getIncidence()) {
			case HOLDS:
				return resolved.withIncidence(RelationIntegrityRecord.derive("P on r",
						RelationIntegrity.STRICT, RelationIntegrityRecord.Evidence.EXACT_EVALUATION,
						Collections.emptyList(), Collections.emptyList(), null));
			case NOT_ON_LIMITED_TARGET:
				return StructuralPointResolution.undefined(Cause.NOT_ON_LIMITED_TARGET,
						"P lies outside the limited part of the target", digest)
						.withTransfer(resolved.getTransfer());
			default:
				return StructuralPointResolution.undefined(
						Cause.INCIDENCE_REFUTED_IN_CONSTRUCTIVE_CONFIGURATION,
						"the exact residual of P on the target is nonzero", digest)
						.withTransfer(resolved.getTransfer())
						.withIncidence(RelationIntegrityRecord.derive("P on r",
								RelationIntegrity.CONTRADICTED,
								RelationIntegrityRecord.Evidence.EXACT_EVALUATION,
								Collections.emptyList(), Collections.emptyList(), null));
			}
		}
		SubstituteGenericEvidence.Result relaxed = SubstituteGenericEvidence.incidence("P on r",
				cone, point, target, budget);
		if (relaxed.getRecord().getIntegrity() != RelationIntegrity.RELAXED) {
			return StructuralPointResolution.undefined(Cause.INCIDENCE_NOT_ESTABLISHED,
					String.valueOf(relaxed), digest).withTransfer(resolved.getTransfer())
					.withIncidence(relaxed.getRecord())
					.withRelaxedCounters(relaxed.getCounters());
		}
		return resolved.withIncidence(relaxed.getRecord())
				.withRelaxedCounters(relaxed.getCounters());
	}

	/**
	 * Facts 1 and 2: an admitted provenance of {@code D} on a support, and that support is the
	 * support of the driver, the same object.
	 *
	 * @return {@code null} when both hold, otherwise the cause
	 */
	private static Cause provenance(GeoPoint point, GeoElement support, GeoPoint driver,
			List<String> premises) {
		if (point == driver) {
			return Cause.PROVENANCE_NOT_ESTABLISHED;
		}
		// A defining point of a segment is often free, so it is recognized before its parent.
		AlgoElement supportParent = support.getParentAlgorithm();
		if (supportParent instanceof AlgoJoinPointsSegment
				&& (((AlgoJoinPointsSegment) supportParent).getP() == point
						|| ((AlgoJoinPointsSegment) supportParent).getQ() == point)) {
			premises.add("DEFINING_POINT of the support of the driver");
			return null;
		}
		AlgoElement parent = point.getParentAlgorithm();
		if (parent == null) {
			return Cause.PROVENANCE_NOT_ESTABLISHED;
		}
		if (parent.getClass() == AlgoPointOnPath.class) {
			if (parent.getInput(0).toGeoElement() != support) {
				return Cause.DRIVER_SUPPORT_MISMATCH;
			}
			premises.add("PATH_POINT on the support of the driver");
			return null;
		}
		AlgoElement source = parent instanceof AlgoIntersectSingle
				? ((AlgoIntersectSingle) parent).getSourceIntersection() : parent;
		if (source instanceof AlgoIntersect || source instanceof AlgoIntersectLines) {
			boolean onSupport = source instanceof AlgoIntersectLineConic
					? ((AlgoIntersectLineConic) source).getLine() == support
							|| ((AlgoIntersectLineConic) source).getConic() == support
					: inputsContain(source, support);
			if (!onSupport) {
				return Cause.DRIVER_SUPPORT_MISMATCH;
			}
			premises.add("INTERSECTION_OUTPUT of " + source.getClass().getSimpleName()
					+ " with the support of the driver among its inputs");
			return null;
		}
		return Cause.PROVENANCE_NOT_ESTABLISHED;
	}

	private static boolean inputsContain(AlgoElement algorithm, GeoElement geo) {
		for (int index = 0; index < algorithm.getInputLength(); index++) {
			if (algorithm.getInput(index).toGeoElement() == geo) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the centre of a rigorous enclosure of the constructive point and its half widths,
	 *         or {@code null} when it has none
	 */
	private static double[] position(ConstructiveCone cone, int point) {
		Result enclosure = ConstructiveConeEvaluator.intervals(cone).evaluate(point);
		if (!enclosure.hasValue() || !(enclosure.getValue() instanceof Point)) {
			return null;
		}
		@SuppressWarnings("unchecked")
		Point<OutwardInterval> value = (Point<OutwardInterval>) enclosure.getValue();
		OutwardInterval x = value.getX();
		OutwardInterval y = value.getY();
		if (!x.isFinite() || !y.isFinite()) {
			return null;
		}
		return new double[] {x.getLower() / 2 + x.getUpper() / 2,
				y.getLower() / 2 + y.getUpper() / 2, x.width() / 2, y.width() / 2};
	}

	private static Cause cause(Result failure) {
		Failure kind = failure == null ? null : failure.getFailure();
		if (kind == null) {
			return Cause.CORRESPONDENCE_NOT_CERTIFIED;
		}
		switch (kind) {
		case BUDGET_EXHAUSTED:
			return Cause.EXACT_EVALUATION_BUDGET_EXHAUSTED;
		case SELECTION_NOT_RECONSTRUCTED:
		case BRANCH_CORRESPONDENCE_NOT_DEMONSTRATED:
		case LIVE_DECISION_DISAGREES:
			return Cause.SELECTION_NOT_RECONSTRUCTED;
		case OUT_OF_CLASS:
			return Cause.EXACT_EVALUATION_OUT_OF_CLASS;
		case INVALID:
		case SUBSTITUTE_UNDEFINED:
			return Cause.DRIVER_POINT_UNDEFINED;
		default:
			return Cause.CORRESPONDENCE_NOT_CERTIFIED;
		}
	}

	private static Cause cause(Cu2CertificateTransfer.Record transfer) {
		switch (transfer.getCause()) {
		case COMPONENT_BOUNDARY_UNDECIDED:
			return Cause.COMPONENT_BOUNDARY_UNDECIDED;
		case SEAM_SIDE_UNDECIDED:
			return Cause.SEAM_SIDE_UNDECIDED;
		case OUT_OF_DRIVER_DOMAIN:
			return Cause.OUT_OF_DRIVER_DOMAIN;
		case POINT_COMPONENT_NOT_MATERIALIZED:
			return Cause.POINT_COMPONENT_NOT_MATERIALIZED;
		case NOT_IN_CERTIFIED_COMPONENT:
			return Cause.OUTSIDE_CERTIFIED_COMPONENTS;
		case CORRESPONDENCE_NOT_CERTIFIED:
			return Cause.CORRESPONDENCE_NOT_CERTIFIED;
		case UNSUPPORTED_DRIVER_FAMILY:
			return Cause.UNSUPPORTED_DRIVER_FAMILY;
		case BUDGET_EXHAUSTED:
			return Cause.EXACT_EVALUATION_BUDGET_EXHAUSTED;
		default:
			return Cause.MEMBERSHIP_NOT_TRANSFERRED;
		}
	}
}
