/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.geogebra.common.kernel.algos.ConstructionElement;
import org.geogebra.common.kernel.geos.GeoElement;

/**
 * Construction-confined owner of all durable spatial IDs and inert records.
 * It does not listen for geometry changes and does not schedule evaluation.
 */
public final class SpatialIdentityRegistry implements SpatialIdentityGraph {
	public static final int XML_VERSION = 1;
	private static final int DEFAULT_ALLOCATION_ATTEMPTS = 32;
	private static final SpatialLifecycleRuntime NO_OP_LIFECYCLE_RUNTIME =
			new SpatialLifecycleRuntime() {
				@Override
				public PreparedSwitch prepare(
						SpatialLifecycleProspectiveGraph graph) {
					return new PreparedSwitch() {
						@Override
						public void commit() {
							// Ownerless registry tests have no semantic runtime.
						}

						@Override
						public void rollback() {
							// No prepared runtime state exists.
						}
					};
				}
			};

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
	private SpatialLifecycleRuntime lifecycleRuntime;
	private long mutationEpoch;
	private long graphPublicationEpoch;
	private RedefinePublicationLease activeRedefinePublicationLease;
	private final Set<SpatialRedefineContext> pendingRedefineCompletions =
			Collections.newSetFromMap(
					new IdentityHashMap<SpatialRedefineContext, Boolean>());
	private boolean graphSwitchInProgress;
	private int redefineGraphPublicationPermitDepth;
	private RedefineRebuildToken activeRedefineRebuildToken;
	private int redefineExternalCallbackDepth;
	private SpatialRedefineCandidateParticipation activeCandidateParticipation;
	private final Set<SpatialRedefineCandidateParticipation>
			claimedCandidateParticipations = Collections.newSetFromMap(
					new IdentityHashMap<SpatialRedefineCandidateParticipation, Boolean>());

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

