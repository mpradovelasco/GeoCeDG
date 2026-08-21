/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.ConstructionFidelity;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationMethod;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.RepresentationRole;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.MacroKernel;
import org.geogebra.common.kernel.Path;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.ConstructionElement;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoConicPart;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.kernelND.GeoConicNDConstants;
import org.geogebra.common.kernel.kernelND.GeoPointND;

/**
 * Reconstructible isolated construction-slice evaluator. Copies are resolved
 * only through durable IDs; labels, coordinates, samples and XML order are not
 * identity authority.
 */
public final class ReconstructibleLocusEvaluator2D implements LocusEvaluator2D {
	private static final LocusQuality2D QUALITY = new LocusQuality2D(
			ConstructionFidelity.SEMANTICALLY_CONSTRUCTED,
			EvaluationMethod.DETERMINISTIC_NUMERIC_DEPENDENCY,
			RepresentationRole.SEMANTIC_RESULT,
			NumericGuarantee.FLOATING_POINT_UNCERTIFIED);

	private final SemanticGeneratorDescriptor1D descriptor;
	private final Construction isolatedConstruction;
	private final Map<PersistentGeoId, GeoElement> originals = new LinkedHashMap<>();
	private final Map<PersistentGeoId, GeoElement> isolated = new LinkedHashMap<>();
	private final GeoPointND isolatedDependentPoint;
	private final GeoElement isolatedState;
	private final GeoElement isolatedCoordinate;
	private final GeoElement isolatedSupport;

	/** Builds one immutable identity-addressed construction slice. */
	public ReconstructibleLocusEvaluator2D(Construction construction,
			SemanticGeneratorDescriptor1D descriptor,
			Collection<GeoElement> reconstructionSlice) {
		Objects.requireNonNull(construction);
		this.descriptor = Objects.requireNonNull(descriptor);
		SpatialIdentityRegistry sourceRegistry = construction
				.getSpatialIdentityRegistry();
		Set<ConstructionElement> elements = new TreeSet<>();
		Set<Long> usedAlgorithmIds = new TreeSet<>();
		for (GeoElement sliceGeo : Objects.requireNonNull(reconstructionSlice)) {
			GeoElement current = Objects.requireNonNull(sliceGeo);
			if (current.getConstruction() != construction) {
				throw new IllegalArgumentException(
						"Evaluator reconstruction slice crosses constructions");
			}
			// The isolated MacroKernel supplies its own canonical constants.
			if (construction.isConstantElement(current)) {
				continue;
			}
			elements.add(current);
			AlgoElement parent = current.getParentAlgorithm();
			if (parent != null && parent.isInConstructionList()) {
				Macro.addDependentAlgo(parent, elements, usedAlgorithmIds);
			}
		}
		for (PersistentGeoId id : descriptor.getDependencyIds()) {
			GeoElement original = sourceRegistry.getGeo(id);
			if (original == null) {
				throw new IllegalArgumentException(
						"Missing active generator dependency: " + id);
			}
			originals.put(id, original);
			elements.add(original);
			AlgoElement parent = original.getParentAlgorithm();
			if (parent != null && parent.isInConstructionList()) {
				Macro.addDependentAlgo(parent, elements, usedAlgorithmIds);
			}
		}
		MacroKernel macroKernel = construction.getKernel().newMacroKernel();
		macroKernel.setGlobalVariableLookup(false);
		try {
			macroKernel.loadXML(Macro.buildMacroXML(construction.getKernel(), elements)
					.toString());
		} catch (Exception exception) {
			throw new IllegalArgumentException(
					"Could not reconstruct the durable Locus V2 evaluator slice",
					exception);
		}
		isolatedConstruction = macroKernel.getConstruction();
		SpatialIdentityRegistry isolatedRegistry = isolatedConstruction
				.getSpatialIdentityRegistry();
		for (PersistentGeoId id : descriptor.getDependencyIds()) {
			GeoElement copy = isolatedRegistry.getGeo(id);
			if (copy == null) {
				throw new IllegalArgumentException(
						"Reconstructed evaluator omitted durable dependency: " + id);
			}
			isolated.put(id, copy);
		}
		GeoElement dependent = isolated.get(descriptor.getDependentPointId());
		if (!(dependent instanceof GeoPointND) || dependent.isGeoElement3D()) {
			throw new IllegalArgumentException(
					"The dependent evaluator output must be a finite 2D point");
		}
		isolatedDependentPoint = (GeoPointND) dependent;
		isolatedState = requiredCopy(descriptor.getStateId());
		isolatedCoordinate = requiredCopy(descriptor.getCoordinateId());
		isolatedSupport = descriptor.getSupportId() == null ? null
				: requiredCopy(descriptor.getSupportId());
		validateTypedFamily();
		resetExternalState();
	}

