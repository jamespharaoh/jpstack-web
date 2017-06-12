package wbs.smsapps.forwarder.api;

import static wbs.utils.collection.SetUtils.emptySet;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.rpc.core.Rpc;
import wbs.platform.rpc.core.RpcChecker;
import wbs.platform.rpc.core.RpcDefinition;
import wbs.platform.rpc.core.RpcType;

@SingletonComponent ("forwarderSendExRequestDef")
public
class ForwarderSendExRequestDef
	implements ComponentFactory <RpcDefinition> {

	// singleton components

	@SingletonDependency
	ForwarderApiLogic forwarderApiLogic;

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
				"forwarder-send-ex-request",
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
					"message-chains",
					RpcType.rList,

					Rpc.rpcDefinition (
						"message-chain",
						RpcType.rStructure,

					Rpc.rpcDefinition (
						"reply-to-server-id",
						null,
						RpcType.rInteger),

						Rpc.rpcDefinition (
							"unqueueExMessages",
							RpcType.rList,

							Rpc.rpcDefinition (
								"message",
								RpcType.rStructure,

								Rpc.rpcDefinition (
									"type",
									ForwarderMessageType.sms,
									RpcType.rString,
									Rpc.rpcEnumChecker (
										messageTypeEnumMap)),

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
									"url",
									null,
									RpcType.rString),

								Rpc.rpcDefinition (
									"route",
									"free",
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
									RpcType.rInteger),

								Rpc.rpcDefinition (
									"service",
									"default",
									RpcType.rString),

								Rpc.rpcDefinition (
									"reports",
									false,
									RpcType.rBoolean),

								Rpc.rpcDefinition (
									"retry-days",
									null,
									RpcType.rInteger),

								Rpc.rpcDefinition (
									"tags",
									emptySet (),
									RpcType.rList,
									Rpc.rpcSetChecker (),

									Rpc.rpcDefinition (
										"tag",
										RpcType.rString)),

								Rpc.rpcDefinition (
									"medias",
									null,
									RpcType.rList,

									Rpc.rpcDefinition (
										"media",
										RpcType.rStructure,

										Rpc.rpcDefinition (
											"url",
											RpcType.rString),

										Rpc.rpcDefinition (
											"message",
											null,
											RpcType.rString))),

								Rpc.rpcDefinition (
									"network-id",
									null,
									RpcType.rInteger))))));

		}

	}

	// data

	private final static
	Map <String, ForwarderMessageType> messageTypeEnumMap =
		ImmutableMap.<String, ForwarderMessageType> builder ()

		.put (
			"sms",
			ForwarderMessageType.sms)

		.put (
			"wap-push",
			ForwarderMessageType.wapPush)

		.put (
			"mms",
			ForwarderMessageType.mms)

		.build ();

}