	/**
	 * Registers the construction's only atomic semantic-runtime participant.
	 * Ownerless registries may omit this adapter and use the no-op unit-test path.
	 */
	public void registerLifecycleRuntime(SpatialLifecycleRuntime runtime) {
		Objects.requireNonNull(runtime);
		if (lifecycleRuntime != null) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"A lifecycle runtime is already registered"));
		}
		lifecycleRuntime = runtime;
	}

	/** @return immutable accumulated structured diagnostics */
	public List<SpatialIdentityDiagnostic> getDiagnostics() {
		return Collections.unmodifiableList(new ArrayList<>(diagnostics));
	}

	/** @return the geo ID, or {@code null} when the geo is unassociated */
	@Override
	public PersistentGeoId getPersistentGeoId(GeoElement geo) {
		return idsByGeo.get(geo);
	}

	/** @return the overlaid or currently published ID used for element XML */
	public PersistentGeoId getPersistentGeoIdForSerialization(GeoElement geo) {
		PersistentGeoId overlay = serializationOverlay.get(geo);
		return overlay == null ? idsByGeo.get(geo) : overlay;
	}

	/** @return the currently attached geo, or {@code null} */
	@Override
	public GeoElement getGeo(PersistentGeoId id) {
		return geosById.get(id);
	}

	/** @return the inert record, or {@code null} */
	@Override
	public SpatialIdentityRecord getRecord(SpatialIdentityId id) {
		return records.get(id);
	}

	/** @return the participating-geo record, or {@code null} */
	public GeoIdentityRecord getGeoRecord(PersistentGeoId id) {
		SpatialIdentityRecord record = records.get(id);
		return record instanceof GeoIdentityRecord ? (GeoIdentityRecord) record : null;
	}

	/** @return current persistence resolution evidence, or {@code null} */
	@Override
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
	@Override
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

	/** @return fresh IDs still reserved by an uncommitted typed operation */
	public int getReservedIdentityCount() {
		return reservedTokenIndex.size();
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
			mutationEpoch++;
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

	/**
	 * Atomically abandons only still-reserved construction-geo identities.
	 * Active, retired, foreign-kind or otherwise non-reserved IDs reject the
	 * complete request before any reservation is changed.
	 *
	 * @param ids reservations owned by one failed construction preflight
	 */
	public void abandonReservedConstructionIdentities(
			Collection<PersistentGeoId> ids) {
		Objects.requireNonNull(ids);
		LinkedHashSet<PersistentGeoId> checked = new LinkedHashSet<>();
		for (PersistentGeoId id : ids) {
			PersistentGeoId current = Objects.requireNonNull(id);
			if (!checked.add(current)
					|| records.containsKey(current)
					|| geosById.containsKey(current)
					|| !current.equals(
							reservedTokenIndex.get(current.getRawToken()))) {
				throw new IllegalArgumentException(
						"Construction identity is not an unused reservation: "
								+ current);
			}
		}
		for (PersistentGeoId id : checked) {
			releaseReservation(id);
		}
	}

	/**
	 * Opens the lexical pre-provider staging boundary used only while an explicit
	 * participating redefine candidate is parsed.
	 *
	 * @param context authority captured before candidate parsing
	 * @return opaque candidate participation scope
	 */
	public SpatialRedefineCandidateParticipation
			beginRedefineCandidateParticipation(SpatialRedefineContext context) {
		Objects.requireNonNull(context);
		if (owner == null || context.getOldTarget().getConstruction() != owner
				|| activeCandidateParticipation != null) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"A redefine candidate participation scope is already active or "
							+ "belongs to another construction",
					context.getOldId()));
		}
		requireCurrentContext(context);
		validateRedefineHostRollback(context);
		activeCandidateParticipation =
				new SpatialRedefineCandidateParticipation(this, context);
		return activeCandidateParticipation;
	}

	/** @return whether the construction is inside the explicit candidate scope */
	public boolean isRedefineCandidateParticipationActive() {
		boolean active = activeCandidateParticipation != null
				&& activeCandidateParticipation.getState()
						== SpatialRedefineCandidateParticipation.State.ACTIVE;
		if (active && redefineExternalCallbackDepth > 0) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"A callback cannot inherit the lexical redefine candidate scope",
					activeCandidateParticipation.getContext().getOldId()));
		}
		return active;
	}

	/**
	 * Rejects a public-construction entry inherited through an external callback.
	 * Normal candidate parsing and provider-owned file reconstruction remain
	 * eligible; the callback marker is the lexical authority boundary.
	 */
	public void requireCandidateParticipationCallerAllowed() {
		if (redefineExternalCallbackDepth > 0
				&& (activeCandidateParticipation != null
						|| !claimedCandidateParticipations.isEmpty())) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"A callback cannot add public construction participation to "
							+ "an active redefine"));
		}
	}

	/**
	 * Returns an earlier staged ID from the current lexical scope. This never
	 * consults labels and never publishes the reservation.
	 *
	 * @param geo exact candidate geo handle
	 * @return staged ID, or {@code null}
	 */
	public PersistentGeoId getStagedRedefineCandidateIdentity(GeoElement geo) {
		if (!isRedefineCandidateParticipationActive()) {
			return null;
		}
		GeoIdentityRecord staged = activeCandidateParticipation.getStagedRecord(geo);
		return staged == null ? null : staged.getId();
	}

	/**
	 * Transfers one fully reserved public-construction batch into the lexical
	 * redefine scope without changing the live graph or forcing persistence labels.
	 *
	 * @param participations reserved records keyed by their exact geo handles
	 */
	public void stageRedefineCandidateParticipations(
			Map<? extends GeoElement, ? extends GeoIdentityRecord> participations) {
		Objects.requireNonNull(participations);
		SpatialRedefineCandidateParticipation scope = activeCandidateParticipation;
		if (scope == null || scope.getState()
				!= SpatialRedefineCandidateParticipation.State.ACTIVE
				|| redefineExternalCallbackDepth != 0) {
			throw new IllegalStateException(
					"No active redefine candidate participation scope");
		}
		requireCurrentContext(scope.getContext());
		validateRedefineHostRollback(scope.getContext());
		IdentityHashMap<GeoElement, GeoIdentityRecord> prospective =
				scope.copyRecordsByGeo();
		Map<PersistentGeoId, GeoElement> prospectiveById = scope.copyGeosById();
		for (Map.Entry<? extends GeoElement, ? extends GeoIdentityRecord> entry
				: participations.entrySet()) {
			GeoElement geo = Objects.requireNonNull(entry.getKey());
			GeoIdentityRecord record = Objects.requireNonNull(entry.getValue());
			validateStagedRedefineCandidateGeo(geo, record.getId());
			validateRecordShape(record);
			if (owner != geo.getConstruction()
					|| idsByGeo.containsKey(geo)
					|| records.containsKey(record.getId())
					|| !record.getId().equals(reservedTokenIndex.get(
							record.getId().getRawToken()))
					|| !hasDirectedConstructionDependencies(record)) {
				throw new IllegalArgumentException(
						"Invalid reserved construction participation in redefine scope");
			}
			GeoIdentityRecord previous = prospective.put(geo, record);
			GeoElement previousGeo = prospectiveById.put(record.getId(), geo);
			if (previous != null || previousGeo != null) {
				throw new IllegalArgumentException(
						"Redefine candidate participation duplicates a geo or identity");
			}
		}
		LinkedHashSet<SpatialIdentityId> available =
				new LinkedHashSet<>(records.keySet());
		for (GeoIdentityRecord record : prospective.values()) {
			available.add(record.getId());
		}
		for (GeoIdentityRecord record : prospective.values()) {
			for (SpatialIdentityId dependency : record.getReferences()) {
				if (!available.contains(dependency)) {
					throw failure(SpatialIdentityDiagnostic.forReference(
							SpatialIdentityDiagnostic.Code.MISSING_REFERENCE,
							"Staged redefine participation omits a durable dependency",
							record.getId(), dependency));
				}
			}
		}
		java.util.function.Function<GeoElement, GeoIdentityRecord>
				prospectiveRecord = geo -> {
					GeoIdentityRecord staged = prospective.get(geo);
					if (staged != null) {
						return staged;
					}
					SpatialIdentityRecord current = records.get(idsByGeo.get(geo));
					return current instanceof GeoIdentityRecord
							? (GeoIdentityRecord) current : null;
				};
		validateConstructionIdentityDependencyDags(prospective,
				prospectiveRecord);
		for (Map.Entry<? extends GeoElement, ? extends GeoIdentityRecord> entry
				: participations.entrySet()) {
			GeoElement geo = entry.getKey();
			scope.put(geo, entry.getValue(), geo.isAuxiliaryObject());
		}
	}

	void sealRedefineCandidateParticipation(
			SpatialRedefineCandidateParticipation participation) {
		if (participation == null || participation != activeCandidateParticipation
				|| participation.getState()
						!= SpatialRedefineCandidateParticipation.State.ACTIVE) {
			throw new IllegalStateException(
					"Redefine candidate participation is not the active scope");
		}
		requireCurrentContext(participation.getContext());
		validateRedefineHostRollback(participation.getContext());
		participation.setState(SpatialRedefineCandidateParticipation.State.SEALED);
	}

	void abandonRedefineCandidateParticipation(
			SpatialRedefineCandidateParticipation participation) {
		if (participation == null || (participation.getState()
				!= SpatialRedefineCandidateParticipation.State.ACTIVE
				&& participation.getState()
						!= SpatialRedefineCandidateParticipation.State.SEALED)) {
			return;
		}
		if (activeCandidateParticipation == participation) {
			activeCandidateParticipation = null;
		}
		participation.rollbackPromotions();
		for (PersistentGeoId id : participation.copyGeosById().keySet()) {
			releaseReservation(id);
		}
		participation.setState(
				SpatialRedefineCandidateParticipation.State.ABANDONED);
		flushRuntimeAnnouncementsWhenUnleased();
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

	/**
	 * Atomically publishes a closed batch of ordinary construction-defined geo
	 * identities. Every declared dependency must already be active or be staged in
	 * this same batch; partial identity publication is impossible.
	 */
	public void registerConstructionParticipations(
			Map<? extends GeoElement, ? extends GeoIdentityRecord> participations) {
		Objects.requireNonNull(participations);
		IdentityHashMap<GeoElement, PersistentGeoId> attachments =
				new IdentityHashMap<>();
		ArrayList<SpatialIdentityRecord> batch = new ArrayList<>();
		LinkedHashSet<SpatialIdentityId> stagedIds = new LinkedHashSet<>();
		try {
			for (Map.Entry<? extends GeoElement, ? extends GeoIdentityRecord> entry
					: participations.entrySet()) {
				GeoElement geo = Objects.requireNonNull(entry.getKey());
				GeoIdentityRecord record = Objects.requireNonNull(entry.getValue());
				if (!hasDirectedConstructionDependencies(record)) {
					throw new IllegalArgumentException("Construction participation requires "
							+ "CONSTRUCTION_DEFINED and NOT_APPLICABLE");
				}
				if (!stagedIds.add(record.getId())) {
					throw new IllegalArgumentException(
							"Construction participation contains a duplicate identity");
				}
				attachments.put(geo, record.getId());
				batch.add(record);
			}
			for (SpatialIdentityRecord record : batch) {
				for (SpatialIdentityId dependency : record.getReferences()) {
					if (!records.containsKey(dependency)
							&& !stagedIds.contains(dependency)) {
						throw failure(SpatialIdentityDiagnostic.forReference(
								SpatialIdentityDiagnostic.Code.MISSING_REFERENCE,
								"Construction participation omits a durable dependency",
								record.getId(), dependency));
					}
				}
			}
			publishBatch(batch, attachments, false, false);
		} catch (RuntimeException exception) {
			for (SpatialIdentityId staged : stagedIds) {
				releaseReservation(staged);
			}
			throw exception;
		}
	}

	/** Atomically publishes non-geo records against the current registry. */
	public void registerRecords(Collection<? extends SpatialIdentityRecord> newRecords) {
		publishBatch(newRecords, new IdentityHashMap<GeoElement, PersistentGeoId>(),
				false, false);
	}

	/**
	 * Prepares a complete prospective lifecycle graph without changing live
	 * records, attachments, resolutions, XML or runtime topology.
	 *
	 * @return prepared atomic lifecycle transaction
	 */
	public SpatialLifecycleTransaction prepareLifecycleMutation(
			SpatialLifecycleMutation mutation) {
		Objects.requireNonNull(mutation);
		requireGraphSwitchNotInProgress();
		instrumentation.recordLifecyclePreparationAttempt();
		try {
			SpatialLifecycleProspectiveGraph graph = prospectiveGraph(mutation);
			SpatialLifecycleRuntime runtime = lifecycleRuntime;
			if (runtime == null && owner != null) {
				throw failure(SpatialIdentityDiagnostic.of(
						SpatialIdentityDiagnostic.Code.LIFECYCLE_RUNTIME_MISSING,
						"A construction-owned lifecycle commit requires its runtime adapter"));
			}
			if (runtime == null) {
				runtime = NO_OP_LIFECYCLE_RUNTIME;
			}
			SpatialLifecycleRuntime.PreparedSwitch preparedSwitch =
					Objects.requireNonNull(runtime.prepare(graph),
							"Lifecycle runtime returned no prepared switch");
			instrumentation.recordLifecyclePrepared();
			return new SpatialLifecycleTransaction(this, mutation, graph,
					preparedSwitch, mutationEpoch);
		} catch (RuntimeException exception) {
			releaseLifecycleReservations(mutation);
			instrumentation.recordLifecyclePreflightReject();
			recordOperationRollback(mutation.getOperationKind());
			throw exception;
		}
	}

	/**
	 * Prepares one complete explicit legacy-to-POINT association.
	 *
	 * @return prepared atomic migration transaction
	 */
	public SpatialLifecycleTransaction preparePointMigration(
			SpatialPointMigrationPlan plan) {
		Objects.requireNonNull(plan);
		SpatialLifecycleMutation.Builder mutation = SpatialLifecycleMutation.builder(
				SpatialLifecycleOperationKind.EXPLICIT_MIGRATION,
				plan.getProvenanceToken()).validatedMigrationPlan();
		for (SpatialIdentityRecord record : plan.getRecords()) {
			mutation.create(record instanceof SpatialObjectRecord
					? ((SpatialObjectRecord) record).withAssociationProvenance(
							SpatialObjectRecord.EXPLICIT_ASSOCIATION)
					: record);
		}
		for (Map.Entry<GeoElement, PersistentGeoId> attachment
				: plan.getAttachments().entrySet()) {
			mutation.attach(attachment.getKey(), attachment.getValue());
		}
		SpatialLifecycleMutation candidate = mutation.build();
		for (Map.Entry<GeoElement, PersistentGeoId> attachment
				: plan.getAttachments().entrySet()) {
			if (isParticipating(attachment.getKey())) {
				releaseLifecycleReservations(candidate);
				instrumentation.recordLifecyclePreparationAttempt();
				instrumentation.recordLifecyclePreflightReject();
				recordOperationRollback(candidate.getOperationKind());
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.MIGRATION_ALREADY_ASSOCIATED,
						"Explicit migration target is already associated",
						getPersistentGeoId(attachment.getKey())));
			}
		}
		return prepareLifecycleMutation(candidate);
	}

	/**
	 * Prepares one explicit complete-closure or same-construction copy plan.
	 *
	 * @return prepared atomic copy transaction
	 */
	public SpatialLifecycleTransaction prepareCopy(SpatialCopyPlan plan) {
		Objects.requireNonNull(plan);
		if (plan.getDestinationRegistry() != this) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.LIFECYCLE_EXTERNAL_REFERENCE,
					"Spatial copy plan names a different destination registry"));
		}
		SpatialIdentityRegistry source = plan.getSourceRegistry();
		LinkedHashSet<SpatialIdentityId> ownedIds = new LinkedHashSet<>();
		if (plan.getPolicy() == SpatialCopyPolicy.COMPLETE_CLOSURE) {
			for (SpatialIdentityRecord record : source.getClosureRecords(
					plan.getGeoCopies().keySet())) {
				ownedIds.add(record.getId());
			}
		} else {
			if (source != this) {
				throw failure(SpatialIdentityDiagnostic.of(
						SpatialIdentityDiagnostic.Code.LIFECYCLE_EXTERNAL_REFERENCE,
						"Declared external references are forbidden across constructions"));
			}
			ownedIds.addAll(plan.getOwnedRecordIds());
		}
		if (ownedIds.isEmpty()) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.INCOMPLETE_CLOSURE,
					"Spatial copy plan has no participating identity closure"));
		}

		ArrayList<SpatialIdentityRecord> ownedRecords = new ArrayList<>();
		for (SpatialIdentityId id : ownedIds) {
			SpatialIdentityRecord record = source.getRecord(id);
			if (record == null) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.INCOMPLETE_CLOSURE,
						"Spatial copy owns an identity absent from its source", id));
			}
			ownedRecords.add(record);
		}
		Set<SpatialIdentityId> externalContextIds = deriveExternalCopyContext(plan,
				ownedIds);
		validateCopyReferences(plan, ownedIds, ownedRecords, externalContextIds);

		Map<SpatialIdentityId, SpatialIdentityId> remap = allocateRemap(ownedRecords);
		try {
			SpatialLifecycleOperationKind operation = plan.getPolicy()
					== SpatialCopyPolicy.COMPLETE_CLOSURE
							? SpatialLifecycleOperationKind.COMPLETE_CLOSURE_COPY
							: SpatialLifecycleOperationKind.DECLARED_EXTERNAL_COPY;
			SpatialLifecycleMutation.Builder mutation = SpatialLifecycleMutation.builder(
					operation, plan.getProvenanceToken()).validatedCopyPlan();
			for (SpatialIdentityId external : externalContextIds) {
				mutation.declareExternalReference(external);
			}
			for (SpatialIdentityRecord record : ownedRecords) {
				mutation.create(record.remap(remap, true));
			}
			attachCopiedGeos(plan, ownedIds, remap, mutation);
			return prepareLifecycleMutation(mutation.build());
		} catch (RuntimeException exception) {
			for (SpatialIdentityId allocated : remap.values()) {
				releaseReservation(allocated);
			}
			throw exception;
		}
	}

	private Set<SpatialIdentityId> deriveExternalCopyContext(SpatialCopyPlan plan,
			Set<SpatialIdentityId> ownedIds) {
		if (plan.getPolicy() == SpatialCopyPolicy.COMPLETE_CLOSURE) {
			if (!plan.getExternalContextRootIds().isEmpty()) {
				throw failure(SpatialIdentityDiagnostic.of(
						SpatialIdentityDiagnostic.Code.LIFECYCLE_EXTERNAL_REFERENCE,
						"Complete-closure copy cannot retain external references"));
			}
			return Collections.emptySet();
		}
		LinkedHashSet<SpatialIdentityId> context = new LinkedHashSet<>();
		ArrayList<SpatialIdentityId> pending = new ArrayList<>();
		for (SpatialIdentityId root : plan.getExternalContextRootIds()) {
			SpatialIdentityRecord rootRecord = getRecord(root);
			if (!(rootRecord instanceof ProjectionSystemRecord)
					&& !(rootRecord instanceof ProjectionDiagramMapRecord)) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.LIFECYCLE_EXTERNAL_REFERENCE,
						"Declared external roots are restricted to typed system/map IDs",
						root));
			}
			pending.add(root);
		}
		for (int index = 0; index < pending.size(); index++) {
			SpatialIdentityId id = pending.get(index);
			if (!context.add(id)) {
				continue;
			}
			if (ownedIds.contains(id)) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.LIFECYCLE_EXTERNAL_REFERENCE,
						"A copied identity cannot also belong to retained context", id));
			}
			SpatialIdentityRecord record = getRecord(id);
			SpatialRecordResolution resolution = getResolution(id);
			if (!isAllowedExternalContextRecord(record) || resolution == null
					|| resolution.getState() != SpatialResolutionState.ACTIVE) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.LIFECYCLE_EXTERNAL_REFERENCE,
						"Retained external context must be a complete active POINT context",
						id));
			}
			for (SpatialIdentityId reference : record.getReferences()) {
				if (!context.contains(reference)) {
					pending.add(reference);
				}
			}
		}
		return Collections.unmodifiableSet(context);
	}

	private static boolean isAllowedExternalContextRecord(
			SpatialIdentityRecord record) {
		if (record instanceof GeoIdentityRecord) {
			return isPointPilotGeo((GeoIdentityRecord) record);
		}
		return record != null && record.getSemanticVersion() == 2
				&& (record instanceof ProjectionFrameRecord
						|| record instanceof ProjectionSystemRecord
						|| record instanceof ProjectionDiagramMapRecord
						|| record instanceof ProjectionFrameRelationRecord);
	}

	private void validateCopyReferences(SpatialCopyPlan plan,
			Set<SpatialIdentityId> ownedIds,
			Collection<? extends SpatialIdentityRecord> ownedRecords,
			Set<SpatialIdentityId> externalIds) {
		for (SpatialIdentityId external : externalIds) {
			if (ownedIds.contains(external)) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.LIFECYCLE_EXTERNAL_REFERENCE,
						"A copied identity cannot also be declared external", external));
			}
			SpatialIdentityRecord record = getRecord(external);
			SpatialRecordResolution resolution = getResolution(external);
			if (record == null || resolution == null
					|| resolution.getState() != SpatialResolutionState.ACTIVE) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.LIFECYCLE_EXTERNAL_REFERENCE,
						"Declared external identity is not active in the destination",
						external));
			}
		}
		for (SpatialIdentityRecord record : ownedRecords) {
			for (SpatialIdentityId reference : record.getReferences()) {
				if (!ownedIds.contains(reference) && !externalIds.contains(reference)) {
					throw failure(SpatialIdentityDiagnostic.forReference(
							SpatialIdentityDiagnostic.Code.INCOMPLETE_CLOSURE,
							"Spatial copy has an undeclared external reference",
							record.getId(), reference));
				}
			}
		}
	}

	private void attachCopiedGeos(SpatialCopyPlan plan,
			Set<SpatialIdentityId> ownedIds,
			Map<SpatialIdentityId, SpatialIdentityId> remap,
			SpatialLifecycleMutation.Builder mutation) {
		Map<GeoElement, GeoElement> geoCopies = plan.getGeoCopies();
		Set<GeoElement> consumedSources = Collections.newSetFromMap(
				new IdentityHashMap<GeoElement, Boolean>());
		Set<GeoElement> consumedTargets = Collections.newSetFromMap(
				new IdentityHashMap<GeoElement, Boolean>());
		for (SpatialIdentityId id : ownedIds) {
			SpatialIdentityRecord record = plan.getSourceRegistry().getRecord(id);
			if (!(record instanceof GeoIdentityRecord)) {
				continue;
			}
			GeoElement sourceGeo = plan.getSourceRegistry().getGeo((PersistentGeoId) id);
			GeoElement targetGeo = sourceGeo == null ? null : geoCopies.get(sourceGeo);
			if (sourceGeo == null || targetGeo == null) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.INCOMPLETE_CLOSURE,
						"Copied geo identity has no explicit source-to-target handle", id));
			}
			if (!consumedTargets.add(targetGeo)) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.GEO_ALREADY_PARTICIPATING,
						"Two copied identities target the same ordinary geo", id));
			}
			consumedSources.add(sourceGeo);
			mutation.attach(targetGeo, (PersistentGeoId) remap.get(id));
		}
		if (consumedSources.size() != geoCopies.size()) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.INCOMPLETE_CLOSURE,
					"Spatial copy contains a geo handle outside its owned closure"));
		}
	}

	private SpatialLifecycleProspectiveGraph prospectiveGraph(
			SpatialLifecycleMutation mutation) {
		validateLifecycleCapabilitySeal(mutation);
		validateExpectedLifecycleRecords(mutation);
		Map<SpatialIdentityId, SpatialIdentityRecord> prospectiveRecords =
				new LinkedHashMap<>(records);
		IdentityHashMap<GeoElement, PersistentGeoId> prospectiveIdsByGeo =
				new IdentityHashMap<>(idsByGeo);
		Map<PersistentGeoId, GeoElement> prospectiveGeosById =
				new LinkedHashMap<>(geosById);

		for (Map.Entry<GeoElement, PersistentGeoId> detachment
				: mutation.copyDetachments().entrySet()) {
			PersistentGeoId current = prospectiveIdsByGeo.get(detachment.getKey());
			if (!detachment.getValue().equals(current)
					|| prospectiveGeosById.get(current) != detachment.getKey()) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.LIFECYCLE_STALE_SOURCE,
						"Lifecycle detachment does not name the current geo identity",
						detachment.getValue()));
			}
			prospectiveIdsByGeo.remove(detachment.getKey());
			prospectiveGeosById.remove(current);
		}

		for (SpatialIdentityId retiredId : mutation.getRetiredIds()) {
			SpatialIdentityRecord current = prospectiveRecords.get(retiredId);
			if (current == null) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.LIFECYCLE_STALE_SOURCE,
						"Lifecycle retirement target is no longer current", retiredId));
			}
			if (current instanceof GeoIdentityRecord
					&& prospectiveGeosById.containsKey(retiredId)) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.GEO_ATTACHMENT_MISSING,
						"Retiring a geo identity requires explicit detachment", retiredId));
			}
			prospectiveRecords.remove(retiredId);
		}

		for (SpatialIdentityRecord replacement
				: mutation.getReplacementRecords().values()) {
			validateRecordShape(replacement);
			if (!prospectiveRecords.containsKey(replacement.getId())) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.LIFECYCLE_STALE_SOURCE,
						"Lifecycle replacement target is no longer current",
						replacement.getId()));
			}
			prospectiveRecords.put(replacement.getId(), replacement);
		}

		for (SpatialIdentityRecord created : mutation.getCreatedRecords().values()) {
			validateRecordShape(created);
			if (prospectiveRecords.containsKey(created.getId())) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.DUPLICATE_ID,
						"Lifecycle create identity is already active", created.getId()));
			}
			requireLifecycleCreateReservation(created.getId(),
					mutation.getOperationKind());
			prospectiveRecords.put(created.getId(), created);
		}

		for (Map.Entry<GeoElement, PersistentGeoId> attachment
				: mutation.copyAttachments().entrySet()) {
			validateAttachmentGeo(attachment.getKey(), attachment.getValue());
			SpatialIdentityRecord record = prospectiveRecords.get(attachment.getValue());
			if (!(record instanceof GeoIdentityRecord)) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.GEO_ATTACHMENT_MISSING,
						"Lifecycle attachment has no prospective geo record",
						attachment.getValue()));
			}
			PersistentGeoId existingId = prospectiveIdsByGeo.get(attachment.getKey());
			GeoElement existingGeo = prospectiveGeosById.get(attachment.getValue());
			if ((existingId != null && !existingId.equals(attachment.getValue()))
					|| (existingGeo != null && existingGeo != attachment.getKey())) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.GEO_ALREADY_PARTICIPATING,
						"Lifecycle attachment conflicts with current participation",
						attachment.getValue()));
			}
			prospectiveIdsByGeo.put(attachment.getKey(), attachment.getValue());
			prospectiveGeosById.put(attachment.getValue(), attachment.getKey());
		}

		validateProspectiveAttachments(prospectiveRecords, prospectiveIdsByGeo,
				prospectiveGeosById);
		for (SpatialIdentityRecord record : prospectiveRecords.values()) {
			validateRecordShape(record);
		}
		validatePointV2Reciprocity(prospectiveRecords);
		Map<SpatialIdentityId, SpatialRecordResolution> prospectiveResolutions =
				resolveAll(prospectiveRecords);
		validateLifecycleResolutions(mutation, prospectiveRecords,
				prospectiveResolutions);
		validateLifecycleRevisions(mutation, prospectiveRecords);

		Set<SpatialIdentityId> changedRecords = changedRecordIds(records,
				prospectiveRecords);
		Set<SpatialIdentityId> changedResolutions = changedResolutionIds(resolutions,
				prospectiveResolutions);
		return new SpatialLifecycleProspectiveGraph(mutation.getOperationKind(),
				mutation.getProvenanceToken(), prospectiveRecords, prospectiveIdsByGeo,
				prospectiveGeosById, prospectiveResolutions, changedRecords,
				changedResolutions, mutation.getDeclaredExternalReferenceIds());
	}

	private void validateLifecycleCapabilitySeal(
			SpatialLifecycleMutation mutation) {
		SpatialLifecycleOperationKind kind = mutation.getOperationKind();
		boolean sealed = true;
		if (kind == SpatialLifecycleOperationKind.COMPLETE_CLOSURE_COPY
				|| kind == SpatialLifecycleOperationKind.DECLARED_EXTERNAL_COPY) {
			sealed = mutation.isCopyPlanValidated();
		} else if (kind == SpatialLifecycleOperationKind.EXPLICIT_MIGRATION) {
			sealed = mutation.isMigrationPlanValidated();
		} else if (kind == SpatialLifecycleOperationKind.REFERENCE_RECOVERY) {
			sealed = mutation.isReferenceRecoveryValidated();
		} else if (kind == SpatialLifecycleOperationKind.SEMANTIC_NO_OP
				|| kind
						== SpatialLifecycleOperationKind.COMPATIBLE_DEFINITION_CHANGE
				|| kind == SpatialLifecycleOperationKind.ADMITTED_TOPOLOGY_CHANGE
				|| kind == SpatialLifecycleOperationKind.TRUE_REPLACEMENT) {
			sealed = mutation.isProviderValidatedRedefine();
		}
		if (!sealed) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.LIFECYCLE_SCOPE_VIOLATION,
					"This lifecycle operation requires its internal validated plan"));
		}
	}

	private void validateExpectedLifecycleRecords(SpatialLifecycleMutation mutation) {
		for (SpatialIdentityRecord expected : mutation.getExpectedRecords().values()) {
			SpatialIdentityRecord current = records.get(expected.getId());
			if (current == null || !SpatialRecordXmlCodec.writeRecord(current).equals(
					SpatialRecordXmlCodec.writeRecord(expected))) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.LIFECYCLE_STALE_SOURCE,
						"Lifecycle expected source record is no longer exact",
						expected.getId()));
			}
		}
	}

	private void requireLifecycleCreateReservation(SpatialIdentityId id,
			SpatialLifecycleOperationKind operationKind) {
		SpatialIdentityId reserved = reservedTokenIndex.get(id.getRawToken());
		if (id.equals(reserved)) {
			return;
		}
		if (operationKind == SpatialLifecycleOperationKind.REFERENCE_RECOVERY
				&& !issuedTokenIndex.containsKey(id.getRawToken())
				&& !rawTokenIndex.containsKey(id.getRawToken())
				&& !reservedTokenIndex.containsKey(id.getRawToken())
				&& !retiredTokenIndex.containsKey(id.getRawToken())
				&& isExplicitlyMissing(id)) {
			// The exact referenced token is prospective only. It is not an ID
			// returned by this registry's allocator and becomes issued only if the
			// complete graph/runtime transaction commits.
			return;
		}
		throw failure(SpatialIdentityDiagnostic.forSubject(
				SpatialIdentityDiagnostic.Code.LIFECYCLE_CREATE_NOT_RESERVED,
				"Lifecycle create requires a fresh reservation in this registry", id));
	}

	private boolean isExplicitlyMissing(SpatialIdentityId id) {
		for (SpatialIdentityRecord record : records.values()) {
			if (record.getReferences().contains(id)) {
				SpatialRecordResolution resolution = resolutions.get(record.getId());
				if (resolution != null
						&& resolution.getState() == SpatialResolutionState.BROKEN) {
					return true;
				}
			}
		}
		return false;
	}

	private void validateProspectiveAttachments(
			Map<SpatialIdentityId, SpatialIdentityRecord> prospectiveRecords,
			IdentityHashMap<GeoElement, PersistentGeoId> prospectiveIdsByGeo,
			Map<PersistentGeoId, GeoElement> prospectiveGeosById) {
		for (SpatialIdentityRecord record : prospectiveRecords.values()) {
			if (record instanceof GeoIdentityRecord) {
				GeoElement geo = prospectiveGeosById.get(record.getId());
				if (geo == null || !record.getId().equals(prospectiveIdsByGeo.get(geo))) {
					throw failure(SpatialIdentityDiagnostic.forSubject(
							SpatialIdentityDiagnostic.Code.GEO_ATTACHMENT_MISSING,
							"Prospective geo record has no reciprocal attachment",
							record.getId()));
				}
			}
		}
		for (Map.Entry<GeoElement, PersistentGeoId> attachment
				: prospectiveIdsByGeo.entrySet()) {
			if (!(prospectiveRecords.get(attachment.getValue())
					instanceof GeoIdentityRecord)
					|| prospectiveGeosById.get(attachment.getValue())
							!= attachment.getKey()) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.GEO_ATTACHMENT_MISSING,
						"Prospective attachment has no reciprocal geo record",
						attachment.getValue()));
			}
		}
	}

	private void validatePointV2Reciprocity(
			Map<SpatialIdentityId, SpatialIdentityRecord> graph) {
		for (SpatialIdentityRecord record : graph.values()) {
			if (record.getSemanticVersion() != 2) {
				continue;
			}
			if (record instanceof ProjectionSystemRecord) {
				validateSystemReciprocity(graph, (ProjectionSystemRecord) record);
			} else if (record instanceof ProjectionDiagramMapRecord) {
				validateMapReciprocity(graph, (ProjectionDiagramMapRecord) record);
			} else if (record instanceof ProjectionFrameRelationRecord) {
				validateRelationReciprocity(graph,
						(ProjectionFrameRelationRecord) record);
			} else if (record instanceof SpatialObjectRecord) {
				validateObjectReciprocity(graph, (SpatialObjectRecord) record);
			} else if (record instanceof ProjectionBindingRecord) {
				validateBindingReciprocity(graph, (ProjectionBindingRecord) record);
			}
		}
	}

	private void validateSystemReciprocity(
			Map<SpatialIdentityId, SpatialIdentityRecord> graph,
			ProjectionSystemRecord system) {
		for (ProjectionDiagramMapId mapId : system.getMapIds()) {
			ProjectionDiagramMapRecord map = graphRecordIfPresent(graph, mapId,
					ProjectionDiagramMapRecord.class, system.getId());
			if (map != null && (map.getSemanticVersion() != 2
					|| !system.getId().equals(map.getSystemId()))) {
				throw reciprocalFailure(system.getId(), mapId,
						"Projection system/map ownership is not reciprocal");
			}
		}
		for (ProjectionFrameRelationId relationId : system.getRelationIds()) {
			ProjectionFrameRelationRecord relation = graphRecordIfPresent(graph,
					relationId,
					ProjectionFrameRelationRecord.class, system.getId());
			if (relation != null && (relation.getSemanticVersion() != 2
					|| !system.getId().equals(relation.getSystemId()))) {
				throw reciprocalFailure(system.getId(), relationId,
						"Projection system/relation ownership is not reciprocal");
			}
		}
	}

	private void validateMapReciprocity(
			Map<SpatialIdentityId, SpatialIdentityRecord> graph,
			ProjectionDiagramMapRecord map) {
		ProjectionSystemRecord system = graphRecordIfPresent(graph, map.getSystemId(),
				ProjectionSystemRecord.class, map.getId());
		ProjectionFrameRecord frame = graphRecordIfPresent(graph, map.getFrameId(),
				ProjectionFrameRecord.class, map.getId());
		if (system != null && (system.getSemanticVersion() != 2
				|| !system.getMapIds().contains(map.getId())
				|| !system.getUnits().equals(map.getUnits()))) {
			throw reciprocalFailure(map.getId(), map.getSystemId(),
					"Version-two map system membership or target units disagree");
		}
		if (frame != null && frame.getSemanticVersion() != 2) {
			throw reciprocalFailure(map.getId(), map.getFrameId(),
					"Version-two map requires a version-two source frame");
		}
		for (ProjectionFrameRelationId relationId : map.getRelationIds()) {
			ProjectionFrameRelationRecord relation = graphRecordIfPresent(graph,
					relationId,
					ProjectionFrameRelationRecord.class, map.getId());
			if (relation != null
					&& (!map.getSystemId().equals(relation.getSystemId())
					|| (!map.getId().equals(relation.getSourceMapId())
							&& !map.getId().equals(relation.getDestinationMapId())))) {
				throw reciprocalFailure(map.getId(), relationId,
						"Map/relation membership is not reciprocal");
			}
		}
	}

	private void validateRelationReciprocity(
			Map<SpatialIdentityId, SpatialIdentityRecord> graph,
			ProjectionFrameRelationRecord relation) {
		ProjectionSystemRecord system = graphRecordIfPresent(graph,
				relation.getSystemId(),
				ProjectionSystemRecord.class, relation.getId());
		ProjectionDiagramMapRecord source = graphRecordIfPresent(graph,
				relation.getSourceMapId(), ProjectionDiagramMapRecord.class,
				relation.getId());
		ProjectionDiagramMapRecord destination = graphRecordIfPresent(graph,
				relation.getDestinationMapId(), ProjectionDiagramMapRecord.class,
				relation.getId());
		if (relation.getSourceMapId().equals(relation.getDestinationMapId())
				|| system != null && (system.getSemanticVersion() != 2
						|| !system.getRelationIds().contains(relation.getId()))
				|| source != null && (source.getSemanticVersion() != 2
						|| !relation.getSystemId().equals(source.getSystemId())
						|| !source.getRelationIds().contains(relation.getId()))
				|| destination != null && (destination.getSemanticVersion() != 2
						|| !relation.getSystemId().equals(destination.getSystemId())
						|| !destination.getRelationIds().contains(relation.getId()))) {
			throw reciprocalFailure(relation.getId(), relation.getSystemId(),
					"Version-two relation endpoints are not reciprocal");
		}
	}

	private void validateObjectReciprocity(
			Map<SpatialIdentityId, SpatialIdentityRecord> graph,
			SpatialObjectRecord object) {
		ProjectionSystemRecord system = graphRecordIfPresent(graph,
				object.getSystemId(),
				ProjectionSystemRecord.class, object.getId());
		if (system != null && system.getSemanticVersion() != 2) {
			throw reciprocalFailure(object.getId(), system.getId(),
					"Version-two POINT object requires a version-two system");
		}
		for (ProjectionBindingId bindingId : object.getBindingIds()) {
			ProjectionBindingRecord binding = graphRecordIfPresent(graph, bindingId,
					ProjectionBindingRecord.class, object.getId());
			if (binding != null && (binding.getSemanticVersion() != 2
					|| !object.getId().equals(binding.getObjectId())
					|| !object.getSystemId().equals(binding.getSystemId()))) {
				throw reciprocalFailure(object.getId(), bindingId,
						"POINT object/binding ownership is not reciprocal");
			}
		}
	}

	private void validateBindingReciprocity(
			Map<SpatialIdentityId, SpatialIdentityRecord> graph,
			ProjectionBindingRecord binding) {
		SpatialObjectRecord object = graphRecordIfPresent(graph, binding.getObjectId(),
				SpatialObjectRecord.class, binding.getId());
		ProjectionSystemRecord system = graphRecordIfPresent(graph,
				binding.getSystemId(),
				ProjectionSystemRecord.class, binding.getId());
		ProjectionDiagramMapRecord map = graphRecordIfPresent(graph,
				binding.getDiagramMapId(), ProjectionDiagramMapRecord.class,
				binding.getId());
		if (object != null && (object.getSemanticVersion() != 2
				|| !object.getBindingIds().contains(binding.getId())
				|| !object.getSystemId().equals(binding.getSystemId()))
				|| system != null && system.getSemanticVersion() != 2
				|| map != null && (map.getSemanticVersion() != 2
						|| !binding.getSystemId().equals(map.getSystemId())
						|| !binding.getFrameId().equals(map.getFrameId()))
				|| system != null && map != null
						&& !system.getMapIds().contains(map.getId())) {
			throw reciprocalFailure(binding.getId(), binding.getObjectId(),
					"POINT binding/object/system/map membership is not reciprocal");
		}
	}

	private <T extends SpatialIdentityRecord> T graphRecordIfPresent(
			Map<SpatialIdentityId, SpatialIdentityRecord> graph, SpatialIdentityId id,
			Class<T> type, SpatialIdentityId subject) {
		SpatialIdentityRecord record = graph.get(id);
		if (record == null) {
			return null;
		}
		if (!type.isInstance(record)) {
			throw reciprocalFailure(subject, id,
					"Prospective graph reference has the wrong record kind");
		}
		return type.cast(record);
	}

	private SpatialIdentityException reciprocalFailure(SpatialIdentityId subject,
			SpatialIdentityId reference, String message) {
		return failure(SpatialIdentityDiagnostic.forReference(
				SpatialIdentityDiagnostic.Code.LIFECYCLE_RECIPROCAL_MISMATCH,
				message, subject, reference));
	}

	private Map<SpatialIdentityId, SpatialRecordResolution> resolveAll(
			Map<SpatialIdentityId, SpatialIdentityRecord> graph) {
		Map<SpatialIdentityId, SpatialRecordResolution> result = new LinkedHashMap<>();
		for (SpatialIdentityRecord record : immutableSortedRecords(graph.values())) {
			ArrayList<SpatialIdentityDiagnostic> missing = new ArrayList<>();
			for (SpatialIdentityId reference : record.getReferences()) {
				if (!graph.containsKey(reference)) {
					missing.add(SpatialIdentityDiagnostic.forReference(
							SpatialIdentityDiagnostic.Code.MISSING_REFERENCE,
							"Record retains an unresolved typed reference",
							record.getId(), reference));
				}
			}
			result.put(record.getId(), missing.isEmpty()
					? SpatialRecordResolution.active()
					: new SpatialRecordResolution(SpatialResolutionState.BROKEN, missing));
		}
		return result;
	}

	private void validateLifecycleResolutions(SpatialLifecycleMutation mutation,
			Map<SpatialIdentityId, SpatialIdentityRecord> prospectiveRecords,
			Map<SpatialIdentityId, SpatialRecordResolution> prospectiveResolutions) {
		for (SpatialIdentityRecord changed : changedLifecycleRecords(mutation)) {
			SpatialRecordResolution resolution = prospectiveResolutions.get(changed.getId());
			if (resolution == null
					|| resolution.getState() != SpatialResolutionState.ACTIVE) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.LIFECYCLE_NEW_BROKEN_REFERENCE,
						"Created or replaced lifecycle record is not fully resolved",
						changed.getId()));
			}
		}
		for (Map.Entry<SpatialIdentityId, SpatialRecordResolution> current
				: resolutions.entrySet()) {
			SpatialRecordResolution next = prospectiveResolutions.get(current.getKey());
			if (current.getValue().getState() == SpatialResolutionState.ACTIVE
					&& next != null && next.getState() == SpatialResolutionState.BROKEN) {
				String message = mutation.getOperationKind()
						== SpatialLifecycleOperationKind.SYSTEM_REPLACEMENT
								? "System replacement must explicitly retire or replace "
										+ "every affected active dependent"
								: "Lifecycle mutation would create a dangling active reference";
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.LIFECYCLE_NEW_BROKEN_REFERENCE,
						message,
						current.getKey()));
			}
		}
		for (SpatialIdentityId external
				: mutation.getDeclaredExternalReferenceIds()) {
			if (!prospectiveRecords.containsKey(external)
					|| mutation.getCreatedRecords().containsKey(external)
					|| mutation.getReplacementRecords().containsKey(external)
					|| mutation.getRetiredIds().contains(external)
					|| prospectiveResolutions.get(external).getState()
							!= SpatialResolutionState.ACTIVE) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.LIFECYCLE_EXTERNAL_REFERENCE,
						"Declared external identity must remain active and unchanged",
						external));
			}
		}
		if (mutation.getOperationKind()
				== SpatialLifecycleOperationKind.EXPLICIT_MIGRATION) {
			Set<SpatialIdentityId> created = mutation.getCreatedRecords().keySet();
			for (SpatialIdentityRecord record : mutation.getCreatedRecords().values()) {
				for (SpatialIdentityId reference : record.getReferences()) {
					if (!created.contains(reference)) {
						throw failure(SpatialIdentityDiagnostic.forReference(
								SpatialIdentityDiagnostic.Code.MIGRATION_INCOMPLETE,
								"Explicit migration must supply the complete typed closure",
								record.getId(), reference));
					}
				}
			}
			validateMigrationClosure(mutation);
		}
		if (mutation.getOperationKind()
				== SpatialLifecycleOperationKind.REFERENCE_RECOVERY) {
			boolean repaired = false;
			for (Map.Entry<SpatialIdentityId, SpatialRecordResolution> current
					: resolutions.entrySet()) {
				SpatialRecordResolution next = prospectiveResolutions.get(current.getKey());
				repaired |= current.getValue().getState() == SpatialResolutionState.BROKEN
						&& next != null
						&& next.getState() == SpatialResolutionState.ACTIVE;
			}
			if (!repaired) {
				throw failure(SpatialIdentityDiagnostic.of(
						SpatialIdentityDiagnostic.Code.LIFECYCLE_NEW_BROKEN_REFERENCE,
						"Reference recovery did not activate any exact broken reference"));
			}
		}
	}

	private void validateMigrationClosure(SpatialLifecycleMutation mutation) {
		Map<SpatialIdentityId, SpatialIdentityRecord> created =
				mutation.getCreatedRecords();
		LinkedHashSet<SpatialIdentityId> pointRoots = new LinkedHashSet<>();
		boolean hasSystem = false;
		boolean hasMap = false;
		boolean hasFrame = false;
		boolean hasBinding = false;
		for (SpatialIdentityRecord record : created.values()) {
			if (record instanceof GeoIdentityRecord) {
				GeoIdentityRecord geo = (GeoIdentityRecord) record;
				if (geo.getAuthority() != EditAuthorityMode.PROJECTION_DEFINED
						|| !SpatialObjectRecord.POINT_SCHEMA_ID.equals(geo.getSchemaId())
						|| geo.getSchemaVersion()
								!= SpatialObjectRecord.POINT_SCHEMA_VERSION
						|| geo.getOutputCardinality() != 1
						|| !mutation.getAttachments().containsValue(geo.getId())) {
					throw migrationFailure(geo.getId(),
							"Migration geo is not an attached POINT-v2 input identity");
				}
				continue;
			}
			if (record.getSemanticVersion() != 2) {
				throw migrationFailure(record.getId(),
						"Migration association contains a non-v2 semantic record");
			}
			if (record instanceof SpatialObjectRecord) {
				SpatialObjectRecord object = (SpatialObjectRecord) record;
				if (!SpatialObjectRecord.POINT_TYPE.equals(object.getSpatialType())
						|| object.getAuthority()
								!= EditAuthorityMode.PROJECTION_DEFINED
						|| !SpatialObjectRecord.POINT_SCHEMA_ID.equals(
								object.getSchemaId())
						|| object.getSchemaVersion()
								!= SpatialObjectRecord.POINT_SCHEMA_VERSION
						|| !SpatialObjectRecord.EXPLICIT_ASSOCIATION.equals(
								object.getAssociationProvenance())
						|| !hasDefiningBinding(object, created)) {
					throw migrationFailure(object.getId(),
							"Migration object is not an admitted projection-defined POINT");
				}
				pointRoots.add(object.getId());
			} else if (record instanceof ProjectionSystemRecord) {
				hasSystem = true;
			} else if (record instanceof ProjectionDiagramMapRecord) {
				hasMap = true;
			} else if (record instanceof ProjectionFrameRecord) {
				hasFrame = true;
			} else if (record instanceof ProjectionBindingRecord) {
				hasBinding = true;
			} else if (!(record instanceof ProjectionFrameRelationRecord)) {
				throw migrationFailure(record.getId(),
						"Migration association contains an unrelated record kind");
			}
		}
		if (pointRoots.isEmpty() || !hasSystem || !hasMap || !hasFrame
				|| !hasBinding) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.MIGRATION_INCOMPLETE,
					"Migration requires a complete POINT/system/map/frame/binding graph"));
		}
		LinkedHashSet<SpatialIdentityId> reachable = new LinkedHashSet<>(pointRoots);
		boolean changed;
		do {
			changed = false;
			for (SpatialIdentityId id : new ArrayList<>(reachable)) {
				SpatialIdentityRecord record = created.get(id);
				if (record != null) {
					changed |= reachable.addAll(record.getReferences());
				}
			}
		} while (changed);
		if (!reachable.equals(created.keySet())) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.MIGRATION_INCOMPLETE,
					"Migration contains records unrelated to its POINT association"));
		}
	}

	private boolean hasDefiningBinding(SpatialObjectRecord object,
			Map<SpatialIdentityId, SpatialIdentityRecord> recordsById) {
		for (ProjectionBindingId bindingId : object.getBindingIds()) {
			SpatialIdentityRecord record = recordsById.get(bindingId);
			if (record instanceof ProjectionBindingRecord
					&& ((ProjectionBindingRecord) record).getRole()
							== ProjectionBindingRole.DEFINING) {
				return true;
			}
		}
		return false;
	}

	private SpatialIdentityException migrationFailure(SpatialIdentityId subject,
			String message) {
		return failure(SpatialIdentityDiagnostic.forSubject(
				SpatialIdentityDiagnostic.Code.MIGRATION_INCOMPLETE,
				message, subject));
	}

	private Collection<SpatialIdentityRecord> changedLifecycleRecords(
			SpatialLifecycleMutation mutation) {
		ArrayList<SpatialIdentityRecord> changed = new ArrayList<>();
		changed.addAll(mutation.getCreatedRecords().values());
		changed.addAll(mutation.getReplacementRecords().values());
		return changed;
	}

	private void validateLifecycleRevisions(SpatialLifecycleMutation mutation,
			Map<SpatialIdentityId, SpatialIdentityRecord> prospectiveRecords) {
		validatePointLifecycleBoundary(mutation, prospectiveRecords);
		validateOperationShape(mutation);
		SpatialLifecycleOperationKind operation = mutation.getOperationKind();
		for (SpatialIdentityRecord created : mutation.getCreatedRecords().values()) {
			if (operation != SpatialLifecycleOperationKind.COMPLETE_CLOSURE_COPY
					&& operation != SpatialLifecycleOperationKind.DECLARED_EXTERNAL_COPY
					&& !hasZeroRevisions(created)) {
				throw revisionFailure(created.getId(),
						"A fresh lifecycle identity must start at revision zero");
			}
			if ((operation == SpatialLifecycleOperationKind.COMPLETE_CLOSURE_COPY
					|| operation == SpatialLifecycleOperationKind.DECLARED_EXTERNAL_COPY)
					&& created.getCopySourceId() == null) {
				throw revisionFailure(created.getId(),
						"Copied records require immediate copy-source provenance");
			}
		}
		for (SpatialIdentityRecord replacement
				: mutation.getReplacementRecords().values()) {
			SpatialIdentityRecord current = records.get(replacement.getId());
			if (current == null || current.getClass() != replacement.getClass()) {
				throw revisionFailure(replacement.getId(),
						"A compatible replacement must preserve record kind and class");
			}
			validateSameIdentityContract(operation, current, replacement);
			validateReplacementRevision(operation, current, replacement);
		}
		if (operation == SpatialLifecycleOperationKind.SEMANTIC_NO_OP
				&& !changedRecordIds(records, prospectiveRecords).isEmpty()) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.LIFECYCLE_REVISION_MISMATCH,
					"A semantic no-op cannot change the canonical identity graph"));
		}
	}

	private void validatePointLifecycleBoundary(SpatialLifecycleMutation mutation,
			Map<SpatialIdentityId, SpatialIdentityRecord> prospectiveRecords) {
		SpatialLifecycleOperationKind operation = mutation.getOperationKind();
		if (operation == SpatialLifecycleOperationKind.COMPLETE_CLOSURE_COPY
				|| operation == SpatialLifecycleOperationKind.DECLARED_EXTERNAL_COPY) {
			return;
		}
		ArrayList<SpatialIdentityRecord> touched = new ArrayList<>();
		touched.addAll(mutation.getCreatedRecords().values());
		touched.addAll(mutation.getReplacementRecords().values());
		for (SpatialIdentityId retiredId : mutation.getRetiredIds()) {
			SpatialIdentityRecord retired = records.get(retiredId);
			if (retired != null) {
				touched.add(retired);
			}
		}
		for (SpatialIdentityRecord record : touched) {
			if (record instanceof GeoIdentityRecord) {
				GeoIdentityRecord geo = (GeoIdentityRecord) record;
				if (isProviderValidatedRedefine(mutation, operation)) {
					continue;
				}
				boolean explicitFreshReplacement = operation
						== SpatialLifecycleOperationKind.TRUE_REPLACEMENT
						&& mutation.getCreatedRecords().get(record.getId()) == record;
				boolean pointInput = isPointPilotGeo(geo)
						&& (explicitFreshReplacement
								|| isReferencedByVersionTwo(record.getId(), records)
								|| isReferencedByVersionTwo(record.getId(),
										prospectiveRecords));
				if (!pointInput) {
					throw lifecycleScopeFailure(record.getId(),
							"Lifecycle geo is outside the POINT-v2 semantic closure");
				}
			} else if (record.getSemanticVersion() != 2
					|| !isPointPilotRecord(record)) {
				throw lifecycleScopeFailure(record.getId(),
						"Productive lifecycle operations admit POINT-v2 records only");
			}
		}
	}

	private static boolean isProviderValidatedRedefine(
			SpatialLifecycleMutation mutation,
			SpatialLifecycleOperationKind operation) {
		return mutation.isProviderValidatedRedefine()
				&& (operation == SpatialLifecycleOperationKind.SEMANTIC_NO_OP
						|| operation
								== SpatialLifecycleOperationKind.COMPATIBLE_DEFINITION_CHANGE
						|| operation
								== SpatialLifecycleOperationKind.ADMITTED_TOPOLOGY_CHANGE
						|| operation == SpatialLifecycleOperationKind.TRUE_REPLACEMENT);
	}

	private static boolean isPointPilotGeo(GeoIdentityRecord geo) {
		return geo.getAuthority() == EditAuthorityMode.PROJECTION_DEFINED
				&& SpatialObjectRecord.POINT_SCHEMA_ID.equals(geo.getSchemaId())
				&& geo.getSchemaVersion() == SpatialObjectRecord.POINT_SCHEMA_VERSION
				&& geo.getOutputCardinality() == 1;
	}

	private static boolean isReferencedByVersionTwo(SpatialIdentityId id,
			Map<SpatialIdentityId, SpatialIdentityRecord> graph) {
		for (SpatialIdentityRecord record : graph.values()) {
			if (record.getSemanticVersion() == 2
					&& record.getReferences().contains(id)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isPointPilotRecord(SpatialIdentityRecord record) {
		return record instanceof SpatialObjectRecord
				|| record instanceof ProjectionFrameRecord
				|| record instanceof ProjectionSystemRecord
				|| record instanceof ProjectionDiagramMapRecord
				|| record instanceof ProjectionFrameRelationRecord
				|| record instanceof ProjectionBindingRecord;
	}

	private SpatialIdentityException lifecycleScopeFailure(
			SpatialIdentityId subject, String message) {
		return failure(SpatialIdentityDiagnostic.forSubject(
				SpatialIdentityDiagnostic.Code.LIFECYCLE_SCOPE_VIOLATION,
				message, subject));
	}

	private void validateOperationShape(SpatialLifecycleMutation mutation) {
		SpatialLifecycleOperationKind kind = mutation.getOperationKind();
		validateLifecycleAttachmentPolicy(mutation, kind);
		if (kind == SpatialLifecycleOperationKind.SEMANTIC_NO_OP
				|| kind == SpatialLifecycleOperationKind.COMPATIBLE_DEFINITION_CHANGE
				|| kind == SpatialLifecycleOperationKind.ADMITTED_TOPOLOGY_CHANGE) {
			validateSealedProviderRedefineShape(mutation);
		} else if (kind == SpatialLifecycleOperationKind.BINDING_ADD) {
			requireExactLifecycleShape(mutation, 1, 0, 1,
					new Class<?>[] {ProjectionBindingRecord.class},
					new Class<?>[0], new Class<?>[] {SpatialObjectRecord.class});
			validateBindingMembershipContinuity(mutation, true);
		} else if (kind == SpatialLifecycleOperationKind.BINDING_REMOVE) {
			requireExactLifecycleShape(mutation, 0, 1, 1, new Class<?>[0],
					new Class<?>[] {ProjectionBindingRecord.class},
					new Class<?>[] {SpatialObjectRecord.class});
			validateBindingMembershipContinuity(mutation, false);
		} else if (kind == SpatialLifecycleOperationKind.BINDING_REROLE) {
			requireExactLifecycleShape(mutation, 1, 1, 2,
					new Class<?>[] {ProjectionBindingRecord.class},
					new Class<?>[] {ProjectionBindingRecord.class},
					new Class<?>[] {SpatialObjectRecord.class,
							GeoIdentityRecord.class});
			validateBindingReroleContinuity(mutation);
		} else if (kind == SpatialLifecycleOperationKind.MAP_ADD) {
			validateMapAddRemoveShape(mutation, true);
		} else if (kind == SpatialLifecycleOperationKind.MAP_REMOVE) {
			validateMapAddRemoveShape(mutation, false);
		} else if (kind == SpatialLifecycleOperationKind.MAP_CHANGE) {
			requireExactLifecycleShape(mutation, 0, 0, 2, new Class<?>[0],
					new Class<?>[0], new Class<?>[] {ProjectionDiagramMapRecord.class,
							ProjectionSystemRecord.class});
			validateMapChangeContinuity(mutation);
		} else if (kind == SpatialLifecycleOperationKind.MAP_REROLE) {
			validateMapReroleShape(mutation);
		} else if (kind == SpatialLifecycleOperationKind.FRAME_CHANGE) {
			requireExactLifecycleShape(mutation, 0, 0, 2, new Class<?>[0],
					new Class<?>[0], new Class<?>[] {ProjectionFrameRecord.class,
							ProjectionSystemRecord.class});
			validateFrameChangeContinuity(mutation);
		} else if (kind == SpatialLifecycleOperationKind.RELATION_ADD) {
			requireExactLifecycleShape(mutation, 1, 0, 3,
					new Class<?>[] {ProjectionFrameRelationRecord.class},
					new Class<?>[0], new Class<?>[] {ProjectionSystemRecord.class,
							ProjectionDiagramMapRecord.class,
							ProjectionDiagramMapRecord.class});
			validateRelationMembershipContinuity(mutation, true);
		} else if (kind == SpatialLifecycleOperationKind.RELATION_REMOVE) {
			requireExactLifecycleShape(mutation, 0, 1, 3, new Class<?>[0],
					new Class<?>[] {ProjectionFrameRelationRecord.class},
					new Class<?>[] {ProjectionSystemRecord.class,
							ProjectionDiagramMapRecord.class,
							ProjectionDiagramMapRecord.class});
			validateRelationMembershipContinuity(mutation, false);
		} else if (kind == SpatialLifecycleOperationKind.RELATION_CHANGE) {
			requireExactLifecycleShape(mutation, 0, 0, 2, new Class<?>[0],
					new Class<?>[0], new Class<?>[] {ProjectionFrameRelationRecord.class,
							ProjectionSystemRecord.class});
			validateRelationChangeContinuity(mutation);
		} else if (kind == SpatialLifecycleOperationKind.RELATION_REROLE) {
			validateRelationReroleShape(mutation);
		} else if (kind == SpatialLifecycleOperationKind.SYSTEM_REPLACEMENT) {
			validateSystemReplacementShape(mutation);
		} else if (kind == SpatialLifecycleOperationKind.TRUE_REPLACEMENT) {
			validateTrueReplacementShape(mutation);
		} else if (kind == SpatialLifecycleOperationKind.REFERENCE_RECOVERY) {
			if (!mutation.isReferenceRecoveryValidated()
					|| mutation.getCreatedRecords().size() != 1
					|| !mutation.getRetiredIds().isEmpty()
					|| !mutation.getReplacementRecords().isEmpty()) {
				throw lifecycleOperationShapeFailure();
			}
		} else if (kind == SpatialLifecycleOperationKind.EXPLICIT_MIGRATION
				|| kind == SpatialLifecycleOperationKind.COMPLETE_CLOSURE_COPY
				|| kind == SpatialLifecycleOperationKind.DECLARED_EXTERNAL_COPY) {
			boolean sealed = kind == SpatialLifecycleOperationKind.EXPLICIT_MIGRATION
					? mutation.isMigrationPlanValidated()
					: mutation.isCopyPlanValidated();
			if (!sealed || mutation.getCreatedRecords().isEmpty()
					|| !mutation.getRetiredIds().isEmpty()
					|| !mutation.getReplacementRecords().isEmpty()) {
				throw lifecycleOperationShapeFailure();
			}
		}
	}

	private void validateBindingMembershipContinuity(
			SpatialLifecycleMutation mutation, boolean add) {
		ProjectionBindingRecord binding = add
				? firstRecord(mutation.getCreatedRecords().values(),
						ProjectionBindingRecord.class)
				: firstRecord(retiredRecords(mutation), ProjectionBindingRecord.class);
		SpatialObjectRecord replacement = firstRecord(
				mutation.getReplacementRecords().values(), SpatialObjectRecord.class);
		SpatialObjectRecord current = (SpatialObjectRecord) records.get(
				replacement.getId());
		if (current == null || !sameObjectDefinition(current, replacement)
				|| !binding.getObjectId().equals(current.getId())
				|| !binding.getSystemId().equals(current.getSystemId())) {
			throw lifecycleOperationShapeFailure();
		}
		ArrayList<ProjectionBindingId> expected = new ArrayList<>(
				add ? replacement.getBindingIds() : current.getBindingIds());
		boolean changed = add ? expected.remove(binding.getId())
				: removeExactlyOnce(expected, binding.getId());
		List<ProjectionBindingId> baseline = add ? current.getBindingIds()
				: replacement.getBindingIds();
		if (!changed || !expected.equals(baseline)) {
			throw lifecycleOperationShapeFailure();
		}
	}

	private void validateBindingReroleContinuity(
			SpatialLifecycleMutation mutation) {
		ProjectionBindingRecord current = firstRecord(retiredRecords(mutation),
				ProjectionBindingRecord.class);
		ProjectionBindingRecord replacement = firstRecord(
				mutation.getCreatedRecords().values(), ProjectionBindingRecord.class);
		SpatialObjectRecord currentObject = (SpatialObjectRecord) records.get(
				current.getObjectId());
		SpatialObjectRecord replacementObject = (SpatialObjectRecord) mutation
				.getReplacementRecords().get(current.getObjectId());
		GeoIdentityRecord currentProjected = getGeoRecord(
				current.getProjectedPointGeoId());
		GeoIdentityRecord replacementProjected = currentProjected == null ? null
				: (GeoIdentityRecord) mutation.getReplacementRecords().get(
						currentProjected.getId());
		boolean continuous = current.getSemanticVersion() == 2
				&& replacement.getSemanticVersion() == 2
				&& !current.getId().equals(replacement.getId())
				&& current.getObjectId().equals(replacement.getObjectId())
				&& current.getSystemId().equals(replacement.getSystemId())
				&& current.getDiagramMapId().equals(replacement.getDiagramMapId())
				&& current.getFrameId().equals(replacement.getFrameId())
				&& current.getRole() != replacement.getRole()
				&& current.getRepresentationType().equals(
						replacement.getRepresentationType())
				&& current.getExpectedSpatialType().equals(
						replacement.getExpectedSpatialType())
				&& current.getSchemaId().equals(replacement.getSchemaId())
				&& current.getSchemaVersion() == replacement.getSchemaVersion()
				&& current.getProjectedPointGeoId().equals(
						replacement.getProjectedPointGeoId())
				&& Objects.equals(current.getFidelity(), replacement.getFidelity())
				&& Objects.equals(current.getCorrespondence(),
						replacement.getCorrespondence())
				&& replacement.getCopySourceId() == null
				&& currentObject != null && replacementObject != null
				&& sameObjectDefinition(currentObject, replacementObject)
				&& replaceIds(currentObject.getBindingIds(), current.getId(),
						replacement.getId()).equals(replacementObject.getBindingIds())
				&& currentProjected != null && replacementProjected != null
				&& currentProjected.getBindingRole() == current.getRole()
				&& replacementProjected.getBindingRole() == replacement.getRole();
		if (!continuous) {
			throw lifecycleOperationShapeFailure();
		}
	}

	private void validateLifecycleAttachmentPolicy(SpatialLifecycleMutation mutation,
			SpatialLifecycleOperationKind kind) {
		boolean providerRedefine = isProviderValidatedRedefine(mutation, kind);
		boolean explicitAttachmentOperation = kind
				== SpatialLifecycleOperationKind.EXPLICIT_MIGRATION
				|| kind == SpatialLifecycleOperationKind.COMPLETE_CLOSURE_COPY
				|| kind == SpatialLifecycleOperationKind.DECLARED_EXTERNAL_COPY
				|| kind == SpatialLifecycleOperationKind.REFERENCE_RECOVERY;
		if (!providerRedefine && !explicitAttachmentOperation
				&& (!mutation.getAttachments().isEmpty()
						|| !mutation.getDetachments().isEmpty())) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.LIFECYCLE_SCOPE_VIOLATION,
					"This lifecycle operation must preserve all geo attachments"));
		}
		if (explicitAttachmentOperation && !mutation.getDetachments().isEmpty()) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.LIFECYCLE_SCOPE_VIOLATION,
					"Migration, copy, and recovery cannot detach participating geos"));
		}
	}

	private void validateMapAddRemoveShape(SpatialLifecycleMutation mutation,
			boolean add) {
		Collection<? extends SpatialIdentityRecord> children = add
				? mutation.getCreatedRecords().values() : retiredRecords(mutation);
		Collection<? extends SpatialIdentityRecord> opposite = add
				? retiredRecords(mutation) : mutation.getCreatedRecords().values();
		if (!opposite.isEmpty() || mutation.getReplacementRecords().size() != 1
				|| countRecords(children, ProjectionDiagramMapRecord.class) != 1
				|| countRecords(children, ProjectionFrameRecord.class) > 1
				|| children.size() < 1 || children.size() > 2
				|| countRecords(mutation.getReplacementRecords().values(),
						ProjectionSystemRecord.class) != 1
				|| !containsOnlyTypes(children, ProjectionDiagramMapRecord.class,
						ProjectionFrameRecord.class)) {
			throw lifecycleOperationShapeFailure();
		}
		ProjectionDiagramMapRecord map = firstRecord(children,
				ProjectionDiagramMapRecord.class);
		ProjectionSystemRecord replacementSystem = firstRecord(
				mutation.getReplacementRecords().values(), ProjectionSystemRecord.class);
		ProjectionSystemRecord currentSystem = (ProjectionSystemRecord) records.get(
				replacementSystem.getId());
		ArrayList<ProjectionDiagramMapId> expectedMaps = new ArrayList<>(
				add ? replacementSystem.getMapIds() : currentSystem.getMapIds());
		boolean membershipChanged = add ? expectedMaps.remove(map.getId())
				: removeExactlyOnce(expectedMaps, map.getId());
		List<ProjectionDiagramMapId> baseline = add ? currentSystem.getMapIds()
				: replacementSystem.getMapIds();
		ProjectionFrameRecord frame = countRecords(children,
				ProjectionFrameRecord.class) == 0 ? null
						: firstRecord(children, ProjectionFrameRecord.class);
		boolean exactFrame = frame == null || map.getFrameId().equals(frame.getId());
		if (frame != null && !add) {
			for (SpatialIdentityRecord record : records.values()) {
				if (record instanceof ProjectionDiagramMapRecord
						&& !record.getId().equals(map.getId())
						&& ((ProjectionDiagramMapRecord) record).getFrameId()
								.equals(frame.getId())) {
					exactFrame = false;
				}
			}
		}
		if (currentSystem == null
				|| !sameSystemDefinition(currentSystem, replacementSystem)
				|| !map.getSystemId().equals(currentSystem.getId())
				|| !membershipChanged || !expectedMaps.equals(baseline)
				|| !currentSystem.getRelationIds().equals(
						replacementSystem.getRelationIds()) || !exactFrame) {
			throw lifecycleOperationShapeFailure();
		}
	}

	private void validateUnchangedSystemMembership(
			SpatialLifecycleMutation mutation) {
		ProjectionSystemRecord replacement = firstRecord(
				mutation.getReplacementRecords().values(), ProjectionSystemRecord.class);
		ProjectionSystemRecord current = (ProjectionSystemRecord) records.get(
				replacement.getId());
		if (current == null || !sameSystemDefinition(current, replacement)
				|| !current.getMapIds().equals(replacement.getMapIds())
				|| !current.getRelationIds().equals(replacement.getRelationIds())) {
			throw lifecycleOperationShapeFailure();
		}
	}

	private void validateMapChangeContinuity(
			SpatialLifecycleMutation mutation) {
		validateUnchangedSystemMembership(mutation);
		ProjectionDiagramMapRecord replacement = firstRecord(
				mutation.getReplacementRecords().values(),
				ProjectionDiagramMapRecord.class);
		ProjectionDiagramMapRecord current = (ProjectionDiagramMapRecord) records.get(
				replacement.getId());
		if (current == null || !current.getRelationIds().equals(
				replacement.getRelationIds())) {
			throw lifecycleOperationShapeFailure();
		}
	}

	private void validateFrameChangeContinuity(
			SpatialLifecycleMutation mutation) {
		validateUnchangedSystemMembership(mutation);
		ProjectionFrameRecord replacementFrame = firstRecord(
				mutation.getReplacementRecords().values(), ProjectionFrameRecord.class);
		ProjectionSystemRecord replacementSystem = firstRecord(
				mutation.getReplacementRecords().values(), ProjectionSystemRecord.class);
		boolean referenced = false;
		for (SpatialIdentityRecord record : records.values()) {
			if (!(record instanceof ProjectionDiagramMapRecord)) {
				continue;
			}
			ProjectionDiagramMapRecord map = (ProjectionDiagramMapRecord) record;
			if (map.getFrameId().equals(replacementFrame.getId())) {
				referenced = true;
				if (!map.getSystemId().equals(replacementSystem.getId())
						|| !replacementSystem.getMapIds().contains(map.getId())) {
					throw lifecycleOperationShapeFailure();
				}
			}
		}
		if (!referenced) {
			throw lifecycleOperationShapeFailure();
		}
	}

	private void validateRelationChangeContinuity(
			SpatialLifecycleMutation mutation) {
		validateUnchangedSystemMembership(mutation);
	}

	private void validateRelationMembershipContinuity(
			SpatialLifecycleMutation mutation, boolean add) {
		ProjectionFrameRelationRecord relation = add
				? firstRecord(mutation.getCreatedRecords().values(),
						ProjectionFrameRelationRecord.class)
				: firstRecord(retiredRecords(mutation),
						ProjectionFrameRelationRecord.class);
		ProjectionSystemRecord replacementSystem = firstRecord(
				mutation.getReplacementRecords().values(), ProjectionSystemRecord.class);
		ProjectionSystemRecord currentSystem = (ProjectionSystemRecord) records.get(
				replacementSystem.getId());
		if (currentSystem == null
				|| !sameSystemDefinition(currentSystem, replacementSystem)
				|| !relation.getSystemId().equals(currentSystem.getId())
				|| !membershipDelta(currentSystem.getRelationIds(),
						replacementSystem.getRelationIds(), relation.getId(), add)
				|| !currentSystem.getMapIds().equals(replacementSystem.getMapIds())) {
			throw lifecycleOperationShapeFailure();
		}
		LinkedHashSet<ProjectionDiagramMapId> endpoints = new LinkedHashSet<>(
				Arrays.asList(relation.getSourceMapId(), relation.getDestinationMapId()));
		for (ProjectionDiagramMapId endpoint : endpoints) {
			ProjectionDiagramMapRecord current =
					(ProjectionDiagramMapRecord) records.get(endpoint);
			ProjectionDiagramMapRecord replacement =
					(ProjectionDiagramMapRecord) mutation.getReplacementRecords().get(endpoint);
			if (current == null || replacement == null
					|| !membershipDelta(current.getRelationIds(),
							replacement.getRelationIds(), relation.getId(), add)
					|| !sameMapDefinition(current, replacement)) {
				throw lifecycleOperationShapeFailure();
			}
		}
	}

	private static boolean sameMapDefinition(ProjectionDiagramMapRecord first,
			ProjectionDiagramMapRecord second) {
		return first.getId().equals(second.getId())
				&& first.getSemanticVersion() == second.getSemanticVersion()
				&& Objects.equals(first.getCopySourceId(), second.getCopySourceId())
				&& first.getDefinitionGeoIds().equals(second.getDefinitionGeoIds())
				&& first.getSystemId().equals(second.getSystemId())
				&& first.getFrameId().equals(second.getFrameId())
				&& first.getFrameUseRole() == second.getFrameUseRole()
				&& first.getFamily().equals(second.getFamily())
				&& Objects.equals(first.getOrientation(), second.getOrientation())
				&& Objects.equals(first.getUnits(), second.getUnits())
				&& Objects.equals(first.getFidelity(), second.getFidelity())
				&& Objects.equals(first.getA00GeoId(), second.getA00GeoId())
				&& Objects.equals(first.getA01GeoId(), second.getA01GeoId())
				&& Objects.equals(first.getA10GeoId(), second.getA10GeoId())
				&& Objects.equals(first.getA11GeoId(), second.getA11GeoId())
				&& Objects.equals(first.getB0GeoId(), second.getB0GeoId())
				&& Objects.equals(first.getB1GeoId(), second.getB1GeoId())
				&& Objects.equals(first.getDeclaredScaleGeoId(),
						second.getDeclaredScaleGeoId());
	}

	private static boolean sameSystemDefinition(ProjectionSystemRecord first,
			ProjectionSystemRecord second) {
		return first.getId().equals(second.getId())
				&& first.getSemanticVersion() == second.getSemanticVersion()
				&& Objects.equals(first.getCopySourceId(), second.getCopySourceId())
				&& first.getDefinitionGeoIds().equals(second.getDefinitionGeoIds())
				&& Objects.equals(first.getUnits(), second.getUnits())
				&& Double.compare(first.getAbsoluteTolerance(),
						second.getAbsoluteTolerance()) == 0
				&& Double.compare(first.getRelativeTolerance(),
						second.getRelativeTolerance()) == 0
				&& Double.compare(first.getRankTolerance(),
						second.getRankTolerance()) == 0
				&& Double.compare(first.getMapTolerance(),
						second.getMapTolerance()) == 0
				&& Double.compare(first.getHingeTolerance(),
						second.getHingeTolerance()) == 0
				&& Double.compare(first.getConditionLimit(),
						second.getConditionLimit()) == 0;
	}

	private static boolean sameObjectDefinition(SpatialObjectRecord first,
			SpatialObjectRecord second) {
		return first.getId().equals(second.getId())
				&& first.getSemanticVersion() == second.getSemanticVersion()
				&& Objects.equals(first.getCopySourceId(), second.getCopySourceId())
				&& first.getDefinitionGeoIds().equals(second.getDefinitionGeoIds())
				&& first.getSpatialType().equals(second.getSpatialType())
				&& first.getAuthority() == second.getAuthority()
				&& first.getSchemaId().equals(second.getSchemaId())
				&& first.getSchemaVersion() == second.getSchemaVersion()
				&& Objects.equals(first.getSystemId(), second.getSystemId())
				&& first.getAssociationProvenance().equals(
						second.getAssociationProvenance());
	}

	private static <T> boolean membershipDelta(List<T> current,
			List<T> replacement, T child, boolean add) {
		ArrayList<T> reduced = new ArrayList<>(add ? replacement : current);
		boolean changed = removeExactlyOnce(reduced, child);
		return changed && reduced.equals(add ? current : replacement);
	}

	private static <T> boolean removeExactlyOnce(List<T> source, T value) {
		int index = source.indexOf(value);
		if (index < 0 || source.lastIndexOf(value) != index) {
			return false;
		}
		source.remove(index);
		return true;
	}

	private void validateMapReroleShape(SpatialLifecycleMutation mutation) {
		Collection<SpatialIdentityRecord> retired = retiredRecords(mutation);
		Collection<SpatialIdentityRecord> created =
				mutation.getCreatedRecords().values();
		Collection<SpatialIdentityRecord> replaced =
				mutation.getReplacementRecords().values();
		if (countRecords(created, ProjectionDiagramMapRecord.class) != 1
				|| countRecords(retired, ProjectionDiagramMapRecord.class) != 1
				|| countRecords(created, ProjectionBindingRecord.class)
						!= countRecords(retired, ProjectionBindingRecord.class)
				|| countRecords(created, ProjectionFrameRelationRecord.class)
						!= countRecords(retired, ProjectionFrameRelationRecord.class)
				|| countRecords(replaced, ProjectionSystemRecord.class) != 1
				|| !containsOnlyTypes(created, ProjectionDiagramMapRecord.class,
						ProjectionBindingRecord.class,
						ProjectionFrameRelationRecord.class)
				|| !containsOnlyTypes(retired, ProjectionDiagramMapRecord.class,
						ProjectionBindingRecord.class,
						ProjectionFrameRelationRecord.class)
				|| !containsOnlyTypes(replaced, ProjectionSystemRecord.class,
						ProjectionDiagramMapRecord.class, SpatialObjectRecord.class)) {
			throw lifecycleOperationShapeFailure();
		}
		ProjectionDiagramMapRecord current = firstRecord(retired,
				ProjectionDiagramMapRecord.class);
		ProjectionDiagramMapRecord replacement = firstRecord(created,
				ProjectionDiagramMapRecord.class);
		if (!current.getSystemId().equals(replacement.getSystemId())
				|| !current.getFrameId().equals(replacement.getFrameId())
				|| current.getFrameUseRole() == replacement.getFrameUseRole()
				|| !current.getFamily().equals(replacement.getFamily())
				|| !Objects.equals(current.getOrientation(), replacement.getOrientation())
				|| !Objects.equals(current.getUnits(), replacement.getUnits())
				|| !Objects.equals(current.getFidelity(), replacement.getFidelity())
				|| !Objects.equals(current.getA00GeoId(), replacement.getA00GeoId())
				|| !Objects.equals(current.getA01GeoId(), replacement.getA01GeoId())
				|| !Objects.equals(current.getA10GeoId(), replacement.getA10GeoId())
				|| !Objects.equals(current.getA11GeoId(), replacement.getA11GeoId())
				|| !Objects.equals(current.getB0GeoId(), replacement.getB0GeoId())
				|| !Objects.equals(current.getB1GeoId(), replacement.getB1GeoId())
				|| !Objects.equals(current.getDeclaredScaleGeoId(),
						replacement.getDeclaredScaleGeoId())
				|| replacement.getCopySourceId() != null
				|| !validateMapReroleClosure(mutation, current, replacement)) {
			throw lifecycleOperationShapeFailure();
		}
	}

	private boolean validateMapReroleClosure(SpatialLifecycleMutation mutation,
			ProjectionDiagramMapRecord currentMap,
			ProjectionDiagramMapRecord replacementMap) {
		LinkedHashMap<ProjectionBindingId, ProjectionBindingId> bindingRemap =
				new LinkedHashMap<>();
		LinkedHashMap<ProjectionFrameRelationId, ProjectionFrameRelationId>
				relationRemap = new LinkedHashMap<>();
		Collection<SpatialIdentityRecord> created =
				mutation.getCreatedRecords().values();
		Collection<SpatialIdentityRecord> retired = retiredRecords(mutation);
		for (SpatialIdentityRecord record : retired) {
			if (record instanceof ProjectionBindingRecord) {
				ProjectionBindingRecord oldBinding = (ProjectionBindingRecord) record;
				ProjectionBindingRecord freshBinding = uniqueRetargetedBinding(
						oldBinding, created, replacementMap.getId());
				if (freshBinding == null
						|| !oldBinding.getDiagramMapId().equals(currentMap.getId())) {
					return false;
				}
				bindingRemap.put(oldBinding.getId(), freshBinding.getId());
			} else if (record instanceof ProjectionFrameRelationRecord) {
				ProjectionFrameRelationRecord oldRelation =
						(ProjectionFrameRelationRecord) record;
				ProjectionFrameRelationRecord freshRelation = uniqueRetargetedRelation(
						oldRelation, created, currentMap.getId(), replacementMap.getId());
				if (freshRelation == null) {
					return false;
				}
				relationRemap.put(oldRelation.getId(), freshRelation.getId());
			}
		}
		if (bindingRemap.size() != countRecords(created,
				ProjectionBindingRecord.class)
				|| relationRemap.size() != countRecords(created,
						ProjectionFrameRelationRecord.class)
				|| !remapList(currentMap.getRelationIds(), relationRemap).equals(
						replacementMap.getRelationIds())) {
			return false;
		}
		LinkedHashSet<SpatialIdentityId> expectedReplacementIds = new LinkedHashSet<>();
		expectedReplacementIds.add(currentMap.getSystemId());
		ProjectionSystemRecord currentSystem = (ProjectionSystemRecord) records.get(
				currentMap.getSystemId());
		ProjectionSystemRecord replacementSystem = (ProjectionSystemRecord) mutation
				.getReplacementRecords().get(currentMap.getSystemId());
		if (currentSystem == null || replacementSystem == null
				|| !sameSystemDefinition(currentSystem, replacementSystem)
				|| !replaceIds(currentSystem.getMapIds(), currentMap.getId(),
						replacementMap.getId()).equals(replacementSystem.getMapIds())
				|| !remapList(currentSystem.getRelationIds(), relationRemap).equals(
						replacementSystem.getRelationIds())) {
			return false;
		}
		for (Map.Entry<ProjectionBindingId, ProjectionBindingId> entry
				: bindingRemap.entrySet()) {
			ProjectionBindingRecord binding = (ProjectionBindingRecord) records.get(
					entry.getKey());
			SpatialObjectRecord currentObject = (SpatialObjectRecord) records.get(
					binding.getObjectId());
			SpatialObjectRecord replacementObject = (SpatialObjectRecord) mutation
					.getReplacementRecords().get(binding.getObjectId());
			if (currentObject == null || replacementObject == null
					|| !sameObjectDefinition(currentObject, replacementObject)
					|| !remapList(currentObject.getBindingIds(), bindingRemap).equals(
							replacementObject.getBindingIds())) {
				return false;
			}
			expectedReplacementIds.add(binding.getObjectId());
		}
		for (ProjectionFrameRelationId relationId : relationRemap.keySet()) {
			ProjectionFrameRelationRecord relation =
					(ProjectionFrameRelationRecord) records.get(relationId);
			for (ProjectionDiagramMapId mapId : Arrays.asList(relation.getSourceMapId(),
					relation.getDestinationMapId())) {
				if (mapId.equals(currentMap.getId())) {
					continue;
				}
				ProjectionDiagramMapRecord peer =
						(ProjectionDiagramMapRecord) records.get(mapId);
				ProjectionDiagramMapRecord replacementPeer =
						(ProjectionDiagramMapRecord) mutation.getReplacementRecords().get(mapId);
				if (peer == null || replacementPeer == null
						|| !sameMapDefinition(peer, replacementPeer)
						|| !remapList(peer.getRelationIds(), relationRemap).equals(
								replacementPeer.getRelationIds())) {
					return false;
				}
				expectedReplacementIds.add(mapId);
			}
		}
		return expectedReplacementIds.equals(
				mutation.getReplacementRecords().keySet());
	}

	private static ProjectionBindingRecord uniqueRetargetedBinding(
			ProjectionBindingRecord current,
			Collection<? extends SpatialIdentityRecord> candidates,
			ProjectionDiagramMapId freshMapId) {
		ProjectionBindingRecord match = null;
		for (SpatialIdentityRecord record : candidates) {
			if (!(record instanceof ProjectionBindingRecord)) {
				continue;
			}
			ProjectionBindingRecord candidate = (ProjectionBindingRecord) record;
			boolean compatible = candidate.getSemanticVersion() == 2
					&& current.getObjectId().equals(candidate.getObjectId())
					&& current.getSystemId().equals(candidate.getSystemId())
					&& freshMapId.equals(candidate.getDiagramMapId())
					&& current.getFrameId().equals(candidate.getFrameId())
					&& current.getRole() == candidate.getRole()
					&& current.getRepresentationType().equals(
							candidate.getRepresentationType())
					&& current.getExpectedSpatialType().equals(
							candidate.getExpectedSpatialType())
					&& current.getSchemaId().equals(candidate.getSchemaId())
					&& current.getSchemaVersion() == candidate.getSchemaVersion()
					&& current.getProjectedPointGeoId().equals(
							candidate.getProjectedPointGeoId())
					&& Objects.equals(current.getFidelity(), candidate.getFidelity())
					&& Objects.equals(current.getCorrespondence(),
							candidate.getCorrespondence())
					&& candidate.getCopySourceId() == null;
			if (compatible) {
				if (match != null) {
					return null;
				}
				match = candidate;
			}
		}
		return match;
	}

	private static ProjectionFrameRelationRecord uniqueRetargetedRelation(
			ProjectionFrameRelationRecord current,
			Collection<? extends SpatialIdentityRecord> candidates,
			ProjectionDiagramMapId currentMapId,
			ProjectionDiagramMapId freshMapId) {
		ProjectionFrameRelationRecord match = null;
		ProjectionDiagramMapId source = current.getSourceMapId().equals(currentMapId)
				? freshMapId : current.getSourceMapId();
		ProjectionDiagramMapId destination = current.getDestinationMapId()
				.equals(currentMapId) ? freshMapId : current.getDestinationMapId();
		for (SpatialIdentityRecord record : candidates) {
			if (!(record instanceof ProjectionFrameRelationRecord)) {
				continue;
			}
			ProjectionFrameRelationRecord candidate =
					(ProjectionFrameRelationRecord) record;
			boolean compatible = candidate.getSemanticVersion() == 2
					&& current.getSystemId().equals(candidate.getSystemId())
					&& source.equals(candidate.getSourceMapId())
					&& destination.equals(candidate.getDestinationMapId())
					&& current.getRelationKind().equals(candidate.getRelationKind())
					&& current.getSupportStartGeoId().equals(
							candidate.getSupportStartGeoId())
					&& current.getSupportEndGeoId().equals(
							candidate.getSupportEndGeoId())
					&& current.getOrientation().equals(candidate.getOrientation())
					&& current.getProvenance().equals(candidate.getProvenance())
					&& Objects.equals(current.getFoldSignGeoId(),
							candidate.getFoldSignGeoId())
					&& candidate.getCopySourceId() == null;
			if (compatible) {
				if (match != null) {
					return null;
				}
				match = candidate;
			}
		}
		return match;
	}

	private void validateRelationReroleShape(SpatialLifecycleMutation mutation) {
		requireExactLifecycleShape(mutation, 1, 1, 3,
				new Class<?>[] {ProjectionFrameRelationRecord.class},
				new Class<?>[] {ProjectionFrameRelationRecord.class},
				new Class<?>[] {ProjectionSystemRecord.class,
						ProjectionDiagramMapRecord.class,
						ProjectionDiagramMapRecord.class});
		ProjectionFrameRelationRecord current = firstRecord(retiredRecords(mutation),
				ProjectionFrameRelationRecord.class);
		ProjectionFrameRelationRecord replacement = firstRecord(
				mutation.getCreatedRecords().values(),
				ProjectionFrameRelationRecord.class);
		boolean roleChanged = !current.getRelationKind().equals(
				replacement.getRelationKind())
				|| !current.getSupportStartGeoId().equals(
						replacement.getSupportStartGeoId())
				|| !current.getSupportEndGeoId().equals(
						replacement.getSupportEndGeoId())
				|| !Objects.equals(current.getFoldSignGeoId(),
						replacement.getFoldSignGeoId())
				|| !current.getOrientation().equals(replacement.getOrientation());
		ProjectionSystemRecord currentSystem = (ProjectionSystemRecord) records.get(
				current.getSystemId());
		ProjectionSystemRecord replacementSystem = (ProjectionSystemRecord) mutation
				.getReplacementRecords().get(current.getSystemId());
		ProjectionDiagramMapRecord currentSource =
				(ProjectionDiagramMapRecord) records.get(current.getSourceMapId());
		ProjectionDiagramMapRecord replacementSource =
				(ProjectionDiagramMapRecord) mutation.getReplacementRecords().get(
						current.getSourceMapId());
		ProjectionDiagramMapRecord currentDestination =
				(ProjectionDiagramMapRecord) records.get(current.getDestinationMapId());
		ProjectionDiagramMapRecord replacementDestination =
				(ProjectionDiagramMapRecord) mutation.getReplacementRecords().get(
						current.getDestinationMapId());
		if (!current.getSystemId().equals(replacement.getSystemId())
				|| !current.getSourceMapId().equals(replacement.getSourceMapId())
				|| !current.getDestinationMapId().equals(
						replacement.getDestinationMapId())
				|| !current.getProvenance().equals(replacement.getProvenance())
				|| replacement.getCopySourceId() != null || !roleChanged
				|| currentSystem == null || replacementSystem == null
				|| !sameSystemDefinition(currentSystem, replacementSystem)
				|| !currentSystem.getMapIds().equals(replacementSystem.getMapIds())
				|| !replaceIds(currentSystem.getRelationIds(), current.getId(),
						replacement.getId()).equals(replacementSystem.getRelationIds())
				|| currentSource == null || replacementSource == null
				|| !sameMapDefinition(currentSource, replacementSource)
				|| !replaceIds(currentSource.getRelationIds(), current.getId(),
						replacement.getId()).equals(replacementSource.getRelationIds())
				|| currentDestination == null || replacementDestination == null
				|| !sameMapDefinition(currentDestination, replacementDestination)
				|| !replaceIds(currentDestination.getRelationIds(), current.getId(),
						replacement.getId()).equals(
							replacementDestination.getRelationIds())) {
			throw lifecycleOperationShapeFailure();
		}
	}

	private static <T extends Comparable<? super T>> List<T> replaceIds(
			List<T> source, T current, T replacement) {
		ArrayList<T> result = new ArrayList<>(source);
		int index = result.indexOf(current);
		if (index < 0) {
			return Collections.emptyList();
		}
		result.set(index, replacement);
		Collections.sort(result);
		return result;
	}

	private static <T extends Comparable<? super T>> List<T> remapList(
			List<T> source, Map<T, T> remap) {
		ArrayList<T> result = new ArrayList<>(source.size());
		for (T id : source) {
			T replacement = remap.get(id);
			result.add(replacement == null ? id : replacement);
		}
		Collections.sort(result);
		return result;
	}

	private void validateSystemReplacementShape(SpatialLifecycleMutation mutation) {
		Collection<SpatialIdentityRecord> created =
				mutation.getCreatedRecords().values();
		Collection<SpatialIdentityRecord> retired = retiredRecords(mutation);
		if (!mutation.getReplacementRecords().isEmpty()
				|| countRecords(created, ProjectionSystemRecord.class) != 1
				|| countRecords(retired, ProjectionSystemRecord.class) != 1
				|| !containsOnlyTypes(created, SpatialObjectRecord.class,
						ProjectionFrameRecord.class, ProjectionSystemRecord.class,
						ProjectionDiagramMapRecord.class,
						ProjectionFrameRelationRecord.class,
						ProjectionBindingRecord.class)
				|| !containsOnlyTypes(retired, SpatialObjectRecord.class,
						ProjectionFrameRecord.class, ProjectionSystemRecord.class,
						ProjectionDiagramMapRecord.class,
						ProjectionFrameRelationRecord.class,
						ProjectionBindingRecord.class)) {
			throw lifecycleOperationShapeFailure();
		}
		ProjectionSystemRecord retiredSystem = firstRecord(retired,
				ProjectionSystemRecord.class);
		ProjectionSystemRecord freshSystem = firstRecord(created,
				ProjectionSystemRecord.class);
		Set<SpatialIdentityId> exactRetired = nonGeoComponent(records,
				retiredSystem.getId());
		if (!exactRetired.equals(mutation.getRetiredIds())) {
			throw lifecycleOperationShapeFailure();
		}
		LinkedHashMap<SpatialIdentityId, SpatialIdentityRecord> createdById =
				new LinkedHashMap<>();
		for (SpatialIdentityRecord record : created) {
			createdById.put(record.getId(), record);
			for (SpatialIdentityId reference : record.getReferences()) {
				if (!createdById.containsKey(reference)) {
					SpatialIdentityRecord retained = records.get(reference);
					if (retained != null && !(retained instanceof GeoIdentityRecord)) {
						throw lifecycleOperationShapeFailure();
					}
				}
			}
		}
		if (!nonGeoComponent(createdById, freshSystem.getId()).equals(
				createdById.keySet())) {
			throw lifecycleOperationShapeFailure();
		}
	}

	private static Set<SpatialIdentityId> nonGeoComponent(
			Map<SpatialIdentityId, SpatialIdentityRecord> graph,
			SpatialIdentityId root) {
		LinkedHashSet<SpatialIdentityId> component = new LinkedHashSet<>();
		component.add(root);
		boolean changed;
		do {
			changed = false;
			for (SpatialIdentityRecord record : graph.values()) {
				if (record instanceof GeoIdentityRecord) {
					continue;
				}
				boolean connected = component.contains(record.getId());
				for (SpatialIdentityId reference : record.getReferences()) {
					connected |= component.contains(reference);
					SpatialIdentityRecord referenced = graph.get(reference);
					if (referenced != null && !(referenced instanceof GeoIdentityRecord)
							&& component.contains(record.getId())) {
						connected = true;
					}
				}
				if (connected && component.add(record.getId())) {
					changed = true;
				}
				if (component.contains(record.getId())) {
					for (SpatialIdentityId reference : record.getReferences()) {
						SpatialIdentityRecord referenced = graph.get(reference);
						if (referenced != null
								&& !(referenced instanceof GeoIdentityRecord)
								&& component.add(reference)) {
							changed = true;
						}
					}
				}
			}
		} while (changed);
		return component;
	}

	private void validateTrueReplacementShape(SpatialLifecycleMutation mutation) {
		if (!mutation.isProviderValidatedRedefine()
				|| !mutation.getReplacementRecords().isEmpty()
				|| countRecords(mutation.getCreatedRecords().values(),
						GeoIdentityRecord.class) == 0
				|| countRecords(retiredRecords(mutation), GeoIdentityRecord.class) == 0
				|| !containsOnlyTypes(mutation.getCreatedRecords().values(),
						GeoIdentityRecord.class)) {
			throw lifecycleOperationShapeFailure();
		}
	}

	private void requireExactLifecycleShape(SpatialLifecycleMutation mutation,
			int createdCount, int retiredCount, int replacementCount,
			Class<?>[] createdTypes, Class<?>[] retiredTypes,
			Class<?>[] replacementTypes) {
		Collection<SpatialIdentityRecord> retired = retiredRecords(mutation);
		if (mutation.getCreatedRecords().size() != createdCount
				|| retired.size() != retiredCount
				|| mutation.getReplacementRecords().size() != replacementCount
				|| !containsExactTypes(mutation.getCreatedRecords().values(), createdTypes)
				|| !containsExactTypes(retired, retiredTypes)
				|| !containsExactTypes(mutation.getReplacementRecords().values(),
						replacementTypes)) {
			throw lifecycleOperationShapeFailure();
		}
	}

	private Collection<SpatialIdentityRecord> retiredRecords(
			SpatialLifecycleMutation mutation) {
		ArrayList<SpatialIdentityRecord> retired = new ArrayList<>();
		for (SpatialIdentityId id : mutation.getRetiredIds()) {
			SpatialIdentityRecord record = records.get(id);
			if (record != null) {
				retired.add(record);
			}
		}
		return retired;
	}

	private static boolean containsExactTypes(
			Collection<? extends SpatialIdentityRecord> records,
			Class<?>[] expectedTypes) {
		ArrayList<Class<?>> unmatched = new ArrayList<>(Arrays.asList(expectedTypes));
		for (SpatialIdentityRecord record : records) {
			int matched = -1;
			for (int index = 0; index < unmatched.size(); index++) {
				if (unmatched.get(index).isInstance(record)) {
					matched = index;
					break;
				}
			}
			if (matched < 0) {
				return false;
			}
			unmatched.remove(matched);
		}
		return unmatched.isEmpty();
	}

	private static boolean containsOnlyTypes(
			Collection<? extends SpatialIdentityRecord> records,
			Class<?>... allowedTypes) {
		for (SpatialIdentityRecord record : records) {
			boolean allowed = false;
			for (Class<?> type : allowedTypes) {
				allowed |= type.isInstance(record);
			}
			if (!allowed) {
				return false;
			}
		}
		return true;
	}

	private static int countRecords(
			Collection<? extends SpatialIdentityRecord> records, Class<?> type) {
		int count = 0;
		for (SpatialIdentityRecord record : records) {
			if (type.isInstance(record)) {
				count++;
			}
		}
		return count;
	}

	private static <T extends SpatialIdentityRecord> T firstRecord(
			Collection<? extends SpatialIdentityRecord> records, Class<T> type) {
		for (SpatialIdentityRecord record : records) {
			if (type.isInstance(record)) {
				return type.cast(record);
			}
		}
		throw new IllegalArgumentException("Missing lifecycle record type "
				+ type.getSimpleName());
	}

	private SpatialIdentityException lifecycleOperationShapeFailure() {
		return failure(SpatialIdentityDiagnostic.of(
				SpatialIdentityDiagnostic.Code.LIFECYCLE_SCOPE_VIOLATION,
				"Lifecycle operation contains an incomplete or extraneous record shape"));
	}

	private void validateSealedProviderRedefineShape(
			SpatialLifecycleMutation mutation) {
		if (!mutation.isProviderValidatedRedefine()
				|| !mutation.getCreatedRecords().isEmpty()
				|| !mutation.getRetiredIds().isEmpty()
				|| !containsOnlyGeoRecords(mutation.getExpectedRecords().values())
				|| !containsOnlyGeoRecords(mutation.getReplacementRecords().values())) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.LIFECYCLE_SCOPE_VIOLATION,
					"Generic lifecycle effects are sealed to provider-validated "
							+ "GeoIdentity redefine transactions"));
		}
	}

	private static boolean containsOnlyGeoRecords(
			Collection<? extends SpatialIdentityRecord> source) {
		for (SpatialIdentityRecord record : source) {
			if (!(record instanceof GeoIdentityRecord)) {
				return false;
			}
		}
		return true;
	}

	private void requireLifecycleTypes(SpatialLifecycleMutation mutation,
			Class<? extends SpatialIdentityRecord> created,
			Class<? extends SpatialIdentityRecord> retired,
			Class<? extends SpatialIdentityRecord> replacement) {
		if (created != null && !containsRecordType(
				mutation.getCreatedRecords().values(), created)
				|| retired != null && !containsRetiredType(mutation, retired)
				|| replacement != null && !containsRecordType(
						mutation.getReplacementRecords().values(), replacement)) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.LIFECYCLE_REVISION_MISMATCH,
					"Lifecycle operation does not contain its required typed records"));
		}
	}

	private boolean containsRetiredType(SpatialLifecycleMutation mutation,
			Class<? extends SpatialIdentityRecord> type) {
		for (SpatialIdentityId id : mutation.getRetiredIds()) {
			if (type.isInstance(records.get(id))) {
				return true;
			}
		}
		return false;
	}

	private static boolean containsRecordType(
			Collection<? extends SpatialIdentityRecord> source,
			Class<? extends SpatialIdentityRecord> type) {
		for (SpatialIdentityRecord record : source) {
			if (type.isInstance(record)) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasZeroRevisions(SpatialIdentityRecord record) {
		if (record instanceof GeoIdentityRecord) {
			GeoIdentityRecord geo = (GeoIdentityRecord) record;
			return geo.getDefinitionRevision() == 0 && geo.getTopologyRevision() == 0;
		}
		if (record instanceof SpatialObjectRecord) {
			SpatialObjectRecord object = (SpatialObjectRecord) record;
			return object.getDefinitionRevision() == 0
					&& object.getTopologyRevision() == 0;
		}
		return childRevision(record) == 0;
	}

	private void validateSameIdentityContract(SpatialLifecycleOperationKind operation,
			SpatialIdentityRecord current, SpatialIdentityRecord replacement) {
		boolean compatible = current.getSemanticVersion()
				== replacement.getSemanticVersion()
				&& Objects.equals(current.getCopySourceId(), replacement.getCopySourceId());
		if (current instanceof GeoIdentityRecord) {
			GeoIdentityRecord first = (GeoIdentityRecord) current;
			GeoIdentityRecord second = (GeoIdentityRecord) replacement;
			if (operation == SpatialLifecycleOperationKind.BINDING_REROLE) {
				compatible &= first.getProvider().equals(second.getProvider())
						&& first.getFamily().equals(second.getFamily())
						&& first.getSchemaId().equals(second.getSchemaId())
						&& first.getSchemaVersion() == second.getSchemaVersion()
						&& first.getAuthority() == second.getAuthority()
						&& first.getBindingRole() != second.getBindingRole()
						&& first.getStableOutputRole().equals(
								second.getStableOutputRole())
						&& first.getOutputCardinality()
								== second.getOutputCardinality();
			} else {
				compatible &= first.toRedefineSignature().isExactlyCompatibleWith(
						second.toRedefineSignature());
			}
		} else if (current instanceof SpatialObjectRecord) {
			SpatialObjectRecord first = (SpatialObjectRecord) current;
			SpatialObjectRecord second = (SpatialObjectRecord) replacement;
			compatible &= first.getSpatialType().equals(second.getSpatialType())
					&& first.getAuthority() == second.getAuthority()
					&& first.getSchemaId().equals(second.getSchemaId())
					&& first.getSchemaVersion() == second.getSchemaVersion()
					&& Objects.equals(first.getSystemId(), second.getSystemId())
					&& first.getAssociationProvenance().equals(
							second.getAssociationProvenance());
		} else if (current instanceof ProjectionBindingRecord) {
			ProjectionBindingRecord first = (ProjectionBindingRecord) current;
			ProjectionBindingRecord second = (ProjectionBindingRecord) replacement;
			compatible &= first.getObjectId().equals(second.getObjectId())
					&& first.getSystemId().equals(second.getSystemId())
					&& first.getDiagramMapId().equals(second.getDiagramMapId())
					&& first.getFrameId().equals(second.getFrameId())
					&& first.getRole() == second.getRole()
					&& first.getRepresentationType().equals(
							second.getRepresentationType())
					&& first.getExpectedSpatialType().equals(
							second.getExpectedSpatialType())
					&& first.getSchemaId().equals(second.getSchemaId())
					&& first.getSchemaVersion() == second.getSchemaVersion()
					&& first.getProjectedPointGeoId().equals(
							second.getProjectedPointGeoId())
					&& Objects.equals(first.getFidelity(), second.getFidelity())
					&& Objects.equals(first.getCorrespondence(),
							second.getCorrespondence());
		} else if (current instanceof ProjectionSystemRecord) {
			ProjectionSystemRecord first = (ProjectionSystemRecord) current;
			ProjectionSystemRecord second = (ProjectionSystemRecord) replacement;
			compatible &= Objects.equals(first.getUnits(), second.getUnits())
					&& Double.compare(first.getAbsoluteTolerance(),
							second.getAbsoluteTolerance()) == 0
					&& Double.compare(first.getRelativeTolerance(),
							second.getRelativeTolerance()) == 0
					&& Double.compare(first.getRankTolerance(),
							second.getRankTolerance()) == 0
					&& Double.compare(first.getMapTolerance(),
							second.getMapTolerance()) == 0
					&& Double.compare(first.getHingeTolerance(),
							second.getHingeTolerance()) == 0
					&& Double.compare(first.getConditionLimit(),
							second.getConditionLimit()) == 0;
		} else if (current instanceof ProjectionDiagramMapRecord) {
			ProjectionDiagramMapRecord first = (ProjectionDiagramMapRecord) current;
			ProjectionDiagramMapRecord second = (ProjectionDiagramMapRecord) replacement;
			compatible &= first.getSystemId().equals(second.getSystemId())
					&& first.getFrameId().equals(second.getFrameId())
					&& first.getFrameUseRole() == second.getFrameUseRole()
					&& first.getFamily().equals(second.getFamily())
					&& Objects.equals(first.getOrientation(), second.getOrientation())
					&& Objects.equals(first.getUnits(), second.getUnits())
					&& Objects.equals(first.getFidelity(), second.getFidelity());
		} else if (current instanceof ProjectionFrameRelationRecord) {
			ProjectionFrameRelationRecord first =
					(ProjectionFrameRelationRecord) current;
			ProjectionFrameRelationRecord second =
					(ProjectionFrameRelationRecord) replacement;
			compatible &= first.getSystemId().equals(second.getSystemId())
					&& first.getSourceMapId().equals(second.getSourceMapId())
					&& first.getDestinationMapId().equals(second.getDestinationMapId())
					&& first.getRelationKind().equals(second.getRelationKind())
					&& Objects.equals(first.getOrientation(), second.getOrientation())
					&& Objects.equals(first.getProvenance(), second.getProvenance());
		} else if (current instanceof ProjectionFrameRecord) {
			ProjectionFrameRecord first = (ProjectionFrameRecord) current;
			ProjectionFrameRecord second = (ProjectionFrameRecord) replacement;
			compatible &= Objects.equals(first.getFamily(), second.getFamily())
					&& Objects.equals(first.getUnits(), second.getUnits())
					&& Objects.equals(first.getHandedness(), second.getHandedness())
					&& Objects.equals(first.getFidelity(), second.getFidelity());
		}
		if (!compatible) {
			throw revisionFailure(replacement.getId(),
					"Same-ID lifecycle replacement changes an identity-defining contract");
		}
	}

	private void validateReplacementRevision(SpatialLifecycleOperationKind operation,
			SpatialIdentityRecord current, SpatialIdentityRecord replacement) {
		if (operation == SpatialLifecycleOperationKind.SEMANTIC_NO_OP) {
			if (!SpatialRecordXmlCodec.writeRecord(current).equals(
					SpatialRecordXmlCodec.writeRecord(replacement))) {
				throw revisionFailure(replacement.getId(),
						"Provider-declared no-op changed its record");
			}
			return;
		}
		if (current instanceof GeoIdentityRecord) {
			GeoIdentityRecord first = (GeoIdentityRecord) current;
			GeoIdentityRecord second = (GeoIdentityRecord) replacement;
			requireRevisionIncrement(first.getDefinitionRevision(),
					second.getDefinitionRevision(), replacement.getId());
			boolean topology = operation
					== SpatialLifecycleOperationKind.ADMITTED_TOPOLOGY_CHANGE
					|| operation == SpatialLifecycleOperationKind.BINDING_REROLE;
			requireTopologyRevision(first.getTopologyRevision(),
					second.getTopologyRevision(), topology, replacement.getId());
			return;
		}
		if (current instanceof SpatialObjectRecord) {
			SpatialObjectRecord first = (SpatialObjectRecord) current;
			SpatialObjectRecord second = (SpatialObjectRecord) replacement;
			requireRevisionIncrement(first.getDefinitionRevision(),
					second.getDefinitionRevision(), replacement.getId());
			boolean topology = operation
					== SpatialLifecycleOperationKind.ADMITTED_TOPOLOGY_CHANGE
					|| operation == SpatialLifecycleOperationKind.BINDING_ADD
					|| operation == SpatialLifecycleOperationKind.BINDING_REMOVE
					|| operation == SpatialLifecycleOperationKind.BINDING_REROLE
					|| !first.getBindingIds().equals(second.getBindingIds());
			requireTopologyRevision(first.getTopologyRevision(),
					second.getTopologyRevision(), topology, replacement.getId());
			return;
		}
		requireRevisionIncrement(childRevision(current), childRevision(replacement),
				replacement.getId());
	}

	private void requireRevisionIncrement(long current, long replacement,
			SpatialIdentityId id) {
		long expected;
		try {
			expected = Math.addExact(current, 1);
		} catch (ArithmeticException exception) {
			throw revisionFailure(id, "Lifecycle revision cannot advance");
		}
		if (replacement != expected) {
			throw revisionFailure(id, "Lifecycle revision must advance exactly once");
		}
	}

	private void requireTopologyRevision(long current, long replacement,
			boolean increment, SpatialIdentityId id) {
		if (increment) {
			requireRevisionIncrement(current, replacement, id);
		} else if (replacement != current) {
			throw revisionFailure(id,
					"Topology revision changed without an admitted topology effect");
		}
	}

	private SpatialIdentityException revisionFailure(SpatialIdentityId id,
			String message) {
		return failure(SpatialIdentityDiagnostic.forSubject(
				SpatialIdentityDiagnostic.Code.LIFECYCLE_REVISION_MISMATCH,
				message, id));
	}

	private static long childRevision(SpatialIdentityRecord record) {
		if (record instanceof ProjectionBindingRecord) {
			return ((ProjectionBindingRecord) record).getRevision();
		}
		if (record instanceof ProjectionSystemRecord) {
			return ((ProjectionSystemRecord) record).getRevision();
		}
		if (record instanceof ProjectionDiagramMapRecord) {
			return ((ProjectionDiagramMapRecord) record).getRevision();
		}
		if (record instanceof ProjectionFrameRelationRecord) {
			return ((ProjectionFrameRelationRecord) record).getRevision();
		}
		if (record instanceof ProjectionFrameRecord) {
			return ((ProjectionFrameRecord) record).getRevision();
		}
		throw new IllegalArgumentException(
				"Record has no single child revision: " + record.getId());
	}

	private static Set<SpatialIdentityId> changedRecordIds(
			Map<SpatialIdentityId, SpatialIdentityRecord> current,
			Map<SpatialIdentityId, SpatialIdentityRecord> prospective) {
		LinkedHashSet<SpatialIdentityId> ids = new LinkedHashSet<>(current.keySet());
		ids.addAll(prospective.keySet());
		LinkedHashSet<SpatialIdentityId> changed = new LinkedHashSet<>();
		for (SpatialIdentityId id : ids) {
			SpatialIdentityRecord first = current.get(id);
			SpatialIdentityRecord second = prospective.get(id);
			if (first == null || second == null
					|| !SpatialRecordXmlCodec.writeRecord(first).equals(
							SpatialRecordXmlCodec.writeRecord(second))) {
				changed.add(id);
			}
		}
		return changed;
	}

	private static Set<SpatialIdentityId> changedResolutionIds(
			Map<SpatialIdentityId, SpatialRecordResolution> current,
			Map<SpatialIdentityId, SpatialRecordResolution> prospective) {
		LinkedHashSet<SpatialIdentityId> ids = new LinkedHashSet<>(current.keySet());
		ids.addAll(prospective.keySet());
		LinkedHashSet<SpatialIdentityId> changed = new LinkedHashSet<>();
		for (SpatialIdentityId id : ids) {
			if (!resolutionKey(current.get(id)).equals(
					resolutionKey(prospective.get(id)))) {
				changed.add(id);
			}
		}
		return changed;
	}

	private static String resolutionKey(SpatialRecordResolution resolution) {
		if (resolution == null) {
			return "ABSENT";
		}
		StringBuilder key = new StringBuilder(resolution.getState().name());
		for (SpatialIdentityDiagnostic diagnostic : resolution.getDiagnostics()) {
			key.append('|').append(diagnostic.toString());
		}
		return key.toString();
	}

	void commitLifecycle(SpatialLifecycleTransaction transaction) {
		requirePreparedLifecycle(transaction);
		if (transaction.getExpectedRegistryEpoch() != mutationEpoch) {
			rollbackLifecycle(transaction);
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.LIFECYCLE_STALE_SOURCE,
					"Registry changed after lifecycle preparation"));
		}
		try {
			beginGraphSwitch();
		} catch (RuntimeException reentrant) {
			rollbackLifecycle(transaction);
			throw reentrant;
		}
		RegistryState before = new RegistryState();
		try {
			installProspectiveGraph(transaction.getProspectiveGraph());
			transaction.getRuntimeSwitch().commit();
		} catch (RuntimeException exception) {
			before.restore();
			RuntimeException rollbackFailure = null;
			try {
				transaction.getRuntimeSwitch().rollback();
			} catch (RuntimeException failedRollback) {
				rollbackFailure = failedRollback;
			}
			releaseLifecycleReservations(transaction.getMutation());
			transaction.markRolledBack();
			instrumentation.recordLifecycleRollback();
			recordOperationRollback(transaction.getMutation().getOperationKind());
			SpatialIdentityException lifecycleFailure = failure(
					SpatialIdentityDiagnostic.of(
							SpatialIdentityDiagnostic.Code.LIFECYCLE_RUNTIME_FAILURE,
							"Lifecycle registry/runtime switch failed atomically"),
					exception);
			if (rollbackFailure != null) {
				lifecycleFailure.addSuppressed(rollbackFailure);
			}
			throw lifecycleFailure;
		} finally {
			endGraphSwitch();
		}
		// A successful runtime switch is terminal: old algorithms may already be
		// retired, so only no-fail state/counter publication follows this point.
		transaction.markCommitted();
		recordLifecycleRevisionCounters(transaction.getMutation());
		instrumentation.recordLifecycleCommit(
				transaction.getMutation().getCreatedRecords().size(),
				transaction.getMutation().getReplacementRecords().size(),
				transaction.getMutation().getRetiredIds().size(),
				transaction.getProspectiveGraph().getChangedResolutionIds().size());
		recordOperationCommit(transaction.getMutation());
		flushRuntimeAnnouncementsWhenUnleased();
		notifyPersistentIdentityAttachments(
				transaction.getMutation().getAttachments());
	}

	void rollbackLifecycle(SpatialLifecycleTransaction transaction) {
		requirePreparedLifecycle(transaction);
		RuntimeException rollbackFailure = null;
		try {
			transaction.getRuntimeSwitch().rollback();
		} catch (RuntimeException exception) {
			rollbackFailure = exception;
		}
		releaseLifecycleReservations(transaction.getMutation());
		transaction.markRolledBack();
		instrumentation.recordLifecycleRollback();
		recordOperationRollback(transaction.getMutation().getOperationKind());
		if (rollbackFailure != null) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.LIFECYCLE_RUNTIME_FAILURE,
					"Prepared lifecycle runtime could not roll back"), rollbackFailure);
		}
	}

	private void requirePreparedLifecycle(SpatialLifecycleTransaction transaction) {
		if (transaction == null || !transaction.isOwnedBy(this)
				|| transaction.getState()
						!= SpatialLifecycleTransaction.State.PREPARED) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"Lifecycle transaction is not prepared by this registry"));
		}
	}

	private SpatialLifecycleProspectiveGraph prospectiveGraphSnapshot(
			SpatialLifecycleOperationKind operationKind, String provenanceToken,
			Map<SpatialIdentityId, SpatialIdentityRecord> prospectiveRecords,
			IdentityHashMap<GeoElement, PersistentGeoId> prospectiveIdsByGeo,
			Map<PersistentGeoId, GeoElement> prospectiveGeosById,
			Collection<SpatialIdentityId> declaredExternalReferenceIds) {
		Map<SpatialIdentityId, SpatialRecordResolution> prospectiveResolutions =
				resolveAll(prospectiveRecords);
		return new SpatialLifecycleProspectiveGraph(operationKind, provenanceToken,
				prospectiveRecords, prospectiveIdsByGeo, prospectiveGeosById,
				prospectiveResolutions, changedRecordIds(records, prospectiveRecords),
				changedResolutionIds(resolutions, prospectiveResolutions),
				declaredExternalReferenceIds);
	}

	private SpatialLifecycleRuntime.PreparedSwitch prepareRuntimeSwitch(
			SpatialLifecycleProspectiveGraph graph) {
		return prepareRuntimeSwitch(graph, false);
	}

	private SpatialLifecycleRuntime.PreparedSwitch prepareRuntimeSwitch(
			SpatialLifecycleProspectiveGraph graph,
			boolean allowSealedProviderPublication) {
		requireGraphSwitchNotInProgress(allowSealedProviderPublication);
		SpatialLifecycleRuntime runtime = lifecycleRuntime;
		if (runtime == null && owner != null) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.LIFECYCLE_RUNTIME_MISSING,
					"A construction-owned graph switch requires its runtime adapter"));
		}
		if (runtime == null) {
			runtime = NO_OP_LIFECYCLE_RUNTIME;
		}
		return Objects.requireNonNull(runtime.prepare(graph),
				"Lifecycle runtime returned no prepared switch");
	}

	private void commitPreparedGraph(SpatialLifecycleProspectiveGraph graph,
			SpatialLifecycleRuntime.PreparedSwitch runtimeSwitch,
			String failureMessage) {
		commitPreparedGraph(graph, runtimeSwitch, failureMessage, false);
	}

	private void commitPreparedGraph(SpatialLifecycleProspectiveGraph graph,
			SpatialLifecycleRuntime.PreparedSwitch runtimeSwitch,
			String failureMessage, boolean allowSealedProviderPublication) {
		beginGraphSwitch(allowSealedProviderPublication);
		RegistryState before = new RegistryState();
		try {
			installProspectiveGraph(graph);
			runtimeSwitch.commit();
		} catch (RuntimeException exception) {
			before.restore();
			RuntimeException rollbackFailure = null;
			try {
				runtimeSwitch.rollback();
			} catch (RuntimeException failedRollback) {
				rollbackFailure = failedRollback;
			}
			SpatialIdentityException atomicFailure = failure(
					SpatialIdentityDiagnostic.of(
							SpatialIdentityDiagnostic.Code.LIFECYCLE_RUNTIME_FAILURE,
							failureMessage), exception);
			if (rollbackFailure != null) {
				atomicFailure.addSuppressed(rollbackFailure);
			}
			throw atomicFailure;
		} finally {
			endGraphSwitch();
		}
	}

	private void beginGraphSwitch() {
		beginGraphSwitch(false);
	}

	private void beginGraphSwitch(boolean allowSealedProviderPublication) {
		requireGraphSwitchNotInProgress(allowSealedProviderPublication);
		graphSwitchInProgress = true;
	}

	private void requireGraphSwitchNotInProgress() {
		requireGraphSwitchNotInProgress(false);
	}

	private void requireGraphSwitchNotInProgress(
			boolean allowSealedProviderPublication) {
		boolean redefinePublicationUnauthorized =
				activeRedefinePublicationLease != null
						&& (redefineGraphPublicationPermitDepth == 0
								|| redefineExternalCallbackDepth > 0);
		boolean sealedProviderPublicationPermitted =
				allowSealedProviderPublication
						&& activeCandidateParticipation != null
						&& activeCandidateParticipation.getState()
								== SpatialRedefineCandidateParticipation.State.SEALED
						&& claimedCandidateParticipations.isEmpty()
						&& activeRedefinePublicationLease == null
						&& redefineExternalCallbackDepth == 0;
		boolean candidatePublicationUnauthorized =
				(activeCandidateParticipation != null
						|| !claimedCandidateParticipations.isEmpty())
						&& !sealedProviderPublicationPermitted
						&& (activeRedefinePublicationLease == null
								|| redefineGraphPublicationPermitDepth == 0
								|| redefineExternalCallbackDepth > 0);
		if (activeRedefinePublicationLease != null
				&& (graphSwitchInProgress || redefinePublicationUnauthorized
						|| candidatePublicationUnauthorized)) {
			activeRedefinePublicationLease.poison();
		}
		if (graphSwitchInProgress || redefinePublicationUnauthorized
				|| candidatePublicationUnauthorized
				|| (activeRedefinePublicationLease == null
						&& !pendingRedefineCompletions.isEmpty())) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"Spatial identity graph publication cannot overlap an active "
							+ "switch or uncompleted redefine"));
		}
	}

	private void endGraphSwitch() {
		graphSwitchInProgress = false;
	}

	private void flushRuntimeAnnouncementsWhenUnleased() {
		if (activeRedefinePublicationLease == null
				&& activeCandidateParticipation == null
				&& claimedCandidateParticipations.isEmpty()
				&& pendingRedefineCompletions.isEmpty() && owner != null) {
			owner.getSpatialSemanticRuntime().flushPendingAnnouncements();
		}
	}

	private void installProspectiveGraph(SpatialLifecycleProspectiveGraph graph) {
		Set<SpatialIdentityId> retired = new LinkedHashSet<>(records.keySet());
		retired.removeAll(graph.copyRecordMap().keySet());
		for (SpatialIdentityId id : retired) {
			retiredTokenIndex.put(id.getRawToken(), id);
		}
		records.clear();
		records.putAll(graph.copyRecordMap());
		idsByGeo.clear();
		idsByGeo.putAll(graph.copyIdsByGeo());
		geosById.clear();
		geosById.putAll(graph.copyGeosById());
		resolutions.clear();
		resolutions.putAll(graph.copyResolutionMap());
		rawTokenIndex.clear();
		for (SpatialIdentityRecord record : records.values()) {
			SpatialIdentityId id = record.getId();
			issuedTokenIndex.put(id.getRawToken(), id);
			rawTokenIndex.put(id.getRawToken(), id);
			reservedTokenIndex.remove(id.getRawToken());
			retiredTokenIndex.remove(id.getRawToken());
		}
		mutationEpoch++;
		graphPublicationEpoch = Math.addExact(graphPublicationEpoch, 1);
	}

	private void releaseLifecycleReservations(SpatialLifecycleMutation mutation) {
		for (SpatialIdentityId id : mutation.getCreatedRecords().keySet()) {
			releaseReservation(id);
		}
	}

	void abandonLifecycleReservations(
			Collection<? extends SpatialIdentityId> ids) {
		for (SpatialIdentityId id : Objects.requireNonNull(ids)) {
			releaseReservation(id);
		}
	}

	private void recordLifecycleRevisionCounters(SpatialLifecycleMutation mutation) {
		for (SpatialIdentityRecord replacement
				: mutation.getReplacementRecords().values()) {
			SpatialIdentityRecord current = mutation.getExpectedRecords().get(
					replacement.getId());
			if (current instanceof GeoIdentityRecord) {
				GeoIdentityRecord first = (GeoIdentityRecord) current;
				GeoIdentityRecord second = (GeoIdentityRecord) replacement;
				if (second.getDefinitionRevision() != first.getDefinitionRevision()) {
					instrumentation.recordDefinitionRevisionChange();
				}
				if (second.getTopologyRevision() != first.getTopologyRevision()) {
					instrumentation.recordTopologyRevisionChange();
				}
			} else if (current instanceof SpatialObjectRecord) {
				SpatialObjectRecord first = (SpatialObjectRecord) current;
				SpatialObjectRecord second = (SpatialObjectRecord) replacement;
				if (second.getDefinitionRevision() != first.getDefinitionRevision()) {
					instrumentation.recordDefinitionRevisionChange();
				}
				if (second.getTopologyRevision() != first.getTopologyRevision()) {
					instrumentation.recordTopologyRevisionChange();
				}
			}
		}
	}

	private void recordOperationCommit(SpatialLifecycleMutation mutation) {
		SpatialLifecycleOperationKind kind = mutation.getOperationKind();
		if (kind == SpatialLifecycleOperationKind.COMPLETE_CLOSURE_COPY
				|| kind == SpatialLifecycleOperationKind.DECLARED_EXTERNAL_COPY) {
			instrumentation.recordCopyCommit();
		}
		if (kind == SpatialLifecycleOperationKind.DECLARED_EXTERNAL_COPY) {
			instrumentation.recordDeclaredExternalCopyCommit(
					mutation.getDeclaredExternalReferenceIds().size());
		} else if (kind == SpatialLifecycleOperationKind.EXPLICIT_MIGRATION) {
			instrumentation.recordExplicitMigrationCommit();
		}
	}

	private void recordOperationRollback(SpatialLifecycleOperationKind kind) {
		if (kind == SpatialLifecycleOperationKind.COMPLETE_CLOSURE_COPY
				|| kind == SpatialLifecycleOperationKind.DECLARED_EXTERNAL_COPY) {
			instrumentation.recordCopyRollback();
		}
		if (kind == SpatialLifecycleOperationKind.DECLARED_EXTERNAL_COPY) {
			instrumentation.recordDeclaredExternalCopyRollback();
		} else if (kind == SpatialLifecycleOperationKind.EXPLICIT_MIGRATION) {
			instrumentation.recordExplicitMigrationRollback();
		}
	}

	private final class RegistryState {
		private final IdentityHashMap<GeoElement, PersistentGeoId> savedIdsByGeo =
				new IdentityHashMap<>(idsByGeo);
		private final Map<PersistentGeoId, GeoElement> savedGeosById =
				new LinkedHashMap<>(geosById);
		private final Map<SpatialIdentityId, SpatialIdentityRecord> savedRecords =
				new LinkedHashMap<>(records);
		private final Map<String, SpatialIdentityId> savedIssuedTokens =
				new LinkedHashMap<>(issuedTokenIndex);
		private final Map<String, SpatialIdentityId> savedRawTokens =
				new LinkedHashMap<>(rawTokenIndex);
		private final Map<String, SpatialIdentityId> savedReservedTokens =
				new LinkedHashMap<>(reservedTokenIndex);
		private final Map<String, SpatialIdentityId> savedRetiredTokens =
				new LinkedHashMap<>(retiredTokenIndex);
		private final Map<SpatialIdentityId, SpatialRecordResolution> savedResolutions =
				new LinkedHashMap<>(resolutions);
		private final long savedEpoch = mutationEpoch;
		private final long savedGraphPublicationEpoch = graphPublicationEpoch;

		private void restore() {
			idsByGeo.clear();
			idsByGeo.putAll(savedIdsByGeo);
			geosById.clear();
			geosById.putAll(savedGeosById);
			records.clear();
			records.putAll(savedRecords);
			issuedTokenIndex.clear();
			issuedTokenIndex.putAll(savedIssuedTokens);
			rawTokenIndex.clear();
			rawTokenIndex.putAll(savedRawTokens);
			reservedTokenIndex.clear();
			reservedTokenIndex.putAll(savedReservedTokens);
			retiredTokenIndex.clear();
			retiredTokenIndex.putAll(savedRetiredTokens);
			resolutions.clear();
			resolutions.putAll(savedResolutions);
			mutationEpoch = savedEpoch;
			graphPublicationEpoch = savedGraphPublicationEpoch;
		}
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
		Map<SpatialIdentityId, SpatialIdentityRecord> prospectiveRecords =
				new LinkedHashMap<>(records);
		IdentityHashMap<GeoElement, PersistentGeoId> prospectiveIdsByGeo =
				new IdentityHashMap<>(idsByGeo);
		Map<PersistentGeoId, GeoElement> prospectiveGeosById =
				new LinkedHashMap<>(geosById);
		for (SpatialIdentityId retiredId : retired) {
			SpatialIdentityRecord removed = prospectiveRecords.remove(retiredId);
			if (removed instanceof GeoIdentityRecord) {
				GeoElement removedGeo = prospectiveGeosById.remove(retiredId);
				if (removedGeo != null) {
					prospectiveIdsByGeo.remove(removedGeo);
				}
			}
		}
		SpatialLifecycleProspectiveGraph graph = prospectiveGraphSnapshot(
				SpatialLifecycleOperationKind.ADMITTED_TOPOLOGY_CHANGE,
				"registry-retirement", prospectiveRecords, prospectiveIdsByGeo,
				prospectiveGeosById, Collections.<SpatialIdentityId>emptySet());
		try {
			SpatialLifecycleRuntime.PreparedSwitch runtimeSwitch =
					prepareRuntimeSwitch(graph);
			commitPreparedGraph(graph, runtimeSwitch,
					"Identity retirement/runtime switch failed atomically");
			instrumentation.recordDeleteCommit();
			flushRuntimeAnnouncementsWhenUnleased();
		} catch (RuntimeException exception) {
			instrumentation.recordDeleteRollback();
			throw exception;
		}
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
		requireGraphSwitchNotInProgress();
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
		mutationEpoch++;
		graphPublicationEpoch = Math.addExact(graphPublicationEpoch, 1);
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
		return beginLoadSession(purpose, XML_VERSION);
	}

	/** @return a two-stage load session that validates the declared section version */
	public LoadSession beginLoadSession(LoadPurpose purpose, int sectionVersion) {
		if (purpose == LoadPurpose.REDEFINE_REBUILD) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"REDEFINE_REBUILD requires an opaque active rebuild token"));
		}
		return new LoadSession(purpose, sectionVersion, null);
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
		IdentityHashMap<GeoElement, PersistentGeoId> entries = new IdentityHashMap<>();
		entries.put(candidate, decidedId);
		return new SerializationOverlay(entries);
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
		ArrayList<SpatialRedefinePersistedOutput> participating = new ArrayList<>();
		for (GeoElement output : hostOutputs(explicitOldTarget)) {
			PersistentGeoId outputId = idsByGeo.get(output);
			if (outputId != null) {
				GeoIdentityRecord outputRecord = getGeoRecord(outputId);
				if (outputRecord == null) {
					throw failure(SpatialIdentityDiagnostic.forSubject(
							SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
							"Participating sibling has no identity record", outputId));
				}
				participating.add(new SpatialRedefinePersistedOutput(output, outputId,
						outputRecord.toRedefineSignature(),
						outputRecord.getDefinitionRevision(),
						outputRecord.getTopologyRevision()));
			}
		}
		SpatialRedefineOutputGroup<SpatialRedefinePersistedOutput> oldOutputs;
		try {
			oldOutputs = SpatialRedefineOutputGroup.of(participating);
		} catch (IllegalArgumentException exception) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.REDEFINE_INCOMPATIBLE,
					"Participating siblings have duplicate or missing stable roles", id),
					exception);
		}
		return new SpatialRedefineContext(oldOutputs,
				record.getStableOutputRole(), hostOutputCount(explicitOldTarget),
				explicitOldTarget.getConstruction().getCurrentUndoXML(false).toString(),
				explicitOldTarget.getConstruction()
						.captureSpatialRedefineHostOperationEpoch(),
				graphPublicationEpoch, runtimePublicationEpoch());
	}

	/**
	 * Rejects a participating redefine that reached a pre-parse authority boundary
	 * without an explicit context, preserving the historical functional counters.
	 *
	 * @param context explicit pre-parse context, required to be non-null
	 */
	public void requireExplicitRedefineContextPresent(
			SpatialRedefineContext context) {
		if (context == null) {
			instrumentation.recordRedefineMissingContext();
			instrumentation.recordRedefineDecision(SpatialRedefineDecision.REJECT);
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
					"Participating redefine requires explicit pre-parse authority"));
		}
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

	/**
	 * Validates the one-shot host rollback boundary without consuming it. This is
	 * used only to preflight an atomic collected rollback.
	 *
	 * @param context pre-operation context
	 */
	public void validateRedefineHostRollback(SpatialRedefineContext context) {
		Objects.requireNonNull(context);
		Construction construction = context.getOldTarget().getConstruction();
		if ((owner != null && owner != construction)
				|| !context.isRollbackAvailable(
						construction.getSpatialRedefineHostOperationEpoch(),
						graphPublicationEpoch, runtimePublicationEpoch())) {
			throw staleHostRollback(context);
		}
	}

	/** Consumes the exact current host rollback boundary once. */
	public void claimRedefineHostRollback(SpatialRedefineContext context) {
		try {
			validateRedefineHostRollback(context);
		} catch (RuntimeException stale) {
			abandonPendingRedefineCompletions(
					Collections.singletonList(context), true);
			throw stale;
		}
		Construction construction = context.getOldTarget().getConstruction();
		if (!context.claimRollback(
				construction.getSpatialRedefineHostOperationEpoch(),
				graphPublicationEpoch, runtimePublicationEpoch())) {
			abandonPendingRedefineCompletions(
					Collections.singletonList(context), true);
			throw staleHostRollback(context);
		}
		pendingRedefineCompletions.remove(context);
	}

	/** @return whether this context still owns the current rollback boundary */
	public boolean isRedefineHostRollbackAvailable(
			SpatialRedefineContext context) {
		if (context == null) {
			return false;
		}
		Construction construction = context.getOldTarget().getConstruction();
		return (owner == null || owner == construction)
				&& context.isRollbackAvailable(
						construction.getSpatialRedefineHostOperationEpoch(),
						graphPublicationEpoch, runtimePublicationEpoch());
	}

	/** Closes a successful host operation so its snapshot cannot be replayed. */
	public void completeRedefineHostOperation(SpatialRedefineContext context) {
		completeRedefineHostOperations(Collections.singletonList(context));
	}

	/** Atomically closes every successful context in one collected host batch. */
	public void completeRedefineHostOperations(
			Collection<SpatialRedefineContext> contexts) {
		Objects.requireNonNull(contexts);
		if (contexts.isEmpty()) {
			throw new IllegalArgumentException(
					"Completed redefine context collection cannot be empty");
		}
		ArrayList<SpatialRedefineContext> validated = new ArrayList<>();
		Set<SpatialRedefineContext> unique = Collections.newSetFromMap(
				new IdentityHashMap<SpatialRedefineContext, Boolean>());
		try {
			for (SpatialRedefineContext context : contexts) {
				SpatialRedefineContext present = Objects.requireNonNull(context);
				if (!unique.add(present)) {
					throw failure(SpatialIdentityDiagnostic.of(
							SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
							"Completed redefine batch repeats one context"));
				}
				validateRedefineHostRollback(present);
				validated.add(present);
			}
		} catch (RuntimeException stale) {
			abandonPendingRedefineCompletions(contexts, true);
			throw stale;
		}
		boolean completesStagedRuntime = owner != null
				&& pendingRedefineCompletions.size() == validated.size()
				&& pendingRedefineCompletions.containsAll(validated);
		if (completesStagedRuntime) {
			owner.getSpatialSemanticRuntime().preflightRedefineCompletion();
		}
		for (SpatialRedefineContext context : validated) {
			context.completeRollbackAuthority();
			pendingRedefineCompletions.remove(context);
		}
		if (completesStagedRuntime) {
			owner.getSpatialSemanticRuntime().commitRedefineCompletion();
		}
		flushRuntimeAnnouncementsWhenUnleased();
	}

	/**
	 * Opens the only scope allowed to advance redefine rollback authority across
	 * this registry's own synchronous graph publication. Every context must own
	 * the current host and graph boundary when the scope begins.
	 *
	 * @param contexts exact redefine operations whose publication is about to run
	 * @return one-shot scope advancing those contexts to the final graph epoch
	 */
	public RedefinePublicationLease beginRedefinePublicationLease(
			Collection<SpatialRedefineContext> contexts) {
		Objects.requireNonNull(contexts);
		if (activeRedefinePublicationLease != null || contexts.isEmpty()) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"A redefine publication lease is active, awaiting completion, "
							+ "or empty"));
		}
		Set<SpatialRedefineContext> unique = Collections.newSetFromMap(
				new IdentityHashMap<SpatialRedefineContext, Boolean>());
		ArrayList<SpatialRedefineContext> validated = new ArrayList<>();
		for (SpatialRedefineContext context : contexts) {
			SpatialRedefineContext present = Objects.requireNonNull(context);
			if (!unique.add(present)) {
				throw failure(SpatialIdentityDiagnostic.of(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"A redefine publication lease repeats one context"));
			}
			if (present.hasAdvancedRollbackPublicationEpoch()) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"A redefine context cannot own a second publication",
						present.getOldId()));
			}
			validateRedefineHostRollback(present);
			validated.add(present);
		}
		if (!unique.containsAll(pendingRedefineCompletions)) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"A redefine publication lease omitted an awaiting batch context"));
		}
		Set<SpatialRedefineContext> candidateContexts = Collections.newSetFromMap(
				new IdentityHashMap<SpatialRedefineContext, Boolean>());
		for (SpatialRedefineCandidateParticipation participation
				: claimedCandidateParticipations) {
			candidateContexts.add(participation.getContext());
		}
		boolean exactCandidateSet = candidateContexts.isEmpty()
				|| (unique.size() == candidateContexts.size()
						&& unique.containsAll(candidateContexts));
		if (activeCandidateParticipation != null
				|| !exactCandidateSet) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"A redefine publication lease does not own the exact staged "
							+ "candidate set"));
		}
		RedefinePublicationLease lease = new RedefinePublicationLease(validated,
				graphPublicationEpoch, runtimePublicationEpoch(),
				runtimePublicationCounts(), redefineRuntimeSubjects(validated));
		activeRedefinePublicationLease = lease;
		return lease;
	}

	/** @return whether the active lexical lease contains this exact context */
	public boolean isRedefinePublicationLeaseActiveFor(
			SpatialRedefineContext context) {
		return activeRedefinePublicationLease != null
				&& activeRedefinePublicationLease.contains(context);
	}

	/**
	 * @return whether semantic evidence belongs to a redefine that can still
	 *         complete or roll back as one host operation
	 */
	public boolean isRedefineRuntimePublicationStaged() {
		return activeRedefinePublicationLease != null
				|| !pendingRedefineCompletions.isEmpty();
	}

	/**
	 * Rejects nested host-context capture before its Construction epoch changes.
	 * A callback cannot stale the exact outer rollback boundary merely by
	 * attempting and catching another participating redefine.
	 */
	public void requireRedefineHostCaptureAllowed() {
		if (activeRedefinePublicationLease != null
				|| activeCandidateParticipation != null
				|| !claimedCandidateParticipations.isEmpty()) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"A nested redefine cannot capture host authority inside an "
							+ "active redefine publication"));
		}
	}

	/**
	 * Allows the host to capture the next explicit target in one already-open
	 * collected redefine batch. Earlier claimed candidates belong to that batch;
	 * lexical publication, candidate parsing and callback scopes remain sealed.
	 */
	public void requireCollectedRedefineHostCaptureAllowed() {
		if (activeRedefinePublicationLease != null
				|| activeCandidateParticipation != null
				|| redefineExternalCallbackDepth > 0) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"A nested redefine cannot capture host authority inside an "
							+ "active collected redefine scope"));
		}
	}

	/**
	 * Seals direct mutable runtime entry points while a provider-owned redefine
	 * is active or awaiting host completion. Atomic prepared switches use the
	 * private lifecycle runtime capability and do not pass through this seam.
	 */
	public void requireDirectSpatialRuntimeMutationAllowed() {
		if (activeRedefinePublicationLease != null) {
			activeRedefinePublicationLease.poison();
		}
		if (activeRedefinePublicationLease != null
				|| activeCandidateParticipation != null
				|| !claimedCandidateParticipations.isEmpty()
				|| !pendingRedefineCompletions.isEmpty()) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"Direct spatial runtime mutation cannot overlap a redefine"));
		}
	}

	/**
	 * Publication boundary used by live G9A2 DAG algorithms. During the host
	 * mutation half of a redefine, affected certificates retain their previous
	 * payload until the sealed registry/runtime switch can publish the final
	 * revision exactly once. A callback-originated publication is never part of
	 * the provider-owned operation.
	 *
	 * @param subject typed system or object publication subject
	 * @return whether the live algorithm must defer this publication
	 */
	public boolean deferAuthoritativeRuntimePublication(SpatialIdentityId subject) {
		Objects.requireNonNull(subject);
		RedefinePublicationLease lease = activeRedefinePublicationLease;
		if (lease == null) {
			if (activeCandidateParticipation != null
					|| !claimedCandidateParticipations.isEmpty()) {
				if (redefineExternalCallbackDepth > 0
						|| !candidateRuntimeSubjects().contains(subject)) {
					throw failure(SpatialIdentityDiagnostic.forSubject(
							SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
							"Unrelated authoritative runtime publication cannot overlap "
									+ "redefine candidate preparation",
							subject));
				}
				return true;
			}
			// Collected host redefines close each synchronous mutation lease while
			// retaining their exact rollback contexts. Keep the semantic DAG sealed
			// across that gap; the final batch graph switch publishes once.
			return !pendingRedefineCompletions.isEmpty();
		}
		if (redefineExternalCallbackDepth > 0) {
			lease.poison();
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"Unsealed callback attempted semantic publication during redefine",
					subject));
		}
		if (redefineGraphPublicationPermitDepth > 0) {
			return false;
		}
		if (!lease.allowsRuntimeSubject(subject)) {
			lease.poison();
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"Unrelated semantic subject attempted publication during redefine",
					subject));
		}
		return true;
	}

	private Set<SpatialIdentityId> candidateRuntimeSubjects() {
		ArrayList<SpatialRedefineContext> contexts = new ArrayList<>();
		if (activeCandidateParticipation != null) {
			contexts.add(activeCandidateParticipation.getContext());
		}
		for (SpatialRedefineCandidateParticipation participation
				: claimedCandidateParticipations) {
			contexts.add(participation.getContext());
		}
		return redefineRuntimeSubjects(contexts);
	}

	/**
	 * Marks a synchronous Kernel-to-View callback. A reentrant listener cannot
	 * inherit either the host lease or its private graph-switch permit.
	 *
	 * @return nesting-safe lexical callback marker
	 */
	public RedefineExternalCallbackScope beginRedefineExternalCallback() {
		boolean active = activeRedefinePublicationLease != null
				|| activeCandidateParticipation != null
				|| !claimedCandidateParticipations.isEmpty();
		if (active) {
			redefineExternalCallbackDepth++;
		}
		return new RedefineExternalCallbackScope(active);
	}

	/** Construction-local, non-authoritative callback marker. */
	public final class RedefineExternalCallbackScope implements AutoCloseable {
		private final boolean active;
		private boolean closed;

		private RedefineExternalCallbackScope(boolean active) {
			this.active = active;
		}

		@Override
		public void close() {
			if (closed) {
				return;
			}
			if (active) {
				if (redefineExternalCallbackDepth <= 0) {
					throw new IllegalStateException(
							"Redefine callback scope underflow");
				}
				redefineExternalCallbackDepth--;
			}
			closed = true;
		}
	}

	/** Closes an owning host lease before an exact rollback snapshot is claimed. */
	public void closeRedefinePublicationLeaseForRollback(
			SpatialRedefineContext context) {
		RedefinePublicationLease lease = activeRedefinePublicationLease;
		if (lease == null) {
			return;
		}
		if (!lease.contains(context)) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"Active redefine lease belongs to another operation"));
		}
		try {
			lease.close();
		} catch (RuntimeException failure) {
			if (!lease.isRollbackReadyAfterCloseFailure()) {
				throw failure;
			}
		}
	}

	/** Synchronous publication capability tied to validated redefine contexts. */
	public final class RedefinePublicationLease implements AutoCloseable {
		private final List<SpatialRedefineContext> contexts;
		private final long entryGraphPublicationEpoch;
		private final long entryRuntimePublicationEpoch;
		private final Map<SpatialIdentityId, Long> entryRuntimePublicationCounts;
		private final Set<SpatialIdentityId> allowedRuntimeSubjects;
		private final Map<SpatialIdentityId, Long> authorizedRuntimePublications =
				new LinkedHashMap<>();
		private boolean rebuildTokenIssued;
		private boolean poisoned;
		private boolean rollbackReadyAfterCloseFailure;
		private boolean closed;

		private RedefinePublicationLease(
				Collection<SpatialRedefineContext> contexts,
				long entryGraphPublicationEpoch,
				long entryRuntimePublicationEpoch,
				Map<SpatialIdentityId, Long> entryRuntimePublicationCounts,
				Set<SpatialIdentityId> allowedRuntimeSubjects) {
			this.contexts = Collections.unmodifiableList(
					new ArrayList<>(contexts));
			this.entryGraphPublicationEpoch = entryGraphPublicationEpoch;
			this.entryRuntimePublicationEpoch = entryRuntimePublicationEpoch;
			this.entryRuntimePublicationCounts = new LinkedHashMap<>(
					entryRuntimePublicationCounts);
			this.allowedRuntimeSubjects = new LinkedHashSet<>(allowedRuntimeSubjects);
		}

		private boolean contains(SpatialRedefineContext context) {
			for (SpatialRedefineContext present : contexts) {
				if (present == context) {
					return true;
				}
			}
			return false;
		}

		private boolean allowsRuntimeSubject(SpatialIdentityId subject) {
			return allowedRuntimeSubjects.contains(subject);
		}

		private void poison() {
			poisoned = true;
		}

		private boolean isRollbackReadyAfterCloseFailure() {
			return rollbackReadyAfterCloseFailure;
		}

		private boolean matchesExactContexts(
				Collection<SpatialRedefineContext> proposed) {
			if (proposed.size() != contexts.size()) {
				return false;
			}
			Set<SpatialRedefineContext> exact = Collections.newSetFromMap(
					new IdentityHashMap<SpatialRedefineContext, Boolean>());
			exact.addAll(contexts);
			for (SpatialRedefineContext context : proposed) {
				if (!exact.remove(context)) {
					return false;
				}
			}
			return exact.isEmpty();
		}

		private boolean hasIssuedRebuildToken() {
			return rebuildTokenIssued;
		}

		private void markRebuildTokenIssued() {
			rebuildTokenIssued = true;
		}

		@Override
		public void close() {
			if (closed) {
				return;
			}
			if (activeRedefinePublicationLease != this) {
				throw failure(SpatialIdentityDiagnostic.of(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"Redefine publication lease is not the active scope"));
			}
			try {
				validateRuntimePublicationSubjects();
				if (redefineGraphPublicationPermitDepth != 0
						|| redefineExternalCallbackDepth != 0
						|| activeRedefineRebuildToken != null) {
					throw failure(SpatialIdentityDiagnostic.of(
							SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
							"Redefine publication scope leaked a nested capability"));
				}
				for (SpatialRedefineContext context : contexts) {
					if (!context.canAdvanceRollbackPublicationEpoch(
							entryGraphPublicationEpoch, graphPublicationEpoch,
							entryRuntimePublicationEpoch, runtimePublicationEpoch())) {
						throw failure(SpatialIdentityDiagnostic.forSubject(
								SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
								"Redefine publication lease became stale",
								context.getOldId()));
					}
				}
				for (SpatialRedefineContext context : contexts) {
					context.advanceRollbackPublicationEpoch(
							entryGraphPublicationEpoch, graphPublicationEpoch,
							entryRuntimePublicationEpoch, runtimePublicationEpoch());
				}
				pendingRedefineCompletions.addAll(contexts);
				rollbackReadyAfterCloseFailure = true;
				if (poisoned) {
					// Every observed delta was proven to belong to this exact sealed
					// operation. Advance its one-shot rollback authority before reporting
					// the caught callback violation so the outer host can restore the
					// operation snapshot. Unscoped deltas fail validation above and never
					// reach this path.
					throw failure(SpatialIdentityDiagnostic.of(
							SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
							"Redefine publication scope was contaminated by a "
									+ "caught reentrant publication attempt"));
				}
			} finally {
				activeRedefinePublicationLease = null;
				closed = true;
			}
		}

		private void validateRuntimePublicationSubjects() {
			Map<SpatialIdentityId, Long> current = runtimePublicationCounts();
			for (Map.Entry<SpatialIdentityId, Long> entry : current.entrySet()) {
				long before = entryRuntimePublicationCounts.getOrDefault(
						entry.getKey(), 0L);
				long authorized = authorizedRuntimePublications.getOrDefault(
						entry.getKey(), 0L);
				long unscoped = entry.getValue() - before - authorized;
				if (unscoped > 0) {
					throw failure(SpatialIdentityDiagnostic.forSubject(
							SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
							"Redefine published outside its sealed graph switch",
							entry.getKey()));
				}
			}
		}
	}

	private final class RedefineGraphPublicationPermit implements AutoCloseable {
		private final RedefinePublicationLease lease;
		private final Map<SpatialIdentityId, Long> entryCounts;
		private boolean closed;

		private RedefineGraphPublicationPermit(RedefinePublicationLease lease) {
			this.lease = lease;
			this.entryCounts = runtimePublicationCounts();
		}

		@Override
		public void close() {
			if (closed) {
				return;
			}
			try {
				Map<SpatialIdentityId, Long> current = runtimePublicationCounts();
				Map<SpatialIdentityId, Long> merged = new LinkedHashMap<>(
						lease.authorizedRuntimePublications);
				for (Map.Entry<SpatialIdentityId, Long> entry : current.entrySet()) {
					long delta = entry.getValue()
							- entryCounts.getOrDefault(entry.getKey(), 0L);
					if (delta > 0) {
						merged.put(entry.getKey(), Math.addExact(
								merged.getOrDefault(entry.getKey(), 0L), delta));
					}
				}
				lease.authorizedRuntimePublications.clear();
				lease.authorizedRuntimePublications.putAll(merged);
			} finally {
				redefineGraphPublicationPermitDepth--;
				closed = true;
			}
		}
	}

	private RedefineGraphPublicationPermit beginRedefineGraphPublicationPermit() {
		if (activeRedefinePublicationLease == null
				|| redefineGraphPublicationPermitDepth != 0) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"Redefine graph publication has no exact lexical permit"));
		}
		redefineGraphPublicationPermitDepth++;
		return new RedefineGraphPublicationPermit(activeRedefinePublicationLease);
	}

	/**
	 * Issues the non-forgeable two-phase authority used by a full XML redefine
	 * rebuild. The token is bound by object identity to the exact active lease.
	 *
	 * @param contexts complete redefine context set for the rebuild
	 * @return opaque phased token for Construction/MyXMLHandler
	 */
	public RedefineRebuildToken beginRedefineRebuild(
			Collection<SpatialRedefineContext> contexts) {
		Objects.requireNonNull(contexts);
		if (activeRedefinePublicationLease == null
				|| !activeRedefinePublicationLease.matchesExactContexts(contexts)
				|| activeRedefineRebuildToken != null
				|| activeRedefinePublicationLease.hasIssuedRebuildToken()) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"Redefine rebuild token does not match the active lease"));
		}
		RedefineRebuildToken token = new RedefineRebuildToken(
				activeRedefinePublicationLease);
		activeRedefinePublicationLease.markRebuildTokenIssued();
		activeRedefineRebuildToken = token;
		return token;
	}

	/** Clears registry/runtime state under the token's first exact graph permit. */
	public void clearForRedefineRebuild(RedefineRebuildToken token) {
		requireActiveRebuildToken(token, RedefineRebuildToken.Phase.ISSUED);
		try (RedefineGraphPublicationPermit ignored =
				beginRedefineGraphPublicationPermit()) {
			SpatialLifecycleProspectiveGraph empty = prospectiveGraphSnapshot(
					SpatialLifecycleOperationKind.ADMITTED_TOPOLOGY_CHANGE,
					"redefine-rebuild-clear",
					Collections.<SpatialIdentityId, SpatialIdentityRecord>emptyMap(),
					new IdentityHashMap<GeoElement, PersistentGeoId>(),
					Collections.<PersistentGeoId, GeoElement>emptyMap(),
					Collections.<SpatialIdentityId>emptySet());
			SpatialLifecycleRuntime.PreparedSwitch runtimeSwitch =
					prepareRuntimeSwitch(empty);
			commitPreparedGraph(empty, runtimeSwitch,
					"Redefine rebuild clear/runtime switch failed atomically");
			token.phase = RedefineRebuildToken.Phase.CLEARED;
		} catch (RuntimeException failure) {
			abortRedefineRebuild(token);
			throw failure;
		}
	}

	/**
	 * Opens the token's sole identity-bearing load session after exact clear.
	 *
	 * @return authorized rebuild load session
	 */
	public LoadSession beginRedefineRebuildLoad(RedefineRebuildToken token,
			int sectionVersion) {
		requireActiveRebuildToken(token, RedefineRebuildToken.Phase.CLEARED);
		token.phase = RedefineRebuildToken.Phase.LOAD_OPEN;
		return new LoadSession(LoadPurpose.REDEFINE_REBUILD, sectionVersion, token);
	}

	/** Abandons an unused or failed rebuild capability without publishing state. */
	public void abortRedefineRebuild(RedefineRebuildToken token) {
		if (token == null || token.phase == RedefineRebuildToken.Phase.COMMITTED
				|| token.phase == RedefineRebuildToken.Phase.ABORTED) {
			return;
		}
		if (activeRedefineRebuildToken != token) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"Redefine rebuild token is not active in this registry"));
		}
		token.phase = RedefineRebuildToken.Phase.ABORTED;
		activeRedefineRebuildToken = null;
	}

	private void requireActiveRebuildToken(RedefineRebuildToken token,
			RedefineRebuildToken.Phase phase) {
		if (token == null || activeRedefineRebuildToken != token
				|| token.lease != activeRedefinePublicationLease
				|| token.phase != phase) {
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"Redefine rebuild token is stale, foreign, or out of phase"));
		}
	}

	/** Opaque registry-issued authority for one clear/load redefine rebuild. */
	public final class RedefineRebuildToken {
		private enum Phase {
			ISSUED, CLEARED, LOAD_OPEN, COMMITTED, ABORTED
		}

		private final RedefinePublicationLease lease;
		private Phase phase = Phase.ISSUED;

		private RedefineRebuildToken(RedefinePublicationLease lease) {
			this.lease = lease;
		}
	}

	private long runtimePublicationEpoch() {
		return owner == null ? 0
				: owner.getSpatialSemanticRuntime().getPublicationEpoch();
	}

	private Map<SpatialIdentityId, Long> runtimePublicationCounts() {
		return owner == null ? Collections.<SpatialIdentityId, Long>emptyMap()
				: owner.getSpatialSemanticRuntime().getInstrumentation()
						.snapshotAuthoritativePublicationCounts();
	}

	private Set<SpatialIdentityId> redefineRuntimeSubjects(
			Collection<SpatialRedefineContext> contexts) {
		LinkedHashSet<SpatialIdentityId> subjects = new LinkedHashSet<>();
		for (SpatialRedefineContext context : contexts) {
			for (SpatialRedefinePersistedOutput output
					: context.getOldOutputs().getOutputs()) {
				for (SpatialIdentityId dependent : dependentClosure(output.getId())) {
					SpatialIdentityRecord record = records.get(dependent);
					if (record instanceof ProjectionSystemRecord
							|| record instanceof SpatialObjectRecord) {
						subjects.add(dependent);
					}
				}
			}
		}
		return Collections.unmodifiableSet(subjects);
	}

	private void abandonPendingRedefineCompletions(
			Collection<SpatialRedefineContext> contexts, boolean flushCurrent) {
		for (SpatialRedefineContext context : contexts) {
			pendingRedefineCompletions.remove(context);
		}
		if (flushCurrent) {
			flushRuntimeAnnouncementsWhenUnleased();
		}
	}

	private SpatialIdentityException staleHostRollback(
			SpatialRedefineContext context) {
		return failure(SpatialIdentityDiagnostic.forSubject(
				SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
				"Spatial redefine rollback context is stale or already consumed",
				context.getOldId()));
	}

	/** @return the provider-inspected decision frozen before host mutation */
	public SpatialRedefineTransaction prepareRedefine(SpatialRedefineContext context,
			SpatialRedefineProposal proposal) {
		return prepareRedefine(context, proposal, null);
	}

	private SpatialRedefineTransaction prepareRedefine(
			SpatialRedefineContext context, SpatialRedefineProposal proposal,
			SpatialRedefineCandidateParticipation participation) {
		Objects.requireNonNull(proposal);
		if (context == null) {
			instrumentation.recordRedefineMissingContext();
			instrumentation.recordRedefineDecision(SpatialRedefineDecision.REJECT);
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
					"A participating redefine cannot retain identity without context"));
		}
		requireCurrentContext(context);
		if (participation != null) {
			candidateGraph(context, participation);
			validateRedefineHostRollback(context);
		}
		SpatialRedefineProvider provider = redefineProviders.get(
				context.getOldSignature().getProvider());
		if (provider == null) {
			instrumentation.recordRedefineDecision(SpatialRedefineDecision.REJECT);
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.REDEFINE_PROVIDER_MISSING,
					"No registered provider can inspect the redefine", context.getOldId()));
		}
		boolean completeGroups = isCompleteOldGroup(context)
				&& isCompleteCandidateGroup(proposal)
				&& hasExactRedefineRoleShape(context, proposal)
				&& groupProvidersMatch(context, proposal, provider.getProviderId());
		SpatialRedefineDecision decision = completeGroups
				? Objects.requireNonNull(provider.inspect(context, proposal))
				: SpatialRedefineDecision.REJECT;
		if (participation != null) {
			validateRedefineHostRollback(context);
		}
		if (!completeGroups) {
			instrumentation.recordRedefineMultiOutputRejection();
		}
		if (decision == SpatialRedefineDecision.RETAIN
				&& proposal.getEffect()
						== SpatialRedefineEffect.ADMITTED_TOPOLOGY_CHANGE
				&& !proposal.isEffectExplicit()) {
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
		validateCandidateParticipationForDecision(context, proposal, decision,
				participation);
		Map<String, PersistentGeoId> decidedIds = allocateRedefineIds(context,
				proposal, decision, participation);
		Set<SpatialIdentityId> retiredIds = redefineRetiredClosure(context, decision);
		instrumentation.recordRedefineDecision(decision);
		SpatialRedefineTransaction transaction = new SpatialRedefineTransaction(this,
				context, proposal, decision, decidedIds, retiredIds, participation);
		if (participation != null) {
			if (activeCandidateParticipation != participation
					|| !claimedCandidateParticipations.add(participation)) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"Redefine candidate participation lost its lexical owner",
						context.getOldId()));
			}
			activeCandidateParticipation = null;
			participation.setState(
					SpatialRedefineCandidateParticipation.State.CLAIMED);
		}
		return transaction;
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
		validateRedefineHostRollback(transaction.getContext());
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
		SpatialRedefineSignature signature;
		boolean providerTopologyPreserving;
		try {
			signature = Objects.requireNonNull(
					provider.describeCandidate(context, candidate));
			providerTopologyPreserving = provider.isTopologyPreserving(context,
					candidate);
		} catch (SpatialIdentityException exception) {
			throw exception;
		} catch (RuntimeException exception) {
			instrumentation.recordRedefineDecision(SpatialRedefineDecision.REJECT);
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.REDEFINE_INCOMPATIBLE,
					"Provider could not describe a compatible redefine candidate",
					context.getOldId()), exception);
		}
		return prepareRedefine(context, new SpatialRedefineProposal(candidate, signature,
				targetedOutputCount,
				topologyPreserving && providerTopologyPreserving,
				replacementOperationSelected));
	}

	/**
	 * Builds a provider-owned stable-role proposal from the complete parsed host
	 * output enumeration. The targeted candidate is explicit; list ordinal is not
	 * used as a semantic role.
	 *
	 * @return provider-inspected group transaction
	 */
	public SpatialRedefineTransaction prepareRedefine(SpatialRedefineContext context,
			GeoElement targetedCandidate, List<GeoElement> candidates,
			boolean replacementOperationSelected) {
		return prepareRedefine(context, targetedCandidate, candidates,
				replacementOperationSelected, null);
	}

	/**
	 * Builds a provider-owned proposal against the explicit staged candidate graph.
	 * The staging object is claimed by the returned transaction only after provider
	 * inspection and ID allocation both succeed.
	 *
	 * @return provider-inspected group transaction
	 */
	public SpatialRedefineTransaction prepareRedefine(SpatialRedefineContext context,
			GeoElement targetedCandidate, List<GeoElement> candidates,
			boolean replacementOperationSelected,
			SpatialRedefineCandidateParticipation participation) {
		if (context == null) {
			instrumentation.recordRedefineMissingContext();
			instrumentation.recordRedefineDecision(SpatialRedefineDecision.REJECT);
			throw failure(SpatialIdentityDiagnostic.of(
					SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
					"A participating redefine cannot retain identity without context"));
		}
		requireCurrentContext(context);
		if (participation != null) {
			validateRedefineHostRollback(context);
		}
		SpatialRedefineProvider provider = redefineProviders.get(
				context.getOldSignature().getProvider());
		if (provider == null) {
			instrumentation.recordRedefineDecision(SpatialRedefineDecision.REJECT);
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.REDEFINE_PROVIDER_MISSING,
					"No registered provider can describe the redefine group",
					context.getOldId()));
		}
		List<GeoElement> enumeration = Collections.unmodifiableList(
				new ArrayList<>(Objects.requireNonNull(candidates)));
		SpatialIdentityGraph candidateGraph = candidateGraph(context, participation);
		SpatialRedefineOutputGroup<SpatialRedefineCandidateOutput> candidateGroup;
		try {
			candidateGroup = Objects.requireNonNull(provider.describeCandidateGroup(
					context, enumeration, candidateGraph));
		} catch (SpatialIdentityException exception) {
			throw exception;
		} catch (RuntimeException exception) {
			instrumentation.recordRedefineDecision(SpatialRedefineDecision.REJECT);
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.REDEFINE_INCOMPATIBLE,
					"Provider could not describe a compatible redefine group",
					context.getOldId()), exception);
		}
		if (!sameGeoEnumeration(enumeration, candidateGroup)) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.REDEFINE_INCOMPATIBLE,
					"Provider candidate roles do not cover the host output group",
					context.getOldId()));
		}
		String targetedRole = roleForGeo(candidateGroup,
				Objects.requireNonNull(targetedCandidate));
		if (targetedRole == null) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.REDEFINE_INCOMPATIBLE,
					"Provider did not map the explicit replacement candidate",
					context.getOldId()));
		}
		SpatialRedefineEffect effect;
		try {
			effect = Objects.requireNonNull(
					provider.describeEffect(context, candidateGroup));
		} catch (SpatialIdentityException exception) {
			throw exception;
		} catch (RuntimeException exception) {
			instrumentation.recordRedefineDecision(SpatialRedefineDecision.REJECT);
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.REDEFINE_INCOMPATIBLE,
					"Provider could not prove the redefine effect",
					context.getOldId()), exception);
		}
		return prepareRedefine(context, new SpatialRedefineProposal(candidateGroup,
				targetedRole, effect, replacementOperationSelected), participation);
	}

	private SpatialIdentityGraph candidateGraph(SpatialRedefineContext context,
			SpatialRedefineCandidateParticipation participation) {
		if (participation == null) {
			return this;
		}
		if (participation != activeCandidateParticipation
				|| participation.getState()
				!= SpatialRedefineCandidateParticipation.State.SEALED
				|| participation.getContext() != context) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"Redefine candidate participation is not sealed for this context",
					context.getOldId()));
		}
		return participation;
	}

	private void validateCandidateParticipationForDecision(
			SpatialRedefineContext context, SpatialRedefineProposal proposal,
			SpatialRedefineDecision decision,
			SpatialRedefineCandidateParticipation participation) {
		if (participation == null) {
			return;
		}
		candidateGraph(context, participation);
		Set<GeoElement> outputs = Collections.newSetFromMap(
				new IdentityHashMap<GeoElement, Boolean>());
		for (SpatialRedefineCandidateOutput output
				: proposal.getCandidateOutputs().getOutputs()) {
			outputs.add(output.getGeo());
		}
		int stagedNonOutputs = 0;
		for (GeoElement staged : participation.copyRecordsByGeo().keySet()) {
			if (!outputs.contains(staged)) {
				stagedNonOutputs++;
			}
		}
		if (decision == SpatialRedefineDecision.RETAIN && stagedNonOutputs != 0) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.REDEFINE_INCOMPATIBLE,
					"Retained redefine cannot publish an uninspected candidate "
							+ "dependency identity",
					context.getOldId()));
		}
	}

	void activateRedefineCandidateParticipation(
			SpatialRedefineTransaction transaction) {
		requireOwnedPrepared(transaction);
		SpatialRedefineCandidateParticipation participation =
				transaction.getCandidateParticipation();
		if (participation == null || participation.areLabelsActivated()) {
			return;
		}
		if (participation.getState()
				!= SpatialRedefineCandidateParticipation.State.CLAIMED
				|| participation.getContext() != transaction.getContext()) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"Redefine candidate participation is not owned by this transaction",
					transaction.getContext().getOldId()));
		}
		if (transaction.getDecision() == SpatialRedefineDecision.REJECT) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.REDEFINE_REJECTED,
					"Rejected redefine cannot activate candidate persistence labels",
					transaction.getContext().getOldId()));
		}
		ArrayList<Map.Entry<GeoElement, GeoIdentityRecord>> staged =
				orderedCandidateParticipations(participation);
		Set<GeoElement> candidateOutputs = Collections.newSetFromMap(
				new IdentityHashMap<GeoElement, Boolean>());
		for (SpatialRedefineCandidateOutput output
				: transaction.getProposal().getCandidateOutputs().getOutputs()) {
			candidateOutputs.add(output.getGeo());
		}
		// From this point rollback must remove even a partially assigned label set.
		participation.markLabelsActivated();
		for (Map.Entry<GeoElement, GeoIdentityRecord> stagedEntry : staged) {
			GeoElement geo = stagedEntry.getKey();
			if (candidateOutputs.contains(geo)) {
				continue;
			}
			if (!owner.isInConstructionList(geo)) {
				ConstructionElement element = geo.isIndependent() ? geo
						: geo.getParentAlgorithm();
				if (element == null) {
					throw failure(SpatialIdentityDiagnostic.forSubject(
							SpatialIdentityDiagnostic.Code.GEO_NOT_SERIALIZABLE,
							"Staged redefine geo has no serializable construction "
									+ "element",
							stagedEntry.getValue().getId()));
				}
				owner.addToConstructionList(element, owner.steps());
			}
			if (!geo.isLabelSet()) {
				geo.setLabel(null);
			}
			if (!geo.isLabelSet()) {
				geo.setLoadedLabel(geo.getFreeLabel(null));
			}
			if (!geo.isLabelSet()) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"Staged redefine participation could not acquire an ordinary "
								+ "persistence label",
						transaction.getContext().getOldId()));
			}
			validateAttachmentGeo(geo,
					participation.getPersistentGeoId(geo));
		}
	}

	private static ArrayList<Map.Entry<GeoElement, GeoIdentityRecord>>
			orderedCandidateParticipations(
					SpatialRedefineCandidateParticipation participation) {
		ArrayList<Map.Entry<GeoElement, GeoIdentityRecord>> remaining =
				new ArrayList<>(participation.copyRecordsByGeo().entrySet());
		Collections.sort(remaining,
				new Comparator<Map.Entry<GeoElement, GeoIdentityRecord>>() {
					@Override
					public int compare(
							Map.Entry<GeoElement, GeoIdentityRecord> first,
							Map.Entry<GeoElement, GeoIdentityRecord> second) {
						int byConstruction = Integer.compare(
								constructionIndex(first.getKey()),
								constructionIndex(second.getKey()));
						return byConstruction != 0 ? byConstruction
								: first.getValue().getId().compareTo(
										second.getValue().getId());
					}
				});
		LinkedHashSet<PersistentGeoId> pending = new LinkedHashSet<>();
		for (Map.Entry<GeoElement, GeoIdentityRecord> entry : remaining) {
			pending.add(entry.getValue().getId());
		}
		ArrayList<Map.Entry<GeoElement, GeoIdentityRecord>> ordered =
				new ArrayList<>();
		while (!remaining.isEmpty()) {
			boolean advanced = false;
			for (int index = 0; index < remaining.size();) {
				Map.Entry<GeoElement, GeoIdentityRecord> entry =
						remaining.get(index);
				boolean ready = true;
				for (PersistentGeoId dependency
						: entry.getValue().getDependencies()) {
					if (pending.contains(dependency)) {
						ready = false;
						break;
					}
				}
				if (!ready) {
					index++;
					continue;
				}
				remaining.remove(index);
				pending.remove(entry.getValue().getId());
				ordered.add(entry);
				advanced = true;
			}
			if (!advanced) {
				throw new IllegalStateException(
						"Redefine candidate participations contain a cycle");
			}
		}
		return ordered;
	}

	private static int constructionIndex(GeoElement geo) {
		ConstructionElement element = geo.isIndependent() ? geo
				: geo.getParentAlgorithm();
		return element == null ? Integer.MAX_VALUE
				: element.getConstructionIndex();
	}

	private void validateStagedRedefineCandidateGeo(GeoElement geo,
			PersistentGeoId id) {
		if (owner == null || geo.getConstruction() != owner
				|| geo.isGeoCasCell() || geo.getCorrespondingCasCell() != null
				|| owner.isConstantElement(geo)) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.GEO_NOT_SERIALIZABLE,
					"Staged redefine geo cannot become an ordinary serializable "
							+ "construction element",
					id));
		}
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
		SpatialRedefineCandidateParticipation participation =
				transaction.getCandidateParticipation();
		if (participation != null && !participation.areLabelsActivated()) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"Candidate participation labels were not activated after provider "
							+ "approval",
					transaction.getContext().getOldId()));
		}
		IdentityHashMap<GeoElement, PersistentGeoId> entries = new IdentityHashMap<>();
		for (SpatialRedefineCandidateOutput output
				: transaction.getProposal().getCandidateOutputs().getOutputs()) {
			PersistentGeoId decided = transaction.getDecidedId(
					output.getStableOutputRole());
			if (decided == null || entries.put(output.getGeo(), decided) != null
					|| serializationOverlay.containsKey(output.getGeo())) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"Redefine serialization group is incomplete or already active",
						transaction.getContext().getOldId()));
			}
		}
		for (Map.Entry<GeoElement, GeoIdentityRecord> staged
				: finalizedStagedNonOutputRecords(transaction).entrySet()) {
			if (entries.put(staged.getKey(), staged.getValue().getId()) != null
					|| serializationOverlay.containsKey(staged.getKey())) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"Staged redefine participation has an ambiguous serialization "
								+ "attachment",
						transaction.getContext().getOldId()));
			}
		}
		serializationOverlay.putAll(entries);
		return new SerializationOverlay(entries);
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
			if (!finalizedStagedNonOutputRecords(transaction).isEmpty()) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"Retained redefine cannot serialize new dependency identities",
						transaction.getContext().getOldId()));
			}
			return writeSpatialSectionForRetainedRedefines(
					Collections.singletonList(transaction));
		}
		requireCurrentFreshClosure(transaction);
		Set<SpatialIdentityId> removed = transaction.getRetiredIds();
		ArrayList<SpatialIdentityRecord> view = new ArrayList<>();
		for (SpatialIdentityRecord record : records.values()) {
			if (!removed.contains(record.getId())) {
				view.add(record);
			}
		}
		for (GeoIdentityRecord record
				: finalizedFreshCandidateRecords(transaction).values()) {
			view.add(record);
		}
		transaction.markRebuildViewWritten();
		return SpatialRecordXmlCodec.writeSection(view);
	}

	/**
	 * Builds one atomic post-decision record view for a collected retained
	 * redefine. The load publishes this combined view once; individual
	 * transaction commits then only validate the rebuilt result.
	 *
	 * @param transactions complete collected retained transaction set
	 * @return deterministic spatial section carrying every decided revision
	 */
	public String writeSpatialSectionForRetainedRedefines(
			Collection<SpatialRedefineTransaction> transactions) {
		Objects.requireNonNull(transactions);
		if (transactions.isEmpty()) {
			throw new IllegalArgumentException(
					"Retained redefine transaction set cannot be empty");
		}
		Map<SpatialIdentityId, GeoIdentityRecord> replacements = new LinkedHashMap<>();
		for (SpatialRedefineTransaction transaction : transactions) {
			requireOwnedPrepared(transaction);
			requireCurrentContext(transaction.getContext());
			if (transaction.getDecision() != SpatialRedefineDecision.RETAIN) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"Collected redefine serialization admits retained decisions only",
						transaction.getContext().getOldId()));
			}
			if (!finalizedStagedNonOutputRecords(transaction).isEmpty()) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"Collected retained redefine contains staged dependency "
								+ "participation",
						transaction.getContext().getOldId()));
			}
			for (SpatialRedefinePersistedOutput output
					: transaction.getContext().getOldOutputs().getOutputs()) {
				GeoIdentityRecord current = getGeoRecord(output.getId());
				requireCurrentRetainedOutput(output, current,
						geosById.get(output.getId()), output.getGeo());
				GeoIdentityRecord replacement = redefineRevision(current,
						transaction.getProposal().getEffect());
				if (replacements.put(output.getId(), replacement) != null) {
					throw failure(SpatialIdentityDiagnostic.forSubject(
							SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
							"Collected redefine targets one stable output more than once",
							output.getId()));
				}
			}
			transaction.markRebuildViewWritten();
		}
		ArrayList<SpatialIdentityRecord> view = new ArrayList<>();
		for (SpatialIdentityRecord record : records.values()) {
			SpatialIdentityRecord replacement = replacements.get(record.getId());
			view.add(replacement == null ? record : replacement);
		}
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
		Map<String, GeoElement> actualOutputs = resolveActualRedefineOutputs(
				transaction, actualResult);
		boolean rebuilt = isRebuiltRedefineGroup(transaction, actualOutputs);
		if (rebuilt && !transaction.isRebuildViewWritten()) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"Rebuilt redefine has no validated serialization view",
					transaction.getContext().getOldId()));
		}
		if (!rebuilt) {
			requireCurrentContext(transaction.getContext());
		}
		if (rebuilt) {
			if (transaction.getDecision() == SpatialRedefineDecision.RETAIN) {
				validateRebuiltRetainedRedefine(transaction, actualOutputs);
			} else {
				validateRebuiltFreshRedefine(transaction, actualOutputs);
			}
		} else if (isRedefinePublicationLeaseActiveFor(
				transaction.getContext())) {
			try (RedefineGraphPublicationPermit ignored =
					beginRedefineGraphPublicationPermit()) {
				if (transaction.getDecision() == SpatialRedefineDecision.RETAIN) {
					commitRetainedRedefine(transaction, actualOutputs);
				} else {
					commitFreshRedefine(transaction, actualOutputs);
				}
			}
		} else {
			try (RedefinePublicationLease ignored =
					beginRedefinePublicationLease(Collections.singletonList(
							transaction.getContext()));
					RedefineGraphPublicationPermit permit =
							beginRedefineGraphPublicationPermit()) {
				if (transaction.getDecision() == SpatialRedefineDecision.RETAIN) {
					commitRetainedRedefine(transaction, actualOutputs);
				} else {
					commitFreshRedefine(transaction, actualOutputs);
				}
			}
		}
		completeRedefineCandidateParticipation(transaction, actualOutputs);
		transaction.markCommitted();
		instrumentation.recordRedefineCommit();
	}

	private void commitRetainedRedefine(SpatialRedefineTransaction transaction,
			Map<String, GeoElement> actualOutputs) {
		if (!finalizedStagedNonOutputRecords(transaction).isEmpty()) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"Retained redefine contains unpublished dependency identities",
					transaction.getContext().getOldId()));
		}
		SpatialRedefineEffect effect = transaction.getProposal().getEffect();
		SpatialLifecycleMutation.Builder mutation = SpatialLifecycleMutation.builder(
				lifecycleKind(effect), "explicit-spatial-redefine:" + effect.name())
				.providerValidatedRedefine();
		boolean graphChange = false;
		for (SpatialRedefinePersistedOutput oldOutput
				: transaction.getContext().getOldOutputs().getOutputs()) {
			String role = oldOutput.getStableOutputRole();
			GeoIdentityRecord current = getGeoRecord(oldOutput.getId());
			GeoElement currentGeo = geosById.get(oldOutput.getId());
			GeoElement actual = actualOutputs.get(role);
			requireCurrentRetainedOutput(oldOutput, current, currentGeo, actual);
			GeoIdentityRecord replacement = redefineRevision(current, effect);
			if (replacement == current) {
				mutation.expect(current);
			} else {
				mutation.replace(current, replacement);
				graphChange = true;
			}
			if (currentGeo != actual) {
				mutation.detach(currentGeo, oldOutput.getId());
				mutation.attach(actual, oldOutput.getId());
				graphChange = true;
			}
		}
		if (graphChange || effect == SpatialRedefineEffect.NO_OP) {
			SpatialLifecycleTransaction lifecycle = prepareLifecycleMutation(
					mutation.build());
			lifecycle.commit();
		}
	}

	private void commitFreshRedefine(SpatialRedefineTransaction transaction,
			Map<String, GeoElement> actualOutputs) {
		requireCurrentFreshClosure(transaction);
		SpatialLifecycleMutation.Builder mutation = SpatialLifecycleMutation.builder(
				SpatialLifecycleOperationKind.TRUE_REPLACEMENT,
				"explicit-spatial-replacement").providerValidatedRedefine();
		for (SpatialIdentityId retiredId : transaction.getRetiredIds()) {
			SpatialIdentityRecord retired = records.get(retiredId);
			if (retired == null) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
						"Fresh redefine retirement closure is no longer current",
						retiredId));
			}
			if (retired instanceof GeoIdentityRecord) {
				GeoElement retiredGeo = geosById.get(retiredId);
				if (retiredGeo == null) {
					throw failure(SpatialIdentityDiagnostic.forSubject(
							SpatialIdentityDiagnostic.Code.GEO_ATTACHMENT_MISSING,
							"Fresh redefine geo closure lost its attachment", retiredId));
				}
				if (!isSameHandleFreshOutput(transaction, actualOutputs,
						(PersistentGeoId) retiredId, retiredGeo)) {
					mutation.detach(retiredGeo, (PersistentGeoId) retiredId);
				}
			}
			mutation.retire(retired);
		}
		for (Map.Entry<GeoElement, GeoIdentityRecord> staged
				: finalizedStagedNonOutputRecords(transaction).entrySet()) {
			mutation.create(staged.getValue());
			mutation.attach(staged.getKey(), staged.getValue().getId());
		}
		for (SpatialRedefineCandidateOutput output
				: transaction.getProposal().getCandidateOutputs().getOutputs()) {
			PersistentGeoId id = transaction.getDecidedId(
					output.getStableOutputRole());
			GeoElement actual = actualOutputs.get(output.getStableOutputRole());
			GeoIdentityRecord record = candidateRecord(id,
					finalizedCandidateSignature(transaction, output));
			mutation.create(record);
			SpatialRedefinePersistedOutput old = transaction.getContext()
					.getOldOutputs().get(output.getStableOutputRole());
			if (old != null && old.getGeo() == actual) {
				mutation.reattach(actual, old.getId(), id);
			} else {
				mutation.attach(actual, id);
			}
		}
		SpatialLifecycleTransaction lifecycle = prepareLifecycleMutation(
				mutation.build());
		lifecycle.commit();
	}

	private static boolean isSameHandleFreshOutput(
			SpatialRedefineTransaction transaction,
			Map<String, GeoElement> actualOutputs, PersistentGeoId retiredId,
			GeoElement retiredGeo) {
		for (SpatialRedefinePersistedOutput old
				: transaction.getContext().getOldOutputs().getOutputs()) {
			if (old.getId().equals(retiredId)
					&& old.getGeo() == retiredGeo
					&& actualOutputs.get(old.getStableOutputRole()) == retiredGeo) {
				return true;
			}
		}
		return false;
	}

	private void validateRebuiltFreshRedefine(
			SpatialRedefineTransaction transaction,
			Map<String, GeoElement> actualOutputs) {
		for (SpatialIdentityId retiredId : transaction.getRetiredIds()) {
			if (records.containsKey(retiredId)) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"Rebuilt fresh redefine retained an old closure identity",
						retiredId));
			}
			retiredTokenIndex.put(retiredId.getRawToken(), retiredId);
		}
		for (SpatialRedefineCandidateOutput output
				: transaction.getProposal().getCandidateOutputs().getOutputs()) {
			PersistentGeoId id = transaction.getDecidedId(
					output.getStableOutputRole());
			GeoIdentityRecord expected = candidateRecord(id,
					finalizedCandidateSignature(transaction, output));
			GeoIdentityRecord actualRecord = getGeoRecord(id);
			if (actualRecord == null || geosById.get(id)
					!= actualOutputs.get(output.getStableOutputRole())
					|| !expected.toRedefineSignature().isExactlyCompatibleWith(
							actualRecord.toRedefineSignature())
					|| actualRecord.getDefinitionRevision() != 0
					|| actualRecord.getTopologyRevision() != 0) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"Rebuilt fresh redefine does not match its sealed role view",
						id));
			}
		}
		for (GeoIdentityRecord expected
				: finalizedStagedNonOutputRecords(transaction).values()) {
			GeoIdentityRecord actual = getGeoRecord(expected.getId());
			if (actual == null || geosById.get(expected.getId()) == null
					|| !SpatialRecordXmlCodec.writeRecord(expected).equals(
							SpatialRecordXmlCodec.writeRecord(actual))) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"Rebuilt fresh redefine omitted a sealed staged dependency",
						expected.getId()));
			}
		}
	}

	private Map<String, GeoElement> resolveActualRedefineOutputs(
			SpatialRedefineTransaction transaction, GeoElement targetedResult) {
		LinkedHashMap<String, GeoElement> actual = new LinkedHashMap<>();
		Set<GeoElement> used = Collections.newSetFromMap(
				new IdentityHashMap<GeoElement, Boolean>());
		String targetedRole = transaction.getProposal()
				.getTargetedStableOutputRole();
		PersistentGeoId targetedId = transaction.getDecidedId(targetedRole);
		SpatialRedefinePersistedOutput oldTarget = transaction.getContext()
				.getOldOutputs().get(targetedRole);
		boolean publishedRebuild = transaction.isRebuildViewWritten()
				&& targetedId != null && targetedResult != null
				&& targetedId.equals(idsByGeo.get(targetedResult))
				&& geosById.get(targetedId) == targetedResult
				&& (oldTarget == null || oldTarget.getGeo() != targetedResult);
		SpatialRedefineCandidateOutput proposedTarget = transaction.getProposal()
				.getCandidateOutputs().get(targetedRole);
		if (targetedResult == null || proposedTarget == null
				|| (!publishedRebuild && targetedResult != proposedTarget.getGeo()
						&& (oldTarget == null
								|| targetedResult != oldTarget.getGeo()))) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"Redefine result is not the provider-mapped targeted output",
					transaction.getContext().getOldId()));
		}
		for (SpatialRedefineCandidateOutput candidate
				: transaction.getProposal().getCandidateOutputs().getOutputs()) {
			String role = candidate.getStableOutputRole();
			PersistentGeoId decided = transaction.getDecidedId(role);
			GeoElement resolved;
			if (role.equals(targetedRole)) {
				resolved = targetedResult;
			} else if (publishedRebuild) {
				resolved = geosById.get(decided);
			} else if (oldTarget != null
					&& oldTarget.getGeo() == targetedResult) {
				// Same-definition/in-place/soft host routes retain their live parent
				// outputs. The parsed candidate siblings are not installed.
				SpatialRedefinePersistedOutput old = transaction.getContext()
						.getOldOutputs().get(role);
				resolved = old == null ? null : old.getGeo();
			} else {
				// Provider-owned stable roles, never host ordinal or the old live
				// attachment, select every sibling in a live grouped redefine.
				resolved = candidate.getGeo();
			}
			if (decided == null || resolved == null || !used.add(resolved)) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"Redefine actual outputs are incomplete or ambiguous",
						transaction.getContext().getOldId()));
			}
			validateAttachmentGeo(resolved, decided);
			PersistentGeoId existing = idsByGeo.get(resolved);
			SpatialRedefinePersistedOutput old =
					transaction.getContext().getOldOutputs().get(role);
			if (existing != null && !decided.equals(existing)
					&& (old == null || !old.getId().equals(existing))) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.GEO_ALREADY_PARTICIPATING,
						"Redefine result already has another durable identity",
						existing));
			}
			actual.put(role, resolved);
		}
		return Collections.unmodifiableMap(actual);
	}

	private boolean isRebuiltRedefineGroup(SpatialRedefineTransaction transaction,
			Map<String, GeoElement> actualOutputs) {
		boolean replaced = false;
		for (Map.Entry<String, GeoElement> entry : actualOutputs.entrySet()) {
			PersistentGeoId decided = transaction.getDecidedId(entry.getKey());
			if (!decided.equals(idsByGeo.get(entry.getValue()))
					|| geosById.get(decided) != entry.getValue()) {
				return false;
			}
			SpatialRedefinePersistedOutput old =
					transaction.getContext().getOldOutputs().get(entry.getKey());
			replaced = replaced || old == null || old.getGeo() != entry.getValue();
		}
		return replaced;
	}

	private void validateRebuiltRetainedRedefine(
			SpatialRedefineTransaction transaction,
			Map<String, GeoElement> actualOutputs) {
		SpatialRedefineEffect effect = transaction.getProposal().getEffect();
		for (SpatialRedefinePersistedOutput old
				: transaction.getContext().getOldOutputs().getOutputs()) {
			GeoElement actual = actualOutputs.get(old.getStableOutputRole());
			GeoIdentityRecord current = getGeoRecord(old.getId());
			if (actual == null || geosById.get(old.getId()) != actual
					|| !old.getId().equals(idsByGeo.get(actual)) || current == null
					|| !old.getSignature().isExactlyCompatibleWith(
							current.toRedefineSignature())) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"Rebuilt retained redefine did not restore its stable-role output",
						old.getId()));
			}
			long expectedDefinition = old.getDefinitionRevision();
			long expectedTopology = old.getTopologyRevision();
			try {
				if (effect != SpatialRedefineEffect.NO_OP) {
					expectedDefinition = Math.addExact(expectedDefinition, 1);
				}
				if (effect == SpatialRedefineEffect.ADMITTED_TOPOLOGY_CHANGE) {
					expectedTopology = Math.addExact(expectedTopology, 1);
				}
			} catch (ArithmeticException exception) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"Definition revision cannot advance", old.getId()),
						exception);
			}
			if (current.getDefinitionRevision() != expectedDefinition
					|| current.getTopologyRevision() != expectedTopology) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"Rebuilt retained redefine published an unexpected revision",
						old.getId()));
			}
		}
	}

	private void requireCurrentRetainedOutput(
			SpatialRedefinePersistedOutput expected, GeoIdentityRecord current,
			GeoElement currentGeo, GeoElement actual) {
		PersistentGeoId actualId = idsByGeo.get(actual);
		if (current == null || currentGeo == null
				|| !expected.getSignature().isExactlyCompatibleWith(
						current.toRedefineSignature())
				|| expected.getDefinitionRevision()
						!= current.getDefinitionRevision()
				|| expected.getTopologyRevision() != current.getTopologyRevision()
				|| (currentGeo != expected.getGeo() && currentGeo != actual)
				|| (actualId != null && !expected.getId().equals(actualId))) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
					"Retained stable-role output changed after inspection",
					expected.getId()));
		}
	}

	private GeoIdentityRecord redefineRevision(GeoIdentityRecord current,
			SpatialRedefineEffect effect) {
		if (effect == SpatialRedefineEffect.NO_OP) {
			return current;
		}
		try {
			long definition = Math.addExact(current.getDefinitionRevision(), 1);
			long topology = effect
					== SpatialRedefineEffect.ADMITTED_TOPOLOGY_CHANGE
							? Math.addExact(current.getTopologyRevision(), 1)
							: current.getTopologyRevision();
			return current.withRevisions(definition, topology);
		} catch (ArithmeticException exception) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					"Definition revision cannot advance", current.getId()), exception);
		}
	}

	private static SpatialLifecycleOperationKind lifecycleKind(
			SpatialRedefineEffect effect) {
		switch (effect) {
		case NO_OP:
			return SpatialLifecycleOperationKind.SEMANTIC_NO_OP;
		case DEFINITION_CHANGE:
			return SpatialLifecycleOperationKind.COMPATIBLE_DEFINITION_CHANGE;
		case ADMITTED_TOPOLOGY_CHANGE:
			return SpatialLifecycleOperationKind.ADMITTED_TOPOLOGY_CHANGE;
		default:
			throw new IllegalArgumentException("Unsupported redefine effect: " + effect);
		}
	}

	private static GeoIdentityRecord candidateRecord(PersistentGeoId id,
			SpatialRedefineSignature signature) {
		return new GeoIdentityRecord(id, signature.getProvider(),
				signature.getFamily(), signature.getSchemaId(),
				signature.getSchemaVersion(), signature.getAuthority(),
				signature.getBindingRole(), signature.getStableOutputRole(),
				signature.getOutputCardinality(), signature.getDependencies(), 0, 0);
	}

	private IdentityHashMap<GeoElement, GeoIdentityRecord>
			finalizedStagedNonOutputRecords(
					SpatialRedefineTransaction transaction) {
		IdentityHashMap<GeoElement, GeoIdentityRecord> result =
				new IdentityHashMap<>();
		SpatialRedefineCandidateParticipation participation =
				transaction.getCandidateParticipation();
		if (participation == null) {
			return result;
		}
		Set<GeoElement> outputs = Collections.newSetFromMap(
				new IdentityHashMap<GeoElement, Boolean>());
		for (SpatialRedefineCandidateOutput output
				: transaction.getProposal().getCandidateOutputs().getOutputs()) {
			outputs.add(output.getGeo());
		}
		Map<SpatialIdentityId, SpatialIdentityId> remap =
				candidateOutputIdentityRemap(transaction);
		for (Map.Entry<GeoElement, GeoIdentityRecord> staged
				: participation.copyRecordsByGeo().entrySet()) {
			if (!outputs.contains(staged.getKey())) {
				result.put(staged.getKey(), staged.getValue().remap(remap, false));
			}
		}
		return result;
	}

	private Map<SpatialIdentityId, SpatialIdentityId> candidateOutputIdentityRemap(
			SpatialRedefineTransaction transaction) {
		LinkedHashMap<SpatialIdentityId, SpatialIdentityId> remap =
				new LinkedHashMap<>();
		SpatialRedefineCandidateParticipation participation =
				transaction.getCandidateParticipation();
		if (participation == null) {
			return remap;
		}
		IdentityHashMap<GeoElement, GeoIdentityRecord> staged =
				participation.copyRecordsByGeo();
		for (SpatialRedefineCandidateOutput output
				: transaction.getProposal().getCandidateOutputs().getOutputs()) {
			GeoIdentityRecord record = staged.get(output.getGeo());
			PersistentGeoId decided = transaction.getDecidedId(
					output.getStableOutputRole());
			if (record != null && decided != null
					&& !record.getId().equals(decided)) {
				remap.put(record.getId(), decided);
			}
		}
		return remap;
	}

	private SpatialRedefineSignature finalizedCandidateSignature(
			SpatialRedefineTransaction transaction,
			SpatialRedefineCandidateOutput output) {
		SpatialRedefineSignature signature = output.getSignature();
		Map<SpatialIdentityId, SpatialIdentityId> remap =
				candidateOutputIdentityRemap(transaction);
		ArrayList<PersistentGeoId> dependencies = new ArrayList<>();
		for (PersistentGeoId dependency : signature.getDependencies()) {
			SpatialIdentityId mapped = remap.get(dependency);
			dependencies.add(mapped == null ? dependency : (PersistentGeoId) mapped);
		}
		Collections.sort(dependencies);
		return new SpatialRedefineSignature(signature.getProvider(),
				signature.getFamily(), signature.getSchemaId(),
				signature.getSchemaVersion(), signature.getAuthority(),
				signature.getBindingRole(), signature.getStableOutputRole(),
				signature.getOutputCardinality(), dependencies);
	}

	private IdentityHashMap<GeoElement, GeoIdentityRecord>
			finalizedFreshCandidateRecords(
					SpatialRedefineTransaction transaction) {
		IdentityHashMap<GeoElement, GeoIdentityRecord> result =
				finalizedStagedNonOutputRecords(transaction);
		for (SpatialRedefineCandidateOutput output
				: transaction.getProposal().getCandidateOutputs().getOutputs()) {
			result.put(output.getGeo(), candidateRecord(transaction.getDecidedId(
					output.getStableOutputRole()),
					finalizedCandidateSignature(transaction, output)));
		}
		return result;
	}

	private Map<String, PersistentGeoId> allocateRedefineIds(
			SpatialRedefineContext context, SpatialRedefineProposal proposal,
			SpatialRedefineDecision decision,
			SpatialRedefineCandidateParticipation participation) {
		if (decision == SpatialRedefineDecision.REJECT) {
			return Collections.emptyMap();
		}
		LinkedHashMap<String, PersistentGeoId> decided = new LinkedHashMap<>();
		if (decision == SpatialRedefineDecision.RETAIN) {
			for (SpatialRedefinePersistedOutput output
					: context.getOldOutputs().getOutputs()) {
				decided.put(output.getStableOutputRole(), output.getId());
			}
			return decided;
		}
		try {
			for (SpatialRedefineCandidateOutput output
					: proposal.getCandidateOutputs().getOutputs()) {
				PersistentGeoId staged = participation == null ? null
						: stagedCandidateOutputId(participation, output);
				decided.put(output.getStableOutputRole(), staged == null
						? allocatePersistentGeoId() : staged);
			}
			return decided;
		} catch (RuntimeException exception) {
			for (PersistentGeoId allocated : decided.values()) {
				if (participation == null
						|| !participation.copyGeosById().containsKey(allocated)) {
					releaseReservation(allocated);
				}
			}
			throw exception;
		}
	}

	private PersistentGeoId stagedCandidateOutputId(
			SpatialRedefineCandidateParticipation participation,
			SpatialRedefineCandidateOutput output) {
		GeoIdentityRecord record = participation.copyRecordsByGeo().get(
				output.getGeo());
		if (record == null) {
			return null;
		}
		if (!record.toRedefineSignature().equals(output.getSignature())) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.REDEFINE_INCOMPATIBLE,
					"Staged candidate output disagrees with the provider signature",
					participation.getContext().getOldId()));
		}
		return record.getId();
	}

	private Set<SpatialIdentityId> redefineRetiredClosure(
			SpatialRedefineContext context, SpatialRedefineDecision decision) {
		if (decision != SpatialRedefineDecision.FRESH) {
			return Collections.emptySet();
		}
		LinkedHashSet<SpatialIdentityId> closure = new LinkedHashSet<>();
		for (SpatialRedefinePersistedOutput output
				: context.getOldOutputs().getOutputs()) {
			closure.addAll(dependentClosure(output.getId()));
		}
		return Collections.unmodifiableSet(closure);
	}

	private void requireCurrentFreshClosure(
			SpatialRedefineTransaction transaction) {
		Set<SpatialIdentityId> current = redefineRetiredClosure(
				transaction.getContext(), SpatialRedefineDecision.FRESH);
		if (!current.equals(transaction.getRetiredIds())) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.REDEFINE_CONTEXT_MISSING,
					"Fresh redefine closure changed after inspection",
					transaction.getContext().getOldId()));
		}
	}

	private void completeRedefineCandidateParticipation(
			SpatialRedefineTransaction transaction,
			Map<String, GeoElement> actualOutputs) {
		SpatialRedefineCandidateParticipation participation =
				transaction.getCandidateParticipation();
		if (participation == null) {
			return;
		}
		if (transaction.getDecision() == SpatialRedefineDecision.RETAIN) {
			Set<GeoElement> installed = Collections.newSetFromMap(
					new IdentityHashMap<GeoElement, Boolean>());
			installed.addAll(actualOutputs.values());
			participation.rollbackUninstalledRetainedPromotions(installed);
		}
		claimedCandidateParticipations.remove(participation);
		for (PersistentGeoId id : participation.copyGeosById().keySet()) {
			// Successful graph publication consumed every final ID. A retained
			// candidate output keeps its old ID, so its provisional reservation is
			// deliberately abandoned here.
			releaseReservation(id);
		}
		participation.setState(
				SpatialRedefineCandidateParticipation.State.COMPLETED);
	}

	void rollbackRedefine(SpatialRedefineTransaction transaction) {
		requireOwnedPrepared(transaction);
		LinkedHashSet<PersistentGeoId> reservations = new LinkedHashSet<>();
		if (transaction.getDecision() == SpatialRedefineDecision.FRESH) {
			reservations.addAll(transaction.getDecidedIds().values());
		}
		SpatialRedefineCandidateParticipation participation =
				transaction.getCandidateParticipation();
		if (participation != null) {
			claimedCandidateParticipations.remove(participation);
			participation.rollbackPromotions();
			reservations.addAll(participation.copyGeosById().keySet());
			participation.setState(
					SpatialRedefineCandidateParticipation.State.ABANDONED);
		}
		for (PersistentGeoId reserved : reservations) {
			releaseReservation(reserved);
		}
		transaction.markRolledBack();
		instrumentation.recordRedefineRollback();
		flushRuntimeAnnouncementsWhenUnleased();
	}

	/** @return a two-phase macro template-to-instance remap session */
	public MacroInstantiationSession beginMacroInstantiation(
			SpatialIdentityRegistry templateRegistry, boolean handledByFileImport) {
		return new MacroInstantiationSession(templateRegistry, handledByFileImport);
	}

	private boolean isRetainCompatible(SpatialRedefineContext context,
			SpatialRedefineProposal proposal) {
		if (proposal.getTargetedOutputCount()
				!= proposal.getCandidateOutputs().size()
				|| !context.getOldOutputs().getRoles().equals(
						proposal.getCandidateOutputs().getRoles())
				|| !context.getTargetedStableOutputRole().equals(
						proposal.getTargetedStableOutputRole())) {
			return false;
		}
		for (String role : context.getOldOutputs().getRoles()) {
			if (!context.getOldOutputs().get(role).getSignature()
					.isExactlyCompatibleWith(
							proposal.getCandidateOutputs().get(role).getSignature())) {
				return false;
			}
		}
		return true;
	}

	private boolean isCompleteOldGroup(SpatialRedefineContext context) {
		SpatialRedefineOutputGroup<SpatialRedefinePersistedOutput> group =
				context.getOldOutputs();
		return group.size() == context.getOldHostOutputCount()
				&& group.declaresCardinality(group.size())
				&& sameGeoEnumeration(java.util.Arrays.asList(
						hostOutputs(context.getOldTarget())), group);
	}

	private boolean isCompleteCandidateGroup(SpatialRedefineProposal proposal) {
		SpatialRedefineOutputGroup<SpatialRedefineCandidateOutput> group =
				proposal.getCandidateOutputs();
		return group.size() == proposal.getTargetedOutputCount()
				&& group.declaresCardinality(group.size())
				&& sameGeoEnumeration(java.util.Arrays.asList(
						hostOutputs(proposal.getCandidate())), group);
	}

	private static boolean hasExactRedefineRoleShape(
			SpatialRedefineContext context, SpatialRedefineProposal proposal) {
		return context.getOldOutputs().size()
				== proposal.getCandidateOutputs().size()
				&& context.getOldHostOutputCount()
						== proposal.getTargetedOutputCount()
				&& context.getOldOutputs().getRoles().equals(
						proposal.getCandidateOutputs().getRoles())
				&& context.getTargetedStableOutputRole().equals(
						proposal.getTargetedStableOutputRole());
	}

	private static boolean groupProvidersMatch(SpatialRedefineContext context,
			SpatialRedefineProposal proposal, String providerId) {
		for (SpatialRedefinePersistedOutput output
				: context.getOldOutputs().getOutputs()) {
			if (!providerId.equals(output.getSignature().getProvider())) {
				return false;
			}
		}
		for (SpatialRedefineCandidateOutput output
				: proposal.getCandidateOutputs().getOutputs()) {
			if (!providerId.equals(output.getSignature().getProvider())) {
				return false;
			}
		}
		return true;
	}

	private static boolean sameGeoEnumeration(List<GeoElement> enumeration,
			SpatialRedefineOutputGroup<? extends SpatialRedefineOutput> group) {
		if (enumeration.size() != group.size()) {
			return false;
		}
		Set<GeoElement> unmatched = Collections.newSetFromMap(
				new IdentityHashMap<GeoElement, Boolean>());
		for (GeoElement geo : enumeration) {
			if (geo == null || !unmatched.add(geo)) {
				return false;
			}
		}
		for (SpatialRedefineOutput output : group.getOutputs()) {
			if (!unmatched.remove(output.getGeo())) {
				return false;
			}
		}
		return unmatched.isEmpty();
	}

	private static String roleForGeo(
			SpatialRedefineOutputGroup<SpatialRedefineCandidateOutput> group,
			GeoElement target) {
		String result = null;
		for (SpatialRedefineCandidateOutput output : group.getOutputs()) {
			if (output.getGeo() == target) {
				if (result != null) {
					return null;
				}
				result = output.getStableOutputRole();
			}
		}
		return result;
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
		boolean current = hostOutputCount(context.getOldTarget())
				== context.getOldHostOutputCount()
				&& isCompleteOldGroup(context);
		for (SpatialRedefinePersistedOutput output
				: context.getOldOutputs().getOutputs()) {
			GeoIdentityRecord record = getGeoRecord(output.getId());
			current = current && output.getId().equals(idsByGeo.get(output.getGeo()))
					&& geosById.get(output.getId()) == output.getGeo()
					&& record != null
					&& output.getSignature().isExactlyCompatibleWith(
							record.toRedefineSignature())
					&& output.getDefinitionRevision()
							== record.getDefinitionRevision()
					&& output.getTopologyRevision()
							== record.getTopologyRevision();
		}
		if (!current) {
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

	private static GeoElement[] hostOutputs(GeoElement geo) {
		return geo.getParentAlgorithm() == null
				? new GeoElement[] {geo} : geo.getParentAlgorithm().getOutput();
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
		boolean sealedProviderPublication = validateSealedProviderPublication(
				batch, attachments);
		validateBatch(batch, attachments, requireCompleteClosure, copied,
				allowExactIssuedRestore);
		if (batch.isEmpty() && attachments.isEmpty()) {
			return;
		}
		Map<SpatialIdentityId, SpatialIdentityRecord> prospectiveRecords =
				new LinkedHashMap<>(records);
		IdentityHashMap<GeoElement, PersistentGeoId> prospectiveIdsByGeo =
				new IdentityHashMap<>(idsByGeo);
		Map<PersistentGeoId, GeoElement> prospectiveGeosById =
				new LinkedHashMap<>(geosById);
		for (SpatialIdentityRecord record : batch) {
			prospectiveRecords.put(record.getId(), record);
		}
		for (Map.Entry<GeoElement, PersistentGeoId> attachment : attachments.entrySet()) {
			prospectiveIdsByGeo.put(attachment.getKey(), attachment.getValue());
			prospectiveGeosById.put(attachment.getValue(), attachment.getKey());
		}
		SpatialLifecycleProspectiveGraph graph = prospectiveGraphSnapshot(
				SpatialLifecycleOperationKind.ADMITTED_TOPOLOGY_CHANGE,
				"registry-publication", prospectiveRecords, prospectiveIdsByGeo,
				prospectiveGeosById, Collections.<SpatialIdentityId>emptySet());
		SpatialLifecycleRuntime.PreparedSwitch runtimeSwitch =
				prepareRuntimeSwitch(graph, sealedProviderPublication);
		commitPreparedGraph(graph, runtimeSwitch,
				"Identity publication/runtime switch failed atomically",
				sealedProviderPublication);
		for (SpatialIdentityRecord record : batch) {
			SpatialRecordResolution resolution = resolutions.get(record.getId());
			if (resolution != null
					&& resolution.getState() == SpatialResolutionState.BROKEN) {
				for (SpatialIdentityDiagnostic diagnostic
						: resolution.getDiagnostics()) {
					diagnostics.add(diagnostic);
					instrumentation.recordUnresolvedReference(
							record.getId().getKind());
				}
			}
		}
		if (copied) {
			instrumentation.recordCopyCommit();
		}
		flushRuntimeAnnouncementsWhenUnleased();
		notifyPersistentIdentityAttachments(attachments);
	}

	private boolean validateSealedProviderPublication(
			List<SpatialIdentityRecord> batch,
			IdentityHashMap<GeoElement, PersistentGeoId> attachments) {
		SpatialRedefineCandidateParticipation participation =
				activeCandidateParticipation;
		if (participation == null || participation.getState()
				!= SpatialRedefineCandidateParticipation.State.SEALED
				|| redefineExternalCallbackDepth > 0
				|| !claimedCandidateParticipations.isEmpty()) {
			return false;
		}
		Set<PersistentGeoId> stagedIds = participation.copyGeosById().keySet();
		for (SpatialIdentityRecord record : batch) {
			Objects.requireNonNull(record);
			if (stagedIds.contains(record.getId())) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"Provider publication overlaps a staged candidate identity",
						record.getId()));
			}
			for (SpatialIdentityId reference : record.getReferences()) {
				if (stagedIds.contains(reference)) {
					throw failure(SpatialIdentityDiagnostic.forReference(
							SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
							"Provider publication references a staged candidate identity",
							record.getId(), reference));
				}
			}
		}
		for (Map.Entry<GeoElement, PersistentGeoId> attachment
				: attachments.entrySet()) {
			if (!participation.wasPresentAtEntry(attachment.getKey())
					|| participation.getStagedRecord(attachment.getKey()) != null
					|| stagedIds.contains(attachment.getValue())) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
						"Provider publication overlaps a parsed redefine candidate",
						attachment.getValue()));
			}
		}
		return true;
	}

	private void notifyPersistentIdentityAttachments(
			Map<? extends GeoElement, PersistentGeoId> attachments) {
		ArrayList<Map.Entry<? extends GeoElement, PersistentGeoId>> ordered =
				new ArrayList<>(attachments.entrySet());
		ordered.sort((first, second) -> {
			int byConstruction = Integer.compare(
					constructionIndex(first.getKey()),
					constructionIndex(second.getKey()));
			return byConstruction != 0 ? byConstruction
					: first.getValue().compareTo(second.getValue());
		});
		for (Map.Entry<? extends GeoElement, PersistentGeoId> attachment
				: ordered) {
			GeoElement geo = attachment.getKey();
			if (geo instanceof PersistentGeoIdentityListener) {
				((PersistentGeoIdentityListener) geo)
						.onPersistentGeoIdentityAttached(attachment.getValue());
			}
			SpatialIdentityRecord record = records.get(attachment.getValue());
			if (record instanceof GeoIdentityRecord
					&& ConstructionGeoRedefineProvider
							.INTERACTION_POINT_OUTPUT_ROLE.equals(
									((GeoIdentityRecord) record).getStableOutputRole())
					&& geo.getParentAlgorithm()
							instanceof org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D) {
				((org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D)
						geo.getParentAlgorithm()).restoreOwnedInputPresentation();
			}
		}
	}

	private void validateBatch(List<SpatialIdentityRecord> batch,
			IdentityHashMap<GeoElement, PersistentGeoId> attachments,
			boolean requireCompleteClosure, boolean copied,
			boolean allowExactIssuedRestore) {
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
		validatePersistentIdentityAttachmentListeners(attachments, batchIds,
				copied);
	}

	private void validatePersistentIdentityAttachmentListeners(
			IdentityHashMap<GeoElement, PersistentGeoId> attachments,
			Map<SpatialIdentityId, SpatialIdentityRecord> batchIds,
			boolean immediateCopy) {
		java.util.function.Function<GeoElement, GeoIdentityRecord>
				prospectiveRecord =
				geo -> {
					PersistentGeoId staged = attachments.get(geo);
					PersistentGeoId id = staged == null ? idsByGeo.get(geo) : staged;
					SpatialIdentityRecord record = staged == null
							? records.get(id) : batchIds.get(id);
					return record instanceof GeoIdentityRecord
							? (GeoIdentityRecord) record : null;
				};
		IdentityHashMap<GeoElement, GeoIdentityRecord> stagedRecordsByGeo =
				new IdentityHashMap<>();
		for (Map.Entry<GeoElement, PersistentGeoId> attachment
				: attachments.entrySet()) {
			SpatialIdentityRecord record = batchIds.get(attachment.getValue());
			if (record instanceof GeoIdentityRecord) {
				stagedRecordsByGeo.put(attachment.getKey(),
						(GeoIdentityRecord) record);
			}
		}
		validateConstructionIdentityDependencyDags(stagedRecordsByGeo,
				prospectiveRecord);
		for (Map.Entry<GeoElement, PersistentGeoId> attachment
				: attachments.entrySet()) {
			GeoElement geo = attachment.getKey();
			if (!(geo instanceof PersistentGeoIdentityListener)) {
				continue;
			}
			SpatialIdentityRecord record = batchIds.get(attachment.getValue());
			if (!(record instanceof GeoIdentityRecord)) {
				continue;
			}
			try {
				((PersistentGeoIdentityListener) geo)
						.validatePersistentGeoIdentityAttachment(
								attachment.getValue(), (GeoIdentityRecord) record,
								prospectiveRecord, immediateCopy);
			} catch (RuntimeException exception) {
				throw failure(SpatialIdentityDiagnostic.forSubject(
						SpatialIdentityDiagnostic.Code.MALFORMED_RECORD,
						"Geo rejected its prospective persistent identity context",
						attachment.getValue()), exception);
			}
		}
	}

	private void validateConstructionIdentityDependencyDags(
			Map<? extends GeoElement, ? extends GeoIdentityRecord> attachments,
			java.util.function.Function<GeoElement, GeoIdentityRecord>
					prospectiveRecord) {
		for (Map.Entry<? extends GeoElement, ? extends GeoIdentityRecord>
				attachment : attachments.entrySet()) {
			GeoIdentityRecord attachedRecord = attachment.getValue();
			if (!hasDirectedConstructionDependencies(attachedRecord)) {
				continue;
			}
			GeoElement attachedGeo = attachment.getKey();
			if (!hasExpectedConstructionIdentityContract(attachedGeo,
					attachedRecord)) {
				throw malformedConstructionIdentity(attachedRecord,
						"base contract disagrees with the attached geo");
			}
			boolean requireCompleteDependencies =
					ConstructionGeoRedefineProvider.isPublicLocusV2Output(
							attachedGeo);
			ArrayList<PersistentGeoId> expected = new ArrayList<>();
			for (GeoElement dependencyGeo
					: ConstructionGeoRedefineProvider.durableDependencyGeos(
							attachedGeo)) {
				GeoIdentityRecord dependencyRecord =
						prospectiveRecord.apply(dependencyGeo);
				if (dependencyRecord == null && requireCompleteDependencies) {
					throw malformedConstructionIdentity(attachedRecord,
							"required public dependency has no prospective identity");
				}
				if (dependencyRecord != null && !expected.contains(
						dependencyRecord.getId())) {
					expected.add(dependencyRecord.getId());
				}
			}
			Collections.sort(expected);
			if (!expected.equals(attachedRecord.getDependencies())) {
				throw malformedConstructionIdentity(attachedRecord,
						"dependencies disagree with the prospective algorithm DAG");
			}
		}
	}

	private static boolean hasExpectedConstructionIdentityContract(
			GeoElement geo, GeoIdentityRecord record) {
		return ConstructionGeoRedefineProvider.PROVIDER_ID.equals(
				record.getProvider())
				&& ConstructionGeoRedefineProvider.familyFor(geo).equals(
						record.getFamily())
				&& ConstructionGeoRedefineProvider.SCHEMA_ID.equals(
						record.getSchemaId())
				&& record.getSchemaVersion()
						== ConstructionGeoRedefineProvider.SCHEMA_VERSION
				&& record.getAuthority() == EditAuthorityMode.CONSTRUCTION_DEFINED
				&& record.getBindingRole() == ProjectionBindingRole.NOT_APPLICABLE
				&& ConstructionGeoRedefineProvider.supportsStableOutputRole(
						geo, record.getStableOutputRole())
				&& record.getOutputCardinality() == 1;
	}

	private SpatialIdentityException malformedConstructionIdentity(
			GeoIdentityRecord record, String inconsistency) {
		return failure(SpatialIdentityDiagnostic.forSubject(
				SpatialIdentityDiagnostic.Code.MALFORMED_RECORD,
				"Construction identity " + inconsistency, record.getId()));
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
		int semanticVersion = record.getSemanticVersion();
		boolean geoVersion = record instanceof GeoIdentityRecord
				&& semanticVersion == XML_VERSION;
		boolean typedVersion = !(record instanceof GeoIdentityRecord)
				&& (semanticVersion == 1 || semanticVersion == 2);
		if (!geoVersion && !typedVersion) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.UNSUPPORTED_VERSION,
					"Unsupported record semantic version: "
							+ semanticVersion, record.getId()));
		}
		if (semanticVersion == 2) {
			validateVersionTwoShape(record);
		}
	}

	private void validateVersionTwoShape(SpatialIdentityRecord record) {
		boolean valid;
		if (record instanceof ProjectionFrameRecord) {
			ProjectionFrameRecord frame = (ProjectionFrameRecord) record;
			valid = exactIds(frame.getDefinitionGeoIds(), frame.getOriginGeoId(),
					frame.getUGeoId(), frame.getVGeoId()) && frame.getFamily() != null
					&& frame.getUnits() != null && frame.getHandedness() != null
					&& frame.getFidelity() != null;
		} else if (record instanceof ProjectionDiagramMapRecord) {
			ProjectionDiagramMapRecord map = (ProjectionDiagramMapRecord) record;
			valid = exactIds(map.getDefinitionGeoIds(), map.getA00GeoId(),
					map.getA01GeoId(), map.getA10GeoId(), map.getA11GeoId(),
					map.getB0GeoId(), map.getB1GeoId(), map.getDeclaredScaleGeoId())
					&& map.getOrientation() != null && map.getUnits() != null
					&& map.getFidelity() != null;
		} else if (record instanceof ProjectionFrameRelationRecord) {
			ProjectionFrameRelationRecord relation =
					(ProjectionFrameRelationRecord) record;
			boolean hinge = ProjectionFrameRelationRecord.HINGE_UNFOLD.equals(
					relation.getRelationKind())
					&& exactIds(relation.getDefinitionGeoIds(),
							relation.getSupportStartGeoId(),
							relation.getSupportEndGeoId(),
							relation.getFoldSignGeoId());
			boolean changeOfPlane = ProjectionFrameRelationRecord.CHANGE_OF_PLANE.equals(
					relation.getRelationKind()) && relation.getFoldSignGeoId() == null
					&& exactIds(relation.getDefinitionGeoIds(),
							relation.getSupportStartGeoId(),
							relation.getSupportEndGeoId());
			boolean orientation = ProjectionFrameRelationRecord.POSITIVE_ORIENTATION
					.equals(relation.getOrientation())
					|| ProjectionFrameRelationRecord.NEGATIVE_ORIENTATION
							.equals(relation.getOrientation());
			valid = (hinge || changeOfPlane) && orientation
					&& ProjectionFrameRelationRecord.EXPLICIT_CONSTRUCTION.equals(
							relation.getProvenance());
		} else if (record instanceof ProjectionSystemRecord) {
			ProjectionSystemRecord system = (ProjectionSystemRecord) record;
			valid = system.getDefinitionGeoIds().isEmpty() && system.getUnits() != null
					&& finitePositive(system.getAbsoluteTolerance())
					&& finitePositive(system.getRelativeTolerance())
					&& finitePositive(system.getRankTolerance())
					&& finitePositive(system.getMapTolerance())
					&& finitePositive(system.getHingeTolerance())
					&& finitePositive(system.getConditionLimit());
		} else if (record instanceof ProjectionBindingRecord) {
			ProjectionBindingRecord binding = (ProjectionBindingRecord) record;
			valid = SpatialObjectRecord.POINT_TYPE.equals(
					binding.getRepresentationType())
					&& SpatialObjectRecord.POINT_TYPE.equals(
							binding.getExpectedSpatialType())
					&& SpatialObjectRecord.POINT_SCHEMA_ID.equals(binding.getSchemaId())
					&& binding.getSchemaVersion()
							== SpatialObjectRecord.POINT_SCHEMA_VERSION
					&& exactIds(binding.getProjectedGeoIds(),
							binding.getProjectedPointGeoId())
					&& binding.getFidelity() != null
					&& binding.getCorrespondence() != null;
		} else if (record instanceof SpatialObjectRecord) {
			SpatialObjectRecord object = (SpatialObjectRecord) record;
			valid = SpatialObjectRecord.POINT_TYPE.equals(object.getSpatialType())
					&& object.getAuthority() == EditAuthorityMode.PROJECTION_DEFINED
					&& SpatialObjectRecord.POINT_SCHEMA_ID.equals(object.getSchemaId())
					&& object.getSchemaVersion()
							== SpatialObjectRecord.POINT_SCHEMA_VERSION
					&& object.getDefinitionGeoIds().isEmpty()
					&& object.getSystemId() != null && !object.getBindingIds().isEmpty();
		} else {
			valid = false;
		}
		if (!valid) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.MALFORMED_RECORD,
					"Version-two record does not have the exact typed persistence shape",
					record.getId()));
		}
	}

	private static boolean exactIds(List<PersistentGeoId> actual,
			PersistentGeoId... explicit) {
		ArrayList<PersistentGeoId> expected = new ArrayList<>();
		for (PersistentGeoId id : explicit) {
			if (id != null && !expected.contains(id)) {
				expected.add(id);
			}
		}
		Collections.sort(expected);
		return actual.equals(expected);
	}

	private static boolean finitePositive(double value) {
		return Double.isFinite(value) && value > 0;
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
						|| !owner.isInConstructionList(geo)
						|| owner.lookupLabel(geo.getLabelSimple()) != geo))) {
			throw failure(SpatialIdentityDiagnostic.forSubject(
					SpatialIdentityDiagnostic.Code.GEO_NOT_SERIALIZABLE,
					"Participating geo has no stable ordinary-element attachment", id));
		}
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
				if (!hasDirectedConstructionDependencies(record)) {
					for (SpatialIdentityId reference : record.getReferences()) {
						connected |= closure.contains(reference);
					}
				}
				if (connected) {
					changed |= closure.add(record.getId());
					changed |= closure.addAll(record.getReferences());
				}
			}
		} while (changed);
		return closure;
	}

	private static boolean hasDirectedConstructionDependencies(
			SpatialIdentityRecord record) {
		if (!(record instanceof GeoIdentityRecord)) {
			return false;
		}
		GeoIdentityRecord geo = (GeoIdentityRecord) record;
		return geo.getAuthority() == EditAuthorityMode.CONSTRUCTION_DEFINED
				&& geo.getBindingRole() == ProjectionBindingRole.NOT_APPLICABLE;
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
			mutationEpoch++;
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
					SpatialIdentityDiagnostic.Code code;
					if (isUnsupportedRecordVersion(message)) {
						code = SpatialIdentityDiagnostic.Code.UNSUPPORTED_VERSION;
					} else if (message != null && (message.contains("identity")
							|| message.contains("token")
							|| message.contains("Expected "))) {
						code = SpatialIdentityDiagnostic.Code.MALFORMED_ID;
					} else {
						code = SpatialIdentityDiagnostic.Code.MALFORMED_RECORD;
					}
					throw preflightFailure(code,
							"Malformed clipboard " + tag + " persistence record",
							exception);
				}
			}
		}

		private static boolean isUnsupportedRecordVersion(String message) {
			return message != null
					&& message.contains("Unsupported record semantic version");
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
		private final RedefineRebuildToken redefineRebuildToken;
		private final ArrayList<SpatialIdentityRecord> stagedRecords = new ArrayList<>();
		private final IdentityHashMap<GeoElement, PersistentGeoId> attachments =
				new IdentityHashMap<>();
		private SpatialIdentityException stagingFailure;
		private boolean finished;

		private LoadSession(LoadPurpose purpose, int sectionVersion,
				RedefineRebuildToken redefineRebuildToken) {
			this.purpose = Objects.requireNonNull(purpose);
			this.sectionVersion = sectionVersion;
			this.redefineRebuildToken = redefineRebuildToken;
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

		/** Parses and stages one supported versioned flat record. */
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
				SpatialIdentityDiagnostic.Code code = isUnsupportedVersion(exception)
						? SpatialIdentityDiagnostic.Code.UNSUPPORTED_VERSION
						: isIdentityParseFailure(exception)
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
				if (redefineRebuildToken != null) {
					finishRedefineRebuildLoad();
				}
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
			if (redefineRebuildToken != null) {
				commitRedefineRebuildLoad();
			} else {
				publishBatch(stagedRecords, attachments, false, false, true);
			}
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
			if (redefineRebuildToken != null) {
				abortRedefineRebuild(redefineRebuildToken);
			}
		}

		private void commitRedefineRebuildLoad() {
			requireActiveRebuildToken(redefineRebuildToken,
					RedefineRebuildToken.Phase.LOAD_OPEN);
			try {
				try (RedefineGraphPublicationPermit ignored =
						beginRedefineGraphPublicationPermit()) {
					publishBatch(stagedRecords, attachments, false, false, true);
				}
				finishRedefineRebuildLoad();
			} catch (RuntimeException failure) {
				abortRedefineRebuild(redefineRebuildToken);
				throw failure;
			}
		}

		private void finishRedefineRebuildLoad() {
			requireActiveRebuildToken(redefineRebuildToken,
					RedefineRebuildToken.Phase.LOAD_OPEN);
			redefineRebuildToken.phase = RedefineRebuildToken.Phase.COMMITTED;
			activeRedefineRebuildToken = null;
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
			return message != null
					&& (message.startsWith("Malformed spatial identity: ")
							|| message.startsWith("Unknown spatial identity kind: ")
							|| message.startsWith("Unsupported spatial identity kind: ")
							|| message.startsWith("Spatial identity token ")
							|| (message.startsWith("Expected ")
									&& message.contains(" identity, found ")));
		}

		private boolean isUnsupportedVersion(IllegalArgumentException exception) {
			String message = exception.getMessage();
			return message != null
					&& message.contains("Unsupported record semantic version");
		}
	}

	/** Lexically scoped candidate-to-ID XML overlay. */
	public final class SerializationOverlay implements AutoCloseable {
		private final IdentityHashMap<GeoElement, PersistentGeoId> entries;
		private boolean closed;

		private SerializationOverlay(
				IdentityHashMap<GeoElement, PersistentGeoId> entries) {
			this.entries = new IdentityHashMap<>(entries);
		}

		/** Ends this overlay without changing published registry state. */
		@Override
		public void close() {
			if (!closed) {
				for (Map.Entry<GeoElement, PersistentGeoId> entry
						: entries.entrySet()) {
					if (entry.getValue().equals(
							serializationOverlay.get(entry.getKey()))) {
						serializationOverlay.remove(entry.getKey());
					}
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
