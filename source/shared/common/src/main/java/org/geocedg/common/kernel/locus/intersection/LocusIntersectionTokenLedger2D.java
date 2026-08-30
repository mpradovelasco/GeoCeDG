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

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ComputationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.IdentityStatus;

/**
 * Persistent semantic-incarnation ledger for one public rich intersection.
 *
 * <p>The opaque token never stores coordinates, parameters, candidate order or
 * sample indices. Current public allocations bind an intrinsic deterministic
 * selector in a separate durable binding. New R4 tokens also frame that selector
 * in their opaque continuation key; an exact R3 token can therefore acquire the
 * new binding without changing any of its token material. Separately, the ledger
 * persists exact provider/target contracts and canonical-parameter bits as
 * revision evidence. Parameter evidence is never selector or token authority.</p>
 */
public final class LocusIntersectionTokenLedger2D {
	private static final String FORMAT_VERSION = "4";
	private static final String PHASE_FORMAT_VERSION = "3";
	private static final String PREVIOUS_FORMAT_VERSION = "2";
	private static final String LEGACY_FORMAT_VERSION = "1";
	private static final String CURRENT_ROOT_ALLOCATION_PREFIX =
			"g9u0-r4/ledger-current-root/v2/";
	private static final String LEGACY_PUBLIC_SINGLETON_PREFIX =
			"g9u0/g8c1-explicit-unique-local-root/v1/";
	private long nextIncarnation = 1;
	private Snapshot current;
	private Snapshot copySource;
	private String authorizedCopySourceOwner;
	private final Map<String, Integer> materializedClaimCounts =
			new LinkedHashMap<>();

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
		final long committedNextIncarnation = evaluation.usedFreshEpoch
				? Math.max(Math.addExact(nextIncarnation, 1),
						evaluation.nextAllocatedIncarnation)
				: nextIncarnation;
		evaluation.finished = true;
		boolean nonFiniteAuthority = published.getGeometryKind() == GeometryKind.OVERLAP
				|| published.getGeometryKind() == GeometryKind.INFINITELY_MANY
				|| published.getGeometryKind() == GeometryKind.UNSUPPORTED_OVERLAP;
		ArrayList<Entry> retained = new ArrayList<>();
		LinkedHashSet<String> retainedSemanticKeys = new LinkedHashSet<>();
		java.util.Set<CurrentRootAllocationKey> retainedBindings = new HashSet<>();
		java.util.Set<PeriodicAllocationGroupKey> claimedPeriodicGroups =
				claimedPeriodicGroups(evaluation);
		LinkedHashSet<String> burned = burnedSemanticKeys(evaluation, published);
		for (String retired : evaluation.permanentlyRetiredTokens) {
			Entry entry = evaluation.entryForPublishedToken(retired,
					evaluation.startingSnapshot);
			if (entry != null) {
				burned.add(entry.semanticKey());
			}
		}
		if (!nonFiniteAuthority && isFinitePublication(published)) {
			for (LocusIntersectionSolution2D solution
					: published.getFiniteSolutions()) {
				IdentityStatus status = solution.getIdentity().getIdentityStatus();
				if (status != IdentityStatus.CONTINUATION_ESTABLISHED
						&& status
								!= IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED
						&& status != IdentityStatus.NEW_TOPOLOGICAL_SOLUTION) {
					continue;
				}
				String token = solution.getIdentity().getRootToken();
				Entry entry = evaluation.entryForPublishedToken(token,
						evaluation.startingSnapshot);
				if (entry != null && !evaluation.duplicateTokens.contains(token)
						&& evaluation.hasCompatibleStagedCandidate(entry)
						&& !burned.contains(entry.semanticKey())
						&& retainedSemanticKeys.add(entry.semanticKey())) {
					Entry retainedEntry = entry.withStatus(isClaimed(evaluation, entry)
							? Status.CLAIMED_ACTIVE : Status.ACTIVE);
					retained.add(retainedEntry);
					retainedEntry.currentRootBinding.ifPresent(retainedBindings::add);
				}
			}
		}
		for (Entry prior : evaluation.startingEntries) {
			String priorToken = evaluation.startingSnapshot.token(prior);
			Entry retainedPrior = evaluation.authorizedCopyQuarantineEntries
					.getOrDefault(priorToken, prior);
			CurrentRootAllocationKey copiedBinding =
					evaluation.authorizedCopySuccessorBindings.get(priorToken);
			boolean copiedToCurrent = copiedBinding != null
					&& retainedBindings.contains(copiedBinding);
			boolean quarantined = !evaluation.releasedPeriodicQuarantineTokens
					.contains(priorToken)
					&& (prior.status.isPeriodicallyQuarantined()
							|| evaluation.periodicallyQuarantinedTokens
									.contains(priorToken)
							|| periodicAllocationGroup(prior)
									.filter(claimedPeriodicGroups::contains)
									.isPresent());
			if ((isClaimed(evaluation, prior) || quarantined)
					&& !evaluation.permanentlyRetiredTokens.contains(priorToken)
					&& !copiedToCurrent
					&& retainedSemanticKeys.add(retainedPrior.semanticKey())) {
				Status retainedStatus = Status.CLAIMED_DORMANT;
				if (quarantined) {
					retainedStatus = isClaimed(evaluation, prior)
							? Status.CLAIMED_PERIODIC_QUARANTINE
							: Status.PERIODIC_QUARANTINE;
				}
				retained.add(retainedPrior.withStatus(retainedStatus));
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

	private boolean isClaimed(Evaluation evaluation, Entry entry) {
		if (entry.status.isClaimed()) {
			return true;
		}
		String currentToken = evaluation.material.token(entry);
		if (materializedClaimCounts.getOrDefault(currentToken, 0) > 0) {
			return true;
		}
		String startingToken = evaluation.startingSnapshot == null ? null
				: evaluation.startingSnapshot.token(entry);
		return startingToken != null
				&& materializedClaimCounts.getOrDefault(startingToken, 0) > 0;
	}

	private java.util.Set<PeriodicAllocationGroupKey> claimedPeriodicGroups(
			Evaluation evaluation) {
		java.util.Set<PeriodicAllocationGroupKey> claimed = new HashSet<>();
		for (Entry entry : evaluation.startingEntries) {
			if (isClaimed(evaluation, entry)) {
				periodicAllocationGroup(entry).ifPresent(claimed::add);
			}
		}
		return claimed;
	}

	private static Optional<PeriodicAllocationGroupKey> periodicAllocationGroup(
			Entry entry) {
		return currentRootAllocation(entry)
				.flatMap(PeriodicAllocationGroupKey::from);
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

	/** Makes claimed identities dormant after an unavailable current revision. */
	public void observeUnavailable() {
		authorizedCopySourceOwner = null;
		if (current == null) {
			return;
		}
		java.util.Set<PeriodicAllocationGroupKey> claimedPeriodicGroups =
				new HashSet<>();
		for (Entry entry : current.entries) {
			String token = current.token(entry);
			if (entry.status.isClaimed()
					|| materializedClaimCounts.getOrDefault(token, 0) > 0) {
				periodicAllocationGroup(entry).ifPresent(claimedPeriodicGroups::add);
			}
		}
		ArrayList<Entry> retained = new ArrayList<>();
		for (Entry entry : current.entries) {
			String token = current.token(entry);
			boolean periodicGroupClaimed = periodicAllocationGroup(entry)
					.filter(claimedPeriodicGroups::contains).isPresent();
			if (entry.status.isPeriodicallyQuarantined()
					|| periodicGroupClaimed) {
				retained.add(entry.withStatus(entry.status.isClaimed()
						|| materializedClaimCounts.getOrDefault(token, 0) > 0
								? Status.CLAIMED_PERIODIC_QUARANTINE
								: Status.PERIODIC_QUARANTINE));
			} else if (entry.status.isClaimed()
					|| materializedClaimCounts.getOrDefault(token, 0) > 0) {
				retained.add(entry.withStatus(Status.CLAIMED_DORMANT));
			}
		}
		current = new Snapshot(current.material, retained);
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
			int sameIncarnation = 0;
			int compatible = 0;
			for (Entry destinationEntry : destination.entries) {
				if (sourceEntry.incarnation == destinationEntry.incarnation) {
					sameIncarnation++;
					if (destinationEntry.sameAuthorizedCopyContinuity(
							sourceEntry)) {
						compatible++;
					}
				}
			}
			if (sameIncarnation > 0 && compatible != 1) {
				throw new IllegalArgumentException(
						"Token-ledger copy entries lack one semantic mapping");
			}
		}
		for (Entry destinationEntry : destination.entries) {
			int sameIncarnation = 0;
			int compatible = 0;
			for (Entry sourceEntry : source.entries) {
				if (sourceEntry.incarnation == destinationEntry.incarnation) {
					sameIncarnation++;
					if (destinationEntry.sameAuthorizedCopyContinuity(
							sourceEntry)) {
						compatible++;
					}
				}
			}
			if (sameIncarnation > 0 && compatible != 1) {
				throw new IllegalArgumentException(
						"Token-ledger copy entries lack one reverse mapping");
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
		return rebaseCopiedRetainedToken(token, currentOwnerIdentity,
				immediateCopySourceOwner)
				.filter(this::validatesCurrentToken)
				.filter(remapped -> published
						.findPointAdmissibleSolution(remapped).isPresent());
	}

	/**
	 * Validates and remaps one exact retained token from an immediate closure
	 * copy, without requiring that the selected root is current in this revision.
	 *
	 * @return the exact copied current-or-dormant token
	 */
	public Optional<String> rebaseCopiedRetainedToken(String token,
			String currentOwnerIdentity, String immediateCopySourceOwner) {
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
		String remapped = null;
		for (Entry candidate : current.entries) {
			if (candidate.incarnation == old.incarnation
					&& candidate.sameAuthorizedCopyContinuity(old)) {
				if (remapped != null) {
					return Optional.empty();
				}
				remapped = current.token(candidate);
			}
		}
		return Optional.ofNullable(remapped);
	}

	/** @return whether the exact token is an active entry of this ledger */
	public boolean validatesCurrentToken(String token) {
		return current != null && current.validatedEntry(token)
				.filter(entry -> entry.status.isActive()).isPresent();
	}

	/** @return whether the exact token is retained as current or dormant */
	public boolean validatesRetainedToken(String token) {
		return current != null && current.validatedEntry(token).isPresent();
	}

	/**
	 * Retains one exact allocation for an existing materialized point child.
	 *
	 * @return whether the exact current-or-dormant token was retained
	 */
	public boolean retainMaterializedToken(String token) {
		if (current == null || token == null) {
			return false;
		}
		Optional<Entry> validated = current.validatedEntry(token);
		if (!validated.isPresent()) {
			return false;
		}
		int count;
		try {
			count = Math.addExact(materializedClaimCounts.getOrDefault(token, 0), 1);
		} catch (ArithmeticException exception) {
			throw new IllegalStateException(
					"Materialized token claim count exhausted", exception);
		}
		materializedClaimCounts.put(token, count);
		Entry entry = validated.get();
		if (entry.status == Status.ACTIVE) {
			current = current.replacing(entry,
					entry.withStatus(Status.CLAIMED_ACTIVE));
		} else if (entry.status == Status.PERIODIC_QUARANTINE) {
			current = current.replacing(entry,
					entry.withStatus(Status.CLAIMED_PERIODIC_QUARANTINE));
		}
		return true;
	}

	/** Releases one runtime materialized-point claim without retiring identity. */
	public void releaseMaterializedToken(String token) {
		Integer count = token == null ? null : materializedClaimCounts.get(token);
		if (count == null) {
			return;
		}
		if (count > 1) {
			materializedClaimCounts.put(token, count - 1);
			return;
		}
		materializedClaimCounts.remove(token);
		if (current == null) {
			return;
		}
		Optional<Entry> validated = current.validatedEntry(token);
		if (!validated.isPresent()) {
			return;
		}
		Entry entry = validated.get();
		if (entry.status == Status.CLAIMED_ACTIVE) {
			current = current.replacing(entry, entry.withStatus(Status.ACTIVE));
		} else if (entry.status == Status.CLAIMED_DORMANT) {
			current = current.without(entry);
		} else if (entry.status == Status.CLAIMED_PERIODIC_QUARANTINE) {
			Entry unclaimed = entry.withStatus(Status.PERIODIC_QUARANTINE);
			current = current.replacing(entry, unclaimed);
			pruneUnclaimedPeriodicQuarantineGroup(unclaimed);
		}
	}

	private void pruneUnclaimedPeriodicQuarantineGroup(Entry member) {
		Optional<CurrentRootAllocationKey> memberBinding =
				currentRootAllocation(member);
		if (current == null || !memberBinding.isPresent()) {
			return;
		}
		CurrentRootAllocationKey expected = memberBinding.get();
		ArrayList<Entry> group = new ArrayList<>();
		for (Entry entry : current.entries) {
			Optional<CurrentRootAllocationKey> binding =
					currentRootAllocation(entry);
			if (!entry.status.isPeriodicallyQuarantined()
					|| !binding.isPresent()
					|| !binding.get().samePhaseGroup(expected)) {
				continue;
			}
			String token = current.token(entry);
			if (entry.status.isClaimed()
					|| materializedClaimCounts.getOrDefault(token, 0) > 0) {
				return;
			}
			group.add(entry);
		}
		if (group.isEmpty()) {
			return;
		}
		ArrayList<Entry> retained = new ArrayList<>(current.entries);
		retained.removeAll(group);
		current = new Snapshot(current.material, retained);
	}

	/** @return strict compact XML attribute value for this ledger */
	public String exportState() {
		return FORMAT_VERSION + "|" + nextIncarnation + "|"
				+ encodeSnapshot(current, FORMAT_VERSION) + "|"
				+ encodeSnapshot(copySource, FORMAT_VERSION);
	}

	/** Restores only durable lineage/high-water evidence, never numeric results. */
	public void importState(String state) {
		String[] fields = requireText(state, "Token-ledger state")
				.split("\\|", -1);
		if (fields.length != 4 || (!FORMAT_VERSION.equals(fields[0])
				&& !PHASE_FORMAT_VERSION.equals(fields[0])
				&& !PREVIOUS_FORMAT_VERSION.equals(fields[0])
				&& !LEGACY_FORMAT_VERSION.equals(fields[0]))) {
			throw new IllegalArgumentException("Unsupported token-ledger state");
		}
		String importedVersion = fields[0];
		long parsedNext = parsePositiveLong(fields[1], "next incarnation");
		Snapshot parsedCurrent = decodeSnapshot(fields[2], importedVersion);
		Snapshot parsedCopySource = decodeSnapshot(fields[3], importedVersion);
		if (parsedCurrent == null && parsedCopySource != null) {
			throw new IllegalArgumentException(
					"Token-ledger copy source requires a current snapshot");
		}
		long maximum = maximumIncarnation(parsedCurrent, parsedCopySource);
		if (parsedNext <= maximum) {
			throw new IllegalArgumentException(
					"Token-ledger high-water mark is not monotone");
		}
		String canonical = importedVersion + "|" + parsedNext + "|"
				+ encodeSnapshot(parsedCurrent, importedVersion) + "|"
				+ encodeSnapshot(parsedCopySource, importedVersion);
		if (!canonical.equals(state)) {
			throw new IllegalArgumentException(
					"Token-ledger state is not canonically encoded");
		}
		nextIncarnation = parsedNext;
		current = parsedCurrent;
		copySource = parsedCopySource;
		authorizedCopySourceOwner = null;
		materializedClaimCounts.clear();
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
		private final Map<CurrentRootAllocationKey, Entry> stagedByBinding =
				new LinkedHashMap<>();
		private final java.util.Set<ContinuityKey> stagedContinuities =
				new HashSet<>();
		private final Map<String, Integer> tokenUses = new LinkedHashMap<>();
		private final Map<String, Integer> semanticUses = new LinkedHashMap<>();
		private final java.util.Set<String> duplicateTokens = new HashSet<>();
		private final java.util.Set<String> duplicateSemanticKeys = new HashSet<>();
		private final long freshEpoch;
		private long nextAllocatedIncarnation;
		private boolean usedFreshEpoch;
		private long revisionLocalHandleOrdinal;
		private final String authorizedCopySourceOwner;
		private final java.util.Set<String> permanentlyRetiredTokens =
				new LinkedHashSet<>();
		private final java.util.Set<String> periodicallyQuarantinedTokens =
				new LinkedHashSet<>();
		private final java.util.Set<String> releasedPeriodicQuarantineTokens =
				new LinkedHashSet<>();
		private final Map<String, CurrentRootAllocationKey>
				authorizedCopySuccessorBindings = new LinkedHashMap<>();
		private final Map<String, Entry> authorizedCopyQuarantineEntries =
				new LinkedHashMap<>();
		private boolean finished;

		private Evaluation(Material material, long nextIncarnation,
				Snapshot startingSnapshot, String authorizedCopySourceOwner) {
			this.material = material;
			this.freshEpoch = nextIncarnation;
			this.nextAllocatedIncarnation = nextIncarnation;
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
			ensureOpen();
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
						.withStatus(selected.status.isClaimed()
								? Status.CLAIMED_ACTIVE : Status.ACTIVE);
			}
			return stage(selected);
		}

		/**
		 * Retires one exact prior allocation after the caller has established an
		 * intrinsically non-reactivatable identity transition (for example typed
		 * unresolved periodic monodromy). Ordinary ambiguity or absence must not use
		 * this seam; claimed allocations become dormant instead.
		 *
		 * @param token exact prior token to retire permanently
		 */
		public void retirePersistedToken(String token) {
			ensureOpen();
			if (startingSnapshot == null
					|| !startingSnapshot.validatedEntry(token).isPresent()) {
				throw new IllegalArgumentException(
						"Permanent retirement requires an exact prior token");
			}
			periodicallyQuarantinedTokens.remove(token);
			releasedPeriodicQuarantineTokens.remove(token);
			permanentlyRetiredTokens.add(token);
		}

		/**
		 * Retains exact periodic phase evidence while current cyclic identity is
		 * insufficiently established. Quarantine is durable, but never becomes root
		 * identity and never permits a competing fresh allocation.
		 *
		 * @param tokens complete exact prior collision-group tokens
		 */
		public void quarantinePersistedPeriodicTokens(List<String> tokens) {
			ensureOpen();
			List<Entry> validatedGroup = completePeriodicCollisionGroup(tokens,
					false);
			boolean retainedConsumer = false;
			for (Entry entry : validatedGroup) {
				String token = startingSnapshot.token(entry);
				if (permanentlyRetiredTokens.contains(token)) {
					throw new IllegalStateException(
							"A retired token cannot enter periodic quarantine");
				}
				retainedConsumer |= isClaimed(this, entry);
			}
			if (retainedConsumer) {
				periodicallyQuarantinedTokens.addAll(tokens);
				releasedPeriodicQuarantineTokens.removeAll(tokens);
			}
		}

		/**
		 * Reframes a group entering or already in copied quarantine under the
		 * destination continuation contract without releasing it or changing its
		 * selector/incarnation/phase evidence.
		 */
		void rebasePersistedPeriodicQuarantineForCopy(List<String> tokens,
				String successorContract) {
			ensureOpen();
			if (authorizedCopySourceOwner == null) {
				throw new IllegalStateException(
						"Periodic quarantine rebasing requires an authorized copy");
			}
			List<Entry> validatedGroup = completePeriodicCollisionGroup(tokens,
					false);
			String contract = requireText(successorContract,
					"Successor continuation contract");
			for (Entry source : validatedGroup) {
				String token = startingSnapshot.token(source);
				IntersectionRootDeterministicSelector2D selector =
						currentRootAllocationSelector(source).orElseThrow();
				String key = currentRootAllocationKey(contract, selector,
						source.incarnation);
				Entry rebased = allocatedEntry(source.branchLineage, key,
						source.addressProof, source.incarnation, contract, selector)
							.withStatus(source.status);
				Entry prior = authorizedCopyQuarantineEntries.put(token, rebased);
				if (prior != null && !prior.semanticKey().equals(
						rebased.semanticKey())) {
					throw new IllegalStateException(
							"A copied quarantine cannot acquire two contracts");
				}
			}
		}

		/**
		 * Releases one complete quarantined group only after the resolver proves the
		 * unique zero cyclic offset from its persisted semantic phase evidence.
		 */
		public void releasePersistedPeriodicQuarantine(List<String> tokens) {
			ensureOpen();
			if (authorizedCopySourceOwner != null) {
				throw new IllegalStateException(
						"An authorized copy cannot release periodic quarantine");
			}
			completePeriodicCollisionGroup(tokens, true);
			releasedPeriodicQuarantineTokens.addAll(tokens);
		}

		private List<Entry> completePeriodicCollisionGroup(List<String> tokens,
				boolean requireQuarantine) {
			if (startingSnapshot == null || tokens == null || tokens.isEmpty()) {
				throw new IllegalArgumentException(
						"Periodic operation requires prior group evidence");
			}
			LinkedHashSet<String> exactTokens = new LinkedHashSet<>();
			ArrayList<Entry> entries = new ArrayList<>();
			PeriodicAllocationGroupKey expectedGroup = null;
			boolean[] ranks = null;
			for (String token : tokens) {
				if (token == null || !exactTokens.add(token)) {
					throw new IllegalArgumentException(
							"Periodic group tokens must be exact and distinct");
				}
				Entry entry = startingSnapshot.validatedEntry(token).orElseThrow(
						() -> new IllegalArgumentException(
								"Periodic operation requires an exact prior token"));
				CurrentRootAllocationKey binding = currentRootAllocation(entry)
						.orElseThrow(() -> new IllegalArgumentException(
								"Periodic operation requires a root selector"));
				PeriodicAllocationGroupKey group = PeriodicAllocationGroupKey
						.from(binding).orElseThrow(() -> new IllegalArgumentException(
								"Periodic operation requires periodic phase identity"));
				if (expectedGroup == null) {
					expectedGroup = group;
					ranks = new boolean[group.cardinality];
				} else if (!expectedGroup.equals(group)) {
					throw new IllegalArgumentException(
							"Periodic group tokens must be homogeneous");
				}
				int rank = binding.selector.getIntrinsicPhaseRank().orElseThrow();
				if (ranks[rank]) {
					throw new IllegalArgumentException(
							"Periodic group contains a duplicate phase rank");
				}
				ranks[rank] = true;
				if (requireQuarantine
						&& !entry.status.isPeriodicallyQuarantined()) {
					throw new IllegalArgumentException(
							"Periodic group is not wholly quarantined");
				}
				entries.add(entry);
			}
			PeriodicAllocationGroupKey completeGroup = expectedGroup;
			LinkedHashSet<String> completeStartingGroup = new LinkedHashSet<>();
			for (Entry entry : startingEntries) {
				if (periodicAllocationGroup(entry).filter(completeGroup::equals)
						.isPresent()) {
					completeStartingGroup.add(startingSnapshot.token(entry));
				}
			}
			if (ranks == null || entries.size() != ranks.length
					|| completeStartingGroup.size() != ranks.length
					|| !completeStartingGroup.equals(exactTokens)) {
				throw new IllegalArgumentException(
						"Periodic operation requires one complete collision group");
			}
			for (boolean present : ranks) {
				if (!present) {
					throw new IllegalArgumentException(
							"Periodic operation requires every intrinsic phase rank");
				}
			}
			return List.copyOf(entries);
		}

		/**
		 * Returns immutable prior periodic phase evidence for one exact query
		 * contract. Canonical parameter bits are revision evidence, never identity.
		 */
		List<PersistedPhaseAllocation> persistedPhaseAllocations(String contract) {
			ensureOpen();
			if (startingSnapshot == null) {
				return List.of();
			}
			String expectedContract = requireText(contract,
					"Continuation contract");
			ArrayList<PersistedPhaseAllocation> allocations = new ArrayList<>();
			for (Entry entry : startingEntries) {
				Optional<CurrentRootAllocationKey> binding =
						currentRootAllocation(entry);
				if (!binding.isPresent()
						|| (!binding.get().contract.equals(expectedContract)
								&& authorizedCopySourceOwner == null)) {
					continue;
				}
				IntersectionRootDeterministicSelector2D selector =
						binding.get().selector;
				if (!selector.hasIntrinsicPhase() || !selector.isPeriodicPhase()) {
					continue;
				}
				allocations.add(new PersistedPhaseAllocation(
						startingSnapshot.token(entry), selector,
						Double.longBitsToDouble(entry.addressProof
								.getCanonicalParameterBits()),
						entry.status.isPeriodicallyQuarantined()));
			}
			allocations.sort(Comparator.comparing(
					PersistedPhaseAllocation::getSelector));
			return List.copyOf(allocations);
		}

		/** Returns one collision group from the immutable batch evidence. */
		List<PersistedPhaseAllocation> persistedPhaseAllocations(String contract,
				IntersectionRootDeterministicSelector2D baseSelector) {
			java.util.Objects.requireNonNull(baseSelector);
			ArrayList<PersistedPhaseAllocation> matching = new ArrayList<>();
			for (PersistedPhaseAllocation allocation
					: persistedPhaseAllocations(contract)) {
				IntersectionRootDeterministicSelector2D selector =
						allocation.getSelector();
				if (selector.getComponentLineage().equals(
						baseSelector.getComponentLineage())
						&& selector.getCurrentRootGerm().equals(
								baseSelector.getCurrentRootGerm())) {
					matching.add(allocation);
				}
			}
			return List.copyOf(matching);
		}

		boolean isAuthorizedCopy() {
			return authorizedCopySourceOwner != null;
		}

		/**
		 * Resolves one unique current root through intrinsic semantic selection.
		 *
		 * <p>The selector, branch and provider/target contract are current-snapshot
		 * authority. A matching committed allocation is resumed independently of
		 * the root's previous parameter value or the path used to reach this
		 * snapshot. Exact parameter bits are replaced only as revision evidence.
		 * Duplicate calls for one selector stage the same token so the ordinary
		 * duplicate-publication rules fail closed.</p>
		 *
		 * @param branchLineage exact stable semantic component lineage
		 * @param continuationContract stable provider/parameter/target contract
		 * @param selector unique intrinsic selector in the current snapshot
		 * @param addressProof current revision address proof
		 * @return current opaque allocation and lifecycle disposition
		 */
		public IntersectionRootAllocation2D resolveCurrentRoot(
				String branchLineage, String continuationContract,
				IntersectionRootDeterministicSelector2D selector,
				IntersectionRootAddressProof2D addressProof) {
			return resolveCurrentRoot(branchLineage, continuationContract,
					selector, addressProof, false);
		}

		/**
		 * Resolves a current root and, when explicitly safe, binds one exact R3
		 * singleton allocation to the new selector without changing its token.
		 *
		 * @param allowLegacySingletonBinding whether the caller proved that this is
		 *        the only finite current root on the exact semantic component
		 * @return current opaque allocation and lifecycle disposition
		 */
		public IntersectionRootAllocation2D resolveCurrentRoot(
				String branchLineage, String continuationContract,
				IntersectionRootDeterministicSelector2D selector,
				IntersectionRootAddressProof2D addressProof,
				boolean allowLegacySingletonBinding) {
			ensureOpen();
			String branch = requireText(branchLineage, "Branch lineage");
			String contract = requireText(continuationContract,
					"Continuation contract");
			java.util.Objects.requireNonNull(selector);
			java.util.Objects.requireNonNull(addressProof);
			Entry selected = uniqueStagedAllocation(branch, contract, selector,
					addressProof);
			boolean reused = selected != null;
			if (selected == null) {
				selected = uniqueStartingAllocation(branch, contract, selector,
						addressProof);
				reused = selected != null;
			}
			if (selected == null && allowLegacySingletonBinding) {
				selected = uniqueLegacySingletonAllocation(branch, contract,
						selector, addressProof, false);
				reused = selected != null;
			}
			if (selected == null && authorizedCopySourceOwner != null) {
				Entry copySource = uniqueAuthorizedCopyStartingAllocation(branch,
						selector, addressProof);
				if (copySource == null && allowLegacySingletonBinding) {
					copySource = uniqueLegacySingletonAllocation(branch, contract,
							selector, addressProof, true);
				}
				if (copySource != null) {
					selected = allocatedEntry(branch,
							currentRootAllocationKey(contract, selector,
									copySource.incarnation),
							addressProof, copySource.incarnation, contract, selector)
							.withStatus(copySource.status.isClaimed()
									? Status.CLAIMED_ACTIVE : Status.ACTIVE);
					CurrentRootAllocationKey successorBinding =
							selected.currentRootBinding.orElseThrow();
					CurrentRootAllocationKey priorBinding =
							authorizedCopySuccessorBindings.put(
									startingSnapshot.token(copySource),
									successorBinding);
					if (priorBinding != null
							&& !priorBinding.equals(successorBinding)) {
						throw new IllegalStateException(
								"One copied token cannot acquire two current selectors");
					}
					reused = true;
				}
			}
			if (selected == null) {
				if (hasBlockedPeriodicAllocation(branch, contract, selector,
						addressProof)) {
					throw new IllegalStateException(
							"Periodic quarantine blocks a competing allocation");
				}
				long incarnation = allocateIncarnation();
				selected = allocatedEntry(branch,
						currentRootAllocationKey(contract, selector, incarnation),
						addressProof, incarnation, contract, selector);
			} else {
				selected = selected.withAddressProof(addressProof)
						.withStatus(selected.status.isClaimed()
								? Status.CLAIMED_ACTIVE : Status.ACTIVE);
			}
			String token = stage(selected);
			return new IntersectionRootAllocation2D(token,
					selected.continuationKey.orElseThrow(), reused);
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

		private void ensureOpen() {
			if (finished) {
				throw new IllegalStateException(
						"Token evaluation is already finished");
			}
		}

		private long allocateIncarnation() {
			long incarnation = nextAllocatedIncarnation;
			if (incarnation == Long.MAX_VALUE) {
				throw new IllegalStateException(
						"Root token incarnation sequence exhausted");
			}
			nextAllocatedIncarnation = Math.addExact(incarnation, 1);
			usedFreshEpoch = true;
			return incarnation;
		}

		private Entry uniqueStagedAllocation(String branch, String contract,
				IntersectionRootDeterministicSelector2D selector,
				IntersectionRootAddressProof2D addressProof) {
			Entry found = stagedByBinding.get(
					new CurrentRootAllocationKey(contract, selector));
			return found != null && found.branchLineage.equals(branch)
					&& sameTargetContract(found, addressProof) ? found : null;
		}

		private Entry uniqueStartingAllocation(String branch, String contract,
				IntersectionRootDeterministicSelector2D selector,
				IntersectionRootAddressProof2D addressProof) {
			if (startingSnapshot == null) {
				return null;
			}
			Entry found = startingSnapshot.byBinding.get(
					new CurrentRootAllocationKey(contract, selector));
			if (found == null || !found.branchLineage.equals(branch)
					|| !sameTargetContract(found, addressProof)) {
				return null;
			}
			String token = startingSnapshot.token(found);
			return !found.status.isPeriodicallyQuarantined()
					|| releasedPeriodicQuarantineTokens.contains(token)
							? found : null;
		}

		private boolean hasBlockedPeriodicAllocation(String branch, String contract,
				IntersectionRootDeterministicSelector2D selector,
				IntersectionRootAddressProof2D addressProof) {
			if (startingSnapshot == null) {
				return false;
			}
			Entry found = startingSnapshot.byBinding.get(
					new CurrentRootAllocationKey(contract, selector));
			if (found != null && found.status.isPeriodicallyQuarantined()
					&& !releasedPeriodicQuarantineTokens.contains(
							startingSnapshot.token(found))
					&& found.branchLineage.equals(branch)
					&& sameTargetContract(found, addressProof)) {
				return true;
			}
			if (authorizedCopySourceOwner == null) {
				return false;
			}
			for (Entry entry : startingEntries) {
				if (entry.status.isPeriodicallyQuarantined()
						&& entry.branchLineage.equals(branch)
						&& currentRootAllocationSelector(entry)
								.filter(selector::equals).isPresent()
						&& entry.addressProof
								.sameAddressUnderAuthorizedCopy(addressProof)) {
					return true;
				}
			}
			return false;
		}

		private Entry uniqueLegacySingletonAllocation(String branch,
				String contract,
				IntersectionRootDeterministicSelector2D selector,
				IntersectionRootAddressProof2D addressProof,
				boolean authorizedCopy) {
			if (selector.hasIntrinsicPhase()) {
				return null;
			}
			Entry found = null;
			for (Entry entry : startingEntries) {
				boolean matchingAddress = authorizedCopy
						? entry.addressProof.sameAddressUnderAuthorizedCopy(
								addressProof)
						: entry.addressProof.equals(addressProof);
				if (isLegacyPublicSingletonAllocation(entry, branch)
						&& matchingAddress) {
					if (found != null) {
						return null;
					}
					found = entry.withCurrentRootBinding(contract, selector);
				}
			}
			return found;
		}

		private boolean sameTargetContract(Entry entry,
				IntersectionRootAddressProof2D addressProof) {
			return entry.addressProof.getTargetContractSignature().equals(
					addressProof.getTargetContractSignature());
		}

		private Entry uniqueAuthorizedCopyStartingAllocation(String branch,
				IntersectionRootDeterministicSelector2D selector,
				IntersectionRootAddressProof2D addressProof) {
			Entry found = null;
			for (Entry entry : startingEntries) {
				if (!entry.status.isPeriodicallyQuarantined()
						&& entry.branchLineage.equals(branch)
						&& currentRootAllocationSelector(entry)
								.filter(selector::equals).isPresent()
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

		private String stage(Entry selected) {
			String token = material.token(selected);
			byToken.put(token, selected);
			selected.currentRootBinding.ifPresent(binding ->
					stagedByBinding.put(binding, selected));
			stagedContinuities.add(selected.continuityKey());
			String semantic = selected.semanticKey();
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
			return stagedContinuities.contains(published.continuityKey());
		}
	}

	/** Persisted intrinsic phase evidence exposed only to the R4 resolver. */
	static final class PersistedPhaseAllocation {
		private final String token;
		private final IntersectionRootDeterministicSelector2D selector;
		private final double canonicalParameter;
		private final boolean periodicallyQuarantined;

		private PersistedPhaseAllocation(String token,
				IntersectionRootDeterministicSelector2D selector,
				double canonicalParameter, boolean periodicallyQuarantined) {
			this.token = token;
			this.selector = selector;
			this.canonicalParameter = canonicalParameter;
			this.periodicallyQuarantined = periodicallyQuarantined;
		}

		String getToken() {
			return token;
		}

		IntersectionRootDeterministicSelector2D getSelector() {
			return selector;
		}

		double getCanonicalParameter() {
			return canonicalParameter;
		}

		boolean isPeriodicallyQuarantined() {
			return periodicallyQuarantined;
		}
	}

	private static Entry allocatedEntry(String branch, String key,
			IntersectionRootAddressProof2D addressProof, long incarnation,
			String contract,
			IntersectionRootDeterministicSelector2D selector) {
		Optional<String> continuation = Optional.of(key);
		String solution = branch + "/solution/" + key;
		return new Entry(solution, branch, continuation, addressProof,
				incarnation, Status.ACTIVE).withCurrentRootBinding(contract,
						selector);
	}

	private static String currentRootAllocationKey(String contract,
			IntersectionRootDeterministicSelector2D selector,
			long incarnation) {
		String selected = selector.toExternalForm();
		return CURRENT_ROOT_ALLOCATION_PREFIX + framed(contract)
				+ framed(selected) + Long.toUnsignedString(incarnation, 16);
	}

	private static boolean isCurrentRootAllocation(Entry entry,
			String expectedContract,
			IntersectionRootDeterministicSelector2D expectedSelector) {
		return currentRootAllocation(entry).filter(key ->
				key.contract.equals(expectedContract)
						&& key.selector.equals(expectedSelector)).isPresent();
	}

	private static Optional<String> currentRootAllocationContract(Entry entry) {
		return currentRootAllocation(entry).map(key -> key.contract);
	}

	private static Optional<IntersectionRootDeterministicSelector2D>
			currentRootAllocationSelector(Entry entry) {
		return currentRootAllocation(entry).map(key -> key.selector);
	}

	/**
	 * Reads the exact selector framed in one published R4 continuation key.
	 *
	 * <p>This is used only as prior-snapshot topology evidence. It avoids
	 * reconstructing a historical selector with the current domain/provider
	 * definition. Legacy singleton keys deliberately return empty.</p>
	 *
	 * @param continuationKey exact published continuation key
	 * @return canonical framed selector, or empty for legacy/malformed material
	 */
	static Optional<IntersectionRootDeterministicSelector2D>
			selectorFromContinuationKey(String continuationKey) {
		if (continuationKey == null
				|| !continuationKey.startsWith(CURRENT_ROOT_ALLOCATION_PREFIX)) {
			return Optional.empty();
		}
		try {
			String encoded = continuationKey.substring(
					CURRENT_ROOT_ALLOCATION_PREFIX.length());
			Frame contract = readFrame(encoded, 0);
			Frame selector = readFrame(encoded, contract.nextOffset);
			String incarnation = encoded.substring(selector.nextOffset);
			long parsedIncarnation = Long.parseUnsignedLong(incarnation, 16);
			if (parsedIncarnation < 1
					|| !Long.toUnsignedString(parsedIncarnation, 16)
							.equals(incarnation)) {
				return Optional.empty();
			}
			return Optional.of(IntersectionRootDeterministicSelector2D.parse(
					selector.value));
		} catch (ArithmeticException | IllegalArgumentException exception) {
			return Optional.empty();
		}
	}

	private static Optional<CurrentRootAllocationKey> currentRootAllocation(
			Entry entry) {
		Optional<CurrentRootAllocationKey> encoded =
				encodedCurrentRootAllocation(entry);
		if (entry.currentRootBinding.isPresent()) {
			boolean currentR4Key = entry.continuationKey.filter(key ->
					key.startsWith(CURRENT_ROOT_ALLOCATION_PREFIX)).isPresent();
			if (currentR4Key && !entry.currentRootBinding.equals(encoded)) {
				return Optional.empty();
			}
			if (!currentR4Key) {
				if (!hasLegacyPublicSingletonTokenMaterial(entry,
						entry.branchLineage)
						|| entry.currentRootBinding.get().selector
								.hasIntrinsicPhase()) {
					return Optional.empty();
				}
			}
			return entry.currentRootBinding;
		}
		return encoded;
	}

	private static Optional<CurrentRootAllocationKey>
			encodedCurrentRootAllocation(Entry entry) {
		if (!entry.continuationKey.isPresent()) {
			return Optional.empty();
		}
		String key = entry.continuationKey.get();
		if (!key.startsWith(CURRENT_ROOT_ALLOCATION_PREFIX)) {
			return Optional.empty();
		}
		try {
			String encoded = key.substring(
					CURRENT_ROOT_ALLOCATION_PREFIX.length());
			Frame contract = readFrame(encoded, 0);
			Frame selector = readFrame(encoded, contract.nextOffset);
			String incarnation = encoded.substring(selector.nextOffset);
			if (!Long.toUnsignedString(entry.incarnation, 16)
					.equals(incarnation)
					|| Long.parseUnsignedLong(incarnation, 16)
							!= entry.incarnation) {
				return Optional.empty();
			}
			return Optional.of(new CurrentRootAllocationKey(contract.value,
					IntersectionRootDeterministicSelector2D.parse(selector.value)));
		} catch (ArithmeticException | NumberFormatException exception) {
			return Optional.empty();
		} catch (IllegalArgumentException exception) {
			return Optional.empty();
		}
	}

	private static boolean isLegacyPublicSingletonAllocation(Entry entry,
			String branch) {
		return !entry.currentRootBinding.isPresent()
				&& hasLegacyPublicSingletonTokenMaterial(entry, branch);
	}

	private static boolean hasLegacyPublicSingletonTokenMaterial(Entry entry,
			String branch) {
		if (!entry.branchLineage.equals(branch)
				|| !entry.continuationKey.isPresent()) {
			return false;
		}
		String continuation = entry.continuationKey.get();
		String expected = LEGACY_PUBLIC_SINGLETON_PREFIX + branch.length()
				+ ":" + branch;
		return continuation.equals(expected)
				&& entry.solutionLineage.equals(branch + "/solution/" + expected);
	}

	private static Frame readFrame(String value, int offset) {
		int separator = value.indexOf(':', offset);
		if (separator <= offset) {
			throw new IllegalArgumentException("Malformed allocation frame");
		}
		String lengthText = value.substring(offset, separator);
		int length = Integer.parseInt(lengthText);
		int start = separator + 1;
		int end = Math.addExact(start, length);
		if (length < 1 || !Integer.toString(length).equals(lengthText)
				|| end > value.length()) {
			throw new IllegalArgumentException("Malformed allocation frame");
		}
		return new Frame(value.substring(start, end), end);
	}

	private static String framed(String value) {
		return value.length() + ":" + value;
	}

	private static final class Frame {
		private final String value;
		private final int nextOffset;

		Frame(String value, int nextOffset) {
			this.value = value;
			this.nextOffset = nextOffset;
		}
	}

	private static final class CurrentRootAllocationKey {
		private final String contract;
		private final IntersectionRootDeterministicSelector2D selector;

		CurrentRootAllocationKey(String contract,
				IntersectionRootDeterministicSelector2D selector) {
			this.contract = contract;
			this.selector = selector;
		}

		private boolean samePhaseGroup(CurrentRootAllocationKey other) {
			Optional<PeriodicAllocationGroupKey> first =
					PeriodicAllocationGroupKey.from(this);
			return first.isPresent()
					&& first.equals(PeriodicAllocationGroupKey.from(other));
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof CurrentRootAllocationKey)) {
				return false;
			}
			CurrentRootAllocationKey key = (CurrentRootAllocationKey) other;
			return contract.equals(key.contract) && selector.equals(key.selector);
		}

		@Override
		public int hashCode() {
			return 31 * contract.hashCode() + selector.hashCode();
		}
	}

	private static final class PeriodicAllocationGroupKey {
		private final String contract;
		private final String componentLineage;
		private final String rootGerm;
		private final Orientation orientation;
		private final int cardinality;

		private PeriodicAllocationGroupKey(String contract,
				IntersectionRootDeterministicSelector2D selector) {
			this.contract = contract;
			this.componentLineage = selector.getComponentLineage();
			this.rootGerm = selector.getCurrentRootGerm();
			this.orientation = selector.getPhaseOrientation().orElseThrow();
			this.cardinality = selector.getCollisionCardinality().orElseThrow();
		}

		private static Optional<PeriodicAllocationGroupKey> from(
				CurrentRootAllocationKey binding) {
			IntersectionRootDeterministicSelector2D selector = binding.selector;
			if (!selector.hasIntrinsicPhase() || !selector.isPeriodicPhase()) {
				return Optional.empty();
			}
			return Optional.of(new PeriodicAllocationGroupKey(binding.contract,
					selector));
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof PeriodicAllocationGroupKey)) {
				return false;
			}
			PeriodicAllocationGroupKey key = (PeriodicAllocationGroupKey) other;
			return contract.equals(key.contract)
					&& componentLineage.equals(key.componentLineage)
					&& rootGerm.equals(key.rootGerm)
					&& orientation == key.orientation
					&& cardinality == key.cardinality;
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(contract, componentLineage, rootGerm,
					orientation, cardinality);
		}
	}

	private static final class ContinuityKey {
		private final String semanticKey;
		private final IntersectionRootAddressProof2D addressProof;
		private final Optional<CurrentRootAllocationKey> currentRootBinding;

		private ContinuityKey(String semanticKey,
				IntersectionRootAddressProof2D addressProof,
				Optional<CurrentRootAllocationKey> currentRootBinding) {
			this.semanticKey = semanticKey;
			this.addressProof = addressProof;
			this.currentRootBinding = currentRootBinding;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof ContinuityKey)) {
				return false;
			}
			ContinuityKey key = (ContinuityKey) other;
			return semanticKey.equals(key.semanticKey)
					&& addressProof.equals(key.addressProof)
					&& currentRootBinding.equals(key.currentRootBinding);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(semanticKey, addressProof,
					currentRootBinding);
		}
	}

	private enum Status {
		ACTIVE("a", true, false, false),
		CLAIMED_ACTIVE("c", true, true, false),
		CLAIMED_DORMANT("d", false, true, false),
		PERIODIC_QUARANTINE("q", false, false, true),
		CLAIMED_PERIODIC_QUARANTINE("r", false, true, true);

		private final String code;
		private final boolean active;
		private final boolean claimed;
		private final boolean periodicallyQuarantined;

		Status(String code, boolean active, boolean claimed,
				boolean periodicallyQuarantined) {
			this.code = code;
			this.active = active;
			this.claimed = claimed;
			this.periodicallyQuarantined = periodicallyQuarantined;
		}

		private boolean isActive() {
			return active;
		}

		private boolean isClaimed() {
			return claimed;
		}

		private boolean isPeriodicallyQuarantined() {
			return periodicallyQuarantined;
		}

		private static Status parse(String value, String version) {
			for (Status status : values()) {
				if (status.code.equals(value)) {
					if (status != ACTIVE && !FORMAT_VERSION.equals(version)) {
						throw new IllegalArgumentException(
								"Legacy token ledger cannot declare retained status");
					}
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
		private final Optional<CurrentRootAllocationKey> currentRootBinding;

		private Entry(String solutionLineage, String branchLineage,
				Optional<String> continuationKey,
				IntersectionRootAddressProof2D addressProof, long incarnation,
				Status status) {
			this(solutionLineage, branchLineage, continuationKey, addressProof,
					incarnation, status, Optional.empty());
		}

		private Entry(String solutionLineage, String branchLineage,
				Optional<String> continuationKey,
				IntersectionRootAddressProof2D addressProof, long incarnation,
				Status status,
				Optional<CurrentRootAllocationKey> currentRootBinding) {
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
			this.currentRootBinding = java.util.Objects.requireNonNull(
					currentRootBinding);
		}

		private Entry withStatus(Status newStatus) {
			return new Entry(solutionLineage, branchLineage, continuationKey,
					addressProof, incarnation, newStatus, currentRootBinding);
		}

		private Entry withAddressProof(
				IntersectionRootAddressProof2D newAddressProof) {
			return new Entry(solutionLineage, branchLineage, continuationKey,
					newAddressProof, incarnation, status, currentRootBinding);
		}

		private Entry withCurrentRootBinding(String contract,
				IntersectionRootDeterministicSelector2D selector) {
			return new Entry(solutionLineage, branchLineage, continuationKey,
					addressProof, incarnation, status,
					Optional.of(new CurrentRootAllocationKey(
							requireText(contract, "Continuation contract"),
							java.util.Objects.requireNonNull(selector))));
		}

		private String semanticKey() {
			return LocusIntersectionTokenLedger2D.semanticKey(solutionLineage,
					branchLineage, continuationKey);
		}

		private ContinuityKey continuityKey() {
			return new ContinuityKey(semanticKey(), addressProof,
					currentRootBinding);
		}

		private boolean sameAuthorizedCopyContinuity(Entry other) {
			boolean copiedAllocation = incarnation == other.incarnation
					&& branchLineage.equals(other.branchLineage)
					&& currentRootAllocationSelector(this).isPresent()
					&& currentRootAllocationSelector(this).equals(
							currentRootAllocationSelector(other));
			boolean migratedLegacyAllocation = incarnation == other.incarnation
					&& branchLineage.equals(other.branchLineage)
					&& currentRootBinding.isPresent()
					&& isLegacyPublicSingletonAllocation(other,
							other.branchLineage);
			return (copiedAllocation
					|| migratedLegacyAllocation
					|| semanticKey().equals(other.semanticKey()))
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
		private final Map<CurrentRootAllocationKey, Entry> byBinding;
		private final Map<String, Entry> byToken;

		private Snapshot(Material material, List<Entry> entries) {
			this.material = java.util.Objects.requireNonNull(material);
			ArrayList<Entry> sorted = new ArrayList<>(entries);
			sorted.sort(Comparator.comparing(Entry::semanticKey));
			this.entries = List.copyOf(sorted);
			LinkedHashMap<String, Entry> bySemantic = new LinkedHashMap<>();
			LinkedHashMap<CurrentRootAllocationKey, Entry> indexedByBinding =
					new LinkedHashMap<>();
			LinkedHashMap<String, Entry> indexedByToken = new LinkedHashMap<>();
			for (Entry entry : this.entries) {
				if (entry.currentRootBinding.isPresent()
						&& !currentRootAllocation(entry).equals(
								entry.currentRootBinding)) {
					throw new IllegalArgumentException(
							"Token ledger has an inconsistent root binding");
				}
				if (!entry.currentRootBinding.isPresent()
						&& entry.continuationKey.filter(key -> key.startsWith(
								CURRENT_ROOT_ALLOCATION_PREFIX)).isPresent()) {
					throw new IllegalArgumentException(
							"R4 token allocation lacks its selector binding");
				}
				if (entry.currentRootBinding.isPresent()
						&& indexedByBinding.put(entry.currentRootBinding.get(), entry)
								!= null) {
					throw new IllegalArgumentException(
							"Token ledger has a duplicate deterministic binding");
				}
				Entry duplicate = bySemantic.put(entry.semanticKey(), entry);
				if (duplicate != null) {
					throw new IllegalArgumentException(
							"Token ledger has duplicate semantic continuity");
				}
				if (indexedByToken.put(token(entry), entry) != null) {
					throw new IllegalArgumentException(
							"Token ledger has a duplicate opaque token");
				}
			}
			this.byBinding = java.util.Collections.unmodifiableMap(indexedByBinding);
			this.byToken = java.util.Collections.unmodifiableMap(indexedByToken);
		}

		private String token(Entry entry) {
			return material.token(entry);
		}

		private Optional<Entry> validatedEntry(String token) {
			return token == null ? Optional.empty()
					: Optional.ofNullable(byToken.get(token));
		}

		private Snapshot replacing(Entry oldEntry, Entry replacement) {
			ArrayList<Entry> changed = new ArrayList<>(entries);
			int index = changed.indexOf(oldEntry);
			if (index < 0) {
				throw new IllegalArgumentException(
						"Token-ledger replacement entry is not current");
			}
			changed.set(index, replacement);
			return new Snapshot(material, changed);
		}

		private Snapshot without(Entry oldEntry) {
			ArrayList<Entry> changed = new ArrayList<>(entries);
			if (!changed.remove(oldEntry)) {
				throw new IllegalArgumentException(
						"Token-ledger removal entry is not current");
			}
			return new Snapshot(material, changed);
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
					if (evaluation.periodicallyQuarantinedTokens
							.contains(parent)) {
						continue;
					}
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

	private static String semanticKey(String solution, String branch,
			Optional<String> continuation) {
		return solution.length() + ":" + solution + ":" + branch.length()
				+ ":" + branch + ":" + continuation.orElse("");
	}

	private static String encodeSnapshot(Snapshot snapshot, String version) {
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
			if (hasDeterministicBindingFields(version)) {
				encoded.append(',').append(hex(entry.currentRootBinding
						.map(binding -> binding.contract).orElse("")))
						.append(',').append(hex(entry.currentRootBinding
						.map(binding -> binding.selector.toExternalForm())
						.orElse("")));
			} else if (!LEGACY_FORMAT_VERSION.equals(version)) {
				throw new IllegalArgumentException(
						"Unsupported token-ledger encoding version");
			}
		}
		return encoded.toString();
	}

	private static Snapshot decodeSnapshot(String encoded, String version) {
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
			int expectedFields = hasDeterministicBindingFields(version) ? 10 : 8;
			if (entry.length != expectedFields) {
				throw new IllegalArgumentException("Malformed token-ledger entry");
			}
			String continuation = unhex(entry[4]);
			IntersectionRootAddressProof2D addressProof =
					IntersectionRootAddressProof2D.fromBits(unhex(entry[5]),
							unhex(entry[6]), parseParameterBits(entry[7]));
			Optional<CurrentRootAllocationKey> binding = Optional.empty();
			if (hasDeterministicBindingFields(version)) {
				String contract = unhex(entry[8]);
				String selector = unhex(entry[9]);
				if (contract.isEmpty() != selector.isEmpty()) {
					throw new IllegalArgumentException(
							"Incomplete deterministic root binding");
				}
				if (!contract.isEmpty()) {
					IntersectionRootDeterministicSelector2D parsedSelector =
							IntersectionRootDeterministicSelector2D.parse(selector);
					if (PREVIOUS_FORMAT_VERSION.equals(version)
							&& parsedSelector.hasIntrinsicPhase()) {
						throw new IllegalArgumentException(
								"Ledger v2 cannot contain intrinsic phase selectors");
					}
					binding = Optional.of(new CurrentRootAllocationKey(contract,
							parsedSelector));
				}
			} else if (!LEGACY_FORMAT_VERSION.equals(version)) {
				throw new IllegalArgumentException(
						"Unsupported token-ledger decoding version");
			}
			entries.add(new Entry(unhex(entry[2]), unhex(entry[3]),
					continuation.isEmpty() ? Optional.empty()
							: Optional.of(continuation),
					addressProof,
					parsePositiveLong(entry[1], "entry incarnation"),
					Status.parse(entry[0], version), binding));
		}
		return new Snapshot(material, entries);
	}

	private static boolean hasDeterministicBindingFields(String version) {
		return FORMAT_VERSION.equals(version)
				|| PHASE_FORMAT_VERSION.equals(version)
				|| PREVIOUS_FORMAT_VERSION.equals(version);
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
