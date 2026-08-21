/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.io.IOException;
import java.nio.file.Path;

/** Narrow filesystem port for deterministic paired-output failure tests. */
interface DxfFileOperations {

	boolean exists(Path path);

	Path createTempFile(Path directory, String prefix, String suffix)
			throws IOException;

	void write(Path path, byte[] bytes) throws IOException;

	byte[] read(Path path) throws IOException;

	boolean isRegularFile(Path path);

	void move(Path source, Path target) throws IOException;

	boolean deleteIfExists(Path path) throws IOException;
}
