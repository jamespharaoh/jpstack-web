package wbs.integrations.txtnation.api;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
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

@SingletonComponent ("txtNationApiServletModule")
public
class TxtNationApiServletModule
	implements WebModule {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	TxtNationRoutePathHandlerEntry txtNationRoutePathHandlerEntry;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <RegexpPathHandler> regexpPathHandlerProvider;

	// state

	PathHandler pathHandler;

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

			pathHandler =
				regexpPathHandlerProvider.provide (
					taskLogger)

				.add (
					txtNationRoutePathHandlerEntry)

			;

		}

	}

	// implementation

	@Override
	public
	Map <String, PathHandler> webModulePaths (
			@NonNull TaskLogger parentTaskLogger) {

		return ImmutableMap.<String, PathHandler> builder ()

			.put (
				"/txtnation",
				pathHandler)

			.build ();

	}

}
