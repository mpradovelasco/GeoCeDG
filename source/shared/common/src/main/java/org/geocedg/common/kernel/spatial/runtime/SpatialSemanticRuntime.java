/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.geocedg.common.kernel.spatial.identity.ProjectionSystemId;
import org.geocedg.common.kernel.spatial.identity.ProjectionSystemRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityGraph;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialLifecycleProspectiveGraph;
import org.geocedg.common.kernel.spatial.identity.SpatialLifecycleRuntime;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectId;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectRecord;
import org.geocedg.common.kernel.spatial.semantic.SpatialSemanticInstrumentation;
import org.geogebra.common.geogebra3D.kernel3D.geos.GeoPoint3D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;

/**
 * Construction-confined lifecycle owner for the G9A2 normal-DAG point pilot.
 */
public final class SpatialSemanticRuntime implements SpatialLifecycleRuntime {
	private final Construction construction;
	private final SpatialIdentityRegistry registry;
	private final SpatialSemanticInstrumentation instrumentation =
			new SpatialSemanticInstrumentation();
	private Map<ProjectionSystemId, AlgoProjectionSystemCertificate> systems =
			new LinkedHashMap<>();
	private Map<SpatialObjectId, AlgoProjectionDefinedSpatialPoint> points =
			new LinkedHashMap<>();
	private final List<String> diagnostics = new ArrayList<>();
	private final List<AlgoProjectionDefinedSpatialPoint> pendingAnnouncements =
			new ArrayList<>();
	private final List<GeoElement> pendingWithdrawals = new ArrayList<>();
	private final Map<GeoElement, SpatialObjectId> pendingWithdrawalSubjects =
			new IdentityHashMap<>();
	private SpatialSemanticInstrumentation pendingRedefineInstrumentation;
	private SpatialSemanticInstrumentation rollbackRestoreInstrumentation;
	private Map<SpatialObjectId, GeoElement> rollbackRestoreAnnouncedPoints;
	private int rollbackRestoreDepth;
	private boolean rollbackRestoreFailed;

	/** Creates an initially empty runtime owned by one construction. */
	public SpatialSemanticRuntime(Construction construction) {
		this.construction = Objects.requireNonNull(construction);
		this.registry = construction.getSpatialIdentityRegistry();
	}

	/**
	 * Reconciles normal-DAG algorithms after an atomic registry publication.
	 * This method wires dependencies; ordinary value changes remain DAG work.
	 */
	public void onRecordsPublished(Collection<SpatialIdentityId> changedIds) {
		registry.requireDirectSpatialRuntimeMutationAllowed();
		if (changedIds == null) {
			diagnostics.add("Spatial semantic publication callback had no identities");
			return;
		}
		try {
			reconcileAll(registry);
		} catch (RuntimeException exception) {
			diagnostics.add("Spatial semantic reconciliation failed: "
					+ exception.getClass().getSimpleName());
			// Legacy additive publication has already changed the live registry.
			// Withdraw affected old algorithms so their payload cannot appear current.
			onRecordsRetired(changedIds);
		}
	}

	/**
	 * Immediately withdraws affected publications during a registry retirement.
	 * Host deletion then removes the already-wired algorithms through the normal DAG.
	 */
	public void onRecordsRetired(Collection<SpatialIdentityId> retiredIds) {
		registry.requireDirectSpatialRuntimeMutationAllowed();
		if (retiredIds == null) {
			diagnostics.add("Spatial semantic retirement callback had no identities");
			return;
		}
		Set<SpatialIdentityId> retired = new LinkedHashSet<>(retiredIds);
		LinkedHashMap<ProjectionSystemId, AlgoProjectionSystemCertificate> remainingSystems =
				new LinkedHashMap<>();
		for (Map.Entry<ProjectionSystemId, AlgoProjectionSystemCertificate> entry
				: systems.entrySet()) {
			if (intersects(entry.getValue().getDependencyIdentities(), retired)) {
				retireSystem(entry.getValue());
			} else {
				remainingSystems.put(entry.getKey(), entry.getValue());
			}
		}
		LinkedHashMap<SpatialObjectId, AlgoProjectionDefinedSpatialPoint> remainingPoints =
				new LinkedHashMap<>();
		for (Map.Entry<SpatialObjectId, AlgoProjectionDefinedSpatialPoint> entry
				: points.entrySet()) {
			if (intersects(entry.getValue().getDependencyIdentities(), retired)) {
				retirePoint(entry.getValue());
			} else {
				remainingPoints.put(entry.getKey(), entry.getValue());
			}
		}
		systems = remainingSystems;
		points = remainingPoints;
	}

