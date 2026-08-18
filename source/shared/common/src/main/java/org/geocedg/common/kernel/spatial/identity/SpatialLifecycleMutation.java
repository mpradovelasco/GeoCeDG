/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.geogebra.common.kernel.geos.GeoElement;

/** Immutable explicit record/attachment mutation prepared against one registry. */
public final class SpatialLifecycleMutation {
	private final SpatialLifecycleOperationKind operationKind;
	private final String provenanceToken;
	private final Map<SpatialIdentityId, SpatialIdentityRecord> expectedRecords;
	private final Map<SpatialIdentityId, SpatialIdentityRecord> createdRecords;
	private final Map<SpatialIdentityId, SpatialIdentityRecord> replacementRecords;
	private final Set<SpatialIdentityId> retiredIds;
	private final IdentityHashMap<GeoElement, PersistentGeoId> attachments;
	private final IdentityHashMap<GeoElement, PersistentGeoId> detachments;
	private final Set<SpatialIdentityId> declaredExternalReferenceIds;
	private final boolean providerValidatedRedefine;
	private final boolean copyPlanValidated;
	private final boolean migrationPlanValidated;
	private final boolean referenceRecoveryValidated;

	private SpatialLifecycleMutation(Builder builder) {
		operationKind = builder.operationKind;
		provenanceToken = builder.provenanceToken;
		expectedRecords = immutableMap(builder.expectedRecords);
		createdRecords = immutableMap(builder.createdRecords);
		replacementRecords = immutableMap(builder.replacementRecords);
		retiredIds = Collections.unmodifiableSet(
				new LinkedHashSet<>(builder.retiredIds));
		attachments = new IdentityHashMap<>(builder.attachments);
		detachments = new IdentityHashMap<>(builder.detachments);
		declaredExternalReferenceIds = Collections.unmodifiableSet(
				new LinkedHashSet<>(builder.declaredExternalReferenceIds));
		providerValidatedRedefine = builder.providerValidatedRedefine;
		copyPlanValidated = builder.copyPlanValidated;
		migrationPlanValidated = builder.migrationPlanValidated;
		referenceRecoveryValidated = builder.referenceRecoveryValidated;
	}

	/**
	 * Starts one explicit lifecycle mutation.
	 *
	 * @return fluent mutation builder
	 */
	public static Builder builder(SpatialLifecycleOperationKind operationKind,
			String provenanceToken) {
		return new Builder(operationKind, provenanceToken);
	}

	public SpatialLifecycleOperationKind getOperationKind() {
		return operationKind;
	}

	public String getProvenanceToken() {
		return provenanceToken;
	}

	public Map<SpatialIdentityId, SpatialIdentityRecord> getExpectedRecords() {
		return expectedRecords;
	}

	public Map<SpatialIdentityId, SpatialIdentityRecord> getCreatedRecords() {
		return createdRecords;
	}

	public Map<SpatialIdentityId, SpatialIdentityRecord> getReplacementRecords() {
		return replacementRecords;
	}

	public Set<SpatialIdentityId> getRetiredIds() {
		return retiredIds;
	}

	public Map<GeoElement, PersistentGeoId> getAttachments() {
		return Collections.unmodifiableMap(new IdentityHashMap<>(attachments));
	}

	public Map<GeoElement, PersistentGeoId> getDetachments() {
		return Collections.unmodifiableMap(new IdentityHashMap<>(detachments));
	}

	public Set<SpatialIdentityId> getDeclaredExternalReferenceIds() {
		return declaredExternalReferenceIds;
	}

	IdentityHashMap<GeoElement, PersistentGeoId> copyAttachments() {
		return new IdentityHashMap<>(attachments);
	}

	IdentityHashMap<GeoElement, PersistentGeoId> copyDetachments() {
		return new IdentityHashMap<>(detachments);
	}

	boolean isProviderValidatedRedefine() {
		return providerValidatedRedefine;
	}

	boolean isCopyPlanValidated() {
		return copyPlanValidated;
	}

	boolean isMigrationPlanValidated() {
		return migrationPlanValidated;
	}

	boolean isReferenceRecoveryValidated() {
		return referenceRecoveryValidated;
	}

