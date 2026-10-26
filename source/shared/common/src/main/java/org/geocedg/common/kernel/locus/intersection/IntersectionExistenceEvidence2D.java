/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.Objects;

import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ConstructiveAdmission;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactEvidenceClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactOrder;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ExistenceGrade;

/**
 * Existence evidence of one candidate or finite solution under
 * {@link CertifiedAdmissionPolicy2D#VERSION}, or
 * {@link CertifiedAdmissionPolicy2D#CONSTRUCTIVE_VERSION} under the constructive authority
 * ({@code locus-v2-intersections.md} CU3.2 and CU3.4):
 * its existence grade, the decision configuration that computed it, the certified admission
 * route, the contact evidence class of a candidate without a local sign change, the order of
 * contact of a root that the conflict rule reclassifies and, under the constructive authority, the
 * re-qualification of its admission. It is revision evidence: it is never persisted and never
 * enters a root token, a selector or the graph.
 */
public final class IntersectionExistenceEvidence2D {
	/** Configuration of evidence that no residual enclosure computed. */
	public static final String NOT_COMPUTED = "not-computed";
	/** The constructive configuration of a query whose source or target it does not enclose. */
	public static final String NOT_ENCLOSED_CONSTRUCTIVE = "constructive/not-enclosed";

	private static final IntersectionExistenceEvidence2D LOCALLY_VERIFIED =
			new IntersectionExistenceEvidence2D(ExistenceGrade.LOCALLY_VERIFIED, NOT_COMPUTED,
					false, Double.NaN, null, null, null, null);

	private final ExistenceGrade grade;
	private final String configuration;
	private final boolean certifiedRoute;
	private final double characteristicScale;
	private final ContactEvidenceClass contactEvidence;
	private final String certificate;
	private final ConstructiveAdmission constructiveAdmission;
	private final ContactOrder contactOrder;

	private IntersectionExistenceEvidence2D(ExistenceGrade grade, String configuration,
			boolean certifiedRoute, double characteristicScale,
			ContactEvidenceClass contactEvidence, String certificate,
			ConstructiveAdmission constructiveAdmission, ContactOrder contactOrder) {
		this.grade = Objects.requireNonNull(grade);
		this.configuration = Objects.requireNonNull(configuration);
		this.certifiedRoute = certifiedRoute;
		this.characteristicScale = characteristicScale;
		this.contactEvidence = contactEvidence;
		this.certificate = certificate;
		this.constructiveAdmission = constructiveAdmission;
		this.contactOrder = contactOrder;
		if (certifiedRoute && (grade != ExistenceGrade.EXISTENCE_CERTIFIED
				|| !(characteristicScale > 0) || Double.isInfinite(characteristicScale))) {
			throw new IllegalArgumentException(
					"The certified route needs certified existence and a finite positive scale");
		}
	}

	/** @return the evidence of a solution that only the G8B-R1 predicate verified */
	public static IntersectionExistenceEvidence2D locallyVerified() {
		return LOCALLY_VERIFIED;
	}

	/**
	 * @param grade the existence grade
	 * @param configuration the decision configuration that computed it
	 * @param contactEvidence the contact class of a candidate without a local sign change, or
	 *        {@code null}
	 * @param certificate a description of the certificate, or {@code null}
	 * @return the evidence
	 */
	static IntersectionExistenceEvidence2D of(ExistenceGrade grade, String configuration,
			ContactEvidenceClass contactEvidence, String certificate) {
		return new IntersectionExistenceEvidence2D(grade, configuration, false, Double.NaN,
				contactEvidence, certificate, null, null);
	}

	/**
	 * @param characteristicScale the scale {@code L} of the certified route
	 * @return this evidence, admitted through the certified route
	 */
	IntersectionExistenceEvidence2D withCertifiedRoute(double characteristicScale) {
		return new IntersectionExistenceEvidence2D(grade, configuration, true,
				characteristicScale, contactEvidence, certificate, constructiveAdmission,
				contactOrder);
	}

	/** @return this evidence, its residual condition satisfied by the G8B-R1 threshold */
	IntersectionExistenceEvidence2D withoutCertifiedRoute() {
		return new IntersectionExistenceEvidence2D(grade, configuration, false, Double.NaN,
				contactEvidence, certificate, constructiveAdmission, contactOrder);
	}

	/**
	 * @param admission the re-qualification under the constructive authority
	 * @return this evidence with it
	 */
	IntersectionExistenceEvidence2D withConstructiveAdmission(ConstructiveAdmission admission) {
		return new IntersectionExistenceEvidence2D(grade, configuration, certifiedRoute,
				characteristicScale, contactEvidence, certificate,
				Objects.requireNonNull(admission), contactOrder);
	}

	/**
	 * @param order the order of contact that the conflict rule establishes
	 * @return this evidence with it
	 */
	IntersectionExistenceEvidence2D withContactOrder(ContactOrder order) {
		return new IntersectionExistenceEvidence2D(grade, configuration, certifiedRoute,
				characteristicScale, contactEvidence, certificate, constructiveAdmission,
				Objects.requireNonNull(order));
	}

	/** @return the existence grade */
	public ExistenceGrade getGrade() {
		return grade;
	}

	/** @return the decision configuration, or {@link #NOT_COMPUTED} */
	public String getConfiguration() {
		return configuration;
	}

	/**
	 * @return the policy version: that of the constructive authority for evidence with its
	 *         re-qualification, otherwise that of the legacy policy
	 */
	public String getPolicyVersion() {
		return constructiveAdmission != null ? CertifiedAdmissionPolicy2D.CONSTRUCTIVE_VERSION
				: CertifiedAdmissionPolicy2D.VERSION;
	}

	/**
	 * @return whether the residual condition of G8B-R1 is satisfied by certified existence and
	 *         the coherence of the residual with the characteristic scale, not by the threshold
	 */
	public boolean isCertifiedRoute() {
		return certifiedRoute;
	}

	/** @return the characteristic scale of the certified route, or {@code NaN} */
	public double getCharacteristicScale() {
		return characteristicScale;
	}

	/** @return the contact evidence class, or {@code null} for a transverse candidate */
	public ContactEvidenceClass getContactEvidence() {
		return contactEvidence;
	}

	/** @return a description of the certificate, or {@code null} */
	public String getCertificate() {
		return certificate;
	}

	/** @return the re-qualification under the constructive authority, or {@code null} */
	public ConstructiveAdmission getConstructiveAdmission() {
		return constructiveAdmission;
	}

	/**
	 * @return the order of contact of a root that the conflict rule reclassifies, or
	 *         {@code null}; it never enters a token
	 */
	public ContactOrder getContactOrder() {
		return contactOrder;
	}

	@Override
	public String toString() {
		return grade + " " + configuration + (certifiedRoute ? " certified-route L="
				+ characteristicScale : "") + (contactEvidence == null ? "" : " " + contactEvidence)
				+ (contactOrder == null ? "" : " order " + contactOrder)
				+ (constructiveAdmission == null ? "" : " " + constructiveAdmission)
				+ (certificate == null ? "" : " " + certificate);
	}
}
