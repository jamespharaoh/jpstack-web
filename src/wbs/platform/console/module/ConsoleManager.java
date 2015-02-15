package wbs.platform.console.module;

import javax.inject.Provider;

import wbs.framework.web.Responder;
import wbs.platform.console.context.ConsoleContext;
import wbs.platform.console.context.ConsoleContextStuff;
import wbs.platform.console.context.ConsoleContextType;
import wbs.platform.console.tab.ConsoleContextTab;
import wbs.platform.supervisor.SupervisorConfig;

public
interface ConsoleManager {

	void changeContext (
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

	Provider<Responder> responder (
			String name,
			boolean required);

	void runPostProcessors (
			String name,
			ConsoleContextStuff contextStuff);

	ConsoleContext contextWithParentOfType (
			ConsoleContext parentContext,
			ConsoleContextType contextType,
			boolean required);

	ConsoleContext contextWithoutParentOfType (
			ConsoleContextType contextType,
			boolean required);

	String resolveLocalFile (
			ConsoleContextStuff contextStuff,
			ConsoleContext consoleContext,
			String localFile);

	ConsoleContext relatedContext (
			ConsoleContext sourceContext,
			ConsoleContextType targetContextType);

	SupervisorConfig supervisorConfig (
			String name);

}
