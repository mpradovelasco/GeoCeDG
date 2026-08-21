/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.nio.file.Path;
import java.util.Objects;

/** Successful final state of one safe DXF output operation. */
public final class DxfWriteResult {

	/** Controlled artifact shape written by the operation. */
	public enum ArtifactKind {
		/** One exact-only DXF file. */
		EXACT_DXF,
		/** DXF and its required or requested fidelity manifest. */
		DXF_AND_MANIFEST
	}

	private final ArtifactKind artifactKind;
	private final Path dxfPath;
	private final Path manifestPath;
	private final String dxfSha256;
	private final boolean replacedDxf;
	private final boolean previousManifestExisted;

	DxfWriteResult(ArtifactKind artifactKind, Path dxfPath, Path manifestPath,
			String dxfSha256, boolean replacedDxf,
			boolean previousManifestExisted) {
		this.artifactKind = Objects.requireNonNull(artifactKind);
		this.dxfPath = Objects.requireNonNull(dxfPath);
		this.manifestPath = manifestPath;
		this.dxfSha256 = Objects.requireNonNull(dxfSha256);
		this.replacedDxf = replacedDxf;
		this.previousManifestExisted = previousManifestExisted;
		if ((artifactKind == ArtifactKind.DXF_AND_MANIFEST)
				!= (manifestPath != null)) {
			throw new IllegalArgumentException(
					"Artifact kind and manifest path must agree");
		}
	}

	public ArtifactKind getArtifactKind() {
		return artifactKind;
	}

	public Path getDxfPath() {
		return dxfPath;
	}

	public Path getManifestPath() {
		return manifestPath;
	}

	public String getDxfSha256() {
		return dxfSha256;
	}

	public boolean isDxfReplaced() {
		return replacedDxf;
	}

	public boolean isManifestReplaced() {
		return previousManifestExisted && manifestPath != null;
	}

	/** @return whether an old sidecar was deliberately removed by exact output */
	public boolean isPreviousManifestRemoved() {
		return previousManifestExisted && manifestPath == null;
	}
}
