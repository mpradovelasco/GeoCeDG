/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;

/**
 * PRE-G9B-CU3 characterization of T7-a (author authorization of 2026-10-26), for the shadow
 * copy of {@code RootCountCertificate2D} that {@code run-cu3-t7a-characterization.ps1}
 * generates from the product source: it is never part of a source set and never ships.
 *
 * <p>The shadow routes every {@code certify} call here. In the mode {@code off} the call runs
 * the procedure of the product unchanged. In the mode {@code compare} it runs the procedure of
 * the product, whose certificate it returns, and then the hypothetical T7-a procedure on the same
 * source and box, and records both. In the mode {@code t7a} the T7-a procedure is the one whose
 * certificate is returned, so that the published results and the times of T7-a can be observed;
 * nothing of it enters the product. T7-a differs from the product in one step only: in the
 * analysis of a certified critical point, the exact decision of the configuration is consulted
 * before the bisection of the critical value, and an exact double root whose parameter lies in
 * the bracket ends the search there.</p>
 */
public final class PreG9BCu3T7aLedger {

	/** {@code off}, {@code compare} or {@code t7a}. */
	static final String MODE = System.getProperty("geocedg.t7a.mode", "off");

	private static final ThreadLocal<Run> ACTIVE = new ThreadLocal<>();
	private static final ThreadLocal<Boolean> ANTICIPATE = ThreadLocal.withInitial(
			() -> "t7a".equals(MODE));
	private PreG9BCu3T7aLedger() {
	}

	/** The facts of one procedure on one box. */
	private static final class Run {
		private String critical = "NOT_REACHED";
		private int criticalEvaluations;
		private double[] bracket;
		private String anticipation = "NOT_CONSULTED";
		private long anticipationNanos;
		private long exactNanos;
		private int exactConsultations;
		private long nanos;
		private RootCountCertificate2D result;
	}

	/** @return whether the running procedure is T7-a */
	static boolean anticipate() {
		return ANTICIPATE.get();
	}

	/**
	 * The certificate of the box, as the product computes it, and in the mode {@code compare}
	 * also the T7-a certificate, both recorded; in the mode {@code t7a} the T7-a certificate.
	 */
	static RootCountCertificate2D certify(ResidualJetSource2D source, double lower,
			double upper) {
		if ("off".equals(MODE)) {
			return RootCountCertificate2D.certifyWith(source, lower, upper);
		}
		boolean t7a = "t7a".equals(MODE);
		Run first = new Run();
		Run second = null;
		try {
			ANTICIPATE.set(t7a);
			first.result = run(first, source, lower, upper);
			if (!t7a) {
				second = new Run();
				ANTICIPATE.set(true);
				second.result = run(second, source, lower, upper);
			}
		} finally {
			ANTICIPATE.set(t7a);
			ACTIVE.remove();
		}
		record(source, lower, upper, t7a ? null : first, t7a ? first : second);
		return first.result;
	}

	private static RootCountCertificate2D run(Run path, ResidualJetSource2D source,
			double lower, double upper) {
		ACTIVE.set(path);
		long begin = System.nanoTime();
		try {
			return RootCountCertificate2D.certifyWith(source, lower, upper);
		} finally {
			path.nanos = System.nanoTime() - begin;
		}
	}

	/** The outcome of the bisection of the critical value, in the running procedure. */
	static void critical(double[] bracket, String outcome, int evaluations) {
		Run path = ACTIVE.get();
		if (path != null) {
			path.critical = outcome;
			path.criticalEvaluations = evaluations;
			path.bracket = bracket.clone();
		}
	}

	/** The consultation of the exact decision before the bisection, in T7-a. */
	static void anticipation(double[] bracket, RootCountCertificate2D early, long nanos) {
		Run path = ACTIVE.get();
		if (path != null) {
			path.anticipation = early == null ? "NO_EXACT_CONTACT_IN_BRACKET"
					: early.getForm().name();
			path.anticipationNanos = nanos;
			path.bracket = bracket.clone();
			if (early != null) {
				path.critical = "ANTICIPATED";
			}
		}
	}

	/** A consultation of the exact decision after the bisection, as the product does. */
	static void exactConsulted(long nanos) {
		Run path = ACTIVE.get();
		if (path != null) {
			path.exactNanos += nanos;
			path.exactConsultations++;
		}
	}

	private static void record(ResidualJetSource2D source, double lower, double upper,
			Run current, Run anticipated) {
		StringBuilder line = new StringBuilder("{");
		field(line, "context", context()).append(',');
		field(line, "configuration", source.getConfiguration()).append(',');
		line.append("\"constructive\":").append(source.isConstructive()).append(',');
		line.append("\"box\":").append(interval(new double[] {lower, upper})).append(',');
		line.append("\"current\":").append(current == null ? "null" : path(current)).append(',');
		line.append("\"t7a\":").append(anticipated == null ? "null" : path(anticipated));
		if (current != null && anticipated != null) {
			line.append(',');
			line.append("\"same_decision\":")
					.append(signature(current.result).equals(signature(anticipated.result)));
		}
		append(line.append('}').toString());
	}

