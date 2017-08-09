package wbs.console.html;

import wbs.console.request.ConsoleRequestContext;

/**
 * Stores an html external script reference, designed for storing in a set (ie
 * identical references are "equal"). Consists of a source url and a type.
 */
public abstract
class ScriptRef {

	public abstract
	String getUrl (
			ConsoleRequestContext requestContext);

	public abstract
	String getType ();

}
