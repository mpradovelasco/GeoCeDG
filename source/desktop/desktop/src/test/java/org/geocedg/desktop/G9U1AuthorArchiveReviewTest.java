/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;

import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.desktop.io.DocumentArchivePreflight;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Byte-exact author evidence; never repaired or resaved in place. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class G9U1AuthorArchiveReviewTest {

	@Test
	void historicalAuthorArchiveRemainsByteExactAndTruthfullyRejected() throws Exception {
		G9U1TestApp.create();
		byte[] archive;
		try (InputStream input = getClass().getResourceAsStream(
				"/org/geocedg/desktop/g9u1-review/TestBasic1.cedg")) {
			assertNotNull(input);
			archive = input.readAllBytes();
		}
		assertEquals(31885, archive.length);
		assertEquals("0791895e1133d4a44ff26c88760cfc951db787c42056a8b5758c79a9b5687be0",
				HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(archive)));
		// DEV dev-archive-03 reproduced this failure before the producing-lifecycle fix.
		// The original archive is evidence, not a silently repaired migration fixture.
		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> DocumentArchivePreflight.validate(archive, new AppConfigGeoCeDG(true)));
		assertTrue(failure.getMessage().contains("dependencies disagree"));
		assertTrue(failure.getMessage().contains("geo:b8933eecf9f7df32d9192b9b2312e198"));
	}
}
