/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The impact report of the explicit adoption event {@code AUTHORITY_POLICY_ADOPTION}
 * ({@code locus-v2-constructive-authority.md} sections 4 and 7.4): the relations whose integrity
 * or admission changes when the document moves from the legacy policy to the constructive one,
 * computed before anything is applied. An empty document gives an empty report; the event is
 * explicit all the same. The report never changes an identity, a token or a selector.
 */
public final class AuthorityAdoptionImpact {

	/** One relation whose integrity or admission changes. */
	public static final class Entry {
		private final String subject;
		private final String relation;
		private final String before;
		private final String after;
		private final String cause;

		/**
		 * @param subject presentation name of the object, never its identity
		 * @param relation stable description of the relation
		 * @param before integrity or admission under the legacy policy
		 * @param after integrity or admission under the constructive policy
		 * @param cause typed cause of the change, or {@code null}
		 */
		public Entry(String subject, String relation, String before, String after,
				String cause) {
			this.subject = Objects.requireNonNull(subject);
			this.relation = Objects.requireNonNull(relation);
			this.before = Objects.requireNonNull(before);
			this.after = Objects.requireNonNull(after);
			this.cause = cause;
		}

		/** @return the presentation name of the object */
		public String getSubject() {
			return subject;
		}

		/** @return the stable description of the relation */
		public String getRelation() {
			return relation;
		}

		/** @return the integrity or admission under the legacy policy */
		public String getBefore() {
			return before;
		}

		/** @return the integrity or admission under the constructive policy */
		public String getAfter() {
			return after;
		}

		/** @return the typed cause of the change, or {@code null} */
		public String getCause() {
			return cause;
		}

		@Override
		public String toString() {
			return subject + ": " + relation + " " + before + " -> " + after
					+ (cause == null ? "" : " (" + cause + ")");
		}
	}

	private final List<Entry> entries;
	private final int examinedRelations;

	/**
	 * @param entries relations whose integrity or admission changes, in construction order
	 * @param examinedRelations number of relations examined
	 */
	public AuthorityAdoptionImpact(List<Entry> entries, int examinedRelations) {
		this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
		this.examinedRelations = examinedRelations;
	}

	/** @return the relations whose integrity or admission changes */
	public List<Entry> getEntries() {
		return entries;
	}

	/** @return the number of relations examined */
	public int getExaminedRelations() {
		return examinedRelations;
	}

	/** @return whether nothing changes */
	public boolean isEmpty() {
		return entries.isEmpty();
	}

	@Override
	public String toString() {
		return AuthorityPolicyAdoption.EVENT + ": " + entries.size() + " change(s) in "
				+ examinedRelations + " examined relation(s)";
	}
}
