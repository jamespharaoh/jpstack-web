package wbs.integrations.digitalselect.api;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.pathhandler.PathHandler;
import wbs.web.pathhandler.RegexpPathHandler;
import wbs.web.responder.WebModule;

@SingletonComponent ("digitalSelectApiServletModule")
public
class DigitalSelectApiServletModule
	implements WebModule {

	// singleton dependencies

	@SingletonDependency
	DigitalSelectRoutePathHandlerEntry digitalSelectRoutePathHandlerEntry;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <RegexpPathHandler> regexpPathHandlerProvider;

	// implementation

	@Override
	public
	Map <String, PathHandler> webModulePaths (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"paths");

		) {

			return ImmutableMap.<String,PathHandler>builder ()

				.put (
					"/digitalselect",
					regexpPathHandlerProvider.provide (
						taskLogger)

					.add (
						digitalSelectRoutePathHandlerEntry)

				)

				.build ()

			;

		}

	}

}
