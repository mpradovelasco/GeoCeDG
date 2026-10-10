/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.geos.GeoElement;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R6-plus-E3-R1 (DXF-R1-03, DXF-R1-06): the AC1015 container written by the
 * shared DXF writer is structurally complete — section order, the nine symbol tables
 * with handles, owners, subclass markers and record counts, the space blocks, the
 * root dictionary and the plot-style placeholder, unique handles below
 * {@code $HANDSEED} and resolvable references — while the entity records keep their
 * G5 form and handles. AutoCAD 2026 and ezdxf 1.4.4 acceptance of this container is
 * recorded in the candidate report; this test pins the structure.
 */
class PreG9BR6PlusE3R1DxfContainerTest extends BaseUnitTest {

	private static final List<String> TABLES = List.of("VPORT", "LTYPE", "LAYER",
			"STYLE", "VIEW", "UCS", "APPID", "DIMSTYLE", "BLOCK_RECORD");
	private final GeometryExportService service = new GeometryExportService();

	@Test
	void sectionsAndTablesFormTheAc1015Container() {
		Dxf dxf = Dxf.parse(service.exportDxf(service.createModel(scene(),
				SelectionMode.CURRENT_SELECTION)));
		assertEquals(List.of("HEADER", "TABLES", "BLOCKS", "ENTITIES", "OBJECTS"),
				dxf.sections());
		assertEquals("EOF", dxf.pairs.get(dxf.pairs.size() - 1).value);
		assertEquals("AC1015", dxf.header("$ACADVER"));
		assertEquals("0", dxf.header("$INSUNITS"));
		assertEquals(TABLES, new ArrayList<>(dxf.tables.keySet()));
		for (Map.Entry<String, Record> table : dxf.tables.entrySet()) {
			Record head = table.getValue();
			assertNotNull(head.value(5), table.getKey() + " handle");
			assertEquals("0", head.value(330), table.getKey() + " owner");
			assertEquals("AcDbSymbolTable", head.values(100).get(0), table.getKey());
			List<Record> records = dxf.records.get(table.getKey());
			assertEquals(Integer.parseInt(head.value(70)), records.size(),
					table.getKey() + " record count");
			for (Record record : records) {
				assertEquals(head.value(5), record.value(330), table.getKey() + " owner");
				assertEquals("AcDbSymbolTableRecord", record.values(100).get(0));
				assertEquals(2, record.values(100).size(), "record subclass marker");
			}
		}
		assertEquals(List.of("ByBlock", "ByLayer", "CONTINUOUS"), dxf.names("LTYPE"));
		assertEquals(List.of("Standard"), dxf.names("STYLE"));
		assertEquals(List.of("ACAD"), dxf.names("APPID"));
		assertEquals(List.of("*Model_Space", "*Paper_Space"), dxf.names("BLOCK_RECORD"));
		assertTrue(dxf.records.get("VPORT").isEmpty() && dxf.records.get("VIEW").isEmpty()
				&& dxf.records.get("UCS").isEmpty() && dxf.records.get("DIMSTYLE").isEmpty());
		assertEquals("AcDbDimStyleTable", dxf.tables.get("DIMSTYLE").values(100).get(1));
	}

	@Test
	void handlesAreUniqueBelowTheSeedAndEveryReferenceResolves() {
		Dxf dxf = Dxf.parse(service.exportDxf(service.createModel(scene(),
				SelectionMode.CURRENT_SELECTION)));
		int seed = Integer.parseInt(dxf.header("$HANDSEED"), 16);
		Set<String> handles = new HashSet<>();
		int maxStructural = 0;
		for (Record record : dxf.all) {
			String handle = record.value(5);
			if (handle == null) {
				continue;
			}
			assertTrue(handles.add(handle), "unique handle " + handle);
			int value = Integer.parseInt(handle, 16);
			assertTrue(value < seed, handle + " below $HANDSEED " + seed);
			if (!record.inEntities) {
				maxStructural = Math.max(maxStructural, value);
			}
		}
		assertTrue(maxStructural < 0x100, "structural handles stay below the entities");
		for (Record record : dxf.all) {
			for (int code : new int[] {330, 340, 350, 390}) {
				for (String reference : record.values(code)) {
					assertTrue("0".equals(reference) && code == 330
							|| handles.contains(reference), code + " -> " + reference);
				}
			}
		}
		Record placeholder = dxf.object("ACDBPLACEHOLDER");
		for (Record layer : dxf.records.get("LAYER")) {
			assertEquals(placeholder.value(5), layer.value(390), "plot style of a layer");
			assertEquals("CONTINUOUS", layer.value(6));
		}
		Record root = dxf.object("DICTIONARY");
		assertEquals("ACAD_PLOTSTYLENAME", root.value(3));
		assertEquals(dxf.object("ACDBDICTIONARYWDFLT").value(5), root.value(350));
		List<Record> blocks = dxf.blocks();
		assertEquals(4, blocks.size(), "BLOCK and ENDBLK of both spaces");
		assertEquals(dxf.records.get("BLOCK_RECORD").get(0).value(5), blocks.get(0).value(330));
		assertEquals(dxf.records.get("BLOCK_RECORD").get(1).value(5), blocks.get(2).value(330));
		assertEquals("1", blocks.get(2).value(67), "paper space block");
	}

