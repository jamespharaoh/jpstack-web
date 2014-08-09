package wbs.framework.application.context;

import java.lang.reflect.Type;
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
@DataClass
public
class InjectedProperty {

	@DataParent
	@Getter @Setter
	BeanDefinition beanDefinition;

	@DataAttribute
	@Getter @Setter
	Class<?> fieldDeclaringClass;

	@DataAttribute
	@Getter @Setter
	String fieldName;

	@DataAttribute
	@Getter @Setter
	Type finalType;

	@DataAttribute
	@Getter @Setter
	Type injectType;

	@DataAttribute
	@Getter @Setter
	Type targetType;

	@DataAttribute
	@Getter @Setter
	CollectionType collectionType =
		CollectionType.single;

	@DataAttribute
	@Getter @Setter
	Boolean provider;

	@DataChildren
	@Getter @Setter
	List<String> targetBeanNames =
		new ArrayList<String> ();

	public static
	enum CollectionType {

		single,
		list,
		beanNameMap,
		beanClassMap;

	}

}
