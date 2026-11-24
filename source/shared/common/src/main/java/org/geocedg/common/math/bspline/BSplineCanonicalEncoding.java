/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Canonical encoding and SHA-256 definition digest ({@code bspline-2d} section 12.3;
 * {@code dxf-spline-reference/v1} section 9). The digest is derived data: never serialized as
 * authority, never an identity.
 *
 * <h2>Byte layout</h2>
 * <ol>
 * <li>The contract identifier supplied by the caller (for example {@code bspline-2d/v1} or
 * {@code dxf-spline-reference/v1}), as ASCII bytes (printable characters {@code 0x20..0x7E}
 * only), followed by one terminator byte {@code 0x00}.</li>
 * <li>{@code p}, then {@code n + 1}, each as the big-endian bit pattern of the binary64 value
 * of that integer (8 bytes each).</li>
 * <li>The {@code n + p + 2} knots {@code u_0..u_m} in index order, each as the big-endian
 * bit pattern of {@link Double#doubleToLongBits(double)} (8 bytes each).</li>
 * <li>The control points in index order, {@code x_0, y_0, x_1, y_1, ..., x_n, y_n}, same
 * encoding.</li>
 * <li>One weight marker byte: {@code 'I'} ({@code 0x49}) for implicit unit weights, an
 * explicit absence after which nothing follows; or {@code 'E'} ({@code 0x45}) followed by
 * the {@code n + 1} weights {@code w_0..w_n} in index order, same encoding.</li>
 * </ol>
 * <p>Bit patterns keep {@code -0.0} distinct from {@code 0.0}: two data that differ only in
 * the sign of a zero have different digests, although every structural fact and every
 * evaluation treats them alike. Explicit weights that are all 1 and implicit unit weights
 * have different encodings.</p>
 */
public final class BSplineCanonicalEncoding {

	/** Weight marker of implicit unit weights. */
	public static final byte IMPLICIT_WEIGHTS_MARKER = 'I';
	/** Weight marker of explicit weights. */
	public static final byte EXPLICIT_WEIGHTS_MARKER = 'E';

	private static final char[] HEX = "0123456789abcdef".toCharArray();

	private BSplineCanonicalEncoding() {
	}

	/**
	 * Builds the canonical encoding.
	 *
	 * @param contractIdentifier ASCII contract identifier, printable characters only, not
	 *            empty
	 * @param data validated data
	 * @return a fresh byte array
	 * @throws IllegalArgumentException when the identifier is empty or not printable ASCII (a
	 *             programming error)
	 */
	public static byte[] encode(String contractIdentifier, BSplineData data) {
		Objects.requireNonNull(data, "data");
		byte[] id = identifierBytes(contractIdentifier);
		int n1 = data.xs.length;
		int valueCount = 2 + data.knots.length + 2 * n1 + (data.ws == null ? 0 : n1);
		byte[] out = new byte[id.length + 1 + 8 * valueCount + 1];
		System.arraycopy(id, 0, out, 0, id.length);
		int position = id.length;
		out[position++] = 0;
		position = putDouble(out, position, data.degree);
		position = putDouble(out, position, n1);
		for (double knot : data.knots) {
			position = putDouble(out, position, knot);
		}
		for (int i = 0; i < n1; i++) {
			position = putDouble(out, position, data.xs[i]);
			position = putDouble(out, position, data.ys[i]);
		}
		if (data.ws == null) {
			out[position] = IMPLICIT_WEIGHTS_MARKER;
		} else {
			out[position++] = EXPLICIT_WEIGHTS_MARKER;
			for (double weight : data.ws) {
				position = putDouble(out, position, weight);
			}
		}
		return out;
	}

	/**
	 * @param contractIdentifier ASCII contract identifier
	 * @param data validated data
	 * @return the 32-byte SHA-256 digest of the canonical encoding
	 */
	public static byte[] digest(String contractIdentifier, BSplineData data) {
		return sha256(encode(contractIdentifier, data));
	}

	/**
	 * @param contractIdentifier ASCII contract identifier
	 * @param data validated data
	 * @return the SHA-256 digest of the canonical encoding as 64 lowercase hexadecimal digits
	 */
	public static String digestHex(String contractIdentifier, BSplineData data) {
		byte[] digest = digest(contractIdentifier, data);
		char[] text = new char[2 * digest.length];
		for (int i = 0; i < digest.length; i++) {
			text[2 * i] = HEX[(digest[i] >> 4) & 0xF];
			text[2 * i + 1] = HEX[digest[i] & 0xF];
		}
		return new String(text);
	}

	private static byte[] sha256(byte[] bytes) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(bytes);
		} catch (NoSuchAlgorithmException exception) {
			// Every Java platform is required to provide SHA-256.
			throw new IllegalStateException("SHA-256 is unavailable", exception);
		}
	}

	private static byte[] identifierBytes(String contractIdentifier) {
		Objects.requireNonNull(contractIdentifier, "contractIdentifier");
		if (contractIdentifier.isEmpty()) {
			throw new IllegalArgumentException("Empty contract identifier");
		}
		for (int i = 0; i < contractIdentifier.length(); i++) {
			char c = contractIdentifier.charAt(i);
			if (c < 0x20 || c > 0x7E) {
				throw new IllegalArgumentException("Contract identifier is not printable ASCII");
			}
		}
		return contractIdentifier.getBytes(StandardCharsets.US_ASCII);
	}

	private static int putDouble(byte[] out, int position, double value) {
		long bits = Double.doubleToLongBits(value);
		for (int shift = 56; shift >= 0; shift -= 8) {
			out[position + (56 - shift) / 8] = (byte) (bits >>> shift);
		}
		return position + 8;
	}
}
