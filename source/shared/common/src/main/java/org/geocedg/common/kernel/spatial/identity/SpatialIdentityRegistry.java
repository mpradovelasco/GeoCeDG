/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.geogebra.common.io.DocHandler;
import org.geogebra.common.io.QDParser;
import org.geogebra.common.io.XMLParseException;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;

/**
 * Construction-confined owner of all durable spatial IDs and inert records.
 * It does not listen for geometry changes and does not schedule evaluation.
 */
public final class SpatialIdentityRegistry {
	public static final int XML_VERSION = 1;
	private static final int DEFAULT_ALLOCATION_ATTEMPTS = 32;

	/** Explicit interpretation of one XML parse. */
	public enum LoadPurpose {
		NATIVE_OR_UNDO_RESTORE,
		CLIPBOARD_IMPORT,
		REDEFINE_REBUILD,
		ROLLBACK_RESTORE,
		GENERIC_MERGE
	}

	private final SpatialTokenSource tokenSource;
	private final int maximumAllocationAttempts;
	private final Construction owner;
	private final IdentityHashMap<GeoElement, PersistentGeoId> idsByGeo =
			new IdentityHashMap<>();
	private final Map<PersistentGeoId, GeoElement> geosById = new LinkedHashMap<>();
	private final Map<SpatialIdentityId, SpatialIdentityRecord> records =
			new LinkedHashMap<>();
	private final Map<String, SpatialIdentityId> issuedTokenIndex =
			new LinkedHashMap<>();
	private final Map<String, SpatialIdentityId> rawTokenIndex = new LinkedHashMap<>();
	private final Map<String, SpatialIdentityId> reservedTokenIndex = new LinkedHashMap<>();
	private final Map<String, SpatialIdentityId> retiredTokenIndex = new LinkedHashMap<>();
	private final Map<SpatialIdentityId, SpatialRecordResolution> resolutions =
			new LinkedHashMap<>();
	private final Map<String, SpatialRedefineProvider> redefineProviders =
			new LinkedHashMap<>();
	private final IdentityHashMap<GeoElement, PersistentGeoId> serializationOverlay =
			new IdentityHashMap<>();
	private final List<SpatialIdentityDiagnostic> diagnostics = new ArrayList<>();
	private final SpatialIdentityInstrumentation instrumentation =
			new SpatialIdentityInstrumentation();

	/** Creates a registry with the shared-Java token source and bounded retries. */
	public SpatialIdentityRegistry() {
		this(null, new DefaultSpatialTokenSource(), DEFAULT_ALLOCATION_ATTEMPTS);
	}

	/** Creates a construction-owned registry for productive host persistence. */
	public SpatialIdentityRegistry(Construction owner) {
		this(Objects.requireNonNull(owner), new DefaultSpatialTokenSource(),
				DEFAULT_ALLOCATION_ATTEMPTS);
	}

	/** Creates a registry with an injectable token source and retry bound. */
	public SpatialIdentityRegistry(SpatialTokenSource tokenSource,
			int maximumAllocationAttempts) {
		this(null, tokenSource, maximumAllocationAttempts);
	}

	/** Creates a construction-owned registry with an injectable test token source. */
	public SpatialIdentityRegistry(Construction owner, SpatialTokenSource tokenSource,
			int maximumAllocationAttempts) {
		this.owner = owner;
		this.tokenSource = Objects.requireNonNull(tokenSource);
		this.maximumAllocationAttempts = SpatialRecordSupport.requirePositive(
				maximumAllocationAttempts, "maximumAllocationAttempts");
	}

	/** @return the registry's deterministic functional counters */
	public SpatialIdentityInstrumentation getInstrumentation() {
		return instrumentation;
	}

	/** @return immutable accumulated structured diagnostics */
	public List<SpatialIdentityDiagnostic> getDiagnostics() {
		return Collections.unmodifiableList(new ArrayList<>(diagnostics));
	}

	/** @return the geo ID, or {@code null} when the geo is unassociated */
	public PersistentGeoId getPersistentGeoId(GeoElement geo) {
		return idsByGeo.get(geo);
	}

	/** @return the overlaid or currently published ID used for element XML */
	public PersistentGeoId getPersistentGeoIdForSerialization(GeoElement geo) {
		PersistentGeoId overlay = serializationOverlay.get(geo);
		return overlay == null ? idsByGeo.get(geo) : overlay;
	}

	/** @return the currently attached geo, or {@code null} */
	public GeoElement getGeo(PersistentGeoId id) {
		return geosById.get(id);
	}

	/** @return the inert record, or {@code null} */
	public SpatialIdentityRecord getRecord(SpatialIdentityId id) {
		return records.get(id);
	}

	/** @return the participating-geo record, or {@code null} */
	public GeoIdentityRecord getGeoRecord(PersistentGeoId id) {
		SpatialIdentityRecord record = records.get(id);
		return record instanceof GeoIdentityRecord ? (GeoIdentityRecord) record : null;
	}

	/** @return current persistence resolution evidence, or {@code null} */
	public SpatialRecordResolution getResolution(SpatialIdentityId id) {
		return resolutions.get(id);
	}

	/** @return whether this exact geo instance is currently attached */
	public boolean isParticipating(GeoElement geo) {
		return idsByGeo.containsKey(geo);
	}

	/** @return the number of published inert records */
	public int size() {
		return records.size();
	}

	/** @return whether the registry has no published records */
	public boolean isEmpty() {
		return records.isEmpty();
	}

	/** @return all records in deterministic kind-and-ID order */
	public List<SpatialIdentityRecord> getRecords() {
		return immutableSortedRecords(records.values());
	}

	/** @return active record counts grouped by globally distinct kind */
	public Map<SpatialIdentityKind, Integer> getRecordCountsByKind() {
		Map<SpatialIdentityKind, Integer> counts =
				new EnumMap<>(SpatialIdentityKind.class);
		for (SpatialIdentityId id : records.keySet()) {
			Integer current = counts.get(id.getKind());
			counts.put(id.getKind(), current == null ? 1 : current + 1);
		}
		return Collections.unmodifiableMap(counts);
	}

	/**
	 * Allocates and reserves one construction-unique typed ID.
	 *
	 * @return a fresh reserved ID of the requested kind
	 */
	public SpatialIdentityId allocate(SpatialIdentityKind kind) {
		Objects.requireNonNull(kind);
		for (int attempt = 0; attempt < maximumAllocationAttempts; attempt++) {
			instrumentation.recordAllocationAttempt();
			String token = tokenSource.nextToken();
			try {
				SpatialIdentityId.validateRawToken(token);
			} catch (IllegalArgumentException exception) {
				throw failure(SpatialIdentityDiagnostic.of(
						SpatialIdentityDiagnostic.Code.MALFORMED_ID,
						"Token source returned a malformed token"), exception);
			}
			if (issuedTokenIndex.containsKey(token)) {
				instrumentation.recordCollision(kind);
				continue;
			}
			SpatialIdentityId id = createId(kind, token);
			issuedTokenIndex.put(token, id);
			reservedTokenIndex.put(token, id);
			instrumentation.recordAllocation(kind);
			return id;
		}
		throw failure(SpatialIdentityDiagnostic.of(
				SpatialIdentityDiagnostic.Code.ALLOCATION_EXHAUSTED,
				"Spatial identity allocation exhausted its bounded collision retries"));
	}

	/** @return a fresh reserved participating-geo ID */
	public PersistentGeoId allocatePersistentGeoId() {
		return (PersistentGeoId) allocate(SpatialIdentityKind.GEO);
	}

	/** @return a fresh reserved spatial-object ID */
	public SpatialObjectId allocateSpatialObjectId() {
		return (SpatialObjectId) allocate(SpatialIdentityKind.SPATIAL_OBJECT);
	}

	/** @return a fresh reserved projection-frame ID */
	public ProjectionFrameId allocateProjectionFrameId() {
		return (ProjectionFrameId) allocate(SpatialIdentityKind.PROJECTION_FRAME);
	}

	/** @return a fresh reserved projection-system ID */
	public ProjectionSystemId allocateProjectionSystemId() {
		return (ProjectionSystemId) allocate(SpatialIdentityKind.PROJECTION_SYSTEM);
	}

