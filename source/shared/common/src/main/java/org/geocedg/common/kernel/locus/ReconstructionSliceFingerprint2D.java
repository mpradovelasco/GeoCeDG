/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoIntersect;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.algos.AlgoIntersectSingle;
import org.geogebra.common.kernel.arithmetic.MyDouble;
import org.geogebra.common.kernel.geos.GeoElement;

/**
 * Exact, versioned fingerprint of the non-driver inputs read by a reconstruction slice
 * (PRE-G9B-CU2, D-CU2-20-R; {@code locus-v2-semantics.md} v1.3).
 *
 * <p>It has four parts, each digested separately so that the cause of a change is
 * typed:</p>
 * <ul>
 * <li>structure: the serialized definition of every algorithm and the serialized options of
 * every dependent element, without the tags of its derived value, which its inputs and its
 * definition determine; a free element enters only by its kind and label, because its
 * serialized form is representation only;</li>
 * <li>values: the exact binary64 bits of every independent element, of every unlabelled
 * input of an algorithm and of every number literal of a definition;</li>
 * <li>selection: the selection state of every line-conic intersection, read through
 * read-only accessors (D-CU2-17);</li>
 * <li>labels: the label state of the outputs of every intersection.</li>
 * </ul>
 *
 * <p>Moving only the driver leaves every part unchanged. The fingerprint uses no Java
 * object identity, proximity, circumstantial index or drawing data, and never replaces a
 * durable identity.</p>
 */
public final class ReconstructionSliceFingerprint2D {
	/** Version participating in the evaluator signature. */
	public static final String VERSION = "reconstruction-slice-fingerprint/v2";

	/**
	 * Tags that carry the derived value of a dependent element, recomputed from its inputs
	 * and its definition, or the driver position of a point on a path or in a region.
	 */
	private static final Pattern DERIVED_VALUE_TAG = Pattern.compile(
			"<(?:coords|value|matrix|eigenvectors|pathParameter|regionParameters)\\b[^>]*/>");
	private static final Comparator<GeoElement> CONSTRUCTION_ORDER =
			Comparator.comparingInt(GeoElement::getConstructionIndex)
					.thenComparingLong(GeoElement::getID);

	/** Part of the snapshot whose change explains a new revision. */
	public enum Change {
		/** The snapshot is unchanged: an equivalent recompute. */
		NONE,
		/** A definition, an element option or the slice composition changed. */
		STRUCTURE,
		/** An exact authoritative value changed. */
		VALUES,
		/** The label state of an intersection output changed. */
		LABELS,
		/** The inherited selection state of an intersection changed. */
		SELECTION
	}

	/** Exact, structured snapshot of the non-driver inputs of a slice. */
	public static final class Snapshot {
		private final String structure;
		private final String values;
		private final String selection;
		private final String labels;
		private final String digest;

		private Snapshot(String structure, String values, String selection,
				String labels) {
			this.structure = structure;
			this.values = values;
			this.selection = selection;
			this.labels = labels;
			this.digest = sha256(VERSION + "|" + ReconstructionFidelity2D.VERSION
					+ "|" + structure + "|" + values + "|" + selection + "|" + labels);
		}

		/** @return digest of the whole snapshot */
		public String getDigest() {
			return digest;
		}

		/** @return digest of the structure part */
		public String getStructureDigest() {
			return structure;
		}

		/** @return digest of the exact values part */
		public String getValuesDigest() {
			return values;
		}

		/** @return digest of the selection part */
		public String getSelectionDigest() {
			return selection;
		}

		/** @return digest of the label part */
		public String getLabelsDigest() {
			return labels;
		}

		/**
		 * @param previous snapshot of the published revision, or {@code null}
		 * @return the first changed part, in the order structure, values, labels,
		 *         selection
		 */
		public Change changeFrom(Snapshot previous) {
			if (previous == null || !structure.equals(previous.structure)) {
				return Change.STRUCTURE;
			}
			if (!values.equals(previous.values)) {
				return Change.VALUES;
			}
			if (!labels.equals(previous.labels)) {
				return Change.LABELS;
			}
			return selection.equals(previous.selection) ? Change.NONE : Change.SELECTION;
		}
	}

	private ReconstructionSliceFingerprint2D() {
		// Utility class.
	}

	/**
	 * @param slice elements read by the reconstruction slice
	 * @param driverRoots the driver state and coordinate
	 * @return SHA-256 hexadecimal digest of the exact slice snapshot
	 */
	public static String compute(Collection<GeoElement> slice,
			Collection<GeoElement> driverRoots) {
		return snapshot(slice, driverRoots).getDigest();
	}

