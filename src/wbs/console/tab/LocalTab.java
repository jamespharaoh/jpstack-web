package wbs.console.tab;

import wbs.console.lookup.BooleanLookup;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.SingletonDependency;

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
	String getUrl () {

		return requestContext.resolveLocalUrl (
			localUrl);

	}

	@Override
	public
	boolean isAvailable () {

		for (BooleanLookup lookup
				: lookups) {

			if (! lookup.lookup (
					requestContext.contextStuff ()))
				return false;

		}

		return true;

	}

}
