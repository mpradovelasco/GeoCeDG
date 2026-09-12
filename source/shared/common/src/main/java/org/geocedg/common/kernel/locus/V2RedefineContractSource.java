/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

/**
 * Declares the reconstructible semantic-constructor contract used by a public
 * V2 output during compatible redefine. The returned token is kernel authority;
 * Java class, command text and output ordinal are not.
 */
public interface V2RedefineContractSource {
	/**
	 * @return stable, versioned semantic-constructor contract identifier
	 */
	String getV2RedefineContractId();
}
