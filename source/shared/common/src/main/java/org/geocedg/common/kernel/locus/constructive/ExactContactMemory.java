/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactOrder;

/**
 * The exact decisions of the queries of one public intersection under the constructive
 * authority, kept between its revisions (PRE-G9B-CU3 increment E3): per exact parameter of the
 * driver, the decision of the last state of the cone of the query, and the constructive identity
 * certificate behind it, which a later revision rechecks without symbolic work while its
 * structural key and its cell hold ({@code locus-v2-constructive-authority.md} section 8.2). It is
 * revision evidence: it is never persisted and enters no token, selector or graph.
 */
final class ExactContactMemory {

	/** The decision at one exact parameter. */
	static final class Decision {
		private final ContactOrder order;
		private final int curvature;
		private final int side;
		private final String description;

		/**
		 * @param order the order of contact, or {@code null} when the parameter gives no exact
		 *        contact
		 * @param curvature the certified sign of {@code h''(t0)}, or {@code 0}
		 * @param description a description of the certificates
		 */
		Decision(ContactOrder order, int curvature, String description) {
			this(order, curvature, 0, description);
		}

		/**
		 * @param side the certified side of a regular tangent contact with the canonical
		 *        orientation of the target, or {@code 0} (PRE-G9B-CU3 increment {@code CU3-B2})
		 */
		Decision(ContactOrder order, int curvature, int side, String description) {
			this.order = order;
			this.curvature = curvature;
			this.side = side;
			this.description = Objects.requireNonNull(description);
		}

		/** @return the certified canonical side of a regular tangent contact, or {@code 0} */
		int getSide() {
			return side;
		}

		/** @return the order of contact, or {@code null} when there is no exact contact */
		ContactOrder getOrder() {
			return order;
		}

		/** @return the certified sign of {@code h''(t0)}, or {@code 0} */
		int getCurvature() {
			return curvature;
		}

		/** @return a description of the certificates */
		String getDescription() {
			return description;
		}
	}

	private final Map<Integer, String> digests = new HashMap<>();
	private final Map<Integer, Decision> decisions = new HashMap<>();
	private final Map<Integer, ConstructiveIdentityCertificate> certificates = new HashMap<>();

	/**
	 * @param quarter the exact parameter, in quarter turns
	 * @param digest the state of the cone of the query
	 * @return the decision of that state, or {@code null} when another state took the last one
	 */
	Decision decision(int quarter, String digest) {
		return digest.equals(digests.get(quarter)) ? decisions.get(quarter) : null;
	}

	/**
	 * @param quarter the exact parameter, in quarter turns
	 * @return the certificate of the last decision, or {@code null}
	 */
	ConstructiveIdentityCertificate certificate(int quarter) {
		return certificates.get(quarter);
	}

	/**
	 * @param quarter the exact parameter, in quarter turns
	 * @param digest the state of the cone of the query
	 * @param certificate the certificate behind the decision
	 * @param decision the decision
	 */
	void store(int quarter, String digest, ConstructiveIdentityCertificate certificate,
			Decision decision) {
		digests.put(quarter, Objects.requireNonNull(digest));
		certificates.put(quarter, Objects.requireNonNull(certificate));
		decisions.put(quarter, Objects.requireNonNull(decision));
	}
}
