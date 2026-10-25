/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.geocedg.common.kernel.locus.constructive.ConstructiveClassTable.Classification;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.arithmetic.NumberValue;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoPoint;

/**
 * The constructive cone of a relation ({@code locus-v2-constructive-authority.md} sections 1, 3
 * and 8.1): the objects of the relation and all their ancestors down to the free inputs, each
 * classified by {@code constructive-class/v1}, in an order in which every input precedes its
 * consumers. Every object has one node and therefore one constructive meaning, whatever the
 * relation, driver or consumer that asks for it; a declared boundary object is a leaf, except a
 * rotation, whose inputs are kept for the rigorous enclosure of its ideal value.
 *
 * <p>A cone may substitute the driver of one locus slice: the traced point is then evaluated
 * with the driver replaced by another node, as {@code Point(L, D)} requires. The objects that
 * depend on the driver have a second node inside that slice; every other object keeps its single
 * node, shared by the slice and the rest of the cone. The cone reads the construction and never
 * changes it; it is revision evidence and is never persisted.</p>
 */
public final class ConstructiveCone {

	/** Typed cause: the replacement of a substituted driver depends on the driver itself. */
	public static final String REPLACEMENT_DEPENDS_ON_DRIVER = "REPLACEMENT_DEPENDS_ON_DRIVER";

	private final List<ConstructiveConeNode> nodes;
	private final Map<GeoElement, Integer> documentNodes;
	private final Map<GeoElement, Integer> sliceNodes;
	private final int replacementNode;

	private ConstructiveCone(Builder builder) {
		nodes = Collections.unmodifiableList(new ArrayList<>(builder.nodes));
		documentNodes = new IdentityHashMap<>(builder.documentNodes);
		sliceNodes = new IdentityHashMap<>(builder.sliceNodes);
		replacementNode = builder.replacementNode;
	}

	/**
	 * @param construction the construction whose identity registry gives durable identities
	 * @return a builder of one cone
	 */
	public static Builder builder(Construction construction) {
		return new Builder(construction);
	}

	/** @return the nodes, every input before its consumers */
	public List<ConstructiveConeNode> getNodes() {
		return nodes;
	}

	/**
	 * @param index node index
	 * @return the node
	 */
	public ConstructiveConeNode getNode(int index) {
		return nodes.get(index);
	}

	/** @return the number of nodes */
	public int size() {
		return nodes.size();
	}

	/**
	 * @param geo an object of the construction
	 * @return its node outside any substituted slice, or {@code -1} when it is not in the cone
	 */
	public int nodeOf(GeoElement geo) {
		Integer node = documentNodes.get(geo);
		return node == null ? -1 : node;
	}

	/**
	 * @param geo an object of the construction
	 * @return its node inside the substituted slice: its own slice node when it depends on the
	 *         driver, otherwise its single node; {@code -1} when it is not in the cone
	 */
	public int substitutedNodeOf(GeoElement geo) {
		Integer node = sliceNodes.get(geo);
		return node == null ? nodeOf(geo) : node;
	}

	/** @return whether the cone substitutes the driver of a slice */
	public boolean hasDriverSubstitution() {
		return replacementNode >= 0;
	}

	/** @return the node that replaces the substituted driver, or {@code -1} */
	public int getReplacementNode() {
		return replacementNode;
	}

	/** @return the declared boundary and outside nodes, in cone order */
	public List<ConstructiveConeNode> getBoundaryNodes() {
		List<ConstructiveConeNode> boundary = new ArrayList<>();
		for (ConstructiveConeNode node : nodes) {
			ConstructiveOperation.Membership membership = node.getOperation().getMembership();
			if (membership == ConstructiveOperation.Membership.BOUNDARY
					|| membership == ConstructiveOperation.Membership.OUTSIDE) {
				boundary.add(node);
			}
		}
		return boundary;
	}

	/**
	 * The structural description of the cone: per node, its durable identity when it has one
	 * and its structural definition. It contains no label and no value, so a rename keeps it and
	 * a redefinition changes it.
	 *
	 * @return the description, one line per node
	 */
	public String describe() {
		StringBuilder text = new StringBuilder(ConstructiveClassTable.VERSION).append('\n');
		for (ConstructiveConeNode node : nodes) {
			text.append('n').append(node.getIndex()).append(' ')
					.append(node.getDurableIdentity() == null ? "-"
							: node.getDurableIdentity().toExternalForm())
					.append(' ').append(node.describe()).append('\n');
		}
		if (hasDriverSubstitution()) {
			text.append("driver := n").append(replacementNode).append('\n');
		}
		return text.toString();
	}

