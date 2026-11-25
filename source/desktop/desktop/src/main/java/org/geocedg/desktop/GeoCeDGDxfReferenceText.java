/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.Map;

import org.geocedg.common.importer.dxf.DxfDiagnostic;
import org.geocedg.common.importer.dxf.DxfImportReport;
import org.geocedg.common.importer.dxf.DxfRejectedEntity;
import org.geocedg.common.importer.dxf.DxfSplineReference;
import org.geocedg.common.importer.dxf.DxfTessellation;
import org.geocedg.common.math.bspline.BSplineInteriorKnot;

/**
 * Deterministic EN/ES texts of the DXF import report (contract section 15) and of the
 * read-only reference inspector (section 10). Typed causes, natures and diagnostics are shown
 * by their contract identifiers; numbers are the exact binary64 values in shortest
 * round-trip form. Nothing here claims exactness with respect to the decimal text of the
 * file or of the render.
 */
final class GeoCeDGDxfReferenceText {

	private GeoCeDGDxfReferenceText() {
	}

	/**
	 * PRE-G9B-MAINT-A-DXF0-R1: recognizable name of a reference: its DXF layer and entity
	 * handle as read, which is how the drawing names it. Never an identity (contract section 9).
	 */
	static String displayName(DxfSplineReference record) {
		return record.layer() + " \u00b7 " + record.handle();
	}

	/** @return the localized geometric nature of a record (contract section 3) */
	static String natureLabel(AppGeoCeDG app, DxfSplineReference record) {
		return app.layerText("DxfReference.Nature." + record.nature().name());
	}

	/** @return one compact line of the session manager: name, file, nature, visibility */
	static String listLine(AppGeoCeDG app, GeoCeDGDxfReferenceSession.Entry entry) {
		DxfSplineReference record = entry.reference();
		return displayName(record) + " (" + record.file().fileBaseName() + ") \u2014 "
				+ natureLabel(app, record) + (entry.isVisible() ? ""
						: " (" + app.layerText("DxfReference.Hidden") + ")");
	}

	/**
	 * PRE-G9B-MAINT-A-DXF0-R1: the ordinary import report: file name, admitted references by
	 * name and nature, and brief notices. Hashes, versions, counts by type, transformation,
	 * structural data, rejection causes and diagnostics are in {@link #report}, shown on
	 * request.
	 */
	static String summary(AppGeoCeDG app, DxfImportReport report) {
		StringBuilder text = new StringBuilder();
		text.append(app.layerText("DxfReference.Report.FileName", report.file().fileBaseName()))
				.append('\n');
		if (report.fileCause() != null) {
			text.append(app.layerText("DxfReference.Report.FileRejected",
					report.fileCause().name())).append('\n');
		}
		text.append(app.layerText("DxfReference.Report.Admitted",
				Integer.toString(report.records().size()))).append('\n');
		int notices = report.diagnostics().size();
		for (DxfSplineReference record : report.records()) {
			text.append("  ").append(displayName(record)).append(" \u2014 ")
					.append(natureLabel(app, record)).append('\n');
			notices += record.diagnostics().size();
		}
		if (report.file().unitsDiffer()) {
			text.append(app.layerText("DxfReference.Transform.UnitsDiffer")).append('\n');
		}
		if (!report.rejected().isEmpty()) {
			text.append(app.layerText("DxfReference.Report.RejectedSummary",
					Integer.toString(report.rejected().size()))).append('\n');
		}
		if (notices > 0) {
			text.append(app.layerText("DxfReference.Report.NoticesSummary",
					Integer.toString(notices))).append('\n');
		}
		return text.toString().trim();
	}

	static String report(AppGeoCeDG app, DxfImportReport report) {
		StringBuilder text = new StringBuilder();
		text.append(app.layerText("DxfReference.Report.File", report.file().fileBaseName(),
				report.file().fileSha256().isEmpty() ? "-" : report.file().fileSha256()))
				.append('\n');
		text.append(app.layerText("DxfReference.Report.Version", report.file().acadVersion()))
				.append('\n');
		text.append(units(app, report)).append('\n');
		if (report.fileCause() != null) {
			text.append(app.layerText("DxfReference.Report.FileRejected",
					report.fileCause().name())).append('\n');
		}
		StringBuilder counts = new StringBuilder();
		for (Map.Entry<String, Integer> entry : report.entityCounts().entrySet()) {
			if (counts.length() > 0) {
				counts.append(", ");
			}
			counts.append(entry.getKey()).append(" ").append(entry.getValue());
		}
		text.append(app.layerText("DxfReference.Report.Counts",
				counts.length() == 0 ? "-" : counts.toString())).append('\n');
		text.append(app.layerText("DxfReference.Report.Transform",
				report.transform().toString())).append('\n');
		text.append(app.layerText("DxfReference.Report.Admitted",
				Integer.toString(report.records().size()))).append('\n');
		for (DxfSplineReference record : report.records()) {
			text.append("  #").append(record.ordinal()).append(' ').append(record.handle())
					.append(" [").append(record.layer()).append("] ")
					.append(record.nature().name()).append(", p=")
					.append(record.data().degree()).append(", n+1=")
					.append(record.data().controlPointCount()).append(record.isExactlyClosed()
							? ", EXACTLY_CLOSED" : ", OPEN").append(diagnostics(record))
					.append('\n');
		}
		text.append(app.layerText("DxfReference.Report.Rejected",
				Integer.toString(report.rejected().size()))).append('\n');
		for (DxfRejectedEntity rejected : report.rejected()) {
			text.append("  ").append(rejected.section()).append(" #")
					.append(rejected.ordinal()).append(' ').append(rejected.handle())
					.append(" [").append(rejected.layer()).append("] ")
					.append(rejected.cause().name()).append('\n');
		}
		for (DxfDiagnostic diagnostic : report.diagnostics()) {
			text.append(app.layerText("DxfReference.Report.Diagnostic", diagnostic.name()))
					.append('\n');
		}
		text.append(app.layerText("DxfReference.Report.NoClaim"));
		return text.toString();
	}

