/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operand;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.OperandKind;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusPointComponent2D;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Circle;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Line;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Point;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Result;
import org.geocedg.common.kernel.locus.constructive.Cu2ProgramCorrespondence.ConstantBinding;
import org.geocedg.common.kernel.locus.constructive.ExactQuadraticTower.Element;
import org.geocedg.common.kernel.locus.exact.ExactRational2D;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxModel2D;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D;
import org.geocedg.common.kernel.spatial.identity.DocumentAuthorityRecord;
import org.geogebra.common.kernel.algos.AlgoElement;

/**
 * Transfer of a CU2 existence certificate to the constructive authority,
 * {@code cu2-certificate-transfer/v1} ({@code locus-v2-certified-construction-model.md} section
 * 12.2), for the membership of the point of a locus at the exact replacement of its driver, as
 * {@code Point(L, D)} uses it. The source certificate keeps its text, fingerprint, structure and
 * lineage keys; the transfer is new and separate revision evidence, never persisted.
 *
 * <p>The transfer is admitted only when (N1) the captured class-v3 program is structurally the
 * program of the constructive slice ({@link Cu2ProgramCorrespondence}), (N2) the certificate and
 * the fingerprint are those of the consuming revision, and (N3) the decisions the membership
 * uses are established in the constructive configuration by the margin route: the driver
 * parameter of the exact point lies, with its enclosure, in the interior of a certified
 * continuous component of the source certificate, and the validity predicates of the slice keep
 * their strict signs when every captured constant is replaced by the hull of its captured value
 * and a rigorous enclosure of its constructive value, and the driver by the hull of the exact
 * point and of the captured driver at the enclosed parameter. The enclosure of a constructive
 * value contains the ideal value, through points on paths, rotations and every object of the
 * cone; an object without a rigorous enclosure leaves the transfer not established. Without a
 * demonstrated transfer the result is {@code EVIDENCE_INSUFFICIENT} with a typed cause, and the
 * membership is never published as {@code STRICT}.</p>
 */
public final class Cu2CertificateTransfer {

	/** Version of the transfer policy. */
	public static final String VERSION = DocumentAuthorityRecord.TRANSFER;

	/** Admitted routes of the policy. */
	public enum Route {
		/** Open decisions re-evaluated on the hull of captured and constructive values. */
		MARGIN,
		/** Closed decisions implied by strict structural premises. */
		STRUCTURE,
		/** Closed decisions taken again in the constructive configuration. */
		REDECISION
	}

	/** Outcome of a transfer. */
	public enum Status {
		/** The statement holds in the constructive configuration. */
		TRANSFERRED,
		/** The transfer is not demonstrated; the typed cause says why. */
		EVIDENCE_INSUFFICIENT
	}

	/** Typed cause of a transfer that is not demonstrated. */
	public enum Cause {
		/** The locus has no certificate or no class-v3 program of the consuming revision. */
		CERTIFICATE_NOT_AVAILABLE,
		/** The certificate is not that of the consuming revision (N2). */
		REVISION_MISMATCH,
		/** The captured program is not the program of the constructive slice (N1). */
		CORRESPONDENCE_NOT_DEMONSTRATED,
		/** The exact point gives no rigorous driver parameter. */
		CORRESPONDENCE_NOT_CERTIFIED,
		/** The driver family has no parameter correspondence in this version. */
		UNSUPPORTED_DRIVER_FAMILY,
		/** The parameter enclosure meets the end of a component. */
		COMPONENT_BOUNDARY_UNDECIDED,
		/** The parameter enclosure meets a point component. */
		POINT_COMPONENT_NOT_MATERIALIZED,
		/** The parameter lies in no certified continuous component. */
		NOT_IN_CERTIFIED_COMPONENT,
		/** A strict sign of the slice is not kept on the hull (N3, margin). */
		OPEN_MARGIN_INSUFFICIENT,
		/** The versioned budget of the exact evaluation is exhausted; nothing is decided. */
		BUDGET_EXHAUSTED
	}

