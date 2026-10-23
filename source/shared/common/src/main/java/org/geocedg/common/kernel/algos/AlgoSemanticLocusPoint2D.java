/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.algos;

import java.util.Collections;
import java.util.Objects;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusComponentLineage2D;
import org.geocedg.common.kernel.locus.LocusComponentSelectorResolver2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusDriverDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPrincipalBranchSelector2D;
import org.geocedg.common.kernel.locus.LocusPrincipalBranchState2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D.SeamSide;
import org.geocedg.common.kernel.locus.LocusSemanticAddressState2D;
import org.geocedg.common.kernel.locus.LocusSemanticPointCause2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricPositionBinder2D;
import org.geocedg.common.kernel.locus.metric.LocusSemanticPosition2D;
import org.geocedg.common.kernel.locus.metric.MetricPositionBinding2D;
import org.geocedg.common.kernel.spatial.identity.ConstructionGeoRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
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
	private LocusSemanticAddress2D lastAcceptedAddress;
	private MetricPositionBinding2D positionBinding;
	private LocusSemanticPointCause2D cause = LocusSemanticPointCause2D.INVALID_SOURCE;
	private LocusComponentSelectorResolver2D.Status selectorStatus;
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
		semanticAddress = null;
		positionBinding = null;
		selectorStatus = null;
		if (!branchInput.isDefined()) {
			undefined(LocusSemanticPointCause2D.INVALID_ADDRESS);
			return;
		}
		String branchState = branchInput.getTextString();
		LocusSemanticAddressState2D.Decoded persistedState;
		LocusPrincipalBranchState2D.Decoded principalState;
		try {
			persistedState = LocusSemanticAddressState2D.decode(branchState);
			principalState = persistedState == null
					? LocusPrincipalBranchState2D.decode(branchState) : null;
		} catch (IllegalArgumentException exception) {
			undefined(LocusSemanticPointCause2D.INVALID_ADDRESS);
			return;
		}
		if (persistedState != null && ownsDedicatedInteractionInputs()) {
			enforceDedicatedStatePresentation();
		} else if (principalState != null && ownsDedicatedPrincipalBranchInput()) {
			enforceDedicatedPrincipalBranchPresentation();
		}
		if (!source.isDefined() || !parameterInput.toGeoElement().isDefined()) {
			undefined(LocusSemanticPointCause2D.INVALID_SOURCE);
			return;
		}
		PersistentGeoId sourceId = source.getPersistentLocusId();
		LocusDefinition2D definition = source.getSemanticDefinition();
		double rawParameter = parameterInput.getDouble();
		LocusSemanticAddress2D principalAddress = null;
		if (principalState != null) {
			try {
				principalAddress = LocusPrincipalBranchSelector2D.select(source,
						rawParameter);
			} catch (IllegalArgumentException exception) {
				undefined(LocusSemanticPointCause2D.PRINCIPAL_BRANCH_UNRESOLVED);
				return;
			}
			LocusBranch2D principalBranch = definition == null ? null
					: definition.getBranch(principalAddress.getBranchKey());
			if (principalBranch == null || !principalState.resolves(principalAddress,
					principalBranch, definition.getProvider().getDomainEpsilon())) {
				undefined(LocusSemanticPointCause2D.PRINCIPAL_BRANCH_UNRESOLVED);
				return;
			}
		}
		String branchKey = persistedState != null ? persistedState.getBranchKey()
				: principalState != null ? principalState.getBranchKey() : branchState;
		if (sourceId == null || definition == null) {
			undefined(LocusSemanticPointCause2D.INVALID_SOURCE);
			return;
		}
		if (branchKey == null || branchKey.trim().isEmpty()
				|| !branchKey.equals(branchKey.trim()) || !Double.isFinite(rawParameter)) {
			undefined(LocusSemanticPointCause2D.INVALID_ADDRESS);
			return;
		}
		LocusBranch2D branch = definition.getBranch(branchKey);
		if (branch == null) {
			undefined(LocusSemanticPointCause2D.BRANCH_MISSING);
			return;
		}
		LocusDriverDomainProvider2D provider = definition.getProvider();
		LocusSemanticAddress2D persistedAddress = persistedState == null ? null
				: persistedState.toSemanticAddress(sourceId);
		double canonical = persistedAddress != null
				? persistedAddress.getCanonicalParameter()
				: principalAddress != null ? principalAddress.getCanonicalParameter()
						: provider.canonicalize(rawParameter);
		Long liftValue = persistedAddress != null
				? persistedAddress.getPeriodicLift()
				: principalAddress != null ? principalAddress.getPeriodicLift()
						: periodicLift(provider, rawParameter, canonical);
		if (!Double.isFinite(canonical) || liftValue == null
				|| persistedAddress != null
				&& !persistedAddressMatchesInputs(provider, persistedAddress,
						rawParameter)) {
			undefined(LocusSemanticPointCause2D.INVALID_ADDRESS);
			return;
		}
		String retainedComponentLineage = persistedState != null
				? persistedState.getComponentLineageKey()
				: principalState != null ? principalState.getComponentLineageKey() : null;
		ContinuousComponent component = containingComponent(branch, canonical, provider,
				retainedComponentLineage);
		if (component == null) {
			return;
		}
		long lift = liftValue;
		LocusSemanticAddress2D candidate = persistedAddress != null
				? persistedAddress : principalAddress != null ? principalAddress
				: new LocusSemanticAddress2D(sourceId, provider.getProviderId(),
						branchKey, LocusComponentLineage2D.of(branch.getExistenceStructure(),
								component), canonical,
						lift, seamSide(provider, rawParameter, canonical, lift));
		long rawBits = Double.doubleToLongBits(rawParameter == 0 ? 0 : rawParameter);
		boolean sameAddressInputs = previousRawParameterBits == rawBits
				&& branchKey.equals(previousBranchInput)
				&& sourceId.equals(previousSourceId);
		if (sameAddressInputs && lastAcceptedAddress != null
				&& !lastAcceptedAddress.getComponentLineageKey().equals(
						candidate.getComponentLineageKey())) {
			undefined(LocusSemanticPointCause2D.COMPONENT_CHANGED);
			return;
		}
		MetricPositionBinding2D candidateBinding = binder.bind(
				candidate.toMetricPosition(), definition);
		if (!candidateBinding.isValid()) {
			// The invalid binding is kept: it carries the typed cause of the failure.
			positionBinding = candidateBinding;
			undefined(LocusSemanticPointCause2D.POSITION_UNBOUND);
			return;
		}
		final org.geocedg.common.kernel.locus.LocusPoint2D evaluated = candidateBinding
				.getEvaluatedPoint().get();
		semanticAddress = candidate;
		lastAcceptedAddress = candidate;
		previousRawParameterBits = rawBits;
		previousBranchInput = branchKey;
		previousSourceId = sourceId;
		positionBinding = candidateBinding;
		cause = LocusSemanticPointCause2D.RESOLVED;
		point.setCoords(evaluated.getX(), evaluated.getY(), 1);
	}

	private void undefined(LocusSemanticPointCause2D unresolved) {
		cause = unresolved;
		point.setUndefined();
	}

	@Override
	public void remove() {
		boolean removeDedicatedInputs = ownsDedicatedInteractionInputs();
		boolean removePrincipalInput = ownsDedicatedPrincipalBranchInput();
		GeoElement parameterGeo = parameterInput.toGeoElement();
		super.remove();
		if (removeDedicatedInputs) {
			removeOrphanedDedicatedInput(branchInput);
			removeOrphanedDedicatedInput(parameterGeo);
		}
		if (removePrincipalInput) {
			removeOrphanedDedicatedInput(branchInput);
		}
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

	/**
	 * Restores the presentation contract of identity-owned address inputs after
	 * host XML attachment. The durable role and structural ownership are both
	 * required; an ordinary user input is never hidden merely because its text
	 * resembles an R6 address codec.
	 */
	public void restoreOwnedInputPresentation() {
		if (ownsDedicatedInteractionInputs()) {
			enforceDedicatedStatePresentation();
			hydratePersistedSelectionAfterIdentityAttachment();
		} else if (ownsDedicatedPrincipalBranchInput()) {
			enforceDedicatedPrincipalBranchPresentation();
			hydratePersistedSelectionAfterIdentityAttachment();
		}
	}

	/**
	 * Returns the durable semantic selection last accepted for this point.
	 *
	 * <p>This selection survives a temporary component/topology failure so the
	 * point cannot be retargeted while it is undefined. It is deliberately not
	 * evidence that the address resolves in the current source revision; callers
	 * needing current admissibility must use {@link #getCurrentSemanticAddress()}.
	 *
	 * @return durable last-accepted address, or {@code null} before first acceptance
	 */
	public LocusSemanticAddress2D getSemanticAddress() {
		return lastAcceptedAddress;
	}

	/**
	 * @return address resolved and bound in the current source revision, or
	 *         {@code null} while the point is unresolved
	 */
	public LocusSemanticAddress2D getCurrentSemanticAddress() {
		return semanticAddress;
	}

	/** @return current-bound G7-compatible semantic position, or {@code null} */
	public LocusSemanticPosition2D getSemanticPosition() {
		return semanticAddress == null ? null : semanticAddress.toMetricPosition();
	}

	/** @return current revision binding, including explicit invalid status */
	public MetricPositionBinding2D getMetricPositionBinding() {
		return positionBinding;
	}

	/**
	 * @return typed cause of the state of the point in the current revision,
	 *         {@link LocusSemanticPointCause2D#RESOLVED} while it is defined
	 */
	public LocusSemanticPointCause2D getResolutionCause() {
		return cause;
	}

	/**
	 * @return status of the resolution of the retained component selector in the current
	 *         revision, or {@code null} when the address retains none
	 */
	public LocusComponentSelectorResolver2D.Status getSelectorStatus() {
		return selectorStatus;
	}

	/**
	 * Rebinds the currently admissible address against the source revision.
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

	/**
	 * The certified component of the position (consumption design section 3): a retained
	 * selector resolves only through a demonstrated structural correspondence
	 * ({@code locus-v2-existence-components.md} section 15.6); without one, the parameter
	 * must lie in exactly one certified component. A point component is never selected
	 * (section 15.8). Otherwise the point is undefined with its typed cause.
	 *
	 * @return the component, or {@code null} once the point is undefined
	 */
	private ContinuousComponent containingComponent(LocusBranch2D branch, double parameter,
			LocusDriverDomainProvider2D provider, String retainedComponentLineage) {
		LocusExistenceStructure2D structure = branch.getExistenceStructure();
		if (retainedComponentLineage != null) {
			LocusComponentSelectorResolver2D.Resolution resolution =
					LocusComponentSelectorResolver2D.resolve(retainedComponentLineage,
							structure, Collections.singletonList(branch.getLineage()),
							parameter, provider.getDomainEpsilon());
			selectorStatus = resolution.getStatus();
			if (resolution.getStatus().isResolved()) {
				return resolution.getComponent();
			}
			undefined(LocusSemanticPointCause2D.SELECTOR_UNRESOLVED);
			return null;
		}
		ContinuousComponent unqualified = null;
		for (ContinuousComponent component : structure.getContinuousValidComponents()) {
			if (!component.contains(parameter, provider.getDomainEpsilon())) {
				continue;
			}
			if (unqualified != null) {
				undefined(LocusSemanticPointCause2D.AMBIGUOUS_COMPONENT);
				return null;
			}
			unqualified = component;
		}
		if (unqualified == null) {
			undefined(structure.getPointComponentEnclosing(parameter) != null
					? LocusSemanticPointCause2D.POINT_COMPONENT
					: LocusSemanticPointCause2D.OUTSIDE_COMPONENTS);
		}
		return unqualified;
	}

	private static Long periodicLift(LocusDriverDomainProvider2D provider,
			double raw, double canonical) {
		if (!provider.isPeriodic()) {
			return 0L;
		}
		LocusInterval2D domain = provider.getDeclaredDomain();
		double period = domain.getUpper() - domain.getLower();
		double quotient = Math.floor((raw - domain.getLower()) / period);
		if (!Double.isFinite(quotient) || quotient < Long.MIN_VALUE
				|| quotient > Long.MAX_VALUE) {
			return null;
		}
		long lift = (long) quotient;
		if (!domain.isLowerClosed() && canonical == domain.getUpper()) {
			lift--;
		}
		return lift;
	}

	private static boolean persistedAddressMatchesInputs(
			LocusDriverDomainProvider2D provider,
			LocusSemanticAddress2D address, double rawParameter) {
		// The encoded address owns the exact canonical bits. Re-canonicalizing
		// canonical + lift * period can change those bits at a valid seam crossing.
		// The numeric input is therefore checked as an exact reconstruction of the
		// encoded address; it is never used to re-identify that address by tolerance.
		if (!provider.getProviderId().equals(address.getProviderVersion())
				|| Double.doubleToLongBits(address.getCanonicalParameter())
						!= Double.doubleToLongBits(provider.canonicalize(
								address.getCanonicalParameter()))) {
			return false;
		}
		double expectedRaw = address.getCanonicalParameter();
		if (provider.isPeriodic()) {
			LocusInterval2D domain = provider.getDeclaredDomain();
			double period = domain.getUpper() - domain.getLower();
			expectedRaw += address.getPeriodicLift() * period;
		}
		if (!Double.isFinite(expectedRaw)
				|| Double.doubleToLongBits(normalized(rawParameter))
						!= Double.doubleToLongBits(normalized(expectedRaw))) {
			return false;
		}
		Long reconstructedLift = periodicLift(provider, expectedRaw,
				provider.canonicalize(expectedRaw));
		return reconstructedLift != null
				&& reconstructedLift == address.getPeriodicLift()
				&& seamSide(provider, expectedRaw,
						address.getCanonicalParameter(), reconstructedLift)
						== address.getSeamSide();
	}

	private static double normalized(double value) {
		return value == 0 ? 0 : value;
	}

	private void enforceDedicatedStatePresentation() {
		GeoElement parameterGeo = parameterInput.toGeoElement();
		branchInput.setAuxiliaryObject(true);
		branchInput.setEuclidianVisible(false);
		branchInput.setRestrictedEuclidianVisibility(true);
		parameterGeo.setAuxiliaryObject(true);
		parameterGeo.setEuclidianVisible(false);
		parameterGeo.setRestrictedEuclidianVisibility(true);
	}

	private void enforceDedicatedPrincipalBranchPresentation() {
		branchInput.setAuxiliaryObject(true);
		branchInput.setEuclidianVisible(false);
		branchInput.setRestrictedEuclidianVisibility(true);
	}

	private void hydratePersistedSelectionAfterIdentityAttachment() {
		if (lastAcceptedAddress != null || !branchInput.isDefined()) {
			return;
		}
		LocusSemanticAddressState2D.Decoded persistedState;
		LocusPrincipalBranchState2D.Decoded principalState;
		try {
			persistedState = LocusSemanticAddressState2D.decode(
					branchInput.getTextString());
			principalState = persistedState == null
					? LocusPrincipalBranchState2D.decode(branchInput.getTextString())
					: null;
		} catch (IllegalArgumentException exception) {
			return;
		}
		PersistentGeoId sourceId = source.getPersistentLocusId();
		LocusDefinition2D definition = source.getSemanticDefinition();
		if (persistedState != null && sourceId != null && definition != null
				&& persistedState.hasProviderVersion(
						definition.getProvider().getProviderId())) {
			lastAcceptedAddress = persistedState.toSemanticAddress(sourceId);
		} else if (principalState != null) {
			try {
				LocusSemanticAddress2D current =
						LocusPrincipalBranchSelector2D.select(source,
								parameterInput.getDouble());
				LocusBranch2D branch = definition == null ? null
						: definition.getBranch(current.getBranchKey());
				if (branch != null && principalState.resolves(current, branch,
						definition.getProvider().getDomainEpsilon())) {
					lastAcceptedAddress = current;
				}
			} catch (IllegalArgumentException exception) {
				// Retain unresolved state until the exact selector becomes current again.
			}
		}
	}

	private boolean ownsDedicatedInteractionInputs() {
		return hasInteractionPointRole() && ConstructionGeoRedefineProvider
				.hasDedicatedInteractionPointState(point);
	}

	private boolean hasInteractionPointRole() {
		PersistentGeoId pointId = cons.getSpatialIdentityRegistry()
				.getPersistentGeoId(point);
		GeoIdentityRecord record = pointId == null ? null
				: cons.getSpatialIdentityRegistry().getGeoRecord(pointId);
		return record != null && ConstructionGeoRedefineProvider
				.INTERACTION_POINT_OUTPUT_ROLE.equals(record.getStableOutputRole());
	}

	private boolean ownsDedicatedPrincipalBranchInput() {
		return hasPrincipalBranchPointRole() && ConstructionGeoRedefineProvider
				.hasDedicatedPrincipalBranchState(point);
	}

	private boolean hasPrincipalBranchPointRole() {
		PersistentGeoId pointId = cons.getSpatialIdentityRegistry()
				.getPersistentGeoId(point);
		GeoIdentityRecord record = pointId == null ? null
				: cons.getSpatialIdentityRegistry().getGeoRecord(pointId);
		return record != null && ConstructionGeoRedefineProvider
				.PRINCIPAL_BRANCH_POINT_OUTPUT_ROLE.equals(
						record.getStableOutputRole());
	}

	private static void removeOrphanedDedicatedInput(GeoElement input) {
		if (input.isIndependent() && input.getAlgorithmList().isEmpty()) {
			input.remove();
		}
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
