package wbs.console.tab;

import lombok.NonNull;

import wbs.console.lookup.BooleanLookup;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

public
class LocalTab
	extends Tab {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

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
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getUrl");

		) {

			return requestContext.resolveLocalUrl (
				localUrl);

		}

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
