/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.geos;

import org.geocedg.common.kernel.locus.intersection.IntersectionSourceBinding2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.arithmetic.ValueType;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.plugin.GeoClass;

/** Internal nonnumeric rich intersection authority in the normal kernel DAG. */
public final class GeoLocusIntersectionResult extends GeoElement {
	private final String sourcePairIdentity;
	private IntersectionSourceBinding2D currentSourceBinding;
	private LocusIntersectionResult2D intersectionResult;
	private boolean explicitlyUndefined;

	/** Creates an unpublished rich intersection Geo for one constructive pair. */
	public GeoLocusIntersectionResult(Construction construction,
			String sourcePairIdentity) {
		super(construction);
		if (sourcePairIdentity == null || sourcePairIdentity.trim().isEmpty()) {
			throw new IllegalArgumentException("Source-pair identity is required");
		}
		this.sourcePairIdentity = sourcePairIdentity;
		setEuclidianVisible(false);
		setAuxiliaryObject(true);
	}

	public String getSourcePairIdentity() {
		return sourcePairIdentity;
	}

	public IntersectionSourceBinding2D getCurrentSourceBinding() {
		return currentSourceBinding;
	}

	public LocusIntersectionResult2D getIntersectionResult() {
		return intersectionResult;
	}

	/** Makes every previous revision-bound payload unavailable before work. */
	public void beginIntersectionRevision(IntersectionSourceBinding2D binding) {
		if (!sourcePairIdentity.equals(binding.getSourcePairIdentity())) {
			throw new IllegalArgumentException("Source-pair binding mismatch");
		}
		currentSourceBinding = binding;
		intersectionResult = null;
		explicitlyUndefined = false;
	}

	/** Atomically publishes one coherent immutable result snapshot. */
	public void publishIntersectionResult(IntersectionSourceBinding2D binding,
			LocusIntersectionResult2D result) {
		if (binding != currentSourceBinding
				|| result.getSourceBinding() != binding) {
			throw new IllegalArgumentException(
					"Intersection publication does not match current revision");
		}
		intersectionResult = java.util.Objects.requireNonNull(result);
		explicitlyUndefined = false;
	}

	/** @return whether the current rich result admits this exact token */
	public boolean isPointAdmissible(String rootToken) {
		return isDefined() && intersectionResult
				.findPointAdmissibleSolution(rootToken).isPresent();
	}

	@Override
	public GeoClass getGeoClassType() {
		return GeoClass.LOCUS_INTERSECTION_RESULT;
	}

	@Override
	public ValueType getValueType() {
		return ValueType.VOID;
	}

	@Override
	public String translatedTypeString() {
		return "Locus intersection result (experimental)";
	}

	@Override
	public String translatedTypeStringForAlgebraView() {
		return translatedTypeString();
	}

	@Override
	public GeoElement copy() {
		return new GeoLocusIntersectionResult(cons, sourcePairIdentity);
	}

	@Override
	public GeoElement copyInternal(Construction targetConstruction) {
		return new GeoLocusIntersectionResult(targetConstruction,
				sourcePairIdentity);
	}

	/** Assignment never imports a revision-bound result or continuation state. */
	@Override
	public void set(GeoElementND geo) {
		if (!(geo instanceof GeoLocusIntersectionResult)) {
			throw new IllegalArgumentException(
					"Only another rich intersection result may be assigned");
		}
		currentSourceBinding = null;
		intersectionResult = null;
		explicitlyUndefined = true;
	}

	@Override
	public boolean isDefined() {
		return !explicitlyUndefined && currentSourceBinding != null
				&& intersectionResult != null;
	}

	@Override
	public void setUndefined() {
		intersectionResult = null;
		explicitlyUndefined = true;
	}

	@Override
	public void doRemove() {
		setUndefined();
		currentSourceBinding = null;
		super.doRemove();
	}

	@Override
	public String toValueString(StringTemplate template) {
		if (!isDefined()) {
			return "LocusIntersectionResult[unpublished]";
		}
		return "LocusIntersectionResult[revision="
				+ currentSourceBinding.getLocusSemanticRevision() + ", kind="
				+ intersectionResult.getGeometryKind() + ", completeness="
				+ intersectionResult.getCompletenessEvidence().getCompleteness()
				+ "]";
	}

	@Override
	protected boolean showInEuclidianView() {
		return false;
	}

	@Override
	public boolean isAlgebraViewEditable() {
		return false;
	}

	/** G8B is internal and nonpersistent; no XML element is emitted. */
	@Override
	public void getXML(boolean getListenersToo, XMLStringBuilder builder) {
		// Persistence requires a separate future author-approved contract.
	}
}
