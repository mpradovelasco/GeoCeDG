/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */
package org.geocedg.common.kernel.algos;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusPrincipalBranchState2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.SemanticGeneratorDescriptor1D;
import org.geocedg.common.kernel.locus.SemanticGeneratorFamily1D;
import org.geocedg.common.kernel.locus.intersection.IntersectionEndpointProvenanceResolver2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricPositionBinder2D;
import org.geocedg.common.kernel.locus.metric.MetricPositionBinding2D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoPointOnPath;
import org.geogebra.common.kernel.algos.AlgoTranslate;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.geos.GeoVector;

/** An explicit point constructed by substitution in a proven endpoint recipe. */
public final class AlgoProvenLocusEndpoint2D extends AlgoElement {

	public static final String RECIPE_PREFIX = "locus-endpoint/translation/v1|";

	public enum Cause {
		RESOLVED, INVALID_SOURCE, PREMISE_LOST, COMPONENT_UNRESOLVED, POSITION_UNBOUND
	}

	/** A proposal identifies a single witness and the premises the author may commit. */
	public record Proposal(GeoPoint witness, GeoVector vector, String recipe) { }

	private final GeoLocusV2 source;
	private final GeoPoint reference;
	private final GeoPoint witness;
	private final GeoVector vector;
	private final GeoText recipe;
	private final GeoPoint output;
	private final LocusMetricPositionBinder2D binder = new LocusMetricPositionBinder2D();
	private LocusSemanticAddress2D address;
	private MetricPositionBinding2D binding;
	private Cause cause;

	/** Discovers the witness from the typed construction of the reference. */
	public static Proposal propose(GeoLocusV2 source, GeoPoint reference) {
		AlgoTranslate translation = translationOf(reference);
		if (translation == null || !(translation.getInput(0) instanceof GeoPoint witness)
				|| !(translation.getInput(1) instanceof GeoVector vector)
				|| !provesSubstitution(source, reference, witness, vector)) {
			return null;
		}
		LocusSemanticAddress2D address = resolveAddress(source, witness);
		if (address == null || !new LocusMetricPositionBinder2D()
				.bind(address.toMetricPosition(), source.getSemanticDefinition()).isValid()) {
			return null;
		}
		return new Proposal(witness, vector,
				RECIPE_PREFIX + LocusPrincipalBranchState2D.encode(address));
	}

	public AlgoProvenLocusEndpoint2D(Construction construction, GeoLocusV2 source,
			GeoPoint reference, GeoPoint witness, GeoVector vector, GeoText recipe) {
		super(construction);
		this.source = source;
		this.reference = reference;
		this.witness = witness;
		this.vector = vector;
		this.recipe = recipe;
		output = new GeoPoint(construction);
		setInputOutput();
		compute();
	}

	@Override
	protected void setInputOutput() {
		input = new GeoElement[] {source, reference, witness, vector, recipe};
		setOnlyOutput(output);
		setProtectedInput(true);
		setDependencies();
	}

	@Override
	public void compute() {
		address = null;
		binding = null;
		if (!source.isDefined() || source.getPersistentLocusId() == null) {
			undefined(Cause.INVALID_SOURCE);
			return;
		}
		if (!provesSubstitution(source, reference, witness, vector)) {
			undefined(Cause.PREMISE_LOST);
			return;
		}
		LocusSemanticAddress2D candidate = resolveAddress(source, witness);
		LocusDefinition2D definition = source.getSemanticDefinition();
		var selector = decodeRecipe(recipe.getTextString());
		if (candidate == null || selector == null || !recipe.isDefined()
				|| !selector.resolves(candidate, definition.getBranch(candidate.getBranchKey()),
						definition.getProvider().getDomainEpsilon())) {
			undefined(Cause.COMPONENT_UNRESOLVED);
			return;
		}
		binding = binder.bind(candidate.toMetricPosition(), definition);
		if (!binding.isValid()) {
			undefined(Cause.POSITION_UNBOUND);
			return;
		}
		var point = binding.getEvaluatedPoint().orElseThrow();
		output.setCoords(point.getX(), point.getY(), 1);
		address = candidate;
		cause = Cause.RESOLVED;
	}

	public static LocusPrincipalBranchState2D.Decoded decodeRecipe(String value) {
		if (value == null || !value.startsWith(RECIPE_PREFIX)) {
			return null;
		}
		try {
			return LocusPrincipalBranchState2D.decode(value.substring(RECIPE_PREFIX.length()));
		} catch (IllegalArgumentException malformed) {
			return null;
		}
	}

	private static boolean provesSubstitution(GeoLocusV2 source, GeoPoint reference,
			GeoPoint witness, GeoVector vector) {
		if (!(source.getParentAlgorithm() instanceof AlgoDependentPointLocusV2 generator)
				|| !(generator.getInput(0) instanceof GeoPoint traced)
				|| !(generator.getInput(1) instanceof GeoPoint driver)
				|| reference.getConstruction() != source.getConstruction()
				|| reference == traced || witness == driver
				|| !reference.isDefined() || !witness.isDefined() || !vector.isDefined()) {
			return false;
		}
		SemanticGeneratorDescriptor1D descriptor = generator.getGeneratorDescriptor();
		if (descriptor == null || driver.getPath() == null
				|| driver.getPath() != witness.getPath()
				|| !(witness.getParentAlgorithm() instanceof AlgoPointOnPath pathPoint)
				|| pathPoint.getP() != witness || pathPoint.getPath() != driver.getPath()
				|| witness.isChildOf(driver) || vector.isChildOf(driver)) {
			return false;
		}
		for (GeoElement input : pathPoint.getInput()) {
			if (!input.isDefined()) {
				return false;
			}
		}
		if (descriptor.getFamily() != SemanticGeneratorFamily1D.CIRCLE_POINT
				&& descriptor.getFamily() != SemanticGeneratorFamily1D.SEGMENT_POINT) {
			return false;
		}
		AlgoTranslate tracedTranslation = translationOf(traced);
		AlgoTranslate referenceTranslation = translationOf(reference);
		return tracedTranslation != null && referenceTranslation != null
				&& tracedTranslation.getInput(0) == driver && tracedTranslation.getInput(1) == vector
				&& referenceTranslation.getInput(0) == witness
				&& referenceTranslation.getInput(1) == vector;
	}

	private static AlgoTranslate translationOf(GeoPoint point) {
		AlgoElement parent = point.getParentAlgorithm();
		return parent != null && parent.getClass() == AlgoTranslate.class
				&& parent.getOutput(0) == point ? (AlgoTranslate) parent : null;
	}

	private static LocusSemanticAddress2D resolveAddress(GeoLocusV2 source, GeoPoint witness) {
		LocusDefinition2D definition = source.getSemanticDefinition();
		double parameter = witness.getPathParameter().getT();
		return definition == null || !Double.isFinite(parameter) || source.getPersistentLocusId() == null
				? null : IntersectionEndpointProvenanceResolver2D.addressFromRawParameter(
						definition, source.getPersistentLocusId(),
						SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY, parameter);
	}

	private void undefined(Cause failure) {
		cause = failure;
		output.setUndefined();
	}

	@Override
	public Commands getClassName() { return Commands.Point; }

	public GeoPoint getPoint() { return output; }

	public GeoLocusV2 getSource() { return source; }

	public LocusSemanticAddress2D getAddress() { return address; }

	public MetricPositionBinding2D getBinding() { return binding; }

	public Cause getCause() { return cause; }
}
