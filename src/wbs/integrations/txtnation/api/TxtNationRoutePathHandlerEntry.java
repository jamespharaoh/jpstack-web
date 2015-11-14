package wbs.integrations.txtnation.api;

import java.util.Map;
import java.util.regex.Matcher;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.web.RegexpPathHandler;
import wbs.framework.web.RequestContext;
import wbs.framework.web.WebFile;

@SingletonComponent ("txtNationRoutePathHandlerEntry")
public
class TxtNationRoutePathHandlerEntry
	extends RegexpPathHandler.Entry {

	@Inject
	RequestContext requestContext;

	@Inject
	TxtNationRouteInFile txtNationRouteInFile;

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

	Map<String,WebFile> files;

	@PostConstruct
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
