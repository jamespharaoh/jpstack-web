package wbs.framework.component.xml;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@DataClass ("property")
public
class ComponentPropertyValueSpec {

	@DataAncestor
	@Getter @Setter
	ComponentsSpec beans;

	@DataAncestor
	@Getter @Setter
	ComponentsComponentSpec bean;

	@DataParent
	@Getter @Setter
	ComponentsPropertiesPropertySpec propertiesProperty;

	@DataAttribute (required = true)
	@Getter @Setter
	String name;

	@DataAttribute (required = true)
	@Getter @Setter
	String value;

}
