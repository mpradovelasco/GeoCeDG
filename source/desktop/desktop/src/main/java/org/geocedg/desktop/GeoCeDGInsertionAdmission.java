/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geocedg.common.kernel.algos.AlgoSplineV2;
import org.geocedg.common.kernel.dimension.AlgoAlignedDimension;
import org.geocedg.common.kernel.dimension.AlgoLinearDimension;
import org.geocedg.common.kernel.sheet.AlgoIsoABorder;
import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.algos.AlgoAnglePoints;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoMacro;
import org.geogebra.common.kernel.geos.GeoAngle;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoFunction;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoPolygon;
import org.geogebra.common.kernel.geos.GeoRay;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.geos.GeoVector;
import org.geogebra.common.kernel.kernelND.GeoConicNDConstants;
import org.geogebra.common.plugin.EventType;

/**
 * PRE-G9B-R6-plus-F3 admission of an Insert File source (contract R, matrix P4, macro
 * policy M1/P3, DQ-F3-24): a whitelist evaluated on the loaded hidden helper before
 * anything in the target changes. Successful parsing is never evidence of
 * admissibility; every object of the source construction must match a row.
 */
final class GeoCeDGInsertionAdmission {

	/** Admission families of the matrix. */
	enum Family {
		/** elementary 2D construction types admitted without condition */
		ELEMENTARY,
		/** Locus V2 family: identity-bearing, conditions of P4 */
		LOCUS_V2,
		/** Spline V2: identity-bearing, conditions of P4 */
		SPLINE_V2,
		/** LinearDimension and AlignedDimension outputs */
		DIMENSION,
		/** IsoABorder outputs; the session export link is never imported */
		ISO_A_BORDER,
		/** macro-generated objects under M1 */
		MACRO
	}

	private static final String LOCUS_V2_PACKAGE = "org.geocedg.common.kernel.algos";

	private final boolean admitted;
	private final String reasonKey;
	private final String reasonArgument;
	private final List<GeoElement> source;
	private final Map<GeoElement, Family> families;
	private final boolean identityBearing;
	private final Set<String> duplicateLabels;
	private final Set<String> reusedMacros;
	private final Set<String> transferredMacros;

	private GeoCeDGInsertionAdmission(boolean admitted, String reasonKey, String reasonArgument,
			List<GeoElement> source, Map<GeoElement, Family> families, boolean identityBearing,
			Set<String> duplicateLabels, Set<String> reusedMacros, Set<String> transferredMacros) {
		this.admitted = admitted;
		this.reasonKey = reasonKey;
		this.reasonArgument = reasonArgument;
		this.source = source;
		this.families = families;
		this.identityBearing = identityBearing;
		this.duplicateLabels = duplicateLabels;
		this.reusedMacros = reusedMacros;
		this.transferredMacros = transferredMacros;
	}

	private static GeoCeDGInsertionAdmission reject(String key, String argument) {
		return new GeoCeDGInsertionAdmission(false, key, argument, List.of(), Map.of(), false,
				Set.of(), Set.of(), Set.of());
	}

