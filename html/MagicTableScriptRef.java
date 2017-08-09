package wbs.console.html;

import wbs.console.request.ConsoleRequestContext;

public
class MagicTableScriptRef
	extends ScriptRef {

	private
	MagicTableScriptRef () {
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
	MagicTableScriptRef instance =
		new MagicTableScriptRef ();

	public static final
	String path =
		"/js/magic-table.js";

}
