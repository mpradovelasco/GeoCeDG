/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.Collection;
import java.util.regex.Pattern;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.geocedg.common.kernel.dimension.AlgoNativeDimension;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.ExpressionValue;
import org.geogebra.common.kernel.arithmetic.MyDouble;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.plugin.Operation;
import org.geogebra.desktop.main.AppD;

/**
 * POST-E2-P4: the explicit "Make offset draggable" action of a native dimension. A
 * dimension typed with a literal offset, {@code AlignedDimension(A, B, 1)}, becomes the
 * equivalent of {@code offset1 = 1; AlignedDimension(A, B, offset1)}: the literal number
 * the algorithm already holds receives a free label and moves in the construction order
 * to just before the dimension. No object is replaced or recreated, no dependency edge
 * changes and the command signature is unchanged; afterwards the existing offset drag
 * (native-dimensions section 8.4) applies. One undo point; literal arguments are never
 * changed implicitly while dragging.
 */
final class GeoCeDGDimensionOffsetConversion {

	/** Prefix of the collision-safe numbered label of the new offset number. */
	static final String LABEL_PREFIX = "offset";

	/** A plain signed decimal literal as the kernel prints it. */
	private static final Pattern DECIMAL_LITERAL = Pattern
			.compile("[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?");

	/** Whether and why a dimension can be converted. */
	enum Eligibility {
		/** literal offset; the action converts it */
		ELIGIBLE(null),
		/** the offset is already a free, labelled, unlocked number */
		ALREADY_DRAGGABLE("Dimensions.Offset.AlreadyDraggable"),
		/** the offset is a named object that is not a free unlocked number */
		NAMED_OFFSET("Dimensions.Offset.Named"),
		/** the offset is an expression, a constant or a computed value */
		EXPRESSION("Dimensions.Offset.Expression"),
		/** the dimension is undefined */
		UNDEFINED("Dimensions.Offset.Undefined"),
		/** not a dimension of the open construction at its last step */
		UNAVAILABLE("Dimensions.Offset.Unavailable");

		private final String reasonKey;

		Eligibility(String reasonKey) {
			this.reasonKey = reasonKey;
		}

		/**
		 * @return profile text key explaining why the action is unavailable, or null
		 */
		String getReasonKey() {
			return reasonKey;
		}
	}

	private final AppD app;
	private final Runnable afterLabel;

	GeoCeDGDimensionOffsetConversion(AppD app) {
		this(app, () -> {
			// production: no intermediate step
		});
	}

	/**
	 * @param app application
	 * @param afterLabel test seam run between labelling and ordering
	 */
	GeoCeDGDimensionOffsetConversion(AppD app, Runnable afterLabel) {
		this.app = app;
		this.afterLabel = afterLabel;
	}

	/**
	 * The semantic dimension of a context: every element must be an output of the same
	 * native dimension algorithm. Proximity, labels and positions play no part.
	 *
	 * @param geos context elements
	 * @return their common dimension, or null
	 */
	static AlgoNativeDimension contextDimension(Collection<GeoElement> geos) {
		if (geos == null || geos.isEmpty()) {
			return null;
		}
		AlgoNativeDimension owner = null;
		for (GeoElement geo : geos) {
			AlgoNativeDimension candidate = AlgoNativeDimension.ownerOf(geo);
			if (candidate == null || (owner != null && candidate != owner)) {
				return null;
			}
			owner = candidate;
		}
		return owner;
	}

	/**
	 * @param owner dimension algorithm
	 * @return whether and why the dimension can be converted
	 */
	Eligibility eligibility(AlgoNativeDimension owner) {
		Construction cons = app.getKernel().getConstruction();
		if (owner == null || owner.getConstruction() != cons
				|| !owner.isInConstructionList()
				|| owner.getConstructionIndex() > cons.getStep()
				|| cons.getStep() != cons.steps() - 1) {
			return Eligibility.UNAVAILABLE;
		}
		GeoElement offset = owner.getOffsetInput().toGeoElement();
		if (offset.isLabelSet()) {
			return GeoCeDGEuclidianController.isDraggableOffset(owner)
					? Eligibility.ALREADY_DRAGGABLE : Eligibility.NAMED_OFFSET;
		}
		if (offset.getClass() != GeoNumeric.class || !offset.isIndependent()
				|| offset.isLocked() || offset.getAlgorithmList().size() != 1
				|| !isLiteral((GeoNumeric) offset)) {
			return Eligibility.EXPRESSION;
		}
		if (!owner.getValue().isDefined()) {
			return Eligibility.UNDEFINED;
		}
		return Eligibility.ELIGIBLE;
	}

