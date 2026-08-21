/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Deterministic DXF text plus actual neutral-entity handle mappings. */
public final class DxfEncodingResult {

	/** Actual DXF identity assigned to one neutral entity. */
	public static final class EntityEncoding {
		private final String neutralEntityId;
		private final String handle;
		private final String entityType;

		/**
		 * @param neutralEntityId neutral export entity identifier
		 * @param handle actual hexadecimal DXF handle
		 * @param entityType actual DXF entity type
		 */
		EntityEncoding(String neutralEntityId, String handle,
				String entityType) {
			this.neutralEntityId = requireText(neutralEntityId,
					"neutral entity id");
			this.handle = requireText(handle, "DXF handle");
			this.entityType = requireText(entityType, "DXF entity type");
		}

		public String getNeutralEntityId() {
			return neutralEntityId;
		}

		public String getHandle() {
			return handle;
		}

		public String getEntityType() {
			return entityType;
		}
	}

	private final String dxfText;
	private final Map<String, EntityEncoding> encodedEntities;
	private final GeometryExportModel sourceModel;

	/**
	 * @param dxfText complete deterministic DXF text
	 * @param encodedEntities mappings in emitted entity order
	 */
	DxfEncodingResult(GeometryExportModel sourceModel, String dxfText,
			Map<String, EntityEncoding> encodedEntities) {
		this.sourceModel = require(sourceModel, "source export model");
		if (dxfText == null || dxfText.isEmpty()) {
			throw new IllegalArgumentException("DXF text is required");
		}
		if (encodedEntities == null) {
			throw new IllegalArgumentException("DXF entity mappings are required");
		}
		LinkedHashMap<String, EntityEncoding> copy = new LinkedHashMap<>();
		for (Map.Entry<String, EntityEncoding> entry : encodedEntities.entrySet()) {
			String key = requireText(entry.getKey(), "neutral entity id");
			EntityEncoding value = require(entry.getValue(), "entity encoding");
			if (!key.equals(value.getNeutralEntityId())) {
				throw new IllegalArgumentException(
						"DXF mapping key does not match its neutral entity id");
			}
			copy.put(key, value);
		}
		this.dxfText = dxfText;
		this.encodedEntities = Collections.unmodifiableMap(copy);
	}

	public String getDxfText() {
		return dxfText;
	}

	public Map<String, EntityEncoding> getEncodedEntities() {
		return encodedEntities;
	}

	/**
	 * @param model model presented to a controlled manifest boundary
	 * @return whether this encoding was produced from that exact immutable model
	 */
	public boolean isEncodingOf(GeometryExportModel model) {
		return sourceModel == model;
	}

	/**
	 * @param neutralEntityId neutral entity identifier
	 * @return actual encoding, or null when the entity was not emitted
	 */
	public EntityEncoding getEncoding(String neutralEntityId) {
		return encodedEntities.get(neutralEntityId);
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}

	private static <T> T require(T value, String name) {
		if (value == null) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}
}
