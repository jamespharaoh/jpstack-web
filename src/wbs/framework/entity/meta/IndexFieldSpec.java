package wbs.framework.entity.meta;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("index-field")
@PrototypeComponent ("indexFieldSpec")
@ModelMetaData
public
class IndexFieldSpec
	implements ModelFieldSpec {

	@DataAttribute
	String name;

	@DataAttribute (
		name = "counter")
	String counterName;

	@DataAttribute (
		name = "column")
	String columnName;

}