	/** The transfer record: revision evidence, never persisted. */
	public static final class Record {
		private final Status status;
		private final Cause cause;
		private final String message;
		private final Route route;
		private final long revision;
		private final String fingerprint;
		private final String sourceContract;
		private final String sourcePolicy;
		private final String structureSignature;
		private final String component;
		private final String parameter;
		private final List<String> premises;

		private Record(Status status, Cause cause, String message, Route route, long revision,
				String fingerprint, String sourceContract, String sourcePolicy,
				String structureSignature, String component, String parameter,
				List<String> premises) {
			this.status = status;
			this.cause = cause;
			this.message = message;
			this.route = route;
			this.revision = revision;
			this.fingerprint = fingerprint;
			this.sourceContract = sourceContract;
			this.sourcePolicy = sourcePolicy;
			this.structureSignature = structureSignature;
			this.component = component;
			this.parameter = parameter;
			this.premises = Collections.unmodifiableList(new ArrayList<>(premises));
		}

		/** @return whether the transfer is established */
		public boolean isTransferred() {
			return status == Status.TRANSFERRED;
		}

		/** @return the outcome */
		public Status getStatus() {
			return status;
		}

		/** @return the typed cause of a transfer that is not established, or {@code null} */
		public Cause getCause() {
			return cause;
		}

		/** @return a description of the cause, or {@code null} */
		public String getMessage() {
			return message;
		}

		/** @return the route of an established transfer, or {@code null} */
		public Route getRoute() {
			return route;
		}

		/** @return the semantic revision of the consuming locus */
		public long getRevision() {
			return revision;
		}

		/** @return the fingerprint of the consuming revision that the transfer cites */
		public String getFingerprint() {
			return fingerprint;
		}

		/** @return the contract of the source certificate */
		public String getSourceContract() {
			return sourceContract;
		}

		/** @return the policy signature of the source certificate */
		public String getSourcePolicy() {
			return sourcePolicy;
		}

		/** @return the structure signature of the source certificate */
		public String getStructureSignature() {
			return structureSignature;
		}

		/** @return the evidence key of the source component, or {@code null} */
		public String getComponent() {
			return component;
		}

		/** @return the driver parameter enclosure, or {@code null} */
		public String getParameter() {
			return parameter;
		}

		/** @return the premises the transfer uses, in order */
		public List<String> getPremises() {
			return premises;
		}

		@Override
		public String toString() {
			return VERSION + " " + status + (cause == null ? " " + route : " " + cause + ": "
					+ message) + " at " + parameter;
		}
	}

	private Cu2CertificateTransfer() {
	}

