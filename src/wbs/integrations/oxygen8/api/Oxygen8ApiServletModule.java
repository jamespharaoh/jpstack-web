package wbs.integrations.oxygen8.api;

import static wbs.utils.etc.NumberUtils.parseIntegerRequired;

import java.util.Map;
import java.util.regex.Matcher;

import javax.inject.Named;

import com.google.common.collect.ImmutableMap;

import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.web.PathHandler;
import wbs.framework.web.RegexpPathHandler;
import wbs.framework.web.RequestContext;
import wbs.framework.web.ServletModule;
import wbs.framework.web.WebFile;

@SingletonComponent ("oxygen8ApiServletModule")
public
class Oxygen8ApiServletModule
	implements ServletModule {

	// singleton dependencies

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	@Named
	WebFile oxygen8ReportFile;

	@SingletonDependency
	@Named
	WebFile oxygen8InboundFile;

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

		return ImmutableMap.<String,PathHandler>builder ()

			.put (
				"/oxygen8",
				pathHandler)

			.build ();

	}

	@Override
	public
	Map<String,WebFile> files () {
		return null;
	}

	// ========================================================= servlet module

	@NormalLifecycleSetup
	public
	void afterPropertiesSet () {

		defaultFiles =
			ImmutableMap.<String,WebFile>builder ()

			.put (
				"report",
				oxygen8ReportFile)

			.put (
				"in",
				oxygen8InboundFile)

			.build ();

	}

}