/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.algos;

import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.Algos;
import org.geogebra.common.kernel.algos.GetCommand;
import org.geogebra.common.kernel.arithmetic.ExpressionNodeConstants.StringType;
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
		this(construction, label, richInput, Commands.Length);
	}

	/** Creates a public numeric result with the requested reconstructible command. */
	public AlgoLocusMetricScalarAdapter(Construction construction, String label,
			GeoLocusMetricResult richInput, Commands commandName) {
		this(construction, richInput, label, true, commandName);
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
		setUpdateAfterAlgo(richInput.getParentAlgorithm());
		setDependencies();
		if (addToConstructionList) {
			construction.getConstructionDefaults().setLabelVisibilityAtCreation(scalarOutput);
		}
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

	/** Resolves the rich authority of a metric result selected in a frontend. */
	public static GeoLocusMetricResult richResultOf(GeoElement result) {
		if (result instanceof GeoLocusMetricResult rich) {
			return rich;
		}
		return result != null
				&& result.getParentAlgorithm() instanceof AlgoLocusMetricScalarAdapter adapter
				? adapter.getRichInput() : null;
	}

	public GeoNumeric getScalarOutput() {
		return scalarOutput;
	}

	/**
	 * Presents the public construction without replacing its rich dependency.
	 * XML, symbolic exports and internal adapters retain the actual input.
	 */
	@Override
	public String getDefinition(StringTemplate template) {
		AlgoElement richParent = richInput.getParentAlgorithm();
		if ((commandName != Commands.Length && commandName != Commands.LocusLength)
				|| !isPublicPresentation(template)
				|| !(richParent instanceof AlgoLocusMetricV2
						|| richParent instanceof AlgoLocusBetweenMetricV2)) {
			return super.getDefinition(template);
		}
		StringBuilder description = new StringBuilder(
				template.isPrintLocalizedCommandNames()
						? getLoc().getCommandHead(commandName.getCommand())
						: commandName.getCommand());
		description.append(template.leftCommandBracket(getLoc()));
		for (int index = 0; index < richParent.getInputLength(); index++) {
			if (index > 0) {
				description.append(',');
				template.appendOptionalSpace(description);
			}
			description.append(richParent.getInput(index).getLabel(template));
		}
		description.append(template.rightCommandBracket(getLoc()));
		return description.toString();
	}

	private static boolean isPublicPresentation(StringTemplate template) {
		return template.hasType(StringType.GEOGEBRA) || template.isLatex()
				|| template.hasType(StringType.SCREEN_READER_ASCII)
				|| template.hasType(StringType.SCREEN_READER_UNICODE);
	}

	@Override
	public GetCommand getClassName() {
		return commandName;
	}

}
