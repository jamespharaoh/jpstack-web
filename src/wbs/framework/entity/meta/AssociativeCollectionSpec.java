package wbs.framework.entity.meta;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("associative-collection")
@ModelMetaData
@PrototypeComponent ("associativeCollectionSpec")
public
class AssociativeCollectionSpec
	implements ModelCollectionSpec {

	@DataAncestor
	ModelMetaSpec model;

	@DataAttribute
	String name;

	@DataAttribute (
		value = "type",
		required = true)
	String typeName;

	@DataAttribute (
		value = "table",
		required = true)
	String tableName;

	@DataAttribute (
		value = "value-column")
	String valueColumnName;

	@DataAttribute
	String whereSql;

	@DataAttribute
	String orderSql;

}
