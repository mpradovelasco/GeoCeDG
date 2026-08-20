/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;

/**
 * Immutable reconstructible descriptor for one closed G9U0 semantic generator.
 * Cartesian coordinates, labels, render samples and construction order are
 * deliberately absent.
 */
public final class SemanticGeneratorDescriptor1D {
	public static final int SEMANTIC_VERSION = 1;
	public static final String OUTPUT_BRANCH_KEY = "generator.main";

	private final SemanticGeneratorFamily1D family;
	private final PersistentGeoId dependentPointId;
	private final PersistentGeoId stateId;
	private final PersistentGeoId coordinateId;
	private final PersistentGeoId supportId;
	private final PersistentGeoId domainInputId;
	private final PersistentGeoId supportBranchInputId;
	private final String supportBranchKey;
	private final LocusInterval2D declaredDomain;
	private final List<LocusInterval2D> validComponents;
	private final Orientation orientation;
	private final boolean periodic;
	private final double domainEpsilon;
	private final List<PersistentGeoId> dependencyIds;
	private final String dependencyRevisionSignature;

	/** Creates one complete closed-family descriptor. */
	public SemanticGeneratorDescriptor1D(SemanticGeneratorFamily1D family,
			PersistentGeoId dependentPointId,
			PersistentGeoId stateId, PersistentGeoId coordinateId,
			PersistentGeoId supportId, PersistentGeoId domainInputId,
			PersistentGeoId supportBranchInputId, String supportBranchKey,
			LocusInterval2D declaredDomain,
			List<LocusInterval2D> validComponents, Orientation orientation,
			boolean periodic, double domainEpsilon,
			List<PersistentGeoId> dependencyIds,
			String dependencyRevisionSignature) {
		this.family = Objects.requireNonNull(family);
		this.dependentPointId = Objects.requireNonNull(dependentPointId);
		this.stateId = Objects.requireNonNull(stateId);
		this.coordinateId = Objects.requireNonNull(coordinateId);
		this.supportId = supportId;
		this.domainInputId = domainInputId;
		this.supportBranchInputId = supportBranchInputId;
		this.supportBranchKey = canonicalOptionalToken(supportBranchKey);
		this.declaredDomain = Objects.requireNonNull(declaredDomain);
		this.validComponents = checkedComponents(validComponents, declaredDomain);
		this.orientation = Objects.requireNonNull(orientation);
		this.periodic = periodic;
		if (!Double.isFinite(domainEpsilon) || domainEpsilon < 0) {
			throw new IllegalArgumentException(
					"Generator domain epsilon must be finite and nonnegative");
		}
		this.domainEpsilon = domainEpsilon == 0 ? 0 : domainEpsilon;
		this.dependencyIds = checkedIds(dependencyIds);
		this.dependencyRevisionSignature = canonicalRequiredText(
				dependencyRevisionSignature, "dependencyRevisionSignature");
		validateFamilyShape();
		validatePeriodicity();
	}

	private void validateFamilyShape() {
		boolean scalar = family == SemanticGeneratorFamily1D.SCALAR_STATE;
		boolean locusSupport = family
				== SemanticGeneratorFamily1D.LOCUS_BRANCH_POINT;
		if (scalar && (supportId != null || supportBranchKey != null
				|| supportBranchInputId != null)) {
			throw new IllegalArgumentException(
					"A scalar generator has no point-support identity");
		}
		if (scalar != (domainInputId != null)) {
			throw new IllegalArgumentException(
					"Only a scalar generator declares a domain GeoList identity");
		}
		if (!scalar && supportId == null) {
			throw new IllegalArgumentException(
					"A point-state generator requires a durable support identity");
		}
		if (locusSupport != (supportBranchKey != null)
				|| locusSupport != (supportBranchInputId != null)) {
			throw new IllegalArgumentException(
					"Only a locus-support generator declares a source branch key");
		}
		if (!dependencyIds.contains(dependentPointId)
				|| !dependencyIds.contains(stateId)
				|| !dependencyIds.contains(coordinateId)
				|| supportId != null && !dependencyIds.contains(supportId)
				|| domainInputId != null && !dependencyIds.contains(domainInputId)
				|| supportBranchInputId != null
						&& !dependencyIds.contains(supportBranchInputId)) {
			throw new IllegalArgumentException(
					"Generator dependencies omit a required durable input");
		}
	}

	private void validatePeriodicity() {
		if (!periodic) {
			return;
		}
		if (declaredDomain.getUpper() <= declaredDomain.getLower()
				|| validComponents.size() != 1
				|| !validComponents.get(0).equals(declaredDomain)
				|| declaredDomain.isLowerClosed()
						== declaredDomain.isUpperClosed()) {
			throw new IllegalArgumentException(
					"A periodic generator requires one positive half-open component");
		}
	}

