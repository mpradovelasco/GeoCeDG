/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Immutable observations and differential evidence for the Desktop inspection worker. */
record GeoCeDGInspectionResult(List<Stage> stages, Admission admission,
		List<Comparison> comparisons, List<Diagnostic> diagnostics) {
	GeoCeDGInspectionResult {
		stages = List.copyOf(stages);
		comparisons = List.copyOf(comparisons);
		diagnostics = List.copyOf(diagnostics);
	}

	enum Phase { SOURCE, RECEIVER, TRIAL, REOPENED }

	enum Status {
		VERIFIED_UNCHANGED, VERIFIED_CHANGED, KNOWN_OMISSION, NOT_ESTABLISHED,
		NOT_EVALUATED, INVALID, TECHNICAL_FAILURE
	}

	enum Basis { LIVE_INSTANCE, NATIVE_COPY_TRACE, DURABLE_ID_IN_SAME_DOCUMENT }

	enum Property {
		PRESENCE, RUNTIME_INSTANCE, LABEL, DURABLE_ID, CLASS, PRODUCER, INPUTS, OUTPUTS,
		DEFINITION_TEXT, DEFINEDNESS, CURRENT_VALUE, PRESENTATION_FIELDS
	}

	enum AdmissionState { ADMITTED, REJECTED, NOT_EVALUATED }

	record Admission(AdmissionState state, String reason, String detail) {
	}

	record Diagnostic(Phase phase, Status status, String code, String detail) {
	}

	record Stage(Phase phase, String sha256, String documentId, Integer angleUnit,
			List<GeoCeDGInspectionArchive.Entry> entries, List<ObservedObject> objects) {
		Stage {
			entries = List.copyOf(entries);
			objects = List.copyOf(objects);
			Set<String> identities = new HashSet<>();
			if (objects.stream().map(ObservedObject::identity).filter(Objects::nonNull)
					.anyMatch(id -> !identities.add(id))) {
				throw new IllegalArgumentException("Duplicate durable identity in inspection stage");
			}
		}
	}

	/** A ref is a locator within one stage. An identity is attached by the native registry. */
	record ObservedObject(String ref, String label, String identity, String copySourceId, String geoClass,
			String producer, String p4Family, List<String> inputs, List<String> outputs,
			String definition, boolean defined, String value, String presentation) {
		ObservedObject {
			inputs = List.copyOf(inputs);
			outputs = List.copyOf(outputs);
		}
	}

	record Observation(Property property, Status status, String before, String after) {
	}

	record ObjectDelta(String beforeRef, String afterRef, List<Observation> observations) {
		ObjectDelta {
			observations = List.copyOf(observations);
		}
	}

	record Comparison(Phase before, Phase after, Basis basis, Status status,
			List<ObjectDelta> objects, List<String> added, List<String> unmatchedAfter) {
		Comparison {
			objects = List.copyOf(objects);
			added = List.copyOf(added);
			unmatchedAfter = List.copyOf(unmatchedAfter);
		}
	}

	/**
	 * Compares fields for established pairs. Callers supply only correspondence
	 * and omissions for which their stage-specific evidence is complete.
	 */
	static Comparison compare(Stage before, Stage after, Basis basis,
			Map<String, String> correspondence, Set<String> omissions) {
		Map<String, ObservedObject> indexed = new LinkedHashMap<>();
		after.objects().forEach(object -> indexed.put(object.ref(), object));
		Set<String> fromRefs = new HashSet<>();
		before.objects().forEach(object -> fromRefs.add(object.ref()));
		if (fromRefs.size() != before.objects().size() || indexed.size() != after.objects().size()
				|| !fromRefs.containsAll(correspondence.keySet()) || !fromRefs.containsAll(omissions)
				|| !indexed.keySet().containsAll(correspondence.values())
				|| new HashSet<>(correspondence.values()).size() != correspondence.size()
				|| omissions.stream().anyMatch(correspondence::containsKey)) {
			throw new IllegalArgumentException("Invalid inspection correspondence");
		}
		List<ObjectDelta> changes = new ArrayList<>();
		for (ObservedObject object : before.objects()) {
			ObservedObject target = indexed.get(correspondence.get(object.ref()));
			if (target == null) {
				Status status = omissions.contains(object.ref())
						? Status.KNOWN_OMISSION : Status.NOT_ESTABLISHED;
				List<Observation> evidence = new ArrayList<>();
				evidence.add(new Observation(Property.PRESENCE, status, object.ref(), null));
				if (basis == Basis.LIVE_INSTANCE) {
					evidence.add(new Observation(Property.RUNTIME_INSTANCE,
							Status.VERIFIED_CHANGED, object.ref(), null));
				}
				changes.add(new ObjectDelta(object.ref(), null, evidence));
				continue;
			}
			changes.add(compareObject(object, target, correspondence));
		}
		Set<String> matched = new HashSet<>(correspondence.values());
		List<String> unmatched = indexed.keySet().stream().filter(ref -> !matched.contains(ref))
				.toList();
		// Classify additions only when every resident instance survived.
		boolean additive = basis == Basis.LIVE_INSTANCE
				&& correspondence.size() == before.objects().size();
		boolean changed = additive && !unmatched.isEmpty()
				|| changes.stream().flatMap(delta -> delta.observations().stream())
						.anyMatch(observation -> observation.status() == Status.VERIFIED_CHANGED
								|| observation.status() == Status.KNOWN_OMISSION);
		// Aggregate equivalence is outside I2's sample coverage.
		return new Comparison(before.phase(), after.phase(), basis,
				changed ? Status.VERIFIED_CHANGED : Status.NOT_ESTABLISHED, changes,
				additive ? unmatched : List.of(), additive ? List.of() : unmatched);
	}

	private static ObjectDelta compareObject(ObservedObject before, ObservedObject after,
			Map<String, String> correspondence) {
		return new ObjectDelta(before.ref(), after.ref(), List.of(
				new Observation(Property.PRESENCE, Status.VERIFIED_UNCHANGED,
						before.ref(), after.ref()),
				field(Property.LABEL, before.label(), after.label()),
				new Observation(Property.DURABLE_ID,
						Objects.equals(before.identity(), after.identity())
								? Status.VERIFIED_UNCHANGED : Status.VERIFIED_CHANGED,
						before.identity(), after.identity()),
				field(Property.CLASS, before.geoClass(), after.geoClass()),
				field(Property.PRODUCER, before.producer(), after.producer()),
				edges(Property.INPUTS, before.inputs(), after.inputs(), correspondence),
				edges(Property.OUTPUTS, before.outputs(), after.outputs(), correspondence),
				field(Property.DEFINITION_TEXT, before.definition(), after.definition()),
				field(Property.DEFINEDNESS, Boolean.toString(before.defined()),
						Boolean.toString(after.defined())),
				field(Property.CURRENT_VALUE, before.value(), after.value()),
				field(Property.PRESENTATION_FIELDS, before.presentation(), after.presentation())));
	}

	private static Observation field(Property property, String before, String after) {
		Status status = before == null || after == null ? Status.NOT_ESTABLISHED
				: Objects.equals(before, after) ? Status.VERIFIED_UNCHANGED : Status.VERIFIED_CHANGED;
		return new Observation(property, status, before, after);
	}

	private static Observation edges(Property property, List<String> before, List<String> after,
			Map<String, String> correspondence) {
		Status status = before.size() == after.size() ? Status.VERIFIED_UNCHANGED
				: Status.VERIFIED_CHANGED;
		for (int i = 0; i < before.size() && status != Status.VERIFIED_CHANGED; i++) {
			String source = before.get(i);
			String target = after.get(i);
			String expected = source.startsWith("axis:") ? source : correspondence.get(source);
			if (expected == null || target.startsWith("unresolved:")) {
				status = Status.NOT_ESTABLISHED;
			} else if (!expected.equals(target)) {
				status = Status.VERIFIED_CHANGED;
			}
		}
		return new Observation(property, status, before.toString(), after.toString());
	}
}
