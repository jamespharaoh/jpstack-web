package wbs.framework.entity.meta.model;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("implements-interface")
@PrototypeComponent ("modelImplementsInterfaceSpec")
public
class ModelImplementsInterfaceSpec
	implements ModelDataSpec {

	@DataAncestor
	ModelMetaSpec modelMeta;

	@DataAttribute (
		name = "package")
	String packageName;

	@DataAttribute (
		required = true)
	String name;

}
