/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geocedg.common.export.ApproximationEvidence.Guarantee;

/**
 * Immutable fidelity and deterministic-work policy for one export snapshot.
 * Semantic domains are model/parameter data supplied explicitly by the caller;
 * they are never inferred from a viewport or render tessellation.
 */
public final class GeometryExportRequest {

	/** Conservative deterministic default evaluation budget. */
	public static final long DEFAULT_MAXIMUM_EVALUATIONS = 65536;
	/** Conservative deterministic default dyadic depth. */
	public static final int DEFAULT_MAXIMUM_DEPTH = 20;
	/** Maximum meaningful dyadic depth for binary64 parameter intervals. */
	public static final int MAXIMUM_SUPPORTED_DEPTH = 52;
	/** Conservative deterministic default vertices per component. */
	public static final int DEFAULT_MAXIMUM_VERTICES_PER_COMPONENT = 16384;
	/** Conservative deterministic default vertices across the request. */
	public static final int DEFAULT_MAXIMUM_TOTAL_VERTICES = 131072;

	/** Explicit, oriented, finite semantic parameter subdomain. */
	public static final class SemanticDomain {
		private final String branchKey;
		private final String key;
		private final double startParameter;
		private final double endParameter;
		private final boolean startClosed;
		private final boolean endClosed;

		/**
		 * @param key revision-local domain/component key
		 * @param startParameter oriented start parameter
		 * @param endParameter oriented end parameter
		 * @param startClosed whether the oriented start is included
		 * @param endClosed whether the oriented end is included
		 */
		public SemanticDomain(String key, double startParameter,
				double endParameter, boolean startClosed, boolean endClosed) {
			this(null, key, startParameter, endParameter, startClosed, endClosed);
		}

		/**
		 * @param branchKey authoritative branch key, or null for a non-branch source
		 * @param key revision-local domain/component key
		 * @param startParameter oriented start parameter
		 * @param endParameter oriented end parameter
		 * @param startClosed whether the oriented start is included
		 * @param endClosed whether the oriented end is included
		 */
		public SemanticDomain(String branchKey, String key, double startParameter,
				double endParameter, boolean startClosed, boolean endClosed) {
			if (key == null || key.trim().isEmpty()) {
				throw new IllegalArgumentException("Semantic domain key is required");
			}
			if (branchKey != null && branchKey.trim().isEmpty()) {
				throw new IllegalArgumentException(
						"Semantic branch key must not be blank");
			}
			if (!Double.isFinite(startParameter)
					|| !Double.isFinite(endParameter)
					|| startParameter == endParameter) {
				throw new IllegalArgumentException(
						"Semantic domain requires distinct finite endpoints");
			}
			this.branchKey = branchKey;
			this.key = key;
			this.startParameter = normalizeZero(startParameter);
			this.endParameter = normalizeZero(endParameter);
			this.startClosed = startClosed;
			this.endClosed = endClosed;
		}

		/** @return authoritative branch key, or null for a non-branch source */
		public String getBranchKey() {
			return branchKey;
		}

		public String getKey() {
			return key;
		}

		public double getStartParameter() {
			return startParameter;
		}

		public double getEndParameter() {
			return endParameter;
		}

		public boolean isStartClosed() {
			return startClosed;
		}

		public boolean isEndClosed() {
			return endClosed;
		}

		/** @return whether the oriented parameter increases */
		public boolean isIncreasing() {
			return endParameter > startParameter;
		}

		private static double normalizeZero(double value) {
			return value == 0 ? 0 : value;
		}
	}

	/** Fluent constructor retaining strict partial-output defaults. */
	public static final class Builder {
		private final double requestedTolerance;
		private Set<Guarantee> allowedGuarantees =
				EnumSet.of(Guarantee.ESTIMATED_ERROR);
		private final List<SemanticDomain> defaultSemanticDomains =
				new ArrayList<>();
		private final Map<String, List<SemanticDomain>> sourceSemanticDomains =
				new LinkedHashMap<>();
		private long maximumEvaluations = DEFAULT_MAXIMUM_EVALUATIONS;
		private int maximumDepth = DEFAULT_MAXIMUM_DEPTH;
		private int maximumVerticesPerComponent =
				DEFAULT_MAXIMUM_VERTICES_PER_COMPONENT;
		private int maximumTotalVertices = DEFAULT_MAXIMUM_TOTAL_VERTICES;
		private boolean approximationAllowed = true;
		private boolean partialOutputAllowed;
		private boolean sidecarRequested;

