/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.algos.AlgoLocusBetweenMetricV2;
import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.algos.AlgoLocusIntersectionV2;
import org.geocedg.common.kernel.algos.AlgoLocusLocusIntersectionV2;
import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.algos.AlgoLocusMetricV2;
import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.spatial.identity.ConstructionGeoRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.EditAuthorityMode;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRole;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Path;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.ConstructionElement;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoConicPart;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoConicNDConstants;

/**
 * Single feature-gated construction boundary for the public Locus V2 surface.
 *
 * <p>Every productive object is a normal {@link AlgoElement} output. Interactive
 * creation publishes its durable identity and exact dependency envelope as one
 * registry batch; native XML loading reconstructs the same algorithms first and
 * lets the already-serialized identity section attach them at construction
 * commit. Labels are persistence handles only and are never identity authority.</p>
 */
public final class LocusV2PublicOperations {
	private static final double DEFAULT_DOMAIN_EPSILON = 1e-12;

	private LocusV2PublicOperations() {
		// Static public construction boundary.
	}

	/**
	 * Creates {@code LocusV2[Q,P]} for one closed point-driver family.
	 *
	 * @return public point-driven Locus V2
	 */
	public static GeoLocusV2 createPointDriven(Construction construction,
			String label, GeoPoint dependent, GeoPoint driverPoint) {
		requireAccess(construction);
		PointDriver driver = PointDriver.resolve(driverPoint);
		ParticipationBatch batch = new ParticipationBatch(construction);
		if (!construction.isFileLoading()) {
			batch.prepareAll(driver.dependencies(dependent));
		}
		PersistentGeoId outputId = batch.reserveOutput();
		AlgoDependentPointLocusV2 algorithm = null;
		try {
			algorithm = new AlgoDependentPointLocusV2(construction, dependent,
					driverPoint, driver.coordinate, driver.support, null,
					driver.branch, driver.family, DEFAULT_DOMAIN_EPSILON);
			GeoLocusV2 output = algorithm.getLocus();
			finishLabel(output, label, false);
			if (!construction.isFileLoading()) {
				batch.stageReserved(output, outputId,
						algorithm.getDurableDependencyGeos());
				batch.publish();
			}
			return output;
		} catch (RuntimeException exception) {
			batch.abandonBeforePublication();
			removeFailed(algorithm);
			throw exception;
		}
	}

	/**
	 * Creates the identity or mapped scalar public Locus V2 form.
	 *
	 * @return public scalar-driven Locus V2
	 */
	public static GeoLocusV2 createScalar(Construction construction,
			String label, GeoPoint dependent, GeoNumeric state,
			GeoNumeric trueCoordinate, LocusV2DomainDescriptor domain) {
		requireAccess(construction);
		if (domain == null) {
			throw new IllegalArgumentException("A scalar Locus V2 needs a domain");
		}
		ParticipationBatch batch = new ParticipationBatch(construction);
		List<GeoElement> dependencies = direct(dependent, state, trueCoordinate,
				domain.getSource());
		if (!construction.isFileLoading()) {
			batch.prepareAll(dependencies);
		}
		PersistentGeoId outputId = batch.reserveOutput();
		AlgoDependentPointLocusV2 algorithm = null;
		try {
			algorithm = new AlgoDependentPointLocusV2(construction, dependent,
					state, trueCoordinate, null, domain.getSource(), null,
					SemanticGeneratorFamily1D.SCALAR_STATE,
					DEFAULT_DOMAIN_EPSILON);
			GeoLocusV2 output = algorithm.getLocus();
			finishLabel(output, label, false);
			if (!construction.isFileLoading()) {
				batch.stageReserved(output, outputId,
						algorithm.getDurableDependencyGeos());
				batch.publish();
			}
			return output;
		} catch (RuntimeException exception) {
			batch.abandonBeforePublication();
			removeFailed(algorithm);
			throw exception;
		}
	}

