/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Last exact two-source address evidence, not a durable selector or a numerical
 * certificate. Current admissibility must be recomputed from both sources.
 */
public final class PairRootAddressProof2D {
	private final String firstSource;
	private final String firstProvider;
	private final long firstParameterBits;
	private final String secondSource;
	private final String secondProvider;
	private final long secondParameterBits;

	private PairRootAddressProof2D(String firstSource, String firstProvider,
			long firstParameterBits, String secondSource, String secondProvider,
			long secondParameterBits) {
		this.firstSource = PairSemanticSlotSelector2D.text(firstSource);
		this.firstProvider = PairSemanticSlotSelector2D.text(firstProvider);
		this.secondSource = PairSemanticSlotSelector2D.text(secondSource);
		this.secondProvider = PairSemanticSlotSelector2D.text(secondProvider);
		this.firstParameterBits = finiteBits(firstParameterBits);
		this.secondParameterBits = finiteBits(secondParameterBits);
		if (firstSource.compareTo(secondSource) >= 0) {
			throw new IllegalArgumentException("Pair address axes are not canonical");
		}
	}

	/** @return exact address evidence with associated sources canonically ordered */
	public static PairRootAddressProof2D of(String firstSource,
			String firstProvider, double firstParameter, String secondSource,
			String secondProvider, double secondParameter) {
		return fromBits(firstSource, firstProvider,
				Double.doubleToLongBits(firstParameter), secondSource,
				secondProvider, Double.doubleToLongBits(secondParameter));
	}

	private static PairRootAddressProof2D fromBits(String firstSource,
			String firstProvider, long firstBits, String secondSource,
			String secondProvider, long secondBits) {
		PairSemanticSlotSelector2D.text(firstSource);
		PairSemanticSlotSelector2D.text(secondSource);
		return firstSource.compareTo(secondSource) < 0
				? new PairRootAddressProof2D(firstSource, firstProvider, firstBits,
						secondSource, secondProvider, secondBits)
				: new PairRootAddressProof2D(secondSource, secondProvider, secondBits,
						firstSource, firstProvider, firstBits);
	}

	public String getFirstSource() {
		return firstSource;
	}

	public String getSecondSource() {
		return secondSource;
	}

	public String getFirstProvider() {
		return firstProvider;
	}

	public String getSecondProvider() {
		return secondProvider;
	}

	public long getFirstParameterBits() {
		return firstParameterBits;
	}

	public long getSecondParameterBits() {
		return secondParameterBits;
	}

	/** @return whether these current address axes match the structural selector */
	public boolean matchesSources(PairSemanticSlotSelector2D selector) {
		return firstSource.equals(selector.getFirst().getSourceId())
				&& secondSource.equals(selector.getSecond().getSourceId());
	}

	/** @return exact address evidence after an externally authorized source remap */
	public PairRootAddressProof2D remapSources(Map<String, String> mapping) {
		return fromBits(PairSemanticSlotSelector2D.mapped(mapping, firstSource),
				firstProvider, firstParameterBits,
				PairSemanticSlotSelector2D.mapped(mapping, secondSource),
				secondProvider, secondParameterBits);
	}

	/**
	 * Exact semantic-address check after the caller proved closure provenance.
	 * Provider signatures may contain newly copied IDs; source association and
	 * structural contracts are checked separately by the remapped selector.
	 *
	 * @return whether both remapped address bit patterns agree
	 */
	public boolean sameAddressUnderAuthorizedCopy(PairRootAddressProof2D other) {
		return firstSource.equals(other.firstSource)
				&& secondSource.equals(other.secondSource)
				&& firstParameterBits == other.firstParameterBits
				&& secondParameterBits == other.secondParameterBits;
	}

	/** @return exact canonical last-address evidence, not token material */
	public String toExternalForm() {
		return PairSemanticSlotSelector2D.frame(firstSource)
				+ PairSemanticSlotSelector2D.frame(firstProvider)
				+ PairSemanticSlotSelector2D.frame(Long.toHexString(firstParameterBits))
				+ PairSemanticSlotSelector2D.frame(secondSource)
				+ PairSemanticSlotSelector2D.frame(secondProvider)
				+ PairSemanticSlotSelector2D.frame(Long.toHexString(secondParameterBits));
	}

	/** @return strict canonical two-address evidence */
	public static PairRootAddressProof2D parse(String encoded) {
		List<String> fields = PairSemanticSlotSelector2D.fields(encoded, 6);
		PairRootAddressProof2D result = new PairRootAddressProof2D(fields.get(0),
				fields.get(1), parseBits(fields.get(2)), fields.get(3), fields.get(4),
				parseBits(fields.get(5)));
		if (!result.toExternalForm().equals(encoded)) {
			throw new IllegalArgumentException("Noncanonical pair address proof");
		}
		return result;
	}

	private static long parseBits(String value) {
		long bits = Long.parseUnsignedLong(value, 16);
		if (!Long.toHexString(bits).equals(value)) {
			throw new IllegalArgumentException("Noncanonical pair address bits");
		}
		return finiteBits(bits);
	}

	private static long finiteBits(long value) {
		if (!Double.isFinite(Double.longBitsToDouble(value))) {
			throw new IllegalArgumentException("Pair address must be finite");
		}
		return value;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof PairRootAddressProof2D
				&& toExternalForm().equals(((PairRootAddressProof2D) other)
						.toExternalForm());
	}

	@Override
	public int hashCode() {
		return Objects.hash(firstSource, firstProvider, firstParameterBits,
				secondSource, secondProvider, secondParameterBits);
	}
}
