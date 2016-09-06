package wbs.framework.entity.generate.fields;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.codegen.JavaImportRegistry;
import wbs.framework.utils.formatwriter.FormatWriter;

@Accessors (fluent = true)
@Data
public
class ModelFieldWriterTarget {

	JavaImportRegistry imports;

	FormatWriter formatWriter;

}
