/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.algos;

import java.util.Objects;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.SemanticGeneratorDescriptor1D;
import org.geocedg.common.kernel.locus.intersection.IntersectionEndpointProvenanceResolver2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionEndpointProvenanceResult2D;
import org.geocedg.common.kernel.locus.metric.MetricPositionBinding2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spline.SplineConstructorOccurrenceResolver2D;
import org.geocedg.common.kernel.spline.SplineConstructorOccurrenceResult2D;
import org.geogebra.common.kernel.algos.AlgoDependentPoint;
import org.geogebra.common.kernel.algos.AlgoDilate;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoMirror;
import org.geogebra.common.kernel.algos.AlgoRotate;
import org.geogebra.common.kernel.algos.AlgoRotatePoint;
import org.geogebra.common.kernel.algos.AlgoTranslate;
import org.geogebra.common.kernel.arithmetic.ExpressionValue;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.kernelND.GeoPointND;

/**
 * Ordered admission walk for between-position metric endpoints
 * ({@code locus-v2-metrics.md} section 24). Direct families are consulted
 * before derived ones and every result other than {@code NO_ADDRESS} is final.
 * Coordinates, proximity, labels, order and render state are never consulted.
 */
public final class SemanticMetricEndpointResolver2D {
	private static final String SEMANTIC_POINT_VERSION =
			"metric-endpoint/semantic-point/v1";
	private static final String GENERATOR_VERSION =
			"metric-endpoint/generator-occurrence/v1";
	private static final String IMAGE_VERSION =
			"metric-endpoint/similarity-image-occurrence/v1";
	private static final String COPY_VERSION =
			"metric-endpoint/dependent-copy-occurrence/v1";

	/** Admitted endpoint families in dispatch order. */
	public enum Family {
		SEMANTIC_POINT, PAIR_INTERSECTION_OCCURRENCE,
		SINGLE_SOURCE_INTERSECTION_OCCURRENCE, GENERATOR_OCCURRENCE,
		CONSTRUCTOR_OCCURRENCE, SIMILARITY_IMAGE_OCCURRENCE,
		DEPENDENT_COPY_OCCURRENCE
	}

	/** Closed resolution states; none authorizes coordinate repair. */
	public enum Status {
		NO_ADDRESS, UNIQUE, MULTIPLE, UNRESOLVED_INVALID
	}

	/** One typed resolution of an endpoint on an addressed source. */
	public static final class Resolution {
		private final Status status;
		private final Family family;
		private final String occurrenceKey;
		private final LocusSemanticAddress2D address;
		private final MetricPositionBinding2D binding;
		private final boolean membership;
		private final String diagnostic;

		private Resolution(Status status, Family family, String occurrenceKey,
				LocusSemanticAddress2D address, MetricPositionBinding2D binding,
				boolean membership, String diagnostic) {
			this.status = status;
			this.family = family;
			this.occurrenceKey = occurrenceKey;
			this.address = address;
			this.binding = binding;
			this.membership = membership;
			this.diagnostic = diagnostic;
		}

		static Resolution noAddress(String diagnostic) {
			return new Resolution(Status.NO_ADDRESS, null, null, null, null, false,
					diagnostic);
		}

		static Resolution failed(Status status, Family family, String diagnostic) {
			return new Resolution(status, family, null, null, null, false, diagnostic);
		}

		static Resolution unique(Family family, String key,
				LocusSemanticAddress2D address, boolean membership) {
			return new Resolution(Status.UNIQUE, family, key, address, null,
					membership, "Exactly one current " + family + " address");
		}

		public Status getStatus() {
			return status;
		}

		/** @return accepted family, or {@code null} for {@code NO_ADDRESS} */
		public Family getFamily() {
			return family;
		}

		/** @return versioned in-memory key; {@code null} for a top-level semantic point */
		public String getOccurrenceKey() {
			return occurrenceKey;
		}

		/** @return current address on the addressed source, when resolved */
		public LocusSemanticAddress2D getAddress() {
			return address;
		}

		/** @return the semantic point's own current binding (top level only) */
		public MetricPositionBinding2D getBinding() {
			return binding;
		}

		/** @return whether the endpoint lies on the addressed source by construction */
		public boolean isMembership() {
			return membership;
		}

		public String getDiagnostic() {
			return diagnostic;
		}
	}

	private final IntersectionEndpointProvenanceResolver2D intersections =
			new IntersectionEndpointProvenanceResolver2D();
	private final SplineConstructorOccurrenceResolver2D constructors =
			new SplineConstructorOccurrenceResolver2D();

