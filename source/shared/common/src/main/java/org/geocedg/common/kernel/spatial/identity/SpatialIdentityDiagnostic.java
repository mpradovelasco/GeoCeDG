/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.Objects;

/** Structured evidence from identity validation, loading or lifecycle work. */
public final class SpatialIdentityDiagnostic {
	/** Stable machine-readable categories; messages are explanatory only. */
	public enum Code {
		MALFORMED_ID,
		DUPLICATE_ID,
		CROSS_KIND_TOKEN_REUSE,
		RETIRED_ID_REUSE,
		UNSUPPORTED_VERSION,
		MISSING_REFERENCE,
		INCOMPLETE_CLOSURE,
		GENERIC_MERGE_FORBIDDEN,
		ALLOCATION_EXHAUSTED,
		GEO_ALREADY_PARTICIPATING,
		GEO_ATTACHMENT_MISSING,
		GEO_NOT_SERIALIZABLE,
		RECORD_KIND_MISMATCH,
		REDEFINE_CONTEXT_MISSING,
		REDEFINE_PROVIDER_MISSING,
		REDEFINE_INCOMPATIBLE,
		REDEFINE_REJECTED,
		TRANSACTION_STATE,
		LIFECYCLE_RUNTIME_MISSING,
		LIFECYCLE_STALE_SOURCE,
		LIFECYCLE_CREATE_NOT_RESERVED,
		LIFECYCLE_SCOPE_VIOLATION,
		LIFECYCLE_REVISION_MISMATCH,
		LIFECYCLE_RECIPROCAL_MISMATCH,
		LIFECYCLE_NEW_BROKEN_REFERENCE,
		LIFECYCLE_EXTERNAL_REFERENCE,
		LIFECYCLE_RUNTIME_FAILURE,
		MIGRATION_ALREADY_ASSOCIATED,
		MIGRATION_INCOMPLETE,
		MACRO_MAP_INCOMPLETE,
		MALFORMED_RECORD
	}

	private final Code code;
	private final String message;
	private final SpatialIdentityId subject;
	private final SpatialIdentityId reference;

	/** Creates immutable structured diagnostic evidence. */
	public SpatialIdentityDiagnostic(Code code, String message,
			SpatialIdentityId subject, SpatialIdentityId reference) {
		this.code = Objects.requireNonNull(code);
		this.message = Objects.requireNonNull(message);
		this.subject = subject;
		this.reference = reference;
	}

	/** @return a diagnostic without identity operands */
	public static SpatialIdentityDiagnostic of(Code code, String message) {
		return new SpatialIdentityDiagnostic(code, message, null, null);
	}

	/** @return a diagnostic attached to one subject identity */
	public static SpatialIdentityDiagnostic forSubject(Code code, String message,
			SpatialIdentityId subject) {
		return new SpatialIdentityDiagnostic(code, message, subject, null);
	}

	/** @return a diagnostic attached to a subject and referenced identity */
	public static SpatialIdentityDiagnostic forReference(Code code, String message,
			SpatialIdentityId subject, SpatialIdentityId reference) {
		return new SpatialIdentityDiagnostic(code, message, subject, reference);
	}

	public Code getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public SpatialIdentityId getSubject() {
		return subject;
	}

	public SpatialIdentityId getReference() {
		return reference;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(code.name()).append(": ").append(message);
		if (subject != null) {
			builder.append(" [subject=").append(subject).append(']');
		}
		if (reference != null) {
			builder.append(" [reference=").append(reference).append(']');
		}
		return builder.toString();
	}
}
