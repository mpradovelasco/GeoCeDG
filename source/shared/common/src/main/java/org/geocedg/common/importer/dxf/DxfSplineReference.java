/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.importer.dxf;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckForNull;

import org.geocedg.common.math.bspline.BSplineCanonicalEncoding;
import org.geocedg.common.math.bspline.BSplineData;
import org.geocedg.common.math.bspline.BSplineInteriorKnot;
import org.geocedg.common.math.bspline.BSplineWeightMode;

/**
 * Immutable record of one admitted {@code SPLINE} entity (section 5). It never changes after
 * admission; a new transformation or a re-read produces a new record. It is external data,
 * not a construction object: no identity, no dependencies, no metric, incidence, tangent or
 * intersection authority, and its render certifies nothing.
 */
public final class DxfSplineReference {

	/** The contract identifier, also the canonical-encoding tag of both data digests. */
	public static final String CONTRACT = "dxf-spline-reference/v1";

	private final DxfNature nature;
	private final boolean rationalFlagSet;
	private final BSplineData sourceData;
	private final double planeZ;
	private final DxfAffineTransform transform;
	private final BSplineData data;
	private final DxfFileProvenance file;
	private final String handle;
	private final String layer;
	private final int ordinal;
	private final @CheckForNull double[] normal;
	private final Set<DxfDiagnostic> diagnostics;
	private final List<Integer> unrecognizedCodes;
	private final String sourceDigest;
	private final String dataDigest;
	private final DxfTessellation tessellation;

	DxfSplineReference(DxfNature nature, boolean rationalFlagSet, BSplineData sourceData,
			double planeZ, DxfAffineTransform transform, BSplineData data,
			DxfFileProvenance file, String handle, String layer, int ordinal,
			@CheckForNull double[] normal, Set<DxfDiagnostic> diagnostics,
			List<Integer> unrecognizedCodes, DxfTessellation tessellation) {
		this.nature = nature;
		this.rationalFlagSet = rationalFlagSet;
		this.sourceData = sourceData;
		this.planeZ = planeZ;
		this.transform = transform;
		this.data = data;
		this.file = file;
		this.handle = handle;
		this.layer = layer;
		this.ordinal = ordinal;
		this.normal = normal;
		this.diagnostics = diagnostics.isEmpty() ? Collections.emptySet()
				: Collections.unmodifiableSet(EnumSet.copyOf(diagnostics));
		this.unrecognizedCodes = List.copyOf(unrecognizedCodes);
		this.sourceDigest = BSplineCanonicalEncoding.digestHex(CONTRACT, sourceData);
		this.dataDigest = BSplineCanonicalEncoding.digestHex(CONTRACT, data);
		this.tessellation = tessellation;
	}

	/**
	 * @return {@link #CONTRACT}
	 */
	public String contract() {
		return CONTRACT;
	}

	/**
	 * @return nature decided by the effective weights
	 */
	public DxfNature nature() {
		return nature;
	}

	/**
	 * @return the code-70 rational flag (bit 4) as read
	 */
	public boolean rationalFlagSet() {
		return rationalFlagSet;
	}

	/**
	 * @return {@code EXPLICIT} when the file holds the code-41 values, {@code IMPLICIT_UNIT}
	 *         when it holds none (never recorded as code-41 values)
	 */
	public BSplineWeightMode weightProvenance() {
		return sourceData.weightMode();
	}

	/**
	 * @return degree, knots, control points {@code (x, y)} and weights exactly as read
	 */
	public BSplineData sourceData() {
		return sourceData;
	}

	/**
	 * @return the common {@code z} of the control points (plane {@code z = c})
	 */
	public double planeZ() {
		return planeZ;
	}

	/**
	 * @return effective weights {@code w_0..w_n} (1 for implicit unit weights)
	 */
	public double[] effectiveWeights() {
		double[] weights = new double[sourceData.controlPointCount()];
		for (int i = 0; i < weights.length; i++) {
			weights[i] = sourceData.weight(i);
		}
		return weights;
	}

	/**
	 * @return the transformation applied to the control points
	 */
	public DxfAffineTransform transform() {
		return transform;
	}

	/**
	 * @return the transformed data (same degree, knots and weights)
	 */
	public BSplineData data() {
		return data;
	}

	/**
	 * @return domain start {@code u_p}; orientation is increasing {@code u}
	 */
	public double domainStart() {
		return data.domainStart();
	}

	/**
	 * @return domain end {@code u_{n+1}}
	 */
	public double domainEnd() {
		return data.domainEnd();
	}

	/**
	 * @return whether {@code P_0} and {@code P_n} are equal under binary64 {@code ==}
	 */
	public boolean isExactlyClosed() {
		return sourceData.isExactlyClosed();
	}

	/**
	 * @return structural parametric continuity {@code C^{p-k}} per interior knot; no claim of
	 *         regularity or geometric continuity
	 */
	public List<BSplineInteriorKnot> continuity() {
		return data.interiorKnots();
	}

	/**
	 * @return file-level provenance
	 */
	public DxfFileProvenance file() {
		return file;
	}

	/**
	 * @return handle (code 5) as read, provenance only; empty when absent
	 */
	public String handle() {
		return handle;
	}

	/**
	 * @return layer name (code 8) as read, provenance only; empty when absent
	 */
	public String layer() {
		return layer;
	}

	/**
	 * @return one-based ordinal of the entity among the ENTITIES section entries
	 */
	public int ordinal() {
		return ordinal;
	}

	/**
	 * @return plane normal (codes 210/220/230) as recorded, or {@code null}; never used
	 */
	public @CheckForNull double[] normal() {
		return normal == null ? null : normal.clone();
	}

	/**
	 * @return informative diagnostics and ignored data
	 */
	public Set<DxfDiagnostic> diagnostics() {
		return diagnostics;
	}

	/**
	 * @return unrecognized group codes, increasing, each once
	 */
	public List<Integer> unrecognizedCodes() {
		return unrecognizedCodes;
	}

	/**
	 * @return SHA-256 of the canonical encoding of {@link #sourceData()}
	 */
	public String sourceDigest() {
		return sourceDigest;
	}

	/**
	 * @return SHA-256 of the canonical encoding of {@link #data()}
	 */
	public String dataDigest() {
		return dataDigest;
	}

	/**
	 * @return the derived render tessellation in model units
	 */
	public DxfTessellation tessellation() {
		return tessellation;
	}
}
