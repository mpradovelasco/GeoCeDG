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
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Step;
import org.geocedg.common.kernel.locus.LocusBoundary2D;
import org.geocedg.common.kernel.locus.LocusCertifiedFold2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Coverage;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPointComponent2D;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Circle;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Decision;
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
	/** The operation table of the symmetries of the folds of {@code CU2}. */
	private static final String SYMMETRY = "fold-symmetry/v1";
	/** The largest number of pieces of the isolation band of a fold. */
	private static final int MAXIMUM_PIECES = 64;

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
		EVIDENCE_INSUFFICIENT,
		/** A re-decision refutes the statement in the constructive configuration. */
		CONTRADICTED
	}

	/**
	 * Closed decisions of a source certificate that a strict consumer may request at the
	 * driver parameter of an exact point (section 12.2, routes structure and re-decision).
	 */
	public enum ClosedDecision {
		/** A certified fold of the traced map: existence by symmetry, isolation by its band. */
		FOLD,
		/** The location of the degenerate root of a component end or of a point component. */
		ROOT_LOCATION,
		/** The multiplicity of a root. */
		MULTIPLICITY,
		/** The root count of a Sturm certificate. */
		STURM_COUNT,
		/** A critical value of the configuration, such as the bite of the author's model. */
		CRITICAL_VALUE
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
		/** The exact parameter lies outside the bounded driver domain. */
		OUT_OF_DRIVER_DOMAIN,
		/** The parameter enclosure meets the end of a component. */
		COMPONENT_BOUNDARY_UNDECIDED,
		/** The parameter enclosure meets the seam of a periodic domain at a component end. */
		SEAM_SIDE_UNDECIDED,
		/** The parameter enclosure meets a point component. */
		POINT_COMPONENT_NOT_MATERIALIZED,
		/** The parameter lies in no certified continuous component. */
		NOT_IN_CERTIFIED_COMPONENT,
		/** A strict sign of the slice is not kept on the hull (N3, margin). */
		OPEN_MARGIN_INSUFFICIENT,
		/** The certificate records no such closed decision at the parameter. */
		DECISION_NOT_IN_CERTIFICATE,
		/** A premise of a structural decision is not {@code STRICT} (N3, structure). */
		STRUCTURAL_PREMISE_NOT_STRICT,
		/** No admitted method takes the closed decision again (N3, re-decision). */
		CLOSED_DECISION_NOT_REDECIDED,
		/** The re-decision refutes the statement for the constructive objects. */
		CONTRADICTED_IN_CONSTRUCTIVE_CONFIGURATION,
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

		/** @return whether a re-decision refutes the statement */
		public boolean isContradicted() {
			return status == Status.CONTRADICTED;
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
		Builder record = new Builder(locus.getSemanticRevision());
		Prepared prepared = prepare(locus, cone, traced, exact, record);
		if (prepared.refusal != null) {
			return prepared.refusal;
		}
		LocusExistenceStructure2D structure = prepared.structure;
		DriverParameterEnclosure parameter = prepared.parameter;
		try {
			if (Boolean.TRUE.equals(parameter.isOutside(structure.getCanonicalDomain()))) {
				return record.refuse(Cause.OUT_OF_DRIVER_DOMAIN, "the exact parameter "
						+ parameter + " lies outside the driver domain "
						+ structure.getCanonicalDomain());
			}
		} catch (ExactEvaluationBudgetExhaustedException exhaustion) {
			return record.refuse(Cause.BUDGET_EXHAUSTED, exhaustion.getMessage());
		}
		Cause placement = place(structure, parameter, record);
		if (placement != null) {
			return record.refuse(placement, "the parameter " + parameter
					+ " is not in the interior of a certified continuous component");
		}
		String failure = margin(cone, traced, prepared.correspondence, prepared.point,
				parameter, record);
		if (failure != null) {
			return record.refuse(Cause.OPEN_MARGIN_INSUFFICIENT, failure);
		}
		return record.transferred(Route.MARGIN);
	}

	/**
	 * Establishes in the constructive configuration a closed decision of the source certificate
	 * at the driver parameter of the exact point of the replacement, for a strict consumer:
	 * a fold by the route structure, when every premise of its symmetry is {@code STRICT} and its
	 * isolation band keeps its sign by margin; the location of a degenerate root by the route
	 * re-decision, the exact evaluation of the slice at the replacement with constructible
	 * premises only. A closed decision that no implemented method takes again is never consumed
	 * from the captured configuration: {@code CLOSED_DECISION_NOT_REDECIDED}.
	 *
	 * @param locus the locus
	 * @param cone a cone that substitutes the driver of the slice of the locus
	 * @param traced the slice node of the traced point
	 * @param support the cone node of the support of the driver
	 * @param exact the exact evaluation of the cone at its base configuration
	 * @param decision the closed decision requested
	 * @return the transfer record
	 */
	public static Record closedDecision(GeoLocusV2 locus, ConstructiveCone cone, int traced,
			int support, ConstructiveConeEvaluator<Element> exact, ClosedDecision decision) {
		Objects.requireNonNull(locus);
		Objects.requireNonNull(decision);
		Builder record = new Builder(locus.getSemanticRevision());
		Prepared prepared = prepare(locus, cone, traced, exact, record);
		if (prepared.refusal != null) {
			return prepared.refusal;
		}
		try {
			switch (decision) {
			case FOLD:
				return fold(prepared, cone, traced, support, exact, record);
			case ROOT_LOCATION:
				return rootLocation(prepared, cone, traced, exact, record);
			default:
				return record.refuse(Cause.CLOSED_DECISION_NOT_REDECIDED, decision
						+ ": no implemented method decides it again in the constructive"
						+ " configuration");
			}
		} catch (ExactEvaluationBudgetExhaustedException exhaustion) {
			return record.refuse(Cause.BUDGET_EXHAUSTED, exhaustion.getMessage());
		}
	}

	/** Conditions N1 and N2 and the exact driver parameter of the replacement. */
	private static Prepared prepare(GeoLocusV2 locus, ConstructiveCone cone, int traced,
			ConstructiveConeEvaluator<Element> exact, Builder record) {
		Objects.requireNonNull(cone);
		Objects.requireNonNull(exact);
		AlgoElement parent = locus.getParentAlgorithm();
		if (!(parent instanceof AlgoDependentPointLocusV2)) {
			return Prepared.refused(record.refuse(Cause.CERTIFICATE_NOT_AVAILABLE,
					"not a dependent-point locus"));
		}
		AlgoDependentPointLocusV2 algorithm = (AlgoDependentPointLocusV2) parent;
		HybridExistenceCertifier2D.Result certificate =
				algorithm.getHybridExistenceCertificate();
		ConstructionBoxModel2D model = ConstructionBoxModel2D.capture(
				locus.getSemanticDefinition());
		if (certificate == null || model == null) {
			return Prepared.refused(record.refuse(Cause.CERTIFICATE_NOT_AVAILABLE,
					"no certificate or class-v3 program"));
		}
		long revision = locus.getSemanticRevision();
		// The certifier records an unbound structure; its publication binds it to the revision.
		LocusExistenceStructure2D structure = certificate.getStructure().bindToRevision(revision);
		record.source(algorithm.getSliceFingerprint(), certificate.getPolicySignature(),
				structure.getSemanticSignature());
		if (locus.getSemanticDefinition().getSemanticRevision() != revision
				|| record.fingerprint == null) {
			return Prepared.refused(record.refuse(Cause.REVISION_MISMATCH,
					"the certificate is not that of the consuming revision"));
		}
		record.premise("N2: fingerprint " + record.fingerprint + " of revision "
				+ locus.getSemanticRevision());
		CertifiedConstructionProgram2D program = model.getProgram();
		Cu2ProgramCorrespondence correspondence =
				Cu2ProgramCorrespondence.match(program, cone, traced);
		if (!correspondence.isDemonstrated()) {
			return Prepared.refused(record.refuse(Cause.CORRESPONDENCE_NOT_DEMONSTRATED,
					correspondence.getFailure()));
		}
		record.premise("N1: " + program.getSteps().size() + " captured steps, "
				+ correspondence.getConstants().size() + " captured constants");
		int replacement = cone.getReplacementNode();
		Result driverPoint = exact.evaluate(replacement);
		if (driverPoint.getFailure() == ConstructiveConeEvaluator.Failure.BUDGET_EXHAUSTED) {
			return Prepared.refused(record.refuse(Cause.BUDGET_EXHAUSTED,
					driverPoint.getMessage()));
		}
		if (!driverPoint.hasValue() || exact.dependsOnSubstitute(replacement)) {
			return Prepared.refused(record.refuse(Cause.CORRESPONDENCE_NOT_CERTIFIED,
					"the replacement has no exact constructive point"));
		}
		@SuppressWarnings("unchecked")
		Point<Element> point = (Point<Element>) driverPoint.getValue();
		if (program.getDriver() != CertifiedConstructionProgram2D.Driver.CIRCLE
				&& program.getDriver() != CertifiedConstructionProgram2D.Driver.SEGMENT) {
			return Prepared.refused(record.refuse(Cause.UNSUPPORTED_DRIVER_FAMILY,
					program.getDriver().name()));
		}
		DriverParameterEnclosure parameter;
		try {
			parameter = DriverParameterEnclosure.of(program,
					((ConstructiveArithmetic.Exact) exact.getArithmetic()).getTower(),
					point.getX(), point.getY());
		} catch (ExactEvaluationBudgetExhaustedException exhaustion) {
			return Prepared.refused(record.refuse(Cause.BUDGET_EXHAUSTED,
					exhaustion.getMessage()));
		}
		if (parameter == null) {
			return Prepared.refused(record.refuse(Cause.CORRESPONDENCE_NOT_CERTIFIED,
					"no rigorous driver parameter of the exact point"));
		}
		record.parameter = parameter.toString();
		return new Prepared(structure, program, correspondence, point, parameter, null);
	}

	/**
	 * Route structure for a fold of {@code fold-symmetry/v1}: the certificate records a fold at
	 * the parameter, the parameter is exactly a fixed point of its reflection, every premise of
	 * the symmetry holds for the constructive objects, exactly or by a constructive identity
	 * certificate for every value of a cut, and the isolation band keeps its sign by margin.
	 */
	private static Record fold(Prepared prepared, ConstructiveCone cone, int traced, int support,
			ConstructiveConeEvaluator<Element> exact, Builder record) {
		DriverParameterEnclosure parameter = prepared.parameter;
		LocusCertifiedFold2D fold = null;
		for (LocusCertifiedFold2D candidate : prepared.structure.getCertifiedFolds()) {
			if (fold == null && meetsModuloPeriod(candidate.getParameterLower(),
					candidate.getParameterUpper(), parameter)) {
				fold = candidate;
			}
		}
		if (fold == null) {
			return record.refuse(Cause.DECISION_NOT_IN_CERTIFICATE,
					"the certificate records no fold at " + parameter);
		}
		String symmetry = fold.getSymmetry();
		boolean horizontal = (SYMMETRY + ":HORIZONTAL").equals(symmetry);
		if (!horizontal && !(SYMMETRY + ":VERTICAL").equals(symmetry)) {
			return record.refuse(Cause.STRUCTURAL_PREMISE_NOT_STRICT, "the symmetry "
					+ symmetry + " has no structural premises in this version");
		}
		record.premise("CU2 fold [" + fold.getParameterLower() + ", "
				+ fold.getParameterUpper() + "] by " + symmetry);
		List<String> fixedPoints = horizontal ? List.of("0", "pi") : List.of("pi/2", "-pi/2");
		if (!fixedPoints.contains(parameter.getExact())) {
			return record.refuse(Cause.CLOSED_DECISION_NOT_REDECIDED, "the parameter "
					+ parameter + " is not decided exactly at a fixed point of the reflection");
		}
		String premise = symmetryPremises(prepared, cone, support, exact, horizontal, record);
		if (premise != null) {
			return record.refuse(Cause.STRUCTURAL_PREMISE_NOT_STRICT, premise);
		}
		String isolation = foldIsolation(prepared, cone, traced, support, fold, record);
		if (isolation != null) {
			return record.refuse(Cause.OPEN_MARGIN_INSUFFICIENT, isolation);
		}
		return record.transferred(Route.STRUCTURE);
	}

	/**
	 * The premises of {@code fold-symmetry/v1} for the constructive objects: the driver is a
	 * circle of {@code constructive-class/v1} read in the frame of the axes, and every step that
	 * reads the driver point does so through a magnitude that the reflection keeps, decided
	 * exactly on constructive values without a boundary substitute or, when a constant reads
	 * one, by a constructive identity certificate for every value of the cut.
	 *
	 * @return {@code null} when every premise is {@code STRICT}, otherwise the first that is not
	 */
	private static String symmetryPremises(Prepared prepared, ConstructiveCone cone, int support,
			ConstructiveConeEvaluator<Element> exact, boolean horizontal, Builder record) {
		CertifiedConstructionProgram2D program = prepared.program;
		ConstructiveOperation circle = cone.getNode(support).getOperation();
		if (circle != ConstructiveOperation.CIRCLE_THROUGH
				&& circle != ConstructiveOperation.CIRCLE_RADIUS
				|| program.getDriverConstant(3) != 0 || program.getDriverConstant(4) != 0) {
			return "the driver is not a circle of constructive-class/v1 read in the frame of the"
					+ " axes";
		}
		Object driver = exactValue(exact, support);
		if (!(driver instanceof Circle)) {
			return "the driver circle has no exact value without a boundary substitute";
		}
		@SuppressWarnings("unchecked")
		Circle<Element> driverCircle = (Circle<Element>) driver;
		record.premise("structure: the driver is " + circle + " of constructive-class/v1");
		List<Step> steps = program.getSteps();
		for (int index = 0; index < steps.size(); index++) {
			Step step = steps.get(index);
			if (!readsDriver(step)) {
				continue;
			}
			int programNode = index + 1;
			String failure;
			switch (step.getOperation()) {
			case PARALLEL:
				failure = linePremise(prepared, cone, exact, programNode, !horizontal, record);
				break;
			case PERPENDICULAR:
				failure = linePremise(prepared, cone, exact, programNode, horizontal, record);
				break;
			case CIRCLE_THROUGH:
				failure = centrePremise(prepared, cone, support, exact, programNode, driverCircle,
						horizontal, record);
				break;
			default:
				failure = "step " + programNode + " reads the driver outside " + SYMMETRY;
			}
			if (failure != null) {
				return failure;
			}
			record.premise("structure: step " + programNode + " " + step.getOperation()
					+ " reads the driver symmetrically, decided exactly");
		}
		return null;
	}

	/**
	 * The constant line of a parallel or perpendicular to the driver is exactly horizontal or
	 * vertical for the constructive objects: decided exactly when it reads no boundary
	 * substitute, otherwise by a constructive identity certificate for every value of the cut
	 * (increment E2).
	 */
	private static String linePremise(Prepared prepared, ConstructiveCone cone,
			ConstructiveConeEvaluator<Element> exact, int programNode, boolean horizontalLine,
			Builder record) {
		ConstantBinding binding = binding(prepared, programNode, 1);
		if (binding == null) {
			return "the constant line of step " + programNode + " has no cone node";
		}
		String direction = horizontalLine ? "horizontal" : "vertical";
		Object value = exactValue(exact, binding.getConeNode());
		if (value instanceof Line) {
			@SuppressWarnings("unchecked")
			Line<Element> line = (Line<Element>) value;
			Element zero = horizontalLine ? line.getA() : line.getB();
			Element other = horizontalLine ? line.getB() : line.getA();
			if (zero.signum() != 0 || other.signum() == 0) {
				return "the constant line of step " + programNode + " is not exactly "
						+ direction;
			}
			return null;
		}
		ConstructiveIdentityCertificate certificate = ConstructiveIdentityCertifier.axisParallel(
				"the constant line of step " + programNode + " is " + direction, cone,
				binding.getConeNode(), horizontalLine, ConstructiveIdentityBudget.V1);
		if (!certificate.isIdentityCertified()) {
			return "the constant line of step " + programNode + " is not certified " + direction
					+ " for every value of the cut: " + certificate;
		}
		record.premise("structure: the constant line of step " + programNode + " is "
				+ direction + " by " + ConstructiveIdentityCertificate.VERSION + " "
				+ certificate.getKey());
		return null;
	}

	/**
	 * The constant centre of a circle through the driver lies exactly on the mirror: decided
	 * exactly when it reads no boundary substitute, otherwise by a constructive identity
	 * certificate for every value of the cut (increment E2).
	 */
	private static String centrePremise(Prepared prepared, ConstructiveCone cone, int support,
			ConstructiveConeEvaluator<Element> exact, int programNode, Circle<Element> driver,
			boolean horizontal, Builder record) {
		ConstantBinding binding = binding(prepared, programNode, 0);
		if (binding == null) {
			return "the constant centre of step " + programNode + " has no cone node";
		}
		Object value = exactValue(exact, binding.getConeNode());
		if (value instanceof Point) {
			@SuppressWarnings("unchecked")
			Point<Element> centre = (Point<Element>) value;
			Element offset = horizontal ? centre.getY().subtract(driver.getCenterY())
					: centre.getX().subtract(driver.getCenterX());
			return offset.signum() == 0 ? null : "the constant centre of step " + programNode
					+ " is not exactly on the mirror";
		}
		ConstructiveIdentityCertificate certificate = ConstructiveIdentityCertifier
				.sameCoordinate("the constant centre of step " + programNode + " is on the mirror",
						cone, binding.getConeNode(), support, horizontal,
						ConstructiveIdentityBudget.V1);
		if (!certificate.isIdentityCertified()) {
			return "the constant centre of step " + programNode + " is not certified on the"
					+ " mirror for every value of the cut: " + certificate;
		}
		record.premise("structure: the constant centre of step " + programNode
				+ " is on the mirror by " + ConstructiveIdentityCertificate.VERSION + " "
				+ certificate.getKey());
		return null;
	}

	/**
	 * Route margin for the isolation of a fold: on its band, one component of {@code F''} keeps
	 * a strict sign when every captured constant is the hull of its captured and constructive
	 * values and the driver circle the hull of the captured and constructive circles, in
	 * interval jets on at most {@value #MAXIMUM_PIECES} pieces of the band.
	 */
	@SuppressWarnings("unchecked")
	private static String foldIsolation(Prepared prepared, ConstructiveCone cone, int traced,
			int support, LocusCertifiedFold2D fold, Builder record) {
		Map<Integer, Object> hulls = new HashMap<>();
		String failure = hulls(cone, prepared.correspondence, hulls);
		if (failure != null) {
			return failure;
		}
		Result value = ConstructiveConeEvaluator.intervals(cone).evaluate(support);
		if (!(value.getValue() instanceof Circle)) {
			return "no rigorous enclosure of the constructive driver circle";
		}
		Circle<OutwardInterval> ideal = (Circle<OutwardInterval>) value.getValue();
		CertifiedConstructionProgram2D program = prepared.program;
		OutwardInterval[] driver = {
				ideal.getCenterX().hull(OutwardInterval.point(program.getDriverConstant(6))),
				ideal.getCenterY().hull(OutwardInterval.point(program.getDriverConstant(7))),
				ideal.getRadiusSquared().sqrt().hull(OutwardInterval.point(
						program.getDriverConstant(0))).hull(OutwardInterval.point(
								program.getDriverConstant(1)))};
		Map<Integer, Object> jets = new HashMap<>();
		for (Map.Entry<Integer, Object> entry : hulls.entrySet()) {
			jets.put(entry.getKey(), constantJet(entry.getValue()));
		}
		for (int pieces = 1; pieces <= MAXIMUM_PIECES; pieces *= 2) {
			int component = isolation(cone, traced, program, jets, driver, fold, pieces);
			if (component >= 0) {
				record.premise("margin: " + (component == 0 ? "x''" : "y''")
						+ " keeps a strict sign on the band [" + fold.getBandLower() + ", "
						+ fold.getBandUpper() + "] in " + pieces + " pieces");
				return null;
			}
		}
		return "no component of F'' keeps a strict sign on the band [" + fold.getBandLower()
				+ ", " + fold.getBandUpper() + "]";
	}

	/**
	 * @return the component of {@code F''} that keeps one strict sign on every piece of the
	 *         band, or {@code -1}
	 */
	@SuppressWarnings("unchecked")
	private static int isolation(ConstructiveCone cone, int traced,
			CertifiedConstructionProgram2D program, Map<Integer, Object> jets,
			OutwardInterval[] driver, LocusCertifiedFold2D fold, int pieces) {
		int[] signs = new int[2];
		double width = (fold.getBandUpper() - fold.getBandLower()) / pieces;
		for (int piece = 0; piece < pieces; piece++) {
			double lower = piece == 0 ? fold.getBandLower()
					: Math.nextDown(fold.getBandLower() + piece * width);
			double upper = piece == pieces - 1 ? fold.getBandUpper()
					: Math.nextUp(fold.getBandLower() + (piece + 1) * width);
			OutwardInterval[] trig = DriverParameterEnclosure.cosSin(lower, upper);
			if (trig == null) {
				return -1;
			}
			IntervalJet cos = IntervalJet.of(trig[0], trig[1].negate(), trig[0].negate());
			IntervalJet sin = IntervalJet.of(trig[1], trig[0], trig[1].negate());
			IntervalJet radius = IntervalJet.constant(driver[2]);
			IntervalJet along = cos.multiply(constant(program.getDriverConstant(2)))
					.add(sin.multiply(constant(program.getDriverConstant(4))));
			IntervalJet across = cos.multiply(constant(program.getDriverConstant(3)))
					.add(sin.multiply(constant(program.getDriverConstant(5))));
			Map<Integer, Object> fixed = new HashMap<>(jets);
			fixed.put(cone.getReplacementNode(), new Point<>(IntervalJet.constant(driver[0])
					.add(radius.multiply(along)), IntervalJet.constant(driver[1])
							.add(radius.multiply(across))));
			Result point;
			try {
				point = new ConstructiveConeEvaluator<>(cone, IntervalJet.Arithmetic.INSTANCE,
						false, Collections.emptyMap(), fixed).evaluate(traced);
			} catch (ArithmeticException | IllegalArgumentException undefined) {
				return -1;
			}
			if (!(point.getValue() instanceof Point)) {
				return -1;
			}
			Point<IntervalJet> value = (Point<IntervalJet>) point.getValue();
			int[] pieceSigns = {value.getX().getSecond().certifiedSign(),
					value.getY().getSecond().certifiedSign()};
			for (int component = 0; component < 2; component++) {
				signs[component] = piece == 0 ? pieceSigns[component]
						: signs[component] == pieceSigns[component] ? signs[component] : 0;
			}
		}
		return signs[0] != 0 ? 0 : signs[1] != 0 ? 1 : -1;
	}

	/**
	 * Route re-decision for the location of a degenerate root: the certificate records a
	 * component end or a point component at the parameter, and the exact evaluation of the
	 * slice at the replacement, with constructible premises only, decides whether one of its
	 * predicates vanishes there. The isolation and the multiplicity of the root are other closed
	 * decisions.
	 */
	private static Record rootLocation(Prepared prepared, ConstructiveCone cone, int traced,
			ConstructiveConeEvaluator<Element> exact, Builder record) {
		String recorded = closedRootAt(prepared.structure, prepared.parameter);
		if (recorded == null) {
			return record.refuse(Cause.DECISION_NOT_IN_CERTIFICATE, "the certificate records"
					+ " no component end or point component at " + prepared.parameter);
		}
		record.premise("CU2 records " + recorded);
		Result slice = exact.evaluate(traced);
		if (slice.getFailure() == ConstructiveConeEvaluator.Failure.BUDGET_EXHAUSTED) {
			return record.refuse(Cause.BUDGET_EXHAUSTED, slice.getMessage());
		}
		if (exact.dependsOnSubstitute(traced)) {
			return record.refuse(Cause.CLOSED_DECISION_NOT_REDECIDED, "the slice at D* reads a"
					+ " boundary substitute, which is no constructible premise");
		}
		for (Decision decision : exact.getDecisions()) {
			if (decision.getSign() == 0 && cone.getNode(decision.getNode()).isDriverSlice()) {
				record.premise("re-decision: " + decision.getPredicate() + " of slice node "
						+ decision.getNode() + " vanishes exactly at D*");
				return record.transferred(Route.REDECISION);
			}
		}
		if (slice.hasValue() || slice.getFailure() == ConstructiveConeEvaluator.Failure.INVALID) {
			return record.contradict("every predicate of the slice has a strict sign at D*: no"
					+ " degenerate root lies at " + prepared.parameter);
		}
		return record.refuse(Cause.CLOSED_DECISION_NOT_REDECIDED, String.valueOf(slice));
	}

	/**
	 * @return the closed decision of the certificate at the parameter, a point component or
	 *         the enclosed end of a certified component, or {@code null}
	 */
	private static String closedRootAt(LocusExistenceStructure2D structure,
			DriverParameterEnclosure parameter) {
		for (LocusPointComponent2D point : structure.getPointComponents()) {
			if (meetsModuloPeriod(point.getParameterLower(), point.getParameterUpper(),
					parameter)) {
				return "a point component " + point;
			}
		}
		for (ContinuousComponent component : structure.getContinuousValidComponents()) {
			for (LocusBoundary2D boundary : new LocusBoundary2D[] {
					component.getLowerBoundary(), component.getUpperBoundary()}) {
				if (component.getNature().certifies() && boundary.hasEnclosure()
						&& boundary.getKind() == LocusBoundary2D.Kind.CERTIFIED_TRANSITION
						&& meetsModuloPeriod(boundary.getEnclosureLower(),
								boundary.getEnclosureUpper(), parameter)) {
					return "the end " + boundary + " of component " + component.getEvidenceKey();
				}
			}
		}
		return null;
	}

	/** @return whether the interval meets the parameter enclosure, modulo {@code 2 pi} */
	private static boolean meetsModuloPeriod(double lower, double upper,
			DriverParameterEnclosure parameter) {
		// 2 pi lies between the binary64 period and the next value above it.
		double below = 2 * Math.PI;
		double above = Math.nextUp(below);
		double[][] shifted = {{lower, upper},
				{Math.nextDown(lower + below), Math.nextUp(upper + above)},
				{Math.nextDown(lower - above), Math.nextUp(upper - below)}};
		for (double[] interval : shifted) {
			if (!(interval[1] < parameter.getLower() || interval[0] > parameter.getUpper())) {
				return true;
			}
		}
		return false;
	}

	private static boolean readsDriver(Step step) {
		for (Operand operand : step.getOperands()) {
			if (operand.getKind() == OperandKind.NODE && operand.getNode() == 0) {
				return true;
			}
		}
		return false;
	}

	private static ConstantBinding binding(Prepared prepared, int programNode, int operand) {
		for (ConstantBinding binding : prepared.correspondence.getConstants()) {
			if (binding.getProgramNode() == programNode && binding.getOperand() == operand) {
				return binding;
			}
		}
		return null;
	}

	/** @return the exact value of a node computed without a boundary substitute, or null */
	private static Object exactValue(ConstructiveConeEvaluator<Element> exact, int node) {
		Result value = exact.evaluate(node);
		return value.hasValue() && !exact.dependsOnSubstitute(node) ? value.getValue() : null;
	}

	@SuppressWarnings("unchecked")
	private static Object constantJet(Object hull) {
		if (hull instanceof OutwardInterval) {
			return IntervalJet.constant((OutwardInterval) hull);
		}
		if (hull instanceof Point) {
			Point<OutwardInterval> point = (Point<OutwardInterval>) hull;
			return new Point<>(IntervalJet.constant(point.getX()),
					IntervalJet.constant(point.getY()));
		}
		if (hull instanceof Line) {
			Line<OutwardInterval> line = (Line<OutwardInterval>) hull;
			return new Line<>(IntervalJet.constant(line.getA()), IntervalJet.constant(line.getB()),
					IntervalJet.constant(line.getC()), false, false, false, null, null);
		}
		Circle<OutwardInterval> circle = (Circle<OutwardInterval>) hull;
		return new Circle<>(IntervalJet.constant(circle.getCenterX()),
				IntervalJet.constant(circle.getCenterY()),
				IntervalJet.constant(circle.getRadiusSquared()));
	}

	private static IntervalJet constant(double value) {
		return IntervalJet.constant(OutwardInterval.point(value));
	}

	/**
	 * @return {@code null} when a certified continuous component contains the parameter
	 *         enclosure in its interior, otherwise why not: a point component that it meets, a
	 *         certified absence that covers it (whose cause in {@code CU2} becomes a premise), the
	 *         seam at the end of a component, or else the end of a component, a transition, an
	 *         unresolved band or a gap that it meets
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
		boolean periodic = structure.getTopology()
				== LocusExistenceStructure2D.DomainTopology.PERIODIC;
		double domainLower = structure.getCanonicalDomain().getLower();
		double domainUpper = structure.getCanonicalDomain().getUpper();
		boolean meetsSeam = periodic && (lower <= domainLower || upper >= domainUpper);
		boolean seam = false;
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
				if (!periodic && atIncludedDomainEnds(component, candidate)) {
					// Stage A design section 2.3: an exact parameter at an included end of the
					// driver domain belongs to the component; the end is no validity transition.
					record.component = component.getEvidenceKey();
					record.premise("component " + component.getEvidenceKey() + " "
							+ component.getInterval() + " " + component.getNature()
							+ " contains " + parameter + " up to an included end of the domain");
					return null;
				}
			}
			seam |= meetsSeam && endsAtSeam(component, domainLower, domainUpper);
		}
		String absence = certifiedAbsence(structure, lower, upper);
		if (absence != null) {
			record.premise(absence);
			return Cause.NOT_IN_CERTIFIED_COMPONENT;
		}
		return seam ? Cause.SEAM_SIDE_UNDECIDED : Cause.COMPONENT_BOUNDARY_UNDECIDED;
	}

	/**
	 * Whether an end of a component lies at the seam of the periodic domain: its binary64 end
	 * is an end of the canonical domain, or the enclosure of its boundary reaches one. The ideal
	 * seam {@code pi} lies above the binary64 end of the half-open domain.
	 */
	private static boolean endsAtSeam(ContinuousComponent component, double domainLower,
			double domainUpper) {
		return component.getStart() <= domainLower || component.getEnd() >= domainUpper
				|| reachesSeam(component.getLowerBoundary(), domainLower, domainUpper)
				|| reachesSeam(component.getUpperBoundary(), domainLower, domainUpper);
	}

	private static boolean reachesSeam(LocusBoundary2D boundary, double domainLower,
			double domainUpper) {
		return boundary.hasEnclosure() && (boundary.getEnclosureLower() <= domainLower
				|| boundary.getEnclosureUpper() >= domainUpper);
	}

	/**
	 * Whether the coverage of {@code CU2} certifies, without a gap, that the locus does not
	 * exist at any parameter of the enclosure. An enclosure that leaves the canonical domain is
	 * never covered: the seam stays undecided.
	 *
	 * @return the certified absence as a premise, or {@code null} when it does not cover the
	 *         enclosure
	 */
	private static String certifiedAbsence(LocusExistenceStructure2D structure, double lower,
			double upper) {
		LocusInterval2D domain = structure.getCanonicalDomain();
		if (lower < domain.getLower() || upper > domain.getUpper()) {
			return null;
		}
		double reach = lower;
		boolean reachCovered = false;
		List<String> evidence = new ArrayList<>();
		boolean progress = true;
		while (progress && !(reach > upper || reach == upper && reachCovered)) {
			progress = false;
			for (Coverage item : structure.getCoverage()) {
				if (!certifiesAbsence(item)) {
					continue;
				}
				LocusInterval2D interval = item.getInterval();
				boolean starts = interval.getLower() < reach || interval.getLower() == reach
						&& (reachCovered || interval.isLowerClosed());
				boolean extendsReach = interval.getUpper() > reach || !reachCovered
						&& interval.getUpper() == reach && interval.isUpperClosed();
				if (starts && extendsReach) {
					if (interval.getUpper() > reach) {
						reach = interval.getUpper();
						reachCovered = interval.isUpperClosed();
					} else {
						reachCovered = true;
					}
					evidence.add(interval + " " + item.getState() + " " + item.getCause() + " "
							+ item.getNature());
					progress = true;
				}
			}
		}
		return reach > upper || reach == upper && reachCovered
				? "CU2 certified absence: " + String.join(", ", evidence) : null;
	}

	private static boolean certifiesAbsence(Coverage item) {
		// Point components are refused before: an isolated-points interval is absence elsewhere.
		return item.getNature().certifies()
				&& (item.getState() == CoverageState.CERTIFIED_DOES_NOT_EXIST
						|| item.getState() == CoverageState.CERTIFIED_ISOLATED_POINTS);
	}

	/**
	 * @return whether the enclosure lies in the closed component and touches it only at ends
	 *         that are included ends of the driver domain
	 */
	private static boolean atIncludedDomainEnds(ContinuousComponent component,
			double[] candidate) {
		double start = component.getInterval().getLower();
		double end = component.getInterval().getUpper();
		if (candidate[0] < start || candidate[1] > end) {
			return false;
		}
		boolean lowerSide = candidate[0] > start || isIncludedDomainEnd(
				component.getLowerBoundary());
		boolean upperSide = candidate[1] < end || isIncludedDomainEnd(
				component.getUpperBoundary());
		return lowerSide && upperSide;
	}

	private static boolean isIncludedDomainEnd(LocusBoundary2D boundary) {
		return boundary.getKind() == LocusBoundary2D.Kind.DOMAIN_END && boundary.isIncluded();
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
		Map<Integer, Object> hulls = new HashMap<>();
		String failure = hulls(cone, correspondence, hulls);
		if (failure != null) {
			return failure;
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

	/**
	 * The hull of the captured values and of the rigorous enclosure of the constructive value of
	 * every captured constant, by cone node.
	 *
	 * @return {@code null} when every constant has one, otherwise why not
	 */
	private static String hulls(ConstructiveCone cone, Cu2ProgramCorrespondence correspondence,
			Map<Integer, Object> hulls) {
		ConstructiveConeEvaluator<OutwardInterval> constructive =
				ConstructiveConeEvaluator.intervals(cone);
		Map<Integer, List<ConstantBinding>> byNode = new LinkedHashMap<>();
		for (ConstantBinding binding : correspondence.getConstants()) {
			byNode.computeIfAbsent(binding.getConeNode(), node -> new ArrayList<>())
					.add(binding);
		}
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

		private Record contradict(String message) {
			return new Record(Status.CONTRADICTED,
					Cause.CONTRADICTED_IN_CONSTRUCTIVE_CONFIGURATION, message, Route.REDECISION,
					revision, fingerprint, HybridExistenceCertifier2D.CONTRACT, policy, structure,
					component, parameter, premises);
		}
	}

	/** Conditions N1 and N2 with the exact driver parameter, or the refusal of the transfer. */
	private static final class Prepared {
		private final LocusExistenceStructure2D structure;
		private final CertifiedConstructionProgram2D program;
		private final Cu2ProgramCorrespondence correspondence;
		private final Point<Element> point;
		private final DriverParameterEnclosure parameter;
		private final Record refusal;

		private Prepared(LocusExistenceStructure2D structure,
				CertifiedConstructionProgram2D program, Cu2ProgramCorrespondence correspondence,
				Point<Element> point, DriverParameterEnclosure parameter, Record refusal) {
			this.structure = structure;
			this.program = program;
			this.correspondence = correspondence;
			this.point = point;
			this.parameter = parameter;
			this.refusal = refusal;
		}

		private static Prepared refused(Record refusal) {
			return new Prepared(null, null, null, null, null, refusal);
		}
	}
}
