/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.geogebra.common.kernel.geos.GeoElement;

/**
 * Complete caller-supplied POINT association plan for legacy ordinary geos.
 */
public final class SpatialPointMigrationPlan {
	private final String provenanceToken;
	private final Map<SpatialIdentityId, SpatialIdentityRecord> records;
	private final IdentityHashMap<GeoElement, PersistentGeoId> attachments;

	private SpatialPointMigrationPlan(Builder builder) {
		provenanceToken = builder.provenanceToken;
		records = Collections.unmodifiableMap(new LinkedHashMap<>(builder.records));
		attachments = new IdentityHashMap<>(builder.attachments);
	}

	/**
	 * Starts one explicit POINT migration plan.
	 *
	 * @return fluent migration-plan builder
	 */
	public static Builder builder(String provenanceToken) {
		return new Builder(provenanceToken);
	}

	public String getProvenanceToken() {
		return provenanceToken;
	}

	public List<SpatialIdentityRecord> getRecords() {
		return Collections.unmodifiableList(new ArrayList<>(records.values()));
	}

	public Map<GeoElement, PersistentGeoId> getAttachments() {
		return Collections.unmodifiableMap(new IdentityHashMap<>(attachments));
	}

	IdentityHashMap<GeoElement, PersistentGeoId> copyAttachments() {
		return new IdentityHashMap<>(attachments);
	}

	/** Builder that accepts only actual geos and already typed records. */
	public static final class Builder {
		private final String provenanceToken;
		private final Map<SpatialIdentityId, SpatialIdentityRecord> records =
				new LinkedHashMap<>();
		private final IdentityHashMap<GeoElement, PersistentGeoId> attachments =
				new IdentityHashMap<>();

		private Builder(String provenanceToken) {
			this.provenanceToken = SpatialRecordSupport.requireText(provenanceToken,
					"provenanceToken");
		}

		/**
		 * Adds one caller-selected legacy geo and its fresh typed geo record.
		 *
		 * @return this builder
		 */
		public Builder attach(GeoElement geo, GeoIdentityRecord record) {
			GeoElement checkedGeo = Objects.requireNonNull(geo);
			GeoIdentityRecord checkedRecord = Objects.requireNonNull(record);
			putRecord(checkedRecord);
			PersistentGeoId previous = attachments.put(checkedGeo, checkedRecord.getId());
			if (previous != null && !previous.equals(checkedRecord.getId())) {
				throw new IllegalArgumentException(
						"One migration geo cannot receive two durable identities");
			}
			return this;
		}

		/**
		 * Adds one explicit non-geo record from the complete typed association.
		 *
		 * @return this builder
		 */
		public Builder record(SpatialIdentityRecord record) {
			if (record instanceof GeoIdentityRecord) {
				throw new IllegalArgumentException(
						"Migration geo records require an explicit GeoElement attachment");
			}
			putRecord(Objects.requireNonNull(record));
			return this;
		}

		/** @return immutable complete migration plan */
		public SpatialPointMigrationPlan build() {
			if (records.isEmpty() || attachments.isEmpty()) {
				throw new IllegalArgumentException(
						"A POINT migration requires a complete typed graph and attachments");
			}
			return new SpatialPointMigrationPlan(this);
		}

		private void putRecord(SpatialIdentityRecord record) {
			SpatialIdentityRecord previous = records.put(record.getId(), record);
			if (previous != null) {
				throw new IllegalArgumentException(
						"Duplicate migration identity: " + record.getId());
			}
		}
	}
}