	/**
	 * The state of the cone that its evaluations read: the structural definition of every node
	 * and the captured binary64 state of every node outside a substituted slice. Durable
	 * identities never enter an evaluation, so attaching one keeps the digest; the captured state
	 * of a slice node belongs to the live driver and is never read, so moving the driver of the
	 * slice keeps it too; any change of an input of the relation changes it.
	 *
	 * @return the digest text
	 */
	public String getStateDigest() {
		StringBuilder text = new StringBuilder(ConstructiveClassTable.VERSION).append('\n');
		for (ConstructiveConeNode node : nodes) {
			text.append(node.describe()).append('\n');
		}
		text.append("driver := n").append(replacementNode).append('\n');
		for (ConstructiveConeNode node : nodes) {
			if (node.isDriverSlice()) {
				continue;
			}
			text.append('n').append(node.getIndex()).append(node.isLiveDefined() ? '+' : '-');
			for (double value : node.getLiveState()) {
				text.append(Long.toHexString(Double.doubleToLongBits(value))).append(',');
			}
			text.append('\n');
		}
		return text.toString();
	}

	@Override
	public String toString() {
		return describe();
	}

	/** Typed refusal of a cone that cannot be built. */
	public static final class ConstructiveConeException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private final String code;

		ConstructiveConeException(String code, String message) {
			super(code + ": " + message);
			this.code = code;
		}

