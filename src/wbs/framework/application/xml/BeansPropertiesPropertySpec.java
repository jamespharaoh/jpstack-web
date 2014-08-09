package wbs.framework.application.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.context.BeanDefinition;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@DataClass ("properties-property")
public
class BeansPropertiesPropertySpec
	implements BeansBeanPropertySpec {

	@DataAncestor
	@Getter @Setter
	BeansSpec beans;

	@DataParent
	@Getter @Setter
	BeansBeanSpec bean;

	@DataAttribute
	@Getter @Setter
	String name;

	@DataChildren (direct = true)
	@Getter @Setter
	List<BeansPropertyValueSpec> properties =
		new ArrayList<BeansPropertyValueSpec> ();

	@Override
	public
	int register (
			BeanDefinition beanDefinition) {

		Properties properties =
			new Properties ();

		for (BeansPropertyValueSpec propertyValue
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
