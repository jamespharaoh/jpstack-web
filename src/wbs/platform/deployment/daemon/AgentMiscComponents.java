package wbs.platform.deployment.daemon;

import javax.inject.Provider;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.UninitializedDependency;
import wbs.framework.component.config.WbsSpecialConfig;

import wbs.utils.thread.ThreadManager;
import wbs.utils.thread.ThreadManagerImplementation;

@SingletonComponent ("agentMiscComponents")
public
class AgentMiscComponents {

	// unitialized dependencies

	@UninitializedDependency
	Provider <ThreadManagerImplementation> threadManagerImplementationProvider;

	// components

	@SingletonComponent ("threadManager")
	public
	ThreadManager threadManager () {

		return threadManagerImplementationProvider.get ()

			.exceptionTypeCode (
				"agent");

	}

	@SingletonComponent ("wbsSpecialConfig")
	public
	WbsSpecialConfig wbsSpecialConfig () {

		return new WbsSpecialConfig ()

			.assumeNegativeCache (
				false);

	}

}
