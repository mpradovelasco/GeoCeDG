/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The resolution of one structural point at one revision: its position with the declared error
 * of the representation, or the typed cause of its absence, with the integrity records of
 * {@code P in L} and {@code P in r} and the transfer record they rely on. It is revision
 * evidence: it never enters a token, a selector or the construction graph, and it is never
 * persisted.
 */
public final class StructuralPointResolution {

	private final boolean defined;
	private final StructuralPointResolver.Cause cause;
	private final String message;
	private final double[] position;
	private final RelationIntegrityRecord membership;
	private final RelationIntegrityRecord incidence;
	private final Cu2CertificateTransfer.Record transfer;
	private final List<String> premises;
	private final String stateDigest;
	private final ExactEvaluationCounters exactCounters;
	private final List<ExactEvaluationCounters> relaxedCounters;
	private final ConstructiveIdentityCertificate certificate;
	private final Cu2CertificateTransfer.Record fold;

	private StructuralPointResolution(boolean defined, StructuralPointResolver.Cause cause,
			String message, double[] position, RelationIntegrityRecord membership,
			RelationIntegrityRecord incidence, Cu2CertificateTransfer.Record transfer,
			List<String> premises, String stateDigest, ExactEvaluationCounters exactCounters,
			List<ExactEvaluationCounters> relaxedCounters,
			ConstructiveIdentityCertificate certificate, Cu2CertificateTransfer.Record fold) {
		this.defined = defined;
		this.cause = cause;
		this.message = message;
		this.position = position;
		this.membership = membership;
		this.incidence = incidence;
		this.transfer = transfer;
		this.premises = Collections.unmodifiableList(new ArrayList<>(premises));
		this.stateDigest = stateDigest;
		this.exactCounters = exactCounters;
		this.relaxedCounters = Collections.unmodifiableList(new ArrayList<>(relaxedCounters));
		this.certificate = certificate;
		this.fold = fold;
	}

	static StructuralPointResolution undefined(StructuralPointResolver.Cause cause,
			String message, String stateDigest) {
		return new StructuralPointResolution(false, cause, message, null, null, null, null,
				Collections.emptyList(), stateDigest, null, Collections.emptyList(), null, null);
	}

	static StructuralPointResolution defined(double[] position,
			RelationIntegrityRecord membership, RelationIntegrityRecord incidence,
			List<String> premises, String stateDigest) {
		return new StructuralPointResolution(true, null, null, position.clone(), membership,
				incidence, null, premises, stateDigest, null, Collections.emptyList(), null, null);
	}

	StructuralPointResolution withTransfer(Cu2CertificateTransfer.Record record) {
		return new StructuralPointResolution(defined, cause, message, position, membership,
				incidence, record, premises, stateDigest, exactCounters, relaxedCounters,
				certificate, fold);
	}

	StructuralPointResolution withIncidence(RelationIntegrityRecord record) {
		return new StructuralPointResolution(defined, cause, message, position, membership,
				record, transfer, premises, stateDigest, exactCounters, relaxedCounters,
				certificate, fold);
	}

	StructuralPointResolution withExactCounters(ExactEvaluationCounters counters) {
		return new StructuralPointResolution(defined, cause, message, position, membership,
				incidence, transfer, premises, stateDigest, counters, relaxedCounters,
				certificate, fold);
	}

	StructuralPointResolution withRelaxedCounters(List<ExactEvaluationCounters> counters) {
		return new StructuralPointResolution(defined, cause, message, position, membership,
				incidence, transfer, premises, stateDigest, exactCounters, counters,
				certificate, fold);
	}

	StructuralPointResolution withCertificate(ConstructiveIdentityCertificate value) {
		return new StructuralPointResolution(defined, cause, message, position, membership,
				incidence, transfer, premises, stateDigest, exactCounters, relaxedCounters,
				value, fold);
	}

	StructuralPointResolution withFold(Cu2CertificateTransfer.Record value) {
		return new StructuralPointResolution(defined, cause, message, position, membership,
				incidence, transfer, premises, stateDigest, exactCounters, relaxedCounters,
				certificate, value);
	}

	/** @return whether the point is defined */
	public boolean isDefined() {
		return defined;
	}

	/** @return the typed cause of an undefined point, or {@code null} */
	public StructuralPointResolver.Cause getCause() {
		return cause;
	}

	/** @return a description of the cause, or {@code null} */
	public String getMessage() {
		return message;
	}

	/** @return the abscissa of the materialized position */
	public double getX() {
		return position == null ? Double.NaN : position[0];
	}

	/** @return the ordinate of the materialized position */
	public double getY() {
		return position == null ? Double.NaN : position[1];
	}

	/**
	 * @return the declared error of the position: half widths of a rigorous enclosure of the
	 *         constructive point in each coordinate, or {@code null} for an undefined point
	 */
	public double[] getDeclaredError() {
		return position == null ? null : new double[] {position[2], position[3]};
	}

	/** @return the integrity record of {@code P in L}, or {@code null} */
	public RelationIntegrityRecord getMembership() {
		return membership;
	}

	/** @return the integrity record of {@code P in r}, or {@code null} */
	public RelationIntegrityRecord getIncidence() {
		return incidence;
	}

	/** @return the transfer record of the membership, or {@code null} */
	public Cu2CertificateTransfer.Record getTransfer() {
		return transfer;
	}

	/** @return the premises of the provenance and the support */
	public List<String> getPremises() {
		return premises;
	}

	/**
	 * @return the state digest of the cone the resolution read, or {@code null}; an unchanged
	 *         digest means an unchanged revision of every input
	 */
	public String getStateDigest() {
		return stateDigest;
	}

	/**
	 * @return the deterministic counters of the exact evaluation at the base configuration,
	 *         against {@code cu3-exact-evaluation-budget/v1}, or {@code null} when none ran
	 */
	public ExactEvaluationCounters getExactCounters() {
		return exactCounters;
	}

	/**
	 * @return the counters of the exact evaluations of {@code SUBSTITUTE_GENERIC}, one for the
	 *         base and one for each perturbation, or an empty list when it did not run
	 */
	public List<ExactEvaluationCounters> getRelaxedCounters() {
		return relaxedCounters;
	}

	/**
	 * @return the constructive identity certificate of {@code P in r}, with its order of
	 *         contact, or {@code null} when none was attempted
	 */
	public ConstructiveIdentityCertificate getCertificate() {
		return certificate;
	}

	/**
	 * @return the transfer record of the fold of {@code CU2} at a stationary contact, or
	 *         {@code null} when the contact is not stationary or no certificate holds
	 */
	public Cu2CertificateTransfer.Record getFold() {
		return fold;
	}

	/**
	 * @return the integrity of the point: the worst of the relations its definition relies on,
	 *         {@code NOT_ESTABLISHED} for an undefined point
	 */
	public RelationIntegrity getIntegrity() {
		if (!defined) {
			return RelationIntegrity.NOT_ESTABLISHED;
		}
		List<RelationIntegrity> relations = new ArrayList<>();
		relations.add(membership.getIntegrity());
		if (incidence != null) {
			relations.add(incidence.getIntegrity());
		}
		return RelationIntegrity.propagate(RelationIntegrity.STRICT, relations);
	}

	@Override
	public String toString() {
		return defined ? "(" + getX() + ", " + getY() + ") " + getIntegrity() + " [" + membership
				+ (incidence == null ? "" : "; " + incidence) + "]"
				: "undefined " + cause + ": " + message;
	}
}
