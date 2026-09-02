/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.interaction;

import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;

/** Typed, query-local evidence for one semantic inverse candidate. */
public final class LocusPointInteractionLocalEvidence2D {
	/** Whether a bounded semantic cell establishes one usable local minimum. */
	public enum Status {
		ESTABLISHED,
		NOT_A_LOCAL_MINIMUM,
		NOT_ESTABLISHED,
		SINGULAR
	}

	/** Semantic method used to establish the local result. */
	public enum Method {
		CERTIFIED_AFFINE_PROJECTION,
		POLYNOMIAL_STATIONARY_CELL,
		POLYNOMIAL_ENDPOINT,
		SEMANTIC_EVALUATOR_BRACKET,
		SEMANTIC_COMPONENT_ENDPOINT,
		ISOLATED_SEMANTIC_COMPONENT,
		NOT_ESTABLISHED
	}

	private final Status status;
	private final Method method;
	private final NumericGuarantee numericGuarantee;
	private final String diagnostic;

	/** Creates immutable resolver-owned local-isolation evidence. */
	private LocusPointInteractionLocalEvidence2D(Status status, Method method,
			NumericGuarantee numericGuarantee, String diagnostic) {
		this.status = Objects.requireNonNull(status);
		this.method = Objects.requireNonNull(method);
		this.numericGuarantee = Objects.requireNonNull(numericGuarantee);
		if (diagnostic == null || diagnostic.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"Local interaction evidence needs a diagnostic");
		}
		this.diagnostic = diagnostic;
		if (status == Status.ESTABLISHED && method == Method.NOT_ESTABLISHED) {
			throw new IllegalArgumentException(
					"Established evidence needs a semantic method");
		}
	}

	/** @return established estimated evidence for one bounded semantic cell */
	static LocusPointInteractionLocalEvidence2D established(
			Method method, NumericGuarantee guarantee, String diagnostic) {
		return new LocusPointInteractionLocalEvidence2D(Status.ESTABLISHED,
				method, guarantee, diagnostic);
	}

	/** @return typed nonminimum evidence used only during candidate discovery */
	static LocusPointInteractionLocalEvidence2D notMinimum(String diagnostic) {
		return new LocusPointInteractionLocalEvidence2D(
				Status.NOT_A_LOCAL_MINIMUM, Method.NOT_ESTABLISHED,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED, diagnostic);
	}

	/** @return typed insufficient bounded evidence */
	static LocusPointInteractionLocalEvidence2D unresolved(String diagnostic) {
		return new LocusPointInteractionLocalEvidence2D(Status.NOT_ESTABLISHED,
				Method.NOT_ESTABLISHED,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED, diagnostic);
	}

	/** @return typed singular local source evidence */
	static LocusPointInteractionLocalEvidence2D singular(String diagnostic) {
		return new LocusPointInteractionLocalEvidence2D(Status.SINGULAR,
				Method.NOT_ESTABLISHED,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED, diagnostic);
	}

	public Status getStatus() {
		return status;
	}

	public Method getMethod() {
		return method;
	}

	public NumericGuarantee getNumericGuarantee() {
		return numericGuarantee;
	}

	public String getDiagnostic() {
		return diagnostic;
	}

	/** @return whether one bounded semantic cell established local uniqueness */
	public boolean isEstablished() {
		return status == Status.ESTABLISHED;
	}
}
