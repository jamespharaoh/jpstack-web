package wbs.integrations.oxygen8.api;

import static wbs.utils.etc.NumberUtils.parseIntegerRequired;

import java.util.Map;
import java.util.regex.Matcher;

import javax.inject.Named;
import javax.inject.Provider;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.api.mvc.ApiFile;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.web.context.RequestContext;
import wbs.web.file.WebFile;
import wbs.web.pathhandler.PathHandler;
import wbs.web.pathhandler.RegexpPathHandler;
import wbs.web.responder.WebModule;

@SingletonComponent ("oxygen8ApiServletModule")
public
class Oxygen8ApiServletModule
	implements WebModule {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	@Named
	WebFile oxygen8InboundFile;

	// prototype dependencies

	@PrototypeDependency
	Provider <ApiFile> apiFileProvider;

	// ================================================================ entries

	final
	RegexpPathHandler.Entry routeEntry =
		new RegexpPathHandler.Entry (
			"/route/([0-9]+)/([^/]+)") {

			@Override
			protected
			WebFile handle (
					Matcher matcher) {

				requestContext.request (
					"routeId",
					parseIntegerRequired (
						matcher.group (1)));

				return defaultFiles.get (
					matcher.group (2));

			}

		};

	// =========================================================== path handler

	final
	PathHandler pathHandler =
		new RegexpPathHandler (routeEntry);

	// ================================================================== files

	Map <String, WebFile> defaultFiles;

	// ========================================================= servlet module

	@Override
	public
	Map <String, PathHandler> paths () {

		return ImmutableMap. <String, PathHandler> builder ()

			.put (
				"/oxygen8",
				pathHandler)

			.build ();

	}

	@Override
	public
	Map <String, WebFile> files () {
		return null;
	}

	// ========================================================= servlet module

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"setup");

		defaultFiles =
			ImmutableMap. <String, WebFile> builder ()

			.put (
				"report",
				apiFileProvider.get ()

					.postActionName (
						taskLogger,
						"oxygen8RouteReportAction")

			)

			.put (
				"in",
				oxygen8InboundFile)

			.build ();

	}

}