	/**
	 * @param target document receiving the insertion
	 * @param helper hidden helper holding the loaded source
	 * @return the admission decision and, when admitted, the insertion plan
	 */
	static GeoCeDGInsertionAdmission admit(AppGeoCeDG target, AppGeoCeDG helper) {
		Construction sourceConstruction = helper.getKernel().getConstruction();
		List<GeoElement> source = new ArrayList<>(
				sourceConstruction.getGeoSetWithCasCellsConstructionOrder());
		if (source.isEmpty()) {
			return reject("Insert.Reject.Empty", "");
		}
		List<String> scripted = new ArrayList<>();
		List<String> unsupported = new ArrayList<>();
		Map<GeoElement, Family> families = new LinkedHashMap<>();
		boolean identity = false;
		for (GeoElement geo : source) {
			if (hasScript(geo)) {
				scripted.add(geo.getLabelSimple());
				continue;
			}
			Family family = classify(geo);
			if (family == null) {
				unsupported.add(geo.getLabelSimple() + " (" + geo.getTypeString() + ")");
				continue;
			}
			families.put(geo, family);
			identity |= sourceConstruction.getSpatialIdentityRegistry().isParticipating(geo);
		}
		if (!scripted.isEmpty()) {
			return reject("Insert.Reject.Scripts", String.join(", ", scripted));
		}
		if (!unsupported.isEmpty()) {
			return reject("Insert.Reject.Unsupported", String.join(", ", unsupported));
		}
		// Elementary inputs of a Locus V2 or Spline V2 (its parameter, its point) take part
		// in the identity registry with it; spatial and projection-binding records are
		// rejected by the matrix itself (their algorithms and classes are not admitted).
		LinkedHashSet<Macro> required = requiredMacros(source);
		if (identity && !required.isEmpty()) {
			return reject("Insert.Reject.IdentityMacros", macroNames(required));
		}
		if (identity && !RuntimeFeatureService.mayCreateLocusV2(
				target.getKernel().getConstruction())) {
			return reject("Insert.Reject.LocusV2Unavailable", "");
		}
		Set<String> reused = new LinkedHashSet<>();
		Set<String> transferred = new LinkedHashSet<>();
		// source registration order is a valid dependency order for the transfer
		List<Macro> ordered = new ArrayList<>();
		if (helper.getKernel().hasMacros()) {
			ordered.addAll(helper.getKernel().getAllMacros());
		}
		ordered.retainAll(required);
		for (Macro macro : required) {
			if (!ordered.contains(macro)) {
				ordered.add(macro);
			}
		}
		for (Macro macro : ordered) {
			String sourceDigest;
			try {
				sourceDigest = GeoCeDGUserToolLibrary.definitionDigest(macro);
			} catch (IOException | RuntimeException malformed) {
				return reject("Insert.Reject.MacroMalformed", macro.getCommandName());
			}
			Macro existing = target.getKernel().getMacro(macro.getCommandName());
			if (existing == null) {
				transferred.add(macro.getCommandName());
				continue;
			}
			String targetDigest;
			try {
				targetDigest = GeoCeDGUserToolLibrary.definitionDigest(existing);
			} catch (IOException | RuntimeException malformed) {
				targetDigest = null;
			}
			if (!sourceDigest.equals(targetDigest)) {
				// name equality alone never establishes equivalence
				return reject("Insert.Reject.MacroConflict", macro.getCommandName());
			}
			reused.add(macro.getCommandName());
		}
		Set<String> duplicates = new LinkedHashSet<>();
		for (GeoElement geo : source) {
			if (geo.isLabelSet() && target.getKernel().lookupLabel(geo.getLabelSimple()) != null) {
				duplicates.add(geo.getLabelSimple());
			}
		}
		return new GeoCeDGInsertionAdmission(true, null, null,
				Collections.unmodifiableList(source), Collections.unmodifiableMap(families),
				identity, Collections.unmodifiableSet(duplicates),
				Collections.unmodifiableSet(reused), Collections.unmodifiableSet(transferred));
	}

