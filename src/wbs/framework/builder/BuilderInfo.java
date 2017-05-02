package wbs.framework.builder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.inject.Provider;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class BuilderInfo {

	Class <?> builderClass;
	Provider <?> builderProvider;

	Field parentField;
	Field targetField;
	Field sourceField;

	Method buildMethod;

}
