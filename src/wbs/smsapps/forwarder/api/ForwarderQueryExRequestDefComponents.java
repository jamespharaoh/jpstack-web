package wbs.smsapps.forwarder.api;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;

import wbs.platform.rpc.core.Rpc;
import wbs.platform.rpc.core.RpcDefinition;
import wbs.platform.rpc.core.RpcType;

@SingletonComponent ("forwarderQueryExRequestDefComponents")
public
class ForwarderQueryExRequestDefComponents {

	// singleton dependencies

	@SingletonDependency
	ForwarderQueryExMessageChecker forwarderQueryExMessageChecker;

	// components

	@SingletonComponent ("forwarderQueryExRequestDef")
	public
	RpcDefinition forwarderQueryExRequestDef () {

		return Rpc.rpcDefinition (
			"forwarder-query-ex-request",
			RpcType.rStructure,

			Rpc.rpcDefinition (
				"slice",
				RpcType.rString),

			Rpc.rpcDefinition (
				"forwarder",
				RpcType.rString),

			Rpc.rpcDefinition (
				"password",
				RpcType.rString),

			Rpc.rpcDefinition (
				"unqueueExMessages",
				RpcType.rList,

				Rpc.rpcDefinition (
					"message",
					RpcType.rStructure,
					forwarderQueryExMessageChecker,

					Rpc.rpcDefinition (
						"server-id",
						null,
						RpcType.rInteger),

					Rpc.rpcDefinition (
						"client-id",
						null,
						RpcType.rString))));

	}

}
