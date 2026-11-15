/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.common.kernel.locus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import org.geocedg.common.kernel.locus.constructive.AuthorityPolicy;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.kernel.algos.AlgoCirclePointRadius;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.algos.AlgoLinePointLine;
import org.geogebra.common.kernel.algos.AlgoPointOnPath;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;

/** Versioned declarations of driver directions, independent of locus image coordinates. */
public final class ConstructiveLocusCuts2D {

	public static final String CONTRACT = "constructive-cuts/v1";
	private static final String STATE_VERSION = "cut-witness-bindings/v1";

	private final GeoText version;
	private final List<GeoPoint> witnesses;
	// Keys identify live operands. Values are local to the owning locus.
	private Map<String, String> bindings;

	public ConstructiveLocusCuts2D(GeoText version, List<GeoPoint> witnesses) {
		if (version.isLabelSet() || !version.isIndependent() || !CONTRACT.equals(version.getTextString())
				|| witnesses.isEmpty()) {
			throw new IllegalArgumentException("Invalid constructive cut declaration");
		}
		this.version = version;
		this.witnesses = List.copyOf(witnesses);
	}

	public List<GeoPoint> getWitnesses() {
		return witnesses;
	}

	/** Reconstructs the declaration identities and their explicit witness associations. */
	public void restoreBindings(String state) {
		String[] fields = state.split("\\|", -1);
		if (bindings != null || fields.length < 2 || !STATE_VERSION.equals(fields[0])) {
			throw new IllegalArgumentException("Invalid or duplicate constructive cut bindings");
		}
		Map<String, String> restored = new TreeMap<>();
		for (int i = 1; i < fields.length; i++) {
			String[] pair = fields[i].split("=", -1);
			if (pair.length != 2 || restored.put(PersistentGeoId.parse(pair[0]).toExternalForm(),
					PersistentGeoId.parse(pair[1]).toExternalForm()) != null) {
				throw new IllegalArgumentException("Invalid constructive cut witness binding");
			}
		}
		if (new HashSet<>(restored.values()).size() != restored.size() || !encode(restored).equals(state)) {
			throw new IllegalArgumentException("Noncanonical constructive cut witness bindings");
		}
		bindings = restored;
	}

	/** @return persisted declaration state, or {@code null} while witnesses are unattached */
	public String bindingsForXML() {
		Map<String, String> state = bindings == null ? witnessBindings(witness -> witness.getConstruction()
				.getSpatialIdentityRegistry().getPersistentGeoIdForSerialization(witness)) : bindings;
		return state == null ? null : encode(state);
	}

	/** Rejects a persisted association that does not name the reconstructed witness operands. */
	public void validateBindings(Function<GeoPoint, PersistentGeoId> identity) {
		if (bindings != null) {
			Map<String, String> actual = witnessBindings(identity);
			if (actual == null || !bindings.keySet().equals(actual.keySet())) {
				throw new IllegalArgumentException("Cut bindings disagree with the witness operands");
			}
		}
	}

	/** Translates only the live witness side of each validated clipboard attachment. */
	public void copyWitnessBindings(Map<GeoElement, PersistentGeoId> original,
			Map<GeoElement, PersistentGeoId> copied) {
		Map<String, String> inputs = witnessBindings(original::get);
		if (inputs == null || bindings != null && !bindings.keySet().equals(inputs.keySet())) {
			throw new IllegalArgumentException("Cut declaration does not name the original witness closure");
		}
		Map<String, String> translated = new TreeMap<>();
		for (GeoPoint witness : witnesses) {
			PersistentGeoId replacement = copied.get(witness);
			if (replacement == null) {
				throw new IllegalArgumentException("Cut witness has no clipboard attachment");
			}
			String prior = original.get(witness).toExternalForm();
			translated.put(replacement.toExternalForm(), bindings == null ? prior : bindings.get(prior));
		}
		bindings = translated;
	}

	private Map<String, String> witnessBindings(Function<GeoPoint, PersistentGeoId> identity) {
		Map<String, String> ids = new TreeMap<>();
		for (GeoPoint witness : witnesses) {
			PersistentGeoId id = identity.apply(witness);
			if (id == null) {
				return null;
			}
			ids.put(id.toExternalForm(), id.toExternalForm());
		}
		return ids;
	}

	private static String encode(Map<String, String> state) {
		StringBuilder encoded = new StringBuilder(STATE_VERSION);
		state.forEach((witness, declaration) -> encoded.append('|').append(witness)
				.append('=').append(declaration));
		return encoded.toString();
	}

