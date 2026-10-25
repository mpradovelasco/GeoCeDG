/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The persisted geometric authority policy of a document ({@code locus-v2-constructive-authority.md}
 * sections 4 and 5, ADR 0040): one flat record of the {@code geocedgSpatial} section,
 * {@code <authorityPolicy authority="..." classTable="..." relaxedAdmission="..." certifier="..."
 * transfer="..."/>}, written before the identity records. A document without it is governed by
 * {@code legacy-representation-authority/v1}.
 *
 * <p>The section is read strictly: a build that does not know this record rejects it as
 * {@code MALFORMED_RECORD} and loads nothing, so a constructive document is never read under
 * another authority. This build reads only its own exact versions; any other value fails
 * closed.</p>
 */
public final class DocumentAuthorityRecord {

	/** Element name inside {@code geocedgSpatial}. */
	public static final String ELEMENT = "authorityPolicy";
	/** The constructive authority of version 1. */
	public static final String CONSTRUCTIVE_AUTHORITY = "constructive-authority/v1";
	/** The class table of version 1. */
	public static final String CLASS_TABLE = "constructive-class/v1";
	/** The relaxed admission policy of version 1. */
	public static final String RELAXED_ADMISSION = "constructive-relaxed-admission/v1";
	/** The constructive identity certificate of version 1. */
	public static final String CERTIFIER = "constructive-identity-certificate/v1";
	/** The transfer of CU2 certificates of version 1. */
	public static final String TRANSFER = "cu2-certificate-transfer/v1";

	/** The only record this build writes and reads. */
	public static final DocumentAuthorityRecord CONSTRUCTIVE_V1 = new DocumentAuthorityRecord(
			CONSTRUCTIVE_AUTHORITY, CLASS_TABLE, RELAXED_ADMISSION, CERTIFIER, TRANSFER);

	private static final List<String> ATTRIBUTES = Arrays.asList("authority", "classTable",
			"relaxedAdmission", "certifier", "transfer");

	private final String authority;
	private final String classTable;
	private final String relaxedAdmission;
	private final String certifier;
	private final String transfer;

	private DocumentAuthorityRecord(String authority, String classTable,
			String relaxedAdmission, String certifier, String transfer) {
		this.authority = authority;
		this.classTable = classTable;
		this.relaxedAdmission = relaxedAdmission;
		this.certifier = certifier;
		this.transfer = transfer;
	}

	/**
	 * Reads one record after the host XML reader has decoded its attributes.
	 *
	 * @return {@link #CONSTRUCTIVE_V1}
	 * @throws IllegalArgumentException for an unknown or missing attribute, or for any version
	 *         other than those of this build (whose message names an unsupported record
	 *         semantic version)
	 */
	public static DocumentAuthorityRecord read(Map<String, String> attributes) {
		Objects.requireNonNull(attributes);
		for (String name : attributes.keySet()) {
			if (!ATTRIBUTES.contains(name)) {
				throw new IllegalArgumentException("Unknown authorityPolicy attribute: " + name);
			}
		}
		for (String name : ATTRIBUTES) {
			if (attributes.get(name) == null) {
				throw new IllegalArgumentException("Missing authorityPolicy attribute: " + name);
			}
		}
		DocumentAuthorityRecord candidate = new DocumentAuthorityRecord(
				attributes.get("authority"), attributes.get("classTable"),
				attributes.get("relaxedAdmission"), attributes.get("certifier"),
				attributes.get("transfer"));
		if (!CONSTRUCTIVE_V1.equals(candidate)) {
			throw new IllegalArgumentException(
					"Unsupported record semantic version of the authority policy");
		}
		return CONSTRUCTIVE_V1;
	}

	/** @return the canonical self-closing record element */
	public String writeXml() {
		return "<" + ELEMENT + " authority=\"" + authority + "\" classTable=\"" + classTable
				+ "\" relaxedAdmission=\"" + relaxedAdmission + "\" certifier=\"" + certifier
				+ "\" transfer=\"" + transfer + "\"/>";
	}

	/** @return the authority version name */
	public String getAuthority() {
		return authority;
	}

	/** @return the class table version */
	public String getClassTable() {
		return classTable;
	}

	/** @return the relaxed admission version */
	public String getRelaxedAdmission() {
		return relaxedAdmission;
	}

	/** @return the certifier version */
	public String getCertifier() {
		return certifier;
	}

	/** @return the transfer version */
	public String getTransfer() {
		return transfer;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof DocumentAuthorityRecord)) {
			return false;
		}
		DocumentAuthorityRecord that = (DocumentAuthorityRecord) other;
		return authority.equals(that.authority) && classTable.equals(that.classTable)
				&& relaxedAdmission.equals(that.relaxedAdmission)
				&& certifier.equals(that.certifier) && transfer.equals(that.transfer);
	}

	@Override
	public int hashCode() {
		return Objects.hash(authority, classTable, relaxedAdmission, certifier, transfer);
	}

	@Override
	public String toString() {
		return writeXml();
	}
}
