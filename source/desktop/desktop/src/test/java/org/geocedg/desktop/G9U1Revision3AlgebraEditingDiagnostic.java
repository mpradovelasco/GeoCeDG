/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Explicit local diagnostic over the ignored, byte-sealed author smoke archive. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class G9U1Revision3AlgebraEditingDiagnostic {

	@Test
	void authorRevision3ExercisesEveryOrdinaryAlgebraEditRoute(
			@TempDir Path directory) throws Exception {
		Path archive = G9U1AlgebraGestureEditingTest.findRepositoryRoot()
				.resolve(G9U1AlgebraGestureEditingTest.REVISION3_PATH);
		G9U1AlgebraGestureEditingTest.assertRevision3Archive(archive, directory);
	}
}
