/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.algos;

import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.Algos;
import org.geogebra.common.kernel.algos.GetCommand;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;

/**
 * Explicit opt-in scalar adapter; the rich Geo remains non-numeric authority.
 */
public final class AlgoLocusMetricScalarAdapter extends AlgoElement {
	private final GeoLocusMetricResult richInput;
	private final GetCommand commandName;
	private final GeoNumeric scalarOutput;

	/** Creates and wires one derived scalar adapter. */
	public AlgoLocusMetricScalarAdapter(Construction construction,
			GeoLocusMetricResult richInput) {
		this(construction, richInput, null, false, Algos.Expression);
	}

	/** Creates the reconstructible standard scalar child of one rich query. */
	public AlgoLocusMetricScalarAdapter(Construction construction, String label,
			GeoLocusMetricResult richInput) {
		this(construction, richInput, label, true, Commands.Length);
	}

	private AlgoLocusMetricScalarAdapter(Construction construction,
			GeoLocusMetricResult richInput, String label,
			boolean addToConstructionList, GetCommand commandName) {
		super(construction, addToConstructionList);
		this.richInput = java.util.Objects.requireNonNull(richInput);
		this.commandName = commandName;
		this.scalarOutput = new GeoNumeric(construction, false);
		setProtectedInput(true);
		setInputOutput();
		setDependencies();
		compute();
		if (label != null) {
			scalarOutput.setLabel(label);
		}
	}

	@Override
	protected void setInputOutput() {
		input = new GeoElement[] {richInput};
		setOnlyOutput(scalarOutput);
	}

	@Override
	public void compute() {
		if (!richInput.isScalarAdmissible()) {
			scalarOutput.setUndefined();
			return;
		}
		scalarOutput.setValue(richInput.getMetricResult().getMetricValue()
				.getFiniteValue().orElseThrow());
	}

	public GeoLocusMetricResult getRichInput() {
		return richInput;
	}

	public GeoNumeric getScalarOutput() {
		return scalarOutput;
	}

	@Override
	public GetCommand getClassName() {
		return commandName;
	}

}
