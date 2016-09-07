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
	ComponentDefinition componentDefinition;

	@DataAttribute
	@Getter @Setter
	Class <?> fieldDeclaringClass;

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

	@DataAttribute
	@Getter @Setter
	Boolean initialized;

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
