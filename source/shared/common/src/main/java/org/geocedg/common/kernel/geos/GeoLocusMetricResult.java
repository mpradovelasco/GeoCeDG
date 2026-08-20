/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.geos;

import java.util.Objects;

import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.MetricValueKind;
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
 * Rich metric GeoElement published in the normal kernel DAG.
 *
 * <p>It deliberately implements no numeric interface.</p>
 */
public final class GeoLocusMetricResult extends GeoElement
		implements PersistentGeoIdentityListener {
	private String sourceLocusIdentity;
	private long sourceSemanticRevision;
	private LocusMetricResult2D metricResult;
	private boolean explicitlyUndefined;
	private boolean publicPersistence;

	/** Creates an unpublished rich metric result for one source identity. */
	public GeoLocusMetricResult(Construction construction,
			String sourceLocusIdentity) {
		super(construction);
		if (sourceLocusIdentity == null
				|| sourceLocusIdentity.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"Metric result source identity is required");
		}
		this.sourceLocusIdentity = sourceLocusIdentity;
		setEuclidianVisible(false);
		setAuxiliaryObject(true);
	}

	public String getSourceLocusIdentity() {
		return sourceLocusIdentity;
	}

	/** Refreshes the source identity after an authorized copy/load remap. */
	public void refreshSourceLocusIdentity(String identity) {
		if (identity == null || identity.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"Metric result source identity is required");
		}
		sourceLocusIdentity = identity;
	}

	/** Marks this result as reconstructible through its public parent command. */
	public void enablePublicPersistence() {
		publicPersistence = true;
	}

	public long getSourceSemanticRevision() {
		return sourceSemanticRevision;
	}

	public LocusMetricResult2D getMetricResult() {
		return metricResult;
	}

	public boolean isScalarAdmissible() {
		return isDefined() && metricResult.isScalarAdmissible();
	}

	/**
	 * P1 invalidation: old payload becomes non-current before computation.
	 */
	public void beginMetricRevision(long revision) {
		if (revision < 1) {
			throw new IllegalArgumentException(
					"Metric source revision must be positive");
		}
		sourceSemanticRevision = revision;
		metricResult = null;
		explicitlyUndefined = false;
	}

	/** Atomically publishes one coherent immutable rich result. */
	public void publishMetricResult(long revision,
			LocusMetricResult2D result) {
		if (revision != sourceSemanticRevision) {
			throw new IllegalArgumentException(
					"Metric publication revision does not match current P1 revision");
		}
		metricResult = Objects.requireNonNull(result);
		explicitlyUndefined = false;
	}

	/** Publishes a handled rich failure using the same atomic contract. */
	public void publishMetricFailure(long revision,
			LocusMetricResult2D failure) {
		if (failure.getMetricValue().getKind() != MetricValueKind.ABSENT) {
			throw new IllegalArgumentException(
					"A P1 failure must carry an absent metric value");
		}
		publishMetricResult(revision, failure);
	}

	@Override
	public GeoClass getGeoClassType() {
		return GeoClass.LOCUS_METRIC_RESULT;
	}

	@Override
	public ValueType getValueType() {
		return ValueType.VOID;
	}

	@Override
	public String translatedTypeString() {
		return getLoc().getMenu("LocusMetricResult");
	}

	@Override
	public String translatedTypeStringForAlgebraView() {
		return translatedTypeString();
	}

	@Override
	public GeoElement copy() {
		GeoLocusMetricResult copy = new GeoLocusMetricResult(cons,
				sourceLocusIdentity);
		copy.publicPersistence = publicPersistence;
		return copy;
	}

	@Override
	public GeoElement copyInternal(Construction targetConstruction) {
		GeoLocusMetricResult copy = new GeoLocusMetricResult(targetConstruction,
				sourceLocusIdentity);
		copy.publicPersistence = publicPersistence;
		return copy;
	}

	/**
	 * Assignment never copies a revision-bound payload, binding or metric state.
	 */
	@Override
	public void set(GeoElementND geo) {
		if (!(geo instanceof GeoLocusMetricResult)) {
			throw new IllegalArgumentException(
					"Only another rich locus metric result may be assigned");
		}
		sourceSemanticRevision = 0;
		metricResult = null;
		explicitlyUndefined = true;
	}

	@Override
	public boolean isDefined() {
		return !explicitlyUndefined && sourceSemanticRevision > 0
				&& metricResult != null;
	}

	@Override
	public void setUndefined() {
		metricResult = null;
		explicitlyUndefined = true;
	}

	@Override
	public void onPersistentGeoIdentityAttached(PersistentGeoId attachedId) {
		try {
			if (!attachedId.equals(cons.getSpatialIdentityRegistry()
					.getPersistentGeoId(this))) {
				setUndefined();
				return;
			}
			if (getParentAlgorithm() != null) {
				getParentAlgorithm().update();
				updateCascade();
			}
		} catch (RuntimeException exception) {
			setUndefined();
		}
	}

	@Override
	public String toValueString(StringTemplate template) {
		if (!isDefined()) {
			return "LocusMetricResult[unpublished]";
		}
		return "LocusMetricResult[revision=" + sourceSemanticRevision
				+ ", value=" + metricResult.getMetricValue() + ", status="
				+ metricResult.getComputationStatus() + ", coverage="
				+ metricResult.getCoverage() + "]";
	}

	@Override
	protected boolean showInEuclidianView() {
		return false;
	}

	@Override
	public boolean isAlgebraViewEditable() {
		return false;
	}

	/** Internal G7B results remain transient; public G9U0 results persist. */
	@Override
	public void getXML(boolean getListenersToo, XMLStringBuilder builder) {
		if (publicPersistence) {
			super.getXML(getListenersToo, builder);
		}
	}
}