	@Override
	public synchronized LocusEvaluation2D evaluate(LocusDefinition2D definition,
			LocusBranch2D branch, double canonicalParameter,
			LocusEvaluationSession2D session) {
		try {
			resetExternalState();
			applyCoordinate(canonicalParameter);
			GeoElement pointGeo = isolatedDependentPoint.toGeoElement();
			if (!pointGeo.isDefined() || isolatedDependentPoint.isInfinite()
					|| !Double.isFinite(isolatedDependentPoint.getInhomX())
					|| !Double.isFinite(isolatedDependentPoint.getInhomY())) {
				return LocusEvaluation2D.invalid(
						LocusSemanticMetadata2D.EvaluationStatus.DEPENDENCY_UNDEFINED,
						QUALITY, "Dependent point is undefined or nonfinite");
			}
			return LocusEvaluation2D.valid(new LocusPoint2D(
					isolatedDependentPoint.getInhomX(),
					isolatedDependentPoint.getInhomY()), Regularity.UNKNOWN, QUALITY);
		} catch (RuntimeException exception) {
			return LocusEvaluation2D.invalid(
					LocusSemanticMetadata2D.EvaluationStatus.EVALUATION_FAILED,
					QUALITY, exception.getClass().getSimpleName() + ": "
							+ exception.getMessage());
		}
	}

	/** @return deterministic evaluator reconstruction signature */
	public String getEvaluatorSignature() {
		return "reconstructible-construction-slice/v1|"
				+ descriptor.getSemanticSignature();
	}

	private void applyCoordinate(double parameter) {
		switch (descriptor.getFamily()) {
		case SCALAR_STATE:
		case LOCUS_BRANCH_POINT:
			if (!(isolatedCoordinate instanceof GeoNumeric)) {
				throw new IllegalStateException(
						"Scalar coordinate copy is not numeric");
			}
			((GeoNumeric) isolatedCoordinate).setValue(parameter);
			isolatedCoordinate.updateCascade();
			break;
		case SEGMENT_POINT:
		case CIRCLE_POINT:
		case CIRCULAR_ARC_POINT:
			GeoPointND point = (GeoPointND) isolatedState;
			point.getPathParameter().setT(parameter);
			((Path) isolatedSupport).pathChanged(point);
			point.toGeoElement().updateCascade();
			break;
		default:
			throw new IllegalStateException("Unsupported semantic generator family");
		}
	}

	private void resetExternalState() {
		for (Map.Entry<PersistentGeoId, GeoElement> entry : originals.entrySet()) {
			GeoElement original = entry.getValue();
			GeoElement copy = isolated.get(entry.getKey());
			if (!original.isIndependent() || original.isGeoFunction()
					|| copy == isolatedCoordinate) {
				continue;
			}
			copy.set(original);
			copy.update();
		}
		isolatedConstruction.updateConstruction(false);
	}

	private void validateTypedFamily() {
		switch (descriptor.getFamily()) {
		case SCALAR_STATE:
			if (!(isolatedState instanceof GeoNumeric)
					|| !(isolatedCoordinate instanceof GeoNumeric)) {
				throw new IllegalArgumentException(
						"Scalar state and true coordinate must be numeric");
			}
			break;
		case SEGMENT_POINT:
			requirePointOnSupport(GeoSegment.class);
			break;
		case CIRCLE_POINT:
			requirePointOnSupport(GeoConic.class);
			if (isolatedSupport instanceof GeoConicPart
					|| ((GeoConic) isolatedSupport).getType()
							!= GeoConicNDConstants.CONIC_CIRCLE) {
				throw new IllegalArgumentException(
						"Circle generator requires one complete circle");
			}
			break;
		case CIRCULAR_ARC_POINT:
			requirePointOnSupport(GeoConicPart.class);
			GeoConicPart arc = (GeoConicPart) isolatedSupport;
			if (arc.getConicPartType() != GeoConicNDConstants.CONIC_PART_ARC
					|| arc.getType() != GeoConicNDConstants.CONIC_CIRCLE) {
				throw new IllegalArgumentException(
						"Circular-arc generator requires one circle arc");
			}
			break;
		case LOCUS_BRANCH_POINT:
			if (!(isolatedState instanceof GeoPointND)
					|| !(isolatedSupport instanceof GeoLocusV2)
					|| !(isolatedCoordinate instanceof GeoNumeric)) {
				throw new IllegalArgumentException(
						"Locus-support generator requires a V2 source, point and coordinate");
			}
			break;
		default:
			throw new IllegalStateException("Unsupported semantic generator family");
		}
	}

	private void requirePointOnSupport(Class<? extends GeoElement> supportType) {
		if (!(isolatedState instanceof GeoPointND)
				|| !supportType.isInstance(isolatedSupport)
				|| !((GeoPointND) isolatedState).isPointOnPath()
				|| ((GeoPointND) isolatedState).getPath() != isolatedSupport) {
			throw new IllegalArgumentException(
					"Point generator state is not constrained to its typed support");
		}
	}

	private GeoElement requiredCopy(PersistentGeoId id) {
		GeoElement copy = isolated.get(id);
		if (copy == null) {
			throw new IllegalArgumentException(
					"Missing evaluator copy for durable identity: " + id);
		}
		return copy;
	}
}
