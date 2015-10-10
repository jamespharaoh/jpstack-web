package wbs.console.context;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Random;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import wbs.console.html.ScriptRef;
import wbs.console.request.ConsoleRequestContext;

@EqualsAndHashCode (
	of = { "contextSource", "type" },
	callSuper = false
)
public
class ConsoleContextScriptRef
	extends ScriptRef {

	protected
	String contextSource;

	protected
	String type;

	public
	ConsoleContextScriptRef (
			@NonNull String src,
			@NonNull String type) {

		this.contextSource = src;
		this.type = type;

	}

	public static
	ConsoleContextScriptRef javascript (
			String contextSource) {

		return new ConsoleContextScriptRef (
			contextSource,
			"text/javascript");

	}

	static
	int reloadHack =
		100 +
		new Random ().nextInt (
			999 - 100);

	@Override
	public
	String getUrl (
			ConsoleRequestContext requestContext) {

		return requestContext.resolveContextUrl (
			stringFormat (
				"%s",
				contextSource,
				"?v=%u",
				reloadHack));

	}

	@Override
	public
	String getType () {

		return type;

	}

}
