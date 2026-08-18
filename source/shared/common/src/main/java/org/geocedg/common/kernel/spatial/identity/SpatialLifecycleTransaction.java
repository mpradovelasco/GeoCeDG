/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

/** Prepared lifecycle graph and runtime switch with one-shot commit/rollback. */
public final class SpatialLifecycleTransaction {
	/** Observable transaction state. */
	public enum State {
		PREPARED,
		COMMITTED,
		ROLLED_BACK
	}

	private final SpatialIdentityRegistry registry;
	private final SpatialLifecycleMutation mutation;
	private final SpatialLifecycleProspectiveGraph prospectiveGraph;
	private final SpatialLifecycleRuntime.PreparedSwitch runtimeSwitch;
	private final long expectedRegistryEpoch;
	private State state = State.PREPARED;

	SpatialLifecycleTransaction(SpatialIdentityRegistry registry,
			SpatialLifecycleMutation mutation,
			SpatialLifecycleProspectiveGraph prospectiveGraph,
			SpatialLifecycleRuntime.PreparedSwitch runtimeSwitch,
			long expectedRegistryEpoch) {
		this.registry = registry;
		this.mutation = mutation;
		this.prospectiveGraph = prospectiveGraph;
		this.runtimeSwitch = runtimeSwitch;
		this.expectedRegistryEpoch = expectedRegistryEpoch;
	}

	public SpatialLifecycleMutation getMutation() {
		return mutation;
	}

	public SpatialLifecycleProspectiveGraph getProspectiveGraph() {
		return prospectiveGraph;
	}

	public State getState() {
		return state;
	}

	/** Atomically installs the prepared registry and runtime graph. */
	public void commit() {
		registry.commitLifecycle(this);
	}

	/** Abandons prepared state and retires every unused fresh reservation. */
	public void rollback() {
		registry.rollbackLifecycle(this);
	}

	SpatialLifecycleRuntime.PreparedSwitch getRuntimeSwitch() {
		return runtimeSwitch;
	}

	long getExpectedRegistryEpoch() {
		return expectedRegistryEpoch;
	}

	boolean isOwnedBy(SpatialIdentityRegistry candidate) {
		return registry == candidate;
	}

	void markCommitted() {
		state = State.COMMITTED;
	}

	void markRolledBack() {
		state = State.ROLLED_BACK;
	}
}
