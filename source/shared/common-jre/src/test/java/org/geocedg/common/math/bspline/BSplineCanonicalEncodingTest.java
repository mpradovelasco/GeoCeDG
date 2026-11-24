/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;

import org.junit.jupiter.api.Test;

/** Canonical byte layout and SHA-256 definition digest (spec section 12.3, import section 9). */
final class BSplineCanonicalEncodingTest {

	private static final double[] KNOTS = {0, 0, 0, 1, 2, 2, 2};
	private static final double[] XS = {1, 0.5, -1, 1};
	private static final double[] YS = {0, 2, 0, -0.25};

	/** Independent construction of the documented layout with a ByteBuffer. */
	private static byte[] expected(String id, int p, double[] knots, double[] xs, double[] ys,
			double[] ws) {
		byte[] idBytes = id.getBytes(StandardCharsets.US_ASCII);
		int doubles = 2 + knots.length + 2 * xs.length + (ws == null ? 0 : ws.length);
		ByteBuffer buffer = ByteBuffer.allocate(idBytes.length + 1 + 8 * doubles + 1)
				.order(ByteOrder.BIG_ENDIAN);
		buffer.put(idBytes).put((byte) 0).putDouble(p).putDouble(xs.length);
		for (double knot : knots) {
			buffer.putDouble(knot);
		}
		for (int i = 0; i < xs.length; i++) {
			buffer.putDouble(xs[i]).putDouble(ys[i]);
		}
		if (ws == null) {
			buffer.put((byte) 'I');
		} else {
			buffer.put((byte) 'E');
			for (double w : ws) {
				buffer.putDouble(w);
			}
		}
		return buffer.array();
	}

	@Test
	void layoutMatchesTheDocumentedBytes() throws Exception {
		BSplineData implicit = BSplineTestData.data(2, KNOTS, XS, YS, null);
		byte[] encoding = BSplineCanonicalEncoding.encode("bspline-2d/v1", implicit);
		assertArrayEquals(expected("bspline-2d/v1", 2, KNOTS, XS, YS, null), encoding);
		assertEquals('I', encoding[encoding.length - 1]);
		double[] ws = {1, 0.5, 2, 1};
		BSplineData explicit = BSplineTestData.data(2, KNOTS, XS, YS, ws);
		assertArrayEquals(expected("dxf-spline-reference/v1", 2, KNOTS, XS, YS, ws),
				BSplineCanonicalEncoding.encode("dxf-spline-reference/v1", explicit));
		byte[] digest = BSplineCanonicalEncoding.digest("bspline-2d/v1", implicit);
		assertArrayEquals(MessageDigest.getInstance("SHA-256").digest(encoding), digest);
		assertEquals(HexFormat.of().formatHex(digest),
				BSplineCanonicalEncoding.digestHex("bspline-2d/v1", implicit));
		assertEquals(64, BSplineCanonicalEncoding.digestHex("bspline-2d/v1", implicit).length());
	}

	@Test
	void digestIsDeterministicAndDistinguishesRepresentations() {
		BSplineData a = BSplineTestData.data(2, KNOTS, XS, YS, null);
		BSplineData b = BSplineTestData.data(2, KNOTS.clone(), XS.clone(), YS.clone(), null);
		assertEquals(BSplineCanonicalEncoding.digestHex("bspline-2d/v1", a),
				BSplineCanonicalEncoding.digestHex("bspline-2d/v1", b));
		BSplineData ones = BSplineTestData.data(2, KNOTS, XS, YS, new double[] {1, 1, 1, 1});
		assertFalse(Arrays.equals(BSplineCanonicalEncoding.digest("x", a),
				BSplineCanonicalEncoding.digest("x", ones)));
		assertFalse(Arrays.equals(BSplineCanonicalEncoding.digest("bspline-2d/v1", a),
				BSplineCanonicalEncoding.digest("dxf-spline-reference/v1", a)));
		double[] negativeZero = YS.clone();
		negativeZero[0] = -0.0;
		BSplineData signed = BSplineTestData.data(2, KNOTS, XS, negativeZero, null);
		assertTrue(signed.isExactlyClosed() == a.isExactlyClosed());
		assertFalse(Arrays.equals(BSplineCanonicalEncoding.digest("x", a),
				BSplineCanonicalEncoding.digest("x", signed)));
		// Prefix-free: identifier terminator separates "ab" + data from "a" + data.
		assertFalse(Arrays.equals(BSplineCanonicalEncoding.encode("ab", a),
				BSplineCanonicalEncoding.encode("a", a)));
	}

	@Test
	void identifierMustBePrintableAscii() {
		BSplineData data = BSplineTestData.data(2, KNOTS, XS, YS, null);
		assertThrows(IllegalArgumentException.class,
				() -> BSplineCanonicalEncoding.encode("", data));
		assertThrows(IllegalArgumentException.class,
				() -> BSplineCanonicalEncoding.encode("a\u0000b", data));
		assertThrows(IllegalArgumentException.class,
				() -> BSplineCanonicalEncoding.encode(String.valueOf((char) 0xE9), data));
	}
}
