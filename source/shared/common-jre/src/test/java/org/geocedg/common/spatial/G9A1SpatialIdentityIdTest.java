/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingId;
import org.geocedg.common.kernel.spatial.identity.ProjectionDiagramMapId;
import org.geocedg.common.kernel.spatial.identity.ProjectionFrameId;
import org.geocedg.common.kernel.spatial.identity.ProjectionFrameRelationId;
import org.geocedg.common.kernel.spatial.identity.ProjectionSystemId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityId;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectId;
import org.junit.jupiter.api.Test;

class G9A1SpatialIdentityIdTest {
	private static final String TOKEN = "0123456789abcdef0123456789abcdef";

	@Test
	void parsesEveryKindWithoutAliases() {
		assertInstanceOf(PersistentGeoId.class,
				SpatialIdentityId.parse("geo:" + TOKEN));
		assertInstanceOf(SpatialObjectId.class,
				SpatialIdentityId.parse("object:" + TOKEN));
		assertInstanceOf(ProjectionFrameId.class,
				SpatialIdentityId.parse("frame:" + TOKEN));
		assertInstanceOf(ProjectionSystemId.class,
				SpatialIdentityId.parse("system:" + TOKEN));
		assertInstanceOf(ProjectionDiagramMapId.class,
				SpatialIdentityId.parse("map:" + TOKEN));
		assertInstanceOf(ProjectionFrameRelationId.class,
				SpatialIdentityId.parse("relation:" + TOKEN));
		assertInstanceOf(ProjectionBindingId.class,
				SpatialIdentityId.parse("binding:" + TOKEN));
	}

	@Test
	void canonicalExternalFormRoundTripsExactly() {
		PersistentGeoId id = PersistentGeoId.parse("geo:" + TOKEN);

		assertEquals(TOKEN, id.getRawToken());
		assertEquals("geo:" + TOKEN, id.toExternalForm());
		assertEquals(id, SpatialIdentityId.parse(id.toExternalForm()));
	}

	@Test
	void sameOpaqueTokenDoesNotCollapseKinds() {
		PersistentGeoId geo = new PersistentGeoId(TOKEN);
		SpatialObjectId object = new SpatialObjectId(TOKEN);

		assertNotEquals(geo, object);
		assertNotEquals(0, geo.compareTo(object));
	}

	@Test
	void rejectsMalformedOrNonCanonicalIds() {
		assertThrows(IllegalArgumentException.class,
				() -> SpatialIdentityId.parse(TOKEN));
		assertThrows(IllegalArgumentException.class,
				() -> SpatialIdentityId.parse("unknown:" + TOKEN));
		assertThrows(IllegalArgumentException.class,
				() -> SpatialIdentityId.parse("geo:0123"));
		assertThrows(IllegalArgumentException.class,
				() -> SpatialIdentityId.parse("geo:0123456789ABCDEF0123456789ABCDEF"));
		assertThrows(IllegalArgumentException.class,
				() -> SpatialIdentityId.parse("geo:" + TOKEN + ":tail"));
	}

	@Test
	void typedParserRejectsAValidIdOfAnotherKind() {
		assertThrows(IllegalArgumentException.class,
				() -> PersistentGeoId.parse("frame:" + TOKEN));
	}
}