		private Builder(double requestedTolerance) {
			assertPositiveFinite(requestedTolerance, "Requested tolerance");
			this.requestedTolerance = requestedTolerance;
		}

		/**
		 * Replace the admissible approximation guarantees.
		 * @return this builder
		 */
		public Builder allowedGuarantees(Set<Guarantee> guarantees) {
			if (guarantees == null || guarantees.isEmpty()) {
				throw new IllegalArgumentException(
						"At least one approximation guarantee is required");
			}
			this.allowedGuarantees = EnumSet.copyOf(guarantees);
			return this;
		}

		/**
		 * Add one fallback domain used when a source has no override.
		 * @return this builder
		 */
		public Builder addDefaultSemanticDomain(SemanticDomain domain) {
			SemanticDomain checked = requireDomain(domain);
			assertUniqueDomainKey(defaultSemanticDomains, checked);
			defaultSemanticDomains.add(checked);
			return this;
		}

		/**
		 * Replace the fallback semantic-domain partition.
		 * @return this builder
		 */
		public Builder defaultSemanticDomains(List<SemanticDomain> domains) {
			defaultSemanticDomains.clear();
			defaultSemanticDomains.addAll(copyDomains(domains));
			return this;
		}

		/**
		 * Add one explicit domain override for a source identifier.
		 * @return this builder
		 */
		public Builder addSourceSemanticDomain(String sourceId,
				SemanticDomain domain) {
			String checkedId = requireText(sourceId, "Source identifier");
			List<SemanticDomain> domains = sourceSemanticDomains.computeIfAbsent(
					checkedId, ignored -> new ArrayList<>());
			SemanticDomain checked = requireDomain(domain);
			assertUniqueDomainKey(domains, checked);
			domains.add(checked);
			return this;
		}

		/**
		 * Replace the explicit semantic-domain partition for one source.
		 * @return this builder
		 */
		public Builder sourceSemanticDomains(String sourceId,
				List<SemanticDomain> domains) {
			sourceSemanticDomains.put(requireText(sourceId, "Source identifier"),
					copyDomains(domains));
			return this;
		}

		/** @return this builder */
		public Builder maximumEvaluations(long maximum) {
			if (maximum < 1) {
				throw new IllegalArgumentException(
						"Maximum evaluations must be positive");
			}
			maximumEvaluations = maximum;
			return this;
		}

		/** @return this builder */
		public Builder maximumDepth(int maximum) {
			if (maximum < 0 || maximum > MAXIMUM_SUPPORTED_DEPTH) {
				throw new IllegalArgumentException(
						"Maximum dyadic depth must be between 0 and "
								+ MAXIMUM_SUPPORTED_DEPTH);
			}
			maximumDepth = maximum;
			return this;
		}

		/** @return this builder */
		public Builder maximumVerticesPerComponent(int maximum) {
			if (maximum < 2) {
				throw new IllegalArgumentException(
						"A component budget must allow at least two vertices");
			}
			maximumVerticesPerComponent = maximum;
			return this;
		}

		/** @return this builder */
		public Builder maximumTotalVertices(int maximum) {
			if (maximum < 2) {
				throw new IllegalArgumentException(
						"The total budget must allow at least two vertices");
			}
			maximumTotalVertices = maximum;
			return this;
		}

		/** @return this builder */
		public Builder allowApproximation(boolean allowed) {
			approximationAllowed = allowed;
			return this;
		}

		/**
		 * Retains an explicit future policy bit. Product callers keep this false;
		 * this class does not itself authorize or perform partial output.
		 * @return this builder
		 */
		public Builder allowPartialOutput(boolean allowed) {
			partialOutputAllowed = allowed;
			return this;
		}

		/** @return this builder */
		public Builder requestSidecar(boolean requested) {
			sidecarRequested = requested;
			return this;
		}

		/** @return immutable export request */
		public GeometryExportRequest build() {
			return new GeometryExportRequest(this);
		}
	}

	private final double requestedTolerance;
	private final Set<Guarantee> allowedGuarantees;
	private final List<SemanticDomain> defaultSemanticDomains;
	private final Map<String, List<SemanticDomain>> sourceSemanticDomains;
	private final long maximumEvaluations;
	private final int maximumDepth;
	private final int maximumVerticesPerComponent;
	private final int maximumTotalVertices;
	private final boolean approximationAllowed;
	private final boolean partialOutputAllowed;
	private final boolean sidecarRequested;

