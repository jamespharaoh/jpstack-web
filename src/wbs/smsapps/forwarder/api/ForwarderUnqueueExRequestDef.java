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

@SingletonComponent ("forwarderUnqueueExRequestDef")
public
class ForwarderUnqueueExRequestDef
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

}
