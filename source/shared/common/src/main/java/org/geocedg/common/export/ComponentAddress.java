/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

/**
 * Revision-local address of one independently classified source component.
 */
public final class ComponentAddress {

	private final String branchKey;
	private final String componentKey;
	private final Double parameterStart;
	private final Double parameterEnd;
	private final boolean startIncluded;
	private final boolean endIncluded;

	/**
	 * Creates an address without a semantic parameter interval.
	 *
	 * @param branchKey optional semantic branch key
	 * @param componentKey revision-local component key
	 */
	public ComponentAddress(String branchKey, String componentKey) {
		this(branchKey, componentKey, null, null, false, false);
	}

	/**
	 * Creates an address over one oriented finite semantic interval.
	 *
	 * @param branchKey optional semantic branch key
	 * @param componentKey revision-local component key
	 * @param parameterStart oriented interval start
	 * @param parameterEnd oriented interval end
	 * @param startIncluded whether the start belongs to the source domain
	 * @param endIncluded whether the end belongs to the source domain
	 */
	public ComponentAddress(String branchKey, String componentKey,
			double parameterStart, double parameterEnd, boolean startIncluded,
			boolean endIncluded) {
		this(branchKey, componentKey, Double.valueOf(parameterStart),
				Double.valueOf(parameterEnd), startIncluded, endIncluded);
	}

	private ComponentAddress(String branchKey, String componentKey,
			Double parameterStart, Double parameterEnd, boolean startIncluded,
			boolean endIncluded) {
		this.branchKey = optionalText(branchKey, "branch key");
		this.componentKey = requireText(componentKey, "component key");
		if ((parameterStart == null) != (parameterEnd == null)) {
			throw new IllegalArgumentException(
					"A semantic interval requires both endpoints");
		}
		if (parameterStart != null) {
			assertFinite(parameterStart, "parameter start");
			assertFinite(parameterEnd, "parameter end");
		}
		this.parameterStart = parameterStart;
		this.parameterEnd = parameterEnd;
		this.startIncluded = parameterStart != null && startIncluded;
		this.endIncluded = parameterEnd != null && endIncluded;
	}

	public String getBranchKey() {
		return branchKey;
	}

	public String getComponentKey() {
		return componentKey;
	}

	/** @return whether this address includes a semantic parameter interval */
	public boolean hasSemanticInterval() {
		return parameterStart != null;
	}

	public Double getParameterStart() {
		return parameterStart;
	}

	public Double getParameterEnd() {
		return parameterEnd;
	}

	public boolean isStartIncluded() {
		return startIncluded;
	}

	public boolean isEndIncluded() {
		return endIncluded;
	}

	private static String optionalText(String value, String name) {
		if (value == null) {
			return null;
		}
		return requireText(value, name);
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}

	private static void assertFinite(double value, String name) {
		if (Double.isNaN(value) || Double.isInfinite(value)) {
			throw new IllegalArgumentException(name + " must be finite");
		}
	}
}
