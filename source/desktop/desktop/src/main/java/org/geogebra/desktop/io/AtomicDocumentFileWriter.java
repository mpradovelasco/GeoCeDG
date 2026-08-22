/*
 * GeoGebra - Dynamic Mathematics for Everyone
 * Copyright (c) GeoGebra GmbH, Altenbergerstr. 69, 4040 Linz, Austria
 * https://www.geogebra.org
 *
 * Modified for GeoCeDG: fail-closed publication of native document files.
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geogebra.desktop.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Writes a complete document beside its target before replacing the target. */
public final class AtomicDocumentFileWriter {

	/** Archive writer used to keep the existing ZIP/XML implementation unchanged. */
	@FunctionalInterface
	public interface ArchiveWriter {
		/** @param path temporary same-directory path */
		void write(Path path) throws IOException;
	}

	private AtomicDocumentFileWriter() {
	}

	/**
	 * @param target final document path
	 * @param writer existing archive writer
	 * @throws IOException when writing or publication fails
	 */
	public static void write(Path target, ArchiveWriter writer) throws IOException {
		Path absoluteTarget = target.toAbsolutePath();
		Path parent = absoluteTarget.getParent();
		if (parent == null) {
			throw new IOException("Document target has no parent directory");
		}
		Path temporary = Files.createTempFile(parent,
				"." + absoluteTarget.getFileName() + "-", ".tmp");
		boolean published = false;
		try {
			writer.write(temporary);
			Files.move(temporary, absoluteTarget,
					StandardCopyOption.ATOMIC_MOVE,
					StandardCopyOption.REPLACE_EXISTING);
			published = true;
		} finally {
			if (!published) {
				Files.deleteIfExists(temporary);
			}
		}
	}
}
