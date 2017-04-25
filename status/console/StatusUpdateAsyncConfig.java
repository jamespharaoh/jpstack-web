package wbs.platform.status.console;

import javax.inject.Provider;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.UninitializedDependency;

import wbs.platform.core.console.ConsoleAsyncSubscription;

@SingletonComponent ("statusUpdateAsyncConfig")
public
class StatusUpdateAsyncConfig {

	// singleton dependencies

	@SingletonDependency
	StatusUpdateAsyncHelper statusUpdateAsyncHelper;

	// unitialized dependencies

	@UninitializedDependency
	Provider <ConsoleAsyncSubscription <Object>>
	consoleAsyncSubscriptionProvider;

	// components

	@SingletonComponent ("statusUpdateAsyncEndpoint")
	public
	ConsoleAsyncSubscription <Object> statusUpdateAsyncEndpoint () {

		return consoleAsyncSubscriptionProvider.get ()

			.helper (
				statusUpdateAsyncHelper)

		;

	}

}
