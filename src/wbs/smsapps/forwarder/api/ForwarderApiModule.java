package wbs.smsapps.forwarder.api;

import java.util.Map;

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
import wbs.framework.component.manager.ComponentProvider;
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
	ComponentProvider <ApiFile> apiFile;

	@PrototypeDependency
	ComponentProvider <ForwarderPeekExRpcHandler> peekExRpcHandlerProvider;

	@PrototypeDependency
	ComponentProvider <PhpRpcAction> phpRpcAction;

	@PrototypeDependency
	ComponentProvider <ForwarderQueryExRpcHandler> queryExRpcHandlerProvider;

	@PrototypeDependency
	ComponentProvider <ForwarderSendExRpcHandler> sendExRpcHandlerProvider;

	@PrototypeDependency
	ComponentProvider <ForwarderSendRpcHandler> sendRpcHandlerProvider;

	@PrototypeDependency
	ComponentProvider <ForwarderUnqueueExRpcHandler>
		unqueueExRpcHandlerProvider;

	@PrototypeDependency
	ComponentProvider <XmlRpcAction> xmlRpcAction;

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
					apiFile.provide (
						taskLogger)

					.getActionName (
						taskLogger,
						"forwarderInAction")

					.postActionName (
						taskLogger,
						"forwarderInAction")

				)

				.put (
					"/forwarder/in",
					apiFile.provide (
						taskLogger)

					.getActionName (
						taskLogger,
						"forwarderInAction")

					.postActionName (
						taskLogger,
						"forwarderInAction")

				)

				.put (
					"/forwarder/out",
					apiFile.provide (
						taskLogger)

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
					apiFile.provide (
						taskLogger)

					.postActionProvider (
						taskLoggerNested ->
							phpRpcAction.provide (
							taskLoggerNested)

						.rpcHandlerProvider (
							sendRpcHandlerProvider)

					)

				)

				.put (
					"/forwarder/php/sendEx",
					apiFile.provide (
						taskLogger)

					.postActionProvider (
						taskLoggerNested ->
							phpRpcAction.provide (
								taskLoggerNested)

						.rpcHandlerProvider (
							sendExRpcHandlerProvider)

					)

				)

				.put (
					"/forwarder/php/queryEx",
					apiFile.provide (
						taskLogger)

					.postActionProvider (
						taskLoggerNested ->
							phpRpcAction.provide (
								taskLoggerNested)

						.rpcHandlerProvider (
							queryExRpcHandlerProvider)

					)

				)

				.put (
					"/forwarder/php/peekEx",
					apiFile.provide (
						taskLogger)

					.postActionProvider (
						taskLoggerNested ->
							phpRpcAction.provide (
								taskLoggerNested)

						.rpcHandlerProvider (
							peekExRpcHandlerProvider)

					)

				)

				.put (
					"/forwarder/php/unqueueEx",
					apiFile.provide (
						taskLogger)

					.postActionProvider (
						taskLoggerNested ->
							phpRpcAction.provide (
								taskLoggerNested)

						.rpcHandlerProvider (
							unqueueExRpcHandlerProvider)

					)

				)

				// ---------- xml

				.put (
					"/forwarder/xml/send",
					apiFile.provide (
						taskLogger)

					.postActionProvider (
						taskLoggerNested ->
							xmlRpcAction.provide (
								taskLoggerNested)

						.rpcHandlerProvider (
							sendRpcHandlerProvider)

					)

				)

				.put (
					"/forwarder/xml/sendEx",
					apiFile.provide (
						taskLogger)

					.postActionProvider (
						taskLoggerNested ->
							xmlRpcAction.provide (
								taskLoggerNested)

						.rpcHandlerProvider (
							sendExRpcHandlerProvider)

					)

				)

				.put (
					"/forwarder/xml/queryEx",
					apiFile.provide (
						taskLogger)

					.postActionProvider (
						taskLoggerNested ->
							xmlRpcAction.provide (
								taskLoggerNested)

						.rpcHandlerProvider (
							queryExRpcHandlerProvider)

					)

				)

				.put (
					"/forwarder/xml/peekEx",
					apiFile.provide (
						taskLogger)

					.postActionProvider (
						taskLoggerNested ->
							xmlRpcAction.provide (
								taskLoggerNested)

						.rpcHandlerProvider (
							peekExRpcHandlerProvider)

					)

				)

				.put (
					"/forwarder/xml/unqueueEx",
					apiFile.provide (
						taskLogger)

					.postActionProvider (
						taskLoggerNested ->
							xmlRpcAction.provide (
								taskLoggerNested)

						.rpcHandlerProvider (
							unqueueExRpcHandlerProvider)

					)

				)

				.build ();

		}

	}

	@Override
	public
	Map <String, WebFile> webModuleFiles (
			@NonNull TaskLogger parentTaskLogger) {

		return files;

	}

}
