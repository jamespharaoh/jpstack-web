package wbs.framework.builder;

import java.util.Map;

import wbs.framework.component.manager.ComponentProvider;
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
			ComponentProvider <?> builderProvider);

	<Type>
	Factory addBuilders (
			TaskLogger parentTaskLogger,
			Map <Class <?>, ComponentProvider <Type>> builders);

	Builder <Context> create (
			TaskLogger parentTaskLogger);

}
