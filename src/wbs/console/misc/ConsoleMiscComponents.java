package wbs.console.misc;

import javax.inject.Named;
import javax.inject.Provider;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.annotations.UninitializedDependency;
import wbs.framework.utils.ThreadManager;
import wbs.framework.utils.ThreadManagerImplementation;
import wbs.framework.web.DelegatingPathHandler;
import wbs.framework.web.PathHandler;

@SingletonComponent ("consoleMiscComponents")
public
class ConsoleMiscComponents {

	// unitialized dependencies

	@UninitializedDependency
	Provider <DelegatingPathHandler> delegatingPathHandlerProvider;

	@UninitializedDependency
	Provider <ThreadManagerImplementation> threadManagerImplementationProvider;

	// components

	@SingletonComponent ("threadManager")
	public
	ThreadManager threadManager () {

		return threadManagerImplementationProvider.get ()

			.exceptionTypeCode (
				"console");

	}

	@SingletonComponent ("rootPathHandler")
	@Named
	public
	PathHandler rootPathHandler () {

		return delegatingPathHandlerProvider.get ();

	}

}
