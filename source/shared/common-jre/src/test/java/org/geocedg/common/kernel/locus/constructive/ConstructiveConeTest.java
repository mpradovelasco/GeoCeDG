/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.locus.constructive.ConstructiveCone.ConstructiveConeException;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.junit.jupiter.api.Test;

/**
 * The constructive cone of a relation ({@code locus-v2-constructive-authority.md} sections 1,
 * 3 and 8.1) on the replica of the author's model: one meaning per object whatever the relation
 * or driver that asks for it (V-AUT-PROV-2), the substituted driver slice, the declared boundary
 * and the cut of what reads an object outside the class, and no change to the construction.
 */
final class ConstructiveConeTest extends ConstructiveModelReplicaTest {

	@Test
	void theConeOfAMandatoryContactFollowsTheConstruction() {
		buildAuthorReplica();
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int target = builder.add(geo("n"));
		int point = builder.addWithDriverSubstitution(geo("J_1"), geo("E_1"), geo("K"));
		ConstructiveCone cone = builder.build();

		ConstructiveConeNode replacement = cone.getNode(cone.getReplacementNode());
		assertEquals(cone.nodeOf(geo("K")), cone.getReplacementNode());
		assertEquals(ConstructiveOperation.LINE_CIRCLE_OUTPUT, replacement.getOperation());
		assertEquals(0, replacement.getOutputIndex(), "K is output 0 of the replica");
		assertEquals(cone.nodeOf(geo("i")), replacement.getInput(0));
		assertEquals(cone.nodeOf(geo("p")), replacement.getInput(1));
		assertEquals(ConstructiveOperation.SEGMENT, cone.getNode(target).getOperation());
		ConstructiveConeNode traced = cone.getNode(point);
		assertEquals(ConstructiveOperation.MEET, traced.getOperation());
		assertTrue(traced.isDriverSlice());
		assertEquals(point, cone.substitutedNodeOf(geo("J_1")));
		assertEquals(-1, cone.nodeOf(geo("J_1")), "J_1 is not used outside the slice");
		assertEquals(-1, cone.nodeOf(geo("E_1")), "the substituted driver has no node");

		assertEquals(List.of("Cr"), names(cone.getBoundaryNodes()),
				"the only boundary object is the rotation of C");
		ConstructiveConeNode rotation = cone.getBoundaryNodes().get(0);
		assertEquals(ConstructiveOperation.ROTATE_POINT, rotation.getOperation());
		assertEquals(cone.nodeOf(geo("C")), rotation.getInput(0));
		assertEquals(cone.nodeOf(geo("alpha")), rotation.getInput(1));
		assertEquals(cone.nodeOf(geo("A")), rotation.getInput(2));
		for (String label : new String[] {"B", "C", "H"}) {
			assertEquals(ConstructiveOperation.PATH_POINT,
					cone.getNode(cone.nodeOf(geo(label))).getOperation(), label);
		}
		assertEquals(cone.nodeOf(geo("j")),
				cone.getNode(cone.nodeOf(geo("H"))).getInput(0), "H is on j");
		for (String label : new String[] {"A", "rbCone", "alpha", "D_1"}) {
			assertEquals(ConstructiveOperation.Membership.FREE_INPUT,
					cone.getNode(cone.nodeOf(geo(label))).getOperation().getMembership(),
					label);
		}
		for (ConstructiveConeNode node : cone.getNodes()) {
			for (int position = 0; position < node.getInputCount(); position++) {
				assertTrue(node.getInput(position) < node.getIndex(), "inputs come first");
			}
		}
		String description = cone.describe();
		for (String label : new String[] {"J_1", "E_1", "rbCone", "alpha", "c_1"}) {
			assertFalse(description.contains(label), "a label never identifies a node");
		}
	}

	@Test
	void anObjectThatDependsOnTheDriverKeepsItsOwnMeaningOutsideTheSlice() {
		buildAuthorReplica();
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int document = builder.add(geo("n_1"));
		builder.addWithDriverSubstitution(geo("K_1"), geo("E_1"), geo("K"));
		ConstructiveCone cone = builder.build();

		int sliced = cone.substitutedNodeOf(geo("n_1"));
		assertNotEquals(document, sliced, "n_1 has one node in the document and one in the slice");
		assertFalse(cone.getNode(document).isDriverSlice());
		assertTrue(cone.getNode(sliced).isDriverSlice());
		assertEquals(cone.nodeOf(geo("E_1")), cone.getNode(document).getInput(1),
				"outside the slice the ray goes through the driver point itself");
		assertEquals(ConstructiveOperation.PATH_POINT,
				cone.getNode(cone.nodeOf(geo("E_1"))).getOperation());
		assertEquals(cone.getReplacementNode(), cone.getNode(sliced).getInput(1),
				"inside the slice it goes through the replacement");
		assertEquals(cone.nodeOf(geo("I")), cone.substitutedNodeOf(geo("I")),
				"an object independent of the driver has a single node");
	}

