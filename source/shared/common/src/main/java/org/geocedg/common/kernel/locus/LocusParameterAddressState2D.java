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
 * Versioned hidden-input representation of a parameter address: the address of a point at a
 * parameter that no certified component contains (ADR 0042; contract {@code
 * post-r1-conditioned-use.md}, section 6.1). It carries the branch key, the provider
 * contract, the canonical parameter, the periodic lift and the seam side, and no component
 * selector. Its codec is {@value #KIND}, distinct from {@code geocedg-locus-address/v1} and
 * {@code geocedg-locus-principal-selection/v1}, whose texts and meanings do not change. A
 * text of one codec is never read as a text of another.
 *
 * <p>Two forms exist. The <em>explicit</em> form states the canonical parameter, the lift
 * and the seam side, and the numeric input of the point must reconstruct them exactly. The
 * <em>input</em> form states that the parameter, the lift and the seam side are those of
 * the numeric input of the command, for a point whose parameter is a dependent number; it
 * adds no selection of its own. Neither form contains a sample, a cell, a node, an index or
 * a coordinate.</p>
 */
public final class LocusParameterAddressState2D {
	/** Kind and version of the codec. */
	public static final String KIND = "geocedg-locus-parameter-address/v1";
	private static final String PREFIX = KIND + "|";
	private static final String INPUT = "input";

	private LocusParameterAddressState2D() {
		// Utility class.
	}

	/**
	 * @param address parameter address
	 * @return deterministic opaque text of the explicit form
	 */
	public static String encode(LocusSemanticAddress2D address) {
		if (!Objects.requireNonNull(address).isParameterAddress()) {
			throw new IllegalArgumentException(
					"An address with a component selector is not a parameter address");
		}
		return PREFIX + text(address.getBranchKey()) + "|"
				+ text(address.getProviderVersion()) + "|"
				+ Long.toUnsignedString(Double.doubleToLongBits(normalized(
						address.getCanonicalParameter())), 16) + "|"
				+ address.getPeriodicLift() + "|" + address.getSeamSide().name();
	}

	/**
	 * @param branchKey branch of the source
	 * @param providerVersion provider contract of the source
	 * @return deterministic opaque text of the input form
	 */
	public static String encodeInputSelection(String branchKey, String providerVersion) {
		return PREFIX + text(branchKey) + "|" + text(providerVersion) + "|" + INPUT;
	}

	/**
	 * Decodes only this codec.
	 *
	 * @return decoded state, or {@code null} for any text that is not of this codec
	 */
	public static Decoded decode(String value) {
		if (value == null || !value.startsWith(PREFIX)) {
			return null;
		}
		String[] fields = value.substring(PREFIX.length()).split("\\|", -1);
		try {
			if (fields.length == 3 && INPUT.equals(fields[2])) {
				return new Decoded(decodedText(fields[0]), decodedText(fields[1]), true,
						0, 0, SeamSide.UNSPECIFIED);
			}
			if (fields.length != 5) {
				throw new IllegalArgumentException(
						"Malformed parameter-address state field count");
			}
			long canonicalBits = Long.parseUnsignedLong(fields[2], 16);
			long periodicLift = Long.parseLong(fields[3]);
			if (!Long.toUnsignedString(canonicalBits, 16).equals(fields[2])
					|| !Long.toString(periodicLift).equals(fields[3])) {
				throw new IllegalArgumentException(
						"Noncanonical parameter-address numeric field");
			}
			return new Decoded(decodedText(fields[0]), decodedText(fields[1]), false,
					Double.longBitsToDouble(canonicalBits), periodicLift,
					SeamSide.valueOf(fields[4]));
		} catch (RuntimeException exception) {
			throw new IllegalArgumentException("Malformed parameter-address state",
					exception);
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
				throw new IllegalArgumentException("Non-hexadecimal address field");
			}
			bytes[index / 2] = (byte) ((high << 4) | low);
		}
		String decoded = new String(bytes, StandardCharsets.UTF_8);
		if (!text(decoded).equals(value)) {
			throw new IllegalArgumentException(
					"Noncanonical UTF-8 parameter-address field");
		}
		return decoded;
	}

	private static double normalized(double value) {
		return value == 0 ? 0 : value;
	}

	/** Immutable decoded fields. */
	public static final class Decoded {
		private final String branchKey;
		private final String providerVersion;
		private final boolean inputSelection;
		private final double canonicalParameter;
		private final long periodicLift;
		private final SeamSide seamSide;

		private Decoded(String branchKey, String providerVersion, boolean inputSelection,
				double canonicalParameter, long periodicLift, SeamSide seamSide) {
			this.branchKey = Objects.requireNonNull(branchKey);
			this.providerVersion = Objects.requireNonNull(providerVersion);
			this.inputSelection = inputSelection;
			this.canonicalParameter = normalized(canonicalParameter);
			this.periodicLift = periodicLift;
			this.seamSide = Objects.requireNonNull(seamSide);
			if (branchKey.isEmpty() || providerVersion.isEmpty()
					|| !branchKey.equals(branchKey.trim())
					|| !providerVersion.equals(providerVersion.trim())
					|| !Double.isFinite(canonicalParameter)
					|| !inputSelection && seamSide == SeamSide.UNSPECIFIED) {
				throw new IllegalArgumentException(
						"Parameter-address state fields must be finite and canonical");
			}
		}

		/** @return branch of the source */
		public String getBranchKey() {
			return branchKey;
		}

		/**
		 * @return whether the parameter, the lift and the seam side are those of the numeric
		 *         input of the command
		 */
		public boolean isInputSelection() {
			return inputSelection;
		}

		/**
		 * @param currentProviderVersion provider contract of the current revision
		 * @return whether this state belongs to that contract
		 */
		public boolean hasProviderVersion(String currentProviderVersion) {
			return providerVersion.equals(currentProviderVersion);
		}

		/**
		 * Reconstructs the address of the explicit form, independently of the evidence and
		 * of the admissibility of the current revision.
		 *
		 * @param sourceLocusId persistent source identity, owned outside this codec
		 * @return exact parameter address
		 */
		public LocusSemanticAddress2D toParameterAddress(PersistentGeoId sourceLocusId) {
			if (inputSelection) {
				throw new IllegalStateException(
						"The input form states no parameter of its own");
			}
			return LocusSemanticAddress2D.byParameter(sourceLocusId, providerVersion,
					branchKey, canonicalParameter, periodicLift, seamSide);
		}

		/**
		 * @param address address of the current revision
		 * @return whether this explicit state is exactly that parameter address
		 */
		public boolean matches(LocusSemanticAddress2D address) {
			return !inputSelection && address.isParameterAddress()
					&& branchKey.equals(address.getBranchKey())
					&& providerVersion.equals(address.getProviderVersion())
					&& Double.doubleToLongBits(canonicalParameter)
							== Double.doubleToLongBits(normalized(
									address.getCanonicalParameter()))
					&& periodicLift == address.getPeriodicLift()
					&& seamSide == address.getSeamSide();
		}
	}
}