	/** @return everything a certificate decides, without its work count */
	private static String signature(RootCountCertificate2D certificate) {
		StringBuilder text = new StringBuilder(certificate.getForm().name()).append('|')
				.append(hex(certificate.getLower())).append(',').append(hex(certificate.getUpper()))
				.append('|');
		for (double[] piece : certificate.getPieces()) {
			text.append(hex(piece[0])).append(',').append(hex(piece[1])).append(';');
		}
		text.append('|');
		for (double[] root : certificate.getRoots()) {
			text.append(hex(root[0])).append(',').append(hex(root[1])).append(';');
		}
		text.append('|');
		if (certificate.getCritical() != null) {
			text.append(hex(certificate.getCritical()[0])).append(',')
					.append(hex(certificate.getCritical()[1]));
		}
		text.append('|').append(certificate.getContact()).append('|');
		ExactContactEvidence2D exact = certificate.getExact();
		if (exact != null) {
			text.append(exact.getParameter()).append(',').append(hex(exact.getLower())).append(',')
					.append(hex(exact.getUpper())).append(',').append(exact.getOrder()).append(',')
					.append(exact.getCurvature()).append(',').append(exact.getEvidence());
		}
		return text.append('|').append(certificate.getReason()).toString();
	}

	private static String path(Run path) {
		StringBuilder text = new StringBuilder("{");
		RootCountCertificate2D result = path.result;
		field(text, "form", result.getForm().name()).append(',');
		field(text, "contact", String.valueOf(result.getContact())).append(',');
		field(text, "reason", result.getReason()).append(',');
		text.append("\"evaluations\":").append(result.getEvaluations()).append(',');
		text.append("\"critical_bracket\":").append(result.getCritical() == null ? "null"
				: interval(result.getCritical())).append(',');
		text.append("\"roots\":").append(result.getRoots().size()).append(',');
		ExactContactEvidence2D exact = result.getExact();
		field(text, "exact", exact == null ? "none" : exact.getParameter() + " "
				+ exact.getOrder() + " curvature " + exact.getCurvature()).append(',');
		field(text, "critical_value", path.critical).append(',');
		text.append("\"critical_value_evaluations\":").append(path.criticalEvaluations)
				.append(',');
		text.append("\"bracket\":").append(path.bracket == null ? "null"
				: interval(path.bracket)).append(',');
		field(text, "anticipation", path.anticipation).append(',');
		text.append("\"anticipation_ms\":").append(ms(path.anticipationNanos)).append(',');
		text.append("\"exact_consultations_after_bisection\":").append(path.exactConsultations)
				.append(',');
		text.append("\"exact_ms_after_bisection\":").append(ms(path.exactNanos)).append(',');
		text.append("\"ms\":").append(ms(path.nanos)).append(',');
		field(text, "signature", signature(result));
		return text.append('}').toString();
	}

	private static String context() {
		StackTraceElement[] stack = new Throwable().getStackTrace();
		for (int index = stack.length - 1; index >= 0; index--) {
			String type = stack[index].getClassName();
			if ((type.endsWith("Test") || type.endsWith("Probe")) && !type.contains("$")) {
				String detail = System.getProperty("geocedg.t7a.context", "");
				return type.substring(type.lastIndexOf('.') + 1) + "." + stack[index]
						.getMethodName() + (detail.isEmpty() ? "" : " " + detail);
			}
		}
		return "unknown";
	}

	private static String interval(double[] value) {
		return String.format(Locale.ROOT, "[\"%s\",\"%s\",%s,%s]", hex(value[0]), hex(value[1]),
				Double.toString(value[0]), Double.toString(value[1]));
	}

	private static String hex(double value) {
		return Long.toHexString(Double.doubleToRawLongBits(value));
	}

	private static String ms(long nanos) {
		return String.format(Locale.ROOT, "%.4f", nanos / 1E6);
	}

	private static StringBuilder field(StringBuilder text, String name, String value) {
		return text.append('"').append(name).append("\":\"").append(value == null ? "null"
				: value.replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
	}

	/**
	 * Appends one record to {@code <geocedg.t7a.ledger>-<pid>.jsonl} at once: the test JVMs of the
	 * Desktop module may end without running shutdown hooks.
	 */
	private static synchronized void append(String line) {
		String base = System.getProperty("geocedg.t7a.ledger", "");
		if (base.isEmpty()) {
			return;
		}
		try {
			Path file = Path.of(base + "-" + ProcessHandle.current().pid() + ".jsonl");
			Files.createDirectories(file.getParent());
			Files.write(file, List.of(line), StandardCharsets.UTF_8, StandardOpenOption.CREATE,
					StandardOpenOption.APPEND);
		} catch (IOException failed) {
			System.err.println("T7-a ledger not written: " + failed);
		}
	}
}