	/**
	 * Creates {@code Point[L,"branch",parameter]} with an exact preimage.
	 *
	 * @return exact semantic point on the locus
	 */
	public static GeoPoint createSemanticPoint(Construction construction,
			String label, GeoLocusV2 source, GeoText branch,
			GeoNumberValue parameter) {
		requireAccess(construction);
		GeoElement parameterGeo = parameter.toGeoElement();
		ParticipationBatch batch = new ParticipationBatch(construction);
		List<GeoElement> dependencies = direct(source, branch, parameterGeo);
		if (!construction.isFileLoading()) {
			batch.prepareAll(dependencies);
		}
		PersistentGeoId outputId = batch.reserveOutput();
		AlgoSemanticLocusPoint2D algorithm = null;
		try {
			algorithm = new AlgoSemanticLocusPoint2D(construction, source, branch,
					parameter);
			GeoPoint output = algorithm.getPoint();
			finishLabel(output, label, false);
			if (!construction.isFileLoading()) {
				batch.stageReserved(output, outputId, dependencies);
				batch.publish();
			}
			return output;
		} catch (RuntimeException exception) {
			batch.abandonBeforePublication();
			removeFailed(algorithm);
			throw exception;
		}
	}

	/**
	 * Creates one public rich total metric result.
	 *
	 * @return rich total metric result
	 */
	public static GeoLocusMetricResult totalMetric(Construction construction,
			String label, GeoLocusV2 source) {
		return totalMetric(construction, label, source, false);
	}

	/**
	 * Creates one public rich between-position metric result.
	 *
	 * @return rich between-position metric result
	 */
	public static GeoLocusMetricResult betweenMetric(Construction construction,
			String label, GeoLocusV2 source, GeoPoint start, GeoPoint end) {
		requireAccess(construction);
		ParticipationBatch batch = new ParticipationBatch(construction);
		List<GeoElement> dependencies = direct(source, start, end);
		if (!construction.isFileLoading()) {
			batch.prepareAll(dependencies);
		}
		PersistentGeoId outputId = batch.reserveOutput();
		AlgoLocusBetweenMetricV2 algorithm = null;
		try {
			algorithm = new AlgoLocusBetweenMetricV2(construction, label, source,
					start, end, outputId);
			GeoLocusMetricResult output = algorithm.getResult();
			finishLabel(output, label, false);
			if (!construction.isFileLoading()) {
				batch.stageReserved(output, outputId, dependencies);
				batch.publish();
			}
			return output;
		} catch (RuntimeException exception) {
			batch.abandonBeforePublication();
			removeFailed(algorithm);
			throw exception;
		}
	}

	/**
	 * Creates the normalized rich-parent plus scalar {@code Length[L]} DAG.
	 *
	 * @return public scalar length result
	 */
	public static GeoNumeric scalarLength(Construction construction,
			String label, GeoLocusV2 source) {
		requireAccess(construction);
		ParticipationBatch batch = new ParticipationBatch(construction);
		List<GeoElement> sourceDependency = direct(source);
		if (!construction.isFileLoading()) {
			batch.prepareAll(sourceDependency);
		}
		PersistentGeoId richId = batch.reserveOutput();
		PersistentGeoId scalarId = batch.reserveOutput();
		AlgoLocusMetricV2 richAlgorithm = null;
		AlgoLocusMetricScalarAdapter scalarAlgorithm = null;
		try {
			richAlgorithm = new AlgoLocusMetricV2(construction, null, source,
					richId);
			GeoLocusMetricResult rich = richAlgorithm.getResult();
			finishLabel(rich, null, true);
			scalarAlgorithm = new AlgoLocusMetricScalarAdapter(construction, label,
					rich);
			GeoNumeric scalar = scalarAlgorithm.getScalarOutput();
			finishLabel(scalar, label, false);
			if (!construction.isFileLoading()) {
				batch.stageReserved(rich, richId, sourceDependency);
				batch.stageReserved(scalar, scalarId, direct(rich));
				batch.publish();
			}
			return scalar;
		} catch (RuntimeException exception) {
			batch.abandonBeforePublication();
			removeFailed(scalarAlgorithm);
			removeFailed(richAlgorithm);
			throw exception;
		}
	}

