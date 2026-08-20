/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.geos;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInstrumentation2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.metric.LocusMetricInstrumentation2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricOwnerLease2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricSharedOwner2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoIdentityListener;
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
public final class GeoLocusV2 extends GeoElement
		implements PersistentGeoIdentityListener {
	private final String bootstrapIdentity;
	private final LocusInstrumentation2D instrumentation;
	private final LocusMetricInstrumentation2D metricInstrumentation;
	private LocusDefinition2D definition;
	private boolean explicitlyUndefined;
	private LocusMetricSharedOwner2D metricOwner;
	private String metricOwnerIdentity;

	/** Creates an empty internal V2 object with a caller-owned stable identity. */
	public GeoLocusV2(Construction construction, String locusIdentity) {
		super(construction);
		if (locusIdentity == null || locusIdentity.trim().isEmpty()) {
			throw new IllegalArgumentException("Locus V2 identity is required");
		}
		this.bootstrapIdentity = locusIdentity;
		this.instrumentation = new LocusInstrumentation2D();
		this.metricInstrumentation = new LocusMetricInstrumentation2D();
		setEuclidianVisible(true);
		setAuxiliaryObject(true);
	}

	/** Creates an unassociated copy shell; no semantic snapshot is copied. */
	public GeoLocusV2(Construction construction) {
		super(construction);
		this.bootstrapIdentity = null;
		this.instrumentation = new LocusInstrumentation2D();
		this.metricInstrumentation = new LocusMetricInstrumentation2D();
		setEuclidianVisible(true);
		setAuxiliaryObject(true);
	}

	/** @return current lifecycle identity in its external form */
	public String getLocusIdentity() {
		PersistentGeoId persistent = getPersistentLocusId();
		if (persistent != null) {
			return persistent.toExternalForm();
		}
		if (bootstrapIdentity == null) {
			throw new IllegalStateException(
					"Locus V2 has no attached durable identity");
		}
		return bootstrapIdentity;
	}

	/** @return current lifecycle-owned locus ID, never a cached pre-copy value */
	public PersistentGeoId getPersistentLocusId() {
		return cons.getSpatialIdentityRegistry().getPersistentGeoId(this);
	}

	@Override
	public void onPersistentGeoIdentityAttached(PersistentGeoId attachedId) {
		try {
			if (!attachedId.equals(getPersistentLocusId())) {
				setUndefined();
				return;
			}
			if (getParentAlgorithm() != null) {
				getParentAlgorithm().update();
			}
		} catch (RuntimeException exception) {
			setUndefined();
		}
	}

	/** Refreshes only the identity carried by an immutable semantic snapshot. */
	public void refreshPersistentIdentity() {
		if (definition == null) {
			return;
		}
		String current = getLocusIdentity();
		if (!current.equals(definition.getLocusIdentity())) {
			definition = definition.withLocusIdentity(current);
			if (metricOwner != null) {
				metricOwner.releaseSource();
				metricOwner = null;
				metricOwnerIdentity = null;
			}
		}
	}

	/** @return current semantic definition, or {@code null} while undefined */
	public LocusDefinition2D getSemanticDefinition() {
		refreshPersistentIdentity();
		return definition;
	}

	public long getSemanticRevision() {
		return getSemanticDefinition() == null ? 0 : definition.getSemanticRevision();
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
		String currentIdentity = getLocusIdentity();
		if (metricOwner == null || metricOwner.isReleased()
				|| !currentIdentity.equals(metricOwnerIdentity)) {
			if (metricOwner != null && !metricOwner.isReleased()) {
				metricOwner.releaseSource();
			}
			metricOwner = new LocusMetricSharedOwner2D(currentIdentity,
					metricInstrumentation);
			metricOwnerIdentity = currentIdentity;
		}
		return metricOwner.acquireLease();
	}

	/** @return active owner for diagnostics only, or {@code null} */
	public LocusMetricSharedOwner2D getMetricSharedOwnerForDiagnostics() {
		return metricOwner;
	}

	/** Publishes one immutable snapshot from normal AlgoElement recompute only. */
	public void publishSemanticDefinition(LocusDefinition2D semanticDefinition) {
		if (!getLocusIdentity().equals(semanticDefinition.getLocusIdentity())) {
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

	/** @return localized experimental public type text */
	@Override
	public String translatedTypeString() {
		return getLoc().getMenu("LocusV2");
	}

	/** @return localized Algebra View type text */
	@Override
	public String translatedTypeStringForAlgebraView() {
		return translatedTypeString();
	}

	@Override
	public GeoElement copy() {
		requirePublicPersistence("copy");
		return copyInternal(cons);
	}

	@Override
	public GeoElement copyInternal(Construction targetConstruction) {
		requirePublicPersistence("copyInternal");
		GeoLocusV2 copy = new GeoLocusV2(targetConstruction);
		copy.setVisualStyle(this);
		return copy;
	}

	@Override
	public void set(GeoElementND geo) {
		if (!isPublicPersistentLocus()) {
			throw new UnsupportedOperationException(
					"Internal Locus V2 assignment is not a persistence contract");
		}
		if (!(geo instanceof GeoLocusV2)) {
			setUndefined();
			return;
		}
		definition = null;
		setUndefined();
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
		setUndefined();
		if (metricOwner != null) {
			metricOwner.releaseSource();
			metricOwner = null;
			metricOwnerIdentity = null;
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

	/** Persists only reconstructible parent inputs, styles and the durable ID. */
	@Override
	public void getXML(boolean getListenersToo, XMLStringBuilder builder) {
		if (!isPublicPersistentLocus()) {
			return;
		}
		super.getXML(getListenersToo, builder);
	}

	private boolean isPublicPersistentLocus() {
		return getPersistentLocusId() != null
				|| getParentAlgorithm() instanceof AlgoDependentPointLocusV2;
	}

	private void requirePublicPersistence(String operation) {
		if (!isPublicPersistentLocus()) {
			throw new UnsupportedOperationException(
					"Internal Locus V2 " + operation
							+ " is not a persistence contract");
		}
	}
}
