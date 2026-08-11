/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.geos;

import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInstrumentation2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.arithmetic.ValueType;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.plugin.GeoClass;

/**
 * Parallel experimental semantic 2D locus. It deliberately implements neither
 * Path nor any legacy GeoLocus interface in G6B.
 */
public final class GeoLocusV2 extends GeoElement {
	private final String locusIdentity;
	private final LocusInstrumentation2D instrumentation;
	private LocusDefinition2D definition;
	private boolean explicitlyUndefined;

	/** Creates an empty internal V2 object with a caller-owned stable identity. */
	public GeoLocusV2(Construction construction, String locusIdentity) {
		super(construction);
		if (locusIdentity == null || locusIdentity.trim().isEmpty()) {
			throw new IllegalArgumentException("Locus V2 identity is required");
		}
		this.locusIdentity = locusIdentity;
		this.instrumentation = new LocusInstrumentation2D();
		setEuclidianVisible(true);
		setAuxiliaryObject(true);
	}

	public String getLocusIdentity() {
		return locusIdentity;
	}

	public LocusDefinition2D getSemanticDefinition() {
		return definition;
	}

	public long getSemanticRevision() {
		return definition == null ? 0 : definition.getSemanticRevision();
	}

	public LocusInstrumentation2D getInstrumentation() {
		return instrumentation;
	}

	/** Publishes one immutable snapshot from normal AlgoElement recompute only. */
	public void publishSemanticDefinition(LocusDefinition2D semanticDefinition) {
		if (!locusIdentity.equals(semanticDefinition.getLocusIdentity())) {
			throw new IllegalArgumentException("Definition identity does not match geo");
		}
		if (definition != null && semanticDefinition.getSemanticRevision()
				<= definition.getSemanticRevision()) {
			throw new IllegalArgumentException("Semantic revisions must increase");
		}
		definition = semanticDefinition;
		explicitlyUndefined = false;
	}

	/**
	 * Evaluates semantic data; no render/sample structure is consulted.
	 *
	 * @return typed semantic point result or invalid status
	 */
	public LocusEvaluation2D evaluate(String branchKey, double semanticParameter,
			LocusEvaluationSession2D session) {
		if (definition == null) {
			throw new IllegalStateException("Locus V2 has no semantic definition");
		}
		return definition.evaluate(branchKey, semanticParameter, session);
	}

	/**
	 * Separate render entry point for instrumentation and derived tessellation.
	 *
	 * @return semantic evaluation consumed only by derived rendering
	 */
	public LocusEvaluation2D evaluateForRender(String branchKey,
			double semanticParameter, LocusEvaluationSession2D session) {
		instrumentation.recordRenderEvaluation();
		return evaluate(branchKey, semanticParameter, session);
	}

	@Override
	public GeoClass getGeoClassType() {
		return GeoClass.LOCUS_V2;
	}

	@Override
	public ValueType getValueType() {
		return ValueType.VOID;
	}

	@Override
	public GeoElement copy() {
		GeoLocusV2 copy = new GeoLocusV2(cons, locusIdentity + "/internal-copy");
		if (definition != null) {
			copy.definition = definition.copyFor(copy.locusIdentity,
					copy.instrumentation);
		}
		copy.explicitlyUndefined = explicitlyUndefined;
		return copy;
	}

	@Override
	public void set(GeoElementND geo) {
		if (!(geo instanceof GeoLocusV2)) {
			setUndefined();
			return;
		}
		GeoLocusV2 source = (GeoLocusV2) geo;
		definition = source.definition == null ? null
				: source.definition.copyFor(locusIdentity, instrumentation);
		explicitlyUndefined = source.explicitlyUndefined;
	}

	@Override
	public boolean isDefined() {
		if (explicitlyUndefined || definition == null) {
			return false;
		}
		DefinitionStatus status = definition.getDefinitionStatus();
		return status == DefinitionStatus.VALID
				|| status == DefinitionStatus.EMPTY_DOMAIN;
	}

	@Override
	public void setUndefined() {
		explicitlyUndefined = true;
	}

	@Override
	public String toValueString(StringTemplate template) {
		return definition == null ? "LocusV2[experimental, unpublished]"
				: "LocusV2[experimental, revision="
						+ definition.getSemanticRevision() + "]";
	}

	@Override
	protected boolean showInEuclidianView() {
		return isDefined() && definition.getDefinitionStatus() == DefinitionStatus.VALID;
	}

	@Override
	public boolean isAlgebraViewEditable() {
		return false;
	}

	/** G6B is intentionally nonpersistent: no XML element is emitted. */
	@Override
	public void getXML(boolean getListenersToo, XMLStringBuilder builder) {
		// Persistence and migration require a separate author-approved contract.
	}
}