	/**
	 * Reconstructs the hidden rich parent of a serialized {@code Length[locus]}.
	 *
	 * @return reconstructed scalar child
	 */
	public static GeoNumeric scalarFromMetric(Construction construction,
			String label, GeoLocusMetricResult rich) {
		if (!construction.isFileLoading()) {
			throw new IllegalStateException(
					"Length[richMetric] is reserved for XML reconstruction");
		}
		requireAccess(construction);
		ParticipationBatch batch = new ParticipationBatch(construction);
		List<GeoElement> dependencies = direct(rich);
		if (!construction.isFileLoading()) {
			batch.prepareAll(dependencies);
		}
		PersistentGeoId outputId = batch.reserveOutput();
		AlgoLocusMetricScalarAdapter algorithm = null;
		try {
			algorithm = new AlgoLocusMetricScalarAdapter(construction, label, rich);
			GeoNumeric output = algorithm.getScalarOutput();
			finishLabel(output, label, false);
			if (!construction.isFileLoading()) {
				batch.stageReserved(output, outputId, dependencies);
				batch.publish();
			}
			return output;
		} catch (RuntimeException exception) {
			batch.abandonBeforePublication();
			removeFailed(algorithm);
			throw exception;
		}
	}

	/**
	 * Creates the rich general-command intersection result.
	 *
	 * @return rich semantic intersection result
	 */
	public static GeoLocusIntersectionResult intersect(Construction construction,
			String label, GeoLocusV2 source, GeoElement target) {
		requireAccess(construction);
		ParticipationBatch batch = new ParticipationBatch(construction);
		List<GeoElement> dependencies = direct(source, target);
		if (!construction.isFileLoading()) {
			batch.prepareAll(dependencies);
		}
		PersistentGeoId outputId = batch.reserveOutput();
		AlgoElement algorithm = null;
		try {
			GeoLocusIntersectionResult output;
			if (target instanceof GeoLocusV2) {
				AlgoLocusLocusIntersectionV2 pair =
						new AlgoLocusLocusIntersectionV2(construction, label,
								source, (GeoLocusV2) target, outputId);
				algorithm = pair;
				output = pair.getResult();
			} else {
				AlgoLocusIntersectionV2 single = new AlgoLocusIntersectionV2(
						construction, label, source, target, outputId);
				algorithm = single;
				output = single.getResult();
			}
			finishLabel(output, label, false);
			if (!construction.isFileLoading()) {
				batch.stageReserved(output, outputId, dependencies);
				batch.publish();
			}
			return output;
		} catch (RuntimeException exception) {
			batch.abandonBeforePublication();
			removeFailed(algorithm);
			throw exception;
		}
	}

	/**
	 * Creates an exact-token derived point from one rich result.
	 *
	 * @return exact-token derived point
	 */
	public static GeoPoint selectIntersectionPoint(Construction construction,
			String label, GeoLocusIntersectionResult result, GeoText token) {
		requireAccess(construction);
		ParticipationBatch batch = new ParticipationBatch(construction);
		List<GeoElement> dependencies = direct(result, token);
		if (!construction.isFileLoading()) {
			batch.prepareAll(dependencies);
		}
		PersistentGeoId outputId = batch.reserveOutput();
		AlgoLocusIntersectionPointV2 algorithm = null;
		try {
			algorithm = new AlgoLocusIntersectionPointV2(construction, label,
					result, token);
			GeoPoint output = algorithm.getPoint();
			finishLabel(output, label, false);
			if (!construction.isFileLoading()) {
				batch.stageReserved(output, outputId, dependencies);
				batch.publish();
			}
			return output;
		} catch (RuntimeException exception) {
			batch.abandonBeforePublication();
			removeFailed(algorithm);
			throw exception;
		}
	}

