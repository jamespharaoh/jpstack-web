package wbs.framework.application.xml;

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
class BeansPropertyValueSpec {

	@DataAncestor
	@Getter @Setter
	BeansSpec beans;

	@DataAncestor
	@Getter @Setter
	BeansBeanSpec bean;

	@DataParent
	@Getter @Setter
	BeansPropertiesPropertySpec propertiesProperty;

	@DataAttribute (required = true)
	@Getter @Setter
	String name;

	@DataAttribute (required = true)
	@Getter @Setter
	String value;

}
