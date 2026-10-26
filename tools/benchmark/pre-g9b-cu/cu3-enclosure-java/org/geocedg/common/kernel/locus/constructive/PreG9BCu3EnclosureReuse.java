/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Point;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Result;
import org.geocedg.common.kernel.locus.intersection.ResidualJetEnclosure2D;
import org.geocedg.common.kernel.locus.intersection.ResidualJetSource2D;
import org.geogebra.common.kernel.geos.GeoElement;

/**
 * PRE-G9B-CU3 focal optimization T6, for {@code PreG9BCu3EnclosureReuseProbe} only: where the
 * time of the box enclosures of the constructive residual sources goes (driver point, traced
 * point, target, and both with the values that do not depend on the driver given), how many
 * boxes the queries of one locus share and whether their traced points are bitwise equal across
 * the cones of the queries, the cost of a box lookup, and, on a product that has the
 * accelerated sources, their bitwise equality with a full evaluation on the same boxes and the
 * size of the slice memories. Read through package access and reflection; it changes nothing but
 * the memories a computation fills. It is not part of any source set.
 */
public final class PreG9BCu3EnclosureReuse {

	private PreG9BCu3EnclosureReuse() {
	}

	/** The reflected parts of one constructive source. */
	private static final class Parts {
		private ResidualJetSource2D source;
		private ConstructiveCone cone;
		private int traced;
		private int target;
		private int driver;
		private Method driverPoint;
		private boolean[] dependsOnDriver;
	}

	/**
	 * @param sources the source of the last computation of each query, by query
	 * @param boxes the boxes asked of each query in that computation
	 * @param loci the locus and target of each query, by query
	 * @param repetitions repetitions of the timed passes
	 */
	public static Map<String, Object> analyze(Map<String, ResidualJetSource2D> sources,
			Map<String, List<double[]>> boxes, Map<String, GeoElement[]> loci, int repetitions)
			throws Exception {
		Map<String, Object> rows = new LinkedHashMap<>();
		Map<String, Parts> parts = new LinkedHashMap<>();
		for (Map.Entry<String, ResidualJetSource2D> entry : sources.entrySet()) {
			parts.put(entry.getKey(), parts(entry.getValue()));
		}
		Map<String, Map<String, Double>> perBoxTraced = new LinkedHashMap<>();
		for (Map.Entry<String, Parts> entry : parts.entrySet()) {
			Parts p = entry.getValue();
			List<double[]> list = boxes.get(entry.getKey());
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("boxes", list.size());
			row.put("cone_nodes", p.cone.size());
			int dependent = 0;
			for (boolean d : p.dependsOnDriver) {
				dependent += d ? 1 : 0;
			}
			row.put("nodes_depending_on_the_driver", dependent);
			row.put("target_depends_on_the_driver", p.dependsOnDriver[p.target]);
			Map<Integer, Object> independent = independent(p, list.get(0));
			row.put("independent_values", independent.size());
			long[][] samples = new long[repetitions][6];
			Map<String, Double> traced = new LinkedHashMap<>();
			for (int run = 0; run < repetitions; run++) {
				for (double[] box : list) {
					long t0 = System.nanoTime();
					p.source.enclose(box[0], box[1]);
					long t1 = System.nanoTime();
					Object driverPoint = driverPoint(p, box);
					long t2 = System.nanoTime();
					evaluator(p, driverPoint, Collections.emptyMap()).evaluate(p.traced);
					long t3 = System.nanoTime();
					evaluator(p, driverPoint, Collections.emptyMap()).evaluate(p.target);
					long t4 = System.nanoTime();
					evaluator(p, driverPoint, independent).evaluate(p.traced);
					long t5 = System.nanoTime();
					evaluator(p, driverPoint, independent).evaluate(p.target);
					long t6 = System.nanoTime();
					long[] sample = samples[run];
					sample[0] += t1 - t0;
					sample[1] += t2 - t1;
					sample[2] += t3 - t2;
					sample[3] += t4 - t3;
					sample[4] += t5 - t4;
					sample[5] += t6 - t5;
					if (run == repetitions - 1) {
						traced.put(key(box), (t3 - t2) / 1E6);
					}
				}
			}
			String[] names = {"enclose_as_in_the_product_ms", "driver_point_ms",
					"traced_point_ms", "target_ms", "traced_point_with_independent_values_ms",
					"target_with_independent_values_ms"};
			for (int column = 0; column < names.length; column++) {
				List<Long> values = new ArrayList<>();
				for (long[] sample : samples) {
					values.add(sample[column]);
				}
				Collections.sort(values);
				row.put("median_total_" + names[column], values.get(values.size() / 2) / 1E6);
			}
			perBoxTraced.put(entry.getKey(), traced);
			rows.put(entry.getKey(), row);
		}
		rows.put("sharing", sharing(parts, boxes, perBoxTraced));
		rows.put("lookup", lookup(boxes));
		rows.put("accelerated_against_full_evaluation", equivalence(boxes, loci));
		return rows;
	}

