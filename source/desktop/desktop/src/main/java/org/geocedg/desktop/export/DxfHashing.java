/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Deterministic byte hashing used by the paired DXF artifact boundary. */
final class DxfHashing {
	private static final char[] HEXADECIMAL =
			"0123456789abcdef".toCharArray();

	private DxfHashing() {
	}

	static String sha256(byte[] bytes) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
			char[] hexadecimal = new char[digest.length * 2];
			for (int index = 0; index < digest.length; index++) {
				int value = digest[index] & 0xff;
				hexadecimal[index * 2] = HEXADECIMAL[value >>> 4];
				hexadecimal[index * 2 + 1] = HEXADECIMAL[value & 0xf];
			}
			return new String(hexadecimal);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is unavailable", exception);
		}
	}
}
