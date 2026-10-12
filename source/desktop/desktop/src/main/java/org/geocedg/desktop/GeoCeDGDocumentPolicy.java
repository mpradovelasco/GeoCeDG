/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.File;

import org.geogebra.common.util.FileExtensions;
import org.geogebra.common.util.StringUtil;

/** Filename-routing policy; archive contents and geometry never derive from it. */
final class GeoCeDGDocumentPolicy {

	private GeoCeDGDocumentPolicy() {
	}

	static boolean isNative(File file) {
		return file != null && FileExtensions.GEOCEDG.equals(
				StringUtil.getFileExtension(file.getName()));
	}

	static boolean isCompatibilityInput(File file) {
		return file != null && FileExtensions.GEOGEBRA.equals(
				StringUtil.getFileExtension(file.getName()));
	}

	static boolean requiresNativeSaveAs(File currentFile) {
		return !isNative(currentFile);
	}

	static FileExtensions[] documentOpenExtensions() {
		return new FileExtensions[] {
				FileExtensions.GEOCEDG, FileExtensions.GEOGEBRA };
	}

	static File nativeSuggestion(File currentFile) {
		if (currentFile == null) {
			return null;
		}
		String name = currentFile.getName();
		int dot;
		while ((dot = name.lastIndexOf('.')) > 0
				&& !FileExtensions.UNKNOWN.equals(
						StringUtil.getFileExtension(name))) {
			name = name.substring(0, dot);
		}
		return new File(currentFile.getParentFile(), name + ".cedg");
	}

	static boolean hasConflictingSuffix(File nativeTarget) {
		if (!isNative(nativeTarget)) {
			return true;
		}
		String name = nativeTarget.getName();
		String stem = name.substring(0, name.length() - ".cedg".length());
		return !FileExtensions.UNKNOWN.equals(
				StringUtil.getFileExtension(stem));
	}

	static File normalizeNativeSuffix(File nativeTarget) {
		if (!isNative(nativeTarget)) {
			return nativeTarget;
		}
		String name = nativeTarget.getName();
		return new File(nativeTarget.getParentFile(),
				name.substring(0, name.length() - ".cedg".length()) + ".cedg");
	}

	/** @return whether the name carries the OFF extension (outside the GeoCeDG UI) */
	static boolean isOff(File file) {
		return file != null && FileExtensions.OFF.equals(
				StringUtil.getFileExtension(file.getName()));
	}

	/**
	 * PRE-G9B-R6-plus-F3 X1: Insert File and Apply Styles accept one .cedg or .ggb
	 * container. An existing file keeps its name; a missing name tries .cedg, then .ggb.
	 *
	 * @param selected chooser selection
	 * @return the resolved file (it may still be refused by {@link #insertionRefusal})
	 */
	static File resolveInsertionName(File selected) {
		if (selected == null || selected.exists() || isOff(selected)) {
			return selected;
		}
		for (FileExtensions extension : documentOpenExtensions()) {
			File candidate = new File(selected.getParentFile(),
					selected.getName() + "." + extension);
			if (candidate.isFile()) {
				return candidate;
			}
		}
		return selected;
	}

	/**
	 * @param file resolved source
	 * @return the localized reason key of a refusal, or null when the container is
	 *         accepted (its contents are admitted only by the insertion contract)
	 */
	static String insertionRefusal(File file) {
		if (isOff(file)) {
			return "Insert.Reject.Off";
		}
		if (file == null || !file.isFile()) {
			return "Insert.Reject.Missing";
		}
		if (!isNative(file) && !isCompatibilityInput(file)) {
			return "Insert.Reject.Extension";
		}
		return null;
	}

	/**
	 * PRE-G9B-R6-plus-F3 GeoCeDG Open (A1, OFF-B): an existing file keeps its name; a
	 * missing name tries .cedg, .ggb, .ggt, .html, .htm, never .off.
	 *
	 * @param selected chooser selection
	 * @return the file to open, or null when it does not resolve or is an OFF file
	 */
	static File resolveOpenName(File selected) {
		if (selected == null || isOff(selected)) {
			return null;
		}
		if (selected.exists()) {
			return selected;
		}
		FileExtensions[] candidates = {FileExtensions.GEOCEDG, FileExtensions.GEOGEBRA,
				FileExtensions.GEOGEBRA_TOOL, FileExtensions.HTML, FileExtensions.HTM};
		for (FileExtensions extension : candidates) {
			File candidate = new File(selected.getParentFile(),
					selected.getName() + "." + extension);
			if (candidate.isFile()) {
				return candidate;
			}
		}
		return null;
	}

	/**
	 * @param currentFile current document file, or null
	 * @return suggested name of a recovery copy next to it, or null
	 */
	static File recoveryCopySuggestion(File currentFile) {
		File suggestion = nativeSuggestion(currentFile);
		if (suggestion == null) {
			return null;
		}
		String name = suggestion.getName();
		return new File(suggestion.getParentFile(),
				name.substring(0, name.length() - ".cedg".length()) + "-recovered.cedg");
	}
}