	/**
	 * Bitwise equality of the sources of the product with the full evaluation of every box, when
	 * the product has the switch of T6; the sizes of the slice memories.
	 */
	private static Map<String, Object> equivalence(Map<String, List<double[]>> boxes,
			Map<String, GeoElement[]> loci) throws Exception {
		Map<String, Object> rows = new LinkedHashMap<>();
		Method capture;
		try {
			capture = ConstructiveResidualJetSource.class.getDeclaredMethod("capture",
					GeoLocusV2.class, GeoElement.class, ExactContactMemory.class,
					ConstructiveIdentityBudget.class, boolean.class);
		} catch (NoSuchMethodException absent) {
			rows.put("available", false);
			return rows;
		}
		capture.setAccessible(true);
		rows.put("available", true);
		int compared = 0;
		int equal = 0;
		int undefined = 0;
		List<String> differences = new ArrayList<>();
		for (Map.Entry<String, List<double[]>> entry : boxes.entrySet()) {
			GeoElement[] pair = loci.get(entry.getKey());
			ResidualJetSource2D fast = (ResidualJetSource2D) capture.invoke(null, pair[0],
					pair[1], new ExactContactMemory(), ConstructiveIdentityBudget.V1, true);
			ResidualJetSource2D full = (ResidualJetSource2D) capture.invoke(null, pair[0],
					pair[1], new ExactContactMemory(), ConstructiveIdentityBudget.V1, false);
			for (double[] box : entry.getValue()) {
				compared++;
				String a = text(fast.enclose(box[0], box[1]));
				String b = text(full.enclose(box[0], box[1]));
				if (a.equals(b)) {
					equal++;
					undefined += "null".equals(b) ? 1 : 0;
				} else if (differences.size() < 5) {
					differences.add(entry.getKey() + " " + key(box) + ": " + a + " / " + b);
				}
			}
		}
		rows.put("boxes_compared", compared);
		rows.put("bitwise_equal", equal);
		rows.put("of_which_not_enclosed", undefined);
		rows.put("differences", differences);
		Map<String, Object> memories = new LinkedHashMap<>();
		Set<GeoElement> seen = new LinkedHashSet<>();
		// By name: the class exists only in a product with T6.
		Method of = Class.forName(PreG9BCu3EnclosureReuse.class.getPackageName()
				+ ".ConstructiveSliceEnclosureMemory").getDeclaredMethod("of", GeoLocusV2.class);
		of.setAccessible(true);
		for (GeoElement[] pair : loci.values()) {
			if (seen.add(pair[0])) {
				Object memory = of.invoke(null, pair[0]);
				Method size = memory.getClass().getDeclaredMethod("size");
				size.setAccessible(true);
				Method reused = memory.getClass().getDeclaredMethod("getReused");
				reused.setAccessible(true);
				Map<String, Object> row = new LinkedHashMap<>();
				row.put("boxes_kept", size.invoke(memory));
				row.put("enclosures_reused_so_far", reused.invoke(memory));
				memories.put(pair[0].getLabelSimple(), row);
			}
		}
		rows.put("slice_memories", memories);
		return rows;
	}

