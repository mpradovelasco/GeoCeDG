/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.DependentAlgo;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.ExpressionValue;
import org.geogebra.common.kernel.arithmetic.MyBoolean;
import org.geogebra.common.kernel.arithmetic.NumberValue;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumberValue;

/**
 * Declarative identity of the pieces of a declared driver domain ({@code
 * locus-v2-existence-components.md} section 20; final corrective intervention of GeoCeDG
 * 1.2.2, alternative alpha): the piece is named by what its list element declares, never by
 * where the element stands in the list.
 *
 * <p>An element that is itself a durable object is named by its durable identity. Any other
 * element is named by the declaration of its two ends: a durable object by its durable
 * identity, a literal by its exact value, and an anonymous dependent value by the operation
 * that defines it over ends named in the same way. A piece whose declaration reaches a named
 * object without a durable identity has no declarative identity: a label is no identity,
 * and such a piece stays a place of its list, with the positional meaning that
 * {@code spline-v2-pair-materialization.md} approves. A durable identity is taken at the root of
 * its copy provenance, so that the copy of a construction declares the pieces of its
 * original. No index, order of enumeration, label, current value of a dependent object,
 * coordinate or Java reference enters the text.</p>
 */
public final class LocusDeclaredPieceIdentity {
	/** Mark of the identity of the only piece of a declaration, which is its whole domain. */
	private static final String SOLE = "sole/";
	/** Longest chain of anonymous operations that is named structurally. */
	private static final int MAXIMUM_DEPTH = 24;
	/** Longest chain of copy provenance that is followed to its root. */
	private static final int MAXIMUM_COPY_CHAIN = 64;

	private LocusDeclaredPieceIdentity() {
		// Utility class.
	}

	/**
	 * Names the pieces of a domain list of the shape that {@link LocusV2DomainDescriptor}
	 * accepts, in the order of the list.
	 *
	 * @param source domain list, already validated by the descriptor
	 * @param registry durable identities of the construction
	 * @return one identity per piece, pairwise distinct; {@code null} for a piece that has
	 *         no declarative identity: its declaration reaches a named object without a
	 *         durable identity, or another piece of the list repeats it. Such a piece is a
	 *         place of the list, as every piece was before
	 */
	public static List<String> of(GeoList source, SpatialIdentityRegistry registry) {
		List<String> identities = pieces(source, registry, true);
		ArrayList<String> named = new ArrayList<>(identities);
		named.removeIf(java.util.Objects::isNull);
		if (new HashSet<>(named).size() != named.size()) {
			// An object and its copy declare two pieces of one list: each is itself.
			identities = pieces(source, registry, false);
		}
		ArrayList<String> distinct = new ArrayList<>();
		for (String identity : identities) {
			distinct.add(identity == null
					|| identities.indexOf(identity) != identities.lastIndexOf(identity)
							? null : identities.size() == 1 ? SOLE + identity : identity);
		}
		return distinct;
	}

	/**
	 * @param identity identity of a declared piece
	 * @return whether the piece is the only one of its declaration: the whole declared
	 *         domain, whose roots keep the frame of the whole domain
	 */
	public static boolean isSole(String identity) {
		return identity.startsWith(SOLE);
	}

	/**
	 * @param identity identity of a declared piece
	 * @return what the piece declares, the same text whether it is the only piece of its
	 *         declaration or one of several
	 */
	public static String declaration(String identity) {
		return isSole(identity) ? identity.substring(SOLE.length()) : identity;
	}

	private static List<String> pieces(GeoList source, SpatialIdentityRegistry registry,
			boolean copyRoot) {
		ArrayList<String> identities = new ArrayList<>();
		for (int index = 1; index < source.size(); index++) {
			GeoElement element = source.get(index);
			String durable = durable(element, registry, copyRoot);
			if (durable != null) {
				identities.add("piece/" + durable);
				continue;
			}
			GeoList piece = (GeoList) element;
			try {
				identities.add("ends/" + framed(end(piece.get(0), registry, copyRoot, 0))
						+ framed(end(piece.get(1), registry, copyRoot, 0)));
			} catch (Unnamed unnamed) {
				identities.add(null);
			}
		}
		return identities;
	}

