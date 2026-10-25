/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.geocedg.common.kernel.spatial.identity.DocumentAuthorityRecord;
import org.junit.jupiter.api.Test;

/** The versioned document authority policy ({@code locus-v2-constructive-authority.md} section 4). */
final class AuthorityPolicyTest {

	@Test
	void theLegacyPolicyHasNoConstructiveVersions() {
		AuthorityPolicy legacy = AuthorityPolicy.LEGACY_REPRESENTATION;

		assertFalse(legacy.isConstructive());
		assertEquals("legacy-representation-authority/v1", legacy.getAuthority());
		assertNull(legacy.getClassTable());
		assertNull(legacy.getRelaxedAdmission());
	}

	@Test
	void theConstructivePolicyNamesEveryApprovedVersion() {
		AuthorityPolicy constructive = AuthorityPolicy.CONSTRUCTIVE_V1;

		assertTrue(constructive.isConstructive());
		assertEquals("constructive-authority/v1", constructive.getAuthority());
		assertEquals("constructive-class/v1", constructive.getClassTable());
		assertEquals("constructive-relaxed-admission/v1", constructive.getRelaxedAdmission());
		assertEquals("constructive-identity-certificate/v1", constructive.getCertifier());
		assertEquals("cu2-certificate-transfer/v1", constructive.getTransfer());
	}

	@Test
	void onlyTheExactVersionsOfThisBuildAreRead() {
		assertSame(AuthorityPolicy.CONSTRUCTIVE_V1,
				AuthorityPolicy.fromRecord(DocumentAuthorityRecord.CONSTRUCTIVE_V1));
		assertSame(AuthorityPolicy.LEGACY_REPRESENTATION, AuthorityPolicy.fromRecord(null),
				"the legacy authority is the absence of a record");
		assertSame(DocumentAuthorityRecord.CONSTRUCTIVE_V1,
				AuthorityPolicy.CONSTRUCTIVE_V1.toRecord());
		assertNull(AuthorityPolicy.LEGACY_REPRESENTATION.toRecord());
		for (String[] changed : new String[][] {{"authority", "constructive-authority/v2"},
				{"classTable", "constructive-class/v2"},
				{"relaxedAdmission", "constructive-relaxed-admission/v2"},
				{"certifier", "constructive-identity-certificate/v2"},
				{"transfer", "cu2-certificate-transfer/v2"}}) {
			Map<String, String> attributes = attributes();
			attributes.put(changed[0], changed[1]);
			IllegalArgumentException unsupported = assertThrows(IllegalArgumentException.class,
					() -> DocumentAuthorityRecord.read(attributes));
			assertTrue(unsupported.getMessage().contains("Unsupported record semantic version"));
		}
		Map<String, String> missing = attributes();
		missing.remove("transfer");
		assertThrows(IllegalArgumentException.class, () -> DocumentAuthorityRecord.read(missing));
		Map<String, String> unknown = attributes();
		unknown.put("future", "x");
		assertThrows(IllegalArgumentException.class, () -> DocumentAuthorityRecord.read(unknown));
		assertSame(DocumentAuthorityRecord.CONSTRUCTIVE_V1,
				DocumentAuthorityRecord.read(attributes()));
	}

	private static Map<String, String> attributes() {
		Map<String, String> attributes = new LinkedHashMap<>();
		attributes.put("authority", "constructive-authority/v1");
		attributes.put("classTable", "constructive-class/v1");
		attributes.put("relaxedAdmission", "constructive-relaxed-admission/v1");
		attributes.put("certifier", "constructive-identity-certificate/v1");
		attributes.put("transfer", "cu2-certificate-transfer/v1");
		return attributes;
	}
}
