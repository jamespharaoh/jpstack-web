package wbs.framework.application.xml;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@DataClass ("bean")
public
class BeansBeanSpec {

	@DataParent
	@Getter @Setter
	BeansSpec beans;

	@DataAttribute (
		required = true)
	@Getter @Setter
	String name;

	@DataAttribute (
		name = "class",
		required = true)
	@Getter @Setter
	String className;

	@DataAttribute (
		name = "factory")
	@Getter @Setter
	String factoryClassName;

	@DataAttribute
	@Getter @Setter
	Boolean hide = false;

	@DataAttribute (
		required = true)
	@Getter @Setter
	String scope;

	@DataChildren (
		direct = true)
	@Getter @Setter
	List<BeansBeanPropertySpec> properties =
		new ArrayList<BeansBeanPropertySpec> ();

}
