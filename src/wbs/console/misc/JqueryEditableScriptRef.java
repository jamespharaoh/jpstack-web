package wbs.console.misc;

import lombok.NonNull;

import wbs.console.html.ScriptRef;
import wbs.console.request.ConsoleRequestContext;

public
class JqueryEditableScriptRef
	extends ScriptRef {

	@Override
	public
	String getType () {
		return "text/javascript";
	}

	@Override
	public
	String getUrl (
			@NonNull ConsoleRequestContext requestContext) {

		return requestContext.resolveApplicationUrl (
			path);

	}

	public static final
	JqueryEditableScriptRef instance =
		new JqueryEditableScriptRef ();

	public static final
	String path =
		"/js/jquery-jeditable-dea6556.js";

}
