/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.Objects;

/** Base value for a strict, opaque, typed spatial identity. */
public abstract class SpatialIdentityId implements Comparable<SpatialIdentityId> {
	private static final int TOKEN_LENGTH = 32;

	private final SpatialIdentityKind kind;
	private final String token;

	/** Creates a typed ID from an already separated raw token. */
	protected SpatialIdentityId(SpatialIdentityKind kind, String token) {
		this.kind = Objects.requireNonNull(kind);
		validateRawToken(token);
		this.token = token;
	}

	/** @return the globally distinct identity kind */
	public final SpatialIdentityKind getKind() {
		return kind;
	}

	/** @return the 32-character lowercase hexadecimal token without its kind */
	public final String getRawToken() {
		return token;
	}

	/** @return the canonical {@code kind:token} representation */
	public final String toExternalForm() {
		return kind.getPrefix() + ":" + token;
	}

	/**
	 * Parses an ID while preserving its globally declared kind.
	 *
	 * @param externalForm canonical {@code kind:token} text
	 * @return the parsed typed identity
	 */
	public static SpatialIdentityId parse(String externalForm) {
		Objects.requireNonNull(externalForm);
		int separator = externalForm.indexOf(':');
		if (separator <= 0 || separator != externalForm.lastIndexOf(':')) {
			throw new IllegalArgumentException("Malformed spatial identity: " + externalForm);
		}
		SpatialIdentityKind kind = SpatialIdentityKind.fromPrefix(
				externalForm.substring(0, separator));
		String token = externalForm.substring(separator + 1);
		validateRawToken(token);
		switch (kind) {
		case GEO:
			return new PersistentGeoId(token);
		case SPATIAL_OBJECT:
			return new SpatialObjectId(token);
		case PROJECTION_FRAME:
			return new ProjectionFrameId(token);
		case PROJECTION_SYSTEM:
			return new ProjectionSystemId(token);
		case PROJECTION_DIAGRAM_MAP:
			return new ProjectionDiagramMapId(token);
		case PROJECTION_FRAME_RELATION:
			return new ProjectionFrameRelationId(token);
		case PROJECTION_BINDING:
			return new ProjectionBindingId(token);
		default:
			throw new IllegalArgumentException("Unsupported spatial identity kind: " + kind);
		}
	}

	/** Parses an ID and rejects a syntactically valid value of another kind. */
	protected static String parseToken(String externalForm, SpatialIdentityKind expected) {
		SpatialIdentityId parsed = parse(externalForm);
		if (parsed.kind != expected) {
			throw new IllegalArgumentException("Expected " + expected.getPrefix()
					+ " identity, found " + parsed.kind.getPrefix());
		}
		return parsed.token;
	}

	/** Enforces the canonical raw-token spelling. */
	public static void validateRawToken(String token) {
		if (token == null || token.length() != TOKEN_LENGTH) {
			throw new IllegalArgumentException(
					"Spatial identity token must contain 32 lowercase hexadecimal characters");
		}
		for (int i = 0; i < token.length(); i++) {
			char character = token.charAt(i);
			if (!((character >= '0' && character <= '9')
					|| (character >= 'a' && character <= 'f'))) {
				throw new IllegalArgumentException(
						"Spatial identity token is not canonical lowercase hexadecimal");
			}
		}
	}

	@Override
	public final int compareTo(SpatialIdentityId other) {
		int kindComparison = kind.ordinal() - other.kind.ordinal();
		return kindComparison == 0 ? token.compareTo(other.token) : kindComparison;
	}

	@Override
	public final boolean equals(Object other) {
		if (!(other instanceof SpatialIdentityId)) {
			return false;
		}
		SpatialIdentityId identity = (SpatialIdentityId) other;
		return kind == identity.kind && token.equals(identity.token);
	}

	@Override
	public final int hashCode() {
		return 31 * kind.hashCode() + token.hashCode();
	}

	@Override
	public final String toString() {
		return toExternalForm();
	}
}
