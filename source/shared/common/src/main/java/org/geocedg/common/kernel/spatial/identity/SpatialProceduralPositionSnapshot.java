/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.ConstructionElement;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoElementND;

/** Operation-entry evidence for the A4/P3-R1 procedural position. */
final class SpatialProceduralPositionSnapshot {
	private final Construction construction;
	private final ConstructionElement oldTarget;
	private final int oldSlot;
	private final int entryElementCount;
	private final List<Anchor> leftAnchors;
	private final List<Anchor> rightAnchors;
	private final List<ConstructionElement> entryUnaffectedOrder;

	private SpatialProceduralPositionSnapshot(Construction construction,
			ConstructionElement oldTarget, int oldSlot, int entryElementCount,
			List<Anchor> leftAnchors, List<Anchor> rightAnchors,
			List<ConstructionElement> entryUnaffectedOrder) {
		this.construction = construction;
		this.oldTarget = oldTarget;
		this.oldSlot = oldSlot;
		this.entryElementCount = entryElementCount;
		this.leftAnchors = Collections.unmodifiableList(
				new ArrayList<>(leftAnchors));
		this.rightAnchors = Collections.unmodifiableList(
				new ArrayList<>(rightAnchors));
		this.entryUnaffectedOrder = Collections.unmodifiableList(
				new ArrayList<>(entryUnaffectedOrder));
	}

	static SpatialProceduralPositionSnapshot capture(
			SpatialIdentityRegistry registry,
			SpatialRedefineOutputGroup<SpatialRedefinePersistedOutput> target) {
		Objects.requireNonNull(registry);
		Objects.requireNonNull(target);
		ConstructionElement targetElement = commonOutputElement(target.getOutputs());
		Construction construction = targetElement.getConstruction();
		int targetIndex = targetElement.getConstructionIndex();
		if (targetIndex < 0) {
			throw new IllegalArgumentException(
					"Redefine target group has no construction-list position");
		}
		ArrayList<Anchor> left = new ArrayList<>();
		ArrayList<Anchor> right = new ArrayList<>();
		ArrayList<ConstructionElement> unaffected = new ArrayList<>();
		for (int index = 0; index < construction.steps(); index++) {
			ConstructionElement element = construction.getConstructionElement(index);
			if (element == targetElement) {
				continue;
			}
			unaffected.add(element);
			Anchor anchor = Anchor.capture(registry, element);
			if (index < targetIndex) {
				left.add(anchor);
			} else {
				right.add(anchor);
			}
		}
		Collections.reverse(left);
		return new SpatialProceduralPositionSnapshot(construction, targetElement,
				targetIndex, construction.steps(), left, right, unaffected);
	}

	/**
	 * Assesses the P3-R1 destination while the entry construction and candidate
	 * producer are both available. This method does not move or publish anything.
	 */
	Plan assess(Iterable<GeoElement> candidateOutputs) {
		ConstructionElement candidate = commonGeoElement(candidateOutputs);
		if (candidate.getConstruction() != construction
				|| !entryOrderIsStillPresent()) {
			return Plan.unavailable("Operation-entry procedural evidence is stale");
		}
		int lower = candidate.getMinConstructionIndex();
		int upper = oldTarget.getMaxConstructionIndex();
		if (lower < 0 || upper < lower) {
			return Plan.unavailable("Candidate DAG has no legal P3-R1 interval");
		}
		int destination = clamp(oldSlot, lower, upper);
		return new Plan(oldSlot, lower, upper, destination,
				destination != oldSlot, null);
	}

	void enforce(SpatialIdentityRegistry registry,
			Map<String, GeoElement> actualOutputs, PersistentGeoId subject) {
		ConstructionElement target = commonGeoElement(actualOutputs.values());
		Construction currentConstruction = target.getConstruction();
		if (currentConstruction != construction) {
			throw incompatible(subject,
					"Redefined group belongs to another construction");
		}
		int preferred = preferredSurvivingSlot(registry, currentConstruction,
				target, subject);
		int lower = target.getMinConstructionIndex();
		int upper = target.getMaxConstructionIndex();
		if (lower < 0 || upper < lower) {
			throw incompatible(subject,
					"The candidate DAG has no legal construction interval");
		}
		int destination = clamp(preferred, lower, upper);
		int current = target.getConstructionIndex();
		if (destination != current
				&& !currentConstruction.moveInConstructionList(current, destination)) {
			throw incompatible(subject,
					"The host could not apply the minimal DAG-forced relocation");
		}
		validateDagPosition(target, subject);
	}

