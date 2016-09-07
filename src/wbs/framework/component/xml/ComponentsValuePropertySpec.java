package wbs.framework.component.xml;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@DataClass ("value-property")
public
class ComponentsValuePropertySpec
	implements ComponentsComponentPropertySpec {

	@DataAncestor
	@Getter @Setter
	ComponentsSpec beans;

	@DataParent
	@Getter @Setter
	ComponentsComponentSpec bean;

	@DataAttribute
	@Getter @Setter
	String name;

	@DataAttribute
	@Getter @Setter
	String value;

	@Override
	public
	int register (
			ComponentDefinition beanDefinition) {

		beanDefinition.addValueProperty (
			name,
			value);

		return 0;

	}

}