	public SemanticGeneratorFamily1D getFamily() {
		return family;
	}

	public String getProviderId() {
		return family.getProviderId();
	}

	public PersistentGeoId getDependentPointId() {
		return dependentPointId;
	}

	public PersistentGeoId getStateId() {
		return stateId;
	}

	public PersistentGeoId getCoordinateId() {
		return coordinateId;
	}

	public PersistentGeoId getSupportId() {
		return supportId;
	}

	public PersistentGeoId getDomainInputId() {
		return domainInputId;
	}

	public PersistentGeoId getSupportBranchInputId() {
		return supportBranchInputId;
	}

	public String getSupportBranchKey() {
		return supportBranchKey;
	}

	public LocusInterval2D getDeclaredDomain() {
		return declaredDomain;
	}

	public List<LocusInterval2D> getValidComponents() {
		return validComponents;
	}

	public Orientation getOrientation() {
		return orientation;
	}

	public boolean isPeriodic() {
		return periodic;
	}

	public double getDomainEpsilon() {
		return domainEpsilon;
	}

	public List<PersistentGeoId> getDependencyIds() {
		return dependencyIds;
	}

	public String getDependencyRevisionSignature() {
		return dependencyRevisionSignature;
	}

	/** @return deterministic content signature, never an identity allocator */
	public String getSemanticSignature() {
		StringBuilder signature = new StringBuilder(family.getProviderId())
				.append('|').append(SEMANTIC_VERSION).append('|')
				.append(dependentPointId.toExternalForm()).append('|')
				.append(stateId.toExternalForm()).append('|')
				.append(coordinateId.toExternalForm()).append('|')
				.append(supportId == null ? "-" : supportId.toExternalForm())
				.append('|').append(domainInputId == null ? "-"
						: domainInputId.toExternalForm())
				.append('|').append(supportBranchInputId == null ? "-"
						: supportBranchInputId.toExternalForm())
				.append('|').append(supportBranchKey == null ? "-" : supportBranchKey)
				.append('|').append(declaredDomain).append('|')
				.append(validComponents).append('|').append(orientation)
				.append("|periodic=").append(periodic).append("|eps=")
				.append(Double.toHexString(domainEpsilon)).append('|');
		for (PersistentGeoId dependency : dependencyIds) {
			signature.append(dependency.toExternalForm()).append(',');
		}
		return signature.append('|').append(dependencyRevisionSignature).toString();
	}

	private static List<PersistentGeoId> checkedIds(List<PersistentGeoId> ids) {
		Objects.requireNonNull(ids);
		if (new HashSet<PersistentGeoId>(ids).size() != ids.size()) {
			throw new IllegalArgumentException("Generator dependency IDs must be unique");
		}
		ArrayList<PersistentGeoId> copy = new ArrayList<>();
		for (PersistentGeoId id : ids) {
			copy.add(Objects.requireNonNull(id));
		}
		Collections.sort(copy);
		return Collections.unmodifiableList(copy);
	}

	private static List<LocusInterval2D> checkedComponents(
			List<LocusInterval2D> components, LocusInterval2D declared) {
		Objects.requireNonNull(components);
		ArrayList<LocusInterval2D> copy = new ArrayList<>();
		for (LocusInterval2D component : components) {
			LocusInterval2D checked = Objects.requireNonNull(component);
			if (checked.getLower() < declared.getLower()
					|| checked.getUpper() > declared.getUpper()) {
				throw new IllegalArgumentException(
						"A valid component lies outside the declared domain");
			}
			copy.add(checked);
		}
		Collections.sort(copy, new Comparator<LocusInterval2D>() {
			@Override
			public int compare(LocusInterval2D first, LocusInterval2D second) {
				int lower = Double.compare(first.getLower(), second.getLower());
				return lower == 0 ? Double.compare(first.getUpper(), second.getUpper())
						: lower;
			}
		});
		for (int index = 1; index < copy.size(); index++) {
			LocusInterval2D previous = copy.get(index - 1);
			LocusInterval2D current = copy.get(index);
			if (current.getLower() < previous.getUpper()
					|| current.getLower() == previous.getUpper()
							&& current.isLowerClosed() && previous.isUpperClosed()) {
				throw new IllegalArgumentException(
						"Valid generator components must be disjoint");
			}
		}
		return Collections.unmodifiableList(copy);
	}

	private static String canonicalOptionalToken(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		if (trimmed.isEmpty() || !trimmed.equals(value)) {
			throw new IllegalArgumentException("Semantic branch key must be canonical");
		}
		return value;
	}

	private static String canonicalRequiredText(String value, String name) {
		if (value == null || value.trim().isEmpty()
				|| !value.equals(value.trim())) {
			throw new IllegalArgumentException(name + " must be canonical");
		}
		return value;
	}
}
