/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.kernelND.GeoElementND;

/**
 * A replica, built by commands, of the part of the author's model that the five mandatory
 * contacts of {@code CU3} use (Stage A design section 7): the cone construction with
 * {@code C' = Rotate(C, 47 degrees, A)}, the cut point {@code H} on {@code j = Line(A, C')}, the
 * circle {@code p} that carries the driver {@code E_1}, the slices of {@code e_1} and
 * {@code o_1}, and the targets {@code n}, {@code r}, {@code c_1} and {@code h_2}, with the
 * captured coordinates of the free points and of the points on paths. Labels follow the model
 * except {@code alpha} for {@code alpha_cone} and {@code Cr} for {@code C'}; {@code M_1} is the
 * indexed form of the model's intersection. The outputs of {@code Intersect(p, i)} are named by
 * position, as in the model: {@code K = B + (5, 0)} and {@code J = B - (5, 0)}, the point of the
 * seam; a fresh intersection selects them with the identity permutation, so here {@code K} is
 * output 0, while the permutation of the model makes it output 1.
 */
abstract class ConstructiveModelReplicaTest extends BaseUnitTest {

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	/** Builds the replica in the construction of the test. */
	protected void buildAuthorReplica() {
		add("A=(-0.9380450720469029,5.256462311160281)");
		add("g=OrthogonalLine(A,xAxis)");
		add("B=Point(g)");
		place("B", -0.9380450720469029, -5.367469712127068);
		add("rbCone=5");
		add("h=OrthogonalLine(A,g)");
		add("i=Line(B,h)");
		add("C=Point(h)");
		place("C", -1.079040900048968, 5.256462311160281);
		add("alpha=47°");
		add("Cr=Rotate(C,alpha,A)");
		add("j=Line(A,Cr)");
		add("H=Point(j)");
		place("H", 6.635494872916436, 13.378089572261038);
		add("k=Line(H,g)");
		add("I=Intersect(i,k)");
		add("p=Circle(B,rbCone)");
		GeoElementND[] roots = getElements("Intersect(p,i)");
		roots[0].toGeoElement().rename("K");
		roots[1].toGeoElement().rename("J");
		add("l=Line(J,g)");
		add("m=Line(K,g)");
		add("L=Intersect(l,h)");
		add("M=Intersect(h,m)");
		add("n=Segment(H,M)");
		add("r=Segment(H,L)");
		add("D_1=(8.871653198789899,7.8269897780685485)");
		add("c_1=Circle(H,D_1)");
		add("E_1=Point(p)");
		place("E_1", -4.382465549491176, -1.743112172567462);
		add("n_1=Ray(I,E_1)");
		add("p_1=Line(E_1,k)");
		add("F_1=Intersect(h,p_1)");
		add("d_1=Circle(I,E_1)");
		add("H_1=Intersect(d_1,i,1)");
		add("r_1=Line(H_1,k)");
		add("I_1=Intersect(r_1,h)");
		add("q_1=Ray(H,F_1)");
		add("s_1=Ray(H,I_1)");
		add("G_1=Intersect(c_1,s_1,1)");
		add("t_1=OrthogonalLine(G_1,k)");
		add("J_1=Intersect(q_1,t_1)");
		add("b_1=Line(J_1,k)");
		add("K_1=Intersect(b_1,n_1)");
		add("e_1=LocusV2(J_1,E_1)");
		add("o_1=LocusV2(K_1,E_1)");
		add("M_1=Intersect(c_1,n,1)");
		add("h_2=Line(M_1,k)");
		// A new line-circle intersection sets its permutation at its next computation; one
		// update settles every selection state, as any later edit of the document does.
		getKernel().updateConstruction(false);
	}

	/** Places a point on its path at the captured coordinates of the model. */
	protected void place(String label, double x, double y) {
		GeoPoint point = (GeoPoint) lookup(label);
		point.setCoords(x, y, 1.0);
		point.updateCascade();
	}

	/**
	 * @param label label of an object of the replica
	 * @return the object
	 */
	protected GeoElement geo(String label) {
		GeoElement geo = lookup(label);
		if (geo == null) {
			throw new IllegalStateException("No object " + label);
		}
		return geo;
	}
}
