package wbs.integrations.g8wave.api;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import wbs.framework.web.RequestContext;
import wbs.framework.web.WbsServlet;
import wbs.framework.web.WebFile;

public
class G8waveApiServlet
	extends WbsServlet {

	private static final
	long serialVersionUID =
		-6263296001265277885L;

	@Inject
	RequestContext requestContext;

	@Inject
	G8waveInFile g8waveInFile;

	private final static
	Map<String,WebFile> routeFiles =
		new HashMap<String,WebFile> ();

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
	WebFile processPath () {

		String path =
			requestContext.pathInfo ();

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