	/**
	 * Transfers the membership of the traced point of a locus at the replacement of its driver.
	 *
	 * @param locus the locus
	 * @param cone a cone that substitutes the driver of the slice of the locus
	 * @param traced the slice node of the traced point
	 * @param exact the exact evaluation of the cone at its base configuration
	 * @return the transfer record
	 */
	public static Record membership(GeoLocusV2 locus, ConstructiveCone cone, int traced,
			ConstructiveConeEvaluator<Element> exact) {
		Objects.requireNonNull(locus);
		Objects.requireNonNull(cone);
		Objects.requireNonNull(exact);
		Builder record = new Builder(locus.getSemanticRevision());
		AlgoElement parent = locus.getParentAlgorithm();
		if (!(parent instanceof AlgoDependentPointLocusV2)) {
			return record.refuse(Cause.CERTIFICATE_NOT_AVAILABLE, "not a dependent-point locus");
		}
		AlgoDependentPointLocusV2 algorithm = (AlgoDependentPointLocusV2) parent;
		HybridExistenceCertifier2D.Result certificate =
				algorithm.getHybridExistenceCertificate();
		ConstructionBoxModel2D model = ConstructionBoxModel2D.capture(
				locus.getSemanticDefinition());
		if (certificate == null || model == null) {
			return record.refuse(Cause.CERTIFICATE_NOT_AVAILABLE,
					"no certificate or class-v3 program");
		}
		long revision = locus.getSemanticRevision();
		// The certifier records an unbound structure; its publication binds it to the revision.
		LocusExistenceStructure2D structure = certificate.getStructure().bindToRevision(revision);
		record.source(algorithm.getSliceFingerprint(), certificate.getPolicySignature(),
				structure.getSemanticSignature());
		if (locus.getSemanticDefinition().getSemanticRevision() != revision
				|| record.fingerprint == null) {
			return record.refuse(Cause.REVISION_MISMATCH,
					"the certificate is not that of the consuming revision");
		}
		record.premise("N2: fingerprint " + record.fingerprint + " of revision "
				+ locus.getSemanticRevision());
		CertifiedConstructionProgram2D program = model.getProgram();
		Cu2ProgramCorrespondence correspondence =
				Cu2ProgramCorrespondence.match(program, cone, traced);
		if (!correspondence.isDemonstrated()) {
			return record.refuse(Cause.CORRESPONDENCE_NOT_DEMONSTRATED,
					correspondence.getFailure());
		}
		record.premise("N1: " + program.getSteps().size() + " captured steps, "
				+ correspondence.getConstants().size() + " captured constants");
		int replacement = cone.getReplacementNode();
		Result driverPoint = exact.evaluate(replacement);
		if (driverPoint.getFailure() == ConstructiveConeEvaluator.Failure.BUDGET_EXHAUSTED) {
			return record.refuse(Cause.BUDGET_EXHAUSTED, driverPoint.getMessage());
		}
		if (!driverPoint.hasValue() || exact.dependsOnSubstitute(replacement)) {
			return record.refuse(Cause.CORRESPONDENCE_NOT_CERTIFIED,
					"the replacement has no exact constructive point");
		}
		@SuppressWarnings("unchecked")
		Point<Element> point = (Point<Element>) driverPoint.getValue();
		if (program.getDriver() != CertifiedConstructionProgram2D.Driver.CIRCLE
				&& program.getDriver() != CertifiedConstructionProgram2D.Driver.SEGMENT) {
			return record.refuse(Cause.UNSUPPORTED_DRIVER_FAMILY, program.getDriver().name());
		}
		DriverParameterEnclosure parameter;
		try {
			parameter = DriverParameterEnclosure.of(program,
					((ConstructiveArithmetic.Exact) exact.getArithmetic()).getTower(),
					point.getX(), point.getY());
		} catch (ExactEvaluationBudgetExhaustedException exhaustion) {
			return record.refuse(Cause.BUDGET_EXHAUSTED, exhaustion.getMessage());
		}
		if (parameter == null) {
			return record.refuse(Cause.CORRESPONDENCE_NOT_CERTIFIED,
					"no rigorous driver parameter of the exact point");
		}
		record.parameter = parameter.toString();
		Cause placement = place(structure, parameter, record);
		if (placement != null) {
			return record.refuse(placement, "the parameter " + parameter
					+ " is not in the interior of a certified continuous component");
		}
		String failure = margin(cone, traced, correspondence, point, parameter, record);
		if (failure != null) {
			return record.refuse(Cause.OPEN_MARGIN_INSUFFICIENT, failure);
		}
		return record.transferred(Route.MARGIN);
	}

	/**
	 * @return {@code null} when a certified continuous component contains the parameter
	 *         enclosure in its interior, otherwise why not
	 */
	private static Cause place(LocusExistenceStructure2D structure,
			DriverParameterEnclosure parameter, Builder record) {
		double lower = parameter.getLower();
		double upper = parameter.getUpper();
		for (LocusPointComponent2D point : structure.getPointComponents()) {
			if (!(point.getParameterUpper() < lower || point.getParameterLower() > upper)) {
				return Cause.POINT_COMPONENT_NOT_MATERIALIZED;
			}
		}
		boolean touches = false;
		for (ContinuousComponent component : structure.getContinuousValidComponents()) {
			if (!component.getNature().certifies()) {
				continue;
			}
			double start = component.getInterval().getLower();
			double end = component.getInterval().getUpper();
			double[][] candidates = component.isArc()
					? new double[][] {{lower, upper}, shifted(lower, upper)}
					: new double[][] {{lower, upper}};
			for (double[] candidate : candidates) {
				if (component.isClosedPeriodic()
						|| start < candidate[0] && candidate[1] < end) {
					record.component = component.getEvidenceKey();
					record.premise("component " + component.getEvidenceKey() + " "
							+ component.getInterval() + " " + component.getNature()
							+ (component.isClosedPeriodic()
									? " is closed periodic; the margin establishes the open"
											+ " conditions at " + parameter
									: " contains " + parameter + " in its interior"));
					return null;
				}
				touches |= !(candidate[1] < start || candidate[0] > end);
			}
		}
		return touches ? Cause.COMPONENT_BOUNDARY_UNDECIDED : Cause.NOT_IN_CERTIFIED_COMPONENT;
	}

