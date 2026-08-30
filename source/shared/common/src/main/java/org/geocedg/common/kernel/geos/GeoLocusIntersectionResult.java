/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.geos;

import java.util.Optional;
import java.util.function.Function;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionV2;
import org.geocedg.common.kernel.locus.intersection.IntersectionSourceBinding2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTokenLedger2D;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoIdentityListener;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.arithmetic.ValueType;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.plugin.GeoClass;

/** Internal nonnumeric rich intersection authority in the normal kernel DAG. */
public final class GeoLocusIntersectionResult extends GeoElement
		implements PersistentGeoIdentityListener {
	private String sourcePairIdentity;
	private final LocusIntersectionTokenLedger2D tokenLedger =
			new LocusIntersectionTokenLedger2D();
	private IntersectionSourceBinding2D currentSourceBinding;
	private LocusIntersectionResult2D intersectionResult;
	private boolean explicitlyUndefined;
	private boolean publicPersistence;
	private boolean tokenLedgerRequiredFromXml;
	private boolean tokenLedgerRestoredFromXml;

	/** Creates an unpublished rich intersection Geo for one constructive pair. */
	public GeoLocusIntersectionResult(Construction construction,
			String sourcePairIdentity) {
		super(construction);
		if (sourcePairIdentity == null || sourcePairIdentity.trim().isEmpty()) {
			throw new IllegalArgumentException("Source-pair identity is required");
		}
		this.sourcePairIdentity = sourcePairIdentity;
		setEuclidianVisible(false);
		setAuxiliaryObject(true);
	}

	public String getSourcePairIdentity() {
		return sourcePairIdentity;
	}

	/** Refreshes the source-pair identity after an authorized copy/load remap. */
	public void refreshSourcePairIdentity(String identity) {
		if (identity == null || identity.trim().isEmpty()) {
			throw new IllegalArgumentException("Source-pair identity is required");
		}
		sourcePairIdentity = identity;
	}

	/** Marks this result as reconstructible through its public parent command. */
	public void enablePublicPersistence() {
		if (!publicPersistence) {
			tokenLedgerRequiredFromXml = cons.isFileLoading();
		}
		publicPersistence = true;
	}

	/**
	 * @return whether a public result may evaluate without inventing ledger state
	 *         that was absent from its serialized element
	 */
	public boolean isTokenLedgerReadyForEvaluation() {
		return !tokenLedgerRequiredFromXml || tokenLedgerRestoredFromXml;
	}

	public IntersectionSourceBinding2D getCurrentSourceBinding() {
		return currentSourceBinding;
	}

	public LocusIntersectionResult2D getIntersectionResult() {
		return intersectionResult;
	}

	/** Makes every previous revision-bound payload unavailable before work. */
	public void beginIntersectionRevision(IntersectionSourceBinding2D binding) {
		if (!sourcePairIdentity.equals(binding.getSourcePairIdentity())) {
			throw new IllegalArgumentException("Source-pair binding mismatch");
		}
		currentSourceBinding = binding;
		intersectionResult = null;
		explicitlyUndefined = false;
	}

	/** Atomically publishes one coherent immutable result snapshot. */
	public void publishIntersectionResult(IntersectionSourceBinding2D binding,
			LocusIntersectionResult2D result) {
		validatePublication(binding, result);
		intersectionResult = java.util.Objects.requireNonNull(result);
		explicitlyUndefined = false;
	}

	/**
	 * Begins one public token-ledger evaluation after durable ID attachment.
	 *
	 * @return isolated token-ledger evaluation
	 */
	public LocusIntersectionTokenLedger2D.Evaluation beginTokenEvaluation(
			String ownerIdentity, String constructiveLineage,
			String topologyContext) {
		if (!publicPersistence) {
			throw new IllegalStateException(
					"Internal G8 intersections do not use the public token ledger");
		}
		return tokenLedger.begin(ownerIdentity, sourcePairIdentity,
				constructiveLineage, topologyContext);
	}

	/** Atomically publishes the rich snapshot and its exact token evidence. */
	public void publishIntersectionResult(IntersectionSourceBinding2D binding,
			LocusIntersectionResult2D result,
			LocusIntersectionTokenLedger2D.Evaluation evaluation) {
		validatePublication(binding, result);
		tokenLedger.commit(evaluation, result);
		intersectionResult = java.util.Objects.requireNonNull(result);
		explicitlyUndefined = false;
	}

	/** Aborts one failed token attempt without publishing provisional evidence. */
	public void abortTokenEvaluation(
			LocusIntersectionTokenLedger2D.Evaluation evaluation) {
		tokenLedger.abort(evaluation);
	}

	/**
	 * Clears only revision-bound payload while the identity section is pending.
	 * Durable ledger evidence is intentionally left untouched.
	 */
	public void deferUntilPersistentIdentityAttachment() {
		currentSourceBinding = null;
		intersectionResult = null;
		explicitlyUndefined = true;
	}

	/**
	 * Finds only a current point whose full token validates against the ledger.
	 *
	 * @return exact current solution, or empty when the token is inadmissible
	 */
	public Optional<LocusIntersectionSolution2D>
			findExactPointAdmissibleSolution(String token) {
		if (!isDefined() || token == null) {
			return Optional.empty();
		}
		if (publicPersistence && !tokenLedger.validatesCurrentToken(token)) {
			return Optional.empty();
		}
		return intersectionResult.findPointAdmissibleSolution(token);
	}

	/**
	 * Rebases only an exact token carried by the same immediate closure copy as
	 * this rich result and its derived point. No freshly typed text, branch-only
	 * match or coordinate fallback is permitted.
	 *
	 * @return rebased exact solution, or empty when copy provenance is invalid
	 */
	public Optional<LocusIntersectionSolution2D>
			rebaseCopiedPointAdmissibleSolution(String token,
					GeoText copiedTokenInput, GeoElement copiedPoint) {
		if (!isDefined()) {
			return Optional.empty();
		}
		return resolveRetainedMaterializedToken(token, copiedTokenInput,
				copiedPoint).flatMap(this::findExactPointAdmissibleSolution);
	}

	/**
	 * Resolves one exact current-or-dormant token for an existing materialized
	 * point, including the one permitted immediate closure-copy rebase.
	 *
	 * @return exact token owned by this result, or empty for invalid provenance
	 */
	public Optional<String> resolveRetainedMaterializedToken(String token,
			GeoText copiedTokenInput, GeoElement copiedPoint) {
		if (!publicPersistence || token == null || copiedTokenInput == null
				|| copiedPoint == null) {
			return Optional.empty();
		}
		SpatialIdentityRegistry registry = cons.getSpatialIdentityRegistry();
		PersistentGeoId ownerId = registry.getPersistentGeoId(this);
		if (ownerId != null && tokenLedger.validatesRetainedToken(token)
				&& tokenLedger.hasCurrentOwner(ownerId.toExternalForm())) {
			return Optional.of(token);
		}
		PersistentGeoId tokenId = registry.getPersistentGeoId(copiedTokenInput);
		PersistentGeoId pointId = registry.getPersistentGeoId(copiedPoint);
		GeoIdentityRecord ownerRecord = ownerId == null ? null
				: registry.getGeoRecord(ownerId);
		GeoIdentityRecord tokenRecord = tokenId == null ? null
				: registry.getGeoRecord(tokenId);
		GeoIdentityRecord pointRecord = pointId == null ? null
				: registry.getGeoRecord(pointId);
		if (ownerRecord == null || ownerRecord.getCopySourceId() == null
				|| tokenRecord == null || tokenRecord.getCopySourceId() == null
				|| pointRecord == null || pointRecord.getCopySourceId() == null
				|| !hasExactDependencies(pointRecord, ownerId, tokenId)) {
			return Optional.empty();
		}
		GeoIdentityRecord sourcePoint = registry.getGeoRecord(
				pointRecord.getCopySourceId());
		if (sourcePoint != null && !hasExactDependencies(sourcePoint,
				ownerRecord.getCopySourceId(), tokenRecord.getCopySourceId())) {
			return Optional.empty();
		}
		return tokenLedger.rebaseCopiedRetainedToken(token,
				ownerId.toExternalForm(),
				ownerRecord.getCopySourceId().toExternalForm());
	}

	/** @return whether one exact point-child token is now retained */
	public boolean retainMaterializedPointToken(String token) {
		return publicPersistence && tokenLedger.retainMaterializedToken(token);
	}

	/** Releases one existing point-child claim without changing root semantics. */
	public void releaseMaterializedPointToken(String token) {
		if (publicPersistence) {
			tokenLedger.releaseMaterializedToken(token);
		}
	}

	/** Strict XML restoration of durable ledger evidence. */
	public void restoreTokenLedgerState(String state) {
		if (!publicPersistence || tokenLedgerRestoredFromXml) {
			throw new IllegalStateException(
					"Unexpected or duplicate public token-ledger state");
		}
		tokenLedger.importState(state);
		tokenLedgerRestoredFromXml = true;
	}

	/** @return canonical persisted ledger state for verification */
	public String getTokenLedgerState() {
		return tokenLedger.exportState();
	}

	/** @return whether the current rich result admits this exact token */
	public boolean isPointAdmissible(String rootToken) {
		return findExactPointAdmissibleSolution(rootToken).isPresent();
	}

	@Override
	public GeoClass getGeoClassType() {
		return GeoClass.LOCUS_INTERSECTION_RESULT;
	}

	@Override
	public ValueType getValueType() {
		return ValueType.VOID;
	}

	@Override
	public String translatedTypeString() {
		return getLoc().getMenu("LocusIntersectionResult");
	}

	@Override
	public String translatedTypeStringForAlgebraView() {
		return translatedTypeString();
	}

	@Override
	public GeoElement copy() {
		GeoLocusIntersectionResult copy = new GeoLocusIntersectionResult(cons,
				sourcePairIdentity);
		copy.publicPersistence = publicPersistence;
		copy.tokenLedger.set(tokenLedger);
		copy.tokenLedgerRequiredFromXml = tokenLedgerRequiredFromXml;
		copy.tokenLedgerRestoredFromXml = tokenLedgerRestoredFromXml;
		return copy;
	}

	@Override
	public GeoElement copyInternal(Construction targetConstruction) {
		GeoLocusIntersectionResult copy = new GeoLocusIntersectionResult(
				targetConstruction, sourcePairIdentity);
		copy.publicPersistence = publicPersistence;
		copy.tokenLedger.set(tokenLedger);
		copy.tokenLedgerRequiredFromXml = tokenLedgerRequiredFromXml;
		copy.tokenLedgerRestoredFromXml = tokenLedgerRestoredFromXml;
		return copy;
	}

	/** Assignment never imports a revision-bound result or continuation state. */
	@Override
	public void set(GeoElementND geo) {
		if (!(geo instanceof GeoLocusIntersectionResult)) {
			throw new IllegalArgumentException(
					"Only another rich intersection result may be assigned");
		}
		currentSourceBinding = null;
		intersectionResult = null;
		explicitlyUndefined = true;
	}

	@Override
	public boolean isDefined() {
		return !explicitlyUndefined && currentSourceBinding != null
				&& intersectionResult != null;
	}

	@Override
	public void setUndefined() {
		intersectionResult = null;
		explicitlyUndefined = true;
	}

	@Override
	public void validatePersistentGeoIdentityAttachment(
			PersistentGeoId attachedId, GeoIdentityRecord attachedRecord,
			Function<GeoElement, GeoIdentityRecord> prospectiveRecord,
			boolean immediateCopy) {
		if (!publicPersistence || !tokenLedger.hasCurrentSnapshot()) {
			return;
		}
		if (!(getParentAlgorithm() instanceof AlgoLocusIntersectionV2)) {
			throw new IllegalArgumentException(
					"Persisted token ledger has no public intersection parent");
		}
		AlgoLocusIntersectionV2 parent =
				(AlgoLocusIntersectionV2) getParentAlgorithm();
		GeoIdentityRecord sourceRecord = prospectiveRecord.apply(
				parent.getSource());
		GeoIdentityRecord targetRecord = prospectiveRecord.apply(
				parent.getTarget());
		if (sourceRecord == null || targetRecord == null
				|| !hasExactDependencies(attachedRecord, sourceRecord.getId(),
						targetRecord.getId())) {
			throw new IllegalArgumentException(
					"Persisted token ledger lacks its exact reconstructed inputs");
		}
		String attachedOwner = attachedId.toExternalForm();
		String declaredCopySource = attachedRecord.getCopySourceId() == null
				? null : attachedRecord.getCopySourceId().toExternalForm();
		String expectedSourcePair;
		String expectedCopySourcePair = null;
		if (tokenLedger.hasCurrentOwner(attachedOwner)) {
			expectedSourcePair = sourcePair(sourceRecord.getId(),
					targetRecord.getId());
			if (declaredCopySource != null) {
				PersistentGeoId sourceCopy = sourceRecord.getCopySourceId();
				PersistentGeoId targetCopy = targetRecord.getCopySourceId();
				if (sourceCopy == null || targetCopy == null) {
					throw new IllegalArgumentException(
							"Token-ledger copy provenance lacks copied inputs");
				}
				expectedCopySourcePair = sourcePair(sourceCopy, targetCopy);
			}
		} else {
			PersistentGeoId sourceCopy = sourceRecord.getCopySourceId();
			PersistentGeoId targetCopy = targetRecord.getCopySourceId();
			if (!immediateCopy || sourceCopy == null || targetCopy == null) {
				throw new IllegalArgumentException(
						"Token-ledger owner transition lacks copied inputs");
			}
			expectedSourcePair = sourcePair(sourceCopy, targetCopy);
		}
		tokenLedger.validatePreattachmentContext(attachedOwner,
				expectedSourcePair,
				parent.getConstructiveIntersectionLineage(),
				parent.getTopologyContext(), declaredCopySource,
				expectedCopySourcePair, immediateCopy);
	}

	@Override
	public void onPersistentGeoIdentityAttached(PersistentGeoId attachedId) {
		try {
			if (publicPersistence && !isTokenLedgerReadyForEvaluation()) {
				deferUntilPersistentIdentityAttachment();
				return;
			}
			PersistentGeoId current = cons.getSpatialIdentityRegistry()
					.getPersistentGeoId(this);
			GeoIdentityRecord record = current == null ? null
					: cons.getSpatialIdentityRegistry().getGeoRecord(current);
			if (!attachedId.equals(current) || record == null) {
				deferUntilPersistentIdentityAttachment();
				return;
			}
			String copySource = record.getCopySourceId() == null ? null
					: record.getCopySourceId().toExternalForm();
			tokenLedger.prepareAttachedOwner(attachedId.toExternalForm(),
					copySource);
			if (getParentAlgorithm() != null) {
				getParentAlgorithm().update();
				// Identity attachment is the publication seam at which an immediate
				// closure copy gains its final owner and exact rebase authority.
				// Recompute existing token children once through the normal DAG so
				// they can consume that authority without waiting for a later edit.
				updateCascade();
			}
		} catch (RuntimeException exception) {
			deferUntilPersistentIdentityAttachment();
		}
	}

	@Override
	public void doRemove() {
		setUndefined();
		currentSourceBinding = null;
		super.doRemove();
	}

	@Override
	public String toValueString(StringTemplate template) {
		if (!isDefined()) {
			return "LocusIntersectionResult[unpublished]";
		}
		return "LocusIntersectionResult[revision="
				+ currentSourceBinding.getLocusSemanticRevision() + ", kind="
				+ intersectionResult.getGeometryKind() + ", completeness="
				+ intersectionResult.getCompletenessEvidence().getCompleteness()
				+ "]";
	}

	@Override
	protected boolean showInEuclidianView() {
		return false;
	}

	@Override
	public boolean isAlgebraViewEditable() {
		return false;
	}

	/** Internal G8 results remain transient; public G9U0 results persist. */
	@Override
	public void getXML(boolean getListenersToo, XMLStringBuilder builder) {
		if (publicPersistence) {
			super.getXML(getListenersToo, builder);
		}
	}

	@Override
	protected void getXMLTags(XMLStringBuilder builder) {
		super.getXMLTags(builder);
		if (publicPersistence && isTokenLedgerReadyForEvaluation()) {
			builder.startTag("locusIntersectionTokenLedger")
					.attr("state", tokenLedger.exportState()).endTag();
		}
	}

	private void validatePublication(IntersectionSourceBinding2D binding,
			LocusIntersectionResult2D result) {
		if (binding != currentSourceBinding
				|| result.getSourceBinding() != binding) {
			throw new IllegalArgumentException(
					"Intersection publication does not match current revision");
		}
	}

	private static boolean hasExactDependencies(GeoIdentityRecord record,
			PersistentGeoId first, PersistentGeoId second) {
		return record.getDependencies().size() == 2
				&& record.getDependencies().contains(first)
				&& record.getDependencies().contains(second);
	}

	private static String sourcePair(PersistentGeoId source,
			PersistentGeoId target) {
		return framed(source.toExternalForm()) + framed(target.toExternalForm());
	}

	private static String framed(String value) {
		return value.length() + ":" + value;
	}
}