	/** Fluent builder whose operands are all explicit typed identities or geos. */
	public static final class Builder {
		private final SpatialLifecycleOperationKind operationKind;
		private final String provenanceToken;
		private final Map<SpatialIdentityId, SpatialIdentityRecord> expectedRecords =
				new LinkedHashMap<>();
		private final Map<SpatialIdentityId, SpatialIdentityRecord> createdRecords =
				new LinkedHashMap<>();
		private final Map<SpatialIdentityId, SpatialIdentityRecord> replacementRecords =
				new LinkedHashMap<>();
		private final Set<SpatialIdentityId> retiredIds = new LinkedHashSet<>();
		private final IdentityHashMap<GeoElement, PersistentGeoId> attachments =
				new IdentityHashMap<>();
		private final IdentityHashMap<GeoElement, PersistentGeoId> detachments =
				new IdentityHashMap<>();
		private final Set<GeoElement> reattachments = Collections.newSetFromMap(
				new IdentityHashMap<GeoElement, Boolean>());
		private final Set<SpatialIdentityId> declaredExternalReferenceIds =
				new LinkedHashSet<>();
		private boolean providerValidatedRedefine;
		private boolean copyPlanValidated;
		private boolean migrationPlanValidated;
		private boolean referenceRecoveryValidated;

		private Builder(SpatialLifecycleOperationKind operationKind,
				String provenanceToken) {
			this.operationKind = Objects.requireNonNull(operationKind);
			this.provenanceToken = SpatialRecordSupport.requireText(provenanceToken,
					"provenanceToken");
		}

		/**
		 * Captures the exact immutable current record expected at preparation.
		 *
		 * @return this builder
		 */
		public Builder expect(SpatialIdentityRecord currentRecord) {
			SpatialIdentityRecord checked = Objects.requireNonNull(currentRecord);
			putUnique(expectedRecords, checked);
			return this;
		}

		/**
		 * Stages a fresh reserved record identity.
		 *
		 * @return this builder
		 */
		public Builder create(SpatialIdentityRecord record) {
			SpatialIdentityRecord checked = Objects.requireNonNull(record);
			requireNoOperationConflict(checked.getId());
			putUnique(createdRecords, checked);
			return this;
		}

		/**
		 * Stages a same-ID immutable replacement and its exact expected source.
		 *
		 * @return this builder
		 */
		public Builder replace(SpatialIdentityRecord currentRecord,
				SpatialIdentityRecord replacement) {
			SpatialIdentityRecord current = Objects.requireNonNull(currentRecord);
			SpatialIdentityRecord candidate = Objects.requireNonNull(replacement);
			if (!current.getId().equals(candidate.getId())) {
				throw new IllegalArgumentException(
						"Lifecycle replacement must preserve the exact typed ID");
			}
			requireNoOperationConflict(candidate.getId());
			putUnique(expectedRecords, current);
			putUnique(replacementRecords, candidate);
			return this;
		}

		/**
		 * Stages explicit retirement and captures the exact expected source.
		 *
		 * @return this builder
		 */
		public Builder retire(SpatialIdentityRecord currentRecord) {
			SpatialIdentityRecord current = Objects.requireNonNull(currentRecord);
			requireNoOperationConflict(current.getId());
			putUnique(expectedRecords, current);
			retiredIds.add(current.getId());
			return this;
		}

		/**
		 * Stages one explicit ordinary-geo attachment.
		 *
		 * @return this builder
		 */
		public Builder attach(GeoElement geo, PersistentGeoId id) {
			GeoElement checkedGeo = Objects.requireNonNull(geo);
			PersistentGeoId checkedId = Objects.requireNonNull(id);
			PersistentGeoId previous = attachments.put(checkedGeo, checkedId);
			if (previous != null && !previous.equals(checkedId)) {
				throw new IllegalArgumentException(
						"One geo cannot stage two lifecycle attachments");
			}
			return this;
		}

		/**
		 * Stages detachment from the exact expected durable geo identity.
		 *
		 * @return this builder
		 */
		public Builder detach(GeoElement geo, PersistentGeoId expectedId) {
			GeoElement checkedGeo = Objects.requireNonNull(geo);
			PersistentGeoId checkedId = Objects.requireNonNull(expectedId);
			PersistentGeoId previous = detachments.put(checkedGeo, checkedId);
			if (previous != null && !previous.equals(checkedId)) {
				throw new IllegalArgumentException(
						"One geo cannot stage two lifecycle detachments");
			}
			return this;
		}

