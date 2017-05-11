package wbs.smsapps.forwarder.api;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.rpc.core.Rpc;
import wbs.platform.rpc.core.RpcDefinition;
import wbs.platform.rpc.core.RpcType;

@SingletonComponent ("forwarderQueryExRequestDef")
public
class ForwarderQueryExRequestDef
	implements ComponentFactory <RpcDefinition> {

	// singleton dependencies

	@SingletonDependency
	ForwarderQueryExMessageChecker forwarderQueryExMessageChecker;

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	RpcDefinition makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

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

}
