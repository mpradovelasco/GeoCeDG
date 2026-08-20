/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.algos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.ConstructionFidelity;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationMethod;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.RepresentationRole;
import org.geocedg.common.kernel.locus.LocusV2DomainDescriptor;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.SemanticGeneratorDescriptor1D;
import org.geocedg.common.kernel.locus.SemanticGeneratorDomainProvider1D;
import org.geocedg.common.kernel.locus.SemanticGeneratorFamily1D;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoPointND;

/** Productive reconstructible parent for a dependent-point Locus V2. */
public final class AlgoDependentPointLocusV2 extends AlgoLocusV2 {
	private static final String PROVENANCE = "public-dependent-construction/v1";

	private final GeoPointND dependentPoint;
	private final GeoElement state;
	private final GeoElement coordinate;
	private final GeoElement support;
	private final GeoList domainInput;
	private final GeoText supportBranchInput;
	private final SemanticGeneratorFamily1D family;
	private final double configuredDomainEpsilon;
	private SemanticGeneratorDescriptor1D descriptor;
	private SemanticGeneratorDomainProvider1D provider;
	private ReconstructibleLocusEvaluator2D evaluator;

	/**
	 * Creates a public parent from its real serialized command inputs. Durable
	 * identities are deliberately resolved only after the identity section has
	 * attached the output and direct participating roots.
	 */
	public AlgoDependentPointLocusV2(Construction construction,
			GeoPointND dependentPoint, GeoElement state, GeoElement coordinate,
			GeoElement support, GeoList domainInput, GeoText supportBranchInput,
			SemanticGeneratorFamily1D family, double domainEpsilon) {
		super(construction,
				publicCommandInputs(dependentPoint, state, coordinate, support,
						domainInput, supportBranchInput, family, domainEpsilon),
				evaluatorInputs(dependentPoint, state, coordinate, support,
						domainInput, supportBranchInput));
		this.dependentPoint = Objects.requireNonNull(dependentPoint);
		this.state = Objects.requireNonNull(state);
		this.coordinate = Objects.requireNonNull(coordinate);
		this.support = support;
		this.domainInput = domainInput;
		this.supportBranchInput = supportBranchInput;
		this.family = Objects.requireNonNull(family);
		configuredDomainEpsilon = domainEpsilon == 0 ? 0 : domainEpsilon;
		publishInitialSnapshot();
	}

	/** @return current late-bound descriptor, or {@code null} before attachment */
	public SemanticGeneratorDescriptor1D getGeneratorDescriptor() {
		return descriptor;
	}

	/**
	 * @return direct durable roots read by this parent, identity-deduplicated in
	 *         command/provider order
	 */
	public List<GeoElement> getDurableDependencyGeos() {
		return directInputs(dependentPoint, state, coordinate, support, domainInput,
				supportBranchInput);
	}

	/**
	 * @return complete reconstructible slice in construction order; predecessor
	 *         geos need no separate durable identity
	 */
	public List<GeoElement> getReconstructionSliceGeos() {
		return collectRequiredInputs(dependentPoint, state, coordinate, support,
				domainInput, supportBranchInput);
	}

	@Override
	protected boolean isSemanticPublicationReady() {
		if (getLocus().getPersistentLocusId() == null) {
			return false;
		}
		for (GeoElement dependency : getDurableDependencyGeos()) {
			if (!dependency.isDefined()) {
				return false;
			}
		}
		try {
			rebuildEvaluatorFromCurrentSlice();
			return true;
		} catch (RuntimeException exception) {
			return false;
		}
	}

