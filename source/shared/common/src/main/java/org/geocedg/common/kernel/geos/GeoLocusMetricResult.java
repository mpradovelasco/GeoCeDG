/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.geos;

import java.util.Objects;

import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.MetricValueKind;
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
public final class GeoLocusMetricResult extends GeoElement {
	private final String sourceLocusIdentity;
	private long sourceSemanticRevision;
	private LocusMetricResult2D metricResult;
	private boolean explicitlyUndefined;

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
		return "Locus metric result (experimental)";
	}

	@Override
	public String translatedTypeStringForAlgebraView() {
		return translatedTypeString();
	}

	@Override
	public GeoElement copy() {
		return new GeoLocusMetricResult(cons, sourceLocusIdentity);
	}

	@Override
	public GeoElement copyInternal(Construction targetConstruction) {
		return new GeoLocusMetricResult(targetConstruction,
				sourceLocusIdentity);
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

	/** G7B is intentionally nonpersistent: no XML element is emitted. */
	@Override
	public void getXML(boolean getListenersToo, XMLStringBuilder builder) {
		// Persistence requires a separate future author-approved contract.
	}
}
