package wbs.console.misc;

import wbs.console.html.ScriptRef;
import wbs.console.request.ConsoleRequestContext;

public
class JqueryScriptRef
	extends ScriptRef {

	private
	JqueryScriptRef () {
	}

	@Override
	public
	String getType () {
		return "text/javascript";
	}

	@Override
	public
	String getUrl (
			ConsoleRequestContext requestContext) {

		return requestContext.resolveApplicationUrl (
			path);

	}

	public static final
	JqueryScriptRef instance =
		new JqueryScriptRef ();

	public static final
	String path =
		"/js/jquery-1.11.2.js";

}