	@Override
	protected LocusDefinition2D createCandidate(long candidateRevision) {
		Set<BranchProperty> properties = EnumSet.of(BranchProperty.FINITE);
		if (descriptor.isPeriodic()) {
			properties.add(BranchProperty.PERIODIC);
		}
		LocusQuality2D quality = new LocusQuality2D(
				ConstructionFidelity.SEMANTICALLY_CONSTRUCTED,
				EvaluationMethod.DETERMINISTIC_NUMERIC_DEPENDENCY,
				RepresentationRole.SEMANTIC_RESULT,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED);
		LocusBranch2D branch = new LocusBranch2D(
				SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY,
				descriptor.getDeclaredDomain(), descriptor.getValidComponents(),
				descriptor.getOrientation(), PROVENANCE,
				LocusLineage2D.unchanged(), properties, quality);
		return new LocusDefinition2D(getLocus().getLocusIdentity(),
				candidateRevision, DefinitionStatus.VALID, provider,
				Collections.singletonList(branch), evaluator,
				Determinism.POINTWISE_DETERMINISTIC,
				evaluator.getEvaluatorSignature() + "|dependency-update="
						+ candidateRevision,
				getLocus().getInstrumentation());
	}

	@Override
	public Commands getClassName() {
		return Commands.LocusV2;
	}

	/**
	 * Returns the complete construction-order slice used only for isolated XML
	 * reconstruction. Descriptor identity is independently sorted by durable ID.
	 *
	 * @return immutable construction-order input slice
	 */
	public static List<GeoElement> collectRequiredInputs(GeoPointND dependentPoint,
			GeoElement state, GeoElement coordinate, GeoElement support,
			GeoList domainInput, GeoText supportBranchInput) {
		LinkedHashSet<GeoElement> required = new LinkedHashSet<>();
		addWithPredecessors(required, dependentPoint.toGeoElement());
		addWithPredecessors(required, state);
		addWithPredecessors(required, coordinate);
		addWithPredecessors(required, support);
		addWithPredecessors(required, domainInput);
		addWithPredecessors(required, supportBranchInput);
		ArrayList<GeoElement> result = new ArrayList<>(required);
		Collections.sort(result);
		return Collections.unmodifiableList(result);
	}

	private void rebuildEvaluatorFromCurrentSlice() {
		SpatialIdentityRegistry registry = cons.getSpatialIdentityRegistry();
		DomainSnapshot domain = resolveDomain();
		List<GeoElement> required = getDurableDependencyGeos();
		ArrayList<PersistentGeoId> dependencyIds = new ArrayList<>();
		StringBuilder revisionSignature = new StringBuilder("records/v1");
		for (GeoElement geo : required) {
			PersistentGeoId id = registry.getPersistentGeoId(geo);
			GeoIdentityRecord record = id == null ? null : registry.getGeoRecord(id);
			if (id == null || record == null) {
				throw new IllegalStateException(
						"Locus V2 dependency has no published durable record");
			}
			if (geo == getLocus()) {
				throw new IllegalArgumentException(
						"A Locus V2 evaluator cannot depend on its own output");
			}
			dependencyIds.add(id);
		}
		Collections.sort(dependencyIds);
		for (PersistentGeoId id : dependencyIds) {
			GeoIdentityRecord record = registry.getGeoRecord(id);
			revisionSignature.append('|').append(id.toExternalForm())
					.append('@').append(record.getDefinitionRevision())
					.append(':').append(record.getTopologyRevision());
		}
		SemanticGeneratorDescriptor1D candidate =
				new SemanticGeneratorDescriptor1D(family,
						requireId(dependentPoint.toGeoElement()), requireId(state),
						requireId(coordinate), optionalId(support),
						optionalId(domainInput), optionalId(supportBranchInput),
						domain.supportBranchKey, domain.declared,
						domain.components, domain.orientation, domain.periodic,
						domain.epsilon, dependencyIds,
						revisionSignature.toString());
		// Rebuild on every normal-DAG update. Unregistered predecessor values are
		// captured by the reconstructed slice, while identity authority remains
		// confined to the direct durable roots above.
		ReconstructibleLocusEvaluator2D rebuilt =
				new ReconstructibleLocusEvaluator2D(cons, candidate,
						getReconstructionSliceGeos());
		descriptor = candidate;
		provider = new SemanticGeneratorDomainProvider1D(candidate);
		evaluator = rebuilt;
	}

