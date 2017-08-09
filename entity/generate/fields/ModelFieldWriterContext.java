package wbs.framework.entity.generate.fields;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.entity.meta.model.RecordSpec;

@Accessors (fluent = true)
@Data
public
class ModelFieldWriterContext {

	RecordSpec modelMeta;

	String recordClassName;

}
