/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.geocedg.common.export.SourceExportOutcome.Fidelity;

/**
 * Immutable result of adapting and classifying one requested export before any
 * destination is selected or written.
 */
public final class GeometryExportPreflight {

	@FunctionalInterface
	interface SourceRevisionGuard {
		/** @return whether the captured source authority is still current */
		boolean isCurrent();
	}

	private final GeometryExportRequest request;
	private final GeometryExportModel model;
	private final List<SourceRevisionGuard> revisionGuards;
	private final int exactCount;
	private final int approximateCount;
	private final int unsupportedCount;
	private final int invalidCount;
	private final int hiddenCount;
	private final boolean sidecarRequired;
	private final boolean writable;

	GeometryExportPreflight(GeometryExportRequest request,
			GeometryExportModel model, List<SourceRevisionGuard> revisionGuards) {
		if (request == null || model == null || revisionGuards == null) {
			throw new IllegalArgumentException(
					"Request, model, and source revision guards are required");
		}
		this.request = request;
		this.model = model;
		this.revisionGuards = Collections.unmodifiableList(
				new ArrayList<>(revisionGuards));
		int exact = 0;
		int approximate = 0;
		int unsupported = 0;
		int invalid = 0;
		Set<String> hiddenSources = new LinkedHashSet<>();
		for (SourceExportOutcome outcome : model.getOutcomes()) {
			if (!outcome.isVisible()) {
				hiddenSources.add(outcome.getIdentityScope() + ":"
						+ outcome.getSourceId());
			}
			switch (outcome.getFidelity()) {
			case EXACT:
				exact++;
				break;
			case APPROXIMATE:
				approximate++;
				break;
			case UNSUPPORTED:
				unsupported++;
				break;
			case INVALID:
				invalid++;
				break;
			default:
				throw new IllegalStateException("Unknown export fidelity");
			}
		}
		exactCount = exact;
		approximateCount = approximate;
		unsupportedCount = unsupported;
		invalidCount = invalid;
		hiddenCount = hiddenSources.size();
		sidecarRequired = request.isSidecarRequested()
				|| model.hasFidelityReduction();
		writable = !request.isPartialOutputAllowed()
				&& !model.getEntities().isEmpty() && unsupported == 0
				&& invalid == 0;
	}

	public GeometryExportRequest getRequest() {
		return request;
	}

	public GeometryExportModel getModel() {
		return model;
	}

	public int getExactCount() {
		return exactCount;
	}

	public int getApproximateCount() {
		return approximateCount;
	}

	public int getUnsupportedCount() {
		return unsupportedCount;
	}

	public int getInvalidCount() {
		return invalidCount;
	}

	/** @return requested components that will not be emitted */
	public int getOmittedCount() {
		return unsupportedCount + invalidCount;
	}

	public int getHiddenCount() {
		return hiddenCount;
	}

	public boolean isSidecarRequired() {
		return sidecarRequired;
	}

	/**
	 * @return whether strict policy admits the complete request before a
	 *         destination is chosen
	 */
	public boolean isWritable() {
		return writable;
	}

	/** @return whether every source snapshot still matches preflight */
	public boolean isSourceRevisionCurrent() {
		for (SourceRevisionGuard guard : revisionGuards) {
			try {
				if (guard == null || !guard.isCurrent()) {
					return false;
				}
			} catch (RuntimeException exception) {
				return false;
			}
		}
		return true;
	}

	GeometryExportModel requireWritableCurrentModel() {
		if (!writable) {
			throw new IllegalStateException(
					"Strict preflight does not admit the complete export request");
		}
		if (!isSourceRevisionCurrent()) {
			throw new IllegalStateException(
					Fidelity.INVALID + ": STALE_SOURCE_REVISION");
		}
		return model;
	}
}
