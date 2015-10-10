package wbs.console.module;

import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextType;
import wbs.console.forms.FormFieldSet;
import wbs.console.supervisor.SupervisorConfig;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.ContextTabPlacement;
import wbs.framework.web.Responder;
import wbs.framework.web.ServletModule;
import wbs.framework.web.WebFile;

public
interface ConsoleModule
	extends ServletModule {

	List<ConsoleContextType> contextTypes ();

	List<ConsoleContext> contexts ();

	List<ConsoleContextTab> tabs ();

	Map<String,List<ContextTabPlacement>> tabPlacementsByContextType ();

	Map<String,WebFile> contextFiles ();

	Map<String,List<String>> contextFilesByContextType ();

	Map<String,Provider<Responder>> responders ();

	Map<String,FormFieldSet> formFieldSets ();

	Map<String,SupervisorConfig> supervisorConfigs ();

}
