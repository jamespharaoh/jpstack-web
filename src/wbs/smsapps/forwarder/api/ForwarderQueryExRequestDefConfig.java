package wbs.smsapps.forwarder.api;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.rpc.core.Rpc;
import wbs.platform.rpc.core.RpcDefinition;
import wbs.platform.rpc.core.RpcType;

@SingletonComponent ("forwarderQueryExRequestDefConfig")
public
class ForwarderQueryExRequestDefConfig {

	@Inject
	ForwarderQueryExMessageChecker forwarderQueryExMessageChecker;

	@SingletonComponent ("fowarderQueryExRequestDef")
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