	private static boolean isLiteral(GeoNumeric number) {
		if (!Double.isFinite(number.getDouble())) {
			return false;
		}
		ExpressionNode definition = number.getDefinition();
		if (definition == null) {
			return true;
		}
		ExpressionValue leaf = definition.unwrap();
		return definition.getOperation() == Operation.NO_OPERATION
				&& leaf instanceof MyDouble
				&& DECIMAL_LITERAL.matcher(leaf.toString(StringTemplate.xmlTemplate))
						.matches();
	}

	/**
	 * Converts an eligible dimension. On any failure the literal number gets back its
	 * previous state and position, so nothing is left half converted or orphaned.
	 *
	 * @param owner dimension algorithm
	 * @return the labelled offset number, or null when the dimension is not eligible
	 */
	GeoNumeric convert(AlgoNativeDimension owner) {
		if (eligibility(owner) != Eligibility.ELIGIBLE) {
			return null;
		}
		Construction cons = owner.getConstruction();
		GeoNumeric number = (GeoNumeric) owner.getOffsetInput().toGeoElement();
		int index = owner.getConstructionIndex();
		boolean auxiliary = number.isAuxiliaryObject();
		boolean visible = number.isEuclidianVisible();
		String label = cons.getLabelManager().getNextNumberedLabel(LABEL_PREFIX);
		try {
			// as the tool-created offset (native-dimensions section 8.2)
			number.setAuxiliaryObject(true);
			number.setEuclidianVisible(false);
			number.setLabel(label);
			afterLabel.run();
			if (!label.equals(number.getLabelSimple())
					|| !cons.moveInConstructionList(number, index)
					|| number.getConstructionIndex() != owner.getConstructionIndex() - 1
					|| owner.getOffsetInput() != number
					|| !GeoCeDGEuclidianController.isDraggableOffset(owner)) {
				throw new IllegalStateException("offset conversion incomplete");
			}
		} catch (RuntimeException failure) {
			restore(cons, number, auxiliary, visible);
			throw failure;
		}
		number.updateRepaint();
		return number;
	}

	private static void restore(Construction cons, GeoNumeric number, boolean auxiliary,
			boolean visible) {
		if (number.isLabelSet()) {
			cons.removeFromConstructionList(number);
			cons.removeLabel(number);
			number.notifyRemove();
			number.setLabelSet(false);
			number.setLabelSimple(null);
		}
		number.setAuxiliaryObject(auxiliary);
		number.setEuclidianVisible(visible);
	}

	/**
	 * @param geos context elements
	 * @return the context menu item for their dimension, or null when the context is
	 *         not one native dimension
	 */
	JMenuItem menuItem(Collection<GeoElement> geos) {
		AlgoNativeDimension owner = contextDimension(geos);
		if (owner == null) {
			return null;
		}
		JMenuItem item = new JMenuItem(text("Dimensions.Offset.MakeDraggable"));
		Eligibility eligibility = eligibility(owner);
		if (eligibility == Eligibility.ELIGIBLE) {
			item.setToolTipText(text("Dimensions.Offset.MakeDraggableTip"));
			item.addActionListener(event -> run(owner));
		} else {
			item.setEnabled(false);
			item.setToolTipText(text(eligibility.getReasonKey()));
		}
		return item;
	}

	/**
	 * Runs the action as one user-visible transaction with one undo point.
	 *
	 * @param owner dimension algorithm
	 * @return whether the dimension was converted
	 */
	boolean run(AlgoNativeDimension owner) {
		Eligibility eligibility = eligibility(owner);
		if (eligibility != Eligibility.ELIGIBLE) {
			warn(text(eligibility.getReasonKey()));
			return false;
		}
		try {
			convert(owner);
		} catch (RuntimeException failure) {
			warn(text("Dimensions.Offset.Failed"));
			return false;
		}
		app.storeUndoInfo();
		return true;
	}

	private void warn(String message) {
		JOptionPane.showMessageDialog(app.getMainComponent(), message,
				text("Dimensions.Offset.MakeDraggable"), JOptionPane.WARNING_MESSAGE);
	}

	private String text(String key) {
		return GeoCeDGProfile.getText(key, app.getLocale().getLanguage());
	}
}