	/** Withdraws runtime publications before the construction clears host state. */
	public void clear() {
		registry.requireDirectSpatialRuntimeMutationAllowed();
		pendingAnnouncements.clear();
		for (AlgoProjectionSystemCertificate algorithm : systems.values()) {
			retireSystem(algorithm);
		}
		for (AlgoProjectionDefinedSpatialPoint algorithm : points.values()) {
			retirePoint(algorithm);
		}
		systems = new LinkedHashMap<>();
		points = new LinkedHashMap<>();
		diagnostics.clear();
	}

	/**
	 * Starts a rollback-only rebuild whose runtime evidence is intentionally
	 * discarded. The failed redefine's staged evidence and adapter notifications
	 * are abandoned before host XML is replayed.
	 */
	public void beginRollbackRestore() {
		if (rollbackRestoreDepth > 0) {
			rollbackRestoreDepth++;
			return;
		}
		pendingRedefineInstrumentation = null;
		pendingAnnouncements.clear();
		rollbackRestoreAnnouncedPoints = new LinkedHashMap<>();
		for (Map.Entry<SpatialObjectId, AlgoProjectionDefinedSpatialPoint> entry
				: points.entrySet()) {
			if (entry.getValue().isDerivedPointAnnounced()) {
				rollbackRestoreAnnouncedPoints.put(entry.getKey(),
						entry.getValue().getDerivedPoint());
			}
		}
		for (GeoElement withdrawal : pendingWithdrawals) {
			SpatialObjectId subject = pendingWithdrawalSubjects.get(withdrawal);
			if (subject != null) {
				rollbackRestoreAnnouncedPoints.putIfAbsent(subject, withdrawal);
			}
		}
		pendingWithdrawals.clear();
		pendingWithdrawalSubjects.clear();
		rollbackRestoreInstrumentation = new SpatialSemanticInstrumentation();
		rollbackRestoreFailed = false;
		rollbackRestoreDepth = 1;
	}

	/** Removes the failed runtime graph without publishing an invalid revision. */
	public void clearForRollbackRestore() {
		if (rollbackRestoreDepth <= 0 || rollbackRestoreInstrumentation == null) {
			throw new IllegalStateException("Spatial rollback restore is not active");
		}
		pendingAnnouncements.clear();
		pendingWithdrawals.clear();
		pendingWithdrawalSubjects.clear();
		for (AlgoProjectionDefinedSpatialPoint algorithm : points.values()) {
			algorithm.useInstrumentation(rollbackRestoreInstrumentation);
			retirePoint(algorithm, false, false);
		}
		for (AlgoProjectionSystemCertificate algorithm : systems.values()) {
			algorithm.useInstrumentation(rollbackRestoreInstrumentation);
			retireSystem(algorithm, false);
		}
		systems = new LinkedHashMap<>();
		points = new LinkedHashMap<>();
		diagnostics.clear();
	}

	/**
	 * Makes the silently rebuilt algorithms live while discarding all evaluation
	 * evidence produced solely to restore the pre-operation snapshot.
	 */
	public void finishRollbackRestore(boolean successful) {
		if (rollbackRestoreDepth <= 0) {
			return;
		}
		if (!successful) {
			rollbackRestoreFailed = true;
		}
		rollbackRestoreDepth--;
		if (rollbackRestoreDepth > 0) {
			return;
		}
		for (AlgoProjectionSystemCertificate algorithm : systems.values()) {
			algorithm.useInstrumentation(instrumentation);
		}
		for (AlgoProjectionDefinedSpatialPoint algorithm : points.values()) {
			algorithm.useInstrumentation(instrumentation);
		}
		pendingAnnouncements.clear();
		pendingWithdrawals.clear();
		pendingWithdrawalSubjects.clear();
		Map<SpatialObjectId, GeoElement> previousAdapters =
				rollbackRestoreAnnouncedPoints;
		boolean transportAdapters = successful && !rollbackRestoreFailed;
		rollbackRestoreAnnouncedPoints = null;
		rollbackRestoreInstrumentation = null;
		rollbackRestoreFailed = false;
		if (transportAdapters && previousAdapters != null) {
			transportRollbackAdapters(previousAdapters);
		}
	}

