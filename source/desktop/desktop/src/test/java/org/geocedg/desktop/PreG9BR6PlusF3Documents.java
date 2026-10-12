/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.app;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.baseline;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.defineMyMid;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.save;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.undo.UndoCommand;
import org.geogebra.common.plugin.EventType;
import org.geogebra.common.plugin.ScriptType;

/** PRE-G9B-R6-plus-F3 source and target documents of the focused tests. */
final class PreG9BR6PlusF3Documents {
	static final UnitState MM = UnitState.of(UnitToken.MM, null, null);
	static final UnitState M = UnitState.of(UnitToken.M, null, null);

	private PreG9BR6PlusF3Documents() {
	}

	/** Every elementary row of the matrix, with label conflicts against the target. */
	static File elementary(Path dir, String name) throws Exception {
		AppGeoCeDG s = app(M);
		for (String c : new String[] {"a=2", "A=(1,1)", "B=(4,2)", "C=(2,5)",
				"f(x)=a x^2/8", "g(x)=f(x)+1", "P=Point(g)", "sg=Segment(A,B)",
				"pol=Polygon(A,B,C)", "cA=Circle(A,B)", "el=Ellipse(A,B,6)", "m=Midpoint(sg)",
				"rA=Ray(A,C)", "vA=Vector(A,C)", "lAB=Line(A,B)", "t1=\"len \" + sg",
				"bb=a>1", "lst={A,B}", "sq=Sequence((k,0),k,1,3)", "ang=Angle(A,B,C)"}) {
			must(s, c);
		}
		s.getKernel().lookupLabel("C").setShowObjectCondition(
				(GeoBoolean) s.getKernel().lookupLabel("bb"));
		s.getKernel().lookupLabel("B").setLayer(2);
		return save(s, dir, name);
	}

	/** Locus V2 family (locus, length, intersection) and Spline V2. */
	static File locus(Path dir, String name) {
		AppGeoCeDG s = app(MM);
		for (String c : new String[] {"A=(1,1)", "B=(4,2)", "C=(2,5)", "E=(5,0)", "s=0",
				"Q=(s,0)", "D={false,{-2,2,true,true}}", "L=LocusV2(Q,s,D)",
				"len=LocusLength(L)", "S=SplineV2({A,B,C,E},3)", "lenS=LocusLength(S)"}) {
			must(s, c);
		}
		return save(s, dir, name);
	}

	/** Linear and Aligned dimensions in metres. */
	static File dimensions(Path dir, String name) {
		AppGeoCeDG s = app(M);
		for (String c : new String[] {"A=(1,1)", "B=(4,2)", "C=(2,5)",
				"dl=LinearDimension(B,C,Vector((1,2)),6)", "ad=AlignedDimension(A,B,1)"}) {
			must(s, c);
		}
		return save(s, dir, name);
	}

	/** One IsoABorder sheet (millimetres). */
	static File isoA(Path dir, String name) {
		AppGeoCeDG s = app(MM);
		must(s, "Tp=(40,40)");
		must(s, "Tsheet=IsoABorder(Tp,4,true,1,1,1.0,true)");
		return save(s, dir, name);
	}

	/** A macro-generated midpoint (MyMid). */
	static File macro(Path dir, String name, boolean third) throws Exception {
		AppGeoCeDG s = app(M);
		defineMyMid(s, "Kq", third);
		must(s, "A=(1,1)");
		must(s, "B=(4,2)");
		must(s, "N=MyMid(A,B)");
		return save(s, dir, name);
	}

	/** MyQuarter uses MyMid inside its definition (transitive closure). */
	static File nestedMacro(Path dir, String name) throws Exception {
		AppGeoCeDG s = app(M);
		defineMyMid(s, "Kq", false);
		GeoElement x1 = must(s, "X1=(0,0)");
		GeoElement x2 = must(s, "X2=(8,0)");
		must(s, "Mx=MyMid(X1,X2)");
		GeoElement out = must(s, "Qx=MyMid(X1,Mx)");
		s.getKernel().addMacro(new Macro(s.getKernel(), "MyQuarter",
				new GeoElement[] {x1, x2}, new GeoElement[] {out}));
		x1.remove();
		x2.remove();
		must(s, "A=(1,1)");
		must(s, "B=(5,1)");
		must(s, "R=MyQuarter(A,B)");
		return save(s, dir, name);
	}

	/** Locus V2 plus a tool: rejected (DQ-F3-24). */
	static File identityWithMacro(Path dir, String name) throws Exception {
		AppGeoCeDG s = app(MM);
		defineMyMid(s, "Kq", false);
		must(s, "A=(1,1)");
		must(s, "B=(4,2)");
		must(s, "N=MyMid(A,B)");
		must(s, "s=0");
		must(s, "Q=(s,0)");
		must(s, "D={false,{-2,2,true,true}}");
		must(s, "L=LocusV2(Q,s,D)");
		return save(s, dir, name);
	}

