/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.CompletenessMethod;

/** Independent result-set completeness evidence. */
public final class IntersectionCompletenessEvidence2D {
	private final Completeness completeness;
	private final CompletenessMethod method;
	private final int verifiedRootCount;
	private final List<String> coveredComponentKeys;
	private final List<IntersectionDiagnostic2D> diagnostics;

	/** Creates explicit exhaustive/non-exhaustive evidence. */
	public IntersectionCompletenessEvidence2D(Completeness completeness,
			CompletenessMethod method, int verifiedRootCount,
			List<String> coveredComponentKeys,
			List<IntersectionDiagnostic2D> diagnostics) {
		this.completeness = java.util.Objects.requireNonNull(completeness);
		this.method = java.util.Objects.requireNonNull(method);
		if (verifiedRootCount < 0) {
			throw new IllegalArgumentException("Verified root count cannot be negative");
		}
		this.verifiedRootCount = verifiedRootCount;
		this.coveredComponentKeys = immutableStrings(coveredComponentKeys);
		this.diagnostics = immutableDiagnostics(diagnostics);
	}

	public Completeness getCompleteness() {
		return completeness;
	}

	public CompletenessMethod getMethod() {
		return method;
	}

	public int getVerifiedRootCount() {
		return verifiedRootCount;
	}

	public List<String> getCoveredComponentKeys() {
		return coveredComponentKeys;
	}

	public List<IntersectionDiagnostic2D> getDiagnostics() {
		return diagnostics;
	}

	private static List<String> immutableStrings(List<String> input) {
		java.util.Objects.requireNonNull(input);
		ArrayList<String> copy = new ArrayList<>();
		for (String value : input) {
			if (value == null || value.trim().isEmpty()) {
				throw new IllegalArgumentException("Component key cannot be blank");
			}
			copy.add(value);
		}
		return Collections.unmodifiableList(copy);
	}

	private static List<IntersectionDiagnostic2D> immutableDiagnostics(
			List<IntersectionDiagnostic2D> input) {
		java.util.Objects.requireNonNull(input);
		ArrayList<IntersectionDiagnostic2D> copy = new ArrayList<>();
		for (IntersectionDiagnostic2D diagnostic : input) {
			copy.add(java.util.Objects.requireNonNull(diagnostic));
		}
		return Collections.unmodifiableList(copy);
	}
}