	private static String text(ResidualJetEnclosure2D enclosure) {
		if (enclosure == null) {
			return "null";
		}
		StringBuilder text = new StringBuilder();
		for (double[] interval : new double[][] {enclosure.getValue(), enclosure.getFirst(),
				enclosure.getSecond()}) {
			text.append(Long.toHexString(Double.doubleToRawLongBits(interval[0]))).append(',')
					.append(Long.toHexString(Double.doubleToRawLongBits(interval[1])))
					.append(';');
		}
		return text.toString();
	}

	private static Map<String, Object> sharing(Map<String, Parts> parts,
			Map<String, List<double[]>> boxes, Map<String, Map<String, Double>> traced)
			throws Exception {
		Map<String, Object> rows = new LinkedHashMap<>();
		Map<String, List<String>> owners = new LinkedHashMap<>();
		double total = 0;
		for (Map.Entry<String, List<double[]>> entry : boxes.entrySet()) {
			if (!entry.getKey().startsWith("e_1 ")) {
				continue;
			}
			for (double[] box : entry.getValue()) {
				owners.computeIfAbsent(key(box), ignored -> new ArrayList<>()).add(entry.getKey());
				total += traced.get(entry.getKey()).get(key(box));
			}
		}
		double union = 0;
		int shared = 0;
		int identical = 0;
		int different = 0;
		for (Map.Entry<String, List<String>> entry : owners.entrySet()) {
			List<String> queries = entry.getValue();
			union += traced.get(queries.get(0)).get(entry.getKey());
			if (new LinkedHashSet<>(queries).size() < 2) {
				continue;
			}
			shared++;
			String[] ends = entry.getKey().split(":");
			double[] box = {Double.longBitsToDouble(Long.parseUnsignedLong(ends[0], 16)),
					Double.longBitsToDouble(Long.parseUnsignedLong(ends[1], 16))};
			String reference = null;
			boolean same = true;
			for (String query : new LinkedHashSet<>(queries)) {
				Parts p = parts.get(query);
				Result value = evaluator(p, driverPoint(p, box), Collections.emptyMap())
						.evaluate(p.traced);
				String text = value.hasValue() ? bits(value.getValue()) : "failure " + value;
				if (reference == null) {
					reference = text;
				} else {
					same &= reference.equals(text);
				}
			}
			if (same) {
				identical++;
			} else {
				different++;
			}
		}
		rows.put("distinct_boxes_of_e_1", owners.size());
		rows.put("boxes_of_e_1_in_two_or_more_queries", shared);
		rows.put("shared_boxes_with_bitwise_identical_traced_point", identical);
		rows.put("shared_boxes_with_different_traced_point", different);
		rows.put("traced_point_ms_all_queries_of_e_1", total);
		rows.put("traced_point_ms_once_per_distinct_box", union);
		rows.put("maximum_saving_of_sharing_ms", total - union);
		return rows;
	}

	private static Map<String, Object> lookup(Map<String, List<double[]>> boxes) {
		Map<String, Object> rows = new LinkedHashMap<>();
		Map<List<Long>, Object> memory = new HashMap<>();
		int count = 0;
		for (List<double[]> list : boxes.values()) {
			for (double[] box : list) {
				memory.put(List.of(Double.doubleToRawLongBits(box[0]),
						Double.doubleToRawLongBits(box[1])), Boolean.TRUE);
				count++;
			}
		}
		long start = System.nanoTime();
		int hits = 0;
		for (int run = 0; run < 20; run++) {
			for (List<double[]> list : boxes.values()) {
				for (double[] box : list) {
					hits += memory.containsKey(List.of(Double.doubleToRawLongBits(box[0]),
							Double.doubleToRawLongBits(box[1]))) ? 1 : 0;
				}
			}
		}
		rows.put("entries", memory.size());
		rows.put("lookup_ns_per_box", (System.nanoTime() - start) / (20.0 * count));
		rows.put("hits", hits);
		return rows;
	}