	private void transportRollbackAdapters(
			Map<SpatialObjectId, GeoElement> previousAdapters) {
		for (Map.Entry<SpatialObjectId, GeoElement> entry : previousAdapters.entrySet()) {
			AlgoProjectionDefinedSpatialPoint restored = points.get(entry.getKey());
			GeoElement previous = entry.getValue();
			if (restored != null && restored.getDerivedPoint() == previous) {
				continue;
			}
			try {
				previous.notifyRemove();
			} catch (RuntimeException exception) {
				diagnostics.add("Could not replace prior rollback adapter: "
						+ exception.getClass().getSimpleName());
			}
			if (restored != null) {
				try {
					restored.announceDerivedPoint();
				} catch (RuntimeException exception) {
					diagnostics.add("Could not announce restored rollback adapter: "
							+ exception.getClass().getSimpleName());
				}
			}
		}
	}

	/** Preflights the evidence merge before successful host authority is consumed. */
	public void preflightRedefineCompletion() {
		if (pendingRedefineInstrumentation != null) {
			instrumentation.preflightMergeFrom(pendingRedefineInstrumentation);
		}
	}

	/** Commits evidence from the now-terminal provider-owned redefine. */
	public void commitRedefineCompletion() {
		if (pendingRedefineInstrumentation == null) {
			return;
		}
		instrumentation.mergeFrom(pendingRedefineInstrumentation);
		for (AlgoProjectionSystemCertificate algorithm : systems.values()) {
			algorithm.useInstrumentation(instrumentation);
		}
		for (AlgoProjectionDefinedSpatialPoint algorithm : points.values()) {
			algorithm.useInstrumentation(instrumentation);
		}
		pendingRedefineInstrumentation = null;
	}

	public SpatialSemanticInstrumentation getInstrumentation() {
		return instrumentation;
	}

	/** @return monotone authoritative certificate-publication epoch */
	public long getPublicationEpoch() {
		return instrumentation.getAuthoritativePublicationEpoch();
	}

	/**
	 * Announces terminal one-way adapters only after registry publication leases
	 * have closed. A listener-triggered model mutation is therefore a subsequent
	 * publication that stales, rather than being absorbed by, an older rollback.
	 */
	public void flushPendingAnnouncements() {
		registry.requireDirectSpatialRuntimeMutationAllowed();
		if (pendingAnnouncements.isEmpty() && pendingWithdrawals.isEmpty()) {
			return;
		}
		List<GeoElement> withdrawals = new ArrayList<>(pendingWithdrawals);
		pendingWithdrawals.clear();
		pendingWithdrawalSubjects.clear();
		List<AlgoProjectionDefinedSpatialPoint> pending =
				new ArrayList<>(pendingAnnouncements);
		pendingAnnouncements.clear();
		for (GeoElement withdrawal : withdrawals) {
			try {
				withdrawal.notifyRemove();
			} catch (RuntimeException exception) {
				diagnostics.add("Could not withdraw derived spatial point: "
						+ exception.getClass().getSimpleName());
			}
		}
		for (AlgoProjectionDefinedSpatialPoint point : pending) {
			if (!points.containsValue(point)) {
				continue;
			}
			try {
				point.announceDerivedPoint();
			} catch (RuntimeException exception) {
				diagnostics.add("Could not announce derived spatial point: "
						+ exception.getClass().getSimpleName());
			}
		}
	}

