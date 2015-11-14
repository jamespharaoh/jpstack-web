package wbs.framework.entity.generate;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.entity.meta.ModelMetaSpec;

@Accessors (fluent = true)
@Data
public
class ModelFieldWriterContext {

	ModelMetaSpec modelMeta;

	String recordClassName;

}
