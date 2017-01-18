package wbs.integrations.g8wave.api;

import static wbs.utils.etc.OptionalUtils.optionalOrEmptyString;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.web.context.RequestContext;
import wbs.web.file.WebFile;
import wbs.web.servlet.WbsServlet;

public
class G8waveApiServlet
	extends WbsServlet {

	// singleton dependencies

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	G8waveInFile g8waveInFile;

	// state

	private final static
	Map <String, WebFile> routeFiles =
		new HashMap<> ();

	// implementation

	@Override
	public
	void init () {

		routeFiles.put (
			"/in",
			g8waveInFile);

	}

	private static final
	Pattern routePathPattern =
		Pattern.compile ("/route/(\\d+)(/[^/]+)");

	@Override
	protected
	WebFile processPath (
			@NonNull TaskLogger taskLogger) {

		String path =
			optionalOrEmptyString (
				requestContext.pathInfo ());

		Matcher matcher;

		matcher =
			routePathPattern.matcher (path);

		if (matcher.matches ()) {

			requestContext.request (
				"route_id",
				Integer.parseInt (matcher.group (1)));

			return routeFiles.get (
				matcher.group (2));

		}

		return null;

	}

}
