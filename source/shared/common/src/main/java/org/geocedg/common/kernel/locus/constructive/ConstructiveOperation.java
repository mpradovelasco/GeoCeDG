/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

/**
 * The operation of one node of a constructive cone under the class table
 * {@code constructive-class/v1} ({@code locus-v2-constructive-authority.md} section 3.2).
 * Membership in the class is decided by the class of the algorithm that defines an object, never
 * by its value. The structural positions of the inputs of each operation are fixed here.
 */
public enum ConstructiveOperation {
	/** An independent point; its binary64 value is its definition. No input. */
	FREE_POINT(Shape.POINT, Membership.FREE_INPUT),
	/** An independent number or angle; its binary64 value is its definition. No input. */
	FREE_NUMBER(Shape.NUMBER, Membership.FREE_INPUT),
	/** A coordinate axis, a constant line. No input. */
	AXIS(Shape.LINE, Membership.FREE_INPUT),
	/**
	 * A point on a path: the point of the ideal path that the inherited path map assigns to the
	 * persisted coordinates of the point. Input: the path, a line, segment, ray or circle of the
	 * class.
	 */
	PATH_POINT(Shape.POINT, Membership.PATH_POINT),
	/** Line through two points. Inputs: the two points. */
	JOIN(Shape.LINE, Membership.IN_CLASS),
	/** Segment between two points. Inputs: start and end point. */
	SEGMENT(Shape.LINE, Membership.IN_CLASS),
	/** Ray from a point through another. Inputs: start point and point on the ray. */
	RAY(Shape.LINE, Membership.IN_CLASS),
	/** Line through a point parallel to a line. Inputs: the point and the line. */
	PARALLEL(Shape.LINE, Membership.IN_CLASS),
	/** Line through a point perpendicular to a line. Inputs: the point and the line. */
	PERPENDICULAR(Shape.LINE, Membership.IN_CLASS),
	/** Intersection of two lines. Inputs: the two lines, in the order of the algorithm. */
	MEET(Shape.POINT, Membership.IN_CLASS),
	/**
	 * One output of a line-circle intersection with its inherited selection state. Inputs: the
	 * line and the circle.
	 */
	LINE_CIRCLE_OUTPUT(Shape.POINT, Membership.IN_CLASS),
	/** Midpoint of two points. Inputs: the two points. */
	MIDPOINT(Shape.POINT, Membership.IN_CLASS),
	/** Circle by centre and point. Inputs: the centre and the point. */
	CIRCLE_THROUGH(Shape.CIRCLE, Membership.IN_CLASS),
	/** Circle by centre and radius. Inputs: the centre and the radius, a number or a segment. */
	CIRCLE_RADIUS(Shape.CIRCLE, Membership.IN_CLASS),
	/**
	 * Declared boundary: rotation of a point by an angle, about a point or the origin. Its
	 * ideal value has a rigorous enclosure; an exact evaluation takes its binary64 state as a
	 * substitute. Inputs: the point, the angle and, when present, the centre.
	 */
	ROTATE_POINT(Shape.POINT, Membership.BOUNDARY),
	/** Declared boundary point, a substitute in an exact evaluation. No input. */
	BOUNDARY_POINT(Shape.POINT, Membership.BOUNDARY),
	/** Declared boundary number, a substitute in an exact evaluation. No input. */
	BOUNDARY_NUMBER(Shape.NUMBER, Membership.BOUNDARY),
	/**
	 * An object outside the class that is neither a point nor a number; its relations stay
	 * {@code NOT_ESTABLISHED}. No input.
	 */
	OUTSIDE(Shape.OTHER, Membership.OUTSIDE);

	/** The geometric kind of the value of a node. */
	public enum Shape {
		/** A point of the plane. */
		POINT,
		/** A real number. */
		NUMBER,
		/** A line, segment or ray. */
		LINE,
		/** A circle. */
		CIRCLE,
		/** Any other object. */
		OTHER
	}

	/** How a node enters the constructive meaning. */
	public enum Membership {
		/** A free input or an axis: its binary64 value is its definition. */
		FREE_INPUT,
		/** A point on a path of the class. */
		PATH_POINT,
		/** An algorithm of the class with exact semantics. */
		IN_CLASS,
		/** A declared boundary object: a substitute in an exact evaluation, a certificate cut. */
		BOUNDARY,
		/** Outside the class without a substitute. */
		OUTSIDE
	}

	private final Shape shape;
	private final Membership membership;

	ConstructiveOperation(Shape shape, Membership membership) {
		this.shape = shape;
		this.membership = membership;
	}

	/** @return the geometric kind of the value */
	public Shape getShape() {
		return shape;
	}

	/** @return how the node enters the constructive meaning */
	public Membership getMembership() {
		return membership;
	}

	/** @return whether the operation is a line of the class or an axis, usable as a line path */
	public boolean isClassLine() {
		return shape == Shape.LINE && (membership == Membership.IN_CLASS || this == AXIS);
	}

	/** @return whether the operation is a circle of the class */
	public boolean isClassCircle() {
		return shape == Shape.CIRCLE && membership == Membership.IN_CLASS;
	}

	/** @return whether a line of this operation is bounded at its start, as a segment or a ray */
	public boolean isBounded() {
		return this == SEGMENT || this == RAY;
	}
}
