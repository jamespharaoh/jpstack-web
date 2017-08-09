package wbs.framework.component.registry;

import java.lang.reflect.Field;
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
@DataClass ("injected-property")
public
class InjectedProperty {

	@DataParent
	@Getter @Setter
	ComponentDefinition componentDefinition;

	@DataAttribute
	@Getter @Setter
	Field field;

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
	Boolean prototype;

	@DataAttribute
	@Getter @Setter
	Boolean weak;

	@DataChildren
	@Getter @Setter
	List <String> targetComponentNames =
		new ArrayList<> ();

	public static
	enum CollectionType {

		single,
		list,
		componentNameMap,
		componentClassMap;

	}

}
