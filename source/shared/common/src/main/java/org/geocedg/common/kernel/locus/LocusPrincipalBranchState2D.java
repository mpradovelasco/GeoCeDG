/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/** Versioned hidden state for one retained principal-branch selection. */
public final class LocusPrincipalBranchState2D {
	private static final String PREFIX = "geocedg-locus-principal-selection/v1|";

	private LocusPrincipalBranchState2D() {
		// Utility class.
	}

	/** @return deterministic opaque text retaining branch/component/provider */
	public static String encode(LocusSemanticAddress2D address) {
		Objects.requireNonNull(address);
		return PREFIX + text(address.getBranchKey()) + "|"
				+ text(address.getComponentLineageKey()) + "|"
				+ text(address.getProviderVersion());
	}

	/** @return decoded selector, or {@code null} for another input form */
	public static Decoded decode(String value) {
		if (value == null || !value.startsWith(PREFIX)) {
			return null;
		}
		String[] fields = value.substring(PREFIX.length()).split("\\|", -1);
		if (fields.length != 3) {
			throw new IllegalArgumentException(
					"Malformed principal-branch selection state");
		}
		return new Decoded(decodedText(fields[0]), decodedText(fields[1]),
				decodedText(fields[2]));
	}

	private static String text(String value) {
		byte[] bytes = Objects.requireNonNull(value).getBytes(StandardCharsets.UTF_8);
		StringBuilder encoded = new StringBuilder(bytes.length * 2);
		for (byte currentByte : bytes) {
			int current = currentByte & 0xff;
			encoded.append(Character.forDigit(current >>> 4, 16));
			encoded.append(Character.forDigit(current & 0x0f, 16));
		}
		return encoded.toString();
	}

	private static String decodedText(String value) {
		if ((value.length() & 1) != 0) {
			throw new IllegalArgumentException("Odd hexadecimal selector field");
		}
		byte[] bytes = new byte[value.length() / 2];
		for (int index = 0; index < value.length(); index += 2) {
			int high = Character.digit(value.charAt(index), 16);
			int low = Character.digit(value.charAt(index + 1), 16);
			if (high < 0 || low < 0) {
				throw new IllegalArgumentException(
						"Non-hexadecimal selector field");
			}
			bytes[index / 2] = (byte) ((high << 4) | low);
		}
		String decoded = new String(bytes, StandardCharsets.UTF_8);
		if (!text(decoded).equals(value)) {
			throw new IllegalArgumentException(
					"Noncanonical UTF-8 selector field");
		}
		return decoded;
	}

	/** Immutable retained selector fields; the parameter remains a normal input. */
	public static final class Decoded {
		private final String branchKey;
		private final String componentLineageKey;
		private final String providerVersion;

		private Decoded(String branchKey, String componentLineageKey,
				String providerVersion) {
			this.branchKey = requireNonempty(branchKey);
			this.componentLineageKey = requireNonempty(componentLineageKey);
			this.providerVersion = requireNonempty(providerVersion);
		}

		public String getBranchKey() {
			return branchKey;
		}

		public String getComponentLineageKey() {
			return componentLineageKey;
		}

		/** @return whether the current address is exactly this retained selector */
		public boolean matches(LocusSemanticAddress2D address) {
			return branchKey.equals(address.getBranchKey())
					&& componentLineageKey.equals(address.getComponentLineageKey())
					&& providerVersion.equals(address.getProviderVersion());
		}

		private static String requireNonempty(String value) {
			if (value == null || value.isEmpty()) {
				throw new IllegalArgumentException(
						"Principal-branch selector fields must be nonempty");
			}
			return value;
		}
	}
}
