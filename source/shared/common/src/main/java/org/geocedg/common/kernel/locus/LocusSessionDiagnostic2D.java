/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Typed diagnostic emitted by one disposable semantic evaluation session. */
public final class LocusSessionDiagnostic2D {
	/** Session-level failures that remain separate from evaluation status. */
	public enum Kind {
		NONE,
		CYCLE_REENTRY,
		INCOHERENT_REVISION,
		CLOSED_SESSION
	}

	private static final LocusSessionDiagnostic2D NONE =
			new LocusSessionDiagnostic2D(Kind.NONE, "", Collections.emptyList());

	private final Kind kind;
	private final String message;
	private final List<LocusSemanticKey2D> activePath;

	/** Creates immutable diagnostic evidence. */
	public LocusSessionDiagnostic2D(Kind kind, String message,
			List<LocusSemanticKey2D> activePath) {
		this.kind = Objects.requireNonNull(kind);
		this.message = Objects.requireNonNull(message);
		Objects.requireNonNull(activePath);
		ArrayList<LocusSemanticKey2D> copy = new ArrayList<>();
		for (LocusSemanticKey2D key : activePath) {
			copy.add(Objects.requireNonNull(key));
		}
		this.activePath = Collections.unmodifiableList(copy);
	}

	/** @return the absence of a session-level failure */
	public static LocusSessionDiagnostic2D none() {
		return NONE;
	}

	public Kind getKind() {
		return kind;
	}

	public String getMessage() {
		return message;
	}

	public List<LocusSemanticKey2D> getActivePath() {
		return activePath;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LocusSessionDiagnostic2D)) {
			return false;
		}
		LocusSessionDiagnostic2D diagnostic = (LocusSessionDiagnostic2D) other;
		return kind == diagnostic.kind && message.equals(diagnostic.message)
				&& activePath.equals(diagnostic.activePath);
	}

	@Override
	public int hashCode() {
		return Objects.hash(kind, message, activePath);
	}

	@Override
	public String toString() {
		return kind + (message.isEmpty() ? "" : ": " + message);
	}
}
