/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.algos;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusExploratoryEvidence2D;
import org.geocedg.common.kernel.locus.V2RedefineContractSource;
import org.geogebra.common.kernel.Construction;
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
	}

	/**
	 * PRE-G9B-R6-plus-A-1: the V2 output never reaches the construction-defaults
	 * path, so it asks the same application seam for its creation layer. Outside
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
