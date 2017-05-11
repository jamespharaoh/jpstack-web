package wbs.framework.component.scaffold;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataIgnore;

@Accessors (fluent = true)
@DataClass ("wbs-build")
public
class BuildSpec {

	@DataAttribute
	@Getter @Setter
	String name;

	@DataChildren
	@Getter @Setter
	List <BuildPluginSpec> plugins;

	@DataChildren
	@Getter @Setter
	List <BuildLayerSpec> layers;

	@DataIgnore
	Object gitLinks = null;

}