	/**
	 * Returns the current publication for a projection system.
	 *
	 * @param systemId durable projection-system identity
	 * @return current certificate publication, or {@code null} when not active
	 */
	public ProjectionSystemPilotCertificate getProjectionSystemCertificate(
			ProjectionSystemId systemId) {
		AlgoProjectionSystemCertificate algorithm = systems.get(systemId);
		return algorithm == null ? null : algorithm.getCertificate();
	}

	/**
	 * Returns the current publication for a projection-defined spatial point.
	 *
	 * @param objectId durable spatial-object identity
	 * @return current certificate publication, or {@code null} when not active
	 */
	public SpatialPointPilotCertificate getSpatialPointCertificate(
			SpatialObjectId objectId) {
		AlgoProjectionDefinedSpatialPoint algorithm = points.get(objectId);
		return algorithm == null ? null : algorithm.getCertificate();
	}

	/** @return the one-way transient point, or {@code null} when not active */
	public GeoPoint3D getDerivedPoint(SpatialObjectId objectId) {
		AlgoProjectionDefinedSpatialPoint algorithm = points.get(objectId);
		return algorithm == null ? null : algorithm.getDerivedPoint();
	}

	/**
	 * Returns the normal-DAG algorithm for a projection system.
	 *
	 * @param systemId durable projection-system identity
	 * @return active algorithm, or {@code null} when not wired
	 */
	public AlgoProjectionSystemCertificate getSystemAlgorithm(
			ProjectionSystemId systemId) {
		return systems.get(systemId);
	}

	/**
	 * Returns the normal-DAG algorithm for a projection-defined spatial point.
	 *
	 * @param objectId durable spatial-object identity
	 * @return active algorithm, or {@code null} when not wired
	 */
	public AlgoProjectionDefinedSpatialPoint getPointAlgorithm(
			SpatialObjectId objectId) {
		return points.get(objectId);
	}

	public List<String> getDiagnostics() {
		return Collections.unmodifiableList(new ArrayList<>(diagnostics));
	}

	@Override
	public PreparedSwitch prepare(SpatialLifecycleProspectiveGraph graph) {
		return prepareReconciliation(Objects.requireNonNull(graph));
	}

	private void reconcileAll(SpatialIdentityGraph graph) {
		PreparedReconciliation prepared = prepareReconciliation(graph);
		try {
			prepared.commit();
		} catch (RuntimeException exception) {
			prepared.rollback();
			throw exception;
		}
	}

	private PreparedReconciliation prepareReconciliation(SpatialIdentityGraph graph) {
		LinkedHashMap<ProjectionSystemId, ProjectionSystemRecord> desiredSystems =
				new LinkedHashMap<>();
		LinkedHashMap<SpatialObjectId, SpatialObjectRecord> desiredObjects =
				new LinkedHashMap<>();
		for (SpatialIdentityRecord record : graph.getRecords()) {
			if (record instanceof ProjectionSystemRecord
					&& record.getSemanticVersion() == 2) {
				ProjectionSystemRecord system = (ProjectionSystemRecord) record;
				desiredSystems.put(system.getId(), system);
			} else if (record instanceof SpatialObjectRecord
					&& record.getSemanticVersion() == 2) {
				SpatialObjectRecord object = (SpatialObjectRecord) record;
				desiredObjects.put(object.getId(), object);
			}
		}
		LinkedHashMap<ProjectionSystemId, AlgoProjectionSystemCertificate> nextSystems =
				new LinkedHashMap<>();
		LinkedHashMap<SpatialObjectId, AlgoProjectionDefinedSpatialPoint> nextPoints =
				new LinkedHashMap<>();
		ArrayList<AlgoProjectionSystemCertificate> preparedSystems = new ArrayList<>();
		ArrayList<AlgoProjectionDefinedSpatialPoint> preparedPoints = new ArrayList<>();
		SpatialSemanticInstrumentation preparedInstrumentation =
				new SpatialSemanticInstrumentation();
		try (Construction.NewTypeRegistrationScope ignored =
				construction.suppressNewTypeRegistration()) {
			for (ProjectionSystemRecord system : desiredSystems.values()) {
				SpatialSemanticInputs.SystemTopology topology =
						SpatialSemanticInputs.systemTopology(graph, system);
				AlgoProjectionSystemCertificate current = systems.get(system.getId());
				if (sameTopologyAndInputs(current, topology)) {
					nextSystems.put(system.getId(), current);
				} else {
					AlgoProjectionSystemCertificate replacement =
							new AlgoProjectionSystemCertificate(construction, topology,
									preparedInstrumentation, false);
					preparedSystems.add(replacement);
					nextSystems.put(system.getId(), replacement);
				}
			}
			for (SpatialObjectRecord object : desiredObjects.values()) {
				SpatialSemanticInputs.PointTopology topology =
						SpatialSemanticInputs.pointTopology(graph, object);
				AlgoProjectionDefinedSpatialPoint current = points.get(object.getId());
				if (sameTopologyAndInputs(current, topology)) {
					nextPoints.put(object.getId(), current);
				} else {
					AlgoProjectionDefinedSpatialPoint replacement =
							new AlgoProjectionDefinedSpatialPoint(construction, topology,
									preparedInstrumentation, false);
					preparedPoints.add(replacement);
					nextPoints.put(object.getId(), replacement);
				}
			}
			return new PreparedReconciliation(systems, points, nextSystems, nextPoints,
					preparedSystems, preparedPoints, preparedInstrumentation);
		} catch (RuntimeException exception) {
			discardPrepared(preparedSystems, preparedPoints);
			throw exception;
		}
	}

