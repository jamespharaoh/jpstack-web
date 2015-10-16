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
		value = "type",
		required = true)
	String typeName;

	@DataAttribute (
		value = "join-column")
	String joinColumnName;

	@DataAttribute (
		value = "map-column",
		required = true)
	String mapColumnName;

	@DataAttribute (
		value = "map-type",
		required = true)
	String mapType;

	@DataAttribute
	String orderSql;

}
