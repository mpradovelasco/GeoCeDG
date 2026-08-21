/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Production {@code java.nio} implementation of the Desktop filesystem port. */
final class NioDxfFileOperations implements DxfFileOperations {

	@Override
	public boolean exists(Path path) {
		return Files.exists(path);
	}

	@Override
	public Path createTempFile(Path directory, String prefix, String suffix)
			throws IOException {
		return Files.createTempFile(directory, prefix, suffix);
	}

	@Override
	public void write(Path path, byte[] bytes) throws IOException {
		Files.write(path, bytes);
	}

	@Override
	public byte[] read(Path path) throws IOException {
		return Files.readAllBytes(path);
	}

	@Override
	public void move(Path source, Path target) throws IOException {
		Files.move(source, target);
	}

	@Override
	public boolean isRegularFile(Path path) {
		return Files.isRegularFile(path);
	}

	@Override
	public boolean deleteIfExists(Path path) throws IOException {
		return Files.deleteIfExists(path);
	}
}
