package wbs.integrations.digitalselect.api;

import java.util.Map;
import java.util.regex.Matcher;

import javax.annotation.PostConstruct;

import com.google.common.collect.ImmutableMap;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.web.RegexpPathHandler;
import wbs.framework.web.RequestContext;
import wbs.framework.web.WebFile;

@SingletonComponent ("digitalSelectRoutePathHandlerEntry")
public
class DigitalSelectRoutePathHandlerEntry
	extends RegexpPathHandler.Entry {

	// singleton dependencies

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	DigitalSelectRouteReportFile digitalSelectRouteReportFile;

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
				.put ("report", digitalSelectRouteReportFile)
				.build ();

	}

}
