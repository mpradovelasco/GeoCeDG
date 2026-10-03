/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.layers;

/**
 * PRE-G9B-R6-plus-A-2 (DQ-A2-4): fail-closed rejection of recognized hidden-layer
 * metadata. It is unchecked and not a {@code MyError}, so the Desktop document
 * preflight and the rejected-parse restore treat it as a rejection of the whole
 * load, exactly as for unit metadata.
 */
public final class HiddenLayerMetadataException extends IllegalStateException {
	private static final long serialVersionUID = 1L;

	/** Defect classes reported to the user. */
	public enum Code {
		/** A {@code version} greater than the highest supported version. */
		UNSUPPORTED_VERSION,
		/** A malformed version, attribute, layer list, child element or text. */
		MALFORMED_ELEMENT,
		/** A persisted hidden layer outside the reader's layer domain. */
		OUT_OF_DOMAIN_LAYER,
		/** Every layer of the domain hidden: no legal working layer exists. */
		ALL_LAYERS_HIDDEN,
		/** More than one element in one document. */
		DUPLICATE_ELEMENT,
		/** An element anywhere but directly under {@code <geogebra>}. */
		MISPLACED_ELEMENT
	}

	private final Code code;

	/**
	 * @param code defect class
	 * @param detail technical detail for the log
	 */
	public HiddenLayerMetadataException(Code code, String detail) {
		super(HiddenLayersXml.ELEMENT + " " + code + ": " + detail);
		this.code = code;
	}

	/**
	 * @return defect class
	 */
	public Code getCode() {
		return code;
	}

	/**
	 * @param failure a load failure
	 * @return the hidden-layer rejection in its cause or suppressed chain, or
	 *         {@code null}
	 */
	public static HiddenLayerMetadataException find(Throwable failure) {
		Throwable current = failure;
		int depth = 0;
		while (current != null && depth < 32) {
			if (current instanceof HiddenLayerMetadataException) {
				return (HiddenLayerMetadataException) current;
			}
			for (Throwable suppressed : current.getSuppressed()) {
				if (suppressed instanceof HiddenLayerMetadataException) {
					return (HiddenLayerMetadataException) suppressed;
				}
			}
			current = current.getCause();
			depth++;
		}
		return null;
	}
}
