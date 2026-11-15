/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusSemanticAddress2D.SeamSide;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;

/**
 * Versioned hidden-input representation of one exact semantic address.
 * It is construction state, not a public label or a replacement for the
 * source's durable identity.
 */
public final class LocusSemanticAddressState2D {
	private static final String PREFIX = "geocedg-locus-address/v1|";

	private LocusSemanticAddressState2D() {
		// Utility class.
	}

	/** @return deterministic opaque text for one reconstructible address */
	public static String encode(LocusSemanticAddress2D address) {
		Objects.requireNonNull(address);
		return PREFIX + text(address.getBranchKey()) + "|"
				+ text(address.getComponentLineageKey()) + "|"
				+ text(address.getProviderVersion()) + "|"
				+ Long.toUnsignedString(Double.doubleToLongBits(normalized(
						address.getCanonicalParameter())), 16) + "|"
				+ address.getPeriodicLift() + "|" + address.getSeamSide().name();
	}

	/**
	 * Decodes only the internal versioned form.
	 *
	 * @return decoded state, or {@code null} for an ordinary public branch key
	 */
	public static Decoded decode(String value) {
		if (value == null || !value.startsWith(PREFIX)) {
			return null;
		}
		String[] fields = value.substring(PREFIX.length()).split("\\|", -1);
		if (fields.length != 6) {
			throw new IllegalArgumentException(
					"Malformed semantic-address state field count");
		}
		try {
			long canonicalBits = Long.parseUnsignedLong(fields[3], 16);
			long periodicLift = Long.parseLong(fields[4]);
			if (!Long.toUnsignedString(canonicalBits, 16).equals(fields[3])
					|| !Long.toString(periodicLift).equals(fields[4])) {
				throw new IllegalArgumentException(
						"Noncanonical semantic-address numeric field");
			}
			return new Decoded(decodedText(fields[0]), decodedText(fields[1]),
					decodedText(fields[2]), Double.longBitsToDouble(canonicalBits),
					periodicLift, SeamSide.valueOf(fields[5]));
		} catch (RuntimeException exception) {
			throw new IllegalArgumentException(
					"Malformed semantic-address state", exception);
		}
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
			throw new IllegalArgumentException("Odd hexadecimal address field");
		}
		byte[] bytes = new byte[value.length() / 2];
		for (int index = 0; index < value.length(); index += 2) {
			int high = Character.digit(value.charAt(index), 16);
			int low = Character.digit(value.charAt(index + 1), 16);
			if (high < 0 || low < 0) {
				throw new IllegalArgumentException(
						"Non-hexadecimal address field");
			}
			bytes[index / 2] = (byte) ((high << 4) | low);
		}
		String decoded = new String(bytes, StandardCharsets.UTF_8);
		if (!text(decoded).equals(value)) {
			throw new IllegalArgumentException(
					"Noncanonical UTF-8 semantic-address field");
		}
		return decoded;
	}

	private static double normalized(double value) {
		return value == 0 ? 0 : value;
	}

	/** Immutable decoded address fields used to fail closed on recomputation. */
	public static final class Decoded {
		private final String branchKey;
		private final String componentLineageKey;
		private final String providerVersion;
		private final double canonicalParameter;
		private final long periodicLift;
		private final SeamSide seamSide;

		private Decoded(String branchKey, String componentLineageKey,
				String providerVersion, double canonicalParameter,
				long periodicLift, SeamSide seamSide) {
			this.branchKey = Objects.requireNonNull(branchKey);
			this.componentLineageKey = Objects.requireNonNull(componentLineageKey);
			this.providerVersion = Objects.requireNonNull(providerVersion);
			this.canonicalParameter = normalized(canonicalParameter);
			this.periodicLift = periodicLift;
			this.seamSide = Objects.requireNonNull(seamSide);
			if (branchKey.isEmpty() || componentLineageKey.isEmpty()
					|| providerVersion.isEmpty()
					|| !Double.isFinite(canonicalParameter)) {
				throw new IllegalArgumentException(
						"Semantic-address state fields must be finite and nonempty");
			}
		}

		public String getBranchKey() {
			return branchKey;
		}

		public String getComponentLineageKey() {
			return componentLineageKey;
		}

		/** @return whether this selector belongs to the current provider contract */
		public boolean hasProviderVersion(String currentProviderVersion) {
			return providerVersion.equals(currentProviderVersion);
		}

		/**
		 * Reconstructs the durable selector independently of current topology or
		 * evaluation admissibility.
		 *
		 * @param sourceLocusId persistent source identity owned outside this codec
		 * @return exact decoded semantic selector
		 */
		public LocusSemanticAddress2D toSemanticAddress(
				PersistentGeoId sourceLocusId) {
			return new LocusSemanticAddress2D(sourceLocusId, providerVersion,
					branchKey, componentLineageKey, canonicalParameter, periodicLift,
					seamSide);
		}

		/** @return whether this persisted state is exactly the current address */
		public boolean matches(LocusSemanticAddress2D address) {
			return branchKey.equals(address.getBranchKey())
					&& componentLineageKey.equals(
							address.getComponentLineageKey())
					&& providerVersion.equals(address.getProviderVersion())
					&& Double.doubleToLongBits(canonicalParameter)
							== Double.doubleToLongBits(normalized(
									address.getCanonicalParameter()))
					&& periodicLift == address.getPeriodicLift()
					&& seamSide == address.getSeamSide();
		}
	}
}
