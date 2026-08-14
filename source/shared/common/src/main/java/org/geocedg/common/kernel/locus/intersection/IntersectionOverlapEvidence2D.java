/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

/** Typed component-level evidence for overlap or infinitely many solutions. */
public final class IntersectionOverlapEvidence2D {
	private final String branchKey;
	private final String componentKey;
	private final String proofMethod;
	private final String diagnostic;

	/** Creates immutable overlap evidence without point sampling. */
	public IntersectionOverlapEvidence2D(String branchKey, String componentKey,
			String proofMethod, String diagnostic) {
		this.branchKey = requireText(branchKey, "Branch key");
		this.componentKey = requireText(componentKey, "Component key");
		this.proofMethod = requireText(proofMethod, "Overlap proof method");
		this.diagnostic = requireText(diagnostic, "Overlap diagnostic");
	}

	public String getBranchKey() {
		return branchKey;
	}

	public String getComponentKey() {
		return componentKey;
	}

	public String getProofMethod() {
		return proofMethod;
	}

	public String getDiagnostic() {
		return diagnostic;
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}
}
