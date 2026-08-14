/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.OptionalInt;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DomainLocation;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.MultiplicityStatus;

/** Orthogonal contact, multiplicity, domain and regularity classification. */
public final class IntersectionClassification2D {
	private final ContactClass contactClass;
	private final MultiplicityStatus multiplicityStatus;
	private final OptionalInt establishedMultiplicity;
	private final DomainLocation domainLocation;
	private final boolean targetIncludedBoundary;
	private final Regularity sourceRegularity;

	/** Creates one closed classification without numeric sentinels. */
	public IntersectionClassification2D(ContactClass contactClass,
			MultiplicityStatus multiplicityStatus,
			OptionalInt establishedMultiplicity, DomainLocation domainLocation,
			boolean targetIncludedBoundary, Regularity sourceRegularity) {
		this.contactClass = java.util.Objects.requireNonNull(contactClass);
		this.multiplicityStatus =
				java.util.Objects.requireNonNull(multiplicityStatus);
		this.establishedMultiplicity =
				java.util.Objects.requireNonNull(establishedMultiplicity);
		this.domainLocation = java.util.Objects.requireNonNull(domainLocation);
		this.targetIncludedBoundary = targetIncludedBoundary;
		this.sourceRegularity = java.util.Objects.requireNonNull(sourceRegularity);
		if (multiplicityStatus == MultiplicityStatus.ESTABLISHED
				&& (!establishedMultiplicity.isPresent()
						|| establishedMultiplicity.getAsInt() < 1)) {
			throw new IllegalArgumentException(
					"Established multiplicity must be positive");
		}
		if (multiplicityStatus == MultiplicityStatus.NOT_ESTABLISHED
				&& establishedMultiplicity.isPresent()) {
			throw new IllegalArgumentException(
					"Unknown multiplicity cannot carry an integer sentinel");
		}
	}

	public ContactClass getContactClass() {
		return contactClass;
	}

	public MultiplicityStatus getMultiplicityStatus() {
		return multiplicityStatus;
	}

	public OptionalInt getEstablishedMultiplicity() {
		return establishedMultiplicity;
	}

	public DomainLocation getDomainLocation() {
		return domainLocation;
	}

	public boolean isTargetIncludedBoundary() {
		return targetIncludedBoundary;
	}

	public Regularity getSourceRegularity() {
		return sourceRegularity;
	}
}
