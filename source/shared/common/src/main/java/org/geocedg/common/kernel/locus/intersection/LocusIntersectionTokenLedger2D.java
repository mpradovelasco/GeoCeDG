/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ComputationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.IdentityStatus;

/**
 * Persistent semantic-incarnation ledger for one public rich intersection.
 *
 * <p>The opaque token never stores coordinates, parameters, candidate order or
 * sample indices. Separately, the ledger persists exact provider/target
 * contracts and canonical-parameter bits as conservative revision evidence.
 * That evidence may justify stable-preimage reuse, but is never encoded into
 * token material.</p>
 */
public final class LocusIntersectionTokenLedger2D {
	private static final String FORMAT_VERSION = "1";
	private long nextIncarnation = 1;
	private Snapshot current;
	private Snapshot copySource;
	private String authorizedCopySourceOwner;

	/**
	 * Begins one isolated token-minting attempt.
	 *
	 * @return isolated token-ledger evaluation
	 */
	public Evaluation begin(String ownerIdentity, String sourcePairIdentity,
			String constructiveLineage, String topologyContext) {
		Material material = new Material(ownerIdentity, sourcePairIdentity,
				constructiveLineage, topologyContext);
		boolean sameMaterial = current != null
				&& current.material.equals(material);
		boolean authorizedCopy = current != null && !sameMaterial
				&& authorizedCopySourceOwner != null
				&& current.material.owner.equals(authorizedCopySourceOwner);
		if (current != null && !sameMaterial && !authorizedCopy) {
			throw new IllegalStateException(
					"Token-ledger material changed without copy authorization");
		}
		return new Evaluation(material, nextIncarnation,
				sameMaterial || authorizedCopy ? current : null,
				authorizedCopy ? authorizedCopySourceOwner : null);
	}

	/** Commits only token evidence represented by the published current result. */
	public void commit(Evaluation evaluation,
			LocusIntersectionResult2D published) {
		java.util.Objects.requireNonNull(evaluation);
		java.util.Objects.requireNonNull(published);
		if (evaluation.finished) {
			throw new IllegalStateException("Token evaluation is already finished");
		}
		long committedNextIncarnation = evaluation.usedFreshEpoch
				? Math.addExact(nextIncarnation, 1) : nextIncarnation;
		evaluation.finished = true;
		boolean nonFiniteAuthority = published.getGeometryKind() == GeometryKind.OVERLAP
				|| published.getGeometryKind() == GeometryKind.INFINITELY_MANY
				|| published.getGeometryKind() == GeometryKind.UNSUPPORTED_OVERLAP;
		ArrayList<Entry> retained = new ArrayList<>();
		LinkedHashSet<String> burned = burnedSemanticKeys(evaluation, published);
		if (!nonFiniteAuthority && isFinitePublication(published)) {
			for (LocusIntersectionSolution2D solution
					: published.getFiniteSolutions()) {
				IdentityStatus status = solution.getIdentity().getIdentityStatus();
				if (status != IdentityStatus.CONTINUATION_ESTABLISHED
						&& status != IdentityStatus.NEW_TOPOLOGICAL_SOLUTION) {
					continue;
				}
				String token = solution.getIdentity().getRootToken();
				Entry entry = evaluation.entryForPublishedToken(token,
						evaluation.startingSnapshot);
				if (entry != null && !evaluation.duplicateTokens.contains(token)
						&& evaluation.hasCompatibleStagedCandidate(entry)
						&& !burned.contains(entry.semanticKey())
						&& !containsSemanticEntry(retained, entry)) {
					retained.add(entry.withStatus(Status.ACTIVE));
				}
			}
		}
		Snapshot next = new Snapshot(evaluation.material, retained);
		if (current != null && !current.material.equals(next.material)) {
			copySource = evaluation.authorizedCopySourceOwner != null
					&& current.material.owner.equals(
							evaluation.authorizedCopySourceOwner)
					? current : null;
		}
		authorizedCopySourceOwner = null;
		current = next;
		nextIncarnation = committedNextIncarnation;
	}

	/**
	 * Discards an unpublishable token-minting attempt without consuming IDs.
	 * Immediate-copy authority remains armed until a publication actually moves
	 * the ledger to the attached owner; this lets the enclosing algorithm publish
	 * its explicit failure snapshot atomically after a provisional solver or
	 * token-commit failure.
	 */
	public void abort(Evaluation evaluation) {
		if (evaluation != null) {
			evaluation.finished = true;
		}
	}

