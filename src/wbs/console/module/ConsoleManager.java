package wbs.console.module;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;

import com.google.common.base.Optional;

import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextStuff;
import wbs.console.context.ConsoleContextType;
import wbs.console.supervisor.SupervisorConfig;
import wbs.console.tab.ConsoleContextTab;

import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;

public
interface ConsoleManager {

	void changeContext (
			TaskLogger taskLogger,
			ConsoleContext context,
			String contextPartSuffix);

	ConsoleContext context (
			String name,
			boolean required);

	ConsoleContextType contextType (
			String name,
			boolean required);

	ConsoleContextTab tab (
			String name,
			boolean required);

	void runPostProcessors (
			Transaction parentTransaction,
			String name,
			ConsoleContextStuff contextStuff);

	Optional <ConsoleContext> contextWithParentOfType (
			ConsoleContext parentContext,
			ConsoleContextType contextType,
			boolean required);

	Optional <ConsoleContext> contextWithParentOfType (
			ConsoleContext parentContext,
			ConsoleContextType contextType);

	ConsoleContext contextWithParentOfTypeRequired (
			ConsoleContext parentContext,
			ConsoleContextType contextType);

	Optional<ConsoleContext> contextWithoutParentOfType (
			ConsoleContextType contextType,
			boolean required);

	ConsoleContext contextWithoutParentOfTypeRequired (
			ConsoleContextType contextType);

	Optional <ConsoleContext> contextWithoutParentOfType (
			ConsoleContextType contextType);

	String resolveLocalFile (
			Transaction parentTransaction,
			ConsoleContextStuff contextStuff,
			ConsoleContext consoleContext,
			String localFile);

	Optional <ConsoleContext> relatedContext (
			TaskLogger parentTaskLogger,
			ConsoleContext sourceContext,
			ConsoleContextType targetContextType,
			boolean required);

	default
	Optional <ConsoleContext> relatedContext (
			TaskLogger parentTaskLogger,
			ConsoleContext sourceContext,
			ConsoleContextType targetContextType) {

		return relatedContext (
			parentTaskLogger,
			sourceContext,
			targetContextType,
			false);

	}

	default
	ConsoleContext relatedContextRequired (
			TaskLogger taskLogger,
			ConsoleContext sourceContext,
			ConsoleContextType targetContextType) {

		return optionalGetRequired (
			relatedContext (
				taskLogger,
				sourceContext,
				targetContextType,
				true));

	}


	SupervisorConfig supervisorConfig (
			String name);

}
