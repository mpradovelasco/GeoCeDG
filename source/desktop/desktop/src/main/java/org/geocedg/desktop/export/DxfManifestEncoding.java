/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Pattern;

/** Validated deterministic UTF-8 encoding of one DXF fidelity manifest. */
public final class DxfManifestEncoding {

	private static final String HEADER =
			"{\"schema\":\"org.geocedg.dxf.fidelity-manifest\","
					+ "\"schema_version\":1,";
	private static final Pattern SHA_256 = Pattern.compile("[0-9a-f]{64}");
	private final byte[] bytes;
	private final String dxfSha256;

	/**
	 * @param bytes canonical UTF-8 manifest bytes
	 * @param dxfSha256 SHA-256 of the paired DXF bytes
	 */
	public DxfManifestEncoding(byte[] bytes, String dxfSha256) {
		if (bytes == null || bytes.length == 0) {
			throw new IllegalArgumentException("Manifest bytes are required");
		}
		if (dxfSha256 == null || !SHA_256.matcher(dxfSha256).matches()) {
			throw new IllegalArgumentException(
					"Manifest DXF SHA-256 must be lowercase hexadecimal");
		}
		String text;
		try {
			text = StandardCharsets.UTF_8.newDecoder()
					.onMalformedInput(CodingErrorAction.REPORT)
					.onUnmappableCharacter(CodingErrorAction.REPORT)
					.decode(ByteBuffer.wrap(bytes)).toString();
		} catch (CharacterCodingException exception) {
			throw new IllegalArgumentException(
					"Manifest must be valid UTF-8", exception);
		}
		if (!text.endsWith("\n") || text.indexOf('\r') >= 0) {
			throw new IllegalArgumentException(
					"Manifest must use canonical LF text with a final newline");
		}
		if (!text.startsWith(HEADER) || !text.endsWith("}\n")
				|| !text.contains("\"sha256\":\"" + dxfSha256 + "\"")) {
			throw new IllegalArgumentException(
					"Manifest schema and paired DXF hash are required");
		}
		this.bytes = Arrays.copyOf(bytes, bytes.length);
		this.dxfSha256 = dxfSha256;
	}

	/** @return defensive copy of canonical UTF-8 bytes */
	public byte[] getBytes() {
		return Arrays.copyOf(bytes, bytes.length);
	}

	public String getDxfSha256() {
		return dxfSha256;
	}
}
