package wbs.framework.entity.generate;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.SimpleExceptionLogger;

@SingletonComponent ("modelGenerateComponents")
public 
class ModelGenerateComponents {

	// prototype dependencies

	@Inject
	Provider <SimpleExceptionLogger> simpleExceptionLoggerProvider;

	// components

	@SingletonComponent ("exceptionLogger")
	public
	ExceptionLogger exceptionLogger () {

		return simpleExceptionLoggerProvider.get (); 

	}

}