	/**
	 * Resolves one endpoint for {@code Length(S,P,Q)} / {@code LocusLength(S,P,Q)}.
	 *
	 * @return the first non-{@code NO_ADDRESS} family result, or {@code NO_ADDRESS}
	 */
	public Resolution resolve(GeoLocusV2 source, GeoPoint endpoint) {
		if (source == null || endpoint == null) {
			return Resolution.failed(Status.UNRESOLVED_INVALID, null,
					"A source and endpoint are required");
		}
		return resolve(source, endpoint, 0, true);
	}

	private Resolution resolve(GeoLocusV2 source, GeoPoint endpoint, int depth,
			boolean topLevel) {
		if (depth > LocusEvaluationSession2D.MAXIMUM_SAFE_ACTIVE_DEPTH) {
			return Resolution.failed(Status.UNRESOLVED_INVALID, null,
					"Endpoint derivation exceeds the safe composition depth");
		}
		Resolution direct = semanticPoint(source, endpoint, topLevel);
		if (direct.status != Status.NO_ADDRESS) {
			return direct;
		}
		IntersectionEndpointProvenanceResult2D pair =
				intersections.resolve(source, endpoint);
		if (pair.getStatus() != IntersectionEndpointProvenanceResult2D.Status.NO_ADDRESS) {
			// UNIQUE, MULTIPLE and UNRESOLVED_INVALID are final (sections 23.6, 24.2).
			return fromIntersection(Family.PAIR_INTERSECTION_OCCURRENCE, pair);
		}
		IntersectionEndpointProvenanceResult2D single =
				intersections.resolveSingleSource(source, endpoint);
		if (single.getStatus()
				!= IntersectionEndpointProvenanceResult2D.Status.NO_ADDRESS) {
			return fromIntersection(Family.SINGLE_SOURCE_INTERSECTION_OCCURRENCE,
					single);
		}
		direct = generator(source, endpoint);
		if (direct.status != Status.NO_ADDRESS) {
			return direct;
		}
		direct = constructor(source, endpoint);
		if (direct.status != Status.NO_ADDRESS) {
			return direct;
		}
		// Derived families only after every direct family (section 24.2).
		Resolution derived = image(source, endpoint, depth);
		if (derived.status != Status.NO_ADDRESS) {
			return derived;
		}
		derived = copy(source, endpoint, depth);
		if (derived.status != Status.NO_ADDRESS) {
			return derived;
		}
		return Resolution.noAddress(
				"No admitted endpoint family addresses this source; "
						+ direct.diagnostic);
	}

	private static Resolution semanticPoint(GeoLocusV2 source, GeoPoint endpoint,
			boolean topLevel) {
		AlgoElement parent = endpoint.getParentAlgorithm();
		if (!(parent instanceof AlgoSemanticLocusPoint2D)
				|| ((AlgoSemanticLocusPoint2D) parent).getSource() != source) {
			return Resolution.noAddress("The endpoint is not a semantic point of the source");
		}
		AlgoSemanticLocusPoint2D semantic = (AlgoSemanticLocusPoint2D) parent;
		MetricPositionBinding2D binding = semantic.bindCurrentPosition();
		LocusSemanticAddress2D address = semantic.getCurrentSemanticAddress();
		if (binding == null || !binding.isValid() || address == null) {
			return Resolution.failed(Status.UNRESOLVED_INVALID, Family.SEMANTIC_POINT,
					"Explicit semantic address is not current");
		}
		if (topLevel) {
			return new Resolution(Status.UNIQUE, Family.SEMANTIC_POINT, null, address,
					binding, true, "Explicit current semantic point");
		}
		PersistentGeoId sourceId = source.getPersistentLocusId();
		PersistentGeoId pointId = identity(endpoint);
		if (sourceId == null || pointId == null) {
			return Resolution.failed(Status.UNRESOLVED_INVALID, Family.SEMANTIC_POINT,
					"Durable source and point identities are required");
		}
		return Resolution.unique(Family.SEMANTIC_POINT, SEMANTIC_POINT_VERSION
				+ "|addressed-source=" + sourceId.toExternalForm() + "|point="
				+ pointId.toExternalForm(), address, true);
	}