	private DomainSnapshot resolveDomain() {
		switch (family) {
		case SCALAR_STATE:
			LocusV2DomainDescriptor scalar = LocusV2DomainDescriptor.parse(
					domainInput);
			return new DomainSnapshot(scalar.getDeclaredDomain(),
					scalar.getValidComponents(), scalar.getOrientation(),
					scalar.isPeriodic(), configuredDomainEpsilon, null);
		case SEGMENT_POINT:
			return fixedDomain(new LocusInterval2D(0, 1, true, true), false);
		case CIRCLE_POINT:
			return fixedDomain(new LocusInterval2D(-Math.PI, Math.PI, true, false),
					true);
		case CIRCULAR_ARC_POINT:
			return fixedDomain(new LocusInterval2D(0, 1, true, true), false);
		case LOCUS_BRANCH_POINT:
			if (!(support instanceof GeoLocusV2) || supportBranchInput == null
					|| !supportBranchInput.isDefined()) {
				throw new IllegalArgumentException(
						"Locus support requires an explicit current branch input");
			}
			GeoLocusV2 source = (GeoLocusV2) support;
			LocusDefinition2D definition = source.getSemanticDefinition();
			String branchKey = supportBranchInput.getTextString();
			LocusBranch2D branch = definition == null || branchKey == null
					? null : definition.getBranch(branchKey);
			if (branch == null) {
				throw new IllegalArgumentException(
						"The declared Locus V2 support branch is not current");
			}
			return new DomainSnapshot(branch.getDeclaredDriverDomain(),
					branch.getValidDomainComponents(), branch.getOrientation(),
					definition.getProvider().isPeriodic(),
					definition.getProvider().getDomainEpsilon(), branchKey);
		default:
			throw new IllegalStateException("Unsupported semantic generator family");
		}
	}

	private DomainSnapshot fixedDomain(LocusInterval2D domain,
			boolean periodic) {
		return new DomainSnapshot(domain, Collections.singletonList(domain),
				Orientation.INCREASING, periodic, configuredDomainEpsilon, null);
	}

	private static void validateDeclaredShape(SemanticGeneratorFamily1D family,
			GeoElement support, GeoList domainInput, GeoText supportBranchInput) {
		boolean scalar = family == SemanticGeneratorFamily1D.SCALAR_STATE;
		boolean locusSupport = family
				== SemanticGeneratorFamily1D.LOCUS_BRANCH_POINT;
		if (scalar != (domainInput != null) || scalar && support != null
				|| scalar && supportBranchInput != null) {
			throw new IllegalArgumentException(
					"Scalar Locus V2 requires exactly one domain GeoList and no support");
		}
		if (!scalar && (support == null || domainInput != null)) {
			throw new IllegalArgumentException(
					"Point-driven Locus V2 requires one typed support and no domain list");
		}
		if (locusSupport != (supportBranchInput != null)) {
			throw new IllegalArgumentException(
					"Only a Locus V2 support declares a branch GeoText input");
		}
	}

	private static void validateDependencyRoles(GeoPointND dependentPoint,
			GeoElement state, GeoElement coordinate,
			SemanticGeneratorFamily1D family) {
		GeoElement dependent = dependentPoint.toGeoElement();
		if (dependent == state || !dependent.getAllPredecessors().contains(state)) {
			throw new IllegalArgumentException(
					"Dependent point does not depend on the declared generator state");
		}
		if (family == SemanticGeneratorFamily1D.SCALAR_STATE && state != coordinate
				&& !state.getAllPredecessors().contains(coordinate)) {
			throw new IllegalArgumentException(
					"Scalar state does not depend on the explicit true coordinate");
		}
	}

