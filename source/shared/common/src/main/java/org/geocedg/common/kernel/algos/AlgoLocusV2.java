/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.algos;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.Algos;
import org.geogebra.common.kernel.algos.GetCommand;
import org.geogebra.common.kernel.geos.GeoElement;

/** Base for internal V2 algorithms publishing immutable semantic snapshots. */
public abstract class AlgoLocusV2 extends AlgoElement {
	private final GeoLocusV2 locus;
	private final GeoElement[] configuredInputs;

	/** Wires only the supplied normal kernel DAG inputs and one V2 output. */
	protected AlgoLocusV2(Construction construction, String locusIdentity,
			GeoElement[] inputs) {
		super(construction, false);
		locus = new GeoLocusV2(construction, locusIdentity);
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
		locus = new GeoLocusV2(construction);
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
		locus = new GeoLocusV2(construction);
		configuredInputs = commandInputs.clone();
		setInputOutput();
		setEfficientDependencies(configuredInputs, evaluatorInputs.clone());
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
			locus.setUndefined();
			return;
		}
		long candidateRevision = Math.max(1, locus.getSemanticRevision() + 1);
		LocusDefinition2D candidate = createCandidate(candidateRevision);
		LocusDefinition2D current = locus.getSemanticDefinition();
		if (current == null || !current.hasSameSemanticContent(candidate)) {
			locus.publishSemanticDefinition(candidate.withRevision(candidateRevision));
			locus.getInstrumentation().recordRevisionPublication();
		} else {
			locus.restoreDefinedStateAfterEquivalentRecompute();
		}
	}

	protected abstract LocusDefinition2D createCandidate(long candidateRevision);

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
