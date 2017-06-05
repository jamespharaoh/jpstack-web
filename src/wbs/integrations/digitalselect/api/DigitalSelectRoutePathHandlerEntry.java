package wbs.integrations.digitalselect.api;

import static wbs.utils.etc.NumberUtils.parseIntegerRequired;

import java.util.Map;
import java.util.regex.Matcher;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.context.RequestContext;
import wbs.web.file.WebFile;
import wbs.web.pathhandler.RegexpPathHandler;

@SingletonComponent ("digitalSelectRoutePathHandlerEntry")
public
class DigitalSelectRoutePathHandlerEntry
	extends RegexpPathHandler.Entry {

	// singleton dependencies

	@SingletonDependency
	DigitalSelectRouteReportFile digitalSelectRouteReportFile;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// constructoors

	public
	DigitalSelectRoutePathHandlerEntry () {
		super ("/route/([0-9]+)/([^/]+)");
	}

	@Override
	protected
	WebFile handle (
			Matcher matcher) {

		requestContext.request (
			"routeId",
			parseIntegerRequired (
				matcher.group (1)));

		return files.get (
			matcher.group (2));

	}

	Map <String, WebFile> files;

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

			files =
				ImmutableMap.<String, WebFile> builder ()
					.put ("report", digitalSelectRouteReportFile)
					.build ();

		}

	}

}