		/** @return the typed cause */
		public String getCode() {
			return code;
		}
	}

	/** Builds one cone from the objects of a relation. */
	public static final class Builder {
		private final SpatialIdentityRegistry registry;
		private final List<ConstructiveConeNode> nodes = new ArrayList<>();
		private final Map<GeoElement, Integer> documentNodes = new IdentityHashMap<>();
		private final Map<GeoElement, Integer> sliceNodes = new IdentityHashMap<>();
		private final Map<GeoElement, Boolean> driven = new IdentityHashMap<>();
		private GeoElement driver;
		private int replacementNode = -1;
		private boolean built;

		private Builder(Construction construction) {
			registry = Objects.requireNonNull(construction).getSpatialIdentityRegistry();
		}

		/**
		 * Adds an object of the relation with its ancestors.
		 *
		 * @param geo the object
		 * @return its node
		 */
		public int add(GeoElement geo) {
			requireOpen();
			return walk(Objects.requireNonNull(geo), false);
		}

		/**
		 * Adds the traced point of a locus slice with its driver replaced by another object,
		 * which is added first with its own ancestors. One cone substitutes at most one driver.
		 *
		 * @param traced the traced point of the slice
		 * @param sliceDriver the driver of the slice
		 * @param replacement the object that replaces the driver
		 * @return the node of the traced point inside the substituted slice
		 * @throws ConstructiveConeException {@value #REPLACEMENT_DEPENDS_ON_DRIVER} when the
		 *         replacement depends on the driver
		 */
		public int addWithDriverSubstitution(GeoElement traced, GeoElement sliceDriver,
				GeoElement replacement) {
			requireOpen();
			Objects.requireNonNull(traced);
			Objects.requireNonNull(replacement);
			if (driver != null) {
				throw new IllegalStateException("A cone substitutes at most one driver");
			}
			driver = Objects.requireNonNull(sliceDriver);
			driven.clear();
			if (isDriven(replacement)) {
				driver = null;
				driven.clear();
				throw new ConstructiveConeException(REPLACEMENT_DEPENDS_ON_DRIVER,
						"the point that replaces the driver depends on the driver");
			}
			replacementNode = walk(replacement, false);
			return walk(traced, true);
		}

		/** @return the cone */
		public ConstructiveCone build() {
			requireOpen();
			built = true;
			return new ConstructiveCone(this);
		}

		private void requireOpen() {
			if (built) {
				throw new IllegalStateException("The cone is already built");
			}
		}

		/** Post-order walk without recursion: every input gets its node first. */
		private int walk(GeoElement root, boolean slice) {
			Integer known = resolve(root, slice);
			if (known != null) {
				return known;
			}
			Deque<Frame> stack = new ArrayDeque<>();
			stack.push(new Frame(root, inSlice(root, slice)));
			int last = -1;
			while (!stack.isEmpty()) {
				Frame frame = stack.peek();
				List<GeoElement> inputs = frame.classification.getInputs();
				if (frame.next < inputs.size()) {
					GeoElement input = inputs.get(frame.next);
					Integer inputNode = resolve(input, frame.slice);
					if (inputNode != null) {
						frame.inputNodes[frame.next++] = inputNode;
					} else {
						stack.push(new Frame(input, inSlice(input, frame.slice)));
					}
					continue;
				}
				stack.pop();
				last = create(frame);
				if (!stack.isEmpty()) {
					Frame consumer = stack.peek();
					consumer.inputNodes[consumer.next++] = last;
				}
			}
			return last;
		}

		private Integer resolve(GeoElement geo, boolean slice) {
			if (slice && geo == driver) {
				return replacementNode;
			}
			return (inSlice(geo, slice) ? sliceNodes : documentNodes).get(geo);
		}

		private boolean inSlice(GeoElement geo, boolean slice) {
			return slice && geo != driver && isDriven(geo);
		}

		/**
		 * Creates the node of a walked object. An object computed from an object outside the
		 * class is cut: a point becomes a declared boundary substitute and any other object
		 * stays outside the class (section 3.2).
		 */
		private int create(Frame frame) {
			Classification classification = frame.classification;
			GeoElement geo = frame.geo;
			ConstructiveOperation operation = classification.getOperation();
			int[] inputs = frame.inputNodes;
			int outputIndex = classification.getOutputIndex();
			InheritedLineCircleSelection selection = classification.getSelection();
			ConstructiveClassTable.BoundaryCategory category = classification.getCategory();
			String reason = classification.getReason();
			if (readsOutsideObject(inputs)) {
				operation = operation.getShape() == ConstructiveOperation.Shape.POINT
						? ConstructiveOperation.BOUNDARY_POINT : ConstructiveOperation.OUTSIDE;
				inputs = new int[0];
				outputIndex = -1;
				selection = null;
				category = ConstructiveClassTable.BoundaryCategory.OTHER_ALGORITHM;
				reason = "computed from an object outside the class";
			}
			int index = nodes.size();
			nodes.add(new ConstructiveConeNode(index, operation, inputs, outputIndex, selection,
					operation.isBounded() && classification.hasOutlyingIntersections(), category,
					reason, frame.slice, durableIdentity(geo),
					geo.isLabelSet() ? geo.getLabelSimple() : null, geo.isDefined(),
					liveState(geo, operation)));
			(frame.slice ? sliceNodes : documentNodes).put(geo, index);
			return index;
		}

		private boolean readsOutsideObject(int[] inputs) {
			for (int input : inputs) {
				if (nodes.get(input).getOperation().getMembership()
						== ConstructiveOperation.Membership.OUTSIDE) {
					return true;
				}
			}
			return false;
		}

		private PersistentGeoId durableIdentity(GeoElement geo) {
			return registry.getPersistentGeoId(geo);
		}

		/**
		 * Whether the value of an object moves with the driver. The value of a locus does not
		 * depend on the driver position, so objects computed from a locus are classified by
		 * their other inputs, as in the revision fingerprint of class v3.
		 */
		private boolean isDriven(GeoElement root) {
			if (driver == null) {
				return false;
			}
			Deque<GeoElement> stack = new ArrayDeque<>();
			stack.push(root);
			while (!stack.isEmpty()) {
				GeoElement geo = stack.peek();
				if (driven.containsKey(geo)) {
					stack.pop();
					continue;
				}
				if (geo == driver) {
					driven.put(geo, true);
					stack.pop();
					continue;
				}
				AlgoElement parent = geo.getParentAlgorithm();
				if (parent == null || ConstructiveClassTable.isLocusOutput(geo)) {
					driven.put(geo, false);
					stack.pop();
					continue;
				}
				boolean pending = false;
				boolean result = false;
				for (int index = 0; index < parent.getInputLength() && !result; index++) {
					GeoElement input = parent.getInput(index).toGeoElement();
					Boolean known = driven.get(input);
					if (known == null) {
						stack.push(input);
						pending = true;
					} else {
						result = known;
					}
				}
				if (result || !pending) {
					driven.put(geo, result);
					stack.pop();
				}
			}
			return driven.get(root);
		}

		private static double[] liveState(GeoElement geo,
				ConstructiveOperation operation) {
			switch (operation.getShape()) {
			case POINT:
				if (geo instanceof GeoPoint) {
					GeoPoint point = (GeoPoint) geo;
					return new double[] {point.getX(), point.getY(), point.getZ()};
				}
				return new double[0];
			case NUMBER:
				return geo instanceof NumberValue
						? new double[] {((NumberValue) geo).getDouble()} : new double[0];
			case LINE:
				if (geo instanceof GeoLine) {
					GeoLine line = (GeoLine) geo;
					return new double[] {line.getX(), line.getY(), line.getZ()};
				}
				return new double[0];
			case CIRCLE:
				return geo instanceof GeoConic ? ((GeoConic) geo).getFlatMatrix().clone()
						: new double[0];
			default:
				return new double[0];
			}
		}

		/** One object of the walk with the nodes of its inputs found so far. */
		private static final class Frame {
			private final GeoElement geo;
			private final boolean slice;
			private final Classification classification;
			private final int[] inputNodes;
			private int next;

			private Frame(GeoElement geo, boolean slice) {
				this.geo = geo;
				this.slice = slice;
				classification = ConstructiveClassTable.classify(geo);
				inputNodes = new int[classification.getInputs().size()];
			}
		}
	}
}