	private int preferredSurvivingSlot(SpatialIdentityRegistry registry,
			Construction currentConstruction, ConstructionElement target,
			PersistentGeoId subject) {
		if (currentConstruction.steps() == entryElementCount) {
			// The host XML redefine substitutes only the target block. With the same
			// cardinality, its entry slot is a transaction-local procedural coordinate,
			// never a persisted identity or a cross-operation association.
			return Math.min(oldSlot, currentConstruction.steps() - 1);
		}
		if (hasUnresolvableOrdinaryAnchor(currentConstruction, target)) {
			throw incompatible(subject,
					"Ordinary transaction-local anchor survival is not established");
		}
		AnchorResolution left = firstSurviving(registry, leftAnchors,
				currentConstruction, target);
		AnchorResolution right = firstSurviving(registry, rightAnchors,
				currentConstruction, target);
		if (left == null && right == null) {
			if (leftAnchors.isEmpty() && rightAnchors.isEmpty()) {
				return 0;
			}
			throw incompatible(subject,
					"No pre-transaction procedural anchor survived");
		}
		int targetIndex = target.getConstructionIndex();
		// moveInConstructionList removes the target before inserting it at the
		// requested index. Account for that shift when an entry anchor is on the
		// side from which the target moves; otherwise two adjacent surviving
		// anchors would incorrectly appear to leave no insertion slot.
		int intervalLower = left == null ? 0
				: left.index + (targetIndex < left.index ? 0 : 1);
		int intervalUpper = right == null ? currentConstruction.steps() - 1
				: right.index - (targetIndex < right.index ? 1 : 0);
		if (intervalLower > intervalUpper) {
			throw incompatible(subject,
					"Surviving entry anchors do not establish a procedural interval");
		}
		return clamp(target.getConstructionIndex(), intervalLower, intervalUpper);
	}

	private boolean entryOrderIsStillPresent() {
		int previous = -1;
		for (ConstructionElement element : entryUnaffectedOrder) {
			if (element.getConstruction() != construction
					|| element.getConstructionIndex() < 0
					|| element.getConstructionIndex() <= previous) {
				return false;
			}
			previous = element.getConstructionIndex();
		}
		return oldTarget.getConstruction() == construction
				&& oldTarget.getConstructionIndex() >= 0;
	}

	private boolean hasUnresolvableOrdinaryAnchor(Construction currentConstruction,
			ConstructionElement target) {
		for (Anchor anchor : leftAnchors) {
			ConstructionElement local = anchor.resolveLocal(currentConstruction);
			if (anchor.isOrdinary() && local == null && local != target) {
				return true;
			}
		}
		for (Anchor anchor : rightAnchors) {
			ConstructionElement local = anchor.resolveLocal(currentConstruction);
			if (anchor.isOrdinary() && local == null && local != target) {
				return true;
			}
		}
		return false;
	}

	private static int clamp(int value, int lower, int upper) {
		return Math.max(lower, Math.min(value, upper));
	}

	private static void validateDagPosition(ConstructionElement target,
			PersistentGeoId subject) {
		int index = target.getConstructionIndex();
		if (index < target.getMinConstructionIndex()
				|| index > target.getMaxConstructionIndex()) {
			throw incompatible(subject,
					"The redefined group violates its DAG construction bounds");
		}
	}

	private static AnchorResolution firstSurviving(
			SpatialIdentityRegistry registry, List<Anchor> anchors,
			Construction construction, ConstructionElement target) {
		for (Anchor anchor : anchors) {
			ConstructionElement element = anchor.resolve(registry, construction);
			if (element != null && element != target) {
				return new AnchorResolution(element.getConstructionIndex());
			}
		}
		return null;
	}

	private static ConstructionElement commonOutputElement(
			Iterable<? extends SpatialRedefineOutput> outputs) {
		ArrayList<GeoElement> geos = new ArrayList<>();
		for (SpatialRedefineOutput output : outputs) {
			geos.add(output.getGeo());
		}
		return commonGeoElement(geos);
	}

	private static ConstructionElement commonGeoElement(
			Iterable<GeoElement> geos) {
		ConstructionElement common = null;
		for (GeoElement geo : geos) {
			ConstructionElement element = constructionElement(geo);
			if (element == null || common != null && common != element) {
				throw new IllegalArgumentException(
						"Stable-role group does not have one producer");
			}
			common = element;
		}
		if (common == null) {
			throw new IllegalArgumentException("Stable-role group is empty");
		}
		return common;
	}

	private static ConstructionElement constructionElement(GeoElement geo) {
		return geo == null ? null
				: geo.isIndependent() ? geo : geo.getParentAlgorithm();
	}

	private static SpatialIdentityException incompatible(PersistentGeoId subject,
			String message) {
		return new SpatialIdentityException(SpatialIdentityDiagnostic.forSubject(
				SpatialIdentityDiagnostic.Code.REDEFINE_INCOMPATIBLE, message,
				subject));
	}