	/** Burns identities after an unavailable revision without a spanning proof. */
	public void observeUnavailable() {
		authorizedCopySourceOwner = null;
		if (current == null) {
			return;
		}
		current = new Snapshot(current.material, List.of());
	}

	/** Authorizes one immediate G9A closure-copy owner transition. */
	public void authorizeImmediateCopy(String copySourceOwner) {
		if (current == null || !current.material.owner.equals(
				requireText(copySourceOwner, "Copy-source owner"))) {
			throw new IllegalArgumentException(
					"Copy-source owner does not match the token ledger");
		}
		authorizedCopySourceOwner = copySourceOwner;
	}

	/**
	 * Validates the owner found after an identity-section commit and arms the
	 * only permitted owner transition: an exact lifecycle-declared copy.
	 *
	 * @param attachedOwner committed owner identity
	 * @param declaredCopySource exact lifecycle copy source, or {@code null}
	 */
	public void prepareAttachedOwner(String attachedOwner,
			String declaredCopySource) {
		String attached = requireText(attachedOwner, "Attached owner");
		if (current == null || current.material.owner.equals(attached)) {
			authorizedCopySourceOwner = null;
			return;
		}
		if (declaredCopySource == null
				|| !current.material.owner.equals(declaredCopySource)) {
			throw new IllegalArgumentException(
					"Token-ledger owner does not match the attached identity");
		}
		authorizeImmediateCopy(declaredCopySource);
	}

	/** @return whether the restored/current ledger already owns this identity */
	public boolean hasCurrentOwner(String ownerIdentity) {
		return current != null && ownerIdentity != null
				&& current.material.owner.equals(ownerIdentity);
	}

	/** @return whether persisted/current material exists to validate */
	public boolean hasCurrentSnapshot() {
		return current != null;
	}

	/**
	 * Validates the complete reconstructed material before an identity batch may
	 * install its graph. This method is read-only. An owner transition is legal
	 * only in the exact immediate copy batch whose record names the current owner
	 * as its direct copy source.
	 */
	public void validatePreattachmentContext(String attachedOwner,
			String expectedSourcePair, String expectedConstructiveLineage,
			String expectedTopologyContext, String declaredCopySource,
			String expectedCopySourcePair, boolean immediateCopy) {
		if (current == null) {
			return;
		}
		String attached = requireText(attachedOwner, "Attached owner");
		String sourcePair = requireText(expectedSourcePair,
				"Expected source-pair identity");
		String constructive = requireText(expectedConstructiveLineage,
				"Expected constructive lineage");
		String topology = requireText(expectedTopologyContext,
				"Expected topology context");
		boolean sameOwner = current.material.owner.equals(attached);
		if (sameOwner && immediateCopy) {
			throw new IllegalArgumentException(
					"Immediate copy must retain its source ledger owner until commit");
		}
		if (!sameOwner && (!immediateCopy || declaredCopySource == null
				|| !current.material.owner.equals(declaredCopySource))) {
			throw new IllegalArgumentException(
					"Token-ledger owner transition lacks exact direct-copy authority");
		}
		if (!current.material.sourcePair.equals(sourcePair)
				|| !current.material.constructive.equals(constructive)
				|| !current.material.topology.equals(topology)) {
			throw new IllegalArgumentException(
					"Token-ledger material disagrees with reconstructed inputs");
		}
		if (sameOwner) {
			validatePersistedCopySource(declaredCopySource,
					expectedCopySourcePair, constructive, topology);
		} else {
			validateNestedCopySource();
		}
	}

	private void validatePersistedCopySource(String declaredCopySource,
			String expectedCopySourcePair, String constructive,
			String topology) {
		if (declaredCopySource == null) {
			if (copySource != null) {
				throw new IllegalArgumentException(
						"Token ledger has undeclared copy-source provenance");
			}
			return;
		}
		String expectedPair = requireText(expectedCopySourcePair,
				"Expected copy-source pair identity");
		if (copySource == null
				|| !copySource.material.owner.equals(declaredCopySource)
				|| !copySource.material.sourcePair.equals(expectedPair)
				|| !copySource.material.constructive.equals(constructive)
				|| !copySource.material.topology.equals(topology)) {
			throw new IllegalArgumentException(
					"Token-ledger copy-source material disagrees with provenance");
		}
		validateCopyContinuity(copySource, current);
	}