	private static Parts parts(ResidualJetSource2D source) throws Exception {
		Parts p = new Parts();
		p.source = source;
		Class<?> type = source.getClass();
		p.cone = (ConstructiveCone) field(type, "cone").get(source);
		p.traced = field(type, "traced").getInt(source);
		p.target = field(type, "target").getInt(source);
		p.driver = field(type, "driver").getInt(source);
		p.driverPoint = type.getDeclaredMethod("driverPoint", IntervalJet.class);
		p.driverPoint.setAccessible(true);
		p.dependsOnDriver = new boolean[p.cone.size()];
		for (ConstructiveConeNode node : p.cone.getNodes()) {
			boolean depends = node.getIndex() == p.driver || node.isDriverSlice();
			for (int input = 0; input < node.getInputCount(); input++) {
				depends |= p.dependsOnDriver[node.getInput(input)];
			}
			p.dependsOnDriver[node.getIndex()] = depends;
		}
		return p;
	}

	private static Field field(Class<?> type, String name) throws Exception {
		Field field = type.getDeclaredField(name);
		field.setAccessible(true);
		return field;
	}

	private static Object driverPoint(Parts p, double[] box) throws Exception {
		IntervalJet parameter = IntervalJet.of(OutwardInterval.of(box[0], box[1]),
				OutwardInterval.point(1), OutwardInterval.point(0));
		return p.driverPoint.invoke(p.source, parameter);
	}

	private static ConstructiveConeEvaluator<IntervalJet> evaluator(Parts p, Object driverPoint,
			Map<Integer, Object> given) {
		Map<Integer, Object> fixed = new HashMap<>(given);
		fixed.put(p.driver, driverPoint);
		return new ConstructiveConeEvaluator<>(p.cone, IntervalJet.Arithmetic.INSTANCE, true,
				Collections.emptyMap(), fixed);
	}

	/** The values of the driver-independent inputs of driver-dependent nodes and the target. */
	private static Map<Integer, Object> independent(Parts p, double[] box) throws Exception {
		ConstructiveConeEvaluator<IntervalJet> evaluator = evaluator(p, driverPoint(p, box),
				Collections.emptyMap());
		Set<Integer> inputs = new LinkedHashSet<>();
		for (ConstructiveConeNode node : p.cone.getNodes()) {
			if (!p.dependsOnDriver[node.getIndex()] || node.getIndex() == p.driver) {
				continue;
			}
			for (int input = 0; input < node.getInputCount(); input++) {
				if (!p.dependsOnDriver[node.getInput(input)]) {
					inputs.add(node.getInput(input));
				}
			}
		}
		if (!p.dependsOnDriver[p.target]) {
			inputs.add(p.target);
		}
		Map<Integer, Object> values = new HashMap<>();
		for (int input : inputs) {
			Result result = evaluator.evaluate(input);
			if (result.hasValue()) {
				values.put(input, result.getValue());
			}
		}
		return values;
	}

	private static String key(double[] box) {
		return Long.toHexString(Double.doubleToRawLongBits(box[0])) + ":"
				+ Long.toHexString(Double.doubleToRawLongBits(box[1]));
	}

	@SuppressWarnings("unchecked")
	private static String bits(Object value) {
		if (!(value instanceof Point)) {
			return String.valueOf(value);
		}
		Point<IntervalJet> point = (Point<IntervalJet>) value;
		StringBuilder text = new StringBuilder();
		for (IntervalJet jet : new IntervalJet[] {point.getX(), point.getY()}) {
			for (OutwardInterval interval : new OutwardInterval[] {jet.getValue(), jet.getFirst(),
					jet.getSecond()}) {
				text.append(Long.toHexString(Double.doubleToRawLongBits(interval.getLower())))
						.append(',').append(Long.toHexString(Double.doubleToRawLongBits(
								interval.getUpper()))).append(';');
			}
		}
		return text.toString();
	}
}
