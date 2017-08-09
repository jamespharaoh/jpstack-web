package wbs.console.request;

import static wbs.utils.etc.Misc.requiredValue;
import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOrElseRequired;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.joinWithoutSeparator;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringFormatArray;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextStuff;

public
interface ConsoleRequestContextContextMethods
	extends ConsoleRequestContextCoreMethods {

	// console context

	default
	Optional <ConsoleContext> consoleContext () {

		State state =
			consoleRequestContextContextMethodsState ();

		return optionalFromNullable (
			state.consoleContext);

	}

	default
	ConsoleContext consoleContextRequired () {

		State state =
			consoleRequestContextContextMethodsState ();

		return requiredValue (
			state.consoleContext);

	}

	default
	void consoleContext (
			@NonNull ConsoleContext consoleContext) {

		State state =
			consoleRequestContextContextMethodsState ();

		state.consoleContext =
			consoleContext;

	}

	// console context stuff

	default
	Optional <ConsoleContextStuff> consoleContextStuff () {

		State state =
			consoleRequestContextContextMethodsState ();

		return optionalFromNullable (
			state.consoleContextStuff);

	}

	default
	ConsoleContextStuff consoleContextStuffRequired () {

		State state =
			consoleRequestContextContextMethodsState ();

		return requiredValue (
			state.consoleContextStuff);

	}

	default
	void consoleContextStuff (
			@NonNull ConsoleContextStuff consoleContextStuff) {

		State state =
			consoleRequestContextContextMethodsState ();

		state.consoleContextStuff =
			consoleContextStuff;

	}

	default
	Object stuff (
			@NonNull String key) {

		return consoleContextStuffRequired ().get (
			key);

	}

	default
	Long stuffIntegerRequired (
			@NonNull String key) {

		return genericCastUnchecked (
			consoleContextStuffRequired ().get (
				key));

	}

	default
	Optional <Long> stuffInteger (
			@NonNull String key) {

		return optionalCast (
			Long.class,
			optionalFromNullable (
				consoleContextStuffRequired ().get (
					key)));

	}

	default
	String stuffString (
			@NonNull String key) {

		return genericCastUnchecked (
			consoleContextStuffRequired ().get (
				key));

	}

	default
	void grant (
			@NonNull String string) {

		consoleContextStuffRequired ().grant (
			string);

	}

	default
	boolean canContext (
			@NonNull String... privKeys) {

		return consoleContextStuffRequired ().can (
			privKeys);

	}

	// foreign context path

	default
	Optional <String> foreignContextPath () {

		State state =
			consoleRequestContextContextMethodsState ();

		return optionalFromNullable (
			state.foreignContextPath);

	}

	default
	String foreignContextPathRequired () {

		State state =
			consoleRequestContextContextMethodsState ();

		return requiredValue (
			state.foreignContextPath);

	}

	default
	void foreignContextPath (
			@NonNull String foreignContextPath) {

		State state =
			consoleRequestContextContextMethodsState ();

		state.foreignContextPath =
			foreignContextPath;

	}

	// changed context path

	default
	Optional <String> changedContextPath () {

		State state =
			consoleRequestContextContextMethodsState ();

		return optionalFromNullable (
			state.changedContextPath);

	}

	default
	String changedContextPathRequired () {

		State state =
			consoleRequestContextContextMethodsState ();

		return requiredValue (
			state.changedContextPath);

	}

	default
	void changedContextPath (
			@NonNull String changedContextPath) {

		State state =
			consoleRequestContextContextMethodsState ();

		state.changedContextPath =
			changedContextPath;

	}

	// resolve context url

	default
	String resolveContextUrl (
			@NonNull String contextUrl) {

		if (foreignContextPath () == null) {

			throw new IllegalStateException (
				stringFormat (
					"Unable due resolve a context URL, as there is no current ",
					"context."));

		}

		return joinWithoutSeparator (
			foreignContextPathRequired (),
			contextUrl);

	}

	default
	String resolveContextUrlFormat (
			@NonNull String ... arguments) {

		return resolveContextUrl (
			stringFormatArray (
				arguments));

	}

	// resolve local url

	default
	String resolveLocalUrl (
			@NonNull String wantedPath) {

		if (! wantedPath.startsWith ("/")) {

			throw new IllegalArgumentException (
				stringFormat (
					"Invalid wanted path: %s",
					wantedPath));

		}

		if (
			optionalIsPresent (
				changedContextPath ())
		) {

			return joinWithoutSeparator (
				requestContext ().applicationPathPrefix (),
				changedContextPathRequired (),
				wantedPath);

		} else {

			return wantedPath.substring (1);

		}

	}

	default
	String resolveLocalUrlFormat (
			@NonNull String ... arguments) {

		return resolveLocalUrl (
			stringFormatArray (
				arguments));

	}

	// state

	final static
	String STATE_KEY =
		"CONSOLE_REQUEST_CONTEXT_CONTEXT_METHODS_STATE";

	default
	State consoleRequestContextContextMethodsState () {

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

		ConsoleContext consoleContext;
		ConsoleContextStuff consoleContextStuff;

		String foreignContextPath;
		String changedContextPath;

	}

}
