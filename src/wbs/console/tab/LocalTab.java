package wbs.console.tab;

import javax.inject.Inject;

import wbs.console.lookup.BooleanLookup;
import wbs.console.request.ConsoleRequestContext;

public
class LocalTab
	extends Tab {

	@Inject
	ConsoleRequestContext requestContext;

	private final
	String localUrl;

	private final
	BooleanLookup[] lookups;

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
