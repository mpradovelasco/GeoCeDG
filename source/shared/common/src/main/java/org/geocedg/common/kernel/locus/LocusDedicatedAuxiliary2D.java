/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.spatial.identity.ConstructionGeoRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.arithmetic.ValidExpression;
import org.geogebra.common.kernel.arithmetic.variable.Variable;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;

/**
 * Ownership of the dedicated auxiliary inputs of a semantic point on a Locus V2
 * ({@code locus-v2-point-interaction.md} section 12): the selector text of
 * {@code Point(L, u)} and the address text and number of an interaction-owned point are
 * dedicated, independent, exclusive parents of that point, and its persisted identity role
 * depends on that exclusivity. An ordinary text or number that a user passes to
 * {@code Point(L, "branch", u)} has no owner and stays shareable.
 *
 * <p>Post-R1 repair F7: a second consumer of a dedicated auxiliary, or its redefinition, is
 * refused before any mutation, with a typed cause. The auxiliary is never copied on behalf
 * of the new consumer: a copy would replace a requested dependency by a frozen value.</p>
 */
public final class LocusDedicatedAuxiliary2D {
	/** Localization key of the refusal. */
	public static final String MESSAGE_KEY = "LocusV2.DedicatedAuxiliary";

	private LocusDedicatedAuxiliary2D() {
		// Utility class.
	}

	/** Typed refusal of a foreign use of a dedicated auxiliary input. */
	public static final class OwnershipException extends IllegalArgumentException {
		private static final long serialVersionUID = 1L;
		private final String auxiliaryLabel;
		private final String ownerLabel;

		private OwnershipException(GeoElement auxiliary, GeoPoint owner) {
			super("DEDICATED_AUXILIARY_OWNED: " + auxiliary.getLabelSimple()
					+ " is the dedicated input of the Locus V2 point " + owner.getLabelSimple());
			auxiliaryLabel = auxiliary.getLabelSimple();
			ownerLabel = owner.getLabelSimple();
		}

		/** @return label of the dedicated auxiliary */
		public String getAuxiliaryLabel() {
			return auxiliaryLabel;
		}

		/** @return label of the point that owns it */
		public String getOwnerLabel() {
			return ownerLabel;
		}
	}

	/**
	 * The owner is decided by the persisted identity role of the point and by the place of
	 * the input in its parent, never by the content of a text: a text that merely looks like
	 * a selector codec has no owner. The current number of consumers is not consulted, so a
	 * document whose exclusivity is already broken still reports the owner.
	 *
	 * @param input a construction element
	 * @return the semantic point whose identity role makes the element its dedicated
	 *         auxiliary input, or {@code null} for an ordinary, shareable element
	 */
	public static GeoPoint ownerOf(GeoElement input) {
		if (input == null || !input.isIndependent() || input.getAlgorithmList() == null) {
			return null;
		}
		Construction construction = input.getConstruction();
		SpatialIdentityRegistry registry = construction.getSpatialIdentityRegistry();
		for (AlgoElement consumer : input.getAlgorithmList()) {
			if (!(consumer instanceof AlgoSemanticLocusPoint2D)) {
				continue;
			}
			AlgoSemanticLocusPoint2D parent = (AlgoSemanticLocusPoint2D) consumer;
			GeoPoint point = parent.getPoint();
			PersistentGeoId id = registry.getPersistentGeoId(point);
			GeoIdentityRecord record = id == null ? null : registry.getGeoRecord(id);
			if (record == null) {
				continue;
			}
			String role = record.getStableOutputRole();
			boolean branch = parent.getBranchInput() == input;
			boolean parameter = parent.getParameterInput().toGeoElement() == input;
			if (ConstructionGeoRedefineProvider.PRINCIPAL_BRANCH_POINT_OUTPUT_ROLE.equals(role)
					&& branch
					|| ConstructionGeoRedefineProvider.INTERACTION_POINT_OUTPUT_ROLE
							.equals(role) && (branch || parameter)) {
				return point;
			}
		}
		return null;
	}

	/**
	 * @param input an input that a new consumer is about to read
	 * @throws OwnershipException when the input is the dedicated auxiliary of a point
	 */
	public static void requireShareable(GeoElement input) {
		GeoPoint owner = ownerOf(input);
		if (owner != null) {
			throw new OwnershipException(input, owner);
		}
	}

	/**
	 * Admission of an expression entered outside a file read: it may not read or redefine a
	 * dedicated auxiliary, except while it redefines the owner of that auxiliary, whose
	 * definition names it. Reading a document, an undo step or a copied closure rebuilds the
	 * owners themselves and is not checked here; the identity validation of the document
	 * diagnoses a record that is already inconsistent.
	 *
	 * @param kernel the kernel that resolves the labels
	 * @param expression the parsed, unresolved expression
	 * @param redefined the element that the expression redefines, or {@code null}
	 * @throws OwnershipException before any element or algorithm is created
	 */
	public static void requireAdmissibleExpression(Kernel kernel, ValidExpression expression,
			GeoElement redefined) {
		Construction construction = kernel.getConstruction();
		if (!(kernel.getApplication().getConfig() instanceof AppConfigGeoCeDG)
				|| kernel.isMacroKernel() || construction.isFileLoading()) {
			return;
		}
		requireShareable(redefined);
		OwnershipException[] refusal = new OwnershipException[1];
		expression.any(part -> {
			GeoElement referenced = null;
			if (part instanceof Variable) {
				referenced = kernel.lookupLabel(((Variable) part).getName());
			} else if (part instanceof GeoElement) {
				referenced = (GeoElement) part;
			}
			GeoPoint owner = ownerOf(referenced);
			if (owner != null && owner != redefined) {
				refusal[0] = new OwnershipException(referenced, owner);
				return true;
			}
			return false;
		});
		if (refusal[0] != null) {
			throw refusal[0];
		}
	}
}
