/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.geocedg.common.kernel.spatial.identity.ProjectionSystemId;
import org.geocedg.common.kernel.spatial.identity.ProjectionSystemRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectId;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectRecord;
import org.geocedg.common.kernel.spatial.semantic.SpatialSemanticInstrumentation;
import org.geogebra.common.geogebra3D.kernel3D.geos.GeoPoint3D;
import org.geogebra.common.kernel.Construction;

/**
 * Construction-confined lifecycle owner for the G9A2 normal-DAG point pilot.
 */
public final class SpatialSemanticRuntime {
	private final Construction construction;
	private final SpatialIdentityRegistry registry;
	private final SpatialSemanticInstrumentation instrumentation =
			new SpatialSemanticInstrumentation();
	private final Map<ProjectionSystemId, AlgoProjectionSystemCertificate> systems =
			new LinkedHashMap<>();
	private final Map<SpatialObjectId, AlgoProjectionDefinedSpatialPoint> points =
			new LinkedHashMap<>();
	private final List<String> diagnostics = new ArrayList<>();

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
		if (changedIds == null) {
			diagnostics.add("Spatial semantic publication callback had no identities");
			return;
		}
		try {
			reconcileAll();
		} catch (RuntimeException exception) {
			diagnostics.add("Spatial semantic reconciliation failed: "
					+ exception.getClass().getSimpleName());
		}
	}

	/**
	 * Immediately withdraws affected publications during a registry retirement.
	 * Host deletion then removes the already-wired algorithms through the normal DAG.
	 */
	public void onRecordsRetired(Collection<SpatialIdentityId> retiredIds) {
		if (retiredIds == null) {
			diagnostics.add("Spatial semantic retirement callback had no identities");
			return;
		}
		Set<SpatialIdentityId> retired = new LinkedHashSet<>(retiredIds);
		Iterator<Map.Entry<ProjectionSystemId, AlgoProjectionSystemCertificate>>
				systemIterator = systems.entrySet().iterator();
		while (systemIterator.hasNext()) {
			Map.Entry<ProjectionSystemId, AlgoProjectionSystemCertificate> entry =
					systemIterator.next();
			if (intersects(entry.getValue().getDependencyIdentities(), retired)) {
				retireSystem(entry.getValue());
				systemIterator.remove();
			}
		}
		Iterator<Map.Entry<SpatialObjectId, AlgoProjectionDefinedSpatialPoint>>
				pointIterator = points.entrySet().iterator();
		while (pointIterator.hasNext()) {
			Map.Entry<SpatialObjectId, AlgoProjectionDefinedSpatialPoint> entry =
					pointIterator.next();
			if (intersects(entry.getValue().getDependencyIdentities(), retired)) {
				retirePoint(entry.getValue());
				pointIterator.remove();
			}
		}
	}

	/** Withdraws runtime publications before the construction clears host state. */
	public void clear() {
		for (AlgoProjectionSystemCertificate algorithm : systems.values()) {
			retireSystem(algorithm);
		}
		for (AlgoProjectionDefinedSpatialPoint algorithm : points.values()) {
			retirePoint(algorithm);
		}
		systems.clear();
		points.clear();
		diagnostics.clear();
	}

	public SpatialSemanticInstrumentation getInstrumentation() {
		return instrumentation;
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

	private void reconcileAll() {
		LinkedHashMap<ProjectionSystemId, ProjectionSystemRecord> desiredSystems =
				new LinkedHashMap<>();
		LinkedHashMap<SpatialObjectId, SpatialObjectRecord> desiredObjects =
				new LinkedHashMap<>();
		for (SpatialIdentityRecord record : registry.getRecords()) {
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
		removeAbsentSystems(desiredSystems.keySet());
		removeAbsentPoints(desiredObjects.keySet());
		for (ProjectionSystemRecord system : desiredSystems.values()) {
			SpatialSemanticInputs.SystemTopology topology =
					SpatialSemanticInputs.systemTopology(registry, system);
			AlgoProjectionSystemCertificate current = systems.get(system.getId());
			if (current != null
					&& current.getTopologyToken().equals(topology.getStructureToken())) {
				continue;
			}
			if (current != null) {
				systems.remove(system.getId());
				retireSystem(current);
			}
			try {
				systems.put(system.getId(), new AlgoProjectionSystemCertificate(
						construction, topology, instrumentation));
			} catch (RuntimeException exception) {
				systems.remove(system.getId());
				diagnostics.add("Could not wire projection system " + system.getId()
						+ ": " + exception.getClass().getSimpleName());
			}
		}
		for (SpatialObjectRecord object : desiredObjects.values()) {
			SpatialSemanticInputs.PointTopology topology =
					SpatialSemanticInputs.pointTopology(registry, object);
			AlgoProjectionDefinedSpatialPoint current = points.get(object.getId());
			if (current != null
					&& current.getTopologyToken().equals(topology.getStructureToken())) {
				continue;
			}
			if (current != null) {
				points.remove(object.getId());
				retirePoint(current);
			}
			try {
				points.put(object.getId(), new AlgoProjectionDefinedSpatialPoint(
						construction, topology, instrumentation));
			} catch (RuntimeException exception) {
				points.remove(object.getId());
				diagnostics.add("Could not wire spatial point " + object.getId()
						+ ": " + exception.getClass().getSimpleName());
			}
		}
	}

	private void removeAbsentSystems(Set<ProjectionSystemId> desired) {
		Iterator<Map.Entry<ProjectionSystemId, AlgoProjectionSystemCertificate>> iterator =
				systems.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<ProjectionSystemId, AlgoProjectionSystemCertificate> entry =
					iterator.next();
			if (!desired.contains(entry.getKey())) {
				retireSystem(entry.getValue());
				iterator.remove();
			}
		}
	}

	private void removeAbsentPoints(Set<SpatialObjectId> desired) {
		Iterator<Map.Entry<SpatialObjectId, AlgoProjectionDefinedSpatialPoint>> iterator =
				points.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<SpatialObjectId, AlgoProjectionDefinedSpatialPoint> entry =
					iterator.next();
			if (!desired.contains(entry.getKey())) {
				retirePoint(entry.getValue());
				iterator.remove();
			}
		}
	}

	private void retireSystem(AlgoProjectionSystemCertificate algorithm) {
		try {
			algorithm.invalidateCurrentRevision();
		} catch (RuntimeException exception) {
			diagnostics.add("Could not invalidate projection-system evidence: "
					+ exception.getClass().getSimpleName());
		}
		try {
			algorithm.remove();
		} catch (RuntimeException exception) {
			diagnostics.add("Could not remove projection-system algorithm: "
					+ exception.getClass().getSimpleName());
		}
	}

	private void retirePoint(AlgoProjectionDefinedSpatialPoint algorithm) {
		try {
			algorithm.invalidateCurrentRevision();
		} catch (RuntimeException exception) {
			diagnostics.add("Could not invalidate spatial-point evidence: "
					+ exception.getClass().getSimpleName());
		}
		try {
			algorithm.remove();
		} catch (RuntimeException exception) {
			diagnostics.add("Could not remove spatial-point algorithm: "
					+ exception.getClass().getSimpleName());
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
