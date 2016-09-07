package wbs.framework.entity.build;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.entity.meta.model.ModelMetaSpec;
import wbs.framework.entity.model.ModelField;

@Accessors (fluent = true)
@Data
public
class ModelFieldBuilderContext {

	ModelMetaSpec modelMeta;

	Class<?> recordClass;

	ModelField parentModelField;

}
