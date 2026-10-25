/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.Objects;

import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactEvidenceClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactOrder;

/**
 * The exact contact evidence of a decision configuration at an exact parameter {@code t0} of
 * the driver (PRE-G9B-CU3 increment E3; Stage B design section 9.1, form "one double root at an
 * exact point"): {@code h(t0) = 0} established by structure or by exact arithmetic, and
 * {@code h'(t0) = 0} established by exact arithmetic or by the exact symmetry of a certified fold.
 * It carries the exact parameter, an enclosure of it in binary64, the order of contact and the
 * sign of {@code h''(t0)} when the exact evaluation decides it. The enclosure is outward and is
 * given at the copy of the parameter that the box asks for, so it is never shifted in binary64. It
 * is revision evidence: it is never persisted and never enters a root token, a selector or the
 * graph. A configuration gives it only when both vanishings are established; anything less is no
 * evidence.
 */
public final class ExactContactEvidence2D {
	private final String parameter;
	private final double lower;
	private final double upper;
	private final ContactEvidenceClass evidence;
	private final ContactOrder order;
	private final int curvature;
	private final String description;

	/**
	 * @param parameter the exact parameter, such as {@code 0}, {@code pi/2}, {@code pi} or
	 *        {@code -pi/2}
	 * @param lower lower end of a binary64 enclosure of the parameter
	 * @param upper upper end of that enclosure, not below {@code lower}
	 * @param evidence {@link ContactEvidenceClass#CONTACT_STRUCTURAL} or
	 *        {@link ContactEvidenceClass#CONTACT_EXISTENCE_EXACT}
	 * @param order the order of contact
	 * @param curvature the sign of {@code h''(t0)} that the exact evaluation certifies, or
	 *        {@code 0} when it decides none
	 * @param description a description of the certificates
	 */
	public ExactContactEvidence2D(String parameter, double lower, double upper,
			ContactEvidenceClass evidence, ContactOrder order, int curvature,
			String description) {
		this.parameter = Objects.requireNonNull(parameter);
		if (!(lower <= upper) || Double.isInfinite(lower) || Double.isInfinite(upper)) {
			throw new IllegalArgumentException("The enclosure of the parameter must be finite");
		}
		if (evidence != ContactEvidenceClass.CONTACT_STRUCTURAL
				&& evidence != ContactEvidenceClass.CONTACT_EXISTENCE_EXACT) {
			throw new IllegalArgumentException("Only classes 1 and 2 establish a contact");
		}
		if (curvature < -1 || curvature > 1) {
			throw new IllegalArgumentException("The curvature is a sign");
		}
		this.lower = lower;
		this.upper = upper;
		this.evidence = evidence;
		this.order = Objects.requireNonNull(order);
		this.curvature = curvature;
		this.description = Objects.requireNonNull(description);
	}

	/** @return the exact parameter */
	public String getParameter() {
		return parameter;
	}

	/** @return lower end of the binary64 enclosure of the parameter */
	public double getLower() {
		return lower;
	}

	/** @return upper end of the binary64 enclosure of the parameter */
	public double getUpper() {
		return upper;
	}

	/** @return the contact evidence class, 1 or 2 */
	public ContactEvidenceClass getEvidence() {
		return evidence;
	}

	/** @return the order of contact */
	public ContactOrder getOrder() {
		return order;
	}

	/** @return the certified sign of {@code h''(t0)}, or {@code 0} when it is not decided */
	public int getCurvature() {
		return curvature;
	}

	/** @return a description of the certificates */
	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return evidence + " " + order + " at " + parameter + " [" + lower + "," + upper + "]"
				+ (curvature == 0 ? "" : " h''" + (curvature > 0 ? ">0" : "<0")) + " ("
				+ description + ")";
	}
}
