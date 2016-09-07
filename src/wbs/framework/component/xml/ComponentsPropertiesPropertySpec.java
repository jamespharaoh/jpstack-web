package wbs.framework.component.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@DataClass ("properties-property")
public
class ComponentsPropertiesPropertySpec
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

	@DataChildren (direct = true)
	@Getter @Setter
	List<ComponentPropertyValueSpec> properties =
		new ArrayList<ComponentPropertyValueSpec> ();

	@Override
	public
	int register (
			ComponentDefinition beanDefinition) {

		Properties properties =
			new Properties ();

		for (ComponentPropertyValueSpec propertyValue
				: this.properties) {

			properties.setProperty (
				propertyValue.name (),
				propertyValue.value ());

		}

		beanDefinition.addValueProperty (
			name,
			properties);

		return 0;

	}

}
