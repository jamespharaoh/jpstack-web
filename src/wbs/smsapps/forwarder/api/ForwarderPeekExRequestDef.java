package wbs.smsapps.forwarder.api;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.rpc.core.Rpc;
import wbs.platform.rpc.core.RpcDefinition;
import wbs.platform.rpc.core.RpcType;

@SingletonComponent ("forwarderPeekExRequestDef")
public
class ForwarderPeekExRequestDef
	implements ComponentFactory <RpcDefinition> {

	// singleton dependencies

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

}
