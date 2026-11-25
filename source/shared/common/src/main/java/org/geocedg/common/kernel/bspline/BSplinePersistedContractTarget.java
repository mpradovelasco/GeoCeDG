/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.bspline;

/**
 * Receives, after a {@code BSpline} command was rebuilt from XML, the contract read from its
 * family element ({@code bspline-2d.md} section 8), so that a passive approximation
 * provenance block is written back unchanged.
 */
public interface BSplinePersistedContractTarget {
	/**
	 * @param contract contract read for this command
	 */
	void restorePersistedContract(BSplineContractXml.PersistedContract contract);
}