	/** @return a fresh reserved diagram-map ID */
	public ProjectionDiagramMapId allocateProjectionDiagramMapId() {
		return (ProjectionDiagramMapId) allocate(
				SpatialIdentityKind.PROJECTION_DIAGRAM_MAP);
	}

	/** @return a fresh reserved frame-relation ID */
	public ProjectionFrameRelationId allocateProjectionFrameRelationId() {
		return (ProjectionFrameRelationId) allocate(
				SpatialIdentityKind.PROJECTION_FRAME_RELATION);
	}

	/** @return a fresh reserved projection-binding ID */
	public ProjectionBindingId allocateProjectionBindingId() {
		return (ProjectionBindingId) allocate(SpatialIdentityKind.PROJECTION_BINDING);
	}

	/** Atomically publishes one explicitly participating geo and inert record batch. */
	public void registerParticipation(GeoElement geo, GeoIdentityRecord geoRecord,
			Collection<? extends SpatialIdentityRecord> additionalRecords) {
		Objects.requireNonNull(geo);
		Objects.requireNonNull(geoRecord);
		Objects.requireNonNull(additionalRecords);
		IdentityHashMap<GeoElement, PersistentGeoId> attachments = new IdentityHashMap<>();
		attachments.put(geo, geoRecord.getId());
		ArrayList<SpatialIdentityRecord> batch = new ArrayList<>();
		batch.add(geoRecord);
		batch.addAll(additionalRecords);
		publishBatch(batch, attachments, false, false);
	}

	/** Atomically publishes one explicitly participating geo record. */
	public void registerParticipation(GeoElement geo, GeoIdentityRecord geoRecord) {
		registerParticipation(geo, geoRecord,
				Collections.<SpatialIdentityRecord>emptyList());
	}

	/** Atomically publishes non-geo records against the current registry. */
	public void registerRecords(Collection<? extends SpatialIdentityRecord> newRecords) {
		publishBatch(newRecords, new IdentityHashMap<GeoElement, PersistentGeoId>(),
				false, false);
	}

	/**
	 * Removes the participating geo and every transitively dependent inert record.
	 *
	 * @return immutable IDs retired by the transaction
	 */
	public Set<SpatialIdentityId> retireGeo(GeoElement geo) {
		PersistentGeoId id = idsByGeo.get(geo);
		if (id == null) {
			return Collections.emptySet();
		}
		LinkedHashSet<SpatialIdentityId> retired = dependentClosure(id);
		for (SpatialIdentityId retiredId : retired) {
			SpatialIdentityRecord removed = records.remove(retiredId);
			resolutions.remove(retiredId);
			rawTokenIndex.remove(retiredId.getRawToken());
			retiredTokenIndex.put(retiredId.getRawToken(), retiredId);
			if (removed instanceof GeoIdentityRecord) {
				GeoElement removedGeo = geosById.remove(retiredId);
				if (removedGeo != null) {
					idsByGeo.remove(removedGeo);
				}
			}
		}
		instrumentation.recordDeleteCommit();
		return Collections.unmodifiableSet(retired);
	}

	/** Clears live state while retaining construction-lifetime issued tokens. */
	public void clear() {
		clearLiveState();
	}

	/** Clears live state while retaining construction-lifetime retired tokens. */
	public void clearPreservingRetiredTokens() {
		clearLiveState();
	}

	private void clearLiveState() {
		for (SpatialIdentityId active : rawTokenIndex.values()) {
			retiredTokenIndex.put(active.getRawToken(), active);
		}
		for (SpatialIdentityId reserved : reservedTokenIndex.values()) {
			retiredTokenIndex.put(reserved.getRawToken(), reserved);
		}
		idsByGeo.clear();
		geosById.clear();
		records.clear();
		rawTokenIndex.clear();
		reservedTokenIndex.clear();
		resolutions.clear();
		serializationOverlay.clear();
		diagnostics.clear();
	}

	/**
	 * Adds every geo in the selected semantic connected component.
	 *
	 * @return immutable expanded geo selection
	 */
	public Set<GeoElement> expandSemanticClosure(Collection<GeoElement> selection) {
		LinkedHashSet<SpatialIdentityId> closure = closureIds(selection);
		LinkedHashSet<GeoElement> result = new LinkedHashSet<>();
		result.addAll(selection);
		for (SpatialIdentityId id : closure) {
			if (id instanceof PersistentGeoId) {
				GeoElement geo = geosById.get(id);
				if (geo != null) {
					result.add(geo);
				}
			}
		}
		return Collections.unmodifiableSet(result);
	}

	/** @return deterministic complete inert records connected to the supplied geos */
	public List<SpatialIdentityRecord> getClosureRecords(
			Collection<GeoElement> participatingGeos) {
		Set<SpatialIdentityId> closure = closureIds(participatingGeos);
		ArrayList<SpatialIdentityRecord> result = new ArrayList<>();
		for (SpatialIdentityId id : closure) {
			SpatialIdentityRecord record = records.get(id);
			if (record != null) {
				result.add(record);
			}
		}
		return immutableSortedRecords(result);
	}

	/** @return deterministic full-construction spatial section XML */
	public String writeSpatialSection() {
		return SpatialRecordXmlCodec.writeSection(getRecords());
	}

	/** @return deterministic semantic-closure spatial section XML */
	public String writeClosureSection(Collection<GeoElement> participatingGeos) {
		return SpatialRecordXmlCodec.writeSection(getClosureRecords(participatingGeos));
	}

	/** @return deterministic macro-template closure section XML */
	public String writeMacroTemplateClosure(Collection<GeoElement> templateGeos) {
		return writeClosureSection(templateGeos);
	}

	/** @return a version-one two-stage load session for the explicit purpose */
	public LoadSession beginLoadSession(LoadPurpose purpose) {
		return new LoadSession(purpose, XML_VERSION);
	}

	/** @return a two-stage load session that validates the declared section version */
	public LoadSession beginLoadSession(LoadPurpose purpose, int sectionVersion) {
		return new LoadSession(purpose, sectionVersion);
	}

	/**
	 * Parses and validates identity data in a clipboard fragment without creating
	 * geos, allocating destination IDs, or publishing registry state.
	 *
	 * @param clipboardXml construction-element XML fragment
	 * @return whether the parsed fragment carries a spatial section or geo attachment
	 * @throws SpatialIdentityException if an identity-bearing fragment is malformed
	 */
	public static boolean preflightClipboardFragment(String clipboardXml) {
		Objects.requireNonNull(clipboardXml);
		ClipboardPreflightHandler handler = new ClipboardPreflightHandler();
		try {
			new QDParser().parse(handler,
					new StringReader("<construction>" + clipboardXml
							+ "</construction>"));
		} catch (IOException exception) {
			throw preflightFailure(SpatialIdentityDiagnostic.Code.MALFORMED_RECORD,
					"Clipboard identity preflight could not read its in-memory XML",
					exception);
		} catch (XMLParseException exception) {
			if (!handler.isIdentityBearing()) {
				return false;
			}
			throw preflightFailure(SpatialIdentityDiagnostic.Code.MALFORMED_RECORD,
					"Malformed identity-bearing clipboard XML", exception);
		}
		if (!handler.isIdentityBearing()) {
			return false;
		}
		SpatialIdentityRegistry validator = new SpatialIdentityRegistry();
		validator.validateStagedImportData(handler.getRecords(),
				handler.getAttachmentIds());
		return true;
	}

