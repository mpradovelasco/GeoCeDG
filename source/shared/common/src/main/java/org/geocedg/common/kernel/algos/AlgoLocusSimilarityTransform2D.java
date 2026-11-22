/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.algos;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExploratoryEvidence2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusPointComponent2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSimilarityEvaluator2D;
import org.geocedg.common.kernel.locus.LocusSimilarityTransform2D;
import org.geocedg.common.kernel.locus.LocusSimilarityTransform2D.Kind;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidence2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidenceOwner2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedImageOwner2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedSample2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedSampler2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedTraversalPolicy2D;
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
	/** Owners of the conditioned evidence of the published definition, by branch. */
	private final java.util.Map<String, LocusConditionedImageOwner2D> conditionedOwners =
			new java.util.HashMap<>();
	private LocusDefinition2D conditionedDefinition;
	private boolean conditionedCascade;

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

	/**
	 * The image inherits the existence structure of its source with its unresolved bands,
	 * so it explores them with the source's exploratory evidence (decision E-1), evaluated
	 * through the transform.
	 */
	@Override
	public LocusExploratoryEvidence2D getExploratoryEvidence(String branchKey) {
		return sourceSnapshot != null && sourceSnapshot == source.getSemanticDefinition()
				? source.getExploratoryEvidence(branchKey) : null;
	}

	/**
	 * The image inherits the conditioned evidence of its source cell by cell and states it
	 * against the policy in its own world (ADR 0042; contract {@code
	 * post-r1-conditioned-use.md}, section 8.7; {@code
	 * locus-v2-similarity-transformations.md}, conditioned use amendment). The image is
	 * never computed from a tessellation of the source.
	 */
	@Override
	public LocusConditionedEvidence2D getConditionedEvidence(String branchKey) {
		LocusConditionedImageOwner2D owner = conditionedOwner(branchKey);
		if (owner == null) {
			return null;
		}
		LocusDefinition2D definition = conditionedDefinition;
		LocusSimilarityTransform2D transform = transformSnapshot;
		LocusConditionedEvidence2D evidence = owner.get(definition.getSemanticRevision(),
				transform.getSemanticSignature(), transform.getLengthScale(),
				conditionedSource(branchKey), conditionedSampler(branchKey, transform),
				points -> imageOfNodes(points, transform));
		followSourceConfirmation(owner);
		return evidence;
	}

	@Override
	public LocusConditionedEvidence2D getPublishedConditionedEvidence(String branchKey) {
		LocusConditionedImageOwner2D owner = conditionedOwner(branchKey);
		LocusConditionedEvidence2D published = owner == null ? null : owner.getPublished();
		LocusConditionedEvidence2D inherited = published == null ? null
				: source.getPublishedConditionedEvidence(branchKey);
		// Only a content that still inherits the published content of the source.
		return inherited != null && !published.getInheritedSources().isEmpty()
				&& published.getInheritedSources().get(0).endsWith(
						"@" + inherited.getContentIdentity()) ? published : null;
	}

	@Override
	public LocusConditionedSample2D sampleConditioned(String branchKey,
			double canonicalParameter) {
		LocusSimilarityTransform2D transform = transformSnapshot;
		if (transform == null || sourceSnapshot == null
				|| sourceSnapshot != source.getSemanticDefinition()) {
			return null;
		}
		return conditionedSampler(branchKey, transform).sample(canonicalParameter);
	}

	@Override
	public long getConditionedPublicationCounter(String branchKey) {
		LocusConditionedImageOwner2D owner = conditionedOwner(branchKey);
		return owner == null ? 0 : owner.getPublicationCounter();
	}

	@Override
	public LocusConditionedEvidenceOwner2D.ReportOutcome reportConditionedContradiction(
			String branchKey, double parameter) {
		LocusConditionedImageOwner2D owner = conditionedOwner(branchKey);
		if (owner == null || getConditionedEvidence(branchKey) == null) {
			return null;
		}
		LocusConditionedEvidenceOwner2D.ReportOutcome outcome = owner.report(parameter,
				conditionedSource(branchKey), conditionedSampler(branchKey,
						transformSnapshot));
		followSourceConfirmation(owner);
		return outcome;
	}

	@Override
	public LocusConditionedTraversalPolicy2D getConditionedTraversalPolicy() {
		return source.getParentAlgorithm() instanceof AlgoLocusV2
				? ((AlgoLocusV2) source.getParentAlgorithm()).getConditionedTraversalPolicy()
				: LocusConditionedTraversalPolicy2D.v1();
	}

	/** @return the owner of a branch of the published definition, or {@code null} */
	private LocusConditionedImageOwner2D conditionedOwner(String branchKey) {
		LocusDefinition2D definition = getLocus().isDefined()
				? getLocus().getSemanticDefinition() : null;
		if (definition == null || transformSnapshot == null || sourceSnapshot == null
				|| sourceSnapshot != source.getSemanticDefinition()
				|| definition.getBranch(branchKey) == null) {
			return null;
		}
		if (conditionedDefinition != definition) {
			// A new revision discards the evidence and the contradiction set of the image.
			conditionedDefinition = definition;
			conditionedOwners.clear();
		}
		return conditionedOwners.computeIfAbsent(branchKey, key ->
				new LocusConditionedImageOwner2D(getConditionedTraversalPolicy()));
	}

	private LocusConditionedImageOwner2D.Source conditionedSource(String branchKey) {
		return new LocusConditionedImageOwner2D.Source() {
			@Override
			public LocusConditionedEvidence2D evidence() {
				return source.getConditionedEvidence(branchKey);
			}

			@Override
			public LocusConditionedEvidenceOwner2D.ReportOutcome report(double parameter) {
				// The producer of the source decides with its own evaluator; its dependents
				// follow after this owner has published (followSourceConfirmation).
				return source.getParentAlgorithm() instanceof AlgoLocusV2
						? ((AlgoLocusV2) source.getParentAlgorithm())
								.reportConditionedContradiction(branchKey, parameter) : null;
			}

			@Override
			public String identity() {
				return source.getLocusIdentity() + "/" + branchKey;
			}
		};
	}

	/** The source evaluator composed with the similarity, with the source testimony. */
	private LocusConditionedSampler2D conditionedSampler(String branchKey,
			LocusSimilarityTransform2D transform) {
		return parameter -> {
			LocusConditionedSample2D sample = source.sampleConditioned(branchKey, parameter);
			if (sample == null) {
				return LocusConditionedSample2D.failed("SOURCE_NOT_EVALUABLE");
			}
			if (!sample.isValid()) {
				return sample;
			}
			LocusPoint2D image = transform.transform(new LocusPoint2D(sample.getX(),
					sample.getY()));
			return LocusConditionedSample2D.valid(image.getX(), image.getY(),
					sample.getTestimony());
		};
	}

	private static double[] imageOfNodes(double[] points,
			LocusSimilarityTransform2D transform) {
		double[] images = new double[points.length];
		for (int index = 0; index + 1 < points.length; index += 2) {
			LocusPoint2D image = transform.transform(new LocusPoint2D(points[index],
					points[index + 1]));
			images[index] = image.getX();
			images[index + 1] = image.getY();
		}
		return images;
	}

	/**
	 * A node that the image added contradicted a span of the source and its producer
	 * confirmed it: the dependents of the source follow its new content.
	 */
	private void followSourceConfirmation(LocusConditionedImageOwner2D owner) {
		if (owner.takeSourceConfirmed() && !conditionedCascade) {
			conditionedCascade = true;
			try {
				source.updateCascade();
			} finally {
				conditionedCascade = false;
			}
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
			// A collapsed image of a source that may have a valid address: one with a
			// certified component, or one whose existence is not established (contract
			// post-r1-conditioned-use.md, section 8.7, "Finite ratio zero").
			if (transformSnapshot.isCollapsed() && (!sourceBranch.getExistenceStructure()
					.getContinuousValidComponents().isEmpty()
					|| sourceBranch.getExistenceStructure().getCompleteness()
							!= LocusExistenceStructure2D.Completeness.COMPLETE)) {
				properties.add(BranchProperty.COLLAPSED_IMAGE);
			}
			LocusQuality2D quality = new LocusQuality2D(
					sourceBranch.getQuality().getConstructionFidelity(),
					sourceBranch.getQuality().getEvaluationMethod(),
					sourceBranch.getQuality().getRepresentationRole(),
					NumericGuarantee.FLOATING_POINT_UNCERTIFIED);
			branches.add(new LocusBranch2D(sourceBranch.getBranchKey(),
					sourceBranch.getDeclaredDriverDomain(),
					transported(sourceBranch.getExistenceStructure()),
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

	/**
	 * The existence structure of the source with the position enclosure of each point
	 * component transformed ({@code locus-v2-similarity-transformations.md}, PRE-G9B-CU2 scope
	 * note A2.5): its parameter enclosure, structural source, maximality, proof and lineage
	 * stay those of the source, whose topology the image never recomputes.
	 */
	private LocusExistenceStructure2D transported(LocusExistenceStructure2D structure) {
		if (structure.getPointComponents().isEmpty()) {
			return structure;
		}
		List<LocusPointComponent2D> images = new ArrayList<>();
		for (LocusPointComponent2D point : structure.getPointComponents()) {
			double[] position = point.getPositionState()
					== LocusPointComponent2D.PositionState.CERTIFIED_ENCLOSURE
							? transformSnapshot.transformEnclosure(point.getPositionEnclosure())
							: null;
			images.add(new LocusPointComponent2D(point.getParameterLower(),
					point.getParameterUpper(), point.getSource(), point.getMaximality(),
					point.getNature(), point.getEvidenceKey(), position));
		}
		return structure.withPointComponents(images);
	}

	@Override
	public Commands getClassName() {
		return command;
	}

	@Override
	public String getV2RedefineContractId() {
		switch (command) {
		case Translate:
			return "locus-v2/similarity/translate-vector/v1";
		case Rotate:
			return parameters.length == 1
					? "locus-v2/similarity/rotate-origin/v1"
					: "locus-v2/similarity/rotate-point/v1";
		case Mirror:
			return parameters[0] instanceof GeoPoint
					? "locus-v2/similarity/reflect-point/v1"
					: "locus-v2/similarity/reflect-line/v1";
		case Dilate:
			return parameters.length == 1
					? "locus-v2/similarity/dilate-origin/v1"
					: "locus-v2/similarity/dilate-point/v1";
		default:
			throw new IllegalStateException("Unsupported similarity command");
		}
	}

	/** @return exact source and transform-input dependencies */
	public GeoElement[] getDurableDependencyGeos() {
		return getInput().clone();
	}

	/** @return transformation captured for the current published revision */
	public LocusSimilarityTransform2D getTransformSnapshot() {
		return transformSnapshot;
	}

	/** @return exact semantic source whose address space this R5 image preserves */
	public GeoLocusV2 getSource() {
		return source;
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
