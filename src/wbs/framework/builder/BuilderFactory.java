package wbs.framework.builder;

import java.util.Map;

import javax.inject.Provider;

import wbs.framework.logging.TaskLogger;

public
interface BuilderFactory <
	Factory extends BuilderFactory <Factory, Context>,
	Context
> {

	Factory contextClass (
			Class <Context> contextClass);

	Factory addBuilder (
			TaskLogger parentTaskLogger,
			Class <?> builderClass,
			Provider <?> builderProvider);

	<Type>
	Factory addBuilders (
			TaskLogger parentTaskLogger,
			Map <Class <?>, Provider <Type>> builders);

	Builder <Context> create (
			TaskLogger parentTaskLogger);

}