	private void validateNestedCopySource() {
		if (copySource == null) {
			return;
		}
		if (copySource.material.owner.equals(current.material.owner)
				|| !copySource.material.constructive.equals(
						current.material.constructive)
				|| !copySource.material.topology.equals(current.material.topology)) {
			throw new IllegalArgumentException(
					"Nested token-ledger copy provenance is inconsistent");
		}
		validateCopyContinuity(copySource, current);
	}

	private static void validateCopyContinuity(Snapshot source,
			Snapshot destination) {
		for (Entry sourceEntry : source.entries) {
			for (Entry destinationEntry : destination.entries) {
				if (sourceEntry.incarnation == destinationEntry.incarnation
						&& !destinationEntry.sameAuthorizedCopyContinuity(
								sourceEntry)) {
					throw new IllegalArgumentException(
							"Token-ledger copy entries have incompatible continuity");
				}
			}
		}
	}

	/**
	 * Validates and remaps one exact token from the immediate copied result.
	 *
	 * @return the corresponding current token, or empty for tampering/ambiguity
	 */
	public Optional<String> rebaseCopiedToken(String token,
			String currentOwnerIdentity, String immediateCopySourceOwner,
			LocusIntersectionResult2D published) {
		if (copySource == null || current == null || token == null
				|| currentOwnerIdentity == null
				|| immediateCopySourceOwner == null
				|| !current.material.owner.equals(currentOwnerIdentity)
				|| !copySource.material.owner.equals(immediateCopySourceOwner)) {
			return Optional.empty();
		}
		Optional<Entry> sourceEntry = copySource.validatedEntry(token);
		if (!sourceEntry.isPresent()) {
			return Optional.empty();
		}
		Entry old = sourceEntry.get();
		for (Entry candidate : current.entries) {
			if (candidate.status == Status.ACTIVE
					&& candidate.incarnation == old.incarnation
					&& candidate.sameAuthorizedCopyContinuity(old)) {
				String remapped = current.token(candidate);
				if (published.findPointAdmissibleSolution(remapped).isPresent()) {
					return Optional.of(remapped);
				}
			}
		}
		return Optional.empty();
	}

	/** @return whether the exact token is an active entry of this ledger */
	public boolean validatesCurrentToken(String token) {
		return current != null && current.validatedEntry(token)
				.filter(entry -> entry.status == Status.ACTIVE).isPresent();
	}

	/** @return strict compact XML attribute value for this ledger */
	public String exportState() {
		return FORMAT_VERSION + "|" + nextIncarnation + "|"
				+ encodeSnapshot(current) + "|" + encodeSnapshot(copySource);
	}

	/** Restores only durable lineage/high-water evidence, never numeric results. */
	public void importState(String state) {
		String[] fields = requireText(state, "Token-ledger state")
				.split("\\|", -1);
		if (fields.length != 4 || !FORMAT_VERSION.equals(fields[0])) {
			throw new IllegalArgumentException("Unsupported token-ledger state");
		}
		long parsedNext = parsePositiveLong(fields[1], "next incarnation");
		Snapshot parsedCurrent = decodeSnapshot(fields[2]);
		Snapshot parsedCopySource = decodeSnapshot(fields[3]);
		if (parsedCurrent == null && parsedCopySource != null) {
			throw new IllegalArgumentException(
					"Token-ledger copy source requires a current snapshot");
		}
		long maximum = maximumIncarnation(parsedCurrent, parsedCopySource);
		if (parsedNext <= maximum) {
			throw new IllegalArgumentException(
					"Token-ledger high-water mark is not monotone");
		}
		String canonical = FORMAT_VERSION + "|" + parsedNext + "|"
				+ encodeSnapshot(parsedCurrent) + "|"
				+ encodeSnapshot(parsedCopySource);
		if (!canonical.equals(state)) {
			throw new IllegalArgumentException(
					"Token-ledger state is not canonically encoded");
		}
		nextIncarnation = parsedNext;
		current = parsedCurrent;
		copySource = parsedCopySource;
		authorizedCopySourceOwner = null;
	}