	/** @return the durable identity of a published object, or {@code null} */
	private static String durable(GeoElement geo, SpatialIdentityRegistry registry,
			boolean copyRoot) {
		PersistentGeoId id = registry == null ? null : registry.getPersistentGeoId(geo);
		GeoIdentityRecord record = id == null ? null : registry.getGeoRecord(id);
		if (record == null) {
			return null;
		}
		for (int step = 0; copyRoot && step < MAXIMUM_COPY_CHAIN; step++) {
			PersistentGeoId original = record.getCopySourceId();
			GeoIdentityRecord next = original == null ? null
					: registry.getGeoRecord(original);
			if (next == null) {
				// A provenance that names an object that no longer exists still names it.
				if (original != null) {
					id = original;
				}
				break;
			}
			id = original;
			record = next;
		}
		return "geo:" + id.toExternalForm();
	}

	private static String end(GeoElement geo, SpatialIdentityRegistry registry,
			boolean copyRoot, int depth) {
		String durable = durable(geo, registry, copyRoot);
		if (durable != null) {
			return durable;
		}
		if (geo.isLabelSet() || depth >= MAXIMUM_DEPTH) {
			// A named object without a durable identity has only its label, which is no
			// identity: the piece that it declares is a place of the list.
			throw new Unnamed();
		}
		AlgoElement parent = geo.getParentAlgorithm();
		if (parent == null) {
			return literal(geo);
		}
		if (parent instanceof DependentAlgo
				&& ((DependentAlgo) parent).getExpression() != null) {
			return "expr:" + expression(((DependentAlgo) parent).getExpression(), registry,
					copyRoot, depth + 1);
		}
		StringBuilder text = new StringBuilder("algo:")
				.append(parent.getClassName().getCommand()).append('#');
		for (int index = 0; index < parent.getOutputLength(); index++) {
			if (parent.getOutput(index) == geo) {
				text.append(index);
			}
		}
		text.append('(');
		for (GeoElement input : parent.getInput()) {
			text.append(framed(end(input, registry, copyRoot, depth + 1)));
		}
		return text.append(')').toString();
	}

	private static String expression(ExpressionValue value,
			SpatialIdentityRegistry registry, boolean copyRoot, int depth) {
		ExpressionValue plain = value == null ? null : value.unwrap();
		if (plain == null) {
			return "none";
		}
		if (plain instanceof GeoElement) {
			return end((GeoElement) plain, registry, copyRoot, depth);
		}
		if (plain instanceof ExpressionNode) {
			ExpressionNode node = (ExpressionNode) plain;
			if (depth >= MAXIMUM_DEPTH) {
				throw new Unnamed();
			}
			return "(" + framed(expression(node.getLeft(), registry, copyRoot, depth + 1))
					+ node.getOperation().name()
					+ framed(expression(node.getRight(), registry, copyRoot, depth + 1))
					+ ")";
		}
		if (plain instanceof MyBoolean) {
			return "bool:" + ((MyBoolean) plain).getBoolean();
		}
		if (plain instanceof NumberValue && plain.isConstant()) {
			return number(((NumberValue) plain).getDouble());
		}
		// Any other value has no declarative name here.
		throw new Unnamed();
	}

	private static String literal(GeoElement geo) {
		if (geo instanceof GeoBoolean) {
			return "bool:" + ((GeoBoolean) geo).getBoolean();
		}
		if (geo instanceof GeoNumberValue) {
			return number(((GeoNumberValue) geo).getDouble());
		}
		throw new Unnamed();
	}

	/** A declaration that reaches something without a declarative name. */
	private static final class Unnamed extends RuntimeException {
		private static final long serialVersionUID = 1L;

		Unnamed() {
			super(null, null, false, false);
		}
	}

	private static String number(double value) {
		return "num:" + Double.toHexString(value == 0 ? 0 : value);
	}

	private static String framed(String value) {
		return value.length() + ":" + value;
	}
}
