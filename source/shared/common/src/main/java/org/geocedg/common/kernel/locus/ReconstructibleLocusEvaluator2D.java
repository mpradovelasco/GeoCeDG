/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.geocedg.common.kernel.algos.AlgoLocusBetweenMetricV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.ConstructionFidelity;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationMethod;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.RepresentationRole;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxModel2D;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricFunctionStructure2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.MacroKernel;
import org.geogebra.common.kernel.Path;
import org.geogebra.common.kernel.PathParameter;
import org.geogebra.common.kernel.SetRandomValue;
import org.geogebra.common.kernel.algos.AlgoDependentPoint;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.algos.ConstructionElement;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.ExpressionValue;
import org.geogebra.common.kernel.arithmetic.MyDouble;
import org.geogebra.common.kernel.arithmetic.MyVecNode;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoConicPart;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.geos.LimitedPath;
import org.geogebra.common.kernel.kernelND.GeoConicNDConstants;
import org.geogebra.common.kernel.kernelND.GeoPointND;

/**
 * Reconstructible isolated construction-slice evaluator. Copies are resolved
 * only through durable IDs; labels, coordinates, samples and XML order are not
 * identity authority.
 */
public final class ReconstructibleLocusEvaluator2D implements LocusEvaluator2D,
		CertifiedAffineLocus2D {
	private static final LocusQuality2D QUALITY = new LocusQuality2D(
			ConstructionFidelity.SEMANTICALLY_CONSTRUCTED,
			EvaluationMethod.DETERMINISTIC_NUMERIC_DEPENDENCY,
			RepresentationRole.SEMANTIC_RESULT,
			NumericGuarantee.FLOATING_POINT_UNCERTIFIED);
	/**
	 * Strict total order of the slice: construction index, then creation id. The
	 * natural order is not total (geos compare creation ids, algorithms
	 * construction indices, and an inline literal outside the construction list
	 * disagrees with both), and Macro.buildMacroXML re-adds its closure to the set,
	 * which would otherwise write an algorithm twice and duplicate the driver.
	 */
	private static final Comparator<ConstructionElement> RECONSTRUCTION_ORDER =
			Comparator.comparingInt(ConstructionElement::getConstructionIndex)
					.thenComparingLong(ConstructionElement::getID);
	/**
	 * Version of the settlement of the inherited selection of a reconstructed slice
	 * (normative precision D-CU2-T11-R1 of D-CU2-17 and D-CU2-20-R).
	 */
	public static final String SELECTION_SETTLEMENT = "selection-settlement/v1";
	/**
	 * Auxiliary driver parameters of a settlement: the interior points of the dyadic grid of
	 * level 6 of the declared domain, in radical-inverse order.
	 */
	public static final int SETTLEMENT_PARAMETERS = 63;

	private final SemanticGeneratorDescriptor1D descriptor;
	private final Construction isolatedConstruction;
	private final Map<PersistentGeoId, GeoElement> originals = new LinkedHashMap<>();
	private final Map<PersistentGeoId, GeoElement> isolated = new LinkedHashMap<>();
	private final GeoPointND isolatedDependentPoint;
	private final GeoElement isolatedState;
	private final GeoElement isolatedCoordinate;
	private final GeoElement isolatedSupport;
	private final double[][] certifiedAffineCoefficients;
	private final long sliceScope = LocusEvaluationSession2D.newSliceScope();
	private final List<GeoElement> liveSlice = new ArrayList<>();
	private final List<GeoElement> liveDriverRoots = new ArrayList<>();
	private final LocusInstrumentation2D fidelityOwner;
	private ReconstructionFidelity2D.Report fidelity;
	private final Optional<CertifiedConstructionProgram2D> programV3;
	private String programV3Refusal;
	private Map<Integer, AlgoLocusBetweenMetricV2> programV3MetricSources = Map.of();
	private Map<Integer, LocusMetricFunctionStructure2D> metricStructuresV3;
	private List<CertifiedConstructionCapture2D.StepOrigin> programV3Origins = List.of();
	private List<String> operationIdentitiesV3;
	private ConstructionBoxModel2D boxModelV3;
	private final CertifiedConstructionCapture2D.Composition compositionV3;
	/** Copy intersections settled by a point query that ended their first initialization. */
	private final Set<AlgoIntersectLineConic> departedSelections =
			Collections.newSetFromMap(new IdentityHashMap<>());
	/** Copy intersections in their first initialization, like their live ones. */
	private List<AlgoIntersectLineConic> departureWatch = List.of();
	private SettlementOutcome settlement = SettlementOutcome.NOT_NEEDED;
	private int settlementParameters;
	private boolean selectionLost;

	/** Builds one immutable identity-addressed construction slice. */
	public ReconstructibleLocusEvaluator2D(Construction construction,
			SemanticGeneratorDescriptor1D descriptor,
			Collection<GeoElement> reconstructionSlice) {
		this(construction, descriptor, reconstructionSlice, null);
	}

	/**
	 * Builds one immutable identity-addressed construction slice whose nested
	 * Locus V2 copies report their work to the owner's instrumentation
	 * (PRE-G9B-CU1).
	 *
	 * @param nestedWorkOwner instrumentation of the locus that owns the slice,
	 *        or {@code null}
	 */
	public ReconstructibleLocusEvaluator2D(Construction construction,
			SemanticGeneratorDescriptor1D descriptor,
			Collection<GeoElement> reconstructionSlice,
			LocusInstrumentation2D nestedWorkOwner) {
		Objects.requireNonNull(construction);
		this.descriptor = Objects.requireNonNull(descriptor);
		SpatialIdentityRegistry sourceRegistry = construction
				.getSpatialIdentityRegistry();
		Set<ConstructionElement> elements = new TreeSet<>(RECONSTRUCTION_ORDER);
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
		certifiedAffineCoefficients = directScalarAffineCertificate();
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
		for (ConstructionElement element : elements) {
			if (element instanceof GeoElement) {
				liveSlice.add((GeoElement) element);
			}
		}
		liveDriverRoots.add(originals.get(descriptor.getStateId()));
		liveDriverRoots.add(originals.get(descriptor.getCoordinateId()));
		fidelityOwner = nestedWorkOwner;
		isolatedConstruction = macroKernel.getConstruction();
		withholdPendingLabels();
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
		if (nestedWorkOwner != null) {
			for (GeoElement geo : isolatedConstruction.getGeoSetConstructionOrder()) {
				if (geo instanceof GeoLocusV2) {
					((GeoLocusV2) geo).attachNestedWorkParent(nestedWorkOwner);
				}
			}
		}
		resetExternalState();
		fidelity = validateFidelity();
		programV3 = captureV3();
		compositionV3 = captureCompositionV3();
	}

	/**
	 * @return whether the slice copy reproduces every authoritative input it reads
	 *         (PRE-G9B-CU2, D-CU2-20-R)
	 */
	public synchronized boolean isReconstructionFaithful() {
		return fidelity.isFaithful();
	}

	/** @return report of the last fidelity validation of the slice copy */
	public synchronized ReconstructionFidelity2D.Report getFidelityReport() {
		return fidelity;
	}

	/**
	 * @return outcome of the settlement of the inherited selection of this revision
	 *         ({@value #SELECTION_SETTLEMENT})
	 */
	public synchronized SettlementOutcome getSettlementOutcome() {
		return settlement;
	}

	/** @return auxiliary driver parameters computed by the settlements of this revision */
	public synchronized int getSettlementParameters() {
		return settlementParameters;
	}

	/**
	 * @return copy intersections that a point query took out of their first initialization
	 *         and that the evaluator settled at that parameter
	 */
	public synchronized int getDepartedSelections() {
		return departedSelections.size();
	}

	/**
	 * Evaluates one canonical parameter. The slice was synchronized with its
	 * inputs once, when it was built for the current revision; a point query only
	 * applies the driver coordinate and updates the driver-dependent subgraph
	 * (PRE-G9B-CU1, {@code locus-v2-semantics.md} v1.2). Services that run inside
	 * that update open slice-scoped child sessions of {@code session}.
	 */
	@Override
	public synchronized LocusEvaluation2D evaluate(LocusDefinition2D definition,
			LocusBranch2D branch, double canonicalParameter,
			LocusEvaluationSession2D session) {
		if (!fidelity.isFaithful()) {
			return LocusEvaluation2D.invalid(
					LocusSemanticMetadata2D.EvaluationStatus.EVALUATION_FAILED, QUALITY,
					"RECONSTRUCTION_FIDELITY_NOT_ESTABLISHED: " + fidelity);
		}
		try {
			LocusEvaluationSession2D.runWithNestedServiceParent(session, sliceScope, () -> {
				applyCoordinate(canonicalParameter);
				settleDepartures(() -> applyCoordinate(canonicalParameter));
			});
			String departure = departureRefusal();
			if (departure != null) {
				recordWork(LocusCertificationWork2D.SELECTION_DEPARTURE_REFUSAL, 1);
				return LocusEvaluation2D.invalid(
						LocusSemanticMetadata2D.EvaluationStatus.EVALUATION_FAILED, QUALITY,
						"SELECTION_NOT_REPRODUCED: " + departure);
			}
			GeoElement pointGeo = isolatedDependentPoint.toGeoElement();
			if (!pointGeo.isDefined() || isolatedDependentPoint.isInfinite()
					|| !Double.isFinite(isolatedDependentPoint.getInhomX())
					|| !Double.isFinite(isolatedDependentPoint.getInhomY())) {
				String computational = computationalMetricFailure();
				if (computational != null) {
					// PRE-G9B-CU2, P5: a computational failure of a dependency is not
					// nonexistence (locus-v2-existence-components.md section 15.5).
					return LocusEvaluation2D.invalid(
							LocusSemanticMetadata2D.EvaluationStatus.EVALUATION_FAILED,
							QUALITY, LocusDefinition2D.COMPUTATIONAL_FAILURE + ": "
									+ computational);
				}
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

	/**
	 * Evaluates the one-sided limit at the excluded end of an origin wrap of a length of the
	 * slice ({@code locus-v2-metrics.md} section 26.2): the length is continuous on each side
	 * and its limit on the excluded side is its value at the origin under the opposite
	 * same-position policy, which the copy of the length uses for this one evaluation
	 * (consumption design section 5.3). Any other excluded end fails explicitly.
	 */
	@Override
	public synchronized LocusEvaluation2D evaluateOneSidedLimit(LocusDefinition2D definition,
			LocusBranch2D branch, LocusExistenceStructure2D.ContinuousComponent component,
			boolean upperEnd, LocusEvaluationSession2D session) {
		LocusBoundary2D end = upperEnd ? component.getUpperBoundary()
				: component.getLowerBoundary();
		double parameter = upperEnd ? component.getEnd() : component.getStart();
		AlgoLocusBetweenMetricV2 metric = originWrapMetric(end, parameter, upperEnd);
		if (metric == null) {
			return LocusEvaluation2D.invalid(
					LocusSemanticMetadata2D.EvaluationStatus.EVALUATION_FAILED, QUALITY,
					LocusEvaluator2D.ONE_SIDED_LIMIT_NOT_AVAILABLE + ": " + end.getKind() + " "
							+ end.getSource());
		}
		metric.setOppositeSamePositionForLimit(true);
		try {
			return evaluate(definition, branch, parameter, session);
		} finally {
			metric.setOppositeSamePositionForLimit(false);
		}
	}

	/**
	 * Encloses the image of a certified root of the class-v3 program of this revision with the
	 * limit form of the certificate (certification design section 3.4): the box model of the
	 * program assumes the predicate of the root on its certified enclosure. The source names
	 * the step by the durable operation identity the certificate used.
	 */
	@Override
	public synchronized LocusBoundaryImage2D encloseBoundaryImage(
			LocusDefinition2D definition, LocusBranch2D branch, double parameterLower,
			double parameterUpper, LocusBoundarySource2D source) {
		if (!programV3.isPresent()) {
			return LocusBoundaryImage2D.notAvailable(LocusEvaluator2D.LIMIT_FORM_NOT_AVAILABLE
					+ ": no class-v3 program (" + programV3Refusal + ")");
		}
		if (boxModelV3 == null) {
			boxModelV3 = new ConstructionBoxModel2D(programV3.get());
		}
		double[] enclosure = HybridExistenceCertifier2D.encloseRootImage(boxModelV3,
				getOperationIdentitiesV3(), source, parameterLower, parameterUpper,
				descriptor.isPeriodic());
		return enclosure == null ? LocusBoundaryImage2D.notAvailable(
				LocusEvaluator2D.LIMIT_FORM_NOT_AVAILABLE
						+ ": the limit form does not enclose the image of " + source)
				: LocusBoundaryImage2D.enclosed(enclosure);
	}

	/**
	 * @return the copy of the length whose certified origin wrap is the excluded end, or
	 *         {@code null}
	 */
	private AlgoLocusBetweenMetricV2 originWrapMetric(LocusBoundary2D end, double parameter,
			boolean upperEnd) {
		if (end.isBinaryEndIncluded()
				|| end.getKind() != LocusBoundary2D.Kind.STRUCTURAL_EVENT
						&& end.getKind() != LocusBoundary2D.Kind.UNRESOLVED_BAND_EDGE
				|| !"ORIGIN_WRAP".equals(end.getSource().getPredicate())
				|| !(upperEnd ? "end" : "start").equals(end.getSource().getSide())) {
			return null;
		}
		int node = getOperationIdentitiesV3().indexOf(end.getSource().getOrigin()) + 1;
		AlgoLocusBetweenMetricV2 metric = programV3MetricSources.get(node);
		LocusMetricFunctionStructure2D structure = getMetricStructuresV3().get(node);
		if (metric == null || structure == null) {
			return null;
		}
		for (LocusMetricFunctionStructure2D.OriginWrap wrap : structure.getEvents()) {
			// The excluded side is the one the persisted same-position policy leaves out.
			if (wrap.getDriverParameter() == parameter
					&& wrap.isStartIncluded() == upperEnd) {
				return metric;
			}
		}
		return null;
	}

	/**
	 * @return description of a computational failure of a length of the slice on which
	 *         the traced point depends, or {@code null} when none failed computationally
	 */
	private String computationalMetricFailure() {
		GeoElement pointGeo = isolatedDependentPoint.toGeoElement();
		for (AlgoElement algorithm : isolatedConstruction.getAlgoList()) {
			if (!(algorithm instanceof AlgoLocusBetweenMetricV2)) {
				continue;
			}
			AlgoLocusBetweenMetricV2 metric = (AlgoLocusBetweenMetricV2) algorithm;
			MetricComputationStatus status = metric.getCurrentComputationStatus();
			if ((status == MetricComputationStatus.NUMERICAL_FAILURE
					|| status == MetricComputationStatus.LIMIT_NOT_ESTABLISHED
					|| status == MetricComputationStatus.UNSUPPORTED)
					&& pointGeo.isChildOf(metric.getResult())) {
				return "dependency length " + metric.getResult().getLabelSimple() + " "
						+ status;
			}
		}
		return null;
	}

	/** @return deterministic evaluator reconstruction signature */
	public String getEvaluatorSignature() {
		return "reconstructible-construction-slice/v1|"
				+ descriptor.getSemanticSignature() + "|affine="
				+ getCertifiedAffineSignature() + "|fidelity="
				+ ReconstructionFidelity2D.VERSION + ":"
				+ (fidelity.isFaithful() ? "faithful" : "unfaithful");
	}

	@Override
	public boolean supportsCertifiedAffine(LocusDefinition2D definition) {
		return certifiedAffineCoefficients != null && !descriptor.isPeriodic()
				&& definition != null
				&& definition.getBranch(SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY)
						!= null;
	}

	@Override
	public double[] getCertifiedAffineCoefficients(String branchKey,
			int coordinate) {
		if (certifiedAffineCoefficients == null
				|| !SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY.equals(branchKey)
				|| coordinate < 0 || coordinate > 1) {
			throw new IllegalArgumentException(
					"No certified affine coefficients for the requested branch");
		}
		return certifiedAffineCoefficients[coordinate].clone();
	}

	@Override
	public String getCertifiedAffineSignature() {
		if (certifiedAffineCoefficients == null) {
			return "none";
		}
		return "direct-scalar-cartesian/v1|x="
				+ Double.toHexString(certifiedAffineCoefficients[0][0]) + ","
				+ Double.toHexString(certifiedAffineCoefficients[0][1]) + "|y="
				+ Double.toHexString(certifiedAffineCoefficients[1][0]) + ","
				+ Double.toHexString(certifiedAffineCoefficients[1][1]);
	}

	/**
	 * Captures the certified construction program (class v1 or v2) of this
	 * evaluator's own isolated slice ({@code locus-v2-certified-construction-model.md}
	 * section 3). Capture never mutates the live construction and never throws.
	 *
	 * @return current program, or empty when the slice is outside the class
	 */
	public synchronized Optional<CertifiedConstructionProgram2D>
			captureCertifiedProgram() {
		try {
			resetExternalState();
			settleDepartures(this::updateCopy);
			fidelity = validateFidelity();
		} catch (RuntimeException exception) {
			return Optional.empty();
		}
		if (!fidelity.isFaithful()) {
			return Optional.empty();
		}
		return CertifiedConstructionCapture2D.capture(descriptor,
				isolatedDependentPoint.toGeoElement(), isolatedState, isolatedSupport,
				certifiedAffineCoefficients);
	}

	/**
	 * Returns the certified construction program of class v3 of this evaluator's own
	 * isolated slice, for existence certification
	 * ({@code locus-v2-certified-construction-model.md} section 10.1). It is captured once,
	 * when the slice is built for its revision and validated: the selection state of the
	 * copy then derives from the live inputs only, and no later point query, whose order
	 * would otherwise reach the inherited proximity reinitialization of the copy (F-CU2-8),
	 * changes it. A slice whose fidelity is not established, or whose live selection is not
	 * yet settled, yields no program.
	 *
	 * @return program of the revision, or empty when the slice is outside the class
	 */
	public synchronized Optional<CertifiedConstructionProgram2D>
			captureCertifiedProgramV3() {
		return programV3;
	}

	/**
	 * @return typed reason why the revision has no class-v3 program
	 *         ({@code reconstruction-fidelity-not-established}, {@code unsettled-selection},
	 *         {@code step-limit-exceeded}, {@code outside-class} or an unexpected shape), or
	 *         {@code null} when it has one
	 */
	public synchronized String getProgramV3Refusal() {
		return programV3Refusal;
	}

	/**
	 * Structural certificates of the metric inputs of the class-v3 program of this revision
	 * ({@code locus-v2-metrics.md} section 26), by program node, computed once from the rich
	 * lengths of the isolated slice. Only an input whose origin is fixed and whose target is
	 * the generator occurrence of a source driven by the same driver point has one; the
	 * computation never reads a metric value.
	 *
	 * @return structure of each eligible metric input node
	 */
	public synchronized Map<Integer, LocusMetricFunctionStructure2D> getMetricStructuresV3() {
		if (metricStructuresV3 == null) {
			Map<Integer, LocusMetricFunctionStructure2D> structures = new LinkedHashMap<>();
			for (Map.Entry<Integer, AlgoLocusBetweenMetricV2> entry
					: programV3MetricSources.entrySet()) {
				structures.put(entry.getKey(), entry.getValue().getFunctionStructure());
			}
			metricStructuresV3 = Collections.unmodifiableMap(structures);
		}
		return metricStructuresV3;
	}

	/**
	 * Steps from the generator point of a locus support to the traced point, for the minimal
	 * certified composition ({@code locus-v2-certified-construction-model.md} section 10.10,
	 * author decision V-CON-6), captured once with the class-v3 program of the revision.
	 *
	 * @return the steps, or empty with {@link #getCompositionRefusal()}
	 */
	public synchronized Optional<List<CertifiedConstructionProgram2D.Step>>
			getCompositionStepsV3() {
		return compositionV3.getSteps();
	}

	/**
	 * @return typed reason why the revision has no composition steps, or {@code null} when it
	 *         has them
	 */
	public synchronized String getCompositionRefusal() {
		return compositionV3.getRefusal();
	}

	/**
	 * Captures the composition steps of a dependent on a locus support from the freshly
	 * validated slice, under the fidelity rules of the class-v3 program; never throws.
	 */
	private CertifiedConstructionCapture2D.Composition captureCompositionV3() {
		if (descriptor.getFamily() != SemanticGeneratorFamily1D.LOCUS_BRANCH_POINT) {
			return CertifiedConstructionCapture2D.Composition.refused(
					CertifiedConstructionCapture2D.OUTSIDE_CLASS);
		}
		if (!fidelity.isFaithful() || !fidelity.getUnsettledSelections().isEmpty()) {
			return CertifiedConstructionCapture2D.Composition.refused(programV3Refusal);
		}
		return CertifiedConstructionCapture2D.captureComposition(
				isolatedDependentPoint.toGeoElement(), isolatedState, isolatedCoordinate);
	}

	/** Captures the class-v3 program of the freshly validated slice; never throws. */
	private Optional<CertifiedConstructionProgram2D> captureV3() {
		if (!fidelity.isFaithful()) {
			// The evidence names the outcome of an attempted settlement (D-CU2-T11-R1).
			programV3Refusal = "reconstruction-fidelity-not-established"
					+ (settlement == SettlementOutcome.NOT_NEEDED
							|| settlement == SettlementOutcome.NOT_APPLICABLE ? ""
									: ";" + SELECTION_SETTLEMENT + "=" + settlement.getToken());
			return Optional.empty();
		}
		if (!fidelity.getUnsettledSelections().isEmpty()) {
			programV3Refusal = "unsettled-selection";
			return Optional.empty();
		}
		CertifiedConstructionCapture2D.Outcome outcome =
				CertifiedConstructionCapture2D.captureWithReason(descriptor,
						isolatedDependentPoint.toGeoElement(), isolatedState, isolatedSupport,
						certifiedAffineCoefficients, true);
		programV3Refusal = outcome.getRefusal();
		programV3MetricSources = outcome.getMetricSources();
		programV3Origins = outcome.getOrigins();
		return outcome.getProgram();
	}

	/**
	 * Durable operation identity of each step of the class-v3 program
	 * ({@code locus-v2-existence-components.md} section 15.6): the durable identity of the
	 * live geo that its captured copy reproduces, followed by its path inside expanded macros,
	 * an ordinal among the steps of that geo with the same operation, and the operation. A
	 * step whose anchor has no durable identity keeps its revision-local program address.
	 *
	 * @return token of each step in program order, valid as a boundary source origin
	 */
	public synchronized List<String> getOperationIdentitiesV3() {
		if (operationIdentitiesV3 == null) {
			operationIdentitiesV3 = operationIdentities(programV3Origins);
		}
		return operationIdentitiesV3;
	}

	private List<String> operationIdentities(
			List<CertifiedConstructionCapture2D.StepOrigin> origins) {
		// The slice carries the durable identities of its live geos.
		SpatialIdentityRegistry registry = isolatedConstruction.getSpatialIdentityRegistry();
		List<String> identities = new ArrayList<>();
		Map<String, Integer> counts = new LinkedHashMap<>();
		for (int index = 0; index < origins.size(); index++) {
			CertifiedConstructionCapture2D.StepOrigin origin = origins.get(index);
			PersistentGeoId id = origin.getAnchor() == null ? null
					: registry.getPersistentGeoId(origin.getAnchor());
			if (id == null) {
				identities.add("class-v3-step-" + (index + 1) + "-" + origin.getOperation());
				continue;
			}
			String base = id.toExternalForm() + origin.getMacroPath();
			int ordinal = counts.merge(base + "-" + origin.getOperation(), 1, Integer::sum)
					- 1;
			identities.add(base + (ordinal == 0 ? "" : "-" + ordinal) + "-"
					+ origin.getOperation());
		}
		return Collections.unmodifiableList(identities);
	}

	/**
	 * Class-v3 program of a revision whose only remaining discrepancies are selections of
	 * line-conic intersections, for a proof of nonexistence that uses none of those
	 * selections ({@code selection-independent-nonexistence/v1}, normative precision
	 * D-CU2-T11-R1). The program is captured from the copy as it is; the selection that it
	 * holds for those intersections is never evidence, and the revision stays unfaithful.
	 *
	 * @return the program with the nodes of the unreproduced intersections, or empty when the
	 *         revision is faithful, when a discrepancy is anything else, or when the capture
	 *         refuses the copy
	 */
	public synchronized Optional<SelectionIndependentProgram>
			captureSelectionIndependentProgram() {
		if (fidelity.isFaithful()) {
			return Optional.empty();
		}
		Map<String, ReconstructionFidelity2D.SelectionPair> pairs = new LinkedHashMap<>();
		for (ReconstructionFidelity2D.SelectionPair pair : fidelity.getSelectionPairs()) {
			pairs.put(pair.getOwner(), pair);
		}
		Set<AlgoElement> unreproduced = Collections.newSetFromMap(new IdentityHashMap<>());
		for (ReconstructionFidelity2D.Discrepancy discrepancy : fidelity.getDiscrepancies()) {
			ReconstructionFidelity2D.SelectionPair pair = discrepancy.getKind()
					== ReconstructionFidelity2D.DiscrepancyKind.SELECTION
							? pairs.get(discrepancy.getElement()) : null;
			if (pair == null) {
				return Optional.empty();
			}
			unreproduced.add(pair.getCopy());
		}
		CertifiedConstructionCapture2D.Outcome outcome =
				CertifiedConstructionCapture2D.captureWithReason(descriptor,
						isolatedDependentPoint.toGeoElement(), isolatedState, isolatedSupport,
						certifiedAffineCoefficients, true);
		if (!outcome.getProgram().isPresent()) {
			return Optional.empty();
		}
		Set<Integer> nodes = new TreeSet<>();
		for (AlgoElement copy : unreproduced) {
			Integer node = outcome.getIntersectionNodes().get(copy);
			if (node == null) {
				return Optional.empty();
			}
			nodes.add(node);
		}
		return Optional.of(new SelectionIndependentProgram(outcome.getProgram().get(), nodes,
				operationIdentities(outcome.getOrigins())));
	}

	/**
	 * Program captured from a copy whose listed line-circle steps do not reproduce the live
	 * selection (D-CU2-T11-R1); only a proof that uses none of those selections may read it.
	 */
	public static final class SelectionIndependentProgram {
		private final CertifiedConstructionProgram2D program;
		private final Set<Integer> unreproducedNodes;
		private final List<String> operations;

		private SelectionIndependentProgram(CertifiedConstructionProgram2D program,
				Set<Integer> unreproducedNodes, List<String> operations) {
			this.program = program;
			this.unreproducedNodes = Collections.unmodifiableSet(unreproducedNodes);
			this.operations = operations;
		}

		/** @return program captured from the copy as it is */
		public CertifiedConstructionProgram2D getProgram() {
			return program;
		}

		/** @return nodes of the line-circle steps whose selection is not reproduced */
		public Set<Integer> getUnreproducedNodes() {
			return unreproducedNodes;
		}

		/** @return durable identity token of each step, in program order */
		public List<String> getOperations() {
			return operations;
		}
	}

	private double[][] directScalarAffineCertificate() {
		if (descriptor.getFamily() != SemanticGeneratorFamily1D.SCALAR_STATE) {
			return null;
		}
		GeoElement dependent = originals.get(descriptor.getDependentPointId());
		GeoElement coordinate = originals.get(descriptor.getCoordinateId());
		if (!(coordinate instanceof GeoNumeric)
				|| !(dependent != null
						&& dependent.getParentAlgorithm() instanceof AlgoDependentPoint)
				|| dependent.getDefinition() == null) {
			return null;
		}
		ExpressionValue unwrapped = dependent.getDefinition().unwrap();
		if (!(unwrapped instanceof MyVecNode)
				|| ((MyVecNode) unwrapped).hasPolarCoords()) {
			return null;
		}
		MyVecNode vector = (MyVecNode) unwrapped;
		double[] x = directAffineCoordinate(vector.getX(), coordinate);
		double[] y = directAffineCoordinate(vector.getY(), coordinate);
		if (x == null || y == null || x[0] == 0 && y[0] == 0) {
			return null;
		}
		return new double[][] {x, y};
	}

	private static double[] directAffineCoordinate(ExpressionValue expression,
			GeoElement coordinate) {
		ExpressionValue value = expression instanceof ExpressionNode
				? ((ExpressionNode) expression).unwrap() : expression;
		if (value == coordinate) {
			return new double[] {1, 0};
		}
		if (value instanceof MyDouble
				&& Double.isFinite(((MyDouble) value).getDouble())) {
			return new double[] {0, ((MyDouble) value).getDouble()};
		}
		return null;
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
			// The update path of AlgoPointOnPath: GeoSegment.pathChanged leaves the
			// inhomogeneous coordinates to updateCoords, which conic paths refresh
			// themselves; without it a segment driver lags one parameter behind.
			((Path) isolatedSupport).pathChanged(point);
			point.updateCoords();
			point.toGeoElement().updateCascade();
			break;
		default:
			throw new IllegalStateException("Unsupported semantic generator family");
		}
	}

	/**
	 * Validates the copy against the live slice, restores the admitted discrepancies, settles
	 * the inherited selection of the copy where the contract admits it and counts the work
	 * (PRE-G9B-CU2, D-CU2-20-R; {@value #SELECTION_SETTLEMENT}, D-CU2-T11-R1).
	 */
	private ReconstructionFidelity2D.Report validateFidelity() {
		ReconstructionFidelity2D.Report report = ReconstructionFidelity2D.validate(
				liveSlice, isolatedConstruction, liveDriverRoots, true, departedSelections);
		recordWork(LocusCertificationWork2D.FIDELITY_VALIDATION, 1);
		recordWork(LocusCertificationWork2D.FIDELITY_RESTORATION, report.getRestorations());
		List<AlgoIntersectLineConic> targets = settlementTargets(report);
		if (!targets.isEmpty()) {
			recordWork(LocusCertificationWork2D.SELECTION_SETTLEMENT, 1);
			SettlementOutcome outcome = settle(targets, report);
			report = ReconstructionFidelity2D.validate(liveSlice, isolatedConstruction,
					liveDriverRoots, false, departedSelections);
			recordWork(LocusCertificationWork2D.FIDELITY_VALIDATION, 1);
			if (outcome == SettlementOutcome.SETTLED && !report.isFaithful()) {
				outcome = SettlementOutcome.STATE_DIFFERS;
			}
			if (outcome != SettlementOutcome.SETTLED) {
				recordWork(LocusCertificationWork2D.SELECTION_SETTLEMENT_FAILURE, 1);
			}
			settlement = outcome;
		} else if (!report.isFaithful()) {
			settlement = SettlementOutcome.NOT_APPLICABLE;
		}
		if (!report.isFaithful()) {
			recordWork(LocusCertificationWork2D.FIDELITY_FAILURE, 1);
		}
		departureWatch = initializingCopies(report);
		return report;
	}

	private void recordWork(LocusCertificationWork2D kind, int count) {
		if (fidelityOwner != null) {
			fidelityOwner.recordCertificationWork(kind, count);
		}
	}

	/**
	 * Precondition 1 of {@value #SELECTION_SETTLEMENT}: every remaining discrepancy is the
	 * selection of a line-conic intersection whose live algorithm is settled and whose copy
	 * is not, with two outputs, no existing intersection and a kernel that is not continuous.
	 *
	 * @return intersections of the copy to settle, or an empty list when the contract does
	 *         not admit a settlement
	 */
	private static List<AlgoIntersectLineConic> settlementTargets(
			ReconstructionFidelity2D.Report report) {
		if (report.isFaithful()) {
			return List.of();
		}
		Map<String, ReconstructionFidelity2D.SelectionPair> pairs = new LinkedHashMap<>();
		for (ReconstructionFidelity2D.SelectionPair pair : report.getSelectionPairs()) {
			pairs.put(pair.getOwner(), pair);
		}
		List<AlgoIntersectLineConic> targets = new ArrayList<>();
		for (ReconstructionFidelity2D.Discrepancy discrepancy : report.getDiscrepancies()) {
			ReconstructionFidelity2D.SelectionPair pair = discrepancy.getKind()
					== ReconstructionFidelity2D.DiscrepancyKind.SELECTION
							? pairs.get(discrepancy.getElement()) : null;
			if (pair == null || !pair.isLiveSettled()
					|| ReconstructionFidelity2D.isSettled(pair.getCopy())
					|| !settleable(pair.getCopy())) {
				return List.of();
			}
			targets.add(pair.getCopy());
		}
		return targets;
	}

	private static boolean settleable(AlgoIntersectLineConic copy) {
		return copy.getIntersectionPoints().length == 2 && copy.existingIntersections() == 0
				&& !copy.getKernel().isContinuous();
	}

	/**
	 * @return copy intersections in their first initialization like their live ones, which a
	 *         point query may take out of it
	 */
	private static List<AlgoIntersectLineConic> initializingCopies(
			ReconstructionFidelity2D.Report report) {
		List<AlgoIntersectLineConic> copies = new ArrayList<>();
		for (ReconstructionFidelity2D.SelectionPair pair : report.getSelectionPairs()) {
			AlgoIntersectLineConic copy = pair.getCopy();
			if (pair.isLiveFirstPending() && copy.isSelectionFirstIntersectionPending()
					&& copy.getIntersectionPoints().length == 2) {
				copies.add(copy);
			}
		}
		return copies;
	}

	/**
	 * Settles the targets through the inherited computation of the copy at the auxiliary
	 * parameters of {@value #SELECTION_SETTLEMENT}: two computations per parameter, in a
	 * fixed order, until no target is left unsettled; the exact driver state of the copy is
	 * restored afterwards. The live construction and every selection state are only read.
	 *
	 * @return typed outcome, before the validation that follows
	 */
	private SettlementOutcome settle(List<AlgoIntersectLineConic> targets,
			ReconstructionFidelity2D.Report report) {
		List<AlgoIntersectLineConic> initializing = initializingCopies(report);
		DriverState saved = new DriverState();
		SettlementOutcome outcome = SettlementOutcome.BUDGET_EXHAUSTED;
		LocusInterval2D domain = descriptor.getDeclaredDomain();
		double width = domain.getUpper() - domain.getLower();
		try {
			for (int index = 1; index <= SETTLEMENT_PARAMETERS; index++) {
				double parameter = domain.getLower() + width * radicalInverse(index);
				applyCoordinate(parameter);
				applyCoordinate(parameter);
				settlementParameters++;
				recordWork(LocusCertificationWork2D.SELECTION_SETTLEMENT_PARAMETER, 1);
				if (leftFirstInitialization(initializing)) {
					outcome = SettlementOutcome.FIRST_PENDING_INITIALIZED;
					break;
				}
				if (targets.stream().allMatch(ReconstructionFidelity2D::isSettled)) {
					outcome = SettlementOutcome.SETTLED;
					break;
				}
			}
		} catch (RuntimeException exception) {
			outcome = SettlementOutcome.COMPUTATION_FAILED;
		}
		if (!saved.restore()) {
			outcome = SettlementOutcome.RESTORATION_DIFFERS;
		}
		return outcome;
	}

	/** @return {@code k}-th term of the base-2 radical inverse sequence: 1/2, 1/4, 3/4, ... */
	static double radicalInverse(int index) {
		double value = 0;
		double scale = 0.5;
		for (int rest = index; rest > 0; rest >>= 1) {
			if ((rest & 1) != 0) {
				value += scale;
			}
			scale /= 2;
		}
		return value;
	}

	private static boolean leftFirstInitialization(List<AlgoIntersectLineConic> copies) {
		for (AlgoIntersectLineConic copy : copies) {
			if (!copy.isSelectionFirstIntersectionPending()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Settles at once, by a second computation at the same parameter, every watched copy
	 * intersection that the last computation took out of its first initialization, so that
	 * no later query depends on which query did it (D-CU2-T11-R1, step 8).
	 *
	 * @param recompute the computation to repeat
	 */
	private void settleDepartures(Runnable recompute) {
		List<AlgoIntersectLineConic> leaving = new ArrayList<>();
		for (AlgoIntersectLineConic copy : departureWatch) {
			if (!departedSelections.contains(copy)
					&& !copy.isSelectionFirstIntersectionPending()) {
				leaving.add(copy);
			}
		}
		if (leaving.isEmpty()) {
			return;
		}
		recompute.run();
		for (AlgoIntersectLineConic copy : leaving) {
			if (ReconstructionFidelity2D.isSettledIdentity(copy)) {
				departedSelections.add(copy);
				recordWork(LocusCertificationWork2D.SELECTION_DEPARTURE, 1);
			} else {
				selectionLost = true;
			}
		}
	}

	/**
	 * @return why the outputs of a departed intersection are not those of the selection
	 *         rule of the revision, the compaction of the roots on the paths in raw order, or
	 *         {@code null}; with the identity permutation they are exactly when every defined
	 *         output lies on both paths as the rule decides it ({@link #onBothPaths}) and
	 *         output 0 is not undefined with output 1 defined
	 */
	private String departureRefusal() {
		if (selectionLost) {
			return "a departed intersection did not settle in the identity permutation";
		}
		for (AlgoIntersectLineConic copy : departedSelections) {
			GeoPoint[] points = copy.getIntersectionPoints();
			boolean first = points[0].isDefined();
			boolean second = points[1].isDefined();
			if (first && !onBothPaths(copy, points[0])
					|| second && !onBothPaths(copy, points[1]) || !first && second) {
				return "the outputs of a departed intersection differ from the compaction"
						+ " rule of the revision";
			}
		}
		return null;
	}

	/** @return whether the driver of the slice is a number, as {@link #applyCoordinate} sets */
	private boolean scalarDriver() {
		return descriptor.getFamily() == SemanticGeneratorFamily1D.SCALAR_STATE
				|| descriptor.getFamily() == SemanticGeneratorFamily1D.LOCUS_BRANCH_POINT;
	}

	/**
	 * @return whether the point lies on both paths as the rule of the revision and its
	 *         certificate decide it: the inherited incidence tests and, on a ray or segment
	 *         that bounds its intersections, its parameter range without the inherited
	 *         tolerance {@link Kernel#MIN_PRECISION}. Within that tolerance before the start,
	 *         the inherited compaction also counts a root that the rule does not, and its
	 *         output 0 is then another root (PRE-G9B-CU2, review finding H-CU2A-8).
	 */
	private static boolean onBothPaths(AlgoIntersectLineConic intersection, GeoPoint point) {
		GeoLine line = intersection.getLine();
		return line.isIntersectionPointIncident(point, Kernel.MIN_PRECISION)
				&& (!(line instanceof LimitedPath)
						|| ((LimitedPath) line).allowOutlyingIntersections()
						|| line.respectLimitedPath(point.getCoordsInD2(), 0))
				&& intersection.getConic().isIntersectionPointIncident(point,
						Kernel.MIN_PRECISION);
	}

	/** Typed outcome of the selection settlement of a revision (D-CU2-T11-R1). */
	public enum SettlementOutcome {
		/** The copy reproduced every selection without a settlement. */
		NOT_NEEDED("not-needed"),
		/** A discrepancy outside the contract: no settlement is attempted. */
		NOT_APPLICABLE("not-applicable"),
		/** Every target settled and the copy was validated faithful. */
		SETTLED("settled"),
		/** No auxiliary parameter of the sequence settled every target. */
		BUDGET_EXHAUSTED("budget-exhausted"),
		/** An auxiliary parameter initialized an intersection whose live one is initializing. */
		FIRST_PENDING_INITIALIZED("first-pending-initialized"),
		/** The validation after the settlement still found a difference. */
		STATE_DIFFERS("state-differs"),
		/** The inherited computation failed at an auxiliary parameter. */
		COMPUTATION_FAILED("computation-failed"),
		/** The exact driver state of the copy could not be restored. */
		RESTORATION_DIFFERS("restoration-differs");

		private final String token;

		SettlementOutcome(String token) {
			this.token = token;
		}

		/** @return token of the outcome in evidence and diagnostics */
		public String getToken() {
			return token;
		}
	}

	/**
	 * Exact driver state of the copy, restored after a settlement (D-CU2-T11-R1, step 6):
	 * the bits of the coordinate number, or the homogeneous coordinates and the path
	 * parameter of the driver point, assigned back without recomputing the position from the
	 * path parameter.
	 */
	private final class DriverState {
		private final double value;
		private final double x;
		private final double y;
		private final double z;
		private final PathParameter path;

		private DriverState() {
			if (scalarDriver()) {
				value = ((GeoNumeric) isolatedCoordinate).getValue();
				x = Double.NaN;
				y = Double.NaN;
				z = Double.NaN;
				path = null;
			} else {
				GeoPoint point = (GeoPoint) isolatedState;
				value = Double.NaN;
				x = point.x;
				y = point.y;
				z = point.z;
				path = new PathParameter(point.getPathParameter());
			}
		}

		/** @return whether the driver state is restored bit for bit */
		private boolean restore() {
			if (path == null) {
				GeoNumeric number = (GeoNumeric) isolatedCoordinate;
				number.setValue(value);
				number.updateCascade();
				return Double.doubleToRawLongBits(number.getValue())
						== Double.doubleToRawLongBits(value);
			}
			GeoPoint point = (GeoPoint) isolatedState;
			point.getPathParameter().set(path);
			point.x = x;
			point.y = y;
			point.z = z;
			point.updateCoords();
			point.updateCascade();
			return Double.doubleToRawLongBits(point.x) == Double.doubleToRawLongBits(x)
					&& Double.doubleToRawLongBits(point.y) == Double.doubleToRawLongBits(y)
					&& Double.doubleToRawLongBits(point.z) == Double.doubleToRawLongBits(z)
					&& Double.doubleToRawLongBits(point.getPathParameter().getT())
							== Double.doubleToRawLongBits(path.getT());
		}
	}

	/**
	 * The copy reproduces the label state of its revision until the next one (finding
	 * H-T13-A, author decision D-CU2-T13-R6): an output that the live construction has not
	 * labelled stays unlabelled in the copy. The inherited element labels a wanted label as
	 * soon as it is first defined, and a point query defines outputs at parameters that the
	 * live driver has not visited; so no unlabelled output of the copy keeps a wanted label.
	 * When the live output gets its label, the label state of the slice changes and the next
	 * revision rebuilds the copy from it.
	 */
	private void withholdPendingLabels() {
		for (AlgoElement algorithm : isolatedConstruction.getAlgoList()) {
			for (GeoElement output : algorithm.getOutput()) {
				if (!output.isLabelSet()) {
					output.setLabelWanted(false);
				}
			}
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
		updateCopy();
	}

	/**
	 * Updates the copy as {@code Construction.updateConstruction(false)} does, without its
	 * reinitialization of the inherited selection state by proximity (F-CU2-8): the copy
	 * keeps the selection state with which its revision was validated, whatever point
	 * queries reached it before (PRE-G9B-CU2, D-CU2-20-R, phase 4).
	 */
	private void updateCopy() {
		for (int index = 0; index < isolatedConstruction.steps(); index++) {
			ConstructionElement element = isolatedConstruction.getConstructionElement(index);
			if (element.isIndependent()) {
				element.update();
			}
		}
		for (AlgoElement algorithm : new ArrayList<>(isolatedConstruction.getAlgoList())) {
			if (!(algorithm instanceof SetRandomValue) || !((SetRandomValue) algorithm)
					.setRandomValue(algorithm.getOutput(0))) {
				algorithm.update();
			}
		}
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
