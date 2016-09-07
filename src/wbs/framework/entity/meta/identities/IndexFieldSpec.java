package wbs.framework.entity.meta.identities;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelFieldSpec;
import wbs.framework.entity.meta.model.ModelMetaData;

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

	@DataAttribute (
		name = "cacheable")
	Boolean cacheable;

}
