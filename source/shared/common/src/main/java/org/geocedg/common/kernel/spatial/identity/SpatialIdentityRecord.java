/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.List;
import java.util.Map;

/** Common contract for immutable, inert G9A1 persistence records. */
public interface SpatialIdentityRecord {
	/** @return this record's typed durable identity */
	SpatialIdentityId getId();

	/** @return the positive semantic record version */
	int getSemanticVersion();

	/** @return the canonical flat XML element name */
	String getXmlElementName();

	/** @return live typed references, excluding historical copy provenance */
	List<SpatialIdentityId> getReferences();

	/** @return optional immediate copy source, which is not a live reference */
	SpatialIdentityId getCopySourceId();

	/**
	 * Rewrites the record and its live references through one whole-closure map.
	 *
	 * @return the immutable remapped record
	 */
	SpatialIdentityRecord remap(Map<SpatialIdentityId, SpatialIdentityId> remap,
			boolean recordImmediateCopySource);
}
