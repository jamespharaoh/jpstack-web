package wbs.framework.application.scaffold;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

import com.google.common.collect.ImmutableList;

@Accessors (fluent = true)
@DataClass ("dependencies")
public
class PluginDependenciesSpec {

	@DataParent
	@Getter @Setter
	PluginSpec plugin;

	@DataChildren (
		direct = true)
	@Getter @Setter
	List<PluginProjectDependencySpec> projects =
		ImmutableList.of ();

}