	/** Generic XML merge is not an identity import authority. */
	public void rejectGenericMergeIfIdentityBearing(boolean identityBearing) {
		if (identityBearing) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.GENERIC_MERGE_FORBIDDEN,
					"Identity-bearing XML requires an explicit import plan"));
		}
	}

	/** @return a lexical candidate-to-ID serialization overlay */
	public SerializationOverlay beginSerializationOverlay(GeoElement candidate,
			PersistentGeoId decidedId) {
		Objects.requireNonNull(candidate);
		Objects.requireNonNull(decidedId);
		if (serializationOverlay.containsKey(candidate)) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"A serialization overlay is already active for this candidate"));
		}
		serializationOverlay.put(candidate, decidedId);
		return new SerializationOverlay(candidate, decidedId);
	}

	/** Registers the unique provider inspection boundary for one provider ID. */
	public void registerRedefineProvider(SpatialRedefineProvider provider) {
		Objects.requireNonNull(provider);
		String providerId = SpatialRecordSupport.requireText(provider.getProviderId(),
				"providerId");
		SpatialRedefineProvider existing = redefineProviders.get(providerId);
		if (existing != null && existing != provider) {
			throw new IllegalArgumentException("Redefine provider is already registered: "
					+ providerId);
		}
		redefineProviders.put(providerId, provider);
	}

	/** @return context from the explicit old target, or {@code null} if unassociated */
	public SpatialRedefineContext captureRedefineContext(GeoElement explicitOldTarget) {
		PersistentGeoId id = idsByGeo.get(explicitOldTarget);
		if (id == null) {
			return null;
		}
		GeoIdentityRecord record = getGeoRecord(id);
		if (record == null) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
					"Participating geo has no identity record", id));
		}
		return new SpatialRedefineContext(explicitOldTarget, id,
				record.toRedefineSignature(), hostOutputCount(explicitOldTarget),
				record.getDefinitionRevision(), record.getTopologyRevision(),
				explicitOldTarget.getConstruction().getCurrentUndoXML(false).toString());
	}

	/**
	 * Verifies that a previously captured context still names the exact current
	 * target and revision. This performs no allocation or publication.
	 *
	 * @param context explicit operation context
	 * @param explicitOldTarget target supplied by the host operation
	 */
	public void validateRedefineContext(SpatialRedefineContext context,
			GeoElement explicitOldTarget) {
		Objects.requireNonNull(context);
		if (context.getOldTarget() != explicitOldTarget) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
					"Explicit redefine context does not match the host target",
					context.getOldId()));
		}
		requireCurrentContext(context);
	}

	/** @return the provider-inspected decision frozen before host mutation */
	public SpatialRedefineTransaction prepareRedefine(SpatialRedefineContext context,
			SpatialRedefineProposal proposal) {
		Objects.requireNonNull(proposal);
		if (context == null) {
			instrumentation.recordRedefineMissingContext();
			instrumentation.recordRedefineDecision(SpatialRedefineDecision.REJECT);
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
					"A participating redefine cannot retain identity without context"));
		}
		requireCurrentContext(context);
		SpatialRedefineProvider provider = redefineProviders.get(
				context.getOldSignature().getProvider());
		if (provider == null) {
			instrumentation.recordRedefineDecision(SpatialRedefineDecision.REJECT);
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.REDEFINE_PROVIDER_MISSING,
					"No registered provider can inspect the redefine", context.getOldId()));
		}
		SpatialRedefineDecision decision = Objects.requireNonNull(
				provider.inspect(context, proposal));
		if (proposal.getTargetedOutputCount() != 1
				|| hostOutputCount(proposal.getCandidate()) != 1
				|| context.getOldHostOutputCount() != 1
				|| context.getOldSignature().getOutputCardinality() != 1
				|| proposal.getSignature().getOutputCardinality() != 1) {
			instrumentation.recordRedefineMultiOutputRejection();
			decision = SpatialRedefineDecision.REJECT;
		}
		if (decision == SpatialRedefineDecision.RETAIN
				&& !proposal.isTopologyPreserving()) {
			decision = SpatialRedefineDecision.REJECT;
		}
		if (decision == SpatialRedefineDecision.RETAIN
				&& !isRetainCompatible(context, proposal)) {
			decision = SpatialRedefineDecision.REJECT;
		}
		if (decision == SpatialRedefineDecision.FRESH
				&& !proposal.isReplacementOperationSelected()) {
			decision = SpatialRedefineDecision.REJECT;
		}
		PersistentGeoId freshId = decision == SpatialRedefineDecision.FRESH
				? allocatePersistentGeoId() : null;
		Set<SpatialIdentityId> retiredIds = decision == SpatialRedefineDecision.FRESH
				? dependentClosure(context.getOldId())
				: Collections.<SpatialIdentityId>emptySet();
		instrumentation.recordRedefineDecision(decision);
		return new SpatialRedefineTransaction(this, context, proposal, decision, freshId,
				retiredIds);
	}

	/**
	 * Validates that a caller-supplied transaction still belongs to this
	 * registry, remains prepared, and is current at the last pre-mutation seam.
	 *
	 * @param transaction transaction about to enter a host replacement branch
	 */
	public void validatePreparedRedefineTransaction(
			SpatialRedefineTransaction transaction) {
		requireOwnedPrepared(transaction);
		requireCurrentContext(transaction.getContext());
	}

	/** @return a transaction built from provider-inspected candidate metadata */
	public SpatialRedefineTransaction prepareRedefine(SpatialRedefineContext context,
			GeoElement candidate, int targetedOutputCount,
			boolean topologyPreserving) {
		return prepareRedefine(context, candidate, targetedOutputCount,
				topologyPreserving, false);
	}

	/** @return a transaction with explicit true-replacement intent evidence */
	public SpatialRedefineTransaction prepareRedefine(SpatialRedefineContext context,
			GeoElement candidate, int targetedOutputCount,
			boolean topologyPreserving, boolean replacementOperationSelected) {
		if (context == null) {
			instrumentation.recordRedefineMissingContext();
			instrumentation.recordRedefineDecision(SpatialRedefineDecision.REJECT);
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
					"A participating redefine cannot retain identity without context"));
		}
		requireCurrentContext(context);
		SpatialRedefineProvider provider = redefineProviders.get(
				context.getOldSignature().getProvider());
		if (provider == null) {
			instrumentation.recordRedefineDecision(SpatialRedefineDecision.REJECT);
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.REDEFINE_PROVIDER_MISSING,
					"No registered provider can describe the redefine candidate",
					context.getOldId()));
		}
		SpatialRedefineSignature signature = Objects.requireNonNull(
				provider.describeCandidate(context, candidate));
		boolean providerTopologyPreserving = provider.isTopologyPreserving(context,
				candidate);
		return prepareRedefine(context, new SpatialRedefineProposal(candidate, signature,
				targetedOutputCount,
				topologyPreserving && providerTopologyPreserving,
				replacementOperationSelected));
	}

	/** @return a lexical overlay using the transaction's decided identity */
	public SerializationOverlay beginRedefineSerializationOverlay(
			SpatialRedefineTransaction transaction) {
		requireOwnedPrepared(transaction);
		requireCurrentContext(transaction.getContext());
		if (transaction.getDecision() == SpatialRedefineDecision.REJECT) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.REDEFINE_REJECTED,
					"Rejected redefine has no serialization identity"));
		}
		return beginSerializationOverlay(transaction.getProposal().getCandidate(),
				transaction.getDecidedId());
	}

	/**
	 * Returns the record view for an XML rebuild without publishing the decision.
	 * A FRESH view removes the old dependent closure and adds only the proposed
	 * participating geo record; bindings are never transferred implicitly.
	 *
	 * @return deterministic transaction-specific section XML
	 */
	public String writeSpatialSectionForRedefine(
			SpatialRedefineTransaction transaction) {
		requireOwnedPrepared(transaction);
		requireCurrentContext(transaction.getContext());
		if (transaction.getDecision() == SpatialRedefineDecision.REJECT) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.REDEFINE_REJECTED,
					"Rejected redefine has no serialization view"));
		}
		if (transaction.getDecision() == SpatialRedefineDecision.RETAIN) {
			transaction.markRebuildViewWritten();
			return writeSpatialSection();
		}
		requireCurrentFreshClosure(transaction);
		Set<SpatialIdentityId> removed = transaction.getRetiredIds();
		ArrayList<SpatialIdentityRecord> view = new ArrayList<>();
		for (SpatialIdentityRecord record : records.values()) {
			if (!removed.contains(record.getId())) {
				view.add(record);
			}
		}
		SpatialRedefineSignature signature = transaction.getProposal().getSignature();
		view.add(new GeoIdentityRecord(transaction.getDecidedId(),
				signature.getProvider(), signature.getFamily(), signature.getSchemaId(),
				signature.getSchemaVersion(), signature.getAuthority(),
				signature.getBindingRole(), signature.getStableOutputRole(),
				signature.getOutputCardinality(), 0, 0));
		transaction.markRebuildViewWritten();
		return SpatialRecordXmlCodec.writeSection(view);
	}

	void commitRedefine(SpatialRedefineTransaction transaction, GeoElement actualResult) {
		requireOwnedPrepared(transaction);
		Objects.requireNonNull(actualResult);
		if (transaction.getDecision() == SpatialRedefineDecision.REJECT) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.REDEFINE_REJECTED,
					"Provider rejected redefine before host mutation",
					transaction.getContext().getOldId()));
		}
		SpatialRedefineContext context = transaction.getContext();
		PersistentGeoId decidedId = transaction.getDecidedId();
		PersistentGeoId existingActualId = idsByGeo.get(actualResult);
		boolean rebuiltResult = actualResult != context.getOldTarget()
				&& decidedId.equals(existingActualId)
				&& geosById.get(decidedId) == actualResult;
		boolean freshInPlaceTarget = transaction.getDecision()
				== SpatialRedefineDecision.FRESH
				&& actualResult == context.getOldTarget()
				&& context.getOldId().equals(existingActualId)
				&& geosById.get(context.getOldId()) == actualResult;
		if ((existingActualId != null && !decidedId.equals(existingActualId)
				&& !freshInPlaceTarget)
				|| (!rebuiltResult && actualResult != context.getOldTarget()
						&& actualResult != transaction.getProposal().getCandidate())) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"Redefine result is not the inspected or rebuilt target", decidedId));
		}
		validateAttachmentGeo(actualResult, decidedId);
		if (rebuiltResult) {
			if (!transaction.isRebuildViewWritten()) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"Rebuilt redefine has no validated serialization view",
						decidedId));
			}
		} else {
			requireCurrentContext(context);
		}
		GeoIdentityRecord oldRecord = getGeoRecord(context.getOldId());
		if (transaction.getDecision() == SpatialRedefineDecision.RETAIN) {
			GeoElement current = geosById.get(context.getOldId());
			if (oldRecord == null || (current != context.getOldTarget()
					&& current != actualResult)) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"Retained identity does not resolve to the old or rebuilt result",
						context.getOldId()));
			}
			if (oldRecord.getDefinitionRevision()
					!= context.getOldDefinitionRevision()
					|| oldRecord.getTopologyRevision()
							!= context.getOldTopologyRevision()) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
						"Retained identity revisions changed after inspection",
						context.getOldId()));
			}
			long nextDefinitionRevision;
			try {
				nextDefinitionRevision = Math.addExact(
						oldRecord.getDefinitionRevision(), 1);
			} catch (ArithmeticException exception) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"Definition revision cannot advance", context.getOldId()),
						exception);
			}
			GeoIdentityRecord updatedRecord = oldRecord.withRevisions(
					nextDefinitionRevision, oldRecord.getTopologyRevision());
			validateRecordShape(updatedRecord);
			idsByGeo.remove(context.getOldTarget());
			idsByGeo.put(actualResult, context.getOldId());
			geosById.put(context.getOldId(), actualResult);
			records.put(context.getOldId(), updatedRecord);
			instrumentation.recordDefinitionRevisionChange();
		} else {
			SpatialRedefineSignature signature = transaction.getProposal().getSignature();
			GeoIdentityRecord newRecord = new GeoIdentityRecord(
					transaction.getDecidedId(), signature.getProvider(),
					signature.getFamily(), signature.getSchemaId(),
					signature.getSchemaVersion(), signature.getAuthority(),
					signature.getBindingRole(), signature.getStableOutputRole(),
					signature.getOutputCardinality(), 0, 0);
			// A rebuild may already have restored the transaction-specific FRESH view.
			if (geosById.get(transaction.getDecidedId()) == actualResult) {
				for (SpatialIdentityId retiredId : transaction.getRetiredIds()) {
					retiredTokenIndex.put(retiredId.getRawToken(), retiredId);
				}
			} else {
				requireCurrentFreshClosure(transaction);
				if (oldRecord == null || geosById.get(context.getOldId())
						!= context.getOldTarget()) {
					throw failure(SpatialIdentityDiagnostic.forSubject(
							SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
							"Fresh redefine lost both old and rebuilt identity state",
							context.getOldId()));
				}
				// Validate every fallible attachment/record precondition before the
				// old closure is retired. Publication below is then a no-fail switch.
				validateRecordShape(newRecord);
				retireGeo(context.getOldTarget());
				registerParticipation(actualResult, newRecord);
			}
		}
		transaction.markCommitted();
		instrumentation.recordRedefineCommit();
	}

	private void requireCurrentFreshClosure(
			SpatialRedefineTransaction transaction) {
		Set<SpatialIdentityId> current = dependentClosure(
				transaction.getContext().getOldId());
		if (!current.equals(transaction.getRetiredIds())) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
					"Fresh redefine closure changed after inspection",
					transaction.getContext().getOldId()));
		}
	}

	void rollbackRedefine(SpatialRedefineTransaction transaction) {
		requireOwnedPrepared(transaction);
		if (transaction.getDecidedId() != null
				&& transaction.getDecision() == SpatialRedefineDecision.FRESH) {
			releaseReservation(transaction.getDecidedId());
		}
		transaction.markRolledBack();
		instrumentation.recordRedefineRollback();
	}

	/** @return a two-phase macro template-to-instance remap session */
	public MacroInstantiationSession beginMacroInstantiation(
			SpatialIdentityRegistry templateRegistry, boolean handledByFileImport) {
		return new MacroInstantiationSession(templateRegistry, handledByFileImport);
	}

	private boolean isRetainCompatible(SpatialRedefineContext context,
			SpatialRedefineProposal proposal) {
		return proposal.getTargetedOutputCount() == 1
				&& proposal.isTopologyPreserving()
				&& context.getOldSignature().getOutputCardinality() == 1
				&& context.getOldSignature().isExactlyCompatibleWith(
						proposal.getSignature());
	}

	private void requireOwnedPrepared(SpatialRedefineTransaction transaction) {
		if (transaction == null || !transaction.isOwnedBy(this)) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"Redefine transaction belongs to another registry"));
		}
		if (transaction.getState() != SpatialRedefineTransaction.State.PREPARED) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"Redefine transaction is no longer prepared"));
		}
	}

	private void requireCurrentContext(SpatialRedefineContext context) {
		GeoIdentityRecord currentRecord = getGeoRecord(context.getOldId());
		if (!context.getOldId().equals(idsByGeo.get(context.getOldTarget()))
				|| geosById.get(context.getOldId()) != context.getOldTarget()
				|| currentRecord == null
				|| !context.getOldSignature().isExactlyCompatibleWith(
						currentRecord.toRedefineSignature())
				|| context.getOldDefinitionRevision()
						!= currentRecord.getDefinitionRevision()
				|| context.getOldTopologyRevision()
						!= currentRecord.getTopologyRevision()
				|| hostOutputCount(context.getOldTarget())
						!= context.getOldHostOutputCount()) {
			instrumentation.recordRedefineMissingContext();
			instrumentation.recordRedefineDecision(SpatialRedefineDecision.REJECT);
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
					"Explicit redefine context is stale or no longer current",
					context.getOldId()));
		}
	}

	private static int hostOutputCount(GeoElement geo) {
		return geo.getParentAlgorithm() == null ? 1
				: geo.getParentAlgorithm().getOutputLength();
	}

	private void publishBatch(Collection<? extends SpatialIdentityRecord> newRecords,
			IdentityHashMap<GeoElement, PersistentGeoId> attachments,
			boolean requireCompleteClosure, boolean copied) {
		publishBatch(newRecords, attachments, requireCompleteClosure, copied, false);
	}

	private void publishBatch(Collection<? extends SpatialIdentityRecord> newRecords,
			IdentityHashMap<GeoElement, PersistentGeoId> attachments,
			boolean requireCompleteClosure, boolean copied,
			boolean allowExactIssuedRestore) {
		ArrayList<SpatialIdentityRecord> batch = new ArrayList<>();
		batch.addAll(newRecords);
		validateBatch(batch, attachments, requireCompleteClosure,
				allowExactIssuedRestore);
		Map<SpatialIdentityId, SpatialRecordResolution> batchResolutions =
				resolveBatch(batch);
		for (SpatialIdentityRecord record : batch) {
			records.put(record.getId(), record);
			issuedTokenIndex.put(record.getId().getRawToken(), record.getId());
			rawTokenIndex.put(record.getId().getRawToken(), record.getId());
			reservedTokenIndex.remove(record.getId().getRawToken());
			if (allowExactIssuedRestore) {
				retiredTokenIndex.remove(record.getId().getRawToken());
			}
			resolutions.put(record.getId(), batchResolutions.get(record.getId()));
		}
		for (Map.Entry<GeoElement, PersistentGeoId> attachment : attachments.entrySet()) {
			idsByGeo.put(attachment.getKey(), attachment.getValue());
			geosById.put(attachment.getValue(), attachment.getKey());
		}
		if (copied) {
			instrumentation.recordCopyCommit();
		}
	}

	private void validateBatch(List<SpatialIdentityRecord> batch,
			IdentityHashMap<GeoElement, PersistentGeoId> attachments,
			boolean requireCompleteClosure, boolean allowExactIssuedRestore) {
		Map<SpatialIdentityId, SpatialIdentityRecord> batchIds = new LinkedHashMap<>();
		Map<String, SpatialIdentityId> batchTokens = new LinkedHashMap<>();
		for (SpatialIdentityRecord record : batch) {
			Objects.requireNonNull(record);
			validateRecordShape(record);
			SpatialIdentityId id = record.getId();
			if (batchIds.put(id, record) != null || records.containsKey(id)) {
				instrumentation.recordCollision(id.getKind());
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.DUPLICATE_ID,
						"Duplicate spatial identity", id));
			}
			SpatialIdentityId sameToken = batchTokens.put(id.getRawToken(), id);
			SpatialIdentityId issuedToken = issuedTokenIndex.get(id.getRawToken());
			SpatialIdentityId currentToken = rawTokenIndex.get(id.getRawToken());
			SpatialIdentityId reservedToken = reservedTokenIndex.get(id.getRawToken());
			SpatialIdentityId retiredToken = retiredTokenIndex.get(id.getRawToken());
			if ((issuedToken != null && !issuedToken.equals(id))
					|| (retiredToken != null && !retiredToken.equals(id))) {
				instrumentation.recordCollision(id.getKind());
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.CROSS_KIND_TOKEN_REUSE,
						"Raw token was issued for another identity kind", id));
			}
			boolean reservedPublication = id.equals(reservedToken);
			if ((issuedToken != null || retiredToken != null) && !reservedPublication
					&& !allowExactIssuedRestore) {
				instrumentation.recordCollision(id.getKind());
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.RETIRED_ID_REUSE,
						"Issued identity tokens cannot be reused", id));
			}
			if ((sameToken != null && !sameToken.equals(id))
					|| (currentToken != null && !currentToken.equals(id))
					|| (reservedToken != null && !reservedToken.equals(id))) {
				instrumentation.recordCollision(id.getKind());
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.CROSS_KIND_TOKEN_REUSE,
						"Raw token is already used by another identity kind", id));
			}
		}
		for (Map.Entry<GeoElement, PersistentGeoId> attachment : attachments.entrySet()) {
			validateAttachmentGeo(attachment.getKey(), attachment.getValue());
			if (idsByGeo.containsKey(attachment.getKey())) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.GEO_ALREADY_PARTICIPATING,
						"Geo already has a durable identity", attachment.getValue()));
			}
			SpatialIdentityRecord record = batchIds.get(attachment.getValue());
			if (!(record instanceof GeoIdentityRecord)) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.GEO_ATTACHMENT_MISSING,
						"Geo attachment has no staged geo record", attachment.getValue()));
			}
		}
		for (SpatialIdentityRecord record : batch) {
			if (record instanceof GeoIdentityRecord
					&& !containsAttachedId(attachments, record.getId())
					&& !geosById.containsKey(record.getId())) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.GEO_ATTACHMENT_MISSING,
						"Geo record has no concrete geo attachment", record.getId()));
			}
			if (requireCompleteClosure) {
				for (SpatialIdentityId reference : record.getReferences()) {
					if (!batchIds.containsKey(reference)) {
						throw failure(SpatialIdentityDiagnostic.forReference(
								SpatialIdentityDiagnostic.Code.INCOMPLETE_CLOSURE,
								"Import closure omits a referenced identity",
								record.getId(), reference));
					}
				}
			}
		}
	}

	/** Validates a source closure without treating destination ID reuse as a collision. */
	private void validateStagedImport(List<SpatialIdentityRecord> batch,
			IdentityHashMap<GeoElement, PersistentGeoId> attachments) {
		validateStagedImportData(batch, attachments.values());
		Map<SpatialIdentityId, SpatialIdentityRecord> batchIds = new LinkedHashMap<>();
		for (SpatialIdentityRecord record : batch) {
			batchIds.put(record.getId(), record);
		}
		for (Map.Entry<GeoElement, PersistentGeoId> attachment : attachments.entrySet()) {
			validateAttachmentGeo(attachment.getKey(), attachment.getValue());
			if (idsByGeo.containsKey(attachment.getKey())) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.GEO_ALREADY_PARTICIPATING,
						"Import target geo is already participating", attachment.getValue()));
			}
			if (!(batchIds.get(attachment.getValue()) instanceof GeoIdentityRecord)) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.GEO_ATTACHMENT_MISSING,
						"Import attachment has no declared geo record",
						attachment.getValue()));
			}
		}
	}

	private void validateStagedImportData(List<SpatialIdentityRecord> batch,
			Collection<PersistentGeoId> attachmentIds) {
		Map<SpatialIdentityId, SpatialIdentityRecord> batchIds = new LinkedHashMap<>();
		Map<String, SpatialIdentityId> batchTokens = new LinkedHashMap<>();
		for (SpatialIdentityRecord record : batch) {
			Objects.requireNonNull(record);
			validateRecordShape(record);
			SpatialIdentityId id = record.getId();
			if (batchIds.put(id, record) != null) {
				instrumentation.recordCollision(id.getKind());
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.DUPLICATE_ID,
						"Import closure declares an ID more than once", id));
			}
			SpatialIdentityId priorKind = batchTokens.put(id.getRawToken(), id);
			if (priorKind != null && !priorKind.equals(id)) {
				instrumentation.recordCollision(id.getKind());
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.CROSS_KIND_TOKEN_REUSE,
						"Import closure reuses one raw token across kinds", id));
			}
		}
		LinkedHashSet<SpatialIdentityId> reachable = new LinkedHashSet<>();
		for (PersistentGeoId attachmentId : attachmentIds) {
			if (!reachable.add(attachmentId)) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.DUPLICATE_ID,
						"Import closure attaches one geo ID more than once", attachmentId));
			}
		}
		boolean changed;
		do {
			changed = false;
			for (SpatialIdentityRecord record : batch) {
				boolean connected = reachable.contains(record.getId());
				for (SpatialIdentityId reference : record.getReferences()) {
					connected |= reachable.contains(reference);
				}
				if (connected) {
					changed |= reachable.add(record.getId());
					changed |= reachable.addAll(record.getReferences());
				}
			}
		} while (changed);
		for (SpatialIdentityRecord record : batch) {
			if (!reachable.contains(record.getId())) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.INCOMPLETE_CLOSURE,
						"Import contains a record disconnected from every pasted geo",
						record.getId()));
			}
		}
		for (SpatialIdentityRecord record : batch) {
			for (SpatialIdentityId reference : record.getReferences()) {
				if (!batchIds.containsKey(reference)) {
					throw failure(SpatialIdentityDiagnostic.forReference(
							SpatialIdentityDiagnostic.Code.INCOMPLETE_CLOSURE,
							"Import closure omits a referenced identity",
							record.getId(), reference));
				}
			}
			if (record instanceof GeoIdentityRecord
					&& !reachableAttachment(attachmentIds, record.getId())) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.GEO_ATTACHMENT_MISSING,
						"Import geo record has no pasted geo attachment", record.getId()));
			}
		}
		for (PersistentGeoId attachmentId : attachmentIds) {
			if (!(batchIds.get(attachmentId) instanceof GeoIdentityRecord)) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.GEO_ATTACHMENT_MISSING,
						"Import attachment has no declared geo record",
						attachmentId));
			}
		}
	}

	private static boolean reachableAttachment(
			Collection<PersistentGeoId> attachmentIds, SpatialIdentityId id) {
		for (PersistentGeoId attachmentId : attachmentIds) {
			if (id.equals(attachmentId)) {
				return true;
			}
		}
		return false;
	}

	private void validateRecordShape(SpatialIdentityRecord record) {
		SpatialIdentityKind kind = record.getId().getKind();
		boolean kindMatches = kind == SpatialIdentityKind.GEO
				&& record instanceof GeoIdentityRecord
				|| kind == SpatialIdentityKind.SPATIAL_OBJECT
				&& record instanceof SpatialObjectRecord
				|| kind == SpatialIdentityKind.PROJECTION_FRAME
				&& record instanceof ProjectionFrameRecord
				|| kind == SpatialIdentityKind.PROJECTION_SYSTEM
				&& record instanceof ProjectionSystemRecord
				|| kind == SpatialIdentityKind.PROJECTION_DIAGRAM_MAP
				&& record instanceof ProjectionDiagramMapRecord
				|| kind == SpatialIdentityKind.PROJECTION_FRAME_RELATION
				&& record instanceof ProjectionFrameRelationRecord
				|| kind == SpatialIdentityKind.PROJECTION_BINDING
				&& record instanceof ProjectionBindingRecord;
		if (!kindMatches) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.RECORD_KIND_MISMATCH,
					"Record class does not match its globally typed identity",
					record.getId()));
		}
		if (record.getSemanticVersion() != XML_VERSION) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.UNSUPPORTED_VERSION,
					"Unsupported record semantic version: "
							+ record.getSemanticVersion(), record.getId()));
		}
	}

	private void validateAttachmentGeo(GeoElement geo, PersistentGeoId id) {
		if (owner != null && geo.getConstruction() != owner) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.GEO_ATTACHMENT_MISSING,
					"Participating geo belongs to another construction", id));
		}
		if (!geo.isLabelSet() || geo.isGeoCasCell()
				|| geo.getCorrespondingCasCell() != null
				|| (owner != null && (owner.isConstantElement(geo)
						|| !owner.isInConstructionList(geo)))) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.GEO_NOT_SERIALIZABLE,
					"Participating geo has no stable ordinary-element attachment", id));
		}
	}

	private Map<SpatialIdentityId, SpatialRecordResolution> resolveBatch(
			List<SpatialIdentityRecord> batch) {
		Map<SpatialIdentityId, SpatialIdentityRecord> available = new LinkedHashMap<>(records);
		for (SpatialIdentityRecord record : batch) {
			available.put(record.getId(), record);
		}
		Map<SpatialIdentityId, SpatialRecordResolution> result = new LinkedHashMap<>();
		for (SpatialIdentityRecord record : batch) {
			ArrayList<SpatialIdentityDiagnostic> missing = new ArrayList<>();
			for (SpatialIdentityId reference : record.getReferences()) {
				if (!available.containsKey(reference)) {
					SpatialIdentityDiagnostic diagnostic =
							SpatialIdentityDiagnostic.forReference(
									SpatialIdentityDiagnostic.Code.MISSING_REFERENCE,
									"Record retains an unresolved typed reference",
									record.getId(), reference);
					missing.add(diagnostic);
					diagnostics.add(diagnostic);
					instrumentation.recordUnresolvedReference(record.getId().getKind());
				}
			}
			result.put(record.getId(), missing.isEmpty()
					? SpatialRecordResolution.active()
					: new SpatialRecordResolution(SpatialResolutionState.BROKEN, missing));
		}
		return result;
	}

	private static boolean containsAttachedId(
			IdentityHashMap<GeoElement, PersistentGeoId> attachments,
			SpatialIdentityId id) {
		for (PersistentGeoId attachedId : attachments.values()) {
			if (id.equals(attachedId)) {
				return true;
			}
		}
		return false;
	}

	private LinkedHashSet<SpatialIdentityId> closureIds(Collection<GeoElement> selection) {
		LinkedHashSet<SpatialIdentityId> closure = new LinkedHashSet<>();
		for (GeoElement geo : selection) {
			PersistentGeoId id = idsByGeo.get(geo);
			if (id != null) {
				closure.add(id);
			}
		}
		boolean changed;
		do {
			changed = false;
			for (SpatialIdentityRecord record : records.values()) {
				boolean connected = closure.contains(record.getId());
				for (SpatialIdentityId reference : record.getReferences()) {
					connected |= closure.contains(reference);
				}
				if (connected) {
					changed |= closure.add(record.getId());
					changed |= closure.addAll(record.getReferences());
				}
			}
		} while (changed);
		return closure;
	}

	private LinkedHashSet<SpatialIdentityId> dependentClosure(SpatialIdentityId seed) {
		LinkedHashSet<SpatialIdentityId> result = new LinkedHashSet<>();
		result.add(seed);
		boolean changed;
		do {
			changed = false;
			for (SpatialIdentityRecord record : records.values()) {
				for (SpatialIdentityId reference : record.getReferences()) {
					if (result.contains(reference)) {
						changed |= result.add(record.getId());
					}
				}
			}
		} while (changed);
		return result;
	}

	private Map<SpatialIdentityId, SpatialIdentityId> allocateRemap(
			Collection<? extends SpatialIdentityRecord> source) {
		Map<SpatialIdentityId, SpatialIdentityId> remap = new LinkedHashMap<>();
		try {
			for (SpatialIdentityRecord record : immutableSortedRecords(source)) {
				remap.put(record.getId(), allocate(record.getId().getKind()));
			}
			return remap;
		} catch (RuntimeException exception) {
			for (SpatialIdentityId allocated : remap.values()) {
				releaseReservation(allocated);
			}
			throw exception;
		}
	}

	private void releaseReservation(SpatialIdentityId id) {
		if (id != null && id.equals(reservedTokenIndex.get(id.getRawToken()))) {
			reservedTokenIndex.remove(id.getRawToken());
			retiredTokenIndex.put(id.getRawToken(), id);
		}
	}

	private static SpatialIdentityId createId(SpatialIdentityKind kind, String token) {
		switch (kind) {
		case GEO:
			return new PersistentGeoId(token);
		case SPATIAL_OBJECT:
			return new SpatialObjectId(token);
		case PROJECTION_FRAME:
			return new ProjectionFrameId(token);
		case PROJECTION_SYSTEM:
			return new ProjectionSystemId(token);
		case PROJECTION_DIAGRAM_MAP:
			return new ProjectionDiagramMapId(token);
		case PROJECTION_FRAME_RELATION:
			return new ProjectionFrameRelationId(token);
		case PROJECTION_BINDING:
			return new ProjectionBindingId(token);
		default:
			throw new IllegalArgumentException("Unsupported identity kind: " + kind);
		}
	}

	private static List<SpatialIdentityRecord> immutableSortedRecords(
			Collection<? extends SpatialIdentityRecord> source) {
		ArrayList<SpatialIdentityRecord> result = new ArrayList<>();
		result.addAll(source);
		Collections.sort(result, new Comparator<SpatialIdentityRecord>() {
			/** {@inheritDoc} */
			@Override
			public int compare(SpatialIdentityRecord first, SpatialIdentityRecord second) {
				return first.getId().compareTo(second.getId());
			}
		});
		return Collections.unmodifiableList(result);
	}

	private SpatialIdentityException failure(SpatialIdentityDiagnostic diagnostic) {
		diagnostics.add(diagnostic);
		return new SpatialIdentityException(diagnostic);
	}

	private SpatialIdentityException failure(SpatialIdentityDiagnostic diagnostic,
			Throwable cause) {
		diagnostics.add(diagnostic);
		return new SpatialIdentityException(diagnostic, cause);
	}

	private static SpatialIdentityException preflightFailure(
			SpatialIdentityDiagnostic.Code code, String message) {
		return new SpatialIdentityException(SpatialIdentityDiagnostic.of(code, message));
	}

	private static SpatialIdentityException preflightFailure(
			SpatialIdentityDiagnostic.Code code, String message, Throwable cause) {
		return new SpatialIdentityException(SpatialIdentityDiagnostic.of(code, message),
				cause);
	}

	private static final class ClipboardPreflightHandler implements DocHandler {
		private final ArrayList<String> elements = new ArrayList<>();
		private final ArrayList<SpatialIdentityRecord> records = new ArrayList<>();
		private final ArrayList<PersistentGeoId> attachmentIds = new ArrayList<>();
		private boolean identityBearing;
		private boolean sectionSeen;
		private int sectionDepth = -1;

		@Override
		public void startElement(String tag, Map<String, String> attributes)
				throws XMLParseException {
			String parent = elements.isEmpty() ? null
					: elements.get(elements.size() - 1);
			elements.add(tag);
			if (attributes.containsKey("geocedgId")) {
				identityBearing = true;
				if (!"element".equals(tag) || attributes.get("label") == null
						|| attributes.get("label").trim().isEmpty()) {
					throw preflightFailure(
							SpatialIdentityDiagnostic.Code.GEO_NOT_SERIALIZABLE,
							"Clipboard geocedgId must attach to a labeled element");
				}
				try {
					attachmentIds.add(PersistentGeoId.parse(
							attributes.get("geocedgId")));
				} catch (IllegalArgumentException exception) {
					throw preflightFailure(SpatialIdentityDiagnostic.Code.MALFORMED_ID,
							"Malformed clipboard geocedgId attachment", exception);
				}
			}
			if ("geocedgSpatial".equals(tag)) {
				identityBearing = true;
				if (sectionSeen || !"construction".equals(parent)
						|| elements.size() != 2) {
					throw preflightFailure(
							SpatialIdentityDiagnostic.Code.MALFORMED_RECORD,
							"Clipboard must contain one flat geocedgSpatial section");
				}
				sectionSeen = true;
				if (attributes.size() != 1 || !attributes.containsKey("version")) {
					throw preflightFailure(
							SpatialIdentityDiagnostic.Code.MALFORMED_RECORD,
							"Clipboard geocedgSpatial requires only a version");
				}
				int version;
				try {
					version = Integer.parseInt(attributes.get("version"));
				} catch (NumberFormatException exception) {
					throw preflightFailure(
							SpatialIdentityDiagnostic.Code.MALFORMED_RECORD,
							"Malformed clipboard geocedgSpatial version", exception);
				}
				if (version != XML_VERSION) {
					throw preflightFailure(
							SpatialIdentityDiagnostic.Code.UNSUPPORTED_VERSION,
							"Unsupported clipboard geocedgSpatial version: " + version);
				}
				sectionDepth = elements.size();
				return;
			}
			if (sectionDepth >= 0) {
				if (elements.size() != sectionDepth + 1) {
					throw preflightFailure(
							SpatialIdentityDiagnostic.Code.MALFORMED_RECORD,
							"Clipboard geocedgSpatial records must be flat");
				}
				try {
					records.add(SpatialRecordXmlCodec.parseRecord(tag, attributes));
				} catch (IllegalArgumentException exception) {
					String message = exception.getMessage();
					SpatialIdentityDiagnostic.Code code = message != null
							&& (message.contains("identity")
									|| message.contains("token")
									|| message.contains("Expected "))
							? SpatialIdentityDiagnostic.Code.MALFORMED_ID
							: SpatialIdentityDiagnostic.Code.MALFORMED_RECORD;
					throw preflightFailure(code,
							"Malformed clipboard " + tag + " persistence record",
							exception);
				}
			}
		}

		@Override
		public void endElement(String tag) throws XMLParseException {
			if (elements.isEmpty()
					|| !tag.equals(elements.get(elements.size() - 1))) {
				throw new XMLParseException("Mismatched clipboard XML element: " + tag);
			}
			if ("geocedgSpatial".equals(tag)) {
				sectionDepth = -1;
			}
			elements.remove(elements.size() - 1);
		}

		@Override
		public void startDocument() {
			// Fresh handler state is already empty.
		}

		@Override
		public void endDocument() throws XMLParseException {
			if (!elements.isEmpty()) {
				throw new XMLParseException("Unclosed clipboard XML element");
			}
		}

		@Override
		public void text(String text) {
			if (sectionDepth >= 0 && !text.trim().isEmpty()) {
				throw preflightFailure(SpatialIdentityDiagnostic.Code.MALFORMED_RECORD,
						"Clipboard geocedgSpatial cannot contain text");
			}
		}

		private boolean isIdentityBearing() {
			return identityBearing;
		}

		private List<SpatialIdentityRecord> getRecords() {
			return records;
		}

		private List<PersistentGeoId> getAttachmentIds() {
			return attachmentIds;
		}
	}

	/** Disposable two-stage XML load transaction. */
	public final class LoadSession {
		private final LoadPurpose purpose;
		private final int sectionVersion;
		private final ArrayList<SpatialIdentityRecord> stagedRecords = new ArrayList<>();
		private final IdentityHashMap<GeoElement, PersistentGeoId> attachments =
				new IdentityHashMap<>();
		private SpatialIdentityException stagingFailure;
		private boolean finished;

		private LoadSession(LoadPurpose purpose, int sectionVersion) {
			this.purpose = Objects.requireNonNull(purpose);
			this.sectionVersion = sectionVersion;
		}

		/** @return the explicit lifecycle interpretation for this parse */
		public LoadPurpose getPurpose() {
			return purpose;
		}

		/** Stages one already parsed inert record without publishing it. */
		public void stageRecord(SpatialIdentityRecord record) {
			requireOpen();
			stagedRecords.add(Objects.requireNonNull(record));
		}

		/** Parses and stages one version-one flat record. */
		public void stageRecord(String xmlElementName, Map<String, String> attributes) {
			requireOpen();
			if (sectionVersion != XML_VERSION) {
				stagingFailure = failure(SpatialIdentityDiagnostic.of(
						SpatialIdentityDiagnostic.Code.UNSUPPORTED_VERSION,
						"Unsupported geocedgSpatial version: " + sectionVersion));
				throw stagingFailure;
			}
			try {
				stageRecord(SpatialRecordXmlCodec.parseRecord(xmlElementName, attributes));
			} catch (IllegalArgumentException exception) {
				SpatialIdentityDiagnostic.Code code = isIdentityParseFailure(exception)
						? SpatialIdentityDiagnostic.Code.MALFORMED_ID
						: SpatialIdentityDiagnostic.Code.MALFORMED_RECORD;
				stagingFailure = failure(SpatialIdentityDiagnostic.of(code,
						"Malformed " + xmlElementName + " persistence record"), exception);
				throw stagingFailure;
			}
		}

		/** Parses and stages an element-to-geo-ID attachment. */
		public void stageElementAttachment(GeoElement geo, String externalId) {
			requireOpen();
			try {
				stageElementAttachment(geo, PersistentGeoId.parse(externalId));
			} catch (IllegalArgumentException exception) {
				stagingFailure = failure(SpatialIdentityDiagnostic.of(
						SpatialIdentityDiagnostic.Code.MALFORMED_ID,
						"Malformed geocedgId element attachment"), exception);
				throw stagingFailure;
			}
		}

		/** Alias used by host XML integration for a string attachment. */
		public void stageGeoAttachment(GeoElement geo, String externalId) {
			stageElementAttachment(geo, externalId);
		}

		/** Stages an already parsed element-to-geo-ID attachment. */
		public void stageElementAttachment(GeoElement geo, PersistentGeoId id) {
			requireOpen();
			Objects.requireNonNull(geo);
			Objects.requireNonNull(id);
			if (attachments.containsKey(geo) || containsAttachedId(attachments, id)) {
				stagingFailure = failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.DUPLICATE_ID,
						"Duplicate staged geo attachment", id));
				throw stagingFailure;
			}
			attachments.put(geo, id);
		}

		/** Alias used by host XML integration for a typed attachment. */
		public void stageGeoAttachment(GeoElement geo, PersistentGeoId id) {
			stageElementAttachment(geo, id);
		}

		/** @return immutable old-to-fresh IDs for import, otherwise an empty map */
		public Map<SpatialIdentityId, SpatialIdentityId> commit() {
			requireOpen();
			finished = true;
			if (stagingFailure != null) {
				throw stagingFailure;
			}
			if (sectionVersion != XML_VERSION) {
				throw failure(SpatialIdentityDiagnostic.of(
						SpatialIdentityDiagnostic.Code.UNSUPPORTED_VERSION,
						"Invalid geocedgSpatial version: " + sectionVersion));
			}
			if (purpose == LoadPurpose.GENERIC_MERGE) {
				throw failure(SpatialIdentityDiagnostic.of(
						SpatialIdentityDiagnostic.Code.GENERIC_MERGE_FORBIDDEN,
						"Generic merge cannot activate spatial identities"));
			}
			if (stagedRecords.isEmpty() && attachments.isEmpty()) {
				return Collections.emptyMap();
			}
			if (purpose == LoadPurpose.CLIPBOARD_IMPORT) {
				Map<SpatialIdentityId, SpatialIdentityId> remap =
						Collections.emptyMap();
				try {
					validateStagedImport(stagedRecords, attachments);
					remap = allocateRemap(stagedRecords);
					ArrayList<SpatialIdentityRecord> remappedRecords = new ArrayList<>();
					for (SpatialIdentityRecord record : stagedRecords) {
						remappedRecords.add(record.remap(remap, true));
					}
					IdentityHashMap<GeoElement, PersistentGeoId> remappedAttachments =
							new IdentityHashMap<>();
					for (Map.Entry<GeoElement, PersistentGeoId> entry
							: attachments.entrySet()) {
						remappedAttachments.put(entry.getKey(),
								(PersistentGeoId) remap.get(entry.getValue()));
					}
					publishBatch(remappedRecords, remappedAttachments, true, true);
					for (SpatialIdentityRecord record : remappedRecords) {
						instrumentation.recordRemap(record.getId().getKind());
					}
					return Collections.unmodifiableMap(remap);
				} catch (RuntimeException exception) {
					for (SpatialIdentityId allocated : remap.values()) {
						releaseReservation(allocated);
					}
					instrumentation.recordCopyRollback();
					throw exception;
				}
			}
			publishBatch(stagedRecords, attachments, false, false, true);
			for (SpatialIdentityRecord record : stagedRecords) {
				instrumentation.recordRestore(record.getId().getKind());
			}
			return Collections.emptyMap();
		}

		/** Abandons all staged state without publication. */
		public void abort() {
			if (!finished && purpose == LoadPurpose.CLIPBOARD_IMPORT) {
				instrumentation.recordCopyRollback();
			}
			finished = true;
			stagedRecords.clear();
			attachments.clear();
		}

		private void requireOpen() {
			if (finished) {
				throw failure(SpatialIdentityDiagnostic.of(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"Load session is already finished"));
			}
		}

		private boolean isIdentityParseFailure(IllegalArgumentException exception) {
			String message = exception.getMessage();
			return message != null && (message.contains("identity")
					|| message.contains("token") || message.contains("Expected "));
		}
	}

	/** Lexically scoped candidate-to-ID XML overlay. */
	public final class SerializationOverlay implements AutoCloseable {
		private final GeoElement candidate;
		private final PersistentGeoId id;
		private boolean closed;

		private SerializationOverlay(GeoElement candidate, PersistentGeoId id) {
			this.candidate = candidate;
			this.id = id;
		}

		/** Ends this overlay without changing published registry state. */
		@Override
		public void close() {
			if (!closed) {
				if (id.equals(serializationOverlay.get(candidate))) {
					serializationOverlay.remove(candidate);
				}
				closed = true;
			}
		}
	}

	/** Two-phase macro template-to-instance remap transaction. */
	public final class MacroInstantiationSession {
		private final SpatialIdentityRegistry templateRegistry;
		private final boolean handledByFileImport;
		private final IdentityHashMap<GeoElement, GeoElement> instanceByTemplate =
				new IdentityHashMap<>();
		private final Set<GeoElement> createdTemplateGeos =
				Collections.newSetFromMap(new IdentityHashMap<GeoElement, Boolean>());
		private boolean finished;

		private MacroInstantiationSession(SpatialIdentityRegistry templateRegistry,
				boolean handledByFileImport) {
			this.templateRegistry = Objects.requireNonNull(templateRegistry);
			this.handledByFileImport = handledByFileImport;
		}

		/**
		 * Adds one authoritative template-to-instance geo mapping.
		 * Caller-owned inputs are mapped for closure validation only; they never
		 * receive a template-derived identity.
		 *
		 * @param templateGeo geo in the macro template construction
		 * @param instanceGeo corresponding geo in the caller construction
		 * @param createdByMacro whether the instance is created by this invocation
		 */
		public void map(GeoElement templateGeo, GeoElement instanceGeo,
				boolean createdByMacro) {
			requireOpen();
			Objects.requireNonNull(templateGeo);
			Objects.requireNonNull(instanceGeo);
			GeoElement existing = instanceByTemplate.put(templateGeo, instanceGeo);
			if (existing != null && existing != instanceGeo) {
				throw failure(SpatialIdentityDiagnostic.of(
						SpatialIdentityDiagnostic.Code.MACRO_MAP_INCOMPLETE,
						"Macro template geo has conflicting instance mappings"));
			}
			if (createdByMacro) {
				createdTemplateGeos.add(templateGeo);
			} else {
				createdTemplateGeos.remove(templateGeo);
			}
		}

		/** @return immutable template-to-fresh identity remap */
		public Map<SpatialIdentityId, SpatialIdentityId> commit() {
			requireOpen();
			finished = true;
			if (handledByFileImport || createdTemplateGeos.isEmpty()) {
				return Collections.emptyMap();
			}
			Set<GeoElement> expanded = templateRegistry.expandSemanticClosure(
					createdTemplateGeos);
			for (GeoElement templateGeo : expanded) {
				if (templateRegistry.isParticipating(templateGeo)) {
					if (!instanceByTemplate.containsKey(templateGeo)) {
						throw failure(SpatialIdentityDiagnostic.of(
								SpatialIdentityDiagnostic.Code.MACRO_MAP_INCOMPLETE,
								"Macro instance map omits a participating closure geo"));
					}
					if (!createdTemplateGeos.contains(templateGeo)) {
						throw failure(SpatialIdentityDiagnostic.of(
								SpatialIdentityDiagnostic.Code.MACRO_MAP_INCOMPLETE,
								"Macro closure requires a caller-owned input identity"));
					}
				}
			}
			List<SpatialIdentityRecord> templateRecords =
					templateRegistry.getClosureRecords(expanded);
			Map<SpatialIdentityId, SpatialIdentityId> remap =
					allocateRemap(templateRecords);
			ArrayList<SpatialIdentityRecord> remappedRecords = new ArrayList<>();
			IdentityHashMap<GeoElement, PersistentGeoId> remappedAttachments =
					new IdentityHashMap<>();
			for (SpatialIdentityRecord record : templateRecords) {
				remappedRecords.add(record.remap(remap, true));
			}
			for (Map.Entry<GeoElement, GeoElement> entry : instanceByTemplate.entrySet()) {
				if (!createdTemplateGeos.contains(entry.getKey())) {
					continue;
				}
				PersistentGeoId templateId = templateRegistry.getPersistentGeoId(
						entry.getKey());
				if (templateId != null) {
					remappedAttachments.put(entry.getValue(),
							(PersistentGeoId) remap.get(templateId));
				}
			}
			try {
				publishBatch(remappedRecords, remappedAttachments, true, true);
			} catch (RuntimeException exception) {
				for (SpatialIdentityId allocated : remap.values()) {
					releaseReservation(allocated);
				}
				throw exception;
			}
			for (SpatialIdentityRecord record : remappedRecords) {
				instrumentation.recordRemap(record.getId().getKind());
			}
			return Collections.unmodifiableMap(remap);
		}

		/** Abandons the mapping session without publication. */
		public void abort() {
			finished = true;
			instanceByTemplate.clear();
			createdTemplateGeos.clear();
		}

		private void requireOpen() {
			if (finished) {
				throw failure(SpatialIdentityDiagnostic.of(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"Macro instantiation session is already finished"));
			}
		}
	}
}
