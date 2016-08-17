package wbs.framework.schema.tool;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.SimpleExceptionLogger;

@SingletonComponent ("schemaToolComponents")
public 
class SchemaToolComponents {

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