		/**
		 * Stages an explicit same-handle old-ID to fresh-ID switch for true replacement.
		 *
		 * @return this builder
		 */
		public Builder reattach(GeoElement geo, PersistentGeoId expectedOldId,
				PersistentGeoId freshId) {
			if (operationKind != SpatialLifecycleOperationKind.TRUE_REPLACEMENT) {
				throw new IllegalArgumentException(
						"Same-handle reattachment is admitted for true replacement only");
			}
			GeoElement checkedGeo = Objects.requireNonNull(geo);
			PersistentGeoId checkedOldId = Objects.requireNonNull(expectedOldId);
			PersistentGeoId checkedFreshId = Objects.requireNonNull(freshId);
			if (checkedOldId.equals(checkedFreshId)) {
				throw new IllegalArgumentException(
						"True replacement must switch to a distinct fresh geo identity");
			}
			PersistentGeoId previousDetachment = detachments.put(
					checkedGeo, checkedOldId);
			PersistentGeoId previousAttachment = attachments.put(
					checkedGeo, checkedFreshId);
			if (previousDetachment != null
					&& !previousDetachment.equals(checkedOldId)
					|| previousAttachment != null
							&& !previousAttachment.equals(checkedFreshId)) {
				throw new IllegalArgumentException(
						"Conflicting same-handle true-replacement attachment");
			}
			reattachments.add(checkedGeo);
			return this;
		}

		/**
		 * Declares one retained same-construction external dependency explicitly.
		 *
		 * @return this builder
		 */
		public Builder declareExternalReference(SpatialIdentityId id) {
			declaredExternalReferenceIds.add(Objects.requireNonNull(id));
			return this;
		}

		/**
		 * Declares retained same-construction external dependencies explicitly.
		 *
		 * @return this builder
		 */
		public Builder declareExternalReferences(
				Collection<? extends SpatialIdentityId> ids) {
			for (SpatialIdentityId id : Objects.requireNonNull(ids)) {
				declareExternalReference(id);
			}
			return this;
		}

		/** Marks a redefine already sealed by the registry's provider transaction. */
		Builder providerValidatedRedefine() {
			providerValidatedRedefine = true;
			return this;
		}

		Builder validatedCopyPlan() {
			copyPlanValidated = true;
			return this;
		}

		Builder validatedMigrationPlan() {
			migrationPlanValidated = true;
			return this;
		}

		Builder validatedReferenceRecovery() {
			referenceRecoveryValidated = true;
			return this;
		}

		/** @return immutable validated lifecycle mutation */
		public SpatialLifecycleMutation build() {
			for (GeoElement geo : attachments.keySet()) {
				if (detachments.containsKey(geo) && !reattachments.contains(geo)) {
					throw new IllegalArgumentException(
							"A lifecycle mutation cannot attach and detach the same geo");
				}
			}
			for (GeoElement geo : reattachments) {
				PersistentGeoId oldId = detachments.get(geo);
				PersistentGeoId freshId = attachments.get(geo);
				SpatialIdentityRecord oldRecord = expectedRecords.get(oldId);
				SpatialIdentityRecord freshRecord = createdRecords.get(freshId);
				if (operationKind != SpatialLifecycleOperationKind.TRUE_REPLACEMENT
						|| !(oldRecord instanceof GeoIdentityRecord)
						|| !(freshRecord instanceof GeoIdentityRecord)
						|| !retiredIds.contains(oldId) || oldId.equals(freshId)) {
					throw new IllegalArgumentException(
							"True-replacement reattachment requires an explicitly "
									+ "retired old geo identity and created fresh geo identity");
				}
			}
			return new SpatialLifecycleMutation(this);
		}

		private void requireNoOperationConflict(SpatialIdentityId id) {
			if (createdRecords.containsKey(id) || replacementRecords.containsKey(id)
					|| retiredIds.contains(id)) {
				throw new IllegalArgumentException(
						"A lifecycle identity has more than one staged operation: " + id);
			}
		}

		private static void putUnique(
				Map<SpatialIdentityId, SpatialIdentityRecord> target,
				SpatialIdentityRecord record) {
			SpatialIdentityRecord previous = target.put(record.getId(), record);
			if (previous != null
					&& !SpatialRecordXmlCodec.writeRecord(previous).equals(
							SpatialRecordXmlCodec.writeRecord(record))) {
				throw new IllegalArgumentException(
						"Conflicting lifecycle record for identity " + record.getId());
			}
		}
	}

	private static Map<SpatialIdentityId, SpatialIdentityRecord> immutableMap(
			Map<SpatialIdentityId, SpatialIdentityRecord> source) {
		return Collections.unmodifiableMap(new LinkedHashMap<>(source));
	}
}
