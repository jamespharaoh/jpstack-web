package wbs.console.request;

import static wbs.utils.etc.Misc.requiredValue;
import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOrElse;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.tab.Tab;
import wbs.console.tab.TabContext;
import wbs.console.tab.TabList;

public
interface ConsoleRequestContextTabMethods
	extends ConsoleRequestContextCoreMethods {

	// tab context

	default
	Optional <TabContext> tabContext () {

		State state =
			consoleRequestContextTabMethodsState ();

		return optionalFromNullable (
			state.tabContext);

	}

	default
	TabContext tabContextRequired () {

		State state =
			consoleRequestContextTabMethodsState ();

		return requiredValue (
			state.tabContext);

	}

	default
	TabContext setTabContext (
			@NonNull TabContext tabContext) {

		State state =
			consoleRequestContextTabMethodsState ();

		state.tabContext =
			tabContext;

		return tabContext;

	}

	default
	void addTabContext (
			@NonNull Tab parentTab,
			@NonNull String title,
			@NonNull TabList tabList) {

		tabContextRequired ().add (
			parentTab,
			title,
			tabList);

	}

	default
	void tabContext (
			@NonNull String title1,
			@NonNull TabList tabList1) {

		setTabContext (
			new TabContext (
				title1,
				tabList1));

	}

	// state

	final static
	String STATE_KEY =
		"CONSOLE_REQUEST_CONTEXT_TAB_METHODS_STATE";

	default
	State consoleRequestContextTabMethodsState () {

		return optionalOrElse (
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
		TabContext tabContext;
	}

}
