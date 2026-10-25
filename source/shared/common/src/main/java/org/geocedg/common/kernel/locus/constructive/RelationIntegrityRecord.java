/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The integrity of one relation in one revision, with its method, its premises, the boundary
 * substitutes and inherited decisions its evidence uses, its typed cause and the policy version
 * ({@code locus-v2-constructive-authority.md} sections 6 and 7). It is revision evidence: it is
 * never persisted and never enters a token, a selector or the construction graph.
 */
public final class RelationIntegrityRecord {

	/** The kind of evidence of a relation. */
	public enum Evidence {
		/** A relation established by provenance, the ideal output existing. */
		PROVENANCE,
		/** A CU2 certificate transferred by margin. */
		TRANSFERRED_BY_MARGIN,
		/** A CU2 certificate transferred by structure. */
		TRANSFERRED_BY_STRUCTURE,
		/** A CU2 certificate transferred by a new rigorous decision. */
		TRANSFERRED_BY_REDECISION,
		/** An exact evaluation whose premises are all constructible. */
		EXACT_EVALUATION,
		/** A rigorous interval decision on enclosures of the ideal values. */
		CERTIFIED_ENCLOSURE,
		/** The relaxed kind {@code SUBSTITUTE_GENERIC}. */
		SUBSTITUTE_GENERIC,
		/** The relaxed kind {@code ENCLOSURE_COMPATIBLE}. */
		ENCLOSURE_COMPATIBLE,
		/** A constructive identity certificate. */
		CONSTRUCTIVE_IDENTITY_CERTIFICATE,
		/** No evidence. */
		NONE
	}

	private final String relation;
	private final RelationIntegrity integrity;
	private final Evidence evidence;
	private final List<RelationIntegrityRecord> premises;
	private final List<String> unestablishedPremises;
	private final String cause;
	private final String policyVersion;

	private RelationIntegrityRecord(String relation, RelationIntegrity integrity,
			Evidence evidence, List<RelationIntegrityRecord> premises,
			List<String> unestablishedPremises, String cause, String policyVersion) {
		this.relation = relation;
		this.integrity = integrity;
		this.evidence = evidence;
		this.premises = premises;
		this.unestablishedPremises = unestablishedPremises;
		this.cause = cause;
		this.policyVersion = policyVersion;
	}

	/**
	 * Builds a record whose integrity is that of its method bounded by the worst of its
	 * premises (section 6). A named unestablished premise, such as a non-constructible boundary
	 * substitute or an inherited decision, is never a strict premise (rule P1): with a relaxed
	 * kind of evidence it bounds the relation to {@code RELAXED} (rule P5); with any other method,
	 * including a refutation, it leaves the relation {@code NOT_ESTABLISHED}.
	 *
	 * @param relation stable description of the relation, for instance {@code P on L}
	 * @param method integrity the method gives with every premise strict
	 * @param evidence kind of evidence
	 * @param premises records of the relations the proof uses
	 * @param unestablishedPremises premises the evidence uses that are not established
	 * @param cause typed cause, or {@code null}
	 * @return the record with its propagated integrity
	 */
	public static RelationIntegrityRecord derive(String relation, RelationIntegrity method,
			Evidence evidence, List<RelationIntegrityRecord> premises,
			List<String> unestablishedPremises, String cause) {
		Objects.requireNonNull(relation);
		Objects.requireNonNull(method);
		Objects.requireNonNull(evidence);
		List<RelationIntegrity> values = new ArrayList<>();
		for (RelationIntegrityRecord premise : premises) {
			values.add(premise.getIntegrity());
		}
		boolean relaxedKind = evidence == Evidence.SUBSTITUTE_GENERIC
				|| evidence == Evidence.ENCLOSURE_COMPATIBLE;
		// A relaxed kind of evidence is never a proof: it never gives STRICT (section 7.3).
		RelationIntegrity effectiveMethod = relaxedKind && method == RelationIntegrity.STRICT
				? RelationIntegrity.RELAXED : method;
		RelationIntegrity propagated;
		if (!unestablishedPremises.isEmpty()
				&& (effectiveMethod == RelationIntegrity.CONTRADICTED || !relaxedKind)) {
			propagated = RelationIntegrity.NOT_ESTABLISHED;
		} else {
			propagated = RelationIntegrity.propagate(effectiveMethod, values);
		}
		return new RelationIntegrityRecord(relation, propagated, evidence,
				Collections.unmodifiableList(new ArrayList<>(premises)),
				Collections.unmodifiableList(new ArrayList<>(unestablishedPremises)), cause,
				AuthorityPolicy.CONSTRUCTIVE_AUTHORITY + "|" + RelationIntegrity.VERSION);
	}

	/**
	 * @param relation stable description of the relation
	 * @param cause typed cause of the absence of evidence
	 * @return a {@code NOT_ESTABLISHED} record without evidence
	 */
	public static RelationIntegrityRecord notEstablished(String relation, String cause) {
		return new RelationIntegrityRecord(Objects.requireNonNull(relation),
				RelationIntegrity.NOT_ESTABLISHED, Evidence.NONE, Collections.emptyList(),
				Collections.emptyList(), Objects.requireNonNull(cause),
				AuthorityPolicy.CONSTRUCTIVE_AUTHORITY + "|" + RelationIntegrity.VERSION);
	}

	/** @return the stable description of the relation */
	public String getRelation() {
		return relation;
	}

	/** @return the propagated integrity */
	public RelationIntegrity getIntegrity() {
		return integrity;
	}

	/** @return the kind of evidence */
	public Evidence getEvidence() {
		return evidence;
	}

	/** @return the records of the premises the proof uses */
	public List<RelationIntegrityRecord> getPremises() {
		return premises;
	}

	/** @return the premises the evidence uses that are not established */
	public List<String> getUnestablishedPremises() {
		return unestablishedPremises;
	}

	/** @return the typed cause, or {@code null} */
	public String getCause() {
		return cause;
	}

	/** @return the versions of the authority and of the integrity contract */
	public String getPolicyVersion() {
		return policyVersion;
	}

	/**
	 * @param minimum integrity a consumer requires
	 * @return {@code null} when the requirement is met, otherwise the typed cause of its failure
	 */
	public String unmetRequirementCause(RelationIntegrity minimum) {
		if (integrity.satisfies(minimum)) {
			return null;
		}
		return "INTEGRITY_REQUIREMENT_NOT_MET: " + relation + " is " + integrity + ", "
				+ minimum + " required" + (cause == null ? "" : " (" + cause + ")");
	}

	@Override
	public String toString() {
		return relation + ": " + integrity + " by " + evidence
				+ (unestablishedPremises.isEmpty() ? ""
						: " with unestablished " + unestablishedPremises)
				+ (cause == null ? "" : " [" + cause + "]");
	}
}
