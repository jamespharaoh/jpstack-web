package wbs.smsapps.forwarder.api;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.platform.rpc.core.Rpc;
import wbs.platform.rpc.core.RpcDefinition;
import wbs.platform.rpc.core.RpcType;

@SingletonComponent ("forwarderPeekExRequestDefConfig")
public
class ForwarderPeekExRequestDefConfig {

	@SingletonComponent ("forwarderPeekExRequestDef")
	public
	RpcDefinition forwarderPeekExRequestDef () {

		return Rpc.rpcDefinition (
			"forwarder-peek-ex-request",
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
				"get-unqueueExMessages",
				true,
				RpcType.rBoolean),

			Rpc.rpcDefinition (
				"get-reports",
				false,
				RpcType.rBoolean),

			Rpc.rpcDefinition (
				"max-results",
				10,
				RpcType.rInteger),

			Rpc.rpcDefinition (
				"advanced-reporting",
				false,
				RpcType.rBoolean));

	}

}