	private GeometryExportRequest(Builder builder) {
		requestedTolerance = builder.requestedTolerance;
		allowedGuarantees = Collections.unmodifiableSet(
				EnumSet.copyOf(builder.allowedGuarantees));
		defaultSemanticDomains = immutableDomains(builder.defaultSemanticDomains);
		sourceSemanticDomains = immutableDomainMap(builder.sourceSemanticDomains);
		maximumEvaluations = builder.maximumEvaluations;
		maximumDepth = builder.maximumDepth;
		maximumVerticesPerComponent = builder.maximumVerticesPerComponent;
		maximumTotalVertices = builder.maximumTotalVertices;
		approximationAllowed = builder.approximationAllowed;
		partialOutputAllowed = builder.partialOutputAllowed;
		sidecarRequested = builder.sidecarRequested;
	}

	/**
	 * Start a request with explicit tolerance and strict partiality.
	 * @return new request builder
	 */
	public static Builder builder(double requestedTolerance) {
		return new Builder(requestedTolerance);
	}

	public double getRequestedTolerance() {
		return requestedTolerance;
	}

	public Set<Guarantee> getAllowedGuarantees() {
		return allowedGuarantees;
	}

	/** @return whether the guarantee is admissible */
	public boolean allowsGuarantee(Guarantee guarantee) {
		return allowedGuarantees.contains(guarantee);
	}

	public List<SemanticDomain> getDefaultSemanticDomains() {
		return defaultSemanticDomains;
	}

	public Map<String, List<SemanticDomain>> getSourceSemanticDomains() {
		return sourceSemanticDomains;
	}

	/**
	 * Resolves a source-specific partition, falling back to the explicit default.
	 * An empty result means that no export domain was supplied.
	 * @return resolved immutable semantic domains
	 */
	public List<SemanticDomain> resolveSemanticDomains(String sourceId) {
		List<SemanticDomain> domains = sourceSemanticDomains.get(sourceId);
		return domains == null ? defaultSemanticDomains : domains;
	}

	public long getMaximumEvaluations() {
		return maximumEvaluations;
	}

	public int getMaximumDepth() {
		return maximumDepth;
	}

	public int getMaximumVerticesPerComponent() {
		return maximumVerticesPerComponent;
	}

	public int getMaximumTotalVertices() {
		return maximumTotalVertices;
	}

	public boolean isApproximationAllowed() {
		return approximationAllowed;
	}

	public boolean isPartialOutputAllowed() {
		return partialOutputAllowed;
	}

	public boolean isSidecarRequested() {
		return sidecarRequested;
	}

	private static Map<String, List<SemanticDomain>> immutableDomainMap(
			Map<String, List<SemanticDomain>> source) {
		Map<String, List<SemanticDomain>> copy = new LinkedHashMap<>();
		for (Map.Entry<String, List<SemanticDomain>> entry : source.entrySet()) {
			copy.put(entry.getKey(), immutableDomains(entry.getValue()));
		}
		return Collections.unmodifiableMap(copy);
	}

	private static List<SemanticDomain> immutableDomains(
			List<SemanticDomain> domains) {
		return Collections.unmodifiableList(copyDomains(domains));
	}

	private static List<SemanticDomain> copyDomains(List<SemanticDomain> domains) {
		if (domains == null) {
			throw new IllegalArgumentException("Semantic-domain list is required");
		}
		List<SemanticDomain> copy = new ArrayList<>();
		for (SemanticDomain domain : domains) {
			SemanticDomain checked = requireDomain(domain);
			assertUniqueDomainKey(copy, checked);
			copy.add(checked);
		}
		return copy;
	}

	private static void assertUniqueDomainKey(List<SemanticDomain> domains,
			SemanticDomain candidate) {
		for (SemanticDomain previous : domains) {
			if (previous.getKey().equals(candidate.getKey())) {
				throw new IllegalArgumentException(
						"Duplicate semantic domain key: " + candidate.getKey());
			}
		}
	}

	private static SemanticDomain requireDomain(SemanticDomain domain) {
		if (domain == null) {
			throw new IllegalArgumentException("Semantic domain is required");
		}
		return domain;
	}

	private static String requireText(String value, String description) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(description + " is required");
		}
		return value;
	}

	private static void assertPositiveFinite(double value, String description) {
		if (!Double.isFinite(value) || value <= 0) {
			throw new IllegalArgumentException(description + " must be positive");
		}
	}
}
