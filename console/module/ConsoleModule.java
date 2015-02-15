package wbs.platform.console.module;

import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import wbs.framework.web.Responder;
import wbs.framework.web.ServletModule;
import wbs.framework.web.WebFile;
import wbs.platform.console.context.ConsoleContext;
import wbs.platform.console.context.ConsoleContextType;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.tab.ConsoleContextTab;
import wbs.platform.console.tab.ContextTabPlacement;
import wbs.platform.supervisor.SupervisorConfig;

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