	private static boolean sameTopologyAndInputs(
			AlgoProjectionSystemCertificate current,
			SpatialSemanticInputs.SystemTopology topology) {
		return current != null
				&& current.getTopologyToken().equals(topology.getStructureToken())
				&& sameInputInstances(current.getInput(), topology.getInputs())
				&& current.isCurrentFor(topology.capture());
	}

	private static boolean sameTopologyAndInputs(
			AlgoProjectionDefinedSpatialPoint current,
			SpatialSemanticInputs.PointTopology topology) {
		return current != null
				&& current.getTopologyToken().equals(topology.getStructureToken())
				&& sameInputInstances(current.getInput(), topology.getInputs())
				&& current.isCurrentFor(topology.capture());
	}

	private static boolean sameInputInstances(GeoElement[] current,
			GeoElement[] prospective) {
		if (current.length != prospective.length) {
			return false;
		}
		for (int index = 0; index < current.length; index++) {
			// Reference equality is scheduler wiring evidence only, never semantic
			// continuity or durable identity authority.
			if (current[index] != prospective[index]) {
				return false;
			}
		}
		return true;
	}

	private void discardPrepared(
			Collection<AlgoProjectionSystemCertificate> preparedSystems,
			Collection<AlgoProjectionDefinedSpatialPoint> preparedPoints) {
		for (AlgoProjectionDefinedSpatialPoint algorithm : preparedPoints) {
			try {
				algorithm.discardPrepared();
			} catch (RuntimeException exception) {
				diagnostics.add("Could not discard prepared spatial point: "
						+ exception.getClass().getSimpleName());
			}
		}
		for (AlgoProjectionSystemCertificate algorithm : preparedSystems) {
			try {
				algorithm.discardPrepared();
			} catch (RuntimeException exception) {
				diagnostics.add("Could not discard prepared projection system: "
						+ exception.getClass().getSimpleName());
			}
		}
	}

	private final class PreparedReconciliation implements PreparedSwitch {
		private final Map<ProjectionSystemId, AlgoProjectionSystemCertificate>
				previousSystems;
		private final Map<SpatialObjectId, AlgoProjectionDefinedSpatialPoint>
				previousPoints;
		private final Map<ProjectionSystemId, AlgoProjectionSystemCertificate>
				preparedSystemMap;
		private final Map<SpatialObjectId, AlgoProjectionDefinedSpatialPoint>
				preparedPointMap;
		private final List<AlgoProjectionSystemCertificate> preparedSystems;
		private final List<AlgoProjectionDefinedSpatialPoint> preparedPoints;
		private final SpatialSemanticInstrumentation preparedInstrumentation;
		private boolean finished;

