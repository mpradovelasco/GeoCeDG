/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;

/**
 * Structural D2 pair slot. Eligibility requires a separate current certificate
 * proving exactly one regular root of this germ on the whole component product.
 * No parameter, numerical box, span, chart, count or discovery order is identity.
 */
public final class PairSemanticSlotSelector2D {

	private static final String SCHEME = "pair-singleton-transverse-germ/v1/";

	/** Semantic domain class, independent of its current numerical endpoints. */
	public enum DomainKind {
		NON_PERIODIC, PERIODIC
	}

	/** Structural metadata kept associated with its durable source identity. */
	public static final class SourceDescriptor {
		private final String sourceId;
		private final String branchLineage;
		private final String componentLineage;
		private final Orientation orientation;
		private final DomainKind domainKind;
		private final String parameterizationContract;

		/** Creates a descriptor; the contract is structural, not a revision signature. */
		public SourceDescriptor(String sourceId, String branchLineage,
				String componentLineage, Orientation orientation,
				DomainKind domainKind, String parameterizationContract) {
			this.sourceId = text(sourceId);
			this.branchLineage = text(branchLineage);
			this.componentLineage = text(componentLineage);
			this.orientation = Objects.requireNonNull(orientation);
			this.domainKind = Objects.requireNonNull(domainKind);
			this.parameterizationContract = text(parameterizationContract);
		}

		public String getSourceId() {
			return sourceId;
		}

		public String getBranchLineage() {
			return branchLineage;
		}

		public String getComponentLineage() {
			return componentLineage;
		}

		public Orientation getOrientation() {
			return orientation;
		}

		public DomainKind getDomainKind() {
			return domainKind;
		}

		public String getParameterizationContract() {
			return parameterizationContract;
		}

		private SourceDescriptor remap(Map<String, String> mapping) {
			return new SourceDescriptor(mapped(mapping, sourceId), branchLineage,
					componentLineage, orientation, domainKind,
					parameterizationContract);
		}

		private String external() {
			return frame(sourceId) + frame(branchLineage) + frame(componentLineage)
					+ frame(orientation.name()) + frame(domainKind.name())
					+ frame(parameterizationContract);
		}

		private static SourceDescriptor parse(String value) {
			List<String> fields = fields(value, 6);
			return new SourceDescriptor(fields.get(0), fields.get(1), fields.get(2),
					Orientation.valueOf(fields.get(3)),
					DomainKind.valueOf(fields.get(4)), fields.get(5));
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof SourceDescriptor
					&& external().equals(((SourceDescriptor) other).external());
		}

		@Override
		public int hashCode() {
			return Objects.hash(sourceId, branchLineage, componentLineage,
					orientation, domainKind, parameterizationContract);
		}
	}

	private final SourceDescriptor first;
	private final SourceDescriptor second;
	private final int germ;

	private PairSemanticSlotSelector2D(SourceDescriptor first,
			SourceDescriptor second, int germ) {
		this.first = first;
		this.second = second;
		this.germ = germ;
	}

	/**
	 * Canonicalizes source axes and the orientation-normalized transverse sign.
	 * The supplied sign is det[epsilon1 C1', -epsilon2 C2'] in caller axes.
	 *
	 * @return source-order-independent structural selector
	 */
	public static PairSemanticSlotSelector2D of(SourceDescriptor first,
			SourceDescriptor second, int callerGerm) {
		Objects.requireNonNull(first);
		Objects.requireNonNull(second);
		if (Math.abs(callerGerm) != 1
				|| first.sourceId.equals(second.sourceId)) {
			throw new IllegalArgumentException(
					"Pair selector requires distinct sources and a transverse sign");
		}
		return first.sourceId.compareTo(second.sourceId) < 0
				? new PairSemanticSlotSelector2D(first, second, callerGerm)
				: new PairSemanticSlotSelector2D(second, first, -callerGerm);
	}

	public SourceDescriptor getFirst() {
		return first;
	}

	public SourceDescriptor getSecond() {
		return second;
	}

	public int getGerm() {
		return germ;
	}

	public String getSourcePairIdentity() {
		return LocusPairIdentity2D.sourcePair(first.sourceId, second.sourceId);
	}

	/** @return selector remapped only through an externally validated closure map */
	public PairSemanticSlotSelector2D remapSources(Map<String, String> mapping) {
		return of(first.remap(mapping), second.remap(mapping), germ);
	}

	/** @return exact canonical structural tuple, never a presentation label */
	public String toExternalForm() {
		return SCHEME + frame(first.external()) + frame(second.external())
				+ frame(germ > 0 ? "POSITIVE" : "NEGATIVE");
	}

	/** @return strictly parsed canonical structural tuple */
	public static PairSemanticSlotSelector2D parse(String value) {
		if (value == null || !value.startsWith(SCHEME)) {
			throw new IllegalArgumentException("Unknown pair selector scheme");
		}
		List<String> parsed = fields(value.substring(SCHEME.length()), 3);
		int sign;
		if ("POSITIVE".equals(parsed.get(2))) {
			sign = 1;
		} else if ("NEGATIVE".equals(parsed.get(2))) {
			sign = -1;
		} else {
			throw new IllegalArgumentException("Unknown pair germ");
		}
		PairSemanticSlotSelector2D selector = of(
				SourceDescriptor.parse(parsed.get(0)),
				SourceDescriptor.parse(parsed.get(1)), sign);
		if (!selector.toExternalForm().equals(value)) {
			throw new IllegalArgumentException("Noncanonical pair selector");
		}
		return selector;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof PairSemanticSlotSelector2D)) {
			return false;
		}
		PairSemanticSlotSelector2D selector = (PairSemanticSlotSelector2D) other;
		return first.equals(selector.first) && second.equals(selector.second)
				&& germ == selector.germ;
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second, germ);
	}

	static String text(String value) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException("Structural identity is required");
		}
		return value;
	}

	static String mapped(Map<String, String> mapping, String identity) {
		return text(Objects.requireNonNull(mapping).get(identity));
	}

	static String frame(String value) {
		return value.length() + ":" + value;
	}

	static List<String> fields(String value, int count) {
		ArrayList<String> result = new ArrayList<>();
		int offset = 0;
		for (int index = 0; index < count; index++) {
			int colon = value.indexOf(':', offset);
			if (colon < offset) {
				throw new IllegalArgumentException("Malformed framed pair field");
			}
			String lengthText = value.substring(offset, colon);
			int length = Integer.parseInt(lengthText);
			if (length < 0 || !Integer.toString(length).equals(lengthText)
					|| length > value.length() - colon - 1) {
				throw new IllegalArgumentException("Noncanonical pair field length");
			}
			offset = colon + 1 + length;
			result.add(value.substring(colon + 1, offset));
		}
		if (offset != value.length()) {
			throw new IllegalArgumentException("Unexpected trailing pair fields");
		}
		return result;
	}
}
