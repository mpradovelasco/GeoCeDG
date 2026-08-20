/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.algos;

import java.util.Objects;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusComponentLineage2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusDriverDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D.SeamSide;
import org.geocedg.common.kernel.locus.metric.LocusMetricPositionBinder2D;
import org.geocedg.common.kernel.locus.metric.LocusSemanticPosition2D;
import org.geocedg.common.kernel.locus.metric.MetricPositionBinding2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoPointND;

/** Ordinary dynamic point bound only to one exact Locus V2 semantic address. */
public final class AlgoSemanticLocusPoint2D extends AlgoElement {
	private final GeoLocusV2 source;
	private final GeoText branchInput;
	private final GeoNumberValue parameterInput;
	private final GeoPoint point;
	private final LocusMetricPositionBinder2D binder =
			new LocusMetricPositionBinder2D();
	private LocusSemanticAddress2D semanticAddress;
	private MetricPositionBinding2D positionBinding;
	private long previousRawParameterBits = Long.MIN_VALUE;
	private String previousBranchInput;
	private PersistentGeoId previousSourceId;

	/** Creates one exact-address point through normal kernel dependencies. */
	public AlgoSemanticLocusPoint2D(Construction construction, GeoLocusV2 source,
			GeoText branchInput, GeoNumberValue parameterInput) {
		super(construction);
		this.source = Objects.requireNonNull(source);
		this.branchInput = Objects.requireNonNull(branchInput);
		this.parameterInput = Objects.requireNonNull(parameterInput);
		this.point = new GeoPoint(construction);
		setInputOutput();
		compute();
	}

	@Override
	protected void setInputOutput() {
		input = new GeoElement[] {source, branchInput,
				parameterInput.toGeoElement()};
		setOnlyOutput(point);
		setDependencies();
	}

	@Override
	public void compute() {
		positionBinding = null;
		if (!source.isDefined() || !branchInput.isDefined()
				|| !parameterInput.toGeoElement().isDefined()) {
			point.setUndefined();
			return;
		}
		PersistentGeoId sourceId = source.getPersistentLocusId();
		LocusDefinition2D definition = source.getSemanticDefinition();
		String branchKey = branchInput.getTextString();
		double rawParameter = parameterInput.getDouble();
		if (sourceId == null || definition == null || branchKey == null
				|| branchKey.trim().isEmpty() || !branchKey.equals(branchKey.trim())
				|| !Double.isFinite(rawParameter)) {
			point.setUndefined();
			return;
		}
		LocusBranch2D branch = definition.getBranch(branchKey);
		if (branch == null) {
			point.setUndefined();
			return;
		}
		LocusDriverDomainProvider2D provider = definition.getProvider();
		double canonical = provider.canonicalize(rawParameter);
		LocusInterval2D component = containingComponent(branch, canonical, provider);
		if (component == null) {
			point.setUndefined();
			return;
		}
		long lift = periodicLift(provider, rawParameter, canonical);
		SeamSide seam = seamSide(provider, rawParameter, canonical, lift);
		LocusSemanticAddress2D candidate = new LocusSemanticAddress2D(sourceId,
				provider.getProviderId(), branchKey, componentLineage(branchKey, component),
				canonical, lift, seam);
		long rawBits = Double.doubleToLongBits(rawParameter == 0 ? 0 : rawParameter);
		boolean sameAddressInputs = previousRawParameterBits == rawBits
				&& branchKey.equals(previousBranchInput)
				&& sourceId.equals(previousSourceId);
		if (sameAddressInputs && semanticAddress != null
				&& !semanticAddress.getComponentLineageKey().equals(
						candidate.getComponentLineageKey())) {
			point.setUndefined();
			return;
		}
		semanticAddress = candidate;
		previousRawParameterBits = rawBits;
		previousBranchInput = branchKey;
		previousSourceId = sourceId;
		positionBinding = binder.bind(semanticAddress.toMetricPosition(), definition);
		if (!positionBinding.isValid()) {
			point.setUndefined();
			return;
		}
		org.geocedg.common.kernel.locus.LocusPoint2D evaluated = positionBinding
				.getEvaluatedPoint().get();
		point.setCoords(evaluated.getX(), evaluated.getY(), 1);
	}