	static File scripted(Path dir, String name) {
		AppGeoCeDG s = app(M);
		GeoElement a = must(s, "A=(1,1)");
		must(s, "b=1");
		a.setScript(ScriptType.GGBSCRIPT.newScript(s, "SetValue(b,2)"), EventType.UPDATE);
		return save(s, dir, name);
	}

	static File single(Path dir, String name, String... commands) {
		AppGeoCeDG s = app(M);
		for (String c : commands) {
			must(s, c);
		}
		return save(s, dir, name);
	}

	static File empty(Path dir, String name) {
		return save(app(M), dir, name);
	}

	/**
	 * Target in millimetres with label conflicts, a Locus V2, an IsoABorder linked to the
	 * export area, F2 memory, a selection, a hidden layer, a copy buffer, a saved file
	 * and a mixed history with a Redo state.
	 */
	static AppGeoCeDG target(Path dir, String name) throws Exception {
		AppGeoCeDG t = app(MM);
		for (String c : new String[] {"A=(0,0)", "B=(9,9)", "tc=Circle(A,B)", "ln=Line(A,B)",
				"a=7", "s=0", "Q=(s,1)", "D={false,{-2,2,true,true}}", "Lt=LocusV2(Q,s,D)",
				"Tp=(40,40)", "Tsheet=IsoABorder(Tp,4,true,1,1,1.0,true)"}) {
			must(t, c);
		}
		t.getKernel().lookupLabel("B").setLayer(3);
		t.getLayerWorkspace().setWorkingLayer(4);
		t.getLayerWorkspace().setLayerHidden(3, true);
		File file = save(t, dir, name);
		t.setCurrentFile(file);
		baseline(t);
		PreG9BR6PlusF3Fixtures.mixedHistory(t);
		t.getCopyPaste().copyToXML(t, new ArrayList<>(List.of(t.getKernel().lookupLabel("tc"))));
		((GeoCeDGEuclidianController) t.getActiveEuclidianView().getEuclidianController())
				.getReferenceMemory().remember(GeoCeDGLineReferenceMemory.Slot.PARALLEL,
						t.getKernel().lookupLabel("ln"));
		t.getExportAreaSession().useIsoABorder(t.getEuclidianView1().getViewID(),
				org.geocedg.common.kernel.sheet.AlgoIsoABorder.ownerOf(
						t.getKernel().lookupLabel("Tsheet")));
		t.getSelectionManager().setSelectedGeos(new ArrayList<>(List.of(
				t.getKernel().lookupLabel("tc"))));
		t.setSaved();
		return t;
	}

	/** Complete observable state of a target, serializable and runtime separately. */
	static final class Observation {
		final GeoCeDGDocumentInsertion.Certificate certificate;
		final List<UndoCommand> commands;
		final int cursor;
		final String history;
		final Map<UndoCommand, List<String>> labels = new IdentityHashMap<>();
		final boolean buffer;
		final Object provenance;
		final String recent;

		Observation(AppGeoCeDG app) {
			certificate = GeoCeDGDocumentInsertion.Certificate.of(app);
			GeoCeDGUndoManager undo = PreG9BR6PlusF3Fixtures.undo(app);
			commands = undo.getCommands();
			cursor = undo.getCursor();
			history = PreG9BR6PlusF3Fixtures.historySignature(app);
			for (UndoCommand command : commands) {
				labels.put(command, command.labels == null ? null
						: new ArrayList<>(command.labels));
			}
			buffer = app.getCopyPaste().isEmpty();
			provenance = ((GeoCeDGCopyPaste) app.getCopyPaste()).getProvenance();
			StringBuilder files = new StringBuilder();
			for (int i = 0; i < org.geogebra.desktop.main.AppD.getFileListSize(); i++) {
				files.append(org.geogebra.desktop.main.AppD.getFromFileList(i)).append(';');
			}
			recent = files.toString();
		}

		/** @return differences in the serializable state and the history (empty = same) */
		List<String> serializableDifferences(Observation after) {
			List<String> differences = certificate.serializableDifferences(after.certificate);
			if (!commands.equals(after.commands) || cursor != after.cursor
					|| !history.equals(after.history)) {
				differences.add("history " + history + " -> " + after.history);
			}
			for (UndoCommand command : commands) {
				if (!java.util.Objects.equals(labels.get(command), command.labels)) {
					differences.add("labels");
				}
			}
			if (buffer != after.buffer || provenance != after.provenance) {
				differences.add("copy buffer");
			}
			if (!recent.equals(after.recent)) {
				differences.add("recent files");
			}
			return differences;
		}

		boolean sameInstances(Observation after) {
			return certificate.sameInstances(after.certificate);
		}
	}
}
