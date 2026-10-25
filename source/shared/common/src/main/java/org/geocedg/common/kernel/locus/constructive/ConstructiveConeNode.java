/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.Arrays;

import org.geocedg.common.kernel.locus.constructive.ConstructiveClassTable.BoundaryCategory;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;

/**
 * One node of a constructive cone: an object of the construction with its operation, its
 * inputs by structural position, and the binary64 state it had when the cone was captured.
 * A node is identified by its durable identity when the object has one and otherwise by its
 * structural definition; its label is a presentation name and never identifies it.
 *
 * <p>The captured state is the definition of a free input, the persisted coordinates of a point
 * on a path and the value of a boundary substitute. For every other node it is the live output,
 * used only to check that a branch corresponds to the live algorithm outside the slice of a
 * substituted driver; inside that slice it belongs to another driver position and is never
 * consulted.</p>
 */
public final class ConstructiveConeNode {

	private final int index;
	private final ConstructiveOperation operation;
	private final int[] inputs;
	private final int outputIndex;
	private final InheritedLineCircleSelection selection;
	private final boolean outlyingIntersections;
	private final BoundaryCategory category;
	private final String reason;
	private final boolean driverSlice;
	private final PersistentGeoId durableIdentity;
	private final String presentationName;
	private final boolean liveDefined;
	private final double[] liveState;

	ConstructiveConeNode(int index, ConstructiveOperation operation, int[] inputs,
			int outputIndex, InheritedLineCircleSelection selection,
			boolean outlyingIntersections, BoundaryCategory category, String reason,
			boolean driverSlice, PersistentGeoId durableIdentity, String presentationName,
			boolean liveDefined, double[] liveState) {
		this.index = index;
		this.operation = operation;
		this.inputs = inputs.clone();
		this.outputIndex = outputIndex;
		this.selection = selection;
		this.outlyingIntersections = outlyingIntersections;
		this.category = category;
		this.reason = reason;
		this.driverSlice = driverSlice;
		this.durableIdentity = durableIdentity;
		this.presentationName = presentationName;
		this.liveDefined = liveDefined;
		this.liveState = liveState.clone();
	}

	/** @return the position of the node in the cone; inputs always come first */
	public int getIndex() {
		return index;
	}

	/** @return the operation of the node */
	public ConstructiveOperation getOperation() {
		return operation;
	}

	/** @return the number of inputs */
	public int getInputCount() {
		return inputs.length;
	}

	/**
	 * @param position structural position
	 * @return the index of the input node at that position
	 */
	public int getInput(int position) {
		return inputs[position];
	}

	/** @return the output index of a line-circle output, otherwise {@code -1} */
	public int getOutputIndex() {
		return outputIndex;
	}

	/**
	 * @return the inherited selection state of a line-circle output, or {@code null} when it
	 *         is not reconstructible
	 */
	public InheritedLineCircleSelection getSelection() {
		return selection;
	}

	/** @return whether a segment or ray admits intersections outside its bounded part */
	public boolean hasOutlyingIntersections() {
		return outlyingIntersections;
	}

	/** @return the boundary category of a boundary or outside node, otherwise {@code null} */
	public BoundaryCategory getCategory() {
		return category;
	}

	/** @return why the node is boundary or outside, otherwise {@code null} */
	public String getReason() {
		return reason;
	}

	/**
	 * @return whether the node depends on a substituted driver: its value is that of the
	 *         object with the driver replaced, and its captured state is not consulted
	 */
	public boolean isDriverSlice() {
		return driverSlice;
	}

	/** @return the durable identity of the object, or {@code null} when it has none */
	public PersistentGeoId getDurableIdentity() {
		return durableIdentity;
	}

	/** @return the label of the object for presentation, or {@code null}; never an identity */
	public String getPresentationName() {
		return presentationName;
	}

	/** @return whether the object was defined when the cone was captured */
	public boolean isLiveDefined() {
		return liveDefined;
	}

	/**
	 * @return the captured binary64 state: homogeneous coordinates of a point, the value of a
	 *         number, the coefficients of a line, the flat matrix of a circle, empty otherwise
	 */
	public double[] getLiveState() {
		return liveState.clone();
	}

	/**
	 * @param position index in the captured state
	 * @return one value of the captured state
	 */
	public double getLiveState(int position) {
		return liveState[position];
	}

	/**
	 * The structural definition of the node: operation, input nodes by structural position,
	 * output role, inherited selection state and classification. It never contains a label.
	 *
	 * @return the definition
	 */
	public String describe() {
		StringBuilder text = new StringBuilder(operation.name());
		text.append('(');
		for (int position = 0; position < inputs.length; position++) {
			text.append(position == 0 ? "" : ",").append('n').append(inputs[position]);
		}
		text.append(')');
		if (outputIndex >= 0) {
			text.append('#').append(outputIndex).append('[')
					.append(selection == null ? InheritedLineCircleSelection.NOT_RECONSTRUCTED
							: selection.describe())
					.append(']');
		}
		if (outlyingIntersections) {
			text.append("[outlying]");
		}
		if (category != null) {
			text.append('{').append(category.name()).append(':').append(reason).append('}');
		}
		if (driverSlice) {
			text.append("[slice]");
		}
		return text.toString();
	}

	@Override
	public String toString() {
		return "n" + index + "=" + describe()
				+ (presentationName == null ? "" : " \"" + presentationName + "\"")
				+ (liveDefined ? " " + Arrays.toString(liveState) : " undefined");
	}
}
