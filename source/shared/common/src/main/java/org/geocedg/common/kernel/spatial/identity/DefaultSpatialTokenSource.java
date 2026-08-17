/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

/**
 * Shared-Java opaque token source. It deliberately uses neither desktop UUIDs
 * nor GeoGebra's construction random state; registry allocation still checks
 * every candidate for construction-wide collision.
 */
public final class DefaultSpatialTokenSource implements SpatialTokenSource {
	private static long sourceSequence;

	private long state;
	private long sequence;

	/** Creates a source seeded independently of construction random state. */
	public DefaultSpatialTokenSource() {
		long source;
		synchronized (DefaultSpatialTokenSource.class) {
			source = ++sourceSequence;
		}
		state = mix64(System.currentTimeMillis())
				^ mix64(System.nanoTime()) ^ mix64(source);
		if (state == 0) {
			state = 0x6a09e667f3bcc909L;
		}
	}

	@Override
	public String nextToken() {
		long first = nextLong();
		long second = nextLong() ^ mix64(++sequence);
		return fixedHex(first) + fixedHex(second);
	}

	private long nextLong() {
		state += 0x9e3779b97f4a7c15L;
		return mix64(state);
	}

	private static long mix64(long value) {
		long mixed = value;
		mixed = (mixed ^ (mixed >>> 30)) * 0xbf58476d1ce4e5b9L;
		mixed = (mixed ^ (mixed >>> 27)) * 0x94d049bb133111ebL;
		return mixed ^ (mixed >>> 31);
	}

	private static String fixedHex(long value) {
		String hex = Long.toHexString(value);
		StringBuilder result = new StringBuilder(16);
		for (int index = hex.length(); index < 16; index++) {
			result.append('0');
		}
		return result.append(hex).toString();
	}
}