		PreparedReconciliation(
				Map<ProjectionSystemId, AlgoProjectionSystemCertificate> previousSystems,
				Map<SpatialObjectId, AlgoProjectionDefinedSpatialPoint> previousPoints,
				Map<ProjectionSystemId, AlgoProjectionSystemCertificate> preparedSystemMap,
				Map<SpatialObjectId, AlgoProjectionDefinedSpatialPoint> preparedPointMap,
				List<AlgoProjectionSystemCertificate> preparedSystems,
				List<AlgoProjectionDefinedSpatialPoint> preparedPoints,
				SpatialSemanticInstrumentation preparedInstrumentation) {
			this.previousSystems = new LinkedHashMap<>(previousSystems);
			this.previousPoints = new LinkedHashMap<>(previousPoints);
			this.preparedSystemMap = new LinkedHashMap<>(preparedSystemMap);
			this.preparedPointMap = new LinkedHashMap<>(preparedPointMap);
			this.preparedSystems = new ArrayList<>(preparedSystems);
			this.preparedPoints = new ArrayList<>(preparedPoints);
			this.preparedInstrumentation = preparedInstrumentation;
		}

		@Override
		public void commit() {
			requireOpen();
			SpatialSemanticInstrumentation committedInstrumentation;
			boolean stagedRedefine = registry.isRedefineRuntimePublicationStaged();
			boolean silentRestore = rollbackRestoreDepth > 0;
			try {
				// Preparation has not touched the live DAG. Wire and evaluate only at
				// the construction-confined commit boundary, while every old publication
				// remains current until all replacements are ready.
				for (AlgoProjectionSystemCertificate system : preparedSystems) {
					system.activatePrepared();
				}
				for (AlgoProjectionDefinedSpatialPoint point : preparedPoints) {
					point.activatePrepared();
				}
				for (AlgoProjectionSystemCertificate system : preparedSystems) {
					system.compute();
				}
				for (AlgoProjectionDefinedSpatialPoint point : preparedPoints) {
					point.compute();
				}
				if (stagedRedefine || silentRestore) {
					SpatialSemanticInstrumentation accumulated =
							new SpatialSemanticInstrumentation();
					SpatialSemanticInstrumentation existing = silentRestore
							? rollbackRestoreInstrumentation
							: pendingRedefineInstrumentation;
					if (existing != null) {
						accumulated.mergeFrom(existing);
					}
					accumulated.mergeFrom(preparedInstrumentation);
					if (stagedRedefine) {
						instrumentation.preflightMergeFrom(accumulated);
					}
					committedInstrumentation = accumulated;
				} else {
					instrumentation.preflightMergeFrom(preparedInstrumentation);
					committedInstrumentation = instrumentation;
				}
				for (AlgoProjectionSystemCertificate system : preparedSystems) {
					construction.addUsedType(
							system.getStatusOutput().getGeoClassType());
				}
				for (AlgoProjectionDefinedSpatialPoint point : preparedPoints) {
					construction.addUsedType(
							point.getDerivedPoint().getGeoClassType());
				}
				// Assign the prepared evidence only after every potentially throwing
				// activation/used-type step. Redefine evidence remains staged until the
				// host operation becomes terminal; rollback-restore evidence is discarded.
				if (silentRestore) {
					rollbackRestoreInstrumentation = committedInstrumentation;
				} else if (stagedRedefine) {
					pendingRedefineInstrumentation = committedInstrumentation;
				} else {
					instrumentation.mergeFrom(preparedInstrumentation);
				}
			} catch (RuntimeException exception) {
				discardPrepared(preparedSystems, preparedPoints);
				finished = true;
				throw exception;
			}

			for (AlgoProjectionSystemCertificate system : previousSystems.values()) {
				system.useInstrumentation(committedInstrumentation);
			}
			for (AlgoProjectionDefinedSpatialPoint point : previousPoints.values()) {
				point.useInstrumentation(committedInstrumentation);
			}
			for (AlgoProjectionSystemCertificate system : preparedSystems) {
				system.useInstrumentation(committedInstrumentation);
			}
			for (AlgoProjectionDefinedSpatialPoint point : preparedPoints) {
				point.useInstrumentation(committedInstrumentation);
			}
			systems = preparedSystemMap;
			points = preparedPointMap;
			retireSupersededPoints();
			retireSupersededSystems();
			if (!silentRestore) {
				pendingAnnouncements.addAll(preparedPoints);
			}
			finished = true;
		}