	private static String units(AppGeoCeDG app, DxfImportReport report) {
		String units = app.layerText("DxfReference.Transform.Units",
				report.file().insUnitsName(), report.file().documentUnit());
		return report.file().unitsDiffer()
				? units + " " + app.layerText("DxfReference.Transform.UnitsDiffer") : units;
	}

	private static String diagnostics(DxfSplineReference record) {
		if (record.diagnostics().isEmpty()) {
			return "";
		}
		StringBuilder text = new StringBuilder(" {");
		boolean first = true;
		for (DxfDiagnostic diagnostic : record.diagnostics()) {
			text.append(first ? "" : ", ").append(diagnostic.name());
			first = false;
		}
		return text.append('}').toString();
	}

	static String inspector(AppGeoCeDG app, GeoCeDGDxfReferenceSession.Entry entry) {
		DxfSplineReference record = entry.reference();
		StringBuilder text = new StringBuilder();
		text.append(app.layerText("DxfReference.ReadOnlyState")).append('\n');
		// PRE-G9B-MAINT-A-DXF0-R1: what identifies and manages the reference first
		line(app, text, "Name", displayName(record) + " (" + record.file().fileBaseName() + ")");
		line(app, text, "Nature", natureLabel(app, record));
		line(app, text, "Visible", app.layerText(entry.isVisible() ? "DxfReference.Yes"
				: "DxfReference.No"));
		text.append('\n').append(app.layerText("DxfReference.DetailsTitle")).append('\n');
		line(app, text, "Contract", record.contract() + ", " + record.nature().name());
		line(app, text, "Representation", (record.rationalFlagSet() ? "SET" : "CLEAR") + ", "
				+ record.weightProvenance().name());
		line(app, text, "Degree", Integer.toString(record.data().degree()));
		line(app, text, "ControlPoints", Integer.toString(record.data().controlPointCount()));
		line(app, text, "Domain", "[" + record.domainStart() + ", " + record.domainEnd() + "]");
		line(app, text, "Closure", record.isExactlyClosed() ? "EXACTLY_CLOSED" : "OPEN");
		StringBuilder continuity = new StringBuilder();
		for (BSplineInteriorKnot knot : record.continuity()) {
			continuity.append(continuity.length() == 0 ? "" : ", ").append("u=")
					.append(knot.value()).append(": C^").append(knot.continuityOrder());
		}
		line(app, text, "Continuity", continuity.length() == 0 ? "-" : continuity.toString());
		line(app, text, "File", record.file().fileBaseName() + " SHA-256 "
				+ record.file().fileSha256());
		line(app, text, "Version", record.file().acadVersion());
		line(app, text, "Entity", "#" + record.ordinal() + " " + record.handle() + " ["
				+ record.layer() + "]");
		line(app, text, "Units", record.file().insUnitsName() + " / "
				+ record.file().documentUnit());
		line(app, text, "Plane", "z = " + record.planeZ());
		line(app, text, "Transform", record.transform().toString());
		line(app, text, "SourceDigest", record.sourceDigest());
		line(app, text, "DataDigest", record.dataDigest());
		line(app, text, "Diagnostics", record.diagnostics().isEmpty() ? "-"
				: diagnostics(record).trim());
		DxfTessellation render = record.tessellation();
		line(app, text, "Render", app.layerText("DxfReference.Inspector.RenderValue",
				Integer.toString(render.size()), Double.toString(render.tolerance()),
				render.isComplete() ? "COMPLETE" : "INCOMPLETE"));
		text.append(app.layerText("DxfReference.Inspector.Authority"));
		return text.toString();
	}

	private static void line(AppGeoCeDG app, StringBuilder text, String field, String value) {
		text.append(app.layerText("DxfReference.Inspector." + field)).append(": ").append(value)
				.append('\n');
	}
}
