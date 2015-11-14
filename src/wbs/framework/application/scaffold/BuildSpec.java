package wbs.framework.application.scaffold;

import java.util.ArrayList;
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
	List<BuildPluginSpec> plugins =
		new ArrayList<BuildPluginSpec> ();

	@DataIgnore
	Object gitLinks = null;

}