	/** Copies durable lineage only; revision-bound result evidence is excluded. */
	public void set(LocusIntersectionTokenLedger2D other) {
		importState(java.util.Objects.requireNonNull(other).exportState());
	}

	/** One isolated token attempt bound to an exact owner/query context. */
	public final class Evaluation {
		private final Material material;
		private final List<Entry> startingEntries;
		private final Snapshot startingSnapshot;
		private final Map<String, Entry> byToken = new LinkedHashMap<>();
		private final Map<String, Integer> tokenUses = new LinkedHashMap<>();
		private final Map<String, Integer> semanticUses = new LinkedHashMap<>();
		private final java.util.Set<String> duplicateTokens = new HashSet<>();
		private final java.util.Set<String> duplicateSemanticKeys = new HashSet<>();
		private final long freshEpoch;
		private boolean usedFreshEpoch;
		private long revisionLocalHandleOrdinal;
		private final String authorizedCopySourceOwner;
		private boolean finished;

		private Evaluation(Material material, long nextIncarnation,
				Snapshot startingSnapshot, String authorizedCopySourceOwner) {
			this.material = material;
			this.freshEpoch = nextIncarnation;
			this.startingSnapshot = startingSnapshot;
			this.startingEntries = startingSnapshot == null ? new ArrayList<>()
					: new ArrayList<>(startingSnapshot.entries);
			this.authorizedCopySourceOwner = authorizedCopySourceOwner;
		}

		/**
		 * Returns a stable token only when lineage and exact address proof agree.
		 *
		 * @return current opaque semantic token
		 */
		public String mint(IntersectionTokenLineage2D lineage,
				IntersectionRootAddressProof2D addressProof) {
			if (finished) {
				throw new IllegalStateException("Token evaluation is already finished");
			}
			java.util.Objects.requireNonNull(addressProof);
			String semantic = semanticKey(lineage.getSolutionLineageKey(),
					lineage.getEstablishedBranchLineage(),
					lineage.getContinuationKey());
			Entry selected = uniqueStarting(semantic, addressProof);
			if (selected == null && authorizedCopySourceOwner != null) {
				selected = uniqueAuthorizedCopyStarting(semantic, addressProof);
			}
			if (selected == null) {
				selected = new Entry(lineage.getSolutionLineageKey(),
						lineage.getEstablishedBranchLineage(),
						lineage.getContinuationKey(), addressProof, freshEpoch,
						Status.ACTIVE);
				usedFreshEpoch = true;
			} else {
				selected = selected.withAddressProof(addressProof)
						.withStatus(Status.ACTIVE);
			}
			String token = material.token(selected);
			byToken.put(token, selected);
			int semanticCount = semanticUses.getOrDefault(semantic, 0) + 1;
			semanticUses.put(semantic, semanticCount);
			if (semanticCount > 1) {
				duplicateSemanticKeys.add(semantic);
			}
			int uses = tokenUses.getOrDefault(token, 0) + 1;
			tokenUses.put(token, uses);
			if (uses > 1) {
				duplicateTokens.add(token);
			}
			return token;
		}

		/**
		 * Issues a deterministic handle for a verified but non-identifiable root.
		 * The handle never enters staged entries or persisted ledger state.
		 *
		 * @return revision-local non-durable handle
		 */
		public String revisionLocalHandle(IntersectionTokenLineage2D lineage,
				IntersectionRootRevisionEvidence2D revisionEvidence) {
			if (finished) {
				throw new IllegalStateException("Token evaluation is already finished");
			}
			java.util.Objects.requireNonNull(lineage);
			revisionLocalHandleOrdinal = Math.addExact(
					revisionLocalHandleOrdinal, 1);
			return LocusSemanticIntersectionToken2D.createRevisionLocalHandle(
					material.owner, material.sourcePair, material.constructive,
					material.topology, revisionEvidence,
					revisionLocalHandleOrdinal);
		}

		private Entry uniqueStarting(String semantic,
				IntersectionRootAddressProof2D addressProof) {
			Entry found = null;
			for (Entry entry : startingEntries) {
				if (entry.semanticKey().equals(semantic)
						&& entry.addressProof.equals(addressProof)) {
					if (found != null) {
						return null;
					}
					found = entry;
				}
			}
			return found;
		}

