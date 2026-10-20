/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLocusND;

/**
 * Exact fingerprint of the non-driver inputs read by a reconstruction slice
 * (PRE-G9B-CU1, {@code locus-v2-semantics.md} v1.2).
 *
 * <p>The slice is reconstructed from the serialized form of its elements, so
 * the fingerprint is taken from the same serialization:</p>
 * <ul>
 * <li>the structural definition of every algorithm;</li>
 * <li>the complete serialized state of every element whose value does not
 * depend on the driver position;</li>
 * <li>for driver-dependent elements, their serialized options with numeric
 * values neutralized.</li>
 * </ul>
 *
 * <p>Moving only the driver therefore leaves the fingerprint unchanged, while
 * any change of a free value, of a definition or of an element option changes
 * it. The value of a locus output does not depend on the driver position, so
 * elements derived from such an output are classified by their other inputs.</p>
 */
public final class ReconstructionSliceFingerprint2D {
	/** Version participating in the evaluator signature. */
	public static final String VERSION = "reconstruction-slice-fingerprint/v1";

	private static final Pattern NUMERIC_ATTRIBUTE = Pattern.compile(
			"=\"[-+]?(?:[0-9]+\\.?[0-9]*|\\.[0-9]+)(?:[eE][-+]?[0-9]+)?\""
					+ "|=\"[-+]?(?:NaN|Infinity)\"");
	private static final Comparator<GeoElement> CONSTRUCTION_ORDER =
			Comparator.comparingInt(GeoElement::getConstructionIndex)
					.thenComparingLong(GeoElement::getID);

	private ReconstructionSliceFingerprint2D() {
		// Utility class.
	}

	/**
	 * @param slice elements read by the reconstruction slice
	 * @param driverRoots the driver state and coordinate
	 * @return SHA-256 hexadecimal digest of the normalized slice content
	 */
	public static String compute(Collection<GeoElement> slice,
			Collection<GeoElement> driverRoots) {
		List<GeoElement> ordered = new ArrayList<>(slice);
		ordered.sort(CONSTRUCTION_ORDER);
		Set<GeoElement> driverDependent =
				Collections.newSetFromMap(new IdentityHashMap<>());
		for (GeoElement root : driverRoots) {
			if (root != null) {
				driverDependent.add(root);
			}
		}
		Set<AlgoElement> written =
				Collections.newSetFromMap(new IdentityHashMap<>());
		StringBuilder content = new StringBuilder(VERSION);
		for (GeoElement geo : ordered) {
			AlgoElement parent = geo.getParentAlgorithm();
			if (!driverDependent.contains(geo) && parent != null
					&& !isLocusOutput(geo)
					&& readsAny(parent, driverDependent)) {
				driverDependent.add(geo);
			}
			if (parent != null && written.add(parent)) {
				XMLStringBuilder algorithm = new XMLStringBuilder(new StringBuilder());
				parent.getXML(algorithm, false);
				content.append("|A:").append(algorithm);
			}
			XMLStringBuilder element = new XMLStringBuilder(new StringBuilder());
			geo.getXML(false, element);
			String text = element.toString();
			content.append("|G:").append(driverDependent.contains(geo)
					? NUMERIC_ATTRIBUTE.matcher(text).replaceAll("=\"#\"") : text);
		}
		return sha256(content.toString());
	}

	private static boolean isLocusOutput(GeoElement geo) {
		return geo instanceof GeoLocusV2 || geo instanceof GeoLocusND;
	}

	private static boolean readsAny(AlgoElement algorithm,
			Set<GeoElement> driverDependent) {
		GeoElement[] inputs = algorithm.getInput();
		if (inputs == null) {
			return false;
		}
		for (GeoElement input : inputs) {
			if (input != null && driverDependent.contains(input)) {
				return true;
			}
		}
		return false;
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
