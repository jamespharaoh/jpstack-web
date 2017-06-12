package wbs.smsapps.forwarder.api;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.rpc.core.Rpc;
import wbs.platform.rpc.core.RpcChecker;
import wbs.platform.rpc.core.RpcDefinition;
import wbs.platform.rpc.core.RpcType;

@SingletonComponent ("forwarderSendRequestDef")
public
class ForwarderSendRequestDef
	implements ComponentFactory <RpcDefinition> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

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
				"forwarder-send-request",
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
					"num-from",
					RpcType.rString),

				Rpc.rpcDefinition (
					"num-to",
					RpcType.rString,
					RpcChecker.stringNumeric),

				Rpc.rpcDefinition (
					"message",
					RpcType.rString),

				Rpc.rpcDefinition (
					"route",
					"free",
					RpcType.rString),

				Rpc.rpcDefinition (
					"service",
					"default",
					RpcType.rString),

				Rpc.rpcDefinition (
					"client-id",
					null,
					RpcType.rString),

				Rpc.rpcDefinition (
					"reply-to-server-id",
					null,
					RpcType.rInteger),

				Rpc.rpcDefinition (
					"pri",
					0,
					RpcType.rInteger));

		}

	}

}
