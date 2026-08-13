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
import org.geocedg.common.kernel.locus.metric.LocusMetricInstrumentation2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricOwnerLease2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricSharedOwner2D;
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
	private static final String UNSUPPORTED_COPY =
			"GeoLocusV2 cannot be copied before an approved persistence/lifecycle contract";
	private final String locusIdentity;
	private final LocusInstrumentation2D instrumentation;
	private final LocusMetricInstrumentation2D metricInstrumentation;
	private LocusDefinition2D definition;
	private boolean explicitlyUndefined;
	private LocusMetricSharedOwner2D metricOwner;

	/** Creates an empty internal V2 object with a caller-owned stable identity. */
	public GeoLocusV2(Construction construction, String locusIdentity) {
		super(construction);
		if (locusIdentity == null || locusIdentity.trim().isEmpty()) {
			throw new IllegalArgumentException("Locus V2 identity is required");
		}
		this.locusIdentity = locusIdentity;
		this.instrumentation = new LocusInstrumentation2D();
		this.metricInstrumentation = new LocusMetricInstrumentation2D();
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

	/** @return functional metric counters, separate from render instrumentation */
	public LocusMetricInstrumentation2D getMetricInstrumentation() {
		return metricInstrumentation;
	}

	/**
	 * Acquires the narrow per-source metric-state owner seam. No dependency edge
	 * or query/result authority is stored here.
	 *
	 * @return one active metric-consumer lease
	 */
	public LocusMetricOwnerLease2D acquireMetricOwnerLease() {
		if (metricOwner == null || metricOwner.isReleased()) {
			metricOwner = new LocusMetricSharedOwner2D(locusIdentity,
					metricInstrumentation);
		}
		return metricOwner.acquireLease();
	}

	/** @return active owner for diagnostics only, or {@code null} */
	public LocusMetricSharedOwner2D getMetricSharedOwnerForDiagnostics() {
		return metricOwner;
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
		if (metricOwner != null && !metricOwner.isReleased()) {
			metricOwner.invalidateObsoleteRevision(
					semanticDefinition.getSemanticRevision());
		}
		definition = semanticDefinition;
		explicitlyUndefined = false;
	}

	/**
	 * Restores defined state after a normal-DAG recompute republishes equivalent
	 * semantic content. No semantic revision is created by this state recovery.
	 */
	public void restoreDefinedStateAfterEquivalentRecompute() {
		if (definition == null) {
			throw new IllegalStateException("Cannot restore an unpublished Locus V2");
		}
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

	/** @return safe developer-only type text without an upstream translation key */
	@Override
	public String translatedTypeString() {
		return "Locus V2 (experimental)";
	}

	/** @return safe developer-only Algebra View type text */
	@Override
	public String translatedTypeStringForAlgebraView() {
		return translatedTypeString();
	}

	@Override
	public GeoElement copy() {
		throw new UnsupportedOperationException(UNSUPPORTED_COPY);
	}

	@Override
	public GeoElement copyInternal(Construction targetConstruction) {
		throw new UnsupportedOperationException(UNSUPPORTED_COPY);
	}

	@Override
	public void set(GeoElementND geo) {
		throw new UnsupportedOperationException(
				"GeoLocusV2 assignment is unavailable before an approved lifecycle contract");
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
		if (metricOwner != null && !metricOwner.isReleased()) {
			metricOwner.clear();
		}
	}

	@Override
	public void doRemove() {
		if (metricOwner != null) {
			metricOwner.releaseSource();
			metricOwner = null;
		}
		super.doRemove();
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