		private Entry uniqueAuthorizedCopyStarting(String semantic,
				IntersectionRootAddressProof2D addressProof) {
			Entry found = null;
			for (Entry entry : startingEntries) {
				if (entry.semanticKey().equals(semantic)
						&& entry.addressProof
								.sameAddressUnderAuthorizedCopy(addressProof)) {
					if (found != null) {
						return null;
					}
					found = entry;
				}
			}
			return found;
		}

		private Entry entryForPublishedToken(String token, Snapshot prior) {
			Entry staged = byToken.get(token);
			if (staged != null) {
				return staged;
			}
			return prior == null ? null : prior.validatedEntry(token).orElse(null);
		}

		private boolean hasCompatibleStagedCandidate(Entry published) {
			for (Entry staged : byToken.values()) {
				if (staged.sameContinuity(published)) {
					return true;
				}
			}
			return false;
		}
	}

	private enum Status {
		ACTIVE("a");

		private final String code;

		Status(String code) {
			this.code = code;
		}

		private static Status parse(String value) {
			for (Status status : values()) {
				if (status.code.equals(value)) {
					return status;
				}
			}
			throw new IllegalArgumentException("Unknown token-ledger status");
		}
	}

	private static final class Entry {
		private final String solutionLineage;
		private final String branchLineage;
		private final Optional<String> continuationKey;
		private final IntersectionRootAddressProof2D addressProof;
		private final long incarnation;
		private final Status status;

		private Entry(String solutionLineage, String branchLineage,
				Optional<String> continuationKey,
				IntersectionRootAddressProof2D addressProof, long incarnation,
				Status status) {
			this.solutionLineage = requireText(solutionLineage,
					"Solution lineage");
			this.branchLineage = requireText(branchLineage, "Branch lineage");
			this.continuationKey = java.util.Objects.requireNonNull(continuationKey);
			if (continuationKey.isPresent()
					&& continuationKey.get().trim().isEmpty()) {
				throw new IllegalArgumentException("Continuation key cannot be blank");
			}
			this.addressProof = java.util.Objects.requireNonNull(addressProof);
			if (incarnation <= 0) {
				throw new IllegalArgumentException("Incarnation must be positive");
			}
			this.incarnation = incarnation;
			this.status = java.util.Objects.requireNonNull(status);
		}

		private Entry withStatus(Status newStatus) {
			return new Entry(solutionLineage, branchLineage, continuationKey,
					addressProof, incarnation, newStatus);
		}

		private Entry withAddressProof(
				IntersectionRootAddressProof2D newAddressProof) {
			return new Entry(solutionLineage, branchLineage, continuationKey,
					newAddressProof, incarnation, status);
		}

		private String semanticKey() {
			return LocusIntersectionTokenLedger2D.semanticKey(solutionLineage,
					branchLineage, continuationKey);
		}

		private boolean sameContinuity(Entry other) {
			return semanticKey().equals(other.semanticKey())
					&& addressProof.equals(other.addressProof);
		}

		private boolean sameAuthorizedCopyContinuity(Entry other) {
			return semanticKey().equals(other.semanticKey())
					&& addressProof.sameAddressUnderAuthorizedCopy(
							other.addressProof);
		}

		private IntersectionTokenLineage2D lineage() {
			return new IntersectionTokenLineage2D(solutionLineage, branchLineage,
					continuationKey);
		}
	}

	private static final class Material {
		private final String owner;
		private final String sourcePair;
		private final String constructive;
		private final String topology;

		private Material(String owner, String sourcePair, String constructive,
				String topology) {
			this.owner = requireText(owner, "Result owner identity");
			this.sourcePair = requireText(sourcePair, "Source-pair identity");
			this.constructive = requireText(constructive,
					"Constructive lineage");
			this.topology = requireText(topology, "Topology context");
		}

