/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.interaction;

import java.util.Objects;
import java.util.Optional;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;

/** Immutable current-revision inverse semantic-address query. */
public final class LocusPointInteractionQuery2D {
	private final GeoLocusV2 source;
	private final double targetX;
	private final double targetY;
	private final LocusPointInteractionPolicy2D policy;
	private final LocusSemanticAddress2D currentAddress;

	/** Creates a creation query over every source branch/component. */
	public LocusPointInteractionQuery2D(GeoLocusV2 source, double targetX,
			double targetY, LocusPointInteractionPolicy2D policy) {
		this(source, targetX, targetY, policy, null);
	}

	/** Creates a move query constrained by an existing exact address. */
	public LocusPointInteractionQuery2D(GeoLocusV2 source, double targetX,
			double targetY, LocusPointInteractionPolicy2D policy,
			LocusSemanticAddress2D currentAddress) {
		if (!Double.isFinite(targetX) || !Double.isFinite(targetY)) {
			throw new IllegalArgumentException("Interaction target must be finite");
		}
		this.source = Objects.requireNonNull(source);
		this.targetX = targetX == 0 ? 0 : targetX;
		this.targetY = targetY == 0 ? 0 : targetY;
		this.policy = Objects.requireNonNull(policy);
		this.currentAddress = currentAddress;
	}

	public GeoLocusV2 getSource() {
		return source;
	}

	public double getTargetX() {
		return targetX;
	}

	public double getTargetY() {
		return targetY;
	}

	public LocusPointInteractionPolicy2D getPolicy() {
		return policy;
	}

	public Optional<LocusSemanticAddress2D> getCurrentAddress() {
		return Optional.ofNullable(currentAddress);
	}
}