	/**
	 * @param slice elements read by the reconstruction slice
	 * @param driverRoots the driver state and coordinate
	 * @return exact, structured snapshot of the non-driver inputs
	 */
	public static Snapshot snapshot(Collection<GeoElement> slice,
			Collection<GeoElement> driverRoots) {
		List<GeoElement> ordered = new ArrayList<>(Objects.requireNonNull(slice));
		ordered.sort(CONSTRUCTION_ORDER);
		Set<GeoElement> driverDependent = ReconstructionExactState2D.driverDependent(
				ordered, driverRoots);
		Set<AlgoElement> written = Collections.newSetFromMap(new IdentityHashMap<>());
		StringBuilder structure = new StringBuilder();
		StringBuilder values = new StringBuilder();
		StringBuilder selection = new StringBuilder();
		StringBuilder labels = new StringBuilder();
		for (GeoElement geo : ordered) {
			String label = geo.getLabelSimple();
			AlgoElement parent = geo.getParentAlgorithm();
			if (parent != null && written.add(parent)) {
				XMLStringBuilder algorithm = new XMLStringBuilder(new StringBuilder());
				parent.getXML(algorithm, false);
				structure.append("|A:").append(algorithm);
				for (ReconstructionExactState2D.InlineInput inline
						: ReconstructionExactState2D.inlineInputs(parent)) {
					appendInline(values, label, inline, driverDependent);
				}
				appendSelection(selection, labels, label, parent);
			}
			String text;
			if (geo.isIndependent()) {
				// The serialized form of a free element is representation only: a value
				// set by program drops its literal and adds options. Its exact value is
				// in the values part, unless it is the driver.
				text = geo.getClass().getSimpleName() + ":" + label;
				if (!driverDependent.contains(geo)) {
					values.append("|V:").append(label).append(':')
							.append(ReconstructionExactState2D.of(geo));
				}
			} else {
				XMLStringBuilder element = new XMLStringBuilder(new StringBuilder());
				geo.getXML(false, element);
				text = DERIVED_VALUE_TAG.matcher(element.toString()).replaceAll("");
				if (!driverDependent.contains(geo)) {
					// Derived, but not always a function of the slice inputs: a random
					// value changes on recomputation and must publish a revision.
					values.append("|D:").append(label).append(':')
							.append(ReconstructionExactState2D.of(geo));
				}
			}
			structure.append("|G:").append(text);
			if (!geo.isIndependent()) {
				appendLiterals(values, "D:" + label, geo);
			}
		}
		return new Snapshot(sha256(structure.toString()), sha256(values.toString()),
				sha256(selection.toString()), sha256(labels.toString()));
	}

	private static void appendInline(StringBuilder values, String owner,
			ReconstructionExactState2D.InlineInput inline, Set<GeoElement> driverDependent) {
		values.append("|I:").append(owner).append(':').append(inline.path);
		if (inline.geo.isIndependent()) {
			values.append(':').append(ReconstructionExactState2D.of(inline.geo));
			return;
		}
		appendLiterals(values, "", inline.geo);
		if (!driverDependent.contains(inline.geo)
				&& !readsDriver(inline.geo, driverDependent)) {
			values.append(":=").append(ReconstructionExactState2D.of(inline.geo));
		}
	}

	private static boolean readsDriver(GeoElement geo, Set<GeoElement> driverDependent) {
		for (GeoElement predecessor : geo.getAllPredecessors()) {
			if (driverDependent.contains(predecessor)) {
				return true;
			}
		}
		return false;
	}

	private static void appendLiterals(StringBuilder values, String owner,
			GeoElement geo) {
		int index = 0;
		for (MyDouble literal : ReconstructionExactState2D.literals(geo.getDefinition())) {
			values.append("|L:").append(owner).append(':').append(index++).append(':')
					.append(Double.toHexString(literal.getDouble()));
		}
	}

	private static void appendSelection(StringBuilder selection, StringBuilder labels,
			String owner, AlgoElement algorithm) {
		if (algorithm instanceof AlgoIntersectSingle) {
			AlgoIntersectSingle single = (AlgoIntersectSingle) algorithm;
			selection.append("|S1:").append(owner).append(":index=")
					.append(single.getSelectedIndex());
			appendSelection(selection, labels, owner + "/source",
					single.getSourceIntersection());
			return;
		}
		if (algorithm instanceof AlgoIntersectLineConic) {
			AlgoIntersectLineConic intersection = (AlgoIntersectLineConic) algorithm;
			selection.append("|S:").append(owner).append(":permutation=")
					.append(Arrays.toString(intersection.getSelectionPermutation()))
					.append(":first=")
					.append(intersection.isSelectionFirstIntersectionPending())
					.append(":pending=")
					.append(intersection.isSelectionPermutationPending());
		}
		if (algorithm instanceof AlgoIntersect) {
			labels.append("|O:").append(owner).append(':');
			for (GeoElement output : algorithm.getOutput()) {
				labels.append(output.isLabelSet() ? '1' : '0');
			}
		}
	}

	private static String sha256(String text) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
					.digest(text.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder(digest.length * 2);
			for (byte value : digest) {
				hex.append(Character.forDigit((value >> 4) & 0xF, 16))
						.append(Character.forDigit(value & 0xF, 16));
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is unavailable", exception);
		}
	}
}