	@Test
	void everyObjectHasOneMeaningWhateverTheRelationThatAsksForIt() {
		buildAuthorReplica();
		ConstructiveCone.Builder alone = ConstructiveCone.builder(getConstruction());
		int first = alone.add(geo("h_2"));
		ConstructiveCone single = alone.build();
		ConstructiveCone.Builder contact = ConstructiveCone.builder(getConstruction());
		contact.add(geo("c_1"));
		contact.add(geo("r"));
		int second = contact.add(geo("h_2"));
		contact.addWithDriverSubstitution(geo("K_1"), geo("E_1"), geo("K"));
		ConstructiveCone larger = contact.build();
		ConstructiveCone.Builder other = ConstructiveCone.builder(getConstruction());
		int third = other.addWithDriverSubstitution(geo("J_1"), geo("E_1"), geo("J"));
		int fourth = other.add(geo("h_2"));
		ConstructiveCone driven = other.build();

		String meaning = definition(single, first);
		assertEquals(meaning, definition(larger, second),
				"V-AUT-PROV-2: h_2 has one constructive meaning");
		assertEquals(meaning, definition(driven, fourth));
		assertTrue(driven.getNode(third).isDriverSlice());
	}

	@Test
	void aReplacementThatDependsOnTheDriverIsRefused() {
		buildAuthorReplica();
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());

		ConstructiveConeException refused = assertThrows(ConstructiveConeException.class,
				() -> builder.addWithDriverSubstitution(geo("J_1"), geo("E_1"), geo("F_1")));

		assertEquals(ConstructiveCone.REPLACEMENT_DEPENDS_ON_DRIVER, refused.getCode());
	}

	@Test
	void whatReadsAnObjectOutsideTheClassIsCut() {
		add("A=(0,0)");
		add("B=(1,1)");
		add("k: x^2+y^2=4");
		add("free: y=2x+1");
		add("f=Line(A,B)");
		add("X=Intersect(k,f,1)");
		add("par=Line(A,free)");
		add("Y=Intersect(par,f)");
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int cut = builder.add(lookup("X"));
		int parallel = builder.add(lookup("par"));
		int meet = builder.add(lookup("Y"));
		ConstructiveCone cone = builder.build();

		assertEquals(ConstructiveOperation.BOUNDARY_POINT, cone.getNode(cut).getOperation(),
				"an intersection with a circle given by an equation is a substitute");
		assertEquals(0, cone.getNode(cut).getInputCount());
		assertEquals("computed from an object outside the class", cone.getNode(cut).getReason());
		assertEquals(ConstructiveOperation.OUTSIDE, cone.getNode(parallel).getOperation(),
				"a line parallel to a free line stays outside the class");
		assertEquals(ConstructiveOperation.BOUNDARY_POINT, cone.getNode(meet).getOperation());
	}

	@Test
	void aRenameKeepsTheDescriptionAndARedefinitionChangesIt() {
		buildAuthorReplica();
		String before = cone("n").describe();

		geo("H").rename("Hcut");
		geo("rbCone").rename("radius");
		assertEquals(before, cone("n").describe(), "labels are presentation only");

		editGeoElement(geo("m"), "OrthogonalLine(K,h)");
		assertNotEquals(before, cone("n").describe(), "a redefinition changes the structure");
	}

	@Test
	void aPendingPermutationIsRecordedAsNotReconstructed() {
		add("A=(0,0)");
		add("c=Circle(A,(3,0))");
		add("f=Line((-5,1),(5,1))");
		GeoElementND[] outputs = getElements("Intersect(c,f)");
		ConstructiveCone.Builder fresh = ConstructiveCone.builder(getConstruction());
		int created = fresh.add(outputs[1].toGeoElement());

		assertEquals(null, fresh.build().getNode(created).getSelection(),
				"the next computation sets the permutation by proximity");
		getKernel().updateConstruction(false);
		ConstructiveCone.Builder settled = ConstructiveCone.builder(getConstruction());
		int updated = settled.add(outputs[1].toGeoElement());
		assertEquals("permutation(0,1),labels(1,1)",
				settled.build().getNode(updated).getSelection().describe());
	}

	@Test
	void aRotationThatMovesWithTheDriverBelongsToTheSlice() {
		add("A=(0,0)");
		add("c=Circle(A,(2,0))");
		add("E=Point(c)");
		add("w=30°");
		add("Q=Rotate(E,w,A)");
		add("D=(0,2)");
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int traced = builder.addWithDriverSubstitution(lookup("Q"), lookup("E"), lookup("D"));
		ConstructiveConeNode rotation = builder.build().getNode(traced);

		assertEquals(ConstructiveOperation.ROTATE_POINT, rotation.getOperation());
		assertTrue(rotation.isDriverSlice(), "its captured state belongs to another driver");
	}

	@Test
	void buildingAConeChangesNothing() {
		buildAuthorReplica();
		String before = getApp().getXML();

		cone("h_2");
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		builder.add(geo("r"));
		builder.addWithDriverSubstitution(geo("J_1"), geo("E_1"), geo("J"));
		builder.build();

		assertEquals(before, getApp().getXML(), "V-AUT-NUM-6: the cone only reads");
	}

	private ConstructiveCone cone(String label) {
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		builder.add(geo(label));
		return builder.build();
	}

	/** The structural definition of a node with the definitions of all its ancestors. */
	private static String definition(ConstructiveCone cone, int node) {
		ConstructiveConeNode current = cone.getNode(node);
		StringBuilder text = new StringBuilder(current.getOperation().name()).append('(');
		for (int position = 0; position < current.getInputCount(); position++) {
			text.append(position == 0 ? "" : ",")
					.append(definition(cone, current.getInput(position)));
		}
		text.append(')');
		if (current.getOutputIndex() >= 0) {
			text.append('#').append(current.getOutputIndex());
		}
		return text.toString();
	}

	private static List<String> names(List<ConstructiveConeNode> nodes) {
		List<String> names = new ArrayList<>();
		for (ConstructiveConeNode node : nodes) {
			names.add(node.getPresentationName());
		}
		return names;
	}
}