	private static GeoLocusMetricResult totalMetric(Construction construction,
			String label, GeoLocusV2 source, boolean auxiliary) {
		requireAccess(construction);
		ParticipationBatch batch = new ParticipationBatch(construction);
		List<GeoElement> dependencies = direct(source);
		if (!construction.isFileLoading()) {
			batch.prepareAll(dependencies);
		}
		PersistentGeoId outputId = batch.reserveOutput();
		AlgoLocusMetricV2 algorithm = null;
		try {
			algorithm = new AlgoLocusMetricV2(construction, label, source, outputId);
			GeoLocusMetricResult output = algorithm.getResult();
			finishLabel(output, label, auxiliary);
			if (!construction.isFileLoading()) {
				batch.stageReserved(output, outputId, dependencies);
				batch.publish();
			}
			return output;
		} catch (RuntimeException exception) {
			batch.abandonBeforePublication();
			removeFailed(algorithm);
			throw exception;
		}
	}

	private static void requireAccess(Construction construction) {
		construction.getSpatialIdentityRegistry()
				.requireCandidateParticipationCallerAllowed();
		if (!RuntimeFeatureService.mayUseLocusV2(construction)) {
			throw new IllegalArgumentException(
					"The experimental Locus V2 surface is not enabled");
		}
	}

	private static void finishLabel(GeoElement geo, String label,
			boolean auxiliary) {
		// Persist the canonical concrete mode. LABEL_DEFAULT (4) is normalized to
		// LABEL_NAME (0) by the ordinary XML reader, so leaving the transient
		// default here would make save/reopen/save and lifecycle rollback differ.
		if (geo.getLabelMode() == GeoElement.LABEL_DEFAULT) {
			geo.setLabelMode(GeoElement.LABEL_NAME);
		}
		if (geo.getConstruction().getSpatialIdentityRegistry()
				.isRedefineCandidateParticipationActive()) {
			// Candidate parsing is pre-provider. The sealed transaction activates
			// ordinary persistence labels only after an admitted decision.
			geo.setAuxiliaryObject(auxiliary);
			return;
		}
		if (!geo.isLabelSet()) {
			geo.setLabel(label);
		}
		if (!geo.isLabelSet()) {
			geo.setLoadedLabel(geo.getFreeLabel(label));
		}
		if (!geo.isLabelSet()) {
			throw new IllegalArgumentException(
					"A public persistent output requires an ordinary label");
		}
		geo.setAuxiliaryObject(auxiliary);
	}

	private static void removeFailed(AlgoElement algorithm) {
		if (algorithm != null) {
			algorithm.remove();
		}
	}

	private static List<GeoElement> direct(GeoElement... geos) {
		ArrayList<GeoElement> result = new ArrayList<>();
		Map<GeoElement, Boolean> seen = new IdentityHashMap<>();
		for (GeoElement geo : geos) {
			if (geo != null && seen.put(geo, Boolean.TRUE) == null) {
				result.add(geo);
			}
		}
		return Collections.unmodifiableList(result);
	}

	private static final class PointDriver {
		private final GeoPoint state;
		private final GeoElement coordinate;
		private final GeoElement support;
		private final GeoText branch;
		private final SemanticGeneratorFamily1D family;

		private PointDriver(GeoPoint state, GeoElement coordinate,
				GeoElement support,
				GeoText branch, SemanticGeneratorFamily1D family) {
			this.state = state;
			this.coordinate = coordinate;
			this.support = support;
			this.branch = branch;
			this.family = family;
		}

