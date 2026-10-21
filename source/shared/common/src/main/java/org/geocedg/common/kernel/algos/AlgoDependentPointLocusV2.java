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
import org.geocedg.common.kernel.locus.LocusExploratoryEvidence2D;
import org.geocedg.common.kernel.locus.LocusInstrumentation2D;
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
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.algos.AlgoIntersectSingle;
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
	private ReconstructibleLocusEvaluator2D certifiedEvaluator;
	private String certifiedLimitation;
	private HybridExistenceCertifier2D.Result certificate;
	private ReconstructibleLocusEvaluator2D routeFactsEvaluator;
	private double routeLengthBound = Double.NaN;
	private String certifiedSelection;
	private List<LocusLineage2D> certificateLineage = List.of();

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

	/**
	 * Publication rule of PRE-G9B-CU2 (H01; {@code locus-v2-semantics.md}, section
	 * "PRE-G9B-CU2 amendment"): the locus is published when its definition inputs are
	 * defined; whether the traced point is defined at the current driver position never
	 * affects publication. While a construction is read as a whole, a locus whose slice has an
	 * intersection with an unsettled selection is published by the update that ends the read.
	 */
	@Override
	protected boolean isSemanticPublicationReady() {
		if (getLocus().getPersistentLocusId() == null) {
			return false;
		}
		if (!kernel.isMacroKernel()
				&& cons.getSpatialIdentityRegistry().isPublishingConstructionRestore()
				&& hasUnsettledSelection()) {
			// The revision published now would be a transient one, never certified (unsettled
			// selection); the update of the whole construction that ends the read settles the
			// selection and publishes the first revision once.
			return false;
		}
		for (GeoElement input : getDefinitionInputs()) {
			if (!input.isDefined()) {
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

	/**
	 * @return whether a line-conic intersection of the slice is initialized but has not set
	 *         its permutation yet, which its next computation does (read-only accessors of
	 *         D-CU2-17; {@code ReconstructionFidelity2D} calls such a selection unsettled)
	 */
	private boolean hasUnsettledSelection() {
		for (GeoElement geo : getReconstructionSliceGeos()) {
			AlgoElement parent = geo.getParentAlgorithm();
			if (parent instanceof AlgoIntersectSingle) {
				parent = ((AlgoIntersectSingle) parent).getSourceIntersection();
			}
			if (parent instanceof AlgoIntersectLineConic) {
				AlgoIntersectLineConic intersection = (AlgoIntersectLineConic) parent;
				if (!intersection.isSelectionFirstIntersectionPending()
						&& intersection.isSelectionPermutationPending()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Definition inputs of the publication rule: the elements of the reconstruction slice
	 * that are neither the driver nor depend on it. They are not the durable dependency
	 * projection of ADR 0029, which does not change.
	 *
	 * @return definition inputs in construction order
	 */
	public List<GeoElement> getDefinitionInputs() {
		ArrayList<GeoElement> inputs = new ArrayList<>();
		for (GeoElement geo : getReconstructionSliceGeos()) {
			if (geo != state && geo != coordinate && !geo.isChildOf(state)
					&& !geo.isChildOf(coordinate)) {
				inputs.add(geo);
			}
		}
		return Collections.unmodifiableList(inputs);
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
		// PRE-G9B-CU2 step T11 (locus-v2-existence-components.md section 15.7): the
		// certificate of the revision is published; a locus that cannot be certified is
		// NOT_ESTABLISHED and never falls back to a compatibility certificate.
		HybridExistenceCertifier2D.Result published = getHybridExistenceCertificate();
		LocusBranch2D branch = new LocusBranch2D(
				SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY,
				descriptor.getDeclaredDomain(), published.getStructure(),
				descriptor.getOrientation(), PROVENANCE, branchLineage(), properties,
				quality);
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
	 * Existence certificate of the current revision, the one this producer publishes
	 * (contract {@value HybridExistenceCertifier2D#CONTRACT}, ADR 0038; {@code
	 * locus-v2-existence-components.md} section 15.7), computed once per revision inside a
	 * counted scope and shared by every consumer. A selected SplineV2 root is certified by
	 * its particular case of the contract (D-CU2-9).
	 *
	 * @return certificate of the current revision, or {@code null} before the slice of the
	 *         first revision is built
	 */
	public HybridExistenceCertifier2D.Result getHybridExistenceCertificate() {
		ReconstructibleLocusEvaluator2D current = evaluator;
		if (current == null || descriptor == null) {
			return null;
		}
		String limitation = upstreamLimitation();
		if (certifiedEvaluator != current
				|| !Objects.equals(certifiedLimitation, limitation)) {
			LocusExistenceStructure2D previous = certificate == null ? null
					: certificate.getStructure();
			String previousSelection = certifiedSelection;
			certificate = LocusCertificationScope2D.certify(getLocus().getInstrumentation(),
					() -> declared(certificate(current, limitation), limitation));
			certifiedSelection = current.captureCertifiedProgramV3()
					.map(CertifiedConstructionProgram2D::getSelectionSignature).orElse(null);
			certificateLineage = LocusComponentLineage2D.transitions(previous,
					certificate.getStructure(),
					Objects.equals(previousSelection, certifiedSelection));
			certifiedEvaluator = current;
			certifiedLimitation = limitation;
		}
		return certificate;
	}

	/**
	 * Lineage transitions of the components of the certificate from the previous one
	 * computed for this locus ({@code locus-v2-existence-components.md} section 15.6); the
	 * first certificate after loading relates to nothing earlier.
	 *
	 * @return transitions into the current certificate, empty before the first one
	 */
	public List<LocusLineage2D> getHybridLineage() {
		return getHybridExistenceCertificate() == null ? List.of() : certificateLineage;
	}

	/**
	 * Exploratory evidence of the unresolved bands of the published certificate (decision
	 * E-1); it is not part of the definition and never certifies.
	 */
	@Override
	public LocusExploratoryEvidence2D getExploratoryEvidence(String branchKey) {
		HybridExistenceCertifier2D.Result current = certificate;
		return current == null || certifiedEvaluator != evaluator
				|| !SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY.equals(branchKey)
						? null : current.getExploration();
	}

	/**
	 * Lineage of the published branch: the transition into the components of the current
	 * certificate, that is the last of {@link #getHybridLineage()}; a disappearance that has
	 * a separate appearance stays in that list.
	 */
	private LocusLineage2D branchLineage() {
		LocusLineage2D lineage = LocusLineage2D.unchanged();
		for (LocusLineage2D transition : certificateLineage) {
			lineage = transition;
		}
		return lineage;
	}

	private HybridExistenceCertifier2D.Result certificate(
			ReconstructibleLocusEvaluator2D current, String limitation) {
		LocusInterval2D domain = descriptor.getDeclaredDomain();
		boolean periodic = descriptor.isPeriodic();
		String key = SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY;
		HybridExistenceCertifier2D.Policy policy = HybridExistenceCertifier2D.Policy.DEFAULT;
		LocusInstrumentation2D instrumentation = getLocus().getInstrumentation();
		if (!current.isReconstructionFaithful()) {
			// A proof that uses no unreproduced selection may still establish that no point
			// exists on the whole domain (normative precision D-CU2-T11-R1); otherwise the
			// evaluations of an unfaithful slice fail explicitly: nothing to explore.
			Optional<HybridExistenceCertifier2D.Result> empty =
					selectionIndependentNonexistence(current, domain, periodic, key, policy,
							instrumentation);
			if (empty.isPresent()) {
				return empty.get();
			}
			return HybridExistenceCertifier2D.notEstablished(domain, periodic, key,
					current.getFidelityReport().isSelectionOnly()
							? LocusCoverageCause2D.SELECTION_NOT_REPRODUCED
							: LocusCoverageCause2D.RECONSTRUCTION_FIDELITY_NOT_ESTABLISHED,
					current.getProgramV3Refusal(), null, policy, instrumentation);
		}
		try (LocusEvaluationSession2D session =
				LocusEvaluationSession2D.openServiceSession(false, 1)) {
			// Pointwise validity of the slice, the only input of exploratory evidence; the
			// reconstructible evaluator reads neither a definition nor a branch.
			HybridExistenceCertifier2D.Sampler sampler = parameter ->
					LocusCertificationScope2D.explore(() -> current.evaluate(null, null,
							parameter, session).isValid());
			Optional<LocusExistenceStructure2D> selectedRoot =
					SelectedRootContinuousDomainCertifier2D.certify(descriptor,
							dependentPoint, state, support);
			if (selectedRoot.isPresent()) {
				return HybridExistenceCertifier2D.particular(selectedRoot.get(), sampler,
						policy, instrumentation);
			}
			if (limitation != null) {
				return HybridExistenceCertifier2D.notEstablished(domain, periodic, key,
						LocusCoverageCause2D.UPSTREAM_NOT_ESTABLISHED, limitation, sampler,
						policy, instrumentation);
			}
			if (dependsOnSelectedIntersectionRoot()) {
				return HybridExistenceCertifier2D.notEstablished(domain, periodic, key,
						LocusCoverageCause2D.NOT_CERTIFIED,
						"selected-root-continuity-not-established", sampler, policy,
						instrumentation);
			}
			Optional<CertifiedConstructionProgram2D> program =
					current.captureCertifiedProgramV3();
			if (!program.isPresent()) {
				String refusal = current.getProgramV3Refusal();
				return HybridExistenceCertifier2D.notEstablished(domain, periodic, key,
						"unsettled-selection".equals(refusal)
								? LocusCoverageCause2D.NOT_CERTIFIED
								: LocusCoverageCause2D.OPERATION_OUTSIDE_CLASS,
						refusal == null ? "outside-class" : refusal, sampler, policy,
						instrumentation);
			}
			return HybridExistenceCertifier2D.certify(
					new ConstructionBoxModel2D(program.get()), current.getMetricStructuresV3(),
					current.getOperationIdentitiesV3(), domain, periodic, key, sampler, policy,
					instrumentation);
		}
	}

	/**
	 * Nonexistence on the whole domain proved without the selections that the slice copy
	 * does not reproduce ({@code selection-independent-nonexistence/v1}, normative precision
	 * D-CU2-T11-R1): applicable only when every remaining discrepancy is the selection of a
	 * line-conic intersection; never a general bypass of the fidelity validation.
	 *
	 * @return the complete certificate with no component, or empty without such a proof
	 */
	private static Optional<HybridExistenceCertifier2D.Result> selectionIndependentNonexistence(
			ReconstructibleLocusEvaluator2D current, LocusInterval2D domain, boolean periodic,
			String key, HybridExistenceCertifier2D.Policy policy,
			LocusInstrumentation2D instrumentation) {
		if (!current.getFidelityReport().isSelectionOnly()) {
			return Optional.empty();
		}
		return current.captureSelectionIndependentProgram().flatMap(program ->
				HybridExistenceCertifier2D.selectionIndependentNonexistence(
						new ConstructionBoxModel2D(program.getProgram()),
						program.getUnreproducedNodes(), program.getOperations(), domain,
						periodic, key, policy, instrumentation));
	}

	/**
	 * The certificate restricted to the components that the generator declares for its
	 * driver domain, a scalar domain list or the components of a complete locus support:
	 * outside them the locus has no driver parameter (ADR 0027). A support whose components
	 * are not established declares nothing, so nothing is restricted.
	 */
	private HybridExistenceCertifier2D.Result declared(HybridExistenceCertifier2D.Result result,
			String limitation) {
		List<LocusInterval2D> components = descriptor.getValidComponents();
		if (limitation != null || components.size() == 1
				&& components.get(0).equals(descriptor.getDeclaredDomain())) {
			return result;
		}
		return result.restrictedTo(components, family.getProviderId() + "/declared-components");
	}

	/**
	 * The limitation that a dependent on a locus support declares (consumption design
	 * section 3; ADR 0027): its completeness is never above the support's, and a support
	 * with point components is not composed.
	 *
	 * @return the limitation, or {@code null} when the support is complete without point
	 *         components or the locus has no locus support
	 */
	private String upstreamLimitation() {
		if (family != SemanticGeneratorFamily1D.LOCUS_BRANCH_POINT
				|| !(support instanceof GeoLocusV2) || descriptor == null) {
			return null;
		}
		LocusDefinition2D sourceDefinition = ((GeoLocusV2) support).getSemanticDefinition();
		LocusBranch2D sourceBranch = sourceDefinition == null
				|| descriptor.getSupportBranchKey() == null ? null
						: sourceDefinition.getBranch(descriptor.getSupportBranchKey());
		if (sourceBranch == null || sourceBranch.getExistenceStructure().getCompleteness()
				!= LocusExistenceStructure2D.Completeness.COMPLETE) {
			return "upstream-locus-coverage-not-established";
		}
		return sourceBranch.getExistenceStructure().getPointComponents().isEmpty() ? null
				: "upstream-point-components-not-composed";
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
		HybridExistenceCertifier2D.Result published = getHybridExistenceCertificate();
		ReconstructibleLocusEvaluator2D current = certifiedEvaluator;
		Optional<CertifiedConstructionProgram2D> program = current == null
				? Optional.empty() : current.captureCertifiedProgramV3();
		if (published == null || !program.isPresent()
				|| !published.getStructure().isClosedPeriodic()) {
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
			// The canonical pieces of the complete inventory of the support; an arc
			// across the seam is composed by the existence structure (PRE-G9B-CU2).
			List<LocusInterval2D> pieces = new ArrayList<>();
			if (branch.getExistenceStructure().getCompleteness()
					== LocusExistenceStructure2D.Completeness.COMPLETE) {
				for (LocusExistenceStructure2D.ContinuousComponent component
						: branch.getExistenceStructure().getContinuousValidComponents()) {
					pieces.addAll(component.getCanonicalPieces());
				}
			}
			return new DomainSnapshot(branch.getDeclaredDriverDomain(), pieces,
					branch.getOrientation(), definition.getProvider().isPeriodic(),
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
