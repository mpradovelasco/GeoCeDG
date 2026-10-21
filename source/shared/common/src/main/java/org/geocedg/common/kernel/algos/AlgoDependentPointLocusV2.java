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
import java.util.Optional;
import java.util.Set;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusCertificationScope2D;
import org.geocedg.common.kernel.locus.LocusCertificationWork2D;
import org.geocedg.common.kernel.locus.LocusComponentLineage2D;
import org.geocedg.common.kernel.locus.LocusCoverageCause2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
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
import org.geocedg.common.kernel.locus.ReconstructionSliceFingerprint2D;
import org.geocedg.common.kernel.locus.SemanticGeneratorDescriptor1D;
import org.geocedg.common.kernel.locus.SemanticGeneratorDomainProvider1D;
import org.geocedg.common.kernel.locus.SemanticGeneratorFamily1D;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxModel2D;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D;
import org.geocedg.common.kernel.locus.intersection.RouteSourceFacts2D;
import org.geocedg.common.kernel.locus.intersection.SelectedRootContinuousDomainCertifier2D;
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
	private String sliceFingerprint;
	private ReconstructionSliceFingerprint2D.Snapshot sliceSnapshot;
	private ReconstructibleLocusEvaluator2D shadowEvaluator;
	private HybridExistenceCertifier2D.Result shadowCertificate;
	private ReconstructibleLocusEvaluator2D routeFactsEvaluator;
	private double routeLengthBound = Double.NaN;
	private String shadowSelection;
	private List<LocusLineage2D> shadowLineage = List.of();

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
			synchronizeEvaluatorWithCurrentSlice();
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
		LocusExistenceStructure2D existence = !evaluator.isReconstructionFaithful()
				? unfaithfulReconstruction()
				: SelectedRootContinuousDomainCertifier2D.certify(descriptor,
						dependentPoint, state, support).orElseGet(() ->
						fallbackExistenceStructure());
		LocusBranch2D branch = new LocusBranch2D(
				SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY,
				descriptor.getDeclaredDomain(), existence,
				descriptor.getOrientation(), PROVENANCE,
				LocusLineage2D.unchanged(), properties, quality);
		return new LocusDefinition2D(getLocus().getLocusIdentity(),
				candidateRevision, DefinitionStatus.VALID, provider,
				Collections.singletonList(branch), evaluator,
				Determinism.POINTWISE_DETERMINISTIC,
				evaluator.getEvaluatorSignature() + "|slice=" + sliceFingerprint,
				getLocus().getInstrumentation());
	}

	@Override
	public Commands getClassName() {
		return Commands.LocusV2;
	}

	/**
	 * Hybrid existence certificate of the current revision (contract
	 * {@value HybridExistenceCertifier2D#CONTRACT}, ADR 0038), computed once per revision
	 * and shared by every caller. It is kept in shadow: the published existence structure
	 * stays the current one until step T11 of PRE-G9B-CU2 replaces it.
	 *
	 * @return certificate of the current revision, or {@code null} before the slice of the
	 *         first revision is built
	 */
	public HybridExistenceCertifier2D.Result getHybridExistenceCertificate() {
		ReconstructibleLocusEvaluator2D current = evaluator;
		if (current == null || descriptor == null) {
			return null;
		}
		if (shadowEvaluator != current) {
			LocusExistenceStructure2D previous = shadowCertificate == null ? null
					: shadowCertificate.getStructure();
			String previousSelection = shadowSelection;
			shadowCertificate = LocusCertificationScope2D.certify(
					getLocus().getInstrumentation(), () -> hybridCertificate(current));
			shadowSelection = current.captureCertifiedProgramV3()
					.map(CertifiedConstructionProgram2D::getSelectionSignature).orElse(null);
			shadowLineage = LocusComponentLineage2D.transitions(previous,
					shadowCertificate.getStructure(),
					Objects.equals(previousSelection, shadowSelection));
			shadowEvaluator = current;
		}
		return shadowCertificate;
	}

	/**
	 * Lineage transitions of the components of the hybrid certificate from the previous one
	 * computed for this locus ({@code locus-v2-existence-components.md} section 15.6), kept in
	 * shadow with it; the first certificate after loading relates to nothing earlier.
	 *
	 * @return transitions into the current certificate, empty before the first one
	 */
	public List<LocusLineage2D> getHybridLineage() {
		return getHybridExistenceCertificate() == null ? List.of() : shadowLineage;
	}

	private HybridExistenceCertifier2D.Result hybridCertificate(
			ReconstructibleLocusEvaluator2D current) {
		LocusInterval2D domain = descriptor.getDeclaredDomain();
		String key = SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY;
		HybridExistenceCertifier2D.Policy policy = HybridExistenceCertifier2D.Policy.DEFAULT;
		if (!current.isReconstructionFaithful()) {
			// Evaluations of an unfaithful slice fail explicitly: nothing to explore.
			return HybridExistenceCertifier2D.notEstablished(domain, descriptor.isPeriodic(),
					key, current.getFidelityReport().isSelectionOnly()
							? LocusCoverageCause2D.SELECTION_NOT_REPRODUCED
							: LocusCoverageCause2D.RECONSTRUCTION_FIDELITY_NOT_ESTABLISHED,
					current.getProgramV3Refusal(), null, policy,
					getLocus().getInstrumentation());
		}
		LocusDefinition2D definition = getLocus().getSemanticDefinition();
		LocusBranch2D branch = definition == null
				|| definition.getEvaluatorCapability() != current ? null
						: definition.getBranch(key);
		try (LocusEvaluationSession2D session =
				LocusEvaluationSession2D.openServiceSession(false, 1)) {
			HybridExistenceCertifier2D.Sampler sampler = branch == null ? null
					: parameter -> current.evaluate(definition, branch, parameter, session)
							.isValid();
			Optional<CertifiedConstructionProgram2D> program =
					current.captureCertifiedProgramV3();
			if (!program.isPresent()) {
				String refusal = current.getProgramV3Refusal();
				return HybridExistenceCertifier2D.notEstablished(domain,
						descriptor.isPeriodic(), key, "unsettled-selection".equals(refusal)
								? LocusCoverageCause2D.NOT_CERTIFIED
								: LocusCoverageCause2D.OPERATION_OUTSIDE_CLASS,
						refusal == null ? "outside-class" : refusal, sampler, policy,
						getLocus().getInstrumentation());
			}
			return HybridExistenceCertifier2D.certify(
					new ConstructionBoxModel2D(program.get()), current.getMetricStructuresV3(),
					current.getOperationIdentitiesV3(), domain, descriptor.isPeriodic(), key,
					sampler, policy, getLocus().getInstrumentation());
		}
	}

	/**
	 * Rigorous facts of this locus as the source of an oriented length ({@code
	 * locus-v2-metrics.md} section 26), from the class-v3 program of the current revision:
	 * a lower bound of a chord from the origin, and the upper bound of the length of a
	 * closed periodic certificate, computed once per revision. Neither reads a metric value.
	 *
	 * @param origin canonical address of the origin of the route
	 * @return {@code {chord, lengthBound}}, each {@code NaN} when not certified
	 */
	public double[] getRouteSourceFacts(double origin) {
		HybridExistenceCertifier2D.Result certificate = getHybridExistenceCertificate();
		ReconstructibleLocusEvaluator2D current = shadowEvaluator;
		Optional<CertifiedConstructionProgram2D> program = current == null
				? Optional.empty() : current.captureCertifiedProgramV3();
		if (certificate == null || !program.isPresent()
				|| !certificate.getStructure().isClosedPeriodic()) {
			return new double[] {Double.NaN, Double.NaN};
		}
		ConstructionBoxModel2D model = new ConstructionBoxModel2D(program.get());
		LocusInterval2D domain = descriptor.getDeclaredDomain();
		if (routeFactsEvaluator != current) {
			routeLengthBound = RouteSourceFacts2D.lengthUpperBound(model, domain);
			routeFactsEvaluator = current;
		}
		return new double[] {RouteSourceFacts2D.chordLowerBound(model, domain, origin),
			routeLengthBound};
	}

	/**
	 * A slice whose copy does not reproduce its authoritative inputs certifies nothing and
	 * its evaluations fail explicitly (PRE-G9B-CU2, D-CU2-20-R).
	 */
	private LocusExistenceStructure2D unfaithfulReconstruction() {
		return LocusExistenceStructure2D.notEstablished(
				SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY,
				descriptor.getDeclaredDomain(),
				LocusExistenceStructure2D.topologyOf(descriptor.getDeclaredDomain(),
						descriptor.isPeriodic()),
				evaluator.getFidelityReport().isSelectionOnly()
						? LocusCoverageCause2D.SELECTION_NOT_REPRODUCED
						: LocusCoverageCause2D.RECONSTRUCTION_FIDELITY_NOT_ESTABLISHED,
				"reconstruction-fidelity-not-established");
	}

	private LocusExistenceStructure2D fallbackExistenceStructure() {
		LocusExistenceStructure2D.DomainTopology topology =
				LocusExistenceStructure2D.topologyOf(descriptor.getDeclaredDomain(),
						descriptor.isPeriodic());
		if (dependsOnSelectedIntersectionRoot()) {
			return LocusExistenceStructure2D.notEstablished(
					SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY,
					descriptor.getDeclaredDomain(), topology,
					LocusCoverageCause2D.NOT_CERTIFIED,
					"selected-root-continuity-not-established");
		}
		if (family == SemanticGeneratorFamily1D.LOCUS_BRANCH_POINT
				&& support instanceof GeoLocusV2) {
			LocusDefinition2D sourceDefinition =
					((GeoLocusV2) support).getSemanticDefinition();
			LocusBranch2D sourceBranch = sourceDefinition == null
					|| descriptor.getSupportBranchKey() == null ? null
							: sourceDefinition.getBranch(
									descriptor.getSupportBranchKey());
			if (sourceBranch == null || sourceBranch.getExistenceStructure()
					.getCompleteness()
							!= LocusExistenceStructure2D.Completeness.COMPLETE) {
				return LocusExistenceStructure2D.notEstablished(
						SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY,
						descriptor.getDeclaredDomain(), topology,
						LocusCoverageCause2D.UPSTREAM_NOT_ESTABLISHED,
						"upstream-locus-coverage-not-established");
			}
		}
		return LocusExistenceStructure2D.completeCompatibility(
				SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY,
				descriptor.getDeclaredDomain(), descriptor.getValidComponents(),
				descriptor.isPeriodic());
	}

	private boolean dependsOnSelectedIntersectionRoot() {
		for (GeoElement geo : getReconstructionSliceGeos()) {
			if (geo.getParentAlgorithm() instanceof AlgoLocusIntersectionPointV2) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getV2RedefineContractId() {
		return "locus-v2/dependent-generator/" + family.getProviderId();
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

	/**
	 * Keeps the published evaluator while the semantic snapshot is unchanged and
	 * rebuilds the slice once when it changes (PRE-G9B-CU1,
	 * {@code locus-v2-semantics.md} v1.2). The snapshot is the generator
	 * descriptor plus the exact fingerprint of the non-driver slice inputs; the
	 * driver position is not part of it, so moving only the driver keeps the
	 * revision, the evaluator and every revision-keyed cache.
	 */
	private void synchronizeEvaluatorWithCurrentSlice() {
		SemanticGeneratorDescriptor1D candidate = currentDescriptor();
		List<GeoElement> slice = getReconstructionSliceGeos();
		ReconstructionSliceFingerprint2D.Snapshot snapshot =
				ReconstructionSliceFingerprint2D.snapshot(slice,
						java.util.Arrays.asList(state, coordinate));
		getLocus().getInstrumentation().recordCertificationWork(
				LocusCertificationWork2D.FINGERPRINT_COMPUTATION);
		if (evaluator != null && descriptor != null
				&& candidate.getSemanticSignature()
						.equals(descriptor.getSemanticSignature())
				&& snapshot.getDigest().equals(sliceFingerprint)) {
			return;
		}
		ReconstructibleLocusEvaluator2D rebuilt =
				new ReconstructibleLocusEvaluator2D(cons, candidate, slice,
						getLocus().getInstrumentation());
		getLocus().getInstrumentation().recordSliceChange(
				snapshot.changeFrom(sliceSnapshot));
		descriptor = candidate;
		provider = new SemanticGeneratorDomainProvider1D(candidate);
		evaluator = rebuilt;
		sliceFingerprint = snapshot.getDigest();
		sliceSnapshot = snapshot;
		getLocus().getInstrumentation().recordSliceBuild();
	}

	private SemanticGeneratorDescriptor1D currentDescriptor() {
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
		// Unregistered predecessor values are covered by the slice fingerprint,
		// while identity authority remains confined to the direct durable roots.
		return new SemanticGeneratorDescriptor1D(family,
				requireId(dependentPoint.toGeoElement()), requireId(state),
				requireId(coordinate), optionalId(support),
				optionalId(domainInput), optionalId(supportBranchInput),
				domain.supportBranchKey, domain.declared,
				domain.components, domain.orientation, domain.periodic,
				domain.epsilon, dependencyIds, revisionSignature.toString());
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