	/** Frozen result of the non-mutating P3-R1 assessment. */
	static final class Plan {
		private final int oldSlot;
		private final int predecessorBound;
		private final int dependentBound;
		private final int destination;
		private final boolean relocationRequired;
		private final String unavailableReason;

		private Plan(int oldSlot, int predecessorBound, int dependentBound,
				int destination, boolean relocationRequired,
				String unavailableReason) {
			this.oldSlot = oldSlot;
			this.predecessorBound = predecessorBound;
			this.dependentBound = dependentBound;
			this.destination = destination;
			this.relocationRequired = relocationRequired;
			this.unavailableReason = unavailableReason;
		}

		private static Plan unavailable(String reason) {
			return new Plan(-1, -1, -1, -1, false, reason);
		}

		boolean isAvailable() {
			return unavailableReason == null;
		}

		boolean isRelocationRequired() {
			return relocationRequired;
		}

		int getOldSlot() {
			return oldSlot;
		}

		int getPredecessorBound() {
			return predecessorBound;
		}

		int getDependentBound() {
			return dependentBound;
		}

		int getDestination() {
			return destination;
		}

		String getUnavailableReason() {
			return unavailableReason;
		}
	}

	private static final class Anchor {
		private final Map<String, PersistentGeoId> idsByRole;
		private final Map<String, GeoIdentityRecord> recordsByRole;
		private final ConstructionElement localElement;

		private Anchor(Map<String, PersistentGeoId> idsByRole,
				Map<String, GeoIdentityRecord> recordsByRole,
				ConstructionElement localElement) {
			this.idsByRole = Collections.unmodifiableMap(
					new TreeMap<>(idsByRole));
			this.recordsByRole = Collections.unmodifiableMap(
					new TreeMap<>(recordsByRole));
			this.localElement = localElement;
		}

		private static Anchor capture(SpatialIdentityRegistry registry,
				ConstructionElement element) {
			TreeMap<String, PersistentGeoId> ids = new TreeMap<>();
			TreeMap<String, GeoIdentityRecord> records = new TreeMap<>();
			String provider = null;
			int cardinality = -1;
			int outputCount = 0;
			for (GeoElementND value : element.getGeoElements()) {
				outputCount++;
				GeoElement geo = value.toGeoElement();
				PersistentGeoId id = registry.getPersistentGeoId(geo);
				GeoIdentityRecord record = id == null ? null
						: registry.getGeoRecord(id);
				if (record == null) {
					continue;
				}
				if (provider == null) {
					provider = record.getProvider();
					cardinality = record.getOutputCardinality();
				} else if (!provider.equals(record.getProvider())
						|| cardinality != record.getOutputCardinality()) {
					return new Anchor(Collections.emptyMap(),
							Collections.emptyMap(), null);
				}
				if (ids.put(record.getStableOutputRole(), id) != null) {
					return new Anchor(Collections.emptyMap(),
							Collections.emptyMap(), null);
				}
				records.put(record.getStableOutputRole(), record);
			}
			if (ids.isEmpty()) {
				return new Anchor(Collections.emptyMap(),
						Collections.emptyMap(), element);
			}
			return cardinality == ids.size() && ids.size() == outputCount
					? new Anchor(ids, records, null)
					: new Anchor(Collections.emptyMap(),
							Collections.emptyMap(), null);
		}

		private boolean isOrdinary() {
			return idsByRole.isEmpty();
		}

		private ConstructionElement resolveLocal(Construction construction) {
			return localElement != null
					&& localElement.getConstruction() == construction
					&& localElement.getConstructionIndex() >= 0
					? localElement : null;
		}

		private ConstructionElement resolve(SpatialIdentityRegistry registry,
				Construction construction) {
			if (idsByRole.isEmpty()) {
				return resolveLocal(construction);
			}
			ConstructionElement element = null;
			for (Map.Entry<String, PersistentGeoId> entry : idsByRole.entrySet()) {
				GeoElement geo = registry.getGeo(entry.getValue());
				GeoIdentityRecord record = registry.getGeoRecord(entry.getValue());
				ConstructionElement current = constructionElement(geo);
				if (geo == null || record == null
						|| !entry.getKey().equals(record.getStableOutputRole())
						|| !record.equals(recordsByRole.get(entry.getKey()))
						|| record.getOutputCardinality() != idsByRole.size()
						|| current == null || current.getConstruction() != construction
						|| current.getConstructionIndex() < 0
						|| element != null && element != current) {
					return null;
				}
				element = current;
			}
			Anchor current = element == null ? null : capture(registry, element);
			return current != null && idsByRole.equals(current.idsByRole)
					? element : null;
		}
	}

	private static final class AnchorResolution {
		private final int index;

		private AnchorResolution(int index) {
			this.index = index;
		}
	}
}
