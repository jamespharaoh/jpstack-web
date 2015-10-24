package wbs.framework.entity.build;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.entity.model.Model;
import wbs.framework.entity.model.ModelField;

@Accessors (fluent = true)
@Data
public
class ModelFieldBuilderTarget {

	Model model;

	List<ModelField> fields;
	Map<String,ModelField> fieldsByName;

}
