/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.algos;

import java.util.ArrayList;
import java.util.EnumSet;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSimilarityEvaluator2D;
import org.geocedg.common.kernel.locus.LocusSimilarityTransform2D;
import org.geocedg.common.kernel.locus.LocusSimilarityTransform2D.Kind;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoVec3D;

/** Reconstructible normal-DAG parent for ordinary similarity images of Locus V2. */
public final class AlgoLocusSimilarityTransform2D extends AlgoLocusV2 {
	private static final String PROVENANCE = "locus-similarity-image/v1";

	private final GeoLocusV2 source;
	private final Commands command;
	private final GeoElement[] parameters;
	private LocusDefinition2D sourceSnapshot;
	private LocusSimilarityTransform2D transformSnapshot;

	/** Creates one public parent from the exact ordinary command arguments. */
	public AlgoLocusSimilarityTransform2D(Construction construction,
			Commands command, GeoLocusV2 source, GeoElement... parameters) {
		super(construction, commandInputs(source, parameters));
		this.command = requireCommand(command);
		this.source = source;
		this.parameters = parameters.clone();
		validateShape();
		publishInitialSnapshot();
	}

	@Override
	protected boolean isSemanticPublicationReady() {
		if (getLocus().getPersistentLocusId() == null || source == null
				|| !source.isDefined()) {
			return false;
		}
		for (GeoElement parameter : parameters) {
			if (parameter == null || !parameter.isDefined()) {
				return false;
			}
		}
		try {
			sourceSnapshot = source.getSemanticDefinition();
			transformSnapshot = captureTransform();
			return sourceSnapshot != null;
		} catch (RuntimeException exception) {
			return false;
		}
	}

	@Override
	protected LocusDefinition2D createCandidate(long candidateRevision) {
		LocusSimilarityEvaluator2D evaluator = new LocusSimilarityEvaluator2D(
				sourceSnapshot, transformSnapshot);
		ArrayList<LocusBranch2D> branches = new ArrayList<>();
		for (LocusBranch2D sourceBranch : sourceSnapshot.getBranches()) {
			EnumSet<BranchProperty> properties = sourceBranch.getProperties().isEmpty()
					? EnumSet.noneOf(BranchProperty.class)
					: EnumSet.copyOf(sourceBranch.getProperties());
			if (transformSnapshot.isCollapsed()
					&& !sourceBranch.getValidDomainComponents().isEmpty()) {
				properties.add(BranchProperty.COLLAPSED_IMAGE);
			}
			LocusQuality2D quality = new LocusQuality2D(
					sourceBranch.getQuality().getConstructionFidelity(),
					sourceBranch.getQuality().getEvaluationMethod(),
					sourceBranch.getQuality().getRepresentationRole(),
					NumericGuarantee.FLOATING_POINT_UNCERTIFIED);
			branches.add(new LocusBranch2D(sourceBranch.getBranchKey(),
					sourceBranch.getDeclaredDriverDomain(),
					sourceBranch.getValidDomainComponents(),
					sourceBranch.getOrientation(),
					PROVENANCE + "|source=" + sourceSnapshot.getLocusIdentity()
							+ "|kind=" + transformSnapshot.getKind()
							+ "|source-provenance="
							+ sourceBranch.getProvenance(),
					sourceBranch.getLineage(), properties, quality));
		}
		return new LocusDefinition2D(getLocus().getLocusIdentity(),
				candidateRevision, sourceSnapshot.getDefinitionStatus(),
				sourceSnapshot.getProvider(), branches, evaluator,
				sourceSnapshot.getDeterminism(), evaluator.getEvaluatorSignature(),
				getLocus().getInstrumentation());
	}

	@Override
	public Commands getClassName() {
		return command;
	}

	/** @return exact source and transform-input dependencies */
	public GeoElement[] getDurableDependencyGeos() {
		return getInput().clone();
	}

	/** @return transformation captured for the current published revision */
	public LocusSimilarityTransform2D getTransformSnapshot() {
		return transformSnapshot;
	}

	private LocusSimilarityTransform2D captureTransform() {
		switch (command) {
		case Translate:
			GeoVec3D vector = (GeoVec3D) parameters[0];
			return LocusSimilarityTransform2D.translation(vector.getX(),
					vector.getY());
		case Rotate:
			double angle = ((GeoNumberValue) parameters[0]).getDouble();
			return parameters.length == 1
					? LocusSimilarityTransform2D.rotation(angle, 0, 0)
					: LocusSimilarityTransform2D.rotation(angle,
							((GeoPoint) parameters[1]).getInhomX(),
							((GeoPoint) parameters[1]).getInhomY());
		case Mirror:
			if (parameters[0] instanceof GeoPoint) {
				GeoPoint point = (GeoPoint) parameters[0];
				return LocusSimilarityTransform2D.pointReflection(point.getInhomX(),
						point.getInhomY());
			}
			GeoLine line = (GeoLine) parameters[0];
			return LocusSimilarityTransform2D.lineReflection(line.getX(),
					line.getY(), line.getZ());
		case Dilate:
			double factor = ((GeoNumberValue) parameters[0]).getDouble();
			return parameters.length == 1
					? LocusSimilarityTransform2D.dilation(factor, 0, 0)
					: LocusSimilarityTransform2D.dilation(factor,
							((GeoPoint) parameters[1]).getInhomX(),
							((GeoPoint) parameters[1]).getInhomY());
		default:
			throw new IllegalStateException("Unsupported similarity command");
		}
	}

	private void validateShape() {
		switch (command) {
		case Translate:
			if (parameters.length != 1 || !(parameters[0] instanceof GeoVec3D)
					|| parameters[0].isGeoElement3D()) {
				throw new IllegalArgumentException("Translate needs one 2D vector");
			}
			break;
		case Rotate:
		case Dilate:
			if (parameters.length < 1 || parameters.length > 2
					|| !(parameters[0] instanceof GeoNumberValue)
					|| parameters.length == 2
							&& (!(parameters[1] instanceof GeoPoint)
									|| parameters[1].isGeoElement3D())) {
				throw new IllegalArgumentException(
						"Rotate/Dilate needs a number and optional 2D center");
			}
			break;
		case Mirror:
			if (parameters.length != 1
					|| !(parameters[0] instanceof GeoPoint
							|| parameters[0] instanceof GeoLine)
					|| parameters[0].isGeoElement3D()) {
				throw new IllegalArgumentException(
						"Mirror needs one 2D point or line");
			}
			break;
		default:
			throw new IllegalArgumentException("Unsupported similarity command");
		}
	}

	private static Commands requireCommand(Commands command) {
		if (command != Commands.Translate && command != Commands.Rotate
				&& command != Commands.Mirror && command != Commands.Dilate) {
			throw new IllegalArgumentException("Unsupported similarity command");
		}
		return command;
	}

	private static GeoElement[] commandInputs(GeoLocusV2 source,
			GeoElement[] parameters) {
		GeoElement[] inputs = new GeoElement[parameters.length + 1];
		inputs[0] = source;
		System.arraycopy(parameters, 0, inputs, 1, parameters.length);
		return inputs;
	}
}
