package wbs.framework.builder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.manager.ComponentProvider;

@Accessors (fluent = true)
@Data
public
class BuilderInfo {

	Class <?> builderClass;
	ComponentProvider <?> builderProvider;

	Field parentField;
	Field targetField;
	Field sourceField;

	Method buildMethod;

}