	private PersistentGeoId requireId(GeoElement geo) {
		PersistentGeoId id = optionalId(geo);
		if (id == null) {
			throw new IllegalStateException("Required Locus V2 input has no identity");
		}
		return id;
	}

	private PersistentGeoId optionalId(GeoElement geo) {
		return geo == null ? null
				: cons.getSpatialIdentityRegistry().getPersistentGeoId(geo);
	}

	private static GeoElement[] publicCommandInputs(GeoPointND dependentPoint,
			GeoElement state, GeoElement coordinate, GeoElement support,
			GeoList domainInput, GeoText supportBranchInput,
			SemanticGeneratorFamily1D family, double domainEpsilon) {
		Objects.requireNonNull(dependentPoint);
		Objects.requireNonNull(state);
		Objects.requireNonNull(coordinate);
		Objects.requireNonNull(family);
		if (!Double.isFinite(domainEpsilon) || domainEpsilon < 0) {
			throw new IllegalArgumentException(
					"Generator domain epsilon must be finite and nonnegative");
		}
		validateDeclaredShape(family, support, domainInput, supportBranchInput);
		validateDependencyRoles(dependentPoint, state, coordinate, family);
		ArrayList<GeoElement> inputs = new ArrayList<>();
		inputs.add(dependentPoint.toGeoElement());
		inputs.add(state);
		if (family == SemanticGeneratorFamily1D.SCALAR_STATE) {
			if (coordinate != state) {
				inputs.add(coordinate);
			}
			inputs.add(domainInput);
		}
		return inputs.toArray(new GeoElement[0]);
	}

	private static List<GeoElement> directInputs(GeoPointND dependentPoint,
			GeoElement state, GeoElement coordinate, GeoElement support,
			GeoList domainInput, GeoText supportBranchInput) {
		ArrayList<GeoElement> inputs = new ArrayList<>();
		Set<GeoElement> seen = Collections.newSetFromMap(new IdentityHashMap<>());
		addDirect(inputs, seen,
				Objects.requireNonNull(dependentPoint).toGeoElement());
		addDirect(inputs, seen, Objects.requireNonNull(state));
		addDirect(inputs, seen, Objects.requireNonNull(coordinate));
		addDirect(inputs, seen, support);
		addDirect(inputs, seen, domainInput);
		addDirect(inputs, seen, supportBranchInput);
		return Collections.unmodifiableList(inputs);
	}

	private static void addDirect(List<GeoElement> result, Set<GeoElement> seen,
			GeoElement geo) {
		if (geo != null && seen.add(geo)) {
			result.add(geo);
		}
	}

	private static GeoElement[] evaluatorInputs(GeoPointND dependentPoint,
			GeoElement state, GeoElement coordinate, GeoElement support,
			GeoList domainInput, GeoText supportBranchInput) {
		return collectRequiredInputs(dependentPoint, state, coordinate, support,
				domainInput, supportBranchInput).toArray(new GeoElement[0]);
	}

	private static void addWithPredecessors(Set<GeoElement> result,
			GeoElement geo) {
		if (geo == null) {
			return;
		}
		result.addAll(geo.getAllPredecessors());
		result.add(geo);
	}

	private static final class DomainSnapshot {
		private final LocusInterval2D declared;
		private final List<LocusInterval2D> components;
		private final Orientation orientation;
		private final boolean periodic;
		private final double epsilon;
		private final String supportBranchKey;

		private DomainSnapshot(LocusInterval2D declared,
				List<LocusInterval2D> components, Orientation orientation,
				boolean periodic, double epsilon, String supportBranchKey) {
			this.declared = Objects.requireNonNull(declared);
			this.components = Collections.unmodifiableList(
					new ArrayList<>(components));
			this.orientation = Objects.requireNonNull(orientation);
			this.periodic = periodic;
			this.epsilon = epsilon;
			this.supportBranchKey = supportBranchKey;
		}
	}
}
