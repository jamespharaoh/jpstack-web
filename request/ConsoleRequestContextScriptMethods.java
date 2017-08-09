package wbs.console.request;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOrElseRequired;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringFormatArray;
import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockClose;
import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockOpen;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

import wbs.utils.string.FormatWriter;

public
interface ConsoleRequestContextScriptMethods
	extends ConsoleRequestContextCoreMethods {

	// methods

	default
	List <String> scripts () {

		return genericCastUnchecked (
			requestContext ().requestOrElseSet (
				"scripts",
				() -> new ArrayList <String> ()));

	}

	default
	void addScript (
			@NonNull String script) {

		scripts ().add (
			script);

	}


	default
	void addScriptFormat (
			@NonNull String ... arguments) {

		addScript (
			stringFormatArray (
				arguments));

	}

	default
	void flushScripts (
			@NonNull FormatWriter formatWriter) {

		State state =
			consoleRequestContextScriptMethodsState ();

		if (
			collectionIsNotEmpty (
				state.scripts)
		) {

			htmlScriptBlockOpen (
				formatWriter);

			state.scripts.forEach (
				script ->
					formatWriter.writeLineFormat (
						"%s",
						script));

			htmlScriptBlockClose (
				formatWriter);

			state.scripts.clear ();

		}

	}

	// state

	final static
	String STATE_KEY =
		"CONSOLE_REQUEST_CONTEXT_SCRIPT_METHODS_STATE";

	default
	State consoleRequestContextScriptMethodsState () {

		return optionalOrElseRequired (
			optionalCast (
				State.class,
				optionalFromNullable (
					requestContext ().request ().getAttribute (
						STATE_KEY))),
			() -> {

			State state =
				new State ();

			requestContext ().request ().setAttribute (
				STATE_KEY,
				state);

			return state;

		});

	}

	static
	class State {

		List <String> scripts =
			new ArrayList <String> ();

	}

}