	/** The version is a command literal, and the witnesses are ordinary DAG inputs. */
	public List<GeoElement> getCommandArguments() {
		List<GeoElement> arguments = new ArrayList<>();
		arguments.add(version);
		arguments.addAll(witnesses);
		return arguments;
	}

	/** @return revision-scoped directions, or {@code null} if a witness has lost its premises */
	public List<LocusTraversalCut2D> resolve(GeoElement state, GeoElement support,
			SemanticGeneratorDescriptor1D descriptor, LocusDriverDomainProvider2D provider) {
		if (!(state instanceof GeoPoint driver)
				|| descriptor.getFamily() != SemanticGeneratorFamily1D.CIRCLE_POINT
				&& descriptor.getFamily() != SemanticGeneratorFamily1D.SEGMENT_POINT
				&& descriptor.getFamily() != SemanticGeneratorFamily1D.CIRCULAR_ARC_POINT) {
			return null;
		}
		var registry = driver.getConstruction().getSpatialIdentityRegistry();
		Map<String, String> inputs = witnessBindings(registry::getPersistentGeoId);
		if (inputs == null || bindings != null && !bindings.keySet().equals(inputs.keySet())) {
			return null;
		}
		Map<String, String> declarationsByWitness = bindings == null ? inputs : bindings;
		Map<Double, List<String>> declarations = new TreeMap<>();
		for (GeoPoint witness : witnesses) {
			if (witness == driver || witness.getConstruction() != driver.getConstruction()
					|| witness.isChildOf(driver) || !witness.isDefined()) {
				return null;
			}
			var id = registry.getPersistentGeoId(witness);
			double parameter = provider.canonicalize(parameter(witness, driver, support));
			if (id == null || !descriptor.getDeclaredDomain().contains(parameter, 0)) {
				return null;
			}
			declarations.computeIfAbsent(parameter, key -> new ArrayList<>())
					.add(declarationsByWitness.get(id.toExternalForm()));
		}
		return declarations.entrySet().stream().map(entry ->
				new LocusTraversalCut2D(entry.getKey(), entry.getValue())).toList();
	}

	private static double parameter(GeoPoint witness, GeoPoint driver, GeoElement support) {
		if (witness.getParentAlgorithm() instanceof AlgoPointOnPath point
				&& point.getP() == witness && point.getPath() == driver.getPath()
				&& witness.getPath() == driver.getPath()) {
			for (GeoElement input : point.getInput()) {
				if (!input.isDefined()) {
					return Double.NaN;
				}
			}
			return witness.getPathParameter().getT();
		}
		if (support instanceof GeoConic circle
				&& !AuthorityPolicy.of(circle.getConstruction()).isConstructive()) {
			return horizontalDiameterDirection(witness, circle);
		}
		return Double.NaN;
	}

	/** Reads the selected root of a proved horizontal diameter, including the cone witness K. */
	private static double horizontalDiameterDirection(GeoPoint witness, GeoConic circle) {
		if (!(witness.getParentAlgorithm() instanceof AlgoIntersectLineConic intersection)
				|| intersection.getConic() != circle
				|| !(circle.getParentAlgorithm() instanceof AlgoCirclePointRadius definedCircle)
				|| intersection.isSelectionFirstIntersectionPending()
				|| intersection.isSelectionPermutationPending()
				|| intersection.getSelectionExistingIntersection() != null) {
			return Double.NaN;
		}
		GeoLine diameter = intersection.getLine();
		if (diameter.getClass() != GeoLine.class
				|| !(diameter.getParentAlgorithm() instanceof AlgoLinePointLine throughCenter)
				|| throughCenter.getInput(0) != definedCircle.getCenter()
				|| diameter.getX() != 0 || diameter.getY() == 0
				|| circle.getEigenvec(0).getY() != 0 || Math.abs(circle.getEigenvec(0).getX()) != 1
				|| !(circle.getHalfAxis(0) > 0)) {
			return Double.NaN;
		}
		GeoPoint[] outputs = intersection.getIntersectionPoints();
		int index = outputs[0] == witness ? 0 : outputs[1] == witness ? 1 : -1;
		int[] permutation = intersection.getSelectionPermutation();
		if (index < 0 || permutation == null || permutation[index] < 0 || permutation[index] > 1) {
			return Double.NaN;
		}
		// Raw roots are ordered along (line.y, -line.x). The center and positive radius
		// guarantee distinct roots of opposite direction in the provider's local axes.
		boolean positive = (permutation[index] == 1)
				== (Math.copySign(1, diameter.getY()) == circle.getEigenvec(0).getX());
		return positive ? 0 : -Math.PI;
	}
}
