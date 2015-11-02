package wbs.framework.entity.meta;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("children-mapping")
@PrototypeComponent ("childrenMappingSpec")
@ModelMetaData
public
class ChildrenMappingSpec
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
		name = "join-column")
	String joinColumnName;

	@DataAttribute (
		name = "map-column",
		required = true)
	String mapColumnName;

	@DataAttribute (
		required = true)
	String mapType;

	@DataAttribute
	String whereSql;

	@DataAttribute
	String orderSql;

}
