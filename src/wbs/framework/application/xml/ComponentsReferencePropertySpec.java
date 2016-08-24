package wbs.framework.application.xml;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.application.context.ComponentDefinition;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@DataClass ("reference-property")
public
class ComponentsReferencePropertySpec
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
	String target;

	@Override
	public
	int register (
			ComponentDefinition beanDefinition) {

		beanDefinition.addReferenceProperty (
			name,
			target);

		return 0;

	}

}