	private static Resolution fromIntersection(Family family,
			IntersectionEndpointProvenanceResult2D result) {
		switch (result.getStatus()) {
		case UNIQUE:
			IntersectionEndpointProvenanceResult2D.Match match = result.getUniqueMatch();
			return Resolution.unique(family, match.getOccurrenceKey(),
					match.getAddress(), true);
		case MULTIPLE:
			return Resolution.failed(Status.MULTIPLE, family, result.getDiagnostic());
		default:
			return Resolution.failed(Status.UNRESOLVED_INVALID, family,
					result.getDiagnostic());
		}
	}

	/** Section 24.5: the dependent generator point of S at the live driver value. */
	private static Resolution generator(GeoLocusV2 source, GeoPoint endpoint) {
		AlgoElement parent = source.getParentAlgorithm();
		if (!(parent instanceof AlgoDependentPointLocusV2)
				|| parent.getInput(0).toGeoElement() != endpoint) {
			return Resolution.noAddress("The endpoint is not the generator of the source");
		}
		SemanticGeneratorDescriptor1D descriptor =
				((AlgoDependentPointLocusV2) parent).getGeneratorDescriptor();
		LocusDefinition2D definition = source.getSemanticDefinition();
		PersistentGeoId sourceId = source.getPersistentLocusId();
		PersistentGeoId pointId = identity(endpoint);
		if (descriptor == null || definition == null || !source.isDefined()
				|| sourceId == null || pointId == null
				|| !pointId.equals(descriptor.getDependentPointId())) {
			return Resolution.failed(Status.UNRESOLVED_INVALID,
					Family.GENERATOR_OCCURRENCE, "The generator descriptor is not current");
		}
		double raw = liveParameter(descriptor,
				source.getConstruction().getSpatialIdentityRegistry());
		LocusSemanticAddress2D address = !Double.isFinite(raw) ? null
				: IntersectionEndpointProvenanceResolver2D.addressFromRawParameter(
						definition, sourceId, SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY,
						raw);
		if (address == null) {
			return Resolution.failed(Status.UNRESOLVED_INVALID,
					Family.GENERATOR_OCCURRENCE,
					"The live driver value has no unique current component");
		}
		return Resolution.unique(Family.GENERATOR_OCCURRENCE, GENERATOR_VERSION
				+ "|addressed-source=" + sourceId.toExternalForm() + "|point="
				+ pointId.toExternalForm(), address, true);
	}

	/** @return the raw parameter the evaluator itself applies, or NaN */
	private static double liveParameter(SemanticGeneratorDescriptor1D descriptor,
			SpatialIdentityRegistry registry) {
		switch (descriptor.getFamily()) {
		case CIRCLE_POINT:
		case SEGMENT_POINT:
		case CIRCULAR_ARC_POINT:
			GeoElement state = registry.getGeo(descriptor.getStateId());
			if (!(state instanceof GeoPointND) || !state.isDefined()
					|| !((GeoPointND) state).isPointOnPath()) {
				return Double.NaN;
			}
			return ((GeoPointND) state).getPathParameter().getT();
		case SCALAR_STATE:
		case LOCUS_BRANCH_POINT:
			GeoElement coordinate = registry.getGeo(descriptor.getCoordinateId());
			return coordinate instanceof GeoNumeric && coordinate.isDefined()
					? ((GeoNumeric) coordinate).getDouble() : Double.NaN;
		default:
			return Double.NaN;
		}
	}

	/** Section 21, unchanged; membership only on the root spline itself. */
	private Resolution constructor(GeoLocusV2 source, GeoPoint endpoint) {
		SplineConstructorOccurrenceResult2D result =
				constructors.resolve(source, endpoint);
		switch (result.getStatus()) {
		case NO_ADDRESS:
			return Resolution.noAddress(result.getStatus() + ": " + result.getDiagnostic());
		case UNIQUE:
			SplineConstructorOccurrenceResult2D.Match match = result.getUniqueMatch();
			return Resolution.unique(Family.CONSTRUCTOR_OCCURRENCE,
					match.getOccurrenceKey(), match.getAddress(),
					source.getParentAlgorithm() instanceof AlgoSplineV2);
		case MULTIPLE:
			return Resolution.failed(Status.MULTIPLE, Family.CONSTRUCTOR_OCCURRENCE,
					result.getDiagnostic());
		default:
			return Resolution.failed(Status.UNRESOLVED_INVALID,
					Family.CONSTRUCTOR_OCCURRENCE, result.getDiagnostic());
		}
	}

