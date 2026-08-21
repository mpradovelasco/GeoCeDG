/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.util.Arrays;

import org.geocedg.common.export.GeometryExportPreflight;

/** Fully serialized and internally consistent DXF output prepared for writing. */
public final class DxfPreparedOutput {

	private final byte[] dxfBytes;
	private final String dxfSha256;
	private final DxfManifestEncoding manifest;
	private final GeometryExportPreflight sourcePreflight;

	private DxfPreparedOutput(byte[] dxfBytes,
			DxfManifestEncoding manifest,
			GeometryExportPreflight sourcePreflight) {
		validateDxf(dxfBytes);
		this.dxfBytes = Arrays.copyOf(dxfBytes, dxfBytes.length);
		this.dxfSha256 = DxfHashing.sha256(this.dxfBytes);
		if (manifest != null
				&& !dxfSha256.equals(manifest.getDxfSha256())) {
			throw new IllegalArgumentException(
					"Manifest does not identify the supplied DXF bytes");
		}
		this.manifest = manifest;
		this.sourcePreflight = sourcePreflight;
	}

	/** @return prepared exact-only output without a sidecar */
	static DxfPreparedOutput exact(byte[] dxfBytes) {
		return new DxfPreparedOutput(dxfBytes, null, null);
	}

	/** @return exact-only output guarded by its source preflight */
	static DxfPreparedOutput exact(byte[] dxfBytes,
			GeometryExportPreflight sourcePreflight) {
		return new DxfPreparedOutput(dxfBytes, null, sourcePreflight);
	}

	/** @return prepared paired output whose manifest hashes the supplied DXF */
	static DxfPreparedOutput paired(byte[] dxfBytes,
			DxfManifestEncoding manifest) {
		return paired(dxfBytes, manifest, null);
	}

	/** @return paired output guarded by its source preflight */
	static DxfPreparedOutput paired(byte[] dxfBytes,
			DxfManifestEncoding manifest,
			GeometryExportPreflight sourcePreflight) {
		if (manifest == null) {
			throw new IllegalArgumentException("Paired output requires a manifest");
		}
		return new DxfPreparedOutput(dxfBytes, manifest, sourcePreflight);
	}

	/** @return defensive copy of deterministic ASCII DXF bytes */
	public byte[] getDxfBytes() {
		return Arrays.copyOf(dxfBytes, dxfBytes.length);
	}

	public String getDxfSha256() {
		return dxfSha256;
	}

	/** @return whether a fidelity manifest accompanies the DXF */
	public boolean hasManifest() {
		return manifest != null;
	}

	public DxfManifestEncoding getManifest() {
		return manifest;
	}

	void requireSourceRevisionCurrent() {
		if (sourcePreflight != null
				&& !sourcePreflight.isSourceRevisionCurrent()) {
			throw new IllegalStateException(
					"Source revision changed before DXF publication");
		}
	}

	private static void validateDxf(byte[] bytes) {
		if (bytes == null || bytes.length < 2
				|| bytes[bytes.length - 2] != '\r'
				|| bytes[bytes.length - 1] != '\n') {
			throw new IllegalArgumentException(
					"DXF must be nonempty ASCII text ending in CRLF");
		}
		for (byte value : bytes) {
			if ((value & 0x80) != 0) {
				throw new IllegalArgumentException("DXF bytes must be ASCII");
			}
		}
	}
}
