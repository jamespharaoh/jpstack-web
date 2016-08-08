package wbs.console.context;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.Random;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

import wbs.console.html.ScriptRef;
import wbs.console.request.ConsoleRequestContext;

@EqualsAndHashCode (
	of = { "applicationSource", "type" },
	callSuper = false
)
public
class ConsoleApplicationScriptRef
	extends ScriptRef {

	protected
	String applicationSource;

	protected
	String type;

	public
	ConsoleApplicationScriptRef (
			@NonNull String applicationSource,
			@NonNull String type) {

		this.applicationSource = applicationSource;
		this.type = type;

	}

	public static
	ConsoleApplicationScriptRef javascript (
			String applicationSource) {

		return new ConsoleApplicationScriptRef (
			applicationSource,
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

		return requestContext.resolveApplicationUrl (
			stringFormat (
				"%s",
				applicationSource,
				"?v=%u",
				reloadHack));

	}

	@Override
	public
	String getType () {

		return type;

	}

}
