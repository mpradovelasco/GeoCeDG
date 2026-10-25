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
		assertSame(AuthorityPolicy.CONSTRUCTIVE_V1, AuthorityPolicy.fromRecord(
				"constructive-authority/v1", "constructive-class/v1",
				"constructive-relaxed-admission/v1", "constructive-identity-certificate/v1",
				"cu2-certificate-transfer/v1"));
		assertThrows(IllegalArgumentException.class, () -> AuthorityPolicy.fromRecord(
				"constructive-authority/v2", "constructive-class/v1",
				"constructive-relaxed-admission/v1", "constructive-identity-certificate/v1",
				"cu2-certificate-transfer/v1"));
		assertThrows(IllegalArgumentException.class, () -> AuthorityPolicy.fromRecord(
				"constructive-authority/v1", "constructive-class/v2",
				"constructive-relaxed-admission/v1", "constructive-identity-certificate/v1",
				"cu2-certificate-transfer/v1"));
		assertThrows(IllegalArgumentException.class, () -> AuthorityPolicy.fromRecord(
				"legacy-representation-authority/v1", null, null, null, null),
				"the legacy authority is never persisted as a record");
	}
}
