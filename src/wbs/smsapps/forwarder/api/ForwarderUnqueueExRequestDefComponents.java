package wbs.smsapps.forwarder.api;

import wbs.framework.component.annotations.SingletonComponent;

import wbs.platform.rpc.core.Rpc;
import wbs.platform.rpc.core.RpcDefinition;
import wbs.platform.rpc.core.RpcType;

@SingletonComponent ("forwarderUnqueueExRequestDefComponents")
public
class ForwarderUnqueueExRequestDefComponents {

	@SingletonComponent ("forwarderUnqueueExRequestDef")
	public
	RpcDefinition forwarderUnqueueExRequestDef () {

		return Rpc.rpcDefinition (
			"forwarder-unqueue-ex-request",
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
				"allow-partial",
				false,
				RpcType.rBoolean),

			Rpc.rpcDefinition (
				"unqueueExMessages",
				null,
				RpcType.rList,

				Rpc.rpcDefinition (
					"message",
					RpcType.rStructure,

					Rpc.rpcDefinition (
						"server-id",
						RpcType.rInteger))),

			Rpc.rpcDefinition (
				"reports",
				null,
				RpcType.rList,

				Rpc.rpcDefinition (
					"report",
					RpcType.rStructure,

					Rpc.rpcDefinition (
						"report-id",
						RpcType.rInteger))));

	}

}
