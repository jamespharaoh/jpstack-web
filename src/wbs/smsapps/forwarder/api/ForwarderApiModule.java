package wbs.smsapps.forwarder.api;

import java.util.Map;

import javax.inject.Provider;

import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.api.mvc.ApiFile;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.StrongPrototypeDependency;
import wbs.framework.database.Database;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.logic.RawMediaLogic;
import wbs.platform.rpc.core.RpcDefinition;
import wbs.platform.rpc.php.PhpRpcAction;
import wbs.platform.rpc.xml.XmlRpcAction;

import wbs.smsapps.forwarder.logic.ForwarderLogic;
import wbs.smsapps.forwarder.model.ForwarderMessageInObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderMessageOutObjectHelper;

import wbs.web.file.WebFile;
import wbs.web.pathhandler.PathHandler;
import wbs.web.responder.WebModule;

@Accessors (fluent = true)
@SingletonComponent ("forwarderApiModule")
public
class ForwarderApiModule
	implements WebModule {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ForwarderApiLogic forwarderApiLogic;

	@SingletonDependency
	ForwarderLogic forwarderLogic;

	@SingletonDependency
	ForwarderMessageInObjectHelper forwarderMessageInHelper;

	@SingletonDependency
	ForwarderMessageOutObjectHelper forwarderMessageOutHelper;

	@SingletonDependency
	ForwarderQueryExMessageChecker forwarderQueryExMessageChecker;

	@SingletonDependency
	@NamedDependency
	RpcDefinition forwarderPeekExRequestDef;

	@SingletonDependency
	@NamedDependency
	RpcDefinition forwarderQueryExRequestDef;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaLogic mediaLogic;

	@SingletonDependency
	RawMediaLogic rawMediaLogic;

	// prototype dependencies

	@StrongPrototypeDependency
	Provider <ApiFile> apiFile;

	@PrototypeDependency
	Provider <ForwarderPeekExRpcHandler> peekExRpcHandlerProvider;

	@PrototypeDependency
	Provider <PhpRpcAction> phpRpcAction;

	@PrototypeDependency
	Provider <ForwarderQueryExRpcHandler> queryExRpcHandlerProvider;

	@PrototypeDependency
	Provider <ForwarderSendExRpcHandler> sendExRpcHandlerProvider;

	@PrototypeDependency
	Provider <ForwarderSendRpcHandler> sendRpcHandlerProvider;

	@PrototypeDependency
	Provider <ForwarderUnqueueExRpcHandler> unqueueExRpcHandlerProvider;

	@PrototypeDependency
	Provider <XmlRpcAction> xmlRpcAction;

	// properties

	@Getter
	Map <String, PathHandler> paths =
		ImmutableMap.of ();

	@Getter
	Map <String, WebFile> files;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			initFiles (
				taskLogger);

		}

	}

	private
	void initFiles (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"initFiles");

		) {

			files =
				ImmutableMap.<String, WebFile> builder ()

				.put (
					"/forwarder/control",
					apiFile.get ()

					.getActionName (
						taskLogger,
						"forwarderInAction")

					.postActionName (
						taskLogger,
						"forwarderInAction")

				)

				.put (
					"/forwarder/in",
					apiFile.get ()

					.getActionName (
						taskLogger,
						"forwarderInAction")

					.postActionName (
						taskLogger,
						"forwarderInAction")

				)

				.put (
					"/forwarder/out",
					apiFile.get ()

					.getActionName (
						taskLogger,
						"forwarderOutAction")

					.postActionName (
						taskLogger,
						"forwarderOutAction")

				)

				// ---------- php

				.put (
					"/forwarder/php/send",
					apiFile.get ()

					.postActionProvider (
						() -> phpRpcAction.get ()

						.rpcHandlerProvider (
							sendRpcHandlerProvider)

					)

				)

				.put (
					"/forwarder/php/sendEx",
					apiFile.get ()

					.postActionProvider (
						() -> phpRpcAction.get ()

						.rpcHandlerProvider (
							sendExRpcHandlerProvider)

					)

				)

				.put (
					"/forwarder/php/queryEx",
					apiFile.get ()

					.postActionProvider (
						() -> phpRpcAction.get ()

						.rpcHandlerProvider (
							queryExRpcHandlerProvider)

					)

				)

				.put (
					"/forwarder/php/peekEx",
					apiFile.get ()

					.postActionProvider (
						() -> phpRpcAction.get ()

						.rpcHandlerProvider (
							peekExRpcHandlerProvider)

					)

				)

				.put (
					"/forwarder/php/unqueueEx",
					apiFile.get ()

					.postActionProvider (
						() -> phpRpcAction.get ()

						.rpcHandlerProvider (
							unqueueExRpcHandlerProvider)

					)

				)

				// ---------- xml

				.put (
					"/forwarder/xml/send",
					apiFile.get ()

					.postActionProvider (
						() -> xmlRpcAction.get ()

						.rpcHandlerProvider (
							sendRpcHandlerProvider)

					)

				)

				.put (
					"/forwarder/xml/sendEx",
					apiFile.get ()

					.postActionProvider (
						() -> xmlRpcAction.get ()

						.rpcHandlerProvider (
							sendExRpcHandlerProvider)

					)

				)

				.put (
					"/forwarder/xml/queryEx",
					apiFile.get ()

					.postActionProvider (
						() -> xmlRpcAction.get ()

						.rpcHandlerProvider (
							queryExRpcHandlerProvider)

					)

				)

				.put (
					"/forwarder/xml/peekEx",
					apiFile.get ()

					.postActionProvider (
						() -> xmlRpcAction.get ()

						.rpcHandlerProvider (
							peekExRpcHandlerProvider)

					)

				)

				.put (
					"/forwarder/xml/unqueueEx",
					apiFile.get ()

					.postActionProvider (
						() -> xmlRpcAction.get ()

						.rpcHandlerProvider (
							unqueueExRpcHandlerProvider)

					)

				)

				.build ();

		}

	}

}
