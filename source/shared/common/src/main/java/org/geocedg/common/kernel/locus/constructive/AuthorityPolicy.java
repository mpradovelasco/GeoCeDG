/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.Objects;

import org.geocedg.common.kernel.spatial.identity.DocumentAuthorityRecord;
import org.geogebra.common.kernel.Construction;

/**
 * The geometric authority policy of one document ({@code locus-v2-constructive-authority.md}
 * section 4). A document without a persisted policy record is governed by
 * {@link #LEGACY_REPRESENTATION}; it adopts {@link #CONSTRUCTIVE_V1} only through the explicit
 * adoption event. One document has one authority, never chosen per relation, object or command.
 */
public final class AuthorityPolicy {

	/** Authority of documents without the record: the contracts approved before ADR 0040. */
	public static final String LEGACY_AUTHORITY = "legacy-representation-authority/v1";
	/** The single constructive authority of ADR 0040. */
	public static final String CONSTRUCTIVE_AUTHORITY =
			DocumentAuthorityRecord.CONSTRUCTIVE_AUTHORITY;
	/** Class table of version 1. */
	public static final String CLASS_TABLE = DocumentAuthorityRecord.CLASS_TABLE;
	/** Relaxed admission policy of version 1. */
	public static final String RELAXED_ADMISSION = DocumentAuthorityRecord.RELAXED_ADMISSION;
	/** Constructive identity certificate of version 1. */
	public static final String CERTIFIER = DocumentAuthorityRecord.CERTIFIER;
	/** Transfer of CU2 certificates of version 1. */
	public static final String TRANSFER = DocumentAuthorityRecord.TRANSFER;

	/** The policy of every document that has not adopted the constructive authority. */
	public static final AuthorityPolicy LEGACY_REPRESENTATION = new AuthorityPolicy(
			LEGACY_AUTHORITY, null, null, null, null);
	/** The constructive authority with the versions approved on 2026-10-25. */
	public static final AuthorityPolicy CONSTRUCTIVE_V1 = new AuthorityPolicy(
			CONSTRUCTIVE_AUTHORITY, CLASS_TABLE, RELAXED_ADMISSION, CERTIFIER, TRANSFER);

	private final String authority;
	private final String classTable;
	private final String relaxedAdmission;
	private final String certifier;
	private final String transfer;

	private AuthorityPolicy(String authority, String classTable, String relaxedAdmission,
			String certifier, String transfer) {
		this.authority = authority;
		this.classTable = classTable;
		this.relaxedAdmission = relaxedAdmission;
		this.certifier = certifier;
		this.transfer = transfer;
	}

	/**
	 * @param construction a construction
	 * @return the policy of its document, from its persisted record
	 */
	public static AuthorityPolicy of(Construction construction) {
		return fromRecord(Objects.requireNonNull(construction).getSpatialIdentityRegistry()
				.getDocumentAuthorityRecord());
	}

	/**
	 * Resolves a persisted record. Only the exact versions of this build exist as records; the
	 * absence of a record is the legacy policy.
	 *
	 * @param record the persisted record, or {@code null}
	 * @return the policy
	 * @throws IllegalArgumentException for any record other than the one of this build
	 */
	public static AuthorityPolicy fromRecord(DocumentAuthorityRecord record) {
		if (record == null) {
			return LEGACY_REPRESENTATION;
		}
		if (!DocumentAuthorityRecord.CONSTRUCTIVE_V1.equals(record)) {
			throw new IllegalArgumentException("Unsupported authority policy record");
		}
		return CONSTRUCTIVE_V1;
	}

	/** @return the persisted record of this policy, or {@code null} for the legacy policy */
	public DocumentAuthorityRecord toRecord() {
		return isConstructive() ? DocumentAuthorityRecord.CONSTRUCTIVE_V1 : null;
	}

	/** @return whether the document is governed by the constructive authority */
	public boolean isConstructive() {
		return CONSTRUCTIVE_AUTHORITY.equals(authority);
	}

	/** @return the authority version name */
	public String getAuthority() {
		return authority;
	}

	/** @return the class table version, or {@code null} under the legacy authority */
	public String getClassTable() {
		return classTable;
	}

	/** @return the relaxed admission version, or {@code null} under the legacy authority */
	public String getRelaxedAdmission() {
		return relaxedAdmission;
	}

	/** @return the certifier version, or {@code null} under the legacy authority */
	public String getCertifier() {
		return certifier;
	}

	/** @return the transfer version, or {@code null} under the legacy authority */
	public String getTransfer() {
		return transfer;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AuthorityPolicy)) {
			return false;
		}
		AuthorityPolicy that = (AuthorityPolicy) other;
		return Objects.equals(authority, that.authority)
				&& Objects.equals(classTable, that.classTable)
				&& Objects.equals(relaxedAdmission, that.relaxedAdmission)
				&& Objects.equals(certifier, that.certifier)
				&& Objects.equals(transfer, that.transfer);
	}

	@Override
	public int hashCode() {
		return Objects.hash(authority, classTable, relaxedAdmission, certifier, transfer);
	}

	@Override
	public String toString() {
		return authority;
	}
}