	@Test
	void entityRecordsKeepTheirG5FormHandlesAndLayers() {
		GeometryExportModel model = service.createModel(scene(),
				SelectionMode.CURRENT_SELECTION);
		Dxf dxf = Dxf.parse(service.exportDxf(model));
		Set<String> declared = new HashSet<>(dxf.names("LAYER"));
		int expected = 0x100;
		for (Record entity : dxf.entities()) {
			assertEquals(Integer.toHexString(expected++).toUpperCase(), entity.value(5));
			assertEquals("AcDbEntity", entity.values(100).get(0));
			assertTrue(declared.contains(entity.value(8)), "declared layer " + entity.value(8));
			assertTrue(entity.values(330).isEmpty(), "G5 entity form without an owner tag");
		}
		assertEquals(model.getEntities().size(), expected - 0x100);
		assertEquals(Integer.toHexString(expected).toUpperCase(), dxf.header("$HANDSEED"));
		assertEquals(service.exportDxf(model), service.exportDxf(service.createModel(scene(),
				SelectionMode.CURRENT_SELECTION)), "deterministic");
	}

	@Test
	void anEmptyModelStillHasAConsistentContainer() {
		Dxf dxf = Dxf.parse(service.exportDxf(service.createModel(new ArrayList<>(),
				SelectionMode.CURRENT_SELECTION)));
		assertEquals("100", dxf.header("$HANDSEED"));
		assertTrue(dxf.entities().isEmpty());
		assertEquals(List.of("0"), dxf.names("LAYER"));
	}

	private List<GeoElement> scene() {
		List<GeoElement> source = new ArrayList<>();
		source.add(add("A=(1,2)"));
		source.add(add("s=Segment((0,0),(3,4))"));
		source.add(add("c=Circle((5,6),2)"));
		source.add(add("q=Polyline((0,0),(1,2),(3,2))"));
		GeoElement hidden = add("h=Segment((0,1),(1,2))");
		hidden.setEuclidianVisible(false);
		hidden.setLayer(3);
		source.add(hidden);
		return source;
	}

	/** One DXF object: its type and its group pairs in order. */
	private static final class Record {
		private final String type;
		private final boolean inEntities;
		private final List<String[]> pairs = new ArrayList<>();

		private Record(String type, boolean inEntities) {
			this.type = type;
			this.inEntities = inEntities;
		}

		private String value(int code) {
			List<String> values = values(code);
			return values.isEmpty() ? null : values.get(0);
		}

		private List<String> values(int code) {
			List<String> values = new ArrayList<>();
			for (String[] pair : pairs) {
				if (Integer.parseInt(pair[0]) == code) {
					values.add(pair[1]);
				}
			}
			return values;
		}
	}

	/** Minimal pair parser of the writer's own output. */
	private static final class Dxf {
		private final List<Pair> pairs = new ArrayList<>();
		private final List<Record> all = new ArrayList<>();
		private final Map<String, Record> tables = new LinkedHashMap<>();
		private final Map<String, List<Record>> records = new HashMap<>();
		private final List<String> sections = new ArrayList<>();
		private final Map<String, List<Record>> bySection = new HashMap<>();

		private static Dxf parse(String text) {
			assertTrue(text.endsWith("\r\n"));
			String[] lines = text.split("\r\n", -1);
			assertEquals(1, lines.length % 2, "pairs plus the final empty element");
			Dxf dxf = new Dxf();
			for (int i = 0; i + 1 < lines.length; i += 2) {
				dxf.pairs.add(new Pair(lines[i].trim(), lines[i + 1]));
			}
			String section = null;
			String table = null;
			Record current = null;
			for (int i = 0; i < dxf.pairs.size(); i++) {
				Pair pair = dxf.pairs.get(i);
				if ("0".equals(pair.code)) {
					current = null;
					if ("SECTION".equals(pair.value)) {
						section = dxf.pairs.get(++i).value;
						dxf.sections.add(section);
						dxf.bySection.put(section, new ArrayList<>());
						continue;
					}
					if ("ENDSEC".equals(pair.value) || "EOF".equals(pair.value)) {
						section = null;
						continue;
					}
					if ("ENDTAB".equals(pair.value)) {
						table = null;
						continue;
					}
					current = new Record(pair.value, "ENTITIES".equals(section));
					dxf.all.add(current);
					if ("TABLE".equals(pair.value)) {
						table = dxf.pairs.get(i + 1).value;
						dxf.tables.put(table, current);
						dxf.records.put(table, new ArrayList<>());
					} else if (table != null) {
						dxf.records.get(table).add(current);
					} else if (section != null) {
						dxf.bySection.get(section).add(current);
					}
					continue;
				}
				if (current != null) {
					current.pairs.add(new String[] {pair.code, pair.value});
				}
			}
			return dxf;
		}

		private List<String> sections() {
			return sections;
		}

		private String header(String variable) {
			for (int i = 0; i + 1 < pairs.size(); i++) {
				if ("9".equals(pairs.get(i).code) && variable.equals(pairs.get(i).value)) {
					return pairs.get(i + 1).value;
				}
			}
			throw new AssertionError("missing header variable " + variable);
		}

		private List<String> names(String table) {
			List<String> names = new ArrayList<>();
			for (Record record : records.get(table)) {
				names.add(record.value(2));
			}
			return names;
		}

		private List<Record> entities() {
			return bySection.get("ENTITIES");
		}

		private List<Record> blocks() {
			return bySection.get("BLOCKS");
		}

		private Record object(String type) {
			for (Record record : bySection.get("OBJECTS")) {
				if (type.equals(record.type)) {
					return record;
				}
			}
			assertFalse(true, "missing object " + type);
			return null;
		}
	}

	private static final class Pair {
		private final String code;
		private final String value;

		private Pair(String code, String value) {
			this.code = code;
			this.value = value;
		}
	}
}
