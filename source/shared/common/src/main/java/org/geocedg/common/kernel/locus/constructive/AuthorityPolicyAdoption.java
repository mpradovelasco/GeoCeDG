/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.geocedg.common.kernel.spatial.identity.DocumentAuthorityRecord;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.MacroConstruction;
import org.geogebra.common.main.App;

/**
 * The explicit semantic event {@code AUTHORITY_POLICY_ADOPTION} of a document
 * ({@code locus-v2-constructive-authority.md} section 4, ADR 0040 decision 2). It is the only way
 * a document, new or existing, moves from {@code legacy-representation-authority/v1} to
 * {@code constructive-authority/v1}: it reports its impact before it is applied, applies the
 * policy in one operation with one undo point, changes no identity, token or selector, and has no
 * automatic way back; returning to the earlier policy is Undo.
 */
public final class AuthorityPolicyAdoption {

	/** Name of the semantic event. */
	public static final String EVENT = "AUTHORITY_POLICY_ADOPTION";
	/** Typed cause of a construct that requires the constructive policy in a legacy document. */
	public static final String REQUIRED_CAUSE = "CONSTRUCTIVE_AUTHORITY_REQUIRED";

	private AuthorityPolicyAdoption() {
	}

	/**
	 * Computes the impact report without applying anything.
	 *
	 * @param construction a document construction governed by the legacy policy
	 * @return the relations whose integrity or admission would change
	 * @throws IllegalStateException if the document already has the constructive policy
	 */
	public static AuthorityAdoptionImpact report(Construction construction) {
		requireLegacyDocument(construction);
		List<AuthorityAdoptionImpact.Entry> entries = new ArrayList<>();
		int examined = 0;
		for (AdoptionImpactContributor contributor : AdoptionImpactContributor.registered()) {
			examined += contributor.contribute(construction, entries);
		}
		return new AuthorityAdoptionImpact(entries, examined);
	}

	/**
	 * Applies the adoption: the report is computed first; then, in one atomic construction
	 * mutation, the policy is set and the construction is updated under it; finally one undo
	 * point is stored. A failure restores the document exactly as it was before the event.
	 *
	 * @param construction a document construction governed by the legacy policy
	 * @return the impact report computed before the adoption
	 */
	public static AuthorityAdoptionImpact adopt(Construction construction) {
		AuthorityAdoptionImpact impact = adoptWithinTransaction(construction);
		App app = construction.getApplication();
		if (app != null) {
			app.storeUndoInfo();
		}
		return impact;
	}

	/**
	 * Applies the adoption as {@link #adopt(Construction)} does, but inside an explicit
	 * transaction of the caller, which stores the one undo point of the whole transaction
	 * afterwards; Undo then returns to the earlier policy together with the rest of the
	 * transaction. No second undo point is stored, so two asynchronous history stores never
	 * race.
	 *
	 * @param construction a document construction governed by the legacy policy
	 * @return the impact report computed before the adoption
	 */
	public static AuthorityAdoptionImpact adoptWithinTransaction(Construction construction) {
		AuthorityAdoptionImpact impact = report(construction);
		construction.runAtomicConstructionMutation(() -> {
			construction.getSpatialIdentityRegistry()
					.adoptDocumentAuthority(DocumentAuthorityRecord.CONSTRUCTIVE_V1);
			construction.updateConstruction(false);
		});
		return impact;
	}

	/**
	 * @param construction a construction
	 * @return whether its document is governed by the constructive authority
	 */
	public static boolean isConstructive(Construction construction) {
		return AuthorityPolicy.of(construction).isConstructive();
	}

	private static void requireLegacyDocument(Construction construction) {
		Objects.requireNonNull(construction);
		if (construction instanceof MacroConstruction) {
			throw new IllegalStateException("A macro construction has no authority policy");
		}
		if (isConstructive(construction)) {
			throw new IllegalStateException("The document is already governed by "
					+ AuthorityPolicy.CONSTRUCTIVE_AUTHORITY);
		}
	}

	/**
	 * A part of the kernel whose relations change with the policy contributes its entries to the
	 * report. The contributors are fixed in code, in a deterministic order.
	 */
	interface AdoptionImpactContributor {
		/**
		 * @param construction the document construction
		 * @param entries receives the changed relations, in construction order
		 * @return the number of relations examined
		 */
		int contribute(Construction construction, List<AuthorityAdoptionImpact.Entry> entries);

		/** @return the contributors in their fixed order */
		static List<AdoptionImpactContributor> registered() {
			List<AdoptionImpactContributor> contributors = new ArrayList<>();
			contributors.add(new IntersectionAdmissionImpact());
			return contributors;
		}
	}
}
