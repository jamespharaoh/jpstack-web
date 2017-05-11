package wbs.console.misc;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.UninitializedDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.TaskLogger;

import wbs.utils.thread.ThreadManager;
import wbs.utils.thread.ThreadManagerImplementation;

@SingletonComponent ("threadManager")
public
class ConsoleThreadManager
	implements ComponentFactory <ThreadManager> {

	// unitialized dependencies

	@UninitializedDependency
	Provider <ThreadManagerImplementation> threadManagerImplementationProvider;

	// implemetation

	@Override
	public
	ThreadManager makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		return threadManagerImplementationProvider.get ()

			.exceptionTypeCode (
				"console");

	}

}
