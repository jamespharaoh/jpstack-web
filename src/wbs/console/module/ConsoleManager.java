package wbs.console.module;

import javax.inject.Provider;

import com.google.common.base.Optional;

import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextStuff;
import wbs.console.context.ConsoleContextType;
import wbs.console.supervisor.SupervisorConfig;
import wbs.console.tab.ConsoleContextTab;
import wbs.framework.web.Responder;

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

	Optional<ConsoleContext> contextWithParentOfType (
			ConsoleContext parentContext,
			ConsoleContextType contextType,
			boolean required);

	Optional<ConsoleContext> contextWithParentOfType (
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

	Optional<ConsoleContext> contextWithoutParentOfType (
			ConsoleContextType contextType);

	String resolveLocalFile (
			ConsoleContextStuff contextStuff,
			ConsoleContext consoleContext,
			String localFile);

	Optional<ConsoleContext> relatedContext (
			ConsoleContext sourceContext,
			ConsoleContextType targetContextType,
			boolean required);

	Optional<ConsoleContext> relatedContext (
			ConsoleContext sourceContext,
			ConsoleContextType targetContextType);

	ConsoleContext relatedContextRequired (
			ConsoleContext sourceContext,
			ConsoleContextType targetContextType);

	SupervisorConfig supervisorConfig (
			String name);

}