	public GeoPoint getPoint() {
		return point;
	}

	public GeoLocusV2 getSource() {
		return source;
	}

	public GeoText getBranchInput() {
		return branchInput;
	}

	public GeoNumberValue getParameterInput() {
		return parameterInput;
	}

	/** @return current durable address, or {@code null} while unresolved */
	public LocusSemanticAddress2D getSemanticAddress() {
		return semanticAddress;
	}

	/** @return current G7-compatible semantic position, or {@code null} */
	public LocusSemanticPosition2D getSemanticPosition() {
		return semanticAddress == null ? null : semanticAddress.toMetricPosition();
	}

	/** @return current revision binding, including explicit invalid status */
	public MetricPositionBinding2D getMetricPositionBinding() {
		return positionBinding;
	}

	/**
	 * Rebinds the current durable address against the current source revision.
	 *
	 * @return current binding, or {@code null} while the address is unresolved
	 */
	public MetricPositionBinding2D bindCurrentPosition() {
		if (semanticAddress == null || source.getSemanticDefinition() == null) {
			return null;
		}
		positionBinding = binder.bind(semanticAddress.toMetricPosition(),
				source.getSemanticDefinition());
		return positionBinding;
	}

	/**
	 * Resolves only an explicitly constructed semantic point. Arbitrary coincident
	 * points are rejected rather than assigned a preimage.
	 *
	 * @return exact semantic-point parent algorithm
	 */
	public static AlgoSemanticLocusPoint2D requireSemanticParent(GeoPointND point) {
		AlgoElement parent = Objects.requireNonNull(point).toGeoElement()
				.getParentAlgorithm();
		if (!(parent instanceof AlgoSemanticLocusPoint2D)) {
			throw new IllegalArgumentException(
					"Point has no exact Locus V2 semantic-address parent");
		}
		return (AlgoSemanticLocusPoint2D) parent;
	}

	@Override
	public Commands getClassName() {
		return Commands.Point;
	}

	private static LocusInterval2D containingComponent(LocusBranch2D branch,
			double parameter, LocusDriverDomainProvider2D provider) {
		for (LocusInterval2D component : branch.getValidDomainComponents()) {
			if (component.contains(parameter, provider.getDomainEpsilon())) {
				return component;
			}
		}
		return null;
	}

	private static String componentLineage(String branchKey,
			LocusInterval2D component) {
		return LocusComponentLineage2D.create(branchKey, component);
	}

	private static long periodicLift(LocusDriverDomainProvider2D provider,
			double raw, double canonical) {
		if (!provider.isPeriodic()) {
			return 0;
		}
		LocusInterval2D domain = provider.getDeclaredDomain();
		double period = domain.getUpper() - domain.getLower();
		double quotient = Math.floor((raw - domain.getLower()) / period);
		if (quotient < Long.MIN_VALUE || quotient > Long.MAX_VALUE) {
			throw new IllegalArgumentException("Periodic lift exceeds durable range");
		}
		long lift = (long) quotient;
		if (!domain.isLowerClosed() && canonical == domain.getUpper()) {
			lift--;
		}
		return lift;
	}

	private static SeamSide seamSide(LocusDriverDomainProvider2D provider,
			double raw, double canonical, long lift) {
		if (!provider.isPeriodic()) {
			return SeamSide.NOT_PERIODIC;
		}
		LocusInterval2D domain = provider.getDeclaredDomain();
		if (canonical != domain.getLower() && canonical != domain.getUpper()) {
			return SeamSide.INTERIOR;
		}
		boolean nativeEndpoint = raw == canonical && lift == 0;
		return nativeEndpoint && canonical == domain.getLower()
				? SeamSide.LOWER_APPROACH : SeamSide.UPPER_APPROACH;
	}
}