		private String token(Entry entry) {
			return LocusSemanticIntersectionToken2D.create(owner, sourcePair,
					constructive, topology, entry.lineage(), entry.incarnation);
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Material)) {
				return false;
			}
			Material material = (Material) other;
			return owner.equals(material.owner)
					&& sourcePair.equals(material.sourcePair)
					&& constructive.equals(material.constructive)
					&& topology.equals(material.topology);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(owner, sourcePair, constructive, topology);
		}
	}

	private static final class Snapshot {
		private final Material material;
		private final List<Entry> entries;

		private Snapshot(Material material, List<Entry> entries) {
			this.material = java.util.Objects.requireNonNull(material);
			ArrayList<Entry> sorted = new ArrayList<>(entries);
			sorted.sort(Comparator.comparing(Entry::semanticKey));
			this.entries = List.copyOf(sorted);
			LinkedHashMap<String, Entry> bySemantic = new LinkedHashMap<>();
			for (Entry entry : this.entries) {
				Entry duplicate = bySemantic.put(entry.semanticKey(), entry);
				if (duplicate != null) {
					throw new IllegalArgumentException(
							"Token ledger has duplicate semantic continuity");
				}
			}
		}

		private String token(Entry entry) {
			return material.token(entry);
		}

		private Optional<Entry> validatedEntry(String token) {
			Optional<LocusSemanticIntersectionToken2D.DecodedToken> decoded =
					LocusSemanticIntersectionToken2D.decode(token);
			if (!decoded.isPresent()
					|| !decoded.get().getResultOwnerIdentity().equals(material.owner)) {
				return Optional.empty();
			}
			for (Entry entry : entries) {
				if (entry.incarnation == decoded.get().getIncarnation()
						&& entry.branchLineage.equals(
								decoded.get().getEstablishedBranchLineage())
						&& entry.continuationKey.equals(
								decoded.get().getContinuationKey())
						&& token(entry).equals(token)) {
					return Optional.of(entry);
				}
			}
			return Optional.empty();
		}
	}

	private static boolean isFinitePublication(LocusIntersectionResult2D result) {
		return result.getComputationStatus() == ComputationStatus.SUCCESS
				&& (result.getGeometryKind() == GeometryKind.FINITE
						|| result.getGeometryKind()
								== GeometryKind.MIXED_FINITE_OVERLAP);
	}

	private static LinkedHashSet<String> burnedSemanticKeys(Evaluation evaluation,
			LocusIntersectionResult2D result) {
		LinkedHashSet<String> burned =
				new LinkedHashSet<>(evaluation.duplicateSemanticKeys);
		for (String duplicate : evaluation.duplicateTokens) {
			Entry entry = evaluation.byToken.get(duplicate);
			if (entry != null) {
				burned.add(entry.semanticKey());
			}
		}
		for (LocusIntersectionSolution2D solution : result.getFiniteSolutions()) {
			IdentityStatus status = solution.getIdentity().getIdentityStatus();
			if (status == IdentityStatus.AMBIGUOUS_CONTINUATION
					|| status == IdentityStatus.IDENTITY_DISCONTINUITY) {
				Entry own = evaluation.entryForPublishedToken(
						solution.getIdentity().getRootToken(),
						evaluation.startingSnapshot);
				if (own != null) {
					burned.add(own.semanticKey());
				}
				for (String parent : solution.getLineage()
						.getCandidateParentTokens()) {
					Entry old = evaluation.entryForPublishedToken(parent,
							evaluation.startingSnapshot);
					if (old != null) {
						burned.add(old.semanticKey());
					}
				}
			}
		}
		return burned;
	}

	private static boolean containsSemanticEntry(List<Entry> entries,
			Entry candidate) {
		for (Entry entry : entries) {
			if (entry.semanticKey().equals(candidate.semanticKey())) {
				return true;
			}
		}
		return false;
	}

	private static String semanticKey(String solution, String branch,
			Optional<String> continuation) {
		return solution.length() + ":" + solution + ":" + branch.length()
				+ ":" + branch + ":" + continuation.orElse("");
	}

	private static String encodeSnapshot(Snapshot snapshot) {
		if (snapshot == null) {
			return "-";
		}
		StringBuilder encoded = new StringBuilder();
		encoded.append(hex(snapshot.material.owner)).append('~')
				.append(hex(snapshot.material.sourcePair)).append('~')
				.append(hex(snapshot.material.constructive)).append('~')
				.append(hex(snapshot.material.topology)).append('~')
				.append(snapshot.entries.size());
		for (Entry entry : snapshot.entries) {
			encoded.append('~').append(entry.status.code).append(',')
					.append(entry.incarnation).append(',')
					.append(hex(entry.solutionLineage)).append(',')
					.append(hex(entry.branchLineage)).append(',')
					.append(hex(entry.continuationKey.orElse(""))).append(',')
					.append(hex(entry.addressProof.getSourceProviderSignature()))
					.append(',')
					.append(hex(entry.addressProof.getTargetContractSignature()))
					.append(',').append(Long.toHexString(
							entry.addressProof.getCanonicalParameterBits()));
		}
		return encoded.toString();
	}

	private static Snapshot decodeSnapshot(String encoded) {
		if ("-".equals(encoded)) {
			return null;
		}
		String[] parts = encoded.split("~", -1);
		if (parts.length < 5) {
			throw new IllegalArgumentException("Malformed token-ledger snapshot");
		}
		int count = parseNonNegativeInt(parts[4], "entry count");
		if (parts.length != count + 5) {
			throw new IllegalArgumentException("Token-ledger entry count mismatch");
		}
		Material material = new Material(unhex(parts[0]), unhex(parts[1]),
				unhex(parts[2]), unhex(parts[3]));
		ArrayList<Entry> entries = new ArrayList<>();
		for (int index = 0; index < count; index++) {
			String[] entry = parts[index + 5].split(",", -1);
			if (entry.length != 8) {
				throw new IllegalArgumentException("Malformed token-ledger entry");
			}
			String continuation = unhex(entry[4]);
			IntersectionRootAddressProof2D addressProof =
					IntersectionRootAddressProof2D.fromBits(unhex(entry[5]),
							unhex(entry[6]), parseParameterBits(entry[7]));
			entries.add(new Entry(unhex(entry[2]), unhex(entry[3]),
					continuation.isEmpty() ? Optional.empty()
							: Optional.of(continuation),
					addressProof,
					parsePositiveLong(entry[1], "entry incarnation"),
					Status.parse(entry[0])));
		}
		return new Snapshot(material, entries);
	}

	private static long maximumIncarnation(Snapshot... snapshots) {
		long maximum = 0;
		for (Snapshot snapshot : snapshots) {
			if (snapshot != null) {
				for (Entry entry : snapshot.entries) {
					maximum = Math.max(maximum, entry.incarnation);
				}
			}
		}
		return maximum;
	}

	private static String hex(String value) {
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		StringBuilder result = new StringBuilder(bytes.length * 2);
		for (byte currentByte : bytes) {
			int current = currentByte & 0xff;
			result.append(Character.forDigit(current >>> 4, 16));
			result.append(Character.forDigit(current & 0x0f, 16));
		}
		return result.toString();
	}

	private static String unhex(String value) {
		if ((value.length() & 1) != 0) {
			throw new IllegalArgumentException("Odd hexadecimal ledger field");
		}
		byte[] bytes = new byte[value.length() / 2];
		for (int index = 0; index < value.length(); index += 2) {
			int high = Character.digit(value.charAt(index), 16);
			int low = Character.digit(value.charAt(index + 1), 16);
			if (high < 0 || low < 0) {
				throw new IllegalArgumentException(
						"Non-hexadecimal token-ledger field");
			}
			bytes[index / 2] = (byte) (high << 4 | low);
		}
		return new String(bytes, StandardCharsets.UTF_8);
	}

	private static long parsePositiveLong(String value, String name) {
		try {
			long parsed = Long.parseLong(value);
			if (parsed <= 0) {
				throw new IllegalArgumentException(name + " must be positive");
			}
			return parsed;
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException("Malformed " + name, exception);
		}
	}

	private static long parseParameterBits(String value) {
		try {
			long parsed = Long.parseUnsignedLong(value, 16);
			if (!Long.toHexString(parsed).equals(value)) {
				throw new IllegalArgumentException(
						"Canonical parameter bits are not canonical");
			}
			return parsed;
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException(
					"Malformed canonical parameter bits", exception);
		}
	}

	private static int parseNonNegativeInt(String value, String name) {
		try {
			int parsed = Integer.parseInt(value);
			if (parsed < 0) {
				throw new IllegalArgumentException(name + " must be nonnegative");
			}
			return parsed;
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException("Malformed " + name, exception);
		}
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}
}