	private static boolean hasScript(GeoElement geo) {
		for (EventType type : EventType.values()) {
			if (geo.getScript(type) != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param geo source object
	 * @return its admission family, or null when no row of the matrix admits it
	 */
	static Family classify(GeoElement geo) {
		if (geo.isGeoElement3D()) {
			return null;
		}
		AlgoElement parent = geo.getParentAlgorithm();
		if (parent != null && isThreeDimensional(parent)) {
			return null;
		}
		if (parent instanceof AlgoMacro) {
			return isElementaryClass(geo) ? Family.MACRO : null;
		}
		if (parent instanceof AlgoSplineV2) {
			return Family.SPLINE_V2;
		}
		if (parent != null && LOCUS_V2_PACKAGE.equals(parent.getClass().getPackageName())) {
			return Family.LOCUS_V2;
		}
		if (parent != null && (parent.getClass() == AlgoLinearDimension.class
				|| parent.getClass() == AlgoAlignedDimension.class)) {
			return Family.DIMENSION;
		}
		if (parent != null && parent.getClass() == AlgoIsoABorder.class) {
			return Family.ISO_A_BORDER;
		}
		if (parent != null && parent.getClass().getName().startsWith("org.geocedg.")) {
			// spatial, projection-binding and every other unlisted product algorithm
			return null;
		}
		if (geo.getClass().getName().startsWith("org.geocedg.")) {
			return null;
		}
		if (!isElementaryClass(geo)) {
			return null;
		}
		if (geo.getClass() == GeoAngle.class && !(parent instanceof AlgoAnglePoints)) {
			// angles are admitted from three points only
			return null;
		}
		if (geo.getClass() == GeoText.class && parent == null) {
			// dependent texts only
			return null;
		}
		return Family.ELEMENTARY;
	}

	private static boolean isThreeDimensional(AlgoElement parent) {
		String name = parent.getClass().getName();
		return name.contains(".kernel3D.") || name.contains("geogebra3D");
	}

	/**
	 * Exact classes of the elementary rows (subclasses such as arcs, images, buttons,
	 * input boxes or CAS cells are not admitted).
	 */
	private static boolean isElementaryClass(GeoElement geo) {
		Class<?> type = geo.getClass();
		if (type == GeoPoint.class || type == GeoLine.class || type == GeoRay.class
				|| type == GeoSegment.class || type == GeoVector.class
				|| type == GeoFunction.class || type == GeoPolygon.class
				|| type == GeoAngle.class || type == GeoNumeric.class
				|| type == GeoBoolean.class || type == GeoText.class) {
			return true;
		}
		if (type == GeoConic.class) {
			int conic = ((GeoConic) geo).getType();
			return conic == GeoConicNDConstants.CONIC_CIRCLE
					|| conic == GeoConicNDConstants.CONIC_ELLIPSE;
		}
		if (type == GeoList.class) {
			GeoList list = (GeoList) geo;
			for (int i = 0; i < list.size(); i++) {
				GeoElement element = list.get(i);
				if (element.isGeoElement3D() || !isElementaryClass(element)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * @param source source construction objects
	 * @return the macros of macro-generated objects and, transitively, every macro used
	 *         inside their definitions
	 */
	static LinkedHashSet<Macro> requiredMacros(List<GeoElement> source) {
		LinkedHashSet<Macro> needed = new LinkedHashSet<>();
		List<Macro> queue = new ArrayList<>();
		for (GeoElement geo : source) {
			if (geo.getParentAlgorithm() instanceof AlgoMacro algo) {
				queue.add(algo.getMacro());
			}
		}
		while (!queue.isEmpty()) {
			Macro macro = queue.remove(0);
			if (needed.add(macro)) {
				for (GeoElement inner : macro.getMacroConstruction().getGeoSetConstructionOrder()) {
					if (inner.getParentAlgorithm() instanceof AlgoMacro algo) {
						queue.add(algo.getMacro());
					}
				}
			}
		}
		return needed;
	}

	private static String macroNames(Set<Macro> macros) {
		List<String> names = new ArrayList<>();
		for (Macro macro : macros) {
			names.add(macro.getCommandName());
		}
		return String.join(", ", names);
	}

	boolean isAdmitted() {
		return admitted;
	}

	String getReasonKey() {
		return reasonKey;
	}

	String getReasonArgument() {
		return reasonArgument;
	}

	/** @return source objects in construction order */
	List<GeoElement> getSource() {
		return source;
	}

	/** @return family of every source object */
	Map<GeoElement, Family> getFamilies() {
		return families;
	}

	/** @return whether a source object participates in the identity registry */
	boolean isIdentityBearing() {
		return identityBearing;
	}

	/** @return source labels already used in the target (renamed by L1) */
	Set<String> getDuplicateLabels() {
		return duplicateLabels;
	}

	/** @return required macros reused from the target (equal product digest) */
	Set<String> getReusedMacros() {
		return reusedMacros;
	}

	/** @return required macros the target does not have (transferred once) */
	Set<String> getTransferredMacros() {
		return transferredMacros;
	}
}
