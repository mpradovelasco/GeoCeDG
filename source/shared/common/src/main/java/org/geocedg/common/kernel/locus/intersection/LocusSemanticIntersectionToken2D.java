/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/** Versioned opaque token codec for public semantic intersection solutions. */
public final class LocusSemanticIntersectionToken2D {
	private static final String PREFIX = "locus-root/v3/";
	private static final String REVISION_LOCAL_PREFIX =
			"locus-root-local/v1/";
	private static final int[] SHA256_CONSTANTS = {
			0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5,
			0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
			0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3,
			0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
			0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc,
			0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
			0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7,
			0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
			0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13,
			0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
			0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3,
			0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
			0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5,
			0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
			0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208,
			0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
	};

	private LocusSemanticIntersectionToken2D() {
		// Utility class.
	}

	/**
	 * Mints a token solely from durable query/solution lineage and incarnation.
	 *
	 * @return opaque token carrying no coordinates, parameters or ordinals
	 */
	public static String create(String resultOwnerIdentity,
			String sourcePairIdentity, String constructiveLineage,
			String topologyContext, IntersectionTokenLineage2D lineage,
			long incarnation) {
		String owner = requireText(resultOwnerIdentity, "Result owner identity");
		String source = requireText(sourcePairIdentity, "Source-pair identity");
		String constructive = requireText(constructiveLineage,
				"Constructive lineage");
		String topology = requireText(topologyContext, "Topology context");
		if (incarnation <= 0) {
			throw new IllegalArgumentException("Token incarnation must be positive");
		}
		String continuation = lineage.getContinuationKey().orElse("");
		String material = framed(owner) + framed(source) + framed(constructive)
				+ framed(topology) + framed(lineage.getSolutionLineageKey())
				+ framed(lineage.getEstablishedBranchLineage())
				+ framed(continuation) + framed(Long.toString(incarnation));
		return PREFIX + hex(owner) + "/"
				+ hex(lineage.getEstablishedBranchLineage()) + "/"
				+ hex(continuation) + "/"
				+ Long.toUnsignedString(incarnation, 16) + "/"
				+ sha256Hex(material.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Decodes the explicit non-geometric routing fields of a v3 token.
	 *
	 * <p>Decoding is not validation. The owning token ledger must recompute the
	 * digest from its persisted semantic lineage before accepting the token.</p>
	 *
	 * @return decoded token, or empty for foreign/malformed token text
	 */
	public static Optional<DecodedToken> decode(String token) {
		if (token == null || !token.startsWith(PREFIX)) {
			return Optional.empty();
		}
		String[] fields = token.substring(PREFIX.length()).split("/", -1);
		if (fields.length != 5 || fields[3].isEmpty()
				|| fields[4].length() != 64 || !isUnsignedHex(fields[4])) {
			return Optional.empty();
		}
		try {
			String owner = unhex(fields[0]);
			String branch = unhex(fields[1]);
			String continuation = unhex(fields[2]);
			if (owner.isEmpty() || branch.isEmpty()
					|| !isUnsignedHex(fields[3])) {
				return Optional.empty();
			}
			long incarnation = Long.parseUnsignedLong(fields[3], 16);
			if (incarnation <= 0) {
				return Optional.empty();
			}
			return Optional.of(new DecodedToken(owner, branch,
					continuation.isEmpty() ? Optional.empty()
							: Optional.of(continuation), incarnation,
					fields[4]));
		} catch (IllegalArgumentException exception) {
			return Optional.empty();
		}
	}

	/**
	 * Creates a deterministic internal handle for one current-revision root whose
	 * durable semantic identity is not established. This is deliberately outside
	 * the semantic-token format, is never persisted by the token ledger and must
	 * never be accepted by the token-point overload. The ordinal only distinguishes
	 * verified roots inside this captured revision; it is not durable identity.
	 *
	 * @return deterministic revision-local non-durable handle
	 */
	public static String createRevisionLocalHandle(String resultOwnerIdentity,
			String sourcePairIdentity, String constructiveLineage,
			String topologyContext,
			IntersectionRootRevisionEvidence2D revisionEvidence, long ordinal) {
		String owner = requireText(resultOwnerIdentity, "Result owner identity");
		String source = requireText(sourcePairIdentity, "Source-pair identity");
		String constructive = requireText(constructiveLineage,
				"Constructive lineage");
		String topology = requireText(topologyContext, "Topology context");
		IntersectionRootRevisionEvidence2D evidence =
				java.util.Objects.requireNonNull(revisionEvidence);
		if (ordinal < 1) {
			throw new IllegalArgumentException(
					"Revision-local handle ordinal must be positive");
		}
		String revision = Long.toString(evidence.getLocusSemanticRevision());
		String targetStamp = Long.toString(evidence.getTargetUpdateStamp());
		String discriminator = Long.toString(ordinal);
		String material = framed(owner) + framed(source) + framed(constructive)
				+ framed(topology) + framed(revision) + framed(targetStamp)
				+ framed(evidence.getBranchSnapshotKey())
				+ framed(evidence.getResolvedValidComponentKey())
				+ framed(discriminator);
		return REVISION_LOCAL_PREFIX + hex(owner) + "/" + revision + "/"
				+ targetStamp + "/" + discriminator + "/"
				+ sha256Hex(material.getBytes(StandardCharsets.UTF_8));
	}

	/** @return whether text is explicitly a non-durable current-revision handle */
	public static boolean isRevisionLocalHandle(String value) {
		return value != null && value.startsWith(REVISION_LOCAL_PREFIX);
	}

	/** Explicit, non-geometric routing material recovered from a v3 token. */
	public static final class DecodedToken {
		private final String resultOwnerIdentity;
		private final String establishedBranchLineage;
		private final Optional<String> continuationKey;
		private final long incarnation;
		private final String digest;

		private DecodedToken(String resultOwnerIdentity,
				String establishedBranchLineage,
				Optional<String> continuationKey, long incarnation,
				String digest) {
			this.resultOwnerIdentity = resultOwnerIdentity;
			this.establishedBranchLineage = establishedBranchLineage;
			this.continuationKey = continuationKey;
			this.incarnation = incarnation;
			this.digest = digest;
		}

		public String getResultOwnerIdentity() {
			return resultOwnerIdentity;
		}

		public String getEstablishedBranchLineage() {
			return establishedBranchLineage;
		}

		public Optional<String> getContinuationKey() {
			return continuationKey;
		}

		public long getIncarnation() {
			return incarnation;
		}

		public String getDigest() {
			return digest;
		}
	}

	private static String framed(String value) {
		return value.length() + ":" + value;
	}

	private static String hex(String value) {
		return bytesToHex(value.getBytes(StandardCharsets.UTF_8));
	}

	private static String unhex(String value) {
		if ((value.length() & 1) != 0) {
			throw new IllegalArgumentException("Odd hexadecimal token field");
		}
		byte[] bytes = new byte[value.length() / 2];
		for (int index = 0; index < value.length(); index += 2) {
			int high = Character.digit(value.charAt(index), 16);
			int low = Character.digit(value.charAt(index + 1), 16);
			if (high < 0 || low < 0) {
				throw new IllegalArgumentException("Non-hexadecimal token field");
			}
			bytes[index / 2] = (byte) ((high << 4) | low);
		}
		return new String(bytes, StandardCharsets.UTF_8);
	}

	private static boolean isUnsignedHex(String value) {
		if (value.isEmpty()) {
			return false;
		}
		for (int index = 0; index < value.length(); index++) {
			if (Character.digit(value.charAt(index), 16) < 0) {
				return false;
			}
		}
		return true;
	}

	private static String sha256Hex(byte[] input) {
		int[] state = {0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a,
				0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19};
		byte[] padded = pad(input);
		int[] words = new int[64];
		for (int offset = 0; offset < padded.length; offset += 64) {
			for (int index = 0; index < 16; index++) {
				int at = offset + index * 4;
				words[index] = (padded[at] & 0xff) << 24
						| (padded[at + 1] & 0xff) << 16
						| (padded[at + 2] & 0xff) << 8
						| padded[at + 3] & 0xff;
			}
			for (int index = 16; index < 64; index++) {
				int s0 = Integer.rotateRight(words[index - 15], 7)
						^ Integer.rotateRight(words[index - 15], 18)
						^ words[index - 15] >>> 3;
				int s1 = Integer.rotateRight(words[index - 2], 17)
						^ Integer.rotateRight(words[index - 2], 19)
						^ words[index - 2] >>> 10;
				words[index] = words[index - 16] + s0 + words[index - 7] + s1;
			}
			compress(state, words);
		}
		byte[] digest = new byte[32];
		for (int index = 0; index < state.length; index++) {
			int value = state[index];
			digest[index * 4] = (byte) (value >>> 24);
			digest[index * 4 + 1] = (byte) (value >>> 16);
			digest[index * 4 + 2] = (byte) (value >>> 8);
			digest[index * 4 + 3] = (byte) value;
		}
		return bytesToHex(digest);
	}

	private static byte[] pad(byte[] input) {
		int paddedLength = ((input.length + 9 + 63) / 64) * 64;
		byte[] padded = new byte[paddedLength];
		System.arraycopy(input, 0, padded, 0, input.length);
		padded[input.length] = (byte) 0x80;
		long bitLength = (long) input.length * 8;
		for (int index = 0; index < 8; index++) {
			padded[padded.length - 1 - index] = (byte) (bitLength >>> index * 8);
		}
		return padded;
	}

	private static void compress(int[] state, int[] words) {
		int a = state[0];
		int b = state[1];
		int c = state[2];
		int d = state[3];
		int e = state[4];
		int f = state[5];
		int g = state[6];
		int h = state[7];
		for (int index = 0; index < 64; index++) {
			int sum1 = Integer.rotateRight(e, 6) ^ Integer.rotateRight(e, 11)
					^ Integer.rotateRight(e, 25);
			int choice = e & f ^ ~e & g;
			int first = h + sum1 + choice + SHA256_CONSTANTS[index]
					+ words[index];
			int sum0 = Integer.rotateRight(a, 2) ^ Integer.rotateRight(a, 13)
					^ Integer.rotateRight(a, 22);
			int majority = a & b ^ a & c ^ b & c;
			final int second = sum0 + majority;
			h = g;
			g = f;
			f = e;
			e = d + first;
			d = c;
			c = b;
			b = a;
			a = first + second;
		}
		state[0] += a;
		state[1] += b;
		state[2] += c;
		state[3] += d;
		state[4] += e;
		state[5] += f;
		state[6] += g;
		state[7] += h;
	}

	private static String bytesToHex(byte[] bytes) {
		StringBuilder encoded = new StringBuilder(bytes.length * 2);
		for (byte current : bytes) {
			int unsigned = current & 0xff;
			encoded.append(Character.forDigit(unsigned >>> 4, 16));
			encoded.append(Character.forDigit(unsigned & 0x0f, 16));
		}
		return encoded.toString();
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}
}