	/** Section 24.6: the same similarity, with the same parameter objects. */
	private Resolution image(GeoLocusV2 source, GeoPoint endpoint, int depth) {
		AlgoElement pointParent = endpoint.getParentAlgorithm();
		AlgoElement sourceParent = source.getParentAlgorithm();
		if (!(sourceParent instanceof AlgoLocusSimilarityTransform2D)
				|| !isPointSimilarity(pointParent)
				|| pointParent.getClassName() != sourceParent.getClassName()
				|| pointParent.getInputLength() != sourceParent.getInputLength()) {
			return Resolution.noAddress("The endpoint is not an image under the source map");
		}
		for (int index = 1; index < pointParent.getInputLength(); index++) {
			if (pointParent.getInput(index).toGeoElement()
					!= sourceParent.getInput(index).toGeoElement()) {
				return Resolution.noAddress(
						"Transform parameters are not the same construction objects");
			}
		}
		GeoElement preImage = pointParent.getInput(0).toGeoElement();
		if (!(preImage instanceof GeoPoint) || preImage.isGeoElement3D()) {
			return Resolution.noAddress("The transformed object is not a 2D point");
		}
		Resolution inner = resolve(
				((AlgoLocusSimilarityTransform2D) sourceParent).getSource(),
				(GeoPoint) preImage, depth + 1, false);
		return derive(Family.SIMILARITY_IMAGE_OCCURRENCE, IMAGE_VERSION, source,
				endpoint, inner, depth);
	}

	/** Section 24.7: a dependent copy whose definition is exactly one point. */
	private Resolution copy(GeoLocusV2 source, GeoPoint endpoint, int depth) {
		AlgoElement parent = endpoint.getParentAlgorithm();
		if (parent == null || parent.getClass() != AlgoDependentPoint.class
				|| endpoint.getDefinition() == null) {
			return Resolution.noAddress("The endpoint is not a dependent copy");
		}
		ExpressionValue copied = endpoint.getDefinition().unwrap();
		if (!(copied instanceof GeoPoint) || copied == endpoint
				|| ((GeoPoint) copied).isGeoElement3D()) {
			return Resolution.noAddress("The dependent point is not an exact copy");
		}
		Resolution inner = resolve(source, (GeoPoint) copied, depth + 1, false);
		return derive(Family.DEPENDENT_COPY_OCCURRENCE, COPY_VERSION, source,
				endpoint, inner, depth);
	}

	private static Resolution derive(Family family, String version,
			GeoLocusV2 source, GeoPoint endpoint, Resolution inner, int depth) {
		if (inner.status == Status.NO_ADDRESS) {
			return Resolution.noAddress("The derived point's origin is not an endpoint");
		}
		if (inner.status != Status.UNIQUE) {
			return Resolution.failed(inner.status, family, inner.diagnostic);
		}
		if (!inner.membership) {
			return Resolution.noAddress(
					"The origin occurrence is a transport, not membership");
		}
		PersistentGeoId sourceId = source.getPersistentLocusId();
		PersistentGeoId pointId = identity(endpoint);
		// Only the endpoint itself needs durable identity: the DAG fixes every
		// intermediate derivation step below it.
		if (sourceId == null || pointId == null && depth == 0
				|| inner.occurrenceKey == null) {
			return Resolution.failed(Status.UNRESOLVED_INVALID, family,
					"Durable source, point and origin identities are required");
		}
		LocusSemanticAddress2D origin = inner.address;
		LocusSemanticAddress2D address = new LocusSemanticAddress2D(sourceId,
				origin.getProviderVersion(), origin.getBranchKey(),
				origin.getComponentLineageKey(), origin.getCanonicalParameter(),
				origin.getPeriodicLift(), origin.getSeamSide());
		return Resolution.unique(family, version + "|addressed-source="
				+ sourceId.toExternalForm() + "|point="
				+ (pointId == null ? "derived" : pointId.toExternalForm())
				+ "|inner=" + inner.occurrenceKey, address, true);
	}

	private static boolean isPointSimilarity(AlgoElement parent) {
		if (parent == null) {
			return false;
		}
		Class<?> type = parent.getClass();
		return type == AlgoTranslate.class || type == AlgoRotate.class
				|| type == AlgoRotatePoint.class || type == AlgoMirror.class
				|| type == AlgoDilate.class;
	}

	private static PersistentGeoId identity(GeoPoint point) {
		return Objects.requireNonNull(point).getConstruction()
				.getSpatialIdentityRegistry().getPersistentGeoId(point);
	}
}