		private static PointDriver resolve(GeoPoint point) {
			if (point.getParentAlgorithm() instanceof AlgoSemanticLocusPoint2D) {
				AlgoSemanticLocusPoint2D parent =
						(AlgoSemanticLocusPoint2D) point.getParentAlgorithm();
				return new PointDriver(point,
						parent.getParameterInput().toGeoElement(),
						parent.getSource(), parent.getBranchInput(),
						SemanticGeneratorFamily1D.LOCUS_BRANCH_POINT);
			}
			if (!point.isPointOnPath()) {
				throw new IllegalArgumentException(
						"The point generator needs one approved typed support");
			}
			Path path = point.getPath();
			if (!(path instanceof GeoElement)) {
				throw new IllegalArgumentException(
						"The point support is not a serializable GeoElement");
			}
			GeoElement support = (GeoElement) path;
			SemanticGeneratorFamily1D family;
			if (support instanceof GeoSegment) {
				family = SemanticGeneratorFamily1D.SEGMENT_POINT;
			} else if (support instanceof GeoConicPart
					&& ((GeoConicPart) support).getConicPartType()
							== GeoConicNDConstants.CONIC_PART_ARC
					&& ((GeoConicPart) support).getType()
							== GeoConicNDConstants.CONIC_CIRCLE) {
				family = SemanticGeneratorFamily1D.CIRCULAR_ARC_POINT;
			} else if (support instanceof GeoConic
					&& !(support instanceof GeoConicPart)
					&& ((GeoConic) support).isCircle()) {
				family = SemanticGeneratorFamily1D.CIRCLE_POINT;
			} else {
				throw new IllegalArgumentException(
						"Unsupported point-driver support family");
			}
			return new PointDriver(point, point, support, null, family);
		}

		private List<GeoElement> dependencies(GeoPoint dependent) {
			return direct(dependent, state, coordinate, support, branch);
		}
	}

	private static final class ParticipationBatch {
		private final Construction construction;
		private final SpatialIdentityRegistry registry;
		private final Map<GeoElement, PersistentGeoId> stagedIds =
				new IdentityHashMap<>();
		private final Map<GeoElement, List<GeoElement>> explicitDependencies =
				new IdentityHashMap<>();
		private final LinkedHashSet<PersistentGeoId> reservations =
				new LinkedHashSet<>();
		private final List<PromotedGeo> promotions = new ArrayList<>();
		private boolean published;

		private ParticipationBatch(Construction construction) {
			this.construction = construction;
			registry = construction.getSpatialIdentityRegistry();
		}

		private void prepareAll(List<GeoElement> geos) {
			for (GeoElement geo : geos) {
				prepare(geo);
			}
		}

		private PersistentGeoId prepare(GeoElement geo) {
			PersistentGeoId existing = registry.getPersistentGeoId(geo);
			if (existing != null) {
				return existing;
			}
			PersistentGeoId sharedStaged =
					registry.getStagedRedefineCandidateIdentity(geo);
			if (sharedStaged != null) {
				return sharedStaged;
			}
			PersistentGeoId staged = stagedIds.get(geo);
			if (staged != null) {
				return staged;
			}
			boolean alreadyInConstruction = construction.isInConstructionList(geo);
			boolean alreadyAuxiliary = geo.isAuxiliaryObject();
			boolean promoted = !geo.isLabelSet();
			finishLabel(geo, null, geo.isAuxiliaryObject());
			if (promoted) {
				promotions.add(new PromotedGeo(geo, alreadyInConstruction,
						alreadyAuxiliary));
			}
			PersistentGeoId reserved = registry.allocatePersistentGeoId();
			stagedIds.put(geo, reserved);
			reservations.add(reserved);
			explicitDependencies.put(geo, parentDependencies(geo));
			return reserved;
		}

		private PersistentGeoId reserveOutput() {
			if (construction.isFileLoading()) {
				return null;
			}
			PersistentGeoId reserved = registry.allocatePersistentGeoId();
			reservations.add(reserved);
			return reserved;
		}

		private void stageReserved(GeoElement geo, PersistentGeoId id,
				List<GeoElement> dependencies) {
			if (id == null || registry.getPersistentGeoId(geo) != null
					|| stagedIds.containsKey(geo)) {
				throw new IllegalArgumentException(
						"Invalid public output identity reservation");
			}
			stagedIds.put(geo, id);
			explicitDependencies.put(geo,
					Collections.unmodifiableList(new ArrayList<>(dependencies)));
		}