		@Override
		public void rollback() {
			if (!finished) {
				discardPrepared(preparedSystems, preparedPoints);
				finished = true;
			}
		}

		private void retireSupersededPoints() {
			for (Map.Entry<SpatialObjectId, AlgoProjectionDefinedSpatialPoint> entry
					: previousPoints.entrySet()) {
				if (preparedPointMap.get(entry.getKey()) != entry.getValue()) {
					retirePoint(entry.getValue(), false, rollbackRestoreDepth <= 0);
				}
			}
		}

		private void retireSupersededSystems() {
			for (Map.Entry<ProjectionSystemId, AlgoProjectionSystemCertificate> entry
					: previousSystems.entrySet()) {
				if (preparedSystemMap.get(entry.getKey()) != entry.getValue()) {
					retireSystem(entry.getValue(), false);
				}
			}
		}

		private void requireOpen() {
			if (finished) {
				throw new IllegalStateException(
						"Spatial semantic runtime switch is already finished");
			}
		}
	}

	private void retireSystem(AlgoProjectionSystemCertificate algorithm) {
		retireSystem(algorithm, true);
	}

	private void retireSystem(AlgoProjectionSystemCertificate algorithm,
			boolean publishInvalidation) {
		try {
			if (publishInvalidation) {
				algorithm.invalidateCurrentRevision();
			} else {
				algorithm.retireWithoutAuthoritativePublication();
			}
		} catch (RuntimeException exception) {
			diagnostics.add("Could not invalidate projection-system evidence: "
					+ exception.getClass().getSimpleName());
		}
		try (Construction.SpatialSemanticAdapterNotificationScope ignored =
				construction.suppressSpatialSemanticAdapterNotifications()) {
			algorithm.remove();
		} catch (RuntimeException exception) {
			diagnostics.add("Could not remove projection-system algorithm: "
					+ exception.getClass().getSimpleName());
		}
	}

	private void retirePoint(AlgoProjectionDefinedSpatialPoint algorithm) {
		retirePoint(algorithm, true, true);
	}

	private void retirePoint(AlgoProjectionDefinedSpatialPoint algorithm,
			boolean publishInvalidation) {
		retirePoint(algorithm, publishInvalidation, true);
	}

	private void retirePoint(AlgoProjectionDefinedSpatialPoint algorithm,
			boolean publishInvalidation, boolean queueWithdrawal) {
		boolean announced = algorithm.isDerivedPointAnnounced();
		GeoElement derivedPoint = algorithm.getDerivedPoint();
		try {
			if (publishInvalidation) {
				algorithm.invalidateCurrentRevision();
			} else {
				algorithm.retireWithoutAuthoritativePublication();
			}
		} catch (RuntimeException exception) {
			diagnostics.add("Could not invalidate spatial-point evidence: "
					+ exception.getClass().getSimpleName());
		}
		try (Construction.SpatialSemanticAdapterNotificationScope ignored =
				construction.suppressSpatialSemanticAdapterNotifications()) {
			algorithm.remove();
		} catch (RuntimeException exception) {
			diagnostics.add("Could not remove spatial-point algorithm: "
					+ exception.getClass().getSimpleName());
		}
		if (queueWithdrawal && announced
				&& !pendingWithdrawals.contains(derivedPoint)) {
			pendingWithdrawals.add(derivedPoint);
			pendingWithdrawalSubjects.put(derivedPoint, algorithm.getObjectId());
		}
	}

	private static boolean intersects(Set<SpatialIdentityId> first,
			Set<SpatialIdentityId> second) {
		for (SpatialIdentityId id : first) {
			if (second.contains(id)) {
				return true;
			}
		}
		return false;
	}
}