	/** @return the enclosure plus {@code 2 pi}, rounded outward */
	private static double[] shifted(double lower, double upper) {
		return new double[] {Math.nextDown(lower + 2 * Math.PI),
				Math.nextUp(upper + Math.nextUp(2 * Math.PI))};
	}

	/**
	 * Route margin: the slice evaluated on the hull of the captured and constructive values of
	 * every captured constant and of the driver keeps every strict sign.
	 *
	 * @return {@code null} when it does, otherwise why not
	 */
	private static String margin(ConstructiveCone cone, int traced,
			Cu2ProgramCorrespondence correspondence, Point<Element> point,
			DriverParameterEnclosure parameter, Builder record) {
		ConstructiveConeEvaluator<OutwardInterval> constructive =
				ConstructiveConeEvaluator.intervals(cone);
		Map<Integer, List<ConstantBinding>> byNode = new LinkedHashMap<>();
		for (ConstantBinding binding : correspondence.getConstants()) {
			byNode.computeIfAbsent(binding.getConeNode(), node -> new ArrayList<>())
					.add(binding);
		}
		Map<Integer, Object> hulls = new HashMap<>();
		for (Map.Entry<Integer, List<ConstantBinding>> entry : byNode.entrySet()) {
			Result value = constructive.evaluate(entry.getKey());
			if (!value.hasValue()) {
				return "no rigorous enclosure of the constructive value of node "
						+ entry.getKey() + ": " + value;
			}
			Object hull = hull(value.getValue(), entry.getValue());
			if (hull == null) {
				return "no hull of the captured and constructive values of node "
						+ entry.getKey();
			}
			hulls.put(entry.getKey(), hull);
		}
		OutwardInterval[] driven = parameter.getDrivenPoint();
		hulls.put(cone.getReplacementNode(), new Point<>(
				enclosure(point.getX()).hull(driven[0]), enclosure(point.getY()).hull(driven[1])));
		ConstructiveConeEvaluator<OutwardInterval> margin = new ConstructiveConeEvaluator<>(cone,
				ConstructiveArithmetic.intervals(), false, Collections.emptyMap(), hulls);
		Result slice = margin.evaluate(traced);
		if (!slice.hasValue()) {
			return slice.toString();
		}
		record.premise("margin: " + hulls.size() + " hulls, " + margin.getDecisions().size()
				+ " strict signs kept");
		return null;
	}