		private List<GeoElement> parentDependencies(GeoElement geo) {
			AlgoElement parent = geo.getParentAlgorithm();
			if (parent == null) {
				return Collections.emptyList();
			}
			return direct(parent.getInput());
		}

		private void publish() {
			Map<GeoElement, GeoIdentityRecord> participations =
					new LinkedHashMap<>();
			for (Map.Entry<GeoElement, PersistentGeoId> staged
					: stagedIds.entrySet()) {
				GeoElement geo = staged.getKey();
				ArrayList<PersistentGeoId> dependencyIds = new ArrayList<>();
				for (GeoElement dependency : explicitDependencies.get(geo)) {
					PersistentGeoId dependencyId = currentId(dependency);
					if (dependencyId != null && !dependencyId.equals(staged.getValue())
							&& !dependencyIds.contains(dependencyId)) {
						dependencyIds.add(dependencyId);
					}
				}
				Collections.sort(dependencyIds);
				participations.put(geo, record(staged.getValue(), geo,
						dependencyIds));
			}
			if (registry.isRedefineCandidateParticipationActive()) {
				registry.stageRedefineCandidateParticipations(participations);
				published = true;
				reservations.clear();
			} else {
				try {
					registry.registerConstructionParticipations(participations);
					published = true;
				} finally {
					// The ordinary registration path owns rollback of every supplied
					// reservation when its atomic publication rejects.
					reservations.clear();
				}
			}
		}

		private PersistentGeoId currentId(GeoElement geo) {
			PersistentGeoId staged = stagedIds.get(geo);
			if (staged != null) {
				return staged;
			}
			PersistentGeoId shared =
					registry.getStagedRedefineCandidateIdentity(geo);
			return shared == null ? registry.getPersistentGeoId(geo) : shared;
		}

		private GeoIdentityRecord record(PersistentGeoId id, GeoElement geo,
				List<PersistentGeoId> dependencies) {
			return new GeoIdentityRecord(id,
					ConstructionGeoRedefineProvider.PROVIDER_ID,
					ConstructionGeoRedefineProvider.familyFor(geo),
					ConstructionGeoRedefineProvider.SCHEMA_ID,
					ConstructionGeoRedefineProvider.SCHEMA_VERSION,
					EditAuthorityMode.CONSTRUCTION_DEFINED,
					ProjectionBindingRole.NOT_APPLICABLE,
					ConstructionGeoRedefineProvider.STABLE_OUTPUT_ROLE, 1,
					dependencies, 0, 0);
		}

		private void abandonBeforePublication() {
			if (!published && !reservations.isEmpty()) {
				registry.abandonReservedConstructionIdentities(reservations);
				reservations.clear();
			}
			if (!published) {
				for (int index = promotions.size() - 1; index >= 0; index--) {
					promotions.get(index).rollback(construction);
				}
				promotions.clear();
			}
		}
	}

	private static final class PromotedGeo {
		private final GeoElement geo;
		private final ConstructionElement constructionElement;
		private final boolean alreadyInConstruction;
		private final boolean auxiliary;

		private PromotedGeo(GeoElement geo, boolean alreadyInConstruction,
				boolean auxiliary) {
			this.geo = geo;
			constructionElement = geo.isIndependent() ? geo
					: geo.getParentAlgorithm();
			this.alreadyInConstruction = alreadyInConstruction;
			this.auxiliary = auxiliary;
		}

		private void rollback(Construction construction) {
			construction.removeLabel(geo);
			geo.setLabelSimple(null);
			geo.setLabelSet(false);
			geo.setLabelWanted(false);
			geo.setAuxiliaryObject(auxiliary);
			if (!alreadyInConstruction && constructionElement != null) {
				construction.removeFromConstructionList(constructionElement);
			}
		}
	}
}
