/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.importer.dxf;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.CheckForNull;

/**
 * Deterministic report of one import (section 15): file provenance, entity counts by type,
 * admitted records in file order, rejected entities with their causes, report diagnostics
 * and the transformation. A file cause means the whole import was rejected and nothing was
 * admitted. The report never claims exactness with respect to the decimal text of the file
 * or of the render.
 */
public final class DxfImportReport {

	private final @CheckForNull DxfCause fileCause;
	private final DxfFileProvenance file;
	private final DxfAffineTransform transform;
	private final SortedMap<String, Integer> entityCounts;
	private final List<DxfSplineReference> records;
	private final List<DxfRejectedEntity> rejected;
	private final Set<DxfDiagnostic> diagnostics;

	DxfImportReport(@CheckForNull DxfCause fileCause, DxfFileProvenance file,
			DxfAffineTransform transform, SortedMap<String, Integer> entityCounts,
			List<DxfSplineReference> records, List<DxfRejectedEntity> rejected,
			Set<DxfDiagnostic> diagnostics) {
		this.fileCause = fileCause;
		this.file = file;
		this.transform = transform;
		this.entityCounts = Collections.unmodifiableSortedMap(new TreeMap<>(entityCounts));
		this.records = List.copyOf(records);
		this.rejected = List.copyOf(rejected);
		this.diagnostics = diagnostics.isEmpty() ? Collections.emptySet()
				: Collections.unmodifiableSet(EnumSet.copyOf(diagnostics));
	}

	/**
	 * @return the cause that rejected the whole file, or {@code null}
	 */
	public @CheckForNull DxfCause fileCause() {
		return fileCause;
	}

	/**
	 * @return file provenance
	 */
	public DxfFileProvenance file() {
		return file;
	}

	/**
	 * @return the transformation requested for this import
	 */
	public DxfAffineTransform transform() {
		return transform;
	}

	/**
	 * @return counts of ENTITIES section entries by type, sorted by type name
	 */
	public SortedMap<String, Integer> entityCounts() {
		return entityCounts;
	}

	/**
	 * @return admitted records in file order
	 */
	public List<DxfSplineReference> records() {
		return records;
	}

	/**
	 * @return rejected entities in file order (BLOCKS after ENTITIES)
	 */
	public List<DxfRejectedEntity> rejected() {
		return rejected;
	}

	/**
	 * @return report diagnostics ({@link DxfDiagnostic#UNITS_DIFFER},
	 *         {@link DxfDiagnostic#INSERT_NOT_EXPANDED})
	 */
	public Set<DxfDiagnostic> diagnostics() {
		return diagnostics;
	}
}