	/** The hull of the constructive enclosure of a node and of its captured values. */
	@SuppressWarnings("unchecked")
	private static Object hull(Object constructive, List<ConstantBinding> bindings) {
		Object hull = constructive;
		for (ConstantBinding binding : bindings) {
			if (binding.getValue() == null) {
				if (!(hull instanceof OutwardInterval)) {
					return null;
				}
				hull = ((OutwardInterval) hull).hull(OutwardInterval.point(binding.getRadius()));
				continue;
			}
			Operand captured = binding.getValue();
			if (captured.getKind() == OperandKind.POINT && hull instanceof Point) {
				Point<OutwardInterval> p = (Point<OutwardInterval>) hull;
				ExactRational2D z = ExactRational2D.ofBinary64(captured.getValue(2));
				if (z.isZero()) {
					return null;
				}
				hull = new Point<>(p.getX().hull(OutwardInterval.point(captured.getValue(3)))
						.hull(OutwardInterval.enclosing(ExactRational2D.ofBinary64(
								captured.getValue(0)).divide(z))),
						p.getY().hull(OutwardInterval.point(captured.getValue(4)))
								.hull(OutwardInterval.enclosing(ExactRational2D.ofBinary64(
										captured.getValue(1)).divide(z))));
			} else if (captured.getKind() == OperandKind.LINE && hull instanceof Line) {
				hull = lineHull((Line<OutwardInterval>) hull, captured);
				if (hull == null) {
					return null;
				}
			} else if (captured.getKind() == OperandKind.CIRCLE && hull instanceof Circle) {
				Circle<OutwardInterval> c = (Circle<OutwardInterval>) hull;
				ExactRational2D a2 = ExactRational2D.ofBinary64(captured.getValue(0));
				ExactRational2D a4 = ExactRational2D.ofBinary64(captured.getValue(1));
				ExactRational2D a5 = ExactRational2D.ofBinary64(captured.getValue(2));
				ExactRational2D half = ExactRational2D.ofBinary64(captured.getValue(3));
				hull = new Circle<>(c.getCenterX().hull(OutwardInterval.point(-captured
						.getValue(1))), c.getCenterY().hull(OutwardInterval.point(-captured
								.getValue(2))), c.getRadiusSquared()
										.hull(OutwardInterval.enclosing(a4.multiply(a4)
												.add(a5.multiply(a5)).subtract(a2)))
										.hull(OutwardInterval.enclosing(half.multiply(half))));
			} else {
				return null;
			}
		}
		return hull;
	}

	/**
	 * The hull of an unbounded line in a canonical scale: both coefficient vectors divided by
	 * the absolute value of the coefficient that dominates in the captured line, which keeps
	 * the orientation and every predicate of the slice.
	 */
	private static Line<OutwardInterval> lineHull(Line<OutwardInterval> constructive,
			Operand captured) {
		if (constructive.isLimited()) {
			return null;
		}
		double[] coefficients = {captured.getValue(0), captured.getValue(1),
				captured.getValue(2)};
		int dominant = Math.abs(coefficients[0]) >= Math.abs(coefficients[1]) ? 0 : 1;
		if (coefficients[dominant] == 0) {
			return null;
		}
		OutwardInterval[] ideal = {constructive.getA(), constructive.getB(),
				constructive.getC()};
		int sign = ideal[dominant].certifiedSign();
		if (sign != (coefficients[dominant] > 0 ? 1 : -1)) {
			return null;
		}
		OutwardInterval scale = sign > 0 ? ideal[dominant] : ideal[dominant].negate();
		ExactRational2D capturedScale = ExactRational2D.ofBinary64(
				Math.abs(coefficients[dominant]));
		OutwardInterval[] hull = new OutwardInterval[3];
		for (int index = 0; index < 3; index++) {
			hull[index] = ideal[index].divide(scale).hull(OutwardInterval.enclosing(
					ExactRational2D.ofBinary64(coefficients[index]).divide(capturedScale)));
		}
		return new Line<>(hull[0], hull[1], hull[2], false, false, false, null, null);
	}

	private static OutwardInterval enclosure(Element value) {
		double[] bounds = value.outwardEnclosure();
		return OutwardInterval.of(bounds[0], bounds[1]);
	}

	/** Accumulates one record. */
	private static final class Builder {
		private final long revision;
		private final List<String> premises = new ArrayList<>();
		private String fingerprint;
		private String policy;
		private String structure;
		private String component;
		private String parameter;

		private Builder(long revision) {
			this.revision = revision;
		}

		private void source(String sliceFingerprint, String sourcePolicy,
				String structureSignature) {
			fingerprint = sliceFingerprint;
			policy = sourcePolicy;
			structure = structureSignature;
		}

		private void premise(String premise) {
			premises.add(premise);
		}

		private Record refuse(Cause cause, String message) {
			return new Record(Status.EVIDENCE_INSUFFICIENT, cause, message, null, revision,
					fingerprint, HybridExistenceCertifier2D.CONTRACT, policy, structure,
					component, parameter, premises);
		}

		private Record transferred(Route route) {
			return new Record(Status.TRANSFERRED, null, null, route, revision, fingerprint,
					HybridExistenceCertifier2D.CONTRACT, policy, structure, component, parameter,
					premises);
		}
	}
}
