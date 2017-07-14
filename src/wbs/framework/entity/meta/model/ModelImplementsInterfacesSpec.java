package wbs.framework.entity.meta.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("implements-interfaces")
@PrototypeComponent ("modelImplementsInterfacesSpec")
public
class ModelImplementsInterfacesSpec
	implements ModelDataSpec {

	@DataChildren (
		direct = true)
	List <ModelImplementsInterfaceSpec> implementsInterfaces =
		new ArrayList<> ();

}
