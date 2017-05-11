package wbs.framework.component.scaffold;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
public
class PluginManager {

	// state

	@Getter @Setter
	List <PluginSpec> plugins;

	@Getter @Setter
	Map <String, PluginModelSpec> pluginModelsByName;

	@Getter @Setter
	Map <String, PluginEnumTypeSpec> pluginEnumTypesByName;

	@Getter @Setter
	Map <String, PluginCustomTypeSpec> pluginCustomTypesByName;

}
