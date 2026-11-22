/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.algos;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusExploratoryEvidence2D;
import org.geocedg.common.kernel.locus.V2RedefineContractSource;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidence2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidenceOwner2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedSample2D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.ConstructionDefaults;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.Algos;
import org.geogebra.common.kernel.algos.GetCommand;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.App;

/** Base for internal V2 algorithms publishing immutable semantic snapshots. */
public abstract class AlgoLocusV2 extends AlgoElement
		implements V2RedefineContractSource {
	private final GeoLocusV2 locus;
	private final GeoElement[] configuredInputs;

	/** Wires only the supplied normal kernel DAG inputs and one V2 output. */
	protected AlgoLocusV2(Construction construction, String locusIdentity,
			GeoElement[] inputs) {
		super(construction, false);
		locus = createOutput(new GeoLocusV2(construction, locusIdentity));
		configuredInputs = inputs.clone();
		setInputOutput();
		setDependencies();
	}

	/**
	 * Wires a public reconstructible parent whose durable output identity is
	 * attached by the construction identity section after command creation.
	 */
	protected AlgoLocusV2(Construction construction, GeoElement[] inputs) {
		super(construction);
		locus = createOutput(new GeoLocusV2(construction));
		configuredInputs = inputs.clone();
		setInputOutput();
		setDependencies();
		initializeAppearance(construction);
	}

	/**
	 * Wires public command arguments for XML together with the complete normal-DAG
	 * update slice used by a reconstructible evaluator. The second array affects
	 * propagation only; it never changes the serialized command spelling.
	 */
	protected AlgoLocusV2(Construction construction, GeoElement[] commandInputs,
			GeoElement[] evaluatorInputs) {
		super(construction);
		locus = createOutput(new GeoLocusV2(construction));
		configuredInputs = commandInputs.clone();
		setInputOutput();
		setEfficientDependencies(configuredInputs, evaluatorInputs.clone());
		initializeAppearance(construction);
	}

	private void initializeAppearance(Construction construction) {
		ConstructionDefaults defaults = construction.getConstructionDefaults();
		if (defaults != null && defaults.getDefaultGeo(ConstructionDefaults.DEFAULT_LOCUS_V2)
				!= null && !construction.isFileLoading() && !construction.getKernel().getLoadingMode()) {
			locus.setConstructionDefaults(false, false);
		}
	}

	/**
	 * PRE-G9B-R6-plus-A-1: the V2 output asks the application seam for its
	 * creation layer. The curve prototype owns appearance only. Outside
	 * GeoCeDG interactive creation the seam returns the current layer unchanged;
	 * a stored layer is restored afterwards from the document.
	 */
	private static GeoLocusV2 createOutput(GeoLocusV2 output) {
		App app = output.getKernel().getApplication();
		if (app != null) {
			output.setLayer(app.getLayerForNewObject(output.getConstruction(),
					output.getLayer()));
		}
		return output;
	}

	@Override
	protected final void setInputOutput() {
		input = configuredInputs;
		setOnlyOutput(locus);
	}

	/** Must be called by the concrete constructor after its fields are ready. */
	protected final void publishInitialSnapshot() {
		compute();
	}

	@Override
	public final void compute() {
		locus.getInstrumentation().recordDependencyUpdate();
		if (!isSemanticPublicationReady()) {
			// PRE-G9B-CU2 (locus-v2-semantics.md, publication; finding F-CU2-3): a recompute
			// that does not publish discards the previous definition.
			locus.discardSemanticDefinition();
			return;
		}
		long candidateRevision = locus.getNextSemanticRevision();
		// PRE-G9B-CU2 gates GC-1 to GC-3 and GC-6: a producer that computes an existence
		// certificate counts it inside its own scope, once per revision
		// (LocusCertificationScope2D); building a candidate from it is not a computation.
		LocusDefinition2D candidate = createCandidate(candidateRevision);
		LocusDefinition2D current = locus.getSemanticDefinition();
		if (current == null || !current.hasSameSemanticContent(candidate)) {
			locus.publishSemanticDefinition(candidate.withRevision(candidateRevision));
			locus.getInstrumentation().recordRevisionPublication();
		} else {
			locus.restoreDefinedStateAfterEquivalentRecompute();
			locus.getInstrumentation().recordEquivalentRecompute();
		}
	}

	protected abstract LocusDefinition2D createCandidate(long candidateRevision);

	/**
	 * Exploratory evidence of the unresolved bands of one branch of the published
	 * definition (PRE-G9B-CU2, decision E-1); producers without unresolved bands derive
	 * none.
	 *
	 * @param branchKey branch of the published definition
	 * @return evidence, or {@code null}
	 */
	public LocusExploratoryEvidence2D getExploratoryEvidence(String branchKey) {
		return null;
	}

	/**
	 * Conditioned evidence of the part of one branch of the published definition that no
	 * certificate covers (ADR 0042; contract {@code post-r1-conditioned-use.md}, sections 3
	 * and 7). It is revision evidence of nature {@code NUMERICAL_UNDER_HYPOTHESES}, owned by
	 * the producer: derived, never persisted, never identity and never part of the
	 * definition. Producers that publish none return {@code null}.
	 *
	 * @param branchKey branch of the published definition
	 * @return published content of the current revision, or {@code null}
	 */
	public LocusConditionedEvidence2D getConditionedEvidence(String branchKey) {
		return null;
	}

	/**
	 * @return the policy with which this producer traverses its conditioned evidence; a
	 *         similarity image publishes under the policy of its source
	 */
	public org.geocedg.common.kernel.locus.conditioned.LocusConditionedTraversalPolicy2D
			getConditionedTraversalPolicy() {
		return org.geocedg.common.kernel.locus.conditioned.LocusConditionedTraversalPolicy2D
				.v1();
	}

	/**
	 * @param branchKey branch of the published definition
	 * @return the conditioned evidence that this producer has already published for the
	 *         current revision, or {@code null}; it is never computed by this call
	 */
	public LocusConditionedEvidence2D getPublishedConditionedEvidence(String branchKey) {
		return null;
	}

	/**
	 * One evaluation of the producer's own evaluator with the testimony of its selecting
	 * dependencies, for the point validity of a parameter address (contract section 6.3).
	 *
	 * @param branchKey branch of the published definition
	 * @param canonicalParameter canonical driver parameter
	 * @return evaluation with testimony, or {@code null} when this producer gives none
	 */
	public LocusConditionedSample2D sampleConditioned(String branchKey,
			double canonicalParameter) {
		return null;
	}

	/**
	 * @param branchKey branch of the published definition
	 * @return counter of the publications of the conditioned evidence of the current
	 *         revision; it orders them and is never an identity
	 */
	public long getConditionedPublicationCounter(String branchKey) {
		return 0;
	}

	/**
	 * Receives the report of a parameter at which an observer saw an evaluation that the
	 * kind of its cell excludes. The producer evaluates the parameter itself; the report
	 * changes nothing unless the contradiction is confirmed.
	 *
	 * @param branchKey branch of the published definition
	 * @param parameter reported canonical parameter
	 * @return what the producer decided, or {@code null} when it publishes no evidence
	 */
	public LocusConditionedEvidenceOwner2D.ReportOutcome reportConditionedContradiction(
			String branchKey, double parameter) {
		return null;
	}

	/**
	 * @return whether all durable/reconstructible inputs needed by this parent are
	 *         currently attached; internal G6-G8 parents are ready immediately
	 */
	protected boolean isSemanticPublicationReady() {
		return true;
	}

	public GeoLocusV2 getLocus() {
		return locus;
	}

	@Override
	public GetCommand getClassName() {
		return Algos.Expression;
	}
}
