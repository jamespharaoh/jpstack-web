package wbs.integrations.txtnation.api;

import java.util.Map;
import java.util.regex.Matcher;

import com.google.common.collect.ImmutableMap;

import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;

import wbs.web.context.RequestContext;
import wbs.web.file.WebFile;
import wbs.web.pathhandler.RegexpPathHandler;

@SingletonComponent ("txtNationRoutePathHandlerEntry")
public
class TxtNationRoutePathHandlerEntry
	extends RegexpPathHandler.Entry {

	// singleton dependencies

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	TxtNationRouteInFile txtNationRouteInFile;

	// constructors

	public
	TxtNationRoutePathHandlerEntry () {

		super (
			"/route/([0-9]+)/([^/]+)");

	}

	@Override
	protected
	WebFile handle (
			Matcher matcher) {

		requestContext.request (
			"routeId",
			Integer.parseInt (matcher.group (1)));

		return files.get (
			matcher.group (2));

	}

	Map <String, WebFile> files;

	@NormalLifecycleSetup
	public
	void init () {

		files =
			ImmutableMap.<String,WebFile>builder ()

			.put (
				"in",
				txtNationRouteInFile)

			.build ();

	}

}
