package wbs.console.tab;

import lombok.NonNull;

import wbs.console.lookup.BooleanLookup;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

public
class LocalTab
	extends Tab {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	private final
	String localUrl;

	private final
	BooleanLookup[] lookups;

	// constructors

	public
	LocalTab (
			String defaultLabel,
			String newLocalUrl,
			BooleanLookup... newLookups) {

		super (
			defaultLabel);

		localUrl =
			newLocalUrl;

		lookups =
			newLookups;

	}

	@Override
	public
	String getUrl (
			@NonNull TaskLogger parentTaskLogger) {

		return requestContext.resolveLocalUrl (
			localUrl);

	}

	@Override
	public
	boolean isAvailable () {

		for (
			BooleanLookup lookup
				: lookups
		) {

			if (
				! lookup.lookup (
					requestContext.consoleContextStuffRequired ())
			) {
				return false;
			}

		}

		return true;

	}

}
