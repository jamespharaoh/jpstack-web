package wbs.framework.entity.meta;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("associative-list")
@ModelMetaData
@PrototypeComponent ("associativeListSpec")
public
class AssociativeListSpec
	implements ModelCollectionSpec {

	@DataAncestor
	ModelMetaSpec model;

	@DataAttribute
	String name;

	@DataAttribute (
		name = "type",
		required = true)
	String typeName;

	@DataAttribute (
		name = "table",
		required = true)
	String tableName;

	@DataAttribute (
		name = "list-column")
	String listColumnName;

	@DataAttribute
	String whereSql;

	@DataAttribute
	String orderSql;

	@DataAttribute
	Boolean owned;

}
